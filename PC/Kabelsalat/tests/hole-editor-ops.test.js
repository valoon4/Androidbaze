'use strict';
/* KAB-0235–0250: pure 2D editor transactions, nominal vs physical dimensions.
 * Synthesized fixtures are NOT approved field measurements.
 */
const assert=require('node:assert/strict');
const ops=require('../hole-editor-ops.js');
const specs=require('../pipe-dimensions.js');
const fs=require('node:fs'),path=require('node:path');
const html=fs.readFileSync(path.resolve(__dirname,'../index.html'),'utf8');
const app=fs.readFileSync(path.resolve(__dirname,'../app.js'),'utf8');
const project=()=>({dims:{w:2400,d:1900,h:2100},walls:[
 {id:'SW1',holes:[{id:'A',u:.2,v:.5,dn:50},{id:'B',u:.5,v:.5,dn:110}]},
 {id:'SW2',holes:[{id:'OTHER',u:.7,v:.5,dn:75}]},
 {id:'SW3',holes:[]},{id:'SW4',holes:[]}],
 cables:[{id:'C1',a:{wall:'SW1',hole:'A'},b:{wall:'SW2',hole:'OTHER'},route:[[10,20,30]]}]});
const p=project(),original=JSON.stringify(p);
assert.equal(ops.span(p,'SW1'),1900);
assert.equal(ops.span(p,'SW2'),2400);
const pdf=project();pdf.sourceInfo={sourceVersion:'PDF V2 (Konzept)'};
assert.equal(ops.span(pdf,'SW1'),2400);
assert.equal(ops.span(pdf,'SW2'),1900);
assert.deepEqual(ops.COMMON_DN,[30,40,50,60,63,75,90,110,116,125,140,160]);
assert.equal(ops.snapMm(123,1),123);
assert.equal(ops.snapMm(123,25),125);
assert.equal(ops.snapMm(111,10),110);
assert.throws(()=>ops.snapMm(Infinity),/Fangraster/);
assert.throws(()=>ops.snapMm(100,0),/Fangraster/);
assert.equal(ops.uniqueId(p,'SW1'),'SW1-free-0001');
p.walls[0].holes.push({id:'SW1-free-0001',u:.8,v:.8,dn:50});
assert.equal(ops.uniqueId(p,'SW1'),'SW1-free-0002');
p.walls[0].holes.pop();
const clear=ops.candidate(p,'SW1',{xMm:500,yMm:350,dn:50});
assert.equal(clear.ok,true);
assert.equal(clear.warnings.length,0);
assert.ok(Math.abs(clear.u-500/1900)<1e-10);
const collision=ops.candidate(p,'SW1',{xMm:390,yMm:1050,dn:110});
assert.equal(collision.ok,true);
assert.ok(collision.warnings.some(s=>s.includes('A')));
const edge=ops.candidate(p,'SW1',{xMm:5,yMm:5,dn:110});
assert.equal(edge.ok,false);
assert.match(edge.errors[0],/außerhalb/);
assert.throws(()=>ops.candidate(p,'SW1',{xMm:NaN,yMm:500,dn:50}),/ungültig/i);
assert.throws(()=>ops.candidate(p,'SW9',{xMm:100,yMm:100,dn:50}),/Wand-ID/);
const spec=specs.normalize({nominalMm:110,outerMm:125.5,innerMm:109.5,
 wallThicknessMm:8,openingMm:140,source:'Lieferant',reviewer:'Prüfer',
 status:'reviewed',method:'manual'},110);
assert.equal(spec.wallThicknessMm,8);
assert.equal(spec.outerMm,125.5);
assert.equal(spec.nominalMm,110);
const afterSave=specs.forHole({dn:110,dimensionSpec:spec});
assert.equal(afterSave.wallThicknessMm,8);
assert.match(specs.describe({dn:110,dimensionSpec:spec}),/Rohrwand 8 mm/);
assert.equal(specs.normalize({wallThicknessMm:'',status:'unverified'},50).wallThicknessMm,null);
assert.equal(specs.normalize({wallThicknessMm:999},50).wallThicknessMm,null);
const copy=ops.copySpec({dimensionSpec:spec});
assert.equal(copy.wallThicknessMm,8);
assert.equal(copy.status,'unverified');
assert.equal(copy.source,'');
assert.equal(copy.reviewer,'');
const beforeGroup=JSON.stringify(p);
const group=ops.planGroup(p,'SW3',{xMm:250,yMm:250,rows:2,cols:3,
 dxMm:200,dyMm:220,dns:[30,50,110,160]});
assert.equal(group.count,6);
assert.deepEqual(group.holes.map(h=>h.dn),[30,50,110,160,30,50]);
assert.equal(new Set(group.holes.map(h=>h.id)).size,6);
assert.equal(JSON.stringify(p),beforeGroup,'Planning cannot mutate project');
assert.equal(ops.applyGroup(p,group),6);
assert.equal(p.walls[2].holes.length,6);
assert.equal(JSON.stringify(p.cables),JSON.stringify(project().cables));
assert.throws(()=>ops.applyGroup(p,group),/veraltet/);
const otherGroup=ops.planGroup(project(),'SW4',{xMm:120,yMm:120,rows:1,cols:2,dxMm:200,dyMm:200,dns:[50,110]});
assert.equal(otherGroup.count,2);
assert.throws(()=>ops.planGroup(project(),'SW3',{xMm:5,yMm:5,dns:[160]}),/außerhalb/);
assert.throws(()=>ops.planGroup(project(),'SW3',{xMm:100,yMm:100,rows:20,cols:20}),/ungültig/);
assert.throws(()=>ops.planGroup(pdf,'SW3',{xMm:200,yMm:200}),/Referenzquelle/);
const overlapPlan=ops.planGroup(project(),'SW3',{xMm:300,yMm:300,rows:1,cols:2,
 dxMm:10,dyMm:100,dns:[110]});
assert.ok(overlapPlan.warnings.length>0,'Group collision must be explicit');
const transferProject=project(),snap=JSON.stringify(transferProject);
const transfer=ops.planTransfer(transferProject,'SW1','A','SW3');
assert.deepEqual(transfer.connected,['C1']);
assert.equal(JSON.stringify(transferProject),snap);
assert.throws(()=>ops.applyTransfer(transferProject,transfer),/bestätigt/);
assert.equal(JSON.stringify(transferProject),snap);
assert.equal(ops.applyTransfer(transferProject,transfer,{approveConnected:true}), 'A');
assert.equal(transferProject.walls[0].holes.some(h=>h.id==='A'),false);
assert.equal(transferProject.walls[2].holes.some(h=>h.id==='A'),true);
assert.deepEqual(transferProject.cables[0].a,{wall:'SW3',hole:'A'});
assert.equal(transferProject.cables[0].id,'C1');
assert.throws(()=>ops.applyTransfer(transferProject,transfer,{approveConnected:true}),/veraltet/);
assert.throws(()=>ops.planTransfer(pdf,'SW1','A','SW3'),/Referenzquelle/);
const conflictingIds=project();
conflictingIds.walls[2].holes.push({id:'A',u:.8,v:.8,dn:75});
const conflictingBefore=JSON.stringify(conflictingIds);
assert.throws(()=>ops.planTransfer(conflictingIds,'SW1','A','SW3'),/Rohr-ID existiert/);
assert.equal(JSON.stringify(conflictingIds),conflictingBefore,'ID conflict must not mutate either wall');
assert.match(app,/wallCoords\(w\.id,p,false\)/,
 '3D dragging must inspect unclamped wall positions');
assert.ok(app.includes('3D-Verschieben blockiert:')&&app.includes('resetGroupPreview();'),
 '3D drag must show geometry collision errors and invalidate stale batch proposals');

const invalidWall=project();invalidWall.walls[2].holes.push({id:'BLOCK',u:.2,v:.5,dn:160});
const blocked=ops.planTransfer(invalidWall,'SW1','A','SW3');
assert.ok(blocked.warnings.some(s=>s.includes('BLOCK')));
assert.throws(()=>ops.applyTransfer(invalidWall,blocked,{approveConnected:true}),/Zielwand-Konflikt/);
const transferSnap=JSON.stringify(invalidWall);
assert.equal(JSON.stringify(invalidWall),transferSnap);
assert.equal(JSON.stringify(project()),original);
for(const id of ['holeDnSuggestions','holeWallMm','draftSnapStep','draftNewDn',
 'groupStartX','groupStartY','groupRows','groupCols','groupDx','groupDy',
 'groupDns','groupPreview','groupApply','holeTransferWall','holeTransfer',
 'holeEditStatus','transferStatus','groupStatus'])
 assert.ok(html.includes('id="'+id+'"'),'Missing editor field: '+id);
assert.ok(html.includes('src="hole-editor-ops.js?v='+require('../package.json').version+'"'));
assert.match(app,/KabelsalatHoleEditorOps/,'Hole editor must be attached to app');
console.log('OK: hole-edit pure batch/group, copy, DN, dimensions, safety, transfer, snap and UI regressions');
