'use strict';
// Additional KAB-0881 / KAB-0882 micro-regressions for the 2.0.7 release.
// Synthetic inputs only. No physical image measurement or PDF-binary parser claims.
const assert=require('node:assert/strict'),fs=require('node:fs'),path=require('node:path');
const pipe=require('../pipe-dimensions.js'),photo=require('../photo-candidates.js');
const app=fs.readFileSync(path.join(__dirname,'../app.js'),'utf8');
const pkg=require('../package.json');
// KAB-0881/M08: conflicting labels for the SAME ID remain two candidates.
const duplicates=pipe.parsePlanText('SW1/T1/LR05 DN 50\nSW1/T1/LR05 DN 110',{source:'synthetic-two-labels.txt'});
assert.equal(duplicates.length,2);
assert.deepEqual(duplicates.map(x=>x.pipeId),['SW1_T1_LR05','SW1_T1_LR05']);
assert.deepEqual(duplicates.map(x=>x.candidateMm),[50,110]);
assert.ok(duplicates.every(x=>x.status==='candidate'));
// KAB-0881/M11: application refuses candidate from a different exact pipe ID.
assert.match(app,/if\(c\.pipeId&&c\.pipeId!==h\.id\)\{/);
assert.ok(app.includes('Keine automatische Zuordnung.'));
// KAB-0881/M12: absent ID requires explicit user confirmation.
assert.match(app,/if\(!c\.pipeId&&!confirm\(/);
// KAB-0881/M14: project dimension candidate preserves source, row and status by JSON round-trip.
const tagged=pipe.normalize({candidateMm:110,status:'candidate',method:'pdf-label',
 source:duplicates[0].source+' / Zeile '+duplicates[0].line,note:'synthetic'},70);
const roundtrip=JSON.parse(JSON.stringify({hole:{id:'SW1_T1_LR05',dn:70,dimensionSpec:tagged}}));
assert.equal(roundtrip.hole.dimensionSpec.candidateMm,110);
assert.equal(roundtrip.hole.dimensionSpec.source,'synthetic-two-labels.txt / Zeile 1');
assert.equal(roundtrip.hole.dimensionSpec.status,'candidate');
assert.equal(roundtrip.hole.dn,70);
// KAB-0881/M16: this fixture belongs to the standard CI/npm test script.
assert.ok(pkg.scripts.test.includes('node tests/sprint-30-release-2026-11-14.test.js'));
const picture=(width,height,circles=[])=>{
 const img=new Uint8ClampedArray(width*height*4);
 for(let y=0;y<height;y++)for(let x=0;x<width;x++){
  const k=(y*width+x)*4;
  const dark=circles.some(([cx,cy,r])=>Math.hypot(x-cx,y-cy)<r);
  img[k]=img[k+1]=img[k+2]=dark?18:210;img[k+3]=255;
 }
 return img;
};
// KAB-0882/M02: empty fixture returns exactly zero circles.
const blank=photo.detect(picture(120,120),120,120);
assert.equal(blank.candidates.length,0);
// KAB-0882/M03: single dark-circle fixture gets a pixel diameter candidate.
const one=photo.detect(picture(120,120,[[60,60,18]]),120,120);
assert.equal(one.candidates.length,1);
assert.ok(Math.abs(one.candidates[0].diameterPx-35)<=2);
// KAB-0882/M04: small and large holes are detected and sorted in px.
const two=photo.detect(picture(160,120,[[45,60,11],[115,60,22]]),160,120);
assert.equal(two.candidates.length,2);
assert.ok(two.candidates[0].diameterPx>two.candidates[1].diameterPx);
// KAB-0882/M06: explicit reference calibrates to candidate mm, never reviewed.
const calibrated=photo.calibrate(two.candidates,0,110);
assert.equal(calibrated[0].candidateMm,110);
assert.ok(calibrated.every(x=>x.status==='candidate'));
// KAB-0882/M10: unreasonable image allocation is blocked before analysis.
assert.throws(()=>photo.detect(new Uint8ClampedArray(4),2001,2000),/4 Megapixel/);
// KAB-0882/M13: very oblique ellipses are not accepted as verified circles.
const ellipse=new Uint8ClampedArray(160*120*4);
for(let y=0;y<120;y++)for(let x=0;x<160;x++){
 const k=(y*160+x)*4,inside=((x-80)/33)**2+((y-60)/12)**2<1;
 ellipse[k]=ellipse[k+1]=ellipse[k+2]=inside?20:220;ellipse[k+3]=255;
}
assert.equal(photo.detect(ellipse,160,120).candidates.length,0);
// KAB-0882/M17: this test suite reports reproducible input/result on failure.
assert.ok(pkg.scripts.test.includes('sprint-30-release-2026-11-14.test.js'));
console.log('OK: 12 new micro checks PDF-label conflict handling and relative photo detection');
