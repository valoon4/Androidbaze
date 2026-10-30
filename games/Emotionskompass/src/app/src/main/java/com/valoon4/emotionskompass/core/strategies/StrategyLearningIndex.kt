package com.valoon4.emotionskompass.core.strategies

import com.valoon4.emotionskompass.core.lessons.KnowledgeLesson
import com.valoon4.emotionskompass.core.lessons.LessonCatalog

/**
 * Editorial learning paths between existing reference cards and the M6.3.2 articles.
 * No inference from users' feelings, journal entries or personal needs.
 * A mapping is an educational cross-reference, never a claim of effectiveness.
 */
object StrategyLearningIndex {
    val deeperArticleIdByStrategy: Map<String, String> = mapOf(
        "problem-solving" to "s-vertiefung-einfluss",
        "intentional-distraction" to "s-vertiefung-abstand",
        "reappraisal" to "s-vertiefung-neubewertung",
        "social-support" to "s-vertiefung-sozial",
        "acceptance" to "s-vertiefung-akzeptanz",
        "planning" to "s-vertiefung-kombinieren",
        "prioritizing" to "s-vertiefung-einfluss",
        "practical-support" to "s-vertiefung-sozial",
        "boundaries" to "s-vertiefung-sozial",
        "changing-situation" to "s-vertiefung-belastung",
        "gentle-breath" to "s-vertiefung-koerper",
        "grounding" to "s-vertiefung-koerper",
        "muscle-relaxation" to "s-vertiefung-koerper",
        "name-feelings" to "s-vertiefung-begriffe",
        "mindful-observation" to "s-vertiefung-koerper",
        "movement" to "s-vertiefung-koerper",
        "assumptions" to "s-vertiefung-neubewertung",
        "perspective-taking" to "s-vertiefung-neubewertung",
        "self-compassion" to "s-vertiefung-anpassen",
        "compassionate-self-talk" to "s-vertiefung-anpassen",
        "rumination-interrupt" to "s-vertiefung-anpassen",
        "emotional-support" to "s-vertiefung-sozial",
        "communicate-needs" to "s-vertiefung-begriffe",
        "dialogue" to "s-vertiefung-sozial",
        "values-clarification" to "s-vertiefung-flexibilitaet",
        "values-small-step" to "s-vertiefung-kombinieren",
        "restorative-rest" to "s-vertiefung-belastung",
        "protective-withdrawal" to "s-vertiefung-abstand",
        "prolonged-avoidance" to "s-vertiefung-abstand",
        "emotional-suppression" to "s-vertiefung-modelle"
    )

    /** Keep old links stable. Avoid displaying generic "Artikel ansehen" placeholders. */
    fun articlesFor(card: StrategyCard, lessons: LessonCatalog): List<KnowledgeLesson> =
        (card.relatedLessonIds + listOfNotNull(deeperArticleIdByStrategy[card.id]))
            .distinct()
            .mapNotNull(lessons::find)
            .take(3)

    /** Only explicitly editorially tagged need links. Never use fuzzy matching. */
    fun strategiesForNeed(needId: String, catalog: StrategyCatalog): List<StrategyCard> =
        catalog.strategies.filter { needId in it.relatedNeedIds &&
            "patterns" !in it.familyIds }.take(3)
}
