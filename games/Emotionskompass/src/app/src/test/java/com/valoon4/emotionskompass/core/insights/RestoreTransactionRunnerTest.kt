package com.valoon4.emotionskompass.core.insights

import com.valoon4.emotionskompass.core.checkin.CheckInDraft
import com.valoon4.emotionskompass.core.journal.JournalEntry
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class RestoreTransactionRunnerTest {
    private val target = InsightBackupContents(
        journal = listOf(JournalEntry("fictional-entry", 1000, 1000,
            CheckInDraft(emotions = mapOf("freude" to 0)))),
        insights = listOf(PersonalInsight(
            UUID.fromString("bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb").toString(),
            2000L, 2000L, "Eine ausdrücklich gespeicherte, erfundene Erkenntnis.",
            journalIds = setOf("fictional-entry")
        ))
    )

    private class FakeStorage(
        var failAfterJournal: Boolean = false,
        var failClearing: Boolean = false
    ) : RestoreTransactionIo {
        var marker: ByteArray? = null
        var journal: List<JournalEntry> = emptyList()
        var insights: List<PersonalInsight> = emptyList()
        var applies: Int = 0
        var clears: Int = 0

        override fun pending(): ByteArray? = marker?.copyOf()
        override fun stage(plaintext: ByteArray) {
            check(marker == null) { "Cannot replace an earlier intent" }
            marker = plaintext.copyOf()
        }
        override fun replaceBoth(contents: InsightBackupContents) {
            applies++
            journal = contents.journal
            if (failAfterJournal) {
                failAfterJournal = false
                throw IllegalStateException("Simulated process stop between encrypted writes")
            }
            insights = requireNotNull(contents.insights)
        }
        override fun clear() {
            if (failClearing) {
                failClearing = false
                throw IllegalStateException("Simulated failure clearing durable intent")
            }
            clears++
            marker = null
        }
    }

    @Test fun interruptedDualWriteIsReplayedBeforeDataIsExposed() {
        val store = FakeStorage(failAfterJournal = true)
        assertThrows(IllegalStateException::class.java) {
            RestoreTransactionRunner.commit(store, target)
        }
        assertNotNull(store.marker)
        assertEquals(target.journal, store.journal)
        assertTrue(store.insights.isEmpty())
        assertTrue(RestoreTransactionRunner.recover(store))
        assertEquals(target.journal, store.journal)
        assertEquals(target.insights, store.insights)
        assertNull(store.marker)
        assertFalse(RestoreTransactionRunner.recover(store))
    }

    @Test fun clearingFailureIsSafelyReplayable() {
        val store = FakeStorage(failClearing = true)
        assertThrows(IllegalStateException::class.java) {
            RestoreTransactionRunner.commit(store, target)
        }
        assertNotNull(store.marker)
        assertEquals(target.insights, store.insights)
        assertTrue(RestoreTransactionRunner.recover(store))
        assertEquals(2, store.applies)
        assertEquals(1, store.clears)
    }

    @Test fun invalidPendingDataNeverCausesWritesOrSilentReset() {
        val store = FakeStorage()
        store.marker = "not a valid transaction".toByteArray()
        assertThrows(Exception::class.java) { RestoreTransactionRunner.recover(store) }
        assertNotNull(store.marker)
        assertEquals(0, store.applies)
        assertTrue(store.journal.isEmpty())
    }

    @Test fun missingDurableIntentNeverReportsSuccess() {
        val brokenStorage = object : RestoreTransactionIo {
            override fun pending(): ByteArray? = null
            override fun stage(plaintext: ByteArray) { /* simulate lost fsync */ }
            override fun replaceBoth(contents: InsightBackupContents) {
                fail("No destination may be written without a durable intent")
            }
            override fun clear() = Unit
        }
        assertThrows(IllegalStateException::class.java) {
            RestoreTransactionRunner.commit(brokenStorage, target)
        }
    }

    @Test fun impossibleRestoreWithoutBothAreasIsRejected() {
        val store = FakeStorage()
        assertThrows(IllegalArgumentException::class.java) {
            RestoreTransactionRunner.commit(store, target.copy(insights = null))
        }
        assertNull(store.marker)
        assertEquals(0, store.applies)
    }
}
