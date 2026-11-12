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
 // Deterministic speckled aggregate. No network texture and no saved project data.
 // Material represents a plausible demo appearance, not a certified concrete finish.
 function texture(THREE,width,height){
  if(!THREE.DataTexture)return null; // headless test stub
  const side=128,pixels=new Uint8Array(side*side*4);
  let seed=0x4b1d32e5;
  for(let y=0;y<side;y++)for(let x=0;x<side;x++){
   seed=(Math.imul(seed,1664525)+1013904223)>>>0;
   const noise=(seed>>>24)-128;
   const vein=Math.sin(x*.18+y*.055)*5+Math.cos(y*.14-x*.1)*4;
   const grain=Math.max(100,Math.min(211,158+noise*.21+vein));
   const i=(y*side+x)*4;
   pixels[i]=Math.round(grain*.99);pixels[i+1]=Math.round(grain);
   pixels[i+2]=Math.round(grain*.97);pixels[i+3]=255;
  }
  const map=new THREE.DataTexture(pixels,side,side,THREE.RGBAFormat);
  map.wrapS=THREE.RepeatWrapping;map.wrapT=THREE.RepeatWrapping;
  map.repeat.set(width*2.5,height*2.5);map.needsUpdate=true;
  if(THREE.SRGBColorSpace)map.colorSpace=THREE.SRGBColorSpace;
  return map;
 }
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
  const surface=texture(THREE,width,height);
  const materials=[
   new THREE.MeshStandardMaterial({...shared,color:surface?'#c1c4bf':'#747a79',
    ...(surface?{map:surface}:{})}),
   new THREE.MeshStandardMaterial({...shared,color:'#505956'})
  ];
  const geometry=new THREE.ExtrudeGeometry(shape,{
   depth,bevelEnabled:false,curveSegments:32,steps:1
  });
  const mesh=new THREE.Mesh(geometry,materials);
  return {mesh,accepted,excluded};
 }
 return {build};
});
