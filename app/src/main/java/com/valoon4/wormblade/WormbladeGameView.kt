package com.valoon4.wormblade

import android.content.Context
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
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class WormbladeGameView(context: Context) : View(context) {
    private enum class Screen { OVERWORLD, GAME }

    private data class Projectile(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        val damage: Int,
        var pierceLeft: Int,
        var lastHitKey: Long = -1L,
        val critical: Boolean = false
    )

    private data class Drop(
        val type: Int,
        var x: Float,
        var y: Float,
        var vy: Float,
        var age: Float = 0f
    )

    private data class FloatText(
        val text: String,
        var x: Float,
        var y: Float,
        var life: Float,
        val color: Int,
        val scale: Float = 1f
    )

    private data class Worm(
        val id: Int,
        var x: Float,
        var y: Float,
        var phase: Float,
        var speed: Float,
        val amplitude: Float,
        val spacing: Float,
        val radius: Float,
        val hp: IntArray,
        val maxHp: IntArray,
        val treasure: BooleanArray,
        val boss: Boolean
    )

    private data class SpawnPlan(
        val at: Float,
        val xFrac: Float,
        val segments: Int,
        val speed: Float,
        val amplitudeFrac: Float,
        val hp: Int,
        val boss: Boolean,
        val seed: Int
    )

    companion object {
        private const val DROP_COIN = 0
        private const val DROP_GEM = 1
        private const val DROP_HEART = 2
        private const val DROP_CHEST = 3
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
    private val rng = Random(20260829)
    private val prefs = context.getSharedPreferences("wormblade_progress", Context.MODE_PRIVATE)

    private val projectiles = ArrayList<Projectile>()
    private val worms = ArrayList<Worm>()
    private val drops = ArrayList<Drop>()
    private val floatTexts = ArrayList<FloatText>()
    private val spawns = ArrayList<SpawnPlan>()
    private val bladeCooldown = HashMap<Long, Float>()
    private val upgradeStacks = HashMap<String, Int>()
    private var upgradeChoices = emptyList<UpgradeOption>()
    private var queuedChests = 0

    private var screen = Screen.OVERWORLD
    private var config = GameContent.levels.first()
    private var currentLevelId = 1
    private var currentWave = 1
    private var lastFrameNs = 0L
    private var gameTime = 0f
    private var waveTime = 0f
    private var fireTimer = 0f
    private var transitionTimer = 0f
    private var nextWormId = 1
    private var volleyCounter = 0
    private var startBanner = 0f

    private var hp = 1000
    private var maxHp = 1000
    private var coins = 0
    private var gems = 0
    private var level = 1
    private var xp = 0
    private var playerX = 0f
    private var playerY = 0f
    private var invulnUntil = 0f
    private var dragging = false

    private var paused = false
    private var gameOver = false
    private var victory = false
    private var renderError: String? = null
    private var clearMask = prefs.getInt("clearMask", 0)
    private var bankCoins = prefs.getInt("bankCoins", 0)

    init {
        isFocusable = true
        keepScreenOn = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (w <= 0 || h <= 0) return
        if (playerX == 0f) playerX = w * 0.5f
        if (playerY == 0f) playerY = h * 0.74f
        lastFrameNs = System.nanoTime()
    }

    fun handleBack(): Boolean {
        if (screen == Screen.OVERWORLD) return false
        if (upgradeChoices.isNotEmpty()) return true
        if (!paused && !gameOver && !victory) {
            paused = true
            invalidate()
            return true
        }
        returnToMap()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 1 || height <= 1) {
            postInvalidateOnAnimation()
            return
        }
        try {
            renderError = null
            val now = System.nanoTime()
            if (lastFrameNs == 0L) lastFrameNs = now
            val dt = min(0.034f, max(0f, (now - lastFrameNs) / 1_000_000_000f))
            lastFrameNs = now
            if (screen == Screen.OVERWORLD) {
                drawOverworld(canvas)
            } else {
                val running = !paused && !gameOver && !victory && upgradeChoices.isEmpty()
                if (running) updateGame(dt)
                drawBackground(canvas)
                drawWorld(canvas)
                drawHud(canvas)
                if (startBanner > 0f && running) drawStartBanner(canvas)
                if (paused) drawPauseOverlay(canvas)
                if (gameOver) drawEndOverlay(canvas, false)
                if (victory) drawEndOverlay(canvas, true)
                if (upgradeChoices.isNotEmpty()) drawUpgradeOverlay(canvas)
            }
        } catch (t: Throwable) {
            renderError = t.javaClass.simpleName + ": " + (t.message ?: "render error")
            drawRenderError(canvas)
        }
        postInvalidateOnAnimation()
    }

    private fun startLevel(id: Int) {
        currentLevelId = id.coerceIn(1, GameContent.levels.size)
        config = GameContent.levels[currentLevelId - 1]
        screen = Screen.GAME
        projectiles.clear()
        worms.clear()
        drops.clear()
        floatTexts.clear()
        spawns.clear()
        bladeCooldown.clear()
        upgradeStacks.clear()
        upgradeChoices = emptyList()
        queuedChests = 0
        gameTime = 0f
        waveTime = 0f
        fireTimer = 0.12f
        transitionTimer = 0f
        nextWormId = 1
        volleyCounter = 0
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
        playerY = height * 0.74f
        startBanner = 3.8f
        prepareWave(1)
        lastFrameNs = System.nanoTime()
        invalidate()
    }

    private fun returnToMap() {
        screen = Screen.OVERWORLD
        paused = false
        gameOver = false
        victory = false
        upgradeChoices = emptyList()
        dragging = false
        lastFrameNs = System.nanoTime()
        invalidate()
    }

    private fun prepareWave(wave: Int) {
        spawns.clear()
        waveTime = 0f
        transitionTimer = 0f
        val baseCount = config.wormsPerWave + (wave - 1) / 2
        val count = if (config.boss && wave == config.waves) max(2, baseCount - 2) else baseCount
        for (i in 0 until count) {
            val lane = when (i % 4) {
                0 -> 0.25f
                1 -> 0.72f
                2 -> 0.43f
                else -> 0.59f
            }
            val hpScale = 1f + (wave - 1) * 0.16f
            spawns += SpawnPlan(
                at = 0.25f + i * max(0.64f, 1.03f - currentLevelId * 0.025f),
                xFrac = lane,
                segments = config.segmentBase + (wave - 1) + (i % 2),
                speed = config.baseSpeed + wave * 8f + i * 3.5f,
                amplitudeFrac = config.amplitude + (i % 3) * 0.010f,
                hp = (config.baseHp * hpScale).toInt(),
                boss = false,
                seed = currentLevelId * 1000 + wave * 100 + i
            )
        }
        if (config.boss && wave == config.waves) {
            spawns += SpawnPlan(
                at = 0.75f + count * 0.82f,
                xFrac = 0.50f,
                segments = config.segmentBase + if (currentLevelId == 10) 11 else 7,
                speed = config.baseSpeed * 0.82f,
                amplitudeFrac = config.amplitude * 1.18f,
                hp = (config.baseHp * if (currentLevelId == 10) 3.1f else 2.45f).toInt(),
                boss = true,
                seed = 99900 + currentLevelId
            )
        }
        if (wave > 1) {
            hp = min(maxHp, hp + 70 + currentLevelId * 5)
            floatTexts += FloatText("WAVE $wave", width * 0.5f, height * 0.42f, 1.2f, config.accentColor, 1.3f)
        }
    }

    private fun updateGame(dt: Float) {
        gameTime += dt
        waveTime += dt
        startBanner = max(0f, startBanner - dt)
        fireTimer -= dt
        if (fireTimer <= 0f) {
            fireVolley()
            fireTimer += fireInterval()
        }

        val spawnIt = spawns.iterator()
        while (spawnIt.hasNext()) {
            val plan = spawnIt.next()
            if (waveTime >= plan.at) {
                spawnWorm(plan)
                spawnIt.remove()
            }
        }

        updateProjectiles(dt)
        updateWorms(dt)
        updateDrops(dt)
        updateFloatText(dt)

        if (spawns.isEmpty() && worms.isEmpty() && !gameOver && upgradeChoices.isEmpty()) {
            transitionTimer += dt
            if (transitionTimer > 1.25f) {
                if (currentWave >= config.waves) completeLevel() else {
                    currentWave++
                    prepareWave(currentWave)
                }
            }
        }
    }

    private fun fireInterval(): Float {
        val stacks = stack("firerate")
        return max(0.065f, 0.19f / (1f + stacks * 0.25f + (level - 1) * 0.035f))
    }

    private fun fireVolley() {
        volleyCounter++
        val shotCount = 1 + stack("multishot") * 2
        val overdrive = stack("overdrive") > 0 && volleyCounter % 8 == 0
        val totalShots = if (overdrive) shotCount * 2 else shotCount
        val baseDamage = 48f * (1f + stack("damage") * 0.40f) * (1f + (level - 1) * 0.06f)
        val projectileSpeed = height * 0.76f
        val spreadStep = when {
            totalShots <= 1 -> 0f
            totalShots <= 3 -> 9f
            totalShots <= 5 -> 7f
            else -> 5.5f
        }
        for (i in 0 until totalShots) {
            val offset = (i - (totalShots - 1) / 2f) * spreadStep
            val radians = offset * PI.toFloat() / 180f
            val crit = rng.nextFloat() < min(0.65f, stack("crit") * 0.12f)
            val damage = (baseDamage * if (crit) 2f else 1f).toInt()
            projectiles += Projectile(
                x = playerX,
                y = playerY - width * 0.072f,
                vx = sin(radians) * projectileSpeed,
                vy = -cos(radians) * projectileSpeed,
                damage = damage,
                pierceLeft = stack("pierce"),
                critical = crit
            )
        }
        if (overdrive) {
            floatTexts += FloatText("OVERDRIVE", playerX, playerY - width * 0.12f, 0.6f, Color.rgb(255, 226, 88), 0.9f)
        }
    }

    private fun updateProjectiles(dt: Float) {
        val pIt = projectiles.iterator()
        while (pIt.hasNext()) {
            val p = pIt.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            if (p.y < -100f || p.x < -80f || p.x > width + 80f) {
                pIt.remove()
                continue
            }
            var remove = false
            hitLoop@ for (worm in worms) {
                for (i in worm.hp.indices) {
                    if (worm.hp[i] <= 0) continue
                    val key = segmentKey(worm.id, i)
                    if (key == p.lastHitKey) continue
                    val pos = segmentPosition(worm, i)
                    val rr = worm.radius * 0.72f + width * 0.009f
                    val dx = p.x - pos.first
                    val dy = p.y - pos.second
                    if (dx * dx + dy * dy <= rr * rr) {
                        damageSegment(worm, i, p.damage, pos.first, pos.second, p.critical)
                        p.lastHitKey = key
                        if (p.pierceLeft <= 0) remove = true else p.pierceLeft--
                        if (remove) break@hitLoop
                    }
                }
            }
            if (remove) pIt.remove()
        }
    }

    private fun updateWorms(dt: Float) {
        val bladeCount = 2 + stack("bladeCount")
        val bladeReach = width * 0.165f * (1f + stack("bladeReach") * 0.18f)
        val bladeSize = width * 0.040f * (1f + stack("bladeReach") * 0.10f)
        val bladeDamage = (34f * (1f + stack("bladeDamage") * 0.35f) * (1f + (level - 1) * 0.04f)).toInt()
        val angle = gameTime * 4.15f
        val speedMultiplier = max(0.60f, 1f - stack("slow") * 0.10f)
        val armorMultiplier = max(0.35f, 1f - stack("armor") * 0.15f)
        val bladePositions = ArrayList<Pair<Float, Float>>(bladeCount)
        for (b in 0 until bladeCount) {
            val a = angle + (2f * PI.toFloat() * b / bladeCount)
            bladePositions += Pair(
                playerX + cos(a) * bladeReach,
                playerY + sin(a) * bladeReach * 0.55f
            )
        }

        val wormIt = worms.iterator()
        while (wormIt.hasNext()) {
            val worm = wormIt.next()
            worm.y += worm.speed * densityScale() * speedMultiplier * dt
            worm.phase += dt * (if (worm.boss) 1.25f else 1.72f)
            for (i in worm.hp.indices) {
                if (worm.hp[i] <= 0) continue
                val pos = segmentPosition(worm, i)
                for (bp in bladePositions) {
                    val dx = pos.first - bp.first
                    val dy = pos.second - bp.second
                    val hitRadius = worm.radius * 0.70f + bladeSize
                    if (dx * dx + dy * dy < hitRadius * hitRadius) {
                        val key = segmentKey(worm.id, i)
                        val readyAt = bladeCooldown[key] ?: 0f
                        if (gameTime >= readyAt) {
                            bladeCooldown[key] = gameTime + 0.17f
                            damageSegment(worm, i, bladeDamage, pos.first, pos.second, false)
                        }
                    }
                }
                val dx = pos.first - playerX
                val dy = pos.second - playerY
                val danger = worm.radius * 0.76f + width * 0.045f
                if (gameTime >= invulnUntil && dx * dx + dy * dy < danger * danger) {
                    val raw = if (worm.boss) 145 else 82 + currentLevelId * 4
                    val damage = max(20, (raw * armorMultiplier).toInt())
                    hp = max(0, hp - damage)
                    invulnUntil = gameTime + 0.70f
                    floatTexts += FloatText("-$damage", playerX, playerY - 58f, 0.82f, Color.rgb(248, 72, 88), 1.05f)
                    if (hp <= 0) gameOver = true
                }
            }
            if (worm.hp.all { it <= 0 }) {
                wormIt.remove()
                continue
            }
            if (segmentPosition(worm, 0).second > height * 0.84f) {
                val damage = max(35, ((if (worm.boss) 220 else 120 + currentLevelId * 8) * armorMultiplier).toInt())
                hp = max(0, hp - damage)
                floatTexts += FloatText("BREACH -$damage", playerX, playerY - 74f, 0.9f, Color.rgb(250, 86, 78), 0.92f)
                wormIt.remove()
                if (hp <= 0) gameOver = true
            }
        }
    }

    private fun updateDrops(dt: Float) {
        val dIt = drops.iterator()
        while (dIt.hasNext()) {
            val d = dIt.next()
            d.age += dt
            d.y += d.vy * dt
            var dx = d.x - playerX
            var dy = d.y - playerY
            val baseMagnet = width * 0.105f * (1f + stack("magnet") * 0.45f)
            val forcedChest = d.type == DROP_CHEST && d.age > 0.38f
            if (forcedChest || dx * dx + dy * dy < baseMagnet * baseMagnet) {
                val pull = if (d.type == DROP_CHEST) 5.5f else 7.5f
                d.x += (playerX - d.x) * min(1f, dt * pull)
                d.y += (playerY - d.y) * min(1f, dt * pull)
                dx = d.x - playerX
                dy = d.y - playerY
            }
            val collectRadius = width * if (d.type == DROP_CHEST) 0.068f else 0.052f
            if (dx * dx + dy * dy < collectRadius * collectRadius) {
                when (d.type) {
                    DROP_COIN -> {
                        coins++
                        floatTexts += FloatText("+1", playerX + 30f, playerY - 34f, 0.65f, Color.rgb(255, 210, 62), 0.75f)
                    }
                    DROP_GEM -> {
                        gems++
                        gainXp(1)
                    }
                    DROP_HEART -> {
                        hp = min(maxHp, hp + 170)
                        floatTexts += FloatText("+170", playerX, playerY - 48f, 0.78f, Color.rgb(86, 230, 126), 0.85f)
                    }
                    DROP_CHEST -> openChest()
                }
                dIt.remove()
            } else if (d.type != DROP_CHEST && d.y > height * 0.88f) {
                dIt.remove()
            } else if (d.type == DROP_CHEST && d.y > height * 0.70f) {
                d.y = height * 0.70f
                d.vy = 0f
            }
        }
    }

    private fun updateFloatText(dt: Float) {
        val it = floatTexts.iterator()
        while (it.hasNext()) {
            val f = it.next()
            f.life -= dt
            f.y -= height * 0.034f * dt
            if (f.life <= 0f) it.remove()
        }
    }

    private fun spawnWorm(plan: SpawnPlan) {
        val radius = width * (if (plan.boss) 0.060f else 0.044f)
        val hp = IntArray(plan.segments) { index ->
            val segmentBoost = if (index == 0) 1.10f else 1f
            (plan.hp * segmentBoost).toInt()
        }
        val treasure = BooleanArray(plan.segments)
        for (i in plan.segments.indices) {
            if (i > 0 && (i + plan.seed) % config.chestEvery == 0) treasure[i] = true
            if (stack("lucky") > 0 && i > 0 && rng.nextFloat() < stack("lucky") * 0.035f) treasure[i] = true
        }
        if (plan.boss) {
            treasure[max(1, plan.segments / 3)] = true
            treasure[max(2, plan.segments * 2 / 3)] = true
        }
        worms += Worm(
            id = nextWormId++,
            x = width * plan.xFrac,
            y = -radius * 1.8f,
            phase = rng.nextFloat() * 6.28f,
            speed = plan.speed,
            amplitude = width * plan.amplitudeFrac,
            spacing = radius * if (plan.boss) 1.60f else 1.68f,
            radius = radius,
            hp = hp,
            maxHp = hp.copyOf(),
            treasure = treasure,
            boss = plan.boss
        )
        if (plan.boss) {
            floatTexts += FloatText("ROYAL WORM!", width * 0.5f, height * 0.28f, 1.6f, Color.rgb(255, 224, 78), 1.25f)
        }
    }

    private fun segmentPosition(worm: Worm, index: Int): Pair<Float, Float> = Pair(
        worm.x + sin(worm.phase + index * if (worm.boss) 0.48f else 0.58f) * worm.amplitude,
        worm.y - index * worm.spacing
    )

    private fun segmentKey(wormId: Int, index: Int): Long =
        (wormId.toLong() shl 32) or (index.toLong() and 0xffffffffL)

    private fun damageSegment(worm: Worm, index: Int, damage: Int, x: Float, y: Float, critical: Boolean) {
        if (worm.hp[index] <= 0) return
        val actual = min(worm.hp[index], damage)
        worm.hp[index] -= damage
        floatTexts += FloatText(
            if (critical) "CRIT $actual" else actual.toString(),
            x + (rng.nextFloat() - 0.5f) * 26f,
            y - 8f,
            if (critical) 0.72f else 0.55f,
            if (critical) Color.rgb(255, 111, 70) else Color.WHITE,
            if (critical) 0.95f else 0.72f
        )
        if (worm.hp[index] <= 0) {
            worm.hp[index] = 0
            if (worm.treasure[index]) {
                drops += Drop(DROP_CHEST, x, y, height * 0.030f)
                floatTexts += FloatText("CHEST!", x, y - worm.radius, 0.95f, Color.rgb(255, 219, 72), 0.85f)
            } else {
                val roll = rng.nextFloat()
                val type = when {
                    roll < 0.075f -> DROP_HEART
                    roll < 0.20f -> DROP_GEM
                    roll < 0.72f -> DROP_COIN
                    else -> -1
                }
                if (type >= 0) drops += Drop(type, x, y, height * (0.050f + rng.nextFloat() * 0.025f))
            }
        }
    }

    private fun gainXp(amount: Int) {
        xp += amount
        var need = 3 + level
        while (xp >= need && level < 12) {
            xp -= need
            level++
            maxHp += 45
            hp = min(maxHp, hp + 130)
            floatTexts += FloatText("LEVEL UP!", playerX, playerY - 105f, 1.05f, Color.rgb(88, 226, 255), 1.0f)
            need = 3 + level
        }
    }

    private fun openChest() {
        if (upgradeChoices.isNotEmpty()) {
            queuedChests++
            return
        }
        val available = GameContent.upgrades.filter { stack(it.id) < it.maxStacks }
        if (available.isEmpty()) {
            hp = min(maxHp, hp + 250)
            coins += 8
            floatTexts += FloatText("MAXED! +8 COINS", playerX, playerY - 90f, 1f, Color.rgb(255, 220, 80), 0.8f)
            return
        }
        val shuffled = available.shuffled(rng)
        upgradeChoices = shuffled.take(min(3, shuffled.size))
    }

    private fun chooseUpgrade(index: Int) {
        val option = upgradeChoices.getOrNull(index) ?: return
        upgradeStacks[option.id] = stack(option.id) + 1
        when (option.id) {
            "health" -> {
                maxHp += 220
                hp = min(maxHp, hp + 260)
            }
            "bladeCount" -> floatTexts += FloatText("BLADE +1", playerX, playerY - 95f, 0.9f, config.accentColor, 0.85f)
            else -> Unit
        }
        upgradeChoices = emptyList()
        lastFrameNs = System.nanoTime()
        if (queuedChests > 0) {
            queuedChests--
            openChest()
        }
    }

    private fun stack(id: String): Int = upgradeStacks[id] ?: 0

    private fun completeLevel() {
        victory = true
        val bit = 1 shl (currentLevelId - 1)
        val firstClear = clearMask and bit == 0
        clearMask = clearMask or bit
        val reward = coins + if (firstClear) 25 + currentLevelId * 5 else 0
        bankCoins += reward
        val bestKey = "bestCoins_$currentLevelId"
        val oldBest = prefs.getInt(bestKey, 0)
        prefs.edit()
            .putInt("clearMask", clearMask)
            .putInt("bankCoins", bankCoins)
            .putInt(bestKey, max(oldBest, coins))
            .apply()
    }

    private fun densityScale(): Float = max(0.75f, width / 1080f)

    private fun drawOverworld(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        paint.shader = LinearGradient(0f, 0f, 0f, h, Color.rgb(73, 122, 103), Color.rgb(194, 158, 91), Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, w, h, paint)
        paint.shader = null

        paint.color = Color.rgb(68, 116, 74)
        canvas.drawRect(0f, 0f, w * 0.09f, h, paint)
        canvas.drawRect(w * 0.91f, 0f, w, h, paint)
        for (i in 0 until 18) {
            paint.color = if (i % 2 == 0) Color.rgb(94, 152, 76) else Color.rgb(78, 137, 70)
            val y = h * (0.07f + i * 0.055f)
            canvas.drawCircle(w * 0.055f, y, w * 0.055f, paint)
            canvas.drawCircle(w * 0.945f, y + h * 0.02f, w * 0.055f, paint)
        }

        drawOutlinedText(canvas, "WORMBLADE", w * 0.5f, h * 0.073f, w * 0.085f, Color.WHITE, Color.rgb(31, 40, 38), Paint.Align.CENTER)
        drawOutlinedText(canvas, "WORLD MAP • ALL LEVELS OPEN", w * 0.5f, h * 0.112f, w * 0.030f, Color.rgb(216, 245, 238), Color.rgb(40, 55, 48), Paint.Align.CENTER)

        paint.color = Color.argb(190, 36, 45, 56)
        canvas.drawRoundRect(RectF(w * 0.18f, h * 0.128f, w * 0.82f, h * 0.174f), w * 0.020f, w * 0.020f, paint)
        val clears = Integer.bitCount(clearMask and 0x3FF)
        drawMiniCoin(canvas, w * 0.245f, h * 0.151f, w * 0.018f)
        drawOutlinedText(canvas, bankCoins.toString(), w * 0.285f, h * 0.160f, w * 0.032f, Color.WHITE, Color.rgb(27, 31, 37), Paint.Align.LEFT)
        drawOutlinedText(canvas, "CLEARED $clears/10", w * 0.66f, h * 0.160f, w * 0.030f, Color.WHITE, Color.rgb(27, 31, 37), Paint.Align.CENTER)

        val positions = levelNodePositions(w, h)
        stroke.strokeWidth = w * 0.016f
        stroke.color = Color.argb(190, 242, 211, 119)
        for (i in 0 until positions.lastIndex) {
            canvas.drawLine(positions[i].first, positions[i].second, positions[i + 1].first, positions[i + 1].second, stroke)
        }
        stroke.strokeWidth = w * 0.006f
        stroke.color = Color.argb(190, 89, 71, 48)
        for (i in 0 until positions.lastIndex) {
            canvas.drawLine(positions[i].first, positions[i].second, positions[i + 1].first, positions[i + 1].second, stroke)
        }

        for (i in positions.indices) {
            val levelConfig = GameContent.levels[i]
            drawLevelNode(canvas, positions[i].first, positions[i].second, levelConfig)
        }

        drawOutlinedText(canvas, "Tap a level to deploy", w * 0.5f, h * 0.973f, w * 0.031f, Color.rgb(245, 239, 219), Color.rgb(51, 48, 42), Paint.Align.CENTER)
    }

    private fun levelNodePositions(w: Float, h: Float): List<Pair<Float, Float>> = listOf(
        Pair(w * 0.27f, h * 0.225f),
        Pair(w * 0.70f, h * 0.285f),
        Pair(w * 0.31f, h * 0.355f),
        Pair(w * 0.69f, h * 0.430f),
        Pair(w * 0.28f, h * 0.505f),
        Pair(w * 0.70f, h * 0.582f),
        Pair(w * 0.31f, h * 0.660f),
        Pair(w * 0.69f, h * 0.738f),
        Pair(w * 0.31f, h * 0.817f),
        Pair(w * 0.67f, h * 0.895f)
    )

    private fun drawLevelNode(canvas: Canvas, cx: Float, cy: Float, levelConfig: LevelConfig) {
        val w = width.toFloat()
        val cleared = clearMask and (1 shl (levelConfig.id - 1)) != 0
        val r = w * if (levelConfig.boss) 0.075f else 0.062f
        paint.color = Color.argb(70, 30, 30, 30)
        canvas.drawOval(RectF(cx - r, cy + r * 0.58f, cx + r, cy + r * 1.04f), paint)
        paint.shader = LinearGradient(cx - r, cy - r, cx + r, cy + r, levelConfig.wormLight, levelConfig.wormDark, Shader.TileMode.CLAMP)
        canvas.drawCircle(cx, cy, r, paint)
        paint.shader = null
        stroke.color = if (cleared) Color.rgb(103, 238, 134) else if (levelConfig.boss) Color.rgb(255, 226, 77) else Color.rgb(69, 65, 54)
        stroke.strokeWidth = w * if (cleared) 0.012f else 0.008f
        canvas.drawCircle(cx, cy, r, stroke)
        if (levelConfig.boss) drawCrown(canvas, cx, cy - r * 0.78f, r * 0.70f)
        drawOutlinedText(canvas, levelConfig.id.toString(), cx, cy + r * 0.18f, r * 0.72f, Color.WHITE, Color.rgb(73, 53, 31), Paint.Align.CENTER)
        val nameY = cy + r * 1.42f
        paint.color = Color.argb(205, 42, 48, 51)
        canvas.drawRoundRect(RectF(cx - w * 0.115f, nameY - w * 0.031f, cx + w * 0.115f, nameY + w * 0.016f), w * 0.014f, w * 0.014f, paint)
        drawOutlinedText(canvas, levelConfig.name, cx, nameY, w * 0.024f, Color.WHITE, Color.rgb(26, 28, 29), Paint.Align.CENTER)
        if (cleared) {
            drawOutlinedText(canvas, "✓", cx + r * 0.70f, cy - r * 0.60f, r * 0.52f, Color.rgb(110, 255, 145), Color.rgb(34, 70, 45), Paint.Align.CENTER)
        }
    }

    private fun drawBackground(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        paint.shader = LinearGradient(0f, 0f, 0f, h, config.topColor, config.bottomColor, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, w, h, paint)
        paint.shader = null
        paint.color = config.edgeColor
        canvas.drawRect(0f, 0f, w * 0.095f, h, paint)
        canvas.drawRect(w * 0.905f, 0f, w, h, paint)
        for (i in 0..12) {
            val y = h * (i / 12f)
            paint.color = shiftColor(config.edgeColor, if (i % 2 == 0) 18 else -10)
            canvas.drawCircle(w * 0.065f, y, w * 0.056f, paint)
            canvas.drawCircle(w * 0.935f, y + h * 0.025f, w * 0.056f, paint)
        }
        paint.color = Color.argb(32, 60, 45, 30)
        for (i in 0 until 22) {
            val x = w * (0.14f + ((i * 37 + currentLevelId * 11) % 70) / 100f)
            val y = h * (0.10f + ((i * 61 + currentLevelId * 7) % 70) / 100f)
            canvas.drawOval(RectF(x, y, x + w * 0.025f, y + h * 0.008f), paint)
        }
        for (i in 0 until 5) {
            val left = i % 2 == 0
            drawRuneStone(canvas, if (left) w * 0.045f else w * 0.955f, h * (0.18f + i * 0.16f), !left)
        }
        for (i in 0..5) {
            drawFlower(canvas, if (i % 2 == 0) w * 0.055f else w * 0.945f, h * (0.12f + i * 0.14f), w * 0.011f)
        }
        if (config.id >= 7) drawAmbientSparkles(canvas)
    }

    private fun drawAmbientSparkles(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        paint.color = Color.argb(90, 220, 240, 255)
        for (i in 0 until 11) {
            val x = w * (0.14f + ((i * 17 + currentLevelId * 13) % 72) / 100f)
            val y = h * (0.17f + ((i * 29 + currentLevelId * 19) % 60) / 100f)
            val r = w * (0.0025f + (i % 3) * 0.0018f)
            canvas.drawCircle(x, y, r, paint)
        }
    }

    private fun drawRuneStone(canvas: Canvas, cx: Float, cy: Float, right: Boolean) {
        val w = width.toFloat()
        paint.color = Color.rgb(76, 82, 75)
        canvas.drawRoundRect(RectF(cx - w * 0.052f, cy - w * 0.060f, cx + w * 0.052f, cy + w * 0.060f), w * 0.020f, w * 0.020f, paint)
        val runeX = cx + if (right) -w * 0.010f else w * 0.010f
        paint.color = Color.rgb(43, 50, 47)
        canvas.drawCircle(runeX, cy, w * 0.022f, paint)
        stroke.color = config.accentColor
        stroke.strokeWidth = w * 0.007f
        canvas.drawCircle(runeX, cy, w * 0.016f, stroke)
        canvas.drawLine(runeX - w * 0.009f, cy, runeX + w * 0.009f, cy, stroke)
    }

    private fun drawFlower(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        paint.color = shiftColor(config.accentColor, 20)
        for (i in 0 until 5) {
            val a = i * 6.283f / 5f
            canvas.drawCircle(cx + cos(a) * r, cy + sin(a) * r, r * 0.55f, paint)
        }
        paint.color = Color.rgb(255, 224, 85)
        canvas.drawCircle(cx, cy, r * 0.42f, paint)
    }

    private fun drawWorld(canvas: Canvas) {
        for (worm in worms) drawWorm(canvas, worm)
        for (drop in drops) drawDrop(canvas, drop)
        for (p in projectiles) drawProjectile(canvas, p)
        drawPlayer(canvas)
        for (f in floatTexts) {
            val alpha = (255 * min(1f, f.life * 1.9f)).toInt().coerceIn(0, 255)
            val fill = Color.argb(alpha, Color.red(f.color), Color.green(f.color), Color.blue(f.color))
            val outline = Color.argb(alpha, 36, 31, 28)
            drawOutlinedText(canvas, f.text, f.x, f.y, width * 0.038f * f.scale, fill, outline, Paint.Align.CENTER)
        }
    }

    private fun drawWorm(canvas: Canvas, worm: Worm) {
        for (i in worm.hp.indices.reversed()) {
            if (worm.hp[i] <= 0) continue
            val (x, y) = segmentPosition(worm, i)
            if (y < -worm.radius * 2.2f || y > height + worm.radius * 2.2f) continue
            val r = worm.radius
            paint.color = Color.argb(52, 70, 46, 22)
            canvas.drawOval(RectF(x - r * 0.82f, y + r * 0.52f, x + r * 0.82f, y + r * 1.02f), paint)

            val light = if (worm.boss) shiftColor(config.wormLight, 18) else config.wormLight
            val dark = if (worm.boss) shiftColor(config.wormDark, -10) else config.wormDark
            paint.shader = LinearGradient(x - r, y - r, x + r, y + r, light, dark, Shader.TileMode.CLAMP)
            canvas.drawCircle(x, y, r, paint)
            paint.shader = null

            stroke.color = if (worm.treasure[i]) Color.rgb(255, 235, 92) else if (worm.boss) Color.rgb(120, 69, 34) else Color.rgb(132, 83, 31)
            stroke.strokeWidth = r * if (worm.treasure[i]) 0.16f else 0.09f
            canvas.drawCircle(x, y, r, stroke)

            paint.color = Color.argb(185, 255, 255, 226)
            canvas.drawCircle(x - r * 0.30f, y - r * 0.34f, r * 0.16f, paint)
            paint.color = dark
            canvas.drawCircle(x - r * 0.57f, y - r * 0.68f, r * 0.15f, paint)
            canvas.drawCircle(x + r * 0.57f, y - r * 0.68f, r * 0.15f, paint)

            if (i == 0) {
                paint.color = Color.rgb(45, 42, 36)
                canvas.drawCircle(x - r * 0.34f, y - r * 0.02f, r * 0.10f, paint)
                canvas.drawCircle(x + r * 0.34f, y - r * 0.02f, r * 0.10f, paint)
                paint.color = Color.WHITE
                canvas.drawCircle(x - r * 0.37f, y - r * 0.055f, r * 0.032f, paint)
                canvas.drawCircle(x + r * 0.31f, y - r * 0.055f, r * 0.032f, paint)
                if (worm.boss) drawCrown(canvas, x, y - r * 0.88f, r * 0.62f)
            }

            val hpText = compactNumber(worm.hp[i])
            drawOutlinedText(
                canvas,
                hpText,
                x,
                y + r * 0.38f,
                r * if (hpText.length >= 4) 0.38f else 0.46f,
                Color.WHITE,
                Color.rgb(64, 49, 35),
                Paint.Align.CENTER
            )
            if (worm.treasure[i]) {
                drawTinyChest(canvas, x + r * 0.54f, y + r * 0.46f, r * 0.37f)
            }

            val frac = worm.hp[i] / worm.maxHp[i].toFloat()
            if (frac < 0.36f) {
                stroke.color = Color.argb(160, 255, 112, 75)
                stroke.strokeWidth = r * 0.06f
                canvas.drawArc(RectF(x - r * 0.76f, y - r * 0.76f, x + r * 0.76f, y + r * 0.76f), -90f, 360f * frac, false, stroke)
            }
        }
    }

    private fun drawProjectile(canvas: Canvas, p: Projectile) {
        val rw = width * if (p.critical) 0.016f else 0.012f
        val rh = width * if (p.critical) 0.053f else 0.043f
        paint.color = if (p.critical) Color.argb(90, 255, 167, 58) else Color.argb(70, 68, 220, 255)
        canvas.drawCircle(p.x, p.y, rw * 2.5f, paint)
        path.reset()
        path.moveTo(p.x, p.y - rh)
        path.lineTo(p.x + rw, p.y)
        path.lineTo(p.x, p.y + rh * 0.55f)
        path.lineTo(p.x - rw, p.y)
        path.close()
        paint.color = if (p.critical) Color.rgb(255, 203, 64) else Color.rgb(73, 216, 255)
        canvas.drawPath(path, paint)
        stroke.color = Color.WHITE
        stroke.strokeWidth = max(2f, width * 0.003f)
        canvas.drawPath(path, stroke)
    }

    private fun drawPlayer(canvas: Canvas) {
        val w = width.toFloat()
        val r = w * 0.058f
        paint.color = Color.argb(55, 28, 32, 29)
        canvas.drawOval(RectF(playerX - r, playerY + r * 0.70f, playerX + r, playerY + r * 1.14f), paint)

        val bladeCount = 2 + stack("bladeCount")
        val bladeReach = w * 0.165f * (1f + stack("bladeReach") * 0.18f)
        val bladeScale = 1f + stack("bladeReach") * 0.10f
        val angle = gameTime * 4.15f
        stroke.color = Color.argb(125, 118, 231, 255)
        stroke.strokeWidth = w * 0.006f
        canvas.drawOval(RectF(playerX - bladeReach, playerY - bladeReach * 0.55f, playerX + bladeReach, playerY + bladeReach * 0.55f), stroke)
        for (b in 0 until bladeCount) {
            val a = angle + 2f * PI.toFloat() * b / bladeCount
            drawOrbitBlade(
                canvas,
                playerX + cos(a) * bladeReach,
                playerY + sin(a) * bladeReach * 0.55f,
                a * 57.2958f + 25f,
                bladeScale
            )
        }

        paint.color = Color.rgb(34, 91, 130)
        canvas.drawRoundRect(RectF(playerX - r * 0.62f, playerY + r * 0.42f, playerX - r * 0.16f, playerY + r * 0.95f), r * 0.17f, r * 0.17f, paint)
        canvas.drawRoundRect(RectF(playerX + r * 0.16f, playerY + r * 0.42f, playerX + r * 0.62f, playerY + r * 0.95f), r * 0.17f, r * 0.17f, paint)
        paint.shader = LinearGradient(playerX - r, playerY - r, playerX + r, playerY + r, Color.rgb(76, 198, 250), Color.rgb(28, 103, 175), Shader.TileMode.CLAMP)
        canvas.drawRoundRect(RectF(playerX - r * 0.78f, playerY - r * 0.18f, playerX + r * 0.78f, playerY + r * 0.70f), r * 0.32f, r * 0.32f, paint)
        paint.shader = null
        stroke.color = Color.rgb(16, 60, 96)
        stroke.strokeWidth = r * 0.09f
        canvas.drawRoundRect(RectF(playerX - r * 0.78f, playerY - r * 0.18f, playerX + r * 0.78f, playerY + r * 0.70f), r * 0.32f, r * 0.32f, stroke)

        paint.shader = LinearGradient(playerX, playerY - r * 1.15f, playerX, playerY + r * 0.08f, Color.rgb(198, 242, 255), Color.rgb(42, 142, 208), Shader.TileMode.CLAMP)
        canvas.drawCircle(playerX, playerY - r * 0.40f, r * 0.75f, paint)
        paint.shader = null
        stroke.color = Color.rgb(14, 65, 104)
        stroke.strokeWidth = r * 0.09f
        canvas.drawCircle(playerX, playerY - r * 0.40f, r * 0.75f, stroke)

        paint.color = Color.rgb(24, 35, 58)
        canvas.drawRoundRect(RectF(playerX - r * 0.54f, playerY - r * 0.62f, playerX + r * 0.54f, playerY - r * 0.20f), r * 0.20f, r * 0.20f, paint)
        paint.color = Color.rgb(139, 231, 255)
        canvas.drawCircle(playerX - r * 0.22f, playerY - r * 0.40f, r * 0.055f, paint)
        canvas.drawCircle(playerX + r * 0.22f, playerY - r * 0.40f, r * 0.055f, paint)
        paint.color = Color.rgb(25, 75, 111)
        canvas.drawRoundRect(RectF(playerX - r * 0.24f, playerY + r * 0.10f, playerX + r * 0.24f, playerY + r * 0.46f), r * 0.08f, r * 0.08f, paint)
        paint.color = config.accentColor
        canvas.drawCircle(playerX, playerY + r * 0.28f, r * 0.075f, paint)

        if (gameTime < invulnUntil && (gameTime * 12f).toInt() % 2 == 0) {
            stroke.color = Color.argb(210, 255, 255, 255)
            stroke.strokeWidth = w * 0.006f
            canvas.drawCircle(playerX, playerY - r * 0.10f, r * 1.15f, stroke)
        }
    }

    private fun drawOrbitBlade(canvas: Canvas, x: Float, y: Float, rotation: Float, scale: Float) {
        val s = width * 0.052f * scale
        canvas.save()
        canvas.translate(x, y)
        canvas.rotate(rotation)
        paint.color = Color.rgb(18, 77, 116)
        canvas.drawCircle(0f, 0f, s * 0.28f, paint)
        stroke.color = Color.rgb(9, 46, 72)
        stroke.strokeWidth = s * 0.12f
        canvas.drawCircle(0f, 0f, s * 0.28f, stroke)
        path.reset()
        path.moveTo(-s * 0.10f, 0f)
        path.quadTo(-s * 0.55f, -s * 0.52f, -s * 1.05f, -s * 0.12f)
        path.quadTo(-s * 0.60f, -s * 0.02f, -s * 0.28f, s * 0.17f)
        path.close()
        paint.shader = LinearGradient(-s, -s * 0.5f, 0f, s * 0.3f, Color.rgb(111, 229, 255), Color.rgb(22, 117, 195), Shader.TileMode.CLAMP)
        canvas.drawPath(path, paint)
        paint.shader = null
        stroke.color = Color.rgb(12, 60, 97)
        stroke.strokeWidth = s * 0.10f
        canvas.drawPath(path, stroke)
        path.reset()
        path.moveTo(s * 0.10f, 0f)
        path.quadTo(s * 0.55f, s * 0.52f, s * 1.05f, s * 0.12f)
        path.quadTo(s * 0.60f, s * 0.02f, s * 0.28f, -s * 0.17f)
        path.close()
        paint.shader = LinearGradient(0f, -s * 0.3f, s, s * 0.5f, Color.rgb(111, 229, 255), Color.rgb(22, 117, 195), Shader.TileMode.CLAMP)
        canvas.drawPath(path, paint)
        paint.shader = null
        canvas.drawPath(path, stroke)
        canvas.restore()
    }

    private fun drawDrop(canvas: Canvas, d: Drop) {
        val r = width * if (d.type == DROP_CHEST) 0.036f else 0.025f
        when (d.type) {
            DROP_COIN -> drawMiniCoin(canvas, d.x, d.y, r)
            DROP_GEM -> drawMiniGem(canvas, d.x, d.y, r)
            DROP_HEART -> drawHeart(canvas, d.x, d.y, r * 1.14f, Color.rgb(246, 72, 94))
            DROP_CHEST -> {
                val pulse = 1f + sin(gameTime * 8f) * 0.08f
                drawTinyChest(canvas, d.x, d.y, r * 1.65f * pulse)
                stroke.color = Color.argb(140, 255, 233, 102)
                stroke.strokeWidth = width * 0.006f
                canvas.drawCircle(d.x, d.y, r * 1.65f, stroke)
            }
        }
    }

    private fun drawTinyChest(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        paint.color = Color.rgb(116, 65, 32)
        canvas.drawRoundRect(RectF(cx - s, cy - s * 0.42f, cx + s, cy + s * 0.63f), s * 0.16f, s * 0.16f, paint)
        paint.color = Color.rgb(219, 148, 43)
        canvas.drawRoundRect(RectF(cx - s * 0.92f, cy - s * 0.36f, cx + s * 0.92f, cy + s * 0.55f), s * 0.12f, s * 0.12f, paint)
        paint.color = Color.rgb(255, 218, 74)
        canvas.drawRect(cx - s * 0.12f, cy - s * 0.35f, cx + s * 0.12f, cy + s * 0.55f, paint)
        canvas.drawRoundRect(RectF(cx - s * 0.22f, cy + s * 0.03f, cx + s * 0.22f, cy + s * 0.32f), s * 0.07f, s * 0.07f, paint)
        stroke.color = Color.rgb(84, 47, 27)
        stroke.strokeWidth = max(1.5f, s * 0.10f)
        canvas.drawLine(cx - s * 0.86f, cy - s * 0.02f, cx + s * 0.86f, cy - s * 0.02f, stroke)
    }

    private fun drawHud(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        paint.color = Color.rgb(62, 68, 89)
        canvas.drawRoundRect(RectF(w * 0.035f, h * 0.018f, w * 0.145f, h * 0.082f), w * 0.018f, w * 0.018f, paint)
        stroke.color = Color.rgb(27, 30, 43)
        stroke.strokeWidth = w * 0.006f
        canvas.drawRoundRect(RectF(w * 0.035f, h * 0.018f, w * 0.145f, h * 0.082f), w * 0.018f, w * 0.018f, stroke)
        paint.color = Color.WHITE
        canvas.drawRoundRect(RectF(w * 0.070f, h * 0.031f, w * 0.085f, h * 0.069f), 6f, 6f, paint)
        canvas.drawRoundRect(RectF(w * 0.098f, h * 0.031f, w * 0.113f, h * 0.069f), 6f, 6f, paint)

        drawOutlinedText(canvas, "LEVEL $currentLevelId", w * 0.50f, h * 0.054f, w * 0.063f, Color.WHITE, Color.rgb(37, 31, 29), Paint.Align.CENTER)
        drawOutlinedText(canvas, config.name, w * 0.50f, h * 0.081f, w * 0.025f, Color.rgb(247, 239, 214), Color.rgb(58, 49, 39), Paint.Align.CENTER)

        paint.color = Color.rgb(47, 48, 69)
        canvas.drawRoundRect(RectF(w * 0.76f, h * 0.020f, w * 0.96f, h * 0.078f), w * 0.026f, w * 0.026f, paint)
        drawMiniCoin(canvas, w * 0.80f, h * 0.049f, w * 0.026f)
        drawOutlinedText(canvas, coins.toString(), w * 0.90f, h * 0.061f, w * 0.050f, Color.WHITE, Color.rgb(35, 31, 35), Paint.Align.CENTER)

        val barLeft = w * 0.27f
        val barRight = w * 0.73f
        val barY = h * 0.116f
        stroke.color = Color.rgb(55, 55, 62)
        stroke.strokeWidth = w * 0.011f
        canvas.drawLine(barLeft, barY, barRight, barY, stroke)
        val points = max(2, config.waves)
        for (i in 0 until points) {
            val x = if (points == 1) (barLeft + barRight) / 2f else barLeft + (barRight - barLeft) * i / (points - 1f)
            paint.color = if (i < currentWave) config.accentColor else Color.rgb(118, 118, 126)
            canvas.drawCircle(x, barY, w * 0.015f, paint)
            stroke.color = Color.rgb(49, 49, 55)
            stroke.strokeWidth = w * 0.004f
            canvas.drawCircle(x, barY, w * 0.015f, stroke)
        }
        drawFlag(canvas, barRight + w * 0.025f, barY)
        drawOutlinedText(canvas, "WAVE $currentWave/${config.waves}", w * 0.50f, h * 0.149f, w * 0.040f, Color.WHITE, Color.rgb(55, 44, 34), Paint.Align.CENTER)

        val hpTop = h * 0.895f
        drawOutlinedText(canvas, "HP", w * 0.045f, hpTop, w * 0.044f, Color.WHITE, Color.rgb(42, 37, 34), Paint.Align.LEFT)
        val heartY = h * 0.874f
        val hpFrac = hp / maxHp.toFloat()
        for (i in 0 until 5) {
            drawHeart(canvas, w * (0.18f + i * 0.063f), heartY, w * 0.024f,
                if (hpFrac > i / 5f) Color.rgb(243, 63, 78) else Color.rgb(104, 85, 80))
        }
        val bar = RectF(w * 0.045f, h * 0.915f, w * 0.43f, h * 0.955f)
        paint.color = Color.rgb(46, 48, 50)
        canvas.drawRoundRect(bar, w * 0.018f, w * 0.018f, paint)
        val inner = RectF(bar.left + w * 0.008f, bar.top + w * 0.008f, bar.right - w * 0.008f, bar.bottom - w * 0.008f)
        paint.color = Color.rgb(94, 116, 55)
        canvas.drawRoundRect(inner, w * 0.012f, w * 0.012f, paint)
        paint.shader = LinearGradient(inner.left, inner.top, inner.left, inner.bottom, Color.rgb(116, 231, 47), Color.rgb(66, 180, 36), Shader.TileMode.CLAMP)
        canvas.drawRoundRect(RectF(inner.left, inner.top, inner.left + inner.width() * hpFrac.coerceIn(0f, 1f), inner.bottom), w * 0.012f, w * 0.012f, paint)
        paint.shader = null
        drawOutlinedText(canvas, "$hp/$maxHp", bar.centerX(), h * 0.947f, w * 0.035f, Color.WHITE, Color.rgb(35, 39, 33), Paint.Align.CENTER)

        val shots = 1 + stack("multishot") * 2
        val blades = 2 + stack("bladeCount")
        val dmgPercent = (100 + stack("damage") * 40)
        drawStatSlot(canvas, w * 0.57f, h * 0.918f, "SHOT", "x$shots", 0)
        drawStatSlot(canvas, w * 0.73f, h * 0.918f, "BLADE", "x$blades", 1)
        drawStatSlot(canvas, w * 0.89f, h * 0.918f, "DMG", "$dmgPercent%", 2)

        drawMiniGem(canvas, w * 0.52f, h * 0.853f, w * 0.015f)
        drawOutlinedText(canvas, "$gems  XP $xp/${3 + level}", w * 0.555f, h * 0.862f, w * 0.024f, Color.WHITE, Color.rgb(50, 45, 40), Paint.Align.LEFT)
        if (upgradeStacks.isNotEmpty()) {
            drawOutlinedText(canvas, "UPGRADES ${upgradeStacks.values.sum()}", w * 0.84f, h * 0.862f, w * 0.022f, Color.rgb(255, 236, 139), Color.rgb(58, 48, 36), Paint.Align.CENTER)
        }
    }

    private fun drawStatSlot(canvas: Canvas, cx: Float, cy: Float, label: String, value: String, icon: Int) {
        val s = width * 0.112f
        paint.color = Color.rgb(38, 91, 139)
        canvas.drawRoundRect(RectF(cx - s * 0.52f, cy - s * 0.60f, cx + s * 0.52f, cy + s * 0.60f), s * 0.13f, s * 0.13f, paint)
        stroke.color = Color.rgb(24, 31, 45)
        stroke.strokeWidth = s * 0.07f
        canvas.drawRoundRect(RectF(cx - s * 0.52f, cy - s * 0.60f, cx + s * 0.52f, cy + s * 0.60f), s * 0.13f, s * 0.13f, stroke)
        when (icon) {
            0 -> {
                path.reset()
                path.moveTo(cx, cy - s * 0.36f)
                path.lineTo(cx + s * 0.10f, cy - s * 0.05f)
                path.lineTo(cx, cy + s * 0.08f)
                path.lineTo(cx - s * 0.10f, cy - s * 0.05f)
                path.close()
                paint.color = Color.rgb(95, 225, 255)
                canvas.drawPath(path, paint)
            }
            1 -> drawOrbitBlade(canvas, cx, cy - s * 0.16f, -20f, 0.48f)
            else -> drawOutlinedText(canvas, "⚡", cx, cy - s * 0.02f, s * 0.34f, Color.rgb(255, 222, 75), Color.rgb(46, 46, 48), Paint.Align.CENTER)
        }
        drawOutlinedText(canvas, label, cx, cy + s * 0.23f, s * 0.16f, Color.rgb(202, 236, 248), Color.rgb(27, 36, 49), Paint.Align.CENTER)
        drawOutlinedText(canvas, value, cx, cy + s * 0.49f, s * 0.23f, Color.WHITE, Color.rgb(27, 36, 49), Paint.Align.CENTER)
    }

    private fun drawStartBanner(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val alpha = min(210, (startBanner * 85f).toInt()).coerceAtLeast(0)
        paint.color = Color.argb(alpha, 34, 42, 55)
        canvas.drawRoundRect(RectF(w * 0.18f, h * 0.73f, w * 0.82f, h * 0.79f), w * 0.025f, w * 0.025f, paint)
        drawOutlinedText(canvas, "DRAG TO MOVE • AUTO FIRE", w * 0.5f, h * 0.768f, w * 0.030f, Color.WHITE, Color.rgb(30, 34, 40), Paint.Align.CENTER)
    }

    private fun drawPauseOverlay(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        paint.color = Color.argb(185, 20, 22, 30)
        canvas.drawRect(0f, 0f, w, h, paint)
        drawOutlinedText(canvas, "PAUSED", w * 0.5f, h * 0.32f, w * 0.085f, Color.WHITE, Color.rgb(18, 22, 30), Paint.Align.CENTER)
        drawOverlayButton(canvas, 0, "RESUME", config.accentColor)
        drawOverlayButton(canvas, 1, "RESTART", Color.rgb(255, 193, 75))
        drawOverlayButton(canvas, 2, "WORLD MAP", Color.rgb(146, 204, 162))
    }

    private fun drawEndOverlay(canvas: Canvas, clear: Boolean) {
        val w = width.toFloat()
        val h = height.toFloat()
        paint.color = Color.argb(192, 20, 22, 30)
        canvas.drawRect(0f, 0f, w, h, paint)
        val title = if (clear) "LEVEL CLEAR!" else "GAME OVER"
        val color = if (clear) Color.rgb(255, 225, 84) else Color.rgb(255, 104, 95)
        drawOutlinedText(canvas, title, w * 0.5f, h * 0.285f, w * 0.082f, color, Color.rgb(21, 24, 31), Paint.Align.CENTER)
        if (clear) {
            drawOutlinedText(canvas, "${config.name} • $coins coins", w * 0.5f, h * 0.335f, w * 0.033f, Color.WHITE, Color.rgb(21, 24, 31), Paint.Align.CENTER)
            val firstClear = clearMask and (1 shl (currentLevelId - 1)) != 0
            if (firstClear) drawOutlinedText(canvas, "✓ Clear saved", w * 0.5f, h * 0.372f, w * 0.028f, Color.rgb(118, 245, 145), Color.rgb(21, 24, 31), Paint.Align.CENTER)
            drawOverlayButton(canvas, 0, if (currentLevelId < 10) "NEXT LEVEL" else "REPLAY", config.accentColor)
            drawOverlayButton(canvas, 1, "REPLAY", Color.rgb(255, 193, 75))
            drawOverlayButton(canvas, 2, "WORLD MAP", Color.rgb(146, 204, 162))
        } else {
            drawOutlinedText(canvas, "Wave $currentWave/${config.waves}", w * 0.5f, h * 0.345f, w * 0.033f, Color.WHITE, Color.rgb(21, 24, 31), Paint.Align.CENTER)
            drawOverlayButton(canvas, 0, "RETRY", config.accentColor)
            drawOverlayButton(canvas, 1, "WORLD MAP", Color.rgb(146, 204, 162))
        }
    }

    private fun drawOverlayButton(canvas: Canvas, row: Int, label: String, color: Int) {
        val w = width.toFloat()
        val h = height.toFloat()
        val top = h * (0.43f + row * 0.105f)
        val rect = RectF(w * 0.22f, top, w * 0.78f, top + h * 0.072f)
        paint.color = Color.argb(235, Color.red(color), Color.green(color), Color.blue(color))
        canvas.drawRoundRect(rect, w * 0.028f, w * 0.028f, paint)
        stroke.color = Color.rgb(30, 34, 41)
        stroke.strokeWidth = w * 0.007f
        canvas.drawRoundRect(rect, w * 0.028f, w * 0.028f, stroke)
        drawOutlinedText(canvas, label, rect.centerX(), rect.centerY() + w * 0.016f, w * 0.041f, Color.WHITE, Color.rgb(38, 39, 42), Paint.Align.CENTER)
    }

    private fun drawUpgradeOverlay(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        paint.color = Color.argb(208, 19, 22, 31)
        canvas.drawRect(0f, 0f, w, h, paint)
        drawTinyChest(canvas, w * 0.5f, h * 0.125f, w * 0.065f)
        drawOutlinedText(canvas, "TREASURE!", w * 0.5f, h * 0.205f, w * 0.078f, Color.rgb(255, 225, 82), Color.rgb(39, 31, 24), Paint.Align.CENTER)
        drawOutlinedText(canvas, "Choose one upgrade", w * 0.5f, h * 0.245f, w * 0.032f, Color.WHITE, Color.rgb(30, 31, 35), Paint.Align.CENTER)

        for (i in upgradeChoices.indices) {
            val option = upgradeChoices[i]
            val rect = upgradeCardRect(i, w, h)
            paint.color = Color.rgb(48, 61, 78)
            canvas.drawRoundRect(rect, w * 0.028f, w * 0.028f, paint)
            stroke.color = when (i) {
                0 -> Color.rgb(84, 221, 255)
                1 -> Color.rgb(255, 203, 78)
                else -> Color.rgb(197, 125, 255)
            }
            stroke.strokeWidth = w * 0.008f
            canvas.drawRoundRect(rect, w * 0.028f, w * 0.028f, stroke)
            val iconX = rect.left + w * 0.085f
            val iconY = rect.centerY()
            paint.color = stroke.color
            canvas.drawCircle(iconX, iconY, w * 0.043f, paint)
            drawOutlinedText(canvas, upgradeIcon(option.id), iconX, iconY + w * 0.016f, w * 0.043f, Color.WHITE, Color.rgb(44, 44, 48), Paint.Align.CENTER)
            drawOutlinedText(canvas, option.title, rect.left + w * 0.155f, rect.top + h * 0.044f, w * 0.038f, Color.WHITE, Color.rgb(27, 31, 38), Paint.Align.LEFT)
            drawOutlinedText(canvas, option.description, rect.left + w * 0.155f, rect.top + h * 0.078f, w * 0.025f, Color.rgb(207, 229, 240), Color.rgb(27, 31, 38), Paint.Align.LEFT)
            val current = stack(option.id)
            drawOutlinedText(canvas, "Lv ${current + 1}/${option.maxStacks}", rect.right - w * 0.045f, rect.top + h * 0.044f, w * 0.024f, Color.rgb(255, 232, 131), Color.rgb(31, 33, 37), Paint.Align.RIGHT)
        }
        if (queuedChests > 0) {
            drawOutlinedText(canvas, "+$queuedChests chest queued", w * 0.5f, h * 0.89f, w * 0.026f, Color.rgb(255, 226, 112), Color.rgb(32, 33, 38), Paint.Align.CENTER)
        }
    }

    private fun upgradeCardRect(index: Int, w: Float, h: Float): RectF {
        val top = h * (0.295f + index * 0.175f)
        return RectF(w * 0.10f, top, w * 0.90f, top + h * 0.135f)
    }

    private fun upgradeIcon(id: String): String = when (id) {
        "multishot" -> "✦"
        "damage" -> "↑"
        "firerate" -> "»"
        "pierce" -> "➤"
        "crit" -> "!"
        "bladeCount" -> "+"
        "bladeDamage" -> "⚔"
        "bladeReach" -> "◎"
        "armor" -> "◆"
        "health" -> "♥"
        "magnet" -> "U"
        "slow" -> "❄"
        "overdrive" -> "⚡"
        else -> "★"
    }

    private fun drawHeart(canvas: Canvas, cx: Float, cy: Float, r: Float, color: Int) {
        path.reset()
        path.moveTo(cx, cy + r * 0.92f)
        path.cubicTo(cx - r * 1.35f, cy + r * 0.05f, cx - r * 1.05f, cy - r * 0.92f, cx - r * 0.38f, cy - r * 0.78f)
        path.cubicTo(cx - r * 0.08f, cy - r * 0.72f, cx, cy - r * 0.45f, cx, cy - r * 0.32f)
        path.cubicTo(cx, cy - r * 0.45f, cx + r * 0.08f, cy - r * 0.72f, cx + r * 0.38f, cy - r * 0.78f)
        path.cubicTo(cx + r * 1.05f, cy - r * 0.92f, cx + r * 1.35f, cy + r * 0.05f, cx, cy + r * 0.92f)
        path.close()
        paint.color = color
        canvas.drawPath(path, paint)
        stroke.color = Color.rgb(104, 39, 43)
        stroke.strokeWidth = max(2f, r * 0.13f)
        canvas.drawPath(path, stroke)
        paint.color = Color.argb(150, 255, 255, 255)
        canvas.drawCircle(cx - r * 0.33f, cy - r * 0.35f, r * 0.13f, paint)
    }

    private fun drawMiniCoin(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        paint.color = Color.rgb(255, 185, 30)
        canvas.drawCircle(cx, cy, r, paint)
        stroke.color = Color.rgb(185, 115, 20)
        stroke.strokeWidth = max(1.5f, r * 0.18f)
        canvas.drawCircle(cx, cy, r, stroke)
        drawOutlinedText(canvas, "★", cx, cy + r * 0.34f, r * 0.85f, Color.rgb(255, 237, 114), Color.rgb(208, 135, 19), Paint.Align.CENTER)
    }

    private fun drawMiniGem(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        path.reset()
        path.moveTo(cx, cy - r)
        path.lineTo(cx + r * 0.86f, cy - r * 0.15f)
        path.lineTo(cx + r * 0.45f, cy + r)
        path.lineTo(cx - r * 0.45f, cy + r)
        path.lineTo(cx - r * 0.86f, cy - r * 0.15f)
        path.close()
        paint.color = Color.rgb(49, 210, 255)
        canvas.drawPath(path, paint)
        stroke.color = Color.rgb(25, 91, 172)
        stroke.strokeWidth = max(1.5f, r * 0.12f)
        canvas.drawPath(path, stroke)
    }

    private fun drawFlag(canvas: Canvas, x: Float, y: Float) {
        stroke.color = Color.rgb(73, 67, 68)
        stroke.strokeWidth = width * 0.008f
        canvas.drawLine(x, y - width * 0.035f, x, y + width * 0.035f, stroke)
        path.reset()
        path.moveTo(x, y - width * 0.035f)
        path.lineTo(x + width * 0.050f, y - width * 0.020f)
        path.lineTo(x, y - width * 0.002f)
        path.close()
        paint.color = Color.rgb(239, 71, 101)
        canvas.drawPath(path, paint)
    }

    private fun drawCrown(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        path.reset()
        path.moveTo(cx - s, cy + s * 0.46f)
        path.lineTo(cx - s * 0.80f, cy - s * 0.35f)
        path.lineTo(cx - s * 0.28f, cy + s * 0.02f)
        path.lineTo(cx, cy - s * 0.62f)
        path.lineTo(cx + s * 0.28f, cy + s * 0.02f)
        path.lineTo(cx + s * 0.80f, cy - s * 0.35f)
        path.lineTo(cx + s, cy + s * 0.46f)
        path.close()
        paint.color = Color.rgb(255, 218, 54)
        canvas.drawPath(path, paint)
        stroke.color = Color.rgb(154, 103, 24)
        stroke.strokeWidth = max(1.5f, s * 0.10f)
        canvas.drawPath(path, stroke)
        paint.color = Color.rgb(255, 94, 90)
        canvas.drawCircle(cx, cy + s * 0.20f, s * 0.12f, paint)
    }

    private fun drawOutlinedText(canvas: Canvas, text: String, x: Float, y: Float, size: Float, fill: Int, outline: Int, align: Paint.Align) {
        textPaint.textSize = max(1f, size)
        textPaint.textAlign = align
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.style = Paint.Style.STROKE
        textPaint.strokeWidth = max(2f, size * 0.11f)
        textPaint.color = outline
        canvas.drawText(text, x, y, textPaint)
        textPaint.style = Paint.Style.FILL
        textPaint.color = fill
        canvas.drawText(text, x, y, textPaint)
    }

    private fun drawRenderError(canvas: Canvas) {
        paint.shader = null
        paint.color = Color.rgb(30, 31, 38)
        canvas.drawColor(Color.rgb(30, 31, 38))
        drawOutlinedText(canvas, "WORMBLADE", width * 0.5f, height * 0.40f, width * 0.07f, Color.WHITE, Color.BLACK, Paint.Align.CENTER)
        drawOutlinedText(canvas, "Renderer recovered", width * 0.5f, height * 0.47f, width * 0.035f, Color.rgb(255, 193, 93), Color.BLACK, Paint.Align.CENTER)
        drawOutlinedText(canvas, renderError ?: "Unknown error", width * 0.5f, height * 0.53f, width * 0.022f, Color.LTGRAY, Color.BLACK, Paint.Align.CENTER)
        drawOutlinedText(canvas, "Tap to return to map", width * 0.5f, height * 0.61f, width * 0.03f, Color.WHITE, Color.BLACK, Paint.Align.CENTER)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        val w = width.toFloat()
        val h = height.toFloat()

        if (renderError != null) {
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                renderError = null
                returnToMap()
            }
            return true
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (screen == Screen.OVERWORLD) {
                    val positions = levelNodePositions(w, h)
                    for (i in positions.indices) {
                        val dx = x - positions[i].first
                        val dy = y - positions[i].second
                        val hit = w * 0.095f
                        if (dx * dx + dy * dy <= hit * hit) {
                            startLevel(i + 1)
                            return true
                        }
                    }
                    return true
                }

                if (upgradeChoices.isNotEmpty()) {
                    for (i in upgradeChoices.indices) {
                        if (upgradeCardRect(i, w, h).contains(x, y)) {
                            chooseUpgrade(i)
                            return true
                        }
                    }
                    return true
                }

                if (paused) {
                    val row = overlayButtonAt(x, y, w, h, 3)
                    when (row) {
                        0 -> { paused = false; lastFrameNs = System.nanoTime() }
                        1 -> startLevel(currentLevelId)
                        2 -> returnToMap()
                    }
                    return true
                }

                if (gameOver) {
                    val row = overlayButtonAt(x, y, w, h, 2)
                    when (row) {
                        0 -> startLevel(currentLevelId)
                        1 -> returnToMap()
                    }
                    return true
                }

                if (victory) {
                    val row = overlayButtonAt(x, y, w, h, 3)
                    when (row) {
                        0 -> startLevel(if (currentLevelId < 10) currentLevelId + 1 else currentLevelId)
                        1 -> startLevel(currentLevelId)
                        2 -> returnToMap()
                    }
                    return true
                }

                if (x in w * 0.02f..w * 0.16f && y in 0f..h * 0.10f) {
                    paused = true
                    lastFrameNs = System.nanoTime()
                    return true
                }

                dragging = true
                movePlayerTo(x, y)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (dragging && screen == Screen.GAME && !paused && !gameOver && !victory && upgradeChoices.isEmpty()) {
                    movePlayerTo(x, y)
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                dragging = false
                return true
            }
        }
        return true
    }

    private fun movePlayerTo(x: Float, y: Float) {
        val w = width.toFloat()
        val h = height.toFloat()
        playerX = x.coerceIn(w * 0.12f, w * 0.88f)
        playerY = y.coerceIn(h * 0.56f, h * 0.82f)
    }

    private fun overlayButtonAt(x: Float, y: Float, w: Float, h: Float, rows: Int): Int {
        for (row in 0 until rows) {
            val top = h * (0.43f + row * 0.105f)
            val rect = RectF(w * 0.22f, top, w * 0.78f, top + h * 0.072f)
            if (rect.contains(x, y)) return row
        }
        return -1
    }

    private fun compactNumber(value: Int): String = when {
        value >= 1_000_000 -> String.format("%.1M", value / 1_000_000f)
        value >= 10_000 -> String.format("%.0K", value / 1_000f)
        value >= 1_000 -> String.format("%.1K", value / 1_000f)
        else -> value.toString()
    }

    private fun shiftColor(color: Int, delta: Int): Int = Color.rgb(
        (Color.red(color) + delta).coerceIn(0, 255),
        (Color.green(color) + delta).coerceIn(0, 255),
        (Color.blue(color) + delta).coerceIn(0, 255)
    )
}
