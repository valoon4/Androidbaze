'use strict';
/* KAB-0832 / KAB-0037/0038/0039
 * Source PDF circle centers are document-derived, not surveyed positions.
 * Old v2.0.1 saved projects had those same centers compacted by 62% in u/v.
 * Migrate ONLY untouched, exactly matching old demo coordinates. Edits survive.
 */
(function(root,factory){
 const api=factory();
 if(typeof module==='object'&&module.exports)module.exports=api;
 if(root)root.KabelsalatSchematicPositions=api;
})(typeof globalThis!=='undefined'?globalThis:this,function(){
 const FACTOR=.62,round=v=>Math.round(v*1e5)/1e5;
 const valid=v=>Number.isFinite(v)&&v>=0&&v<=1;
 const source=h=>h&&valid(h.sourceU)&&valid(h.sourceV);
 const original=h=>source(h)&&Math.abs(h.u-h.sourceU)<1e-7&&Math.abs(h.v-h.sourceV)<1e-7;
 function compact(v){return round(.5+(v-.5)*FACTOR);}
 function legacy(h){
  return source(h)&&Math.abs(h.u-compact(h.sourceU))<.000001&&
   Math.abs(h.v-compact(h.sourceV))<.000001&&!original(h);
 }
 function migrate(h,pdf){
  if(!pdf||!legacy(h))return h;
  // No in-place mutation, all cable endpoints continue pointing to the same pipe ID.
  return {...h,u:h.sourceU,v:h.sourceV};
 }
 function display(h,compactView){
  // Compaction is *only* a viewing preference for unchanged PDF source data.
  // User-moved holes may not be silently re-centered.
  if(!compactView||!source(h)||!original(h))return h;
  return {...h,u:compact(h.sourceU),v:compact(h.sourceV)};
 }
 function transformProjectShunt(network){
  // Normalize historic snapshots too: switching shafts must not reinstate compact data.
  if(!network?.shafts)return network;
  for(const entry of network.shafts){
   const snap=entry.snapshot;
   if(!snap?.sourceInfo?.sourceVersion?.startsWith('PDF V2'))continue;
   for(const wall of snap.walls||[])
    if(Array.isArray(wall.holes))wall.holes=wall.holes.map(h=>migrate(h,true));
  }
  return network;
 }
 return {FACTOR,compact,legacy,migrate,display,transformProjectShunt};
});
