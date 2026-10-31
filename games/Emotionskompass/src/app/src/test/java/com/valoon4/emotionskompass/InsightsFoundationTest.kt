package com.valoon4.emotionskompass

import com.valoon4.emotionskompass.core.checkin.CheckInDraft
import com.valoon4.emotionskompass.core.checkin.LifeArea
import com.valoon4.emotionskompass.core.insights.CalculateOverview
import com.valoon4.emotionskompass.core.insights.InsightEvidence
import com.valoon4.emotionskompass.core.insights.InsightTimeWindow
import com.valoon4.emotionskompass.core.insights.JournalSnapshot
import com.valoon4.emotionskompass.core.journal.JournalEntry
import com.valoon4.emotionskompass.core.journal.JournalFormat
import org.junit.Assert.*
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class InsightsFoundationTest {
    private val berlin = ZoneId.of("Europe/Berlin")

    private fun entry(
        id: String,
        time: String,
        emotions: Map<String, Int?> = emptyMap(),
        needs: Set<String> = emptySet(),
        area: LifeArea? = null,
        narrative: String = ""
    ): JournalEntry {
        val created = Instant.parse(time).toEpochMilli()
        return JournalEntry(
            id = id, createdAt = created, updatedAt = created,
            draft = CheckInDraft(
                emotions = emotions, needs = needs, area = area,
                situation = narrative, note = narrative,
                interpretation = narrative, insight = narrative,
                nextStepNote = narrative
            )
        )
    }

    private fun range(start: String, end: String): InsightTimeWindow =
        InsightTimeWindow.inclusiveDays(LocalDate.parse(start), LocalDate.parse(end), berlin)

    @Test fun noEntriesDoNotProduceMisleadingPercentagesOrMentions() {
        val window = range("2026-10-01", "2026-10-30")
        val result = CalculateOverview.calculate(JournalSnapshot.fromEntries(emptyList()), window)
        assertEquals(0, result.entryCount)
        assertEquals(0, result.emotionMentionCount)
        assertEquals(0, result.needMentionCount)
        assertEquals(0, result.entriesWithEmotions)
        assertEquals(0, result.entriesWithNamedNeeds)
        assertTrue(result.emotionCounts.isEmpty())
        assertTrue(result.needCounts.isEmpty())
        assertNull(InsightEvidence(emptyList(), emptyList()).fraction)
    }

    @Test fun oneEntryCanMentionSeveralEmotionsAndNeedsButCountsOnceAsEntry() {
        val snapshot = JournalSnapshot.fromEntries(listOf(
            entry("a", "2026-10-29T08:00:00Z",
                emotions = mapOf("angst" to null, "freude" to 0, "ruhe" to 8),
                needs = setOf("klarheit", "naehe"), area = LifeArea.ALLTAG),
            entry("b", "2026-10-29T10:00:00Z",
                emotions = mapOf("freude" to 4), needs = setOf("klarheit")),
            entry("c", "2026-10-30T08:00:00Z")
        ))
        val result = CalculateOverview.calculate(snapshot, range("2026-10-29", "2026-10-30"))
        assertEquals(3, result.entryCount)
        assertEquals(2, result.entriesWithEmotions)
        assertEquals(2, result.entriesWithNamedNeeds)
        assertEquals(4, result.emotionMentionCount)
        assertEquals(3, result.needMentionCount)
        assertEquals("freude", result.emotionCounts.first().termId)
        assertEquals(2, result.emotionCounts.first().mentions)
        assertEquals(listOf("b", "a"), result.emotionCounts.first().sourceJournalIds)
        assertEquals("klarheit", result.needCounts.first().termId)
        assertEquals(listOf("b", "a"), result.needCounts.first().sourceJournalIds)
        assertEquals(listOf("c", "b", "a"), result.journalIds)
    }

    @Test fun missingIntensityAndExplicitZeroRemainDifferent() {
        val snapshot = JournalSnapshot.fromEntries(listOf(
            entry("a", "2026-10-30T10:00:00Z",
                emotions = mapOf("angst" to null, "freude" to 0))
        ))
        val item = snapshot.entries.single()
        assertTrue(item.hasEmotionWithoutIntensity("angst"))
        assertFalse(item.hasEmotionWithoutIntensity("freude"))
        assertTrue(item.emotionIntensities.containsKey("angst"))
        assertNull(item.emotionIntensities["angst"])
        assertEquals(0, item.emotionIntensities["freude"])
        val result = CalculateOverview.calculate(snapshot, range("2026-10-30", "2026-10-30"))
        assertEquals(2, result.emotionMentionCount)
    }

    @Test fun unknownNeedIsExplicitButNeverCountedAsNamedOrUnmetNeed() {
        val snapshot = JournalSnapshot.fromEntries(listOf(
            entry("unknown", "2026-10-30T08:00:00Z",
                needs = setOf(CheckInDraft.UNKNOWN_NEED_ID)),
            entry("blank", "2026-10-30T08:01:00Z"),
            entry("named", "2026-10-30T08:02:00Z", needs = setOf("klarheit"))
        ))
        val result = CalculateOverview.calculate(snapshot, range("2026-10-30", "2026-10-30"))
        assertEquals(3, result.entryCount)
        assertEquals(1, result.needMentionCount)
        assertEquals(1, result.entriesWithNamedNeeds)
        assertEquals(1, result.entriesWithUnknownNeedChoice)
        assertEquals(listOf("klarheit"), result.needCounts.map { it.termId })
        assertEquals(0, result.entriesWithEmotions)
    }

    @Test fun structuredProjectionExcludesEveryNarrativeAndSurvivesInputMutation() {
        val mutable = mutableMapOf<String, Int?>("freude" to 0)
        val source = entry("e", "2026-10-30T09:00:00Z",
            emotions = mutable, narrative = "Nicht in Auswertungen übernehmen")
        val original = JournalFormat.encode(listOf(source))
        val snapshot = JournalSnapshot.fromEntries(listOf(source))
        mutable["freude"] = 10
        assertEquals(0, snapshot.entries.single().emotionIntensities["freude"])
        assertFalse(snapshot.entries.single().toString().contains("Nicht in Auswertungen"))
        assertArrayEquals(original, JournalFormat.encode(listOf(source.copy(
            draft = source.draft.copy(emotions = mapOf("freude" to 0))
        ))))
    }

    @Test fun editsAndDeletionsAreRecomputedFromFreshEntriesWithoutStaleEvidence() {
        val first = entry("stable", "2026-10-30T08:00:00Z",
            emotions = mapOf("angst" to null), needs = setOf("klarheit"))
        val second = entry("other", "2026-10-30T08:30:00Z", needs = setOf("naehe"))
        val before = CalculateOverview.calculate(
            JournalSnapshot.fromEntries(listOf(first, second)),
            range("2026-10-30", "2026-10-30")
        )
        val edited = first.copy(updatedAt = first.updatedAt + 1,
            draft = first.draft.copy(emotions = mapOf("freude" to 2), needs = setOf("ruhe")))
        val after = CalculateOverview.calculate(
            JournalSnapshot.fromEntries(listOf(edited)),
            range("2026-10-30", "2026-10-30")
        )
        assertEquals(listOf("angst"), before.emotionCounts.map { it.termId })
        assertEquals(2, before.entryCount)
        assertEquals(1, after.entryCount)
        assertEquals(listOf("freude"), after.emotionCounts.map { it.termId })
        assertEquals(listOf("ruhe"), after.needCounts.map { it.termId })
        assertEquals(listOf("stable"), after.journalIds)
        assertTrue(after.needCounts.none { it.termId == "naehe" || it.termId == "klarheit" })
    }

    @Test fun firstDayAndEndExclusiveBoundariesRespectLocalDates() {
        val selected = listOf(
            entry("before", "2026-10-27T22:59:59.999Z"),
            entry("first", "2026-10-27T23:00:00Z"),
            entry("last", "2026-10-28T23:00:00Z"),
            entry("after", "2026-10-29T23:00:00Z")
        )
        // Germany in late October: UTC+1, midnight Oct 28 = Oct 27 23:00Z.
        val result = CalculateOverview.calculate(
            JournalSnapshot.fromEntries(selected), range("2026-10-28", "2026-10-29")
        )
        assertEquals(listOf("last", "first"), result.journalIds)
    }

    @Test fun springDstDayHas23HoursAndFallDstDayHas25Hours() {
        val spring = range("2026-03-29", "2026-03-29")
        val autumn = range("2026-10-25", "2026-10-25")
        assertEquals(Duration.ofHours(23).toMillis(),
            spring.endExclusiveUtcMillis - spring.startUtcMillis)
        assertEquals(Duration.ofHours(25).toMillis(),
            autumn.endExclusiveUtcMillis - autumn.startUtcMillis)
        assertFalse(spring.contains(spring.endExclusiveUtcMillis))
        assertTrue(spring.contains(spring.startUtcMillis))
    }

    @Test fun last7DaysIncludeCurrentLocalDayNot7Times24Hours() {
        val now = Instant.parse("2026-10-30T18:00:00Z")
        val window = InsightTimeWindow.recentDays(7, now, berlin)
        assertEquals(LocalDate.parse("2026-10-24"), window.firstDay)
        assertEquals(LocalDate.parse("2026-10-30"), window.lastDayInclusive)
        assertEquals(25, Duration.ofHours(25).toHours().toInt()) // DST anchor
        assertEquals(Duration.ofHours(169).toMillis(),
            window.endExclusiveUtcMillis - window.startUtcMillis)
        assertThrows(IllegalArgumentException::class.java) {
            InsightTimeWindow.recentDays(8, now, berlin)
        }
    }

    @Test fun manualDateRangeRejectsReverseOrUnboundedIntervals() {
        assertThrows(IllegalArgumentException::class.java) {
            range("2026-10-30", "2026-10-29")
        }
        assertThrows(IllegalArgumentException::class.java) {
            range("2000-01-01", "2026-10-30")
        }
    }

    @Test fun evidenceContractRequiresExplicitSubsetAndNoDuplicates() {
        val evidence = InsightEvidence(listOf("a", "b", "c", "d"), listOf("a", "d"))
        assertEquals(2, evidence.numerator)
        assertEquals(4, evidence.denominator)
        assertEquals(0.5, evidence.fraction!!, 0.000001)
        assertThrows(IllegalArgumentException::class.java) {
            InsightEvidence(listOf("a"), listOf("other"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            InsightEvidence(listOf("a", "a"), listOf("a"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            InsightEvidence(listOf("a"), listOf("a", "a"))
        }
    }

    @Test fun duplicateOrInvalidEntriesCannotEnterAnAnalysisSnapshot() {
        val e = entry("same", "2026-10-30T08:00:00Z")
        assertThrows(IllegalArgumentException::class.java) {
            JournalSnapshot.fromEntries(listOf(e, e))
        }
        assertThrows(IllegalArgumentException::class.java) {
            JournalSnapshot.fromEntries(listOf(e.copy(id = " ")))
        }
        assertThrows(IllegalArgumentException::class.java) {
            JournalSnapshot.fromEntries(listOf(e.copy(createdAt = 0)))
        }
        assertThrows(IllegalArgumentException::class.java) {
            JournalSnapshot.fromEntries(listOf(e.copy(updatedAt = 1)))
        }
        assertThrows(IllegalArgumentException::class.java) {
            JournalSnapshot.fromEntries(listOf(e.copy(
                draft = e.draft.copy(emotions = mapOf("f" to 99))
            )))
        }
        assertThrows(IllegalArgumentException::class.java) {
            JournalSnapshot.fromEntries((0..3000).map { e.copy(id = "id-$it") })
        }
    }

    @Test fun stableOrderingNeverDependsOnOriginalJournalOrder() {
        val left = entry("a", "2026-10-30T09:00:00Z",
            emotions = mapOf("freude" to null, "angst" to 2))
        val right = entry("b", "2026-10-30T09:00:00Z",
            emotions = mapOf("angst" to 6, "freude" to null))
        val window = range("2026-10-30", "2026-10-30")
        val one = CalculateOverview.calculate(JournalSnapshot.fromEntries(listOf(left, right)), window)
        val two = CalculateOverview.calculate(JournalSnapshot.fromEntries(listOf(right, left)), window)
        assertEquals(one, two)
        assertEquals(listOf("a", "b"), one.journalIds)
        assertEquals(listOf("angst", "freude"), one.emotionCounts.map { it.termId })
    }
}
