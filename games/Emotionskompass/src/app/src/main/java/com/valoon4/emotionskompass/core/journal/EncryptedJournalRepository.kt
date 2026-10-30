package com.valoon4.emotionskompass.core.journal

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import java.io.File
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * One private encrypted file, atomically replaced on each write.
 *
 * The AES key is non-exportable from AndroidKeyStore; no unencrypted journal file,
 * database, preferences, export or cloud backup is created. No automatic key reset
 * occurs if the data is corrupt or the key becomes unavailable.
 */
class EncryptedJournalRepository(context: Context) {
    private val file = File(context.filesDir, "private_journal_v1.enc")
    private val atomicFile = AtomicFile(file)

    @Synchronized
    fun list(): List<JournalEntry> {
        if (!file.exists() && !File(file.path + ".bak").exists()) return emptyList()
        val key = getKey(create = false)
            ?: throw IllegalStateException("Journal-Schlüssel fehlt. Die Einträge können nicht entschlüsselt werden.")
        return try {
            val envelope = atomicFile.openRead().use { source ->
                BoundedJournalInput.read(source, JournalFormat.MAX_PLAINTEXT_BYTES + 128)
            }
            try {
                val plaintext = JournalEncryption.open(envelope, key)
                try {
                    JournalFormat.decode(plaintext)
                } finally {
                    plaintext.fill(0)
                }
            } finally {
                envelope.fill(0)
            }
        } catch (e: Exception) {
            throw JournalAccessException("Das Tagebuch konnte nicht sicher geöffnet werden. Deine Daten wurden nicht verändert.", e)
        }
    }

    @Synchronized
    fun save(draft: com.valoon4.emotionskompass.core.checkin.CheckInDraft, existingId: String? = null): JournalEntry {
        val current = list() // Always decrypt successfully before writing; never overwrite damaged data.
        val now = System.currentTimeMillis()
        val old = existingId?.let { id -> current.firstOrNull { it.id == id } }
        if (existingId != null && old == null) throw IllegalArgumentException("Eintrag nicht gefunden")
        val entry = if (old != null) old.copy(updatedAt = maxOf(now, old.createdAt), draft = draft)
                    else JournalFormat.newEntry(draft, now)
        val next = if (old != null) current.map { if (it.id == old.id) entry else it } else current + entry
        write(next)
        return entry
    }

    @Synchronized
    fun delete(id: String): Boolean {
        val current = list()
        if (current.none { it.id == id }) return false
        write(current.filterNot { it.id == id })
        return true
    }

    @Synchronized
    fun deleteAll() {
        // Load and authenticate first; never silently discard unreadable encrypted material.
        list()
        write(emptyList())
    }

    /**
     * Export is never a raw copy of the device-keyed ciphertext. We decrypt in
     * RAM then re-encrypt using a fresh salt and passphrase-derived portable key.
     */
    @Synchronized
    fun encryptedBackup(passphrase: CharArray): ByteArray {
        val content = JournalFormat.encode(list())
        return try { JournalBackup.seal(content, passphrase) }
        finally { content.fill(0) }
    }

    /**
     * Destructive restore is an explicit, user-confirmed replacement.
     * Authenticate the backup and validate its format *before* touching the
     * existing encrypted journal; verify the current journal is readable too.
     */
    @Synchronized
    fun restoreBackup(bytes: ByteArray, passphrase: CharArray): Int {
        val clear = JournalBackup.open(bytes, passphrase)
        val restored = try { JournalFormat.decode(clear) }
        finally { clear.fill(0) }
        list() // Fails closed if the original encrypted data are unreadable.
        write(restored)
        return restored.size
    }

    private fun write(entries: List<JournalEntry>) {
        val plaintext = JournalFormat.encode(entries)
        val key = getKey(create = true)
            ?: throw JournalAccessException("Android konnte keinen sicheren Schlüssel bereitstellen.")
        val sealed = JournalEncryption.seal(plaintext, key)
        val stream = atomicFile.startWrite()
        try {
            stream.write(sealed)
            atomicFile.finishWrite(stream)
        } catch (e: Exception) {
            atomicFile.failWrite(stream)
            throw JournalAccessException("Speichern fehlgeschlagen. Bisherige Einträge bleiben erhalten.", e)
        } finally {
            plaintext.fill(0)
        }
    }

    private fun getKey(create: Boolean): SecretKey? {
        val ks = KeyStore.getInstance("AndroidKeyStore")
        ks.load(null)
        (ks.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        if (!create) return null
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setKeySize(256)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .setUserAuthenticationRequired(false)
                .build()
        )
        return generator.generateKey()
    }

    private companion object {
        const val KEY_ALIAS = "emotionskompass_private_journal_v1"
    }
}

class JournalAccessException(message: String, cause: Throwable? = null) : Exception(message, cause)
