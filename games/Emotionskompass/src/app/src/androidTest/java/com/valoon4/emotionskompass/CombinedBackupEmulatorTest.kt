package com.valoon4.emotionskompass

import android.content.Context
import android.content.ContextWrapper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.valoon4.emotionskompass.core.checkin.CheckInDraft
import com.valoon4.emotionskompass.core.insights.CombinedBackupRepository
import com.valoon4.emotionskompass.core.insights.EncryptedPersonalInsightRepository
import com.valoon4.emotionskompass.core.insights.PersonalInsight
import com.valoon4.emotionskompass.core.journal.EncryptedJournalRepository
import com.valoon4.emotionskompass.core.journal.JournalBackup
import com.valoon4.emotionskompass.core.journal.JournalFormat
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/** Real AndroidKeyStore/AtomicFile integration, with only isolated fictional test files. */
@RunWith(AndroidJUnit4::class)
class CombinedBackupEmulatorTest {
    private val target get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun isolatedContext(): Pair<Context, File> {
        val dir = File(target.cacheDir, "m73-test-${UUID.randomUUID()}")
        check(dir.mkdirs())
        return object : ContextWrapper(target) {
            override fun getFilesDir(): File = dir
        } to dir
    }

    @Test fun portableEkb2RoundtripAndLegacyImportKeepPersonalInsights() {
        val (sourceContext, sourceDir) = isolatedContext()
        val (restoredContext, restoredDir) = isolatedContext()
        val passphrase = "Nur-fuer-fiktive-Tests-2026!".toCharArray()
        try {
            val sourceJournal = EncryptedJournalRepository(sourceContext)
            val sourceInsights = EncryptedPersonalInsightRepository(sourceContext)
            val original = sourceJournal.save(
                CheckInDraft(emotions = mapOf("freude" to 0), needs = setOf("klarheit"),
                    note = "Nur erfundene Android-Testdaten")
            )
            val personal = PersonalInsight.create(
                "Ich formuliere diese Test-Erkenntnis ausdrücklich selbst.",
                System.currentTimeMillis(), setOf(original.id)
            )
            sourceInsights.save(personal)
            val source = CombinedBackupRepository(sourceContext, sourceJournal, sourceInsights)
            val portable = source.exportBackup(passphrase)
            assertEquals("EKB2", portable.copyOfRange(0, 4).decodeToString())

            val restoredJournal = EncryptedJournalRepository(restoredContext)
            val restoredInsights = EncryptedPersonalInsightRepository(restoredContext)
            val restored = CombinedBackupRepository(restoredContext, restoredJournal, restoredInsights)
            assertEquals(1, restored.restoreBackup(portable, passphrase))
            assertFalse(restored.recoverPending())
            assertEquals(sourceJournal.list(), restoredJournal.list())
            assertEquals(listOf(personal), restoredInsights.list())

            // Legacy EKB1 has no insight section; import must keep written text
            // while removing links to journal entries no longer in its backup.
            val legacy = JournalBackup.seal(JournalFormat.encode(emptyList()), passphrase)
            assertEquals(0, restored.restoreBackup(legacy, passphrase))
            assertTrue(restoredJournal.list().isEmpty())
            val preserved = restoredInsights.list().single()
            assertEquals(personal.id, preserved.id)
            assertEquals(personal.text, preserved.text)
            assertTrue(preserved.journalIds.isEmpty())
        } finally {
            passphrase.fill('0')
            sourceDir.deleteRecursively()
            restoredDir.deleteRecursively()
        }
    }

    @Test fun journalDeletionPreservesUserWordsAndClearsOnlyBrokenLinks() {
        val (context, dir) = isolatedContext()
        try {
            val journal = EncryptedJournalRepository(context)
            val insights = EncryptedPersonalInsightRepository(context)
            val first = journal.save(CheckInDraft(note = "Fiktiver Eintrag A"))
            val second = journal.save(CheckInDraft(note = "Fiktiver Eintrag B"))
            val personal = PersonalInsight.create(
                "Mein selbst formulierter, fiktiver Gedanke soll bestehen bleiben.",
                System.currentTimeMillis(), setOf(first.id, second.id)
            )
            insights.save(personal)
            val combined = CombinedBackupRepository(context, journal, insights)
            assertTrue(combined.deleteJournalEntry(first.id))
            assertEquals(listOf(second.id), journal.list().map { it.id })
            assertEquals(setOf(second.id), insights.list().single().journalIds)
            assertEquals(personal.text, insights.list().single().text)
            combined.deleteEntireJournal()
            assertTrue(journal.list().isEmpty())
            assertTrue(insights.list().single().journalIds.isEmpty())
            assertEquals(personal.text, insights.list().single().text)
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test fun incorrectPasswordCannotReplaceEitherEncryptedStore() {
        val (context, dir) = isolatedContext()
        val passphrase = "Nur-fuer-fiktive-Tests-2026!".toCharArray()
        try {
            val journal = EncryptedJournalRepository(context)
            val insights = EncryptedPersonalInsightRepository(context)
            val current = journal.save(CheckInDraft(note = "Fiktiver Originaleintrag"))
            val insight = PersonalInsight.create(
                "Darf bei falscher Passphrase nicht verloren gehen.",
                System.currentTimeMillis(), setOf(current.id)
            )
            insights.save(insight)
            val combined = CombinedBackupRepository(context, journal, insights)
            val bytes = combined.exportBackup(passphrase)
            assertThrows(Exception::class.java) {
                combined.restoreBackup(bytes, "falsche-passphrase-2026".toCharArray())
            }
            assertEquals(current.id, journal.list().single().id)
            assertEquals(insight.text, insights.list().single().text)
            assertFalse(combined.recoverPending())
        } finally {
            passphrase.fill('0')
            dir.deleteRecursively()
        }
    }
}
