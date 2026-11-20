package com.valoon4.communicationtrainer.inference

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
 * D05.0 first real roleplay. This UI is intentionally in the Practice tab,
 * separate from all D03 saved offline chats and drafts.
 */
@Composable
fun D05TrainingCard() {
    val session by D05TrainingSession.state.collectAsState()
    val authenticated by ChatGptPlanProbe.authenticated.collectAsState()
    val models by ChatGptPlanProbe.models.collectAsState()
    val loadingModels by ChatGptPlanProbe.busy.collectAsState()
    var chosenScenario by remember { mutableStateOf(D05Scenario.WORKLOAD) }
    var chosenModel by remember { mutableStateOf("") }
    var scenarioMenu by remember { mutableStateOf(false) }
    var modelMenu by remember { mutableStateOf(false) }
    var confirmed by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("D05 · KI-Trainingsgespräch", style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold)
            Text(
                "Probiere ein fiktives Gespräch mit einem KI-Rollenspielpartner aus. " +
                    "Du entscheidest bei jeder Nachricht über den Online-Versand. " +
                    "Deine bisherigen lokalen Chats und Entwürfe bleiben unverändert.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!session.active && !session.ended) {
                if (!authenticated) {
                    Text(
                        "Melde dich oben unter D04.2 mit ChatGPT an. " +
                            "Die D05-Übung beginnt erst nach deiner gesonderten Zustimmung.",
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !loadingModels,
                        onClick = { ChatGptPlanProbe.loadAvailableModels() }
                    ) { Text("Verfügbare ChatGPT-Modelle laden") }
                    Box {
                        OutlinedButton(onClick = { scenarioMenu = true },
                            modifier = Modifier.fillMaxWidth()) {
                            Text("Situation: " + chosenScenario.displayName)
                        }
                        DropdownMenu(expanded = scenarioMenu, onDismissRequest = { scenarioMenu = false }) {
                            D05Scenario.entries.forEach { scenario ->
                                DropdownMenuItem(text = { Text(scenario.displayName) }, onClick = {
                                    chosenScenario = scenario
                                    scenarioMenu = false
                                })
                            }
                        }
                    }
                    Text(chosenScenario.description, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (models.isNotEmpty()) {
                        Box {
                            OutlinedButton(onClick = { modelMenu = true },
                                modifier = Modifier.fillMaxWidth()) {
                                val selection = models.find { it.slug == chosenModel }
                                Text("Modell: " + (selection?.displayName ?: "Bitte auswählen"))
                            }
                            DropdownMenu(expanded = modelMenu, onDismissRequest = { modelMenu = false }) {
                                models.forEach { model ->
                                    DropdownMenuItem(
                                        text = { Text(model.displayName) },
                                        onClick = { chosenModel = model.slug; modelMenu = false }
                                    )
                                }
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.Top) {
                        Checkbox(checked = confirmed, onCheckedChange = { confirmed = it })
                        Text(
                            "Ich stimme zu, dass ausschließlich meine Nachrichten aus dieser neuen " +
                                "Übung und der zugehörige Übungsverlauf an ChatGPT gesendet werden. " +
                                "Bis zu fünf Anfragen über mein Plan-Kontingent. Keine API-Ausweichkosten. " +
                                "Ich habe die Credit-Freigabe in ChatGPT geprüft und nutze hier keine echten Namen oder privaten Details.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                    Button(
                        onClick = {
                            if (D05TrainingSession.start(chosenScenario, chosenModel)) draft = ""
                        },
                        enabled = confirmed && chosenModel.isNotBlank() &&
                            models.any { it.slug == chosenModel } && !loadingModels,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Fiktive Online-Übung starten") }
                }
            }
            if (session.active) {
                Text(
                    session.scenario?.displayName ?: "Übung",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text("Gesprächspartner (fiktiv):", style = MaterialTheme.typography.labelMedium)
                Text(
                    session.scenario?.opening ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
                for (turn in session.turns) {
                    Surface(
                        color = if (turn.role == "user")
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                if (turn.role == "user") "DU"
                                else "KI-ROLLENSPIEL · FIKTIV",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(turn.text, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                if (session.working) {
                    Text("Antwort wird online erzeugt …", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else if (session.remaining > 0) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { value ->
                            // Input bound prevents accidental large transfers.
                            if (value.length <= D05TrainingProtocol.MAX_USER_CHARS + 1) draft = value
                        },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Deine Antwort an die fiktive Person") },
                        supportingText = {
                            Text("${draft.length}/${D05TrainingProtocol.MAX_USER_CHARS} Zeichen. Nur diese Übung wird online gesendet.")
                        }
                    )
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = D05TrainingProtocol.validUserMessage(draft),
                        onClick = {
                            if (D05TrainingSession.send(draft)) draft = ""
                        }
                    ) { Text("Diese Nachricht online senden") }
                }
                Text(
                    "${session.sentCount}/${D05TrainingProtocol.MAX_MESSAGES} bewusste Anfragen in dieser Übung.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !session.working,
                    onClick = { D05TrainingSession.end(); draft = "" }
                ) { Text("Übung beenden und Verlauf löschen") }
            }
            Text(
                session.status,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
            session.elapsedMillis?.let { ms ->
                Text("Letzte vollständig empfangene Antwort: ${ms} ms.",
                    style = MaterialTheme.typography.bodySmall)
            }
            Text(
                "D05-Vorschau: flüchtiger Verlauf, kein Speichern, kein Coaching-Urteil. " +
                    "Nach Ablauf oder Fehler ist eine erneute Anmeldung erforderlich. " +
                    "Der KI-Gesprächspartner ist keine echte Person.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
