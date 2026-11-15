'use strict';
/* KAB-0811…0828: independent negative checks for canonical ID, priority/epic
 * counts, atomic completion proof and no fabricated milestone progress. */
const assert=require('node:assert/strict');
const mod=require('../TASKS/audit-workflow.js');
const files=mod.disk();
const before=JSON.stringify(files),valid=mod.audit(files);
assert.equal(valid.totals.parents,888);
assert.ok(valid.totals.closed>=200,'200-parent milestone must not regress');
assert.equal(valid.totals.open,888-valid.totals.closed);
assert.equal(valid.totals.micros,13012);
assert.equal(valid.totals.completed,files['ATOMIC/COMPLETED.csv'].trim().split(/\r?\n/).length-1);
assert.equal(valid.totals.completed+valid.totals.remaining,13012);
assert.equal(valid.waves.length,7);
assert.equal(valid.epics.reduce((n,e)=>n+e.total,0),13012);
assert.equal(valid.waves.reduce((n,w)=>n+w.complete,0),valid.totals.completed);
assert.equal(valid.waves.reduce((n,w)=>n+w.open,0),valid.totals.remaining);
assert.equal(Object.values(valid.parentFileCounts).reduce((a,b)=>a+b,0),888);
assert.ok(valid.milestones.statement.includes('keine Task-Abnahmequoten'));
assert.deepEqual(mod.csv('"a";"b;semi";"a""b"', ';'),['a','b;semi','a"b']);
assert.throws(()=>mod.csv('"unclosed',';'),/unmatched quote/);
const md=mod.markdown(valid);
assert.ok(md.includes('| **Mikro-Tasks** | **'+valid.totals.completed+'**'));
assert.ok(md.includes('| Eltern-Tickets | '+valid.totals.closed+' | '+valid.totals.open+' | 888 |'));
for(const id of ['KAB-0884','KAB-0886','KAB-0887','KAB-0888']){
 const file=Object.entries(files).find(([key,text])=>/^0[78]-BRO-/.test(key)&&text.includes(`- [x] **${id}** –`));
 assert.ok(file,`${id} must remain closed only with a documented technical acceptance`);
 assert.match(file[1],new RegExp('\\*\\*'+id+'\\*\\*.*Nachweis: https://github\\.com/'));
}
for(const id of ['KAB-0051','KAB-0052','KAB-0053']){
 const source=files['01-P0-QUELLE-UND-PITCH.md'];
 assert.ok(source.includes('- [x] **'+id+'** –'),'Spacing parent must be closed: '+id);
 assert.match(source.split('\n').find(l=>l.includes('**'+id+'**')),/Nachweis: https:\/\/github\.com\//);
}
for(let n=235;n<=250;n++){
 const id='KAB-'+String(n).padStart(4,'0');
 const line=files['02-P1-EDITOR-UND-NETZ.md'].split('\n').find(l=>l.startsWith('- [x] **'+id+'** –'));
 assert.ok(line,'Missing technical editor parent: '+id);
 assert.match(line,/Nachweis: https:\/\/github\.com\//);
}
// The 58 newly reviewed parents remain individually accountable with immutable proof links.
const hundred=[...Array.from({length:20},(_,i)=>i+251),
 ...Array.from({length:16},(_,i)=>i+775),792,
 ...Array.from({length:5},(_,i)=>i+811),
 ...Array.from({length:11},(_,i)=>i+818),
 745,649,650,651,652].map(n=>'KAB-'+String(n).padStart(4,'0'));
assert.equal(hundred.length,58);
for(const id of hundred){
 const file=id.slice(4)>='0649'?'04-P3-QA-BETRIEB-UND-FEEDBACK.md':'02-P1-EDITOR-UND-NETZ.md';
 const row=files[file].split(String.fromCharCode(10)).find(l=>l.startsWith('- [x] **'+id+'** – '));
 assert.ok(row,'Reviewed parent still missing: '+id);
 assert.ok(row.includes('Nachweis: https://github.com/valoon4/Androidbaze/blob/'),
  'Immutable evidence URL missing: '+id);
 assert.match(row,new RegExp('blob/[0-9a-f]{40}/'),'Evidence must pin a commit: '+id);
}
// Second giant: 47 + 28 + 25 = 100 NEW parents with three independent proof docs.
const waves=[
 {commit:'7e63d5adb1d59e4c0fa4b602e4ede99efc1550ae',
  parents:[...Array.from({length:18},(_,i)=>397+i),
   ...Array.from({length:18},(_,i)=>433+i),
   271,272,273,275,276,277,278,279,280,281,287]},
 {commit:'cc87614d522a21fc93ee85bcd2cea77314e7c21f',
  parents:[343,345,346,348,349,351,352,353,354,355,358,359,360,
   361,362,363,364,365,366,367,369,370,373,374,375,376,377,378]},
 {commit:'982f6d6a89fb0747d1580e45549d5d34ccfd229e',
  parents:[415,419,420,421,422,425,426,427,428,429,432,
   ...Array.from({length:14},(_,i)=>653+i)]}
];
assert.deepEqual(waves.map(w=>w.parents.length),[47,28,25]);
assert.equal(new Set(waves.flatMap(w=>w.parents)).size,100);
for(const w of waves)for(const n of w.parents){
 const id='KAB-'+String(n).padStart(4,'0');
 const filename=n>=649?'04-P3-QA-BETRIEB-UND-FEEDBACK.md':
  n>=433?'03-P2-DATEN-UND-PRUEFUNG.md':'02-P1-EDITOR-UND-NETZ.md';
 const row=files[filename].split('\n').find(l=>l.startsWith('- [x] **'+id+'** – '));
 assert.ok(row,'Missing accepted parent: '+id);
 assert.ok(row.includes('Nachweis: https://github.com/valoon4/Androidbaze/blob/'+w.commit+
  '/PC/Kabelsalat/TASKS/EVIDENCE_2026-11-15_NEXT_100_WAVE'),
  'Wrong immutable acceptance proof: '+id);
}

for(const wave of mod.WAVE)assert.ok(md.includes('| '+wave+' |'));
const copy=()=>({...files});
function mutate(key,fn,pattern){
 const c=copy();c[key]=fn(c[key]);assert.throws(()=>mod.audit(c),pattern,key);
}
const first=files[mod.SOURCE[0]].match(/^- \[ \] \*\*KAB-\d{4}\*\* – .+$/m)?.[0];
assert.ok(first);
mutate(mod.SOURCE[0],s=>s.replace(first,first.replace(/KAB-\d{4}/,'KAB-0002')),/Duplicate canonical ID|Canonical ID gap/);
mutate(mod.SOURCE[0],s=>s.replace(first,''),/Canonical ID gap|baseline changed/);
mutate(mod.SOURCE[0],s=>s.replace(first,first.replace('- [ ]','- [x]')),/Parent closed without evidence/);
const waveFile='ATOMIC/S6.csv';
const one=files[waveFile].match(/;"offen";/)?.[0];assert.ok(one);
mutate(waveFile,s=>s.replace(';"offen";',';"erledigt";'),/Atomic tracker and proof ledger disagree/);
mutate('ATOMIC/COMPLETED.csv',s=>s+s.trim().split(/\r?\n/).at(-1)+'\n',/invalid completion proof/);
mutate('ATOMIC/COMPLETED.csv',s=>s.replace(/https:\/\/github\.com\//,'https://example.com/'),/invalid completion proof/);
const csvRows=files[waveFile].split(/\r?\n/);
mutate(waveFile,s=>s.replace(csvRows[1],csvRows[1].replace(/KAB-\d{4}\/M\d{2}/,'KAB-XXXX/M02')),/Invalid\/duplicate atomic row/);
assert.equal(JSON.stringify(files),before,'Validation must never mutate data');
console.log('WORKFLOW AUDIT OK: 888 unique parent IDs, 13012 atomic slots, seven stages, commit proof and negative cases');
