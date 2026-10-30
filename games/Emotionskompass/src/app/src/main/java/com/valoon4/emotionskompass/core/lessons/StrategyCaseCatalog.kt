package com.valoon4.emotionskompass.core.lessons

import java.text.Normalizer
import java.util.Locale
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.int

/** A self-contained, voluntary scenario. Choices compare contextual fit, not people. */
data class StrategyCase(
    val id: String,
    val theme: String,
    val scenario: String,
    val prompt: String,
    val choices: List<String>,
    val bestIndex: Int,
    val feedback: String,
    val reflection: String,
    val relatedStrategyIds: List<String>,
    val sources: List<String>
)

/** Never stores a personal response or contacts a server. */
class StrategyCaseCatalog private constructor(val cases: List<StrategyCase>) {
    fun find(id: String): StrategyCase? = cases.firstOrNull { it.id == id }
    val themes: List<String> get() = cases.map { it.theme }.distinct()

    fun browse(query: String = "", theme: String? = null): List<StrategyCase> {
        val q = normalize(query.trim())
        return cases.filter { item ->
            (theme == null || item.theme == theme) &&
                (q.isEmpty() || normalize(listOf(item.theme, item.scenario, item.prompt,
                    item.reflection).joinToString(" ")).contains(q))
        }
    }

    companion object {
        private val validId = Regex("^[a-z0-9-]{3,70}$")

        private fun normalize(value: String): String =
            Normalizer.normalize(
                value.lowercase(Locale.GERMAN).replace("ß", "ss"),
                Normalizer.Form.NFD
            ).replace(Regex("\\p{M}+"), "")

        /** All scenario shards are validated together: duplicate IDs and missing
         * strategy references fail closed, leaving the knowledge articles usable. */
        fun fromJsonParts(rawParts: List<String>, strategyIds: Set<String>): StrategyCaseCatalog {
            require(rawParts.isNotEmpty() && rawParts.size <= 10)
            require(strategyIds.isNotEmpty())
            val all = rawParts.flatMap { raw ->
                val obj = Json.parseToJsonElement(raw).jsonObject
                require(obj.keys == setOf("schemaVersion", "locale", "cases"))
                require(obj.getValue("schemaVersion").jsonPrimitive.int == 1)
                require(obj.getValue("locale").jsonPrimitive.content == "de")
                obj.getValue("cases").jsonArray.map { element ->
                    val c = element.jsonObject
                    fun value(name: String) = c.getValue(name).jsonPrimitive.content
                    fun values(name: String) = c.getValue(name).jsonArray.map {
                        it.jsonPrimitive.content
                    }
                    StrategyCase(
                        id = value("id"),
                        theme = value("theme"),
                        scenario = value("scenario"),
                        prompt = value("prompt"),
                        choices = values("choices"),
                        bestIndex = c.getValue("bestIndex").jsonPrimitive.int,
                        feedback = value("feedback"),
                        reflection = value("reflection"),
                        relatedStrategyIds = values("relatedStrategyIds"),
                        sources = values("sources")
                    )
                }
            }
            require(all.size in 1..100)
            require(all.map { it.id }.distinct().size == all.size)
            require(all.all { c ->
                validId.matches(c.id) && c.theme.length >= 5 &&
                    c.scenario.length >= 90 && c.prompt.length >= 35 &&
                    c.choices.size == 2 && c.choices.distinct().size == 2 &&
                    c.choices.all { it.length >= 60 } &&
                    c.bestIndex in 0..1 && c.feedback.length >= 100 &&
                    c.reflection.length >= 55 &&
                    c.relatedStrategyIds.isNotEmpty() &&
                    c.relatedStrategyIds.distinct().size == c.relatedStrategyIds.size &&
                    c.relatedStrategyIds.all { it in strategyIds } &&
                    c.sources.isNotEmpty() && c.sources.all { it.startsWith("https://") }
            })
            return StrategyCaseCatalog(all)
        }
    }
}
