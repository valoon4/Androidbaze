'use strict';
const assert=require('node:assert/strict'),pipe=require('../pipe-dimensions.js');
const legacy={id:'SW1_T1_LR01',dn:110,sourceU:.2,sourceV:.4};
assert.deepEqual(pipe.forHole(legacy),pipe.normalize({},110));
assert.equal(pipe.apertureMm(legacy),110,'Old DN stays a provisional drawing proxy');
assert.match(pipe.describe(legacy),/unbestätigt/);
const manual={...legacy,dimensionSpec:{nominalMm:110,innerMm:90,outerMm:116,
 openingMm:118,source:'Referenzmessung R03',method:'manual',status:'reviewed',
 reviewer:'Test-Fachpruefer'}};
assert.deepEqual(pipe.forHole(manual),{
 nominalMm:110,innerMm:90,outerMm:116,openingMm:118,candidateMm:null,
 unit:'mm',method:'manual',source:'Referenzmessung R03',status:'reviewed',
 reviewer:'Test-Fachpruefer',reviewedAt:'',confidence:null,note:''});
assert.equal(pipe.apertureMm(manual),118,'Opening bore must not be DN or outer diameter');
assert.match(pipe.describe(manual),/Öffnung-Ø 118 mm/);
assert.equal(pipe.forHole({...manual,dimensionSpec:{openingMm:118,status:'reviewed'}}).status,
 'candidate','Unproven reviewed dimension must be downgraded');
assert.equal(pipe.forHole({...manual,dimensionSpec:{openingMm:119,status:'candidate',source:''}}).status,
 'unverified','Unattributed measurement cannot be a confident candidate');
assert.equal(pipe.apertureMm({dn:116,dimensionSpec:{openingMm:'evil',candidateMm:60}}),116);
const raw='SW1/T1/LR05 DN 110\nSW2_T3_LR01 Ø 75 mm\nSW3/T2/LR2 DM 60\nDurchmesser 116 mm';
const out=pipe.parsePlanText(raw,{source:'PDF-Vektortext, Testfixture'});
assert.deepEqual(out.map(x=>x.candidateMm),[110,75,60,116]);
assert.equal(out[0].pipeId,'SW1_T1_LR05');
assert.equal(out[1].pipeId,'SW2_T3_LR01');
assert.equal(out[2].pipeId,'SW3_T2_LR02');
assert.equal(out[3].pipeId,null);
assert.ok(out.every(x=>x.status==='candidate'&&x.confidence===null));
assert.deepEqual(pipe.parsePlanText('60 110 116 ganz ohne DN oder Ø'),[],
 'Bare digits must never be inventively classified as nominal pipe dimensions');
const serialized=JSON.parse(JSON.stringify(manual));
assert.deepEqual(serialized,manual,'Dimension model survives JSON serialization');
console.log('OK: independent DN/inner/outer/bore/candidate dimensions and sourced PDF-label proposals');
