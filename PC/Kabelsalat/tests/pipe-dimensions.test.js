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
 'unverified','Unattributed reviewed dimension must be downgraded to unverified');
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
// KAB-0881: extracted page text is untrusted, bounded, versioned by page and
// never silently merged into the source project.
const planPages=[
 {pageNumber:3,rotation:90,text:'SW1/T1/LR05 DN 30\nSW2_T2_LR07 DN 110'},
 {pageNumber:5,rotation:0,text:'SW1/T1/LR05 DN 160\nSW3_T2_LR01 Ø 75'},
 {pageNumber:6,text:'SW4/T2/LR01 DN 50\nSW4/T2/LR02 DN 50'}
];
const pagesBefore=JSON.stringify(planPages),multiple=pipe.parsePlanPages(planPages,{source:'fixture-plan.pdf',documentVersion:'v2'});
assert.deepEqual(multiple.map(x=>x.page),[3,3,5,5,6,6]);
assert.equal(multiple[0].rotation,90);
assert.equal(multiple[0].provenance.source,'fixture-plan.pdf');
assert.equal(multiple[0].provenance.documentVersion,'v2');
assert.equal(multiple[0].scaleStatus,'not-provided');
assert.equal(multiple[0].measurementBasis,'drawing-label');
assert.equal(multiple[0].provenance.line,1);
assert.equal(multiple[0].candidateMm,30);
assert.equal(multiple[2].candidateMm,160);
assert.ok(multiple[0].conflict&&multiple[2].conflict);
assert.ok(!multiple[1].conflict);
assert.equal(multiple[4].pipeId,'SW4_T2_LR01');
assert.equal(multiple[5].pipeId,'SW4_T2_LR02');
const ambiguous=pipe.parsePlanPages([{text:'SW1_T1_LR01 SW1_T1_LR02 DN 50'}]);
assert.equal(ambiguous.length,1);
assert.equal(ambiguous[0].pipeId,null,'Multiple pipe IDs must not silently assign the first');
assert.match(ambiguous[0].reason,/Mehrere Rohr-IDs/);
assert.equal(multiple[0].status,'candidate');
assert.equal(multiple[0].reviewed,false);
assert.equal(JSON.stringify(planPages),pagesBefore,'PDF text parsing must be read-only');
const sorted=pipe.planCandidateTable(planPages,{source:'fixture-plan.pdf'});
assert.deepEqual(sorted.map(x=>x.pipeId),[...sorted.map(x=>x.pipeId)].sort());
assert.deepEqual(pipe.parsePlanPages([{text:'No recognized label'}]),[]);
assert.throws(()=>pipe.parsePlanPages([{text:'DN 30'},{text:'DN 50'}],{maxPages:1}),/Seitenlimit/);
assert.throws(()=>pipe.parsePlanPages([{text:'DN 30'}],{maxChars:2}),/Textbudget/);
assert.throws(()=>pipe.parsePlanPages([{text:'DN 30',rotation:45}]),/rotation/i);
assert.throws(()=>pipe.parsePlanPages([{text:'DN 30'}],{documentVersion:42}),/Dokumentversion/);
assert.throws(()=>pipe.parsePlanPages([{text:null}]),/Textlayer/);
assert.throws(()=>pipe.parsePlanPages({text:'DN 50'}),/Liste/);
const invalidBefore=JSON.stringify(planPages);
assert.throws(()=>pipe.parsePlanPages([...planPages,{text:'DN 30',rotation:11}]),/rotation/i);
assert.equal(JSON.stringify(planPages),invalidBefore,'Failed page batch leaves input untouched');
const standalone=JSON.parse(JSON.stringify(multiple));
assert.deepEqual(standalone,multiple,'Page source metadata survives JSON export/import');
// KAB-0881/M30/M37/M38/M39/M43: independent parse transaction and
// explicit recoverable error reports for scanned or malformed PDF pages.
const reportPages=[{pageNumber:21,rotation:180,text:'SW1_T1_LR01 DN 50'}];
const reportSnapshot=JSON.stringify(reportPages);
const report=pipe.inspectPlanPages(reportPages,{source:'fixture-a.pdf',documentVersion:'r3'});
assert.equal(report.status,'candidates');
assert.equal(report.candidates[0].provenance.documentVersion,'r3');
assert.equal(report.candidates[0].provenance.page,21);
assert.equal(JSON.parse(JSON.stringify(report)).candidates[0].provenance.source,'fixture-a.pdf');
assert.equal(JSON.stringify(reportPages),reportSnapshot);
const scan=pipe.inspectPlanPages([{pageNumber:9,text:null}],{source:'scan.pdf'});
assert.equal(scan.status,'manual-review');
assert.deepEqual(scan.candidates,[]);
assert.equal(scan.errors[0].code,'missing-text-layer');
assert.match(scan.errors[0].message,/OCR/);
const corrupt=pipe.inspectPlanPages([{pageNumber:17,rotation:17,text:'DN 60'}]);
assert.equal(corrupt.status,'invalid-source');
assert.equal(corrupt.candidates.length,0);
assert.equal(corrupt.errors[0].code,'invalid-pdf-text');
assert.match(corrupt.errors[0].message,/rotation/i);
const invalidList=pipe.inspectPlanPages({text:'DN 50'});
assert.equal(invalidList.status,'invalid-source');
assert.equal(invalidList.errors[0].code,'invalid-pages');
const overPages=pipe.inspectPlanPages([{text:'DN 30'},{text:'DN 160'}],{maxPages:1});
assert.equal(overPages.status,'invalid-source');
assert.match(overPages.errors[0].message,/Seitenlimit/);
const overText=pipe.inspectPlanPages([{text:'SW1_T1_LR02 DN 110'}],{maxChars:4});
assert.equal(overText.status,'invalid-source');
assert.match(overText.errors[0].message,/Textbudget/);
assert.equal(JSON.stringify(reportPages),reportSnapshot,'No partial source change on error');
console.log('OK: independent DN/inner/outer/bore/candidate dimensions and sourced PDF-label proposals');
