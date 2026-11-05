# Schachtwand Planer 3D – Windows (v0.2)

Prototyp eines **lokalen, portablen Windows-Schachtwandeditors**, weiterentwickelt anhand eines 61-sekündigen zweiten Videos und eines Screenshots vom Referenzschacht 13_T_021 (AVL 0116). Kein Login, kein Internetbetrieb und keine dauerhafte Installation erforderlich.

## Bereits umgesetzt

- **Vier Schachtwände** SW1–SW4, je einzeln sichtbar, unabhängig konfigurierbar (Raster, Wandstärke in mm, Standard-Rohrdurchmesser in mm).
- **Leerrohre** mit individuellen DN-/Durchmesserangaben und mm-genauer horizontaler sowie vertikaler Position. Löcher als Geometrie-Ausschnitte durch die modellierte Wandstärke.
- **Wand- und Top-Ansichten**: 3D / Oben / Bodenplatten / SW1 / SW2 / SW3 / SW4. Die SW-Ansichten verwenden eine orthografische Kamera.
- **16 Demo-Kabel** in Gruppen OLA (3), MS (2) und Weitere (11). Eigene Kabel anlegen, Farbe, Durchmesser, Gruppe sowie Ein- und Ausgang auswählen.
- **Kabelverlauf**: automatisch als gekrümmte Linie visualisiert oder mittels zusätzlicher Bogenpunkte, die rechts über X/Y/Z in mm bearbeitet oder als gelbe Griffe im Modell gezogen werden.
- **Getrennte Einhausungen** OLA und MS als je sechs Platten (Boden, Decke, vier Seiten) mit unabhängiger Sichtbarkeit und Deckkraft; optional gemeinsam ausblendbar.
- **Bearbeiten**: Drehen mit Maus, Zoomen mit Mausrad, Öffnungen platzieren/verschieben/löschen, Kabelgriffe auf neue Leerrohre ziehen, Rückgängig/Wiederholen.
- **Geometrische Hinweise**: Rohrkreise, die sich in einer Wand überlappen oder Wandränder schneiden, werden **numerisch** gemeldet.
- **Lokal speichern**: Projekt-JSON exportieren/importieren, Autosave, PNG der aktuellen Modellansicht.

## WICHTIGE SICHERHEITS- UND PLANUNGSGRENZEN

Diese Software ist ein **Visualisierungs- und Datenmodellierungsprototyp**. Weder Kabelwege noch Einhausungen wurden statisch oder elektrotechnisch berechnet. **Es gibt KEINE** Prüfung von Mindestbiegeradien, Rohrfüllung, Einziehlängen, thermischer Belastbarkeit, Brandschutz-Abschottungen, Schallschutz, Schachtstatik, Freigaben, Toleranzen, normgerechter Installation oder DIN-/VDE-Konformität. Die Einhausungsplatten sind einfache schematische Rechteckflächen, keine konstruktiv detaillierten Baugruppen; sie bilden keine dicht geprüften Gehäuse. Eine fehlende geometrische Warnung ist **keine Ausführungsfreigabe**. Nicht als alleinige Grundlage für Bauausführung, Ausschreibung oder Abnahme verwenden.

Die gezeigten Demo-Maße, Öffnungen und Kabelführungen sind keine as-built-Daten eines realen Referenzschachts. Fachliche Ist-Soll-Prüfung und versionierte Bauwerksdokumentation stehen noch aus.

## Erste Bedienung

1. GitHub-Release öffnen und die Windows-EXE herunterladen.
2. **Neu** erzeugt einen leeren Schacht (4 Wände ohne Öffnungen und Kabel). Alternativ direkt mit dem Referenzbeispiel starten.
3. In der linken Leiste Breite, Tiefe, Höhe sowie pro Wand **Wandstärke** und **Standard-DN** eintragen.
4. Über **Raster neu erstellen** Öffnungen anlegen oder über **+ Leerrohr** eine Position im 3D- oder Wandmodus anklicken. Ein Leerrohr anwählen, um horizontale Position, Höhe und Durchmesser in mm zu bearbeiten.
5. Rechts **+ Kabel** auswählen, Gruppe (OLA/MS/Weitere), Kabel-Außendurchmesser und Anfang/Ende eingeben; Bogenpunkte setzen, um den Verlauf anzupassen.
6. Im unteren Modellrand auf **Oben**, **Bodenplatten** oder **SW1–SW4** wechseln. Rechts OLA-/MS-Einhausungen ein- oder ausblenden.
7. **Projekt speichern** speichert eine JSON-Datei. **Öffnen** lädt diese erneut; der Browser-Kern hält zusätzlich ein lokales Autosave. **PNG** exportiert die momentane Ansicht.

## EXE / Quellcode

Release: https://github.com/valoon4/Androidbaze/releases/tag/schachtwand-planer-latest

Das neueste Artefakt heißt **SchachtwandPlaner-0.2.0-Windows.exe** und wird durch GitHub Actions unter Windows gebaut.

Zum lokalen Ausführen mit Node.js 22 oder neuer im Ordner PC/SchachtwandPlaner:

    npm install
    npm test
    npm start

Zum Bauen auf Windows:

    npm run dist:win

Das Ergebnis befindet sich im Verzeichnis dist/. Verwendet wird die lokale Three.js-Bibliothek mit der separat enthaltenen Originallizenz; WebGL-Unterstützung ist erforderlich.

## Anforderungen aus dem zweiten Video

Siehe [VIDEO2_REQUIREMENTS.md](./VIDEO2_REQUIREMENTS.md): sichtbare Funktionen, Ableitungen und noch unbestätigte Fachfragen.

**Nicht enthalten in v0.2:** freie Schachtgeometrie beliebig vieler Wände, generischer 2D-CAD-Import, Kollisionsprüfung von Kabeln und Baukörpern, überprüfte Mindestbiegeradien und Rohrfüllgrade, Konsolen-Editor, Materialbibliothek, gemeinsames Echtzeit-Arbeiten, PDF-Pläne, Berichte zur Bauabnahme.
