package com.valoon4.emotionskompass

import com.valoon4.emotionskompass.core.lessons.LessonCatalog
import com.valoon4.emotionskompass.core.lessons.StrategyCaseCatalog
import com.valoon4.emotionskompass.core.learning.LearningProgressFormat
import com.valoon4.emotionskompass.core.learning.LearningState
import com.valoon4.emotionskompass.core.strategies.StrategyCatalog
import java.io.File
import org.junit.Assert.*
import org.junit.Test

class StrategyEducationTest {
    private fun bundled(path: String): String = File("src/main/assets/" + path).readText()
    private val oldLessons by lazy { bundled("lessons/knowledge.de.v1.json") }
    private val chapters by lazy { listOf("basics", "coping", "flex").map {
        bundled("lessons/strategy-depth-$it.de.v1.json")
    } }
    private val library by lazy { LessonCatalog.fromJsonParts(oldLessons, chapters) }
    private val strategyCatalog by lazy {
        StrategyCatalog.fromJsonParts(bundled("catalog/strategies.de.v1.json"),
            listOf("situation", "regulation-a", "regulation-b", "cognition-a",
                "cognition-b", "social", "values", "recovery", "patterns").map {
                bundled("catalog/strategies.$it.de.v1.json")
            })
    }
    private val parts by lazy { listOf("a", "b", "c").map {
        bundled("lessons/strategy-cases-$it.de.v1.json")
    } }
    private val cases by lazy {
        StrategyCaseCatalog.fromJsonParts(parts, strategyCatalog.strategies.map { it.id }.toSet())
    }

    @Test fun twelveNewArticlesAndThreeOpenSeriesKeepOriginalTwentyAndFour() {
        assertEquals(20, LessonCatalog.fromJson(oldLessons).lessons.size)
        assertEquals(32, library.lessons.size)
        assertEquals(7, library.series.size)
        val old = LessonCatalog.fromJson(oldLessons)
        old.lessons.forEach { before -> assertEquals(before, library.find(before.id)) }
        old.series.forEach { before ->
            assertEquals(before, library.series.first { it.id == before.id })
        }
        val newSeries = library.series.filter { it.id.startsWith("strategiewelt-") }
        assertEquals(3, newSeries.size)
        assertTrue(newSeries.all { it.lessonIds.size == 4 })
        assertEquals(12, newSeries.flatMap { it.lessonIds }.toSet().size)
    }

    @Test fun newArticlesAreWellStructuredReadableSearchableAndEvidenceAware() {
        val ids = library.series.filter { it.id.startsWith("strategiewelt-") }
            .flatMap { it.lessonIds }
        assertEquals(ids.size, ids.distinct().size)
        ids.forEach { id ->
            val lesson = library.find(id)!!
            assertEquals("STRATEGIEN", lesson.topic.name)
            assertTrue(lesson.sections.size >= 3)
            assertTrue(lesson.sections.all { it.text.length >= 100 })
            assertTrue(lesson.sources.size >= 2)
            assertTrue(lesson.sources.all { it.startsWith("https://") })
            assertTrue(lesson.check.correctIndex in 0..1)
            assertTrue(lesson.learningGoal.length >= 25)
            assertTrue(library.browse(lesson.title).contains(lesson))
        }
        assertEquals(setOf(0, 1), ids.map { library.find(it)!!.check.correctIndex }.toSet())
    }

    @Test fun fifteenStandaloneCasesWithExplanationsUncertaintyAndValidStrategyLinks() {
        assertEquals(15, cases.cases.size)
        assertEquals(15, cases.cases.map { it.id }.toSet().size)
        val known = strategyCatalog.strategies.map { it.id }.toSet()
        cases.cases.forEach { item ->
            assertEquals(2, item.choices.size)
            assertTrue(item.bestIndex in 0..1)
            assertTrue(item.relatedStrategyIds.isNotEmpty())
            assertTrue(item.relatedStrategyIds.all { it in known })
            assertTrue(item.feedback.length >= 100)
            assertTrue(item.reflection.length >= 40)
            assertTrue(item.sources.isNotEmpty())
        }
        assertEquals(setOf(0, 1), cases.cases.map { it.bestIndex }.toSet())
        assertTrue(cases.browse("Akzeptanz").isNotEmpty())
        assertTrue(cases.browse("grenze").isNotEmpty())
    }

    @Test fun malformedPartsAreRejectedWithoutCorruptingTheOriginalLibrary() {
        assertThrows(IllegalArgumentException::class.java) {
            LessonCatalog.fromJsonParts(oldLessons, chapters + chapters.first())
        }
        assertThrows(IllegalArgumentException::class.java) {
            LessonCatalog.fromJsonParts(oldLessons,
                listOf(chapters.first().replaceFirst(
                    Regex("\"schemaVersion\"\\s*:\\s*1"), "\"schemaVersion\":2"
                )) + chapters.drop(1))
        }
        assertEquals(20, LessonCatalog.fromJson(oldLessons).lessons.size)
        assertThrows(IllegalArgumentException::class.java) {
            StrategyCaseCatalog.fromJsonParts(parts + parts.first(),
                strategyCatalog.strategies.map { it.id }.toSet())
        }
        assertThrows(IllegalArgumentException::class.java) {
            StrategyCaseCatalog.fromJsonParts(parts, setOf("not-a-strategy"))
        }
    }

    @Test fun oldLessonBookmarksAndReadProgressSurviveCatalogExpansion() {
        val before = LearningState()
            .toggleArticleBookmark("strategien-pruefen")
            .markArticleRead("strategien-pruefen")
        val restored = LearningProgressFormat.decode(LearningProgressFormat.encode(before))
        assertEquals(before, restored)
        assertNotNull(library.find(restored.readArticles.first()))
        assertNotNull(library.find("s-vertiefung-flexibilitaet"))
        assertTrue(restored.readArticles.none { it.startsWith("s-vertiefung-") })
    }
}
