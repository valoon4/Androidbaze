package com.valoon4.emotionskompass.core.insights

import android.content.Context
import com.valoon4.emotionskompass.core.checkin.CheckInDraft
import com.valoon4.emotionskompass.core.journal.JournalEntry
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
    private val insights: EncryptedPersonalInsightRepository,
    private val strategies: EncryptedStrategyExperimentRepository? = null
) {
    private val marker = File(context.filesDir, "private_restore_intent_v1.enc")
    private val atomic = AtomicFile(marker)
    private val tripleIntent: EncryptedStrategyRestoreIntent? = strategies?.let {
        EncryptedStrategyRestoreIntent(context, journal, insights, it)
    }

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
    fun recoverPending(): Boolean {
        // Always complete a pending legacy EKR1 before reading or writing EKR2.
        val old = RestoreTransactionRunner.recover(transaction)
        val next = tripleIntent?.let { StrategyRestoreTransactionRunner.recover(it) } ?: false
        return old || next
    }

    /** Export both authenticated stores into one portable, encrypted EKB2 file. */
    @Synchronized
    fun exportBackup(password: CharArray): ByteArray {
        recoverPending()
        val journalEntries = journal.list()
        val insightEntries = insights.list()
        val bytes = if (strategies == null) {
            InsightBundleFormat.encode(journalEntries, insightEntries)
        } else {
            StrategyBundleFormat.encode(StrategyBackupContents(
                journalEntries, insightEntries, strategies.list()
            ))
        }
        return try {
            if (strategies == null) InsightBundleBackup.seal(bytes, password)
            else StrategyBundleBackup.seal(bytes, password)
        } finally { bytes.fill(0) }
    }

    /**
     * Authenticating PREVIEW, not a write: counts and the exact ciphertext digest
     * can be shown before the user confirms an import. A missing legacy area is
     * represented as null; zero entries in EKB3 is an explicitly empty area.
     */
    @Synchronized
    fun previewBackup(bytes: ByteArray, password: CharArray): BackupImportPreview {
        recoverPending()
        journal.list()
        insights.list()
        strategies?.list()
        require(bytes.size >= 4)
        val format = bytes.copyOfRange(0, 4).decodeToString()
        val parsed = if (strategies == null) {
            val old = InsightBundleBackup.open(bytes, password)
            StrategyBackupContents(old.journal, old.insights, null)
        } else {
            StrategyBundleBackup.open(bytes, password)
        }
        require(format in setOf("EKB1", "EKB2", "EKB3"))
        return BackupImportPreview(format, parsed.journal.size,
            parsed.insights?.size, parsed.experiments?.size,
            encryptedBackupDigest(bytes))
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
        if (strategies != null) {
            val parsed = StrategyBundleBackup.open(bytes, password)
            // All three existing areas must authenticate BEFORE staging replacement.
            journal.list()
            val oldInsights = insights.list()
            val oldExperiments = strategies.list()
            val journalIds = parsed.journal.mapTo(hashSetOf()) { it.id }
            val selectedInsights = parsed.insights ?: oldInsights.map { entry ->
                val linked = entry.journalIds.intersect(journalIds)
                if (linked == entry.journalIds) entry
                else entry.copy(journalIds = linked,
                    updatedAtUtcMillis = maxOf(entry.updatedAtUtcMillis, System.currentTimeMillis()))
            }
            val insightIds = selectedInsights.mapTo(hashSetOf()) { it.id }
            // An EKB1/EKB2 file has NO experiment section. Preserve user words
            // and only unlink references to records absent from the restored set.
            val selectedExperiments = parsed.experiments
                ?: StrategyExperimentChanges.pruneMissingReferences(
                    oldExperiments, journalIds, insightIds, System.currentTimeMillis()
                )
            val target = StrategyBackupContents(
                parsed.journal, selectedInsights, selectedExperiments
            )
            val intent = requireNotNull(tripleIntent)
            StrategyRestoreTransactionRunner.commit(intent, target)
            return parsed.journal.size
        }
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

    /**
     * Journal writes must use the same monitor as export/import: unlike the old
     * ViewModel path, a check-in can no longer race a three-area backup.
     */
    @Synchronized
    fun saveJournalEntry(draft: CheckInDraft, existingId: String? = null): JournalEntry {
        recoverPending()
        journal.list()
        insights.list()
        strategies?.list()
        return journal.save(draft, existingId)
    }

    /** Authenticated, coordinated M7.4.1 storage. No UI writes yet. */
    @Synchronized
    fun listStrategyExperiments(): List<StrategyExperiment> {
        recoverPending()
        journal.list()
        insights.list()
        // Older isolated EKB2 tests construct this coordinator without EKS1.
        return strategies?.list() ?: emptyList()
    }

    @Synchronized
    fun saveStrategyExperiment(entry: StrategyExperiment): List<StrategyExperiment> {
        recoverPending()
        val journalIds = journal.list().mapTo(hashSetOf()) { it.id }
        val insightIds = insights.list().mapTo(hashSetOf()) { it.id }
        require(entry.relatedJournalIds.all { it in journalIds })
        require(entry.relatedInsightIds.all { it in insightIds })
        return requireNotNull(strategies).save(entry)
    }

    @Synchronized
    fun deleteStrategyExperiment(id: String): List<StrategyExperiment> {
        recoverPending()
        journal.list()
        insights.list()
        return requireNotNull(strategies).delete(id)
    }

    /** All insight operations share the same coordinator lock as portable import/export. */
    @Synchronized
    fun listInsights(): List<PersonalInsight> {
        recoverPending()
        journal.list()
        return insights.list()
    }

    @Synchronized
    fun saveInsight(insight: PersonalInsight): List<PersonalInsight> {
        recoverPending()
        val validIds = journal.list().mapTo(hashSetOf()) { it.id }
        strategies?.list()
        require(insight.journalIds.all { it in validIds }) {
            "Der verknüpfte Tagebucheintrag ist nicht mehr vorhanden."
        }
        return insights.save(insight)
    }

    @Synchronized
    fun deleteInsight(id: String): List<PersonalInsight> {
        recoverPending()
        val journalEntries = journal.list()
        val insightEntries = insights.list()
        val store = strategies ?: return insights.delete(id)
        val experiments = store.list()
        if (insightEntries.none { it.id == id }) return insightEntries
        val nextInsights = PersonalInsightChanges.delete(insightEntries, id)
        val nextExperiments = StrategyExperimentChanges.pruneMissingReferences(
            experiments, journalEntries.mapTo(hashSetOf()) { it.id },
            nextInsights.mapTo(hashSetOf()) { it.id }, System.currentTimeMillis()
        )
        StrategyRestoreTransactionRunner.commit(requireNotNull(tripleIntent),
            StrategyBackupContents(journalEntries, nextInsights, nextExperiments))
        return nextInsights
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
        val remaining = current.filterNot { it.id == id }
        val store = strategies
        if (store == null) {
            RestoreTransactionRunner.commit(
                transaction, InsightBackupContents(remaining, updated)
            )
        } else {
            val experiments = StrategyExperimentChanges.pruneMissingReferences(
                store.list(), remaining.mapTo(hashSetOf()) { it.id },
                updated.mapTo(hashSetOf()) { it.id }, System.currentTimeMillis()
            )
            StrategyRestoreTransactionRunner.commit(requireNotNull(tripleIntent),
                StrategyBackupContents(remaining, updated, experiments))
        }
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
        if (strategies == null) {
            RestoreTransactionRunner.commit(
                transaction, InsightBackupContents(emptyList(), withoutLinks)
            )
        } else {
            val experiments = StrategyExperimentChanges.pruneMissingReferences(
                strategies.list(), emptySet(), withoutLinks.mapTo(hashSetOf()) { it.id },
                System.currentTimeMillis()
            )
            StrategyRestoreTransactionRunner.commit(requireNotNull(tripleIntent),
                StrategyBackupContents(emptyList(), withoutLinks, experiments))
        }
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
