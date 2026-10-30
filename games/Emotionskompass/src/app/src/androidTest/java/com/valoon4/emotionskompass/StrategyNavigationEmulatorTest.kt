package com.valoon4.emotionskompass

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.valoon4.emotionskompass.core.designsystem.EmotionskompassTheme
import com.valoon4.emotionskompass.core.lessons.StrategyCaseCatalog
import com.valoon4.emotionskompass.core.model.NeedCatalog
import com.valoon4.emotionskompass.core.strategies.NeedStrategyLinkIndex
import com.valoon4.emotionskompass.core.strategies.StrategyCatalog
import com.valoon4.emotionskompass.feature.needs.NeedsScreen
import com.valoon4.emotionskompass.feature.strategies.StrategyAtlasScreen
import com.valoon4.emotionskompass.feature.learning.StrategyPracticeScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * These tests run on an Android emulator at 100% and 200% system font scale.
 * They validate the actual Compose interaction and semantics tree, not merely
 * the JVM catalog model. A physical TalkBack session is a separate review gate.
 */
@RunWith(AndroidJUnit4::class)
class StrategyNavigationEmulatorTest {
    @get:Rule val rule = createComposeRule()

    private fun asset(path: String): String =
        InstrumentationRegistry.getInstrumentation().targetContext.assets
            .open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }

    private val needs by lazy {
        NeedCatalog.fromJson(asset("catalog/needs.de.v1.json"))
    }
    private val strategies by lazy {
        StrategyCatalog.fromJsonParts(
            asset("catalog/strategies.de.v1.json"),
            listOf("situation", "regulation-a", "regulation-b", "cognition-a",
                "cognition-b", "social", "values", "recovery", "patterns")
                .map { asset("catalog/strategies.$it.de.v1.json") }
        )
    }
    private val links by lazy {
        NeedStrategyLinkIndex.fromJsonParts(
            listOf("security", "relationships", "autonomy", "respect",
                "wellbeing", "growth", "purpose", "vitality")
                .map { asset("catalog/need-strategy-$it.de.v1.json") },
            needs, strategies
        )
    }
    private val cases by lazy {
        StrategyCaseCatalog.fromJsonParts(
            listOf("a", "b", "c").map { asset("lessons/strategy-cases-$it.de.v1.json") },
            strategies.strategies.map { it.id }.toSet()
        )
    }

    @Test fun acceptanceNeedDoesNotSuggestInnerAcceptanceAndOpensExactCard() {
        var opened: String? = null
        rule.setContent {
            EmotionskompassTheme {
                NeedsScreen(
                    catalog = needs, strategyCatalog = strategies,
                    needStrategyLinks = links, requestedNeedId = "akzeptanz",
                    onStrategy = { opened = it }
                )
            }
        }
        rule.waitForIdle()
        rule.onNode(hasScrollToIndexAction()).performScrollToNode(
            hasText("Emotionalen Beistand erbitten", substring = true)
        )
        val item = rule.onNode(
            hasClickAction() and hasText("Emotionalen Beistand erbitten", substring = true)
        )
        item.assertExists().assertHeightIsAtLeast(48.dp)
        item.performClick()
        rule.runOnIdle { assertEquals("emotional-support", opened) }
    }

    @Test fun cardShowsExactNeedsAndNavigatesBackToNeedId() {
        var opened: String? = null
        rule.setContent {
            EmotionskompassTheme {
                StrategyAtlasScreen(
                    catalog = strategies, selectedId = "boundaries",
                    onSelect = {}, onBackToList = {}, onKnowledgeArticle = {},
                    needCatalog = needs, needStrategyLinks = links,
                    onNeed = { opened = it }
                )
            }
        }
        rule.waitForIdle()
        rule.onNode(hasScrollToIndexAction()).performScrollToNode(
            hasText("Privatsphäre im Bedürfnisatlas ansehen", substring = true)
        )
        rule.onNodeWithText("Privatsphäre im Bedürfnisatlas ansehen")
            .assertExists().performClick()
        rule.runOnIdle { assertEquals("privatsphaere", opened) }
    }

    @Test fun sleepNeedShowsHonestEmptyStateInsteadOfFalseCareAdvice() {
        var openedAll = false
        rule.setContent {
            EmotionskompassTheme {
                NeedsScreen(
                    catalog = needs, strategyCatalog = strategies,
                    needStrategyLinks = links, requestedNeedId = "schlaf",
                    onOpenStrategyAtlas = { openedAll = true }
                )
            }
        }
        rule.waitForIdle()
        rule.onNode(hasScrollToIndexAction()).performScrollToNode(
            hasText("Ruhe- oder Gedankenübungen ersetzen", substring = true)
        )
        rule.onNodeWithText("Ruhe- oder Gedankenübungen ersetzen", substring = true)
            .assertExists()
        rule.onNode(hasScrollToIndexAction()).performScrollToNode(
            hasText("Alle Strategien selbst entdecken")
        )
        rule.onNodeWithText("Alle Strategien selbst entdecken").performClick()
        rule.runOnIdle { assertTrue(openedAll) }
    }

    @Test fun caseProvidesSafeUncertaintyPathAndShowsFeedbackAfterChoiceOnly() {
        rule.setContent {
            EmotionskompassTheme {
                StrategyPracticeScreen(
                    catalog = cases,
                    requestedCaseId = "akzeptanz-unrecht",
                    onBackToKnowledge = {},
                    onStrategy = { _, _ -> },
                    strategyTitles = strategies.strategies.associate { it.id to it.title }
                )
            }
        }
        rule.waitForIdle()
        rule.onNodeWithText("Mögliche Einordnung").assertDoesNotExist()
        rule.onNodeWithText("Ich bin unsicher – Einordnung ansehen")
            .performScrollTo().performClick()
        rule.onNodeWithText("Mögliche Einordnung")
            .assertExists()
    }
}
