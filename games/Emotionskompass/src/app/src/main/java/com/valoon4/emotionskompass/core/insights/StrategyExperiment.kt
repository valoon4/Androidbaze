package com.valoon4.emotionskompass.core.insights

import java.util.UUID

/** Only user-authored ideas, never automatically inferred from observed patterns. */
enum class ExperimentStatus { OPEN, SAVED_FOR_LATER, TRIED, NOT_TRIED, CLOSED }
enum class ExperimentEvaluation { HELPFUL, PARTLY, NOT_FITTING, UNCLEAR }

data class StrategyExperiment(
    val id: String,
    val createdAtUtcMillis: Long,
    val updatedAtUtcMillis: Long,
    val sourceStrategyId: String? = null,
    val customTitle: String? = null,
    val sourceTitleFallback: String? = null,
    val planText: String = "",
    val cautionText: String = "",
    val status: ExperimentStatus = ExperimentStatus.OPEN,
    val evaluation: ExperimentEvaluation? = null,
    val reflectionText: String = "",
    val archived: Boolean = false,
    val relatedInsightIds: Set<String> = emptySet(),
    val relatedJournalIds: Set<String> = emptySet()
) {
    init {
        require(runCatching { UUID.fromString(id) }.isSuccess)
        require(createdAtUtcMillis > 0 && updatedAtUtcMillis >= createdAtUtcMillis)
        require((sourceStrategyId != null) xor (customTitle != null))
        require(sourceStrategyId == null || Regex("^[a-z0-9-]{3,70}$").matches(sourceStrategyId))
        require(customTitle == null || (customTitle == customTitle.trim() &&
            customTitle.isNotBlank() && customTitle.length <= MAX_TITLE))
        require(sourceTitleFallback == null || (sourceTitleFallback == sourceTitleFallback.trim() &&
            sourceTitleFallback.isNotBlank() && sourceTitleFallback.length <= MAX_TITLE))
        require(sourceStrategyId != null || sourceTitleFallback == null)
        require(planText.length <= MAX_PLAN && cautionText.length <= MAX_CAUTION &&
            reflectionText.length <= MAX_REFLECTION)
        require(relatedInsightIds.size <= MAX_LINKS && relatedJournalIds.size <= MAX_LINKS)
        require((relatedInsightIds + relatedJournalIds).all {
            runCatching { UUID.fromString(it) }.isSuccess
        })
    }

    companion object {
        const val MAX_TITLE = 140
        const val MAX_PLAN = 800
        const val MAX_CAUTION = 800
        const val MAX_REFLECTION = 1200
        const val MAX_LINKS = 12
        const val MAX_EXPERIMENTS = 300

        fun fromOwnIdea(title: String, now: Long): StrategyExperiment =
            StrategyExperiment(UUID.randomUUID().toString(), now, now, customTitle = title.trim())

        fun fromCatalog(id: String, title: String, now: Long): StrategyExperiment =
            StrategyExperiment(UUID.randomUUID().toString(), now, now,
                sourceStrategyId = id, sourceTitleFallback = title.trim())
    }
}

/** Referential cleanup never removes the user's own words. */
object StrategyExperimentChanges {
    fun save(items: List<StrategyExperiment>, next: StrategyExperiment): List<StrategyExperiment> {
        validate(items)
        val old = items.firstOrNull { it.id == next.id }
        require(old != null || items.size < StrategyExperiment.MAX_EXPERIMENTS)
        require(old == null || (old.createdAtUtcMillis == next.createdAtUtcMillis &&
            next.updatedAtUtcMillis >= old.updatedAtUtcMillis))
        return (items.filterNot { it.id == next.id } + next)
            .sortedWith(compareByDescending<StrategyExperiment> { it.updatedAtUtcMillis }.thenBy { it.id })
    }

    fun delete(items: List<StrategyExperiment>, id: String): List<StrategyExperiment> =
        items.filterNot { it.id == id }

    fun pruneMissingReferences(
        items: List<StrategyExperiment>, journalIds: Set<String>,
        insightIds: Set<String>, now: Long
    ): List<StrategyExperiment> = items.map { old ->
        val journal = old.relatedJournalIds.intersect(journalIds)
        val insights = old.relatedInsightIds.intersect(insightIds)
        if (journal == old.relatedJournalIds && insights == old.relatedInsightIds) old
        else old.copy(relatedJournalIds = journal, relatedInsightIds = insights,
            updatedAtUtcMillis = maxOf(now, old.updatedAtUtcMillis))
    }

    fun validate(items: List<StrategyExperiment>) {
        require(items.size <= StrategyExperiment.MAX_EXPERIMENTS)
        require(items.map { it.id }.distinct().size == items.size)
    }
}
