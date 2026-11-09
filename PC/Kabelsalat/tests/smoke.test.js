'use strict';
const assert=require('node:assert/strict'),fs=require('node:fs'),path=require('node:path');
globalThis.KabelsalatPdfGeometry=require('../pdf-geometry.js');
const app=fs.readFileSync(path.resolve(__dirname,'../app.js'),'utf8');
const html=fs.readFileSync(path.resolve(__dirname,'../index.html'),'utf8');
assert.ok(html.includes('Kabelsalat'), 'UI must display Kabelsalat brand');
for(const asset of ['recovery.js','app.js','vendor/three.min.js'])
 assert.ok(html.includes('src="'+asset+'?v=1.9.2-beta.1"'),'Cache-busted script missing: '+asset);
for(const id of ['startupRecovery','reloadFresh','resetDisplay','recoveryReason'])
 assert.ok(html.includes('id="'+id+'"'),'Recovery interface missing: '+id);
const recovery=fs.readFileSync(path.resolve(__dirname,'../recovery.js'),'utf8');
assert.ok(recovery.includes('kabelsalat_sidebar_layout_v1')&&recovery.includes('kabelsalat_ui_mode_v1'),
 'Display reset must only clear UI preferences');
assert.ok(!recovery.includes("removeItem('eugi.schachtwand.3d.v1')"),
 'Display reset must not touch saved projects');
for(const asset of ['pdf-geometry.js','pdf-visual.js'])
 assert.ok(html.includes('src="'+asset+'?v=1.9.2-beta.1"'),
  'New visual source asset missing: '+asset);
for(const id of ['pdfVisualPanel','pdfOpen','pdfClose','oldProjectNotice','switchToPdf'])
 assert.ok(html.includes('id="'+id+'"'),'PDF visual UI missing: '+id);
assert.ok(app.includes('pdfGeometry.hole(wid,h,s)')&&
 app.includes('pdfGeometry.transform(w.id,s)')&&
 app.includes('pdfGeometry.camera(viewMode,d)'),
 '3D must use correct PDF-world wall coordinates on every view');
assert.ok(app.includes('new THREE.CylinderGeometry(r+.010,r+.010,.20'),
 'DN pipes must be visually modelled, not only described as text');
assert.ok(app.includes('cableDisplayPoint(c.a,c)')&&app.includes("pdfView()?.94"),
 'Kabel strands must be visible and individually fanned out');
assert.ok(app.includes("oldProjectNotice').hidden=pdfView()"),
 'Older browser-saved shaft projects must be visibly identified');
assert.ok(app.includes("document.body.classList.add('app-ready')"),
 '3D app must mark successful boot');
assert.ok(html.includes('id="draftCanvas"'), 'Parallel 2D canvas missing');
assert.ok(html.includes('id="splitEnabled"'), '2D toggle missing');
assert.ok(html.includes('src="editor2d.js?v=1.9.2-beta.1"'), '2D module not loaded');
assert.ok(html.includes('src="components.js?v=1.9.2-beta.1"'), 'Components module not loaded');
assert.ok(html.includes('src="network.js?v=1.9.2-beta.1"'), 'Network module not loaded');
assert.ok(html.includes('src="quality.js?v=1.9.2-beta.1"'), 'Quality module not loaded');
assert.ok(html.includes('src="reports.js?v=1.9.2-beta.1"'), 'Report module not loaded');
assert.ok(html.includes('src="examples.js?v=1.9.2-beta.1"'), 'Examples module not loaded');
assert.ok(html.includes('src="reference-data.js?v=1.9.2-beta.1"'), 'Reference data module not loaded');
assert.ok(html.includes('id="exampleSelect"'), 'Example selector missing');
assert.ok(html.includes('id="loadExample"'), 'Example action missing');
for(const id of ['toggleExpert','exploreGuide','questNetwork','questShaft','questCable',
 'exploreTip','exploreSceneOverlay','explore2DToggle','exploreSelectionBody','cablesOverview'])
 assert.ok(html.includes('id="'+id+'"'),'Basic-Mode UI missing: '+id);
assert.ok(html.includes('body.explore-mode .sidebar.right'), 'Expert panels are not progressively hidden');
assert.ok(app.includes('EXPLORE_MODE_KEY'), 'Mode preference must be independent of project');
assert.ok(app.includes('function openQuest(which)'), 'Guided quests must be wired');
assert.ok(app.includes('function renderExplorerUI()'), 'Selection explanation must be live');
assert.ok(app.includes("exploreConstructionMode==='hidden'?.34:.76"),
 'Explorer needs saturated cables when showing a translucent construction');
assert.ok(html.includes('id="exploreConstructionMode"'), 'Presentation button missing');
assert.ok(html.includes('id="exploreConstructionNote"'), 'Presentation state explanation missing');
for(const id of ['pdfWallSection','pdfWallViews','pdfWallContext','loadPdfV2'])
 assert.ok(html.includes('id="'+id+'"'),'PDF 1.6 UI missing: '+id);
assert.ok(app.includes("new THREE.TorusGeometry(r,exploreMode?.006:.004,6,32,Math.max(.005,arc-.025))"),
 '3D multi-trade ring segments not wired');
assert.ok(app.includes("ctx.arc(p.x,p.y,r+2,cursor,cursor+arc-.025)"),
 '2D multi-trade ring segments not wired');
assert.ok(app.includes('cableDisplayPoint(c.a,c)')&&app.includes('cableDisplayPoint(c.b,c)'),
 'Multiple PDF cables must remain individually visible');
assert.ok(app.includes('PDF_WALLS[wallId]'),'Source wall context missing');
assert.ok(html.includes('pdf-crosssection')&&html.includes('Variante A:'),
 'Powerpanel schema and 75mm variants not explained');
for(const id of ['startPitch','exitPitch','toggleSceneInfo','exploreSceneTitle','exploreSourceLine'])
 assert.ok(html.includes('id="'+id+'"'),'1.7 presentation UI missing: '+id);
assert.ok(!html.includes('Eugi-Edition')&&!html.includes('Eugi-Modus'),
 'Personal branding must not appear in public interface');
assert.ok(html.includes('Basic-Modus')&&app.includes("'◉ Basic-Modus'"),
 'Neutral Basic-Modus must be present');
assert.ok(app.includes("$('loadExample').click()")&&app.includes('if(!pdfView())return;'),
 'Presentation loading must respect the existing overwrite confirmation');
assert.ok(app.includes("event.key==='Escape'&&presentationFocus"),
 'Escape must always exit the presentation');
assert.ok(html.includes('body.scene-info-compact .explore-scene-overlay>:not(.explore-scene-bar)'),
 'Compact control state must leave the toggle reachable');
assert.ok(html.includes('body.presentation-focus #exitPitch{display:block}'),
 'Presentation exit control must stay visible');
assert.ok(!app.includes('project.sceneInfoCompact')&&!app.includes('project.presentationFocus'),
 'View preferences must never enter engineering JSON');
for(const id of ['exploreProtectionGuide','exploreProtectionStatus'])
 assert.ok(html.includes('id="'+id+'"'),'1.8 PDF guide control missing: '+id);
assert.ok(app.includes('function drawPdfProtectionGuide()'), '3D protection guide must be rendered');
assert.ok(app.includes("if(!exploreMode||!pdfView()||!exploreProtectionGuide||viewMode!=='3D')return"),
 'Protection guide must be restricted to PDF 3D Basic view');
assert.ok(app.includes('const {curve}=cableRenderPath(c,a,b)'),
 'Protection overlay must follow the same schematic curves as actual MS cables');
assert.ok(app.includes("new THREE.TorusGeometry(openingRadius(h)+.045"),
 'Unresolved OLA endpoints must be rings, not invented full cable paths');
assert.ok(app.includes('drawPdfProtectionGuide();view();draw2D();'),
 'Guide must be in the ordinary redraw process');
assert.ok(app.includes('exploreConstructionMode=\'hidden\';exploreProtectionGuide=true;'),
 'Pitch must show schematic guide by default');
assert.ok(!app.includes('project.exploreProtectionGuide'),
 'Display-only guide must not alter project JSON');
assert.ok(html.includes('id="pdfProtectionNote"'),
 'PDF concept must disclose the unresolved protective channel geometry');
assert.ok(app.includes("samples.create('pdf-u028')"), 'Fresh installs must load PDF V2 demo');
assert.ok(app.includes("const occupants=project.sourceInfo?.sourceVersion?.startsWith('PDF V2')"),
 'Occupied PDF conduits must be visually distinguishable');
assert.ok(app.includes("new THREE.LineCurve3(start,end)"),
 'Unpaired PDF cables must appear as clearly incomplete stubs');
assert.ok(app.includes("Gelbe Kabel-Endmarkierung"),
 'Unresolved cable ends must be explained to the presenter');
assert.ok(app.includes("h.id.split('_LR')"), 'PDF view must include LR numbering');
for(const side of ['Left','Right']){
 assert.ok(html.includes('id="sidebarToggle'+side+'"'),'Missing independently accessible '+side+' pane toggle');
 assert.ok(app.includes("$('sidebarToggle"+side+"').addEventListener('click'"),
  'Pane toggle click handler missing: '+side);
}
assert.ok(html.includes('body.sidebar-left-collapsed #app{--left-pane-width:0px}'),
 'Left options pane must release full width to model');
assert.ok(html.includes('body.sidebar-right-collapsed #app{--right-pane-width:0px}'),
 'Right options pane must release full width to model');
assert.ok(html.includes('body.presentation-focus .sidebar-edge-toggle{display:none}'),
 'Independent edge toggles must not block fullscreen focus');
assert.ok(app.includes("const SIDEBAR_LAYOUT_KEY='kabelsalat_sidebar_layout_v1'"),
 'Sidebar preferences must remain separate from project data');
assert.ok(!app.includes('project.leftSidebarCollapsed')&&!app.includes('project.rightSidebarCollapsed'),
 'Sidebar UI must not write into engineering project JSON');
for(const id of ['exploreCutawayWall','exploreCableCoverage'])
 assert.ok(html.includes('id="'+id+'"'), 'Demo v1.4 UI missing: '+id);
assert.ok(app.includes("exploreMode&&viewMode==='3D'&&exploreCutawayWall===w.id"),
 'Cutaway must hide a wall only for 3D explorer view');
assert.ok(app.includes("exploreCutawayWall=Object.hasOwn(CUTAWAY_AZIMUTH,selected)?selected:null"),
 'Wall cutaway must validate wall IDs');
assert.ok(app.includes("const routedCount=presentedCables.filter(c=>endpoint(c.a)&&endpoint(c.b)).length"),
 'Cable visibility must report incomplete assignments');
assert.ok(!app.includes("project.exploreCutawayWall"),
 'Presentation-only cutaway must not modify serialized project');
for(const id of ['exploreSystemFilters','exploreCameraPresets','explorePresentationFocus'])
 assert.ok(html.includes('id="'+id+'"'),'Demo v1.3 UI missing: '+id);
for(const state of ['exploreSystemFocus','presentationFocus','exploreCameraShot'])
 assert.ok(app.includes(state),'Demo v1.3 state missing: '+state);
assert.ok(app.includes("exploreSystemFocus!=='all'&&c.group!==exploreSystemFocus"),
 'Presentation filter must apply to visible cables');
assert.ok(app.includes("exploreSystemFocus!==key"),
 'Presentation filter must apply to visible enclosure groups');
assert.ok(app.includes("exploreSystemFocus!=='all'))return"),
 'Ungrouped supports must not imply trade-specific affiliation');
assert.ok(app.includes("document.body.classList.toggle('presentation-focus',exploreMode&&presentationFocus)"),
 'Full-screen demo must be restricted to explorer mode');
assert.ok(app.includes("if(exploreMode){mode='orbit';grab=null;draftDrag=null;}"),
 'Explorer must still be read-only');
assert.ok(app.includes("const CONSTRUCTION_MODES=['hidden','transparent','solid']"),
 'Presentation must offer hidden, translucent and solid modes');
assert.ok(app.includes("exploreConstructionMode==='hidden'||exploreSystemFocus!=='all'"),
 'Existing supports must be visible in combined presentation and hidden in single-system filters');
assert.ok(app.includes("if(exploreMode||mode!=='add')return;"),
 '2D view must not create pipes in presentation mode');
assert.ok(app.includes("if(exploreMode){mode='orbit';grab=null;draftDrag=null;}"),
 'Entering presentation mode must clear editing gestures');

for(const id of ['globalSearch','qualityList','minPipeGap','minEdgeGap','minBend',
 'reviewTarget','saveReview','exportPdf','exportViewer'])
 assert.ok(html.includes('id="'+id+'"'),'Quality/report UI missing: '+id);
assert.ok(app.includes('function renderQualityUI()'),'Quality view not wired');
assert.ok(app.includes('project.network.audit'),'Audit logging not wired');
assert.ok(html.includes('id="networkCanvas"'), 'Network canvas missing');
assert.ok(html.includes('id="netRouteSelect"'), 'Route editor missing');
assert.ok(html.includes('id="netPositionsFile"'), 'Reviewed coordinate import missing');
assert.ok(html.includes('id="supportList"'), 'Support UI missing');
assert.ok(html.includes('id="panelSelect"'), 'Plate editor missing');
assert.ok(html.includes('id="variantSelect"'), 'Variants UI missing');
assert.ok(app.includes("function draw2D()"), '2D painter missing');
assert.ok(app.includes("draft.addEventListener('pointermove'"), '2D direct editing missing');
assert.ok(app.includes("copySelectedHole"), 'Conduit copy missing');
assert.equal(require('../package.json').build.productName,'Kabelsalat');
new Function(app);
const ids=[...app.matchAll(/\$\('([A-Za-z0-9_-]+)'\)/g)].map(m=>m[1]);
for(const id of new Set(ids))assert.ok(html.includes('id="'+id+'"'),'UI element missing: '+id);
globalThis.KabelsalatComponents=require('../components.js');
globalThis.KabelsalatNetwork=require('../network.js');
globalThis.KabelsalatQuality=require('../quality.js');
globalThis.KabelsalatReports=require('../reports.js');
globalThis.KabelsalatReferences=require('../reference-data.js').factory(globalThis.KabelsalatComponents,globalThis.KabelsalatNetwork);
globalThis.KabelsalatSamples=require('../examples.js').factory(
 globalThis.KabelsalatComponents,globalThis.KabelsalatNetwork);
const before=app.split('let project;')[0].replace(/^\(\(\) => \{\s*/, '');
assert.ok(before.includes('function validProject(o)'), 'Data model must be testable');
assert.ok(app.includes("samples.create('pdf-u028')"), 'First start should load PDF V2 shaft reference');
const {demo,validProject,grid}=new Function(before+'\nreturn {demo,validProject,grid};')();
const initial=demo();
assert.equal(initial.walls.length,4,'Four independent shaft walls');
assert.deepEqual(initial.walls.map(w=>w.id),['SW1','SW2','SW3','SW4']);
assert.equal(initial.cables.length,16,'Reference contains 16 demo cables');
assert.equal(initial.cables.filter(c=>c.group==='OLA').length,3);
assert.equal(initial.cables.filter(c=>c.group==='MS').length,2);
assert.ok(initial.walls.every(w=>w.holes.length===w.rows*w.cols));
assert.ok(initial.walls.every(w=>w.holes.every(h=>h.dn===w.dn)));
assert.ok(app.includes('new THREE.ExtrudeGeometry'), 'Require real through-holes in model');
assert.ok(app.includes("viewMode==='Bodenplatten'"), 'Require separate base view');
assert.ok(html.includes('data-view="SW4"'), 'Require fourth wall view');
const roundtrip=validProject(JSON.parse(JSON.stringify(initial)));
assert.equal(roundtrip.walls.length,4);
assert.deepEqual(roundtrip.network.quality,{minPipeGapMm:null,minEdgeMm:null,minBendRadiusMm:null});
assert.ok(Array.isArray(roundtrip.network.reviews));
assert.ok(Array.isArray(roundtrip.network.audit));
assert.equal(roundtrip.activeShaftId,'S1');
assert.equal(roundtrip.network.shafts.length,1,'v3 single shaft becomes network');
const base=globalThis.KabelsalatNetwork.snapshot(roundtrip);
const extra={...base,name:'Nachbarschacht',cables:[{...base.cables[0],id:'NEWSEG'}]};
const expanded=JSON.parse(JSON.stringify(roundtrip));
expanded.network.shafts.push({id:'S2',name:'Nachbarschacht',x:5000,z:2500,snapshot:extra});
expanded.network.links.push({id:'L1',from:'S1',to:'S2'});
expanded.network.routes.push({id:'R1',name:'Trasse',group:'OLA',
 steps:[{shaftId:'S1',cableId:base.cables[0].id},{shaftId:'S2',cableId:'NEWSEG'}]});
const restored=validProject(JSON.parse(JSON.stringify(expanded)));
assert.equal(restored.network.shafts.length,2);
const withReview=JSON.parse(JSON.stringify(restored));
withReview.network.quality={minPipeGapMm:30,minEdgeMm:20,minBendRadiusMm:null};
withReview.network.reviews=[{shaftId:'S1',entityType:'cable',entityId:'c1',
 status:'Zur Prüfung',source:'Bauakte 12',note:'Vergleichen',checkedBy:'QA',checkedAt:'2026-11-05'}];
withReview.network.audit=[{at:'2026-11-05',shaftId:'S1',action:'Test geändert',description:'Daten'}];
const saved=validProject(JSON.parse(JSON.stringify(withReview)));
assert.equal(saved.network.quality.minPipeGapMm,30);
assert.equal(saved.network.reviews[0].source,'Bauakte 12');
assert.equal(saved.network.audit[0].action,'Test geändert');
assert.deepEqual(globalThis.KabelsalatNetwork.issues(restored.network,restored.network.routes[0]),[]);
assert.equal(restored.network.routes[0].steps.length,2);
assert.equal(restored.network.shafts[1].snapshot.cables[0].id,'NEWSEG');
assert.equal(roundtrip.splitEnabled,true,'parallel layout persists');
assert.equal(roundtrip.cables.length,16);
assert.equal(roundtrip.enclosures.OLA.panels.length,6);
assert.equal(roundtrip.enclosures.MS.panels.length,6);
const withSupports=JSON.parse(JSON.stringify(initial));
withSupports.supports.push(globalThis.KabelsalatComponents.newSupport(initial.dims,'test-support-1'));
withSupports.enclosures.OLA.panels[0].material='GFK';
withSupports.enclosures.OLA.panels[0].thickness=48;
const snap=globalThis.KabelsalatComponents.snapshot(withSupports);
withSupports.variants=[{id:'test-variant',name:'Vorher',createdAt:'2026-11-05',snapshot:snap}];
const v3=validProject(JSON.parse(JSON.stringify(withSupports)));
assert.equal(v3.supports[0].id,'test-support-1');
assert.equal(v3.enclosures.OLA.panels[0].material,'GFK');
assert.equal(v3.enclosures.OLA.panels[0].thickness,48);
assert.equal(v3.variants.length,1);
assert.equal(v3.variants[0].snapshot.supports[0].id,'test-support-1');
assert.deepEqual(roundtrip.cables[0].a,initial.cables[0].a);
const legacy=JSON.parse(JSON.stringify(initial));legacy.version=1;legacy.walls=legacy.walls.slice(0,3);
for(const w of legacy.walls){delete w.thickness;delete w.dn;for(const h of w.holes)delete h.dn;}
for(const c of legacy.cables){delete c.group;delete c.diameter;delete c.route;}
const migrated=validProject(legacy);
assert.equal(migrated.version,5);
assert.equal(migrated.walls.length,4);
assert.equal(migrated.walls[0].holes[0].dn,70);
assert.equal(migrated.cables[0].group,'Weitere');
assert.ok(Array.isArray(migrated.cables[0].route));
assert.ok(grid({id:'SW1',rows:2,cols:3,dn:80}).length===6);
const invalid=JSON.parse(JSON.stringify(initial));invalid.cables[0].a={wall:'BOGUS',hole:'BOGUS'};
assert.equal(validProject(invalid).cables[0].a,null);
console.log('OK – UI IDs, JS syntax, four walls, DN grid, 16 cables, enclosure groups, JSON roundtrip and v1 migration');
