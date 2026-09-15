package com.eugi.apeeconomics

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.*
import kotlin.random.Random

class LiveVillageGameView(context: Context) : View(context) {
    private enum class State { IDLE, TO_JOB, WORK, HOME }
    private data class Ape(val name:String,val role:String,val rating:Int,val job:String,var state:State=State.IDLE,var x:Float=.5f,var y:Float=.72f,var sx:Float=.5f,var sy:Float=.72f,var tx:Float=.5f,var ty:Float=.72f,var start:Long=0,var end:Long=0,var next:Long=0)
    private data class Pop(val text:String,val x:Float,val y:Float,val born:Long)
    private val p=Paint().apply{isAntiAlias=false}
    private val t=Paint().apply{isAntiAlias=false;typeface=Typeface.create(Typeface.MONOSPACE,Typeface.BOLD)}
    private val rng=Random(42)
    private val apes=mutableListOf(
        Ape("Malo","WORKER",7,"PLANTAGE",x=.24f,y=.72f),
        Ape("Poli","POLITICIAN",6,"PARLAMENT",x=.50f,y=.72f),
        Ape("Dubi","TRADER",3,"BANK",x=.76f,y=.72f)
    )
    private val pops=mutableListOf<Pop>()
    private var bananas=100; private var hearts=50; private var influence=1; private var day=1
    private var dayStart=System.currentTimeMillis(); private var event=false; private var eventTitle=""
    private var church=false; private var jordell=false
    private val dayMs=30000L

    init { keepScreenOn=true; apes.forEachIndexed{i,a->a.next=System.currentTimeMillis()+700L+i*500L} }
    fun handleBack():Boolean { if(event){event=false;invalidate();return true}; return false }

    override fun onDraw(c:Canvas){ super.onDraw(c); val now=System.currentTimeMillis(); update(now); drawPixelWorld(c,now); if(event) drawEvent(c); postInvalidateOnAnimation() }

    private fun update(now:Long){
        if(!event && now-dayStart>=dayMs){ day++; dayStart=now; if(day%3==0){event=true;eventTitle=listOf("ERNTEDRUCK!","WAHLKAMPF!","DUBIOSER DEAL!")[day/3%3]} }
        if(event)return
        apes.forEachIndexed{i,a-> when(a.state){
            State.IDLE->if(now>=a.next){ val q=job(a.job); move(a,q.first,q.second,now,1500L);a.state=State.TO_JOB }
            State.TO_JOB->if(now>=a.end){a.x=a.tx;a.y=a.ty;a.state=State.WORK;a.start=now;a.end=now+2200L}
            State.WORK->if(now>=a.end){ val gain=when(a.role){"WORKER"->10;"POLITICIAN"->2;else->if(rng.nextFloat()<.72f)18 else -8}; bananas=max(0,bananas+gain); if(a.role=="POLITICIAN") influence++; pops+=Pop(if(gain>=0) "+$gain 🍌" else "$gain 🍌",a.x,a.y,now); val home=listOf(.23f,.50f,.76f)[i] to .72f;move(a,home.first,home.second,now,1500L);a.state=State.HOME }
            State.HOME->if(now>=a.end){a.x=a.tx;a.y=a.ty;a.state=State.IDLE;a.next=now+900L+rng.nextLong(900L)}
        }}
        apes.forEach{a->if(a.state==State.TO_JOB||a.state==State.HOME){val f=((now-a.start).toFloat()/(a.end-a.start)).coerceIn(0f,1f);a.x=a.sx+(a.tx-a.sx)*f;a.y=a.sy+(a.ty-a.sy)*f}}
        pops.removeAll{now-it.born>1300L}
    }
    private fun move(a:Ape,x:Float,y:Float,n:Long,d:Long){a.sx=a.x;a.sy=a.y;a.tx=x;a.ty=y;a.start=n;a.end=n+d}
    private fun job(s:String)=when(s){"PLANTAGE"->.23f to .37f;"PARLAMENT"->.50f to .38f;else->.77f to .39f}

    private fun drawPixelWorld(c:Canvas,now:Long){
        val w=width.toFloat();val h=height.toFloat(); val hud=h*.145f;val bar=h*.895f
        p.color=Color.rgb(42,54,58);c.drawRect(0f,0f,w,hud,p)
        // parchment HUD
        panel(c,.02f*w,.018f*h,.28f*w,.105f*h,Color.rgb(241,225,174)); label(c,"APE",.15f*w,.052f*h,.042f*w,Color.rgb(91,54,24));label(c,"ECONOMICS",.15f*w,.087f*h,.036f*w,Color.rgb(91,54,24));
        panel(c,.31f*w,.018f*h,.27f*w,.105f*h,Color.rgb(246,239,205)); label(c,"🍌 $bananas",.445f*w,.058f*h,.034f*w,Color.rgb(38,36,30));label(c,"AUTO",.445f*w,.091f*h,.018f*w,Color.rgb(90,72,40))
        panel(c,.59f*w,.018f*h,.18f*w,.105f*h,Color.rgb(246,239,205));label(c,"♥ $hearts",.68f*w,.058f*h,.032f*w,Color.rgb(157,42,55));label(c,"📣 $influence",.68f*w,.091f*h,.018f*w,Color.DKGRAY)
        panel(c,.78f*w,.018f*h,.20f*w,.105f*h,Color.rgb(246,239,205));label(c,"🐒 3/10",.88f*w,.052f*h,.027f*w,Color.DKGRAY);label(c,"TAG $day",.88f*w,.082f*h,.023f*w,Color.DKGRAY);label(c,"☀ ${(dayMs-(now-dayStart)).coerceAtLeast(0)/1000}s",.88f*w,.108f*h,.018f*w,Color.DKGRAY)
        // grass tiles
        p.color=Color.rgb(105,183,67);c.drawRect(0f,hud,w,bar,p)
        val tile=w/12f
        for(y in 0..20)for(x in 0..11){if((x*7+y*13)%11==0){p.color=Color.rgb(79,157,54);c.drawRect(x*tile+tile*.2f,hud+y*tile+tile*.25f,x*tile+tile*.28f,hud+y*tile+tile*.5f,p)}}
        // paths
        p.color=Color.rgb(222,191,118);c.drawRect(w*.46f,h*.15f,w*.54f,bar,p);c.drawRect(w*.08f,h*.47f,w*.92f,h*.53f,p);c.drawRect(w*.10f,h*.72f,w*.90f,h*.78f,p)
        // river pixel bands
        p.color=Color.rgb(42,135,204);c.drawRect(0f,h*.555f,w,h*.635f,p);p.color=Color.rgb(76,181,226);c.drawRect(0f,h*.57f,w,h*.62f,p)
        val wave=((now/180)%4).toInt();p.color=Color.rgb(181,236,244);for(x in 0..9){val xx=x*w/9f-wave*4;c.drawRect(xx,h*.584f,xx+w*.055f,h*.59f,p)}
        // bridge
        p.color=Color.rgb(119,72,34);c.drawRect(w*.445f,h*.55f,w*.555f,h*.64f,p);p.color=Color.rgb(193,130,56);for(k in 0..5)c.drawRect(w*.455f,h*(.555f+k*.014f),w*.545f,h*(.565f+k*.014f),p)
        // trees / borders
        for(i in 0..8){tree(c,w*(.025f+i*.12f),h*.17f,w*.065f);tree(c,w*(.03f+i*.12f),h*.84f,w*.06f)}
        building(c,.07f*w,.235f*h,.30f*w,.43f*h,"PLANTAGE",Color.rgb(190,126,47),"🍌")
        building(c,.35f*w,.225f*h,.64f*w,.44f*h,"PARLAMENT",Color.rgb(143,151,157),"▣")
        building(c,.69f*w,.245f*h,.94f*w,.44f*h,"BANK",Color.rgb(166,105,46),"$")
        building(c,.67f*w,.675f*h,.93f*w,.84f*h,"MARKT",Color.rgb(177,73,46),"🍌")
        if(church)building(c,.08f*w,.67f*h,.30f*w,.83f*h,"KIRCHE",Color.rgb(203,194,155),"+")
        if(jordell)building(c,.34f*w,.67f*h,.58f*w,.83f*h,"JORDELL",Color.rgb(123,83,143),"J")
        // flowers
        for(i in 0..14){val x=((i*71)%91+5)/100f*w;val y=(.18f+((i*47)%62)/100f)*h;pixelFlower(c,x,y,w*.012f)}
        apes.forEachIndexed{i,a->drawMonkey(c,a,w*a.x,h*a.y,w*.052f,now,i)}
        pops.forEach{q->val dy=(now-q.born)/1300f*h*.04f;label(c,q.text,w*q.x,h*q.y-dy,w*.024f,Color.WHITE,true)}
        // bottom bar
        p.color=Color.rgb(66,43,28);c.drawRect(0f,bar,w,h,p);button(c,.04f*w,.915f*h,.30f*w,.978f*h,"🐒 AFFEN",Color.rgb(65,143,73));button(c,.36f*w,.915f*h,.62f*w,.978f*h,"🔨 BAUEN",Color.rgb(143,95,55));button(c,.68f*w,.905f*h,.96f*w,.982f*h,"≫ +10s",Color.rgb(48,139,211))
    }

    private fun panel(c:Canvas,l:Float,top:Float,r:Float,b:Float,col:Int){p.color=Color.rgb(74,48,29);c.drawRect(l-5,top-5,r+5,b+5,p);p.color=col;c.drawRect(l,top,r,b,p);p.color=Color.rgb(220,197,143);c.drawRect(l+5,top+5,r-5,top+9,p)}
    private fun tree(c:Canvas,x:Float,y:Float,s:Float){p.color=Color.rgb(82,49,28);c.drawRect(x-s*.12f,y+s*.3f,x+s*.12f,y+s*.9f,p);p.color=Color.rgb(35,113,47);c.drawRect(x-s*.55f,y-s*.2f,x+s*.55f,y+s*.5f,p);p.color=Color.rgb(58,145,53);c.drawRect(x-s*.4f,y-s*.38f,x+s*.38f,y+s*.32f,p);p.color=Color.rgb(104,181,59);c.drawRect(x-s*.2f,y-s*.28f,x+s*.2f,y,p)}
    private fun pixelFlower(c:Canvas,x:Float,y:Float,s:Float){p.color=Color.rgb(40,126,47);c.drawRect(x-s*.15f,y,x+s*.15f,y+s,p);p.color=if(((x+y).toInt()%2)==0)Color.WHITE else Color.rgb(250,204,73);c.drawRect(x-s*.45f,y-s*.35f,x+s*.45f,y+s*.2f,p)}
    private fun building(c:Canvas,l:Float,top:Float,r:Float,b:Float,name:String,col:Int,icon:String){val ww=r-l;p.color=Color.rgb(77,49,28);c.drawRect(l-ww*.03f,top+ww*.18f,r+ww*.03f,b,p);p.color=col;c.drawRect(l,top+ww*.18f,r,b-ww*.04f,p);p.color=Color.rgb(91,54,28);val roof=Path();roof.moveTo(l-ww*.06f,top+ww*.2f);roof.lineTo((l+r)/2,top);roof.lineTo(r+ww*.06f,top+ww*.2f);roof.close();c.drawPath(roof,p);p.color=Color.rgb(48,34,24);c.drawRect((l+r)/2-ww*.09f,b-ww*.18f,(l+r)/2+ww*.09f,b,p);label(c,icon,(l+r)/2,(top+b)/2,ww*.17f,Color.WHITE,true);p.color=Color.rgb(244,225,170);c.drawRect(l+ww*.08f,b-ww*.02f,r-ww*.08f,b+ww*.12f,p);label(c,name,(l+r)/2,b+ww*.065f,ww*.075f,Color.rgb(53,39,28))}
    private fun drawMonkey(c:Canvas,a:Ape,x:Float,y:Float,s:Float,now:Long,index:Int){val walking=a.state==State.TO_JOB||a.state==State.HOME;val frame=((now/180+index)%2).toInt();val yy=y+if(walking&&frame==1)s*.06f else 0f;p.color=Color.rgb(65,45,29);c.drawRect(x-s*.32f,yy+s*.30f,x+s*.32f,yy+s*.43f,p);p.color=Color.rgb(91,54,29);c.drawRect(x-s*.34f,yy-s*.30f,x+s*.34f,yy+s*.25f,p);p.color=Color.rgb(198,132,66);c.drawRect(x-s*.24f,yy-s*.18f,x+s*.24f,yy+s*.16f,p);p.color=Color.rgb(40,31,24);c.drawRect(x-s*.14f,yy-s*.06f,x-s*.07f,yy+.01f*s,p);c.drawRect(x+s*.07f,yy-s*.06f,x+s*.14f,yy+.01f*s,p);val shirt=when(a.role){"WORKER"->Color.rgb(48,104,170);"POLITICIAN"->Color.rgb(35,55,69);else->Color.rgb(56,112,63)};p.color=shirt;c.drawRect(x-s*.29f,yy+s*.17f,x+s*.29f,yy+s*.62f,p);p.color=Color.rgb(91,54,29);val leg=if(walking&&frame==1)s*.12f else 0f;c.drawRect(x-s*.25f-leg,yy+s*.58f,x-s*.04f-leg,yy+s*.82f,p);c.drawRect(x+s*.04f+leg,yy+s*.58f,x+s*.25f+leg,yy+s*.82f,p);if(a.role=="WORKER"){p.color=Color.rgb(238,190,52);c.drawRect(x-s*.38f,yy-s*.36f,x+s*.38f,yy-s*.22f,p);c.drawRect(x-s*.24f,yy-s*.48f,x+s*.24f,yy-s*.25f,p)};if(a.state==State.WORK){label(c,if(a.role=="WORKER")"!" else if(a.role=="POLITICIAN")"📣" else "$",x,yy-s*.55f,s*.35f,Color.WHITE,true)};label(c,"${tier(a.rating)}${a.rating}",x-s*.45f,yy-s*.38f,s*.22f,Color.WHITE,true)}
    private fun tier(r:Int)=when(r){10->"S";9->"A";in 7..8->"B";in 5..6->"C";else->"D"}
    private fun button(c:Canvas,l:Float,top:Float,r:Float,b:Float,s:String,col:Int){p.color=Color.rgb(38,34,29);c.drawRect(l-5,top-5,r+5,b+5,p);p.color=col;c.drawRect(l,top,r,b,p);p.color=Color.argb(80,255,255,255);c.drawRect(l+5,top+5,r-5,top+10,p);label(c,s,(l+r)/2,(top+b)/2+width*.008f,width*.026f,Color.WHITE,true)}
    private fun label(c:Canvas,s:String,x:Float,y:Float,size:Float,col:Int,outline:Boolean=false){t.textSize=size;t.textAlign=Paint.Align.CENTER;if(outline){t.style=Paint.Style.STROKE;t.strokeWidth=max(2f,size*.13f);t.color=Color.rgb(43,33,25);c.drawText(s,x,y,t)};t.style=Paint.Style.FILL;t.color=col;c.drawText(s,x,y,t)}
    private fun drawEvent(c:Canvas){val w=width.toFloat();val h=height.toFloat();p.color=Color.argb(190,20,24,20);c.drawRect(0f,0f,w,h,p);panel(c,w*.08f,h*.28f,w*.92f,h*.70f,Color.rgb(245,226,174));label(c,eventTitle,w*.5f,h*.35f,w*.05f,Color.rgb(78,48,27));label(c,"DAS DORF BRAUCHT DEINE ENTSCHEIDUNG",w*.5f,h*.41f,w*.021f,Color.DKGRAY);button(c,w*.14f,h*.50f,w*.46f,h*.60f,"VOLK HELFEN",Color.rgb(67,139,72));button(c,w*.54f,h*.50f,w*.86f,h*.60f,"PROFIT",Color.rgb(180,119,42))}
    override fun onTouchEvent(e:MotionEvent):Boolean{if(e.action!=MotionEvent.ACTION_UP)return true;val x=e.x/width;val y=e.y/height;if(event){if(y in .48f..65f){if(x<.5f){hearts+=5;bananas=max(0,bananas-15)}else{bananas+=30;hearts=max(0,hearts-4)};event=false;dayStart=System.currentTimeMillis()};return true};if(y>.89f&&x>.66f){dayStart-=10000L};if(y>.89f&&x in .34f..66f){if(!church&&bananas>=60){bananas-=60;church=true}else if(!jordell&&bananas>=90){bananas-=90;jordell=true}};invalidate();return true}
}
