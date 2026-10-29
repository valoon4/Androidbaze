package com.valoon4.emotionskompass.core.journal

import com.valoon4.emotionskompass.core.checkin.BodyCue
import com.valoon4.emotionskompass.core.checkin.CheckInDraft
import com.valoon4.emotionskompass.core.checkin.CheckInMode
import com.valoon4.emotionskompass.core.checkin.LifeArea
import com.valoon4.emotionskompass.core.checkin.NextStepIdea
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID

/** All fields are encrypted together, including timestamps and tags. */
data class JournalEntry(
    val id: String,
    val createdAt: Long,
    val updatedAt: Long,
    val draft: CheckInDraft
)

/**
 * A deliberately strict, versioned format. Raw JSON is never written to persistent
 * storage: the repository encrypts the entire UTF-8 payload before AtomicFile writes.
 * Only user data is persisted; UI step and filter state are not.
 */
object JournalFormat {
    private const val VERSION = 1
    const val MAX_ENTRIES = 3000
    const val MAX_PLAINTEXT_BYTES = 3_000_000

    fun newEntry(draft: CheckInDraft, now: Long): JournalEntry =
        JournalEntry(UUID.randomUUID().toString(), now, now, draft)

    fun encode(entries: List<JournalEntry>): ByteArray {
        require(entries.size <= MAX_ENTRIES)
        require(entries.map { it.id }.distinct().size == entries.size)
        val root = buildJsonObject {
            put("version", JsonPrimitive(VERSION))
            put("entries", buildJsonArray {
                entries.forEach { entry ->
                    add(buildJsonObject {
                        put("id", JsonPrimitive(entry.id))
                        put("createdAt", JsonPrimitive(entry.createdAt))
                        put("updatedAt", JsonPrimitive(entry.updatedAt))
                        put("mode", JsonPrimitive(entry.draft.mode.name))
                        put("bodyCues", JsonArray(entry.draft.bodyCues.map { JsonPrimitive(it.name) }))
                        put("emotions", buildJsonObject {
                            entry.draft.emotions.forEach { (id, intensity) ->
                                put(id, intensity?.let(::JsonPrimitive) ?: JsonNull)
                            }
                        })
                        put("area", entry.draft.area?.let { JsonPrimitive(it.name) } ?: JsonNull)
                        put("note", JsonPrimitive(entry.draft.note))
                        put("situation", JsonPrimitive(entry.draft.situation))
                        put("interpretation", JsonPrimitive(entry.draft.interpretation))
                        put("needs", JsonArray(entry.draft.needs.map(::JsonPrimitive)))
                        put("insight", JsonPrimitive(entry.draft.insight))
                        put("nextStepIdea", entry.draft.nextStepIdea?.let { JsonPrimitive(it.name) } ?: JsonNull)
                        put("nextStepNote", JsonPrimitive(entry.draft.nextStepNote))
                    })
                }
            })
        }
        return root.toString().toByteArray(Charsets.UTF_8).also {
            require(it.size <= MAX_PLAINTEXT_BYTES) { "Journal capacity reached" }
        }
    }

    fun decode(bytes: ByteArray): List<JournalEntry> {
        require(bytes.size <= MAX_PLAINTEXT_BYTES) { "Journal too large" }
        val root = Json.parseToJsonElement(bytes.toString(Charsets.UTF_8)).jsonObject
        require(root.getValue("version").jsonPrimitive.intOrNull == VERSION) {
            "Unsupported journal format"
        }
        val raw = root.getValue("entries").jsonArray
        require(raw.size <= MAX_ENTRIES)
        val entries = raw.map { item ->
            val x = item.jsonObject
            fun str(key: String): String = x.getValue(key).jsonPrimitive.content
            fun optional(key: String): String? = x.getValue(key).jsonPrimitive.contentOrNull
            fun long(key: String): Long = str(key).toLong()
            val emotions = x.getValue("emotions").jsonObject.mapValues { (_, value) ->
                if (value is JsonNull) null else value.jsonPrimitive.intOrNull
                    ?: throw IllegalArgumentException("Bad intensity")
            }
            require(emotions.values.all { it == null || it in 0..10 })
            val draft = CheckInDraft(
                mode = CheckInMode.valueOf(str("mode")),
                bodyCues = x.getValue("bodyCues").jsonArray.map {
                    BodyCue.valueOf(it.jsonPrimitive.content)
                }.toSet(),
                emotions = emotions,
                area = optional("area")?.let(LifeArea::valueOf),
                note = str("note").take(CheckInDraft.MAX_NOTE_LENGTH),
                situation = str("situation").take(CheckInDraft.MAX_CONTEXT_LENGTH),
                interpretation = str("interpretation").take(CheckInDraft.MAX_CONTEXT_LENGTH),
                needs = x.getValue("needs").jsonArray.map { it.jsonPrimitive.content }.toSet(),
                insight = str("insight").take(CheckInDraft.MAX_INSIGHT_LENGTH),
                nextStepIdea = optional("nextStepIdea")?.let(NextStepIdea::valueOf),
                nextStepNote = str("nextStepNote").take(CheckInDraft.MAX_CONTEXT_LENGTH)
            )
            JournalEntry(str("id"), long("createdAt"), long("updatedAt"), draft)
        }
        require(entries.map { it.id }.distinct().size == entries.size)
        require(entries.all { it.id.isNotBlank() && it.createdAt > 0 && it.updatedAt >= it.createdAt })
        return entries
    }
}
