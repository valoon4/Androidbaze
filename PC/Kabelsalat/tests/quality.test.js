'use strict';
const assert=require('node:assert/strict');
const q=require('../quality.js');
const shaft={id:'S1',name:'Test',x:0,z:0,snapshot:{
 dims:{w:2400,d:1900,h:2100},walls:[{id:'SW1',holes:[
 {id:'h1',u:.5,v:.5,dn:100},{id:'h2',u:.5+20/1900,v:.5,dn:100}]}],
 cables:[{id:'c1',name:'Kabel Alpha',group:'OLA',diameter:125,
 a:{wall:'SW1',hole:'h1'},b:{wall:'SW1',hole:'h2'},route:[[0,950,0]]}]
}};
const net={shafts:[shaft],links:[],routes:[]};
const report=q.evaluate(net,{minPipeGapMm:30,minEdgeMm:100,minBendRadiusMm:200});
assert.ok(report.issues.some(x=>x.code==='PIPE_OVERLAP'));
assert.ok(report.issues.some(x=>x.code==='CABLE_TOO_WIDE'));
const incomplete=JSON.parse(JSON.stringify(net));incomplete.shafts[0].snapshot.cables[0].b=null;
assert.ok(q.evaluate(incomplete,{minBendRadiusMm:500}).issues.some(x=>x.code==='MISSING_END'),
 'Missing cable endpoint must not crash geometry checks');
assert.ok(report.issues.every(x=>x.severity!=='success'));
assert.ok(q.searchNetwork(net,'alpha').some(x=>x.kind==='cable'&&x.cableId==='c1'));
assert.ok(q.searchNetwork(net,'h1').some(x=>x.kind==='hole'));
const reviews=q.normalizeReviews([{shaftId:'S1',entityType:'cable',entityId:'c1',
 status:'Vor Ort abgeglichen',source:'Begehung 2026-11-05'}],net.shafts);
assert.equal(reviews.length,1);assert.equal(reviews[0].source,'Begehung 2026-11-05');
assert.equal(q.normalizeReviews([{shaftId:'S1',entityType:'cable',entityId:'BAD'}],net.shafts).length,0);
assert.equal(q.normalizeSettings({minPipeGapMm:'',minEdgeMm:-12}).minPipeGapMm,null);
assert.equal(q.normalizeSettings({minEdgeMm:-12}).minEdgeMm,null);
assert.ok(Number.isFinite(q.circumradius([0,0,0],[1,1,0],[2,0,0])));
const measured=JSON.parse(JSON.stringify(net));
const bore=measured.shafts[0].snapshot.walls[0].holes;
bore[0].dn=60;bore[1].dn=60;
bore[0].dimensionSpec={openingMm:116,innerMm:45};
bore[1].dimensionSpec={openingMm:116,innerMm:45};
assert.ok(q.evaluate(measured,{}).issues.some(x=>x.code==='PIPE_OVERLAP'),
 'Opening collision uses bore Ø 116 and not nominal DN 60');
assert.ok(q.evaluate(measured,{}).issues.some(x=>x.code==='CABLE_TOO_WIDE'),
 'Cable clearance must compare against documented inside Ø when present');
const sourcePdf={shafts:[{id:'13_U_028',snapshot:{
 dims:{w:2750,d:2350,h:1850},sourceInfo:{sourceVersion:'PDF V2 (Konzept)'},
 walls:[{id:'SW1',holes:[{id:'near',u:.98,v:.5,dn:110}]}],cables:[]
}}]};
assert.equal(q.evaluate(sourcePdf,{}).issues.some(x=>x.code==='OUTSIDE_WALL'),false,
 'PDF SW1 source span is 2750 mm: mirror/width mismatch caused false edge warnings');

// KAB-0433–0450: complete geometry QA regression on distinct synthetic walls.
const qaSource={shafts:[{id:'QA1',snapshot:{name:'FIKTIVER QA SCHACHT',
 dims:{w:2400,d:2000,h:2000},
 walls:[{id:'SW1',thickness:80,holes:[
  {id:'A',u:.3,v:.5,dn:50},{id:'B',u:.355,v:.5,dn:50}]},
  {id:'SW2',thickness:80,holes:[{id:'C',u:.5,v:.5,dn:50}]}],
 cables:[]}}],links:[],routes:[]};
const qaBefore=JSON.stringify(qaSource);
const wallAudit=q.auditWallOpenings(qaSource.shafts[0].snapshot);
assert.equal(wallAudit.walls.length,2);
assert.ok(wallAudit.walls[0].minGapMm>0);
assert.equal(wallAudit.overlaps.length,0);
assert.ok(wallAudit.walls.every(x=>x.minEdgeMm!==null));
assert.equal(JSON.stringify(qaSource),qaBefore,'Read-only quality audit must not mutate sources');
assert.equal(q.dimensionStatus(qaSource.shafts[0].snapshot),'unconfirmed');
const marked=JSON.parse(qaBefore);marked.shafts[0].snapshot.sourceInfo={sourceVersion:'PDF V2'};
assert.equal(q.dimensionStatus(marked.shafts[0].snapshot),'schematic');
const verified=JSON.parse(qaBefore);verified.shafts[0].snapshot.dims.confirmed=true;
assert.equal(q.dimensionStatus(verified.shafts[0].snapshot),'confirmed');
assert.equal(q.evaluate(verified,{}).dataStatus.confirmed,1);
assert.equal(q.evaluate(marked,{}).dataStatus.schematic,1);
const withWall=q.evaluate(qaSource,{wallGapMm:{SW1:100},minPipeGapMm:10});
assert.ok(withWall.issues.some(x=>x.code==='PIPE_CLEARANCE'&&x.detail.includes('100 mm')));
const withoutWall=q.evaluate(qaSource,{wallGapMm:{SW2:100},minPipeGapMm:10});
assert.equal(withoutWall.issues.filter(x=>x.code==='PIPE_CLEARANCE').length,0);
assert.ok(q.evaluate(qaSource,{dnGapMm:{50:110}}).issues.some(x=>x.code==='PIPE_CLEARANCE'));
assert.equal(q.normalizeSettings({wallGapMm:{SW5:3,SW1:45},dnGapMm:{160:120,250:90}}).dnGapMm[160],120);
assert.deepEqual(q.normalizeSettings({wallGapMm:{SW5:12},dnGapMm:{250:20}}),
 {minPipeGapMm:null,minEdgeMm:null,minBendRadiusMm:null});
const missingThickness=JSON.parse(qaBefore);
delete missingThickness.shafts[0].snapshot.walls[0].thickness;
assert.ok(q.evaluate(missingThickness,{}).issues.some(x=>x.code==='WALL_THICKNESS_MISSING'));
const overlaps=JSON.parse(qaBefore);
overlaps.shafts[0].snapshot.walls[0].holes[1].u=.315;
overlaps.shafts[0].snapshot.walls[0].holes[1].dn=100;
const critical=q.evaluate(overlaps,{minPipeGapMm:80,minEdgeMm:50});
const pipeIssue=critical.issues.find(x=>x.code==='PIPE_OVERLAP');
assert.ok(pipeIssue&&pipeIssue.ref==='B'&&pipeIssue.detail.includes('A'));
assert.equal(critical.issues[0].severity,'error','Severe findings must sort first');
assert.equal(new Set(critical.issues.map(x=>[x.code,x.shaftId,x.ref,x.detail].join('|'))).size,
 critical.issues.length,'Duplicate warnings must not survive repeat calculation');
overlaps.shafts[0].snapshot.walls[0].holes[1].u=.6;
assert.equal(q.evaluate(overlaps,{}).issues.some(x=>x.code==='PIPE_OVERLAP'),false,
 'Fixed overlap must disappear immediately');
const equality=JSON.parse(qaBefore),two=equality.shafts[0].snapshot.walls[0].holes;
const equalDistance=(two[1].u-two[0].u)*2000-(two[0].dn+two[1].dn)/2;
assert.equal(q.evaluate(equality,{minPipeGapMm:equalDistance}).issues.some(x=>x.code==='PIPE_CLEARANCE'),false,
 'Exact equality must not be flagged as below threshold');
const highLoad={shafts:[{id:'MASS',snapshot:{
 dims:{w:120000,d:120000,h:120000},
 walls:[{id:'SW1',thickness:100,holes:Array.from({length:2400},(_,i)=>({
 id:'H'+i,u:(i%50+.5)/50,v:(Math.floor(i/50)+.5)/48,dn:30}))}],
 cables:[]}}],links:[],routes:[]};
const highResult=q.evaluate(highLoad,{minPipeGapMm:10});
assert.equal(highResult.counts.error,0);
assert.equal(highResult.counts.warning,0);
assert.equal(highResult.dataStatus.unconfirmed,1);
const uiHtml=require('node:fs').readFileSync(require('node:path').join(__dirname,'../index.html'),'utf8');
const uiApp=require('node:fs').readFileSync(require('node:path').join(__dirname,'../app.js'),'utf8');
for(const id of ['qualityRuleWall','qualityWallGap','qualityRuleDn','qualityDnGap'])
 assert.ok(uiHtml.includes('id="'+id+'"'),'Missing wall/DN threshold form '+id);
assert.ok(uiApp.includes('qualityModel.normalizeSettings({...raw,wallGapMm,dnGapMm})'));
console.log('KAB-0433–0450 OK: 2400-pipe stress, overrides, sorting, provenance, collisions, limits and read-only state');

console.log('OK: global search, physical-bore warnings, PDF wall axes, review metadata and bend proxy');
