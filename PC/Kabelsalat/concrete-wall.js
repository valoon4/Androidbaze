'use strict';
/* KAB-0877: a wall is a single concrete solid with actual through-holes.
 * UI-only material/mesh: never changes source pipe positions or dimensions.
 * Note: nominal DN is still only a schematic hole-size proxy until surveyed.
 */
(function(root,factory){
 const api=factory();
 if(typeof module==='object'&&module.exports)module.exports=api;
 if(root)root.KabelsalatConcreteWall=api;
})(typeof globalThis!=='undefined'?globalThis:this,function(){
 function build(THREE,{width,height,depth,openings,opacity=1}){
  if(!THREE||!Number.isFinite(width)||width<=0||
    !Number.isFinite(height)||height<=0||
    !Number.isFinite(depth)||depth<=0||!Array.isArray(openings))
   throw Error('Ungültige Schachtwand-Geometrie');
  const half=width/2;
  const shape=new THREE.Shape();
  shape.moveTo(-half,0);
  shape.lineTo(half,0);
  shape.lineTo(half,height);
  shape.lineTo(-half,height);
  shape.closePath();
  const accepted=[],excluded=[];
  for(const raw of openings){
   const {u,y,r,id}=raw;
   if(!Number.isFinite(u)||!Number.isFinite(y)||!Number.isFinite(r)||r<=0||
     u-r< -half||u+r>half||y-r<0||y+r>height){
    excluded.push({id,reason:'outside'});continue;
   }
   if(accepted.some(v=>Math.hypot(v.u-u,v.y-y)<v.r+r)){
    excluded.push({id,reason:'overlap'});continue;
   }
   const aperture=new THREE.Path();
   aperture.absarc(u,y,r,0,Math.PI*2,true);
   shape.holes.push(aperture);
   accepted.push({id,u,y,r});
  }
  // Material 0: concrete faces; material 1: cut walls (dark bore/reveal).
  // ExtrudeGeometry creates a continuous wall with truly empty apertures.
  const alpha=Math.max(0,Math.min(1,opacity));
  const shared={roughness:.96,metalness:0,side:THREE.DoubleSide,
   transparent:alpha<.995,opacity:alpha,depthWrite:alpha>=.995};
  const materials=[
   new THREE.MeshStandardMaterial({...shared,color:'#858d90'}),
   new THREE.MeshStandardMaterial({...shared,color:'#5b676b'})
  ];
  const geometry=new THREE.ExtrudeGeometry(shape,{
   depth,bevelEnabled:false,curveSegments:32,steps:1
  });
  const mesh=new THREE.Mesh(geometry,materials);
  return {mesh,accepted,excluded};
 }
 return {build};
});
