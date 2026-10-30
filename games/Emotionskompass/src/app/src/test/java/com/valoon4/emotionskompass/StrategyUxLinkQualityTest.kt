package com.valoon4.emotionskompass

import com.valoon4.emotionskompass.core.lessons.LessonCatalog
import com.valoon4.emotionskompass.core.lessons.StrategyCaseCatalog
import com.valoon4.emotionskompass.core.model.NeedCatalog
import com.valoon4.emotionskompass.core.strategies.StrategyCatalog
import com.valoon4.emotionskompass.core.strategies.StrategyLearningIndex
import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** M6.3.3 contract: only explicit, verifiable educational connections. */
class StrategyUxLinkQualityTest {
    private fun asset(path: String) = File("src/main/assets/$path").readText()
    private val strategies by lazy {
        StrategyCatalog.fromJsonParts(
            asset("catalog/strategies.de.v1.json"),
            listOf("situation", "regulation-a", "regulation-b", "cognition-a",
                "cognition-b", "social", "values", "recovery", "patterns")
                .map { asset("catalog/strategies.$it.de.v1.json") }
        )
    }
    private val lessons by lazy {
        LessonCatalog.fromJsonParts(
            asset("lessons/knowledge.de.v1.json"),
            listOf("basics", "coping", "flex").map {
                asset("lessons/strategy-depth-$it.de.v1.json")
            }
        )
    }
    private val needs by lazy { NeedCatalog.fromJson(asset("catalog/needs.de.v1.json")) }
    private val cases by lazy {
        StrategyCaseCatalog.fromJsonParts(
            listOf("a", "b", "c").map { asset("lessons/strategy-cases-$it.de.v1.json") },
            strategies.strategies.map { it.id }.toSet()
        )
    }

    @Test fun allThirtyStrategiesHaveLiveReadableArticleLinks() {
        assertEquals(30, strategies.strategies.size)
        assertEquals(30, StrategyLearningIndex.deeperArticleIdByStrategy.size)
        assertEquals(strategies.strategies.map { it.id }.toSet(),
            StrategyLearningIndex.deeperArticleIdByStrategy.keys)
        val newArticleIds = lessons.series.filter { it.id.startsWith("strategiewelt-") }
            .flatMap { it.lessonIds }.toSet()
        assertEquals(12, newArticleIds.size)
        assertEquals(newArticleIds, StrategyLearningIndex.deeperArticleIdByStrategy.values.toSet())
        strategies.strategies.forEach { card ->
            val links = StrategyLearningIndex.articlesFor(card, lessons)
            assertTrue("No article: ${card.id}", links.isNotEmpty())
            assertTrue("Too many: ${card.id}", links.size <= 3)
            assertEquals(links.size, links.map { it.id }.toSet().size)
            assertTrue(links.all { it.title.length > 10 })
            val expected = StrategyLearningIndex.deeperArticleIdByStrategy.getValue(card.id)
            assertTrue("${card.id} doesn't link to its depth article",
                links.any { it.id == expected })
        }
    }

    @Test fun needsOnlyHaveExplicitNonDiagnosticStrategyExamples() {
        assertEquals(82, needs.needs.size)
        val withLinks = needs.needs.filter {
            StrategyLearningIndex.strategiesForNeed(it.id, strategies).isNotEmpty()
        }
        assertTrue(withLinks.isNotEmpty())
        assertTrue(withLinks.size < needs.needs.size) // Honest empty-link states exist.
        needs.needs.forEach { need ->
            val links = StrategyLearningIndex.strategiesForNeed(need.id, strategies)
            assertTrue(links.size <= 3)
            assertTrue(links.all { need.id in it.relatedNeedIds })
            assertTrue("Harmful pattern suggested for ${need.id}",
                links.none { "patterns" in it.familyIds })
        }
    }

    @Test fun allFifteenCasesOpenNamedStrategyDetails() {
        assertEquals(15, cases.cases.size)
        val foundIds = cases.cases.flatMap { it.relatedStrategyIds }.toSet()
        assertTrue(foundIds.size >= 10)
        cases.cases.forEach { c ->
            assertTrue(c.relatedStrategyIds.all { strategies.find(it) != null })
            c.relatedStrategyIds.forEach { strategyId ->
                assertTrue("Case link missing: ${c.id} -> $strategyId",
                    cases.forStrategy(strategyId).any { it.id == c.id })
            }
        }
    }

    @Test fun allThreeFullTextSearchesIncludeExplanationsAndReflection() {
        strategies.strategies.forEach { card ->
            assertTrue("Strategy reflection not searchable: ${card.id}",
                strategies.browse(card.reflection.take(22)).contains(card))
        }
        lessons.lessons.forEach { article ->
            assertTrue("Article reflection not searchable: ${article.id}",
                lessons.browse(article.reflection.take(22)).contains(article))
        }
        cases.cases.forEach { example ->
            assertTrue("Feedback not searchable: ${example.id}",
                cases.browse(example.feedback.take(22)).contains(example))
            assertTrue("Options not searchable: ${example.id}",
                cases.browse(example.choices[0].take(22)).contains(example))
        }
    }

    @Test fun glossaryRemainsReachableAndLearningCatalogPreservesOldIds() {
        assertNotNull(lessons.find("s-vertiefung-begriffe"))
        val old = LessonCatalog.fromJson(asset("lessons/knowledge.de.v1.json"))
        old.lessons.forEach { assertEquals(it, lessons.find(it.id)) }
        assertNotNull(strategies.find("prolonged-avoidance"))
        assertNotNull(strategies.find("emotional-suppression"))
    }
}
