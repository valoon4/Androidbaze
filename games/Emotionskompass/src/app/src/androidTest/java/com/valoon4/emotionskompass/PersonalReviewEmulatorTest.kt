package com.valoon4.emotionskompass

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.valoon4.emotionskompass.core.checkin.CheckInDraft
import com.valoon4.emotionskompass.core.designsystem.EmotionskompassTheme
import com.valoon4.emotionskompass.core.insights.JournalSnapshot
import com.valoon4.emotionskompass.core.journal.JournalEntry
import com.valoon4.emotionskompass.core.model.EmotionCatalog
import com.valoon4.emotionskompass.core.model.NeedCatalog
import com.valoon4.emotionskompass.feature.insights.PersonalReviewScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Runs on real Android emulators at standard and 200% font scale with 320dp
 * viewport. All records are fictional and constructed only inside the test.
 */
@RunWith(AndroidJUnit4::class)
class PersonalReviewEmulatorTest {
    @get:Rule val rule = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    private val emotions by lazy {
        EmotionCatalog.fromJson(context.assets.open("catalog/emotions.de.v2.json")
            .bufferedReader().use { it.readText() })
    }
    private val needs by lazy {
        NeedCatalog.fromJson(context.assets.open("catalog/needs.de.v1.json")
            .bufferedReader().use { it.readText() })
    }

    private fun snapshot(): JournalSnapshot {
        val now = System.currentTimeMillis()
        return JournalSnapshot.fromEntries(listOf(
            JournalEntry("beleg-a", now - 120_000L, now - 120_000L,
                CheckInDraft(emotions = mapOf("freude" to 0), needs = setOf("klarheit"))),
            JournalEntry("beleg-b", now - 60_000L, now - 60_000L,
                CheckInDraft(emotions = mapOf("freude" to null, "angst" to 5),
                    needs = setOf(CheckInDraft.UNKNOWN_NEED_ID)))
        ))
    }

    @Test fun countsAreNeutralAndEveryEmotionProvidesAnActualJournalEntry() {
        var opened: String? = null
        val source = snapshot()
        rule.setContent {
            EmotionskompassTheme {
                PersonalReviewScreen(
                    snapshot = source, busy = false, error = null,
                    emotions = emotions, needs = needs, onReload = {},
                    onOpenJournalEntry = { opened = it }, onStartCheckIn = {}
                )
            }
        }
        rule.onNode(
            hasScrollToIndexAction() and
                SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
        ).performScrollToNode(hasText("2 gespeicherte Einträge"))
        rule.onNodeWithText("2 gespeicherte Einträge").assertExists()
        rule.onNodeWithText("3 Gefühlsnennungen").assertExists()
        rule.onNodeWithText("1 Bedürfnisnennungen").assertExists()
        rule.onNode(
            hasScrollToIndexAction() and
                SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
        ).performScrollToNode(hasText("Freude", substring = true))
        rule.onNode(hasClickAction() and hasText("Freude", substring = true))
            .performClick()
        rule.onNodeWithText("Einträge mit „Freude“").assertExists()
        rule.onNode(hasClickAction() and hasText("Tagebucheintrag ansehen", substring = true))
            .performScrollTo().performClick()
        rule.runOnIdle { assertTrue(opened == "beleg-a" || opened == "beleg-b") }
    }

    @Test fun noEntriesShowsHonestOptionalCheckInAction() {
        var started = false
        rule.setContent {
            EmotionskompassTheme {
                PersonalReviewScreen(
                    snapshot = JournalSnapshot.fromEntries(emptyList()),
                    busy = false, error = null, emotions = emotions, needs = needs,
                    onReload = {}, onOpenJournalEntry = {}, onStartCheckIn = { started = true }
                )
            }
        }
        rule.onNode(
            hasScrollToIndexAction() and
                SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
        ).performScrollToNode(hasText("0 gespeicherte Einträge"))
        rule.onNodeWithText("0 gespeicherte Einträge").assertExists()
        rule.onNodeWithText("In diesem Zeitraum gibt es noch keine Einträge.").assertExists()
        rule.onNodeWithText("Freiwilligen Check-in beginnen")
            .performScrollTo().performClick()
        rule.runOnIdle { assertTrue(started) }
    }

    @Test fun unavailableJournalNeverPretendsToBeEmptyAndCustomRangeCanBeChosen() {
        var reloaded = false
        rule.setContent {
            EmotionskompassTheme {
                PersonalReviewScreen(
                    snapshot = null, busy = false, error = "Beispiel: Schlüssel fehlt",
                    emotions = emotions, needs = needs,
                    onReload = { reloaded = true },
                    onOpenJournalEntry = {}, onStartCheckIn = {}
                )
            }
        }
        rule.onNodeWithText("0 gespeicherte Einträge").assertDoesNotExist()
        rule.onNodeWithText("Dein Tagebuch konnte nicht sicher geöffnet werden.").assertExists()
        rule.onNodeWithText("Erneut versuchen").performScrollTo().performClick()
        rule.runOnIdle { assertTrue(reloaded) }
        rule.onNode(
            hasScrollToIndexAction() and
                SemanticsMatcher.keyIsDefined(SemanticsProperties.HorizontalScrollAxisRange)
        ).performScrollToNode(hasText("Eigener Zeitraum"))
        rule.onNodeWithText("Eigener Zeitraum").performClick()
        rule.onNodeWithText("Von:", substring = true).assertExists()
        rule.onNodeWithText("Bis:", substring = true).assertExists()
    }
}
