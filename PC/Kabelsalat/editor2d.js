/* Kabelsalat 2D geometry: all object coordinates use millimetres. */
(function(root,factory){
  const api=factory();
  if(typeof module==='object'&&module.exports)module.exports=api;
  if(root)root.Kabelsalat2D=api;
})(typeof window!=='undefined'?window:null,function(){
'use strict';
const clamp=(v,a,b)=>Math.min(b,Math.max(a,v));
function metrics(spanMm,heightMm,widthPx,heightPx,pad=29){
  if(![spanMm,heightMm,widthPx,heightPx].every(n=>Number.isFinite(n)&&n>0))
    throw new RangeError('Dimensions must be positive numbers');
  const availableW=Math.max(1,widthPx-2*pad),availableH=Math.max(1,heightPx-2*pad);
  const scale=Math.min(availableW/spanMm,availableH/heightMm);
  return {spanMm,heightMm,widthPx,heightPx,pad,scale,
    left:(widthPx-spanMm*scale)/2,top:(heightPx-heightMm*scale)/2};
}
function toPixels(m,xMm,yMm){
  return {x:m.left+xMm*m.scale,y:m.top+(m.heightMm-yMm)*m.scale};
}
function toMillimetres(m,xPx,yPx,step=1){
  step=Math.max(1,Math.round(step));
  const x=clamp((xPx-m.left)/m.scale,0,m.spanMm);
  const y=clamp(m.heightMm-(yPx-m.top)/m.scale,0,m.heightMm);
  return {x:clamp(Math.round(x/step)*step,0,m.spanMm),
    y:clamp(Math.round(y/step)*step,0,m.heightMm)};
}
function holeMillimetres(h,spanMm,heightMm){
  return {x:Number(h.u)*spanMm,y:Number(h.v)*heightMm};
}
function apertureMm(h){
 // Explicit hole bore size is separate from nominal DN; false/suspect values stay schematic.
 const v=h?.dimensionSpec?.openingMm;
 return Number.isFinite(v)&&v>=20&&v<=600?v:Number(h?.dn)||70;
}
function hitHole(holes,m,xPx,yPx){
  let closest=null,dist=Infinity;
  for(const h of holes){
    const p=toPixels(m,Number(h.u)*m.spanMm,Number(h.v)*m.heightMm);
    const d=Math.hypot(p.x-xPx,p.y-yPx);
    const radius=Math.max(9,apertureMm(h)/2*m.scale+5);
    if(d<=radius&&d<dist){closest=h;dist=d;}
  }
  return closest;
}
function clearance(holes,spanMm,heightMm){
  let overlaps=0,minEdge=Infinity,minPairGap=Infinity;
  for(let i=0;i<holes.length;i++){
    const a=holeMillimetres(holes[i],spanMm,heightMm),r=apertureMm(holes[i])/2;
    minEdge=Math.min(minEdge,a.x-r,a.y-r,spanMm-a.x-r,heightMm-a.y-r);
    for(let j=0;j<i;j++){
      const b=holeMillimetres(holes[j],spanMm,heightMm);
      const gap=Math.hypot(a.x-b.x,a.y-b.y)-r-apertureMm(holes[j])/2;
      minPairGap=Math.min(gap,minPairGap);
      if(gap<0)overlaps++;
    }
  }
  return {overlaps,minEdge:Number.isFinite(minEdge)?Math.round(minEdge*10)/10:null,
    minPairGap:Number.isFinite(minPairGap)?Math.round(minPairGap*10)/10:null};
}
function copyOffset(h,spanMm,heightMm,dxMm=100,dyMm=0,step=1){
  const start=holeMillimetres(h,spanMm,heightMm);
  step=Math.max(1,Math.round(step));
  return {u:clamp(Math.round(clamp(start.x+dxMm,0,spanMm)/step)*step,0,spanMm)/spanMm,
    v:clamp(Math.round(clamp(start.y+dyMm,0,heightMm)/step)*step,0,heightMm)/heightMm,
    dn:Number(h.dn)||70,row:0,col:0};
}

/* 2D camera: view-only state. Zoom is anchored in screen space, not written to project JSON. */
function viewMetrics(spanMm,heightMm,widthPx,heightPx,camera={}) {
 const base=metrics(spanMm,heightMm,widthPx,heightPx);
 const zoom=clamp(Number(camera.zoom)||1,.5,8);
 const scale=base.scale*zoom;
 return {...base,scale,
  left:base.left+(Number(camera.panX)||0)-(scale-base.scale)*spanMm/2,
  top:base.top+(Number(camera.panY)||0)-(scale-base.scale)*heightMm/2};
}
function zoomAt(spanMm,heightMm,widthPx,heightPx,camera,screenX,screenY,nextZoom){
 const old=viewMetrics(spanMm,heightMm,widthPx,heightPx,camera);
 const model=toMillimetresRaw(old,screenX,screenY);
 const zoom=clamp(Number(nextZoom)||1,.5,8);
 const target=viewMetrics(spanMm,heightMm,widthPx,heightPx,{...camera,zoom});
 const shown=toPixels(target,model.x,model.y);
 return {zoom,panX:(Number(camera.panX)||0)+screenX-shown.x,
  panY:(Number(camera.panY)||0)+screenY-shown.y};
}
function toMillimetresRaw(m,xPx,yPx){
 return {x:(xPx-m.left)/m.scale,
  y:m.heightMm-(yPx-m.top)/m.scale};
}
function panCamera(camera,dx,dy) {
 if(!Number.isFinite(dx)||!Number.isFinite(dy))throw Error('2D-Pan ungültig');
 return {zoom:clamp(Number(camera?.zoom)||1,.5,8),
  panX:(Number(camera?.panX)||0)+dx,panY:(Number(camera?.panY)||0)+dy};
}
function measure(holes,aId,bId,spanMm,heightMm){
 const a=holes.find(h=>h.id===aId),b=holes.find(h=>h.id===bId);
 if(!a||!b||a.id===b.id)throw Error('Zwei verschiedene gültige Leerrohre auswählen');
 const pa=holeMillimetres(a,spanMm,heightMm),pb=holeMillimetres(b,spanMm,heightMm);
 const centerMm=Math.hypot(pa.x-pb.x,pa.y-pb.y);
 return {a:a.id,b:b.id,centerMm,clearMm:centerMm-(apertureMm(a)+apertureMm(b))/2,
  dxMm:pb.x-pa.x,dyMm:pb.y-pa.y};
}
function edgeDistances(h,spanMm,heightMm){
 const p=holeMillimetres(h,spanMm,heightMm),r=apertureMm(h)/2;
 return {leftMm:p.x-r,rightMm:spanMm-p.x-r,
  bottomMm:p.y-r,topMm:heightMm-p.y-r};
}
function rectSelection(holes,m,x1,y1,x2,y2){
 const left=Math.min(x1,x2),right=Math.max(x1,x2),top=Math.min(y1,y2),bottom=Math.max(y1,y2);
 return holes.filter(h=>{
  const p=toPixels(m,Number(h.u)*m.spanMm,Number(h.v)*m.heightMm);
  return p.x>=left&&p.x<=right&&p.y>=top&&p.y<=bottom;
 }).map(h=>h.id);
}
/* Pure planned group movement, never silently dragging a source-PDF reference or
 * committing a partially overlapping/misaligned group. Apply is stale-plan guarded. */
function planGroupMove(project,wallId,ids,dxMm,dyMm) {
 if(project?.sourceInfo)throw Error('Original-/PDF-Referenz geschützt');
 if(!Number.isInteger(dxMm)||!Number.isInteger(dyMm))throw Error('Gruppenverschiebung in ganzen Millimetern');
 const wall=project?.walls?.find(w=>w.id===wallId);
 if(!wall||!Array.isArray(wall.holes))throw Error('Wand nicht gefunden');
 const chosen=new Set(ids);
 if(!chosen.size||chosen.size!==ids.length)throw Error('Leere oder doppelte Rohrauswahl');
 if(chosen.size>600||chosen.size>wall.holes.length)throw Error('Zu viele Rohre');
 const span=wallId==='SW1'||wallId==='SW3'?project.dims.d:project.dims.w;
 const height=project.dims.h;
 if(!Number.isFinite(span)||!Number.isFinite(height)||span<=0||height<=0)throw Error('Schachtmaße fehlen');
 const baseline=JSON.stringify(wall.holes),changes=[];
 for(const id of chosen){
  const h=wall.holes.find(h=>h.id===id);
  if(!h)throw Error('Leerrohr fehlt: '+id);
  const p=holeMillimetres(h,span,height),x=p.x+dxMm,y=p.y+dyMm;
  changes.push({id,u:x/span,v:y/height});
 }
 const byId=new Map(changes.map(c=>[c.id,c]));
 const proposed=wall.holes.map(h=>byId.has(h.id)?{...h,...byId.get(h.id)}:h);
 const distances=clearance(proposed,span,height);
 if(distances.minEdge!==null&&distances.minEdge<-.000001)
  throw Error('Rohrgruppe überschreitet Wandrand');
 if(distances.overlaps)throw Error('Rohrgruppen-Kollision mit Nachbarrohr');
 return {wallId,changes,baseline,dxMm,dyMm};
}
function applyGroupMove(project,plan){
 if(!plan||project?.sourceInfo)throw Error('Original-/PDF-Referenz geschützt');
 const wall=project.walls.find(w=>w.id===plan.wallId);
 if(!wall||JSON.stringify(wall.holes)!==plan.baseline)
  throw Error('Gruppenverschiebung veraltet');
 if(!Array.isArray(plan.changes)||!plan.changes.length)throw Error('Leerer Verschiebungsplan');
 const fresh=planGroupMove(project,plan.wallId,plan.changes.map(c=>c.id),plan.dxMm,plan.dyMm);
 if(JSON.stringify(fresh.changes)!==JSON.stringify(plan.changes))throw Error('Verschiebungsplan manipuliert');
 for(const c of plan.changes){
  const h=wall.holes.find(h=>h.id===c.id);
  h.u=c.u;h.v=c.v;
 }
 return plan.changes.length;
}

return {metrics,toPixels,toMillimetres,holeMillimetres,apertureMm,hitHole,clearance,copyOffset,viewMetrics,zoomAt,panCamera,toMillimetresRaw,measure,edgeDistances,rectSelection,planGroupMove,applyGroupMove};
});
