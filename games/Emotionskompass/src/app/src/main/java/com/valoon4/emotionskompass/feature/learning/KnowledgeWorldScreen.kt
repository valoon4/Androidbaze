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
import androidx.compose.foundation.ScrollState
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
import androidx.compose.runtime.LaunchedEffect
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
import com.valoon4.emotionskompass.core.lessons.StrategyCaseCatalog
import java.time.LocalDate

/** Independently browsable articles and guided, non-gated learning sequences. */
@Composable
fun KnowledgeWorldScreen(
    catalog: LessonCatalog,
    ui: LearningUiState,
    onBookmark: (String) -> Unit,
    onRead: (String) -> Unit,
    onStrategies: () -> Unit = {},
    onLearning: () -> Unit = {},
    caseCatalog: StrategyCaseCatalog? = null,
    onStrategyDetail: (String, String) -> Unit = { _, _ -> },
    strategyTitles: Map<String, String> = emptyMap(),
    requestedCaseId: String? = null,
    onCaseRequestConsumed: () -> Unit = {},
    onReturnToSource: (() -> Unit)? = null,
    returnLabel: String = "Zur Strategie",
    requestedArticleId: String? = null,
    onRequestConsumed: () -> Unit = {}
) {
    var mode by rememberSaveable { mutableStateOf("Start") }
    var query by rememberSaveable { mutableStateOf("") }
    var topic by rememberSaveable { mutableStateOf("Alle") }
    var seriesId by rememberSaveable { mutableStateOf<String?>(null) }
    var articleId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingCaseId by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(requestedCaseId) {
        if (requestedCaseId != null && caseCatalog?.find(requestedCaseId) != null) {
            mode = "Praxisfälle"
            articleId = null
            seriesId = null
            pendingCaseId = requestedCaseId
            onCaseRequestConsumed()
        }
    }
    val series = catalog.series.firstOrNull { it.id == seriesId }
    val article = articleId?.let(catalog::find)
    LaunchedEffect(requestedArticleId) {
        if (requestedArticleId != null && catalog.find(requestedArticleId) != null) {
            articleId = requestedArticleId
            seriesId = null
            onRequestConsumed()
        }
    }
    val canSave = !ui.busy && ui.error == null
    val progress = ui.progress

    BackHandler(enabled = article != null || series != null) {
        when {
            article != null && onReturnToSource != null -> onReturnToSource()
            article != null -> articleId = null
            else -> seriesId = null
        }
    }
    if (article != null) {
        val position = series?.lessonIds?.indexOf(article.id) ?: -1
        val next = if (position >= 0) series?.lessonIds?.getOrNull(position + 1) else null
        val context = LocalContext.current
        val articleScroll = remember(article.id) { ScrollState(0) }
        var picked by remember(article.id) { mutableStateOf<Int?>(null) }
        var externalError by remember(article.id) { mutableStateOf(false) }
        Column(Modifier.fillMaxSize().verticalScroll(articleScroll).padding(19.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            TextButton(onClick = {
                if (onReturnToSource != null) onReturnToSource() else articleId = null
            }) {
                Text(if (onReturnToSource != null) "← $returnLabel"
                    else if (series != null) "← Zur Lernreihe" else "← Zur Bibliothek")
            }
            Text(article.topic.label.uppercase(), style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary)
            Text(article.title, style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            Text("Etwa " + article.readingMinutes + " Min. Lesezeit",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(article.lead, style = MaterialTheme.typography.bodyLarge)
            Card {
                Column(Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("DAS LERNST DU", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary)
                    Text(article.learningGoal)
                }
            }
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
                    Text("Häufiges Missverständnis", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold)
                    Text(article.commonMistake)
                    Text("Was daran zu kurz greift: " + article.takeaway,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
            Card {
                Column(Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Kurz verstanden?", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    Text("Eine freiwillige Verständnisfrage, keine Bewertung deiner eigenen Gefühle.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(article.check.prompt)
                    article.check.choices.forEachIndexed { index, choice ->
                        OutlinedButton(onClick = { picked = index },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = picked == null) { Text(choice) }
                    }
                    if (picked != null) {
                        Text(if (picked == article.check.correctIndex)
                            "Das passt zur Lektion." else "Die andere Einordnung passt hier besser.",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold)
                        Text(article.check.explanation)
                        TextButton(onClick = { picked = null }) { Text("Erneut ansehen") }
                    }
                }
            }
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
                        externalError = true
                    }
                }) { Text("${index + 1}. " + sourceLabel(url)) }
            }
            if (externalError) Text("Kein externer Browser verfügbar. Du kannst den Artikel weiterhin offline lesen.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall)
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
                Text("Lernziel: " + entries.joinToString(" ") { it.learningGoal }.take(220) + "…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                val next = catalog.continueAt(series, progress.readArticles)
                if (next != null) {
                    Button(onClick = { articleId = next.id },
                        modifier = Modifier.fillMaxWidth()) {
                        Text("Weiterlernen: " + next.title)
                    }
                } else {
                    Text("✓ Alle Kapitel freiwillig als gelesen markiert.",
                        color = MaterialTheme.colorScheme.primary)
                }
            }
            items(entries, key = { it.id }) { item ->
                ArticleTile(item, item.id in progress.articleBookmarks,
                    item.id in progress.readArticles) { articleId = item.id }
            }
        }
        return
    }

    if (mode == "Praxisfälle" && caseCatalog != null) {
        StrategyPracticeScreen(
            catalog = caseCatalog,
            onBackToKnowledge = { mode = "Start" },
            onStrategy = onStrategyDetail,
            strategyTitles = strategyTitles,
            requestedCaseId = pendingCaseId,
            onRequestConsumed = { pendingCaseId = null },
            onReturnToSource = onReturnToSource
        )
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
            Text("${catalog.lessons.size} frei lesbare Artikel und ${catalog.series.size} geführte Lernreihen. Alles offline und ohne Pflichttermine.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(listOf("Start", "Artikel") +
                    (if (caseCatalog != null) listOf("Praxisfälle") else emptyList()) +
                    listOf("Lernreihen", "Merkliste")) { option ->
                    FilterChip(selected = mode == option, onClick = { mode = option },
                        label = { Text(option) })
                }
            }
        }
        when (mode) {
            "Start" -> {
                item {
                    Text("Dein Wissensimpuls heute", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold)
                    ArticleTile(today, today.id in progress.articleBookmarks,
                        today.id in progress.readArticles) { articleId = today.id }
                    Text("Heute nur ein kleiner Impuls. Es gibt keine Lese-Pflicht oder Streak.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                item {
                    Text("Hier weiterlesen", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    val path = catalog.continueSeries(progress.readArticles)
                    val chapter = path?.let { catalog.continueAt(it, progress.readArticles) }
                    if (path != null && chapter != null) {
                        Card(onClick = { seriesId = path.id; articleId = chapter.id }) {
                            Column(Modifier.fillMaxWidth().padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                Text(path.title, fontWeight = FontWeight.SemiBold)
                                Text(chapter.title)
                                Text(path.lessonIds.count { it in progress.readArticles }.toString() +
                                    " von " + path.lessonIds.size + " Kapiteln gelesen",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary)
                                Text("Nächstes Kapitel öffnen",
                                    style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    } else {
                        Text("Alle Lernreihen sind abgeschlossen. Du kannst jeden Artikel erneut lesen.",
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
                item {
                    OutlinedButton(onClick = onStrategies,
                        modifier = Modifier.fillMaxWidth()) {
                        Text("Strategieatlas: Möglichkeiten und Grenzen entdecken")
                    }
                    if (caseCatalog != null) OutlinedButton(
                        onClick = { mode = "Praxisfälle" },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Praxisfälle vergleichen – ${caseCatalog.cases.size} Beispiele") }
                    OutlinedButton(onClick = onLearning,
                        modifier = Modifier.fillMaxWidth()) {
                        Text("Übungen, Wortkarten und Wiederholen")
                    }
                    OutlinedButton(onClick = { mode = "Artikel" },
                        modifier = Modifier.fillMaxWidth()) {
                        Text("Alle Artikel frei entdecken")
                    }
                    OutlinedButton(onClick = { mode = "Lernreihen" },
                        modifier = Modifier.fillMaxWidth()) {
                        Text("Geführte Lernreihen ansehen")
                    }
                }
            }
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
                    Text("Alle Kapitel sind unabhängig lesbar. Du kannst die Reihenfolge selbst bestimmen oder überspringen.",
                        style = MaterialTheme.typography.bodySmall)
                }
                items(catalog.series, key = { it.id }) { path ->
                    Card(onClick = { seriesId = path.id }) {
                        Column(Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Text(path.title, style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold)
                            Text(path.description)
                            val next = catalog.continueAt(path, progress.readArticles)
                            Text(if (next != null) "Als Nächstes: " + next.title else "Alle Kapitel gelesen",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            Text(if (article.id.startsWith("s-vertiefung-"))
                "Vertiefung · " + article.topic.label else article.topic.label,
                style = MaterialTheme.typography.labelSmall,
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

/**
 * Clearly names external sources and distinguishes scientific background from
 * communication practice. URLs are fixed and bundled; nothing is fetched here.
 */
private fun sourceLabel(url: String): String = when (url) {
    "https://pubmed.ncbi.nlm.nih.gov/40816203/" ->
        "Wissenschaft: Emotional granularity – Scoping Review (PubMed)"
    "https://pubmed.ncbi.nlm.nih.gov/31961170/" ->
        "Wissenschaft: Emotion regulation – Übersicht (PubMed)"
    "https://pubmed.ncbi.nlm.nih.gov/39423429/" ->
        "Wissenschaft: Interozeption und Emotionen (PubMed)"
    "https://pubmed.ncbi.nlm.nih.gov/33749936/" ->
        "Wissenschaft: Selbstmitgefühl – Meta-Analyse (PubMed)"
    "https://nonviolentcommunication.com/learn-nonviolent-communication/feelings/" ->
        "Praxisansatz: Gefühle und Bedürfnisse – GFK-Material"
    "https://baynvc.org/nonviolent-communication-quick-reference-guide/" ->
        "Praxisansatz: GFK-Kurzübersicht – BayNVC"
    "https://www.cnvc.org/certification/cpp/preparation/" ->
        "Praxisansatz: GFK-Lernmaterial – CNVC"
    "https://pubmed.ncbi.nlm.nih.gov/26173226/" ->
        "Wissenschaft: Bonanno & Burton – Regulationsflexibilität (2013)"
    "https://pubmed.ncbi.nlm.nih.gov/12696840/" ->
        "Wissenschaft: Skinner et al. – Coping-Klassifikationen (2003)"
    "https://pubmed.ncbi.nlm.nih.gov/24145331/" ->
        "Wissenschaft: Troy et al. – Neubewertung und Kontext (2013)"
    "https://doi.org/10.1080/1047840X.2014.940781" ->
        "Wissenschaft: Gross – Emotionsregulation (2015)"
    "https://www.paho.org/en/documents/doing-what-matters-times-stress-illustrated-guide" ->
        "Praxisleitfaden: WHO/PAHO – Umgang mit Stress"
    else -> "Externe Quelle auf " + (Uri.parse(url).host ?: "unbekannter Website")
}
