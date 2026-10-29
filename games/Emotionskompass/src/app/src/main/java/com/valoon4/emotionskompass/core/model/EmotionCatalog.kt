package com.valoon4.emotionskompass.core.model

import java.text.Normalizer
import java.util.Locale
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Immutable content model for the built-in versioned German lexicon.
 * Read from assets/catalog/emotions.de.v2.json, with no internet or journal storage.
 */
data class Emotion(
    val id: String,
    val name: String,
    val family: String,
    val families: List<String>,
    val kind: EmotionKind,
    val explanation: String,
    val examples: List<String>,
    val synonyms: List<String>,
    val related: List<String>,
    val reflectionQuestion: String
) {
    val example: String get() = examples.first()
}

enum class EmotionKind { EMOTION, MOOD, STATE }

data class EmotionComparison(
    val first: String,
    val second: String,
    val overlap: String,
    val firstDistinction: String,
    val secondDistinction: String,
    val question: String
)

class EmotionCatalog private constructor(
    val schemaVersion: Int,
    val contentRevision: String,
    val editorialStatus: String,
    val familyNames: List<String>,
    val emotions: List<Emotion>,
    val comparisons: List<EmotionComparison>
) {
    val families: List<String> = listOf("Alle") + familyNames
    private val lookup = emotions.associateBy { it.id }

    fun find(id: String): Emotion? = lookup[id]

    /** Compares regardless of selection order, preserving correct contrast labels. */
    fun compare(first: String, second: String): EmotionComparison? {
        val item = comparisons.firstOrNull {
            (it.first == first && it.second == second) ||
                (it.first == second && it.second == first)
        } ?: return null
        return if (item.first == first) item else item.copy(
            first = item.second,
            second = item.first,
            firstDistinction = item.secondDistinction,
            secondDistinction = item.firstDistinction
        )
    }

    /** Curated comparisons whose other term is connected to this one. */
    fun comparisonOptions(id: String): List<String> =
        comparisons.mapNotNull {
            when (id) {
                it.first -> it.second
                it.second -> it.first
                else -> null
            }
        }.distinct()

    /**
     * Search is local, accent-/umlaut-/case-insensitive and prioritizes exact names,
     * then prefixes, aliases, descriptions and example usage.
     */
    fun search(query: String, family: String = "Alle"): List<Emotion> {
        if (family != "Alle" && family !in familyNames) return emptyList()
        val needle = normalize(query)
        val options = emotions.filter { family == "Alle" || family in it.families }
        if (needle.isBlank()) return options.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })

        return options.mapNotNull { emotion ->
            val name = normalize(emotion.name)
            val aliases = emotion.synonyms.map(::normalize)
            val definitions = normalize(emotion.explanation)
            val examples = emotion.examples.map(::normalize)
            val score = when {
                name == needle -> 0
                name.startsWith(needle) -> 1
                name.contains(needle) -> 2
                aliases.any { it == needle || it.startsWith(needle) } -> 3
                aliases.any { it.contains(needle) } -> 4
                definitions.contains(needle) -> 5
                examples.any { it.contains(needle) } -> 6
                else -> null
            }
            if (score == null) null else score to emotion
        }.sortedWith(compareBy<Pair<Int, Emotion>> { it.first }
            .thenBy(String.CASE_INSENSITIVE_ORDER) { it.second.name })
            .map { it.second }
    }

    companion object {
        private const val VERSION = 2
        private val marks = Regex("\\p{M}+")

        private fun normalize(input: String): String {
            val lower = input.trim().lowercase(Locale.ROOT)
                .replace("ä", "a").replace("ö", "o").replace("ü", "u").replace("ß", "ss")
                .replace("ae", "a").replace("oe", "o").replace("ue", "u")
            return Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replace(marks, "")
                .replace(Regex("\\s+"), " ")
        }

        fun fromJson(source: String): EmotionCatalog {
            val json = Json.parseToJsonElement(source).jsonObject
            fun JsonObject.required(key: String): String =
                getValue(key).jsonPrimitive.content.also { require(it.isNotBlank()) { "Empty field: $key" } }
            fun JsonObject.strings(key: String): List<String> =
                getValue(key).jsonArray.map { it.jsonPrimitive.content }

            val version = json.required("schemaVersion").toInt()
            require(version == VERSION) { "Unsupported lexicon schema version $version" }
            require(json.required("locale") == "de") { "Expected German catalog" }
            val familyNames = json.strings("families")
            require(familyNames.size == 8 && familyNames.toSet().size == familyNames.size)
            val emotions = json.getValue("emotions").jsonArray.map { element ->
                val e = element.jsonObject
                Emotion(
                    id = e.required("id"),
                    name = e.required("name"),
                    family = e.required("family"),
                    families = e.strings("families"),
                    kind = EmotionKind.valueOf(e.required("kind")),
                    explanation = e.required("definition"),
                    examples = e.strings("examples"),
                    synonyms = e.strings("synonyms"),
                    related = e.strings("related"),
                    reflectionQuestion = e.required("reflectionQuestion")
                )
            }
            require(emotions.size >= 24) { "M2 lexicon must have at least 24 entries" }
            require(emotions.map { it.id }.toSet().size == emotions.size) { "Duplicate emotion IDs" }
            val idSet = emotions.map { it.id }.toSet()
            for (emotion in emotions) {
                require(emotion.family in familyNames && emotion.family in emotion.families)
                require(emotion.families.isNotEmpty() && emotion.families.all { it in familyNames })
                require(emotion.examples.size >= 2 && emotion.examples.all { it.isNotBlank() })
                require(emotion.synonyms.isNotEmpty() && emotion.synonyms.all { it.isNotBlank() })
                require(emotion.related.distinct().size == emotion.related.size)
                require(emotion.related.all { it in idSet && it != emotion.id }) {
                    "Broken related emotion reference in ${emotion.id}"
                }
            }
            val comparisons = json.getValue("comparisons").jsonArray.map { element ->
                val c = element.jsonObject
                EmotionComparison(
                    first = c.required("first"),
                    second = c.required("second"),
                    overlap = c.required("overlap"),
                    firstDistinction = c.required("firstDistinction"),
                    secondDistinction = c.required("secondDistinction"),
                    question = c.required("question")
                )
            }
            require(comparisons.all { it.first in idSet && it.second in idSet && it.first != it.second })
            require(comparisons.map { setOf(it.first, it.second) }.distinct().size == comparisons.size)
            return EmotionCatalog(
                schemaVersion = version,
                contentRevision = json.required("contentRevision"),
                editorialStatus = json.required("editorialStatus"),
                familyNames = familyNames,
                emotions = emotions,
                comparisons = comparisons
            )
        }
    }
}
