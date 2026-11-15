'use strict';
/* Bro, 2026-11-15: a depicted opening can be a bare concrete through-hole.
 * Tests never treat DN as a verified physical dimension or a pipe as mandatory. */
const assert=require('node:assert/strict'),fs=require('node:fs'),path=require('node:path');
const pipe=require('../pipe-dimensions.js'),quality=require('../quality.js');
const model=require('../hole-editor-ops.js');
const components=require('../components.js'),network=require('../network.js');
globalThis.KabelsalatComponents=components;
globalThis.KabelsalatNetwork=network;
globalThis.KabelsalatRoutePlanner=require('../network-route-planner.js');
globalThis.KabelsalatWorldRoute=require('../network-world-route.js');
globalThis.KabelsalatQuality=quality;
globalThis.KabelsalatReports=require('../reports.js');
globalThis.KabelsalatReferences=require('../reference-data.js').factory(components,network);
globalThis.KabelsalatSamples=require('../examples.js').factory(components,network);
globalThis.KabelsalatPdfGeometry=require('../pdf-geometry.js');
globalThis.KabelsalatSchematicPositions=require('../schematic-positions.js');
globalThis.KabelsalatPipeDimensions=pipe;
globalThis.KabelsalatPhotoCandidates=require('../photo-candidates.js');
globalThis.KabelsalatConcreteWall=require('../concrete-wall.js');
globalThis.KabelsalatSpacingAdjust=require('../spacing-adjust.js');
globalThis.KabelsalatHoleEditorOps=model;
globalThis.KabelsalatCableEditorOps=require('../cable-editor-ops.js');
globalThis.KabelsalatConstructionV2=require('../construction-v2.js');
const app=fs.readFileSync(path.join(__dirname,'../app.js'),'utf8');
const html=fs.readFileSync(path.join(__dirname,'../index.html'),'utf8');
const prefix=app.split('let project;')[0].replace(/^\(\(\) => \{\s*/,'');
const validProject=new Function(prefix+'\nreturn validProject;')();
const untouched={id:'P1',dn:110,u:.25,v:.35};
assert.equal(pipe.openingType(untouched),'unknown');
assert.equal(pipe.apertureMm(untouched),110,'Legacy schematic DN remains display fallback');
assert.match(pipe.describe(untouched),/unbekannt/);
const bare={...untouched,openingType:'bare-hole',dimensionSpec:{openingMm:75,
 source:'Fiktive Testangabe',status:'candidate',method:'manual'}};
const exact=pipe.forHole(bare);
assert.equal(pipe.openingType(bare),'bare-hole');
assert.equal(pipe.apertureMm(bare),75,'Actual specified hole governs concrete geometry');
assert.equal(exact.nominalMm,null,'Bare hole has no pipe nominal DN');
assert.equal(exact.innerMm,null,'Bare hole has no pipe inner diameter');
assert.equal(exact.outerMm,null,'Bare hole has no pipe outer diameter');
assert.equal(exact.wallThicknessMm,null,'Bare hole has no pipe wall thickness');
assert.match(pipe.describe(bare),/Nur Wandloch, kein Leerrohr/);
assert.match(pipe.describe(bare),/Öffnung-Ø 75 mm/);
assert.doesNotMatch(pipe.describe(bare),/DN 110/);
const mounted={...untouched,openingType:'conduit',
 dimensionSpec:{nominalMm:110,innerMm:95,outerMm:116,openingMm:120}};
assert.equal(pipe.openingType(mounted),'conduit');
assert.equal(pipe.apertureMm(mounted),120);
assert.equal(pipe.forHole(mounted).innerMm,95);
assert.match(pipe.describe(mounted),/Leerrohr vorhanden/);
const noMeasure={...untouched,openingType:'bare-hole'};
assert.match(pipe.describe(noMeasure),/Loch-Ø unbekannt/);
assert.equal(pipe.apertureMm(noMeasure),110,'Unmeasured bare hole stays schematic only');
const project={dims:{w:2400,d:1900,h:2100},
 walls:[{id:'SW1',thickness:120,holes:[
  {...bare,id:'BARE',u:.45,v:.5},{...mounted,id:'LINED',u:.75,v:.5}]}],
 cables:[{id:'CABLE-1',name:'Kabel Ø 80',diameter:80,
  a:{wall:'SW1',hole:'BARE'},b:null},
 {id:'CABLE-2',name:'Kabel Ø 90',diameter:90,
  a:{wall:'SW1',hole:'LINED'},b:null}]};
const before=JSON.stringify(project),net={shafts:[{id:'S1',snapshot:project}],
 links:[],routes:[]};
const findings=quality.evaluate(net,{});
const bareError=findings.issues.find(x=>x.code==='CABLE_TOO_WIDE'&&x.ref==='CABLE-1');
assert.ok(bareError,'Cable Ø80 cannot fit bare Ø75');
assert.match(bareError.detail,/dokumentierter Loch-Ø 75/);
assert.equal(findings.issues.some(x=>x.code==='CABLE_TOO_WIDE'&&x.ref==='CABLE-2'),false,
 'Cable Ø90 can fit documented pipe inner Ø95');
assert.equal(JSON.stringify(project),before,'Quality checks never alter the project');
project.cables[0].diameter=75;
assert.equal(quality.evaluate(net,{}).issues.some(x=>
 x.code==='CABLE_TOO_WIDE'&&x.ref==='CABLE-1'),false,'Exact clearance is not too wide');
project.walls[0].holes[0].dimensionSpec.openingMm=74.9;
assert.equal(quality.evaluate(net,{}).issues.some(x=>
 x.code==='CABLE_TOO_WIDE'&&x.ref==='CABLE-1'),true,'Even 0.1mm too small must fail');
project.walls[0].holes[0].dimensionSpec.openingMm=null;
const missing=quality.evaluate(net,{});
assert.ok(missing.issues.some(x=>x.code==='BARE_HOLE_SIZE_UNKNOWN'));
assert.ok(missing.issues.some(x=>x.code==='BARE_HOLE_CABLE_UNCHECKABLE'));
assert.ok(!missing.issues.some(x=>x.code==='CABLE_TOO_WIDE'&&x.ref==='CABLE-1'),
 'Unmeasured hole may never pass/fail cable by fictitious nominal DN');
const source=globalThis.KabelsalatSamples.create('station');
const base=validProject(source);
const srcCount=base.network.shafts.reduce((n,s)=>n+
 s.snapshot.walls.reduce((count,w)=>count+w.holes.length,0),0);
const edited=JSON.parse(JSON.stringify(base));
const h=edited.walls[0].holes[0];
const originalId=h.id;
h.openingType='bare-hole';h.dimensionSpec={openingMm:75,source:'Fiktiv',
 status:'candidate',method:'manual'};
const valid=validProject(edited);
assert.equal(valid.walls[0].holes[0].openingType,'bare-hole');
assert.equal(pipe.apertureMm(valid.walls[0].holes[0]),75);
assert.equal(valid.walls[0].holes[0].id,originalId);
const saved=validProject(JSON.parse(JSON.stringify(valid)));
assert.deepEqual(saved,valid,'Bare hole survives canonical v5 JSON roundtrip');
assert.equal(base.network.shafts.reduce((n,s)=>n+
 s.snapshot.walls.reduce((count,w)=>count+w.holes.length,0),0),srcCount);
assert.deepEqual(validProject(source),base,'Untouched legacy version stays exact');
const inspected=model.candidate({dims:{w:2400,d:1900,h:2100},
 walls:[{id:'SW1',holes:[]}]},'SW1',{
 xMm:60,yMm:100,dn:160,openingMm:75});
assert.equal(inspected.ok,true,'Hole Ø75 must fit 60mm edge distance despite placeholder DN160');
assert.equal(inspected.openingMm,75);
const rejected=model.candidate({dims:{w:2400,d:1900,h:2100},
 walls:[{id:'SW1',holes:[]}]},'SW1',{
 xMm:30,yMm:100,dn:160,openingMm:75});
assert.equal(rejected.ok,false,'Actual opening radius must trigger wall-edge rejection');
assert.ok(html.includes('id="holeOpeningType"'));
assert.ok(html.includes('Nur rundes Loch in Beton (ohne Leerrohr)'));
assert.ok(app.includes("pipeDimensions.openingType(h)==='bare-hole'"));
assert.ok(app.includes('Nur Loch: Loch-Ø von 20–600 mm explizit eingeben.'));
assert.ok(app.includes("'Loch Ø'+Math.round(pipeDimensions.apertureMm(h))"));
assert.ok(app.includes("if(openingType==='unknown')delete h.openingType;else h.openingType=openingType"));
console.log('BARE-HOLE BRO FEEDBACK OK: pure holes vs conduits, real opening Ø fit, 2D/3D, source-safe JSON and edge geometry');
