package com.valoon4.emotionskompass.core.journal

import com.valoon4.emotionskompass.core.insights.PersonalInsight
import com.valoon4.emotionskompass.core.insights.PersonalInsightFormat
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/** EKB2 is a portable single encrypted envelope containing journal + insights. */
data class PersonalBackupContents(
    val journal: List<JournalEntry>,
    val insights: List<PersonalInsight>
)

object PersonalBackup {
    private val magic = "EKB2".toByteArray(Charsets.US_ASCII)
    private val payloadMagic = "EKP2".toByteArray(Charsets.US_ASCII)
    private val aad = "com.valoon4.emotionskompass/portable-backup/v2".toByteArray(Charsets.UTF_8)
    private val random = SecureRandom()
    const val MAX_BACKUP_BYTES = JournalFormat.MAX_PLAINTEXT_BYTES +
        PersonalInsightFormat.MAX_PLAINTEXT_BYTES + 256
    private const val SALT_LENGTH = 16
    private const val NONCE_LENGTH = 12
    private const val PBKDF2_ROUNDS = 250_000

    fun isV2(bytes: ByteArray): Boolean =
        bytes.size >= 4 && bytes.copyOfRange(0, 4).contentEquals(magic)

    fun seal(journal: List<JournalEntry>, insights: List<PersonalInsight>,
             passphrase: CharArray): ByteArray {
        require(passphrase.size >= JournalBackup.MIN_PASSPHRASE_LENGTH)
        val validIds = journal.map { it.id }.toSet()
        require(insights.all { it.journalId == null || it.journalId in validIds }) {
            "Erkenntnisverweis zeigt nicht auf einen Tagebucheintrag."
        }
        val j = JournalFormat.encode(journal)
        val i = PersonalInsightFormat.encode(insights)
        val payload = ByteBuffer.allocate(4 + 4 + j.size + 4 + i.size)
            .put(payloadMagic).putInt(j.size).put(j).putInt(i.size).put(i).array()
        j.fill(0); i.fill(0)
        try {
            val salt = ByteArray(SALT_LENGTH).also(random::nextBytes)
            val nonce = ByteArray(NONCE_LENGTH).also(random::nextBytes)
            val key = derive(passphrase, salt)
            try {
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, nonce))
                cipher.updateAAD(aad)
                return magic + salt + nonce + cipher.doFinal(payload)
            } finally { key.encoded.fill(0) }
        } finally { payload.fill(0) }
    }

    fun open(bytes: ByteArray, passphrase: CharArray): PersonalBackupContents {
        require(bytes.size in 48..MAX_BACKUP_BYTES) { "Ungültige Sicherungsgröße." }
        require(isV2(bytes)) { "Unbekanntes Sicherungsformat." }
        val salt = bytes.copyOfRange(4, 4 + SALT_LENGTH)
        val nonce = bytes.copyOfRange(4 + SALT_LENGTH, 4 + SALT_LENGTH + NONCE_LENGTH)
        val key = derive(passphrase, salt)
        val payload = try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, nonce))
            cipher.updateAAD(aad)
            cipher.doFinal(bytes.copyOfRange(4 + SALT_LENGTH + NONCE_LENGTH, bytes.size))
        } finally { key.encoded.fill(0) }
        try {
            require(payload.size >= 12)
            val reader = ByteBuffer.wrap(payload)
            val signature = ByteArray(4).also(reader::get)
            require(signature.contentEquals(payloadMagic))
            val jSize = reader.int
            require(jSize in 0..JournalFormat.MAX_PLAINTEXT_BYTES && jSize <= reader.remaining() - 4)
            val j = ByteArray(jSize).also(reader::get)
            val iSize = reader.int
            require(iSize in 0..PersonalInsightFormat.MAX_PLAINTEXT_BYTES && iSize == reader.remaining())
            val i = ByteArray(iSize).also(reader::get)
            return try {
                val journal = JournalFormat.decode(j)
                val insights = PersonalInsightFormat.decode(i)
                val validIds = journal.map { it.id }.toSet()
                require(insights.all { it.journalId == null || it.journalId in validIds }) {
                    "Ungültiger Tagebuchbezug im Backup."
                }
                PersonalBackupContents(journal, insights)
            } finally { j.fill(0); i.fill(0) }
        } finally { payload.fill(0) }
    }

    private fun derive(passphrase: CharArray, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(passphrase, salt, PBKDF2_ROUNDS, 256)
        return try {
            val raw = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec).encoded
            try { SecretKeySpec(raw, "AES") }
            finally { raw.fill(0) }
        } finally { spec.clearPassword() }
    }
}
