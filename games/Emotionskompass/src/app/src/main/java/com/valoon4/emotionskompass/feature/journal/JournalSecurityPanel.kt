package com.valoon4.emotionskompass.feature.journal

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.valoon4.emotionskompass.core.journal.JournalBackup
import com.valoon4.emotionskompass.core.insights.BackupImportPreview

/**
 * Both SAF file choices are explicitly initiated by the user. Plain journal JSON
 * never goes to a document provider: the ViewModel exports EKB3 ciphertext
 * containing journal, insights and strategy experiments; legacy EKB1/2 remain readable.
 */
@Composable
fun JournalSecurityPanel(
    isLockedByCredential: Boolean,
    busy: Boolean,
    onToggleLock: () -> Unit,
    onExport: (Uri, String) -> Unit,
    onImport: (Uri, String) -> Unit,
    onSystemPicker: (Boolean) -> Unit,
    onPreviewImport: (Uri, String, (BackupImportPreview?, String?) -> Unit) -> Unit
) {
    var exportPassword by remember { mutableStateOf("") }
    var exportConfirmation by remember { mutableStateOf("") }
    var importPassword by remember { mutableStateOf("") }
    var pendingExport by remember { mutableStateOf("") }
    var pendingImport by remember { mutableStateOf<Uri?>(null) }
    var importPreview by remember { mutableStateOf<BackupImportPreview?>(null) }
    var previewBusy by remember { mutableStateOf(false) }
    var pickerError by remember { mutableStateOf<String?>(null) }

    val exportPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        onSystemPicker(false)
        val secret = pendingExport
        pendingExport = ""
        if (uri != null && secret.isNotBlank()) onExport(uri, secret)
        // The unchosen path never stores anything and the secret is forgotten.
    }
    val importPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        onSystemPicker(false)
        if (uri != null) {
            previewBusy = true
            // Decrypt and authenticate *before* asking to replace any local data.
            onPreviewImport(uri, importPassword) { result, problem ->
                previewBusy = false
                if (result != null) {
                    importPreview = result
                    pendingImport = uri
                } else {
                    importPreview = null
                    pendingImport = null
                    pickerError = problem ?: "Die Sicherung konnte nicht geprüft werden."
                    importPassword = ""
                }
            }
        } else importPassword = ""
    }

    if (pendingImport != null) {
        AlertDialog(
            onDismissRequest = {
                pendingImport = null
                importPreview = null
                importPassword = ""
            },
            title = { Text("Geprüfte Sicherung wirklich übernehmen?") },
            text = {
                Column(
                    modifier = Modifier.heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val report = importPreview
                    if (report != null) {
                        Text("Sicherungsformat: ${report.format} · ${report.journalCount} Tagebucheinträge",
                            fontWeight = FontWeight.SemiBold)
                        Text(if (report.insightCount == null)
                            "Eigene Erkenntnisse: nicht enthalten – bestehende bleiben erhalten."
                        else "Eigene Erkenntnisse: ${report.insightCount} – werden ersetzt.")
                        Text(if (report.strategyExperimentCount == null)
                            "Strategieideen: nicht enthalten – bestehende bleiben erhalten."
                        else "Strategieideen: ${report.strategyExperimentCount} – werden ersetzt.")
                    }
                    Text("Die Datei wurde mit deiner Passphrase geprüft. Nur die aufgeführten Datenbereiche werden ersetzt – auch dann, wenn eine enthaltene Liste leer ist. Ungültige Verknüpfungen werden entfernt, nicht deine eigenen Texte. Das Ersetzen kann nicht rückgängig gemacht werden.")
                    Text("Sichere deinen aktuellen Stand zuerst. Die App prüft beim Ersetzen, ob die Datei seit dieser Vorschau verändert wurde.",
                        style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val uri = pendingImport!!
                    pendingImport = null
                    importPreview = null
                    val password = importPassword
                    importPassword = ""
                    onImport(uri, password)
                }) { Text("Nach Prüfung ersetzen") }
            },
            dismissButton = {
                TextButton(onClick = {
                    pendingImport = null
                    importPreview = null
                    importPassword = ""
                }) { Text("Abbrechen") }
            }
        )
    }

    Card {
        Column(Modifier.fillMaxWidth().padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Text("Sicherheit & Datenkontrolle",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            if (pickerError != null) {
                Text(pickerError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error)
            }
            Text("Das Tagebuch ist weiterhin gerätegebunden mit Android Keystore verschlüsselt. Eine zusätzliche Gerätesperren-Abfrage lässt sich freiwillig aktivieren.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(onClick = onToggleLock, enabled = !busy,
                modifier = Modifier.fillMaxWidth()) {
                Text(if (isLockedByCredential)
                    "App-Sperre mit Geräte-PIN/Muster/Passwort ausschalten"
                else "App-Sperre mit Geräte-PIN/Muster/Passwort einschalten")
            }
            Text("Die App-Sperre verwendet die vorhandene Android-Geräteanmeldung. Sie ist ein zusätzlicher Oberflächenschutz, kein separater Verschlüsselungsschlüssel.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider()
            Text("Verschlüsselte Gesamtsicherung erstellen (EKB3)", fontWeight = FontWeight.SemiBold)
            Text("Die EKB3-Sicherung umfasst Tagebuch, persönliche Erkenntnisse und eigene Strategieideen gemeinsam. Verwende eine Passphrase mit mindestens 12 Zeichen; ohne sie ist die Datei nicht wiederherstellbar. Frühere App-Versionen können EKB3 nicht öffnen.",
                style = MaterialTheme.typography.bodySmall)
            SecretField("Passphrase für diese Sicherung", exportPassword) { exportPassword = it }
            SecretField("Passphrase wiederholen", exportConfirmation) { exportConfirmation = it }
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !busy && exportPassword.length >= JournalBackup.MIN_PASSPHRASE_LENGTH &&
                    exportPassword == exportConfirmation,
                onClick = {
                    pendingExport = exportPassword
                    exportPassword = ""
                    exportConfirmation = ""
                    pickerError = null
                    onSystemPicker(true)
                    try {
                        exportPicker.launch("InnerWave-Gesamtsicherung-EKB3.ekbackup")
                    } catch (e: Exception) {
                        // A missing/disabled document picker must never leave the
                        // background relock bypass enabled indefinitely.
                        onSystemPicker(false)
                        pendingExport = ""
                        pickerError = "Android konnte die Dateiauswahl nicht öffnen."
                    }
                }
            ) { Text("Verschlüsselte Sicherungsdatei speichern") }
            HorizontalDivider()
            Text("Sicherung wiederherstellen", fontWeight = FontWeight.SemiBold)
            Text("EKB1-, EKB2- und EKB3-Dateien werden unterstützt. EKB1 erhält Erkenntnisse und Strategieideen; EKB2 erhält vorhandene Strategieideen. EKB3 ersetzt alle drei Bereiche. Fehlende Bereiche werden niemals als leere Liste behandelt. Ungültige Verknüpfungen werden gelöst, nicht deine eigenen Texte. Du bestätigst jede Wiederherstellung ausdrücklich. Sichere vorher deinen aktuellen Stand.",
                style = MaterialTheme.typography.bodySmall)
            SecretField("Passphrase der bestehenden Sicherung", importPassword) {
                importPassword = it
            }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = !busy && !previewBusy && importPassword.isNotBlank(),
                onClick = {
                    pickerError = null
                    onSystemPicker(true)
                    try {
                        importPicker.launch(arrayOf("application/octet-stream", "application/zip", "*/*"))
                    } catch (e: Exception) {
                        onSystemPicker(false)
                        importPassword = ""
                        pickerError = "Android konnte die Dateiauswahl nicht öffnen."
                    }
                }
            ) { Text("Verschlüsselte Sicherungsdatei auswählen und prüfen") }
            if (previewBusy) {
                Text("Sicherung wird entschlüsselt und geprüft; noch keine Daten werden ersetzt.",
                    style = MaterialTheme.typography.bodySmall)
            }
            Text("Wichtig: Die Passphrase wird nicht gespeichert. Nur selbst gewählte Sicherungsdateien verlassen die App; es findet kein automatischer Cloud-Upload statt.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SecretField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        visualTransformation = PasswordVisualTransformation(),
        singleLine = true
    )
}
