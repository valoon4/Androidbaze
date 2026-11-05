# Schachtwand Planer 3D · Windows

Interaktiver **Offline-Prototyp** nach einem kurzen Referenzvideo: ein 3D-Schacht mit drei Schachtwänden (SW1–SW3), veränderbaren Leerrohr-Rastern, frei positionierbaren Öffnungen, einem optionalen OLA-Kasten und farbigen Kabelwegen.

## Bedienung

1. Windows-EXE aus dem GitHub Release herunterladen und starten.
2. Links Maße, Wand und gewünschte Anzahl Reihen × Öffnungen einstellen. **Raster neu erstellen** legt die Öffnungen an.
3. Im Modus **＋ Leerrohr** durch Klick auf eine Wand neue Öffnungen platzieren; im Modus **↔ Versetzen** Öffnungen mit der Maus verschieben. Auch Prozentpositionen sind bearbeitbar.
4. Rechts Kabel anlegen, benennen, Farbe wählen, Anschlüsse A/B zuordnen. Alternativ die weißen Griffe des ausgewählten Kabels zu anderen Öffnungen ziehen.
5. Maus: ziehen = Modell drehen, Mausrad = zoomen. Wanddeckkraft, OLA-Kasten und Beschriftungen lassen sich anpassen.
6. **Projekt speichern** exportiert eine JSON-Datei; **Öffnen** importiert sie. Ein Autosave in der lokalen App hält die aktuelle Arbeitsfassung vor. **PNG** exportiert die aktuelle 3D-Ansicht.

**Wichtig:** Die dargestellten Wandöffnungen sind grafische Platzhalter und keine tatsächlich durchbrochenen Wandkörper. Bemaßungen sind in Millimetern. Kabelverläufe werden zur Veranschaulichung interpoliert und sind **keine geprüften Leitungswege**. Rohrfüllung, Mindestbiegeradien, Statik, Brandschutz, DIN/VDE und baurechtliche Eignung werden nicht geprüft. Keine Verwendung als alleinige Grundlage für Ausführung oder Abnahme.

## Windows-EXE beziehen

GitHub → **Releases** → **Schachtwand Planer – Windows EXE** → **SchachtwandPlaner-0.1.0-Windows.exe**. Bei neuem Commit startet der Windows-Build über GitHub Actions und aktualisiert das Release nach erfolgreichem Abschluss.

## Selbst starten / bauen

Node.js 22+ vorausgesetzt, im Ordner PC/SchachtwandPlaner:

    npm install
    npm start
    npm test
    npm run dist:win

Das Ergebnis liegt unter dist/. Die App verwendet eine lokal eingebundene Three.js-Bibliothek aus dem bestehenden Repo-Vendorstand, nicht aus einem CDN. Kein Benutzerkonto und kein Server notwendig.

### Offene fachliche Punkte

Die fachliche Bedeutung und geometrische Ausführung des „OLA-Kasten“ sowie reale Anzahl, Maße und Einbaulagen der Schachtwände müssen noch mit dem Anwender abgestimmt werden. Die Demo-Einstellungen sind Platzhalter; das Video allein belegt keine Prüfregeln.
