package com.valoon4.emotionskompass

import com.valoon4.emotionskompass.core.checkin.CheckInDraft
import com.valoon4.emotionskompass.core.model.NeedCatalog
import com.valoon4.emotionskompass.core.model.NeedLens
import com.valoon4.emotionskompass.core.model.EmotionCatalog
import com.valoon4.emotionskompass.core.navigation.AppViewModel
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class NeedCatalogTest {
    private val source by lazy {
        File("src/main/assets/catalog/needs.de.v1.json").readText()
    }
    private val catalog by lazy { NeedCatalog.fromJson(source) }
    private val emotions by lazy {
        EmotionCatalog.fromJson(File("src/main/assets/catalog/emotions.de.v2.json").readText())
    }

    @Test fun atlasContains82DistinctGermanTermsInEightFamilies() {
        assertEquals(82, catalog.needs.size)
        assertEquals(82, catalog.needs.map { it.id }.distinct().size)
        assertEquals(8, catalog.familyNames.size)
        assertTrue(catalog.familyNames.all { catalog.browse("", it).size >= 10 })
    }

    @Test fun eachEntryHasTwoDifferentPerspectivesAndOwnReflection() {
        assertTrue(catalog.needs.all {
            it.definition.isNotBlank() && it.fulfilledExample.isNotBlank() &&
                it.unmetExample.isNotBlank() &&
                it.fulfilledExample != it.unmetExample &&
                it.strategyExample.isNotBlank() && it.reflectionQuestion.length >= 10
        })
    }

    @Test fun referencesResolveAndOverlappingFamiliesAreSupported() {
        assertTrue(catalog.needs.all { word ->
            word.related.all { catalog.find(it) != null }
        })
        assertTrue(catalog.needs.count { it.families.size > 1 } >= 12)
    }

    @Test fun germanSearchWorksWithUmlautsAndAliases() {
        assertEquals("geborgenheit", catalog.browse("Geborgenheit").first().id)
        assertEquals("naehe", catalog.browse("Nähe").first().id)
        assertEquals("naehe", catalog.browse("Naehe").first().id)
        assertEquals("selbstbestimmung", catalog.browse("AUTONOMIE").first().id)
        assertTrue(catalog.browse("", letter = "Ä").isEmpty())
        assertTrue(catalog.browse("", letter = "G").any { it.id == "geborgenheit" })
    }

    @Test fun familyAndQueryFilteringCanCombine() {
        val result = catalog.browse("Ruhe", "Ruhe & Wohlbefinden")
        assertTrue(result.isNotEmpty())
        assertTrue(result.all { "Ruhe & Wohlbefinden" in it.families })
        assertTrue(catalog.browse("", "falsche Familie").isEmpty())
    }

    @Test fun editorialContrastsAreDistinctAndReversible() {
        assertEquals(20, catalog.comparisons.size)
        assertEquals(20, catalog.comparisons.map { setOf(it.first, it.second) }.distinct().size)
        val direct = catalog.compare("sicherheit", "geborgenheit")!!
        val reverse = catalog.compare("geborgenheit", "sicherheit")!!
        assertEquals(direct.firstDistinction, reverse.secondDistinction)
        assertEquals(direct.secondDistinction, reverse.firstDistinction)
    }

    @Test fun allEightLearningPathsHaveValidOrderedTerms() {
        assertEquals(8, catalog.learningPaths.size)
        assertTrue(catalog.learningPaths.all { path ->
            path.needIds.size >= 4 &&
                catalog.itemsInPath(path).map { it.id } == path.needIds
        })
    }

    @Test fun existingCheckInNeedsMapToAtlasIds() {
        val names = listOf("ruhe", "sicherheit", "naehe", "verstandenwerden", "respekt",
            "selbstbestimmung", "klarheit", "unterstuetzung", "fairness",
            "erholung", "ausdruck", "raum_fuer_mich")
        assertTrue(names.all { catalog.find(it) != null })
    }

    @Test fun unknownIdsCannotBeSelectedViaViewModel() {
        val vm = AppViewModel(emotions, catalog)
        vm.openCheckin()
        vm.toggleCheckInNeed("invented_invalid_need")
        assertTrue(vm.checkIn.value.needs.isEmpty())
        vm.toggleCheckInNeed("ruhe")
        assertEquals(setOf("ruhe"), vm.checkIn.value.needs)
    }

    @Test fun notKnowingIsAnExplicitReversibleChoice() {
        val draft = CheckInDraft().toggleNeed("ruhe").toggleNeed("klarheit")
        val unsure = draft.toggleNeed(CheckInDraft.UNKNOWN_NEED_ID)
        assertEquals(setOf(CheckInDraft.UNKNOWN_NEED_ID), unsure.needs)
        assertTrue(unsure.toggleNeed(CheckInDraft.UNKNOWN_NEED_ID).needs.isEmpty())
    }

    @Test fun previewClassificationNeverInfersNeedFromEmotion() {
        val vm = AppViewModel(emotions, catalog)
        vm.openCheckin("aerger")
        assertTrue(vm.checkIn.value.needs.isEmpty())
        vm.toggleCheckInNeed("respekt")
        assertTrue("respekt" in vm.checkIn.value.needs)
        assertTrue("aerger" in vm.checkIn.value.emotions)
        vm.closeCheckIn()
        assertEquals(CheckInDraft(), vm.checkIn.value)
    }

    @Test fun lensOptionsDistinguishImportanceFulfilmentAndUncertainty() {
        assertEquals(4, NeedLens.entries.size)
        assertEquals(NeedLens.WICHTIG, NeedLens.valueOf("WICHTIG"))
        assertEquals("Gut erfüllt", NeedLens.ERFUELLT.label)
        assertEquals("Kommt zu kurz", NeedLens.UNERFUELLT.label)
    }

    @Test fun malformedCatalogIsRejected() {
        val tampered = source.replace("\"related\": [", "\"related\": [\"broken_id\",")
        assertNotEquals(source, tampered)
        assertThrows(IllegalArgumentException::class.java) { NeedCatalog.fromJson(tampered) }
    }
}
