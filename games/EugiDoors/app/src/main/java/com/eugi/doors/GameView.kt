package com.eugi.doors

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.*
import kotlin.random.Random

class GameView(context: Context) : View(context) {
    private enum class Mode { HUB, RUNNER, JUMPER, BOSS, SWARM }
    private val paint = Paint().apply { isAntiAlias = false }
    private val textPaint = Paint().apply { isAntiAlias = false; typeface = Typeface.MONOSPACE }
    private val rng = Random(42)
    private var mode = Mode.HUB
    private var lastNs = System.nanoTime()
    private var score = 0
    private val best = intArrayOf(0,0,0,0)
    private var touchX = 0f
    private var touchY = 0f
    private var touching = false
    private var msg = ""
    private var msgTimer = 0f

    data class Obstacle(var x:Float,var w:Float,var h:Float,var kind:Int)
    private val runnerObs = mutableListOf<Obstacle>()
    private var runnerY = 0f
    private var runnerVy = 0f
    private var runnerSpeed = 300f
    private var runnerSpawn = 1f
    private var runnerAnim = 0f

    data class Platform(var x:Float,var y:Float,var w:Float,var bounce:Boolean=false)
    private val platforms = mutableListOf<Platform>()
    private var jumpX=0f; private var jumpY=0f; private var jumpVx=0f; private var jumpVy=0f

    data class Orb(var x:Float,var y:Float,var vx:Float,var vy:Float,var r:Float=7f)
    private val bossOrbs = mutableListOf<Orb>()
    private var bossHp=300f; private var playerHp=100f
    private var bossX=0f; private var bossY=0f; private var playerX=0f; private var playerY=0f
    private var bossCd=0f; private var slash=0f; private var dodge=0f

    data class Mob(var x:Float,var y:Float,var speed:Float,var hp:Int,var kind:Int)
    private val mobs = mutableListOf<Mob>()
    private val drops = mutableListOf<PointF>()
    private var swarmX=0f; private var swarmY=0f; private var swarmHp=100f
    private var swarmSpawn=0f; private var autoCd=0f; private var wave=1
    private var wormPhase=0f

    override fun onDraw(c: Canvas) {
        val now = System.nanoTime()
        val dt = ((now-lastNs)/1e9f).coerceIn(0f,0.033f)
        lastNs = now
        when(mode){
            Mode.HUB -> drawHub(c)
            Mode.RUNNER -> { updateRunner(dt); drawRunner(c) }
            Mode.JUMPER -> { updateJumper(dt); drawJumper(c) }
            Mode.BOSS -> { updateBoss(dt); drawBoss(c) }
            Mode.SWARM -> { updateSwarm(dt); drawSwarm(c) }
        }
        if(msgTimer>0f){ msgTimer-=dt; drawBanner(c,msg) }
        invalidate()
    }

    private fun color(hex:String)=Color.parseColor(hex)
    private fun rect(c:Canvas,col:Int,l:Float,t:Float,r:Float,b:Float){ paint.color=col; c.drawRect(l,t,r,b,paint) }
    private fun px(c:Canvas,col:Int,x:Float,y:Float,w:Float,h:Float){ paint.color=col; c.drawRect(x,y,x+w,y+h,paint) }
    private fun txt(c:Canvas,s:String,x:Float,y:Float,size:Float,col:Int=Color.WHITE,align:Paint.Align=Paint.Align.LEFT){
        textPaint.color=col; textPaint.textSize=size; textPaint.textAlign=align; textPaint.typeface=Typeface.MONOSPACE; c.drawText(s,x,y,textPaint)
    }
    private fun outlineRect(c:Canvas,l:Float,t:Float,r:Float,b:Float,fill:Int,border:Int,th:Float=3f){
        rect(c,border,l,t,r,b); rect(c,fill,l+th,t+th,r-th,b-th)
    }

    private fun hero(c:Canvas,x:Float,y:Float,s:Float=1f,flip:Boolean=false,attack:Boolean=false){
        val u=3f*s; val dir=if(flip)-1 else 1
        rect(c,color("#20172B"),x-7*u,y+7*u,x+7*u,y+9*u)
        px(c,color("#251B34"),x-5*u,y+2*u,3*u,6*u); px(c,color("#251B34"),x+2*u,y+2*u,3*u,6*u)
        px(c,color("#7B1E3E"),x-6*u,y+7*u,4*u,2*u); px(c,color("#7B1E3E"),x+2*u,y+7*u,4*u,2*u)
        px(c,color("#16131F"),x-6*u,y-7*u,12*u,10*u)
        px(c,color("#A52A4B"),x-6*u,y-4*u,12*u,2*u)
        px(c,color("#E6D7D9"),x-2*u,y-6*u,4*u,5*u)
        px(c,color("#191520"),x-9*u*dir,y-5*u,4*u,3*u); px(c,color("#F1C0B8"),x-10*u*dir,y-4*u,2*u,2*u)
        px(c,color("#191520"),x+5*u*dir,y-4*u,4*u,3*u); px(c,color("#F1C0B8"),x+8*u*dir,y-3*u,2*u,2*u)
        px(c,color("#F1C0B8"),x-5*u,y-15*u,10*u,8*u)
        px(c,color("#16131F"),x-6*u,y-17*u,12*u,5*u); px(c,color("#16131F"),x-7*u,y-15*u,3*u,8*u); px(c,color("#16131F"),x+4*u,y-15*u,3*u,7*u)
        px(c,color("#16131F"),x-9*u,y-12*u,3*u,7*u); px(c,color("#16131F"),x-11*u,y-8*u,3*u,4*u)
        px(c,color("#D12D59"),x-8*u,y-17*u,3*u,3*u); px(c,color("#D12D59"),x-11*u,y-16*u,3*u,2*u)
        px(c,color("#B72049"),x-3*u,y-12*u,2*u,2*u); px(c,color("#B72049"),x+2*u,y-12*u,2*u,2*u)
        px(c,color("#CC3154"),x-9*u*dir,y-6*u,4*u,2*u); px(c,color("#8D1E3B"),x-12*u*dir,y-5*u,4*u,2*u)
        if(attack){
            px(c,color("#F6E6D0"),x+8*u*dir,y-5*u,9*u,2*u)
            px(c,color("#E64C76"),x+16*u*dir,y-6*u,2*u,4*u)
        } else {
            px(c,color("#D5D3D8"),x+5*u,y-13*u,2*u,10*u); px(c,color("#A52A4B"),x+4*u,y-4*u,4*u,2*u)
        }
    }

    private fun slime(c:Canvas,x:Float,y:Float,s:Float=1f){
        val u=3f*s; px(c,color("#5B286E"),x-5*u,y-4*u,10*u,7*u); px(c,color("#7E3D91"),x-4*u,y-6*u,8*u,3*u)
        px(c,color("#F7D8FF"),x-2*u,y-2*u,u,u); px(c,color("#F7D8FF"),x+2*u,y-2*u,u,u)
        px(c,color("#2A1630"),x-4*u,y+3*u,8*u,u)
    }
    private fun bat(c:Canvas,x:Float,y:Float,s:Float=1f){
        val u=2.5f*s; px(c,color("#2B1839"),x-3*u,y-2*u,6*u,5*u); px(c,color("#6B3B7E"),x-8*u,y-4*u,5*u,2*u); px(c,color("#6B3B7E"),x+3*u,y-4*u,5*u,2*u); px(c,color("#F6D55C"),x-u,y-u,u,u);px(c,color("#F6D55C"),x+u,y-u,u,u)
    }
    private fun coin(c:Canvas,x:Float,y:Float){ px(c,color("#7A4C08"),x-6,y-8,12f,16f); px(c,color("#F6C84F"),x-4,y-7,8f,14f); px(c,color("#FFF0A0"),x-2,y-5,2f,10f) }
    private fun gem(c:Canvas,x:Float,y:Float){
        paint.color=color("#F78AA8"); val path=Path(); path.moveTo(x,y-10);path.lineTo(x+7,y);path.lineTo(x,y+10);path.lineTo(x-7,y);path.close();c.drawPath(path,paint)
        px(c,color("#FFE0E9"),x-2,y-6,2f,8f)
    }
    private fun stoneTile(c:Canvas,x:Float,y:Float,w:Float,h:Float,top:Boolean=false){
        rect(c,color("#2D2C34"),x,y,x+w,y+h); rect(c,color("#45444C"),x+2,y+2,x+w-2,y+7)
        if(top) rect(c,color("#7B8153"),x,y,x+w,y+4)
        var yy=y+10; var row=0
        while(yy<y+h){
            rect(c,color("#19191E"),x,yy,x+w,yy+2)
            var xx=x+(if(row%2==0)0 else 12)
            while(xx<x+w){ rect(c,color("#19191E"),xx,yy-9,xx+2,yy); xx+=24 }
            yy+=12; row++
        }
    }
    private fun panel(c:Canvas,l:Float,t:Float,r:Float,b:Float){ outlineRect(c,l,t,r,b,color("#13141B"),color("#D7AE5E"),3f) }
    private fun button(c:Canvas,cx:Float,cy:Float,r:Float,label:String,icon:Int){
        paint.color=color("#D7AE5E"); c.drawCircle(cx,cy,r,paint); paint.color=color("#1B2537"); c.drawCircle(cx,cy,r-4,paint)
        when(icon){
            0->{
                paint.color=Color.WHITE; val p=Path();p.moveTo(cx,cy-r*.35f);p.lineTo(cx-r*.28f,cy);p.lineTo(cx-r*.1f,cy);p.lineTo(cx-r*.1f,cy+r*.28f);p.lineTo(cx+r*.1f,cy+r*.28f);p.lineTo(cx+r*.1f,cy);p.lineTo(cx+r*.28f,cy);p.close();c.drawPath(p,paint)
            }
            1->{ px(c,Color.WHITE,cx-2,cy-r*.45f,4f,r*.65f); px(c,Color.WHITE,cx-r*.18f,cy+r*.05f,r*.36f,4f) }
            2->{ px(c,Color.WHITE,cx-r*.3f,cy-3,r*.45f,6f); px(c,Color.WHITE,cx-r*.45f,cy-10,r*.25f,4f); px(c,Color.WHITE,cx-r*.45f,cy+6,r*.25f,4f) }
        }
        txt(c,label,cx,cy+r*.62f,12f,Color.WHITE,Paint.Align.CENTER)
    }
    private fun portraitHud(c:Canvas,title:String,value:String,sub:String){
        panel(c,10f,10f,88f,88f); hero(c,49f,71f,.62f)
        txt(c,title,100f,35f,18f,color("#F2CA5C")); txt(c,value,100f,59f,25f,Color.WHITE); txt(c,sub,100f,81f,13f,color("#C8C8CF"))
    }
    private fun hearts(c:Canvas,x:Float,y:Float,n:Int,max:Int=5){
        repeat(max){i-> val col=if(i<n) color("#D93C5D") else color("#35353A");
            paint.color=col; val path=Path(); path.moveTo(x+i*22,y+6); path.cubicTo(x+i*22-8,y-3,x+i*22-12,y+8,x+i*22,y+18); path.cubicTo(x+i*22+12,y+8,x+i*22+8,y-3,x+i*22,y+6); c.drawPath(path,paint)
        }
    }

    private fun drawHub(c:Canvas){
        c.drawColor(color("#14131A"))
        tileFloor(c,0f,0f,width.toFloat(),height.toFloat(),32f,color("#24232B"),color("#17171C"))
        txt(c,"EUGI DOORS",width/2f,54f,30f,color("#F2CA5C"),Paint.Align.CENTER)
        txt(c,"RETRO TRIAL HALL",width/2f,79f,14f,color("#C7B7CE"),Paint.Align.CENTER)
        val doorW=(width-50f)/2f; val doorH=(height-190f)/2f
        val labels=arrayOf("01  RUIN RUN","02  SKY CLIMB","03  WITCH QUEEN","04  BLOOD CELL")
        val subs=arrayOf("side runner","vertical jumper","top-down boss","bird-view survival")
        for(i in 0..3){
            val col=i%2; val row=i/2; val l=15f+col*(doorW+20f); val t=105f+row*(doorH+20f); val r=l+doorW; val b=t+doorH
            panel(c,l,t,r,b); stoneTile(c,l+12,t+12,doorW-24,doorH-48,true)
            rect(c,color("#1E1726"),l+doorW*.28f,t+doorH*.22f,l+doorW*.72f,b-56f)
            rect(c,arrayOf(color("#B53B55"),color("#507AB7"),color("#764D96"),color("#8C3A48"))[i],l+doorW*.32f,t+doorH*.27f,l+doorW*.68f,b-61f)
            txt(c,(i+1).toString(),(l+r)/2f,t+doorH*.54f,42f,Color.WHITE,Paint.Align.CENTER)
            txt(c,labels[i],(l+r)/2f,b-30f,13f,color("#F2CA5C"),Paint.Align.CENTER)
            txt(c,subs[i],(l+r)/2f,b-12f,10f,color("#B8B8C0"),Paint.Align.CENTER)
        }
        hero(c,width/2f,height-46f,.85f)
    }
    private fun tileFloor(c:Canvas,l:Float,t:Float,r:Float,b:Float,size:Float,a:Int,bg:Int){
        rect(c,bg,l,t,r,b); var yy=t; var row=0
        while(yy<b){ var xx=l-(if(row%2==0)0 else size/2); while(xx<r){ outlineRect(c,xx,yy,xx+size,yy+size,a,color("#101015"),1f);xx+=size };yy+=size;row++ }
    }

    private fun startRunner(){ mode=Mode.RUNNER;score=0;runnerObs.clear();runnerY=0f;runnerVy=0f;runnerSpeed=300f;runnerSpawn=.8f }
    private fun updateRunner(dt:Float){
        runnerAnim+=dt*12; runnerVy+=950f*dt; runnerY+=runnerVy*dt; if(runnerY>0){runnerY=0f;runnerVy=0f}
        runnerSpeed+=7f*dt; score+=(dt*30).toInt()+1; runnerSpawn-=dt
        if(runnerSpawn<=0){ runnerObs+=Obstacle(width+20f,34f+rng.nextInt(28),32f+rng.nextInt(44),rng.nextInt(3)); runnerSpawn=.65f+rng.nextFloat()*.75f }
        val ground=height*.72f; val heroBox=RectF(width*.22f-18,ground-46+runnerY,width*.22f+18,ground+8+runnerY)
        val it=runnerObs.iterator(); while(it.hasNext()){ val o=it.next();o.x-=runnerSpeed*dt;if(o.x+o.w<0)it.remove() else {
            val box=RectF(o.x,ground-o.h,o.x+o.w,ground); if(RectF.intersects(heroBox,box)){best[0]=max(best[0],score);toHub("RUIN RUN  $score")}
        }}
    }
    private fun drawRunner(c:Canvas){
        val ground=height*.72f
        rect(c,color("#5B65A8"),0f,0f,width.toFloat(),ground)
        for(i in 0..6){ val bx=i*130f-(score%130); rect(c,color("#4F526A"),bx,ground-230,bx+76,ground); rect(c,color("#353845"),bx+14,ground-280,bx+62,ground-230) }
        for(i in 0..10){ val x=i*72f-(score*3%72); paint.color=color("#B8A2C9");c.drawCircle(x,125f+(i%3)*25,28f,paint) }
        stoneTile(c,0f,ground,width.toFloat(),height-ground,true)
        runnerObs.forEach{ o->
            when(o.kind){
                0->{ var x=o.x; while(x<o.x+o.w){ paint.color=color("#E2E1D6"); val p=Path(); p.moveTo(x,ground);p.lineTo(x+7,ground-o.h);p.lineTo(x+14,ground);p.close();c.drawPath(p,paint);x+=14 } }
                1->{ stoneTile(c,o.x,ground-o.h,o.w,o.h,true) }
                else->{ stoneTile(c,o.x,ground-o.h,o.w,o.h,false); rect(c,color("#A43C49"),o.x+8,ground-o.h+8,o.x+o.w-8,ground-8) }
            }
        }
        for(i in 0..3){ val gx=width*.48f+i*46f; gem(c,gx,ground-88f); coin(c,gx,ground-54f) }
        hero(c,width*.22f,ground-7f+runnerY,.9f,false,false)
        portraitHud(c,"SCORE",score.toString().padStart(6,'0'),"BEST ${best[0]}")
        hearts(c,105f,89f,4)
        txt(c,"COIN x ${(score/12)%999}",width-92f,32f,15f,color("#F2CA5C"),Paint.Align.CENTER)
        txt(c,"COMBO x ${(score/30)%99}",width-92f,56f,14f,color("#E9874A"),Paint.Align.CENTER)
        button(c,70f,height-66f,48f,"DASH",2); button(c,width-70f,height-66f,48f,"JUMP",0)
    }

    private fun startJumper(){
        mode=Mode.JUMPER;score=0;jumpX=width/2f;jumpY=height*.68f;jumpVx=0f;jumpVy=-560f;platforms.clear()
        repeat(11){i-> platforms+=Platform(35f+rng.nextFloat()*(width-130),height*.85f-i*78f,72f+rng.nextFloat()*35f,i%4==0) }
    }
    private fun updateJumper(dt:Float){
        if(touching){ val dir=((touchX-width/2f)/(width/2f)).coerceIn(-1f,1f); jumpVx=(jumpVx+dir*900f*dt).coerceIn(-260f,260f) } else jumpVx*=.92f
        jumpX=(jumpX+jumpVx*dt).coerceIn(16f,width-16f)
        val prev=jumpY; jumpVy+=780f*dt; jumpY+=jumpVy*dt
        if(jumpVy>0){ for(pl in platforms){ if(prev<=pl.y-24 && jumpY>=pl.y-24 && jumpX>pl.x-8 && jumpX<pl.x+pl.w+8){ jumpY=pl.y-24;jumpVy=if(pl.bounce)-650f else -530f;score+=if(pl.bounce)20 else 10;break } } }
        if(jumpY<height*.34f){ val d=height*.34f-jumpY;jumpY=height*.34f; platforms.forEach{it.y+=d}; score+=(d/4).toInt() }
        while(platforms.minOfOrNull{it.y}?:0f>60f){ val top=(platforms.minOfOrNull{it.y}?:60f);platforms+=Platform(25f+rng.nextFloat()*(width-120),top-65f-rng.nextFloat()*35f,70f+rng.nextFloat()*35f,rng.nextInt(5)==0) }
        platforms.removeAll{it.y>height+60}
        if(jumpY>height+50){best[1]=max(best[1],score);toHub("SKY CLIMB  $score")}
    }
    private fun drawJumper(c:Canvas){
        rect(c,color("#566BB9"),0f,0f,width.toFloat(),height.toFloat())
        for(i in 0..8){ val y=145f+i*115f; paint.color=if(i%2==0)color("#D3B2CC") else color("#C7C9E8"); c.drawCircle((i*93%width).toFloat(),y,28f,paint);c.drawCircle((i*93%width+30).toFloat(),y+7,20f,paint) }
        for(i in 0..4){ stoneTile(c,20f+i*105f,height-360f-i*50,42f,360f+i*50,false) }
        platforms.forEach{pl-> stoneTile(c,pl.x,pl.y,pl.w,18f,true); if(pl.bounce){rect(c,color("#694CA2"),pl.x+8,pl.y-4,pl.x+pl.w-8,pl.y+4);rect(c,color("#C77BE7"),pl.x+14,pl.y-7,pl.x+pl.w-14,pl.y-3)} }
        platforms.take(4).forEachIndexed{i,pl-> if(i%2==0) gem(c,pl.x+pl.w/2,pl.y-30) else coin(c,pl.x+pl.w/2,pl.y-30) }
        if(platforms.isNotEmpty()){ bat(c,width*.78f,height*.28f,.8f); slime(c,width*.68f,height*.62f,.8f) }
        hero(c,jumpX,jumpY,.78f)
        portraitHud(c,"HEIGHT","${score.toString().padStart(5,'0')}M","BEST ${best[1]}M")
        hearts(c,105f,89f,4)
        txt(c,"COMBO x ${(score/40)%99}",width-95f,51f,15f,color("#E9874A"),Paint.Align.CENTER)
        button(c,70f,height-66f,48f,"LEFT",2); button(c,width-70f,height-66f,48f,"RIGHT",2)
    }

    private fun startBoss(){
        mode=Mode.BOSS;score=0;bossHp=300f;playerHp=100f;bossOrbs.clear();bossCd=.8f;slash=0f;dodge=0f
        playerX=width/2f;playerY=height*.67f;bossX=width/2f;bossY=height*.31f
    }
    private fun updateBoss(dt:Float){
        slash=max(0f,slash-dt);dodge=max(0f,dodge-dt);bossCd-=dt
        if(touching){ val dx=touchX-playerX;val dy=touchY-playerY;val d=hypot(dx,dy).coerceAtLeast(1f);playerX=(playerX+dx/d*170f*dt).coerceIn(35f,width-35f);playerY=(playerY+dy/d*170f*dt).coerceIn(150f,height-120f) }
        if(bossCd<=0){
            val ang=atan2(playerY-bossY,playerX-bossX); val n=if(bossHp<150)5 else 3
            for(i in 0 until n){ val a=ang+(i-(n-1)/2f)*.22f;bossOrbs+=Orb(bossX,bossY,cos(a)*(115f+rng.nextFloat()*40),sin(a)*(115f+rng.nextFloat()*40)) }
            bossCd=if(bossHp<100) .6f else .9f
        }
        val it=bossOrbs.iterator(); while(it.hasNext()){ val o=it.next();o.x+=o.vx*dt;o.y+=o.vy*dt;if(hypot(o.x-playerX,o.y-playerY)<16 && dodge<=0){playerHp-=12;it.remove()} else if(o.x<15||o.x>width-15||o.y<145||o.y>height-105)it.remove() }
        if(playerHp<=0){best[2]=max(best[2],score);toHub("WITCH QUEEN FAILED")}
    }
    private fun bossAttack(){
        if(mode!=Mode.BOSS)return;slash=.18f
        if(hypot(playerX-bossX,playerY-bossY)<155f){bossHp-=22f;score+=100;if(bossHp<=0){best[2]=max(best[2],score+1000);toHub("WITCH QUEEN CLEAR")}}
    }
    private fun bossDodge(){ if(mode==Mode.BOSS)dodge=.4f }
    private fun drawBoss(c:Canvas){
        rect(c,color("#191720"),0f,0f,width.toFloat(),height.toFloat()); tileFloor(c,18f,135f,width-18f,height-95f,30f,color("#343039"),color("#211F26"))
        stoneTile(c,0f,120f,width.toFloat(),24f,false);stoneTile(c,0f,height-100f,width.toFloat(),100f,false)
        repeat(5){i-> rect(c,color("#5A2E70"),20f+i*(width-40f)/4f,153f,26f+i*(width-40f)/4f,174f) }
        witch(c,bossX,bossY,1f)
        bossOrbs.forEach{o-> paint.color=color("#A54DCE");c.drawCircle(o.x,o.y,o.r+2,paint);paint.color=color("#F0A1FF");c.drawCircle(o.x,o.y,o.r-2,paint)}
        hero(c,playerX,playerY,.8f,false,slash>0)
        if(slash>0){ paint.style=Paint.Style.STROKE;paint.strokeWidth=5f;paint.color=color("#F28BC1");c.drawArc(playerX-28,playerY-28,playerX+28,playerY+28,-65f,130f,false,paint);paint.style=Paint.Style.FILL }
        portraitHud(c,"LV. 28","HP ${playerHp.toInt()}/100","TIME ${((300-score/5).coerceAtLeast(0))/60}:${((300-score/5).coerceAtLeast(0))%60).toString().padStart(2,'0')}")
        hearts(c,105f,89f,(playerHp/20).toInt().coerceIn(0,5))
        txt(c,"WITCH QUEEN MORGANA",width/2f,120f,16f,color("#E2B3F5"),Paint.Align.CENTER)
        outlineRect(c,44f,126f,width-44f,139f,color("#3B243F"),color("#D7AE5E"),2f);rect(c,color("#C34168"),48f,130f,48f+(width-96f)*(bossHp/300f),135f)
        dpad(c,68f,height-64f,48f)
        button(c,width-132f,height-70f,48f,"ATTACK",1);button(c,width-50f,height-70f,38f,"DODGE",2)
    }
    private fun witch(c:Canvas,x:Float,y:Float,s:Float){
        val u=3f*s
        px(c,color("#26172E"),x-9*u,y-1*u,18*u,14*u);px(c,color("#4C2A61"),x-7*u,y+4*u,14*u,10*u);px(c,color("#D4A54D"),x-1*u,y+2*u,2*u,10*u)
        px(c,color("#EAC6BE"),x-4*u,y-10*u,8*u,7*u);px(c,color("#221628"),x-6*u,y-13*u,12*u,5*u);px(c,color("#68417F"),x-7*u,y-15*u,14*u,3*u)
        px(c,color("#D8A742"),x-3*u,y-17*u,2*u,4*u);px(c,color("#D8A742"),x+1*u,y-17*u,2*u,4*u)
        px(c,color("#7F6841"),x-11*u,y-11*u,2*u,24*u); paint.color=color("#AE5FD5");c.drawCircle(x-10*u,y-13*u,4*u,paint)
    }
    private fun dpad(c:Canvas,cx:Float,cy:Float,r:Float){
        paint.color=color("#D7AE5E");c.drawCircle(cx,cy,r,paint);paint.color=color("#1C2431");c.drawCircle(cx,cy,r-4,paint)
        txt(c,"+",cx,cy+12f,42f,color("#C9CBD2"),Paint.Align.CENTER)
    }

    private fun startSwarm(){ mode=Mode.SWARM;score=0;wave=1;swarmX=width/2f;swarmY=height/2f;swarmHp=100f;swarmSpawn=.2f;autoCd=0f;mobs.clear();drops.clear();wormPhase=0f }
    private fun updateSwarm(dt:Float){
        wormPhase+=dt
        if(touching){ val dx=touchX-swarmX;val dy=touchY-swarmY;val d=hypot(dx,dy).coerceAtLeast(1f);swarmX=(swarmX+dx/d*190f*dt).coerceIn(22f,width-22f);swarmY=(swarmY+dy/d*190f*dt).coerceIn(120f,height-92f) }
        swarmSpawn-=dt;autoCd-=dt;score+=(dt*20).toInt()+1;wave=1+score/350
        if(swarmSpawn<=0){ val edge=rng.nextInt(4);val x=if(edge<2) if(edge==0)15f else width-15f else rng.nextFloat()*width;val y=if(edge>=2) if(edge==2)130f else height-100f else 130f+rng.nextFloat()*(height-230f);mobs+=Mob(x,y,45f+rng.nextFloat()*45f,if(rng.nextInt(5)==0)2 else 1,rng.nextInt(2));swarmSpawn=max(.12f,.62f-score/2600f) }
        mobs.forEach{m-> val dx=swarmX-m.x;val dy=swarmY-m.y;val d=hypot(dx,dy).coerceAtLeast(1f);m.x+=dx/d*m.speed*dt;m.y+=dy/d*m.speed*dt;if(d<18)swarmHp-=22f*dt }
        if(autoCd<=0 && mobs.isNotEmpty()){ val n=mobs.minByOrNull{hypot(it.x-swarmX,it.y-swarmY)}!!;n.hp--;if(n.hp<=0){drops+=PointF(n.x,n.y);mobs.remove(n);score+=18};autoCd=.18f }
        drops.removeAll{hypot(it.x-swarmX,it.y-swarmY)<18f}
        if(swarmHp<=0){best[3]=max(best[3],score);toHub("BLOOD CELL  $score")}
    }
    private fun drawSwarm(c:Canvas){
        tileFloor(c,0f,0f,width.toFloat(),height.toFloat(),24f,color("#35222D"),color("#241920"))
        repeat(18){i-> paint.color=if(i%2==0)color("#5B2539") else color("#6B2B45");c.drawCircle((i*61%width).toFloat(),(i*97%height).toFloat(),7f+(i%3)*3,paint) }
        val baseX=width-42f; val baseY=165f+(sin(wormPhase*.8f)+1)*70f
        repeat(7){i-> val yy=baseY+i*26f; paint.color=color("#654161");c.drawCircle(baseX-(i%2)*5,yy,16f,paint);paint.color=color("#A54A63");c.drawCircle(baseX-(i%2)*5,yy,5f,paint)}
        paint.color=color("#7A4258");c.drawCircle(baseX,baseY-18,19f,paint);px(c,color("#E8B3B0"),baseX-10,baseY-20,20f,3f)
        mobs.forEach{m->if(m.kind==0)slime(c,m.x,m.y,.65f) else bat(c,m.x,m.y,.65f)}
        drops.forEach{gem(c,it.x,it.y)}
        hero(c,swarmX,swarmY,.72f,false,true)
        mobs.minByOrNull{hypot(it.x-swarmX,it.y-swarmY)}?.let{n-> rect(c,color("#D94B75"),min(swarmX,n.x),min(swarmY,n.y),max(swarmX,n.x)+2,max(swarmY,n.y)+2) }
        portraitHud(c,"HP","${swarmHp.toInt()}/100","EXP ${(score%100)}%")
        txt(c,"WAVE $wave",105f,105f,14f,color("#F2CA5C"));txt(c,"COMBO x ${(score/20)%999}",width-95f,35f,14f,color("#E9874A"),Paint.Align.CENTER)
        txt(c,"${(score/60).toString().padStart(2,'0')}:${(score%60).toString().padStart(2,'0')}",width/2f,105f,16f,Color.WHITE,Paint.Align.CENTER)
        dpad(c,64f,height-62f,48f)
        button(c,width-58f,height-68f,45f,"AUTO",1)
        repeat(3){i-> val l=120f+i*64f;panel(c,l,height-92f,l+54,height-38f);val cols=intArrayOf(color("#C93E5C"),color("#7542A1"),color("#D46A35"));rect(c,cols[i],l+9,height-82f,l+45,height-48f);txt(c,(i+1).toString(),l+27,height-57f,14f,Color.WHITE,Paint.Align.CENTER)}
    }

    private fun drawBanner(c:Canvas,s:String){ panel(c,width*.18f,height*.43f,width*.82f,height*.56f);txt(c,s,width/2f,height*.51f,18f,color("#F2CA5C"),Paint.Align.CENTER) }
    private fun toHub(s:String){mode=Mode.HUB;msg=s;msgTimer=1.5f;touching=false}

    override fun onTouchEvent(e: MotionEvent): Boolean {
        touchX=e.x;touchY=e.y
        when(e.actionMasked){
            MotionEvent.ACTION_DOWN->{
                touching=true
                when(mode){
                    Mode.HUB->{
                        val doorW=(width-50f)/2f; val doorH=(height-190f)/2f
                        val col=if(e.x<width/2f)0 else 1; val row=if(e.y<105f+doorH+10f)0 else 1; val idx=row*2+col
                        if(e.y>100f && e.y<height-85f){ when(idx){0->startRunner();1->startJumper();2->startBoss();3->startSwarm()} }
                    }
                    Mode.RUNNER->{ if(e.x>width*.5f && runnerY==0f)runnerVy=-540f else runnerSpeed+=55f }
                    Mode.BOSS->{ if(e.y>height-135f && e.x>width-180f){ if(e.x<width-90f)bossAttack() else bossDodge() } }
                    else->{}
                }
            }
            MotionEvent.ACTION_UP,MotionEvent.ACTION_CANCEL->touching=false
        }
        return true
    }
}
