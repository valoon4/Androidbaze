package com.valoon4.emotionskompass.feature.checkin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.valoon4.emotionskompass.core.checkin.CheckInDraft
import com.valoon4.emotionskompass.core.checkin.NextStepIdea
import com.valoon4.emotionskompass.core.checkin.PersonalNeed

/** Optional user-authored meaning making; never infers a need from selected feelings. */
@Composable
fun DeepReflectionStep(
    draft: CheckInDraft,
    onSituation: (String) -> Unit,
    onInterpretation: (String) -> Unit,
    onNeed: (PersonalNeed) -> Unit,
    onInsight: (String) -> Unit,
    onNextStep: (NextStepIdea?) -> Unit,
    onNextStepNote: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(17.dp)) {
        Text("Du kannst den Fragen folgen – oder einfach weitergehen.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium)

        Section("1 · Was ist geschehen?",
            "Wenn du magst, beschreibe nur das, was jemand hätte beobachten können.") {
            ReflectionField(
                value = draft.situation,
                onChange = onSituation,
                label = "Die Situation in meinen Worten",
                limit = CheckInDraft.MAX_CONTEXT_LENGTH
            )
        }
        Section("2 · Wie habe ich es erlebt?",
            "Unsere Gedanken über eine Situation sind nicht dasselbe wie die Situation selbst. Beides darf nebeneinanderstehen.") {
            ReflectionField(
                value = draft.interpretation,
                onChange = onInterpretation,
                label = "Meine persönliche Sicht (optional)",
                limit = CheckInDraft.MAX_CONTEXT_LENGTH
            )
        }
        Section("3 · Was könnte mir wichtig sein?",
            "Bedürfnisse sind nur Vorschläge zum Nachdenken. Die App leitet nichts aus deinen Gefühlen ab.") {
            PersonalNeed.entries.chunked(4).forEach { options ->
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(options) { need ->
                        FilterChip(
                            selected = need in draft.needs,
                            onClick = { onNeed(need) },
                            label = { Text(need.label) }
                        )
                    }
                }
            }
            Text("Du kannst mehrere wählen, sie abwählen oder nichts davon.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall)
        }
        Section("4 · Was ist mir klarer geworden?",
            "Nur du kannst entscheiden, ob dieser Moment dir etwas gezeigt hat. Auch „noch nichts“ ist in Ordnung.") {
            ReflectionField(
                value = draft.insight,
                onChange = onInsight,
                label = "Meine eigene Erkenntnis (optional)",
                limit = CheckInDraft.MAX_INSIGHT_LENGTH
            )
        }
        Section("5 · Was würde mir jetzt guttun?",
            "Kein Gefühl muss sofort gelöst werden. Wenn du möchtest, wähle einen kleinen eigenen Schritt.") {
            NextStepIdea.entries.chunked(3).forEach { options ->
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(options) { idea ->
                        FilterChip(
                            selected = draft.nextStepIdea == idea,
                            onClick = {
                                onNextStep(if (draft.nextStepIdea == idea) null else idea)
                            },
                            label = { Text(idea.label) }
                        )
                    }
                }
            }
            ReflectionField(
                value = draft.nextStepNote,
                onChange = onNextStepNote,
                label = "Mein nächster Schritt in eigenen Worten (optional)",
                limit = CheckInDraft.MAX_CONTEXT_LENGTH
            )
        }
        Text("Du bestimmst selbst, was passt. Es gibt keine richtigen Antworten, Noten oder automatischen Bewertungen.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun Section(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            content()
        }
    }
}

@Composable
private fun ReflectionField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    limit: Int
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        minLines = 2,
        maxLines = 4,
        supportingText = { Text("${value.length} / $limit · nur während dieses Check-ins") }
    )
}
