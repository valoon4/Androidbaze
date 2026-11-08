package com.valoon4.communicationtrainer.inference

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
 * The only cloud inference UI in D04.3. Intentionally separated from the D03
 * chat screens and database. One explicit synthetic request per verified login.
 */
@Composable
fun D043PlanInferenceCard() {
    val signedIn by ChatGptPlanProbe.authenticated.collectAsState()
    val models by ChatGptPlanProbe.models.collectAsState()
    val busy by ChatGptPlanProbe.busy.collectAsState()
    val used by ChatGptPlanProbe.used.collectAsState()
    val status by ChatGptPlanProbe.status.collectAsState()
    val answer by ChatGptPlanProbe.response.collectAsState()
    val elapsed by ChatGptPlanProbe.elapsedMillis.collectAsState()
    var confirmed by remember { mutableStateOf(false) }
    var modelId by remember { mutableStateOf("") }
    var menuOpen by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "D04.3 · Erste echte ChatGPT-Antwort",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Diese Funktion sendet nach deiner ausdrücklichen Bestätigung genau eine " +
                    "festgelegte, künstliche Begrüßungsaufgabe an ChatGPT. Kein bestehender " +
                    "Chat, kein Tagebuch und keine persönlichen Texte werden übertragen. " +
                    "Die Antwort erscheint nur hier und wird nicht gespeichert.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!signedIn) {
                Text(
                    "Bitte zuerst oben mit ChatGPT anmelden. Die vorherige Test-App " +
                        "kann ihre Sitzung nicht auf diese getrennte APK übertragen.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                OutlinedButton(
                    onClick = { ChatGptPlanProbe.loadAvailableModels() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !busy && !used
                ) { Text("Für mein Konto verfügbare Modelle laden") }

                if (models.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            enabled = !busy && !used,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { menuOpen = true }
                        ) {
                            val selected = models.find { it.slug == modelId }
                            Text("Modell: " + (selected?.displayName ?: "Bitte auswählen"))
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            models.forEach { model ->
                                DropdownMenuItem(
                                    text = { Text(model.displayName) },
                                    onClick = {
                                        modelId = model.slug
                                        menuOpen = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.Top) {
                    Checkbox(
                        checked = confirmed,
                        enabled = !busy && !used,
                        onCheckedChange = { confirmed = it }
                    )
                    Text(
                        "Ich erlaube genau eine Testanfrage über mein ChatGPT-Kontingent. " +
                            "Ich habe unter ChatGPT → Einstellungen → Nutzung die " +
                            "Credit-Nutzung für Apps und meine App-Limits geprüft. " +
                            "Die App kann zusätzliche Kosten nicht selbst ausschließen.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = confirmed && !busy && !used && models.any { it.slug == modelId },
                    onClick = { ChatGptPlanProbe.sendOneSyntheticRequest(modelId) }
                ) {
                    Text(if (used) "Testanfrage bereits verbraucht"
                        else "Einmalige ChatGPT-Testanfrage starten")
                }
                if (used) {
                    Text(
                        "Für einen weiteren Test ist eine neue, ausdrücklich autorisierte " +
                            "Anmeldung erforderlich. Keine automatischen Wiederholungen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(status, style = MaterialTheme.typography.bodySmall)
            answer?.let { text ->
                Text("Echte, vollständig empfangene Testantwort:", fontWeight = FontWeight.SemiBold)
                Text(text)
                elapsed?.let { millis ->
                    Text("Gemessene End-to-End-Antwortzeit: ${millis} ms.",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
            Text(
                "Nur ein Funktionstest. Noch keine Freigabe für reale Trainingsgespräche. " +
                    "Keine kostenpflichtige API-Ausweichlösung.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
