package com.valoon4.emotionskompass.core.learning

import com.valoon4.emotionskompass.core.model.EmotionCatalog
import com.valoon4.emotionskompass.core.model.NeedCatalog
import java.time.LocalDate

/** Only learning metadata, never journal notes or check-in text. */
data class LearningState(
    val favorites: Set<String> = emptySet(),
    val seen: Set<String> = emptySet(),
    val reviewCounts: Map<String, Int> = emptyMap(),
    val completedStations: Set<String> = emptySet()
) {
    fun toggleFavorite(id: String) = copy(
        favorites = if (id in favorites) favorites - id else favorites + id
    )
    fun markSeen(id: String) = copy(seen = seen + id)
    fun markReviewed(id: String) = copy(
        seen = seen + id,
        reviewCounts = reviewCounts + (id to ((reviewCounts[id] ?: 0) + 1).coerceAtMost(10000))
    )
    fun markStation(station: String) = copy(
        completedStations = completedStations + station
    )
    val reviewTotal: Int get() = reviewCounts.values.sum()
}

enum class LearningKind { EMOTION, NEED }

data class LearningWord(
    val id: String,
    val name: String,
    val description: String,
    val example: String,
    val question: String,
    val kind: LearningKind
) {
    val key: String get() = if (kind == LearningKind.EMOTION) "e:$id" else "n:$id"
}

data class DailyWords(val emotion: LearningWord, val need: LearningWord)

data class LearningQuiz(
    val id: String,
    val prompt: String,
    val choices: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val wordKey: String
) {
    init {
        require(choices.size >= 2 && correctIndex in choices.indices)
        require(choices.distinct().size == choices.size)
    }
}

data class LearningRoute(
    val key: String,
    val title: String,
    val description: String,
    val words: List<LearningWord>
) {
    fun stationId(word: LearningWord) = "$key/${word.key}"
}

/**
 * Educational exercises draw entirely from editorial catalog facts.
 * A right/wrong answer only tests the stated lexical distinction, never
 * predicts or grades a person's real emotions.
 */
object LearningEngine {
    fun allWords(emotions: EmotionCatalog, needs: NeedCatalog): List<LearningWord> =
        emotions.emotions.map {
            LearningWord(it.id, it.name, it.explanation, it.example, it.reflectionQuestion,
                LearningKind.EMOTION)
        } + needs.needs.map {
            LearningWord(it.id, it.name, it.definition, it.fulfilledExample,
                it.reflectionQuestion, LearningKind.NEED)
        }

    fun daily(emotions: EmotionCatalog, needs: NeedCatalog, day: LocalDate): DailyWords {
        val e = emotions.emotions.sortedBy { it.id }
        val n = needs.needs.sortedBy { it.id }
        require(e.isNotEmpty() && n.isNotEmpty())
        val dayNumber = day.toEpochDay()
        val one = e[Math.floorMod(dayNumber, e.size.toLong()).toInt()]
        val two = n[Math.floorMod(dayNumber * 7L + 23L, n.size.toLong()).toInt()]
        return DailyWords(
            LearningWord(one.id, one.name, one.explanation, one.example,
                one.reflectionQuestion, LearningKind.EMOTION),
            LearningWord(two.id, two.name, two.definition, two.fulfilledExample,
                two.reflectionQuestion, LearningKind.NEED)
        )
    }

    fun routes(emotions: EmotionCatalog, needs: NeedCatalog): List<LearningRoute> =
        emotions.learningPaths.map { path ->
            LearningRoute("e:${path.id}", path.title, path.description,
                emotions.emotionsInPath(path).map {
                    LearningWord(it.id, it.name, it.explanation, it.example,
                        it.reflectionQuestion, LearningKind.EMOTION)
                })
        } + needs.learningPaths.map { path ->
            LearningRoute("n:${path.id}", path.title, path.description,
                needs.itemsInPath(path).map {
                    LearningWord(it.id, it.name, it.definition, it.fulfilledExample,
                        it.reflectionQuestion, LearningKind.NEED)
                })
        }

    fun quizzes(emotions: EmotionCatalog, needs: NeedCatalog): List<LearningQuiz> {
        val emotionQuizzes = emotions.comparisons.mapIndexed { index, pair ->
            val first = requireNotNull(emotions.find(pair.first))
            val second = requireNotNull(emotions.find(pair.second))
            val reversed = index % 2 != 0
            LearningQuiz(
                id = "ec:$index",
                prompt = "Welche Beschreibung ist im Atlas eher ${first.name} zugeordnet, nicht ${second.name}?",
                choices = if (reversed) listOf(pair.secondDistinction, pair.firstDistinction)
                    else listOf(pair.firstDistinction, pair.secondDistinction),
                correctIndex = if (reversed) 1 else 0,
                explanation = "${pair.overlap} ${pair.firstDistinction} ${pair.secondDistinction}",
                wordKey = "e:${pair.first}"
            )
        }
        val needQuizzes = needs.comparisons.mapIndexed { index, pair ->
            val first = requireNotNull(needs.find(pair.first))
            val second = requireNotNull(needs.find(pair.second))
            val reversed = index % 2 != 0
            LearningQuiz(
                id = "nc:$index",
                prompt = "Was kennzeichnet laut Atlas eher das Bedürfnis ${first.name} als ${second.name}?",
                choices = if (reversed) listOf(pair.secondDistinction, pair.firstDistinction)
                    else listOf(pair.firstDistinction, pair.secondDistinction),
                correctIndex = if (reversed) 1 else 0,
                explanation = "${pair.overlap} ${pair.firstDistinction} ${pair.secondDistinction}",
                wordKey = "n:${pair.first}"
            )
        }
        val strategyQuizzes = needs.needs.take(32).mapIndexed { index, need ->
            val options = if (index % 2 == 0)
                listOf(need.name, need.strategyExample)
            else listOf(need.strategyExample, need.name)
            LearningQuiz(
                id = "strategy:$index",
                prompt = "Was davon bezeichnet ein Bedürfnis und nicht eine mögliche Handlung?",
                choices = options,
                correctIndex = if (index % 2 == 0) 0 else 1,
                explanation = "${need.name} ist hier das Bedürfnis. „${need.strategyExample}“ ist ein mögliches Vorgehen, kein universelles Rezept.",
                wordKey = "n:${need.id}"
            )
        }
        return (emotionQuizzes + needQuizzes + strategyQuizzes).filter {
            it.choices[0] != it.choices[1]
        }
    }

    /** A small, voluntary mix of known words and under-reviewed favorites. */
    fun reviewQueue(words: List<LearningWord>, state: LearningState, limit: Int = 5): List<LearningWord> {
        if (limit <= 0) return emptyList()
        val candidates = words.filter { it.key in state.seen || it.key in state.favorites }
        return candidates.sortedWith(compareBy<LearningWord> {
            state.reviewCounts[it.key] ?: 0
        }.thenByDescending { it.key in state.favorites }.thenBy { it.name })
            .take(limit)
    }

    fun quizQueue(quizzes: List<LearningQuiz>, state: LearningState, limit: Int = 5): List<LearningQuiz> {
        if (limit <= 0) return emptyList()
        val rank = quizzes.sortedWith(compareBy<LearningQuiz> {
            state.reviewCounts[it.wordKey] ?: 0
        }.thenByDescending { it.wordKey in state.favorites }
            .thenBy { it.id })
        // Both emotion and need topics are present in a short session.
        val mixed = mutableListOf<LearningQuiz>()
        val emotion = rank.filter { it.wordKey.startsWith("e:") }
        val need = rank.filter { it.wordKey.startsWith("n:") }
        var i = 0
        while (mixed.size < limit && (i < emotion.size || i < need.size)) {
            emotion.getOrNull(i)?.let { if (mixed.size < limit) mixed.add(it) }
            need.getOrNull(i)?.let { if (mixed.size < limit) mixed.add(it) }
            i++
        }
        return mixed
    }
}
