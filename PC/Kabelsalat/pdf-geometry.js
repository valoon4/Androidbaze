'use strict';
/* Pure PDF-V2 shaft-world mapping (meters).
   AvL0116 p.4: SW1 Stirnwand z-, SW2 right x+, SW3 opposite z+,
   SW4 left x-. No real survey distances are inferred here. */
(function(root,factory){
 const api=factory();
 if(typeof module==='object'&&module.exports)module.exports=api;
 if(root)root.KabelsalatPdfGeometry=api;
})(typeof globalThis!=='undefined'?globalThis:this,function(){
 const WALLS=Object.freeze({
  SW1:{role:'Stirnwand',rotationY:0,normal:[0,0,1],azimuth:Math.PI},
  SW2:{role:'Rechte Wand',rotationY:-Math.PI/2,normal:[-1,0,0],azimuth:Math.PI/2},
  SW3:{role:'Gegenüber SW1',rotationY:Math.PI,normal:[0,0,-1],azimuth:0},
  SW4:{role:'Linke Wand',rotationY:Math.PI/2,normal:[1,0,0],azimuth:-Math.PI/2}
 });
 const clamp=v=>Math.max(0,Math.min(1,Number(v)||0));
 function wall(id){const w=WALLS[id];if(!w)throw Error('Unbekannte PDF-Wand '+id);return w;}
 function span(id,s){wall(id);return (id==='SW1'||id==='SW3')?s.w:s.d;}
 function hole(id,h,s){
  wall(id);const u=clamp(h.u),y=s.h*clamp(h.v);
  switch(id){
   case 'SW1':return [-s.w/2+s.w*u,y,-s.d/2+.024];
   case 'SW2':return [s.w/2-.024,y,-s.d/2+s.d*u];
   case 'SW3':return [s.w/2-s.w*u,y,s.d/2-.024];
   default:return [-s.w/2+.024,y,s.d/2-s.d*u];
  }
 }
 function transform(id,s){
  const basis=wall(id),position={
   SW1:[0,0,-s.d/2],SW2:[s.w/2,0,0],
   SW3:[0,0,s.d/2],SW4:[-s.w/2,0,0]
  }[id];
  return {position,rotationY:basis.rotationY,span:span(id,s)};
 }
 function inverse(id,p,s){
  wall(id);const x=p[0],z=p[2];
  switch(id){
   case 'SW1':return clamp(x/s.w+.5);
   case 'SW2':return clamp(z/s.d+.5);
   case 'SW3':return clamp(.5-x/s.w);
   default:return clamp(.5-z/s.d);
  }
 }
 function along(id,p,s){
  return inverse(id,p,s)*span(id,s);
 }
 function camera(id,s){
  wall(id);
  const h=s.h/2;
  const pos={
   SW1:[0,h,s.d/2+4],SW2:[-s.w/2-4,h,0],
   SW3:[0,h,-s.d/2-4],SW4:[s.w/2+4,h,0]
  }[id];
  const target={
   SW1:[0,h,-s.d/2],SW2:[s.w/2,h,0],
   SW3:[0,h,s.d/2],SW4:[-s.w/2,h,0]
  }[id];
  return {position:pos,target,span:span(id,s)};
 }
 function tangentOffset(id,angle,r){
  wall(id);const rad=Math.max(0,Number(r)||0);
  return id==='SW1'||id==='SW3'?
   [Math.cos(angle)*rad,Math.sin(angle)*rad,0]:
   [0,Math.sin(angle)*rad,Math.cos(angle)*rad];
 }
 return {WALLS,span,hole,transform,inverse,along,camera,wall,tangentOffset};
});
