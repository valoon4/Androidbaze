/* Kabelsalat example projects – synthetic demonstrations, no real project data.
   Browser and Node compatible. Each example is generated deterministically. */
(function(root,factory){
 const api=factory(root?.KabelsalatComponents||null,root?.KabelsalatNetwork||null);
 if(typeof module==='object'&&module.exports)module.exports={factory};
 if(root)root.KabelsalatSamples=api;
})(typeof window!=='undefined'?window:null,function(components,network){
'use strict';
const referenceModule=typeof globalThis!=='undefined'?globalThis.KabelsalatReferences:null;
const manifest=[
 {id:'reference',name:'1 · Referenzschacht 13_T_021 (DEMO)',description:'Vier Wände, 94 Leerrohre, 16 farbige Kabel, OLA/MS, Konsolen und Variante'},
 {id:'station',name:'2 · Bahnhofsnetz mit 4 Schächten (DEMO)',description:'Vier Schächte, 64 Kabel, Netzverbindungen und drei schachtübergreifende Kabelwege'},
 {id:'quality',name:'3 · Prüffall mit absichtlichen Fehlern (DEMO)',description:'Überschneidungen, unvollständiges Kabel und Geometriehinweise zum Nachvollziehen'},
 {id:'diameters',name:'4 · LR-Größen DN 30–160 (FIKTIVE DEMO)',description:'Alle Leerrohre unterschiedlich groß: DN 30, 50, 60, 110, 116, 160 und weitere; keine realen Bestandsmaße'},
 ...(referenceModule?.manifest||[])
];
const groups=[
 ['OLA-001','OLA','#e84b60'],['OLA-002','OLA','#ff913e'],['OLA-003','OLA','#d85470'],
 ['MS-001','MS','#1d96c1'],['MS-002','MS','#4f8fe7'],['MS-003','MS','#367cc5'],
 ['LST-004','Weitere','#27a77b'],['LST-005','Weitere','#70bd55'],
 ['TK-006','Weitere','#9860d2'],['TK-007','Weitere','#ba84d7'],
 ['RESERVE-008','Weitere','#ae9939'],['TK-009','Weitere','#44a9b0'],
 ['LST-010','Weitere','#c76f56'],['TK-011','Weitere','#a86bce'],
 ['MS-004','MS','#3e8cb4'],['OLA-004','OLA','#e35c76']
];
const refs=[
 ['SW1',1,1,'SW3',2,4],['SW1',2,2,'SW4',1,5],['SW2',3,4,'SW3',1,3],
 ['SW1',3,3,'SW3',4,2],['SW4',2,2,'SW2',1,5],['SW1',4,4,'SW3',3,4],
 ['SW1',2,5,'SW2',3,2],['SW3',2,5,'SW4',2,4],['SW2',2,2,'SW4',3,5],
 ['SW1',3,6,'SW3',2,1],['SW4',1,2,'SW2',4,6],['SW1',1,5,'SW4',2,3],
 ['SW1',3,1,'SW3',1,5],['SW2',1,1,'SW4',1,3],
 ['SW1',4,6,'SW3',3,2],['SW2',4,3,'SW4',2,6]
];
const clone=o=>JSON.parse(JSON.stringify(o));
const grid=wall=>{
 const holes=[];
 for(let row=1;row<=wall.rows;row++)for(let col=1;col<=wall.cols;col++)
  holes.push({id:wall.id+'-r'+row+'-o'+col,row,col,
   u:col/(wall.cols+1),v:1-row/(wall.rows+1),dn:wall.dn});
 return holes;
};
function shaft(id){
 if(!components||!network)throw Error('Beispielmodule wurden nicht korrekt geladen');
 const dims={w:2800,d:2200,h:2550};
 const spec=[['SW1',4,6,100,90],['SW2',4,7,110,90],
 ['SW3',4,6,120,110],['SW4',3,6,100,90]];
 const walls=spec.map(([key,rows,cols,thickness,dn])=>{
 const w={id:key,name:key,rows,cols,thickness,dn,visible:true};w.holes=grid(w);return w;
 });
 const enclosures=components.defaults(dims);
 for(const key of ['OLA','MS']){
  enclosures[key].opacity=42;
  for(const p of enclosures[key].panels){p.opacity=55;p.thickness=key==='OLA'?40:20;
   p.material=key==='OLA'?'Beton (DEMO)':'Stahl (DEMO)';}
 }
 const supports=[
 {id:id+'-K01',name:'Konsole OLA A',type:'Konsole',wall:'SW1',
  x:-1150,y:580,z:-700,w:320,h:80,d:240,visible:true},
 {id:id+'-K02',name:'Auflager MS B',type:'Auflager',wall:'SW3',
  x:1120,y:1600,z:430,w:350,h:120,d:200,visible:true},
 {id:id+'-K03',name:'Konsole TK',type:'Konsole',wall:'SW2',
  x:440,y:1080,z:-890,w:300,h:80,d:220,visible:true}
 ];
 const cables=groups.map(([name,group,color],i)=>{
  const [wa,ra,ca,wb,rb,cb]=refs[i];
  let route=[];
  if(i%3===0)route=[[-300+(i%4)*180,730+i*95,-200+(i%3)*100]];
  if(i%5===0)route=[[-400,540+i*85,100],[450,750+i*75,-250]];
  return {id:id+'-'+name,name:name+' · Segment '+id,group,color,
   diameter:group==='MS'?22:group==='OLA'?18:12,route,
   a:{wall:wa,hole:wa+'-r'+ra+'-o'+ca},
   b:{wall:wb,hole:wb+'-r'+rb+'-o'+cb}};
 });
 return {version:5,name:'DEMO '+id+' · fiktiver Kabelschacht',dims,walls,cables,
 opacity:29,showBox:true,showLabels:true,splitEnabled:true,
 enclosures,supports,variants:[]};
}
function assemble(project,shafts,links=[],routes=[],selected=shafts[0].id){
 const net={version:1,shafts,links,routes,
 quality:{minPipeGapMm:20,minEdgeMm:40,minBendRadiusMm:null},reviews:[],audit:[]};
 return {...project,version:5,activeShaftId:selected,network:net};
}
function create(id){
 if(id==='source-t021'||id==='source-u028'||id==='source-both'||id==='pdf-u028'){
  if(!referenceModule)throw Error('Referenzdaten fehlen: reference-data.js');
  return referenceModule.create(id);
 }
 if(!manifest.some(m=>m.id===id))throw Error('Unbekanntes Beispiel '+id);
 if(id==='diameters'){
  // Explicitly SYNTHETIC size range. Never alter PDF/HTML references to achieve
  // a prettier render. Each LR keeps its ID and coordinate.
  const p=shaft('DN-DEMO');
  p.name='FIKTIVE DEMO · LR-Durchmesser DN 30–160 (keine Bestandsmaße)';
  const sizes=[30,40,50,60,63,75,90,110,116,125,140,160];
  for(let wi=0;wi<p.walls.length;wi++)p.walls[wi].holes.forEach((h,i)=>{
   h.dn=sizes[(i+wi*3)%sizes.length];
   // No physical openingMm: DN is just a labeled visual proxy.
  });
  p.opacity=70;
  const entry={id:'DN-DEMO',name:p.name,x:0,z:0,snapshot:network.snapshot(p)};
  const result=assemble(p,[entry]);
  result.network.quality={minPipeGapMm:null,minEdgeMm:null,minBendRadiusMm:null};
  return result;
 }
 if(id==='reference'){
  const p=shaft('13_T_021');p.name='DEMO Referenzschacht 13_T_021 · KEINE REALDATEN';
  const before=components.snapshot({...p,showBox:false,supports:p.supports.slice(0,2)});
  p.variants=[{id:'V-001',name:'Variante A · Einhausung ausgeblendet',
   createdAt:'2026-11-05T00:00:00Z',snapshot:before}];
  const netShaft={id:'S1',name:p.name,x:0,z:0,snapshot:network.snapshot(p)};
  const result=assemble(p,[netShaft]);
  result.network.reviews=[{shaftId:'S1',entityType:'shaft',entityId:'S1',
   status:'Entwurf',source:'Synthetisches Muster, keine Bestandsdaten',
   checkedBy:'Demo',checkedAt:'2026-11-05T00:00:00Z',
   note:'Nur zur Bedienungsdemonstration, nicht für echte Bahnhofsprojekte.'}];
  return result;
 }
 if(id==='station'){
  const ids=['S01','S02','S03','S04'],pos=[[0,0],[12000,3000],[24500,0],[37000,5600]];
  const data=ids.map(shaft);
  const nodes=data.map((p,i)=>({id:ids[i],name:p.name,x:pos[i][0],z:pos[i][1],snapshot:network.snapshot(p)}));
  const links=[
   {id:'L-01',from:'S01',to:'S02',note:'DEMO Verbindung A'},
   {id:'L-02',from:'S02',to:'S03',note:'DEMO Verbindung B'},
   {id:'L-03',from:'S03',to:'S04',note:'DEMO Verbindung C'},
   {id:'L-04',from:'S02',to:'S04',note:'DEMO Abzweig'}
  ];
  const steps=(parts,code)=>parts.map(shaftId=>({shaftId,cableId:shaftId+'-'+code}));
  const routes=[
   {id:'NET-OLA-001',name:'OLA-001 · fiktive Gesamttrasse',group:'OLA',
    steps:steps(ids,'OLA-001')},
   {id:'NET-MS-001',name:'MS-001 · fiktive Gesamttrasse',group:'MS',
    steps:steps(ids,'MS-001')},
   {id:'NET-TK-007',name:'TK-007 · fiktiver Abzweig',group:'Weitere',
    steps:steps(['S01','S02','S04'],'TK-007')}
  ];
  const result=assemble(data[0],nodes,links,routes,'S01');
  result.name='DEMO Bahnhof · Schacht S01 (fiktiv)';
  result.network.shafts[0].name=result.name;
  result.network.shafts[0].snapshot=network.snapshot(result);
  result.network.reviews=[{shaftId:'S01',entityType:'shaft',entityId:'S01',
   status:'Entwurf',source:'Generierte DEMO-Daten, keine Vermessung',
   checkedBy:'Demo',checkedAt:'2026-11-05T00:00:00Z',
   note:'Die verbundenen Trassen sind nur schematisch zugeordnet.'}];
  return result;
 }
 const p=shaft('CHECK');p.name='DEMO Prüffall · absichtlich ungültige Geometrie';
 p.walls[0].holes.push({id:'SW1-BAD-EDGE',row:0,col:0,u:.005,v:.5,dn:110});
 p.walls[0].holes.push({id:'SW1-BAD-OVERLAP',row:0,col:0,u:1/7+.002,v:1-1/5,dn:90});
 p.cables[0].diameter=80;p.walls[0].holes[0].dn=40;p.cables[1].b=null;
 const node={id:'S1',name:p.name,x:0,z:0,snapshot:network.snapshot(p)};
 const result=assemble(p,[node]);result.network.quality={
 minPipeGapMm:70,minEdgeMm:50,minBendRadiusMm:500};
 result.network.reviews=[{shaftId:'S1',entityType:'shaft',entityId:'S1',
  status:'Zur Prüfung',source:'Synthetischer Prüffall',checkedBy:'Demo',
  checkedAt:'2026-11-05T00:00:00Z',
  note:'Kollisionen und ein fehlender Kabelanschluss sind ABSICHTLICH.'}];
 return result;
}
return {manifest,create};
});
