package com.valoon4.kindhell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.valoon4.kindhell.data.GuildMember
import com.valoon4.kindhell.data.GuildPost
import com.valoon4.kindhell.data.LocalGuildStore
import com.valoon4.kindhell.data.sampleMembers

private val background = Color(0xFF161321)
private val card = Color(0xFF262136)
private val gold = Color(0xFFFFCE75)
private val mint = Color(0xFF86E1CA)
private val lilac = Color(0xFFCEB7FF)
private val pale = Color(0xFFF9F2FF)
private val subtle = Color(0xFFC9BED7)

private val guildColors = darkColorScheme(
    primary = gold,
    onPrimary = background,
    secondary = mint,
    onSecondary = background,
    background = background,
    onBackground = pale,
    surface = card,
    onSurface = pale,
    surfaceVariant = Color(0xFF393147),
    onSurfaceVariant = subtle,
    outline = Color(0xFF8C7A9D)
)

private enum class Section(val label: String, val icon: String) {
    MARKET("Marktplatz", "🌍"),
    ARRIVAL("Ankunft", "🌱"),
    COMPASS("Kompass", "🧭")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        window.statusBarColor = android.graphics.Color.rgb(22, 19, 33)
        @Suppress("DEPRECATION")
        window.navigationBarColor = android.graphics.Color.rgb(22, 19, 33)
        setContent {
            val store = remember { LocalGuildStore(applicationContext) }
            MaterialTheme(colorScheme = guildColors) {
                GuildHome(store)
            }
        }
    }
}

@Composable
private fun GuildHome(store: LocalGuildStore) {
    var sectionName by rememberSaveable { mutableStateOf(Section.MARKET.name) }
    val section = Section.entries.firstOrNull { it.name == sectionName } ?: Section.MARKET
    var showProfile by remember { mutableStateOf(false) }
    var aliasDraft by remember { mutableStateOf(store.alias) }

    Scaffold(
        containerColor = background,
        topBar = {
            Surface(color = background) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 13.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("✦ KIND HELL", color = gold, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                        Text("Come as you are · Offline-Demo", color = subtle, style = MaterialTheme.typography.labelSmall)
                    }
                    TextButton(onClick = {
                        aliasDraft = store.alias
                        showProfile = true
                    }) { Text("👤 ${store.alias}", maxLines = 1, overflow = TextOverflow.Ellipsis) }
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF201B2E), contentColor = pale) {
                Section.entries.forEach { option ->
                    NavigationBarItem(
                        selected = section == option,
                        onClick = { sectionName = option.name },
                        icon = { Text(option.icon) },
                        label = { Text(option.label) },
                        alwaysShowLabel = true
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (section) {
                Section.MARKET -> MarketScreen(store)
                Section.ARRIVAL -> ArrivalScreen(store)
                Section.COMPASS -> CompassScreen(store)
            }
        }
    }

    if (showProfile) {
        AlertDialog(
            onDismissRequest = { showProfile = false },
            title = { Text("Dein Gildenname") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Dein Alias erscheint nur auf diesem Gerät.", color = subtle)
                    OutlinedTextField(
                        value = aliasDraft,
                        onValueChange = { if (it.length <= 24) aliasDraft = it },
                        label = { Text("Alias") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = aliasDraft.isNotBlank(),
                    onClick = {
                        store.updateAlias(aliasDraft)
                        showProfile = false
                    }
                ) { Text("Speichern") }
            },
            dismissButton = { TextButton(onClick = { showProfile = false }) { Text("Abbrechen") } }
        )
    }
}

@Composable
private fun ScrollScreen(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content
    )
}

@Composable
private fun Hero(eyebrow: String, title: String, description: String, accent: Color) {
    Surface(
        color = card,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(eyebrow.uppercase(), color = accent, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Text(description, color = subtle, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun Panel(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Surface(color = card, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(9.dp), content = content)
    }
}

@Composable
private fun Tag(label: String, tint: Color = mint) {
    Surface(color = tint.copy(alpha = 0.13f), shape = RoundedCornerShape(8.dp)) {
        Text(label, color = tint, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp))
    }
}

@Composable
private fun MarketScreen(store: LocalGuildStore) {
    var composeOpen by remember { mutableStateOf(false) }
    var postDraft by remember { mutableStateOf("") }
    var replyPostId by remember { mutableStateOf<Long?>(null) }
    var replyDraft by remember { mutableStateOf("") }

    ScrollScreen {
        Hero("🌍 Der Marktplatz", "Alles darf Thema sein.", "Keine Offtopic-Polizei. Frag, erzähl, denk laut – und komm ins Gespräch.", gold)
        Button(
            onClick = { postDraft = ""; composeOpen = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) { Text("＋ Beitrag schreiben") }
        Text("GESPRÄCHE", color = subtle, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        if (store.posts.isEmpty()) Panel { Text("Noch keine Beiträge. Starte das erste Gespräch!") }
        store.posts.forEach { post ->
            PostCard(post, onReply = {
                replyDraft = ""
                replyPostId = post.id
            })
        }
        DemoFooter("Beiträge und Antworten werden nur auf deinem Gerät gespeichert.")
    }

    if (composeOpen) {
        TextEntryDialog(
            title = "Was bewegt dich?",
            value = postDraft,
            onChange = { postDraft = it },
            maxLength = 400,
            hint = "Hier muss nichts on-topic sein …",
            onDismiss = { composeOpen = false },
            onSubmit = {
                store.addPost(postDraft)
                composeOpen = false
            }
        )
    }
    if (replyPostId != null) {
        val postId = replyPostId!!
        TextEntryDialog(
            title = "Antwort schreiben",
            value = replyDraft,
            onChange = { replyDraft = it },
            maxLength = 250,
            hint = "Sag, was du denkst …",
            onDismiss = { replyPostId = null },
            onSubmit = {
                store.addReply(postId, replyDraft)
                replyPostId = null
            }
        )
    }
}

@Composable
private fun PostCard(post: GuildPost, onReply: () -> Unit) {
    Panel {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("✦ ${post.author}", fontWeight = FontWeight.Bold, color = pale, modifier = Modifier.weight(1f))
            if (post.isDemo) Tag("DEMO", lilac)
        }
        Text(post.message, style = MaterialTheme.typography.bodyLarge)
        if (post.replies.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
            post.replies.forEach { reply ->
                Surface(color = Color(0xFF352D47), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("↳ ${reply.author}", color = mint, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(reply.message, style = MaterialTheme.typography.bodyMedium, color = pale)
                    }
                }
            }
        }
        TextButton(onClick = onReply) { Text("↳ Antworten  ·  ${post.replies.size}") }
    }
}

@Composable
private fun ArrivalScreen(store: LocalGuildStore) {
    var showIntro by remember { mutableStateOf(false) }
    var introDraft by remember { mutableStateOf(store.introduction) }
    val demoIntroductions = listOf(
        "Lumi" to "Hi! Bücher, Anime und lange Gespräche.",
        "Mika" to "Zockt hier jemand gern gemeinsam?",
        "Nova" to "Neu hier! Ich zeichne und freue mich auf neue Gesichter.",
        "Kira" to "Eine Gilde ohne Thema? Bin dabei!",
        "Robin" to "Ich höre gern zu und lerne Menschen kennen."
    )
    ScrollScreen {
        Hero("🌱 Die Ankunft", "Niemand soll unsichtbar bleiben.", "Ein freundliches Hallo kann der Anfang von etwas Großem sein.", mint)
        Panel {
            Text("Willkommensrunde · lokale Vorschau", color = mint, fontWeight = FontWeight.Bold)
            Text("${demoIntroductions.size + if (store.introduction.isNotBlank()) 1 else 0} von ungefähr 20 Plätzen", style = MaterialTheme.typography.titleMedium)
            Text("Runden mit ca. 20 Neulingen sind das geplante Konzept. Die Personen unten sind fiktive Beispiele.", color = subtle)
            Button(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                onClick = {
                    introDraft = store.introduction
                    showIntro = true
                }
            ) { Text(if (store.introduction.isBlank()) "👋 Stell dich vor" else "✎ Vorstellung bearbeiten") }
        }
        Text("UNSERE VORSTELLUNGSRUNDE", color = subtle, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        if (store.introduction.isNotBlank()) {
            Panel {
                Row {
                    Text("✦ ${store.alias}", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Tag("DU", gold)
                }
                Text(store.introduction)
            }
        }
        demoIntroductions.forEach { (name, intro) ->
            Panel {
                Row {
                    Text("✦ $name", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Tag("DEMO", lilac)
                }
                Text(intro)
            }
        }
        DemoFooter("Vorstellungen sind freiwillig und werden noch nicht online veröffentlicht.")
    }
    if (showIntro) {
        TextEntryDialog(
            title = "Dein erstes Hallo",
            value = introDraft,
            onChange = { introDraft = it },
            maxLength = 260,
            hint = "Wer bist du? Was begeistert dich?",
            onDismiss = { showIntro = false },
            onSubmit = {
                store.updateIntroduction(introDraft)
                showIntro = false
            }
        )
    }
}

@Composable
private fun CompassScreen(store: LocalGuildStore) {
    var interest by rememberSaveable { mutableStateOf("Alle") }
    var contact by rememberSaveable { mutableStateOf("Alle") }
    var language by rememberSaveable { mutableStateOf("Alle") }
    val matches = sampleMembers.filter { member ->
        (interest == "Alle" || member.interests.contains(interest)) &&
            (contact == "Alle" || member.contact == contact) &&
            (language == "Alle" || member.language == language)
    }
    ScrollScreen {
        Hero("🧭 Der Kompass", "Finde Menschen, keine Scores.", "Wähle freiwillig, was dir wichtig ist. Andere Perspektiven bleiben willkommen.", lilac)
        Panel {
            Text("INTERESSEN", fontWeight = FontWeight.Bold, color = subtle, style = MaterialTheme.typography.labelMedium)
            ChoiceRow(listOf("Alle", "Anime", "Gaming", "Musik", "Bücher", "Kunst", "Technik"), interest) { interest = it }
            Text("KONTAKTWUNSCH", fontWeight = FontWeight.Bold, color = subtle, style = MaterialTheme.typography.labelMedium)
            ChoiceRow(listOf("Alle", "Freundschaft", "Austausch"), contact) { contact = it }
            Text("SPRACHE", fontWeight = FontWeight.Bold, color = subtle, style = MaterialTheme.typography.labelMedium)
            ChoiceRow(listOf("Alle", "Deutsch", "Englisch"), language) { language = it }
            OutlinedButton(
                onClick = { interest = "Alle"; contact = "Alle"; language = "Alle" },
                shape = RoundedCornerShape(14.dp)
            ) { Text("↺ Filter zurücksetzen") }
        }
        Text("${matches.size} BEISPIELPROFILE", color = subtle, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        if (matches.isEmpty()) {
            Panel { Text("Hier passt gerade niemand in die Auswahl. Probier einen anderen Filter.") }
        }
        matches.forEach { member -> MemberCard(member, store) }
        DemoFooter("Profile sind fiktiv. Merken ist lokal; es wird niemand kontaktiert.")
    }
}

@Composable
private fun ChoiceRow(choices: List<String>, selection: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        choices.forEach { choice ->
            FilterChip(
                selected = choice == selection,
                onClick = { onSelect(choice) },
                label = { Text(choice) }
            )
        }
    }
}

@Composable
private fun MemberCard(member: GuildMember, store: LocalGuildStore) {
    val saved = store.bookmarkedMembers.contains(member.alias)
    Panel {
        Row {
            Text("✦ ${member.alias}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Tag("DEMO", lilac)
        }
        Text(member.intro, color = pale)
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            member.interests.forEach { Tag(it, mint) }
        }
        Text("${member.contact}  ·  ${member.language}", color = subtle, style = MaterialTheme.typography.bodySmall)
        OutlinedButton(onClick = { store.toggleBookmark(member.alias) }, shape = RoundedCornerShape(14.dp)) {
            Text(if (saved) "★ Gemerkt" else "☆ Für später merken")
        }
    }
}

@Composable
private fun DemoFooter(message: String) {
    Text(
        "🔒 OFFLINE-DEMO · $message",
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        color = subtle,
        style = MaterialTheme.typography.labelMedium,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun TextEntryDialog(
    title: String,
    value: String,
    onChange: (String) -> Unit,
    maxLength: Int,
    hint: String,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = value,
                    onValueChange = { if (it.length <= maxLength) onChange(it) },
                    label = { Text(hint) },
                    minLines = 3,
                    maxLines = 7,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("${value.length}/$maxLength Zeichen", color = subtle, style = MaterialTheme.typography.labelSmall)
            }
        },
        confirmButton = {
            TextButton(onClick = onSubmit, enabled = value.isNotBlank()) { Text("Speichern") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        }
    )
}
