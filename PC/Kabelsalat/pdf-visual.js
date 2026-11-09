'use strict';
/* Deterministic PDF V2 diagram view. No Three.js / WebGL required.
   2D drawing positions are sourced from the PDF diagrams, not surveyed mm.
   This file deliberately never writes the engineering project to storage. */
(function(root,factory){
 const api=factory();
 if(typeof module==='object'&&module.exports)module.exports=api;
 if(root)root.KabelsalatPdfVisual=api;
})(typeof globalThis!=='undefined'?globalThis:this,function(){
 const WALL_INFO={
  SW1:{caption:'Stirnwand · 13_U_042 / Gleis · PDF S.21',source:21},
  SW2:{caption:'Rechte Wand · 21_B_010 / Tunnelwand · PDF S.22',source:22},
  SW3:{caption:'Gegenüber SW1 · 13_U_032 · PDF S.23',source:23},
  SW4:{caption:'Linke Wand · 13_U_021 · PDF S.24',source:24}
 };
 const ids=['SW1','SW2','SW3','SW4'];
 const safe=value=>String(value??'').replace(/[&<>"']/g,
  c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
 function occupancy(project){
  const result=new Map();
  for(const c of project.cables||[]){
   for(const ref of [c.a,c.b]){
    if(!ref?.hole)continue;
    if(!result.has(ref.hole))result.set(ref.hole,[]);
    result.get(ref.hole).push(c);
   }
  }
  return result;
 }
 function validate(project){
  const walls=project?.walls||[];
  if(walls.length!==4||ids.some(id=>!walls.some(w=>w.id===id)))
   throw Error('PDF-Referenz benötigt vier Wände SW1–SW4.');
  return walls;
 }
 function pipe(h,items){
  const x=37+h.u*740,y=54+(1-h.v)*470,r=Math.max(13,Number(h.dn)*.23);
  const count=items.length,colors=new Map();
  for(const c of items)colors.set(c.color||'#687e91',(colors.get(c.color||'#687e91')||0)+1);
  const circ=2*Math.PI*(r+4);let offset=0;
  const arcs=[...colors].map(([color,n])=>{
   const len=circ*n/count,markup='<circle cx="'+x+'" cy="'+y+'" r="'+(r+4)+
    '" fill="none" stroke="'+safe(color)+'" stroke-width="8" stroke-dasharray="'+
    (Math.max(0,len-2)).toFixed(2)+' '+circ.toFixed(2)+'" stroke-dashoffset="'+
    (-offset).toFixed(2)+'" transform="rotate(-90 '+x+' '+y+')"/>';
   offset+=len;return markup;
  }).join('');
  const digits=(h.id.match(/_LR(\d+)/)||[])[1]||'';
  const trasse=(h.id.match(/_T(\d+)_/)||[])[1]||'1';
  const badge=count?'<g><circle cx="'+(x+r*.75)+'" cy="'+(y-r*.95)+
   '" r="12" fill="#e6eef5"/><text x="'+(x+r*.75)+'" y="'+(y-r*.95+4)+
   '" text-anchor="middle" class="p-count">'+count+'</text></g>':'';
  return '<g data-pipe="'+safe(h.id)+'" data-trasse="'+safe(trasse)+
   '" aria-label="'+safe(h.id+' DN '+h.dn+' mit '+count+' Kabeln')+'">'+
   '<circle cx="'+x+'" cy="'+y+'" r="'+r+
   '" fill="#ffffff" stroke="#273f53" stroke-width="2"/>'+arcs+
   '<text x="'+x+'" y="'+(y+5)+'" text-anchor="middle" class="p-lr">'+
   Number(digits)+'</text><text x="'+x+'" y="'+
   (h.id==='SW1_T1_LR21'?y+25:y+r+14)+
   '" text-anchor="middle" class="p-dn">DN '+safe(h.dn)+'</text>'+badge+'</g>';
 }
 function wallSvg(w,itemsByPipe){
  const label=WALL_INFO[w.id]||{caption:w.id,source:'?'};
  const hasSecondTrasse=w.holes.some(h=>h.id.includes('_T2_')||h.id.includes('_T3_'));
  let divider='';
  if(hasSecondTrasse)divider='<line x1="642" y1="63" x2="642" y2="520" '+
   'stroke="#9bb4c6" stroke-width="2"/>'+
   '<text x="672" y="43" class="p-trasse">TRASSE '+(w.id==='SW1'?'2':'3')+'</text>';
  const types=[...new Set(w.holes.map(h=>(h.id.match(/_T(\d+)_/)||[])[1]))];
  const header='<text x="44" y="43" class="p-trasse">TRASSE '+(types[0]||'1')+'</text>';
  return '<svg class="pdf-reference-svg" viewBox="0 0 830 560" role="img" '+
   'aria-label="'+safe(w.id+' – '+label.caption+'; '+w.holes.length+' Leerrohre')+'">'+
   '<rect width="830" height="560" fill="#f6f9fb"/>'+header+divider+
   w.holes.map(h=>pipe(h,itemsByPipe.get(h.id)||[])).join('')+'</svg>';
 }
 function render(project,active='all'){
  validate(project);
  const lookup=occupancy(project);
  const cards=ids.filter(id=>active==='all'||active===id).map(id=>{
   const w=project.walls.find(w=>w.id===id);
   return '<article class="pdf-wall-card"><h3>'+id+
    ' <small>'+safe(WALL_INFO[id].caption)+'</small></h3>'+wallSvg(w,lookup)+
    '</article>';
  });
  return '<div class="pdf-wall-grid'+(active==='all'?' four':' single')+'">'+cards.join('')+'</div>';
 }
 function audit(project){
  validate(project);
  const lookup=occupancy(project),holes=project.walls.flatMap(w=>w.holes);
  return {walls:project.walls.length,holes:holes.length,
   totalCableObjects:project.cables.length,
   cableAppearances:[...lookup.values()].reduce((a,b)=>a+b.length,0),
   assigned:holes.filter(h=>lookup.has(h.id)).length,
   missingEnds:project.cables.filter(c=>!c.a||!c.b).length,
   ms:project.cables.filter(c=>c.group==='MS'&&c.a&&c.b).length,
   olaOpen:project.cables.filter(c=>c.group==='OLA'&&(!c.a||!c.b)).length};
 }
 function attach(){
  const panel=document.getElementById('pdfVisualPanel');
  if(!panel)return;
  let reference=null,selected='all';
  function model(){
   if(reference)return reference;
   try{reference=window.KabelsalatSamples.create('pdf-u028');}
   catch(e){panel.querySelector('#pdfVisualCanvas').textContent='PDF-Daten nicht geladen: '+e.message;return null;}
   return reference;
  }
  function draw(){
   const p=model();if(!p)return;
   const a=audit(p);
   document.getElementById('pdfVisualCanvas').innerHTML=render(p,selected);
   document.getElementById('pdfVisualSummary').textContent=
    a.walls+' Wände · '+a.holes+' Rohre · '+a.totalCableObjects+
    ' Kabelobjekte · '+a.missingEnds+' offene Verläufe · Quelle: PDF V2 S.21–24';
   panel.querySelectorAll('[data-pdf-wall]').forEach(b=>{
    const selectedButton=b.dataset.pdfWall===selected;
    b.classList.toggle('active',selectedButton);
    b.setAttribute('aria-pressed',String(selectedButton));
   });
  }
  function show(){panel.hidden=false;draw();document.getElementById('pdfOpen').setAttribute('aria-expanded','true');}
  function hide(){panel.hidden=true;document.getElementById('pdfOpen').setAttribute('aria-expanded','false');}
  document.getElementById('pdfOpen').addEventListener('click',()=>panel.hidden?show():hide());
  document.getElementById('pdfClose').addEventListener('click',hide);
  panel.addEventListener('click',event=>{
   const b=event.target.closest('[data-pdf-wall]');
   if(b){selected=b.dataset.pdfWall;draw();}
  });
  document.addEventListener('keydown',event=>{if(event.key==='Escape'&&!panel.hidden){hide();}});
  window.KabelsalatPdfVisual.show=show;
  window.KabelsalatPdfVisual.hide=hide;
  window.KabelsalatPdfVisual.setReference=p=>{reference=p;draw();};
  setTimeout(()=>{
   if(!document.body.classList.contains('app-ready'))show();
  },8500);
 }
 if(typeof document!=='undefined'){
  if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',attach,{once:true});
  else attach();
 }
 return {render,occupancy,audit,WALL_INFO};
});
