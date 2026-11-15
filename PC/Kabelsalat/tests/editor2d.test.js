'use strict';
const assert=require('node:assert/strict');
const geom=require('../editor2d.js');
const m=geom.metrics(2400,2100,520,540);
for(const p of [[0,0],[1200,900],[2400,2100],[700,1455]]){
 const c=geom.toPixels(m,...p),q=geom.toMillimetres(m,c.x,c.y);
 assert.equal(q.x,p[0]);assert.equal(q.y,p[1]);
}
const h1={id:'h1',u:0.5,v:0.5,dn:100};
const p=geom.toPixels(m,1200,1050);
assert.equal(geom.hitHole([h1],m,p.x,p.y)?.id,'h1');
const h2={id:'h2',u:0.5+50/2400,v:0.5,dn:100};
assert.equal(geom.clearance([h1,h2],2400,2100).overlaps,1);
assert.equal(geom.clearance([h1,h2],2400,2100).minPairGap,-50);
const cc=geom.copyOffset(h1,2400,2100,150,25);
assert.equal(Math.round(cc.u*2400),1350);
assert.equal(Math.round(cc.v*2100),1075);
assert.equal(cc.dn,100);
assert.equal(geom.toMillimetres(m,-500,-500).x,0);
assert.equal(geom.toMillimetres(m,9000,9000).y,0);
const aperture={id:'size',u:.3,v:.5,dn:60,dimensionSpec:{openingMm:116}};
assert.equal(geom.apertureMm(aperture),116,'Explicit bore must be distinct from nominal DN');
assert.equal(geom.clearance([{id:'A',u:.5,v:.5,dn:60,dimensionSpec:{openingMm:116}},
 {id:'B',u:.5+100/2400,v:.5,dn:60,dimensionSpec:{openingMm:116}}],2400,2100).overlaps,1);

// KAB-0271–0280: anchored zoom/pan must be an exact view-only transform.
const camera={zoom:1.5,panX:42,panY:-17},x=850,y=1100;
const start=geom.toPixels(geom.viewMetrics(2400,2100,740,510,camera),x,y);
const zoom=geom.zoomAt(2400,2100,740,510,camera,start.x,start.y,3.2);
const anchored=geom.toPixels(geom.viewMetrics(2400,2100,740,510,zoom),x,y);
assert.ok(Math.hypot(anchored.x-start.x,anchored.y-start.y)<1e-7,
 'Wheel zoom must not shift the model under the pointer');
const moved=geom.panCamera(camera,15,-33);
const base=geom.viewMetrics(2400,2100,740,510,camera);
const movedMetrics=geom.viewMetrics(2400,2100,740,510,moved);
const movePoint=geom.toPixels(movedMetrics,x,y);
assert.ok(Math.abs(movePoint.x-start.x-15)<1e-8);
assert.ok(Math.abs(movePoint.y-start.y+33)<1e-8);
assert.throws(()=>geom.panCamera(camera,NaN,0),/Pan ungültig/);
assert.ok(geom.viewMetrics(2400,2100,320,250,{}).scale>0,'Small view must fit');
const selection=geom.rectSelection([h1,h2],m,p.x-12,p.y-10,p.x+12,p.y+10);
assert.deepEqual(selection,['h1','h2']);
const measure=geom.measure([h1,h2],'h1','h2',2400,2100);
assert.ok(Math.abs(measure.centerMm-50)<1e-8);
assert.ok(Math.abs(measure.clearMm+50)<1e-8);
assert.deepEqual(geom.edgeDistances(h1,2400,2100),{
 leftMm:1150,rightMm:1150,bottomMm:1000,topMm:1000});
assert.throws(()=>geom.measure([h1,h2],'h1','h1',2400,2100),/Zwei verschiedene/);
const groupFixture=()=>({dims:{w:2000,d:2000,h:1500},
 walls:[{id:'SW1',holes:[{id:'A',u:.2,v:.4,dn:50},{id:'B',u:.65,v:.45,dn:110},
 {id:'C',u:.85,v:.8,dn:50}]},{id:'SW2',holes:[{id:'A',u:.4,v:.4,dn:30}]}],
 cables:[{id:'cable',a:{wall:'SW1',hole:'A'},b:{wall:'SW2',hole:'A'}}]});
let group=groupFixture();
const beforeGroup=JSON.stringify(group);
const move=geom.planGroupMove(group,'SW1',['A','B'],100,50);
assert.equal(JSON.stringify(group),beforeGroup,'Preview cannot mutate data');
assert.equal(geom.applyGroupMove(group,move),2);
assert.equal(group.walls[0].holes[0].u,.25);
assert.equal(group.walls[0].holes[1].u,.70);
assert.deepEqual(group.cables,groupFixture().cables,'Cable references must remain stable');
assert.deepEqual(group.walls[1],groupFixture().walls[1],'Other wall must not change');
assert.throws(()=>geom.applyGroupMove(group,move),/veraltet/);
group=groupFixture();
assert.throws(()=>geom.planGroupMove(group,'SW1',['A','A'],10,0),/doppelte/);
assert.throws(()=>geom.planGroupMove(group,'SW1',['nope'],10,0),/fehlt/);
assert.throws(()=>geom.planGroupMove(group,'SW1',['A'],-500,0),/Wandrand/);
assert.throws(()=>geom.planGroupMove(group,'SW1',['A'],900,0),/Kollision/);
assert.throws(()=>geom.planGroupMove({...group,sourceInfo:{sourceVersion:'PDF V2'}},'SW1',['A'],50,0),/geschützt/);
const stale=geom.planGroupMove(group,'SW1',['A'],10,0);group.walls[0].holes[0].dn=40;
assert.throws(()=>geom.applyGroupMove(group,stale),/veraltet/);
assert.equal(group.walls[0].holes[0].u,.2,'Rejected move cannot partially write');
const html=require('node:fs').readFileSync(require('node:path').join(__dirname,'../index.html'),'utf8');
const app=require('node:fs').readFileSync(require('node:path').join(__dirname,'../app.js'),'utf8');
for(const id of ['draftResetView','draftViewHint','draftCanvas'])
 assert.ok(html.includes('id="'+id+'"'),'Missing 2D view UI '+id);
assert.ok(app.includes('geom2d.zoomAt(')&&app.includes('geom2d.panCamera('));
assert.ok(app.includes('geom2d.rectSelection(')&&app.includes('geom2d.applyGroupMove('));
console.log('OK: 2D camera anchored pan/zoom, real millimetre measurements, select and collision-safe atomic group movement');

console.log('OK: 2D mapping, separate bore diameter, selection, clearance, copy and round-trip in mm');
