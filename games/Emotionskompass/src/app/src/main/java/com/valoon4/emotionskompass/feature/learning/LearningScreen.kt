package com.valoon4.emotionskompass.feature.learning

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.valoon4.emotionskompass.core.learning.DailyWords
import com.valoon4.emotionskompass.core.learning.LearningEngine
import com.valoon4.emotionskompass.core.learning.LearningKind
import com.valoon4.emotionskompass.core.learning.LearningQuiz
import com.valoon4.emotionskompass.core.learning.LearningRoute
import com.valoon4.emotionskompass.core.learning.LearningUiState
import com.valoon4.emotionskompass.core.learning.LearningWord
import com.valoon4.emotionskompass.core.model.EmotionCatalog
import com.valoon4.emotionskompass.core.model.NeedCatalog
import java.time.LocalDate

/** All six learning features are optional; there are no streaks, scores or pressure. */
@Composable
fun LearningScreen(
    emotions: EmotionCatalog,
    needs: NeedCatalog,
    ui: LearningUiState,
    onFavorite: (String) -> Unit,
    onSeen: (String) -> Unit,
    onReview: (String) -> Unit,
    onStation: (String) -> Unit,
    onReset: () -> Unit,
    onReload: () -> Unit,
    onOpenEmotion: (String) -> Unit
) {
    val words = remember(emotions, needs) { LearningEngine.allWords(emotions, needs) }
    val routes = remember(emotions, needs) { LearningEngine.routes(emotions, needs) }
    val quizzes = remember(emotions, needs) { LearningEngine.quizzes(emotions, needs) }
    var tab by rememberSaveable { mutableStateOf("Heute") }
    var selectedWord by remember { mutableStateOf<LearningWord?>(null) }
    var selectedRoute by remember { mutableStateOf<LearningRoute?>(null) }
    var resetConfirm by remember { mutableStateOf(false) }
    val progress = ui.progress
    val canChange = !ui.busy && ui.error == null

    if (resetConfirm) {
        AlertDialog(
            onDismissRequest = { resetConfirm = false },
            title = { Text("Lernstand zurücksetzen?") },
            text = { Text("Alle Lernfavoriten, entdeckten Begriffe, Wiederholungen und Lernpfad-Markierungen werden gelöscht. Dein Tagebuch und die Atlanten bleiben unverändert.") },
            confirmButton = {
                TextButton(onClick = { resetConfirm = false; onReset() }) {
                    Text("Lernstand löschen")
                }
            },
            dismissButton = {
                TextButton(onClick = { resetConfirm = false }) { Text("Abbrechen") }
            }
        )
    }
    selectedWord?.let { word ->
        AlertDialog(
            onDismissRequest = { selectedWord = null },
            title = { Text(word.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(if (word.kind == LearningKind.EMOTION) "Gefühl / Stimmung / Zustand" else "Bedürfnis",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary)
                    Text(word.description)
                    Text("Beispiel: ${word.example}", style = MaterialTheme.typography.bodySmall)
                    Text(word.question, color = MaterialTheme.colorScheme.primary)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onSeen(word.key)
                    selectedWord = null
                }, enabled = canChange) { Text("Als entdeckt markieren") }
            },
            dismissButton = {
                TextButton(onClick = {
                    selectedWord = null
                    if (word.kind == LearningKind.EMOTION) onOpenEmotion(word.id)
                }) { Text(if (word.kind == LearningKind.EMOTION) "Im Gefühlsatlas öffnen" else "Schließen") }
            }
        )
    }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text("Lernen & Entdecken", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            Text("In deinem Tempo. Ohne Noten, Streaks oder Pflichttermine.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(listOf("Heute", "Favoriten", "Üben", "Wiederholen", "Lernpfade", "Übersicht")) { label ->
                    FilterChip(selected = tab == label, onClick = {
                        tab = label
                        selectedRoute = null
                    }, label = { Text(label) })
                }
            }
            if (ui.busy) Text("Dein Lernstand wird sicher verarbeitet …",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (ui.error != null) {
                Text(ui.error, color = MaterialTheme.colorScheme.error)
                OutlinedButton(onClick = onReload, enabled = !ui.busy) {
                    Text("Lernstand erneut öffnen")
                }
            }
            if (ui.message != null) Text(ui.message,
                color = MaterialTheme.colorScheme.primary)
        }
        when (tab) {
            "Heute" -> {
                val daily = LearningEngine.daily(emotions, needs, LocalDate.now())
                LazyColumn(contentPadding = PaddingValues(18.dp),
                    verticalArrangement = Arrangement.spacedBy(13.dp)) {
                    item {
                        Text("Heute entdecken", style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold)
                        Text("Die Auswahl wechselt mit dem lokalen Kalendertag. Du kannst jederzeit aussetzen.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    item {
                        WordCard(daily.emotion, progress.favorites.contains(daily.emotion.key),
                            daily.emotion.key in progress.seen, canChange,
                            onFavorite, { selectedWord = daily.emotion })
                    }
                    item {
                        WordCard(daily.need, progress.favorites.contains(daily.need.key),
                            daily.need.key in progress.seen, canChange,
                            onFavorite, { selectedWord = daily.need })
                    }
                    item {
                        Text("Beide Begriffe stammen aus den Offline-Atlanten. Die Fragen sind Angebote und keine Deutung deiner eigenen Gefühle.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            "Favoriten" -> {
                FavoritesPane(words, progress.favorites, progress.seen, canChange,
                    onFavorite, { selectedWord = it })
            }
            "Üben" -> {
                // A short session remains stable while the user answers and records reviews.
                QuizPane(quizzes, ui, onReview)
            }
            "Wiederholen" -> {
                ReviewPane(words, ui, onFavorite, onReview, { selectedWord = it })
            }
            "Lernpfade" -> {
                PathsPane(routes, selectedRoute, { selectedRoute = it }, ui,
                    onFavorite, onStation, { selectedWord = it })
            }
            "Übersicht" -> {
                OverviewPane(words, routes, ui, { resetConfirm = true })
            }
        }
    }
}

@Composable
private fun WordCard(
    word: LearningWord,
    favorite: Boolean,
    seen: Boolean,
    enabled: Boolean,
    onFavorite: (String) -> Unit,
    onDetails: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text(if (word.kind == LearningKind.EMOTION) "GEFÜHL · ${if (seen) "ENTDECKT" else "NEU"}"
                else "BEDÜRFNIS · ${if (seen) "ENTDECKT" else "NEU"}",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Text(word.name, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Text(word.description, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onFavorite(word.key) }, enabled = enabled) {
                    Text(if (favorite) "★ Gespeichert" else "☆ Merken")
                }
                TextButton(onClick = onDetails) { Text("Entdecken") }
            }
        }
    }
}

@Composable
private fun FavoritesPane(
    words: List<LearningWord>, favorites: Set<String>, seen: Set<String>,
    enabled: Boolean, onFavorite: (String) -> Unit, onDetails: (LearningWord) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var kind by rememberSaveable { mutableStateOf("Alle") }
    val list = words.filter { it.key in favorites }
        .filter { kind == "Alle" ||
            (kind == "Gefühle" && it.kind == LearningKind.EMOTION) ||
            (kind == "Bedürfnisse" && it.kind == LearningKind.NEED) }
        .filter { query.isBlank() || it.name.contains(query.trim(), ignoreCase = true) }
        .sortedBy { it.name }
    LazyColumn(contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)) {
        item {
            Text("Meine Favoriten", style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold)
            OutlinedTextField(value = query, onValueChange = { query = it },
                label = { Text("Favoriten durchsuchen") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                items(listOf("Alle", "Gefühle", "Bedürfnisse")) { option ->
                    FilterChip(selected = option == kind, onClick = { kind = option },
                        label = { Text(option) })
                }
            }
        }
        if (list.isEmpty()) item {
            Text(if (favorites.isEmpty())
                "Noch keine Favoriten. Speichere Wörter im Bereich „Heute“ oder in Lernpfaden."
            else "Keine passenden Favoriten. Passe Suche oder Filter an.")
        }
        items(list, key = { it.key }) { word ->
            WordCard(word, true, word.key in seen, enabled, onFavorite, { onDetails(word) })
        }
    }
}

@Composable
private fun QuizPane(
    quizzes: List<LearningQuiz>, ui: LearningUiState,
    onReview: (String) -> Unit
) {
    // The order is frozen for this visit; learning activity cannot reshuffle a live question.
    // Shuffle once per study round, never during answer selection/recomposition.
    val round = remember(quizzes) {
        LearningEngine.quizQueue(quizzes, ui.progress, 5).map { it.shuffled() }
    }
    var index by remember { mutableIntStateOf(0) }
    var picked by remember { mutableIntStateOf(-1) }
    val current = round.getOrNull(index)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)) {
        Text("5 Minuten ausprobieren", style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold)
        Text("Hier vergleichst du redaktionelle Formulierungen. Deine persönlichen Gefühle haben keine richtigen oder falschen Antworten.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (current == null) {
            Text("Diese Lerneinheit ist abgeschlossen. Es gibt keine Note und keine tägliche Pflicht.")
            Button(onClick = { index = 0; picked = -1 }) { Text("Nochmal ansehen") }
        } else {
            Text("Frage ${index + 1} von ${round.size}",
                style = MaterialTheme.typography.labelMedium)
            Text(current.prompt, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            current.choices.forEachIndexed { choiceIndex, option ->
                OutlinedButton(
                    onClick = { picked = choiceIndex },
                    enabled = picked == -1 && !ui.busy && ui.error == null,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(option) }
            }
            if (picked >= 0) {
                Card(colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(if (picked == current.correctIndex)
                            "Diese Zuordnung entspricht dem Atlas."
                        else "Im Atlas ist eine andere Formulierung zugeordnet.",
                            fontWeight = FontWeight.SemiBold)
                        Text(current.explanation)
                    }
                }
                Button(onClick = {
                    onReview(current.wordKey)
                    index++
                    picked = -1
                }, enabled = !ui.busy && ui.error == null) { Text("Weiter") }
            }
        }
    }
}

@Composable
private fun ReviewPane(
    words: List<LearningWord>, ui: LearningUiState,
    onFavorite: (String) -> Unit,
    onReview: (String) -> Unit, onDetails: (LearningWord) -> Unit
) {
    // Recalculate after the encrypted progress finishes loading (the first
    // frame may be empty); keep this selection stable across review-count writes.
    val queue = remember(words, ui.progress.seen, ui.progress.favorites) {
        LearningEngine.reviewQueue(words, ui.progress, 5)
    }
    var index by remember { mutableIntStateOf(0) }
    val current = queue.getOrNull(index)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)) {
        Text("Freiwillig wiederholen", style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold)
        Text("Bis zu fünf entdeckte oder gemerkte Wörter. Ohne Punkte, Erinnerungszwang oder Benachrichtigungen.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (queue.isEmpty()) {
            Text("Entdecke zuerst Wörter oder speichere Favoriten. Dann findest du hier eine kleine Wiederholungsrunde.")
        } else if (current == null) {
            Text("Deine kleine Runde ist abgeschlossen. Du entscheidest, wann du wiederholst.")
            Button(onClick = { index = 0 }) { Text("Runde erneut ansehen") }
        } else {
            Text("Begriff ${index + 1} von ${queue.size}")
            WordCard(current, current.key in ui.progress.favorites,
                current.key in ui.progress.seen, !ui.busy && ui.error == null,
                onFavorite, { onDetails(current) })
            Text(current.question, color = MaterialTheme.colorScheme.primary)
            Button(onClick = {
                onReview(current.key)
                index++
            }, enabled = !ui.busy && ui.error == null) {
                Text("Gesehen · nächster Begriff")
            }
            TextButton(onClick = { index++ }) { Text("Überspringen") }
        }
    }
}

@Composable
private fun PathsPane(
    routes: List<LearningRoute>, selected: LearningRoute?,
    onSelect: (LearningRoute?) -> Unit, ui: LearningUiState,
    onFavorite: (String) -> Unit,
    onStation: (String) -> Unit, onDetails: (LearningWord) -> Unit
) {
    val state = ui.progress
    if (selected == null) {
        var filter by rememberSaveable { mutableStateOf("Alle") }
        val list = routes.filter {
            filter == "Alle" || (filter == "Gefühle" && it.key.startsWith("e:")) ||
                (filter == "Bedürfnisse" && it.key.startsWith("n:"))
        }
        LazyColumn(contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text("Lernpfade", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold)
                Text("14 Gefühls- und 8 Bedürfnis-Lernpfade in deinem Tempo. Jede Station kannst du bewusst abhaken.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(listOf("Alle", "Gefühle", "Bedürfnisse")) { kind ->
                        FilterChip(selected = filter == kind, onClick = { filter = kind },
                            label = { Text(kind) })
                    }
                }
            }
            items(list, key = { it.key }) { route ->
                val completed = route.words.count {
                    route.stationId(it) in state.completedStations
                }
                Card(onClick = { onSelect(route) }) {
                    Column(Modifier.fillMaxWidth().padding(15.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text(route.title, fontWeight = FontWeight.SemiBold)
                        Text(route.description, style = MaterialTheme.typography.bodySmall)
                        Text("$completed von ${route.words.size} Stationen freiwillig markiert",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                TextButton(onClick = { onSelect(null) }) { Text("← Alle Lernpfade") }
                Text(selected.title, style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold)
                Text(selected.description, style = MaterialTheme.typography.bodyMedium)
                Text("Markiere Stationen erst, wenn du sie selbst für abgeschlossen hältst.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(selected.words, key = { it.key }) { word ->
                val station = selected.stationId(word)
                val complete = station in state.completedStations
                Card {
                    Column(Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Text(if (complete) "✓ Freiwillig abgeschlossen" else "Offene Station",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary)
                        Text(word.name, style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold)
                        Text(word.description)
                        Text(word.question, color = MaterialTheme.colorScheme.primary)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(onClick = { onDetails(word) }) { Text("Mehr erfahren") }
                            OutlinedButton(onClick = { onFavorite(word.key) },
                                enabled = !ui.busy && ui.error == null) {
                                Text(if (word.key in state.favorites) "★" else "☆ Merken")
                            }
                        }
                        if (!complete) {
                            Button(onClick = { onStation(station) },
                                enabled = !ui.busy && ui.error == null) {
                                Text("Station abschließen")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewPane(
    words: List<LearningWord>, routes: List<LearningRoute>,
    ui: LearningUiState, onReset: () -> Unit
) {
    val progress = ui.progress
    val valid = words.map { it.key }.toSet()
    val emotionCount = progress.seen.count { it.startsWith("e:") && it in valid }
    val needCount = progress.seen.count { it.startsWith("n:") && it in valid }
    val markedRoutes = routes.count { route ->
        route.words.isNotEmpty() && route.words.all {
            route.stationId(it) in progress.completedStations
        }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Mein Lernüberblick", style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold)
        Text("Nur freiwillige Markierungen, keine Messung deines Wohlbefindens.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        OverviewValue("Gefühlsbegriffe entdeckt", "$emotionCount")
        OverviewValue("Bedürfnisse entdeckt", "$needCount")
        OverviewValue("Favoriten", "${progress.favorites.count { it in valid }}")
        OverviewValue("Freiwillige Wiederholungen", "${progress.reviewTotal}")
        OverviewValue("Lernpfade vollständig markiert", "$markedRoutes von ${routes.size}")
        OverviewValue("Stationen markiert", "${progress.completedStations.size}")
        HorizontalDivider()
        Text("Deine Favoriten und Lernmarkierungen sind ausschließlich lokal und separat vom Tagebuch verschlüsselt gespeichert. Du kannst den gesamten Lernstand unabhängig vom Tagebuch löschen.",
            style = MaterialTheme.typography.bodySmall)
        OutlinedButton(onClick = onReset,
            enabled = !ui.busy && ui.error == null) {
            Text("Lernstand zurücksetzen")
        }
    }
}

@Composable
private fun OverviewValue(title: String, value: String) {
    Card {
        Row(Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, modifier = Modifier.weight(1f))
            Text(value, fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary)
        }
    }
}
