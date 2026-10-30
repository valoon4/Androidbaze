package com.valoon4.emotionskompass.core.learning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

data class LearningUiState(
    val progress: LearningState = LearningState(),
    val busy: Boolean = true,
    val error: String? = null,
    val message: String? = null
)

class LearningViewModel(private val store: LearningRepository) : ViewModel() {
    private val mutex = Mutex()
    private val _state = MutableStateFlow(LearningUiState())
    val state = _state.asStateFlow()

    init { reload() }

    fun reload() {
        viewModelScope.launch {
            mutex.withLock {
                _state.value = _state.value.copy(busy = true, error = null, message = null)
                try {
                    val loaded = withContext(Dispatchers.IO) { store.load() }
                    _state.value = LearningUiState(progress = loaded, busy = false)
                } catch (e: Exception) {
                    _state.value = _state.value.copy(busy = false,
                        error = e.message ?: "Lernstand konnte nicht geladen werden.")
                }
            }
        }
    }

    private fun change(transform: (LearningState) -> LearningState) {
        if (_state.value.busy || _state.value.error != null) return
        viewModelScope.launch {
            mutex.withLock {
                if (_state.value.error != null) return@withLock
                _state.value = _state.value.copy(busy = true, message = null)
                try {
                    val saved = withContext(Dispatchers.IO) { store.change(transform) }
                    _state.value = LearningUiState(progress = saved, busy = false)
                } catch (e: Exception) {
                    _state.value = _state.value.copy(busy = false,
                        error = e.message ?: "Lernstand konnte nicht gespeichert werden.")
                }
            }
        }
    }

    fun toggleFavorite(key: String) = change { it.toggleFavorite(key) }
    fun markSeen(key: String) = change { it.markSeen(key) }
    fun review(key: String) = change { it.markReviewed(key) }
    fun completeStation(station: String) = change { it.markStation(station) }

    fun reset() {
        if (_state.value.busy || _state.value.error != null) return
        viewModelScope.launch {
            mutex.withLock {
                _state.value = _state.value.copy(busy = true)
                try {
                    withContext(Dispatchers.IO) { store.reset() }
                    _state.value = LearningUiState(busy = false,
                        message = "Dein Lernstand wurde zurückgesetzt.")
                } catch (e: Exception) {
                    _state.value = _state.value.copy(busy = false,
                        error = e.message ?: "Zurücksetzen fehlgeschlagen.")
                }
            }
        }
    }

    companion object {
        fun factory(store: LearningRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass == LearningViewModel::class.java)
                    return LearningViewModel(store) as T
                }
            }
    }
}
