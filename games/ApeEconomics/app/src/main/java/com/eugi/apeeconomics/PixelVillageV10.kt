package com.eugi.apeeconomics

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.max

class PixelVillageV10(context:Context):View(context){
 data class Ape(var x:Float,var y:Float,val hx:Float,val hy:Float,val jx:Float,val jy:Float,val shirt:Int,var phase:Int=0,var since:Long=0)
 private val p=Paint().apply{isAntiAlias=false};private val t=Paint().apply{isAntiAlias=false;typeface=Typeface.create(Typeface.MONOSPACE,Typeface.BOLD);textAlign=Paint.Align.CENTER}
 private var bananas=128;private var day=1;private var start=System.currentTimeMillis();private var event=false
 private val apes=mutableListOf(Ape(.20f,.35f,.20f,.35f,.20f,.27f,Color.rgb(44,103,166)),Ape(.48f,.36f,.48f,.36f,.48f,.27f,Color.rgb(30,48,62)),Ape(.76f,.36f,.76f,.36f,.76f,.28f,Color.rgb(44,91,59)),Ape(.55f,.69f,.55f,.69f,.76f,.69f,Color.rgb(37,68,77)))
 init{keepScreenOn=true;systemUiVisibility=SYSTEM_UI_FLAG_FULLSCREEN or SYSTEM_UI_FLAG_HIDE_NAVIGATION or SYSTEM_UI_FLAG_IMMERSIVE_STICKY or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or SYSTEM_UI_FLAG_LAYOUT_STABLE;val n=System.currentTimeMillis();apes.forEachIndexed{i,a->a.since=n+i*650}}
 fun handleBack():Boolean{if(event){event=false;return true};return false}
 override fun onWindowVisibilityChanged(v:Int){super.onWindowVisibilityChanged(v);if(v==VISIBLE)systemUiVisibility=5894}
 override fun onDraw(c:Canvas){val n=System.currentTimeMillis();update(n);world(c,n);postInvalidateOnAnimation()}
 private fun update(n:Long){if(!event&&n-start>30000){day++;start=n;if(day%3==0)event=true};if(event)return;apes.forEach{a->val d=when(a.phase){0->900L;1->1700L;2->2500L;else->1700L};if(n-a.since>d){a.phase=(a.phase+1)%4;a.since=n;if(a.phase==3)bananas+=if(a.shirt==Color.rgb(44,103,166))10 else 4};val f=((n-a.since).toFloat()/d).coerceIn(0f,1f);when(a.phase){1->{a.x=a.hx+(a.jx-a.hx)*f;a.y=a.hy+(a.jy-a.hy)*f};2->{a.x=a.jx;a.y=a.jy};3->{a.x=a.jx+(a.hx-a.jx)*f;a.y=a.jy+(a.hy-a.jy)*f};else->{a.x=a.hx;a.y=a.hy}}}}
 private fun world(c:Canvas,n:Long){val w=width.toFloat();val h=height.toFloat();val top=h*.095f;val bot=h*.91f;p.color=Color.rgb(91,178,69);c.drawRect(0f,top,w,bot,p)
  // soft grass texture
  p.color=Color.rgb(69,155,59);for(i in 0..55){val x=((i*71)%97)/100f*w;val y=(.11f+((i*47)%76)/100f)*h;c.drawRect(x,y,x+3,y+6,p)}
  // paths - narrower, connected village lanes
  p.color=Color.rgb(231,201,128);c.drawRect(.465f*w,top,.535f*w,.88f*h,p);c.drawRect(.08f*w,.39f*h,.92f*w,.435f*h,p);c.drawRect(.11f*w,.69f*h,.87f*w,.735f*h,p)
  // river with banks
  p.color=Color.rgb(48,119,57);c.drawRect(0,.49f*h,w,.502f*h,p);p.color=Color.rgb(34,133,202);c.drawRect(0,.502f*h,w,.575f*h,p);p.color=Color.rgb(55,177,220);c.drawRect(0,.515f*h,w,.562f*h,p);p.color=Color.rgb(196,238,233);val off=((n/160)%6)*w*.008f;for(i in 0..9)c.drawRect(i*w*.12f-off,.537f*h,i*w*.12f+w*.045f,.541f*h,p);bridge(c,.455f*w,.49f*h,.545f*w,.585f*h)
  // top district foliage and buildings
  for(i in 0..9)tree(c,w*(.03f+i*.105f),top+h*.025f,w*.040f)
  fence(c,.035f*w,.19f*h,.29f*w,.345f*h);for(r in 0..2)for(k in 0..2)plant(c,w*(.07f+k*.075f),h*(.245f+r*.038f),w*.020f)
  farm(c,.075f*w,.18f*h,.285f*w,.315f*h);parliament(c,.375f*w,.17f*h,.595f*w,.315f*h);bank(c,.69f*w,.19f*h,.91f*w,.325f*h)
  // lower district details
  tree(c,.12f*w,.63f*h,w*.055f);tree(c,.25f*w,.64f*h,w*.055f);bush(c,.38f*w,.68f*h,w*.028f);sign(c,.40f*w,.665f*h);market(c,.69f*w,.61f*h,.91f*w,.755f*h);barrel(c,.66f*w,.72f*h,w*.018f);crate(c,.91f*w,.72f*h,w*.019f);fence(c,.02f*w,.77f*h,.30f*w,.795f*h)
  for(i in 0..38){val x=((i*67)%94+3)/100f*w;val y=(.13f+((i*43)%70)/100f)*h;flower(c,x,y,w*.006f,i%3)}
  apes.sortedBy{it.y}.forEachIndexed{i,a->ape(c,w*a.x,h*a.y,w*.046f,a.shirt,n,i,a.phase==2)};hud(c,w,h,n);buttons(c,w,h);if(event)event(c,w,h)
 }
 private fun hud(c:Canvas,w:Float,h:Float,n:Long){p.color=Color.rgb(27,45,40);c.drawRect(0,0f,w,.095f*h,p);panel(c,.012f*w,.012f*h,.245f*w,.083f*h);label(c,"APE 🍌",.128f*w,.043f*h,.026f*w,Color.rgb(83,49,22));label(c,"ECONOMICS",.128f*w,.068f*h,.019f*w,Color.rgb(83,49,22));panel(c,.258f*w,.012f*h,.49f*w,.083f*h);label(c,"🍌 $bananas",.374f*w,.047f*h,.026f*w,Color.DKGRAY);label(c,"+AUTO",.374f*w,.069f*h,.014f*w,Color.DKGRAY);panel(c,.503f*w,.012f*h,.72f*w,.083f*h);label(c,"♥ 50   📣 1",.611f*w,.052f*h,.021f*w,Color.DKGRAY);panel(c,.733f*w,.012f*h,.988f*w,.083f*h);label(c,"🐒 4/10  TAG $day",.86f*w,.042f*h,.018f*w,Color.DKGRAY);label(c,"☀ ${max(0,(30000-(n-start)).toInt())/1000}s",.86f*w,.067f*h,.015f*w,Color.DKGRAY)}
 private fun panel(c:Canvas,l:Float,tt:Float,r:Float,b:Float){p.color=Color.rgb(72,47,27);c.drawRect(l-4,tt-4,r+4,b+4,p);p.color=Color.rgb(246,235,195);c.drawRect(l,tt,r,b,p);p.color=Color.rgb(218,192,132);c.drawRect(l+4,tt+4,r-4,tt+7,p)}
 private fun farm(c:Canvas,l:Float,tt:Float,r:Float,b:Float){building(c,l,tt,r,b,"PLANTAGE",Color.rgb(188,124,44),Color.rgb(130,76,27));p.color=Color.rgb(239,184,42);c.drawCircle((l+r)/2,tt+(r-l)*.30f,(r-l)*.06f,p)}
 private fun parliament(c:Canvas,l:Float,tt:Float,r:Float,b:Float){building(c,l,tt,r,b,"PARLAMENT",Color.rgb(166,171,166),Color.rgb(91,91,101));p.color=Color.rgb(86,92,103);for(i in 0..3)c.drawRect(l+(r-l)*(.12f+i*.22f),tt+(r-l)*.26f,l+(r-l)*(.18f+i*.22f),b-(r-l)*.16f,p);p.color=Color.rgb(187,46,39);c.drawRect((l+r)/2-(r-l)*.13f,b-(r-l)*.12f,(l+r)/2+(r-l)*.13f,b,p)}
 private fun bank(c:Canvas,l:Float,tt:Float,r:Float,b:Float){building(c,l,tt,r,b,"BANK",Color.rgb(184,119,48),Color.rgb(119,68,28));label(c,"$",(l+r)/2,tt+(r-l)*.38f,(r-l)*.18f,Color.rgb(58,42,27),true)}
 private fun market(c:Canvas,l:Float,tt:Float,r:Float,b:Float){building(c,l,tt,r,b,"MARKT",Color.rgb(189,104,55),Color.rgb(141,60,36));p.color=Color.rgb(245,225,178);c.drawRect(l+(r-l)*.08f,b-(r-l)*.25f,r-(r-l)*.08f,b-(r-l)*.12f,p);p.color=Color.rgb(175,54,44);for(i in 0..3)c.drawRect(l+(r-l)*(.1f+i*.21f),b-(r-l)*.25f,l+(r-l)*(.19f+i*.21f),b-(r-l)*.12f,p)}
 private fun building(c:Canvas,l:Float,tt:Float,r:Float,b:Float,name:String,wall:Int,roof:Int){val q=r-l;p.color=Color.rgb(70,46,28);c.drawRect(l-3,tt+q*.18f,r+3,b,p);p.color=wall;c.drawRect(l,tt+q*.18f,r,b,p);p.color=roof;val z=Path();z.moveTo(l-q*.035f,tt+q*.2f);z.lineTo((l+r)/2,tt);z.lineTo(r+q*.035f,tt+q*.2f);z.close();c.drawPath(z,p);p.color=Color.rgb(49,34,23);c.drawRect((l+r)/2-q*.06f,b-q*.12f,(l+r)/2+q*.06f,b,p);p.color=Color.rgb(245,222,157);c.drawRect(l+q*.08f,b-q*.025f,r-q*.08f,b+q*.055f,p);label(c,name,(l+r)/2,b+q*.018f,q*.052f,Color.rgb(52,37,24))}
 private fun ape(c:Canvas,x:Float,y:Float,s:Float,shirt:Int,n:Long,i:Int,work:Boolean){val f=((n/170+i)%2).toInt();val bob=if(f==1)s*.05f else 0f;val yy=y+bob;p.color=Color.rgb(67,43,27);c.drawCircle(x,yy-s*.25f,s*.42f,p);p.color=Color.rgb(203,132,67);c.drawRect(x-s*.26f,yy-s*.27f,x+s*.26f,yy+s*.08f,p);p.color=Color.BLACK;c.drawRect(x-s*.13f,yy-s*.10f,x-s*.06f,yy-s*.03f,p);c.drawRect(x+s*.06f,yy-s*.10f,x+s*.13f,yy-s*.03f,p);p.color=shirt;c.drawRect(x-s*.31f,yy+s*.09f,x+s*.31f,yy+s*.55f,p);p.color=Color.rgb(67,43,27);val d=if(f==1)s*.1f else 0f;c.drawRect(x-s*.24f-d,yy+s*.52f,x-s*.03f-d,yy+s*.82f,p);c.drawRect(x+s*.03f+d,yy+s*.52f,x+s*.24f+d,yy+s*.82f,p);if(work){p.color=Color.WHITE;c.drawCircle(x,yy-s*.68f,s*.35f,p);label(c,"!",x,yy-s*.58f,s*.29f,Color.DKGRAY)}}
 private fun tree(c:Canvas,x:Float,y:Float,s:Float){p.color=Color.rgb(78,48,27);c.drawRect(x-s*.1f,y+s*.15f,x+s*.1f,y+s*.72f,p);p.color=Color.rgb(28,103,47);c.drawCircle(x,y,s*.57f,p);p.color=Color.rgb(59,148,62);c.drawCircle(x-s*.12f,y-s*.1f,s*.42f,p);p.color=Color.rgb(111,188,73);c.drawCircle(x-s*.2f,y-s*.23f,s*.14f,p)}
 private fun plant(c:Canvas,x:Float,y:Float,s:Float){p.color=Color.rgb(31,125,47);c.drawCircle(x-s*.25f,y,s*.34f,p);c.drawCircle(x+s*.25f,y,s*.34f,p);p.color=Color.rgb(241,188,40);c.drawCircle(x,y,s*.15f,p)}
 private fun flower(c:Canvas,x:Float,y:Float,s:Float,k:Int){p.color=Color.rgb(35,118,47);c.drawRect(x-1,y,x+1,y+s,p);p.color=if(k==0)Color.WHITE else if(k==1)Color.rgb(245,194,53) else Color.rgb(230,89,85);c.drawCircle(x,y-s*.15f,s*.32f,p)}
 private fun bush(c:Canvas,x:Float,y:Float,s:Float){p.color=Color.rgb(30,112,47);c.drawCircle(x,y,s,p);p.color=Color.rgb(76,164,62);c.drawCircle(x-s*.25f,y-s*.25f,s*.55f,p)}
 private fun fence(c:Canvas,l:Float,tt:Float,r:Float,b:Float){p.color=Color.rgb(105,67,34);c.drawRect(l,tt,r,tt+5,p);c.drawRect(l,b-5,r,b,p);var x=l;while(x<r){c.drawRect(x,tt-5,x+5,b+5,p);x+=20}}
 private fun bridge(c:Canvas,l:Float,tt:Float,r:Float,b:Float){p.color=Color.rgb(87,54,30);c.drawRect(l-5,tt,r+5,b,p);p.color=Color.rgb(188,122,51);var y=tt+4;while(y<b){c.drawRect(l,y,r,y+9,p);y+=13}}
 private fun sign(c:Canvas,x:Float,y:Float){p.color=Color.rgb(94,57,30);c.drawRect(x-2,y,x+2,y+27,p);p.color=Color.rgb(208,153,72);c.drawRect(x-15,y-11,x+15,y+7,p)}
 private fun barrel(c:Canvas,x:Float,y:Float,s:Float){p.color=Color.rgb(100,61,31);c.drawCircle(x,y,s,p);p.color=Color.rgb(190,127,52);c.drawCircle(x,y,s*.7f,p)}
 private fun crate(c:Canvas,x:Float,y:Float,s:Float){p.color=Color.rgb(104,63,31);c.drawRect(x-s,y-s,x+s,y+s,p);p.color=Color.rgb(194,128,51);c.drawRect(x-s*.7f,y-s*.7f,x+s*.7f,y+s*.7f,p)}
 private fun buttons(c:Canvas,w:Float,h:Float){p.color=Color.rgb(60,40,27);c.drawRect(0,.91f*h,w,h,p);button(c,.045f*w,.928f*h,.31f*w,.982f*h,"🐒 AFFEN",Color.rgb(55,139,68));button(c,.37f*w,.928f*h,.63f*w,.982f*h,"🔨 BAUEN",Color.rgb(145,94,52));button(c,.69f*w,.925f*h,.955f*w,.982f*h,"≫ +10s",Color.rgb(43,139,211))}
 private fun button(c:Canvas,l:Float,tt:Float,r:Float,b:Float,s:String,col:Int){p.color=Color.rgb(34,30,24);c.drawRect(l-4,tt-4,r+4,b+4,p);p.color=col;c.drawRect(l,tt,r,b,p);label(c,s,(l+r)/2,(tt+b)/2+width*.007f,width*.023f,Color.WHITE,true)}
 private fun label(c:Canvas,s:String,x:Float,y:Float,z:Float,col:Int,out:Boolean=false){t.textSize=z;if(out){t.style=Paint.Style.STROKE;t.strokeWidth=max(2f,z*.11f);t.color=Color.rgb(54,41,27);c.drawText(s,x,y,t)};t.style=Paint.Style.FILL;t.color=col;c.drawText(s,x,y,t)}
 private fun event(c:Canvas,w:Float,h:Float){p.color=Color.argb(210,24,28,25);c.drawRect(0f,0f,w,h,p);panel(c,.1f*w,.32f*h,.9f*w,.64f*h);label(c,"DORF-EREIGNIS",.5f*w,.39f*h,.037f*w,Color.rgb(91,54,27));label(c,"Eine Entscheidung ist fällig!",.5f*w,.45f*h,.021f*w,Color.DKGRAY);button(c,.17f*w,.51f*h,.83f*w,.575f*h,"🍌 Vorräte verteilen",Color.rgb(60,137,70));button(c,.17f*w,.59f*h,.83f*w,.635f*h,"$ Geschäft machen",Color.rgb(158,100,50))}
 override fun onTouchEvent(e:MotionEvent):Boolean{if(e.action!=MotionEvent.ACTION_UP)return true;systemUiVisibility=5894;val x=e.x/width;val y=e.y/height;if(event&&y>.50f&&y<.65f){event=false;if(y<.585f)bananas=max(0,bananas-20)else bananas+=30;start=System.currentTimeMillis();return true};if(y>.90f&&x>.66f)start-=10000;return true}
}
