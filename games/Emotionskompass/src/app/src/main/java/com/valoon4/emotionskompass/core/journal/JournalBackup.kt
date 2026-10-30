package com.valoon4.emotionskompass.core.journal

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Portable, passphrase-encrypted journal backup. Entire content (including IDs and
 * timestamps) is authenticated and encrypted. Different format/key from AndroidKeyStore.
 * The passphrase is never persisted; users must remember it to restore on another device.
 */
object JournalBackup {
    private val magic = "EKB1".toByteArray(Charsets.US_ASCII)
    private val aad = "com.valoon4.emotionskompass/portable-backup/v1".toByteArray()
    private const val SALT_SIZE = 16
    private const val IV_SIZE = 12
    private const val TAG_BITS = 128
    private const val ITERATIONS = 250_000
    private val random = SecureRandom()
    const val MIN_PASSPHRASE_LENGTH = 12
    const val MAX_BACKUP_BYTES = JournalFormat.MAX_PLAINTEXT_BYTES + 128

    fun seal(plaintext: ByteArray, passphrase: CharArray): ByteArray {
        require(passphrase.size >= MIN_PASSPHRASE_LENGTH) { "Passphrase muss mindestens 12 Zeichen enthalten." }
        require(plaintext.size <= JournalFormat.MAX_PLAINTEXT_BYTES)
        val salt = ByteArray(SALT_SIZE).also(random::nextBytes)
        val iv = ByteArray(IV_SIZE).also(random::nextBytes)
        val key = derive(passphrase, salt)
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_BITS, iv))
            cipher.updateAAD(aad)
            magic + salt + iv + cipher.doFinal(plaintext)
        } finally {
            key.encoded?.fill(0)
        }
    }

    fun open(backup: ByteArray, passphrase: CharArray): ByteArray {
        require(backup.size in (magic.size + SALT_SIZE + IV_SIZE + 16)..MAX_BACKUP_BYTES) {
            "Ungültige Sicherungsgröße."
        }
        require(backup.copyOfRange(0, magic.size).contentEquals(magic)) {
            "Unbekanntes Sicherungsformat."
        }
        val salt = backup.copyOfRange(magic.size, magic.size + SALT_SIZE)
        val iv = backup.copyOfRange(magic.size + SALT_SIZE, magic.size + SALT_SIZE + IV_SIZE)
        val ciphertext = backup.copyOfRange(magic.size + SALT_SIZE + IV_SIZE, backup.size)
        val key = derive(passphrase, salt)
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BITS, iv))
            cipher.updateAAD(aad)
            cipher.doFinal(ciphertext)
        } finally {
            key.encoded?.fill(0)
        }
    }

    private fun derive(password: CharArray, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password, salt, ITERATIONS, 256)
        return try {
            val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec).encoded
            try { SecretKeySpec(bytes, "AES") }
            finally { bytes.fill(0) }
        } finally {
            spec.clearPassword()
        }
    }
}
