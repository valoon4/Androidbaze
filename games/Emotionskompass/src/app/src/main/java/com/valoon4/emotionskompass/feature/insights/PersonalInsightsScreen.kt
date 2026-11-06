package com.valoon4.emotionskompass.feature.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.valoon4.emotionskompass.core.insights.PersonalInsight
import com.valoon4.emotionskompass.core.insights.PersonalInsightChanges
import com.valoon4.emotionskompass.core.insights.ReflectionRoute
import com.valoon4.emotionskompass.core.insights.PersonalInsight as UserInsight
import com.valoon4.emotionskompass.core.navigation.InsightUiState
import com.valoon4.emotionskompass.core.journal.JournalEntry
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormat = DateTimeFormatter.ofPattern("dd. MMM yyyy", Locale.GERMANY)

/**
 * M7.3.2: User-written insights only. Dialog text and search query intentionally
 * use remember(), never rememberSaveable(), so they are never persisted in Bundles.
 */
@Composable
fun PersonalInsightsScreen(
    state: InsightUiState,
    journalEntries: List<JournalEntry>,
    onBack: () -> Unit,
    onReload: () -> Unit,
    onSave: (String, String?, String?) -> Unit,
    onArchive: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit,
    onUnlink: (String, String) -> Unit,
    onOpenJournal: (String) -> Unit,
    reflectionOriginLabel: String? = null,
    // Transient navigation context, never stored in a PersonalInsight.
    weeklyReflectionContext: String? = null,
    onWeeklyReflectionDismissed: () -> Unit = {},
    showBackButton: Boolean = true,
    onConsiderIdeaFromInsight: (String) -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var showArchived by remember { mutableStateOf(false) }
    var editor by remember { mutableStateOf<PersonalInsight?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    // An entered note must not disappear after an incidental Back/cancel.
    // Nothing here is ever copied into SavedState or an unencrypted store.
    var confirmDiscardDraft by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf("") }
    var selectedJournalId by remember { mutableStateOf<String?>(null) }
    var showAllJournalEntries by remember { mutableStateOf(false) }
    var journalDateQuery by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<PersonalInsight?>(null) }
    // Deliberately never save private answers, the origin, or progress in a Bundle.
    var showGuide by remember { mutableStateOf(false) }
    var guideRoute by remember { mutableStateOf(ReflectionRoute.OPEN) }
    var initialGuideHandled by remember(reflectionOriginLabel) { mutableStateOf(false) }
    var initialWeekHandled by remember(weeklyReflectionContext) { mutableStateOf(false) }
    var fromWeeklyReflection by remember { mutableStateOf(false) }

    LaunchedEffect(reflectionOriginLabel, state.loaded, state.error) {
        if (!initialGuideHandled && reflectionOriginLabel != null &&
            state.loaded && state.error == null &&
            state.entries.size < UserInsight.MAX_INSIGHTS) {
            initialGuideHandled = true
            guideRoute = ReflectionRoute.OBSERVATION
            showGuide = true
        }
    }

    LaunchedEffect(state.loaded, state.error) {
        if (!state.loaded || state.error != null) {
            // No prior decrypted/entered personal words survive a failed import or lock.
            showGuide = false
            showEditor = false
            confirmDiscardDraft = false
            draft = ""
            editor = null
            selectedJournalId = null
            pendingDelete = null
            saving = false
            fromWeeklyReflection = false
        }
    }

    LaunchedEffect(state.message) {
        if (saving && state.message in listOf("Erkenntnis gespeichert.", "Änderungen gespeichert.")) {
            showEditor = false
            confirmDiscardDraft = false
            draft = ""
            editor = null
            selectedJournalId = null
            saving = false
            fromWeeklyReflection = false
        }
    }

    fun openEditor(insight: PersonalInsight?) {
        editor = insight
        draft = insight?.text.orEmpty()
        selectedJournalId = null
        showAllJournalEntries = false
        journalDateQuery = ""
        confirmDiscardDraft = false
        saving = false
        fromWeeklyReflection = false
        showEditor = true
    }

    // The weekly route bypasses the guided questions deliberately: it opens
    // the existing EMPTY editor, never a pre-populated insight or weekly record.
    LaunchedEffect(weeklyReflectionContext, state.loaded, state.error, state.busy) {
        if (!initialWeekHandled && weeklyReflectionContext != null &&
            state.loaded && !state.busy && state.error == null &&
            state.entries.size < UserInsight.MAX_INSIGHTS) {
            initialWeekHandled = true
            openEditor(null)
            fromWeeklyReflection = true
        }
    }

    fun dismissEditor() {
        val returnToWeek = fromWeeklyReflection
        showEditor = false
        confirmDiscardDraft = false
        draft = ""
        editor = null
        selectedJournalId = null
        saving = false
        fromWeeklyReflection = false
        if (returnToWeek) onWeeklyReflectionDismissed()
    }

    fun requestEditorDismiss() {
        if (state.busy || saving) return
        if (draft.isNotBlank() && draft != editor?.text.orEmpty()) {
            confirmDiscardDraft = true
        } else dismissEditor()
    }

    if (showGuide && state.loaded && state.error == null) {
        GuidedReflectionDialog(
            route = guideRoute,
            observationLabel = reflectionOriginLabel.takeIf {
                guideRoute == ReflectionRoute.OBSERVATION
            },
            onCancel = { showGuide = false },
            onDraftReady = { userWords ->
                showGuide = false
                openEditor(null)
                draft = userWords
            }
        )
    }

    pendingDelete?.let { insight ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Erkenntnis wirklich löschen?") },
            text = { Text("Dein selbst geschriebener Text wird dauerhaft entfernt. Verknüpfte Tagebucheinträge bleiben erhalten.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(insight.id)
                    pendingDelete = null
                }, enabled = !state.busy) { Text("Endgültig löschen") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Abbrechen") }
            }
        )
    }

    if (showEditor) {
        AlertDialog(
            onDismissRequest = { requestEditorDismiss() },
            title = { Text(if (editor == null) "Eigene Erkenntnis notieren" else "Erkenntnis bearbeiten") },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 430.dp)
                        .testTag("insight-editor-list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (fromWeeklyReflection && weeklyReflectionContext != null) {
                        item {
                            Text(
                                "Freiwilliger Rückblick: $weeklyReflectionContext",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.testTag("weekly-reflection-context")
                            )
                            Text(
                                "Was ist mir rückblickend an dieser Woche aufgefallen?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.testTag("weekly-reflection-question")
                            )
                            Text(
                                "Du kannst die Frage überspringen oder ohne Speichern " +
                                    "zur Woche zurückkehren. Der Zeitraum wird nicht " +
                                    "automatisch Teil deiner Erkenntnis.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    item {
                        Text("Schreibe nur auf, was du selbst festhalten möchtest. Die App speichert nichts automatisch.")
                    }
                    item {
                        OutlinedTextField(
                            value = draft,
                            onValueChange = { if (it.length <= UserInsight.MAX_TEXT_LENGTH) draft = it },
                            label = { Text("Deine eigenen Worte") },
                            minLines = 4,
                            modifier = Modifier.fillMaxWidth().testTag("insight-editor")
                        )
                        Text("${draft.length} von ${UserInsight.MAX_TEXT_LENGTH} Zeichen",
                            style = MaterialTheme.typography.bodySmall)
                    }
                    if (journalEntries.isNotEmpty()) {
                        item {
                            Text("Optional mit einem Tagebucheintrag verbinden",
                                fontWeight = FontWeight.SemiBold)
                            Text("Nur wenn du es möchtest. Die Verknüpfung ist keine automatisch erkannte Ursache. Du kannst sie später wieder lösen.",
                                style = MaterialTheme.typography.bodySmall)
                        }
                        item {
                            FilterChip(
                                selected = selectedJournalId == null,
                                onClick = { selectedJournalId = null },
                                label = { Text("Keine neue Verknüpfung") }
                            )
                        }
                        if (journalEntries.size > 7) {
                            item {
                                OutlinedButton(
                                    onClick = {
                                        showAllJournalEntries = !showAllJournalEntries
                                        journalDateQuery = ""
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                        .testTag("insight-journal-expand")
                                ) {
                                    Text(if (showAllJournalEntries) "Nur die letzten 7 Einträge"
                                        else "Weitere Tagebucheinträge auswählen")
                                }
                            }
                        }
                        if (showAllJournalEntries) {
                            item {
                                OutlinedTextField(
                                    value = journalDateQuery,
                                    onValueChange = { journalDateQuery = it.take(40) },
                                    label = { Text("Nach Datum suchen") },
                                    supportingText = { Text("Zum Beispiel 01. Nov oder 2026") },
                                    modifier = Modifier.fillMaxWidth()
                                        .testTag("insight-journal-date-search")
                                )
                            }
                        }
                        val matches = journalEntries.sortedByDescending { it.createdAt }
                            .let { entries ->
                                if (!showAllJournalEntries) entries.take(7)
                                else entries.filter {
                                    formatJournalMoment(it.createdAt).contains(
                                        journalDateQuery.trim(), ignoreCase = true
                                    )
                                }
                            }
                        if (matches.isEmpty()) {
                            item {
                                Text("Kein Tagebucheintrag mit diesem Datum gefunden.",
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        items(matches, key = { it.id }) { item ->
                            FilterChip(
                                selected = selectedJournalId == item.id,
                                onClick = { selectedJournalId = item.id },
                                label = { Text("Eintrag vom ${formatJournalMoment(item.createdAt)}") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    editor?.takeIf { it.journalIds.isNotEmpty() }?.let { old ->
                        item {
                            Text("Bisherige Verknüpfungen (${old.journalIds.size}) bleiben beim Bearbeiten bestehen.",
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    if (state.error != null) {
                        item { Text(state.error, color = MaterialTheme.colorScheme.error) }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !state.busy && !saving && state.error == null && state.loaded &&
                        (editor != null || state.entries.size < UserInsight.MAX_INSIGHTS) &&
                        draft.trim().isNotEmpty() && draft.trim().length <= UserInsight.MAX_TEXT_LENGTH,
                    onClick = {
                        saving = true
                        onSave(draft, editor?.id, selectedJournalId)
                    }
                ) { Text(if (state.busy) "Speichere …" else "Ausdrücklich speichern") }
            },
            dismissButton = {
                TextButton(
                    enabled = !state.busy && !saving,
                    onClick = { requestEditorDismiss() }
                ) { Text("Abbrechen") }
            }
        )
    }

    if (confirmDiscardDraft && showEditor) {
        AlertDialog(
            onDismissRequest = { confirmDiscardDraft = false },
            title = { Text("Ungespeicherte Eingabe verwerfen?") },
            text = {
                Text(
                    "Dein bisheriger Text ist noch nicht gespeichert. " +
                        "Wenn du ihn verwirfst, geht nur diese Eingabe verloren. " +
                        "Vorhandene Tagebucheinträge und Erkenntnisse bleiben erhalten."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { dismissEditor() },
                    modifier = Modifier.testTag("insight-confirm-discard")
                ) { Text("Eingabe verwerfen") }
            },
            dismissButton = {
                TextButton(
                    onClick = { confirmDiscardDraft = false },
                    modifier = Modifier.testTag("insight-continue-editing")
                ) { Text("Weiter bearbeiten") }
            }
        )
    }

    val displayed = PersonalInsightChanges.search(
        state.entries, query, includeArchived = true
    ).filter { it.archived == showArchived }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            if (weeklyReflectionContext != null && !showEditor) {
                TextButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                        .testTag("weekly-reflection-back-to-week")
                ) {
                    Text("Zurück zur ausgewählten Woche")
                }
            } else if (showBackButton) {
                TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Zurück zur Rückblick-Übersicht")
                }
            }
            Text("Deine Erkenntnisse",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() }.testTag("insights-heading"))
            Text("Gedanken, die du selbst festgehalten hast. Beobachtungen aus der App werden nicht automatisch zu Erkenntnissen.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (state.error != null || !state.loaded) {
            item {
                Text(state.error ?: "Deine Erkenntnisse werden sicher geladen …",
                    color = if (state.error != null) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedButton(onClick = onReload, enabled = !state.busy,
                    modifier = Modifier.fillMaxWidth()) {
                    Text("Erneut sicher laden")
                }
            }
        } else {
            item {
                Button(
                    onClick = { openEditor(null) },
                    enabled = !state.busy && state.entries.size < UserInsight.MAX_INSIGHTS,
                    modifier = Modifier.fillMaxWidth().testTag("insight-create")
                ) { Text("Eigene Erkenntnis festhalten") }
                OutlinedButton(
                    onClick = {
                        guideRoute = ReflectionRoute.OPEN
                        showGuide = true
                    },
                    enabled = !state.busy && state.error == null &&
                        state.entries.size < UserInsight.MAX_INSIGHTS,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                        .testTag("insight-guided-reflection")
                ) { Text("Freiwillige Reflexion mit Fragen") }
                Text(
                    "Drei offene Fragen als Anregung – du kannst jederzeit abbrechen " +
                        "oder Fragen überspringen. Nichts wird automatisch gespeichert.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                state.message?.let {
                    Text(it, color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Erkenntnisse durchsuchen") },
                    modifier = Modifier.fillMaxWidth().testTag("insight-search"),
                    singleLine = true
                )
            }
            item {
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !showArchived,
                        onClick = { showArchived = false },
                        label = { Text("Aktuelle") }
                    )
                    FilterChip(
                        selected = showArchived,
                        onClick = { showArchived = true },
                        label = { Text("Archiv") }
                    )
                }
            }
            if (displayed.isEmpty()) {
                item {
                    Card {
                        Column(Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Text(if (query.isNotBlank()) "Keine passenden Erkenntnisse gefunden."
                                else if (showArchived) "Dein Archiv ist noch leer."
                                else "Noch keine eigenen Erkenntnisse festgehalten.",
                                fontWeight = FontWeight.SemiBold)
                            Text("Du entscheidest selbst, ob und wann du etwas notieren möchtest.")
                        }
                    }
                }
            }
            items(displayed, key = { it.id }) { insight ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(formatDate(insight.createdAtUtcMillis),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall)
                        Text(insight.text, style = MaterialTheme.typography.bodyLarge)
                        for (id in insight.journalIds.sorted()) {
                            if (journalEntries.any { it.id == id }) {
                                TextButton(
                                    onClick = { onOpenJournal(id) },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Verknüpften Tagebucheintrag ansehen") }
                                TextButton(
                                    onClick = { onUnlink(insight.id, id) },
                                    enabled = !state.busy,
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Diese Verknüpfung lösen") }
                            }
                        }
                        OutlinedButton(
                            onClick = { onConsiderIdeaFromInsight(insight.id) },
                            enabled = !state.busy,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                                .testTag("insight-consider-idea-${insight.id}")
                        ) { Text("Daraus freiwillig eine Idee entwickeln") }
                        OutlinedButton(
                            onClick = { openEditor(insight) }, enabled = !state.busy,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Bearbeiten") }
                        OutlinedButton(
                            onClick = { onArchive(insight.id, !insight.archived) },
                            enabled = !state.busy, modifier = Modifier.fillMaxWidth()
                        ) { Text(if (insight.archived) "Aus Archiv zurückholen" else "Archivieren") }
                        TextButton(
                            onClick = { pendingDelete = insight },
                            enabled = !state.busy, modifier = Modifier.fillMaxWidth()
                        ) { Text("Löschen …") }
                    }
                }
            }
        }
    }
}

private fun formatDate(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
        .toLocalDate().format(dateFormat)

private val journalMomentFormat =
    DateTimeFormatter.ofPattern("dd. MMM yyyy · HH:mm", Locale.GERMANY)

private fun formatJournalMoment(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(journalMomentFormat)
