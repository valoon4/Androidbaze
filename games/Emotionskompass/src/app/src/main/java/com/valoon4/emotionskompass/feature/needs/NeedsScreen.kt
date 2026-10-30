package com.valoon4.emotionskompass.feature.needs

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler

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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.valoon4.emotionskompass.core.model.HumanNeed
import com.valoon4.emotionskompass.core.model.NeedCatalog
import com.valoon4.emotionskompass.core.model.NeedComparison
import com.valoon4.emotionskompass.core.model.NeedLearningPath
import com.valoon4.emotionskompass.core.strategies.StrategyCatalog
import com.valoon4.emotionskompass.core.strategies.StrategyLearningIndex
import com.valoon4.emotionskompass.core.strategies.NeedStrategyLinkIndex
import com.valoon4.emotionskompass.core.strategies.NeedStrategyOption

/** Offline educational reference; all browsing state stays in memory. */
@Composable
fun NeedsScreen(
    catalog: NeedCatalog,
    favorites: Set<String> = emptySet(),
    seen: Set<String> = emptySet(),
    canSave: Boolean = false,
    onFavorite: (String) -> Unit = {},
    onSeen: (String) -> Unit = {},
    strategyCatalog: StrategyCatalog? = null,
    needStrategyLinks: NeedStrategyLinkIndex? = null,
    onStrategy: (String) -> Unit = {},
    onOpenStrategyAtlas: () -> Unit = {},
    requestedNeedId: String? = null,
    onRequestConsumed: () -> Unit = {},
    onReturnToStrategy: (() -> Unit)? = null
) {
    var tab by rememberSaveable { mutableStateOf("Atlas") }
    var detailId by rememberSaveable { mutableStateOf<String?>(null) }
    var pathId by rememberSaveable { mutableStateOf<String?>(null) }
    var comparison by remember { mutableStateOf<NeedComparison?>(null) }
    val tabStates = rememberSaveableStateHolder()
    LaunchedEffect(requestedNeedId) {
        if (requestedNeedId != null && catalog.find(requestedNeedId) != null) {
            tab = "Atlas"
            detailId = requestedNeedId
            onRequestConsumed()
        }
    }
    val closeDetail: () -> Unit = {
        detailId = null
        if (onReturnToStrategy != null) onReturnToStrategy()
    }
    BackHandler(enabled = detailId != null || pathId != null || comparison != null) {
        when {
            detailId != null -> closeDetail()
            comparison != null -> comparison = null
            else -> pathId = null
        }
    }

    // A personal needs-compass selection should not be exposed in recents/screenshots.
    val activity = LocalContext.current as? Activity
    DisposableEffect(activity, tab) {
        if (tab == "Kompass") {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        onDispose {
            if (tab == "Kompass") {
                activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }

    if (detailId != null) {
        val need = catalog.find(detailId!!)
        if (need != null) {
            NeedDetailScreen(need, catalog,
                onBack = closeDetail,
                onRelated = { detailId = it },
                backLabel = if (onReturnToStrategy != null) "Zur Strategie" else "Zum Bedürfnisatlas",
                strategyCatalog = strategyCatalog,
                needStrategyLinks = needStrategyLinks,
                onStrategy = onStrategy,
                onOpenStrategyAtlas = onOpenStrategyAtlas,
                favorite = "n:${need.id}" in favorites,
                seen = "n:${need.id}" in seen,
                canSave = canSave,
                onFavorite = { onFavorite("n:${need.id}") },
                onSeen = { onSeen("n:${need.id}") })
            return
        }
    }
    if (comparison != null) {
        NeedCompareScreen(catalog, comparison!!, onBack = { comparison = null })
        return
    }
    if (pathId != null) {
        val path = catalog.path(pathId!!)
        if (path != null) {
            NeedPathScreen(catalog, path,
                onBack = { pathId = null },
                onNeed = { detailId = it })
            return
        }
    }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Column(Modifier.fillMaxWidth().padding(start = 19.dp, end = 19.dp, top = 13.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Bedürfnisse verstehen", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            Text("Was ist mir wichtig? Was tut mir gut – und was kommt vielleicht zu kurz?",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                items(listOf("Atlas", "Kompass", "Lernpfade", "Vergleiche")) { option ->
                    FilterChip(selected = tab == option, onClick = { tab = option },
                        label = { Text(option) })
                }
            }
        }
        tabStates.SaveableStateProvider(tab) {
            when (tab) {
                "Atlas" -> NeedAtlasPane(catalog, onNeed = { detailId = it })
                "Kompass" -> NeedCompassScreen(catalog, onNeed = { detailId = it })
                "Lernpfade" -> NeedPathsPane(catalog, onPath = { pathId = it })
                else -> NeedComparisonsPane(catalog, onCompare = { comparison = it })
            }
        }
    }
}

@Composable
private fun NeedAtlasPane(catalog: NeedCatalog, onNeed: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var family by rememberSaveable { mutableStateOf("Alle") }
    var letter by rememberSaveable { mutableStateOf("Alle") }
    val results = remember(catalog, query, family, letter) { catalog.browse(query, family, letter) }

    LazyColumn(Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 19.dp, end = 19.dp, top = 12.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("${catalog.needs.size} Begriffe in acht Familien – keine abschließende wissenschaftliche Liste.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = query, onValueChange = { query = it },
                label = { Text("Bedürfnis oder Synonym suchen") },
                singleLine = true)
        }
        item {
            Text("Thema auswählen", style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                items(catalog.families) { option ->
                    FilterChip(selected = family == option,
                        onClick = { family = option },
                        label = { Text(option) })
                }
            }
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(listOf("Alle") + ('A'..'Z').map { it.toString() }) { option ->
                    FilterChip(selected = letter == option,
                        onClick = { letter = option },
                        label = { Text(option) })
                }
            }
        }
        item {
            // The count and reset action must never compete for horizontal room.
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("${results.size} von ${catalog.needs.size} Begriffen",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (query.isNotBlank() || family != "Alle" || letter != "Alle") {
                    TextButton(onClick = { query = ""; family = "Alle"; letter = "Alle" },
                        modifier = Modifier.fillMaxWidth()) {
                        Text("Suche und Filter zurücksetzen")
                    }
                }
            }
            if (results.isEmpty()) Text("Keine Treffer. Du kannst die Filter zurücksetzen.")
        }
        items(results, key = { it.id }) { need ->
            Card(onClick = { onNeed(need.id) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(15.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(need.family, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary)
                    Text(need.name, style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    Text(need.definition, style = MaterialTheme.typography.bodySmall,
                        maxLines = 2, overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item { NeedDisclaimer() }
    }
}

@Composable
private fun NeedDetailScreen(
    need: HumanNeed,
    catalog: NeedCatalog,
    onBack: () -> Unit,
    onRelated: (String) -> Unit,
    backLabel: String,
    strategyCatalog: StrategyCatalog?,
    needStrategyLinks: NeedStrategyLinkIndex?,
    onStrategy: (String) -> Unit,
    onOpenStrategyAtlas: () -> Unit,
    favorite: Boolean,
    seen: Boolean,
    canSave: Boolean,
    onFavorite: () -> Unit,
    onSeen: () -> Unit
) {
    // Related needs start at the top instead of inheriting the previous detail's position.
    val detailScroll = rememberSaveable(need.id, saver = LazyListState.Saver) { LazyListState() }
    LazyColumn(Modifier.fillMaxSize(), state = detailScroll,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)) {
        item {
            TextButton(onClick = onBack) { Text("← $backLabel") }
            Text(need.family.uppercase(), style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary)
            Text(need.name, style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() })
            Text(need.definition, style = MaterialTheme.typography.bodyLarge)
        }
        item {
            // Vertical actions stay readable at Android font scale 200% on narrow phones.
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(onClick = onFavorite, enabled = canSave,
                    modifier = Modifier.fillMaxWidth()) {
                    Text(if (favorite) "★ Gespeichert" else "☆ Merken")
                }
                TextButton(onClick = onSeen, enabled = canSave && !seen,
                    modifier = Modifier.fillMaxWidth()) {
                    Text(if (seen) "✓ Entdeckt" else "Als entdeckt markieren")
                }
            }
        }
        item { NeedInfoCard("Wenn das Bedürfnis erfüllt erscheint", need.fulfilledExample) }
        item { NeedInfoCard("Wenn es zu kurz kommen könnte", need.unmetExample) }
        item {
            NeedInfoCard("Zum Nachdenken", need.reflectionQuestion)
            Spacer(Modifier.size(12.dp))
            NeedInfoCard("Ein Alltagsbeispiel – kein festes Rezept",
                need.strategyExample)
            Text("Es gibt viele mögliche Wege, ein Bedürfnis zu berücksichtigen. Du entscheidest, welcher zu dir passt.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        // Prefer the reviewed manual index. If an optional asset is corrupted,
        // keep the original 8-ID links rather than affecting the needs atlas.
        val relevantOptions = strategyCatalog?.let { strategyBook ->
            needStrategyLinks?.optionsForNeed(need.id, strategyBook)
                ?: StrategyLearningIndex.strategiesForNeed(need.id, strategyBook)
                    // Social acceptance (a need) is not inner acceptance (a coping tool).
                    .filterNot { need.id == "akzeptanz" && it.id == "acceptance" }
                    .map { NeedStrategyOption(it, "") }
        }.orEmpty()
        item {
            Text("Mögliche Wege für dieses Bedürfnis",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() },
                fontWeight = FontWeight.SemiBold)
            Text("Redaktionell ausgewählte Beispiele, keine Empfehlung für deine persönliche Situation. Entscheidend ist, ob der Weg für dich sicher und passend erscheint.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (need.id == "akzeptanz") item {
            Text("Wichtig: Hier bedeutet Akzeptanz, von anderen angenommen zu werden. Die Strategie „Annehmen, was gerade ist“ beschreibt dagegen den Umgang mit dem eigenen Erleben – nicht dasselbe.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (relevantOptions.isNotEmpty()) {
            items(relevantOptions, key = { "strategy-${it.strategy.id}" }) { option ->
                // One click target and one screen-reader focus for title + reason.
                Card(onClick = { onStrategy(option.strategy.id) },
                    modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(option.strategy.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold)
                        if (option.reason.isNotBlank()) Text(
                            option.reason,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text("Strategie ansehen",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        } else if (strategyCatalog != null) {
            item {
                val emptyNote = when (need.id) {
                    "schlaf", "gesundheit", "naehrung" ->
                        "Dafür gibt es im aktuellen Strategieatlas keine ausreichend spezifische Karte. Ruhe- oder Gedankenübungen ersetzen keine körperliche Versorgung oder nötige medizinische Hilfe."
                    else ->
                        "Für dieses Bedürfnis ist im aktuellen Atlas noch kein ausreichend konkreter Bezug beschrieben. Du musst deshalb nichts aus den vorhandenen Strategien auswählen."
                }
                Text(emptyNote, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (strategyCatalog != null) item {
            TextButton(onClick = onOpenStrategyAtlas) {
                Text("Alle Strategien selbst entdecken")
            }
        }
        item {
            Text("Verwandte Bedürfnisse", fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                items(need.related) { id ->
                    val label = catalog.find(id)?.name ?: id
                    FilterChip(selected = false, onClick = { onRelated(id) },
                        label = { Text(label) })
                }
            }
        }
        item {
            Text("Suchbegriffe: ${need.synonyms.joinToString(" · ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            NeedDisclaimer()
        }
    }
}

@Composable
private fun NeedPathsPane(catalog: NeedCatalog, onPath: (String) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(19.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)) {
        item {
            Text("Themen in deinem Tempo erkunden",
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Die Wege sind offen und ohne Leistungsbewertung.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(catalog.learningPaths, key = { it.id }) { path ->
            Card(onClick = { onPath(path.id) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Column(Modifier.fillMaxWidth().padding(17.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(path.title, fontWeight = FontWeight.SemiBold)
                    Text(path.description, style = MaterialTheme.typography.bodySmall)
                    Text("${path.needIds.size} Begriffe erkunden",
                        style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun NeedPathScreen(
    catalog: NeedCatalog,
    path: NeedLearningPath,
    onBack: () -> Unit,
    onNeed: (String) -> Unit
) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(19.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)) {
        item {
            TextButton(onClick = onBack) { Text("Zurück zu den Lernpfaden") }
            Text(path.title, style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            Text(path.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(catalog.itemsInPath(path), key = { it.id }) { need ->
            Card(onClick = { onNeed(need.id) }) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(need.name, fontWeight = FontWeight.SemiBold)
                    Text(need.definition, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun NeedComparisonsPane(catalog: NeedCatalog, onCompare: (NeedComparison) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(19.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)) {
        item {
            Text("${catalog.comparisons.size} Begriffsvergleiche",
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Ähnliche Bedürfnisse entdecken, ohne sie gleichzusetzen.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(catalog.comparisons, key = { "${it.first}-${it.second}" }) { pair ->
            Card(onClick = { onCompare(pair) }) {
                Column(Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${catalog.find(pair.first)?.name} und ${catalog.find(pair.second)?.name}",
                        fontWeight = FontWeight.SemiBold)
                    Text(pair.overlap, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun NeedCompareScreen(catalog: NeedCatalog, pair: NeedComparison, onBack: () -> Unit) {
    val first = catalog.find(pair.first) ?: return
    val second = catalog.find(pair.second) ?: return
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)) {
        item {
            TextButton(onClick = onBack) { Text("Zurück zu den Vergleichen") }
            Text("${first.name} und ${second.name}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
        }
        item { NeedInfoCard("Was beide verbindet", pair.overlap) }
        item { NeedInfoCard(first.name, pair.firstDistinction) }
        item { NeedInfoCard(second.name, pair.secondDistinction) }
        item { NeedInfoCard("Deine Frage", pair.question) }
        item { NeedDisclaimer() }
    }
}

@Composable
internal fun NeedInfoCard(title: String, content: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(content, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
internal fun NeedDisclaimer() {
    Text("Bedürfnisse sind individuell und überlappen sich. Die Beispiele helfen beim Erkunden; sie sind keine Diagnose und keine feste Zuordnung zu Gefühlen.",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall)
}
