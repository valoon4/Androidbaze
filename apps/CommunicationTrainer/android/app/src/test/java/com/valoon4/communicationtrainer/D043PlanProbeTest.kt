package com.valoon4.communicationtrainer

import com.valoon4.communicationtrainer.inference.ChatGptPlanProbe
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** All tests are offline. No OAuth credentials or API calls are involved. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class D043PlanProbeTest {
    @Test fun requestMustUsePublicResponsesPreviewContract() {
        val body = ChatGptPlanProbe.requestBody("gpt-6.1-sol")
        assertEquals("gpt-6.1-sol", body.getString("model"))
        assertFalse(body.getBoolean("store"))
        assertTrue(body.getBoolean("stream"))
        assertEquals(1, body.getJSONArray("input").length())
        val turn = body.getJSONArray("input").getJSONObject(0)
        assertEquals("user", turn.getString("role"))
        assertTrue(turn.getString("content").contains("Begruesse"))
        for (key in listOf(
            "temperature", "max_output_tokens", "background", "conversation",
            "previous_response_id", "metadata", "user", "truncation"
        )) assertFalse("Unsupported preview field: $key", body.has(key))
        assertEquals(4, body.length())
    }

    @Test fun catalogOnlyListsServerVisibleSafeSlugsAndKeepsOrder() {
        val catalog = JSONObject().put("models", JSONArray()
            .put(JSONObject().put("slug", "gpt-6.1-sol").put("display_name", "GPT 6.1 Sol").put("visibility", "list"))
            .put(JSONObject().put("slug", "hidden").put("visibility", "hidden"))
            .put(JSONObject().put("slug", "../../bad").put("visibility", "list"))
            .put(JSONObject().put("slug", "gpt-6.1-sol").put("display_name", "duplicate").put("visibility", "list"))
            .put(JSONObject().put("slug", "model-b").put("display_name", "Modell B").put("visibility", "list")))
        val models = ChatGptPlanProbe.parseModels(catalog)
        assertEquals(listOf("gpt-6.1-sol", "model-b"), models.map { it.slug })
        assertEquals("GPT 6.1 Sol", models.first().displayName)
    }

    @Test fun onlyCompletedEventIsSuccess() {
        val stream = ChatGptPlanProbe.PlanSseAccumulator()
        stream.accept("response.output_text.delta", JSONObject()
            .put("type", "response.output_text.delta").put("delta", "Hallo ").toString())
        assertFalse(stream.done)
        var premature = false
        try { stream.finalAnswer() } catch (_: Exception) { premature = true }
        assertTrue(premature)
        stream.accept("response.output_text.delta", JSONObject()
            .put("type", "response.output_text.delta").put("delta", "zusammen!").toString())
        stream.accept("response.completed", JSONObject()
            .put("type", "response.completed").toString())
        assertTrue(stream.done)
        assertEquals("Hallo zusammen!", stream.finalAnswer())
    }

    @Test fun limitsAndIncompleteStreamFailClosedWithoutSuccess() {
        val stream = ChatGptPlanProbe.PlanSseAccumulator()
        var rejected = false
        try {
            stream.accept("response.failed", JSONObject()
                .put("type", "response.failed")
                .put("response", JSONObject().put("error",
                    JSONObject().put("code", "subscription_sharing_usage_limit_exceeded")))
                .toString())
        } catch (e: ChatGptPlanProbe.PlanProbeFailure) {
            rejected = e.safeMessage.contains("Kontingent")
        }
        assertTrue(rejected)
        assertFalse(stream.done)
        val incomplete = ChatGptPlanProbe.PlanSseAccumulator()
        var incompleteRejected = false
        try {
            incomplete.accept("response.incomplete", JSONObject()
                .put("type", "response.incomplete").toString())
        } catch (_: ChatGptPlanProbe.PlanProbeFailure) {
            incompleteRejected = true
        }
        assertTrue(incompleteRejected)
        assertFalse(incomplete.done)
    }

    @Test fun sessionRejectsInvalidTokenWithoutPersistence() {
        ChatGptPlanProbe.onVerifiedSignIn("not-an-oauth-token", 3600L)
        assertFalse(ChatGptPlanProbe.authenticated.value)
        ChatGptPlanProbe.clearSession()
        assertFalse(ChatGptPlanProbe.authenticated.value)
    }
}
