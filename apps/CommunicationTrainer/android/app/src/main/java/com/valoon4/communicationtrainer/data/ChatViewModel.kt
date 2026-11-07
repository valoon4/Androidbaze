package com.valoon4.communicationtrainer.data

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
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
    val error: String? = null
) {
    val isDraftSaved: Boolean get() = !isLoadingDraft && draftRevision == persistedRevision
}

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = mutableState
    private var loadJob: Job? = null

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
        mutableState.update {
            it.copy(
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
                mutableState.update {
                    if (it.selectedConversationId == id && it.isLoadingDraft) it.copy(
                        draftText = draft?.text.orEmpty(),
                        draftRevision = draft?.editRevision ?: 0L,
                        persistedRevision = draft?.editRevision ?: 0L,
                        isLoadingDraft = false
                    ) else it
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
        if (before.isLoadingDraft || before.isBusy) return
        val revision = before.draftRevision + 1L
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
                repository.preferences.selectConversation(null)
                loadJob?.cancel()
                mutableState.update { state ->
                    if (state.selectedConversationId == id) state.copy(
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
        mutableState.update { it.copy(selectedConversationId = null, isLoadingDraft = false, error = null) }
        viewModelScope.launch {
            try {
                repository.preferences.selectConversation(null)
            } catch (error: Exception) {
                mutableState.update { it.copy(error = "Auswahl konnte nicht gespeichert werden.") }
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
