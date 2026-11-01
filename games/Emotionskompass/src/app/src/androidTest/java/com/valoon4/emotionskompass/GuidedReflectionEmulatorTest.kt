package com.valoon4.emotionskompass

import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.valoon4.emotionskompass.core.designsystem.EmotionskompassTheme
import com.valoon4.emotionskompass.core.navigation.InsightUiState
import com.valoon4.emotionskompass.feature.insights.PersonalInsightsScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GuidedReflectionEmulatorTest {
    @get:Rule val rule = createComposeRule()

    @Test fun freeGuideProducesOnlyUserAuthoredTextAndNeverAutosaves() {
        var saves = 0
        var savedText = ""
        rule.setContent {
            EmotionskompassTheme {
                PersonalInsightsScreen(
                    state = InsightUiState(loaded = true), journalEntries = emptyList(),
                    onBack = {}, onReload = {},
                    onSave = { text, _, _ -> saves++; savedText = text },
                    onArchive = { _, _ -> }, onDelete = {},
                    onUnlink = { _, _ -> }, onOpenJournal = {}
                )
            }
        }

        rule.onNode(hasScrollToIndexAction()).performScrollToNode(
            hasTestTag("insight-guided-reflection")
        )
        rule.onNodeWithTag("insight-guided-reflection").performClick()
        rule.onNodeWithTag("guided-reflection-steps").performScrollToNode(
            hasTestTag("reflection-answer")
        )
        rule.onNodeWithTag("reflection-answer").performTextInput(
            "Ich bemerke, dass mir eine Pause guttut."
        )
        rule.onNodeWithTag("reflection-next").performClick()
        rule.onNodeWithTag("reflection-next").performClick()
        rule.onNodeWithTag("reflection-to-editor").performClick()
        rule.runOnIdle { assertEquals(0, saves) }

        rule.onNodeWithTag("insight-editor-list").performScrollToNode(
            hasTestTag("insight-editor")
        )
        rule.onNodeWithTag("insight-editor")
            .assertTextContains("Ich bemerke, dass mir eine Pause guttut.")
        rule.onNodeWithText("Ausdrücklich speichern").performClick()
        rule.runOnIdle {
            assertEquals(1, saves)
            assertEquals("Ich bemerke, dass mir eine Pause guttut.", savedText.trim())
        }
    }

    @Test fun observationIsOnlyContextAndSkippedGuideCannotSaveAnEmptyInsight() {
        var saves = 0
        rule.setContent {
            EmotionskompassTheme {
                PersonalInsightsScreen(
                    state = InsightUiState(loaded = true), journalEntries = emptyList(),
                    reflectionOriginLabel = "„Freude“ und „Klarheit“",
                    onBack = {}, onReload = {},
                    onSave = { _, _, _ -> saves++ },
                    onArchive = { _, _ -> }, onDelete = {},
                    onUnlink = { _, _ -> }, onOpenJournal = {}
                )
            }
        }
        rule.waitForIdle()
        rule.onNodeWithText("Ausgangspunkt:", substring = true).assertExists()
        rule.onNodeWithTag("reflection-next").performClick()
        rule.onNodeWithTag("reflection-next").performClick()
        rule.onNodeWithTag("reflection-to-editor").performClick()
        rule.onNodeWithTag("insight-editor-list").performScrollToNode(
            hasTestTag("insight-editor")
        )
        rule.onNodeWithText("Ausdrücklich speichern").assertIsNotEnabled()
        rule.runOnIdle { assertEquals(0, saves) }
    }
}
