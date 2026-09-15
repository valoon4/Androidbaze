package com.eugi.apeeconomics

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.*
import kotlin.random.Random

class PixelVillageV09(context: Context): View(context) {
    enum class S{IDLE,GO,WORK,BACK}
    data class Ape(val role:String,val rating:Int,var x:Float,var y:Float,val hx:Float,val hy:Float,val jx:Float,val jy:Float,var s:S=S.IDLE,var sx:Float=x,var sy:Float=y,var tx:Float=x,var ty:Float=y,var t0:Long=0,var t1:Long=0,var next:Long=0)
    data class Pop(val s:String,val x:Float,val y:Float,val t:Long)
    val p=Paint().apply{isAntiAlias=false}; val txt=Paint().apply{isAntiAlias=false;typeface=Typeface.create(Typeface.MONOSPACE,Typeface.BOLD)}
    val rng=Random(9); var bananas=128;var hearts=50;var influence=1;var day=1;var start=System.currentTimeMillis();val dayMs=30000L;var event=false
    val pops=mutableListOf<Pop>(); val apes=mutableListOf(
        Ape("WORKER",7,.27f,.43f,.27f,.43f,.23f,.315f),Ape("POLITICIAN",6,.52f,.45f,.52f,.45f,.50f,.325f),Ape("BANKER",3,.77f,.45f,.77f,.45f,.79f,.335f),Ape("TRADER",5,.58f,.70f,.58f,.70f,.79f,.73f))
    init{keepScreenOn=true;apes.forEachIndexed{i,a->a.next=System.currentTimeMillis()+i*650}}
    fun handleBack():Boolean{if(event){event=false;invalidate();return true};return false}
    override fun onDraw(c:Canvas){val n=System.currentTimeMillis();tick(n);world(c,n);if(event)event(c);postInvalidateOnAnimation()}
    fun tick(n:Long){if(!event&&n-start>=dayMs){day++;start=n;if(day%3==0)event=true};if(event)return;apes.forEach{a->when(a.s){S.IDLE->if(n>a.next){move(a,a.jx,a.jy,n,1800);a.s=S.GO};S.GO->if(n>a.t1){a.x=a.tx;a.y=a.ty;a.s=S.WORK;a.t0=n;a.t1=n+2400};S.WORK->if(n>a.t1){val g=when(a.role){"WORKER"->10;"POLITICIAN"->2;"BANKER"->6;else->if(rng.nextFloat()<.75f)14 else -6};bananas=max(0,bananas+g);if(a.role=="POLITICIAN")influence++;pops+=Pop((if(g>=0)"+" else "")+g+" 🍌",a.x,a.y,n);move(a,a.hx,a.hy,n,1800);a.s=S.BACK};S.BACK->if(n>a.t1){a.x=a.tx;a.y=a.ty;a.s=S.IDLE;a.next=n+800+rng.nextLong(1300)}};if(a.s==S.GO||a.s==S.BACK){val f=((n-a.t0).toFloat()/(a.t1-a.t0)).coerceIn(0f,1f);a.x=a.sx+(a.tx-a.sx)*f;a.y=a.sy+(a.ty-a.sy)*f}};pops.removeAll{n-it.t>1300}}
    fun move(a:Ape,x:Float,y:Float,n:Long,d:Long){a.sx=a.x;a.sy=a.y;a.tx=x;a.ty=y;a.t0=n;a.t1=n+d}
    fun world(c:Canvas,n:Long){val w=width.toFloat();val h=height.toFloat();val hud=h*.13f;val bottom=h*.89f
        p.color=Color.rgb(31,49,45);c.drawRect(0f,0f,w,hud,p);p.color=Color.rgb(91,56,29);c.drawRect(0f,bottom,w,h,p)
        // dense forest background
        p.color=Color.rgb(104,181,70);c.drawRect(0f,hud,w,bottom,p);for(i in 0..14){tree(c,(i*.073f-.02f)*w,hud+h*.025f,w*.055f);tree(c,(i*.073f-.02f)*w,bottom-h*.025f,w*.055f)}
        // organic sandy path network
        p.color=Color.rgb(226,197,126);c.drawRect(w*.46f,hud,w*.54f,bottom,p);c.drawRect(w*.08f,h*.43f,w*.92f,h*.49f,p);c.drawRect(w*.12f,h*.69f,w*.88f,h*.75f,p)
        // little path speckles
        p.color=Color.rgb(199,164,91);for(i in 0..55){val x=((i*37)%97)/100f*w;val y=(.14f+((i*61)%72)/100f)*h;if(i%3==0)c.drawRect(x,y,x+w*.006f,y+h*.003f,p)}
        // plantation fenced banana beds
        fence(c,w*.055f,h*.205f,w*.31f,h*.405f);for(r in 0..2)for(k in 0..2)bananaPlant(c,w*(.085f+k*.075f),h*(.27f+r*.045f),w*.035f)
        building(c,w*.095f,h*.205f,w*.30f,h*.35f,"PLANTAGE",0)
        building(c,w*.375f,h*.19f,w*.625f,h*.365f,"PARLAMENT",1)
        building(c,w*.705f,h*.215f,w*.925f,h*.375f,"BANK",2)
        // props near upper village
        flag(c,w*.65f,h*.18f);sign(c,w*.93f,h*.22f,"🍌");bush(c,w*.67f,h*.36f,w*.04f);bush(c,w*.94f,h*.40f,w*.035f)
        // river with banks and animated highlights
        p.color=Color.rgb(65,116,61);c.drawRect(0f,h*.505f,w,h*.515f,p);p.color=Color.rgb(31,125,198);c.drawRect(0f,h*.515f,w,h*.585f,p);p.color=Color.rgb(50,165,220);c.drawRect(0f,h*.525f,w,h*.572f,p);p.color=Color.rgb(190,239,242);val sh=((n/180)%5)*w*.008f;for(i in 0..8)c.drawRect(i*w*.13f-sh,h*.54f,i*w*.13f+w*.055f,h*.544f,p);p.color=Color.rgb(65,116,61);c.drawRect(0f,h*.585f,w,h*.595f,p);bridge(c,w*.455f,h*.505f,w*.545f,h*.60f)
        // lower market district
        tree(c,w*.13f,h*.64f,w*.07f);tree(c,w*.25f,h*.64f,w*.07f);bush(c,w*.59f,h*.67f,w*.045f);barrel(c,w*.66f,h*.69f,w*.025f);sign(c,w*.39f,h*.68f,"☷");building(c,w*.70f,h*.625f,w*.93f,h*.80f,"MARKT",3);crate(c,w*.94f,h*.77f,w*.035f);fence(c,w*.02f,h*.79f,w*.31f,h*.81f)
        // flowers / world detail
        for(i in 0..34){val x=((i*67)%94+3)/100f*w;val y=(.15f+((i*43)%69)/100f)*h;flower(c,x,y,w*.009f,i%3)}
        apes.sortedBy{it.y}.forEachIndexed{i,a->ape(c,a,w*a.x,h*a.y,w*.037f,n,i)};pops.forEach{q->label(c,q.s,w*q.x,h*q.y-(n-q.t)/1300f*h*.035f,w*.022f,Color.WHITE,true)}
        hud(c,n,w,h);button(c,.055f*w,.91f*h,.32f*w,.975f*h,"🐒 AFFEN",Color.rgb(54,137,66));button(c,.365f*w,.91f*h,.63f*w,.975f*h,"🔨 BAUEN",Color.rgb(137,91,51));button(c,.68f*w,.905f*h,.95f*w,.978f*h,"≫ +10s",Color.rgb(45,139,210))
    }
    fun hud(c:Canvas,n:Long,w:Float,h:Float){panel(c,.02f*w,.018f*h,.29f*w,.112f*h);label(c,"APE",.155f*w,.05f*h,.035f*w,Color.rgb(80,47,23));label(c,"🍌 ECONOMICS",.155f*w,.082f*h,.026f*w,Color.rgb(80,47,23));label(c,"POWER. BANANAS. EVERY DAY.",.155f*w,.102f*h,.011f*w,Color.rgb(80,47,23));panel(c,.31f*w,.018f*h,.55f*w,.112f*h);label(c,"🍌 $bananas",.43f*w,.055f*h,.031f*w,Color.rgb(38,34,28));label(c,"+ AUTO",.43f*w,.087f*h,.018f*w,Color.rgb(70,55,35));panel(c,.57f*w,.018f*h,.76f*w,.112f*h);label(c,"♥ $hearts",.665f*w,.052f*h,.029f*w,Color.rgb(145,38,48));label(c,"📣 $influence",.665f*w,.087f*h,.019f*w,Color.DKGRAY);panel(c,.78f*w,.018f*h,.98f*w,.112f*h);label(c,"🐒 4/10",.88f*w,.046f*h,.024f*w,Color.DKGRAY);label(c,"TAG $day",.88f*w,.074f*h,.020f*w,Color.DKGRAY);label(c,"☀ ${(dayMs-(n-start)).coerceAtLeast(0)/1000}s",.88f*w,.099f*h,.017f*w,Color.DKGRAY)}
    fun panel(c:Canvas,l:Float,t:Float,r:Float,b:Float){p.color=Color.rgb(72,46,27);c.drawRect(l-5,t-5,r+5,b+5,p);p.color=Color.rgb(244,235,195);c.drawRect(l,t,r,b,p);p.color=Color.rgb(217,194,137);c.drawRect(l+5,t+5,r-5,t+9,p)}
    fun building(c:Canvas,l:Float,t:Float,r:Float,b:Float,name:String,type:Int){val ww=r-l;val wall=when(type){1->Color.rgb(185,184,164);2->Color.rgb(181,119,55);3->Color.rgb(188,107,54);else->Color.rgb(192,132,52)};p.color=Color.rgb(78,49,28);c.drawRect(l-4,t+ww*.15f,r+4,b,p);p.color=wall;c.drawRect(l,t+ww*.16f,r,b,p);p.color=if(type==1)Color.rgb(93,91,100) else Color.rgb(116,69,30);for(i in 0..4)c.drawRect(l+i*ww/5,t+ww*.09f,l+(i+1)*ww/5+2,t+ww*.19f,p);val roof=Path();roof.moveTo(l-ww*.03f,t+ww*.15f);roof.lineTo((l+r)/2,t);roof.lineTo(r+ww*.03f,t+ww*.15f);roof.close();c.drawPath(roof,p);if(type==1){p.color=Color.rgb(218,213,184);c.drawCircle((l+r)/2,t+ww*.08f,ww*.13f,p);p.color=Color.rgb(72,70,76);c.drawCircle((l+r)/2,t+ww*.08f,ww*.08f,p)};p.color=Color.rgb(52,36,24);c.drawRect((l+r)/2-ww*.07f,b-ww*.14f,(l+r)/2+ww*.07f,b,p);p.color=Color.rgb(242,219,157);c.drawRect(l+ww*.08f,b-ww*.05f,r-ww*.08f,b+ww*.075f,p);label(c,name,(l+r)/2,b+ww*.018f,ww*.065f,Color.rgb(52,37,25));}
    fun ape(c:Canvas,a:Ape,x:Float,y:Float,s:Float,n:Long,i:Int){val walk=a.s==S.GO||a.s==S.BACK;val f=((n/160+i)%2).toInt();val bob=if(walk&&f==1)s*.08f else 0f;val yy=y+bob;p.color=Color.rgb(78,47,28);c.drawRect(x-s*.42f,yy-s*.42f,x+s*.42f,yy+s*.18f,p);p.color=Color.rgb(196,125,60);c.drawRect(x-s*.28f,yy-s*.27f,x+s*.28f,yy+s*.12f,p);p.color=Color.rgb(39,29,22);c.drawRect(x-s*.14f,yy-s*.09f,x-s*.07f,yy-s*.02f,p);c.drawRect(x+s*.07f,yy-s*.09f,x+s*.14f,yy-s*.02f,p);p.color=when(a.role){"WORKER"->Color.rgb(49,102,162);"POLITICIAN"->Color.rgb(32,51,65);"BANKER"->Color.rgb(39,65,75);else->Color.rgb(55,102,57)};c.drawRect(x-s*.32f,yy+s*.12f,x+s*.32f,yy+s*.63f,p);p.color=Color.rgb(72,45,28);val d=if(walk&&f==1)s*.13f else 0f;c.drawRect(x-s*.27f-d,yy+s*.6f,x-s*.04f-d,yy+s*.9f,p);c.drawRect(x+s*.04f+d,yy+s*.6f,x+s*.27f+d,yy+s*.9f,p);if(a.role=="WORKER"){p.color=Color.rgb(235,184,47);c.drawRect(x-s*.43f,yy-s*.48f,x+s*.43f,yy-s*.34f,p)};if(a.s==S.WORK)bubble(c,x,yy-s*.7f,if(a.role=="WORKER")"🍌" else if(a.role=="POLITICIAN")"▤" else "$",s);label(c,tier(a.rating)+a.rating,x-s*.5f,yy-s*.45f,s*.24f,Color.WHITE,true)}
    fun bubble(c:Canvas,x:Float,y:Float,s:String,z:Float){p.color=Color.WHITE;c.drawCircle(x,y,z*.45f,p);label(c,s,x,y+z*.1f,z*.35f,Color.DKGRAY,true)}
    fun tier(r:Int)=when(r){10->"S";9->"A";in 7..8->"B";in 5..6->"C";else->"D"}
    fun tree(c:Canvas,x:Float,y:Float,s:Float){p.color=Color.rgb(83,51,29);c.drawRect(x-s*.13f,y+s*.2f,x+s*.13f,y+s*.8f,p);p.color=Color.rgb(30,104,47);c.drawCircle(x,y,s*.6f,p);p.color=Color.rgb(56,143,59);c.drawCircle(x-s*.13f,y-s*.12f,s*.45f,p);p.color=Color.rgb(99,181,70);c.drawCircle(x-s*.2f,y-s*.24f,s*.18f,p)}
    fun bananaPlant(c:Canvas,x:Float,y:Float,s:Float){p.color=Color.rgb(35,124,48);for(k in -1..1)c.drawOval(x+k*s*.25f-s*.2f,y-s*.1f,x+k*s*.25f+s*.2f,y+s*.5f,p);p.color=Color.rgb(244,196,47);c.drawCircle(x,y+s*.2f,s*.14f,p)}
    fun flower(c:Canvas,x:Float,y:Float,s:Float,k:Int){p.color=Color.rgb(35,120,47);c.drawRect(x-s*.12f,y,x+s*.12f,y+s,p);p.color=when(k){0->Color.WHITE;1->Color.rgb(244,191,54);else->Color.rgb(232,92,87)};c.drawRect(x-s*.4f,y-s*.35f,x+s*.4f,y+s*.2f,p)}
    fun bush(c:Canvas,x:Float,y:Float,s:Float){p.color=Color.rgb(31,117,48);c.drawCircle(x,y,s,p);p.color=Color.rgb(73,164,61);c.drawCircle(x-s*.25f,y-s*.25f,s*.55f,p)}
    fun fence(c:Canvas,l:Float,t:Float,r:Float,b:Float){p.color=Color.rgb(104,67,35);c.drawRect(l,t,r,t+6,p);c.drawRect(l,b-6,r,b,p);var x=l;while(x<r){c.drawRect(x,t-7,x+7,b+7,p);x+=22}}
    fun bridge(c:Canvas,l:Float,t:Float,r:Float,b:Float){p.color=Color.rgb(91,55,30);c.drawRect(l-6,t,r+6,b,p);p.color=Color.rgb(184,119,50);var y=t+5;while(y<b){c.drawRect(l,y,r,y+10,p);y+=14}}
    fun sign(c:Canvas,x:Float,y:Float,s:String){p.color=Color.rgb(93,57,30);c.drawRect(x-3,y,x+3,y+35,p);p.color=Color.rgb(205,151,70);c.drawRect(x-22,y-18,x+22,y+12,p);label(c,s,x,y+2,16f,Color.DKGRAY)}
    fun flag(c:Canvas,x:Float,y:Float){p.color=Color.rgb(63,54,39);c.drawRect(x,y,x+5,y+45,p);p.color=Color.rgb(190,58,38);c.drawRect(x+5,y,x+38,y+22,p)}
    fun barrel(c:Canvas,x:Float,y:Float,s:Float){p.color=Color.rgb(101,61,31);c.drawOval(x-s,y-s,x+s,y+s,p);p.color=Color.rgb(191,127,52);c.drawOval(x-s*.8f,y-s*.8f,x+s*.8f,y+s*.8f,p)}
    fun crate(c:Canvas,x:Float,y:Float,s:Float){p.color=Color.rgb(105,64,31);c.drawRect(x-s,y-s,x+s,y+s,p);p.color=Color.rgb(194,128,51);c.drawRect(x-s*.8f,y-s*.8f,x+s*.8f,y+s*.8f,p)}
    fun button(c:Canvas,l:Float,t:Float,r:Float,b:Float,s:String,col:Int){p.color=Color.rgb(36,31,25);c.drawRect(l-5,t-5,r+5,b+5,p);p.color=col;c.drawRect(l,t,r,b,p);p.color=Color.argb(75,255,255,255);c.drawRect(l+5,t+5,r-5,t+10,p);label(c,s,(l+r)/2,(t+b)/2+width*.008f,width*.025f,Color.WHITE,true)}
    fun label(c:Canvas,s:String,x:Float,y:Float,z:Float,col:Int,out:Boolean=false){txt.textSize=z;txt.textAlign=Paint.Align.CENTER;if(out){txt.style=Paint.Style.STROKE;txt.strokeWidth=max(2f,z*.12f);txt.color=Color.rgb(55,42,28);c.drawText(s,x,y,txt)};txt.style=Paint.Style.FILL;txt.color=col;c.drawText(s,x,y,txt)}
    fun event(c:Canvas){val w=width.toFloat(),h=height.toFloat();p.color=Color.argb(205,24,28,25);c.drawRect(0f,0f,w,h,p);panel(c,.10f*w,.31f*h,.90f*w,.66f*h);label(c,"DORF-EREIGNIS",.5f*w,.38f*h,.04f*w,Color.rgb(91,54,27));label(c,"Ein neuer Tag verlangt eine Entscheidung!",.5f*w,.44f*h,.021f*w,Color.DKGRAY);button(c,.17f*w,.50f*h,.83f*w,.57f*h,"🍌 Vorräte verteilen",Color.rgb(63,139,72));button(c,.17f*w,.59f*h,.83f*w,.64f*h,"$ Geschäft machen",Color.rgb(160,101,51))}
    override fun onTouchEvent(e:MotionEvent):Boolean{if(e.action!=MotionEvent.ACTION_UP)return true;val x=e.x/width,y=e.y/height;if(event){if(y>.49f&&y<.66f){if(y<.58f){bananas=max(0,bananas-20);hearts+=5}else{bananas+=30;hearts=max(0,hearts-3)};event=false;start=System.currentTimeMillis()};invalidate();return true};if(y>.89f&&x>.66f)start-=10000;if(y>.89f&&x>.33f&&x<.66f){bananas+=5;pops+=Pop("BAUEN bald :3",.5f,.82f,System.currentTimeMillis())};invalidate();return true}
}
