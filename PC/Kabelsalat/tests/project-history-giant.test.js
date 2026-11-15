'use strict';
/* KAB-0415, 0419–0422, 0425–0429, 0432:
 * Exercise real app validator and real change/undoRedo source on synthetic projects.
 */
const assert=require('node:assert/strict'),fs=require('node:fs'),path=require('node:path');
const components=require('../components.js'),network=require('../network.js');
globalThis.KabelsalatComponents=components;
globalThis.KabelsalatNetwork=network;
globalThis.KabelsalatRoutePlanner=require('../network-route-planner.js');
globalThis.KabelsalatWorldRoute=require('../network-world-route.js');
globalThis.KabelsalatQuality=require('../quality.js');
globalThis.KabelsalatReports=require('../reports.js');
globalThis.KabelsalatReferences=require('../reference-data.js').factory(components,network);
globalThis.KabelsalatSamples=require('../examples.js').factory(components,network);
globalThis.KabelsalatPdfGeometry=require('../pdf-geometry.js');
globalThis.KabelsalatSchematicPositions=require('../schematic-positions.js');
globalThis.KabelsalatPipeDimensions=require('../pipe-dimensions.js');
globalThis.KabelsalatPhotoCandidates=require('../photo-candidates.js');
globalThis.KabelsalatConcreteWall=require('../concrete-wall.js');
globalThis.KabelsalatSpacingAdjust=require('../spacing-adjust.js');
globalThis.KabelsalatHoleEditorOps=require('../hole-editor-ops.js');
globalThis.KabelsalatCableEditorOps=require('../cable-editor-ops.js');
globalThis.KabelsalatConstructionV2=require('../construction-v2.js');
const app=fs.readFileSync(path.join(__dirname,'../app.js'),'utf8');
const before=app.split('let project;')[0].replace(/^\(\(\) => \{\s*/,'');
const {validProject}=new Function(before+'\nreturn {validProject};')();
const station=validProject(globalThis.KabelsalatSamples.create('station'));
// The running application synchronizes the active snapshot before autosave/Undo.
const active=station.network.shafts.find(x=>x.id===station.activeShaftId);
assert.ok(active);
active.snapshot=network.snapshot(station);
assert.equal(station.version,5);
assert.equal(station.network.shafts.length,4);
assert.deepEqual(validProject(JSON.parse(JSON.stringify(station))),station,
 'Current v5 must remain structurally identical under JSON read/write');
assert.equal(JSON.stringify(validProject(JSON.parse(JSON.stringify(station)))),
 JSON.stringify(station),'v5 must have deterministic JSON field ordering');
const originalIds=station.walls.flatMap(w=>w.holes.map(h=>w.id+'/'+h.id));
const savedCableIds=station.cables.map(c=>c.id);
const round=validProject(JSON.parse(JSON.stringify(station)));
assert.deepEqual(round.walls.flatMap(w=>w.holes.map(h=>w.id+'/'+h.id)),originalIds);
assert.deepEqual(round.cables.map(c=>c.id),savedCableIds);
assert.equal(round.network.shafts.length,4);
assert.deepEqual(round.network.shafts.map(s=>s.snapshot.cables.map(c=>c.id)),
 station.network.shafts.map(s=>s.snapshot.cables.map(c=>c.id)));
assert.ok(round.network.shafts.every(s=>s.snapshot.walls.length===4));
const marker=app.slice(app.indexOf("function change(fn,label='Planung geändert')"),
 app.indexOf('function fixSelection()'));
assert.ok(marker.startsWith("function change(")&&marker.includes('function undoRedo('));
assert.ok(marker.includes('undo.push(JSON.stringify(project))'));
assert.ok(marker.includes('redo.length=0'));
assert.ok(marker.includes('validProject(JSON.parse(from.pop()))'));
const source=[
 'let project=JSON.parse(JSON.stringify(seed)),undo=[],redo=[];',
 'const qualityModel=quality,validProject=normalizer;',
 'function clearWorldRoute(){}function clearNetworkPath(){}function resetSpacingPreview(){}',
 'function resetGroupPreview(){}function renderAll(){}function persist(){}function fixSelection(){}',
 'function syncActiveShaft(){',
 ' const shaft=project.network.shafts.find(x=>x.id===project.activeShaftId);',
 ' if(shaft)shaft.snapshot=netModel.snapshot(project);',
 '}',
 marker,
 'return {get project(){return project;},change,undoRedo,',
 ' get undoSize(){return undo.length;},get redoSize(){return redo.length;}};'
].join('\n');
const createHistory=new Function('seed','normalizer','netModel','quality',source);
const h=createHistory(station,validProject,network,globalThis.KabelsalatQuality);
const baseline=JSON.stringify(h.project);
const fresh={id:'FIKTIVES-ROHR',row:0,col:0,u:.12,v:.23,dn:50};
h.change(()=>h.project.walls[0].holes.push(fresh),'Neues fiktives Rohr');
assert.ok(h.project.walls[0].holes.some(p=>p.id==='FIKTIVES-ROHR'));
assert.equal(h.undoSize,1);
const afterHole=JSON.stringify(h.project);
h.undoRedo(true);
assert.equal(JSON.stringify(h.project),baseline,'Undo should restore original model exactly');
h.undoRedo(false);
const canonical=raw=>{const p=validProject(JSON.parse(raw));
 const sh=p.network.shafts.find(x=>x.id===p.activeShaftId);
 if(sh)sh.snapshot=network.snapshot(p);
 return JSON.stringify(p);
};
assert.equal(canonical(JSON.stringify(h.project)),canonical(afterHole),
 'Redo must restore exactly the same canonical engineering model');
const previousWaypoints=JSON.parse(JSON.stringify(h.project.cables[0].route));
h.change(()=>{h.project.cables[0].route=[[100,200,300]];},'Kabel-Wegpunkt gesetzt');
assert.deepEqual(h.project.cables[0].route,[[100,200,300]]);
const afterWaypoint=JSON.stringify(h.project);
h.undoRedo(true);assert.deepEqual(h.project.cables[0].route,previousWaypoints);
h.undoRedo(false);assert.equal(canonical(JSON.stringify(h.project)),canonical(afterWaypoint));
const oldThickness=h.project.enclosures.OLA.panels[0].thickness;
h.change(()=>h.project.enclosures.OLA.panels[0].thickness=oldThickness+4,'Plattendicke geändert');
assert.equal(h.project.enclosures.OLA.panels[0].thickness,oldThickness+4);
h.undoRedo(true);assert.equal(h.project.enclosures.OLA.panels[0].thickness,oldThickness);
h.undoRedo(false);assert.equal(h.project.enclosures.OLA.panels[0].thickness,oldThickness+4);
h.undoRedo(true);assert.ok(h.redoSize>0);
h.change(()=>{h.project.name='Neue Abzweigung';},'Neuer Zweig nach Undo');
assert.equal(h.redoSize,0,'A new change must discard the abandoned redo branch');
const snap1=components.snapshot(h.project);
const changed=JSON.parse(JSON.stringify(snap1));
changed.name='Variante 2';changed.dims.w+=100;
changed.cables=changed.cables.slice(1);
const variants=components.normalizeVariants([
 {id:'V1',name:'Original',snapshot:snap1},{id:'V2',name:'Abzweig',snapshot:changed}]);
assert.equal(variants.length,2);
const diff=components.differences(variants[0].snapshot,variants[1].snapshot);
assert.ok(diff.some(x=>x.includes('breite'))&&diff.some(x=>x.includes('kabel')));
assert.equal(variants[0].name,'Original');
const doc=fs.readFileSync(path.join(__dirname,'../TASKS/PROJECT_FORMAT_V5.md'),'utf8');
assert.ok(doc.includes('Projektformat v5')&&doc.includes('Migrationspfad'));
assert.ok(app.includes("if(grab?.type==='waypoint')"));
assert.ok(app.includes("if(!grab.changed){undo.push(JSON.stringify(project))"));
assert.ok(app.includes("$('undo').addEventListener('click',()=>undoRedo(true))"));
assert.ok(app.includes("$('redo').addEventListener('click',()=>undoRedo(false))"));
assert.ok(app.includes("catch(_){$('saveStatus').textContent='⚠ Speichern fehlgeschlagen'"));
console.log('PROJECT HISTORY GIANT OK: real app v5 validator, 4-shaft IDs and snapshots, exact undo/redo, branch clear, route and panel change, variant diff');
