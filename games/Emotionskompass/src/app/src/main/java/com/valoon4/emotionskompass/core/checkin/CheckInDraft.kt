package com.valoon4.emotionskompass.core.checkin

/**
 * Ephemeral personal check-in. This model is deliberately never serialised or written to disk.
 * On leaving the navigator it is replaced with an empty instance; M4 will design durable storage.
 */
enum class CheckInMode(val label: String) {
    KURZ("Kurz"), VERTIEFT("Vertieft")
}

enum class CheckInStep {
    ANKOMMEN, BENENNEN, NACHSPUEREN, AUSKLANG;

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
    val note: String = ""
) {
    val selectedCount: Int get() = emotions.size

    val progress: Pair<Int, Int> get() = when (mode) {
        CheckInMode.VERTIEFT -> (step.ordinal + 1) to 4
        CheckInMode.KURZ -> step.ordinal.coerceAtLeast(1) to 3
    }

    fun withMode(nextMode: CheckInMode): CheckInDraft =
        copy(mode = nextMode,
            step = if (nextMode == CheckInMode.KURZ && step == CheckInStep.ANKOMMEN)
                CheckInStep.BENENNEN else step)

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

    fun next(): CheckInDraft = copy(step = step.next())
    fun previous(): CheckInDraft = copy(
        step = if (isFirstStep) step else step.previous()
    )

    companion object {
        const val MAX_NOTE_LENGTH = 500

        fun startWith(emotionId: String?): CheckInDraft =
            if (emotionId.isNullOrBlank()) CheckInDraft()
            else CheckInDraft(emotions = mapOf(emotionId to null))
    }
}
