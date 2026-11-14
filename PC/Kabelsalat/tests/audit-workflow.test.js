'use strict';
/* KAB-0811…0828: independent negative checks for canonical ID, priority/epic
 * counts, atomic completion proof and no fabricated milestone progress. */
const assert=require('node:assert/strict');
const mod=require('../TASKS/audit-workflow.js');
const files=mod.disk();
const before=JSON.stringify(files),valid=mod.audit(files);
assert.equal(valid.totals.parents,888);
assert.equal(valid.totals.closed,19);
assert.equal(valid.totals.open,869);
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
assert.ok(md.includes('| Eltern-Tickets | 19 | 869 | 888 |'));
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
