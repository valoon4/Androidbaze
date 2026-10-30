package com.valoon4.emotionskompass

import com.valoon4.emotionskompass.core.lessons.LessonCatalog
import com.valoon4.emotionskompass.core.model.NeedCatalog
import com.valoon4.emotionskompass.core.strategies.StrategyCatalog
import java.io.File
import org.junit.Assert.*
import org.junit.Test

class StrategyCatalogTest {
    private val raw by lazy {
        File("src/main/assets/catalog/strategies.de.v1.json").readText()
    }
    private val parts by lazy {
        listOf(
            "situation", "regulation-a", "regulation-b",
            "cognition-a", "cognition-b", "social",
            "values", "recovery", "patterns"
        ).map { group ->
            File("src/main/assets/catalog/strategies.${group}.de.v1.json").readText()
        }
    }
    private val catalog by lazy { StrategyCatalog.fromJsonParts(raw, parts) }

    @Test fun thirtyCompleteIndependentStrategyCards() {
        assertEquals(30, catalog.strategies.size)
        assertEquals(7, catalog.families.size)
        assertTrue(catalog.strategies.map { it.id }.toSet().containsAll(setOf(
            "problem-solving", "intentional-distraction", "reappraisal",
            "social-support", "acceptance"
        )))
        assertEquals(30, catalog.strategies.map { it.id }.distinct().size)
        assertTrue(catalog.editorialStatus.contains("review-required"))
        assertTrue(catalog.strategies.all { it.benefits.size >= 2 && it.drawbacks.size >= 2 })
        assertTrue(catalog.strategies.all { it.example.length >= 100 &&
            it.misconception.isNotBlank() && it.reflection.isNotBlank() })
    }

    @Test fun allSevenFamiliesHaveBrowsableCards() {
        catalog.families.forEach { family ->
            assertEquals(catalog.strategies.count { family.id in it.familyIds },
                catalog.browse(familyId = family.id).size)
        }
        assertEquals(2, catalog.browse(familyId = "patterns").size)
        assertEquals(30, catalog.browse().size)
    }

    @Test fun searchHandlesUmlautsAndSynonymsAndCombinedFilter() {
        assertEquals(catalog.browse("Ablenkung").map { it.id },
            catalog.browse("ablenkung").map { it.id })
        assertEquals(catalog.browse("Lösen").map { it.id },
            catalog.browse("losen").map { it.id })
        assertEquals(listOf("reappraisal"),
            catalog.browse("neubewertung").map { it.id })
        assertTrue(catalog.browse("neubewertung", "social").isEmpty())
        assertEquals(listOf("reappraisal"),
            catalog.browse("neubewertung", "cognition").map { it.id })
    }

    @Test fun alternativeLinksResolveWithoutSelfLinks() {
        catalog.strategies.forEach { card ->
            assertTrue(card.alternativeIds.none { it == card.id })
            assertEquals(card.alternativeIds,
                catalog.alternatives(card).map { it.id })
            assertTrue(card.sources.all { it.url.startsWith("https://") })
        }
        assertNull(catalog.find("not-a-real-strategy"))
    }

    @Test fun editorialLinksPointOnlyToExistingOfflineNeedsAndLessons() {
        val lessons = LessonCatalog.fromJson(
            File("src/main/assets/lessons/knowledge.de.v1.json").readText()
        ).lessons.map { it.id }.toSet()
        val needs = NeedCatalog.fromJson(
            File("src/main/assets/catalog/needs.de.v1.json").readText()
        ).needs.map { it.id }.toSet()
        catalog.strategies.forEach { card ->
            assertTrue("Invalid lesson link in ${card.id}",
                card.relatedLessonIds.all { it in lessons })
            assertTrue("Invalid need link in ${card.id}",
                card.relatedNeedIds.all { it in needs })
        }
    }

    @Test fun invalidVersionDuplicateIdAndBrokenAlternativeAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            StrategyCatalog.fromJson(raw.replaceFirst(
                "\"schemaVersion\": 1", "\"schemaVersion\": 7"
            ))
        }
        assertThrows(IllegalArgumentException::class.java) {
            StrategyCatalog.fromJson(raw.replaceFirst(
                "\"id\": \"intentional-distraction\"",
                "\"id\": \"problem-solving\""
            ))
        }
        assertThrows(IllegalArgumentException::class.java) {
            StrategyCatalog.fromJson(raw.replaceFirst(
                "\"alternativeIds\": [\n        \"social-support\"",
                "\"alternativeIds\": [\n        \"broken-link\""
            ))
        }
    }

    @Test fun fragmentLoadingIsStrictAndFailsClosedOnDuplicateOrWrongVersion() {
        assertEquals(5, StrategyCatalog.fromJsonParts(raw, emptyList()).strategies.size)
        assertThrows(IllegalArgumentException::class.java) {
            StrategyCatalog.fromJsonParts(raw, parts + parts.first())
        }
        assertThrows(IllegalArgumentException::class.java) {
            StrategyCatalog.fromJsonParts(
                raw, parts.mapIndexed { index, value ->
                    if (index == 0) value.replaceFirst(
                        "\"schemaVersion\":1", "\"schemaVersion\":2"
                    ) else value
                }
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            StrategyCatalog.fromJsonParts(
                raw, parts.mapIndexed { index, value ->
                    if (index == 0) value.replaceFirst(
                        "\"problem-solving\"", "\"broken-ref\""
                    ) else value
                }
            )
        }
    }

    @Test fun oldReferenceCardsRemainStableWithinExpandedCollection() {
        val old = StrategyCatalog.fromJson(raw)
        old.strategies.forEach { before ->
            val now = catalog.find(before.id)
            assertNotNull(now)
            assertEquals(before.title, now!!.title)
            assertEquals(before.alternativeIds, now.alternativeIds)
        }
    }

    @Test fun malformedContentOrEditorialGapsCannotLoad() {
        assertThrows(IllegalArgumentException::class.java) {
            StrategyCatalog.fromJson(raw.replaceFirst(
                "\"editorialStatus\": \"educational-draft-independent-expert-review-required\"",
                "\"editorialStatus\": \"\""
            ))
        }
        assertThrows(IllegalArgumentException::class.java) {
            StrategyCatalog.fromJson(raw.replaceFirst(
                "\"id\": \"situation\"",
                "\"id\": \"invalid/id\""
            ))
        }
    }
}
