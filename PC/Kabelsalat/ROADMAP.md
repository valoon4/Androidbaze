# Kabelsalat – Product Backlog & Akzeptanzkriterien

Dieser Backlog ist ein **Implementierungsplan** für die nächste Iteration. Der derzeitige Prototyp erfüllt nicht den vollständigen fachlichen Umfang. Fachliche Maße, Prüfschwellen und externe Normen dürfen nicht geraten werden.

## Priorität P0 – Editor als tragfähige Grundlage

| ID | Funktion | Vorhanden | Nächster Schritt |
|---|---|---|---|
| P0-01 | Maßhaltige, **gleichzeitige** 2D-/3D-Ansicht eines gemeinsamen Modells | **v0.4:** parallele Zeichenfläche; gemeinsame Rohrauswahl und 1-mm-Positionen, Live-3D-Update | Praxistest; weitere 2D-Editierfunktionen für Kabel und Einhausungen |
| P0-02 | Frei anlegbare Wände mit IDs und Geometrie | Vier feste Wände | N beliebig positionierbare Wände |
| P0-03 | Rohre mit DN, Durchmesser, mm-Abständen, Kopieren und Verschieben | **v0.4:** Rohrduplikat mit neuer ID, 1-mm-Mausraster, geometrische Mindest-Rand- und Paarabstände | Projektbezogene Grenzwerte und fachliche Freigaben |
| P0-04 | Gerade Standardverbindungen; manuelle Kabelführung unter Mindestbiegeradius | Kurvenpunkte nur visuell | Typisierte Kabel, verbindliche Biegeradienparameter und prüfbare Geometrie |
| P0-05 | OLA/MS mit einzeln editierbaren Platten, Material, Stärke, Einzeltransparenz | **v0.5:** sechs eigene Platten je Baugruppe, Material/Maße/Dicke/Versatz/Deckkraft, 3D-Auswahl, AABB-Näherungswarnung | Fachlich valide Kollisionen, direkte Plattenbewegung und Brandschutzprüfung |
| P0-06 | Konsolen und Auflager in 2D/3D | **v0.5:** Bauteil-IDs, Wandbezug, X/Y/Z-Maße und Darstellung in beiden Ansichten | Konstruktive Details, Befestigung und Mindestabstände prüfen |
| P0-07 | Persistenz, Varianten, Wiederöffnung, PDF/Bild/Viewer | **v0.5:** Projektformat v3, benannte Varianten-Snapshots, einfache Detail- und Mengenvergleiche, PNG | PDF, interaktiver Viewer, visuelle geometrische Variantendifferenz |
| P0-08 | Nachweisbare stabile IDs und Koordinatenbasis | Lokale IDs, globale Schachtlage fehlt | Schacht-IDs, lokale vs. globale Transformationsmatrix |

### Prüfkriterien der Editorabnahme

1. Bearbeite einen Leerrohrmittelpunkt in 2D numerisch (in mm), kontrolliere dieselbe Position sofort in 3D; auch inverse 3D-Interaktion muss in 2D erscheinen.
2. Ändere eine Einhausung: Boden, Seiten, Deckel und Stirnplatten sind **einzeln** auswählbar, verschiebbar und prüfbar; Material und Dicke persistieren. Konsolen/Auflager stehen als eigenständige Objekte zur Verfügung.
3. Das aktuell ausgewählte Kabel ist in 2D und 3D **dieselbe** Datenobjektinstanz und hat identische Folge von Kontrollpunkten und Verbindungen.
4. Speichern und wieder laden darf IDs, mm-Geometrie, Verbindungen, Rohrbelegung oder Platten-/Auflagerzuordnung nicht verändern.

## Priorität P1 – Netzansicht und durchgängige Verfolgung

- **v0.6:** Zoom-/Pan-Vogelperspektive für bis zu 30 Schächte mit globalen X/Z-Koordinaten, Links und visueller Auswahl; schematisch.
- **v0.6:** Netz → Schacht und Netz-Kabelsegment → lokales Kabel. Offen: volle netzweite Leerrohrnavigation.
- **v0.6:** geordnete Kabelsegmente nach Schacht- und Kabel-ID, Warnung bei fehlenden Übergangslinks. Offen: tatsächliche Leitungskontinuität, Abzweige und reale Übergänge.
- **v0.6:** lokale Kabel- und Systemfilter, „Gesamtverlauf anzeigen“. Offen: projektweite Suche und Statusfilter.

**Test:** Ein in der Gesamtansicht selektiertes Kabel bleibt im Editor selektiert; alle Schachtübergänge werden in richtiger Reihenfolge angezeigt.

## Priorität P2 – Datenqualität, Import und fachliche Prüfungen

- Plan-/Bestandlisten und Vermessungsdaten mit Quelle, Datum, Koordinatenbezug und Prüffreigabe importieren.
- Dokument-/Bilderkennung liefert lediglich überprüfbare Vorschläge; keine stillschweigende Zuordnung unklarer Anschlüsse.
- Vorgeschriebene Projektabstände, Geometriekollisionen, Kabelbiegeradien und Einhausungsfreiräume nachvollziehbar prüfen.
- Schematische Eingaben von bestätigten Vermessungen trennen; Änderungshistorie und Abnahmeprotokolle speichern.

**Keine Freigabeautomatik:** Ob und nach welchen Vorgaben die Planung genehmigungsfähig ist, muss fachlich geklärt werden.

## 75-%-Entwicklungsstufe

Details: [MILESTONE_75.md](./MILESTONE_75.md). Keine Bau-/Brandschutz-/Ausführungsfreigabe.

## Level-100-Beta v1.0.0-beta.1

- **Neu:** globales Schacht-/Kabel-/Rohr-Suchfeld mit Navigation in den Editor.
- **Neu:** numerische Datenprüfungen mit benutzerdefinierten Mindestwerten und mehreren klar bezeichneten Geometriehinweisen; keine Normprüfung.
- **Neu:** manuelle Quellen-/Statusvermerke pro Schacht oder Kabel, lokale nicht revisionssichere Änderungshistorie.
- **Neu:** druckfähiges Berichtsformat mit nativem PDF-Speichern unter Windows sowie read-only Offline-HTML-Viewer.
- **Weiter offen:** sämtliche fachlich validierten Norm- und Ausführungsnachweise, frei definierte Schachtgeometrie, echte physische Netz-Kabelübergänge, CAD/BIM-/KI-Erkennung, frei verschiebbare Platten in 2D, Mehrbenutzer- und Freigabeworkflow.

**Der Meilenstein ist keine 100-prozentige Anforderungsabdeckung.** [MILESTONE_100.md](./MILESTONE_100.md).
