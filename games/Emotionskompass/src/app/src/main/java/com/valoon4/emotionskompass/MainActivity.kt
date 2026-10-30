package com.valoon4.emotionskompass

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
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
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.valoon4.emotionskompass.core.designsystem.EmotionskompassTheme
import com.valoon4.emotionskompass.core.model.Emotion
import com.valoon4.emotionskompass.core.model.EmotionCatalog
import com.valoon4.emotionskompass.core.model.EmotionKind
import com.valoon4.emotionskompass.core.model.LearningPath
import com.valoon4.emotionskompass.core.model.NeedCatalog
import com.valoon4.emotionskompass.core.lessons.LessonCatalog
import com.valoon4.emotionskompass.core.lessons.StrategyCaseCatalog
import com.valoon4.emotionskompass.core.strategies.StrategyCatalog
import com.valoon4.emotionskompass.core.learning.LearningEngine
import com.valoon4.emotionskompass.core.learning.LearningKind
import com.valoon4.emotionskompass.core.learning.LearningRepository
import com.valoon4.emotionskompass.core.learning.LearningViewModel
import com.valoon4.emotionskompass.core.learning.LearningWord
import com.valoon4.emotionskompass.core.journal.AppLockPreferences
import com.valoon4.emotionskompass.core.journal.EncryptedJournalRepository
import com.valoon4.emotionskompass.core.navigation.AtlasFilters
import com.valoon4.emotionskompass.core.navigation.AppUiState
import com.valoon4.emotionskompass.core.navigation.AppViewModel
import com.valoon4.emotionskompass.core.navigation.Destination
import com.valoon4.emotionskompass.core.navigation.DiscoveryAtlas
import com.valoon4.emotionskompass.core.navigation.MainSection
import com.valoon4.emotionskompass.core.preferences.OnboardingPreferences
import com.valoon4.emotionskompass.feature.checkin.CheckInScreen
import com.valoon4.emotionskompass.feature.needs.NeedsScreen
import com.valoon4.emotionskompass.feature.journal.JournalScreen
import com.valoon4.emotionskompass.feature.learning.LearningScreen
import com.valoon4.emotionskompass.feature.learning.KnowledgeWorldScreen
import com.valoon4.emotionskompass.feature.strategies.StrategyAtlasScreen
import java.time.LocalDate
import kotlinx.coroutines.launch

/**
 * M5: Native offline emotions/needs atlas and explicitly saved AES-GCM journal.
 * Data stays on device; security review and backup/export are separate release gates.
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
                val journal = remember { EncryptedJournalRepository(applicationContext) }
                val lockPreferences = remember { AppLockPreferences(applicationContext) }
                val learningRepository = remember { LearningRepository(applicationContext) }
                // A corrupt or future-incompatible bundled catalog must never
                // prevent access to the existing journal, check-in or atlas.
                val lessonCatalog = remember {
                    runCatching {
                        val original = assets.open("lessons/knowledge.de.v1.json")
                            .bufferedReader(Charsets.UTF_8).use { it.readText() }
                        val supplements = listOf(
                            "lessons/strategy-depth-basics.de.v1.json",
                            "lessons/strategy-depth-coping.de.v1.json",
                            "lessons/strategy-depth-flex.de.v1.json"
                        ).map { filename ->
                            assets.open(filename).bufferedReader(Charsets.UTF_8).use { it.readText() }
                        }
                        // Preserve the original library if a supplemental asset is damaged.
                        runCatching { LessonCatalog.fromJsonParts(original, supplements) }
                            .getOrElse { LessonCatalog.fromJson(original) }
                    }.getOrNull()
                }
                val strategyCatalog = remember {
                    runCatching {
                        val rootJson = assets.open("catalog/strategies.de.v1.json")
                            .bufferedReader(Charsets.UTF_8).use { it.readText() }
                        val partNames = listOf(
                            "catalog/strategies.situation.de.v1.json",
                            "catalog/strategies.regulation-a.de.v1.json",
                            "catalog/strategies.regulation-b.de.v1.json",
                            "catalog/strategies.cognition-a.de.v1.json",
                            "catalog/strategies.cognition-b.de.v1.json",
                            "catalog/strategies.social.de.v1.json",
                            "catalog/strategies.values.de.v1.json",
                            "catalog/strategies.recovery.de.v1.json",
                            "catalog/strategies.patterns.de.v1.json"
                        )
                        StrategyCatalog.fromJsonParts(rootJson, partNames.map { filename ->
                            assets.open(filename).bufferedReader(Charsets.UTF_8).use { it.readText() }
                        })
                    }.getOrNull()
                }
                val caseCatalog = remember(strategyCatalog) {
                    runCatching {
                        StrategyCaseCatalog.fromJsonParts(
                            listOf(
                                "lessons/strategy-cases-a.de.v1.json",
                                "lessons/strategy-cases-b.de.v1.json",
                                "lessons/strategy-cases-c.de.v1.json"
                            ).map { filename ->
                                assets.open(filename).bufferedReader(Charsets.UTF_8)
                                    .use { it.readText() }
                            },
                            strategyCatalog?.strategies?.map { it.id }?.toSet() ?: emptySet()
                        )
                    }.getOrNull()
                }
                if (completed) {
                    EmotionskompassApp(catalog, needCatalog, journal, lockPreferences, learningRepository, lessonCatalog, strategyCatalog, caseCatalog, this@MainActivity)
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
                FeatureLine(Icons.Outlined.MenuBook, "Gefühle und Bedürfnisse kennenlernen", "Begriffe, Unterschiede und Beispiele entdecken.")
                FeatureLine(Icons.Outlined.FavoriteBorder, "Dich selbst verstehen", "Gefühle und Erlebnisse achtsam reflektieren.")
                FeatureLine(Icons.Outlined.Lock, "Privat und ohne Konto", "Check-ins werden nur nach ausdrücklicher Zustimmung verschlüsselt gespeichert.")
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
    journal: EncryptedJournalRepository,
    lockPreferences: AppLockPreferences,
    learningRepository: LearningRepository,
    lessonCatalog: LessonCatalog?,
    strategyCatalog: StrategyCatalog?,
    caseCatalog: StrategyCaseCatalog?,
    hostActivity: ComponentActivity,
    vm: AppViewModel = viewModel(
        factory = AppViewModel.factory(
            catalog, needCatalog, journal, hostActivity.contentResolver,
            lockPreferences.enabled
        )
    ),
    learningVm: LearningViewModel = viewModel(factory = LearningViewModel.factory(learningRepository, lockPreferences.enabled))
) {
    val unlocked by vm.unlocked.collectAsState()
    val lockEnabled by vm.lockEnabled.collectAsState()
    var credentialError by remember { mutableStateOf<String?>(null) }
    var requestedAction by remember { mutableStateOf("unlock") }
    val credentialLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                when (requestedAction) {
                    "enable" -> {
                        lockPreferences.setEnabled(true)
                        vm.setLockEnabled(true)
                    }
                    "disable" -> {
                        lockPreferences.setEnabled(false)
                        vm.setLockEnabled(false)
                    }
                }
                vm.unlockJournal()
                learningVm.unlock()
                credentialError = null
            } catch (e: Exception) {
                credentialError = e.message ?: "Gerätesperre konnte nicht geändert werden."
            }
        } else {
            credentialError = "Gerätebestätigung abgebrochen. Die bisherige Einstellung bleibt erhalten."
        }
        requestedAction = "unlock"
    }
    val requestCredential: (String) -> Unit = { action ->
        val manager = hostActivity.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (!manager.isDeviceSecure) {
            credentialError = "Richte zuerst unter Android-Einstellungen eine Geräte-PIN, ein Muster oder ein Passwort ein."
        } else {
            val intent = manager.createConfirmDeviceCredentialIntent(
                "Emotionskompass entsperren", "Bestätige die Geräteanmeldung zum Schutz deines Tagebuchs."
            )
            if (intent == null) {
                credentialError = "Android konnte keine Gerätebestätigung öffnen."
            } else {
                requestedAction = action
                credentialError = null
                credentialLauncher.launch(intent)
            }
        }
    }
    DisposableEffect(hostActivity, vm) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                vm.onAppBackgrounded()
                if (!vm.unlocked.value) learningVm.lock()
            }
        }
        hostActivity.lifecycle.addObserver(observer)
        onDispose { hostActivity.lifecycle.removeObserver(observer) }
    }
    val state by vm.state.collectAsState()
    val filters by vm.filters.collectAsState()
    val checkIn by vm.checkIn.collectAsState()
    val diary by vm.journalState.collectAsState()
    val learning by learningVm.state.collectAsState()
    val editingId by vm.editingEntryId.collectAsState()
    var confirmDiscard by remember { mutableStateOf(false) }
    var selectedStrategyId by rememberSaveable { mutableStateOf<String?>(null) }
    var strategyHistory by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var strategyReturnTarget by rememberSaveable { mutableStateOf<String?>(null) }
    var returnCaseId by rememberSaveable { mutableStateOf<String?>(null) }
    var requestedNeedId by rememberSaveable { mutableStateOf<String?>(null) }
    var needReturnToStrategy by rememberSaveable { mutableStateOf(false) }
    var requestedCaseId by rememberSaveable { mutableStateOf<String?>(null) }
    var requestedKnowledgeArticleId by rememberSaveable { mutableStateOf<String?>(null) }
    var discoveryAtlas by rememberSaveable { mutableStateOf(DiscoveryAtlas.GEFUEHLE) }
    val mainScreenStates = rememberSaveableStateHolder()
    val atlasStates = rememberSaveableStateHolder()
    val strategyTitles = remember(strategyCatalog) {
        strategyCatalog?.strategies?.associate { it.id to it.title } ?: emptyMap()
    }
    val openStrategy: (String, String?) -> Unit = { id, origin ->
        strategyHistory = emptyList()
        selectedStrategyId = id
        strategyReturnTarget = origin
        discoveryAtlas = DiscoveryAtlas.STRATEGIEN
        vm.selectSection(MainSection.ENTDECKEN)
    }
    val openStrategyOverview: () -> Unit = {
        selectedStrategyId = null
        strategyHistory = emptyList()
        strategyReturnTarget = null
        discoveryAtlas = DiscoveryAtlas.STRATEGIEN
        vm.selectSection(MainSection.ENTDECKEN)
    }
    val returnFromStrategy: () -> Unit = {
        when (strategyReturnTarget) {
            "need" -> {
                selectedStrategyId = null
                strategyHistory = emptyList()
                strategyReturnTarget = null
                discoveryAtlas = DiscoveryAtlas.BEDUERFNISSE
                vm.selectSection(MainSection.ENTDECKEN)
            }
            "case" -> {
                selectedStrategyId = null
                strategyHistory = emptyList()
                strategyReturnTarget = null
                requestedCaseId = returnCaseId
                returnCaseId = null
                vm.selectSection(MainSection.WISSEN)
            }
            else -> selectedStrategyId = null
        }
    }

    // Check-in content must not appear in Android screenshots or recent-app previews.
    val privateScreen = !unlocked || state.destination == Destination.CHECKIN ||
        (state.destination == Destination.MAIN && state.section == MainSection.TAGEBUCH)
    DisposableEffect(hostActivity, privateScreen) {
        if (privateScreen) {
            hostActivity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        onDispose {
            if (privateScreen) {
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
    if (!unlocked) {
        Surface(modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background) {
            // With 200%+ Android text scaling the unlock action must remain
            // reachable on small screens, in landscape and with TalkBack.
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(28.dp),
                verticalArrangement = Arrangement.Center) {
                Text("Emotionskompass ist gesperrt",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Text("Bestätige deine Geräte-PIN, dein Muster oder Passwort. Deine Tagebucheinträge werden erst danach aus der verschlüsselten Datei geladen.")
                Spacer(Modifier.height(20.dp))
                if (credentialError != null) {
                    Text(credentialError!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                }
                Button(onClick = { requestCredential("unlock") },
                    modifier = Modifier.fillMaxWidth()) {
                    Text("Mit Android-Gerätesperre entsperren")
                }
            }
        }
        return
    }
    BackHandler(enabled = state.destination != Destination.MAIN) { requestBack() }
    val navItems = listOf(
        Triple(MainSection.START, "Start", Icons.Outlined.Home),
        Triple(MainSection.ENTDECKEN, "Entdecken", Icons.Outlined.Explore),
        Triple(MainSection.WISSEN, "Wissen", Icons.Outlined.MenuBook),
        Triple(MainSection.TAGEBUCH, "Tagebuch", Icons.Outlined.NoteAlt),
        Triple(MainSection.RUECKBLICK, "Rückblick", Icons.Outlined.Insights)
    )
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp && maxHeight >= 480.dp
        Row(Modifier.fillMaxSize()) {
            if (useRail && state.destination != Destination.CHECKIN) {
                NavigationRail(modifier = Modifier.width(96.dp)) {
                    navItems.forEach { (section, title, icon) ->
                        NavigationRailItem(
                            selected = state.section == section,
                            onClick = { vm.selectSection(section) },
                            icon = { Icon(icon, contentDescription = null) },
                            label = { Text(title, maxLines = 2) },
                            alwaysShowLabel = true
                        )
                    }
                }
            }
    Scaffold(
        modifier = Modifier.weight(1f),
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
            if (!useRail && state.destination != Destination.CHECKIN) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    navItems.forEach { (section, title, icon) ->
                        NavigationBarItem(
                            selected = state.section == section,
                            onClick = { vm.selectSection(section) },
                            icon = { Icon(icon, contentDescription = null) },
                            label = { Text(title, maxLines = 2, textAlign = TextAlign.Center) },
                            alwaysShowLabel = true
                        )
                    }
                }
            }
        }
    ) { inset ->
        Box(Modifier.fillMaxSize().padding(inset)) {
            when (state.destination) {
                Destination.MAIN -> mainScreenStates.SaveableStateProvider(state.section.name) {
                    when (state.section) {
                    MainSection.START -> HomeScreen(
                        termsCount = catalog.emotions.size,
                        needsCount = needCatalog.needs.size,
                        strategyCount = strategyCatalog?.strategies?.size ?: 0,
                        onExplore = {
                            strategyReturnTarget = null
                            needReturnToStrategy = false
                            discoveryAtlas = DiscoveryAtlas.GEFUEHLE
                            vm.selectSection(MainSection.ENTDECKEN)
                        },
                        onNeeds = {
                            strategyReturnTarget = null
                            needReturnToStrategy = false
                            discoveryAtlas = DiscoveryAtlas.BEDUERFNISSE
                            vm.selectSection(MainSection.ENTDECKEN)
                        },
                        onStrategies = openStrategyOverview,
                        onCheckin = vm::openCheckin,
                        onWord = vm::openLearning,
                        onKnowledge = { vm.selectSection(MainSection.WISSEN) },
                        daily = LearningEngine.daily(catalog, needCatalog, LocalDate.now())
                    )
                    MainSection.ENTDECKEN -> Column(Modifier.fillMaxSize()) {
                        BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                            val vertical = maxWidth < 320.dp || LocalDensity.current.fontScale > 1.3f
                            if (vertical) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    DiscoveryAtlas.entries.forEach { atlas ->
                                        FilterChip(
                                            selected = discoveryAtlas == atlas,
                                            onClick = {
                                                discoveryAtlas = atlas
                                                if (atlas != DiscoveryAtlas.STRATEGIEN) strategyReturnTarget = null
                                                if (atlas != DiscoveryAtlas.BEDUERFNISSE) needReturnToStrategy = false
                                            },
                                            label = { Text(atlas.label) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            } else {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()) {
                                    DiscoveryAtlas.entries.forEach { atlas ->
                                        FilterChip(
                                            selected = discoveryAtlas == atlas,
                                            onClick = {
                                                discoveryAtlas = atlas
                                                if (atlas != DiscoveryAtlas.STRATEGIEN) strategyReturnTarget = null
                                                if (atlas != DiscoveryAtlas.BEDUERFNISSE) needReturnToStrategy = false
                                            },
                                            label = {
                                                Text(atlas.label, textAlign = TextAlign.Center,
                                                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                        Box(Modifier.weight(1f)) {
                            atlasStates.SaveableStateProvider(discoveryAtlas.name) {
                                when (discoveryAtlas) {
                                    DiscoveryAtlas.GEFUEHLE -> AtlasScreen(
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
                                    DiscoveryAtlas.BEDUERFNISSE -> NeedsScreen(
                                        needCatalog,
                                        favorites = learning.progress.favorites,
                                        seen = learning.progress.seen,
                                        canSave = !learning.busy && learning.error == null,
                                        onFavorite = learningVm::toggleFavorite,
                                        onSeen = learningVm::markSeen,
                                        strategyCatalog = strategyCatalog,
                                        onStrategy = { openStrategy(it, "need") },
                                        onOpenStrategyAtlas = openStrategyOverview,
                                        requestedNeedId = requestedNeedId,
                                        onRequestConsumed = { requestedNeedId = null },
                                        onReturnToStrategy = if (needReturnToStrategy) {
                                            {
                                                needReturnToStrategy = false
                                                discoveryAtlas = DiscoveryAtlas.STRATEGIEN
                                                vm.selectSection(MainSection.ENTDECKEN)
                                            }
                                        } else null
                                    )
                                    DiscoveryAtlas.STRATEGIEN -> if (strategyCatalog != null) {
                                        StrategyAtlasScreen(
                                            catalog = strategyCatalog,
                                            selectedId = selectedStrategyId,
                                            onSelect = { id ->
                                                val previous = selectedStrategyId
                                                if (previous != null && previous != id) {
                                                    strategyHistory = (strategyHistory + previous).takeLast(12)
                                                }
                                                selectedStrategyId = id
                                            },
                                            onBackToList = {
                                                if (strategyHistory.isNotEmpty()) {
                                                    selectedStrategyId = strategyHistory.last()
                                                    strategyHistory = strategyHistory.dropLast(1)
                                                } else {
                                                    selectedStrategyId = null
                                                    strategyReturnTarget = null
                                                }
                                            },
                                            onKnowledgeArticle = { id ->
                                                requestedKnowledgeArticleId = id
                                                vm.openKnowledgeWorld()
                                            },
                                            needCatalog = needCatalog,
                                            lessonCatalog = lessonCatalog,
                                            caseCatalog = caseCatalog,
                                            onNeed = { id ->
                                                requestedNeedId = id
                                                needReturnToStrategy = true
                                                discoveryAtlas = DiscoveryAtlas.BEDUERFNISSE
                                                vm.selectSection(MainSection.ENTDECKEN)
                                            },
                                            onPracticeCase = { id ->
                                                requestedCaseId = id
                                                vm.openKnowledgeWorld()
                                            },
                                            onReturnToSource = if (strategyReturnTarget != null) returnFromStrategy else null,
                                            sourceLabel = when (strategyReturnTarget) {
                                                "need" -> "Zum Bedürfnis"
                                                "case" -> "Zum Praxisfall"
                                                else -> ""
                                            },
                                            hasPreviousStrategy = strategyHistory.isNotEmpty()
                                        )
                                    } else {
                                        Text("Der Strategieatlas konnte nicht geladen werden. Die anderen Atlanten bleiben verfügbar.",
                                            modifier = Modifier.padding(20.dp))
                                    }
                                }
                            }
                        }
                    }
                    MainSection.WISSEN -> if (lessonCatalog != null) {
                        KnowledgeWorldScreen(
                            catalog = lessonCatalog,
                            ui = learning,
                            onBookmark = learningVm::toggleArticleBookmark,
                            onRead = learningVm::markArticleRead,
                            onLearning = vm::openLearning,
                            caseCatalog = caseCatalog,
                            strategyTitles = strategyTitles,
                            onStrategyDetail = { id, caseId ->
                                returnCaseId = caseId
                                openStrategy(id, "case")
                            },
                            requestedCaseId = requestedCaseId,
                            onCaseRequestConsumed = { requestedCaseId = null },
                            onStrategies = openStrategyOverview
                        )
                    } else {
                        Text("Die Wissenswelt konnte nicht geladen werden. Deine übrigen Bereiche bleiben verfügbar.",
                            modifier = Modifier.padding(20.dp))
                    }
                    MainSection.TAGEBUCH -> JournalScreen(
                        state = diary,
                        emotions = catalog,
                        needs = needCatalog,
                        onStart = { vm.openCheckin() },
                        onEdit = vm::editJournalEntry,
                        onDelete = vm::deleteJournalEntry,
                        onDeleteAll = vm::deleteEntireJournal,
                        onReload = vm::reloadJournal,
                        lockEnabled = lockEnabled,
                        onToggleLock = {
                            requestCredential(if (lockEnabled) "disable" else "enable")
                        },
                        onExport = vm::exportJournal,
                        onImport = vm::importJournal,
                        onSystemPicker = vm::setSystemPickerInProgress
                    )
                    MainSection.RUECKBLICK -> ComingSoonScreen(
                        icon = Icons.Outlined.Insights,
                        title = "Dein Rückblick",
                        subtitle = "Hier werden später persönliche Muster sichtbar – ohne Noten, Diagnosen oder Leistungsdruck.",
                        milestone = "Geplant für M7"
                    )
                    }
                }
                Destination.EMOTION -> {
                    val emotion = state.emotionId?.let(catalog::find)
                    if (emotion != null) {
                        EmotionDetailScreen(
                            emotion,
                            catalog = catalog,
                            onRelated = vm::openEmotion,
                            onCompare = vm::compareWith,
                            onReflect = { vm.openCheckin(emotion.id) },
                            favorite = "e:${emotion.id}" in learning.progress.favorites,
                            seen = "e:${emotion.id}" in learning.progress.seen,
                            canSave = !learning.busy && learning.error == null,
                            onFavorite = { learningVm.toggleFavorite("e:${emotion.id}") },
                            onSeen = { learningVm.markSeen("e:${emotion.id}") }
                        )
                    }
                }
                Destination.VERGLEICH -> ComparisonScreen(state, catalog)
                Destination.LEARN_CARDS -> LearningCardsScreen(catalog, vm::openEmotion)
                Destination.WISSENSWELT -> if (lessonCatalog != null) {
                    KnowledgeWorldScreen(
                        catalog = lessonCatalog,
                        ui = learning,
                        onBookmark = learningVm::toggleArticleBookmark,
                        onRead = learningVm::markArticleRead,
                        onStrategies = openStrategyOverview,
                        onLearning = vm::openLearning,
                        caseCatalog = caseCatalog,
                        strategyTitles = strategyTitles,
                        onStrategyDetail = { id, caseId ->
                            returnCaseId = caseId
                            openStrategy(id, "case")
                        },
                        requestedCaseId = requestedCaseId,
                        onCaseRequestConsumed = { requestedCaseId = null },
                        onReturnToSource = { vm.back() },
                        returnLabel = if (selectedStrategyId == null) "Zur Atlasübersicht" else "Zur Strategie",
                        requestedArticleId = requestedKnowledgeArticleId,
                        onRequestConsumed = { requestedKnowledgeArticleId = null }
                    )
                } else {
                    Text("Die Wissenswelt konnte nicht geladen werden. Dein Tagebuch und die anderen Bereiche bleiben verfügbar.",
                        modifier = Modifier.padding(20.dp))
                }
                Destination.LEARNING -> LearningScreen(
                    emotions = catalog,
                    needs = needCatalog,
                    lessonCatalog = lessonCatalog,
                    ui = learning,
                    onFavorite = learningVm::toggleFavorite,
                    onSeen = learningVm::markSeen,
                    onReview = learningVm::review,
                    onStation = learningVm::completeStation,
                    onArticleBookmark = learningVm::toggleArticleBookmark,
                    onArticleRead = learningVm::markArticleRead,
                    onReset = learningVm::reset,
                    onReload = learningVm::reload,
                    onOpenEmotion = vm::openEmotion
                )
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
                    onClose = requestDiscard,
                    onSave = vm::saveCheckIn,
                    saveBusy = diary.busy,
                    saveError = diary.error,
                    isEditing = editingId != null
                )
                Destination.SETTINGS -> SettingsScreen(catalog, needCatalog)
            }
        }
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
private fun HomeScreen(termsCount: Int, needsCount: Int, strategyCount: Int,
    onExplore: () -> Unit, onNeeds: () -> Unit, onStrategies: () -> Unit,
    onCheckin: () -> Unit, onWord: () -> Unit, onKnowledge: () -> Unit,
    daily: com.valoon4.emotionskompass.core.learning.DailyWords) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(21.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Text("DEIN EMOTIONSKOMPASS", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary, letterSpacing = 1.2.sp)
        Text("Gefühle verstehen. Eigene Wege finden.",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold)
        Text("Entdecke Gefühle, Bedürfnisse und Strategien – in deinem Tempo.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        ActionCard(
            icon = Icons.Outlined.FavoriteBorder,
            title = "Wie geht es dir gerade?",
            description = "Nimm dir einen Moment für dich. Alles darf, nichts muss.",
            footnote = "Freiwilligen Check-in starten",
            emphasized = true,
            onClick = onCheckin
        )
        Text("Drei Atlanten entdecken",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold)
        val largeText = LocalDensity.current.fontScale > 1.3f
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            if (maxWidth < 340.dp || largeText) {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    AtlasShortcut(Icons.Outlined.FavoriteBorder, "Gefühle",
                        "$termsCount Begriffe", onExplore, Modifier.fillMaxWidth())
                    AtlasShortcut(Icons.Outlined.Explore, "Bedürfnisse",
                        "$needsCount Begriffe", onNeeds, Modifier.fillMaxWidth())
                    AtlasShortcut(Icons.Outlined.AutoAwesome, "Strategien",
                        "$strategyCount Karten", onStrategies, Modifier.fillMaxWidth())
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    AtlasShortcut(Icons.Outlined.FavoriteBorder, "Gefühle",
                        "$termsCount Begriffe", onExplore, Modifier.weight(1f))
                    AtlasShortcut(Icons.Outlined.Explore, "Bedürfnisse",
                        "$needsCount Begriffe", onNeeds, Modifier.weight(1f))
                    AtlasShortcut(Icons.Outlined.AutoAwesome, "Strategien",
                        "$strategyCount Karten", onStrategies, Modifier.weight(1f))
                }
            }
        }
        ActionCard(
            icon = Icons.Outlined.MenuBook,
            title = "Wissenswelt",
            description = "Artikel, freie Lernreihen und Übungen – ohne Leistungsdruck.",
            footnote = "Wissen entdecken · komplett offline",
            onClick = onKnowledge
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
                    Text(daily.emotion.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                    Text(daily.emotion.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall)
                }
                Icon(Icons.Outlined.ChevronRight, contentDescription = null)
            }
        }
        Card(onClick = onNeeds, modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(17.dp)) {
                Text("Bedürfnis des Tages: ${daily.need.name}",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(daily.need.description, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text("Alles offline · $termsCount Gefühle · $needsCount Bedürfnisse · $strategyCount Strategiekarten · geschütztes Tagebuch.",
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AtlasShortcut(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.heightIn(min = 110.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.fillMaxWidth().padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(27.dp))
            Text(title, style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            Text(subtitle, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center)
        }
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
    onReflect: () -> Unit,
    favorite: Boolean,
    seen: Boolean,
    canSave: Boolean,
    onFavorite: () -> Unit,
    onSeen: () -> Unit
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
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onFavorite, enabled = canSave) {
                Text(if (favorite) "★ Gespeichert" else "☆ Merken")
            }
            TextButton(onClick = onSeen, enabled = canSave && !seen) {
                Text(if (seen) "✓ Entdeckt" else "Als entdeckt markieren")
            }
        }
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
        Text("M6 · Lernen, Favoriten und sichere Daten", style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold)
        Text("Der Gefühlsatlas enthält ${catalog.emotions.size} Begriffe, der Bedürfnisatlas ${needs.needs.size}. Das Tagebuch bleibt mit Android Keystore verschlüsselt. Neu in M5 sind eine freiwillige App-Sperre mit Geräte-PIN/Muster/Passwort und ausdrücklich passphrasegeschützte Export-/Importdateien. Eine Sicherung kann nach Bestätigung auf einem anderen Gerät wiederhergestellt werden; ohne Sicherung können Deinstallation und Geräteverlust deine Einträge endgültig vernichten. Keine automatische Cloud-Synchronisation. Der Bedürfniskompass bleibt flüchtig.",
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Emotionskompass ist ein Lern- und Reflexionswerkzeug, keine Therapie und keine medizinische Diagnose.",
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        HorizontalDivider()
        Text("Version 0.6.2.1-m6.2.1", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
