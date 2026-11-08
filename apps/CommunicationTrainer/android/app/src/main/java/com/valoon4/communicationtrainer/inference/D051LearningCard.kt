package com.valoon4.communicationtrainer.inference

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    val objective = D051LearningGuide.objectiveFor(scenarioId)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Dein Lernziel", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Text(objective.headline, fontWeight = FontWeight.Medium)
        Text(objective.cue, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Schwierigkeit", fontWeight = FontWeight.Medium)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            D051LearningGuide.Level.entries.forEach { option ->
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    enabled = !working,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 7.dp),
                    onClick = { D051LearningGuide.selectLevel(option) }
                ) {
                    Text((if (option == level) "✓ " else "") + option.label,
                        style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Text(level.explanation + " Ein Wechsel startet die Übung neu.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        HorizontalDivider()
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
