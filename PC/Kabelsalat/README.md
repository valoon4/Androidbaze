# Kabelsalat – Schacht- und Kabeleditor

**Status: Prototyp v0.6.0** (Umbenennung des bisherigen Schachtwand Planer 3D). Eigenständig als **Offline-Browseranwendung** und als **portable Windows-EXE** nutzbar. Verwendet ein und dasselbe Daten- und Darstellungsmodell.

## Herunterladen und starten

Im GitHub-Release [kabelsalat-latest](https://github.com/valoon4/Androidbaze/releases/tag/kabelsalat-latest) gibt es:

- `Kabelsalat-0.6.0-Browser-Offline.zip`: herunterladen, **vollständig entpacken**, `index.html` in einem aktuellen Chrome/Edge öffnen. Kein Node.js, keine Installation und keine Serververbindung nötig. Der Ordner `vendor/` muss neben `index.html` bleiben.
- `Kabelsalat-0.6.0-Windows.exe`: portable Windows 10/11-Anwendung mit Electron. Doppelklick; keine dauerhafte Installation erforderlich.

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
