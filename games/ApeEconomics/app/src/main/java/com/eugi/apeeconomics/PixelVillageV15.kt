package com.eugi.apeeconomics

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import kotlin.math.sin

/**
 * Ape Economics v0.15 visual/navigation hotfix.
 *
 * The v0.13 gameplay/HUD remains the logic source. This layer repaints the
 * world band so the old tiny character pass is fully hidden, then draws one
 * authoritative set of large opaque apes on walkable routes only.
 */
class PixelVillageV15(context: Context) : FrameLayout(context) {
    private val base = PixelVillageV13(context)
    private val overlay = OverlayView(context)

    init {
        addView(base, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        addView(overlay, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        overlay.isClickable = true
        overlay.isFocusable = true
    }

    fun handleBack(): Boolean {
        val local = overlay.handleBackLocal()
        val baseHandled = base.handleBack()
        return local || baseHandled
    }

    private inner class OverlayView(context: Context) : View(context) {
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

        // All resting points are on actual ground/path, never inside the river.
        private val apes = mutableListOf(
            Ape(.195f, .333f, .195f, .333f, .195f, .292f, worker),
            Ape(.500f, .336f, .500f, .336f, .500f, .294f, politician),
            Ape(.805f, .334f, .805f, .334f, .812f, .296f, banker),
            Ape(.610f, .695f, .610f, .695f, .805f, .742f, trader)
        )

        private var localDay = 1
        private var localDayStart = System.currentTimeMillis()
        private var localEventOpen = false

        init {
            val now = System.currentTimeMillis()
            apes.forEachIndexed { i, ape -> ape.since = now + i * 430L }
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val now = System.currentTimeMillis()
            updateApes(now)
            updateLocalDay(now)
            repaintWorldBand(canvas)
            drawCleanLabels(canvas)
            drawLargeOpaqueApes(canvas, now)
            drawVersionTag(canvas)
            if (localEventOpen) drawLocalEvent(canvas)
            postInvalidateOnAnimation()
        }

        /** Repaint just the playable world to erase the old v0.13 mini-sprites. */
        private fun repaintWorldBand(canvas: Canvas) {
            val w = width.toFloat()
            val h = height.toFloat()
            val topRatio = .128f
            val bottomRatio = .906f
            val src = Rect(
                0,
                (world.height * topRatio).toInt(),
                world.width,
                (world.height * bottomRatio).toInt()
            )
            val dst = RectF(0f, h * topRatio, w, h * bottomRatio)
            paint.alpha = 255
            paint.isFilterBitmap = false
            canvas.drawBitmap(world, src, dst, paint)
        }

        private fun updateApes(now: Long) {
            apes.forEach { a ->
                val duration = when (a.phase) {
                    0 -> 1100L
                    1 -> 2400L
                    2 -> 2400L
                    else -> 2400L
                }

                if (now - a.since >= duration) {
                    a.phase = (a.phase + 1) % 4
                    a.since = now
                }

                val f = ((now - a.since).toFloat() / duration).coerceIn(0f, 1f)
                when (a.phase) {
                    0 -> {
                        a.x = a.homeX
                        a.y = a.homeY
                    }
                    1 -> {
                        val q = routePoint(a.homeX, a.homeY, a.jobX, a.jobY, f)
                        a.x = q.first
                        a.y = q.second
                    }
                    2 -> {
                        a.x = a.jobX
                        a.y = a.jobY
                    }
                    else -> {
                        val q = routePoint(a.jobX, a.jobY, a.homeX, a.homeY, f)
                        a.x = q.first
                        a.y = q.second
                    }
                }
            }
        }

        /**
         * If a future NPC route crosses the river, it is forced through the
         * central bridge. Same-side routes stay direct and never touch water.
         */
        private fun routePoint(x0: Float, y0: Float, x1: Float, y1: Float, t: Float): Pair<Float, Float> {
            val riverTop = .355f
            val riverBottom = .474f
            val sameTop = y0 < riverTop && y1 < riverTop
            val sameBottom = y0 > riverBottom && y1 > riverBottom
            if (sameTop || sameBottom) return lerp(x0, x1, t) to lerp(y0, y1, t)

            val bridgeX = .500f
            val entryY = if (y0 < riverTop) .347f else .486f
            val exitY = if (y1 < riverTop) .347f else .486f
            return when {
                t < 1f / 3f -> {
                    val u = t * 3f
                    lerp(x0, bridgeX, u) to lerp(y0, entryY, u)
                }
                t < 2f / 3f -> {
                    val u = (t - 1f / 3f) * 3f
                    bridgeX to lerp(entryY, exitY, u)
                }
                else -> {
                    val u = (t - 2f / 3f) * 3f
                    lerp(bridgeX, x1, u) to lerp(exitY, y1, u)
                }
            }
        }

        private fun drawLargeOpaqueApes(canvas: Canvas, now: Long) {
            val w = width.toFloat()
            val h = height.toFloat()
            val size = w * .086f

            apes.sortedBy { it.y }.forEachIndexed { index, a ->
                val bob = sin((now / 145.0 + index * 1.7)).toFloat() * h * .0014f
                val cx = w * a.x
                val feet = h * a.y + bob

                paint.alpha = 82
                paint.color = Color.rgb(18, 44, 28)
                canvas.drawOval(
                    RectF(cx - size * .34f, feet - size * .08f, cx + size * .34f, feet + size * .10f),
                    paint
                )

                paint.alpha = 255
                paint.isFilterBitmap = false
                canvas.drawBitmap(
                    a.sprite,
                    null,
                    RectF(cx - size * .50f, feet - size, cx + size * .50f, feet),
                    paint
                )
            }
            paint.alpha = 255
        }

        private fun drawCleanLabels(canvas: Canvas) {
            plaque(canvas, .195f, .311f, .176f, "PLANTAGE")
            plaque(canvas, .500f, .314f, .184f, "PARLAMENT")
            plaque(canvas, .815f, .321f, .132f, "BANK")
            plaque(canvas, .815f, .822f, .150f, "MARKT")
        }

        private fun plaque(canvas: Canvas, cx: Float, cy: Float, widthRatio: Float, label: String) {
            val w = width.toFloat()
            val h = height.toFloat()
            val plaqueW = w * widthRatio
            val plaqueH = h * .0225f
            val border = (w * .005f).coerceAtLeast(2f)
            val rect = RectF(
                w * cx - plaqueW * .5f,
                h * cy - plaqueH * .5f,
                w * cx + plaqueW * .5f,
                h * cy + plaqueH * .5f
            )

            paint.alpha = 255
            paint.color = Color.rgb(74, 47, 31)
            canvas.drawRect(rect, paint)
            paint.color = Color.rgb(241, 219, 164)
            canvas.drawRect(rect.left + border, rect.top + border, rect.right - border, rect.bottom - border, paint)

            text.color = Color.rgb(67, 43, 29)
            text.textSize = h * .0082f
            text.textAlign = Paint.Align.CENTER
            canvas.drawText(label, rect.centerX(), rect.centerY() + text.textSize * .34f, text)
        }

        private fun updateLocalDay(now: Long) {
            if (!localEventOpen && now - localDayStart >= 30_000L) {
                localDay += 1
                localDayStart = now
                if (localDay % 3 == 0) localEventOpen = true
            }
        }

        private fun drawLocalEvent(canvas: Canvas) {
            val w = width.toFloat()
            val h = height.toFloat()
            paint.color = Color.argb(170, 5, 20, 16)
            canvas.drawRect(0f, 0f, w, h, paint)

            val box = RectF(w * .10f, h * .34f, w * .90f, h * .63f)
            paint.color = Color.rgb(68, 43, 29)
            canvas.drawRect(box, paint)
            paint.color = Color.rgb(244, 230, 187)
            canvas.drawRect(box.left + w * .010f, box.top + w * .010f, box.right - w * .010f, box.bottom - w * .010f, paint)

            text.color = Color.rgb(68, 43, 29)
            text.textSize = h * .017f
            canvas.drawText("BANANENMARKT!", box.centerX(), box.top + box.height() * .22f, text)
            text.textSize = h * .010f
            canvas.drawText("Bananenaktien explodieren.", box.centerX(), box.top + box.height() * .38f, text)
            canvas.drawText("Waehle das kleinere Uebel.", box.centerX(), box.top + box.height() * .49f, text)
            choice(canvas, box.left + box.width() * .10f, box.top + box.height() * .63f, box.width() * .36f, box.height() * .18f, "+20 BANANEN")
            choice(canvas, box.left + box.width() * .54f, box.top + box.height() * .63f, box.width() * .36f, box.height() * .18f, "+1 APES")
        }

        private fun choice(canvas: Canvas, l: Float, t: Float, w: Float, h: Float, label: String) {
            paint.color = Color.rgb(48, 122, 65)
            canvas.drawRect(l, t, l + w, t + h, paint)
            text.color = Color.WHITE
            text.textSize = height * .008f
            canvas.drawText(label, l + w * .5f, t + h * .61f, text)
        }

        private fun drawVersionTag(canvas: Canvas) {
            val w = width.toFloat()
            val h = height.toFloat()
            paint.alpha = 255
            paint.color = Color.rgb(18, 52, 43)
            canvas.drawRect(0f, h * .1145f, w * .105f, h * .128f, paint)
            text.textAlign = Paint.Align.LEFT
            text.color = Color.argb(180, 255, 255, 255)
            text.textSize = h * .0045f
            canvas.drawText("v0.15 debug", w * .008f, h * .122f, text)
            text.textAlign = Paint.Align.CENTER
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            // Keep the real gameplay view authoritative for buttons/economy.
            base.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_UP) {
                val w = width.toFloat()
                val h = height.toFloat()
                if (localEventOpen) {
                    val box = RectF(w * .10f, h * .34f, w * .90f, h * .63f)
                    if (event.y in (box.top + box.height() * .60f)..box.bottom) {
                        localEventOpen = false
                        localDayStart = System.currentTimeMillis()
                    }
                } else if (event.y >= h * .906f && event.x >= w * 2f / 3f) {
                    localDayStart -= 10_000L
                }
            }
            return true
        }

        fun handleBackLocal(): Boolean {
            if (!localEventOpen) return false
            localEventOpen = false
            return true
        }

        private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t
    }
}
