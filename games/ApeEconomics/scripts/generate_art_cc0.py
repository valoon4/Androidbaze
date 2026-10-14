#!/usr/bin/env python3
"""Asset-driven Level 0 village with layered 16-bit style pixels.
Authoring is deterministic. Source: ArMM1998 Zelda-like tiles (CC0 / OpenGameArt).
The monkey characters and Ape Economics icon remain our separate original assets.
"""
from PIL import Image,ImageDraw,ImageFont,ImageEnhance
import random,math,argparse,os
P=argparse.ArgumentParser()
P.add_argument("--source",required=True)
P.add_argument("--objects",required=True)
P.add_argument("--output",required=True)
args=P.parse_args()
atlas=Image.open(args.source).convert("RGBA")
objs=Image.open(args.objects).convert("RGBA")
r=random.Random(209711)
W=H=704
scene=Image.new("RGBA",(W,H),(76,154,79,255))
d=ImageDraw.Draw(scene)
def box(x,y,w,h,c):d.rectangle((int(x),int(y),int(x+w-1),int(y+h-1)),fill=c)
def crop_blit(atlas,area,x,y,w=None,h=None,flip=False):
    sprite=atlas.crop(area)
    if flip:sprite=sprite.transpose(Image.Transpose.FLIP_LEFT_RIGHT)
    if w is not None:
        sprite=sprite.resize((int(w),int(h)),Image.Resampling.NEAREST)
    scene.alpha_composite(sprite,(int(x),int(y)))
def label(s,x,y,scale=2):
    font=ImageFont.load_default()
    l,t,rr,b=d.textbbox((0,0),s,font=font)
    lay=Image.new("RGBA",(rr-l+6,b-t+6),(0,0,0,0))
    ld=ImageDraw.Draw(lay)
    ld.text((3-l,2-t),s,font=font,fill="#403532",stroke_width=0)
    lay=lay.resize((lay.width*scale,lay.height*scale),Image.Resampling.NEAREST)
    return lay
def sign(title,x,y,w=100):
    # Chunky carved-wood sign with inset parchment, no smooth vector edges.
    box(x-w//2+2,y+5,w,22,"#4a3532")
    box(x-w//2,y,w,22,"#3a3135")
    box(x-w//2+2,y+2,w-4,18,"#976943")
    box(x-w//2+4,y+4,w-8,14,"#e5c184")
    box(x-w//2+7,y+5,w-14,1,"#fff0b2")
    box(x-w//2+5,y+17,w-10,1,"#a27d50")
    for bx in (x-w//2+5,x+w//2-8):
        box(bx,y+5,3,3,"#654a3a")
    word=label(title,0,0,1)
    # Nearest neighbor 12px type to fit just above the game sprite size.
    word=word.resize((min(w-10,word.width),15),Image.Resampling.NEAREST)
    scene.alpha_composite(word,(int(x-word.width/2),int(y+2)))
# Lush grass is a consistent green underlay. Avoid autotile fragments:
# the loose Overworld atlas grass group contains shoreline/water variants.
grass=["#70aa54","#77af59","#7ab35c","#72a951","#7cb55d","#74ab54","#80b660"]
for ty in range(0,H,16):
    for tx in range(0,W,16):
        box(tx,ty,16,16,r.choice(grass))
        for k in range(6):
            sx=tx+r.randrange(16);sy=ty+r.randrange(16)
            box(sx,sy,r.choice([1,2,2,3]),r.choice([1,2]),
                r.choice(["#5c9b53","#a6c978","#65a95c","#85ba68","#93c16b"]))
        if r.random()<.35:
            sx=tx+r.randrange(5,13);sy=ty+r.randrange(4,12)
            box(sx,sy,1,4,"#53934a")
            box(sx-2,sy+1,2,2,"#65a458")
            box(sx+2,sy+2,2,2,"#b2cb79")
for k in range(1250):
    x=r.randrange(12,W-13);y=r.randrange(12,H-13)
    box(x,y,r.choice([1,1,2]),r.choice([1,2]),
        r.choice(["#51964e","#c1d78a","#8ec371","#7bb362"]))
# Roads with chipped border + individual soil fragments. Bend vertical road.
def road_vertical(x0,y0,y1,width):
    for y in range(y0,y1,3):
        x=x0+int(math.sin(y/81)*5)
        off=r.randint(-4,4)
        box(x-width//2-4+off,y,width+8,4,"#9f7f5f")
        box(x-width//2+off,y,width,4,"#d5b888")
    for k in range((y1-y0)*width//24):
        y=r.randrange(y0,y1);x=x0+r.randrange(-width//2,width//2)
        box(x,y,3,r.choice([1,2]),r.choice(["#a58e68","#edcc96","#c4a275"]))
def road_horizontal(y0,x0,x1,width):
    for x in range(x0,x1,3):
        y=y0+int(math.sin(x/65)*4)+r.randint(-3,3)
        box(x,y-width//2-3,4,width+7,"#aa8e64")
        box(x,y-width//2,4,width,"#dabc82")
    for k in range((x1-x0)*width//24):
        x=r.randrange(x0,x1);y=y0+r.randrange(-width//2,width//2)
        box(x,y,r.choice([1,2,3]),2,r.choice(["#987c56","#f0d6a0","#c4a06f"]))
road_vertical(350,204,704,33)
road_horizontal(278,93,611,37)
road_vertical(163,225,314,27)
road_vertical(519,225,319,27)
road_vertical(522,475,612,27)
road_horizontal(543,102,610,33)
# Truly textured water uses the source 16x16 water tiles.
for y in range(352,431,16):
    for x in range(0,W,16):
        area=r.choice([(272,0,288,16),(288,0,304,16),(304,0,320,16),
                       (288,112,304,128),(304,112,320,128)])
        crop_blit(atlas,area,x,y)
# Organic grassy riverbanks. Subtle sand ridges and foam on both shores.
for x in range(0,704,3):
    bankN=355+int(4*math.sin(x/31)+2*math.cos(x/13))
    bankS=425+int(4*math.sin(x/29)+2*math.cos(x/14))
    box(x,bankN-8,3,7,"#447a48")
    box(x,bankN-2,3,4,"#d1d0a2")
    box(x,bankN+2,3,2,"#8bcfd7")
    box(x,bankS-5,3,2,"#d9e3c3")
    box(x,bankS-2,3,5,"#447e55")
    box(x,bankS+3,3,5,"#64a55c")
# Existing real pixel bridges sourced from tilesheet's rails and beams.
box(317,342,68,104,"#56483d")
box(323,338,56,104,"#977049")
for y in range(341,441,10):
    box(325,y,53,9,"#af7950")
    box(325,y,53,2,"#f1bb83")
    box(327,y+4,49,1,"#78543c")
    for x in (328,371):box(x,y+6,2,2,"#4b423a")
for x in (315,378):
    box(x,334,7,115,"#60483e")
    for y in range(336,442,10):
        box(x+1,y,5,8,"#ae8256")
# Grass-line margins, flowers, mushrooms and richly shaded trees.
def large_tree(x,y,size=65):
    # A layered GBA canopy from ArMM1998's transparent foliage sprites.
    # Pixel-ground shadow stays separate from the source's vivid canopy.
    from PIL import ImageDraw
    shadow=Image.new("RGBA",(int(size*1.1),int(size*.34)),(0,0,0,0))
    sd=ImageDraw.Draw(shadow)
    sd.ellipse((2,2,shadow.width-3,shadow.height-3),fill=(33,56,49,105))
    scene.alpha_composite(shadow,(int(x-size*.56),int(y+size*.24)))
    trunkw=max(6,int(size*.12))
    box(x-trunkw/2,y+size*.16,trunkw,size*.34,"#624a3b")
    box(x-trunkw/2+2,y+size*.2,2,size*.20,"#ab7d53")
    # Teal highlights are recolored to lively emerald; retain dark pixel shading.
    def canopy(src,dst,wh):
        im=objs.crop(src).resize(wh,Image.Resampling.NEAREST)
        pix=im.load()
        for yy in range(im.height):
            for xx in range(im.width):
                rr,gg,bb,aa=pix[xx,yy]
                if aa>0 and gg>75 and bb>rr*1.08 and gg>rr*1.17:
                    pix[xx,yy]=(rr,int(min(255,gg*1.03)),int(bb*.62),aa)
        scene.alpha_composite(im,(int(dst[0]),int(dst[1])))
    canopy((128,191,162,222),(x-size//2,y-int(size*.55)),(size,int(size*.8)))
    canopy((197,191,226,222),(x-int(size*.15),y-int(size*.60)),
           (int(size*.6),int(size*.65)))
def shrub(x,y,size=25):
    crop_blit(objs,(97,191,127,223),x-size//2,y-size//2,size,int(size*.98))
def flowers(x,y):
    crop_blit(objs,(55,0,75,20),x-7,y-7,16,16)
    box(x+r.randrange(-5,5),y+4,2,2,"#f6eab4")
for x,y,s in [(32,70,64),(85,84,67),(165,65,70),(260,44,57),
              (440,54,64),(579,67,68),(666,84,70),(681,197,65),
              (668,320,55),(673,490,73),(645,643,69),(556,667,79),
              (420,679,68),(286,661,73),(170,653,72),(70,653,77),
              (23,565,67),(39,469,65),(81,456,62),(245,462,57),
              (35,299,60),(77,336,60)]:
    large_tree(x,y,s)
for x,y in [(235,185),(251,311),(425,300),(449,322),(414,492),
            (584,321),(281,487),(582,613),(418,628)]:
    shrub(x,y,r.randint(25,42))
# Ferns, asymmetric grass tufts, colorful wildflowers.
for k in range(320):
    x=r.randrange(24,674);y=r.randrange(33,678)
    if 336<y<454:continue
    if abs(y-278)<19 or abs(y-543)<16 or abs(x-350)<17:continue
    if 98<x<232 and 105<y<264:continue
    if 261<x<441 and 56<y<237:continue
    if 434<x<591 and 96<y<264:continue
    if 424<x<592 and 429<y<581:continue
    if k%3==0:flowers(x,y)
    else:
        box(x,y,2,6,"#4b8b53")
        box(x-3,y+1,2,3,"#d7d98e")
        box(x+2,y-1,2,3,"#b6ce7f")
# Crop bed, woven fencing and a banana-banner plantation.
for yy in (294,311,328):
    for xx in range(111,228,16):
        crop_blit(atlas,(16,0,32,16),xx,yy,14,14)
        box(xx+2,yy+6,4,5,"#3b7249")
        box(xx+4,yy+5,3,3,"#d7bf61")
for x in list(range(102,232,13)):
    crop_blit(atlas,(48,264,64,296),x,335,13,27)
for y in range(185,335,16):
    crop_blit(atlas,(48,264,64,296),96,y,13,23)
    crop_blit(atlas,(48,264,64,296),233,y,13,23)
# Four distinct, asset-based buildings, using original roofs/walls/awnings.
# Houses from atlas have shadows, roof geometry and windows in the art.
crop_blit(atlas,(96,0,179,81),96,122,138,137)
sign("PLANTAGE",166,185,96)
crop_blit(atlas,(410,353,473,454),284,96,135,143)
# A proper stepped copper roof turns the gate sprite into a real civic building.
crop_blit(atlas,(380,192,507,272),287,78,130,77)
box(302,153,102,3,"#564e51")
# Hanging civic flags, brass detailing and an open entrance.
for flagx in (297,395):
    box(flagx,176,3,32,"#c5a97c")
    box(flagx-2,179,14,27,"#33597d")
    box(flagx,181,10,3,"#7c9cc1")
    box(flagx+5,188,2,12,"#e6ce7f")
    box(flagx+2,194,9,2,"#e6ce7f")
sign("PARLAMENT",352,154,117)
crop_blit(atlas,(180,0,263,82),447,118,138,139)
# Gold-trimmed finance-house facade (a civic building, not just a brown barn).
box(450,222,134,4,"#79583f")
for bx in (461,573):
    box(bx,178,6,61,"#5b4941")
    box(bx+1,179,4,57,"#c49a65")
    box(bx+1,182,3,49,"#edbf82")
box(474,189,18,18,"#463c3a")
box(476,191,14,14,"#e4bf68")
box(544,189,18,18,"#463c3a")
box(546,191,14,14,"#e4bf68")
sign("BANK",516,171,82)
# Replace the blue-white merchant canopy with a warmer red-white awning.
stall=atlas.crop((289,359,380,450)).resize((156,137),Image.Resampling.NEAREST)
pixels=stall.load()
for yy in range(stall.height):
    for xx in range(stall.width):
        rr,gg,bb,aa=pixels[xx,yy]
        if aa>30 and bb>125 and bb>rr*1.25 and bb>gg*.91:
            pixels[xx,yy]=(198,88,72,aa)
scene.alpha_composite(stall,(437,434))
sign("MARKT",515,489,90)
# Building doors and usable walk-in approach zones (aligned to engine).
for xx,yy in [(166,256),(351,234),(516,256),(515,569)]:
    box(xx-11,yy,22,5,"#b99a6e")
    box(xx-6,yy+2,12,2,"#e6c490")
# Detail props: fountain, sacks, pots, market fruits and patterned banners.
for x,y in [(279,302),(400,305)]:
    crop_blit(atlas,(350,145,408,198),x-21,y-20,36,36)
for x,y in [(269,243),(435,248),(281,484),(419,585)]:
    crop_blit(atlas,(510,78,544,114),x,y,27,30)
for xx in range(461,572,22):
    crop_blit(atlas,(385,338,401,354),xx,550,18,18)
# Homes, a square, reeds and layered village decoration on south bank.
crop_blit(atlas,(96,0,179,81),103,497,91,80)
crop_blit(atlas,(180,0,263,82),214,548,96,81)
crop_blit(atlas,(96,0,179,81),581,550,100,91)
for xx,yy in [(287,508),(412,502),(618,338),(152,445),(584,475)]:
    crop_blit(atlas,(354,145,406,198),xx-14,yy-12,23,25)
for k in range(24):
    x=r.randrange(30,671);y=r.choice([r.randrange(340,361),r.randrange(431,459)])
    if 309<x<399:continue
    box(x,y,2,8,"#4b7957")
    box(x-2,y+2,4,4,"#9bc36f")
# Extra banana-world storytelling: sweet fruit, shade plants, lanterns and
# tiny mottled leaf clusters make each clearing feel individually authored.
for px,py in [(115,276),(198,274),(262,299),(434,305),(438,539),(593,545)]:
    box(px,py-13,2,16,"#5c734a")
    box(px-3,py-12,9,6,"#417c50")
    box(px-4,py-7,4,5,"#e6c957")
    box(px,py-5,4,5,"#f9d970")
    box(px+3,py-9,3,6,"#d0ab42")
for xx,yy in [(243,284),(423,286),(297,338),(398,344),(266,543),(425,546)]:
    box(xx,yy-15,4,24,"#695344")
    box(xx-5,yy-23,15,13,"#433c3e")
    box(xx-3,yy-21,11,9,"#e2b65d")
    box(xx-1,yy-19,5,5,"#ffdf93")
for px,py in [(105,318),(221,308),(419,310),(280,556),(568,571),(620,511)]:
    for dx,dy in [(0,0),(7,3),(13,-2),(19,2)]:
        box(px+dx,py+dy,5,5,"#eae8d0")
        box(px+dx+2,py+dy+1,2,2,"#e6b661")
# Final coherent GBA palette: slightly warmer contrast with true source textures.
result=scene.convert("RGB")
result=ImageEnhance.Color(result).enhance(1.13)
result=ImageEnhance.Contrast(result).enhance(1.07)
out=args.output
os.makedirs(os.path.dirname(out),exist_ok=True)
result.save(out,optimize=True)
print("Authored 704x704 Level 0 from CC0 art atlas:",out,os.path.getsize(out))
