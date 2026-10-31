package com.valoon4.emotionskompass

import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.valoon4.emotionskompass.core.designsystem.EmotionskompassTheme
import com.valoon4.emotionskompass.core.insights.PersonalInsight
import com.valoon4.emotionskompass.core.navigation.PersonalInsightUiState
import com.valoon4.emotionskompass.feature.insights.PersonalInsightsScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersonalInsightsEmulatorTest {
    @get:Rule val rule = createComposeRule()

    @Test fun theEditorSavesOnlyAfterExplicitConfirmation() {
        var written: String? = null
        rule.setContent {
            EmotionskompassTheme {
                PersonalInsightsScreen(
                    state = PersonalInsightUiState(loaded = true),
                    journal = emptyList(), journalLoaded = true,
                    onReload = {}, onSave = { text, _, _ -> written = text },
                    onArchive = { _, _ -> }, onDelete = {},
                    onOpenJournalEntry = {}
                )
            }
        }
        rule.onNode(hasScrollToIndexAction()).performScrollToNode(
            hasText("Eigene Erkenntnis schreiben"))
        rule.onNodeWithText("Eigene Erkenntnis schreiben").performClick()
        rule.onNodeWithTag("personal-insight-editor").performTextInput("Eine bewusst formulierte Test-Erkenntnis.")
        rule.runOnIdle { assertNull(written) }
        rule.onNodeWithText("Ausdrücklich speichern").performClick()
        rule.runOnIdle { assertEquals("Eine bewusst formulierte Test-Erkenntnis.", written) }
    }

    @Test fun cancelNeverPersistsAPrivateThought() {
        var calls = 0
        rule.setContent {
            EmotionskompassTheme {
                PersonalInsightsScreen(
                    state = PersonalInsightUiState(loaded = true,
                        items = listOf(PersonalInsight("test", 1000, 1000, "Ein fiktiver Gedanke"))),
                    journal = emptyList(), journalLoaded = true,
                    onReload = {}, onSave = { _, _, _ -> calls++ },
                    onArchive = { _, _ -> }, onDelete = {},
                    onOpenJournalEntry = {}
                )
            }
        }
        rule.onNode(hasScrollToIndexAction()).performScrollToNode(
            hasText("Eigene Erkenntnis schreiben"))
        rule.onNodeWithText("Eigene Erkenntnis schreiben").performClick()
        rule.onNodeWithTag("personal-insight-editor").performTextInput("Nur ein Entwurf.")
        rule.onNodeWithText("Abbrechen").performClick()
        rule.runOnIdle { assertEquals(0, calls) }
    }
}
