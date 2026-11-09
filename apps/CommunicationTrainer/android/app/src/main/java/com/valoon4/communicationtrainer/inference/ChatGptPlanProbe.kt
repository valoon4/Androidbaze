package com.valoon4.communicationtrainer.inference

import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicBoolean
import javax.net.ssl.HttpsURLConnection

/**
 * D04.3: explicitly user-started, one-shot ChatGPT-plan inference.
 *
 * This is NOT a production chat gateway. Only a hardcoded synthetic sentence
 * is sent. OAuth access tokens are held in process memory only. There is
 * no API key, backend-api, private chat content, retry, paid provider fallback,
 * persistence, logging, background generation or automatic credit purchase.
 */
object ChatGptPlanProbe {
    private const val MODELS_URL = "https://api.openai.com/v1/models"
    private const val RESPONSES_URL = "https://api.openai.com/v1/responses"
    private const val SYNTHETIC_INPUT =
        "Antworte auf Deutsch mit genau einem kurzen, freundlichen Satz: Begruesse eine Person, die Kommunikationsuebungen beginnt."
    private const val MAX_HTTP_BYTES = 1024 * 1024
    private const val MAX_EVENT_BYTES = 16 * 1024
    private const val MAX_ANSWER_CHARS = 4000
    private val modelSlugPattern = Regex("^[a-zA-Z0-9][a-zA-Z0-9._-]{1,127}$")

    data class PlanModel(val slug: String, val displayName: String)

    // This holder must never be serialized, logged, exposed to Compose, or
    // passed to other applications. It disappears on process death.
    private data class EphemeralSession(val accessToken: String, val expiresAtMillis: Long)
    @Volatile private var session: EphemeralSession? = null
    private val inFlight = AtomicBoolean(false)
    private val oneShotConsumed = AtomicBoolean(false)
    private val sessionCounter = java.util.concurrent.atomic.AtomicLong(0L)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val models = MutableStateFlow<List<PlanModel>>(emptyList())
    val status = MutableStateFlow("Zuerst mit ChatGPT anmelden.")
    val response = MutableStateFlow<String?>(null)
    val busy = MutableStateFlow(false)
    val used = MutableStateFlow(false)
    val authenticated = MutableStateFlow(false)
    val elapsedMillis = MutableStateFlow<Long?>(null)

    /**
     * Only called after OAuth ID-token signature/aud/nonce and plan scopes were
     * validated by ChatGptSignIn. No refresh credentials are retained.
     */
    internal fun onVerifiedSignIn(accessToken: String, expiresInSeconds: Long) {
        clearSession()
        if (accessToken.length !in 20..8192 || expiresInSeconds !in 61..86_400L) {
            status.value = "Das Sitzungstoken ist für einen sicheren Test nicht verwendbar."
            return
        }
        session = EphemeralSession(
            accessToken,
            System.currentTimeMillis() + (expiresInSeconds - 30) * 1000
        )
        authenticated.value = true
        status.value = "Anmeldung geprüft. Du kannst die verfügbaren Modelle abrufen."
    }

    fun clearSession() {
        sessionCounter.incrementAndGet()
        D05TrainingSession.onAccountChanged()
        session = null
        models.value = emptyList()
        response.value = null
        elapsedMillis.value = null
        oneShotConsumed.set(false)
        used.value = false
        authenticated.value = false
        status.value = "Keine aktive ChatGPT-Testsitzung."
    }

    private fun activeToken(): String? {
        val current = session ?: return null
        if (System.currentTimeMillis() >= current.expiresAtMillis) {
            clearSession()
            return null
        }
        return current.accessToken
    }

    /** Read-only, scoped access for the separate D05 opt-in training engine. */
    internal fun verifiedBearerForExplicitUse(): String? = activeToken()

    /** In-flight D05 results from an older OAuth identity must be discarded. */
    internal fun sessionVersion(): Long = sessionCounter.get()

    fun loadAvailableModels() {
        val token = activeToken()
        if (token == null) {
            status.value = "Keine aktive Sitzung. Melde dich über ChatGPT erneut an."
            return
        }
        if (!inFlight.compareAndSet(false, true)) return
        busy.value = true
        status.value = "Lade nur die für dein Konto freigegebenen Modelle (keine KI-Anfrage)."
        scope.launch {
            try {
                val raw = requestModels(token)
                val parsed = parseModels(JSONObject(raw))
                models.value = parsed
                status.value = if (parsed.isEmpty())
                    "Keine geeigneten, für dein Konto sichtbaren Modelle gefunden."
                else "${parsed.size} Modelle verfügbar. Bitte ein Modell auswählen."
            } catch (_: Exception) {
                models.value = emptyList()
                status.value = "Modellliste konnte nicht geladen werden. Kein KI-Aufruf erfolgt."
            } finally {
                busy.value = false
                inFlight.set(false)
            }
        }
    }

    fun sendOneSyntheticRequest(modelSlug: String) {
        val token = activeToken()
        if (token == null) {
            status.value = "Keine aktive Sitzung. Bitte erneut anmelden."
            return
        }
        if (!models.value.any { it.slug == modelSlug }) {
            status.value = "Bitte ein Modell aus der bestätigten Kontoliste auswählen."
            return
        }
        if (!inFlight.compareAndSet(false, true)) return
        // The one-shot budget is consumed on click, even on timeout/error.
        // No retry with the same session, no fallback and no duplicate POST.
        if (!oneShotConsumed.compareAndSet(false, true)) {
            inFlight.set(false)
            return
        }
        used.value = true
        busy.value = true
        response.value = null
        elapsedMillis.value = null
        status.value = "Genau eine synthetische Anfrage wird gesendet. Keine Chatdaten."
        scope.launch {
            val began = SystemClock.elapsedRealtime()
            try {
                val answer = streamSyntheticResponse(token, modelSlug)
                response.value = answer
                elapsedMillis.value = SystemClock.elapsedRealtime() - began
                status.value = "Echte ChatGPT-Antwort vollständig empfangen (response.completed)."
            } catch (e: PlanProbeFailure) {
                status.value = e.safeMessage
            } catch (_: Exception) {
                status.value = "Netzwerk-/Streamingfehler. Keine automatische Wiederholung."
            } finally {
                busy.value = false
                inFlight.set(false)
            }
        }
    }

    internal fun parseModels(json: JSONObject): List<PlanModel> {
        val array = json.optJSONArray("models") ?: return emptyList()
        val seen = mutableSetOf<String>()
        val results = ArrayList<PlanModel>()
        for (i in 0 until array.length()) {
            val item = array.optJSONObject(i) ?: continue
            if (item.optString("visibility") != "list") continue
            val slug = item.optString("slug")
            if (!modelSlugPattern.matches(slug) || !seen.add(slug)) continue
            val label = item.optString("display_name").takeIf { it.isNotBlank() }
                ?.take(80) ?: slug
            results.add(PlanModel(slug, label))
            if (results.size == 30) break
        }
        return results
    }

    /** Exclude unsupported fields from the plan-sharing Responses preview. */
    internal fun requestBody(modelSlug: String): JSONObject {
        require(modelSlugPattern.matches(modelSlug))
        return JSONObject()
            .put("model", modelSlug)
            .put("input", JSONArray().put(JSONObject()
                .put("role", "user")
                .put("content", SYNTHETIC_INPUT)))
            .put("store", false)
            .put("stream", true)
    }

    private fun connection(endpoint: String, token: String): HttpsURLConnection {
        require(endpoint == MODELS_URL || endpoint == RESPONSES_URL)
        return (URL(endpoint).openConnection() as HttpsURLConnection).also {
            it.connectTimeout = 12_000
            it.readTimeout = 20_000
            it.instanceFollowRedirects = false
            it.useCaches = false
            it.setRequestProperty("Authorization", "Bearer $token")
            it.setRequestProperty("Cache-Control", "no-store")
            it.setRequestProperty("Accept", if (endpoint == RESPONSES_URL) "text/event-stream" else "application/json")
        }
    }

    private fun requestModels(token: String): String {
        val conn = connection(MODELS_URL, token)
        try {
            conn.requestMethod = "GET"
            if (conn.responseCode != 200)
                throw PlanProbeFailure("Modellliste wurde abgelehnt (HTTP ${conn.responseCode}).")
            return conn.inputStream.use { readBounded(it, MAX_HTTP_BYTES) }
        } finally { conn.disconnect() }
    }

    internal fun readBounded(stream: InputStream, maxBytes: Int): String {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        var total = 0
        while (true) {
            val n = stream.read(buffer)
            if (n < 0) break
            total += n
            if (total > maxBytes) throw PlanProbeFailure("Serverantwort zu groß. Test abgebrochen.")
            output.write(buffer, 0, n)
        }
        return output.toString("UTF-8")
    }

    private fun streamSyntheticResponse(token: String, modelSlug: String): String {
        val conn = connection(RESPONSES_URL, token)
        try {
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            val request = requestBody(modelSlug).toString().toByteArray(StandardCharsets.UTF_8)
            conn.outputStream.use { it.write(request) }
            val code = conn.responseCode
            if (code != 200) {
                if (code == 401 || code == 403) throw PlanProbeFailure("Keine KI-Berechtigung oder Sitzung abgelaufen (HTTP $code).")
                if (code == 429) throw PlanProbeFailure("Anfragelimit erreicht. Keine Wiederholung und kein kostenpflichtiger Fallback.")
                throw PlanProbeFailure("Die Anfrage wurde abgelehnt (HTTP $code). Kein Fallback.")
            }
            val events = PlanSseAccumulator()
            conn.inputStream.bufferedReader(StandardCharsets.UTF_8).use { reader ->
                var currentEvent = ""
                val data = StringBuilder()
                var totalChars = 0
                while (true) {
                    val line = reader.readLine()
                        ?: throw PlanProbeFailure("Antwortstream vor vollständigem Abschluss unterbrochen.")
                    totalChars += line.length
                    if (totalChars > MAX_HTTP_BYTES)
                        throw PlanProbeFailure("Antwortstream zu groß. Test abgebrochen.")
                    if (line.isEmpty()) {
                        if (data.isNotEmpty()) {
                            events.accept(currentEvent, data.toString())
                            if (events.done) return events.finalAnswer()
                        }
                        currentEvent = ""
                        data.clear()
                    } else if (line.startsWith("event:")) {
                        currentEvent = line.substringAfter(":").trim()
                    } else if (line.startsWith("data:")) {
                        if (data.isNotEmpty()) data.append('\n')
                        data.append(line.substringAfter(":").trimStart())
                        if (data.length > MAX_EVENT_BYTES) throw PlanProbeFailure("SSE-Ereignis zu groß.")
                    }
                }
            }
            throw PlanProbeFailure("Antwortstream ohne vollständiges Ende. Keine Wiederholung.")
        } finally { conn.disconnect() }
    }

    internal class PlanProbeFailure(val safeMessage: String) : Exception()

    /**
     * Only response.completed may mark success. A partial delta never does.
     */
    internal class PlanSseAccumulator {
        private val buffer = StringBuilder()
        var done = false
            private set
        private var failed = false

        fun accept(eventName: String, payload: String) {
            if (done || failed || payload == "[DONE]") return
            val event = try { JSONObject(payload) } catch (_: Exception) {
                throw PlanProbeFailure("Ungültiges SSE-Ereignis.")
            }
            val type = event.optString("type", eventName)
            when (type) {
                "response.output_text.delta" -> {
                    buffer.append(event.optString("delta"))
                    if (buffer.length > MAX_ANSWER_CHARS)
                        throw PlanProbeFailure("Textausgabe überschreitet das Testlimit.")
                }
                "response.completed" -> done = true
                "response.failed", "response.incomplete", "error" -> {
                    failed = true
                    val reason = event.optJSONObject("response")?.optJSONObject("error")
                        ?.optString("code")
                        ?: event.optJSONObject("error")?.optString("code")
                    throw PlanProbeFailure(
                        if (reason == "subscription_sharing_usage_limit_exceeded" ||
                            reason == "subscription_sharing_usage_unavailable")
                            "ChatGPT-Kontingent für Apps nicht verfügbar/erschöpft. Kein Fallback."
                        else "ChatGPT hat die Ausgabe nicht vollständig abgeschlossen. Kein Fallback."
                    )
                }
            }
        }

        fun finalAnswer(): String {
            if (!done || failed) throw PlanProbeFailure("Antwort nicht vollständig abgeschlossen.")
            if (buffer.isBlank()) throw PlanProbeFailure("Antwort abgeschlossen, aber kein Antworttext empfangen.")
            return buffer.toString()
        }
    }
}
