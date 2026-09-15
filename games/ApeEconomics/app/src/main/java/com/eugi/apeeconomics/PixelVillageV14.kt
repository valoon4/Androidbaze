package com.eugi.apeeconomics

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import kotlin.math.sin

/**
 * Ape Economics v0.14 visual hotfix.
 * Keeps the v0.13 asset-first world, but replaces the too-small translucent
 * character pass with large opaque sprites and covers the AI-baked gibberish
 * building plaques with deterministic in-game labels.
 */
class PixelVillageV14(context: Context) : FrameLayout(context) {
    private val base = PixelVillageV13(context)
    private val overlay = OverlayView(context)

    init {
        addView(base, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        addView(overlay, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        overlay.isClickable = false
        overlay.isFocusable = false
    }

    fun handleBack(): Boolean = base.handleBack()

    private class OverlayView(context: Context) : View(context) {
        private val paint = Paint().apply {
            isAntiAlias = false
            isFilterBitmap = false
        }
        private val text = Paint().apply {
            isAntiAlias = false
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

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

        private val apes = mutableListOf(
            Ape(.195f, .429f, .195f, .429f, .194f, .245f, worker),
            Ape(.497f, .438f, .497f, .438f, .500f, .252f, politician),
            Ape(.796f, .425f, .796f, .425f, .812f, .252f, banker),
            Ape(.604f, .711f, .604f, .711f, .807f, .750f, trader)
        )

        init {
            val now = System.currentTimeMillis()
            apes.forEachIndexed { i, ape -> ape.since = now + i * 430L }
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val now = System.currentTimeMillis()
            updateApes(now)
            drawCleanLabels(canvas)
            drawLargeOpaqueApes(canvas, now)
            drawVersionTag(canvas)
            postInvalidateOnAnimation()
        }

        private fun updateApes(now: Long) {
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

        private fun drawLargeOpaqueApes(canvas: Canvas, now: Long) {
            val w = width.toFloat()
            val h = height.toFloat()
            val size = w * .086f

            apes.sortedBy { it.y }.forEachIndexed { index, a ->
                val bob = sin((now / 145.0 + index * 1.7)).toFloat() * h * .0014f
                val cx = w * a.x
                val feet = h * a.y + bob

                paint.alpha = 80
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
            canvas.drawRect(
                rect.left + border,
                rect.top + border,
                rect.right - border,
                rect.bottom - border,
                paint
            )

            text.color = Color.rgb(67, 43, 29)
            text.textSize = h * .0082f
            text.textAlign = Paint.Align.CENTER
            canvas.drawText(label, rect.centerX(), rect.centerY() + text.textSize * .34f, text)
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
            canvas.drawText("v0.14 debug", w * .008f, h * .122f, text)
            text.textAlign = Paint.Align.CENTER
        }

        private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t
    }
}
