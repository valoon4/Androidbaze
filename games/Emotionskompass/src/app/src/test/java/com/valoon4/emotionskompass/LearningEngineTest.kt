package com.valoon4.emotionskompass

import com.valoon4.emotionskompass.core.learning.LearningEngine
import com.valoon4.emotionskompass.core.learning.LearningKind
import com.valoon4.emotionskompass.core.learning.LearningProgressFormat
import com.valoon4.emotionskompass.core.learning.LearningState
import com.valoon4.emotionskompass.core.model.EmotionCatalog
import com.valoon4.emotionskompass.core.model.NeedCatalog
import java.io.File
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class LearningEngineTest {
    private val emotions by lazy {
        EmotionCatalog.fromJson(File("src/main/assets/catalog/emotions.de.v2.json").readText())
    }
    private val needs by lazy {
        NeedCatalog.fromJson(File("src/main/assets/catalog/needs.de.v1.json").readText())
    }
    private val all by lazy { LearningEngine.allWords(emotions, needs) }

    @Test fun combinedLearningAtlasHasSeparateStableIds() {
        assertEquals(emotions.emotions.size + needs.needs.size, all.size)
        assertEquals(all.size, all.map { it.key }.distinct().size)
        assertEquals(emotions.emotions.size, all.count { it.kind == LearningKind.EMOTION })
        assertEquals(needs.needs.size, all.count { it.kind == LearningKind.NEED })
        assertTrue(all.all { it.description.isNotBlank() && it.question.isNotBlank() })
    }

    @Test fun dailySelectionIsDeterministicOfflineAndFromBothCatalogs() {
        val day = LocalDate.of(2026, 11, 2)
        val a = LearningEngine.daily(emotions, needs, day)
        assertEquals(a, LearningEngine.daily(emotions, needs, day))
        assertEquals(LearningKind.EMOTION, a.emotion.kind)
        assertEquals(LearningKind.NEED, a.need.kind)
        assertNotEquals(a.emotion.key, a.need.key)
        assertNotEquals(a, LearningEngine.daily(emotions, needs, day.plusDays(1)))
    }

    @Test fun learningPathsIncludeAllExistingCatalogPaths() {
        val routes = LearningEngine.routes(emotions, needs)
        assertEquals(emotions.learningPaths.size + needs.learningPaths.size, routes.size)
        assertEquals(routes.size, routes.map { it.key }.distinct().size)
        assertTrue(routes.all { it.words.size >= 3 })
        assertTrue(routes.all { route ->
            route.words.all { route.stationId(it).contains("/" + it.key) }
        })
    }

    @Test fun quizSetContainsEmotionsNeedsAndStrategies() {
        val quizzes = LearningEngine.quizzes(emotions, needs)
        assertTrue(quizzes.size >= 70)
        assertTrue(quizzes.any { it.id.startsWith("ec:") })
        assertTrue(quizzes.any { it.id.startsWith("nc:") })
        assertTrue(quizzes.any { it.id.startsWith("strategy:") })
        assertTrue(quizzes.all { it.correctIndex in it.choices.indices })
        assertTrue(quizzes.all { it.choices.distinct().size == it.choices.size })
        assertTrue(quizzes.all { it.explanation.isNotBlank() })
    }

    @Test fun smallQuizSessionContainsBothTopics() {
        val round = LearningEngine.quizQueue(LearningEngine.quizzes(emotions, needs),
            LearningState(), limit = 5)
        assertEquals(5, round.size)
        assertTrue(round.any { it.wordKey.startsWith("e:") })
        assertTrue(round.any { it.wordKey.startsWith("n:") })
    }

    @Test fun favoritesAreToggledWithoutAffectingJournal() {
        val first = LearningState().toggleFavorite("e:wehmut")
        assertEquals(setOf("e:wehmut"), first.favorites)
        assertTrue(first.seen.isEmpty())
        val second = first.toggleFavorite("n:naehe")
        assertEquals(2, second.favorites.size)
        assertEquals(setOf("n:naehe"), second.toggleFavorite("e:wehmut").favorites)
    }

    @Test fun reviewCountsAndSeenAreVoluntaryAndIndependent() {
        val seen = LearningState().markSeen("e:wehmut")
        assertEquals(0, seen.reviewTotal)
        assertTrue("e:wehmut" in seen.seen)
        val reviewed = seen.markReviewed("e:wehmut").markReviewed("e:wehmut")
        assertEquals(2, reviewed.reviewCounts["e:wehmut"])
        assertEquals(2, reviewed.reviewTotal)
        assertTrue(reviewed.favorites.isEmpty())
    }

    @Test fun repeatQueueIncludesOnlySeenOrFavoriteWords() {
        val state = LearningState().markSeen(all[0].key).toggleFavorite(all.last().key)
        val queue = LearningEngine.reviewQueue(all, state, limit = 5)
        assertEquals(2, queue.size)
        assertEquals(setOf(all[0].key, all.last().key), queue.map { it.key }.toSet())
    }

    @Test fun reviewedItemsAreNotPrioritizedOverUnreviewedItems() {
        val first = all[0]
        val second = all[1]
        val state = LearningState()
            .markReviewed(first.key).markSeen(second.key)
        val queue = LearningEngine.reviewQueue(all, state)
        assertEquals(second.key, queue.first().key)
    }

    @Test fun progressRoundTripRetainsFavoritesHistoryAndPathStations() {
        val route = LearningEngine.routes(emotions, needs).first()
        val word = route.words.first()
        val state = LearningState().toggleFavorite("e:wehmut")
            .toggleFavorite("n:naehe").markSeen("e:wehmut")
            .markReviewed("n:naehe").markStation(route.stationId(word))
        val restored = LearningProgressFormat.decode(LearningProgressFormat.encode(state))
        assertEquals(state, restored)
    }

    @Test fun unknownSchemaVersionIsRejected() {
        val original = LearningProgressFormat.encode(LearningState()).toString(Charsets.UTF_8)
        val wrong = original.replace("\"version\":1", "\"version\":12")
        assertThrows(IllegalArgumentException::class.java) {
            LearningProgressFormat.decode(wrong.toByteArray())
        }
    }

    @Test fun invalidKeysCannotPolluteStoredLearningData() {
        assertThrows(IllegalArgumentException::class.java) {
            LearningProgressFormat.encode(LearningState(favorites = setOf("../private_journal_v1.enc")))
        }
        assertThrows(IllegalArgumentException::class.java) {
            LearningProgressFormat.encode(LearningState(reviewCounts = mapOf("e:valid" to -1)))
        }
    }

    @Test fun emptyProgressRoundTripsAndResetIsNonJournal() {
        val cleared = LearningState()
        assertEquals(cleared, LearningProgressFormat.decode(LearningProgressFormat.encode(cleared)))
        assertEquals(0, cleared.reviewTotal)
    }

    @Test fun zeroLengthStudySessionDoesNotCreateAnyItems() {
        assertTrue(LearningEngine.reviewQueue(all, LearningState(), 0).isEmpty())
        assertTrue(LearningEngine.quizQueue(LearningEngine.quizzes(emotions, needs),
            LearningState(), 0).isEmpty())
    }
}
