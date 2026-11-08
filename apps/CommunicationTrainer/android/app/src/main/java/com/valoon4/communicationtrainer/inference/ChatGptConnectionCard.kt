package com.valoon4.communicationtrainer.inference

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Explicitly opt-in OAuth-only preview. No model requests or chat transmission.
 * A successful connection proves identity/sign-in and the granted scope only.
 */
@Composable
fun ChatGptConnectionCard() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var working by remember { mutableStateOf(false) }
    var outcome by remember { mutableStateOf<ChatGptSignIn.Outcome?>(null) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "D04.2 · ChatGPT-Konto verbinden (Test)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Test der offiziellen Anmeldung über deinen Browser. Nach deiner Zustimmung " +
                    "wird nur geprüft, ob ChatGPT deine Identität und die Berechtigung für " +
                    "ChatGPT-Plan-Nutzung bestätigt. Die App versendet keine Chats, ruft keine KI auf " +
                    "und verwendet keine kostenpflichtige API. " +
                    "Es werden keine Zugangsdaten auf dem Gerät gespeichert.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = {
                    if (working) return@Button
                    working = true
                    outcome = null
                    coroutineScope.launch {
                        outcome = ChatGptSignIn.connect(context)
                        working = false
                    }
                },
                enabled = !working,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (working) "Anmeldung läuft im Browser …" else "Weiter mit ChatGPT") }
            when (val state = outcome) {
                is ChatGptSignIn.Outcome.Success -> {
                    Text(
                        if (state.connection.planUsageGranted)
                            "Anmeldung erfolgreich. ChatGPT-Plan-Nutzung wurde genehmigt."
                        else "Anmeldung erfolgreich. Keine Freigabe zur ChatGPT-Plan-Nutzung.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Es wurde keine KI-Anfrage gesendet. Für die nächste Entwicklungsphase " +
                            "werden zunächst die App-Nutzungsgrenzen und eine kontrollierte " +
                            "synthetische Testanfrage geprüft.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is ChatGptSignIn.Outcome.Failure ->
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                null -> {
                    Text("Noch nicht verbunden. Bestehende Chats bleiben ausschließlich lokal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (outcome is ChatGptSignIn.Outcome.Success) {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { outcome = null }
                ) { Text("Testverbindung ausblenden") }
            }
            Text(
                "Achtung: Browser- und Callback-Kompatibilität auf Android 13 noch nicht " +
                    "durch einen realen Anmeldungstest bestätigt.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
