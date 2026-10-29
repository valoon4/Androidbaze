package com.valoon4.emotionskompass

import android.os.Bundle
import android.view.WindowManager
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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.DisposableEffect
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
import com.valoon4.emotionskompass.core.model.EmotionKind
import com.valoon4.emotionskompass.core.model.LearningPath
import com.valoon4.emotionskompass.core.model.NeedCatalog
import com.valoon4.emotionskompass.core.navigation.AtlasFilters
import com.valoon4.emotionskompass.core.navigation.AppUiState
import com.valoon4.emotionskompass.core.navigation.AppViewModel
import com.valoon4.emotionskompass.core.navigation.Destination
import com.valoon4.emotionskompass.core.navigation.MainSection
import com.valoon4.emotionskompass.core.preferences.OnboardingPreferences
import com.valoon4.emotionskompass.feature.checkin.CheckInScreen
import com.valoon4.emotionskompass.feature.needs.NeedsScreen
import kotlinx.coroutines.launch

/**
 * M3: Native offline emotions atlas and ephemeral guided check-in.
 * No personal reflection is persisted; protected journaling follows in M4-M5.
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
                val catalog = remember {
                    EmotionCatalog.fromJson(
                        assets.open("catalog/emotions.de.v2.json")
                            .bufferedReader(Charsets.UTF_8).use { it.readText() }
                    )
                }
                val needCatalog = remember {
                    NeedCatalog.fromJson(
                        assets.open("catalog/needs.de.v1.json")
                            .bufferedReader(Charsets.UTF_8).use { it.readText() }
                    )
                }
                if (completed) {
                    EmotionskompassApp(catalog, needCatalog, this@MainActivity)
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
                FeatureLine(Icons.Outlined.Lock, "Privat und ohne Konto", "Keine Tagebucheinträge. Nur die Einführung wird gespeichert.")
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
private fun EmotionskompassApp(
    catalog: EmotionCatalog,
    needCatalog: NeedCatalog,
    hostActivity: ComponentActivity,
    vm: AppViewModel = viewModel(factory = AppViewModel.factory(catalog, needCatalog))
) {
    val state by vm.state.collectAsState()
    val filters by vm.filters.collectAsState()
    val checkIn by vm.checkIn.collectAsState()
    var confirmDiscard by remember { mutableStateOf(false) }

    // Check-in content must not appear in Android screenshots or recent-app previews.
    DisposableEffect(hostActivity, state.destination) {
        if (state.destination == Destination.CHECKIN) {
            hostActivity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        onDispose {
            if (state.destination == Destination.CHECKIN) {
                hostActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }

    val requestDiscard: () -> Unit = {
        if (checkIn.hasPersonalInput) confirmDiscard = true
        else vm.closeCheckIn()
    }
    val requestBack: () -> Unit = {
        if (state.destination == Destination.CHECKIN && checkIn.isFirstStep) {
            requestDiscard()
        } else {
            vm.back()
        }
    }
    BackHandler(enabled = state.destination != Destination.MAIN) { requestBack() }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Emotionskompass", fontWeight = FontWeight.SemiBold, fontSize = 19.sp) },
                navigationIcon = {
                    if (state.destination != Destination.MAIN) {
                        IconButton(onClick = requestBack) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Zurück")
                        }
                    } else {
                        Box(Modifier.padding(start = 16.dp)) { Mark(small = true) }
                    }
                },
                actions = {
                    if (state.destination != Destination.SETTINGS && state.destination != Destination.CHECKIN) {
                        IconButton(onClick = vm::openSettings) {
                            Icon(Icons.Outlined.Settings, contentDescription = "Einstellungen")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (state.destination != Destination.CHECKIN) NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                listOf(
                    Triple(MainSection.START, "Start", Icons.Outlined.Home),
                    Triple(MainSection.ENTDECKEN, "Gefühle", Icons.Outlined.FavoriteBorder),
                    Triple(MainSection.BEDUERFNISSE, "Bedürfnisse", Icons.Outlined.Explore),
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
                        termsCount = catalog.emotions.size,
                        needsCount = needCatalog.needs.size,
                        onExplore = { vm.selectSection(MainSection.ENTDECKEN) },
                        onNeeds = { vm.selectSection(MainSection.BEDUERFNISSE) },
                        onCheckin = { vm.openCheckin() },
                        onWord = { vm.openEmotion("wehmut") }
                    )
                    MainSection.ENTDECKEN -> AtlasScreen(
                        catalog = catalog,
                        filters = filters,
                        onEmotion = vm::openEmotion,
                        onPath = vm::openPath,
                        onCards = vm::openLearningCards,
                        onComparisons = vm::openComparisons,
                        onSearch = vm::setSearch,
                        onFamily = vm::setFamily,
                        onKind = vm::setKind,
                        onLetter = vm::setLetter,
                        onClear = vm::clearFilters
                    )
                    MainSection.BEDUERFNISSE -> NeedsScreen(needCatalog)
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
                    val emotion = state.emotionId?.let(catalog::find)
                    if (emotion != null) {
                        EmotionDetailScreen(
                            emotion,
                            catalog = catalog,
                            onRelated = vm::openEmotion,
                            onCompare = vm::compareWith,
                            onReflect = { vm.openCheckin(emotion.id) }
                        )
                    }
                }
                Destination.VERGLEICH -> ComparisonScreen(state, catalog)
                Destination.LEARN_CARDS -> LearningCardsScreen(catalog, vm::openEmotion)
                Destination.COMPARISONS -> ComparisonDirectoryScreen(catalog, vm::openComparison)
                Destination.PATH -> {
                    val path = state.pathId?.let(catalog::findPath)
                    if (path != null) LearningPathScreen(path, catalog, vm::openEmotion)
                }
                Destination.CHECKIN -> CheckInScreen(
                    catalog = catalog,
                    needCatalog = needCatalog,
                    draft = checkIn,
                    onMode = vm::setCheckInMode,
                    onToggleCue = vm::toggleBodyCue,
                    onToggleEmotion = vm::toggleCheckInEmotion,
                    onIntensity = vm::setCheckInIntensity,
                    onArea = vm::setCheckInArea,
                    onNote = vm::setCheckInNote,
                    onSituation = vm::setCheckInSituation,
                    onInterpretation = vm::setCheckInInterpretation,
                    onNeed = vm::toggleCheckInNeed,
                    onInsight = vm::setCheckInInsight,
                    onNextStepIdea = vm::setCheckInNextStep,
                    onNextStepNote = vm::setCheckInNextStepNote,
                    onNext = vm::nextCheckInStep,
                    onPrevious = vm::previousCheckInStep,
                    onRestart = vm::restartCheckIn,
                    onClose = requestDiscard
                )
                Destination.SETTINGS -> SettingsScreen(catalog, needCatalog)
            }
        }
    }
    if (confirmDiscard && state.destination == Destination.CHECKIN) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            title = { Text("Check-in verwerfen?") },
            text = {
                Text("Deine persönlichen Angaben wurden nicht gespeichert und gehen beim Verwerfen verloren. Möchtest du wirklich beenden?")
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmDiscard = false
                    vm.closeCheckIn()
                }) { Text("Angaben verwerfen") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDiscard = false }) {
                    Text("Weiter reflektieren")
                }
            }
        )
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
private fun HomeScreen(termsCount: Int, needsCount: Int, onExplore: () -> Unit,
    onNeeds: () -> Unit, onCheckin: () -> Unit, onWord: () -> Unit) {
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
            footnote = "$termsCount Begriffe · Lernpfade",
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
        Text("M3.2 · $termsCount Gefühle lernen, benennen und behutsam reflektieren.",
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
private fun AtlasScreen(
    catalog: EmotionCatalog,
    filters: AtlasFilters,
    onEmotion: (String) -> Unit,
    onPath: (String) -> Unit,
    onCards: () -> Unit,
    onComparisons: () -> Unit,
    onSearch: (String) -> Unit,
    onFamily: (String) -> Unit,
    onKind: (EmotionKind?) -> Unit,
    onLetter: (String) -> Unit,
    onClear: () -> Unit
) {
    val result = remember(catalog, filters) {
        catalog.browse(filters.query, filters.family, filters.kind, filters.letter)
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 19.dp, end = 19.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Spacer(Modifier.height(4.dp))
                Text("Emotionen entdecken", style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold)
                Text("${catalog.emotions.size} Begriffe, viele Nuancen. Du musst noch nicht wissen, wonach du suchst.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = filters.query, onValueChange = onSearch,
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Outlined.Search, contentDescription = null)
                    },
                    label = { Text("Begriff, Synonym oder Beschreibung") }
                )
                OutlinedButton(
                    onClick = { onEmotion(catalog.emotions.random().id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Überrasch mich mit einem Begriff")
                }
            }
        }
        item {
            Text("Neugierig lernen", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Text("Erst beschreiben, dann benennen – ohne Noten und ohne gespeicherten Lernstand.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onCards, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.MenuBook, contentDescription = null)
                    Spacer(Modifier.size(6.dp))
                    Text("Wortkarten")
                }
                OutlinedButton(onClick = onComparisons, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.CompareArrows, contentDescription = null)
                    Spacer(Modifier.size(6.dp))
                    Text("Vergleiche")
                }
            }
        }
        item {
            Text("Entdeckungswege", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Text("Kurze thematische Reisen durch ähnliche und unterschiedliche Gefühle.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(catalog.learningPaths, key = { it.id }) { path ->
                    Card(
                        onClick = { onPath(path.id) },
                        modifier = Modifier.size(width = 230.dp, height = 146.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        shape = RoundedCornerShape(19.dp)
                    ) {
                        Column(Modifier.fillMaxSize().padding(15.dp),
                            verticalArrangement = Arrangement.SpaceBetween) {
                            Text(path.title, style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold)
                            Text(path.description, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Row(Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                Text("${path.emotionIds.size} Begriffe",
                                    style = MaterialTheme.typography.labelSmall)
                                Icon(Icons.Outlined.ChevronRight,
                                    contentDescription = "Lernpfad öffnen")
                            }
                        }
                    }
                }
            }
        }
        item {
            Text("Gefühlsfamilie", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(catalog.families) { family ->
                    FilterChip(
                        selected = filters.family == family,
                        onClick = { onFamily(family) },
                        label = { Text(family) }
                    )
                }
            }
        }
        item {
            Text("Was möchtest du betrachten?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val kinds = listOf(
                    null to "Alle Arten",
                    EmotionKind.EMOTION to "Gefühle",
                    EmotionKind.MOOD to "Stimmungen",
                    EmotionKind.STATE to "Zustände"
                )
                items(kinds) { (kind, label) ->
                    FilterChip(
                        selected = filters.kind == kind,
                        onClick = { onKind(kind) },
                        label = { Text(label) }
                    )
                }
            }
        }
        item {
            Text("Alphabet", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                items(listOf("Alle") + ('A'..'Z').map(Char::toString)) { letter ->
                    FilterChip(
                        selected = filters.letter == letter,
                        onClick = { onLetter(letter) },
                        label = { Text(letter) }
                    )
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("${result.size} von ${catalog.emotions.size} Begriffen",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (filters != AtlasFilters()) {
                    TextButton(onClick = onClear) { Text("Filter zurücksetzen") }
                }
            }
            if (result.isEmpty()) {
                Text("Keine Treffer. Versuche einen anderen Begriff oder setze die Filter zurück.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        items(result, key = { it.id }) { emotion ->
            Card(
                onClick = { onEmotion(emotion.id) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(emotion.family.uppercase() + " · " + emotionKindLabel(emotion.kind),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.4.sp)
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
        item {
            Text("Der Atlas ist eine redaktionelle Arbeitsfassung und keine psychologische Diagnose.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun emotionKindLabel(kind: EmotionKind): String = when (kind) {
    EmotionKind.EMOTION -> "Gefühl"
    EmotionKind.MOOD -> "Stimmung"
    EmotionKind.STATE -> "Zustand"
}

@Composable
private fun LearningCardsScreen(
    catalog: EmotionCatalog,
    onEmotion: (String) -> Unit
) {
    var family by rememberSaveable { mutableStateOf("Alle") }
    var index by rememberSaveable { mutableStateOf(0) }
    var revealed by rememberSaveable { mutableStateOf(false) }
    val cards = remember(catalog, family) {
        catalog.search("", family)
    }
    val current = cards.getOrNull(index.coerceIn(0, (cards.size - 1).coerceAtLeast(0)))

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Text("Wortkarten entdecken", style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Text("Lies zuerst die Beschreibung. Welches Wort könnte passen? Du musst nichts richtig beantworten.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            items(catalog.families) { option ->
                FilterChip(
                    selected = option == family,
                    onClick = {
                        family = option
                        index = 0
                        revealed = false
                    },
                    label = { Text(option) }
                )
            }
        }
        if (current != null) {
            Tag("KARTE ${index + 1} VON ${cards.size}")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(15.dp)) {
                    Text("Welcher Begriff könnte zu dieser Beschreibung passen?",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary)
                    Text(current.explanation,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    Text("Alltagssituation: ${current.examples.first()}",
                        style = MaterialTheme.typography.bodyMedium)
                    if (revealed) {
                        HorizontalDivider()
                        Text(current.name, style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary)
                        Text(emotionKindLabel(current.kind),
                            style = MaterialTheme.typography.labelSmall)
                        Text(current.reflectionQuestion,
                            style = MaterialTheme.typography.bodyMedium)
                        OutlinedButton(onClick = { onEmotion(current.id) }) {
                            Text("Ganzen Begriff ansehen")
                            Spacer(Modifier.size(6.dp))
                            Icon(Icons.Outlined.ChevronRight, contentDescription = null)
                        }
                    } else {
                        Button(onClick = { revealed = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Begriff aufdecken")
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = {
                        index = (index - 1).coerceAtLeast(0)
                        revealed = false
                    },
                    enabled = index > 0,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Zurück")
                }
                Button(
                    onClick = {
                        index = (index + 1) % cards.size
                        revealed = false
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (index == cards.lastIndex) "Von vorne" else "Nächste Karte")
                }
            }
        }
        Text("Hier wird kein Lernfortschritt gespeichert. Die Karten sind eine freiwillige Einladung zum Entdecken.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ComparisonDirectoryScreen(
    catalog: EmotionCatalog,
    onCompare: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Unterschiede verstehen",
                style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("${catalog.comparisons.size} redaktionelle Begriffsvergleiche. Lerne, was zwei Erfahrungen verbindet und unterscheidet.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(9.dp))
        }
        items(catalog.comparisons, key = { "${it.first}-${it.second}" }) { comparison ->
            val first = catalog.find(comparison.first)
            val second = catalog.find(comparison.second)
            if (first != null && second != null) {
                Card(
                    onClick = { onCompare(comparison.first, comparison.second) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(Modifier.fillMaxWidth().padding(17.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text("${first.name} und ${second.name}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold)
                            Text(comparison.overlap,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Outlined.ChevronRight,
                            contentDescription = "Vergleich ansehen")
                    }
                }
            }
        }
        item {
            Text("Diese Vergleiche sind Orientierungshilfen und keine eindeutigen psychologischen Kategorien.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LearningPathScreen(
    path: LearningPath,
    catalog: EmotionCatalog,
    onEmotion: (String) -> Unit
) {
    val entries = remember(path, catalog) { catalog.emotionsInPath(path) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Tag("LERNPFAD · ${entries.size} STATIONEN")
            Spacer(Modifier.height(14.dp))
            Text(path.title, style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            Text(path.description, style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(9.dp))
            Text("Entdecke die Begriffe in deinem eigenen Tempo. Alle Stationen sind freiwillig.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(entries, key = { it.id }) { emotion ->
            Card(
                onClick = { onEmotion(emotion.id) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(Modifier.fillMaxWidth().padding(17.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(emotion.name, style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold)
                        Text(emotion.explanation, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Outlined.ChevronRight, contentDescription = "Begriff ansehen")
                }
            }
        }
    }
}

@Composable
private fun EmotionDetailScreen(
    emotion: Emotion,
    catalog: EmotionCatalog,
    onRelated: (String) -> Unit,
    onCompare: (String) -> Unit,
    onReflect: () -> Unit
) {
    val comparisons = catalog.comparisonOptions(emotion.id)
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(21.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Tag(emotion.family)
            if (emotion.kind != com.valoon4.emotionskompass.core.model.EmotionKind.EMOTION) {
                Tag(if (emotion.kind == com.valoon4.emotionskompass.core.model.EmotionKind.MOOD) "Stimmung" else "Zustand")
            }
        }
        Text(emotion.name, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text(emotion.explanation, style = MaterialTheme.typography.bodyLarge)
        if (emotion.families.size > 1) {
            Text("Kann auch in andere Themenbereiche passen: " + emotion.families.drop(1).joinToString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        HorizontalDivider()
        Text("So könnte es sich zeigen", style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold)
        emotion.examples.forEach { example ->
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(17.dp)
            ) {
                Text("„$example“", modifier = Modifier.fillMaxWidth().padding(17.dp),
                    style = MaterialTheme.typography.bodyMedium)
            }
        }

        Text("Ein Gedanke zum Nachspüren",
            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(emotion.reflectionQuestion, style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary)
        Text("Auch bekannt als: " + emotion.synonyms.joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        if (comparisons.isNotEmpty()) {
            HorizontalDivider()
            Text("Unterschiede entdecken", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Text("Was ähnelt sich, was ist anders?",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            comparisons.forEach { id ->
                val other = catalog.find(id)
                if (other != null) {
                    OutlinedButton(onClick = { onCompare(other.id) },
                        modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Outlined.CompareArrows, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("${emotion.name} und ${other.name}", modifier = Modifier.weight(1f))
                        Icon(Icons.Outlined.ChevronRight, contentDescription = null)
                    }
                }
            }
        }

        HorizontalDivider()
        Text("Verwandte Begriffe", style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold)
        emotion.related.forEach { id ->
            val other = catalog.find(id)
            if (other != null) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = { onRelated(other.id) }, modifier = Modifier.weight(1f)) {
                        Text(other.name)
                    }
                    IconButton(onClick = { onCompare(other.id) }) {
                        Icon(Icons.Outlined.CompareArrows,
                            contentDescription = "Mit ${other.name} vergleichen")
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
        Text("Arbeitsfassung: Die Erklärungen sind Anregungen, keine Diagnosen. Du entscheidest, was passt.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ComparisonScreen(state: AppUiState, catalog: EmotionCatalog) {
    val first = state.emotionId?.let(catalog::find)
    val second = state.comparisonId?.let(catalog::find)
    if (first == null || second == null) return
    val comparison = catalog.compare(first.id, second.id)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(21.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Tag("BEGRIFFE UNTERSCHEIDEN")
        Text("Ähnlich – und doch anders",
            style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        listOf(first, second).forEach { emotion ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(
                    Modifier.fillMaxWidth().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(emotion.name, style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold)
                    Text(emotion.explanation, style = MaterialTheme.typography.bodyMedium)
                    Text("Beispiel: ${emotion.example}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (comparison != null) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(19.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Was beide gemeinsam haben",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    Text(comparison.overlap)
                    HorizontalDivider()
                    Text(first.name, fontWeight = FontWeight.SemiBold)
                    Text(comparison.firstDistinction)
                    Text(second.name, fontWeight = FontWeight.SemiBold)
                    Text(comparison.secondDistinction)
                    HorizontalDivider()
                    Text("Frage zum Nachspüren", fontWeight = FontWeight.SemiBold)
                    Text(comparison.question, color = MaterialTheme.colorScheme.primary)
                }
            }
        } else {
            Surface(color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(18.dp)) {
                Text("Für dieses Begriffspaar ist noch kein fachlich formulierter Vergleich hinterlegt. Die Erklärungen oben können trotzdem beim Einordnen helfen.",
                    modifier = Modifier.padding(18.dp),
                    style = MaterialTheme.typography.bodyMedium)
            }
        }
        Text("Gefühle können sich überschneiden oder gleichzeitig auftreten. Dieser Vergleich soll Möglichkeiten zeigen, keine eindeutige Einordnung vorgeben.",
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
private fun SettingsScreen(catalog: EmotionCatalog, needs: NeedCatalog) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(21.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("Einstellungen & Informationen", style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        FeatureLine(Icons.Outlined.Lock, "Datenschutz", "Keine Anmeldung. Keine Internetberechtigung.")
        HorizontalDivider()
        Text("M3.3 · Bedürfnisatlas, Bedürfniskompass und Gefühlsnavigator", style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold)
        Text("Der offline verfügbare Gefühlsatlas umfasst ${catalog.emotions.size} Begriffe. Neu: ${needs.needs.size} Bedürfnisbegriffe, ${needs.comparisons.size} Vergleiche und ${needs.learningPaths.size} Lernpfade. Alle Inhalte sind redaktionelle Lernhilfen, keine wissenschaftlich abschließende Taxonomie. Check-in und Bedürfniskompass speichern keine persönlichen Angaben dauerhaft; weiterhin bleibt nur der Abschluss der Einführung gespeichert. Ein geschütztes Tagebuch folgt mit M4.",
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Emotionskompass ist ein Lern- und Reflexionswerkzeug, keine Therapie und keine medizinische Diagnose.",
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        HorizontalDivider()
        Text("Version 0.3.3-m3.3", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
