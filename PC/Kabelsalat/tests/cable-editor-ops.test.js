'use strict';
const assert=require('node:assert/strict');
const ops=require('../cable-editor-ops.js');
function fixture(){
 return {walls:[
  {id:'SW1',holes:[{id:'LR1',dn:50,sourceAddress:'Trasse Z'},{id:'LR2',dn:110,sourceAddress:'Trasse A'}]},
  {id:'SW2',holes:[{id:'LR1',dn:30,sourceAddress:'Trasse A'}]}],
  cables:[
   {id:'c1',name:'Kabel A',group:'OLA',diameter:12,a:{wall:'SW1',hole:'LR1'},b:{wall:'SW1',hole:'LR2'},route:[],source:'Plan 1'},
   {id:'c2',name:'Kabel B',group:'MS',diameter:10,a:{wall:'SW2',hole:'LR1'},b:null,route:[]}],
  activeShaftId:'S1',cableTypes:[],
  network:{routes:[{id:'R1',name:'Route 1',steps:[{shaftId:'S1',cableId:'c1'}]}]}};
}
let p=fixture();
assert.deepEqual(ops.holeDependencies(p,'SW1','LR1').map(x=>x.cableId),['c1']);
assert.deepEqual(ops.holeDependencies(p,'SW2','LR1').map(x=>x.cableId),['c2']);
assert.equal(ops.pipeRows(p,'dn')[0].dn,30);
assert.equal(ops.pipeRows(p,'trasse')[0].trasse,'Trasse A');
assert.equal(ops.pipeRows(p,'wall')[0].wall,'SW1');
assert.throws(()=>ops.pipeRows(p,'evil'),/Sortierschlüssel/);
const before=JSON.stringify(p),holePlan=ops.planHoleDeletion(p,'SW1','LR1');
assert.equal(JSON.stringify(p),before,'Deletion preview must be read-only');
assert.throws(()=>ops.applyHoleDeletion(p,holePlan,false),/bestätigen/);
assert.equal(JSON.stringify(p),before);
const count=ops.applyHoleDeletion(p,holePlan,true);
assert.equal(count,1);
assert.equal(p.walls[0].holes.length,1);
assert.equal(p.cables[0].a,null);
assert.deepEqual(p.cables[0].unresolvedA,{wall:'SW1',hole:'LR1',reason:'Leerrohr entfernt'});
assert.deepEqual(p.cables[0].b,{wall:'SW1',hole:'LR2'});
assert.deepEqual(p.cables[1],fixture().cables[1], 'Same hole ID on another wall must be untouched');
assert.equal(p.cables[0].reviewStatus,'Zur Prüfung');
assert.throws(()=>ops.applyHoleDeletion(p,holePlan,true),/veraltet/);
assert.deepEqual(JSON.parse(JSON.stringify(p)).cables[0].unresolvedA,p.cables[0].unresolvedA);
p=fixture();p.sourceInfo={shaft:'Import'};
assert.throws(()=>ops.planHoleDeletion(p,'SW1','LR1'),/Importreferenz/);
p=fixture();
assert.equal(ops.uniqueCableId(p),'cable-0001');
const newId=ops.createCable(p,'Neues Kabel');
assert.equal(newId,'cable-0001');
assert.equal(p.cables.at(-1).a,null);
assert.equal(p.cables.at(-1).reviewStatus,'Entwurf');
assert.throws(()=>ops.updateCable(p,newId,{diameter:0}),/2 und 80/);
assert.throws(()=>ops.updateCable(p,newId,{diameter:NaN}),/2 und 80/);
assert.throws(()=>ops.updateCable(p,newId,{name:' '}),/Kabelname/);
assert.equal(p.cables.at(-1).diameter,12,'Invalid edits cannot mutate');
ops.updateCable(p,newId,{name:'Kabel C',group:'OLA',diameter:25.5,technicalCategory:'Faseroptisch'});
assert.equal(p.cables.at(-1).technicalCategory,'Faseroptisch');
assert.equal(p.cables.at(-1).group,'OLA');
assert.equal(p.cables.at(-1).diameter,25.5);
// Editing any approved metadata invalidates its prior review; no-op is not a change.
p.cables.at(-1).reviewStatus='Freigegeben';
ops.updateCable(p,newId,{name:'Kabel C'});
assert.equal(p.cables.at(-1).reviewStatus,'Freigegeben','No-op must preserve review');
ops.updateCable(p,newId,{technicalCategory:'Energie'});
assert.equal(p.cables.at(-1).reviewStatus,'Zur Prüfung','Material edit must invalidate approval');
p.cables.at(-1).reviewStatus='Freigegeben';
assert.throws(()=>ops.updateCable(p,newId,{diameter:-1}),/2 und 80/);
assert.equal(p.cables.at(-1).reviewStatus,'Freigegeben','Rejected edit must not invalidate approval');
assert.throws(()=>ops.setEndpoint(p,newId,'a',{wall:'SW9',hole:'LR1'}),/nicht gefunden/);
assert.throws(()=>ops.setEndpoint(p,newId,'a',{wall:'SW1',hole:'unknown'}),/nicht gefunden/);
ops.setEndpoint(p,newId,'a',{wall:'SW1',hole:'LR1'});
ops.setEndpoint(p,newId,'b',{wall:'SW2',hole:'LR1'});
assert.deepEqual(p.cables.at(-1).b,{wall:'SW2',hole:'LR1'});
ops.swapEndpoints(p,newId);
assert.deepEqual(p.cables.at(-1).a,{wall:'SW2',hole:'LR1'});
assert.deepEqual(p.cables.at(-1).b,{wall:'SW1',hole:'LR1'});
ops.setEndpoint(p,newId,'b',null);
assert.equal(p.cables.at(-1).b,null,'One-sided cable must remain valid');
const typeId=ops.addType(p,{name:'Testtyp',category:'Faseroptisch',diameter:26});
assert.equal(typeId,'type-1');
ops.updateCable(p,newId,{typeId});
assert.equal(p.cables.at(-1).typeId,'type-1');
assert.throws(()=>ops.updateCable(p,newId,{typeId:'type-missing'}),/Katalog/);
const copied=ops.duplicateCable(p,'c1');
assert.notEqual(copied,'c1');assert.notEqual(copied,newId);
assert.equal(p.cables.at(-1).source,'Plan 1');
assert.equal(p.cables.at(-1).sourceCopyOf,'c1');
assert.equal(p.cables.at(-1).reviewStatus,'Zur Prüfung');
assert.equal(new Set(p.cables.map(c=>c.id)).size,p.cables.length);
assert.deepEqual(ops.affectedRoutes(p,'c1'),[{id:'R1',name:'Route 1'}]);
const beforeDelete=JSON.stringify(p),plan=ops.planCableDeletion(p,'c1');
assert.equal(JSON.stringify(p),beforeDelete);
assert.throws(()=>ops.applyCableDeletion(p,plan),/bestätigen/);
assert.equal(ops.applyCableDeletion(p,plan,true).length,1);
assert.equal(p.cables.some(c=>c.id==='c1'),false);
assert.equal(p.network.routes[0].steps[0].cableId,'c1','Preserve unresolved route, no silent reroute');
assert.throws(()=>ops.applyCableDeletion(p,plan,true),/veraltet/);
console.log('OK: cable editor: hole/wall dependencies, lossless unresolved ends, DN/trasse sorting, catalogue, roundtrip, routing warnings and unique IDs');
