package com.valoon4.emotionskompass.core.insights

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import com.valoon4.emotionskompass.core.journal.BoundedJournalInput
import com.valoon4.emotionskompass.core.journal.EncryptedJournalRepository
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Coordinates EKB2 + legacy EKB1 imports and portable export.
 *
 * Two independent AtomicFiles cannot commit as one filesystem transaction.
 * Before touching either destination, a complete EKB2 target is written as
 * a third Keystore-encrypted, AtomicFile-backed redo intent. On the next
 * authenticated load it is replayed BEFORE any journal state is exposed.
 * A corrupt or undecryptable pending intent fails closed.
 *
 * Only the app's single ViewModel uses this coordinator for import/export.
 * Uncoordinated writers must not modify either destination during a restore.
 */
class CombinedBackupRepository(
    context: Context,
    private val journal: EncryptedJournalRepository,
    private val insights: EncryptedPersonalInsightRepository
) {
    private val marker = File(context.filesDir, "private_restore_intent_v1.enc")
    private val atomic = AtomicFile(marker)

    private val transaction = object : RestoreTransactionIo {
        override fun pending(): ByteArray? = readPending()

        override fun stage(plaintext: ByteArray) {
            val key = getKey(create = true)
                ?: throw IllegalStateException("Schlüssel für Wiederherstellung fehlt")
            val sealed = seal(plaintext, key)
            val output = atomic.startWrite()
            try {
                output.write(sealed)
                atomic.finishWrite(output)
            } catch (e: Exception) {
                atomic.failWrite(output)
                throw e
            } finally { sealed.fill(0) }
        }

        override fun replaceBoth(contents: InsightBackupContents) {
            journal.replaceVerified(contents.journal)
            insights.replaceVerified(requireNotNull(contents.insights))
        }

        override fun clear() {
            atomic.delete()
            require(!marker.exists() && !File(marker.path + ".bak").exists()) {
                "Wiederherstellungsmarkierung konnte nicht entfernt werden"
            }
        }
    }

    /** Must finish before reading, editing or showing either personal data area. */
    @Synchronized
    fun recoverPending(): Boolean = RestoreTransactionRunner.recover(transaction)

    /** Export both authenticated stores into one portable, encrypted EKB2 file. */
    @Synchronized
    fun exportBackup(password: CharArray): ByteArray {
        recoverPending()
        val journalEntries = journal.list()
        val insightEntries = insights.list()
        val bytes = InsightBundleFormat.encode(journalEntries, insightEntries)
        return try { InsightBundleBackup.seal(bytes, password) }
        finally { bytes.fill(0) }
    }

    /**
     * Called only after the user confirms replacement in the document picker.
     * Fail closed if either existing encrypted store cannot be authenticated.
     * For EKB1, keep existing insights and prune only links to entries missing
     * in the imported legacy journal (never silently erase user-authored text).
     */
    @Synchronized
    fun restoreBackup(bytes: ByteArray, password: CharArray): Int {
        recoverPending()
        val parsed = InsightBundleBackup.open(bytes, password)
        journal.list()
        val oldInsights = insights.list()
        val selected = parsed.insights ?: run {
            val journalIds = parsed.journal.mapTo(hashSetOf()) { it.id }
            oldInsights.map { entry ->
                val surviving = entry.journalIds.filterTo(linkedSetOf()) { it in journalIds }
                if (surviving == entry.journalIds) entry
                else entry.copy(journalIds = surviving,
                    updatedAtUtcMillis = maxOf(entry.updatedAtUtcMillis, System.currentTimeMillis()))
            }
        }
        RestoreTransactionRunner.commit(
            transaction, InsightBackupContents(parsed.journal, selected)
        )
        return parsed.journal.size
    }

    /** Deletes one journal entry and unlinks its evidence from insights atomically by replay. */
    @Synchronized
    fun deleteJournalEntry(id: String): Boolean {
        recoverPending()
        val current = journal.list()
        if (current.none { it.id == id }) return false
        val updated = PersonalInsightChanges.removeJournalReference(
            insights.list(), id, System.currentTimeMillis()
        )
        RestoreTransactionRunner.commit(
            transaction, InsightBackupContents(current.filterNot { it.id == id }, updated)
        )
        return true
    }

    /** Preserves all self-authored insight text while removing orphaned journal links. */
    @Synchronized
    fun deleteEntireJournal() {
        recoverPending()
        journal.list() // refuse to rewrite corrupted data
        val current = insights.list()
        val now = System.currentTimeMillis()
        val withoutLinks = current.map { item ->
            if (item.journalIds.isEmpty()) item
            else item.copy(journalIds = emptySet(),
                updatedAtUtcMillis = maxOf(now, item.updatedAtUtcMillis))
        }
        RestoreTransactionRunner.commit(
            transaction, InsightBackupContents(emptyList(), withoutLinks)
        )
    }

    private fun readPending(): ByteArray? {
        if (!marker.exists() && !File(marker.path + ".bak").exists()) return null
        val key = getKey(create = false)
            ?: throw IllegalStateException("Wiederherstellungsschlüssel fehlt; Daten nicht verändern.")
        val sealed = atomic.openRead().use {
            BoundedJournalInput.read(it, InsightBundleFormat.MAX_PLAINTEXT_BYTES + 128)
        }
        return try {
            require(sealed.size in 32..(InsightBundleFormat.MAX_PLAINTEXT_BYTES + 128))
            require(sealed.copyOfRange(0, 4).contentEquals(MAGIC))
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key,
                GCMParameterSpec(128, sealed.copyOfRange(4, 16)))
            cipher.updateAAD(AAD)
            cipher.doFinal(sealed.copyOfRange(16, sealed.size))
        } finally { sealed.fill(0) }
    }

    private fun seal(bytes: ByteArray, key: SecretKey): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        require(cipher.iv.size == 12)
        cipher.updateAAD(AAD)
        return MAGIC + cipher.iv + cipher.doFinal(bytes)
    }

    private fun getKey(create: Boolean): SecretKey? {
        val ks = KeyStore.getInstance("AndroidKeyStore")
        ks.load(null)
        (ks.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
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
        val MAGIC = "EKR1".toByteArray(Charsets.US_ASCII)
        val AAD = "com.valoon4.emotionskompass/pending-restore/v1".toByteArray()
        const val KEY_ALIAS = "emotionskompass_restore_transaction_v1"
    }
}
