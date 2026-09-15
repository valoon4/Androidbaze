package com.eugi.apeeconomics

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import kotlin.math.sin

/**
 * Ape Economics v0.13
 *
 * Asset-first village renderer:
 * - the environment is one authored pixel-art background asset
 * - characters are existing sprite PNG assets
 * - Canvas is only used for HUD, animation and interactions
 */
class PixelVillageV13(context: Context) : View(context) {
    private val paint = Paint().apply {
        isAntiAlias = false
        isFilterBitmap = false
    }
    private val text = Paint().apply {
        isAntiAlias = false
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val world: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.ape_v13_world)
    private val logo: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.ape_logo)
    private val worker: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.ape_worker)
    private val politician: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.ape_politician)
    private val banker: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.ape_banker)
    private val trader: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.ape_trader)

    private data class Ape(
        var x: Float,
        var y: Float,
        val homeX: Float,
        val homeY: Float,
        val jobX: Float,
        val jobY: Float,
        val sprite: Bitmap,
        var phase: Int = 0,
        var since: Long = 0L
    )

    private var bananas = 128
    private var day = 1
    private var lastIncome = System.currentTimeMillis()
    private var dayStart = System.currentTimeMillis()
    private var eventOpen = false
    private var selectedTab = 0

    private val apes = mutableListOf(
        Ape(.195f, .429f, .195f, .429f, .194f, .245f, worker),
        Ape(.497f, .438f, .497f, .438f, .500f, .252f, politician),
        Ape(.796f, .425f, .796f, .425f, .812f, .252f, banker),
        Ape(.604f, .711f, .604f, .711f, .807f, .750f, trader)
    )

    init {
        keepScreenOn = true
        val now = System.currentTimeMillis()
        apes.forEachIndexed { i, ape -> ape.since = now + i * 430L }
    }

    fun handleBack(): Boolean {
        if (eventOpen) {
            eventOpen = false
            invalidate()
            return true
        }
        return false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val now = System.currentTimeMillis()
        update(now)

        paint.color = Color.rgb(19, 48, 42)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        paint.isFilterBitmap = false
        canvas.drawBitmap(
            world,
            null,
            RectF(0f, 0f, width.toFloat(), height.toFloat()),
            paint
        )

        drawApeSprites(canvas, now)
        drawHud(canvas, now)
        drawBottomBar(canvas)
        if (eventOpen) drawEvent(canvas)

        postInvalidateOnAnimation()
    }

    private fun update(now: Long) {
        while (now - lastIncome >= 5_000L) {
            bananas += 1
            lastIncome += 5_000L
        }

        if (!eventOpen && now - dayStart >= 30_000L) {
            day += 1
            dayStart = now
            if (day % 3 == 0) eventOpen = true
        }

        if (eventOpen) return

        apes.forEach { a ->
            val duration = when (a.phase) {
                0 -> 1000L
                1 -> 2200L
                2 -> 2200L
                else -> 2200L
            }

            if (now - a.since >= duration) {
                a.phase = (a.phase + 1) % 4
                a.since = now
                if (a.phase == 3) bananas += 2
            }

            val f = ((now - a.since).toFloat() / duration).coerceIn(0f, 1f)
            when (a.phase) {
                0 -> {
                    a.x = a.homeX
                    a.y = a.homeY
                }
                1 -> {
                    a.x = lerp(a.homeX, a.jobX, f)
                    a.y = lerp(a.homeY, a.jobY, f)
                }
                2 -> {
                    a.x = a.jobX
                    a.y = a.jobY
                }
                else -> {
                    a.x = lerp(a.jobX, a.homeX, f)
                    a.y = lerp(a.jobY, a.homeY, f)
                }
            }
        }
    }

    private fun drawApeSprites(canvas: Canvas, now: Long) {
        val w = width.toFloat()
        val h = height.toFloat()
        val size = w * .052f

        apes.sortedBy { it.y }.forEachIndexed { index, a ->
            val bob = sin((now / 145.0 + index * 1.7)).toFloat() * h * .0014f
            val cx = w * a.x
            val feet = h * a.y + bob

            paint.color = Color.argb(75, 18, 44, 28)
            canvas.drawOval(
                RectF(cx - size * .33f, feet - size * .08f, cx + size * .33f, feet + size * .10f),
                paint
            )

            paint.isFilterBitmap = false
            canvas.drawBitmap(
                a.sprite,
                null,
                RectF(cx - size * .50f, feet - size, cx + size * .50f, feet),
                paint
            )

            if (a.phase == 2 && (now / 700L + index) % 4L == 0L) {
                paint.color = Color.rgb(247, 203, 50)
                canvas.drawCircle(cx + size * .42f, feet - size * .78f, size * .09f, paint)
                text.color = Color.rgb(86, 57, 27)
                text.textSize = size * .14f
                canvas.drawText("+", cx + size * .42f, feet - size * .74f, text)
            }
        }
    }

    private fun drawHud(canvas: Canvas, now: Long) {
        val w = width.toFloat()
        val h = height.toFloat()
        val top = h * .008f
        val barBottom = h * .114f
        val gap = w * .008f

        paint.color = Color.rgb(18, 52, 43)
        canvas.drawRect(0f, 0f, w, barBottom + h * .006f, paint)

        val widths = floatArrayOf(.288f, .248f, .200f, .220f)
        var x = gap
        val cells = ArrayList<RectF>()
        widths.forEach { ratio ->
            val rw = w * ratio - gap
            cells += RectF(x, top, x + rw, barBottom)
            x += rw + gap
        }

        cells.forEach { cell ->
            paint.color = Color.rgb(88, 58, 38)
            canvas.drawRect(cell, paint)
            val inset = w * .005f
            paint.color = Color.rgb(244, 232, 193)
            canvas.drawRect(
                cell.left + inset,
                cell.top + inset,
                cell.right - inset,
                cell.bottom - inset,
                paint
            )
            paint.color = Color.rgb(207, 181, 112)
            canvas.drawRect(
                cell.left + inset * 1.7f,
                cell.top + inset * 1.7f,
                cell.right - inset * 1.7f,
                cell.top + inset * 2.6f,
                paint
            )
            canvas.drawRect(
                cell.left + inset * 1.7f,
                cell.bottom - inset * 2.6f,
                cell.right - inset * 1.7f,
                cell.bottom - inset * 1.7f,
                paint
            )
        }

        val c0 = cells[0]
        val logoH = c0.height() * .70f
        val logoW = logoH * logo.width.toFloat() / logo.height.toFloat()
        canvas.drawBitmap(
            logo,
            null,
            RectF(
                c0.left + c0.width() * .06f,
                c0.centerY() - logoH * .50f,
                c0.left + c0.width() * .06f + logoW,
                c0.centerY() + logoH * .50f
            ),
            paint
        )
        text.textAlign = Paint.Align.LEFT
        text.color = Color.rgb(67, 45, 32)
        text.textSize = h * .017f
        canvas.drawText("APE", c0.left + c0.width() * .39f, c0.top + c0.height() * .36f, text)
        text.textSize = h * .013f
        canvas.drawText("ECONOMICS", c0.left + c0.width() * .39f, c0.top + c0.height() * .62f, text)
        text.textSize = h * .0048f
        text.letterSpacing = .08f
        canvas.drawText("POWER · BANANAS · PAYS", c0.left + c0.width() * .39f, c0.top + c0.height() * .82f, text)
        text.letterSpacing = 0f

        val c1 = cells[1]
        text.textAlign = Paint.Align.CENTER
        text.color = Color.rgb(68, 48, 34)
        text.textSize = h * .022f
        canvas.drawText("$bananas", c1.centerX() + c1.width() * .08f, c1.top + c1.height() * .47f, text)
        text.color = Color.rgb(48, 126, 63)
        text.textSize = h * .0078f
        canvas.drawText("+12 / MIN", c1.centerX() + c1.width() * .08f, c1.top + c1.height() * .70f, text)
        paint.color = Color.rgb(241, 188, 35)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = w * .010f
        canvas.drawArc(
            RectF(
                c1.left + c1.width() * .12f,
                c1.top + c1.height() * .36f,
                c1.left + c1.width() * .31f,
                c1.top + c1.height() * .72f
            ),
            15f,
            150f,
            false,
            paint
        )
        paint.style = Paint.Style.FILL

        val c2 = cells[2]
        paint.color = Color.rgb(244, 53, 86)
        canvas.drawCircle(c2.left + c2.width() * .22f, c2.top + c2.height() * .42f, w * .012f, paint)
        canvas.drawCircle(c2.left + c2.width() * .32f, c2.top + c2.height() * .42f, w * .012f, paint)
        val heart = android.graphics.Path().apply {
            moveTo(c2.left + c2.width() * .17f, c2.top + c2.height() * .43f)
            lineTo(c2.left + c2.width() * .37f, c2.top + c2.height() * .43f)
            lineTo(c2.left + c2.width() * .27f, c2.top + c2.height() * .62f)
            close()
        }
        canvas.drawPath(heart, paint)
        text.color = Color.rgb(68, 48, 34)
        text.textSize = h * .013f
        canvas.drawText("50", c2.left + c2.width() * .63f, c2.top + c2.height() * .50f, text)
        text.textSize = h * .0058f
        canvas.drawText("ITEM        1", c2.centerX(), c2.top + c2.height() * .78f, text)

        val c3 = cells[3]
        text.color = Color.rgb(68, 48, 34)
        text.textSize = h * .0080f
        canvas.drawText("APES   3/10", c3.centerX(), c3.top + c3.height() * .29f, text)
        canvas.drawText("TAG $day", c3.centerX(), c3.top + c3.height() * .52f, text)
        val seconds = (30 - ((now - dayStart) / 1000L)).coerceIn(0, 30)
        text.color = Color.rgb(174, 113, 39)
        canvas.drawText("SUN   ${seconds}s", c3.centerX(), c3.top + c3.height() * .76f, text)

        text.textAlign = Paint.Align.LEFT
        text.textSize = h * .0045f
        text.color = Color.argb(130, 255, 255, 255)
        canvas.drawText("v0.13 debug", w * .008f, h * .121f, text)
        text.textAlign = Paint.Align.CENTER
    }

    private fun drawBottomBar(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val top = h * .906f

        paint.color = Color.rgb(50, 34, 25)
        canvas.drawRect(0f, top, w, h, paint)

        val gap = w * .025f
        val buttonW = (w - gap * 4f) / 3f
        val labels = arrayOf("APES", "BUILD", "+10s")
        val base = arrayOf(
            Color.rgb(51, 147, 79),
            Color.rgb(166, 102, 54),
            Color.rgb(46, 146, 207)
        )

        for (i in 0..2) {
            val l = gap + i * (buttonW + gap)
            val t = top + h * .013f
            val r = l + buttonW
            val b = h - h * .017f

            paint.color = Color.rgb(31, 26, 23)
            canvas.drawRect(l - w * .007f, t - w * .007f, r + w * .007f, b + w * .007f, paint)

            paint.color = if (selectedTab == i && i < 2) lighten(base[i], 20) else base[i]
            canvas.drawRect(l, t, r, b, paint)

            paint.color = lighten(base[i], 35)
            canvas.drawRect(l + w * .010f, t + h * .007f, r - w * .010f, t + h * .011f, paint)

            paint.color = darken(base[i], 25)
            canvas.drawRect(l + w * .010f, b - h * .011f, r - w * .010f, b - h * .007f, paint)

            text.color = Color.WHITE
            text.textSize = h * .0105f
            canvas.drawText(labels[i], (l + r) * .5f, t + (b - t) * .60f, text)
        }
    }

    private fun drawEvent(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()

        paint.color = Color.argb(170, 5, 20, 16)
        canvas.drawRect(0f, 0f, w, h, paint)

        val box = RectF(w * .10f, h * .34f, w * .90f, h * .63f)
        paint.color = Color.rgb(68, 43, 29)
        canvas.drawRect(box, paint)
        paint.color = Color.rgb(244, 230, 187)
        canvas.drawRect(
            box.left + w * .010f,
            box.top + w * .010f,
            box.right - w * .010f,
            box.bottom - w * .010f,
            paint
        )

        text.color = Color.rgb(68, 43, 29)
        text.textSize = h * .017f
        canvas.drawText("BANANENMARKT!", box.centerX(), box.top + box.height() * .22f, text)
        text.textSize = h * .010f
        canvas.drawText("Bananenaktien explodieren.", box.centerX(), box.top + box.height() * .38f, text)
        canvas.drawText("Waehle das kleinere Uebel.", box.centerX(), box.top + box.height() * .49f, text)

        drawChoice(canvas, box.left + box.width() * .10f, box.top + box.height() * .63f, box.width() * .36f, box.height() * .18f, "+20 BANANEN")
        drawChoice(canvas, box.left + box.width() * .54f, box.top + box.height() * .63f, box.width() * .36f, box.height() * .18f, "+1 APES")
    }

    private fun drawChoice(canvas: Canvas, l: Float, t: Float, w: Float, h: Float, label: String) {
        paint.color = Color.rgb(48, 122, 65)
        canvas.drawRect(l, t, l + w, t + h, paint)
        text.color = Color.WHITE
        text.textSize = height * .008f
        canvas.drawText(label, l + w * .5f, t + h * .61f, text)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_UP) return true

        val x = event.x
        val y = event.y
        val w = width.toFloat()
        val h = height.toFloat()

        if (eventOpen) {
            val box = RectF(w * .10f, h * .34f, w * .90f, h * .63f)
            if (y in (box.top + box.height() * .60f)..box.bottom) {
                if (x < box.centerX()) bananas += 20 else bananas += 5
                eventOpen = false
                dayStart = System.currentTimeMillis()
                invalidate()
            }
            return true
        }

        if (y >= h * .906f) {
            when {
                x < w / 3f -> selectedTab = 0
                x < w * 2f / 3f -> selectedTab = 1
                else -> {
                    dayStart -= 10_000L
                    selectedTab = 2
                }
            }
            invalidate()
            return true
        }

        return true
    }

    private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

    private fun lighten(color: Int, amount: Int): Int = Color.rgb(
        (Color.red(color) + amount).coerceAtMost(255),
        (Color.green(color) + amount).coerceAtMost(255),
        (Color.blue(color) + amount).coerceAtMost(255)
    )

    private fun darken(color: Int, amount: Int): Int = Color.rgb(
        (Color.red(color) - amount).coerceAtLeast(0),
        (Color.green(color) - amount).coerceAtLeast(0),
        (Color.blue(color) - amount).coerceAtLeast(0)
    )
}
