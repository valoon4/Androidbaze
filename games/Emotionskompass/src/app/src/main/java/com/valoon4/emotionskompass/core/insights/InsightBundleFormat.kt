package com.valoon4.emotionskompass.core.insights

import com.valoon4.emotionskompass.core.journal.JournalEntry
import com.valoon4.emotionskompass.core.journal.JournalFormat
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class InsightBackupContents(
    val journal: List<JournalEntry>,
    val insights: List<PersonalInsight>?
)

/** New backup payload; a null insight list signals legacy data, not deletion. */
object InsightBundleFormat {
    const val MAX_PLAINTEXT_BYTES = 4_500_000

    fun encode(journal: List<JournalEntry>, insights: List<PersonalInsight>): ByteArray {
        validateInsights(insights)
        val journalValue = Json.parseToJsonElement(JournalFormat.encode(journal).decodeToString())
        val root = buildJsonObject {
            put("version", JsonPrimitive(2))
            put("journal", journalValue)
            put("insights", buildJsonArray {
                insights.forEach { item ->
                    add(buildJsonObject {
                        put("id", JsonPrimitive(item.id))
                        put("createdAt", JsonPrimitive(item.createdAtUtcMillis))
                        put("updatedAt", JsonPrimitive(item.updatedAtUtcMillis))
                        put("text", JsonPrimitive(item.text))
                        put("archived", JsonPrimitive(item.archived))
                        put("journalIds", JsonArray(item.journalIds.sorted().map(::JsonPrimitive)))
                        put("emotionIds", JsonArray(item.emotionIds.sorted().map(::JsonPrimitive)))
                        put("needIds", JsonArray(item.needIds.sorted().map(::JsonPrimitive)))
                    })
                }
            })
        }
        return root.toString().toByteArray(Charsets.UTF_8).also {
            require(it.size <= MAX_PLAINTEXT_BYTES)
        }
    }

    fun decode(bytes: ByteArray): InsightBackupContents {
        require(bytes.size <= MAX_PLAINTEXT_BYTES)
        val root = Json.parseToJsonElement(bytes.decodeToString()).jsonObject
        require(root.getValue("version").jsonPrimitive.content == "2")
        val journal = JournalFormat.decode(root.getValue("journal").toString().toByteArray())
        val insights = root.getValue("insights").jsonArray.map { element ->
            val x = element.jsonObject
            fun string(name: String) = x.getValue(name).jsonPrimitive.content
            fun ids(name: String): Set<String> {
                val values = x.getValue(name).jsonArray.map { it.jsonPrimitive.content }
                require(values.size == values.distinct().size)
                return values.toSet()
            }
            PersonalInsight(
                id = string("id"),
                createdAtUtcMillis = string("createdAt").toLong(),
                updatedAtUtcMillis = string("updatedAt").toLong(),
                text = string("text"),
                archived = when (string("archived")) {
                    "true" -> true
                    "false" -> false
                    else -> throw IllegalArgumentException("Invalid status")
                },
                journalIds = ids("journalIds"),
                emotionIds = ids("emotionIds"),
                needIds = ids("needIds")
            )
        }
        validateInsights(insights)
        val journalIds = journal.mapTo(hashSetOf()) { it.id }
        require(insights.all { item -> item.journalIds.all { it in journalIds } })
        return InsightBackupContents(journal, insights)
    }

    fun validateInsights(items: List<PersonalInsight>) {
        require(items.size <= PersonalInsight.MAX_INSIGHTS)
        require(items.map { it.id }.distinct().size == items.size)
    }
}
