package com.valoon4.emotionskompass.core.insights

import com.valoon4.emotionskompass.core.checkin.CheckInDraft

/**
 * A TERM MENTION is an explicit ID selection in one journal entry, not
 * a count of how often a feeling/need was experienced. Every source entry
 * is linkable back to the existing encrypted journal, no NLP or diagnosis.
 */
data class InsightTermCount(
    val termId: String,
    val sourceJournalIds: List<String>
) {
    val mentions: Int get() = sourceJournalIds.size
}

/** Read-only result computed on demand; never written or cached to disk. */
data class InsightOverview(
    val window: InsightTimeWindow,
    val journalIds: List<String>,
    val entriesWithEmotions: Int,
    val entriesWithNamedNeeds: Int,
    val entriesWithUnknownNeedChoice: Int,
    val emotionCounts: List<InsightTermCount>,
    val needCounts: List<InsightTermCount>
) {
    val entryCount: Int get() = journalIds.size
    val emotionMentionCount: Int get() = emotionCounts.sumOf { it.mentions }
    val needMentionCount: Int get() = needCounts.sumOf { it.mentions }
}

/**
 * Pure calculation contract for M7.1. It operates only on the structured
 * on-device snapshot, respects explicit choices (including unknown), and
 * never reads the user's narrative text or invents missing needs.
 */
object CalculateOverview {
    fun calculate(snapshot: JournalSnapshot, window: InsightTimeWindow): InsightOverview {
        val chosen = snapshot.entries.filter { window.contains(it.createdAtUtcMillis) }

        fun terms(select: (InsightEntry) -> Iterable<String>): List<InsightTermCount> {
            val sources = linkedMapOf<String, MutableList<String>>()
            chosen.forEach { entry ->
                select(entry).distinct().forEach { id ->
                    sources.getOrPut(id) { mutableListOf() }.add(entry.journalId)
                }
            }
            return sources.map { (id, ids) -> InsightTermCount(id, ids.toList()) }
                .sortedWith(compareByDescending<InsightTermCount> { it.mentions }
                    .thenBy { it.termId })
        }

        val namedNeeds = { entry: InsightEntry ->
            entry.needIds.filter { it != CheckInDraft.UNKNOWN_NEED_ID }
        }

        return InsightOverview(
            window = window,
            journalIds = chosen.map { it.journalId },
            entriesWithEmotions = chosen.count { it.emotionIntensities.isNotEmpty() },
            entriesWithNamedNeeds = chosen.count { namedNeeds(it).isNotEmpty() },
            entriesWithUnknownNeedChoice =
                chosen.count { CheckInDraft.UNKNOWN_NEED_ID in it.needIds },
            emotionCounts = terms { it.emotionIntensities.keys },
            needCounts = terms(namedNeeds)
        )
    }
}
