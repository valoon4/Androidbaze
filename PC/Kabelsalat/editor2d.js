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
return {metrics,toPixels,toMillimetres,holeMillimetres,apertureMm,hitHole,clearance,copyOffset};
});
