package com.valoon4.emotionskompass.core.insights

import com.valoon4.emotionskompass.core.checkin.CheckInDraft
import com.valoon4.emotionskompass.core.journal.JournalEntry
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Fictional fixtures; weekly summaries are pure and not persisted. */
class WeeklyReviewTest {
    private val berlin = ZoneId.of("Europe/Berlin")

    private fun entry(
        id: String,
        day: LocalDate,
        hour: Int = 12,
        emotions: Map<String, Int?> = emptyMap(),
        needs: Set<String> = emptySet()
    ): JournalEntry {
        val time = day.atTime(hour, 0).atZone(berlin).toInstant().toEpochMilli()
        return JournalEntry(id, time, time, CheckInDraft(
            emotions = emotions, needs = needs
        ))
    }

    @Test fun completeMondaySundayWeeksCoverMonthWithoutGaps() {
        val result = WeeklyReview.month(
            JournalSnapshot.fromEntries(emptyList()), YearMonth.of(2026, 11), berlin
        )
        assertEquals(LocalDate.of(2026, 10, 26), result.weeks.first().firstDay)
        assertEquals(LocalDate.of(2026, 11, 1), result.weeks.first().lastDayInclusive)
        assertEquals(LocalDate.of(2026, 11, 30), result.weeks.last().firstDay)
        assertEquals(LocalDate.of(2026, 12, 6), result.weeks.last().lastDayInclusive)
        assertEquals(6, result.weeks.size)
        result.weeks.zipWithNext { a, b ->
            assertEquals(a.lastDayInclusive.plusDays(1), b.firstDay)
        }
        assertTrue(result.weeks.all { it.entryCount == 0 })
    }

    @Test fun countsAreDocumentedSelectionsNotIntensityAndUnknownNeedIsExcluded() {
        val monday = LocalDate.of(2026, 11, 2)
        val journal = listOf(
            entry("a", monday, emotions = mapOf("freude" to 0, "angst" to 9),
                needs = setOf("ruhe", CheckInDraft.UNKNOWN_NEED_ID)),
            entry("b", monday.plusDays(1), emotions = mapOf("freude" to null),
                needs = setOf("ruhe", "klarheit")),
            entry("c", monday.plusDays(6), emotions = mapOf("freude" to 8),
                needs = setOf(CheckInDraft.UNKNOWN_NEED_ID))
        )
        val week = WeeklyReview.month(
            JournalSnapshot.fromEntries(journal), YearMonth.of(2026, 11), berlin
        ).week(monday)!!
        assertEquals(3, week.entryCount)
        assertEquals(listOf("freude", "angst"), week.emotionCounts.map { it.termId })
        assertEquals(listOf(3, 1), week.emotionCounts.map { it.mentions })
        assertEquals(listOf("ruhe", "klarheit"), week.needCounts.map { it.termId })
        assertEquals(listOf(2, 1), week.needCounts.map { it.mentions })
        assertEquals(4, week.emotionCounts.sumOf { it.mentions })
        assertEquals(3, week.needCounts.sumOf { it.mentions })
        assertTrue(week.needCounts.none { it.termId == CheckInDraft.UNKNOWN_NEED_ID })
    }

    @Test fun weekAndTermEvidenceAlwaysReferenceRealJournalIds() {
        val monday = LocalDate.of(2026, 11, 2)
        val journal = listOf(
            entry("earliest", monday, 8, mapOf("freude" to 2)),
            entry("latest", monday, 19, mapOf("freude" to 0), setOf("ruhe")),
            entry("tuesday", monday.plusDays(1), 12, mapOf("angst" to 4), setOf("ruhe")),
            entry("outside", monday.plusDays(7), 10, mapOf("freude" to 2))
        )
        val summary = WeeklyReview.month(
            JournalSnapshot.fromEntries(journal), YearMonth.of(2026, 11), berlin
        )
        val week = summary.week(monday)!!
        assertEquals(listOf("tuesday", "latest", "earliest"), week.journalIds)
        assertEquals(listOf("latest", "earliest"),
            week.emotionCounts.first { it.termId == "freude" }.journalIds)
        assertEquals(listOf("tuesday", "latest"),
            week.needCounts.first { it.termId == "ruhe" }.journalIds)
        assertTrue("outside" !in week.journalIds)
    }

    @Test fun weeksSpanningMonthsAndYearsIncludeAllSevenDays() {
        val boundary = listOf(
            entry("dec", LocalDate.of(2026, 12, 31), 23),
            entry("jan", LocalDate.of(2027, 1, 1), 0)
        )
        val snapshot = JournalSnapshot.fromEntries(boundary)
        val december = WeeklyReview.month(snapshot, YearMonth.of(2026, 12), berlin)
        val january = WeeklyReview.month(snapshot, YearMonth.of(2027, 1), berlin)
        val weekStart = LocalDate.of(2026, 12, 28)
        assertEquals(2, december.week(weekStart)!!.entryCount)
        assertEquals(listOf("dec", "jan"), december.week(weekStart)!!.journalIds)
        assertEquals(2, january.week(weekStart)!!.entryCount)
        assertNull(december.week(LocalDate.of(2027, 1, 4)))
    }

    @Test fun daylightSavingSwitchesDoNotMoveRecordsToAdjacentWeeks() {
        val spring = LocalDate.of(2026, 3, 29)
        val autumn = LocalDate.of(2026, 10, 25)
        val snapshot = JournalSnapshot.fromEntries(listOf(
            entry("spring-before", spring, 1),
            entry("spring-after", spring, 3),
            entry("spring-next", spring.plusDays(1), 0),
            entry("autumn-before", autumn, 1),
            entry("autumn-after", autumn, 3),
            entry("autumn-next", autumn.plusDays(1), 0)
        ))
        val march = WeeklyReview.month(snapshot, YearMonth.of(2026, 3), berlin)
        assertEquals(2, march.week(LocalDate.of(2026, 3, 23))!!.entryCount)
        assertEquals(1, march.week(LocalDate.of(2026, 3, 30))!!.entryCount)
        val october = WeeklyReview.month(snapshot, YearMonth.of(2026, 10), berlin)
        assertEquals(2, october.week(LocalDate.of(2026, 10, 19))!!.entryCount)
        assertEquals(1, october.week(LocalDate.of(2026, 10, 26))!!.entryCount)
    }

    @Test fun zeroThroughThreeThousandEntriesRequireNoSavedWeekIndex() {
        val month = YearMonth.of(2026, 11)
        val monday = LocalDate.of(2026, 11, 2)
        val base = monday.atTime(12, 0).atZone(berlin).toInstant().toEpochMilli()
        for (size in listOf(0, 1, 10, 100, 1000, 3000)) {
            val journal = (0 until size).map { index ->
                val now = base + index * 1_000L
                JournalEntry("f-$index", now, now,
                    CheckInDraft(emotions = mapOf("freude" to null),
                        needs = setOf("ruhe")))
            }
            val summary = WeeklyReview.month(
                JournalSnapshot.fromEntries(journal), month, berlin
            )
            assertEquals(size, summary.week(monday)!!.entryCount)
            assertEquals(size, summary.week(monday)!!.emotionCounts
                .firstOrNull()?.mentions ?: 0)
            assertEquals(size, summary.week(monday)!!.needCounts
                .firstOrNull()?.mentions ?: 0)
            assertEquals(size, summary.weeks.sumOf { it.entryCount })
        }
    }

    @Test fun allLoadedEntriesAreAvailableOutsideStatisticsPresetWindow() {
        val oldMonday = LocalDate.of(2026, 8, 10)
        val source = JournalSnapshot.fromEntries(listOf(
            entry("old", oldMonday.plusDays(2)),
            entry("recent", LocalDate.of(2026, 11, 5))
        ))
        assertEquals(listOf("old"),
            WeeklyReview.month(source, YearMonth.of(2026, 8), berlin)
                .week(oldMonday)!!.journalIds)
        assertEquals(listOf("recent"),
            WeeklyReview.month(source, YearMonth.of(2026, 11), berlin)
                .week(LocalDate.of(2026, 11, 2))!!.journalIds)
    }

    @Test fun recalculationAfterEditOrDeleteChangesOnlyActualEvidence() {
        val monday = LocalDate.of(2026, 11, 2)
        val first = entry("a", monday, emotions = mapOf("freude" to 4))
        val second = entry("b", monday.plusDays(1), needs = setOf("ruhe"))
        fun week(journal: List<JournalEntry>) = WeeklyReview.month(
            JournalSnapshot.fromEntries(journal), YearMonth.of(2026, 11), berlin
        ).week(monday)!!
        assertEquals(2, week(listOf(first, second)).entryCount)
        assertEquals(1, week(listOf(first, second)).emotionCounts.first().mentions)
        assertTrue(week(listOf(second)).emotionCounts.isEmpty())
        assertEquals(listOf("b"), week(listOf(second)).journalIds)
    }
}
