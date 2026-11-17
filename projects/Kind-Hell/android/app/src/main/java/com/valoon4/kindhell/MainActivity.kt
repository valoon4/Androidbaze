package com.valoon4.kindhell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.valoon4.kindhell.data.GuildMember
import com.valoon4.kindhell.data.GuildPost
import com.valoon4.kindhell.data.LocalGuildStore
import com.valoon4.kindhell.data.sampleMembers

// D / Seele Mystic: midnight-blue, moonlit lavender and gentle cyan.
// A deliberately restrained identity: no fantasy illustration wallpaper or fake social proof.
private val background = Color(0xFF080F23)
private val card = Color(0xFF15213B)
private val cardBorder = Color(0xFF334365)
private val gold = Color(0xFFADDFFF)
private val mint = Color(0xFF8FE5E2)
private val lilac = Color(0xFFCBC1FF)
private val pale = Color(0xFFF3F5FF)
private val subtle = Color(0xFFA6B5D5)

private val guildColors = darkColorScheme(
    primary = gold,
    onPrimary = background,
    secondary = mint,
    onSecondary = background,
    background = background,
    onBackground = pale,
    surface = card,
    onSurface = pale,
    surfaceVariant = Color(0xFF233456),
    onSurfaceVariant = subtle,
    outline = Color(0xFF536789)
)

private enum class Section(val label: String, val icon: String) {
    MARKET("Marktplatz", "✧"),
    ARRIVAL("Ankunft", "❀"),
    COMPASS("Kompass", "☼")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        window.statusBarColor = android.graphics.Color.rgb(8, 15, 35)
        @Suppress("DEPRECATION")
        window.navigationBarColor = android.graphics.Color.rgb(8, 15, 35)
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
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier.background(lilac.copy(alpha = 0.12f), RoundedCornerShape(13.dp))
                                .padding(horizontal = 11.dp, vertical = 6.dp)
                        ) { Text("☾", color = lilac, fontSize = 27.sp) }
                        Column {
                            Text("Kind Hell", color = pale, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                            Text("DIE GILDE  ·  OFFLINE-VORSCHAU", color = subtle, fontSize = 9.sp, letterSpacing = 1.sp)
                        }
                    }
                    TextButton(onClick = {
                        aliasDraft = store.alias
                        showProfile = true
                    }) {
                        Text("♧  ${store.alias}", color = mint, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF111B34), contentColor = pale) {
                Section.entries.forEach { option ->
                    NavigationBarItem(
                        selected = section == option,
                        onClick = { sectionName = option.name },
                        icon = { Text(option.icon, fontSize = 23.sp, color = if (section == option) mint else subtle) },
                        label = { Text(option.label, color = if (section == option) pale else subtle) },
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
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(background, Color(0xFF101B36), background)))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content
    )
}

@Composable
private fun Hero(eyebrow: String, title: String, description: String, accent: Color) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(27.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF1B2E57), Color(0xFF172045), Color(0xFF29224F))
                )
            )
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            // A small original moonlit sky painted with primitives, not generated artwork.
            val stars = listOf(
                0.08f to 0.23f, 0.19f to 0.67f, 0.38f to 0.19f,
                0.53f to 0.42f, 0.70f to 0.19f, 0.90f to 0.61f,
                0.80f to 0.81f, 0.36f to 0.87f
            )
            stars.forEach { (x, y) ->
                drawCircle(color = accent.copy(alpha = 0.55f), radius = 1.7.dp.toPx(), center = Offset(size.width * x, size.height * y))
            }
            drawCircle(
                color = Color(0xFFDCD7FF).copy(alpha = 0.35f),
                radius = 51.dp.toPx(),
                center = Offset(size.width * 0.88f, size.height * 0.33f)
            )
            drawCircle(
                color = Color(0xFF1A2750),
                radius = 47.dp.toPx(),
                center = Offset(size.width * 0.93f, size.height * 0.28f)
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 23.dp, vertical = 26.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("✦  " + eyebrow.uppercase(), color = accent, letterSpacing = 1.1.sp, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(title, color = pale, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth(0.76f))
            Text(description, color = Color(0xFFD0DAF3), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth(0.85f))
        }
    }
}

@Composable
private fun Panel(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Surface(
        color = card,
        border = BorderStroke(1.dp, cardBorder.copy(alpha = 0.75f)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(11.dp), content = content)
    }
}

@Composable
private fun Tag(label: String, tint: Color = mint) {
    Surface(color = tint.copy(alpha = 0.13f), shape = RoundedCornerShape(99.dp), border = BorderStroke(1.dp, tint.copy(alpha = 0.2f))) {
        Text(label, color = tint, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
    }
}

@Composable
private fun MarketScreen(store: LocalGuildStore) {
    var composeOpen by remember { mutableStateOf(false) }
    var postDraft by remember { mutableStateOf("") }
    var replyPostId by remember { mutableStateOf<Long?>(null) }
    var replyDraft by remember { mutableStateOf("") }

    ScrollScreen {
        Hero("Der Marktplatz", "Jede Geschichte zählt.", "Ein Ort für spontane Gedanken, besondere Geschichten und echte Gespräche.", lilac)
        Button(
            onClick = { postDraft = ""; composeOpen = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) { Text("✦  Neues Gespräch beginnen") }
        Text("✧  AUS DER GILDE", color = subtle, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
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
            Text("☾ ${post.author}", fontWeight = FontWeight.Bold, color = pale, modifier = Modifier.weight(1f))
            if (post.isDemo) Tag("DEMO", lilac)
        }
        Text(post.message, style = MaterialTheme.typography.bodyLarge)
        if (post.replies.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
            post.replies.forEach { reply ->
                Surface(color = Color(0xFF202D4A), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
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
        Hero("Die Ankunft", "Du gehörst dazu.", "Neue Gesichter. Ein erstes Hallo. Vielleicht der Anfang einer langen Geschichte.", mint)
        Panel {
            Text("✧  WILLKOMMEN IN DER GILDE", color = mint, fontWeight = FontWeight.Bold)
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
        Text("✧  NEUE GESICHTER", color = subtle, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        if (store.introduction.isNotBlank()) {
            Panel {
                Row {
                    Text("☾ ${store.alias}", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Tag("DU", gold)
                }
                Text(store.introduction)
            }
        }
        demoIntroductions.forEach { (name, intro) ->
            Panel {
                Row {
                    Text("☾ $name", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
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
        Hero("Der Kompass", "Finde deine Menschen.", "Interessen verbinden. Persönlichkeit bleibt. Und manchmal beginnt alles mit einem Hallo.", gold)
        Panel {
            Text("✧  INTERESSEN", fontWeight = FontWeight.Bold, color = subtle, style = MaterialTheme.typography.labelMedium)
            ChoiceRow(listOf("Alle", "Anime", "Gaming", "Musik", "Bücher", "Kunst", "Technik"), interest) { interest = it }
            Text("✧  KONTAKTWUNSCH", fontWeight = FontWeight.Bold, color = subtle, style = MaterialTheme.typography.labelMedium)
            ChoiceRow(listOf("Alle", "Freundschaft", "Austausch"), contact) { contact = it }
            Text("✧  SPRACHE", fontWeight = FontWeight.Bold, color = subtle, style = MaterialTheme.typography.labelMedium)
            ChoiceRow(listOf("Alle", "Deutsch", "Englisch"), language) { language = it }
            OutlinedButton(
                onClick = { interest = "Alle"; contact = "Alle"; language = "Alle" },
                shape = RoundedCornerShape(14.dp)
            ) { Text("↺ Filter zurücksetzen") }
        }
        Text("✧  ${matches.size} BEISPIELPROFILE", color = subtle, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
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
            Text("☾ ${member.alias}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Tag("DEMO", lilac)
        }
        Text(member.intro, color = pale)
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            member.interests.forEach { Tag(it, mint) }
        }
        Text("✧  ${member.contact}  ·  ${member.language}", color = subtle, style = MaterialTheme.typography.bodySmall)
        OutlinedButton(onClick = { store.toggleBookmark(member.alias) }, shape = RoundedCornerShape(14.dp)) {
            Text(if (saved) "★ Gemerkt" else "☆ Für später merken")
        }
    }
}

@Composable
private fun DemoFooter(message: String) {
    Text(
        "✧ OFFLINE-DEMO · $message",
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
