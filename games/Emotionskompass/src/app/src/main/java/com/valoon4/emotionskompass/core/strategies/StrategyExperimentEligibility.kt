package com.valoon4.emotionskompass.core.strategies

/**
 * Editorial allowlist for the opt-in "vormerken" action.
 * New/changed catalog entries are deliberately ineligible until reviewed.
 * Explanation and warning-pattern cards must never become self-experiment CTAs.
 * Eligibility is NOT an individual health or safety recommendation.
 */
object StrategyExperimentEligibility {
    private val approvedIds = setOf(
        "problem-solving", "intentional-distraction", "reappraisal", "social-support",
        "acceptance", "planning", "prioritizing", "practical-support", "boundaries",
        "changing-situation", "gentle-breath", "grounding", "muscle-relaxation",
        "name-feelings", "mindful-observation", "movement", "assumptions",
        "perspective-taking", "self-compassion", "compassionate-self-talk",
        "rumination-interrupt", "emotional-support", "communicate-needs",
        "dialogue", "values-clarification", "values-small-step",
        "restorative-rest", "protective-withdrawal"
    )

    fun canOffer(card: StrategyCard): Boolean =
        card.id in approvedIds && "patterns" !in card.familyIds &&
            card.limitations.isNotBlank() && card.whenUseful.isNotBlank()

    fun canOffer(catalog: StrategyCatalog?, id: String): Boolean =
        catalog?.find(id)?.let(::canOffer) == true
}
