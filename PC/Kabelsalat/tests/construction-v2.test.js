'use strict';
const assert=require('node:assert/strict');
const v=require('../construction-v2.js');
const p={a:[.20,.275,-1.175],b:[.20,.275,1.175],wallA:'SW2',wallB:'SW4',refA:'A',refB:'B',radius:.022};
const dims={w:2.75,h:1.85,d:2.35};
const straight=v.cablePath(p.a,p.b,p.wallA,p.wallB,[0,0,1],[0,0,-1],[]);
assert.equal(straight.kind,'straight');
assert.equal(straight.points.length,2);
const side=v.cablePath([1,1,0],[0,1,1],'SW2','SW1',[-1,0,0],[0,0,-1],[]);
assert.equal(side.kind,'smooth-side');
assert.equal(side.points.length,4);
assert.ok(!side.points.some(v=>v.some(x=>!Number.isFinite(x))));
const edited=v.cablePath(p.a,p.b,p.wallA,p.wallB,[0,0,1],[0,0,-1],[[0,.55,0]]);
assert.equal(edited.kind,'edited','Manual waypoints remain editable');
const a=v.construction('OLA',[p],[],dims);
assert.equal(a.status,'concept');
assert.equal(a.variant,'A');
assert.equal(a.panels.length,6);
assert.equal(a.supports.length,5);
assert.ok(a.supports.every((x,i)=>i===0?x.low[1]===0:
 Math.abs(x.low[1]-a.supports[i-1].high[1])<1e-10));
const b=v.construction('MS',[p],[],dims);
assert.equal(b.status,'concept');
assert.equal(b.panels.length,6);
assert.equal(b.supports.length,4);
assert.ok(b.supports.every(x=>Math.abs(x.high[1]-b.panels[0].low[1])<1e-10));
assert.deepEqual(p.a,[.20,.275,-1.175],'Input coordinates must not be mutated');
const blocked=v.construction('OLA',[p],[{id:'foreign',point:[.23,.28,-1.175],radius:.04}],dims);
assert.equal(blocked.status,'pending','No enclosure may block unrelated pipes');
assert.ok(blocked.issues.some(x=>x.includes('foreign')));
const open=v.construction('OLA',[{...p,b:null}],[],dims);
assert.equal(open.status,'pending','Unresolved endpoint must never generate a fake channel');
const corner=v.construction('MS',[{...p,wallB:'SW1'}],[],dims);
assert.equal(corner.status,'pending','Sideways channel needs bend engineering');
assert.equal(v.referenceThresholdMm,75);
// Regression with actual imported 13_T_021 reference geometry, not synthetic pipes.
const refs=require('../reference-data.js').factory(
 require('../components.js'),require('../network.js'));
const ref=refs.create('source-t021');
const size={w:ref.dims.w/1000,d:ref.dims.d/1000,h:ref.dims.h/1000};
function portPoint(w,h){
 const u=h.u,y=size.h*h.v;
 return w==='SW1'?[-size.w/2+.024,y,-size.d/2+size.d*u]:
  w==='SW2'?[-size.w/2+size.w*u,y,-size.d/2+.024]:
  w==='SW3'?[size.w/2-.024,y,-size.d/2+size.d*u]:
  [-size.w/2+size.w*u,y,size.d/2-.024];
}
const map=new Map();
for(const wall of ref.walls)for(const hole of wall.holes)
 map.set(hole.id,{id:hole.id,point:portPoint(wall.id,hole),radius:hole.dn/2000});
for(const group of ['OLA','MS']){
 const list=ref.cables.filter(c=>c.group===group).map(c=>({
  a:map.get(c.a?.hole)?.point,b:map.get(c.b?.hole)?.point,
  wallA:c.a?.wall,wallB:c.b?.wall,refA:c.a?.hole,refB:c.b?.hole,
  radius:Math.max(.009,(c.diameter||12)/2000)
 }));
 const model=v.construction(group,list,[...map.values()],size);
 assert.equal(model.status,'concept',group+' reference geometry must construct safely');
 assert.equal(model.panels.length,6,group+' six channel plates');
 assert.equal(model.supports.length,group==='OLA'?5:4,group+' correct 3D supports');
 for(const end of model.panels.filter(x=>x.kind==='perforated')){
  assert.equal(end.holes.length,list.length);
  const cross=end.axis===0?2:0;
  for(const hole of end.holes){
   assert.ok(hole.at[1]-end.low[1]>=hole.radius);
   assert.ok(end.high[1]-hole.at[1]>=hole.radius);
   assert.ok(hole.at[cross]-end.low[cross]>=hole.radius);
   assert.ok(end.high[cross]-hole.at[cross]>=hole.radius);
  }
 }
}
const pendingSource=refs.create('source-u028');
assert.equal(pendingSource.cables.length,18);
console.log('OK: construction v2 A/B geometry, straight/side paths, blockage, incomplete data');
