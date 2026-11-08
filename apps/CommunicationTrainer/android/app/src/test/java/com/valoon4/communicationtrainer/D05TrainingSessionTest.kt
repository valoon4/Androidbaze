package com.valoon4.communicationtrainer

import com.valoon4.communicationtrainer.inference.D05TrainingSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class D05TrainingSessionTest {
    private val boundary get() = D05TrainingSession.scenarios.first { it.id == "boundary" }
    private val listening get() = D05TrainingSession.scenarios.first { it.id == "listening" }

    @Test fun boundaryScenarioBeginsWithAnActualRequestAtNoInferenceCost() {
        D05TrainingSession.selectScenario("boundary")
        val history = D05TrainingSession.turns.value
        assertEquals(1, history.size)
        assertTrue(history.single().isScriptedOpening)
        assertEquals("assistant", history.single().role)
        assertTrue(history.single().text.contains("könntest du"))
        assertEquals(0, history.count { it.role == "user" })
        assertFalse(D05TrainingSession.busy.value)
    }

    @Test fun listeningScenarioStartsWithEmotionalStatementNotAPromptToChooseATopic() {
        D05TrainingSession.selectScenario("listening")
        val history = D05TrainingSession.turns.value
        assertEquals(1, history.size)
        assertEquals(listening.opening, history.single().text)
        assertTrue(history.single().isScriptedOpening)
        assertTrue(history.single().text.contains("enttäuscht"))
    }

    @Test fun requestIncludesTheScriptedOpeningAndAlternatingRealTurns() {
        val history = listOf(
            D05TrainingSession.scriptedOpening(boundary),
            D05TrainingSession.Turn("user", "Das schaffe ich heute nicht."),
            D05TrainingSession.Turn("assistant", "Kannst du es morgen erledigen?")
        )
        val input = D05TrainingSession.scenarioInput(boundary, history, "Morgen geht es auch nicht.")
        assertEquals(5, input.length())
        assertTrue(input.getJSONObject(0).getString("content").contains(boundary.setup))
        assertEquals("assistant", input.getJSONObject(1).getString("role"))
        assertEquals(boundary.opening, input.getJSONObject(1).getString("content"))
        assertEquals("user", input.getJSONObject(2).getString("role"))
        assertEquals("assistant", input.getJSONObject(3).getString("role"))
        assertEquals("Morgen geht es auch nicht.", input.getJSONObject(4).getString("content"))
        assertTrue(D05TrainingSession.validHistory(boundary, history))
    }

    @Test fun rejectsMissingOpeningWrongScenarioAndBrokenRoleOrdering() {
        fun rejects(run: () -> Unit): Boolean =
            try { run(); false } catch (_: IllegalArgumentException) { true }
        assertTrue(rejects { D05TrainingSession.scenarioInput(boundary, emptyList(), "Hallo") })
        assertTrue(rejects { D05TrainingSession.scenarioInput(boundary,
            listOf(D05TrainingSession.Turn("user", "Hallo")), "Nein") })
        assertTrue(rejects { D05TrainingSession.scenarioInput(boundary,
            listOf(D05TrainingSession.scriptedOpening(listening)), "Nein") })
        assertTrue(rejects { D05TrainingSession.scenarioInput(boundary,
            listOf(D05TrainingSession.scriptedOpening(boundary),
                D05TrainingSession.Turn("assistant", "Falsch"),
                D05TrainingSession.Turn("user", "Falsch")), "Nein") })
        assertTrue(rejects { D05TrainingSession.scenarioInput(boundary,
            listOf(D05TrainingSession.scriptedOpening(boundary)), "x".repeat(601)) })
        assertTrue(rejects { D05TrainingSession.scenarioInput(boundary,
            listOf(D05TrainingSession.scriptedOpening(boundary)), "  ") })
    }

    @Test fun resettingScenarioClearsDraftConsentAndOldPartnerContext() {
        D05TrainingSession.selectScenario("boundary")
        D05TrainingSession.updateDraft("Nachricht")
        D05TrainingSession.setConsent(true)
        D05TrainingSession.selectScenario("listening")
        assertEquals("", D05TrainingSession.draft.value)
        assertFalse(D05TrainingSession.consent.value)
        assertEquals(listOf(D05TrainingSession.scriptedOpening(listening)), D05TrainingSession.turns.value)
    }

    @Test fun noPermissionNoRequestAndDraftRemains() {
        D05TrainingSession.selectScenario("boundary")
        D05TrainingSession.updateDraft("Ich höre dir zu.")
        D05TrainingSession.setConsent(false)
        D05TrainingSession.sendConfirmedTurn()
        assertEquals("Ich höre dir zu.", D05TrainingSession.draft.value)
        assertFalse(D05TrainingSession.busy.value)
        assertEquals(listOf(D05TrainingSession.scriptedOpening(boundary)), D05TrainingSession.turns.value)
    }

    @Test fun accountChangeDiscardsPilotHistoryAndConsent() {
        D05TrainingSession.selectScenario("boundary")
        D05TrainingSession.updateDraft("private probe")
        D05TrainingSession.setConsent(true)
        D05TrainingSession.onAccountChanged()
        assertEquals("", D05TrainingSession.draft.value)
        assertFalse(D05TrainingSession.consent.value)
        assertTrue(D05TrainingSession.turns.value.isEmpty())
    }
}
