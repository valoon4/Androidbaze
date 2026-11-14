'use strict';
/* KAB-0352/0353/0355/0356/0365 + KAB-0375 network topology inspection.
 * Strictly a read-only exploratory graph planner: no real cable installation,
 * surveyed duct connection, travel distance or physical feasibility is inferred.
 * Shaft X/Z model coordinates are schematic mm. The only traversable edges are
 * links explicitly present in the saved project. No invisible new links.
 */
(function(root,factory){
 const api=factory();
 if(typeof module==='object'&&module.exports)module.exports=api;
 if(root)root.KabelsalatRoutePlanner=api;
})(typeof globalThis!=='undefined'?globalThis:this,function(){
 const MAX_SHAFTS=30,MAX_LINKS=150,ROUND=v=>Math.round(v*10)/10;
 const own=(obj,key)=>Object.prototype.hasOwnProperty.call(obj,key);
 const safeId=v=>typeof v==='string'&&v.length<=120&&v.trim().length>0;
 function normalize(net){
  if(!net||!Array.isArray(net.shafts)||!Array.isArray(net.links)||
   net.shafts.length>MAX_SHAFTS||net.links.length>MAX_LINKS)
   throw Error('Netz fehlt oder überschreitet 30 Schächte / 150 Links');
  const map=new Map(),warnings=[];
  for(const s of net.shafts){
   if(!s||!safeId(s.id)||map.has(s.id)||!Number.isSafeInteger(s.x)||!Number.isSafeInteger(s.z)||
       Math.abs(s.x)>1e8||Math.abs(s.z)>1e8)
    throw Error('Ungültige/doppelte Schacht-ID oder unsichere Ganzzahl-Position');
   map.set(s.id,{id:s.id,name:typeof s.name==='string'?s.name.slice(0,80):s.id,x:s.x,z:s.z});
  }
  const links=[],pairSet=new Set(),linkIDs=new Set(),adj=new Map();
  for(const id of map.keys())adj.set(id,[]);
  for(const l of net.links){
   if(!l||!safeId(l.id)||linkIDs.has(l.id)||!map.has(l.from)||!map.has(l.to)||l.from===l.to)
    throw Error('Ungültiger, doppelter oder unbekannter Netzlink');
   linkIDs.add(l.id);
   const pair=[l.from,l.to].sort().join('\u0000');
   if(pairSet.has(pair))throw Error('Doppelte Netzverbindung');
   pairSet.add(pair);
   const a=map.get(l.from),b=map.get(l.to),length=Math.hypot(a.x-b.x,a.z-b.z);
   const link={id:l.id,from:l.from,to:l.to,distanceMm:ROUND(length)};
   links.push(link);
   if(length===0)warnings.push('Schacht-Positionen '+l.from+' / '+l.to+' sind identisch; 0-mm-Scheinlänge');
   adj.get(l.from).push({to:l.to,id:l.id,weight:length});
   adj.get(l.to).push({to:l.from,id:l.id,weight:length});
  }
  for(const edges of adj.values())edges.sort((a,b)=>a.to.localeCompare(b.to)||a.id.localeCompare(b.id));
  return {map,links,adj,warnings};
 }
 function topology(net){
  const g=normalize(net),seen=new Set(),components=[];
  for(const id of [...g.map.keys()].sort()){
   if(seen.has(id))continue;
   const group=[],queue=[id];seen.add(id);
   while(queue.length){
    const node=queue.shift();group.push(node);
    for(const edge of g.adj.get(node))if(!seen.has(edge.to)){
     seen.add(edge.to);queue.push(edge.to);
    }
   }
   group.sort();components.push(group);
  }
  const isolated=components.filter(x=>x.length===1).map(x=>x[0]);
  return {shaftCount:g.map.size,linkCount:g.links.length,components,
   componentCount:components.length,isolated,hasCycles:g.links.length>g.map.size-components.length,
   warnings:[...g.warnings],degrees:[...g.adj].map(([id,edges])=>({shaftId:id,degree:edges.length}))};
 }
 function find(net,from,to,{mode='distance',excludeLinks=[]}={}){
  if(!['distance','hops'].includes(mode))throw Error('Unbekannter Suchmodus');
  const g=normalize(net);
  if(!g.map.has(from)||!g.map.has(to))throw Error('Start/Ziel-Schacht nicht im Netzwerk');
  if(!Array.isArray(excludeLinks)||excludeLinks.some(id=>!safeId(id)))throw Error('Ungültige Link-Sperrliste');
  const excluded=new Set(excludeLinks),best=new Map(),pending=new Set(g.map.keys());
  best.set(from,{cost:0,ids:[from],links:[],exactMm:0});
  const EPS=1e-7;
  while(pending.size){
   let current=null;
   for(const id of pending){
    const value=best.get(id);
    if(!value)continue;
    const ref=current===null?null:best.get(current);
    if(ref===null||value.cost<ref.cost-EPS||
     (Math.abs(value.cost-ref.cost)<=EPS&&value.ids.join('\u0000')<ref.ids.join('\u0000')))current=id;
   }
   if(current===null)break;
   if(current===to)break;
   pending.delete(current);
   const state=best.get(current);
   for(const e of g.adj.get(current)){
    if(!pending.has(e.to)||excluded.has(e.id))continue;
    const next={
     cost:state.cost+(mode==='hops'?1:e.weight),
     ids:[...state.ids,e.to],links:[...state.links,e.id],
     exactMm:state.exactMm+e.weight
    };
    const prior=best.get(e.to),nextLex=next.ids.join('\u0000');
    if(!prior||next.cost<prior.cost-EPS||
     (Math.abs(next.cost-prior.cost)<=EPS&&nextLex<prior.ids.join('\u0000')))best.set(e.to,next);
   }
  }
  const found=best.get(to);
  // Found paths to nodes in pending are speculative until settled; 'to' may be pending
  // after break only when directly selected as global minimum. If no route exists, absent.
  if(!found)return {status:'unreachable',from,to,mode,shaftIds:[],linkIds:[],
   distanceMm:null,hops:null,warnings:[...g.warnings,'Keine Verbindung über gespeicherte Netzlinks'],
   source:'schematic-net-positions',verified:false};
  return {status:'reachable',from,to,mode,shaftIds:found.ids,linkIds:found.links,
   distanceMm:ROUND(found.exactMm),hops:found.links.length,
   warnings:[...g.warnings,'Luftlinien der Modell-X/Z-Koordinaten; keine Trassen-/Kabellängen oder bauliche Machbarkeit'],
   source:'schematic-net-positions',verified:false};
 }
 function alternatives(net,from,to,{mode='distance',max=3}={}){
  if(!Number.isInteger(max)||max<0||max>10)throw Error('Ungültige Alternativenzahl');
  const first=find(net,from,to,{mode});
  if(first.status!=='reachable'||first.linkIds.length===0||max===0)return {primary:first,alternatives:[]};
  const choices=[],seen=new Set([first.linkIds.join('\u0000')]);
  for(const id of first.linkIds){
   const alt=find(net,from,to,{mode,excludeLinks:[id]});
   if(alt.status!=='reachable')continue;
   const key=alt.linkIds.join('\u0000');
   if(seen.has(key))continue;seen.add(key);choices.push(alt);
  }
  choices.sort((a,b)=>mode==='hops'?a.hops-b.hops||a.distanceMm-b.distanceMm||
   a.shaftIds.join('\u0000').localeCompare(b.shaftIds.join('\u0000')):
   a.distanceMm-b.distanceMm||a.hops-b.hops||
   a.shaftIds.join('\u0000').localeCompare(b.shaftIds.join('\u0000')));
  return {primary:first,alternatives:choices.slice(0,max)};
 }
 function compareRoute(net,route){
  const g=normalize(net),details=[],warnings=[],steps=route?.steps;
  if(!Array.isArray(steps))throw Error('Kabelweg ohne Segmente');
  if(steps.length>60)throw Error('Zu viele Kabelsegmente');
  let distance=0;
  for(let i=0;i<steps.length;i++){
   const step=steps[i],shaft=g.map.get(step?.shaftId);
   const saved=net.shafts.find(x=>x.id===step?.shaftId);
   const cable=saved?.snapshot?.cables?.find(x=>x.id===step?.cableId);
   if(!shaft)warnings.push('Schacht unbekannt: '+String(step?.shaftId).slice(0,60));
   if(shaft&&!cable)warnings.push('Kabel in Schacht nicht gefunden: '+String(step?.cableId).slice(0,60));
   if(i===0)continue;
   const prev=steps[i-1],a=g.map.get(prev?.shaftId),b=shaft;
   const link=g.links.find(x=>(x.from===prev?.shaftId&&x.to===step?.shaftId)||
    (x.to===prev?.shaftId&&x.from===step?.shaftId));
   if(a&&b&&!link)warnings.push('Kein direkter Netzlink '+a.id+' → '+b.id);
   if(link){details.push({from:a.id,to:b.id,linkId:link.id,distanceMm:link.distanceMm});
    distance+=Math.hypot(a.x-b.x,a.z-b.z);}
  }
  return {routeId:route.id,steps:steps.length,linkSteps:details,modelDistanceMm:ROUND(distance),
   status:warnings.length?'incomplete':'schematic',warnings,
   physicalClearanceVerified:false,source:'model-links-only'};
 }
 function csv(result){
  if(!result||!Array.isArray(result.shaftIds)||!Array.isArray(result.linkIds)||
   result.shaftIds.length>31||result.linkIds.length>30)throw Error('Ungültige Exportdaten');
  const cell=v=>'"'+String(v??'').replace(/^[=+\-@]/,m=>"'"+m).replaceAll('"','""')+'"';
  const rows=[['Index','Schacht-ID','Nächster Link','Status','Hinweis']];
  for(let i=0;i<result.shaftIds.length;i++)
   rows.push([i+1,result.shaftIds[i],result.linkIds[i]||'',result.status,
    'Schematische Netzlinie, keine reale Kabeltrasse']);
  return '\uFEFF'+rows.map(row=>row.map(cell).join(';')).join('\r\n')+'\r\n';
 }
 return {normalize,topology,find,alternatives,compareRoute,csv};
});
