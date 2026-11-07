package com.valoon4.communicationtrainer.data

/**
 * A closed, deterministic fixture. It does not model a person, interpret sentiment,
 * infer user intentions, connect to a network or pretend to be a real AI response.
 * Later persona-specific simulations and InferenceGateway are explicitly out of D03.
 */
object MockConversationEngine {
    fun answer(userText: String): String {
        val normalized = userText.trim().lowercase()
        return when {
            normalized.contains("hallo") || normalized.contains("hi ") || normalized == "hi" ->
                "Hallo! Danke für deinen Einstieg. Wie möchtest du dieses Übungsgespräch fortsetzen?"
            normalized.contains("treffen") || normalized.contains("termin") ->
                "Für unser Testgespräch: Welcher Zeitpunkt würde dir gut passen?"
            normalized.contains("grenze") || normalized.contains("nein") ->
                "In dieser Simulation kannst du deine Aussage noch einmal klar und freundlich formulieren."
            normalized.contains("frage") || normalized.contains("?") ->
                "Das ist eine vordefinierte Beispielantwort. Welche Frage möchtest du als Nächstes ausprobieren?"
            else ->
                "Danke für deine Nachricht. Welche Antwort würdest du dir in einer echten Unterhaltung wünschen?"
        }
    }
}
