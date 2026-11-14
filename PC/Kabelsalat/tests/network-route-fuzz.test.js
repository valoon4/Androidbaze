'use strict';
/* Giant network QA: 320 deterministic, independently checked graph configurations.
 * Different reference algorithm for path length and connectivity, no network mutation.
 * Synthetic, NON-physical models; not a certificate for cable route feasibility.
 */
const assert=require('node:assert/strict');
const planner=require('../network-route-planner.js');
let state=0x1f7cb329;
function random(){state=(Math.imul(state,1664525)+1013904223)>>>0;return state/4294967296;}
const rand=n=>Math.floor(random()*n);
const round=v=>Math.round(v*10)/10;
function oracle(n,from,to,mode){
 const ids=n.shafts.map(s=>s.id),weights=new Map(ids.map(x=>[x,Infinity]));
 weights.set(from,0);
 for(let i=0;i<ids.length-1;i++){
  let improved=false;
  for(const link of n.links){
   const a=n.shafts.find(s=>s.id===link.from),b=n.shafts.find(s=>s.id===link.to);
   const weight=mode==='hops'?1:Math.hypot(a.x-b.x,a.z-b.z);
   if(weights.get(a.id)+weight<weights.get(b.id)-1e-7){weights.set(b.id,weights.get(a.id)+weight);improved=true;}
   if(weights.get(b.id)+weight<weights.get(a.id)-1e-7){weights.set(a.id,weights.get(b.id)+weight);improved=true;}
  }
  if(!improved)break;
 }
 return weights.get(to);
}
function components(n){
 const known=new Map(n.shafts.map(s=>[s.id,s.id]));
 function root(v){while(known.get(v)!==v)v=known.get(v);return v;}
 for(const e of n.links){const a=root(e.from),b=root(e.to);known.set(a,b);}
 return new Set(n.shafts.map(s=>root(s.id))).size;
}
let reachable=0,unreachable=0,alternate=0,zero=0,totalChecks=0;
for(let iteration=0;iteration<320;iteration++){
 const count=2+rand(29),shafts=Array.from({length:count},(_,i)=>({
  id:'S'+String(i).padStart(2,'0'),
  name:'Fake Schacht '+i,x:rand(2000001)-1000000,z:rand(2000001)-1000000
 }));
 const links=[],pairs=new Set(),wanted=rand(Math.min(150,count*(count-1)/2)+1);
 for(let j=0;j<wanted*4&&links.length<wanted;j++){
  const i=rand(count),k=rand(count);
  if(i===k)continue;
  const pair=[i,k].sort((a,b)=>a-b).join(':');
  if(pairs.has(pair))continue;pairs.add(pair);
  links.push({id:'L'+links.length,from:shafts[i].id,to:shafts[k].id});
 }
 const net={shafts,links},source=JSON.stringify(net);
 const topo=planner.topology(net);
 assert.equal(topo.shaftCount,count);totalChecks++;
 assert.equal(topo.linkCount,links.length);totalChecks++;
 assert.equal(topo.componentCount,components(net));totalChecks++;
 assert.equal(topo.hasCycles,links.length>count-topo.componentCount);totalChecks++;
 for(const mode of ['distance','hops']){
  const start=shafts[rand(count)].id,end=shafts[rand(count)].id;
  const optimal=oracle(net,start,end,mode),actual=planner.find(net,start,end,{mode});
  if(!Number.isFinite(optimal)){
   assert.equal(actual.status,'unreachable');unreachable++;totalChecks++;
   assert.deepEqual(actual.linkIds,[]);totalChecks++;
   assert.equal(actual.distanceMm,null);totalChecks++;
  }else{
   reachable++;
   assert.equal(actual.status,'reachable');totalChecks++;
   assert.equal(actual.shaftIds[0],start);totalChecks++;
   assert.equal(actual.shaftIds.at(-1),end);totalChecks++;
   assert.equal(actual.linkIds.length,actual.shaftIds.length-1);totalChecks++;
   assert.equal(new Set(actual.shaftIds).size,actual.shaftIds.length);totalChecks++;
   let distance=0;
   for(let i=0;i<actual.linkIds.length;i++){
    const l=links.find(x=>x.id===actual.linkIds[i]);
    assert.ok(l);totalChecks++;
    const a=actual.shaftIds[i],b=actual.shaftIds[i+1];
    assert.ok(l.from===a&&l.to===b||l.from===b&&l.to===a);totalChecks++;
    const one=shafts.find(s=>s.id===a),two=shafts.find(s=>s.id===b);
    distance+=Math.hypot(one.x-two.x,one.z-two.z);
   }
   assert.ok(Math.abs(actual.distanceMm-round(distance))<=.11);totalChecks++;
   if(mode==='distance')assert.ok(Math.abs(actual.distanceMm-optimal)<=.11);
   else assert.equal(actual.hops,optimal);
   totalChecks++;
   if(!actual.linkIds.length)zero++;
   if(actual.linkIds.length&&iteration%4===0){
    const alternates=planner.alternatives(net,start,end,{mode,max:3});
    assert.deepEqual(alternates.primary,actual);totalChecks++;
    for(const item of alternates.alternatives){
     alternate++;assert.equal(item.status,'reachable');totalChecks++;
     assert.equal(item.shaftIds[0],start);totalChecks++;
     assert.equal(item.shaftIds.at(-1),end);totalChecks++;
     assert.notDeepEqual(item.linkIds,actual.linkIds);totalChecks++;
     assert.ok(item.distanceMm+0.1>=0);totalChecks++;
    }
   }
  }
  assert.equal(actual.verified,false);totalChecks++;
 }
 assert.equal(JSON.stringify(net),source,'Never mutate source graph at iteration '+iteration);totalChecks++;
 // Distinct input orders must produce exactly equal distance/hop count.
 if(iteration%9===0){
  const reversed={shafts:[...shafts].reverse(),links:[...links].reverse()};
  const a=planner.find(net,shafts[0].id,shafts.at(-1).id);
  const b=planner.find(reversed,shafts[0].id,shafts.at(-1).id);
  assert.equal(a.status,b.status);totalChecks++;
  assert.equal(a.distanceMm,b.distanceMm);totalChecks++;
  assert.equal(a.hops,b.hops);totalChecks++;
 }
}
assert.ok(reachable>100);
assert.ok(unreachable>20);
assert.ok(alternate>0);
assert.ok(zero>=0);
assert.ok(totalChecks>2800);
console.log('OK: 320 deterministic synthetic network graphs, '+totalChecks+
 ' independent checks; reachable='+reachable+', unreachable='+unreachable+
 ', alternatives='+alternate+'; no fabricated cable continuity');
