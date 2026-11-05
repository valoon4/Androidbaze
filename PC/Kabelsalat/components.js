/* Kabelsalat v0.5 — reusable model for enclosure plates, supports and variants.
   Units are millimetres. No building-code or electrical approval is implied. */
(function(root,factory){
 const api=factory();
 if(typeof module==='object'&&module.exports)module.exports=api;
 if(root)root.KabelsalatComponents=api;
})(typeof window!=='undefined'?window:null,function(){
'use strict';
const clamp=(v,a,b)=>Math.min(b,Math.max(a,v));
const FACES=[['floor','Boden'],['roof','Deckel'],['left','Seite links'],['right','Seite rechts'],['front','Stirn vorn'],['back','Stirn hinten']];
const num=(v,fallback,min,max)=>Number.isFinite(Number(v))&&String(v).trim()!==''?clamp(Number(v),min,max):fallback;
const clean=(v,fallback,max=90)=>typeof v==='string'?v.slice(0,max):fallback;
const clone=x=>JSON.parse(JSON.stringify(x));
function defaultBounds(dims,group){
 const ms=group==='MS';
 return {x:0,y:Math.round(dims.h*(ms?.65:.22)),z:Math.round(dims.d*.11),
 w:Math.round(dims.w*.72),h:Math.round(dims.h*.26),d:Math.round(dims.d*.68)};
}
function defaultPlate(group,face,b){
 const horizontal=face==='floor'||face==='roof',xSide=face==='left'||face==='right';
 return {id:group+'-'+face,face,visible:true,opacity:group==='MS'?48:70,
 material:group==='MS'?'Stahl':'Beton',thickness:24,
 width:horizontal||!xSide?b.w:b.d,height:horizontal?b.d:b.h,offset:0};
}
function defaults(dims){
 const result={};
 for(const group of ['OLA','MS']){
   const bounds=defaultBounds(dims,group);
   result[group]={visible:true,opacity:group==='MS'?48:70,bounds,
     panels:FACES.map(([face])=>defaultPlate(group,face,bounds))};
 }
 return result;
}
function normalizeEnclosures(input,dims){
 const out=defaults(dims);
 for(const group of ['OLA','MS']){
  const source=input?.[group]||{},target=out[group];
  target.visible=source.visible!==false;
  target.opacity=num(source.opacity,target.opacity,5,100);
  const original=source.bounds||{},b=target.bounds;
  const bounds={x:num(original.x,b.x,-dims.w/2,dims.w/2),
   y:num(original.y,b.y,0,dims.h),z:num(original.z,b.z,-dims.d/2,dims.d/2),
   w:num(original.w,b.w,100,dims.w),h:num(original.h,b.h,100,dims.h),
   d:num(original.d,b.d,100,dims.d)};
  target.bounds=bounds;
  target.panels=FACES.map(([face])=>{
   const d=defaultPlate(group,face,bounds);
   const p=Array.isArray(source.panels)?source.panels.find(x=>x&&x.face===face)||{}:{};
   return {id:d.id,face,visible:p.visible!==false,opacity:num(p.opacity,source.opacity===undefined?d.opacity:target.opacity,5,100),
    material:clean(p.material,d.material,50),thickness:num(p.thickness,d.thickness,1,300),
    width:num(p.width,d.width,30,Math.max(dims.w,dims.d)*2),
    height:num(p.height,d.height,30,Math.max(dims.h,dims.d)*2),
    offset:num(p.offset,0,-3000,3000)};
  });
 }
 return out;
}
function panelGeometry(e,p){
 const b=e.bounds,t=p.thickness,face=p.face,front=face==='front'||face==='back',
 side=face==='left'||face==='right',horizontal=face==='floor'||face==='roof';
 if(horizontal)return {x:b.x,y:b.y+(face==='floor'?-b.h/2:b.h/2)+p.offset,z:b.z,
 w:p.width,h:t,d:p.height};
 if(side)return {x:b.x+(face==='left'?-b.w/2:b.w/2)+p.offset,y:b.y,z:b.z,
 w:t,h:p.height,d:p.width};
 if(front)return {x:b.x,y:b.y,z:b.z+(face==='front'?-b.d/2:b.d/2)+p.offset,
 w:p.width,h:p.height,d:t};
 throw Error('Unknown plate face');
}
function normalizeSupports(list,dims){
 if(!Array.isArray(list))return [];
 const used=new Set();
 return list.slice(0,100).filter(x=>x&&typeof x==='object').map((v,i)=>{
  const id=clean(v.id,'support-'+i,120);if(used.has(id))return null;used.add(id);
  return {id,name:clean(v.name,'Konsole '+(i+1),80),
   type:v.type==='Auflager'?'Auflager':'Konsole',
   wall:['SW1','SW2','SW3','SW4'].includes(v.wall)?v.wall:'SW1',
   x:num(v.x,0,-dims.w/2,dims.w/2),y:num(v.y,dims.h*.35,0,dims.h),
   z:num(v.z,0,-dims.d/2,dims.d/2),
   w:num(v.w,280,20,2000),h:num(v.h,90,20,1000),d:num(v.d,180,20,2000),
   visible:v.visible!==false};
 }).filter(Boolean);
}
function newSupport(dims,id){
 return normalizeSupports([{id,name:'Neue Konsole',type:'Konsole',wall:'SW1',
 x:-dims.w/2+140,y:Math.round(dims.h*.35),z:0,w:280,h:90,d:180}],dims)[0];
}
function snapshot(project){
 const fields=['name','dims','walls','cables','enclosures','supports','opacity','showBox','showLabels','splitEnabled'];
 const result={version:3};
 for(const f of fields)if(Object.prototype.hasOwnProperty.call(project,f))result[f]=clone(project[f]);
 return result;
}
function normalizeVariants(list){
 if(!Array.isArray(list))return [];
 const ids=new Set();const out=[];
 for(const item of list.slice(0,12)){
  if(!item||typeof item!=='object'||!item.snapshot||!Array.isArray(item.snapshot.walls)||
    !Array.isArray(item.snapshot.cables)||!item.snapshot.dims)continue;
  const id=clean(item.id,'',120);if(!id||ids.has(id))continue;ids.add(id);
  const s=item.snapshot;
  const safe={version:3};
  for(const field of ['name','dims','walls','cables','enclosures','supports','opacity','showBox','showLabels','splitEnabled'])
   if(Object.prototype.hasOwnProperty.call(s,field))safe[field]=clone(s[field]);
  out.push({id,name:clean(item.name,'Variante',80),createdAt:clean(item.createdAt,'',40),snapshot:safe});
 }
 return out;
}
function differences(a,b){
 const holes=p=>p.walls.reduce((n,w)=>n+w.holes.length,0);
 const count=p=>({wände:p.walls.length,leerrohre:holes(p),kabel:p.cables.length,
  auflager:(p.supports||[]).length,breite:p.dims.w,tiefe:p.dims.d,höhe:p.dims.h});
 const aa=count(a),bb=count(b);
 return Object.keys(aa).filter(k=>aa[k]!==bb[k]).map(k=>k+': '+aa[k]+' → '+bb[k]);
}
function potentialBlockages(project,group){
 const e=project.enclosures?.[group];if(!e||!e.visible||project.showBox===false)return [];
 const hits=[],dim=project.dims;
 for(const p of e.panels||[]){
  if(!p.visible)continue;
  const b=panelGeometry(e,p);
  for(const w of project.walls||[])for(const h of w.holes||[]){
   const usedByGroup=(project.cables||[]).some(c=>c.group===group&&[c.a,c.b].some(r=>r?.wall===w.id&&r?.hole===h.id));
   if(usedByGroup)continue;
   const u=Number(h.u),v=Number(h.v),r=Number(h.dn||70)/2;
   const point=w.id==='SW1'?{x:-dim.w/2,y:dim.h*v,z:-dim.d/2+dim.d*u}:
    w.id==='SW3'?{x:dim.w/2,y:dim.h*v,z:-dim.d/2+dim.d*u}:
    w.id==='SW2'?{x:-dim.w/2+dim.w*u,y:dim.h*v,z:-dim.d/2}:
    {x:-dim.w/2+dim.w*u,y:dim.h*v,z:dim.d/2};
   if(Math.abs(point.x-b.x)<b.w/2+r&&Math.abs(point.y-b.y)<b.h/2+r&&
      Math.abs(point.z-b.z)<b.d/2+r)hits.push({panel:p.id,wall:w.id,hole:h.id});
  }
 }
 return hits;
}
return {FACES,defaults,normalizeEnclosures,panelGeometry,normalizeSupports,newSupport,
 snapshot,normalizeVariants,differences,potentialBlockages};
});
