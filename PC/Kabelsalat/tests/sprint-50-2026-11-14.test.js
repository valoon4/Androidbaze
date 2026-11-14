'use strict';
// Sprint 14.11.2026: new regression contracts for KAB-0881 / KAB-0882.
// These tests use synthetic text and photo fixtures only: no claimed OCR,
// original PDF import, or engineering validation.
const assert=require('node:assert/strict');
const fs=require('node:fs'),path=require('node:path');
const pipe=require('../pipe-dimensions.js');
const photo=require('../photo-candidates.js');
const app=fs.readFileSync(path.join(__dirname,'../app.js'),'utf8');
const html=fs.readFileSync(path.join(__dirname,'../index.html'),'utf8');
const packageJson=require('../package.json');

// KAB-0881/M02..M07: individually traceable candidate labels, unverified units.
const cases=[
 ['KAB-0881/M02','SW1/T1/LR05 DN 50',50,'SW1_T1_LR05'],
 ['KAB-0881/M03','SW2/T3/LR02 DN 110',110,'SW2_T3_LR02'],
 ['KAB-0881/M04','SW3/T2/LR09 DM 116',116,'SW3_T2_LR09'],
 ['KAB-0881/M05','SW4/T1/LR01 DN 30',30,'SW4_T1_LR01'],
 ['KAB-0881/M06','SW4/T1/LR02 DN 160',160,'SW4_T1_LR02']
];
for(const [id,line,mm,pipeId] of cases){
 const found=pipe.parsePlanText(line,{source:'Synthetischer Testplan'});
 assert.equal(found.length,1,id);
 assert.equal(found[0].pipeId,pipeId,id);
 assert.equal(found[0].candidateMm,mm,id);
 assert.equal(found[0].status,'candidate',id);
 assert.equal(found[0].confidence,null,id);
 assert.equal(found[0].source,'Synthetischer Testplan',id);
}
assert.match(pipe.parsePlanText('SW3/T2/LR09 DM 116')[0].label,/DM 116/i,
 'KAB-0881/M04: DM original label retained, not reviewed outer diameter');
// KAB-0881/M07: no invented pipe ID.
const orphan=pipe.parsePlanText('Unbekanntes Rohr DN 110')[0];
assert.equal(orphan.pipeId,null);assert.equal(orphan.loosePipeId,null);
// KAB-0881/M09: deterministic 1-based source line.
const provenance=pipe.parsePlanText('Kommentar\nSW1/T1/LR05 DN 50',
 {source:'synthetic-plan.txt'})[0];
assert.equal(provenance.line,2);assert.equal(provenance.source,'synthetic-plan.txt');
// KAB-0881/M10: parse never mutates original project or hole Nennweite.
const sample={id:'SW1_T1_LR05',dn:75,dimensionSpec:{openingMm:90}};
const before=JSON.stringify(sample);
pipe.parsePlanText('SW1/T1/LR05 DN 50');
assert.equal(JSON.stringify(sample),before);
// KAB-0881/M13/M35: text classifier never claims reviewed.
assert.ok(pipe.parsePlanText('SW1/T1/LR05 DN 110\nSW1/T1/LR06 DM 116')
 .every(x=>x.status==='candidate'));
// KAB-0881/M24: DM and DN labels are distinct even for equal mm.
const labels=pipe.parsePlanText('SW1/T1/LR05 DN 110\nSW1/T1/LR05 DM 110');
assert.match(labels[0].label,/DN/i);assert.match(labels[1].label,/DM/i);
assert.equal(labels[0].candidateMm,labels[1].candidateMm);
// KAB-0881/M26: line split does not invent ID attachment across lines.
const broken=pipe.parsePlanText('SW1/T1/LR05\nDN 50')[0];
assert.equal(broken.pipeId,null,'No cross-line inferred pipe mapping');
// KAB-0881/M15/M19: UI explicitly distinguishes pasted TXT from PDF binary.
assert.match(app,/PDF-Binärtexte oder Bildtexte werden hier nicht automatisch extrahiert/);
assert.ok(app.includes("planDimensionText")&&app.includes("planTextFile"));
// KAB-0882/M05: raw circles only give pixel sizes.
const width=120,height=120,pixels=new Uint8ClampedArray(width*height*4);
for(let y=0;y<height;y++)for(let x=0;x<width;x++){
 const ix=(y*width+x)*4;
 const dark=Math.hypot(x-33,y-32)<12||Math.hypot(x-83,y-82)<19;
 pixels[ix]=pixels[ix+1]=pixels[ix+2]=dark?20:190;pixels[ix+3]=255;
}
const input=pixels.slice(),detected=photo.detect(pixels,width,height);
assert.equal(detected.candidates.length,2);
assert.ok(detected.candidates.every(x=>Number.isFinite(x.diameterPx)&&
 !Object.hasOwn(x,'candidateMm')&&x.status==='candidate'));
assert.deepEqual(pixels,input);
// KAB-0882/M07/M08/M09: no false calibration on invalid inputs.
assert.throws(()=>photo.calibrate(detected.candidates,0,0),/Referenzkreis/);
assert.throws(()=>photo.calibrate(detected.candidates,0,NaN),/Referenzkreis/);
assert.throws(()=>photo.calibrate(detected.candidates,0,'text'),/Referenzkreis/);
assert.throws(()=>photo.calibrate(detected.candidates,99,110),/Referenzkreis/);
const invalid=photo.calibrate(detected.candidates,0,110);
assert.ok(invalid.every(x=>x.status==='candidate'&&Number.isFinite(x.candidateMm)));
// KAB-0882/M11/M12: actual UI upload checks, not an assertion of browser click.
assert.ok(app.includes("['image/png','image/jpeg','image/webp'].includes(file.type)"));
assert.ok(app.includes('file.size>10000000'));
// KAB-0882/M14/M15: calibrated result is never auto-reviewed; photo + index provenance.
assert.match(app,/method:'photo-calibrated',source:'Foto: '\+photoAnalysis\.filename/);
assert.match(app,/\(Kreiskandidat '\+\(c\.index\+1\)\+'\)'/);
assert.match(app,/status:'candidate',confidence:c\.quality/);
// KAB-0882/M16: capabilities are explicitly labelled as heuristics.
assert.ok(app.includes('heuristisch, keine KI/OCR'));
assert.match(photo.detect(pixels,width,height).note,/keine KI/);
// KAB-0882/M32: calibration refuses NaN/zero factors before candidate persistence.
assert.throws(()=>photo.calibrate(detected.candidates,0,'NaN'));
assert.throws(()=>photo.calibrate([{diameterPx:0}],0,110));
assert.ok(!JSON.stringify(invalid).includes('NaN'));
// Existing suite integration, no fake runtime/CI evidence.
assert.ok(packageJson.scripts.test.includes('tests/sprint-50-2026-11-14.test.js'));
console.log('OK: synthetic plan labels and photo calibration are candidates only (Sprint 50)');
