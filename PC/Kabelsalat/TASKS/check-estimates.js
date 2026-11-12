'use strict';
/* Verify every currently unchecked canonical task has exactly one estimate.
 * Any change to source tasks or priorities invalidates the snapshot until
 * running: node TASKS/estimate-time.js --write
 */
const assert=require('node:assert/strict');
const fs=require('node:fs');
const path=require('node:path');
const estimator=require('./estimate-time.js');
const root=__dirname,read=name=>fs.readFileSync(path.join(root,name),'utf8');
const expected=estimator.generate(read);
const actual=read('TASK_ESTIMATES.csv');
assert.equal(actual,expected.csv,
 'Task estimates are stale or inconsistent: run node TASKS/estimate-time.js --write');
const rows=estimator.parseCSV(actual,';');
assert.deepEqual(rows[0],estimator.HEADERS);
assert.equal(rows.length-1,expected.count,'Unexpected number of estimate rows');
const priority=estimator.parseCSV(read('PRIORITY_ORDER.csv'));
const source=estimator.canon(read);
let seen=new Set(),minutes=0,dependencies=0;
for(const row of rows.slice(1)){
 assert.equal(row.length,13,'Malformed estimate CSV columns');
 const [rank,id,phase,epic,title,min,likely,max,type,dependency,confidence,reason,file]=row;
 assert.match(id,/^KAB-\d{4,}$/);
 assert.ok(!seen.has(id),'Duplicate estimate ID: '+id);
 seen.add(id);
 const task=source.get(id);
 assert.ok(task&&!task.done,'Only currently open tasks may be estimated: '+id);
 assert.equal(task.title,title,'Description drift: '+id);
 assert.equal(task.file,file,'Wrong estimate source file: '+id);
 assert.equal(priority[Number(rank)]?.[1],id,'Rank or task ID drift: '+id);
 assert.equal(priority[Number(rank)]?.[2],phase,'Wrong sprint: '+id);
 const n=[min,likely,max].map(s=>Number(s.replace(',','.')));
 assert.ok(n.every(v=>Number.isFinite(v)&&v>=.25&&v<=100),
  'Estimate hours must be positive, finite and bounded: '+id);
 assert.ok(n[0]<=n[1]&&n[1]<=n[2],'Estimate range is reversed: '+id);
 assert.ok(type&&epic&&dependency&&reason,'Estimate rationale missing for '+id);
 assert.ok(['mittel','niedrig','sehr niedrig'].includes(confidence));
 minutes+=Math.round(n[1]*60);
 if(dependency!=='Keine')dependencies++;
}
assert.equal(seen.size,expected.count,'Not every open task has a unique estimate');
assert.equal(expected.count+expected.done,expected.total,'Canonical task count mismatch');
console.log('KABELSALAT SCHÄTZUNG: '+expected.count+' offene Tasks einzeln geschätzt; '+
 expected.done+' abgeschlossene Tasks bewusst ausgeschlossen; '+dependencies+
 ' mit Voraussetzung (einschließlich Browser/Testquellen/Fachfeedback).');
console.log('Hinweis: Summe ist Brutto-Einzelaufwand, KEIN Liefertermin oder Projekt-Fertigstellungsversprechen.');
for(const wave of expected.summary){
 const h=n=>(n/60).toFixed(1);
 console.log(' '+wave.phase+': '+wave.count+' offen · KI-Arbeitszeit grob '+h(wave.low)+
 '–'+h(wave.high)+' h (typisch '+h(wave.mins)+' h)');
}
