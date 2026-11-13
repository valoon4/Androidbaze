'use strict';
/*
 * Kabelsalat atomic planner. Covers EVERY currently unchecked parent task.
 * Generates actionable ~3–10-minute checks with individually verifiable
 * outcomes, preserving the 42 original detailed microtasks verbatim.
 * No execution, completion, engineering approval or elapsed time is implied.
 *
 * Usage from PC/Kabelsalat:
 *   node TASKS/generate-atomic.js --write
 *   node TASKS/generate-atomic.js --check
 */
const fs=require('node:fs'),path=require('node:path'),assert=require('node:assert/strict');
const SOURCE=[
 '01-P0-QUELLE-UND-PITCH.md',
 '02-P1-EDITOR-UND-NETZ.md',
 '03-P2-DATEN-UND-PRUEFUNG.md',
 '04-P3-QA-BETRIEB-UND-FEEDBACK.md',
 '05-BRO-CHAT-AVL-UND-REPORTS.md',
 '06-BRO-CHAT-PRUEFFALL-UND-ARTEFAKT-QA.md',
 '07-BRO-BETON-ROHR-DM.md',
 '08-BRO-KURZE-LR-DN30-160.md'
];
function parseCSV(data,delim=','){
 return data.trimEnd().split(/\r?\n/).map(line=>{
  let rows=[],field='',quoted=false;
  for(let i=0;i<line.length;i++){
   const char=line[i];
   if(char==='"'){if(quoted&&line[i+1]==='"'){field+='"';i++;}else quoted=!quoted;}
   else if(char===delim&&!quoted){rows.push(field);field='';}
   else field+=char;
  }
  assert.ok(!quoted,'unterminated CSV field');rows.push(field);return rows;
 });
}
function quote(s){return '"'+String(s).replace(/"/g,'""')+'"';}
function readTasks(read){
 const result=new Map();
 for(const file of SOURCE){
  let epic='';
  for(const line of read(file).split(/\r?\n/)){
   const h=/^## (P[0-3]-\d+ · .+?) \(/.exec(line);
   if(h)epic=h[1];
   const t=/^- \[([ x])\] \*\*(KAB-\d{4})\*\* – (.+)$/.exec(line);
   if(!t)continue;
   assert.ok(!result.has(t[2]),'doppelte Kanon-ID '+t[2]);
   result.set(t[2],{id:t[2],done:t[1]==='x',
    title:t[3].replace(/\s*[·—-]\s*Nachweis:\s*.*$/i,''),
    epic,epicId:epic.split(' · ')[0],file});
  }
 }
 assert.equal(result.size,888,'erwartete 888 kanonische Parent-IDs');
 return result;
}
function seedMicro(read){
 const seed=new Map();
 const text=read('ATOMIC/PILOT_ORIGINAL.md');
 for(const l of text.split(/\r?\n/)){
  const m=/^- \[ \] \*\*(KAB-\d{4})\/M(\d+)\*\* · \*\*(\d+) Min\.\*\* – (.+?) \*\*Fertig, wenn:\*\* (.+)$/.exec(l);
  if(!m)continue;
  if(!seed.has(m[1]))seed.set(m[1],[]);
  seed.get(m[1]).push({action:m[4],result:m[5],min:+m[3],seed:true,index:+m[2]});
 }
 assert.deepEqual([...seed.keys()].sort(),['KAB-0881','KAB-0882','KAB-0885']);
 assert.deepEqual([...seed.values()].map(x=>x.length).sort((a,b)=>a-b),[8,16,18]);
 return seed;
}
/* Epic-specific code and document hints are points of entry, not a claim that
 * the implementation is already in that file. Missing originals are blockers.
 */
const EPICS={
 'P0-01':['reference','reference-data.js / examples.js / tests/references.test.js'],
 'P0-02':['geometry','pdf-geometry.js / app.js / tests/pdf-wall-clearance.test.js'],
 'P0-03':['geometry','schematic-positions.js / quality.js / app.js'],
 'P0-04':['cable','reference-data.js / examples.js / tests/references.test.js'],
 'P0-05':['cable','network.js / reference-data.js / app.js'],
 'P0-06':['geometry','app.js / construction-v2.js'],
 'P0-07':['geometry','quality.js / components.js / app.js'],
 'P0-08':['geometry','construction-v2.js / app.js'],
 'P0-09':['geometry','construction-v2.js / app.js'],
 'P0-10':['geometry','construction-v2.js / components.js'],
 'P0-11':['geometry','construction-v2.js / components.js'],
 'P0-12':['visual','app.js / index.html / tests/browser-visual.js'],
 'P0-13':['geometry','construction-v2.js / quality.js / app.js'],
 'P0-15':['visual','concrete-wall.js / app.js / tests/concrete-wall.test.js'],
 'P0-16':['image','pipe-dimensions.js / photo-candidates.js / app.js'],
 'P0-17':['testing','tests/concrete-wall.test.js / tests/browser-visual.js'],
 'P0-18':['visual','app.js / examples.js / tests/bro-dn-range.test.js'],
 'P1-01':['data','components.js / app.js / editor2d.js'],
 'P1-02':['ui','components.js / app.js / editor2d.js'],
 'P1-03':['cable','components.js / app.js'],
 'P1-04':['ui','editor2d.js / app.js'],
 'P1-05':['visual','app.js / editor2d.js'],
 'P1-06':['ui','index.html / app.js'],
 'P1-07':['network','network.js / components.js'],
 'P1-08':['network','network.js / app.js'],
 'P1-09':['network','network.js / app.js'],
 'P1-10':['network','app.js / network.js'],
 'P1-11':['ui','app.js / network.js / index.html'],
 'P1-12':['data','components.js / network.js / app.js'],
 'P1-13':['report','reports.js / app.js / editor2d.js'],
 'P1-14':['reference','reports.js / historical PDF-/XLSX-Quelldateien'],
 'P2-01':['geometry','quality.js / app.js'],
 'P2-02':['geometry','quality.js / construction-v2.js'],
 'P2-03':['compliance','quality.js / construction-v2.js'],
 'P2-04':['data','components.js / reference-data.js'],
 'P2-05':['import','components.js / app.js'],
 'P2-06':['pdf','pdf-visual.js / pdf-geometry.js / reference-data.js'],
 'P2-07':['image','photo-candidates.js / app.js'],
 'P2-08':['cad','CAD-/BIM-Importmodul (noch zu erstellen) / network.js'],
 'P2-09':['data','components.js / app.js / reports.js'],
 'P2-10':['report','reports.js / app.js'],
 'P2-11':['security','app.js / Projekt- und Rollenmodell'],
 'P2-12':['compliance','quality.js / app.js'],
 'P2-13':['compliance','AVL0116-Originalquelle / reports.js / reference-data.js'],
 'P2-14':['reference','historische PDF-/XLSX-Artefakte / reports.js'],
 'P3-01':['testing','tests/references.test.js / tests/quality.test.js'],
 'P3-02':['testing','tests/browser-visual.js / GitHub Actions'],
 'P3-03':['performance','app.js / quality.js / Browser-Benchmark'],
 'P3-04':['security','app.js / recovery.js / index.html'],
 'P3-05':['ui','index.html / app.js / Tastatur-Navigation'],
 'P3-06':['build','build-standalone.js / package.json / Offline-ZIP'],
 'P3-07':['build','GitHub Actions / package.json / Buildlogs'],
 'P3-08':['docs','README.md / V2_0_STATUS.md'],
 'P3-09':['review','TASKS/README.md / Bro-Feedback'],
 'P3-10':['docs','TASKS/README.md / PRIORITY_ORDER.csv']
};
const TEMPLATES={
 reference:[
 ['Konkrete Originalstelle für {focus} auffinden','Dateiname/Planseite oder fehlende Quelle eindeutig notiert.',5],
 ['Belegte Quellversion für den Vergleich fixieren','Dokumentversion und Blattseite eindeutig zugeordnet.',5],
 ['Im Importmodell betroffene IDs herausfiltern','Nur die zum Ticket gehörenden Rohre/Schächte ausgewählt.',5],
 ['Erwarteten Originalwert aus Quelle getrennt notieren','Quellwert nicht aus Demo-Daten geschätzt.',5],
 ['Eine erste Einzelzuordnung gegen die Quelle prüfen','Wand, ID und Wert gegenübergestellt.',8],
 ['Eine weitere Zuordnung desselben Prüfblocks prüfen','Unabhängiger zweiter Vergleich dokumentiert.',8],
 ['Fehlende oder widersprüchliche Angaben markieren','Unbekannt bleibt unbekannt, nicht ersetzt.',5],
 ['Gezielt den Negativfall einer fremden ID testen','Fremde Zuordnung wird nicht übernommen.',5],
 ['Wand-/Schacht-Rohrzahl vor und nach Abgleich vergleichen','Anzahl unverändert oder Abweichung begründet.',5],
 ['Kleine reproduzierbare Referenzfixture aktualisieren','Erwartete IDs/Werte im Test verankert.',8],
 ['Passenden Quellvergleich automatisiert ausführen','Teststatus oder echter Quellenblocker erfasst.',8],
 ['Prüfergebnis mit Fundstelle am Parent dokumentieren','Planseite und Commit/Testbeleg nachvollziehbar.',5]
 ],
 geometry:[
 ['Betroffenen Geometriepfad für {focus} lokalisieren','Funktionsstelle und Eingabedaten identifiziert.',5],
 ['Maßeinheit und Koordinatenbezug separat kontrollieren','mm/m sowie U/V oder XYZ nicht verwechselt.',5],
 ['Ein minimal reproduzierbares Geometriebeispiel festlegen','Werte, IDs und erwartetes Ergebnis dokumentiert.',5],
 ['Basistest für gültige Modellgeometrie anlegen','Ein positiver Fall wird reproduziert.',8],
 ['Grenzfall direkt am erlaubten Zahlenbereich ergänzen','Kanten-/Radius- oder Höhenwert ist testbar.',8],
 ['Eine lokale Geometriefunktion für das Teilziel anpassen','Eine abgegrenzte Berechnung oder Darstellung wirksam.',10],
 ['Repräsentatives zweites Größen-/Wandbeispiel prüfen','Anderer DN/Wandtyp deckt dieselbe Funktion ab.',8],
 ['Fremde Rohr-/Kabel-ID unverändert halten','Kein unbeabsichtigtes Umhängen nach Modelländerung.',5],
 ['Negativtest für Überschneidung oder falsche Koordinaten anlegen','Falsche Eingabe erzeugt sichtbaren Befund.',8],
 ['2D-Ansicht gegen den berechneten Zustand prüfen','Gleiches Objekt und Position in 2D erkennbar.',8],
 ['3D-Ansicht gegen den berechneten Zustand prüfen','Keine zusätzlich erfundenen Rohre/Platten.',8],
 ['Demo-/Originalpositionen getrennt gegentesten','Quell-U/V und simulierte Koordinaten bleiben getrennt.',8],
 ['Einheiten- und Rundungsgrenzen kontrollieren','Weder Millimeter noch Vorzeichen still geändert.',5],
 ['Geometrieregression der betroffenen Wand ausführen','Testausgabe/konkreter Fehlfall gespeichert.',8],
 ['Technischen Geltungsbereich der Änderung dokumentieren','Schematisch bleibt ausdrücklich nicht vermessen.',5],
 ['Änderung mit Test-/Codebeleg verlinken','Commit und erreichbares Prüfergebnis vorhanden.',5]
 ],
 cable:[
 ['Zuständige Kabel-/Rohrdatenfunktion für {focus} finden','Quellobjekte und stabile IDs bekannt.',5],
 ['Betroffene Wand- und LR-IDs aus Titel isolieren','Start/Ziel und referenzierte Quelle notiert.',5],
 ['Vorhandene Kabelbelegung als unveränderte Referenz sichern','Vorher-Snapshot oder Prüffixture liegt vor.',8],
 ['Ein gültiges Kabelobjekt als Positivfall anlegen','ID, Gruppe und Endpunkt entsprechen der Vorgabe.',8],
 ['Den ersten Endpunkt gezielt zuordnen oder prüfen','Wand/LR-Verweis wird korrekt aufgelöst.',8],
 ['Den zweiten Endpunkt separat zuordnen oder prüfen','Keine erfundene Gegenverbindung.',8],
 ['Eine lokale Zuordnungs- oder Renderkorrektur umsetzen','Nur der betroffene Pfad/Datensatz geändert.',10],
 ['Negativfall mit unbekannter LR-ID hinzufügen','Unbekannter Endpunkt bleibt offen.',5],
 ['Negativfall mit fremder Wand-ID hinzufügen','Anschluss wird nicht auf falsche Wand gesetzt.',5],
 ['Gruppen MS/OLA und deren Varianten unabhängig prüfen','Keine unerlaubte Gleichsetzung MS=B/OLA=A.',8],
 ['Doppelt belegtes LR als eigenständigen Prüffall abbilden','Mehrfachbelegung weder verschluckt noch dupliziert.',8],
 ['Kabelvorkommen gegen Kabelobjektzahl getrennt prüfen','Vorkommen und globale Objekte nicht verwechselt.',8],
 ['3D-Kabelanzeige mit tatsächlicher Belegung vergleichen','Linien sind Kabel, Öffnungen sind LR.',8],
 ['JSON-Speichern und Wiederladen für Endpunkte prüfen','Kabel- und Rohr-IDs unverändert erhalten.',8],
 ['Quellkonflikte mit Fundstelle dokumentieren','Keine automatische Quellkorrektur ohne Plan.',5],
 ['Regression und Commit-Nachweis ans Parent hängen','Ergebnis anhand Test/Commit nachvollziehbar.',5]
 ],
 ui:[
 ['Konkreten Bildschirm und Steuerelement für {focus} finden','Bestehendes HTML-Element oder neue Zielstelle eindeutig.',5],
 ['Bedienzustand vor Änderung nachvollziehbar festhalten','UI-Ausgangslage durch Test oder Screenshot dokumentiert.',5],
 ['Eingabe-/Interaktionsfall als minimalen Test definieren','Ein konkreter Klick/Eintrag und Zielwert festgelegt.',5],
 ['Steuerelement oder sichtbaren Text lokal ergänzen','UI-Funktion betrifft nur die geforderte Aktion.',8],
 ['Eventhandler auf die richtige Objekt-ID verbinden','Kein anderes Element verändert.',8],
 ['Wertebereich bzw. Eingabeverhalten validieren','Ungültige Eingaben werden abgefangen.',8],
 ['Statusanzeige nach gültiger Aktion aktualisieren','Sichtbarer Zustand entspricht Datensatz.',8],
 ['Tastaturbedienung des Elements testen','Fokus und Enter/Escape konsistent.',5],
 ['Interaktion im Basic-Modus prüfen','Keine unfreiwillige Profi-Bearbeitung.',5],
 ['Interaktion im Profi-Modus prüfen','Erlaubte Bearbeitung weiterhin möglich.',5],
 ['Leeren/fehlenden Datensatz als Negativfall prüfen','UI stürzt nicht ab und erfindet keine Werte.',8],
 ['Undo/Redo oder Reload des Zustands prüfen','Keine unerwartete dauerhafte Änderung.',8],
 ['Enge Bildschirmbreite auf überdeckte Controls prüfen','Betroffenes Element erreichbar.',8],
 ['Gezielten Browser-/DOM-Test ausführen','Sichtbare Ausgabe als Test protokolliert.',8],
 ['Abnahmetext und GitHub-Nachweis ergänzen','Funktion und ihre Grenzen klar benannt.',5]
 ],
 data:[
 ['Datenvertrag des Zielobjekts für {focus} lokalisieren','Einzelne betroffene Felder und Typen benannt.',5],
 ['Belegte Quellfelder von berechneten Feldern trennen','Unbestätigte Daten erhalten keinen bestätigten Status.',5],
 ['Einen gültigen Testdatensatz für die Änderung anlegen','Objekt-ID und erwartete Werte sichtbar.',8],
 ['Die kleinste notwendige Schema-Änderung vorbereiten','Nur relevante Pflicht-/Optionalfelder vorgesehen.',5],
 ['Ein konkretes Feld im Datenmodell implementieren','Validierung und Default für das Feld wirksam.',10],
 ['Wert ohne stillschweigende Typumwandlung einlesen','Einheit und Rohwert bleiben erhalten.',8],
 ['Fremde bzw. unbekannte Attribute roundtrip-sicher erhalten','Kein Datenverlust beim Einlesen.',8],
 ['Negativtest mit fehlendem Feld durchführen','Fehler/Unbekannt-Zustand statt Fantasiewert.',8],
 ['Negativtest mit fehlerhaftem Datentyp durchführen','Ungültiger Input wird sicher behandelt.',8],
 ['Einen Altversions-Datensatz migrieren/prüfen','Bestehende Projekte bleiben lesbar.',8],
 ['Zwei Objekte auf stabile und einmalige IDs prüfen','Kein Überschreiben oder Duplikat.',8],
 ['Schreiben und Wiederlesen getrennt prüfen','JSON-/Datei-Roundtrip ohne Drift.',8],
 ['Anzeige oder konsumierenden Modulpfad anbinden','Feld ist dort abrufbar, wo benötigt.',8],
 ['Datenregression für unveränderte Felder ausführen','Keine fremden Referenzen verloren.',8],
 ['Migration/Quellenstatus dokumentieren','Feldbedeutung und Grenzen erläutert.',5],
 ['Parent mit Test- und Commit-Nachweis aktualisieren','Nachprüfbarer Abschluss-/Blockerstand verlinkt.',5]
 ],
 network:[
 ['Betroffene Schacht-/Link-IDs für {focus} herausfiltern','Stabile lokale/globale IDs bekannt.',5],
 ['Aktuelle Netzroute in kleiner Fixture sichern','Vorherzustand ohne Positionsverlust festgehalten.',8],
 ['Gültiges Zweischacht-Beispiel definieren','Link-Enden und Referenzachsen konkret.',8],
 ['Netzfunktion oder Koordinatentransformation lokalisieren','Modulpfad/Einheit identifiziert.',5],
 ['Ein einzelnes Netzdatenfeld ergänzen/prüfen','Stabile Referenz auf Schacht oder Segment.',8],
 ['Eine lokale Link- oder Routenkante implementieren','Gewähltes Paar verbunden, Fremde nicht.',10],
 ['Lokale und globale Achsen getrennt prüfen','Keine Höhen-/Positionserfindung.',8],
 ['Negativfall mit fehlendem Zielschacht ergänzen','Broken Link wird sichtbar, nicht repariert erfunden.',8],
 ['Negativfall mit doppelt verwendeter ID ergänzen','Mehrdeutigkeit nicht still überschrieben.',8],
 ['Ende-zu-Ende-Zuordnung über zweite Kante prüfen','Kabelidentität über Segment erhalten.',8],
 ['Graph-Auswahl und Detailansicht synchronisieren','Netz- und Schachtklick stimmen überein.',8],
 ['Undo/Redo eines Netzänderungsschritts prüfen','Link/Schacht-Rückbau ohne Datenverlust.',8],
 ['JSON-Netz-Roundtrip mit zwei Schacht-IDs testen','Globale Zuordnung nach Reimport gleich.',8],
 ['Browsertest für den neuen Netzweg ausführen','Route/Fehlerstatus sichtbar.',8],
 ['Nicht belegte Verbindungen als ungeprüft markieren','Keine fiktive physische Kabelkontinuität.',5],
 ['Test/Commit am Parent verlinken','Ergebnis reproduzierbar.',5]
 ],
 report:[
 ['Berichtsziel und Ausgabeformat für {focus} präzisieren','PDF, Excel oder CSV mit Feldliste festgelegt.',5],
 ['Vorhandenes Befund-/Berichtsmodell aufrufen','Schacht-ID und Prüfstatus bekannt.',5],
 ['Einen minimalen echten bzw. ausdrücklich fiktiven Berichtdatensatz bauen','Datenherkunft transparent.',8],
 ['Titel-/Kopfbereich mit Schacht-ID erzeugen','Bericht besitzt eindeutige Objektzuordnung.',8],
 ['Ein angefordertes Feld in das Berichtsschema aufnehmen','Wert und Einheit typisiert.',8],
 ['Eine einzelne Tabellen-/Querschnittszeile rendern','Zeile verweist auf reale Rohr-/Kabel-ID.',8],
 ['Blatt- und Quellenreferenz am Befund anbringen','Fundstelle oder Quellenlücke sichtbar.',8],
 ['Farb-/Statuslegende gegen Datenmodell prüfen','Rot heißt nicht automatisch Nichtbestehen.',8],
 ['Negativfall ohne Originalplanseite ausgeben','Nicht vorliegende Quelle klar als offen.',8],
 ['Negativfall ohne Prüfer-/Herstellerbeleg ausgeben','Freigabestatus nicht automatisch gesetzt.',8],
 ['Lange IDs und Textumbrüche im Layout testen','Keine abgeschnittenen Tabelleninhalte.',8],
 ['Zweitens Beispiel mit anderem Schacht prüfen','Kein hartkodierter Einzelschacht.',8],
 ['PDF-/Excel-/CSV-Schema gegen denselben Status vergleichen','Medienwidersprüche sichtbar/verhindert.',8],
 ['Exportfunktion auf Dateiinhalt prüfen','Datei tatsächlich lesbar und verlinkbar.',8],
 ['Veraltete Berichtsdatei nicht als aktuell kennzeichnen','Versionsdatum und Hash/Status klar.',5],
 ['Visuellen Berichtsscreenshot oder Kontrollauszug sichern','Ergebnis für Bro nachvollziehbar.',8],
 ['Regressionen und Commit-Nachweise dokumentieren','Ablage/Prüfstand vollständig.',5]
 ],
 security:[
 ['Betroffene Eingabe-/Ausgabegrenze für {focus} lokalisieren','Angriffsfläche und Kontext eindeutig.',5],
 ['Erlaubten Benutzer-/Objektzustand als Fixture aufbauen','Erwartete Rechte oder escaping-Regel definiert.',8],
 ['Unerlaubte Aktion bzw. Nutzlast als Negativfall formulieren','Missbrauchspfad reproduzierbar.',5],
 ['Validierung direkt am Eintrittspunkt einfügen','Ungültige Anforderung blockiert.',8],
 ['Ausgabe- oder DOM-Kontext getrennt absichern','Kein rohes HTML aus Projekttext.',8],
 ['Fehlerantwort ohne vertrauliche Daten prüfen','Keine Projektdaten im Log oder Alert.',8],
 ['Betrachter- gegen Bearbeiterrolle getrennt prüfen','Schreiboperation nur bei Berechtigung.',8],
 ['Altprojekt ohne neue Metadaten laden','Kompatibilität ohne Rechteeskalation.',8],
 ['Export-/Importpfad auf dieselbe Grenze prüfen','Kein Umgehen durch JSON/CSV.',8],
 ['Negativtest nach der Korrektur erneut ausführen','Unerlaubte Aktion weiterhin verhindert.',8],
 ['Norm-/Sicherheitsfreigabe nicht behaupten','Schutzumfang explizit begrenzt.',5],
 ['Sicherheitscheck/Commit im Parent dokumentieren','Rückverfolgbarer Prüfbeleg vorhanden.',5]
 ],
 testing:[
 ['Konkrete Akzeptanzbehauptung für {focus} isolieren','Ein prüfbarer Sollwert statt breitem Testtitel.',5],
 ['Bestehende Testdatei und passende Fixture lokalisieren','Quell-/Testpfad notiert.',5],
 ['Minimalen positiven Testeingang festlegen','Reproduzierbares Objekt oder DOM-Zustand.',5],
 ['Eine einzige Soll-Assertion hinzufügen','Erwarteter Wert exakt vergleichbar.',5],
 ['Ein zweites gültiges Beispiel ergänzen','Nicht nur hartkodierte Einzelprobe.',8],
 ['Negativfall oder fehlendes Quellfeld anlegen','Fehlerstatus statt erfundenem Erfolg.',8],
 ['Spezifischen Quell-/ID-Erhalt kontrollieren','Keine unabsichtliche Migration/Mutation.',5],
 ['Den lokalen Einzeltest ausführen','Pass/Fail und Fehlermeldung lesbar.',8],
 ['Fehlerzustand gezielt beheben oder als Blocker erfassen','Kein blindes Grünfärben.',8],
 ['Den Test in die vorhandene Suite einhängen','Lauf über npm test/CI möglich.',5],
 ['CI- oder Browserlauf mit Commit abgleichen','Tatsächlicher statt vermuteter Status.',8],
 ['Nachweis und verbliebenen Grenzfall dokumentieren','Parent hat Testlink und genaue Einschränkung.',5]
 ],
 visual:[
 ['Relevanten Screen/3D-Zustand für {focus} finden','Basic-/Profi-Ansicht und Kamerazustand identifiziert.',5],
 ['Ist-Screenshot mit unveränderten Quelldaten erstellen','Vorheransicht reproduzierbar.',8],
 ['Betroffene sichtbare Geometrie identifizieren','Kabelstrang, LR-Öffnung oder Bauteil unterschieden.',5],
 ['Ein konkretes Sollbild oder sichtbares Kriterium festlegen','Abnahme ist nicht nur Geschmacksschätzung.',5],
 ['Eine lokale Material-/Ansichtsoption umsetzen oder prüfen','Nur geforderter Parameter betroffen.',10],
 ['Geometriegröße gegen den angegebenen DN vergleichen','DN50 vs DN110 korrekt relativ dargestellt.',8],
 ['Basic-Ansicht auf richtige Sichtbarkeit prüfen','Keine versteckten Kabel/Leerrohrverwechslungen.',8],
 ['Profi-Ansicht separat kontrollieren','Bearbeitbarkeit bleibt erhalten.',8],
 ['Bei Bedarf 2D-/3D-Ausrichtung vergleichen','Wand-/Rohr-IDs deckungsgleich.',8],
 ['Auswahl/Picking nach geänderter Darstellung prüfen','Öffnungen/Kabel bleiben selektierbar.',8],
 ['Gegenprobe mit zweiter Referenzdatei erstellen','Nicht nur ein Demo-Schacht passend.',8],
 ['Nachher-Screenshot mit gleicher Kamera sichern','Vorher/Nachher vergleichbar.',8],
 ['Visuellen Test im echten Browser ausführen','WebGL-/Console-Fehler separat ausgewertet.',10],
 ['Bro-Frage und erforderliche externe Freigabe offen halten','Keine behauptete Abnahme ohne Antwort.',5],
 ['Screenshot/Testbeleg im Parent verlinken','Exakte Version nachprüfbar.',5]
 ],
 performance:[
 ['Messgröße für {focus} eindeutig benennen','ms, MiB oder Objekte als Einheit definiert.',5],
 ['Messumgebung und Browser-Version festhalten','Messung später reproduzierbar.',5],
 ['Kleine Kontrollfixture mit Ausgangsgröße erzeugen','Baseline verfügbar.',8],
 ['Größere synthetische Fixture anlegen','Keine künstlich echten Schächte erfunden.',8],
 ['Performance-Zähler in Zielpfad instrumentieren','Zeit-/Speicherwert ablesbar.',8],
 ['Eine Basismessung ohne Änderung ausführen','Vorherwert protokolliert.',8],
 ['Engstelle aus Messergebnis isolieren','Funktion und Testfall klar.',8],
 ['Eine lokale Optimierung implementieren','Kleiner nachvollziehbarer Eingriff.',10],
 ['Korrektheit nach Optimierung erneut prüfen','Rohr-IDs und Ergebnis identisch.',8],
 ['Lastmessung mit demselben Input wiederholen','Vorher/Nachher vergleichbar.',8],
 ['Daten-/UI-Ladegrenze als Fehlfall testen','Kein Freeze/Crash bei zu großem Input.',8],
 ['Regression und Schwelle in Testbericht aufnehmen','Messwerte statt gefühlter Geschwindigkeit.',8],
 ['CPU-/Browserabhängigkeit des Resultats dokumentieren','Kein allgemeiner Garantiewert behauptet.',5],
 ['Commit und Messprotokoll verlinken','Benchmark wiederholbar.',5]
 ],
 build:[
 ['Betroffenen Build-/Workflowpfad für {focus} lokalisieren','Konkrete YAML-/Paketstelle identifiziert.',5],
 ['Aktuelle Node-/Abhängigkeitsversion dokumentieren','Buildbasis eindeutig.',5],
 ['Relevante Datei-/Assetliste einzeln prüfen','Erforderliche Quelldatei vorhanden oder blocker.',5],
 ['Einen isolierten statischen Check ergänzen','Fehler wird früh vor Paketbau erkannt.',8],
 ['Ein eigenes Testkommando lokal prüfen','Exit-Code und Output eindeutig.',8],
 ['Build-Skript an genau einer Stelle anpassen','Keine weiteren Artefakte unbeabsichtigt geändert.',8],
 ['Fehlerfall mit fehlendem Asset testen','Build scheitert nachvollziehbar.',8],
 ['Offline- oder Windows-Pfad separat prüfen','Pfadseparatoren und CRLF robust.',8],
 ['Paketinhalt auf tatsächliches Zielasset kontrollieren','Dateiliste überprüft.',8],
 ['Version/Cache-Key gegen Hauptseite abgleichen','Kein veraltetes JavaScript geladen.',5],
 ['Workflow in CI auslösen/Status lesen','Echter Run-Link vorhanden.',8],
 ['Failing CI-Schritt klar isolieren','Kein grüner Status behauptet ohne Run.',5],
 ['Ausgabe-Artefakt und Release-Nachweis verlinken','Gebauter Inhalt abrufbar oder Blocker vermerkt.',5]
 ],
 docs:[
 ['Zu dokumentierenden Befund für {focus} eingrenzen','Konkretes Thema, Datenquelle und Zielgruppe definiert.',5],
 ['Kanonische Datei/Abschnitt und frühere Version finden','Nur richtige Dokumentstelle verändert.',5],
 ['Fachbegriffe mit vorhandenem Quellwortlaut abgleichen','Keine erfundenen Regeln oder ID-Namen.',5],
 ['Eine kurze Rohfassung der erforderlichen Aussage formulieren','Eine überprüfbare Textpassage vorhanden.',8],
 ['Quelllink oder Commitbeleg an die Aussage hängen','Fundstelle klickbar bzw. eindeutig.',5],
 ['Offene Daten und Einschränkungen ausdrücklich ergänzen','Unbekannt/Fachfreigabe nicht verwischt.',5],
 ['Unnötiges Metablabla aus der Übersicht entfernen','Wesentliche Zahlen/Schritte sichtbar.',5],
 ['Links und relative Pfade des Abschnitts prüfen','Keine verwaisten Dateiverweise.',5],
 ['Formulierung gegen Ticketakzeptanz abgleichen','Alle im Titel genannten Anforderungen abgedeckt.',8],
 ['Konsistenz mit kanonischen Parent-Zahlen prüfen','Zähler und ID-Rang nicht verwechselt.',5],
 ['Dokumentation als fokussierten Commit sichern','Änderung nachprüfbar.',5]
 ],
 review:[
 ['Die konkrete Rückmeldung für {focus} aus Originalchat isolieren','Wortlaut und Datum getrennt von Assistentenideen.',5],
 ['Betroffenes UI-/Geometrieteil identifizieren','LR, Kabel und Bauteil nicht verwechselt.',5],
 ['Soll- und Istzustand in zwei kurzen Sätzen gegenüberstellen','Abweichung testbar beschrieben.',5],
 ['Eine konkrete Prüffrage statt Sammelmail formulieren','Eine Antwortentscheidung ermöglicht.',5],
 ['Aktuelle Screenshots oder Testnachweise verlinken','Review findet am richtigen Softwarestand statt.',8],
 ['Unbestätigte Maße/Normen als offene Punkte markieren','Keine automatische Fachfreigabe.',5],
 ['Mögliche Alternative ohne Datenmutation beschreiben','Demo- und Originaldaten klar getrennt.',5],
 ['Benötigte externe Antwort als Blocker erfassen','Wartezeit nicht in meine Minuten gerechnet.',5],
 ['Antwort und betroffene KAB-ID später zuordnen','Rückmeldung nachvollziehbar.',5],
 ['Entscheidungs- und Nachweislink am Parent hinterlegen','Reviewstatus ohne Behauptung eines Erfolgs.',5]
 ],
 compliance:[
 ['Verlangte Fachbehauptung für {focus} präzisieren','Regel, Variante oder Grenzwert konkret benannt.',5],
 ['Echte AVL-/Hersteller-/Planquelle hierfür identifizieren','Dokument, Version und Fundstelle oder Blocker notiert.',5],
 ['Behauptete Norm von Beispiel-/Demoannahme unterscheiden','Keine Normbehauptung aus Bild/Chat abgeleitet.',5],
 ['Bekannte Konfliktwerte gesondert protokollieren','75/100 mm und A/B nicht automatisch entschieden.',5],
 ['Quellbelege als Datenfelder mit Version abbilden','Prüfregel hat eindeutigen Ursprung.',8],
 ['Ein belegtes Positivbeispiel als Fixture festlegen','Reales/ausdrücklich fiktives Beispiel gekennzeichnet.',8],
 ['Ein fehlender Nachweis als Negativfall modellieren','Status offen statt bestanden.',8],
 ['Unbekannte Bodenhöhe bzw. Durchmesser separat testen','Keine willkürliche Bauartzuordnung.',8],
 ['Eine einzelne Prüf-/Anzeige-Regel implementieren','Unbekannt bleibt als Datenlücke sichtbar.',10],
 ['Fachliche Entscheidung als erforderlichen Gate deklarieren','Keine KI-Freigabe ohne externes Votum.',5],
 ['Ausgabebericht auf neutrale Befundwörter prüfen','Fehlender Beleg ≠ negative Normprüfung.',8],
 ['Plan-/Herstellerbeleg an Prüffall hängen','Quelllink oder nicht vorhanden markiert.',5],
 ['Negativtest auf fälschliches PASS ergänzen','Unbelegte Daten erreichen nie geprüft/freigegeben.',8],
 ['Prüfkriterien und Blocker zum Parent schreiben','Rückfragenliste ohne erfundene Entscheidung.',5]
 ],
 image:[
 ['Betroffenen Bild-/Kandidatenpfad für {focus} lokalisieren','Bild, Maßstab und UI-Stelle benannt.',5],
 ['Echte Bildquelle von synthetischer Fixture trennen','Testbild nicht als realen Bestand ausgegeben.',5],
 ['Positivfixture mit eindeutigem Kreis vorbereiten','Pixelkoordinaten und erwarteter Bereich bekannt.',8],
 ['Zweite Fixture mit anderer Kreisgröße ergänzen','Klein/groß unabhängig erfasst.',8],
 ['Einen lokalen Bildvorverarbeitungsschritt implementieren','Ein Pixel-/Farbkanalfall getestet.',10],
 ['Kreiskandidaten-Ausgabe als strukturierte IDs prüfen','Position, Größe und Kandidatenstatus vorhanden.',8],
 ['Kalibrierungsannahme separat festhalten','Ohne Skala keine absolute Millimeterbehauptung.',5],
 ['Referenzmaß auf Null-/NaN-Eingabe negativ testen','Ungültige Referenz wird abgewiesen.',8],
 ['Teilweise verdeckten oder schiefen Kreis negativ testen','Perspektivrisiko/Unsicherheit sichtbar.',8],
 ['Zwei Öffnungen und eine falsche Form unterscheiden','Falschpositive werden nicht automatisch übernommen.',8],
 ['Bilddateigröße und Typen im Upload prüfen','Begrenzung schützt Speicher und Parser.',8],
 ['Manuelle ID-Zuordnung kontrollieren','Kein Rohr ohne eindeutige ID überschrieben.',8],
 ['Kandidatenstatus nach Anwendungsaktion prüfen','Nie automatisch fachlich reviewed.',5],
 ['Originalrohr-/Quelldaten gegen neuen Bildlauf vergleichen','Kein stilles Mutieren bestätigter Daten.',8],
 ['Test im echten Browser bei vorhandenem Bild ausführen','Erkennung/Fehler sichtbar dokumentiert.',8],
 ['Fehlende echte Kalibrierdaten als Blocker markieren','Aussage nicht über Prototypniveau hinaus.',5],
 ['Code/Testlink am Parent dokumentieren','Konkrete Probe später reproduzierbar.',5]
 ],
 pdf:[
 ['Konkrete PDF-Seite und Zielinformation für {focus} bestimmen','Seitenreferenz oder Quellenblocker notiert.',5],
 ['PDF-Binärtext gegen Screenshot-/Vektorquelle unterscheiden','Kein OCR behauptet, wenn keine Textextraktion.',5],
 ['Eine synthetische Textbeschriftung als Positivfixture anlegen','Rohtext mit erwarteter Rohr-ID/Angabe.',8],
 ['Ein zweites Dokumentlabel mit anderer Maßgröße ergänzen','Parser nicht nur auf einen DN hartkodiert.',8],
 ['Seitenindex und Blatt-/Originalversion separat speichern','Planstelle nachvollziehbar.',8],
 ['Einen einzelnen PDF-Datensatz in ein Zwischenmodell übertragen','Quelle, Koordinaten und ID typisiert.',10],
 ['Ein Format mit fehlender Beschriftung negativ testen','Keine erfundene Rohr-ID.',8],
 ['Ein Format mit falscher Einheit negativ testen','mm und Pixel nicht vertauscht.',8],
 ['Vektorpunkt und reine Screenshotposition getrennt behandeln','Kein unskalierter absoluter Durchmesser.',8],
 ['Duplizierte ID in zwei Planstellen als Konflikt markieren','Kein stilles Zusammenführen.',8],
 ['Manuelle Nutzerbestätigung als Gate prüfen','Kandidat bleibt ungeprüft.',8],
 ['Original-PDF-Position in 2D-Gegenansicht vergleichen','U/V-Koordinaten nicht verschoben.',8],
 ['3D-Querschnitt gegen Quell-Rohrzahl vergleichen','Wand/LR-Anzahlen erhalten.',8],
 ['Reimport derselben Planstelle als idempotenten Test ergänzen','Keine verdoppelten Rohrdatensätze.',8],
 ['Fehlende/gesperrte Originaldatei sauber als Blocker melden','Kein Erfolg ohne echte Quelle.',5],
 ['PDF-Regression laufen lassen und Beleg verlinken','Teststatus und Fundstelle nachvollziehbar.',8]
 ],
 cad:[
 ['Kleinstes CAD-/BIM-Teilziel aus {focus} definieren','Ein benanntes Element/Layertyp als erster Umfang.',5],
 ['Quellformat und Version/Einheit isoliert notieren','DXF/IFC usw. nicht gleichgesetzt.',5],
 ['Ein minimales gültiges synthetisches CAD-Objekt anlegen','Eine Wand/Linie/Öffnung mit Koordinaten.',8],
 ['Zielkoordinatenreferenz für das Objekt definieren','Lokal/global und mm/m klar.',8],
 ['Einen einzelnen Import-Tokenizer/Datensatzfall implementieren','Ein Objekt ist maschinell lesbar.',10],
 ['Wand-/Schacht-ID aus einem Datensatz zuordnen','Zuordnung reproduzierbar.',8],
 ['Erste 2D-Vektorkoordinate in Zwischenmodell übernehmen','Korrekte X/Y-Werte.',8],
 ['Einheit umrechnen und Roundtrip kontrollieren','Weder Faktor 1000 noch Vorzeichenfehler.',8],
 ['Negativfall ohne Layer oder mit Fremd-ID prüfen','Datensatz bleibt offen bzw. abgewiesen.',8],
 ['Negativfall mit nicht unterstütztem Elementtyp prüfen','Sichtbarer Hinweis statt stiller Fehlinterpretation.',8],
 ['Rotation/verschobenen Ursprung als getrennten Fall prüfen','Lokale Transformation erhalten.',8],
 ['Ein Objekt auf ein bestehendes Schachtmodell abbilden','Keine duplizierten Wände.',8],
 ['Importwiederholung auf Duplikate prüfen','IDs und Anzahl nach zweitem Import stabil.',8],
 ['Exportgegenprobe für genau ein Element ergänzen','Position und Typ bleiben erhalten.',8],
 ['Keine reale BIM-Freigabe automatisch setzen','Schematische Felder explizit markiert.',5],
 ['Kleinen Testfixture in CI anbinden','Reproduzierbarer Einzeltest vorhanden.',8],
 ['Quellformat-/Typgrenzen in Doku festhalten','Nicht unterstützte Elemente benannt.',5],
 ['Testlog/Commit am Parent anbringen','Anwendbarer Implementierungsschritt belegt.',5]
 ],
 import:[
 ['Tabellenspalte/Dateiformat für {focus} spezifizieren','Eine echte/ausdrücklich synthetische Feldliste.',5],
 ['Eine gültige Ein-Zeilen-Fixture aufbauen','Spalten und Test-ID eindeutig.',8],
 ['Pflichtfeld- und Einheitentypen notieren','mm/m, Name und ID getrennt.',5],
 ['Ersten Header-Fall im Parser implementieren','Spalte eindeutig erkannt.',8],
 ['Ersten Werte-/Zeilenfall implementieren','Eine Zeile wird zum Modellobjekt.',8],
 ['Leere Zelle negativ prüfen','Fehlender Wert nicht erfunden.',8],
 ['Doppelte ID negativ prüfen','Bestehender Datensatz nicht überschrieben.',8],
 ['Ungültige Zahl oder Einheit negativ prüfen','Fehlerstatus mit Zeilenbezug.',8],
 ['Zweite Tabellenzeile unabhängig einlesen','Batch-Pfad arbeitet ohne erste Zeile zu kopieren.',8],
 ['Datei-/Quellenname im Datensatz erhalten','Importprovenienz vollständig.',8],
 ['Roundtrip auf Objekt-ID/Einheiten testen','Export und Wiedereinlesung identisch.',8],
 ['Bestehende Projektdatei vor Import unverändert sichern','Diff nur für genehmigten Import.',8],
 ['Benutzerstatus für abgewiesene Zeilen zeigen','Fehler ist sichtbar, nicht geschluckt.',8],
 ['Kleinen Parser-/Migrationstest ausführen','Pass/Fail und Beispiel im Log.',8],
 ['Grenzen/Spalten im Importvertrag dokumentieren','Nutzbare Formatvorgabe vorhanden.',5],
 ['Commit-/Testbeleg am Parent hinterlegen','Änderung nachprüfbar.',5]
 ]
};
const FULL_EXTRA={
 'KAB-0881':[
  ['PDF-Seitenleser für Textlayer als eigenständige Schnittstelle spezifizieren','Ein- und Ausgabevertrag mit Seite und Dokument-ID.',8],
  ['Fehlenden PDF-Textlayer als erkannten Fall vormerken','Kein stiller OCR-Erfolg.',5],
  ['PDF-Binärdatei von kopiertem Plantext unterscheiden','UI/Parser benennt Ursprung eindeutig.',5],
  ['Nur lesbare Seiten in einen begrenzten Textbuffer übernehmen','Seiten- und Größenlimit im Test.',10],
  ['Seitenindex beim Extrahieren eines Textspans mitführen','Kandidat nennt die richtige PDF-Seite.',8],
  ['Planbeschriftungs-DN 30 als PDF-Text-Fall prüfen','Kandidat 30 samt Blattstelle.',8],
  ['Planbeschriftungs-DN 160 als PDF-Text-Fall prüfen','Kandidat 160 samt Blattstelle.',8],
  ['DM-Angabe von DN-Wortlaut in Testfixture unterscheiden','Keine falsche Innen-/Außenmaßgleichsetzung.',8],
  ['Rohr-ID im Textlayer nur mit eindeutiger Nachbarschaft zuordnen','Mehrdeutige ID bleibt offen.',8],
  ['Beschriftungszeile über Zeilenumbruch hinweg negativ prüfen','Nicht zusammengehörige Angaben nicht verknüpft.',8],
  ['Zwei gleiche DN-Bezeichnungen auf derselben Seite prüfen','Unterschiedliche Rohr-IDs bleiben möglich.',8],
  ['Fehlende Zeichnungsmaßstäbe als eigenen Datenstatus führen','Kein mm-Ø aus Pixelabstand behauptet.',5],
  ['Seitenrotation beim Plan-Textlayer als Testfall ergänzen','ID-/Seitenbezug bleibt konsistent.',8],
  ['Scan ohne Textlayer als manuell zu prüfende Quelle anzeigen','OCR-Lücke erklärt statt erfunden.',5],
  ['Sichtbare Kandidatentabelle nach Rohr-ID und Maß sortieren','Keine stille Auswahl des ersten Treffers.',8],
  ['Quellzeile, PDF-Datei und Blatt am Kandidat zeigen','Herkunft für Prüfer nachvollziehbar.',8],
  ['Anwendung auf falsche Rohr-ID automatisch zurückweisen','Quelle und Ziel-ID müssen übereinstimmen.',8],
  ['Kandidat ohne Rohr-ID nur nach explizitem Nutzerentscheid anbinden','Keine automatische Fantasiezuordnung.',8],
  ['Reviewstatus aus Parser strikt auf candidate begrenzen','Keine automatische technische Freigabe.',5],
  ['Mehrseitenbeispiel mit gleichem Rohr als Konflikt markieren','Widersprüchliche Maße offengelegt.',8],
  ['PDF-Import-Abbruch ohne Teiländerung am Projekt prüfen','Vorher-Snapshot gleich.',8],
  ['Dateigröße/Seitenzahl des PDF-Parsers als Grenzen testen','Verarbeitungsbudget überprüft.',8],
  ['Roundtrip der Planquelle durch JSON-Projekt sichern','Seite/Zeile/Version erhalten.',8],
  ['Ein echtes beschriftetes PDF als externe Testvoraussetzung eintragen','Blocker eindeutig: Originalplan fehlt/vorhanden.',5],
  ['PDF-Gegenprobe für 13_U_028 ohne veränderte 91 LR ausführen','Quelle bleibt vor/nach identisch.',8],
  ['Browser-Kandidatenliste mit einer positiven und einer negativen Seite prüfen','UI bietet Auswahl, kein automatischer Ersatz.',8],
  ['Fehlerbericht bei unerwartetem PDF-Textformat anlegen','Reproduzierbare, isolierte Fehlseite.',8],
  ['Quelltextrechte und OCR-Grenze im Hinweis dokumentieren','Parser nicht als fertiges trainiertes KI-System etikettiert.',5],
  ['Relevante Unit-/Browserregression in npm test anbinden','Grüner Lauf oder echter Fehler protokolliert.',8],
  ['Bros PDF-Dimension-Anforderung anhand nachweisbarer Beispiele abgleichen','Positive und negative Fälle aufgelistet.',8]
 ],
 'KAB-0882':[
  ['Pixelgröße und Bildquelle pro Kreiskandidat einzeln speichern','Datei und px-Einheit vorhanden.',5],
  ['Skalenreferenz als eigene Eingabe statt impliziter DN verwalten','Referenzkreis-ID und physisches Maß getrennt.',8],
  ['Test für Kreis bei heller Umgebung aufbauen','Kandidat mit erwarteten Pixelkoordinaten.',8],
  ['Test für Kreis bei dunkler Umgebung aufbauen','Grenzfall für Schwellenwert.',8],
  ['Test für zwei benachbarte Kreisöffnungen ergänzen','Keine Verschmelzung zu nur einem LR.',8],
  ['Test für DN50-ähnlichen und DN110-ähnlichen Kreis bauen','Relatives Größenverhältnis prüfbar.',8],
  ['Test für Perspektive: elliptische Öffnung als unsicher markieren','Kein metrischer Wert ohne Entzerrung.',8],
  ['Test für abgeschattete Öffnung ergänzen','Falschnegativ dokumentiert.',8],
  ['Test für Blendfleck ohne Öffnung ergänzen','Falschpositiv nicht automatisch übernommen.',8],
  ['Test für Textbeschriftung ohne sichtbaren Kreis ergänzen','Bildparser erfindet keine Öffnung.',8],
  ['Test für Überlappung an Bildkante ergänzen','Abgeschnittener Kreis nicht sicher ausgemessen.',8],
  ['Kalibrierung mit Referenzmaß 50 mm kontrollieren','Pixel/mm-Faktor sichtbar berechnet.',8],
  ['Kalibrierung mit Referenzmaß 110 mm kontrollieren','Skalierung nicht hartkodiert.',8],
  ['Null-/NaN-Skalenfaktor vor Persistierung abweisen','Kein NaN-mm-Wert im Projekt.',5],
  ['Referenzkreiswechsel als Undo-fähige UI-Aktion testen','Frühere Kalibrierung nicht überschrieben.',8],
  ['Kalibrierungsstatus nach Bildwechsel zurücksetzen','Altes Maß nicht auf neues Foto angewandt.',8],
  ['Nichtkalibrierte Bilder nur nach relativer Größe sortieren','Keine Millimeter-Spalte ohne Referenz.',8],
  ['Perspektive/Objektivverzerrung als separate Warnung anzeigen','Keine falsche Fachgenauigkeit.',8],
  ['Quellbild/Index nach Kandidatenübernahme speichern','Referenz für spätere Überprüfung erhalten.',8],
  ['Rohr-ID aus Foto nie automatisch als geprüft setzen','Unbekannte ID bleibt manuelle Zuordnung.',8],
  ['Kandidaten außerhalb DN30–160 nicht still abschneiden','Echter Bildbefund bleibt sichtbar, unbestätigt.',8],
  ['Vier unterschiedliche Bilddateiformate inkl. ungültigem Fall prüfen','PNG/JPEG/WebP zulässig, andere abgefangen.',8],
  ['Große Bilder vor Analyse bewusst verkleinern','Originalpixel und Skalenänderung getrennt.',8],
  ['Speicherverbrauch bei 4-MP-Grenze kontrollieren','Kein unkontrolliertes 100-MP-Array.',8],
  ['Browserabbruch beim Bildwechsel ohne Projektmutation prüfen','Kein alter Kandidat angewandt.',8],
  ['Qualitätsscore eines eindeutig gültigen Kreises prüfen','Qualität als Schätzung, nicht Wahrscheinlichkeit.',8],
  ['Foto-Kandidatenliste auf Mehrfachauswahl-/ID-Fehler prüfen','Nutzer wählt konkret den gewünschten Kreis.',8],
  ['Synthetische Foto-Fixtures im Regressionslauf ergänzen','Positive und negative Fälle CI-reproduzierbar.',8],
  ['Echte Referenzfotos mit bekannter Skala als Blocker dokumentieren','Vorlage explizit erforderlich, nicht erfunden.',5],
  ['Vergleich gegen bekannte Rohrdimensionen nur quellengebunden ausgeben','Kein ungeprüftes reales mm-Maß.',8],
  ['Bros Anforderungen und tatsächliche Erkennungsgrenzen gegenüberstellen','Status Prototyp vs. validierte Erkennung klar.',8]
 ]
};
function classify(t){
 const text=t.title.toLowerCase(),epic=EPICS[t.epicId]||['data','app.js'];
 let type=epic[0],file=epic[1];
 if(/(?:\btest\b|tests|testfall|regression|assert|fixture|smoke|durchtesten|browsertest)/.test(text))type='testing';
 if(/(?:\bpdf\b).{0,32}(?:les|extrahier|scan|erkenn|import)|pdf.vektor/.test(text)&&!/(?:bericht|export)/.test(text))type='pdf';
 if(/(?:bild|foto).{0,35}(?:erkenn|analys|kandidat|segment|maßstab|kalibrier)|bilderkennung/.test(text))type='image';
 if(/(?:hersteller|avl0116|regelgrenze|fachlich|bauart|brandschutz|freigabe|75 mm|100 mm)/.test(text)&&
    !/(?:screenshot|testfall)/.test(text))type='compliance';
 if(/(?:dxf|dwg|ifc|bim|cad|georeferenz|punktwolke)/.test(text))type='cad';
 if(/(?:tabelle|csv|xlsx|excel).{0,32}(?:import|einles|parsen)|csv-schem|tabellenimport/.test(text))type='import';
 if(/(?:bericht|prüfheft|pdf).{0,35}(?:export|erzeug|zusammenstell|layout)|(?:pdf|excel).{0,35}(?:bericht|ausgabe)/.test(text))type='report';
 if(/(?:originaldatei|historische original|altchat behauptet|tatsächlich.*datei|originalplan.*nachprüf|planseite.*nachprüf)/.test(text))type='reference';
 if(/(?:timo|bro.*(?:feedback|abnahme)|rücksprache)/.test(text))type='review';
 if(/(?:readme|dokumentier|glossar|handreichung|checkliste|fazit|beschreib)/.test(text)&&type!=='reference')type='docs';
 // Exact known comparison and original page verification remain a source task.
 if(t.epicId==='P0-01'||t.epicId==='P2-14'||t.epicId==='P1-14'){
  if(/(?:abgleich|verifizier|prüf|zuordn|inventarisier)/.test(text))type='reference';
 }
 // The large baseline epic determines most short single-function tasks.
 return {type,file};
}
function render(s,focus,file,anchor){
 return s.replace(/\{focus\}/g,focus)
  .replace(/\{file\}/g,file).replace(/\{anchor\}/g,anchor);
}
function newSteps(t){
 const {type,file}=classify(t),focus=t.title.length>128?
  t.title.slice(0,125).replace(/\s+\S*$/,'')+'…':t.title;
 const anchorMatch=t.title.match(/(?:SW[1-4](?:[-–\s][A-Za-z0-9-]+)?|13_[A-Z]_\d+(?:\.\d+)?|(?:DN|DM)\s*\d+|(?:KAB-\d{4})|(?:PDF|CSV|XLSX|AVL0116))/);
 const anchor=anchorMatch?anchorMatch[0]:focus;
 let steps=(TEMPLATES[type]||TEMPLATES.data).map(([action,result,min])=>({
  action:render(action,focus,file,anchor),result:render(result,focus,file,anchor),min
 }));
 // Explicitly expose the code/plan entry point and a result tailored to this
 // task rather than treating all 18 tickets of an epic as the same job.
 steps[0].action=steps[0].action+' · Einstieg: '+file;
 const title=t.title.toLowerCase();
 const add=(a,b,min=5)=>steps.push({action:a,result:b,min});
 if(/(?:\bsw[1-4]\b|trasse|rohr.?nr|leerrohr.*\d+)/i.test(t.title))
  add('Den in der Anforderung benannten Wand-/Rohrbezug separat gegenprüfen',
   'Referenz '+anchor+' durch die zugehörige Quell-ID bestätigt oder als fehlend markiert.',5);
 if(/(?:75 mm|100 mm|dn\s*\d+|dm\s*\d+|mm\b)/i.test(t.title))
  add('Explizit genannte mm-/DN-Zahl mit Quelle und Einheit gegenprüfen',
   'Keine automatisch gleichgesetzten Innen-, Außen- oder Lochdurchmesser.',5);
 if(/(?:\bohne\b|niemals|nicht|keine\b|unbekannt)/i.test(title))
  add('Verbotenen Nebeneffekt aus der Anforderung als Negativtest isolieren',
   'Unerlaubtes Verhalten tritt nach der Änderung nicht auf.',8);
 if(/(?:alle|je schacht|jeden|mehrseitig|vollständig)/.test(title))
  add('Vollständigkeit für einen zweiten Datensatz statt nur die erste Fixture prüfen',
   'Keine still ausgelassenen Zeilen/IDs.',8);
 return {type,file,steps};
}
function generate(read){
 const tasks=readTasks(read),pilot=seedMicro(read);
 const rows=parseCSV(read('PRIORITY_ORDER.csv'));
 assert.equal(rows.length,889,'Rangliste muss exakt 888 Eltern-Tasks enthalten');
 const stage={};for(let k=0;k<=6;k++)stage['S'+k]={parents:[],count:0,mins:0,md:[],csv:[]};
 const allIds=new Set(),seenParents=new Set();
 for(const row of rows.slice(1)){
  assert.equal(row.length,7,'falsches Priority-CSV-Format');
  const [rank,id,wave,section,need,title,file]=row;
  assert.match(wave,/^S[0-6]$/);
  assert.ok(!seenParents.has(id),'Parent mehrfach in Rangliste: '+id);
  seenParents.add(id);
  const t=tasks.get(id);
  assert.ok(t,'Parent fehlt: '+id);
  assert.equal(t.title,title,'Beschreibung nicht synchron: '+id);
  assert.equal(t.file,file,'Backlog-Datei nicht synchron: '+id);
  if(t.done)continue;
  const st=stage[wave];
  const micro=[...(pilot.get(id)||[])];
  if(id!=='KAB-0885'){
   const custom=FULL_EXTRA[id]||null;
   const next=custom?custom.map(([action,result,min])=>({action,result,min})):newSteps(t).steps;
   micro.push(...next);
  }
  assert.ok(micro.length>=8,'Parent nicht ausreichend zerlegt: '+id);
  if(id!=='KAB-0885'&&pilot.has(id))assert.ok(micro.length>=35,
   'Spezialparent nicht vollständig mit PDF-/Foto-Aufgabe zerlegt: '+id);
  const headline='### '+id+' · '+t.title;
  st.md.push(headline,'','**Priorität:** '+rank+' · **Parent-Epic:** '+t.epic+
   ' · **Einstieg:** '+classify(t).file+
   ' · **Externe Voraussetzung:** '+(need==='Abhängigkeiten pro Task prüfen'?'bei Ausführung prüfen':need), '');
  st.parents.push(id);
  for(let i=0;i<micro.length;i++){
   const step=micro[i];
   const no=i+1;
   const microId=id+'/M'+String(no).padStart(2,'0');
   assert.ok(!allIds.has(microId),'Doppelte Mikro-ID '+microId);
   allIds.add(microId);
   assert.ok(step.min>=2&&step.min<=10,'Mikrostep über 10 Minuten: '+microId);
   assert.ok(step.action.length>=12&&step.result.length>=10,
    'Triviale/fehlende Handlung oder Abnahme: '+microId);
   const item='- [ ] **'+microId+'** · **'+step.min+' Min.** – '+
    step.action+' **Fertig, wenn:** '+step.result;
   st.md.push(item);
   st.csv.push([rank,id,microId,wave,t.epic,
    step.action,step.result,step.min,'offen',need,t.file].map(quote).join(';'));
   st.count++;st.mins+=step.min;
  }
  st.md.push('');
 }
 assert.equal(seenParents.size,tasks.size,'Keine Parent-ID darf fehlen');
 const open=[...tasks.values()].filter(t=>!t.done);
 assert.equal(open.length,869,'Erwartet: 869 offene kanonische Parents');
 assert.equal(allIds.size,Object.values(stage).reduce((n,s)=>n+s.count,0));
 assert.equal(Object.values(stage).reduce((n,s)=>n+s.parents.length,0),open.length);
 const output={};
 const head=['Rang','Parent-ID','Mikro-ID','Welle','Epic','Handlung','Fertig-wenn','Ziel-Min.','Status','Quellenabhängigkeit','Parent-Datei'].map(quote).join(';');
 const summary=[];
 for(const [wave,st] of Object.entries(stage)){
  const filename='ATOMIC/'+wave;
  output[filename+'.md']=[
   '# Kabelsalat · '+wave+' · vollständig zerlegte offene Eltern-Tickets',
   '',
   '**Stand:** 13.11.2026 · '+st.parents.length+' offene Parent-Tickets · '+st.count+
   ' Mikroschritte à 2–10 Minuten. Dies sind Zielgrößen, keine gemessenen Ausführungszeiten.',
   '**Regel:** Keine Fachfreigabe und kein erledigter Code durch diese Planungsdatei. Bei realem Teilaufwand über 10 Minuten weiter zerlegen.',
   '',
   ...st.md
  ].join('\n')+'\n';
  output[filename+'.csv']=head+'\n'+st.csv.join('\n')+'\n';
  summary.push({wave,parents:st.parents.length,count:st.count,mins:st.mins});
 }
 const parentTotal=summary.reduce((n,s)=>n+s.parents,0),microTotal=allIds.size;
 assert.ok(microTotal>=8500,'Alle Parents zu fein zerlegen: aktuell '+microTotal);
 const index=[
  '# Kabelsalat · alle offenen Tasks atomar zerlegt',
  '',
  '**Vollständig:** '+parentTotal+' von '+parentTotal+' offenen Parent-Tickets auf '+microTotal+
   ' einzeln abhackbarer Mikroschritte (2–10 Minuten Zielaufwand) verteilt.',
  '**Kanonischer Gesamtstand:** 888 Parent-Tickets, 19 erledigt, 869 offen. Hier sind **keine abgeschlossenen Eltern-Tickets** nochmals aufgeführt.',
  '',
  '| Priorität | Offene Parents | Mikro-Tasks | Nachschlagen | CSV |',
  '|---|---:|---:|---|---|',
  ...summary.map(s=>'| '+s.wave+' | '+s.parents+' | '+s.count+' | ['+s.wave+
   ' Mikroaufgaben](./ATOMIC/'+s.wave+'.md) | ['+s.wave+' CSV](./ATOMIC/'+s.wave+'.csv) |'),
  '| **Gesamt** | **'+parentTotal+'** | **'+microTotal+'** | | |',
  '',
  '## Verbindliche Arbeitsregel',
  '',
  '- **ID-Schema:** KAB-XXXX bleibt das Feature-/Prüf-Ticket. KAB-XXXX/M01 bis Mnn sind die atomaren Schritte, ohne Parent-IDs zu ersetzen oder bei der 888er-Zählung mitzurechnen.',
  '- **Eine einzelne Handlung + überprüfbares Fertig-Kriterium**; 2–10 Minuten sind eine Planungs-Zielgröße für meine aktive Arbeit, keine Zusage. Wenn der Schritt real größer wird, noch vor Umsetzung weiter teilen.',
  '- **Schrittstatus:** Die generierten Mikroschritte stehen zunächst alle auf offen. Bereits bestehende Funktionalität ist kein Abnahmenachweis; Test/Code/Commit separat prüfen.',
  '- **Externes:** Warten auf Bro, originale PDFs/XLSX, Hersteller oder Brandschutz-Fachentscheidung ist keine 5-Minuten-Arbeit. Solche Punkte bleiben blockiert, bis Daten vorliegen.',
  '- **Sicherheit:** Keine echten Durchmesser aus unvermessenen Bildern erfinden; DN, Innen-, Außen- und Lochdurchmesser getrennt halten. Demo verändert keine bestätigten Rohrmittelpunkte.',
  '- **Mikro-Aufwände nicht summieren, um Liefertermine zu versprechen.** Die alte grobe Parent-Schätzung wurde nicht proportional verteilt; Integration/Unsicherheit muss nach Durchführung real neu bewertet werden.',
  '',
  '## Aktualisieren / Prüfen',
  '',
  'Aus PC/Kabelsalat: node TASKS/generate-atomic.js --write (regenerieren); '+
  'node TASKS/generate-atomic.js --check (Vollständigkeit, Einzigartigkeit, keine erledigten Parent-IDs, Maximaldauer und Git-Abgleich).',
  '',
  'Vorheriger 42-Schritte-Pilot unverändert archiviert: [Originalpilot](./ATOMIC/PILOT_ORIGINAL.md). '+
  'Seine IDs sind in S0 enthalten; KAB-0881 und KAB-0882 wurden um die vollständigen PDF-/Foto-Teilpfade ergänzt.',
  ''
 ].join('\n');
 output['ATOMIC_TASKS.md']=index;
 return {output,summary,parentTotal,microTotal,seedTotal:42};
}
function main(){
 const root=__dirname,read=name=>fs.readFileSync(path.join(root,name),'utf8');
 const result=generate(read);
 if(process.argv.includes('--write')){
  fs.mkdirSync(path.join(root,'ATOMIC'),{recursive:true});
  for(const [rel,content] of Object.entries(result.output)){
   fs.writeFileSync(path.join(root,rel),content,'utf8');
  }
  console.log('GENERATED '+result.parentTotal+' open parents / '+result.microTotal+' microsteps');
 }else if(process.argv.includes('--check')){
  for(const [rel,expected] of Object.entries(result.output)){
   const actual=read(rel).replace(/\r\n/g,'\n');
   assert.ok(actual===expected,'Atomarer Tracker veraltet: '+rel+' · --write ausführen');
  }
  console.log('ATOMIC OK: '+result.parentTotal+' open parents, '+result.microTotal+
   ' unique 2–10 minute tasks, '+Object.keys(result.output).length+' tracked outputs');
 }else console.log('PREVIEW: '+result.parentTotal+' parents → '+result.microTotal+' microsteps');
}
module.exports={generate,parseCSV,readTasks,seedMicro};
if(require.main===module)main();
