'use strict';
const assert=require('node:assert/strict');
const geom=require('../editor2d.js');
const m=geom.metrics(2400,2100,520,540);
for(const p of [[0,0],[1200,900],[2400,2100],[700,1455]]){
 const c=geom.toPixels(m,...p),q=geom.toMillimetres(m,c.x,c.y);
 assert.equal(q.x,p[0]);assert.equal(q.y,p[1]);
}
const h1={id:'h1',u:0.5,v:0.5,dn:100};
const p=geom.toPixels(m,1200,1050);
assert.equal(geom.hitHole([h1],m,p.x,p.y)?.id,'h1');
const h2={id:'h2',u:0.5+50/2400,v:0.5,dn:100};
assert.equal(geom.clearance([h1,h2],2400,2100).overlaps,1);
assert.equal(geom.clearance([h1,h2],2400,2100).minPairGap,-50);
const cc=geom.copyOffset(h1,2400,2100,150,25);
assert.equal(Math.round(cc.u*2400),1350);
assert.equal(Math.round(cc.v*2100),1075);
assert.equal(cc.dn,100);
assert.equal(geom.toMillimetres(m,-500,-500).x,0);
assert.equal(geom.toMillimetres(m,9000,9000).y,0);
console.log('OK: 2D mapping, selection, clearance, copy and round-trip in mm');
