package com.valoon4.emotionskompass.core.checkin

import com.valoon4.emotionskompass.core.model.Emotion
import com.valoon4.emotionskompass.core.model.EmotionCatalog
import com.valoon4.emotionskompass.core.model.EmotionComparison

/**
 * An explainable, entirely offline map from everyday descriptions to optional vocabulary.
 * These are prompts, not inferred psychological states, risk assessments or diagnoses.
 */
enum class FeelingDirection(val label: String, val explanation: String, val suggestions: List<String>) {
    ANGESPANNT("Angespannt", "Viel innere oder körperliche Spannung", listOf(
        "anspannung", "nervositaet", "unruhe", "ueberforderung", "besorgnis", "angst", "beklemmung")),
    TRAURIG("Traurig", "Etwas fühlt sich schwer oder schmerzlich an", listOf(
        "traurigkeit", "kummer", "enttaeuschung", "wehmut", "einsamkeit", "trauer", "verlassenheit")),
    AERGERLICH("Ärgerlich", "Etwas stört, verletzt oder überschreitet eine Grenze", listOf(
        "aerger", "frustration", "gereiztheit", "wut", "verletztheit", "empoerung", "ungeduld")),
    UNSICHER("Unsicher", "Vieles ist unklar oder schwer einzuschätzen", listOf(
        "unsicherheit", "zweifel", "besorgnis", "angst", "verunsicherung", "hilflosigkeit", "orientierungslosigkeit")),
    ANGENEHM("Angenehm", "Etwas tut gut oder macht Freude", listOf(
        "freude", "zufriedenheit", "erleichterung", "heiterkeit", "stolz", "vorfreude", "gelassenheit")),
    VERBUNDEN("Verbunden", "Nähe zu Menschen oder etwas Wichtigem", listOf(
        "geborgenheit", "zuneigung", "liebe", "dankbarkeit", "zugehoerigkeit", "vertrauen", "sehnsucht")),
    UNKLAR("Schwer zu sagen", "Es ist in Ordnung, noch kein Wort zu haben", listOf(
        "verwirrung", "unsicherheit", "unbehagen", "innere_leere", "ueberwaeltigung", "neugier"))
}

/** Limits only the initial results; users can explicitly open the complete catalog. */
object FeelingDiscovery {
    const val DEFAULT_LIMIT = 8

    fun suggestions(
        catalog: EmotionCatalog,
        direction: FeelingDirection?,
        query: String,
        family: String,
        showAll: Boolean
    ): List<Emotion> {
        val search = query.trim()
        if (search.isNotBlank()) return catalog.search(search, family)
        if (showAll) return catalog.search("", family)
        if (direction == null) return emptyList()
        return direction.suggestions.mapNotNull(catalog::find)
            .filter { family == "Alle" || family in it.families }
            .distinctBy { it.id }
            .take(DEFAULT_LIMIT)
    }

    /**
     * Prioritise a curated direct comparison of two selected terms. When none exists,
     * offer at most two available comparisons involving a selected term instead.
     * We never manufacture a contrast for a pair without curated content.
     */
    fun contrasts(catalog: EmotionCatalog, selected: Set<String>): List<EmotionComparison> {
        if (selected.isEmpty()) return emptyList()
        val directlySelected = catalog.comparisons.filter {
            it.first in selected && it.second in selected
        }
        val connected = catalog.comparisons.filter {
            (it.first in selected || it.second in selected) && it !in directlySelected
        }
        return (directlySelected + connected).take(2)
    }
}
