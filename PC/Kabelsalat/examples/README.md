# Kabelsalat – drei vollständig synthetische Beispielprojekte

**Alle Daten sind frei erfunden und ausschließlich für die Demonstration bestimmt.** Die Projektbezeichnung „13_T_021“ stammt aus der Bildschirmreferenz, ist aber **keine echte Planzeichnung oder Bestandsaufnahme**.

## Direkt öffnen

Links oben steht jetzt **Beispielprojekte**. Im Auswahlfeld eines der drei Beispiele wählen, dann **Beispiel öffnen**. Vor dem Ersetzen eines eigenen Projekts fragt Kabelsalat nach Bestätigung. Bei erstmaligem Start ohne gespeicherte eigene Daten zeigt die Anwendung das Beispiel mit vier Schächten; bestehende lokale Projekte bleiben unangetastet.

Die JSON-Dateien können alternativ über den Button **Öffnen** geladen werden. Beide Release-Pakete (Browser/Windows) enthalten sie im Ordner **examples**. Vor eigener Bearbeitung bitte eine Kopie unter neuem Namen abspeichern.

| Beispieldatei | Inhalt | Sinnvoller Test |
|---|---|---|
| 01_Referenzschacht_13_T_021_DEMO.json | 1 Schacht, 94 Leerrohre, 16 farbige Kabel, OLA/MS und drei Konsolen/Auflager, Planungsvariante | 3D drehen, eine Wand im 2D-Fenster bearbeiten, Rohrkopie, Einzelplatten |
| 02_Bahnhofsnetz_4_Schaechte_DEMO.json | 4 Schächte, 376 Leerrohre, 64 Kabel, 4 Netzverbindungen, 3 durchgehende schematische Kabelwege | **Netz anzeigen**, Kabelweg wählen, Schacht doppelklicken, Netzroute verfolgen, Kabel suchen |
| 03_Prueffall_mit_absichtlichen_Warnungen_DEMO.json | 1 Schacht, 96 Leerrohre, 16 Kabel, absichtlich ungültige Rohr-/Kabeldaten | **Prüfen**: Überlappung, Randüberschreitung, fehlenden Kabelanschluss und nominell zu breites Kabel anzeigen |

## Netzbeispiel – welche Kabel verlaufen wo?

- **NET-OLA-001:** schematischer OLA-Weg S01 → S02 → S03 → S04.
- **NET-MS-001:** schematischer MS-Weg S01 → S02 → S03 → S04.
- **NET-TK-007:** schematischer TK-Weg S01 → S02 → S04 mit Abzweig über Netzlink L-04.

Die drei Wege bestehen die ID-/Schachtlink-Konsistenzprüfung; sie sind **keine real bestätigten, physisch verlegten Leitungen**.

## Prüffall – erwartete Warnungen

Es existiert bewusst ein Rohr nahe bzw. über dem Wandrand, ein überlappendes Rohr, ein unvollständiges Kabel und ein Kabel, dessen äußerer Durchmesser gegenüber dem angegebenen Rohr-DN einen Hinweis auslöst. Alle Warnungen sind **synthetisch** und dienen dem Regressionstest; selbst eine grüne Prüfanzeige wäre keine Baurechts-, VDE- oder Brandschutzfreigabe.

## Bitte beim Praxistest besonders prüfen

1. Gefällt die Bedienung mit 2D-Wandansicht und 3D-Schacht gleichzeitig?
2. Ist die Belegung eines angeklickten Rohrs gut lesbar?
3. Führt das Netzbeispiel von der Gesamtkarte zuverlässig zu einem Kabel im richtigen Schacht?
4. Werden alle drei Projektdateien geöffnet und unverändert wieder gespeichert?
5. Welche Informationen fehlen deinem Bruder für einen echten Planungs-/Kontrollvorgang?

Die vollständige Gegenüberstellung zum verbindlichen AVL0116-Konzept liegt in **../CONCEPT_AUDIT.md**.
