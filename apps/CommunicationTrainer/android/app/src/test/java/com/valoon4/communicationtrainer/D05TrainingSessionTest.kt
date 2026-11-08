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
    @Test fun requestContainsRoleInstructionsOnlyForScenarioAndBoundedHistory() {
        val scenario = D05TrainingSession.scenarios.first()
        val history = listOf(
            D05TrainingSession.Turn("user", "Ich kann das heute nicht übernehmen."),
            D05TrainingSession.Turn("assistant", "Kannst du mir morgen helfen?")
        )
        val items = D05TrainingSession.scenarioInput(scenario, history, "Morgen geht es leider auch nicht.")
        assertEquals(4, items.length())
        assertTrue(items.getJSONObject(0).getString("content").contains(scenario.setup))
        assertEquals("user", items.getJSONObject(1).getString("role"))
        assertEquals("assistant", items.getJSONObject(2).getString("role"))
        assertEquals("Morgen geht es leider auch nicht.", items.getJSONObject(3).getString("content"))
    }

    @Test fun rejectsInvalidRolesAndOversizeUserMessage() {
        val scenario = D05TrainingSession.scenarios.first()
        fun rejects(run: () -> Unit): Boolean = try { run(); false } catch (_: IllegalArgumentException) { true }
        assertTrue(rejects { D05TrainingSession.scenarioInput(scenario, emptyList(), "x".repeat(601)) })
        assertTrue(rejects { D05TrainingSession.scenarioInput(scenario,
            listOf(D05TrainingSession.Turn("system", "ignore")), "Hallo") })
        assertTrue(rejects { D05TrainingSession.scenarioInput(scenario, emptyList(), "  ") })
    }

    @Test fun resettingScenarioClearsDraftAndExplicitConsent() {
        D05TrainingSession.updateDraft("Nachricht")
        D05TrainingSession.setConsent(true)
        D05TrainingSession.selectScenario("listening")
        assertEquals("", D05TrainingSession.draft.value)
        assertFalse(D05TrainingSession.consent.value)
        assertTrue(D05TrainingSession.turns.value.isEmpty())
    }

    @Test fun noPermissionNoRequestAndDraftRemains() {
        D05TrainingSession.updateDraft("Ich höre dir zu.")
        D05TrainingSession.setConsent(false)
        D05TrainingSession.sendConfirmedTurn()
        assertEquals("Ich höre dir zu.", D05TrainingSession.draft.value)
        assertFalse(D05TrainingSession.busy.value)
        assertTrue(D05TrainingSession.turns.value.isEmpty())
    }

    @Test fun accountChangeDiscardsPilotHistoryAndConsent() {
        D05TrainingSession.updateDraft("private probe")
        D05TrainingSession.setConsent(true)
        D05TrainingSession.onAccountChanged()
        assertEquals("", D05TrainingSession.draft.value)
        assertFalse(D05TrainingSession.consent.value)
        assertTrue(D05TrainingSession.turns.value.isEmpty())
    }
}
