/* Kabelsalat: separate read-only batch analysis for up to 5,000 shafts.
 * No inference of physical continuity from shaft membership. */
(function(root,factory){
 const api=factory();
 if(typeof module==='object'&&module.exports)module.exports=api;
 if(root)root.KabelsalatShaftBatch=api;
})(typeof window!=='undefined'?window:null,function(){
'use strict';
const LIMITS={shafts:5000,passages:40000,bytes:5000000};
function parse(text,required,maxRows){
 const input=String(text||'').replace(/^\uFEFF/,'');
 if(input.length>LIMITS.bytes)throw Error('CSV überschreitet 5 MB Textbudget');
 const rows=[];let row=[],field='',quoted=false,after=false,line=1,start=1;
 const first=input.split(/\r?\n/,1)[0],delimiter=first.includes(';')?';':',';
 for(let i=0;i<input.length;i++){
  const c=input[i],n=input[i+1];
  if(quoted){
   if(c==='"'&&n==='"'){field+='"';i++;}
   else if(c==='"')quoted=false;
   else {field+=c;if(c==='\n')line++;}
  }else if(c==='"'){
   if(field.trim())throw Error('Ungültiges CSV-Quote in Zeile '+line);
   quoted=true;after=true;
  }else if(c===delimiter){row.push(field);field='';after=false;}
  else if(c==='\r'&&n==='\n'||c==='\n'){
   row.push(field);
   if(row.some(v=>v.trim()))rows.push({line:start,cells:row});
   if(rows.length>maxRows+1)throw Error('Zu viele CSV-Zeilen; Limit '+maxRows);
   row=[];field='';after=false;
   if(c==='\r')i++;
   line++;start=line;
  }else {
   if(after&&!/\s/.test(c))throw Error('Zeichen nach Quote in Zeile '+line);
   field+=c;
  }
  if(field.length>1000)throw Error('CSV-Feld zu lang');
 }
 if(quoted)throw Error('Nicht abgeschlossene CSV-Anführungszeichen');
 row.push(field);if(row.some(v=>v.trim()))rows.push({line:start,cells:row});
 if(!rows.length)throw Error('CSV hat keine Überschrift');
 const header=rows.shift().cells.map(v=>v.trim().toLowerCase());
 if(header.some((h,i)=>header.indexOf(h)!==i))throw Error('Doppelte CSV-Spalte');
 for(const key of required)if(!header.includes(key))throw Error('CSV-Spalte fehlt: '+key);
 return rows.map(r=>{
  if(r.cells.length!==header.length)throw Error('Zeile '+r.line+': Spaltenzahl stimmt nicht');
  const obj={_line:r.line};
  header.forEach((key,i)=>obj[key]=r.cells[i].trim());
  return obj;
 });
}
const id=s=>typeof s==='string'&&/^[\p{L}\p{N}_.:/+\- ]{1,120}$/u.test(s);
const source=s=>typeof s==='string'?s.slice(0,240):'';
function inventory(shaftsCsv,passagesCsv){
 const shaftRows=parse(shaftsCsv,['shaft_id'],LIMITS.shafts),
  passRows=parse(passagesCsv,['shaft_id','cable_id'],LIMITS.passages);
 const shafts=new Map(),passages=[],errors=[],warnings=[],seenPassages=new Set();
 const issues=(list,line,message)=>list.push({line,message:'Zeile '+line+': '+message});
 for(const row of shaftRows){
  if(!id(row.shaft_id)){issues(errors,row._line,'ungültige Schacht-ID');continue;}
  if(shafts.has(row.shaft_id)){issues(errors,row._line,'doppelte Schacht-ID '+row.shaft_id);continue;}
  shafts.set(row.shaft_id,{id:row.shaft_id,
   name:source(row.shaft_name)||row.shaft_id,
   location:source(row.location),source:source(row.source)});
 }
 for(const row of passRows){
  const shaftId=row.shaft_id,cableId=row.cable_id;
  if(!shafts.has(shaftId)){issues(errors,row._line,'unbekannter Schacht '+shaftId);continue;}
  if(!id(cableId)){issues(errors,row._line,'ungültige Kabel-ID');continue;}
  const rawSeq=row.sequence||'',seq=rawSeq===''?null:Number(rawSeq);
  if(seq!==null&&(!/^[1-9]\d*$/.test(rawSeq)||!Number.isSafeInteger(seq)||seq>100000))
   {issues(errors,row._line,'Reihenfolge muss positive Ganzzahl sein');continue;}
  const key=cableId+'\u0000'+shaftId;
  if(seenPassages.has(key)){issues(errors,row._line,'doppelter Kabel-Schacht-Eintrag '+cableId+'/'+shaftId);continue;}
  seenPassages.add(key);
  const state=row.status||'unverified';
  if(!['unverified','candidate','documented'].includes(state))
   {issues(errors,row._line,'unbekannter Quellenstatus');continue;}
  const proof=source(row.source);
  if(state==='documented'&&!proof){issues(errors,row._line,'dokumentierte Passage ohne Beleg');continue;}
  passages.push({shaftId,cableId,sequence:seq,
   source:proof,status:state,line:row._line});
 }
 // Hard errors are never silently ignored: the full run is blocked.
 return {ok:errors.length===0,shafts:[...shafts.values()],passages:errors.length?[]:passages,
  errors,warnings,summary:{shaftCount:shafts.size,passageCount:passages.length,
   cableCount:new Set(passages.map(x=>x.cableId)).size}};
}
function analyze(data,{query='',includeCandidates=true}={}){
 if(!data?.ok)throw Error('Zuerst gültige CSV-Dateien prüfen');
 const q=String(query).trim().toLocaleLowerCase('de-DE');
 const matches=data.passages.filter(p=>
  (!q||p.cableId.toLocaleLowerCase('de-DE').includes(q))&&
  (includeCandidates||p.status==='documented'));
 const groups=new Map(),shafts=new Map(data.shafts.map(s=>[s.id,s]));
 for(const row of matches){if(!groups.has(row.cableId))groups.set(row.cableId,[]);
  groups.get(row.cableId).push(row);}
 const routes=[];
 for(const [cableId,items] of groups){
  const ordered=items.every(x=>x.sequence!==null)&&
    new Set(items.map(x=>x.sequence)).size===items.length;
  const parts=ordered?[...items].sort((a,b)=>a.sequence-b.sequence):[...items]
   .sort((a,b)=>a.shaftId.localeCompare(b.shaftId,'de'));
  const seqs=parts.map(x=>x.sequence),gaps=ordered&&
   (seqs[0]!==1||seqs.some((n,i)=>i>0&&n!==seqs[i-1]+1));
  const allDocumented=parts.every(x=>x.status==='documented');
  const warnings=[];
  if(!ordered)warnings.push('Reihenfolge nicht eindeutig – kein Verlauf ableitbar');
  if(gaps)warnings.push('Reihenfolge hat Lücken – fehlende Schächte möglich');
  if(!allDocumented)warnings.push('Mindestens ein Schachtdurchgang ohne gesicherten Beleg');
  warnings.push('Physische Kabelkontinuität nicht nachgewiesen');
  routes.push({cableId,shaftCount:parts.length,ordered,hasGaps:!!gaps,
   status:allDocumented&&ordered&&!gaps?'Dokumentierte Reihenfolge – Kontinuität ungeprüft':
    'Manuelle Prüfung nötig',warnings,
   shafts:parts.map(x=>({...x,shaftName:shafts.get(x.shaftId)?.name||x.shaftId}))});
 }
 routes.sort((a,b)=>a.cableId.localeCompare(b.cableId,'de'));
 return {routes,cableCount:routes.length,
  matchedShafts:new Set(routes.flatMap(x=>x.shafts.map(y=>y.shaftId))).size,
  allShafts:data.summary.shaftCount};
}
function safe(value){
 const s=String(value??'');
 return /^[=+\-@\t\r]/.test(s)?'\''+s:s;
}
function csvField(v){
 const s=safe(v);return /[;"\r\n]/.test(s)?'"'+s.replace(/"/g,'""')+'"':s;
}
function csv(result){
 const header=['cable_id','shaft_id','shaft_name','sequence','source','evidence_status',
  'order_status','route_warning'];
 const rows=[header.join(';')];
 for(const route of result.routes)for(const item of route.shafts)
  rows.push([route.cableId,item.shaftId,item.shaftName,item.sequence??'',
   item.source,item.status,route.ordered&&!route.hasGaps?'ordered':'review',
   route.warnings.join(' | ')].map(csvField).join(';'));
 return '\uFEFF'+rows.join('\r\n')+'\r\n';
}
function txt(result){
 const parts=['Kabelsalat – Schacht-/Kabel-Durchgangsliste',
  'Auswertung nur aus importierten Daten. Keine bestätigte physische Durchgängigkeit.',
  result.cableCount+' Kabel · '+result.matchedShafts+' relevante von '+result.allShafts+' Schächten'];
 for(const route of result.routes){
  parts.push('',route.cableId+' · '+route.status,
   ...route.shafts.map(x=>'  '+(x.sequence??'?')+'. '+x.shaftId+' ('+x.shaftName+
    ') · '+x.status+(x.source?' · '+x.source:'')),
   'Prüfung: '+route.warnings.join(' / '));
 }
 return parts.join('\n')+'\n';
}
function template(){
 return {shafts:'shaft_id;shaft_name;location;source\nS001;Schacht Nord;;Liste R1\nS002;Schacht Süd;;Liste R1\n',
  passages:'shaft_id;cable_id;sequence;source;status\nS001;K-01;1;Plan R1 S. 2;candidate\nS002;K-01;2;Plan R1 S. 4;candidate\n'};
}
return {LIMITS,parse,inventory,analyze,csv,txt,template};
});