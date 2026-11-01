package com.valoon4.emotionskompass.core.insights

import com.valoon4.emotionskompass.core.journal.JournalEntry
import kotlinx.serialization.json.*

/**
 * Explicit null means a legacy backup did not contain that data area.
 * Empty non-null lists mean the new backup intentionally contains zero entries.
 */
data class StrategyBackupContents(
    val journal: List<JournalEntry>,
    val insights: List<PersonalInsight>?,
    val experiments: List<StrategyExperiment>?
)

/** Complete, strictly versioned EKB3 plaintext for portable backup and EKR2 redo. */
object StrategyBundleFormat {
    const val MAX_PLAINTEXT_BYTES = 7_000_000

    fun encode(contents: StrategyBackupContents): ByteArray {
        val insights = requireNotNull(contents.insights)
        val experiments = requireNotNull(contents.experiments)
        StrategyExperimentChanges.validate(experiments)
        validateReferences(contents.journal, insights, experiments)
        val legacyBytes = InsightBundleFormat.encode(contents.journal, insights)
        val legacy = try { Json.parseToJsonElement(legacyBytes.decodeToString()).jsonObject }
                     finally { legacyBytes.fill(0) }
        val root = buildJsonObject {
            put("version", JsonPrimitive(3))
            put("journal", legacy.getValue("journal"))
            put("insights", legacy.getValue("insights"))
            put("strategyExperiments", JsonArray(experiments.map(StrategyExperimentFormat::toJson)))
        }
        return root.toString().toByteArray(Charsets.UTF_8).also {
            require(it.size <= MAX_PLAINTEXT_BYTES) { "Gesamtsicherung ist zu groß" }
        }
    }

    fun decode(bytes: ByteArray): StrategyBackupContents {
        require(bytes.size <= MAX_PLAINTEXT_BYTES)
        val root = Json.parseToJsonElement(bytes.decodeToString()).jsonObject
        require(root.keys == setOf("version", "journal", "insights", "strategyExperiments"))
        require(root.getValue("version").jsonPrimitive.content == "3")
        val legacyPayload = buildJsonObject {
            put("version", JsonPrimitive(2))
            put("journal", root.getValue("journal"))
            put("insights", root.getValue("insights"))
        }.toString().toByteArray(Charsets.UTF_8)
        val legacy = try { InsightBundleFormat.decode(legacyPayload) }
                     finally { legacyPayload.fill(0) }
        val items = root.getValue("strategyExperiments").jsonArray
        require(items.size <= StrategyExperiment.MAX_EXPERIMENTS)
        val experiments = items.map { StrategyExperimentFormat.fromJson(it.jsonObject) }
        StrategyExperimentChanges.validate(experiments)
        validateReferences(legacy.journal, requireNotNull(legacy.insights), experiments)
        return StrategyBackupContents(legacy.journal, legacy.insights, experiments)
    }

    fun validateReferences(
        journal: List<JournalEntry>,
        insights: List<PersonalInsight>,
        experiments: List<StrategyExperiment>
    ) {
        val journalIds = journal.mapTo(hashSetOf()) { it.id }
        val insightIds = insights.mapTo(hashSetOf()) { it.id }
        require(experiments.all { entry ->
            entry.relatedJournalIds.all { it in journalIds } &&
                entry.relatedInsightIds.all { it in insightIds }
        }) { "Verknüpfte Einträge fehlen in der Sicherung" }
    }
}
