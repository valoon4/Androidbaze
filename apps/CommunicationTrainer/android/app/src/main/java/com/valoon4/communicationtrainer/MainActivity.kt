package com.valoon4.communicationtrainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { CommunicationTrainerApp() }
    }
}

private val lightColors = lightColorScheme(
    primary = Color(0xFF245B68),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7EEF0),
    onPrimaryContainer = Color(0xFF174951),
    surface = Color(0xFFFAFBFA),
    surfaceVariant = Color(0xFFE8EEED)
)

private val darkColors = darkColorScheme(
    primary = Color(0xFF91D2DA),
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF1C4C55),
    onPrimaryContainer = Color(0xFFC0EDF1)
)

@Composable
fun CommunicationTrainerApp() {
    var selectedSection by rememberSaveable { mutableStateOf(MainSection.CHATS.name) }
    val activeSection = MainSection.valueOf(selectedSection)

    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColors else lightColors) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    MainSection.entries.forEach { section ->
                        NavigationBarItem(
                            selected = section == activeSection,
                            onClick = { selectedSection = section.name },
                            icon = { Text(if (section == activeSection) "●" else "○") },
                            label = { Text(section.title) },
                            alwaysShowLabel = true
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "CommunicationTrainer",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Text(
                text = "D01 · Technische Demo ohne KI",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.labelMedium
            )
        }
        Text(
            text = section.headline,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = section.intro,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (section == MainSection.CHATS) {
            InfoCard(
                heading = "Deine Unterhaltungen",
                description = "Noch keine Gespräche vorhanden. Chatverläufe und Texteingabe folgen in D02 und D03."
            )
            InfoCard(
                heading = "Echte Charaktere, bewusst fiktiv",
                description = "In späteren Entwicklungsstufen lernst du unsere fünf KI-Figuren kennen. " +
                    "Sie sind Simulationen und keine echten Menschen."
            )
        } else {
            InfoCard(
                heading = "Alltagssituationen",
                description = "Wir beginnen später mit zwei gezielten Übungssituationen und erweitern dann auf sechs."
            )
            InfoCard(
                heading = "Lernen ohne Bewertung",
                description = "Es gibt keine Persönlichkeitspunkte und keinen Druck, besonders lange zu chatten."
            )
        }

        Text(
            text = "Die Auswahl unten funktioniert bereits. Nachrichten, Speichern und KI-Antworten werden hier noch nicht angeboten.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun InfoCard(heading: String, description: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = heading,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
