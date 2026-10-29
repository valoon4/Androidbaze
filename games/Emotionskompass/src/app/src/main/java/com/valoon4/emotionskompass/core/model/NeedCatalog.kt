package com.valoon4.emotionskompass.core.model

import java.text.Normalizer
import java.util.Locale
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Editorial learning words, not a diagnostic or universal psychological taxonomy. */
data class HumanNeed(
    val id: String,
    val name: String,
    val family: String,
    val families: List<String>,
    val definition: String,
    val fulfilledExample: String,
    val unmetExample: String,
    val synonyms: List<String>,
    val related: List<String>,
    val reflectionQuestion: String,
    val strategyExample: String
)

data class NeedComparison(
    val first: String,
    val second: String,
    val overlap: String,
    val firstDistinction: String,
    val secondDistinction: String,
    val question: String
)

data class NeedLearningPath(
    val id: String,
    val title: String,
    val description: String,
    val needIds: List<String>
)

/** User-selected exploratory lens. No value is derived from emotions or stored. */
enum class NeedLens(val label: String) {
    WICHTIG("Gerade wichtig"),
    ERFUELLT("Gut erfüllt"),
    UNERFUELLT("Kommt zu kurz"),
    UNKLAR("Noch unklar")
}

class NeedCatalog private constructor(
    val contentRevision: String,
    val editorialStatus: String,
    val familyNames: List<String>,
    val needs: List<HumanNeed>,
    val comparisons: List<NeedComparison>,
    val learningPaths: List<NeedLearningPath>
) {
    val families: List<String> = listOf("Alle") + familyNames
    private val lookup = needs.associateBy { it.id }

    fun find(id: String): HumanNeed? = lookup[id]
    fun path(id: String): NeedLearningPath? = learningPaths.firstOrNull { it.id == id }
    fun itemsInPath(path: NeedLearningPath): List<HumanNeed> = path.needIds.mapNotNull(::find)

    fun browse(query: String, family: String = "Alle", letter: String = "Alle"): List<HumanNeed> {
        if (family != "Alle" && family !in familyNames) return emptyList()
        val options = needs.filter { family == "Alle" || family in it.families }
            .filter { letter == "Alle" || normalized(it.name).startsWith(normalized(letter)) }
        val q = normalized(query)
        if (q.isBlank()) return options.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
        return options.mapNotNull { need ->
            val name = normalized(need.name)
            val aliases = need.synonyms.map(::normalized)
            val score = when {
                name == q -> 0
                name.startsWith(q) -> 1
                aliases.any { it == q } -> 2
                name.contains(q) -> 3
                aliases.any { it.contains(q) } -> 4
                normalized(need.definition).contains(q) -> 5
                normalized(need.fulfilledExample).contains(q) ||
                    normalized(need.unmetExample).contains(q) -> 6
                else -> null
            }
            score?.let { it to need }
        }.sortedWith(compareBy<Pair<Int, HumanNeed>> { it.first }
            .thenBy(String.CASE_INSENSITIVE_ORDER) { it.second.name })
            .map { it.second }
    }

    fun compare(first: String, second: String): NeedComparison? {
        val item = comparisons.firstOrNull {
            (it.first == first && it.second == second) ||
                (it.first == second && it.second == first)
        } ?: return null
        return if (first == item.first) item else item.copy(
            first = item.second, second = item.first,
            firstDistinction = item.secondDistinction,
            secondDistinction = item.firstDistinction
        )
    }

    fun comparisonOptions(id: String): List<NeedComparison> =
        comparisons.filter { it.first == id || it.second == id }

    companion object {
        private val marks = Regex("\\p{M}+")
        private fun normalized(text: String): String {
            val lower = text.trim().lowercase(Locale.ROOT)
                .replace("ä", "a").replace("ö", "o").replace("ü", "u")
                .replace("ß", "ss").replace("ae", "a").replace("oe", "o").replace("ue", "u")
            return Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replace(marks, "").replace(Regex("\\s+"), " ")
        }

        fun fromJson(source: String): NeedCatalog {
            val data = Json.parseToJsonElement(source).jsonObject
            fun JsonObject.required(key: String): String =
                getValue(key).jsonPrimitive.content.also { require(it.isNotBlank()) }
            fun JsonObject.strings(key: String): List<String> =
                getValue(key).jsonArray.map { it.jsonPrimitive.content }

            require(data.required("schemaVersion") == "1")
            require(data.required("locale") == "de")
            val families = data.strings("familyNames")
            require(families.size == 8 && families.distinct().size == families.size)
            val words = data.getValue("needs").jsonArray.map {
                val e = it.jsonObject
                HumanNeed(
                    id = e.required("id"), name = e.required("name"),
                    family = e.required("family"), families = e.strings("families"),
                    definition = e.required("definition"),
                    fulfilledExample = e.required("fulfilledExample"),
                    unmetExample = e.required("unmetExample"),
                    synonyms = e.strings("synonyms"), related = e.strings("related"),
                    reflectionQuestion = e.required("reflectionQuestion"),
                    strategyExample = e.required("strategyExample")
                )
            }
            require(words.size >= 75) { "M3.3 requires a broad needs vocabulary" }
            require(words.map { it.id }.distinct().size == words.size)
            val ids = words.map { it.id }.toSet()
            for (word in words) {
                require(word.family in families && word.family in word.families)
                require(word.families.distinct().size == word.families.size)
                require(word.families.all { it in families })
                require(word.synonyms.isNotEmpty() && word.synonyms.all { it.isNotBlank() })
                require(word.related.isNotEmpty() && word.related.distinct().size == word.related.size)
                require(word.related.all { it in ids && it != word.id }) { "Invalid related ID in ${word.id}" }
                require(word.fulfilledExample != word.unmetExample)
            }
            val comparisons = data.getValue("comparisons").jsonArray.map {
                val x = it.jsonObject
                NeedComparison(x.required("first"), x.required("second"), x.required("overlap"),
                    x.required("firstDistinction"), x.required("secondDistinction"),
                    x.required("question"))
            }
            require(comparisons.size >= 16)
            require(comparisons.all { it.first != it.second && it.first in ids && it.second in ids })
            require(comparisons.map { setOf(it.first, it.second) }.distinct().size == comparisons.size)
            val paths = data.getValue("learningPaths").jsonArray.map {
                val x = it.jsonObject
                NeedLearningPath(x.required("id"), x.required("title"),
                    x.required("description"), x.strings("needIds"))
            }
            require(paths.size >= 6 && paths.map { it.id }.distinct().size == paths.size)
            require(paths.all {
                it.needIds.size >= 4 && it.needIds.distinct().size == it.needIds.size &&
                    it.needIds.all(ids::contains)
            })
            return NeedCatalog(data.required("contentRevision"), data.required("editorialStatus"),
                families, words, comparisons, paths)
        }
    }
}
