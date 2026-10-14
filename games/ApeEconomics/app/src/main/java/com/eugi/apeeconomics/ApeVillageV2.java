package com.eugi.apeeconomics;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.Random;

/**
 * APE ECONOMICS 2.0 - Level-hub vertical slice.
 * Fully pixel-rendered, asset-independent playable village with a small economic loop.
 * The legacy v0.15 renderer is preserved in the repository but never instantiated.
 */
public class ApeVillageV2 extends View {
    private static final int W=240, H=432, MAP=704, WORLD_TOP=63, WORLD_BOTTOM=374;
    private static final float ZOOM=.57f;
    private static final int CREAM=0xfff7eac6, INK=0xff392f38, GREEN=0xff4b8e52;
    private static final int DARKGREEN=0xff305a48, LIME=0xff91c85e, GOLD=0xffe8bc57;
    private static final int BROWN=0xff755344, PINK=0xffe58b88, BLUE=0xff69b7c3;
    private final Paint p=new Paint();
    private final Paint smooth=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random rng=new Random(6721L);
    private final SharedPreferences save;
    private final Bitmap frame=Bitmap.createBitmap(W,H,Bitmap.Config.ARGB_8888);
    private Bitmap terrain;
    private Bitmap[] apeSprites;
    private Bitmap villageLogo;
    private final Rect source = new Rect();
    private final RectF destination = new RectF();
    private final ArrayList<Speck> flecks=new ArrayList<>();
    private final ArrayList<Monkey> monkeys=new ArrayList<>();
    private final Location[] buildings={
            new Location("PLANTATION",162,266,0),
            new Location("MARKET",513,568,1),
            new Location("TOWN HALL",345,222,2),
            new Location("BANK",514,273,3)
    };
    private final String[] worldNames={
            "THE FIRST BANANA", "MERCHANT ISLES", "CAPITAL OF APES",
            "THE STOCK JUNGLE", "REVOLUTION RIDGE"
    };
    private final String[] worldSub={
            "LEVEL 0  /  TUTORIAL", "LEVEL 1  /  ECONOMY", "LEVEL 2  /  POLITICS",
            "LEVEL 3  /  FINANCE", "LEVEL 4  /  REVOLUTION"
    };
    private final String[] goals={
            "FIND THE PLANTATION", "HARVEST YOUR FIRST BANANAS",
            "FIND THE MARKET", "SELL 8 BANANAS AT THE MARKET",
            "FIND THE TOWN HALL", "DONATE 10 COINS TO THE VILLAGE"
    };
    private final String[] hints={
            "WALK TO THE GLOWING STAR", "STAND CLOSE + TAP ACT",
            "FOLLOW THE GOLDEN STAR", "STAND CLOSE + TAP ACT",
            "FOLLOW THE GOLDEN STAR", "STAND CLOSE + TAP ACT"
    };
    private static final int HUB=0, PLAY=1, CLEAR=2;
    private int screen=HUB, selected=0, quest=0, bananas=0, coins=0, influence=0;
    private int day=1, price=2, bankBalance=0;
    private boolean cleared, moveTouch, joystickTouch;
    private float playerX=345, playerY=311, camX,camY;
    private float walkX,walkY,targetX,targetY, faceX=0,faceY=1;
    private float worldTime,animationTime,toastAge=0, harvestCooldown=0, eventAge=0;
    private float touchStartX,touchStartY;
    private String toast="", event="";
    private long lastTime=SystemClock.uptimeMillis();
    private long dayStarted=lastTime, incomeLast=lastTime;
    private final Typeface font=Typeface.create("monospace", Typeface.BOLD);

    private static class Location {
        final String name;final float x,y;final int type;
        Location(String s,float a,float b,int t){name=s;x=a;y=b;type=t;}
    }
    private static class Monkey {
        float x,y,fromX,fromY,toX,toY,phase;int color,job;
        Monkey(float x,float y,int col,int role){this.x=x;this.y=y;this.fromX=x;this.fromY=y;color=col;job=role;}
    }
    private static class Speck {
        float x,y,life,dx,dy;int color;
        Speck(float a,float b,float vx,float vy,int c){x=a;y=b;dx=vx;dy=vy;color=c;life=.5f;}
    }

    public ApeVillageV2(Context context){
        super(context);
        save=context.getSharedPreferences("ape_economics_v2",Context.MODE_PRIVATE);
        cleared=save.getBoolean("tutorial_cleared",false);
        p.setAntiAlias(false);
        p.setFilterBitmap(false);
        p.setTypeface(font);
        setLayerType(View.LAYER_TYPE_SOFTWARE,null);
        int worldId=getResources().getIdentifier("ape_v23_world","drawable",getContext().getPackageName());
        if(worldId!=0) terrain=BitmapFactory.decodeResource(getResources(),worldId);
        if(terrain==null)createWorld();
        villageLogo=BitmapFactory.decodeResource(getResources(),R.drawable.ape_logo);
        apeSprites=new Bitmap[]{
          BitmapFactory.decodeResource(getResources(),R.drawable.ape_worker),
          BitmapFactory.decodeResource(getResources(),R.drawable.ape_politician),
          BitmapFactory.decodeResource(getResources(),R.drawable.ape_banker),
          BitmapFactory.decodeResource(getResources(),R.drawable.ape_trader)
        };
        resetMonkeys();
        setFocusable(true);
        setHapticFeedbackEnabled(true);
    }
    public boolean handleBack(){
        if(screen==PLAY || screen==CLEAR){screen=HUB;resetTouch();invalidate();return true;}
        return false;
    }
    public void pauseGame(){resetTouch();lastTime=SystemClock.uptimeMillis();}
    private void resetTouch(){moveTouch=false;joystickTouch=false;walkX=0;walkY=0;}
    private void say(String s){toast=s;toastAge=2.6f;performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);}
    private static float clamp(float v,float l,float h){return Math.max(l,Math.min(h,v));}
    private static int rgb(int r,int g,int b){return Color.rgb(r,g,b);}
    private void color(int c){p.setColor(c);p.setAlpha(255);p.setStyle(Paint.Style.FILL);}
    private void rect(Canvas c,float x,float y,float w,float h,int cl){color(cl);c.drawRect(x,y,x+w,y+h,p);}
    private void pixel(Canvas c,float x,float y,int cl){rect(c,x,y,1,1,cl);}
    private void stroke(Canvas c,float x,float y,float w,float h,int cl){
        color(cl);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1f);
        c.drawRect(x+.5f,y+.5f,x+w-.5f,y+h-.5f,p);p.setStyle(Paint.Style.FILL);
    }
    private void label(Canvas c,String s,float x,float y,float size,int cl,Paint.Align align){
        color(cl);p.setTypeface(font);p.setTextSize(size);p.setTextAlign(align);c.drawText(s,x,y,p);
    }
    private void txt(Canvas c,String s,float x,float y,float size,int cl){
        label(c,s,x,y,size,cl,Paint.Align.CENTER);
    }
    private void shadowText(Canvas c,String s,float x,float y,float size,int cl){
        txt(c,s,x+1,y+1,size,INK);txt(c,s,x,y,size,cl);
    }
    private void panel(Canvas c,int x,int y,int w,int h,int bg){
        rect(c,x+2,y+3,w,h,0xff273b39);
        rect(c,x,y,w,h,INK);
        rect(c,x+2,y+2,w-4,h-4,bg);
        rect(c,x+4,y+4,w-8,1,0xfff7ecca);
        rect(c,x+4,y+h-6,w-8,2,0xffbeaa86);
    }
    private void button(Canvas c,int x,int y,int w,int h,String title,int bg){
        rect(c,x+1,y+3,w,h,INK);
        rect(c,x,y,w,h,INK);
        rect(c,x+2,y+2,w-4,h-4,bg);
        rect(c,x+4,y+4,w-8,2,0xfff6edbc);
        txt(c,title,x+w/2f,y+h/2f+4,9f,INK);
    }

    private void startLevel(){
        screen=PLAY;
        quest=0;
        bananas=4;coins=0;influence=0;bankBalance=0;price=2;
        playerX=350;playerY=302;faceX=0;faceY=1;camX=0;camY=0;
        day=1;dayStarted=SystemClock.uptimeMillis();incomeLast=dayStarted;
        resetTouch();resetMonkeys();flecks.clear();
        say(cleared?"WELCOME BACK, MAYOR!":"LEVEL 0: THE FIRST BANANA");
    }
    private void resetMonkeys(){
        monkeys.clear();
        monkeys.add(new Monkey(191,293,0xffc87a59,0));
        monkeys.add(new Monkey(331,277,0xffd7a66d,1));
        monkeys.add(new Monkey(505,293,0xff8b6764,2));
        monkeys.add(new Monkey(424,539,0xffcca37a,3));
        monkeys.add(new Monkey(278,552,0xffbd9573,4));
        monkeys.add(new Monkey(603,553,0xffb28b5f,5));
        for(Monkey m:monkeys){m.fromX=m.x;m.fromY=m.y;m.toX=m.x;m.toY=m.y;m.phase=(m.job*.15f);}
    }

    private void createWorld(){
        terrain=Bitmap.createBitmap(MAP,MAP,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(terrain);
        Random drawRng=new Random(22147L);
        rect(c,0,0,MAP,MAP,0xff85bc68);
        for(int y=0;y<44;y++)for(int x=0;x<44;x++){
            int xx=x*16,yy=y*16;
            rect(c,xx,yy,16,16,((x*7+y*13)%9==0)?0xff80b763:0xff87bd6d);
            if((x*17+y*7)%5==0){
                rect(c,xx+2,yy+4,2,3,0xff6eaa5c);
                pixel(c,xx+5,yy+6,0xffb0d27e);
            }
            if((x*11+y*5)%7==0)pixel(c,xx+12,yy+11,0xffa5ca79);
        }
        // Tree-filled forest edges and a tame southern grove.
        for(int y=32;y<704;y+=31){
            for(int x=12;x<704;x+=28){
                boolean edge=x<83 || x>628 || y<75 || y>614;
                boolean south=y>415 && ((x+y)/31)%4==0;
                if((edge||south) && drawRng.nextFloat()>.22f &&
                    !nearBuilding(x,y) && !(y>333&&y<397) &&
                    !(x>294&&x<389&&y>285&&y<590)){
                    tree(c,x+drawRng.nextInt(8),y+drawRng.nextInt(9),drawRng.nextInt(3));
                }
            }
        }
        // Footpaths: broad, warm, asymmetric and flecked.
        path(c,103,274,526,274,21);
        path(c,349,158,349,593,21);
        path(c,167,274,167,225,17);
        path(c,500,274,500,230,17);
        path(c,349,470,511,470,19);
        path(c,342,566,532,566,14);
        path(c,207,553,347,553,17);
        // The river bisects the island. Bridge is the connective spine.
        rect(c,0,361,MAP,43,0xff357e9c);
        rect(c,0,366,MAP,30,0xff4dabc1);
        for(int i=0;i<75;i++){
            int x=(i*79+11)%704;int y=369+(i*17)%27;
            rect(c,x,y,6,2,0xffb3e6da);
        }
        for(int y=348;y<366;y+=4)for(int x=0;x<704;x+=16)rect(c,x+(y%6),y,8,2,0xffd5e0a4);
        for(int y=400;y<408;y+=3)for(int x=0;x<704;x+=16)rect(c,x+(y%5),y,7,2,0xffd5e0a4);
        bridge(c,321,352,58,60);
        // Buildings + their clutter are hand authored in shared 16px-grid geometry.
        building(c,106,158,111,82,0,"BANANA","FARM");
        building(c,441,170,118,83,1,"TRADE","MARKET");
        building(c,290,111,119,84,2,"TOWN","HALL");
        building(c,443,419,115,63,3,"BANANA","BANK");
        // Fountains, seats, fences, farmland, homes and lanterns.
        fountain(c,351,291);
        for(int i=0;i<5;i++){
            int fy=210+i*12;
            fence(c,101,fy);
            fence(c,219,fy);
        }
        for(int i=0;i<6;i++){
            bush(c,247+i*12,199+(i%2)*3);
            bush(c,408+i*10,317+(i%3)*5);
        }
        for(int i=0;i<6;i++){
            crop(c,115+i*16,306);
            crop(c,115+i*16,323);
        }
        for(int i=0;i<15;i++){
            int x=110+(i*41)%477;
            int y=417+(i*29)%194;
            if(Math.abs(x-349)>29 && !nearBuilding(x,y))flower(c,x,y,i);
        }
        house(c,150,458,0xffdc856b);
        house(c,214,483,0xffb58ad0);
        house(c,570,519,0xff6f9fb1);
        well(c,304,530);
        sign(c,400,337,"TOWN");
        sign(c,287,430,"SOUTH");
        for(int i=0;i<9;i++){
            lantern(c,126+i*53,340);
        }
    }
    private boolean nearBuilding(float x,float y){
        return x>95&&x<231&&y>135&&y<248 ||
            x>429&&x<575&&y>144&&y<265 ||
            x>280&&x<422&&y>84&&y<202 ||
            x>432&&x<572&&y>407&&y<493;
    }
    private void path(Canvas c,int x1,int y1,int x2,int y2,int width){
        color(0xffb79971);p.setStrokeWidth(width+4);p.setStrokeCap(Paint.Cap.SQUARE);
        c.drawLine(x1,y1,x2,y2,p);
        color(0xffd8bb86);p.setStrokeWidth(width);c.drawLine(x1,y1,x2,y2,p);
        int dx=x2-x1,dy=y2-y1,len=Math.max(Math.abs(dx),Math.abs(dy));
        for(int d=4;d<len;d+=14){
            int x=x1+(dx*d/Math.max(len,1)),y=y1+(dy*d/Math.max(len,1));
            rect(c,x+(d%7)-3,y+(d%5)-2,3,2,0xffc5a679);
        }
    }
    private void tree(Canvas c,int x,int y,int v){
        rect(c,x-7,y+7,23,7,0x66566d40);
        rect(c,x+1,y-4,5,19,0xff78553b);
        rect(c,x+2,y-6,2,14,0xffa27950);
        rect(c,x-8,y-19,25,23,0xff2d644c);
        rect(c,x-11,y-15,31,14,0xff377c51);
        rect(c,x-5,y-24,20,21,0xff478d52);
        rect(c,x-2,y-25,13,12,0xff73b766);
        rect(c,x-9,y-13,7,5,0xff67ac5e);
        rect(c,x+9,y-11,8,5,0xff2e7451);
        if(v==2){pixel(c,x+2,y-21,0xfff5c8a0);rect(c,x+6,y-16,3,3,0xfff6d8a4);}
    }
    private void bush(Canvas c,int x,int y){
        rect(c,x-7,y,16,5,0xff507b53);
        rect(c,x-8,y-5,16,9,0xff347c51);
        rect(c,x-4,y-9,10,8,0xff77af59);
        rect(c,x+2,y-7,6,4,0xffa8c568);
    }
    private void crop(Canvas c,int x,int y){
        rect(c,x-6,y-6,13,12,0xff996c50);
        for(int j=0;j<3;j++){rect(c,x-5+j*5,y-8,3,12,0xff5f9647);
            pixel(c,x-4+j*5,y-10,0xffd4bf57);}
    }
    private void flower(Canvas c,int x,int y,int index){
        rect(c,x,y-1,2,5,0xff43854f);
        rect(c,x-2,y-4,6,4,index%3==0?PINK:CREAM);
        rect(c,x,y-3,2,2,GOLD);
    }
    private void fence(Canvas c,int x,int y){
        rect(c,x,y-7,4,13,0xff77533c);
        rect(c,x-1,y-7,6,4,0xffb79061);
    }
    private void bridge(Canvas c,int x,int y,int w,int h){
        rect(c,x-4,y-4,w+8,h+8,0xff5b544b);
        rect(c,x,y,w,h,0xff956c4d);
        for(int j=0;j<5;j++){
            rect(c,x,y+j*12,w,10,0xffcf9b65);
            rect(c,x+2,y+j*12+2,w-4,2,0xffe9c184);
            rect(c,x+4+(j%2)*8,y+j*12+6,3,2,0xff846147);
        }
        rect(c,x-5,y-8,6,h+15,0xff785743);
        rect(c,x+w-1,y-8,6,h+15,0xff785743);
    }
    private void building(Canvas c,int x,int y,int w,int h,int type,String top,String bottom){
        // Cast shadows and thick walnut outlines. Building walls are blocked separately.
        rect(c,x+3,y+9,w,h,0x66583b34);
        rect(c,x+8,y+29,w-16,h-30,0xff493e40);
        rect(c,x+11,y+31,w-22,h-35,type==2?0xffc9d6be:0xffdfb98b);
        for(int xx=x+17;xx<x+w-16;xx+=12){
            for(int yy=y+36;yy<y+h-12;yy+=10){
                pixel(c,xx+(yy%7),yy,0xffc3a27e);
            }
        }
        int roof=type==0?0xffbd695c:type==1?0xff547e87:type==2?0xff7281a0:0xff836d9a;
        int roofDark=type==0?0xff89534b:type==1?0xff345e6e:type==2?0xff505b80:0xff594f79;
        rect(c,x,y+17,w,34,0xff403b44);
        rect(c,x+3,y+11,w-6,37,roofDark);
        for(int i=0;i<6;i++){
            int inset=13-i*2;
            rect(c,x+inset,y+5+i*5,w-2*inset,5,roof);
            rect(c,x+inset+2,y+5+i*5,w-2*inset-4,1,0x66f3dcb2);
        }
        for(int i=0;i<7;i++)rect(c,x+8+i*14,y+35+(i%2)*2,7,2,roofDark);
        // Windows with bright little shutters and reflected sky.
        for(int offset:new int[]{18,w-35}){
            rect(c,x+offset-2,y+51,19,20,BROWN);
            rect(c,x+offset,y+53,15,14,0xff4d9caa);
            rect(c,x+offset+2,y+55,4,5,CREAM);
            rect(c,x+offset+8,y+53,2,14,INK);
            rect(c,x+offset,y+60,15,2,INK);
            rect(c,x+offset-2,y+66,19,4,0xffc39465);
        }
        rect(c,x+w/2-10,y+h-30,20,30,0xff58463f);
        rect(c,x+w/2-7,y+h-27,14,26,0xff9b674e);
        rect(c,x+w/2+3,y+h-15,2,2,GOLD);
        rect(c,x+w/2-17,y+50,34,13,INK);
        rect(c,x+w/2-16,y+51,32,11,GOLD);
        txt(c,top,x+w/2,y+59,6.5f,INK);
        txt(c,bottom,x+w/2,y+65,6.2f,INK);
        // Flag gives silhouettes a distinct role.
        rect(c,x+w/2-1,y-7,2,22,0xff6a5648);
        rect(c,x+w/2+1,y-7,13,9,type==0?0xffcbe379:type==1?0xff80d6d8:0xfff3db9c);
        rect(c,x+w/2+3,y-5,6,2,roofDark);
    }
    private void fountain(Canvas c,int x,int y){
        rect(c,x-22,y-5,46,25,0xff667c7c);
        rect(c,x-18,y-8,38,20,0xffa5d8ce);
        rect(c,x-13,y-6,28,13,0xff65abc0);
        rect(c,x-3,y-19,8,14,0xffdcd0a6);
        rect(c,x-7,y-19,16,4,0xfff4dfa6);
        pixel(c,x+3,y-12,CREAM);
    }
    private void house(Canvas c,int x,int y,int roof){
        rect(c,x,y+13,41,31,INK);rect(c,x+3,y+14,35,26,0xffe0bf92);
        for(int i=0;i<5;i++)rect(c,x+8-i*2,y+2+i*4,25+i*4,4,roof);
        rect(c,x+17,y+25,9,15,BROWN);
        rect(c,x+6,y+22,8,8,BLUE);rect(c,x+30,y+22,7,8,BLUE);
    }
    private void well(Canvas c,int x,int y){
        rect(c,x-14,y-8,29,18,INK);rect(c,x-10,y-9,22,11,0xff7bacc0);
        rect(c,x-9,y-7,17,7,0xff325b76);
        rect(c,x-15,y+4,31,8,0xffc4b39a);
        rect(c,x-10,y-19,4,17,BROWN);rect(c,x+8,y-19,4,17,BROWN);
        rect(c,x-13,y-23,28,6,0xffb17a60);
    }
    private void lantern(Canvas c,int x,int y){
        if(x>305 && x<390)return;
        rect(c,x,y-8,2,14,INK);rect(c,x-3,y-14,8,7,INK);
        rect(c,x-2,y-13,6,5,0xfff9dc84);
    }
    private void sign(Canvas c,int x,int y,String name){
        rect(c,x,y,3,18,BROWN);rect(c,x-15,y,33,12,INK);
        rect(c,x-14,y+1,31,10,0xffdab779);txt(c,name,x+1,y+9,7f,INK);
    }

    private void drawBanana(Canvas c,float x,float y){
        rect(c,x-4,y-5,4,10,0xffdcaa37);
        rect(c,x,y+1,5,4,0xffdcaa37);
        rect(c,x-3,y-5,3,8,0xfff5d06a);
        rect(c,x-1,y+2,5,2,0xfff5d06a);
        pixel(c,x-4,y-6,INK);
    }
    /** Real alpha-transparent character art from the original game, not block people. */
    private void drawMonkey(Canvas c,float x,float y,int shade,boolean hero,int direction,float t){
        Bitmap img=apeSprites[hero?0:(direction%apeSprites.length)];
        if(img==null)return;
        c.save();c.translate(Math.round(x),Math.round(y));
        float bob=1.4f*(float)Math.sin(t*7f);
        rect(c,-13,8,26,5,0x66513732);
        // Feet under the character portrait give the sprite actual walking movement.
        rect(c,-12,1+bob,10,12,hero?0xff476c6a:0xff665047);
        rect(c,2,-bob,10,12,hero?0xff476c6a:0xff665047);
        rect(c,-12,10+bob,10,4,0xff403338);
        rect(c,2,10-bob,10,4,0xff403338);
        if(hero&&faceX<-.35f){c.scale(-1,1);}
        p.setFilterBitmap(false);
        color(Color.WHITE);
        c.drawBitmap(img,null,new RectF(-21,-42+bob,21,7+bob),p);
        if(hero){
            rect(c,-12,-3+bob,24,4,0xff51866f);
            rect(c,-4,-3+bob,8,3,0xffdbb45f);
        }
        c.restore();
    }
    private void resetObjectiveFromLocation(){
        if(quest>=6)return;
        int target=(quest<=1)?0:(quest<=3)?1:2;
        Location loc=buildings[target];
        float dx=loc.x-playerX,dy=loc.y-playerY;
        if(Math.sqrt(dx*dx+dy*dy)<47f && quest%2==0){
            quest++;say("NICE! NOW PRESS ACT");
        }
    }
    private boolean near(Location l){
        float dx=l.x-playerX,dy=l.y-playerY;return dx*dx+dy*dy<50f*50f;
    }
    private Location closest(){
        Location found=null;float best=50f*50f;
        for(Location l:buildings){float dx=l.x-playerX,dy=l.y-playerY;
            float d=dx*dx+dy*dy;if(d<best){best=d;found=l;}}
        return found;
    }
    private void action(){
        Location near=closest();
        if(near==null){say("MOVE CLOSER TO A BUILDING");return;}
        if(near.type==0){
            if(harvestCooldown>0){say("THE BANANAS ARE STILL GROWING");return;}
            bananas+=12;harvestCooldown=4.0f;
            spawnParticles(near.x,near.y,0xfff2d06b);
            say("+12 BANANAS! A FINE HARVEST");
            if(quest==1)quest=2;
        }else if(near.type==1){
            if(bananas<8){say("MARKET NEEDS 8 BANANAS");return;}
            bananas-=8;coins+=8*price;
            spawnParticles(near.x,near.y,GOLD);
            say("SOLD! +"+(8*price)+" COINS");
            if(quest==3)quest=4;
        }else if(near.type==2){
            if(coins<10){say("TOWN HALL NEEDS 10 COINS");return;}
            coins-=10;influence+=1;spawnParticles(near.x,near.y,PINK);
            say("THE VILLAGE TRUSTS YOU!");
            if(quest==5){
                quest=6;cleared=true;save.edit().putBoolean("tutorial_cleared",true).apply();
                screen=CLEAR;resetTouch();
            }
        }else if(near.type==3){
            if(coins<10){say("THE BANK TAKES 10 COINS");return;}
            coins-=10;bankBalance+=10;say("DEPOSITED! EARN INTEREST DAILY");
            spawnParticles(near.x,near.y,BLUE);
        }
    }
    private void spawnParticles(float x,float y,int color){
        for(int i=0;i<16;i++)flecks.add(new Speck(x,y,(rng.nextFloat()-.5f)*100,
            (rng.nextFloat()-.55f)*100,color));
    }
    private boolean solid(float x,float y){
        if(y>355&&y<431&&(x<317||x>381))return true;
        if(x>91&&x<232&&y>111&&y<259)return true;
        if(x>268&&x<421&&y>66&&y<217)return true;
        if(x>445&&x<583&&y>106&&y<267)return true;
        if(x>437&&x<590&&y>424&&y<563)return true;
        return false;
    }

    private void animate(float dt){
        animationTime+=dt;
        if(screen!=PLAY)return;
        worldTime+=dt;
        harvestCooldown=Math.max(0,harvestCooldown-dt);
        toastAge=Math.max(0,toastAge-dt);
        eventAge=Math.max(0,eventAge-dt);
        long now=SystemClock.uptimeMillis();
        if(now-incomeLast>13000){
            bananas+=2;incomeLast=now;say("YOUR WORKERS MADE +2 BANANAS");
        }
        if(now-dayStarted>65000){
            day++;dayStarted=now;price=2+(day%3==0?1:0);
            coins+=bankBalance/5;
            event="DAY "+day+"  /  MARKET PRICE: "+price;
            eventAge=3f;
        }
        float dx=walkX,dy=walkY;
        if(moveTouch){
            float goX=targetX,goY=targetY;
            // Auto-route via the real bridge instead of walking into the river.
            boolean crossing=(targetY>433&&playerY<434)||(targetY<350&&playerY>349);
            if(crossing){
                if(Math.abs(playerX-350f)>17f){goX=350f;goY=playerY;}
                else {goX=350f;goY=targetY>433?447f:339f;}
            }
            float tx=goX-playerX,ty=goY-playerY;
            float dist=(float)Math.sqrt(tx*tx+ty*ty);
            if(dist<4){
                if(!crossing){moveTouch=false;dx=dy=0;}
                else {dx=0;dy=targetY>433?1:-1;}
            }else{dx=tx/dist;dy=ty/dist;}
        }
        float mag=(float)Math.sqrt(dx*dx+dy*dy);
        if(mag>.1f){
            dx/=mag;dy/=mag;
            float speed=91f*dt;
            float nextX=clamp(playerX+dx*speed,17,MAP-17);
            float nextY=clamp(playerY+dy*speed,34,MAP-18);
            // River and solid building silhouettes are part of navigation.
            if(!solid(nextX,nextY)){
                playerX=nextX;playerY=nextY;
            }
            faceX=dx;faceY=dy;
        }
        resetObjectiveFromLocation();
        camX=clamp(playerX-W/(ZOOM*2),0,MAP-W/ZOOM);
        camY=clamp(playerY-(WORLD_BOTTOM-WORLD_TOP)*.57f/ZOOM,0,MAP-(WORLD_BOTTOM-WORLD_TOP)/ZOOM);
        for(Monkey m:monkeys){
            float speed=.16f+(m.job%3)*.018f;
            m.phase+=dt*speed;
            float rx= (float)Math.sin(m.phase*6.28318f);
            float ry= (float)Math.cos(m.phase*6.28318f);
            m.x=m.fromX+rx*(10+m.job*3);
            m.y=m.fromY+ry*(5+m.job*2);
        }
        for(int i=flecks.size()-1;i>=0;i--){
            Speck s=flecks.get(i);
            s.x+=s.dx*dt;s.y+=s.dy*dt;s.dy+=100*dt;s.life-=dt;
            if(s.life<=0)flecks.remove(i);
        }
    }

    private void world(Canvas c){
        color(0xff537e65);c.drawRect(0,WORLD_TOP,W,WORLD_BOTTOM,p);
        c.save();c.clipRect(0,WORLD_TOP,W,WORLD_BOTTOM);
        c.translate(0,WORLD_TOP);
        c.scale(ZOOM,ZOOM);
        c.translate(-camX,-camY);
        c.drawBitmap(terrain,0,0,p);
        // Flowing water gleams above the static tilemap.
        for(int i=0;i<34;i++){
            float xx=(i*63+(animationTime*15)%63)%704;
            float yy=373+(i*11)%23;
            rect(c,xx,yy,5,1,0xffd2edda);
        }
        // A few cheerful details convey everyday industry.
        for(int i=0;i<4;i++){
            float bob=(float)Math.sin(animationTime*3+i)*2;
            drawBanana(c,117+i*24,285+bob);
        }
        // Behind sprites: contextual interaction markers / quest breadcrumb.
        if(quest<6){
            Location goal=buildings[(quest<=1)?0:(quest<=3)?1:2];
            float bob=(float)Math.sin(animationTime*4)*3;
            rect(c,goal.x-7,goal.y-34+bob,14,14,INK);
            rect(c,goal.x-6,goal.y-33+bob,12,12,GOLD);
            txt(c,"!",goal.x,goal.y-23+bob,10,INK);
        }
        // Painter's-order depth sort by feet-Y.
        ArrayList<Monkey> list=new ArrayList<>(monkeys);
        list.sort((a,b)->Float.compare(a.y,b.y));
        boolean drawn=false;
        for(Monkey m:list){
            if(!drawn && playerY<m.y){drawPlayer(c);drawn=true;}
            drawMonkey(c,m.x,m.y,m.color,false,m.job%4,animationTime+m.job*.25f);
            // Economically active residents advertise their work in actual sprites.
            if(m.job<4) drawThought(c,m);
        }
        if(!drawn)drawPlayer(c);
        for(Speck s:flecks)rect(c,s.x,s.y,2,2,s.color);
        c.restore();
    }
    private void drawThought(Canvas c,Monkey monkey){
        float x=monkey.x+26f, y=monkey.y-45f+
            (float)Math.sin(animationTime*2.4f+monkey.job)*2f;
        rect(c,x-9,y-12,28,24,0x66503230);
        rect(c,x-12,y-15,28,24,0xff38323a);
        rect(c,x-10,y-13,24,20,0xfff9e8c6);
        rect(c,x-4,y+7,5,5,0xfff9e8c6);
        rect(c,x-4,y+12,3,2,0xff38323a);
        switch(monkey.job){
            case 0: drawBanana(c,x+2,y-3);break;
            case 1:
                rect(c,x-2,y-10,11,15,0xffece2c2);
                rect(c,x-2,y-10,11,15,0xff536d84);
                rect(c,x,y-8,8,11,0xfff9e9c6);
                rect(c,x+2,y-5,5,2,0xff6b6867);
                rect(c,x+2,y-1,5,1,0xff6b6867);
                break;
            case 2:
                rect(c,x-3,y-10,13,13,0xffe1bc5c);
                txt(c,"$",x+3,y,13,0xff5d4637);
                break;
            default:
                rect(c,x-4,y-9,15,11,0xff9a6346);
                rect(c,x-2,y-7,11,7,0xffddb878);
                txt(c,"$",x+3,y-1,7,0xff59463b);
        }
    }
    private void drawPlayer(Canvas c){
        int facing=faceY<-.4?2:faceX<-.4?1:faceX>.4?3:0;
        drawMonkey(c,playerX,playerY,0xffbd8a65,true,facing,animationTime);
        Location spot=closest();
        if(spot!=null){
            float yy=playerY-27+(float)Math.sin(animationTime*5)*2;
            rect(c,playerX-22,yy-13,44,12,INK);
            rect(c,playerX-21,yy-12,42,10,CREAM);
            txt(c,"ACT",playerX,yy-4,8,INK);
        }
    }
    /** Makes distant objectives findable without giving the player a GPS menu. */
    private void questPointer(Canvas c){
        if(quest>=6)return;
        Location goal=buildings[(quest<=1)?0:(quest<=3)?1:2];
        float sx=(goal.x-camX)*ZOOM;
        float sy=(goal.y-camY)*ZOOM+WORLD_TOP;
        if(sx>=14&&sx<=W-14&&sy>=WORLD_TOP+18&&sy<=WORLD_BOTTOM-18)return;
        float bx=clamp(sx,14,W-14),by=clamp(sy,WORLD_TOP+18,WORLD_BOTTOM-18);
        rect(c,bx-11,by-9,22,18,INK);
        rect(c,bx-9,by-7,18,14,GOLD);
        String arrow=Math.abs(sx-bx)>Math.abs(sy-by)?(sx<0?"<":">"):(sy<WORLD_TOP?"^":"v");
        txt(c,arrow,bx,by+4,13,INK);
    }

    /** Pixel-carved mahogany-and-parchment inventory card with enamel corner studs. */
    private void woodenCard(Canvas c,float x,float y,float w,float h,int shade){
        rect(c,x+2,y+3,w,h,0xff292c31);
        rect(c,x,y,w,h,0xff49352f);
        rect(c,x+1,y+1,w-2,h-2,0xff8c6244);
        rect(c,x+3,y+3,w-6,h-6,0xffd6b985);
        rect(c,x+5,y+5,w-10,h-10,0xfff2e2bd);
        rect(c,x+5,y+5,w-10,2,0xfffff1ca);
        rect(c,x+5,y+h-8,w-10,2,0xffb79a70);
        for(int k=0;k<10;k++){
            float xx=x+7+((k*17+3)%(int)Math.max(1,w-15));
            float yy=y+8+((k*13+1)%(int)Math.max(1,h-17));
            rect(c,xx,yy,(k%3==0?2:1),1,0xffdcc99f);
        }
        for(int i=0;i<4;i++){
            float xx=x+(i%2==0?4:w-6);
            float yy=y+(i<2?4:h-6);
            rect(c,xx,yy,3,3,shade);
            rect(c,xx+1,yy,1,1,0xffffdda0);
        }
    }
    private void topHud(Canvas c){
        rect(c,0,0,W,WORLD_TOP,0xff48372d);
        rect(c,0,0,W,3,0xff7ea856);
        rect(c,0,WORLD_TOP-4,W,4,0xff302f35);
        if(villageLogo!=null){
            color(Color.WHITE);
            p.setFilterBitmap(false);
            c.drawBitmap(villageLogo,null,new RectF(1,5,69,52),p);
        }
        woodenCard(c,72,7,55,47,0xffad946a);
        woodenCard(c,130,7,52,47,0xffad946a);
        woodenCard(c,185,7,52,47,0xffad946a);
        drawBanana(c,84,25);
        txt(c,""+bananas,108,28,13,INK);
        txt(c,"BANANAS",100,43,6f,0xff765b46);
        txt(c,"$ "+coins,156,28,13,INK);
        txt(c,"COINS",156,43,6f,0xff765b46);
        txt(c,"DAY "+day,211,25,9.5f,INK);
        txt(c,"INF "+influence,211,38,8f,0xff765b46);
        rect(c,199,49,26,10,0xff526e50);
        txt(c,"HUB",212,56.5f,6.7f,CREAM);
        // Recessed parchment quest ribbon over the top of the environment.
        rect(c,5,WORLD_TOP+5,230,25,0x99504033);
        rect(c,6,WORLD_TOP+5,228,21,0xfff4e4bc);
        rect(c,8,WORLD_TOP+7,224,17,0xff725340);
        String head=quest<6?("QUEST "+Math.min(quest+1,6)+"/6  •  "+goals[quest]):"FREE PLAY: BUILD YOUR INFLUENCE";
        txt(c,head,120,WORLD_TOP+17,7f,CREAM);
        txt(c,quest<6?hints[quest]:"TRADE  •  HARVEST  •  SAVE",120,WORLD_TOP+24,5.4f,GOLD);
    }
    private void drawJoystick(Canvas c){
        rect(c,0,WORLD_BOTTOM,W,H-WORLD_BOTTOM,0xff46352c);
        rect(c,0,WORLD_BOTTOM,W,4,0xff926c42);
        txt(c,"MOVE",44,387,7,CREAM);
        txt(c,"INTERACT",197,387,7,CREAM);
        rect(c,6,390,75,35,0xff2e3236);
        rect(c,9,391,69,31,0xff577c65);
        rect(c,11,393,65,2,0xff8db381);
        txt(c,"<",25,411,17,CREAM);
        txt(c,"^",46,402,13,CREAM);
        txt(c,">",68,411,17,CREAM);
        txt(c,"v",46,420,13,CREAM);
        rect(c,170,390,63,35,0xff383138);
        rect(c,173,392,57,29,GOLD);
        rect(c,176,395,51,2,0xffffefbb);
        txt(c,"ACT",202,412,14,INK);
        Location l=closest();
        if(l!=null)txt(c,l.type==0?"HARVEST":l.type==1?"SELL":l.type==2?"DONATE":"DEPOSIT",201,420,6,INK);
        txt(c,"BANANA PRICE: $"+price+"   •   APES: 6",120,430,6f,CREAM);
    }
    private void alerts(Canvas c){
        if(toastAge>0){
            rect(c,8,342,224,25,INK);
            rect(c,10,344,220,21,CREAM);
            txt(c,toast,120,358,7.5f,INK);
        }else if(eventAge>0){
            rect(c,9,342,222,23,INK);
            rect(c,11,344,218,19,GOLD);
            txt(c,event,120,357,7.5f,INK);
        }
    }
    private void hub(Canvas c){
        rect(c,0,0,W,H,0xff2c4949);
        for(int yy=0;yy<H;yy+=16)for(int xx=0;xx<W;xx+=16){
            rect(c,xx+((yy/16)%2)*2,yy,15,15,((yy+xx)/16)%2==0?0xff365c59:0xff385654);
        }
        rect(c,0,0,W,62,INK);
        rect(c,4,4,232,54,0xff527b63);
        txt(c,"APE ECONOMICS",120,26,18,CREAM);
        txt(c,"THE ISLANDS OF AMBITION",120,45,8.5f,GOLD);
        txt(c,"WORLD SELECT",120,80,10,CREAM);

        // The hub uses a real authored map inset instead of abstract block art.
        rect(c,7,89,226,244,0xff2c7384);
        rect(c,11,93,218,235,0xff455e5d);
        if(terrain!=null){
            p.setFilterBitmap(false);
            color(Color.WHITE);
            c.drawBitmap(terrain,null,new RectF(13,95,227,325),p);
        }
        rect(c,11,93,218,4,0xffb7c582);
        rect(c,11,322,218,5,0xff45413d);
        rect(c,11,94,3,232,0xff45413d);
        rect(c,226,94,3,232,0xff45413d);
        drawHubNode(c,55,159,0, true);
        drawHubNode(c,177,197,1, false);
        drawHubNode(c,76,266,2, false);
        drawHubNode(c,170,293,3, false);
        // Fifth world appears as a future silhouette without a button.
        rect(c,110,109,22,10,INK);
        txt(c,"?",121,117,9,GOLD);
        rect(c,9,338,222,86,INK);
        rect(c,11,340,218,82,0xfff3e5c3);
        rect(c,14,343,212,3,0xffa47d5c);
        label(c,worldSub[selected],21,360,8.5f,0xff876044,Paint.Align.LEFT);
        label(c,worldNames[selected],21,378,12,INK,Paint.Align.LEFT);
        if(selected==0){
            button(c,22,385,196,30,cleared?"REPLAY LEVEL 0":"ENTER TUTORIAL",GOLD);
        }else{
            rect(c,21,385,196,30,0xffc8b993);
            stroke(c,21,385,196,30,0xff8a8069);
            txt(c,"COMING IN A FUTURE UPDATE",120,403,9,INK);
        }
    }
    private void drawHubNode(Canvas c,int x,int y,int n,boolean available){
        boolean chosen=n==selected;
        if(chosen){
            float v=(float)Math.sin(animationTime*4)*2;
            rect(c,x-19,y-17+v,39,37,CREAM);
            rect(c,x-17,y-15+v,35,33,0xffd7a650);
        }
        rect(c,x-14,y-12,29,27,INK);
        rect(c,x-12,y-10,25,23,available?0xffdbb44d:0xff7c8c87);
        if(available){
            // Tiny golden village.
            rect(c,x-5,y-2,11,10,0xffb97755);
            rect(c,x-7,y-6,15,5,0xff805c49);
            rect(c,x-1,y+3,3,5,CREAM);
        }else{
            rect(c,x-4,y-6,8,12,0xff414a51);
            rect(c,x-7,y-6,14,4,0xff414a51);
            txt(c,"?",x,y+8,10,CREAM);
        }
        txt(c,""+n,x,y+24,9,CREAM);
        if(n==0 && cleared){rect(c,x+9,y-17,8,8,GOLD);txt(c,"*",x+13,y-10,9,INK);}
    }
    private void completion(Canvas c){
        // Show live world under an atmospheric reward overlay.
        color(0xc92e3537);c.drawRect(0,0,W,H,p);
        for(int i=0;i<14;i++){
            int x=(i*51+22)%239,y=(i*83+44)%390;
            txt(c,"*",x,y+(float)Math.sin(animationTime*4+i)*3,12,i%2==0?GOLD:CREAM);
        }
        panel(c,13,123,214,198,CREAM);
        txt(c,"LEVEL 0",120,159,17,INK);
        txt(c,"COMPLETE!",120,187,23,0xff448c67);
        txt(c,"YOU BUILT AN ECONOMY!",120,211,9.5f,BROWN);
        txt(c,"BANANAS  >  COINS  >  INFLUENCE",120,230,8f,INK);
        txt(c,"NEW WORLDS ARRIVE IN UPDATES",120,249,8f,BROWN);
        button(c,25,262,190,24,"KEEP PLAYING",0xff9ccd78);
        button(c,25,293,190,23,"RETURN TO WORLD HUB",GOLD);
    }
    private void render(Canvas c){
        if(screen==HUB)hub(c);
        else{
            world(c);questPointer(c);topHud(c);drawJoystick(c);alerts(c);
            if(screen==CLEAR)completion(c);
        }
    }
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        long now=SystemClock.uptimeMillis();
        float dt=clamp((now-lastTime)/1000f,0f,.04f);
        lastTime=now;animate(dt);
        Canvas image=new Canvas(frame);
        render(image);
        p.setFilterBitmap(false);
        color(Color.WHITE);
        canvas.drawBitmap(frame,null,new Rect(0,0,getWidth(),getHeight()),p);
        postInvalidateOnAnimation();
    }
    private void joystick(float x,float y){
        float dx=x-47f,dy=y-399f;
        float distance=(float)Math.sqrt(dx*dx+dy*dy);
        if(distance<6){walkX=walkY=0;return;}
        walkX=dx/distance;walkY=dy/distance;
    }
    @Override public boolean onTouchEvent(MotionEvent e){
        float x=e.getX()*W/Math.max(1,getWidth());
        float y=e.getY()*H/Math.max(1,getHeight());
        int action=e.getActionMasked();
        if(action==MotionEvent.ACTION_DOWN){
            if(screen==HUB){
                float[][] pos={{55,159},{177,197},{76,266},{170,293}};
                for(int i=0;i<pos.length;i++)
                    if(Math.abs(x-pos[i][0])<23 && Math.abs(y-pos[i][1])<24){
                        selected=i;performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);return true;
                    }
                if(selected==0 && y>383 && y<420){startLevel();return true;}
                return true;
            }
            if(screen==CLEAR){
                if(y>=262 && y<293){screen=PLAY;resetTouch();return true;}
                if(y>=293 && y<324){screen=HUB;resetTouch();return true;}
                return true;
            }
            if(y<30 && x>175){screen=HUB;resetTouch();return true;}
            if(y>=389 && x<107){
                joystickTouch=true;moveTouch=false;joystick(x,y);return true;
            }
            if(y>=389 && x>155){action();return true;}
            if(y>=WORLD_TOP && y<WORLD_BOTTOM){
                touchStartX=x;touchStartY=y;
                targetX=clamp(x/ZOOM+camX,17,MAP-17);
                targetY=clamp((y-WORLD_TOP)/ZOOM+camY,20,MAP-20);
                moveTouch=true;joystickTouch=false;return true;
            }
            return true;
        }else if(action==MotionEvent.ACTION_MOVE){
            if(joystickTouch)joystick(x,y);
            return true;
        }else if(action==MotionEvent.ACTION_UP || action==MotionEvent.ACTION_CANCEL){
            if(joystickTouch){joystickTouch=false;walkX=walkY=0;}
            performClick();return true;
        }
        return true;
    }
    @Override public boolean performClick(){super.performClick();return true;}
}
