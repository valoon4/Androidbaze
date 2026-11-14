'use strict';
/* KAB-0795: Source/geometry review gates. Never infer AVL0116 compliance,
 * A/B construction from 75/100-mm historical conflicts, or actual bore DN.
 */
(function(root,factory){
 const api=factory();
 if(typeof module==='object'&&module.exports)module.exports=api;
 if(root)root.KabelsalatTechnicalReview=api;
})(typeof globalThis!=='undefined'?globalThis:this,function(){
 const own=(o,k)=>!!o&&Object.prototype.hasOwnProperty.call(o,k);
 const source=src=>src&&typeof src==='object'&&
  typeof src.document==='string'&&src.document.trim()&&
  typeof src.version==='string'&&src.version.trim()&&
  typeof src.page==='string'&&src.page.trim()?
   {document:src.document.trim().slice(0,160),
    version:src.version.trim().slice(0,80),page:src.page.trim().slice(0,80)}:null;
 function review(input){
  if(!input||typeof input!=='object')throw Error('Prüfeingabe muss Objekt sein');
  const shaftId=typeof input.shaftId==='string'&&input.shaftId.trim()?input.shaftId.trim():null;
  const wallId=['SW1','SW2','SW3','SW4'].includes(input.wallId)?input.wallId:null;
  const original=source(input.source);
  const undersideMm=typeof input.undersideMm==='number'&&Number.isFinite(input.undersideMm)&&
   input.undersideMm>=0&&input.undersideMm<=12000?input.undersideMm:null;
  const openingMm=typeof input.openingMm==='number'&&Number.isFinite(input.openingMm)&&
   input.openingMm>0&&input.openingMm<=1000?input.openingMm:null;
  const conflicts=Array.isArray(input.conflictingThresholdsMm)?
   [...new Set(input.conflictingThresholdsMm.filter(x=>x===75||x===100))].sort((a,b)=>a-b):[];
  const notes=[];
  if(!shaftId)notes.push('Schacht-ID fehlt');
  if(!wallId)notes.push('Wand-ID fehlt oder unbekannt');
  if(!original)notes.push('Originalplan, Version oder Blattstelle fehlen');
  if(undersideMm===null)notes.push('Unterkante der Rohre über Schachtboden: unbekannt');
  if(openingMm===null)notes.push('Tatsächlich belegter Bohrungs-/Rohrdurchmesser: unbekannt');
  if(conflicts.length>1)notes.push('Widerspruch 75/100 mm: keine Regel wählen');
  // Even with every numeric input, a product model is not an external signoff.
  notes.push('Fachliche Abnahme und anwendbarer Normwortlaut extern zu bestätigen');
  const evidence=original?{...original,shaftId,wallId,sourceVerified:false}:null;
  return Object.freeze({
   shaftId,wallId,source:evidence,undersideMm,openingMm,
   conflictingThresholdsMm:conflicts,
   variant:null,ruleMm:null,compliance:null,
   status:'offen',passed:false,approved:false,
   sourceVerified:false,requiresExternalReview:true,
   notes,neutralFinding:notes.join('; '),
   synthetic:input.synthetic===true
  });
 }
 function summary(result){
  if(!result||result.status!=='offen'||result.passed!==false)
   throw Error('Nur neutraler offener Prüfbefund exportierbar');
  const id=String(result.shaftId||'?').replace(/[<>]/g,'');
  return 'Schacht '+id+': NACHWEIS OFFEN. '+result.neutralFinding+
   '. Keine automatisch bestätigte AVL-/Bau-/Brandschutzfreigabe.';
 }
 return {source,review,summary};
});
