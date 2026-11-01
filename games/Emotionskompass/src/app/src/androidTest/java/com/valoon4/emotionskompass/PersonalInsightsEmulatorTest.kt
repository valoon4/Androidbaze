package com.valoon4.emotionskompass

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.valoon4.emotionskompass.core.designsystem.EmotionskompassTheme
import com.valoon4.emotionskompass.core.insights.PersonalInsight
import com.valoon4.emotionskompass.core.navigation.InsightUiState
import com.valoon4.emotionskompass.feature.insights.PersonalInsightsScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class PersonalInsightsEmulatorTest {
    @get:Rule val rule = createComposeRule()

    @Test fun explicitlyTypedInsightCanBeCreatedAndArchived() {
        rule.setContent {
            EmotionskompassTheme {
                var ui by remember { mutableStateOf(InsightUiState(loaded = true)) }
                PersonalInsightsScreen(
                    state = ui, journalEntries = emptyList(), onBack = {}, onReload = {},
                    onSave = { text, _, _ ->
                        val created = PersonalInsight.create(text, System.currentTimeMillis())
                        ui = InsightUiState(entries = listOf(created), loaded = true,
                            message = "Erkenntnis gespeichert.")
                    },
                    onArchive = { id, archived ->
                        ui = InsightUiState(
                            entries = ui.entries.map {
                                if (it.id == id) it.copy(archived = archived) else it
                            }, loaded = true, message = "Erkenntnis archiviert."
                        )
                    },
                    onDelete = { id ->
                        ui = InsightUiState(entries = ui.entries.filterNot { it.id == id },
                            loaded = true)
                    },
                    onUnlink = { _, _ -> }, onOpenJournal = {}
                )
            }
        }
        rule.onNode(hasScrollToIndexAction()).performScrollToNode(
            hasTestTag("insight-create")
        )
        rule.onNodeWithTag("insight-create").performClick()
        rule.onNodeWithTag("insight-editor").performTextInput(
            "Ich habe selbst bemerkt, dass mir verständliche Absprachen helfen."
        )
        rule.onNodeWithText("Ausdrücklich speichern").performClick()
        rule.waitForIdle()
        rule.onNode(hasScrollToIndexAction()).performScrollToNode(
            hasText("Ich habe selbst bemerkt, dass mir verständliche Absprachen helfen.")
        )
        rule.onNodeWithText(
            "Ich habe selbst bemerkt, dass mir verständliche Absprachen helfen."
        ).assertExists()
        rule.onNode(hasScrollToIndexAction()).performScrollToNode(
            hasText("Archivieren")
        )
        rule.onNodeWithText("Archivieren").performClick()
        rule.onNode(hasScrollToIndexAction()).performScrollToNode(
            hasText("Archiv")
        )
        rule.onNodeWithText("Archiv").performClick()
        rule.onNode(hasScrollToIndexAction()).performScrollToNode(
            hasText("Ich habe selbst bemerkt, dass mir verständliche Absprachen helfen.")
        )
        rule.onNodeWithText(
            "Ich habe selbst bemerkt, dass mir verständliche Absprachen helfen."
        ).assertExists()
    }

    @Test fun unavailableInsightsNeverDisplayDecryptedTextOrCreationAction() {
        val privateExample = PersonalInsight(
            UUID.randomUUID().toString(), 1000, 1000,
            "Dieser fiktive private Satz darf im Fehlerfall nicht angezeigt werden."
        )
        rule.setContent {
            EmotionskompassTheme {
                PersonalInsightsScreen(
                    state = InsightUiState(entries = listOf(privateExample),
                        error = "Sicheres Laden fehlgeschlagen."),
                    journalEntries = emptyList(), onBack = {}, onReload = {},
                    onSave = { _, _, _ -> }, onArchive = { _, _ -> },
                    onDelete = {}, onUnlink = { _, _ -> }, onOpenJournal = {}
                )
            }
        }
        rule.onNodeWithText(privateExample.text).assertDoesNotExist()
        rule.onNodeWithTag("insight-create").assertDoesNotExist()
        rule.onNodeWithText("Erneut sicher laden").assertExists()
    }
}
