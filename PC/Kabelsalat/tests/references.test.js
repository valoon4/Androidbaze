'use strict';
const assert=require('node:assert/strict'),fs=require('node:fs'),path=require('node:path');
const components=require('../components.js'),network=require('../network.js'),quality=require('../quality.js');
globalThis.KabelsalatComponents=components;globalThis.KabelsalatNetwork=network;
globalThis.KabelsalatQuality=quality;globalThis.KabelsalatReports=require('../reports.js');
const refs=require('../reference-data.js').factory(components,network);
globalThis.KabelsalatReferences=refs;
const examples=require('../examples.js').factory(components,network);
globalThis.KabelsalatSamples=examples;
const app=fs.readFileSync(path.join(__dirname,'../app.js'),'utf8');
const before=app.split('let project;')[0].replace(/^\(\(\) => \{\s*/,'');
const {validProject}=new Function(before+'\nreturn {validProject};')();
const cases=[
 ['source-t021','04_Referenz_13_T_021_aus_HTML.json',1,48,16,0,0],
 ['source-u028','05_Referenz_13_U_028_aus_HTML.json',1,78,18,15,7],
 ['source-both','06_Referenzvergleich_2_Schaechte_ohne_Verknuepfung.json',2,126,34,15,7]
];
for(const [id,name,shafts,ports,cables,markers,missing] of cases){
 const generated=validProject(examples.create(id));
 const saved=JSON.parse(fs.readFileSync(path.join(__dirname,'../examples',name),'utf8'));
 assert.deepEqual(saved,generated,id+' JSON fixture must match application version');
 assert.deepEqual(validProject(JSON.parse(JSON.stringify(saved))),saved,'Project must roundtrip: '+id);
 assert.equal(saved.network.shafts.length,shafts);
 assert.equal(saved.network.shafts.reduce((n,s)=>n+s.snapshot.walls.reduce((t,w)=>t+w.holes.length,0),0),ports);
 assert.equal(saved.network.shafts.reduce((n,s)=>n+s.snapshot.cables.length,0),cables);
 assert.equal(saved.network.shafts.reduce((n,s)=>n+(s.snapshot.sourceInfo?.planAnchors.length||0),0),markers);
 assert.equal(saved.network.shafts.reduce((n,s)=>n+s.snapshot.cables.filter(c=>!c.a||!c.b).length,0),missing);
 assert.equal(saved.network.links.length,0,'No fabricated physical link between separate source shafts');
 for(const sh of saved.network.shafts){
  assert.equal(sh.snapshot.sourceInfo.geometryStatus,'schematic');
  assert.ok(sh.snapshot.sourceInfo.sourceFile.endsWith('.html'));
  assert.ok(sh.snapshot.walls.flatMap(w=>w.holes).every(h=>h.nominalDnConfirmed===false));
  assert.ok(sh.snapshot.cables.every(c=>c.sourceOriginalA&&c.sourceOriginalB));
  assert.ok(sh.snapshot.cables.every(c=>c.drawingDiameterOnly===true));
  const physical=new Set(sh.snapshot.walls.flatMap(w=>w.holes.map(h=>h.id)));
  assert.ok(sh.snapshot.sourceInfo.planAnchors.every(p=>p.physicalHole===false&&!physical.has(p.id)));
 }
 const warnings=quality.evaluate(saved.network,saved.network.quality);
 assert.ok(warnings.issues.some(x=>x.code==='SCHEMATIC_REFERENCE'));
 if(id!=='source-t021')assert.ok(warnings.issues.some(x=>x.code==='PLAN_MARKERS_UNCONFIRMED'));
}
const u=validProject(examples.create('source-u028'));
assert.equal(u.cables.filter(c=>c.sourceOriginalA?.startsWith('PLAN_')||c.sourceOriginalB?.startsWith('PLAN_')).length,7);
assert.ok(u.sourceInfo.planAnchors.length===15);
assert.ok(u.cables.some(c=>c.referenceGroup==='TK'&&c.group==='Weitere'),'Original source group must be retained');
const t=validProject(examples.create('source-t021'));
assert.equal(t.cables.filter(c=>c.mappingStatus==='shown').length,1);
assert.ok(t.cables.every(c=>c.a&&c.b),'All shown T reference endpoints must be geometrically present in imported model');
assert.equal(examples.manifest.length,6);
const net=validProject(examples.create('source-both')).network;
const switched=validProject({...net.shafts[1].snapshot,network:net,activeShaftId:'13_U_028'});
assert.equal(switched.sourceInfo.shaft,'13_U_028');
assert.equal(switched.network.shafts[0].snapshot.sourceInfo.shaft,'13_T_021');
console.log('OK: 2 original sources, 126 physical holes, 34 cables, 15 plan-only markers, 7 unconfirmed endpoints and JSON roundtrips');
