'use strict';
/* KAB-0505..0522: UTF-8/BOM, semicolon, units, IDs, geometry, all-or-nothing. */
const assert=require('node:assert/strict'),fs=require('node:fs'),path=require('node:path');
const csv=require('../csv-data.js');
const fixture=()=>({version:5,name:'FIKTIVE CSV PROBE',
 dims:{w:2400,d:1900,h:2100},
 walls:[],cables:[],activeShaftId:'S1',
 network:{version:1,shafts:[
  {id:'S1',name:'Fiktiver Schacht',x:100,z:200,y:null,snapshot:{
   dims:{w:2400,d:1900,h:2100},
   walls:[{id:'SW1',thickness:120,holes:[
    {id:'H1',u:.2,v:.4,dn:50},{id:'H2',u:.8,v:.4,dn:110}]},
    {id:'SW2',thickness:100,holes:[]}],
   cables:[{id:'C1',name:'OLA / Probekabel',group:'OLA',diameter:12,
    a:{wall:'SW1',hole:'H1'},b:{wall:'SW1',hole:'H2'},route:[]}],
   supports:[]}},
  {id:'S2',name:'Fiktiver Nachbar',x:5000,z:0,snapshot:{
   dims:{w:2400,d:1900,h:2100},
   walls:[{id:'SW1',thickness:120,holes:[]},{id:'SW3',thickness:110,holes:[]}],
   cables:[]}}],
  links:[{id:'L',from:'S1',to:'S2'}],routes:[],
  sources:{documents:[{id:'D1',title:'Fixture',version:'R1',
   kind:'manual',file:'',pages:[],date:'',capturedAt:''}],
   entries:[],conflicts:[],markers:[]}}});
const p=fixture(),before=JSON.stringify(p);
assert.deepEqual(Object.keys(csv.TYPES),['positions','walls','holes','cables']);
assert.ok(csv.exportCsv(p,'holes').startsWith('\uFEFFshaft_id;wall_id;hole_id'));
assert.ok(csv.exportCsv(p,'positions').includes('S1;100;;200;'));
assert.equal(csv.parseCsv(csv.exportCsv(p,'positions'),'positions').rows[0].y_mm,'');
assert.equal(csv.parseCsv(csv.exportCsv(p,'walls'),'walls').rows.length,4);
assert.equal(csv.parseCsv(csv.exportCsv(p,'cables'),'cables').rows.length,1);
assert.equal(csv.parseCsv(csv.exportCsv(p,'holes'),'holes').rows.length,2);
assert.equal(csv.parseCsv(csv.exportCsv(p,'holes'),'holes').delimiter,';');
assert.equal(csv.parseCsv(csv.exportCsv(p,'holes').replace(/;/g,','),'holes').delimiter,',');
for(const kind of Object.keys(csv.TYPES)){
 const round=csv.preview(p,kind,csv.exportCsv(p,kind));
 assert.equal(round.ok,true,'Export/import schema must parse: '+kind);
 assert.equal(round.errors.length,0);
 assert.equal(JSON.stringify(p),before,'Preview must not mutate source: '+kind);
 assert.deepEqual(csv.apply(p,round).network.sources,p.network.sources,
  'Unrelated source registry must survive: '+kind);
}
const holes='shaft_id;wall_id;hole_id;x_mm;y_mm;dn_mm;opening_mm;opening_type\n'+
 'S1;SW1;BARE-1;600;800;;75;bare-hole\n'+
 'S2;SW3;CONDUIT-1;600;900;110;120;conduit\n';
const plan=csv.preview(p,'holes',holes,{source:'fiktiver-messplan.csv'});
assert.ok(plan.ok,plan.errors.map(x=>x.message).join(';'));
assert.equal(plan.changes.length,2);
const added=csv.apply(p,plan);
assert.equal(added.network.shafts[0].snapshot.walls[0].holes.length,3);
const naked=added.network.shafts[0].snapshot.walls[0].holes.find(h=>h.id==='BARE-1');
assert.equal(naked.openingType,'bare-hole');
assert.equal(naked.dimensionSpec.openingMm,75);
assert.equal(naked.dimensionSpec.nominalMm,null);
assert.equal(naked.dimensionSpec.status,'unverified');
assert.equal(naked.dimensionSpec.source,'fiktiver-messplan.csv');
assert.equal(added.network.shafts[0].snapshot.cables[0].a.hole,'H1');
assert.equal(JSON.stringify(p),before);
assert.equal(csv.parseCsv(csv.exportCsv(added,'holes'),'holes').rows.find(x=>x.hole_id==='BARE-1').dn_mm,'');
const moved='shaft_id;x_mm;y_mm;z_mm;crs\nS1;10,5;;2,5;EPSG:LOCAL\n';
const pos=csv.preview(p,'positions',moved,{unit:'m'});
assert.equal(pos.ok,true);
const coords=csv.apply(p,pos);
assert.equal(coords.network.shafts[0].x,10500);
assert.equal(coords.network.shafts[0].z,2500);
assert.equal(coords.network.shafts[0].y,null,'Unknown height must not become zero');
assert.equal(coords.network.coordinateReference,'EPSG:LOCAL');
assert.equal(p.network.coordinateReference,undefined,'No modification before commit');
assert.throws(()=>csv.apply({...p,name:'Other'},plan),/veraltet/);
const misfit=csv.preview(p,'holes',holes.replace('600;800;;75','20;800;;75'));
assert.equal(misfit.ok,false);
assert.match(misfit.errors[0].message,/Zeile 2/);
assert.throws(()=>csv.apply(p,misfit),/Keine gültige/);
assert.equal(JSON.stringify(p),before);
const badSource=fixture();
badSource.network.shafts[0].snapshot.sourceInfo={sourceVersion:'PDF V2'};
assert.equal(csv.preview(badSource,'holes',holes).ok,false);
assert.equal(csv.preview(badSource,'positions',moved,{unit:'m'}).ok,true);
const badIDs=csv.preview(p,'holes',holes.replace('S2;SW3','NOPE;SW3'));
assert.equal(badIDs.ok,false);
assert.ok(badIDs.errors.some(x=>x.message.includes('Unbekannte Schacht-ID')));
const duplicate=csv.preview(p,'holes',holes+'S1;SW1;BARE-1;650;900;;75;bare-hole\n');
assert.equal(duplicate.ok,false);
assert.ok(duplicate.errors.some(x=>x.message.includes('Doppelte Objekt-ID')));
const crossing=csv.preview(p,'holes',holes.replace('600;800;;75','400;840;;160'));
assert.equal(crossing.ok,false,'Hole overlaps existing H1, no silent merge');
const wallChanges='shaft_id;wall_id;thickness_mm;length_mm;height_mm\nS1;SW1;140;1900;2100\n';
assert.equal(csv.apply(p,csv.preview(p,'walls',wallChanges)).network.shafts[0].snapshot.walls[0].thickness,140);
assert.equal(csv.preview(p,'walls',wallChanges.replace('1900;2100','1800;2100')).ok,false);
const cableHead='shaft_id;cable_id;name;group;diameter_mm;a_wall;a_hole;b_wall;b_hole\n';
const cableText=cableHead+'S1;C1;"Neuer; Name ""Zitat""";MS;12;SW1;H1;SW1;H2\n';
const cablePlan=csv.preview(p,'cables',cableText);
assert.ok(cablePlan.ok,cablePlan.errors.map(x=>x.message).join('\n'));
const changed=csv.apply(p,cablePlan).network.shafts[0].snapshot.cables[0];
assert.equal(changed.name,'Neuer; Name "Zitat"');
assert.equal(changed.group,'MS');
assert.equal(changed.id,'C1');
assert.equal(changed.reviewStatus,'Zur Prüfung');
assert.equal(csv.preview(p,'cables',cableHead+'S1;C1;Oops;MS;12;SW1;MISSING;SW1;H2\n').ok,false);
assert.equal(csv.preview(p,'cables',cableHead+'S1;C1;Oops;MS;12;SW5;H1;SW1;H2\n').ok,false);
assert.equal(csv.preview(p,'cables',cableHead+'S1;C1;Oops;MS;12;SW1;;SW1;H2\n').ok,false);
assert.throws(()=>csv.parseCsv('shaft_id;x_mm;z_mm','positions'),/Spalte fehlt/);
assert.throws(()=>csv.parseCsv('shaft_id;x_mm;y_mm;z_mm;crs\n"S1;1;2;3;x','positions'),/ungeschlossener/);
assert.throws(()=>csv.parseCsv('shaft_id;x_mm;y_mm;z_mm;crs\nS1;1;2;3;x;extra','positions'),/Spalten erwartet/);
assert.throws(()=>csv.preview(p,'holes',holes,{unit:'inch'}),/Einheit/);
const csvDoc=fs.readFileSync(path.join(__dirname,'../index.html'),'utf8');
const csvApp=fs.readFileSync(path.join(__dirname,'../app.js'),'utf8');
for(const id of ['csvFile','csvPaste','csvKind','csvUnit','csvSource',
 'csvPreview','csvApply','csvExport','csvStatus'])
 assert.ok(csvDoc.includes('id="'+id+'"'),'CSV UI field '+id+' missing');
assert.ok(csvDoc.includes('csv-data.js?v='));
assert.ok(csvApp.includes('csvModel.preview(project'));
assert.ok(csvApp.includes('csvModel.apply(project,csvPlan)'));
assert.ok(csvApp.includes('csvModel.exportCsv(project,kind)'));
console.log('CSV GIANT OK: 18 data-contract regressions incl typed bore, sources, BOM, quotes, decimal commas, no partial writes and stale plans');
