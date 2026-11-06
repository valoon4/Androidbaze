package com.valoon4.emotionskompass.feature.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.valoon4.emotionskompass.core.insights.WeeklyReviewMonth
import com.valoon4.emotionskompass.core.insights.WeeklyReviewWeek
import com.valoon4.emotionskompass.core.insights.WeeklyTermCount
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val weekDateFormatter = DateTimeFormatter.ofPattern("dd. MMM yyyy", Locale.GERMANY)
private val monthFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", Locale.GERMANY)

/** Private navigation state stays in memory, never SavedState, backups or preferences. */
class WeeklyReviewUiState {
    var expanded by mutableStateOf(false)
    var month by mutableStateOf(YearMonth.now())
    var selectedWeekStart by mutableStateOf<LocalDate?>(null)
    var showAllTerms by mutableStateOf(false)
    var showEvidence by mutableStateOf(false)
    var evidenceTerm by mutableStateOf<Pair<String, String>?>(null)

    fun toggle() {
        expanded = !expanded
        if (!expanded) clearWeekSelection()
    }

    fun chooseMonth(next: YearMonth) {
        month = next
        clearWeekSelection()
    }

    fun toggleWeek(firstDay: LocalDate) {
        if (selectedWeekStart == firstDay) clearWeekSelection()
        else {
            clearWeekSelection()
            selectedWeekStart = firstDay
        }
    }

    fun openWeekEvidence() {
        evidenceTerm = null
        showEvidence = true
    }

    fun openTermEvidence(kind: String, id: String) {
        require(kind == "emotion" || kind == "need")
        evidenceTerm = kind to id
        // Replacing the full term list with its selected evidence places the
        // actual journal links directly below the week, even at 200% font.
        showAllTerms = false
        showEvidence = true
    }

    fun clearWeekSelection() {
        selectedWeekStart = null
        showAllTerms = false
        showEvidence = false
        evidenceTerm = null
    }

    fun resetPrivateState() {
        expanded = false
        month = YearMonth.now()
        clearWeekSelection()
    }
}

private fun entryLabel(count: Int): String =
    if (count == 1) "1 gespeicherter Eintrag" else "$count gespeicherte Einträge"

private fun weekLabel(week: WeeklyReviewWeek): String =
    "${week.firstDay.format(weekDateFormatter)} – " +
        week.lastDayInclusive.format(weekDateFormatter)

/** Optional heading and month controls, independent of 7/30/90-day statistics. */
@Composable
fun WeeklyReviewHeader(month: WeeklyReviewMonth, state: WeeklyReviewUiState) {
    Column(
        Modifier.fillMaxWidth().testTag("weekly-review-panel"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = state::toggle,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                .testTag("weekly-review-toggle")
        ) {
            Text(if (state.expanded) "Wochenrückblick ausblenden"
                else "Wochenrückblick ansehen")
        }
        if (!state.expanded) return@Column
        Text(
            "Wochenrückblick – ${month.month.atDay(1).format(monthFormatter)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.semantics { heading() }
                .testTag("weekly-review-month")
        )
        // Full-width buttons remain legible and accessible at 320dp / 200%.
        OutlinedButton(
            onClick = { state.chooseMonth(state.month.minusMonths(1)) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                .testTag("weekly-review-previous-month")
        ) { Text("Vorheriger Monat") }
        OutlinedButton(
            onClick = { state.chooseMonth(state.month.plusMonths(1)) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                .testTag("weekly-review-next-month")
        ) { Text("Nächster Monat") }
        if (state.month != YearMonth.now()) {
            OutlinedButton(
                onClick = { state.chooseMonth(YearMonth.now()) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                    .testTag("weekly-review-current-month")
            ) {
                Text("Zum aktuellen Monat")
            }
        }
        Text(
            "Gezeigt werden die vollständigen Wochen von Montag bis Sonntag, " +
                "die diesen Monat berühren. Randwochen enthalten auch Tage aus " +
                "benachbarten Monaten. Grundlage sind alle sicher geladenen " +
                "Tagebucheinträge, unabhängig vom Statistikfilter oben.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "Gezählt werden ausschließlich deine dokumentierten Auswahlen. " +
                "Kein Eintrag bedeutet nicht, dass du nichts erlebt hast. " +
                "Die Nennungen beschreiben weder Gefühlsstärke noch Fortschritt.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider()
    }
}

@Composable
private fun PreviewTerm(
    kind: String,
    count: WeeklyTermCount,
    name: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            .testTag("weekly-review-preview-$kind-${count.termId}")
    ) {
        Text("$name · ${count.mentions}× genannt · Einträge ansehen")
    }
}

/**
 * Exactly one expanded week at a time. At most six preview terms are placed
 * inside the card; the complete list is rendered as separate LazyColumn items.
 */
@Composable
fun WeeklyReviewCard(
    week: WeeklyReviewWeek,
    state: WeeklyReviewUiState,
    emotionName: (String) -> String,
    needName: (String) -> String,
    canReflect: Boolean = false,
    onReflect: () -> Unit = {}
) {
    val open = state.selectedWeekStart == week.firstDay
    Card(Modifier.fillMaxWidth().testTag("weekly-review-week-card-${week.firstDay}")) {
        Column(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { state.toggleWeek(week.firstDay) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                    .testTag("weekly-review-week-${week.firstDay}")
            ) {
                Text(
                    weekLabel(week) + " · " + entryLabel(week.entryCount) +
                        if (open) " · Details ausblenden" else " · Details anzeigen"
                )
            }
            if (!open) return@Column
            if (week.entryCount == 0) {
                Text(
                    "Für diese Woche sind keine Einträge gespeichert. " +
                        "Daraus lässt sich nichts über dein Erleben ableiten.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("weekly-review-empty-${week.firstDay}")
                )
                return@Column
            }
            if (!state.showAllTerms) {
                Text(
                    "Gefühle – dokumentierte Nennungen",
                    style = MaterialTheme.typography.titleSmall
                )
                if (week.emotionCounts.isEmpty()) {
                    Text("Keine Gefühle ausgewählt", style = MaterialTheme.typography.bodySmall)
                } else {
                    week.emotionCounts.take(3).forEach { term ->
                        PreviewTerm("emotion", term, emotionName(term.termId)) {
                            state.openTermEvidence("emotion", term.termId)
                        }
                    }
                }
                Text(
                    "Bedürfnisse – dokumentierte Nennungen",
                    style = MaterialTheme.typography.titleSmall
                )
                if (week.needCounts.isEmpty()) {
                    Text("Keine konkreten Bedürfnisse ausgewählt",
                        style = MaterialTheme.typography.bodySmall)
                } else {
                    week.needCounts.take(3).forEach { term ->
                        PreviewTerm("need", term, needName(term.termId)) {
                            state.openTermEvidence("need", term.termId)
                        }
                    }
                }
            } else {
                Text(
                    "Alle dokumentierten Begriffe folgen als vollständige Liste " +
                        "mit ihren Belegen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (week.emotionCounts.size > 3 || week.needCounts.size > 3 ||
                state.showAllTerms) {
                TextButton(
                    onClick = { state.showAllTerms = !state.showAllTerms },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        .testTag("weekly-review-all-terms-${week.firstDay}")
                ) {
                    Text(if (state.showAllTerms) "Weniger Begriffe anzeigen"
                        else "Alle dokumentierten Begriffe anzeigen")
                }
            }
            OutlinedButton(
                onClick = state::openWeekEvidence,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                    .testTag("weekly-review-evidence-toggle-${week.firstDay}")
            ) { Text("Einträge dieser Woche ansehen") }
            TextButton(
                onClick = onReflect,
                enabled = canReflect,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                    .testTag("weekly-review-reflection-${week.firstDay}")
            ) {
                Text("Freiwillig über diese Woche nachdenken")
            }
            Text(
                "Nur wenn du möchtest: Du kannst eine eigene Erkenntnis " +
                    "notieren. Es wird nichts automatisch interpretiert, " +
                    "verknüpft oder gespeichert.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (state.showEvidence) {
                Text(
                    "Passende Tagebucheinträge folgen direkt unter dieser Woche.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = { state.showEvidence = false; state.evidenceTerm = null },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        .testTag("weekly-review-hide-evidence-${week.firstDay}")
                ) { Text("Eintragsliste ausblenden") }
            }
        }
    }
}

/** Entire selected-week term catalog, each item separately lazy and tappable. */
@Composable
fun WeeklyReviewFullTerm(
    kind: String,
    term: WeeklyTermCount,
    name: String,
    state: WeeklyReviewUiState
) {
    OutlinedButton(
        onClick = { state.openTermEvidence(kind, term.termId) },
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
            .testTag("weekly-review-term-$kind-${term.termId}")
    ) {
        Text("${if (kind == "emotion") "Gefühl" else "Bedürfnis"}: $name · " +
            "${term.mentions}× genannt · Belege ansehen")
    }
}
