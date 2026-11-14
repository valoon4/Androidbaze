'use strict';
/* KAB-0361..0378 · source-aware global XYZ cable-segment assembly.
 * Coordinates are SCHEMATIC model millimetres, not measured cable routes.
 * Each local cable is an independent polyline; a bare graph link is NEVER
 * joined across shafts as a fabricated physical duct/cable segment.
 */
(function(root,factory){
 const api=factory();
 if(typeof module==='object'&&module.exports)module.exports=api;
 if(root)root.KabelsalatWorldRoute=api;
})(typeof globalThis!=='undefined'?globalThis:this,function(){
 const round=v=>Math.round(v*1000)/1000;
 const own=(o,k)=>Object.prototype.hasOwnProperty.call(o,k);
 const validMm=v=>Number.isFinite(v)&&Math.abs(v)<=1e8;
 const limitPoint=p=>Array.isArray(p)&&p.length===3&&p.every(validMm);
 const distance=(a,b)=>Math.hypot(a[0]-b[0],a[1]-b[1],a[2]-b[2]);
 const error=(code,context)=>Object.assign(new Error(context),{code});
 function holePosition(wall,h,dims,sourceInfo){
  if(!['SW1','SW2','SW3','SW4'].includes(wall))
   throw error('BAD_WALL','Unbekannte Schachtwand '+String(wall));
  if(!h||!validMm(h.u)||!validMm(h.v)||h.u<0||h.u>1||h.v<0||h.v>1)
   throw error('BAD_HOLE','Lochkoordinaten ungültig');
  const {w,d,h:height}=dims,u=h.u,v=h.v,y=height*v;
  if(sourceInfo?.sourceVersion?.startsWith('PDF V2')){
   switch(wall){
    case 'SW1':return [-w/2+w*u,y,-d/2+24];
    case 'SW2':return [w/2-24,y,-d/2+d*u];
    case 'SW3':return [w/2-w*u,y,d/2-24];
    default:return [-w/2+24,y,d/2-d*u];
   }
  }
  // Legacy HTML shaft axis mapping retained exactly as app.js holePosition.
  switch(wall){
   case 'SW1':return [-w/2+24,y,-d/2+d*u];
   case 'SW2':return [-w/2+w*u,y,-d/2+24];
   case 'SW3':return [w/2-24,y,-d/2+d*u];
   default:return [-w/2+w*u,y,d/2-24];
  }
 }
 function snapshotShaft(s){
  if(!s||!validMm(s.x)||!validMm(s.z)||!s.snapshot||
     !Array.isArray(s.snapshot.walls)||!Array.isArray(s.snapshot.cables)||
     !s.snapshot.dims)throw error('BAD_SHAFT','Ungültige Schacht-Snapshotdaten');
  const dim=s.snapshot.dims;
  if(!['w','d','h'].every(k=>validMm(dim[k])&&dim[k]>0&&dim[k]<=12000))
   throw error('BAD_DIMS','Schachtmaße ungültig');
  return s.snapshot;
 }
 function segment(net,step,index){
  const shaft=net.shafts.find(s=>s.id===step?.shaftId);
  if(!shaft)return {index:index+1,shaftId:step?.shaftId||'?',cableId:step?.cableId||'?',
   status:'missing-shaft',points:[],issues:['Unbekannter Schacht'],distanceMm:null};
  const snap=snapshotShaft(shaft);
  const cable=snap.cables.find(c=>c.id===step?.cableId);
  if(!cable)return {index:index+1,shaftId:shaft.id,cableId:step?.cableId||'?',
   status:'missing-cable',points:[],issues:['Lokale Kabel-ID unbekannt'],distanceMm:null};
  const issues=[],dims=snap.dims;
  const resolve=(ref,letter)=>{
   if(!ref||!['SW1','SW2','SW3','SW4'].includes(ref.wall)){
    issues.push('Anschluss '+letter+' nicht zugeordnet');return null;
   }
   const wall=snap.walls.find(w=>w.id===ref.wall);
   const hole=wall?.holes?.find(h=>h.id===ref.hole);
   if(!hole){issues.push('Anschluss '+letter+' referenziert unbekanntes Leerrohr');return null;}
   try{return holePosition(ref.wall,hole,dims,snap.sourceInfo);}catch(e){
    issues.push('Anschluss '+letter+': '+e.message);return null;
   }
  };
  const first=resolve(cable.a,'A'),last=resolve(cable.b,'B');
  // Local route waypoints are model shaft-centered XYZ coordinates in mm,
  // identical to app.js cableRenderPath's c.route interpretation.
  const waypoints=Array.isArray(cable.route)?cable.route:[];
  if(waypoints.length>12)issues.push('Mehr als zwölf lokale Wegpunkte: nicht interpretierbar');
  const clean=waypoints.length<=12&&waypoints.every(limitPoint);
  if(!clean)issues.push('Lokale Wegpunkte ungültig (nur 3D-Modell-mm erlaubt)');
  const local=first&&last&&clean?[first,...waypoints.map(p=>p.slice()),last]:[];
  // Only add an explicitly provided, finite vertical shaft offset. Old
  // X/Z-only shafts keep local Y as local model height with no global datum.
  const yKnown=typeof shaft.y==='number'&&Number.isInteger(shaft.y)&&Math.abs(shaft.y)<=100000000;
  const world=local.map(p=>[round(p[0]+shaft.x),round(p[1]+(yKnown?shaft.y:0)),round(p[2]+shaft.z)]);
  let length=0;
  for(let j=1;j<world.length;j++)length+=distance(world[j-1],world[j]);
  return {
   index:index+1,shaftId:shaft.id,cableId:cable.id,group:cable.group||'Weitere',
   status:issues.length?'incomplete':'schematic',
   issues,points:world,localPoints:local,
   localEndpointA:cable.a?{wall:cable.a.wall,hole:cable.a.hole}:null,
   localEndpointB:cable.b?{wall:cable.b.wall,hole:cable.b.hole}:null,
   modelPolylineMm:world.length?round(length):null,
   pointSource:yKnown?'local-shaft-XYZ+explicit-shaft-XYZ':'local-shaft-XYZ+schematic-shaft-XZ',
   verticalShaftOffsetMm:yKnown?shaft.y:null,
   globalElevationReferenceKnown:false,
   sourceVersion:snap.sourceInfo?.sourceVersion||null,
   verified:false
  };
 }
 function assemble(net,route){
  if(!net||!Array.isArray(net.shafts)||!Array.isArray(net.links)||!route||
    !Array.isArray(route.steps)||route.steps.length>60)
   throw error('BAD_ROUTE','Netz-Kabelweg muss 0–60 Segmente enthalten');
  if(net.shafts.length>30||net.links.length>150)throw error('SIZE','Netz größer als 30 Schächte/150 Links');
  const segments=route.steps.map((step,i)=>segment(net,step,i));
  const gaps=[];
  for(let i=1;i<route.steps.length;i++){
   const from=route.steps[i-1]?.shaftId,to=route.steps[i]?.shaftId,
    link=net.links.find(l=>l.from===from&&l.to===to||l.from===to&&l.to===from);
   const before=segments[i-1],after=segments[i];
   const a=before.points?.at(-1),b=after.points?.[0];
   gaps.push({from,to,linkId:link?.id||null,fromXYZ:a||null,toXYZ:b||null,
    airGapMm:a&&b?round(distance(a,b)):null,
    physicalConnectionVerified:false,unknownDuct:true,
    status:!link?'missing-link':a&&b?'unverified-cross-shaft-gap':'insufficient-local-geometry'});
  }
  const issues=[...segments.flatMap(s=>s.issues.map(q=>s.shaftId+'/'+s.cableId+': '+q)),
   ...gaps.filter(g=>!g.linkId).map(g=>'Kein Netzlink '+g.from+' → '+g.to)];
  const totalLocal=segments.every(s=>s.modelPolylineMm!==null)?
   round(segments.reduce((sum,s)=>sum+s.modelPolylineMm,0)):null;
  // Deliberately NEVER add unverified gaps to route length.
  const complete=route.steps.length>=2&&segments.length>=2&&
   segments.every(s=>s.status==='schematic')&&gaps.every(g=>g.linkId);
  return {routeId:route.id||null,segmentCount:segments.length,segments,gaps,
   status:issues.length?'incomplete':complete?'schematic-unverified':'incomplete',
   localModelPolylineTotalMm:totalLocal,
   actualCableLengthMm:null,physicalContinuityVerified:false,
   issues,warnings:[
    'Lokale XYZ-Polylinien sind schematische Modellwerte, keine gemessenen Kabelwege.',
    'Querungen zwischen Schächten bleiben Lücken ohne nachgewiesene Rohre und Anschlussbelege.',
    'Luftlinien und unbekannte Übergänge sind NICHT in der lokalen Modelllänge enthalten.'
   ],origin:'stored-shaft-snapshots',
   coordinateReference:typeof net.coordinateReference==='string'&&net.coordinateReference.trim()?
    net.coordinateReference.trim():null,
   coordinateReferenceVerified:false,verified:false};
 }
 function safeCell(x){
  return '"'+String(x??'').replace(/^[=+\-@]/,c=>"'"+c).replaceAll('"','""')+'"';
 }
 function csv(report){
  if(!report||!Array.isArray(report.segments)||!Array.isArray(report.gaps)||
    report.segments.length>60||report.gaps.length>59)
   throw error('BAD_EXPORT','Ungültiger Routenbericht');
  const rows=[['Art','Segment','Schacht','Kabel','Schritt','X_mm','Y_mm','Z_mm',
   'Status','Quellversion','Hinweis']];
  for(const seg of report.segments){
   if(!Array.isArray(seg.points)||seg.points.length>14)throw error('BAD_EXPORT','Zu viele Punkte');
   for(let i=0;i<seg.points.length;i++){
    const xyz=seg.points[i];if(!limitPoint(xyz))throw error('BAD_EXPORT','Ungültiger Exportpunkt');
    rows.push(['lokal',seg.index,seg.shaftId,seg.cableId,i+1,...xyz,
     seg.status,seg.sourceVersion||'kein PDF-V2-Quellbeleg','Schematische Modell-Koordinate; nicht vermessen']);
   }
  }
  for(let i=0;i<report.gaps.length;i++){
   const gap=report.gaps[i];
   rows.push(['unbekannter Übergang',i+1,gap.from+' → '+gap.to,'','', '', '', '',
    gap.status,'','Keine geprüfte physische Verbindung; nicht interpolieren']);
  }
  return '\uFEFF'+rows.map(row=>row.map(safeCell).join(';')).join('\r\n')+'\r\n';
 }
 return {holePosition,snapshotShaft,segment,assemble,csv};
});
