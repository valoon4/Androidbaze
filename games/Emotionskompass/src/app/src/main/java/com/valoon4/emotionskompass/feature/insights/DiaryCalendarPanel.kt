package com.valoon4.emotionskompass.feature.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.valoon4.emotionskompass.core.insights.DiaryCalendarMonth
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val monthTitleFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", Locale.GERMANY)
private val fullDayFormatter = DateTimeFormatter.ofPattern("EEEE, d. MMMM yyyy", Locale.GERMANY)

/**
 * Ephemeral navigation only; no saved state, no journal words or IDs.
 * App shell owns this state so returning from an entry restores the same day.
 */
class DiaryCalendarUiState {
    var expanded by mutableStateOf(false)
    var month by mutableStateOf(YearMonth.now())
    var selectedDate by mutableStateOf<LocalDate?>(null)
    var listRequested by mutableStateOf(false)

    fun toggle() {
        expanded = !expanded
        if (!expanded) selectedDate = null
    }

    fun chooseMonth(next: YearMonth) {
        month = next
        selectedDate = null
    }

    fun resetPrivateState() {
        expanded = false
        selectedDate = null
        listRequested = false
        month = YearMonth.now()
    }
}

private fun countLabel(count: Int): String =
    if (count == 1) "1 gespeicherter Eintrag" else "$count gespeicherte Einträge"

/**
 * One optional navigation surface. All 28–31 dates and entry counts are
 * equivalent in the visual grid and the accessible list.
 */
@Composable
fun DiaryCalendarPanel(
    calendar: DiaryCalendarMonth,
    state: DiaryCalendarUiState
) {
    Column(
        modifier = Modifier.fillMaxWidth().testTag("diary-calendar-panel"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedButton(
            onClick = state::toggle,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                .testTag("diary-calendar-toggle")
        ) {
            Text(if (state.expanded) "Tagebuchkalender ausblenden"
                else "Tagebuch nach Datum finden")
        }
        if (!state.expanded) return@Column

        if (state.selectedDate != null) {
            val selected = state.selectedDate!!
            val day = calendar.day(selected)
            OutlinedButton(
                onClick = { state.selectedDate = null },
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                    .testTag("diary-calendar-back-to-month")
            ) { Text("Zurück zum Monat") }
            Text(
                selected.format(fullDayFormatter),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.testTag("diary-calendar-selected-date")
            )
            Text(
                countLabel(day?.entryCount ?: 0) + " an diesem Tag.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.testTag("diary-calendar-selected-count")
            )
            if (day == null || day.entryCount == 0) {
                Text(
                    "Für diesen Tag ist kein Eintrag gespeichert. " +
                        "Daraus lässt sich nichts über dein Erleben ableiten.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    "Wähle unten einen Eintrag, um ihn im geschützten Tagebuch anzusehen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Column
        }

        Text(
            calendar.month.atDay(1).format(monthTitleFormatter),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.testTag("diary-calendar-month")
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { state.chooseMonth(state.month.minusMonths(1)) },
                modifier = Modifier.weight(1f).heightIn(min = 52.dp)
                    .testTag("diary-calendar-previous-month")
            ) { Text("‹ Vorheriger Monat") }
            OutlinedButton(
                onClick = { state.chooseMonth(state.month.plusMonths(1)) },
                modifier = Modifier.weight(1f).heightIn(min = 52.dp)
                    .testTag("diary-calendar-next-month")
            ) { Text("Nächster Monat ›") }
        }
        Text(
            "Alle sicher geladenen Tagebucheinträge im gewählten Monat – " +
                "unabhängig vom 7-, 30- oder 90-Tage-Filter oben. " +
                "Leere Tage sagen nichts über dein Erleben aus.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            // Seven cells need >=48dp each; 200% text should use the list.
            val supportsGrid = maxWidth >= 336.dp && LocalDensity.current.fontScale <= 1.3f
            if (!supportsGrid || state.listRequested) {
                if (!supportsGrid) {
                    Text(
                        "Für diese Bildschirm- und Schriftgröße wird die " +
                            "gleichwertige Datumsliste angezeigt.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                            .testTag("diary-calendar-list-fallback")
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    if (supportsGrid) {
                        OutlinedButton(
                            onClick = { state.listRequested = false },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                .testTag("diary-calendar-show-grid")
                        ) { Text("Als Monatsraster ansehen") }
                    }
                    calendar.days.forEach { day ->
                        OutlinedButton(
                            onClick = { state.selectedDate = day.date },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                .testTag("diary-calendar-day-${day.date}")
                        ) {
                            Text(
                                "${day.date.format(fullDayFormatter)} · ${countLabel(day.entryCount)}"
                            )
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    OutlinedButton(
                        onClick = { state.listRequested = true },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                            .testTag("diary-calendar-show-list")
                    ) { Text("Als Datumliste ansehen") }
                    Row(Modifier.fillMaxWidth()) {
                        listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So").forEach { label ->
                            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                Text(label, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                    val startBlank = calendar.month.atDay(1).dayOfWeek.value - 1
                    val cells = List(startBlank) { null } + calendar.days
                    cells.chunked(7).forEach { week ->
                        Row(Modifier.fillMaxWidth()) {
                            (week + List(7 - week.size) { null }).forEach { day ->
                                if (day == null) {
                                    Box(Modifier.weight(1f).heightIn(min = 52.dp))
                                } else {
                                    val description = "${day.date.format(fullDayFormatter)}, " +
                                        countLabel(day.entryCount) + ", Tag auswählen"
                                    Column(
                                        modifier = Modifier.weight(1f).heightIn(min = 52.dp)
                                            .testTag("diary-calendar-day-${day.date}")
                                            .background(
                                                if (day.entryCount > 0)
                                                    MaterialTheme.colorScheme.surfaceVariant
                                                else MaterialTheme.colorScheme.surface,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .clickable(role = Role.Button) {
                                                state.selectedDate = day.date
                                            }
                                            .semantics { contentDescription = description },
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            "${day.date.dayOfMonth}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        if (day.entryCount > 0) {
                                            Text(
                                                "${day.entryCount}×",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
        HorizontalDivider()
    }
}
