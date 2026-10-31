package com.valoon4.emotionskompass

import com.valoon4.emotionskompass.core.checkin.CheckInDraft
import com.valoon4.emotionskompass.core.insights.PersonalInsight
import com.valoon4.emotionskompass.core.insights.PersonalInsightFormat
import com.valoon4.emotionskompass.core.journal.JournalBackup
import com.valoon4.emotionskompass.core.journal.JournalEntry
import com.valoon4.emotionskompass.core.journal.JournalFormat
import com.valoon4.emotionskompass.core.journal.PersonalBackup
import org.junit.Assert.*
import org.junit.Test

class PersonalInsightBackupTest {
    private fun passphrase() = "Mindestens-Zwoelf-Zeichen!2026".toCharArray()
    private val journal = listOf(
        JournalEntry("journal-one", 1000L, 1010L,
            CheckInDraft(emotions = mapOf("freude" to 0), needs = setOf("klarheit")))
    )
    private val insights = listOf(
        PersonalInsight("insight-one", 1000L, 1010L,
            "Eine selbst formulierte, private Erkenntnis.", journalId = "journal-one"),
        PersonalInsight("insight-two", 1100L, 1200L,
            "Ein absichtlich archivierter Gedanke.", archived = true)
    )

    @Test fun strictRoundtripPreservesTextsLinksAndArchiveState() {
        val encoded = PersonalInsightFormat.encode(insights)
        assertEquals(insights, PersonalInsightFormat.decode(encoded))
        assertEquals(2, PersonalInsightFormat.decode(encoded).size)
    }

    @Test fun invalidInputsCannotBeSilentlyTruncatedOrOverwritten() {
        assertThrows(IllegalArgumentException::class.java) {
            PersonalInsightFormat.newInsight("  ")
        }
        assertThrows(IllegalArgumentException::class.java) {
            PersonalInsightFormat.encode(insights + insights.first())
        }
        assertThrows(IllegalArgumentException::class.java) {
            PersonalInsightFormat.encode(listOf(
                insights.first().copy(text = "x".repeat(PersonalInsightFormat.MAX_TEXT_LENGTH + 1))
            ))
        }
        assertThrows(Exception::class.java) {
            PersonalInsightFormat.decode("{\"version\":99,\"insights\":[]}".toByteArray())
        }
    }

    @Test fun ekb2RestoreAuthenticatesBothStoresAndMetadata() {
        val encrypted = PersonalBackup.seal(journal, insights, passphrase())
        assertTrue(PersonalBackup.isV2(encrypted))
        val decoded = PersonalBackup.open(encrypted, passphrase())
        assertEquals(journal, decoded.journal)
        assertEquals(insights, decoded.insights)
        assertFalse(String(encrypted, Charsets.ISO_8859_1).contains(insights.first().text))
    }

    @Test fun corruptedPasswordOrCiphertextRejectsWholeBackup() {
        val encrypted = PersonalBackup.seal(journal, insights, passphrase())
        assertThrows(Exception::class.java) {
            PersonalBackup.open(encrypted, "falsche-Passphrase-2026!".toCharArray())
        }
        val modified = encrypted.copyOf()
        modified[modified.lastIndex] = (modified.last().toInt() xor 1).toByte()
        assertThrows(Exception::class.java) {
            PersonalBackup.open(modified, passphrase())
        }
        val changedHeader = encrypted.copyOf()
        changedHeader[0] = 0
        assertThrows(Exception::class.java) {
            PersonalBackup.open(changedHeader, passphrase())
        }
    }

    @Test fun ekb1StillDecryptsWithoutClaimingAnyInsightContent() {
        val old = JournalBackup.seal(JournalFormat.encode(journal), passphrase())
        assertFalse(PersonalBackup.isV2(old))
        assertEquals(journal, JournalFormat.decode(JournalBackup.open(old, passphrase())))
        // The EKB1 envelope has no insight field; the EKB1 import path must
        // preserve the separate insight repository instead of replacing it.
    }

    @Test fun encryptingTwiceNeverReusesTheSameEnvelope() {
        val first = PersonalBackup.seal(journal, insights, passphrase())
        val second = PersonalBackup.seal(journal, insights, passphrase())
        assertFalse(first.contentEquals(second))
    }
}
