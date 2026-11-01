package com.valoon4.emotionskompass.core.insights

import kotlinx.serialization.json.*

/** Versioned representation used inside device-local encrypted storage. */
object PersonalInsightFormat {
    const val MAX_BYTES = 1_250_000

    fun encode(items: List<PersonalInsight>): ByteArray {
        InsightBundleFormat.validateInsights(items)
        val root = buildJsonObject {
            put("version", JsonPrimitive(1))
            put("entries", buildJsonArray {
                items.forEach { p ->
                    add(buildJsonObject {
                        put("id", JsonPrimitive(p.id))
                        put("createdAt", JsonPrimitive(p.createdAtUtcMillis))
                        put("updatedAt", JsonPrimitive(p.updatedAtUtcMillis))
                        put("text", JsonPrimitive(p.text))
                        put("archived", JsonPrimitive(p.archived))
                        put("journalIds", JsonArray(p.journalIds.sorted().map(::JsonPrimitive)))
                        put("emotionIds", JsonArray(p.emotionIds.sorted().map(::JsonPrimitive)))
                        put("needIds", JsonArray(p.needIds.sorted().map(::JsonPrimitive)))
                    })
                }
            })
        }
        return root.toString().toByteArray(Charsets.UTF_8).also { require(it.size <= MAX_BYTES) }
    }

    fun decode(bytes: ByteArray): List<PersonalInsight> {
        require(bytes.size <= MAX_BYTES)
        val obj = Json.parseToJsonElement(bytes.decodeToString()).jsonObject
        require(obj.getValue("version").jsonPrimitive.content == "1")
        val list = obj.getValue("entries").jsonArray
        require(list.size <= PersonalInsight.MAX_INSIGHTS)
        return list.map { element ->
            val x = element.jsonObject
            fun value(key: String) = x.getValue(key).jsonPrimitive.content
            fun ids(key: String): Set<String> {
                val items = x.getValue(key).jsonArray.map { it.jsonPrimitive.content }
                require(items.size == items.distinct().size)
                return items.toSet()
            }
            PersonalInsight(value("id"), value("createdAt").toLong(),
                value("updatedAt").toLong(), value("text"),
                archived = when (value("archived")) {
                    "true" -> true
                    "false" -> false
                    else -> error("Invalid status")
                },
                journalIds = ids("journalIds"), emotionIds = ids("emotionIds"),
                needIds = ids("needIds"))
        }.also { InsightBundleFormat.validateInsights(it) }
    }
}
