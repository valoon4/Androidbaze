package com.valoon4.emotionskompass.core.insights

import com.valoon4.emotionskompass.core.checkin.CheckInDraft
import com.valoon4.emotionskompass.core.journal.JournalEntry
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * M7.6: Cross-feature regression of two independent read-only views of the
 * same authenticated journal. All fixtures are fictional and stay in memory.
 *
 * A single old or duplicated evidence ID would silently undermine the
 * user's ability to inspect the source behind a weekly or daily count.
 */
class M7ReleaseConsistencyTest {
    private val berlin = ZoneId.of("Europe/Berlin")

    private fun entry(
        id: String,
        date: LocalDate,
        hour: Int = 12,
        zone: ZoneId = berlin,
        emotions: Map<String, Int?> = emptyMap(),
        needs: Set<String> = emptySet()
    ): JournalEntry {
        val at = date.atTime(hour, 0).atZone(zone).toInstant().toEpochMilli()
        return JournalEntry(
            id, at, at,
            CheckInDraft(emotions = emotions, needs = needs)
        )
    }

    @Test fun everyDayAndWeekUseTheExactSameSourceEvidenceForZeroToThreeThousandEntries() {
        for (size in listOf(0, 1, 10, 100, 1000, 3000)) {
            // Spread records across three months, with repeated timestamps,
            // year boundaries, null/zero intensity and unknown needs.
            val journal = (0 until size).map { i ->
                val date = LocalDate.of(2026, 11, 20).plusDays((i * 17L) % 70L)
                val emotions = when (i % 4) {
                    0 -> mapOf("freude" to null)
                    1 -> mapOf("freude" to 0, "angst" to 10)
                    2 -> mapOf("angst" to null)
                    else -> emptyMap()
                }
                val needs = when (i % 3) {
                    0 -> setOf("ruhe", CheckInDraft.UNKNOWN_NEED_ID)
                    1 -> setOf("klarheit")
                    else -> setOf(CheckInDraft.UNKNOWN_NEED_ID)
                }
                entry("fictional-$i", date, (i * 7) % 24, emotions = emotions,
                    needs = needs)
            }
            val source = JournalSnapshot.fromEntries(journal)
            assertEquals(size, source.size)
            val allIds = source.entries.map { it.journalId }.toSet()

            for (month in listOf(
                YearMonth.of(2026, 11), YearMonth.of(2026, 12),
                YearMonth.of(2027, 1)
            )) {
                val calendar = DiaryCalendar.month(source, month, berlin)
                val weeks = WeeklyReview.month(source, month, berlin)
                assertEquals(month.lengthOfMonth(), calendar.days.size)

                val expectedInMonth = source.entries.filter {
                    YearMonth.from(Instant.ofEpochMilli(it.createdAtUtcMillis)
                        .atZone(berlin)) == month
                }.map { it.journalId }.toSet()
                assertEquals(expectedInMonth,
                    calendar.days.flatMap { it.journalIds }.toSet())
                assertEquals(expectedInMonth.size,
                    calendar.days.sumOf { it.entryCount })
                assertEquals(expectedInMonth,
                    weeks.weeks.flatMap { it.journalIds }
                        .filter { it in expectedInMonth }.toSet())

                for (day in calendar.days) {
                    val expected = source.entries.filter {
                        Instant.ofEpochMilli(it.createdAtUtcMillis)
                            .atZone(berlin).toLocalDate() == day.date
                    }.map { it.journalId }
                    assertEquals("Wrong day: size=$size day=${day.date}",
                        expected, day.journalIds)
                    assertEquals(day.journalIds.size,
                        day.journalIds.toSet().size)
                    val monday = day.date.with(
                        TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    val week = weeks.week(monday)!!
                    val dayIdSet = day.journalIds.toSet()
                    assertEquals("The week lost the day's actual evidence",
                        day.journalIds,
                        week.journalIds.filter { it in dayIdSet })
                }

                for (week in weeks.weeks) {
                    val expected = source.entries.filter { item ->
                        val date = Instant.ofEpochMilli(item.createdAtUtcMillis)
                            .atZone(berlin).toLocalDate()
                        !date.isBefore(week.firstDay) &&
                            !date.isAfter(week.lastDayInclusive)
                    }.map { it.journalId }
                    assertEquals("Wrong week: size=$size monday=${week.firstDay}",
                        expected, week.journalIds)
                    assertEquals(week.entryCount, week.journalIds.toSet().size)
                    assertTrue(week.journalIds.all { it in allIds })
                    week.emotionCounts.forEach { term ->
                        assertEquals(
                            week.journalIds.filter { id ->
                                val item = source.entries.first { it.journalId == id }
                                term.termId in item.emotionIntensities
                            }, term.journalIds
                        )
                    }
                    week.needCounts.forEach { term ->
                        assertFalse(term.termId == CheckInDraft.UNKNOWN_NEED_ID)
                        assertEquals(
                            week.journalIds.filter { id ->
                                val item = source.entries.first { it.journalId == id }
                                term.termId in item.needIds
                            }, term.journalIds
                        )
                    }
                }
            }
        }
    }

    @Test fun freshSnapshotAfterEditingOrDeletingCannotExposePreviousDayOrWeekEvidence() {
        val december = YearMonth.of(2026, 12)
        val january = YearMonth.of(2027, 1)
        val monday = LocalDate.of(2026, 12, 28)
        val original = listOf(
            entry("edited", LocalDate.of(2026, 12, 31),
                emotions = mapOf("freude" to 10), needs = setOf("ruhe")),
            entry("deleted", LocalDate.of(2027, 1, 1),
                emotions = mapOf("angst" to null)),
            entry("untouched", LocalDate.of(2027, 1, 2),
                needs = setOf("klarheit"))
        )
        val before = JournalSnapshot.fromEntries(original)
        assertEquals(3, WeeklyReview.month(before, january, berlin)
            .week(monday)!!.entryCount)
        assertEquals(listOf("edited"),
            DiaryCalendar.month(before, december, berlin)
                .day(LocalDate.of(2026, 12, 31))!!.journalIds)

        // Simulate a committed edit (same stable ID, new day and selections)
        // and a deletion. No cache, week index or history is permitted.
        val changed = listOf(
            entry("edited", LocalDate.of(2027, 1, 4),
                emotions = mapOf("freude" to null), needs = setOf("sicherheit")),
            original.last()
        )
        val after = JournalSnapshot.fromEntries(changed)
        val decemberAfter = DiaryCalendar.month(after, december, berlin)
        val januaryAfter = DiaryCalendar.month(after, january, berlin)
        val weekAfter = WeeklyReview.month(after, january, berlin)
        assertTrue(decemberAfter.day(LocalDate.of(2026, 12, 31))!!
            .journalIds.isEmpty())
        assertEquals(listOf("edited"),
            januaryAfter.day(LocalDate.of(2027, 1, 4))!!.journalIds)
        assertEquals(listOf("untouched"), weekAfter.week(monday)!!.journalIds)
        assertEquals(listOf("edited"),
            weekAfter.week(LocalDate.of(2027, 1, 4))!!.journalIds)
        assertTrue(weekAfter.weeks.flatMap { it.journalIds }.none { it == "deleted" })
        assertTrue(weekAfter.week(monday)!!.emotionCounts.isEmpty())
        assertEquals(listOf("sicherheit"),
            weekAfter.week(LocalDate.of(2027, 1, 4))!!.needCounts
                .map { it.termId })
    }

    @Test fun localTimeZonesAreAppliedConsistentlyWithoutA24HourDayAssumption() {
        // An instant that is still Sunday in UTC may already be Monday
        // in Europe/Berlin (including at a DST boundary).
        val instant = Instant.parse("2026-03-29T22:30:00Z").toEpochMilli()
        val source = JournalSnapshot.fromEntries(listOf(
            JournalEntry("timezone-boundary", instant, instant, CheckInDraft(
                emotions = mapOf("freude" to 0)))
        ))
        val month = YearMonth.of(2026, 3)
        for (zone in listOf(ZoneId.of("UTC"), berlin)) {
            val date = Instant.ofEpochMilli(instant).atZone(zone).toLocalDate()
            val calendar = DiaryCalendar.month(source, month, zone)
            val week = WeeklyReview.month(source, month, zone).week(
                date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            )!!
            assertEquals(listOf("timezone-boundary"),
                calendar.day(date)!!.journalIds)
            assertEquals(listOf("timezone-boundary"), week.journalIds)
            assertEquals(1, week.emotionCounts.single().mentions)
        }
    }
}
