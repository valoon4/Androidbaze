'use strict';
/* Canonical ranking validation and next-task queue, independent of closed-task status. */
const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');

function csvFields(line) {
 const out = []; let current = ''; let quoted = false;
 for (let i = 0; i < line.length; i++) {
  const ch = line[i];
  if (ch === '"') {
   if (quoted && line[i + 1] === '"') { current += '"'; i++; }
   else quoted = !quoted;
  } else if (ch === ',' && !quoted) { out.push(current); current = ''; }
  else current += ch;
 }
 out.push(current);
 assert.ok(!quoted, 'Unclosed CSV quote');
 return out;
}

const root = __dirname;
const files = fs.readdirSync(root).filter(f => /^\d\d-.*\.md$/.test(f)).sort();
const truth = new Map();
for (const file of files) {
 const contents = fs.readFileSync(path.join(root, file), 'utf8');
 for (const match of contents.matchAll(/^- \[([ x])\] \*\*(KAB-\d{4,})\*\* – (.+)$/gm)) {
  const [,mark,id,title] = match;
  assert.ok(!truth.has(id), 'Duplicate canonical task: ' + id);
  truth.set(id, {id, done: mark === 'x', title, file});
 }
}
assert.ok(truth.size >= 884, 'The baseline 884 tasks must not disappear');

const lines = fs.readFileSync(path.join(root, 'PRIORITY_ORDER.csv'), 'utf8').trimEnd().split(/\r?\n/);
assert.deepEqual(csvFields(lines.shift()), ['Rang','Task-ID','Sprint','Thema','Quellenabhängigkeit','Aufgabe','Backlog-Datei']);
const ranked = [], unique = new Set();
for (const line of lines) {
 const fields = csvFields(line);
 assert.equal(fields.length, 7, 'Seven fields expected in priority row');
 const [rankText,id,stage,category,dependency,title,file] = fields;
 assert.equal(Number(rankText), ranked.length + 1, 'Priority rank sequence is broken');
 assert.match(id, /^KAB-\d{4,}$/, 'Malformed task ID');
 assert.match(stage, /^S[0-6]$/, 'Unrecognized phase');
 assert.ok(!unique.has(id), 'Duplicate priority task: ' + id);
 unique.add(id);
 const source = truth.get(id);
 assert.ok(source, 'Priority row references missing task: ' + id);
 assert.equal(file, source.file, 'Wrong task source file for ' + id);
 // Task descriptions may gain an appended evidence link when a checkbox is closed.
 const canonicalTitle = source.title.replace(/\s*[·—-]\s*Nachweis:\s*.*$/i, '');
 assert.equal(title, canonicalTitle, 'Task description changed: update priority CSV for ' + id);
 assert.ok(category.trim() && dependency.trim(), 'Priority metadata missing for ' + id);
 ranked.push({...source, rank: ranked.length + 1, stage, dependency});
}
assert.equal(ranked.length, truth.size, 'Every backlog task needs exactly one priority rank');
assert.equal(unique.size, truth.size, 'Priority list is missing task IDs');
const completed = ranked.filter(x=>x.done).length;
console.log('KABELSALAT PRIORITY: ' + completed + ' erledigt / ' + (ranked.length-completed) + ' offen / ' + ranked.length + ' gesamt');
for (const stage of ['S0','S1','S2','S3','S4','S5','S6']) {
 const subset=ranked.filter(x=>x.stage===stage);
 console.log('  '+stage+': '+subset.filter(x=>x.done).length+'/'+subset.length+' erledigt');
}
const arg = process.argv.find(x=>/^--next=\d+$/.test(x));
const max = arg ? Number(arg.split('=')[1]) : 15;
console.log('NÄCHSTE OFFENE TASKS (Rang, ID, Titel; Abhängigkeiten einzeln prüfen):');
for (const t of ranked.filter(x=>!x.done).slice(0,max)) {
 console.log('  #'+t.rank+' ['+t.stage+'] '+t.id+': '+t.title);
}
