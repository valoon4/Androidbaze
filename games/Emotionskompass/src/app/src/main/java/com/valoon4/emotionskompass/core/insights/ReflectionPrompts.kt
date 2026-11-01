package com.valoon4.emotionskompass.core.insights

/**
 * Offline editorial questions. A prompt is not an interpretation, diagnosis or
 * recommendation. The user decides whether and what to write.
 */
enum class ReflectionRoute { OPEN, OBSERVATION }

data class ReflectionPrompt(
    val id: String,
    val stepName: String,
    val question: String
)

object ReflectionPrompts {
    const val MAX_ANSWER_LENGTH = 550

    val open: List<ReflectionPrompt> = listOf(
        ReflectionPrompt(
            "open-notice", "Wahrnehmen",
            "Was ist dir an dieser Situation besonders aufgefallen?"
        ),
        ReflectionPrompt(
            "open-need", "Verstehen",
            "Was war dir in diesem Moment wichtig, oder was hättest du gebraucht?"
        ),
        ReflectionPrompt(
            "open-choice", "Weiterdenken",
            "Welchen Gedanken möchtest du für dich mitnehmen? Du musst nichts verändern."
        )
    )

    val observation: List<ReflectionPrompt> = listOf(
        ReflectionPrompt(
            "pattern-specific", "Erinnern",
            "Wenn du an diese Einträge denkst: Welche konkreten Situationen fallen dir ein?"
        ),
        ReflectionPrompt(
            "pattern-differences", "Unterschiede sehen",
            "Worin haben sich die Situationen unterschieden? Vielleicht gibt es keine klare Gemeinsamkeit."
        ),
        ReflectionPrompt(
            "pattern-own-meaning", "Eigene Sicht",
            "Was möchtest du selbst dazu festhalten – oder was bleibt für dich offen?"
        )
    )

    fun forRoute(route: ReflectionRoute): List<ReflectionPrompt> =
        when (route) {
            ReflectionRoute.OPEN -> open
            ReflectionRoute.OBSERVATION -> observation
        }

    /**
     * Only hand-authored responses become a draft, NEVER any prompt wording or
     * observation label. An empty result is valid, but cannot be saved.
     */
    fun userDraft(route: ReflectionRoute, answers: List<String>): String {
        require(answers.size == forRoute(route).size)
        require(answers.all { it.length <= MAX_ANSWER_LENGTH })
        return answers.map { it.trim() }.filter { it.isNotEmpty() }.joinToString("\n\n")
            .also { require(it.length <= PersonalInsight.MAX_TEXT_LENGTH) }
    }
}
