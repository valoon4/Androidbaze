package com.valoon4.emotionskompass.core.insights

import com.valoon4.emotionskompass.core.checkin.CheckInDraft
import com.valoon4.emotionskompass.core.journal.JournalEntry
import com.valoon4.emotionskompass.core.journal.JournalFormat
import org.junit.Assert.*
import org.junit.Test

class StrategyRestoreTransactionRunnerTest {
    private val now = 1_780_000_000_000L

    private fun target(): StrategyBackupContents {
        val journal = JournalFormat.newEntry(CheckInDraft(note = "fiktiv"), now)
        val insight = PersonalInsight.create("Selbst geschriebene Test-Erkenntnis", now)
        val experiment = StrategyExperiment.fromCatalog("planning", "Kleine Schritte planen", now)
        return StrategyBackupContents(listOf(journal), listOf(insight), listOf(experiment))
    }

    private class InterruptedIo(var failAfter: Int? = null) : StrategyRestoreTransactionIo {
        var journal: List<JournalEntry> = emptyList()
        var insights: List<PersonalInsight> = emptyList()
        var experiments: List<StrategyExperiment> = emptyList()
        var intent: ByteArray? = null
        var clearCount = 0
        override fun pending(): ByteArray? = intent?.copyOf()
        override fun stage(plaintext: ByteArray) {
            check(intent == null) { "Cannot overwrite previous intent" }
            intent = plaintext.copyOf()
        }
        override fun replaceAll(contents: StrategyBackupContents) {
            journal = contents.journal
            if (failAfter == 1) throw IllegalStateException("simulated crash after journal")
            insights = requireNotNull(contents.insights)
            if (failAfter == 2) throw IllegalStateException("simulated crash after insights")
            experiments = requireNotNull(contents.experiments)
            if (failAfter == 3) throw IllegalStateException("simulated crash after experiments")
        }
        override fun clear() {
            check(intent != null)
            intent = null
            clearCount++
        }
    }

    @Test fun interruptionAtEveryWriteBoundaryReplaysFullTarget() {
        for (point in 1..3) {
            val io = InterruptedIo(failAfter = point)
            val expected = target()
            assertThrows(IllegalStateException::class.java) {
                StrategyRestoreTransactionRunner.commit(io, expected)
            }
            assertNotNull(io.intent)
            io.failAfter = null
            assertTrue(StrategyRestoreTransactionRunner.recover(io))
            assertEquals(expected.journal, io.journal)
            assertEquals(expected.insights, io.insights)
            assertEquals(expected.experiments, io.experiments)
            assertNull(io.intent)
            assertFalse(StrategyRestoreTransactionRunner.recover(io))
        }
    }

    @Test fun damagedPendingTargetBlocksNewWritesRatherThanGettingDiscarded() {
        val io = InterruptedIo()
        io.intent = "broken non-json bytes".toByteArray()
        assertThrows(Exception::class.java) {
            StrategyRestoreTransactionRunner.commit(io, target())
        }
        assertArrayEquals("broken non-json bytes".toByteArray(), io.intent)
        assertTrue(io.journal.isEmpty())
        assertTrue(io.insights.isEmpty())
        assertTrue(io.experiments.isEmpty())
    }

    @Test fun explicitEmptyThirdAreaStillCommitsNotMissing() {
        val io = InterruptedIo()
        val original = target().copy(experiments = emptyList())
        StrategyRestoreTransactionRunner.commit(io, original)
        assertEquals(original.journal, io.journal)
        assertEquals(original.insights, io.insights)
        assertEquals(emptyList<StrategyExperiment>(), io.experiments)
        assertEquals(1, io.clearCount)
    }
}
