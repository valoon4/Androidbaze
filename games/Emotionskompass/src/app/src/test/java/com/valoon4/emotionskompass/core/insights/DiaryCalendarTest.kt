package com.valoon4.emotionskompass.core.insights

import com.valoon4.emotionskompass.core.checkin.CheckInDraft
import com.valoon4.emotionskompass.core.journal.JournalEntry
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pure fictional fixtures; no Android Keystore, I/O or customer data. */
class DiaryCalendarTest {
    private val zone = ZoneId.of("Europe/Berlin")

    private fun entry(
        id: String,
        date: LocalDate,
        hour: Int = 12,
        minute: Int = 0
    ): JournalEntry {
        val timestamp = date.atTime(LocalTime.of(hour, minute))
            .atZone(zone).toInstant().toEpochMilli()
        return JournalEntry(id, timestamp, timestamp, CheckInDraft())
    }

    @Test fun emptyMonthAndLeapDayAreNeutralAndComplete() {
        val empty = JournalSnapshot.fromEntries(emptyList())
        val february = DiaryCalendar.month(empty, YearMonth.of(2028, 2), zone)
        assertEquals(29, february.days.size)
        assertTrue(february.days.all { it.entryCount == 0 && it.journalIds.isEmpty() })
        assertEquals(28, DiaryCalendar.month(empty, YearMonth.of(2026, 2), zone).days.size)
        assertEquals(LocalDate.of(2028, 2, 29), february.days.last().date)
    }

    @Test fun monthAndYearBoundariesUseActualLocalDates() {
        val atEnd = entry("october", LocalDate.of(2026, 10, 31), 23, 59)
        val atStart = entry("november", LocalDate.of(2026, 11, 1), 0, 0)
        val endYear = entry("december", LocalDate.of(2026, 12, 31), 23, 59)
        val newYear = entry("january", LocalDate.of(2027, 1, 1), 0, 0)
        val snapshot = JournalSnapshot.fromEntries(listOf(atEnd, atStart, endYear, newYear))
        assertEquals(listOf("october"), DiaryCalendar.month(snapshot,
            YearMonth.of(2026, 10), zone).days.last().journalIds)
        assertEquals(listOf("november"), DiaryCalendar.month(snapshot,
            YearMonth.of(2026, 11), zone).days.first().journalIds)
        assertEquals(listOf("december"), DiaryCalendar.month(snapshot,
            YearMonth.of(2026, 12), zone).days.last().journalIds)
        assertEquals(listOf("january"), DiaryCalendar.month(snapshot,
            YearMonth.of(2027, 1), zone).days.first().journalIds)
    }

    @Test fun daylightSavingChangesDoNotShiftEntriesToNeighbouringDays() {
        val spring = LocalDate.of(2026, 3, 29)
        val autumn = LocalDate.of(2026, 10, 25)
        val snapshot = JournalSnapshot.fromEntries(listOf(
            entry("spring-before", spring, 1, 30),
            entry("spring-after", spring, 3, 30),
            entry("spring-next", spring.plusDays(1), 0, 0),
            entry("autumn-before", autumn, 1, 30),
            entry("autumn-after", autumn, 3, 30)
        ))
        val march = DiaryCalendar.month(snapshot, YearMonth.of(2026, 3), zone)
        assertEquals(2, march.day(spring)!!.entryCount)
        assertEquals(1, march.day(spring.plusDays(1))!!.entryCount)
        val october = DiaryCalendar.month(snapshot, YearMonth.of(2026, 10), zone)
        assertEquals(2, october.day(autumn)!!.entryCount)
    }

    @Test fun multipleEntriesSameDayKeepTrueIDsAndTimeOrder() {
        val date = LocalDate.of(2026, 11, 4)
        val snapshot = JournalSnapshot.fromEntries(listOf(
            entry("early", date, 8), entry("later", date, 19),
            entry("other", date.minusDays(1))
        ))
        val month = DiaryCalendar.month(snapshot, YearMonth.of(2026, 11), zone)
        assertEquals(listOf("later", "early"), month.day(date)!!.journalIds)
        assertEquals(1, month.day(date.minusDays(1))!!.entryCount)
        assertEquals(3, month.days.sumOf { it.entryCount })
        assertEquals(null, month.day(LocalDate.of(2026, 10, 31)))
    }

    @Test fun largerJournalsAreProjectedWithoutAdditionalPersistence() {
        val month = YearMonth.of(2026, 11)
        for (size in listOf(0, 1, 10, 100, 1000, 3000)) {
            val date = LocalDate.of(2026, 11, 4)
            val base = date.atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
            val entries = (0 until size).map { number ->
                val time = base + number * 1_000L
                JournalEntry("fixture-$number", time, time, CheckInDraft())
            }
            val result = DiaryCalendar.month(JournalSnapshot.fromEntries(entries), month, zone)
            assertEquals(size, result.day(date)!!.entryCount)
            assertEquals(size, result.days.sumOf { it.entryCount })
            assertEquals(30, result.days.size)
        }
    }

    @Test fun calendarReadsFullSnapshotIndependentOfPresetStatisticsWindow() {
        val current = entry("newer", LocalDate.of(2026, 11, 5))
        val old = entry("older", LocalDate.of(2026, 8, 14))
        val snapshot = JournalSnapshot.fromEntries(listOf(current, old))
        assertEquals(listOf("older"), DiaryCalendar.month(snapshot,
            YearMonth.of(2026, 8), zone).day(LocalDate.of(2026, 8, 14))!!.journalIds)
    }
}
