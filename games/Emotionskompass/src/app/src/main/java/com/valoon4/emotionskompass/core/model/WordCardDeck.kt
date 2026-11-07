package com.valoon4.emotionskompass.core.model

import kotlin.random.Random

/**
 * Creates one random, non-repeating discovery round from public catalog IDs.
 * No journal entries, answers or learning progress are accessed or stored.
 */
object WordCardDeck {
    fun shuffle(
        ids: List<String>,
        avoidFirst: String? = null,
        random: Random = Random.Default
    ): ArrayList<String> {
        val result = ArrayList(ids.distinct().shuffled(random))
        if (result.size > 1 && result.first() == avoidFirst) {
            // A newly mixed round must not repeat the card visible just before it.
            val first = result[0]
            result[0] = result[1]
            result[1] = first
        }
        return result
    }
}
