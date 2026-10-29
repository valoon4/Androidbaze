package com.valoon4.emotionskompass.feature.journal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.valoon4.emotionskompass.core.journal.JournalEntry
import com.valoon4.emotionskompass.core.navigation.JournalUiState
import com.valoon4.emotionskompass.core.model.EmotionCatalog
import com.valoon4.emotionskompass.core.model.NeedCatalog
import com.valoon4.emotionskompass.core.checkin.CheckInDraft
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JournalScreen(
    state: JournalUiState,
    emotions: EmotionCatalog,
    needs: NeedCatalog,
    onStart: () -> Unit,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit,
    onDeleteAll: () -> Unit,
    onReload: () -> Unit
) {
    var selectedId by remember { mutableStateOf<String?>(null) }
    var deleteId by remember { mutableStateOf<String?>(null) }
    var confirmDeleteAll by remember { mutableStateOf(false) }

    if (deleteId != null) {
        val id = deleteId!!
        AlertDialog(
            onDismissRequest = { deleteId = null },
            title = { Text("Eintrag endgültig löschen?") },
            text = { Text("Dieser Eintrag wird aus dem verschlüsselten Tagebuch entfernt. Es gibt keinen Papierkorb oder Export.") },
            confirmButton = {
                TextButton(onClick = {
                    deleteId = null
                    if (selectedId == id) selectedId = null
                    onDelete(id)
                }) { Text("Eintrag löschen") }
            },
            dismissButton = { TextButton(onClick = { deleteId = null }) { Text("Abbrechen") } }
        )
    }

    if (confirmDeleteAll) {
        AlertDialog(
            onDismissRequest = { confirmDeleteAll = false },
            title = { Text("Alle Tagebucheinträge löschen?") },
            text = { Text("Die gesamte verschlüsselte Tagebuchsammlung wird durch einen leeren verschlüsselten Datenbestand ersetzt. Diese Aktion kann nicht rückgängig gemacht werden.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDeleteAll = false
                    selectedId = null
                    onDeleteAll()
                }) { Text("Alle endgültig löschen") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteAll = false }) { Text("Abbrechen") }
            }
        )
    }

    val selected = state.entries.firstOrNull { it.id == selectedId }
    if (selected != null) {
        JournalDetail(
            entry = selected, emotions = emotions, needs = needs,
            onBack = { selectedId = null },
            onEdit = { onEdit(selected.id) },
            onDelete = { deleteId = selected.id }
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Dein Tagebuch", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            Text("Nur auf diesem Gerät. Einträge werden mit Android Keystore und AES-GCM verschlüsselt gespeichert.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (state.error != null) {
            item {
                Card(colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Column(Modifier.fillMaxWidth().padding(15.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Tagebuch nicht verfügbar", fontWeight = FontWeight.SemiBold)
                        Text(state.error)
                        Text("Vorhandene verschlüsselte Daten wurden nicht automatisch ersetzt.",
                            style = MaterialTheme.typography.bodySmall)
                        OutlinedButton(onClick = onReload, enabled = !state.busy) {
                            Text("Erneut versuchen")
                        }
                    }
                }
            }
        }
        item {
            Button(onClick = onStart, modifier = Modifier.fillMaxWidth(),
                enabled = !state.busy && state.error == null) {
                Text("Neuen Check-in beginnen")
            }
        }
        if (state.busy) {
            item { Text("Tagebuch wird verarbeitet …",
                color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else if (state.entries.isEmpty() && state.error == null) {
            item {
                Card(colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(Modifier.fillMaxWidth().padding(19.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text("Noch keine gespeicherten Einträge", fontWeight = FontWeight.SemiBold)
                        Text("Wenn du möchtest, kannst du einen Check-in am Ende ausdrücklich speichern. Du kannst ihn auch weiterhin verwerfen.")
                    }
                }
            }
        }
        items(state.entries, key = { it.id }) { entry ->
            Card(onClick = { selectedId = entry.id },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(formatTimestamp(entry.createdAt),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary)
                    val words = entry.draft.emotions.keys.mapNotNull(emotions::find)
                        .joinToString(" · ") { it.name }
                    Text(if (words.isBlank()) "Meine Momentaufnahme" else words,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    val summary = entry.draft.insight.ifBlank {
                        entry.draft.note.ifBlank { entry.draft.situation }
                    }
                    if (summary.isNotBlank()) Text(summary,
                        style = MaterialTheme.typography.bodySmall, maxLines = 2,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Ansehen und bearbeiten",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        if (state.entries.isNotEmpty() && state.error == null) {
            item {
                HorizontalDivider()
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { confirmDeleteAll = true },
                    enabled = !state.busy
                ) { Text("Alle Tagebucheinträge löschen") }
            }
        }
        item {
            Text("Keine Cloud und kein automatischer Export. Wenn du die App-Daten löschst oder das Gerät wechselst, sind die Einträge nicht wiederherstellbar. Die Verschlüsselung ersetzt keine unabhängige Sicherheitsprüfung.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun JournalDetail(
    entry: JournalEntry,
    emotions: EmotionCatalog,
    needs: NeedCatalog,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val draft = entry.draft
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            TextButton(onClick = onBack) { Text("Zurück zum Tagebuch") }
            Text("Eintrag vom ${formatTimestamp(entry.createdAt)}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            if (entry.updatedAt > entry.createdAt) {
                Text("Zuletzt bearbeitet: ${formatTimestamp(entry.updatedAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("Dieser Eintrag ist nur auf deinem Gerät gespeichert.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Text("Meine Gefühle", fontWeight = FontWeight.SemiBold)
            if (draft.emotions.isEmpty()) Text("Keine Begriffe ausgewählt")
            draft.emotions.forEach { (id, intensity) ->
                val name = emotions.find(id)?.name ?: "Begriff: $id"
                JournalField(name, intensity?.let { "$it / 10" } ?: "Ohne Intensität")
            }
        }
        if (draft.bodyCues.isNotEmpty()) {
            item { JournalField("Körperwahrnehmungen",
                draft.bodyCues.joinToString(" · ") { it.label }) }
        }
        if (draft.area != null) item { JournalField("Lebensbereich", draft.area.label) }
        if (draft.note.isNotBlank()) item { JournalField("Gedanken", draft.note) }
        if (draft.situation.isNotBlank()) item { JournalField("Meine Situation", draft.situation) }
        if (draft.interpretation.isNotBlank()) item { JournalField("Meine Sicht", draft.interpretation) }
        if (draft.needs.isNotEmpty()) {
            item {
                JournalField("Was mir wichtig sein könnte", draft.needs.joinToString(" · ") { id ->
                    if (id == CheckInDraft.UNKNOWN_NEED_ID) "Ich weiß es noch nicht"
                    else needs.find(id)?.name ?: "Bedürfnis: $id"
                })
            }
        }
        if (draft.insight.isNotBlank()) item { JournalField("Meine Erkenntnis", draft.insight) }
        if (draft.nextStepIdea != null || draft.nextStepNote.isNotBlank()) {
            item {
                JournalField("Mein möglicher nächster Schritt", listOfNotNull(
                    draft.nextStepIdea?.label,
                    draft.nextStepNote.takeIf { it.isNotBlank() }
                ).joinToString(" · "))
            }
        }
        item {
            HorizontalDivider()
            Button(onClick = onEdit, modifier = Modifier.fillMaxWidth()) {
                Text("Eintrag bearbeiten")
            }
            Spacer(Modifier.size(8.dp))
            OutlinedButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) {
                Text("Eintrag löschen")
            }
        }
    }
}

@Composable
private fun JournalField(label: String, text: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(15.dp)) {
        Column(Modifier.fillMaxWidth().padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary)
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun formatTimestamp(timestamp: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.GERMANY)
        .format(Date(timestamp))
