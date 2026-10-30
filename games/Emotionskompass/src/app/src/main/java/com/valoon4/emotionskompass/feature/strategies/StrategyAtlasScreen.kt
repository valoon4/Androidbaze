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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.valoon4.emotionskompass.core.strategies.StrategyLearningIndex
import com.valoon4.emotionskompass.core.model.NeedCatalog
import com.valoon4.emotionskompass.core.lessons.LessonCatalog
import com.valoon4.emotionskompass.core.lessons.StrategyCaseCatalog

/** Read-only offline strategy atlas; user experiences are never inferred. */
@Composable
fun StrategyAtlasScreen(
    catalog: StrategyCatalog,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onBackToList: () -> Unit,
    onKnowledgeArticle: (String) -> Unit,
    needCatalog: NeedCatalog? = null,
    lessonCatalog: LessonCatalog? = null,
    caseCatalog: StrategyCaseCatalog? = null,
    onNeed: (String) -> Unit = {},
    onPracticeCase: (String) -> Unit = {},
    onReturnToSource: (() -> Unit)? = null,
    sourceLabel: String = "",
    hasPreviousStrategy: Boolean = false
) {
    var query by rememberSaveable { mutableStateOf("") }
    var familyId by rememberSaveable { mutableStateOf<String?>(null) }
    var showExplanation by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val selected = selectedId?.let(catalog::find)
    val returnFromDetail: () -> Unit = {
        if (hasPreviousStrategy || onReturnToSource == null) onBackToList()
        else onReturnToSource()
    }
    BackHandler(enabled = selected != null) { returnFromDetail() }
    if (selected != null) {
        StrategyDetail(
            card = selected,
            catalog = catalog,
            onBack = returnFromDetail,
            onRelated = onSelect,
            onKnowledgeArticle = onKnowledgeArticle,
            lessonCatalog = lessonCatalog,
            needCatalog = needCatalog,
            caseCatalog = caseCatalog,
            onNeed = onNeed,
            onPracticeCase = onPracticeCase,
            backLabel = if (hasPreviousStrategy) "Zur vorherigen Strategie"
                else if (onReturnToSource != null) sourceLabel else "Zum Strategieatlas"
        )
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
            Text("${catalog.strategies.size} ausführliche Strategiekarten zu Coping, Emotionsregulation und Bedürfnissen.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Eine Strategie ist eine mögliche Vorgehensweise, kein Patentrezept. Was passt, hängt von der Situation, Sicherheit und deinen Möglichkeiten ab.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp))
            TextButton(onClick = { onKnowledgeArticle("s-vertiefung-begriffe") },
                enabled = lessonCatalog?.find("s-vertiefung-begriffe") != null) {
                Text("Strategie, Bedürfnis und Coping: kurz erklärt")
            }
            TextButton(onClick = { showExplanation = !showExplanation }) {
                Text(if (showExplanation) "Einordnung einklappen" else "Mehr zur Einordnung")
            }
            if (showExplanation) Text(catalog.intro, style = MaterialTheme.typography.bodySmall)
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
    onKnowledgeArticle: (String) -> Unit,
    lessonCatalog: LessonCatalog?,
    needCatalog: NeedCatalog?,
    caseCatalog: StrategyCaseCatalog?,
    onNeed: (String) -> Unit,
    onPracticeCase: (String) -> Unit,
    backLabel: String
) {
    val context = LocalContext.current
    var externalError by remember(card.id) { mutableStateOf(false) }
    // Each card starts at its beginning; saved position survives a temporary article detour.
    val detailScroll = rememberSaveable(card.id, saver = LazyListState.Saver) { LazyListState() }
    LazyColumn(
        state = detailScroll,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        item {
            TextButton(onClick = onBack) { Text("← $backLabel") }
            Text(card.familyIds.joinToString(" · ") { catalog.familyName(it) },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary)
            Text(card.title, style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            Text(card.shortDefinition, style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp))
        }
        item {
            Card {
                Column(Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    SectionHeading("Auf einen Blick")
                    Text("Wann es passen könnte", fontWeight = FontWeight.SemiBold)
                    Text(card.whenUseful)
                    Text("Wichtige Grenze", fontWeight = FontWeight.SemiBold)
                    Text(card.limitations)
                }
            }
        }
        item { DetailSection("Wozu könnte das dienen?", card.purpose) }
        item {
            SectionHeading("Mögliche Vorteile")
            card.benefits.forEach { Text("• $it", modifier = Modifier.padding(top = 6.dp)) }
        }
        item {
            SectionHeading("Mögliche Nachteile")
            card.drawbacks.forEach { Text("• $it", modifier = Modifier.padding(top = 6.dp)) }
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
        // These links are explicit editorial associations, not inferred personal advice.
        val needs = card.relatedNeedIds.mapNotNull { id -> needCatalog?.find(id) }
        if (needs.isNotEmpty()) {
            item {
                SectionHeading("Verwandte Bedürfnisse")
                Text("Was dir wichtig ist und wie du darauf reagierst, sind verschiedene Fragen. Diese Links sind keine automatische Zuordnung.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(needs, key = { "need-${it.id}" }) { need ->
                OutlinedButton(onClick = { onNeed(need.id) },
                    modifier = Modifier.fillMaxWidth()) { Text(need.name + " im Bedürfnisatlas ansehen") }
            }
        }
        val articles = lessonCatalog?.let { StrategyLearningIndex.articlesFor(card, it) }.orEmpty()
        if (articles.isNotEmpty()) {
            item { SectionHeading("Passende Vertiefungen in der Wissenswelt") }
            items(articles, key = { "lesson-${it.id}" }) { lesson ->
                OutlinedButton(onClick = { onKnowledgeArticle(lesson.id) },
                    modifier = Modifier.fillMaxWidth()) { Text(lesson.title) }
            }
        }
        val examples = caseCatalog?.forStrategy(card.id)?.take(2).orEmpty()
        if (examples.isNotEmpty()) {
            item {
                SectionHeading("An Alltagssituationen vergleichen")
                Text("Die Fallbeispiele stellen Möglichkeiten gegenüber – sie messen nicht, ob eine Strategie für dich funktioniert.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(examples, key = { "case-${it.id}" }) { example ->
                OutlinedButton(onClick = { onPracticeCase(example.id) },
                    modifier = Modifier.fillMaxWidth()) {
                    Text(example.theme + ": " + example.prompt)
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
        itemsIndexed(card.sources, key = { _, source -> source.url }) { index, source ->
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
            }) { Text("Quelle ${index + 1} von ${card.sources.size} extern öffnen") }
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
