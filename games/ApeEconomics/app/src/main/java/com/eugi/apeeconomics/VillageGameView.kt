package com.eugi.apeeconomics

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

class VillageGameView(context: Context) : View(context) {
    private enum class Screen { VILLAGE, APES, BUILD, EVENT }

    private data class Ape(
        val name: String,
        val role: String,
        val rating: Int,
        var job: String,
        var equip: String,
        val art: Int,
        val dubious: Boolean = false
    )

    private data class Choice(val label: String, val detail: String, val effect: () -> Unit)
    private data class EventCard(val title: String, val text: String, val art: Int, val choices: List<Choice>)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
    private val path = Path()
    private val rng = Random(20260915)

    private var screen = Screen.VILLAGE
    private var round = 1
    private var bananas = 100
    private var popularity = 50
    private var influence = 0
    private var votes = 0
    private var violence = 0
    private var shares = 0
    private var rivals = 7
    private var builtChurch = false
    private var builtJordell = false
    private var selectedApe = 0
    private var eventIndex = 0
    private var currentEvent: EventCard? = null
    private var toast = "Deine Affen warten auf Arbeit."
    private var toastUntil = 0L

    private val apeBitmaps = HashMap<Int, Bitmap>()
    private val logo by lazy { BitmapFactory.decodeResource(resources, R.drawable.ape_logo) }

    private val apes = mutableListOf(
        Ape("Malo", "Arbeiter", 7, "Plantage", "Werkzeug", R.drawable.ape_worker),
        Ape("Poli", "Redner", 6, "Parlament", "Megafon", R.drawable.ape_politician),
        Ape("Dubi", "Dubioser", 3, "Schwarzmarkt", "Koffer", R.drawable.ape_trader, true)
    )

    init {
        isFocusable = true
        keepScreenOn = true
    }

    fun handleBack(): Boolean {
        return if (screen != Screen.VILLAGE) {
            screen = Screen.VILLAGE
            invalidate()
            true
        } else false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 1 || height <= 1) return
        drawWorldBackground(canvas)
        drawVillageHud(canvas)
        drawVillage(canvas)
        drawBottomBar(canvas)
        when (screen) {
            Screen.APES -> drawApePanel(canvas)
            Screen.BUILD -> drawBuildPanel(canvas)
            Screen.EVENT -> drawEventPanel(canvas)
            else -> Unit
        }
        drawToast(canvas)
        postInvalidateOnAnimation()
    }

    private fun drawWorldBackground(c: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        paint.shader = LinearGradient(0f, 0f, 0f, h, Color.rgb(114, 188, 210), Color.rgb(117, 174, 77), Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, w, h, paint); paint.shader = null
        paint.color = Color.rgb(74, 130, 63)
        c.drawRect(0f, 0f, w * .07f, h, paint); c.drawRect(w * .93f, 0f, w, h, paint)
        val t = System.currentTimeMillis() / 1000f
        for (i in 0 until 18) {
            val y = h * (.09f + i * .052f)
            paint.color = if (i % 2 == 0) Color.rgb(80, 154, 69) else Color.rgb(66, 133, 60)
            c.drawCircle(w * .04f, y, w * .046f, paint); c.drawCircle(w * .96f, y + h * .018f, w * .046f, paint)
        }
        paint.color = Color.argb(90, 255, 247, 196)
        for (i in 0 until 8) {
            val x = ((i * 151f + t * 18f) % (w + 120f)) - 60f
            val y = h * (.16f + (i % 3) * .09f)
            c.drawOval(RectF(x, y, x + w * .08f, y + h * .018f), paint)
        }
    }

    private fun drawVillageHud(c: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        val logoRect = RectF(w * .04f, h * .018f, w * .34f, h * .135f)
        c.drawBitmap(logo, null, logoRect, paint)
        chip(c, w * .38f, h * .022f, w * .28f, h * .052f, "🍌 $bananas", "+${incomePreview()}/R")
        chip(c, w * .68f, h * .022f, w * .28f, h * .052f, "❤️ $popularity", "📣 $influence")
        chip(c, w * .38f, h * .080f, w * .28f, h * .045f, "🗳 $votes", "👊 $violence")
        chip(c, w * .68f, h * .080f, w * .28f, h * .045f, "🐒 ${apes.size}/10", "👑 $rivals")
        drawOutlinedText(c, "RUNDE $round / 35", w * .19f, h * .160f, w * .034f, Color.WHITE, Color.rgb(61, 62, 47), Paint.Align.CENTER)
    }

    private fun chip(c: Canvas, x: Float, y: Float, cw: Float, ch: Float, top: String, bottom: String) {
        paint.color = Color.argb(225, 34, 68, 71)
        c.drawRoundRect(RectF(x, y, x + cw, y + ch), cw * .08f, cw * .08f, paint)
        stroke.color = Color.rgb(28, 46, 45); stroke.strokeWidth = max(2f, width * .004f)
        c.drawRoundRect(RectF(x, y, x + cw, y + ch), cw * .08f, cw * .08f, stroke)
        drawOutlinedText(c, top, x + cw * .5f, y + ch * .42f, width * .030f, Color.WHITE, Color.rgb(29, 34, 32), Paint.Align.CENTER)
        drawOutlinedText(c, bottom, x + cw * .5f, y + ch * .80f, width * .020f, Color.rgb(229, 242, 222), Color.rgb(29, 34, 32), Paint.Align.CENTER)
    }

    private fun drawVillage(c: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        val top = h * .185f; val bottom = h * .82f
        paint.color = Color.argb(190, 118, 189, 82)
        c.drawRoundRect(RectF(w * .08f, top, w * .92f, bottom), w * .05f, w * .05f, paint)

        // river
        path.reset(); path.moveTo(w * .08f, h * .58f); path.cubicTo(w * .28f, h * .52f, w * .62f, h * .66f, w * .92f, h * .57f)
        stroke.color = Color.rgb(76, 177, 214); stroke.strokeWidth = w * .10f; c.drawPath(path, stroke)
        stroke.color = Color.rgb(135, 222, 239); stroke.strokeWidth = w * .065f; c.drawPath(path, stroke)
        // dirt paths
        stroke.color = Color.rgb(203, 167, 95); stroke.strokeWidth = w * .035f
        c.drawLine(w * .49f, h * .29f, w * .49f, h * .71f, stroke)
        c.drawLine(w * .24f, h * .39f, w * .76f, h * .39f, stroke)
        c.drawLine(w * .26f, h * .70f, w * .73f, h * .70f, stroke)

        drawBuilding(c, "PLANTAGE", RectF(w * .11f, h * .23f, w * .39f, h * .40f), Color.rgb(227, 186, 67), "🍌")
        drawBuilding(c, "PARLAMENT", RectF(w * .38f, h * .23f, w * .66f, h * .42f), Color.rgb(218, 220, 201), "🏛")
        drawBuilding(c, "BANK", RectF(w * .67f, h * .28f, w * .90f, h * .44f), Color.rgb(204, 178, 105), "$")
        drawBuilding(c, "MARKT", RectF(w * .63f, h * .62f, w * .88f, h * .76f), Color.rgb(211, 110, 76), "▦")
        if (builtChurch) drawBuilding(c, "KIRCHE", RectF(w * .12f, h * .61f, w * .34f, h * .75f), Color.rgb(231, 222, 196), "✝")
        else drawBuildPlot(c, RectF(w * .12f, h * .61f, w * .34f, h * .75f), "BAUPLATZ")
        if (builtJordell) drawBuilding(c, "JORDELL", RectF(w * .36f, h * .61f, w * .60f, h * .75f), Color.rgb(122, 96, 161), "J")
        else drawBuildPlot(c, RectF(w * .36f, h * .61f, w * .60f, h * .75f), "BAUPLATZ")

        val time = System.currentTimeMillis() / 1000f
        apes.forEachIndexed { i, ape ->
            val base = jobPosition(ape.job)
            val bob = sin(time * 3.2f + i * 1.7f) * h * .005f
            val walk = sin(time * 1.1f + i * 2.2f) * w * .022f
            val x = w * base.first + walk
            val y = h * base.second + bob
            drawApeSprite(c, ape, x, y, w * .16f)
        }
    }

    private fun drawBuilding(c: Canvas, label: String, r: RectF, color: Int, symbol: String) {
        paint.color = Color.argb(55, 41, 41, 31); c.drawOval(RectF(r.left, r.bottom - r.height() * .12f, r.right, r.bottom + r.height() * .08f), paint)
        paint.color = color; c.drawRoundRect(r, r.width() * .08f, r.width() * .08f, paint)
        stroke.color = Color.rgb(89, 69, 44); stroke.strokeWidth = width * .006f; c.drawRoundRect(r, r.width() * .08f, r.width() * .08f, stroke)
        paint.color = Color.rgb(112, 72, 42)
        path.reset(); path.moveTo(r.left - r.width() * .04f, r.top + r.height() * .22f); path.lineTo(r.centerX(), r.top - r.height() * .22f); path.lineTo(r.right + r.width() * .04f, r.top + r.height() * .22f); path.close(); c.drawPath(path, paint)
        drawOutlinedText(c, symbol, r.centerX(), r.centerY() + r.height() * .05f, width * .055f, Color.WHITE, Color.rgb(65, 48, 35), Paint.Align.CENTER)
        paint.color = Color.argb(225, 70, 49, 31); c.drawRoundRect(RectF(r.left + 4f, r.bottom - r.height() * .22f, r.right - 4f, r.bottom + r.height() * .06f), 12f, 12f, paint)
        drawOutlinedText(c, label, r.centerX(), r.bottom - r.height() * .03f, width * .022f, Color.WHITE, Color.rgb(40, 31, 25), Paint.Align.CENTER)
    }

    private fun drawBuildPlot(c: Canvas, r: RectF, label: String) {
        paint.color = Color.argb(90, 87, 63, 39); c.drawRoundRect(r, 16f, 16f, paint)
        stroke.color = Color.rgb(117, 84, 47); stroke.strokeWidth = width * .006f; c.drawRoundRect(r, 16f, 16f, stroke)
        drawOutlinedText(c, "+", r.centerX(), r.centerY(), width * .075f, Color.rgb(240, 224, 173), Color.rgb(85, 61, 36), Paint.Align.CENTER)
        drawOutlinedText(c, label, r.centerX(), r.bottom - 8f, width * .018f, Color.WHITE, Color.rgb(55, 39, 25), Paint.Align.CENTER)
    }

    private fun drawApeSprite(c: Canvas, ape: Ape, cx: Float, cy: Float, size: Float) {
        val bm = apeBitmaps.getOrPut(ape.art) { BitmapFactory.decodeResource(resources, ape.art) }
        paint.color = Color.argb(85, 40, 30, 20); c.drawOval(RectF(cx - size * .40f, cy + size * .27f, cx + size * .40f, cy + size * .48f), paint)
        c.drawBitmap(bm, null, RectF(cx - size * .5f, cy - size * .5f, cx + size * .5f, cy + size * .5f), paint)
        val tier = tier(ape.rating)
        paint.color = tierColor(tier); c.drawCircle(cx - size * .38f, cy - size * .34f, size * .16f, paint)
        drawOutlinedText(c, "$tier${ape.rating}", cx - size * .38f, cy - size * .30f, size * .15f, Color.WHITE, Color.rgb(45, 34, 26), Paint.Align.CENTER)
    }

    private fun jobPosition(job: String): Pair<Float, Float> = when (job) {
        "Plantage" -> .25f to .48f
        "Parlament" -> .51f to .47f
        "Bank" -> .77f to .49f
        "Markt" -> .76f to .78f
        "Schwarzmarkt" -> .44f to .79f
        else -> .50f to .55f
    }

    private fun drawBottomBar(c: Canvas) {
        val w = width.toFloat(); val h = height.toFloat(); val y = h * .845f
        paint.color = Color.rgb(92, 61, 37); c.drawRect(0f, y, w, h, paint)
        button(c, RectF(w * .04f, h * .865f, w * .28f, h * .945f), "🐒 AFFEN", Color.rgb(77, 119, 76))
        button(c, RectF(w * .31f, h * .865f, w * .55f, h * .945f), "🔨 BAUEN", Color.rgb(118, 88, 62))
        button(c, RectF(w * .58f, h * .855f, w * .96f, h * .955f), "▶ NÄCHSTE RUNDE", Color.rgb(208, 148, 49))
    }

    private fun drawApePanel(c: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        dim(c); panel(c, RectF(w * .07f, h * .14f, w * .93f, h * .82f), Color.rgb(244, 225, 174))
        drawOutlinedText(c, "AFFENKADER", w * .5f, h * .19f, w * .055f, Color.rgb(80, 48, 26), Color.WHITE, Paint.Align.CENTER)
        apes.forEachIndexed { i, ape ->
            val top = h * (.23f + i * .145f); val r = RectF(w * .11f, top, w * .89f, top + h * .12f)
            paint.color = if (i == selectedApe) Color.rgb(194, 221, 151) else Color.rgb(228, 207, 160); c.drawRoundRect(r, 22f, 22f, paint)
            stroke.color = if (i == selectedApe) Color.rgb(73, 111, 53) else Color.rgb(108, 81, 55); stroke.strokeWidth = width * .006f; c.drawRoundRect(r, 22f, 22f, stroke)
            drawApeSprite(c, ape, r.left + r.width() * .13f, r.centerY(), r.height() * .85f)
            drawOutlinedText(c, "${ape.name} • ${ape.role} • ${tier(ape.rating)}${ape.rating}", r.left + r.width() * .27f, r.top + r.height() * .31f, w * .027f, Color.rgb(55, 40, 28), Color.WHITE, Paint.Align.LEFT)
            drawOutlinedText(c, "Job: ${ape.job}", r.left + r.width() * .27f, r.top + r.height() * .61f, w * .023f, Color.rgb(66, 55, 43), Color.WHITE, Paint.Align.LEFT)
            drawOutlinedText(c, "🎒 ${ape.equip}", r.left + r.width() * .27f, r.top + r.height() * .86f, w * .021f, Color.rgb(66, 55, 43), Color.WHITE, Paint.Align.LEFT)
        }
        val ape = apes[selectedApe]
        button(c, RectF(w * .11f, h * .69f, w * .48f, h * .755f), "JOB WECHSELN", Color.rgb(77, 119, 76))
        button(c, RectF(w * .52f, h * .69f, w * .89f, h * .755f), "AUSRÜSTUNG", Color.rgb(79, 105, 137))
        drawOutlinedText(c, if (ape.dubious) "⚠ Risk/Reward: 65% Gewinn • 25% Verlust • 10% Ärger" else "Spezialisierung steigert die Rundenproduktion.", w * .5f, h * .79f, w * .020f, Color.rgb(68, 50, 36), Color.WHITE, Paint.Align.CENTER)
    }

    private fun drawBuildPanel(c: Canvas) {
        val w = width.toFloat(); val h = height.toFloat(); dim(c)
        panel(c, RectF(w * .07f, h * .16f, w * .93f, h * .80f), Color.rgb(239, 220, 166))
        drawOutlinedText(c, "DORF AUSBAUEN", w * .5f, h * .21f, w * .052f, Color.rgb(78, 49, 29), Color.WHITE, Paint.Align.CENTER)
        buildCard(c, RectF(w * .11f, h * .26f, w * .89f, h * .38f), "⛪ KIRCHE", "+2 ❤️ pro Runde", 160, builtChurch)
        buildCard(c, RectF(w * .11f, h * .41f, w * .89f, h * .53f), "🏪 JORDELL", "+6 🍌 +1 📣 pro Runde", 220, builtJordell)
        buildCard(c, RectF(w * .11f, h * .56f, w * .89f, h * .68f), "📺 MEDIENHAUS", "später: Propaganda-Boni", 999, false, true)
        drawOutlinedText(c, "Freie Bauplätze erscheinen direkt im Dorf.", w * .5f, h * .75f, w * .021f, Color.rgb(74, 54, 37), Color.WHITE, Paint.Align.CENTER)
    }

    private fun buildCard(c: Canvas, r: RectF, title: String, desc: String, cost: Int, built: Boolean, locked: Boolean = false) {
        paint.color = if (built) Color.rgb(170, 205, 138) else if (locked) Color.rgb(164, 154, 137) else Color.rgb(221, 192, 133)
        c.drawRoundRect(r, 18f, 18f, paint); stroke.color = Color.rgb(102, 75, 49); stroke.strokeWidth = width * .005f; c.drawRoundRect(r, 18f, 18f, stroke)
        drawOutlinedText(c, title, r.left + r.width() * .04f, r.top + r.height() * .33f, width * .030f, Color.rgb(61, 44, 30), Color.WHITE, Paint.Align.LEFT)
        drawOutlinedText(c, desc, r.left + r.width() * .04f, r.top + r.height() * .68f, width * .021f, Color.rgb(72, 58, 43), Color.WHITE, Paint.Align.LEFT)
        val right = when { built -> "GEBAUT"; locked -> "🔒"; else -> "🍌 $cost" }
        drawOutlinedText(c, right, r.right - r.width() * .04f, r.centerY() + width * .012f, width * .028f, Color.rgb(67, 46, 29), Color.WHITE, Paint.Align.RIGHT)
    }

    private fun drawEventPanel(c: Canvas) {
        val e = currentEvent ?: return
        val w = width.toFloat(); val h = height.toFloat(); dim(c)
        panel(c, RectF(w * .055f, h * .13f, w * .945f, h * .84f), Color.rgb(252, 231, 173))
        val bm = apeBitmaps.getOrPut(e.art) { BitmapFactory.decodeResource(resources, e.art) }
        c.drawBitmap(bm, null, RectF(w * .10f, h * .18f, w * .36f, h * .34f), paint)
        drawOutlinedText(c, e.title, w * .39f, h * .22f, w * .046f, Color.rgb(62, 42, 27), Color.WHITE, Paint.Align.LEFT)
        drawMultiline(c, e.text, w * .39f, h * .26f, w * .50f, w * .024f, Color.rgb(71, 57, 42))
        e.choices.forEachIndexed { i, choice ->
            val r = RectF(w * .10f, h * (.40f + i * .12f), w * .90f, h * (.49f + i * .12f))
            val col = listOf(Color.rgb(164, 210, 126), Color.rgb(137, 190, 220), Color.rgb(226, 139, 122))[i]
            paint.color = col; c.drawRoundRect(r, 20f, 20f, paint); stroke.color = Color.rgb(92, 67, 45); stroke.strokeWidth = width * .005f; c.drawRoundRect(r, 20f, 20f, stroke)
            drawOutlinedText(c, choice.label, r.left + r.width() * .04f, r.top + r.height() * .42f, w * .027f, Color.rgb(56, 40, 27), Color.WHITE, Paint.Align.LEFT)
            drawOutlinedText(c, choice.detail, r.left + r.width() * .04f, r.top + r.height() * .76f, w * .021f, Color.rgb(68, 53, 39), Color.WHITE, Paint.Align.LEFT)
            drawOutlinedText(c, "WÄHLEN", r.right - r.width() * .04f, r.centerY() + w * .012f, w * .023f, Color.WHITE, Color.rgb(55, 42, 30), Paint.Align.RIGHT)
        }
    }

    private fun panel(c: Canvas, r: RectF, color: Int) { paint.color = color; c.drawRoundRect(r, 28f, 28f, paint); stroke.color = Color.rgb(92, 67, 45); stroke.strokeWidth = width * .007f; c.drawRoundRect(r, 28f, 28f, stroke) }
    private fun dim(c: Canvas) { paint.color = Color.argb(155, 23, 28, 24); c.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint) }

    private fun button(c: Canvas, r: RectF, label: String, color: Int) {
        paint.color = color; c.drawRoundRect(r, 20f, 20f, paint)
        stroke.color = Color.rgb(58, 45, 32); stroke.strokeWidth = width * .006f; c.drawRoundRect(r, 20f, 20f, stroke)
        drawOutlinedText(c, label, r.centerX(), r.centerY() + width * .014f, width * .026f, Color.WHITE, Color.rgb(55, 41, 29), Paint.Align.CENTER)
    }

    private fun runRound() {
        var gained = 0; var msg = ""
        apes.forEach { ape ->
            val eq = if (ape.equip == "Werkzeug" || ape.equip == "Megafon" || ape.equip == "Koffer") 2 else 0
            when (ape.job) {
                "Plantage" -> { val v = ape.rating * 3 + eq; bananas += v; gained += v }
                "Bank" -> { val v = ape.rating * 2 + eq; bananas += v; gained += v; if (ape.rating >= 7 && rng.nextInt(100) < 25) shares++ }
                "Parlament" -> { votes += max(1, ape.rating / 2); influence += 1 + eq / 2 }
                "Markt" -> { val v = ape.rating * 2; bananas += v; gained += v; popularity += 1 }
                "Schwarzmarkt" -> {
                    val roll = rng.nextInt(100)
                    when {
                        roll < 65 -> { val v = ape.rating * 10; bananas += v; gained += v; msg = "Dubioser Deal geglückt: +$v 🍌" }
                        roll < 90 -> { val v = ape.rating * 4; bananas = max(0, bananas - v); gained -= v; msg = "Dubioser Deal geplatzt: -$v 🍌" }
                        else -> { popularity -= 5; influence -= 2; msg = "Razzia! ❤️ -5  📣 -2" }
                    }
                }
            }
        }
        if (builtChurch) popularity += 2
        if (builtJordell) { bananas += 6; influence += 1; gained += 6 }
        if (shares > 0) { val v = shares * rng.nextInt(-10, 21); bananas = max(0, bananas + v); gained += v }
        round++
        clamp()
        showToast(if (msg.isNotEmpty()) msg else "Runde abgeschlossen: ${if (gained >= 0) "+" else ""}$gained 🍌")
        if (round % 3 == 0) openNextEvent()
    }

    private fun openNextEvent() {
        val events = listOf(
            EventCard("Wahlkampf!", "Die Republik will wissen, wer der Boss-Affe wird.", R.drawable.ape_politician, listOf(
                Choice("Ehrliche Rede", "❤️ +10   🗳 +7") { popularity += 10; votes += 7 },
                Choice("Plakate überall", "🍌 -25   🗳 +12") { bananas = max(0, bananas - 25); votes += 12 },
                Choice("Stimmen kaufen", "🍌 -60   🗳 +18   ❤️ -5") { bananas = max(0, bananas - 60); votes += 18; popularity -= 5 }
            )),
            EventCard("Bananenernte fällt aus", "Die Plantage hat ein Problem. Wer zahlt die Krise?", R.drawable.ape_worker, listOf(
                Choice("Vorräte verteilen", "🍌 -35   ❤️ +10") { bananas = max(0, bananas - 35); popularity += 10 },
                Choice("Preise erhöhen", "🍌 +25   ❤️ -12") { bananas += 25; popularity -= 12 },
                Choice("Andere beschuldigen", "📣 +6   👊 +6") { influence += 6; violence += 6; popularity -= 4 }
            )),
            EventCard("Rivale wird mächtig", "Ein anderer Affe sammelt gefährlich viele Anhänger.", R.drawable.ape_rival, listOf(
                Choice("Debatte fordern", "📣 +8   ❤️ +4") { influence += 8; popularity += 4 },
                Choice("Bestechen", "🍌 -75   👑 -1") { bananas = max(0, bananas - 75); rivals-- },
                Choice("Verschwinden lassen", "👊 +18   👑 -1   ❤️ -12") { violence += 18; rivals--; popularity -= 12 }
            ))
        )
        currentEvent = events[eventIndex % events.size]; eventIndex++
        screen = Screen.EVENT
    }

    private fun incomePreview(): Int {
        var sum = 0
        apes.forEach { ape -> sum += when (ape.job) { "Plantage" -> ape.rating * 3; "Bank", "Markt" -> ape.rating * 2; else -> 0 } }
        if (builtJordell) sum += 6
        return sum
    }

    private fun cycleJob() {
        val jobs = if (apes[selectedApe].dubious) listOf("Schwarzmarkt", "Markt", "Bank") else listOf("Plantage", "Bank", "Parlament", "Markt")
        val ape = apes[selectedApe]; val idx = jobs.indexOf(ape.job).let { if (it < 0) 0 else it }
        ape.job = jobs[(idx + 1) % jobs.size]
        showToast("${ape.name} arbeitet jetzt bei: ${ape.job}")
    }

    private fun cycleEquip() {
        val eq = listOf("Werkzeug", "Megafon", "Koffer", "Keine")
        val ape = apes[selectedApe]; val idx = eq.indexOf(ape.equip).let { if (it < 0) 0 else it }
        ape.equip = eq[(idx + 1) % eq.size]
        showToast("${ape.name}: ${ape.equip}")
    }

    private fun clamp() {
        popularity = popularity.coerceIn(0, 100); influence = influence.coerceIn(0, 100); votes = votes.coerceIn(0, 100); violence = violence.coerceIn(0, 100); rivals = rivals.coerceIn(0, 7); shares = max(0, shares)
    }

    private fun showToast(s: String) { toast = s; toastUntil = System.currentTimeMillis() + 2400L }
    private fun drawToast(c: Canvas) {
        if (System.currentTimeMillis() > toastUntil) return
        val w = width.toFloat(); val h = height.toFloat(); paint.color = Color.argb(225, 48, 45, 35)
        c.drawRoundRect(RectF(w * .15f, h * .80f, w * .85f, h * .845f), 18f, 18f, paint)
        drawOutlinedText(c, toast, w * .5f, h * .829f, w * .021f, Color.WHITE, Color.rgb(38, 31, 25), Paint.Align.CENTER)
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (e.actionMasked != MotionEvent.ACTION_DOWN) return true
        val x = e.x; val y = e.y; val w = width.toFloat(); val h = height.toFloat()
        when (screen) {
            Screen.VILLAGE -> {
                if (RectF(w * .04f, h * .865f, w * .28f, h * .945f).contains(x, y)) { screen = Screen.APES; return true }
                if (RectF(w * .31f, h * .865f, w * .55f, h * .945f).contains(x, y)) { screen = Screen.BUILD; return true }
                if (RectF(w * .58f, h * .855f, w * .96f, h * .955f).contains(x, y)) { runRound(); return true }
                val time = System.currentTimeMillis() / 1000f
                apes.forEachIndexed { i, ape ->
                    val p = jobPosition(ape.job); val ax = w * p.first + sin(time * 1.1f + i * 2.2f) * w * .022f; val ay = h * p.second
                    if ((x-ax)*(x-ax)+(y-ay)*(y-ay) < (w*.10f)*(w*.10f)) { selectedApe = i; screen = Screen.APES; return true }
                }
            }
            Screen.APES -> {
                for (i in apes.indices) {
                    val top = h * (.23f + i * .145f)
                    if (RectF(w * .11f, top, w * .89f, top + h * .12f).contains(x, y)) { selectedApe = i; return true }
                }
                if (RectF(w * .11f, h * .69f, w * .48f, h * .755f).contains(x, y)) { cycleJob(); return true }
                if (RectF(w * .52f, h * .69f, w * .89f, h * .755f).contains(x, y)) { cycleEquip(); return true }
                if (y < h * .14f || y > h * .82f) screen = Screen.VILLAGE
            }
            Screen.BUILD -> {
                if (RectF(w * .11f, h * .26f, w * .89f, h * .38f).contains(x, y) && !builtChurch) {
                    if (bananas >= 160) { bananas -= 160; builtChurch = true; showToast("Kirche gebaut! ❤️ +2/R") } else showToast("Zu wenig Bananen.")
                    return true
                }
                if (RectF(w * .11f, h * .41f, w * .89f, h * .53f).contains(x, y) && !builtJordell) {
                    if (bananas >= 220) { bananas -= 220; builtJordell = true; showToast("JORDELL gebaut xD") } else showToast("Zu wenig Bananen.")
                    return true
                }
                if (y < h * .16f || y > h * .80f) screen = Screen.VILLAGE
            }
            Screen.EVENT -> {
                val ev = currentEvent ?: return true
                ev.choices.forEachIndexed { i, choice ->
                    val r = RectF(w * .10f, h * (.40f + i * .12f), w * .90f, h * (.49f + i * .12f))
                    if (r.contains(x, y)) { choice.effect(); clamp(); screen = Screen.VILLAGE; showToast("Event: ${choice.label}"); return true }
                }
            }
        }
        invalidate(); return true
    }

    private fun tier(rating: Int) = when (rating) { 10 -> "S"; 9 -> "A"; 7,8 -> "B"; 4,5,6 -> "C"; else -> "D" }
    private fun tierColor(t: String) = when (t) { "S" -> Color.rgb(238, 176, 49); "A" -> Color.rgb(184, 91, 210); "B" -> Color.rgb(69, 143, 213); "C" -> Color.rgb(86, 164, 93); else -> Color.rgb(137, 120, 101) }

    private fun drawOutlinedText(c: Canvas, text: String, x: Float, y: Float, size: Float, fill: Int, outline: Int, align: Paint.Align) {
        textPaint.textSize = max(1f, size); textPaint.textAlign = align; textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.style = Paint.Style.STROKE; textPaint.strokeWidth = max(2f, size * .10f); textPaint.color = outline; c.drawText(text, x, y, textPaint)
        textPaint.style = Paint.Style.FILL; textPaint.color = fill; c.drawText(text, x, y, textPaint)
    }

    private fun drawMultiline(c: Canvas, text: String, x: Float, y: Float, maxWidth: Float, size: Float, color: Int) {
        textPaint.textSize = size; textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL); textPaint.color = color; textPaint.style = Paint.Style.FILL; textPaint.textAlign = Paint.Align.LEFT
        val words = text.split(" "); var line = ""; var yy = y
        for (word in words) { val test = if (line.isEmpty()) word else "$line $word"; if (textPaint.measureText(test) > maxWidth && line.isNotEmpty()) { c.drawText(line, x, yy, textPaint); line = word; yy += size * 1.25f } else line = test }
        if (line.isNotEmpty()) c.drawText(line, x, yy, textPaint)
    }
}
