/* Kabelsalat v5 CSV interchange. Dry-run first, never partially apply source data. */
(function(root,factory){
 const api=factory();
 if(typeof module==='object'&&module.exports)module.exports=api;
 if(root)root.KabelsalatCsv=api;
})(typeof window!=='undefined'?window:null,function(){
'use strict';
const TYPES={
 positions:['shaft_id','x_mm','y_mm','z_mm','crs'],
 walls:['shaft_id','wall_id','thickness_mm','length_mm','height_mm'],
 holes:['shaft_id','wall_id','hole_id','x_mm','y_mm','dn_mm','opening_mm','opening_type'],
 cables:['shaft_id','cable_id','name','group','diameter_mm','a_wall','a_hole','b_wall','b_hole']
};
const clone=o=>JSON.parse(JSON.stringify(o));
const validId=v=>typeof v==='string'&&/^[a-z0-9_.:/-]{1,120}$/i.test(v);
const cell=v=>v===null||v===undefined?'':String(v);
const text=v=>cell(v).replace(/^[\uFEFF]/,'');
const number=(v,{required=true,min=-100000000,max=100000000,unit='mm'}={})=>{
 const raw=text(v).trim();
 if(!raw){if(!required)return null;throw Error('Zahlenwert fehlt');}
 if(!/^-?\d+(?:[.,]\d+)?$/.test(raw))throw Error('Keine gültige Dezimalzahl: '+raw.slice(0,45));
 const n=Number(raw.replace(',','.'))*(unit==='m'?1000:1);
 if(!Number.isFinite(n)||n<min||n>max)throw Error('Zahl außerhalb des gültigen Bereichs: '+raw.slice(0,45));
 return Math.round(n*1000)/1000;
};
function format(v){
 const s=cell(v);
 if(!/[;"\r\n]/.test(s))return s;
 return '"'+s.replace(/"/g,'""')+'"';
}
function stringify(rows,kind){
 if(!TYPES[kind])throw Error('CSV-Typ unbekannt');
 return '\uFEFF'+TYPES[kind].join(';')+'\r\n'+rows.map(row=>
  TYPES[kind].map(key=>format(row[key])).join(';')).join('\r\n')+(rows.length?'\r\n':'');
}
function parseCsv(content,kind){
 if(!TYPES[kind])throw Error('CSV-Typ unbekannt');
 const raw=text(content);
 if(raw.length>3500000)throw Error('CSV-Datei größer als 3,5 MB');
 const delim=raw.split(/\r?\n/,1)[0].includes(';')?';':',',rows=[];
 let cells=[],buf='',quoted=false,rowNumber=1,startLine=1,afterQuote=false;
 for(let i=0;i<raw.length;i++){
  const ch=raw[i],next=raw[i+1];
  if(quoted){
   if(ch==='"'&&next==='"'){buf+='"';i++;continue;}
   if(ch==='"'){quoted=false;afterQuote=true;continue;}
   buf+=ch;if(ch==='\n')rowNumber++;
  }else if(ch==='"'){
   if(buf.trim())throw Error('Zeile '+rowNumber+': unzulässiges Anführungszeichen');
   quoted=true;
  }else if(ch===delim){
   if(buf.length>3000)throw Error('Zeile '+rowNumber+': Feld zu lang');
   cells.push(buf);buf='';afterQuote=false;
  }else if(ch==='\r'&&next==='\n'||ch==='\n'){
   cells.push(buf);buf='';afterQuote=false;
   if(cells.some(c=>c.trim()))rows.push({cells,line:startLine});
   cells=[];if(ch==='\r')i++;rowNumber++;startLine=rowNumber;
  }else{
   if(afterQuote&&ch!==' '&&ch!=='\t')throw Error('Zeile '+rowNumber+': Zeichen nach CSV-Anführungszeichen');
   buf+=ch;
  }
  if(rows.length>5001)throw Error('CSV hat mehr als 5.000 Datensätze');
 }
 if(quoted)throw Error('CSV endet mit ungeschlossener Zeichenfolge');
 if(buf||cells.length){cells.push(buf);if(cells.some(c=>c.trim()))rows.push({cells,line:startLine});}
 if(!rows.length)throw Error('CSV ist leer');
 const header=rows.shift().cells.map(x=>x.trim().toLowerCase());
 const required=TYPES[kind],missing=required.filter(key=>!header.includes(key));
 if(missing.length)throw Error('CSV-Spalte fehlt: '+missing.join(', '));
 const duplicates=header.filter((key,i)=>header.indexOf(key)!==i);
 if(duplicates.length)throw Error('Doppelte CSV-Spalte: '+duplicates[0]);
 const mapped=rows.map(row=>{
  const data={__line:row.line};
  if(row.cells.length!==header.length)throw Error('Zeile '+row.line+': '+
   header.length+' Spalten erwartet, '+row.cells.length+' gefunden');
  for(const k of required)data[k]=row.cells[header.indexOf(k)].trim();
  return data;
 });
 return {rows:mapped,header,delimiter:delim};
}
function wallSpan(snapshot,id){
 if(!snapshot?.dims||!Array.isArray(snapshot.walls))throw Error('Schachtgeometrie fehlt');
 const pdf=snapshot.sourceInfo?.sourceVersion?.startsWith('PDF V2')===true;
 return (id==='SW1'||id==='SW3')?
  (pdf?snapshot.dims.w:snapshot.dims.d):
  (pdf?snapshot.dims.d:snapshot.dims.w);
}
function exportCsv(project,kind){
 if(!TYPES[kind])throw Error('CSV-Typ unbekannt');
 const net=project.network||{shafts:[]},rows=[];
 for(const shaft of net.shafts||[]){
  const p=shaft.snapshot;
  if(kind==='positions')rows.push({shaft_id:shaft.id,x_mm:shaft.x,
   y_mm:shaft.y===null||shaft.y===undefined?'':shaft.y,z_mm:shaft.z,
   crs:net.coordinateReference||''});
  if(kind==='walls')for(const w of p.walls||[])rows.push({
   shaft_id:shaft.id,wall_id:w.id,thickness_mm:w.thickness,
   length_mm:wallSpan(p,w.id),height_mm:p.dims.h});
  if(kind==='holes')for(const w of p.walls||[])for(const h of w.holes||[])rows.push({
   shaft_id:shaft.id,wall_id:w.id,hole_id:h.id,
   x_mm:Math.round(h.u*wallSpan(p,w.id)*1000)/1000,
   y_mm:Math.round(h.v*p.dims.h*1000)/1000,
   dn_mm:h.openingType==='bare-hole'?'':h.dn??'',
   opening_mm:h.dimensionSpec?.openingMm??'',
   opening_type:h.openingType||'unknown'});
  if(kind==='cables')for(const c of p.cables||[])rows.push({
   shaft_id:shaft.id,cable_id:c.id,name:c.name,group:c.group,
   diameter_mm:c.diameter,a_wall:c.a?.wall||'',a_hole:c.a?.hole||'',
   b_wall:c.b?.wall||'',b_hole:c.b?.hole||''});
 }
 return stringify(rows,kind);
}
function validateRefs(s,c,field){
 const wall=c[field+'_wall'],hole=c[field+'_hole'];
 if(!wall&&!hole)return null;
 if(!wall||!hole)throw Error('Anschluss '+field.toUpperCase()+' unvollständig');
 if(!s.walls.some(w=>w.id===wall&&w.holes.some(h=>h.id===hole)))
  throw Error('Anschluss '+field.toUpperCase()+' ohne passende Rohr-/Loch-ID: '+wall+'/'+hole);
 return {wall,hole};
}
function preview(project,kind,content,{unit='mm',source='Manueller CSV-Import'}={}){
 if(!['mm','m'].includes(unit))throw Error('Einheit ausdrücklich mm oder m angeben');
 const parsed=parseCsv(content,kind),base=JSON.stringify(project),next=clone(project),
  errors=[],changes=[],seen=new Set();
 if(!next.network?.shafts?.length)throw Error('Schachtnetz fehlt');
 if(parsed.rows.length>5000)throw Error('CSV-Zeilenlimit überschritten');
 for(const row of parsed.rows){
  const line='Zeile '+row.__line+': ';
  try{
   const shaft=next.network.shafts.find(s=>s.id===row.shaft_id);
   if(!shaft)throw Error('Unbekannte Schacht-ID '+row.shaft_id);
   const p=shaft.snapshot,key=kind+'|'+row.shaft_id+'|'+(row.wall_id||'')+'|'+
    (row.hole_id||row.cable_id||'');
   if(seen.has(key))throw Error('Doppelte Objekt-ID im CSV');
   seen.add(key);
   if(kind!=='positions'&&p.sourceInfo)
    throw Error('Original-/PDF-Schacht ist schreibgeschützt; keine stillschweigende Überschreibung');
   if(kind==='positions'){
    const x=number(row.x_mm,{unit}),z=number(row.z_mm,{unit}),
     y=number(row.y_mm,{required:false,unit});
    if(![x,z,y].every(v=>v===null||Number.isInteger(v)))
     throw Error('Koordinaten müssen ganze mm ergeben');
    if(row.crs&&row.crs.length>120)throw Error('Koordinatenreferenz zu lang');
    if(row.crs&&next.network.coordinateReference&&
      row.crs!==next.network.coordinateReference)throw Error('Widersprüchliches CRS');
    if(row.crs)next.network.coordinateReference=row.crs;
    shaft.x=x;shaft.z=z;
    if(row.y_mm!=='')shaft.y=y;
    changes.push({line:row.__line,shaftId:shaft.id,id:shaft.id,field:'XYZ'});
   }else if(kind==='walls'){
    const w=p.walls.find(w=>w.id===row.wall_id);
    if(!w)throw Error('Unbekannte Wand-ID '+row.wall_id);
    const thickness=number(row.thickness_mm,{unit,min:40,max:500}),
     length=number(row.length_mm,{unit,min:600,max:12000}),
     height=number(row.height_mm,{unit,min:600,max:8000});
    if(Math.abs(length-wallSpan(p,w.id))>.001||Math.abs(height-p.dims.h)>.001)
     throw Error('Wandlänge/-höhe weichen vom aktuellen Grundriss ab; kein stilles Resizing');
    w.thickness=thickness;
    changes.push({line:row.__line,shaftId:shaft.id,id:w.id,field:'thickness'});
   }else if(kind==='holes'){
    const w=p.walls.find(w=>w.id===row.wall_id);
    if(!w)throw Error('Unbekannte Wand-ID '+row.wall_id);
    if(!validId(row.hole_id))throw Error('Ungültige Loch-ID');
    const existing=w.holes.find(h=>h.id===row.hole_id);
    if(!existing&&p.walls.some(other=>other.id!==w.id&&other.holes.some(h=>h.id===row.hole_id)))
     throw Error('Loch-ID existiert an anderer Wand');
    if(!existing&&w.holes.length>=600)throw Error('Mehr als 600 Öffnungen in einer Wand');
    const x=number(row.x_mm,{unit,min:0,max:12000}),
     y=number(row.y_mm,{unit,min:0,max:8000});
    const kindValue=row.opening_type||'unknown';
    if(!['bare-hole','conduit','unknown'].includes(kindValue))
     throw Error('Unbekannte Öffnungsbauart');
    const opening=number(row.opening_mm,{required:kindValue==='bare-hole',
     unit,min:20,max:600});
    const dnValue=number(row.dn_mm,{required:kindValue!=='bare-hole',unit,min:20,max:300});
    const dn=dnValue===null?Math.round(Math.min(300,opening)):dnValue;
    if(!Number.isInteger(dn))throw Error('DN / Anzeige-Maß muss ganzzahlig sein');
    const span=wallSpan(p,w.id),r=(opening??dn)/2;
    if(x-r<0||x+r>span||y-r<0||y+r>p.dims.h)
     throw Error('Loch-Ø überschreitet Wandrand');
    for(const other of w.holes){
     if(other.id===row.hole_id)continue;
     const diameter=other.dimensionSpec?.openingMm??other.dn;
     if(Math.hypot(x-other.u*span,y-other.v*p.dims.h)<r+diameter/2)
      throw Error('Öffnung kollidiert mit '+other.id);
    }
    const h=existing||{id:row.hole_id,row:0,col:0};
    h.u=x/span;h.v=y/p.dims.h;h.dn=dn;
    if(kindValue==='unknown')delete h.openingType;else h.openingType=kindValue;
    if(opening!==null||h.dimensionSpec){
     h.dimensionSpec={...h.dimensionSpec,openingMm:opening,
      nominalMm:kindValue==='bare-hole'?null:dn,status:'unverified',
      reviewer:'',reviewedAt:'',method:'manual',source:String(source).slice(0,240)};
    }
    if(!existing)w.holes.push(h);
    changes.push({line:row.__line,shaftId:shaft.id,id:h.id,
     field:existing?'hole-update':'hole-create'});
   }else if(kind==='cables'){
    if(!validId(row.cable_id))throw Error('Ungültige Kabel-ID');
    if(!row.name||row.name.length>80)throw Error('Kabelname fehlt oder zu lang');
    if(!['OLA','MS','Weitere'].includes(row.group))throw Error('Kabelgruppe unbekannt');
    const dia=number(row.diameter_mm,{unit,min:2,max:80});
    const a=validateRefs(p,row,'a'),b=validateRefs(p,row,'b');
    const existing=p.cables.find(c=>c.id===row.cable_id);
    if(!existing&&p.cables.length>=400)throw Error('Mehr als 400 Kabel');
    if(existing){existing.name=row.name;existing.group=row.group;
     existing.diameter=dia;existing.a=a;existing.b=b;
     existing.reviewStatus='Zur Prüfung';}
    else p.cables.push({id:row.cable_id,name:row.name,group:row.group,
     diameter:dia,color:'#398bdb',route:[],a,b,reviewStatus:'Entwurf'});
    changes.push({line:row.__line,shaftId:shaft.id,id:row.cable_id,
     field:existing?'cable-update':'cable-create'});
   }
  }catch(error){errors.push({line:row.__line,code:'invalid-row',
   message:line+String(error.message).slice(0,240)});}
 }
 if(errors.length)return {ok:false,kind,base,errors,changes:[],after:null,source};
 return {ok:true,kind,base,errors:[],changes,after:JSON.stringify(next),
  source:String(source).slice(0,240),unit};
}
function apply(project,plan){
 if(!plan?.ok||plan.errors?.length||!plan.after)throw Error('Keine gültige CSV-Vorschau');
 if(JSON.stringify(project)!==plan.base)throw Error('CSV-Vorschau veraltet – erneut prüfen');
 const next=JSON.parse(plan.after);
 if(!next?.network||!Array.isArray(next.network.shafts))throw Error('CSV-Ziel ungültig');
 return next;
}
return {TYPES,parseCsv,exportCsv,preview,apply,number};
});
