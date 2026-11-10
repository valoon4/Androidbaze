'use strict';
/* Canonical 828+ task counter: parse the checkboxes in TASKS/0*.md, not release labels. */
const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');

const root = __dirname;
const files = fs.readdirSync(root).filter(f => /^\d\d-.*\.md$/.test(f)).sort();
assert.ok(files.length >= 4, 'Four baseline backlog files are required');
const baseline = new Map([
 ['01-P0-QUELLE-UND-PITCH.md', 216],
 ['02-P1-EDITOR-UND-NETZ.md', 216],
 ['03-P2-DATEN-UND-PRUEFUNG.md', 216],
 ['04-P3-QA-BETRIEB-UND-FEEDBACK.md', 180]
]);
const rows = [];
const titleSet = new Set();
for (const file of files) {
 const md = fs.readFileSync(path.join(root, file), 'utf8');
 const lines = md.split(/\r?\n/);
 let current = 0;
 for (let i = 0; i < lines.length; i++) {
  const line = lines[i];
  if (!line.startsWith('- [') || !line.includes('KAB-')) continue;
  const m = /^- \[([ x])\] \*\*(KAB-(\d{4,}))\*\* – (.+)$/.exec(line);
  assert.ok(m, 'Invalid task line: ' + file + ':' + (i + 1));
  const [, mark, id, digits, title] = m;
  assert.ok(title.trim().length >= 18, 'Task must be actionable: ' + id);
  assert.ok(!titleSet.has(title.trim().toLowerCase()), 'Duplicate task title: ' + id);
  titleSet.add(title.trim().toLowerCase());
  if (mark === 'x') assert.match(title, /Nachweis:\s*\S+/i,
   'Completed task needs commit/test/review evidence: ' + id);
  rows.push({ n: Number(digits), id, done: mark === 'x', file });
  current++;
 }
 if (baseline.has(file)) assert.equal(current, baseline.get(file),
  file + ': baseline task count changed: append new tasks in a NEW numbered file');
}
assert.ok(rows.length >= 828, 'Baseline 828 tasks must not disappear');
const sorted = [...rows].sort((a, b) => a.n - b.n);
for (let i = 0; i < sorted.length; i++) {
 assert.equal(sorted[i].n, i + 1, 'Duplicate/missing task ID near ' + sorted[i].id);
}
const done = rows.filter(x => x.done).length;
const open = rows.length - done;
const pct = (100 * done / rows.length).toFixed(2);
console.log('KABELSALAT TASKS: ' + done + ' erledigt / ' + open +
 ' offen / ' + rows.length + ' gesamt (' + pct + '% abgeschlossen)');
for (const file of files) {
 const chunk = rows.filter(x => x.file === file);
 console.log('  ' + file + ': ' + chunk.filter(x => x.done).length +
 '/' + chunk.length + ' erledigt');
}
