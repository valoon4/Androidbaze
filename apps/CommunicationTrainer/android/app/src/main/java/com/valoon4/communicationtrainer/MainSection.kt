package com.valoon4.communicationtrainer

/**
 * The two initial navigation destinations persist through D02.
 * D02 supports local drafts; message sending, simulation and AI begin later.
 */
enum class MainSection(
    val title: String,
    val headline: String,
    val intro: String
) {
    CHATS(
        title = "Chats",
        headline = "Dein Raum für Gespräche",
        intro = "Tausche lokale Testnachrichten aus. Echte KI-Chats folgen später."
    ),
    PRACTICE(
        title = "Üben",
        headline = "Kommunikation ausprobieren",
        intro = "Trainiere später konkrete Alltagssituationen in deinem eigenen Tempo."
    )
}
