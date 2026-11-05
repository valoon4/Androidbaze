package com.valoon4.emotionskompass

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onFirst
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
import com.valoon4.emotionskompass.feature.insights.DiaryCalendarUiState
import com.valoon4.emotionskompass.feature.insights.PersonalReviewScreen
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    @Test fun allThreeReviewTabsStayAccessibleAtLargeFont() {
        rule.setContent {
            EmotionskompassTheme {
                PersonalReviewScreen(
                    snapshot = snapshot(), busy = false, error = null,
                    emotions = emotions, needs = needs,
                    onReload = {}, onOpenJournalEntry = {}, onStartCheckIn = {},
                    selectedTerm = null, onSelectTerm = {},
                    listState = rememberLazyListState(),
                    personalInsights = com.valoon4.emotionskompass.core.navigation.InsightUiState(
                        loaded = true
                    ),
                    strategyExperiments =
                        com.valoon4.emotionskompass.core.navigation.StrategyExperimentUiState(
                            loaded = true
                        )
                )
            }
        }
        rule.onNodeWithTag("review-tab-experiments").performClick()
        rule.onNodeWithTag("experiments-heading").assertExists()
        rule.onNodeWithTag("review-tab-insights").performClick()
        rule.onNodeWithTag("insights-heading").assertExists()
        rule.onNodeWithTag("review-tab-overview").performClick()
        rule.onNodeWithText("Dein Rückblick").assertExists()
    }

    @Test fun persistentTabsExplainPatternsAndRevealEveryEligibleCombination() {
        val now = System.currentTimeMillis()
        val journal = (0..11).map { day ->
            val at = now - day * 86_400_000L
            JournalEntry(
                "fictional-ux-pattern-$day", at, at,
                CheckInDraft(
                    emotions = mapOf("freude" to 4, "angst" to 5),
                    needs = setOf("klarheit"),
                    area = com.valoon4.emotionskompass.core.checkin.LifeArea.ALLTAG
                )
            )
        }
        rule.setContent {
            EmotionskompassTheme {
                PersonalReviewScreen(
                    snapshot = JournalSnapshot.fromEntries(journal),
                    busy = false, error = null, emotions = emotions, needs = needs,
                    onReload = {}, onOpenJournalEntry = {}, onStartCheckIn = {},
                    selectedTerm = null, onSelectTerm = {},
                    listState = rememberLazyListState(),
                    personalInsights = com.valoon4.emotionskompass.core.navigation.InsightUiState(
                        loaded = true
                    ),
                    diaryEntries = journal
                )
            }
        }
        rule.onNodeWithTag("review-tab-insights").performClick()
        rule.onNodeWithText("Deine Erkenntnisse").assertExists()
        rule.onNodeWithTag("review-tab-overview").performClick()
        rule.onNode(hasScrollToIndexAction()).performScrollToNode(
            hasTestTag("review-jump-patterns")
        )
        rule.onNodeWithTag("review-jump-patterns").performClick()
        rule.onNode(hasScrollToIndexAction()).performScrollToNode(
            hasTestTag("review-pattern-criteria")
        )
        rule.onNodeWithTag("review-pattern-criteria").performClick()
        rule.onNode(hasScrollToIndexAction()).performScrollToNode(
            hasText("mindestens 10 vergleichbaren Einträgen", substring = true)
        )
        rule.onNodeWithText("mindestens 10 vergleichbaren Einträgen",
            substring = true).assertExists()
        rule.onNode(hasScrollToIndexAction()).performScrollToNode(
            hasTestTag("review-show-all-patterns")
        )
        rule.onNodeWithTag("review-show-all-patterns").performClick()
        rule.onNode(hasScrollToIndexAction()).performScrollToNode(
            hasText("Weniger Kombinationen anzeigen")
        )
        rule.onNodeWithText("Weniger Kombinationen anzeigen").assertExists()
    }

    @Test fun countsAreNeutralAndEveryEmotionProvidesAnActualJournalEntry() {
        var opened: String? = null
        val source = snapshot()
        rule.setContent {
            EmotionskompassTheme {
                var selectedTerm by remember { mutableStateOf<Pair<String, String>?>(null) }
                PersonalReviewScreen(
                    snapshot = source, busy = false, error = null,
                    emotions = emotions, needs = needs, onReload = {},
                    onOpenJournalEntry = { opened = it }, onStartCheckIn = {},
                    selectedTerm = selectedTerm, onSelectTerm = { selectedTerm = it },
                    listState = rememberLazyListState()
                )
            }
        }
        rule.onNode(
            hasScrollToIndexAction() and
                SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
        ).performScrollToNode(hasText("2 gespeicherte Einträge"))
        rule.onNodeWithText("2 gespeicherte Einträge").assertExists()
        rule.onNodeWithText("3 Gefühlsnennungen").assertExists()
        rule.onNodeWithText("1 Bedürfnisnennung").assertExists()
        rule.onNode(
            hasScrollToIndexAction() and
                SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
        ).performScrollToNode(hasText("Freude", substring = true))
        rule.onNode(hasClickAction() and hasText("Freude", substring = true))
            .performClick()
        rule.onNodeWithText("Einträge mit „Freude“").assertExists()
        // LazyColumn virtualizes offscreen buttons, especially with 200% text.
        // Request the journal item first; only then click its visible action.
        rule.onNode(
            hasScrollToIndexAction() and
                SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
        ).performScrollToNode(hasText("Tagebucheintrag ansehen", substring = true))
        rule.onAllNodes(hasClickAction() and hasText("Tagebucheintrag ansehen", substring = true))
            .onFirst().performClick()
        rule.runOnIdle { assertTrue(opened == "beleg-a" || opened == "beleg-b") }
    }

    @Test fun optionalMentionBarsKeepTheSameCountsAndRealEvidenceLinks() {
        val source = snapshot() // Fictional: Freude twice, Angst once, Klarheit once
        var opened: String? = null
        rule.setContent {
            EmotionskompassTheme {
                var selected by remember { mutableStateOf<Pair<String, String>?>(null) }
                PersonalReviewScreen(
                    snapshot = source, busy = false, error = null,
                    emotions = emotions, needs = needs, onReload = {},
                    onOpenJournalEntry = { opened = it }, onStartCheckIn = {},
                    selectedTerm = selected, onSelectTerm = { selected = it },
                    listState = rememberLazyListState()
                )
            }
        }
        val scroller = hasScrollToIndexAction() and
            SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
        rule.onNode(scroller).performScrollToNode(
            hasTestTag("review-mention-bars-toggle")
        )
        rule.onNodeWithTag("review-mention-bars-toggle").performClick()
        rule.onNodeWithTag("review-mention-bars-explanation").assertExists()
        rule.onNode(scroller).performScrollToNode(
            hasTestTag("review-mention-emotion-freude")
        )
        rule.onNodeWithTag("review-mention-bar-emotion-freude", useUnmergedTree = true)
            .assertExists()
        rule.onNodeWithTag("review-mention-emotion-freude")
            .assertExists()
        rule.onNodeWithText("In 2 Einträgen genannt · Einträge ansehen")
            .assertExists()
        rule.onNodeWithTag("review-mention-emotion-freude").performClick()
        rule.onNodeWithText("Einträge mit „Freude“").assertExists()
        rule.onNode(scroller).performScrollToNode(
            hasText("Tagebucheintrag ansehen", substring = true)
        )
        rule.onAllNodes(hasClickAction() and hasText(
            "Tagebucheintrag ansehen", substring = true
        )).onFirst().performClick()
        rule.runOnIdle { assertTrue(opened == "beleg-a" || opened == "beleg-b") }
    }

    @Test fun mentionBarsCanBeHiddenWithoutHidingTheTermList() {
        rule.setContent {
            EmotionskompassTheme {
                PersonalReviewScreen(
                    snapshot = snapshot(), busy = false, error = null,
                    emotions = emotions, needs = needs, onReload = {},
                    onOpenJournalEntry = {}, onStartCheckIn = {},
                    selectedTerm = null, onSelectTerm = {},
                    listState = rememberLazyListState()
                )
            }
        }
        val scroller = hasScrollToIndexAction() and
            SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
        rule.onNode(scroller).performScrollToNode(
            hasTestTag("review-mention-bars-toggle")
        )
        rule.onNodeWithTag("review-mention-bars-toggle").performClick()
        rule.onNode(scroller).performScrollToNode(
            hasTestTag("review-mention-need-klarheit")
        )
        rule.onNodeWithTag("review-mention-bar-need-klarheit", useUnmergedTree = true)
            .assertExists()
        rule.onNode(scroller).performScrollToNode(
            hasTestTag("review-mention-bars-toggle")
        )
        rule.onNodeWithTag("review-mention-bars-toggle").performClick()
        rule.onNode(scroller).performScrollToNode(
            hasTestTag("review-mention-need-klarheit")
        )
        rule.onNodeWithTag("review-mention-bar-need-klarheit", useUnmergedTree = true)
            .assertDoesNotExist()
        rule.onNodeWithTag("review-mention-need-klarheit").assertExists()
        rule.onNodeWithTag("review-mention-need-klarheit")
            .assertTextContains("In 1 Eintrag genannt · Eintrag ansehen")
    }

    @Test fun noEntriesShowsHonestOptionalCheckInAction() {
        var started = false
        rule.setContent {
            EmotionskompassTheme {
                PersonalReviewScreen(
                    snapshot = JournalSnapshot.fromEntries(emptyList()),
                    busy = false, error = null, emotions = emotions, needs = needs,
                    onReload = {}, onOpenJournalEntry = {}, onStartCheckIn = { started = true },
                    selectedTerm = null, onSelectTerm = {}, listState = rememberLazyListState()
                )
            }
        }
        rule.onNode(
            hasScrollToIndexAction() and
                SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
        ).performScrollToNode(hasText("0 gespeicherte Einträge"))
        rule.onNodeWithText("0 gespeicherte Einträge").assertExists()
        rule.onNodeWithText("In diesem Zeitraum gibt es noch keine Einträge.").assertExists()
        rule.onNode(
            hasScrollToIndexAction() and
                SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
        ).performScrollToNode(hasTestTag("review-start-checkin"))
        rule.onNodeWithTag("review-start-checkin").performClick()
        rule.runOnIdle { assertTrue(started) }
    }

    @Test fun optionalPatternReflectionNeverCreatesAnInsightByItself() {
        val now = System.currentTimeMillis()
        val journal = (0..11).map { day ->
            val at = now - day * 86_400_000L
            JournalEntry(
                "fictional-pattern-$day", at, at,
                CheckInDraft(emotions = mapOf("freude" to 4), needs = setOf("klarheit"))
            )
        }
        var saveCalls = 0
        rule.setContent {
            EmotionskompassTheme {
                PersonalReviewScreen(
                    snapshot = JournalSnapshot.fromEntries(journal),
                    busy = false, error = null, emotions = emotions, needs = needs,
                    onReload = {}, onOpenJournalEntry = {}, onStartCheckIn = {},
                    selectedTerm = null, onSelectTerm = {},
                    listState = rememberLazyListState(),
                    personalInsights = com.valoon4.emotionskompass.core.navigation.InsightUiState(
                        loaded = true
                    ),
                    diaryEntries = journal,
                    onSaveInsight = { _, _, _ -> saveCalls++ }
                )
            }
        }
        rule.onNode(hasScrollToIndexAction() and
            SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
        ).performScrollToNode(hasText("Dazu freiwillig reflektieren"))
        rule.onNodeWithText("Dazu freiwillig reflektieren").performClick()
        rule.onNodeWithText("Ausgangspunkt:", substring = true).assertExists()
        rule.onNodeWithText("Ohne Speichern beenden").performClick()
        rule.runOnIdle { assertEquals(0, saveCalls) }
    }

    private fun calendarJournal(month: YearMonth): List<JournalEntry> {
        val zone = ZoneId.systemDefault()
        val day = month.atDay(4)
        fun at(hour: Int): Long =
            day.atTime(hour, 0).atZone(zone).toInstant().toEpochMilli()
        return listOf(
            JournalEntry("calendar-one", at(8), at(8), CheckInDraft()),
            JournalEntry("calendar-two", at(19), at(19), CheckInDraft()),
            JournalEntry("calendar-outside", day.minusMonths(1).atTime(12, 0)
                .atZone(zone).toInstant().toEpochMilli(),
                day.minusMonths(1).atTime(12, 0)
                    .atZone(zone).toInstant().toEpochMilli(), CheckInDraft())
        )
    }

    @Test fun optInCalendarFindsBothRealEntriesRegardlessOfStatisticFilter() {
        val month = YearMonth.now()
        val source = JournalSnapshot.fromEntries(calendarJournal(month))
        val navigation = DiaryCalendarUiState().apply { this.month = month }
        var opened: String? = null
        rule.setContent {
            EmotionskompassTheme {
                PersonalReviewScreen(
                    snapshot = source, busy = false, error = null,
                    emotions = emotions, needs = needs, onReload = {},
                    onOpenJournalEntry = { opened = it }, onStartCheckIn = {},
                    selectedTerm = null, onSelectTerm = {},
                    listState = rememberLazyListState(),
                    calendarUiState = navigation
                )
            }
        }
        val scroller = hasScrollToIndexAction() and
            SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
        rule.onNode(scroller).performScrollToNode(hasTestTag("diary-calendar-toggle"))
        rule.onNodeWithTag("diary-calendar-toggle").assertExists()
        rule.runOnIdle { assertFalse(navigation.expanded) }
        rule.onNodeWithTag("diary-calendar-toggle").performClick()
        rule.onNode(scroller).performScrollToNode(
            hasTestTag("diary-calendar-day-${month.atDay(4)}"))
        rule.onNodeWithTag("diary-calendar-day-${month.atDay(4)}")
            .assertHeightIsAtLeast(48.dp).performClick()
        rule.onNodeWithTag("diary-calendar-selected-count")
            .assertTextContains("2 gespeicherte Einträge")
        rule.onNode(scroller).performScrollToNode(
            hasTestTag("diary-calendar-entry-calendar-two"))
        rule.onNodeWithTag("diary-calendar-entry-calendar-two").performClick()
        rule.runOnIdle { assertEquals("calendar-two", opened) }
    }

    @Test fun calendarMonthNavigationAndEmptyDaysDoNotBecomeRatings() {
        val month = YearMonth.of(2026, 12)
        val navigation = DiaryCalendarUiState().apply { this.month = month }
        rule.setContent {
            EmotionskompassTheme {
                PersonalReviewScreen(
                    snapshot = JournalSnapshot.fromEntries(calendarJournal(month)),
                    busy = false, error = null, emotions = emotions, needs = needs,
                    onReload = {}, onOpenJournalEntry = {}, onStartCheckIn = {},
                    selectedTerm = null, onSelectTerm = {},
                    listState = rememberLazyListState(), calendarUiState = navigation
                )
            }
        }
        val scroller = hasScrollToIndexAction() and
            SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
        rule.onNode(scroller).performScrollToNode(hasTestTag("diary-calendar-toggle"))
        rule.onNodeWithTag("diary-calendar-toggle").performClick()
        rule.onNodeWithTag("diary-calendar-previous-month").performClick()
        rule.runOnIdle { assertEquals(YearMonth.of(2026, 11), navigation.month) }
        rule.onNodeWithTag("diary-calendar-next-month").performClick()
        rule.onNodeWithTag("diary-calendar-next-month").performClick()
        rule.runOnIdle { assertEquals(YearMonth.of(2027, 1), navigation.month) }
        rule.onNode(scroller).performScrollToNode(
            hasTestTag("diary-calendar-day-2027-01-02"))
        rule.onNodeWithTag("diary-calendar-day-2027-01-02").performClick()
        rule.onNodeWithTag("diary-calendar-selected-count")
            .assertTextContains("0 gespeicherte Einträge")
        rule.onNodeWithText("Daraus lässt sich nichts über dein Erleben ableiten.",
            substring = true).assertExists()
        rule.onNodeWithTag("diary-calendar-back-to-month").performClick()
        rule.runOnIdle { assertEquals(null, navigation.selectedDate) }
    }

    @Test fun calendarIsOnlyAvailableAfterAuthenticatedLoading() {
        val navigation = DiaryCalendarUiState().apply {
            expanded = true
            month = YearMonth.of(2026, 11)
            selectedDate = LocalDate.of(2026, 11, 4)
        }
        rule.setContent {
            EmotionskompassTheme {
                PersonalReviewScreen(
                    snapshot = null, busy = false, error = "Test: Journal-Schlüssel fehlt",
                    emotions = emotions, needs = needs, onReload = {},
                    onOpenJournalEntry = {}, onStartCheckIn = {},
                    selectedTerm = null, onSelectTerm = {},
                    listState = rememberLazyListState(), calendarUiState = navigation
                )
            }
        }
        rule.onNodeWithTag("diary-calendar-panel").assertDoesNotExist()
        rule.runOnIdle {
            assertFalse(navigation.expanded)
            assertEquals(null, navigation.selectedDate)
        }
        rule.onNodeWithText("Dein Tagebuch konnte nicht sicher geöffnet werden.")
            .assertExists()
    }

    @Test fun calendarSelectionCanSurviveAJournalDetailReturnInMemory() {
        val month = YearMonth.now()
        val nav = DiaryCalendarUiState().apply { this.month = month }
        val source = JournalSnapshot.fromEntries(calendarJournal(month))
        rule.setContent {
            EmotionskompassTheme {
                var detail by remember { mutableStateOf(false) }
                if (detail) {
                    Button(onClick = { detail = false }) { Text("Zurück vom Eintrag") }
                } else {
                    PersonalReviewScreen(
                        snapshot = source, busy = false, error = null,
                        emotions = emotions, needs = needs, onReload = {},
                        onOpenJournalEntry = { detail = true }, onStartCheckIn = {},
                        selectedTerm = null, onSelectTerm = {},
                        listState = rememberLazyListState(), calendarUiState = nav
                    )
                }
            }
        }
        val scroller = hasScrollToIndexAction() and
            SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
        rule.onNode(scroller).performScrollToNode(hasTestTag("diary-calendar-toggle"))
        rule.onNodeWithTag("diary-calendar-toggle").performClick()
        rule.onNode(scroller).performScrollToNode(
            hasTestTag("diary-calendar-day-${month.atDay(4)}"))
        rule.onNodeWithTag("diary-calendar-day-${month.atDay(4)}").performClick()
        rule.onNode(scroller).performScrollToNode(
            hasTestTag("diary-calendar-entry-calendar-one"))
        rule.onNodeWithTag("diary-calendar-entry-calendar-one").performClick()
        rule.onNodeWithText("Zurück vom Eintrag").performClick()
        rule.runOnIdle {
            assertTrue(nav.expanded)
            assertEquals(month.atDay(4), nav.selectedDate)
        }
        rule.onNode(scroller).performScrollToNode(
            hasTestTag("diary-calendar-selected-count"))
        rule.onNodeWithTag("diary-calendar-selected-count")
            .assertTextContains("2 gespeicherte Einträge")
    }

    @Test fun unavailableJournalNeverPretendsToBeEmptyAndCustomRangeCanBeChosen() {
        var reloaded = false
        rule.setContent {
            EmotionskompassTheme {
                PersonalReviewScreen(
                    snapshot = null, busy = false, error = "Beispiel: Schlüssel fehlt",
                    emotions = emotions, needs = needs,
                    onReload = { reloaded = true },
                    onOpenJournalEntry = {}, onStartCheckIn = {},
                    selectedTerm = null, onSelectTerm = {}, listState = rememberLazyListState()
                )
            }
        }
        rule.onNodeWithText("0 gespeicherte Einträge").assertDoesNotExist()
        // Error card can be below a four-row, 200%-scale date filter.
        rule.onNode(
            hasScrollToIndexAction() and
                SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
        ).performScrollToNode(hasText("Dein Tagebuch konnte nicht sicher geöffnet werden."))
        rule.onNodeWithText("Dein Tagebuch konnte nicht sicher geöffnet werden.").assertExists()
        rule.onNode(
            hasScrollToIndexAction() and
                SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
        ).performScrollToNode(hasTestTag("review-retry"))
        rule.onNodeWithTag("review-retry").performClick()
        rule.runOnIdle { assertTrue(reloaded) }
        rule.onNode(
            hasScrollToIndexAction() and
                SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
        ).performScrollToNode(hasText("Eigener Zeitraum"))
        rule.onNodeWithText("Eigener Zeitraum").performClick()
        rule.onNodeWithText("Von:", substring = true).assertExists()
        rule.onNodeWithText("Bis:", substring = true).assertExists()
    }
}
