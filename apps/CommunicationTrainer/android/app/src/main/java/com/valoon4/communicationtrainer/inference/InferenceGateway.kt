package com.valoon4.communicationtrainer.inference

import com.valoon4.communicationtrainer.data.MockConversationEngine

/**
 * D04's transport-independent model boundary. No real inference is performed by
 * this contract. Model/locale/privacy/version metadata must accompany each turn.
 *
 * A mode cannot silently route to another mode. In particular, unavailability of
 * an on-device model must NEVER send a local conversation to a cloud provider.
 */
enum class InferenceMode {
    D03_MOCK, LOCAL_ANDROID, CLOUD_BROKER, SELF_HOSTED, CHATGPT_PLAN
}

enum class InferenceOperation {
    GENERATE_CHARACTER_TURN, GENERATE_COACH_FEEDBACK, GENERATE_REPLAY
}

enum class InferenceError {
    INVALID_REQUEST,
    OPERATION_UNAVAILABLE,
    MODEL_NOT_INSTALLED,
    DEVICE_INCOMPATIBLE,
    QUALITY_NOT_APPROVED,
    BROKER_NOT_CONFIGURED,
    AUTHORIZATION_REQUIRED,
    UNSUPPORTED_PLATFORM,
    NETWORK_UNAVAILABLE
}

data class InferenceTurn(val id: String, val role: String, val text: String)

data class InferenceRequest(
    val requestId: String,
    val conversationId: String,
    val branchId: String,
    val personaRef: String,
    val scenarioRef: String?,
    val policyVersion: String,
    val modelVersion: String,
    val privacyPurpose: String,
    val latestUserText: String,
    val history: List<InferenceTurn> = emptyList()
) {
    fun isValid(): Boolean =
        requestId.isNotBlank() &&
            conversationId.isNotBlank() &&
            branchId.isNotBlank() &&
            personaRef.isNotBlank() &&
            policyVersion.isNotBlank() &&
            modelVersion.isNotBlank() &&
            privacyPurpose.isNotBlank() &&
            latestUserText.isNotBlank() &&
            history.all { it.id.isNotBlank() && it.role in setOf("user", "assistant") }
}

sealed interface InferenceResult {
    data class Completed(val text: String, val modelVersion: String, val sourceMode: InferenceMode) :
        InferenceResult
    data class Rejected(val code: InferenceError) : InferenceResult
}

enum class InferenceRequestStatus { UNKNOWN, BLOCKED, COMPLETED }

interface InferenceGateway {
    val mode: InferenceMode
    val supportedOperations: Set<InferenceOperation>

    suspend fun generateCharacterTurn(request: InferenceRequest): InferenceResult
    suspend fun generateCoachFeedback(request: InferenceRequest): InferenceResult
    suspend fun generateReplay(request: InferenceRequest): InferenceResult

    /** Cancellation is optional; false means nothing was cancelled. */
    suspend fun cancel(requestId: String): Boolean
    suspend fun getRequestStatus(requestId: String): InferenceRequestStatus
}

/**
 * Existing D03 fixture is retained as the only enabled inference mode.
 * This is an example generator, not a language model or a realistic persona.
 * Its text is intentionally recognizable and must not be scored as LLM quality.
 */
class DeterministicMockGateway : InferenceGateway {
    override val mode = InferenceMode.D03_MOCK
    override val supportedOperations = setOf(InferenceOperation.GENERATE_CHARACTER_TURN)

    override suspend fun generateCharacterTurn(request: InferenceRequest): InferenceResult {
        if (!request.isValid()) return InferenceResult.Rejected(InferenceError.INVALID_REQUEST)
        if (request.privacyPurpose != "local_test") {
            return InferenceResult.Rejected(InferenceError.AUTHORIZATION_REQUIRED)
        }
        if (request.modelVersion != "d03-deterministic-v1") {
            return InferenceResult.Rejected(InferenceError.QUALITY_NOT_APPROVED)
        }
        return InferenceResult.Completed(
            text = MockConversationEngine.answer(request.latestUserText),
            modelVersion = request.modelVersion,
            sourceMode = mode
        )
    }

    override suspend fun generateCoachFeedback(request: InferenceRequest) =
        InferenceResult.Rejected(InferenceError.OPERATION_UNAVAILABLE)

    override suspend fun generateReplay(request: InferenceRequest) =
        InferenceResult.Rejected(InferenceError.OPERATION_UNAVAILABLE)

    override suspend fun cancel(requestId: String) = false
    override suspend fun getRequestStatus(requestId: String) = InferenceRequestStatus.UNKNOWN
}

/**
 * Disabled candidate modes are explicit types; nothing here opens a socket,
 * accesses a model file, collects tokens, or requests external permissions.
 */
class UnavailableInferenceGateway(
    override val mode: InferenceMode
) : InferenceGateway {
    init {
        require(mode != InferenceMode.D03_MOCK)
    }

    override val supportedOperations: Set<InferenceOperation> = emptySet()

    private fun rejection(request: InferenceRequest): InferenceResult {
        if (!request.isValid()) return InferenceResult.Rejected(InferenceError.INVALID_REQUEST)
        return InferenceResult.Rejected(
            when (mode) {
                InferenceMode.LOCAL_ANDROID -> InferenceError.MODEL_NOT_INSTALLED
                InferenceMode.CLOUD_BROKER, InferenceMode.SELF_HOSTED -> InferenceError.BROKER_NOT_CONFIGURED
                InferenceMode.CHATGPT_PLAN -> InferenceError.UNSUPPORTED_PLATFORM
                InferenceMode.D03_MOCK -> error("D03 mock is never an unavailable candidate")
            }
        )
    }

    override suspend fun generateCharacterTurn(request: InferenceRequest) = rejection(request)
    override suspend fun generateCoachFeedback(request: InferenceRequest) = rejection(request)
    override suspend fun generateReplay(request: InferenceRequest) = rejection(request)
    override suspend fun cancel(requestId: String) = false
    override suspend fun getRequestStatus(requestId: String) = InferenceRequestStatus.BLOCKED
}

object InferenceGateways {
    fun create(mode: InferenceMode): InferenceGateway = when (mode) {
        InferenceMode.D03_MOCK -> DeterministicMockGateway()
        else -> UnavailableInferenceGateway(mode)
    }
}
