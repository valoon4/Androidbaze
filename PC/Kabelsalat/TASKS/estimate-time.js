'use strict';
/*
 * KABELSALAT – planning estimates for ChatGPT's own ACTIVE work.
 * Personal planning heuristic, not measured productivity or a deadline.
 * Each estimate includes reading, coding/research, tests, review, commits.
 * Human/fachliche waiting time and externally missing source files excluded.
 * Only canonical unchecked KAB tasks receive a row.
 */
const fs=require('node:fs');
const path=require('node:path');
const FILES=[
 '01-P0-QUELLE-UND-PITCH.md',
 '02-P1-EDITOR-UND-NETZ.md',
 '03-P2-DATEN-UND-PRUEFUNG.md',
 '04-P3-QA-BETRIEB-UND-FEEDBACK.md',
 '05-BRO-CHAT-AVL-UND-REPORTS.md',
 '06-BRO-CHAT-PRUEFFALL-UND-ARTEFAKT-QA.md',
 '07-BRO-BETON-ROHR-DM.md',
 '08-BRO-KURZE-LR-DN30-160.md'
];
const HEADERS=[
 'Rang','Task-ID','Welle','Arbeitspaket','Aufgabe',
 'Optimistisch (h)','Typisch (h)','Vorsichtig (h)',
 'Aufgabentyp','Abhängigkeit','Schätzsicherheit','Spezifischer Aufwand / Annahme','Backlog-Datei'
];
const MANUAL={
 'KAB-0048':['Visuelle Regression',90,'Chromium-Sichtprüfung','Rote 2D-/3D-Kollisionen sind implementiert; visuelle Abnahme und Regression offen.'],
 'KAB-0830':['Fachliche Klärung',60,'Fachentscheidung','100/75-mm-Widerspruch: Vergleichsunterlage + technische Folgen; externe Entscheidung fehlt.'],
 'KAB-0865':['Quellen-/Artefaktprüfung',180,'Originaldateien','Alte PDFs/XLSX zuerst beschaffen, Hashes und Versionen getrennt registrieren.'],
 'KAB-0866':['Quellen-/Artefaktprüfung',105,'Originaldateien','Gleich benannte Alt-PDFs versionieren und anhand echter Hashes vergleichen.'],
 'KAB-0867':['Massenprüfung/92 Fälle',840,'Originaldateien','92 verifizierte Schacht-IDs, je Datensatz Status und Provenienz; keine erfundenen Fälle.'],
 'KAB-0868':['Massenprüfung/92 Fälle',900,'Originaldateien','92 echte Prüfdatensätze mit Plan-, Kabel- und Befundbelegen verknüpfen.'],
 'KAB-0869':['Massenprüfung/92 Fälle',600,'Originaldateien','Historische und aktuelle Einzelbefunde je Schacht versioniert diffen.'],
 'KAB-0870':['Fachliche Klärung',270,'Hersteller-/Planbelege','Armierte LWL: nur Herstellerbelege, keine Vermutungen zur AVL-Ausnahme.'],
 'KAB-0871':['Quellen-/Artefaktprüfung',390,'Originaldateien','35 behauptete Alt-Abbildungen mit echtem PDF und Quellenstellen vergleichen.'],
 'KAB-0872':['Quellen-/Artefaktprüfung',540,'Originaldateien','274 behauptete PDF-Seiten gegen tatsächliche Schachtfälle abgleichen.'],
 'KAB-0873':['Quellen-/Artefaktprüfung',540,'Originaldateien','276 behauptete Excel-Bilder auf Duplikate, Blatt und Schacht prüfen.'],
 'KAB-0874':['Bericht/Export',480,'Quellenbelege','Gemeinsames versioniertes Befundmodell für Excel und PDF inkl. Vergleichstests.'],
 'KAB-0875':['Visuelle Regression',210,'Bro-Sichtabnahme','Alte Layout-Fehler gegen vorhandene Bildpaare visuell korrigieren.'],
 'KAB-0876':['Fachliche Klärung',75,'Rücksprache Fachperson','Nur Fragen-/Entscheidungsvorlage; Freigabe und Kontakt nicht durch KI ersetzbar.'],
 'KAB-0877':['3D/Geometrie',90,'Bro-Sichtabnahme','Beton-Durchbrüche programmiert; finale Sichtprüfung, Nachbesserung und Nachweis.'],
 'KAB-0878':['3D/Geometrie',75,'Bro-Sichtabnahme','Betontextur vorhanden; Farb-/Materialvergleich und visuelle Feinabstimmung.'],
 'KAB-0879':['3D/Geometrie',90,'Bro-Sichtabnahme','DN-korrekte Kreisgrößen im Demo-Code; Bestätigung an gemischten Ansichten.'],
 'KAB-0881':['KI/Planerkennung',540,'Geeignete Originalpläne','Plantext-Prototyp vorhanden; robuste PDF-Analyse, Rohrzuordnung und Fehlerfälle fehlen.'],
 'KAB-0882':['KI/Bilderkennung',720,'Kalibrierte Beispielfotos','Bildkandidaten-Prototyp vorhanden; zuverlässige Skala, Typzuordnung und Negativtests.'],
 'KAB-0884':['Visuelle Regression',180,'Chromium-Sichtprüfung','Rendering-/Datentests vorhanden; echte Bildvergleichsregression vervollständigen.'],
 'KAB-0885':['Visuelle Regression',60,'Bro-Sichtabnahme','Kabel-/LR-Ansichten bereits getrennt; echte Sichtkontrolle und ggf. Kürzungsfix.'],
 'KAB-0886':['UI/Editor',45,'Keine','DN30–160-UI implementiert; Abnahme des Edits, Maße und Rückwärtskompatibilität.'],
 'KAB-0887':['Test/QA',45,'Keine','Fiktiver DN30–160-Musterschacht vorhanden; Vergleichsbild und Integration prüfen.'],
 'KAB-0888':['Visuelle Regression',105,'Chromium-Sichtprüfung','Test/Screenshot angelegt; echten Browserlauf samt Quell-Roundtrip verifizieren.']
};
const clamp=(n,a,b)=>Math.max(a,Math.min(b,n));
const quarter=(mins)=>Math.max(15,Math.round(mins/15)*15);
const hours=mins=>(mins/60).toFixed(2).replace('.',',');
const quote=value=>'"'+String(value).replace(/"/g,'""')+'"';
function parseCSV(text,delimiter=','){
 const lines=text.trimEnd().split(/\r?\n/);
 return lines.map(line=>{
  let arr=[],v='',inside=false;
  for(let i=0;i<line.length;i++){
   const c=line[i];
   if(c==='"'){if(inside&&line[i+1]==='"'){v+='"';i++;}else inside=!inside;}
   else if(c===delimiter&&!inside){arr.push(v);v='';}
   else v+=c;
  }
  arr.push(v);
  if(inside)throw Error('Unclosed quote in priority CSV');
  return arr;
 });
}
function canon(read){
 const tasks=new Map();
 for(const file of FILES){
  let section='';
  const contents=read(file);
  for(const line of contents.split(/\r?\n/)){
   const header=/^## (P\d+-\d+ · .+?) \(/.exec(line);
   if(header)section=header[1];
   const match=/^- \[([ x])\] \*\*(KAB-(\d{4,}))\*\* – (.+)$/.exec(line);
   if(!match)continue;
   const id=match[2];
   if(tasks.has(id))throw Error('Duplicate task '+id);
   const title=match[4].replace(/\s*[·—-]\s*Nachweis:\s*.*$/i,'');
   tasks.set(id,{id,done:match[1]==='x',title,section,file});
  }
 }
 if(tasks.size<888)throw Error('Expected at least 888 canonical tasks');
 return tasks;
}
function estimate(task){
 const text=task.title.toLowerCase(),id=task.id,num=Number(id.slice(4));
 let type='Einzelfunktion',mins=105,reason='Einzelintegration, funktionaler Test, Commit und Dokumentation.';
 const is=(regex)=>regex.test(text);
 // Priority: apply a *specific* implementation archetype before generic UI verbs.
 if(is(/\b(dxf|dwg|ifc|bim|cad|georeferenz|punktwolke|laserscan|fotogramm|3d.scan)\b/)){
  type='CAD/BIM/Format';mins=600;
  reason='Formatvertrag, Import/Export, fehlerhafte Quellen und 3D-Gegenprobe.';
 }else if(is(/(?:modell|ki|automatisch).{0,30}(?:trainier|objekterkenn|segmentier)|bild.*(?:segment|klassifiz)|bilderkenn|pixel.*(?:klassifiz|erkenn)/)){
  type='KI/Bilderkennung';mins=660;
  reason='Erkennungspipeline, markierte Testquellen, Falschpositiv-/Negativfälle.';
 }else if(is(/\b92\s*(?:schacht|prüf)|\b1\.957|274\s*seiten|276\s*(?:bilder|abbild)|35\s*(?:bilder|quer)/)){
  type='Massenprüfung/92 Fälle';mins=660;
  reason='Serienprüfung zahlreicher Planstellen plus nachvollziehbare Herkunft.';
 }else if(is(/(?:pdf|plan|vektor).{0,35}(?:import|extrahier|erkenn|parser|ausles|abgleichautomat)|(?:foto|bild).{0,40}(?:erkenn|auswert|kalibrier|import)/)){
  type='Plan-/Bildimport';mins=330;
  reason='Import und Validierung anhand echter Positiv-/Negativbeispiele.';
 }else if(is(/(?:excel|xlsx|csv|pdf).{0,30}(?:export|bericht|generier|erzeug)|(?:bericht|querschnitt|zeichnung).{0,35}(?:pdf|excel|export|layout|generier|erzeug)/)){
  type='Bericht/Export';mins=255;
  reason='Layout, prüfbare Quelldaten, Dateiausgabe und Regressionsvergleich.';
 }else if(is(/(?:brandschutz|avl0116|bauart|regelgrenze|hersteller|armier|brandschott|freigabe|konform|prüferangabe)/)){
  type='Fachliche Regel-/AVL-Prüfung';mins=165;
  reason='Normbehauptungen vermeiden; Beleglogik, Unsicherheiten und Negativtests.';
 }else if(is(/(?:\btest\b|tests|testfall|regression|assert|fixture|smoke|browserprüfung|screenshot|chromium|unit.?test|negativtest)/)){
  type='Test/QA';mins=65;
  reason='Gezielte Testfixture, Testausführung, Fehlerbehebung und Nachweis.';
  if(is(/(?:browser|chromium|screenshot|visuell|3d)/))mins=120;
  if(is(/(?:rohrzahl|wandzahl|count|kompletter|unverändert)/)&&!is(/(?:browser|screenshot)/))mins=40;
 }else if(is(/(?:mit pdf s\.\s*\d+|pdf s\.\s*\d+|planseiten? nachprüf|planseite.{0,20}abgleich|pdf.{0,10}einzeln abgleich|quelle.{0,20}gegenprüf|original.*(?:abgleich|verifizier))/)){
  type='Quellen-/Planabgleich';mins=75;
  reason='Positionsweiser Planabgleich mit protokolliertem PDF-Beleg.';
 }else if(is(/(?:benchmark|lasttest|stress|performance|speicherbegrenz|speicherbudget|renderleistung|skalie|timeout)/)){
  type='Performance';mins=210;
  reason='Messaufbau, Lastfall, Profiling und regressionsgeschützte Verbesserung.';
 }else if(is(/(?:\brolle\b|\brechte\b|berechtig|csp|xss|injection|sandbox|sicherheits|zugriffsschutz|geheimnis|schwachstelle)/)){
  type='Sicherheit';mins=150;
  reason='Berechtigungen/Angriffsfläche und negative Sicherheitstests.';
 }else if(is(/(?:github|workflow|action|build|electron|windows|paket|release|deployment|installer|ci\b|offline.zip)/)){
  type='Build/CI';mins=125;
  reason='Buildintegration, ausführbarer QA-Check und Release-Nachweis.';
 }else if(is(/(?:krümm|biegeradius|bezier|catmull|kollision|kollisions|lichte|rohrabstand|randabstand|kurven|durchbruch|geometrie|wandnormal|3d.*wand|3d.*rohr)/)){
  type='3D/Geometrie';mins=195;
  reason='Geometrie und Einheiten absichern, Grenzfälle automatisiert testen.';
 }else if(is(/(?:netz|schachtverbindung|kabelkontinuit|netzkarte|routen?segment|global.*koord|trassenverbindung)/)){
  type='Schachtnetz';mins=180;
  reason='Vernetzte IDs/Koordinaten und mehrschachtige Gegenbeispiele.';
 }else if(is(/(?:import|migration|schema|datenmodell|datenvertrag|provenienz|persist|dateiformat|snapshot|serialis|sourceu|sourcev)/)){
  type='Datenmodell/Import';mins=160;
  reason='Datenvertrag, Formatfehler und Migration/Roundtrip prüfen.';
 }else if(is(/(?:dokumentier|readme|beschreibung|handbuch|glossar|anleitung|legende|kennzeich|hinweistext|checkliste|nachweisnotiz)/)){
  type='Dokumentation';mins=50;
  reason='Inhalt quellengetreu schreiben, verlinken und reviewen.';
 }else if(is(/(?:abnahme|review|feedback|bro[s\W]|rücksprache|besprech|entscheidungsvorlage)/)){
  type='Review/Abstimmung';mins=65;
  reason='Abnahmefragen und Vergleichsansichten vorbereiten, Antworten einarbeiten.';
 }else if(is(/(?:editor|ui\b|tooltip|feld|button|menü|tastatur|einblend|ausblend|filter|ansicht|auswahl|klick|drag|formular|panel|navigator|vorschau)/)){
  type='UI/Editor';mins=105;
  reason='Bedienlogik, Barriere-/Fehlzustände und Browserregression.';
 }
 // Tasks phrased only as a verb are anchored to their exact epic, not a
 // flat blanket estimate. The verb then scales the epic's baseline.
 if(type==='Einzelfunktion'){
  const epic=/^(P\d+-\d+)/.exec(task.section)?.[1]||'';
  const EPICS={
   'P0-01':['Quellen-/Planabgleich',75],
   'P0-02':['3D/Geometrie',150],
   'P0-03':['3D/Geometrie',135],
   'P0-04':['Rohr-/Kabelbelegung',130],
   'P0-05':['Rohr-/Kabelbelegung',135],
   'P0-06':['3D/Kabelrouting',195],
   'P0-07':['3D/Kabelrouting',180],
   'P0-08':['Brandschutzgeometrie',195],
   'P0-09':['Brandschutzgeometrie',195],
   'P0-10':['Brandschutzgeometrie',160],
   'P0-11':['Brandschutzgeometrie',150],
   'P0-12':['Pitch/UX',85],
   'P1-01':['Editor/Schachtwand',175],
   'P1-02':['Editor/Leerrohre',125],
   'P1-03':['Editor/Kabel',135],
   'P1-04':['Editor/2D',145],
   'P1-05':['Editor/3D',165],
   'P1-06':['Pitch/UX',95],
   'P1-07':['Schachtnetz',155],
   'P1-08':['Schachtnetz',180],
   'P1-09':['Schachtnetz',190],
   'P1-10':['Schachtnetz',110],
   'P1-11':['Suche/Filter',95],
   'P1-12':['Projekt/Migration',155],
   'P2-01':['Geometrie/Qualität',150],
   'P2-02':['Geometrie/Kollision',215],
   'P2-03':['Brandschutzprüfung',175],
   'P2-04':['Provenienz/Daten',115],
   'P2-05':['Import/Tabellen',170],
   'P2-06':['Plan/PDF-Abgleich',215],
   'P2-07':['Bildanalyse',255],
   'P2-08':['CAD/BIM/Format',510],
   'P2-09':['Audit/Review',95],
   'P2-10':['Bericht/Export',235],
   'P2-11':['Rollen/Rechte',175],
   'P2-12':['Sicherheitsgrenzen',155],
   'P3-01':['Test/QA',55],
   'P3-02':['Visuelle Regression',100],
   'P3-03':['Performance',200],
   'P3-04':['Sicherheit',135],
   'P3-05':['Barrierefreiheit',105],
   'P3-06':['Build/Offline',105],
   'P3-07':['Build/CI',105],
   'P3-08':['Dokumentation',55],
   'P3-09':['Review/Abstimmung',65],
   'P3-10':['Projektsteuerung',55],
   'P0-13':['Brandschutzgeometrie',195],
   'P2-13':['Fachliche Regel-/AVL-Prüfung',195],
   'P1-13':['Bericht/Export',240],
   'P2-14':['Quellen-/Artefaktprüfung',300],
   'P1-14':['Bericht/Export',220]
  };
  if(EPICS[epic]){
   [type,mins]=EPICS[epic];
   reason='Arbeitspaket '+epic+': '+type+' mit integriertem Test und Nachweis.';
  }
  if(is(/(?:vergleichen|abgleichen|gegenprüfen|nachprüfen|verifizieren|kontrollieren|bewerten)/))
   {mins=Math.round(mins*.68);reason+=' Referenz- oder Negativabgleich.';}
  else if(is(/(?:einführen|umstellen|neu entwickeln|integrieren|automatisieren|vollständig erzeugen|aufbauen)/))
   {mins=Math.round(mins*1.50);reason+=' Architektur-/Integrationsänderung.';}
  else if(is(/(?:berechnen|simulieren|rekonstruieren|transformieren|zusammenführen)/))
   {mins=Math.round(mins*1.23);reason+=' Berechnung mit Grenzfällen.';}
  else if(is(/(?:anzeigen|markieren|benennen|einblenden|ausblenden|beschriften|ausgeben|hinzufügen)/))
   {mins=Math.round(mins*.68);reason+=' Lokale Anzeige-/Feldanpassung.';}
  else if(is(/(?:speichern|lösen|löschen|ersetzen|setzen|verschieben|zuordnen|ändern|anpassen)/))
   {mins=Math.round(mins*1.10);reason+=' Datenzustand und Regression.';}
 }
 // Granular task-specific weighting (active AI effort, never calendared hours).
 if(is(/\b(?:einzeln|je schacht|je planseite)\b/))mins=Math.round(mins*1.30);
 if(is(/\b(?:vollständig|mehrseitig|durchgehend|versioniert|systemübergreifend|gemeinsam)\b/))mins=Math.round(mins*1.25);
 if(is(/\b(?:anzeigen|benennen|kennzeichnen|dokumentieren|festhalten)\b/)&&
    ['Einzelfunktion','UI/Editor','Dokumentation'].includes(type))mins=Math.round(mins*.75);
 if(is(/\b(?:negativfall|grenzfall|abweichung|kollision|unvollständig)\b/)&&type==='Test/QA')mins=Math.round(mins*1.20);
 // Known architectural migrations are not little UI text tasks.
 if(id==='KAB-0217') {type='Datenmodell/Import';mins=480;reason='Dynamische Wandliste durch Datenmodell, Editor, 2D, 3D, Netz und Migration ziehen.';}
 if(id==='KAB-0001') {type='Quellen-/Planabgleich';mins=105;reason='PDF-Quellenindex und Versionsbelege vereinheitlichen.';}
 if(num>=1&&num<=18&&id!=='KAB-0001'&&type==='Einzelfunktion'){
  type='Quellen-/Planabgleich';mins=75;reason='PDF-Quelle kontrollieren und Referenzfixture belegen.';
 }
 // External requirements: classify ability to finish, not a claimed deadline.
 let dependency='Keine';
 if((num>=841&&num<=850)||(num>=865&&num<=873))dependency='Originaldateien';
 else if(is(/(?:100\s*mm.{0,60}75\s*mm|75\s*mm.{0,60}100\s*mm)/))dependency='Fachentscheidung';
 else if(is(/(?:bros? (?:sicht)?abnahme|bros? feedback|timo|rücksprache|mit fachperson|fachlich .*klär|freigabeentscheid|fachliche .*freigabe)/))dependency='Fach-/Bro-Rückmeldung';
 else if(is(/(?:herstellerangabe|herstellerdaten|herstelleraufbau|echtes? aufmaß|verifiziertes? rohrauß|bestätigte[rmn]? .*maß|original.*(?:pdf|datei|plan)|vollständige .*schachtliste)/))dependency='Quell-/Herstellerbeleg';
 else if(type==='KI/Bilderkennung'||type==='Plan-/Bildimport')dependency='Repräsentative Testquellen';
 else if(is(/(?:browser|chromium|echte sichtprüfung|screenshot)/))dependency='Browser-Testumgebung';
 else if(is(/(?:pdf s\.\s*\d+|planseite)/))dependency='PDF-Original zur Kontrolle';
 let note=reason;
 if(MANUAL[id]){const m=MANUAL[id];type=m[0];mins=m[1];dependency=m[2];note=m[3];}
 if(is(/(?:bis zu 2400|2400 leerrohre|2.?400|1000 schächte)/))mins=Math.max(mins,240);
 mins=quarter(mins);
 const low=quarter(mins*(type.includes('KI/')||type.includes('CAD')||type.includes('Massenprüfung')?.50:.65));
 const high=quarter(mins*(dependency!=='Keine'?2.00:1.65));
 let safety=(dependency==='Keine'&&mins<=150)?'mittel':'niedrig';
 if(dependency==='Originaldateien'||dependency==='Fachentscheidung')safety='sehr niedrig';
 return {type,low,mins,high,dependency,safety,note};
}
function generate(read){
 const tasks=canon(read),raw=read('PRIORITY_ORDER.csv'),lines=parseCSV(raw);
 if(lines[0].length!==7||lines[0][1]!=='Task-ID')throw Error('Unexpected priority header');
 const ranks=new Set(),checked=[];
 const summaries=new Map();
 const grouped=new Map();
 const output=[HEADERS.map(quote).join(';')];
 for(let i=1;i<lines.length;i++){
  const row=lines[i];
  if(row.length!==7)throw Error('Priority CSV col count at '+i);
  const [rank,id,phase,category,dep,title,file]=row;
  if(+rank!==i||ranks.has(id))throw Error('Wrong priority rank or duplicate '+id);
  ranks.add(id);
  const task=tasks.get(id);
  if(!task)throw Error('Priority has unknown task '+id);
  if(task.file!==file||task.title!==title)throw Error('Priority description/source mismatch '+id);
  if(task.done){checked.push(id);continue;}
  const e=estimate(task);
  const cells=[i,id,phase,task.section,task.title,hours(e.low),hours(e.mins),
   hours(e.high),e.type,e.dependency,e.safety,e.note,task.file];
  output.push(cells.map(quote).join(';'));
  const cur=summaries.get(phase)||{phase,count:0,low:0,mins:0,high:0,dependency:0};
  cur.count++;cur.low+=e.low;cur.mins+=e.mins;cur.high+=e.high;
  if(e.dependency!=='Keine')cur.dependency++;
  summaries.set(phase,cur);
  const k=grouped.get(e.type)||{type:e.type,count:0,mins:0};
  k.count++;k.mins+=e.mins;grouped.set(e.type,k);
 }
 if(ranks.size!==tasks.size)throw Error('Priority rank total differs from canonical task count');
 if(checked.length+output.length-1!==tasks.size)throw Error('Open task count mismatch');
 return {csv:output.join('\n')+'\n',count:output.length-1,
  done:checked.length,total:tasks.size,
  summary:[...summaries.values()].sort((a,b)=>a.phase.localeCompare(b.phase)),
  types:[...grouped.values()].sort((a,b)=>b.mins-a.mins)};
}
module.exports={FILES,HEADERS,canon,estimate,generate,parseCSV};
if(require.main===module){
 const root=__dirname,read=file=>fs.readFileSync(path.join(root,file),'utf8');
 const result=generate(read);
 if(process.argv.includes('--write')){
  const dest=path.join(root,'TASK_ESTIMATES.csv');
  fs.writeFileSync(dest,result.csv,'utf8');
  console.log('WROTE '+result.count+' estimates to '+dest);
 }else{
  console.log('PREVIEW: '+result.count+' offen, '+result.done+' erledigt');
  console.log('Use node TASKS/estimate-time.js --write to regenerate after task edits.');
 }
}
