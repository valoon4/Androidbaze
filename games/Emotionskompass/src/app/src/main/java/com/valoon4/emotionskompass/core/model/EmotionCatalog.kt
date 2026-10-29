package com.valoon4.emotionskompass.core.model

/** Redaktions-Demodaten für M1. Vollständiger, fachlich geprüfter Atlas folgt in M2. */
data class Emotion(
    val id: String,
    val name: String,
    val family: String,
    val explanation: String,
    val example: String,
    val related: List<String> = emptyList()
)

object EmotionCatalog {
    val families: List<String> = listOf(
        "Alle", "Freude", "Verbundenheit", "Traurigkeit", "Unsicherheit",
        "Ärger", "Selbstbewertung", "Staunen", "Ruhe"
    )

    val emotions: List<Emotion> = listOf(
        Emotion("zufriedenheit", "Zufriedenheit", "Freude",
            "Ein ruhiges Gefühl, bei dem etwas im Moment stimmig oder ausreichend erscheint.",
            "Nach einem erfüllten Tag hast du das Gefühl, gerade nichts verändern zu müssen.",
            listOf("gelassenheit", "erleichterung")),
        Emotion("vorfreude", "Vorfreude", "Freude",
            "Angenehme Erwartung auf etwas, das erst noch bevorsteht.",
            "Du denkst an ein Treffen und merkst, wie du innerlich lebendig wirst.",
            listOf("nervositaet", "begeisterung")),
        Emotion("begeisterung", "Begeisterung", "Freude",
            "Ein lebendiges Gefühl von Interesse, Freude und innerer Beteiligung.",
            "Ein neues Projekt fasziniert dich so sehr, dass du sofort loslegen möchtest.",
            listOf("vorfreude", "staunen")),
        Emotion("geborgenheit", "Geborgenheit", "Verbundenheit",
            "Das Erleben von Schutz, Zugehörigkeit und emotionaler Sicherheit.",
            "Mit einem vertrauten Menschen kannst du dich entspannen und einfach du selbst sein.",
            listOf("dankbarkeit", "gelassenheit")),
        Emotion("dankbarkeit", "Dankbarkeit", "Verbundenheit",
            "Wertschätzung für etwas, das du erhalten oder erlebt hast.",
            "Jemand hört dir aufmerksam zu und du fühlst dich dadurch getragen.",
            listOf("geborgenheit", "zufriedenheit")),
        Emotion("wehmut", "Wehmut", "Traurigkeit",
            "Leise Traurigkeit, die oft mit einer schönen Erinnerung oder Vergänglichkeit verbunden ist.",
            "Ein altes Foto erinnert dich an eine Zeit, die du vermisst.",
            listOf("melancholie", "enttaeuschung")),
        Emotion("melancholie", "Melancholie", "Traurigkeit",
            "Eine nachdenkliche, häufig sanfte Form der Traurigkeit.",
            "An einem regnerischen Sonntag fühlst du dich still und etwas schwer.",
            listOf("wehmut", "einsamkeit")),
        Emotion("einsamkeit", "Einsamkeit", "Traurigkeit",
            "Das schmerzhafte Gefühl, sich nicht ausreichend verbunden zu fühlen.",
            "Obwohl Menschen um dich herum sind, vermisst du echte Nähe.",
            listOf("geborgenheit", "melancholie")),
        Emotion("unsicherheit", "Unsicherheit", "Unsicherheit",
            "Das Gefühl, etwas nicht ausreichend einschätzen oder vorhersagen zu können.",
            "Vor einer wichtigen Entscheidung weißt du noch nicht, welchen Weg du gehen möchtest.",
            listOf("nervositaet", "besorgnis")),
        Emotion("nervositaet", "Nervosität", "Unsicherheit",
            "Innere Aktivierung, die mit Erwartung, Anspannung oder Sorge verbunden sein kann.",
            "Kurz vor einem Gespräch bemerkst du ein schnelleres Herzklopfen.",
            listOf("vorfreude", "unsicherheit")),
        Emotion("besorgnis", "Besorgnis", "Unsicherheit",
            "Gedanken über mögliche Schwierigkeiten, die dir wichtig erscheinen.",
            "Du wartest auf eine Nachricht und überlegst, ob alles gut gegangen ist.",
            listOf("unsicherheit", "frustration")),
        Emotion("frustration", "Frustration", "Ärger",
            "Unzufriedenheit oder Ärger, wenn ein Vorhaben auf Hindernisse stößt.",
            "Du versuchst mehrfach, eine Aufgabe zu lösen, kommst aber nicht weiter.",
            listOf("enttaeuschung", "gereiztheit")),
        Emotion("enttaeuschung", "Enttäuschung", "Ärger",
            "Das unangenehme Erleben, wenn eine Hoffnung oder Erwartung unerfüllt bleibt.",
            "Du freust dich auf ein Treffen, das kurzfristig abgesagt wird.",
            listOf("frustration", "wehmut")),
        Emotion("gereiztheit", "Gereiztheit", "Ärger",
            "Erhöhte Empfindlichkeit gegenüber kleinen Störungen oder Belastungen.",
            "Nach einem anstrengenden Tag ärgern dich Kleinigkeiten stärker als sonst.",
            listOf("frustration", "besorgnis")),
        Emotion("verlegenheit", "Verlegenheit", "Selbstbewertung",
            "Ein unangenehmes Gefühl, wenn man sich unerwartet im Mittelpunkt der Aufmerksamkeit sieht.",
            "Du versprichst dich vor einer Gruppe und wirst rot.",
            listOf("unsicherheit")),
        Emotion("reue", "Reue", "Selbstbewertung",
            "Bedauern über eine eigene Handlung, die man rückblickend anders machen möchte.",
            "Du erinnerst dich an harte Worte und würdest sie gern zurücknehmen.",
            listOf("enttaeuschung")),
        Emotion("staunen", "Staunen", "Staunen",
            "Ein Moment von Aufmerksamkeit und Überraschung angesichts von etwas Unerwartetem.",
            "Du siehst zum ersten Mal einen besonders klaren Sternenhimmel.",
            listOf("begeisterung")),
        Emotion("erleichterung", "Erleichterung", "Ruhe",
            "Nachlassende Anspannung, wenn eine Belastung oder Ungewissheit wegfällt.",
            "Eine schwierige Aufgabe ist geschafft und du kannst durchatmen.",
            listOf("gelassenheit", "zufriedenheit")),
        Emotion("gelassenheit", "Gelassenheit", "Ruhe",
            "Ein Zustand innerer Ruhe, in dem Herausforderungen weniger bedrohlich erscheinen.",
            "Obwohl etwas anders läuft als geplant, bleibst du ruhig.",
            listOf("zufriedenheit", "erleichterung"))
    )

    fun find(id: String): Emotion? = emotions.firstOrNull { it.id == id }

    fun search(query: String, family: String = "Alle"): List<Emotion> {
        val trimmed = query.trim()
        return emotions
            .filter { family == "Alle" || it.family == family }
            .filter {
                trimmed.isEmpty() || it.name.contains(trimmed, ignoreCase = true) ||
                    it.explanation.contains(trimmed, ignoreCase = true)
            }
            .sortedWith(compareBy<Emotion> {
                if (trimmed.isNotBlank() && it.name.startsWith(trimmed, ignoreCase = true)) 0 else 1
            }.thenBy { it.name })
    }
}
