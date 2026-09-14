package com.eugi.apeeconomics

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import kotlin.math.max
import kotlin.random.Random

class MainActivity : Activity() {

    private var round = 1
    private var bananas = 100
    private var influence = 0
    private var popularity = 50
    private var violence = 0
    private var votes = 0
    private var shares = 0
    private var rivals = 7
    private var gameOver = false

    private lateinit var statsView: TextView
    private lateinit var roundView: TextView
    private lateinit var eventView: TextView
    private lateinit var logView: TextView
    private lateinit var choiceBox: LinearLayout

    data class Effect(
        val bananas: Int = 0,
        val influence: Int = 0,
        val popularity: Int = 0,
        val violence: Int = 0,
        val votes: Int = 0,
        val shares: Int = 0,
        val rivals: Int = 0
    )

    data class Choice(val title: String, val detail: String, val effect: Effect)
    data class Event(val title: String, val text: String, val choices: List<Choice>)

    private val events = listOf(
        Event(
            "Bananenernte fällt aus",
            "Die Plantagen liefern kaum etwas. Alle Affen verlangen eine Lösung.",
            listOf(
                Choice("Vorräte verteilen", "-35 🍌, +10 Beliebtheit", Effect(bananas = -35, popularity = 10)),
                Choice("Preise erhöhen", "+25 🍌, -12 Beliebtheit", Effect(bananas = 25, popularity = -12)),
                Choice("Andere beschuldigen", "+6 Einfluss, +6 Gewalt, -4 Beliebtheit", Effect(influence = 6, violence = 6, popularity = -4))
            )
        ),
        Event(
            "Bananenaktien explodieren",
            "Der Bananenfonds geht komplett zum Mond.",
            listOf(
                Choice("Gewinne mitnehmen", "+${200} 🍌 pro Aktie", Effect(bananas = 200)),
                Choice("Noch mehr kaufen", "-80 🍌, +2 Aktien", Effect(bananas = -80, shares = 2)),
                Choice("Kleinanleger hypen", "+10 Einfluss, -8 Beliebtheit", Effect(influence = 10, popularity = -8))
            )
        ),
        Event(
            "Wahlkampf beginnt",
            "Die Affenrepublik bereitet die nächste Wahl vor.",
            listOf(
                Choice("Ehrliche Rede", "+12 Beliebtheit, +8 Stimmen", Effect(popularity = 12, votes = 8)),
                Choice("Plakate überall", "-25 🍌, +12 Stimmen", Effect(bananas = -25, votes = 12)),
                Choice("Stimmen kaufen", "-60 🍌, +18 Stimmen, -5 Beliebtheit", Effect(bananas = -60, votes = 18, popularity = -5))
            )
        ),
        Event(
            "Rivale wird mächtig",
            "Ein anderer Affe sammelt Anhänger und könnte dich bald überholen.",
            listOf(
                Choice("Debatte fordern", "+8 Einfluss, +4 Beliebtheit", Effect(influence = 8, popularity = 4)),
                Choice("Bestechen", "-75 🍌, -1 Rivale", Effect(bananas = -75, rivals = -1)),
                Choice("Verschwinden lassen", "+18 Gewalt, -1 Rivale, -12 Beliebtheit", Effect(violence = 18, rivals = -1, popularity = -12))
            )
        ),
        Event(
            "Arbeitsaffen streiken",
            "Deine Arbeiter wollen mehr von den Bananen sehen.",
            listOf(
                Choice("Löhne erhöhen", "-30 🍌, +14 Beliebtheit", Effect(bananas = -30, popularity = 14)),
                Choice("Ignorieren", "+10 🍌, -10 Beliebtheit", Effect(bananas = 10, popularity = -10)),
                Choice("Streik brechen", "+12 Gewalt, +15 🍌, -15 Beliebtheit", Effect(violence = 12, bananas = 15, popularity = -15))
            )
        ),
        Event(
            "Bananenbank bietet Kredit",
            "Ein Banker-Affe wedelt mit einem sehr verdächtigen Vertrag.",
            listOf(
                Choice("Kredit nehmen", "+90 🍌, -5 Beliebtheit", Effect(bananas = 90, popularity = -5)),
                Choice("Bank ablehnen", "+6 Beliebtheit", Effect(popularity = 6)),
                Choice("Bank kontrollieren", "-40 🍌, +14 Einfluss", Effect(bananas = -40, influence = 14))
            )
        ),
        Event(
            "Gerücht im Dschungel",
            "Jemand behauptet, du würdest heimlich Bananen horten.",
            listOf(
                Choice("Alles offenlegen", "+10 Beliebtheit, -15 🍌", Effect(popularity = 10, bananas = -15)),
                Choice("Gerücht kaufen", "-35 🍌, +8 Einfluss", Effect(bananas = -35, influence = 8)),
                Choice("Kritiker einschüchtern", "+10 Gewalt, -8 Beliebtheit", Effect(violence = 10, popularity = -8))
            )
        ),
        Event(
            "Gratis-Bananen versprochen",
            "Die Menge fordert ein großes Wahlversprechen.",
            listOf(
                Choice("Versprechen machen", "+14 Stimmen, -20 🍌", Effect(votes = 14, bananas = -20)),
                Choice("Realistisch bleiben", "+5 Einfluss, -4 Stimmen", Effect(influence = 5, votes = -4)),
                Choice("Gegner verspotten", "+7 Stimmen, +5 Gewalt, -5 Beliebtheit", Effect(votes = 7, violence = 5, popularity = -5))
            )
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        startRound(first = true)
    }

    private fun buildUi() {
        val bg = Color.rgb(246, 239, 213)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(bg)
        }

        val title = TextView(this).apply {
            text = "🐵 APE ECONOMICS"
            textSize = 28f
            setTextColor(Color.rgb(65, 43, 20))
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 8)
        }
        root.addView(title)

        roundView = TextView(this).apply {
            textSize = 18f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
        }
        root.addView(roundView)

        statsView = TextView(this).apply {
            textSize = 17f
            setTextColor(Color.BLACK)
            setPadding(12, 18, 12, 18)
        }
        root.addView(statsView)

        eventView = TextView(this).apply {
            textSize = 21f
            setTextColor(Color.rgb(60, 45, 20))
            setPadding(12, 14, 12, 14)
        }
        root.addView(eventView)

        choiceBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        root.addView(choiceBox)

        logView = TextView(this).apply {
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setPadding(12, 18, 12, 24)
        }
        root.addView(logView)

        val scroll = ScrollView(this).apply { addView(root) }
        setContentView(scroll)
    }

    private fun startRound(first: Boolean = false) {
        if (gameOver) return

        if (!first) {
            val workerIncome = 3 * 10
            bananas += workerIncome
            if (shares > 0) {
                val market = Random.nextInt(-15, 26)
                bananas += shares * market
                appendLog("Arbeitsaffen: +$workerIncome 🍌 | Fonds: ${if (market >= 0) "+" else ""}$market 🍌 pro Aktie")
            } else {
                appendLog("3 Arbeitsaffen produzieren +$workerIncome 🍌")
            }
            round++
        }

        clampStats()
        if (checkWinLoss()) return

        roundView.text = "Runde $round  •  3/10 Affen unter deiner Kontrolle"
        updateStats()
        showRandomEvent()
    }

    private fun showRandomEvent() {
        val event = events.random()
        eventView.text = "${event.title}\n\n${event.text}"
        choiceBox.removeAllViews()

        event.choices.forEachIndexed { index, choice ->
            val button = Button(this).apply {
                text = "${index + 1}. ${choice.title}\n${choice.detail}"
                textSize = 16f
                isAllCaps = false
                setPadding(12, 14, 12, 14)
                setOnClickListener {
                    if (!gameOver) {
                        applyChoice(choice)
                        startRound()
                    }
                }
            }
            choiceBox.addView(button, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 8, 0, 8) })
        }
    }

    private fun applyChoice(choice: Choice) {
        var bananaDelta = choice.effect.bananas
        if (choice.title == "Gewinne mitnehmen") {
            bananaDelta = shares * 200
            shares = 0
        }

        bananas += bananaDelta
        influence += choice.effect.influence
        popularity += choice.effect.popularity
        violence += choice.effect.violence
        votes += choice.effect.votes
        shares += choice.effect.shares
        rivals += choice.effect.rivals

        appendLog("→ ${choice.title}")
        clampStats()
        updateStats()
    }

    private fun clampStats() {
        bananas = max(0, bananas)
        popularity = popularity.coerceIn(0, 100)
        influence = influence.coerceIn(0, 100)
        violence = violence.coerceIn(0, 100)
        votes = votes.coerceIn(0, 100)
        shares = max(0, shares)
        rivals = rivals.coerceIn(0, 7)
    }

    private fun updateStats() {
        statsView.text = buildString {
            append("🍌 Bananen: $bananas     📈 Aktien: $shares\n")
            append("🗣 Einfluss: $influence     ❤️ Beliebtheit: $popularity\n")
            append("🗳 Stimmen: $votes     👊 Gewalt: $violence\n")
            append("🐒 Rivalen: $rivals")
        }
    }

    private fun checkWinLoss(): Boolean {
        val result = when {
            rivals <= 0 && violence >= 50 -> "GEWALTSIEG\nDu bist der letzte ernsthafte Macht-Affe."
            votes >= 60 && popularity >= 55 -> "DEMOKRATIESIEG\nDie Affen haben dich gewählt. Irgendwie freiwillig."
            influence >= 70 && bananas >= 350 -> "KORRUPTIONSSIEG\nDu hast genug Bananen und Einfluss gekauft, um praktisch alles zu kontrollieren."
            popularity <= 0 -> "VERLOREN\nDie Bevölkerung jagt dich aus dem Dschungel."
            round > 35 -> "VERLOREN\nZu lange gezögert. Ein anderer Affe übernimmt die Macht."
            else -> null
        }

        if (result != null) {
            gameOver = true
            eventView.text = result
            choiceBox.removeAllViews()
            val restart = Button(this).apply {
                text = "🔁 Neues Spiel"
                textSize = 18f
                isAllCaps = false
                setOnClickListener { resetGame() }
            }
            choiceBox.addView(restart)
            updateStats()
            return true
        }
        return false
    }

    private fun resetGame() {
        round = 1
        bananas = 100
        influence = 0
        popularity = 50
        violence = 0
        votes = 0
        shares = 0
        rivals = 7
        gameOver = false
        logView.text = ""
        startRound(first = true)
    }

    private fun appendLog(text: String) {
        val old = logView.text.toString()
        val lines = (text + if (old.isBlank()) "" else "\n$old").lines().take(8)
        logView.text = "Letzte Züge:\n" + lines.joinToString("\n")
    }
}
