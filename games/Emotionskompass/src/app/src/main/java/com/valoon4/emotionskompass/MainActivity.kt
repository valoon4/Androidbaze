package com.valoon4.emotionskompass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CompareArrows
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.valoon4.emotionskompass.core.designsystem.EmotionskompassTheme
import com.valoon4.emotionskompass.core.model.Emotion
import com.valoon4.emotionskompass.core.model.EmotionCatalog
import com.valoon4.emotionskompass.core.navigation.AppUiState
import com.valoon4.emotionskompass.core.navigation.AppViewModel
import com.valoon4.emotionskompass.core.navigation.Destination
import com.valoon4.emotionskompass.core.navigation.MainSection
import com.valoon4.emotionskompass.core.preferences.OnboardingPreferences
import kotlinx.coroutines.launch

/**
 * Milestone M1: Native Compose shell and navigation, *not* a production journal.
 * Personal entries, encryption and the actual reflection navigator follow in M3-M5.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EmotionskompassTheme {
                val preferences = remember { OnboardingPreferences(applicationContext) }
                val completed by preferences.completed.collectAsState(initial = false)
                val scope = rememberCoroutineScope()
                if (completed) {
                    EmotionskompassApp()
                } else {
                    WelcomeScreen(onContinue = { scope.launch { preferences.complete() } })
                }
            }
        }
    }
}

@Composable
private fun WelcomeScreen(onContinue: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 27.dp, vertical = 40.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(19.dp)) {
                Spacer(Modifier.height(28.dp))
                Mark()
                Text("Willkommen bei\nEmotionskompass", style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text("Ein ruhiger Raum, um Gefühle zu entdecken, die richtigen Worte zu finden und dich selbst besser zu verstehen.",
                    style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(15.dp))
                FeatureLine(Icons.Outlined.MenuBook, "Emotionen kennenlernen", "Begriffe, Unterschiede und Beispiele entdecken.")
                FeatureLine(Icons.Outlined.FavoriteBorder, "Dich selbst verstehen", "Gefühle und Erlebnisse achtsam reflektieren.")
                FeatureLine(Icons.Outlined.Lock, "Privat und ohne Konto", "Der aktuelle M1-Prototyp speichert noch keine Tagebucheinträge.")
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Keine Diagnosen, keine Bewertungen. Du entscheidest, welche Worte zu dir passen.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = onContinue, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
                    Text("Ohne Konto starten")
                    Spacer(Modifier.size(10.dp))
                    Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun FeatureLine(icon: ImageVector, title: String, subtitle: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(13.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(15.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
            Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }
        Column {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmotionskompassApp(vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    BackHandler(enabled = state.destination != Destination.MAIN) { vm.back() }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Emotionskompass", fontWeight = FontWeight.SemiBold, fontSize = 19.sp) },
                navigationIcon = {
                    if (state.destination != Destination.MAIN) {
                        IconButton(onClick = vm::back) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Zurück")
                        }
                    } else {
                        Box(Modifier.padding(start = 16.dp)) { Mark(small = true) }
                    }
                },
                actions = {
                    if (state.destination != Destination.SETTINGS) {
                        IconButton(onClick = vm::openSettings) {
                            Icon(Icons.Outlined.Settings, contentDescription = "Einstellungen")
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                listOf(
                    Triple(MainSection.START, "Start", Icons.Outlined.Home),
                    Triple(MainSection.ENTDECKEN, "Entdecken", Icons.Outlined.Explore),
                    Triple(MainSection.TAGEBUCH, "Tagebuch", Icons.Outlined.NoteAlt),
                    Triple(MainSection.RUECKBLICK, "Rückblick", Icons.Outlined.Insights)
                ).forEach { (section, title, icon) ->
                    NavigationBarItem(
                        selected = state.destination == Destination.MAIN && state.section == section,
                        onClick = { vm.selectSection(section) },
                        icon = { Icon(icon, contentDescription = null) },
                        label = { Text(title, maxLines = 1) },
                        alwaysShowLabel = true
                    )
                }
            }
        }
    ) { inset ->
        Box(Modifier.fillMaxSize().padding(inset)) {
            when (state.destination) {
                Destination.MAIN -> when (state.section) {
                    MainSection.START -> HomeScreen(
                        onExplore = { vm.selectSection(MainSection.ENTDECKEN) },
                        onCheckin = vm::openCheckin,
                        onWord = { vm.openEmotion("wehmut") }
                    )
                    MainSection.ENTDECKEN -> AtlasScreen(onEmotion = vm::openEmotion)
                    MainSection.TAGEBUCH -> ComingSoonScreen(
                        icon = Icons.Outlined.NoteAlt,
                        title = "Dein Tagebuch",
                        subtitle = "Hier kannst du später deine Erfahrungen und mehrere Gefühle je Eintrag sicher festhalten.",
                        milestone = "Geplant für M4"
                    )
                    MainSection.RUECKBLICK -> ComingSoonScreen(
                        icon = Icons.Outlined.Insights,
                        title = "Dein Rückblick",
                        subtitle = "Hier werden später persönliche Muster sichtbar – ohne Noten, Diagnosen oder Leistungsdruck.",
                        milestone = "Geplant für M7"
                    )
                }
                Destination.EMOTION -> {
                    val emotion = state.emotionId?.let(EmotionCatalog::find)
                    if (emotion != null) {
                        EmotionDetailScreen(
                            emotion,
                            onRelated = vm::openEmotion,
                            onCompare = vm::compareWith,
                            onReflect = vm::openCheckin
                        )
                    }
                }
                Destination.VERGLEICH -> ComparisonScreen(state)
                Destination.CHECKIN -> ComingSoonScreen(
                    icon = Icons.Outlined.FavoriteBorder,
                    title = "Dein Gefühlsnavigator",
                    subtitle = "Der geführte Check-in mit mehreren Emotionen, optionalen Fragen und einem geschützten Tagebuch entsteht in M3 und M4.",
                    milestone = "Vorschau · noch keine Dateneingabe",
                    onExplore = { vm.selectSection(MainSection.ENTDECKEN) }
                )
                Destination.SETTINGS -> SettingsScreen()
            }
        }
    }
}

@Composable
private fun Mark(small: Boolean = false) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(if (small) 12.dp else 20.dp)
    ) {
        Box(Modifier.size(if (small) 38.dp else 68.dp), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.AutoAwesome, contentDescription = "Emotionskompass-Logo",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(if (small) 25.dp else 38.dp))
        }
    }
}

@Composable
private fun HomeScreen(onExplore: () -> Unit, onCheckin: () -> Unit, onWord: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(21.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Text("DEIN RAUM FÜR GEFÜHLE", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary, letterSpacing = 1.2.sp)
        Text("Wie geht es dir\ngerade?", style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, lineHeight = 38.sp)
        Text("Du musst es noch nicht wissen. Finde in deinem Tempo die passenden Worte.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(3.dp))
        ActionCard(
            icon = Icons.Outlined.FavoriteBorder,
            title = "Mein Gefühl erkunden",
            description = "Wahrnehmen, benennen und reflektieren.",
            footnote = "Geführter Check-in · ab M3",
            emphasized = true,
            onClick = onCheckin
        )
        ActionCard(
            icon = Icons.Outlined.Explore,
            title = "Emotionen entdecken",
            description = "Begriffe und feine Unterschiede kennenlernen.",
            footnote = "Atlas-Vorschau erkunden",
            onClick = onExplore
        )
        Spacer(Modifier.height(10.dp))
        Text("Heute entdecken", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Card(onClick = onWord, modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Row(Modifier.padding(17.dp), horizontalArrangement = Arrangement.spacedBy(15.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Surface(color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(16.dp)) {
                    Box(Modifier.size(53.dp), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.AutoAwesome, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text("Wehmut", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                    Text("Wenn eine Erinnerung gleichzeitig schön und traurig ist.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall)
                }
                Icon(Icons.Outlined.ChevronRight, contentDescription = null)
            }
        }
        Text("M1 · Frühe Android-Vorschau. Noch kein echtes Tagebuch und keine persönlichen Einträge.",
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ActionCard(icon: ImageVector, title: String, description: String,
                       footnote: String, emphasized: Boolean = false, onClick: () -> Unit) {
    Card(
        onClick = onClick, modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(23.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (emphasized) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp))
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            }
            Text(description, style = MaterialTheme.typography.bodyMedium)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text(footnote, style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun AtlasScreen(onEmotion: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var family by rememberSaveable { mutableStateOf("Alle") }
    val result = remember(query, family) { EmotionCatalog.search(query, family) }
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(start = 19.dp, end = 19.dp, top = 12.dp, bottom = 6.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Emotionen entdecken", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            Text("Finde neue Begriffe und lerne die feinen Unterschiede kennen.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = query, onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                label = { Text("Gefühl oder Begriff suchen") }
            )
        }
        LazyRow(contentPadding = PaddingValues(horizontal = 19.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            items(EmotionCatalog.families) { value ->
                FilterChip(selected = family == value, onClick = { family = value },
                    label = { Text(value) })
            }
        }
        Text("${result.size} Begriffe · redaktionelle Vorschau M1",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (result.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Outlined.Search, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Kein passender Begriff gefunden.")
                    TextButton(onClick = { query = ""; family = "Alle" }) { Text("Filter zurücksetzen") }
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(start = 19.dp, end = 19.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)) {
                items(result, key = { it.id }) { emotion ->
                    Card(onClick = { onEmotion(emotion.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(emotion.family.uppercase(), style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary, letterSpacing = 0.7.sp)
                                Text(emotion.name, style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold)
                                Text(emotion.explanation, style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Outlined.ChevronRight, contentDescription = "Öffnen",
                                tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmotionDetailScreen(emotion: Emotion, onRelated: (String) -> Unit,
                                onCompare: (String) -> Unit, onReflect: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(21.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp)) {
        Tag(emotion.family)
        Text(emotion.name, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text(emotion.explanation, style = MaterialTheme.typography.bodyLarge)
        HorizontalDivider()
        Text("So könnte es sich zeigen", style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold)
        Surface(color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(17.dp)) {
            Text("„${emotion.example}“", modifier = Modifier.padding(17.dp),
                style = MaterialTheme.typography.bodyMedium)
        }
        Text("Ähnliche und verwandte Gefühle", style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold)
        emotion.related.forEach { related ->
            val other = EmotionCatalog.find(related)
            if (other != null) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = { onRelated(other.id) }, modifier = Modifier.weight(1f)) {
                        Text(other.name)
                    }
                    IconButton(onClick = { onCompare(other.id) }) {
                        Icon(Icons.Outlined.CompareArrows, contentDescription = "Mit ${other.name} vergleichen")
                    }
                }
            }
        }
        HorizontalDivider()
        Button(onClick = onReflect, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Outlined.FavoriteBorder, contentDescription = null)
            Spacer(Modifier.size(9.dp))
            Text("Über meine Erfahrung nachdenken")
        }
        Text("Die Inhalte sind Orientierungshilfen. Du entscheidest, welche Begriffe passen.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ComparisonScreen(state: AppUiState) {
    val first = state.emotionId?.let(EmotionCatalog::find)
    val second = state.comparisonId?.let(EmotionCatalog::find)
    if (first == null || second == null) return
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(21.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Tag("BEGRIFFE UNTERSCHEIDEN")
        Text("Wie unterscheiden sich diese Gefühle?",
            style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        listOf(first, second).forEach { emotion ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(emotion.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text(emotion.explanation, style = MaterialTheme.typography.bodyMedium)
                    Text("Beispiel: ${emotion.example}", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (first.id == "frustration" && second.id == "enttaeuschung" ||
            first.id == "enttaeuschung" && second.id == "frustration") {
            Surface(color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(18.dp)) {
                Text("Frustration kann entstehen, wenn etwas nicht vorangeht. Enttäuschung kann auftreten, wenn eine Erwartung unerfüllt bleibt. Beide Gefühle können gleichzeitig vorkommen.",
                    modifier = Modifier.padding(18.dp), style = MaterialTheme.typography.bodyMedium)
            }
        }
        Text("Gefühle überschneiden sich häufig. Es gibt nicht immer nur eine passende Bezeichnung.",
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Tag(label: String) {
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
        Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ComingSoonScreen(icon: ImageVector, title: String, subtitle: String,
                             milestone: String, onExplore: (() -> Unit)? = null) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(28.dp),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
            Box(Modifier.size(82.dp), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(39.dp),
                    tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(Modifier.height(22.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center)
        Spacer(Modifier.height(10.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        Tag(milestone)
        if (onExplore != null) {
            Spacer(Modifier.height(27.dp))
            OutlinedButton(onClick = onExplore) { Text("Stattdessen Emotionen entdecken") }
        }
    }
}

@Composable
private fun SettingsScreen() {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(21.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("Einstellungen & Informationen", style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        FeatureLine(Icons.Outlined.Lock, "Datenschutz", "Keine Anmeldung. Keine Internetberechtigung.")
        HorizontalDivider()
        Text("M1 · Native Android-Vorschau", style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold)
        Text("Die App speichert derzeit nur, dass die Einführung abgeschlossen wurde. Tagebucheinträge, verschlüsselte Datenspeicherung und Datenexport folgen erst in weiteren Etappen.",
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Emotionskompass ist ein Lern- und Reflexionswerkzeug, keine Therapie und keine medizinische Diagnose.",
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        HorizontalDivider()
        Text("Version 0.1.0-m1", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
