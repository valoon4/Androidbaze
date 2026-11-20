package com.valoon4.communicationtrainer

import com.valoon4.communicationtrainer.inference.D05Scenario
import com.valoon4.communicationtrainer.inference.D05SseAccumulator
import com.valoon4.communicationtrainer.inference.D05TrainingProtocol
import com.valoon4.communicationtrainer.inference.D05Transport
import com.valoon4.communicationtrainer.inference.D05Turn
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** No real credentials, database, internet calls or personal messages. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class D050OnlineTrainingTest {
    @Test fun firstTurnOnlyContainsExplicitNewExerciseText() {
        val body = D05TrainingProtocol.requestBody(
            "model-alpha", D05Scenario.WORKLOAD, emptyList(), "Heute schaffe ich das leider nicht."
        )
        assertEquals("model-alpha", body.getString("model"))
        assertFalse(body.getBoolean("store"))
        assertTrue(body.getBoolean("stream"))
        val input = body.getJSONArray("input")
        assertEquals(1, input.length())
        assertEquals("user", input.getJSONObject(0).getString("role"))
        assertEquals("Heute schaffe ich das leider nicht.", input.getJSONObject(0).getString("content"))
        assertTrue(body.getString("instructions").contains("fiktive Kollegin"))
        for (field in listOf("previous_response_id", "conversation", "background",
            "max_output_tokens", "temperature", "metadata", "user")) {
            assertFalse("Forbidden preview field: $field", body.has(field))
        }
        assertEquals(5, body.length())
    }

    @Test fun historyLimitedToThisExerciseAndOrderedAsUserAssistantPairs() {
        val history = listOf(
            D05Turn("user", "Ich kann erst morgen."),
            D05Turn("assistant", "Das ist schade, aber okay.")
        )
        val body = D05TrainingProtocol.requestBody(
            "model-alpha", D05Scenario.WORKLOAD, history, "Ich verstehe den Druck."
        )
        val input = body.getJSONArray("input")
        assertEquals(3, input.length())
        assertEquals("Ich kann erst morgen.", input.getJSONObject(0).getString("content"))
        assertEquals("assistant", input.getJSONObject(1).getString("role"))
        assertEquals("Ich verstehe den Druck.", input.getJSONObject(2).getString("content"))
    }

    @Test fun invalidOrOversizedInputNeverReachesTransport() {
        fun rejects(block: () -> Unit): Boolean =
            try { block(); false } catch (_: IllegalArgumentException) { true }
        assertFalse(D05TrainingProtocol.validUserMessage("  "))
        assertFalse(D05TrainingProtocol.validUserMessage("x".repeat(501)))
        assertTrue(D05TrainingProtocol.validUserMessage("Bitte nicht heute."))
        assertTrue(rejects {
            D05TrainingProtocol.requestBody("model-alpha", D05Scenario.CANCELLATION,
                emptyList(), "a".repeat(501))
        })
        assertTrue(rejects {
            D05TrainingProtocol.requestBody("../../invalid", D05Scenario.CANCELLATION,
                emptyList(), "Hallo")
        })
        assertTrue(rejects {
            D05TrainingProtocol.requestBody("model-alpha", D05Scenario.CANCELLATION,
                listOf(D05Turn("assistant", "bad"), D05Turn("user", "wrong")), "Hallo")
        })
        assertTrue(rejects {
            val tooMany = List(10) { i -> D05Turn(if (i % 2 == 0) "user" else "assistant", "hi") }
            D05TrainingProtocol.requestBody("model-alpha", D05Scenario.CANCELLATION, tooMany, "Hi")
        })
    }

    @Test fun successRequiresTerminalEventAndBoundedText() {
        val acc = D05SseAccumulator()
        acc.accept("response.output_text.delta", JSONObject()
            .put("type", "response.output_text.delta")
            .put("delta", "Das ist okay.").toString())
        assertFalse(acc.completed)
        var rejected = false
        try { acc.finalAnswer() } catch (_: Exception) { rejected = true }
        assertTrue(rejected)
        acc.accept("response.completed", JSONObject().put("type", "response.completed").toString())
        assertEquals("Das ist okay.", acc.finalAnswer())
        val big = D05SseAccumulator()
        var sizeRejected = false
        try {
            big.accept("response.output_text.delta", JSONObject()
                .put("type", "response.output_text.delta")
                .put("delta", "a".repeat(D05TrainingProtocol.MAX_BOT_CHARS + 1)).toString())
        } catch (_: D05Transport.Failure) { sizeRejected = true }
        assertTrue(sizeRejected)
    }

    @Test fun serverFailureAndIncompleteStopTheExercise() {
        for (type in listOf("response.failed", "response.incomplete", "error")) {
            val acc = D05SseAccumulator()
            var rejected = false
            try { acc.accept(type, JSONObject().put("type", type).toString()) }
            catch (_: D05Transport.Failure) { rejected = true }
            assertTrue(type, rejected)
            assertFalse(acc.completed)
        }
    }
}
