package com.valoon4.emotionskompass.core.insights

import com.valoon4.emotionskompass.core.checkin.CheckInDraft
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/** An exact number of journal records containing a user-selected term. */
data class WeeklyTermCount(
    val termId: String,
    val journalIds: List<String>
) {
    val mentions: Int get() = journalIds.size
}

/** A complete local Monday–Sunday week, including any adjacent-month days. */
data class WeeklyReviewWeek(
    val firstDay: LocalDate,
    val lastDayInclusive: LocalDate,
    val journalIds: List<String>,
    val emotionCounts: List<WeeklyTermCount>,
    val needCounts: List<WeeklyTermCount>
) {
    val entryCount: Int get() = journalIds.size
}

/** Only the four to six complete weeks intersecting the chosen local month. */
data class WeeklyReviewMonth(
    val month: YearMonth,
    val weeks: List<WeeklyReviewWeek>
) {
    fun week(firstDay: LocalDate): WeeklyReviewWeek? =
        weeks.firstOrNull { it.firstDay == firstDay }
}

/**
 * Read-only, transient view of the complete AUTHENTICATED journal snapshot.
 *
 * A count means "documented in N journal entries", never intensity, duration,
 * psychological progress or actual frequency of lived feelings. This function
 * does not use 7/30/90-day statistics filters, free text or persistent storage.
 */
object WeeklyReview {
    fun month(
        snapshot: JournalSnapshot,
        month: YearMonth,
        zone: ZoneId
    ): WeeklyReviewMonth {
        val firstMonday = month.atDay(1).with(
            TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
        )
        val lastSunday = month.atEndOfMonth().with(
            TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)
        )
        val bounds = InsightTimeWindow.inclusiveDays(firstMonday, lastSunday, zone)
        // JournalSnapshot has already verified unique IDs and sorts newest first.
        // Grouping only passes explicit IDs and structured term selections.
        val byWeek = snapshot.entries.asSequence()
            .filter { bounds.contains(it.createdAtUtcMillis) }
            .groupBy { entry ->
                Instant.ofEpochMilli(entry.createdAtUtcMillis).atZone(zone)
                    .toLocalDate()
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            }

        return WeeklyReviewMonth(
            month = month,
            weeks = generateSequence(firstMonday) { current ->
                current.plusWeeks(1).takeUnless { it.isAfter(lastSunday) }
            }.map { monday ->
                val entries = byWeek[monday].orEmpty()
                val emotions = linkedMapOf<String, MutableList<String>>()
                val needs = linkedMapOf<String, MutableList<String>>()
                entries.forEach { entry ->
                    entry.emotionIntensities.keys.forEach { id ->
                        emotions.getOrPut(id) { mutableListOf() }.add(entry.journalId)
                    }
                    entry.needIds.asSequence()
                        .filter { it != CheckInDraft.UNKNOWN_NEED_ID }
                        .forEach { id ->
                            needs.getOrPut(id) { mutableListOf() }.add(entry.journalId)
                        }
                }
                fun sortedCounts(source: Map<String, List<String>>): List<WeeklyTermCount> =
                    source.map { (id, ids) -> WeeklyTermCount(id, ids) }
                        .sortedWith(compareByDescending<WeeklyTermCount> { it.mentions }
                            .thenBy { it.termId })
                WeeklyReviewWeek(
                    firstDay = monday,
                    lastDayInclusive = monday.plusDays(6),
                    journalIds = entries.map { it.journalId },
                    emotionCounts = sortedCounts(emotions),
                    needCounts = sortedCounts(needs)
                )
            }.toList()
        )
    }
}
