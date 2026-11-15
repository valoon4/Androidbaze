'use strict';
/* Synthetic digital PDFs and route text. Never uses real construction documents. */
const assert=require('node:assert/strict');
const fs=require('node:fs'),path=require('node:path'),zlib=require('node:zlib');
const extractor=require('../plan-route-import.js');
const handover=require('../shaft-handover.js');
const shafts='schacht_id;schachtname\n'+Array.from({length:400},(_,i)=>
 'S'+String(i+1).padStart(4,'0')+';Schacht '+(i+1)).join('\n')+'\n';
const rows=handover.readCsv(shafts,handover.shaftHeaders,500);
function mockPdf(content1,content2,compress=false){
 const bytes1=Buffer.from('BT /F1 11 Tf 30 30 Td ('+content1+') Tj ET','latin1');
 const bytes2=Buffer.from('BT /F1 11 Tf 30 30 Td ('+content2+') Tj ET','latin1');
 const s1=compress?zlib.deflateSync(bytes1):bytes1;
 const s2=compress?zlib.deflateSync(bytes2):bytes2;
 const payload=part=>'<< /Length '+part.length+(compress?' /Filter /FlateDecode':'')+
  ' >>\nstream\n'+part.toString('latin1')+'\nendstream';
 const objects=[
 '<< /Type /Catalog /Pages 2 0 R >>',
 '<< /Type /Pages /Count 2 /Kids [3 0 R 4 0 R] >>',
 '<< /Type /Page /Parent 2 0 R /Contents 5 0 R >>',
 '<< /Type /Page /Parent 2 0 R /Contents 6 0 R >>',
 payload(s1),payload(s2)
 ];
 return Buffer.from('%PDF-1.4\n'+objects.map((x,i)=>String(i+1)+' 0 obj\n'+
   x+'\nendobj\n').join('')+'%%EOF','latin1');
}
async function main(){
 assert.ok(extractor.textOperators('BT (Kabel K01: S0001 > S0002) Tj ET')
  .includes('Kabel K01: S0001 > S0002'));
 for(const compressed of [false,true]){
  const pdf=mockPdf('Kabel K01: S0001 > S0002 > S0003',
   'Kabel K02: S0012 -> S0022',compressed);
  const doc=await extractor.pdfPages(pdf,'plan.pdf');
  assert.equal(doc.pages.length,2,'text pages must be extracted in original order');
  assert.equal(doc.totalPages,2);
  assert.ok(doc.pages[0].text.includes('Kabel K01'));
  const analyzed=extractor.extract(rows,doc.pages);
  assert.equal(analyzed.candidates.length,2);
  assert.equal(analyzed.candidates[0].source,'plan.pdf S. 1 Z. 1');
  assert.deepEqual(analyzed.candidates[0].shaftIds,['S0001','S0002','S0003']);
  assert.equal(analyzed.summary.physicallyVerified,0);
  const csv=extractor.compile(handover,shafts,'',analyzed.candidates);
  assert.equal(csv.added,5);
  assert.equal(csv.report.ok,true);
  assert.ok(csv.report.handover.every(v=>v.status.includes('ungeprüft')));
  assert.equal(csv.report.summary.withoutEvidence,395);
 }
 const scan=mockPdf('','');
 await assert.rejects(extractor.pdfPages(scan,'scan.pdf'),/Keine extrahierbare PDF-Textebene/);
 await assert.rejects(extractor.pdfPages(Buffer.from('%NOTPDF')),/PDF-Header/);
 await assert.rejects(extractor.pdfPages(Buffer.from('%PDF-1.4\n/Encrypt')),/Verschlüsseltes PDF/);
 const source=[{name:'plan.txt',page:null,text:'Kabel K77: S0001 → S0002\n'+
  'Kabel K77: S0001 → S0002\nKabel K77: S0002 → S0003\n'+
  'Kabel K88: S0001 > S0999\nKabel K99: S0001, S0002\n'+
  'Kabel K32: S0001 > S0002 > S0001'}];
 const candidate=extractor.extract(rows,source);
 assert.equal(candidate.candidates.length,2,'duplicate line must not duplicate routes');
 assert.equal(candidate.summary.routesWithConflicts,1);
 assert.ok(candidate.candidates.every(x=>x.conflict));
 assert.ok(candidate.issues.some(x=>x.reason.includes('Widersprüchliche')));
 assert.ok(candidate.issues.some(x=>x.reason.includes('Unbekannte')));
 assert.ok(candidate.issues.some(x=>x.reason.includes('Reihenfolge')));
 assert.ok(candidate.issues.some(x=>x.reason.includes('mehrfach')));
 assert.throws(()=>extractor.compile(handover,shafts,'',candidate.candidates),
  /mehrere unterschiedliche Routen/);
 assert.equal(extractor.compile(handover,shafts,'',[candidate.candidates[0]]).added,2);
 assert.throws(()=>extractor.compile(handover,shafts,
  handover.rowsCsv([{kabel_id:'K77',kabelname:'Vorhanden',schacht_id:'S0001',
   reihenfolge:1,quelle:'Altplan',status:'PLAN'}],handover.passHeaders),
  [candidate.candidates[0]]),/bereits Zuordnungen/);
 const malicious=extractor.issuesCsv(handover,
  [{source:'@FORMULA',reason:'=BAD',snippet:'+DANGER'}]);
 assert.ok(malicious.includes("'@FORMULA"));
 assert.ok(malicious.includes("'=BAD"));
 assert.ok(malicious.includes("'+DANGER"));
 assert.throws(()=>extractor.extract([{schacht_id:'S1'},{schacht_id:'S1'}],[]),
  /doppelt/);
 const pdf=mockPdf('Kabel K01: S0001 > S0002','Kabel K02: S0002 > S0003');
 const fakeFile={name:'demo.pdf',size:pdf.length,
  arrayBuffer:async()=>pdf.buffer.slice(pdf.byteOffset,pdf.byteOffset+pdf.byteLength)};
 assert.equal((await extractor.readFile(fakeFile)).pages.length,2);
 const ui=fs.readFileSync(path.resolve(__dirname,'../index.html'),'utf8');
 const app=fs.readFileSync(path.resolve(__dirname,'../app.js'),'utf8');
 const pkg=require('../package.json');
 for(const id of ['planRouteFiles','planRoutePaste','planRouteAnalyze','planRouteStatus',
  'planRouteResults','planRouteIssues','planRouteApply','planRouteExportIssues'])
  assert.ok(ui.includes('id="'+id+'"'),'missing plan review field: '+id);
 assert.ok(ui.includes('plan-route-import.js?v='+pkg.version));
 assert.ok(app.includes('planRouteModel.extract('));
 assert.ok(app.includes('planRouteModel.compile('));
 assert.ok(app.includes('planRouteSelected'));
 assert.ok(app.includes('textContent=')&&!app.includes('planRouteResults').endsWith('never'));
 assert.ok(pkg.scripts.test.includes('tests/plan-route-import.test.js'));
 assert.ok(pkg.build.files.includes('plan-route-import.js'));
 console.log('OK: 400-shaft text+digital PDF candidate extraction, review gates, conflicts, source provenance and safe CSV');
}
main().catch(err=>{console.error(err);process.exitCode=1;});
