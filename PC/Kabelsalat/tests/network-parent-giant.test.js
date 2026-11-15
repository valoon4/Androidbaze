'use strict';
/* KAB-0343..0360 / 0361..0378: schema-safe five-shaft topology, status and global segments. */
const assert=require('node:assert/strict'),fs=require('node:fs'),path=require('node:path');
const network=require('../network.js'),planner=require('../network-route-planner.js');
const world=require('../network-world-route.js');
const copy=x=>JSON.parse(JSON.stringify(x));
const dims={w:2400,d:1900,h:2100};
const hole=(id,u,v)=>({id,u,v,dn:50});
const snapshot=i=>{
 const walls=[
  {id:'SW1',thickness:100,holes:[hole('A',.3,.4)]},
  {id:'SW3',thickness:100,holes:[hole('B',.7,.6)]}];
 if(i%3!==0)walls.push({id:'SW2',thickness:90,holes:[hole('C',.5,.5)]});
 if(i===3)walls.push({id:'SW4',thickness:90,holes:[hole('D',.5,.5)]});
 return {name:'FIKTIVER Schacht '+i,dims:copy(dims),walls,
  cables:[{id:'C'+i,name:'Segment '+i,group:i===2?'MS':'OLA',
   a:{wall:'SW1',hole:'A'},b:{wall:'SW3',hole:'B'},route:[[0,1000,0]]}]};
};
const shaft=i=>({id:'S'+i,name:'Schacht '+i+' mit extrem langem Testnamen',
 x:i*10000,z:i%2*3000,snapshot:snapshot(i)});
const link=(a,b,id)=>({id,from:'S'+a,to:'S'+b,note:'schematisch',physicalStatus:'unverified'});
const original={version:1,shafts:[1,2,3,4,5].map(shaft),
 links:[link(1,2,'L12'),link(2,3,'L23'),link(3,4,'L34'),link(4,1,'L41'),link(4,5,'L45')],
 routes:[{id:'R1',name:'Drei Segmente',group:'OLA',
  steps:[1,2,3].map(i=>({shaftId:'S'+i,cableId:'C'+i}))}]};
const snap=JSON.stringify(original),net=network.normalizeNetwork(original,snapshot(1));
assert.equal(net.shafts.length,5);
assert.deepEqual(net.shafts.map(x=>x.snapshot.walls.length),[3,3,4,3,3]);
assert.equal(net.links.length,5);
assert.equal(net.links[0].id,'L12');
assert.equal(net.links[0].physicalStatus,'unverified');
assert.equal(JSON.stringify(original),snap,'Normalization must not mutate original shafts');
const self=network.normalizeNetwork({...original,links:[{id:'self',from:'S1',to:'S1'}]},snapshot(1));
assert.equal(self.links.length,0,'Self-link must not be accepted');
const labels=network.placeLabels([{id:'S1',name:'Schachtname sehr lang 123456789',x:100,y:120},
 {id:'S2',name:'Schachtname sehr lang 123456789',x:100,y:120}],500,350);
assert.ok(labels[0].label.endsWith('…'));
assert.ok(labels[0].x!==labels[1].x||labels[0].y!==labels[1].y,'Adjacent labels must separate');
const graph=planner.topology(net);
assert.equal(graph.components.length,1);
assert.equal(graph.hasCycles,true,'Four-shaft ring must be detected');
const split=copy(net);split.links=split.links.filter(l=>l.id!=='L45');
assert.equal(planner.topology(split).components.length,2);
const moved=network.applyPositions(net,[{id:'S2',x:999,z:777}]);
assert.equal(moved.shafts[1].x,999);
assert.equal(moved.shafts[1].z,777);
assert.deepEqual(moved.shafts[1].snapshot,net.shafts[1].snapshot);
assert.equal(net.shafts[1].x,20000,'Original map must stay untouched');
assert.deepEqual(network.issues(net,net.routes[0]),[]);
const details=network.routeDetails(net,net.routes[0]);
assert.equal(details.length,3);
assert.deepEqual(details.map(s=>s.cableId),['C1','C2','C3']);
assert.ok(details.every(s=>s.from==='SW1 / A'&&s.to==='SW3 / B'));
const continuity=planner.continuity(net,net.routes[0]);
assert.equal(continuity.physicalContinuityConfirmed,false);
assert.ok(continuity.warnings.some(x=>/Systemkonflikt/.test(x)),'Foreign MS segment must be flagged');
const report=world.assemble(net,net.routes[0]);
assert.equal(report.segmentCount,3);
assert.equal(report.gaps.length,2);
assert.equal(report.status,'schematic-unverified');
assert.equal(report.verified,false);
assert.equal(report.physicalContinuityVerified,false);
assert.equal(report.actualCableLengthMm,null);
assert.ok(report.segments.every(s=>s.points.length>=2&&s.status==='schematic'));
assert.ok(report.segments.every(s=>s.sourceVersion===null));
assert.ok(report.gaps.every(g=>g.physicalConnectionVerified===false&&g.unknownDuct));
assert.equal(report.localModelPolylineTotalMm,
 Math.round(report.segments.reduce((n,s)=>n+s.modelPolylineMm,0)*1000)/1000);
const brokenLink=copy(net);brokenLink.links=brokenLink.links.filter(l=>l.id!=='L23');
const incomplete=world.assemble(brokenLink,brokenLink.routes[0]);
assert.equal(incomplete.status,'incomplete');
assert.equal(incomplete.gaps[1].status,'missing-link');
assert.deepEqual(brokenLink.routes[0].steps,net.routes[0].steps,'No silent rerouting');
const missingCable=copy(net);missingCable.shafts[1].snapshot.cables=[];
const orphan=world.assemble(missingCable,missingCable.routes[0]);
assert.equal(orphan.status,'incomplete');
assert.equal(orphan.segments[1].status,'missing-cable');
const missingHole=copy(net);missingHole.shafts[2].snapshot.walls[0].holes=[];
assert.equal(world.assemble(missingHole,missingHole.routes[0]).segments[2].status,'missing-endpoint');
const missingBridge=copy(net);missingBridge.shafts[1].snapshot.cables[0].b=null;
assert.equal(world.assemble(missingBridge,missingBridge.routes[0]).status,'incomplete');
const circular=copy(net);circular.routes=[{id:'RING',steps:[
 {shaftId:'S1',cableId:'C1'},{shaftId:'S2',cableId:'C2'},
 {shaftId:'S3',cableId:'C3'},{shaftId:'S4',cableId:'C4'},
 {shaftId:'S1',cableId:'C1'}]}];
assert.equal(world.assemble(circular,circular.routes[0]).segmentCount,5);
const many={shafts:Array.from({length:30},(_,i)=>({
 id:'N'+i,x:i*100,z:0,name:'Netz '+i,snapshot:snapshot(i)})),
 links:Array.from({length:29},(_,i)=>({id:'LN'+i,from:'N'+i,to:'N'+(i+1)})),
 routes:[]};
assert.equal(planner.find(many,'N0','N29').shaftIds.length,30);
const html=fs.readFileSync(path.join(__dirname,'../index.html'),'utf8');
const app=fs.readFileSync(path.join(__dirname,'../app.js'),'utf8');
for(const id of ['netNewShaft','netShaftX','netShaftZ','netAddLink','netDeleteLink',
 'netLinkStatus','netUpdateLinkStatus','networkCanvas','worldRouteAnalyze'])
 assert.ok(html.includes('id="'+id+'"'),'Network UI missing '+id);
assert.ok(app.includes("mapDrag={mode:'shaft'")&&app.includes("mapDrag.preview="));
assert.ok(app.includes("networkModel.placeLabels("));
assert.ok(app.includes("affected.map(r=>r.name+' ['+r.id+']')"));
assert.ok(app.includes("physicalStatus:'unverified'"));
assert.equal(JSON.stringify(original),snap,'All network operations must remain nonmutating');
console.log('NETWORK PARENT GIANT OK: five unequal shafts, cycle, 30 nodes, missing links/segments, source-safe XYZ and physical nonapproval');