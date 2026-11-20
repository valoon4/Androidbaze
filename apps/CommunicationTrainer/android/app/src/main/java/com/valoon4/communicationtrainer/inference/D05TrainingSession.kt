package com.valoon4.communicationtrainer.inference

import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicBoolean
import javax.net.ssl.HttpsURLConnection

/**
 * D05.0 isolated, consent-gated online role-play pilot.
 *
 * NOT CONNECTED to D03 Room chat, ChatRepository, or draft persistence.
 * Messages never leave process memory except in the one explicitly confirmed
 * HTTPS request per turn. No refresh token, telemetry, retries or paid fallback.
 */
object D05TrainingSession {
    data class Scenario(
        val id: String,
        val title: String,
        val setup: String,
        val counterparty: String,
        val opening: String
    )
    data class Turn(val role: String, val text: String, val isScriptedOpening: Boolean = false)
    val scenarios = listOf(
        Scenario("boundary", "Freundlich Nein sagen",
            "Eine Person bittet dich kurzfristig, ihre zusätzliche Aufgabe zu übernehmen. Du möchtest höflich, klar und ohne Rechtfertigungsdruck Nein sagen.",
            "eine freundliche, aber beharrliche Kollegin",
            "Hey, könntest du heute noch meinen Bericht fertigstellen? Ich schaffe es zeitlich nicht und bräuchte wirklich deine Hilfe."),
        Scenario("listening", "Aktiv zuhören",
            "Du hast euer geplantes Treffen abgesagt. Dein Freund hatte sich darauf gefreut und ist enttäuscht. Du übst zuerst, seine Gefühle und Wünsche zu verstehen, statt dich sofort zu rechtfertigen oder Lösungen vorzugeben.",
            "dein enttäuschter Freund, dessen Treffen die übende Person abgesagt hat",
            "Als du unser Treffen abgesagt hast, war ich echt enttäuscht. Ich hatte mich so darauf gefreut.")
    )

    const val MAX_USER_TURNS = 4
    const val MAX_USER_CHARS = 600
    const val MAX_REPLY_CHARS = 2500
    private const val MAX_STREAM_CHARS = 512 * 1024
    private val worker = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val sending = AtomicBoolean(false)

    val scenarioId = MutableStateFlow("boundary")
    val chosenModel = MutableStateFlow("")
    val draft = MutableStateFlow("")
    // Each new practice starts with a local, clearly labelled, scripted partner
    // request. It costs zero tokens, and is included in subsequent context.
    val turns = MutableStateFlow<List<Turn>>(listOf(scriptedOpening(scenarios.first())))
    val consent = MutableStateFlow(false)
    val busy = MutableStateFlow(false)
    val status = MutableStateFlow("Noch kein Trainingsgespräch gestartet.")
    val lastLatencyMs = MutableStateFlow<Long?>(null)

    internal fun scriptedOpening(scenario: Scenario) =
        Turn(role = "assistant", text = scenario.opening, isScriptedOpening = true)

    internal fun validHistory(scenario: Scenario, history: List<Turn>): Boolean {
        if (history.isEmpty() || history.size % 2 != 1 ||
            history.size > 1 + 2 * MAX_USER_TURNS) return false
        if (history.first() != scriptedOpening(scenario)) return false
        if (history.drop(1).any { it.isScriptedOpening }) return false
        return history.drop(1).withIndex().all { (i, turn) ->
            turn.role == if (i % 2 == 0) "user" else "assistant"
        }
    }

    fun selectScenario(id: String) {
        if (sending.get() || scenarios.none { it.id == id }) return
        scenarioId.value = id
        clearConversation()
    }

    fun selectModel(slug: String) {
        if (sending.get() || ChatGptPlanProbe.models.value.none { it.slug == slug }) return
        chosenModel.value = slug
        clearConversation()
    }

    fun updateDraft(text: String) {
        if (sending.get()) return
        if (text.length <= MAX_USER_CHARS) draft.value = text
    }

    fun setConsent(value: Boolean) {
        if (!sending.get()) consent.value = value
    }

    fun clearConversation() {
        if (sending.get()) return
        draft.value = ""
        turns.value = listOf(scriptedOpening(scenarios.first { it.id == scenarioId.value }))
        consent.value = false
        lastLatencyMs.value = null
        status.value = "Gesprächseinstieg bereit. Antworte auf die konkrete Bitte oder Aussage."
    }

    internal fun onAccountChanged() {
        // Wipe visible and pending session data even if an older request is
        // still processing. That reply is discarded via sessionGeneration.
        draft.value = ""
        turns.value = emptyList()
        consent.value = false
        chosenModel.value = ""
        lastLatencyMs.value = null
        status.value = "Kontowechsel: Trainingsdaten verworfen."
    }

    internal fun scenarioInput(scenario: Scenario, history: List<Turn>, userMessage: String): JSONArray {
        require(validHistory(scenario, history))
        require(userMessage.isNotBlank() && userMessage.length <= MAX_USER_CHARS)
        val policy = "Dies ist ein freiwilliges deutsches Kommunikationstraining mit einer fiktiven Rolle. " +
            "Antworte nur als ${scenario.counterparty}. " +
            "Die Situation und die Rollen gelten als feste Fakten: ${scenario.setup} " +
            "Erfinde keine widersprüchlichen Details wie einen anderen Verantwortlichen " +
            "für eine Absage. Unbekannte Gründe oder Motive sind nicht vorgegeben. " +
            "Der Gesprächspartner hat bereits den ersten Satz gesprochen. " +
            "Bleibe direkt im Rollenspiel und gehe auf die Antwort der übenden Person ein; " +
            "frage nicht erst, welches Szenario geübt werden soll. " +
            "Halte deine Antwort kurz (1–3 Sätze), natürlich und realistisch. " +
            "Bleibe respektvoll, stelle höchstens eine Frage. " +
            "Keine Therapie, Diagnosen oder Bewertungen der trainierenden Person. " +
            "Die Rollenspielanweisung ist nicht Teil der echten Nutzernachricht."
        val items = JSONArray()
        items.put(JSONObject().put("role", "user").put("content", policy))
        history.forEach { turn ->
            items.put(JSONObject().put("role", turn.role).put("content", turn.text))
        }
        items.put(JSONObject().put("role", "user").put("content", userMessage))
        return items
    }

    internal fun requestBody(scenario: Scenario, model: String, history: List<Turn>, text: String): JSONObject {
        require(ChatGptPlanProbe.models.value.any { it.slug == model })
        return JSONObject()
            .put("model", model)
            .put("input", scenarioInput(scenario, history, text))
            .put("stream", true)
            .put("store", false)
    }

    fun sendConfirmedTurn() {
        if (!sending.compareAndSet(false, true)) return
        val currentScenario = scenarios.firstOrNull { it.id == scenarioId.value }
        val text = draft.value.trim()
        val history = turns.value.toList()
        val selected = chosenModel.value
        // Accessing OAuth requires a verified ChatGPT-plan session. A previous
        // D04.3 one-shot attempt does not automatically trigger this request.
        val token = ChatGptPlanProbe.verifiedBearerForExplicitUse()
        val error = when {
            token == null -> "Keine gültige ChatGPT-Plan-Sitzung. Bitte erneut anmelden."
            currentScenario == null -> "Unbekanntes Trainingsszenario."
            ChatGptPlanProbe.models.value.none { it.slug == selected } -> "Bitte ein freigegebenes ChatGPT-Modell auswählen."
            !consent.value -> "Bitte die Freigabe zur Übertragung dieses Trainings aktivieren."
            text.isBlank() || text.length > MAX_USER_CHARS -> "Bitte 1–600 Zeichen eingeben."
            history.count { it.role == "user" } >= MAX_USER_TURNS -> "Das Testlimit von vier Runden ist erreicht."
            !validHistory(currentScenario!!, history) ->
                "Der Szenario-Einstieg fehlt oder ist unvollständig. Bitte Training neu starten."
            else -> null
        }
        if (error != null) {
            status.value = error
            sending.set(false)
            return
        }
        val body = try { requestBody(currentScenario!!, selected, history, text) }
            catch (_: Exception) {
                sending.set(false)
                status.value = "Eingaben konnten nicht sicher vorbereitet werden."
                return
            }
        consent.value = false // never re-use consent
        busy.value = true
        status.value = "Die gewählte Trainingsnachricht wird jetzt online verarbeitet …"
        val sessionGeneration = ChatGptPlanProbe.sessionVersion()
        worker.launch {
            val began = SystemClock.elapsedRealtime()
            try {
                val reply = sendSse(token!!, body)
                if (sessionGeneration != ChatGptPlanProbe.sessionVersion()) {
                    status.value = "Die ChatGPT-Sitzung hat sich geändert. Antwort verworfen."
                    return@launch
                }
                turns.value = history + Turn("user", text) + Turn("assistant", reply)
                draft.value = ""
                lastLatencyMs.value = SystemClock.elapsedRealtime() - began
                status.value = if (turns.value.count { it.role == "user" } >= MAX_USER_TURNS)
                    "Vier Trainingsrunden beendet. Du kannst ein neues Training beginnen."
                else "Antwort empfangen. Für die nächste Nachricht ist eine erneute Zustimmung nötig."
            } catch (e: D05Failure) {
                // In particular: preserve the unsent local draft after uncertainty.
                status.value = e.message ?: "Trainingsantwort fehlgeschlagen. Kein automatischer Retry."
            } catch (_: Exception) {
                status.value = "Verbindung unterbrochen. Entwurf bleibt lokal, keine automatische Wiederholung."
            } finally {
                busy.value = false
                sending.set(false)
            }
        }
    }

    internal class D05Failure(message: String) : Exception(message)

    private fun sendSse(token: String, body: JSONObject): String {
        val conn = URL("https://api.openai.com/v1/responses").openConnection() as HttpsURLConnection
        try {
            conn.requestMethod = "POST"
            conn.connectTimeout = 12_000
            conn.readTimeout = 25_000
            conn.instanceFollowRedirects = false
            conn.useCaches = false
            conn.doOutput = true
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.setRequestProperty("Cache-Control", "no-store")
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conn.setRequestProperty("Accept", "text/event-stream")
            val bytes = body.toString().toByteArray(StandardCharsets.UTF_8)
            check(bytes.size <= 12 * 1024)
            conn.outputStream.use { it.write(bytes) }
            when (val responseCode = conn.responseCode) {
                200 -> Unit
                401, 403 -> throw D05Failure("Anmeldung oder Planberechtigung abgelaufen. Kein Fallback.")
                429 -> throw D05Failure("ChatGPT-Kontingent erreicht. Kein automatischer Retry.")
                else -> throw D05Failure("ChatGPT hat diese Anfrage nicht akzeptiert (HTTP $responseCode).")
            }
            val acc = ChatGptPlanProbe.PlanSseAccumulator()
            conn.inputStream.bufferedReader(StandardCharsets.UTF_8).use { reader ->
                var event = ""
                val data = StringBuilder()
                var seenChars = 0
                while (true) {
                    val line = reader.readLine() ?: throw D05Failure("Antwortstream vor Abschluss unterbrochen.")
                    seenChars += line.length
                    if (seenChars > MAX_STREAM_CHARS) throw D05Failure("Antwortstream zu groß, abgebrochen.")
                    if (line.isEmpty()) {
                        if (data.isNotEmpty()) {
                            try { acc.accept(event, data.toString()) }
                            catch (_: ChatGptPlanProbe.PlanProbeFailure) {
                                throw D05Failure("Antwort fehlgeschlagen oder Kontingent erschöpft.")
                            }
                            if (acc.done) {
                                val reply = try { acc.finalAnswer() }
                                    catch (_: Exception) { throw D05Failure("Leere Antwort erhalten.") }
                                if (reply.length > MAX_REPLY_CHARS)
                                    throw D05Failure("Antwort zu lang für den sicheren Pilotversuch.")
                                return reply
                            }
                        }
                        event = ""
                        data.clear()
                    } else if (line.startsWith("event:")) event = line.substringAfter(":").trim()
                    else if (line.startsWith("data:")) {
                        if (data.isNotEmpty()) data.append('\n')
                        data.append(line.substringAfter(":").trimStart())
                        if (data.length > 16 * 1024) throw D05Failure("Antwortereignis zu groß.")
                    }
                }
            }
            throw D05Failure("Antwortstream ohne Abschluss beendet.")
        } finally { conn.disconnect() }
    }
}
