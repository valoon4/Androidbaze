package com.valoon4.emotionskompass

import com.valoon4.emotionskompass.core.checkin.CheckInDraft
import com.valoon4.emotionskompass.core.insights.InsightBundleBackup
import com.valoon4.emotionskompass.core.insights.InsightBundleFormat
import com.valoon4.emotionskompass.core.insights.PersonalInsight
import com.valoon4.emotionskompass.core.insights.PersonalInsightChanges
import com.valoon4.emotionskompass.core.journal.JournalBackup
import com.valoon4.emotionskompass.core.journal.JournalEntry
import com.valoon4.emotionskompass.core.journal.JournalFormat
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class PersonalInsightBackupTest {
    private val passphrase = "Testpassphrase-2026-long".toCharArray()
    private val journal = listOf(
        JournalEntry("test-entry", 1000L, 1000L,
            CheckInDraft(emotions = mapOf("freude" to 0), needs = setOf("klarheit")))
    )
    private val personal = PersonalInsight(
        id = UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa").toString(),
        createdAtUtcMillis = 1200L, updatedAtUtcMillis = 1300L,
        text = "Das ist ausschließlich meine formulierte Erkenntnis.",
        archived = true, journalIds = setOf("test-entry"), emotionIds = setOf("freude")
    )

    @Test fun v2RoundtripPreservesJournalAndInsightFields() {
        val encoded = InsightBundleFormat.encode(journal, listOf(personal))
        val encrypted = InsightBundleBackup.seal(encoded, passphrase)
        assertEquals("EKB2", encrypted.copyOfRange(0, 4).decodeToString())
        assertFalse(encrypted.decodeToString().contains(personal.text))
        val restored = InsightBundleBackup.open(encrypted, passphrase)
        assertEquals(journal, restored.journal)
        assertEquals(listOf(personal), restored.insights)
    }

    @Test fun legacyBackupCannotImplyDeletingInsights() {
        val old = JournalBackup.seal(JournalFormat.encode(journal), passphrase)
        val result = InsightBundleBackup.open(old, passphrase)
        assertEquals(journal, result.journal)
        assertNull(result.insights)
    }

    @Test fun corruptedDataAndWrongPasswordAreRejected() {
        val backup = InsightBundleBackup.seal(
            InsightBundleFormat.encode(journal, listOf(personal)), passphrase
        )
        val changed = backup.copyOf().also { it[it.lastIndex] = (it.last().toInt() xor 1).toByte() }
        assertThrows(Exception::class.java) {
            InsightBundleBackup.open(changed, passphrase)
        }
        assertThrows(Exception::class.java) {
            InsightBundleBackup.open(backup, "Something-else-2026-long".toCharArray())
        }
    }

    @Test fun brokenJournalReferenceAndDuplicateInsightIdAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            InsightBundleFormat.encode(journal, listOf(personal, personal))
        }
        val dangling = personal.copy(journalIds = setOf("deleted-entry"))
        val bytes = InsightBundleFormat.encode(journal, listOf(dangling))
        assertThrows(IllegalArgumentException::class.java) {
            InsightBundleFormat.decode(bytes)
        }
    }

    @Test fun editsArchivesSearchAndDeletionPreserveIdentityAndText() {
        val first = PersonalInsightChanges.save(emptyList(), personal)
        val edited = personal.copy(text = "Eine neue, bewusst gespeicherte Formulierung.",
            updatedAtUtcMillis = 1500L)
        val second = PersonalInsightChanges.save(first, edited)
        assertEquals(1, second.size)
        assertEquals(edited, second.single())
        assertTrue(PersonalInsightChanges.search(second, "neue").isEmpty())
        assertEquals(1, PersonalInsightChanges.search(second, "NEUE", true).size)
        val withoutLink = PersonalInsightChanges.removeJournalReference(second, "test-entry", 1600L)
        assertEquals(edited.text, withoutLink.single().text)
        assertTrue(withoutLink.single().journalIds.isEmpty())
        assertEquals(1600L, withoutLink.single().updatedAtUtcMillis)
        assertTrue(PersonalInsightChanges.delete(withoutLink, personal.id).isEmpty())
    }
}
