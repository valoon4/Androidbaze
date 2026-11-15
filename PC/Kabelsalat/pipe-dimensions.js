'use strict';
/* KAB-0880/0879/0881: auditable pipe dimensions, not an AI certification.
 * In old projects "dn" is a drawing/nominal diameter proxy, NEVER a measured bore.
 * A separate optional dimensionSpec preserves verified and candidate values.
 */
(function(root,factory){
 const api=factory();
 if(typeof module==='object'&&module.exports)module.exports=api;
 if(root)root.KabelsalatPipeDimensions=api;
})(typeof globalThis!=='undefined'?globalThis:this,function(){
 const RANGE=[20,600],STATUSES=['unverified','candidate','reviewed'];
 const clean=x=>typeof x==='string'?x.slice(0,240):'';
 const validMm=x=>Number.isFinite(Number(x))&&x!==null&&x!==''&&Number(x)>=RANGE[0]&&Number(x)<=RANGE[1]?
  Math.round(Number(x)*100)/100:null;
 function normalize(input,legacyDn){
  const raw=input&&typeof input==='object'?input:{};
  const diameter={
   nominalMm:validMm(raw.nominalMm)??validMm(legacyDn),
   innerMm:validMm(raw.innerMm),outerMm:validMm(raw.outerMm),
   openingMm:validMm(raw.openingMm),candidateMm:validMm(raw.candidateMm),
   unit:'mm',method:['manual','pdf-label','photo-calibrated','unknown'].includes(raw.method)?raw.method:'unknown',
   source:clean(raw.source),status:STATUSES.includes(raw.status)?raw.status:'unverified',
   reviewer:clean(raw.reviewer),reviewedAt:clean(raw.reviewedAt),
   confidence:Number.isFinite(raw.confidence)?Math.max(0,Math.min(1,raw.confidence)):null,
   note:clean(raw.note)
  };
  if(diameter.status==='reviewed'&&(!diameter.source||!diameter.reviewer||
     ![diameter.innerMm,diameter.outerMm,diameter.openingMm].some(Number.isFinite)))
   diameter.status='candidate';
  if(diameter.status==='candidate'&&!diameter.source)diameter.status='unverified';
  return diameter;
 }
 function forHole(h){
  return normalize(h?.dimensionSpec,h?.dn);
 }
 function apertureMm(h){
  const entry=forHole(h);
  // Explicit drawing/verified bore radius; legacy DN is a schematic fallback.
  return entry.openingMm??entry.nominalMm??70;
 }
 function describe(h){
  const d=forHole(h),qualified=d.status==='reviewed'?'fachlich dokumentiert':
   d.status==='candidate'?'Kandidat, nicht bestätigt':'schematisch / unbestätigt';
  const parts=[];
  if(d.nominalMm!==null)parts.push('DN '+d.nominalMm+' (Nennangabe)');
  if(d.innerMm!==null)parts.push('Innen-Ø '+d.innerMm+' mm');
  if(d.outerMm!==null)parts.push('Außen-Ø '+d.outerMm+' mm');
  if(d.openingMm!==null)parts.push('Öffnung-Ø '+d.openingMm+' mm');
  if(d.candidateMm!==null)parts.push('Erkannt: Ø '+d.candidateMm+' mm');
  return (parts.join(' · ')||'Durchmesser unbekannt')+' · '+qualified+
   (d.source?' · Quelle: '+d.source:'');
 }
 function parsePlanText(text,meta={}){
  const input=String(text||'').slice(0,100000);
  const source=clean(meta.source||'Eingefügter Plantext (ungeprüft)');
  const matches=[];
  const lines=input.split(/\r?\n/).slice(0,1500);
  for(let row=0;row<lines.length;row++){
   const line=lines[row].slice(0,800);
   const pipe=/\b(SW[1-4])\s*[/_\- ]*T(?:RASSE)?\s*0?(\d+)\s*[/_\- ]*LR\s*0?(\d+)\b/i.exec(line);
   const simple=/\b(SW[1-4])[_/\- ]*(?:LR|LEERROHR)\s*0?(\d+)\b/i.exec(line);
   const pipeCount=(line.match(/\bSW[1-4]\s*[/_\- ]*T(?:RASSE)?\s*0?\d+\s*[/_\- ]*LR\s*0?\d+\b/gi)||[]).length;
   const pipeId=pipe&&pipeCount===1?pipe[1].toUpperCase()+'_T'+Number(pipe[2])+'_LR'+String(+pipe[3]).padStart(2,'0'):null;
   const tokenRx=/(?:\bDN\b|\bDM\b|Ø|\bDURCHMESSER\b)\s*(?:[:=]\s*)?(\d{2,3}(?:[.,]\d)?)\s*(?:mm\b)?/giu;
   let found;
   while((found=tokenRx.exec(line))&&matches.length<600){
    const mm=validMm(found[1].replace(',','.'));
    if(mm===null)continue;
    matches.push({line:row+1,source,pipeId:pipeId||null,
     wall:pipe?pipe[1].toUpperCase():simple?simple[1].toUpperCase():null,
     loosePipeId:!pipeId&&simple?simple[1].toUpperCase()+'_LR'+String(+simple[2]).padStart(2,'0'):null,
     label:found[0].slice(0,65),candidateMm:mm,status:'candidate',
     confidence:null,reason:pipeId?'Textbeleg; exakte Rohr-ID prüfen':pipeCount>1?'Mehrere Rohr-IDs auf einer Zeile; Zuordnung offen':'Rohr-ID manuell zuordnen'});
   }
  }
  return matches;
 }
 // KAB-0881: bounded interface for already-extracted PDF text-layer pages.
 // No binary PDF parsing, OCR or spatial attribution is claimed here.
 function parsePlanPages(pages,{source='PDF-Textlayer (ungeprüft)',documentVersion=null,maxPages=32,maxChars=100000}={}){
  if(!Array.isArray(pages))throw Error('PDF-Seiten müssen eine Liste sein');
  source=clean(source);
  if(documentVersion!==null&&(typeof documentVersion!=='string'||documentVersion.length>120))
   throw Error('Ungültige PDF-Dokumentversion');
  if(!Number.isSafeInteger(maxPages)||maxPages<1||maxPages>100||
     !Number.isSafeInteger(maxChars)||maxChars<1||maxChars>1000000)
   throw Error('Ungültiges PDF-Verarbeitungsbudget');
  if(pages.length>maxPages)throw Error('PDF-Seitenlimit überschritten');
  let budget=0;const results=[];
  pages.forEach((page,i)=>{
   if(!page||typeof page.text!=='string')throw Error('PDF-Seite ohne gültigen Textlayer');
   const number=page.pageNumber===undefined?i+1:page.pageNumber;
   if(!Number.isSafeInteger(number)||number<1||number>100000)
    throw Error('Ungültige PDF-Seitennummer');
   const rotation=page.rotation===undefined?0:page.rotation;
   if(![0,90,180,270].includes(rotation))throw Error('Ungültige PDF-Seitenrotation');
   budget+=page.text.length;
   if(budget>maxChars)throw Error('PDF-Textbudget überschritten');
   const matches=parsePlanText(page.text,{source});
   results.push(...matches.map(m=>({...m,page:number,rotation,source,
    provenance:{source,page:number,line:m.line,rotation,documentVersion},
    scaleStatus:'not-provided',measurementBasis:'drawing-label',
    status:'candidate',reviewed:false})));
  });
  // Identical pipe IDs with conflicting labels across the supplied pages stay
  // unresolved; no automatic overwrite of real pipes or project JSON.
  const byId=new Map();
  for(const row of results)if(row.pipeId){
   const group=byId.get(row.pipeId)||[];group.push(row);byId.set(row.pipeId,group);
  }
  for(const group of byId.values()){
   const ambiguous=new Set(group.map(r=>r.candidateMm)).size>1;
   if(ambiguous)for(const row of group){row.conflict=true;row.reason='Widersprüchliche DN-Beschriftungen; manuell prüfen';}
  }
  return results;
 }
 function planCandidateTable(pages,options){
  return parsePlanPages(pages,options).sort((a,b)=>
   (a.pipeId||'~').localeCompare(b.pipeId||'~')||a.page-b.page||a.line-b.line);
 }
 return {RANGE,normalize,forHole,apertureMm,describe,parsePlanText,parsePlanPages,planCandidateTable};
});
