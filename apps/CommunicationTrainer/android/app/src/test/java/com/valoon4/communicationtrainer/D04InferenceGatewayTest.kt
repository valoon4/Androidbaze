package com.valoon4.communicationtrainer

import com.valoon4.communicationtrainer.inference.DeterministicMockGateway
import com.valoon4.communicationtrainer.inference.InferenceError
import com.valoon4.communicationtrainer.inference.InferenceGateways
import com.valoon4.communicationtrainer.inference.InferenceMode
import com.valoon4.communicationtrainer.inference.InferenceOperation
import com.valoon4.communicationtrainer.inference.InferenceRequest
import com.valoon4.communicationtrainer.inference.InferenceRequestStatus
import com.valoon4.communicationtrainer.inference.InferenceResult
import com.valoon4.communicationtrainer.inference.UnavailableInferenceGateway
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class D04InferenceGatewayTest {
    private fun request(
        purpose: String = "local_test",
        model: String = "d03-deterministic-v1",
        text: String = "Hallo"
    ) = InferenceRequest(
        requestId = "request-1",
        conversationId = "conversation-1",
        branchId = "conversation-1:main",
        personaRef = "d03-testpartner-v1",
        scenarioRef = null,
        policyVersion = "d04-gateway-contract-v1",
        modelVersion = model,
        privacyPurpose = purpose,
        latestUserText = text
    )

    @Test
    fun mockWorksOnlyForLocalTesting() = runBlocking {
        val gateway = DeterministicMockGateway()
        val result = gateway.generateCharacterTurn(request())
        assertTrue(result is InferenceResult.Completed)
        result as InferenceResult.Completed
        assertEquals(InferenceMode.D03_MOCK, result.sourceMode)
        assertEquals("d03-deterministic-v1", result.modelVersion)
        assertTrue(result.text.isNotBlank())
        assertEquals(
            setOf(InferenceOperation.GENERATE_CHARACTER_TURN),
            gateway.supportedOperations
        )
        assertEquals(
            InferenceResult.Rejected(InferenceError.AUTHORIZATION_REQUIRED),
            gateway.generateCharacterTurn(request(purpose = "external_chat"))
        )
    }

    @Test
    fun coachAndReplayAreBlockedRatherThanInvented() = runBlocking {
        val gateway = DeterministicMockGateway()
        val expected = InferenceResult.Rejected(InferenceError.OPERATION_UNAVAILABLE)
        assertEquals(expected, gateway.generateCoachFeedback(request()))
        assertEquals(expected, gateway.generateReplay(request()))
        assertFalse(gateway.cancel("request-1"))
        assertEquals(InferenceRequestStatus.UNKNOWN, gateway.getRequestStatus("request-1"))
    }

    @Test
    fun localModeNeverSilentlyFallsBackToCloud() = runBlocking {
        val local = InferenceGateways.create(InferenceMode.LOCAL_ANDROID)
        assertTrue(local is UnavailableInferenceGateway)
        assertEquals(emptySet<InferenceOperation>(), local.supportedOperations)
        assertEquals(
            InferenceResult.Rejected(InferenceError.MODEL_NOT_INSTALLED),
            local.generateCharacterTurn(request())
        )
        assertEquals(InferenceRequestStatus.BLOCKED, local.getRequestStatus("request-1"))
    }

    @Test
    fun externalModesAreNotConnectedOrAuthorized() = runBlocking {
        val cloudModes = listOf(
            InferenceMode.CLOUD_BROKER,
            InferenceMode.SELF_HOSTED,
            InferenceMode.CHATGPT_PLAN
        )
        for (mode in cloudModes) {
            val gateway = InferenceGateways.create(mode)
            val expected = when (mode) {
                InferenceMode.CHATGPT_PLAN -> InferenceError.UNSUPPORTED_PLATFORM
                else -> InferenceError.BROKER_NOT_CONFIGURED
            }
            assertEquals(
                "No external network requests are possible from disabled gateways",
                InferenceResult.Rejected(expected),
                gateway.generateCharacterTurn(request())
            )
            assertEquals(
                InferenceResult.Rejected(expected),
                gateway.generateCoachFeedback(request())
            )
            assertEquals(
                InferenceResult.Rejected(expected),
                gateway.generateReplay(request())
            )
        }
    }

    @Test
    fun requestWithoutAnyRequiredMetadataIsRejected() = runBlocking {
        val gateway = DeterministicMockGateway()
        val requests = listOf(
            request().copy(requestId = ""),
            request().copy(conversationId = ""),
            request().copy(branchId = ""),
            request().copy(personaRef = ""),
            request().copy(policyVersion = ""),
            request().copy(modelVersion = ""),
            request().copy(privacyPurpose = ""),
            request().copy(latestUserText = "   ")
        )
        assertTrue(requests.all { !it.isValid() })
        for (value in requests) {
            assertEquals(
                InferenceResult.Rejected(InferenceError.INVALID_REQUEST),
                gateway.generateCharacterTurn(value)
            )
        }
    }

    @Test
    fun allCandidateModesStayDisabledExceptMock() {
        assertEquals(InferenceMode.D03_MOCK, InferenceGateways.create(InferenceMode.D03_MOCK).mode)
        assertTrue(
            InferenceMode.entries.filter { it != InferenceMode.D03_MOCK }
                .all { InferenceGateways.create(it).supportedOperations.isEmpty() }
        )
    }
}
