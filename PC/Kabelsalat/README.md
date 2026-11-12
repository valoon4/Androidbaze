# Kabelsalat – Schacht- und Kabeleditor

**Status: Demo-Beta v2.0.6-beta.1 – Konzept teilweise erfüllt, nicht fachlich abgenommen** (Umbenennung des bisherigen Schachtwand Planer 3D). Eigenständig als **Offline-Browseranwendung** und als **portable Windows-EXE** nutzbar. Verwendet ein und dasselbe Daten- und Darstellungsmodell.

## 2.0.6-beta.1 · Nachtrag „Warum sind die LR so lang?“ · 12.11.2026

**Ursache im bestehenden 3D-Renderer:** Die langen farbigen Objekte im Schacht sind *Kabelwege*, keine Leerrohre. Echte LR werden hier als **runde Durchbrüche in der Betonwand** gezeichnet (Wanddicke bleibt wie im Projekt, ohne neues Aufmaß). Im vorherigen Update wurde nur das frei endende Kabelstück von 360 auf 160 mm verkürzt; das beseitigte die Verwechslung noch nicht.

- **Basic → „2 · Schachtansicht“:** Jetzt nur Betonwand und echte LR-Öffnungen (ohne lange Kabel-Tubes). 2D- und 3D-Wandübersicht entsprechen sich.
- **Basic → „3 · Kabelbelegung“:** Hier bleiben alle schematischen farbigen Kabelwege **voll sichtbar**. Beschriftung erklärt ausdrücklich: Kabel ≠ Leerrohre.
- **Profi-Modus:** Verändert sich nicht; volle Routen für Planung/Bearbeitung sichtbar.
- **Reiner Präsentationsfilter:** Rohr-ID, DN/Lochdurchmesser, Position, Wand-/Betondicke, Kabelendpunkte und Projektdatei bleiben unverändert.
- **Offen:** Ob Bro darüber hinaus die tatsächliche **Länge/Tiefe der LR im Beton** kürzer meint, lässt sich ohne visuelle Zuordnung und dokumentiertes Aufmaß nicht fachlich bestimmen. Deshalb keine willkürliche Änderung der Bauteiltiefe. Browser-Screenshots für beide Modi werden in `tests/browser-visual.js` erstellt.

## 2.0.5-beta.1 · Bros jüngstes LR-Feedback · 12.11.2026

- **Kürzer:** Bisherige 360-mm-Kabelend-Markierungen bei nur einseitig bekannten Anschlüssen sind in der 3D-**Visualisierung** auf 160 mm verkürzt; End-Symbol und Linienstärke passen sich kleinen Rohröffnungen an. Das sind keine echten Rohrlängen und keine behaupteten realen Kabellängen.
- **DN30–160 proportional:** Neue/editierte LR im Editor bieten Nennweiten von DN30 bis DN160 in 1-mm-Schritten. Bereits vorhandene importierte Werte außerhalb dieses Eingabebereichs werden beim Laden nicht zerstört.
- **Fiktiver Vergleichsschacht:** In der Beispielauswahl gibt es „4 · LR-Größen DN30–160 (FIKTIVE DEMO)“ mit DN30/40/50/60/63/75/90/110/116/125/140/160; DN50 ist sichtbar kleiner als DN110, ohne dass Bestandspläne erfundene Werte bekommen.
- **Test:** `tests/bro-dn-range.test.js` und ein optionaler Screenshot aus dem realen Chromium-Browser vergleichen Größe, Rohr-IDs, offene Quellen und Persistenz. [Neuer Backlogblock](./TASKS/08-BRO-KURZE-LR-DN30-160.md).
- **Offen:** Bros Sichtabnahme, exakte Rohr-Außen-/Lochdurchmesser und Originalaufmaß; DN ist weiterhin keine bestätigte Betonöffnung.

## 2.0.4-beta.1 · Bros Wandachsen und lichten Rohrabstände · 12.11.2026

- **Wandlage und Ansichten SW1–SW4** werden nun separat gegen den PDF-V2-Weltachsenbezug geprüft; feste 3D-Normalen, kameraseitige Blickrichtung und reversible U/V↔XYZ-Transformation (KAB-0019–0023).
- **Schematische Rohrabstand-Analyse** zeigt die geringste lichte Weite und den geringsten Wandrandabstand aus Modell- und Bildpunkten. DN160/DN110-Nachbarn, DN75 in SW2-Trasse-3 und DN63 in SW1-Trasse-2 werden mit getrennten Radien behandelt (KAB-0044–0047).
- **Geometrische Überschneidungen** werden im 2D-Wandplan und 3D-Schacht als rote Warnringe sichtbar, auch in der optionalen 62-%-Vorschau, **ohne** Rohrachsen oder Anschluss-IDs zu verändern (KAB-0048).
- **Verbindliche Grenzen:** Diese Werte stammen aus schematischen Plan-/Modellkoordinaten und sind **keine gemessenen Betonabstände oder Norm-/Brandschutzprüfung**. Details und Testergebnisse: [Wandachsen und Rohrabstandsprüfung](./WALL_COORDINATES_AND_CLEARANCE.md).

## 2.0.3-beta.1 · Bros 10er-Sprint – 12.11.2026

- **Betonblock:** Dunkelgraue, dezent gekörnte Betonoberfläche im Offline-WebGL-Rendering; echte Lochdurchbrüche statt hervorstehender Metalldemo-Rohre. Optisches Feedback von Bro zu Farbe/Form weiterhin offen.
- **Rohrdimensionen:** DN/Nennangabe, Innen-Ø, Außen-Ø, Beton-Loch-Ø, Erkennungskandidat und prüfbare Quelle/Prüfer als getrennte Eigenschaften. Unterschiedliche Öffnungsmaße steuern Geometrie, 2D-Auswahl und Kollisionen. 60/110/116 mm sind hier synthetische Beispiele, keine real bestätigten Schachtmaße.
- **Plantext/Fotos:** Prototypische **lokale** Erkennung von beschrifteten DN-/DM-/Ø-Textzeilen (TXT oder Eingabefeld) und visuellen Kreiskandidaten (Bild mit manueller Referenzmaß-Kalibrierung). **Keine** automatische PDF-Binärerkennung, trainierte KI, OCR oder verlässliche Rohrtypklassifikation; die Quellfreigabe bleibt manuell.
- **Originalpläne geschützt:** 62-%-Verdichtung wird ausschließlich als umschaltbare Basic-Ansichtstransformation verwendet. Gespeicherte PDF-Quellmittelpunkte und ältere manuell verschobene Rohre werden nicht stillschweigend angepasst. Feste Regressionen zu 48 + 78 + 91 Rohrpositionen und 120 Kabelobjekten mit unveränderten vorhandenen bzw. fehlenden Anschlussreferenzen.
- **Tests/Builds:** Node-Modultests, 97 PDF-Quellprüfungen und 17 lokale CSP-geprüfte Offline-Skripte über GitHub Actions.
- **Export-Audit:** Auch Offline-Projektviewer und druckbarer Prüfbericht beschriften PDF-Vektorpositions- und Modell-mm-Werte jetzt ausdrücklich als **nicht vermessen**. Die PDF-V2-Wandspannen (SW1/SW3 Breite; SW2/SW4 Tiefe) werden auch im Viewer korrekt gezeichnet. KAB-0049 getestet und abgeschlossen. Aktuelle Einzelabnahmen und offene Blocker: [Sprintbericht](./TASKS/SPRINT_2026-11-12_BRO_10.md).
- **Entwicklungsstatus:** [aktuelle Checkboxen und Nachweise](./TASKS/README.md), [Rangliste aller 888 Tasks](./TASKS/PRIORITY_ORDER.csv); Bauartgrenze 75/100 mm und tatsächliche AVL-Konformität nicht abschließend geklärt.

## 2.0.2-beta.1 · KAB-0877 – Betonblock mit runden Durchbrüchen

- **Rendering-Korrektur:** Die zuvor vorhandene Extrusionsgeometrie mit kreisförmigen Löchern wird nun als **zusammenhängende, weitgehend deckende Betonwand** mit getrennt schattierten Durchbruch-Innenflächen dargestellt. Die bisher optisch dominierenden 20-cm-Metallhülsen und aufgesetzten Randkragen sind in der PDF-V2-Ansicht entfernt.
- **Keine Referenzdatenänderung:** Alle Schachtwand-IDs, Rohranzahlen, Endpunkte, DN, Koordinaten und Belegungen bleiben unverändert. `13_T_021`, `13_U_028` (HTML) und die 91-Rohr-PDF-V2-Demo werden in neuen Regressionstests geprüft.
- **Weiterhin offen:** KAB-0878 (finale Betonfarbe/Textur nach Bros Geschmack), KAB-0879–0883 (gemischte reale Größen und geprüfte Erkennung), KAB-0832 (Trennung der 62-%-Demo von fachlichen Rohrpositionen). Die aktuelle Rendering-Demo ist **nicht** maßhaltig oder fachlich abgenommen.
- **Abnahme:** [GitHub Issue #70](https://github.com/valoon4/Androidbaze/issues/70) bleibt bis zur visuellen Rückmeldung von Bro offen.

## 2.0.1 – PDF-Wandbelegung und kompaktere Leerrohre

- Die **13_U_028-PDF-V2-Szene** verwendet die dokumentierten **91 Leerrohre auf SW1–SW4** (29/13/26/23), ohne Trassen/LR-IDs, DN oder Kabelbelegungen zu verändern.
- In der **3D-/Editor-Demo** sind schematische Mittelpunktsabstände zwischen den Leerrohren horizontal und vertikal auf **62 %** reduziert, zentriert zur Wandmitte. Die originalen PDF-Kreismittelpunkte stehen getrennt in `sourceU/sourceV`; die PDF-Wandbilder zeigen nach wie vor diese Originalpositionen. Dies sind weder vermessene Einbauabstände noch eine Fachfreigabe.
- Über **„Aktuelle PDF-Wandbelegung (91 LR) neu laden“** ist die Referenz bewusst neu ladbar, auch wenn im Browser ein altes Modell gespeichert ist. Die vorhandene Rückfrage schützt eigene Projektänderungen. Bestehende Autosaves und importierte JSON-Projekte werden nicht stillschweigend überschrieben.
- Fehlende Endpunkte, Biegeradien und Brandschutzfreigaben bleiben offen.

## 888 konkrete Tasks – inklusive LR-DN-Nachtrag vom 12.11.2026

Der kanonische [Task-Tracker mit 888 nummerierten Aufgaben](./TASKS/README.md) enthält den 828er-Basisplan, 48 Aufgaben aus Bros altem Chat und 8 weitere Anforderungen zu Betonblock-Optik und DM/DN-Erkennung ([Quellabgleich](./TASKS/BRO_CHAT_CROSSWALK.md)). **Der Live-Status steht im Tasktracker (19 erledigt / 869 offen / 888 gesamt vor der neuen Sichtprüfung)**; frühere Meilensteine und 97 PDF-Visualtests sind keine erledigten Tasks. Neue zusätzliche Aufgaben folgen ab KAB-0889. Die zuletzt hinzugefügten [12 Detailaufgaben](./TASKS/06-BRO-CHAT-PRUEFFALL-UND-ARTEFAKT-QA.md) prüfen insbesondere historische PDF-/Excel-Ergebnisse und die echte 92-Schacht-Prüfliste. `npm test` validiert Task-IDs und Statuszähler.

**Neue Umsetzungsvorgabe:** Die [Priorisierung aller 888 Tasks](./TASKS/PRIORITIES.md) und [globale Rangliste](./TASKS/PRIORITY_ORDER.csv) richten sich vorrangig nach Bros jüngstem Praxisfeedback: klassischer dunklerer Betonblock mit echten unterschiedlich großen Öffnungen, sichtbaren Rohrmaßen und überprüfbarer PDF-/Foto-Erkennung. **KAB-0877 ist technisch umgesetzt, aber Bros Sichtabnahme noch offen.**

**Fachlicher Hinweis zu v2.0.1:** Die aktuelle visuelle Zuordnung OLA=A und MS=B ist ein **historischer Demofall**, kein fachliches Auswahlkriterium: Im älteren Chat hängt A/B von der Höhe der Rohrunterkante über Schachtboden ab (bis 100 mm versus darüber). Eine spätere 75-mm-Bildvorgabe widerspricht dem; beide Werte sind bis zum belastbaren Abgleich **ungeklärt**. Die 62-%-Rohrverdichtung verändert Demo-Anzeigekoordinaten und darf **nicht** als reales Aufmaß oder unveränderte Rohrachsen im 3D-Fachmodell interpretiert werden. Siehe KAB-0829–0840.

## Herunterladen und starten

Die aktuellen Dateien werden automatisiert im GitHub-Release veröffentlicht, sobald der Windows-/Offline-Build für `v2.0.3-beta.1` erfolgreich endet. Die darunter genannten Versionen `1.9.3` sind **nur historische Beispiele**; die tatsächlich angebotenen Artefaktnamen im Release prüfen. Im GitHub-Release [kabelsalat-latest](https://github.com/valoon4/Androidbaze/releases/tag/kabelsalat-latest) gibt es:

- `Kabelsalat-1.9.3-beta.1-Browser-Offline.zip`: herunterladen, **vollständig entpacken**, `index.html` in einem aktuellen Chrome/Edge öffnen. Kein Node.js, keine Installation und keine Serververbindung nötig. Der Ordner `vendor/` muss neben `index.html` bleiben.
- `Kabelsalat-1.9.3-beta.1-Windows.exe`: portable Windows 10/11-Anwendung mit Electron. Doppelklick; keine dauerhafte Installation erforderlich.

*Hinweis zur Datenübernahme:* Vor dem Wechsel vom alten Schachtwand Planer die Projekte über **Projekt speichern** als JSON exportieren und anschließend in Kabelsalat über **Öffnen** laden. Das lokale Autosave kann durch die neue App-Identität anders gespeichert werden.

## Vorhandene Funktionen

- Vier Wände (SW1–SW4) mit eigenen Wandstärken und Leerrohrdurchmessern; Raster, freie Platzierung, Rohrkopie mit neuer stabiler ID und millimetergenaue numerische Eingaben.
- **Gleichzeitige 2D- und 3D-Ansicht:** rechts eine millimetergenau skalierten Wandzeichnung, links das live aktualisierte 3D-Modell. Rohrauswahl, Positionen und Duplizieren verwenden dieselben Objekte und IDs. Die orthografischen Wandansichten sind zusätzlich verfügbar.
- Farbige Kabel mit Start-/Zielrohr, Durchmesser, Zuordnung zu OLA/MS/Weitere und frei editierbaren Kurvenpunkten.
- **OLA/MS mit sechs getrennten Platten je Baugruppe:** Einzel-IDs, Material, Breite/Länge, Dicke, Versatz, Sichtbarkeit und Deckkraft je Platte. 3D-Auswahl und vereinfachte geometrische Fremdrohr-Warnungen (keine Fachfreigabe).
- Einfache geometrische Warnungen für sich überlagernde Leerrohre und Überschreitungen von Wandrändern. Die 2D-Zeichnung ermittelt zusätzlich den kleinsten geometrischen Rohr-/Randabstand in mm, **ohne** normative Freigabe.
- **Konsolen/Auflager:** Hinzufügen, Benennen, Wandzuordnung, X/Y/Z-Position, Maße, Sichtbarkeit, Auswahl und Darstellung in 2D/3D.
- **Projektvarianten:** bis zu zwölf benannte Planstände mit Vergleich, Wiederherstellung und Persistenz innerhalb der JSON-Datei.
- **Schachtnetz:** bis zu 30 unabhängige Schächte mit eigenen Kabeln/Wänden. Globale X/Z-mm-Positionen, editierbare Schacht-Links, zoombare Netzkarte und nachverfolgbare Kabelsegmente.
- **CSV:** Netz-Kabelinventar exportieren, globale Schachtkoordinaten nach Prüfung und Bestätigung importieren.
- Lokal speichern/laden als JSON mit Netz, Platten, Auflagern und Varianten; PNG-Export und Rückgängig/Wiederholen.

## Konzeptabgleich (verbindliche Produktanforderungen, noch nicht erfüllt)

Das bereitgestellte Konzept beschreibt **zuerst den vollständigen maßhaltigen 2D-/3D-Editor** und **danach die vernetzte Vogelperspektive mit Schacht- und Kabelverfolgung**. Die bereits bestehende Version deckt nur einen Teil davon ab:

- **Offen – Editor:** frei anlegbare Wände statt festem 4-Wand-Schema, fachlich erforderliche Mindestabstände, 2D-Direktbearbeitung von Kabeln und Platten, gerader Standardverlauf gegenüberliegender Anschlüsse sowie kontrollierte Mindestbiegeradien.
- **Offen – Einhausungen:** fachlich validierte Kollisions-/Brandschutzprüfungen und manuelles Verschieben der Platten direkt im 3D-/2D-Modell. Die jetzige geometrische AABB-Warnung ist nur näherungsweise.
- **Offen – weitere Bauteile:** geprüfte Konstruktion/Tragfähigkeit, Mindestabstände und Befestigungsdetails. Geometrische Konsolen/Auflager lassen sich bereits bearbeiten.
- **Offen – Projektworkflow:** PDF-Export, interaktiver Viewer und visuelle detaillierte Variantendifferenz. Lokale Planungsstände können bereits gesichert, verglichen und geladen werden.
- **Offen – Netz:** real überprüfte Leitungskontinuität über Schachtgrenzen, physische Schachtübergänge, unbegrenztes Netz und projektweite Suche. Die Netzansicht und schematische Segmentverfolgung existieren seit v0.6.
- **Offen – Import/Prüfung:** CAD/BIM-/Plan-/Bestandsimport, KI-Erkennung, Quellen-/Prüfstatus-/Änderungshistorie, fachlich geprüfte Abstände/Kollisionen. CSV-Positionsimport ist bereits vorhanden.

Technische Planung, tatsächliche Bauausführung und Abnahmen dürfen **nicht** allein auf dieser Software basieren. Es sind keine normgerechten DIN-/VDE- oder Brandschutzprüfungen implementiert; die bisherigen Kabelkurven sind nur eine Visualisierung, **keine** berechnete Biegeradiusfreigabe.

## Entwicklung / Tests

Mit Node.js 22 oder neuer in `PC/Kabelsalat`:

```powershell
npm install
npm test
npm start
npm run dist:win
```

Die Offline-Browser-Version benötigt **keinen** `npm install`-Schritt. Der im Repo enthaltene Three.js-Build liegt unter `vendor/` einschließlich Lizenzhinweis.

## Architekturhinweis

Vor einer Netzimplementierung sind Schacht, Wand, Leerrohr, Kabel, Einhausung, einzelne Platten und Auflager mit **stabilen IDs** und eindeutigen Koordinatensystemen zu modellieren. Verwendet wird ein gemeinsamer Datenbestand für 2D und 3D. Die bestehende Datei- und Komponentendatenstruktur ist **noch nicht** das finale Netzmodell.

Weitere Anforderungen und Akzeptanzkriterien: [ROADMAP.md](./ROADMAP.md).

## Meilenstein 25 %

Die erste Projektetappe ist in [MILESTONE_25.md](./MILESTONE_25.md) beschrieben. Die **25 %** bezeichnen einen gewählten Meilenstein der Entwicklungsplanung, keine prüfbar gemessene Erfüllungsquote aller fachlichen Anforderungen. Ein manueller Abnahmetest auf Windows steht aus.

## Meilenstein 50 %

Die zweite Entwicklungsetappe ist in [MILESTONE_50.md](./MILESTONE_50.md) dokumentiert. **50 %** bezeichnet eine vereinbarte Projektetappe, keine empirisch gemessene Quote der vollständigen Konzeptabdeckung. Die Software ist weiterhin ein Prototyp ohne Freigabe für Bau- oder Elektroabnahmen.

## 75-%-Meilenstein

Siehe [MILESTONE_75.md](./MILESTONE_75.md). **75 %** bezeichnet die dritte Entwicklungsetappe, keine nachgewiesene 75-prozentige Erfüllung der Anforderungen. Das Programm ist nicht für elektrotechnische oder brandschutztechnische Freigaben geeignet.

## Level-100-Meilenstein (Beta 1.0)

Neu: **netzübergreifende Suche** nach Schacht, Kabel und Rohr mit direkter Navigation; konfigurierbare, ausdrücklich **nicht normative** Prüfwerte für Rohrabstände, Wandränder und eine reine Kontrollpunkt-Biegeradius-Näherung; automatische Datenhinweise für unvollständige Kabelanschlüsse und zu große Kabeldurchmesser; manuell dokumentierte Prüf-/Quellvermerke und lokale Änderungshistorie; **druckfähiger Projektbericht**, direkter Windows-PDF-Speicherdialog, HTML-Bericht und eigenständiger **interaktiver Offline-Viewer** (schreibgeschützt, 2D-Wände und Netzkabelwege). Zusätzlich können Kabel in der 2D-Wandprojektion ausgewählt werden.

Die neuen Objekt- und Vermerkdaten sind Teil der Projekt-JSON (Formatversion 5). Vollständige alte Einzel-Schachtprojekte werden weiterhin übernommen. Vor dem Upgrade unbedingt ein JSON-Backup aus der bisherigen Programmversion exportieren und nach einem Versionswechsel erst eine Kopie öffnen.

**Grenzen:** 100 % bedeutet Abschluss des vereinbarten vierstufigen Prototyp-Meilensteinplans, nicht die vollständige Erfüllung des AVL0116-Konzepts. Frei modellierbare Schächte, nachgewiesene physische Kabelkontinuität, normgerechte Biegeradien/Brandabschottung und CAD/BIM-/KI-Planerkennung fehlen. Die lokale Änderungshistorie ist **nicht manipulationssicher** und ersetzt keinen revisionssicheren Prüf-/Freigabeprozess. Fachanwender-Tests stehen aus.

Siehe [MILESTONE_100.md](./MILESTONE_100.md) für Testschritte, Risiken und offene Anforderungen.

## Beispielprojekte – beim Start sichtbar

Beim ersten Start ohne lokale Daten wird ein synthetisches Bahnhofsnetz mit vier Schächten, 376 Rohren und 64 Kabeln geladen. Eigene gespeicherte Projekte bleiben bei Updates unberührt. Links oben kannst du über Beispielprojekte jederzeit auch einen Einzelschacht mit 16 Kabeln oder einen absichtlich fehlerhaften Prüffall laden. Vor dem Wechsel bitte eigene Projekte als JSON sichern.

Die drei separat ladbaren JSON-Dateien liegen in examples/. Hinweise und Prüfschritte stehen in examples/README.md. Die verbindliche Bewertung der Anforderungen aus dem Original-Konzept steht in CONCEPT_AUDIT.md. **Diese Beta ist nicht zu 100 % konzeptkonform.**

## Basic-Modus – fokussierter Einstieg (Beta 3)

**Warum?** Der erste realistische Bedienungsscreenshot zeigte zu viele Fachbegriffe und Prüfmasken und ein zu blasses 3D-Modell. Ein Erstnutzer soll sofort erkennen, **was ein Schacht ist, wo Kabel verlaufen und wie die Schächte zusammenhängen**, bevor er Maße oder Prüfdaten bearbeiten muss.

**Neu:** Die Software startet standardmäßig im **Basic-Modus** (3 Auswahlmöglichkeiten: Netzkarte, Schacht verstehen, Kabel verfolgen). Rechts werden nur der aktuell erklärte Kabelweg und anklickbare Kabel gezeigt. Ein Glossar erklärt „Schacht“, „Schachtwand“, „Leerrohr“ und „DN“. Die Missionen haben einfache, dauerhaft gespeicherte Häkchen; niemand muss sie in einer bestimmten Reihenfolge absolvieren.

**Visuelle Klarheit:** Eine breitere 3D-Ansicht, dunklere erkennbare Schachtwände, kräftige, im Fokus hervorgehobene Kabel, klarere Rohröffnungen. Ein 2D-Wandplan lässt sich mit einem eigenen Button zuschalten. Komplexe Einhausungsplatten und Konsolen sind im Basic-Modus nur in der Anzeige ausgeblendet, **nicht aus dem Projekt gelöscht**.

**Profi-Modus:** Über die Schaltfläche oben rechts sind alle bisherigen vollständigen Editoren, Prüfvermerke, Einhausungsplatten, Netz- und Materialeinstellungen wieder sichtbar. Der gewählte Modus ist eine **lokale UI-Einstellung** und beeinflusst weder Projektdaten noch JSON-Export, Undo/Redo oder technische Prüfhinweise. Bestehende lokal gespeicherte Projekte bleiben erhalten.

Die Gestaltung orientiert sich am schnellen Entscheidungsansatz mit wenigen Auswahlmöglichkeiten; **eine medizinisch geprüfte ADHS-Wirksamkeit wird nicht behauptet**.

**Abnahme:** Siehe [Abnahmekriterien Basic-Modus](./EUGI_MODE_ACCEPTANCE.md). Der echte Windows-GUI-Test einschließlich Lesbarkeit/Interaktionsweg steht noch aus.

## 1.9.3 – Sichtbare Profi-Werkzeuge statt gesperrtem Basic

- **Konkreter UX-Fehler:** Bei neuen Installationen startet der **Basic-Modus als reine Ansicht** und blendet die meisten Bearbeitungsfelder aus. Mit eingeklappten Seitenleisten und geöffnetem PDF-Vergleich wirkt Kabelsalat dann wie ein nicht mehr bedienbarer Editor.
- Der neue, im Basic-Modus stets sichtbare Button **„✎ Schacht bearbeiten · Werkzeuge öffnen“** aktiviert Profi, öffnet **beide** Seitenleisten und kehrt aus PDF-, Netz- und Präsentationsansichten in die 3D-Hauptansicht zurück. Er verändert keine Planungsdaten.
- Auch der bisherige **„Profi-Modus“**-Button klappt die Werkzeugleisten jetzt wieder auf.
- Der **echte Chromium-Browsertest** simuliert beide eingeklappten Seitenleisten mit PDF-Overlay und überprüft dann eine echte **Projektnamenänderung über das Formular**, gefolgt von **Rückgängig**.
- Diese gezielte Bedienbarkeitskorrektur beweist nicht, dass die PDF-Kabelverläufe oder Powerpanel-Geometrien bautechnisch korrekt sind; auch ein gerätespezifischer Fehler muss separat nachgewiesen werden.

## 1.9.2 Beta 2 – PDF-Wandkoordinaten und Rückweg zur Hauptansicht

- Ein klar sichtbarer Button **„↩ Zum 3D-Schacht“** führt aus der Prüfübersicht, Netzansicht und PDF-Wandansicht zuverlässig zur Hauptansicht. Zusätzlich eigene Schacht-Zurück-Buttons in den jeweiligen Ansichten. **Esc** schließt die Netzansicht. Die Navigation verändert keine gespeicherten Projektdateien.
- Der 2D-Wandeditor und die geometrische Vorprüfung benutzen für **13_U_028 (PDF V2)** dieselbe Wandausrichtung wie die 3D-Geometrie: SW1/SW3 entlang der Schachtbreite, SW2/SW4 entlang der Schachttiefe (Referenz PDF S.4). Die bisherige Abweichung konnte Rohrabstände und die Darstellung verfälschen.
- Chromium überprüft zusätzlich das Zurücknavigieren aus Netz-, PDF- und Prüfansicht. Die bestehenden **97 PDF-Sichttests** prüfen 91 Rohre, vier Wandausrichtungen und zwei quellenbelegte MS-Verläufe. Das sind **nicht 97 erledigte Langzeit-Tickets**; offene Kabelenden und Brandschutz-Geometrie bleiben fachlich ungeklärt.

## Neu in 1.9.2: PDF-Wände und Kabel wirklich im 3D-Modell umgesetzt

- **Schachtgeometrie tatsächlich korrigiert:** SW1 ist die Stirnwand, SW2 rechts, SW3 gegenüber und SW4 links, wie in der PDF V2 auf Seite 4. Rohre, Kabelenden, Wandansichten, Spiegelungen und Kamera werden über dasselbe geprüfte Koordinatenmodul berechnet. Frühere HTML-Szenarien behalten aus Kompatibilitätsgründen ihre bisherige Orientierung.
- **Leerrohre als sichtbare 3D-Körper:** DN 63, DN 75, DN 110 und DN 160 werden nicht mehr nur als farbige Ringe beschrieben, sondern als **hohle, zylindrische Rohrmündungen** an den vier richtigen Schachtwänden dargestellt. DN bestimmt die kreisförmige Öffnungsgröße; die gezeigte kurze Muffenlänge ist ausdrücklich nur eine optische Illustration.
- **Kabel jetzt besser erkennbar:** alle 86 im PDF-Konzept ableitbaren Kabelobjekte werden als lebhaft gefärbte Kurven oder **erkennbar offene Einzelstücke** gezeichnet. Die Kabeldarstellung ist bewusst **optisch dicker als die nicht belegten realen Kabelaußendurchmesser**. Zwei MS-Kabel haben dokumentierte Rohranschlüsse an beiden Wänden, 66 Kabelobjekte haben weiterhin unbestätigte Enden. Keine erfundenen OLA-Langverbindungen.
- **Visueller PDF-Vergleich unabhängig von WebGL:** Der Button **„PDF-Wandbilder anzeigen“** öffnet vier echte 2D-Schachtdarstellungen mit **allen 91 Rohrkreisen**, LR-Nummern, Nenndurchmessern und farbigen Belegungsringen. Die Vorlagen werden direkt aus den PDF-Referenzdaten erzeugt und sind auch bei fehlgeschlagenem 3D-Start nach einer kurzen Startprüfung automatisch verfügbar.
- **Hinweis bei alten lokalen Projekten:** Bei einer älteren gespeicherten Schachtvorlage erscheint deutlich **„Du siehst noch ein älteres gespeichertes Schachtmodell“**. Über **„PDF V2 als 3D-Modell laden“** kann man gezielt wechseln. Die vorhandene Sicherheitsabfrage gegen unbeabsichtigtes Überschreiben bleibt bestehen; lokal gespeicherte Projekte werden nicht eigenmächtig ersetzt.
- **97 automatisierte PDF-Visual-Abnahmefälle** sichern **91 einzelne Leerrohre, vier Wandachsen und die zwei belegten MS-Verbindungen** ab. Zusätzlich prüfen wir 86 Kabelobjekte, die 106 belegten Rohranschluss-Vorkommen und 66 offene Verläufe. Diese 97 Prüfungen sind **nicht** gleichzusetzen mit 97 abgeschlossenen Tickets des 828-Aufgaben-Langzeitplans.
- **Fachliche Grenzen:** Positionen auf den Wandbildern beruhen auf PDF-Schemata, nicht auf vermessenen XYZ-Koordinaten. Die 3D-Visualisierung ist **keine geprüfte Planung**. MS-/OLA-Powerpanel-H2O-Einhausungen, realer Plattenaufbau, Auflager und Trennung von Fremdkabeln benötigen noch fachliche Maße und Freigaben.

## Neu in 1.9: Cache-Schutz und zuverlässiger Start für den Pitch

- Die Browser-URL bleibt unverändert. Die JavaScript-Dateien bekommen eine Versionskennung (`?v=1.9.0-beta.1`), damit alte Dateien aus dem Cache nicht mit neuer Oberfläche vermischt werden.
- Beim Laden erscheint kurz eine Statusanzeige. Bei einem Startfehler bleiben **Frisch neu laden** und **Ansicht zurücksetzen** erreichbar, auch wenn der 3D-Editor selbst nicht gestartet ist.
- **Ansicht zurücksetzen** entfernt nur die UI-Einstellungen für Seitenleisten, Basic-/Profi-Modus und besuchte Ansichtsschritte; die lokale Planung und gespeicherten JSON-Projekte bleiben erhalten. Der frische URL-Parameter fordert auch neues HTML an.
- Dieselbe Wiederherstellung wird in Windows-EXE, Browser-ZIP und der CSP-geprüften HTML-Einzeldatei mitgeliefert.
- Versionswechsel sind damit robuster; eine vollständige browserübergreifende GUI-Abnahme muss trotzdem der Fachanwender machen. Die PDF-Wanddaten und Brandschutzkonzept-Markierungen bleiben unverändert.

## Neu in 1.8: MS-/OLA-Kabeltrennung als nachvollziehbare 3D-Konzeptmarkierung

- In der PDF-V2-Vorlage `13_U_028` gibt es den Button **„MS/OLA im 3D markieren“**. Er blendet **blaue offene Gitterkonturen** entlang der bereits zugeordneten MS-Kabelwege und **rote Markierungsringe** um die bislang **nur einseitig** dokumentierten OLA-Rohranschlüsse ein.
- **Quellstand ehrlich sichtbar:** Im PDF-Modell sind **2/2 MS** mit zwei Rohranschlüssen zugeordnet, aber **0/4 OLA**. Für OLA werden bewusst **keine erfundenen Verbindungen** durch den Schacht gezogen. Auch die blauen Konturen sind **nur Orientierungslinien um schematische Kabelkurven**, keine geforderte Powerpanel-Kanalgeometrie oder Brandabschottung.
- Der Button funktioniert zusammen mit **Alle / nur MS / nur OLA**, der Kamera und Schacht-Durchblickansicht. **„▶ Präsentieren“** aktiviert die Konzeptmarkierung standardmäßig. Ein weiterer Klick blendet sie wieder aus.
- Die Statusanzeige beschreibt getrennt, welche Kabelendpunkte bekannt sind und welche fehlen. Der bisherige Hinweis auf **separate, allseitig geschlossene Powerpanel-H2O-Kanäle** bleibt bestehen, denn diese sind nach wie vor **nicht geometrisch nachweisbar und nicht im PDF-Schacht gebaut**.
- Keine Änderung an Kabel-IDs, Leerrohr-DN, Quellbelegungen, Schutzplatten oder gespeicherten Projektdateien. Die 3D-Konturen sind nur temporäre Visualisierung und **werden nicht als geprüfte Konstruktion exportiert**.
- Technische Abnahme ist weiter offen: Kanalmaße, Plattenaufbau, 75-mm-Höhenentscheidung und die Trennung von Fremdkabeln müssen fachlich geklärt werden.

## Neu in 1.7: Basic-Modus und professionelle Pitch-Ansicht

- Der sichtbare persönliche Editionsname wurde entfernt. Der UI-Wechsel heißt jetzt **Basic-Modus / Profi-Modus**. Der Basic-Modus schützt weiterhin vor versehentlicher Bearbeitung.
- Mit **„▶ Präsentieren“** startet die große 3D-Ansicht von **13_U_028 (PDF V2)**. Ist eine andere Planung geladen, wird die bereits vorhandene Rückfrage vor dem Laden der PDF-Referenz angezeigt. Die aktuelle Planung sollte vorher als JSON gesichert werden.
- In der Präsentationsansicht ist die Information zunächst **platzsparend eingeklappt**. Über **„▤ Steuerung“** wird sie wieder angezeigt. **„↩ Präsentation beenden“** bleibt immer erreichbar; **Esc** führt ebenfalls zurück.
- Der kompakte Kopf des Infofelds nennt die Rohr- und Kabelanzahl und die Anzahl ungeklärter Verläufe. Die Darstellung bleibt ausdrücklich als **Konzeptstand** gekennzeichnet.
- Die drei Einstiegsbereiche heißen jetzt **Übersicht, Schachtansicht und Kabelbelegung** statt spielerischer Missionsbezeichnungen.
- Die bislang verwendeten internen App-/LocalStorage-IDs bleiben absichtlich erhalten, damit bestehende Windows-Daten und lokal gespeicherte Projekte kompatibel bleiben. **1.7 ist keine fachliche Freigabe** der nur schematischen Kanalgeometrie.

## Neu in 1.6: Bessere PDF-Bildtreue und lesbare Kabelbelegung

- **91 Rohrmittelpunkte** der SW1–SW4 jetzt aus den **Vektorkreisen in den vier PDF-Diagrammen S. 21–24** relativ auf die Wandfläche übertragen. Schachtmaße und bauliche Lage bleiben Platzhalter, nicht vermessen.
- Mehrfarbige Rohr-**Belegungsringe** in 2D und 3D; bei ausreichender 2D-Breite zusätzlich DN-Beschriftungen.
- Kabel aus demselben Rohr werden **rein illustrativ auseinandergefächert**, damit 16 OLSP-Objekte nicht exakt übereinander liegen. Keine bestätigte Kabellage im Rohr.
- **SW1–SW4/3D-Schnellauswahl** zum PDF-Vergleich mit dokumentierten Wand-/Trassen-Bezeichnungen; einfacher Wechsel zur PDF-Referenz auch für Nutzer, deren Browser noch eine frühere Demo gespeichert hat. Beim Laden wird weiterhin vor Verlust des bisherigen Entwurfs gewarnt.
- **MS und OLA:** Separate geschlossene Kanäle sind als ausdrücklich nicht maßstäbliche **Konzeptskizze nach PDF S. 19** mit den 75-mm-Varianten A/B gezeigt. Eine konkrete baulich korrekte Powerpanel-Einhausung, Auflager oder 3D-Befestigungen lassen sich aus dieser PDF **nicht vollständig ableiten**; diese Punkte bleiben zur Prüfung offen.
- Keine Kabelanschlüsse erfunden; die bisherigen 86 Kabelobjekte, offenen Enden und Alt-HTML-Beispiele bleiben bestehen.

## Neu in 1.5: PDF V2 – Schacht 13_U_028

- Neue Vorlage **„★ 13_U_028 · PDF V2 Sollbild (91 LR)“** aus dem Fachkonzept vom 08.11.2026, Seiten 21–24. Vier Wandbilder mit zusammen 91 nummerierten Leerrohren, Nenn-DN und den dokumentierten Kabelbelegungen. Die alte HTML-Referenz bleibt separat erhalten.
- **86 Kabelobjekte** aus 106 Wand-Vorkommen: 20 per eindeutigem Namen zugeordnete Paare und 66 offene/ungenannte Verläufe. Bei offenen Verläufen wird **nur ein kurzes Kabelstück mit gelbem Endpunkt** gezeigt, statt eine fiktive Verbindung durch den Schacht zu erfinden.
- Korrigierte MS-SNG-0056 (SW1 LR27 / SW2 LR5) und MS-SNG-0038 (SW1 LR21 / SW3 LR26), OLSP 2060–2063 (SW2 LR9), OLSP 2960–2963 (SW2 LR10), fünf EEA in SW2 Trasse 3 LR1; LR2 bleibt leer.
- 2D zeigt LR-Nummern, Kabelbelegung sowie farbige Rohrringe; 3D hebt belegte Rohre hervor. Ein Klick zeigt die dokumentierten Einzelkabel.
- **Powerpanel H2O und Brandschutz**: MS/OLA müssen jeweils separat geschlossen eingehaust werden. Die PDF enthält **keine verbindlichen exakten Kanalmaße und Montagepositionen**. Deshalb sind generische Platten und Auflager für diese PDF-Vorlage *nicht als Ausführung eingeblendet*, sondern ausdrücklich offen.
- **Kein exakter Bauplan**: Die Wandbilder und Nenn-DN sind aus dem PDF transkribiert, aber Koordinaten, Schachtmaße, Abstände, Biegeradien und Befestigungen nicht bestätigt. SW2/Trasse 3 widerspricht zwischen S.13 und S.22; hier gilt vorläufig Detailseite 22. 75-mm-Grenze für Varianten A/B benötigt belastbare Höhenangaben.

## Neu in 1.4.1: Mehr Platz für den Schacht – Sofort-Fix nach Fachanwender-Feedback

- **Beide Optionsfenster unabhängig einklappen:** Ein schmaler Pfeil **❮** am linken und **❯** am rechten Rand der 3D-Ansicht blendet die jeweilige Seitenleiste aus. Der Pfeil bleibt sichtbar und öffnet die Leiste auf erneuten Klick wieder. Auch beide gleichzeitig einklappen ist möglich.
- Der Schacht bekommt sofort die frei gewordene Fläche. **3D-Kamera und 2D-Darstellung passen sich der geänderten Fenstergröße an**, ohne Maße oder Projektobjekte zu verändern.
- Die Auswahl wird **ausschließlich als lokale UI-Präferenz** gespeichert, getrennt von Projekt-JSON, Audit und Undo/Redo. Funktioniert im Eugi- und Profi-Modus.
- Der bisherige **Fokusmodus** bleibt separat: In dieser Präsentationsansicht kann weiterhin über „Seitenleisten wieder anzeigen“ im überlagerten Steuerfeld zurückgekehrt werden.
- **Noch offen für 1.5 (höchste Priorität):** Fachgerechter Abgleich von Schachtwandanordnung, Leerrohrpositionen, bestätigten DN-/Durchmessern sowie sämtlichen Kabelkennungen, Belegungen und Verläufen mit den bereitgestellten Planunterlagen. Aktuelle Referenz-HTMLs enthalten schematische Maße und offene Kabelenden. **Keine Werte oder Verbindungen erfinden.**

## Neu in 1.4: Demo-Sprint – Schacht-Durchblick und sichtbare Datenlücken

- Im **Basic-Modus** lässt sich unter **„Schacht öffnen“** eine einzelne Wand SW1–SW4 nur für die 3D-Ansicht ausblenden. Die Kamera springt auf diese Schachtseite, um Rohre, Kabelverläufe und vorhandene Platten im Inneren zu zeigen.
- **„Alle vorhandenen Wände zeigen“** stellt die normale Darstellung wieder her. Keine Wände oder Öffnungen werden gelöscht oder verschoben; die Projektdatei, Varianten, Prüfvermerke und Undo-Historie bleiben davon unberührt.
- Der Präsentationsbereich nennt die **Zahl der Kabel mit zwei zugeordneten sichtbaren Rohranschlüssen**. Nur solche Kabel zeichnet die aktuelle 3D-Ansicht als durchgehende Kurven. Nicht vollständig zugeordnete Kabel werden nicht mit erfundenen Wegen dargestellt.
- Beispielsweise hat die HTML-Referenz **13_U_028** weiterhin 18 Kabel, davon **7 ohne vollständige Anschlusszuordnung**. Die Durchblickfunktion ändert daran nichts. Maßstab, Kabelverläufe, Auflager und Brandschutz bleiben schematisch beziehungsweise ungeprüft.
- Die neue Anzeige funktioniert auch zusammen mit MS-/OLA-Filter, Plattentransparenz und Fokusmodus aus 1.2/1.3. Gezielte optische Schachtöffnungen sind **keine baulichen Aussparungen**.

## Neu in 1.3: Demo-Sprint – Fokusansicht und Gewerke-Filter

- Im **Basic-Modus** lassen sich die gezeigten Kabel und bereits vorhandenen Platten nach **Alle / Nur MS / Nur OLA** filtern. Filter ändern keine Kabelzuordnung, Einhausung oder andere Projektdaten. Nicht eindeutig gewerkbezogene Auflager werden in Einzelfiltern absichtlich ausgeblendet.
- Drei **Kamerapositionen** (Schräg / Nah / Oben) führen mit einem Klick zur passenden Ansicht. Danach kann wie bisher frei gedreht und gezoomt werden.
- **Fokusansicht ohne Seitenleisten** maximiert den Schacht für die Präsentation; derselbe Button bringt die Bedienleisten zurück.
- Kein fachlicher Geometrie-Nachweis: 13_U_028 ist noch ein schematisches Referenzmodell; fehlende oder im Projekt ausgeblendete Brandschutzbauteile werden nicht erfunden. Für eine Vorführung mit vorhandenen Platten und Auflagern eignet sich der **ausdrücklich fiktive** Beispielschacht 13_T_021 (DEMO).
- 1.3 ist ein reines Darstellungs-/Bedienupdate, kein geprüfter Plan- oder Brandschutzstand. Die Projekt-JSON-Struktur bleibt unverändert.

## Neu in 1.2: Demo-Sprint – Einhausungen direkt vorführen

- Im **Basic-Modus** über dem 3D-Schacht auf **„Platten transparent zeigen“** klicken. Die Einhausung wird als vorhandene **Einzelplatten** durchsichtig dargestellt; vorhandene Auflager werden eingeblendet. Ein weiterer Klick macht die Platten deckend, ein dritter blendet sie wieder aus.
- Für die Einhausungsvorführung wird die Schachtwand optisch durchlässiger. Die Anzeigeumschaltung ist **rein visuell**: Sie ändert weder Plattenmaße, Kabelzuordnungen noch gespeicherte Projektdateien, Varianten oder das Änderungsprotokoll.
- Im Basic-Modus können Kabel und Rohre angeklickt, aber **nicht versehentlich verschoben oder neu angelegt** werden. Bauteile bearbeiten geht weiterhin im Profi-Modus.
- Nur im Projekt vorhandene und sichtbare Konstruktionen werden gezeigt. Insbesondere bei **13_U_028** ist die MS-Einhausung in den bisherigen Quelldaten ausgeblendet und es sind **keine Auflager** vorhanden. Die Anzeige benennt das ausdrücklich, statt eine verifizierte Konstruktion vorzutäuschen.
- Die 3D-Kanäle sind schematisch und weder brandtechnisch noch statisch geprüft. Die physische Vollständigkeit/Geometrie muss fachlich abgestimmt werden.

## Neu in 1.1: HTML-Referenzdaten statt nur erfundene Demo-Geometrie

Die zwei beigefügten, eigenständigen **Original-HTML-Editoren** vom 05.11.2026 wurden mit ihren eingebetteten strukturierten Daten ausgewertet, ohne deren sehr große Bilder/Mesh-Base64-Daten in die Anwendung zu kopieren. Im Basic-Modus stehen nun **sechs** Beispiele bereit:

- 13_T_021 aus HTML: 48 physisch dargestellte Öffnungen, 16 Kabel, Originalöffnungs-IDs und Quellen-/Seitenangaben.
- 13_U_028 aus HTML: 78 physisch dargestellte Öffnungen, 18 Kabel, dazu 15 **nur im Plan bezeichnete Marker**, die ausdrücklich nicht als reale Rohröffnungen angelegt werden. Sieben Kabelenden benötigen weiterhin eine bestätigte Zuordnung.
- Vergleich beider Schachtmodelle: 2 Schächte, 126 dargestellte Öffnungen, 34 Kabel, **ohne** erfundene unterirdische Verbindung. Die Kartenkoordinaten dienen ausschließlich der Ansicht.

Die ursprüngliche Schachtgeometrie ist **schematisch**, der zugehörige DN nicht bekannt. Im Editor dargestellte Rohrdurchmesser sind aus den grafischen Radien abgeleitet, Kabeldurchmesser sind Darstellungswerte. Das originale Quellsystem (LST, LWL, TK, OLA, MS usw.) bleibt als Provenienz erhalten, obwohl die vereinfachte interne Farbkategorie nur OLA/MS/Weitere kennt. Bei offener Plananschluss-Zuordnung bleibt das physische Kabelende im Modell absichtlich unbelegt. Die Originalanschlusskennung bleibt gespeichert. **Keine bautechnische Freigabe.**

Die drei als JSON herunterladbaren Datensätze liegen in `examples/04_...`, `05_...` und `06_...`; Details: [SOURCE_DATA_AUDIT_1_1.md](./SOURCE_DATA_AUDIT_1_1.md).

## Ohne Windows-Sicherheitsmeldung: Browser statt EXE

Das ZIP im GitHub-Release enthält sämtliche lokalen Dateien; es muss nur entpackt und `index.html` in Chrome oder Edge geöffnet werden. **Keine EXE-Installation und kein Backend.** Projekte liegen im jeweiligen Browser-Speicher oder als exportierbare JSON-Dateien vor.

Eine zusätzliche öffentlich erreichbare GitHub-Pages-Webseite ist als Workflow vorbereitet. Damit `https://valoon4.github.io/Androidbaze/` tatsächlich funktioniert, muss der Repository-Eigentümer einmal unter **Settings → Pages → Build and deployment → Source → GitHub Actions** aktivieren (wenn Pages noch nicht eingerichtet ist). Erst nach erfolgreicher Veröffentlichung ist der Weblink nutzbar. Die GitHub-Pages-Variante ist clientseitig, aber die statischen Dateien werden beim ersten Aufruf über GitHub geladen; keine vertraulichen Planungsdaten in öffentlich eingecheckte Demo-JSON-Dateien übernehmen.

## Einzeldatei fürs direkte Öffnen in Chrome/Edge (ohne ZIP und Installation)

Die Release-Datei `Kabelsalat-1.9.3-beta.1-Browser-Einzeldatei.html` enthält alle JavaScript-Bibliotheken und Beispiele in einer einzigen HTML-Datei. Datei herunterladen und **doppelklicken**: Sie läuft lokal im Browser ohne irgendeinen Installer. Die HTML-Datei kann per E-Mail/Chat an den Fachanwender weitergegeben werden. Der Code verwendet **SHA-256-basierte CSP-Script-Hashes**, damit keine unbegrenzte Inline-Script-Freigabe nötig ist. Reine Browser-Downloads und lokale Projekt-JSON-Dateien bleiben möglich.

**Hinweis:** Ob `localStorage` für `file://` funktioniert, hängt von Browser- und IT-Sicherheitsrichtlinien ab. Eigene Projekte deshalb immer mit „Projekt speichern“ als JSON exportieren, um Datenverlust auszuschließen. Eine Firmenrichtlinie kann auch lokale HTML-Dateien blockieren; dann IT-Abstimmung oder eine freigegebene GitHub-Pages-/Intranet-Webseite verwenden. **Keine Sicherheitsbarrieren umgehen.**
