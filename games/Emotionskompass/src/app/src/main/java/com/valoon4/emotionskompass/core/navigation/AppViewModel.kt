package com.valoon4.emotionskompass.core.navigation

import androidx.lifecycle.ViewModel
import com.valoon4.emotionskompass.core.model.EmotionCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class MainSection { START, ENTDECKEN, TAGEBUCH, RUECKBLICK }
enum class Destination { MAIN, EMOTION, VERGLEICH, CHECKIN, SETTINGS }

data class AppUiState(
    val section: MainSection = MainSection.START,
    val destination: Destination = Destination.MAIN,
    val emotionId: String? = null,
    val comparisonId: String? = null
)

class AppViewModel : ViewModel() {
    private val _state = MutableStateFlow(AppUiState())
    val state = _state.asStateFlow()
    private val history = ArrayDeque<AppUiState>()

    fun selectSection(section: MainSection) {
        history.clear()
        _state.value = AppUiState(section = section)
    }

    fun openEmotion(id: String) {
        if (EmotionCatalog.find(id) == null) return
        push(_state.value.copy(destination = Destination.EMOTION, emotionId = id))
    }

    fun compareWith(id: String) {
        val first = _state.value.emotionId ?: return
        if (EmotionCatalog.find(id) == null || first == id) return
        push(_state.value.copy(destination = Destination.VERGLEICH, comparisonId = id))
    }

    fun openCheckin() = push(_state.value.copy(destination = Destination.CHECKIN))
    fun openSettings() = push(_state.value.copy(destination = Destination.SETTINGS))

    private fun push(next: AppUiState) {
        history.addLast(_state.value)
        _state.value = next
    }

    fun back() {
        _state.value = if (history.isEmpty()) {
            AppUiState(section = _state.value.section)
        } else {
            history.removeLast()
        }
    }
}
