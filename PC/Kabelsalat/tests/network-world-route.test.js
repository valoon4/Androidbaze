'use strict';
/* KAB-0361..0378 / 0375: global model-XYZ geometry with explicit gap semantics.
 * Synthetic coordinates; NEVER promotes schematic geometry to field measurements. */
const assert=require('node:assert/strict');
const world=require('../network-world-route.js');
const geometry=require('../pdf-geometry.js');
const apps=require('../network-route-planner.js');
const fs=require('node:fs'),path=require('node:path');
const app=fs.readFileSync(path.join(__dirname,'../app.js'),'utf8');
const html=fs.readFileSync(path.join(__dirname,'../index.html'),'utf8');
const pkg=require('../package.json');
const copy=x=>JSON.parse(JSON.stringify(x));
const dims={w:2400,d:1900,h:2100};
const hole=(id,u,v)=>({id,u,v,dn:50});
const snapshot=(sourceInfo=null)=>({
 name:'FIKTIVE QUELLENPROBE',dims,
 walls:[
  {id:'SW1',holes:[hole('A',.25,.3),hole('B',.75,.6)]},
  {id:'SW2',holes:[hole('A',.25,.3),hole('B',.75,.6)]},
  {id:'SW3',holes:[hole('A',.25,.3),hole('B',.75,.6)]},
  {id:'SW4',holes:[hole('A',.25,.3),hole('B',.75,.6)]}
 ],sourceInfo,
 cables:[{id:'C',group:'OLA',a:{wall:'SW1',hole:'A'},b:{wall:'SW3',hole:'B'},
 route:[[0,1200,0],[250,1500,500]]}]
});
const shaft=(id,x,z,sourceInfo=null)=>({id,x,z,name:'Schacht '+id,snapshot:snapshot(sourceInfo)});
const net={shafts:[shaft('S1',0,0),shaft('S2',10000,3000)],
 links:[{id:'L1',from:'S1',to:'S2'}],
 routes:[{id:'R1',group:'OLA',steps:[{shaftId:'S1',cableId:'C'},{shaftId:'S2',cableId:'C'}]}]};
const before=JSON.stringify(net);
const report=world.assemble(net,net.routes[0]);
assert.equal(report.routeId,'R1');
assert.equal(report.segmentCount,2);
assert.equal(report.status,'schematic-unverified');
assert.equal(report.verified,false);
assert.equal(report.physicalContinuityVerified,false);
assert.equal(report.actualCableLengthMm,null);
assert.equal(report.gaps.length,1);
assert.equal(report.gaps[0].linkId,'L1');
assert.equal(report.gaps[0].physicalConnectionVerified,false);
assert.equal(report.gaps[0].unknownDuct,true);
assert.equal(report.gaps[0].status,'unverified-cross-shaft-gap');
assert.ok(report.gaps[0].airGapMm>0);
assert.equal(report.segments[0].points.length,4);
assert.equal(report.segments[1].points.length,4);
assert.deepEqual(report.segments[0].localPoints[1],[0,1200,0]);
assert.deepEqual(report.segments[0].points[1],[0,1200,0]);
assert.deepEqual(report.segments[1].points[1],[10000,1200,3000]);
assert.deepEqual(report.segments[1].points[2],[10250,1500,3500]);
assert.ok(report.segments.every(s=>s.status==='schematic'&&!s.verified));
assert.ok(report.segments.every(s=>s.sourceVersion===null));
assert.equal(report.segments[0].modelPolylineMm,report.segments[1].modelPolylineMm);
assert.equal(report.localModelPolylineTotalMm,2*report.segments[0].modelPolylineMm);
assert.equal(JSON.stringify(net),before,'Must not mutate shaft snapshots');
assert.deepEqual(world.holePosition('SW1',hole('X',.25,.3),dims,null),
 [-1176,630,-475]);
assert.deepEqual(world.holePosition('SW2',hole('X',.25,.3),dims,null),
 [-600,630,-926]);
assert.deepEqual(world.holePosition('SW3',hole('X',.25,.3),dims,null),
 [1176,630,-475]);
assert.deepEqual(world.holePosition('SW4',hole('X',.25,.3),dims,null),
 [-600,630,926]);
for(const wall of ['SW1','SW2','SW3','SW4']){
 const h=hole('T',.25,.3),worldCoords=world.holePosition(wall,h,dims,{sourceVersion:'PDF V2 13_U_028'});
 const fromPdf=geometry.hole(wall,h,{w:2.4,d:1.9,h:2.1}).map(x=>Math.round(x*1000));
 assert.deepEqual(worldCoords,fromPdf,'PDF V2 axes/radii differ from legacy '+wall);
}
assert.deepEqual(world.holePosition('SW1',hole('T',.25,.3),dims,{sourceVersion:'PDF V2'}),
 [-600,630,-926]);
assert.deepEqual(world.holePosition('SW3',hole('T',.25,.3),dims,{sourceVersion:'PDF V2'}),
 [600,630,926]);
const pdfnet=copy(net);pdfnet.shafts[0].snapshot.sourceInfo={sourceVersion:'PDF V2 13_U_028'};
const pdfreport=world.assemble(pdfnet,pdfnet.routes[0]);
assert.equal(pdfreport.segments[0].sourceVersion,'PDF V2 13_U_028');
assert.equal(pdfreport.segments[1].sourceVersion,null);
assert.notDeepEqual(pdfreport.segments[0].points[0],report.segments[0].points[0]);
assert.equal(pdfreport.physicalContinuityVerified,false);
const noLink=copy(net);noLink.links=[];
const without=world.assemble(noLink,noLink.routes[0]);
assert.equal(without.status,'incomplete');
assert.equal(without.gaps[0].linkId,null);
assert.equal(without.gaps[0].status,'missing-link');
assert.match(without.issues.join(' '),/Kein Netzlink/);
const missingHole=copy(net);
missingHole.shafts[1].snapshot.walls[2].holes=[];
const bad=world.assemble(missingHole,missingHole.routes[0]);
assert.equal(bad.status,'incomplete');
assert.equal(bad.segments[1].modelPolylineMm,null);
assert.equal(bad.localModelPolylineTotalMm,null);
assert.equal(bad.gaps[0].status,'insufficient-local-geometry');
assert.ok(bad.segments[1].issues.some(i=>i.includes('unbekanntes Leerrohr')));
const missingCable=copy(net);missingCable.shafts[1].snapshot.cables=[];
const broken=world.assemble(missingCable,missingCable.routes[0]);
assert.equal(broken.status,'incomplete');
assert.equal(broken.segments[1].status,'missing-cable');
assert.equal(broken.segments[1].points.length,0);
const unknown=copy(net);unknown.routes[0].steps[1].shaftId='S404';
const noShaft=world.assemble(unknown,unknown.routes[0]);
assert.equal(noShaft.segments[1].status,'missing-shaft');
const orphan=copy(net);orphan.shafts[1].snapshot.cables[0].a=null;
assert.match(world.assemble(orphan,orphan.routes[0]).issues.join(' '),/nicht zugeordnet/);
const malicious=copy(net);malicious.shafts[0].snapshot.cables[0].route=[[NaN,0,0]];
const invalidWaypoints=world.assemble(malicious,malicious.routes[0]);
assert.equal(invalidWaypoints.status,'incomplete');
assert.ok(invalidWaypoints.segments[0].issues.some(i=>i.includes('Wegpunkte ungültig')));
assert.throws(()=>world.holePosition('SW99',hole('X',.4,.3),dims,{}),/Schachtwand/);
assert.throws(()=>world.holePosition('SW1',hole('X',1.5,.3),dims,{}),/Lochkoordinaten/);
assert.throws(()=>world.assemble({shafts:Array(31).fill(net.shafts[0]),links:[]},net.routes[0]),/größer/);
assert.throws(()=>world.assemble(null,net.routes[0]),/0–60/);
assert.throws(()=>world.assemble(net,{steps:Array(61).fill(net.routes[0].steps[0])}),/0–60/);
const compact=world.assemble(net,{id:'EMPTY',steps:[]});
assert.equal(compact.status,'incomplete');
assert.equal(compact.localModelPolylineTotalMm,0);
const csv=world.csv(report);
assert.ok(csv.startsWith('\uFEFF"Art";"Segment"'));
assert.equal((csv.match(/"unbekannter Übergang"/g)||[]).length,1);
assert.ok(csv.includes('"10000";"1200";"3000"'));
assert.ok(csv.includes('nicht vermessen'));
assert.ok(csv.includes('nicht interpolieren'));
assert.ok(!csv.includes('physisch freigegeben'));
assert.throws(()=>world.csv({segments:Array(61).fill({points:[]}),gaps:[]}),/Routenbericht/);
const dangerous={segments:[{index:1,shaftId:'=HYPERLINK("evil")',cableId:'C',points:[[0,0,0]],status:'schematic'}],gaps:[]};
assert.ok(world.csv(dangerous).includes("'=HYPERLINK"));
assert.ok(pkg.build.files.includes('network-world-route.js'));
assert.ok(html.includes('src="network-world-route.js?v='+pkg.version+'"'));
for(const id of ['worldRouteAnalyze','worldRouteCsv','worldRouteStatus','worldRouteList','worldRouteHeightCanvas'])
 assert.ok(html.includes('id="'+id+'"'),'UI missing '+id);
assert.ok(app.includes('worldRoute.assemble(project.network,r)'));
assert.ok(app.includes('drawWorldRouteElevation()'));
assert.ok(app.includes('worldRoute.csv(worldRouteReport)'));
assert.equal(JSON.stringify(net),before,'All tasks remain read-only');
console.log('OK: global XYZ cable segment assembly, PDF/legacy axes, null gaps, mm provenance, CSV injection and non-mutating source');
