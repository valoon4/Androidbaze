package com.valoon4.emotionskompass

import android.content.Context
import android.content.ContextWrapper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.valoon4.emotionskompass.core.checkin.CheckInDraft
import com.valoon4.emotionskompass.core.insights.EncryptedInsightsRepository
import com.valoon4.emotionskompass.core.journal.EncryptedJournalRepository
import com.valoon4.emotionskompass.core.journal.PersonalBackup
import com.valoon4.emotionskompass.core.journal.PersonalRestoreTransaction
import java.io.File
import java.util.UUID
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersonalInsightsStorageEmulatorTest {
    private fun isolated(test: (Context) -> Unit) {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val folder = File(base.cacheDir, "m73-test-" + UUID.randomUUID().toString())
        check(folder.mkdirs())
        try {
            val context = object : ContextWrapper(base) {
                override fun getFilesDir(): File = folder
            }
            test(context)
        } finally { folder.deleteRecursively() }
    }

    @Test fun storeSupportsAuthenticatedCreateEditArchiveAndDelete() = isolated { context ->
        val repo = EncryptedInsightsRepository(context)
        val saved = repo.save("Ein eigener Gedanke.", journalId = "beleg-123")
        assertEquals("beleg-123", repo.list().single().journalId)
        repo.save("Ein überarbeiteter Gedanke.", saved.id, null)
        assertNull(repo.list().single().journalId)
        repo.archive(saved.id, true)
        assertTrue(repo.list().single().archived)
        repo.archive(saved.id, false)
        assertFalse(repo.list().single().archived)
        repo.delete(saved.id)
        assertTrue(repo.list().isEmpty())
    }

    @Test fun restoreRollbackPreservesBothAuthenticatedOriginals() = isolated { context ->
        val journal = EncryptedJournalRepository(context)
        val insights = EncryptedInsightsRepository(context)
        val transaction = PersonalRestoreTransaction(context)
        val first = journal.save(CheckInDraft(emotions = mapOf("freude" to 0)))
        val original = insights.save("Text vor Wiederherstellung", journalId = first.id)
        val encrypted = PersonalBackup.seal(journal.list(), insights.list(),
            "Passphrase-für-Backup-2026".toCharArray())
        assertEquals(original, PersonalBackup.open(encrypted,
            "Passphrase-für-Backup-2026".toCharArray()).insights.single())

        assertThrows(IllegalStateException::class.java) {
            transaction.replaceTogether {
                journal.replaceAll(emptyList())
                insights.replaceAll(emptyList())
                error("Simulierter Fehler nach beiden Dateischreibvorgängen")
            }
        }
        transaction.recoverIfNeeded() // Idempotent after rollback.
        assertEquals(first, journal.list().single())
        assertEquals(original, insights.list().single())
    }
}
