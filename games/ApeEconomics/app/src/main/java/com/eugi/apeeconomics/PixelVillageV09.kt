package com.eugi.apeeconomics

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.max

class PixelVillageV09(context: Context): View(context) {
    data class Ape(var x:Float,var y:Float,val hx:Float,val hy:Float,val jx:Float,val jy:Float,val shirt:Int,var phase:Int=0,var since:Long=0)
    private val p=Paint().apply{isAntiAlias=false}
    private val t=Paint().apply{isAntiAlias=false;typeface=Typeface.create(Typeface.MONOSPACE,Typeface.BOLD);textAlign=Paint.Align.CENTER}
    private var bananas=128; private var day=1; private var start=System.currentTimeMillis(); private var event=false
    private val apes=mutableListOf(Ape(.25f,.43f,.25f,.43f,.23f,.32f,Color.rgb(45,101,165)),Ape(.50f,.45f,.50f,.45f,.50f,.33f,Color.rgb(34,52,66)),Ape(.78f,.45f,.78f,.45f,.79f,.34f,Color.rgb(45,91,61)),Ape(.58f,.70f,.58f,.70f,.79f,.73f,Color.rgb(37,68,77)))
    init{keepScreenOn=true;val n=System.currentTimeMillis();apes.forEachIndexed{i,a->a.since=n+i*700}}
    fun handleBack():Boolean{if(event){event=false;return true};return false}
    override fun onDraw(c:Canvas){val n=System.currentTimeMillis();update(n);drawWorld(c,n);postInvalidateOnAnimation()}
    private fun update(n:Long){if(!event&&n-start>30000){day++;start=n;if(day%3==0)event=true};if(event)return;apes.forEach{a->val dur=when(a.phase){0->1000L;1->1800L;2->2400L;else->1800L};if(n-a.since>dur){a.phase=(a.phase+1)%4;a.since=n;if(a.phase==3)bananas+=if(a.shirt==Color.rgb(45,101,165))10 else 4};val f=((n-a.since).toFloat()/dur).coerceIn(0f,1f);when(a.phase){1->{a.x=a.hx+(a.jx-a.hx)*f;a.y=a.hy+(a.jy-a.hy)*f};2->{a.x=a.jx;a.y=a.jy};3->{a.x=a.jx+(a.hx-a.jx)*f;a.y=a.jy+(a.hy-a.jy)*f};else->{a.x=a.hx;a.y=a.hy}}}}
    private fun drawWorld(c:Canvas,n:Long){val w=width.toFloat();val h=height.toFloat();val top=h*.13f;val bot=h*.89f;p.color=Color.rgb(96,178,70);c.drawRect(0f,top,w,bot,p)
        // dense foliage borders
        for(i in 0..12){tree(c,w*(i/12f),top+h*.025f,w*.052f);tree(c,w*(i/12f),bot-h*.02f,w*.052f)}
        // paths
        p.color=Color.rgb(229,199,126);c.drawRect(w*.46f,top,w*.54f,bot,p);c.drawRect(w*.06f,h*.43f,w*.94f,h*.49f,p);c.drawRect(w*.10f,h*.69f,w*.91f,h*.75f,p)
        // plantation + details
        fence(c,w*.04f,h*.21f,w*.32f,h*.405f);for(r in 0..2)for(k in 0..2)plant(c,w*(.075f+k*.075f),h*(.27f+r*.043f),w*.025f)
        house(c,w*.09f,h*.205f,w*.30f,h*.35f,"PLANTAGE",Color.rgb(191,128,48));house(c,w*.37f,h*.19f,w*.63f,h*.365f,"PARLAMENT",Color.rgb(166,170,164));house(c,w*.70f,h*.215f,w*.93f,h*.375f,"BANK",Color.rgb(176,111,49))
        // river + bridge
        p.color=Color.rgb(41,117,67);c.drawRect(0f,h*.505f,w,h*.515f,p);p.color=Color.rgb(30,126,201);c.drawRect(0f,h*.515f,w,h*.59f,p);p.color=Color.rgb(51,171,222);c.drawRect(0f,h*.528f,w,h*.577f,p);p.color=Color.rgb(189,239,241);val off=((n/180)%4)*w*.01f;for(i in 0..8)c.drawRect(i*w*.13f-off,h*.55f,i*w*.13f+w*.055f,h*.554f,p);bridge(c,w*.455f,h*.505f,w*.545f,h*.60f)
        // lower district
        tree(c,w*.13f,h*.64f,w*.07f);tree(c,w*.25f,h*.64f,w*.07f);bush(c,w*.61f,h*.68f,w*.035f);sign(c,w*.40f,h*.68f);house(c,w*.70f,h*.625f,w*.93f,h*.80f,"MARKT",Color.rgb(185,80,48));fence(c,0f,h*.79f,w*.31f,h*.81f)
        // flowers and texture
        for(i in 0..34){val x=((i*67)%94+3)/100f*w;val y=(.15f+((i*43)%69)/100f)*h;flower(c,x,y,w*.008f,i%3)}
        apes.sortedBy{it.y}.forEachIndexed{i,a->ape(c,w*a.x,h*a.y,w*.037f,a.shirt,n,i,a.phase==2)}
        hud(c,w,h,n);buttons(c,w,h);if(event)drawEvent(c,w,h)
    }
    private fun hud(c:Canvas,w:Float,h:Float,n:Long){p.color=Color.rgb(29,47,43);c.drawRect(0f,0f,w,h*.13f,p);panel(c,.02f*w,.018f*h,.29f*w,.112f*h);label(c,"APE 🍌",.155f*w,.055f*h,.035f*w,Color.rgb(81,48,23));label(c,"ECONOMICS",.155f*w,.086f*h,.027f*w,Color.rgb(81,48,23));panel(c,.31f*w,.018f*h,.55f*w,.112f*h);label(c,"🍌 $bananas",.43f*w,.058f*h,.030f*w,Color.DKGRAY);label(c,"AUTO",.43f*w,.088f*h,.017f*w,Color.DKGRAY);panel(c,.57f*w,.018f*h,.76f*w,.112f*h);label(c,"♥ 50",.665f*w,.056f*h,.028f*w,Color.rgb(154,42,51));label(c,"📣 1",.665f*w,.088f*h,.017f*w,Color.DKGRAY);panel(c,.78f*w,.018f*h,.98f*w,.112f*h);label(c,"🐒 4/10",.88f*w,.047f*h,.023f*w,Color.DKGRAY);label(c,"TAG $day",.88f*w,.076f*h,.019f*w,Color.DKGRAY);label(c,"☀ ${max(0,(30000-(n-start)).toInt())/1000}s",.88f*w,.101f*h,.016f*w,Color.DKGRAY)}
    private fun buttons(c:Canvas,w:Float,h:Float){p.color=Color.rgb(65,43,28);c.drawRect(0f,h*.89f,w,h,p);button(c,.05f*w,.91f*h,.32f*w,.975f*h,"🐒 AFFEN",Color.rgb(56,137,67));button(c,.365f*w,.91f*h,.635f*w,.975f*h,"🔨 BAUEN",Color.rgb(139,91,51));button(c,.68f*w,.905f*h,.95f*w,.978f*h,"≫ +10s",Color.rgb(45,139,210))}
    private fun panel(c:Canvas,l:Float,tt:Float,r:Float,b:Float){p.color=Color.rgb(75,48,28);c.drawRect(l-5,tt-5,r+5,b+5,p);p.color=Color.rgb(244,235,196);c.drawRect(l,tt,r,b,p);p.color=Color.rgb(216,192,136);c.drawRect(l+5,tt+5,r-5,tt+9,p)}
    private fun house(c:Canvas,l:Float,tt:Float,r:Float,b:Float,name:String,col:Int){val q=r-l;p.color=Color.rgb(77,48,27);c.drawRect(l-4,tt+q*.15f,r+4,b,p);p.color=col;c.drawRect(l,tt+q*.15f,r,b,p);p.color=Color.rgb(112,65,29);val roof=Path();roof.moveTo(l-q*.04f,tt+q*.16f);roof.lineTo((l+r)/2,tt);roof.lineTo(r+q*.04f,tt+q*.16f);roof.close();c.drawPath(roof,p);p.color=Color.rgb(48,34,23);c.drawRect((l+r)/2-q*.07f,b-q*.13f,(l+r)/2+q*.07f,b,p);p.color=Color.rgb(244,221,158);c.drawRect(l+q*.08f,b-q*.04f,r-q*.08f,b+q*.07f,p);label(c,name,(l+r)/2,b+q*.018f,q*.062f,Color.rgb(54,38,25))}
    private fun ape(c:Canvas,x:Float,y:Float,s:Float,shirt:Int,n:Long,i:Int,work:Boolean){val f=((n/170+i)%2).toInt();val bob=if(f==1)s*.06f else 0f;val yy=y+bob;p.color=Color.rgb(76,46,27);c.drawRect(x-s*.4f,yy-s*.42f,x+s*.4f,yy+s*.15f,p);p.color=Color.rgb(199,129,63);c.drawRect(x-s*.27f,yy-s*.27f,x+s*.27f,yy+s*.10f,p);p.color=shirt;c.drawRect(x-s*.31f,yy+s*.11f,x+s*.31f,yy+s*.62f,p);p.color=Color.rgb(70,44,27);val d=if(f==1)s*.12f else 0f;c.drawRect(x-s*.26f-d,yy+s*.58f,x-s*.04f-d,yy+s*.88f,p);c.drawRect(x+s*.04f+d,yy+s*.58f,x+s*.26f+d,yy+s*.88f,p);if(work){p.color=Color.WHITE;c.drawCircle(x,yy-s*.68f,s*.42f,p);label(c,"!",x,yy-s*.58f,s*.35f,Color.DKGRAY)}}
    private fun tree(c:Canvas,x:Float,y:Float,s:Float){p.color=Color.rgb(80,49,28);c.drawRect(x-s*.12f,y+s*.2f,x+s*.12f,y+s*.8f,p);p.color=Color.rgb(29,105,47);c.drawCircle(x,y,s*.58f,p);p.color=Color.rgb(58,145,60);c.drawCircle(x-s*.12f,y-s*.12f,s*.43f,p);p.color=Color.rgb(108,185,72);c.drawCircle(x-s*.22f,y-s*.24f,s*.16f,p)}
    private fun plant(c:Canvas,x:Float,y:Float,s:Float){p.color=Color.rgb(34,126,48);c.drawCircle(x-s*.25f,y,s*.32f,p);c.drawCircle(x+s*.25f,y,s*.32f,p);p.color=Color.rgb(242,191,44);c.drawCircle(x,y,s*.14f,p)}
    private fun flower(c:Canvas,x:Float,y:Float,s:Float,k:Int){p.color=Color.rgb(35,119,47);c.drawRect(x-2,y,x+2,y+s,p);p.color=if(k==0)Color.WHITE else if(k==1)Color.rgb(245,194,53) else Color.rgb(231,91,86);c.drawRect(x-s*.4f,y-s*.3f,x+s*.4f,y+s*.2f,p)}
    private fun bush(c:Canvas,x:Float,y:Float,s:Float){p.color=Color.rgb(31,116,47);c.drawCircle(x,y,s,p);p.color=Color.rgb(76,163,61);c.drawCircle(x-s*.25f,y-s*.25f,s*.55f,p)}
    private fun fence(c:Canvas,l:Float,tt:Float,r:Float,b:Float){p.color=Color.rgb(105,67,34);c.drawRect(l,tt,r,tt+6,p);c.drawRect(l,b-6,r,b,p);var x=l;while(x<r){c.drawRect(x,tt-6,x+6,b+6,p);x+=22}}
    private fun bridge(c:Canvas,l:Float,tt:Float,r:Float,b:Float){p.color=Color.rgb(91,55,30);c.drawRect(l-6,tt,r+6,b,p);p.color=Color.rgb(187,121,50);var y=tt+5;while(y<b){c.drawRect(l,y,r,y+10,p);y+=14}}
    private fun sign(c:Canvas,x:Float,y:Float){p.color=Color.rgb(94,57,30);c.drawRect(x-3,y,x+3,y+34,p);p.color=Color.rgb(207,152,71);c.drawRect(x-20,y-15,x+20,y+10,p);label(c,"☷",x,y+4,14f,Color.DKGRAY)}
    private fun button(c:Canvas,l:Float,tt:Float,r:Float,b:Float,s:String,col:Int){p.color=Color.rgb(35,31,25);c.drawRect(l-5,tt-5,r+5,b+5,p);p.color=col;c.drawRect(l,tt,r,b,p);label(c,s,(l+r)/2,(tt+b)/2+width*.008f,width*.025f,Color.WHITE,true)}
    private fun label(c:Canvas,s:String,x:Float,y:Float,z:Float,col:Int,out:Boolean=false){t.textSize=z;if(out){t.style=Paint.Style.STROKE;t.strokeWidth=max(2f,z*.12f);t.color=Color.rgb(55,42,28);c.drawText(s,x,y,t)};t.style=Paint.Style.FILL;t.color=col;c.drawText(s,x,y,t)}
    private fun drawEvent(c:Canvas,w:Float,h:Float){p.color=Color.argb(205,25,28,25);c.drawRect(0f,0f,w,h,p);panel(c,.1f*w,.32f*h,.9f*w,.65f*h);label(c,"DORF-EREIGNIS",.5f*w,.39f*h,.04f*w,Color.rgb(91,54,27));label(c,"Eine Entscheidung ist fällig!",.5f*w,.45f*h,.022f*w,Color.DKGRAY);button(c,.17f*w,.51f*h,.83f*w,.58f*h,"🍌 Vorräte verteilen",Color.rgb(60,137,70));button(c,.17f*w,.59f*h,.83f*w,.64f*h,"$ Geschäft machen",Color.rgb(158,100,50))}
    override fun onTouchEvent(e:MotionEvent):Boolean{if(e.action!=MotionEvent.ACTION_UP)return true;val x=e.x/width;val y=e.y/height;if(event&&y>.50f&&y<.66f){event=false;if(y<.585f)bananas=max(0,bananas-20)else bananas+=30;start=System.currentTimeMillis();return true};if(y>.89f&&x>.66f)start-=10000;return true}
}
