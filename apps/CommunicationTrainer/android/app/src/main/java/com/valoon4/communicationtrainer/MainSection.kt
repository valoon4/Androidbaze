package com.valoon4.communicationtrainer

/**
 * Only the two initial navigation destinations are available in D01.
 * Free chat, persistence, simulation and AI are intentionally not implemented yet.
 */
enum class MainSection(
    val title: String,
    val headline: String,
    val intro: String
) {
    CHATS(
        title = "Chats",
        headline = "Dein Raum für Gespräche",
        intro = "Hier entstehen später deine freien Gespräche mit fiktiven KI-Charakteren."
    ),
    PRACTICE(
        title = "Üben",
        headline = "Kommunikation ausprobieren",
        intro = "Trainiere später konkrete Alltagssituationen in deinem eigenen Tempo."
    )
}
