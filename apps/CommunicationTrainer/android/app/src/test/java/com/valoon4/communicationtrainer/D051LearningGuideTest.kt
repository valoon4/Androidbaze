package com.valoon4.communicationtrainer

import com.valoon4.communicationtrainer.inference.D051LearningGuide
import com.valoon4.communicationtrainer.inference.D051LocalReflection
import com.valoon4.communicationtrainer.inference.D05TrainingSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** No account, network connection or model calls: D05.1.0 must be offline. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class D051LearningGuideTest {

    @Test fun eachScenarioHasAConcreteDistinctLearningGoalAndSelfCheck() {
        val b = D051LearningGuide.objectiveFor("boundary")
        val l = D051LearningGuide.objectiveFor("listening")
        assertTrue(b.headline.contains("Grenze"))
        assertTrue(l.headline.contains("Gefühle"))
        assertEquals(3, b.observationChoices.size)
        assertEquals(3, l.observationChoices.size)
        assertTrue(b.observationChoices.any { it.contains("Nein") })
        assertTrue(l.observationChoices.any { it.contains("offene Frage") })
        assertTrue(b.reflectionPrompt.isNotBlank())
        assertTrue(l.reflectionPrompt.isNotBlank())
    }

    @Test fun difficultyIsWrittenIntoRoleInstructionsAndChangesWithLevel() {
        val scenario = D05TrainingSession.scenarios.first { it.id == "boundary" }
        val opening = listOf(D05TrainingSession.scriptedOpening(scenario))
        for (level in D051LearningGuide.Level.entries) {
            D051LearningGuide.selectLevel(level)
            val prompt = D05TrainingSession.scenarioInput(scenario, opening,
                "Ich kann das leider nicht übernehmen.").getJSONObject(0).getString("content")
            assertTrue(prompt.contains("Schwierigkeitsgrad: ${level.label}"))
            assertTrue(prompt.contains(D051LearningGuide.instructionFor(scenario.id, level)))
        }
        D051LearningGuide.selectLevel(D051LearningGuide.Level.DAILY)
    }

    @Test fun challengeAsksOnceButMustRespectRepeatedClearBoundary() {
        val scenario = D05TrainingSession.scenarios.first { it.id == "boundary" }
        val opening = D05TrainingSession.scriptedOpening(scenario)
        D051LearningGuide.selectLevel(D051LearningGuide.Level.CHALLENGE)
        val first = D05TrainingSession.scenarioInput(
            scenario, listOf(opening), "Ich habe keine Kapazitäten, tut mir leid."
        ).getJSONObject(0).getString("content")
        assertTrue(first.contains("genau EINE"))
        assertTrue(first.contains("freiwillige"))
        assertTrue(first.contains("kein") || first.contains("Keine"))
        val followUp = listOf(
            opening,
            D05TrainingSession.Turn("user", "Ich habe keine Kapazitäten."),
            D05TrainingSession.Turn("assistant", "Gibt es jemanden, an den ich mich wenden kann?")
        )
        val second = D05TrainingSession.scenarioInput(
            scenario, followUp, "Auch das kann ich heute nicht. Bitte frag die Teamleitung."
        ).getJSONObject(0).getString("content")
        assertTrue(second.contains("KEINE weitere Bitte"))
        assertTrue(second.contains("akzeptiere sie"))
        assertFalse(second.contains("Dies ist die erste Antwort"))
        D051LearningGuide.selectLevel(D051LearningGuide.Level.DAILY)
    }

    @Test fun entryLevelNeverDemandsMoreWorkAfterFirstNo() {
        val cue = D051LearningGuide.responseCue(
            "boundary", D051LearningGuide.Level.START, 0
        )
        assertTrue(cue.contains("ohne Nachverhandlung"))
        val challengeCue = D051LearningGuide.responseCue(
            "boundary", D051LearningGuide.Level.CHALLENGE, 0
        )
        assertTrue(challengeCue.contains("kleinere"))
    }

    @Test fun reflectionIdeasAreSpecificOptionalAndOnlyOffline() {
        val b = D051LearningGuide.reflectionIdeasFor("boundary")
        val l = D051LearningGuide.reflectionIdeasFor("listening")
        assertEquals(3, b.size)
        assertEquals(3, l.size)
        assertTrue(b.any { it.contains("beim Ablehnen") })
        assertTrue(l.any { it.contains("Gefühl") })
        assertFalse(b == l)
        assertTrue(b.all { it.endsWith("?") })
        assertTrue(l.all { it.endsWith("?") })
    }

    @Test fun changingDifficultyClearsDraftAndConsentBeforeAnotherRequest() {
        D051LearningGuide.selectLevel(D051LearningGuide.Level.DAILY)
        D05TrainingSession.selectScenario("boundary")
        D05TrainingSession.updateDraft("Nein, heute nicht.")
        D05TrainingSession.setConsent(true)
        D051LearningGuide.selectLevel(D051LearningGuide.Level.CHALLENGE)
        assertEquals("", D05TrainingSession.draft.value)
        assertFalse(D05TrainingSession.consent.value)
        assertEquals(1, D05TrainingSession.turns.value.size)
        assertTrue(D05TrainingSession.turns.value.single().isScriptedOpening)
        D051LearningGuide.selectLevel(D051LearningGuide.Level.DAILY)
    }

    @Test fun reflectionIsOptionalLocalAndNotSentToTheModel() {
        D05TrainingSession.selectScenario("listening")
        D051LocalReflection.reset()
        D051LocalReflection.toggle(0, "listening", true)
        D051LocalReflection.updateNote("Das Gefühl hatte ich vorher nicht erkannt.")
        assertFalse(D051LocalReflection.confirm()) // Not until one real user turn has completed.

        val scenario = D05TrainingSession.scenarios.first { it.id == "listening" }
        val opener = D05TrainingSession.scriptedOpening(scenario)
        D05TrainingSession.turns.value = listOf(
            opener,
            D05TrainingSession.Turn("user", "Das klingt enttäuschend."),
            D05TrainingSession.Turn("assistant", "Ja, ich war traurig.")
        )
        assertTrue(D051LocalReflection.confirm())
        assertTrue(D051LocalReflection.acknowledged.value)
        val payload = D05TrainingSession.scenarioInput(scenario, D05TrainingSession.turns.value,
            "Was hättest du gebraucht?").toString()
        assertFalse(payload.contains("Das Gefühl hatte ich vorher nicht erkannt."))
        D05TrainingSession.clearConversation()
        assertEquals("", D051LocalReflection.note.value)
        assertFalse(D051LocalReflection.acknowledged.value)
        assertTrue(D051LocalReflection.selectedObservations.value.isEmpty())
    }

    @Test fun reflectionRejectsOversizedNotesAndInvalidObservations() {
        D05TrainingSession.selectScenario("boundary")
        D051LocalReflection.reset()
        D051LocalReflection.updateNote("x".repeat(401))
        assertEquals("", D051LocalReflection.note.value)
        D051LocalReflection.toggle(99, "boundary", true)
        assertTrue(D051LocalReflection.selectedObservations.value.isEmpty())
        D051LocalReflection.toggle(1, "boundary", true)
        assertEquals(setOf(1), D051LocalReflection.selectedObservations.value)
        D05TrainingSession.onAccountChanged()
        assertTrue(D051LocalReflection.selectedObservations.value.isEmpty())
        assertEquals("", D051LocalReflection.note.value)
    }
}
