package com.valoon4.communicationtrainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valoon4.communicationtrainer.data.ChatUiState
import com.valoon4.communicationtrainer.data.ChatViewModel
import com.valoon4.communicationtrainer.data.ConversationEntity
import com.valoon4.communicationtrainer.data.MessageEntity
import com.valoon4.communicationtrainer.data.ThemeMode
import com.valoon4.communicationtrainer.inference.SdStorageInspection
import com.valoon4.communicationtrainer.inference.SdStoragePreflight
import com.valoon4.communicationtrainer.inference.SdStorageStatus
import com.valoon4.communicationtrainer.inference.QwenSdProbeCard
import com.valoon4.communicationtrainer.ui.theme.AuroraPulseAccent
import com.valoon4.communicationtrainer.ui.theme.AuroraPulseTheme

class MainActivity : ComponentActivity() {
    private val storageViewModel: ChatViewModel by viewModels {
        ChatViewModel.Factory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { CommunicationTrainerApp(storageViewModel) }
    }
}

@Composable
fun CommunicationTrainerApp(viewModel: ChatViewModel) {
    val state by viewModel.state.collectAsState()
    var selectedTab by rememberSaveable { mutableStateOf(MainSection.CHATS.name) }
    val section = MainSection.valueOf(selectedTab)

    AuroraPulseTheme(mode = state.themeMode) {
        val scheme = MaterialTheme.colorScheme
        Scaffold(
            containerColor = scheme.background,
            bottomBar = {
                NavigationBar(containerColor = scheme.surface, tonalElevation = 0.dp) {
                    MainSection.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = tab == section,
                            onClick = { selectedTab = tab.name },
                            icon = {
                                Text(
                                    if (tab == MainSection.CHATS) "◉" else "✦",
                                    fontSize = 22.sp,
                                    modifier = Modifier.clearAndSetSemantics {}
                                )
                            },
                            label = { Text(tab.title) },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = scheme.primary,
                                selectedTextColor = scheme.primary,
                                indicatorColor = scheme.primaryContainer,
                                unselectedIconColor = scheme.onSurfaceVariant,
                                unselectedTextColor = scheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        ) { padding ->
            when (section) {
                MainSection.CHATS -> {
                    if (state.selectedConversationId == null) {
                        ChatOverview(state, viewModel, padding)
                    } else {
                        ChatConversation(state, viewModel, padding)
                    }
                }
                MainSection.PRACTICE -> PracticeOverview(state, viewModel, padding)
            }
        }
    }
}

@Composable
private fun BrandHeader() {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "CommunicationTrainer",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = scheme.onBackground
        )
        Surface(
            color = scheme.secondaryContainer,
            shape = RoundedCornerShape(30.dp)
        ) {
            Text(
                "✦ Aurora Pulse",
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
            )
        }
    }
}

@Composable
private fun DemoHero(title: String, description: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        AuroraPulseAccent.HeroStart,
                        AuroraPulseAccent.HeroMiddle,
                        AuroraPulseAccent.HeroEnd
                    )
                )
            )
            .padding(22.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(color = AuroraPulseAccent.HeroStart, shape = RoundedCornerShape(30.dp)) {
                Text(
                    "D03 · LOKALE SIMULATION",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                )
            }
            Text(
                title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(description, color = Color.White, style = MaterialTheme.typography.bodyMedium)
            Text(
                "Keine KI · Keine Netzwerkübertragung",
                color = AuroraPulseAccent.HeroMint,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun ChatOverview(state: ChatUiState, viewModel: ChatViewModel, padding: PaddingValues) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        BrandHeader()
        DemoHero(
            "Gespräche ausprobieren",
            "Starte einen lokalen Testchat. Der Testpartner antwortet mit festen Textbausteinen – nicht mit KI."
        )
        Text(
            "Deine Gespräche",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onBackground
        )
        Button(
            onClick = viewModel::createConversation,
            enabled = !state.isBusy,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Neues Testgespräch") }

        if (state.conversations.isEmpty()) {
            Surface(
                color = scheme.primaryContainer,
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    "Noch kein Gespräch vorhanden. Starte mit deiner ersten Nachricht.",
                    color = scheme.onPrimaryContainer,
                    modifier = Modifier.padding(18.dp)
                )
            }
        }
        state.conversations.forEach { conversation ->
            ConversationCard(conversation, { viewModel.openConversation(conversation.id) })
        }
        if (state.error != null) {
            Text(state.error, style = MaterialTheme.typography.bodySmall, color = scheme.error)
        }
    }
}

@Composable
private fun ConversationCard(conversation: ConversationEntity, open: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(19.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        border = BorderStroke(1.dp, scheme.outline.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(conversation.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "Freies Testgespräch · Simulierter Gesprächspartner",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant
            )
            TextButton(onClick = open) { Text("Gespräch öffnen") }
        }
    }
}

@Composable
private fun ChatConversation(state: ChatUiState, viewModel: ChatViewModel, padding: PaddingValues) {
    val scheme = MaterialTheme.colorScheme
    val activeId = state.selectedConversationId
    var confirmDelete by remember(activeId) { mutableStateOf(false) }
    val listState = rememberLazyListState()
    LaunchedEffect(activeId, state.messages.size) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.lastIndex)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .imePadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = viewModel::closeConversation, enabled = !state.isSending) {
                Text("‹ Alle Gespräche")
            }
            TextButton(onClick = { confirmDelete = true }, enabled = !state.isSending) {
                Text("Löschen", color = scheme.error)
            }
        }
        Text(
            "Testpartner · Simulation",
            style = MaterialTheme.typography.titleLarge,
            color = scheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Surface(
            color = scheme.tertiaryContainer,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "Feste Demo-Antworten, keine echte KI und keine reale Person.",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onTertiaryContainer,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
            )
        }
        if (state.isLoadingDraft) {
            Text("Gespräch wird geladen …", color = scheme.onSurfaceVariant)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                if (state.messages.isEmpty()) {
                    item(key = "empty") {
                        Text(
                            "Beginne mit einer Nachricht. Das Gespräch bleibt lokal gespeichert.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
                items(state.messages, key = { it.id }) { message ->
                    MessageBubble(
                        message = message,
                        isProcessing = state.respondingMessageId == message.id,
                        retry = { viewModel.retryMockReply(message.id) }
                    )
                }
            }
        }

        if (state.error != null) {
            Text(
                state.error,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.error
            )
        }
        val hasPending = state.messages.any {
            it.role == "user" && it.status in setOf("queued", "failed")
        }
        val canSend = !state.isLoadingDraft && !state.isBusy && !state.isSending &&
            state.isDraftSaved && state.draftText.isNotBlank() && !hasPending &&
            state.respondingMessageId == null && state.error == null

        OutlinedTextField(
            value = state.draftText,
            onValueChange = viewModel::editDraft,
            enabled = !state.isBusy && !state.isSending && !state.isLoadingDraft,
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4,
            shape = RoundedCornerShape(18.dp),
            label = { Text("Deine Nachricht") },
            placeholder = { Text("Schreib, was du sagen möchtest …") }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                when {
                    state.isLoadingDraft -> "Lade Entwurf …"
                    state.isSending -> "Wird gesendet …"
                    state.error != null -> "Bitte Fehler beachten"
                    state.isDraftSaved -> "Entwurf gespeichert"
                    else -> "Wird lokal gespeichert …"
                },
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = viewModel::sendMessage, enabled = canSend) {
                Text("Senden")
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = state.failNextMockReply,
                onCheckedChange = viewModel::setFailNextMockReply
            )
            Text(
                "Nächste Testantwort absichtlich scheitern lassen",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant
            )
        }
    }

    if (confirmDelete && activeId != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Gespräch löschen?") },
            text = {
                Text(
                    "Dieses Testgespräch, alle Nachrichten und sein Entwurf werden nur " +
                        "aus dem lokalen App-Speicher entfernt. Die Aktion lässt sich nicht rückgängig machen."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.deleteConversation()
                }) { Text("Endgültig löschen", color = scheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Abbrechen") }
            }
        )
    }
}

@Composable
private fun MessageBubble(message: MessageEntity, isProcessing: Boolean, retry: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val isMine = message.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.widthIn(max = 340.dp),
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                if (isMine) "DU" else "TESTPARTNER · SIMULATION",
                color = scheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall
            )
            Surface(
                color = if (isMine) scheme.primaryContainer else scheme.surface,
                shape = RoundedCornerShape(19.dp),
                border = if (isMine) null else BorderStroke(1.dp, scheme.outline.copy(alpha = 0.45f))
            ) {
                Text(
                    message.content,
                    color = if (isMine) scheme.onPrimaryContainer else scheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(14.dp)
                )
            }
            if (isMine) {
                val statusLabel = when {
                    isProcessing -> "Testantwort wird erstellt …"
                    message.status == "queued" -> "Testantwort steht noch aus"
                    message.status == "failed" -> "Testantwort fehlgeschlagen"
                    message.status == "sent" -> "Gespeichert · Testantwort vorhanden"
                    else -> "Lokal gespeichert"
                }
                Text(
                    statusLabel,
                    color = if (message.status == "failed") scheme.error else scheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                if (!isProcessing && message.status in setOf("queued", "failed")) {
                    TextButton(onClick = retry) { Text("Testantwort erneut versuchen") }
                }
            }
        }
    }
}

@Composable
private fun PracticeOverview(state: ChatUiState, viewModel: ChatViewModel, padding: PaddingValues) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    var sdInspection by remember { mutableStateOf<SdStorageInspection?>(null) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        BrandHeader()
        DemoHero(
            "Kommunikation üben",
            "Die ersten Alltagssituationen und echten KI-Charaktere folgen in späteren Schritten."
        )
        Surface(color = scheme.surface, shape = RoundedCornerShape(18.dp)) {
            Text(
                "D03: Unter „Chats“ kannst du jetzt Nachrichten mit einem lokalen Testpartner austauschen. " +
                    "Die Antworten stammen ausschließlich aus festen Beispielen.",
                color = scheme.onSurface,
                modifier = Modifier.padding(18.dp)
            )
        }
        Surface(color = scheme.surface, shape = RoundedCornerShape(18.dp)) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "D04 · SD-Speicherprüfung",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Prüft nur, ob ein app-eigener Speicherort auf einer eingehängten SD-Karte " +
                        "sichtbar ist und wie viel Platz dort frei ist. " +
                        "Es werden keine Dateien heruntergeladen oder Modelle gestartet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
                OutlinedButton(
                    onClick = { sdInspection = SdStoragePreflight.inspect(context) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("SD-Speicher jetzt prüfen")
                }
                sdInspection?.let { inspection ->
                    Text(
                        text = when (inspection.status) {
                            SdStorageStatus.AVAILABLE_FOR_STORAGE_PROBE ->
                                "SD-Speicherort erkannt. Freier Platz: " +
                                    SdStoragePreflight.gib(inspection.sdFreeBytes ?: 0L) +
                                    ". Nur Speicherprüfung bestanden – Modellladen und KI-Leistung ungetestet."
                            SdStorageStatus.NOT_DETECTED ->
                                "Keine entnehmbare SD-Karte im app-eigenen Speicher erkannt. " +
                                    "Es wurde nichts heruntergeladen."
                            SdStorageStatus.NOT_MOUNTED ->
                                "SD-Karte nicht eingehängt oder nicht zugänglich. Kein Modellstart."
                            SdStorageStatus.NOT_ENOUGH_SPACE ->
                                "SD-Speicherort erkannt, aber für den geplanten Test zu wenig frei: " +
                                    SdStoragePreflight.gib(inspection.sdFreeBytes ?: 0L) +
                                    ". Als vorsichtige Testreserve empfehlen wir 2 GiB."
                            SdStorageStatus.READ_FAILED ->
                                "Freier Speicherplatz konnte nicht bestimmt werden. Keine Dateinutzung."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (inspection.status == SdStorageStatus.AVAILABLE_FOR_STORAGE_PROBE)
                            scheme.onSurface else scheme.onSurfaceVariant
                    )
                    inspection.internalFreeBytes?.let { bytes ->
                        Text(
                            "Interner freier App-Speicher: " + SdStoragePreflight.gib(bytes) +
                                ". Diese Prüfung verschiebt keine vorhandenen Chats.",
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        QwenSdProbeCard()
        Surface(color = scheme.surface, shape = RoundedCornerShape(18.dp)) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Darstellung",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        if (mode == state.themeMode) {
                            Button(
                                onClick = { viewModel.setThemeMode(mode) },
                                modifier = Modifier.weight(1f)
                            ) { Text(mode.label) }
                        } else {
                            OutlinedButton(
                                onClick = { viewModel.setThemeMode(mode) },
                                modifier = Modifier.weight(1f)
                            ) { Text(mode.label) }
                        }
                    }
                }
            }
        }
    }
}
