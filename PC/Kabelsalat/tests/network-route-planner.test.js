'use strict';
/* KAB-0352/0353/0355/0356/0365/0375 giant sprint:
 * actual, independent graph algorithm and integration regressions.
 * No assertion makes an unapproved technical construction claim.
 */
const assert=require('node:assert/strict');
const fs=require('node:fs'),path=require('node:path');
const planner=require('../network-route-planner.js');
const network=require('../network.js');
const app=fs.readFileSync(path.join(__dirname,'../app.js'),'utf8');
const html=fs.readFileSync(path.join(__dirname,'../index.html'),'utf8');
const pkg=require('../package.json');
const clone=o=>JSON.parse(JSON.stringify(o));
const shaft=(id,x,z)=>({id,name:'Schacht '+id,x,z,
 snapshot:{dims:{w:1000,d:1000,h:1500},walls:[{id:'SW1',holes:[{id:'LR1',u:.5,v:.5,dn:50}]}],
 cables:[{id:'C'+id,name:'Kabel '+id,group:'OLA',a:{wall:'SW1',hole:'LR1'},b:null}]}});
const link=(id,from,to)=>({id,from,to,note:'Nur schematisch'});
const base=()=>({shafts:[shaft('A',0,0),shaft('B',1000,0),shaft('C',2000,0),
 shaft('D',1000,1500)],links:[link('ab','A','B'),link('bc','B','C'),link('ad','A','D'),link('dc','D','C')],
 routes:[{id:'R1',name:'Weg von A nach C',group:'OLA',steps:[{shaftId:'A',cableId:'CA'},
 {shaftId:'B',cableId:'CB'},{shaftId:'C',cableId:'CC'}]}]});
const net=base(),snap=JSON.stringify(net);
const graph=planner.normalize(net);
assert.equal(graph.map.size,4);
assert.equal(graph.links.length,4);
assert.deepEqual(graph.adj.get('B').map(x=>x.to),['A','C']);
const topology=planner.topology(net);
assert.equal(topology.shaftCount,4);
assert.equal(topology.linkCount,4);
assert.equal(topology.componentCount,1);
assert.deepEqual(topology.components,[['A','B','C','D']]);
assert.deepEqual(topology.isolated,[]);
assert.equal(topology.hasCycles,true);
assert.deepEqual(topology.degrees.map(x=>x.degree),[2,2,2,2]);
const route=planner.find(net,'A','C');
assert.equal(route.status,'reachable');
assert.equal(route.from,'A');assert.equal(route.to,'C');
assert.equal(route.mode,'distance');
assert.deepEqual(route.shaftIds,['A','B','C']);
assert.deepEqual(route.linkIds,['ab','bc']);
assert.equal(route.distanceMm,2000);
assert.equal(route.hops,2);
assert.equal(route.verified,false);
assert.match(route.warnings.join(' '),/keine Trassen-\/Kabellängen/);
assert.equal(route.source,'schematic-net-positions');
const alt=planner.alternatives(net,'A','C');
assert.deepEqual(alt.primary,route);
assert.equal(alt.alternatives.length,1);
assert.deepEqual(alt.alternatives[0].shaftIds,['A','D','C']);
assert.equal(alt.alternatives[0].distanceMm,3605.6);
assert.equal(alt.alternatives[0].verified,false);
assert.equal(planner.alternatives(net,'A','C',{max:0}).alternatives.length,0);
assert.deepEqual(planner.find(net,'A','C',{excludeLinks:['bc']}).linkIds,['ad','dc']);
assert.equal(planner.find(net,'A','C',{excludeLinks:['ab','ad']}).status,'unreachable');
const reverse=planner.find(net,'C','A');
assert.deepEqual(reverse.shaftIds,['C','B','A']);
const same=planner.find(net,'D','D');
assert.deepEqual(same.shaftIds,['D']);assert.deepEqual(same.linkIds,[]);
assert.equal(same.distanceMm,0);
assert.equal(same.hops,0);
const hops=planner.find(net,'A','C',{mode:'hops'});
assert.deepEqual(hops.shaftIds,['A','B','C']);
assert.equal(hops.hops,2);
assert.equal(hops.distanceMm,2000);
assert.throws(()=>planner.find(net,'A','C',{mode:'unsafe'}),/Suchmodus/);
assert.throws(()=>planner.find(net,'UNKNOWN','C'),/Start\/Ziel/);
assert.throws(()=>planner.find(net,'A','C',{excludeLinks:'ab'}),/Link-Sperrliste/);
assert.throws(()=>planner.alternatives(net,'A','C',{max:11}),/Alternativenzahl/);
assert.throws(()=>planner.alternatives(net,'A','C',{max:-1}),/Alternativenzahl/);
// Multiple connected components, no phantom transitions.
const disconnected=base();
disconnected.shafts.push(shaft('E',6000,0),shaft('F',9000,0),shaft('G',11000,0));
disconnected.links.push(link('ef','E','F'));
const zones=planner.topology(disconnected);
assert.equal(zones.componentCount,3);
assert.deepEqual(zones.components,[['A','B','C','D'],['E','F'],['G']]);
assert.deepEqual(zones.isolated,['G']);
assert.equal(planner.find(disconnected,'A','F').status,'unreachable');
assert.equal(planner.alternatives(disconnected,'A','F').alternatives.length,0);
assert.match(planner.find(disconnected,'A','F').warnings.join(' '),/Keine Verbindung/);
// Even ties must not depend on network array order.
const ties={shafts:[shaft('S',0,0),shaft('B',1000,1000),shaft('A',1000,-1000),shaft('T',2000,0)],
 links:[link('SB','S','B'),link('BT','B','T'),link('SA','S','A'),link('AT','A','T')]};
assert.deepEqual(planner.find(ties,'S','T').shaftIds,['S','A','T']);
const shuffle=clone(ties);shuffle.shafts.reverse();shuffle.links.reverse();
assert.deepEqual(planner.find(shuffle,'S','T').shaftIds,['S','A','T']);
assert.equal(planner.topology(ties).hasCycles,true);
const zero={shafts:[shaft('X',1,1),shaft('Y',1,1)],links:[link('xy','X','Y')]};
assert.equal(planner.find(zero,'X','Y').distanceMm,0);
assert.ok(planner.topology(zero).warnings.some(w=>w.includes('0-mm')));
// Input hygiene.
const invalidInput=[null,{}, {shafts:[],links:[link('ab','A','B')]},
 {...base(),shafts:[shaft('A',0,0),shaft('A',1,1)]},
 {...base(),shafts:[shaft('A',1.5,2)]},
 {...base(),shafts:[shaft('A',Infinity,1)]},
 {...base(),shafts:[shaft('A',Number.MAX_SAFE_INTEGER,1)]},
 {...base(),links:[link('ab','A','A')]},
 {...base(),links:[link('ab','A','NOPE')]},
 {...base(),links:[link('ab','A','B'),link('ab','B','C')]},
 {...base(),links:[link('one','A','B'),link('two','B','A')]}
];
for(const bad of invalidInput)assert.throws(()=>planner.normalize(bad));
assert.throws(()=>planner.normalize({shafts:Array.from({length:31},(_,i)=>shaft('N'+i,i,0)),links:[]}),/30 Schächte/);
assert.throws(()=>planner.normalize({shafts:[],links:Array.from({length:151},()=>link('x','A','B'))}),/150 Links/);
const simple={shafts:[shaft('X',0,0)],links:[]};
assert.deepEqual(planner.topology(simple).components,[['X']]);
assert.equal(planner.topology(simple).hasCycles,false);
// The maximum supported graph is handled without mutation or path explosion.
const big={shafts:Array.from({length:30},(_,i)=>shaft('N'+String(i).padStart(2,'0'),i*500,0)),
 links:Array.from({length:29},(_,i)=>link('L'+i,'N'+String(i).padStart(2,'0'),'N'+String(i+1).padStart(2,'0')))};
const hugePath=planner.find(big,'N00','N29');
assert.equal(hugePath.status,'reachable');
assert.equal(hugePath.shaftIds.length,30);
assert.equal(hugePath.linkIds.length,29);
assert.equal(hugePath.distanceMm,14500);
assert.equal(planner.alternatives(big,'N00','N29').alternatives.length,0);
assert.equal(planner.topology(big).hasCycles,false);
// Compare explicitly registered existing cable segments, never reconstruct physical route.
const compared=planner.compareRoute(net,net.routes[0]);
assert.equal(compared.routeId,'R1');
assert.equal(compared.steps,3);
assert.equal(compared.status,'schematic');
assert.equal(compared.modelDistanceMm,2000);
assert.deepEqual(compared.linkSteps.map(x=>x.linkId),['ab','bc']);
assert.deepEqual(compared.warnings,[]);
assert.equal(compared.physicalClearanceVerified,false);
const missingCable=clone(net.routes[0]);
missingCable.steps[1].cableId='MISSING';
assert.ok(planner.compareRoute(net,missingCable).warnings.some(x=>x.includes('Kabel')));
const gap=base();
gap.links=gap.links.filter(l=>l.id!=='bc');
assert.ok(planner.compareRoute(gap,gap.routes[0]).warnings.some(x=>x.includes('Kein direkter Netzlink')));
assert.equal(planner.compareRoute(gap,gap.routes[0]).status,'incomplete');
assert.throws(()=>planner.compareRoute(net,{}),/ohne Segmente/);
assert.throws(()=>planner.compareRoute(net,{steps:Array.from({length:61},()=>({shaftId:'A',cableId:'CA'}))}),/Zu viele/);
// CSV deliberately uses safe formulas, UTF-8 BOM and CRLF.
const exported=planner.csv(route);
assert.ok(exported.startsWith('\uFEFF"Index";"Schacht-ID"'));
assert.ok(exported.includes('"A";"ab"'));
assert.ok(exported.includes('"C";""'));
assert.match(exported,/Schematische Netzlinie/);
assert.ok(!exported.includes('=HYPERLINK('));
const dangerous={...route,shaftIds:['=IMPORTXML("x")'],linkIds:[]};
assert.ok(planner.csv(dangerous).includes("'=IMPORTXML"));
assert.throws(()=>planner.csv({shaftIds:Array(32).fill('A'),linkIds:[]}),/Ungültige/);
// The planner itself is strictly immutable for every operation above.
assert.equal(JSON.stringify(net),snap);
assert.equal(JSON.stringify(base()),snap);
// End-to-end module hook and packaging smoke.
const version=pkg.version;
assert.ok(html.includes('id="netPlannerFind"')&&html.includes('id="netPlannerShow"'));
assert.ok(html.includes('src="network-route-planner.js?v='+version+'"'));
assert.ok(pkg.build.files.includes('network-route-planner.js'));
assert.ok(app.includes('routePlanner.alternatives(project.network,from,to'));
assert.ok(app.includes('const candidate=routeView();'));
assert.ok(app.includes('routePlanner.csv(result)'));
assert.ok(app.includes("change(fn,label='Planung geändert')"));
console.log('OK: Netzwegfinder topology, deterministic shortest/alternative paths, source limits, disconnections, 30 shafts, no model mutation');
