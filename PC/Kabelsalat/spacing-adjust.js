'use strict';
/* KAB-0051..0053 · Explicit, non-normative, non-destructive wall spacing.
 * The solver runs ONLY on synthetic or user-authored shafts without sourceInfo.
 * Original PDF/HTML reference projects MUST NOT be automatically relocated.
 * All inputs and outputs are schematic millimetres / normalized U,V.
 */
(function(root,factory){
 const api=factory();
 if(typeof module==='object'&&module.exports)module.exports=api;
 if(root)root.KabelsalatSpacingAdjust=api;
})(typeof globalThis!=='undefined'?globalThis:this,function(){
 const round=v=>Math.round(v*1e8)/1e8;
 const integer=v=>Number.isInteger(v)&&v>=0&&v<=1000;
 const getRadius=h=>{
  const physical=h?.dimensionSpec?.openingMm;
  const diameter=Number.isFinite(physical)&&physical>=20&&physical<=600?
   physical:Number(h?.dn);
  if(!Number.isFinite(diameter)||diameter<20||diameter>600)
   throw Error('Öffnungsmaß/DN ungültig: nur Modellwerte 20–600 mm');
  return diameter/2;
 };
 function spanMm(p,wallId){
  if(!p?.dims||!['SW1','SW2','SW3','SW4'].includes(wallId))throw Error('Ungültige Wand');
  // PDF projects are refused, so only the legacy/synthetic-axis convention applies.
  const v=wallId==='SW1'||wallId==='SW3'?p.dims.d:p.dims.w;
  if(!Number.isFinite(v)||v<=0||!Number.isFinite(p.dims.h)||p.dims.h<=0)
   throw Error('Ungültige Schachtabmessungen');
  return v;
 }
 function snapshot(p,wallId){
  const w=p.walls?.find(x=>x.id===wallId);
  if(!w||!Array.isArray(w.holes))throw Error('Wanddaten fehlen');
  return JSON.stringify({dims:p.dims,wallId,
   holes:w.holes.map(h=>({id:h.id,u:h.u,v:h.v,dn:h.dn,openingMm:h.dimensionSpec?.openingMm}))});
 }
 function metrics(items,span,height){
  let minGap=null,minEdge=null,overlap=0,outer=0;
  for(let i=0;i<items.length;i++){
   const a=items[i],edge=Math.min(a.x-a.r,span-a.x-a.r,a.y-a.r,height-a.y-a.r);
   minEdge=minEdge===null?edge:Math.min(minEdge,edge);
   if(edge<-.01)outer++;
   for(let j=0;j<i;j++){
    const b=items[j],gap=Math.hypot(a.x-b.x,a.y-b.y)-a.r-b.r;
    minGap=minGap===null?gap:Math.min(minGap,gap);
    if(gap<-.01)overlap++;
   }
  }
  return {minGapMm:minGap===null?null:Math.round(minGap*10)/10,
   minEdgeMm:minEdge===null?null:Math.round(minEdge*10)/10,
   overlaps:overlap,edgeCrossings:outer};
 }
 function plan(p,wallId,{gapMm=0,edgeMm=0}={}){
  if(p?.sourceInfo)throw Error('Original-/PDF-Referenz geschützt: Abstandregler nur in selbst erstellten oder fiktiven Schächten');
  if(!integer(gapMm)||!integer(edgeMm))throw Error('Abstand und Rand müssen ganze 0–1000 mm sein');
  const span=spanMm(p,wallId),height=p.dims.h,wall=p.walls.find(x=>x.id===wallId);
  if(!wall.holes.length)throw Error('Keine Leerrohre in gewählter Wand');
  if(wall.holes.length>160)throw Error('Maximal 160 Leerrohre pro Optimierungsschritt');
  const ids=new Set();
  const original=wall.holes.map(h=>{
   if(!h||typeof h.id!=='string'||ids.has(h.id)||!Number.isFinite(h.u)||!Number.isFinite(h.v)||
      h.u<0||h.u>1||h.v<0||h.v>1)throw Error('Rohr-ID oder normierte Position ungültig');
   ids.add(h.id);
   const r=getRadius(h);
   if(2*(r+edgeMm)>span||2*(r+edgeMm)>height)
    throw Error('Öffnung + Randabstand passt nicht in die Wand');
   return {id:h.id,x:h.u*span,y:h.v*height,r,u:h.u,v:h.v};
  });
  const list=original.map(x=>({...x})),before=metrics(list,span,height);
  let converged=false;
  // Gauss-Seidel relaxer: circular pair constraints and wall-edge constraints.
  // No RNG or hidden movements. Deterministic tie-breaking for exact coincident centers.
  for(let iteration=0;iteration<350;iteration++){
   let biggest=0;
   for(const a of list){
    const x=a.x,y=a.y;
    a.x=Math.min(span-edgeMm-a.r,Math.max(edgeMm+a.r,a.x));
    a.y=Math.min(height-edgeMm-a.r,Math.max(edgeMm+a.r,a.y));
    biggest=Math.max(biggest,Math.hypot(a.x-x,a.y-y));
   }
   for(let i=0;i<list.length;i++)for(let j=0;j<i;j++){
    const a=list[i],b=list[j],dx=a.x-b.x,dy=a.y-b.y,dist=Math.hypot(dx,dy),
     need=a.r+b.r+gapMm;
    if(dist>=need-.00005)continue;
    const ux=dist>1e-9?dx/dist:((i+j)%2?1:0),
     uy=dist>1e-9?dy/dist:((i+j)%2?0:1);
    const shift=(need-dist)/2;
    a.x+=ux*shift;a.y+=uy*shift;b.x-=ux*shift;b.y-=uy*shift;
    biggest=Math.max(biggest,shift);
   }
   if(biggest<.00005){converged=true;break;}
  }
  // Final clamp + exact constraints check. Never accept a nearly packed arrangement
  // just because the chosen fixed iteration budget ran out.
  for(const a of list){
   a.x=Math.min(span-edgeMm-a.r,Math.max(edgeMm+a.r,a.x));
   a.y=Math.min(height-edgeMm-a.r,Math.max(edgeMm+a.r,a.y));
  }
  const after=metrics(list,span,height);
  let minGap=Infinity,minEdge=Infinity;
  for(let i=0;i<list.length;i++){
   const a=list[i];
   minEdge=Math.min(minEdge,a.x-a.r,span-a.x-a.r,a.y-a.r,height-a.y-a.r);
   for(let j=0;j<i;j++){const b=list[j];
    minGap=Math.min(minGap,Math.hypot(a.x-b.x,a.y-b.y)-a.r-b.r);
   }
  }
  if(minEdge<edgeMm-.01||minGap<gapMm-.01)
   throw Error('Anordnung nicht lösbar: Zielabstände auf dieser Wand zu groß (keine Änderungen übernommen)');
  const changes=list.map((a,i)=>({
   id:a.id,from:{u:original[i].u,v:original[i].v},
   to:{u:round(a.x/span),v:round(a.y/height)},
   deltaMm:Math.round(Math.hypot(a.x-original[i].x,a.y-original[i].y)*10)/10
  })).filter(x=>x.deltaMm>.01);
  return {wallId,gapMm,edgeMm,base:snapshot(p,wallId),before,after,changes,
   converged,moved:changes.length,total:list.length,source:'schematic-only'};
 }
 function apply(p,preview){
  if(!preview||!p||p.sourceInfo||snapshot(p,preview.wallId)!==preview.base)
   throw Error('Die Vorschau ist veraltet oder die Originalquelle geschützt: erneut berechnen');
  const wall=p.walls.find(w=>w.id===preview.wallId);
  const updated=new Map(preview.changes.map(x=>[x.id,x.to]));
  for(const h of wall.holes){const next=updated.get(h.id);if(next){h.u=next.u;h.v=next.v;}}
  // Cable endpoints, pipe IDs, physical dimensions and source fields untouched.
  return preview.moved;
 }
 return {spanMm,metrics,plan,apply,snapshot};
});
