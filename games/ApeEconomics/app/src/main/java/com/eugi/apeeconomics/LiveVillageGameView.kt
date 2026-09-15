package com.eugi.apeeconomics

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.*
import kotlin.random.Random

class LiveVillageGameView(context: Context) : View(context) {
    private enum class Mode { VILLAGE, APES, EVENT }
    private enum class State { IDLE, TO_JOB, WORK, FROM_JOB }
    private data class Ape(val name:String,val role:String,val rating:Int,var job:String,val art:Int,var state:State=State.IDLE,var x:Float=.5f,var y:Float=.72f,var sx:Float=.5f,var sy:Float=.72f,var tx:Float=.5f,var ty:Float=.72f,var started:Long=0,var duration:Long=1000,var workUntil:Long=0,var nextAction:Long=0)
    private data class Pop(var text:String,var x:Float,var y:Float,var born:Long,var good:Boolean=true)
    private data class Choice(val label:String,val detail:String,val effect:()->Unit)
    private data class Event(val title:String,val text:String,val art:Int,val choices:List<Choice>)

    private val p=Paint(Paint.ANTI_ALIAS_FLAG); private val s=Paint(Paint.ANTI_ALIAS_FLAG).apply{style=Paint.Style.STROKE;strokeCap=Paint.Cap.ROUND}; private val tp=Paint(Paint.ANTI_ALIAS_FLAG)
    private val rng=Random(77); private var mode=Mode.VILLAGE; private var bananas=100; private var hearts=50; private var influence=0; private var votes=0; private var violence=0; private var day=1
    private var cycleStarted=System.currentTimeMillis(); private val cycleMs=30000L; private var selected=0; private var event:Event?=null; private var eventNo=0; private val pops=mutableListOf<Pop>(); private val bm=HashMap<Int,Bitmap>()
    private val logo by lazy{BitmapFactory.decodeResource(resources,R.drawable.ape_logo)}
    private val apes=mutableListOf(
        Ape("Malo","Arbeiter",7,"Plantage",R.drawable.ape_worker,x=.25f,y=.74f,nextAction=System.currentTimeMillis()+700),
        Ape("Poli","Redner",6,"Parlament",R.drawable.ape_politician,x=.50f,y=.75f,nextAction=System.currentTimeMillis()+1600),
        Ape("Dubi","Dubioser",3,"Schwarzmarkt",R.drawable.ape_trader,x=.73f,y=.76f,nextAction=System.currentTimeMillis()+2600)
    )
    init{keepScreenOn=true}
    fun handleBack():Boolean=if(mode!=Mode.VILLAGE){mode=Mode.VILLAGE;invalidate();true}else false

    override fun onDraw(c:Canvas){super.onDraw(c);if(width<2)return; val now=System.currentTimeMillis(); tick(now); world(c,now); hud(c,now); if(mode==Mode.APES)apePanel(c); if(mode==Mode.EVENT)eventPanel(c); postInvalidateOnAnimation()}
    private fun tick(now:Long){
        if(mode!=Mode.EVENT){
            apes.forEachIndexed{i,a->
                when(a.state){
                    State.IDLE->if(now>=a.nextAction) startMove(a,jobPos(a.job),now)
                    State.TO_JOB,State.FROM_JOB->{val t=((now-a.started).toFloat()/a.duration).coerceIn(0f,1f);a.x=lerp(a.sx,a.tx,t);a.y=lerp(a.sy,a.ty,t);if(t>=1f){if(a.state==State.TO_JOB){a.state=State.WORK;a.workUntil=now+3500L+a.rating*120}else{a.state=State.IDLE;a.nextAction=now+1200+rng.nextLong(1800)}}}
                    State.WORK->if(now>=a.workUntil){pay(a,i,now); val home=homePos(i);a.state=State.FROM_JOB;startMove(a,home,now,State.FROM_JOB)}
                }
            }
            if(now-cycleStarted>=cycleMs){day++;cycleStarted=now; if(day%3==0)openEvent()}
        }
        pops.removeAll{now-it.born>1700}
    }
    private fun startMove(a:Ape,target:Pair<Float,Float>,now:Long,state:State=State.TO_JOB){a.sx=a.x;a.sy=a.y;a.tx=target.first;a.ty=target.second;a.started=now;val dist=hypot(a.tx-a.x,a.ty-a.y);a.duration=(1200+dist*4200).toLong();a.state=state}
    private fun pay(a:Ape,i:Int,now:Long){
        when(a.job){
            "Plantage"->{val v=a.rating+3;bananas+=v;pop("+$v 🍌",a.x,a.y-.07f,now)}
            "Parlament"->{influence++;if(rng.nextInt(3)==0)votes++;pop("+1 📣",a.x,a.y-.07f,now)}
            "Bank"->{val v=a.rating+1;bananas+=v;pop("+$v 🍌",a.x,a.y-.07f,now)}
            "Schwarzmarkt"->{val r=rng.nextInt(100);if(r<65){val v=a.rating*6;bananas+=v;pop("DEAL +$v 🍌",a.x,a.y-.07f,now)}else if(r<90){val v=a.rating*3;bananas=max(0,bananas-v);pop("-$v 🍌",a.x,a.y-.07f,now,false)}else{hearts=max(0,hearts-3);pop("RAZZIA! ❤️-3",a.x,a.y-.07f,now,false)}}
        }
    }
    private fun pop(t:String,x:Float,y:Float,n:Long,g:Boolean=true){pops+=Pop(t,x,y,n,g)}
    private fun jobPos(j:String)=when(j){"Plantage"->.25f to .37f;"Parlament"->.50f to .37f;"Bank"->.76f to .40f;else->.70f to .66f}
    private fun homePos(i:Int)=listOf(.25f to .73f,.50f to .74f,.73f to .75f)[i%3]
    private fun lerp(a:Float,b:Float,t:Float)=a+(b-a)*t

    private fun world(c:Canvas,now:Long){val w=width.toFloat();val h=height.toFloat();p.color=Color.rgb(117,190,213);c.drawRect(0f,0f,w,h,p);p.color=Color.rgb(103,181,80);c.drawRect(0f,h*.17f,w,h*.86f,p)
        // trees / life
        for(i in 0..9){p.color=Color.rgb(66,139+(i%2)*18,62);c.drawCircle(w*(.03f+(i%2)*.94f),h*(.20f+i*.065f),w*.065f,p)}
        // roads
        s.color=Color.rgb(211,177,106);s.strokeWidth=w*.055f;c.drawLine(w*.5f,h*.30f,w*.5f,h*.76f,s);c.drawLine(w*.20f,h*.48f,w*.80f,h*.48f,s);c.drawLine(w*.22f,h*.70f,w*.78f,h*.70f,s)
        building(c,"PLANTAGE",.10f,.24f,.36f,.43f,Color.rgb(231,190,68),"🍌");building(c,"PARLAMENT",.38f,.23f,.64f,.43f,Color.rgb(224,222,197),"🏛");building(c,"BANK",.68f,.28f,.90f,.44f,Color.rgb(211,181,105),"$");building(c,"MARKT",.66f,.60f,.89f,.75f,Color.rgb(214,111,76),"▦")
        // river foreground
        val path=Path();path.moveTo(0f,h*.56f);path.cubicTo(w*.25f,h*.51f,w*.65f,h*.63f,w,h*.55f);s.color=Color.rgb(73,177,217);s.strokeWidth=w*.11f;c.drawPath(path,s);s.color=Color.rgb(137,224,241);s.strokeWidth=w*.075f;c.drawPath(path,s)
        apes.sortedBy{it.y}.forEachIndexed{i,a->drawApe(c,a,now)}
        pops.forEach{val age=(now-it.born)/1700f; val yy=h*(it.y-age*.055f); text(c,it.text,w*it.x,yy,w*.034f,if(it.good)Color.WHITE else Color.rgb(255,90,70),Paint.Align.CENTER,true)}
    }
    private fun building(c:Canvas,name:String,l:Float,t:Float,r:Float,b:Float,col:Int,sym:String){val w=width.toFloat(),h=height.toFloat();val q=RectF(w*l,h*t,w*r,h*b);p.color=col;c.drawRoundRect(q,18f,18f,p);p.color=Color.rgb(117,76,42);val roof=Path();roof.moveTo(q.left-w*.015f,q.top+h*.04f);roof.lineTo(q.centerX(),q.top-h*.045f);roof.lineTo(q.right+w*.015f,q.top+h*.04f);roof.close();c.drawPath(roof,p);text(c,sym,q.centerX(),q.centerY(),w*.055f,Color.WHITE,Paint.Align.CENTER,true);p.color=Color.rgb(84,58,39);c.drawRect(q.left,q.bottom-h*.035f,q.right,q.bottom,p);text(c,name,q.centerX(),q.bottom-h*.010f,w*.018f,Color.WHITE,Paint.Align.CENTER,true)}
    private fun drawApe(c:Canvas,a:Ape,now:Long){val w=width.toFloat(),h=height.toFloat();val moving=a.state==State.TO_JOB||a.state==State.FROM_JOB;val bob=if(moving)abs(sin(now/110.0))*h*.006f else if(a.state==State.WORK)sin(now/180.0).toFloat()*h*.003f else 0f;val size=w*.145f;val x=w*a.x;val y=h*a.y+bob;p.color=Color.argb(70,30,30,20);c.drawOval(RectF(x-size*.38f,y+size*.30f,x+size*.38f,y+size*.48f),p);val b=bm.getOrPut(a.art){BitmapFactory.decodeResource(resources,a.art)};c.drawBitmap(b,null,RectF(x-size*.5f,y-size*.5f,x+size*.5f,y+size*.5f),p);text(c,"${tier(a.rating)}${a.rating}",x-size*.35f,y-size*.35f,w*.021f,Color.WHITE,Paint.Align.CENTER,true);if(a.state==State.WORK){val dots=".".repeat(((now/450)%3+1).toInt());text(c,"ARBEIT$dots",x,y-size*.58f,w*.018f,Color.WHITE,Paint.Align.CENTER,true)}}

    private fun hud(c:Canvas,now:Long){val w=width.toFloat(),h=height.toFloat();p.color=Color.argb(230,30,39,42);c.drawRect(0f,0f,w,h*.15f,p);c.drawBitmap(logo,null,RectF(w*.02f,h*.012f,w*.25f,h*.13f),p);chip(c,.28f,.018f,"🍌 $bananas","läuft automatisch");chip(c,.54f,.018f,"❤️ $hearts","📣 $influence");chip(c,.80f,.018f,"🗳 $votes","👊 $violence");val remain=((cycleMs-(now-cycleStarted)).coerceAtLeast(0)/1000);text(c,"TAG $day   •   nächstes Ereignisfenster in ${remain}s",w*.50f,h*.145f,w*.019f,Color.WHITE,Paint.Align.CENTER,true)
        p.color=Color.rgb(89,61,39);c.drawRect(0f,h*.86f,w,h,p);button(c,.035f,.88f,.30f,.955f,"🐒 AFFEN",Color.rgb(76,126,79));button(c,.35f,.88f,.65f,.955f,"🏗 BAUEN",Color.rgb(122,91,62));button(c,.70f,.88f,.965f,.955f,"⚡ +10s",Color.rgb(207,148,49))
    }
    private fun chip(c:Canvas,x:Float,y:Float,a:String,b:String){val w=width.toFloat(),h=height.toFloat();p.color=Color.rgb(246,243,221);c.drawRoundRect(RectF(w*x,h*y,w*(x+.22f),h*(y+.09f)),16f,16f,p);text(c,a,w*(x+.11f),h*(y+.038f),w*.027f,Color.rgb(37,39,38),Paint.Align.CENTER,false);text(c,b,w*(x+.11f),h*(y+.071f),w*.015f,Color.rgb(85,75,55),Paint.Align.CENTER,false)}
    private fun button(c:Canvas,l:Float,t:Float,r:Float,b:Float,label:String,col:Int){val w=width.toFloat(),h=height.toFloat();p.color=col;c.drawRoundRect(RectF(w*l,h*t,w*r,h*b),18f,18f,p);text(c,label,w*(l+r)/2,h*(t+b)/2+w*.01f,w*.024f,Color.WHITE,Paint.Align.CENTER,true)}

    private fun apePanel(c:Canvas){val w=width.toFloat(),h=height.toFloat();dim(c);p.color=Color.rgb(246,225,174);c.drawRoundRect(RectF(w*.06f,h*.18f,w*.94f,h*.79f),28f,28f,p);text(c,"AFFEN • JOBS",w*.5f,h*.225f,w*.045f,Color.rgb(75,50,31),Paint.Align.CENTER,false);apes.forEachIndexed{i,a->val y=.27f+i*.14f;p.color=if(i==selected)Color.rgb(184,218,145) else Color.rgb(224,200,151);c.drawRoundRect(RectF(w*.10f,h*y,w*.90f,h*(y+.105f)),18f,18f,p);val b=bm.getOrPut(a.art){BitmapFactory.decodeResource(resources,a.art)};c.drawBitmap(b,null,RectF(w*.11f,h*(y+.008f),w*.25f,h*(y+.098f)),p);text(c,"${a.name} • ${a.role} • ${tier(a.rating)}${a.rating}",w*.28f,h*(y+.040f),w*.024f,Color.rgb(50,40,30),Paint.Align.LEFT,false);text(c,"Job: ${a.job}   ${stateName(a.state)}",w*.28f,h*(y+.077f),w*.019f,Color.DKGRAY,Paint.Align.LEFT,false)};text(c,"Antippen = auswählen • JOB wechselt Einsatzort",w*.5f,h*.72f,w*.018f,Color.DKGRAY,Paint.Align.CENTER,false);button(c,.18f,.735f,.82f,.785f,"JOB WECHSELN",Color.rgb(78,120,77))}
    private fun stateName(st:State)=when(st){State.IDLE->"💤";State.TO_JOB->"🚶→";State.WORK->"⚙ arbeitet";State.FROM_JOB->"←🚶"}
    private fun eventPanel(c:Canvas){val e=event?:return;val w=width.toFloat(),h=height.toFloat();dim(c);p.color=Color.rgb(251,230,173);c.drawRoundRect(RectF(w*.055f,h*.17f,w*.945f,h*.80f),28f,28f,p);val b=bm.getOrPut(e.art){BitmapFactory.decodeResource(resources,e.art)};c.drawBitmap(b,null,RectF(w*.09f,h*.21f,w*.34f,h*.36f),p);text(c,e.title,w*.38f,h*.245f,w*.040f,Color.rgb(62,43,28),Paint.Align.LEFT,false);text(c,e.text,w*.38f,h*.295f,w*.020f,Color.DKGRAY,Paint.Align.LEFT,false);e.choices.forEachIndexed{i,ch->val y=.41f+i*.105f;p.color=listOf(Color.rgb(169,211,126),Color.rgb(143,192,220),Color.rgb(228,143,124))[i];c.drawRoundRect(RectF(w*.10f,h*y,w*.90f,h*(y+.075f)),18f,18f,p);text(c,ch.label,w*.14f,h*(y+.030f),w*.023f,Color.rgb(55,40,28),Paint.Align.LEFT,false);text(c,ch.detail,w*.14f,h*(y+.058f),w*.018f,Color.DKGRAY,Paint.Align.LEFT,false)}}
    private fun openEvent(){eventNo++;event=if(eventNo%2==1)Event("Streik an der Plantage!","Die Arbeiter verlangen ihren Anteil.",R.drawable.ape_worker,listOf(Choice("Löhne erhöhen","🍌 -30  ❤️ +10"){bananas=max(0,bananas-30);hearts+=10},Choice("Verhandeln","📣 +4"){influence+=4},Choice("Streik brechen","👊 +8  ❤️ -8"){violence+=8;hearts-=8})) else Event("Dubioses Angebot","Ein Koffer, keine Fragen. Was könnte schiefgehen?",R.drawable.ape_trader,listOf(Choice("Deal annehmen","65% auf 🍌 +90"){if(rng.nextInt(100)<65)bananas+=90 else bananas=max(0,bananas-45)},Choice("Ablehnen","❤️ +3"){hearts+=3},Choice("Informanten kaufen","🍌 -40  📣 +8"){bananas=max(0,bananas-40);influence+=8}));mode=Mode.EVENT}
    private fun dim(c:Canvas){p.color=Color.argb(170,20,25,22);c.drawRect(0f,0f,width.toFloat(),height.toFloat(),p)}
    private fun tier(r:Int)=when(r){10->"S";9->"A";7,8->"B";4,5,6->"C";else->"D"}
    private fun text(c:Canvas,t:String,x:Float,y:Float,z:Float,col:Int,a:Paint.Align,outline:Boolean){tp.textSize=z;tp.textAlign=a;tp.typeface=Typeface.create(Typeface.DEFAULT,Typeface.BOLD);if(outline){tp.style=Paint.Style.STROKE;tp.strokeWidth=max(2f,z*.11f);tp.color=Color.rgb(55,45,35);c.drawText(t,x,y,tp)};tp.style=Paint.Style.FILL;tp.color=col;c.drawText(t,x,y,tp)}

    override fun onTouchEvent(e:MotionEvent):Boolean{if(e.actionMasked!=MotionEvent.ACTION_DOWN)return true;val x=e.x/width;val y=e.y/height;when(mode){Mode.VILLAGE->{if(y>.86f&&x<.32f){mode=Mode.APES;return true};if(y>.86f&&x>.69f){cycleStarted-=10000L;apes.forEach{if(it.state==State.WORK)it.workUntil-=10000};return true};apes.forEachIndexed{i,a->if(hypot(x-a.x,y-a.y)<.09f){selected=i;mode=Mode.APES;return true}}};Mode.APES->{apes.indices.forEach{i->val yy=.27f+i*.14f;if(x in .10f.. .90f&&y in yy..(yy+.105f)){selected=i;return true}};if(y in .735f.. .80f){val a=apes[selected];val jobs=if(a.name=="Dubi")listOf("Schwarzmarkt","Bank") else listOf("Plantage","Parlament","Bank");a.job=jobs[(jobs.indexOf(a.job).coerceAtLeast(0)+1)%jobs.size];a.state=State.IDLE;a.nextAction=System.currentTimeMillis()+400;mode=Mode.VILLAGE;return true};if(y<.18f||y>.80f)mode=Mode.VILLAGE};Mode.EVENT->{val ev=event?:return true;ev.choices.forEachIndexed{i,ch->val yy=.41f+i*.105f;if(x in .10f.. .90f&&y in yy..(yy+.075f)){ch.effect();hearts=hearts.coerceIn(0,100);influence=influence.coerceIn(0,100);mode=Mode.VILLAGE;cycleStarted=System.currentTimeMillis();return true}}}};invalidate();return true}
}
