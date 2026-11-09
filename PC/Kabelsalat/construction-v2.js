/* Kabelsalat v2.0 – pure geometric concept planning (all coordinates in m).
 * No verified shaft dimensions, fire rating or manufacturer bend radii.
 * The latest 09.11.2026 concept sketches use a 75 mm A/B threshold;
 * the older chat specifies 100 mm. Never infer compliance from that conflict.
 */
(function(root,factory){
 const api=factory();
 if(typeof module==='object'&&module.exports)module.exports=api;
 if(root)root.KabelsalatConstructionV2=api;
})(typeof globalThis!=='undefined'?globalThis:this,function(){
 'use strict';
 const toVec=v=>[Number(v[0]),Number(v[1]),Number(v[2])];
 const subtract=(a,b)=>a.map((x,i)=>x-b[i]);
 const add=(a,b)=>a.map((x,i)=>x+b[i]);
 const scale=(a,n)=>a.map(x=>x*n);
 const length=v=>Math.hypot(...v);
 const clamp=(v,a,b)=>Math.min(b,Math.max(a,v));
 const cross=(a,b)=>[a[1]*b[2]-a[2]*b[1],a[2]*b[0]-a[0]*b[2],a[0]*b[1]-a[1]*b[0]];
 function approximateBendRadius(c){
  const [a,b,d,e]=c;
  let smallest=Infinity;
  for(let i=1;i<40;i++){
   const t=i/40,u=1-t;
   const tangent=add(add(scale(subtract(b,a),3*u*u),
    scale(subtract(d,b),6*u*t)),scale(subtract(e,d),3*t*t));
   const second=add(scale(add(subtract(d,scale(b,2)),a),6*u),
    scale(add(subtract(e,scale(d,2)),b),6*t));
   const curvature=length(cross(tangent,second));
   if(curvature>1e-10)smallest=Math.min(smallest,length(tangent)**3/curvature);
  }
  return Number.isFinite(smallest)?smallest:null;
 }
 const opposite=(a,b)=>!!a&&!!b&&
  ((a==='SW1'&&b==='SW3')||(a==='SW3'&&b==='SW1')||
   (a==='SW2'&&b==='SW4')||(a==='SW4'&&b==='SW2'));
 function cablePath(a,b,wallA,wallB,normalA,normalB,waypoints=[],minRadiusMm=null){
  if(!a||!b)return{kind:'unresolved',points:[],warnings:['Mindestens ein Kabelanschluss fehlt']};
  a=toVec(a);b=toVec(b);
  const userPoints=(waypoints||[]).filter(p=>Array.isArray(p)&&p.length===3&&p.every(Number.isFinite)).map(toVec);
  // Explicitly edited routes remain editable; three.js smooths their control points.
  if(userPoints.length)return{kind:'edited',points:[a,...userPoints,b],warnings:['Biegeradius der bearbeiteten Route nicht nachgewiesen']};
  if(opposite(wallA,wallB)){
   return{kind:'straight',points:[a,b],warnings:Math.abs(a[1]-b[1])>.025?
    ['Leerrohrhöhen unterschiedlich: geradliniger, geneigter Verlauf; Auflage gesondert prüfen']:[]};
  }
  if(!normalA||!normalB)return{kind:'unresolved',points:[],warnings:['Einführungsrichtung unbekannt']};
  const distance=length(subtract(b,a));
  const reach=clamp(distance*.38,.18,.75);
  const c1=add(a,scale(toVec(normalA),reach));
  const c2=add(b,scale(toVec(normalB),reach));
  const minimum=approximateBendRadius([a,c1,c2,b]);
  const threshold=Number(minRadiusMm);
  const warnings=['Seitlicher Abgang: Biegeradius aus schematischem Bézier-Bogen nur näherungsweise'];
  if(minRadiusMm===null||!Number.isFinite(threshold)||threshold<=0)
   warnings.push('Hersteller-Mindestbiegeradius nicht hinterlegt');
  else if(minimum!==null&&minimum*1000<threshold)
   warnings.push('Geometrischer Biegeradius ca. '+Math.round(minimum*1000)+' mm unterschreitet den eingegebenen Projektwert '+threshold+' mm');
  else warnings.push('Projektwert grob verglichen, tatsächlicher Radius und Einbau ungeprüft');
  return{kind:'smooth-side',points:[a,c1,c2,b],minRadiusApproxM:minimum,warnings};
 }
 const rect=(name,kind,low,high)=>({name,kind,low,high});
 function construction(group,cables,ports,dims){
  const result={group,variant:group==='OLA'?'A':group==='MS'?'B':null,
   panels:[],supports:[],issues:[],status:'pending',planOnly:true};
  if(!result.variant){result.issues.push('Kein OLA/MS-System');return result;}
  if(!Array.isArray(cables)||!cables.length){result.issues.push('Keine zugeordneten Kabel');return result;}
  if(cables.some(c=>!c.a||!c.b)){result.issues.push('Offene Endpunkte – kein geschlossener Kanal erfunden');return result;}
  if(cables.some(c=>!opposite(c.wallA,c.wallB))){
   result.issues.push('Seitlicher Abgang: Kanal benötigt geplanten Bogen und Radiusnachweis');
   return result;
  }
  const points=cables.flatMap(c=>[c.a,c.b]);
  const dx=Math.max(...points.map(p=>p[0]))-Math.min(...points.map(p=>p[0]));
  const dz=Math.max(...points.map(p=>p[2]))-Math.min(...points.map(p=>p[2]));
  const along=dx>dz?0:2,cross=along===0?2:0;
  if(cables.some(c=>Math.abs(c.a[1]-c.b[1])>.025)){
   result.issues.push('Höhenversatz: waagerechte Kanalbodenplatte nicht verifiziert');return result;
  }
  if(cables.some(c=>(along===0?Math.abs(c.a[2]-c.b[2]):Math.abs(c.a[0]-c.b[0]))>.025)){
   result.issues.push('Versetzte Rohre: gerade Auflage nicht verifiziert');return result;
  }
  const allR=cables.map(c=>clamp(Number(c.radius)||.022,.005,.065));
  const minY=Math.min(...points.map(p=>p[1])),maxY=Math.max(...points.map(p=>p[1]));
  const t=.035,margin=.055;
  const maxRadius=Math.max(...allR);
  const bot=minY-maxRadius-.003-t,top=maxY+maxRadius+.17;
  if(!Number.isFinite(bot)||!Number.isFinite(top)||bot<.045||top>dims.h-.035){
   result.issues.push('Kanalhöhe außerhalb verfügbarer schematischer Schachtgeometrie');return result;
  }
  const crossMin=Math.max(-dims[['w','h','d'][cross]]/2+.014,
   Math.min(...points.map(p=>p[cross]))-maxRadius-margin);
  const crossMax=Math.min(dims[['w','h','d'][cross]]/2-.014,
   Math.max(...points.map(p=>p[cross]))+maxRadius+margin);
  if(crossMax-crossMin<2*t+.035){result.issues.push('Zu wenig Platz zwischen Kanalwänden');return result;}
  const low=[0,bot,0],high=[0,top,0];
  low[along]=-dims[['w','h','d'][along]]/2;
  high[along]=dims[['w','h','d'][along]]/2;
  low[cross]=crossMin;high[cross]=crossMax;
  // Collision guard: no unrelated pipe on a wall can be hidden by an end plate,
  // including currently unoccupied pipe openings.
  const endpointRefs=new Set(cables.flatMap(c=>[c.refA,c.refB]).filter(Boolean));
  for(const p of ports||[]){
   if(endpointRefs.has(p.id))continue;
   const v=p.point;if(!v)continue;
   const rad=clamp(Number(p.radius)||.035,.005,.18);
   const atEnd=Math.abs(v[along]-low[along])<.13||Math.abs(v[along]-high[along])<.13;
   const inCross=v[cross]+rad>crossMin-.012&&v[cross]-rad<crossMax+.012;
   const inHeight=v[1]+rad>bot-.018&&v[1]-rad<top+.018;
   if(atEnd&&inCross&&inHeight)result.issues.push('Fremdes Leerrohr '+String(p.id)+' würde vom Kanal erfasst');
  }
  if(result.issues.length)return result;
  // Six actual construction panels; both end plates have cable openings.
  const horizontal=(name,y1,y2)=>rect(name,'solid',
   low.map((v,i)=>i===1?y1:v),high.map((v,i)=>i===1?y2:v));
  result.panels.push(horizontal('Kanalboden',bot,bot+t));
  result.panels.push(horizontal('Kanaldeckel',top-t,top));
  for(const side of [0,1]){
   const a=low.slice(),b=high.slice();
   a[1]=bot+t;b[1]=top-t;
   if(side===0)b[cross]=low[cross]+t;else a[cross]=high[cross]-t;
   result.panels.push(rect(side?'Seitenplatte rechts':'Seitenplatte links','solid',a,b));
  }
  for(const side of [0,1]){
   const a=low.slice(),b=high.slice();
   // Endplates extend to the floor and roof so the complete circular cable
   // penetrations stay INSIDE the plate, without being silently discarded.
   a[1]=bot;b[1]=top;
   if(side===0)b[along]=low[along]+t;else a[along]=high[along]-t;
   const panel=rect(side?'Wandabschluss Ende':'Wandabschluss Anfang','perforated',a,b);
   panel.axis=along;panel.holes=cables.map((c,i)=>({
    at:(side?c.a[along]>c.b[along]?c.a:c.b:c.a[along]<c.b[along]?c.a:c.b).slice(),
    radius:allR[i]+.012
   }));
   result.panels.push(panel);
  }
  if(group==='OLA'){
   const n=5,h=bot/n;
   for(let i=0;i<n;i++){
    const a=low.slice(),b=high.slice();a[1]=i*h;b[1]=(i+1)*h;
    result.supports.push(rect('A · Stapelplatte '+(i+1),'stack',a,b));
   }
  }else{
   const n=4,span=high[along]-low[along],center=(crossMin+crossMax)/2;
   for(let i=0;i<n;i++){
    const a=low.slice(),b=high.slice(),v=low[along]+span*(i+1)/(n+1);
    a[1]=0;b[1]=bot;a[along]=v-.055;b[along]=v+.055;
    a[cross]=center-.065;b[cross]=center+.065;
    result.supports.push(rect('B · SP-Stützplatte '+(i+1),'support',a,b));
   }
  }
  result.status='concept';result.low=low;result.high=high;result.axis=along;
  result.issues.push('Schematische Maße: Statik, Durchführungen, Rmin und Brandschutzprüfung offen');
  return result;
 }
 return{cablePath,construction,opposite,referenceThresholdMm:75};
});