package com.androidbaze.starter

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.*
import kotlin.random.Random

class PikminLikeGameView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = false }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        color = Color.WHITE
    }

    private data class Vec(var x: Float, var y: Float)
    private data class Enemy(var p: Vec, var hp: Int = 4, var flash: Int = 0)
    private data class Item(var p: Vec, val kind: Int, var taken: Boolean = false)
    private data class Buddy(var p: Vec, val kind: Int, var target: Vec? = null)

    private val player = Vec(520f, 950f)
    private val buddies = MutableList(18) { i -> Buddy(Vec(500f + (i % 6) * 26f, 1030f + (i / 6) * 28f), i % 3) }
    private val enemies = mutableListOf(
        Enemy(Vec(255f, 470f), 5), Enemy(Vec(780f, 520f), 5), Enemy(Vec(690f, 1120f), 6), Enemy(Vec(310f, 1280f), 4)
    )
    private val items = mutableListOf(
        Item(Vec(170f, 760f), 0), Item(Vec(830f, 770f), 1), Item(Vec(570f, 380f), 2),
        Item(Vec(790f, 1320f), 0), Item(Vec(210f, 1110f), 1), Item(Vec(500f, 1480f), 2)
    )

    private var collectedFruit = 0
    private var collectedCrystal = 0
    private var collectedPellet = 0
    private var moveId = -1
    private var stickOrigin = Vec(150f, 1500f)
    private var stick = Vec(0f, 0f)
    private var lastNanos = System.nanoTime()
    private var whistlePulse = 0f
    private var message = "Explore the garden"
    private var messageTicks = 160

    private val worldW = 1000f
    private val worldH = 1700f

    init {
        setBackgroundColor(Color.rgb(21, 42, 31))
        isFocusable = true
    }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        val now = System.nanoTime()
        val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.033f)
        lastNanos = now
        update(dt)

        val sx = width / worldW
        val sy = height / worldH
        c.save()
        c.scale(sx, sy)
        drawWorld(c)
        drawHud(c)
        c.restore()
        postInvalidateOnAnimation()
    }

    private fun update(dt: Float) {
        val speed = 360f
        player.x = (player.x + stick.x * speed * dt).coerceIn(70f, worldW - 70f)
        player.y = (player.y + stick.y * speed * dt).coerceIn(170f, worldH - 120f)

        buddies.forEachIndexed { i, b ->
            val target = b.target ?: Vec(
                player.x + cos(i * 2.1f) * (70f + (i % 4) * 18f),
                player.y + sin(i * 2.1f) * (70f + (i % 4) * 18f)
            )
            val dx = target.x - b.p.x
            val dy = target.y - b.p.y
            val d = hypot(dx, dy)
            if (d > 5f) {
                val s = if (b.target != null) 520f else 300f
                b.p.x += dx / d * min(d, s * dt)
                b.p.y += dy / d * min(d, s * dt)
            }
            b.target?.let { t -> if (hypot(t.x - b.p.x, t.y - b.p.y) < 20f) b.target = null }
        }

        items.filter { !it.taken }.forEach { item ->
            if (hypot(player.x - item.p.x, player.y - item.p.y) < 65f) {
                item.taken = true
                when (item.kind) {
                    0 -> collectedFruit++
                    1 -> collectedCrystal++
                    else -> collectedPellet++
                }
                showMessage("Collected resource!")
            }
        }
        enemies.forEach { if (it.flash > 0) it.flash-- }
        enemies.removeAll { it.hp <= 0 }
        whistlePulse = max(0f, whistlePulse - dt * 1.8f)
        if (messageTicks > 0) messageTicks--
    }

    private fun drawWorld(c: Canvas) {
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(74, 123, 69)
        c.drawRect(0f, 0f, worldW, worldH, paint)

        // chunky pixel-grass patches
        val rnd = Random(7)
        repeat(115) {
            val x = rnd.nextInt(20, 980).toFloat()
            val y = rnd.nextInt(180, 1660).toFloat()
            paint.color = if (it % 3 == 0) Color.rgb(61, 105, 58) else Color.rgb(91, 142, 74)
            c.drawRect(x, y, x + 9f, y + 20f, paint)
            if (it % 5 == 0) {
                paint.color = Color.rgb(244, 217, 92)
                c.drawRect(x - 4, y - 4, x + 6, y + 6, paint)
            }
        }

        // dirt path
        paint.color = Color.rgb(183, 146, 91)
        c.drawRoundRect(330f, 210f, 665f, 1570f, 90f, 90f, paint)
        paint.color = Color.rgb(205, 171, 112)
        repeat(11) { i -> c.drawRect(390f + (i % 2) * 100, 280f + i * 115, 470f + (i % 2) * 100, 305f + i * 115, paint) }

        // pond
        paint.color = Color.rgb(46, 130, 157)
        c.drawOval(70f, 250f, 320f, 560f, paint)
        paint.color = Color.rgb(89, 176, 179)
        c.drawOval(105f, 290f, 285f, 515f, paint)
        paint.color = Color.rgb(69, 142, 68)
        c.drawOval(135f, 335f, 195f, 375f, paint)
        c.drawOval(220f, 430f, 275f, 466f, paint)

        // base pod
        paint.color = Color.rgb(230, 235, 218)
        c.drawCircle(825f, 285f, 85f, paint)
        paint.color = Color.rgb(188, 61, 52)
        c.drawArc(740f, 200f, 910f, 360f, 200f, 140f, true, paint)
        paint.color = Color.rgb(57, 83, 92)
        c.drawCircle(825f, 300f, 30f, paint)
        paint.color = Color.rgb(119, 228, 116)
        c.drawCircle(825f, 285f, 11f, paint)
        textPaint.textSize = 22f
        c.drawText("BASE", 785f, 390f, textPaint)

        // rocks / logs
        repeat(8) { i ->
            val x = if (i % 2 == 0) 90f + i * 95 else 780f - i * 45
            val y = 620f + i * 105
            paint.color = Color.rgb(93, 93, 78)
            c.drawRoundRect(x, y, x + 75, y + 52, 18f, 18f, paint)
        }

        items.filter { !it.taken }.forEach { drawItem(c, it) }
        enemies.forEach { drawEnemy(c, it) }
        buddies.forEach { drawBuddy(c, it) }
        drawPlayer(c)

        if (whistlePulse > 0f) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 8f
            paint.color = Color.argb((160 * whistlePulse).toInt().coerceIn(0, 160), 255, 255, 190)
            c.drawCircle(player.x, player.y, 80f + (1f - whistlePulse) * 260f, paint)
            paint.style = Paint.Style.FILL
        }
    }

    private fun drawPlayer(c: Canvas) {
        paint.color = Color.rgb(230, 235, 225)
        c.drawCircle(player.x, player.y, 31f, paint)
        paint.color = Color.rgb(191, 61, 52)
        c.drawRect(player.x - 23f, player.y + 20f, player.x + 23f, player.y + 55f, paint)
        paint.color = Color.rgb(52, 84, 105)
        c.drawRect(player.x - 17f, player.y - 8f, player.x + 17f, player.y + 10f, paint)
        paint.color = Color.WHITE
        c.drawRect(player.x - 9f, player.y - 5f, player.x - 2f, player.y + 2f, paint)
        c.drawRect(player.x + 5f, player.y - 5f, player.x + 12f, player.y + 2f, paint)
    }

    private fun drawBuddy(c: Canvas, b: Buddy) {
        paint.color = when (b.kind) {
            0 -> Color.rgb(220, 73, 67)
            1 -> Color.rgb(243, 202, 66)
            else -> Color.rgb(68, 151, 214)
        }
        c.drawCircle(b.p.x, b.p.y, 18f, paint)
        c.drawRect(b.p.x - 13f, b.p.y + 9f, b.p.x + 13f, b.p.y + 29f, paint)
        paint.color = Color.rgb(48, 95, 50)
        c.drawRect(b.p.x - 2f, b.p.y - 34f, b.p.x + 3f, b.p.y - 16f, paint)
        c.drawOval(b.p.x + 1f, b.p.y - 40f, b.p.x + 19f, b.p.y - 27f, paint)
        paint.color = Color.WHITE
        c.drawCircle(b.p.x - 6f, b.p.y - 2f, 4f, paint)
        c.drawCircle(b.p.x + 6f, b.p.y - 2f, 4f, paint)
    }

    private fun drawEnemy(c: Canvas, e: Enemy) {
        paint.color = if (e.flash > 0) Color.WHITE else Color.rgb(196, 88, 64)
        c.drawOval(e.p.x - 50f, e.p.y - 34f, e.p.x + 50f, e.p.y + 40f, paint)
        paint.color = Color.rgb(88, 60, 51)
        c.drawCircle(e.p.x - 25f, e.p.y - 25f, 20f, paint)
        c.drawCircle(e.p.x + 25f, e.p.y - 25f, 20f, paint)
        paint.color = Color.WHITE
        c.drawCircle(e.p.x - 25f, e.p.y - 29f, 8f, paint)
        c.drawCircle(e.p.x + 25f, e.p.y - 29f, 8f, paint)
        paint.color = Color.BLACK
        c.drawCircle(e.p.x - 25f, e.p.y - 29f, 3f, paint)
        c.drawCircle(e.p.x + 25f, e.p.y - 29f, 3f, paint)
        // hp pips
        repeat(e.hp) { i ->
            paint.color = Color.rgb(238, 94, 82)
            c.drawRect(e.p.x - 32f + i * 14f, e.p.y + 55f, e.p.x - 22f + i * 14f, e.p.y + 66f, paint)
        }
    }

    private fun drawItem(c: Canvas, item: Item) {
        when (item.kind) {
            0 -> {
                paint.color = Color.rgb(221, 55, 68)
                c.drawCircle(item.p.x, item.p.y, 28f, paint)
                paint.color = Color.rgb(63, 126, 59)
                c.drawRect(item.p.x - 4, item.p.y - 42, item.p.x + 5, item.p.y - 18, paint)
            }
            1 -> {
                paint.color = Color.rgb(87, 207, 229)
                val p = Path().apply {
                    moveTo(item.p.x, item.p.y - 38); lineTo(item.p.x + 28, item.p.y)
                    lineTo(item.p.x, item.p.y + 38); lineTo(item.p.x - 28, item.p.y); close()
                }
                c.drawPath(p, paint)
            }
            else -> {
                paint.color = Color.rgb(226, 196, 116)
                c.drawCircle(item.p.x, item.p.y, 29f, paint)
                paint.color = Color.rgb(137, 100, 55)
                c.drawCircle(item.p.x, item.p.y, 13f, paint)
            }
        }
    }

    private fun drawHud(c: Canvas) {
        paint.color = Color.argb(220, 25, 34, 35)
        c.drawRect(0f, 0f, worldW, 150f, paint)
        textPaint.textSize = 31f
        c.drawText("DAY 01   10:42", 35f, 52f, textPaint)
        textPaint.textSize = 25f
        c.drawText("SQUAD ${buddies.size}/20", 690f, 52f, textPaint)
        c.drawText("FRUIT $collectedFruit   CRYSTAL $collectedCrystal   PELLET $collectedPellet", 35f, 105f, textPaint)

        if (messageTicks > 0) {
            paint.color = Color.argb(190, 20, 25, 25)
            c.drawRoundRect(260f, 155f, 740f, 215f, 18f, 18f, paint)
            textPaint.textSize = 23f
            textPaint.textAlign = Paint.Align.CENTER
            c.drawText(message, 500f, 194f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT
        }

        // joystick
        paint.color = Color.argb(95, 255, 255, 255)
        c.drawCircle(stickOrigin.x, stickOrigin.y, 92f, paint)
        paint.color = Color.argb(170, 245, 245, 245)
        c.drawCircle(stickOrigin.x + stick.x * 52f, stickOrigin.y + stick.y * 52f, 46f, paint)

        drawButton(c, 825f, 1400f, 78f, Color.rgb(48, 142, 126), "WHISTLE")
        drawButton(c, 690f, 1515f, 78f, Color.rgb(215, 158, 56), "THROW")
        drawButton(c, 865f, 1565f, 86f, Color.rgb(181, 68, 62), "ATTACK")
    }

    private fun drawButton(c: Canvas, x: Float, y: Float, r: Float, color: Int, label: String) {
        paint.color = color
        c.drawCircle(x, y, r, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 7f
        paint.color = Color.argb(180, 255, 255, 255)
        c.drawCircle(x, y, r - 5, paint)
        paint.style = Paint.Style.FILL
        textPaint.textSize = 20f
        textPaint.textAlign = Paint.Align.CENTER
        c.drawText(label, x, y + 7f, textPaint)
        textPaint.textAlign = Paint.Align.LEFT
    }

    private fun showMessage(s: String) { message = s; messageTicks = 120 }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val x = ev.x * worldW / width
        val y = ev.y * worldH / height
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val idx = ev.actionIndex
                val tx = ev.getX(idx) * worldW / width
                val ty = ev.getY(idx) * worldH / height
                if (tx < 330f && ty > 1280f && moveId == -1) {
                    moveId = ev.getPointerId(idx)
                    stickOrigin = Vec(150f, 1500f)
                    updateStick(tx, ty)
                } else if (hypot(tx - 825f, ty - 1400f) < 105f) whistle()
                else if (hypot(tx - 690f, ty - 1515f) < 105f) throwBuddy()
                else if (hypot(tx - 865f, ty - 1565f) < 115f) attack()
            }
            MotionEvent.ACTION_MOVE -> {
                val idx = ev.findPointerIndex(moveId)
                if (idx >= 0) updateStick(ev.getX(idx) * worldW / width, ev.getY(idx) * worldH / height)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                val idx = ev.actionIndex
                if (idx >= 0 && ev.getPointerId(idx) == moveId) {
                    moveId = -1
                    stick = Vec(0f, 0f)
                }
            }
        }
        return true
    }

    private fun updateStick(x: Float, y: Float) {
        val dx = x - stickOrigin.x
        val dy = y - stickOrigin.y
        val d = hypot(dx, dy)
        if (d < 12f) stick = Vec(0f, 0f) else {
            val m = min(1f, d / 85f)
            stick = Vec(dx / d * m, dy / d * m)
        }
    }

    private fun whistle() {
        whistlePulse = 1f
        buddies.forEach { it.target = null }
        showMessage("Squad regrouped!")
    }

    private fun nearestEnemy(maxRange: Float): Enemy? = enemies.minByOrNull { hypot(it.p.x - player.x, it.p.y - player.y) }
        ?.takeIf { hypot(it.p.x - player.x, it.p.y - player.y) <= maxRange }

    private fun throwBuddy() {
        val e = nearestEnemy(420f)
        if (e == null) {
            showMessage("No target nearby")
            return
        }
        val b = buddies.minByOrNull { hypot(it.p.x - player.x, it.p.y - player.y) } ?: return
        b.target = Vec(e.p.x, e.p.y)
        e.hp -= 1
        e.flash = 5
        showMessage("Buddy toss! -1 HP")
    }

    private fun attack() {
        val e = nearestEnemy(210f)
        if (e == null) {
            showMessage("Move closer to attack")
            return
        }
        e.hp -= 2
        e.flash = 7
        buddies.take(6).forEachIndexed { i, b ->
            b.target = Vec(e.p.x + cos(i.toFloat()) * 35f, e.p.y + sin(i.toFloat()) * 35f)
        }
        showMessage(if (e.hp <= 0) "Enemy defeated!" else "Squad attack! -2 HP")
    }
}
