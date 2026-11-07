package com.valoon4.communicationtrainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.valoon4.communicationtrainer.ui.theme.AuroraPulseAccent
import com.valoon4.communicationtrainer.ui.theme.AuroraPulseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { CommunicationTrainerApp() }
    }
}

@Composable
fun CommunicationTrainerApp() {
    var selectedSection by rememberSaveable { mutableStateOf(MainSection.CHATS.name) }
    val activeSection = MainSection.valueOf(selectedSection)

    AuroraPulseTheme {
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
            SectionContent(activeSection, innerPadding)
        }
    }
}

@Composable
private fun SectionContent(section: MainSection, innerPadding: PaddingValues) {
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
            InfoCard(
                eyebrow = "CHATBEREICH",
                heading = "Deine Unterhaltungen",
                description = "Noch keine Gespräche vorhanden. Chatverläufe und Texteingabe folgen in D02 und D03.",
                symbol = "◉",
                accent = scheme.secondary,
                accentContainer = scheme.secondaryContainer,
                accentOnContainer = scheme.onSecondaryContainer
            )
            InfoCard(
                eyebrow = "CHARAKTERE",
                heading = "Fiktive Personen, neue Perspektiven",
                description = "Später übst du mit fünf simulierten Charakteren. Das sind keine echten Menschen.",
                symbol = "✧",
                accent = scheme.tertiary,
                accentContainer = scheme.tertiaryContainer,
                accentOnContainer = scheme.onTertiaryContainer
            )
        } else {
            InfoCard(
                eyebrow = "ÜBUNGSSITUATIONEN",
                heading = "Alltag entspannt ausprobieren",
                description = "Wir starten später mit zwei Situationen und erweitern dann auf sechs.",
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
        }

        Surface(
            color = scheme.surfaceVariant,
            shape = RoundedCornerShape(18.dp)
        ) {
            Text(
                text = "Bereits ausprobierbar: die Navigation. Nachrichten, Speichern und KI-Antworten sind in dieser Demo noch nicht enthalten.",
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
                        text = "D01 · DEMO OHNE KI",
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
