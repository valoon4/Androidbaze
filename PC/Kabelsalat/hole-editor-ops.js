'use strict';
/* KAB-0235–0250: non-normative, source-safe bore editing. All coordinates mm.
 * Pure preview helpers. Actual changes must be executed by app change()/undo.
 */
(function(root,factory){
 const api=factory();
 if(typeof module==='object'&&module.exports)module.exports=api;
 if(root)root.KabelsalatHoleEditorOps=api;
})(typeof globalThis!=='undefined'?globalThis:this,function(){
 const COMMON_DN=[30,40,50,60,63,75,90,110,116,125,140,160];
 const WALL_IDS=['SW1','SW2','SW3','SW4'];
 const isNumber=n=>typeof n==='number'&&Number.isFinite(n);
 const integer=(n,min,max)=>Number.isSafeInteger(n)&&n>=min&&n<=max;
 const clamp=(n,a,b)=>Math.max(a,Math.min(b,n));
 function wall(project,id){
  if(!WALL_IDS.includes(id))throw Error('Wand-ID ungültig');
  const w=project?.walls?.find(w=>w.id===id);
  if(!w||!Array.isArray(w.holes))throw Error('Wanddaten fehlen');
  return w;
 }
 function span(project,id){
  wall(project,id);
  const pdf=project.sourceInfo?.sourceVersion?.startsWith('PDF V2')===true;
  const on13=id==='SW1'||id==='SW3';
  const n=(on13?pdf:!pdf)?project.dims.w:project.dims.d;
  if(!isNumber(n)||n<=0||!isNumber(project.dims.h)||project.dims.h<=0)
   throw Error('Schachtabmessungen ungültig');
  return n;
 }
 function radius(h){
  const raw=h?.dimensionSpec?.openingMm;
  const dia=isNumber(raw)&&raw>=20&&raw<=600?raw:Number(h?.dn);
  if(!Number.isFinite(dia)||dia<20||dia>600)throw Error('Rohrmaß/DN ungültig');
  return dia/2;
 }
 function uniqueId(project,wallId,prefix='free'){
  wall(project,wallId);
  if(!/^[a-z0-9-]{1,16}$/i.test(prefix))throw Error('ID-Präfix ungültig');
  const ids=new Set(project.walls.flatMap(w=>w.holes.map(h=>h.id)));
  let i=1;
  while(i<=100000){const name=wallId+'-'+prefix+'-'+String(i).padStart(4,'0');
   if(!ids.has(name))return name;
   i++;
  }
  throw Error('Kein freier Rohr-Identifier');
 }
 function snapMm(value,step=1){
  if(!isNumber(value)||!integer(step,1,100))throw Error('Fangraster ungültig');
  return Math.round(value/step)*step;
 }
 function candidate(project,wallId,{xMm,yMm,dn,openingMm=null},ignoreId=null,additional=[]){
  const w=wall(project,wallId),s=span(project,wallId),h=project.dims.h;
  if(!isNumber(xMm)||!isNumber(yMm)||!integer(dn,20,300))
   throw Error('Ungültiger numerischer Mittelpunkt oder DN');
  const diam=isNumber(openingMm)&&openingMm>=20&&openingMm<=600?openingMm:dn;
  const r=diam/2;
  const errors=[],warnings=[];
  if(xMm-r<0||xMm+r>s||yMm-r<0||yMm+r>h)
   errors.push('Rohröffnung liegt außerhalb der Wand');
  for(const other of [...w.holes,...additional]){
   if(other.id===ignoreId)continue;
   const ox=other.xMm??other.u*s,oy=other.yMm??other.v*h;
   const or=other.openingMm!==undefined?other.openingMm/2:radius(other);
   const gap=Math.hypot(xMm-ox,yMm-oy)-r-or;
   if(gap<-.01)warnings.push('Überlappung mit '+other.id+' ('+gap.toFixed(1)+' mm)');
  }
  return {ok:errors.length===0,errors,warnings,wall:wallId,
   xMm,yMm,dn,openingMm:diam,u:xMm/s,v:yMm/h};
 }
 function copySpec(source){
  if(!source?.dimensionSpec)return undefined;
  const spec=source.dimensionSpec;
  // Never inherit an old person's verification/provenance for a new pipe.
  return {...spec,source:'',reviewer:'',reviewedAt:'',status:'unverified',
   method:'manual',candidateMm:null,note:'Kopie; Maße/Quelle separat prüfen'};
 }
 function planGroup(project,wallId,{xMm,yMm,rows=1,cols=1,dxMm=150,dyMm=150,dns=[50]}={}){
  if(project?.sourceInfo)throw Error('Referenzquelle geschützt: Gruppenerstellung nur im eigenen/fiktiven Schacht');
  const w=wall(project,wallId);
  if(!integer(rows,1,15)||!integer(cols,1,15)||rows*cols>100||
     !integer(dxMm,1,5000)||!integer(dyMm,1,5000)||!Array.isArray(dns)||
     dns.length<1||dns.length>100||dns.some(dn=>!integer(dn,30,160))||
     !isNumber(xMm)||!isNumber(yMm))throw Error('Gruppenmaße oder DN ungültig');
  if(w.holes.length+rows*cols>600)throw Error('Maximal 600 Leerrohre je Wand');
  const planned=[],warnings=[];
  let index=0;
  for(let row=0;row<rows;row++)for(let col=0;col<cols;col++){
   const dn=dns[index%dns.length],x=xMm+col*dxMm,y=yMm+row*dyMm;
   const inspect=candidate(project,wallId,{xMm:x,yMm:y,dn},null,planned);
   if(!inspect.ok)throw Error('Gruppenöffnung '+(index+1)+': '+inspect.errors.join('; '));
   const id=uniqueGroupId(project,wallId,index,planned);
   planned.push({id,row:0,col:0,u:inspect.u,v:inspect.v,dn,xMm:x,yMm:y,openingMm:dn});
   warnings.push(...inspect.warnings);
   index++;
  }
  return {wallId,count:planned.length,base:JSON.stringify(project),warnings,
   holes:planned.map(({xMm,yMm,openingMm,...h})=>h)};
 }
 function uniqueGroupId(project,wallId,index,planned){
  let name=uniqueId(project,wallId,'group');
  if(planned.some(h=>h.id===name)){
   let num=Number(name.split('-').at(-1))+1;
   while(planned.some(h=>h.id===wallId+'-group-'+String(num).padStart(4,'0'))||
     project.walls.some(w=>w.holes.some(h=>h.id===wallId+'-group-'+String(num).padStart(4,'0'))))num++;
   name=wallId+'-group-'+String(num).padStart(4,'0');
  }
  return name;
 }
 function applyGroup(project,plan){
  if(!plan||!project||project.sourceInfo||JSON.stringify(project)!==plan.base)
   throw Error('Gruppenvorschau veraltet oder Referenzquelle geschützt');
  const w=wall(project,plan.wallId);
  if(!Array.isArray(plan.holes)||w.holes.length+plan.holes.length>600)
   throw Error('Gruppenanzahl ungültig');
  const ids=new Set(project.walls.flatMap(w=>w.holes.map(h=>h.id)));
  for(const h of plan.holes){if(ids.has(h.id))throw Error('Doppelte Rohr-ID');
   ids.add(h.id);
   const c=candidate(project,plan.wallId,{xMm:h.u*span(project,plan.wallId),
    yMm:h.v*project.dims.h,dn:h.dn});
   if(!c.ok)throw Error('Ungültige Gruppenöffnung');
  }
  w.holes.push(...plan.holes.map(h=>({...h})));
  return plan.holes.length;
 }
 function planTransfer(project,from,id,to){
  if(project?.sourceInfo)throw Error('Referenzquelle geschützt: Wandwechsel nur in eigenen/fiktiven Schächten');
  const source=wall(project,from),dest=wall(project,to);
  if(from===to)throw Error('Zielwand und Quellwand sind gleich');
  const hole=source.holes.find(h=>h.id===id);
  if(!hole)throw Error('Rohr-ID nicht gefunden');
  if(dest.holes.length>=600)throw Error('Zielwand hat 600 Leerrohre');
  if(dest.holes.some(h=>h.id===id))throw Error('Rohr-ID existiert bereits auf Zielwand');
  // Preserve physical distance along wall where possible; never clamp into a wall.
  const x=hole.u*span(project,from),y=hole.v*project.dims.h;
  const check=candidate(project,to,{xMm:x,yMm:y,dn:hole.dn,
   openingMm:isNumber(hole.dimensionSpec?.openingMm)?hole.dimensionSpec.openingMm:null});
  if(!check.ok)throw Error(check.errors.join('; '));
  const connected=project.cables.filter(c=>
   c.a?.wall===from&&c.a?.hole===id||c.b?.wall===from&&c.b?.hole===id).map(c=>c.id);
  return {from,to,id,base:JSON.stringify(project),xMm:x,yMm:y,
   connected,warnings:[...check.warnings,...(connected.length?[
    'Kabelanschlüsse '+connected.join(', ')+' müssen für andere Wand geprüft werden']:[])]};
 }
 function applyTransfer(project,plan,{approveConnected=false,approveOverlaps=false}={}){
  if(!plan||!project||JSON.stringify(project)!==plan.base||project.sourceInfo)
   throw Error('Wandwechsel-Vorschau veraltet oder Referenzquelle geschützt');
  const old=wall(project,plan.from),dest=wall(project,plan.to);
  const source=old.holes.find(h=>h.id===plan.id);
  if(!source||dest.holes.length>=600)throw Error('Rohr nicht gefunden oder Zielwand voll');
  if(plan.connected.length&&!approveConnected)throw Error('Kabelanschlusskonflikte müssen bestätigt werden');
  const check=candidate(project,plan.to,{xMm:plan.xMm,yMm:plan.yMm,dn:source.dn,
   openingMm:isNumber(source.dimensionSpec?.openingMm)?source.dimensionSpec.openingMm:null});
  if(!check.ok||check.warnings.length&&!approveOverlaps)
   throw Error('Zielwand-Konflikt: '+[...check.errors,...check.warnings].join('; '));
  // All validation above. Mutation is now atomic and explicit.
  old.holes=old.holes.filter(h=>h.id!==plan.id);
  dest.holes.push({...source,u:check.u,v:check.v});
  for(const c of project.cables){
   for(const key of ['a','b'])if(c[key]?.wall===plan.from&&c[key].hole===plan.id)
    c[key].wall=plan.to;
  }
  return source.id;
 }
 return {COMMON_DN,wall,span,radius,uniqueId,snapMm,candidate,copySpec,
  planGroup,applyGroup,planTransfer,applyTransfer};
});
