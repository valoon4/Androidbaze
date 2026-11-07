package com.valoon4.emotionskompass

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.valoon4.emotionskompass.core.designsystem.EmotionskompassTheme
import com.valoon4.emotionskompass.core.learning.LearningEngine
import com.valoon4.emotionskompass.core.model.EmotionCatalog
import com.valoon4.emotionskompass.core.model.NeedCatalog
import com.valoon4.emotionskompass.feature.needs.NeedsScreen
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Independent interaction regression: tapping the need of the day opens
 * precisely the editorial need with that ID. No private check-in is touched.
 * Executed at both default and 320dp/200% emulator settings.
 */
@RunWith(AndroidJUnit4::class)
class DailyNeedNavigationEmulatorTest {
    @get:Rule val rule = createComposeRule()

    private val catalog by lazy {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        NeedCatalog.fromJson(context.assets.open("catalog/needs.de.v1.json")
            .bufferedReader().use { it.readText() })
    }
    private val emotions by lazy {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        EmotionCatalog.fromJson(context.assets.open("catalog/emotions.de.v2.json")
            .bufferedReader().use { it.readText() })
    }

    @Test fun dailyNeedCardOpensItsSpecificDetailAndReturnsToHome() {
        val daily = LearningEngine.daily(emotions, catalog, LocalDate.of(2026, 11, 7))
        var requested = ""
        var returned = false
        rule.setContent {
            EmotionskompassTheme {
                var showNeeds by remember { mutableStateOf(false) }
                var fromHomeDaily by remember { mutableStateOf(false) }
                var requestedId by remember { mutableStateOf<String?>(null) }
                if (showNeeds) {
                    NeedsScreen(
                        catalog = catalog,
                        requestedNeedId = requestedId,
                        onRequestConsumed = { requestedId = null },
                        onReturnToHome = if (fromHomeDaily) {
                            { returned = true; showNeeds = false }
                        } else null
                    )
                } else {
                    HomeScreen(
                        termsCount = emotions.emotions.size,
                        needsCount = catalog.needs.size,
                        strategyCount = 0,
                        onExplore = {},
                        onNeeds = {
                            fromHomeDaily = false
                            showNeeds = true
                        },
                        onDailyNeed = { id ->
                            requested = id
                            requestedId = id
                            fromHomeDaily = true
                            showNeeds = true
                        },
                        onStrategies = {}, onCheckin = {}, onWord = {}, onKnowledge = {},
                        daily = daily
                    )
                }
            }
        }
        rule.onNodeWithTag("daily-need-card").performScrollTo().performClick()
        rule.waitForIdle()
        rule.onNodeWithText(daily.need.name).assertExists()
        rule.onNodeWithText(catalog.find(daily.need.id)!!.definition).assertExists()
        rule.onNodeWithText("← Zur Startseite").performClick()
        rule.onNodeWithTag("daily-need-card").assertExists()
        rule.runOnIdle {
            assertEquals(daily.need.id, requested)
            assertEquals(true, returned)
        }
    }

    @Test fun generalNeedsShortcutStillShowsTheAtlasRatherThanOneDailyNeed() {
        val daily = LearningEngine.daily(emotions, catalog, LocalDate.of(2026, 11, 7))
        rule.setContent {
            EmotionskompassTheme {
                var atlasOpen by remember { mutableStateOf(false) }
                if (atlasOpen) {
                    NeedsScreen(catalog = catalog)
                } else {
                    HomeScreen(
                        termsCount = emotions.emotions.size,
                        needsCount = catalog.needs.size,
                        strategyCount = 0,
                        onExplore = {},
                        onNeeds = { atlasOpen = true },
                        onDailyNeed = {},
                        onStrategies = {}, onCheckin = {}, onWord = {}, onKnowledge = {},
                        daily = daily
                    )
                }
            }
        }
        rule.onNodeWithText("Bedürfnisse").performClick()
        rule.onNodeWithText("Bedürfnisse verstehen").assertExists()
        rule.onNodeWithText("Atlas").assertExists()
        rule.onNodeWithText("← Zur Startseite").assertDoesNotExist()
    }
}
