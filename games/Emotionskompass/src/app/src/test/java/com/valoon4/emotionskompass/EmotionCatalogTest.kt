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

    @Test fun lexiconContainsFortyEightDistinctEntriesInEightFamilies() {
        assertEquals(2, catalog.schemaVersion)
        assertEquals(48, catalog.emotions.size)
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
        assertEquals(9, catalog.comparisons.size)
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
        assertEquals(48, catalog.search("").size)
        assertEquals("frustration", catalog.search("  FRUST  ").first().id)
    }
}
