package com.valoon4.emotionskompass.feature.journal

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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

/**
 * Both SAF file choices are explicitly initiated by the user. Plain journal JSON
 * never goes to a document provider: the ViewModel writes only EKB1 ciphertext.
 */
@Composable
fun JournalSecurityPanel(
    isLockedByCredential: Boolean,
    busy: Boolean,
    onToggleLock: () -> Unit,
    onExport: (Uri, String) -> Unit,
    onImport: (Uri, String) -> Unit,
    onSystemPicker: (Boolean) -> Unit
) {
    var exportPassword by remember { mutableStateOf("") }
    var exportConfirmation by remember { mutableStateOf("") }
    var importPassword by remember { mutableStateOf("") }
    var pendingExport by remember { mutableStateOf("") }
    var pendingImport by remember { mutableStateOf<Uri?>(null) }

    val exportPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        onSystemPicker(false)
        val secret = pendingExport
        pendingExport = ""
        if (uri != null && secret.isNotBlank()) onExport(uri, secret)
    }
    val importPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        onSystemPicker(false)
        if (uri != null) pendingImport = uri
    }

    if (pendingImport != null) {
        AlertDialog(
            onDismissRequest = { pendingImport = null },
            title = { Text("Tagebuch wirklich ersetzen?") },
            text = { Text("Die Sicherung wird nach Eingabe der Passphrase geprüft. Bei Erfolg werden ALLE momentan gespeicherten Tagebucheinträge durch die Einträge aus der Sicherung ersetzt. Bitte sichere den aktuellen Bestand vorher separat. Dieser Schritt kann nicht rückgängig gemacht werden.") },
            confirmButton = {
                TextButton(onClick = {
                    val uri = pendingImport!!
                    pendingImport = null
                    val password = importPassword
                    importPassword = ""
                    onImport(uri, password)
                }) { Text("Nach Prüfung ersetzen") }
            },
            dismissButton = {
                TextButton(onClick = { pendingImport = null }) { Text("Abbrechen") }
            }
        )
    }

    Card {
        Column(Modifier.fillMaxWidth().padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Text("Sicherheit & Datenkontrolle",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
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
            Text("Verschlüsselte Sicherung erstellen", fontWeight = FontWeight.SemiBold)
            Text("Wähle eine eigene Passphrase mit mindestens 12 Zeichen. Die Datei ist unabhängig vom Android-Keystore wiederherstellbar. Ohne Passphrase ist sie nicht wiederherstellbar.",
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
                    onSystemPicker(true)
                    exportPicker.launch("Emotionskompass-Tagebuch.ekbackup")
                }
            ) { Text("Verschlüsselte Sicherungsdatei speichern") }
            HorizontalDivider()
            Text("Sicherung wiederherstellen", fontWeight = FontWeight.SemiBold)
            Text("Die Wiederherstellung ersetzt erst nach ausdrücklicher Bestätigung und erfolgreicher Entschlüsselung dein aktuelles Tagebuch. Bitte sichere es vorher.",
                style = MaterialTheme.typography.bodySmall)
            SecretField("Passphrase der bestehenden Sicherung", importPassword) {
                importPassword = it
            }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = !busy && importPassword.isNotBlank(),
                onClick = {
                    onSystemPicker(true)
                    importPicker.launch(arrayOf("application/octet-stream", "application/zip", "*/*"))
                }
            ) { Text("Verschlüsselte Sicherungsdatei auswählen") }
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
