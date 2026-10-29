package com.valoon4.emotionskompass.feature.checkin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.valoon4.emotionskompass.core.checkin.BodyCue
import com.valoon4.emotionskompass.core.checkin.CheckInDraft
import com.valoon4.emotionskompass.core.checkin.CheckInStep
import com.valoon4.emotionskompass.core.checkin.LifeArea
import com.valoon4.emotionskompass.core.model.EmotionCatalog
import kotlin.math.roundToInt

/**
 * Entire check-in is temporary; no saveable state, database, network calls, or export.
 * UI callbacks update a ViewModel-held in-memory draft. Closing clears it.
 */
@Composable
fun CheckInScreen(
    catalog: EmotionCatalog,
    draft: CheckInDraft,
    onToggleCue: (BodyCue) -> Unit,
    onToggleEmotion: (String) -> Unit,
    onIntensity: (String, Int?) -> Unit,
    onArea: (LifeArea?) -> Unit,
    onNote: (String) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onRestart: () -> Unit,
    onClose: () -> Unit
) {
    // Search and family are UI-only, also intentionally not saveable into Android instance state.
    var query by remember { mutableStateOf("") }
    var family by remember { mutableStateOf("Alle") }

    val stage = draft.step
    val heading = when (stage) {
        CheckInStep.ANKOMMEN -> "Im Moment ankommen"
        CheckInStep.BENENNEN -> "Gefühle benennen"
        CheckInStep.NACHSPUEREN -> "Ein wenig nachspüren"
        CheckInStep.AUSKLANG -> "Dein Moment im Überblick"
    }
    val subtitle = when (stage) {
        CheckInStep.ANKOMMEN -> "Du kannst wahrnehmen, was gerade in dir geschieht – ohne etwas erklären zu müssen."
        CheckInStep.BENENNEN -> "Mehrere Gefühle dürfen gleichzeitig da sein. Du kannst auch keines auswählen."
        CheckInStep.NACHSPUEREN -> "Intensitäten, Lebensbereich und Gedanken sind freiwillig."
        CheckInStep.AUSKLANG -> "Das ist eine Momentaufnahme, keine Diagnose oder Bewertung."
    }

    val matches = remember(catalog, stage, query, family) {
        if (stage == CheckInStep.BENENNEN) catalog.search(query, family) else emptyList()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text("DEIN GEFÜHLSNAVIGATOR", style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
                Text("${stage.ordinal + 1} / 4", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.size(10.dp))
            Text(heading, style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            Spacer(Modifier.size(7.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Surface(shape = RoundedCornerShape(15.dp),
                color = MaterialTheme.colorScheme.secondaryContainer) {
                Row(Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Icon(Icons.Outlined.Lock, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary)
                    Text("Nur für diesen Check-in: Beim Beenden werden deine Angaben verworfen.",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        when (stage) {
            CheckInStep.ANKOMMEN -> {
                item {
                    Text("Was bemerkst du gerade?", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    Text("Diese körperlichen Eindrücke sind nur Anregungen. Du musst nichts auswählen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                item {
                    // Two shorter rows provide accessible choices without forcing a grid of tiny chips.
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        BodyCue.entries.chunked(4).forEach { subset ->
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(subset) { cue ->
                                    FilterChip(
                                        selected = cue in draft.bodyCues,
                                        onClick = { onToggleCue(cue) },
                                        label = { Text(cue.label) }
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    Card(colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Ein kleiner Impuls", fontWeight = FontWeight.SemiBold)
                            Text("Du kannst deine Füße auf dem Boden spüren oder einfach kurz innehalten. Auch wenn du nichts bemerkst, ist das in Ordnung.")
                        }
                    }
                }
            }

            CheckInStep.BENENNEN -> {
                item {
                    Text("Gewählte Begriffe: ${draft.selectedCount}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    if (draft.emotions.isNotEmpty()) {
                        Text(draft.emotions.keys.mapNotNull(catalog::find)
                            .joinToString(" · ") { it.name },
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium)
                    } else {
                        Text("Du kannst mit oder ohne Begriffe weitermachen.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
                item {
                    OutlinedTextField(
                        value = query, onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Gefühl oder Synonym suchen") },
                        singleLine = true
                    )
                    Spacer(Modifier.size(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        items(catalog.families) { option ->
                            FilterChip(selected = family == option,
                                onClick = { family = option },
                                label = { Text(option) })
                        }
                    }
                    Text("${matches.size} passende Begriffe",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                items(matches, key = { it.id }) { emotion ->
                    val selected = draft.emotions.containsKey(emotion.id)
                    Card(
                        onClick = { onToggleEmotion(emotion.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected)
                                MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.surface)
                    ) {
                        Row(Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = selected, onCheckedChange = null)
                            Column(Modifier.weight(1f)) {
                                Text(emotion.name, fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.titleSmall)
                                Text(emotion.explanation,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                if (matches.isEmpty()) {
                    item {
                        Text("Kein passender Begriff gefunden. Du kannst anders suchen oder ohne Auswahl weitergehen.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            CheckInStep.NACHSPUEREN -> {
                item {
                    Text("Wie stark sind deine Gefühle gerade?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    Text("Optional von 0 (kaum spürbar) bis 10 (sehr intensiv). Nicht bewertete Gefühle bleiben ohne Zahl.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (draft.emotions.isEmpty()) {
                    item {
                        Text("Du hast keinen Begriff gewählt. Auch ohne Benennung darfst du über diesen Moment nachdenken.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                items(draft.emotions.keys.toList(), key = { "intensity-$it" }) { id ->
                    val emotion = catalog.find(id)
                    if (emotion != null) {
                        val intensity = draft.emotions[id]
                        Card(colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(Modifier.fillMaxWidth().padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(emotion.name, fontWeight = FontWeight.SemiBold)
                                    Text(intensity?.let { "$it / 10" } ?: "Ohne Angabe",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (intensity == null) {
                                    OutlinedButton(onClick = { onIntensity(id, 5) }) {
                                        Text("Intensität einschätzen (optional)")
                                    }
                                } else {
                                    Slider(
                                        value = intensity.toFloat(),
                                        onValueChange = { onIntensity(id, it.roundToInt()) },
                                        valueRange = 0f..10f,
                                        steps = 9
                                    )
                                    TextButton(onClick = { onIntensity(id, null) }) {
                                        Text("Intensität nicht angeben")
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    HorizontalDivider()
                    Text("Was könnte dazugehört haben?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    Text("Wenn du magst, wähle einen Lebensbereich. Es muss keinen klaren Anlass geben.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LifeArea.entries.chunked(3).forEach { subset ->
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(subset) { option ->
                                FilterChip(
                                    selected = draft.area == option,
                                    onClick = {
                                        onArea(if (draft.area == option) null else option)
                                    },
                                    label = { Text(option.label) }
                                )
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = draft.note,
                        onValueChange = onNote,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Was beschäftigt dich? (optional)") },
                        minLines = 3,
                        maxLines = 6,
                        supportingText = {
                            Text("${draft.note.length} von ${CheckInDraft.MAX_NOTE_LENGTH} Zeichen · wird nicht gespeichert")
                        }
                    )
                }
            }

            CheckInStep.AUSKLANG -> {
                item {
                    Text("Das hast du bemerkt",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    if (draft.emotions.isEmpty()) {
                        Text("Du hast dir Raum gegeben, ohne ein bestimmtes Gefühl benennen zu müssen.")
                    } else {
                        Text("${draft.selectedCount} ${if (draft.selectedCount == 1) "Begriff" else "Begriffe"} ausgewählt",
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
                items(draft.emotions.keys.toList(), key = { "summary-$it" }) { id ->
                    val item = catalog.find(id)
                    if (item != null) {
                        Card(colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                            Column(Modifier.fillMaxWidth().padding(15.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(item.name, fontWeight = FontWeight.SemiBold)
                                    val value = draft.emotions[id]
                                    Text(value?.let { "$it / 10" } ?: "Ohne Intensität",
                                        style = MaterialTheme.typography.labelMedium)
                                }
                                Text(item.reflectionQuestion,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                item {
                    if (draft.bodyCues.isNotEmpty()) {
                        Text("Wahrgenommen: ${draft.bodyCues.joinToString(" · ") { it.label }}",
                            style = MaterialTheme.typography.bodyMedium)
                    }
                    if (draft.area != null) {
                        Text("Lebensbereich: ${draft.area.label}",
                            style = MaterialTheme.typography.bodyMedium)
                    }
                    if (draft.note.isNotBlank()) {
                        Text("Deine Gedanken", fontWeight = FontWeight.SemiBold)
                        Surface(shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface) {
                            Text(draft.note, modifier = Modifier.fillMaxWidth().padding(16.dp))
                        }
                    }
                }
                item {
                    Text("Mehrere Gefühle können zugleich wahr sein. Du musst daraus jetzt nichts ableiten oder verändern.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Diese Zusammenfassung wird nicht gespeichert oder automatisch ins Tagebuch übernommen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            HorizontalDivider()
            if (stage == CheckInStep.AUSKLANG) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) {
                        Text("Neuen Check-in beginnen")
                    }
                    OutlinedButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
                        Text("Beenden und Angaben verwerfen")
                    }
                }
            } else {
                Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                    val text = when (stage) {
                        CheckInStep.ANKOMMEN -> "Weiter: Gefühle benennen"
                        CheckInStep.BENENNEN -> "Weiter: Nachspüren"
                        CheckInStep.NACHSPUEREN -> "Zusammenfassung ansehen"
                        CheckInStep.AUSKLANG -> ""
                    }
                    Text(text)
                    Spacer(Modifier.size(6.dp))
                    Icon(Icons.Outlined.ChevronRight, contentDescription = null)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    if (stage == CheckInStep.ANKOMMEN) {
                        TextButton(onClick = onClose) { Text("Beenden und verwerfen") }
                    } else {
                        TextButton(onClick = onPrevious) { Text("Zum vorherigen Schritt") }
                        TextButton(onClick = onClose) { Text("Verwerfen") }
                    }
                }
            }
        }
    }
}
