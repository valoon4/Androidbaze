package com.valoon4.emotionskompass.core.insights

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

/** One local calendar day; only authenticated journal IDs, never notes or scores. */
data class DiaryCalendarDay(
    val date: LocalDate,
    val journalIds: List<String>
) {
    val entryCount: Int get() = journalIds.size
}

/**
 * Read-only, transient calendar view of the ENTIRE authenticated journal.
 * It intentionally does not accept a 7/30/90-day statistics filter.
 */
data class DiaryCalendarMonth(
    val month: YearMonth,
    val days: List<DiaryCalendarDay>
) {
    fun day(date: LocalDate): DiaryCalendarDay? =
        days.firstOrNull { it.date == date }
}

object DiaryCalendar {
    fun month(
        snapshot: JournalSnapshot,
        month: YearMonth,
        zone: ZoneId
    ): DiaryCalendarMonth {
        // Local midnight may have a 23- or 25-hour interval during DST.
        val bounds = InsightTimeWindow.inclusiveDays(
            month.atDay(1), month.atEndOfMonth(), zone
        )
        val idsByDay = snapshot.entries.asSequence()
            .filter { bounds.contains(it.createdAtUtcMillis) }
            .groupBy {
                Instant.ofEpochMilli(it.createdAtUtcMillis).atZone(zone).toLocalDate()
            }
            .mapValues { (_, entries) -> entries.map { it.journalId } }

        return DiaryCalendarMonth(
            month = month,
            days = (1..month.lengthOfMonth()).map { day ->
                val date = month.atDay(day)
                DiaryCalendarDay(date, idsByDay[date].orEmpty())
            }
        )
    }
}
