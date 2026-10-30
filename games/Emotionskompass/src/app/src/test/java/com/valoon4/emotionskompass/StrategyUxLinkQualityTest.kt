package com.valoon4.emotionskompass

import com.valoon4.emotionskompass.core.lessons.LessonCatalog
import com.valoon4.emotionskompass.core.lessons.StrategyCaseCatalog
import com.valoon4.emotionskompass.core.model.NeedCatalog
import com.valoon4.emotionskompass.core.strategies.StrategyCatalog
import com.valoon4.emotionskompass.core.strategies.StrategyLearningIndex
import com.valoon4.emotionskompass.core.strategies.NeedStrategyLinkIndex
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
    private val linkShards by lazy {
        listOf("security", "relationships", "autonomy", "respect",
            "wellbeing", "growth", "purpose", "vitality").map {
            asset("catalog/need-strategy-$it.de.v1.json")
        }
    }
    private val needLinks by lazy {
        NeedStrategyLinkIndex.fromJsonParts(linkShards, needs, strategies)
    }
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

    @Test fun seventySixNeedsHaveExplicitContextualLinksWithoutForcingSixOthers() {
        assertEquals(82, needs.needs.size)
        assertEquals(76, needLinks.linkedNeedIds.size)
        assertEquals(143, needLinks.associations.size)
        val deliberatelyUnmapped = needs.needs.map { it.id }.toSet() - needLinks.linkedNeedIds
        assertEquals(setOf("schlaf", "gesundheit", "naehrung",
            "spiritualitaet", "humor", "spontanitaet"), deliberatelyUnmapped)
        needs.needs.forEach { need ->
            val options = needLinks.optionsForNeed(need.id, strategies)
            assertTrue("Too many suggestions: ${need.id}", options.size <= 3)
            assertEquals(options.size, options.map { it.strategy.id }.toSet().size)
            assertTrue(options.all { it.reason.length in 55..300 })
            assertTrue("Pattern accidentally presented for ${need.id}",
                options.none { "patterns" in it.strategy.familyIds })
            options.forEach { option ->
                assertTrue(needLinks.needsForStrategy(option.strategy.id, needs)
                    .any { it.need.id == need.id && it.reason == option.reason })
            }
        }
        assertTrue(needLinks.optionsForNeed("sicherheit", strategies)
            .any { it.strategy.id == "social-support" })
        assertTrue(needLinks.optionsForNeed("privatsphaere", strategies)
            .any { it.strategy.id == "boundaries" })
        assertTrue(needLinks.optionsForNeed("bewegung", strategies)
            .any { it.strategy.id == "movement" })
        // The *need* for acceptance by others must not be equated with
        // inner emotional acceptance as a regulation technique.
        assertTrue(needLinks.optionsForNeed("akzeptanz", strategies)
            .none { it.strategy.id == "acceptance" })
    }

    @Test fun brokenNeedLinksAndDuplicatedEditorialDefinitionsFailClosed() {
        assertThrows(IllegalArgumentException::class.java) {
            NeedStrategyLinkIndex.fromJsonParts(linkShards + linkShards.first(), needs, strategies)
        }
        assertThrows(IllegalArgumentException::class.java) {
            NeedStrategyLinkIndex.fromJsonParts(linkShards.mapIndexed { i, raw ->
                if (i == 0) raw.replaceFirst(
                    Regex("\"schemaVersion\"\\s*:\\s*1"), "\"schemaVersion\":2"
                ) else raw
            }, needs, strategies)
        }
        assertThrows(IllegalArgumentException::class.java) {
            NeedStrategyLinkIndex.fromJsonParts(linkShards.mapIndexed { i, raw ->
                if (i == 0) raw.replaceFirst(
                    Regex("\"strategyId\"\\s*:\\s*\"[a-z-]+\""),
                    "\"strategyId\":\"prolonged-avoidance\""
                ) else raw
            }, needs, strategies)
        }
        assertThrows(IllegalArgumentException::class.java) {
            NeedStrategyLinkIndex.fromJsonParts(linkShards.mapIndexed { i, raw ->
                if (i == 0) raw.replaceFirst(
                    Regex("\"needId\"\\s*:\\s*\"[a-z_]+\""),
                    "\"needId\":\"unbekanntes-beduerfnis\""
                ) else raw
            }, needs, strategies)
        }
        // A malformed optional index must not invalidate the 30-card catalog.
        assertEquals(30, strategies.strategies.size)
        assertEquals(82, needs.needs.size)
    }

    @Test fun everyEditorialLinkHasContextAndNoPrescriptiveGuarantee() {
        // A wording screen, not proof of psychological or clinical effectiveness.
        val contextual = Regex("\\b(wenn|kann|könnte|bei|sofern|falls)\\b",
            RegexOption.IGNORE_CASE)
        val coercive = Regex(
            "\\b(du musst|du sollst unbedingt|garantiert wirksam|heilt|ersetzt eine therapie)\\b",
            RegexOption.IGNORE_CASE
        )
        needLinks.associations.forEach { link ->
            assertTrue("Missing context: ${link.needId} -> ${link.strategyId}",
                contextual.containsMatchIn(link.reason))
            assertFalse("Prescriptive claim: ${link.needId} -> ${link.strategyId}",
                coercive.containsMatchIn(link.reason))
            assertTrue("Inadequate nuance: ${link.needId} -> ${link.strategyId}",
                link.reason.length >= 55)
        }
        // Social acceptance and inner emotional acceptance are conceptually different.
        val socialAcceptance = needLinks.optionsForNeed("akzeptanz", strategies)
        assertTrue(socialAcceptance.none { it.strategy.id == "acceptance" })
        val selfCompassion = socialAcceptance.single { it.strategy.id == "self-compassion" }
        assertTrue(selfCompassion.reason.contains("ersetzt aber nicht"))
        // Sleep, nutrition and health should not be replaced by generic coping techniques.
        listOf("schlaf", "naehrung", "gesundheit").forEach {
            assertTrue("Inappropriate direct care substitution: $it",
                needLinks.optionsForNeed(it, strategies).isEmpty())
        }
        // No direct confrontation is required to seek dignity or protection.
        listOf("schutz", "wuerde", "gleichbehandlung").forEach { id ->
            val options = needLinks.optionsForNeed(id, strategies)
            assertTrue(options.any { it.strategy.id == "social-support" })
            assertTrue(options.none { "patterns" in it.strategy.familyIds })
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
