package com.valoon4.emotionskompass.feature.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.valoon4.emotionskompass.core.insights.PersonalInsight
import com.valoon4.emotionskompass.core.insights.PersonalInsightFormat
import com.valoon4.emotionskompass.core.journal.JournalEntry
import com.valoon4.emotionskompass.core.navigation.PersonalInsightUiState
import java.text.DateFormat
import java.util.Date
import java.util.Locale

/**
 * Explicitly self-written insights: optional prompts, no automatic
 * psychological interpretation, no saving drafts in SavedState/Bundle.
 */
@Composable
fun PersonalInsightsScreen(
    state: PersonalInsightUiState,
    journal: List<JournalEntry>,
    journalLoaded: Boolean,
    onReload: () -> Unit,
    onSave: (String, String?, String?) -> Unit,
    onArchive: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit,
    onOpenJournalEntry: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var showArchived by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<PersonalInsight?>(null) }
    var editorOpen by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf("") }
    var linkId by remember { mutableStateOf<String?>(null) }
    var choosingLink by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf<String?>(null) }
    var promptIndex by remember { mutableStateOf(0) }
    val prompts = listOf(
        "Was möchtest du aus einer Situation für dich mitnehmen?",
        "Welche eigene Beobachtung überrascht dich – ohne sie sofort erklären zu müssen?",
        "Gibt es etwas, das du beim nächsten Mal anders ausprobieren möchtest?",
        "Welche Frage ist für dich gerade noch offen?"
    )
    fun closeEditor() {
        editorOpen = false
        editing = null
        draft = ""
        linkId = null
        choosingLink = false
    }
    fun startEditor(original: PersonalInsight? = null) {
        editing = original
        draft = original?.text ?: ""
        linkId = original?.journalId
        editorOpen = true
    }

    if (confirmDelete != null) {
        val id = confirmDelete!!
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text("Erkenntnis endgültig löschen?") },
            text = { Text("Nur diese selbst geschriebene Erkenntnis wird gelöscht. Verknüpfte Tagebucheinträge bleiben erhalten.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = null
                    onDelete(id)
                }) { Text("Endgültig löschen") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = null }) { Text("Abbrechen") }
            }
        )
    }

    if (choosingLink) {
        AlertDialog(
            onDismissRequest = { choosingLink = false },
            title = { Text("Tagebucheintrag verknüpfen") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Die Verknüpfung ist freiwillig. Sie bleibt nur auf deinem Gerät und kann später entfernt werden.",
                        style = MaterialTheme.typography.bodySmall)
                    LazyColumn(Modifier.heightIn(max = 330.dp)) {
                        item {
                            TextButton(onClick = {
                                linkId = null
                                choosingLink = false
                            }) { Text("Ohne Verknüpfung") }
                        }
                        items(journal, key = { it.id }) { entry ->
                            TextButton(onClick = {
                                linkId = entry.id
                                choosingLink = false
                            }, modifier = Modifier.fillMaxWidth()) {
                                Text(formatInsightDate(entry.createdAt))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { choosingLink = false }) { Text("Schließen") }
            }
        )
    }

    if (editorOpen && !choosingLink) {
        AlertDialog(
            onDismissRequest = ::closeEditor,
            title = { Text(if (editing == null) "Eigene Erkenntnis festhalten" else "Erkenntnis bearbeiten") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Deine eigenen Worte. Nichts wird automatisch ausgewertet oder gespeichert.",
                        style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it.take(PersonalInsightFormat.MAX_TEXT_LENGTH) },
                        label = { Text("Meine Erkenntnis") },
                        minLines = 3,
                        maxLines = 6,
                        supportingText = {
                            Text("${draft.length} / ${PersonalInsightFormat.MAX_TEXT_LENGTH}")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(if (linkId == null) "Kein Tagebucheintrag verknüpft"
                        else "Verknüpft: ${journal.firstOrNull { it.id == linkId }?.let { formatInsightDate(it.createdAt) } ?: "Eintrag nicht verfügbar"}",
                        style = MaterialTheme.typography.bodySmall)
                    if (journalLoaded && journal.isNotEmpty()) {
                        TextButton(onClick = { choosingLink = true }) {
                            Text("Optional Tagebucheintrag wählen")
                        }
                    }
                    if (linkId != null) {
                        TextButton(onClick = { linkId = null }) {
                            Text("Verknüpfung entfernen")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !state.busy && state.loaded && state.error == null &&
                        draft.isNotBlank(),
                    onClick = {
                        onSave(draft, editing?.id, linkId)
                        closeEditor()
                    }
                ) { Text("Ausdrücklich speichern") }
            },
            dismissButton = {
                TextButton(onClick = ::closeEditor) { Text("Abbrechen") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Meine Erkenntnisse", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            Text("Ein privater Platz für Gedanken, die du selbst festhalten möchtest. Nicht die App entscheidet, welche Bedeutung sie haben.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium)
        }
        if (state.error != null) {
            item {
                Card {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Erkenntnisse nicht verfügbar", fontWeight = FontWeight.SemiBold)
                        Text(state.error)
                        OutlinedButton(onClick = onReload, enabled = !state.busy) {
                            Text("Erneut versuchen")
                        }
                    }
                }
            }
        } else if (!state.loaded || state.busy) {
            item {
                Text("Erkenntnisse werden sicher geladen oder aktualisiert …")
            }
        } else {
            if (state.message != null) item { Text(state.message) }
            item {
                Button(onClick = { startEditor() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Eigene Erkenntnis schreiben")
                }
            }
            item {
                Card {
                    Column(Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text("Freiwilliger Reflexionsimpuls",
                            fontWeight = FontWeight.SemiBold)
                        Text(prompts[promptIndex])
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = {
                                promptIndex = (promptIndex + 1) % prompts.size
                            }) { Text("Frage überspringen") }
                            TextButton(onClick = { startEditor() }) { Text("Gedanken notieren") }
                        }
                        Text("Antworten werden nur gespeichert, wenn du das ausdrücklich auswählst.",
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = query, onValueChange = { query = it.take(120) },
                    label = { Text("Erkenntnisse durchsuchen") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = !showArchived, onClick = { showArchived = false },
                        label = { Text("Aktuell") })
                    FilterChip(selected = showArchived, onClick = { showArchived = true },
                        label = { Text("Archiviert") })
                }
            }
            val filtered = state.items.filter { it.archived == showArchived &&
                it.text.contains(query.trim(), ignoreCase = true) }
                .sortedWith(compareByDescending<PersonalInsight> { it.updatedAt }.thenBy { it.id })
            if (filtered.isEmpty()) {
                item {
                    Text(if (query.isNotBlank()) "Keine Erkenntnisse für diese Suche."
                        else if (showArchived) "Noch keine archivierten Erkenntnisse."
                        else "Hier ist noch Platz für deine eigenen Gedanken. Du musst nichts festhalten.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            items(filtered, key = { it.id }) { item ->
                Card {
                    Column(Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(formatInsightDate(item.updatedAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(item.text, style = MaterialTheme.typography.bodyLarge)
                        if (item.journalId != null) {
                            if (journalLoaded && journal.any { it.id == item.journalId }) {
                                TextButton(onClick = { onOpenJournalEntry(item.journalId) }) {
                                    Text("Verknüpften Tagebucheintrag ansehen")
                                }
                            } else {
                                Text("Verknüpfter Eintrag nicht verfügbar",
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        OutlinedButton(onClick = { startEditor(item) },
                            modifier = Modifier.fillMaxWidth()) {
                            Text("Bearbeiten")
                        }
                        OutlinedButton(onClick = { onArchive(item.id, !item.archived) },
                            modifier = Modifier.fillMaxWidth()) {
                            Text(if (item.archived) "Aus Archiv holen" else "Archivieren")
                        }
                        TextButton(onClick = { confirmDelete = item.id }) {
                            Text("Erkenntnis löschen")
                        }
                    }
                }
            }
            item {
                Text("Deine Erkenntnisse werden nur lokal verschlüsselt gespeichert. Bitte sichere Tagebuch und Erkenntnisse gemeinsam über die verschlüsselte Sicherungsfunktion.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun formatInsightDate(epoch: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.GERMANY)
        .format(Date(epoch))
