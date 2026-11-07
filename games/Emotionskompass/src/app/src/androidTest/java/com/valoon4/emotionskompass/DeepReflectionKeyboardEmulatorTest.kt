package com.valoon4.emotionskompass

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.valoon4.emotionskompass.core.checkin.CheckInDraft
import com.valoon4.emotionskompass.core.designsystem.EmotionskompassTheme
import com.valoon4.emotionskompass.core.model.NeedCatalog
import com.valoon4.emotionskompass.feature.checkin.DeepReflectionStep
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Real keyboard/focus smoke test at 100% and 200% font with a 320dp device. */
@RunWith(AndroidJUnit4::class)
class DeepReflectionKeyboardEmulatorTest {
    @get:Rule val rule = createComposeRule()

    private val needs by lazy {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        NeedCatalog.fromJson(context.assets.open("catalog/needs.de.v1.json")
            .bufferedReader().use { it.readText() })
    }

    @Test fun reflectionFieldsRemainReachableAndTypedTextSurvivesImeSwitch() {
        var current = CheckInDraft()
        rule.setContent {
            EmotionskompassTheme {
                var draft by remember { mutableStateOf(CheckInDraft()) }
                current = draft
                Column(Modifier.fillMaxSize().imePadding()) {
                    LazyColumn(Modifier.weight(1f).testTag("reflection-viewport")) {
                        item {
                            DeepReflectionStep(
                                draft = draft,
                                needCatalog = needs,
                                onSituation = { draft = draft.withSituation(it) },
                                onInterpretation = { draft = draft.withInterpretation(it) },
                                onNeed = { draft = draft.toggleNeed(it) },
                                onInsight = { draft = draft.withInsight(it) },
                                onNextStep = { draft = draft.withNextStepIdea(it) },
                                onNextStepNote = { draft = draft.withNextStepNote(it) }
                            )
                        }
                    }
                }
            }
        }
        rule.onNodeWithTag("reflection-situation").performScrollTo().performClick()
        rule.onNodeWithTag("reflection-situation").performTextInput("Sicher sichtbar.")
        rule.waitForIdle()
        rule.onNodeWithTag("reflection-situation").assertIsDisplayed()

        rule.onNodeWithTag("reflection-insight").performScrollTo().performClick()
        rule.onNodeWithTag("reflection-insight").performTextInput("Meine Erkenntnis bleibt.")
        rule.waitForIdle()
        rule.onNodeWithTag("reflection-insight").assertIsDisplayed()
            .assertTextContains("Meine Erkenntnis bleibt.", substring = true)

        rule.onNodeWithTag("reflection-next-step").performScrollTo().performClick()
        rule.onNodeWithTag("reflection-next-step").performTextInput("Mein nächster Schritt.")
        rule.waitForIdle()
        rule.onNodeWithTag("reflection-next-step").assertIsDisplayed()
            .assertTextContains("Mein nächster Schritt.", substring = true)
        rule.runOnIdle {
            assertEquals("Sicher sichtbar.", current.situation)
            assertEquals("Meine Erkenntnis bleibt.", current.insight)
            assertEquals("Mein nächster Schritt.", current.nextStepNote)
        }
    }
}
