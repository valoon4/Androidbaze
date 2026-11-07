package com.valoon4.emotionskompass

import com.valoon4.emotionskompass.core.model.EmotionCatalog
import com.valoon4.emotionskompass.core.model.WordCardDeck
import java.io.File
import kotlin.random.Random
import org.junit.Assert.*
import org.junit.Test

class WordCardDeckTest {
    private val catalog: EmotionCatalog by lazy {
        EmotionCatalog.fromJson(
            File("src/main/assets/catalog/emotions.de.v2.json").readText(Charsets.UTF_8)
        )
    }

    @Test fun eachRoundIncludesEveryUniqueIdExactlyOnce() {
        val ids = catalog.search("").map { it.id }
        val round = WordCardDeck.shuffle(ids, random = Random(2026))
        assertEquals(289, round.size)
        assertEquals(ids.toSet(), round.toSet())
        assertEquals(round.size, round.distinct().size)
        assertNotEquals("The deck should no longer use alphabetic order", ids, round)
    }

    @Test fun anotherRoundNeverImmediatelyRepeatsTheCurrentCard() {
        val ids = listOf("eins", "zwei", "drei", "vier")
        for (seed in 0..99) {
            val next = WordCardDeck.shuffle(ids, avoidFirst = "zwei", random = Random(seed))
            assertEquals(ids.toSet(), next.toSet())
            assertNotEquals("zwei", next.first())
        }
    }

    @Test fun filteringBeforeShufflingKeepsOnlyTheChosenFamily() {
        for (family in catalog.familyNames) {
            val matching = catalog.search("", family).map { it.id }
            val round = WordCardDeck.shuffle(matching, random = Random(7))
            assertEquals(matching.toSet(), round.toSet())
            assertTrue(round.all { id -> family in catalog.find(id)!!.families })
        }
        assertTrue(WordCardDeck.shuffle(emptyList()).isEmpty())
    }

    @Test fun oneCardAndRepeatedInputAreSafe() {
        assertEquals(listOf("eins"),
            WordCardDeck.shuffle(listOf("eins", "eins"), avoidFirst = "eins"))
        assertEquals(listOf("eins", "zwei"),
            WordCardDeck.shuffle(listOf("eins", "zwei"), avoidFirst = "zwei",
                random = Random(0)).sorted())
    }

    @Test fun roundsDoNotMutateTheSourceList() {
        val ids = listOf("alpha", "beta", "gamma")
        val original = ids.toList()
        WordCardDeck.shuffle(ids, random = Random(12))
        assertEquals(original, ids)
    }
}
