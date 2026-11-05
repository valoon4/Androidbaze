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
console.log('OK: global search, programmable warnings, review metadata and bend-radius proxy');
