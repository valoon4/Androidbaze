# Video 2 – Requirements Engineering (05.11.2026)

**Quellen:** Benutzerupload eines 60,9-Sekunden-Videos vom Monitor sowie ein Screenshot mit der Oberfläche "AVL 0116 – Schachteditor / Referenzschacht 13_T_021". Die Angaben zum Markt, zu verlegten Kabelmengen und zu anfallenden Projektkosten stammen aus mündlichen Einschätzungen und sind nicht durch das Video belegt.

## Beobachtungen (Video/Screenshot)

- Im Video wird ein geschlossener Schacht von mehreren Seiten in 3D gedreht. Vier Wände, viele runde Wanddurchbrüche, Boden und Aufbauten sind sichtbar.
- Kabel/Leitungen sind als dicke, farbige geschwungene Stränge dargestellt, die zwischen Öffnungen an unterschiedlichen Wänden verlaufen. Kurven und Kreuzungen sind sichtbar, ohne dass ihr realer Biegeradius überprüfbar ist.
- Der Screenshot zeigt den Titel "Referenzschacht 13_T_021", Reiter "3D", "Oben", "Bodenplatten", "SW1", "SW2", "SW3", "SW4" und Funktionen "Kabel umlegen" bzw. "Ansicht drehen".
- Auf der rechten Seite sind die Gruppen OLA und MS, deren eigene Einhausungen, transparente oder deckende Ansichten, ein Auswahlbereich für Kabel sowie Anschlüsse A/B sichtbar.
- Im linken Chatbereich werden als gewünschte Funktionen Schachtwand-Dimensionen und -Stärken, DN und mm-genaue Rohrpositionen, Leitungsführung mit Mindestbiegeradien, Plattenmaterialien und Konsolen/Auflager sowie Projektdatei und Varianten genannt. Dies ist **Anforderungstext**, kein im Video demonstrierter Funktionsnachweis.

## Umsetzung in v0.2

| ID | Funktion | Status |
|---|---|---|
| REQ-01 | Vier Wände mit eigener Geometrie und Stärke | Demonstrator |
| REQ-02 | Mehrere DN-Leerrohre, Wand-Raster und mm-Positionen | Implementiert, geometrische Hinweise |
| REQ-03 | Farbige Kabel mit Anbindung an Öffnungen | Implementiert |
| REQ-04 | Manuell anpassbare Routen mit Kontrollpunkten | Implementiert als visuelle 3D-Splines |
| REQ-05 | OLA/MS in getrennten Einhausungen, sechs Platten je Einheit | Schematische Darstellung |
| REQ-06 | Orthografische Einzelansichten SW1–SW4 und oben | Implementiert |
| REQ-07 | Projekt speichern, wieder laden, Ansicht als PNG | Implementiert |
| REQ-08 | Mindestbiegeradien technisch validieren | Offen |
| REQ-09 | Material-/Schichtaufbau, Brandschutz, Einbauprüfung | Offen |
| REQ-10 | Konsolen/Auflager frei positionieren | Offen |
| REQ-11 | Bauabschnitts-/Revisionsverwaltung, as-built vs. Soll | Offen |
| REQ-12 | Multiprojekt-/Nutzerverwaltung, Audit-Trail, Rollen | Offen |
| REQ-13 | PDF-Pläne, Stücklisten, Prüfprotokolle | Offen |
| REQ-14 | Eignung zur Ausführungsplanung durch Fachkräfte | Unbestätigt, aktuell ausdrücklich NEIN |

## Fachlich zu klären

- Welche Schachttypen, Wandarten, Materialien und Normen gelten für unterschiedliche Bahnhofsprojekte?
- Werden Kabel in *Leerrohren*, Kanälen oder freier Trasse geführt? Wie wird das vorhandene as-built erfasst?
- Wo werden Anfang/Ende der Kabel exakt definiert (Raum, Ebene, Schacht, Höhe, Rohr)?
- Welche Mindestbiegeradien gelten je Kabeltype, Installation und Hersteller?
- Welche Nachweise benötigt ein Elektriker/Prüfer für eine fachlich verwertbare Freigabe?
- Wie wird ein Kabelverlauf von einer planerischen Soll-Situation zu kontrollierter und dokumentierter Ist-Situation?
- Wie heißen die Gruppen OLA, MS, LST, TK Cu im konkreten Betrieb; sind Einhausungen fachlich für jede Gruppe erforderlich?
- Werden Projektdateien offline ausgetauscht oder benötigt ein Auftraggeber revisionssichere Mehrbenutzerfunktionen?

## Produktstrategie / Priorität

P0: Bedienbare 3D/2D-Geometrie, Leerrohrpositionen, Kabelzuordnung und persistentes Projektformat.
P1: Einzieh- und Kollisionsregeln, Biegeradiusprüfung, korrekte Material- und Schottungsdefinition, Normparameter.
P2: Prüfbericht, Baustellen-Ist-Soll-Abgleich, Versionsgeschichte und CAD/BIM-Austausch.
P3: Mandantenverwaltung, Lizenzen, Berechtigungen, Schulungs- und Supportprozess.

**Vorsicht:** Der gezeigte Referenzschacht ist keine ausreichende Grundlage für physische Sicherheitsprüfungen oder Behauptungen über Bauverzögerungen bzw. fehlerhaft verlegte Kilometer.
