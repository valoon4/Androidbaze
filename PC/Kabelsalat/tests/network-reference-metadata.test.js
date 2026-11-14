'use strict';
/* KAB-0326 / KAB-0328: explicit model elevation + optional CRS metadata,
 * no X/Z to Y inference, no manufactured EPSG, no physical cable continuity. */
const assert=require('node:assert/strict');
const model=require('../network.js'),world=require('../network-world-route.js');
const shaft=(id,x,z,y)=>({id,x,z,...(y!==undefined?{y}:{}),name:id,snapshot:{
 dims:{w:2400,d:1900,h:2100},walls:[
 {id:'SW1',holes:[{id:'H1',u:.4,v:.3}]},
 {id:'SW3',holes:[{id:'H2',u:.6,v:.7}]}
 ],cables:[{id:'C'+id,group:'OLA',a:{wall:'SW1',hole:'H1'},
 b:{wall:'SW3',hole:'H2'},route:[[200,900,100]]}]}
});
const project={name:'Unit',dims:{w:2400,d:1900,h:2100},walls:[],cables:[]};
const net={version:1,shafts:[shaft('S1',0,0,1200),shaft('S2',10000,3000,-500)],
 coordinateReference:'EXPLICIT-MODEL-GRID-NOT-A-SURVEY',
 links:[{id:'L1',from:'S1',to:'S2'}],
 routes:[{id:'R1',name:'Fiktiv',group:'OLA',steps:[
 {shaftId:'S1',cableId:'CS1'},{shaftId:'S2',cableId:'CS2'}]}]};
const before=JSON.stringify(net);
const norm=model.normalizeNetwork(net,project);
assert.equal(norm.shafts[0].y,1200);
assert.equal(norm.shafts[1].y,-500);
assert.equal(norm.coordinateReference,'EXPLICIT-MODEL-GRID-NOT-A-SURVEY');
assert.deepEqual(model.issues(norm,norm.routes[0]),[]);
const report=world.assemble(norm,norm.routes[0]);
assert.equal(report.status,'schematic-unverified');
assert.equal(report.coordinateReference,'EXPLICIT-MODEL-GRID-NOT-A-SURVEY');
assert.equal(report.coordinateReferenceVerified,false);
assert.equal(report.physicalContinuityVerified,false);
assert.equal(report.actualCableLengthMm,null);
assert.equal(report.segments[0].verticalShaftOffsetMm,1200);
assert.equal(report.segments[1].verticalShaftOffsetMm,-500);
assert.deepEqual(report.segments[0].points[1],[200,2100,100]);
assert.deepEqual(report.segments[1].points[1],[10200,400,3100]);
assert.ok(report.segments.every(s=>s.globalElevationReferenceKnown===false));
assert.equal(report.gaps.length,1);
assert.equal(report.gaps[0].physicalConnectionVerified,false);
assert.equal(JSON.stringify(net),before);
const json=JSON.parse(JSON.stringify(norm));
assert.deepEqual(json,norm);
assert.equal(json.shafts[1].id,'S2');
assert.equal(json.coordinateReference,norm.coordinateReference);
const old=JSON.parse(before);delete old.shafts[0].y;delete old.shafts[1].y;delete old.coordinateReference;
const legacy=model.normalizeNetwork(old,project);
assert.ok(!Object.hasOwn(legacy.shafts[0],'y'));
assert.ok(!Object.hasOwn(legacy,'coordinateReference'));
const noHeight=world.assemble(legacy,legacy.routes[0]);
assert.equal(noHeight.segments[0].verticalShaftOffsetMm,null);
assert.deepEqual(noHeight.segments[0].points[1],[200,900,100]);
assert.equal(noHeight.coordinateReference,null);
const corrupt=JSON.parse(before);corrupt.shafts[0].y='1200';
corrupt.shafts[1].y=Infinity;corrupt.coordinateReference='     ';
const safe=model.normalizeNetwork(corrupt,project);
assert.equal(safe.shafts[0].y,null);assert.equal(safe.shafts[1].y,null);
assert.equal(safe.coordinateReference,null);
assert.equal(world.assemble(safe,safe.routes[0]).segments[0].verticalShaftOffsetMm,null);
const dup=JSON.parse(before);dup.shafts.push(shaft('S2',500,500,0));
const unique=model.normalizeNetwork(dup,project);
assert.equal(unique.shafts.length,2);
assert.equal(unique.shafts[1].y,-500);
const broken=JSON.parse(before);broken.links=[];
assert.match(model.issues(model.normalizeNetwork(broken,project),norm.routes[0]).join(' '),/Keine Netzverbindung/);
assert.equal(world.assemble(broken,broken.routes[0]).gaps[0].status,'missing-link');
const unknown=JSON.parse(before);unknown.routes[0].steps[1].shaftId='MISSING';
assert.equal(world.assemble(unknown,unknown.routes[0]).segments[1].status,'missing-shaft');
const third=JSON.parse(before);third.shafts.push(shaft('S3',25000,1000,50));
third.links.push({id:'L2',from:'S2',to:'S3'});
third.routes[0].steps.push({shaftId:'S3',cableId:'CS3'});
const multi=world.assemble(third,third.routes[0]);
assert.equal(multi.segments.length,3);
assert.equal(multi.gaps.length,2);
assert.equal(multi.status,'schematic-unverified');
assert.ok(multi.gaps.every(g=>g.physicalConnectionVerified===false));
assert.equal(JSON.stringify(net),before);
console.log('NETWORK OPTIONAL Y/CRS OK: legacy no-height, two shafts with explicit offsets, 3-shaft gaps, no fictive EPSG or physical route');
