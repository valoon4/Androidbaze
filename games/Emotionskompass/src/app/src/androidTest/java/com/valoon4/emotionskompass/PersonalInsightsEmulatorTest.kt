package com.valoon4.emotionskompass

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.valoon4.emotionskompass.core.checkin.CheckInDraft
import com.valoon4.emotionskompass.core.journal.JournalEntry
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
        rule.onNodeWithTag("insight-editor-list")
            .performScrollToNode(hasTestTag("insight-editor"))
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

    @Test fun insightCanInviteAnIdeaWithoutSavingOrCopyingIt() {
        val note = PersonalInsight.create(
            "Fiktiver persönlicher Gedanke, der nicht kopiert werden darf",
            System.currentTimeMillis()
        )
        var chosen: String? = null
        var saved = 0
        rule.setContent {
            EmotionskompassTheme {
                PersonalInsightsScreen(
                    state = InsightUiState(entries = listOf(note), loaded = true),
                    journalEntries = emptyList(), onBack = {}, onReload = {},
                    onSave = { _, _, _ -> saved++ },
                    onArchive = { _, _ -> }, onDelete = {},
                    onUnlink = { _, _ -> }, onOpenJournal = {},
                    onConsiderIdeaFromInsight = { chosen = it }
                )
            }
        }
        rule.onNode(hasScrollToIndexAction()).performScrollToNode(
            hasTestTag("insight-consider-idea-${note.id}")
        )
        rule.onNodeWithTag("insight-consider-idea-${note.id}").performClick()
        rule.runOnIdle {
            org.junit.Assert.assertEquals(note.id, chosen)
            org.junit.Assert.assertEquals(0, saved)
        }
    }

    @Test fun editorExposesOlderJournalReferencesWithoutGuessingDates() {
        val now = System.currentTimeMillis()
        val journal = (0..11).map { day ->
            val at = now - day * 86_400_000L
            JournalEntry("fictional-ux-entry-$day", at, at, CheckInDraft())
        }
        rule.setContent {
            EmotionskompassTheme {
                PersonalInsightsScreen(
                    state = InsightUiState(loaded = true), journalEntries = journal,
                    onBack = {}, onReload = {}, onSave = { _, _, _ -> },
                    onArchive = { _, _ -> }, onDelete = {},
                    onUnlink = { _, _ -> }, onOpenJournal = {}
                )
            }
        }
        rule.onNode(hasScrollToIndexAction()).performScrollToNode(
            hasTestTag("insight-create")
        )
        rule.onNodeWithTag("insight-create").performClick()
        rule.onNodeWithTag("insight-editor-list").performScrollToNode(
            hasTestTag("insight-journal-expand")
        )
        rule.onNodeWithTag("insight-journal-expand").performClick()
        rule.onNodeWithTag("insight-editor-list").performScrollToNode(
            hasTestTag("insight-journal-date-search")
        )
        rule.onNodeWithTag("insight-journal-date-search").assertExists()
        rule.onNodeWithText("Abbrechen").performClick()
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
