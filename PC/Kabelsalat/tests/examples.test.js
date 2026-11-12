'use strict';
const assert=require('node:assert/strict');
const fs=require('node:fs'),path=require('node:path');
const components=require('../components.js'),network=require('../network.js');
const quality=require('../quality.js');
globalThis.KabelsalatComponents=components;
globalThis.KabelsalatNetwork=network;
globalThis.KabelsalatQuality=quality;
globalThis.KabelsalatReports=require('../reports.js');
globalThis.KabelsalatReferences=require('../reference-data.js').factory(components,network);
const samples=require('../examples.js').factory(components,network);
globalThis.KabelsalatSamples=samples;
globalThis.KabelsalatPdfGeometry=require('../pdf-geometry.js');
globalThis.KabelsalatConcreteWall=require('../concrete-wall.js');
globalThis.KabelsalatConstructionV2=require('../construction-v2.js');
const app=fs.readFileSync(path.resolve(__dirname,'../app.js'),'utf8');
const before=app.split('let project;')[0].replace(/^\(\(\) => \{\s*/,'');
const {validProject}=new Function(before+'\nreturn {validProject};')();
const scenarios=[
 ['reference','01_Referenzschacht_13_T_021_DEMO.json',1,16,94],
 ['station','02_Bahnhofsnetz_4_Schaechte_DEMO.json',4,64,376],
 ['quality','03_Prueffall_mit_absichtlichen_Warnungen_DEMO.json',1,16,96]
];
for(const [id,file,shafts,cables,holes] of scenarios){
 const generated=validProject(samples.create(id));
 const persisted=JSON.parse(fs.readFileSync(path.resolve(__dirname,'../examples',file),'utf8'));
 assert.deepEqual(persisted,generated,id+' generated and downloadable JSON must match');
 const roundtrip=validProject(JSON.parse(JSON.stringify(persisted)));
 assert.deepEqual(roundtrip,persisted,'All example IDs/geometries survive load: '+id);
 assert.equal(persisted.network.shafts.length,shafts,id+' shafts');
 assert.equal(persisted.network.shafts.reduce((sum,s)=>sum+s.snapshot.cables.length,0),cables,id+' cables');
 assert.equal(persisted.network.shafts.reduce((sum,s)=>sum+s.snapshot.walls.reduce((n,w)=>n+w.holes.length,0),0),holes,id+' holes');
 assert.ok(persisted.name.includes('DEMO'),'No sample may be presented as field-survey data');
 const issues=quality.evaluate(persisted.network,persisted.network.quality);
 if(id==='quality'){
  assert.ok(issues.counts.error>=2,'Example checks must expose errors');
  assert.ok(issues.issues.some(x=>x.code==='MISSING_END'),'Missing cable endpoint must be reported');
 }else{
  assert.equal(issues.counts.error,0,id+' clean demo');
  assert.equal(issues.counts.warning,0,id+' clean demo warnings');
 }
 for(const r of persisted.network.routes)assert.deepEqual(network.issues(persisted.network,r),[],id+' complete demo route');
}
const station=samples.create('station');
assert.equal(station.network.routes.length,3);
assert.ok(station.network.shafts.every(s=>s.snapshot.supports.length>=2));
assert.ok(station.network.shafts.every(s=>s.snapshot.enclosures.OLA.panels.length===6));
assert.ok(station.network.shafts.every(s=>s.snapshot.enclosures.MS.panels.length===6));
assert.equal(samples.manifest.length,7);
console.log('OK – 3 sample projects, 4 shafts, 64 cables, 376 openings, routes, JSON roundtrip, deliberate QA conflicts');
