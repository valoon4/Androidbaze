package com.valoon4.emotionskompass

import com.valoon4.emotionskompass.core.checkin.CheckInDraft
import com.valoon4.emotionskompass.core.checkin.CheckInMode
import com.valoon4.emotionskompass.core.checkin.CheckInStep
import com.valoon4.emotionskompass.core.checkin.NextStepIdea
import com.valoon4.emotionskompass.core.checkin.PersonalNeed
import com.valoon4.emotionskompass.core.model.EmotionCatalog
import com.valoon4.emotionskompass.core.navigation.AppViewModel
import com.valoon4.emotionskompass.core.navigation.Destination
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class DeepReflectionTest {
    private val catalog by lazy {
        EmotionCatalog.fromJson(
            File("src/main/assets/catalog/emotions.de.v2.json").readText()
        )
    }

    @Test fun allFiveDeepStepsCanBeTraversedWithoutAnswering() {
        var draft = CheckInDraft()
        assertEquals(CheckInStep.ANKOMMEN, draft.step)
        assertEquals(1 to 5, draft.progress)
        draft = draft.next()
        assertEquals(CheckInStep.BENENNEN, draft.step)
        draft = draft.next()
        assertEquals(CheckInStep.NACHSPUEREN, draft.step)
        draft = draft.next()
        assertEquals(CheckInStep.VERSTEHEN, draft.step)
        assertEquals(4 to 5, draft.progress)
        draft = draft.next()
        assertEquals(CheckInStep.AUSKLANG, draft.step)
        assertEquals(5 to 5, draft.progress)
        assertEquals(CheckInStep.VERSTEHEN, draft.previous().step)
    }

    @Test fun shortModeNeverRequiresTheNewReflectionStep() {
        val draft = CheckInDraft().withMode(CheckInMode.KURZ)
        assertEquals(CheckInStep.BENENNEN, draft.step)
        assertEquals(CheckInStep.NACHSPUEREN, draft.next().step)
        assertEquals(CheckInStep.AUSKLANG, draft.next().next().step)
        assertEquals(CheckInStep.NACHSPUEREN, draft.next().next().previous().step)
        assertEquals(3 to 3, draft.next().next().progress)
    }

    @Test fun changingToShortModeFromReflectionGoesToConclusion() {
        var draft = CheckInDraft()
        repeat(3) { draft = draft.next() }
        val short = draft.withMode(CheckInMode.KURZ)
        assertEquals(CheckInStep.AUSKLANG, short.step)
        assertEquals(3 to 3, short.progress)
    }

    @Test fun defaultInputsAreEntirelyOptionalAndNeverInferred() {
        val draft = CheckInDraft()
        assertTrue(draft.needs.isEmpty())
        assertEquals("", draft.situation)
        assertEquals("", draft.interpretation)
        assertEquals("", draft.insight)
        assertNull(draft.nextStepIdea)
        assertEquals("", draft.nextStepNote)
        assertFalse(draft.hasPersonalInput)
        val selected = draft.toggleEmotion("freude")
        assertTrue(selected.needs.isEmpty())
        assertNull(selected.nextStepIdea)
        assertEquals("", selected.insight)
    }

    @Test fun choosingSeveralNeedsAndDeselectingEachIsPossible() {
        val selected = CheckInDraft()
            .toggleNeed(PersonalNeed.KLARHEIT)
            .toggleNeed(PersonalNeed.RUHE)
        assertEquals(setOf(PersonalNeed.KLARHEIT, PersonalNeed.RUHE), selected.needs)
        assertEquals(setOf(PersonalNeed.RUHE),
            selected.toggleNeed(PersonalNeed.KLARHEIT).needs)
        assertTrue(selected.hasPersonalInput)
    }

    @Test fun notKnowingIsExclusiveWithConcreteNeeds() {
        val unknown = CheckInDraft().toggleNeed(PersonalNeed.UNKLAR)
        assertEquals(setOf(PersonalNeed.UNKLAR), unknown.needs)
        val concrete = unknown.toggleNeed(PersonalNeed.SICHERHEIT)
        assertEquals(setOf(PersonalNeed.SICHERHEIT), concrete.needs)
        assertEquals(setOf(PersonalNeed.UNKLAR),
            concrete.toggleNeed(PersonalNeed.UNKLAR).needs)
    }

    @Test fun contextAndInsightFieldsHaveLengthLimits() {
        val draft = CheckInDraft()
            .withSituation("s".repeat(900))
            .withInterpretation("i".repeat(900))
            .withInsight("e".repeat(900))
            .withNextStepNote("n".repeat(900))
        assertEquals(CheckInDraft.MAX_CONTEXT_LENGTH, draft.situation.length)
        assertEquals(CheckInDraft.MAX_CONTEXT_LENGTH, draft.interpretation.length)
        assertEquals(CheckInDraft.MAX_INSIGHT_LENGTH, draft.insight.length)
        assertEquals(CheckInDraft.MAX_CONTEXT_LENGTH, draft.nextStepNote.length)
    }

    @Test fun situationAndPersonalInterpretationRemainSeparate() {
        val draft = CheckInDraft()
            .withSituation("Das Treffen wurde abgesagt.")
            .withInterpretation("Ich hatte auf Unterstützung gehofft.")
        assertEquals("Das Treffen wurde abgesagt.", draft.situation)
        assertEquals("Ich hatte auf Unterstützung gehofft.", draft.interpretation)
        assertNotEquals(draft.situation, draft.interpretation)
    }

    @Test fun noActionAndNoSelectionAreValidAndReversible() {
        val draft = CheckInDraft().withNextStepIdea(NextStepIdea.KEIN_SCHRITT)
        assertEquals(NextStepIdea.KEIN_SCHRITT, draft.nextStepIdea)
        assertNull(draft.withNextStepIdea(null).nextStepIdea)
        assertEquals("", draft.nextStepNote)
    }

    @Test fun selectedEmotionAndOptionalIntensitySurviveReflection() {
        val draft = CheckInDraft()
            .toggleEmotion("enttaeuschung")
            .withIntensity("enttaeuschung", 0)
            .toggleNeed(PersonalNeed.UNTERSTUETZUNG)
            .withInsight("Ich wünsche mir Rückhalt.")
        assertEquals(0, draft.emotions["enttaeuschung"])
        assertEquals(setOf(PersonalNeed.UNTERSTUETZUNG), draft.needs)
        assertEquals("Ich wünsche mir Rückhalt.", draft.insight)
    }

    @Test fun closingWipesAllReflectiveFieldsInViewModel() {
        val vm = AppViewModel(catalog)
        vm.openCheckin("enttaeuschung")
        vm.setCheckInSituation("Ein Ereignis")
        vm.setCheckInInterpretation("Meine Sicht")
        vm.toggleCheckInNeed(PersonalNeed.RESPEKT)
        vm.setCheckInInsight("Meine Erkenntnis")
        vm.setCheckInNextStep(NextStepIdea.GESPRAECH)
        vm.setCheckInNextStepNote("Mein eigener Schritt")
        assertTrue(vm.checkIn.value.hasPersonalInput)
        vm.closeCheckIn()
        assertEquals(Destination.MAIN, vm.state.value.destination)
        assertEquals(CheckInDraft(), vm.checkIn.value)
    }

    @Test fun freshStartAfterClosingHasNoPreviousInsights() {
        val vm = AppViewModel(catalog)
        vm.openCheckin()
        vm.setCheckInInsight("Private Einsicht")
        vm.closeCheckIn()
        vm.openCheckin()
        assertEquals("", vm.checkIn.value.insight)
        assertTrue(vm.checkIn.value.needs.isEmpty())
        assertNull(vm.checkIn.value.nextStepIdea)
    }

    @Test fun backFromReflectionKeepsEditingAndBackFromFirstStepWipes() {
        val vm = AppViewModel(catalog)
        vm.openCheckin()
        repeat(3) { vm.nextCheckInStep() }
        assertEquals(CheckInStep.VERSTEHEN, vm.checkIn.value.step)
        vm.setCheckInInsight("Bleibt bei Schrittwechsel")
        vm.back()
        assertEquals(CheckInStep.NACHSPUEREN, vm.checkIn.value.step)
        assertEquals("Bleibt bei Schrittwechsel", vm.checkIn.value.insight)
        vm.back()
        vm.back()
        vm.back()
        assertEquals(CheckInDraft(), vm.checkIn.value)
    }
}
