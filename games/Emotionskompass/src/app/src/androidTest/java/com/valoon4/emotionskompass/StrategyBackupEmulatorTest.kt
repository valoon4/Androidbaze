package com.valoon4.emotionskompass

import android.content.Context
import android.content.ContextWrapper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.valoon4.emotionskompass.core.checkin.CheckInDraft
import com.valoon4.emotionskompass.core.insights.*
import com.valoon4.emotionskompass.core.journal.EncryptedJournalRepository
import com.valoon4.emotionskompass.core.journal.JournalBackup
import com.valoon4.emotionskompass.core.journal.JournalFormat
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/** Real AndroidKeyStore/AtomicFile tests using only isolated fictional data. */
@RunWith(AndroidJUnit4::class)
class StrategyBackupEmulatorTest {
    private val target get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val password = "Fiktive-strategie-sicherung-2026!".toCharArray()
    private fun isolated(): Pair<Context, File> {
        val folder = File(target.cacheDir, "m741-" + UUID.randomUUID().toString())
        check(folder.mkdirs())
        return object : ContextWrapper(target) {
            override fun getFilesDir(): File = folder
        } to folder
    }
    private data class Stores(
        val journal: EncryptedJournalRepository,
        val insights: EncryptedPersonalInsightRepository,
        val strategies: EncryptedStrategyExperimentRepository,
        val combined: CombinedBackupRepository
    )
    private fun stores(context: Context): Stores {
        val journal = EncryptedJournalRepository(context)
        val insights = EncryptedPersonalInsightRepository(context)
        val strategies = EncryptedStrategyExperimentRepository(context)
        return Stores(journal, insights, strategies,
            CombinedBackupRepository(context, journal, insights, strategies))
    }
    private data class Fictional(val insight: PersonalInsight,
                                 val experiment: StrategyExperiment, val journalId: String)
    private fun populate(s: Stores): Fictional {
        val journal = s.combined.saveJournalEntry(
            CheckInDraft(note = "Nur eine fiktive Test-Situation"))
        val now = System.currentTimeMillis()
        val insight = PersonalInsight.create(
            "Ich selbst entscheide über eine mögliche Strategie.", now, setOf(journal.id))
        s.combined.saveInsight(insight)
        val experiment = StrategyExperiment.fromCatalog(
            "planning", "Kleine Schritte planen", now
        ).copy(planText = "Vielleicht eine kleine Aufgabe aufschreiben.",
            status = ExperimentStatus.NOT_TRIED, evaluation = null,
            relatedJournalIds = setOf(journal.id), relatedInsightIds = setOf(insight.id))
        s.combined.saveStrategyExperiment(experiment)
        return Fictional(insight, experiment, journal.id)
    }

    @Test fun fullEkb3RestoresThreeEncryptedAreasInIndependentContext() {
        val (contextA, folderA) = isolated()
        val (contextB, folderB) = isolated()
        try {
            val a = stores(contextA)
            val original = populate(a)
            val portable = a.combined.exportBackup(password)
            assertEquals("EKB3", portable.copyOfRange(0, 4).decodeToString())
            val b = stores(contextB)
            assertEquals(1, b.combined.restoreBackup(portable, password))
            assertEquals(listOf(original.journalId), b.journal.list().map { it.id })
            assertEquals(listOf(original.insight), b.insights.list())
            assertEquals(listOf(original.experiment), b.strategies.list())
            assertFalse(b.combined.recoverPending())
        } finally { folderA.deleteRecursively(); folderB.deleteRecursively() }
    }

    @Test fun ekb1AndEkb2PreserveExperimentWordsButUnlinkReplacedRecords() {
        val (context, folder) = isolated()
        try {
            val s = stores(context)
            val original = populate(s)
            val v2 = InsightBundleBackup.seal(
                InsightBundleFormat.encode(emptyList(), emptyList()), password)
            assertEquals(0, s.combined.restoreBackup(v2, password))
            assertTrue(s.insights.list().isEmpty())
            val afterV2 = s.strategies.list().single()
            assertEquals(original.experiment.planText, afterV2.planText)
            assertTrue(afterV2.relatedJournalIds.isEmpty())
            assertTrue(afterV2.relatedInsightIds.isEmpty())
            val entry = s.combined.saveJournalEntry(CheckInDraft(note = "Noch ein fiktiver Eintrag"))
            val newInsight = PersonalInsight.create("EKB1 darf diesen Text nicht löschen.",
                System.currentTimeMillis(), setOf(entry.id))
            s.combined.saveInsight(newInsight)
            val v1 = JournalBackup.seal(JournalFormat.encode(emptyList()), password)
            assertEquals(0, s.combined.restoreBackup(v1, password))
            assertEquals(newInsight.text, s.insights.list().single().text)
            assertTrue(s.insights.list().single().journalIds.isEmpty())
            assertEquals(original.experiment.planText, s.strategies.list().single().planText)
        } finally { folder.deleteRecursively() }
    }

    @Test fun badPasswordAndTamperedBytesNeverReplaceAnyOfThreeAreas() {
        val (context, folder) = isolated()
        try {
            val s = stores(context)
            val original = populate(s)
            val bytes = s.combined.exportBackup(password)
            assertThrows(Exception::class.java) {
                s.combined.restoreBackup(bytes, "Falsche-Passphrase-2026!".toCharArray())
            }
            val corrupt = bytes.copyOf()
            corrupt[corrupt.lastIndex] = (corrupt.last().toInt() xor 1).toByte()
            assertThrows(Exception::class.java) { s.combined.restoreBackup(corrupt, password) }
            assertEquals(original.journalId, s.journal.list().single().id)
            assertEquals(original.insight, s.insights.list().single())
            assertEquals(original.experiment, s.strategies.list().single())
            assertFalse(s.combined.recoverPending())
        } finally { folder.deleteRecursively() }
    }

    @Test fun deletingReferencedJournalAndInsightPreservesExperimentWords() {
        val (context, folder) = isolated()
        try {
            val s = stores(context)
            val original = populate(s)
            assertTrue(s.combined.deleteJournalEntry(original.journalId))
            assertTrue(s.insights.list().single().journalIds.isEmpty())
            assertTrue(s.strategies.list().single().relatedJournalIds.isEmpty())
            s.combined.deleteInsight(original.insight.id)
            assertTrue(s.insights.list().isEmpty())
            assertTrue(s.strategies.list().single().relatedInsightIds.isEmpty())
            assertEquals(original.experiment.planText, s.strategies.list().single().planText)
        } finally { folder.deleteRecursively() }
    }

    @Test fun corruptedEks1PreventsExportAndImportWithoutOverwritingIt() {
        val (context, folder) = isolated()
        try {
            val s = stores(context)
            populate(s)
            val file = File(folder, "private_strategy_experiments_v1.enc")
            assertTrue(file.isFile)
            file.writeBytes("intentionally-damaged-EKS1".toByteArray())
            val damaged = file.readBytes()
            assertThrows(Exception::class.java) { s.combined.exportBackup(password) }
            val incoming = StrategyBundleBackup.seal(
                StrategyBundleFormat.encode(StrategyBackupContents(
                    emptyList(), emptyList(), emptyList())), password)
            assertThrows(Exception::class.java) { s.combined.restoreBackup(incoming, password) }
            assertArrayEquals(damaged, file.readBytes())
            assertTrue(s.journal.list().isNotEmpty())
        } finally { folder.deleteRecursively() }
    }

    @Test fun previewShowsOnlyAuthenticatedCountsAndMissingLegacyAreas() {
        val (context, folder) = isolated()
        try {
            val s = stores(context)
            val original = populate(s)
            val encrypted = s.combined.exportBackup(password)
            val inspected = s.combined.previewBackup(encrypted, password)
            assertEquals("EKB3", inspected.format)
            assertEquals(1, inspected.journalCount)
            assertEquals(1, inspected.insightCount)
            assertEquals(1, inspected.strategyExperimentCount)
            assertEquals(64, inspected.encryptedSha256.length)
            val v2 = InsightBundleBackup.seal(
                InsightBundleFormat.encode(s.journal.list(), s.insights.list()), password)
            val old = s.combined.previewBackup(v2, password)
            assertEquals("EKB2", old.format)
            assertEquals(1, old.insightCount)
            assertNull(old.strategyExperimentCount)
            assertNotEquals(inspected.encryptedSha256, old.encryptedSha256)
            assertThrows(Exception::class.java) {
                s.combined.previewBackup(v2, "Falsche-Passphrase-2026!".toCharArray())
            }
            assertEquals(original.experiment, s.strategies.list().single())
        } finally { folder.deleteRecursively() }
    }

    @Test fun checkInSaveAndPortableExportUseOneConsistentCoordinator() {
        val (context, folder) = isolated()
        val worker = java.util.concurrent.Executors.newFixedThreadPool(2)
        try {
            val s = stores(context)
            val start = java.util.concurrent.CountDownLatch(1)
            val saved = worker.submit<String> {
                start.await()
                s.combined.saveJournalEntry(
                    CheckInDraft(note = "Fiktive parallele Transaktion")
                ).id
            }
            val backedUp = worker.submit<ByteArray> {
                start.await()
                s.combined.exportBackup(password)
            }
            start.countDown()
            val entryId = saved.get(30, java.util.concurrent.TimeUnit.SECONDS)
            val portable = backedUp.get(30, java.util.concurrent.TimeUnit.SECONDS)
            val restored = StrategyBundleBackup.open(portable, password)
            assertTrue(restored.journal.isEmpty() ||
                restored.journal.single().id == entryId)
            assertEquals(emptyList<PersonalInsight>(), restored.insights)
            assertEquals(emptyList<StrategyExperiment>(), restored.experiments)
            assertEquals(entryId, s.journal.list().single().id)
        } finally {
            worker.shutdownNow()
            folder.deleteRecursively()
        }
    }

    @Test fun pendingLegacyEkr1IsReplayedBeforeEkr2AndPrunesOnlyBrokenLinks() {
        val (context, folder) = isolated()
        try {
            val s = stores(context)
            val original = populate(s)
            val store = java.security.KeyStore.getInstance("AndroidKeyStore")
            store.load(null)
            val alias = "emotionskompass_restore_transaction_v1"
            val existing = store.getKey(alias, null) as? javax.crypto.SecretKey
            val key = existing ?: javax.crypto.KeyGenerator.getInstance(
                "AES", "AndroidKeyStore"
            ).run {
                init(android.security.keystore.KeyGenParameterSpec.Builder(
                    alias, android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or
                        android.security.keystore.KeyProperties.PURPOSE_DECRYPT
                ).setKeySize(256)
                    .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(
                        android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE
                    ).setRandomizedEncryptionRequired(true)
                    .setUserAuthenticationRequired(false).build())
                generateKey()
            }
            val target = InsightBundleFormat.encode(emptyList(), emptyList())
            val cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, key)
            cipher.updateAAD("com.valoon4.emotionskompass/pending-restore/v1".toByteArray())
            val intent = "EKR1".toByteArray() + cipher.iv + cipher.doFinal(target)
            File(folder, "private_restore_intent_v1.enc").writeBytes(intent)
            assertTrue(s.combined.recoverPending())
            assertTrue(s.journal.list().isEmpty())
            assertTrue(s.insights.list().isEmpty())
            val remaining = s.strategies.list().single()
            assertEquals(original.experiment.planText, remaining.planText)
            assertTrue(remaining.relatedJournalIds.isEmpty())
            assertTrue(remaining.relatedInsightIds.isEmpty())
            assertFalse(s.combined.recoverPending())
            assertFalse(File(folder, "private_restore_intent_v1.enc").exists())
            assertFalse(File(folder, "private_strategy_restore_intent_v2.enc").exists())
        } finally { folder.deleteRecursively() }
    }
}
