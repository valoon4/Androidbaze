'use strict';
/* KAB-0019–0023 / KAB-0044–0048: source mapping and schematic clearance.
 * PDF S.4 axes are contract tests; no result is a surveyed distance or
 * material/code compliance check.
 */
const assert=require('node:assert/strict');
const geo=require('../pdf-geometry.js');
const quality=require('../quality.js');
const compact=require('../schematic-positions.js');
const components=require('../components.js');
const network=require('../network.js');
globalThis.KabelsalatReferences=require('../reference-data.js').factory(components,network);
const examples=require('../examples.js').factory(components,network);
const project=examples.create('pdf-u028'),size={
 w:project.dims.w/1000,d:project.dims.d/1000,h:project.dims.h/1000
};
const normals={SW1:[0,0,1],SW2:[-1,0,0],SW3:[0,0,-1],SW4:[1,0,0]};
const positions={SW1:[0,0,-size.d/2],SW2:[size.w/2,0,0],
 SW3:[0,0,size.d/2],SW4:[-size.w/2,0,0]};
const rotation={SW1:0,SW2:-Math.PI/2,SW3:Math.PI,SW4:Math.PI/2};
const diff=(a,b)=>a.map((x,i)=>x-b[i]);
const dot=(a,b)=>a.reduce((s,x,i)=>s+x*b[i],0);
const unit=a=>{const m=Math.hypot(...a);return a.map(x=>x/m);};
const close=(a,b,msg)=>assert.ok(Math.abs(a-b)<1e-8,msg+': '+a+' vs '+b);
const direction=(a,b)=>{const c=diff(a,b);return unit(c);};
const outer=p=>p.map(x=>-x);
// Right-handed world: X points toward SW2, Y points upward, Z toward SW3;
// X × Y = Z. Geometry coordinates are document-derived, not surveyed.
assert.deepEqual([0*0-0*1,0*1-1*0,1*1-0*0],[0,0,1]); // X × Y = Z
for(const id of Object.keys(normals)){
 const normal=normals[id],transform=geo.transform(id,size),camera=geo.camera(id,size);
 assert.deepEqual(geo.wall(id).normal,normal,id+' inward normal');
 assert.deepEqual(transform.position,positions[id],id+' plane center');
 assert.equal(transform.rotationY,rotation[id],id+' extrusion rotation');
 const zRot=[Math.sin(rotation[id]),0,Math.cos(rotation[id])];
 close(Math.hypot(...diff(zRot,normal)),0,id+' extrusion must point INSIDE');
 const forward=direction(camera.target,camera.position);
 close(dot(forward,outer(normal)),0,id+' interior camera looks toward wall');
 for(const u of [.05,.21,.5,.79,.95])for(const v of [.12,.5,.88]){
  const point=geo.hole(id,{u,v},size);
  close(geo.inverse(id,point,size),u,id+' 3D->U inverse');
  close(point[1]/size.h,v,id+' height round-trip');
  close(dot(diff(point,positions[id]),normal),.024,id+' bore mouth inside face');
 }
 const extremes=[geo.hole(id,{u:0,v:.5},size),geo.hole(id,{u:1,v:.5},size)];
 const diffAxis=diff(extremes[1],extremes[0]);
 close(Math.hypot(...diffAxis),geo.span(id,size),id+' wall span mismatch');
 close(dot(diffAxis,normal),0,id+' U-axis must be tangent to wall');
}
// These tests intentionally distinguish SOURCE PDF coordinates from 62% view positions.
const original=JSON.stringify(project);
const canonical=quality.auditWallOpenings(project);
const preview=quality.auditWallOpenings(project,h=>compact.display(h,true));
assert.equal(canonical.walls.length,4);
assert.deepEqual(canonical.walls.map(w=>w.openingCount),[29,13,26,23]);
assert.equal(preview.walls.length,4);
assert.equal(canonical.pairs.length,preview.pairs.length);
assert.ok(canonical.minGapMm!==null&&Number.isFinite(canonical.minGapMm));
assert.ok(canonical.minEdgeMm!==null&&Number.isFinite(canonical.minEdgeMm));
assert.ok(Math.abs(canonical.minGapMm-preview.minGapMm)>1,'Preview must change distances but not model');
assert.equal(JSON.stringify(project),original,'Read-only audits must not mutate source');
// DN160 against adjacent DN110 is a model-based gap, not a verified physical clearance.
const mixed=canonical.pairs.filter(p=>[p.dnA,p.dnB].sort((a,b)=>a-b).join('-')==='110-160');
assert.ok(mixed.length>0,'PDF contains adjacent DN160/DN110 pipes');
const closest=mixed.reduce((a,b)=>a.centerMm<b.centerMm?a:b);
assert.ok(Number.isFinite(closest.gapMm)&&closest.wall.startsWith('SW'));
const hole=(wall,id)=>project.walls.find(w=>w.id===wall).holes.find(h=>h.id===id);
const wall=(id)=>canonical.walls.find(w=>w.id===id);
const edge=(h,span,height)=>Math.min(h.u*span,h.v*height,span*(1-h.u),height*(1-h.v))-h.dn/2;
const h75=hole('SW2','SW2_T3_LR01');
assert.equal(h75.dn,75);
const e75=edge(h75,project.dims.d,project.dims.h);
assert.ok(e75>0,'SW2 T3 75mm diagram circle must remain inside wall');
assert.ok(wall('SW2').minEdgeMm<=e75);
const dn63=project.walls.find(w=>w.id==='SW1').holes.filter(h=>h.id.includes('_T2_'));
assert.equal(dn63.length,2);assert.ok(dn63.every(h=>h.dn===63));
for(const h of dn63){
 const e=edge(h,project.dims.w,project.dims.h);
 assert.ok(e>0,'SW1 T2 DN63 must fit model wall: '+h.id);
}
assert.ok(wall('SW1').minEdgeMm!==null);
// KAB-0047: 1D minimum LIGHT gap between circular bores, not center distance.
const synthetic={dims:{w:1000,d:1000,h:1000},sourceInfo:{sourceVersion:'PDF V2 demo'},
 walls:[{id:'SW1',holes:[{id:'A',u:.4,v:.5,dn:60},
  {id:'B',u:.5,v:.5,dn:110}]}]};
const source=quality.auditWallOpenings(synthetic);
close(source.minGapMm,15,'60/110 true light gap');
assert.equal(source.overlaps.length,0);
const compactSource=quality.auditWallOpenings(synthetic,h=>({
 ...h,u:.5+(h.u-.5)*.62,v:.5+(h.v-.5)*.62
}));
assert.equal(compactSource.overlaps.length,1,
 'Tighter preview must raise a collision flag even with unchanged IDs');
assert.ok(compactSource.invalidIds.has('SW1|A')&&compactSource.invalidIds.has('SW1|B'));
assert.equal(synthetic.walls[0].holes[0].u,.4,'Audit must leave original position');
const clipped=quality.auditWallOpenings({dims:{w:1000,d:1000,h:1000},
 walls:[{id:'SW2',holes:[{id:'CLIPPED',u:.02,v:.4,dn:110}]}]});
assert.equal(clipped.edgeCrossings.length,1);
assert.ok(clipped.invalidIds.has('SW2|CLIPPED'));
assert.match(canonical.provenance,/kein Aufmaß/);
console.log('OK KAB-0019–0023/0044–0048: four walls, 91 source holes, edge cases, mixed DN, reversible collision preview');
