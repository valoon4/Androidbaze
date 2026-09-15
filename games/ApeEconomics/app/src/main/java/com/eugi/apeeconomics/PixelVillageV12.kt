package com.eugi.apeeconomics

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min

/**
 * Ape Economics v0.12
 * Full visual rebuild of the village screen. The scene is intentionally drawn
 * as crisp pixel art directly on Canvas so it scales to every phone without
 * placeholder geometry or triangular "programmer art" roofs.
 */
class PixelVillageV12(context: Context) : View(context) {
    private val p = Paint().apply { isAntiAlias = false }
    private val text = Paint().apply {
        isAntiAlias = false
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private data class Ape(
        var x: Float, var y: Float,
        val homeX: Float, val homeY: Float,
        val jobX: Float, val jobY: Float,
        val shirt: Int,
        var phase: Int = 0,
        var since: Long = 0L
    )

    private var bananas = 128
    private var day = 1
    private var dayStart = System.currentTimeMillis()
    private var lastIncome = System.currentTimeMillis()
    private var eventOpen = false

    private val apes = mutableListOf(
        Ape(.20f, .425f, .20f, .425f, .205f, .315f, C(46, 103, 167)),
        Ape(.50f, .445f, .50f, .445f, .505f, .300f, C(59, 70, 78)),
        Ape(.80f, .430f, .80f, .430f, .805f, .315f, C(58, 112, 69)),
        Ape(.60f, .705f, .60f, .705f, .805f, .735f, C(67, 91, 105))
    )

    init {
        keepScreenOn = true
        val now = System.currentTimeMillis()
        apes.forEachIndexed { i, a -> a.since = now + i * 510L }
    }

    fun handleBack(): Boolean {
        if (eventOpen) {
            eventOpen = false
            return true
        }
        return false
    }

    override fun onDraw(c: Canvas) {
        val now = System.currentTimeMillis()
        update(now)
        drawScene(c, now)
        postInvalidateOnAnimation()
    }

    private fun update(now: Long) {
        while (now - lastIncome >= 5_000L) {
            bananas += 1
            lastIncome += 5_000L
        }
        if (!eventOpen && now - dayStart >= 30_000L) {
            day++
            dayStart = now
            if (day % 3 == 0) eventOpen = true
        }
        if (eventOpen) return

        apes.forEach { a ->
            val duration = when (a.phase) {
                0 -> 900L
                1 -> 1750L
                2 -> 2200L
                else -> 1750L
            }
            if (now - a.since > duration) {
                a.phase = (a.phase + 1) % 4
                a.since = now
                if (a.phase == 3) bananas += if (a.shirt == C(46, 103, 167)) 6 else 2
            }
            val f = ((now - a.since).toFloat() / duration).coerceIn(0f, 1f)
            when (a.phase) {
                1 -> { a.x = lerp(a.homeX, a.jobX, f); a.y = lerp(a.homeY, a.jobY, f) }
                2 -> { a.x = a.jobX; a.y = a.jobY }
                3 -> { a.x = lerp(a.jobX, a.homeX, f); a.y = lerp(a.jobY, a.homeY, f) }
                else -> { a.x = a.homeX; a.y = a.homeY }
            }
        }
    }

    private fun drawScene(c: Canvas, now: Long) {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return
        val top = h * .132f
        val bottom = h * .895f
        val px = max(2f, w / 360f)

        fill(c, 0f, top, w, bottom, C(35, 94, 53))
        fill(c, w * .015f, top + px * 3, w * .985f, bottom - px * 2, C(93, 174, 74))

        for (i in 0 until 95) {
            val xx = w * (((i * 47) % 97) / 100f + .015f)
            val yy = top + (bottom - top) * (((i * 71) % 91) / 100f)
            val s = if (i % 3 == 0) px * 2 else px
            p.color = if (i % 4 == 0) C(113, 190, 83) else C(78, 158, 65)
            c.drawRect(xx, yy, xx + s, yy + s, p)
        }

        drawPaths(c, w, h, px)
        drawRiver(c, w, h, px, now)
        drawPlantationZone(c, w, h, px)
        drawParliament(c, w * .365f, h * .190f, w * .635f, h * .365f, px)
        drawBank(c, w * .700f, h * .205f, w * .945f, h * .382f, px)
        drawMarket(c, w * .700f, h * .635f, w * .945f, h * .805f, px)
        drawBridge(c, w * .455f, h * .500f, w * .545f, h * .603f, px)

        val edgeTrees = floatArrayOf(.02f,.08f,.15f,.23f,.31f,.69f,.77f,.85f,.92f,.98f)
        edgeTrees.forEachIndexed { i, x ->
            drawTree(c, w * x, top + h * .025f + (i % 2) * px * 3, w * .050f, i)
        }
        val lowerTrees = arrayOf(
            .075f to .642f, .155f to .655f, .245f to .635f, .325f to .670f,
            .055f to .835f, .145f to .845f, .260f to .830f, .620f to .825f
        )
        lowerTrees.forEachIndexed { i, q -> drawTree(c, w * q.first, h * q.second, w * .055f, i + 20) }

        for (i in 0 until 22) {
            val x = w * (.04f + ((i * 41) % 91) / 100f)
            val y = h * (.155f + ((i * 59) % 68) / 100f)
            if (y !in h*.49f..h*.61f) {
                if (i % 3 == 0) drawBush(c, x, y, w * .020f, i)
                else drawFlower(c, x, y, max(px * 2, w * .006f), i)
            }
        }
        drawSign(c, w * .402f, h * .675f, px)
        drawLamp(c, w * .575f, h * .417f, px)
        drawLamp(c, w * .655f, h * .705f, px)

        apes.sortedBy { it.y }.forEachIndexed { i, a ->
            drawApe(c, w * a.x, h * a.y, w * .035f, a.shirt, now, i, a.phase == 2)
        }

        drawHud(c, w, h, now, px)
        drawBottomBar(c, w, h, px)
        if (eventOpen) drawEvent(c, w, h, px)
    }

    private fun drawPaths(c: Canvas, w: Float, h: Float, px: Float) {
        fill(c, w*.440f, h*.132f, w*.560f, h*.895f, C(177,145,85))
        fill(c, w*.450f, h*.132f, w*.550f, h*.895f, C(229,201,132))
        fill(c, w*.045f, h*.416f, w*.955f, h*.497f, C(177,145,85))
        fill(c, w*.055f, h*.426f, w*.945f, h*.487f, C(229,201,132))
        fill(c, w*.085f, h*.678f, w*.930f, h*.755f, C(177,145,85))
        fill(c, w*.095f, h*.688f, w*.920f, h*.745f, C(229,201,132))

        for (i in 0 until 34) {
            val x = w * (.07f + ((i*31)%84)/100f)
            val y = h * (.435f + (i%3)*.014f)
            fill(c, x, y, x + px*3, y + px, C(211,177,110))
        }
        for (i in 0 until 25) {
            val x = w * (.11f + ((i*37)%79)/100f)
            val y = h * (.696f + (i%2)*.020f)
            fill(c, x, y, x + px*2, y + px, C(211,177,110))
        }
    }

    private fun drawRiver(c: Canvas, w: Float, h: Float, px: Float, now: Long) {
        fill(c, 0f, h*.498f, w, h*.512f, C(40,103,59))
        fill(c, 0f, h*.512f, w, h*.598f, C(28,111,183))
        fill(c, 0f, h*.526f, w, h*.584f, C(45,152,211))
        fill(c, 0f, h*.584f, w, h*.598f, C(25,94,157))
        val shift = ((now / 160L) % 24L).toFloat() * px
        for (i in -1..9) {
            val x = i * w*.13f + shift
            fill(c, x, h*.542f, x + w*.055f, h*.546f, C(178,226,232))
            fill(c, x + w*.025f, h*.570f, x + w*.075f, h*.574f, C(123,204,229))
        }
        for (i in 0 until 12) {
            val x = w * ((i*83)%100) / 100f
            fill(c, x, h*.502f, x+px*4, h*.508f, C(104,130,76))
            if (i%2==0) {
                fill(c, x+px, h*.490f, x+px*2, h*.508f, C(50,128,58))
                fill(c, x+px*3, h*.494f, x+px*4, h*.508f, C(63,145,63))
            }
        }
    }

    private fun drawPlantationZone(c: Canvas, w: Float, h: Float, px: Float) {
        drawFence(c, w*.035f, h*.195f, w*.325f, h*.408f, px)
        drawPlantationBuilding(c, w*.080f, h*.198f, w*.305f, h*.310f, px)
        for (r in 0..2) for (k in 0..3) {
            val x = w*(.065f+k*.065f)
            val y = h*(.330f+r*.024f)
            drawBananaPlant(c, x, y, w*.018f, px)
        }
        drawCrate(c, w*.300f, h*.386f, w*.025f, px)
    }

    private fun drawPlantationBuilding(c: Canvas, l: Float, tt: Float, r: Float, b: Float, px: Float) {
        val q = r-l
        fill(c,l+px*5,tt+q*.11f,r+px*7,b+px*5,C(43,75,45))
        fill(c,l-q*.035f,tt+q*.075f,r+q*.035f,tt+q*.145f,C(82,48,26))
        fill(c,l+q*.015f,tt+q*.035f,r-q*.015f,tt+q*.090f,C(111,62,29))
        fill(c,l+q*.070f,tt,r-q*.070f,tt+q*.050f,C(139,78,33))
        for (i in 0..7) {
            val x=l+q*.02f+i*q*.125f
            fill(c,x,tt+q*.045f,x+px*2,tt+q*.142f,C(77,45,26))
        }
        fill(c,l,tt+q*.14f,r,b,C(174,109,45))
        fill(c,l+px*4,tt+q*.16f,r-px*4,b-px*4,C(199,133,55))
        for (i in 0..6) {
            val x=l+i*q/6f
            fill(c,x,tt+q*.15f,x+px*2,b,C(117,70,34))
        }
        fill(c,l+q*.39f,b-q*.25f,l+q*.61f,b,C(73,48,31))
        fill(c,l+q*.43f,b-q*.20f,l+q*.57f,b,C(111,71,38))
        window(c,l+q*.10f,tt+q*.19f,q*.16f,q*.13f,px)
        window(c,r-q*.26f,tt+q*.19f,q*.16f,q*.13f,px)
        drawCrate(c,l+q*.07f,b-q*.08f,q*.08f,px)
        drawCrate(c,r-q*.15f,b-q*.08f,q*.08f,px)
        plaque(c,(l+r)/2,b+q*.065f,q*.62f,q*.105f,"PLANTAGE",px)
    }

    private fun drawParliament(c: Canvas, l: Float, tt: Float, r: Float, b: Float, px: Float) {
        val q=r-l
        fill(c,l+px*6,tt+q*.08f,r+px*7,b+px*6,C(44,74,48))
        fill(c,l-q*.02f,tt+q*.105f,r+q*.02f,tt+q*.155f,C(72,77,75))
        fill(c,l+q*.05f,tt+q*.065f,r-q*.05f,tt+q*.115f,C(99,103,99))
        fill(c,l+q*.15f,tt+q*.030f,r-q*.15f,tt+q*.075f,C(123,126,120))
        fill(c,l+q*.34f,tt,r-q*.34f,tt+q*.04f,C(154,155,144))
        fill(c,(l+r)/2-px,tt-q*.06f,(l+r)/2+px,tt+q*.015f,C(73,53,35))
        fill(c,(l+r)/2+px,tt-q*.055f,(l+r)/2+q*.12f,tt-q*.015f,C(220,177,54))
        fill(c,l,tt+q*.15f,r,b,C(159,160,151))
        fill(c,l+px*4,tt+q*.17f,r-px*4,b-px*4,C(189,188,174))
        fill(c,l+q*.35f,tt+q*.13f,r-q*.35f,b,C(135,138,133))
        fill(c,l+q*.39f,b-q*.25f,r-q*.39f,b,C(66,63,58))
        fill(c,l+q*.42f,b-q*.20f,r-q*.42f,b,C(103,78,46))
        for (i in 0..3) {
            val x=l+q*(.10f+i*.21f)
            fill(c,x,b-q*.31f,x+q*.035f,b-q*.03f,C(219,214,194))
            fill(c,x-px,b-q*.32f,x+q*.035f+px,b-q*.29f,C(128,132,128))
        }
        window(c,l+q*.07f,tt+q*.20f,q*.13f,q*.12f,px)
        window(c,l+q*.235f,tt+q*.20f,q*.13f,q*.12f,px)
        window(c,r-q*.365f,tt+q*.20f,q*.13f,q*.12f,px)
        window(c,r-q*.20f,tt+q*.20f,q*.13f,q*.12f,px)
        plaque(c,(l+r)/2,b+q*.060f,q*.70f,q*.105f,"PARLAMENT",px)
    }

    private fun drawBank(c: Canvas, l: Float, tt: Float, r: Float, b: Float, px: Float) {
        val q=r-l
        fill(c,l+px*6,tt+q*.09f,r+px*7,b+px*6,C(44,74,48))
        fill(c,l-q*.025f,tt+q*.10f,r+q*.025f,tt+q*.165f,C(102,55,31))
        fill(c,l+q*.04f,tt+q*.055f,r-q*.04f,tt+q*.115f,C(146,78,38))
        fill(c,l+q*.12f,tt+q*.018f,r-q*.12f,tt+q*.070f,C(187,106,49))
        for(i in 0..8) {
            val x=l+q*.04f+i*q*.115f
            fill(c,x,tt+q*.062f,x+px,tt+q*.158f,C(93,52,31))
        }
        fill(c,l,tt+q*.16f,r,b,C(171,120,72))
        fill(c,l+px*4,tt+q*.18f,r-px*4,b-px*4,C(205,157,96))
        for(i in 0..3) {
            val x=l+q*(.12f+i*.23f)
            fill(c,x,b-q*.34f,x+q*.045f,b-q*.035f,C(229,214,176))
            fill(c,x-px,b-q*.355f,x+q*.045f+px,b-q*.325f,C(120,94,65))
        }
        fill(c,l+q*.405f,b-q*.28f,r-q*.405f,b,C(64,62,57))
        fill(c,l+q*.44f,b-q*.225f,r-q*.44f,b,C(93,94,89))
        p.color=C(214,174,58); c.drawCircle((l+r)/2,b-q*.12f,q*.035f,p)
        fill(c,l+q*.40f,tt+q*.185f,r-q*.40f,tt+q*.31f,C(92,62,32))
        label(c,"B",(l+r)/2,tt+q*.285f,q*.105f,C(237,191,54),true)
        plaque(c,(l+r)/2,b+q*.063f,q*.46f,q*.105f,"BANK",px)
    }

    private fun drawMarket(c: Canvas, l: Float, tt: Float, r: Float, b: Float, px: Float) {
        val q=r-l
        fill(c,l+px*6,tt+q*.10f,r+px*7,b+px*5,C(44,74,48))
        fill(c,l-q*.025f,tt+q*.08f,r+q*.025f,tt+q*.17f,C(107,48,35))
        fill(c,l+q*.03f,tt+q*.035f,r-q*.03f,tt+q*.105f,C(160,66,46))
        fill(c,l+q*.12f,tt,r-q*.12f,tt+q*.055f,C(190,83,55))
        for(i in 0..7) {
            val x=l+i*q*.14f
            fill(c,x,tt+q*.09f,x+q*.08f,tt+q*.15f,if(i%2==0) C(200,91,57) else C(137,57,42))
        }
        fill(c,l,tt+q*.16f,r,b,C(178,120,69))
        fill(c,l+px*4,tt+q*.18f,r-px*4,b-px*4,C(213,157,91))
        for(i in 0..5) {
            val x=l+q*.08f+i*q*.14f
            fill(c,x,b-q*.39f,x+q*.07f,b-q*.29f,if(i%2==0) C(239,225,185) else C(179,65,52))
        }
        fill(c,l+q*.07f,b-q*.29f,r-q*.07f,b-q*.25f,C(91,55,34))
        drawCrate(c,l+q*.10f,b-q*.15f,q*.12f,px)
        drawCrate(c,l+q*.25f,b-q*.15f,q*.12f,px)
        p.color=C(240,192,45); c.drawCircle(l+q*.16f,b-q*.18f,q*.018f,p)
        p.color=C(218,68,51); c.drawCircle(l+q*.31f,b-q*.18f,q*.018f,p)
        fill(c,r-q*.22f,b-q*.31f,r-q*.10f,b,C(89,55,35))
        plaque(c,(l+r)/2,b+q*.063f,q*.48f,q*.105f,"MARKT",px)
    }

    private fun drawBridge(c: Canvas, l: Float, tt: Float, r: Float, b: Float, px: Float) {
        fill(c,l-px*5,tt,r+px*5,b,C(73,47,28))
        fill(c,l,tt,r,b,C(170,111,52))
        var y=tt+px*2
        var k=0
        while(y<b) {
            fill(c,l+px,y,r-px,y+px*5,if(k%2==0) C(194,132,61) else C(154,95,45))
            fill(c,l+px,y+px*5,r-px,y+px*6,C(100,61,32))
            y+=px*8; k++
        }
        fill(c,l-px*5,tt,l-px*2,b,C(84,52,29))
        fill(c,r+px*2,tt,r+px*5,b,C(84,52,29))
        for(i in 0..4) {
            val yy=tt+i*(b-tt)/4f
            fill(c,l-px*8,yy,l-px*2,yy+px*3,C(119,75,34))
            fill(c,r+px*2,yy,r+px*8,yy+px*3,C(119,75,34))
        }
    }

    private fun drawHud(c: Canvas, w: Float, h: Float, now: Long, px: Float) {
        fill(c,0f,0f,w,h*.132f,C(25,46,42))
        fill(c,0f,h*.124f,w,h*.132f,C(17,32,30))

        hudPanel(c,w*.018f,h*.015f,w*.294f,h*.112f,px)
        p.color=C(240,193,48); c.drawCircle(w*.078f,h*.058f,w*.036f,p)
        p.color=C(95,56,29); c.drawCircle(w*.068f,h*.055f,w*.020f,p)
        fill(c,w*.052f,h*.067f,w*.087f,h*.074f,C(95,56,29))
        text.textAlign=Paint.Align.LEFT
        label(c,"APE",w*.118f,h*.052f,w*.038f,C(82,49,27),true)
        label(c,"ECONOMICS",w*.112f,h*.083f,w*.026f,C(82,49,27),false)
        label(c,"POWER • BANANAS • PAYS",w*.112f,h*.101f,w*.0105f,C(126,93,54),false)
        text.textAlign=Paint.Align.CENTER

        hudPanel(c,w*.310f,h*.015f,w*.553f,h*.112f,px)
        p.style=Paint.Style.STROKE; p.strokeWidth=px*4; p.color=C(239,190,45)
        c.drawArc(RectF(w*.330f,h*.035f,w*.375f,h*.083f),15f,145f,false,p)
        p.style=Paint.Style.FILL
        label(c,"$bananas",w*.447f,h*.060f,w*.031f,C(62,55,42),false)
        label(c,"+12 / MIN",w*.447f,h*.088f,w*.015f,C(67,127,62),false)

        hudPanel(c,w*.568f,h*.015f,w*.758f,h*.112f,px)
        label(c,"♥",w*.605f,h*.061f,w*.033f,C(190,55,61),false)
        label(c,"50",w*.675f,h*.061f,w*.027f,C(62,55,42),false)
        label(c,"ITEM",w*.606f,h*.091f,w*.012f,C(118,91,57),false)
        label(c,"1",w*.690f,h*.093f,w*.018f,C(62,55,42),false)

        hudPanel(c,w*.772f,h*.015f,w*.982f,h*.112f,px)
        label(c,"APES  3/10",w*.877f,h*.044f,w*.0165f,C(62,55,42),false)
        label(c,"TAG $day",w*.877f,h*.071f,w*.0165f,C(62,55,42),false)
        val sec=max(0,(30_000L-(now-dayStart)).toInt())/1000
        label(c,"SUN  ${sec}s",w*.877f,h*.098f,w*.015f,C(176,115,38),false)
    }

    private fun drawBottomBar(c: Canvas, w: Float, h: Float, px: Float) {
        fill(c,0f,h*.895f,w,h,C(56,40,29))
        fill(c,0f,h*.895f,w,h*.905f,C(31,88,49))
        button(c,w*.045f,h*.918f,w*.322f,h*.978f,"APES",C(58,139,70),px)
        button(c,w*.361f,h*.918f,w*.640f,h*.978f,"BUILD",C(151,96,50),px)
        button(c,w*.680f,h*.912f,w*.955f,h*.980f,"+10s",C(45,137,201),px)
        drawMiniApe(c,w*.082f,h*.949f,w*.018f,px)
        drawHammer(c,w*.397f,h*.948f,w*.018f,px)
        label(c,">>",w*.718f,h*.957f,w*.022f,Color.WHITE,true)
    }

    private fun drawEvent(c: Canvas, w: Float, h: Float, px: Float) {
        p.color=Color.argb(210,18,24,21); c.drawRect(0f,0f,w,h,p)
        fill(c,w*.085f,h*.290f,w*.915f,h*.680f,C(74,49,30))
        fill(c,w*.098f,h*.303f,w*.902f,h*.667f,C(244,232,190))
        fill(c,w*.115f,h*.320f,w*.885f,h*.355f,C(214,181,116))
        label(c,"DORF-EREIGNIS",w*.5f,h*.395f,w*.037f,C(83,50,29),true)
        label(c,"Eine Entscheidung ist fällig.",w*.5f,h*.445f,w*.021f,C(67,58,44),false)
        label(c,"Wähle das kleinere Übel:",w*.5f,h*.477f,w*.017f,C(111,87,55),false)
        button(c,w*.165f,h*.515f,w*.835f,h*.574f,"VORRÄTE VERTEILEN   -20",C(64,137,70),px)
        button(c,w*.165f,h*.595f,w*.835f,h*.654f,"GESCHÄFT MACHEN   +30",C(164,103,48),px)
    }

    private fun drawTree(c: Canvas, x: Float, y: Float, s: Float, seed: Int) {
        fill(c,x-s*.10f,y+s*.22f,x+s*.10f,y+s*.82f,C(94,57,31))
        fill(c,x-s*.16f,y+s*.45f,x+s*.16f,y+s*.66f,C(117,72,36))
        val dark=C(27,104,48); val mid=C(45,139,57); val light=C(82,170,68)
        p.color=dark; c.drawCircle(x,y,s*.54f,p)
        c.drawCircle(x-s*.32f,y+s*.03f,s*.36f,p)
        c.drawCircle(x+s*.30f,y+s*.06f,s*.35f,p)
        p.color=mid; c.drawCircle(x-s*.12f,y-s*.22f,s*.40f,p)
        c.drawCircle(x+s*.22f,y-s*.13f,s*.33f,p)
        p.color=light; c.drawCircle(x-s*.25f,y-s*.29f,s*.14f,p)
        c.drawCircle(x+s*.08f,y-s*.31f,s*.11f,p)
        if(seed%3==0){p.color=C(235,186,46);c.drawCircle(x+s*.25f,y+s*.02f,s*.06f,p)}
    }

    private fun drawApe(c: Canvas,x:Float,y:Float,s:Float,shirt:Int,now:Long,index:Int,working:Boolean) {
        val step=((now/180L+index)%2L).toInt()
        val bob=if(step==1) s*.05f else 0f
        val yy=y+bob
        p.color=Color.argb(70,27,64,33); c.drawOval(RectF(x-s*.45f,yy+s*.69f,x+s*.45f,yy+s*.90f),p)
        p.color=C(84,49,27); c.drawCircle(x-s*.34f,yy-s*.13f,s*.18f,p); c.drawCircle(x+s*.34f,yy-s*.13f,s*.18f,p)
        fill(c,x-s*.34f,yy-s*.50f,x+s*.34f,yy+s*.10f,C(82,48,27))
        fill(c,x-s*.25f,yy-s*.28f,x+s*.25f,yy+s*.04f,C(204,137,72))
        fill(c,x-s*.18f,yy-s*.05f,x+s*.18f,yy+s*.12f,C(225,165,96))
        fill(c,x-s*.14f,yy-s*.20f,x-s*.07f,yy-s*.12f,C(33,29,24))
        fill(c,x+s*.07f,yy-s*.20f,x+s*.14f,yy-s*.12f,C(33,29,24))
        fill(c,x-s*.30f,yy+s*.11f,x+s*.30f,yy+s*.60f,shirt)
        fill(c,x-s*.22f,yy+s*.20f,x+s*.22f,yy+s*.31f,lighten(shirt,24))
        val d=if(step==1) s*.10f else 0f
        fill(c,x-s*.25f-d,yy+s*.58f,x-s*.05f-d,yy+s*.88f,C(76,45,28))
        fill(c,x+s*.05f+d,yy+s*.58f,x+s*.25f+d,yy+s*.88f,C(76,45,28))
        if(working){
            p.color=Color.WHITE;c.drawCircle(x,yy-s*.72f,s*.34f,p)
            fill(c,x-s*.05f,yy-s*.79f,x+s*.05f,yy-s*.61f,C(70,57,40))
            fill(c,x-s*.05f,yy-s*.57f,x+s*.05f,yy-s*.52f,C(70,57,40))
        }
    }

    private fun drawBananaPlant(c: Canvas,x:Float,y:Float,s:Float,px:Float) {
        fill(c,x-px,y,x+px,y+s*.75f,C(61,116,44))
        p.color=C(37,126,49)
        c.drawOval(RectF(x-s*.85f,y-s*.25f,x-s*.02f,y+s*.25f),p)
        c.drawOval(RectF(x+s*.02f,y-s*.25f,x+s*.85f,y+s*.25f),p)
        c.drawOval(RectF(x-s*.58f,y+s*.12f,x,y+s*.55f),p)
        c.drawOval(RectF(x,y+s*.12f,x+s*.58f,y+s*.55f),p)
        p.color=C(244,196,46); c.drawCircle(x+s*.12f,y+s*.43f,s*.12f,p)
        c.drawCircle(x+s*.20f,y+s*.49f,s*.10f,p)
    }

    private fun drawBush(c:Canvas,x:Float,y:Float,s:Float,seed:Int){
        p.color=C(27,112,47);c.drawCircle(x-s*.35f,y,s*.55f,p);c.drawCircle(x+s*.30f,y,s*.55f,p)
        p.color=C(65,157,59);c.drawCircle(x,y-s*.25f,s*.50f,p)
        if(seed%2==0){p.color=C(219,67,64);c.drawCircle(x+s*.24f,y-s*.20f,s*.09f,p)}
    }

    private fun drawFlower(c:Canvas,x:Float,y:Float,s:Float,seed:Int){
        fill(c,x-s*.08f,y,x+s*.08f,y+s*.9f,C(45,126,48))
        p.color=when(seed%4){0->Color.WHITE;1->C(242,193,48);2->C(232,99,108);else->C(133,100,190)}
        c.drawCircle(x,y,s*.32f,p)
        p.color=C(236,177,49);c.drawCircle(x,y,s*.10f,p)
    }

    private fun drawFence(c:Canvas,l:Float,tt:Float,r:Float,b:Float,px:Float){
        val wood=C(116,76,39); val hi=C(163,109,51)
        var x=l
        while(x<=r){
            fill(c,x,tt-px*3,x+px*4,b+px*3,wood)
            fill(c,x+px,tt-px*2,x+px*2,b+px*2,hi)
            x+=px*19
        }
        fill(c,l,tt+px*2,r,tt+px*6,wood);fill(c,l,b-px*6,r,b-px*2,wood)
        fill(c,l,tt+px*2,r,tt+px*3,hi);fill(c,l,b-px*6,r,b-px*5,hi)
    }

    private fun drawCrate(c:Canvas,x:Float,y:Float,s:Float,px:Float){
        fill(c,x-s/2,y-s/2,x+s/2,y+s/2,C(108,66,34))
        fill(c,x-s*.42f,y-s*.40f,x+s*.42f,y+s*.40f,C(174,111,49))
        fill(c,x-s*.42f,y-px,x+s*.42f,y+px,C(108,66,34))
        fill(c,x-px,y-s*.40f,x+px,y+s*.40f,C(108,66,34))
    }

    private fun drawSign(c:Canvas,x:Float,y:Float,px:Float){
        fill(c,x-px*2,y,x+px*2,y+px*18,C(91,56,31))
        fill(c,x-px*13,y-px*8,x+px*13,y+px*4,C(102,62,34))
        fill(c,x-px*11,y-px*6,x+px*11,y+px*2,C(211,154,73))
        label(c,"→",x,y+px,wUnit()*0.015f,C(69,49,31),true)
    }

    private fun drawLamp(c:Canvas,x:Float,y:Float,px:Float){
        fill(c,x-px,y,x+px,y+px*15,C(55,55,48))
        fill(c,x-px*3,y-px*4,x+px*3,y+px*2,C(54,49,40))
        fill(c,x-px*2,y-px*3,x+px*2,y+px,C(242,202,89))
    }

    private fun drawMiniApe(c:Canvas,x:Float,y:Float,s:Float,px:Float){
        p.color=C(84,49,27);c.drawCircle(x,y-s*.18f,s*.45f,p)
        fill(c,x-s*.30f,y+s*.17f,x+s*.30f,y+s*.72f,C(218,143,70))
        fill(c,x-s*.18f,y-s*.10f,x+s*.18f,y+s*.08f,C(222,159,91))
    }

    private fun drawHammer(c:Canvas,x:Float,y:Float,s:Float,px:Float){
        fill(c,x-px*2,y-s*.25f,x+px*2,y+s*.62f,C(115,73,37))
        fill(c,x-s*.42f,y-s*.45f,x+s*.42f,y-s*.15f,C(91,93,90))
        fill(c,x-s*.30f,y-s*.38f,x+s*.30f,y-s*.23f,C(172,173,163))
    }

    private fun window(c:Canvas,x:Float,y:Float,w:Float,h:Float,px:Float){
        fill(c,x-px*2,y-px*2,x+w+px*2,y+h+px*2,C(72,52,35))
        fill(c,x,y,x+w,y+h,C(88,157,179))
        fill(c,x+w/2-px/2,y,x+w/2+px/2,y+h,C(229,211,158))
        fill(c,x,y+h/2-px/2,x+w,y+h/2+px/2,C(229,211,158))
        fill(c,x+px,y+px,x+w*.42f,y+h*.40f,C(170,221,221))
    }

    private fun plaque(c:Canvas,cx:Float,cy:Float,w:Float,h:Float,s:String,px:Float){
        fill(c,cx-w/2-px*2,cy-h/2-px*2,cx+w/2+px*2,cy+h/2+px*2,C(78,50,30))
        fill(c,cx-w/2,cy-h/2,cx+w/2,cy+h/2,C(238,215,157))
        fill(c,cx-w*.44f,cy-h*.32f,cx+w*.44f,cy-h*.22f,C(206,175,111))
        label(c,s,cx,cy+h*.20f,min(w*.12f,h*.62f),C(67,47,31),true)
    }

    private fun hudPanel(c:Canvas,l:Float,tt:Float,r:Float,b:Float,px:Float){
        fill(c,l-px*2,tt-px*2,r+px*2,b+px*2,C(73,48,30))
        fill(c,l,tt,r,b,C(244,234,198))
        fill(c,l+px*3,tt+px*3,r-px*3,tt+px*6,C(218,192,133))
        fill(c,l+px*3,b-px*5,r-px*3,b-px*3,C(221,204,160))
    }

    private fun button(c:Canvas,l:Float,tt:Float,r:Float,b:Float,s:String,col:Int,px:Float){
        fill(c,l-px*3,tt-px*3,r+px*3,b+px*3,C(31,29,24))
        fill(c,l,tt,r,b,col)
        fill(c,l+px*3,tt+px*3,r-px*3,tt+px*7,lighten(col,28))
        fill(c,l+px*3,b-px*6,r-px*3,b-px*3,darken(col,24))
        label(c,s,(l+r)/2,(tt+b)/2+width*.008f,width*.024f,Color.WHITE,true)
    }

    private fun label(c:Canvas,s:String,x:Float,y:Float,size:Float,col:Int,outline:Boolean){
        text.textSize=max(8f,size)
        if(outline){
            text.style=Paint.Style.STROKE
            text.strokeWidth=max(2f,size*.13f)
            text.color=C(55,42,29)
            c.drawText(s,x,y,text)
        }
        text.style=Paint.Style.FILL
        text.color=col
        c.drawText(s,x,y,text)
    }

    private fun fill(c:Canvas,l:Float,t:Float,r:Float,b:Float,col:Int){
        p.style=Paint.Style.FILL
        p.color=col
        c.drawRect(l,t,r,b,p)
    }

    private fun C(r:Int,g:Int,b:Int)=Color.rgb(r,g,b)
    private fun lerp(a:Float,b:Float,f:Float)=a+(b-a)*f
    private fun wUnit()=width.toFloat()
    private fun lighten(col:Int,amount:Int)=Color.rgb(
        min(255,Color.red(col)+amount),min(255,Color.green(col)+amount),min(255,Color.blue(col)+amount)
    )
    private fun darken(col:Int,amount:Int)=Color.rgb(
        max(0,Color.red(col)-amount),max(0,Color.green(col)-amount),max(0,Color.blue(col)-amount)
    )

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if(e.action != MotionEvent.ACTION_UP) return true
        val x=e.x/width
        val y=e.y/height
        if(eventOpen && y in .50f..68f){
            eventOpen=false
            if(y<.588f) bananas=max(0,bananas-20) else bananas+=30
            dayStart=System.currentTimeMillis()
            return true
        }
        if(y>.895f && x>.66f) dayStart-=10_000L
        return true
    }
}
