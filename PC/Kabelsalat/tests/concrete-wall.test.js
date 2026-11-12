'use strict';
const assert=require('node:assert/strict'),fs=require('node:fs'),path=require('node:path');
const concrete=require('../concrete-wall.js');
class Shape {
 constructor(){this.holes=[];this.points=[];}
 moveTo(...p){this.points.push(['move',...p]);}
 lineTo(...p){this.points.push(['line',...p]);}
 closePath(){this.points.push(['close']);}
}
class Path {absarc(...p){this.arc=p;}}
class ExtrudeGeometry {
 constructor(shape,options){this.shape=shape;this.options=options;}
}
class MeshStandardMaterial {constructor(options){this.options=options;}}
class DataTexture {
 constructor(data,width,height,format){
  this.image={data,width,height};this.format=format;this.repeat={set:(x,y)=>{this.repeat.x=x;this.repeat.y=y;}};
 }
 dispose(){this.disposed=true;}
}
class Mesh {constructor(geometry,material){this.geometry=geometry;this.material=material;this.userData={};this.position={set(){}};}}
const THREE={Shape,Path,ExtrudeGeometry,MeshStandardMaterial,Mesh,DataTexture,DoubleSide:2,RGBAFormat:1023,RepeatWrapping:1000,SRGBColorSpace:'srgb'};
const build=(openings,{width=2,height=2,depth=.12,opacity=1}={})=>
 concrete.build(THREE,{width,height,depth,openings,opacity});
const cases=[
 {id:'SMALL',u:-.25,y:.5,r:.03},
 {id:'LARGE',u:.3,y:1,r:.08}
];
const original=JSON.stringify(cases);
const normal=build(cases);
assert.equal(normal.accepted.length,2);
assert.equal(normal.excluded.length,0);
assert.equal(normal.mesh.geometry.shape.holes.length,2);
assert.equal(normal.mesh.geometry.options.depth,.12);
assert.equal(normal.mesh.geometry.options.bevelEnabled,false);
assert.ok(normal.mesh.geometry.options.curveSegments>=24,'Circle bore edges must be smooth');
assert.ok(normal.mesh.material.length===2,'Concrete faces and dark bore must use separate materials');
assert.notEqual(normal.mesh.material[0].options.color,normal.mesh.material[1].options.color);
assert.equal(normal.mesh.material[0].options.opacity,1);
const surface=normal.mesh.material[0].options.map;
assert.ok(surface instanceof DataTexture,'Concrete surface must use an offline generated procedural texture');
assert.equal(surface.image.width,128);
assert.equal(surface.image.height,128);
assert.ok(surface.repeat.x>0&&surface.repeat.y>0,'Concrete aggregate must tile across wall surface');
const values=new Set(surface.image.data.filter((_,i)=>i%4===0));
assert.ok(values.size>20,'Texture must have visibly varied but deterministic concrete grains');
assert.equal(normal.mesh.material[0].options.transparent,false);
assert.deepEqual(normal.mesh.geometry.shape.holes.map(x=>x.arc[2]),[.03,.08]);
assert.deepEqual(normal.accepted.map(x=>x.id),['SMALL','LARGE']);
assert.equal(JSON.stringify(cases),original,'Rendering must not modify pipe data');
const ghost=build(cases,{opacity:.8});
assert.equal(ghost.mesh.material[0].options.transparent,true);
assert.equal(ghost.mesh.material[0].options.depthWrite,false);
assert.equal(ghost.mesh.material[0].options.opacity,.8);
const bad=build([
 {id:'A',u:0,y:.8,r:.15},
 {id:'B',u:.1,y:.8,r:.15},
 {id:'C',u:.95,y:.8,r:.1},
 {id:'D',u:0,y:NaN,r:.1},
 {id:'E',u:.4,y:1.5,r:0}
]);
assert.deepEqual(bad.accepted.map(x=>x.id),['A']);
assert.deepEqual(bad.excluded.map(x=>x.reason),['overlap','outside','outside','outside']);
assert.equal(bad.mesh.geometry.shape.holes.length,1);
assert.equal(build([]).mesh.geometry.shape.holes.length,0);
const diameters=[60,110,116],dims=require('../pipe-dimensions.js');
const mixed=diameters.map((diameter,i)=>({id:'mix-'+i,u:(i-1)*.36,y:1,r:dims.apertureMm({dn:diameter})/2000}));
const sized=build(mixed);
assert.equal(sized.mesh.geometry.shape.holes.length,3);
assert.deepEqual(sized.mesh.geometry.shape.holes.map(p=>Math.round(p.arc[2]*2000)),diameters,
 'Opening geometry must show 60/110/116 as distinct circles');
const withBore=dims.apertureMm({dn:60,dimensionSpec:{openingMm:116}});
assert.equal(withBore,116,'Real bore replaces illustrative nominal DN when explicitly supplied');
assert.throws(()=>concrete.build(THREE,{width:0,height:2,depth:.12,openings:[]}));

// Both real reference shaft models must remain unmodified by the new view renderer.
const originals=[
 ['13_T_021','04_Referenz_13_T_021_aus_HTML.json',48],
 ['13_U_028','05_Referenz_13_U_028_aus_HTML.json',78]
];
for(const [shaft,file,count] of originals){
 const project=JSON.parse(fs.readFileSync(path.join(__dirname,'../examples',file),'utf8'));
 const frozen=JSON.stringify(project),s=project.dims;
 const all=project.walls.flatMap(w=>w.holes);
 assert.equal(all.length,count,'Expected imported pipe count '+shaft);
 for(const wall of project.walls){
  const width=(wall.id==='SW1'||wall.id==='SW3'?s.d:s.w)/1000;
  const input=wall.holes.map(h=>({id:h.id,u:(h.u-.5)*width,y:h.v*s.h/1000,r:(h.dn||70)/2000}));
  const built=build(input,{width,height:s.h/1000,depth:wall.thickness/1000});
  assert.equal(built.accepted.length+built.excluded.length,wall.holes.length,
   'All original pipes must be retained in data even if schematic holes collide');
 }
 assert.equal(JSON.stringify(project),frozen,'No data mutations in '+shaft);
}
// PDF-V2 is a separate, confirmed-by-PDF transcription with 91 schematic centers.
const components=require('../components.js'),network=require('../network.js');
const pdf=require('../reference-data.js').factory(components,network);
globalThis.KabelsalatReferences=pdf; // Source examples load from the canonical reference factory.
const examples=require('../examples.js').factory(components,network);
const project=examples.create('pdf-u028'),copy=JSON.stringify(project),sizes=project.dims;
assert.deepEqual(project.walls.map(w=>w.holes.length),[29,13,26,23]);
for(const wall of project.walls){
 const width=(wall.id==='SW1'||wall.id==='SW3'?sizes.w:sizes.d)/1000;
 const display=require('../schematic-positions.js');
 const holes=wall.holes.map(h=>{const p=display.display(h,true);return {id:h.id,u:(p.u-.5)*width,y:p.v*sizes.h/1000,r:(h.dn||70)/2000};});
 const built=build(holes,{width,height:sizes.h/1000,depth:wall.thickness/1000});
 assert.equal(built.accepted.length,wall.holes.length,
  'PDF-V2 91 real circular openings must survive the geometry cutter for '+wall.id);
 assert.equal(built.excluded.length,0);
 assert.equal(built.mesh.geometry.shape.holes.length,wall.holes.length);
}
assert.equal(JSON.stringify(project),copy,'PDF source positions and occupancy must remain unchanged');
console.log('OK KAB-0877: concrete wall through-holes, two materials, 48/78/91 source pipes unchanged');
