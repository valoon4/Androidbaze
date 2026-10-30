package com.valoon4.emotionskompass

import com.valoon4.emotionskompass.core.lessons.LessonCatalog
import com.valoon4.emotionskompass.core.lessons.LessonTopic
import com.valoon4.emotionskompass.core.learning.LearningState
import com.valoon4.emotionskompass.core.learning.LearningProgressFormat
import java.io.File
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class KnowledgeLessonTest {
    private val raw by lazy {
        File("src/main/assets/lessons/knowledge.de.v1.json").readText()
    }
    private val catalog by lazy { LessonCatalog.fromJson(raw) }

    @Test fun exactlyTwentyOfflineLessonsAndFourGuidedSeries() {
        assertEquals(20, catalog.lessons.size)
        assertEquals(4, catalog.series.size)
        assertTrue(catalog.lessons.map { it.id }.distinct().size == 20)
    }

    @Test fun everyTopicHasFiveIndependentArticles() {
        LessonTopic.entries.forEach { topic ->
            assertEquals(5, catalog.lessons.count { it.topic == topic })
        }
    }

    @Test fun seriesContainsEachLessonExactlyOnce() {
        val ids = catalog.series.flatMap { it.lessonIds }
        assertEquals(20, ids.size)
        assertEquals(ids.size, ids.distinct().size)
        assertEquals(catalog.lessons.map { it.id }.toSet(), ids.toSet())
        assertTrue(catalog.series.all { catalog.within(it).size == 5 })
    }

    @Test fun allLessonsHaveEditorialStructureAndClickableReferences() {
        assertTrue(catalog.lessons.all { it.lead.length >= 40 })
        assertTrue(catalog.lessons.all { it.sections.size >= 3 })
        assertTrue(catalog.lessons.all { it.sections.all { p -> p.text.length >= 100 } })
        assertTrue(catalog.lessons.all { it.sources.isNotEmpty() })
        assertTrue(catalog.lessons.all { it.sources.all { s -> s.startsWith("https://") } })
        assertTrue(catalog.lessons.all { it.reflection.isNotBlank() && it.takeaway.isNotBlank() })
    }

    @Test fun dailyReadingIsStableWithinCalendarDayAndRotates() {
        val day = LocalDate.of(2026, 10, 30)
        assertEquals(catalog.daily(day), catalog.daily(day))
        assertNotEquals(catalog.daily(day), catalog.daily(day.plusDays(1)))
        assertEquals(20, (0L until 20L).map {
            catalog.daily(day.plusDays(it)).id
        }.toSet().size)
    }

    @Test fun searchCoversTextAndFiltersByTopic() {
        assertNotNull(catalog.find("beduerfnis-und-strategie"))
        assertTrue(catalog.browse("Strategie").isNotEmpty())
        assertEquals(5, catalog.browse(topic = LessonTopic.REFLEXION).size)
        assertTrue(catalog.browse("gewaltfrei-nicht-im-text").isEmpty())
    }

    @Test fun arbitraryArticleIsReadableWithoutFollowingSeries() {
        val direct = catalog.find("gefuehle-benennen")
        assertNotNull(direct)
        assertTrue(direct!!.readingMinutes >= 2)
        assertTrue(catalog.series.any { direct.id in it.lessonIds })
    }

    @Test fun learningProgressRoundTripKeepsAllM6AndM62Fields() {
        val progress = LearningState()
            .toggleFavorite("e:wehmut")
            .markSeen("n:sicherheit")
            .markReviewed("e:wehmut")
            .markStation("e:path/e:wehmut")
            .toggleArticleBookmark("gefuehle-als-signale")
            .markArticleRead("gefuehle-als-signale")
        val bytes = LearningProgressFormat.encode(progress)
        assertEquals(progress, LearningProgressFormat.decode(bytes))
    }

    @Test fun oldM6ProgressFileWithoutNewFieldsLoadsUnchanged() {
        val original = LearningProgressFormat.encode(LearningState(
            favorites = setOf("e:wehmut"),
            seen = setOf("e:wehmut")
        )).toString(Charsets.UTF_8)
        val legacy = original
            .replace(Regex(",\"articleBookmarks\":\\[.*?\\]"), "")
            .replace(Regex(",\"readArticles\":\\[.*?\\]"), "")
        val loaded = LearningProgressFormat.decode(legacy.toByteArray())
        assertEquals(setOf("e:wehmut"), loaded.favorites)
        assertEquals(setOf("e:wehmut"), loaded.seen)
        assertTrue(loaded.articleBookmarks.isEmpty())
        assertTrue(loaded.readArticles.isEmpty())
    }

    @Test fun articleBookmarksCanBeRemovedWithoutDeletingReadingProgress() {
        val a = LearningState()
            .toggleArticleBookmark("gefuehle-als-signale")
            .markArticleRead("gefuehle-als-signale")
        val b = a.toggleArticleBookmark("gefuehle-als-signale")
        assertTrue(b.articleBookmarks.isEmpty())
        assertEquals(setOf("gefuehle-als-signale"), b.readArticles)
    }

    @Test fun unsafeArticleIdsAreRejectedBeforePersistence() {
        assertThrows(IllegalArgumentException::class.java) {
            LearningProgressFormat.encode(LearningState(
                readArticles = setOf("../private_journal_v1.enc")
            ))
        }
    }

    @Test fun invalidSchemaAndDuplicateSeriesEntriesAreRejected() {
        val invalidVersion = raw.replace("\"schemaVersion\": 1", "\"schemaVersion\": 2")
        assertThrows(IllegalArgumentException::class.java) {
            LessonCatalog.fromJson(invalidVersion)
        }
        val duplicates = raw.replace("\"gefuehle-benennen\"", "\"gefuehle-als-signale\"")
        assertThrows(IllegalArgumentException::class.java) {
            LessonCatalog.fromJson(duplicates)
        }
    }
}
