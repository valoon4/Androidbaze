package com.valoon4.emotionskompass

import com.valoon4.emotionskompass.core.checkin.BodyCue
import com.valoon4.emotionskompass.core.checkin.CheckInDraft
import com.valoon4.emotionskompass.core.checkin.CheckInStep
import com.valoon4.emotionskompass.core.checkin.LifeArea
import com.valoon4.emotionskompass.core.model.EmotionCatalog
import com.valoon4.emotionskompass.core.navigation.AppViewModel
import com.valoon4.emotionskompass.core.navigation.Destination
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class CheckInDraftTest {
    private val catalog by lazy {
        EmotionCatalog.fromJson(File("src/main/assets/catalog/emotions.de.v2.json").readText())
    }

    @Test fun freshCheckInContainsNoPersonalData() {
        assertEquals(CheckInStep.ANKOMMEN, CheckInDraft().step)
        assertEquals(0, CheckInDraft().selectedCount)
        assertTrue(CheckInDraft().bodyCues.isEmpty())
        assertNull(CheckInDraft().area)
        assertEquals("", CheckInDraft().note)
    }

    @Test fun multipleSimultaneousEmotionsAreSupported() {
        val draft = CheckInDraft().toggleEmotion("angst").toggleEmotion("vorfreude")
        assertEquals(2, draft.selectedCount)
        assertTrue(draft.emotions.containsKey("angst"))
        assertTrue(draft.emotions.containsKey("vorfreude"))
        assertNull(draft.emotions["angst"])
        assertNull(draft.emotions["vorfreude"])
    }

    @Test fun choosingEmotionTwiceDeselectsIt() {
        val draft = CheckInDraft().toggleEmotion("freude").toggleEmotion("freude")
        assertTrue(draft.emotions.isEmpty())
    }

    @Test fun zeroIntensityIsNotTheSameAsMissingIntensity() {
        val draft = CheckInDraft().toggleEmotion("trauer")
        assertTrue(draft.emotions.containsKey("trauer"))
        assertNull(draft.emotions["trauer"])
        val rated = draft.withIntensity("trauer", 0)
        assertEquals(0, rated.emotions["trauer"])
        val reset = rated.withIntensity("trauer", null)
        assertTrue(reset.emotions.containsKey("trauer"))
        assertNull(reset.emotions["trauer"])
    }

    @Test fun intensityCannotBeOutOfRange() {
        val draft = CheckInDraft().toggleEmotion("angst")
        assertThrows(IllegalArgumentException::class.java) {
            draft.withIntensity("angst", -1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            draft.withIntensity("angst", 11)
        }
        assertThrows(IllegalArgumentException::class.java) {
            draft.withIntensity("freude", 5)
        }
    }

    @Test fun allFourStepsCanBeCompletedWithoutSelectingAnything() {
        var draft = CheckInDraft()
        repeat(4) { draft = draft.next() }
        assertEquals(CheckInStep.AUSKLANG, draft.step)
        assertTrue(draft.emotions.isEmpty())
        assertEquals(CheckInStep.VERSTEHEN, draft.previous().step)
    }

    @Test fun selectedBodyCuesCanBeRemovedWithoutChangingEmotions() {
        val draft = CheckInDraft().toggleEmotion("freude")
            .toggleCue(BodyCue.WAERME)
            .toggleCue(BodyCue.ANSPANNUNG)
            .toggleCue(BodyCue.WAERME)
        assertEquals(setOf(BodyCue.ANSPANNUNG), draft.bodyCues)
        assertEquals(1, draft.selectedCount)
    }

    @Test fun optionalAreaAndNoteAreBoundedAndCanBeCleared() {
        val draft = CheckInDraft().withArea(LifeArea.BEZIEHUNGEN)
            .withNote("a".repeat(900))
        assertEquals(LifeArea.BEZIEHUNGEN, draft.area)
        assertEquals(CheckInDraft.MAX_NOTE_LENGTH, draft.note.length)
        assertNull(draft.withArea(null).area)
        assertEquals("", draft.withNote("").note)
    }

    @Test fun existingAtlasTermCanPrefillCheckIn() {
        val seeded = CheckInDraft.startWith("wehmut")
        assertTrue(seeded.emotions.containsKey("wehmut"))
        assertNull(CheckInDraft.startWith(null).emotions["wehmut"])
        assertEquals(1, seeded.selectedCount)
    }

    @Test fun viewModelClearsPersonalInformationOnClose() {
        val vm = AppViewModel(catalog)
        vm.openCheckin("wehmut")
        vm.setCheckInNote("Privater Gedanke")
        vm.toggleBodyCue(BodyCue.ENGE)
        vm.setCheckInIntensity("wehmut", 7)
        assertEquals(Destination.CHECKIN, vm.state.value.destination)
        assertEquals("Privater Gedanke", vm.checkIn.value.note)
        vm.closeCheckIn()
        assertEquals(Destination.MAIN, vm.state.value.destination)
        assertEquals(CheckInDraft(), vm.checkIn.value)
    }

    @Test fun androidBackWithinCheckInMovesOneStepThenDiscardsOnExit() {
        val vm = AppViewModel(catalog)
        vm.openCheckin()
        vm.nextCheckInStep()
        vm.setCheckInNote("Wird verworfen")
        vm.back()
        assertEquals(CheckInStep.ANKOMMEN, vm.checkIn.value.step)
        assertEquals(Destination.CHECKIN, vm.state.value.destination)
        vm.back()
        assertEquals(Destination.MAIN, vm.state.value.destination)
        assertEquals(CheckInDraft(), vm.checkIn.value)
    }

    @Test fun switchingToAnotherTabClearsTheDraft() {
        val vm = AppViewModel(catalog)
        vm.openCheckin("angst")
        vm.setCheckInNote("Privater Text")
        vm.selectSection(com.valoon4.emotionskompass.core.navigation.MainSection.ENTDECKEN)
        assertEquals(CheckInDraft(), vm.checkIn.value)
    }

    @Test fun unknownEmotionCannotBeAddedByViewModel() {
        val vm = AppViewModel(catalog)
        vm.openCheckin()
        vm.toggleCheckInEmotion("not_a_real_emotion")
        assertEquals(0, vm.checkIn.value.selectedCount)
    }
}
