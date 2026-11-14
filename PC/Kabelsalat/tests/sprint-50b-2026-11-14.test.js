'use strict';
// KAB-0051..0054/KAB-0884: non-mutating, schematic geometry regressions.
// No browser screenshot or professional clearance is inferred by these checks.
const assert=require('node:assert/strict');
const fs=require('node:fs'),path=require('node:path');
const quality=require('../quality.js'),schematic=require('../schematic-positions.js');
const concrete=require('../concrete-wall.js');
const components=require('../components.js'),network=require('../network.js');
globalThis.KabelsalatReferences=require('../reference-data.js').factory(components,network);
const examples=require('../examples.js').factory(components,network);
const app=fs.readFileSync(path.join(__dirname,'../app.js'),'utf8');
const pdf=examples.create('pdf-u028'),baseline=JSON.stringify(pdf);
// KAB-0051 / 0052 / 0053 / 0054: schematic clearance uses model-mm and
// normalized U,V values; raw source must survive a compact preview.
const demo={dims:{w:1000,d:1000,h:1000},
 sourceInfo:{sourceVersion:'PDF V2 synthetic'},
 walls:[{id:'SW1',holes:[
 {id:'LR-A',u:.4,v:.5,dn:60,sourceU:.4,sourceV:.5},
 {id:'LR-B',u:.5,v:.5,dn:110,sourceU:.5,sourceV:.5}
 ]}],cables:[{id:'C-1',a:{wall:'SW1',hole:'LR-A'},b:{wall:'SW1',hole:'LR-B'}}]};
const snap=JSON.stringify(demo);
const source=quality.auditWallOpenings(demo);
assert.equal(source.walls[0].openingCount,2);
assert.equal(source.pairs.length,1);
assert.equal(source.pairs[0].centerMm,100);
assert.equal(source.pairs[0].gapMm,15,'100 mm - 30 mm - 55 mm = 15 mm light gap');
assert.equal(source.overlaps.length,0);
const preview=quality.auditWallOpenings(demo,h=>schematic.display(h,true));
assert.equal(preview.walls[0].openingCount,2);
assert.ok(preview.minGapMm<source.minGapMm);
assert.deepEqual(demo.walls[0].holes.map(x=>x.id),['LR-A','LR-B']);
assert.deepEqual(demo.cables[0].a,{wall:'SW1',hole:'LR-A'});
assert.deepEqual(demo.cables[0].b,{wall:'SW1',hole:'LR-B'});
assert.equal(JSON.stringify(demo),snap,'preview/audit never persists synthetic coordinates');
assert.equal(schematic.display(demo.walls[0].holes[0],false),demo.walls[0].holes[0]);
assert.equal(schematic.display(demo.walls[0].holes[0],true).u,.438);
assert.match(source.provenance,/kein Aufmaß/);
assert.match(app,/function change\(fn,label=/);
assert.match(app,/function undoRedo\(back\)/);
assert.ok(app.includes('undo.push(JSON.stringify(project))'));
assert.match(app,/function wallSpanMm\(id\)/);
// KAB-0884: distinct 60/110/116 mm openings preserve exact radii.
class Shape{constructor(){this.holes=[];}moveTo(){}lineTo(){}closePath(){}}
class HolePath{absarc(...args){this.arc=args;}}
class ExtrudeGeometry{constructor(shape,options){this.shape=shape;this.options=options;}}
class MeshStandardMaterial{constructor(options){this.options=options;}}
class Mesh{constructor(geometry,material){this.geometry=geometry;this.material=material;}}
const THREE={Shape,Path:HolePath,ExtrudeGeometry,MeshStandardMaterial,Mesh,DoubleSide:2};
const diameters=[60,110,116];
const holes=diameters.map((dn,i)=>({id:'DN-'+dn,u:(i-1)*.38,y:1,r:dn/2000}));
const before=JSON.stringify(holes);
const made=concrete.build(THREE,{width:2,height:2,depth:.12,openings:holes});
assert.equal(made.accepted.length,3);
assert.deepEqual(made.mesh.geometry.shape.holes.map(h=>Math.round(h.arc[2]*2000)),diameters);
assert.equal(JSON.stringify(holes),before);
assert.equal(made.mesh.material.length,2);
// Non-mutating source reference contract.
assert.deepEqual(pdf.walls.map(w=>w.holes.length),[29,13,26,23]);
quality.auditWallOpenings(pdf);
assert.equal(JSON.stringify(pdf),baseline);
console.log('OK: KAB-0051..0054 schematic source/preview geometry; KAB-0884 DN regression');
