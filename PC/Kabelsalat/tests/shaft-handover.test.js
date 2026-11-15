'use strict';
/* Bro's 400 shaft handover: deterministic 400+ routes, no topology hallucinations. */
const assert=require('node:assert/strict'),fs=require('node:fs'),path=require('node:path');
const handover=require('../shaft-handover.js');
const shafts='schacht_id;schachtname\n'+Array.from({length:400},(_,i)=>
 'S'+String(i+1).padStart(4,'0')+';Schacht '+(i+1)).join('\n')+'\n';
const routeRows=[
 'K01;Hauptkabel;S0001;1;Plan 1 S. 3;PLAN',
 'K01;Hauptkabel;S0002;2;Plan 1 S. 4;PLAN',
 'K01;Hauptkabel;S0003;3;Plan 1 S. 5;KANDIDAT',
 'K02;Zweiter Strang;S0003;1;Vor-Ort-Liste;VOR_ORT',
 'K02;Zweiter Strang;S0004;2;Vor-Ort-Liste;VOR_ORT'
];
const header='kabel_id;kabelname;schacht_id;reihenfolge;quelle;status\n';
const routes=header+routeRows.join('\n')+'\n',before=[shafts,routes].join('\u0000');
const result=handover.analyze(shafts,routes);
assert.equal(result.ok,true);
assert.equal(result.summary.shafts,400);
assert.equal(result.summary.cables,2);
assert.equal(result.summary.associations,5);
assert.equal(result.summary.withoutEvidence,396);
assert.equal(result.summary.physicallyVerified,0);
assert.equal(result.cables[0].shaftPath,'S0001 > S0002 > S0003');
assert.equal(result.cables[0].steps[1].shaftId,'S0002');
assert.equal(result.handover.find(x=>x.schacht_id==='S0002').rolle,'Durchgang laut Liste');
assert.equal(result.handover.find(x=>x.schacht_id==='S0001').rolle,'Erster dokumentierter Schacht');
assert.equal(result.handover.find(x=>x.schacht_id==='S0003'&&x.kabel_id==='K01').rolle,
 'Letzter dokumentierter Schacht');
assert.equal(result.shafts.find(x=>x.id==='S0003').cables.length,2);
assert.equal(result.shafts.find(x=>x.id==='S0009').cables.length,0);
assert.ok(result.open.some(q=>q.schacht_id==='S0009'&&q.frage.includes('Keine Kabelzuordnung')));
assert.ok(result.open.some(q=>q.schacht_id==='S0003'&&q.frage.includes('Kandidaten')));
assert.ok(result.handover.every(x=>x.hinweis.includes('Keine automatisch bestätigte')));
assert.equal(shafts+'\u0000'+routes,before,'No source mutation');
const exportCable=handover.exportHandover(result);
assert.ok(exportCable.startsWith('\uFEFFkabel_id;kabelname'));
assert.equal(handover.readCsv(exportCable,[
 'kabel_id','kabelname','schacht_id','reihenfolge','rolle',
 'quelle','status','schachtfolge','hinweis'],10000).length,5);
const exportShaft=handover.exportByShaft(result);
assert.equal(handover.readCsv(exportShaft,['schacht_id','schachtname',
 'kabelanzahl','kabel_ids','hinweis'],10000).length,400);
assert.ok(exportShaft.includes('"S0009"'));
const exportOpen=handover.exportOpen(result);
assert.ok(exportOpen.includes('Keine Kabelzuordnung eingetragen'));
assert.ok(exportOpen.includes('Kandidatenzuordnung'));
const filtered=handover.analyze(shafts,routes,{filter:'K01'});
assert.equal(filtered.filtered.length,3);
assert.equal(filtered.summary.shafts,400);
assert.equal(handover.analyze(shafts,routes,{filter:'S0003'}).filtered.length,2);
const onlyShaft=handover.analyze(shafts,'');
assert.equal(onlyShaft.ok,true);
assert.equal(onlyShaft.handover.length,0);
assert.equal(onlyShaft.open.length,400);
assert.equal(onlyShaft.summary.withoutEvidence,400);
assert.equal(onlyShaft.summary.cables,0);
const duplicate=handover.analyze(shafts,routes+routeRows[0]+'\n');
assert.equal(duplicate.ok,false);
assert.match(duplicate.errors.join('; '),/doppelte Kabel-\/Schachtzuordnung/);
assert.throws(()=>handover.exportHandover(duplicate),/Keine gültige/);
const orphan=handover.analyze(shafts,header+'K03;X;S0999;1;Plan;PLAN\n');
assert.equal(orphan.ok,false);
assert.match(orphan.errors.join('; '),/nicht in Schachtliste/);
const unsourced=handover.analyze(shafts,header+'K03;X;S0008;1;;PLAN\n');
assert.equal(unsourced.ok,false);
assert.match(unsourced.errors.join('; '),/Quellenangabe/);
const badStatus=handover.analyze(shafts,header+'K03;X;S0008;1;Plan;FREIGEGEBEN\n');
assert.equal(badStatus.ok,false);
assert.match(badStatus.errors.join('; '),/Status muss/);
const missingOrder=handover.analyze(shafts,header+'K03;X;S0008;;Plan;PLAN\n');
assert.equal(missingOrder.ok,false);
assert.match(missingOrder.errors.join('; '),/Reihenfolge/);
const collision=handover.analyze(shafts,header+
 'K03;X;S0008;1;Plan;PLAN\nK03;X;S0009;1;Plan;PLAN\n');
assert.equal(collision.ok,false);
assert.match(collision.errors.join('; '),/Reihenfolge 1 mehrfach/);
const broken=handover.analyze(shafts,header+
 'K03;X;S0008;1;Plan;PLAN\nK03;X;S0009;3;Plan;PLAN\n');
assert.equal(broken.ok,true);
assert.match(broken.warnings.join('; '),/Lücke in Reihenfolge/);
const single=handover.analyze(shafts,header+'K04;Einzeln;S0002;1;Plan;PLAN\n');
assert.equal(single.ok,true);
assert.match(single.handover[0].rolle,/Einzelzuordnung/);
assert.ok(single.open.some(x=>x.frage.includes('Verlauf ungeklärt')));
assert.throws(()=>handover.readCsv('schacht_id;schachtname\n"S0001;Name',handover.shaftHeaders,600),
 /Nicht geschlossenes/);
assert.throws(()=>handover.readCsv('schacht_id;schachtname\nS1;Name;extra',
 handover.shaftHeaders,600),/Spalten erwartet/);
assert.throws(()=>handover.analyze('schacht_id;schachtname\n'+
 Array.from({length:501},(_,i)=>'S'+i+';N'+i).join('\n'),''),/Mehr als 500/);
assert.equal(handover.MAX_SHAFTS>=400,true);
const escaped='schacht_id;schachtname\nS0001;"Nord; Eingang ""A"""\n';
const escapedResult=handover.analyze(escaped,header+
 'K01;"Kabel; Spezial";S0001;1;"Plan; S. 2";PLAN\n');
assert.equal(escapedResult.ok,true);
assert.equal(escapedResult.shafts[0].name,'Nord; Eingang "A"');
assert.equal(escapedResult.handover[0].quelle,'Plan; S. 2');
const longRoutes=header+Array.from({length:400},(_,i)=>
 'K100;Langer Weg;S'+String(i+1).padStart(4,'0')+';'+(i+1)+
 ';Planseite '+(i+1)+';PLAN').join('\n')+'\n';
const longReport=handover.analyze(shafts,longRoutes);
assert.equal(longReport.ok,true);
assert.equal(longReport.summary.associations,400);
assert.equal(longReport.handover[399].rolle,'Letzter dokumentierter Schacht');
assert.equal(longReport.handover[188].rolle,'Durchgang laut Liste');
const project={network:{shafts:[
 {id:'A1',name:'Probe A'},{id:'A2',name:'Probe B'}],
 routes:[{id:'R1',name:'Modellroute',steps:[
 {shaftId:'A1',cableId:'local-1'},{shaftId:'A2',cableId:'local-2'}]}]}};
const seeded=handover.fromProject(project),seedReport=
 handover.analyze(seeded.shaftCsv,seeded.passageCsv);
assert.equal(seedReport.ok,true);
assert.equal(seedReport.summary.shafts,2);
assert.equal(seedReport.summary.cables,1);
assert.equal(seedReport.summary.physicallyVerified,0);
assert.ok(seedReport.handover[0].quelle.includes('schematische Route'));
const malicious=handover.analyze(
 'schacht_id;schachtname\nS1;=HYPERLINK(1)\n',
 header+'K1;@cmd;S1;1;=cmd;PLAN\n');
assert.equal(malicious.ok,true);
const safeHandover=handover.exportHandover(malicious);
const safeShaft=handover.exportByShaft(malicious);
assert.ok(safeHandover.includes("'@cmd"));
assert.ok(safeHandover.includes("'=cmd"));
assert.ok(safeShaft.includes("'=HYPERLINK"));
assert.ok(handover.aiBrief().includes('Ohne Beleg keine Zuordnung erzeugen'));
assert.ok(handover.aiBrief().includes('KANDIDAT'));
const html=fs.readFileSync(path.join(__dirname,'../index.html'),'utf8');
const app=fs.readFileSync(path.join(__dirname,'../app.js'),'utf8');
const pkg=JSON.parse(fs.readFileSync(path.join(__dirname,'../package.json'),'utf8'));
for(const id of ['handoverShaftFile','handoverShaftCsv','handoverPassFile',
 'handoverPassCsv','handoverUseProject','handoverAnalyze','handoverFilter',
 'handoverExportCable','handoverExportShaft','handoverExportOpen','handoverAiBrief'])
 assert.ok(html.includes('id="'+id+'"'),'Missing 400-shaft field: '+id);
assert.ok(html.includes('shaft-handover.js?v='));
assert.ok(app.includes('handoverModel.analyze('));
assert.ok(app.includes('handoverModel.exportHandover('));
assert.ok(app.includes('handoverModel.exportByShaft('));
assert.ok(app.includes('handoverModel.exportOpen('));
assert.ok(pkg.scripts.test.includes('tests/shaft-handover.test.js'));
assert.ok(pkg.build.files.includes('shaft-handover.js'));
console.log('BRO 400 SHAFT TEST OK: 400 realistic rows, ordered cable paths, unknowns, rejects, source-free export safety and 3 CSV views');
