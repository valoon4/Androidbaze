'use strict';
const assert=require('node:assert/strict'),fs=require('node:fs'),path=require('node:path');
const components=require('../components.js'),network=require('../network.js'),quality=require('../quality.js');
globalThis.KabelsalatComponents=components;globalThis.KabelsalatNetwork=network;
globalThis.KabelsalatQuality=quality;globalThis.KabelsalatReports=require('../reports.js');
const refs=require('../reference-data.js').factory(components,network);
globalThis.KabelsalatReferences=refs;
const examples=require('../examples.js').factory(components,network);
globalThis.KabelsalatSamples=examples;
globalThis.KabelsalatPdfGeometry=require('../pdf-geometry.js');
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
assert.equal(u.cables.filter(c=>c.a&&c.b).length,11,
 'U_028: show 11 fully connected 3D paths and report 7 incomplete cable routes');
assert.ok(u.cables.some(c=>c.referenceGroup==='TK'&&c.group==='Weitere'),'Original source group must be retained');
const t=validProject(examples.create('source-t021'));
assert.equal(t.cables.filter(c=>c.mappingStatus==='shown').length,1);
assert.ok(t.cables.every(c=>c.a&&c.b),'All shown T reference endpoints must be geometrically present in imported model');
assert.equal(examples.manifest.length,7);
const net=validProject(examples.create('source-both')).network;
const switched=validProject({...net.shafts[1].snapshot,network:net,activeShaftId:'13_U_028'});
assert.equal(switched.sourceInfo.shaft,'13_U_028');
assert.equal(switched.network.shafts[0].snapshot.sourceInfo.shaft,'13_T_021');
const pdf=validProject(examples.create('pdf-u028'));
assert.deepEqual(pdf.walls.map(w=>w.holes.length),[29,13,26,23]);
for(const [wid,hid,u,v] of [
 ['SW1','SW1_T1_LR01',.0513,.8373],
 ['SW1','SW1_T2_LR02',.9653,.8373],
 ['SW2','SW2_T3_LR01',.8597,.8174],
 ['SW3','SW3_T1_LR26',.0754,.1790],
 ['SW4','SW4_T1_LR23',.2127,.3345]
]){
 const hole=pdf.walls.find(w=>w.id===wid).holes.find(h=>h.id===hid);
 assert.ok(hole,'Missing PDF vector-coordinate reference '+hid);
 assert.equal(hole.u,u,'Relative PDF x-center '+hid);
 assert.equal(hole.v,v,'Relative PDF y-center '+hid);
}
assert.ok(pdf.sourceInfo.warning.includes('PDF-Vektorgrafik'));
const ph=(w,t,lr)=>w+'_T'+t+'_LR'+String(lr).padStart(2,'0');
const pp=(w,t,lr)=>pdf.cables.filter(c=>[c.a,c.b].some(end=>end?.hole===ph(w,t,lr)));
for(const w of pdf.walls)for(const h of w.holes)
 assert.equal(pdf.cables.filter(c=>[c.a,c.b].some(e=>e?.hole===h.id)).length,
  h.sourceCount,'PDF occupancy '+h.id);
const nominal=(w,t,lr)=>pdf.walls.find(x=>x.id===w).holes.find(h=>h.id===ph(w,t,lr)).dn;
assert.equal(nominal('SW1',1,27),160);
assert.equal(nominal('SW1',1,21),160);
assert.equal(nominal('SW2',1,5),160);
assert.equal(nominal('SW2',3,1),75);
assert.equal(nominal('SW1',2,1),63);
assert.equal(nominal('SW3',1,26),160);
for(const [name,start,end] of [
 ['MS-SNG-0056',ph('SW1',1,27),ph('SW2',1,5)],
 ['MS-SNG-0038',ph('SW1',1,21),ph('SW3',1,26)]])
 assert.ok(pdf.cables.some(c=>c.name===name&&c.a?.hole===start&&c.b?.hole===end));
assert.equal(pp('SW1',1,8).filter(c=>c.group==='MS').length,0);
assert.equal(pp('SW1',1,9).filter(c=>c.group==='MS').length,0);
assert.deepEqual(pp('SW2',1,9).map(c=>c.name).sort(),
 ['OLSP 2060','OLSP 2061','OLSP 2062','OLSP 2063']);
assert.deepEqual(pp('SW2',1,10).map(c=>c.name).sort(),
 ['OLSP 2960','OLSP 2961','OLSP 2962','OLSP 2963']);
assert.equal(pp('SW2',3,1).length,5);
assert.equal(pp('SW2',3,2).length,0);
assert.equal(pp('SW3',1,18).length,16);
const msCablePaths=pdf.cables.filter(c=>c.group==='MS');
const olaCablePaths=pdf.cables.filter(c=>c.group==='OLA');
assert.equal(msCablePaths.length,2,'Two named MS cable runs in PDF V2');
assert.ok(msCablePaths.every(c=>c.a&&c.b),'MS connections need source-confirmed endpoints');
assert.equal(olaCablePaths.length,4,'Four OLA occurrences in PDF V2');
assert.ok(olaCablePaths.every(c=>c.a&&!c.b),
 'Never invent unreferenced OLA destinations for Powerpanel presentation');
assert.equal(pdf.supports.length,0,'No fabricated Powerpanel supports in source reference');
assert.equal(pdf.enclosures.MS.visible,false);
assert.equal(pdf.enclosures.OLA.visible,false);
assert.ok(pdf.sourceInfo.warning.includes('Platzhalter'));
assert.ok(pdf.cables.some(c=>!c.b));
assert.deepEqual(validProject(JSON.parse(JSON.stringify(pdf))),pdf);
console.log('OK: original HTML datasets unchanged; PDF V2 91 nominal LRs and 4 walls correctly transcribed');
