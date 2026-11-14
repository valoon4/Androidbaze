'use strict';
/* Giant sprint KAB-0365–0378: never equate a drawn net link with confirmed
 * physical cable continuity. 192 deterministic negative/positive scenario
 * variations, all source snapshots immutable. */
const assert=require('node:assert/strict');
const p=require('../network-route-planner.js');
const clone=x=>JSON.parse(JSON.stringify(x));
const shaft=(id,x)=>({id,x,z:0,name:'Fake '+id,snapshot:{
 walls:[{id:'SW1',holes:[{id:'H1'},{id:'H2'}]}],
 cables:[{id:'C'+id,name:'Synthetic '+id,group:'OLA',
  a:{wall:'SW1',hole:'H1'},b:{wall:'SW1',hole:'H2'}}],
 dims:{w:1000,h:1000,d:1000},
 sourceInfo:{geometryStatus:'surveyed',sourceFile:'fake-photo.txt'}
}});
function fixture(){
 const shafts=[shaft('A',0),shaft('B',1000),shaft('C',2000)];
 return {shafts,links:[{id:'AB',from:'A',to:'B'},{id:'BC',from:'B',to:'C'}],
  routes:[{id:'R',name:'A–C Test',group:'OLA',
   steps:[{shaftId:'A',cableId:'CA'},{shaftId:'B',cableId:'CB'},
    {shaftId:'C',cableId:'CC'}]}]};
}
const base=fixture(),baseline=JSON.stringify(base),route=base.routes[0];
const stable=p.continuity(base,route);
assert.equal(stable.status,'unverified');
assert.deepEqual(stable.errors,[]);
assert.equal(stable.segments.length,3);
assert.equal(stable.transitions.length,2);
assert.deepEqual(stable.transitions.map(t=>t.linkId),['AB','BC']);
assert.ok(stable.segments.every(s=>s.exists&&s.localA==='linked-local-hole'&&s.localB==='linked-local-hole'));
assert.ok(stable.transitions.every(t=>t.physicalConnectionConfirmed===false&&t.requiresEvidence===true));
assert.equal(stable.physicalContinuityConfirmed,false);
assert.equal(stable.needsManualReview,true);
assert.match(stable.warnings.join(' '),/nicht|unverifiziert|keine/i);
assert.ok(stable.segments.every(s=>s.sourceConfirmed===false),
 'Even a fake geometryStatus:surveyed is not a cable transition certificate');
assert.equal(JSON.stringify(base),baseline);
let mutations=0;
const cases=[
 ['missing-link',net=>{net.links.pop()},'broken',/Kein expliziter Link/],
 ['missing-cable',net=>{net.shafts[1].snapshot.cables=[]},'broken',/Kabel.*fehlt/],
 ['missing-local-hole',net=>{net.shafts[0].snapshot.walls[0].holes=[]},'broken',/Lokales Rohr/],
 ['unassigned-start',net=>{net.shafts[0].snapshot.cables[0].a=null},'unverified',/unzugeordnete/],
 ['unassigned-middle',net=>{net.shafts[1].snapshot.cables[0].b=null},'unverified',/unzugeordnete/],
 ['unassigned-end',net=>{net.shafts[2].snapshot.cables[0].b=null},'unverified',/unzugeordnete/],
 ['wrong-group',net=>{net.shafts[1].snapshot.cables[0].group='MS'},'unverified',/Systemkonflikt/],
 ['missing-shaft',net=>{net.routes[0].steps[1].shaftId='missing'},'broken',/Schacht fehlt/],
 ['duplicate-shaft',net=>{net.routes[0].steps[2].shaftId='B'},'broken',/Kein expliziter Link/]
];
for(const [name,mutate,status,re] of cases){
 const changed=fixture();mutate(changed);
 const before=JSON.stringify(changed);
 const check=p.continuity(changed,changed.routes[0]);
 assert.equal(check.status,status,name);
 const issues=[...check.errors,...check.warnings].join(' ');
 assert.match(issues,re,name);
 assert.equal(check.physicalContinuityConfirmed,false,name);
 assert.equal(JSON.stringify(changed),before,name+' must preserve source snapshot');
 mutations++;
}
const empty=p.continuity(fixture(),{id:'R',group:'OLA',steps:[]});
assert.equal(empty.status,'broken');
assert.match(empty.errors.join(' '),/Weniger als zwei/);
assert.throws(()=>p.continuity(fixture(),{steps:Array.from({length:61},()=>({shaftId:'A',cableId:'CA'}))}),/gültige Segmente/);
// 192 deterministic, independently attributable variants against the
// unverified source boundary (no assumed changes to real pipe positions).
for(let i=0;i<192;i++){
 const changed=fixture();
 const index=i%3;
 if(i%4===0)changed.shafts[index].snapshot.cables[0].a=null;
 if(i%4===1)changed.shafts[index].snapshot.cables[0].b={wall:'SW1',hole:'UNKNOWN'};
 if(i%4===2)changed.shafts[index].snapshot.cables[0].group='MS';
 if(i%4===3)changed.shafts[index].snapshot.sourceInfo.geometryStatus='surveyed';
 const before=JSON.stringify(changed);
 const report=p.continuity(changed,changed.routes[0]);
 assert.equal(report.physicalContinuityConfirmed,false);
 assert.ok(report.transitions.every(t=>!t.physicalConnectionConfirmed));
 assert.equal(report.segments.length,3);
 assert.equal(report.status,i%4===1?'broken':'unverified');
 assert.equal(JSON.stringify(changed),before);
 mutations++;
}
assert.ok(mutations>=200);
console.log('OK: '+mutations+' continuity audits do not invent shaft transitions, dangling cables or confirmed physical LR connections');
