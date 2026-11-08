package com.valoon4.communicationtrainer.inference

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
 * Separate 4-turn online conversation pilot. D03 chats and Room are not used.
 * Draft/turns persist across Compose navigation only while app process lives.
 */
@Composable
fun D05TrainingCard() {
    val authenticated by ChatGptPlanProbe.authenticated.collectAsState()
    val catalog by ChatGptPlanProbe.models.collectAsState()
    val selectedScenario by D05TrainingSession.scenarioId.collectAsState()
    val selectedModel by D05TrainingSession.chosenModel.collectAsState()
    val draft by D05TrainingSession.draft.collectAsState()
    val turns by D05TrainingSession.turns.collectAsState()
    val consent by D05TrainingSession.consent.collectAsState()
    val working by D05TrainingSession.busy.collectAsState()
    val status by D05TrainingSession.status.collectAsState()
    val latency by D05TrainingSession.lastLatencyMs.collectAsState()
    var expandedModel by remember { mutableStateOf(false) }

    Card(shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("D05.0 · Echte Trainingsgespräche (Pilot)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Text(
                "Übe einen kurzen Dialog mit einer fiktiven KI-Person. " +
                    "Jedes Szenario beginnt mit einer konkreten Bitte oder Aussage " +
                    "(als vorgegebener Text, ohne KI-Kosten). " +
                    "Die anschließende KI-Antwort berücksichtigt den Übungsverlauf. " +
                    "Du gibst jede Online-Nachricht separat frei. " +
                    "Die alten lokalen Chats und Entwürfe werden nicht gelesen oder verändert.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Achtung: Diese neue Trainingssitzung wird noch nicht gespeichert. " +
                    "Wenn Android die App beendet, gehen die Pilot-Nachrichten verloren. " +
                    "Verwende keine vertraulichen oder identifizierenden Informationen.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
            Text("Übung auswählen", fontWeight = FontWeight.SemiBold)
            D05TrainingSession.scenarios.forEach { option ->
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !working,
                    onClick = { D05TrainingSession.selectScenario(option.id) }
                ) {
                    Text((if (selectedScenario == option.id) "✓ " else "") + option.title)
                }
            }
            val scenario = D05TrainingSession.scenarios.first { it.id == selectedScenario }
            Text(scenario.setup, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            D051LearningGoalControls(selectedScenario, working)

            if (!authenticated) {
                Text("Melde dich zuerst oben mit ChatGPT an, um eine Trainingssitzung zu starten.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !working,
                    onClick = { ChatGptPlanProbe.loadAvailableModels() }
                ) { Text("Verfügbare ChatGPT-Modelle laden") }

                if (catalog.isNotEmpty()) {
                    Column {
                        OutlinedButton(modifier = Modifier.fillMaxWidth(), enabled = !working,
                            onClick = { expandedModel = true }) {
                            Text("Modell: " + (catalog.firstOrNull { it.slug == selectedModel }
                                ?.displayName ?: "Bitte auswählen"))
                        }
                        DropdownMenu(expanded = expandedModel, onDismissRequest = { expandedModel = false }) {
                            catalog.forEach { model ->
                                DropdownMenuItem(
                                    text = { Text(model.displayName) },
                                    onClick = {
                                        D05TrainingSession.selectModel(model.slug)
                                        expandedModel = false
                                    }
                                )
                            }
                        }
                    }
                }

                turns.forEach { turn ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = if (turn.role == "user")
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                when {
                                    turn.role == "user" -> "Du"
                                    turn.isScriptedOpening -> "Trainingspartner · vorgegebener Einstieg"
                                    else -> "Trainingspartner · KI-Antwort"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(turn.text)
                        }
                    }
                }
                Text("${turns.count { it.role == "user" }} / ${D05TrainingSession.MAX_USER_TURNS} Trainingsrunden",
                    style = MaterialTheme.typography.labelMedium)
                val canCompose = !working &&
                    turns.count { it.role == "user" } < D05TrainingSession.MAX_USER_TURNS
                OutlinedTextField(
                    value = draft,
                    onValueChange = D05TrainingSession::updateDraft,
                    enabled = canCompose,
                    label = { Text("Deine Antwort an den Trainingspartner") },
                    supportingText = { Text("${draft.length}/600 Zeichen · Nicht dauerhaft gespeichert") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 5
                )
                Row(verticalAlignment = Alignment.Top) {
                    Checkbox(
                        checked = consent,
                        enabled = canCompose,
                        onCheckedChange = D05TrainingSession::setConsent
                    )
                    Text(
                        "Ich erlaube für diese Runde die Online-Übertragung meiner " +
                            "eingegebenen Nachricht und des bisherigen Übungsdialogs an ChatGPT. " +
                            "Ich habe Credits und App-Limits in ChatGPT geprüft. " +
                            "Die App kann zusätzliche Kosten nicht selbst ausschließen.",
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canCompose && consent && selectedModel.isNotBlank() && draft.isNotBlank(),
                    onClick = { D05TrainingSession.sendConfirmedTurn() }
                ) { Text(if (working) "Antwort läuft …" else "Nachricht freigeben und senden") }
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !working,
                    onClick = { D05TrainingSession.clearConversation() }
                ) { Text("Neues Training beginnen / Verlauf verwerfen") }
            }
            Text(status, style = MaterialTheme.typography.bodySmall)
            latency?.let { Text("Letzte Antwortzeit: ${it} ms", style = MaterialTheme.typography.labelSmall) }
            D051ReflectionControls(selectedScenario, working)
            Text(
                "Pilot: keine Qualitätsbewertung, kein Coach-Feedback und noch keine " +
                    "Wiederaufnahme nach App-Neustart. Kein automatischer Retry.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
