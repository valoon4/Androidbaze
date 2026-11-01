package com.valoon4.emotionskompass.feature.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.valoon4.emotionskompass.core.insights.ReflectionPrompts
import com.valoon4.emotionskompass.core.insights.ReflectionRoute

/**
 * A voluntary, entirely in-memory three-step exercise.
 * No content is written until the user ALSO confirms the regular insight editor.
 * Neither questions nor the source pattern are copied into a user's insight.
 */
@Composable
fun GuidedReflectionDialog(
    route: ReflectionRoute,
    observationLabel: String?,
    onCancel: () -> Unit,
    onDraftReady: (String) -> Unit
) {
    val prompts = ReflectionPrompts.forRoute(route)
    var step by remember(route, observationLabel) { mutableIntStateOf(0) }
    var answers by remember(route, observationLabel) {
        mutableStateOf(List(prompts.size) { "" })
    }

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text("Freiwillige Selbstreflexion", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 460.dp)
                    .testTag("guided-reflection-steps"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (observationLabel != null) {
                    item {
                        Text("Ausgangspunkt: $observationLabel",
                            fontWeight = FontWeight.SemiBold)
                        Text(
                            "Die Begriffe wurden in mehreren Einträgen gemeinsam gewählt. " +
                                "Das sagt nichts über Ursachen oder über dich als Person aus.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                item {
                    Text("Frage ${step + 1} von ${prompts.size} · ${prompts[step].stepName}",
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.testTag("reflection-step"))
                    Text(prompts[step].question, style = MaterialTheme.typography.bodyLarge)
                }
                item {
                    OutlinedTextField(
                        value = answers[step],
                        onValueChange = { text ->
                            if (text.length <= ReflectionPrompts.MAX_ANSWER_LENGTH) {
                                answers = answers.toMutableList().also { it[step] = text }
                            }
                        },
                        label = { Text("Deine Gedanken (freiwillig)") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("reflection-answer")
                    )
                    Text(
                        "${answers[step].length} / ${ReflectionPrompts.MAX_ANSWER_LENGTH} Zeichen",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                item {
                    Text(
                        "Du darfst jede Frage leer lassen oder die Reflexion jederzeit beenden. " +
                            "Nur deine eigenen Antworten gelangen als Entwurf in den Editor. " +
                            "Erst nach deinem ausdrücklichen Speichern wird etwas abgelegt.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (step > 0) {
                    item {
                        TextButton(onClick = { step-- },
                            modifier = Modifier.fillMaxWidth()) {
                            Text("Zur vorherigen Frage")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (step < prompts.lastIndex) step++
                    else onDraftReady(ReflectionPrompts.userDraft(route, answers))
                },
                modifier = Modifier.testTag(
                    if (step == prompts.lastIndex) "reflection-to-editor" else "reflection-next"
                )
            ) {
                Text(if (step == prompts.lastIndex) "Entwurf ansehen" else "Weiter")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Ohne Speichern beenden") }
        }
    )
}
