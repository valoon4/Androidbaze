(() => {
'use strict';
const $=id=>document.getElementById(id),STORAGE='eugi.schachtwand.3d.v1';
const components=globalThis.KabelsalatComponents;
if(!components)throw Error('Kabelsalat-Komponentenmodul fehlt');
const networkModel=globalThis.KabelsalatNetwork;
if(!networkModel)throw Error('Kabelsalat-Netzmodul fehlt');
const qualityModel=globalThis.KabelsalatQuality,reportModel=globalThis.KabelsalatReports;
if(!qualityModel||!reportModel)throw Error('Kabelsalat-Prüf- oder Berichtmodul fehlt');
const samples=globalThis.KabelsalatSamples;
if(!samples)throw Error('Kabelsalat-Beispielmodul fehlt');
const pdfGeometry=globalThis.KabelsalatPdfGeometry;
if(!pdfGeometry)throw Error('Kabelsalat-PDF-Geometriemodul fehlt');
const schematicPositions=globalThis.KabelsalatSchematicPositions;
if(!schematicPositions)throw Error('Kabelsalat-Demo-Koordinatenmodul fehlt');
const pipeDimensions=globalThis.KabelsalatPipeDimensions;
if(!pipeDimensions)throw Error('Kabelsalat-Rohrmaßmodul fehlt');
const photoCandidates=globalThis.KabelsalatPhotoCandidates;
if(!photoCandidates)throw Error('Kabelsalat-Fotoerkennungsmodul fehlt');
const concreteWall=globalThis.KabelsalatConcreteWall;
if(!concreteWall)throw Error('Kabelsalat-Betonwandmodul fehlt');
const constructionV2=globalThis.KabelsalatConstructionV2;
if(!constructionV2)throw Error('Kabelsalat-2.0-Geometriemodul fehlt');
const avlReference=()=>['13_T_021','13_U_028'].includes(project?.sourceInfo?.shaft);
const COLORS=['#e75160','#1aa676','#398bdb','#e3ad3f','#9a6dcd','#12a5ae'];
const WALL_NAMES={SW1:'Linke Schachtwand',SW2:'Rückwand',SW3:'Rechte Schachtwand',SW4:'Vordere Schachtwand'}; // Pre-V2 projects unchanged.
const PDF_WALLS={
 SW1:'Stirnwand · 13_U_042 / Gleis · Trasse 1 + 2 · PDF S. 21',
 SW2:'Rechte Wand · 21_B_010 / Tunnelwand · Trasse 1 + 3 · PDF S. 22',
 SW3:'Gegenüber SW1 · 13_U_032 · gespiegelte LR-Reihenfolge · PDF S. 23',
 SW4:'Linke Wand · 13_U_021 · Trasse 1 · PDF S. 24'
};
const pdfView=()=>project.sourceInfo?.sourceVersion?.startsWith('PDF V2')===true;
const clamp=(v,a,b)=>Math.max(a,Math.min(b,v));
const esc=x=>String(x).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
let idCounter=0;const uid=p=>p+'-'+Date.now().toString(36)+'-'+(++idCounter).toString(36);

function grid(wall){
 const holes=[];for(let row=1;row<=wall.rows;row++)for(let col=1;col<=wall.cols;col++)
 holes.push({id:wall.id+'-r'+row+'-o'+col,row,col,u:col/(wall.cols+1),v:1-row/(wall.rows+1),dn:wall.dn||70});
 return holes;
}
function demo(){
 const walls=[
 {id:'SW1',name:'SW1',visible:true,rows:5,cols:5,thickness:120,dn:70,holes:[]},
 {id:'SW2',name:'SW2',visible:true,rows:4,cols:7,thickness:120,dn:70,holes:[]},
 {id:'SW3',name:'SW3',visible:true,rows:5,cols:5,thickness:120,dn:70,holes:[]},
 {id:'SW4',name:'SW4',visible:true,rows:5,cols:6,thickness:120,dn:70,holes:[]}
 ];
 walls.forEach(w=>w.holes=grid(w));
 const pairs=[
 ['SW1',2,2,'SW3',2,3],['SW1',3,2,'SW3',4,2],['SW1',4,3,'SW2',3,2],
 ['SW2',1,3,'SW3',3,4],['SW1',1,4,'SW3',1,2],['SW2',3,4,'SW3',4,4],
 ['SW1',4,1,'SW2',2,6],['SW1',2,4,'SW3',5,3],
 ['SW4',2,2,'SW1',2,3],['SW4',3,3,'SW2',1,6],['SW4',4,4,'SW3',3,2],
 ['SW1',1,1,'SW4',1,4],['SW1',5,2,'SW4',5,5],['SW2',4,2,'SW4',4,2],
 ['SW2',2,5,'SW4',2,5],['SW3',1,3,'SW4',1,2]
 ];
 const names=['OLA · LR18','OLA · LR19','OLA · LR20','MS · LR13','MS · LR14',
 'LST · LR5','LST · LR6','TK Cu · LR7','TK Cu · LR8','Weitere · LR1','Weitere · LR2',
 'Weitere · LR3','Weitere · LR4','Weitere · LR5','Weitere · LR6','Weitere · LR7'];
 const colors=['#d94b57','#d94b57','#d94b57','#248dba','#248dba','#349e7b',
 '#349e7b','#9b67bd','#9b67bd','#d8a43e','#d8a43e','#9b67bd','#9b67bd','#349e7b','#8a929c','#8a929c'];
 const cables=pairs.map((p,i)=>({id:'c'+(i+1),name:names[i],color:colors[i],group:i<3?'OLA':i<5?'MS':'Weitere',
 diameter:12,route:[],a:{wall:p[0],hole:p[0]+'-r'+p[1]+'-o'+p[2]},
 b:{wall:p[3],hole:p[3]+'-r'+p[4]+'-o'+p[5]}}));
 return {version:3,name:'Referenzschacht · 3D-Entwurf',
 dims:{w:2400,d:1900,h:2100},walls,cables,opacity:54,showBox:true,showLabels:true,splitEnabled:true,
 enclosures:components.defaults({w:2400,d:1900,h:2100}),supports:[],variants:[]};
}
const cleanReferenceText=(v,max=240)=>typeof v==='string'?v.slice(0,max):'';
function normalizeSourceInfo(x){
 if(!x||typeof x!=='object')return null;
 return {shaft:cleanReferenceText(x.shaft,90),sourceFile:cleanReferenceText(x.sourceFile,220),
 savedAt:cleanReferenceText(x.savedAt,70),document:cleanReferenceText(x.document,300),
 sourceVersion:cleanReferenceText(x.sourceVersion,150),
 sourcePages:(Array.isArray(x.sourcePages)?x.sourcePages:[]).slice(0,30).filter(Number.isFinite),
 geometryStatus:'schematic',warning:cleanReferenceText(x.warning,650),
 originalNote:cleanReferenceText(x.originalNote,1800),
 planAnchors:(Array.isArray(x.planAnchors)?x.planAnchors:[]).slice(0,100).map(p=>({
 id:cleanReferenceText(p.id,140),wall:cleanReferenceText(p.wall,10),
 sourceAddress:cleanReferenceText(p.sourceAddress,150),
 sourcePage:Number.isFinite(p.sourcePage)?p.sourcePage:null,
 destination:cleanReferenceText(p.destination,140),physicalHole:false})),
 unmatchedEndpointCount:Math.max(0,Math.min(800,Number(x.unmatchedEndpointCount)||0)),
 originalCableCount:Math.max(0,Math.min(1000,Number(x.originalCableCount)||0)),
 originalPhysicalPortCount:Math.max(0,Math.min(2400,Number(x.originalPhysicalPortCount)||0))};
}
function validProject(o){
 if(!o||typeof o!=='object'||!o.dims||!Array.isArray(o.walls)||!Array.isArray(o.cables))throw Error('Ungültige Projektdatei');
 const base=demo(),ids=['SW1','SW2','SW3','SW4'];
 const dims={w:clamp(Number(o.dims.w)||2400,600,12000),
 d:clamp(Number(o.dims.d)||1900,600,12000),h:clamp(Number(o.dims.h)||2100,600,8000)};
 const walls=ids.map(id=>{
 const w=o.walls.find(x=>x&&x.id===id)||base.walls.find(x=>x.id===id);
 const thickness=clamp(Number(w.thickness)||120,40,500),dn=clamp(Number(w.dn)||70,20,300);
 const holes=Array.isArray(w.holes)?w.holes.filter(h=>h&&typeof h.id==='string'&&Number.isFinite(+h.u)&&Number.isFinite(+h.v)).slice(0,600).map(h=>({
 id:h.id.slice(0,120),row:clamp(Number(h.row)||0,0,30),col:clamp(Number(h.col)||0,0,30),
 u:clamp(+schematicPositions.migrate(h,o.sourceInfo?.sourceVersion?.startsWith('PDF V2')).u,0,1),
 v:clamp(+schematicPositions.migrate(h,o.sourceInfo?.sourceVersion?.startsWith('PDF V2')).v,0,1),
 dn:clamp(Number(h.dn)||dn,20,300),
 ...(h.dimensionSpec?{dimensionSpec:pipeDimensions.normalize(h.dimensionSpec,h.dn)}:{}),
 sourceAddress:cleanReferenceText(h.sourceAddress,140),
 sourcePage:Number.isFinite(h.sourcePage)?h.sourcePage:null,
 sourceCount:Number.isFinite(h.sourceCount)?h.sourceCount:null,
 sourceCategory:cleanReferenceText(h.sourceCategory,50),
 nominalDnConfirmed:h.nominalDnConfirmed===true,
 origin:cleanReferenceText(h.origin,70),
 ...(Number.isFinite(h.sourceU)&&Number.isFinite(h.sourceV)?{
  sourceU:clamp(h.sourceU,0,1),sourceV:clamp(h.sourceV,0,1)}:{})
 })):grid(w);
 return {id,name:id,visible:w.visible!==false,rows:clamp(Number(w.rows)||5,1,20),
 cols:clamp(Number(w.cols)||5,1,20),thickness,dn,holes};
 });
 const find=r=>r&&walls.some(w=>w.id===r.wall&&w.holes.some(h=>h.id===r.hole));
 const cables=o.cables.slice(0,400).filter(c=>c&&typeof c==='object').map((c,i)=>({
 id:String(c.id||uid('c')).slice(0,120),name:String(c.name||'Kabel '+(i+1)).slice(0,80),
 color:/^#[0-9a-f]{6}$/i.test(c.color)?c.color:COLORS[i%COLORS.length],
 group:['OLA','MS','Weitere'].includes(c.group)?c.group:'Weitere',
 diameter:clamp(Number(c.diameter)||12,2,80),
 route:Array.isArray(c.route)?c.route.filter(p=>Array.isArray(p)&&p.length===3&&p.every(n=>Number.isFinite(+n)))
 .slice(0,12).map(p=>[clamp(+p[0],-dims.w/2,dims.w/2),clamp(+p[1],0,dims.h),clamp(+p[2],-dims.d/2,dims.d/2)]):[],
 a:find(c.a)?{wall:c.a.wall,hole:c.a.hole}:null,
 b:find(c.b)?{wall:c.b.wall,hole:c.b.hole}:null,
 referenceGroup:cleanReferenceText(c.referenceGroup,50),
 sourceCableId:cleanReferenceText(c.sourceCableId,120),
 sourceOriginalA:cleanReferenceText(c.sourceOriginalA,140),
 sourceOriginalB:cleanReferenceText(c.sourceOriginalB,140),
 sourceA:cleanReferenceText(c.sourceA,140),sourceB:cleanReferenceText(c.sourceB,140),
 source:cleanReferenceText(c.source,250),
 sourcePages:(Array.isArray(c.sourcePages)?c.sourcePages:[]).slice(0,20).filter(Number.isFinite),
 mappingStatus:cleanReferenceText(c.mappingStatus,200),
 sourceKind:cleanReferenceText(c.sourceKind,80),
 drawingDiameterOnly:c.drawingDiameterOnly===true
 }));
 const enclosures=components.normalizeEnclosures(o.enclosures,dims);
 const supports=components.normalizeSupports(o.supports,dims);
 const variants=components.normalizeVariants(o.variants);
 const result={version:5,name:String(o.name||'Neues Projekt').slice(0,80),dims,walls,cables,
 opacity:clamp(Number(o.opacity)||55,10,100),showBox:o.showBox!==false,
 showLabels:o.showLabels!==false,splitEnabled:o.splitEnabled!==false,
 enclosures,supports,variants,sourceInfo:normalizeSourceInfo(o.sourceInfo)};
 result.network=networkModel.normalizeNetwork(o.network,networkModel.snapshot(result));
 schematicPositions.transformProjectShunt(result.network);
 result.network.quality=qualityModel.normalizeSettings(o.network?.quality);
 result.network.reviews=qualityModel.normalizeReviews(o.network?.reviews,result.network.shafts);
 result.network.audit=qualityModel.normalizeAudit(o.network?.audit);
 result.activeShaftId=result.network.shafts.some(x=>x.id===o.activeShaftId)?
 o.activeShaftId:result.network.shafts[0].id;
 return result;
}
let project;
try{
 const previous=localStorage.getItem(STORAGE);
 project=validProject(previous?JSON.parse(previous):samples.create('pdf-u028'));
}catch(_){project=validProject(samples.create('pdf-u028'));}
let wallId='SW1',holeId=null,cableId=project.cables[0]?.id||null,filter='all',mode='orbit',grab=null;
let panelKey='OLA-floor',supportId=null,variantId=null;
let networkOpen=false,netRouteId=null,netSegmentShaftId=null,netLinkId=null;
let mapZoom=1,mapOffsetX=0,mapOffsetY=0,mapDrag=null;
let netLinkFromId=null,netLinkToId=null,reviewKey='shaft';
/* Basic-mode UI preferences are separate from project data and never affect engineering exports. */
const EXPLORE_MODE_KEY='kabelsalat_ui_mode_v1';
const EXPLORE_PROGRESS_KEY='kabelsalat_explore_progress_v1';
let exploreMode=true,exploreWith2D=false,exploreStage='shaft';
let demoCompactView=true; // Visual-only, never saved in project or source coordinates.
/* Presentation-only visibility: never included in project JSON, undo or audit logs. */
let exploreConstructionMode='hidden';
let exploreProtectionGuide=false; // View-only PDF concept; never saved to project JSON.
const CONSTRUCTION_MODES=['hidden','transparent','solid'];
/* Pure presentation controls, never serialized to the project. */
let exploreSystemFocus='all',presentationFocus=false,exploreCameraShot='overview';
let sceneInfoCompact=false; // View-only; not persisted into project or exported.
let pdfNoticeDismissed=false;
let exploreCutawayWall=null; // Temporary 3D cutaway; never persisted or exported.
/* Pane layout lives in browser UI preferences, never in project JSON. */
const SIDEBAR_LAYOUT_KEY='kabelsalat_sidebar_layout_v1';
let leftSidebarCollapsed=false,rightSidebarCollapsed=false;
try{
 const savedLayout=JSON.parse(localStorage.getItem(SIDEBAR_LAYOUT_KEY)||'{}');
 leftSidebarCollapsed=savedLayout.left===true;
 rightSidebarCollapsed=savedLayout.right===true;
}catch(_){/* If storage is unavailable, retain both panes. */}
const CUTAWAY_AZIMUTH={SW1:-Math.PI/2,SW2:Math.PI,SW3:Math.PI/2,SW4:0};
const PRESENTATION_GROUPS=['all','OLA','MS'];
const CAMERA_SHOTS={
 overview:{azimuth:.72,elevation:.42,zoom:1,view:'3D'},
 close:{azimuth:-.65,elevation:.65,zoom:.72,view:'3D'},
 top:{azimuth:.72,elevation:.42,zoom:.88,view:'Oben'}
};
let exploreVisited=new Set();
try{
 exploreMode=localStorage.getItem(EXPLORE_MODE_KEY)!=='expert';
 const stored=JSON.parse(localStorage.getItem(EXPLORE_PROGRESS_KEY)||'[]');
 if(Array.isArray(stored))exploreVisited=new Set(stored.filter(x=>['network','shaft','cable'].includes(x)));
}catch(_){exploreMode=true;}
document.body.classList.toggle('explore-mode',exploreMode);
document.body.classList.toggle('explore-with-2d',exploreMode&&exploreWith2D);

const undo=[],redo=[];
const wall=()=>project.walls.find(w=>w.id===wallId);
const cable=()=>project.cables.find(c=>c.id===cableId);
const hole=()=>wall()?.holes.find(h=>h.id===holeId);
const plate=()=>project.enclosures[panelKey.split('-')[0]]?.panels.find(p=>p.id===panelKey);
const support=()=>project.supports.find(s=>s.id===supportId);
const currentVariant=()=>project.variants.find(v=>v.id===variantId);
function syncActiveShaft(){
 const s=project.network?.shafts.find(x=>x.id===project.activeShaftId);
 if(s){s.name=project.name;s.snapshot=networkModel.snapshot(project);}
}
function persist(){
 syncActiveShaft();
 try{localStorage.setItem(STORAGE,JSON.stringify(project));$('saveStatus').textContent='● Lokal gespeichert';}
 catch(_){$('saveStatus').textContent='⚠ Speichern fehlgeschlagen';}
}
function change(fn,label='Planung geändert'){
 undo.push(JSON.stringify(project));if(undo.length>80)undo.shift();redo.length=0;
 fn();syncActiveShaft();
 if(project.network){
 const entry={at:new Date().toISOString(),shaftId:project.activeShaftId,
 action:String(label).slice(0,120),description:project.name};
 project.network.audit=qualityModel.normalizeAudit([...(project.network.audit||[]),entry]);
 }
 renderAll();persist();
}
function undoRedo(back){const from=back?undo:redo,to=back?redo:undo;if(!from.length)return;to.push(JSON.stringify(project));project=validProject(JSON.parse(from.pop()));fixSelection();syncActiveShaft();renderAll();persist();}
function fixSelection(){if(!project.walls.some(w=>w.id===wallId))wallId='SW1';if(!wall()?.holes.some(h=>h.id===holeId))holeId=null;if(!project.cables.some(c=>c.id===cableId))cableId=project.cables[0]?.id||null;
 if(!plate())panelKey='OLA-floor';
 if(!project.supports.some(s=>s.id===supportId))supportId=project.supports[0]?.id||null;
 if(!project.variants.some(v=>v.id===variantId))variantId=project.variants[0]?.id||null;
 if(!project.network.routes.some(v=>v.id===netRouteId))netRouteId=project.network.routes[0]?.id||null;
 if(!project.network.shafts.some(v=>v.id===netSegmentShaftId))netSegmentShaftId=project.activeShaftId;}
function wallSpanMm(id){
 // PDF V2 S.4: SW1/SW3 span shaft width; SW2/SW4 span depth.
 // Legacy HTML examples preserve their older axis convention.
 return pdfView()?pdfGeometry.span(id,project.dims):
  id==='SW1'||id==='SW3'?project.dims.d:project.dims.w;
}
function geometryWarnings(){
 const report=qualityModel.auditWallOpenings(project,visualHole);
 return {overlaps:report.overlaps.length,edges:report.edgeCrossings.length,
  minGapMm:report.minGapMm,minEdgeMm:report.minEdgeMm,invalidIds:report.invalidIds};
}
function holeText(w,h){
 if(project.sourceInfo?.sourceVersion?.startsWith('PDF V2')&&h.sourceAddress)
  return w.id+' · '+h.sourceAddress;
 return w.id+' · '+(h.row?'Reihe '+h.row+', Öffnung '+h.col:'Frei platziert');
}
function referenceText(r){if(!r)return 'Nicht zugeordnet';const w=project.walls.find(x=>x.id===r.wall),h=w?.holes.find(x=>x.id===r.hole);return h?holeText(w,h)+' · '+pipeDimensions.describe(h):'Nicht zugeordnet';}
function hueGroup(color){
 const r=parseInt(color.slice(1,3),16)/255,g=parseInt(color.slice(3,5),16)/255,b=parseInt(color.slice(5,7),16)/255;
 const max=Math.max(r,g,b),min=Math.min(r,g,b),d=max-min;
 if(d<.1)return 'other';
 let h=(max===r?(g-b)/d+(g<b?6:0):max===g?(b-r)/d+2:(r-g)/d+4)*60;
 return h<26||h>=340?'red':h<70?'yellow':h<165?'green':h<260?'blue':'other';
}
function optionsHTML(selected){let html='<option value="">– Nicht zugeordnet –</option>';
 for(const w of project.walls){html+='<optgroup label="'+esc(w.id)+'">';
 for(const h of w.holes){const key=w.id+'|'+h.id;html+='<option value="'+esc(key)+'"'+(selected&&selected.wall===w.id&&selected.hole===h.id?' selected':'')+'>'+esc(holeText(w,h))+'</option>';}
 html+='</optgroup>';}return html;}
function parseEndpoint(v){if(!v)return null;const i=v.indexOf('|');return i<0?null:{wall:v.slice(0,i),hole:v.slice(i+1)};}
function renderUI(){
 const totalShafts=project.network.shafts.length;
 const totalCables=project.network.shafts.reduce((n,s)=>n+s.snapshot.cables.length,0);
 $('exampleInfo').textContent=totalShafts+' Schächte · '+totalCables+
 ' Kabel im Projekt · aktuelle Ansicht: '+project.activeShaftId+
 (project.sourceInfo?.sourceVersion?.startsWith('PDF V2')?
 ' · PDF V2: DN und Belegung aus Wandzeichnungen, Abstände und Brandschutz ungeprüft.':
 project.sourceInfo?' · HTML-Referenz: Maße, DN und Wege nicht fachlich bestätigt.':' · Fiktive Beispieldaten.');
 $('projectName').value=project.name;$('width').value=project.dims.w;$('depth').value=project.dims.d;$('height').value=project.dims.h;
 $('rows').value=wall().rows;$('cols').value=wall().cols;$('wallThickness').value=wall().thickness;$('wallDn').value=wall().dn;$('wallTitle').textContent=wallId+' · Konfiguration';
 $('copyHole').disabled=!hole();$('draftCopy').disabled=!hole();
 $('toggleWall').textContent=wall().visible?'Sichtbar':'Ausgeblendet';
 $('wallList').innerHTML=project.walls.map(w=>'<div class="wall-card '+(w.id===wallId?'active':'')+'" data-wall="'+w.id+'"><div class="row spread"><b><span class="dot" style="background:'+(w.visible?'#68a989':'#c9d2d7')+'"></span>'+w.id+'</b><span class="badge">'+w.holes.length+' Öffnungen</span></div><div class="small">'+(pdfView()?esc(PDF_WALLS[w.id]):WALL_NAMES[w.id]+' · '+w.rows+' × '+w.cols)+'</div></div>').join('');
 $('viewport').classList.toggle('with-2d',project.splitEnabled!==false);$('splitEnabled').checked=project.splitEnabled!==false;
 $('opacity').value=project.opacity;$('opacityText').textContent=project.opacity+' %';$('showBox').checked=project.showBox;$('showLabels').checked=project.showLabels;
 $('modeButtons').querySelectorAll('button').forEach(b=>b.classList.toggle('active',b.dataset.mode===mode));$('scene').classList.toggle('placing',mode==='add');
 $('modeHint').textContent=mode==='add'?'Klicke auf eine Schachtwand, um dort ein frei positioniertes Leerrohr zu platzieren.':
 mode==='move'?'Ziehe ein vorhandenes Leerrohr auf seiner Wand. Verschobene Öffnungen bleiben Kabelanschlüsse.':
 'Modell mit der Maus drehen, mit dem Mausrad zoomen. Öffnungen oder Kabel anklicken, um sie zu bearbeiten.';
 const h=hole();$('holeEditor').style.display=h?'block':'none';
 if(h){
 $('holeTitle').textContent=wallId+' · '+holeText(wall(),h);
 $('holeU').value=Math.round(h.u*wallSpanMm(wallId));
 $('holeV').value=Math.round(h.v*project.dims.h);
 $('holeDn').value=h.dn;
 const dims=pipeDimensions.forHole(h);
 $('holeDimensionSummary').textContent=pipeDimensions.describe(h);
 for(const [id,val] of [['holeInnerMm',dims.innerMm],['holeOuterMm',dims.outerMm],
  ['holeOpeningMm',dims.openingMm]])$(id).value=val??'';
 $('holeDimensionSource').value=dims.source;
 $('holeDimensionReviewer').value=dims.reviewer;
 $('holeDimensionStatus').value=dims.status;
 const occupied=project.cables.filter(c=>
  c.a?.wall===wallId&&c.a?.hole===h.id||c.b?.wall===wallId&&c.b?.hole===h.id);
 $('holeOccupancy').innerHTML=(project.sourceInfo&&h.nominalDnConfirmed!==true?
 '<div class="small" style="color:#995e36">⚠ '+
 (project.sourceInfo.sourceVersion?.startsWith('PDF V2')?
  'DN aus PDF-Wandbild; reale Lage und Maße nicht bestätigt. ':
  'Rohrdurchmesser aus grafischer HTML-Öffnung; DN nicht bestätigt. ')+
 esc(h.sourceAddress||'')+'</div>':'')+(occupied.length?occupied.map(c=>
  '<div class="search-hit" data-occupied-cable="'+esc(c.id)+'">'+
  '<span class="dot" style="background:'+c.color+'"></span>'+
  esc(c.name)+' ('+esc(c.group)+', '+c.diameter+' mm)</div>').join(''):
  '<div class="small">Frei · keine Kabel zugeordnet</div>');
}
 $('cableFilters').querySelectorAll('button').forEach(b=>{
 b.classList.toggle('active',b.dataset.filter===filter);
 if(['OLA','MS','Weitere'].includes(b.dataset.filter))
 b.textContent=b.dataset.filter+' '+project.cables.filter(c=>c.group===b.dataset.filter).length;
 if(b.dataset.filter==='all')b.textContent='Alle '+project.cables.length;
 });
 const match=c=>filter==='all'||(['OLA','MS','Weitere'].includes(filter)?c.group===filter:hueGroup(c.color)===filter);
 $('cableList').innerHTML=project.cables.filter(match).map(c=>'<div class="cable '+(c.id===cableId?'active':'')+'" data-cable="'+esc(c.id)+'"><span class="swatch" style="background:'+c.color+'"></span><div style="flex:1;min-width:0"><div class="cable-name">'+esc(c.name)+'</div><div class="cable-path">'+esc(referenceText(c.a))+' → '+esc(referenceText(c.b))+'</div></div></div>').join('') || '<p class="small">Keine Kabel in diesem Filter.</p>';
 const c=cable();$('empty-cable').hidden=!!c;$('cableForm').hidden=!c;
 if(c){
 $('cableName').value=c.name;$('cableColor').value=c.color;$('cableGroup').value=c.group;
 $('cableDiameter').value=c.diameter;$('cableA').innerHTML=optionsHTML(c.a);$('cableB').innerHTML=optionsHTML(c.b);
 $('waypointList').innerHTML=c.route.map((p,i)=>'<div class="point-row" data-index="'+i+'"><b>'+String(i+1)+'</b>'+
 ['X','Y','Z'].map((axis,k)=>'<input type="number" data-axis="'+k+'" value="'+Math.round(p[k])+'" title="'+axis+' (mm)" step="10">').join('')+
 '<button data-remove="'+i+'" title="Bogenpunkt entfernen">×</button></div>').join('')||
 '<span class="muted">Automatischer Verlauf (noch keine manuellen Bogenpunkte)</span>';
 }
 for(const k of ['ola','ms']){const e=project.enclosures[k.toUpperCase()];
 $(k+'Visible').checked=e.visible;$(k+'Opacity').value=e.opacity;$(k+'OpacityText').textContent=e.opacity+' %';}

 const names=Object.fromEntries(components.FACES);
 $('panelSelect').innerHTML=['OLA','MS'].flatMap(k=>project.enclosures[k].panels.map(p=>
 '<option value="'+p.id+'">'+k+' · '+names[p.face]+'</option>')).join('');
 $('panelSelect').value=panelKey;
 const pl=plate();
 if(pl){
 $('panelMaterial').value=pl.material;$('panelWidth').value=pl.width;
 $('panelHeight').value=pl.height;$('panelThickness').value=pl.thickness;
 $('panelOffset').value=pl.offset;$('panelOpacity').value=pl.opacity;
 $('panelOpacityText').textContent=pl.opacity+' %';$('panelVisible').checked=pl.visible;
 }
 const blocked=['OLA','MS'].flatMap(g=>components.potentialBlockages(project,g));
 $('panelWarning').textContent=blocked.length?
 '⚠ Mögliche Rohrüberdeckung: '+blocked.length+' betroffene Platten/Rohr-Paare. Nur vereinfachte geometrische Näherung.':
 'Kein möglicher Rohrkonflikt nach vereinfachtem geometrischem Check erkannt. Keine normgerechte Freigabe!';
 $('panelWarning').style.background=blocked.length?'#fff1e7':'#edf8f1';
 $('supportList').innerHTML=project.supports.map(x=>
 '<div class="support-item '+(x.id===supportId?'active':'')+'" data-support="'+esc(x.id)+'"><b>'+esc(x.name)+
 '</b><div class="small">'+esc(x.type)+' · '+esc(x.wall)+' · '+Math.round(x.y)+' mm Höhe</div></div>').join('')||
 '<div class="small">Noch keine Konsole oder Auflager vorhanden.</div>';
 const st=support();$('supportForm').hidden=!st;
 if(st){
 $('supportName').value=st.name;$('supportType').value=st.type;$('supportWall').value=st.wall;
 for(const k of ['X','Y','Z','W','H','D'])$('support'+k).value=st[k.toLowerCase()];
 $('supportVisible').checked=st.visible;
 }
 $('variantSelect').innerHTML='<option value="">– Planungsstand wählen –</option>'+
 project.variants.map(v=>'<option value="'+esc(v.id)+'">'+esc(v.name)+'</option>').join('');
 $('variantSelect').value=variantId||'';
 const v=currentVariant();
 $('restoreVariant').disabled=!v;$('deleteVariant').disabled=!v;
 const diff=v?components.differences(v.snapshot,components.snapshot(project)):[];
 $('variantDiff').textContent=v?('Vergleich mit „'+v.name+'“: '+
 (diff.length?diff.join(' · '):'Keine erfassten Unterschiede.')):
 'Bis zu 12 lokale Varianten werden in derselben Projektdatei gespeichert.';

 $('viewTabs').querySelectorAll('button').forEach(b=>b.classList.toggle('active',b.dataset.view===viewMode));
 const count=project.walls.reduce((s,w)=>s+w.holes.length,0);
 $('sceneStats').textContent=project.walls.length+' Wände · '+count+' Öffnungen · '+project.cables.length+' Kabel';
 const problems=geometryWarnings();
 $('collisionInfo').textContent=(problems.overlaps||problems.edges?'⚠ Geometrischer Hinweis: ':'✓ Geometrie: ')+
 problems.overlaps+' überlappende Rohrpaare, '+problems.edges+' Randüberschreitungen.'+
 (problems.minGapMm===null?'':' Geringste lichte Modellweite: '+problems.minGapMm.toFixed(1)+' mm.')+
 (problems.minEdgeMm===null?'':' Geringster Randabstand: '+problems.minEdgeMm.toFixed(1)+' mm.')+
 ' Nur schematische Abstände aus Plan-/Modelldaten, kein Aufmaß und keine Normfreigabe.';
 $('collisionInfo').style.background=problems.overlaps||problems.edges?'#fff1e7':'#edf8f1';
 $('viewInfo').textContent=project.name;$('undo').disabled=!undo.length;$('redo').disabled=!redo.length;
}
const scene=new THREE.Scene();scene.background=new THREE.Color('#edf2f4');
const camera=new THREE.PerspectiveCamera(42,1,.015,200);
const orthoCamera=new THREE.OrthographicCamera(-2,2,2,-2,.01,200);let activeCamera=camera;
const renderer=new THREE.WebGLRenderer({canvas:$('scene'),antialias:true,preserveDrawingBuffer:true,alpha:false});
renderer.setPixelRatio(Math.min(window.devicePixelRatio||1,2));
renderer.setClearColor('#e2edf1');
if('outputColorSpace' in renderer)renderer.outputColorSpace=THREE.SRGBColorSpace;
scene.add(new THREE.HemisphereLight(0xffffff,0xa3b4bf,1.35));
const light=new THREE.DirectionalLight(0xffffff,1.1);light.position.set(-4,8,6);scene.add(light);
scene.add(new THREE.AmbientLight(0xffffff,.15));
const models=new THREE.Group();scene.add(models);
const hitObjects=[],wallObjects=[],raycaster=new THREE.Raycaster(),pointer=new THREE.Vector2();
let visibleClearance=null; // View-only alert state, never serialized into engineering data.
let azimuth=.72,elevation=.42,zoom=1.0,viewMode='3D';
const sizes=()=>({w:project.dims.w/1000,d:project.dims.d/1000,h:project.dims.h/1000});
/* PDF V2 p.4 is authoritative:
   SW1 = Stirnwand (z-), SW2 = rechts (x+), SW3 = gegenüber (z+),
   SW4 = links (x-). Earlier demos retain their original axes. */
const pdfWallWidth=(id,s)=>pdfGeometry.span(id,s);
function view(){
 const d=sizes(),aspect=Math.max(.3,$('scene').clientWidth/Math.max(1,$('scene').clientHeight));
 if(viewMode==='3D'){
 activeCamera=camera;
 const r=Math.max(d.w,d.d,d.h)*1.7*zoom,t=new THREE.Vector3(0,d.h*.48,0);
 camera.position.set(r*Math.sin(azimuth)*Math.cos(elevation),t.y+r*Math.sin(elevation),
 r*Math.cos(azimuth)*Math.cos(elevation));camera.lookAt(t);camera.updateProjectionMatrix();
 }else{
 activeCamera=orthoCamera;
 let span=d.w,target=new THREE.Vector3(0,d.h/2,0),pos=new THREE.Vector3(0,d.h+8,0);
 orthoCamera.up.set(0,1,0);
 if(viewMode==='Oben'||viewMode==='Bodenplatten'){
 span=d.w;target.set(0,d.h*.45,0);pos.set(0,d.h+8,0);orthoCamera.up.set(0,0,-1);
 }else{
 span=viewMode==='SW1'||viewMode==='SW3'?d.d:d.w;
 if(pdfView()){
  const referenceCamera=pdfGeometry.camera(viewMode,d);
  span=referenceCamera.span;
  pos.set(...referenceCamera.position);target.set(...referenceCamera.target);
 }else{
  if(viewMode==='SW1'){pos.set(d.w/2+4,d.h/2,0);target.set(-d.w/2,d.h/2,0);}
  if(viewMode==='SW3'){pos.set(-d.w/2-4,d.h/2,0);target.set(d.w/2,d.h/2,0);}
  if(viewMode==='SW2'){pos.set(0,d.h/2,d.d/2+4);target.set(0,d.h/2,-d.d/2);}
  if(viewMode==='SW4'){pos.set(0,d.h/2,-d.d/2-4);target.set(0,d.h/2,d.d/2);}
 }
 }
 const neededHeight=Math.max((span*1.38)/aspect,
 (viewMode==='Oben'||viewMode==='Bodenplatten'?d.d:d.h)*1.35)*zoom;
 orthoCamera.left=-neededHeight*aspect/2;orthoCamera.right=neededHeight*aspect/2;
 orthoCamera.top=neededHeight/2;orthoCamera.bottom=-neededHeight/2;
 orthoCamera.position.copy(pos);orthoCamera.lookAt(target);orthoCamera.updateProjectionMatrix();
 }
}
function disposeModel(){models.traverse(obj=>{if(obj.geometry)obj.geometry.dispose();if(obj.material){const a=Array.isArray(obj.material)?obj.material:[obj.material];a.forEach(m=>{if(m.map)m.map.dispose();m.dispose();});}});models.clear();hitObjects.length=0;wallObjects.length=0;}
function material(color,opacity=1){return new THREE.MeshStandardMaterial({color,roughness:.8,metalness:.05,transparent:opacity<1,opacity,side:THREE.DoubleSide,depthWrite:opacity>=1});}
function box(w,h,d,x,y,z,col,opacity=1){const m=new THREE.Mesh(new THREE.BoxGeometry(w,h,d),material(col,opacity));m.position.set(x,y,z);models.add(m);return m;}
function createLabel(text,pos){const c=document.createElement('canvas');c.width=280;c.height=76;const ctx=c.getContext('2d');
 ctx.fillStyle='#244753';ctx.font='bold 32px Segoe UI,Arial';ctx.textAlign='center';ctx.fillText(text,140,47);
 const tex=new THREE.CanvasTexture(c),sprite=new THREE.Sprite(new THREE.SpriteMaterial({map:tex,transparent:true,depthTest:false}));
 sprite.position.copy(pos);sprite.scale.set(.5,.14,1);models.add(sprite);}

function visualHole(h){return pdfView()?schematicPositions.display(h,exploreMode&&demoCompactView):h;}
function holePosition(wid,h){
 const s=sizes(),shown=visualHole(h),u=clamp(shown.u,0,1),y=s.h*clamp(shown.v,0,1);
 if(pdfView())return new THREE.Vector3(...pdfGeometry.hole(wid,shown,s));
 if(wid==='SW1')return new THREE.Vector3(-s.w/2+.024,y,-s.d/2+s.d*u);
 if(wid==='SW2')return new THREE.Vector3(-s.w/2+s.w*u,y,-s.d/2+.024);
 if(wid==='SW3')return new THREE.Vector3(s.w/2-.024,y,-s.d/2+s.d*u);
 return new THREE.Vector3(-s.w/2+s.w*u,y,s.d/2-.024);
}
function normal(wid){
 if(pdfView())return new THREE.Vector3(...pdfGeometry.wall(wid).normal);
 return new THREE.Vector3(wid==='SW1'?1:wid==='SW3'?-1:0,0,
  wid==='SW2'?1:wid==='SW4'?-1:0);
}
function openingRadius(h){return pipeDimensions.apertureMm(h)/2000;}
function drawOpening(w,h){
 const pos=holePosition(w.id,h),n=normal(w.id),r=openingRadius(h),selected=w.id===wallId&&h.id===holeId;
 const group=new THREE.Group();group.position.copy(pos);group.quaternion.setFromUnitVectors(new THREE.Vector3(0,0,1),n);
 // Invisible picking disc INSIDE the real hole. It must not visually fill it.
 const circle=new THREE.Mesh(new THREE.CircleGeometry(r,32),new THREE.MeshBasicMaterial({
 color:'#506269',colorWrite:false,depthWrite:false,side:THREE.DoubleSide}));
 group.add(circle);circle.userData={type:'hole',wall:w.id,hole:h.id};
 // No projecting cylinder, metal collar or pipe stub: the concrete wall
 // itself contains the opening and the bore has its own shaded material.
 const occupants=project.sourceInfo?.sourceVersion?.startsWith('PDF V2')?
 project.cables.filter(c=>[c.a,c.b].some(e=>e?.wall===w.id&&e?.hole===h.id)):[];
 if(occupants.length&&pdfView()){
  const segments=new Map();
  for(const c of occupants)segments.set(c.color,(segments.get(c.color)||0)+1);
  let cursor=-Math.PI/2;
  for(const [color,count] of segments){
   const arc=2*Math.PI*count/occupants.length;
   const part=new THREE.Mesh(new THREE.TorusGeometry(r,exploreMode?.006:.004,6,32,Math.max(.005,arc-.025)),
    new THREE.MeshBasicMaterial({color}));
   part.rotation.z=cursor;part.position.z=.004;group.add(part);cursor+=arc;
  }
 }else{
  const ring=new THREE.Mesh(new THREE.TorusGeometry(r,exploreMode?.006:.004,6,32),
   new THREE.MeshBasicMaterial({color:exploreMode?'#426f7c':'#cad6d9'}));
  ring.position.z=.004;group.add(ring);
 }
 if(selected){
  const focus=new THREE.Mesh(new THREE.TorusGeometry(r+.011,.004,6,32),
   new THREE.MeshBasicMaterial({color:'#f3bb48'}));
  focus.position.z=.005;group.add(focus);
 }
 if(visibleClearance?.invalidIds.has(w.id+'|'+h.id)){
  // KAB-0048: visibly disclose compact-preview collisions; do not move any pipe.
  const conflict=new THREE.Mesh(new THREE.TorusGeometry(r+.014,.008,8,36),
   new THREE.MeshBasicMaterial({color:'#d91e2b',depthTest:false}));
  conflict.position.z=.010;group.add(conflict);
 }
 models.add(group);hitObjects.push(circle);
}
function highlightBox(mesh,color='#eec657'){
 const edges=new THREE.LineSegments(new THREE.EdgesGeometry(mesh.geometry),
 new THREE.LineBasicMaterial({color,depthTest:false,transparent:true,opacity:.9}));
 edges.position.copy(mesh.position);models.add(edges);
}
function drawHousing(key){
 const e=project.enclosures[key];
 if(!e?.visible||viewMode.startsWith('SW')||
  (exploreMode&&exploreProtectionGuide&&avlReference())||
  (exploreMode?(exploreConstructionMode==='hidden'||
    (exploreSystemFocus!=='all'&&exploreSystemFocus!==key)):!project.showBox))return;
 for(const p of e.panels){
  if(!p.visible)continue;
  const g=components.panelGeometry(e,p),baseOpacity=(p.opacity/100)*(e.opacity/100);
  const opacity=exploreMode?
   (exploreConstructionMode==='transparent'?Math.min(.32,Math.max(.18,baseOpacity)):.95):
   baseOpacity;
  const mat=p.material.toLowerCase(),color=mat.includes('stahl')?'#779ba9':
    mat.includes('beton')?'#b1b9be':mat.includes('holz')?'#bd9266':
    mat.includes('brand')?'#d1a9ac':'#b5c5cc';
  const mesh=box(g.w/1000,g.h/1000,g.d/1000,g.x/1000,g.y/1000,g.z/1000,
    color,opacity);
  mesh.userData={type:'panel',id:p.id,group:key};
  hitObjects.push(mesh);
  if(!exploreMode&&p.id===panelKey)highlightBox(mesh);
 }
}
function drawSupports(){
 if(exploreMode&&(exploreConstructionMode==='hidden'||exploreSystemFocus!=='all'))return;
 // Supports have no verified OLA/MS assignment: hide in single-system view.
 for(const s of project.supports){
  if(!s.visible||(viewMode.startsWith('SW')&&viewMode!==s.wall))continue;
  const mesh=box(s.w/1000,s.h/1000,s.d/1000,
  s.x/1000,s.y/1000,s.z/1000,s.type==='Auflager'?'#c79a5b':'#5c83b0',.9);
  mesh.userData={type:'support',id:s.id};hitObjects.push(mesh);
  if(s.id===supportId)highlightBox(mesh,'#f3c756');
 }
}
function drawWalls(){
 const s=sizes(),op=exploreMode?
  (avlReference()? .94 :
   (exploreConstructionMode==='hidden'?Math.max(.52,project.opacity/100):.24)):
  project.opacity/100;
 // Schematic reference has a concrete shell, not ghostly translucent pipe rings.
 box(s.w+.16,.085,s.d+.16,0,-.055,0,'#747a79',exploreMode?.9:1);
 const floor=new THREE.GridHelper(Math.max(s.w,s.d)*1.7,18,'#d7e1e5','#e3eaed');
 floor.position.y=-.096;models.add(floor);
 for(const w of project.walls){
 if(!w.visible||(viewMode.startsWith('SW')&&w.id!==viewMode)||
  (exploreMode&&viewMode==='3D'&&exploreCutawayWall===w.id))continue;
 const span=pdfView()?pdfWallWidth(w.id,s):(w.id==='SW2'||w.id==='SW4')?s.w:s.d;
 const candidates=w.holes.map(h=>{
  const shown=visualHole(h),p=holePosition(w.id,h);
  return {id:h.id,u:pdfView()?(shown.u-.5)*span:
   w.id==='SW2'?-p.x:w.id==='SW3'?-p.z:w.id==='SW4'?p.x:p.z,
   y:p.y,r:openingRadius(h)};
 });
 const concrete=concreteWall.build(THREE,{
  width:span,height:s.h,depth:w.thickness/1000,openings:candidates,opacity:op
 });
 const mesh=concrete.mesh;
 if(pdfView()){
  const transform=pdfGeometry.transform(w.id,s);
  mesh.rotation.y=transform.rotationY;
  mesh.position.set(...transform.position);
 }else{
  if(w.id==='SW1'){mesh.rotation.y=-Math.PI/2;mesh.position.x=-s.w/2;}
  else if(w.id==='SW3'){mesh.rotation.y=Math.PI/2;mesh.position.x=s.w/2;}
  else if(w.id==='SW2'){mesh.rotation.y=Math.PI;mesh.position.z=-s.d/2;}
  else mesh.position.z=s.d/2;
 }
 mesh.userData={type:'wall',wall:w.id,apertures:concrete.accepted.length,
  excludedApertures:concrete.excluded.length};
 models.add(mesh);wallObjects.push(mesh);
 if(project.showLabels)createLabel(w.id,pdfView()?
  new THREE.Vector3(w.id==='SW2'?s.w/2:w.id==='SW4'?-s.w/2:0,
   s.h+.15,w.id==='SW1'?-s.d/2:w.id==='SW3'?s.d/2:0):
  new THREE.Vector3(w.id==='SW1'?-s.w/2:w.id==='SW3'?s.w/2:0,
   s.h+.15,w.id==='SW2'?-s.d/2:w.id==='SW4'?s.d/2:0));
 for(const h of w.holes)drawOpening(w,h);
 }
 drawHousing('OLA');drawHousing('MS');drawSupports();
}
function endpoint(r){if(!r)return null;const w=project.walls.find(x=>x.id===r.wall),h=w?.holes.find(x=>x.id===r.hole);return h?holePosition(w.id,h):null;}
function cableDisplayPoint(ref,c){
 const pos=endpoint(ref);
 if(!pos||!pdfView())return pos;
 const w=project.walls.find(w=>w.id===ref.wall),hole=w?.holes.find(h=>h.id===ref.hole);
 if(!hole)return pos;
 const peers=project.cables.filter(other=>
  [other.a,other.b].some(e=>e?.wall===ref.wall&&e?.hole===ref.hole));
 const slot=peers.findIndex(other=>other.id===c.id);
 if(slot<0||peers.length<2)return pos;
 // Visual fan-out of multi-cable opening: actual physical positions are unknown.
 const a=slot*2.39996323,r=openingRadius(hole)*.55*Math.sqrt((slot+.5)/peers.length);
 const delta=pdfView()?new THREE.Vector3(...pdfGeometry.tangentOffset(ref.wall,a,r)):
  (['SW1','SW3'].includes(ref.wall)?
   new THREE.Vector3(0,Math.sin(a)*r,Math.cos(a)*r):
   new THREE.Vector3(Math.cos(a)*r,Math.sin(a)*r,0));
 return pos.clone().add(delta);
}
/* Shared render path for both real cable drawing and the explicitly schematic
   3D protection-route guide. It does not establish a manufacturer bend radius. */
function cableRenderPath(c,a,b){
 const route=(c.route||[]).map(p=>new THREE.Vector3(p[0]/1000,p[1]/1000,p[2]/1000));
 const auto=exploreMode&&avlReference()&&c.drawingDiameterOnly&&['OLA','MS'].includes(c.group);
 const spec=constructionV2.cablePath(a.toArray(),b.toArray(),c.a.wall,c.b.wall,
  normal(c.a.wall).toArray(),normal(c.b.wall).toArray(),auto?[]:route.map(p=>p.toArray()),
  project.network?.quality?.minBendRadiusMm??null);
 let curve;
 if(spec.kind==='straight')curve=new THREE.LineCurve3(a.clone(),b.clone());
 else if(spec.kind==='smooth-side')curve=new THREE.CubicBezierCurve3(...spec.points.map(p=>new THREE.Vector3(...p)));
 else curve=new THREE.CatmullRomCurve3(spec.points.map(p=>new THREE.Vector3(...p)),false,'centripetal',.5);
 return{curve,route,routeKind:spec.kind,routeWarnings:spec.warnings};
}
/* Version 2.0: reference-only construction visualization. Unknown endpoints,
   manufacturer radii and unmeasured shaft geometry are NEVER treated as verified. */
function avlConstructionPlan(group){
 const cs=project.cables.filter(c=>c.group===group);
 const cables=cs.map(c=>{
  const a=cableDisplayPoint(c.a,c),b=cableDisplayPoint(c.b,c);
  return{a:a?.toArray()||null,b:b?.toArray()||null,
   wallA:c.a?.wall,wallB:c.b?.wall,refA:c.a?.hole,refB:c.b?.hole,
   radius:Math.max(.009,Math.min(.04,(Number(c.diameter)||12)/2000))};
 });
 const ports=project.walls.flatMap(w=>w.holes.map(h=>({
  id:h.id,wall:w.id,point:holePosition(w.id,h).toArray(),radius:openingRadius(h)})));
 return constructionV2.construction(group,cables,ports,sizes());
}
function drawAvlEndPanel(panel,plan,color,opacity){
 const axis=panel.axis,cross=axis===0?2:0;
 const cx=(panel.low[cross]+panel.high[cross])/2,cy=(panel.low[1]+panel.high[1])/2;
 const w=panel.high[cross]-panel.low[cross],h=panel.high[1]-panel.low[1];
 const shape=new THREE.Shape();
 shape.moveTo(-w/2,-h/2);shape.lineTo(w/2,-h/2);
 shape.lineTo(w/2,h/2);shape.lineTo(-w/2,h/2);shape.closePath();
 for(const hole of panel.holes){
  const x=hole.at[cross]-cx,y=hole.at[1]-cy;
  if(Math.abs(x)+hole.radius>w/2||Math.abs(y)+hole.radius>h/2)continue;
  const opening=new THREE.Path();
  opening.absarc(x,y,hole.radius,0,Math.PI*2,true);shape.holes.push(opening);
 }
 const thick=panel.high[axis]-panel.low[axis];
 const mesh=new THREE.Mesh(new THREE.ExtrudeGeometry(shape,
  {depth:thick,steps:1,bevelEnabled:false,curveSegments:24}),material(color,opacity));
 if(axis===2)mesh.position.set(cx,cy,panel.low[2]);
 else{mesh.rotation.y=-Math.PI/2;mesh.position.set(panel.high[0],cy,cx);}
 mesh.userData={type:'concept-only',group:plan.group};models.add(mesh);
}
function drawAvlConstruction(plan){
 if(plan.status!=='concept')return;
 const color=plan.group==='OLA'?'#c6b9a1':'#acbfca';
 const opacity=exploreConstructionMode==='solid'?.91:.29;
 for(const p of plan.panels){
  if(p.kind==='perforated'){drawAvlEndPanel(p,plan,color,opacity);continue;}
  const mid=p.low.map((v,i)=>(v+p.high[i])/2);
  const size=p.low.map((v,i)=>p.high[i]-v);
  const mesh=box(size[0],size[1],size[2],...mid,color,opacity);
  mesh.userData={type:'concept-only',group:plan.group};
 }
 for(const p of plan.supports){
  const mid=p.low.map((v,i)=>(v+p.high[i])/2);
  const size=p.low.map((v,i)=>p.high[i]-v);
  const mesh=box(size[0],size[1],size[2],...mid,
   p.kind==='stack'?'#aeb9be':'#7d939e',.94);
  mesh.userData={type:'concept-only',group:plan.group};
 }
}
function drawPdfProtectionGuide(){
 if(!exploreMode||!avlReference()||!exploreProtectionGuide||viewMode!=='3D')return;
 for(const group of ['MS','OLA']){
  if(exploreSystemFocus!=='all'&&exploreSystemFocus!==group)continue;
  const plan=avlConstructionPlan(group);
  if(exploreConstructionMode!=='hidden')drawAvlConstruction(plan);
  for(const c of project.cables.filter(c=>c.group===group)){
   const a=cableDisplayPoint(c.a,c),b=cableDisplayPoint(c.b,c);
   const color=group==='MS'?'#2b8eef':'#ed4151';
   if(a&&b){
    if(plan.status==='concept')continue;
    const {curve}=cableRenderPath(c,a,b);
    const shell=new THREE.Mesh(new THREE.TubeGeometry(curve,64,.055,8,false),
     new THREE.MeshBasicMaterial({color,wireframe:true,transparent:true,
      opacity:.34,depthTest:false,depthWrite:false}));
    shell.userData={type:'concept-only',group};models.add(shell);
   }else{
    const ref=a?c.a:b?c.b:null;if(!ref)continue;
    const w=project.walls.find(x=>x.id===ref.wall);
    const hole=w?.holes.find(h=>h.id===ref.hole);if(!hole)continue;
    const ring=new THREE.Mesh(new THREE.TorusGeometry(openingRadius(hole)+.045,
     .008,8,32),new THREE.MeshBasicMaterial({color,transparent:true,
     opacity:.9,depthTest:false,depthWrite:false}));
    ring.quaternion.setFromUnitVectors(new THREE.Vector3(0,0,1),normal(ref.wall));
    ring.position.copy(endpoint(ref)).addScaledVector(normal(ref.wall),.065);
    ring.userData={type:'concept-only',group};models.add(ring);
   }
  }
 }
}
function drawCable(c){
 if(exploreMode&&exploreSystemFocus!=='all'&&c.group!==exploreSystemFocus)return;
 const a=cableDisplayPoint(c.a,c),b=cableDisplayPoint(c.b,c);
 if(!a||!b){
  if(!project.sourceInfo?.sourceVersion?.startsWith('PDF V2')||!(a||b))return;
  // Incomplete: only show a short stub, NOT an invented full cable route.
  const known=c.a&&a?c.a:c.b,start=a||b;
  if(viewMode.startsWith('SW')&&known.wall!==viewMode)return;
  const end=start.clone().addScaledVector(normal(known.wall),.36);
  const selected=c.id===cableId;
  const stub=new THREE.Mesh(new THREE.TubeGeometry(new THREE.LineCurve3(start,end),12,
   selected?.019:.012,9,false),
   new THREE.MeshBasicMaterial({color:c.color,transparent:true,
    opacity:selected?1:.78,depthWrite:false}));
  stub.userData={type:'cable',id:c.id};models.add(stub);hitObjects.push(stub);
  const cap=new THREE.Mesh(new THREE.SphereGeometry(.027,12,10),
   new THREE.MeshBasicMaterial({color:'#f0ad36'}));
  cap.position.copy(end);models.add(cap);
  return;
 }
 if(viewMode.startsWith('SW')&&c.a.wall!==viewMode&&c.b.wall!==viewMode)return;
 const chosen=c.id===cableId;
 const thickness=Math.max(.004,(Number(c.diameter)||12)/2000);
 const {curve,route}=cableRenderPath(c,a,b);
 const cableRadius=exploreMode?
  (pdfView()?(chosen?.022:.014):Math.max(.006,thickness)*(chosen?1.75:1.08)):
  chosen?thickness*1.22:thickness;
 const cableMaterial=exploreMode?
 new THREE.MeshBasicMaterial({color:c.color,transparent:true,
  opacity:chosen?1:(pdfView()?.94:(exploreConstructionMode==='hidden'?.34:.76)),depthWrite:false}):
 new THREE.MeshStandardMaterial({color:c.color,roughness:.34,metalness:.04,transparent:true,opacity:chosen?1:.88});
 const line=new THREE.Mesh(new THREE.TubeGeometry(curve,Math.min(100,48+route.length*10),cableRadius,10,false),
 cableMaterial);
 line.userData={type:'cable',id:c.id};models.add(line);hitObjects.push(line);
 if(chosen&&!exploreMode){
 for(const [key,vec] of [['a',a],['b',b]]){
 const radius=Math.max(.019,thickness*2.3);
 const sphere=new THREE.Mesh(new THREE.SphereGeometry(radius,16,12),new THREE.MeshBasicMaterial({color:'#ffffff'}));
 sphere.position.copy(vec);sphere.userData={type:'handle',id:c.id,end:key};models.add(sphere);hitObjects.push(sphere);
 const outline=new THREE.Mesh(new THREE.SphereGeometry(radius*1.2,12,10),new THREE.MeshBasicMaterial({color:c.color,wireframe:true}));
 outline.position.copy(vec);models.add(outline);
 }
 for(let i=0;i<route.length;i++){
 const point=new THREE.Mesh(new THREE.SphereGeometry(Math.max(.026,thickness*2.6),16,12),
 new THREE.MeshBasicMaterial({color:'#f5c467'}));
 point.position.copy(route[i]);point.userData={type:'waypoint',id:c.id,index:i};models.add(point);hitObjects.push(point);
 }
 }
}
function rebuild(){disposeModel();visibleClearance=qualityModel.auditWallOpenings(project,visualHole);
 drawWalls();project.cables.forEach(drawCable);drawPdfProtectionGuide();view();draw2D();}
function renderAll(){
 fixSelection();renderUI();
 // The network pane toggles the 3D canvas's CSS display state. Restore it
 // BEFORE camera calculations and rebuilding scene meshes.
 renderNetworkUI();rebuild();renderQualityUI();renderExplorerUI();drawNetwork();
}
function pick(event,targets=hitObjects){const rect=$('scene').getBoundingClientRect();
 pointer.x=((event.clientX-rect.left)/rect.width)*2-1;pointer.y=-((event.clientY-rect.top)/rect.height)*2+1;
 raycaster.setFromCamera(pointer,activeCamera);return raycaster.intersectObjects(targets,false)[0]||null;}
function wallCoords(wid,p){
 const s=sizes();
 const u=pdfView()?pdfGeometry.inverse(wid,[p.x,p.y,p.z],s):
  (wid==='SW2'||wid==='SW4')?p.x/s.w+.5:p.z/s.d+.5;
 return {u:clamp(u,0,1),v:clamp(p.y/s.h,0,1)};
}
function newOpening(wid,point){const w=project.walls.find(x=>x.id===wid),coords=wallCoords(wid,point),id=uid(wid+'-free');
 change(()=>{w.holes.push({id,row:0,col:0,dn:w.dn,...coords});wallId=wid;holeId=id;});}



/* Review metadata and project-controlled geometry checks: NOT normative approvals. */
let lastQualityResult=null;
function renderQualityUI(){
 const net=project.network,cfg=net.quality||qualityModel.normalizeSettings();
 for(const [id,key] of [['minPipeGap','minPipeGapMm'],['minEdgeGap','minEdgeMm'],['minBend','minBendRadiusMm']]){
  $(id).value=cfg[key]===null?'':cfg[key];
 }
 const query=$('globalSearch').value.trim();
 const hits=qualityModel.searchNetwork(net,query,90);
 $('globalResults').innerHTML=query?
  (hits.map((x,i)=>'<div class="search-hit" data-result="'+i+'"><b>'+esc(x.kind==='shaft'?'Schacht':x.kind==='cable'?'Kabel':'Leerrohr')+'</b> · '+
  esc(x.label)+'</div>').join('')||'<div class="small">Keine Treffer im Netz.</div>'):
  '<div class="small">Schacht, Kabel oder Leerrohr im gesamten Projekt finden.</div>';
 $('globalResults').dataset.results=JSON.stringify(hits);
 lastQualityResult=qualityModel.evaluate(net,cfg);
 const counts=lastQualityResult.counts;
 $('qualitySummary').textContent=counts.error+' geometrische/Datenfehler · '+counts.warning+
 ' Warnungen · '+counts.info+' Hinweise.\nKeine fachliche DIN/VDE- oder Brandschutzfreigabe.';
 $('qualitySummary').style.background=counts.error||counts.warning?'#fff1e7':'#edf8f1';
 $('qualityList').innerHTML=lastQualityResult.issues.slice(0,28).map((issue,i)=>
  '<div class="quality-hit '+issue.severity+'" data-quality="'+i+'"><b>'+
  esc(issue.code)+'</b> · '+esc(issue.detail)+'</div>').join('')||
  '<div class="small">Keine Hinweise im aktuellen, eingeschränkten Prüfumfang – keine Fachfreigabe.</div>';
 const options='<option value="shaft">Schacht · '+esc(project.activeShaftId)+'</option>'+
 project.cables.map(c=>'<option value="cable:'+esc(c.id)+'">Kabel · '+esc(c.name)+'</option>').join('');
 $('reviewTarget').innerHTML=options;
 if(!['shaft',...project.cables.map(c=>'cable:'+c.id)].includes(reviewKey))reviewKey='shaft';
 $('reviewTarget').value=reviewKey;
 const entityType=reviewKey==='shaft'?'shaft':'cable',
 entityId=entityType==='shaft'?project.activeShaftId:reviewKey.slice(6);
 const entry=(net.reviews||[]).find(x=>x.shaftId===project.activeShaftId&&
  x.entityType===entityType&&x.entityId===entityId);
 $('reviewStatus').value=entry?.status||'Entwurf';
 $('reviewSource').value=entry?.source||'';
 $('reviewBy').value=entry?.checkedBy||'';
 $('reviewNote').value=entry?.note||'';
 $('auditLast').textContent=net.audit?.length?
  'Letzter protokollierter Vorgang: '+net.audit[net.audit.length-1].action+' ('+
  net.audit[net.audit.length-1].at+')':
  'Noch kein gespeicherter Änderungsvorgang.';
}

/* Interactive network overview: global X/Z shaft positions in millimetres. */
const networkCanvas=$('networkCanvas');
const networkPos=e=>{const r=networkCanvas.getBoundingClientRect();return {x:e.clientX-r.left,y:e.clientY-r.top};};
const activeShaft=()=>project.network.shafts.find(s=>s.id===project.activeShaftId);
const selectedRoute=()=>project.network.routes.find(r=>r.id===netRouteId);
function switchShaft(id,selectedCable=null){
 if(!project.network.shafts.some(s=>s.id===id))return;
 if(id===project.activeShaftId){
  if(selectedCable&&project.cables.some(c=>c.id===selectedCable)){cableId=selectedCable;filter='all';renderAll();}
  return;
 }
 change(()=>{
  syncActiveShaft();
  const net=project.network,next=net.shafts.find(s=>s.id===id);
  project=validProject({...next.snapshot,network:net,activeShaftId:id});
  wallId='SW1';holeId=null;cableId=selectedCable||project.cables[0]?.id||null;
  panelKey='OLA-floor';supportId=null;variantId=null;filter='all';
  viewMode='3D';zoom=1;mapZoom=Math.max(.35,mapZoom);
 });
}
function renderNetworkUI(){
 const net=project.network,shaft=activeShaft();
 $('viewport').classList.toggle('network-open',networkOpen);
 $('toggleNetwork').textContent=networkOpen?'↩ Zum Editor':'◎ Netz anzeigen';
 const opts=net.shafts.map(s=>'<option value="'+esc(s.id)+'">'+esc(s.id)+' · '+esc(s.name)+'</option>').join('');
 for(const id of ['netShaftSelect','netLinkFrom','netLinkTo','netSegmentShaft'])$(id).innerHTML=opts;
 $('netShaftSelect').value=project.activeShaftId;
 $('netShaftName').value=shaft?.name||'';
 $('netShaftX').value=shaft?.x??0;$('netShaftZ').value=shaft?.z??0;
 $('netDeleteShaft').disabled=net.shafts.length<=1;
 if(!net.shafts.some(s=>s.id===netLinkFromId))netLinkFromId=project.activeShaftId;
 if(!net.shafts.some(s=>s.id===netLinkToId))netLinkToId=net.shafts.find(s=>s.id!==netLinkFromId)?.id||netLinkFromId;
 $('netLinkFrom').value=netLinkFromId;$('netLinkTo').value=netLinkToId;
 $('netLinkSelect').innerHTML='<option value="">– Verbindung auswählen –</option>'+net.links.map(l=>
 '<option value="'+esc(l.id)+'">'+esc(l.from)+' ↔ '+esc(l.to)+'</option>').join('');
 if(!net.links.some(l=>l.id===netLinkId))netLinkId=null;
 $('netLinkSelect').value=netLinkId||'';$('netDeleteLink').disabled=!netLinkId;
 $('netAddLink').disabled=net.shafts.length<2||netLinkFromId===netLinkToId;
 $('netRouteSelect').innerHTML='<option value="">– Kabelweg auswählen –</option>'+net.routes.map(r=>
 '<option value="'+esc(r.id)+'">'+esc(r.name)+'</option>').join('');
 if(!net.routes.some(r=>r.id===netRouteId))netRouteId=net.routes[0]?.id||null;
 $('netRouteSelect').value=netRouteId||'';
 const route=selectedRoute();
 $('routeFields').hidden=!route;$('netDeleteRoute').disabled=!route;
 $('netShowTrace').disabled=!route;
 if(route){
  $('netRouteName').value=route.name;$('netRouteGroup').value=route.group;
  $('netTraceList').innerHTML=networkModel.routeDetails(net,route).map(s=>
   '<div class="trace-item" data-trace-shaft="'+esc(s.shaftId)+'" data-trace-cable="'+esc(s.cableId)+'">'+
   '<b>'+s.order+'. '+esc(s.shaft)+'</b> · '+esc(s.cable)+
   '<div class="small">'+esc(s.from)+' → '+esc(s.to)+'</div></div>').join('')||
   '<div class="small">Noch keine lokalen Kabelsegmente eingetragen.</div>';
  const problems=networkModel.issues(net,route);
  $('netTraceStatus').textContent=problems.length?'⚠ Noch nicht durchgehend:\n'+problems.join('\n'):
   '✓ Alle Kabel-IDs vorhanden und alle Schachtübergänge im Netz verbunden. Kein Nachweis der realen Kabelführung!';
  $('netTraceStatus').style.background=problems.length?'#fff1e7':'#edf8f1';
 }
 if(!net.shafts.some(s=>s.id===netSegmentShaftId))netSegmentShaftId=project.activeShaftId;
 $('netSegmentShaft').value=netSegmentShaftId;
 const segmentShaft=net.shafts.find(s=>s.id===netSegmentShaftId);
 const old=$('netSegmentCable').value;
 $('netSegmentCable').innerHTML=(segmentShaft?.snapshot.cables||[]).map(c=>
 '<option value="'+esc(c.id)+'">'+esc(c.name)+' · '+esc(c.id)+'</option>').join('');
 if((segmentShaft?.snapshot.cables||[]).some(c=>c.id===old))$('netSegmentCable').value=old;
 $('netAppendSegment').disabled=!route||!segmentShaft?.snapshot.cables.length||route.steps.length>=60;
 $('netPopSegment').disabled=!route||!route.steps.length;
 $('netMapSummary').textContent=net.shafts.length+' Schächte · '+net.links.length+
 ' Verbindungen · '+net.routes.length+' Netz-Kabelwege'+(route?' · '+route.name:'');
}
function networkProjection(){
 const r=networkCanvas.getBoundingClientRect(),w=Math.max(1,r.width),h=Math.max(1,r.height);
 const ss=project.network.shafts,xs=ss.map(s=>s.x),zs=ss.map(s=>s.z);
 const xmin=Math.min(...xs),xmax=Math.max(...xs),zmin=Math.min(...zs),zmax=Math.max(...zs);
 const cx=(xmin+xmax)/2,cz=(zmin+zmax)/2;
 const sx=Math.max(10000,xmax-xmin),sz=Math.max(10000,zmax-zmin);
 const scale=Math.min(Math.max(1,w-150)/sx,Math.max(1,h-150)/sz)*mapZoom;
 const point=s=>({x:w/2+(s.x-cx)*scale+mapOffsetX,y:h/2+(s.z-cz)*scale+mapOffsetY});
 return {w,h,point,scale,cx,cz};
}
function drawNetwork(){
 if(!networkOpen)return;
 const {w,h,point,scale}=networkProjection();if(w<6||h<6)return;
 const dpr=Math.min(window.devicePixelRatio||1,2),pw=Math.round(w*dpr),ph=Math.round(h*dpr);
 if(networkCanvas.width!==pw||networkCanvas.height!==ph){networkCanvas.width=pw;networkCanvas.height=ph;}
 const ctx=networkCanvas.getContext('2d');ctx.setTransform(dpr,0,0,dpr,0,0);
 ctx.clearRect(0,0,w,h);ctx.fillStyle='#fafdfe';ctx.fillRect(0,0,w,h);
 const net=project.network;
 ctx.strokeStyle='#e8eff2';ctx.lineWidth=1;
 for(let x=25;x<w;x+=50){ctx.beginPath();ctx.moveTo(x,0);ctx.lineTo(x,h);ctx.stroke();}
 for(let y=25;y<h;y+=50){ctx.beginPath();ctx.moveTo(0,y);ctx.lineTo(w,y);ctx.stroke();}
 const byID=id=>net.shafts.find(s=>s.id===id);
 for(const link of net.links){
  const a=byID(link.from),b=byID(link.to);if(!a||!b)continue;
  const p=point(a),q=point(b);
  ctx.strokeStyle='#92a5af';ctx.lineWidth=5;ctx.beginPath();ctx.moveTo(p.x,p.y);ctx.lineTo(q.x,q.y);ctx.stroke();
 }
 const route=selectedRoute();
 if(route){
  const colors={OLA:'#d75c60',MS:'#3298bb',Weitere:'#b98b34'};
  ctx.strokeStyle=colors[route.group]||'#b98b34';ctx.lineWidth=6;ctx.setLineDash([10,6]);
  for(let i=1;i<route.steps.length;i++){
   const a=byID(route.steps[i-1].shaftId),b=byID(route.steps[i].shaftId);
   if(!a||!b)continue;
   const p=point(a),q=point(b);
   ctx.beginPath();ctx.moveTo(p.x,p.y);ctx.lineTo(q.x,q.y);ctx.stroke();
  }
  ctx.setLineDash([]);
 }
 for(const s of net.shafts){
  const p=point(s),chosen=s.id===project.activeShaftId,
   traced=route?.steps.some(t=>t.shaftId===s.id);
  ctx.beginPath();ctx.arc(p.x,p.y,chosen?24:20,0,Math.PI*2);
  ctx.fillStyle=chosen?'#185b53':traced?'#d7a44d':'#527b8b';ctx.fill();
  ctx.strokeStyle='#ffffff';ctx.lineWidth=3;ctx.stroke();
  ctx.textAlign='center';ctx.font='bold 12px Segoe UI,Arial';ctx.fillStyle='#ffffff';
  ctx.fillText(s.id.slice(0,6),p.x,p.y+4);
  ctx.font='bold 12px Segoe UI,Arial';ctx.fillStyle='#244452';
  ctx.fillText(s.name.slice(0,22),p.x,p.y-32);
  ctx.font='10px Segoe UI,Arial';ctx.fillStyle='#7a919d';
  ctx.fillText(s.x+' / '+s.z+' mm',p.x,p.y+38);
 }
 ctx.fillStyle='#597580';ctx.font='11px Segoe UI,Arial';ctx.textAlign='left';
 ctx.fillText('Globale X/Z-Koordinaten (mm) · schematischer Lageplan, keine Vermessungsfreigabe',12,h-12);
}

const draft=$('draftCanvas'),geom2d=window.Kabelsalat2D;
if(!geom2d)throw Error('Kabelsalat-2D-Modul fehlt');
let draftMetrics=null,draftDrag=null;
function projectedCablePoints(c,m){
 const a=endpoint(c.a),b=endpoint(c.b);
 if(!a||!b)return [];
 const points=[a,...(c.route||[]).map(p=>new THREE.Vector3(p[0]/1000,p[1]/1000,p[2]/1000)),b];
 return points.map(p=>{
  const along=pdfView()?pdfGeometry.along(wallId,[p.x,p.y,p.z],sizes())*1000:
  (wallId==='SW1'||wallId==='SW3'?
    p.z*1000+project.dims.d/2:p.x*1000+project.dims.w/2);
  return geom2d.toPixels(m,along,p.y*1000);
 });
}
function distanceSegment(x,y,a,b){
 const dx=b.x-a.x,dy=b.y-a.y,den=dx*dx+dy*dy;
 if(den<.0001)return Math.hypot(x-a.x,y-a.y);
 const t=clamp(((x-a.x)*dx+(y-a.y)*dy)/den,0,1);
 return Math.hypot(x-a.x-t*dx,y-a.y-t*dy);
}
function draw2D(){
 if(!$('viewport').classList.contains('with-2d'))return;
 const box=draft.getBoundingClientRect(),cw=box.width,ch=box.height;
 if(cw<8||ch<8)return;
 const dpr=Math.min(2,window.devicePixelRatio||1);
 const pxw=Math.max(1,Math.round(cw*dpr)),pxh=Math.max(1,Math.round(ch*dpr));
 if(draft.width!==pxw||draft.height!==pxh){draft.width=pxw;draft.height=pxh;}
 const ctx=draft.getContext('2d');
 ctx.setTransform(dpr,0,0,dpr,0,0);
 ctx.clearRect(0,0,cw,ch);ctx.fillStyle='#fafdfe';ctx.fillRect(0,0,cw,ch);
 const w=wall(),span=wallSpanMm(wallId),height=project.dims.h,m=geom2d.metrics(span,height,cw,ch);
 draftMetrics=m;$('draftWallSelect').value=wallId;
 const p1=geom2d.toPixels(m,0,0),p2=geom2d.toPixels(m,span,height);
 ctx.save();ctx.strokeStyle='#e8f0f2';ctx.lineWidth=1;
 const step=span>6000?1000:span>3000?500:250;
 for(let x=0;x<=span;x+=step){const p=geom2d.toPixels(m,x,0);ctx.beginPath();ctx.moveTo(p.x,p2.y);ctx.lineTo(p.x,p1.y);ctx.stroke();}
 for(let y=0;y<=height;y+=step){const p=geom2d.toPixels(m,0,y);ctx.beginPath();ctx.moveTo(p1.x,p.y);ctx.lineTo(p2.x,p.y);ctx.stroke();}
 ctx.strokeStyle='#6c8591';ctx.lineWidth=2;ctx.strokeRect(p1.x,p2.y,p2.x-p1.x,p1.y-p2.y);
 ctx.font='11px Segoe UI,Arial';ctx.fillStyle='#607d89';ctx.textAlign='center';
 ctx.fillText(span+' mm',cw/2,Math.min(ch-7,p1.y+18));
 ctx.save();ctx.translate(Math.max(11,p1.x-13),ch/2);ctx.rotate(-Math.PI/2);ctx.fillText(height+' mm',0,0);ctx.restore();
 // Orthographic projection for visual cross-check only; not a verified cable route.
 for(const c of project.cables){
  if(c.a?.wall!==wallId&&c.b?.wall!==wallId)continue;
  const projected=projectedCablePoints(c,m);
  if(projected.length<2)continue;
  ctx.beginPath();projected.forEach((p,i)=>i?ctx.lineTo(p.x,p.y):ctx.moveTo(p.x,p.y));
  ctx.strokeStyle=c.color;ctx.lineWidth=c.id===cableId?3.8:1.4;
  ctx.globalAlpha=c.id===cableId?.95:.3;ctx.setLineDash(c.id===cableId?[]:[5,4]);
  ctx.stroke();ctx.globalAlpha=1;ctx.setLineDash([]);
 }
 for(const h of w.holes){
 const shown=visualHole(h),p=geom2d.toPixels(m,shown.u*span,shown.v*height);
 const r=Math.max(2,pipeDimensions.apertureMm(h)/2*m.scale),selected=h.id===holeId;
 ctx.beginPath();ctx.arc(p.x,p.y,r,0,Math.PI*2);
 ctx.fillStyle=selected?'#b7e9d7':'#e5eff2';ctx.fill();
 ctx.lineWidth=selected?3:1.5;ctx.strokeStyle=selected?'#18745e':'#708b97';ctx.stroke();
 if(visibleClearance?.invalidIds.has(wallId+'|'+h.id)){
  ctx.save();ctx.beginPath();ctx.arc(p.x,p.y,r+4,0,Math.PI*2);
  ctx.strokeStyle='#d91e2b';ctx.lineWidth=3;ctx.setLineDash([4,3]);ctx.stroke();ctx.restore();
 }
 if(project.sourceInfo?.sourceVersion?.startsWith('PDF V2')){
  const occupants=project.cables.filter(c=>[c.a,c.b].some(e=>e?.wall===wallId&&e?.hole===h.id));
  if(occupants.length){
   const segments=new Map();
   for(const c of occupants)segments.set(c.color,(segments.get(c.color)||0)+1);
   let cursor=-Math.PI/2;
   for(const [color,count] of segments){
    const arc=2*Math.PI*count/occupants.length;
    ctx.beginPath();ctx.arc(p.x,p.y,r+2,cursor,cursor+arc-.025);
    ctx.strokeStyle=color;ctx.lineWidth=3;ctx.stroke();cursor+=arc;
   }
  }
  ctx.font='bold 9px Segoe UI,Arial';ctx.textAlign='center';ctx.fillStyle='#1b3142';
  ctx.fillText(String(Number(h.id.split('_LR')[1])),p.x,p.y+3);
  if(cw>=295){
   ctx.font='8px Segoe UI,Arial';ctx.textAlign='center';ctx.fillStyle='#55707d';
   ctx.fillText('DN '+h.dn,p.x,p.y+r+11);
  }
  if(occupants.length){
   ctx.font='bold 9px Segoe UI,Arial';ctx.textAlign='left';ctx.fillStyle='#9a5718';
   ctx.fillText(String(occupants.length),p.x+r+3,p.y-r-3);
  }
 }
 if(cable()?.a?.wall===wallId&&cable()?.a?.hole===h.id||
 cable()?.b?.wall===wallId&&cable()?.b?.hole===h.id){
 ctx.beginPath();ctx.arc(p.x,p.y,r+5,0,Math.PI*2);ctx.strokeStyle=cable().color;
 ctx.lineWidth=2.5;ctx.stroke();
 }
 }
 for(const st of project.supports){
  if(!st.visible||st.wall!==wallId)continue;
  const x=st.wall==='SW1'||st.wall==='SW3'?st.z+project.dims.d/2:st.x+project.dims.w/2;
  const wmm=st.wall==='SW1'||st.wall==='SW3'?st.d:st.w;
  const centre=geom2d.toPixels(m,x,st.y);
  const bw=Math.max(4,wmm*m.scale),bh=Math.max(4,st.h*m.scale);
  ctx.fillStyle=st.type==='Auflager'?'#f7e3ba':'#d8e8f9';
  ctx.fillRect(centre.x-bw/2,centre.y-bh/2,bw,bh);
  ctx.strokeStyle=st.id===supportId?'#b75e2f':'#527c99';
  ctx.lineWidth=st.id===supportId?3:1.5;
  ctx.strokeRect(centre.x-bw/2,centre.y-bh/2,bw,bh);
  ctx.font='10px Segoe UI,Arial';ctx.fillStyle='#456377';
  ctx.textAlign='left';ctx.fillText(st.name.slice(0,16),centre.x+5,centre.y-5);
 }
 if(hole()){
 const h=hole(),pos=geom2d.holeMillimetres(h,span,height),p=geom2d.toPixels(m,pos.x,pos.y);
 ctx.font='bold 11px Segoe UI,Arial';ctx.textAlign='left';ctx.fillStyle='#1a655a';
 ctx.fillText('DN '+h.dn+(pipeDimensions.forHole(h).openingMm!==null?' · Ø '+pipeDimensions.forHole(h).openingMm:'') ,p.x+9,p.y-9);
 }
 ctx.restore();
 const clear=geom2d.clearance(w.holes,span,height),sel=hole();
 const pos=sel?geom2d.holeMillimetres(sel,span,height):null;
 $('draftCoords').textContent=sel?
 (project.sourceInfo?.sourceVersion?.startsWith('PDF V2')?
  w.id+' · '+sel.sourceAddress+' · schematische Lage · '+pipeDimensions.describe(sel):
  w.id+' · '+Math.round(pos.x)+' mm horizontal · '+Math.round(pos.y)+' mm Höhe · '+pipeDimensions.describe(sel)):
 w.id+' · '+w.holes.length+' Rohre · Mindest-Randabstand: '+(clear.minEdge===null?'–':clear.minEdge+' mm')+
 ' · kleinster Rohrabstand: '+(clear.minPairGap===null?'–':clear.minPairGap+' mm');
 $('draftCoords').style.background=clear.overlaps||clear.minEdge!==null&&clear.minEdge<0?'#fff1e7':'#edf7f3';
}


function copySelectedHole(){
 const w=wall(),selected=hole();if(!selected)return;
 if(w.holes.length>=600){alert('Maximal 600 Leerrohre je Wand.');return;}
 const span=wallSpanMm(w.id),height=project.dims.h,x=selected.u*span;
 const dx=x+100+(selected.dn||70)/2<=span?100:-100;
 const clone={id:uid(w.id+'-copy'),...geom2d.copyOffset(selected,span,height,dx,0)};
 if(selected.dimensionSpec){
  const old=pipeDimensions.forHole(selected);
  clone.dimensionSpec=pipeDimensions.normalize({...old,source:'',reviewer:'',
   status:'unverified',method:'manual',candidateMm:null,
   note:'Geometrische Kopie; Maß- und Quellbestätigung nicht mitkopiert'},clone.dn);
 }
 change(()=>{w.holes.push(clone);holeId=clone.id;});
}
function draftPoint(e){
 const rect=draft.getBoundingClientRect();
 return {x:e.clientX-rect.left,y:e.clientY-rect.top};
}
draft.addEventListener('pointerdown',e=>{
 if(e.button!==0||!draftMetrics)return;
 const p=draftPoint(e),selected=geom2d.hitHole(wall().holes.map(visualHole),draftMetrics,p.x,p.y);
 if(selected){
 holeId=selected.id;renderAll();
 if(exploreMode){e.preventDefault();return;}
 draftDrag={holeId:selected.id,wallId,changed:false,x:p.x,y:p.y};
 draft.setPointerCapture(e.pointerId);e.preventDefault();return;
 }
 const chosenSupport=project.supports.find(st=>{
 if(st.wall!==wallId||!st.visible)return false;
 const x=st.wall==='SW1'||st.wall==='SW3'?st.z+project.dims.d/2:st.x+project.dims.w/2;
 const wmm=st.wall==='SW1'||st.wall==='SW3'?st.d:st.w;
 const c=geom2d.toPixels(draftMetrics,x,st.y);
 return Math.abs(p.x-c.x)<Math.max(6,wmm*draftMetrics.scale/2)&&
   Math.abs(p.y-c.y)<Math.max(6,st.h*draftMetrics.scale/2);
 });
 if(chosenSupport){supportId=chosenSupport.id;renderAll();e.preventDefault();return;}
 let lineCable=null,best=9;
 for(const c of project.cables){
  if(c.a?.wall!==wallId&&c.b?.wall!==wallId)continue;
  const pts=projectedCablePoints(c,draftMetrics);
  for(let i=1;i<pts.length;i++){
   const d=distanceSegment(p.x,p.y,pts[i-1],pts[i]);
   if(d<best){best=d;lineCable=c;}
  }
 }
 if(lineCable){
  cableId=lineCable.id;filter='all';renderAll();e.preventDefault();return;
 }
 if(exploreMode||mode!=='add')return;
 const w=wall();if(w.holes.length>=600){alert('Maximal 600 Leerrohre je Wand.');return;}
 const mm=geom2d.toMillimetres(draftMetrics,p.x,p.y);
 const newHole={id:uid(w.id+'-2d'),row:0,col:0,u:mm.x/wallSpanMm(w.id),v:mm.y/project.dims.h,dn:w.dn};
 change(()=>{w.holes.push(newHole);holeId=newHole.id;});e.preventDefault();
});
draft.addEventListener('pointermove',e=>{
 if(exploreMode||!draftDrag||!draftMetrics)return;
 const p=draftPoint(e);
 if(!draftDrag.changed&&Math.hypot(p.x-draftDrag.x,p.y-draftDrag.y)<1)return;
 const w=project.walls.find(x=>x.id===draftDrag.wallId),h=w?.holes.find(x=>x.id===draftDrag.holeId);
 if(!h)return;
 if(!draftDrag.changed){undo.push(JSON.stringify(project));if(undo.length>80)undo.shift();redo.length=0;draftDrag.changed=true;}
 const mm=geom2d.toMillimetres(draftMetrics,p.x,p.y,1);
 h.u=mm.x/wallSpanMm(w.id);h.v=mm.y/project.dims.h;
 rebuild();e.preventDefault();
});
function finishDraftDrag(){if(draftDrag?.changed){renderAll();persist();}draftDrag=null;}
draft.addEventListener('pointerup',finishDraftDrag);
draft.addEventListener('pointercancel',finishDraftDrag);
$('draftWallSelect').addEventListener('change',e=>{wallId=e.target.value;holeId=null;if(viewMode.startsWith('SW'))viewMode='3D';renderAll();});
$('draftCopy').addEventListener('click',copySelectedHole);
$('copyHole').addEventListener('click',copySelectedHole);
$('draftAdd').addEventListener('click',()=>{mode='add';renderUI();});
$('splitEnabled').addEventListener('change',()=>{
 change(()=>{project.splitEnabled=$('splitEnabled').checked;});
 requestAnimationFrame(resize);
});

const canvas=$('scene');
canvas.addEventListener('pointerdown',e=>{
 if(e.button!==0)return;canvas.setPointerCapture(e.pointerId);const hit=pick(e),obj=hit?.object.userData||{};
 if(exploreMode){
  if(obj.type==='hole'){wallId=obj.wall;holeId=obj.hole;renderAll();return;}
  if(obj.type==='cable'){cableId=obj.id;filter='all';renderAll();return;}
  if(viewMode==='3D'){grab={type:'orbit',x:e.clientX,y:e.clientY};canvas.classList.add('dragging');}
  return;
 }
 if(mode==='add'){const wallHit=pick(e,wallObjects);if(wallHit){newOpening(wallHit.object.userData.wall,wallHit.point);return;}}
 if(obj.type==='handle'){grab={type:'handle',id:obj.id,end:obj.end};return;}
 if(obj.type==='waypoint'){
 const c=project.cables.find(x=>x.id===obj.id),p=c?.route[obj.index];
 if(p){const normal=new THREE.Vector3();activeCamera.getWorldDirection(normal);
 const point=new THREE.Vector3(p[0]/1000,p[1]/1000,p[2]/1000);
 grab={type:'waypoint',id:obj.id,index:obj.index,changed:false,plane:new THREE.Plane().setFromNormalAndCoplanarPoint(normal,point)};}
 return;
 }
 if(obj.type==='panel'){panelKey=obj.id;renderAll();return;}
 if(obj.type==='support'){supportId=obj.id;renderAll();return;}
 if(obj.type==='hole'){wallId=obj.wall;holeId=obj.hole;renderAll();if(mode==='move'){const w=project.walls.find(x=>x.id===obj.wall),p=holePosition(obj.wall,w.holes.find(h=>h.id===obj.hole));
 grab={type:'move',wall:obj.wall,hole:obj.hole,changed:false,
 plane:new THREE.Plane().setFromNormalAndCoplanarPoint(normal(obj.wall),p)};}return;}
 if(obj.type==='cable'){cableId=obj.id;filter='all';renderAll();return;}
 if(viewMode==='3D'){grab={type:'orbit',x:e.clientX,y:e.clientY};canvas.classList.add('dragging');}
});
canvas.addEventListener('pointermove',e=>{
 if(grab?.type==='orbit'){azimuth-=(e.clientX-grab.x)*.006;elevation=clamp(elevation+(e.clientY-grab.y)*.005,-.08,1.45);grab.x=e.clientX;grab.y=e.clientY;view();return;}
 if(grab?.type==='move'){const w=project.walls.find(x=>x.id===grab.wall),h=w?.holes.find(x=>x.id===grab.hole);
 pick(e,[]);const p=new THREE.Vector3();const hit=raycaster.ray.intersectPlane(grab.plane,p);
 if(h&&hit){if(!grab.changed){undo.push(JSON.stringify(project));if(undo.length>80)undo.shift();redo.length=0;grab.changed=true;}
 Object.assign(h,wallCoords(w.id,p));rebuild();}return;}
 if(grab?.type==='waypoint'){
 const c=project.cables.find(x=>x.id===grab.id);if(!c||!c.route[grab.index])return;
 pick(e,[]);
 const p=new THREE.Vector3(),sz=sizes();
 if(raycaster.ray.intersectPlane(grab.plane,p)){
 if(!grab.changed){undo.push(JSON.stringify(project));if(undo.length>80)undo.shift();redo.length=0;grab.changed=true;}
 c.route[grab.index]=[Math.round(clamp(p.x,-sz.w/2,sz.w/2)*1000),
 Math.round(clamp(p.y,0,sz.h)*1000),Math.round(clamp(p.z,-sz.d/2,sz.d/2)*1000)];
 rebuild();
 }return;
 }
 if(grab?.type==='handle'){const hit=pick(e,hitObjects.filter(o=>o.userData.type==='hole'));
 $('tooltip').style.display=hit?'block':'none';if(hit){const o=hit.object.userData;$('tooltip').textContent=referenceText({wall:o.wall,hole:o.hole});$('tooltip').style.left=(e.offsetX+13)+'px';$('tooltip').style.top=(e.offsetY+13)+'px';}return;}
 const hit=pick(e),o=hit?.object.userData;$('tooltip').style.display=o?'block':'none';
 if(o){$('tooltip').textContent=o.type==='hole'?referenceText({wall:o.wall,hole:o.hole}):o.type==='cable'?project.cables.find(c=>c.id===o.id)?.name||'Kabel':o.type==='handle'?'Griff auf Leerrohr ziehen':'';
 $('tooltip').style.left=(e.offsetX+13)+'px';$('tooltip').style.top=(e.offsetY+13)+'px';}
});
canvas.addEventListener('pointerup',e=>{
 if(grab?.type==='handle'){const hit=pick(e,hitObjects.filter(o=>o.userData.type==='hole'));
 if(hit){const o=hit.object.userData,c=project.cables.find(x=>x.id===grab.id);if(c)change(()=>{c[grab.end]={wall:o.wall,hole:o.hole};});}}
 if((grab?.type==='move'||grab?.type==='waypoint')&&grab.changed){renderAll();persist();}
 grab=null;canvas.classList.remove('dragging');
});
canvas.addEventListener('pointercancel',()=>{grab=null;canvas.classList.remove('dragging');renderAll();});
canvas.addEventListener('wheel',e=>{e.preventDefault();zoom=clamp(zoom*(e.deltaY>0?1.11:.9),.45,4);view();},{passive:false});
canvas.addEventListener('contextmenu',e=>e.preventDefault());


function showGlobalResult(hit){
 if(!hit)return;
 switchShaft(hit.shaftId,hit.kind==='cable'?hit.cableId:null);
 if(hit.kind==='hole'){wallId=hit.wallId;holeId=hit.holeId;viewMode='3D';}
 if(hit.kind==='cable'){cableId=hit.cableId;filter='all';}
 networkOpen=false;renderAll();requestAnimationFrame(resize);
}
$('globalSearch').addEventListener('input',renderQualityUI);
$('globalResults').addEventListener('click',e=>{
 const el=e.target.closest('[data-result]');if(!el)return;
 const hits=JSON.parse($('globalResults').dataset.results||'[]');
 showGlobalResult(hits[Number(el.dataset.result)]);
});
$('applyQuality').addEventListener('click',()=>{
 const raw={minPipeGapMm:$('minPipeGap').value,minEdgeMm:$('minEdgeGap').value,
 minBendRadiusMm:$('minBend').value};
 const invalid=Object.values(raw).some(v=>v!==''&&(!Number.isFinite(Number(v))||Number(v)<0||Number(v)>100000));
 if(invalid){alert('Prüfparameter: gültige Werte zwischen 0 und 100.000 mm eingeben oder Feld leer lassen.');return;}
 change(()=>{project.network.quality=qualityModel.normalizeSettings(raw);},'Projekt-Prüfwerte geändert');
});
$('runQuality').addEventListener('click',renderQualityUI);
$('qualityList').addEventListener('click',e=>{
 const item=e.target.closest('[data-quality]');if(!item||!lastQualityResult)return;
 const issue=lastQualityResult.issues[Number(item.dataset.quality)];if(!issue)return;
 const shaft=project.network.shafts.find(s=>s.id===issue.shaftId);
 if(shaft){
  const c=shaft.snapshot.cables.find(c=>c.id===issue.ref);
  const h=shaft.snapshot.walls.find(w=>w.holes.some(x=>x.id===issue.ref));
  showGlobalResult(c?{kind:'cable',shaftId:issue.shaftId,cableId:c.id}:
   h?{kind:'hole',shaftId:issue.shaftId,wallId:h.id,holeId:issue.ref}:
   {kind:'shaft',shaftId:issue.shaftId});
 }
});
$('reviewTarget').addEventListener('change',()=>{reviewKey=$('reviewTarget').value;renderQualityUI();});
$('saveReview').addEventListener('click',()=>{
 const entityType=reviewKey==='shaft'?'shaft':'cable',entityId=entityType==='shaft'?
 project.activeShaftId:reviewKey.slice(6);
 const status=$('reviewStatus').value,source=$('reviewSource').value.trim().slice(0,250),
 by=$('reviewBy').value.trim().slice(0,80),note=$('reviewNote').value.trim().slice(0,500);
 if(status==='Vor Ort abgeglichen'&&!source){
  alert('Für „Vor Ort abgeglichen“ bitte mindestens die Quelle / den Beleg dokumentieren.');return;
 }
 if(entityType==='cable'&&!project.cables.some(c=>c.id===entityId))return;
 change(()=>{
  const entries=project.network.reviews||[];
  const value={shaftId:project.activeShaftId,entityType,entityId,status,source,
   checkedBy:by,note,checkedAt:new Date().toISOString()};
  project.network.reviews=qualityModel.normalizeReviews([
   value,...entries.filter(x=>!(x.shaftId===value.shaftId&&x.entityType===value.entityType&&x.entityId===value.entityId))
  ],project.network.shafts);
 },'Manueller Prüfvermerk gespeichert');
});
function renderReport(){syncActiveShaft();return reportModel.reportHTML(project,qualityModel.evaluate(project.network,project.network.quality));}
$('exportReportHtml').addEventListener('click',()=>{
 download(filename()+'-Pruefbericht.html',renderReport(),'text/html;charset=utf-8');
});
$('exportViewer').addEventListener('click',()=>{
 syncActiveShaft();
 download(filename()+'-Offline-Viewer.html',reportModel.viewerHTML(project),'text/html;charset=utf-8');
});
$('exportPdf').addEventListener('click',async()=>{
 const html=renderReport();
 if(!window.kabelsalatDesktop?.saveReportPDF){
  download(filename()+'-PDF-Druckvorlage.html',html,'text/html;charset=utf-8');
  alert('Die Browser-Version öffnet keinen nativen PDF-Speicherdialog. HTML-Bericht öffnen und mit Strg+P → Als PDF speichern.');
  return;
 }
 try{
  const result=await window.kabelsalatDesktop.saveReportPDF(html,filename()+'-Pruefbericht.pdf');
  if(result&&!result.ok&&!result.cancelled)alert('PDF-Export nicht möglich: '+(result.error||'Unbekannter Fehler'));
 }catch(ex){alert('PDF konnte nicht gespeichert werden: '+ex.message);}
});


/* Samples are bundled as JavaScript data, so file:// offline installations require no fetch. */
$('exampleSelect').innerHTML=samples.manifest.map(example=>
 '<option value="'+esc(example.id)+'">'+esc(example.name)+'</option>').join('');
$('exampleSelect').value='pdf-u028';
$('loadExample').addEventListener('click',()=>{
 const selected=$('exampleSelect').value;
 const meta=samples.manifest.find(x=>x.id===selected);
 if(!meta||!confirm('Beispiel „'+meta.name+'“ laden? Das aktuelle Projekt wird ersetzt. '+
 'Bitte vorher als JSON sichern, falls du deine Arbeit behalten möchtest.'))return;
 change(()=>{
  project=validProject(samples.create(selected));
  wallId='SW1';holeId=null;cableId=project.cables[0]?.id||null;
  panelKey='OLA-floor';supportId=project.supports[0]?.id||null;
  variantId=project.variants[0]?.id||null;netRouteId=project.network.routes[0]?.id||null;
  netSegmentShaftId=project.activeShaftId;netLinkFromId=null;netLinkToId=null;netLinkId=null;
  filter='all';viewMode='3D';zoom=1;networkOpen=selected==='station';
  if(selected==='pdf-u028')pdfNoticeDismissed=false;
  exploreCutawayWall=null;exploreSystemFocus='all';exploreCameraShot='overview';
  mapZoom=1;mapOffsetX=0;mapOffsetY=0;reviewKey='shaft';
 },'Beispiel '+selected+' geladen');
 requestAnimationFrame(resize);
});
$('showExampleNetwork').addEventListener('click',()=>setNetworkOpen(true));


/* Pane toggles are always reachable from the model, even in expert mode. */
function renderSidebarLayout(){
 document.body.classList.toggle('sidebar-left-collapsed',leftSidebarCollapsed);
 document.body.classList.toggle('sidebar-right-collapsed',rightSidebarCollapsed);
 for(const [side,collapsed] of [['Left',leftSidebarCollapsed],['Right',rightSidebarCollapsed]]){
  const button=$('sidebarToggle'+side);
  const label=(side==='Left'?'Linke':'Rechte')+' Seitenleiste '+(collapsed?'aufklappen':'einklappen');
  button.textContent=side==='Left'?(collapsed?'❯':'❮'):(collapsed?'❮':'❯');
  button.title=label;
  button.setAttribute('aria-label',label);
  button.setAttribute('aria-expanded',String(!collapsed));
 }
}
function saveSidebarLayout(){
 try{localStorage.setItem(SIDEBAR_LAYOUT_KEY,JSON.stringify({
  left:leftSidebarCollapsed,right:rightSidebarCollapsed
 }));}catch(_){/* UI controls still work if browser storage is blocked. */}
}
/* Guided, low-noise exploration: no hidden mutations to the project data. */
function renderExplorerUI(){
 renderSidebarLayout();
 $('oldProjectNotice').hidden=pdfView()||pdfNoticeDismissed||!exploreMode||presentationFocus;
 document.body.classList.toggle('explore-mode',exploreMode);
 document.body.classList.toggle('explore-with-2d',exploreMode&&exploreWith2D);
 document.body.classList.toggle('presentation-focus',exploreMode&&presentationFocus);
 document.body.classList.toggle('scene-info-compact',exploreMode&&sceneInfoCompact);
 $('toggleSceneInfo').textContent=sceneInfoCompact?'▤ Steuerung':'–';
 $('toggleSceneInfo').setAttribute('aria-expanded',String(!sceneInfoCompact));
 $('toggleSceneInfo').setAttribute('aria-label',
  sceneInfoCompact?'Schachtinformationen und Steuerung aufklappen':'Schachtinformationen und Steuerung einklappen');
 $('startPitch').setAttribute('aria-pressed',String(exploreMode&&presentationFocus));
 $('explorePresentationFocus').textContent=presentationFocus?'↙ Seitenleisten wieder anzeigen':'▣ Fokusansicht ohne Seitenleisten';
 $('explorePresentationFocus').setAttribute('aria-pressed',String(presentationFocus));
 document.querySelectorAll('#exploreSystemFilters [data-presentation-group]').forEach(btn=>{
  const active=btn.dataset.presentationGroup===exploreSystemFocus;
  btn.classList.toggle('active',active);
  btn.setAttribute('aria-pressed',String(active));
 });
 document.querySelectorAll('#exploreCameraPresets [data-camera-shot]').forEach(btn=>{
  const active=btn.dataset.cameraShot===exploreCameraShot;
  btn.classList.toggle('active',active);
  btn.setAttribute('aria-pressed',String(active));
 });
 $('toggleExpert').textContent=exploreMode?'🛠 Profi-Modus':'◉ Basic-Modus';
 $('toggleExpert').setAttribute('aria-pressed',String(!exploreMode));
 $('explore2DToggle').textContent=exploreWith2D?'▢ 2D-Plan ausblenden':'▤ Wand als 2D-Plan sehen';
 $('exploreCutawayWall').value=exploreCutawayWall||'none';
 $('exploreDimensionInfo').textContent=hole()?pipeDimensions.describe(hole()):
  'Öffnung anklicken: DN, Öffnungsgröße und Herkunft erscheinen hier. Ungeprüfte Maße sind schematisch.';
 $('demoCompactionToggle').hidden=!pdfView();
 $('demoCompactionToggle').textContent=demoCompactView?'↔ PDF-Originalpunkte ansehen':'↔ 62-%-Vorschau ansehen';
 $('demoCompactionToggle').setAttribute('aria-pressed',String(demoCompactView));
 const presentedCables=project.cables.filter(c=>
  exploreSystemFocus==='all'||c.group===exploreSystemFocus);
 const routedCount=presentedCables.filter(c=>endpoint(c.a)&&endpoint(c.b)).length;
 const openings=project.walls.reduce((sum,w)=>sum+w.holes.length,0);
 $('exploreSceneTitle').textContent=avlReference()?project.sourceInfo.shaft+' · AVL0116 V2.0':'Kabelschacht · 3D';
 $('exploreSourceLine').textContent=pdfView()?
  openings+' Leerrohre · '+project.cables.length+' Kabelobjekte · '+
  (project.cables.length-project.cables.filter(c=>endpoint(c.a)&&endpoint(c.b)).length)+
  ' ungeklärte Verläufe · Konzeptstand · '+
  (demoCompactView&&exploreMode?'62%-Vorschauskalierung (nur Ansicht)':'Original-PDF-Kreismittelpunkte (nicht vermessen)')+
  (exploreProtectionGuide?' · MS/OLA-Konzeptmarkierungen sichtbar':''):
  openings+' Rohröffnungen · schematische Darstellung';
 $('exploreCableCoverage').textContent='3D-Kabelwege mit beiden Rohranschlüssen: '+
  routedCount+' / '+presentedCables.length+
  (routedCount<presentedCables.length?
   ' · '+(presentedCables.length-routedCount)+' ohne vollständigen sichtbaren Verlauf.':'')+
  ' Keine bautechnische Bestätigung.';
 const labels={
  hidden:'👁 Platten transparent zeigen',
  transparent:'▣ Platten deckend zeigen',
  solid:'⊘ Platten ausblenden'
 };
 $('exploreConstructionMode').textContent=labels[exploreConstructionMode];
 $('exploreProtectionGuide').textContent=exploreProtectionGuide?
  '🔵🔴 V2.0-Konstruktion ausblenden':'🔵🔴 V2.0-Konstruktion anzeigen';
 $('exploreProtectionGuide').setAttribute('aria-pressed',String(exploreProtectionGuide));
 $('exploreProtectionGuide').hidden=!avlReference();
 $('exploreProtectionStatus').hidden=!avlReference();
 const protectedGroups=['MS','OLA'].map(group=>{
  const list=project.cables.filter(c=>c.group===group);
  const known=list.filter(c=>endpoint(c.a)&&endpoint(c.b)).length;
  return {group,known,total:list.length};
 });
 $('exploreProtectionStatus').textContent=avlReference()?
  protectedGroups.map(x=>{
   const p=avlConstructionPlan(x.group);
   return x.group+': '+x.known+'/'+x.total+' Endpunktpaare · '+
    (p.status==='concept'?
     (x.group==='OLA'?'A · Stapelplatten':'B · SP-Stützen')+' (schematische 3D-Bauteile)':
     'Kanal nicht gebaut – '+p.issues[0]);
  }).join(' | ')+
  '. Maße, Konstruktionsstatik und Brandschutzfreigabe offen. Biegeradius: '+
  (project.network?.quality?.minBendRadiusMm==null?'Herstellerwert fehlt':
   'Projektwert '+project.network.quality.minBendRadiusMm+' mm, nur geometrische Näherung')+'.':'';

 $('exploreConstructionMode').setAttribute('aria-pressed',String(exploreConstructionMode!=='hidden'));
 const selectedGroups=['OLA','MS'].filter(k=>exploreSystemFocus==='all'||exploreSystemFocus===k);
 const shown=selectedGroups.filter(k=>project.enclosures[k]?.visible);
 const missing=selectedGroups.filter(k=>!project.enclosures[k]?.visible);
 $('pdfProtectionNote').hidden=!pdfView();
 $('pdfWallSection').hidden=!pdfView();
 $('loadPdfV2').hidden=false;
 $('loadPdfV2').textContent=pdfView()?'↻ Aktuelle PDF-Wandbelegung (91 LR) neu laden':'📄 PDF-Referenz 13_U_028 öffnen';
 $('pdfWallContext').textContent=pdfView()?wallId+' · '+PDF_WALLS[wallId]+
  ' · '+wall().holes.length+' LR · PDF-Belegung erhalten, Demo-Rohrabstände enger (keine vermessenen mm)':'';
 document.querySelectorAll('#pdfWallViews [data-pdf-view]').forEach(btn=>{
  const active=btn.dataset.pdfView===viewMode;
  btn.classList.toggle('active',active);
  btn.setAttribute('aria-pressed',String(active));
 });
 $('exploreConstructionNote').textContent=exploreConstructionMode==='hidden'?
  'Für die Vorführung: vorhandene Platten und Auflager einblenden (nur Ansicht).':
  (shown.length?shown.join(' / ')+'-Platten im 3D-Modell sichtbar. ':'Keine Einhausung aktiv. ')+
  (missing.length?missing.join(' / ')+' ist im Projekt ausgeblendet. ':'')+
  (exploreSystemFocus!=='all'?'Auflager ohne Gewerkszuordnung im Filter ausgeblendet. ':
   project.supports.some(s=>s.visible)?'Vorhandene Auflager sichtbar. ':'Keine Auflager hinterlegt. ')+
  'Schematische Konstruktion – kein geprüfter Brandschutz.'+
 (project.sourceInfo?.sourceVersion?.startsWith('PDF V2')?
  ' Powerpanel-Maße und Montagepositionen sind nicht bestätigt.':'');
 const tips={
  network:'🗺️ Die Kreise sind Schächte. Linien verbinden sie schematisch. Tippe auf einen Kreis oder doppelklicke, um einen Schacht zu öffnen.',
  shaft:'🏗️ Der graublaue Kasten ist ein Schacht. Kleine Kreise sind Rohröffnungen; die Kabel laufen als bunte Linien hindurch. Mit der Maus drehen, mit dem Rad zoomen.',
  cable:'🔴 Das kräftige Kabel ist ausgewählt. Die anderen Linien treten in den Hintergrund. Rechts kannst du ein anderes Kabel anklicken.'
 };
 $('exploreTip').textContent=tips[exploreStage];
 for(const key of ['network','shaft','cable']){
  const button=$('quest'+key[0].toUpperCase()+key.slice(1));
  const done=exploreVisited.has(key);
  button.classList.toggle('active',exploreStage===key);
  button.querySelector('.quest-check').textContent=done?'✓':'○';
  button.setAttribute('aria-pressed',String(exploreStage===key));
 }
 document.querySelectorAll('.quest-progress span').forEach((x,i)=>x.classList.toggle('done',
  exploreVisited.has(['network','shaft','cable'][i])));
 const c=cable(),w=wall();
 const target=$('exploreSelectionBody');
 const describe=(r)=>r?('Wand '+r.wall+' · Rohröffnung '+r.hole):'Noch nicht festgelegt';
 if(c){
  target.innerHTML='<strong><span class="selected-color" style="background:'+esc(c.color)+'"></span>'+
  esc(c.name)+'</strong><div class="small">Gruppe: '+esc(c.group)+' · Kabeldurchmesser: '+
  esc(c.diameter)+(c.drawingDiameterOnly?' mm · nur Darstellungswert (nicht gemessen)':' mm')+'</div><span class="endpoint"><b>Start A:</b> '+esc(describe(c.a))+
  '</span><span class="endpoint"><b>Ziel B:</b> '+esc(describe(c.b))+
  '</span><p class="small" style="margin:9px 0 0">Das ist eine schematische Planung, kein überprüfter Bauverlauf. Klick rechts ein anderes Kabel an.</p>'+
  (project.sourceInfo?'<div class="endpoint"><b>Quellgruppe:</b> '+esc(c.referenceGroup||c.group)+
  '<br><b>Quellanschluss A:</b> '+esc(c.sourceA||c.sourceOriginalA||'offen')+
  '<br><b>Quellanschluss B:</b> '+esc(c.sourceB||c.sourceOriginalB||'offen')+
  '<br><b>Zuordnung:</b> '+esc(c.mappingStatus||'schematisch')+
  '<br><b>Quelle:</b> '+esc(project.sourceInfo.document)+
  '</div>':'');
 }else{
  target.textContent='Noch kein Kabel ausgewählt. Klick rechts auf einen farbigen Eintrag.';
 }
 $('exploreSceneText').textContent=project.sourceInfo?.sourceVersion?.startsWith('PDF V2')?
 'PDF V2: Belegungen und DN laut Konzept, Positionen schematisch. Gelbe Kabel-Endmarkierung = Fortsetzung ungeklärt.':
 exploreStage==='cable'&&c?
  'Der kräftige farbige Weg gehört zu „'+c.name+'“. Start und Ziel siehst du rechts.':
  'Runde Öffnungen nehmen Rohre auf. Die Linien zeigen schematische Kabelwege. Ziehe mit der Maus, um den Schacht zu drehen.';
}
function rememberQuest(which){
 exploreStage=which;exploreVisited.add(which);
 try{localStorage.setItem(EXPLORE_PROGRESS_KEY,JSON.stringify([...exploreVisited]));}catch(_){}
}
function openQuest(which){
 rememberQuest(which);
 if(which==='network'){networkOpen=true;}
 else{
  networkOpen=false;viewMode='3D';zoom=1;
  if(which==='cable'){filter='all';cableId=project.cables.find(x=>x.group==='OLA')?.id||
    project.cables[0]?.id||null;}
 }
 renderAll();
 requestAnimationFrame(resize);
}
$('questNetwork').addEventListener('click',()=>openQuest('network'));
$('questShaft').addEventListener('click',()=>openQuest('shaft'));
$('questCable').addEventListener('click',()=>openQuest('cable'));
$('toggleExpert').addEventListener('click',()=>{
 if(exploreMode){enterEditWorkspace();return;}
 exploreMode=true;mode='orbit';grab=null;draftDrag=null;
 try{localStorage.setItem(EXPLORE_MODE_KEY,'explore');}catch(_){}
 renderAll();requestAnimationFrame(resize);
});
$('switchToPdf').addEventListener('click',()=>{
 $('exampleSelect').value='pdf-u028';$('loadExample').click();
});
$('previewPdfNow').addEventListener('click',()=>globalThis.KabelsalatPdfVisual?.show?.());
$('dismissPdfNotice').addEventListener('click',()=>{
 pdfNoticeDismissed=true;$('oldProjectNotice').hidden=true;
});
$('loadPdfV2').addEventListener('click',()=>{
 $('exampleSelect').value='pdf-u028';
 $('loadExample').click(); // retains unsaved-work confirmation
});
$('pdfWallViews').addEventListener('click',event=>{
 const button=event.target.closest('[data-pdf-view]');
 const requested=button?.dataset.pdfView;
 if(!pdfView()||!['3D','SW1','SW2','SW3','SW4'].includes(requested))return;
 if(requested==='3D'){viewMode='3D';exploreWith2D=false;}
 else{wallId=requested;viewMode=requested;exploreWith2D=project.splitEnabled!==false;}
 networkOpen=false;renderAll();requestAnimationFrame(resize);
});
$('demoCompactionToggle').addEventListener('click',()=>{
 if(!pdfView())return;
 demoCompactView=!demoCompactView;viewMode='3D';renderAll();
});
$('explore2DToggle').addEventListener('click',()=>{
 exploreWith2D=!exploreWith2D;
 if(!project.splitEnabled){exploreWith2D=false;alert('Die parallele 2D-Ansicht ist in diesem Projekt deaktiviert. Im Profi-Modus kannst du sie einschalten.');return;}
 renderAll();requestAnimationFrame(resize);
});
$('exploreCameraReset').addEventListener('click',()=>{
 azimuth=.72;elevation=.42;zoom=1;viewMode='3D';exploreCameraShot='overview';renderAll();
});
$('exploreConstructionMode').addEventListener('click',()=>{
 exploreConstructionMode=CONSTRUCTION_MODES[
  (CONSTRUCTION_MODES.indexOf(exploreConstructionMode)+1)%CONSTRUCTION_MODES.length];
 viewMode='3D';exploreCameraShot='overview';renderAll();
});
$('exploreProtectionGuide').addEventListener('click',()=>{
 if(!avlReference())return;
 exploreProtectionGuide=!exploreProtectionGuide;
 if(exploreProtectionGuide){viewMode='3D';networkOpen=false;}
 renderAll();
});
$('exploreSystemFilters').addEventListener('click',event=>{
 const button=event.target.closest('[data-presentation-group]');
 if(!button||!PRESENTATION_GROUPS.includes(button.dataset.presentationGroup))return;
 exploreSystemFocus=button.dataset.presentationGroup;
 if(exploreConstructionMode==='hidden')exploreConstructionMode='transparent';
 renderAll();
});
$('exploreCutawayWall').addEventListener('change',()=>{
 const selected=$('exploreCutawayWall').value;
 exploreCutawayWall=Object.hasOwn(CUTAWAY_AZIMUTH,selected)?selected:null;
 if(exploreCutawayWall){
  // Face the missing wall to reveal the interior, without moving any object.
  azimuth=pdfView()?pdfGeometry.wall(exploreCutawayWall).azimuth:
   CUTAWAY_AZIMUTH[exploreCutawayWall];
  elevation=.46;zoom=.9;
  exploreCameraShot='custom';
 }
 viewMode='3D';networkOpen=false;renderAll();
});
$('exploreCameraPresets').addEventListener('click',event=>{
 const button=event.target.closest('[data-camera-shot]');
 const shot=CAMERA_SHOTS[button?.dataset.cameraShot];
 if(!shot)return;
 exploreCameraShot=button.dataset.cameraShot;
 azimuth=shot.azimuth;elevation=shot.elevation;zoom=shot.zoom;viewMode=shot.view;
 networkOpen=false;renderAll();
});
function exitPresentation(){
 presentationFocus=false;sceneInfoCompact=false;
 renderAll();requestAnimationFrame(resize);
}
/* Read-only Basic, collapsed panes and PDF overlays should never trap editors.
   This changes UI state only, not underlying pipe/cable project data. */
function enterEditWorkspace(){
 window.KabelsalatPdfVisual?.hide?.();
 exploreMode=false;presentationFocus=false;sceneInfoCompact=false;
 networkOpen=false;viewMode='3D';exploreWith2D=false;
 leftSidebarCollapsed=false;rightSidebarCollapsed=false;
 mode='orbit';grab=null;draftDrag=null;
 try{localStorage.setItem(EXPLORE_MODE_KEY,'expert');}catch(_){}
 saveSidebarLayout();
 $('leftSidebar').scrollTop=0;$('rightSidebar').scrollTop=0;
 renderAll();requestAnimationFrame(resize);
}
$('enableEditing').addEventListener('click',enterEditWorkspace);
/* Read-only home navigation for network, PDF, quality and presentations. */
function returnToShaft(){
 window.KabelsalatPdfVisual?.hide?.();
 networkOpen=false;
 presentationFocus=false;sceneInfoCompact=false;
 viewMode='3D';exploreWith2D=false;
 mode='orbit';grab=null;draftDrag=null;
 $('leftSidebar').scrollTop=0;$('rightSidebar').scrollTop=0;
 renderAll();requestAnimationFrame(resize);
}
for(const id of ['returnToShaft','qualityBackToShaft','pdfGoShaft'])
 $(id).addEventListener('click',returnToShaft);
$('startPitch').addEventListener('click',()=>{
 // Different source projects still use the existing unsaved-work warning.
 if(!pdfView()){
  $('exampleSelect').value='pdf-u028';
  $('loadExample').click();
  if(!pdfView())return; // User declined to replace the current project.
 }
 exploreMode=true;mode='orbit';grab=null;draftDrag=null;
 try{localStorage.setItem(EXPLORE_MODE_KEY,'explore');}catch(_){}
 networkOpen=false;presentationFocus=true;sceneInfoCompact=true;
 exploreWith2D=false;exploreCutawayWall=null;exploreSystemFocus='all';
 exploreConstructionMode='transparent';exploreProtectionGuide=true;
 viewMode='3D';exploreCameraShot='overview';
 azimuth=.72;elevation=.42;zoom=.86;
 renderAll();requestAnimationFrame(resize);
});
$('toggleSceneInfo').addEventListener('click',()=>{
 sceneInfoCompact=!sceneInfoCompact;
 renderExplorerUI();requestAnimationFrame(resize);
});
$('exitPitch').addEventListener('click',exitPresentation);
document.addEventListener('keydown',event=>{
 if(event.key!=='Escape')return;
 if(presentationFocus){event.preventDefault();exitPresentation();}
 else if(networkOpen){event.preventDefault();returnToShaft();}
});
$('explorePresentationFocus').addEventListener('click',()=>{
 presentationFocus=!presentationFocus;
 sceneInfoCompact=presentationFocus;
 networkOpen=false;
 renderAll();requestAnimationFrame(resize);
});
$('sidebarToggleLeft').addEventListener('click',()=>{
 leftSidebarCollapsed=!leftSidebarCollapsed;
 saveSidebarLayout();renderSidebarLayout();
 requestAnimationFrame(resize);
});
$('sidebarToggleRight').addEventListener('click',()=>{
 rightSidebarCollapsed=!rightSidebarCollapsed;
 saveSidebarLayout();renderSidebarLayout();
 requestAnimationFrame(resize);
});

/* Network workspace commands. All changes go through the undo/persistence system. */
function setNetworkOpen(next){
 networkOpen=next;renderAll();requestAnimationFrame(resize);
}
$('toggleNetwork').addEventListener('click',()=>setNetworkOpen(!networkOpen));
$('netBackEditor').addEventListener('click',returnToShaft);
$('netOpenShaft').addEventListener('click',()=>setNetworkOpen(false));
$('netShaftSelect').addEventListener('change',()=>switchShaft($('netShaftSelect').value));
$('netNewShaft').addEventListener('click',()=>{
 if(project.network.shafts.length>=30){alert('Bis zu 30 Schächte pro Netz in dieser Version.');return;}
 const net=project.network,old=activeShaft(),id=uid('S'),blank=demo();
 blank.name='Schacht '+(net.shafts.length+1);blank.cables=[];
 for(const w of blank.walls){w.rows=3;w.cols=4;w.holes=[];}
 blank.variants=[];blank.supports=[];
 change(()=>{
  syncActiveShaft();
  net.shafts.push({id,name:blank.name,x:(old?.x||0)+10000,z:old?.z||0,snapshot:networkModel.snapshot(blank)});
  project=validProject({...blank,network:net,activeShaftId:id});
  netSegmentShaftId=id;netLinkFromId=old?.id||'S1';netLinkToId=id;
  cableId=null;holeId=null;supportId=null;variantId=null;wallId='SW1';viewMode='3D';
 });
});
$('netUpdateShaft').addEventListener('click',()=>{
 const name=$('netShaftName').value.trim().slice(0,80)||'Schacht';
 const x=Number($('netShaftX').value),z=Number($('netShaftZ').value);
 if(!Number.isSafeInteger(x)||!Number.isSafeInteger(z)||Math.abs(x)>100000000||Math.abs(z)>100000000){
  alert('Bitte ganzzahlige X-/Z-Koordinaten zwischen -100.000.000 und 100.000.000 mm eingeben.');return;
 }
 change(()=>{const p=activeShaft();p.name=name;p.x=x;p.z=z;project.name=name;});
});
$('netDeleteShaft').addEventListener('click',()=>{
 const net=project.network,id=project.activeShaftId;
 if(net.shafts.length<=1)return;
 const count=net.links.filter(l=>l.from===id||l.to===id).length,
 segments=net.routes.reduce((n,r)=>n+r.steps.filter(s=>s.shaftId===id).length,0);
 if(!confirm('Schacht '+id+' entfernen? '+count+' Verbindungen und '+segments+
 ' Kabelweg-Segmente werden ebenfalls entfernt. Dies ist rückgängig machbar.'))return;
 change(()=>{
  syncActiveShaft();
  net.shafts=net.shafts.filter(x=>x.id!==id);
  net.links=net.links.filter(l=>l.from!==id&&l.to!==id);
  for(const r of net.routes)r.steps=r.steps.filter(x=>x.shaftId!==id);
  const newShaft=net.shafts[0];
  project=validProject({...newShaft.snapshot,network:net,activeShaftId:newShaft.id});
  netLinkFromId=null;netLinkToId=null;netSegmentShaftId=newShaft.id;
  wallId='SW1';holeId=null;cableId=project.cables[0]?.id||null;
  panelKey='OLA-floor';supportId=null;variantId=null;
 });
});
$('netLinkFrom').addEventListener('change',()=>{netLinkFromId=$('netLinkFrom').value;renderNetworkUI();});
$('netLinkTo').addEventListener('change',()=>{netLinkToId=$('netLinkTo').value;renderNetworkUI();});
$('netLinkSelect').addEventListener('change',()=>{netLinkId=$('netLinkSelect').value||null;renderNetworkUI();});
$('netAddLink').addEventListener('click',()=>{
 const a=netLinkFromId,b=netLinkToId;
 if(!a||!b||a===b)return;
 if(project.network.links.some(l=>(l.from===a&&l.to===b)||(l.from===b&&l.to===a))){
  alert('Diese Verbindung existiert bereits.');return;
 }
 change(()=>{const id=uid('L');project.network.links.push({id,from:a,to:b,note:'Schematische Verbindung'});
 netLinkId=id;});
});
$('netDeleteLink').addEventListener('click',()=>{
 if(!netLinkId)return;
 if(!confirm('Netzverbindung löschen? Kabelwege können dadurch unvollständig werden.'))return;
 change(()=>{project.network.links=project.network.links.filter(l=>l.id!==netLinkId);netLinkId=null;});
});
$('netRouteSelect').addEventListener('change',()=>{netRouteId=$('netRouteSelect').value||null;renderAll();});
$('netNewRoute').addEventListener('click',()=>{
 if(project.network.routes.length>=200){alert('Maximal 200 Netz-Kabelwege.');return;}
 const name=prompt('Neuer Kabelweg – Bezeichnung','Trasse '+(project.network.routes.length+1));
 if(name===null)return;
 change(()=>{const id=uid('R');project.network.routes.push({id,name:name.trim().slice(0,80)||'Kabelweg',
 group:'Weitere',steps:[]});netRouteId=id;netSegmentShaftId=project.activeShaftId;});
});
$('netDeleteRoute').addEventListener('click',()=>{
 if(!selectedRoute()||!confirm('Ausgewählten Netz-Kabelweg löschen?'))return;
 change(()=>{project.network.routes=project.network.routes.filter(r=>r.id!==netRouteId);
 netRouteId=project.network.routes[0]?.id||null;});
});
$('netUpdateRoute').addEventListener('click',()=>{
 if(!selectedRoute())return;
 const name=$('netRouteName').value.trim().slice(0,80)||'Kabelweg',group=$('netRouteGroup').value;
 change(()=>{const r=selectedRoute();r.name=name;r.group=group;});
});
$('netSegmentShaft').addEventListener('change',()=>{
 netSegmentShaftId=$('netSegmentShaft').value;renderNetworkUI();
});
$('netAppendSegment').addEventListener('click',()=>{
 const route=selectedRoute(),shaft=project.network.shafts.find(s=>s.id===netSegmentShaftId);
 const cid=$('netSegmentCable').value;
 if(!route||!shaft||!shaft.snapshot.cables.some(c=>c.id===cid)||route.steps.length>=60)return;
 change(()=>{route.steps.push({shaftId:shaft.id,cableId:cid});});
});
$('netPopSegment').addEventListener('click',()=>{
 const r=selectedRoute();if(r&&r.steps.length)change(()=>r.steps.pop());
});
$('netTraceList').addEventListener('click',e=>{
 const node=e.target.closest('[data-trace-shaft]');if(!node)return;
 switchShaft(node.dataset.traceShaft,node.dataset.traceCable);
 setNetworkOpen(false);
});
$('netShowTrace').addEventListener('click',()=>{
 if(!selectedRoute())return;setNetworkOpen(true);
});
$('netExportCSV').addEventListener('click',()=>{
 syncActiveShaft();
 download(filename()+'-netzinventar.csv',networkModel.inventoryCSV(project.network),'text/csv;charset=utf-8');
});
$('netImportPositions').addEventListener('click',()=>$('netPositionsFile').click());
$('netPositionsFile').addEventListener('change',async e=>{
 const file=e.target.files?.[0];e.target.value='';if(!file)return;
 try{
  if(file.size>50000)throw Error('CSV-Datei zu groß (maximal 50 KB).');
  const review=networkModel.parsePositionsCSV(await file.text(),project.network);
  if(review.warnings.length){
   alert('CSV-Import abgebrochen – bitte erst korrigieren:\n'+review.warnings.join('\n').slice(0,1400));return;
  }
  if(!review.rows.length){alert('Keine Positionsdaten in der Datei.');return;}
  const preview=review.rows.slice(0,12).map(x=>x.id+': X '+x.x+' / Z '+x.z+' mm').join('\n');
  if(!confirm('Koordinatenimport mit '+review.rows.length+' Schacht-Positionen übernehmen?\n\n'+preview+
    (review.rows.length>12?'\n… weitere Einträge':'')+'\n\nAlle Werte sind zunächst ungeprüfte Planwerte.'))return;
  change(()=>{project.network=networkModel.applyPositions(project.network,review.rows);});
 }catch(err){alert('Koordinatenimport nicht möglich: '+err.message);}
});
function networkHit(p){
 const proj=networkProjection();let nearest=null,dist=Infinity;
 for(const s of project.network.shafts){
  const q=proj.point(s),d=Math.hypot(q.x-p.x,q.y-p.y);
  if(d<27&&d<dist){dist=d;nearest=s;}
 }
 return nearest;
}
networkCanvas.addEventListener('pointerdown',e=>{
 if(e.button!==0)return;
 networkCanvas.setPointerCapture(e.pointerId);
 const p=networkPos(e);mapDrag={...p,lastX:p.x,lastY:p.y,moved:false};
});
networkCanvas.addEventListener('pointermove',e=>{
 if(!mapDrag)return;const p=networkPos(e);
 if(Math.hypot(p.x-mapDrag.x,p.y-mapDrag.y)>4)mapDrag.moved=true;
 if(mapDrag.moved){
  mapOffsetX+=p.x-mapDrag.lastX;mapOffsetY+=p.y-mapDrag.lastY;drawNetwork();
 }
 mapDrag.lastX=p.x;mapDrag.lastY=p.y;
});
networkCanvas.addEventListener('pointerup',e=>{
 if(mapDrag&&!mapDrag.moved){const node=networkHit(networkPos(e));if(node)switchShaft(node.id);}
 mapDrag=null;
});
networkCanvas.addEventListener('pointercancel',()=>{mapDrag=null;});
networkCanvas.addEventListener('dblclick',e=>{
 const node=networkHit(networkPos(e));if(node){switchShaft(node.id);setNetworkOpen(false);}
});
networkCanvas.addEventListener('wheel',e=>{
 e.preventDefault();mapZoom=clamp(mapZoom*(e.deltaY>0?.87:1.15),.25,8);drawNetwork();
},{passive:false});
networkCanvas.addEventListener('contextmenu',e=>e.preventDefault());
new ResizeObserver(drawNetwork).observe($('networkPane'));

$('holeOccupancy').addEventListener('click',e=>{
 const node=e.target.closest('[data-occupied-cable]');if(!node)return;
 cableId=node.dataset.occupiedCable;filter='all';renderAll();
});
$('wallList').addEventListener('click',e=>{const card=e.target.closest('[data-wall]');if(card){wallId=card.dataset.wall;holeId=null;if(viewMode.startsWith('SW'))viewMode='3D';renderAll();}});
$('generate').addEventListener('click',()=>{
 const w=wall(),rows=clamp(Number($('rows').value)||w.rows,1,20),cols=clamp(Number($('cols').value)||w.cols,1,20);
 const affected=project.cables.some(c=>c.a?.wall===w.id||c.b?.wall===w.id);
 if(affected&&!confirm('Das Raster wird neu aufgebaut. Kabelanschlüsse an '+w.id+' werden entfernt. Fortfahren?'))return;
 change(()=>{w.rows=rows;w.cols=cols;w.holes=grid(w);project.cables.forEach(c=>{if(c.a?.wall===w.id)c.a=null;if(c.b?.wall===w.id)c.b=null;});holeId=null;});
});
$('toggleWall').addEventListener('click',()=>change(()=>{wall().visible=!wall().visible;}));
$('modeButtons').addEventListener('click',e=>{const b=e.target.closest('[data-mode]');if(b){mode=b.dataset.mode;renderUI();}});
$('deleteHole').addEventListener('click',()=>{const w=wall(),h=hole();if(!h)return;
 const n=project.cables.filter(c=>c.a?.hole===h.id||c.b?.hole===h.id).length;
 if(n&&!confirm(n+' Kabel haben diesen Anschluss. Das Leerrohr wirklich entfernen?'))return;
 change(()=>{w.holes=w.holes.filter(x=>x.id!==h.id);project.cables.forEach(c=>{if(c.a?.hole===h.id)c.a=null;if(c.b?.hole===h.id)c.b=null;});holeId=null;});
});
$('applyHole').addEventListener('click',()=>{const h=hole();if(!h)return;const span=wallSpanMm(wallId);
 change(()=>{h.u=clamp((Number($('holeU').value)||0)/span,0,1);h.v=clamp((Number($('holeV').value)||0)/project.dims.h,0,1);
 h.dn=clamp(Number($('holeDn').value)||h.dn,20,300);
 if(h.dimensionSpec)h.dimensionSpec.nominalMm=h.dn;},'Rohrposition / Nenn-DN geändert');});
// Separate physical pipe dimensions from legacy schematic DN (KAB-0880/0883).
$('saveHoleDimensions').addEventListener('click',()=>{
 const h=hole();if(!h)return;
 const prev=pipeDimensions.forHole(h);
 const source=$('holeDimensionSource').value.trim().slice(0,240);
 const raw={...prev,nominalMm:h.dn,
  innerMm:$('holeInnerMm').value,outerMm:$('holeOuterMm').value,
  openingMm:$('holeOpeningMm').value,source,
  reviewer:$('holeDimensionReviewer').value.trim().slice(0,240),
  method:'manual',status:$('holeDimensionStatus').value,
  note:'Nutzerangabe: noch nicht automatisch vermessen'};
 const spec=pipeDimensions.normalize(raw,h.dn);
 if(raw.status==='reviewed'&&spec.status!=='reviewed')
  alert('Prüfstatus nicht übernommen: Quelle, Prüfer und mindestens ein explizites physisches Maß erforderlich.');
 change(()=>{h.dimensionSpec=spec;},'Rohrmaße und Herkunft aktualisiert');
});
// Text recognized from labelled plan pages is ONLY a candidate. It cannot silently replace nominal DN.
let planCandidates=[];
$('planTextFile').addEventListener('change',async()=>{
 const file=$('planTextFile').files[0];if(!file)return;
 if(file.size>200000){alert('Textdatei zu groß (maximal 200 KB).');return;}
 $('planDimensionText').value=await file.text();
 $('planDimensionSource').value=file.name;
});
$('parsePlanDimensions').addEventListener('click',()=>{
 planCandidates=pipeDimensions.parsePlanText($('planDimensionText').value,
  {source:$('planDimensionSource').value.trim()||'Manuell eingefügter Plantext'});
 const s=$('planDimensionCandidates');s.replaceChildren();
 planCandidates.forEach((c,i)=>{
  const o=document.createElement('option');o.value=String(i);
  o.textContent='Zeile '+c.line+' · '+(c.pipeId||'Rohr ungeklärt')+' · '+c.label+' · Kandidat';
  s.append(o);
 });
 $('planDimensionStatus').textContent=planCandidates.length+
  ' Maßkandidaten aus Beschriftung; PDF-Binärtexte oder Bildtexte werden hier nicht automatisch extrahiert.';
});
$('applyPlanDimension').addEventListener('click',()=>{
 const h=hole(),c=planCandidates[Number($('planDimensionCandidates').value)];
 if(!h||!c)return;
 if(c.pipeId&&c.pipeId!==h.id){
  alert('Kandidat gehört zu '+c.pipeId+', ausgewählt ist '+h.id+'. Keine automatische Zuordnung.');return;
 }
 if(!c.pipeId&&!confirm('Die Rohr-ID ist im Text unklar. Kandidat dieser Öffnung zuweisen (ohne Bestätigung)?'))return;
 const next=pipeDimensions.normalize({...pipeDimensions.forHole(h),candidateMm:c.candidateMm,
  method:'pdf-label',source:c.source+' / Zeile '+c.line,status:'candidate',
  note:'Aus Planbeschriftung erkannt, manuell nachprüfen'},h.dn);
 change(()=>{h.dimensionSpec=next;},'Unbestätigten PDF-Rohrmaßkandidaten übernommen');
});
// Deterministic local pixel-circle proposals. Absolute mm only after calibration.
let photoAnalysis=null;
function showPhotoCandidates(){
 const s=$('photoCandidates');s.replaceChildren();
 for(const c of photoAnalysis?.candidates||[]){
  const o=document.createElement('option');o.value=String(c.index);
  o.textContent='#'+(c.index+1)+' · '+c.relativeSize+' · Ø '+c.diameterPx+' px'+
   (c.candidateMm?' · ca. '+c.candidateMm+' mm (ungeprüft)':'');
  s.append(o);
 }
 $('photoAnalysisStatus').textContent=photoAnalysis?
  photoAnalysis.candidates.length+' Öffnungs-Kandidaten (heuristisch, keine KI/OCR) · '+
  'Ohne Referenz nur relative Größe. Perspektivenfehler möglich.':'Noch kein Bild untersucht.';
}
$('photoDimensionFile').addEventListener('change',async()=>{
 const file=$('photoDimensionFile').files[0];if(!file)return;
 if(!['image/png','image/jpeg','image/webp'].includes(file.type)||file.size>10000000){
  alert('Bitte ein Bild bis 10 MB verwenden.');return;
 }
 try{
  const bitmap=await createImageBitmap(file);
  const canvas=$('photoDimensionPreview'),scale=Math.min(1,900/Math.max(bitmap.width,bitmap.height));
  canvas.width=Math.max(16,Math.round(bitmap.width*scale));
  canvas.height=Math.max(16,Math.round(bitmap.height*scale));
  const ctx=canvas.getContext('2d',{willReadFrequently:true});
  ctx.drawImage(bitmap,0,0,canvas.width,canvas.height);bitmap.close();
  const pixels=ctx.getImageData(0,0,canvas.width,canvas.height);
  photoAnalysis=photoCandidates.detect(pixels.data,canvas.width,canvas.height);
  photoAnalysis.filename=file.name.slice(0,200);
  ctx.strokeStyle='#ecba43';ctx.lineWidth=2;ctx.font='bold 14px sans-serif';
  for(const c of photoAnalysis.candidates){
   ctx.beginPath();ctx.arc(c.x,c.y,c.diameterPx/2,0,2*Math.PI);ctx.stroke();
   ctx.fillStyle='#fff6e2';ctx.fillText('#'+(c.index+1),c.x+8,c.y-7);
  }
  showPhotoCandidates();
 }catch(err){
  photoAnalysis=null;showPhotoCandidates();
  $('photoAnalysisStatus').textContent='Bild kann nicht analysiert werden: '+err.message;
 }
});
$('photoCalibrate').addEventListener('click',()=>{
 if(!photoAnalysis)return;
 try{
  photoAnalysis.candidates=photoCandidates.calibrate(photoAnalysis.candidates,
   Number($('photoCandidates').value),Number($('photoReferenceMm').value));
  showPhotoCandidates();
 }catch(e){alert(e.message);}
});
$('applyPhotoDimension').addEventListener('click',()=>{
 const h=hole(),c=photoAnalysis?.candidates[Number($('photoCandidates').value)];
 if(!h||!c||!Number.isFinite(c.candidateMm)){
  alert('Für einen Millimeter-Kandidaten zuerst einen Referenzkreis kalibrieren.');return;
 }
 const next=pipeDimensions.normalize({...pipeDimensions.forHole(h),candidateMm:c.candidateMm,
  method:'photo-calibrated',source:'Foto: '+photoAnalysis.filename+' (Kreiskandidat '+(c.index+1)+')',
  status:'candidate',confidence:c.quality,note:c.note||'Perspektivenunsichere Bildkandidaten'},h.dn);
 change(()=>{h.dimensionSpec=next;},'Unbestätigten Fotomaßkandidaten übernommen');
});

$('cableList').addEventListener('click',e=>{const card=e.target.closest('[data-cable]');if(card){cableId=card.dataset.cable;renderAll();}});
$('cableFilters').addEventListener('click',e=>{const b=e.target.closest('[data-filter]');if(b){filter=b.dataset.filter;renderUI();}});
$('addCable').addEventListener('click',()=>change(()=>{const n=project.cables.length+1,id=uid('c');
 project.cables.push({id,name:'Kabel '+n,color:COLORS[(n-1)%COLORS.length],group:'Weitere',diameter:12,route:[],a:null,b:null});
 cableId=id;filter='all';}));
$('deleteCable').addEventListener('click',()=>{if(!cable())return;change(()=>{project.cables=project.cables.filter(c=>c.id!==cableId);cableId=project.cables[0]?.id||null;});});
$('swapCable').addEventListener('click',()=>{const c=cable();if(c)change(()=>{const t=c.a;c.a=c.b;c.b=t;});});
$('cableName').addEventListener('change',()=>{const c=cable();if(c)change(()=>c.name=$('cableName').value.trim().slice(0,80)||'Kabel');});
$('cableColor').addEventListener('change',()=>{const c=cable();if(c)change(()=>{c.color=$('cableColor').value;});});
$('cableGroup').addEventListener('change',()=>{const c=cable();if(c)change(()=>{c.group=$('cableGroup').value;});});
$('cableDiameter').addEventListener('change',()=>{const c=cable();if(c)change(()=>{c.diameter=clamp(Number($('cableDiameter').value)||12,2,80);});});
$('addWaypoint').addEventListener('click',()=>{const c=cable();if(!c)return;
 const a=endpoint(c.a),b=endpoint(c.b),sz=sizes();if(!a||!b){alert('Bitte zuerst die Anschlüsse A und B auswählen.');return;}
 const vec=a.clone().lerp(b,.5);vec.y=clamp(vec.y+(.15),.05,sz.h-.05);
 change(()=>{if(c.route.length>=12){alert('Maximal 12 manuelle Bogenpunkte je Kabel.');return;}
 c.route.push([Math.round(vec.x*1000),Math.round(vec.y*1000),Math.round(vec.z*1000)]);});});
$('clearWaypoints').addEventListener('click',()=>{const c=cable();if(c&&c.route.length)change(()=>{c.route=[];});});
$('waypointList').addEventListener('change',e=>{const input=e.target.closest('[data-axis]');if(!input)return;
 const c=cable(),i=Number(input.closest('[data-index]').dataset.index),axis=Number(input.dataset.axis);
 if(!c||!c.route[i])return;const extent=axis===1?project.dims.h:(axis===0?project.dims.w/2:project.dims.d/2);
 change(()=>{c.route[i][axis]=clamp(Number(input.value)||0,axis===1?0:-extent,extent);});});
$('waypointList').addEventListener('click',e=>{const btn=e.target.closest('[data-remove]');if(!btn)return;
 const c=cable(),i=Number(btn.dataset.remove);if(c&&c.route[i])change(()=>{c.route.splice(i,1);});});

$('panelSelect').addEventListener('change',()=>{panelKey=$('panelSelect').value;renderAll();});
$('panelOpacity').addEventListener('input',()=>{$('panelOpacityText').textContent=$('panelOpacity').value+' %';});
$('applyPanel').addEventListener('click',()=>{
 const selected=plate();if(!selected)return;
 const group=panelKey.split('-')[0],enclosure=project.enclosures[group];
 const draft={...selected,material:$('panelMaterial').value.trim().slice(0,50)||selected.material,
 visible:$('panelVisible').checked,
 width:Number($('panelWidth').value),height:Number($('panelHeight').value),
 thickness:Number($('panelThickness').value),offset:Number($('panelOffset').value),
 opacity:Number($('panelOpacity').value)};
 change(()=>{
  const model={...enclosure,panels:enclosure.panels.map(p=>p.id===panelKey?draft:p)};
  const sanitized=components.normalizeEnclosures({[group]:model},project.dims)[group];
  enclosure.panels=sanitized.panels;
 });
});
$('supportList').addEventListener('click',e=>{
 const item=e.target.closest('[data-support]');if(!item)return;
 supportId=item.dataset.support;renderAll();
});
$('addSupport').addEventListener('click',()=>{
 if(project.supports.length>=100){alert('Maximal 100 Konsolen und Auflager.');return;}
 const next=components.newSupport(project.dims,uid('support'));
 change(()=>{project.supports.push(next);supportId=next.id;});
});
$('applySupport').addEventListener('click',()=>{
 const st=support();if(!st)return;
 const data={...st,name:$('supportName').value.trim().slice(0,80)||'Konsole',
 type:$('supportType').value,wall:$('supportWall').value,visible:$('supportVisible').checked};
 for(const k of ['X','Y','Z','W','H','D'])data[k.toLowerCase()]=Number($('support'+k).value);
 change(()=>Object.assign(st,components.normalizeSupports([data],project.dims)[0]));
});
$('deleteSupport').addEventListener('click',()=>{
 if(!support())return;change(()=>{
 project.supports=project.supports.filter(x=>x.id!==supportId);supportId=project.supports[0]?.id||null;
 });
});
$('variantSelect').addEventListener('change',()=>{
 variantId=$('variantSelect').value||null;renderUI();
});
$('createVariant').addEventListener('click',()=>{
 if(project.variants.length>=12){alert('Bis zu 12 Varianten pro Projektdatei sind möglich.');return;}
 const name=prompt('Wie soll dieser Planungsstand heißen?','Variante '+(project.variants.length+1));
 if(name===null)return;
 const cleaned=name.trim().slice(0,80)||'Variante '+(project.variants.length+1);
 const snap=components.snapshot(project),id=uid('variant');
 change(()=>{
 project.variants.push({id,name:cleaned,createdAt:new Date().toISOString(),snapshot:snap});
 variantId=id;
 });
});
$('restoreVariant').addEventListener('click',()=>{
 const v=currentVariant();if(!v||!confirm('Variante „'+v.name+'“ laden? Die aktuelle Planung wird ersetzt, bleibt aber über Rückgängig erreichbar.'))return;
 const snapshot=JSON.parse(JSON.stringify(v.snapshot)),all=project.variants;
 change(()=>{
 project=validProject({...snapshot,variants:all,network:project.network,activeShaftId:project.activeShaftId});
 wallId='SW1';holeId=null;cableId=project.cables[0]?.id||null;
 supportId=project.supports[0]?.id||null;panelKey='OLA-floor';filter='all';
 });
});
$('deleteVariant').addEventListener('click',()=>{
 const v=currentVariant();if(!v||!confirm('Variante „'+v.name+'“ löschen?'))return;
 change(()=>{project.variants=project.variants.filter(x=>x.id!==v.id);variantId=project.variants[0]?.id||null;});
});

for(const k of ['ola','ms']){const key=k.toUpperCase();
 $(k+'Visible').addEventListener('change',()=>change(()=>{project.enclosures[key].visible=$(k+'Visible').checked;}));
 $(k+'Opacity').addEventListener('input',()=>{$(k+'OpacityText').textContent=$(k+'Opacity').value+' %';});
 $(k+'Opacity').addEventListener('change',()=>change(()=>{project.enclosures[key].opacity=Number($(k+'Opacity').value);}));
}
for(const [el,key,min,max] of [['wallThickness','thickness',40,500],['wallDn','dn',20,300]])
 $(el).addEventListener('change',()=>change(()=>{wall()[key]=clamp(Number($(el).value)||wall()[key],min,max);}));

for(const [field,key] of [['cableA','a'],['cableB','b']])$(field).addEventListener('change',()=>{const c=cable();if(c)change(()=>{c[key]=parseEndpoint($(field).value);});});
$('projectName').addEventListener('change',()=>change(()=>project.name=$('projectName').value.trim().slice(0,80)||'Neues Projekt'));
for(const [el,key,min,max] of [['width','w',600,12000],['depth','d',600,12000],['height','h',600,8000]])
 $(el).addEventListener('change',()=>{const x=clamp(Number($(el).value)||project.dims[key],min,max);change(()=>{project.dims[key]=x;});});
$('opacity').addEventListener('change',()=>change(()=>project.opacity=Number($('opacity').value)));
$('opacity').addEventListener('input',()=>{$('opacityText').textContent=$('opacity').value+' %';});
$('showBox').addEventListener('change',()=>change(()=>project.showBox=$('showBox').checked));
$('showLabels').addEventListener('change',()=>change(()=>project.showLabels=$('showLabels').checked));
$('resetView').addEventListener('click',()=>{azimuth=.72;elevation=.42;zoom=1;view();});
$('viewTabs').addEventListener('click',e=>{
 const b=e.target.closest('[data-view]');if(!b)return;
 viewMode=b.dataset.view;if(viewMode.startsWith('SW')){wallId=viewMode;holeId=null;}zoom=1;renderAll();
});
$('undo').addEventListener('click',()=>undoRedo(true));$('redo').addEventListener('click',()=>undoRedo(false));
document.addEventListener('keydown',e=>{if((e.ctrlKey||e.metaKey)&&e.key.toLowerCase()==='z'){e.preventDefault();undoRedo(!e.shiftKey);}else if((e.ctrlKey||e.metaKey)&&e.key.toLowerCase()==='y'){e.preventDefault();undoRedo(false);}});
function download(name,data,type){const blob=data instanceof Blob?data:new Blob([data],{type}),href=URL.createObjectURL(blob),a=document.createElement('a');a.href=href;a.download=name;document.body.append(a);a.click();a.remove();setTimeout(()=>URL.revokeObjectURL(href),1500);}
function filename(){return project.name.replace(/[^\p{L}\p{N}_-]+/gu,'_').slice(0,50)||'Schachtwand';}
$('saveProject').addEventListener('click',()=>download(filename()+'.json',JSON.stringify(project,null,2),'application/json'));
$('openProject').addEventListener('click',()=>$('fileInput').click());
$('fileInput').addEventListener('change',async e=>{const f=e.target.files?.[0];if(!f)return;
 try{const next=validProject(JSON.parse(await f.text()));change(()=>{project=next;wallId='SW1';holeId=null;cableId=project.cables[0]?.id||null;});}
 catch(ex){alert('Projektdatei konnte nicht geladen werden: '+ex.message);}e.target.value='';});
$('newProject').addEventListener('click',()=>{if(!confirm('Neue leere Planung starten? Die vorherige Planung bleibt über Rückgängig erreichbar.'))return;
 change(()=>{project=demo();project.name='Neue Schachtwandplanung';project.cables=[];
 project.walls.forEach(w=>{w.holes=[];w.rows=3;w.cols=4;});
 wallId='SW1';holeId=null;cableId=null;filter='all';viewMode='3D';
 panelKey='OLA-floor';supportId=null;variantId=null;
 project.network=networkModel.createNetwork(project);project.activeShaftId='S1';
 project=validProject(project);
 networkOpen=false;netRouteId=null;netSegmentShaftId='S1';});});
$('exportPng').addEventListener('click',()=>{renderer.render(scene,activeCamera);renderer.domElement.toBlob(blob=>{if(blob)download(filename()+'.png',blob,'image/png');},'image/png');});
function resize(){
 const rect=$('scene').getBoundingClientRect();
 // Never shrink WebGL to 10×10 while the network overlay hides the canvas.
 // Hidden canvas sizing could leave a blank viewport after returning to 3D.
 if(rect.width<20||rect.height<20)return;
 renderer.setSize(Math.max(10,rect.width),Math.max(10,rect.height),false);
 camera.aspect=rect.width/Math.max(1,rect.height);
 camera.updateProjectionMatrix();view();draw2D();
 renderer.render(scene,activeCamera);
}
new ResizeObserver(resize).observe($('viewport'));
new ResizeObserver(()=>draw2D()).observe($('draft2d'));
function animate(){requestAnimationFrame(animate);renderer.render(scene,activeCamera);}
renderAll();persist();resize();animate();
document.body.classList.add('app-ready');
})();