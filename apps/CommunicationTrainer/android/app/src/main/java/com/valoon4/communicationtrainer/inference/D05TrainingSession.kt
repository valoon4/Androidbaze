package com.valoon4.communicationtrainer.inference

import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicBoolean
import javax.net.ssl.HttpsURLConnection

data class D05SessionState(
    val scenario: D05Scenario? = null,
    val modelSlug: String? = null,
    val turns: List<D05Turn> = emptyList(),
    val sentCount: Int = 0,
    val working: Boolean = false,
    val ended: Boolean = false,
    val status: String = "Noch keine Online-Übung gestartet.",
    val elapsedMillis: Long? = null
) {
    val active: Boolean get() = scenario != null && !ended
    val remaining: Int get() = D05TrainingProtocol.MAX_MESSAGES - sentCount
}

/**
 * Preview: a single memory-only roleplay per login, up to 5 user-initiated
 * calls. This controller NEVER accesses the D03 ChatDatabase or ViewModel.
 */
object D05TrainingSession {
    private val worker = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val sending = AtomicBoolean(false)
    @Volatile private var startedInThisLogin = false
    @Volatile private var epoch = 0L
    @Volatile private var activeConnection: HttpsURLConnection? = null

    val state = MutableStateFlow(D05SessionState())

    fun resetForNewLogin() {
        epoch++
        activeConnection?.disconnect()
        startedInThisLogin = false
        state.value = D05SessionState(status = "Neuer ChatGPT-Login erforderlich, bevor ein Training startet.")
    }

    fun start(scenario: D05Scenario, modelSlug: String): Boolean {
        if (sending.get() || startedInThisLogin) return false
        if (!D05TrainingProtocol.SLUG_PATTERN.matches(modelSlug) ||
            !ChatGptPlanProbe.models.value.any { it.slug == modelSlug } ||
            ChatGptPlanProbe.ephemeralAccessToken() == null
        ) {
            state.value = state.value.copy(status = "Bitte zuerst anmelden und ein verfügbares Modell wählen.")
            return false
        }
        startedInThisLogin = true
        state.value = D05SessionState(
            scenario = scenario,
            modelSlug = modelSlug,
            status = "Fiktives Rollenspiel gestartet. Du entscheidest bei jeder Nachricht über den Online-Versand."
        )
        return true
    }

    /** No network call is made by ending a session. */
    fun end() {
        if (state.value.working) return
        state.value = state.value.copy(
            ended = true,
            turns = emptyList(),
            status = "Übung beendet und Verlauf aus dem Arbeitsspeicher entfernt. Zum Neustart erneut anmelden."
        )
    }

    /** At most one POST per button press. Incomplete/uncertain requests count as spent. */
    fun send(text: String): Boolean {
        val current = state.value
        if (!current.active || current.working ||
            current.sentCount >= D05TrainingProtocol.MAX_MESSAGES ||
            !D05TrainingProtocol.validUserMessage(text) ||
            !sending.compareAndSet(false, true)
        ) return false

        val model = current.modelSlug ?: run { sending.set(false); return false }
        val scenario = current.scenario ?: run { sending.set(false); return false }
        if (!ChatGptPlanProbe.models.value.any { it.slug == model }) {
            sending.set(false)
            return false
        }
        val token = ChatGptPlanProbe.ephemeralAccessToken()
        if (token == null) {
            sending.set(false)
            state.value = current.copy(ended = true, status = "ChatGPT-Sitzung abgelaufen. Keine Nachricht übertragen.")
            return false
        }
        // Validate and bound the full body before consuming the request.
        val payload = try {
            D05TrainingProtocol.requestBody(model, scenario, current.turns, text).toString()
        } catch (_: Exception) {
            sending.set(false)
            state.value = current.copy(status = "Die Nachricht ist zu lang oder der Kontext ungültig.")
            return false
        }
        val capturedEpoch = epoch
        val sentCount = current.sentCount + 1
        state.value = current.copy(
            working = true,
            sentCount = sentCount,
            status = "Diese Nachricht wird über deine ChatGPT-Plan-Freigabe online verarbeitet …",
            elapsedMillis = null
        )
        worker.launch {
            val start = SystemClock.elapsedRealtime()
            try {
                val reply = D05Transport.request(token, payload) { conn -> activeConnection = conn }
                if (epoch == capturedEpoch) {
                    state.value = state.value.copy(
                        turns = current.turns + D05Turn("user", text) + D05Turn("assistant", reply),
                        working = false,
                        status = if (sentCount == D05TrainingProtocol.MAX_MESSAGES)
                            "Fünf Nachrichten abgeschlossen. Übungslimit erreicht."
                        else "Antwort vollständig empfangen. Du kannst bewusst eine weitere Nachricht senden.",
                        elapsedMillis = SystemClock.elapsedRealtime() - start
                    )
                }
            } catch (e: D05Transport.Failure) {
                if (epoch == capturedEpoch) {
                    state.value = state.value.copy(
                        ended = true,
                        working = false,
                        status = e.safeMessage + " Keine automatische Wiederholung. Übung beendet."
                    )
                }
            } catch (_: Exception) {
                if (epoch == capturedEpoch) {
                    state.value = state.value.copy(
                        ended = true,
                        working = false,
                        status = "Verbindung unterbrochen. Eine Verarbeitung auf dem Server ist möglich. " +
                            "Keine automatische Wiederholung; Übung beendet."
                    )
                }
            } finally {
                activeConnection = null
                sending.set(false)
            }
        }
        return true
    }
}

/** Only the publicly documented ChatGPT-plan Responses endpoint is used. */
internal object D05Transport {
    private const val ENDPOINT = "https://api.openai.com/v1/responses"
    class Failure(val safeMessage: String) : Exception()

    fun request(
        token: String,
        payload: String,
        whenConnected: (HttpsURLConnection) -> Unit = {}
    ): String {
        val connection = (URL(ENDPOINT).openConnection() as HttpsURLConnection)
        whenConnected(connection)
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 12_000
            connection.readTimeout = 20_000
            connection.instanceFollowRedirects = false
            connection.useCaches = false
            connection.doOutput = true
            connection.setRequestProperty("Authorization", "Bearer $token")
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("Accept", "text/event-stream")
            connection.setRequestProperty("Cache-Control", "no-store")
            connection.outputStream.use {
                it.write(payload.toByteArray(StandardCharsets.UTF_8))
            }
            val code = connection.responseCode
            if (code != 200) throw Failure(when (code) {
                401, 403 -> "ChatGPT-Zugriff nicht mehr erlaubt (HTTP $code)."
                429 -> "ChatGPT-App-Kontingent erreicht (HTTP 429)."
                else -> "ChatGPT hat die Anfrage abgelehnt (HTTP $code)."
            })
            val events = D05SseAccumulator()
            val deadline = SystemClock.elapsedRealtime() + 90_000L
            connection.inputStream.bufferedReader(StandardCharsets.UTF_8).use { reader ->
                var eventName = ""
                val json = StringBuilder()
                var streamChars = 0
                while (true) {
                    if (SystemClock.elapsedRealtime() > deadline)
                        throw Failure("Antwortzeitlimit überschritten.")
                    val line = reader.readLine() ?: throw Failure("Antwortstream unerwartet beendet.")
                    streamChars += line.length + 1
                    if (streamChars > D05TrainingProtocol.MAX_STREAM_BYTES)
                        throw Failure("Antwortstream zu groß.")
                    if (line.isEmpty()) {
                        if (json.isNotEmpty()) {
                            events.accept(eventName, json.toString())
                            if (events.completed) return events.finalAnswer()
                        }
                        json.clear()
                        eventName = ""
                    } else if (line.startsWith("event:")) {
                        eventName = line.substringAfter(":").trim()
                    } else if (line.startsWith("data:")) {
                        if (json.isNotEmpty()) json.append('\n')
                        json.append(line.substringAfter(":").trimStart())
                        if (json.length > D05TrainingProtocol.MAX_SSE_EVENT_BYTES)
                            throw Failure("SSE-Ereignis überschreitet das Limit.")
                    }
                }
            }
            throw Failure("Antwortstream ohne Abschlussereignis beendet.")
        } finally {
            connection.disconnect()
        }
    }
}

/** Response text is displayed only after a verified response.completed. */
internal class D05SseAccumulator {
    private val text = StringBuilder()
    var completed = false
        private set

    fun accept(eventType: String, payload: String) {
        if (completed || payload == "[DONE]") return
        val event = try { JSONObject(payload) } catch (_: Exception) {
            throw D05Transport.Failure("Ungültige Streamingantwort.")
        }
        when (event.optString("type", eventType)) {
            "response.output_text.delta" -> {
                text.append(event.optString("delta"))
                if (text.length > D05TrainingProtocol.MAX_BOT_CHARS)
                    throw D05Transport.Failure("Antwort zu lang für dieses Testtraining.")
            }
            "response.completed" -> completed = true
            "response.failed", "response.incomplete", "error" ->
                throw D05Transport.Failure("ChatGPT hat keine vollständige Antwort geliefert.")
        }
    }

    fun finalAnswer(): String {
        if (!completed || text.isBlank()) throw D05Transport.Failure("Keine vollständige Trainingsantwort.")
        return text.toString()
    }
}
