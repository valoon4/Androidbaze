'use strict';
/* KAB-0203…0206: source-exact PDF wall against view-only 62% pitch preview.
   Side-by-side comparisons preserve every source pipe ID and source U/V.
   NOT a survey, measured dimension, plan correction, or approval. */
(function(root,factory){
 const api=factory();
 if(typeof module==='object'&&module.exports)module.exports=api;
 if(root)root.KabelsalatPitchWalls=api;
})(typeof globalThis!=='undefined'?globalThis:this,function(){
 const WALLS=['SW1','SW2','SW3','SW4'],COUNTS=[29,13,26,23];
 const clamp=x=>Math.max(0,Math.min(1,x));
 function compareWall(project,wallId,positions){
  if(!project?.sourceInfo?.sourceVersion?.startsWith('PDF V2')||
     project.sourceInfo?.shaft!=='13_U_028')throw Error('Nur die bekannte PDF-V2-Referenz 13_U_028 ist zugelassen');
  if(!positions||positions.FACTOR!==.62||typeof positions.display!=='function')
   throw Error('Verifizierter 62-Prozent-Vorschaumodus fehlt');
  const index=WALLS.indexOf(wallId);
  if(index<0)throw Error('Unbekannte Schachtwand '+wallId);
  const wall=project.walls?.find(w=>w.id===wallId);
  if(!wall||!Array.isArray(wall.holes)||wall.holes.length!==COUNTS[index])
   throw Error('Quelle unvollständig: erwartete Rohrzahl für '+wallId+' ist '+COUNTS[index]);
  const ids=new Set(),source=[],demo=[];
  for(const h of wall.holes){
   if(!h||typeof h.id!=='string'||!h.id.startsWith(wallId+'_')||ids.has(h.id)||
      !Number.isFinite(h.sourceU)||!Number.isFinite(h.sourceV)||
      !Number.isFinite(h.u)||!Number.isFinite(h.v)||
      [h.sourceU,h.sourceV,h.u,h.v].some(v=>v<0||v>1))
    throw Error('Ungültige oder doppelte Rohr-ID/Position auf '+wallId);
   ids.add(h.id);
   // Reject modified or already compacted source before presenting it as exact original.
   if(Math.abs(h.u-h.sourceU)>1e-7||Math.abs(h.v-h.sourceV)>1e-7)
    throw Error('Veränderte PDF-Quelle; kein Originalvergleich für '+h.id);
   const p=positions.display(h,true);
   if(p===h||!Number.isFinite(p.u)||!Number.isFinite(p.v)||p.id!==h.id)
    throw Error('Vorschau-Datensatz nicht unabhängig prüfbar: '+h.id);
   source.push({id:h.id,u:h.sourceU,v:h.sourceV,sourcePage:h.sourcePage??null,
    sourceAddress:String(h.sourceAddress||'').slice(0,100)});
   demo.push({id:h.id,u:p.u,v:p.v,sourcePage:h.sourcePage??null,
    sourceAddress:String(h.sourceAddress||'').slice(0,100)});
  }
  const sort=(a,b)=>a.id.localeCompare(b.id,'de',{numeric:true});
  source.sort(sort);demo.sort(sort);
  if(source.some((a,i)=>a.id!==demo[i].id))throw Error('Vorschau-ID nicht originalgetreu');
  return {wallId,source,demo,count:source.length,sourceType:'PDF V2 · Quellmarker (keine Vermessung)',
   demoType:'62% rein visuelle Verdichtung · keine neuen Rohrpositionen',factor:.62};
 }
 function compareAll(project,positions){
  const result=WALLS.map(id=>compareWall(project,id,positions));
  const ids=new Set(result.flatMap(x=>x.source.map(y=>y.id)));
  if(ids.size!==91||result.reduce((sum,x)=>sum+x.count,0)!==91)
   throw Error('PDF-Referenz muss genau 91 eindeutige LR-Marker enthalten');
  return result;
 }
 function escapeXML(value){
  return String(value??'').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;')
   .replace(/"/g,'&quot;').replace(/'/g,'&#39;');
 }
 function svg(records,{wallId,preview=false}={}){
  if(!Array.isArray(records)||records.length>40)throw Error('Ungültige Pitch-Rohrliste');
  const viewW=420,viewH=240,pad=24;
  const points=records.map(h=>{
   if(!h||!Number.isFinite(h.u)||!Number.isFinite(h.v)||h.u<0||h.u>1||h.v<0||h.v>1)
    throw Error('Ungültiger SVG-Rohrmarker');
   const x=(pad+(viewW-pad*2)*clamp(h.u)).toFixed(2),
    y=(viewH-pad-(viewH-pad*2)*clamp(h.v)).toFixed(2);
   // Constant radius because *no* actual diameter of a PDF marker is confirmed.
   return '<circle cx="'+x+'" cy="'+y+'" r="4.5" fill="'+(preview?'#1b9b8b':'#54758b')+
    '" stroke="'+(preview?'#075749':'#244457')+'" stroke-width="1.5"><title>'+
    escapeXML(h.id)+' · '+escapeXML(h.sourceAddress)+' · '+(preview?'62% Schema':'PDF-V2-Quelle')+
    ' · NICHT vermessen</title></circle>';
  });
  return '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 '+viewW+' '+viewH+
   '" role="img" aria-label="'+escapeXML(wallId)+' '+(preview?'62%-Vorschau':'PDF-V2-Originalmarker')+'">'+
   '<rect x="1" y="1" width="418" height="238" rx="12" fill="#f3f8fa" stroke="#bdd1d8"/>'+
   '<rect x="24" y="24" width="372" height="192" rx="3" fill="#fff" stroke="#91acb5" stroke-width="2"/>'+
   '<path d="M210 24V216M24 120H396" stroke="#cedde2" stroke-dasharray="4 5"/>'+
   points.join('')+'</svg>';
 }
 return {WALLS,COUNTS,compareWall,compareAll,svg};
});
