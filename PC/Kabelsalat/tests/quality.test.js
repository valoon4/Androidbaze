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
console.log('OK: global search, physical-bore warnings, PDF wall axes, review metadata and bend proxy');
