# Kabelsalat – Product Backlog & Akzeptanzkriterien

Dieser Backlog ist ein **Implementierungsplan** für die nächste Iteration. Der derzeitige Prototyp erfüllt nicht den vollständigen fachlichen Umfang. Fachliche Maße, Prüfschwellen und externe Normen dürfen nicht geraten werden.

## Priorität P0 – Editor als tragfähige Grundlage

| ID | Funktion | Vorhanden | Nächster Schritt |
|---|---|---|---|
| P0-01 | Maßhaltige, **gleichzeitige** 2D-/3D-Ansicht eines gemeinsamen Modells | Umschaltbare Ansichten | Teilfenster nebeneinander, gemeinsame Selektion und Live-Updates |
| P0-02 | Frei anlegbare Wände mit IDs und Geometrie | Vier feste Wände | N beliebig positionierbare Wände |
| P0-03 | Rohre mit DN, Durchmesser, mm-Abständen, Kopieren und Verschieben | Teilweise | ID-sichere Kopierfunktion, Rand- und Paarabstände, Fangraster |
| P0-04 | Gerade Standardverbindungen; manuelle Kabelführung unter Mindestbiegeradius | Kurvenpunkte nur visuell | Typisierte Kabel, verbindliche Biegeradienparameter und prüfbare Geometrie |
| P0-05 | Trennung OLA/MS: alle Platten einzeln editierbar, Material, Stärke, Einzeltransparenz | Nur schematische sechs Flächen | Datenmodell für Platten, Kollisionsprüfung mit Fremdrohren |
| P0-06 | Konsolen und Auflager in 2D/3D | Fehlt | Komponenten, Maß-/Positionseditor, Zuordnung |
| P0-07 | Persistenz, Varianten, Wiederöffnung, PDF/Bild/Viewer | JSON & PNG | Versionssicherer Import, Varianten, Vergleich, PDF/Viewer |
| P0-08 | Nachweisbare stabile IDs und Koordinatenbasis | Lokale IDs, globale Schachtlage fehlt | Schacht-IDs, lokale vs. globale Transformationsmatrix |

### Prüfkriterien der Editorabnahme

1. Bearbeite einen Leerrohrmittelpunkt in 2D numerisch (in mm), kontrolliere dieselbe Position sofort in 3D; auch inverse 3D-Interaktion muss in 2D erscheinen.
2. Ändere eine Einhausung: Boden, Seiten, Deckel und Stirnplatten sind **einzeln** auswählbar, verschiebbar und prüfbar; Material und Dicke persistieren. Konsolen/Auflager stehen als eigenständige Objekte zur Verfügung.
3. Das aktuell ausgewählte Kabel ist in 2D und 3D **dieselbe** Datenobjektinstanz und hat identische Folge von Kontrollpunkten und Verbindungen.
4. Speichern und wieder laden darf IDs, mm-Geometrie, Verbindungen, Rohrbelegung oder Platten-/Auflagerzuordnung nicht verändern.

## Priorität P1 – Netzansicht und durchgängige Verfolgung

- Zoom-/Pan-fähige Vogelperspektive für zahlreiche Schächte mit globalen Koordinaten und Verbindungen.
- Navigation **Netz → Schacht → Leerrohr → Kabel** mit gemeinsamem Selektionszustand.
- Vollständige Leitungsverfolgung über Schachtgrenzen, belegte Leerrohre, Start/Ziel und Abzweige.
- Suche/Filter nach IDs, System und Status; „Gesamtverlauf anzeigen“.

**Test:** Ein in der Gesamtansicht selektiertes Kabel bleibt im Editor selektiert; alle Schachtübergänge werden in richtiger Reihenfolge angezeigt.

## Priorität P2 – Datenqualität, Import und fachliche Prüfungen

- Plan-/Bestandlisten und Vermessungsdaten mit Quelle, Datum, Koordinatenbezug und Prüffreigabe importieren.
- Dokument-/Bilderkennung liefert lediglich überprüfbare Vorschläge; keine stillschweigende Zuordnung unklarer Anschlüsse.
- Vorgeschriebene Projektabstände, Geometriekollisionen, Kabelbiegeradien und Einhausungsfreiräume nachvollziehbar prüfen.
- Schematische Eingaben von bestätigten Vermessungen trennen; Änderungshistorie und Abnahmeprotokolle speichern.

**Keine Freigabeautomatik:** Ob und nach welchen Vorgaben die Planung genehmigungsfähig ist, muss fachlich geklärt werden.
