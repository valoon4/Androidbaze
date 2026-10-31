package com.valoon4.emotionskompass.core.insights

import com.valoon4.emotionskompass.core.checkin.CheckInDraft
import com.valoon4.emotionskompass.core.journal.JournalEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class DiscoverPatternsTest {
    private val zone = ZoneId.of("Europe/Berlin")
    private val start = LocalDate.of(2026, 10, 1)
    private val window = InsightTimeWindow.inclusiveDays(start, start.plusDays(20), zone)

    private fun entries(count: Int, selected: Set<Int>): JournalSnapshot {
        val records = (0 until count).map { i ->
            val date = start.plusDays((i % 4).toLong())
            val time = date.atStartOfDay(zone).toInstant().toEpochMilli() + 3600000L + i
            JournalEntry(
                id = "record-$i",
                createdAt = time,
                updatedAt = time,
                draft = CheckInDraft(
                    emotions = if (i in selected) mapOf("freude" to null, "angst" to 0)
                        else mapOf("ruhe" to 2),
                    needs = if (i in selected) setOf("klarheit")
                        else setOf(CheckInDraft.UNKNOWN_NEED_ID)
                )
            )
        }
        return JournalSnapshot.fromEntries(records)
    }

    @Test fun missingOrSparseEvidenceProducesNoObservations() {
        assertTrue(DiscoverPatterns.discover(entries(9, setOf(0, 1, 2, 3)), window, zone).isEmpty())
        assertTrue(DiscoverPatterns.discover(entries(12, setOf(0, 1)), window, zone).isEmpty())
    }

    @Test fun trueDenominatorsAndJournalIdsRemainTraceable() {
        val snapshot = entries(12, setOf(0, 1, 2, 3))
        val observations = DiscoverPatterns.discover(snapshot, window, zone)
        val paired = observations.first { it.kind == "emotion-need" &&
            it.firstId == "freude" && it.secondId == "klarheit" }
        assertEquals(4, paired.evidence.numerator)
        // Only entries with BOTH a named need and an emotion are eligible.
        assertEquals(4, paired.evidence.denominator)
        assertEquals(setOf("record-0", "record-1", "record-2", "record-3"),
            paired.evidence.matchingJournalIds.toSet())
        assertEquals(4, paired.distinctDays)
        assertEquals(4, paired.evidence.eligibleJournalIds.distinct().size)
    }

    @Test fun noRepeatedDayPatternOrInventedUnknownNeed() {
        val oneDay = (0 until 12).map { i ->
            val time = start.atStartOfDay(zone).toInstant().toEpochMilli() + i * 1000L
            JournalEntry(
                "single-$i", time, time,
                CheckInDraft(
                    emotions = mapOf("freude" to null, "angst" to 0),
                    needs = setOf(CheckInDraft.UNKNOWN_NEED_ID)
                )
            )
        }
        val observations = DiscoverPatterns.discover(
            JournalSnapshot.fromEntries(oneDay), window, zone
        )
        assertTrue(observations.isEmpty())
    }

    @Test fun removingEvidenceRecomputesWithoutStaleLinks() {
        val before = DiscoverPatterns.discover(entries(12, setOf(0, 1, 2, 3)), window, zone)
        val after = DiscoverPatterns.discover(entries(12, setOf(0, 1)), window, zone)
        assertTrue(before.isNotEmpty())
        assertTrue(after.isEmpty())
    }
}
