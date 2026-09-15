package com.eugi.apeeconomics

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.*
import kotlin.random.Random

class LiveVillageGameView(context: Context) : View(context) {
    private enum class State { IDLE, TO_JOB, WORK, HOME }
    private data class Ape(val name:String,val rating:Int,val job:String,val art:Int,var state:State=State.IDLE,var x:Float=.5f,var y:Float=.72f,var sx:Float=.5f,var sy:Float=.72f,var tx:Float=.5f,var ty:Float=.72f,var start:Long=0,var end:Long=0,var next:Long=0)
    private data class Pop(val text:String,val x:Float,val y:Float,val born:Long,val bad:Boolean=false)
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private val stroke=Paint(Paint.ANTI_ALIAS_FLAG).apply{style=Paint.Style.STROKE;strokeCap=Paint.Cap.ROUND}
    private val text=Paint(Paint.ANTI_ALIAS_FLAG).apply{typeface=Typeface.DEFAULT_BOLD}
    private val rng=Random(42)
    private val bitmaps=HashMap<Int,Bitmap>()
    private val logo by lazy { BitmapFactory.decodeResource(resources,R.drawable.ape_logo) }
    private var bananas=100; private var hearts=50; private var influence=0; private var day=1
    private var dayStart=System.currentTimeMillis(); private val dayMs=30000L
    private var event=false; private var eventType=0
    private val pops=mutableListOf<Pop>()
    private val apes=mutableListOf(
        Ape("Malo",7,"Plantage",R.drawable.ape_worker,x=.22f,y=.76f,next=System.currentTimeMillis()+500),
        Ape("Poli",6,"Parlament",R.drawable.ape_politician,x=.50f,y=.76f,next=System.currentTimeMillis()+1300),
        Ape("Dubi",3,"Schwarzmarkt",R.drawable.ape_trader,x=.76f,y=.76f,next=System.currentTimeMillis()+2200)
    )
    init { keepScreenOn=true }
    fun handleBack():Boolean { if(event){event=false;invalidate();return true};return false }

    override fun onDraw(c:Canvas){ super.onDraw(c); if(width<2)return; val now=System.currentTimeMillis(); update(now); drawScene(c,now); drawHud(c,now); if(event)drawEvent(c); postInvalidateOnAnimation() }

    private fun update(now:Long){
        if(!event){
            apes.forEachIndexed { i,a -> when(a.state){
                State.IDLE -> if(now>=a.next) move(a,job(a.job),now,State.TO_JOB)
                State.TO_JOB,State.HOME -> { val d=max(1L,a.end-a.start);val t=((now-a.start).toFloat()/d).coerceIn(0f,1f);a.x=a.sx+(a.tx-a.sx)*t;a.y=a.sy+(a.ty-a.sy)*t;if(t>=1f){if(a.state==State.TO_JOB){a.state=State.WORK;a.end=now+3500L+a.rating*100}else{a.state=State.IDLE;a.next=now+1000L+rng.nextLong(1800L)}} }
                State.WORK -> if(now>=a.end){ earn(a,now);move(a,home(i),now,State.HOME) }
            }}
            if(now-dayStart>=dayMs){day++;dayStart=now;if(day%3==0){eventType++;event=true}}
        }
        pops.removeAll{now-it.born>1600}
    }
    private fun move(a:Ape,d:Pair<Float,Float>,now:Long,state:State){a.sx=a.x;a.sy=a.y;a.tx=d.first;a.ty=d.second;a.start=now;a.end=now+1700L;a.state=state}
    private fun job(j:String)=when(j){"Plantage"->.22f to .43f;"Parlament"->.50f to .43f;else->.78f to .67f}
    private fun home(i:Int)=listOf(.22f to .76f,.50f to .76f,.76f to .76f)[i]
    private fun earn(a:Ape,now:Long){when(a.job){"Plantage"->{val v=a.rating+3;bananas+=v;pops+=Pop("+$v 🍌",a.x,a.y-.06f,now)};"Parlament"->{influence++;pops+=Pop("+1 📣",a.x,a.y-.06f,now)};else->{val r=rng.nextInt(100);if(r<65){val v=a.rating*6;bananas+=v;pops+=Pop("DEAL +$v 🍌",a.x,a.y-.06f,now)}else{val v=a.rating*2;bananas=max(0,bananas-v);pops+=Pop("DEAL -$v 🍌",a.x,a.y-.06f,now,true)}}}}

    private fun drawScene(c:Canvas,now:Long){val w=width.toFloat();val h=height.toFloat();p.color=Color.rgb(117,191,213);c.drawRect(0f,0f,w,h,p);p.color=Color.rgb(105,181,79);c.drawRect(0f,h*.15f,w,h*.87f,p)
        for(i in 0..10){p.color=if(i%2==0)Color.rgb(63,140,61) else Color.rgb(78,156,67);c.drawCircle(w*.025f,h*(.18f+i*.065f),w*.065f,p);c.drawCircle(w*.975f,h*(.20f+i*.063f),w*.065f,p)}
        stroke.color=Color.rgb(210,174,101);stroke.strokeWidth=w*.045f;c.drawLine(w*.5f,h*.31f,w*.5f,h*.76f,stroke);c.drawLine(w*.20f,h*.49f,w*.80f,h*.49f,stroke);c.drawLine(w*.22f,h*.72f,w*.78f,h*.72f,stroke)
        building(c,"PLANTAGE",.08f,.24f,.34f,.43f,Color.rgb(232,191,67),"🍌");building(c,"PARLAMENT",.37f,.23f,.63f,.43f,Color.rgb(225,223,201),"🏛");building(c,"BANK",.68f,.27f,.91f,.44f,Color.rgb(210,180,104),"$");building(c,"MARKT",.68f,.59f,.91f,.74f,Color.rgb(214,111,76),"▦")
        val river=Path();river.moveTo(0f,h*.56f);river.cubicTo(w*.28f,h*.50f,w*.64f,h*.63f,w,h*.55f);stroke.color=Color.rgb(72,176,216);stroke.strokeWidth=w*.105f;c.drawPath(river,stroke);stroke.color=Color.rgb(139,225,241);stroke.strokeWidth=w*.07f;c.drawPath(river,stroke)
        apes.sortedBy{it.y}.forEach{drawApe(c,it,now)}
        pops.forEach{val age=(now-it.born)/1600f;label(c,it.text,w*it.x,h*(it.y-age*.06f),w*.031f,if(it.bad)Color.rgb(255,75,60) else Color.WHITE,Paint.Align.CENTER,true)}
    }
    private fun building(c:Canvas,n:String,l:Float,t:Float,r:Float,b:Float,col:Int,sym:String){val w=width.toFloat();val h=height.toFloat();val q=RectF(w*l,h*t,w*r,h*b);p.color=col;c.drawRoundRect(q,18f,18f,p);p.color=Color.rgb(116,75,42);val roof=Path();roof.moveTo(q.left-w*.02f,q.top+h*.04f);roof.lineTo(q.centerX(),q.top-h*.045f);roof.lineTo(q.right+w*.02f,q.top+h*.04f);roof.close();c.drawPath(roof,p);label(c,sym,q.centerX(),q.centerY(),w*.052f,Color.WHITE,Paint.Align.CENTER,true);p.color=Color.rgb(82,57,39);c.drawRect(q.left,q.bottom-h*.035f,q.right,q.bottom,p);label(c,n,q.centerX(),q.bottom-h*.011f,w*.017f,Color.WHITE,Paint.Align.CENTER,true)}
    private fun drawApe(c:Canvas,a:Ape,now:Long){val w=width.toFloat();val h=height.toFloat();val walking=a.state==State.TO_JOB||a.state==State.HOME;val bob=if(walking)abs(sin(now/120.0)).toFloat()*h*.006f else if(a.state==State.WORK)sin(now/180.0).toFloat()*h*.003f else 0f;val z=w*.15f;val x=w*a.x;val y=h*a.y+bob;p.color=Color.argb(70,30,20,15);c.drawOval(RectF(x-z*.38f,y+z*.30f,x+z*.38f,y+z*.47f),p);val bm=bitmaps.getOrPut(a.art){BitmapFactory.decodeResource(resources,a.art)};c.drawBitmap(bm,null,RectF(x-z*.5f,y-z*.5f,x+z*.5f,y+z*.5f),p);label(c,"${tier(a.rating)}${a.rating}",x-z*.34f,y-z*.35f,w*.021f,Color.WHITE,Paint.Align.CENTER,true);if(a.state==State.WORK)label(c,"ARBEIT...",x,y-z*.58f,w*.017f,Color.WHITE,Paint.Align.CENTER,true)}

    private fun drawHud(c:Canvas,now:Long){val w=width.toFloat();val h=height.toFloat();p.color=Color.argb(235,31,40,42);c.drawRect(0f,0f,w,h*.15f,p);c.drawBitmap(logo,null,RectF(w*.015f,h*.012f,w*.24f,h*.13f),p);chip(c,.27f,"🍌 $bananas","AUTO");chip(c,.51f,"❤️ $hearts","📣 $influence");chip(c,.75f,"🐒 3/10","TAG $day");val left=((dayMs-(now-dayStart)).coerceAtLeast(0)/1000);label(c,"Das Dorf läuft • nächster Tag in ${left}s",w*.5f,h*.145f,w*.018f,Color.WHITE,Paint.Align.CENTER,true);p.color=Color.rgb(91,61,39);c.drawRect(0f,h*.87f,w,h,p);button(c,.05f,.89f,.31f,.96f,"🐒 AFFEN");button(c,.37f,.89f,.63f,.96f,"🏗 BAUEN");button(c,.69f,.89f,.95f,.96f,"⚡ +10s")}
    private fun chip(c:Canvas,x:Float,a:String,b:String){val w=width.toFloat();val h=height.toFloat();p.color=Color.rgb(247,244,222);c.drawRoundRect(RectF(w*x,h*.025f,w*(x+.21f),h*.105f),15f,15f,p);label(c,a,w*(x+.105f),h*.060f,w*.026f,Color.rgb(35,39,38),Paint.Align.CENTER,false);label(c,b,w*(x+.105f),h*.089f,w*.015f,Color.DKGRAY,Paint.Align.CENTER,false)}
    private fun button(c:Canvas,l:Float,t:Float,r:Float,b:Float,v:String){val w=width.toFloat();val h=height.toFloat();p.color=Color.rgb(70,142,191);c.drawRoundRect(RectF(w*l,h*t,w*r,h*b),18f,18f,p);label(c,v,w*(l+r)/2,h*(t+b)/2+w*.009f,w*.022f,Color.WHITE,Paint.Align.CENTER,true)}
    private fun drawEvent(c:Canvas){val w=width.toFloat();val h=height.toFloat();p.color=Color.argb(175,20,25,22);c.drawRect(0f,0f,w,h,p);p.color=Color.rgb(251,230,173);c.drawRoundRect(RectF(w*.07f,h*.25f,w*.93f,h*.72f),28f,28f,p);val title=if(eventType%2==1)"STREIK!" else "DUBIOSER DEAL";val sub=if(eventType%2==1)"Die Arbeiter verlangen mehr Bananen." else "Ein Koffer. Keine Fragen. 65% Chance.";label(c,title,w*.5f,h*.32f,w*.045f,Color.rgb(72,47,29),Paint.Align.CENTER,false);label(c,sub,w*.5f,h*.37f,w*.020f,Color.DKGRAY,Paint.Align.CENTER,false);eventButton(c,.42f,"Nachgeben",if(eventType%2==1)"🍌 -30  ❤️ +8" else "65%: 🍌 +90");eventButton(c,.51f,"Verhandeln","📣 +4");eventButton(c,.60f,"Hart bleiben","👊 +8  ❤️ -6")}
    private fun eventButton(c:Canvas,y:Float,a:String,b:String){val w=width.toFloat();val h=height.toFloat();p.color=Color.rgb(171,210,128);c.drawRoundRect(RectF(w*.13f,h*y,w*.87f,h*(y+.065f)),16f,16f,p);label(c,a,w*.17f,h*(y+.028f),w*.022f,Color.rgb(55,40,28),Paint.Align.LEFT,false);label(c,b,w*.17f,h*(y+.052f),w*.017f,Color.DKGRAY,Paint.Align.LEFT,false)}
    private fun tier(r:Int)=when(r){10->"S";9->"A";7,8->"B";4,5,6->"C";else->"D"}
    private fun label(c:Canvas,v:String,x:Float,y:Float,z:Float,col:Int,align:Paint.Align,out:Boolean){text.textSize=z;text.textAlign=align;text.typeface=Typeface.DEFAULT_BOLD;if(out){text.style=Paint.Style.STROKE;text.strokeWidth=max(2f,z*.11f);text.color=Color.rgb(55,43,32);c.drawText(v,x,y,text)};text.style=Paint.Style.FILL;text.color=col;c.drawText(v,x,y,text)}

    override fun onTouchEvent(e:MotionEvent):Boolean{if(e.actionMasked!=MotionEvent.ACTION_DOWN)return true;val x=e.x/width;val y=e.y/height;if(event){if(x in .13f.. .87f){when{y in .42f.. .485f->{if(eventType%2==1){bananas=max(0,bananas-30);hearts=min(100,hearts+8)}else if(rng.nextInt(100)<65)bananas+=90 else bananas=max(0,bananas-45);event=false};y in .51f.. .575f->{influence=min(100,influence+4);event=false};y in .60f.. .665f->{hearts=max(0,hearts-6);event=false}}};return true};if(y>.87f&&x>.67f){dayStart-=10000L;apes.forEach{if(it.state==State.WORK)it.end-=10000L};return true};return true}
}
