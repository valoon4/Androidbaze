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
            "Eine realistische, respektvolle Rückfrage bei der ersten Absage.",
            "Bei einer ersten höflichen Absage: Zeige kurz echte Enttäuschung und frage, " +
                "falls passend, genau einmal nach einer deutlich kleineren freiwilligen Hilfe. " +
                "Ein erneutes klares Nein akzeptierst du sofort. " +
                "Keine Vorwürfe, kein Überreden, keine Schuldgefühle und keine Drohungen.",
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

    /**
     * Turn-aware, safety-bounded difficulty. The model is asked to vary the
     * interpersonal challenge, but it may never disregard a repeated boundary.
     * This is an instruction, NOT a guarantee of specific model behavior.
     */
    fun responseCue(scenarioId: String, level: Level, completedUserTurns: Int): String {
        require(completedUserTurns in 0 until D05TrainingSession.MAX_USER_TURNS)
        if (scenarioId != "boundary") return when (level) {
            Level.START -> "Antworte verständnisvoll und benenne ein Gefühl auf Nachfrage."
            Level.DAILY -> "Erkläre ein Gefühl oder einen Wunsch etwas genauer, wenn zugehört wird."
            Level.CHALLENGE -> "Lass eine sanfte Ambivalenz erkennen; keine Vorwürfe oder Schuldzuweisungen."
        }
        return when (level) {
            Level.START -> "Akzeptiere bereits die erste höfliche Absage ohne Nachverhandlung."
            Level.DAILY -> if (completedUserTurns == 0)
                "Nach einer ersten Absage ist höchstens eine kurze, freiwillige kleinere Nachfrage möglich; keine Pflicht dazu."
            else "Eine wiederholte klare Grenze wird ohne neue Bitte respektiert."
            Level.CHALLENGE -> if (completedUserTurns == 0)
                "Dies ist die erste Antwort des Gegenübers auf deine Bitte. " +
                    "Wenn es höflich ablehnt, gib nicht sofort auf: Stelle genau EINE " +
                    "realistische, klar kleinere und freiwillige Rückfrage (z. B. nur einen Ansprechpartner nennen), " +
                    "außer das Gegenüber hat Nachfragen ausdrücklich ausgeschlossen. " +
                    "Betone keine Schuld und lege keinerlei Druck auf. Respektiere das Nein als gültig."
            else
                "Die Person hat schon mindestens einmal geantwortet. " +
                    "Wenn sie ihre Grenze erneut deutlich macht, akzeptiere sie nun ausdrücklich; " +
                    "stelle KEINE weitere Bitte. Ein früheres Nein bleibt gültig. " +
                    "Wenn sie freiwillig Hilfe anbietet, reagiere passend ohne neue Forderungen."
        }
    }

    fun reflectionIdeasFor(scenarioId: String): List<String> = when (scenarioId) {
        "boundary" -> listOf(
            "Was genau an deiner Antwort war ein Nein – war es für dich selbst deutlich?",
            "Wie hast du dich beim Ablehnen gefühlt: sicher, unsicher oder ganz anders?",
            "Was hast du an der Reaktion der Kollegin bemerkt?"
        )
        "listening" -> listOf(
            "Welches Gefühl hast du bei deinem Freund wahrgenommen – und wie sicher bist du dir?",
            "Welche deiner Fragen hat dem Freund Raum zum Erzählen gegeben?",
            "Gab es einen Moment, in dem du lieber sofort eine Lösung anbieten wolltest?"
        )
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
