'use strict';
// KAB-0203/0204/0205/0206: four source-faithful, view-only pitch comparisons.
// Never assert that PDF drawing-marker positions represent measured pipe apertures.
const assert=require('node:assert/strict');
const fs=require('node:fs'),path=require('node:path');
const components=require('../components.js');
const network=require('../network.js');
const positions=require('../schematic-positions.js');
const reference=require('../reference-data.js').factory(components,network);
const pitch=require('../pitch-wall-compare.js');
const project=reference.create('pdf-u028'),before=JSON.stringify(project);
const groups=pitch.compareAll(project,positions);
assert.deepEqual(groups.map(w=>w.wallId),['SW1','SW2','SW3','SW4']);
assert.deepEqual(groups.map(w=>w.count),[29,13,26,23]);
assert.equal(groups.reduce((sum,w)=>sum+w.count,0),91);
const keys=new Set();
for(const wall of groups){
 assert.equal(wall.sourceType,'PDF V2 · Quellmarker (keine Vermessung)');
 assert.ok(wall.demoType.includes('keine neuen Rohrpositionen'));
 assert.equal(wall.source.length,wall.demo.length);
 const sourceIDs=wall.source.map(h=>h.id),demoIDs=wall.demo.map(h=>h.id);
 assert.deepEqual(demoIDs,sourceIDs,'IDs must never change or reorder');
 for(let i=0;i<wall.count;i++){
  const a=wall.source[i],b=wall.demo[i];
  assert.ok(!keys.has(a.id),'91 source IDs must be globally unique');
  keys.add(a.id);
  assert.equal(a.u,project.walls.find(w=>w.id===wall.wallId).holes.find(h=>h.id===a.id).sourceU);
  assert.equal(a.v,project.walls.find(w=>w.id===wall.wallId).holes.find(h=>h.id===a.id).sourceV);
  assert.equal(b.u,positions.compact(a.u));
  assert.equal(b.v,positions.compact(a.v));
  assert.equal(b.id,a.id);
  assert.equal(b.sourcePage,a.sourcePage);
  assert.equal(b.sourceAddress,a.sourceAddress);
 }
 for(const [records,preview] of [[wall.source,false],[wall.demo,true]]){
  const image=pitch.svg(records,{wallId:wall.wallId,preview});
  assert.match(image,/<svg /);
  assert.equal((image.match(/<circle /g)||[]).length,wall.count,
   'Exactly one marker per ID, no fake additional physical openings');
  assert.equal((image.match(/<title>/g)||[]).length,wall.count);
  assert.ok(image.includes('nicht vermessen')||image.includes('NICHT vermessen'));
  assert.ok(image.includes('aria-label="'+wall.wallId));
 }
}
assert.equal(keys.size,91);
assert.equal(JSON.stringify(project),before,'Entire PDF source/geometry/cables must remain unchanged');
// Historical demo project with altered positions must NOT be presented as original.
const tampered=JSON.parse(before);
tampered.walls[0].holes[0].u=.5;
assert.throws(()=>pitch.compareAll(tampered,positions),/Veränderte PDF-Quelle/);
const swapped=JSON.parse(before);
swapped.walls[0].holes[1].id=swapped.walls[0].holes[0].id;
assert.throws(()=>pitch.compareAll(swapped,positions),/doppelte/);
const missing=JSON.parse(before);
missing.walls[2].holes.pop();
assert.throws(()=>pitch.compareAll(missing,positions),/Quelle unvollständig/);
const notPdf=JSON.parse(before);notPdf.sourceInfo.sourceVersion='HTML';
assert.throws(()=>pitch.compareAll(notPdf,positions),/PDF-V2-Referenz/);
assert.throws(()=>pitch.compareWall(project,'SW5',positions),/Unbekannte Schachtwand/);
assert.throws(()=>pitch.compareWall(project,'SW1',{FACTOR:.75,display:positions.display}),/62-Prozent/);
// Untrusted source-address/id strings MUST be escaped in inline SVG.
const malicious=pitch.svg([{id:'<svg onload="alert(1)">',sourceAddress:'evil & "quote"',u:.3,v:.4}],{wallId:'SW1'});
assert.ok(!malicious.includes('<svg onload='));
assert.ok(malicious.includes('&lt;svg onload='));
assert.ok(malicious.includes('evil &amp; &quot;quote&quot;'));
const html=fs.readFileSync(path.join(__dirname,'../pitch-walls.html'),'utf8');
const start=fs.readFileSync(path.join(__dirname,'../pitch.html'),'utf8');
const version=require('../package.json').version;
assert.ok(start.includes('href="pitch-walls.html"'),'Pitch launch must link to comparison');
assert.ok(start.includes('Pitch-Sicherheitsstart · v'+version),
 'Pitch start must display the same real version as the published app');
assert.ok(html.includes('<meta name="kabelsalat-version" content="'+version+'">'));
assert.ok(html.includes('src="pitch-wall-compare.js?v='+version+'"'));
assert.ok(html.includes("KabelsalatPitchWalls.compareAll(source,window.KabelsalatSchematicPositions)"));
for(const id of ['SW1','SW2','SW3','SW4'])
 assert.ok(html.includes('SW'+id.slice(2)),'Wall title missing: '+id);
assert.equal(JSON.stringify(project),before);
console.log('PITCH WALLS OK: SW1=29 SW2=13 SW3=26 SW4=23, all 91 PDF IDs, source-vs-62%-view, no engineering changes');
