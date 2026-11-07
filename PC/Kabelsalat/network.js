/* Kabelsalat — local multi-shaft network data model. Coordinates in millimetres. */
(function(root,factory){
 const api=factory();
 if(typeof module==='object'&&module.exports)module.exports=api;
 if(root)root.KabelsalatNetwork=api;
})(typeof window!=='undefined'?window:null,function(){
'use strict';
const clone=x=>JSON.parse(JSON.stringify(x));
const clean=(v,alt,max=90)=>typeof v==='string'&&v.trim()?v.trim().slice(0,max):alt;
const finite=(v,fallback=0,low=-100000000,high=100000000)=>{
 const n=Number(v);return Number.isFinite(n)?Math.max(low,Math.min(high,Math.round(n))):fallback;
};
const snapshot=p=>{
 const f=['name','dims','walls','cables','enclosures','supports','variants','opacity','showBox','showLabels','splitEnabled','sourceInfo'];
 const o={version:3};
 for(const key of f)if(Object.prototype.hasOwnProperty.call(p,key))o[key]=clone(p[key]);
 return o;
};
function createNetwork(active){
 return {version:1,shafts:[{id:'S1',name:clean(active?.name,'Schacht 1'),x:0,z:0,
 snapshot:snapshot(active)}],links:[],routes:[]};
}
function normalizeNetwork(n,active){
 if(!n||!Array.isArray(n.shafts)||!n.shafts.length)return createNetwork(active);
 const seen=new Set();
 const shafts=[];
 for(const s of n.shafts.slice(0,30)){
  if(!s||typeof s!=='object'||!s.snapshot||!Array.isArray(s.snapshot.walls)||
   !Array.isArray(s.snapshot.cables)||!s.snapshot.dims)continue;
  const id=clean(s.id,'',100);
  if(!id||seen.has(id))continue;seen.add(id);
  shafts.push({id,name:clean(s.name,'Schacht',80),x:finite(s.x),z:finite(s.z),
   snapshot:snapshot(s.snapshot)});
 }
 if(!shafts.length)return createNetwork(active);
 const ids=new Set(shafts.map(x=>x.id)),linked=new Set(),links=[];
 for(const link of Array.isArray(n.links)?n.links.slice(0,150):[]){
  if(!link||!ids.has(link.from)||!ids.has(link.to)||link.from===link.to)continue;
  const key=[link.from,link.to].sort().join('\u0000');if(linked.has(key))continue;linked.add(key);
  links.push({id:clean(link.id,'link-'+links.length,100),from:link.from,to:link.to,
   note:clean(link.note,'Verbindung',80)});
 }
 const routes=[],routeIDs=new Set();
 for(const r of Array.isArray(n.routes)?n.routes.slice(0,200):[]){
  if(!r||typeof r!=='object')continue;
  const id=clean(r.id,'',100);if(!id||routeIDs.has(id))continue;routeIDs.add(id);
  const steps=(Array.isArray(r.steps)?r.steps:[]).slice(0,60)
   .filter(v=>v&&typeof v.shaftId==='string'&&typeof v.cableId==='string')
   .map(v=>({shaftId:clean(v.shaftId,'?',100),cableId:clean(v.cableId,'?',120)}));
  routes.push({id,name:clean(r.name,'Netz-Kabelweg',80),
   group:['OLA','MS','Weitere'].includes(r.group)?r.group:'Weitere',
   steps});
 }
 return {version:1,shafts,links,routes};
}
function issues(net,route){
 const problems=[],steps=route?.steps||[];
 if(steps.length<2)problems.push('Mindestens zwei Kabelsegmente in Schächten erforderlich');
 for(let i=0;i<steps.length;i++){
  const s=steps[i],shaft=net.shafts.find(x=>x.id===s.shaftId);
  if(!shaft){problems.push('Unbekannter Schacht '+s.shaftId);continue;}
  if(!shaft.snapshot.cables.some(c=>c.id===s.cableId))
   problems.push('Unbekanntes Kabel '+s.cableId+' in '+shaft.name);
  if(i>0){
   const before=steps[i-1];
   if(before.shaftId===s.shaftId)problems.push('Aufeinanderfolgende Segmente im gleichen Schacht '+shaft.name);
   else if(!net.links.some(l=>(l.from===before.shaftId&&l.to===s.shaftId)||
      (l.from===s.shaftId&&l.to===before.shaftId)))
     problems.push('Keine Netzverbindung zwischen '+before.shaftId+' und '+s.shaftId);
  }
 }
 return problems;
}
function routeDetails(net,route){
 return (route?.steps||[]).map((s,i)=>{
 const shaft=net.shafts.find(x=>x.id===s.shaftId);
 const cable=shaft?.snapshot.cables.find(x=>x.id===s.cableId);
 const describe=ref=>ref?ref.wall+' / '+ref.hole:'Nicht zugeordnet';
 return {order:i+1,shaftId:s.shaftId,shaft:shaft?.name||'Schacht fehlt',cableId:s.cableId,
 cable:cable?.name||'Kabel fehlt',from:cable?describe(cable.a):'–',
 to:cable?describe(cable.b):'–',group:cable?.group||'–'};
 });
}
function shaftCables(shaft,search=''){
 const q=String(search||'').toLocaleLowerCase('de-DE').trim();
 return (shaft?.snapshot?.cables||[]).filter(c=>
  [c.name,c.id,c.group].some(v=>String(v||'').toLocaleLowerCase('de-DE').includes(q)));
}
function csvCell(x){return '"'+String(x??'').replaceAll('"','""').replace(/^[=+\-@]/,m=>"'"+m)+'"';}
function inventoryCSV(net){
 const lines=[['Schacht-ID','Schachtname','X (mm)','Z (mm)','Kabel-ID','Kabelname','Gruppe','Anschluss A','Anschluss B','Projektstatus']];
 for(const s of net.shafts){
  const cables=s.snapshot.cables;
  if(!cables.length)lines.push([s.id,s.name,s.x,s.z,'','','','','','Entwurf']);
  for(const c of cables)lines.push([s.id,s.name,s.x,s.z,c.id,c.name,c.group,
   c.a?c.a.wall+'/'+c.a.hole:'nicht zugeordnet',
   c.b?c.b.wall+'/'+c.b.hole:'nicht zugeordnet','Entwurf']);
 }
 return '\uFEFF'+lines.map(row=>row.map(csvCell).join(';')).join('\r\n')+'\r\n';
}
function parsePositionsCSV(content,net){
 const lines=String(content||'').replace(/^\uFEFF/,'').trim().split(/\r?\n/);
 const warnings=[],rows=[],seen=new Set();
 if(lines.length<2||lines[0].trim().toLowerCase()!=='shaft_id;x_mm;z_mm')
  return {rows:[],warnings:['CSV-Kopf muss exakt shaft_id;x_mm;z_mm lauten']};
 for(let i=1;i<Math.min(lines.length,302);i++){
  if(!lines[i].trim())continue;
  const values=lines[i].split(';').map(x=>x.trim());
  if(values.length!==3){warnings.push('Zeile '+(i+1)+': Genau drei Felder erwartet');continue;}
  const [id,x,z]=values;
  if(!net.shafts.some(s=>s.id===id)){warnings.push('Zeile '+(i+1)+': Unbekannte Schacht-ID '+id);continue;}
  if(seen.has(id)){warnings.push('Zeile '+(i+1)+': Doppelte Schacht-ID '+id);continue;}
  if(!/^-?\d+$/.test(x)||! /-?\d+/.test(z)||! /^-?\d+$/.test(z)||
    Math.abs(Number(x))>100000000||Math.abs(Number(z))>100000000){
   warnings.push('Zeile '+(i+1)+': Ungültige Ganzzahl-Koordinaten');continue;
  }
  seen.add(id);rows.push({id,x:Number(x),z:Number(z)});
 }
 if(lines.length>301)warnings.push('Import auf 300 Datensätze beschränkt');
 return {rows,warnings};
}
function applyPositions(net,rows){
 const result=clone(net);
 for(const item of rows){
  const s=result.shafts.find(x=>x.id===item.id);
  if(s){s.x=item.x;s.z=item.z;}
 }
 return result;
}
return {snapshot,createNetwork,normalizeNetwork,issues,routeDetails,shaftCables,
 inventoryCSV,parsePositionsCSV,applyPositions};
});
