package com.valoon4.emotionskompass.feature.needs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.valoon4.emotionskompass.core.model.NeedCatalog
import com.valoon4.emotionskompass.core.model.NeedLens

/**
 * A temporary, entirely user-led exploration. No saveable state, network,
 * data store, or psychological inference; selected lenses are not journal entries.
 */
@Composable
fun NeedCompassScreen(catalog: NeedCatalog, onNeed: (String) -> Unit) {
    var activeLens by remember { mutableStateOf(NeedLens.WICHTIG) }
    var family by remember { mutableStateOf("Alle") }
    var query by remember { mutableStateOf("") }
    var showAll by remember { mutableStateOf(false) }
    val selected = remember { mutableStateMapOf<String, NeedLens>() }
    val initialIds = remember {
        listOf("ruhe", "sicherheit", "naehe", "selbstbestimmung", "respekt",
            "klarheit", "unterstuetzung", "freude")
    }
    val matches = remember(catalog, family, query, showAll) {
        if (query.isNotBlank() || showAll || family != "Alle") {
            catalog.browse(query, family)
        } else initialIds.mapNotNull(catalog::find)
    }

    LazyColumn(Modifier.fillMaxSize(),
        contentPadding = PaddingValues(19.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Dein Bedürfniskompass", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)
            Text("Ohne Test, Diagnose oder Punktzahl. Du entscheidest selbst, was dir wichtig erscheint oder bereits gut erfüllt wird.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Text("Mit welcher Frage möchtest du starten?", fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                items(NeedLens.entries) { lens ->
                    FilterChip(selected = activeLens == lens,
                        onClick = { activeLens = lens },
                        label = { Text(lens.label) })
                }
            }
            Text("Tippe anschließend auf passende Bedürfnisse. Du kannst jedem Bedürfnis eine andere Einschätzung geben.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (selected.isNotEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(Modifier.fillMaxWidth().padding(15.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Deine Momentaufnahme · ${selected.size}",
                            fontWeight = FontWeight.SemiBold)
                        selected.toMap().forEach { (id, lens) ->
                            val need = catalog.find(id)
                            if (need != null) {
                                Row(Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column(Modifier.weight(1f)) {
                                        Text(need.name, fontWeight = FontWeight.SemiBold)
                                        Text(lens.label, style = MaterialTheme.typography.labelSmall)
                                        Text(need.reflectionQuestion,
                                            style = MaterialTheme.typography.bodySmall)
                                    }
                                    TextButton(onClick = { selected.remove(id) }) {
                                        Text("Entfernen")
                                    }
                                }
                            }
                        }
                        Text("Dies ist nur eine temporäre Übersicht. Beim Verlassen wird nichts gespeichert.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Nach einem Bedürfnis suchen") }
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                items(catalog.families) { option ->
                    FilterChip(selected = family == option, onClick = { family = option },
                        label = { Text(option) })
                }
            }
            if (family == "Alle" && query.isBlank()) {
                TextButton(onClick = { showAll = !showAll }) {
                    Text(if (showAll) "Wieder Einstiegsbegriffe" else "Alle ${catalog.needs.size} Begriffe ansehen")
                }
            }
        }
        item {
            Text("${matches.size} Begriffe zur Auswahl",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (matches.isEmpty()) Text("Keine Treffer – andere Begriffe oder eine andere Familie versuchen.")
        }
        items(matches, key = { it.id }) { need ->
            val current = selected[need.id]
            Card(
                onClick = {
                    if (current == activeLens) selected.remove(need.id)
                    else selected[need.id] = activeLens
                },
                colors = CardDefaults.cardColors(
                    containerColor = if (current != null)
                        MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.fillMaxWidth().padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(need.name, fontWeight = FontWeight.SemiBold)
                    Text(need.definition, style = MaterialTheme.typography.bodySmall)
                    if (current != null) {
                        Text("Deine Auswahl: ${current.label}",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium)
                    }
                    TextButton(onClick = { onNeed(need.id) }) {
                        Text("Erklärung und Beispiele lesen")
                    }
                }
            }
        }
        item {
            if (selected.isNotEmpty()) {
                Button(onClick = { selected.clear() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Momentaufnahme verwerfen")
                }
            }
            NeedDisclaimer()
        }
    }
}
