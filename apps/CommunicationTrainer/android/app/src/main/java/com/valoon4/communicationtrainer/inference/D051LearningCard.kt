package com.valoon4.communicationtrainer.inference

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * D05.1.0 – clear goals and optional offline-only self-reflection.
 * This is not AI assessment and never submits a model request.
 */
@Composable
fun D051LearningGoalControls(scenarioId: String, working: Boolean) {
    val level by D051LearningGuide.level.collectAsState()
    val turns by D05TrainingSession.turns.collectAsState()
    val draft by D05TrainingSession.draft.collectAsState()
    var menuOpen by remember { mutableStateOf(false) }
    var confirmChange by remember { mutableStateOf<D051LearningGuide.Level?>(null) }
    val objective = D051LearningGuide.objectiveFor(scenarioId)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Dein Lernziel", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Text(objective.headline, fontWeight = FontWeight.Medium)
        Text(objective.cue, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Schwierigkeit", fontWeight = FontWeight.Medium)
        Column {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = !working,
                onClick = { menuOpen = true }
            ) { Text(level.label + " · Schwierigkeitsgrad ändern") }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                D051LearningGuide.Level.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label + " – " + option.explanation) },
                        onClick = {
                            menuOpen = false
                            if (level != option) {
                                if (turns.any { it.role == "user" } || draft.isNotBlank())
                                    confirmChange = option
                                else D051LearningGuide.selectLevel(option)
                            }
                        }
                    )
                }
            }
        }
        Text(level.explanation + " Beim Wechsel wird eine laufende Übung nach Bestätigung verworfen.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        HorizontalDivider()
        confirmChange?.let { next ->
            AlertDialog(
                onDismissRequest = { confirmChange = null },
                title = { Text("Schwierigkeitsgrad wechseln?") },
                text = { Text("Dein bisheriger Pilotdialog und ein möglicher Entwurf gehen verloren. Es wird keine KI-Anfrage gesendet.") },
                confirmButton = {
                    TextButton(onClick = {
                        D051LearningGuide.selectLevel(next)
                        confirmChange = null
                    }) { Text("Verwerfen und wechseln") }
                },
                dismissButton = {
                    TextButton(onClick = { confirmChange = null }) { Text("Abbrechen") }
                }
            )
        }
    }
}

@Composable
fun D051ReflectionControls(scenarioId: String, working: Boolean) {
    val turns by D05TrainingSession.turns.collectAsState()
    val selections by D051LocalReflection.selectedObservations.collectAsState()
    val note by D051LocalReflection.note.collectAsState()
    val acknowledged by D051LocalReflection.acknowledged.collectAsState()
    val objective = D051LearningGuide.objectiveFor(scenarioId)
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        HorizontalDivider()
        Text("Deine Reflexion (freiwillig)", style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold)
        if (turns.none { it.role == "user" }) {
            Text("Nach deiner ersten KI-Antwort kannst du hier eigene Beobachtungen festhalten.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Text(
                "Keine KI-Bewertung, keine Noten: Nur du entscheidest, was dir gelungen ist.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            objective.observationChoices.forEachIndexed { i, statement ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = i in selections,
                        enabled = !working,
                        onCheckedChange = { D051LocalReflection.toggle(i, scenarioId, it) }
                    )
                    Text(statement, modifier = Modifier.padding(start = 4.dp),
                        style = MaterialTheme.typography.bodySmall)
                }
            }
            OutlinedTextField(
                value = note,
                onValueChange = D051LocalReflection::updateNote,
                enabled = !working,
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                label = { Text("Was hast du selbst bemerkt?") },
                supportingText = { Text(objective.reflectionPrompt + "  ${note.length}/400 Zeichen") }
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !working && !acknowledged &&
                    (selections.isNotEmpty() || note.isNotBlank()),
                onClick = { D051LocalReflection.confirm() }
            ) { Text("Reflexion für diese Sitzung festhalten") }
            if (acknowledged) {
                Text(
                    "Deine eigene Beobachtung ist für diese Sitzung vorgemerkt. " +
                        "Sie wird nicht gesendet, bewertet oder dauerhaft gespeichert.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
