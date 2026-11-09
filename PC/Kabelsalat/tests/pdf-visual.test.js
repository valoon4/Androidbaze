'use strict';
/* 97 independent PDF-V2 visual acceptance cases:
   91 source pipes + four true wall compass directions + two MS cable paths.
   These are NOT equivalent to 97 completed long-term product backlog tickets. */
const assert=require('node:assert/strict'),fs=require('node:fs'),path=require('node:path');
const components=require('../components.js'),network=require('../network.js');
const source=require('../reference-data.js').factory(components,network).create('pdf-u028');
const diagram=require('../pdf-visual.js'),geo=require('../pdf-geometry.js');
const sizes={w:source.dims.w/1000,d:source.dims.d/1000,h:source.dims.h/1000};
const expectedRows={
 SW1:[
  [1,[1,2,3,4,5],[.0513,.1734,.2956,.4178,.5400],.8373],
  [1,[6,7,8,9,10],[.0513,.1734,.2956,.4178,.5400],.6727],
  [1,[11,12,13,14,15],[.0513,.1734,.2956,.4178,.5400],.5081],
  [1,[16,17,18,19,20,21],[.0513,.1734,.2956,.4178,.5400,.6621],.3436],
  [1,[22,23,24,25,26,27],[.0513,.1734,.2956,.4178,.5400,.6621],.1790],
  [2,[1,2],[.8597,.9653],.8373]],
 SW2:[
  [1,[11,10,9],[.2489,.3906,.5324],.8174],
  [1,[5,8,7,6],[.1071,.2489,.3906,.5324],.6528],
  [1,[1,4,3,2],[.1071,.2489,.3906,.5324],.4882],
  [3,[1],[.8597],.8174],[3,[2],[.8597],.6528]],
 SW3:[
  [1,[5,4,3,2,1],[.1976,.3198,.4419,.5641,.6863],.8373],
  [1,[10,9,8,7,6],[.1976,.3198,.4419,.5641,.6863],.6727],
  [1,[15,14,13,12,11],[.1976,.3198,.4419,.5641,.6863],.5081],
  [1,[20,19,18,17,16],[.1976,.3198,.4419,.5641,.6863],.3436],
  [1,[26,25,24,23,22,21],[.0754,.1976,.3198,.4419,.5641,.6863],.1790]],
 SW4:[
  [1,[6,5,4,3,2,1],[.0754,.2127,.3499,.4872,.6244,.7617],.8282],
  [1,[12,11,10,9,8,7],[.0754,.2127,.3499,.4872,.6244,.7617],.6637],
  [1,[18,17,16,15,14,13],[.0754,.2127,.3499,.4872,.6244,.7617],.4991],
  [1,[23,22,21,20,19],[.2127,.3499,.4872,.6244,.7617],.3345]]
};
const sourcePages={SW1:21,SW2:22,SW3:23,SW4:24};
const id=(w,t,lr)=>w+'_T'+t+'_LR'+String(lr).padStart(2,'0');
const dnFor=(w,t,lr)=>{
 if(w==='SW1'&&t===2)return 63;
 if(w==='SW1'&&t===1&&[21,27].includes(lr))return 160;
 if(w==='SW2'&&t===1&&lr===5)return 160;
 if(w==='SW2'&&t===3&&lr===1)return 75;
 if(w==='SW3'&&lr===26)return 160;
 return 110;
};
let passed=0;const checks=[],seen=new Set();
function accept(label,fn){
 assert.doesNotThrow(fn,label);passed++;checks.push(label);
}
const occupation=diagram.occupancy(source),allSvg=diagram.render(source,'all');
assert.ok(allSvg.includes('<svg')&&allSvg.includes('p-trasse'),'PDF vector wall diagrams must render');
for(const [wallId,rows] of Object.entries(expectedRows)){
 const wall=source.walls.find(w=>w.id===wallId);
 assert.ok(wall,'Source wall missing '+wallId);
 for(const [trasse,numbers,xx,v] of rows)for(let index=0;index<numbers.length;index++){
  const lr=numbers[index],key=id(wallId,trasse,lr),u=xx[index];
  accept('Rohr '+key+' – PDF-Lage, DN und Kabelring sichtbar',()=>{
   assert.ok(!seen.has(key),'duplicate PDF pipe');seen.add(key);
   const hole=wall.holes.find(h=>h.id===key);
   assert.ok(hole,'missing source pipe '+key);
   assert.equal(hole.sourcePage,sourcePages[wallId]);
   assert.equal(hole.dn,dnFor(wallId,trasse,lr),'source DN wrong');
   assert.equal(hole.u,u,'PDF vector X changed');
   assert.equal(hole.v,v,'PDF vector Y changed');
   assert.ok(allSvg.includes('data-pipe="'+key+'"'),'2D SVG missing pipe');
   assert.ok(allSvg.includes(key+' DN '+hole.dn+' mit '),'accessible SVG label missing');
   const cableCount=(occupation.get(key)||[]).length;
   assert.equal(cableCount,hole.sourceCount,'PDF occupancy mismatch');
   const world=geo.hole(wallId,hole,sizes);
   assert.ok(world.every(Number.isFinite),'invalid 3D pipe');
   assert.ok(Math.abs(geo.inverse(wallId,world,sizes)-u)<1e-9,'2D↔3D mirror mismatch');
   assert.ok(world[1]>=0&&world[1]<=sizes.h,'pipe leaves floor/roof');
   const edge=geo.span(wallId,sizes)*(Math.min(u,1-u));
   assert.ok(edge>hole.dn/2000,'pipe touches/overflows lateral wall edge');
  });
 }
}
// Direction, projection and view context independently checked for all 4 walls.
const expectedOrientation={
 SW1:{normal:[0,0,1],rotation:0},
 SW2:{normal:[-1,0,0],rotation:-Math.PI/2},
 SW3:{normal:[0,0,-1],rotation:Math.PI},
 SW4:{normal:[1,0,0],rotation:Math.PI/2}
};
for(const [w,expected] of Object.entries(expectedOrientation)){
 accept('Wand '+w+' – Position, Ausrichtung und PDF-Einzelansicht',()=>{
  const basis=geo.wall(w),transform=geo.transform(w,sizes),camera=geo.camera(w,sizes);
  assert.deepEqual(basis.normal,expected.normal);
  assert.equal(transform.rotationY,expected.rotation);
  assert.equal(transform.span,geo.span(w,sizes));
  for(const angle of [0,1.2,2.7]){
   const fan=geo.tangentOffset(w,angle,.03);
   const dot=fan.reduce((sum,v,i)=>sum+v*expected.normal[i],0);
   assert.ok(Math.abs(dot)<1e-10,'Cable strand leaves source pipe face');
   assert.ok(Math.abs(Math.hypot(...fan)-.03)<1e-10);
  }
  assert.ok(camera.position.every(Number.isFinite));
  assert.ok(diagram.render(source,w).includes('aria-label="'+w+' – '),
   'PDF side view missing');
  assert.equal((diagram.render(source,w).match(/data-pipe=/g)||[]).length,
   source.walls.find(x=>x.id===w).holes.length);
 });
}
for(const [name,from,to] of [
 ['MS-SNG-0038',id('SW1',1,21),id('SW3',1,26)],
 ['MS-SNG-0056',id('SW1',1,27),id('SW2',1,5)]
]){
 accept('MS-Kabel '+name+' – beide dokumentierten Rohranschlüsse',()=>{
  const cable=source.cables.find(c=>c.name===name);
  assert.ok(cable,'MS cable missing');
  assert.equal(cable.group,'MS');
  assert.equal(cable.a.hole,from);
  assert.equal(cable.b.hole,to);
  const a=source.walls.find(w=>w.id===cable.a.wall).holes.find(h=>h.id===from);
  const b=source.walls.find(w=>w.id===cable.b.wall).holes.find(h=>h.id===to);
  assert.notDeepEqual(geo.hole(cable.a.wall,a,sizes),geo.hole(cable.b.wall,b,sizes));
 });
}
assert.equal(passed,97,'Visual acceptance suite must have exactly 97 source-based cases');
assert.equal(seen.size,91);
const stats=diagram.audit(source);
assert.deepEqual({walls:stats.walls,holes:stats.holes,totalCableObjects:stats.totalCableObjects,
 cableAppearances:stats.cableAppearances,missingEnds:stats.missingEnds},
 {walls:4,holes:91,totalCableObjects:86,cableAppearances:106,missingEnds:66});
assert.equal(stats.ms,2);
assert.equal(stats.olaOpen,4);
assert.equal(source.enclosures.MS.visible,false);
assert.equal(source.enclosures.OLA.visible,false);
assert.equal(source.supports.length,0);
const app=fs.readFileSync(path.resolve(__dirname,'../app.js'),'utf8');
const html=fs.readFileSync(path.resolve(__dirname,'../index.html'),'utf8');
assert.ok(app.includes('new THREE.CylinderGeometry(r+.010,r+.010,.20'),'3D pipe sleeves absent');
assert.ok(app.includes("opacity:chosen?1:(pdfView()?.94"),'3D source cables not emphasized');
assert.ok(app.includes('pdfGeometry.hole(wid,h,s)'),'3D editor must use tested geometry');
assert.ok(app.includes('pdfGeometry.tangentOffset(ref.wall,a,r)'),
 'PDF cable strands must use wall-tangent displacement vectors');
assert.ok(app.includes('pdfGeometry.transform(w.id,s)'),'3D wall mesh must use tested orientation');
assert.ok(app.includes('pdfGeometry.camera(viewMode,d)'),'PDF side camera must use tested orientation');
assert.ok(html.includes('id="oldProjectNotice"'),'Old browser project must be clearly identified');
assert.ok(html.includes('id="pdfVisualPanel"'),'PDF visual fallback must be available');
assert.ok(html.includes('id="pdfOpen"'),'PDF fallback must be accessible');
assert.ok(!app.includes("project.pdfVisualPanel"),'Viewer must not be persisted into project JSON');
const before=JSON.stringify(source);diagram.render(source,'all');assert.equal(JSON.stringify(source),before);
console.log('OK: '+passed+' independent PDF visual cases passed (91 pipes, 4 wall axes, 2 MS routes); '+
 '86 cable objects / 106 appearances / 66 incomplete routes; no fabricated Powerpanel geometry.');
