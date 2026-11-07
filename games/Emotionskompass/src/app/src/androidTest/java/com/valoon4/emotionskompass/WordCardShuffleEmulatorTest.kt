package com.valoon4.emotionskompass

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.valoon4.emotionskompass.core.designsystem.EmotionskompassTheme
import com.valoon4.emotionskompass.core.model.EmotionCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Real touch/UI checks at normal and 200% font size; all cards are public. */
@RunWith(AndroidJUnit4::class)
class WordCardShuffleEmulatorTest {
    @get:Rule val rule = createComposeRule()

    private val catalog by lazy {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        EmotionCatalog.fromJson(context.assets.open("catalog/emotions.de.v2.json")
            .bufferedReader().use { it.readText() })
    }

    private fun revealName(): String {
        rule.onNodeWithText("Begriff aufdecken").performScrollTo().performClick()
        return rule.onNodeWithTag("word-card-revealed-term")
            .fetchSemanticsNode().config[SemanticsProperties.Text].first().text
    }

    @Test fun nextBackAndReshuffleNeverRepeatImmediately() {
        rule.setContent { EmotionskompassTheme { LearningCardsScreen(catalog, {}) } }
        val first = revealName()
        rule.onNodeWithText("Nächste Karte").performScrollTo().performClick()
        val second = revealName()
        assertNotEquals(first, second)

        rule.onNodeWithText("Zurück").performScrollTo().performClick()
        assertEquals(first, revealName())

        rule.onNodeWithTag("word-cards-reshuffle").performScrollTo().performClick()
        rule.onNodeWithText("KARTE 1 VON 289").assertExists()
        assertNotEquals(first, revealName())
    }

    @Test fun selectingAnotherFamilyCreatesAFilteredFreshRound() {
        rule.setContent { EmotionskompassTheme { LearningCardsScreen(catalog, {}) } }
        rule.onNodeWithText("Nächste Karte").performScrollTo().performClick()
        val family = catalog.familyNames.first()
        val filtered = catalog.search("", family)
        // A family chip can lie outside the viewport at 320dp and 200% text.
        // Assert its selection and the actual revealed catalog term, not
        // a counter label whose visibility depends on scrolling/text scale.
        val chip = rule.onNodeWithTag("word-card-family-$family")
        chip.performScrollTo().performClick()
        chip.assertIsSelected()
        rule.waitForIdle()
        val shown = revealName()
        assertTrue(filtered.any { it.name == shown })
    }

    @Test fun savedNavigationHolderRetainsShuffledRoundAndPosition() {
        rule.setContent {
            EmotionskompassTheme {
                var showCards by remember { mutableStateOf(true) }
                val holder = rememberSaveableStateHolder()
                Column {
                    Button(onClick = { showCards = !showCards }) { Text("Ansicht wechseln") }
                    if (showCards) {
                        holder.SaveableStateProvider("word-cards") {
                            LearningCardsScreen(catalog, {})
                        }
                    } else {
                        Text("Andere Ansicht")
                    }
                }
            }
        }
        rule.onNodeWithText("Nächste Karte").performScrollTo().performClick()
        val second = revealName()
        rule.onNodeWithText("Ansicht wechseln").performClick()
        rule.onNodeWithText("Andere Ansicht").assertExists()
        rule.onNodeWithText("Ansicht wechseln").performClick()
        rule.onNodeWithText("KARTE 2 VON 289").assertExists()
        rule.onNodeWithTag("word-card-revealed-term").assertExists()
        val restored = rule.onNodeWithTag("word-card-revealed-term")
            .fetchSemanticsNode().config[SemanticsProperties.Text].first().text
        assertEquals(second, restored)
    }
}
