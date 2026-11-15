'use strict';
/* KAB-0487..0504 / 0602..0609: source metadata never manufactures a survey or approval. */
const assert=require('node:assert/strict'),fs=require('node:fs'),path=require('node:path');
const quality=require('../quality.js'),reports=require('../reports.js');
const components=require('../components.js'),network=require('../network.js');
globalThis.KabelsalatComponents=components;
globalThis.KabelsalatNetwork=network;
globalThis.KabelsalatRoutePlanner=require('../network-route-planner.js');
globalThis.KabelsalatWorldRoute=require('../network-world-route.js');
globalThis.KabelsalatQuality=quality;
globalThis.KabelsalatReports=reports;
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
const html=fs.readFileSync(path.join(__dirname,'../index.html'),'utf8');
const validProject=new Function(app.split('let project;')[0].replace(/^\(\(\) => \{\s*/,'')+
 '\nreturn validProject;')();
const template=globalThis.KabelsalatSamples.create('reference');
const p=validProject(template),shaftId=p.activeShaftId,hole=p.walls[0].holes[0],cable=p.cables[0];
const baseline=JSON.stringify(p);
const documentA={id:'PDF-42',title:'Originalplan 42',version:'R3',
 kind:'pdf',file:'plan-42.pdf',date:'2026-10-01',pages:[2,4,7]};
const documentB={id:'AUFM-42',title:'Fiktives Kontrollaufmaß',
 version:'2026-11',kind:'survey',pages:[3]};
let registry=quality.addSourceDocument(null,documentA);
registry=quality.addSourceDocument(registry,documentB);
assert.equal(registry.documents.length,2);
assert.equal(registry.entries.length,0);
assert.ok(registry.documents.every(d=>d.id&&d.version&&d.title));
assert.equal(registry.documents[0].pages.length,3);
assert.equal(quality.sourceSearch(registry,'R3').length,1);
assert.equal(quality.sourceSearch(registry,'4').length,2,'Page/version search covers all fields');
assert.throws(()=>quality.addSourceDocument(registry,documentA),/Doppelte Quell-ID/);
assert.throws(()=>quality.addSourceDocument(registry,{id:'x',title:'no version'}),/Version/);
const unreviewed={shaftId,entity:'hole',objectId:hole.id,field:'openingMm',
 ref:{documentId:documentA.id,page:4,region:'SW1/T1',basis:'graphic',
  evidence:'Kreis mit der Maus abgelesen',verified:true,
  checkedBy:'fixture',checkedAt:'2026-11-15T09:00:00Z'}};
const originalRegistry=JSON.stringify(registry);
registry=quality.attachSource(registry,unreviewed);
assert.equal(registry.entries.length,1);
assert.equal(registry.entries[0].ref.verified,false,
 'Unscaled graphic cannot become a verified diameter even if verified=true');
assert.equal(registry.entries[0].status,'unverified');
assert.equal(originalRegistry,JSON.stringify(quality.normalizeSources({
 documents:[documentA,documentB],entries:[],conflicts:[],markers:[]})));
assert.throws(()=>quality.attachSource(registry,unreviewed),/bereits belegt/);
const survey={shaftId,entity:'hole',objectId:hole.id,field:'nominalMm',
 ref:{documentId:documentB.id,page:3,basis:'survey',evidence:'Fiktive Prüfmessung 80 mm',
 measuredValue:80,unit:'mm',checkedBy:'Fixture Prüfer',
 checkedAt:'2026-11-15T10:00:00Z',verified:true}};
registry=quality.attachSource(registry,survey);
assert.equal(registry.entries[1].status,'survey-reviewed');
assert.equal(registry.entries[1].ref.verified,true);
const cableRef={shaftId,entity:'end-a',objectId:cable.id,field:'connection',
 ref:{documentId:documentA.id,page:7,region:'T1',basis:'label',
 evidence:'SW1 / Anschluss A'}};
registry=quality.attachSource(registry,cableRef);
assert.equal(registry.entries.length,3);
assert.equal(registry.entries[2].ref.page,7);
assert.throws(()=>quality.attachSource(registry,{...cableRef,ref:{
 ...cableRef.ref,documentId:'MISSING'}}),/nicht im Register/);
assert.throws(()=>quality.sourceEntry({...cableRef,entity:'evil'}),/ungültig/);
assert.equal(quality.sourceRef({documentId:'X',page:12}).verified,false);
const trackedNet={...p.network,sources:registry};
const coverage=quality.sourceCoverage(trackedNet);
assert.equal(coverage.lost.length,0);
assert.equal(coverage.unverified,2);
assert.equal(coverage.conflictsOpen,0);
const moved=quality.attachSource(registry,{...cableRef,objectId:'NO-LONGER-EXISTS',
 field:'bad cable ref'});
assert.equal(quality.sourceCoverage({...p.network,sources:moved}).lost.length,1);
const conflict={id:'CONFLICT-42',objectId:hole.id,field:'openingMm',
 left:{documentId:'PDF-42',page:4,basis:'graphic',evidence:'75'},
 right:{documentId:'AUFM-42',page:3,basis:'survey',evidence:'80'}};
registry=quality.sourceConflict(registry,conflict);
assert.equal(registry.conflicts[0].status,'open');
assert.equal(quality.sourceCoverage({...p.network,sources:registry}).conflictsOpen,1);
assert.throws(()=>quality.decideSourceConflict(registry,'CONFLICT-42',{
 decision:'Aufmaß',decidedBy:'Reviewer'}),/Zeit/);
registry=quality.decideSourceConflict(registry,'CONFLICT-42',{
 decision:'Unabhängige Vor-Ort-Begehung erforderlich',
 decidedBy:'Fixture Prüfer',decidedAt:'2026-11-15T12:30:00Z'});
assert.equal(registry.conflicts[0].status,'resolved');
assert.equal(registry.conflicts[0].left.documentId,'PDF-42');
assert.equal(registry.conflicts[0].right.documentId,'AUFM-42');
assert.equal(quality.sourceCoverage({...p.network,sources:registry}).conflictsOpen,0);
registry.markers.push({id:'M-1',documentId:'PDF-42',page:7,wallId:'SW1',
 label:'Unklare Öffnung',status:'unmatched'});
assert.equal(quality.normalizeSources(registry).markers[0].status,'unmatched');
p.network.sources=registry;
const normalized=validProject(p);
assert.deepEqual(normalized.network.sources,quality.normalizeSources(registry));
assert.deepEqual(validProject(JSON.parse(JSON.stringify(normalized))),normalized,
 'Sources, versions, pages, conflict decision and unmatched markers survive v5 roundtrip');
assert.equal(validProject(template).network.sources,undefined,
 'Legacy projects do not gain fictional source registers');
const qualityResult=quality.evaluate(normalized.network,normalized.network.quality);
const report=reports.reportHTML(normalized,qualityResult);
for(const content of ['Originalplan 42','AUFM-42','2026-11','Quellwidersprüche',
 'CONFLICT-42','Wandöffnungen und Belegung'])assert.ok(report.includes(content),content);
const hostile=JSON.parse(JSON.stringify(normalized));
hostile.network.sources.documents[0].title='<img src=x onerror=alert(1)>';
const safe=reports.reportHTML(hostile,qualityResult);
assert.ok(safe.includes('&lt;img src=x onerror=alert(1)&gt;'));
assert.ok(!safe.includes('<img src=x onerror=alert(1)>'));
const noSource=validProject(template);
const missingSource=reports.reportHTML(noSource,quality.evaluate(noSource.network,{}));
assert.ok(missingSource.includes('Quellenliste fehlt'));
const holeCopy=JSON.parse(JSON.stringify(normalized));holeCopy.walls[0].holes[0].openingType='bare-hole';
holeCopy.walls[0].holes[0].dimensionSpec={openingMm:75,status:'unverified'};
holeCopy.network.shafts[0].snapshot=network.snapshot(holeCopy);
const bareReport=reports.reportHTML(holeCopy,qualityResult);
assert.ok(bareReport.includes('Nur Betonloch'));
assert.ok(bareReport.includes('Nicht anwendbar'));
assert.ok(bareReport.includes('75'));
const viewer=reports.viewerHTML(holeCopy);
assert.ok(viewer.includes('Nur Betonloch, Ø'));
for(const id of ['sourceDocId','sourceDocVersion','sourceDocTitle','sourceDocKind',
 'sourceAddDocument','sourceDocumentRef','sourceTarget','sourceField','sourcePage',
 'sourceRegion','sourceBasis','sourceEvidence','sourceAttachObject','sourceRegistryList'])
 assert.ok(html.includes('id="'+id+'"'),'Missing source register UI: '+id);
assert.ok(app.includes('qualityModel.sourceCoverage(net)'));
assert.ok(app.includes('qualityModel.addSourceDocument('));
assert.ok(app.includes('qualityModel.attachSource('));
assert.ok(app.includes('result.network.sources=qualityModel.normalizeSources'));
assert.equal(JSON.stringify(template),JSON.stringify(globalThis.KabelsalatSamples.create('reference')));
console.log('SOURCE PROVENANCE GIANT OK: 18 source controls, measured vs graphic, page/version, conflicts, orphan detection and export XSS');
