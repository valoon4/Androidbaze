package com.valoon4.emotionskompass.feature.learning

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.valoon4.emotionskompass.core.lessons.StrategyCaseCatalog

/**
 * Read-only learning scenarios. Answers are ephemeral Compose state only:
 * no stats, scores, profiles, journal access or tracking.
 */
@Composable
fun StrategyPracticeScreen(
    catalog: StrategyCaseCatalog,
    onBackToKnowledge: () -> Unit,
    onStrategies: () -> Unit
) {
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var theme by rememberSaveable { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val selected = selectedId?.let(catalog::find)
    BackHandler(enabled = selected != null) { selectedId = null }
    if (selected != null) {
        // This choice exists only as local in-memory state; it is not saved or exported.
        var answer by remember(selected.id) { mutableStateOf<Int?>(null) }
        val context = LocalContext.current
        var sourceError by remember(selected.id) { mutableStateOf(false) }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                TextButton(onClick = { selectedId = null }) { Text("← Alle Praxisfälle") }
                Text(selected.theme, style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary)
                Text("Eine Alltagssituation", style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold)
                Text(selected.scenario)
                Card {
                    Column(Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(selected.prompt, style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold)
                        Text("Zwei plausible Überlegungen – keine Bewertung deiner Person. Die Einordnung erscheint erst nach deiner Auswahl.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        selected.choices.forEachIndexed { index, choice ->
                            OutlinedButton(
                                onClick = { answer = index },
                                enabled = answer == null,
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(choice) }
                        }
                        OutlinedButton(
                            onClick = { answer = 2 },
                            enabled = answer == null,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Ich bin unsicher – Einordnung ansehen") }
                    }
                }
            }
            if (answer != null) {
                item {
                    Card {
                        Column(Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(9.dp)) {
                            Text("Mögliche Einordnung", style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold)
                            if (answer == 2) {
                                Text("Unsicher zu sein ist in Ordnung. Alltagssituationen sind selten eindeutig.")
                            } else {
                                Text(if (answer == selected.bestIndex)
                                    "Diese Überlegung berücksichtigt wichtige Aspekte der Situation."
                                    else "Die andere Überlegung berücksichtigt hier weitere wichtige Aspekte.")
                            }
                            Text(selected.feedback)
                            Text("Frage zum Nachdenken", fontWeight = FontWeight.SemiBold)
                            Text(selected.reflection)
                            Text("Deine Wahl wird nicht gespeichert. Es gibt keine Punkte oder Pflichtaufgaben.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            TextButton(onClick = { answer = null }) { Text("Andere Überlegung prüfen") }
                        }
                    }
                }
            }
            item {
                OutlinedButton(onClick = onStrategies, modifier = Modifier.fillMaxWidth()) {
                    Text("Die zugehörigen Strategien im Atlas erkunden")
                }
                Text("Wissenschaftliche Ausgangsquellen; die Fälle sind redaktionell erfundene Lernbeispiele und keine individuellen Handlungsanweisungen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                selected.sources.forEach { source ->
                    TextButton(onClick = {
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(source)))
                        } catch (_: Exception) { sourceError = true }
                    }) { Text("Wissenschaftliche Quelle extern öffnen") }
                }
                if (sourceError) Text("Kein Browser verfügbar. Die Übung bleibt vollständig offline lesbar.",
                    color = MaterialTheme.colorScheme.error)
            }
        }
        return
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TextButton(onClick = onBackToKnowledge) { Text("← Zur Wissenswelt") }
            Text("Praxisfälle vergleichen", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            Text("${catalog.cases.size} freiwillige Mini-Fälle. Du kannst jede Situation unabhängig öffnen, überspringen oder abbrechen.",
                style = MaterialTheme.typography.bodyMedium)
            Text("Die Beispiele sind erfunden. Es geht um Zusammenhänge und Sicherheit, nicht um eine Diagnose, Punkte oder eine richtige Antwort auf deine persönliche Situation.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = query, onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                label = { Text("In Praxisfällen suchen") },
                singleLine = true
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item { FilterChip(selected = theme == null, onClick = { theme = null },
                    label = { Text("Alle Themen") }) }
                items(catalog.themes, key = { it }) { category ->
                    FilterChip(selected = theme == category,
                        onClick = { theme = if (theme == category) null else category },
                        label = { Text(category) })
                }
            }
        }
        val results = catalog.browse(query, theme)
        if (results.isEmpty()) item {
            Text("Keine Praxisfälle gefunden.")
            TextButton(onClick = { query = ""; theme = null }) {
                Text("Suche und Filter zurücksetzen")
            }
        }
        items(results, key = { it.id }) { item ->
            Card(onClick = { selectedId = item.id }) {
                Column(Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(item.theme, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary)
                    Text(item.scenario, style = MaterialTheme.typography.bodyMedium)
                    Text("Vergleich ansehen", style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
