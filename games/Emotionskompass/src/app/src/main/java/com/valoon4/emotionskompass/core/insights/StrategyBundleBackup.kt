package com.valoon4.emotionskompass.core.insights

import com.valoon4.emotionskompass.core.journal.JournalBackup
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * New portable EKB3 AES-GCM envelope; older EKB1/EKB2 are decoded through
 * the unchanged, well-tested InsightBundleBackup implementation.
 */
object StrategyBundleBackup {
    private val magic = "EKB3".toByteArray(Charsets.US_ASCII)
    private val aad = "com.valoon4.emotionskompass/portable-backup/v3".toByteArray()
    private const val SALT_BYTES = 16
    private const val NONCE_BYTES = 12
    private const val KDF_ITERATIONS = 250_000
    const val MAX_BACKUP_BYTES = StrategyBundleFormat.MAX_PLAINTEXT_BYTES + 128

    fun seal(plaintext: ByteArray, password: CharArray): ByteArray {
        require(password.size >= JournalBackup.MIN_PASSPHRASE_LENGTH)
        require(plaintext.size <= StrategyBundleFormat.MAX_PLAINTEXT_BYTES)
        StrategyBundleFormat.decode(plaintext)
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val nonce = ByteArray(NONCE_BYTES).also { SecureRandom().nextBytes(it) }
        val key = derive(password, salt)
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, nonce))
            cipher.updateAAD(aad)
            magic + salt + nonce + cipher.doFinal(plaintext)
        } finally { key.encoded.fill(0) }
    }

    fun open(contents: ByteArray, password: CharArray): StrategyBackupContents {
        require(contents.size in 48..MAX_BACKUP_BYTES)
        val header = contents.copyOfRange(0, 4)
        if (!header.contentEquals(magic)) {
            // No experimental data area in EKB1/EKB2. This must never delete
            // a local experimental data set on import.
            val old = InsightBundleBackup.open(contents, password)
            return StrategyBackupContents(old.journal, old.insights, null)
        }
        val salt = contents.copyOfRange(4, 4 + SALT_BYTES)
        val nonce = contents.copyOfRange(4 + SALT_BYTES, 4 + SALT_BYTES + NONCE_BYTES)
        val key = derive(password, salt)
        val plaintext = try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, nonce))
            cipher.updateAAD(aad)
            cipher.doFinal(contents.copyOfRange(4 + SALT_BYTES + NONCE_BYTES, contents.size))
        } finally { key.encoded.fill(0) }
        return try { StrategyBundleFormat.decode(plaintext) }
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
