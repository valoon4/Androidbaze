package com.valoon4.emotionskompass

import com.valoon4.emotionskompass.core.model.EmotionCatalog
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class EmotionCatalogTest {
    private val source: String by lazy {
        File("src/main/assets/catalog/emotions.de.v2.json").readText(Charsets.UTF_8)
    }
    private val catalog: EmotionCatalog by lazy { EmotionCatalog.fromJson(source) }

    @Test fun lexiconContainsTwoHundredEightyNineUniqueEntriesInEightFamilies() {
        assertEquals(2, catalog.schemaVersion)
        assertEquals(289, catalog.emotions.size)
        assertEquals(8, catalog.familyNames.size)
        assertEquals(catalog.emotions.size, catalog.emotions.map { it.id }.toSet().size)
    }

    @Test fun everyEntryHasTwoExamplesAliasesAndReflectionQuestion() {
        assertTrue(catalog.emotions.all {
            it.explanation.isNotBlank() && it.examples.size >= 2 &&
                it.synonyms.isNotEmpty() && it.reflectionQuestion.isNotBlank()
        })
    }

    @Test fun referencesAndComparisonsResolveToRealTerms() {
        val ids = catalog.emotions.map { it.id }.toSet()
        assertTrue(catalog.emotions.flatMap { it.related }.all { it in ids })
        assertEquals(41, catalog.comparisons.size)
        assertTrue(catalog.comparisons.all { it.first in ids && it.second in ids })
    }

    @Test fun searchRanksNameBeforeExampleAndSynonym() {
        assertEquals("frustration", catalog.search("Frust").first().id)
        assertEquals("traurigkeit", catalog.search("Traurigkeit").first().id)
        assertEquals("angst", catalog.search("angst").first().id)
    }

    @Test fun searchFindsSynonymsAndUmlautTransliterations() {
        assertEquals("nervositaet", catalog.search("aufgeregt").first().id)
        assertEquals("aerger", catalog.search("Ärger").first().id)
        assertEquals("aerger", catalog.search("Aerger").first().id)
        assertEquals("enttaeuschung", catalog.search("Enttäuschung").first().id)
        assertEquals(catalog.search("WEHMUT"), catalog.search("wehmut"))
    }

    @Test fun familyMembershipAllowsMoreThanOneCategory() {
        assertTrue(catalog.search("", "Ärger").any { it.id == "enttaeuschung" })
        assertTrue(catalog.search("", "Traurigkeit").any { it.id == "enttaeuschung" })
        assertTrue(catalog.search("", "Verbundenheit").all { "Verbundenheit" in it.families })
        assertTrue(catalog.search("", "unknown").isEmpty())
    }

    @Test fun comparingInReverseOrderKeepsExplanationsAligned() {
        val forward = catalog.compare("frustration", "enttaeuschung")!!
        val backward = catalog.compare("enttaeuschung", "frustration")!!
        assertEquals(forward.firstDistinction, backward.secondDistinction)
        assertEquals(forward.secondDistinction, backward.firstDistinction)
        assertEquals(forward.overlap, backward.overlap)
    }

    @Test fun unknownComparisonDoesNotInventExplanation() {
        assertNull(catalog.compare("zufriedenheit", "angst"))
    }

    @Test fun rejectsUnsupportedSchemaVersion() {
        val altered = source.replace("\"schemaVersion\": 2", "\"schemaVersion\": 999")
        assertThrows(IllegalArgumentException::class.java) { EmotionCatalog.fromJson(altered) }
    }

    @Test fun rejectsBrokenRelationReference() {
        val altered = source.replace(
            "\"id\": \"frustration\"",
            "\"id\": \"not-found-frustration\""
        )
        assertThrows(IllegalArgumentException::class.java) { EmotionCatalog.fromJson(altered) }
    }

    @Test fun catalogIsOfflineAndHasNoNetworkDependency() {
        assertNotNull(catalog.find("wehmut"))
        assertEquals(289, catalog.search("").size)
        assertEquals("frustration", catalog.search("  FRUST  ").first().id)
    }
    @Test fun learningPathsContainValidAndOrderedTerms() {
        assertEquals(14, catalog.learningPaths.size)
        assertTrue(catalog.learningPaths.all { catalog.emotionsInPath(it).size == it.emotionIds.size })
        val example = catalog.findPath("neid_eifersucht")!!
        assertEquals("neid", catalog.emotionsInPath(example).first().id)
        assertNull(catalog.findPath("unknown"))
    }

    @Test fun letterFilterHandlesUmlautsAndFilterCombinations() {
        assertTrue(catalog.browse("", letter = "A").any { it.id == "aerger" })
        assertTrue(catalog.browse("", letter = "E").any { it.id == "eifersucht" })
        assertTrue(catalog.browse("", family = "Ärger", letter = "E").any { it.id == "eifersucht" })
        assertTrue(catalog.browse("", family = "Ruhe", letter = "E").all { "Ruhe" in it.families })
    }

    @Test fun emotionTypesCanBeBrowsedWithoutExcludingFamilyMembership() {
        val moods = catalog.browse("", kind = com.valoon4.emotionskompass.core.model.EmotionKind.MOOD)
        val emotions = catalog.browse("", kind = com.valoon4.emotionskompass.core.model.EmotionKind.EMOTION)
        val states = catalog.browse("", kind = com.valoon4.emotionskompass.core.model.EmotionKind.STATE)
        assertEquals(289, moods.size + emotions.size + states.size)
        assertTrue(moods.any { it.id == "euphorie" })
        assertTrue(states.any { it.id == "akzeptanz" })
    }

    @Test fun similarTermsHaveDistinctDefinitionsAndExamples() {
        val envy = catalog.find("neid")!!
        val jealousy = catalog.find("eifersucht")!!
        assertNotEquals(envy.explanation, jealousy.explanation)
        assertNotEquals(envy.examples.first(), jealousy.examples.first())
        assertNotNull(catalog.compare(envy.id, jealousy.id))
        assertNotNull(catalog.compare("resignation", "akzeptanz"))
    }

    @Test fun rejectsLearningPathWithUnknownTerm() {
        val bad = source.replace(
            "\"emotionIds\": [",
            "\"emotionIds\": [\"missing_word\","
        )
        assertNotEquals(source, bad)
        assertThrows(IllegalArgumentException::class.java) { EmotionCatalog.fromJson(bad) }
    }

    @Test fun everyFamilyHasBroadVocabulary() {
        assertTrue(catalog.familyNames.all { family ->
            catalog.search("", family).size >= 15
        })
        assertTrue(catalog.emotions.count { it.families.size > 1 } >= 20)
    }

    @Test fun allEightFamiliesIncludeManyDistinctExamples() {
        assertEquals(289, catalog.emotions.map { it.id }.distinct().size)
        assertTrue(catalog.familyNames.all { catalog.search("", it).size >= 30 })
        assertTrue(catalog.emotions.all { emotion ->
            emotion.examples.size == 2 && emotion.examples.distinct().size == 2 &&
                emotion.related.isNotEmpty() && emotion.reflectionQuestion.length >= 10
        })
    }

    @Test fun newComparisonLibraryIsCompleteAndBidirectional() {
        assertEquals(41, catalog.comparisons.size)
        val comparedPairs = catalog.comparisons.map { setOf(it.first, it.second) }
        assertEquals(41, comparedPairs.distinct().size)
        assertNotNull(catalog.compare("ekel", "abscheu"))
        assertNotNull(catalog.compare("abscheu", "ekel"))
        assertEquals("ekel", catalog.compare("abscheu", "ekel")!!.second)
        assertNotNull(catalog.compare("selbstmitgefuehl", "selbstmitleid"))
    }

    @Test fun thematicLearningRoutesPreserveSequence() {
        assertEquals(14, catalog.learningPaths.size)
        assertTrue(catalog.learningPaths.all { it.emotionIds.size >= 3 })
        val path = catalog.findPath("zurueck_in_die_ruhe")!!
        assertEquals("anspannung", catalog.emotionsInPath(path).first().id)
        assertEquals("seelenruhe", catalog.emotionsInPath(path).last().id)
    }

    @Test fun GermanSearchIncludesNewDistinctTerms() {
        assertEquals("gluecksgefuehl", catalog.search("Glücksgefühl").first().id)
        assertEquals("ekel", catalog.search("Ekel").first().id)
        assertEquals("wissensdurst", catalog.search("Wissensdurst").first().id)
        assertEquals("herzlichkeit", catalog.search("HERZLICHKEIT").first().id)
        assertTrue(catalog.browse("", letter = "Z").any { it.id == "zentriertheit" })
    }

    @Test fun noPersonalRecordsInEditorialCatalog() {
        assertEquals("2026-10-m2.2", catalog.contentRevision)
        assertTrue(catalog.editorialStatus.contains("not-clinically-validated"))
    }

}
