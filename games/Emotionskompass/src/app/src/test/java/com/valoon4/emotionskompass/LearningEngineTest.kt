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

    /** All curated comparison questions must hide the compared labels from choices. */
    @Test fun emotionComparisonsDoNotGiveAwayTheirTermNames() {
        val questions = LearningEngine.quizzes(emotions, needs)
            .filter { it.id.startsWith("ec:") }
        assertEquals(emotions.comparisons.size, questions.size)
        questions.forEach { quiz ->
            val pair = emotions.comparisons[quiz.id.substringAfter(':').toInt()]
            val names = listOf(
                requireNotNull(emotions.find(pair.first)).name,
                requireNotNull(emotions.find(pair.second)).name
            )
            quiz.choices.forEach { choice ->
                assertTrue("Nicht neutralisiert: ${quiz.id}: $choice",
                    choice.startsWith("Bei diesem Gefühl") || choice.startsWith("Dieses Gefühl"))
                names.forEach { name ->
                    val pattern = Regex(
                        "(?<!\\p{L})" + Regex.escape(name) +
                            "(?:en|n|s|e|er)?(?!\\p{L})",
                        RegexOption.IGNORE_CASE
                    )
                    assertFalse("Begriff $name verrät die Lösung in ${quiz.id}: $choice",
                        pattern.containsMatchIn(choice))
                }
            }
            assertTrue(quiz.explanation.contains(names[0]))
            assertTrue(quiz.explanation.contains(names[1]))
        }
    }

    @Test fun needComparisonsDoNotGiveAwayTheirTermNames() {
        val questions = LearningEngine.quizzes(emotions, needs)
            .filter { it.id.startsWith("nc:") }
        assertEquals(needs.comparisons.size, questions.size)
        questions.forEach { quiz ->
            val pair = needs.comparisons[quiz.id.substringAfter(':').toInt()]
            val names = listOf(
                requireNotNull(needs.find(pair.first)).name,
                requireNotNull(needs.find(pair.second)).name
            )
            quiz.choices.forEach { choice ->
                assertTrue("Nicht neutralisiert: ${quiz.id}: $choice",
                    choice.startsWith("Bei diesem Bedürfnis") ||
                        choice.startsWith("Dieses Bedürfnis"))
                names.forEach { name ->
                    val pattern = Regex(
                        "(?<!\\p{L})" + Regex.escape(name) +
                            "(?:en|n|s|e|er)?(?!\\p{L})",
                        RegexOption.IGNORE_CASE
                    )
                    assertFalse("Bedürfnis $name verrät die Lösung in ${quiz.id}: $choice",
                        pattern.containsMatchIn(choice))
                }
            }
        }
    }

    @Test fun strategyQuestionsHaveNeitherTermNamesNorTheObviousNeedPrefix() {
        val quizzes = LearningEngine.quizzes(emotions, needs)
            .filter { it.id.startsWith("strategy:") }
        assertEquals(32, quizzes.size)
        quizzes.forEach { quiz ->
            val need = needs.needs[quiz.id.substringAfter(':').toInt()]
            assertTrue(quiz.choices.none { it.startsWith("Das Bedürfnis") })
            val literalName = Regex(
                "(?<!\\p{L})" + Regex.escape(need.name) +
                    "(?:en|n|s|e|er)?(?!\\p{L})",
                RegexOption.IGNORE_CASE
            )
            assertTrue("Der direkte Begriff darf nicht als Antwort erscheinen: ${quiz.id}",
                quiz.choices.none { literalName.containsMatchIn(it) })
            assertTrue(quiz.explanation.contains(need.name))
        }
    }

    @Test fun shuffledAnswersKeepTheCorrectMeaningForEveryQuiz() {
        val quizzes = LearningEngine.quizzes(emotions, needs)
        for ((index, original) in quizzes.withIndex()) {
            val answer = original.choices[original.correctIndex]
            for (seed in listOf(1, 41, index + 2026)) {
                val randomized = original.shuffled(kotlin.random.Random(seed))
                assertEquals(original.id, randomized.id)
                assertEquals(original.prompt, randomized.prompt)
                assertEquals(original.explanation, randomized.explanation)
                assertEquals(answer, randomized.choices[randomized.correctIndex])
                assertEquals(original.choices.toSet(), randomized.choices.toSet())
            }
        }
    }

    @Test fun regressionFrustrationAndSafetyQuizzesOnlyShowNeutralDescriptions() {
        val allQuizzes = LearningEngine.quizzes(emotions, needs)
        val emotionsQuiz = allQuizzes.first { it.id == "ec:0" }
        assertTrue(emotionsQuiz.choices[0].startsWith("Bei diesem Gefühl steht häufig"))
        assertTrue(emotionsQuiz.choices[1].startsWith("Bei diesem Gefühl steht häufig"))
        assertFalse(emotionsQuiz.choices.any { it.contains("Frustration") || it.contains("Enttäuschung") })
        val needsQuiz = allQuizzes.first { it.id == "nc:0" }
        assertEquals("Dieses Bedürfnis betont Schutz und Handlungsfähigkeit.", needsQuiz.choices[0])
        assertEquals("Dieses Bedürfnis betont das Gefühl, gut aufgehoben zu sein.", needsQuiz.choices[1])
    }

}
