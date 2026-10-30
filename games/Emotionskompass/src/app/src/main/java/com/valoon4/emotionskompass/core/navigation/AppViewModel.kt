package com.valoon4.emotionskompass.core.navigation

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.valoon4.emotionskompass.core.checkin.BodyCue
import com.valoon4.emotionskompass.core.checkin.CheckInDraft
import com.valoon4.emotionskompass.core.checkin.CheckInMode
import com.valoon4.emotionskompass.core.checkin.CheckInStep
import com.valoon4.emotionskompass.core.checkin.LifeArea
import com.valoon4.emotionskompass.core.checkin.NextStepIdea
import com.valoon4.emotionskompass.core.journal.BoundedJournalInput
import com.valoon4.emotionskompass.core.journal.EncryptedJournalRepository
import com.valoon4.emotionskompass.core.journal.JournalEntry
import com.valoon4.emotionskompass.core.journal.JournalBackup
import com.valoon4.emotionskompass.core.model.NeedCatalog
import com.valoon4.emotionskompass.core.model.EmotionCatalog
import com.valoon4.emotionskompass.core.model.EmotionKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class MainSection { START, ENTDECKEN, WISSEN, TAGEBUCH, RUECKBLICK }

/** Equal-level sub-areas of Entdecken. Display order is intentionally stable. */
enum class DiscoveryAtlas(val label: String) {
    GEFUEHLE("Gefühle"), BEDUERFNISSE("Bedürfnisse"), STRATEGIEN("Strategien")
}
enum class Destination { MAIN, EMOTION, VERGLEICH, PATH, LEARN_CARDS, COMPARISONS, LEARNING, WISSENSWELT, STRATEGIES, CHECKIN, SETTINGS }

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

data class JournalUiState(
    val entries: List<JournalEntry> = emptyList(),
    val busy: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

class AppViewModel(
    private val catalog: EmotionCatalog,
    private val needCatalog: NeedCatalog? = null,
    private val journal: EncryptedJournalRepository? = null,
    private val resolver: ContentResolver? = null,
    initialLockEnabled: Boolean = false
) : ViewModel() {
    private val _state = MutableStateFlow(AppUiState())
    val state = _state.asStateFlow()
    private val history = ArrayDeque<AppUiState>()
    private val _filters = MutableStateFlow(AtlasFilters())
    val filters = _filters.asStateFlow()
    private val _checkIn = MutableStateFlow(CheckInDraft())
    val checkIn = _checkIn.asStateFlow()
    private val _journalState = MutableStateFlow(JournalUiState())
    val journalState = _journalState.asStateFlow()
    private val _editingEntryId = MutableStateFlow<String?>(null)
    val editingEntryId = _editingEntryId.asStateFlow()

    private val _lockEnabled = MutableStateFlow(initialLockEnabled)
    val lockEnabled = _lockEnabled.asStateFlow()
    private val _unlocked = MutableStateFlow(!initialLockEnabled)
    val unlocked = _unlocked.asStateFlow()

    /** Reject async reads/results that originated before the latest relock. */
    private var accessEpoch = 0L
    private fun currentAccess(epoch: Long): Boolean = allowed() && epoch == accessEpoch

    init { if (journal != null && _unlocked.value) reloadJournal() }

    fun setLockEnabled(enabled: Boolean) {
        _lockEnabled.value = enabled
        if (!enabled) _unlocked.value = true
    }

    /** Called only after Android confirms the device credential. */
    fun unlockJournal() {
        accessEpoch++
        _unlocked.value = true
        reloadJournal()
    }

    /** Clear decrypted journal and private draft whenever the app backgrounds. */
    fun lockJournal() {
        if (!_lockEnabled.value) return
        accessEpoch++
        _unlocked.value = false
        _journalState.value = JournalUiState()
        _editingEntryId.value = null
        _checkIn.value = CheckInDraft()
        history.clear()
        _state.value = AppUiState(section = MainSection.START)
    }

    /** A system document picker temporarily takes focus; re-authentication is
     * still required after a normal background transition. */
    private var systemPickerInProgress = false
    fun setSystemPickerInProgress(value: Boolean) { systemPickerInProgress = value }

    fun onAppBackgrounded() {
        if (!systemPickerInProgress) lockJournal()
    }

    private fun allowed(): Boolean = _unlocked.value

    fun clearTransferMessage() {
        _journalState.value = _journalState.value.copy(error = null, message = null)
    }

    /**
     * SAF stream: only encrypted portable bytes ever leave this process.
     * The user chooses the destination through Android's document picker.
     */
    fun exportJournal(uri: Uri, password: String) {
        val store = journal ?: return
        val io = resolver ?: return
        if (!allowed() || _journalState.value.busy) return
        val epoch = accessEpoch
        _journalState.value = _journalState.value.copy(busy = true, error = null, message = null)
        viewModelScope.launch {
            val chars = password.toCharArray()
            try {
                withContext(Dispatchers.IO) {
                    val backup = store.encryptedBackup(chars)
                    try {
                        val output = io.openOutputStream(uri, "w")
                            ?: throw IllegalStateException("Zieldatei nicht beschreibbar.")
                        output.use { it.write(backup); it.flush() }
                    } finally { backup.fill(0) }
                }
                if (currentAccess(epoch)) _journalState.value = _journalState.value.copy(
                    busy = false, message = "Verschlüsselte Sicherung wurde erstellt.")
            } catch (e: Exception) {
                if (currentAccess(epoch)) _journalState.value = _journalState.value.copy(
                    busy = false, error = "Export fehlgeschlagen: ${e.message}. Eine unvollständige Zieldatei bitte löschen.")
            } finally { chars.fill(0.toChar()) }
        }
    }

    /**
     * Explicitly confirmed replacement, never an automatic merge. Refuse
     * oversized data and do not alter existing entries for wrong passwords.
     */
    fun importJournal(uri: Uri, password: String) {
        val store = journal ?: return
        val io = resolver ?: return
        if (!allowed() || _journalState.value.busy) return
        val epoch = accessEpoch
        _journalState.value = _journalState.value.copy(busy = true, error = null, message = null)
        viewModelScope.launch {
            val chars = password.toCharArray()
            try {
                val count = withContext(Dispatchers.IO) {
                    val input = io.openInputStream(uri)
                        ?: throw IllegalStateException("Sicherungsdatei konnte nicht gelesen werden.")
                    val bytes = input.use { stream ->
                        BoundedJournalInput.read(stream, JournalBackup.MAX_BACKUP_BYTES)
                    }
                    try {
                        require(bytes.size <= JournalBackup.MAX_BACKUP_BYTES) {
                            "Sicherungsdatei ist zu groß."
                        }
                        store.restoreBackup(bytes, chars)
                    } finally { bytes.fill(0) }
                }
                if (allowed()) {
                    // JournalUiState() resets busy=false before the fresh authenticated read.
                    _journalState.value = JournalUiState(
                        message = "Wiederherstellung erfolgreich: $count Einträge.")
                    reloadJournal()
                }
            } catch (e: Exception) {
                if (currentAccess(epoch)) _journalState.value = _journalState.value.copy(
                    busy = false, error = "Wiederherstellung fehlgeschlagen: ${e.message}. Bestehende Daten wurden nicht absichtlich überschrieben.")
            } finally { chars.fill(0.toChar()) }
        }
    }

    fun clearJournalError() { _journalState.value = _journalState.value.copy(error = null) }

    /** A decryption or key failure never replaces the previous encrypted data. */
    fun reloadJournal() {
        val store = journal ?: return
        if (!allowed() || _journalState.value.busy) return
        val epoch = accessEpoch
        val transferMessage = _journalState.value.message
        _journalState.value = _journalState.value.copy(busy = true, error = null)
        viewModelScope.launch {
            try {
                val entries = withContext(Dispatchers.IO) { store.list() }
                if (currentAccess(epoch)) _journalState.value = JournalUiState(
                    entries.sortedByDescending { it.createdAt }, message = transferMessage
                )
            } catch (e: Exception) {
                if (currentAccess(epoch)) {
                    // Fail closed: do not leave previously decrypted entries visible
                    // when re-reading/authenticating the local encrypted file fails.
                    _journalState.value = JournalUiState(
                        error = e.message ?: "Tagebuch konnte nicht geöffnet werden"
                    )
                }
            }
        }
    }

    fun saveCheckIn() {
        val store = journal ?: return
        if (!allowed() || _journalState.value.busy || _state.value.destination != Destination.CHECKIN ||
            _checkIn.value.step != CheckInStep.AUSKLANG || !_checkIn.value.hasPersonalInput) return
        val epoch = accessEpoch
        val snapshot = _checkIn.value
        val existingId = _editingEntryId.value
        _journalState.value = _journalState.value.copy(busy = true, error = null)
        viewModelScope.launch {
            try {
                val saved = withContext(Dispatchers.IO) { store.save(snapshot, existingId) }
                if (!currentAccess(epoch)) return@launch
                _journalState.value = JournalUiState(
                    (_journalState.value.entries.filterNot { it.id == saved.id } + saved)
                        .sortedByDescending { it.createdAt }
                )
                _checkIn.value = CheckInDraft()
                _editingEntryId.value = null
                history.clear()
                _state.value = AppUiState(section = MainSection.TAGEBUCH)
            } catch (e: Exception) {
                if (currentAccess(epoch)) _journalState.value = _journalState.value.copy(busy = false,
                    error = e.message ?: "Eintrag konnte nicht gespeichert werden")
            }
        }
    }

    fun editJournalEntry(id: String) {
        if (!allowed() || _journalState.value.busy) return
        val entry = _journalState.value.entries.firstOrNull { it.id == id } ?: return
        _editingEntryId.value = id
        _checkIn.value = entry.draft.copy(
            step = if (entry.draft.mode == CheckInMode.KURZ) CheckInStep.BENENNEN
                else CheckInStep.ANKOMMEN
        )
        push(_state.value.copy(destination = Destination.CHECKIN))
    }

    fun deleteJournalEntry(id: String) {
        val store = journal ?: return
        if (!allowed() || _journalState.value.busy) return
        val epoch = accessEpoch
        _journalState.value = _journalState.value.copy(busy = true, error = null)
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { store.delete(id) }
                if (!currentAccess(epoch)) return@launch
                _journalState.value = JournalUiState(
                    _journalState.value.entries.filterNot { it.id == id }
                )
            } catch (e: Exception) {
                if (currentAccess(epoch)) _journalState.value = _journalState.value.copy(busy = false,
                    error = e.message ?: "Eintrag konnte nicht gelöscht werden")
            }
        }
    }

    fun deleteEntireJournal() {
        val store = journal ?: return
        if (!allowed() || _journalState.value.busy) return
        val epoch = accessEpoch
        _journalState.value = _journalState.value.copy(busy = true, error = null)
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { store.deleteAll() }
                if (currentAccess(epoch)) _journalState.value = JournalUiState()
            } catch (e: Exception) {
                if (currentAccess(epoch)) _journalState.value = _journalState.value.copy(busy = false,
                    error = e.message ?: "Tagebuch konnte nicht gelöscht werden")
            }
        }
    }

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
        _editingEntryId.value = null
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

    fun openLearning() = push(_state.value.copy(destination = Destination.LEARNING, section = MainSection.WISSEN))
    fun openKnowledgeWorld() = push(_state.value.copy(destination = Destination.WISSENSWELT, section = MainSection.WISSEN))
    fun openStrategies() = push(_state.value.copy(destination = Destination.STRATEGIES, section = MainSection.ENTDECKEN))
    fun openLearningCards() = push(_state.value.copy(destination = Destination.LEARN_CARDS))
    fun openComparisons() = push(_state.value.copy(destination = Destination.COMPARISONS))

    fun openComparison(first: String, second: String) {
        if (catalog.compare(first, second) == null) return
        push(_state.value.copy(destination = Destination.VERGLEICH, emotionId = first, comparisonId = second))
    }

    fun openCheckin(emotionId: String? = null) {
        _editingEntryId.value = null
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
    fun toggleCheckInNeed(id: String) {
        if (id == CheckInDraft.UNKNOWN_NEED_ID ||
            needCatalog?.find(id) != null ||
            needCatalog == null // Allows existing isolated ViewModel tests without catalog injection.
        ) modifyCheckIn { it.toggleNeed(id) }
    }
    fun setCheckInInsight(text: String) = modifyCheckIn { it.withInsight(text) }
    fun setCheckInNextStep(idea: NextStepIdea?) = modifyCheckIn { it.withNextStepIdea(idea) }
    fun setCheckInNextStepNote(text: String) = modifyCheckIn { it.withNextStepNote(text) }
    fun nextCheckInStep() = modifyCheckIn { it.next() }
    fun previousCheckInStep() = modifyCheckIn { it.previous() }

    /** Wipes sensitive draft values before navigating away. */
    fun closeCheckIn() {
        if (_state.value.destination != Destination.CHECKIN) return
        _checkIn.value = CheckInDraft()
        _editingEntryId.value = null
        navigateBack()
    }

    fun restartCheckIn() {
        _editingEntryId.value = null
        modifyCheckIn { CheckInDraft() }
    }
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
        fun factory(
            catalog: EmotionCatalog,
            needCatalog: NeedCatalog? = null,
            journal: EncryptedJournalRepository? = null,
            resolver: ContentResolver? = null,
            initialLockEnabled: Boolean = false
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass == AppViewModel::class.java)
                    return AppViewModel(catalog, needCatalog, journal, resolver, initialLockEnabled) as T
                }
            }
    }
}
