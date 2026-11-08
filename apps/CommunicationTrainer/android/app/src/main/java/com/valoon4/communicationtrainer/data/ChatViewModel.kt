package com.valoon4.communicationtrainer.data

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.valoon4.communicationtrainer.inference.InferenceGateways
import com.valoon4.communicationtrainer.inference.InferenceMode
import com.valoon4.communicationtrainer.inference.InferenceRequest
import com.valoon4.communicationtrainer.inference.InferenceResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val conversations: List<ConversationEntity> = emptyList(),
    val selectedConversationId: String? = null,
    val draftText: String = "",
    val draftRevision: Long = 0,
    val persistedRevision: Long = 0,
    val isLoadingDraft: Boolean = false,
    val isBusy: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val messages: List<MessageEntity> = emptyList(),
    val isSending: Boolean = false,
    val respondingMessageId: String? = null,
    val failNextMockReply: Boolean = false,
    val error: String? = null
) {
    val isDraftSaved: Boolean get() = !isLoadingDraft && draftRevision == persistedRevision
}

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {
    // Only the existing local D03 fixture is allowed. Switching to another
    // mode requires future explicit product, permission, and security gates.
    private val inferenceGateway = InferenceGateways.create(InferenceMode.D03_MOCK)
    private val mutableState = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = mutableState
    private var loadJob: Job? = null
    private var messageJob: Job? = null
    private val processingReplies = mutableSetOf<String>()
    // Latest in-flight text remains visible when the user navigates away and back
    // before a database commit. It is not counted as durable until SQLite confirms.
    private data class PendingDraft(val text: String, val revision: Long)
    private val pendingDrafts = mutableMapOf<String, PendingDraft>()

    init {
        viewModelScope.launch {
            repository.conversations.collect { items ->
                mutableState.update { state -> state.copy(conversations = items) }
            }
        }
        viewModelScope.launch {
            var restoredOnce = false
            repository.preferences.settings.collect { prefs ->
                mutableState.update { state -> state.copy(themeMode = prefs.themeMode) }
                if (!restoredOnce) {
                    restoredOnce = true
                    prefs.selectedConversationId?.let { id ->
                        // Stale IDs are ignored, including after a deleted conversation.
                        if (repository.getConversation(id) != null) {
                            openConversation(id, persistSelection = false)
                        } else {
                            repository.preferences.selectConversation(null)
                        }
                    }
                }
            }
        }
    }

    fun createConversation() {
        if (mutableState.value.isBusy) return
        mutableState.update { it.copy(isBusy = true, error = null) }
        viewModelScope.launch {
            try {
                val id = repository.newConversation()
                openConversation(id, persistSelection = true)
            } catch (error: Exception) {
                mutableState.update { it.copy(error = "Gespräch konnte nicht angelegt werden.") }
            } finally {
                mutableState.update { it.copy(isBusy = false) }
            }
        }
    }

    fun openConversation(id: String, persistSelection: Boolean = true) {
        loadJob?.cancel()
        messageJob?.cancel()
        mutableState.update {
            it.copy(
                messages = emptyList(),
                selectedConversationId = id,
                draftText = "",
                draftRevision = 0,
                persistedRevision = 0,
                isLoadingDraft = true,
                error = null
            )
        }
        loadJob = viewModelScope.launch {
            try {
                val existing = repository.getConversation(id)
                if (existing == null) {
                    mutableState.update {
                        if (it.selectedConversationId == id) it.copy(selectedConversationId = null, isLoadingDraft = false)
                        else it
                    }
                    repository.preferences.selectConversation(null)
                    return@launch
                }
                val draft = repository.getDraft(id)
                val storedMessages = repository.messages(id).first()
                val confirmedRevision = draft?.editRevision ?: 0L
                val pending = pendingDrafts[id]
                val usePending = pending != null && pending.revision > confirmedRevision
                if (!usePending) pendingDrafts.remove(id)
                mutableState.update {
                    if (it.selectedConversationId == id && it.isLoadingDraft) it.copy(
                        messages = storedMessages,
                        draftText = if (usePending) pending!!.text else draft?.text.orEmpty(),
                        draftRevision = if (usePending) pending!!.revision else confirmedRevision,
                        persistedRevision = confirmedRevision,
                        isLoadingDraft = false
                    ) else it
                }
                messageJob = viewModelScope.launch {
                    repository.messages(id).collect { items ->
                        mutableState.update { state ->
                            if (state.selectedConversationId == id) state.copy(messages = items)
                            else state
                        }
                    }
                }
                if (persistSelection) repository.preferences.selectConversation(id)
            } catch (error: Exception) {
                mutableState.update {
                    if (it.selectedConversationId == id) it.copy(
                        isLoadingDraft = false,
                        error = "Entwurf konnte nicht geladen werden."
                    ) else it
                }
            }
        }
    }

    /**
     * Keystrokes are queued immediately (no debounce), with monotonic revision checks in Room.
     * "Gespeichert" is only shown after the corresponding SQLite transaction has succeeded.
     * A sudden power loss before that acknowledgement can still lose the last input.
     */
    fun editDraft(text: String) {
        val before = mutableState.value
        val id = before.selectedConversationId ?: return
        if (before.isLoadingDraft || before.isBusy || before.isSending) return
        val revision = before.draftRevision + 1L
        pendingDrafts[id] = PendingDraft(text, revision)
        mutableState.update { current ->
            if (current.selectedConversationId == id) current.copy(
                draftText = text,
                draftRevision = revision,
                error = null
            ) else current
        }
        viewModelScope.launch {
            try {
                repository.saveDraft(id, text, revision)
                if (pendingDrafts[id]?.revision == revision) pendingDrafts.remove(id)
                mutableState.update { state ->
                    if (state.selectedConversationId == id) state.copy(
                        persistedRevision = maxOf(state.persistedRevision, revision)
                    ) else state
                }
            } catch (error: Exception) {
                mutableState.update { state ->
                    if (state.selectedConversationId == id) state.copy(
                        error = "Speichern fehlgeschlagen. Bitte Text kopieren, bevor du die App schließt."
                    ) else state
                }
            }
        }
    }

    fun deleteConversation() {
        val id = mutableState.value.selectedConversationId ?: return
        if (mutableState.value.isBusy) return
        mutableState.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            try {
                repository.deleteConversation(id)
                pendingDrafts.remove(id)
                repository.preferences.selectConversation(null)
                loadJob?.cancel()
                messageJob?.cancel()
                mutableState.update { state ->
                    if (state.selectedConversationId == id) state.copy(
                        messages = emptyList(),
                        selectedConversationId = null,
                        draftText = "",
                        draftRevision = 0,
                        persistedRevision = 0,
                        isLoadingDraft = false,
                        error = null
                    ) else state
                }
            } catch (error: Exception) {
                mutableState.update { it.copy(error = "Löschen fehlgeschlagen.") }
            } finally {
                mutableState.update { it.copy(isBusy = false) }
            }
        }
    }

    fun closeConversation() {
        loadJob?.cancel()
        messageJob?.cancel()
        mutableState.update {
            it.copy(selectedConversationId = null, messages = emptyList(), isLoadingDraft = false, error = null)
        }
        viewModelScope.launch {
            try {
                repository.preferences.selectConversation(null)
            } catch (error: Exception) {
                mutableState.update { it.copy(error = "Auswahl konnte nicht gespeichert werden.") }
            }
        }
    }

    /**
     * Sends ONLY a confirmed SQLite draft. A duplicate tap cannot start a second
     * send; the DAO transaction binds message insertion to draft clearing.
     */
    fun sendMessage() {
        val snapshot = mutableState.value
        val id = snapshot.selectedConversationId ?: return
        if (snapshot.isBusy || snapshot.isSending || snapshot.isLoadingDraft ||
            !snapshot.isDraftSaved || snapshot.error != null ||
            snapshot.draftText.isBlank() ||
            snapshot.respondingMessageId != null ||
            snapshot.messages.any { it.role == "user" && it.status in setOf("queued", "failed") }
        ) return

        mutableState.update { it.copy(isSending = true, error = null) }
        val clientId = UUID.randomUUID().toString()
        viewModelScope.launch {
            try {
                val message = repository.enqueueSavedDraft(id, snapshot.draftRevision, clientId)
                if (message == null) {
                    mutableState.update { current ->
                        if (current.selectedConversationId == id) current.copy(
                            error = "Entwurf ist noch nicht sicher gespeichert. Bitte erneut versuchen."
                        ) else current
                    }
                    return@launch
                }
                pendingDrafts.remove(id)
                mutableState.update { current ->
                    if (current.selectedConversationId == id) {
                        current.copy(
                            draftText = "",
                            draftRevision = snapshot.draftRevision + 1L,
                            persistedRevision = snapshot.draftRevision + 1L
                        )
                    } else current
                }
                val failOnce = mutableState.value.failNextMockReply
                mutableState.update { it.copy(failNextMockReply = false) }
                simulateAnswer(message, failOnce)
            } catch (error: Exception) {
                mutableState.update {
                    if (it.selectedConversationId == id) it.copy(
                        error = "Senden fehlgeschlagen. Dein bestätigter Entwurf bleibt erhalten."
                    ) else it
                }
            } finally {
                mutableState.update { it.copy(isSending = false) }
            }
        }
    }

    fun retryMockReply(messageId: String) {
        val current = mutableState.value
        if (current.isSending || current.respondingMessageId != null) return
        val candidate = current.messages.firstOrNull {
            it.id == messageId && it.role == "user" && it.status in setOf("queued", "failed")
        } ?: return
        if (!processingReplies.add(candidate.id)) return
        // Retry keeps the original clientMessageId; it must never create another user message.
        viewModelScope.launch {
            try {
                val prepared = repository.prepareMockRetry(candidate.id)
                if (prepared != null) {
                    processingReplies.remove(candidate.id)
                    simulateAnswer(prepared, false)
                }
            } catch (error: Exception) {
                mutableState.update { it.copy(error = "Wiederholen fehlgeschlagen. Bitte erneut versuchen.") }
            } finally {
                processingReplies.remove(candidate.id)
            }
        }
    }

    fun setFailNextMockReply(enabled: Boolean) {
        mutableState.update { it.copy(failNextMockReply = enabled) }
    }

    private fun simulateAnswer(message: MessageEntity, shouldFail: Boolean) {
        if (!processingReplies.add(message.id)) return
        mutableState.update { it.copy(respondingMessageId = message.id) }
        viewModelScope.launch {
            try {
                delay(500)
                if (shouldFail) {
                    repository.failMockReply(message.id)
                } else {
                    val response = inferenceGateway.generateCharacterTurn(
                        InferenceRequest(
                            requestId = "mock:${message.clientMessageId}",
                            conversationId = message.conversationId,
                            branchId = message.branchId,
                            personaRef = "d03-testpartner-v1",
                            scenarioRef = null,
                            policyVersion = "d04-gateway-contract-v1",
                            modelVersion = "d03-deterministic-v1",
                            privacyPurpose = "local_test",
                            latestUserText = message.content
                        )
                    )
                    when (response) {
                        is InferenceResult.Completed ->
                            repository.completeMockReply(message.id, response.text)
                        is InferenceResult.Rejected -> {
                            repository.failMockReply(message.id)
                            mutableState.update {
                                it.copy(error = "Simulationsantwort gesperrt: ${response.code}.")
                            }
                        }
                    }
                }
            } catch (error: Exception) {
                // A recoverable failed state remains in Room across process death.
                try {
                    repository.failMockReply(message.id)
                } catch (_: Exception) {
                    mutableState.update {
                        it.copy(error = "Antwortstatus konnte nicht gespeichert werden. Bitte Chat erneut öffnen.")
                    }
                }
            } finally {
                processingReplies.remove(message.id)
                mutableState.update {
                    if (it.respondingMessageId == message.id) it.copy(respondingMessageId = null)
                    else it
                }
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            try {
                repository.preferences.setThemeMode(mode)
            } catch (error: Exception) {
                mutableState.update { it.copy(error = "Darstellung konnte nicht gespeichert werden.") }
            }
        }
    }

    class Factory(context: Context) : ViewModelProvider.Factory {
        private val appContext = context.applicationContext

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(ChatViewModel::class.java))
            return ChatViewModel(ChatRepository(appContext)) as T
        }
    }
}
