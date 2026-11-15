'use strict';
/* Independent 400-shaft handover analyzer: never treats graph proximity as evidence
 * of physical cable continuity. Input is solely explicit shaft/cable associations.
 * Does NOT mutate the 30-shaft 3D-editor model or claim to invoke remote AI.
 */
(function(root,factory){
 const api=factory();
 if(typeof module==='object'&&module.exports)module.exports=api;
 if(root)root.KabelsalatHandover=api;
})(typeof window!=='undefined'?window:null,function(){
 const MAX_SHAFTS=500,MAX_PASSAGES=25000,MAX_BYTES=3500000;
 const shaftHeaders=['schacht_id','schachtname'];
 const passHeaders=['kabel_id','kabelname','schacht_id','reihenfolge','quelle','status'];
 const clean=v=>String(v??'').trim();
 const key=v=>clean(v).toLocaleUpperCase('de-DE');
 const validId=s=>typeof s==='string'&&s.length>0&&s.length<=120&&!/[\r\n<>]/.test(s);
 const safeText=(s,n=180)=>clean(s).slice(0,n);
 const encode=s=>{
  const raw=String(s??'');
  // Spreadsheet CSV injection: never let untrusted source titles/cable names
  // become Excel formulas (=, +, -, @), even when the cell is quoted.
  const safe=/^[\u0000-\u0020]*[=+\-@]/.test(raw)?"'"+raw:raw;
  return '"'+safe.replace(/"/g,'""')+'"';
 };
 function rowsCsv(rows,fields){
  return '\uFEFF'+fields.join(';')+'\r\n'+rows.map(row=>
   fields.map(f=>encode(row[f]??'')).join(';')).join('\r\n')+(rows.length?'\r\n':'');
 }
 function readCsv(content,required,maxRows){
  const src=String(content??'').replace(/^\uFEFF/,'');
  if(src.length>MAX_BYTES)throw Error('CSV überschreitet 3,5 MB');
  if(!src.trim())throw Error('CSV ist leer');
  const firstLine=src.split(/\r?\n/,1)[0],separator=firstLine.includes(';')?';':',';
  let quote=false,afterQuote=false,cell='',record=[],records=[],line=1,start=1;
  const pushCell=()=>{if(cell.length>1000)throw Error('CSV-Feld länger als 1000 Zeichen');
   record.push(cell);cell='';afterQuote=false;};
  const pushRow=()=>{pushCell();if(record.some(v=>v.trim()))records.push({values:record,line:start});
   record=[];start=line; if(records.length>maxRows+1)throw Error('Mehr als '+maxRows+' CSV-Zeilen');};
  for(let i=0;i<src.length;i++){
   const ch=src[i],next=src[i+1];
   if(quote){
    if(ch==='"'&&next==='"'){cell+='"';i++;continue;}
    if(ch==='"'){quote=false;afterQuote=true;continue;}
    cell+=ch;if(ch==='\n')line++;
   }else if(ch==='"'){
    if(cell.trim())throw Error('Anführungszeichen mitten im Feld, Zeile '+line);
    quote=true;
   }else if(ch===separator){pushCell();}
   else if(ch==='\r'&&next==='\n'||ch==='\n'){
    pushRow();if(ch==='\r')i++;line++;start=line;
   }else{
    if(afterQuote&&ch!==' '&&ch!=='\t')
     throw Error('Unerwartetes Zeichen nach Anführungszeichen, Zeile '+line);
    cell+=ch;
   }
  }
  if(quote)throw Error('Nicht geschlossenes CSV-Anführungszeichen');
  if(cell||record.length)pushRow();
  if(!records.length)throw Error('CSV-Kopf fehlt');
  const headers=records.shift().values.map(x=>x.trim().toLocaleLowerCase('de-DE'));
  for(const h of required)if(!headers.includes(h))throw Error('CSV-Spalte fehlt: '+h);
  if(new Set(headers).size!==headers.length)throw Error('Doppelte CSV-Kopfspalte');
  return records.map(r=>{
   if(r.values.length!==headers.length)throw Error('Zeile '+r.line+
    ': '+headers.length+' Spalten erwartet');
   const obj={line:r.line};
   for(const name of required)obj[name]=clean(r.values[headers.indexOf(name)]);
   return obj;
  });
 }
 function analyze(shaftCsv,passageCsv,{filter='',maxShafts=MAX_SHAFTS}={}){
  const rawShafts=readCsv(shaftCsv,shaftHeaders,Math.min(MAX_SHAFTS,maxShafts));
  const rawPassages=clean(passageCsv)?readCsv(passageCsv,passHeaders,MAX_PASSAGES):[];
  const errors=[],warnings=[],shafts=[],shaftById=new Map();
  for(const s of rawShafts){
   const id=key(s.schacht_id),name=safeText(s.schachtname);
   if(!validId(id)){errors.push('Zeile '+s.line+' Schacht-ID fehlt/ungültig');continue;}
   if(shaftById.has(id)){errors.push('Doppelte Schacht-ID '+id);continue;}
   const entry={id,name:name||id,cables:[],status:'ohne Kabelnachweis'};
   shaftById.set(id,entry);shafts.push(entry);
  }
  const groups=new Map(),seen=new Set();
  for(const p of rawPassages){
   const cableId=key(p.kabel_id),shaftId=key(p.schacht_id),
    name=safeText(p.kabelname),source=safeText(p.quelle,240),status=key(p.status);
   if(!validId(cableId)||!validId(shaftId)){errors.push('Zeile '+p.line+' Kabel-/Schacht-ID fehlt');continue;}
   if(!shaftById.has(shaftId)){errors.push('Zeile '+p.line+': Schacht '+shaftId+' nicht in Schachtliste');continue;}
   if(!['PLAN','KANDIDAT','VOR_ORT'].includes(status)){
    errors.push('Zeile '+p.line+': Status muss PLAN, KANDIDAT oder VOR_ORT sein');continue;
   }
   if(!source){errors.push('Zeile '+p.line+': Quellenangabe erforderlich');continue;}
   const order=Number(p.reihenfolge);
   if(!/^\d+$/.test(p.reihenfolge)||!Number.isSafeInteger(order)||order<1||order>MAX_SHAFTS){
    errors.push('Zeile '+p.line+': Reihenfolge 1–'+MAX_SHAFTS+' erwartet');continue;
   }
   const idPair=cableId+'|'+shaftId;
   if(seen.has(idPair)){errors.push('Zeile '+p.line+': doppelte Kabel-/Schachtzuordnung '+idPair);continue;}
   seen.add(idPair);
   if(!groups.has(cableId))groups.set(cableId,{id:cableId,name:name||cableId,steps:[]});
   const cable=groups.get(cableId);
   if(name&&cable.name!==cableId&&name!==cable.name){
    warnings.push('Kabel '+cableId+': unterschiedliche Kabelnamen; erster Name gilt');
   }else if(cable.name===cableId&&name)cable.name=name;
   cable.steps.push({shaftId,order,source,status});
  }
  const cables=[],handover=[],open=[];
  for(const cable of groups.values()){
   cable.steps.sort((a,b)=>a.order-b.order||a.shaftId.localeCompare(b.shaftId));
   const orders=new Set(),uncertain=[];
   for(const step of cable.steps){
    if(orders.has(step.order))errors.push('Kabel '+cable.id+': Reihenfolge '+step.order+' mehrfach');
    orders.add(step.order);
    if(step.status!=='VOR_ORT')uncertain.push(step.shaftId);
   }
   if(cable.steps.length===1)warnings.push('Kabel '+cable.id+': nur eine Schachtzuordnung, tatsächlicher Verlauf ungeklärt');
   if(cable.steps.length>1){
    for(let i=1;i<cable.steps.length;i++)if(cable.steps[i].order!==cable.steps[i-1].order+1)
     warnings.push('Kabel '+cable.id+': Lücke in Reihenfolge '+cable.steps[i-1].order+' → '+cable.steps[i].order);
   }
   const path=cable.steps.map(x=>x.shaftId).join(' > ');
   for(let i=0;i<cable.steps.length;i++){
    const step=cable.steps[i],shaft=shaftById.get(step.shaftId);
    const role=cable.steps.length===1?'Einzelzuordnung, Verlauf offen':
     i===0?'Erster dokumentierter Schacht':
     i===cable.steps.length-1?'Letzter dokumentierter Schacht':
     'Durchgang laut Liste';
    const status=step.status==='VOR_ORT'?'Vor-Ort-Angabe (ungeprüft)':
     step.status==='PLAN'?'Planangabe (ungeprüft)':'KI-/Textkandidat (ungeprüft)';
    shaft.cables.push({id:cable.id,name:cable.name,role,source:step.source,status});
    shaft.status='Zuordnung aus Eingabe';
    handover.push({kabel_id:cable.id,kabelname:cable.name,
     schacht_id:shaft.id,schachtname:shaft.name,reihenfolge:step.order,
     rolle:role,quelle:step.source,status,schachtfolge:path,
     hinweis:'Keine automatisch bestätigte physische Kabelkontinuität'});
   }
   cables.push({...cable,shaftPath:path,uncertain});
  }
  for(const s of shafts)if(!s.cables.length)open.push({
   schacht_id:s.id,schachtname:s.name,frage:'Keine Kabelzuordnung eingetragen – Plan/Begehung prüfen',
   quelle:'',status:'offen'});
  for(const cable of cables)for(const step of cable.steps)if(step.status!=='VOR_ORT')
   open.push({schacht_id:step.shaftId,schachtname:shaftById.get(step.shaftId).name,
    frage:'Kabel '+cable.id+': '+(step.status==='KANDIDAT'?'Kandidatenzuordnung überprüfen':'Planbezug vor Ort abgleichen'),
    quelle:step.source,status:'offen'});
  for(const issue of warnings)if(issue.includes('Lücke')||issue.includes('Verlauf ungeklärt'))
   open.push({schacht_id:'',schachtname:'',frage:issue,quelle:'',status:'offen'});
  const term=key(filter);const filtered=term?handover.filter(r=>
   [r.kabel_id,r.kabelname,r.schacht_id,r.schachtname]
    .some(x=>key(x).includes(term))):handover;
  const shaftMatches=term?shafts.filter(s=>
   [s.id,s.name,...s.cables.map(c=>c.id)].some(x=>key(x).includes(term))):shafts;
  return {ok:errors.length===0,errors,warnings,shafts,cables,handover,open,
   filtered,shaftMatches,summary:{shafts:shafts.length,cables:cables.length,
    associations:handover.length,withoutEvidence:shafts.filter(x=>!x.cables.length).length,
    candidates:handover.filter(x=>x.status.includes('Kandidat')).length,
    physicallyVerified:0}};
 }
 function exportHandover(result){
  if(!result?.ok)throw Error('Keine gültige Auswertung zur Übergabe');
  return rowsCsv(result.handover,['kabel_id','kabelname','schacht_id','schachtname',
   'reihenfolge','rolle','quelle','status','schachtfolge','hinweis']);
 }
 function exportOpen(result){
  if(!result?.ok)throw Error('Keine gültige Auswertung');
  return rowsCsv(result.open,['schacht_id','schachtname','frage','quelle','status']);
 }
 function exportByShaft(result){
  if(!result?.ok)throw Error('Keine gültige Auswertung');
  return rowsCsv(result.shafts.map(s=>({
   schacht_id:s.id,schachtname:s.name,kabelanzahl:s.cables.length,
   kabel_ids:s.cables.map(c=>c.id).join(' | '),hinweis:s.cables.length?
    'Zuordnung aus vorliegenden Quellen, Kabelkontinuität ungeprüft':
    'Keine Kabelzuordnung eingetragen; keine Aussage über Belegung'})),
   ['schacht_id','schachtname','kabelanzahl','kabel_ids','hinweis']);
 }
 function aiBrief(){
  return [
   'Kabelsalat – Auftrag zur 400-Schacht-Kabelweg-Auswertung',
   '',
   'Lies die separat angehängte Schachtliste und die relevanten Pläne/Begehungsdaten.',
   'Ordne Kabel nur dann einem Schacht zu, wenn eine konkrete Quelle die Zuordnung trägt.',
   'Gib eine CSV mit exakt diesen Spalten zurück:',
   passHeaders.join(';'),
   '',
   'Eine Zeile je Kabel/Schacht. reihenfolge ist 1,2,3... in dokumentierter Laufrichtung.',
   'Status: PLAN = ausdrücklich im Plan, KANDIDAT = plausible unbestätigte Textzuordnung,',
   'VOR_ORT = nur mit entsprechendem eingereichten Vor-Ort-Beleg, nicht vom KI-Modell erfunden.',
   'quelle = Dokumentkennung plus Seite/Abschnitt. Ohne Beleg keine Zuordnung erzeugen.',
   'Mehrdeutige oder fehlende Schachtfolgen nicht ergänzen: Liste offener Fragen ausgeben.',
   'Nicht aus geografischer Nähe, Schacht-Namen, Luftlinie oder grafischen Kreuzungen',
   'automatisch eine physische Durchleitung ableiten. Keine Vermessungs-/Fachfreigabe behaupten.',
   'Bei nur vorhandenen Schacht-IDs und fehlenden Kabelverläufen alle Kabelzuordnungen offen lassen.',
   '',
   'Erwartete Schachtlisten-Spalten:',
   shaftHeaders.join(';'),
   '',
   'Die erzeugten Kabelzuordnungen anschließend in der Kabelsalat-400-Schacht-Auswertung',
   'einlesen; die drei Übergabe-CSV-Dateien dienen dem Abstimmungs-/Prüfprozess.'
  ].join('\n');
 }
 function fromProject(project){
  const shafts=(project?.network?.shafts||[]),routes=(project?.network?.routes||[]);
  const shaftCsv=rowsCsv(shafts.map(s=>({schacht_id:s.id,schachtname:s.name})),shaftHeaders);
  const passes=[];
  for(const route of routes)for(let i=0;i<(route.steps||[]).length;i++){
   const step=route.steps[i],shaft=shafts.find(s=>s.id===step.shaftId);
   if(!shaft)continue;
   passes.push({kabel_id:route.id,kabelname:route.name||route.id,
    schacht_id:step.shaftId,reihenfolge:i+1,
    quelle:'Kabelsalat-Netz: schematische Route '+route.id,status:'PLAN'});
  }
  return {shaftCsv,passageCsv:rowsCsv(passes,passHeaders)};
 }
 return {MAX_SHAFTS,MAX_PASSAGES,shaftHeaders,passHeaders,readCsv,rowsCsv,analyze,
  exportHandover,exportByShaft,exportOpen,fromProject,aiBrief};
});
