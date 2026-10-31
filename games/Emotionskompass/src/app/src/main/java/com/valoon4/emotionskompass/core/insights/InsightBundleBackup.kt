package com.valoon4.emotionskompass.core.insights

import com.valoon4.emotionskompass.core.journal.JournalBackup
import com.valoon4.emotionskompass.core.journal.JournalFormat
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/** Separate authenticated version-2 backup container. Old EKB1 remains readable. */
object InsightBundleBackup {
    private val magic = "EKB2".toByteArray(Charsets.US_ASCII)
    private val legacyMagic = "EKB1".toByteArray(Charsets.US_ASCII)
    private val aad = "com.valoon4.emotionskompass/portable-backup/v2".toByteArray()
    private const val SALT_BYTES = 16
    private const val NONCE_BYTES = 12
    private const val KDF_ITERATIONS = 250_000
    const val MAX_BACKUP_BYTES = InsightBundleFormat.MAX_PLAINTEXT_BYTES + 128

    fun seal(contents: ByteArray, password: CharArray): ByteArray {
        require(password.size >= JournalBackup.MIN_PASSPHRASE_LENGTH)
        require(contents.size <= InsightBundleFormat.MAX_PLAINTEXT_BYTES)
        // Verify structure before encryption so an export can never silently omit fields.
        InsightBundleFormat.decode(contents)
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val nonce = ByteArray(NONCE_BYTES).also { SecureRandom().nextBytes(it) }
        val key = derive(password, salt)
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, nonce))
            cipher.updateAAD(aad)
            magic + salt + nonce + cipher.doFinal(contents)
        } finally { key.encoded.fill(0) }
    }

    fun open(contents: ByteArray, password: CharArray): InsightBackupContents {
        require(contents.size >= 48 && contents.size <= MAX_BACKUP_BYTES)
        val header = contents.copyOfRange(0, 4)
        if (header.contentEquals(legacyMagic)) {
            // The old format has NO insight section. Callers must preserve existing
            // insights and must never interpret this as an empty insight collection.
            val old = JournalBackup.open(contents, password)
            return try { InsightBackupContents(JournalFormat.decode(old), null) }
            finally { old.fill(0) }
        }
        require(header.contentEquals(magic)) { "Unbekanntes Sicherungsformat" }
        val salt = contents.copyOfRange(4, 4 + SALT_BYTES)
        val nonce = contents.copyOfRange(4 + SALT_BYTES, 4 + SALT_BYTES + NONCE_BYTES)
        val key = derive(password, salt)
        val plaintext = try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, nonce))
            cipher.updateAAD(aad)
            cipher.doFinal(contents.copyOfRange(4 + SALT_BYTES + NONCE_BYTES, contents.size))
        } finally { key.encoded.fill(0) }
        return try { InsightBundleFormat.decode(plaintext) }
        finally { plaintext.fill(0) }
    }

    private fun derive(password: CharArray, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password, salt, KDF_ITERATIONS, 256)
        return try {
            val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec).encoded
            try { SecretKeySpec(bytes, "AES") }
            finally { bytes.fill(0) }
        } finally { spec.clearPassword() }
    }
}
