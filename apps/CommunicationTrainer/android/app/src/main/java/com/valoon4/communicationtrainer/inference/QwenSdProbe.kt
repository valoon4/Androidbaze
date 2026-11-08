package com.valoon4.communicationtrainer.inference

import android.app.ActivityManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.system.Os
import android.system.OsConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.security.MessageDigest

/**
 * Experimental, user-triggered on-device benchmark. Never touches Room/chat state,
 * never downloads a model, and never falls back to a remote provider.
 */
object QwenSdProbe {
    const val MODEL_FILE = "Qwen3-0.6B-Q4_K_M.gguf"
    // Pinned unsloth/Qwen3-0.6B-GGUF revision 17429132c7b3c2dbe0ec7e06ee085aa89a96994f.
    const val EXPECTED_SHA256 = "ac2d97712095a558e31573f62f466a3f9d93990898b0ec79d7c974c1780d524a"
    const val MAX_MODEL_BYTES = 600L * 1024 * 1024
    const val MIN_AVAILABLE_RAM = 1100L * 1024 * 1024
    const val MIN_INTERNAL_HEADROOM = 512L * 1024 * 1024

    fun deviceGate(arm64: Boolean, lowRam: Boolean, availableRam: Long, internalFree: Long): String? {
        if (!arm64) return "Dieses Testpaket benötigt arm64-v8a."
        if (lowRam || availableRam < MIN_AVAILABLE_RAM) return "Zu wenig freier Arbeitsspeicher für den vorsichtigen Modelltest."
        if (internalFree < MIN_INTERNAL_HEADROOM) return "Zu wenig interner Sicherheits-Speicherpuffer."
        return null
    }

    fun inspectDevice(context: Context): String? {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memory = ActivityManager.MemoryInfo().also(manager::getMemoryInfo)
        val sd = SdStoragePreflight.inspect(context)
        if (sd.status != SdStorageStatus.AVAILABLE_FOR_STORAGE_PROBE) {
            return "SD-Speicherprüfung nicht bestanden. Erst die SD-Karte prüfen."
        }
        return deviceGate(
            arm64 = Build.SUPPORTED_ABIS.contains("arm64-v8a"),
            lowRam = memory.lowMemory || manager.isLowRamDevice,
            availableRam = memory.availMem,
            internalFree = sd.internalFreeBytes ?: 0L
        )
    }

    fun calculateSha256(stream: InputStream): Pair<String, Long> {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(128 * 1024)
        var total = 0L
        while (true) {
            val size = stream.read(buffer)
            if (size < 0) break
            if (size == 0) continue
            total += size
            require(total <= MAX_MODEL_BYTES) { "Datei größer als 600 MiB – Test abgebrochen." }
            digest.update(buffer, 0, size)
        }
        val sha = digest.digest().joinToString("") { "%02x".format(it.toInt() and 0xff) }
        return sha to total
    }

    fun verifyModel(context: Context, uri: Uri): String? = try {
        val pair = context.contentResolver.openInputStream(uri)?.use(::calculateSha256)
            ?: return "Datei konnte nicht geöffnet werden."
        if (pair.second < 300L * 1024 * 1024) "Datei zu klein für den erwarteten Qwen-Test."
        else if (pair.first != EXPECTED_SHA256)
            "SHA-256 stimmt nicht: Es ist nicht die freigegebene, festgelegte Testdatei."
        else null
    } catch (e: Exception) {
        "Dateiprüfung gescheitert: ${e.message ?: "Lesefehler"}"
    }

    fun runOnSelectedFile(context: Context, uri: Uri): String {
        // A Content-URI is NOT a native path. Use only a seekable, already-open
        // descriptor; the native library attempts to reopen /proc/self/fd/N.
        // If the provider forbids this, STOP. No internal shadow-copy.
        context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
            try {
                Os.lseek(pfd.fileDescriptor, 0L, OsConstants.SEEK_SET)
            } catch (_: Exception) {
                return "Die SD-Datei ist nicht seekbar. Kein Modell geladen."
            }
            return QwenNative.runBenchmark(pfd.fd, 2, 1024, 48)
        }
        return "Die ausgewählte Modelldatei ist nicht verfügbar."
    }
}

object QwenNative {
    init { System.loadLibrary("ctqwen") }
    external fun runBenchmark(fd: Int, threads: Int, contextTokens: Int, maxTokens: Int): String
}

@Composable
fun QwenSdProbeCard() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var verifiedUri by remember { mutableStateOf<Uri?>(null) }
    var busy by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("Noch keine GGUF-Datei ausgewählt.") }
    val chooseModel = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        selectedUri = uri
        verifiedUri = null
        result = if (uri == null) "Keine Datei gewählt." else
            "Datei gewählt. Zuerst Prüfsumme bestätigen; es wird nichts kopiert."
    }

    Surface(color = MaterialTheme.colorScheme.surface, shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("D04.1 · Qwen3-Gerätetest (experimentell)",
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "Nur synthetische deutsche Testeingabe; 2 CPU-Threads, 1.024 Kontexttoken und maximal 48 Antworttoken. " +
                    "Kein automatischer Download, keine Cloud, kein Chat-Zugriff, keine interne Modellkopie. " +
                    "Benötigt die exakt angegebene Qwen3-0.6B-Q4_K_M.gguf-Datei auf deiner SD-Karte.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(
                enabled = !busy,
                onClick = { chooseModel.launch(arrayOf("*/*")) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("GGUF auf SD-Karte auswählen") }
            OutlinedButton(
                enabled = !busy && selectedUri != null,
                onClick = {
                    val uri = selectedUri ?: return@OutlinedButton
                    busy = true
                    result = "Prüfe die gesamte Modelldatei (SHA-256) …"
                    scope.launch {
                        val error = withContext(Dispatchers.IO) { QwenSdProbe.verifyModel(context, uri) }
                        verifiedUri = if (error == null) uri else null
                        result = error ?: "Prüfsumme korrekt. Noch kein KI-Modell geladen."
                        busy = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (busy) "Prüfung läuft …" else "GGUF-Prüfsumme überprüfen") }
            Button(
                enabled = !busy && verifiedUri != null,
                onClick = {
                    val uri = verifiedUri ?: return@Button
                    val blocked = QwenSdProbe.inspectDevice(context)
                    if (blocked != null) {
                        result = blocked
                        return@Button
                    }
                    busy = true
                    result = "Lade das Modell nur für diesen Test. Gerät kann kurzzeitig stark belastet werden …"
                    scope.launch {
                        val answer = withContext(Dispatchers.Default) {
                            try {
                                QwenSdProbe.runOnSelectedFile(context, uri)
                            } catch (e: Throwable) {
                                "Modellversuch fehlgeschlagen: ${e.message ?: e.javaClass.simpleName}"
                            }
                        }
                        result = answer
                        busy = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Lokalen Modelltest bewusst starten") }
            Text(result, style = MaterialTheme.typography.bodySmall)
            Text("Experimenteller Build – kein Nachweis für Gesprächsqualität oder dauerhafte Stabilität.",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
