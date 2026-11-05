(() => {
'use strict';
const $=id=>document.getElementById(id),STORAGE='eugi.schachtwand.3d.v1';
const COLORS=['#e75160','#1aa676','#398bdb','#e3ad3f','#9a6dcd','#12a5ae'];
const WALL_NAMES={SW1:'Linke Schachtwand',SW2:'Rückwand',SW3:'Rechte Schachtwand',SW4:'Vordere Schachtwand'};
const clamp=(v,a,b)=>Math.max(a,Math.min(b,v));
const esc=x=>String(x).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
let idCounter=0;const uid=p=>p+'-'+Date.now().toString(36)+'-'+(++idCounter).toString(36);

function grid(wall){
 const holes=[];for(let row=1;row<=wall.rows;row++)for(let col=1;col<=wall.cols;col++)
 holes.push({id:wall.id+'-r'+row+'-o'+col,row,col,u:col/(wall.cols+1),v:1-row/(wall.rows+1),dn:wall.dn||70});
 return holes;
}
function demo(){
 const walls=[
 {id:'SW1',name:'SW1',visible:true,rows:5,cols:5,thickness:120,dn:70,holes:[]},
 {id:'SW2',name:'SW2',visible:true,rows:4,cols:7,thickness:120,dn:70,holes:[]},
 {id:'SW3',name:'SW3',visible:true,rows:5,cols:5,thickness:120,dn:70,holes:[]},
 {id:'SW4',name:'SW4',visible:true,rows:5,cols:6,thickness:120,dn:70,holes:[]}
 ];
 walls.forEach(w=>w.holes=grid(w));
 const pairs=[
 ['SW1',2,2,'SW3',2,3],['SW1',3,2,'SW3',4,2],['SW1',4,3,'SW2',3,2],
 ['SW2',1,3,'SW3',3,4],['SW1',1,4,'SW3',1,2],['SW2',3,4,'SW3',4,4],
 ['SW1',4,1,'SW2',2,6],['SW1',2,4,'SW3',5,3],
 ['SW4',2,2,'SW1',2,3],['SW4',3,3,'SW2',1,6],['SW4',4,4,'SW3',3,2],
 ['SW1',1,1,'SW4',1,4],['SW1',5,2,'SW4',5,5],['SW2',4,2,'SW4',4,2],
 ['SW2',2,5,'SW4',2,5],['SW3',1,3,'SW4',1,2]
 ];
 const names=['OLA · LR18','OLA · LR19','OLA · LR20','MS · LR13','MS · LR14',
 'LST · LR5','LST · LR6','TK Cu · LR7','TK Cu · LR8','Weitere · LR1','Weitere · LR2',
 'Weitere · LR3','Weitere · LR4','Weitere · LR5','Weitere · LR6','Weitere · LR7'];
 const colors=['#d94b57','#d94b57','#d94b57','#248dba','#248dba','#349e7b',
 '#349e7b','#9b67bd','#9b67bd','#d8a43e','#d8a43e','#9b67bd','#9b67bd','#349e7b','#8a929c','#8a929c'];
 const cables=pairs.map((p,i)=>({id:'c'+(i+1),name:names[i],color:colors[i],group:i<3?'OLA':i<5?'MS':'Weitere',
 diameter:12,route:[],a:{wall:p[0],hole:p[0]+'-r'+p[1]+'-o'+p[2]},
 b:{wall:p[3],hole:p[3]+'-r'+p[4]+'-o'+p[5]}}));
 return {version:2,name:'Referenzschacht · 3D-Entwurf',
 dims:{w:2400,d:1900,h:2100},walls,cables,opacity:54,showBox:true,showLabels:true,
 enclosures:{OLA:{visible:true,opacity:70},MS:{visible:true,opacity:48}}};
}
function validProject(o){
 if(!o||typeof o!=='object'||!o.dims||!Array.isArray(o.walls)||!Array.isArray(o.cables))throw Error('Ungültige Projektdatei');
 const base=demo(),ids=['SW1','SW2','SW3','SW4'];
 const dims={w:clamp(Number(o.dims.w)||2400,600,12000),
 d:clamp(Number(o.dims.d)||1900,600,12000),h:clamp(Number(o.dims.h)||2100,600,8000)};
 const walls=ids.map(id=>{
 const w=o.walls.find(x=>x&&x.id===id)||base.walls.find(x=>x.id===id);
 const thickness=clamp(Number(w.thickness)||120,40,500),dn=clamp(Number(w.dn)||70,20,300);
 const holes=Array.isArray(w.holes)?w.holes.filter(h=>h&&typeof h.id==='string'&&Number.isFinite(+h.u)&&Number.isFinite(+h.v)).slice(0,600).map(h=>({
 id:h.id.slice(0,120),row:clamp(Number(h.row)||0,0,30),col:clamp(Number(h.col)||0,0,30),
 u:clamp(+h.u,0,1),v:clamp(+h.v,0,1),dn:clamp(Number(h.dn)||dn,20,300)})):grid(w);
 return {id,name:id,visible:w.visible!==false,rows:clamp(Number(w.rows)||5,1,20),
 cols:clamp(Number(w.cols)||5,1,20),thickness,dn,holes};
 });
 const find=r=>r&&walls.some(w=>w.id===r.wall&&w.holes.some(h=>h.id===r.hole));
 const cables=o.cables.slice(0,400).filter(c=>c&&typeof c==='object').map((c,i)=>({
 id:String(c.id||uid('c')).slice(0,120),name:String(c.name||'Kabel '+(i+1)).slice(0,80),
 color:/^#[0-9a-f]{6}$/i.test(c.color)?c.color:COLORS[i%COLORS.length],
 group:['OLA','MS','Weitere'].includes(c.group)?c.group:'Weitere',
 diameter:clamp(Number(c.diameter)||12,2,80),
 route:Array.isArray(c.route)?c.route.filter(p=>Array.isArray(p)&&p.length===3&&p.every(n=>Number.isFinite(+n)))
 .slice(0,12).map(p=>[clamp(+p[0],-dims.w/2,dims.w/2),clamp(+p[1],0,dims.h),clamp(+p[2],-dims.d/2,dims.d/2)]):[],
 a:find(c.a)?{wall:c.a.wall,hole:c.a.hole}:null,
 b:find(c.b)?{wall:c.b.wall,hole:c.b.hole}:null
 }));
 const encl=o.enclosures||{};
 const group=k=>({visible:encl[k]?.visible!==false,opacity:clamp(Number(encl[k]?.opacity)||60,10,100)});
 return {version:2,name:String(o.name||'Neues Projekt').slice(0,80),dims,walls,cables,
 opacity:clamp(Number(o.opacity)||55,10,100),showBox:o.showBox!==false,
 showLabels:o.showLabels!==false,enclosures:{OLA:group('OLA'),MS:group('MS')}};
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
function wallSpanMm(id){return id==='SW1'||id==='SW3'?project.dims.d:project.dims.w;}
function geometryWarnings(){
 let overlaps=0,edges=0;
 for(const w of project.walls){
 const span=wallSpanMm(w.id),height=project.dims.h;
 for(let i=0;i<w.holes.length;i++){
 const a=w.holes[i],ax=a.u*span,ay=a.v*height,ar=(a.dn||70)/2;
 if(ax-ar<0||ax+ar>span||ay-ar<0||ay+ar>height)edges++;
 for(let j=0;j<i;j++){
 const b=w.holes[j],dx=ax-b.u*span,dy=ay-b.v*height;
 if(Math.hypot(dx,dy)<ar+(b.dn||70)/2)overlaps++;
 }
 }
 }
 return {overlaps,edges};
}
function holeText(w,h){return w.id+' · '+(h.row?'Reihe '+h.row+', Öffnung '+h.col:'Frei platziert');}
function referenceText(r){if(!r)return 'Nicht zugeordnet';const w=project.walls.find(x=>x.id===r.wall),h=w?.holes.find(x=>x.id===r.hole);return h?holeText(w,h):'Nicht zugeordnet';}
function hueGroup(color){
 const r=parseInt(color.slice(1,3),16)/255,g=parseInt(color.slice(3,5),16)/255,b=parseInt(color.slice(5,7),16)/255;
 const max=Math.max(r,g,b),min=Math.min(r,g,b),d=max-min;
 if(d<.1)return 'other';
 let h=(max===r?(g-b)/d+(g<b?6:0):max===g?(b-r)/d+2:(r-g)/d+4)*60;
 return h<26||h>=340?'red':h<70?'yellow':h<165?'green':h<260?'blue':'other';
}
function optionsHTML(selected){let html='<option value="">– Nicht zugeordnet –</option>';
 for(const w of project.walls){html+='<optgroup label="'+esc(w.id)+'">';
 for(const h of w.holes){const key=w.id+'|'+h.id;html+='<option value="'+esc(key)+'"'+(selected&&selected.wall===w.id&&selected.hole===h.id?' selected':'')+'>'+esc(holeText(w,h))+'</option>';}
 html+='</optgroup>';}return html;}
function parseEndpoint(v){if(!v)return null;const i=v.indexOf('|');return i<0?null:{wall:v.slice(0,i),hole:v.slice(i+1)};}
function renderUI(){
 $('projectName').value=project.name;$('width').value=project.dims.w;$('depth').value=project.dims.d;$('height').value=project.dims.h;
 $('rows').value=wall().rows;$('cols').value=wall().cols;$('wallThickness').value=wall().thickness;$('wallDn').value=wall().dn;$('wallTitle').textContent=wallId+' · Konfiguration';
 $('toggleWall').textContent=wall().visible?'Sichtbar':'Ausgeblendet';
 $('wallList').innerHTML=project.walls.map(w=>'<div class="wall-card '+(w.id===wallId?'active':'')+'" data-wall="'+w.id+'"><div class="row spread"><b><span class="dot" style="background:'+(w.visible?'#68a989':'#c9d2d7')+'"></span>'+w.id+'</b><span class="badge">'+w.holes.length+' Öffnungen</span></div><div class="small">'+WALL_NAMES[w.id]+' · '+w.rows+' × '+w.cols+'</div></div>').join('');
 $('opacity').value=project.opacity;$('opacityText').textContent=project.opacity+' %';$('showBox').checked=project.showBox;$('showLabels').checked=project.showLabels;
 $('modeButtons').querySelectorAll('button').forEach(b=>b.classList.toggle('active',b.dataset.mode===mode));$('scene').classList.toggle('placing',mode==='add');
 $('modeHint').textContent=mode==='add'?'Klicke auf eine Schachtwand, um dort ein frei positioniertes Leerrohr zu platzieren.':
 mode==='move'?'Ziehe ein vorhandenes Leerrohr auf seiner Wand. Verschobene Öffnungen bleiben Kabelanschlüsse.':
 'Modell mit der Maus drehen, mit dem Mausrad zoomen. Öffnungen oder Kabel anklicken, um sie zu bearbeiten.';
 const h=hole();$('holeEditor').style.display=h?'block':'none';
 if(h){$('holeTitle').textContent=wallId+' · '+holeText(wall(),h);$('holeU').value=Math.round(h.u*wallSpanMm(wallId));$('holeV').value=Math.round(h.v*project.dims.h);$('holeDn').value=h.dn;}
 $('cableFilters').querySelectorAll('button').forEach(b=>{
 b.classList.toggle('active',b.dataset.filter===filter);
 if(['OLA','MS','Weitere'].includes(b.dataset.filter))
 b.textContent=b.dataset.filter+' '+project.cables.filter(c=>c.group===b.dataset.filter).length;
 if(b.dataset.filter==='all')b.textContent='Alle '+project.cables.length;
 });
 const match=c=>filter==='all'||(['OLA','MS','Weitere'].includes(filter)?c.group===filter:hueGroup(c.color)===filter);
 $('cableList').innerHTML=project.cables.filter(match).map(c=>'<div class="cable '+(c.id===cableId?'active':'')+'" data-cable="'+esc(c.id)+'"><span class="swatch" style="background:'+c.color+'"></span><div style="flex:1;min-width:0"><div class="cable-name">'+esc(c.name)+'</div><div class="cable-path">'+esc(referenceText(c.a))+' → '+esc(referenceText(c.b))+'</div></div></div>').join('') || '<p class="small">Keine Kabel in diesem Filter.</p>';
 const c=cable();$('empty-cable').hidden=!!c;$('cableForm').hidden=!c;
 if(c){
 $('cableName').value=c.name;$('cableColor').value=c.color;$('cableGroup').value=c.group;
 $('cableDiameter').value=c.diameter;$('cableA').innerHTML=optionsHTML(c.a);$('cableB').innerHTML=optionsHTML(c.b);
 $('waypointList').innerHTML=c.route.map((p,i)=>'<div class="point-row" data-index="'+i+'"><b>'+String(i+1)+'</b>'+
 ['X','Y','Z'].map((axis,k)=>'<input type="number" data-axis="'+k+'" value="'+Math.round(p[k])+'" title="'+axis+' (mm)" step="10">').join('')+
 '<button data-remove="'+i+'" title="Bogenpunkt entfernen">×</button></div>').join('')||
 '<span class="muted">Automatischer Verlauf (noch keine manuellen Bogenpunkte)</span>';
 }
 for(const k of ['ola','ms']){const e=project.enclosures[k.toUpperCase()];
 $(k+'Visible').checked=e.visible;$(k+'Opacity').value=e.opacity;$(k+'OpacityText').textContent=e.opacity+' %';}
 $('viewTabs').querySelectorAll('button').forEach(b=>b.classList.toggle('active',b.dataset.view===viewMode));
 const count=project.walls.reduce((s,w)=>s+w.holes.length,0);
 $('sceneStats').textContent=project.walls.length+' Wände · '+count+' Öffnungen · '+project.cables.length+' Kabel';
 const problems=geometryWarnings();
 $('collisionInfo').textContent=(problems.overlaps||problems.edges?'⚠ Geometrischer Hinweis: ':'✓ Geometrie: ')+
 problems.overlaps+' überlappende Rohrpaare, '+problems.edges+' Randüberschreitungen.'+
 ' Keine DIN-, VDE- oder Brandschutzprüfung.';
 $('collisionInfo').style.background=problems.overlaps||problems.edges?'#fff1e7':'#edf8f1';
 $('viewInfo').textContent=project.name;$('undo').disabled=!undo.length;$('redo').disabled=!redo.length;
}
const scene=new THREE.Scene();scene.background=new THREE.Color('#edf2f4');
const camera=new THREE.PerspectiveCamera(42,1,.015,200);
const orthoCamera=new THREE.OrthographicCamera(-2,2,2,-2,.01,200);let activeCamera=camera;
const renderer=new THREE.WebGLRenderer({canvas:$('scene'),antialias:true,preserveDrawingBuffer:true,alpha:false});
renderer.setPixelRatio(Math.min(window.devicePixelRatio||1,2));
renderer.setClearColor('#edf2f4');
if('outputColorSpace' in renderer)renderer.outputColorSpace=THREE.SRGBColorSpace;
scene.add(new THREE.HemisphereLight(0xffffff,0xb1bbc4,2.4));
const light=new THREE.DirectionalLight(0xffffff,2);light.position.set(-4,8,6);scene.add(light);
scene.add(new THREE.AmbientLight(0xffffff,.3));
const models=new THREE.Group();scene.add(models);
const hitObjects=[],wallObjects=[],raycaster=new THREE.Raycaster(),pointer=new THREE.Vector2();
let azimuth=.72,elevation=.42,zoom=1.0,viewMode='3D';
const sizes=()=>({w:project.dims.w/1000,d:project.dims.d/1000,h:project.dims.h/1000});
function view(){
 const d=sizes(),aspect=Math.max(.3,$('viewport').clientWidth/Math.max(1,$('viewport').clientHeight));
 if(viewMode==='3D'){
 activeCamera=camera;
 const r=Math.max(d.w,d.d,d.h)*1.7*zoom,t=new THREE.Vector3(0,d.h*.48,0);
 camera.position.set(r*Math.sin(azimuth)*Math.cos(elevation),t.y+r*Math.sin(elevation),
 r*Math.cos(azimuth)*Math.cos(elevation));camera.lookAt(t);camera.updateProjectionMatrix();
 }else{
 activeCamera=orthoCamera;
 let span=d.w,target=new THREE.Vector3(0,d.h/2,0),pos=new THREE.Vector3(0,d.h+8,0);
 orthoCamera.up.set(0,1,0);
 if(viewMode==='Oben'||viewMode==='Bodenplatten'){
 span=d.w;target.set(0,d.h*.45,0);pos.set(0,d.h+8,0);orthoCamera.up.set(0,0,-1);
 }else{
 span=viewMode==='SW1'||viewMode==='SW3'?d.d:d.w;
 if(viewMode==='SW1'){pos.set(d.w/2+4,d.h/2,0);target.set(-d.w/2,d.h/2,0);}
 if(viewMode==='SW3'){pos.set(-d.w/2-4,d.h/2,0);target.set(d.w/2,d.h/2,0);}
 if(viewMode==='SW2'){pos.set(0,d.h/2,d.d/2+4);target.set(0,d.h/2,-d.d/2);}
 if(viewMode==='SW4'){pos.set(0,d.h/2,-d.d/2-4);target.set(0,d.h/2,d.d/2);}
 }
 const neededHeight=Math.max((span*1.38)/aspect,
 (viewMode==='Oben'||viewMode==='Bodenplatten'?d.d:d.h)*1.35)*zoom;
 orthoCamera.left=-neededHeight*aspect/2;orthoCamera.right=neededHeight*aspect/2;
 orthoCamera.top=neededHeight/2;orthoCamera.bottom=-neededHeight/2;
 orthoCamera.position.copy(pos);orthoCamera.lookAt(target);orthoCamera.updateProjectionMatrix();
 }
}
function disposeModel(){models.traverse(obj=>{if(obj.geometry)obj.geometry.dispose();if(obj.material){const a=Array.isArray(obj.material)?obj.material:[obj.material];a.forEach(m=>{if(m.map)m.map.dispose();m.dispose();});}});models.clear();hitObjects.length=0;wallObjects.length=0;}
function material(color,opacity=1){return new THREE.MeshStandardMaterial({color,roughness:.8,metalness:.05,transparent:opacity<1,opacity,side:THREE.DoubleSide,depthWrite:opacity>=1});}
function box(w,h,d,x,y,z,col,opacity=1){const m=new THREE.Mesh(new THREE.BoxGeometry(w,h,d),material(col,opacity));m.position.set(x,y,z);models.add(m);return m;}
function createLabel(text,pos){const c=document.createElement('canvas');c.width=280;c.height=76;const ctx=c.getContext('2d');
 ctx.fillStyle='#244753';ctx.font='bold 32px Segoe UI,Arial';ctx.textAlign='center';ctx.fillText(text,140,47);
 const tex=new THREE.CanvasTexture(c),sprite=new THREE.Sprite(new THREE.SpriteMaterial({map:tex,transparent:true,depthTest:false}));
 sprite.position.copy(pos);sprite.scale.set(.5,.14,1);models.add(sprite);}

function holePosition(wid,h){
 const s=sizes(),u=clamp(h.u,0,1),y=s.h*clamp(h.v,0,1);
 if(wid==='SW1')return new THREE.Vector3(-s.w/2+.024,y,-s.d/2+s.d*u);
 if(wid==='SW2')return new THREE.Vector3(-s.w/2+s.w*u,y,-s.d/2+.024);
 if(wid==='SW3')return new THREE.Vector3(s.w/2-.024,y,-s.d/2+s.d*u);
 return new THREE.Vector3(-s.w/2+s.w*u,y,s.d/2-.024);
}
function normal(wid){return new THREE.Vector3(wid==='SW1'?1:wid==='SW3'?-1:0,0,wid==='SW2'?1:wid==='SW4'?-1:0);}
function openingRadius(h){return clamp(Number(h.dn)||70,20,300)/2000;}
function drawOpening(w,h){
 const pos=holePosition(w.id,h),n=normal(w.id),r=openingRadius(h),selected=w.id===wallId&&h.id===holeId;
 const group=new THREE.Group();group.position.copy(pos);group.quaternion.setFromUnitVectors(new THREE.Vector3(0,0,1),n);
 const circle=new THREE.Mesh(new THREE.CircleGeometry(r,30),new THREE.MeshBasicMaterial({
 color:selected?'#366f68':'#52636d',transparent:true,opacity:selected?.3:.045,depthWrite:false,side:THREE.DoubleSide}));
 group.add(circle);circle.userData={type:'hole',wall:w.id,hole:h.id};
 const ring=new THREE.Mesh(new THREE.TorusGeometry(r,.004,6,32),new THREE.MeshBasicMaterial({color:selected?'#f9cf68':'#cad6d9'}));
 ring.position.z=.004;group.add(ring);models.add(group);hitObjects.push(circle);
}
function drawHousing(key){
 const s=sizes(),e=project.enclosures[key];
 if(!project.showBox||!e.visible)return;
 const isMS=key==='MS',y0=isMS?s.h*.51:s.h*.09,y1=isMS?s.h*.77:s.h*.35,
 bw=s.w*.72,bd=s.d*.68,cz=s.d*.11;
 const op=e.opacity/100,wallColor=isMS?'#9bb2bf':'#c4b9b5';
 box(bw,.024,bd,0,y0,cz,wallColor,op);
 box(bw,.024,bd,0,y1,cz,wallColor,op);
 box(.024,y1-y0,bd,-bw/2,(y1+y0)/2,cz,wallColor,op);
 box(.024,y1-y0,bd,bw/2,(y1+y0)/2,cz,wallColor,op);
 box(bw,y1-y0,.024,0,(y1+y0)/2,cz-bd/2,wallColor,op);
 box(bw,y1-y0,.024,0,(y1+y0)/2,cz+bd/2,wallColor,op);
}
function drawWalls(){
 const s=sizes(),op=project.opacity/100;
 box(s.w+.16,.085,s.d+.16,0,-.055,0,'#a1afb7');
 const floor=new THREE.GridHelper(Math.max(s.w,s.d)*1.7,18,'#d7e1e5','#e3eaed');
 floor.position.y=-.096;models.add(floor);
 for(const w of project.walls){
 if(!w.visible||(viewMode.startsWith('SW')&&w.id!==viewMode))continue;
 const span=(w.id==='SW2'||w.id==='SW4')?s.w:s.d,half=span/2;
 const shape=new THREE.Shape();
 shape.moveTo(-half,0);shape.lineTo(half,0);shape.lineTo(half,s.h);shape.lineTo(-half,s.h);shape.closePath();
 const included=[];
 for(const h of w.holes){
 const p=holePosition(w.id,h),u=w.id==='SW2'?-p.x:w.id==='SW3'?-p.z:w.id==='SW4'?p.x:p.z,r=openingRadius(h);
 if(u-r< -half||u+r>half||p.y-r<0||p.y+r>s.h)continue;
 if(included.some(v=>Math.hypot(v.u-u,v.y-p.y)<v.r+r))continue;
 const path=new THREE.Path();path.absarc(u,p.y,r,0,Math.PI*2,true);shape.holes.push(path);
 included.push({u,y:p.y,r});
 }
 const mesh=new THREE.Mesh(new THREE.ExtrudeGeometry(shape,{depth:w.thickness/1000,bevelEnabled:false,curveSegments:12,steps:1}),
 material(w.id==='SW2'||w.id==='SW4'?'#8296a1':'#899ba4',op));
 if(w.id==='SW1'){mesh.rotation.y=-Math.PI/2;mesh.position.x=-s.w/2;}
 else if(w.id==='SW3'){mesh.rotation.y=Math.PI/2;mesh.position.x=s.w/2;}
 else if(w.id==='SW2'){mesh.rotation.y=Math.PI;mesh.position.z=-s.d/2;}
 else mesh.position.z=s.d/2;
 mesh.userData={type:'wall',wall:w.id};models.add(mesh);wallObjects.push(mesh);
 if(project.showLabels)createLabel(w.id,new THREE.Vector3(w.id==='SW1'?-s.w/2:w.id==='SW3'?s.w/2:0,
 s.h+.15,w.id==='SW2'?-s.d/2:w.id==='SW4'?s.d/2:0));
 for(const h of w.holes)drawOpening(w,h);
 }
 drawHousing('OLA');drawHousing('MS');
}
function endpoint(r){if(!r)return null;const w=project.walls.find(x=>x.id===r.wall),h=w?.holes.find(x=>x.id===r.hole);return h?holePosition(w.id,h):null;}
function drawCable(c){
 const a=endpoint(c.a),b=endpoint(c.b);if(!a||!b)return;
 if(viewMode.startsWith('SW')&&c.a.wall!==viewMode&&c.b.wall!==viewMode)return;
 const chosen=c.id===cableId,sz=sizes(),na=normal(c.a.wall),nb=normal(c.b.wall);
 const thickness=Math.max(.004,(Number(c.diameter)||12)/2000);
 const mid=a.clone().add(b).multiplyScalar(.5);
 mid.y=clamp(mid.y+.04,.18,sz.h*.85);mid.z=clamp(mid.z+.08,-sz.d/2+.18,sz.d/2-.14);
 const route=(c.route||[]).map(p=>new THREE.Vector3(p[0]/1000,p[1]/1000,p[2]/1000));
 const pts=[a.clone(),a.clone().addScaledVector(na,.12),
 ...(route.length?route:[mid]),b.clone().addScaledVector(nb,.12),b.clone()];
 const curve=new THREE.CatmullRomCurve3(pts,false,'centripetal',.45);
 const line=new THREE.Mesh(new THREE.TubeGeometry(curve,Math.min(100,48+route.length*10),chosen?thickness*1.22:thickness,8,false),
 new THREE.MeshStandardMaterial({color:c.color,roughness:.34,metalness:.04,transparent:true,opacity:chosen?1:.88}));
 line.userData={type:'cable',id:c.id};models.add(line);hitObjects.push(line);
 if(chosen){
 for(const [key,vec] of [['a',a],['b',b]]){
 const radius=Math.max(.019,thickness*2.3);
 const sphere=new THREE.Mesh(new THREE.SphereGeometry(radius,16,12),new THREE.MeshBasicMaterial({color:'#ffffff'}));
 sphere.position.copy(vec);sphere.userData={type:'handle',id:c.id,end:key};models.add(sphere);hitObjects.push(sphere);
 const outline=new THREE.Mesh(new THREE.SphereGeometry(radius*1.2,12,10),new THREE.MeshBasicMaterial({color:c.color,wireframe:true}));
 outline.position.copy(vec);models.add(outline);
 }
 for(let i=0;i<route.length;i++){
 const point=new THREE.Mesh(new THREE.SphereGeometry(Math.max(.026,thickness*2.6),16,12),
 new THREE.MeshBasicMaterial({color:'#f5c467'}));
 point.position.copy(route[i]);point.userData={type:'waypoint',id:c.id,index:i};models.add(point);hitObjects.push(point);
 }
 }
}
function rebuild(){disposeModel();drawWalls();project.cables.forEach(drawCable);view();}
function renderAll(){fixSelection();renderUI();rebuild();}
function pick(event,targets=hitObjects){const rect=$('scene').getBoundingClientRect();
 pointer.x=((event.clientX-rect.left)/rect.width)*2-1;pointer.y=-((event.clientY-rect.top)/rect.height)*2+1;
 raycaster.setFromCamera(pointer,activeCamera);return raycaster.intersectObjects(targets,false)[0]||null;}
function wallCoords(wid,p){const s=sizes(),u=(wid==='SW2'||wid==='SW4')?(p.x/s.w+.5):p.z/s.d+.5;
 return {u:clamp(u,0,1),v:clamp(p.y/s.h,0,1)};}
function newOpening(wid,point){const w=project.walls.find(x=>x.id===wid),coords=wallCoords(wid,point),id=uid(wid+'-free');
 change(()=>{w.holes.push({id,row:0,col:0,...coords});wallId=wid;holeId=id;});}
const canvas=$('scene');
canvas.addEventListener('pointerdown',e=>{
 if(e.button!==0)return;canvas.setPointerCapture(e.pointerId);const hit=pick(e),obj=hit?.object.userData||{};
 if(mode==='add'){const wallHit=pick(e,wallObjects);if(wallHit){newOpening(wallHit.object.userData.wall,wallHit.point);return;}}
 if(obj.type==='handle'){grab={type:'handle',id:obj.id,end:obj.end};return;}
 if(obj.type==='waypoint'){
 const c=project.cables.find(x=>x.id===obj.id),p=c?.route[obj.index];
 if(p){const normal=new THREE.Vector3();activeCamera.getWorldDirection(normal);
 const point=new THREE.Vector3(p[0]/1000,p[1]/1000,p[2]/1000);
 grab={type:'waypoint',id:obj.id,index:obj.index,changed:false,plane:new THREE.Plane().setFromNormalAndCoplanarPoint(normal,point)};}
 return;
 }
 if(obj.type==='hole'){wallId=obj.wall;holeId=obj.hole;renderAll();if(mode==='move'){const w=project.walls.find(x=>x.id===obj.wall),p=holePosition(obj.wall,w.holes.find(h=>h.id===obj.hole));
 grab={type:'move',wall:obj.wall,hole:obj.hole,changed:false,
 plane:new THREE.Plane().setFromNormalAndCoplanarPoint(normal(obj.wall),p)};}return;}
 if(obj.type==='cable'){cableId=obj.id;filter='all';renderAll();return;}
 if(viewMode==='3D'){grab={type:'orbit',x:e.clientX,y:e.clientY};canvas.classList.add('dragging');}
});
canvas.addEventListener('pointermove',e=>{
 if(grab?.type==='orbit'){azimuth-=(e.clientX-grab.x)*.006;elevation=clamp(elevation+(e.clientY-grab.y)*.005,-.08,1.45);grab.x=e.clientX;grab.y=e.clientY;view();return;}
 if(grab?.type==='move'){const w=project.walls.find(x=>x.id===grab.wall),h=w?.holes.find(x=>x.id===grab.hole);
 pick(e,[]);const p=new THREE.Vector3();const hit=raycaster.ray.intersectPlane(grab.plane,p);
 if(h&&hit){if(!grab.changed){undo.push(JSON.stringify(project));if(undo.length>80)undo.shift();redo.length=0;grab.changed=true;}
 Object.assign(h,wallCoords(w.id,p));rebuild();}return;}
 if(grab?.type==='waypoint'){
 const c=project.cables.find(x=>x.id===grab.id);if(!c||!c.route[grab.index])return;
 pick(e,[]);
 const p=new THREE.Vector3(),sz=sizes();
 if(raycaster.ray.intersectPlane(grab.plane,p)){
 if(!grab.changed){undo.push(JSON.stringify(project));if(undo.length>80)undo.shift();redo.length=0;grab.changed=true;}
 c.route[grab.index]=[Math.round(clamp(p.x,-sz.w/2,sz.w/2)*1000),
 Math.round(clamp(p.y,0,sz.h)*1000),Math.round(clamp(p.z,-sz.d/2,sz.d/2)*1000)];
 rebuild();
 }return;
 }
 if(grab?.type==='handle'){const hit=pick(e,hitObjects.filter(o=>o.userData.type==='hole'));
 $('tooltip').style.display=hit?'block':'none';if(hit){const o=hit.object.userData;$('tooltip').textContent=referenceText({wall:o.wall,hole:o.hole});$('tooltip').style.left=(e.offsetX+13)+'px';$('tooltip').style.top=(e.offsetY+13)+'px';}return;}
 const hit=pick(e),o=hit?.object.userData;$('tooltip').style.display=o?'block':'none';
 if(o){$('tooltip').textContent=o.type==='hole'?referenceText({wall:o.wall,hole:o.hole}):o.type==='cable'?project.cables.find(c=>c.id===o.id)?.name||'Kabel':o.type==='handle'?'Griff auf Leerrohr ziehen':'';
 $('tooltip').style.left=(e.offsetX+13)+'px';$('tooltip').style.top=(e.offsetY+13)+'px';}
});
canvas.addEventListener('pointerup',e=>{
 if(grab?.type==='handle'){const hit=pick(e,hitObjects.filter(o=>o.userData.type==='hole'));
 if(hit){const o=hit.object.userData,c=project.cables.find(x=>x.id===grab.id);if(c)change(()=>{c[grab.end]={wall:o.wall,hole:o.hole};});}}
 if((grab?.type==='move'||grab?.type==='waypoint')&&grab.changed){renderAll();persist();}
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
$('applyHole').addEventListener('click',()=>{const h=hole();if(!h)return;const span=wallSpanMm(wallId);
 change(()=>{h.u=clamp((Number($('holeU').value)||0)/span,0,1);h.v=clamp((Number($('holeV').value)||0)/project.dims.h,0,1);
 h.dn=clamp(Number($('holeDn').value)||h.dn,20,300);});});
$('cableList').addEventListener('click',e=>{const card=e.target.closest('[data-cable]');if(card){cableId=card.dataset.cable;renderAll();}});
$('cableFilters').addEventListener('click',e=>{const b=e.target.closest('[data-filter]');if(b){filter=b.dataset.filter;renderUI();}});
$('addCable').addEventListener('click',()=>change(()=>{const n=project.cables.length+1,id=uid('c');
 project.cables.push({id,name:'Kabel '+n,color:COLORS[(n-1)%COLORS.length],group:'Weitere',diameter:12,route:[],a:null,b:null});
 cableId=id;filter='all';}));
$('deleteCable').addEventListener('click',()=>{if(!cable())return;change(()=>{project.cables=project.cables.filter(c=>c.id!==cableId);cableId=project.cables[0]?.id||null;});});
$('swapCable').addEventListener('click',()=>{const c=cable();if(c)change(()=>{const t=c.a;c.a=c.b;c.b=t;});});
$('cableName').addEventListener('change',()=>{const c=cable();if(c)change(()=>c.name=$('cableName').value.trim().slice(0,80)||'Kabel');});
$('cableColor').addEventListener('change',()=>{const c=cable();if(c)change(()=>{c.color=$('cableColor').value;});});
$('cableGroup').addEventListener('change',()=>{const c=cable();if(c)change(()=>{c.group=$('cableGroup').value;});});
$('cableDiameter').addEventListener('change',()=>{const c=cable();if(c)change(()=>{c.diameter=clamp(Number($('cableDiameter').value)||12,2,80);});});
$('addWaypoint').addEventListener('click',()=>{const c=cable();if(!c)return;
 const a=endpoint(c.a),b=endpoint(c.b),sz=sizes();if(!a||!b){alert('Bitte zuerst die Anschlüsse A und B auswählen.');return;}
 const vec=a.clone().lerp(b,.5);vec.y=clamp(vec.y+(.15),.05,sz.h-.05);
 change(()=>{if(c.route.length>=12){alert('Maximal 12 manuelle Bogenpunkte je Kabel.');return;}
 c.route.push([Math.round(vec.x*1000),Math.round(vec.y*1000),Math.round(vec.z*1000)]);});});
$('clearWaypoints').addEventListener('click',()=>{const c=cable();if(c&&c.route.length)change(()=>{c.route=[];});});
$('waypointList').addEventListener('change',e=>{const input=e.target.closest('[data-axis]');if(!input)return;
 const c=cable(),i=Number(input.closest('[data-index]').dataset.index),axis=Number(input.dataset.axis);
 if(!c||!c.route[i])return;const extent=axis===1?project.dims.h:(axis===0?project.dims.w/2:project.dims.d/2);
 change(()=>{c.route[i][axis]=clamp(Number(input.value)||0,axis===1?0:-extent,extent);});});
$('waypointList').addEventListener('click',e=>{const btn=e.target.closest('[data-remove]');if(!btn)return;
 const c=cable(),i=Number(btn.dataset.remove);if(c&&c.route[i])change(()=>{c.route.splice(i,1);});});
for(const k of ['ola','ms']){const key=k.toUpperCase();
 $(k+'Visible').addEventListener('change',()=>change(()=>{project.enclosures[key].visible=$(k+'Visible').checked;}));
 $(k+'Opacity').addEventListener('input',()=>{$(k+'OpacityText').textContent=$(k+'Opacity').value+' %';});
 $(k+'Opacity').addEventListener('change',()=>change(()=>{project.enclosures[key].opacity=Number($(k+'Opacity').value);}));
}
for(const [el,key,min,max] of [['wallThickness','thickness',40,500],['wallDn','dn',20,300]])
 $(el).addEventListener('change',()=>change(()=>{wall()[key]=clamp(Number($(el).value)||wall()[key],min,max);}));

for(const [field,key] of [['cableA','a'],['cableB','b']])$(field).addEventListener('change',()=>{const c=cable();if(c)change(()=>{c[key]=parseEndpoint($(field).value);});});
$('projectName').addEventListener('change',()=>change(()=>project.name=$('projectName').value.trim().slice(0,80)||'Neues Projekt'));
for(const [el,key,min,max] of [['width','w',600,12000],['depth','d',600,12000],['height','h',600,8000]])
 $(el).addEventListener('change',()=>{const x=clamp(Number($(el).value)||project.dims[key],min,max);change(()=>{project.dims[key]=x;});});
$('opacity').addEventListener('change',()=>change(()=>project.opacity=Number($('opacity').value)));
$('opacity').addEventListener('input',()=>{$('opacityText').textContent=$('opacity').value+' %';});
$('showBox').addEventListener('change',()=>change(()=>project.showBox=$('showBox').checked));
$('showLabels').addEventListener('change',()=>change(()=>project.showLabels=$('showLabels').checked));
$('resetView').addEventListener('click',()=>{azimuth=.72;elevation=.42;zoom=1;view();});
$('viewTabs').addEventListener('click',e=>{
 const b=e.target.closest('[data-view]');if(!b)return;
 viewMode=b.dataset.view;zoom=1;renderAll();
});
$('undo').addEventListener('click',()=>undoRedo(true));$('redo').addEventListener('click',()=>undoRedo(false));
document.addEventListener('keydown',e=>{if((e.ctrlKey||e.metaKey)&&e.key.toLowerCase()==='z'){e.preventDefault();undoRedo(!e.shiftKey);}else if((e.ctrlKey||e.metaKey)&&e.key.toLowerCase()==='y'){e.preventDefault();undoRedo(false);}});
function download(name,data,type){const blob=data instanceof Blob?data:new Blob([data],{type}),href=URL.createObjectURL(blob),a=document.createElement('a');a.href=href;a.download=name;document.body.append(a);a.click();a.remove();setTimeout(()=>URL.revokeObjectURL(href),1500);}
function filename(){return project.name.replace(/[^\p{L}\p{N}_-]+/gu,'_').slice(0,50)||'Schachtwand';}
$('saveProject').addEventListener('click',()=>download(filename()+'.json',JSON.stringify(project,null,2),'application/json'));
$('openProject').addEventListener('click',()=>$('fileInput').click());
$('fileInput').addEventListener('change',async e=>{const f=e.target.files?.[0];if(!f)return;
 try{const next=validProject(JSON.parse(await f.text()));change(()=>{project=next;wallId='SW1';holeId=null;cableId=project.cables[0]?.id||null;});}
 catch(ex){alert('Projektdatei konnte nicht geladen werden: '+ex.message);}e.target.value='';});
$('newProject').addEventListener('click',()=>{if(!confirm('Neue leere Planung starten? Die vorherige Planung bleibt über Rückgängig erreichbar.'))return;
 change(()=>{project=demo();project.name='Neue Schachtwandplanung';project.cables=[];
 project.walls.forEach(w=>{w.holes=[];w.rows=3;w.cols=4;});
 wallId='SW1';holeId=null;cableId=null;filter='all';viewMode='3D';});});
$('exportPng').addEventListener('click',()=>{renderer.render(scene,activeCamera);renderer.domElement.toBlob(blob=>{if(blob)download(filename()+'.png',blob,'image/png');},'image/png');});
function resize(){const rect=$('viewport').getBoundingClientRect();renderer.setSize(Math.max(10,rect.width),Math.max(10,rect.height),false);camera.aspect=rect.width/Math.max(1,rect.height);camera.updateProjectionMatrix();view();}
new ResizeObserver(resize).observe($('viewport'));
function animate(){requestAnimationFrame(animate);renderer.render(scene,activeCamera);}
renderAll();persist();resize();animate();
})();