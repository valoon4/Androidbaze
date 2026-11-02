package com.valoon4.emotionskompass.feature.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.valoon4.emotionskompass.core.insights.ExperimentEvaluation
import com.valoon4.emotionskompass.core.insights.ExperimentStatus
import com.valoon4.emotionskompass.core.insights.StrategyExperiment
import com.valoon4.emotionskompass.core.navigation.StrategyExperimentUiState

/** No private editor field is rememberSaveable, persisted to logs or autosaved. */
@Composable
fun StrategyExperimentsScreen(
    state: StrategyExperimentUiState,
    onReload: () -> Unit,
    onSave: (StrategyExperiment) -> Unit,
    onArchive: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var showArchived by rememberSaveable { mutableStateOf(false) }
    var editor by remember { mutableStateOf<StrategyExperiment?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var plan by remember { mutableStateOf("") }
    var caution by remember { mutableStateOf("") }
    var reflection by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(ExperimentStatus.OPEN) }
    var evaluation by remember { mutableStateOf<ExperimentEvaluation?>(null) }
    var waitingForSave by remember { mutableStateOf(false) }
    var confirmDiscard by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<StrategyExperiment?>(null) }

    fun resetEditor() {
        showEditor = false
        editor = null
        title = ""
        plan = ""
        caution = ""
        reflection = ""
        status = ExperimentStatus.OPEN
        evaluation = null
        confirmDiscard = false
        waitingForSave = false
    }

    fun openEditor(item: StrategyExperiment?) {
        editor = item
        title = item?.customTitle ?: item?.sourceTitleFallback.orEmpty()
        plan = item?.planText.orEmpty()
        caution = item?.cautionText.orEmpty()
        reflection = item?.reflectionText.orEmpty()
        status = item?.status ?: ExperimentStatus.OPEN
        evaluation = item?.evaluation
        showEditor = true
        waitingForSave = false
        confirmDiscard = false
    }

    LaunchedEffect(state.loaded, state.error) {
        if (!state.loaded || state.error != null) {
            resetEditor()
            pendingDelete = null
            query = ""
        }
    }
    LaunchedEffect(state.message) {
        if (waitingForSave && state.message in listOf("Idee gespeichert.", "Idee aktualisiert.")) {
            resetEditor()
        }
    }

    if (confirmDiscard && showEditor) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            title = { Text("Entwurf verwerfen?") },
            text = { Text("Deine noch nicht gespeicherten Angaben werden verworfen. Vorhandene Ideen bleiben unverändert.") },
            confirmButton = {
                TextButton(onClick = { resetEditor() }) { Text("Entwurf verwerfen") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDiscard = false }) { Text("Weiter bearbeiten") }
            }
        )
    }

    pendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Idee endgültig löschen?") },
            text = { Text("Nur diese persönliche Idee wird gelöscht. Tagebuch und eigene Erkenntnisse bleiben erhalten. Es gibt keinen Papierkorb.") },
            confirmButton = {
                TextButton(
                    enabled = state.loaded && !state.busy && state.error == null,
                    onClick = { pendingDelete = null; onDelete(item.id) },
                    modifier = Modifier.testTag("experiment-confirm-delete")
                ) { Text("Endgültig löschen") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Abbrechen") }
            }
        )
    }

    if (showEditor && state.loaded && state.error == null) {
        AlertDialog(
            onDismissRequest = { if (!state.busy) confirmDiscard = true },
            title = {
                Text(if (editor == null) "Eigene Idee notieren" else "Eigene Idee bearbeiten")
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 460.dp)
                        .testTag("experiment-editor-list"),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("Du entscheidest selbst, ob du eine Idee ausprobieren möchtest. Auch Nicht-Ausprobieren ist völlig in Ordnung. Nichts wird automatisch gespeichert.",
                            style = MaterialTheme.typography.bodyMedium)
                    }
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = {
                                if (editor?.sourceStrategyId == null &&
                                    it.length <= StrategyExperiment.MAX_TITLE) title = it
                            },
                            enabled = editor?.sourceStrategyId == null && !state.busy,
                            label = { Text("Meine Idee") },
                            supportingText = { Text("${title.length} / ${StrategyExperiment.MAX_TITLE} Zeichen") },
                            modifier = Modifier.fillMaxWidth().testTag("experiment-title"),
                            minLines = 1,
                            maxLines = 3
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = plan,
                            onValueChange = { if (it.length <= StrategyExperiment.MAX_PLAN) plan = it },
                            enabled = !state.busy,
                            label = { Text("Was könnte ich ausprobieren? (freiwillig)") },
                            supportingText = { Text("${plan.length} / ${StrategyExperiment.MAX_PLAN} Zeichen") },
                            modifier = Modifier.fillMaxWidth().testTag("experiment-plan"),
                            minLines = 2
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = caution,
                            onValueChange = { if (it.length <= StrategyExperiment.MAX_CAUTION) caution = it },
                            enabled = !state.busy,
                            label = { Text("Was sollte ich dabei beachten? (freiwillig)") },
                            modifier = Modifier.fillMaxWidth().testTag("experiment-caution"),
                            minLines = 2
                        )
                    }
                    item {
                        Text("Wo stehe ich damit?", fontWeight = FontWeight.SemiBold)
                        Text("Das ist nur eine eigene Notiz – keine Bewertung oder Pflicht.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ExperimentStatus.entries.forEach { choice ->
                                FilterChip(
                                    selected = status == choice,
                                    onClick = { status = choice },
                                    enabled = !state.busy,
                                    label = { Text(choice.label()) },
                                    modifier = Modifier.testTag("experiment-status-${choice.name}")
                                )
                            }
                        }
                    }
                    item {
                        Text("Meine Einschätzung (optional)", fontWeight = FontWeight.SemiBold)
                        Text("„Keine Einschätzung“ ist ebenso gültig. Die App bewertet deine Strategie nicht.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = evaluation == null,
                                onClick = { evaluation = null },
                                enabled = !state.busy,
                                label = { Text("Keine Einschätzung") },
                                modifier = Modifier.testTag("experiment-evaluation-none")
                            )
                            ExperimentEvaluation.entries.forEach { choice ->
                                FilterChip(
                                    selected = evaluation == choice,
                                    onClick = { evaluation = choice },
                                    enabled = !state.busy,
                                    label = { Text(choice.label()) },
                                    modifier = Modifier.testTag("experiment-evaluation-${choice.name}")
                                )
                            }
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = reflection,
                            onValueChange = {
                                if (it.length <= StrategyExperiment.MAX_REFLECTION) reflection = it
                            },
                            enabled = !state.busy,
                            label = { Text("Was habe ich selbst bemerkt? (freiwillig)") },
                            supportingText = { Text("Du musst nichts Positives erlebt haben.") },
                            modifier = Modifier.fillMaxWidth().testTag("experiment-reflection"),
                            minLines = 3
                        )
                    }
                    item {
                        Text("Nur durch „Ausdrücklich speichern“ wird dieser Entwurf lokal verschlüsselt abgelegt. Die Idee wird in deiner EKB3-Gesamtsicherung mitgesichert.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = state.loaded && !state.busy && !waitingForSave &&
                        state.error == null && title.trim().isNotEmpty() &&
                        (editor != null || state.entries.size < StrategyExperiment.MAX_EXPERIMENTS),
                    onClick = {
                        val now = System.currentTimeMillis()
                        val item = editor
                        val next = if (item == null) {
                            StrategyExperiment.fromOwnIdea(title.trim(), now)
                        } else {
                            item.copy(
                                updatedAtUtcMillis = maxOf(now, item.updatedAtUtcMillis),
                                customTitle = if (item.sourceStrategyId == null) title.trim()
                                    else null
                            )
                        }
                        waitingForSave = true
                        onSave(next.copy(
                            planText = plan, cautionText = caution, status = status,
                            evaluation = evaluation, reflectionText = reflection
                        ))
                    },
                    modifier = Modifier.testTag("experiment-explicit-save")
                ) { Text(if (state.busy || waitingForSave) "Speichere …" else "Ausdrücklich speichern") }
            },
            dismissButton = {
                TextButton(
                    onClick = { confirmDiscard = true },
                    enabled = !state.busy,
                    modifier = Modifier.testTag("experiment-cancel")
                ) { Text("Abbrechen") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("experiments-list"),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Deine Ideen zum Ausprobieren",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() }.testTag("experiments-heading"))
            Text("Du entscheidest selbst, ob du etwas versuchen möchtest. Du kannst Ideen sammeln, ohne sie umzusetzen, und später festhalten, was für dich passend war – oder nicht.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (!state.loaded || state.error != null) {
            item {
                Text(
                    state.error ?: "Deine persönlichen Ideen werden sicher geladen …",
                    color = if (state.error == null) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("experiments-load-state")
                )
                OutlinedButton(
                    onClick = onReload, enabled = !state.busy,
                    modifier = Modifier.fillMaxWidth().testTag("experiments-retry")
                ) { Text("Erneut sicher laden") }
            }
        } else {
            item {
                Button(
                    onClick = { openEditor(null) },
                    enabled = !state.busy && state.entries.size < StrategyExperiment.MAX_EXPERIMENTS,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                        .testTag("experiment-create")
                ) { Text("Eigene Idee notieren") }
                if (state.entries.size >= StrategyExperiment.MAX_EXPERIMENTS) {
                    Text("Die Sammlung ist voll. Du kannst vorhandene Ideen bearbeiten oder nach Bestätigung löschen.",
                        style = MaterialTheme.typography.bodySmall)
                }
                state.message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
            }
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it.take(140) },
                    label = { Text("Meine Ideen durchsuchen") },
                    modifier = Modifier.fillMaxWidth().testTag("experiment-search"),
                    singleLine = true
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = !showArchived, onClick = { showArchived = false },
                        label = { Text("Aktuelle") })
                    FilterChip(selected = showArchived, onClick = { showArchived = true },
                        label = { Text("Archiv") })
                }
            }
            val displayed = state.entries.filter { item ->
                item.archived == showArchived &&
                    (query.isBlank() ||
                        item.customTitle.orEmpty().contains(query.trim(), ignoreCase = true) ||
                        item.sourceTitleFallback.orEmpty().contains(query.trim(), ignoreCase = true) ||
                        item.planText.contains(query.trim(), ignoreCase = true))
            }
            if (displayed.isEmpty()) {
                item {
                    Card {
                        Column(Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                when {
                                    query.isNotBlank() -> "Keine passenden Ideen gefunden."
                                    showArchived -> "Dein Archiv ist noch leer."
                                    else -> "Hier hast du noch keine Idee gespeichert."
                                },
                                fontWeight = FontWeight.SemiBold
                            )
                            Text("Du kannst eine eigene Möglichkeit notieren, ohne sie ausprobieren zu müssen.")
                        }
                    }
                }
            }
            items(displayed, key = { it.id }) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(item.customTitle ?: item.sourceTitleFallback ?: "Eigene Idee",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold)
                        Text("Status: ${item.status.label()} · Einschätzung: ${item.evaluation?.label() ?: "Keine Einschätzung"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (item.planText.isNotBlank()) Text(item.planText)
                        if (item.reflectionText.isNotBlank()) {
                            Text("Meine Beobachtung", fontWeight = FontWeight.SemiBold)
                            Text(item.reflectionText)
                        }
                        OutlinedButton(
                            enabled = !state.busy,
                            onClick = { openEditor(item) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                                .testTag("experiment-edit-${item.id}")
                        ) { Text("Bearbeiten und reflektieren") }
                        OutlinedButton(
                            enabled = !state.busy,
                            onClick = { onArchive(item.id, !item.archived) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                        ) { Text(if (item.archived) "Aus Archiv zurückholen" else "Archivieren") }
                        TextButton(
                            enabled = !state.busy,
                            onClick = { pendingDelete = item },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                                .testTag("experiment-delete-${item.id}")
                        ) { Text("Löschen …") }
                    }
                }
            }
        }
    }
}

private fun ExperimentStatus.label(): String = when (this) {
    ExperimentStatus.OPEN -> "Offen"
    ExperimentStatus.SAVED_FOR_LATER -> "Vorgemerkt"
    ExperimentStatus.TRIED -> "Ausprobiert"
    ExperimentStatus.NOT_TRIED -> "Bewusst nicht ausprobiert"
    ExperimentStatus.CLOSED -> "Abgeschlossen"
}

private fun ExperimentEvaluation.label(): String = when (this) {
    ExperimentEvaluation.HELPFUL -> "Hilfreich"
    ExperimentEvaluation.PARTLY -> "Teilweise"
    ExperimentEvaluation.NOT_FITTING -> "Für mich nicht passend"
    ExperimentEvaluation.UNCLEAR -> "Noch unklar"
}
