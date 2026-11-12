# Kabelsalat · PDF-V2-Koordinaten und Rohrabstandsprüfung

**Version:** 12.11.2026, technische Softwareprüfung nach der vorhandenen PDF-V2-Dokumentation (S.4, S.21–24). **Keine Vermessungs- oder AVL0116-Baufreigabe.** Testbasis: `pdf-geometry.js`, `quality.js`, `tests/pdf-wall-clearance.test.js`, bestehende `pdf-u028`-Referenz.

## Rechtshändiges Weltkoordinatensystem (KAB-0023)

- **X+** zeigt im 3D-Referenzsystem zur rechten Schachtwand **SW2**, **X−** nach **SW4**. 
- **Y+** zeigt vertikal nach oben. Der Schachtboden liegt bei **Y=0** im Modell.
- **Z+** zeigt zur Gegenwand **SW3**, **Z−** zur Stirnwand **SW1**.
- Rechtshändig: **X × Y = Z**. Maße in `pdf-geometry.js` in Metern; Import-/Modellmaße in Millimetern.
- `u,v∈[0,1]` sind 2D-Diagrammanteile auf der jeweiligen Wand, **keine vermessenen realen Millimeterkoordinaten**. `v=0` ist unten, `v=1` oben.

## Wandrichtung, Öffnung, Kamera (KAB-0019–0022)

| Wand | Wandebene | Lokales U von 0→1 | Innennormale | Außennormale | Extrusionsrotation Y | Innenkamera |
|---|---|---|---|---|---|---|
| SW1 · Stirnwand | Z = −D/2 | X: −W/2 → +W/2 | (0,0,+1) | (0,0,−1) | 0 | Kamera von Z+ |
| SW2 · rechte Wand | X = +W/2 | Z: −D/2 → +D/2 | (−1,0,0) | (+1,0,0) | −π/2 | Kamera von X− |
| SW3 · Gegenwand | Z = +D/2 | X: +W/2 → −W/2 | (0,0,−1) | (0,0,+1) | π | Kamera von Z− |
| SW4 · linke Wand | X = −W/2 | Z: +D/2 → −D/2 | (+1,0,0) | (−1,0,0) | +π/2 | Kamera von X+ |

Die **24-mm-Verschiebung** der symbolischen Öffnungsmittelpunkte zur Schachtinnenseite ist eine bestehende **Rendering-Offset-Konstante**, **kein** bestätigtes Rohr-Aufmaß. Die Wandextrusion folgt der Innennormalen; Rohrmittelpunkte dürfen nicht zwischen SW1/SW3 oder SW2/SW4 gespiegelt werden. Regression prüft U→XYZ→U für alle vier Wände, v-Höhen und 3D-Kamerablick.

## Lichte Rohr- und Wandabstände (KAB-0044–0048)

Für Kreisöffnung `i` mit Mittelpunkt `(ui·W,vi·H)` und **Öffnungsradius `ri`**, nicht beliebig gleichgesetztem DN, gilt:

- Mittelpunktabstand zweier Rohre: `dij = hypot((ui−uj)·W, (vi−vj)·H)`.
- **Lichte Weite (modellbasiert)**: `gij = dij − ri − rj`; negativ = modellierte Durchbrüche schneiden sich.
- **Randabstand**: `ei = min(ui·W−ri, (1−ui)·W−ri, vi·H−ri, (1−vi)·H−ri)`; negativ = Randüberschreitung.
- `dimensionSpec.openingMm` hat Vorrang vor schematischem `dn` bei der Anzeige-/Modellberechnung. Eine **unbestätigte** Größe bleibt ausdrücklich unbestätigt.
- `quality.auditWallOpenings(project[,visualHole])` prüft alle Paare und Wandränder, liefert kleinste lichten Werte, betroffene Rohr-IDs und rohe Paarwerte; bei optionaler 62-%-Vorschau **nur Anzeige-Koordinaten**, keine Änderung von `u/v`, `sourceU/sourceV` oder Kabelanschlüssen.
- 3D- und 2D-Ansicht markieren **Kollisionen rot**. Die Modellprüfung benennt ihre schematische Herkunft und enthält keinerlei automatische DIN-, VDE- oder Brandschutzfreigabe.

### Referenzfall `13_U_028` (schematische PDF V2)

Synthetische 3D-Modelldimensionen: W=2.750 mm, D=2.350 mm, H=1.850 mm. **Diese Werte sind keine vermessenen Schachtmaße.**

| Prüffall | Verbindliche Testaussage |
|---|---|
| SW1–SW4 | 29 / 13 / 26 / 23 Rohröffnungen, insgesamt 91 |
| DN160 / DN110 | Berechnung verwendet je Rohr den eigenen Radius, prüft nächstliegende gemischte DN-Paare statt gleiche Radien zu unterstellen |
| SW2 / Trasse 3 / LR01 · DN75 | Modell-/Diagramm-Randabstand wird für SW2 mit **D** gerechnet; Wert positiv im vorhandenen V2-Diagramm |
| SW1 / Trasse 2 / LR01 und LR02 · DN63 | Modell-/Diagramm-Randabstand wird für SW1 mit **W** gerechnet; beide im Diagramm innerhalb der Wand |
| Unverdichtete PDF-Vektorpunkte | Mindestweite über alle Rohrpaare ca. **144,5 mm**, geringster Randabstand ca. **63,9 mm** |
| Historische 62-%-Basic-Demovorschau | Mindestweite ca. **28,8 mm**, ohne Änderung der Quellkoordinaten; keine Null-/Negativkollision im aktuellen PDF-Satz |
| Synthetische Kollisions-Fallprobe | Zwei Öffnungen DN60 + DN110 bei 100 mm Mittenabstand = **15 mm** Lichte; 62-%-Vorschau = **−23 mm**, damit sichtbarer roter Warnring in 2D und 3D |

**Wichtig:** Dass diese *schematischen* Abstände im Demomodell nicht negativ sind, sagt **nichts** über die realen Betonüberdeckungen, Trassen, mechanische Zulässigkeit oder AVL0116-Konformität aus. Rohrkollisionen können in einem anderen Projekt/bei manuell eingegebenen Größen auftreten und werden dann angezeigt.

## Technik und Nachweise

- Quelle/Mapping: `pdf-geometry.js` (vier Richtungen, Transform, Inverse, Kamera).
- Geometriedaten: `quality.js → auditWallOpenings` (nicht destruktiv), Aufruf aus `app.js` (2D/3D/Profi-Hinweis).
- Unit-Regression: `tests/pdf-wall-clearance.test.js` (4 Wand-IDs, Richtungen, XYZ-Rückabbildung, 91 PDF-Öffnungen, DN160/110, DN75, DN63, synthetischer Überlappungsfall).
- Änderungen an der Dokumentationsquelle, fehlende Herstellerangaben oder Grenzen → **offen**, keine automatische technische Abnahme.
