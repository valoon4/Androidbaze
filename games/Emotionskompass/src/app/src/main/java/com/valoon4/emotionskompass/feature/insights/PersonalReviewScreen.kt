package com.valoon4.emotionskompass.feature.insights

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.valoon4.emotionskompass.core.checkin.LifeArea
import com.valoon4.emotionskompass.core.insights.DiaryCalendar
import com.valoon4.emotionskompass.core.insights.WeeklyReview
import com.valoon4.emotionskompass.core.insights.CalculateOverview
import com.valoon4.emotionskompass.core.insights.DiscoverPatterns
import com.valoon4.emotionskompass.core.insights.PatternObservation
import com.valoon4.emotionskompass.core.insights.PersonalInsight
import com.valoon4.emotionskompass.core.insights.InsightEntry
import com.valoon4.emotionskompass.core.insights.InsightOverview
import com.valoon4.emotionskompass.core.insights.InsightTermCount
import com.valoon4.emotionskompass.core.insights.InsightTimeWindow
import com.valoon4.emotionskompass.core.insights.JournalSnapshot
import com.valoon4.emotionskompass.core.journal.JournalEntry
import com.valoon4.emotionskompass.core.navigation.InsightUiState
import com.valoon4.emotionskompass.core.navigation.StrategyExperimentUiState
import com.valoon4.emotionskompass.core.strategies.StrategyCatalog
import com.valoon4.emotionskompass.core.strategies.StrategyCard
import com.valoon4.emotionskompass.core.model.EmotionCatalog
import com.valoon4.emotionskompass.core.model.NeedCatalog
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

private val dayFormatter = DateTimeFormatter.ofPattern("dd. MMM yyyy", Locale.GERMANY)
private const val DAY_MILLIS = 86_400_000L

/**
 * M7.1 is intentionally an exploration of *recorded selections*, not a
 * psychological score or an automatic interpretation of the user's life.
 * All journal IDs and the projection are transient in-memory data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalReviewScreen(
    snapshot: JournalSnapshot?,
    busy: Boolean,
    error: String?,
    emotions: EmotionCatalog,
    needs: NeedCatalog,
    onReload: () -> Unit,
    onOpenJournalEntry: (String) -> Unit,
    onStartCheckIn: () -> Unit,
    selectedTerm: Pair<String, String>?,
    onSelectTerm: (Pair<String, String>?) -> Unit,
    listState: LazyListState,
    personalInsights: InsightUiState = InsightUiState(),
    diaryEntries: List<JournalEntry> = emptyList(),
    onSaveInsight: (String, String?, String?) -> Unit = { _, _, _ -> },
    onArchiveInsight: (String, Boolean) -> Unit = { _, _ -> },
    onDeleteInsight: (String) -> Unit = { _ -> },
    onUnlinkInsight: (String, String) -> Unit = { _, _ -> },
    strategyExperiments: StrategyExperimentUiState = StrategyExperimentUiState(),
    onSaveExperiment: (com.valoon4.emotionskompass.core.insights.StrategyExperiment) -> Unit = { },
    onArchiveExperiment: (String, Boolean) -> Unit = { _, _ -> },
    onDeleteExperiment: (String) -> Unit = { },
    strategyCatalog: StrategyCatalog? = null,
    incomingStrategyCard: StrategyCard? = null,
    onCatalogProposalConsumed: () -> Unit = {},
    onBrowseStrategyAtlas: () -> Unit = {},
    onOpenStrategyCard: (String) -> Unit = {},
    calendarUiState: DiaryCalendarUiState? = null,
    weeklyUiState: WeeklyReviewUiState? = null
) {
    // Non-personal calendar selections may survive tab navigation/rotation.
    var preset by rememberSaveable { mutableStateOf("30") }
    var showInsightPage by rememberSaveable { mutableStateOf(false) }
    var showExperimentsPage by rememberSaveable { mutableStateOf(false) }
    // This is an in-memory hint only, not a copy of private insight text.
    var incomingIdeaInsightId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(incomingStrategyCard?.id) {
        if (incomingStrategyCard != null) {
            showInsightPage = false
            showExperimentsPage = true
        }
    }
    // Selected observation wording is in-memory only, never rememberSaveable.
    var reflectionOriginLabel by remember { mutableStateOf<String?>(null) }
    // This date-only week label is private session navigation, not an insight
    // draft, journal link, persisted weekly reflection, or Android SavedState.
    var weeklyReflectionContext by remember { mutableStateOf<String?>(null) }
    BackHandler(enabled = showInsightPage || showExperimentsPage) {
        showInsightPage = false
        showExperimentsPage = false
        reflectionOriginLabel = null
        weeklyReflectionContext = null
    }
    val today = LocalDate.now()
    var startText by rememberSaveable { mutableStateOf(today.minusDays(29).toString()) }
    var endText by rememberSaveable { mutableStateOf(today.toString()) }
    var pickingStart by remember { mutableStateOf<Boolean?>(null) }
    var patternsVisible by rememberSaveable { mutableStateOf(true) }
    var showAllPatterns by rememberSaveable { mutableStateOf(false) }
    var showPatternCriteria by rememberSaveable { mutableStateOf(false) }
    // All calendar navigation remains outside Android SavedState and encrypted stores.
    // The app shell supplies its instance to survive journal detail navigation.
    val diaryCalendarState = calendarUiState ?: remember { DiaryCalendarUiState() }
    val weeklyState = weeklyUiState ?: remember { WeeklyReviewUiState() }
    var showMentionBars by remember { mutableStateOf(false) }
    LaunchedEffect(snapshot, busy, error) {
        if (snapshot == null || busy || error != null) {
            showMentionBars = false
            diaryCalendarState.resetPrivateState()
            weeklyState.resetPrivateState()
            // A lock/import failure must never retain a weekly prompt context.
            if (weeklyReflectionContext != null) {
                weeklyReflectionContext = null
                showInsightPage = false
            }
        }
    }
    BackHandler(
        enabled = !showInsightPage && !showExperimentsPage && selectedTerm == null &&
            diaryCalendarState.expanded && diaryCalendarState.selectedDate != null
    ) {
        diaryCalendarState.selectedDate = null
    }
    // Back returns from a selected week evidence list to its card, then to
    // the month overview. This does not affect other journal navigation.
    BackHandler(
        enabled = !showInsightPage && !showExperimentsPage && selectedTerm == null &&
            weeklyState.expanded && weeklyState.selectedWeekStart != null
    ) {
        if (weeklyState.showEvidence) {
            weeklyState.showEvidence = false
            weeklyState.evidenceTerm = null
        } else weeklyState.clearWeekSelection()
    }
    val scope = rememberCoroutineScope()
    // The evidence list replaces the overview's lazy items. Always reveal
    // its heading instead of retaining an offscreen overview scroll position.
    LaunchedEffect(selectedTerm) {
        if (selectedTerm != null) listState.scrollToItem(0)
    }
    // Selection is held only in the locked ViewModel's in-memory Flow, never Bundle.
    val zone = ZoneId.systemDefault()
    val window = remember(preset, startText, endText, today, zone) {
        runCatching {
            if (preset == "custom") {
                InsightTimeWindow.inclusiveDays(
                    LocalDate.parse(startText), LocalDate.parse(endText), zone
                )
            } else {
                InsightTimeWindow.recentDays(preset.toInt(), Instant.now(), zone)
            }
        }.getOrNull()
    }
    // Calendar always sees the whole authenticated journal, regardless of the
    // independent 7/30/90-day overview filter. No extra persistence or API.
    val diaryCalendarMonth = remember(snapshot, diaryCalendarState.month, zone) {
        snapshot?.let { DiaryCalendar.month(it, diaryCalendarState.month, zone) }
    }
    val diaryById = remember(snapshot) {
        snapshot?.entries?.associateBy { it.journalId }.orEmpty()
    }
    // Weekly cards are independent of the statistics filter and do not
    // inspect diary narrative text, insight drafts, or strategy ideas.
    val weeklyMonth = remember(snapshot, weeklyState.month, zone) {
        snapshot?.let { WeeklyReview.month(it, weeklyState.month, zone) }
    }
    val overview = remember(snapshot, window) {
        if (snapshot != null && window != null) CalculateOverview.calculate(snapshot, window)
        else null
    }

    val patterns = remember(snapshot, window, zone) {
        if (snapshot != null && window != null) DiscoverPatterns.discover(snapshot, window, zone)
        else emptyList()
    }
    fun patternTitle(pattern: PatternObservation): String {
        val first = emotions.find(pattern.firstId)?.name ?: "Unbekannter Gefühlsbegriff"
        val second = when (pattern.kind) {
            "emotion-need" -> needs.find(pattern.secondId)?.name ?: "Unbekanntes Bedürfnis"
            "emotion-area" -> LifeArea.entries.firstOrNull {
                it.name == pattern.secondId
            }?.label ?: "Unbekannter Lebensbereich"
            else -> emotions.find(pattern.secondId)?.name ?: "Unbekannter Gefühlsbegriff"
        }
        return "„$first“ und „$second“"
    }

    fun inviteReflection(pattern: PatternObservation) {
        if (!personalInsights.loaded || personalInsights.busy || personalInsights.error != null ||
            personalInsights.entries.size >= PersonalInsight.MAX_INSIGHTS) return
        reflectionOriginLabel = patternTitle(pattern)
        showExperimentsPage = false
        showInsightPage = true
    }

    if (pickingStart != null) {
        val isStart = pickingStart == true
        val initial = runCatching {
            LocalDate.parse(if (isStart) startText else endText)
        }.getOrElse { today }
        key(isStart) {
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = initial.toEpochDay() * DAY_MILLIS
        )
        DatePickerDialog(
            onDismissRequest = { pickingStart = null },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let { utcMillis ->
                        val chosen = LocalDate.ofEpochDay(Math.floorDiv(utcMillis, DAY_MILLIS))
                        if (isStart) startText = chosen.toString()
                        else endText = chosen.toString()
                    }
                    pickingStart = null
                    onSelectTerm(null)
                }) { Text("Übernehmen") }
            },
            dismissButton = {
                TextButton(onClick = { pickingStart = null }) { Text("Abbrechen") }
            }
        ) { DatePicker(state = dateState) }
        }
    }

    // These two equally visible areas belong to the same Rückblick section.
    // Keep the choice visible while scrolling, rather than hiding a second page.
    Column(modifier = Modifier.fillMaxSize()) {
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            FilterChip(
                selected = !showInsightPage && !showExperimentsPage,
                onClick = {
                    showInsightPage = false
                    showExperimentsPage = false
                    reflectionOriginLabel = null
                    weeklyReflectionContext = null
                },
                label = { Text("Übersicht & Muster") },
                modifier = Modifier.testTag("review-tab-overview")
            )
            FilterChip(
                selected = showInsightPage,
                onClick = {
                    reflectionOriginLabel = null
                    weeklyReflectionContext = null
                    showExperimentsPage = false
                    showInsightPage = true
                },
                label = { Text("Meine Erkenntnisse") },
                modifier = Modifier.testTag("review-tab-insights")
            )
            FilterChip(
                selected = showExperimentsPage,
                onClick = {
                    reflectionOriginLabel = null
                    weeklyReflectionContext = null
                    showInsightPage = false
                    showExperimentsPage = true
                },
                label = { Text("Ausprobieren") },
                modifier = Modifier.testTag("review-tab-experiments")
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            if (showInsightPage) {
                PersonalInsightsScreen(
                    state = personalInsights,
                    journalEntries = diaryEntries,
                    onBack = {
                        showInsightPage = false
                        reflectionOriginLabel = null
                        weeklyReflectionContext = null
                    },
                    onReload = onReload,
                    onSave = onSaveInsight,
                    onArchive = onArchiveInsight,
                    onDelete = onDeleteInsight,
                    onUnlink = onUnlinkInsight,
                    onOpenJournal = onOpenJournalEntry,
                    reflectionOriginLabel = reflectionOriginLabel,
                    weeklyReflectionContext = weeklyReflectionContext,
                    onWeeklyReflectionDismissed = {
                        // Return to the same expanded week and its evidence.
                        weeklyReflectionContext = null
                        showInsightPage = false
                    },
                    showBackButton = false,
                    onConsiderIdeaFromInsight = { id ->
                        incomingIdeaInsightId = id
                        showInsightPage = false
                        showExperimentsPage = true
                    }
                )
            } else if (showExperimentsPage) {
                StrategyExperimentsScreen(
                    state = strategyExperiments,
                    onReload = onReload,
                    onSave = onSaveExperiment,
                    onArchive = onArchiveExperiment,
                    onDelete = onDeleteExperiment,
                    catalog = strategyCatalog,
                    insights = personalInsights.entries,
                    insightsReady = personalInsights.loaded && !personalInsights.busy &&
                        personalInsights.error == null,
                    insightsBusy = personalInsights.busy || !personalInsights.loaded &&
                        personalInsights.error == null,
                    insightsError = personalInsights.error != null,
                    incomingCatalog = incomingStrategyCard,
                    incomingInsightId = incomingIdeaInsightId,
                    onConsumeIncoming = {
                        incomingIdeaInsightId = null
                        onCatalogProposalConsumed()
                    },
                    onBrowseAtlas = onBrowseStrategyAtlas,
                    onOpenStrategy = onOpenStrategyCard
                )
            } else {
                BoxWithConstraints(Modifier.fillMaxSize()) {
                    // Content has 20dp horizontal lazy padding on both sides.
                    // Seven 48dp cells cannot fit on a 320dp device.
                    val supportsCalendarGrid = maxWidth - 40.dp >= 336.dp &&
                        LocalDensity.current.fontScale <= 1.3f
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(15.dp)
                    ) {
        // A selected evidence view should start with its own heading and
        // records, not two large overview sections at 200% font scale.
        if (selectedTerm == null) {
        item {
            Text("Dein Rückblick", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() })
            Text(
                "Hier findest du deine Einträge, Häufigkeiten und wiederkehrende Auswahlen. Deine Gedanken stehen unter „Meine Erkenntnisse“. Unter „Ausprobieren“ kannst du freiwillig eigene Ideen festhalten. Keine Bewertung deiner Gefühle.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        item {
            Text("Zeitraum", style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() })
            // Wrap chips instead of hiding the custom period behind horizontal
            // scrolling. At 200% font scale, every option remains discoverable.
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("7" to "7 Tage", "30" to "30 Tage",
                    "90" to "90 Tage", "custom" to "Eigener Zeitraum")
                    .forEach { (key, label) ->
                        FilterChip(
                            selected = key == preset,
                            onClick = { preset = key; onSelectTerm(null) },
                            label = { Text(label) }
                        )
                    }
            }
            if (preset == "custom") {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    OutlinedButton(onClick = { pickingStart = true },
                        modifier = Modifier.fillMaxWidth()) {
                        Text("Von: ${displayDate(startText)}")
                    }
                    OutlinedButton(onClick = { pickingStart = false },
                        modifier = Modifier.fillMaxWidth()) {
                        Text("Bis: ${displayDate(endText)}")
                    }
                }
            }
            if (window == null) {
                Text(
                    "Bitte wähle einen gültigen Zeitraum. Das Anfangsdatum darf nicht nach dem Enddatum liegen; maximal sind zehn Jahre möglich.",
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Text("${window.firstDay.format(dayFormatter)} – ${window.lastDayInclusive.format(dayFormatter)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        } // overview introduction and period picker
        if (selectedTerm == null && error == null && !busy && snapshot != null &&
            diaryCalendarMonth != null) {
            item(key = "diary-calendar") {
                DiaryCalendarPanel(
                    diaryCalendarMonth,
                    diaryCalendarState,
                    supportsGrid = supportsCalendarGrid
                )
            }
            // In list mode each local date is independently lazy and scrollable.
            // Avoid a 31-button child column that can be unreachable at 200% font.
            if (diaryCalendarState.expanded && diaryCalendarState.selectedDate == null &&
                (!supportsCalendarGrid || diaryCalendarState.listRequested)) {
                items(diaryCalendarMonth.days, key = { "calendar-day-${it.date}" }) { day ->
                    DiaryCalendarDateListItem(day) {
                        diaryCalendarState.selectedDate = day.date
                    }
                }
            }
            // Lazily render this day's evidence, not thousands of cards in
            // one calendar item. These IDs come from the same secure snapshot.
            val chosenDay = diaryCalendarState.selectedDate?.let(diaryCalendarMonth::day)
            if (diaryCalendarState.expanded && chosenDay != null) {
                items(chosenDay.journalIds, key = { "calendar-entry-$it" }) { id ->
                    val entry = diaryById[id]
                    if (entry != null) {
                        Card(
                            onClick = { onOpenJournalEntry(id) },
                            modifier = Modifier.fillMaxWidth()
                                .testTag("diary-calendar-entry-$id")
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    formatTime(entry.createdAtUtcMillis, zone),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "Tagebucheintrag ansehen",
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
        if (selectedTerm == null && error == null && !busy && snapshot != null &&
            weeklyMonth != null) {
            item(key = "weekly-review-header") {
                WeeklyReviewHeader(weeklyMonth, weeklyState)
            }
            if (weeklyState.expanded) {
                weeklyMonth.weeks.forEach { week ->
                    item(key = "weekly-review-week-item-${week.firstDay}") {
                        WeeklyReviewCard(
                            week = week,
                            state = weeklyState,
                            emotionName = { id ->
                                emotions.find(id)?.name ?: "Unbekannter Gefühlsbegriff"
                            },
                            needName = { id ->
                                needs.find(id)?.name ?: "Unbekanntes Bedürfnis"
                            },
                            canReflect = personalInsights.loaded &&
                                !personalInsights.busy && personalInsights.error == null &&
                                personalInsights.entries.size < PersonalInsight.MAX_INSIGHTS,
                            onReflect = {
                                reflectionOriginLabel = null
                                weeklyReflectionContext =
                                    "${week.firstDay.format(dayFormatter)} – " +
                                    week.lastDayInclusive.format(dayFormatter)
                                showExperimentsPage = false
                                showInsightPage = true
                            }
                        )
                    }
                    if (weeklyState.selectedWeekStart == week.firstDay) {
                        if (weeklyState.showAllTerms) {
                            items(
                                week.emotionCounts,
                                key = { "weekly-all-emotion-${week.firstDay}-${it.termId}" }
                            ) { term ->
                                WeeklyReviewFullTerm(
                                    kind = "emotion", term = term,
                                    name = emotions.find(term.termId)?.name
                                        ?: "Unbekannter Gefühlsbegriff",
                                    state = weeklyState
                                )
                            }
                            items(
                                week.needCounts,
                                key = { "weekly-all-need-${week.firstDay}-${it.termId}" }
                            ) { term ->
                                WeeklyReviewFullTerm(
                                    kind = "need", term = term,
                                    name = needs.find(term.termId)?.name
                                        ?: "Unbekanntes Bedürfnis",
                                    state = weeklyState
                                )
                            }
                        }
                        if (weeklyState.showEvidence && week.entryCount > 0) {
                            val term = weeklyState.evidenceTerm
                            val ids = when (term?.first) {
                                "emotion" -> week.emotionCounts.firstOrNull {
                                    it.termId == term.second
                                }?.journalIds.orEmpty()
                                "need" -> week.needCounts.firstOrNull {
                                    it.termId == term.second
                                }?.journalIds.orEmpty()
                                else -> week.journalIds
                            }
                            item(key = "weekly-evidence-heading-${week.firstDay}") {
                                Text(
                                    if (term == null) "Einträge dieser Woche"
                                    else {
                                        val name = when (term.first) {
                                            "emotion" -> emotions.find(term.second)?.name
                                                ?: "Unbekannter Gefühlsbegriff"
                                            "need" -> needs.find(term.second)?.name
                                                ?: "Unbekanntes Bedürfnis"
                                            else -> "Begriff"
                                        }
                                        "Einträge mit „$name“ in dieser Woche"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.semantics { heading() }
                                        .testTag("weekly-review-evidence-heading")
                                )
                            }
                            items(ids, key = { "weekly-entry-${week.firstDay}-$it" }) { id ->
                                val entry = diaryById[id]
                                if (entry != null) {
                                    Card(
                                        onClick = { onOpenJournalEntry(id) },
                                        modifier = Modifier.fillMaxWidth()
                                            .testTag("weekly-review-entry-$id")
                                    ) {
                                        Column(
                                            Modifier.fillMaxWidth().padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Text(
                                                formatTime(entry.createdAtUtcMillis, zone),
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                "Tagebucheintrag ansehen",
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (error != null) {
            item {
                Card(colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Text("Dein Tagebuch konnte nicht sicher geöffnet werden.",
                            fontWeight = FontWeight.SemiBold)
                        Text(error)
                        Text("Vorhandene Daten bleiben unverändert. Es werden keine scheinbar leeren Statistiken angezeigt.")
                    }
                }
            }
            // The retry control gets its own lazy item. This remains fully
            // reachable even when error text spans several lines at 200%.
            item {
                OutlinedButton(onClick = onReload, enabled = !busy,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("review-retry")) {
                    Text("Erneut versuchen")
                }
            }
        } else if (busy || snapshot == null) {
            item {
                Text("Deine gespeicherten Einträge werden sicher geladen …",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else if (overview != null) {
            val chosen = selectedTerm
            if (chosen != null) {
                val matching = when (chosen.first) {
                    "emotion" -> overview.emotionCounts.firstOrNull { it.termId == chosen.second }
                    "need" -> overview.needCounts.firstOrNull { it.termId == chosen.second }
                    "all" -> InsightTermCount("all", overview.journalIds)
                    "pattern" -> patterns.firstOrNull { it.key == chosen.second }?.let {
                        InsightTermCount(it.key, it.evidence.matchingJournalIds)
                    }
                    else -> null
                }
                val heading = when (chosen.first) {
                    "emotion" -> "Einträge mit „${emotions.find(chosen.second)?.name ?: "Gefühlsbegriff"}“"
                    "need" -> "Einträge mit „${needs.find(chosen.second)?.name ?: "Bedürfnisbegriff"}“"
                    "pattern" -> patterns.firstOrNull { it.key == chosen.second }?.let {
                        "Einträge zu ${patternTitle(it)}"
                    } ?: "Beobachtung nicht mehr verfügbar"
                    else -> "Alle Einträge im Zeitraum"
                }
                item {
                    TextButton(onClick = { onSelectTerm(null) },
                        modifier = Modifier.fillMaxWidth()) { Text("Zurück zum Überblick") }
                    Text(heading,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.semantics { heading() })
                    if (chosen.first == "pattern") {
                        patterns.firstOrNull { it.key == chosen.second }?.let { p ->
                            Text("${p.evidence.numerator} von ${p.evidence.denominator} auswertbaren Einträgen im gewählten Zeitraum; an ${p.distinctDays} verschiedenen Tagen. Die beiden Begriffe wurden im selben Eintrag ausgewählt. Das belegt keinen ursächlichen Zusammenhang.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            OutlinedButton(
                                onClick = { inviteReflection(p) },
                                enabled = personalInsights.loaded && !personalInsights.busy &&
                                    personalInsights.error == null &&
                                    personalInsights.entries.size < PersonalInsight.MAX_INSIGHTS,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                                    .testTag("review-reflect-on-pattern")
                            ) {
                                Text("Über diese Beobachtung nachdenken")
                            }
                        }
                    }
                    Text(
                        if (chosen.first == "all")
                            "${overview.entryCount} gespeicherte Einträge im ausgewählten Zeitraum. Tippe auf einen Eintrag, um ihn anzusehen."
                        else "In ${matching?.mentions ?: 0} gespeicherten Einträgen ausdrücklich ausgewählt. Tippe auf einen Eintrag, um ihn anzusehen.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (matching == null) {
                    item { Text("Diese Auswahl ist in diesem Zeitraum nicht mehr vorhanden.") }
                } else {
                    val index = snapshot.entries.associateBy { it.journalId }
                    items(matching.sourceJournalIds, key = { it }) { id ->
                        val e = index[id]
                        if (e != null) {
                            Card(onClick = { onOpenJournalEntry(id) },
                                modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(17.dp),
                                    verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Text(formatTime(e.createdAtUtcMillis, zone),
                                        style = MaterialTheme.typography.titleMedium)
                                    Text("Tagebucheintrag ansehen",
                                        color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    Card(colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Column(Modifier.fillMaxWidth().padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("${overview.entryCount} gespeicherte Einträge",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold)
                            Text(if (overview.emotionMentionCount == 1) "1 Gefühlsnennung"
                                else "${overview.emotionMentionCount} Gefühlsnennungen")
                            Text(if (overview.needMentionCount == 1) "1 Bedürfnisnennung"
                                else "${overview.needMentionCount} Bedürfnisnennungen")
                            Text("Ein Eintrag kann mehrere Wörter enthalten. Gezählt wird nur, was du selbst ausgewählt hast – nicht, wie oft du etwas erlebt hast.",
                                style = MaterialTheme.typography.bodySmall)
                            OutlinedButton(
                                onClick = {
                                    // The optional calendar/weekly list can insert
                                    // extra lazy items ahead of the pattern section.
                                    // Jump to the real heading, not a fixed index.
                                    val calendarExtras = if (diaryCalendarState.expanded) {
                                        if (diaryCalendarState.selectedDate != null)
                                            diaryCalendarState.selectedDate?.let {
                                                diaryCalendarMonth?.day(it)?.entryCount
                                            } ?: 0
                                        else if (!supportsCalendarGrid ||
                                            diaryCalendarState.listRequested)
                                            diaryCalendarMonth?.days?.size ?: 0
                                        else 0
                                    } else 0
                                    val weeklyExtras = if (weeklyState.expanded &&
                                        weeklyMonth != null) {
                                        val selected = weeklyState.selectedWeekStart?.let {
                                            weeklyMonth.week(it)
                                        }
                                        val termItems = if (selected != null &&
                                            weeklyState.showAllTerms)
                                            selected.emotionCounts.size + selected.needCounts.size
                                        else 0
                                        val evidenceItems = if (selected != null &&
                                            weeklyState.showEvidence && selected.entryCount > 0) {
                                            val term = weeklyState.evidenceTerm
                                            1 + when (term?.first) {
                                                "emotion" -> selected.emotionCounts.firstOrNull {
                                                    it.termId == term.second
                                                }?.journalIds?.size ?: 0
                                                "need" -> selected.needCounts.firstOrNull {
                                                    it.termId == term.second
                                                }?.journalIds?.size ?: 0
                                                else -> selected.entryCount
                                            }
                                        } else 0
                                        weeklyMonth.weeks.size + termItems + evidenceItems
                                    } else 0
                                    scope.launch {
                                        listState.animateScrollToItem(
                                            5 + calendarExtras + weeklyExtras
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                                    .testTag("review-jump-patterns")
                            ) { Text("Zu gemeinsam ausgewählten Begriffen") }
                        }
                    }
                }
                if (overview.entryCount > 0) {
                    item {
                        Text("Gemeinsam ausgewählte Begriffe",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.semantics { heading() }
                                .testTag("review-patterns-heading"))
                        Text("Hier siehst du Gefühle, Bedürfnisse oder Lebensbereiche, die du in mehreren Einträgen zusammen ausgewählt hast. Das sagt nichts über Ursachen aus.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        TextButton(
                            onClick = { showPatternCriteria = !showPatternCriteria },
                            modifier = Modifier.fillMaxWidth()
                                .testTag("review-pattern-criteria")
                        ) {
                            Text(if (showPatternCriteria) "Erklärung ausblenden" else "Wie entstehen diese Hinweise?")
                        }
                        if (showPatternCriteria) {
                            Text("Ein Hinweis erscheint erst bei mindestens 10 vergleichbaren Einträgen. Dieselbe Kombination muss mindestens 3-mal, an mindestens 2 verschiedenen Tagen und in mindestens einem Viertel dieser Einträge ausgewählt worden sein. Die App liest dafür keine freien Notizen aus.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(
                            onClick = { patternsVisible = !patternsVisible },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (patternsVisible) "Kombinationen ausblenden" else "Kombinationen anzeigen")
                        }
                    }
                    if (!patternsVisible) {
                        // Hidden by choice; never persist or modify the source entries.
                    } else if (patterns.isEmpty()) {
                        item {
                            Text("Im gewählten Zeitraum gibt es noch keine ausreichend belegten Kombinationen. Das ist keine Bewertung deiner Erfahrungen.")
                            if (preset != "90") {
                                OutlinedButton(
                                    onClick = { preset = "90"; onSelectTerm(null) },
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                                        .testTag("review-patterns-90days")
                                ) { Text("90 Tage betrachten") }
                            }
                        }
                    } else {
                        items(if (showAllPatterns) patterns else patterns.take(3),
                            key = { "pattern-${it.key}" }) { pattern ->
                            Card(
                                onClick = { onSelectTerm("pattern" to pattern.key) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(patternTitle(pattern),
                                        fontWeight = FontWeight.SemiBold)
                                    Text("${pattern.evidence.numerator} von ${pattern.evidence.denominator} vergleichbaren Einträgen · ${pattern.distinctDays} verschiedene Tage",
                                        style = MaterialTheme.typography.bodyMedium)
                                    Text("Gemeinsam ausgewählt, ohne Aussage über Ursache oder Bedeutung. Belege ansehen",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            OutlinedButton(
                                onClick = { inviteReflection(pattern) },
                                enabled = personalInsights.loaded && !personalInsights.busy &&
                                    personalInsights.error == null &&
                                    personalInsights.entries.size < PersonalInsight.MAX_INSIGHTS,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                                    .testTag("review-pattern-reflection-${pattern.key}")
                            ) {
                                Text("Dazu freiwillig reflektieren")
                            }
                        }
                        if (patterns.size > 3) {
                            item {
                                OutlinedButton(
                                    onClick = { showAllPatterns = !showAllPatterns },
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                                        .testTag("review-show-all-patterns")
                                ) {
                                    Text(if (showAllPatterns) "Weniger Kombinationen anzeigen"
                                        else "Alle ${patterns.size} Kombinationen anzeigen")
                                }
                            }
                        }
                    }
                }
                if (overview.entryCount == 0) {
                    item {
                        Card {
                            Column(Modifier.fillMaxWidth().padding(17.dp),
                                verticalArrangement = Arrangement.spacedBy(9.dp)) {
                                Text("In diesem Zeitraum gibt es noch keine Einträge.",
                                    fontWeight = FontWeight.SemiBold)
                                Text("Das ist völlig in Ordnung. Du kannst einen anderen Zeitraum wählen oder einen Check-in festhalten, wenn du möchtest.")
                            }
                        }
                    }
                    // Separate full-width lazy item improves touch visibility
                    // and scrolling on 200%-font, narrow 320dp Android phones.
                    item {
                        Button(onClick = onStartCheckIn,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("review-start-checkin")) {
                            Text("Freiwilligen Check-in beginnen")
                        }
                    }
                } else {
                    item {
                        OutlinedButton(
                            onClick = { onSelectTerm("all" to "all") },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Alle ${overview.entryCount} Einträge ansehen") }
                    }
                    item {
                        OutlinedButton(
                            onClick = { showMentionBars = !showMentionBars },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                                .testTag("review-mention-bars-toggle")
                        ) {
                            Text(if (showMentionBars) "Nennungsbalken ausblenden"
                                else "Nennungsbalken anzeigen")
                        }
                        if (showMentionBars) {
                            Text(
                                "Die Balken vergleichen nur, wie oft du Begriffe im gewählten Zeitraum in deinen Einträgen ausgewählt hast. Sie zeigen weder Gefühlsstärke noch Fortschritt. Der längste Balken steht jeweils für den am häufigsten genannten Begriff innerhalb der Gefühle bzw. Bedürfnisse.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.testTag("review-mention-bars-explanation")
                            )
                        }
                    }
                    item {
                        Text("Gefühle", style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.semantics { heading() })
                        Text("${overview.entriesWithEmotions} Einträge mit ausdrücklich ausgewählten Gefühlen",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall)
                    }
                    if (overview.emotionCounts.isEmpty()) {
                        item { Text("Keine Gefühle ausgewählt. Das ist keine Aussage darüber, was du gefühlt hast.") }
                    }
                    items(overview.emotionCounts, key = { "emotion-${it.termId}" }) { term ->
                        MentionCard(
                            term, emotions.find(term.termId)?.name ?: "Unbekannter Gefühlsbegriff",
                            onClick = { onSelectTerm("emotion" to term.termId) },
                            showBar = showMentionBars,
                            maxMentions = overview.emotionCounts.firstOrNull()?.mentions ?: 1,
                            tag = "emotion"
                        )
                    }
                    item {
                        HorizontalDivider()
                        Text("Bedürfnisse", style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.semantics { heading() })
                        Text("${overview.entriesWithNamedNeeds} Einträge mit ausdrücklich ausgewählten Bedürfnissen",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall)
                    }
                    if (overview.needCounts.isEmpty()) {
                        item { Text("Keine benannten Bedürfnisse ausgewählt. Daraus folgt nicht, dass Bedürfnisse unerfüllt sind.") }
                    }
                    items(overview.needCounts, key = { "need-${it.termId}" }) { term ->
                        MentionCard(
                            term, needs.find(term.termId)?.name ?: "Unbekannter Bedürfnisbegriff",
                            onClick = { onSelectTerm("need" to term.termId) },
                            showBar = showMentionBars,
                            maxMentions = overview.needCounts.firstOrNull()?.mentions ?: 1,
                            tag = "need"
                        )
                    }
                    if (overview.entriesWithUnknownNeedChoice > 0) {
                        item {
                            Text(
                                "Bei ${overview.entriesWithUnknownNeedChoice} Einträgen hast du „Ich weiß es noch nicht“ gewählt. Diese Auswahl wird nicht als konkretes Bedürfnis gezählt.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    item {
                        Text("Hinweis: Hier siehst du Nennungen in gespeicherten Einträgen, keine Messung deiner Stimmung oder psychischen Gesundheit. Zusammenhänge werden noch nicht interpretiert.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
                } // LazyColumn
                } // BoxWithConstraints
            } // overview / insights
        } // content
    } // Rückblick tabs
}

/** The decorative bar is optional; the same exact counts and evidence link remain. */
@Composable
private fun MentionCard(
    term: InsightTermCount,
    title: String,
    onClick: () -> Unit,
    showBar: Boolean,
    maxMentions: Int,
    tag: String
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().testTag("review-mention-$tag-${term.termId}"),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Text(if (term.mentions == 1) "In 1 Eintrag genannt · Eintrag ansehen"
                else "In ${term.mentions} Einträgen genannt · Einträge ansehen",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (showBar) {
                // Compare only within this category; the bar is not a score.
                val fraction = (term.mentions.toFloat() /
                    maxMentions.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
                Box(
                    Modifier.fillMaxWidth().height(8.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(8.dp))
                        .testTag("review-mention-bar-$tag-${term.termId}")
                ) {
                    Box(
                        Modifier.fillMaxWidth(fraction).height(8.dp)
                            .background(MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(8.dp))
                    )
                }
            }
        }
    }
}

private fun displayDate(text: String): String = runCatching {
    LocalDate.parse(text).format(dayFormatter)
}.getOrElse { text }

private fun formatTime(time: Long, zone: ZoneId): String =
    DateTimeFormatter.ofPattern("dd. MMM yyyy, HH:mm", Locale.GERMANY)
        .format(Instant.ofEpochMilli(time).atZone(zone))
