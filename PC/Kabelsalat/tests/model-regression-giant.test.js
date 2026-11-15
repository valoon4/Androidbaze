'use strict';
/* KAB-0653–0666: independent source, path, construction and migration gates. */
const assert=require('node:assert/strict');
const components=require('../components.js'),network=require('../network.js');
const refs=require('../reference-data.js').factory(components,network);
globalThis.KabelsalatReferences=refs;
const examples=require('../examples.js').factory(components,network);
const quality=require('../quality.js'),geom=require('../pdf-geometry.js');
const positions=require('../schematic-positions.js'),construction=require('../construction-v2.js');
const pdf=examples.create('pdf-u028');
const original=JSON.stringify(pdf);
assert.deepEqual(pdf.walls.map(w=>w.holes.length),[29,13,26,23]);
assert.equal(pdf.walls[3].holes.length,23,'SW4 PDF-V2 count');
const occupancy=(p)=>p.walls.flatMap(w=>w.holes.map(h=>[
 w.id,h.id,h.sourceCount,
 p.cables.filter(c=>[c.a,c.b].some(e=>e?.wall===w.id&&e?.hole===h.id)).length
]));
for(const [,id,count,actual] of occupancy(pdf))assert.equal(actual,count,'Source occupancy '+id);
const shown=JSON.parse(JSON.stringify(pdf));
for(const wall of shown.walls)wall.holes=wall.holes.map(h=>positions.display(h,true));
assert.deepEqual(occupancy(shown),occupancy(pdf),'Compacted view must not change occupancy');
assert.equal(JSON.stringify(pdf),original,'Presentation may not modify saved source');
const copy=JSON.parse(JSON.stringify(pdf));
for(let wi=0;wi<pdf.walls.length;wi++){
 for(let i=0;i<pdf.walls[wi].holes.length;i++){
  const a=pdf.walls[wi].holes[i],b=copy.walls[wi].holes[i];
  assert.equal(a.sourceU,b.sourceU);assert.equal(a.sourceV,b.sourceV);
 }
}
const straight=construction.cablePath([-1,.4,0],[1,.4,0],'SW1','SW3',[0,0,1],[0,0,-1],[]);
assert.equal(straight.kind,'straight');assert.equal(straight.points.length,2);
const incline=construction.cablePath([-1,.3,0],[1,.7,0],'SW1','SW3',[0,0,1],[0,0,-1],[]);
assert.equal(incline.kind,'straight');assert.equal(incline.points.length,2);
assert.ok(incline.warnings.some(x=>x.includes('geneigter')));
const curve=construction.cablePath([1,.6,0],[0,.6,1],'SW1','SW2',[0,0,1],[-1,0,0],[]);
assert.equal(curve.kind,'smooth-side');assert.equal(curve.points.length,4);
assert.ok(curve.points.flat().every(Number.isFinite));
assert.ok(curve.warnings.some(x=>x.includes('näherungsweise')));
const unfinished=construction.cablePath([0,.5,0],null,'SW1',null,[0,0,1],null,[]);
assert.equal(unfinished.kind,'unresolved');assert.deepEqual(unfinished.points,[]);
const size={w:2.75,d:2.35,h:1.85};
for(const wall of ['SW1','SW2','SW3','SW4']){
 assert.deepEqual(geom.wall(wall).normal,{
  SW1:[0,0,1],SW2:[-1,0,0],SW3:[0,0,-1],SW4:[1,0,0]}[wall]);
 for(const u of [.03,.5,.87]){
  const pt=geom.hole(wall,{u,v:.47},size);
  assert.ok(Math.abs(geom.inverse(wall,pt,size)-u)<1e-10);
 }
}
const sample={dims:{w:1000,d:1000,h:1000},sourceInfo:{sourceVersion:'PDF V2 demo'},
 walls:[{id:'SW1',holes:[{id:'A',u:.4,v:.5,dn:60},{id:'B',u:.5,v:.5,dn:110}]}]};
assert.equal(quality.auditWallOpenings(sample).overlaps.length,0);
assert.equal(quality.auditWallOpenings(sample,h=>({
 ...h,u:positions.compact(h.u),v:positions.compact(h.v)})).overlaps.length,1);
const p={a:[.2,.275,-1.175],b:[.2,.275,1.175],
 wallA:'SW2',wallB:'SW4',refA:'A',refB:'B',radius:.022};
const built=construction.construction('OLA',[p],[],size);
assert.equal(built.status,'concept');
assert.equal(built.supports.length,5);
assert.ok(built.supports.every((x,i)=>i===0?x.low[1]===0:
 Math.abs(x.low[1]-built.supports[i-1].high[1])<1e-10));
const ms=construction.construction('MS',[p],[],size);
assert.equal(ms.status,'concept');assert.equal(ms.supports.length,4);
assert.ok(ms.supports.every(x=>Math.abs(x.high[1]-ms.panels[0].low[1])<1e-10));
for(const model of [built,ms]){
 const ends=model.panels.filter(x=>x.kind==='perforated');
 assert.equal(ends.length,2);
 assert.ok(ends.every(x=>x.holes.length===1&&x.holes[0].radius>p.radius));
}
const blocked=construction.construction('OLA',[p],[{id:'FOREIGN',
 point:[.2,.28,-1.175],radius:.04}],size);
assert.equal(blocked.status,'pending');
assert.ok(blocked.issues.some(x=>x.includes('FOREIGN')));
const legacy=JSON.parse(JSON.stringify(pdf));
for(const w of legacy.walls)for(const h of w.holes){
 h.u=positions.compact(h.sourceU);h.v=positions.compact(h.sourceV);
}
const migrated=JSON.parse(JSON.stringify(legacy));
for(const w of migrated.walls)w.holes=w.holes.map(h=>positions.migrate(h,true));
assert.deepEqual(migrated.walls.map(w=>w.holes.map(h=>[h.id,h.u,h.v])),
 pdf.walls.map(w=>w.holes.map(h=>[h.id,h.u,h.v])));
const again=JSON.parse(JSON.stringify(migrated));
for(const w of again.walls)w.holes=w.holes.map(h=>positions.migrate(h,true));
assert.deepEqual(again,migrated,'Second migration must be idempotent');
const edited={...legacy.walls[0].holes[0],u:.171,v:.622};
assert.deepEqual(positions.migrate(edited,true),edited,'User edits must not be overwritten');
assert.deepEqual(occupancy(migrated),occupancy(pdf),'IDs and cable occupancy survive migration');
assert.equal(JSON.stringify(pdf),original);
console.log('MODEL REGRESSION GIANT OK: 14 parent requirements, 91 source bores, immutable compaction, routing, A/B panels and repeatable PDF migration');