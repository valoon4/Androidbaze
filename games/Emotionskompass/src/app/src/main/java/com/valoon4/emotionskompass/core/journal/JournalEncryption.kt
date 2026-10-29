package com.valoon4.emotionskompass.core.journal

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Versioned authenticated envelope; all metadata and notes are in the ciphertext. */
object JournalEncryption {
    private val magic = "EKJ1".toByteArray(Charsets.US_ASCII)
    private val aad = "com.valoon4.emotionskompass/journal/v1".toByteArray(Charsets.UTF_8)
    private const val IV_LENGTH = 12
    private const val TAG_LENGTH_BITS = 128

    fun seal(plaintext: ByteArray, key: SecretKey, random: SecureRandom = SecureRandom()): ByteArray {
        require(plaintext.size <= JournalFormat.MAX_PLAINTEXT_BYTES)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        // AndroidKeyStore generates the IV; do not supply a caller-generated IV for Keystore encryption.
        val iv = cipher.iv
        require(iv.size == IV_LENGTH)
        cipher.updateAAD(aad)
        return magic + iv + cipher.doFinal(plaintext)
    }

    fun open(envelope: ByteArray, key: SecretKey): ByteArray {
        require(envelope.size >= magic.size + IV_LENGTH + TAG_LENGTH_BITS / 8)
        require(envelope.copyOfRange(0, magic.size).contentEquals(magic)) {
            "Unknown encrypted journal format"
        }
        require(envelope.size <= JournalFormat.MAX_PLAINTEXT_BYTES + 128)
        val iv = envelope.copyOfRange(magic.size, magic.size + IV_LENGTH)
        val ciphertext = envelope.copyOfRange(magic.size + IV_LENGTH, envelope.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BITS, iv))
        cipher.updateAAD(aad)
        return cipher.doFinal(ciphertext)
    }
}
