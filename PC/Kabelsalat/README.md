# Kabelsalat – Schacht- und Kabeleditor

**Status: Prototyp v0.4.0** (Umbenennung des bisherigen Schachtwand Planer 3D). Eigenständig als **Offline-Browseranwendung** und als **portable Windows-EXE** nutzbar. Verwendet ein und dasselbe Daten- und Darstellungsmodell.

## Herunterladen und starten

Im GitHub-Release [kabelsalat-latest](https://github.com/valoon4/Androidbaze/releases/tag/kabelsalat-latest) gibt es:

- `Kabelsalat-0.4.0-Browser-Offline.zip`: herunterladen, **vollständig entpacken**, `index.html` in einem aktuellen Chrome/Edge öffnen. Kein Node.js, keine Installation und keine Serververbindung nötig. Der Ordner `vendor/` muss neben `index.html` bleiben.
- `Kabelsalat-0.4.0-Windows.exe`: portable Windows 10/11-Anwendung mit Electron. Doppelklick; keine dauerhafte Installation erforderlich.

*Hinweis zur Datenübernahme:* Vor dem Wechsel vom alten Schachtwand Planer die Projekte über **Projekt speichern** als JSON exportieren und anschließend in Kabelsalat über **Öffnen** laden. Das lokale Autosave kann durch die neue App-Identität anders gespeichert werden.

## Vorhandene Funktionen

- Vier Wände (SW1–SW4) mit eigenen Wandstärken und Leerrohrdurchmessern; Raster, freie Platzierung, Rohrkopie mit neuer stabiler ID und millimetergenaue numerische Eingaben.
- **Gleichzeitige 2D- und 3D-Ansicht:** rechts eine millimetergenau skalierten Wandzeichnung, links das live aktualisierte 3D-Modell. Rohrauswahl, Positionen und Duplizieren verwenden dieselben Objekte und IDs. Die orthografischen Wandansichten sind zusätzlich verfügbar.
- Farbige Kabel mit Start-/Zielrohr, Durchmesser, Zuordnung zu OLA/MS/Weitere und frei editierbaren Kurvenpunkten.
- Schematische getrennte OLA- und MS-Einhausungen; Sichtbarkeit und globale Deckkraft.
- Einfache geometrische Warnungen für sich überlagernde Leerrohre und Überschreitungen von Wandrändern. Die 2D-Zeichnung ermittelt zusätzlich den kleinsten geometrischen Rohr-/Randabstand in mm, **ohne** normative Freigabe.
- Lokal speichern/laden als Projekt-JSON, Bildexport als PNG sowie Rückgängig/Wiederholen.

## Konzeptabgleich (verbindliche Produktanforderungen, noch nicht erfüllt)

Das bereitgestellte Konzept beschreibt **zuerst den vollständigen maßhaltigen 2D-/3D-Editor** und **danach die vernetzte Vogelperspektive mit Schacht- und Kabelverfolgung**. Die bereits bestehende Version deckt nur einen Teil davon ab:

- **Offen – Editor:** frei anlegbare Wände statt festem 4-Wand-Schema, fachlich erforderliche Mindestabstände, 2D-Direktbearbeitung von Kabeln und Platten, gerader Standardverlauf gegenüberliegender Anschlüsse sowie kontrollierte Mindestbiegeradien.
- **Offen – Einhausungen:** sechs einzeln bearbeitbare Platten je Einhausung, Material und Dicke, getrennte Transparenz und Kollisionsprüfung mit Fremdrohren.
- **Offen – weitere Bauteile:** Konsolen/Auflager und deren 2D-/3D-Positionen.
- **Offen – Projektworkflow:** benannte Varianten, Vergleich, PDF-Export und interaktiver Viewer.
- **Offen – Netz:** mehrere Schächte mit globalen Koordinaten, Verbindungen, stabile netzweite Objekt-IDs, Filter und vollständige Kabelverfolgung.
- **Offen – Import/Prüfung:** Plan- und Bestandsimport, bestätigungspflichtige Erkennung, Quellen/Prüfstatus/Historie, numerisch prüfbare Abstände und Kollisionswarnungen.

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
