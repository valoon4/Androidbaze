'use strict';
/* KAB-0251–0270: deterministic cable/hole editing, no engineering acceptance.
 * All changes are explicit; callers wrap successful mutations in Undo checkpoints.
 */
(function(root,factory){
 const api=factory();
 if(typeof module==='object'&&module.exports)module.exports=api;
 if(root)root.KabelsalatCableEditorOps=api;
})(typeof globalThis!=='undefined'?globalThis:this,function(){
 const clone=x=>JSON.parse(JSON.stringify(x));
 const text=(v,max=120)=>typeof v==='string'?v.trim().slice(0,max):'';
 const same=(ref,wallId,holeId)=>ref?.wall===wallId&&ref?.hole===holeId;
 function selectedHole(project,wallId,holeId){
  const wall=project?.walls?.find(w=>w.id===wallId);
  const hole=wall?.holes?.find(h=>h.id===holeId);
  if(!hole)throw Error('Leerrohr nicht gefunden: '+wallId+'/'+holeId);
  return {wall,hole};
 }
 function holeDependencies(project,wallId,holeId){
  selectedHole(project,wallId,holeId);
  return project.cables.flatMap(c=>['a','b'].filter(end=>same(c[end],wallId,holeId))
   .map(end=>({cableId:c.id,name:c.name,end:end.toUpperCase()})));
 }
 function planHoleDeletion(project,wallId,holeId){
  if(project.sourceInfo)throw Error('Importreferenz geschützt: Originalrohr nicht löschen');
  return {wallId,holeId,affected:holeDependencies(project,wallId,holeId),base:JSON.stringify(project)};
 }
 function applyHoleDeletion(project,plan,confirmed=false){
  if(!plan||project.sourceInfo||plan.base!==JSON.stringify(project))
   throw Error('Löschvorschau veraltet oder Importreferenz geschützt');
  selectedHole(project,plan.wallId,plan.holeId);
  if(plan.affected.length&&!confirmed)throw Error('Kabelabhängigkeiten vorher bestätigen');
  const affected=holeDependencies(project,plan.wallId,plan.holeId);
  if(JSON.stringify(affected)!==JSON.stringify(plan.affected))throw Error('Kabelabhängigkeiten geändert');
  for(const c of project.cables)for(const end of ['a','b']){
   if(same(c[end],plan.wallId,plan.holeId)){
    c['unresolved'+end.toUpperCase()]={wall:plan.wallId,hole:plan.holeId,reason:'Leerrohr entfernt'};
    c[end]=null;c.reviewStatus='Zur Prüfung';
   }
  }
  const wall=project.walls.find(w=>w.id===plan.wallId);
  wall.holes=wall.holes.filter(h=>h.id!==plan.holeId);
  return affected.length;
 }
 function pipeRows(project,sort='wall'){
  if(!['wall','trasse','dn'].includes(sort))throw Error('Sortierschlüssel ungültig');
  const rows=project.walls.flatMap(w=>w.holes.map(h=>({
   wall:w.id,hole:h.id,dn:Number(h.dn)||0,
   trasse:text(h.trasse||h.sourceAddress||h.sourceCategory||'',140),
   label:text(h.sourceAddress||h.id,140)
  })));
  const cmp=(a,b)=>String(a).localeCompare(String(b),'de',{numeric:true});
  rows.sort((a,b)=>sort==='dn'?(a.dn-b.dn||cmp(a.wall,b.wall)||cmp(a.hole,b.hole)):
   sort==='trasse'?(cmp(a.trasse,b.trasse)||cmp(a.wall,b.wall)||cmp(a.hole,b.hole)):
   (cmp(a.wall,b.wall)||cmp(a.hole,b.hole)));
  return rows;
 }
 function cable(project,id){
  const item=project?.cables?.find(c=>c.id===id);
  if(!item)throw Error('Kabel-ID nicht gefunden');
  return item;
 }
 function uniqueCableId(project){
  const taken=new Set(project.cables.map(c=>c.id));
  let i=1;while(i<1000000){
   const id='cable-'+String(i).padStart(4,'0');
   if(!taken.has(id))return id;i++;
  }
  throw Error('Keine freie Kabel-ID');
 }
 function createCable(project,name='Neues Kabel'){
  if(project.cables.length>=400)throw Error('Maximal 400 Kabel im Schacht');
  const id=uniqueCableId(project);
  const c={id,name:text(name,80)||'Neues Kabel',color:'#398bdb',
   group:'Weitere',technicalCategory:'Nicht klassifiziert',typeId:'',
   diameter:12,route:[],a:null,b:null,reviewStatus:'Entwurf'};
  project.cables.push(c);return id;
 }
 function validateEndpoint(project,value){
  if(value===null)return null;
  if(!value||typeof value.wall!=='string'||typeof value.hole!=='string')
   throw Error('Kabelanschluss ungültig');
  selectedHole(project,value.wall,value.hole);
  return {wall:value.wall,hole:value.hole};
 }
 function setEndpoint(project,id,end,value){
  if(!['a','b'].includes(end))throw Error('Anschluss muss A oder B sein');
  const ref=validateEndpoint(project,value);
  const c=cable(project,id);
  c[end]=ref;
  if(ref)delete c['unresolved'+end.toUpperCase()];
  c.reviewStatus='Zur Prüfung';
  return ref;
 }
 function updateCable(project,id,values){
  const c=cable(project,id);
  const next={};
  if(Object.hasOwn(values,'name')){next.name=text(values.name,80);if(!next.name)throw Error('Kabelname fehlt');}
  if(Object.hasOwn(values,'diameter')){
   if(typeof values.diameter!=='number'||!Number.isFinite(values.diameter)||values.diameter<2||values.diameter>80)
    throw Error('Kabel-Ø muss zwischen 2 und 80 mm liegen');
   next.diameter=values.diameter;
  }
  if(Object.hasOwn(values,'group')){
   if(!['OLA','MS','Weitere'].includes(values.group))throw Error('Unbekannte Kabelgruppe');
   next.group=values.group;
  }
  if(Object.hasOwn(values,'technicalCategory')){
   next.technicalCategory=text(values.technicalCategory,80)||'Nicht klassifiziert';
  }
  if(Object.hasOwn(values,'typeId')){
   const id=text(values.typeId,100);
   if(id&&!project.cableTypes?.some(type=>type.id===id))throw Error('Kabeltyp nicht im Katalog');
   next.typeId=id;
  }
  Object.assign(c,next);return c;
 }
 function addType(project,{name,category,diameter}){
  const title=text(name,80),group=text(category,80)||'Nicht klassifiziert',d=Number(diameter);
  if(!title)throw Error('Typbezeichnung fehlt');
  if(!Number.isFinite(d)||d<2||d>80)throw Error('Typ-Ø ungültig');
  if(!Array.isArray(project.cableTypes))project.cableTypes=[];
  if(project.cableTypes.length>=100)throw Error('Kabeltypkatalog voll');
  const id='type-'+(project.cableTypes.reduce((n,t)=>Math.max(n,Number(String(t.id).replace(/^type-/,'')||0)||0),0)+1);
  project.cableTypes.push({id,name:title,category:group,diameter:d});
  return id;
 }
 function swapEndpoints(project,id){
  const c=cable(project,id),old=c.a;c.a=c.b;c.b=old;
  const pending=c.unresolvedA;c.unresolvedA=c.unresolvedB;c.unresolvedB=pending;
  c.reviewStatus='Zur Prüfung';return c;
 }
 function duplicateCable(project,id){
  const src=cable(project,id);
  if(project.cables.length>=400)throw Error('Maximal 400 Kabel im Schacht');
  const copy=clone(src);
  copy.id=uniqueCableId(project);copy.name=(src.name.slice(0,72)+' · Kopie').slice(0,80);
  copy.reviewStatus='Zur Prüfung';copy.sourceCopyOf=src.id;
  project.cables.push(copy);return copy.id;
 }
 function affectedRoutes(project,id){
  cable(project,id);
  const shaftId=project.activeShaftId;
  return (project.network?.routes||[]).filter(route=>
   route.steps?.some(step=>step.shaftId===shaftId&&step.cableId===id))
   .map(route=>({id:route.id,name:route.name}));
 }
 function planCableDeletion(project,id){
  return {id,base:JSON.stringify(project),affectedRoutes:affectedRoutes(project,id)};
 }
 function applyCableDeletion(project,plan,confirmed=false){
  if(!plan||plan.base!==JSON.stringify(project))throw Error('Kabel-Löschvorschau veraltet');
  cable(project,plan.id);
  if(!confirmed)throw Error('Kabelentfernung vorher bestätigen');
  project.cables=project.cables.filter(c=>c.id!==plan.id);
  // Deliberately retain network segments as visibly unresolved instead of silently rerouting.
  return plan.affectedRoutes;
 }
 return {selectedHole,holeDependencies,planHoleDeletion,applyHoleDeletion,pipeRows,
  cable,uniqueCableId,createCable,validateEndpoint,setEndpoint,updateCable,addType,
  swapEndpoints,duplicateCable,affectedRoutes,planCableDeletion,applyCableDeletion};
});
