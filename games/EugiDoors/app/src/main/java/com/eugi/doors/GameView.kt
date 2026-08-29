package com.eugi.doors

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

class GameView(context: Context) : View(context) {
    private enum class Mode { HUB, RUNNER, JUMPER, BOSS, SWARM }

    private val p = Paint().apply { isAntiAlias = false }
    private val tp = Paint().apply {
        isAntiAlias = false
        typeface = Typeface.MONOSPACE
    }
    private val rng = Random(7)
    private var mode = Mode.HUB
    private var lastNs = System.nanoTime()
    private var touchX = 0f
    private var touchY = 0f
    private var touching = false
    private var score = 0
    private var banner = ""
    private var bannerTime = 0f

    private data class Obstacle(var x: Float, val w: Float, val h: Float, val type: Int)
    private val obstacles = mutableListOf<Obstacle>()
    private var runY = 0f
    private var runVy = 0f
    private var runSpeed = 300f
    private var runSpawn = .7f

    private data class Platform(var x: Float, var y: Float, val w: Float, val boost: Boolean)
    private val platforms = mutableListOf<Platform>()
    private var jumpX = 0f
    private var jumpY = 0f
    private var jumpVx = 0f
    private var jumpVy = 0f

    private data class Orb(var x: Float, var y: Float, val vx: Float, val vy: Float)
    private val orbs = mutableListOf<Orb>()
    private var heroX = 0f
    private var heroY = 0f
    private var heroHp = 100f
    private var bossX = 0f
    private var bossY = 0f
    private var bossHp = 300f
    private var bossCooldown = .5f
    private var slashTime = 0f
    private var dodgeTime = 0f

    private data class Mob(var x: Float, var y: Float, val speed: Float, var hp: Int, val type: Int)
    private val mobs = mutableListOf<Mob>()
    private val drops = mutableListOf<PointF>()
    private var swarmX = 0f
    private var swarmY = 0f
    private var swarmHp = 100f
    private var swarmSpawn = .2f
    private var autoCooldown = 0f
    private var wormPhase = 0f

    override fun onDraw(c: Canvas) {
        val now = System.nanoTime()
        val dt = ((now - lastNs) / 1_000_000_000f).coerceIn(0f, .033f)
        lastNs = now

        when (mode) {
            Mode.HUB -> drawHub(c)
            Mode.RUNNER -> { updateRunner(dt); drawRunner(c) }
            Mode.JUMPER -> { updateJumper(dt); drawJumper(c) }
            Mode.BOSS -> { updateBoss(dt); drawBoss(c) }
            Mode.SWARM -> { updateSwarm(dt); drawSwarm(c) }
        }

        if (bannerTime > 0f) {
            bannerTime -= dt
            panel(c, width * .14f, height * .44f, width * .86f, height * .55f)
            text(c, banner, width / 2f, height * .505f, 18f, gold(), Paint.Align.CENTER)
        }
        invalidate()
    }

    private fun col(s: String) = Color.parseColor(s)
    private fun gold() = col("#E3BC62")
    private fun rect(c: Canvas, color: Int, l: Float, t: Float, r: Float, b: Float) {
        p.color = color
        p.style = Paint.Style.FILL
        c.drawRect(l, t, r, b, p)
    }
    private fun text(c: Canvas, s: String, x: Float, y: Float, size: Float, color: Int = Color.WHITE, align: Paint.Align = Paint.Align.LEFT) {
        tp.color = color
        tp.textSize = size
        tp.textAlign = align
        c.drawText(s, x, y, tp)
    }
    private fun panel(c: Canvas, l: Float, t: Float, r: Float, b: Float) {
        rect(c, gold(), l, t, r, b)
        rect(c, col("#15151D"), l + 3f, t + 3f, r - 3f, b - 3f)
    }
    private fun tileFloor(c: Canvas, top: Float = 0f, bottom: Float = height.toFloat(), organic: Boolean = false) {
        val bg = if (organic) col("#241820") else col("#23232A")
        val tile = if (organic) col("#35212C") else col("#36353E")
        rect(c, bg, 0f, top, width.toFloat(), bottom)
        val s = 28f
        var y = top
        var row = 0
        while (y < bottom) {
            var x = if (row % 2 == 0) 0f else -s / 2f
            while (x < width) {
                rect(c, tile, x + 1f, y + 1f, x + s - 1f, y + s - 1f)
                x += s
            }
            y += s
            row++
        }
    }
    private fun stone(c: Canvas, x: Float, y: Float, w: Float, h: Float) {
        rect(c, col("#2C2D34"), x, y, x + w, y + h)
        rect(c, col("#747751"), x, y, x + w, y + 5f)
        var yy = y + 13f
        while (yy < y + h) {
            rect(c, col("#17181D"), x, yy, x + w, yy + 2f)
            yy += 14f
        }
    }
    private fun hero(c: Canvas, x: Float, y: Float, scale: Float = 1f, attacking: Boolean = false) {
        val u = 3f * scale
        rect(c, col("#16131E"), x - 6*u, y - 6*u, x + 6*u, y + 5*u)
        rect(c, col("#A82E4D"), x - 6*u, y - 3*u, x + 6*u, y - u)
        rect(c, col("#F0C4B9"), x - 5*u, y - 14*u, x + 5*u, y - 7*u)
        rect(c, col("#15131D"), x - 7*u, y - 17*u, x + 7*u, y - 12*u)
        rect(c, col("#15131D"), x - 8*u, y - 14*u, x - 5*u, y - 5*u)
        rect(c, col("#D23559"), x - 10*u, y - 17*u, x - 6*u, y - 14*u)
        rect(c, col("#B92249"), x - 3*u, y - 11*u, x - u, y - 9*u)
        rect(c, col("#B92249"), x + 2*u, y - 11*u, x + 4*u, y - 9*u)
        rect(c, col("#24202E"), x - 5*u, y + 4*u, x - 2*u, y + 10*u)
        rect(c, col("#24202E"), x + 2*u, y + 4*u, x + 5*u, y + 10*u)
        rect(c, col("#8A2440"), x - 6*u, y + 9*u, x - 2*u, y + 11*u)
        rect(c, col("#8A2440"), x + 2*u, y + 9*u, x + 6*u, y + 11*u)
        rect(c, col("#C83A59"), x - 12*u, y - 4*u, x - 6*u, y - 2*u)
        if (attacking) {
            rect(c, col("#F3E7CE"), x + 6*u, y - 5*u, x + 18*u, y - 3*u)
            rect(c, col("#EB6081"), x + 17*u, y - 7*u, x + 19*u, y - u)
        } else {
            rect(c, col("#D7D5D8"), x + 5*u, y - 14*u, x + 7*u, y - 3*u)
        }
    }
    private fun slime(c: Canvas, x: Float, y: Float, scale: Float = 1f) {
        val u = 3f * scale
        rect(c, col("#633078"), x - 5*u, y - 4*u, x + 5*u, y + 4*u)
        rect(c, col("#8C4C9F"), x - 4*u, y - 6*u, x + 4*u, y - 3*u)
        rect(c, Color.WHITE, x - 2*u, y - u, x - u, y)
        rect(c, Color.WHITE, x + u, y - u, x + 2*u, y)
    }
    private fun bat(c: Canvas, x: Float, y: Float, scale: Float = 1f) {
        val u = 3f * scale
        rect(c, col("#2B1936"), x - 3*u, y - 2*u, x + 3*u, y + 3*u)
        rect(c, col("#754484"), x - 8*u, y - 4*u, x - 3*u, y - u)
        rect(c, col("#754484"), x + 3*u, y - 4*u, x + 8*u, y - u)
        rect(c, col("#F1D45A"), x - 2*u, y - u, x - u, y)
        rect(c, col("#F1D45A"), x + u, y - u, x + 2*u, y)
    }
    private fun gem(c: Canvas, x: Float, y: Float) {
        p.color = col("#F183A7")
        val path = Path()
        path.moveTo(x, y - 10f)
        path.lineTo(x + 7f, y)
        path.lineTo(x, y + 10f)
        path.lineTo(x - 7f, y)
        path.close()
        c.drawPath(path, p)
        rect(c, col("#FFE1EA"), x - 2f, y - 6f, x, y + 3f)
    }
    private fun coin(c: Canvas, x: Float, y: Float) {
        rect(c, col("#80510A"), x - 6f, y - 8f, x + 6f, y + 8f)
        rect(c, col("#F5C64E"), x - 4f, y - 7f, x + 4f, y + 7f)
        rect(c, col("#FFF0A0"), x - 2f, y - 5f, x, y + 5f)
    }
    private fun portraitHud(c: Canvas, title: String, value: String, sub: String) {
        panel(c, 9f, 9f, 88f, 89f)
        hero(c, 48f, 70f, .6f)
        text(c, title, 99f, 34f, 17f, gold())
        text(c, value, 99f, 59f, 23f)
        text(c, sub, 99f, 80f, 12f, col("#C8C7CF"))
    }
    private fun hearts(c: Canvas, x: Float, y: Float, count: Int) {
        for (i in 0 until 5) {
            val cx = x + i.toFloat() * 21f
            val color = if (i < count) col("#D83C59") else col("#3B3A42")
            rect(c, color, cx - 5f, y, cx + 5f, y + 9f)
            rect(c, color, cx - 8f, y + 3f, cx + 8f, y + 7f)
        }
    }
    private fun roundButton(c: Canvas, cx: Float, cy: Float, r: Float, label: String) {
        p.color = gold(); c.drawCircle(cx, cy, r, p)
        p.color = col("#1D293C"); c.drawCircle(cx, cy, r - 4f, p)
        text(c, label, cx, cy + 5f, 12f, Color.WHITE, Paint.Align.CENTER)
    }
    private fun dpad(c: Canvas, cx: Float, cy: Float) {
        p.color = gold(); c.drawCircle(cx, cy, 47f, p)
        p.color = col("#1B2535"); c.drawCircle(cx, cy, 43f, p)
        text(c, "+", cx, cy + 13f, 42f, col("#DADBE0"), Paint.Align.CENTER)
    }

    private fun drawHub(c: Canvas) {
        tileFloor(c)
        text(c, "EUGI DOORS", width / 2f, 48f, 28f, gold(), Paint.Align.CENTER)
        text(c, "RETRO TRIAL HALL", width / 2f, 72f, 13f, col("#C6B7CE"), Paint.Align.CENTER)
        val gap = 16f
        val w = (width - gap * 3f) / 2f
        val h = (height - 190f) / 2f
        val names = arrayOf("RUIN RUN", "SKY CLIMB", "WITCH QUEEN", "BLOOD CELL")
        val colors = intArrayOf(col("#A93A50"), col("#426CA5"), col("#71478C"), col("#843746"))
        for (i in 0..3) {
            val column = i % 2
            val row = i / 2
            val l = gap + column.toFloat() * (w + gap)
            val t = 96f + row.toFloat() * (h + gap)
            panel(c, l, t, l + w, t + h)
            stone(c, l + 10f, t + 10f, w - 20f, h - 42f)
            val dl = l + w * .3f
            val dr = l + w * .7f
            rect(c, colors[i], dl, t + 42f, dr, t + h - 50f)
            text(c, (i + 1).toString(), (dl + dr) / 2f, t + h * .53f, 38f, Color.WHITE, Paint.Align.CENTER)
            text(c, names[i], l + w / 2f, t + h - 18f, 12f, gold(), Paint.Align.CENTER)
        }
        hero(c, width / 2f, height - 38f, .78f)
    }

    private fun startRunner() {
        mode = Mode.RUNNER
        score = 0
        obstacles.clear()
        runY = 0f
        runVy = 0f
        runSpeed = 300f
        runSpawn = .65f
    }
    private fun updateRunner(dt: Float) {
        runVy += 930f * dt
        runY += runVy * dt
        if (runY > 0f) { runY = 0f; runVy = 0f }
        runSpeed += 7f * dt
        score += 1
        runSpawn -= dt
        if (runSpawn <= 0f) {
            obstacles += Obstacle(width + 20f, 34f + rng.nextInt(30), 35f + rng.nextInt(42), rng.nextInt(3))
            runSpawn = .7f + rng.nextFloat() * .65f
        }
        val ground = height * .72f
        val hx = width * .22f
        val iterator = obstacles.iterator()
        while (iterator.hasNext()) {
            val o = iterator.next()
            o.x -= runSpeed * dt
            if (o.x + o.w < 0f) {
                iterator.remove()
            } else if (hx + 17f > o.x && hx - 17f < o.x + o.w && ground + runY > ground - o.h && ground - 46f + runY < ground) {
                backToHub("RUIN RUN  $score")
                return
            }
        }
    }
    private fun drawRunner(c: Canvas) {
        val ground = height * .72f
        rect(c, col("#5E69A9"), 0f, 0f, width.toFloat(), ground)
        for (i in 0..5) {
            val x = i.toFloat() * 110f - (score % 110).toFloat()
            rect(c, col("#4B5064"), x, ground - 230f, x + 55f, ground)
            rect(c, col("#343844"), x + 10f, ground - 280f, x + 45f, ground - 230f)
        }
        stone(c, 0f, ground, width.toFloat(), height - ground)
        for (o in obstacles) {
            if (o.type == 0) {
                var x = o.x
                while (x < o.x + o.w) {
                    p.color = col("#E1DFD4")
                    val path = Path()
                    path.moveTo(x, ground)
                    path.lineTo(x + 7f, ground - o.h)
                    path.lineTo(x + 14f, ground)
                    path.close(); c.drawPath(path, p)
                    x += 14f
                }
            } else {
                stone(c, o.x, ground - o.h, o.w, o.h)
                if (o.type == 2) rect(c, col("#9D3649"), o.x + 8f, ground - o.h + 8f, o.x + o.w - 8f, ground - 8f)
            }
        }
        for (i in 0..3) {
            val x = width * .48f + i.toFloat() * 45f
            gem(c, x, ground - 88f); coin(c, x, ground - 55f)
        }
        hero(c, width * .22f, ground - 8f + runY, .84f)
        portraitHud(c, "SCORE", score.toString().padStart(6, '0'), "RUNNER")
        hearts(c, 105f, 91f, 4)
        text(c, "COMBO x ${(score / 30) % 99}", width - 94f, 47f, 14f, col("#E88448"), Paint.Align.CENTER)
        roundButton(c, 67f, height - 65f, 46f, "DASH")
        roundButton(c, width - 67f, height - 65f, 46f, "JUMP")
    }

    private fun startJumper() {
        mode = Mode.JUMPER
        score = 0
        jumpX = width / 2f
        jumpY = height * .72f
        jumpVx = 0f
        jumpVy = -520f
        platforms.clear()
        for (i in 0..11) {
            platforms += Platform(30f + rng.nextFloat() * (width - 125f), height * .86f - i.toFloat() * 76f, 70f + rng.nextFloat() * 30f, i % 4 == 0)
        }
    }
    private fun updateJumper(dt: Float) {
        if (touching) {
            val direction = ((touchX - width / 2f) / (width / 2f)).coerceIn(-1f, 1f)
            jumpVx = (jumpVx + direction * 850f * dt).coerceIn(-250f, 250f)
        } else jumpVx *= .91f
        jumpX = (jumpX + jumpVx * dt).coerceIn(16f, width - 16f)
        val previousY = jumpY
        jumpVy += 760f * dt
        jumpY += jumpVy * dt
        if (jumpVy > 0f) {
            for (platform in platforms) {
                if (previousY <= platform.y - 22f && jumpY >= platform.y - 22f && jumpX >= platform.x - 8f && jumpX <= platform.x + platform.w + 8f) {
                    jumpY = platform.y - 22f
                    jumpVy = if (platform.boost) -640f else -520f
                    score += if (platform.boost) 22 else 10
                    break
                }
            }
        }
        if (jumpY < height * .34f) {
            val shift = height * .34f - jumpY
            jumpY = height * .34f
            for (platform in platforms) platform.y += shift
            score += (shift / 3f).toInt()
        }
        var top = platforms.minOfOrNull { it.y } ?: 80f
        while (top > 30f) {
            top -= 70f + rng.nextFloat() * 30f
            platforms += Platform(24f + rng.nextFloat() * (width - 120f), top, 70f + rng.nextFloat() * 30f, rng.nextInt(5) == 0)
        }
        platforms.removeAll { it.y > height + 50f }
        if (jumpY > height + 40f) backToHub("SKY CLIMB  $score")
    }
    private fun drawJumper(c: Canvas) {
        rect(c, col("#6175BA"), 0f, 0f, width.toFloat(), height.toFloat())
        for (i in 0..7) {
            p.color = if (i % 2 == 0) col("#D7BCD3") else col("#C9CDE7")
            val x = ((i * 87) % max(1, width)).toFloat()
            val y = 130f + i.toFloat() * 115f
            c.drawCircle(x, y, 28f, p); c.drawCircle(x + 28f, y + 8f, 20f, p)
        }
        for (platform in platforms) {
            stone(c, platform.x, platform.y, platform.w, 18f)
            if (platform.boost) rect(c, col("#B46BD0"), platform.x + 8f, platform.y - 6f, platform.x + platform.w - 8f, platform.y + 2f)
        }
        platforms.take(5).forEachIndexed { index, platform ->
            if (index % 2 == 0) gem(c, platform.x + platform.w / 2f, platform.y - 28f) else coin(c, platform.x + platform.w / 2f, platform.y - 28f)
        }
        bat(c, width * .78f, height * .28f, .72f)
        slime(c, width * .72f, height * .62f, .72f)
        hero(c, jumpX, jumpY, .74f)
        portraitHud(c, "HEIGHT", "${score.toString().padStart(5, '0')}M", "SKY CLIMB")
        hearts(c, 105f, 91f, 4)
        roundButton(c, 67f, height - 65f, 46f, "LEFT")
        roundButton(c, width - 67f, height - 65f, 46f, "RIGHT")
    }

    private fun startBoss() {
        mode = Mode.BOSS
        score = 0
        heroHp = 100f
        bossHp = 300f
        heroX = width / 2f
        heroY = height * .68f
        bossX = width / 2f
        bossY = height * .31f
        orbs.clear()
        bossCooldown = .5f
        slashTime = 0f
        dodgeTime = 0f
    }
    private fun updateBoss(dt: Float) {
        slashTime = max(0f, slashTime - dt)
        dodgeTime = max(0f, dodgeTime - dt)
        if (touching && touchY < height - 120f) {
            val dx = touchX - heroX
            val dy = touchY - heroY
            val d = hypot(dx, dy).coerceAtLeast(1f)
            heroX = (heroX + dx / d * 175f * dt).coerceIn(30f, width - 30f)
            heroY = (heroY + dy / d * 175f * dt).coerceIn(155f, height - 120f)
        }
        bossCooldown -= dt
        if (bossCooldown <= 0f) {
            val base = atan2(heroY - bossY, heroX - bossX)
            val count = if (bossHp < 150f) 5 else 3
            for (i in 0 until count) {
                val offset = (i.toFloat() - (count - 1).toFloat() / 2f) * .22f
                val angle = base + offset
                val speed = 120f + rng.nextFloat() * 35f
                orbs += Orb(bossX, bossY, cos(angle) * speed, sin(angle) * speed)
            }
            bossCooldown = if (bossHp < 100f) .58f else .9f
        }
        val iterator = orbs.iterator()
        while (iterator.hasNext()) {
            val orb = iterator.next()
            orb.x += orb.vx * dt; orb.y += orb.vy * dt
            if (hypot(orb.x - heroX, orb.y - heroY) < 16f && dodgeTime <= 0f) {
                heroHp -= 12f; iterator.remove()
            } else if (orb.x < 10f || orb.x > width - 10f || orb.y < 145f || orb.y > height - 100f) iterator.remove()
        }
        if (heroHp <= 0f) backToHub("WITCH QUEEN FAILED")
    }
    private fun bossAttack() {
        slashTime = .18f
        if (hypot(heroX - bossX, heroY - bossY) < 155f) {
            bossHp -= 24f; score += 100
            if (bossHp <= 0f) backToHub("WITCH QUEEN CLEAR")
        }
    }
    private fun drawBoss(c: Canvas) {
        tileFloor(c, 130f, height - 100f)
        stone(c, 0f, 115f, width.toFloat(), 25f)
        stone(c, 0f, height - 100f, width.toFloat(), 100f)
        witch(c, bossX, bossY)
        for (orb in orbs) {
            p.color = col("#9C4BC2"); c.drawCircle(orb.x, orb.y, 9f, p)
            p.color = col("#F0A1FF"); c.drawCircle(orb.x, orb.y, 4f, p)
        }
        hero(c, heroX, heroY, .76f, slashTime > 0f)
        if (slashTime > 0f) {
            p.style = Paint.Style.STROKE; p.strokeWidth = 5f; p.color = col("#EF82BB")
            c.drawCircle(heroX + 16f, heroY - 4f, 27f, p)
            p.style = Paint.Style.FILL
        }
        val remaining = max(0, 180 - score / 5)
        val timer = "${remaining / 60}:${(remaining % 60).toString().padStart(2, '0')}"
        portraitHud(c, "LV.28", "HP ${heroHp.toInt()}/100", "TIME $timer")
        hearts(c, 105f, 91f, (heroHp / 20f).toInt().coerceIn(0, 5))
        text(c, "WITCH QUEEN MORGANA", width / 2f, 119f, 15f, col("#E4B7F1"), Paint.Align.CENTER)
        panel(c, 43f, 125f, width - 43f, 141f)
        rect(c, col("#C43F67"), 48f, 130f, 48f + (width - 96f) * (bossHp / 300f).coerceIn(0f, 1f), 136f)
        dpad(c, 67f, height - 62f)
        roundButton(c, width - 130f, height - 67f, 47f, "ATTACK")
        roundButton(c, width - 48f, height - 67f, 36f, "DODGE")
    }
    private fun witch(c: Canvas, x: Float, y: Float) {
        val u = 3f
        rect(c, col("#281831"), x - 9*u, y - u, x + 9*u, y + 14*u)
        rect(c, col("#55306A"), x - 7*u, y + 4*u, x + 7*u, y + 14*u)
        rect(c, gold(), x - u, y + 3*u, x + u, y + 13*u)
        rect(c, col("#E8C2B9"), x - 4*u, y - 10*u, x + 4*u, y - 3*u)
        rect(c, col("#24162B"), x - 6*u, y - 14*u, x + 6*u, y - 9*u)
        rect(c, col("#774D8B"), x - 7*u, y - 16*u, x + 7*u, y - 13*u)
        rect(c, gold(), x - 3*u, y - 19*u, x - u, y - 15*u)
        rect(c, gold(), x + u, y - 19*u, x + 3*u, y - 15*u)
        rect(c, col("#775E3D"), x - 12*u, y - 12*u, x - 10*u, y + 13*u)
        p.color = col("#B766D2"); c.drawCircle(x - 11*u, y - 14*u, 10f, p)
    }

    private fun startSwarm() {
        mode = Mode.SWARM
        score = 0
        swarmX = width / 2f
        swarmY = height / 2f
        swarmHp = 100f
        swarmSpawn = .2f
        autoCooldown = 0f
        wormPhase = 0f
        mobs.clear(); drops.clear()
    }
    private fun updateSwarm(dt: Float) {
        wormPhase += dt
        if (touching) {
            val dx = touchX - swarmX; val dy = touchY - swarmY
            val d = hypot(dx, dy).coerceAtLeast(1f)
            swarmX = (swarmX + dx / d * 190f * dt).coerceIn(22f, width - 22f)
            swarmY = (swarmY + dy / d * 190f * dt).coerceIn(120f, height - 100f)
        }
        score += 1
        swarmSpawn -= dt; autoCooldown -= dt
        if (swarmSpawn <= 0f) {
            val edge = rng.nextInt(4)
            val x = if (edge == 0) 14f else if (edge == 1) width - 14f else rng.nextFloat() * width
            val y = if (edge == 2) 125f else if (edge == 3) height - 105f else 125f + rng.nextFloat() * (height - 230f)
            mobs += Mob(x, y, 46f + rng.nextFloat() * 45f, if (rng.nextInt(5) == 0) 2 else 1, rng.nextInt(2))
            swarmSpawn = max(.14f, .6f - score / 4000f)
        }
        for (mob in mobs) {
            val dx = swarmX - mob.x; val dy = swarmY - mob.y
            val d = hypot(dx, dy).coerceAtLeast(1f)
            mob.x += dx / d * mob.speed * dt; mob.y += dy / d * mob.speed * dt
            if (d < 18f) swarmHp -= 22f * dt
        }
        if (autoCooldown <= 0f && mobs.isNotEmpty()) {
            val target = mobs.minByOrNull { hypot(it.x - swarmX, it.y - swarmY) }
            if (target != null) {
                target.hp--
                if (target.hp <= 0) { drops += PointF(target.x, target.y); mobs.remove(target); score += 20 }
            }
            autoCooldown = .18f
        }
        drops.removeAll { hypot(it.x - swarmX, it.y - swarmY) < 20f }
        if (swarmHp <= 0f) backToHub("BLOOD CELL  $score")
    }
    private fun drawSwarm(c: Canvas) {
        tileFloor(c, 0f, height.toFloat(), true)
        for (i in 0..16) {
            p.color = if (i % 2 == 0) col("#5B263A") else col("#6D2C46")
            val x = ((i * 61) % max(1, width)).toFloat()
            val y = ((i * 97) % max(1, height)).toFloat()
            c.drawCircle(x, y, 7f + (i % 3).toFloat() * 3f, p)
        }
        // Flat bird-view worm: small equal-sized segments, no perspective scaling.
        val wx = width - 42f
        val wy = 175f + (sin(wormPhase * .8f) + 1f) * 60f
        for (i in 0..7) {
            val sy = wy + i.toFloat() * 25f
            val sx = wx - (i % 2).toFloat() * 5f
            p.color = col("#674460"); c.drawCircle(sx, sy, 15f, p)
            p.color = col("#A54A62"); c.drawCircle(sx, sy, 5f, p)
        }
        p.color = col("#784459"); c.drawCircle(wx, wy - 18f, 18f, p)
        rect(c, col("#E6B4B0"), wx - 9f, wy - 21f, wx + 9f, wy - 18f)

        for (mob in mobs) if (mob.type == 0) slime(c, mob.x, mob.y, .62f) else bat(c, mob.x, mob.y, .62f)
        for (drop in drops) gem(c, drop.x, drop.y)
        hero(c, swarmX, swarmY, .7f, true)
        portraitHud(c, "HP", "${swarmHp.toInt()}/100", "EXP ${score % 100}%")
        text(c, "WAVE ${1 + score / 350}", 105f, 105f, 13f, gold())
        text(c, "COMBO x ${(score / 20) % 999}", width - 95f, 37f, 14f, col("#E88448"), Paint.Align.CENTER)
        dpad(c, 64f, height - 62f)
        roundButton(c, width - 58f, height - 68f, 44f, "AUTO")
        for (i in 0..2) {
            val l = 120f + i.toFloat() * 62f
            panel(c, l, height - 91f, l + 52f, height - 39f)
            val color = if (i == 0) col("#C9415E") else if (i == 1) col("#7747A1") else col("#D36A35")
            rect(c, color, l + 8f, height - 82f, l + 44f, height - 48f)
            text(c, (i + 1).toString(), l + 26f, height - 58f, 13f, Color.WHITE, Paint.Align.CENTER)
        }
    }

    private fun backToHub(message: String) {
        mode = Mode.HUB
        touching = false
        banner = message
        bannerTime = 1.5f
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        touchX = event.x; touchY = event.y
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touching = true
                when (mode) {
                    Mode.HUB -> {
                        if (event.y > 90f && event.y < height - 80f) {
                            val row = if (event.y < height / 2f) 0 else 1
                            val column = if (event.x < width / 2f) 0 else 1
                            when (row * 2 + column) {
                                0 -> startRunner()
                                1 -> startJumper()
                                2 -> startBoss()
                                3 -> startSwarm()
                            }
                        }
                    }
                    Mode.RUNNER -> {
                        if (event.x > width / 2f && runY == 0f) runVy = -535f else runSpeed += 45f
                    }
                    Mode.BOSS -> {
                        if (event.y > height - 135f && event.x > width - 185f) {
                            if (event.x < width - 88f) bossAttack() else dodgeTime = .42f
                        }
                    }
                    else -> Unit
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> touching = false
        }
        return true
    }
}
