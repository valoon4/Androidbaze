'use strict';
/* KAB micro-sprint: synthetic photo candidates and DN regressions.
 * This is a deterministic unit suite, NOT real-photo validation or OCR.
 */
const assert=require('node:assert/strict');
const fs=require('node:fs'),path=require('node:path');
const photo=require('../photo-candidates.js');
const pipes=require('../pipe-dimensions.js');
const components=require('../components.js'),network=require('../network.js');
globalThis.KabelsalatReferences=require('../reference-data.js').factory(components,network);
const examples=require('../examples.js').factory(components,network);
const app=fs.readFileSync(path.join(__dirname,'../app.js'),'utf8');
const html=fs.readFileSync(path.join(__dirname,'../index.html'),'utf8');
const make=(width=160,height=120,background=195,paint=()=>false)=>{
 const pix=new Uint8ClampedArray(width*height*4);
 for(let y=0;y<height;y++)for(let x=0;x<width;x++){
  const p=(y*width+x)*4,v=paint(x,y)?20:background;
  pix[p]=pix[p+1]=pix[p+2]=v;pix[p+3]=255;
 }
 return {pixels:pix,width,height};
};
const disc=(cx,cy,r)=>(x,y)=>Math.hypot(x-cx,y-cy)<r;
const detect=f=>photo.detect(f.pixels,f.width,f.height);
const pale=make(160,120,230,disc(60,60,18));
const paleResult=detect(pale); // KAB-0882/M21
assert.equal(paleResult.candidates.length,1);
assert.ok(Math.abs(paleResult.candidates[0].x-60)<1);
assert.ok(Math.abs(paleResult.candidates[0].y-60)<1);
const dark=make(160,120,85,disc(60,60,18)); // KAB-0882/M22
const darkResult=detect(dark);
assert.equal(darkResult.candidates.length,1);
assert.ok(Math.abs(darkResult.candidates[0].diameterPx-35)<3);
const adjacent=make(200,140,195,(x,y)=>disc(55,70,18)(x,y)||disc(125,70,18)(x,y));
// KAB-0882/M23: separate circles stay independently identified.
assert.equal(detect(adjacent).candidates.length,2);
const mixed=make(200,160,195,(x,y)=>disc(45,65,13)(x,y)||disc(145,65,30)(x,y));
const mr=detect(mixed); // KAB-0882/M24: no imaginary nominal DN inference.
assert.equal(mr.candidates.length,2);
assert.ok(mr.candidates[0].diameterPx>mr.candidates[1].diameterPx*2);
assert.ok(mr.candidates.every(x=>!('candidateMm' in x)));
// KAB-0882/M25: strongly oblique ellipse is rejected as a confident circle.
const ellipse=make(160,120,195,(x,y)=>((x-80)/34)**2+((y-60)/12)**2<1);
assert.equal(detect(ellipse).candidates.length,0);
// KAB-0882/M26: heavy occlusion is a documented heuristic limitation.
const occluded=make(160,120,195,(x,y)=>disc(80,60,23)(x,y)&&x<72);
const occlusionResult=detect(occluded);
assert.ok(occlusionResult.candidates.length<=1);
assert.ok(occlusionResult.candidates.every(c=>c.status==='candidate'&&!('candidateMm' in c)));
// KAB-0882/M27: bright spot on uniform darker backdrop cannot certify bore.
const highlight=make(160,120,75,(x,y)=>!disc(80,60,20)(x,y));
const hl=detect(highlight);
assert.ok(hl.candidates.every(x=>!('candidateMm' in x)&&x.status==='candidate'));
// KAB-0882/M28: text-like stripes do not turn into fabricated dimensions.
const textOnly=make(160,120,195,(x,y)=>y>=40&&y<46&&x>=20&&x<140&&Math.floor(x/6)%2===0);
assert.ok(detect(textOnly).candidates.every(x=>!('candidateMm' in x)));
// KAB-0882/M29: clipped circles get no verified mm without calibration.
const clipped=make(160,120,195,disc(0,60,22));
assert.ok(detect(clipped).candidates.every(x=>!('candidateMm' in x)));
// KAB-0882/M30, M31: same reference pixels support different explicit units.
const ref=mr.candidates;
for(const mm of [50,110]){
 const scaled=photo.calibrate(ref,0,mm);
 assert.equal(scaled[0].candidateMm,mm);
 assert.ok(scaled[1].candidateMm<mm);
 assert.ok(scaled.every(x=>x.status==='candidate'));
}
// KAB-0882/M35 / M44: uncalibrated relative sizes and heuristic quality.
assert.deepEqual(mr.candidates.map(c=>c.relativeSize),['groß','klein']);
assert.ok(mr.candidates.every(c=>c.quality>=0&&c.quality<=1&&c.status==='candidate'));
// KAB-0882/M38: photo candidates never set reviewed or inject pipe IDs.
assert.ok(mr.candidates.every(c=>!('pipeId' in c)&&c.status!=='reviewed'));
// KAB-0882/M39: outside UI DN30..160 does not silently clamp observed mm.
const large=photo.calibrate(ref,0,200);
assert.equal(large[0].candidateMm,200);
// KAB-0882/M40: file upload permitlist is explicit, not a claim of byte inspection.
assert.ok(app.includes("['image/png','image/jpeg','image/webp'].includes(file.type)"));
assert.ok(app.includes('file.size>10000000'));
// KAB-0882/M41: images scale before analysis, but no source image coordinates
// or perspective-correct physical dimensions are claimed.
assert.ok(app.includes('900/Math.max(bitmap.width,bitmap.height)'));
assert.ok(app.includes('ctx.drawImage(bitmap,0,0,canvas.width,canvas.height)'));
// KAB-0882/M42: 4 MP guard and malformed image-size rejection.
assert.throws(()=>photo.detect(new Uint8Array(16),3000,2000),/4 Megapixel/);
assert.throws(()=>photo.detect(new Uint8Array(16),12,12),/Ungültiges Bild/);
// KAB-0882/M46: positive/negative synthetic fixtures covered by suite.
assert.ok(paleResult.candidates.length>0&&detect(make(160,120)).candidates.length===0);
// KAB-0882/M48: no physical size inference from pixels without explicit known diameter.
assert.ok(mr.candidates.every(x=>x.candidateMm===undefined));
assert.equal(photo.calibrate(ref,0,110)[0].status,'candidate');
// KAB-0882/M20: reference index and physical millimeters are independent inputs.
assert.ok(app.includes("Number($('photoCandidates').value),Number($('photoReferenceMm').value)"));

// KAB-0887/M04, M05, M06, M14: new assertions for 12 synthetic DN samples,
// two valid examples and a required missing-source case (not real measurements).
const sample=examples.create('diameters'),sampleBefore=JSON.stringify(sample);
const vals=sample.walls.flatMap(w=>w.holes.map(h=>h.dn));
const nominal=[30,40,50,60,63,75,90,110,116,125,140,160];
for(const dn of nominal)assert.ok(vals.includes(dn),'KAB-0887/M04: missing DN '+dn);
assert.ok(pipes.apertureMm({dn:30})<pipes.apertureMm({dn:160}));
const secondary=examples.create('diameters');
assert.equal(JSON.stringify(secondary),sampleBefore);
assert.ok(!sample.sourceInfo && !secondary.sourceInfo);
assert.equal(pipes.forHole({dn:50,dimensionSpec:{status:'reviewed'}}).status,'unverified');
assert.equal(JSON.stringify(sample),sampleBefore);
// KAB-0886/M06 and KAB-0888/M06: proportional schematic DN sizes only.
assert.equal(pipes.apertureMm({dn:50}),50);
assert.equal(pipes.apertureMm({dn:110}),110);
assert.equal(pipes.apertureMm({dn:110})/pipes.apertureMm({dn:50}),2.2);
// KAB-0886/M17 and KAB-0888/M11: real PDF source never rewritten by demo.
const pdf=examples.create('pdf-u028'),beforePdf=JSON.stringify(pdf);
assert.deepEqual(pdf.walls.map(w=>w.holes.length),[29,13,26,23]);
examples.create('diameters');
assert.equal(JSON.stringify(pdf),beforePdf);
assert.ok(html.includes('id="holeDn" min="30" max="160" step="1"'));
console.log('OK KAB sprint: photo heuristics/DN synthetic fixtures; no real-image approval');
