/* Kabelsalat: transparent, configurable geometry hints and unverified review metadata.
 * Not a DIN/VDE standard checker, engineering release, or proof of cable continuity.
 */
(function(root,factory){
 const api=factory();
 if(typeof module==='object'&&module.exports)module.exports=api;
 if(root)root.KabelsalatQuality=api;
})(typeof window!=='undefined'?window:null,function(){
'use strict';
const saneText=(v,limit=200)=>typeof v==='string'?v.trim().slice(0,limit):'';
const STATES=['Entwurf','Zur Prüfung','Vor Ort abgeglichen'];
function threshold(v){
 if(v===null||v===undefined||v==='')return null;
 const n=Number(v);
 return Number.isFinite(n)&&n>=0&&n<=100000?Math.round(n*10)/10:null;
}
function normalizeSettings(s){
 return {minPipeGapMm:threshold(s?.minPipeGapMm),
 minEdgeMm:threshold(s?.minEdgeMm),minBendRadiusMm:threshold(s?.minBendRadiusMm)};
}
function normalizeReviews(items,shafts){
 const ids=new Set(shafts.map(x=>x.id)),keys=new Set();
 if(!Array.isArray(items))return [];
 return items.slice(0,1500).filter(x=>x&&ids.has(x.shaftId)).map(x=>{
  const shaft=shafts.find(s=>s.id===x.shaftId),entityType=x.entityType==='cable'?'cable':'shaft';
  const entityId=entityType==='shaft'?x.shaftId:String(x.entityId||'').slice(0,120);
  if(entityType==='cable'&&!shaft.snapshot.cables.some(c=>c.id===entityId))return null;
  const key=x.shaftId+'|'+entityType+'|'+entityId;
  if(keys.has(key))return null;keys.add(key);
  return {shaftId:x.shaftId,entityType,entityId,
   status:STATES.includes(x.status)?x.status:'Entwurf',
   source:saneText(x.source,250),note:saneText(x.note,500),
   checkedBy:saneText(x.checkedBy,80),checkedAt:saneText(x.checkedAt,45)};
 }).filter(Boolean);
}
function normalizeAudit(events){
 return (Array.isArray(events)?events:[]).slice(-500).filter(x=>x&&typeof x==='object')
 .map(x=>({at:saneText(x.at,45),shaftId:saneText(x.shaftId,100),
 action:saneText(x.action,120),description:saneText(x.description,300)}));
}
function searchNetwork(net,query,limit=120){
 const q=String(query||'').toLocaleLowerCase('de-DE').trim();
 if(!q)return [];
 const items=[];
 const match=(...args)=>args.some(x=>String(x??'').toLocaleLowerCase('de-DE').includes(q));
 for(const shaft of net.shafts||[]){
  if(match(shaft.id,shaft.name,shaft.x,shaft.z))
   items.push({kind:'shaft',shaftId:shaft.id,label:shaft.id+' · '+shaft.name});
  for(const c of shaft.snapshot.cables||[]){
   if(match(c.id,c.name,c.group))
    items.push({kind:'cable',shaftId:shaft.id,cableId:c.id,
     label:shaft.id+' · '+c.name+' ['+c.id+']'});
  }
  for(const w of shaft.snapshot.walls||[]){
   for(const h of w.holes||[]){
    if(match(h.id,w.id+'/'+h.id))
     items.push({kind:'hole',shaftId:shaft.id,wallId:w.id,holeId:h.id,
      label:shaft.id+' · '+w.id+' · '+h.id});
   }
  }
  if(items.length>=limit)break;
 }
 return items.slice(0,limit);
}
function mmPosition(p,r){
 if(!r||typeof r.wall!=='string'||typeof r.hole!=='string')return null;
 const w=p.dims.w,d=p.dims.d,h=p.dims.h;
 const wall=p.walls.find(x=>x.id===r.wall),hole=wall?.holes.find(x=>x.id===r.hole);
 if(!hole)return null;
 const u=hole.u,v=hole.v;
 if(p.sourceInfo?.sourceVersion?.startsWith('PDF V2')){
  if(r.wall==='SW1')return [-w/2+w*u,h*v,-d/2+24];
  if(r.wall==='SW2')return [w/2-24,h*v,-d/2+d*u];
  if(r.wall==='SW3')return [w/2-w*u,h*v,d/2-24];
  if(r.wall==='SW4')return [-w/2+24,h*v,d/2-d*u];
 }
 if(r.wall==='SW1')return [-w/2,h*v,-d/2+d*u];
 if(r.wall==='SW3')return [w/2,h*v,-d/2+d*u];
 if(r.wall==='SW2')return [-w/2+w*u,h*v,-d/2];
 if(r.wall==='SW4')return [-w/2+w*u,h*v,d/2];
 return null;
}
function circumradius(a,b,c){
 const u=a.map((n,i)=>b[i]-n),v=a.map((n,i)=>c[i]-n);
 const l=Math.hypot(...u),m=Math.hypot(...v),p=Math.hypot(...a.map((n,i)=>c[i]-b[i]));
 const cross=[u[1]*v[2]-u[2]*v[1],u[2]*v[0]-u[0]*v[2],u[0]*v[1]-u[1]*v[0]];
 const doubleArea=Math.hypot(...cross);
 if(doubleArea<.000001)return Infinity;
 return l*m*p/(2*doubleArea);
}
function physicalOpening(h){
 const val=h?.dimensionSpec?.openingMm;
 return Number.isFinite(val)&&val>=20&&val<=600?val:Number(h?.dn)||70;
}
function effectiveInner(h){
 const val=h?.dimensionSpec?.innerMm;
 return Number.isFinite(val)&&val>=20&&val<=600?val:Number(h?.dn)||70;
}
function evaluate(net,settings){
 const cfg=normalizeSettings(settings),issues=[];
 function add(severity,code,shaftId,ref,detail){
  if(issues.length<800)issues.push({severity,code,shaftId,ref,detail});
 }
 for(const shaft of net.shafts||[]){
  const p=shaft.snapshot;
  if(p.sourceInfo){
   add('info','SCHEMATIC_REFERENCE',shaft.id,shaft.id,
    'HTML-Referenz '+p.sourceInfo.shaft+': Abmessungen, nominale Rohr-DN und Kabelführung sind nicht bestätigt');
   if(p.sourceInfo.planAnchors?.length){
    add('info','PLAN_MARKERS_UNCONFIRMED',shaft.id,shaft.id,
     p.sourceInfo.planAnchors.length+' Planmarker sind keine bestätigten physischen Leerrohre');
   }
  }
  for(const wall of p.walls||[]){
   const pdf=p.sourceInfo?.sourceVersion?.startsWith('PDF V2');
   const span=wall.id==='SW1'||wall.id==='SW3'?
    (pdf?p.dims.w:p.dims.d):(pdf?p.dims.d:p.dims.w);
   for(let i=0;i<wall.holes.length;i++){
    const a=wall.holes[i],r=physicalOpening(a)/2,ax=a.u*span,ay=a.v*p.dims.h;
    const edge=Math.min(ax-r,span-ax-r,ay-r,p.dims.h-ay-r);
    if(edge<0)add('error','OUTSIDE_WALL',shaft.id,a.id,
     wall.id+' / '+a.id+': Rohröffnung überschreitet Wandrand um '+Math.round(-edge)+' mm');
    else if(cfg.minEdgeMm!==null&&edge<cfg.minEdgeMm)
     add('warning','EDGE_CLEARANCE',shaft.id,a.id,
      wall.id+' / '+a.id+': Randabstand '+Math.round(edge)+' mm unter Projektwert '+cfg.minEdgeMm+' mm');
    for(let j=0;j<i;j++){
     const b=wall.holes[j],br=physicalOpening(b)/2;
     const gap=Math.hypot(ax-b.u*span,ay-b.v*p.dims.h)-r-br;
     if(gap<0)add('error','PIPE_OVERLAP',shaft.id,a.id,
      wall.id+': '+a.id+' und '+b.id+' überschneiden sich geometrisch ('+Math.round(-gap)+' mm)');
     else if(cfg.minPipeGapMm!==null&&gap<cfg.minPipeGapMm)
      add('warning','PIPE_CLEARANCE',shaft.id,a.id,
       wall.id+': '+a.id+' und '+b.id+' Abstand '+Math.round(gap)+' mm unter Projektwert '+cfg.minPipeGapMm+' mm');
    }
   }
  }
  for(const cable of p.cables||[]){
   for(const key of ['a','b']){
    const end=cable[key],wall=p.walls.find(x=>x.id===end?.wall),
      hole=wall?.holes.find(x=>x.id===end?.hole);
    if(!hole){add('warning','MISSING_END',shaft.id,cable.id,cable.name+': Anschluss '+key.toUpperCase()+' fehlt');continue;}
    if(Number(cable.diameter)>effectiveInner(hole))
     add('error','CABLE_TOO_WIDE',shaft.id,cable.id,
      cable.name+': Kabel-Ø '+cable.diameter+' mm größer als '+
      (hole.dimensionSpec?.innerMm?'dokumentierter Rohr-Innen-Ø ':'schematischer Leerrohr-DN ')+
      effectiveInner(hole)+' mm ('+key.toUpperCase()+')');
   }
   if(cfg.minBendRadiusMm!==null){
    const points=[];
    const first=mmPosition(p,cable.a),last=mmPosition(p,cable.b);
    if(first)points.push(first);
    for(const r of cable.route||[])if(Array.isArray(r)&&r.length===3)points.push(r);
    if(last)points.push(last);
    if(points.length<3){
      add('info','BEND_UNCHECKABLE',shaft.id,cable.id,
       cable.name+': Näherungs-Biegeradius mangels Stützpunkten nicht beurteilbar');
    }else{
      const radii=[];for(let i=1;i<points.length-1;i++)radii.push(circumradius(points[i-1],points[i],points[i+1]));
      const radius=Math.min(...radii);
      if(radius<cfg.minBendRadiusMm)
       add('warning','BEND_PROXY',shaft.id,cable.id,
        cable.name+': Drei-Punkt-Bogenradius ca. '+Math.round(radius)+' mm unter Projektwert '+cfg.minBendRadiusMm+
        ' mm (nur Kontrollpunkt-Näherung, NICHT der tatsächliche Mindestbiegeradius)');
    }
   }
  }
 }
 for(const route of net.routes||[]){
  if(route.steps.length<2)add('warning','ROUTE_INCOMPLETE',route.steps[0]?.shaftId||'',route.id,
   'Netzkabelweg '+route.name+' hat weniger als zwei Segmente');
  for(let i=0;i<route.steps.length;i++){
   const step=route.steps[i],shaft=net.shafts.find(s=>s.id===step.shaftId);
   if(!shaft||!shaft.snapshot.cables.some(c=>c.id===step.cableId))
    add('error','UNKNOWN_SEGMENT',step.shaftId,route.id,
     'Netzkabelweg '+route.name+': Segment '+step.cableId+' in '+step.shaftId+' fehlt');
   if(i>0){
    const before=route.steps[i-1];
    if(!net.links.some(l=>(l.from===before.shaftId&&l.to===step.shaftId)||
      (l.to===before.shaftId&&l.from===step.shaftId)))
     add('warning','UNLINKED_SHAFT',step.shaftId,route.id,
      'Netzkabelweg '+route.name+': keine Verbindung '+before.shaftId+' → '+step.shaftId);
   }
  }
 }
 return {config:cfg,issues,counts:{error:issues.filter(x=>x.severity==='error').length,
 warning:issues.filter(x=>x.severity==='warning').length,
 info:issues.filter(x=>x.severity==='info').length},
 advisory:'Nur Daten- und Geometriehinweise. Keine DIN-/VDE-/Brandschutz- oder Abnahmeprüfung.'};
}
return {STATES,normalizeSettings,normalizeReviews,normalizeAudit,searchNetwork,mmPosition,
circumradius,evaluate};
});
