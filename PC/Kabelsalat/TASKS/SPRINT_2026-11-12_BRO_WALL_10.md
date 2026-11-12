# Kabelsalat · zweiter Bro-Fokus-Sprint: Wandnormalen und Rohrabstände

**Datum:** 12.11.2026 · **Build:** v2.0.4-beta.1 · **Implementierung:** [8c384b5](https://github.com/valoon4/Androidbaze/commit/8c384b567e7842adcfcab1c4aeba8938835227c2). Technische Softwareaufgaben dürfen anhand ihrer klaren Tests abgeschlossen werden; die fachliche Vermessung und die Bro-Sichtabnahme bleiben eigenständige offene Anforderungen.

## 10 bearbeitete und umgesetzte Aufgaben

| KAB | Umsetzung / Nachweis | Status |
|---|---|---|
| **0019** | SW1-Stirnwand: inward (0,0,1), outward (0,0,−1), Extrusion nach innen, PDF-Vektorpositions- und Kameratest. | **Abgeschlossen** |
| **0020** | SW2 rechte Wand: inward (−1,0,0), outward (+1,0,0), U-Achse über Z, Transformation/Kamera getestet. | **Abgeschlossen** |
| **0021** | SW3-Gegenwand: inward (0,0,−1), outward (+Z), U-Spiegelung gegenüber SW1, inverse Positionen getestet. | **Abgeschlossen** |
| **0022** | SW4 linke Wand: inward (+1,0,0), outward (−1,0,0), U-Spiegelung gegenüber SW2, inverse Positionen getestet. | **Abgeschlossen** |
| **0023** | Rechtshändiges Weltkoordinatensystem X zu SW2/Y nach oben/Z zu SW3, Längen-/Diagrammeinheiten und Alt-HTML-Ausnahme dokumentiert. | **Abgeschlossen** |
| **0044** | Alle gemischten DN160/DN110-Paare der PDF-V2-Szene mit ihren **individuellen** Radiuswerten und tatsächlichem Modell-Mittenabstand ausgewertet. | **Abgeschlossen** |
| **0045** | DN75 auf SW2/Trasse 3/LR01 gegen korrekte PDF-Wandbreite D und Randabstand als schematischer Modellhinweis geprüft. | **Abgeschlossen** |
| **0046** | Zwei DN63-Leerrohre auf SW1/Trasse 2 gegen die für PDF korrekte Wandbreite W und das Modell-Randmaß geprüft. | **Abgeschlossen** |
| **0047** | `quality.auditWallOpenings` berechnet kleinste lichte Breite, Randabstände, Überlappungen und betroffene IDs; unterscheidet Quelle/62-%-Vorschau, kein Projektmutieren. | **Abgeschlossen** |
| **0048** | Im 3D-Rohr-Renderer und 2D-Plan sind rote Warnringe für durch Verschiebung/Verdichtung kollidierende oder randüberschreitende Durchbrüche implementiert; synthetische Kollisionstestfixture geprüft. | **Implementiert; 3D-Chromium-Sichtnachweis noch offen** |

## Zahlen nur zum Verständnis, keine Vermessung

Im vorhandenen Modell `13_U_028 / PDF V2` mit 91 Leerrohren berechnet die rein geometrische Modellprüfung aus **PDF-Diagrammkoordinaten** und **Platzhalter-Schachtabmessungen** eine minimale lichte Breite ca. **144,5 mm**, einen minimalen Randabstand ca. **63,9 mm** und in der optionalen 62-%-Vorschau eine minimale lichte Weite ca. **28,8 mm**. Das ist **keine** Aussage über reales Beton- oder Brandschutzmaß und nicht für Bauentscheidungen verwendbar.

Ein **explizit synthetischer** DN60/DN110-Fall zeigt 15 mm lichte Weite in Ausgangsposition und −23 mm nach einer auf 62 % skalierten Vorschau; rote Warnungen müssen dann in 2D/3D sichtbar sein. Die Quelle selbst bleibt unverändert.

## Test- und Buildnachweise

- [x] `tests/pdf-wall-clearance.test.js` wurde zusätzlich in `npm test` aufgenommen; GitHub Pages-`npm test` **grün** ([Run #43206964603](https://github.com/valoon4/Androidbaze/actions/runs/43206964603)).
- [x] Windows-Portable-EXE / Browser-ZIP / Einzel-HTML mitsamt `npm test` **grün** ([Run #43206964606](https://github.com/valoon4/Androidbaze/actions/runs/43206964606)).
- [ ] Separater echter Chromium-Visualtest für 2D/3D ([Run #43206964608](https://github.com/valoon4/Androidbaze/actions/runs/43206964608)); bei Anlage dieses Sprintreports nicht beendet.
- [ ] Bro hat die neue Warnring-Darstellung noch nicht selbst visuell beurteilt.

**Kanonischer Fortschritt nach diesem Sprint:** **19/884 erledigt**, davon **9** neu geschlossen; **865 offen**. **KAB-0048** bleibt bis zum visuellen Nachweis offen. Kein anderer Task wurde nur wegen ähnlicher Funktionalität automatisch als erfüllt markiert.

**Detailkonzept und Formeln:** [WALL_COORDINATES_AND_CLEARANCE.md](../WALL_COORDINATES_AND_CLEARANCE.md). **GitHub-Issue:** [#71](https://github.com/valoon4/Androidbaze/issues/71).
