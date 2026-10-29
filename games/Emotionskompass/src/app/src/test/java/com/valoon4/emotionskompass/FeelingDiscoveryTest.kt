package com.valoon4.emotionskompass

import com.valoon4.emotionskompass.core.checkin.FeelingDirection
import com.valoon4.emotionskompass.core.checkin.FeelingDiscovery
import com.valoon4.emotionskompass.core.checkin.CheckInDraft
import com.valoon4.emotionskompass.core.checkin.CheckInMode
import com.valoon4.emotionskompass.core.checkin.CheckInStep
import com.valoon4.emotionskompass.core.model.EmotionCatalog
import com.valoon4.emotionskompass.core.navigation.AppViewModel
import com.valoon4.emotionskompass.core.navigation.Destination
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class FeelingDiscoveryTest {
    private val catalog by lazy {
        EmotionCatalog.fromJson(
            File("src/main/assets/catalog/emotions.de.v2.json").readText()
        )
    }

    @Test fun guidanceNeverPretendsToInferAnEmotionWithoutInput() {
        assertTrue(FeelingDiscovery.suggestions(catalog, null, "", "Alle", false).isEmpty())
        assertEquals(289, FeelingDiscovery.suggestions(catalog, null, "", "Alle", true).size)
    }

    @Test fun allDirectionsResolveToCuratedExistingTerms() {
        assertEquals(7, FeelingDirection.entries.size)
        for (direction in FeelingDirection.entries) {
            assertTrue(direction.label.isNotBlank())
            assertTrue(direction.suggestions.size in 5..FeelingDiscovery.DEFAULT_LIMIT)
            assertEquals(direction.suggestions.size, direction.suggestions.toSet().size)
            assertTrue(direction.suggestions.all { catalog.find(it) != null })
            assertEquals(direction.suggestions, FeelingDiscovery.suggestions(
                catalog, direction, "", "Alle", false
            ).map { it.id })
        }
    }

    @Test fun guidedSuggestionsStillRespectTheFamilyFilter() {
        val matches = FeelingDiscovery.suggestions(
            catalog, FeelingDirection.ANGESPANNT, "", "Unsicherheit", false
        )
        assertTrue(matches.isNotEmpty())
        assertTrue(matches.all { "Unsicherheit" in it.families })
    }

    @Test fun explicitSearchOverridesSuggestionsButRemainsOffline() {
        val matches = FeelingDiscovery.suggestions(
            catalog, FeelingDirection.ANGENEHM, "Neid", "Alle", false
        )
        assertEquals("neid", matches.first().id)
        assertFalse(matches.any { it.id == "freude" })
    }

    @Test fun curatedContrastUsesActualEditorialPair() {
        val matches = FeelingDiscovery.contrasts(catalog, setOf("neid", "eifersucht"))
        assertTrue(matches.isNotEmpty())
        assertEquals(setOf("neid", "eifersucht"), setOf(matches.first().first, matches.first().second))
        assertTrue(matches.size <= 2)
    }

    @Test fun emptySelectionOffersNoComparisons() {
        assertTrue(FeelingDiscovery.contrasts(catalog, emptySet()).isEmpty())
    }

    @Test fun shortModeStartsAtNamingWithoutBodyStep() {
        val draft = CheckInDraft().withMode(CheckInMode.KURZ)
        assertEquals(CheckInStep.BENENNEN, draft.step)
        assertEquals(1 to 3, draft.progress)
        assertTrue(draft.isFirstStep)
        assertEquals(draft, draft.previous())
        assertEquals(CheckInStep.NACHSPUEREN, draft.next().step)
        assertEquals(3 to 3, draft.next().next().progress)
    }

    @Test fun deepModeRetainsAllFourSteps() {
        val draft = CheckInDraft()
        assertEquals(CheckInMode.VERTIEFT, draft.mode)
        assertEquals(CheckInStep.ANKOMMEN, draft.step)
        assertEquals(1 to 5, draft.progress)
        assertEquals(4 to 5, draft.next().next().next().progress)
    }

    @Test fun switchingModePreservesEmotionsAndIntensity() {
        val original = CheckInDraft().toggleEmotion("freude").withIntensity("freude", 0)
        val short = original.withMode(CheckInMode.KURZ)
        val deep = short.withMode(CheckInMode.VERTIEFT)
        assertEquals(0, deep.emotions["freude"])
        assertTrue(deep.emotions.containsKey("freude"))
        assertEquals(CheckInMode.VERTIEFT, deep.mode)
    }

    @Test fun quickBackDiscardsSensitiveDraftInViewModel() {
        val vm = AppViewModel(catalog)
        vm.openCheckin()
        vm.setCheckInMode(CheckInMode.KURZ)
        vm.toggleCheckInEmotion("angst")
        vm.setCheckInNote("Ein privater Satz")
        assertEquals(Destination.CHECKIN, vm.state.value.destination)
        vm.back()
        assertEquals(Destination.MAIN, vm.state.value.destination)
        assertEquals(CheckInDraft(), vm.checkIn.value)
    }
}
