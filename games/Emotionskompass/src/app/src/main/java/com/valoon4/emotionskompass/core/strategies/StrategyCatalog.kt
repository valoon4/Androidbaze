package com.valoon4.emotionskompass.core.strategies

import java.text.Normalizer
import java.util.Locale
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.int

data class StrategyFamily(val id: String, val title: String, val description: String)

data class StrategySource(
    val url: String,
    val description: String,
    val evidenceType: String
)

data class StrategyCard(
    val id: String,
    val title: String,
    val aliases: List<String>,
    val familyIds: List<String>,
    val tags: List<String>,
    val shortDefinition: String,
    val purpose: String,
    val whenUseful: String,
    val benefits: List<String>,
    val drawbacks: List<String>,
    val limitations: String,
    val example: String,
    val misconception: String,
    val clarification: String,
    val reflection: String,
    val optionalPractice: String,
    val alternativeIds: List<String>,
    val relatedNeedIds: List<String>,
    val relatedLessonIds: List<String>,
    val sources: List<StrategySource>
) {
    val searchText: String get() = (
        listOf(title, shortDefinition, purpose, whenUseful, limitations,
            example, misconception, clarification, reflection, optionalPractice) +
            aliases + benefits + drawbacks + sources.map { it.description }
        ).joinToString(" ")
}

/** Editorial, bundled, immutable content. No personal input and no network calls. */
class StrategyCatalog private constructor(
    val revision: String,
    val editorialStatus: String,
    val intro: String,
    val families: List<StrategyFamily>,
    val strategies: List<StrategyCard>
) {
    private val byId = strategies.associateBy { it.id }

    fun find(id: String): StrategyCard? = byId[id]

    fun browse(query: String = "", familyId: String? = null): List<StrategyCard> {
        val normalizedQuery = normalized(query.trim())
        return strategies.filter { card ->
            (familyId == null || familyId in card.familyIds) &&
                (normalizedQuery.isEmpty() || normalized(card.searchText).contains(normalizedQuery))
        }
    }

    fun alternatives(card: StrategyCard): List<StrategyCard> =
        card.alternativeIds.mapNotNull(byId::get)

    fun familyName(id: String): String =
        families.firstOrNull { it.id == id }?.title ?: "Weitere Strategie"

    companion object {
        private val validId = Regex("^[a-z0-9-]{3,70}$")
        private val allowedTags = setOf("coping", "emotion-regulation", "needs-action", "communication")
        private val allowedEvidence = setOf(
            "THEORY_REVIEW", "PRACTICE_GUIDE", "PRIMARY_STUDY",
            "OBSERVATIONAL_STUDY", "SYSTEMATIC_REVIEW"
        )

        private fun normalized(input: String): String =
            Normalizer.normalize(
                input.lowercase(Locale.GERMAN).replace("ß", "ss"),
                Normalizer.Form.NFD
            ).replace(Regex("\\p{M}+"), "")

        private fun JsonObject.value(key: String): String =
            getValue(key).jsonPrimitive.content

        private fun JsonObject.values(key: String): List<String> =
            getValue(key).jsonArray.map { it.jsonPrimitive.content }

        /** Assemble versioned editorial shards before applying the one strict catalog validator.
         * All links, IDs and seven families are verified across the complete data set. */
        fun fromJsonParts(base: String, parts: List<String>): StrategyCatalog {
            if (parts.isEmpty()) return fromJson(base)
            val root = Json.parseToJsonElement(base).jsonObject
            val extra = parts.flatMap { part ->
                val obj = Json.parseToJsonElement(part).jsonObject
                require(obj.keys == setOf("schemaVersion", "locale", "strategies"))
                require(obj.getValue("schemaVersion").jsonPrimitive.int == 1)
                require(obj.value("locale") == "de")
                obj.getValue("strategies").jsonArray.toList()
            }
            val all = root.getValue("strategies").jsonArray.toList() + extra
            val joined = buildJsonObject {
                root.forEach { (key, value) ->
                    put(key, when(key) {
                        "strategies" -> JsonArray(all)
                        "contentRevision" -> kotlinx.serialization.json.JsonPrimitive("2026-10-m6.3.1-full-30")
                        else -> value
                    })
                }
            }
            return fromJson(joined.toString())
        }

        fun fromJson(raw: String): StrategyCatalog {
            val root = Json.parseToJsonElement(raw).jsonObject
            require(root.getValue("schemaVersion").jsonPrimitive.int == 1)
            require(root.value("locale") == "de")
            require(root.value("contentRevision").isNotBlank())
            require(root.value("editorialStatus").isNotBlank())
            val intro = root.value("intro")
            require(intro.length >= 100)
            val families = root.getValue("families").jsonArray.map { element ->
                val obj = element.jsonObject
                StrategyFamily(obj.value("id"), obj.value("title"), obj.value("description"))
            }
            require(families.size == 7 && families.map { it.id }.distinct().size == families.size)
            require(families.all { validId.matches(it.id) && it.title.isNotBlank() &&
                it.description.length >= 30 })
            val idsOfFamilies = families.map { it.id }.toSet()
            val cards = root.getValue("strategies").jsonArray.map { element ->
                val obj = element.jsonObject
                StrategyCard(
                    id = obj.value("id"),
                    title = obj.value("title"),
                    aliases = obj.values("aliases"),
                    familyIds = obj.values("familyIds"),
                    tags = obj.values("tags"),
                    shortDefinition = obj.value("shortDefinition"),
                    purpose = obj.value("purpose"),
                    whenUseful = obj.value("whenUseful"),
                    benefits = obj.values("benefits"),
                    drawbacks = obj.values("drawbacks"),
                    limitations = obj.value("limitations"),
                    example = obj.value("example"),
                    misconception = obj.value("misconception"),
                    clarification = obj.value("clarification"),
                    reflection = obj.value("reflection"),
                    optionalPractice = obj.value("optionalPractice"),
                    alternativeIds = obj.values("alternativeIds"),
                    relatedNeedIds = obj.values("relatedNeedIds"),
                    relatedLessonIds = obj.values("relatedLessonIds"),
                    sources = obj.getValue("sources").jsonArray.map { source ->
                        val s = source.jsonObject
                        StrategySource(s.value("url"), s.value("description"), s.value("evidenceType"))
                    }
                )
            }
            require(cards.isNotEmpty() && cards.size <= 100)
            val ids = cards.map { it.id }.toSet()
            require(ids.size == cards.size)
            require(cards.all { card ->
                validId.matches(card.id) && card.title.length >= 5 &&
                    card.shortDefinition.length >= 70 && card.purpose.length >= 80 &&
                    card.whenUseful.length >= 80 && card.limitations.length >= 100 &&
                    card.example.length >= 100 && card.misconception.length >= 40 &&
                    card.clarification.length >= 70 && card.reflection.length >= 50 &&
                    card.familyIds.isNotEmpty() && card.familyIds.distinct() == card.familyIds &&
                    card.familyIds.all { it in idsOfFamilies } &&
                    card.tags.isNotEmpty() && card.tags.all { it in allowedTags } &&
                    card.benefits.size >= 2 && card.drawbacks.size >= 2 &&
                    (card.benefits + card.drawbacks).all { it.length >= 55 } &&
                    card.sources.isNotEmpty() && card.sources.all {
                        it.url.startsWith("https://") && it.description.length >= 30 &&
                            it.evidenceType in allowedEvidence
                    } &&
                    card.alternativeIds.isNotEmpty() && card.alternativeIds.distinct().size ==
                        card.alternativeIds.size &&
                    card.alternativeIds.all { it in ids && it != card.id } &&
                    card.relatedNeedIds.distinct().size == card.relatedNeedIds.size &&
                    card.relatedLessonIds.distinct().size == card.relatedLessonIds.size &&
                    card.relatedNeedIds.all(validId::matches) &&
                    card.relatedLessonIds.all(validId::matches)
            })
            return StrategyCatalog(
                revision = root.value("contentRevision"),
                editorialStatus = root.value("editorialStatus"),
                intro = intro,
                families = families,
                strategies = cards
            )
        }
    }
}
