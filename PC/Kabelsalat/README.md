# Kabelsalat – Schacht- und Kabeleditor

**Status: Demo-Beta v1.2.0-beta.1 – Konzept teilweise erfüllt, nicht fachlich abgenommen** (Umbenennung des bisherigen Schachtwand Planer 3D). Eigenständig als **Offline-Browseranwendung** und als **portable Windows-EXE** nutzbar. Verwendet ein und dasselbe Daten- und Darstellungsmodell.

## Herunterladen und starten

Im GitHub-Release [kabelsalat-latest](https://github.com/valoon4/Androidbaze/releases/tag/kabelsalat-latest) gibt es:

- `Kabelsalat-1.2.0-beta.1-Browser-Offline.zip`: herunterladen, **vollständig entpacken**, `index.html` in einem aktuellen Chrome/Edge öffnen. Kein Node.js, keine Installation und keine Serververbindung nötig. Der Ordner `vendor/` muss neben `index.html` bleiben.
- `Kabelsalat-1.2.0-beta.1-Windows.exe`: portable Windows 10/11-Anwendung mit Electron. Doppelklick; keine dauerhafte Installation erforderlich.

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

## Eugi-Modus – fokussierter Einstieg (Beta 3)

**Warum?** Der erste realistische Bedienungsscreenshot zeigte zu viele Fachbegriffe und Prüfmasken und ein zu blasses 3D-Modell. Ein Erstnutzer soll sofort erkennen, **was ein Schacht ist, wo Kabel verlaufen und wie die Schächte zusammenhängen**, bevor er Maße oder Prüfdaten bearbeiten muss.

**Neu:** Die Software startet standardmäßig im **Eugi-Modus** (3 Auswahlmöglichkeiten: Netzkarte, Schacht verstehen, Kabel verfolgen). Rechts werden nur der aktuell erklärte Kabelweg und anklickbare Kabel gezeigt. Ein Glossar erklärt „Schacht“, „Schachtwand“, „Leerrohr“ und „DN“. Die Missionen haben einfache, dauerhaft gespeicherte Häkchen; niemand muss sie in einer bestimmten Reihenfolge absolvieren.

**Visuelle Klarheit:** Eine breitere 3D-Ansicht, dunklere erkennbare Schachtwände, kräftige, im Fokus hervorgehobene Kabel, klarere Rohröffnungen. Ein 2D-Wandplan lässt sich mit einem eigenen Button zuschalten. Komplexe Einhausungsplatten und Konsolen sind im Eugi-Modus nur in der Anzeige ausgeblendet, **nicht aus dem Projekt gelöscht**.

**Profi-Modus:** Über die Schaltfläche oben rechts sind alle bisherigen vollständigen Editoren, Prüfvermerke, Einhausungsplatten, Netz- und Materialeinstellungen wieder sichtbar. Der gewählte Modus ist eine **lokale UI-Einstellung** und beeinflusst weder Projektdaten noch JSON-Export, Undo/Redo oder technische Prüfhinweise. Bestehende lokal gespeicherte Projekte bleiben erhalten.

Die Gestaltung orientiert sich am schnellen Entscheidungsansatz mit wenigen Auswahlmöglichkeiten; **eine medizinisch geprüfte ADHS-Wirksamkeit wird nicht behauptet**.

**Abnahme:** Siehe [EUGI_MODE_ACCEPTANCE.md](./EUGI_MODE_ACCEPTANCE.md). Der echte Windows-GUI-Test einschließlich Lesbarkeit/Interaktionsweg steht noch aus.

## Neu in 1.2: Demo-Sprint – Einhausungen direkt vorführen

- Im **Eugi-Modus** über dem 3D-Schacht auf **„Platten transparent zeigen“** klicken. Die Einhausung wird als vorhandene **Einzelplatten** durchsichtig dargestellt; vorhandene Auflager werden eingeblendet. Ein weiterer Klick macht die Platten deckend, ein dritter blendet sie wieder aus.
- Für die Einhausungsvorführung wird die Schachtwand optisch durchlässiger. Die Anzeigeumschaltung ist **rein visuell**: Sie ändert weder Plattenmaße, Kabelzuordnungen noch gespeicherte Projektdateien, Varianten oder das Änderungsprotokoll.
- Im Eugi-Modus können Kabel und Rohre angeklickt, aber **nicht versehentlich verschoben oder neu angelegt** werden. Bauteile bearbeiten geht weiterhin im Profi-Modus.
- Nur im Projekt vorhandene und sichtbare Konstruktionen werden gezeigt. Insbesondere bei **13_U_028** ist die MS-Einhausung in den bisherigen Quelldaten ausgeblendet und es sind **keine Auflager** vorhanden. Die Anzeige benennt das ausdrücklich, statt eine verifizierte Konstruktion vorzutäuschen.
- Die 3D-Kanäle sind schematisch und weder brandtechnisch noch statisch geprüft. Die physische Vollständigkeit/Geometrie muss fachlich abgestimmt werden.

## Neu in 1.1: HTML-Referenzdaten statt nur erfundene Demo-Geometrie

Die zwei beigefügten, eigenständigen **Original-HTML-Editoren** vom 05.11.2026 wurden mit ihren eingebetteten strukturierten Daten ausgewertet, ohne deren sehr große Bilder/Mesh-Base64-Daten in die Anwendung zu kopieren. Im Eugi-Modus stehen nun **sechs** Beispiele bereit:

- 13_T_021 aus HTML: 48 physisch dargestellte Öffnungen, 16 Kabel, Originalöffnungs-IDs und Quellen-/Seitenangaben.
- 13_U_028 aus HTML: 78 physisch dargestellte Öffnungen, 18 Kabel, dazu 15 **nur im Plan bezeichnete Marker**, die ausdrücklich nicht als reale Rohröffnungen angelegt werden. Sieben Kabelenden benötigen weiterhin eine bestätigte Zuordnung.
- Vergleich beider Schachtmodelle: 2 Schächte, 126 dargestellte Öffnungen, 34 Kabel, **ohne** erfundene unterirdische Verbindung. Die Kartenkoordinaten dienen ausschließlich der Ansicht.

Die ursprüngliche Schachtgeometrie ist **schematisch**, der zugehörige DN nicht bekannt. Im Editor dargestellte Rohrdurchmesser sind aus den grafischen Radien abgeleitet, Kabeldurchmesser sind Darstellungswerte. Das originale Quellsystem (LST, LWL, TK, OLA, MS usw.) bleibt als Provenienz erhalten, obwohl die vereinfachte interne Farbkategorie nur OLA/MS/Weitere kennt. Bei offener Plananschluss-Zuordnung bleibt das physische Kabelende im Modell absichtlich unbelegt. Die Originalanschlusskennung bleibt gespeichert. **Keine bautechnische Freigabe.**

Die drei als JSON herunterladbaren Datensätze liegen in `examples/04_...`, `05_...` und `06_...`; Details: [SOURCE_DATA_AUDIT_1_1.md](./SOURCE_DATA_AUDIT_1_1.md).

## Ohne Windows-Sicherheitsmeldung: Browser statt EXE

Das ZIP im GitHub-Release enthält sämtliche lokalen Dateien; es muss nur entpackt und `index.html` in Chrome oder Edge geöffnet werden. **Keine EXE-Installation und kein Backend.** Projekte liegen im jeweiligen Browser-Speicher oder als exportierbare JSON-Dateien vor.

Eine zusätzliche öffentlich erreichbare GitHub-Pages-Webseite ist als Workflow vorbereitet. Damit `https://valoon4.github.io/Androidbaze/` tatsächlich funktioniert, muss der Repository-Eigentümer einmal unter **Settings → Pages → Build and deployment → Source → GitHub Actions** aktivieren (wenn Pages noch nicht eingerichtet ist). Erst nach erfolgreicher Veröffentlichung ist der Weblink nutzbar. Die GitHub-Pages-Variante ist clientseitig, aber die statischen Dateien werden beim ersten Aufruf über GitHub geladen; keine vertraulichen Planungsdaten in öffentlich eingecheckte Demo-JSON-Dateien übernehmen.

## Einzeldatei fürs direkte Öffnen in Chrome/Edge (ohne ZIP und Installation)

Die Release-Datei `Kabelsalat-1.2.0-beta.1-Browser-Einzeldatei.html` enthält alle JavaScript-Bibliotheken und Beispiele in einer einzigen HTML-Datei. Datei herunterladen und **doppelklicken**: Sie läuft lokal im Browser ohne irgendeinen Installer. Die HTML-Datei kann per E-Mail/Chat an den Fachanwender weitergegeben werden. Der Code verwendet **SHA-256-basierte CSP-Script-Hashes**, damit keine unbegrenzte Inline-Script-Freigabe nötig ist. Reine Browser-Downloads und lokale Projekt-JSON-Dateien bleiben möglich.

**Hinweis:** Ob `localStorage` für `file://` funktioniert, hängt von Browser- und IT-Sicherheitsrichtlinien ab. Eigene Projekte deshalb immer mit „Projekt speichern“ als JSON exportieren, um Datenverlust auszuschließen. Eine Firmenrichtlinie kann auch lokale HTML-Dateien blockieren; dann IT-Abstimmung oder eine freigegebene GitHub-Pages-/Intranet-Webseite verwenden. **Keine Sicherheitsbarrieren umgehen.**
