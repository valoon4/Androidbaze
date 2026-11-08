package com.valoon4.communicationtrainer.inference

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * First D05.1 learning layer: transparent, deterministic, offline guidance.
 * Not AI-generated feedback and never an assessment of someone's ability.
 */
object D051LearningGuide {
    enum class Level(
        val label: String,
        val explanation: String,
        val boundaryInstruction: String,
        val listeningInstruction: String
    ) {
        START(
            "Einstieg",
            "Ruhig und verständnisvoll – gut für erste Versuche.",
            "Bitte zunächst um Hilfe, aber akzeptiere eine klare Absage ohne weitere Druckversuche.",
            "Drücke Gefühle offen aus, lasse Pausen zu und beantworte offene Fragen ruhig."
        ),
        DAILY(
            "Alltag",
            "Natürliche Rückfragen und leichte Unsicherheit.",
            "Stelle bei der ersten Absage höchstens eine kleinere Nachfrage. Akzeptiere eine erneute klare Grenze.",
            "Sei etwas unsicher, was hinter der Enttäuschung steckt, und werde bei einfühlsamen Fragen konkreter."
        ),
        CHALLENGE(
            "Anspruchsvoll",
            "Etwas mehr Widerstand, aber immer respektvoll.",
            "Reagiere einmal beharrlich oder enttäuscht, ohne zu drohen oder Schuldgefühle zu erzeugen. Akzeptiere anschließend eine klare Grenze.",
            "Zeige zwiespältige Gefühle und spreche ein Missverständnis an, ohne zu beschuldigen oder zu beleidigen."
        )
    }

    data class Objective(
        val headline: String,
        val cue: String,
        val reflectionPrompt: String,
        val observationChoices: List<String>
    )

    val level = MutableStateFlow(Level.DAILY)

    fun objectiveFor(scenarioId: String): Objective = when (scenarioId) {
        "boundary" -> Objective(
            "Eine Grenze freundlich und klar setzen",
            "Antworte auf die konkrete Bitte. Formuliere, was du nicht übernehmen kannst, ohne die Person abzuwerten.",
            "Was hat dir geholfen, bei deiner Grenze zu bleiben? Was würdest du beim nächsten Mal anders sagen?",
            listOf(
                "Ich habe mein Nein klar ausgedrückt.",
                "Ich bin respektvoll geblieben.",
                "Ich habe meine Grenze auch bei Nachfragen vertreten."
            )
        )
        "listening" -> Objective(
            "Gefühle und Wünsche verstehen",
            "Spiegle zunächst, was dein Freund fühlen könnte, und frage nach, bevor du Lösungen anbietest.",
            "Welches Gefühl oder welcher Wunsch wurde im Gespräch deutlicher? Was möchtest du beim nächsten Mal ausprobieren?",
            listOf(
                "Ich habe ein Gefühl vorsichtig gespiegelt.",
                "Ich habe eine offene Frage gestellt.",
                "Ich habe nicht vorschnell eine Lösung vorgeschlagen."
            )
        )
        else -> error("Unbekanntes Trainingsszenario")
    }

    fun instructionFor(scenarioId: String, level: Level): String = when (scenarioId) {
        "boundary" -> level.boundaryInstruction
        "listening" -> level.listeningInstruction
        else -> error("Unbekanntes Trainingsszenario")
    }

    /** Changing the level discards the current session, never covertly swaps its prompt. */
    fun selectLevel(next: Level) {
        if (D05TrainingSession.busy.value || level.value == next) return
        level.value = next
        D05TrainingSession.clearConversation()
    }
}

/**
 * Local, voluntary self-observation only. No score, no remote model request,
 * and no persistence. Resets on scenario/level/model changes and account switch.
 */
object D051LocalReflection {
    private const val MAX_NOTE = 400
    val selectedObservations = MutableStateFlow<Set<Int>>(emptySet())
    val note = MutableStateFlow("")
    val acknowledged = MutableStateFlow(false)

    fun toggle(index: Int, scenarioId: String, checked: Boolean) {
        if (D05TrainingSession.busy.value || index !in D051LearningGuide.objectiveFor(scenarioId).observationChoices.indices)
            return
        selectedObservations.value =
            if (checked) selectedObservations.value + index else selectedObservations.value - index
        acknowledged.value = false
    }

    fun updateNote(value: String) {
        if (D05TrainingSession.busy.value || value.length > MAX_NOTE) return
        note.value = value
        acknowledged.value = false
    }

    fun confirm(): Boolean {
        if (D05TrainingSession.busy.value || D05TrainingSession.turns.value.none { it.role == "user" })
            return false
        if (selectedObservations.value.isEmpty() && note.value.isBlank()) return false
        acknowledged.value = true
        return true
    }

    fun reset() {
        selectedObservations.value = emptySet()
        note.value = ""
        acknowledged.value = false
    }
}
