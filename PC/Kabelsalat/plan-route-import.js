'use strict';
/* Bro 400: local, conservative plan-text extraction with reviewed route candidates.
 * Digitally generated simple PDFs with readable text operators are supported.
 * No OCR, no trained AI, no inferred topology, no server uploads.
 */
(function(root,factory){
 const api=factory();
 if(typeof module==='object'&&module.exports)module.exports=api;
 if(root)root.KabelsalatPlanRoutes=api;
})(typeof window!=='undefined'?window:null,function(){
 const MAX_FILE=8*1024*1024,MAX_PAGES=250,MAX_LINES=120000,MAX_CANDIDATES=25000;
 const clean=s=>String(s??'').trim(),upper=s=>clean(s).toLocaleUpperCase('de-DE');
 const idOk=s=>/^[A-Z0-9][A-Z0-9_.:/-]{0,119}$/.test(s);
 function bytesToLatin(bytes){
  const parts=[];
  for(let i=0;i<bytes.length;i+=16384)
   parts.push(String.fromCharCode.apply(null,bytes.subarray(i,i+16384)));
  return parts.join('');
 }
 function literalString(token){
  let body=token.slice(1,-1);
  let out='';
  for(let i=0;i<body.length;i++){
   if(body[i]!=='\\'){out+=body[i];continue;}
   const c=body[++i];if(c===undefined)break;
   if(c==='\n')continue;
   if(c==='\r'){if(body[i+1]==='\n')i++;continue;}
   if(/[0-7]/.test(c)){
    let oct=c;
    while(oct.length<3&&/[0-7]/.test(body[i+1]||''))oct+=body[++i];
    out+=String.fromCharCode(parseInt(oct,8));continue;
   }
   out+=({n:'\n',r:'\r',t:'\t',b:'\b',f:'\f'})[c]??c;
  }
  return out;
 }
 function pdfToken(token){
  if(token.startsWith('('))return literalString(token);
  const hex=token.slice(1,-1).replace(/\s+/g,'');
  if(!/^[a-f\d]*$/i.test(hex))return '';
  const raw=Array.from({length:Math.ceil(hex.length/2)},(_,i)=>
   parseInt((hex.slice(i*2,i*2+2)+'0').slice(0,2),16));
  if(raw[0]===254&&raw[1]===255){
   let s='';for(let i=2;i+1<raw.length;i+=2)s+=String.fromCharCode((raw[i]<<8)|raw[i+1]);
   return s;
  }
  return String.fromCharCode(...raw);
 }
 function textOperators(stream){
  const lines=[];
  for(const block of stream.matchAll(/\bBT\b([\s\S]*?)\bET\b/g)){
   const commands=block[1];
   const txt=[];
   const re=/(\((?:\\[\s\S]|[^\\)])*\)|<[\da-fA-F\s]*>)\s*Tj\b|\[([\s\S]*?)\]\s*TJ\b/g;
   let m;
   while((m=re.exec(commands))){
    if(m[1])txt.push(pdfToken(m[1]));
    else {
     const parts=[...m[2].matchAll(/\((?:\\[\s\S]|[^\\)])*\)|<[\da-fA-F\s]*>/g)]
      .map(x=>pdfToken(x[0]));
     txt.push(parts.join(''));
    }
   }
   if(txt.length)lines.push(txt.join(' ').trim());
  }
  return lines.join('\n');
 }
 async function inflate(bytes){
  if(typeof DecompressionStream!=='undefined'){
   const stream=new Blob([bytes]).stream().pipeThrough(new DecompressionStream('deflate'));
   const array=new Uint8Array(await new Response(stream).arrayBuffer());
   if(array.length>4*1024*1024)throw Error('PDF-Textstrom überschreitet 4 MB');
   return array;
  }
  if(typeof require==='function'){
   const z=require('node:zlib');
   return z.inflateSync(Buffer.from(bytes),{maxOutputLength:4*1024*1024});
  }
  throw Error('Dieser Browser unterstützt keine Deflate-Dekomprimierung');
 }
 function objRefs(src){return [...src.matchAll(/(\d+)\s+\d+\s+R/g)].map(x=>Number(x[1]));}
 async function pdfPages(bytes,name='plan.pdf'){
  if(!(bytes instanceof Uint8Array))bytes=new Uint8Array(bytes);
  if(bytes.length>MAX_FILE)throw Error('PDF größer als 8 MB');
  const pdf=bytesToLatin(bytes);
  if(!pdf.startsWith('%PDF-'))throw Error('Datei enthält keinen gültigen PDF-Header');
  if(/\/Encrypt\b/.test(pdf))throw Error('Verschlüsseltes PDF: zuerst eine lesbare Originalkopie bereitstellen');
  const objects=new Map();
  const objectRegex=/(\d+)\s+\d+\s+obj\b([\s\S]*?)\bendobj\b/g;
  let match;
  while((match=objectRegex.exec(pdf)))objects.set(Number(match[1]),match[2]);
  const catalog=[...objects.entries()].find(([,s])=>/\/Type\s*\/Catalog\b/.test(s));
  if(!catalog)throw Error('PDF-Seitenbaum nicht lesbar');
  const root=/\/Pages\s+(\d+)\s+\d+\s+R\b/.exec(catalog[1]);
  if(!root)throw Error('PDF-Seitenbaum fehlt');
  const ordered=[],visited=new Set();
  function walk(id,depth){
   if(depth>40||visited.has(id))throw Error('PDF-Seitenbaum ungültig oder zyklisch');
   visited.add(id);
   const obj=objects.get(id);
   if(!obj)throw Error('PDF-Seitenobjekt '+id+' fehlt');
   if(/\/Type\s*\/Page\b(?!s)/.test(obj)){ordered.push({id,obj});return;}
   if(!/\/Type\s*\/Pages\b/.test(obj))throw Error('PDF-Knoten ist keine Seite');
   const kids=/\/Kids\s*\[([\s\S]*?)\]/.exec(obj);
   if(!kids)throw Error('PDF-Kindseiten nicht auflösbar');
   for(const ref of objRefs(kids[1]))walk(ref,depth+1);
  }
  walk(Number(root[1]),0);
  if(ordered.length>MAX_PAGES)throw Error('Mehr als '+MAX_PAGES+' PDF-Seiten');
  if(!ordered.length)throw Error('PDF enthält keine lesbaren Seitenobjekte');
  const pages=[],warnings=[];
  for(let i=0;i<ordered.length;i++){
   const {obj}=ordered[i],contents=/\/Contents\s*(\[[\s\S]*?\]|\d+\s+\d+\s+R)/.exec(obj);
   const refs=contents?objRefs(contents[1]):[];
   if(!refs.length){warnings.push('Seite '+(i+1)+': kein referenzierter Textstrom');continue;}
   const texts=[];
   for(const ref of refs){
    const streamObj=objects.get(ref)||'';
    const sm=/\bstream\r?\n/.exec(streamObj),end=sm?streamObj.indexOf('endstream',sm.index+sm[0].length):-1;
    if(!sm||end<0){warnings.push('Seite '+(i+1)+': Textstrom '+ref+' nicht lesbar');continue;}
    const prefix=streamObj.slice(0,sm.index);
    if(/\/Filter\b/.test(prefix)&&!/\/FlateDecode\b/.test(prefix)){
     warnings.push('Seite '+(i+1)+': nicht unterstützter PDF-Filter');continue;
    }
    if(/\/Filter\s*\[/.test(prefix)&&/\/(?:ASCII85Decode|LZWDecode|Crypt|RunLengthDecode)\b/.test(prefix)){
     warnings.push('Seite '+(i+1)+': Filterkette nicht unterstützt');continue;
    }
    const bytes0=Uint8Array.from(streamObj.slice(sm.index+sm[0].length,end).replace(/\r?\n$/,''),
     c=>c.charCodeAt(0));
    try{
     const raw=/\/FlateDecode\b/.test(prefix)?await inflate(bytes0):bytes0;
     const text=textOperators(bytesToLatin(raw));
     if(text)texts.push(text);
    }catch(e){warnings.push('Seite '+(i+1)+': Textdekodierung fehlgeschlagen ('+clean(e.message).slice(0,80)+')');}
   }
   if(texts.length)pages.push({name,page:i+1,text:texts.join('\n')});
   else warnings.push('Seite '+(i+1)+': keine extrahierbare Textebene');
  }
  if(!pages.length)throw Error('Keine extrahierbare PDF-Textebene. Scan/OCR oder komplexe PDF-Schriftkodierung derzeit nicht unterstützt');
  return {pages,warnings,totalPages:ordered.length};
 }
 async function readFile(file){
  if(!file||!file.name)throw Error('Plandatei fehlt');
  if(file.size>MAX_FILE)throw Error(file.name+': maximal 8 MB');
  const name=clean(file.name).slice(0,100);
  if(/\.pdf$/i.test(name))return pdfPages(new Uint8Array(await file.arrayBuffer()),name);
  if(!/\.(txt|csv|md)$/i.test(name))throw Error(name+': nur PDF, TXT, CSV und MD');
  const text=await file.text();
  return {pages:[{name,page:null,text}],warnings:[],totalPages:1};
 }
 function extract(shaftRows,documents){
  const shafts=new Map();
  for(const item of shaftRows||[]){
   const id=upper(item.schacht_id);
   if(!idOk(id)||shafts.has(id))throw Error('Schacht-IDs fehlen oder sind doppelt');
   shafts.set(id,clean(item.schachtname));
  }
  if(!shafts.size||shafts.size>500)throw Error('1–500 Schacht-IDs erforderlich');
  const candidates=[],issues=[],seen=new Map();
  let seenLines=0;
  for(const doc of documents){
   const name=clean(doc.name).slice(0,100)||'Textplan';
   const page=Number.isInteger(doc.page)&&doc.page>0?' S. '+doc.page:'';
   const lines=String(doc.text||'').split(/\r?\n/);
   if(lines.length>MAX_LINES)throw Error('Zu viele Planzeilen');
   for(let i=0;i<lines.length;i++){
    if(++seenLines>MAX_LINES)throw Error('Mehr als '+MAX_LINES+' Planzeilen');
    const line=lines[i].trim();
    if(!line||line.length>1800)continue;
    let m=/^(?:Kabel(?:weg|-ID)?\s*[:#]?\s*)([A-Za-z0-9][A-Za-z0-9_.:/-]{0,119})\s*[:;|=]\s*(.+)$/i.exec(line);
    if(!m)m=/^([Kk][A-Za-z0-9_.:/-]{0,119})\s*[:;|]\s*(.+)$/.exec(line);
    if(!m)continue;
    const cable=upper(m[1]),source=name+page+' Z. '+(i+1);
    if(!idOk(cable)||shafts.has(cable)){
     issues.push({source,reason:'Kabel-ID fehlt oder ist mit einer Schacht-ID identisch',snippet:line.slice(0,160)});continue;
    }
    const tail=m[2];
    if(!/(?:-|=)?>|→|⇒|;/.test(tail)){
     issues.push({source,reason:'Keine explizite Schacht-Reihenfolge (Pfeile oder Semikolon) erkennbar',snippet:line.slice(0,160)});continue;
    }
    const steps=tail.split(/\s*(?:--?>|=>|→|⇒|>|;)\s*/).map(x=>x.trim()).filter(Boolean)
     .map(x=>x.replace(/^Schacht\s+/i,'').trim());
    if(!steps.length||steps.length>500||steps.some(x=>!idOk(upper(x))||!shafts.has(upper(x)))){
     issues.push({source,reason:'Unbekannte oder nicht eindeutig abgegrenzte Schacht-ID in der angegebenen Folge',snippet:line.slice(0,160)});continue;
    }
    const ids=steps.map(upper);
    if(new Set(ids).size!==ids.length){
     issues.push({source,reason:'Schacht in derselben Kabelroute mehrfach genannt',snippet:line.slice(0,160)});continue;
    }
    const signature=ids.join('>');
    const duplicate=seen.get(cable+'|'+signature);
    if(duplicate){duplicate.sources.push(source);continue;}
    const candidate={key:'r'+candidates.length,cableId:cable,cableName:cable,
     shaftIds:ids,source,sources:[source],status:'KANDIDAT',conflict:false,snippet:line.slice(0,160)};
    candidates.push(candidate);seen.set(cable+'|'+signature,candidate);
    if(candidates.length>MAX_CANDIDATES)throw Error('Mehr als '+MAX_CANDIDATES+' Kandidaten');
   }
  }
  const grouped=new Map();
  for(const c of candidates){if(!grouped.has(c.cableId))grouped.set(c.cableId,[]);grouped.get(c.cableId).push(c);}
  for(const [id,group] of grouped)if(group.length>1){
   for(const c of group)c.conflict=true;
   issues.push({source:group.map(x=>x.source).join(' | ').slice(0,220),
    reason:'Widersprüchliche/teilweise Kabelrouten für '+id+'; nicht automatisch verknüpfen',snippet:''});
  }
  return {candidates,issues,summary:{candidates:candidates.length,
   routesWithConflicts:[...grouped.values()].filter(x=>x.length>1).length,
   examinedLines:seenLines,physicallyVerified:0}};
 }
 function compile(handover,shaftCsv,existingCsv,selected){
  if(!handover||!selected?.length)throw Error('Keine ausgewählten Plan-Kandidaten');
  const current=clean(existingCsv)?handover.readCsv(existingCsv,handover.passHeaders,25000):[];
  const existingIds=new Set(current.map(x=>upper(x.kabel_id)));
  const chosen=new Set(),rows=[];
  for(const candidate of selected){
   if(existingIds.has(candidate.cableId))throw Error('Kabel '+candidate.cableId+
    ' besitzt bereits Zuordnungen; manuelle Konfliktklärung erforderlich');
   if(chosen.has(candidate.cableId))throw Error('Für Kabel '+candidate.cableId+
    ' wurden mehrere unterschiedliche Routen ausgewählt');
   chosen.add(candidate.cableId);
   for(let i=0;i<candidate.shaftIds.length;i++)
    rows.push({kabel_id:candidate.cableId,kabelname:candidate.cableName,
     schacht_id:candidate.shaftIds[i],reihenfolge:i+1,
     quelle:candidate.source,status:'KANDIDAT'});
  }
  const all=current.concat(rows);
  if(all.length>25000)throw Error('Mehr als 25.000 Kabel-Schacht-Zuordnungen');
  const csv=handover.rowsCsv(all,handover.passHeaders);
  const report=handover.analyze(shaftCsv,csv);
  if(!report.ok)throw Error('Importkonflikt: '+report.errors.slice(0,4).join(' · '));
  return {csv,report,added:rows.length};
 }
 function issuesCsv(handover,issues){
  return handover.rowsCsv(issues.map(x=>({quelle:x.source,problem:x.reason,
   originalzeile:x.snippet,status:'offen'})),
   ['quelle','problem','originalzeile','status']);
 }
 return {MAX_FILE,readFile,pdfPages,textOperators,extract,compile,issuesCsv};
});
