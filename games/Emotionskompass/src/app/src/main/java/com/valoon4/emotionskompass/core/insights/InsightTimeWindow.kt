package com.valoon4.emotionskompass.core.insights

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * A half-open UTC interval, derived from *local calendar days* in the selected
 * timezone. DST changes are handled by ZoneId.atStartOfDay, never by adding
 * 24 hours to a local midnight.
 */
data class InsightTimeWindow private constructor(
    val firstDay: LocalDate,
    val lastDayInclusive: LocalDate,
    val zone: ZoneId,
    val startUtcMillis: Long,
    val endExclusiveUtcMillis: Long
) {
    init { require(endExclusiveUtcMillis > startUtcMillis) }

    fun contains(utcMillis: Long): Boolean =
        utcMillis >= startUtcMillis && utcMillis < endExclusiveUtcMillis

    companion object {
        private const val MAX_WINDOW_DAYS = 3650L

        /** A user-defined inclusive range of local calendar dates. */
        fun inclusiveDays(first: LocalDate, last: LocalDate, zone: ZoneId): InsightTimeWindow {
            require(!last.isBefore(first)) { "Date range is reversed" }
            require(ChronoUnit.DAYS.between(first, last) < MAX_WINDOW_DAYS) {
                "Date range too large"
            }
            return InsightTimeWindow(
                firstDay = first, lastDayInclusive = last, zone = zone,
                startUtcMillis = first.atStartOfDay(zone).toInstant().toEpochMilli(),
                endExclusiveUtcMillis = last.plusDays(1).atStartOfDay(zone)
                    .toInstant().toEpochMilli()
            )
        }

        /** Last 7/30/90 local days INCLUDING today, not arbitrary 24h blocks. */
        fun recentDays(days: Int, now: Instant, zone: ZoneId): InsightTimeWindow {
            require(days == 7 || days == 30 || days == 90) {
                "Only the proposed preset windows are supported"
            }
            val today = now.atZone(zone).toLocalDate()
            return inclusiveDays(today.minusDays((days - 1).toLong()), today, zone)
        }
    }
}
