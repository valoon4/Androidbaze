package com.valoon4.emotionskompass.core.insights

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID

/** Self-authored text, never derived from a pattern or automatically saved. */
data class PersonalInsight(
    val id: String,
    val createdAt: Long,
    val updatedAt: Long,
    val text: String,
    val archived: Boolean = false,
    val journalId: String? = null
)

/** Versioned, strictly bounded plaintext payload. Only the encrypted form is persisted. */
object PersonalInsightFormat {
    const val MAX_ITEMS = 1000
    const val MAX_TEXT_LENGTH = 1200
    const val MAX_PLAINTEXT_BYTES = 1_500_000
    private const val VERSION = 1

    fun newInsight(text: String, journalId: String? = null, now: Long = System.currentTimeMillis()): PersonalInsight {
        val cleaned = text.trim()
        require(cleaned.isNotEmpty() && cleaned.length <= MAX_TEXT_LENGTH)
        require(journalId == null || (journalId.isNotBlank() && journalId.length <= 100))
        require(now > 0)
        return PersonalInsight(UUID.randomUUID().toString(), now, now, cleaned, journalId = journalId)
    }

    private fun validate(items: List<PersonalInsight>) {
        require(items.size <= MAX_ITEMS) { "Zu viele Erkenntnisse." }
        require(items.map { it.id }.distinct().size == items.size) { "Doppelte Erkenntnis-ID." }
        items.forEach {
            require(it.id.isNotBlank() && it.id.length <= 100)
            require(it.createdAt > 0 && it.updatedAt >= it.createdAt)
            require(it.text.isNotBlank() && it.text.length <= MAX_TEXT_LENGTH)
            require(it.journalId == null || (it.journalId.isNotBlank() && it.journalId.length <= 100))
        }
    }

    fun encode(items: List<PersonalInsight>): ByteArray {
        validate(items)
        val bytes = buildJsonObject {
            put("version", JsonPrimitive(VERSION))
            put("insights", buildJsonArray {
                items.forEach { entry ->
                    add(buildJsonObject {
                        put("id", JsonPrimitive(entry.id))
                        put("createdAt", JsonPrimitive(entry.createdAt))
                        put("updatedAt", JsonPrimitive(entry.updatedAt))
                        put("text", JsonPrimitive(entry.text))
                        put("archived", JsonPrimitive(entry.archived))
                        put("journalId", entry.journalId?.let(::JsonPrimitive) ?: JsonNull)
                    })
                }
            })
        }.toString().toByteArray(Charsets.UTF_8)
        require(bytes.size <= MAX_PLAINTEXT_BYTES) { "Erkenntnissammlung ist zu groß." }
        return bytes
    }

    fun decode(bytes: ByteArray): List<PersonalInsight> {
        require(bytes.size <= MAX_PLAINTEXT_BYTES)
        val root = Json.parseToJsonElement(bytes.toString(Charsets.UTF_8)).jsonObject
        require(root.getValue("version").jsonPrimitive.intOrNull == VERSION)
        val raw = root.getValue("insights").jsonArray
        require(raw.size <= MAX_ITEMS)
        val items = raw.map { element ->
            val item = element.jsonObject
            fun str(name: String): String = item.getValue(name).jsonPrimitive.content
            PersonalInsight(
                id = str("id"),
                createdAt = str("createdAt").toLong(),
                updatedAt = str("updatedAt").toLong(),
                text = str("text"),
                archived = item.getValue("archived").jsonPrimitive.content.toBooleanStrict(),
                journalId = item.getValue("journalId").jsonPrimitive.contentOrNull
            )
        }
        validate(items)
        return items
    }
}
