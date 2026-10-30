package com.valoon4.emotionskompass.feature.learning

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
import com.valoon4.emotionskompass.core.learning.LearningUiState
import com.valoon4.emotionskompass.core.lessons.KnowledgeLesson
import com.valoon4.emotionskompass.core.lessons.LessonCatalog
import com.valoon4.emotionskompass.core.lessons.LessonTopic
import java.time.LocalDate

/** Independently browsable articles and guided, non-gated learning sequences. */
@Composable
fun KnowledgeWorldScreen(
    catalog: LessonCatalog,
    ui: LearningUiState,
    onBookmark: (String) -> Unit,
    onRead: (String) -> Unit
) {
    var mode by rememberSaveable { mutableStateOf("Artikel") }
    var query by rememberSaveable { mutableStateOf("") }
    var topic by rememberSaveable { mutableStateOf("Alle") }
    var seriesId by rememberSaveable { mutableStateOf<String?>(null) }
    var articleId by rememberSaveable { mutableStateOf<String?>(null) }
    val series = catalog.series.firstOrNull { it.id == seriesId }
    val article = articleId?.let(catalog::find)
    val canSave = !ui.busy && ui.error == null
    val progress = ui.progress

    BackHandler(enabled = article != null || series != null) {
        if (article != null) articleId = null else seriesId = null
    }
    if (article != null) {
        val position = series?.lessonIds?.indexOf(article.id) ?: -1
        val next = if (position >= 0) series?.lessonIds?.getOrNull(position + 1) else null
        val context = LocalContext.current
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(19.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            TextButton(onClick = { articleId = null }) {
                Text(if (series != null) "← Zur Lernreihe" else "← Zur Bibliothek")
            }
            Text(article.topic.label.uppercase(), style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary)
            Text(article.title, style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            Text("Etwa " + article.readingMinutes + " Min. Lesezeit",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(article.lead, style = MaterialTheme.typography.bodyLarge)
            OutlinedButton(onClick = { onBookmark(article.id) }, enabled = canSave) {
                Text(if (article.id in progress.articleBookmarks) "★ Gemerkt" else "☆ Artikel merken")
            }
            article.sections.forEach { section ->
                HorizontalDivider()
                Text(section.title, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Text(section.text, style = MaterialTheme.typography.bodyLarge)
            }
            Card {
                Column(Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Beispiel aus dem Alltag", fontWeight = FontWeight.SemiBold)
                    Text(article.example)
                }
            }
            Text("Das Wichtigste", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Text(article.takeaway)
            Card {
                Column(Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Eine Frage zum Nachdenken", fontWeight = FontWeight.SemiBold)
                    Text(article.reflection)
                    Text("Die Antwort ist freiwillig und wird nicht gespeichert.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Button(onClick = { onRead(article.id) },
                enabled = canSave && article.id !in progress.readArticles,
                modifier = Modifier.fillMaxWidth()) {
                Text(if (article.id in progress.readArticles)
                    "✓ Bereits als gelesen markiert" else "Als gelesen markieren")
            }
            if (next != null) OutlinedButton(
                onClick = { articleId = next }, modifier = Modifier.fillMaxWidth()
            ) { Text("Nächste Lektion der Reihe") }
            HorizontalDivider()
            Text("Quellen & Vertiefung", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Text("Redaktionelle Lerntexte, keine medizinische Diagnose oder Therapie. Die folgenden Quellen öffnen sich nur auf deinen Wunsch im externen Browser.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            article.sources.forEachIndexed { index, url ->
                TextButton(onClick = {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (_: Exception) {
                        // No browser installed: the offline reading view remains usable.
                    }
                }) { Text("Quelle " + (index + 1) + " im Browser öffnen") }
            }
        }
        return
    }

    if (series != null) {
        val entries = catalog.within(series)
        LazyColumn(contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                TextButton(onClick = { seriesId = null }) { Text("← Alle Lernreihen") }
                Text(series.title, style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold)
                Text(series.description)
                Text(entries.count { it.id in progress.readArticles }.toString() +
                    " von " + entries.size + " Lektionen freiwillig gelesen",
                    color = MaterialTheme.colorScheme.primary)
                Text("Alle Kapitel sind frei zugänglich und lassen sich überspringen.",
                    style = MaterialTheme.typography.bodySmall)
            }
            items(entries, key = { it.id }) { item ->
                ArticleTile(item, item.id in progress.articleBookmarks,
                    item.id in progress.readArticles) { articleId = item.id }
            }
        }
        return
    }

    val results = catalog.browse(query,
        LessonTopic.entries.firstOrNull { it.label == topic })
    val today = catalog.daily(LocalDate.now())
    LazyColumn(contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Wissenswelt", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            Text("20 frei lesbare Artikel und vier geführte Lernreihen. Alles offline und ohne Pflichttermine.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(listOf("Artikel", "Lernreihen", "Merkliste")) { option ->
                    FilterChip(selected = mode == option, onClick = { mode = option },
                        label = { Text(option) })
                }
            }
        }
        when (mode) {
            "Artikel" -> {
                item {
                    Text("Artikel des Tages", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    ArticleTile(today, today.id in progress.articleBookmarks,
                        today.id in progress.readArticles) { articleId = today.id }
                    Text("Die Empfehlung wechselt täglich. Alle Artikel bleiben jederzeit verfügbar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                    Text("Alle Artikel", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(value = query, onValueChange = { query = it },
                        label = { Text("In Artikeln suchen") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf("Alle") + LessonTopic.entries.map { it.label }) { option ->
                            FilterChip(selected = topic == option, onClick = { topic = option },
                                label = { Text(option) })
                        }
                    }
                    Text(results.size.toString() + " Artikel",
                        style = MaterialTheme.typography.labelSmall)
                }
                if (results.isEmpty()) item { Text("Keine Treffer. Filter oder Suche zurücksetzen.") }
                items(results, key = { it.id }) { item ->
                    ArticleTile(item, item.id in progress.articleBookmarks,
                        item.id in progress.readArticles) { articleId = item.id }
                }
            }
            "Lernreihen" -> {
                item {
                    Text("Geführt lernen", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold)
                    Text("Je fünf aufeinander aufbauende Artikel. Du kannst jeden frei öffnen oder in Reihenfolge lesen.",
                        style = MaterialTheme.typography.bodySmall)
                }
                items(catalog.series, key = { it.id }) { path ->
                    Card(onClick = { seriesId = path.id }) {
                        Column(Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Text(path.title, style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold)
                            Text(path.description)
                            Text(path.lessonIds.count { it in progress.readArticles }.toString() +
                                " / " + path.lessonIds.size + " freiwillig gelesen",
                                color = MaterialTheme.colorScheme.primary)
                            Text("Lernreihe öffnen", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            else -> {
                val bookmarks = catalog.lessons.filter { it.id in progress.articleBookmarks }
                item {
                    Text("Meine gemerkten Artikel", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold)
                    if (bookmarks.isEmpty()) Text("Noch keine Artikel gemerkt. Öffne einen Artikel und wähle „Artikel merken“.")
                }
                items(bookmarks, key = { it.id }) { item ->
                    ArticleTile(item, true, item.id in progress.readArticles) {
                        articleId = item.id
                    }
                }
            }
        }
    }
}

@Composable
private fun ArticleTile(
    article: KnowledgeLesson,
    bookmarked: Boolean,
    read: Boolean,
    onOpen: () -> Unit
) {
    Card(onClick = onOpen) {
        Column(Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(article.topic.label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary)
            Text(article.title, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Text(article.lead, style = MaterialTheme.typography.bodySmall)
            Text(article.readingMinutes.toString() + " Min. · " +
                (if (read) "✓ Gelesen" else "Noch nicht markiert") +
                (if (bookmarked) " · ★ Gemerkt" else ""),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary)
        }
    }
}
