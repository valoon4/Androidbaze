package com.valoon4.wormblade

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUi()
        setContentView(WormbladeGameView(this))
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUi()
    }

    private fun hideSystemUi() {
        if (Build.VERSION.SDK_INT >= 30) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            run {
                window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            }
        }
    }
}

private class WormbladeGameView(context: android.content.Context) : View(context) {
    private data class Projectile(var x: Float, var y: Float, var vy: Float, val damage: Int)
    private data class Drop(val type: Int, var x: Float, var y: Float, var vy: Float)
    private data class FloatText(val text: String, var x: Float, var y: Float, var life: Float, val color: Int)
    private data class Worm(
        val id: Int,
        var x: Float,
        var y: Float,
        var phase: Float,
        val speed: Float,
        val amplitude: Float,
        val spacing: Float,
        val radius: Float,
        val hp: IntArray
    )
    private data class SpawnPlan(
        val at: Float,
        val xFrac: Float,
        val segments: Int,
        val speed: Float,
        val amplitudeFrac: Float,
        val hp: Int
    )

    companion object {
        private const val DROP_COIN = 0
        private const val DROP_GEM = 1
        private const val DROP_HEART = 2
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val path = Path()
    private val rng = Random(1337)
    private val projectiles = ArrayList<Projectile>()
    private val worms = ArrayList<Worm>()
    private val drops = ArrayList<Drop>()
    private val floatTexts = ArrayList<FloatText>()
    private val spawns = ArrayList<SpawnPlan>()
    private val bladeCooldown = HashMap<Long, Float>()

    private var lastFrameNs = 0L
    private var gameTime = 0f
    private var waveTime = 0f
    private var fireTimer = 0f
    private var transitionTimer = 0f
    private var nextWormId = 1
    private var currentWave = 1
    private var hp = 1000
    private var maxHp = 1000
    private var coins = 0
    private var gems = 0
    private var level = 1
    private var xp = 0
    private var paused = false
    private var gameOver = false
    private var victory = false
    private var playerX = 0f
    private var playerY = 0f
    private var invulnUntil = 0f
    private var dragging = false

    init {
        isFocusable = true
        keepScreenOn = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        playerX = w * 0.5f
        playerY = h * 0.735f
        restart()
    }

    private fun restart() {
        projectiles.clear()
        worms.clear()
        drops.clear()
        floatTexts.clear()
        bladeCooldown.clear()
        gameTime = 0f
        waveTime = 0f
        fireTimer = 0f
        transitionTimer = 0f
        nextWormId = 1
        currentWave = 1
        hp = 1000
        maxHp = 1000
        coins = 0
        gems = 0
        level = 1
        xp = 0
        paused = false
        gameOver = false
        victory = false
        invulnUntil = 0f
        playerX = width * 0.5f
        playerY = height * 0.735f
        prepareWave(1)
        lastFrameNs = System.nanoTime()
        invalidate()
    }

    private fun prepareWave(wave: Int) {
        spawns.clear()
        transitionTimer = 0f
        val count = when (wave) { 1 -> 3; 2 -> 4; else -> 5 }
        for (i in 0 until count) {
            val side = if (i % 2 == 0) 0.28f else 0.72f
            val jitter = ((i % 3) - 1) * 0.055f
            spawns += SpawnPlan(
                at = 0.25f + i * 1.05f,
                xFrac = side + jitter,
                segments = 9 + wave * 2 + (i % 2),
                speed = 170f + wave * 22f + i * 6f,
                amplitudeFrac = 0.035f + (i % 3) * 0.012f,
                hp = 75 + wave * 25
            )
        }
        waveTime = 0f
    }

    override fun onDraw(canvas: android.graphics.Canvas) {
        super.onDraw(canvas)
        val now = System.nanoTime()
        if (lastFrameNs == 0L) lastFrameNs = now
        val dt = min(0.034f, (now - lastFrameNs) / 1_000_000_000f)
        lastFrameNs = now
        if (!paused && !gameOver && !victory) updateGame(dt)
        drawBackground(canvas)
        drawWorld(canvas)
        drawHud(canvas)
        if (paused) drawOverlay(canvas, "PAUSED", "Tap pause to continue")
        if (gameOver) drawOverlay(canvas, "GAME OVER", "Tap to restart")
        if (victory) drawOverlay(canvas, "LEVEL CLEAR!", "Tap to play again")
        postInvalidateOnAnimation()
    }

    private fun updateGame(dt: Float) {
        gameTime += dt
        waveTime += dt
        fireTimer -= dt
        if (fireTimer <= 0f) {
            fireTimer += max(0.11f, 0.19f - (level - 1) * 0.008f)
            projectiles += Projectile(playerX, playerY - width * 0.075f, -height * 0.72f, 46 + level * 3)
        }

        val spawnIt = spawns.iterator()
        while (spawnIt.hasNext()) {
            val plan = spawnIt.next()
            if (waveTime >= plan.at) {
                spawnWorm(plan)
                spawnIt.remove()
            }
        }

        val pIt = projectiles.iterator()
        while (pIt.hasNext()) {
            val p = pIt.next()
            p.y += p.vy * dt
            if (p.y < -80f) { pIt.remove(); continue }
            var hit = false
            loop@ for (worm in worms) {
                for (i in worm.hp.indices) {
                    if (worm.hp[i] <= 0) continue
                    val pos = segmentPosition(worm, i)
                    val dx = p.x - pos.first
                    val dy = p.y - pos.second
                    val rr = worm.radius * 0.72f
                    if (dx * dx + dy * dy <= rr * rr) {
                        damageSegment(worm, i, p.damage, pos.first, pos.second)
                        hit = true
                        break@loop
                    }
                }
            }
            if (hit) pIt.remove()
        }

        val bladeRadius = width * 0.165f
        val angle = gameTime * 4.1f
        val bladePositions = arrayOf(
            Pair(playerX + cos(angle) * bladeRadius, playerY + sin(angle) * bladeRadius * 0.55f),
            Pair(playerX + cos(angle + Math.PI.toFloat()) * bladeRadius, playerY + sin(angle + Math.PI.toFloat()) * bladeRadius * 0.55f)
        )

        val wormIt = worms.iterator()
        while (wormIt.hasNext()) {
            val worm = wormIt.next()
            worm.y += worm.speed * densityScale() * dt
            worm.phase += dt * 1.7f
            for (i in worm.hp.indices) {
                if (worm.hp[i] <= 0) continue
                val pos = segmentPosition(worm, i)
                for (bp in bladePositions) {
                    val dx = pos.first - bp.first
                    val dy = pos.second - bp.second
                    val hitRadius = worm.radius * 0.72f + width * 0.038f
                    if (dx * dx + dy * dy < hitRadius * hitRadius) {
                        val key = (worm.id.toLong() shl 32) or (i.toLong() and 0xffffffffL)
                        val readyAt = bladeCooldown[key] ?: 0f
                        if (gameTime >= readyAt) {
                            bladeCooldown[key] = gameTime + 0.16f
                            damageSegment(worm, i, 32 + level * 4, pos.first, pos.second)
                        }
                    }
                }
                val dx = pos.first - playerX
                val dy = pos.second - playerY
                val danger = worm.radius * 0.78f + width * 0.047f
                if (gameTime >= invulnUntil && dx * dx + dy * dy < danger * danger) {
                    hp = max(0, hp - 85)
                    invulnUntil = gameTime + 0.72f
                    floatTexts += FloatText("-85", playerX, playerY - 60f, 0.8f, Color.rgb(244, 68, 83))
                    if (hp <= 0) gameOver = true
                }
            }
            if (worm.hp.all { it <= 0 }) { wormIt.remove(); continue }
            if (segmentPosition(worm, 0).second > height * 0.81f) {
                hp = max(0, hp - 120)
                floatTexts += FloatText("-120", playerX, playerY - 70f, 0.85f, Color.rgb(244, 68, 83))
                wormIt.remove()
                if (hp <= 0) gameOver = true
            }
        }

        val dIt = drops.iterator()
        while (dIt.hasNext()) {
            val d = dIt.next()
            d.y += d.vy * dt
            var dx = d.x - playerX
            var dy = d.y - playerY
            val magnet = width * 0.11f
            if (dx * dx + dy * dy < magnet * magnet) {
                d.x += (playerX - d.x) * min(1f, dt * 7f)
                d.y += (playerY - d.y) * min(1f, dt * 7f)
                dx = d.x - playerX
                dy = d.y - playerY
            }
            val collect = width * 0.055f
            if (dx * dx + dy * dy < collect * collect) {
                when (d.type) {
                    DROP_COIN -> { coins++; floatTexts += FloatText("+1", playerX + 30f, playerY - 30f, 0.7f, Color.rgb(255, 207, 53)) }
                    DROP_GEM -> { gems++; gainXp(1) }
                    DROP_HEART -> { hp = min(maxHp, hp + 150); floatTexts += FloatText("+150", playerX, playerY - 45f, 0.8f, Color.rgb(76, 220, 120)) }
                }
                dIt.remove()
            } else if (d.y > height * 0.86f) dIt.remove()
        }

        val fIt = floatTexts.iterator()
        while (fIt.hasNext()) {
            val f = fIt.next()
            f.life -= dt
            f.y -= height * 0.035f * dt
            if (f.life <= 0f) fIt.remove()
        }

        if (spawns.isEmpty() && worms.isEmpty() && !gameOver) {
            transitionTimer += dt
            if (transitionTimer > 1.4f) {
                if (currentWave >= 3) victory = true else { currentWave++; prepareWave(currentWave) }
            }
        }
    }

    private fun densityScale(): Float = max(0.75f, width / 1080f)

    private fun gainXp(amount: Int) {
        xp += amount
        val need = 3 + level
        if (xp >= need && level < 10) {
            xp -= need
            level++
            maxHp += 45
            hp = min(maxHp, hp + 120)
            floatTexts += FloatText("LEVEL UP!", playerX, playerY - 110f, 1.1f, Color.rgb(88, 220, 255))
        }
    }

    private fun spawnWorm(plan: SpawnPlan) {
        val radius = width * 0.045f
        worms += Worm(
            nextWormId++, width * plan.xFrac, -radius * 1.5f, rng.nextFloat() * 6.28f,
            plan.speed, width * plan.amplitudeFrac, radius * 1.68f, radius,
            IntArray(plan.segments) { plan.hp }
        )
    }

    private fun segmentPosition(worm: Worm, index: Int): Pair<Float, Float> = Pair(
        worm.x + sin(worm.phase + index * 0.58f) * worm.amplitude,
        worm.y - index * worm.spacing
    )

    private fun damageSegment(worm: Worm, index: Int, damage: Int, x: Float, y: Float) {
        if (worm.hp[index] <= 0) return
        worm.hp[index] -= damage
        floatTexts += FloatText(damage.toString(), x + (rng.nextFloat() - 0.5f) * 28f, y - 10f, 0.58f, Color.WHITE)
        if (worm.hp[index] <= 0) {
            worm.hp[index] = 0
            val roll = rng.nextFloat()
            val type = when { roll < 0.08f -> DROP_HEART; roll < 0.20f -> DROP_GEM; roll < 0.72f -> DROP_COIN; else -> -1 }
            if (type >= 0) drops += Drop(type, x, y, height * (0.055f + rng.nextFloat() * 0.025f))
        }
    }

    private fun drawBackground(canvas: android.graphics.Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        paint.shader = LinearGradient(0f, 0f, 0f, h, Color.rgb(221, 174, 79), Color.rgb(238, 194, 99), Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, w, h, paint); paint.shader = null
        paint.color = Color.rgb(73, 144, 58)
        canvas.drawRect(0f, 0f, w * 0.095f, h, paint); canvas.drawRect(w * 0.905f, 0f, w, h, paint)
        paint.color = Color.rgb(112, 171, 69)
        for (i in 0..11) {
            val y = h * (i / 11f)
            canvas.drawCircle(w * 0.075f, y, w * 0.055f, paint)
            canvas.drawCircle(w * 0.925f, y + h * 0.035f, w * 0.055f, paint)
        }
        paint.color = Color.argb(38, 110, 72, 25)
        for (i in 0 until 18) {
            val x = w * (0.14f + ((i * 37) % 70) / 100f)
            val y = h * (0.10f + ((i * 61) % 70) / 100f)
            canvas.drawOval(RectF(x, y, x + w * 0.025f, y + h * 0.009f), paint)
        }
        drawSideDecor(canvas, w * 0.035f, h * 0.17f, false)
        drawSideDecor(canvas, w * 0.965f, h * 0.30f, true)
        drawSideDecor(canvas, w * 0.045f, h * 0.58f, false)
        drawSideDecor(canvas, w * 0.955f, h * 0.68f, true)
        for (i in 0..5) drawFlower(canvas, if (i % 2 == 0) w * 0.055f else w * 0.945f, h * (0.10f + i * 0.145f), w * 0.012f)
    }

    private fun drawSideDecor(canvas: android.graphics.Canvas, cx: Float, cy: Float, right: Boolean) {
        val w = width.toFloat()
        paint.color = Color.rgb(86, 91, 79)
        canvas.drawRoundRect(RectF(cx - w * 0.055f, cy - w * 0.065f, cx + w * 0.055f, cy + w * 0.065f), w * 0.022f, w * 0.022f, paint)
        paint.color = Color.rgb(53, 62, 57)
        stroke.color = Color.rgb(57, 210, 245); stroke.strokeWidth = w * 0.009f
        val runeX = cx + if (right) -w * 0.012f else w * 0.012f
        canvas.drawCircle(runeX, cy, w * 0.024f, paint); canvas.drawCircle(runeX, cy, w * 0.017f, stroke)
        canvas.drawLine(runeX - w * 0.011f, cy, runeX + w * 0.011f, cy, stroke)
    }

    private fun drawFlower(canvas: android.graphics.Canvas, cx: Float, cy: Float, r: Float) {
        paint.color = Color.rgb(104, 203, 252)
        for (i in 0 until 5) { val a = i * 6.283f / 5f; canvas.drawCircle(cx + cos(a) * r, cy + sin(a) * r, r * 0.55f, paint) }
        paint.color = Color.rgb(255, 222, 73); canvas.drawCircle(cx, cy, r * 0.45f, paint)
    }

    private fun drawWorld(canvas: android.graphics.Canvas) {
        for (worm in worms) drawWorm(canvas, worm)
        for (drop in drops) drawDrop(canvas, drop)
        for (p in projectiles) drawProjectile(canvas, p)
        drawPlayer(canvas)
        for (f in floatTexts) {
            val alpha = (255 * min(1f, f.life * 1.8f)).toInt().coerceIn(0, 255)
            drawOutlinedText(canvas, f.text, f.x, f.y, width * 0.043f,
                Color.argb(alpha, Color.red(f.color), Color.green(f.color), Color.blue(f.color)),
                Color.argb(alpha, 35, 31, 26), Paint.Align.CENTER)
        }
    }

    private fun drawWorm(canvas: android.graphics.Canvas, worm: Worm) {
        for (i in worm.hp.indices.reversed()) {
            if (worm.hp[i] <= 0) continue
            val (x, y) = segmentPosition(worm, i)
            if (y < -worm.radius * 2f || y > height + worm.radius * 2f) continue
            val r = worm.radius
            paint.color = Color.argb(55, 80, 50, 20)
            canvas.drawOval(RectF(x - r * 0.85f, y + r * 0.50f, x + r * 0.85f, y + r * 1.02f), paint)
            paint.shader = LinearGradient(x - r, y - r, x + r, y + r, Color.rgb(255, 225, 83), Color.rgb(243, 161, 30), Shader.TileMode.CLAMP)
            canvas.drawCircle(x, y, r, paint); paint.shader = null
            stroke.color = Color.rgb(149, 89, 20); stroke.strokeWidth = r * 0.10f; canvas.drawCircle(x, y, r, stroke)
            paint.color = Color.argb(190, 255, 255, 220); canvas.drawCircle(x - r * 0.30f, y - r * 0.33f, r * 0.18f, paint)
            paint.color = Color.rgb(245, 180, 42)
            canvas.drawCircle(x - r * 0.57f, y - r * 0.68f, r * 0.16f, paint); canvas.drawCircle(x + r * 0.57f, y - r * 0.68f, r * 0.16f, paint)
            if (i == 0) {
                paint.color = Color.rgb(46, 44, 38)
                canvas.drawCircle(x - r * 0.30f, y + r * 0.05f, r * 0.11f, paint); canvas.drawCircle(x + r * 0.30f, y + r * 0.05f, r * 0.11f, paint)
                paint.color = Color.WHITE
                canvas.drawCircle(x - r * 0.33f, y + r * 0.01f, r * 0.035f, paint); canvas.drawCircle(x + r * 0.27f, y + r * 0.01f, r * 0.035f, paint)
                paint.color = Color.rgb(237, 104, 85)
                canvas.drawCircle(x - r * 0.55f, y + r * 0.32f, r * 0.12f, paint); canvas.drawCircle(x + r * 0.55f, y + r * 0.32f, r * 0.12f, paint)
                stroke.color = Color.rgb(81, 57, 35); stroke.strokeWidth = r * 0.06f
                path.reset(); path.moveTo(x - r * 0.16f, y + r * 0.34f); path.quadTo(x, y + r * 0.46f, x + r * 0.16f, y + r * 0.34f); canvas.drawPath(path, stroke)
            }
        }
    }

    private fun drawProjectile(canvas: android.graphics.Canvas, p: Projectile) {
        val rw = width * 0.012f; val rh = width * 0.044f
        paint.color = Color.argb(65, 64, 220, 255); canvas.drawCircle(p.x, p.y, rw * 2.4f, paint)
        path.reset(); path.moveTo(p.x, p.y - rh); path.lineTo(p.x + rw, p.y); path.lineTo(p.x, p.y + rh * 0.55f); path.lineTo(p.x - rw, p.y); path.close()
        paint.color = Color.rgb(78, 214, 255); canvas.drawPath(path, paint)
        stroke.color = Color.WHITE; stroke.strokeWidth = max(2f, width * 0.003f); canvas.drawPath(path, stroke)
    }

    private fun drawPlayer(canvas: android.graphics.Canvas) {
        val w = width.toFloat(); val r = w * 0.058f
        paint.color = Color.argb(55, 30, 35, 30)
        canvas.drawOval(RectF(playerX - r, playerY + r * 0.72f, playerX + r, playerY + r * 1.16f), paint)
        val bladeRadius = w * 0.165f; val angle = gameTime * 4.1f
        drawOrbitBlade(canvas, playerX + cos(angle) * bladeRadius, playerY + sin(angle) * bladeRadius * 0.55f, angle * 57.2958f + 20f)
        drawOrbitBlade(canvas, playerX + cos(angle + Math.PI.toFloat()) * bladeRadius, playerY + sin(angle + Math.PI.toFloat()) * bladeRadius * 0.55f, angle * 57.2958f + 200f)
        stroke.color = Color.argb(150, 110, 226, 255); stroke.strokeWidth = w * 0.008f
        canvas.drawArc(RectF(playerX - bladeRadius, playerY - bladeRadius * 0.55f, playerX + bladeRadius, playerY + bladeRadius * 0.55f), 205f, 95f, false, stroke)
        canvas.drawArc(RectF(playerX - bladeRadius, playerY - bladeRadius * 0.55f, playerX + bladeRadius, playerY + bladeRadius * 0.55f), 25f, 95f, false, stroke)
        paint.color = Color.rgb(34, 91, 130)
        canvas.drawRoundRect(RectF(playerX - r * 0.62f, playerY + r * 0.42f, playerX - r * 0.16f, playerY + r * 0.95f), r * 0.17f, r * 0.17f, paint)
        canvas.drawRoundRect(RectF(playerX + r * 0.16f, playerY + r * 0.42f, playerX + r * 0.62f, playerY + r * 0.95f), r * 0.17f, r * 0.17f, paint)
        paint.shader = LinearGradient(playerX - r, playerY - r, playerX + r, playerY + r, Color.rgb(75, 192, 246), Color.rgb(30, 111, 178), Shader.TileMode.CLAMP)
        canvas.drawRoundRect(RectF(playerX - r * 0.78f, playerY - r * 0.18f, playerX + r * 0.78f, playerY + r * 0.70f), r * 0.32f, r * 0.32f, paint); paint.shader = null
        stroke.color = Color.rgb(18, 62, 99); stroke.strokeWidth = r * 0.09f
        canvas.drawRoundRect(RectF(playerX - r * 0.78f, playerY - r * 0.18f, playerX + r * 0.78f, playerY + r * 0.70f), r * 0.32f, r * 0.32f, stroke)
        paint.shader = LinearGradient(playerX, playerY - r * 1.15f, playerX, playerY + r * 0.08f, Color.rgb(193, 239, 255), Color.rgb(43, 143, 207), Shader.TileMode.CLAMP)
        canvas.drawCircle(playerX, playerY - r * 0.40f, r * 0.75f, paint); paint.shader = null
        stroke.color = Color.rgb(16, 67, 106); stroke.strokeWidth = r * 0.09f; canvas.drawCircle(playerX, playerY - r * 0.40f, r * 0.75f, stroke)
        paint.color = Color.rgb(25, 36, 58)
        canvas.drawRoundRect(RectF(playerX - r * 0.54f, playerY - r * 0.62f, playerX + r * 0.54f, playerY - r * 0.20f), r * 0.20f, r * 0.20f, paint)
        paint.color = Color.rgb(138, 230, 255)
        canvas.drawCircle(playerX - r * 0.22f, playerY - r * 0.40f, r * 0.055f, paint); canvas.drawCircle(playerX + r * 0.22f, playerY - r * 0.40f, r * 0.055f, paint)
        paint.color = Color.rgb(25, 75, 111)
        canvas.drawRoundRect(RectF(playerX - r * 0.24f, playerY + r * 0.10f, playerX + r * 0.24f, playerY + r * 0.46f), r * 0.08f, r * 0.08f, paint)
        paint.color = Color.rgb(75, 235, 255); canvas.drawCircle(playerX, playerY + r * 0.28f, r * 0.075f, paint)
        if (gameTime < invulnUntil && (gameTime * 12f).toInt() % 2 == 0) {
            stroke.color = Color.argb(210, 255, 255, 255); stroke.strokeWidth = w * 0.006f; canvas.drawCircle(playerX, playerY - r * 0.10f, r * 1.15f, stroke)
        }
    }

    private fun drawOrbitBlade(canvas: android.graphics.Canvas, x: Float, y: Float, rotation: Float) {
        val s = width * 0.052f
        canvas.save(); canvas.translate(x, y); canvas.rotate(rotation)
        paint.color = Color.rgb(18, 77, 116); canvas.drawCircle(0f, 0f, s * 0.28f, paint)
        stroke.color = Color.rgb(9, 46, 72); stroke.strokeWidth = s * 0.12f; canvas.drawCircle(0f, 0f, s * 0.28f, stroke)
        path.reset(); path.moveTo(-s * 0.10f, 0f); path.quadTo(-s * 0.55f, -s * 0.52f, -s * 1.05f, -s * 0.12f); path.quadTo(-s * 0.60f, -s * 0.02f, -s * 0.28f, s * 0.17f); path.close()
        paint.shader = LinearGradient(-s, -s * 0.5f, 0f, s * 0.3f, Color.rgb(104, 224, 255), Color.rgb(22, 117, 195), Shader.TileMode.CLAMP)
        canvas.drawPath(path, paint); paint.shader = null; stroke.color = Color.rgb(12, 60, 97); stroke.strokeWidth = s * 0.10f; canvas.drawPath(path, stroke)
        path.reset(); path.moveTo(s * 0.10f, 0f); path.quadTo(s * 0.55f, s * 0.52f, s * 1.05f, s * 0.12f); path.quadTo(s * 0.60f, s * 0.02f, s * 0.28f, -s * 0.17f); path.close()
        paint.shader = LinearGradient(0f, -s * 0.3f, s, s * 0.5f, Color.rgb(104, 224, 255), Color.rgb(22, 117, 195), Shader.TileMode.CLAMP)
        canvas.drawPath(path, paint); paint.shader = null; canvas.drawPath(path, stroke); canvas.restore()
    }

    private fun drawDrop(canvas: android.graphics.Canvas, d: Drop) {
        val r = width * 0.025f
        when (d.type) {
            DROP_COIN -> {
                paint.color = Color.rgb(255, 187, 29); canvas.drawCircle(d.x, d.y, r, paint)
                stroke.color = Color.rgb(184, 112, 17); stroke.strokeWidth = r * 0.20f; canvas.drawCircle(d.x, d.y, r, stroke)
                paint.color = Color.rgb(255, 239, 126); canvas.drawCircle(d.x - r * 0.28f, d.y - r * 0.30f, r * 0.22f, paint)
            }
            DROP_GEM -> {
                path.reset(); path.moveTo(d.x, d.y - r * 1.2f); path.lineTo(d.x + r, d.y - r * 0.20f); path.lineTo(d.x + r * 0.55f, d.y + r); path.lineTo(d.x - r * 0.55f, d.y + r); path.lineTo(d.x - r, d.y - r * 0.20f); path.close()
                paint.color = Color.rgb(45, 207, 255); canvas.drawPath(path, paint)
                stroke.color = Color.rgb(21, 91, 184); stroke.strokeWidth = r * 0.16f; canvas.drawPath(path, stroke)
                paint.color = Color.WHITE; canvas.drawCircle(d.x - r * 0.25f, d.y - r * 0.32f, r * 0.14f, paint)
            }
            DROP_HEART -> drawHeart(canvas, d.x, d.y, r * 1.15f, Color.rgb(246, 72, 94))
        }
    }

    private fun drawHud(canvas: android.graphics.Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        paint.color = Color.rgb(64, 70, 91)
        canvas.drawRoundRect(RectF(w * 0.035f, h * 0.018f, w * 0.145f, h * 0.082f), w * 0.018f, w * 0.018f, paint)
        stroke.color = Color.rgb(27, 30, 43); stroke.strokeWidth = w * 0.006f
        canvas.drawRoundRect(RectF(w * 0.035f, h * 0.018f, w * 0.145f, h * 0.082f), w * 0.018f, w * 0.018f, stroke)
        paint.color = Color.WHITE
        canvas.drawRoundRect(RectF(w * 0.070f, h * 0.031f, w * 0.085f, h * 0.069f), 6f, 6f, paint)
        canvas.drawRoundRect(RectF(w * 0.098f, h * 0.031f, w * 0.113f, h * 0.069f), 6f, 6f, paint)
        drawOutlinedText(canvas, "LEVEL 1-1", w * 0.50f, h * 0.055f, w * 0.067f, Color.WHITE, Color.rgb(37, 31, 29), Paint.Align.CENTER)
        paint.color = Color.rgb(47, 48, 69)
        canvas.drawRoundRect(RectF(w * 0.76f, h * 0.020f, w * 0.96f, h * 0.078f), w * 0.026f, w * 0.026f, paint)
        drawMiniCoin(canvas, w * 0.80f, h * 0.049f, w * 0.026f)
        drawOutlinedText(canvas, coins.toString(), w * 0.90f, h * 0.061f, w * 0.052f, Color.WHITE, Color.rgb(35, 31, 35), Paint.Align.CENTER)
        val barLeft = w * 0.30f; val barRight = w * 0.70f; val barY = h * 0.108f
        stroke.color = Color.rgb(52, 52, 59); stroke.strokeWidth = w * 0.012f; canvas.drawLine(barLeft, barY, barRight, barY, stroke)
        for (i in 0..3) {
            val x = barLeft + (barRight - barLeft) * i / 3f
            paint.color = if (i < currentWave) Color.rgb(87, 200, 238) else Color.rgb(118, 118, 126)
            canvas.drawCircle(x, barY, w * 0.016f, paint); stroke.color = Color.rgb(49, 49, 55); stroke.strokeWidth = w * 0.005f; canvas.drawCircle(x, barY, w * 0.016f, stroke)
        }
        drawFlag(canvas, barRight + w * 0.025f, barY)
        drawOutlinedText(canvas, "WAVE $currentWave/3", w * 0.50f, h * 0.143f, w * 0.043f, Color.WHITE, Color.rgb(55, 44, 34), Paint.Align.CENTER)
        val hpTop = h * 0.895f
        drawOutlinedText(canvas, "HP", w * 0.045f, hpTop, w * 0.046f, Color.WHITE, Color.rgb(42, 37, 34), Paint.Align.LEFT)
        val heartY = h * 0.875f
        for (i in 0 until 5) drawHeart(canvas, w * (0.18f + i * 0.063f), heartY, w * 0.025f, if (hp / maxHp.toFloat() > i / 5f) Color.rgb(243, 63, 78) else Color.rgb(104, 85, 80))
        val bar = RectF(w * 0.045f, h * 0.915f, w * 0.43f, h * 0.955f)
        paint.color = Color.rgb(46, 48, 50); canvas.drawRoundRect(bar, w * 0.018f, w * 0.018f, paint)
        val inner = RectF(bar.left + w * 0.008f, bar.top + w * 0.008f, bar.right - w * 0.008f, bar.bottom - w * 0.008f)
        paint.color = Color.rgb(94, 116, 55); canvas.drawRoundRect(inner, w * 0.012f, w * 0.012f, paint)
        val hpFrac = hp / maxHp.toFloat()
        paint.shader = LinearGradient(inner.left, inner.top, inner.left, inner.bottom, Color.rgb(116, 231, 47), Color.rgb(66, 180, 36), Shader.TileMode.CLAMP)
        canvas.drawRoundRect(RectF(inner.left, inner.top, inner.left + inner.width() * hpFrac, inner.bottom), w * 0.012f, w * 0.012f, paint); paint.shader = null
        drawOutlinedText(canvas, "$hp/$maxHp", bar.centerX(), h * 0.947f, w * 0.037f, Color.WHITE, Color.rgb(35, 39, 33), Paint.Align.CENTER)
        drawSkillSlot(canvas, w * 0.57f, h * 0.91f, true, "Lv.$level")
        drawSkillSlot(canvas, w * 0.73f, h * 0.91f, level >= 5, "Lv.5")
        drawSkillSlot(canvas, w * 0.89f, h * 0.91f, level >= 10, "Lv.10")
        drawMiniGem(canvas, w * 0.54f, h * 0.858f, w * 0.016f)
        drawOutlinedText(canvas, "$gems  XP $xp/${3 + level}", w * 0.575f, h * 0.867f, w * 0.025f, Color.WHITE, Color.rgb(50, 45, 40), Paint.Align.LEFT)
    }

    private fun drawSkillSlot(canvas: android.graphics.Canvas, cx: Float, cy: Float, unlocked: Boolean, label: String) {
        val s = width * 0.115f
        paint.color = if (unlocked) Color.rgb(33, 135, 205) else Color.rgb(54, 55, 70)
        canvas.drawRoundRect(RectF(cx - s * 0.52f, cy - s * 0.58f, cx + s * 0.52f, cy + s * 0.58f), s * 0.13f, s * 0.13f, paint)
        stroke.color = Color.rgb(25, 31, 45); stroke.strokeWidth = s * 0.07f
        canvas.drawRoundRect(RectF(cx - s * 0.52f, cy - s * 0.58f, cx + s * 0.52f, cy + s * 0.58f), s * 0.13f, s * 0.13f, stroke)
        if (unlocked) {
            canvas.save(); canvas.translate(cx, cy - s * 0.08f); canvas.scale(0.55f, 0.55f); drawOrbitBlade(canvas, 0f, 0f, -18f); canvas.restore()
        } else {
            stroke.color = Color.rgb(165, 168, 182); stroke.strokeWidth = s * 0.09f
            canvas.drawArc(RectF(cx - s * 0.18f, cy - s * 0.25f, cx + s * 0.18f, cy + s * 0.12f), 190f, 160f, false, stroke)
            paint.color = Color.rgb(165, 168, 182); canvas.drawRoundRect(RectF(cx - s * 0.22f, cy - s * 0.05f, cx + s * 0.22f, cy + s * 0.24f), s * 0.05f, s * 0.05f, paint)
        }
        drawOutlinedText(canvas, label, cx, cy + s * 0.47f, s * 0.25f, Color.WHITE, Color.rgb(32, 32, 40), Paint.Align.CENTER)
    }

    private fun drawHeart(canvas: android.graphics.Canvas, cx: Float, cy: Float, r: Float, color: Int) {
        path.reset(); path.moveTo(cx, cy + r * 0.92f)
        path.cubicTo(cx - r * 1.35f, cy + r * 0.05f, cx - r * 1.05f, cy - r * 0.92f, cx - r * 0.38f, cy - r * 0.78f)
        path.cubicTo(cx - r * 0.08f, cy - r * 0.72f, cx, cy - r * 0.45f, cx, cy - r * 0.32f)
        path.cubicTo(cx, cy - r * 0.45f, cx + r * 0.08f, cy - r * 0.72f, cx + r * 0.38f, cy - r * 0.78f)
        path.cubicTo(cx + r * 1.05f, cy - r * 0.92f, cx + r * 1.35f, cy + r * 0.05f, cx, cy + r * 0.92f); path.close()
        paint.color = color; canvas.drawPath(path, paint); stroke.color = Color.rgb(104, 39, 43); stroke.strokeWidth = max(2f, r * 0.13f); canvas.drawPath(path, stroke)
        paint.color = Color.argb(150, 255, 255, 255); canvas.drawCircle(cx - r * 0.33f, cy - r * 0.35f, r * 0.13f, paint)
    }

    private fun drawMiniCoin(canvas: android.graphics.Canvas, cx: Float, cy: Float, r: Float) {
        paint.color = Color.rgb(255, 185, 30); canvas.drawCircle(cx, cy, r, paint)
        stroke.color = Color.rgb(185, 115, 20); stroke.strokeWidth = r * 0.18f; canvas.drawCircle(cx, cy, r, stroke)
        drawOutlinedText(canvas, "★", cx, cy + r * 0.34f, r * 0.85f, Color.rgb(255, 237, 114), Color.rgb(208, 135, 19), Paint.Align.CENTER)
    }

    private fun drawMiniGem(canvas: android.graphics.Canvas, cx: Float, cy: Float, r: Float) {
        path.reset(); path.moveTo(cx, cy - r); path.lineTo(cx + r * 0.86f, cy - r * 0.15f); path.lineTo(cx + r * 0.45f, cy + r); path.lineTo(cx - r * 0.45f, cy + r); path.lineTo(cx - r * 0.86f, cy - r * 0.15f); path.close()
        paint.color = Color.rgb(49, 210, 255); canvas.drawPath(path, paint)
    }

    private fun drawFlag(canvas: android.graphics.Canvas, x: Float, y: Float) {
        stroke.color = Color.rgb(73, 67, 68); stroke.strokeWidth = width * 0.008f; canvas.drawLine(x, y - width * 0.035f, x, y + width * 0.035f, stroke)
        path.reset(); path.moveTo(x, y - width * 0.035f); path.lineTo(x + width * 0.050f, y - width * 0.020f); path.lineTo(x, y - width * 0.002f); path.close()
        paint.color = Color.rgb(239, 71, 101); canvas.drawPath(path, paint)
    }

    private fun drawOutlinedText(canvas: android.graphics.Canvas, text: String, x: Float, y: Float, size: Float, fill: Int, outline: Int, align: Paint.Align) {
        textPaint.textSize = size; textPaint.textAlign = align; textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.style = Paint.Style.STROKE; textPaint.strokeWidth = max(2f, size * 0.11f); textPaint.color = outline; canvas.drawText(text, x, y, textPaint)
        textPaint.style = Paint.Style.FILL; textPaint.color = fill; canvas.drawText(text, x, y, textPaint)
    }

    private fun drawOverlay(canvas: android.graphics.Canvas, title: String, subtitle: String) {
        val w = width.toFloat(); val h = height.toFloat()
        paint.color = Color.argb(175, 24, 25, 34); canvas.drawRect(0f, 0f, w, h, paint)
        paint.color = Color.rgb(50, 53, 72)
        canvas.drawRoundRect(RectF(w * 0.15f, h * 0.38f, w * 0.85f, h * 0.60f), w * 0.045f, w * 0.045f, paint)
        stroke.color = Color.rgb(92, 215, 255); stroke.strokeWidth = w * 0.010f
        canvas.drawRoundRect(RectF(w * 0.15f, h * 0.38f, w * 0.85f, h * 0.60f), w * 0.045f, w * 0.045f, stroke)
        drawOutlinedText(canvas, title, w * 0.50f, h * 0.475f, w * 0.075f, Color.WHITE, Color.rgb(20, 25, 36), Paint.Align.CENTER)
        drawOutlinedText(canvas, subtitle, w * 0.50f, h * 0.535f, w * 0.035f, Color.rgb(197, 232, 244), Color.rgb(20, 25, 36), Paint.Align.CENTER)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x; val y = event.y; val w = width.toFloat(); val h = height.toFloat()
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (gameOver || victory) { restart(); return true }
                if (x in w * 0.02f..w * 0.16f && y in 0f..h * 0.10f) { paused = !paused; lastFrameNs = System.nanoTime(); return true }
                if (paused) return true
                dragging = true; playerX = x.coerceIn(w * 0.12f, w * 0.88f); return true
            }
            MotionEvent.ACTION_MOVE -> if (dragging && !paused && !gameOver && !victory) { playerX = x.coerceIn(w * 0.12f, w * 0.88f); return true }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { dragging = false; return true }
        }
        return true
    }
}
