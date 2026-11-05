(() => {
'use strict';
const $=id=>document.getElementById(id),STORAGE='eugi.schachtwand.3d.v1';
const COLORS=['#e75160','#1aa676','#398bdb','#e3ad3f','#9a6dcd','#12a5ae'];
const WALL_NAMES={SW1:'Linke Schachtwand',SW2:'Rückwand',SW3:'Rechte Schachtwand'};
const clamp=(v,a,b)=>Math.max(a,Math.min(b,v));
const esc=x=>String(x).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
let idCounter=0;const uid=p=>p+'-'+Date.now().toString(36)+'-'+(++idCounter).toString(36);
function grid(wall){const holes=[];for(let row=1;row<=wall.rows;row++)for(let col=1;col<=wall.cols;col++)
 holes.push({id:wall.id+'-r'+row+'-o'+col,row,col,u:col/(wall.cols+1),v:1-row/(wall.rows+1)});return holes;}
function demo(){
 const walls=[{id:'SW1',name:'SW1',visible:true,rows:5,cols:5,holes:[]},{id:'SW2',name:'SW2',visible:true,rows:4,cols:7,holes:[]},{id:'SW3',name:'SW3',visible:true,rows:5,cols:5,holes:[]}];
 walls.forEach(w=>w.holes=grid(w));
 const pairs=[['SW1',2,2,'SW3',2,3],['SW1',3,2,'SW3',4,2],['SW1',4,3,'SW2',3,2],['SW2',1,3,'SW3',3,4],['SW1',1,4,'SW3',1,2],['SW2',3,4,'SW3',4,4],['SW1',4,1,'SW2',2,6],['SW1',2,4,'SW3',5,3]];
 const names=['Rot 1','Rot 2','Grün 1','Blau 1','Gelb 1','Grün 2','Blau 2','Grün 3'],cs=[0,0,1,2,3,1,2,1];
 const cables=pairs.map((p,i)=>({id:'c'+(i+1),name:names[i],color:COLORS[cs[i]],a:{wall:p[0],hole:p[0]+'-r'+p[1]+'-o'+p[2]},b:{wall:p[3],hole:p[3]+'-r'+p[4]+'-o'+p[5]}}));
 return {version:1,name:'Beispiel · Schachtwandbelegung',dims:{w:2400,d:1900,h:2100},walls,cables,opacity:54,showBox:true,showLabels:true};
}
function validProject(o){
 if(!o||typeof o!=='object'||!o.dims||!Array.isArray(o.walls)||!Array.isArray(o.cables))throw Error('Ungültige Projektdatei');
 const base=demo(),ids=['SW1','SW2','SW3'];
 const walls=ids.map(id=>{
 const w=o.walls.find(x=>x&&x.id===id)||base.walls.find(x=>x.id===id);
 const holes=Array.isArray(w.holes)?w.holes.filter(h=>h&&typeof h.id==='string'&&Number.isFinite(+h.u)&&Number.isFinite(+h.v)).slice(0,600).map(h=>({
 id:h.id.slice(0,120),row:clamp(Number(h.row)||0,0,30),col:clamp(Number(h.col)||0,0,30),u:clamp(+h.u,0,1),v:clamp(+h.v,0,1)})):grid(w);
 return {id,name:id,visible:w.visible!==false,rows:clamp(Number(w.rows)||5,1,20),cols:clamp(Number(w.cols)||5,1,20),holes};
 });
 const find=r=>r&&walls.some(w=>w.id===r.wall&&w.holes.some(h=>h.id===r.hole));
 const cables=o.cables.slice(0,400).filter(c=>c&&typeof c==='object').map((c,i)=>({
 id:String(c.id||uid('c')).slice(0,120),name:String(c.name||'Kabel '+(i+1)).slice(0,80),
 color:/^#[0-9a-f]{6}$/i.test(c.color)?c.color:COLORS[i%COLORS.length],
 a:find(c.a)?{wall:c.a.wall,hole:c.a.hole}:null,b:find(c.b)?{wall:c.b.wall,hole:c.b.hole}:null
 }));
 return {version:1,name:String(o.name||'Neues Projekt').slice(0,80),
 dims:{w:clamp(Number(o.dims.w)||2400,600,12000),d:clamp(Number(o.dims.d)||1900,600,12000),h:clamp(Number(o.dims.h)||2100,600,8000)},
 walls,cables,opacity:clamp(Number(o.opacity)||55,10,100),showBox:o.showBox!==false,showLabels:o.showLabels!==false};
}
let project;try{project=validProject(JSON.parse(localStorage.getItem(STORAGE)))}catch(_){project=demo();}
let wallId='SW1',holeId=null,cableId=project.cables[0]?.id||null,filter='all',mode='orbit',grab=null;
const undo=[],redo=[];
const wall=()=>project.walls.find(w=>w.id===wallId);
const cable=()=>project.cables.find(c=>c.id===cableId);
const hole=()=>wall()?.holes.find(h=>h.id===holeId);
function persist(){try{localStorage.setItem(STORAGE,JSON.stringify(project));$('saveStatus').textContent='● Lokal gespeichert';}catch(_){$('saveStatus').textContent='⚠ Speichern fehlgeschlagen';}}
function change(fn){undo.push(JSON.stringify(project));if(undo.length>80)undo.shift();redo.length=0;fn();renderAll();persist();}
function undoRedo(back){const from=back?undo:redo,to=back?redo:undo;if(!from.length)return;to.push(JSON.stringify(project));project=validProject(JSON.parse(from.pop()));fixSelection();renderAll();persist();}
function fixSelection(){if(!project.walls.some(w=>w.id===wallId))wallId='SW1';if(!wall()?.holes.some(h=>h.id===holeId))holeId=null;if(!project.cables.some(c=>c.id===cableId))cableId=project.cables[0]?.id||null;}
function holeText(w,h){return w.id+' · '+(h.row?'Reihe '+h.row+', Öffnung '+h.col:'Frei platziert');}
function referenceText(r){if(!r)return 'Nicht zugeordnet';const w=project.walls.find(x=>x.id===r.wall),h=w?.holes.find(x=>x.id===r.hole);return h?holeText(w,h):'Nicht zugeordnet';}
function optionsHTML(selected){let html='<option value="">– Nicht zugeordnet –</option>';
 for(const w of project.walls){html+='<optgroup label="'+esc(w.id)+'">';
 for(const h of w.holes){const key=w.id+'|'+h.id;html+='<option value="'+esc(key)+'"'+(selected&&selected.wall===w.id&&selected.hole===h.id?' selected':'')+'>'+esc(holeText(w,h))+'</option>';}
 html+='</optgroup>';}return html;}
function parseEndpoint(v){if(!v)return null;const i=v.indexOf('|');return i<0?null:{wall:v.slice(0,i),hole:v.slice(i+1)};}
function renderUI(){
 $('projectName').value=project.name;$('width').value=project.dims.w;$('depth').value=project.dims.d;$('height').value=project.dims.h;
 $('rows').value=wall().rows;$('cols').value=wall().cols;$('wallTitle').textContent=wallId+' · Konfiguration';
 $('toggleWall').textContent=wall().visible?'Sichtbar':'Ausgeblendet';
 $('wallList').innerHTML=project.walls.map(w=>'<div class="wall-card '+(w.id===wallId?'active':'')+'" data-wall="'+w.id+'"><div class="row spread"><b><span class="dot" style="background:'+(w.visible?'#68a989':'#c9d2d7')+'"></span>'+w.id+'</b><span class="badge">'+w.holes.length+' Öffnungen</span></div><div class="small">'+WALL_NAMES[w.id]+' · '+w.rows+' × '+w.cols+'</div></div>').join('');
 $('opacity').value=project.opacity;$('opacityText').textContent=project.opacity+' %';$('showBox').checked=project.showBox;$('showLabels').checked=project.showLabels;
 $('modeButtons').querySelectorAll('button').forEach(b=>b.classList.toggle('active',b.dataset.mode===mode));$('scene').classList.toggle('placing',mode==='add');
 $('modeHint').textContent=mode==='add'?'Klicke auf eine Schachtwand, um dort ein frei positioniertes Leerrohr zu platzieren.':
 mode==='move'?'Ziehe ein vorhandenes Leerrohr auf seiner Wand. Verschobene Öffnungen bleiben Kabelanschlüsse.':
 'Modell mit der Maus drehen, mit dem Mausrad zoomen. Öffnungen oder Kabel anklicken, um sie zu bearbeiten.';
 const h=hole();$('holeEditor').style.display=h?'block':'none';
 if(h){$('holeTitle').textContent=wallId+' · '+holeText(wall(),h);$('holeU').value=Math.round(h.u*100);$('holeV').value=Math.round(h.v*100);}
 $('cableFilters').querySelectorAll('button').forEach(b=>b.classList.toggle('active',b.dataset.filter===filter));
 const match=c=>filter==='all'||(filter==='red'?c.color.toLowerCase().startsWith('#e'):filter==='green'?c.color.toLowerCase().startsWith('#1'):filter==='blue'?c.color.toLowerCase().startsWith('#3'):c.color.toLowerCase().startsWith('#e3'));
 $('cableList').innerHTML=project.cables.filter(match).map(c=>'<div class="cable '+(c.id===cableId?'active':'')+'" data-cable="'+esc(c.id)+'"><span class="swatch" style="background:'+c.color+'"></span><div style="flex:1;min-width:0"><div class="cable-name">'+esc(c.name)+'</div><div class="cable-path">'+esc(referenceText(c.a))+' → '+esc(referenceText(c.b))+'</div></div></div>').join('') || '<p class="small">Keine Kabel in diesem Filter.</p>';
 const c=cable();$('empty-cable').hidden=!!c;$('cableForm').hidden=!c;
 if(c){$('cableName').value=c.name;$('cableColor').value=c.color;$('cableA').innerHTML=optionsHTML(c.a);$('cableB').innerHTML=optionsHTML(c.b);}
 const count=project.walls.reduce((s,w)=>s+w.holes.length,0);
 $('sceneStats').textContent=project.walls.length+' Wände · '+count+' Öffnungen · '+project.cables.length+' Kabel';
 $('viewInfo').textContent=project.name;$('undo').disabled=!undo.length;$('redo').disabled=!redo.length;
}
const scene=new THREE.Scene();scene.background=new THREE.Color('#edf2f4');
const camera=new THREE.PerspectiveCamera(42,1,.015,200);
const renderer=new THREE.WebGLRenderer({canvas:$('scene'),antialias:true,preserveDrawingBuffer:true,alpha:false});
renderer.setPixelRatio(Math.min(window.devicePixelRatio||1,2));
renderer.setClearColor('#edf2f4');
if('outputColorSpace' in renderer)renderer.outputColorSpace=THREE.SRGBColorSpace;
scene.add(new THREE.HemisphereLight(0xffffff,0xb1bbc4,2.4));
const light=new THREE.DirectionalLight(0xffffff,2);light.position.set(-4,8,6);scene.add(light);
scene.add(new THREE.AmbientLight(0xffffff,.3));
const models=new THREE.Group();scene.add(models);
const hitObjects=[],wallObjects=[],raycaster=new THREE.Raycaster(),pointer=new THREE.Vector2();
let azimuth=.72,elevation=.42,zoom=1.0;
const sizes=()=>({w:project.dims.w/1000,d:project.dims.d/1000,h:project.dims.h/1000});
function view(){const d=sizes(),r=Math.max(d.w,d.d,d.h)*1.7*zoom,t=new THREE.Vector3(0,d.h*.48,0);
 camera.position.set(r*Math.sin(azimuth)*Math.cos(elevation),t.y+r*Math.sin(elevation),r*Math.cos(azimuth)*Math.cos(elevation));
 camera.lookAt(t);camera.updateProjectionMatrix();}
function disposeModel(){models.traverse(obj=>{if(obj.geometry)obj.geometry.dispose();if(obj.material){const a=Array.isArray(obj.material)?obj.material:[obj.material];a.forEach(m=>{if(m.map)m.map.dispose();m.dispose();});}});models.clear();hitObjects.length=0;wallObjects.length=0;}
function material(color,opacity=1){return new THREE.MeshStandardMaterial({color,roughness:.8,metalness:.05,transparent:opacity<1,opacity,side:THREE.DoubleSide,depthWrite:opacity>=1});}
function box(w,h,d,x,y,z,col,opacity=1){const m=new THREE.Mesh(new THREE.BoxGeometry(w,h,d),material(col,opacity));m.position.set(x,y,z);models.add(m);return m;}
function createLabel(text,pos){const c=document.createElement('canvas');c.width=280;c.height=76;const ctx=c.getContext('2d');
 ctx.fillStyle='#244753';ctx.font='bold 32px Segoe UI,Arial';ctx.textAlign='center';ctx.fillText(text,140,47);
 const tex=new THREE.CanvasTexture(c),sprite=new THREE.Sprite(new THREE.SpriteMaterial({map:tex,transparent:true,depthTest:false}));
 sprite.position.copy(pos);sprite.scale.set(.5,.14,1);models.add(sprite);}
function holePosition(wid,h){const s=sizes(),u=.10+.8*clamp(h.u,0,1),y=s.h*(.12+.76*clamp(h.v,0,1));
 if(wid==='SW1')return new THREE.Vector3(-s.w/2+.052,y,-s.d/2+s.d*u);
 if(wid==='SW2')return new THREE.Vector3(-s.w/2+s.w*u,y,-s.d/2+.052);
 return new THREE.Vector3(s.w/2-.052,y,-s.d/2+s.d*u);}
function normal(wid){return new THREE.Vector3(wid==='SW1'?1:wid==='SW3'?-1:0,0,wid==='SW2'?1:0);}
function drawOpening(w,h){const pos=holePosition(w.id,h),n=normal(w.id),r=Math.min(sizes().w,sizes().d,sizes().h)*.025;
 const selected=w.id===wallId&&h.id===holeId,group=new THREE.Group();group.position.copy(pos);group.quaternion.setFromUnitVectors(new THREE.Vector3(0,0,1),n);
 const circle=new THREE.Mesh(new THREE.CircleGeometry(r,30),new THREE.MeshBasicMaterial({color:selected?'#366f68':'#52636d',side:THREE.DoubleSide}));
 group.add(circle);circle.userData={type:'hole',wall:w.id,hole:h.id};
 const ring=new THREE.Mesh(new THREE.TorusGeometry(r,.007,5,32),new THREE.MeshBasicMaterial({color:selected?'#f9cf68':'#cad6d9'}));ring.position.z=.008;group.add(ring);
 models.add(group);hitObjects.push(circle);}
function drawWalls(){const s=sizes(),op=project.opacity/100;
 box(s.w+.16,.085,s.d+.16,0,-.055,0,'#a1afb7');
 const floor=new THREE.GridHelper(Math.max(s.w,s.d)*1.7,18,'#d7e1e5','#e3eaed');floor.position.y=-.096;models.add(floor);
 for(const w of project.walls){if(!w.visible)continue;let m;
 if(w.id==='SW1')m=box(.085,s.h,s.d,-s.w/2,s.h/2,0,'#899ba4',op);
 if(w.id==='SW2')m=box(s.w,s.h,.085,0,s.h/2,-s.d/2,'#8296a1',op);
 if(w.id==='SW3')m=box(.085,s.h,s.d,s.w/2,s.h/2,0,'#879aa5',op);
 m.userData={type:'wall',wall:w.id};wallObjects.push(m);
 if(project.showLabels)createLabel(w.id,new THREE.Vector3(w.id==='SW1'?-s.w/2:w.id==='SW3'?s.w/2:0,s.h+.15,w.id==='SW2'?-s.d/2:0));
 for(const h of w.holes)drawOpening(w,h);}
 if(project.showBox){const x=0,z=s.d*.22,bw=s.w*.75,bh=s.h*.31,bd=s.d*.32;
 box(bw,.045,bd,x,bh,z,'#b5bec2',.92);box(bw,.045,bd,x,.08,z,'#b6c0c5',.90);
 box(.04,bh,bd,-bw/2,bh/2+.06,z,'#a2afb6',.75);box(.04,bh,bd,bw/2,bh/2+.06,z,'#a2afb6',.75);}
}
function endpoint(r){if(!r)return null;const w=project.walls.find(x=>x.id===r.wall),h=w?.holes.find(x=>x.id===r.hole);return h?holePosition(w.id,h):null;}
function drawCable(c){const a=endpoint(c.a),b=endpoint(c.b);if(!a||!b)return;
 const chosen=c.id===cableId,s=sizes(),na=normal(c.a.wall),nb=normal(c.b.wall),thickness=Math.min(s.w,s.d,s.h)*.008;
 const mid=a.clone().add(b).multiplyScalar(.5);mid.y=clamp(mid.y+.04,.18,s.h*.85);mid.z=clamp(mid.z+.08,-s.d/2+.18,s.d/2-.14);
 const curve=new THREE.CatmullRomCurve3([a.clone(),a.clone().addScaledVector(na,.18),mid,b.clone().addScaledVector(nb,.18),b.clone()],false,'centripetal',.5);
 const line=new THREE.Mesh(new THREE.TubeGeometry(curve,52,chosen?thickness*1.55:thickness,7,false),
 new THREE.MeshStandardMaterial({color:c.color,roughness:.34,metalness:.04,transparent:true,opacity:chosen?1:.88}));
 line.userData={type:'cable',id:c.id};models.add(line);hitObjects.push(line);
 if(chosen)for(const [key,vec] of [['a',a],['b',b]]){
 const radius=Math.min(s.w,s.d,s.h)*.026;
 const sphere=new THREE.Mesh(new THREE.SphereGeometry(radius,16,12),new THREE.MeshBasicMaterial({color:'#ffffff'}));
 sphere.position.copy(vec);sphere.userData={type:'handle',id:c.id,end:key};models.add(sphere);hitObjects.push(sphere);
 const outline=new THREE.Mesh(new THREE.SphereGeometry(radius*1.25,16,12),new THREE.MeshBasicMaterial({color:c.color,wireframe:true}));outline.position.copy(vec);models.add(outline);}
}
function rebuild(){disposeModel();drawWalls();project.cables.forEach(drawCable);view();}
function renderAll(){fixSelection();renderUI();rebuild();}
function pick(event,targets=hitObjects){const rect=$('scene').getBoundingClientRect();
 pointer.x=((event.clientX-rect.left)/rect.width)*2-1;pointer.y=-((event.clientY-rect.top)/rect.height)*2+1;
 raycaster.setFromCamera(pointer,camera);return raycaster.intersectObjects(targets,false)[0]||null;}
function wallCoords(wid,p){const s=sizes(),u=wid==='SW2'?(p.x/s.w+.5):p.z/s.d+.5;
 return {u:clamp((u-.1)/.8,0,1),v:clamp((p.y/s.h-.12)/.76,0,1)};}
function newOpening(wid,point){const w=project.walls.find(x=>x.id===wid),coords=wallCoords(wid,point),id=uid(wid+'-free');
 change(()=>{w.holes.push({id,row:0,col:0,...coords});wallId=wid;holeId=id;});}
const canvas=$('scene');
canvas.addEventListener('pointerdown',e=>{
 if(e.button!==0)return;canvas.setPointerCapture(e.pointerId);const hit=pick(e),obj=hit?.object.userData||{};
 if(mode==='add'){const wallHit=pick(e,wallObjects);if(wallHit){newOpening(wallHit.object.userData.wall,wallHit.point);return;}}
 if(obj.type==='handle'){grab={type:'handle',id:obj.id,end:obj.end};return;}
 if(obj.type==='hole'){wallId=obj.wall;holeId=obj.hole;renderAll();if(mode==='move')grab={type:'move',wall:obj.wall,hole:obj.hole,changed:false};return;}
 if(obj.type==='cable'){cableId=obj.id;filter='all';renderAll();return;}
 grab={type:'orbit',x:e.clientX,y:e.clientY};canvas.classList.add('dragging');
});
canvas.addEventListener('pointermove',e=>{
 if(grab?.type==='orbit'){azimuth-=(e.clientX-grab.x)*.006;elevation=clamp(elevation+(e.clientY-grab.y)*.005,-.08,1.45);grab.x=e.clientX;grab.y=e.clientY;view();return;}
 if(grab?.type==='move'){const w=project.walls.find(x=>x.id===grab.wall),h=w?.holes.find(x=>x.id===grab.hole);
 const hit=pick(e,wallObjects.filter(x=>x.userData.wall===grab.wall));
 if(h&&hit){if(!grab.changed){undo.push(JSON.stringify(project));if(undo.length>80)undo.shift();redo.length=0;grab.changed=true;}
 Object.assign(h,wallCoords(w.id,hit.point));rebuild();}return;}
 if(grab?.type==='handle'){const hit=pick(e,hitObjects.filter(o=>o.userData.type==='hole'));
 $('tooltip').style.display=hit?'block':'none';if(hit){const o=hit.object.userData;$('tooltip').textContent=referenceText({wall:o.wall,hole:o.hole});$('tooltip').style.left=(e.offsetX+13)+'px';$('tooltip').style.top=(e.offsetY+13)+'px';}return;}
 const hit=pick(e),o=hit?.object.userData;$('tooltip').style.display=o?'block':'none';
 if(o){$('tooltip').textContent=o.type==='hole'?referenceText({wall:o.wall,hole:o.hole}):o.type==='cable'?project.cables.find(c=>c.id===o.id)?.name||'Kabel':o.type==='handle'?'Griff auf Leerrohr ziehen':'';
 $('tooltip').style.left=(e.offsetX+13)+'px';$('tooltip').style.top=(e.offsetY+13)+'px';}
});
canvas.addEventListener('pointerup',e=>{
 if(grab?.type==='handle'){const hit=pick(e,hitObjects.filter(o=>o.userData.type==='hole'));
 if(hit){const o=hit.object.userData,c=project.cables.find(x=>x.id===grab.id);if(c)change(()=>{c[grab.end]={wall:o.wall,hole:o.hole};});}}
 if(grab?.type==='move'&&grab.changed){renderAll();persist();}
 grab=null;canvas.classList.remove('dragging');
});
canvas.addEventListener('pointercancel',()=>{grab=null;canvas.classList.remove('dragging');renderAll();});
canvas.addEventListener('wheel',e=>{e.preventDefault();zoom=clamp(zoom*(e.deltaY>0?1.11:.9),.45,4);view();},{passive:false});
canvas.addEventListener('contextmenu',e=>e.preventDefault());
$('wallList').addEventListener('click',e=>{const card=e.target.closest('[data-wall]');if(card){wallId=card.dataset.wall;holeId=null;renderAll();}});
$('generate').addEventListener('click',()=>{
 const w=wall(),rows=clamp(Number($('rows').value)||w.rows,1,20),cols=clamp(Number($('cols').value)||w.cols,1,20);
 const affected=project.cables.some(c=>c.a?.wall===w.id||c.b?.wall===w.id);
 if(affected&&!confirm('Das Raster wird neu aufgebaut. Kabelanschlüsse an '+w.id+' werden entfernt. Fortfahren?'))return;
 change(()=>{w.rows=rows;w.cols=cols;w.holes=grid(w);project.cables.forEach(c=>{if(c.a?.wall===w.id)c.a=null;if(c.b?.wall===w.id)c.b=null;});holeId=null;});
});
$('toggleWall').addEventListener('click',()=>change(()=>{wall().visible=!wall().visible;}));
$('modeButtons').addEventListener('click',e=>{const b=e.target.closest('[data-mode]');if(b){mode=b.dataset.mode;renderUI();}});
$('deleteHole').addEventListener('click',()=>{const w=wall(),h=hole();if(!h)return;
 const n=project.cables.filter(c=>c.a?.hole===h.id||c.b?.hole===h.id).length;
 if(n&&!confirm(n+' Kabel haben diesen Anschluss. Das Leerrohr wirklich entfernen?'))return;
 change(()=>{w.holes=w.holes.filter(x=>x.id!==h.id);project.cables.forEach(c=>{if(c.a?.hole===h.id)c.a=null;if(c.b?.hole===h.id)c.b=null;});holeId=null;});
});
$('applyHole').addEventListener('click',()=>{const h=hole();if(!h)return;change(()=>{h.u=clamp((Number($('holeU').value)||0)/100,0,1);h.v=clamp((Number($('holeV').value)||0)/100,0,1);});});
$('cableList').addEventListener('click',e=>{const card=e.target.closest('[data-cable]');if(card){cableId=card.dataset.cable;renderAll();}});
$('cableFilters').addEventListener('click',e=>{const b=e.target.closest('[data-filter]');if(b){filter=b.dataset.filter;renderUI();}});
$('addCable').addEventListener('click',()=>change(()=>{const n=project.cables.length+1,id=uid('c');project.cables.push({id,name:'Kabel '+n,color:COLORS[(n-1)%COLORS.length],a:null,b:null});cableId=id;filter='all';}));
$('deleteCable').addEventListener('click',()=>{if(!cable())return;change(()=>{project.cables=project.cables.filter(c=>c.id!==cableId);cableId=project.cables[0]?.id||null;});});
$('swapCable').addEventListener('click',()=>{const c=cable();if(c)change(()=>{const t=c.a;c.a=c.b;c.b=t;});});
$('cableName').addEventListener('change',()=>{const c=cable();if(c)change(()=>c.name=$('cableName').value.trim().slice(0,80)||'Kabel');});
$('cableColor').addEventListener('change',()=>{const c=cable();if(c)change(()=>{c.color=$('cableColor').value;});});
for(const [field,key] of [['cableA','a'],['cableB','b']])$(field).addEventListener('change',()=>{const c=cable();if(c)change(()=>{c[key]=parseEndpoint($(field).value);});});
$('projectName').addEventListener('change',()=>change(()=>project.name=$('projectName').value.trim().slice(0,80)||'Neues Projekt'));
for(const [el,key,min,max] of [['width','w',600,12000],['depth','d',600,12000],['height','h',600,8000]])
 $(el).addEventListener('change',()=>{const x=clamp(Number($(el).value)||project.dims[key],min,max);change(()=>{project.dims[key]=x;});});
$('opacity').addEventListener('change',()=>change(()=>project.opacity=Number($('opacity').value)));
$('opacity').addEventListener('input',()=>{$('opacityText').textContent=$('opacity').value+' %';});
$('showBox').addEventListener('change',()=>change(()=>project.showBox=$('showBox').checked));
$('showLabels').addEventListener('change',()=>change(()=>project.showLabels=$('showLabels').checked));
$('resetView').addEventListener('click',()=>{azimuth=.72;elevation=.42;zoom=1;view();});
$('undo').addEventListener('click',()=>undoRedo(true));$('redo').addEventListener('click',()=>undoRedo(false));
document.addEventListener('keydown',e=>{if((e.ctrlKey||e.metaKey)&&e.key.toLowerCase()==='z'){e.preventDefault();undoRedo(!e.shiftKey);}else if((e.ctrlKey||e.metaKey)&&e.key.toLowerCase()==='y'){e.preventDefault();undoRedo(false);}});
function download(name,data,type){const blob=data instanceof Blob?data:new Blob([data],{type}),href=URL.createObjectURL(blob),a=document.createElement('a');a.href=href;a.download=name;document.body.append(a);a.click();a.remove();setTimeout(()=>URL.revokeObjectURL(href),1500);}
function filename(){return project.name.replace(/[^\p{L}\p{N}_-]+/gu,'_').slice(0,50)||'Schachtwand';}
$('saveProject').addEventListener('click',()=>download(filename()+'.json',JSON.stringify(project,null,2),'application/json'));
$('openProject').addEventListener('click',()=>$('fileInput').click());
$('fileInput').addEventListener('change',async e=>{const f=e.target.files?.[0];if(!f)return;
 try{const next=validProject(JSON.parse(await f.text()));change(()=>{project=next;wallId='SW1';holeId=null;cableId=project.cables[0]?.id||null;});}
 catch(ex){alert('Projektdatei konnte nicht geladen werden: '+ex.message);}e.target.value='';});
$('newProject').addEventListener('click',()=>{if(!confirm('Neue Demo-Planung starten? Ungespeicherte Änderungen sind danach nur noch über Rückgängig verfügbar.'))return;
 change(()=>{project=demo();wallId='SW1';holeId=null;cableId=project.cables[0].id;filter='all';});});
$('exportPng').addEventListener('click',()=>{renderer.render(scene,camera);renderer.domElement.toBlob(blob=>{if(blob)download(filename()+'.png',blob,'image/png');},'image/png');});
function resize(){const rect=$('viewport').getBoundingClientRect();renderer.setSize(Math.max(10,rect.width),Math.max(10,rect.height),false);camera.aspect=rect.width/Math.max(1,rect.height);camera.updateProjectionMatrix();view();}
new ResizeObserver(resize).observe($('viewport'));
function animate(){requestAnimationFrame(animate);renderer.render(scene,camera);}
renderAll();persist();resize();animate();
})();