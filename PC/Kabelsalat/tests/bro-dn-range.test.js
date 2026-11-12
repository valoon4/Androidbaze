'use strict';
/* Bro: LR stubs shorter, mix DN30 to DN160, drawn proportionally. All sample
 * dimensions remain NOMINAL and synthetic; no PDF/HTML source data are edited.
 */
const assert=require('node:assert/strict');
const fs=require('node:fs'),path=require('node:path');
const p=require('../pipe-dimensions.js');
const concrete=require('../concrete-wall.js');
const components=require('../components.js'),network=require('../network.js');
globalThis.KabelsalatReferences=require('../reference-data.js').factory(components,network);
const examples=require('../examples.js').factory(components,network);
class Shape{constructor(){this.holes=[];}moveTo(){}lineTo(){}closePath(){}}
class HolePath{absarc(...coords){this.arc=coords;}}
class ExtrudeGeometry{constructor(shape,options){this.shape=shape;this.options=options;}}
class MeshStandardMaterial{constructor(o){this.options=o;}}
class Mesh{constructor(geometry,material){this.geometry=geometry;this.material=material;}}
const THREE={Shape,Path:HolePath,ExtrudeGeometry,MeshStandardMaterial,Mesh,DoubleSide:2};
const project=examples.create('diameters'),original=JSON.stringify(project);
assert.ok(project.name.startsWith('FIKTIVE DEMO'));
assert.ok(!project.sourceInfo,'No source reference allowed for synthetic DN mix');
const holes=project.walls.flatMap(w=>w.holes);
assert.equal(holes.length,94);
const candidates=[30,50,60,75,110,116,160];
for(const mm of candidates){
 const h=holes.find(h=>h.dn===mm);
 assert.ok(h,'DN '+mm+' missing in synthetic demo');
 assert.equal(p.apertureMm(h),mm,'Exact schematic radius for DN '+mm);
 assert.equal(p.forHole(h).status,'unverified');
}
const specimen=[30,50,110,160].map((mm,i)=>({id:'LR-DN'+mm,u:-.9+i*.60,y:1,r:mm/2000}));
const built=concrete.build(THREE,{width:2.2,height:2,depth:.12,openings:specimen});
assert.equal(built.accepted.length,4);
assert.deepEqual(built.mesh.geometry.shape.holes.map(h=>Math.round(h.arc[2]*2000)),
 [30,50,110,160],'Visually proportional concrete openings follow nominal sizes');
assert.ok(Math.abs(built.mesh.geometry.shape.holes[2].arc[2]/built.mesh.geometry.shape.holes[1].arc[2]-110/50)<1e-12);
assert.equal(JSON.stringify(project),original,'Renderer may not mutate synthetic data');
const pdf=examples.create('pdf-u028');
assert.deepEqual(pdf.walls.map(w=>w.holes.length),[29,13,26,23]);
assert.ok(pdf.walls.some(w=>w.holes.some(h=>h.dn===160)));
const app=fs.readFileSync(path.resolve(__dirname,'../app.js'),'utf8');
const html=fs.readFileSync(path.resolve(__dirname,'../index.html'),'utf8');
assert.match(app,/addScaledVector\(normal\(known\.wall\),\.16\)/,
 'Open-ended cables should be a short 160 mm graphic marker, not 360 mm');
assert.ok(!app.includes('addScaledVector(normal(known.wall),.36)'),
 'Long 360 mm free cable mark must be gone');
assert.ok(app.includes('knownHole?openingRadius(knownHole)*.48:.009'),
 'End marker radius must adapt to small LR openings');
assert.ok(html.includes('id="wallDn" type="number" min="30" max="160" step="1"'));
assert.ok(html.includes('id="holeDn" min="30" max="160" step="1"'));
assert.ok(app.includes("h.dn=clamp(Number($('holeDn').value)||h.dn,30,160)"),
 'Edited DN must support 30–160 in 1 mm increments');
assert.ok(app.includes("['wallDn','dn',30,160]"),'New LR default must support DN 30–160');
// Preserve existing source data: the separate PDF reference remains 91 openings and still uses original DN values.
console.log('OK: Short 160mm visual cable-end markers, DN30–160 synthetic LR mix, DN50 < DN110 and PDF source unchanged');
