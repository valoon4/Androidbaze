package com.androidbaze.starter

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.*
import kotlin.random.Random

class PikminLikeGameView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        color = Color.WHITE
    }

    private data class V(var x: Float, var y: Float)
    private data class Buddy(var p: V, val type: Int, var target: V? = null)
    private data class Enemy(var p: V, var hp: Int, var flash: Int = 0)
    private data class Item(var p: V, val type: Int, var taken: Boolean = false)

    private val worldW = 1920f
    private val worldH = 1080f
    private val player = V(820f, 610f)
    private val buddies = MutableList(24) { i ->
        Buddy(V(760f + (i % 8) * 34f, 690f + (i / 8) * 34f), i % 3)
    }
    private val enemies = mutableListOf(
        Enemy(V(430f, 420f), 6), Enemy(V(1260f, 500f), 7), Enemy(V(1510f, 760f), 8), Enemy(V(650f, 820f), 5)
    )
    private val items = mutableListOf(
        Item(V(330f, 690f), 0), Item(V(560f, 310f), 1), Item(V(1080f, 760f), 2),
        Item(V(1410f, 330f), 0), Item(V(1660f, 620f), 1), Item(V(1160f, 330f), 2)
    )

    private var fruit = 0
    private var crystal = 0
    private var pellet = 0
    private var stickId = -1
    private val stickOrigin = V(175f, 880f)
    private val stick = V(0f, 0f)
    private var last = System.nanoTime()
    private var whistle = 0f
    private var toast = "Gather resources and test the squad"
    private var toastTicks = 240

    init {
        setBackgroundColor(Color.rgb(34, 66, 42))
        isFocusable = true
    }

    override fun onDraw(c: Canvas) {
        val now = System.nanoTime()
        val dt = ((now - last) / 1_000_000_000f).coerceIn(0f, 0.033f)
        last = now
        update(dt)
        c.save()
        c.scale(width / worldW, height / worldH)
        drawScene(c)
        drawHud(c)
        c.restore()
        postInvalidateOnAnimation()
    }

    private fun update(dt: Float) {
        val speed = 410f
        player.x = (player.x + stick.x * speed * dt).coerceIn(90f, worldW - 90f)
        player.y = (player.y + stick.y * speed * dt).coerceIn(190f, worldH - 90f)

        buddies.forEachIndexed { i, b ->
            val fallback = V(
                player.x + cos(i * 1.9f) * (75f + (i % 4) * 22f),
                player.y + sin(i * 1.9f) * (60f + (i % 3) * 20f)
            )
            val t = b.target ?: fallback
            val dx = t.x - b.p.x
            val dy = t.y - b.p.y
            val d = hypot(dx, dy)
            if (d > 4f) {
                val s = if (b.target != null) 620f else 340f
                b.p.x += dx / d * min(d, s * dt)
                b.p.y += dy / d * min(d, s * dt)
            }
            if (b.target != null && d < 24f) b.target = null
        }

        items.filter { !it.taken }.forEach {
            if (hypot(player.x - it.p.x, player.y - it.p.y) < 70f) {
                it.taken = true
                when (it.type) { 0 -> fruit++; 1 -> crystal++; else -> pellet++ }
                show("Resource collected")
            }
        }
        enemies.forEach { if (it.flash > 0) it.flash-- }
        enemies.removeAll { it.hp <= 0 }
        whistle = max(0f, whistle - dt * 1.6f)
        if (toastTicks > 0) toastTicks--
    }

    private fun drawScene(c: Canvas) {
        // lawn base
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(89, 145, 75)
        c.drawRect(0f, 0f, worldW, worldH, paint)

        // darker outer foliage
        paint.color = Color.rgb(50, 100, 56)
        c.drawRect(0f, 145f, worldW, 225f, paint)
        c.drawRect(0f, 995f, worldW, worldH, paint)

        // dirt path network like mockup
        paint.color = Color.rgb(201, 169, 112)
        c.drawRoundRect(360f, 260f, 1600f, 910f, 210f, 210f, paint)
        paint.color = Color.rgb(105, 157, 82)
        c.drawRoundRect(500f, 355f, 1480f, 805f, 170f, 170f, paint)
        paint.color = Color.rgb(221, 190, 131)
        c.drawRoundRect(760f, 210f, 1010f, 945f, 80f, 80f, paint)

        // pond
        paint.color = Color.rgb(48, 135, 165)
        c.drawOval(55f, 245f, 390f, 575f, paint)
        paint.color = Color.rgb(80, 176, 184)
        c.drawOval(95f, 285f, 350f, 535f, paint)
        repeat(5) { i ->
            paint.color = Color.rgb(77, 144, 75)
            val x = 135f + i * 43f
            val y = 360f + (i % 2) * 70f
            c.drawOval(x, y, x + 62f, y + 34f, paint)
        }

        // flowers / grass decor
        val rnd = Random(19)
        repeat(150) { i ->
            val x = rnd.nextInt(20, 1880).toFloat()
            val y = rnd.nextInt(220, 990).toFloat()
            if (x in 400f..1580f && y in 300f..900f && i % 3 != 0) return@repeat
            paint.color = if (i % 2 == 0) Color.rgb(55, 112, 61) else Color.rgb(72, 126, 62)
            c.drawRect(x, y, x + 5f, y + 20f, paint)
            if (i % 6 == 0) {
                paint.color = listOf(Color.rgb(247,223,105), Color.rgb(243,154,181), Color.WHITE)[i % 3]
                c.drawCircle(x + 2f, y - 2f, 7f, paint)
            }
        }

        // pod / base top-right
        paint.color = Color.argb(80, 238, 255, 221)
        c.drawCircle(1680f, 300f, 130f, paint)
        paint.color = Color.rgb(231, 236, 220)
        c.drawCircle(1680f, 300f, 92f, paint)
        paint.color = Color.rgb(193, 65, 56)
        c.drawArc(1588f, 208f, 1772f, 392f, 195f, 150f, true, paint)
        paint.color = Color.rgb(52, 78, 91)
        c.drawCircle(1680f, 318f, 34f, paint)
        paint.color = Color.rgb(111, 226, 118)
        c.drawCircle(1680f, 305f, 11f, paint)
        paint.strokeWidth = 8f
        paint.style = Paint.Style.STROKE
        paint.color = Color.rgb(224, 230, 212)
        c.drawLine(1680f, 213f, 1718f, 172f, paint)
        c.drawCircle(1728f, 162f, 21f, paint)
        paint.style = Paint.Style.FILL
        text.textSize = 28f
        c.drawText("BASE POD", 1600f, 425f, text)

        // crates
        paint.color = Color.rgb(116, 76, 45)
        c.drawRect(1530f, 450f, 1620f, 520f, paint)
        paint.color = Color.rgb(174, 119, 66)
        c.drawRect(1540f, 462f, 1610f, 508f, paint)

        items.filter { !it.taken }.forEach { drawItem(c, it) }
        enemies.forEach { drawEnemy(c, it) }
        buddies.forEach { drawBuddy(c, it) }
        drawPlayer(c)

        if (whistle > 0f) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 8f
            paint.color = Color.argb((180 * whistle).toInt(), 255, 248, 176)
            c.drawCircle(player.x, player.y, 90f + (1f - whistle) * 250f, paint)
            paint.style = Paint.Style.FILL
        }
    }

    private fun drawPlayer(c: Canvas) {
        // shadow
        paint.color = Color.argb(70, 0, 0, 0)
        c.drawOval(player.x - 42f, player.y + 38f, player.x + 42f, player.y + 65f, paint)
        paint.color = Color.rgb(235, 237, 221)
        c.drawCircle(player.x, player.y, 34f, paint)
        paint.color = Color.rgb(50, 82, 103)
        c.drawRoundRect(player.x - 24f, player.y - 11f, player.x + 24f, player.y + 10f, 8f, 8f, paint)
        paint.color = Color.rgb(198, 63, 52)
        c.drawRoundRect(player.x - 28f, player.y + 28f, player.x + 28f, player.y + 67f, 10f, 10f, paint)
        paint.color = Color.WHITE
        c.drawCircle(player.x - 9f, player.y - 1f, 4f, paint)
        c.drawCircle(player.x + 9f, player.y - 1f, 4f, paint)
    }

    private fun drawBuddy(c: Canvas, b: Buddy) {
        paint.color = Color.argb(55,0,0,0)
        c.drawOval(b.p.x-18f,b.p.y+16f,b.p.x+18f,b.p.y+28f,paint)
        paint.color = when (b.type) {
            0 -> Color.rgb(224, 73, 67)
            1 -> Color.rgb(246, 205, 60)
            else -> Color.rgb(69, 156, 220)
        }
        c.drawOval(b.p.x - 16f, b.p.y - 12f, b.p.x + 16f, b.p.y + 22f, paint)
        paint.color = Color.WHITE
        c.drawCircle(b.p.x - 6f, b.p.y - 2f, 4.5f, paint)
        c.drawCircle(b.p.x + 6f, b.p.y - 2f, 4.5f, paint)
        paint.color = Color.rgb(39, 75, 43)
        c.drawRect(b.p.x - 2f, b.p.y - 34f, b.p.x + 2f, b.p.y - 13f, paint)
        c.drawOval(b.p.x + 1f, b.p.y - 42f, b.p.x + 20f, b.p.y - 28f, paint)
    }

    private fun drawEnemy(c: Canvas, e: Enemy) {
        paint.color = Color.argb(70,0,0,0)
        c.drawOval(e.p.x-62f,e.p.y+28f,e.p.x+62f,e.p.y+52f,paint)
        paint.color = if (e.flash > 0) Color.WHITE else Color.rgb(191, 93, 62)
        c.drawOval(e.p.x - 58f, e.p.y - 34f, e.p.x + 58f, e.p.y + 38f, paint)
        paint.color = Color.rgb(91, 60, 48)
        c.drawCircle(e.p.x - 30f, e.p.y - 28f, 22f, paint)
        c.drawCircle(e.p.x + 30f, e.p.y - 28f, 22f, paint)
        paint.color = Color.WHITE
        c.drawCircle(e.p.x - 30f, e.p.y - 31f, 10f, paint)
        c.drawCircle(e.p.x + 30f, e.p.y - 31f, 10f, paint)
        paint.color = Color.BLACK
        c.drawCircle(e.p.x - 30f, e.p.y - 31f, 4f, paint)
        c.drawCircle(e.p.x + 30f, e.p.y - 31f, 4f, paint)
        paint.color = Color.rgb(54, 45, 38)
        repeat(3) { i -> c.drawCircle(e.p.x - 25f + i * 25f, e.p.y + 2f, 8f, paint) }
        paint.color = Color.rgb(61, 40, 35)
        c.drawRoundRect(e.p.x-52f,e.p.y+54f,e.p.x+52f,e.p.y+69f,7f,7f,paint)
        paint.color = Color.rgb(236, 82, 69)
        c.drawRoundRect(e.p.x-49f,e.p.y+57f,e.p.x-49f+98f*(e.hp/8f).coerceIn(0f,1f),e.p.y+66f,5f,5f,paint)
    }

    private fun drawItem(c: Canvas, it: Item) {
        paint.color = Color.argb(60,0,0,0)
        c.drawOval(it.p.x-34f,it.p.y+26f,it.p.x+34f,it.p.y+42f,paint)
        when (it.type) {
            0 -> {
                paint.color = Color.rgb(222, 54, 66)
                c.drawCircle(it.p.x, it.p.y, 35f, paint)
                paint.color = Color.rgb(58, 123, 57)
                c.drawOval(it.p.x-18f,it.p.y-50f,it.p.x+20f,it.p.y-25f,paint)
                paint.color = Color.rgb(255, 209, 90)
                repeat(6){i-> c.drawCircle(it.p.x-18f+(i%3)*18f,it.p.y-8f+(i/3)*18f,3f,paint)}
            }
            1 -> {
                paint.color = Color.rgb(74, 206, 232)
                val p = Path().apply { moveTo(it.p.x,it.p.y-48f); lineTo(it.p.x+34f,it.p.y); lineTo(it.p.x,it.p.y+48f); lineTo(it.p.x-34f,it.p.y); close() }
                c.drawPath(p,paint)
                paint.color = Color.rgb(180,245,255)
                c.drawPath(Path().apply{moveTo(it.p.x,it.p.y-36f);lineTo(it.p.x+10f,it.p.y-2f);lineTo(it.p.x,it.p.y+9f);lineTo(it.p.x-7f,it.p.y-4f);close()},paint)
            }
            else -> {
                paint.color = Color.rgb(231, 197, 102)
                c.drawCircle(it.p.x,it.p.y,34f,paint)
                paint.color = Color.rgb(152, 108, 50)
                c.drawCircle(it.p.x,it.p.y,15f,paint)
            }
        }
    }

    private fun drawHud(c: Canvas) {
        paint.color = Color.argb(225, 27, 38, 38)
        c.drawRoundRect(20f, 18f, 1900f, 135f, 28f, 28f, paint)
        text.textSize = 32f
        c.drawText("DAY 01", 55f, 62f, text)
        text.color = Color.rgb(245,205,80)
        c.drawText("10:42", 55f, 108f, text)
        text.color = Color.WHITE
        text.textSize = 27f
        c.drawText("MISSION  Bring resources back to the pod", 280f, 62f, text)
        c.drawText("SQUAD  ${buddies.size}/30", 1515f, 62f, text)
        c.drawText("FRUIT $fruit    CRYSTAL $crystal    PELLET $pellet", 280f, 108f, text)

        if (toastTicks > 0) {
            paint.color = Color.argb(190, 25, 34, 34)
            c.drawRoundRect(700f, 155f, 1220f, 215f, 22f, 22f, paint)
            text.textAlign = Paint.Align.CENTER
            text.textSize = 22f
            c.drawText(toast, 960f, 194f, text)
            text.textAlign = Paint.Align.LEFT
        }

        paint.color = Color.argb(90,255,255,255)
        c.drawCircle(stickOrigin.x,stickOrigin.y,105f,paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
        paint.color = Color.argb(150,255,255,255)
        c.drawCircle(stickOrigin.x,stickOrigin.y,105f,paint)
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(175,245,245,245)
        c.drawCircle(stickOrigin.x+stick.x*58f,stickOrigin.y+stick.y*58f,48f,paint)

        button(c, 1595f, 830f, 78f, Color.rgb(44,143,128), "WHISTLE")
        button(c, 1745f, 900f, 78f, Color.rgb(217,162,58), "THROW")
        button(c, 1570f, 965f, 84f, Color.rgb(183,68,61), "ATTACK")
    }

    private fun button(c: Canvas, x:Float,y:Float,r:Float,color:Int,label:String){
        paint.color=color;c.drawCircle(x,y,r,paint)
        paint.style=Paint.Style.STROKE;paint.strokeWidth=6f;paint.color=Color.argb(190,255,255,255);c.drawCircle(x,y,r-5f,paint);paint.style=Paint.Style.FILL
        text.textAlign=Paint.Align.CENTER;text.textSize=19f;c.drawText(label,x,y+6f,text);text.textAlign=Paint.Align.LEFT
    }

    private fun show(s:String){toast=s;toastTicks=120}

    private fun nearestEnemy(max:Float): Enemy? = enemies.minByOrNull { hypot(it.p.x-player.x,it.p.y-player.y) }?.takeIf { hypot(it.p.x-player.x,it.p.y-player.y) <= max }

    private fun attack(power:Int){
        val e=nearestEnemy(330f)
        if(e==null){show("Move closer to an enemy");return}
        e.hp-=power;e.flash=8
        buddies.take(power*3).forEach { it.target=V(e.p.x+Random.nextInt(-45,46),e.p.y+Random.nextInt(-35,36)) }
        show("Squad attack!")
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val x=ev.x*worldW/width
        val y=ev.y*worldH/height
        when(ev.actionMasked){
            MotionEvent.ACTION_DOWN,MotionEvent.ACTION_POINTER_DOWN->{
                val idx=ev.actionIndex;val id=ev.getPointerId(idx);val px=ev.getX(idx)*worldW/width;val py=ev.getY(idx)*worldH/height
                if(hypot(px-stickOrigin.x,py-stickOrigin.y)<150f && stickId==-1){stickId=id;updateStick(px,py);return true}
                if(hypot(px-1595f,py-830f)<95f){whistle=1f;buddies.forEach{it.target=null};show("Squad regrouped");return true}
                if(hypot(px-1745f,py-900f)<95f){attack(1);return true}
                if(hypot(px-1570f,py-965f)<105f){attack(2);return true}
            }
            MotionEvent.ACTION_MOVE->{
                val idx=ev.findPointerIndex(stickId)
                if(idx>=0)updateStick(ev.getX(idx)*worldW/width,ev.getY(idx)*worldH/height)
            }
            MotionEvent.ACTION_UP,MotionEvent.ACTION_POINTER_UP,MotionEvent.ACTION_CANCEL->{
                val id=ev.getPointerId(ev.actionIndex)
                if(id==stickId){stickId=-1;stick.x=0f;stick.y=0f}
            }
        }
        return true
    }

    private fun updateStick(x:Float,y:Float){
        var dx=x-stickOrigin.x;var dy=y-stickOrigin.y;val d=hypot(dx,dy)
        if(d>1f){val m=min(d,80f);dx=dx/d*m;dy=dy/d*m}
        stick.x=(dx/80f).coerceIn(-1f,1f);stick.y=(dy/80f).coerceIn(-1f,1f)
    }
}
