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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Explicitly opt-in OAuth-only preview. No model requests or chat transmission.
 * A successful connection proves identity/sign-in and the granted scope only.
 */
@Composable
fun ChatGptConnectionCard() {
    val context = LocalContext.current
    val working by ChatGptSignIn.working.collectAsState()
    val outcome by ChatGptSignIn.signInOutcome.collectAsState()
    val diagnostic by ChatGptSignIn.probeOutcome.collectAsState()
    val progress by ChatGptSignIn.progress.collectAsState()

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
                "Die Anmeldung erfolgt im Systembrowser. Wir prüfen deine Identität und " +
                    "die ausdrücklich erteilte ChatGPT-Plan-Berechtigung. " +
                    "Anschließend kannst du unter D04.3 freiwillig eine einzige künstliche " +
                    "Testanfrage ausführen. Bestehende Chats bleiben lokal. " +
                    "Tokens werden nur vorübergehend im Arbeitsspeicher gehalten.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(
                enabled = !working,
                onClick = { ChatGptSignIn.beginLocalBrowserTest(context) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("1. Lokale Browser-Rückleitung testen (ohne Konto)") }
            diagnostic?.let { message ->
                Text(message, style = MaterialTheme.typography.bodySmall,
                    color = if (message.startsWith("Browser-Rückleitung erfolgreich"))
                        MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error)
            }
            Button(
                onClick = { ChatGptSignIn.beginSignIn(context) },
                enabled = !working,
                modifier = Modifier.fillMaxWidth()
            ) { Text("2. Weiter mit ChatGPT") }
            if (working) {
                Text(progress, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            when (val state = outcome) {
                is ChatGptSignIn.Outcome.Success -> {
                    Text(
                        if (state.connection.planUsageGranted)
                            "Anmeldung erfolgreich. ChatGPT-Plan-Nutzung wurde genehmigt."
                        else "Anmeldung erfolgreich. Keine Freigabe zur ChatGPT-Plan-Nutzung.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Der Login selbst sendet keine KI-Anfrage. " +
                            "Der D04.3-Test unten erfordert eine eigene ausdrückliche Freigabe.",
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
                Text("Diese Bestätigung gilt nur für diese Test-Sitzung.", style = MaterialTheme.typography.bodySmall)
            }
            D043PlanInferenceCard()
            Text(
                "Wichtig: Ein fehlgeschlagener Rücksprung ist kein Hinweis auf falsches Passwort. " +
                    "Bitte niemals vollständige Callback-URLs teilen – sie können Zugangscodes enthalten. " +
                    "Bei Android-Browserproblemen zuerst Schritt 1 testen.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
