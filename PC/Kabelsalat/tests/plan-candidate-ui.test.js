'use strict';
/* KAB-0881/M31/M32: read-only candidate table rendering from pasted plan TXT.
 * This is a DOM-mock unit test, NOT a PDF-binary import or a Chromium screenshot.
 */
const assert=require('node:assert/strict');
const fs=require('node:fs'),path=require('node:path'),vm=require('node:vm');
const dims=require('../pipe-dimensions.js');
const html=fs.readFileSync(path.resolve(__dirname,'../index.html'),'utf8');
const app=fs.readFileSync(path.resolve(__dirname,'../app.js'),'utf8');
assert.ok(html.includes('id="planDimensionCandidateRows"'));
const start="$('parsePlanDimensions').addEventListener('click',()=>{";
const begin=app.indexOf(start),end=app.indexOf('\n});',begin);
assert.ok(begin>=0&&end>begin,'Review handler must exist');
const handler=app.slice(begin+start.length,end);
const el=tag=>({tag,children:[],textContent:'',value:'',
 append(node){this.children.push(node)},replaceChildren(){this.children=[]}});
const ui={planDimensionText:{value:'SW2_T2_LR02 DN 110\nSW1_T1_LR03 DN 30\nSW1_T1_LR01 DN 160'},
 planDimensionSource:{value:'synthetisch-plan.txt'},
 planDimensionCandidates:el('select'),planDimensionCandidateRows:el('tbody'),planDimensionStatus:el('p')};
const context={pipeDimensions:dims,$:id=>ui[id],document:{createElement:el},planCandidates:[]};
vm.runInNewContext(handler,context,{timeout:2000});
assert.deepEqual(Array.from(context.planCandidates,c=>c.pipeId),
 ['SW1_T1_LR01','SW1_T1_LR03','SW2_T2_LR02']);
assert.equal(ui.planDimensionCandidateRows.children.length,3);
assert.equal(ui.planDimensionCandidates.children.length,3);
const row=ui.planDimensionCandidateRows.children[0];
assert.equal(row.children[0].textContent,'SW1_T1_LR01');
assert.equal(row.children[1].textContent,'160 mm');
assert.match(row.children[2].textContent,/synthetisch-plan\.txt · 1 · 3/);
assert.equal(row.children[3].textContent,'Unbestätigt');
assert.match(ui.planDimensionStatus.textContent,/3 Maßkandidaten/);
// All display strings use textContent; no HTML from untrusted plan labels.
assert.ok(handler.includes('cell.textContent=value'));
assert.ok(!handler.includes('innerHTML'));
// Contradictory labels must show a warning and never be applied silently.
ui.planDimensionText.value='SW1_T1_LR01 DN 30\\nSW1_T1_LR01 DN 160';
vm.runInNewContext(handler,context,{timeout:2000});
assert.equal(ui.planDimensionCandidateRows.children.length,2);
assert.ok(context.planCandidates.every(candidate=>candidate.conflict));
assert.equal(ui.planDimensionCandidateRows.children[0].children[3].textContent,'Konflikt');
assert.ok(app.includes('if(c.conflict&&!confirm('),
 'Conflicting DN candidate requires a separate affirmative manual choice');
console.log('OK: sorted plan candidates with source/page/line in text-only review table');
