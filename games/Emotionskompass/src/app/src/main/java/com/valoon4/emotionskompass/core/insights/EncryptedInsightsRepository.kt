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
 * A separate, atomic AES-256-GCM encrypted insight store. No plaintext file,
 * database, preferences or automatic Android backup; never reset a bad key.
 */
class EncryptedInsightsRepository(context: Context) {
    private val file = File(context.filesDir, "private_personal_insights_v1.enc")
    private val atomic = AtomicFile(file)

    @Synchronized
    fun list(): List<PersonalInsight> {
        if (!file.exists() && !File(file.path + ".bak").exists()) return emptyList()
        val key = key(create = false)
            ?: throw IllegalStateException("Erkenntnis-Schlüssel fehlt. Die Daten bleiben unverändert.")
        return try {
            val envelope = atomic.openRead().use {
                BoundedJournalInput.read(it, PersonalInsightFormat.MAX_PLAINTEXT_BYTES + 128)
            }
            try {
                val clear = InsightEncryption.open(envelope, key)
                try { PersonalInsightFormat.decode(clear) }
                finally { clear.fill(0) }
            } finally { envelope.fill(0) }
        } catch (e: Exception) {
            throw IllegalStateException("Erkenntnisse konnten nicht sicher geöffnet werden. Nichts wurde überschrieben.", e)
        }
    }

    @Synchronized
    fun save(text: String, existingId: String? = null, journalId: String? = null): PersonalInsight {
        val original = list() // Authentication must succeed before any write.
        val now = System.currentTimeMillis()
        val existing = existingId?.let { id -> original.firstOrNull { it.id == id } }
        require(existingId == null || existing != null) { "Erkenntnis nicht mehr vorhanden." }
        val cleaned = text.trim()
        require(cleaned.isNotBlank() && cleaned.length <= PersonalInsightFormat.MAX_TEXT_LENGTH)
        val saved = if (existing == null) {
            PersonalInsightFormat.newInsight(cleaned, journalId, now)
        } else {
            existing.copy(text = cleaned, journalId = journalId, updatedAt = maxOf(now, existing.updatedAt))
        }
        write(if (existing == null) original + saved
            else original.map { if (it.id == existingId) saved else it })
        return saved
    }

    @Synchronized
    fun archive(id: String, archived: Boolean) {
        val old = list()
        require(old.any { it.id == id })
        val now = System.currentTimeMillis()
        write(old.map { if (it.id == id) it.copy(
            archived = archived, updatedAt = maxOf(now, it.updatedAt)
        ) else it })
    }

    @Synchronized
    fun delete(id: String) {
        val old = list()
        require(old.any { it.id == id })
        write(old.filterNot { it.id == id })
    }

    /** Keep user-written text, but unlink references to missing journal entries. */
    @Synchronized
    fun retainJournalReferences(validIds: Set<String>) {
        val old = list()
        val next = old.map {
            if (it.journalId != null && it.journalId !in validIds)
                it.copy(journalId = null, updatedAt = maxOf(System.currentTimeMillis(), it.updatedAt))
            else it
        }
        if (next != old) write(next)
    }

    /** Validated replacement only; callers must explicitly confirm cross-device restore. */
    @Synchronized
    fun replaceAll(items: List<PersonalInsight>) {
        list()
        write(items)
    }

    private fun write(items: List<PersonalInsight>) {
        val clear = PersonalInsightFormat.encode(items)
        try {
            val secret = key(create = true) ?: error("Android-Keystore nicht verfügbar.")
            val encrypted = InsightEncryption.seal(clear, secret)
            try {
                val stream = atomic.startWrite()
                try {
                    stream.write(encrypted)
                    atomic.finishWrite(stream)
                } catch (e: Exception) {
                    atomic.failWrite(stream)
                    throw e
                }
            } finally { encrypted.fill(0) }
        } finally { clear.fill(0) }
    }

    private fun key(create: Boolean): SecretKey? {
        val ks = KeyStore.getInstance("AndroidKeyStore")
        ks.load(null)
        (ks.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        if (!create) return null
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(KeyGenParameterSpec.Builder(
            KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).setKeySize(256)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .setUserAuthenticationRequired(false)
            .build())
        return generator.generateKey()
    }

    private companion object {
        const val KEY_ALIAS = "emotionskompass_private_insights_v1"
    }
}

internal object InsightEncryption {
    private val magic = "EKI1".toByteArray(Charsets.US_ASCII)
    private val aad = "com.valoon4.emotionskompass/insights/v1".toByteArray(Charsets.UTF_8)
    private const val NONCE_LENGTH = 12

    fun seal(clear: ByteArray, key: SecretKey): ByteArray {
        require(clear.size <= PersonalInsightFormat.MAX_PLAINTEXT_BYTES)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val nonce = cipher.iv
        require(nonce.size == NONCE_LENGTH)
        cipher.updateAAD(aad)
        return magic + nonce + cipher.doFinal(clear)
    }

    fun open(bytes: ByteArray, key: SecretKey): ByteArray {
        require(bytes.size in (4 + NONCE_LENGTH + 16)..(PersonalInsightFormat.MAX_PLAINTEXT_BYTES + 128))
        require(bytes.copyOfRange(0, 4).contentEquals(magic)) { "Falsches Erkenntnisformat" }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(
            128, bytes.copyOfRange(4, 4 + NONCE_LENGTH)
        ))
        cipher.updateAAD(aad)
        return cipher.doFinal(bytes.copyOfRange(4 + NONCE_LENGTH, bytes.size))
    }
}
