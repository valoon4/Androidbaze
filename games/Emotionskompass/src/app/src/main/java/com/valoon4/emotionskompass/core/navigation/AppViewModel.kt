package com.valoon4.emotionskompass.core.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.valoon4.emotionskompass.core.checkin.BodyCue
import com.valoon4.emotionskompass.core.checkin.CheckInDraft
import com.valoon4.emotionskompass.core.checkin.CheckInMode
import com.valoon4.emotionskompass.core.checkin.CheckInStep
import com.valoon4.emotionskompass.core.checkin.LifeArea
import com.valoon4.emotionskompass.core.checkin.PersonalNeed
import com.valoon4.emotionskompass.core.checkin.NextStepIdea
import com.valoon4.emotionskompass.core.model.EmotionCatalog
import com.valoon4.emotionskompass.core.model.EmotionKind
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class MainSection { START, ENTDECKEN, TAGEBUCH, RUECKBLICK }
enum class Destination { MAIN, EMOTION, VERGLEICH, PATH, LEARN_CARDS, COMPARISONS, CHECKIN, SETTINGS }

data class AtlasFilters(
    val query: String = "",
    val family: String = "Alle",
    val kind: EmotionKind? = null,
    val letter: String = "Alle"
)

data class AppUiState(
    val section: MainSection = MainSection.START,
    val destination: Destination = Destination.MAIN,
    val emotionId: String? = null,
    val comparisonId: String? = null,
    val pathId: String? = null
)

class AppViewModel(private val catalog: EmotionCatalog) : ViewModel() {
    private val _state = MutableStateFlow(AppUiState())
    val state = _state.asStateFlow()
    private val history = ArrayDeque<AppUiState>()
    private val _filters = MutableStateFlow(AtlasFilters())
    val filters = _filters.asStateFlow()
    private val _checkIn = MutableStateFlow(CheckInDraft())
    val checkIn = _checkIn.asStateFlow()

    fun setSearch(query: String) { _filters.value = _filters.value.copy(query = query) }
    fun setFamily(family: String) { _filters.value = _filters.value.copy(family = family) }
    fun setKind(kind: EmotionKind?) { _filters.value = _filters.value.copy(kind = kind) }
    fun setLetter(letter: String) { _filters.value = _filters.value.copy(letter = letter) }
    fun clearFilters() { _filters.value = AtlasFilters() }

    fun openPath(id: String) {
        if (catalog.findPath(id) == null) return
        push(_state.value.copy(destination = Destination.PATH, pathId = id))
    }

    fun selectSection(section: MainSection) {
        _checkIn.value = CheckInDraft()
        history.clear()
        _state.value = AppUiState(section = section)
    }

    fun openEmotion(id: String) {
        if (catalog.find(id) == null) return
        push(_state.value.copy(destination = Destination.EMOTION, emotionId = id,
            comparisonId = null))
    }

    fun compareWith(id: String) {
        val first = _state.value.emotionId ?: return
        if (catalog.find(id) == null || first == id) return
        push(_state.value.copy(destination = Destination.VERGLEICH, comparisonId = id))
    }

    fun openLearningCards() = push(_state.value.copy(destination = Destination.LEARN_CARDS))
    fun openComparisons() = push(_state.value.copy(destination = Destination.COMPARISONS))

    fun openComparison(first: String, second: String) {
        if (catalog.compare(first, second) == null) return
        push(_state.value.copy(destination = Destination.VERGLEICH, emotionId = first, comparisonId = second))
    }

    fun openCheckin(emotionId: String? = null) {
        val validId = emotionId?.takeIf { catalog.find(it) != null }
        _checkIn.value = CheckInDraft.startWith(validId)
        push(_state.value.copy(destination = Destination.CHECKIN))
    }

    private fun modifyCheckIn(update: (CheckInDraft) -> CheckInDraft) {
        if (_state.value.destination == Destination.CHECKIN) {
            _checkIn.value = update(_checkIn.value)
        }
    }

    fun setCheckInMode(mode: CheckInMode) = modifyCheckIn { it.withMode(mode) }
    fun toggleBodyCue(cue: BodyCue) = modifyCheckIn { it.toggleCue(cue) }

    fun toggleCheckInEmotion(id: String) {
        if (catalog.find(id) != null) modifyCheckIn { it.toggleEmotion(id) }
    }

    fun setCheckInIntensity(id: String, intensity: Int?) {
        modifyCheckIn { draft ->
            if (id in draft.emotions && (intensity == null || intensity in 0..10))
                draft.withIntensity(id, intensity) else draft
        }
    }

    fun setCheckInArea(area: LifeArea?) = modifyCheckIn { it.withArea(area) }
    fun setCheckInNote(note: String) = modifyCheckIn { it.withNote(note) }
    fun setCheckInSituation(text: String) = modifyCheckIn { it.withSituation(text) }
    fun setCheckInInterpretation(text: String) = modifyCheckIn { it.withInterpretation(text) }
    fun toggleCheckInNeed(need: PersonalNeed) = modifyCheckIn { it.toggleNeed(need) }
    fun setCheckInInsight(text: String) = modifyCheckIn { it.withInsight(text) }
    fun setCheckInNextStep(idea: NextStepIdea?) = modifyCheckIn { it.withNextStepIdea(idea) }
    fun setCheckInNextStepNote(text: String) = modifyCheckIn { it.withNextStepNote(text) }
    fun nextCheckInStep() = modifyCheckIn { it.next() }
    fun previousCheckInStep() = modifyCheckIn { it.previous() }

    /** Wipes sensitive draft values before navigating away. */
    fun closeCheckIn() {
        if (_state.value.destination != Destination.CHECKIN) return
        _checkIn.value = CheckInDraft()
        navigateBack()
    }

    fun restartCheckIn() = modifyCheckIn { CheckInDraft() }
    fun openSettings() = push(_state.value.copy(destination = Destination.SETTINGS))

    private fun push(next: AppUiState) {
        history.addLast(_state.value)
        _state.value = next
    }

    fun back() {
        if (_state.value.destination == Destination.CHECKIN) {
            if (_checkIn.value.isFirstStep) closeCheckIn()
            else previousCheckInStep()
            return
        }
        navigateBack()
    }

    private fun navigateBack() {
        _state.value = if (history.isEmpty()) AppUiState(section = _state.value.section)
        else history.removeLast()
    }

    companion object {
        fun factory(catalog: EmotionCatalog): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass == AppViewModel::class.java)
                    return AppViewModel(catalog) as T
                }
            }
    }
}
