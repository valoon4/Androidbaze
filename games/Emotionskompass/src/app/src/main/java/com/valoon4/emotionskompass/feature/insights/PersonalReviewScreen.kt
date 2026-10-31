package com.valoon4.emotionskompass.feature.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.valoon4.emotionskompass.core.insights.CalculateOverview
import com.valoon4.emotionskompass.core.insights.InsightEntry
import com.valoon4.emotionskompass.core.insights.InsightOverview
import com.valoon4.emotionskompass.core.insights.InsightTermCount
import com.valoon4.emotionskompass.core.insights.InsightTimeWindow
import com.valoon4.emotionskompass.core.insights.JournalSnapshot
import com.valoon4.emotionskompass.core.model.EmotionCatalog
import com.valoon4.emotionskompass.core.model.NeedCatalog
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    listState: LazyListState
) {
    // Non-personal calendar selections may survive tab navigation/rotation.
    var preset by rememberSaveable { mutableStateOf("30") }
    val today = LocalDate.now()
    var startText by rememberSaveable { mutableStateOf(today.minusDays(29).toString()) }
    var endText by rememberSaveable { mutableStateOf(today.toString()) }
    var pickingStart by remember { mutableStateOf<Boolean?>(null) }
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
    val overview = remember(snapshot, window) {
        if (snapshot != null && window != null) CalculateOverview.calculate(snapshot, window)
        else null
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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        item {
            Text("Dein Rückblick", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() })
            Text(
                "Ein Überblick über das, was du selbst in deinem Tagebuch festgehalten hast. Keine Bewertung deiner Gefühle.",
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
                        OutlinedButton(onClick = onReload, enabled = !busy) {
                            Text("Erneut versuchen")
                        }
                    }
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
                    else -> null
                }
                val heading = when (chosen.first) {
                    "emotion" -> "Einträge mit „${emotions.find(chosen.second)?.name ?: "Gefühlsbegriff"}“"
                    "need" -> "Einträge mit „${needs.find(chosen.second)?.name ?: "Bedürfnisbegriff"}“"
                    else -> "Alle Einträge im Zeitraum"
                }
                item {
                    TextButton(onClick = { onSelectTerm(null) },
                        modifier = Modifier.fillMaxWidth()) { Text("Zurück zum Überblick") }
                    Text(heading,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.semantics { heading() })
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
                                Button(onClick = onStartCheckIn,
                                    modifier = Modifier.fillMaxWidth()) {
                                    Text("Freiwilligen Check-in beginnen")
                                }
                            }
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
                        MentionCard(term, emotions.find(term.termId)?.name ?: "Unbekannter Gefühlsbegriff",
                            onClick = { onSelectTerm("emotion" to term.termId) })
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
                        MentionCard(term, needs.find(term.termId)?.name ?: "Unbekannter Bedürfnisbegriff",
                            onClick = { onSelectTerm("need" to term.termId) })
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
    }
}

/** Separate list mode that reuses the very same immutable evidence snapshot. */
@Composable
private fun MentionCard(term: InsightTermCount, title: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Text("In ${term.mentions} Einträgen genannt · Einträge ansehen",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
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
