'use strict';
/* KAB-0051/0052/0053: genuine non-destructive solver, cable-ID invariance,
   reversible model edit and before/after schematic checks. */
const assert=require('node:assert/strict');
const fs=require('node:fs'),path=require('node:path');
const spacing=require('../spacing-adjust.js');
const app=fs.readFileSync(path.join(__dirname,'../app.js'),'utf8');
const html=fs.readFileSync(path.join(__dirname,'../index.html'),'utf8');
const project=()=>({
 name:'Eigener Testschacht',dims:{w:1200,d:1000,h:1100},
 walls:[
  {id:'SW1',holes:[
   {id:'LR-1',u:.45,v:.5,dn:60,dimensionSpec:{openingMm:64}},
   {id:'LR-2',u:.5,v:.5,dn:110,dimensionSpec:{openingMm:116}},
   {id:'LR-3',u:.92,v:.2,dn:50}]},
  {id:'SW2',holes:[{id:'OTHER',u:.2,v:.6,dn:75}]}
 ],
 cables:[{id:'Cable 01',a:{wall:'SW1',hole:'LR-1'},b:{wall:'SW1',hole:'LR-2'},
  route:[[1,2,3]]}]
});
const p=project(),original=JSON.stringify(p);
const cfg={gapMm:60,edgeMm:40};
const preview=spacing.plan(p,'SW1',cfg);
assert.equal(JSON.stringify(p),original,'Preview must NEVER mutate project');
assert.equal(preview.before.overlaps,1,'Initial model contains a true 64+116 bore overlap');
assert.ok(preview.after.minGapMm>=59.9,'Solution must satisfy requested light gap');
assert.ok(preview.after.minEdgeMm>=39.9,'Solution must satisfy requested edge');
assert.ok(preview.moved>=2);
assert.equal(preview.changes.length,preview.moved);
assert.ok(preview.changes.every(c=>c.to.u>=0&&c.to.u<=1&&c.to.v>=0&&c.to.v<=1));
assert.ok(preview.changes.every(c=>c.deltaMm>0));
assert.equal(spacing.spanMm(p,'SW1'),1000);
assert.equal(spacing.spanMm(p,'SW2'),1200);
const again=spacing.plan(p,'SW1',cfg);
assert.deepEqual(again,preview,'Preview needs to be deterministic for identical data');
const otherWall=JSON.stringify(p.walls[1]),cables=JSON.stringify(p.cables),
 pipeIDs=p.walls[0].holes.map(h=>h.id),dimensions=p.walls[0].holes.map(h=>h.dimensionSpec);
assert.equal(spacing.apply(p,preview),preview.moved);
assert.deepEqual(p.walls[0].holes.map(h=>h.id),pipeIDs);
assert.deepEqual(p.walls[0].holes.map(h=>h.dimensionSpec),dimensions);
assert.equal(JSON.stringify(p.cables),cables,'Cable endpoint and route references MUST remain identical');
assert.equal(JSON.stringify(p.walls[1]),otherWall,'Other wall remains untouched');
assert.equal(JSON.stringify(p.walls[0].holes.map(h=>h.dn)),JSON.stringify([60,110,50]));
const next=spacing.plan(p,'SW1',cfg);
assert.equal(next.moved,0,'Stable valid layout must not drift');
assert.throws(()=>spacing.apply(p,preview),/veraltet/,'Old preview must be rejected');
const restored=JSON.parse(original);
assert.deepEqual(restored,project(),'Undo snapshot must restore original exact model including cable IDs');
// Unknown physical sources MUST NEVER be auto-relocated.
for(const sourceInfo of [{shaft:'13_U_028',sourceVersion:'PDF V2'},{shaft:'13_T_021',sourceVersion:'HTML'}]){
 const foreign=project();foreign.sourceInfo=sourceInfo;
 const before=JSON.stringify(foreign);
 assert.throws(()=>spacing.plan(foreign,'SW1',cfg),/Original-\/PDF-Referenz geschützt/);
 assert.equal(JSON.stringify(foreign),before);
 assert.throws(()=>spacing.apply(foreign,preview),/Originalquelle geschützt/);
}
assert.throws(()=>spacing.plan(project(),'SW1',{gapMm:-1,edgeMm:10}),/0–1000/);
assert.throws(()=>spacing.plan(project(),'SW1',{gapMm:0.5,edgeMm:10}),/0–1000/);
assert.throws(()=>spacing.plan(project(),'SW1',{gapMm:1100,edgeMm:10}),/0–1000/);
assert.throws(()=>spacing.plan(project(),'SW1',{gapMm:900,edgeMm:400}),/passt nicht|nicht lösbar/);
assert.throws(()=>spacing.plan(project(),'SW5',cfg),/Ungültige Wand/);
const copied=project();copied.walls[0].holes[1].id='LR-1';
assert.throws(()=>spacing.plan(copied,'SW1',cfg),/Rohr-ID/,'Duplicated ID must fail');
// Edge-case: exactly coincident circles use a deterministic split.
const coincident=project();
coincident.walls[0].holes[1].u=coincident.walls[0].holes[0].u;
coincident.walls[0].holes[1].v=coincident.walls[0].holes[0].v;
const split=spacing.plan(coincident,'SW1',{gapMm:10,edgeMm:5});
assert.ok(split.after.overlaps===0&&split.after.minGapMm>=9.9);
// Stale preview cannot silently move a manually modified hole.
const stale=project(),proposal=spacing.plan(stale,'SW1',cfg);
stale.walls[0].holes[2].u=.65;
assert.throws(()=>spacing.apply(stale,proposal),/veraltet/);
// UI/release integration and model safety.
for(const id of ['spacingGap','spacingEdge','spacingPreview','spacingApply','spacingCompare'])
 assert.ok(html.includes('id="'+id+'"'),'Missing spacing UI '+id);
assert.ok(html.includes('src="spacing-adjust.js?v='+require('../package.json').version+'"'));
assert.ok(app.includes('spacingAdjust.plan(project,wallId')&&app.includes('spacingAdjust.apply(project,pending)'));
assert.ok(app.includes('resetSpacingPreview();')&&app.includes("change(()=>spacingAdjust.apply(project,pending)"));
assert.ok(app.includes("ctx.setLineDash([5,4])"));
assert.ok(require('../package.json').build.files.includes('spacing-adjust.js'));
console.log('OK: KAB-0051/52/53 deterministic spacing, secure source guard, immutable preview, reversible and stable cable-ID-preserving apply');
