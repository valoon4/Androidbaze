package com.valoon4.emotionskompass.feature.strategies

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
import androidx.compose.material3.HorizontalDivider
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
import com.valoon4.emotionskompass.core.strategies.StrategyCard
import com.valoon4.emotionskompass.core.strategies.StrategyCatalog

/** Read-only offline strategy atlas; user experiences are never inferred. */
@Composable
fun StrategyAtlasScreen(
    catalog: StrategyCatalog,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onBackToList: () -> Unit,
    onKnowledgeArticle: (String) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var familyId by rememberSaveable { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val selected = selectedId?.let(catalog::find)
    BackHandler(enabled = selected != null) { onBackToList() }
    if (selected != null) {
        StrategyDetail(selected, catalog, onBackToList, onSelect, onKnowledgeArticle)
        return
    }
    val filtered = catalog.browse(query, familyId)
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Strategien entdecken", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            Text("Fünf ausführliche Referenzkarten. Der Atlas wird schrittweise erweitert.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(catalog.intro, modifier = Modifier.padding(top = 8.dp))
            Text("Redaktionelle Entwürfe – unabhängige fachliche Prüfung steht noch aus.",
                modifier = Modifier.padding(top = 6.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall)
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Strategien suchen") },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                singleLine = true
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 8.dp)) {
                item {
                    FilterChip(selected = familyId == null, onClick = { familyId = null },
                        label = { Text("Alle Themen") })
                }
                items(catalog.families, key = { it.id }) { family ->
                    FilterChip(
                        selected = familyId == family.id,
                        onClick = { familyId = if (familyId == family.id) null else family.id },
                        label = { Text(family.title) }
                    )
                }
            }
            Text("${filtered.size} von ${catalog.strategies.size} Strategiekarten",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 8.dp))
        }
        if (filtered.isEmpty()) {
            item {
                Text("Hier gibt es noch keinen Treffer. Versuche eine andere Suche oder wähle „Alle Themen“.")
                TextButton(onClick = { query = ""; familyId = null }) {
                    Text("Suche und Filter zurücksetzen")
                }
            }
        }
        items(filtered, key = { it.id }) { strategy ->
            Card(onClick = { onSelect(strategy.id) }) {
                Column(Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(strategy.title, style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    Text(strategy.familyIds.joinToString(" · ") { catalog.familyName(it) },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary)
                    Text(strategy.shortDefinition, style = MaterialTheme.typography.bodyMedium)
                    Text("Nutzen, Grenzen und Beispiele ansehen",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun StrategyDetail(
    card: StrategyCard,
    catalog: StrategyCatalog,
    onBack: () -> Unit,
    onRelated: (String) -> Unit,
    onKnowledgeArticle: (String) -> Unit
) {
    val context = LocalContext.current
    var externalError by remember(card.id) { mutableStateOf(false) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        item {
            TextButton(onClick = onBack) { Text("← Zum Strategieatlas") }
            Text(card.familyIds.joinToString(" · ") { catalog.familyName(it) },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary)
            Text(card.title, style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            Text(card.shortDefinition, style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp))
        }
        item { DetailSection("Wozu könnte das dienen?", card.purpose) }
        item { DetailSection("Wann könnte es passen?", card.whenUseful) }
        item {
            SectionHeading("Mögliche Vorteile")
            card.benefits.forEach { Text("• $it", modifier = Modifier.padding(top = 6.dp)) }
        }
        item {
            SectionHeading("Mögliche Nachteile und Grenzen")
            card.drawbacks.forEach { Text("• $it", modifier = Modifier.padding(top = 6.dp)) }
            Text(card.limitations, modifier = Modifier.padding(top = 8.dp))
        }
        item {
            Card {
                Column(Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeading("Beispiel aus dem Alltag")
                    Text(card.example)
                }
            }
        }
        item {
            DetailSection("Häufiges Missverständnis", card.misconception)
            Text(card.clarification, modifier = Modifier.padding(top = 8.dp))
        }
        item {
            Card {
                Column(Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeading("Eine freiwillige Frage")
                    Text(card.reflection)
                    Text("Deine Antwort wird hier nicht erfasst oder gespeichert.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        if (card.optionalPractice.isNotBlank()) item {
            DetailSection("Kleine freiwillige Anregung", card.optionalPractice)
        }
        item {
            HorizontalDivider()
            SectionHeading("Weitere Möglichkeiten")
        }
        items(catalog.alternatives(card), key = { "alt-${it.id}" }) { alternative ->
            OutlinedButton(onClick = { onRelated(alternative.id) },
                modifier = Modifier.fillMaxWidth()) { Text(alternative.title) }
        }
        if (card.relatedLessonIds.isNotEmpty()) {
            item { SectionHeading("Passende Artikel in der Wissenswelt") }
            items(card.relatedLessonIds, key = { "lesson-$it" }) { lessonId ->
                OutlinedButton(onClick = { onKnowledgeArticle(lessonId) },
                    modifier = Modifier.fillMaxWidth()) {
                    Text(when (lessonId) {
                        "strategien-pruefen" -> "Was tun, wenn eine Strategie nicht hilft?"
                        "mehrere-wege" -> "Mehr als einen Weg ausprobieren"
                        "beobachtung-interpretation" -> "Beobachtung und Interpretation trennen"
                        "gedanken-und-gefuehle" -> "Gedanken sind nicht dasselbe wie Gefühle"
                        "achtsam-ohne-druck" -> "Achtsamkeit ohne Leistungsdruck"
                        "bitte-statt-forderung" -> "Eine Bitte formulieren, die ein Nein zulässt"
                        else -> "Artikel ansehen"
                    })
                }
            }
        }
        item {
            HorizontalDivider()
            SectionHeading("Quellen und Einordnung")
            Text("Die Quellen stützen unterschiedliche Aspekte. Sie beweisen keine garantierte Wirkung in deinem Einzelfall. Externe Seiten öffnen sich nur nach deiner Auswahl.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(card.sources, key = { it.url }) { source ->
            Text(source.description)
            Text("Quellentyp: ${when (source.evidenceType) {
                "PRIMARY_STUDY" -> "Einzelstudie"
                "OBSERVATIONAL_STUDY" -> "Beobachtungsstudie"
                "SYSTEMATIC_REVIEW" -> "Systematische Übersicht"
                "THEORY_REVIEW" -> "Theoretischer Überblick"
                else -> "Praxisleitfaden"
            }}", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = {
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(source.url)))
                } catch (_: Exception) {
                    externalError = true
                }
            }) { Text("Quelle extern öffnen") }
        }
        if (externalError) item {
            Text("Kein Browser verfügbar. Der Strategieartikel bleibt offline lesbar.",
                color = MaterialTheme.colorScheme.error)
        }
        item {
            Text("Lernmaterial, keine Diagnose oder individuelle Therapieempfehlung.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SectionHeading(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold)
}

@Composable
private fun DetailSection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SectionHeading(title)
        Text(body, style = MaterialTheme.typography.bodyLarge)
    }
}
