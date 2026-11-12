'use strict';
/* KAB-0882: conservative local/offline visual circle proposals.
 * NOT trained AI / NOT a calibrated physical measurement or OCR.
 * Never infer absolute mm without an explicit known reference diameter.
 */
(function(root,factory){
 const api=factory();
 if(typeof module==='object'&&module.exports)module.exports=api;
 if(root)root.KabelsalatPhotoCandidates=api;
})(typeof globalThis!=='undefined'?globalThis:this,function(){
 function detect(pixels,width,height,{threshold=null,minDiameter=10,maxCandidates=80}={}){
  if(!pixels||!Number.isSafeInteger(width)||!Number.isSafeInteger(height)||
    width<8||height<8||width*height>4000000||pixels.length<width*height*4)
   throw Error('Ungültiges Bild: maximal 4 Megapixel Analysefläche');
  const n=width*height,bright=new Uint8Array(n);let mean=0;
  for(let i=0;i<n;i++){
   const k=i*4,alpha=pixels[k+3]/255;
   const l=Math.round((pixels[k]*.2126+pixels[k+1]*.7152+pixels[k+2]*.0722)*alpha+255*(1-alpha));
   bright[i]=l;mean+=l;
  }
  mean/=n;
  const cutoff=Number.isFinite(threshold)?Math.max(0,Math.min(255,threshold)):
   Math.max(24,Math.min(170,Math.round(mean*.65)));
  const seen=new Uint8Array(n),candidate=[];
  for(let i=0;i<n;i++){
   if(seen[i]||bright[i]>cutoff)continue;
   const stack=[i];seen[i]=1;let count=0,minX=width,minY=height,maxX=0,maxY=0, sx=0,sy=0;
   while(stack.length){
    const j=stack.pop(),x=j%width,y=Math.floor(j/width);
    count++;sx+=x;sy+=y;
    minX=Math.min(minX,x);maxX=Math.max(maxX,x);
    minY=Math.min(minY,y);maxY=Math.max(maxY,y);
    const next=[x>0?j-1:-1,x+1<width?j+1:-1,y>0?j-width:-1,y+1<height?j+width:-1];
    for(const pos of next)if(pos>=0&&!seen[pos]&&bright[pos]<=cutoff){seen[pos]=1;stack.push(pos);}
   }
   const w=maxX-minX+1,h=maxY-minY+1,diameter=Math.max(w,h),
    ratio=Math.min(w,h)/Math.max(w,h),fill=count/(Math.PI*diameter*diameter/4);
   if(diameter<minDiameter||ratio<.70||fill<.27||fill>1.25||
     diameter>Math.min(width,height)*.92||count<30)continue;
   candidate.push({x:Math.round(sx/count*10)/10,y:Math.round(sy/count*10)/10,
    diameterPx:Math.round((w+h)/2*10)/10,
    relativeSize:'unbestimmt',quality:Math.round(Math.max(0,1-Math.abs(fill-.78))*ratio*100)/100,
    status:'candidate',method:'image-contour-unverified'});
  }
  candidate.sort((a,b)=>b.diameterPx-a.diameterPx||a.y-b.y||a.x-b.x);
  const max=candidate[0]?.diameterPx||1;
  return {threshold:cutoff,meanBrightness:Math.round(mean),candidates:candidate.slice(0,maxCandidates).map((x,i)=>({
   ...x,index:i,relativeSize:x.diameterPx/max>.8?'groß':x.diameterPx/max>.5?'mittel':'klein'})),
   note:'Bildbasierte Kreis-Kandidaten, keine KI und keine verifizierten Rohrmaße.'};
 }
 function calibrate(candidates,index,referenceMm){
  const mm=Number(referenceMm),selected=candidates?.[index];
  if(!selected||!Number.isFinite(mm)||mm<20||mm>600||selected.diameterPx<=0)
   throw Error('Bekannter Referenzkreis und geprüfter Ø 20–600 mm erforderlich');
  const factor=mm/selected.diameterPx;
  return candidates.map(c=>({...c,candidateMm:Math.round(c.diameterPx*factor*10)/10,
   status:'candidate',method:'photo-calibrated',
   note:'Nur perspektivische Näherung: Referenz-Ø '+mm+' mm. Manuell prüfen.'}));
 }
 return {detect,calibrate};
});
