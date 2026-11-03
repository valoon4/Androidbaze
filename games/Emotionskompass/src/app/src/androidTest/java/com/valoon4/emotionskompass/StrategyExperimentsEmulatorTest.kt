package com.valoon4.emotionskompass

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.valoon4.emotionskompass.core.insights.PersonalInsight
import com.valoon4.emotionskompass.core.strategies.StrategyCatalog
import com.valoon4.emotionskompass.core.designsystem.EmotionskompassTheme
import com.valoon4.emotionskompass.core.insights.ExperimentEvaluation
import com.valoon4.emotionskompass.core.insights.ExperimentStatus
import com.valoon4.emotionskompass.core.insights.StrategyExperiment
import com.valoon4.emotionskompass.core.navigation.StrategyExperimentUiState
import com.valoon4.emotionskompass.feature.insights.StrategyExperimentsScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StrategyExperimentsEmulatorTest {
    @get:Rule val rule = createComposeRule()

    private val catalog by lazy {
        val assets = InstrumentationRegistry.getInstrumentation().targetContext.assets
        fun read(file: String) = assets.open(file).bufferedReader().use { it.readText() }
        StrategyCatalog.fromJsonParts(read("catalog/strategies.de.v1.json"),
            listOf("situation", "regulation-a", "regulation-b", "cognition-a",
                "cognition-b", "social", "values", "recovery", "patterns")
                .map { read("catalog/strategies.$it.de.v1.json") })
    }

    @Test fun catalogProposalIsAnUnsavedDraftUntilExplicitConfirmation() {
        var saved: StrategyExperiment? = null
        val selected = requireNotNull(catalog.find("planning"))
        rule.setContent {
            EmotionskompassTheme {
                StrategyExperimentsScreen(
                    state = StrategyExperimentUiState(loaded = true),
                    onReload = {}, onSave = { saved = it },
                    onArchive = { _, _ -> }, onDelete = {},
                    catalog = catalog, incomingCatalog = selected
                )
            }
        }
        rule.onNodeWithTag("experiment-editor-list").performScrollToNode(
            hasTestTag("experiment-title")
        )
        rule.runOnIdle { assertNull(saved) }
        rule.onNodeWithTag("experiment-explicit-save").performClick()
        rule.runOnIdle {
            assertEquals("planning", saved?.sourceStrategyId)
            assertEquals(selected.title, saved?.sourceTitleFallback)
            assertTrue(saved?.relatedInsightIds.orEmpty().isEmpty())
        }
    }

    @Test fun insightReferenceIsNotAddedUntilItIsExplicitlySelected() {
        val insight = PersonalInsight.create(
            "Nur ein fiktiver eigener Erkenntnistext", System.currentTimeMillis()
        )
        var saved: StrategyExperiment? = null
        rule.setContent {
            EmotionskompassTheme {
                StrategyExperimentsScreen(
                    state = StrategyExperimentUiState(loaded = true),
                    onReload = {}, onSave = { saved = it },
                    onArchive = { _, _ -> }, onDelete = {},
                    insights = listOf(insight), incomingInsightId = insight.id
                )
            }
        }
        rule.onNodeWithTag("experiment-editor-list").performScrollToNode(
            hasTestTag("experiment-title")
        )
        rule.onNodeWithTag("experiment-title").performTextInput("Eine eigene Idee")
        rule.onNodeWithTag("experiment-editor-list").performScrollToNode(
            hasTestTag("experiment-insight-link-${insight.id}")
        )
        rule.onNodeWithTag("experiment-insight-link-${insight.id}").performClick()
        rule.onNodeWithTag("experiment-explicit-save").performClick()
        rule.runOnIdle {
            assertEquals(setOf(insight.id), saved?.relatedInsightIds)
            assertEquals("Eine eigene Idee", saved?.customTitle)
            assertTrue(saved?.planText.orEmpty().isEmpty())
        }
    }

    @Test fun absentCatalogCardRetainsUsersSavedIdeaWithoutGuessingReplacement() {
        val item = StrategyExperiment.fromCatalog(
            "old-not-present", "Frühere Beispielkarte", System.currentTimeMillis()
        )
        rule.setContent {
            EmotionskompassTheme {
                StrategyExperimentsScreen(
                    state = StrategyExperimentUiState(entries = listOf(item), loaded = true),
                    onReload = {}, onSave = {},
                    onArchive = { _, _ -> }, onDelete = {},
                    catalog = catalog,
                    onOpenStrategy = { throw AssertionError("must not guess catalog ID") }
                )
            }
        }
        rule.onNodeWithTag("experiments-list").performScrollToNode(
            hasTestTag("experiment-missing-catalog")
        )
        rule.onNodeWithTag("experiment-missing-catalog").assertExists()
        rule.onNodeWithTag("experiment-open-strategy").assertDoesNotExist()
    }

    @Test fun cancellingTypedIdeaNeverCallsSaveOrCreatesAnEntry() {
        var saves = 0
        rule.setContent {
            EmotionskompassTheme {
                StrategyExperimentsScreen(
                    state = StrategyExperimentUiState(loaded = true),
                    onReload = {}, onSave = { saves++ },
                    onArchive = { _, _ -> }, onDelete = {}
                )
            }
        }
        rule.onNodeWithTag("experiment-create").performClick()
        rule.onNodeWithTag("experiment-editor-list").performScrollToNode(
            hasTestTag("experiment-title"))
        rule.onNodeWithTag("experiment-title").performTextInput(
            "Eine Frage stellen, falls etwas unklar ist")
        rule.onNodeWithTag("experiment-cancel").performClick()
        rule.onNodeWithText("Entwurf verwerfen").performClick()
        rule.runOnIdle { assertEquals(0, saves) }
        rule.onNodeWithTag("experiments-list").performScrollToNode(
            hasText("Hier hast du noch keine Idee gespeichert."))
        rule.onNodeWithText("Hier hast du noch keine Idee gespeichert.").assertExists()
        rule.onNodeWithText("Eine Frage stellen, falls etwas unklar ist").assertDoesNotExist()
    }

    @Test fun statusNotTriedAndNoEvaluationAreValidThenCanReflectWithoutScoring() {
        var saved: StrategyExperiment? = null
        rule.setContent {
            EmotionskompassTheme {
                var ui by remember { mutableStateOf(StrategyExperimentUiState(loaded = true)) }
                StrategyExperimentsScreen(
                    state = ui, onReload = {},
                    onSave = { item ->
                        saved = item
                        ui = StrategyExperimentUiState(
                            entries = listOf(item), loaded = true,
                            message = if (ui.entries.isEmpty()) "Idee gespeichert."
                                else "Idee aktualisiert."
                        )
                    },
                    onArchive = { _, _ -> }, onDelete = {}
                )
            }
        }
        rule.onNodeWithTag("experiment-create").performClick()
        rule.onNodeWithTag("experiment-editor-list").performScrollToNode(
            hasTestTag("experiment-title"))
        rule.onNodeWithTag("experiment-title").performTextInput("Fiktiv: Pause überlegen")
        rule.onNodeWithTag("experiment-editor-list").performScrollToNode(
            hasTestTag("experiment-status-NOT_TRIED"))
        rule.onNodeWithTag("experiment-status-NOT_TRIED").performClick()
        rule.onNodeWithTag("experiment-explicit-save").performClick()
        rule.runOnIdle {
            assertEquals(ExperimentStatus.NOT_TRIED, saved?.status)
            assertNull(saved?.evaluation)
        }
        val id = requireNotNull(saved).id
        rule.onNodeWithTag("experiments-list").performScrollToNode(
            hasTestTag("experiment-edit-$id"))
        rule.onNodeWithTag("experiment-edit-$id").performClick()
        rule.onNodeWithTag("experiment-editor-list").performScrollToNode(
            hasTestTag("experiment-status-TRIED"))
        rule.onNodeWithTag("experiment-status-TRIED").performClick()
        rule.onNodeWithTag("experiment-editor-list").performScrollToNode(
            hasTestTag("experiment-evaluation-UNCLEAR"))
        rule.onNodeWithTag("experiment-evaluation-UNCLEAR").performClick()
        rule.onNodeWithTag("experiment-editor-list").performScrollToNode(
            hasTestTag("experiment-reflection"))
        rule.onNodeWithTag("experiment-reflection").performTextInput(
            "Ich weiß noch nicht, wie es für mich war.")
        rule.onNodeWithTag("experiment-explicit-save").performClick()
        rule.runOnIdle {
            assertEquals(ExperimentStatus.TRIED, saved?.status)
            assertEquals(ExperimentEvaluation.UNCLEAR, saved?.evaluation)
            assertEquals("Ich weiß noch nicht, wie es für mich war.", saved?.reflectionText)
        }
    }

    @Test fun archiveRestoreAndConfirmedDeleteNeverModifyTheJournal() {
        val created = StrategyExperiment.fromOwnIdea(
            "Fiktive selbstbestimmte Idee", System.currentTimeMillis()
        )
        var deletedId: String? = null
        var archiveChanges = 0
        rule.setContent {
            EmotionskompassTheme {
                var ui by remember {
                    mutableStateOf(StrategyExperimentUiState(
                        entries = listOf(created), loaded = true))
                }
                StrategyExperimentsScreen(
                    state = ui, onReload = {}, onSave = {},
                    onArchive = { id, archived ->
                        archiveChanges++
                        ui = StrategyExperimentUiState(
                            entries = ui.entries.map {
                                if (it.id == id) it.copy(archived = archived) else it
                            }, loaded = true, message = "Idee archiviert."
                        )
                    },
                    onDelete = { id ->
                        deletedId = id
                        ui = StrategyExperimentUiState(
                            entries = ui.entries.filterNot { it.id == id },
                            loaded = true, message = "Idee gelöscht."
                        )
                    }
                )
            }
        }
        rule.onNodeWithTag("experiments-list").performScrollToNode(
            hasText("Archivieren"))
        rule.onNodeWithText("Archivieren").performClick()
        rule.onNodeWithTag("experiments-list").performScrollToNode(
            hasText("Archiv"))
        rule.onNodeWithText("Archiv").performClick()
        rule.onNodeWithTag("experiments-list").performScrollToNode(
            hasText("Aus Archiv zurückholen"))
        rule.onNodeWithText("Aus Archiv zurückholen").performClick()
        rule.onNodeWithTag("experiments-list").performScrollToNode(
            hasText("Aktuelle"))
        rule.onNodeWithText("Aktuelle").performClick()
        rule.onNodeWithTag("experiments-list").performScrollToNode(
            hasTestTag("experiment-delete-${created.id}"))
        rule.onNodeWithTag("experiment-delete-${created.id}").performClick()
        rule.onNodeWithTag("experiment-confirm-delete").performClick()
        rule.runOnIdle {
            assertEquals(2, archiveChanges)
            assertEquals(created.id, deletedId)
        }
        rule.onNodeWithTag("experiments-list").performScrollToNode(
            hasText("Hier hast du noch keine Idee gespeichert."))
        rule.onNodeWithText("Hier hast du noch keine Idee gespeichert.").assertExists()
    }

    @Test fun failedAuthenticatedLoadNeverDisplaysPrivateIdeaOrCreation() {
        val item = StrategyExperiment.fromOwnIdea(
            "Private Testidee, die niemals angezeigt werden darf",
            System.currentTimeMillis()
        )
        rule.setContent {
            EmotionskompassTheme {
                StrategyExperimentsScreen(
                    state = StrategyExperimentUiState(
                        entries = listOf(item), loaded = false,
                        error = "Sichere Entschlüsselung fehlgeschlagen."
                    ),
                    onReload = {}, onSave = {}, onArchive = { _, _ -> },
                    onDelete = {}
                )
            }
        }
        rule.onNodeWithText(item.customTitle!!).assertDoesNotExist()
        rule.onNodeWithTag("experiment-create").assertDoesNotExist()
        rule.onNodeWithTag("experiments-retry").assertExists()
    }

    @Test fun selectedAtlasLimitIsVisibleBeforeUserSaves() {
        val selected = requireNotNull(catalog.find("boundaries"))
        var saves = 0
        rule.setContent {
            EmotionskompassTheme {
                StrategyExperimentsScreen(
                    state = StrategyExperimentUiState(loaded = true),
                    onReload = {}, onSave = { saves++ },
                    onArchive = { _, _ -> }, onDelete = {},
                    catalog = catalog, incomingCatalog = selected
                )
            }
        }
        rule.onNodeWithTag("experiment-editor-list").performScrollToNode(
            hasTestTag("experiment-source-limit")
        )
        rule.onNodeWithTag("experiment-source-limit-heading").assertExists()
        rule.onNodeWithText(selected.limitations).assertExists()
        rule.runOnIdle { assertEquals(0, saves) }
        rule.onNodeWithTag("experiment-cancel").performClick()
        rule.onNodeWithText("Entwurf verwerfen").performClick()
        rule.runOnIdle { assertEquals(0, saves) }
    }

    @Test fun longPrivateNotesCollapseAndCanBeExpandedWithoutEditing() {
        val original = StrategyExperiment.fromOwnIdea(
            "Fiktive längere Idee", System.currentTimeMillis()
        ).copy(
            planText = "P".repeat(245),
            cautionText = "Achte auf Grenzen: " + "G".repeat(205),
            reflectionText = "R".repeat(250)
        )
        rule.setContent {
            EmotionskompassTheme {
                StrategyExperimentsScreen(
                    state = StrategyExperimentUiState(
                        entries = listOf(original), loaded = true
                    ),
                    onReload = {}, onSave = {},
                    onArchive = { _, _ -> }, onDelete = {}
                )
            }
        }
        rule.onNodeWithTag("experiments-list").performScrollToNode(
            hasTestTag("experiment-expand-${original.id}")
        )
        rule.onNodeWithText(original.planText).assertDoesNotExist()
        rule.onNodeWithTag("experiment-expand-${original.id}").performClick()
        rule.onNodeWithText("Weniger anzeigen").assertExists()
        rule.onNodeWithText(original.planText).assertExists()
        rule.onNodeWithText(original.cautionText).assertExists()
        rule.onNodeWithText(original.reflectionText).assertExists()
        // The expanded card is taller than the viewport. Bring its final action
        // into view so the collapse button is not clipped at the viewport edge.
        rule.onNodeWithTag("experiments-list").performScrollToNode(
            hasTestTag("experiment-delete-${original.id}")
        )
        rule.onNodeWithTag("experiment-expand-${original.id}").performClick()
        // Collapsing shortens the LazyColumn item, so the button can leave the
        // composed viewport. Scroll it into view before asserting its label.
        rule.onNodeWithTag("experiments-list").performScrollToNode(
            hasTestTag("experiment-expand-${original.id}")
        )
        rule.onNodeWithTag("experiment-expand-${original.id}")
            .assertTextEquals("Alle eigenen Texte anzeigen")
        rule.onNodeWithText(original.planText).assertDoesNotExist()
    }

    @Test fun searchingAlsoFindsOwnSafetyNotesAndReflections() {
        val item = StrategyExperiment.fromOwnIdea(
            "Fiktive Überschrift", System.currentTimeMillis()
        ).copy(cautionText = "Individuelles Warnsignal beachten",
            reflectionText = "Gedanken am Abend")
        rule.setContent {
            EmotionskompassTheme {
                StrategyExperimentsScreen(
                    state = StrategyExperimentUiState(
                        entries = listOf(item), loaded = true
                    ),
                    onReload = {}, onSave = {},
                    onArchive = { _, _ -> }, onDelete = {}
                )
            }
        }
        rule.onNodeWithTag("experiments-list").performScrollToNode(
            hasTestTag("experiment-search")
        )
        rule.onNodeWithTag("experiment-search").performTextInput("Warnsignal")
        rule.onNodeWithText("Fiktive Überschrift").assertExists()
    }

    @Test fun staleInsightLinksNeedExplicitRemovalBeforeSaving() {
        val missing = java.util.UUID.randomUUID().toString()
        val original = StrategyExperiment.fromOwnIdea(
            "Meine bestehende Testidee", System.currentTimeMillis()
        ).copy(relatedInsightIds = setOf(missing))
        var saved: StrategyExperiment? = null
        rule.setContent {
            EmotionskompassTheme {
                StrategyExperimentsScreen(
                    state = StrategyExperimentUiState(
                        entries = listOf(original), loaded = true
                    ),
                    onReload = {}, onSave = { saved = it },
                    onArchive = { _, _ -> }, onDelete = {},
                    insights = emptyList()
                )
            }
        }
        rule.onNodeWithTag("experiments-list").performScrollToNode(
            hasTestTag("experiment-edit-${original.id}")
        )
        rule.onNodeWithTag("experiment-edit-${original.id}").performClick()
        rule.onNodeWithTag("experiment-editor-list").performScrollToNode(
            hasTestTag("experiment-stale-link-warning")
        )
        rule.onNodeWithTag("experiment-stale-link-warning").assertExists()
        rule.onNodeWithTag("experiment-explicit-save").assertIsNotEnabled()
        // The warning may be visible while its action is still below the fold.
        rule.onNodeWithTag("experiment-editor-list").performScrollToNode(
            hasTestTag("experiment-remove-stale-links")
        )
        rule.onNodeWithTag("experiment-remove-stale-links").performClick()
        rule.waitForIdle()
        // Verify that the invalid links really were removed before saving.
        rule.onNodeWithTag("experiment-explicit-save").assertIsEnabled()
        rule.onNodeWithTag("experiment-explicit-save").performClick()
        rule.runOnIdle {
            assertTrue(saved != null)
            assertTrue(saved?.relatedInsightIds.orEmpty().isEmpty())
            assertEquals(original.planText, saved?.planText)
        }
    }

    @Test fun transientInsightLoadingDoesNotEraseUnsavedIdea() {
        var changeAvailability: (Boolean) -> Unit = {}
        var saved: StrategyExperiment? = null
        rule.setContent {
            EmotionskompassTheme {
                var ready by remember { mutableStateOf(true) }
                changeAvailability = { ready = it }
                StrategyExperimentsScreen(
                    state = StrategyExperimentUiState(loaded = true),
                    onReload = {}, onSave = { saved = it },
                    onArchive = { _, _ -> }, onDelete = {},
                    insightsReady = ready,
                    insightsBusy = !ready
                )
            }
        }
        rule.onNodeWithTag("experiment-create").performClick()
        rule.onNodeWithTag("experiment-title").performTextInput(
            "Idee nicht durch kurzes Laden verlieren"
        )
        rule.runOnIdle { changeAvailability(false) }
        rule.onNodeWithTag("experiment-title").assertDoesNotExist()
        rule.onNodeWithText(
            "Deine persönlichen Ideen und Erkenntnisse werden sicher geladen …"
        ).assertExists()
        rule.runOnIdle { changeAvailability(true) }
        rule.onNodeWithTag("experiment-title")
            .assertTextContains("Idee nicht durch kurzes Laden verlieren")
        rule.onNodeWithTag("experiment-explicit-save").performClick()
        rule.runOnIdle { assertEquals("Idee nicht durch kurzes Laden verlieren", saved?.customTitle) }
    }


    @Test fun relockWhileEditingDiscardsPrivateUnsavedDraft() {
        var updateLoaded: (Boolean) -> Unit = {}
        rule.setContent {
            EmotionskompassTheme {
                var loaded by remember { mutableStateOf(true) }
                updateLoaded = { loaded = it }
                StrategyExperimentsScreen(
                    state = StrategyExperimentUiState(loaded = loaded),
                    onReload = {}, onSave = {},
                    onArchive = { _, _ -> }, onDelete = {}
                )
            }
        }
        rule.onNodeWithTag("experiment-create").performClick()
        rule.onNodeWithTag("experiment-title").performTextInput(
            "Entwurf ist nach Sperre nicht wiederherstellbar"
        )
        rule.runOnIdle { updateLoaded(false) }
        rule.onNodeWithTag("experiment-title").assertDoesNotExist()
        rule.runOnIdle { updateLoaded(true) }
        rule.onNodeWithTag("experiments-list").performScrollToNode(
            hasTestTag("experiment-create")
        )
        rule.onNodeWithTag("experiment-create").performClick()
        val editable = rule.onNodeWithTag("experiment-title")
            .fetchSemanticsNode().config[SemanticsProperties.EditableText].text
        rule.runOnIdle { assertEquals("", editable) }
    }
}
