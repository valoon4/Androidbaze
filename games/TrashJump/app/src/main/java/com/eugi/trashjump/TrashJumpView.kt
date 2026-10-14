package com.eugi.trashjump

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.SystemClock
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * TRASH JUMP. An original, asset-free, progressively cursed endless jumper.
 * The simulation uses world coordinates (positive Y = up) and a scrolling camera.
 */
class TrashJumpView(context: Context) : View(context) {
    private val widthGame = 480f
    private val heightGame = 800f
    private val r = Random(SystemClock.uptimeMillis())
    private val prefs = context.getSharedPreferences("trash_jump_save_v1", Context.MODE_PRIVATE)
    private val pen = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bold = Typeface.create("sans-serif-black", Typeface.BOLD)
    private val regular = Typeface.create("sans-serif", Typeface.NORMAL)

    private enum class GameState { MENU, PLAY, PAUSE, DEAD }
    private enum class PlatformType { NORMAL, MOVING, FRAGILE, SPRING, GLITCH, ICE }
    private enum class PickupType { TICKET, SHIELD, ROCKET, MAGNET, JACKPOT }

    private data class Platform(
        val originX: Float, var x: Float, val y: Float, val w: Float,
        val kind: PlatformType, val seed: Float, var used: Boolean = false
    )
    private data class Pickup(var x: Float, val y: Float, val kind: PickupType, var taken: Boolean = false)
    private data class Enemy(val originX: Float, var x: Float, val y: Float, val seed: Float,
                             val boss: Boolean, var alive: Boolean = true)
    private data class Spark(var x: Float, var y: Float, var vx: Float, var vy: Float,
                             var remaining: Float, val color: Int)

    private val platforms = mutableListOf<Platform>()
    private val pickups = mutableListOf<Pickup>()
    private val enemies = mutableListOf<Enemy>()
    private val sparks = mutableListOf<Spark>()

    private var state = GameState.MENU
    private var px = 240f
    private var py = 151f
    private var vx = 0f
    private var vy = 750f
    private var cameraBottom = 0f
    private var recordHeight = 151f
    private var nextPlatformY = 122f
    private var previousCenter = 240f
    private var inputDirection = 0
    private var lastFrame = SystemClock.uptimeMillis()
    private var clock = 0f
    private var runTime = 0f
    private var shield = 0f
    private var rocket = 0f
    private var magnet = 0f
    private var invulnerable = 0f
    private var bannerTimer = 0f
    private var banner = ""
    private var shake = 0f
    private var currentTier = 0
    private var runTickets = 0
    private var bestMeters = prefs.getInt("best", 0)
    private var bank = prefs.getInt("bank", 0)
    private var boots = prefs.getInt("boots", 0).coerceIn(0, 4)
    private var insurance = prefs.getBoolean("insurance", false)

    private val cyan = Color.rgb(88, 255, 219)
    private val yellow = Color.rgb(255, 229, 84)
    private val pink = Color.rgb(255, 92, 172)
    private val violet = Color.rgb(181, 123, 255)
    private val navy = Color.rgb(18, 18, 42)
    private val white = Color.rgb(245, 247, 255)

    init {
        isClickable = true
        isHapticFeedbackEnabled = true
        resetWorld()
    }

    fun pauseGame() {
        if (state == GameState.PLAY) state = GameState.PAUSE
        inputDirection = 0
    }

    private fun resetWorld() {
        platforms.clear()
        pickups.clear()
        enemies.clear()
        sparks.clear()
        px = 240f
        py = 151f
        vx = 0f
        vy = 750f + boots * 24f
        cameraBottom = 0f
        recordHeight = py
        nextPlatformY = 122f
        previousCenter = 240f
        inputDirection = 0
        runTime = 0f
        shield = if (insurance) 8f else 0f
        rocket = 0f
        magnet = 0f
        invulnerable = 0f
        currentTier = 0
        runTickets = 0
        banner = "WELCOME TO THE DUMP"
        bannerTimer = 2.6f
        shake = 0f
        platforms.add(Platform(174f, 174f, 122f, 132f, PlatformType.NORMAL, 0f))
        while (nextPlatformY < 1500f) generatePlatform()
    }

    private fun begin() {
        resetWorld()
        state = GameState.PLAY
        lastFrame = SystemClock.uptimeMillis()
    }

    private fun save() {
        prefs.edit().putInt("bank", bank).putInt("best", bestMeters)
            .putInt("boots", boots).putBoolean("insurance", insurance).apply()
    }

    private fun endRun() {
        state = GameState.DEAD
        inputDirection = 0
        bestMeters = max(bestMeters, meters())
        save()
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    private fun meters(): Int = (recordHeight / 10f).toInt()
    private fun tier(): Int = max(0, ((recordHeight - 150f) / 540f).toInt())
    private fun mode(): Int = tier() % 6

    private fun modeName(): String = when (mode()) {
        1 -> "MIRROR BRAIN"
        2 -> "MOON SOUP"
        3 -> "WINDY TAX SEASON"
        4 -> "PINBALL APOCALYPSE"
        5 -> "DISCO LANDFILL"
        else -> "NORMAL (SUSPICIOUS)"
    }

    private fun generatePlatform() {
        val difficulty = min(1f, nextPlatformY / 5000f)
        val gap = 95f + r.nextFloat() * (45f + 11f * difficulty)
        nextPlatformY += gap
        val w = 112f - difficulty * 34f + r.nextFloat() * 20f
        // Cap lateral movement: every new ledge remains reachable on a normal jump.
        val dx = (r.nextFloat() - 0.5f) * (205f + difficulty * 35f)
        val x = (previousCenter + dx - w / 2f).coerceIn(18f, widthGame - 18f - w)
        previousCenter = x + w / 2f
        val n = r.nextFloat()
        val kind = if (nextPlatformY < 360f) PlatformType.NORMAL else when {
            n < 0.13f -> PlatformType.MOVING
            n < 0.26f -> PlatformType.FRAGILE
            n < 0.39f -> PlatformType.SPRING
            n < 0.51f -> PlatformType.GLITCH
            n < 0.64f -> PlatformType.ICE
            else -> PlatformType.NORMAL
        }
        platforms.add(Platform(x, x, nextPlatformY, w, kind, r.nextFloat() * 6.28f))
        if (r.nextFloat() < 0.67f) {
            pickups.add(Pickup(x + w / 2f, nextPlatformY + 44f, PickupType.TICKET))
        }
        if (nextPlatformY > 420f && r.nextFloat() < 0.18f) {
            val power = when (r.nextInt(4)) {
                0 -> PickupType.SHIELD
                1 -> PickupType.ROCKET
                2 -> PickupType.MAGNET
                else -> PickupType.JACKPOT
            }
            pickups.add(Pickup(x + w / 2f + (r.nextFloat() - 0.5f) * 70f, nextPlatformY + 94f, power))
        }
        if (nextPlatformY > 700f && r.nextFloat() < 0.17f) {
            enemies.add(Enemy(x + w / 2f, x + w / 2f, nextPlatformY + 94f,
                r.nextFloat() * 6.28f, ((nextPlatformY / 1350f).toInt() % 3 == 2) && r.nextFloat() < 0.3f))
        }
    }

    private fun burst(x: Float, y: Float, color: Int, count: Int = 12) {
        if (sparks.size > 130) sparks.subList(0, sparks.size - 105).clear()
        repeat(count) {
            sparks.add(Spark(x, y, (r.nextFloat() - 0.5f) * 490f,
                (r.nextFloat() - 0.28f) * 450f, 0.25f + r.nextFloat() * 0.42f, color))
        }
    }

    private fun popup(text: String) {
        banner = text
        bannerTimer = 2f
    }

    private fun takePickup(item: Pickup) {
        item.taken = true
        when (item.kind) {
            PickupType.TICKET -> {
                val value = if (mode() == 5) 2 else 1
                bank += value
                runTickets += value
                burst(item.x, item.y, yellow, 6)
            }
            PickupType.SHIELD -> {
                shield = max(shield, 11f)
                popup("GARBAGE ARMOR ONLINE")
                burst(item.x, item.y, cyan, 18)
            }
            PickupType.ROCKET -> {
                rocket = 1.5f
                vy = max(vy, 1080f)
                popup("ROCKET-FUELED BAD IDEA")
                burst(item.x, item.y, pink, 25)
            }
            PickupType.MAGNET -> {
                magnet = 13f
                popup("TRASH MAGNET ACTIVATED")
                burst(item.x, item.y, violet, 16)
            }
            PickupType.JACKPOT -> {
                bank += 12
                runTickets += 12
                popup("ILLEGAL +12 TICKETS")
                burst(item.x, item.y, yellow, 28)
            }
        }
        if (item.kind != PickupType.TICKET) performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        save()
    }

    private fun bounce(p: Platform) {
        val launch = 750f + boots * 24f
        vy = when (p.kind) {
            PlatformType.SPRING -> launch * 1.39f
            PlatformType.ICE -> launch * 1.06f
            else -> launch
        }
        py = p.y + 20f
        when (p.kind) {
            PlatformType.FRAGILE -> {
                p.used = true
                burst(px, p.y, yellow, 13)
            }
            PlatformType.GLITCH -> {
                px = widthGame - px
                vx = -vx
                popup("YOU HAVE BEEN REPOSITIONED")
                shake = 0.4f
                burst(px, py, violet, 16)
            }
            PlatformType.SPRING -> {
                shake = 0.28f
                burst(px, py, pink, 15)
            }
            PlatformType.ICE -> vx += inputDirection * 140f
            else -> burst(px, py - 18f, cyan, 4)
        }
        if (mode() == 4) vx += (if (px < 240f) 1f else -1f) * 225f
        performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    private fun update(dt: Float) {
        clock += dt
        if (state != GameState.PLAY) return
        runTime += dt
        shield = max(0f, shield - dt)
        rocket = max(0f, rocket - dt)
        magnet = max(0f, magnet - dt)
        invulnerable = max(0f, invulnerable - dt)
        bannerTimer = max(0f, bannerTimer - dt)
        shake = max(0f, shake - dt * 2.6f)
        val m = mode()
        var steer = inputDirection.toFloat()
        if (m == 1) steer *= -1f
        val wind = if (m == 3) sin(runTime * 2.1).toFloat() * 135f else 0f
        val steeringSpeed = if (m == 4) 455f else 355f
        val friction = if (m == 4) 0.982f else 0.93f
        px += (steer * steeringSpeed + wind + vx) * dt
        vx *= friction
        if (px < -22f) px += widthGame + 44f
        if (px > widthGame + 22f) px -= widthGame + 44f

        val oldFeet = py - 20f
        val gravity = if (m == 2) 680f else 1130f
        vy -= gravity * dt
        if (rocket > 0f) vy = max(vy, 1080f)
        py += vy * dt
        recordHeight = max(recordHeight, py)
        val newTier = tier()
        if (newTier != currentTier) {
            currentTier = newTier
            popup("REALITY ERROR: " + modeName())
            burst(px, py + 60f, violet, 22)
            shake = 0.4f
        }
        cameraBottom = max(cameraBottom, py - 465f)

        for (p in platforms) {
            if (p.kind == PlatformType.MOVING) {
                p.x = (p.originX + sin(runTime * 1.75f + p.seed).toFloat() * 53f)
                    .coerceIn(8f, widthGame - p.w - 8f)
            }
        }
        if (vy < 0 && rocket <= 0f) {
            for (p in platforms) {
                if (!p.used && oldFeet >= p.y && py - 20f <= p.y &&
                    px + 19f > p.x && px - 19f < p.x + p.w) {
                    bounce(p)
                    break
                }
            }
        }
        for (enemy in enemies) {
            if (!enemy.alive) continue
            enemy.x = enemy.originX + sin(runTime * (if (enemy.boss) 1.15f else 1.85f) +
                enemy.seed).toFloat() * (if (enemy.boss) 98f else 76f)
            if (abs(enemy.x - px) < (if (enemy.boss) 53f else 33f) &&
                abs(enemy.y - py) < (if (enemy.boss) 46f else 32f)) {
                if (vy < -60f && oldFeet >= enemy.y + 8f) {
                    enemy.alive = false
                    vy = 875f
                    bank += if (enemy.boss) 20 else 5
                    runTickets += if (enemy.boss) 20 else 5
                    save()
                    popup(if (enemy.boss) "BOSS AUDIT FAILED +20" else "TAX EVASION +5")
                    burst(enemy.x, enemy.y, yellow, 25)
                    shake = 0.35f
                } else if (rocket > 0f || shield > 0f || invulnerable > 0f) {
                    enemy.alive = false
                    if (rocket <= 0f && invulnerable <= 0f) {
                        shield = 0f
                        invulnerable = 1.6f
                        popup("SHIELD ATE THE AUDIT")
                        shake = 0.45f
                    }
                    burst(enemy.x, enemy.y, pink, 16)
                } else {
                    endRun()
                    return
                }
            }
        }
        for (item in pickups) {
            if (item.taken) continue
            if (magnet > 0f && item.kind == PickupType.TICKET) {
                val dx = px - item.x
                val dy = py - item.y
                if (dx * dx + dy * dy < 160f * 160f) item.x += dx * min(1f, dt * 8f)
            }
            val dx = item.x - px
            val dy = item.y - py
            if (dx * dx + dy * dy < 34f * 34f) takePickup(item)
        }
        for (s in sparks) {
            s.x += s.vx * dt
            s.y += s.vy * dt
            s.vy -= 730f * dt
            s.remaining -= dt
        }
        sparks.removeAll { it.remaining <= 0f }
        while (nextPlatformY < cameraBottom + 1340f) generatePlatform()
        platforms.removeAll { it.y < cameraBottom - 140f }
        pickups.removeAll { it.taken || it.y < cameraBottom - 130f }
        enemies.removeAll { !it.alive || it.y < cameraBottom - 130f }
        if (py < cameraBottom - 80f) endRun()
    }

    private fun worldToScreen(y: Float) = heightGame - (y - cameraBottom)

    private fun set(color: Int, alpha: Int = 255, stroke: Float = 0f) {
        pen.shader = null
        pen.color = color
        pen.alpha = alpha.coerceIn(0, 255)
        pen.style = if (stroke > 0f) Paint.Style.STROKE else Paint.Style.FILL
        pen.strokeWidth = stroke
        pen.strokeCap = Paint.Cap.ROUND
        pen.strokeJoin = Paint.Join.ROUND
    }

    private fun box(c: Canvas, x: Float, y: Float, w: Float, h: Float,
                    radius: Float, color: Int, alpha: Int = 255) {
        set(color, alpha)
        c.drawRoundRect(RectF(x, y, x + w, y + h), radius, radius, pen)
    }

    private fun line(c: Canvas, x1: Float, y1: Float, x2: Float, y2: Float,
                     color: Int, thickness: Float = 3f) {
        set(color, 255, thickness)
        c.drawLine(x1, y1, x2, y2, pen)
        pen.style = Paint.Style.FILL
    }

    private fun text(c: Canvas, msg: String, x: Float, y: Float, size: Float, color: Int,
                     align: Paint.Align = Paint.Align.CENTER, heavy: Boolean = true) {
        set(color)
        pen.typeface = if (heavy) bold else regular
        pen.textSize = size
        pen.textAlign = align
        pen.letterSpacingCompat()
        c.drawText(msg, x, y, pen)
    }

    // Kept local because android.graphics.Paint does not support TextView's letterSpacing.
    private fun Paint.letterSpacingCompat() { }

    private fun bg(c: Canvas) {
        val scheme = when (mode()) {
            1 -> intArrayOf(Color.rgb(58, 24, 86), Color.rgb(17, 19, 49))
            2 -> intArrayOf(Color.rgb(24, 56, 95), Color.rgb(10, 22, 48))
            3 -> intArrayOf(Color.rgb(66, 30, 51), Color.rgb(22, 18, 44))
            4 -> intArrayOf(Color.rgb(71, 34, 31), Color.rgb(20, 20, 43))
            5 -> intArrayOf(Color.rgb(57, 28, 96), Color.rgb(16, 15, 51))
            else -> intArrayOf(Color.rgb(20, 43, 80), Color.rgb(15, 18, 43))
        }
        pen.style = Paint.Style.FILL
        pen.alpha = 255
        pen.shader = LinearGradient(0f, 0f, 0f, heightGame,
            scheme[0], scheme[1], Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, widthGame, heightGame, pen)
        pen.shader = null
        for (i in 0 until 85) {
            val x = ((i * 97 + 51) % 481).toFloat()
            val y = ((i * 173 + 41f + cameraBottom * (if (i % 4 == 0) 0.19f else 0.08f))
                % (heightGame + 45f)) - 20f
            set(if (i % 7 == 0) pink else cyan, if (i % 4 == 0) 75 else 35)
            c.drawCircle(x, y, if (i % 5 == 0) 2.6f else 1.4f, pen)
        }
        // Parallax landfill silhouette.
        val parallax = (cameraBottom * 0.12f) % 90f
        for (i in -1..12) {
            val x = i * 43f
            val h = 72f + ((i * 67 + 270) % 92)
            box(c, x, heightGame - h + parallax, 40f, h + 95f, 3f,
                Color.rgb(33, 38, 72), 140)
            for (j in 0..2) {
                for (k in 0..2) {
                    box(c, x + 8f + j * 10f, heightGame - h + 12f + k * 21f + parallax,
                        4f, 6f, 1f, yellow, if ((i + j + k) % 3 == 0) 65 else 16)
                }
            }
        }
    }

    private fun drawPlatforms(c: Canvas) {
        for (p in platforms) {
            val y = worldToScreen(p.y)
            if (y < -35f || y > heightGame + 30f || p.used) continue
            val color = when (p.kind) {
                PlatformType.NORMAL -> cyan
                PlatformType.MOVING -> pink
                PlatformType.FRAGILE -> yellow
                PlatformType.SPRING -> Color.rgb(132, 255, 116)
                PlatformType.GLITCH -> violet
                PlatformType.ICE -> Color.rgb(114, 217, 255)
            }
            box(c, p.x + 2f, y + 7f, p.w, 18f, 8f, Color.BLACK, 85)
            box(c, p.x, y, p.w, 18f, 8f, color)
            box(c, p.x + 6f, y + 3f, p.w - 12f, 4f, 2f, white, 95)
            when (p.kind) {
                PlatformType.MOVING -> text(c, "< >", p.x + p.w / 2f, y + 15f, 15f, navy)
                PlatformType.FRAGILE -> {
                    line(c, p.x + p.w * 0.39f, y + 2f, p.x + p.w * 0.52f, y + 13f, navy, 2f)
                    line(c, p.x + p.w * 0.52f, y + 13f, p.x + p.w * 0.62f, y + 3f, navy, 2f)
                }
                PlatformType.SPRING -> text(c, "BOING", p.x + p.w / 2f, y + 14f, 13f, navy)
                PlatformType.GLITCH -> text(c, "???", p.x + p.w / 2f, y + 14f, 14f, navy)
                PlatformType.ICE -> text(c, "SLIP", p.x + p.w / 2f, y + 14f, 13f, navy)
                else -> Unit
            }
        }
    }

    private fun drawPickups(c: Canvas) {
        for (item in pickups) {
            if (item.taken) continue
            val y = worldToScreen(item.y) + sin(clock * 4f + item.y * 0.019f).toFloat() * 5f
            if (y < -32f || y > heightGame + 32f) continue
            when (item.kind) {
                PickupType.TICKET -> {
                    set(yellow)
                    c.drawCircle(item.x, y, 13f, pen)
                    set(Color.rgb(255, 147, 53))
                    c.drawCircle(item.x, y, 9.4f, pen)
                    text(c, "$", item.x, y + 5f, 16f, yellow)
                }
                else -> {
                    val tint = when (item.kind) {
                        PickupType.SHIELD -> cyan
                        PickupType.ROCKET -> pink
                        PickupType.MAGNET -> violet
                        else -> yellow
                    }
                    set(tint, 55)
                    c.drawCircle(item.x, y, 29f + sin(clock * 5f).toFloat() * 3f, pen)
                    set(tint)
                    c.drawCircle(item.x, y, 19f, pen)
                    text(c, when (item.kind) {
                        PickupType.SHIELD -> "S"
                        PickupType.ROCKET -> "R"
                        PickupType.MAGNET -> "M"
                        else -> "12"
                    }, item.x, y + 6f, 18f, navy)
                }
            }
        }
    }

    private fun drawEnemies(c: Canvas) {
        for (e in enemies) {
            if (!e.alive) continue
            val y = worldToScreen(e.y)
            if (y < -80f || y > heightGame + 90f) continue
            val radius = if (e.boss) 47f else 29f
            box(c, e.x - radius, y - radius * 0.68f, radius * 2f,
                radius * 1.37f, radius * 0.46f, if (e.boss) violet else pink)
            box(c, e.x - radius * 0.7f, y - radius * 0.45f, radius * 1.4f,
                radius * 0.43f, 7f, navy)
            set(yellow)
            c.drawCircle(e.x - radius * 0.30f, y - radius * 0.23f, 4.6f, pen)
            c.drawCircle(e.x + radius * 0.30f, y - radius * 0.23f, 4.6f, pen)
            text(c, if (e.boss) "BIG TAX" else "TAX", e.x,
                y + radius * 0.38f, if (e.boss) 16f else 13f, navy)
            line(c, e.x - radius * 0.5f, y + radius * 0.7f,
                e.x - radius * 0.65f, y + radius, white, 2.7f)
            line(c, e.x + radius * 0.5f, y + radius * 0.7f,
                e.x + radius * 0.65f, y + radius, white, 2.7f)
        }
    }

    private fun drawGoblin(c: Canvas) {
        val y = worldToScreen(py)
        if (invulnerable > 0f && (clock * 14f).toInt() % 2 == 0) return
        c.save()
        c.translate(px, y)
        val tilt = (inputDirection * 10f + sin(clock * 9f).toFloat() * 2f)
        c.rotate(tilt)
        if (rocket > 0f) {
            set(pink, 220)
            val flame = 36f + r.nextFloat() * 32f
            val path = Path().apply {
                moveTo(-12f, 15f); lineTo(0f, flame + 22f)
                lineTo(12f, 15f); close()
            }
            c.drawPath(path, pen)
        }
        if (shield > 0f) {
            set(cyan, 160, 4f)
            c.drawCircle(0f, 0f, 33f + sin(clock * 8f).toFloat() * 2f, pen)
        }
        // Bag ears, squashy body, junk-can crown and unreasonably confident face.
        set(Color.rgb(41, 46, 69))
        c.drawOval(RectF(-20f, -12f, 20f, 25f), pen)
        set(Color.rgb(113, 250, 162))
        c.drawOval(RectF(-23f, -25f, 23f, 22f), pen)
        val ear = Path().apply {
            moveTo(-20f, -9f); lineTo(-30f, -29f); lineTo(-8f, -22f); close()
        }
        c.drawPath(ear, pen)
        val ear2 = Path().apply {
            moveTo(20f, -9f); lineTo(30f, -29f); lineTo(8f, -22f); close()
        }
        c.drawPath(ear2, pen)
        box(c, -14f, -37f, 28f, 14f, 4f, Color.rgb(185, 196, 215))
        box(c, -18f, -27f, 36f, 5f, 2f, Color.rgb(114, 128, 161))
        set(navy)
        c.drawCircle(-9f, -5f, 5f, pen)
        c.drawCircle(9f, -5f, 5f, pen)
        set(white)
        c.drawCircle(-7f, -7f, 2f, pen)
        c.drawCircle(11f, -7f, 2f, pen)
        line(c, -5f, 9f, 5f, 11f, navy, 3f)
        line(c, 5f, 11f, 11f, 6f, navy, 3f)
        c.restore()
    }

    private fun drawSparks(c: Canvas) {
        for (s in sparks) {
            val y = worldToScreen(s.y)
            set(s.color, (min(1f, s.remaining * 2f) * 255f).toInt())
            c.drawCircle(s.x, y, 2f + s.remaining * 4f, pen)
        }
    }

    private fun hud(c: Canvas) {
        box(c, 12f, 18f, 456f, 83f, 20f, navy, 224)
        text(c, "TRASH", 32f, 46f, 22f, cyan, Paint.Align.LEFT)
        text(c, "JUMP", 32f, 73f, 22f, pink, Paint.Align.LEFT)
        text(c, "${meters()}m", 250f, 57f, 27f, white)
        text(c, "$runTickets tickets", 252f, 85f, 16f, yellow)
        box(c, 405f, 31f, 47f, 52f, 12f, violet)
        text(c, "II", 428.5f, 67f, 25f, navy)
        box(c, 17f, 110f, 446f, 30f, 10f, navy, 195)
        text(c, modeName(), 240f, 132f, 17f,
            if (mode() == 0) cyan else yellow)
        if (shield > 0f || rocket > 0f || magnet > 0f) {
            val labels = buildList {
                if (shield > 0f) add("SHIELD ${shield.toInt()}s")
                if (rocket > 0f) add("ROCKET!")
                if (magnet > 0f) add("MAGNET ${magnet.toInt()}s")
            }
            text(c, labels.joinToString("   "), 240f, 164f, 15f, white)
        }
        if (bannerTimer > 0f) {
            val y = if (shield > 0f || rocket > 0f || magnet > 0f) 191f else 172f
            box(c, 28f, y, 424f, 37f, 13f, Color.rgb(255, 221, 101),
                (min(1f, bannerTimer) * 245f).toInt())
            text(c, banner, 240f, y + 25f, 15f, navy)
        }
        // Two touch zones are always subtly visible for first-time discoverability.
        box(c, 12f, 749f, 102f, 35f, 13f, navy, inputDirection == -1 ? 210 : 115)
        box(c, 366f, 749f, 102f, 35f, 13f, navy, inputDirection == 1 ? 210 : 115)
        text(c, "< LEFT", 63f, 774f, 17f, cyan)
        text(c, "RIGHT >", 416f, 774f, 17f, cyan)
    }

    private fun button(c: Canvas, x: Float, y: Float, w: Float, h: Float,
                       label: String, accent: Int, size: Float = 19f) {
        box(c, x + 2f, y + 6f, w, h, 17f, Color.BLACK, 130)
        box(c, x, y, w, h, 17f, accent)
        box(c, x + 9f, y + 5f, w - 18f, 5f, 2f, white, 90)
        text(c, label, x + w / 2f, y + h / 2f + size * 0.36f, size, navy)
    }

    private fun overlay(c: Canvas) {
        if (state == GameState.PLAY) return
        set(navy, 198)
        c.drawRect(0f, 0f, widthGame, heightGame, pen)
        when (state) {
            GameState.MENU -> {
                text(c, "TRASH", 240f, 219f, 83f, cyan)
                text(c, "JUMP", 240f, 302f, 92f, pink)
                text(c, "THE FLOOR IS A SCAM", 240f, 338f, 21f, yellow)
                box(c, 38f, 377f, 404f, 101f, 18f, navy, 224)
                text(c, "HOLD LEFT OR RIGHT TO STEER", 240f, 410f, 19f, white)
                text(c, "BOUNCE. ESCAPE. GET WEIRD.", 240f, 446f, 18f, cyan)
                button(c, 67f, 501f, 346f, 72f, "START THE DISASTER", yellow, 22f)
                text(c, "BANK: $bank TICKETS   |   BEST: ${bestMeters}m",
                    240f, 609f, 17f, white)
                val bootLabel = if (boots >= 4) "BOOTS MAX" else "BOOTS ${boots}/4: ${55 * (boots + 1)}"
                button(c, 32f, 645f, 199f, 63f, bootLabel, cyan, 15f)
                button(c, 248f, 645f, 199f, 63f,
                    if (insurance) "SHIELD OWNED" else "SHIELD: 85", violet, 15f)
                text(c, "UPGRADES ARE PERMANENT", 240f, 748f, 14f, white)
            }
            GameState.PAUSE -> {
                text(c, "PAUSE", 240f, 257f, 72f, cyan)
                text(c, "THE GARBAGE CAN WAIT", 240f, 315f, 20f, white)
                button(c, 76f, 418f, 328f, 76f, "RESUME THE MESS", yellow)
                button(c, 76f, 527f, 328f, 69f, "QUIT TO MENU", pink)
            }
            GameState.DEAD -> {
                text(c, "YOU BECAME", 240f, 227f, 43f, yellow)
                text(c, "LITTER", 240f, 304f, 85f, pink)
                text(c, "ALTITUDE: ${meters()}m", 240f, 375f, 28f, white)
                text(c, "+$runTickets TRASH TICKETS", 240f, 414f, 20f, yellow)
                text(c, "PERSONAL BEST: ${bestMeters}m", 240f, 454f, 19f, cyan)
                button(c, 71f, 505f, 338f, 77f, "ONE MORE DISASTER", yellow)
                button(c, 71f, 612f, 338f, 65f, "BACK TO DUMP", violet)
            }
            else -> Unit
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val now = SystemClock.uptimeMillis()
        val dt = ((now - lastFrame).coerceIn(0L, 34L)) / 1000f
        lastFrame = now
        update(dt)
        canvas.save()
        canvas.scale(width / widthGame, height / heightGame)
        bg(canvas)
        canvas.save()
        if (shake > 0f) {
            canvas.translate(sin(clock * 84f).toFloat() * shake * 13f,
                cos(clock * 68f).toFloat() * shake * 9f)
        }
        drawPlatforms(canvas)
        drawPickups(canvas)
        drawEnemies(canvas)
        drawSparks(canvas)
        drawGoblin(canvas)
        canvas.restore()
        if (state == GameState.PLAY) hud(canvas)
        overlay(canvas)
        canvas.restore()
        postInvalidateOnAnimation()
    }

    private fun inside(x: Float, y: Float, l: Float, t: Float, w: Float, h: Float) =
        x >= l && x <= l + w && y >= t && y <= t + h

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x * widthGame / max(width.toFloat(), 1f)
        val y = event.y * heightGame / max(height.toFloat(), 1f)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                when (state) {
                    GameState.MENU -> {
                        if (inside(x, y, 32f, 645f, 199f, 63f)) {
                            val price = 55 * (boots + 1)
                            if (boots < 4 && bank >= price) {
                                bank -= price
                                boots++
                                save()
                                popup("ROCKET BOOTS UPDATED")
                            } else if (boots < 4) popup("NOT ENOUGH TRASH TICKETS")
                        } else if (inside(x, y, 248f, 645f, 199f, 63f)) {
                            if (!insurance && bank >= 85) {
                                bank -= 85
                                insurance = true
                                save()
                                popup("INSURANCE ACQUIRED")
                            } else if (!insurance) popup("NOT ENOUGH TRASH TICKETS")
                        } else if (inside(x, y, 67f, 501f, 346f, 72f)) begin()
                    }
                    GameState.PLAY -> {
                        if (y < 108f && x > 392f) {
                            state = GameState.PAUSE
                            inputDirection = 0
                        } else inputDirection = if (x < widthGame / 2f) -1 else 1
                    }
                    GameState.PAUSE -> {
                        if (inside(x, y, 76f, 418f, 328f, 76f)) {
                            state = GameState.PLAY
                            lastFrame = SystemClock.uptimeMillis()
                        } else if (inside(x, y, 76f, 527f, 328f, 69f)) {
                            state = GameState.MENU
                            resetWorld()
                        }
                    }
                    GameState.DEAD -> {
                        if (inside(x, y, 71f, 505f, 338f, 77f)) begin()
                        else if (inside(x, y, 71f, 612f, 338f, 65f)) {
                            resetWorld()
                            state = GameState.MENU
                        }
                    }
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (state == GameState.PLAY) inputDirection = if (x < 240f) -1 else 1
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                inputDirection = 0
                performClick()
                return true
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
