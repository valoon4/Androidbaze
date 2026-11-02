package com.valoon4.emotionskompass.core.insights

import kotlinx.serialization.json.*

/** Strict versioned JSON, shared by device-local EKS1 and EKB3 payloads. */
object StrategyExperimentFormat {
    const val MAX_BYTES = 1_450_000

    fun encode(items: List<StrategyExperiment>): ByteArray {
        StrategyExperimentChanges.validate(items)
        val root = buildJsonObject {
            put("version", JsonPrimitive(1))
            put("entries", buildJsonArray {
                items.forEach { add(toJson(it)) }
            })
        }
        return root.toString().toByteArray(Charsets.UTF_8).also { require(it.size <= MAX_BYTES) }
    }

    fun decode(bytes: ByteArray): List<StrategyExperiment> {
        require(bytes.size <= MAX_BYTES)
        val root = Json.parseToJsonElement(bytes.decodeToString()).jsonObject
        require(root.keys == setOf("version", "entries"))
        require(root.getValue("version").jsonPrimitive.content == "1")
        val items = root.getValue("entries").jsonArray
        require(items.size <= StrategyExperiment.MAX_EXPERIMENTS)
        return items.map { fromJson(it.jsonObject) }
            .also(StrategyExperimentChanges::validate)
    }

    fun toJson(item: StrategyExperiment): JsonObject = buildJsonObject {
        put("id", JsonPrimitive(item.id))
        put("createdAt", JsonPrimitive(item.createdAtUtcMillis))
        put("updatedAt", JsonPrimitive(item.updatedAtUtcMillis))
        put("strategyId", item.sourceStrategyId?.let(::JsonPrimitive) ?: JsonNull)
        put("customTitle", item.customTitle?.let(::JsonPrimitive) ?: JsonNull)
        put("titleFallback", item.sourceTitleFallback?.let(::JsonPrimitive) ?: JsonNull)
        put("plan", JsonPrimitive(item.planText))
        put("caution", JsonPrimitive(item.cautionText))
        put("status", JsonPrimitive(item.status.name))
        put("evaluation", item.evaluation?.let { JsonPrimitive(it.name) } ?: JsonNull)
        put("reflection", JsonPrimitive(item.reflectionText))
        put("archived", JsonPrimitive(item.archived))
        put("insightIds", JsonArray(item.relatedInsightIds.sorted().map(::JsonPrimitive)))
        put("journalIds", JsonArray(item.relatedJournalIds.sorted().map(::JsonPrimitive)))
    }

    fun fromJson(x: JsonObject): StrategyExperiment {
        require(x.keys == REQUIRED_KEYS) { "Strategy experiment format mismatch" }
        fun value(key: String): String = x.getValue(key).jsonPrimitive.content
        fun nullable(key: String): String? = x.getValue(key).let {
            if (it == JsonNull) null else it.jsonPrimitive.content
        }
        fun ids(key: String): Set<String> {
            val a = x.getValue(key).jsonArray.map { it.jsonPrimitive.content }
            require(a.size == a.distinct().size)
            return a.toSet()
        }
        val archived = when(value("archived")) {
            "true" -> true
            "false" -> false
            else -> throw IllegalArgumentException("Invalid archived flag")
        }
        return StrategyExperiment(
            id = value("id"),
            createdAtUtcMillis = value("createdAt").toLong(),
            updatedAtUtcMillis = value("updatedAt").toLong(),
            sourceStrategyId = nullable("strategyId"),
            customTitle = nullable("customTitle"),
            sourceTitleFallback = nullable("titleFallback"),
            planText = value("plan"), cautionText = value("caution"),
            status = ExperimentStatus.valueOf(value("status")),
            evaluation = nullable("evaluation")?.let(ExperimentEvaluation::valueOf),
            reflectionText = value("reflection"), archived = archived,
            relatedInsightIds = ids("insightIds"), relatedJournalIds = ids("journalIds")
        )
    }

    private val REQUIRED_KEYS = setOf(
        "id", "createdAt", "updatedAt", "strategyId", "customTitle", "titleFallback",
        "plan", "caution", "status", "evaluation", "reflection",
        "archived", "insightIds", "journalIds"
    )
}
