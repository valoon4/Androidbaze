package com.valoon4.emotionskompass.core.insights

import com.valoon4.emotionskompass.core.checkin.CheckInDraft
import com.valoon4.emotionskompass.core.checkin.LifeArea
import com.valoon4.emotionskompass.core.journal.JournalEntry
import com.valoon4.emotionskompass.core.journal.JournalFormat

/**
 * Ephemeral, read-only projection of an authenticated journal read.
 *
 * Only structured, explicitly selected user input is projected. No narrative
 * text, notes, interpretation, private insight, body cues or future action
 * description crosses the statistics boundary. No disk/DB/cache/logging.
 *
 * The caller (AppViewModel) is responsible for enforcing the app-lock gate
 * before constructing or retaining a snapshot. This class has no key access.
 */
data class InsightEntry internal constructor(
    val journalId: String,
    val createdAtUtcMillis: Long,
    val updatedAtUtcMillis: Long,
    val emotionIntensities: Map<String, Int?>,
    val needIds: Set<String>,
    val area: LifeArea?
) {
    /** The user explicitly selected this but did not rate intensity. */
    fun hasEmotionWithoutIntensity(id: String): Boolean =
        emotionIntensities.containsKey(id) && emotionIntensities[id] == null
}

/** No stored state or cached derived metrics: produce this only on demand. */
class JournalSnapshot private constructor(val entries: List<InsightEntry>) {
    val size: Int get() = entries.size

    companion object {
        fun fromEntries(journalEntries: List<JournalEntry>): JournalSnapshot {
            require(journalEntries.size <= JournalFormat.MAX_ENTRIES) {
                "Journal snapshot exceeds the supported entry count"
            }
            require(journalEntries.map { it.id }.distinct().size == journalEntries.size) {
                "Duplicate journal entry ID"
            }

            val projected = journalEntries.map { entry ->
                require(entry.id.isNotBlank() && entry.createdAt > 0L &&
                    entry.updatedAt >= entry.createdAt) { "Invalid journal entry metadata" }
                require(entry.draft.emotions.all { (id, value) ->
                    id.isNotBlank() && (value == null || value in 0..10)
                }) { "Invalid structured emotion value" }
                require(entry.draft.needs.none { it.isBlank() }) { "Invalid need ID" }
                InsightEntry(
                    journalId = entry.id,
                    createdAtUtcMillis = entry.createdAt,
                    updatedAtUtcMillis = entry.updatedAt,
                    emotionIntensities = entry.draft.emotions.toMap(),
                    // Keep __unklar__ here to distinguish explicit uncertainty;
                    // aggregations must not count it as a named unmet need.
                    needIds = entry.draft.needs.toSet(),
                    area = entry.draft.area
                )
            }.sortedWith(compareByDescending<InsightEntry> { it.createdAtUtcMillis }
                .thenBy { it.journalId })
            return JournalSnapshot(projected)
        }
    }
}

/**
 * A suggested pattern MUST supply its true eligible sample, rather than
 * presenting matching rows as if they were the denominator. No diagnosis,
 * correlation, score or interpretation is implied by this structural type.
 */
class InsightEvidence(
    val eligibleJournalIds: List<String>,
    val matchingJournalIds: List<String>
) {
    init {
        require(eligibleJournalIds.size == eligibleJournalIds.distinct().size)
        require(matchingJournalIds.size == matchingJournalIds.distinct().size)
        require(eligibleJournalIds.none { it.isBlank() })
        require(matchingJournalIds.none { it.isBlank() })
        require(matchingJournalIds.all { it in eligibleJournalIds }) {
            "Evidence must be a subset of eligible journal entries"
        }
    }

    val numerator: Int get() = matchingJournalIds.size
    val denominator: Int get() = eligibleJournalIds.size

    /** A denominator of zero has no percentage, rather than 0% or 100%. */
    val fraction: Double? get() = if (denominator == 0) null
        else numerator.toDouble() / denominator
}
