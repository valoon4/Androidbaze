#!/usr/bin/env python3
"""APE ECONOMICS: authored asset generator for the Level 0 Zelda-like art pass.

Outputs a real 704x704 PNG; the Android renderer reads the bitmap, not a
rectangle-only substitute. Requires Pillow. Seeded for reproducible art.
"""
from PIL import Image, ImageDraw, ImageFont
import math, os, random
R=random.Random(342099)
S=704
im=Image.new("RGB",(S,S),"#70b65f")
d=ImageDraw.Draw(im)
def rect(x,y,w,h,c):d.rectangle((int(x),int(y),int(x+w-1),int(y+h-1)),fill=c)
def poly(points,c):d.polygon([(int(x),int(y)) for x,y in points],fill=c)
def ellipse(box,c):d.ellipse(tuple(map(int,box)),fill=c)
def line(points,c,w=1):d.line(points,fill=c,width=w)
def text(msg,x,y,col="#3e3533",scale=1):
    ft=ImageFont.load_default()
    bb=d.textbbox((0,0),msg,font=ft)
    tmp=Image.new("RGBA",(bb[2]-bb[0]+5,bb[3]-bb[1]+5))
    ImageDraw.Draw(tmp).text((2-bb[0],1-bb[1]),msg,font=ft,fill=col)
    tmp=tmp.resize((tmp.width*scale,tmp.height*scale),Image.Resampling.NEAREST)
    im.paste(tmp,(int(x-tmp.width/2),int(y-tmp.height/2)),tmp)
# Chromatic grassy base, hand-dithered 16x16 tiles without hard tile borders
for y in range(0,S,16):
    for x in range(0,S,16):
        base=R.choice(["#70b75e","#77bd62","#79be66","#78b95e","#73b75d","#80bf67"])
        rect(x,y,16,16,base)
        for k in range(5):
            xx=x+R.randrange(1,15); yy=y+R.randrange(1,14)
            rect(xx,yy,R.choice([1,2,3]),R.choice([1,2]),R.choice(["#b9d77d","#5ca45b","#88c56c"]))
for _ in range(5000):
    x=R.randint(3,700);y=R.randint(3,700)
    rect(x,y,R.choice([1,1,2]),R.choice([1,2,3]),R.choice(["#4d9d50","#98c969","#b8d77a","#59ab57"]))
# Winding, dithered paths with porous dirt edges.
def path(x0,y0,x1,y1,thick=32):
    length=max(1,int(math.dist((x0,y0),(x1,y1))))
    for i in range(0,length,3):
        f=i/length
        x=x0+(x1-x0)*f; y=y0+(y1-y0)*f
        if abs(x1-x0)>abs(y1-y0):
            rect(x,y-thick/2+R.randrange(-4,5),4,thick+R.randrange(-3,4),"#b28c60")
            rect(x,y-thick/2+4,4,thick-8,"#dfbe7b")
        else:
            rect(x-thick/2+R.randrange(-4,5),y,thick+R.randrange(-3,4),4,"#b28c60")
            rect(x-thick/2+4,y,thick-8,4,"#dfbe7b")
    for i in range(int(length*thick/36)):
        t=R.random();x=x0+(x1-x0)*t+R.uniform(-thick/2,thick/2)
        y=y0+(y1-y0)*t+R.uniform(-thick/2,thick/2)
        rect(x,y,R.choice([1,2,3,5]),R.choice([1,2]),R.choice(["#b79059","#f4d59a","#c9a36f"]))
path(70,284,633,284,42)
path(344,76,344,649,37)
path(162,198,162,280,29)
path(510,214,510,280,29)
path(509,446,509,564,29)
path(120,534,631,534,29)
# Curving coast, submerged cliffs, surf foam and layered water glints.
rect(0,357,704,72,"#325b7b")
rect(0,364,704,56,"#337da9")
for y in range(364,422,9):
    for x in range(0,704,20):
        dx=R.randrange(0,13);dy=R.randrange(0,6)
        rect(x+dx,y+dy,R.randrange(2,8),R.choice([1,2]),R.choice(["#9fd4df","#c6e7dc","#6cb0ce","#4b96bf"]))
for x in range(0,S,3):
    a=352+int(math.sin(x*.065)*5+math.sin(x*.2)*2)
    rect(x,a,4,6,"#365c52");rect(x,a-7,4,8,"#418e50")
    rect(x,a+6,4,2,"#e2ead1")
    b=424+int(math.cos(x*.065)*5+math.cos(x*.19)*2)
    rect(x,b-4,4,3,"#d9e7d1");rect(x,b-1,4,7,"#2e6c56")
    rect(x,b+3,4,10,"#488a51")
# Bridge with many outlined planks / nail heads.
rect(306,345,77,94,"#394147")
rect(311,339,65,105,"#614838")
rect(318,344,51,98,"#a7784d")
for y in range(345,442,10):
    rect(317,y,53,9,"#9a6b49");rect(319,y+1,49,5,"#dca86f")
    rect(321,y+2,44,1,"#f4ca91")
    for x in (324,361):rect(x,y+6,2,2,"#67513e")
for x in (305,371):
    rect(x,337,10,111,"#584338")
    for y in range(340,447,12):rect(x+2,y,5,8,"#b7845a")
# Tree canopy multi-ring highlights + woody shadows
def tree(x,y,r=28):
    rect(x-r*.17,y+r*.33,r*.34,r*.95,"#5f4638")
    rect(x-r*.09,y+r*.5,r*.12,r*.53,"#ac7950")
    ellipse((x-r-4,y-r*.8,x+r+6,y+r*.9),"#273d43")
    for i in range(13):
        a=i*math.tau/13
        xx=x+math.cos(a)*r*.49+R.randint(-3,3)
        yy=y+math.sin(a)*r*.40+R.randint(-3,3)
        rr=R.randint(int(r*.32),int(r*.49))
        ellipse((xx-rr,yy-rr,xx+rr,yy+rr),R.choice(["#255f4b","#35774e","#448751","#3c8350","#5a9c51"]))
    ellipse((x-r*.65,y-r*.79,x+r*.2,y+r*.18),"#63ad59")
    for i in range(r*5):
        a=R.random()*math.tau;rad=R.random()*r*.84
        xx=x+math.cos(a)*rad; yy=y+math.sin(a)*rad*.71
        rect(xx,yy,R.randrange(1,5),R.randrange(1,4),R.choice(["#86c965","#3a7d4b","#75bd61","#9fd56f","#4e9953"]))
    for _ in range(13):
        dx=R.randrange(-r//2,r//2);dy=R.randrange(-r//2,r//3)
        rect(x+dx,y+dy,3,2,"#a2d879")
# Buildings: layered timber/stone, shingled perspective roofs,
# overhang shadows, inner lit windows, shop awning and inscriptions.
def building(x,y,w,h,kind,sign):
    roof=["#a66c4e","#8c6556","#737786","#b86e50"][kind]
    ridge=["#6c483b","#594c49","#535e6e","#824e43"][kind]
    wall=["#cda06b","#c8976e","#bdc4b4","#e1b586"][kind]
    rect(x+7,y+24,w,h-18,"#375d4c")
    rect(x,y+22,w,h-20,"#41363a")
    rect(x+5,y+27,w-10,h-29,wall)
    for yy in range(y+36,y+h-4,9):
        for xx in range(x+6,x+w-6,13):
            if R.random()<.70:rect(xx+((yy+x)%7),yy,5,1,"#e7c698" if kind!=2 else "#9ea9a5")
    poly([(x-10,y+29),(x+w/2,y-21),(x+w+10,y+29),(x+w+3,y+37),(x-3,y+37)],"#3c3540")
    poly([(x-4,y+28),(x+w/2,y-16),(x+w+4,y+28),(x+w,y+33),(x,y+33)],roof)
    poly([(x+9,y+24),(x+w/2,y-9),(x+w-9,y+24)],"#d8af79" if kind!=2 else "#bec9c6")
    for t in range(20):
        yy=y-13+t*2.1
        span=max(4,(yy-(y-18))*w/88)
        if yy>y+29:break
        rect(x+w/2-span,yy,2*span,1,ridge)
        for xx in range(int(x+w/2-span+5),int(x+w/2+span-4),9):
            rect(xx+(t%2)*2,yy-1,5,1,"#f1cf94" if kind!=2 else "#cfd9d5")
    # Overhang eaves and roof-edge depth
    rect(x-2,y+27,w+4,4,ridge)
    rect(x+4,y+31,w-8,2,"#e5b980" if kind!=2 else "#a2abb0")
    # Window shutters, pane shine
    for xx in (x+15,x+w-38):
        rect(xx-5,y+52,31,33,"#5c413e")
        rect(xx-2,y+54,25,27,"#b7885f")
        rect(xx+1,y+55,19,21,"#40718a")
        rect(xx+2,y+56,17,19,"#5da8c4")
        rect(xx+3,y+57,7,9,"#c4e9d6")
        rect(xx+10,y+54,2,25,"#4d4146")
        rect(xx,y+67,23,2,"#4d4146")
        rect(xx-5,y+79,31,5,"#966f4b")
        rect(xx-8,y+56,4,24,ridge);rect(xx+24,y+56,4,24,ridge)
    door=x+w/2
    rect(door-15,y+h-49,30,49,"#443b3c")
    rect(door-11,y+h-45,22,44,"#80543d")
    rect(door-8,y+h-42,16,38,"#b37e55")
    rect(door+5,y+h-20,3,3,"#f8d775")
    rect(door-19,y+h-2,38,6,"#e1b785")
    # Signed carved wood panels; GUI will handle economy details.
    rect(door-47,y+41,94,25,"#40343b")
    rect(door-44,y+43,88,20,"#d0a774")
    rect(door-41,y+45,82,15,"#f3d49a")
    text(sign,door,y+52,"#543f36")
    for yy in (y+42,y+63):rect(door-45,yy,90,2,"#5d423e")
    if kind==0:
        for i in range(6):
            rect(x+10+i*17,y+h-10,15,13,"#815532")
            rect(x+12+i*17,y+h-9,11,8,"#d7a566")
            ellipse((x+11+i*17,y+h-18,x+18+i*17,y+h-13),"#ecc45f")
    if kind==2:
        for xx in (x+4,x+w-18):
            rect(xx,y+9,14,82,"#4d555e")
            rect(xx+2,y+12,10,74,"#9facaa")
            for sy in range(y+13,y+86,9):rect(xx+2,sy,10,2,"#dee2d3")
        for xx in (x+28,x+w-45):
            rect(xx,y+65,18,31,"#354f73")
            rect(xx+2,y+67,14,27,"#44669a")
            rect(xx+8,y+71,2,13,"#efd28e")
        ellipse((door-24,y-28,door+24,y+10),"#55566b")
        ellipse((door-19,y-27,door+17,y+4),"#aaaab5")
        rect(door,y-48,3,31,"#3c3845")
        poly([(door+3,y-47),(door+27,y-40),(door+3,y-31)],"#cb7352")
    else:
        rect(door,y-27,3,13,"#5d463c")
        poly([(door+3,y-27),(door+22,y-23),(door+3,y-16)],"#e6b970")
    if kind==3:
        for i in range(10):
            xx=x+6+i*13
            rect(xx,y+79,13,23,"#e8d8bc" if i%2 else "#b66352")
            rect(xx,y+99,13,5,"#815349")
            rect(xx+1,y+79,11,2,"#f9ede0")
# Finished buildings located to match game objectives.
building(97,130,130,126,0,"PLANTAGE")
building(274,90,142,122,2,"PARLAMENT")
building(450,130,129,131,1,"BANK")
building(442,445,144,114,3,"MARKT")
# Herb gardens, crop beds, small timber houses, bridges and street props.
def plant(x,y):
    rect(x,y+7,17,14,"#855c3e")
    rect(x+1,y+9,15,10,"#b68a50")
    for k in range(3):
        xx=x+3+5*k
        line([(xx,y+12),(xx-2,y+1)],"#377d4c",2)
        ellipse((xx-5,y-2,xx+2,y+5),"#78bc5e")
        rect(xx+1,y+3,3,3,"#ecc75d")
for x in range(99,222,18):
    plant(x,291);plant(x,318)
def fence(x,y):
    rect(x,y,9,22,"#514038")
    rect(x+2,y+2,5,18,"#ac784c")
    rect(x+3,y+3,2,15,"#ecbb77")
    rect(x-2,y+5,13,2,"#cca071")
for y in range(207,335,18):
    fence(88,y);fence(234,y)
for x in range(103,237,12):fence(x,337)
def bush(x,y):
    ellipse((x-15,y-6,x+15,y+11),"#2d654e")
    ellipse((x-12,y-11,x+13,y+5),"#4b9950")
    for k in range(12):
        a=R.random()*math.tau
        rect(x+math.cos(a)*R.randrange(2,12),y+math.sin(a)*R.randrange(2,7),3,2,R.choice(["#aed36e","#87bc64","#307751"]))
for x,y in [(238,235),(249,321),(427,301),(410,312),(313,486),(393,495),(580,316),(603,600)]:
    bush(x,y)
for x,y in [(86,79),(167,62),(225,65),(595,62),(650,78),(665,197),(651,310),(670,466),
            (645,593),(610,649),(541,665),(358,666),(261,659),(141,645),(47,644),(40,481),
            (69,476),(250,488),(35,305),(49,198),(36,114),(603,332),(126,464)]:
    tree(x,y,R.randrange(22,39))
# Secondary flowers & ambient leaf litter with generous detail, preserving main paths.
for i in range(195):
    x=R.randrange(36,664);y=R.randrange(35,645)
    if (101<x<232 and 121<y<269) or (267<x<423 and 77<y<222) or (438<x<594 and 126<y<267) or 348<y<432:continue
    rect(x,y,2,8,"#387a50")
    rect(x-3,y-3,8,5,R.choice(["#f4eddb","#f7d37e","#e7a3a1"]))
    rect(x,y-2,2,2,"#e6b95a")
# Homesteads in southern village.
def house(x,y,roof):
    rect(x+4,y+20,69,53,"#393940")
    rect(x+7,y+23,63,45,"#e0c394")
    poly([(x-4,y+23),(x+38,y-15),(x+81,y+23)],"#4a3842")
    poly([(x+2,y+18),(x+38,y-10),(x+75,y+18)],roof)
    for yy in (y+12,y+17):rect(x+4,yy,69,2,"#edcb95")
    rect(x+29,y+43,15,24,"#76513f")
    for xx in (x+13,x+52):
        rect(xx,y+33,11,12,"#54839b")
        rect(xx+1,y+34,4,4,"#bbdbd0")
house(91,529,"#a66e59")
house(175,558,"#7a81a1")
house(581,541,"#bd7e67")
# Main market-village signboards, warm lamps and wooden benches.
for x,y in [(241,302),(402,306),(102,440),(604,442),(222,533),(604,533)]:
    rect(x,y,4,27,"#4d3f3a")
    rect(x-4,y-8,12,13,"#373640")
    rect(x-2,y-6,8,9,"#edc878")
    rect(x,y-5,3,4,"#fff0b0")
for x,y,s in [(273,331,"VILLAGE"),(396,327,"BANANAS"),(387,553,"TO MARKET")]:
    rect(x,y+7,4,25,"#634938")
    rect(x-28,y-4,60,20,"#3c333d")
    rect(x-25,y-1,54,14,"#ecc68a")
    text(s,x+2,y+7,"#55453c")
# Small springs, mailbox and garden still-life.
ellipse((307,516,342,545),"#4c6b72")
ellipse((311,519,339,541),"#b3d9c5")
ellipse((315,522,335,538),"#4187a6")
rect(307,534,36,7,"#d7c6a1")
for x,y in [(137,580),(475,595),(371,250)]:
    rect(x,y,16,12,"#865d43");rect(x+2,y+2,12,8,"#cca26d")
    rect(x+5,y+4,4,3,"#e9c67d")
OUT=os.environ.get("APE_ART_OUTPUT","games/ApeEconomics/app/src/main/res/drawable-nodpi/ape_v23_world.png")
os.makedirs(os.path.dirname(OUT),exist_ok=True)
im.save(OUT,optimize=True)
print("generated asset:",OUT,im.size,os.path.getsize(OUT))
