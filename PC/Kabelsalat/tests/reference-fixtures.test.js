'use strict';
/* KAB-0833/0834/0037: immutable, reviewed source wall positions and endpoint IDs.
 * Do not regenerate the baseline as part of the test: it is a frozen fixture.
 */
const assert=require('node:assert/strict'),fs=require('node:fs'),path=require('node:path');
const comp=require('../components.js'),net=require('../network.js');
const refs=require('../reference-data.js').factory(comp,net);
globalThis.KabelsalatReferences=refs;
const examples=require('../examples.js').factory(comp,net);
const source=JSON.parse(fs.readFileSync(path.join(__dirname,'fixtures/reference-walls.json'),'utf8'));
assert.equal(source.cases.length,3);
const samples={
 '13_T_021_HTML':JSON.parse(fs.readFileSync(path.join(__dirname,'../examples/04_Referenz_13_T_021_aus_HTML.json'),'utf8')),
 '13_U_028_HTML':JSON.parse(fs.readFileSync(path.join(__dirname,'../examples/05_Referenz_13_U_028_aus_HTML.json'),'utf8')),
 '13_U_028_PDF_V2':examples.create('pdf-u028')
};
const pick=(project,kind)=>({
 walls:project.walls.map(w=>({
  id:w.id,holes:w.holes.map(h=>({id:h.id,u:h.u,v:h.v,dn:h.dn,
    sourceU:h.sourceU??null,sourceV:h.sourceV??null}))
 })),
 cables:project.cables.map(c=>({id:c.id,a:c.a||null,b:c.b||null}))
});
for(const fixture of source.cases){
 const project=samples[fixture.id];
 assert.ok(project,'Missing source for '+fixture.id);
 const expected={walls:fixture.walls,cables:fixture.cables};
 assert.deepEqual(pick(project,fixture.kind),expected,
  fixture.id+' changed source coordinates, wall assignment, pipe count or cable endpoints');
 assert.deepEqual(project.walls.map(w=>w.id),['SW1','SW2','SW3','SW4']);
 assert.ok(project.walls.every(w=>w.holes.every(h=>h.id.startsWith(w.id))),
  'Wall assignment must match pipe ID prefix');
}
assert.deepEqual(source.cases.map(c=>c.walls.map(w=>w.holes.length)),
 [[4,20,4,20],[25,12,29,12],[29,13,26,23]]);
assert.deepEqual(source.cases.map(c=>c.cables.length),[16,18,86]);
const pdf=samples['13_U_028_PDF_V2'];
assert.ok(pdf.walls.every(w=>w.holes.every(h=>h.u===h.sourceU&&h.v===h.sourceV)),
 'PDF data layer must store original vector centers, not compressed display positions');
const visual=require('../pdf-visual.js'),positions=require('../schematic-positions.js');
const referenceSvg=visual.render(pdf,'all'),compactProject=JSON.parse(JSON.stringify(pdf));
for(const wall of compactProject.walls)for(const h of wall.holes){
 h.u=positions.compact(h.sourceU);h.v=positions.compact(h.sourceV);
}
assert.equal(visual.render(compactProject,'all'),referenceSvg,
 'KAB-0050: source PDF wall images may not change when schematic 3D coordinates change');
assert.equal(visual.render(pdf,'all'),referenceSvg,'Rendering may not mutate source PDF imagery');
console.log('OK KAB-0833/0834/0037/0050: 48+78+91 locked pipes, 120 cable IDs and unmodified source-PDF wall images');
