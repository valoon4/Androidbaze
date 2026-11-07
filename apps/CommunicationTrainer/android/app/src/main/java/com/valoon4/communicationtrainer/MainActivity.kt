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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valoon4.communicationtrainer.data.ChatUiState
import com.valoon4.communicationtrainer.data.ChatViewModel
import com.valoon4.communicationtrainer.data.ThemeMode
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
    val storageState by viewModel.state.collectAsState()
    var selectedSection by rememberSaveable { mutableStateOf(MainSection.CHATS.name) }
    val activeSection = MainSection.valueOf(selectedSection)

    AuroraPulseTheme(mode = storageState.themeMode) {
        val scheme = MaterialTheme.colorScheme
        Scaffold(
            containerColor = scheme.background,
            bottomBar = {
                NavigationBar(containerColor = scheme.surface, tonalElevation = 0.dp) {
                    MainSection.entries.forEach { section ->
                        val isSelected = section == activeSection
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedSection = section.name },
                            icon = {
                                Text(
                                    text = if (section == MainSection.CHATS) "◉" else "✦",
                                    fontSize = 23.sp,
                                    modifier = Modifier.clearAndSetSemantics {}
                                )
                            },
                            label = { Text(section.title) },
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
        ) { innerPadding ->
            SectionContent(activeSection, innerPadding, storageState, viewModel)
        }
    }
}

@Composable
private fun SectionContent(
    section: MainSection,
    innerPadding: PaddingValues,
    storageState: ChatUiState,
    viewModel: ChatViewModel
) {
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "CommunicationTrainer",
                style = MaterialTheme.typography.titleMedium,
                color = scheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Surface(
                shape = RoundedCornerShape(50),
                color = scheme.secondaryContainer
            ) {
                Text(
                    text = "✦ Aurora Pulse",
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        AuroraHero(section)

        Text(
            text = if (section == MainSection.CHATS) "Ein Ort für neue Gespräche" else "Schritt für Schritt sicherer werden",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onBackground
        )

        if (section == MainSection.CHATS) {
            DraftWorkspace(storageState, viewModel)
        } else {
            InfoCard(
                eyebrow = "ÜBUNGSSITUATIONEN",
                heading = "Alltag entspannt ausprobieren",
                description = "Ab D03 kommen die ersten real benutzbaren Übungsgespräche hinzu.",
                symbol = "✦",
                accent = scheme.secondary,
                accentContainer = scheme.secondaryContainer,
                accentOnContainer = scheme.onSecondaryContainer
            )
            InfoCard(
                eyebrow = "DEIN TEMPO",
                heading = "Lernen ohne Leistungsdruck",
                description = "Hier gibt es keine Persönlichkeitspunkte und keinen Zwang, besonders lange zu chatten.",
                symbol = "◌",
                accent = scheme.tertiary,
                accentContainer = scheme.tertiaryContainer,
                accentOnContainer = scheme.onTertiaryContainer
            )
            ThemeControl(storageState.themeMode, viewModel::setThemeMode)
        }

        Surface(
            color = scheme.surfaceVariant,
            shape = RoundedCornerShape(18.dp)
        ) {
            Text(
                text = "D02: Gesprächsentwürfe und Darstellung werden lokal gespeichert. Nachrichten versenden und KI-Antworten sind noch nicht enthalten.",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun AuroraHero(section: MainSection) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        AuroraPulseAccent.HeroStart,
                        AuroraPulseAccent.HeroMiddle,
                        AuroraPulseAccent.HeroEnd
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = AuroraPulseAccent.HeroStart
                ) {
                    Text(
                        text = "D02 · LOKAL OHNE KI",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
                Text(
                    text = "✦",
                    fontSize = 32.sp,
                    color = AuroraPulseAccent.HeroMint,
                    modifier = Modifier.clearAndSetSemantics {}
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = section.headline,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = section.intro,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            Text(
                text = "Entdecken  ·  Ausprobieren  ·  Wachsen",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = AuroraPulseAccent.HeroMint
            )
        }
    }
}

@Composable
private fun InfoCard(
    eyebrow: String,
    heading: String,
    description: String,
    symbol: String,
    accent: Color,
    accentContainer: Color,
    accentOnContainer: Color
) {
    val scheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, scheme.outline.copy(alpha = 0.55f)),
        colors = CardDefaults.cardColors(containerColor = scheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(accentContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = symbol,
                    fontSize = 25.sp,
                    color = accentOnContainer,
                    modifier = Modifier.clearAndSetSemantics {}
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    text = eyebrow,
                    style = MaterialTheme.typography.labelMedium,
                    color = accent,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = heading,
                    style = MaterialTheme.typography.titleMedium,
                    color = scheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * D02 deliberately exposes real, local draft storage without pretending that a message
 * can already be sent. D03 adds a full chat composer and send/response states.
 */
@Composable
private fun DraftWorkspace(state: ChatUiState, viewModel: ChatViewModel) {
    val scheme = MaterialTheme.colorScheme
    var showDeleteDialog by remember { mutableStateOf(false) }
    val activeId = state.selectedConversationId

    if (activeId == null) {
        Text(
            text = "Deine lokalen Entwürfe",
            style = MaterialTheme.typography.titleLarge,
            color = scheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Du kannst schon jetzt einen Gesprächsentwurf notieren. Er bleibt auf diesem Gerät erhalten.",
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant
        )
        Button(
            onClick = viewModel::createConversation,
            enabled = !state.isBusy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Neuen Entwurf anlegen")
        }
        if (state.conversations.isEmpty()) {
            Surface(color = scheme.primaryContainer, shape = RoundedCornerShape(20.dp)) {
                Text(
                    "Noch keine Entwürfe. Starte mit einer Idee für dein nächstes Gespräch.",
                    color = scheme.onPrimaryContainer,
                    modifier = Modifier.padding(18.dp)
                )
            }
        }
        state.conversations.forEach { conversation ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = scheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(conversation.title, color = scheme.onSurface, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Nur auf diesem Gerät · Noch nicht gesendet",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                    TextButton(onClick = { viewModel.openConversation(conversation.id) }) {
                        Text("Entwurf öffnen")
                    }
                }
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = viewModel::closeConversation, enabled = !state.isBusy) {
                Text("‹ Alle Entwürfe")
            }
            TextButton(onClick = { showDeleteDialog = true }, enabled = !state.isBusy) {
                Text("Löschen", color = scheme.error)
            }
        }
        Text(
            "Dein Gesprächsentwurf",
            style = MaterialTheme.typography.titleLarge,
            color = scheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(
            value = state.draftText,
            onValueChange = viewModel::editDraft,
            label = { Text("Was möchtest du sagen?") },
            placeholder = { Text("Schreib deine Gedanken auf …") },
            minLines = 4,
            maxLines = 10,
            enabled = !state.isLoadingDraft && !state.isBusy,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp)
        )
        Text(
            text = when {
                state.isLoadingDraft -> "Entwurf wird geladen …"
                state.error != null -> state.error
                state.isDraftSaved -> "Auf diesem Gerät gespeichert"
                else -> "Wird gespeichert … Bitte vor dem Verlassen auf die Bestätigung achten."
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (state.error != null) scheme.error else scheme.onSurfaceVariant
        )
        Surface(color = scheme.tertiaryContainer, shape = RoundedCornerShape(18.dp)) {
            Text(
                "Noch kein Chat: Dieser Text bleibt ein Entwurf und wird nicht an eine KI gesendet.",
                color = scheme.onTertiaryContainer,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    if (state.error != null && activeId == null) {
        Text(state.error, color = scheme.error, style = MaterialTheme.typography.bodySmall)
    }
    if (showDeleteDialog && activeId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Entwurf wirklich löschen?") },
            text = { Text("Der Entwurf sowie zugehörige lokale Nachrichten und Replay-Zweige werden von diesem Gerät entfernt. Das kann nicht rückgängig gemacht werden.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteConversation()
                }) { Text("Endgültig löschen", color = scheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Abbrechen") }
            }
        )
    }
}

@Composable
private fun ThemeControl(current: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(color = scheme.surface, shape = RoundedCornerShape(20.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Darstellung",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Die Einstellung wird mit DataStore dauerhaft auf deinem Gerät gespeichert.",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.entries.forEach { mode ->
                    if (mode == current) {
                        Button(onClick = { onSelect(mode) }, modifier = Modifier.weight(1f)) {
                            Text(mode.label)
                        }
                    } else {
                        OutlinedButton(onClick = { onSelect(mode) }, modifier = Modifier.weight(1f)) {
                            Text(mode.label)
                        }
                    }
                }
            }
        }
    }
}
