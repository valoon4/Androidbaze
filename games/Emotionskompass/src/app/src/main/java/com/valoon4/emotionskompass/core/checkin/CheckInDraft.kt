package com.valoon4.emotionskompass.core.checkin

/**
 * Ephemeral personal check-in. This model is deliberately never serialised or written to disk.
 * On leaving the navigator it is replaced with an empty instance; M4 will design durable storage.
 */
enum class CheckInMode(val label: String) {
    KURZ("Kurz"), VERTIEFT("Vertieft")
}

enum class CheckInStep {
    ANKOMMEN, BENENNEN, NACHSPUEREN, VERSTEHEN, AUSKLANG;

    fun next(): CheckInStep = entries[(ordinal + 1).coerceAtMost(entries.lastIndex)]
    fun previous(): CheckInStep = entries[(ordinal - 1).coerceAtLeast(0)]
}

enum class BodyCue(val label: String) {
    ANSPANNUNG("Anspannung"),
    WAERME("Wärme"),
    ENGE("Enge"),
    LEICHTIGKEIT("Leichtigkeit"),
    UNRUHE("Unruhe"),
    ENTSPANNUNG("Entspannung"),
    MUEDIGKEIT("Müdigkeit"),
    SONSTIGES("Etwas anderes")
}

enum class LifeArea(val label: String) {
    BEZIEHUNGEN("Beziehungen"),
    ALLTAG("Arbeit & Alltag"),
    KOERPER("Körper & Energie"),
    ERINNERUNGEN("Erinnerungen"),
    UNBESTIMMT("Kein klarer Anlass"),
    ANDERES("Etwas anderes")
}

/** Possible needs are prompts chosen by the user, never inferred from their emotion. */
enum class PersonalNeed(val label: String) {
    RUHE("Ruhe"),
    SICHERHEIT("Sicherheit"),
    NAEHE("Nähe & Verbundenheit"),
    VERSTAENDNIS("Verständnis"),
    RESPEKT("Respekt"),
    SELBSTBESTIMMUNG("Selbstbestimmung"),
    KLARHEIT("Klarheit"),
    UNTERSTUETZUNG("Unterstützung"),
    GERECHTIGKEIT("Fairness"),
    ERHOLUNG("Erholung"),
    AUSDRUCK("Ausdruck & Gehörtwerden"),
    RAUM("Raum für mich"),
    ANDERES("Etwas anderes"),
    UNKLAR("Ich weiß es noch nicht")
}

/** An optional self-chosen idea, not automatically prescribed advice. */
enum class NextStepIdea(val label: String) {
    PAUSE("Eine Pause"),
    GESPRAECH("Ein Gespräch"),
    HILFE("Unterstützung erbitten"),
    GRENZE("Eine Grenze klären"),
    INFORMATION("Etwas klären"),
    BEWEGUNG("Bewegung oder frische Luft"),
    FREUNDLICHKEIT("Freundlich mit mir sein"),
    ZEIT("Mir Zeit geben"),
    KEIN_SCHRITT("Gerade nichts tun"),
    EIGENE_IDEE("Meine eigene Idee")
}

/**
 * Missing intensity (null) means "not rated"; 0 means intentionally selected as "kaum spürbar".
 * Zero selected emotions, zero body cues and no note are legitimate outcomes.
 */
data class CheckInDraft(
    val step: CheckInStep = CheckInStep.ANKOMMEN,
    val mode: CheckInMode = CheckInMode.VERTIEFT,
    val bodyCues: Set<BodyCue> = emptySet(),
    val emotions: Map<String, Int?> = emptyMap(),
    val area: LifeArea? = null,
    val note: String = "",
    val situation: String = "",
    val interpretation: String = "",
    val needs: Set<PersonalNeed> = emptySet(),
    val insight: String = "",
    val nextStepIdea: NextStepIdea? = null,
    val nextStepNote: String = ""
) {
    val selectedCount: Int get() = emotions.size

    /** Used to ask for confirmation before discarding a check-in with personal inputs. */
    val hasPersonalInput: Boolean get() =
        emotions.isNotEmpty() || bodyCues.isNotEmpty() || area != null ||
            note.isNotBlank() || situation.isNotBlank() ||
            interpretation.isNotBlank() || needs.isNotEmpty() ||
            insight.isNotBlank() || nextStepIdea != null || nextStepNote.isNotBlank()

    val progress: Pair<Int, Int> get() = when (mode) {
        CheckInMode.VERTIEFT -> (step.ordinal + 1) to 5
        CheckInMode.KURZ -> when (step) {
            CheckInStep.ANKOMMEN, CheckInStep.BENENNEN -> 1 to 3
            CheckInStep.NACHSPUEREN -> 2 to 3
            CheckInStep.VERSTEHEN, CheckInStep.AUSKLANG -> 3 to 3
        }
    }

    fun withMode(nextMode: CheckInMode): CheckInDraft =
        copy(mode = nextMode,
            step = when {
                nextMode == CheckInMode.KURZ && step == CheckInStep.ANKOMMEN ->
                    CheckInStep.BENENNEN
                nextMode == CheckInMode.KURZ && step == CheckInStep.VERSTEHEN ->
                    CheckInStep.AUSKLANG
                else -> step
            })

    val isFirstStep: Boolean get() =
        step == CheckInStep.ANKOMMEN ||
            (mode == CheckInMode.KURZ && step == CheckInStep.BENENNEN)

    fun toggleCue(cue: BodyCue): CheckInDraft =
        copy(bodyCues = if (cue in bodyCues) bodyCues - cue else bodyCues + cue)

    fun toggleEmotion(id: String): CheckInDraft {
        require(id.isNotBlank())
        return copy(emotions = if (emotions.containsKey(id)) emotions - id
            else emotions + (id to null))
    }

    fun withIntensity(id: String, value: Int?): CheckInDraft {
        require(id in emotions) { "Emotion not selected: $id" }
        require(value == null || value in 0..10) { "Intensity must be 0–10" }
        return copy(emotions = emotions + (id to value))
    }

    fun withArea(value: LifeArea?): CheckInDraft = copy(area = value)

    fun withNote(value: String): CheckInDraft =
        copy(note = value.take(MAX_NOTE_LENGTH))

    fun withSituation(value: String): CheckInDraft =
        copy(situation = value.take(MAX_CONTEXT_LENGTH))

    fun withInterpretation(value: String): CheckInDraft =
        copy(interpretation = value.take(MAX_CONTEXT_LENGTH))

    fun toggleNeed(need: PersonalNeed): CheckInDraft = copy(
        needs = when {
            need in needs -> needs - need
            need == PersonalNeed.UNKLAR -> setOf(PersonalNeed.UNKLAR)
            else -> (needs - PersonalNeed.UNKLAR) + need
        }
    )

    fun withInsight(value: String): CheckInDraft =
        copy(insight = value.take(MAX_INSIGHT_LENGTH))

    fun withNextStepIdea(value: NextStepIdea?): CheckInDraft =
        copy(nextStepIdea = value)

    fun withNextStepNote(value: String): CheckInDraft =
        copy(nextStepNote = value.take(MAX_CONTEXT_LENGTH))

    fun next(): CheckInDraft = copy(step = when {
        mode == CheckInMode.KURZ && step == CheckInStep.NACHSPUEREN ->
            CheckInStep.AUSKLANG
        else -> step.next()
    })

    fun previous(): CheckInDraft = copy(step = when {
        isFirstStep -> step
        mode == CheckInMode.KURZ && step == CheckInStep.AUSKLANG ->
            CheckInStep.NACHSPUEREN
        else -> step.previous()
    })

    companion object {
        const val MAX_NOTE_LENGTH = 500
        const val MAX_CONTEXT_LENGTH = 280
        const val MAX_INSIGHT_LENGTH = 400

        fun startWith(emotionId: String?): CheckInDraft =
            if (emotionId.isNullOrBlank()) CheckInDraft()
            else CheckInDraft(emotions = mapOf(emotionId to null))
    }
}
