package com.valoon4.emotionskompass.core.insights

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import com.valoon4.emotionskompass.core.journal.BoundedJournalInput
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Local device-only, Keystore-backed encrypted EKI1 insight store.
 * This repository is NOT exposed in the UI until combined backup/restore is ready.
 */
class EncryptedPersonalInsightRepository(context: Context) {
    private val file = File(context.filesDir, "private_insights_v1.enc")
    private val atomic = AtomicFile(file)

    @Synchronized
    fun list(): List<PersonalInsight> {
        if (!file.exists() && !File(file.path + ".bak").exists()) return emptyList()
        val key = getKey(false) ?: throw IllegalStateException(
            "Erkenntnisschlüssel fehlt. Bestehende Daten bleiben unverändert."
        )
        val ciphertext = atomic.openRead().use {
            BoundedJournalInput.read(it, PersonalInsightFormat.MAX_BYTES + 128)
        }
        return try {
            val plaintext = open(ciphertext, key)
            try { PersonalInsightFormat.decode(plaintext) }
            finally { plaintext.fill(0) }
        } catch (error: Exception) {
            throw IllegalStateException("Erkenntnisse konnten nicht sicher geöffnet werden.", error)
        } finally { ciphertext.fill(0) }
    }

    @Synchronized
    fun save(item: PersonalInsight): List<PersonalInsight> {
        val existing = list()
        val next = PersonalInsightChanges.save(existing, item)
        write(next)
        return next
    }

    @Synchronized
    fun delete(id: String): List<PersonalInsight> {
        val existing = list()
        val next = PersonalInsightChanges.delete(existing, id)
        if (next.size != existing.size) write(next)
        return next
    }

    /** Only call after an authenticated EKB2 import and explicit confirmation. */
    @Synchronized
    fun replaceVerified(items: List<PersonalInsight>) {
        write(items)
    }

    private fun write(items: List<PersonalInsight>) {
        val plaintext = PersonalInsightFormat.encode(items)
        try {
            val key = getKey(true) ?: throw IllegalStateException("Kein sicherer Schlüssel")
            val encrypted = seal(plaintext, key)
            val output = atomic.startWrite()
            try {
                output.write(encrypted)
                atomic.finishWrite(output)
            } catch (error: Exception) {
                atomic.failWrite(output)
                throw error
            } finally { encrypted.fill(0) }
        } finally { plaintext.fill(0) }
    }

    private fun seal(bytes: ByteArray, key: SecretKey): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        require(iv.size == 12)
        cipher.updateAAD(AAD)
        return MAGIC + iv + cipher.doFinal(bytes)
    }

    private fun open(bytes: ByteArray, key: SecretKey): ByteArray {
        require(bytes.size in 32..(PersonalInsightFormat.MAX_BYTES + 128))
        require(bytes.copyOfRange(0, 4).contentEquals(MAGIC))
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key,
            GCMParameterSpec(128, bytes.copyOfRange(4, 16)))
        cipher.updateAAD(AAD)
        return cipher.doFinal(bytes.copyOfRange(16, bytes.size))
    }

    private fun getKey(create: Boolean): SecretKey? {
        val store = KeyStore.getInstance("AndroidKeyStore")
        store.load(null)
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        if (!create) return null
        val generator = KeyGenerator.getInstance("AES", "AndroidKeyStore")
        generator.init(KeyGenParameterSpec.Builder(
            KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).setKeySize(256).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .setUserAuthenticationRequired(false).build())
        return generator.generateKey()
    }

    private companion object {
        val MAGIC = "EKI1".toByteArray(Charsets.US_ASCII)
        val AAD = "com.valoon4.emotionskompass/insights/v1".toByteArray()
        const val KEY_ALIAS = "emotionskompass_private_insights_v1"
    }
}
