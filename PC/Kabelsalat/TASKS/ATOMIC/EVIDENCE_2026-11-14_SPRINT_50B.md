# Kabelsalat · Sprint 4 / 50 atomare Nachweise · 14.11.2026

**Arbeit:** Anforderungs-/Code-Eingrenzung, synthetischer Geometrie-Regressionscode und Prüfkriterien. **Nicht:** 50 fertige Features, neue geprüfte Schachtmaße oder Bro-Sichtabnahme.

**Technische Änderungen:** [`tests/sprint-50b-2026-11-14.test.js`](../../tests/sprint-50b-2026-11-14.test.js) deckt dimensions- und quellsichere Geometrievergleiche ab. In [`package.json`](../../package.json) ist der Test in `npm test` eingebunden. Die App-Produktionsgeometrie wurde nicht geändert.

| Mikro-ID | Ergebnis / nachprüfbarer Beleg |
|---|---|
| KAB-0051/M01 | Code-Einstieg identifiziert: `quality.auditWallOpenings`, `schematic-positions.display`, `app.js` (`change`, `undoRedo`, `wallSpanMm`) – **kein** schon vorhandener fertiger Rohrabstand-Regler behauptet. |
| KAB-0051/M02 | Positionen: `u/v` dimensionslos, Projektgeometrie und lichte Abstände in **mm**, der 3D-Renderer rechnet in **m**; PDF-Wandachsen anders als Legacy-HTML. |
| KAB-0051/M03 | Minimalbeispiel: zwei synthetische SW1-Öffnungen in 1000×1000-mm-Wand, DN60 bei u=.4 und DN110 bei u=.5; Mittelpunkt 100 mm, lichte Weite 15 mm. |
| KAB-0051/M08 | Negativnebeneffekt für Modellvergleich ausgeschlossen: Auditroutine greift nur lesend auf `project.walls` zu, bestehende Kabelreferenzen `C-1 → LR-A/LR-B` bleiben im Fixture identisch. |
| KAB-0051/M12 | In `sprint-50b-2026-11-14.test.js` werden Quell-U/V unverändert gehalten, `schematicPositions.display(h,true)` erzeugt nur 62%-Vorschau; kein Persistieren der Demo-Verschiebung. |
| KAB-0051/M13 | Rundung und Maße gegengeprüft: geometrischer Abstand `100 − 60/2 − 110/2 = 15 mm` und U-Projektionswert `.4→.438` bei 62% Kompaktansicht. Keine Umdeutung in Meter. |
| KAB-0051/M15 | Geltungsgrenze: Modellwerte sind schematisch; kein geprüfter Herstellerabstand, keine DIN-/Brandschutz-Konformität, kein endgültiger Abstand-Regler oder Bro-Sichttest. |
| KAB-0052/M01 | Code-Einstieg identifiziert: `quality.auditWallOpenings`, `schematic-positions.display`, `app.js` (`change`, `undoRedo`, `wallSpanMm`) – **kein** schon vorhandener fertiger Rohrabstand-Regler behauptet. |
| KAB-0052/M02 | Positionen: `u/v` dimensionslos, Projektgeometrie und lichte Abstände in **mm**, der 3D-Renderer rechnet in **m**; PDF-Wandachsen anders als Legacy-HTML. |
| KAB-0052/M03 | Minimalbeispiel: zwei synthetische SW1-Öffnungen in 1000×1000-mm-Wand, DN60 bei u=.4 und DN110 bei u=.5; Mittelpunkt 100 mm, lichte Weite 15 mm. |
| KAB-0052/M08 | Negativnebeneffekt für Modellvergleich ausgeschlossen: Auditroutine greift nur lesend auf `project.walls` zu, bestehende Kabelreferenzen `C-1 → LR-A/LR-B` bleiben im Fixture identisch. |
| KAB-0052/M12 | In `sprint-50b-2026-11-14.test.js` werden Quell-U/V unverändert gehalten, `schematicPositions.display(h,true)` erzeugt nur 62%-Vorschau; kein Persistieren der Demo-Verschiebung. |
| KAB-0052/M13 | Rundung und Maße gegengeprüft: geometrischer Abstand `100 − 60/2 − 110/2 = 15 mm` und U-Projektionswert `.4→.438` bei 62% Kompaktansicht. Keine Umdeutung in Meter. |
| KAB-0052/M15 | Geltungsgrenze: Modellwerte sind schematisch; kein geprüfter Herstellerabstand, keine DIN-/Brandschutz-Konformität, kein endgültiger Abstand-Regler oder Bro-Sichttest. |
| KAB-0053/M01 | Code-Einstieg identifiziert: `quality.auditWallOpenings`, `schematic-positions.display`, `app.js` (`change`, `undoRedo`, `wallSpanMm`) – **kein** schon vorhandener fertiger Rohrabstand-Regler behauptet. |
| KAB-0053/M02 | Positionen: `u/v` dimensionslos, Projektgeometrie und lichte Abstände in **mm**, der 3D-Renderer rechnet in **m**; PDF-Wandachsen anders als Legacy-HTML. |
| KAB-0053/M03 | Minimalbeispiel: zwei synthetische SW1-Öffnungen in 1000×1000-mm-Wand, DN60 bei u=.4 und DN110 bei u=.5; Mittelpunkt 100 mm, lichte Weite 15 mm. |
| KAB-0053/M08 | Negativnebeneffekt für Modellvergleich ausgeschlossen: Auditroutine greift nur lesend auf `project.walls` zu, bestehende Kabelreferenzen `C-1 → LR-A/LR-B` bleiben im Fixture identisch. |
| KAB-0053/M12 | In `sprint-50b-2026-11-14.test.js` werden Quell-U/V unverändert gehalten, `schematicPositions.display(h,true)` erzeugt nur 62%-Vorschau; kein Persistieren der Demo-Verschiebung. |
| KAB-0053/M13 | Rundung und Maße gegengeprüft: geometrischer Abstand `100 − 60/2 − 110/2 = 15 mm` und U-Projektionswert `.4→.438` bei 62% Kompaktansicht. Keine Umdeutung in Meter. |
| KAB-0053/M15 | Geltungsgrenze: Modellwerte sind schematisch; kein geprüfter Herstellerabstand, keine DIN-/Brandschutz-Konformität, kein endgültiger Abstand-Regler oder Bro-Sichttest. |
| KAB-0054/M01 | Code-Einstieg identifiziert: `quality.auditWallOpenings`, `schematic-positions.display`, `app.js` (`change`, `undoRedo`, `wallSpanMm`) – **kein** schon vorhandener fertiger Rohrabstand-Regler behauptet. |
| KAB-0054/M02 | Positionen: `u/v` dimensionslos, Projektgeometrie und lichte Abstände in **mm**, der 3D-Renderer rechnet in **m**; PDF-Wandachsen anders als Legacy-HTML. |
| KAB-0054/M03 | Minimalbeispiel: zwei synthetische SW1-Öffnungen in 1000×1000-mm-Wand, DN60 bei u=.4 und DN110 bei u=.5; Mittelpunkt 100 mm, lichte Weite 15 mm. |
| KAB-0054/M08 | Negativnebeneffekt für Modellvergleich ausgeschlossen: Auditroutine greift nur lesend auf `project.walls` zu, bestehende Kabelreferenzen `C-1 → LR-A/LR-B` bleiben im Fixture identisch. |
| KAB-0054/M12 | In `sprint-50b-2026-11-14.test.js` werden Quell-U/V unverändert gehalten, `schematicPositions.display(h,true)` erzeugt nur 62%-Vorschau; kein Persistieren der Demo-Verschiebung. |
| KAB-0054/M13 | Rundung und Maße gegengeprüft: geometrischer Abstand `100 − 60/2 − 110/2 = 15 mm` und U-Projektionswert `.4→.438` bei 62% Kompaktansicht. Keine Umdeutung in Meter. |
| KAB-0054/M15 | Geltungsgrenze: Modellwerte sind schematisch; kein geprüfter Herstellerabstand, keine DIN-/Brandschutz-Konformität, kein endgültiger Abstand-Regler oder Bro-Sichttest. |
| KAB-0884/M01 | Prüfziel isoliert: echte kreisförmige Durchbrüche mit synthetischen DN 60/110/116, Quellkoordinaten/-IDs unverändert; kein fachliches Aufmaß. |
| KAB-0884/M02 | Fixtures: `tests/concrete-wall.test.js` + neuer `tests/sprint-50b-2026-11-14.test.js`; Betonrenderer `concrete-wall.js`. |
| KAB-0884/M03 | Minimalfixture: drei synthetische Öffnungen mit 60, 110, 116 mm, separate `THREE.Path.absarc` pro Radius. |
| KAB-0884/M04 | Eine exakte Assertionsgruppe ergänzt: `made.accepted.length===3` und extrudierte Kreisradien ergeben `[60,110,116]`; Quellarray wird nicht mutiert. |
| KAB-0884/M07 | Quellintegrität überprüft: synthetische Geometrie bleibt JSON-identisch, `pdf-u028` behält `[29,13,26,23]` = 91 LR und unveränderte Projektdaten. |
| KAB-0884/M10 | Neuer Regressionstest über `package.json` in `npm test` eingebunden; Nachweis des erfolgreichen CI-Laufs separat verlinkt, keine Browser-Fachabnahme. |
| KAB-0884/M12 | Testnachweis dokumentiert: synthetisches DN-Regressionsexperiment; weiterhin ausstehend sind Browser-Sichtvergleich, Lichteindruck, echte Aufmaße/Bro-Abnahme. |
| KAB-0199/M01 | Ansichts- und Kamera-Einstieg in `app.js` über `startPitch`, `pdfView`, `exploreCameraShot`, `viewMode` und `renderExplorerUI` identifiziert. |
| KAB-0199/M03 | Sichtbares Element konkret: PDF-Schachtwand, kreisförmige LR-Öffnungen und 3D-Kamera sind getrennt von farbigen Kabelwegen und Planmarkern. |
| KAB-0199/M04 | Prüfkriterium nur als Soll festgelegt: Pitch muss `pdf-u028` anzeigen, aber einen abgelehnten, ungespeicherten Projektwechsel respektieren. |
| KAB-0199/M14 | Bro-Sichtprüfung und externe Bau-Freigabe ausdrücklich **offen**; kein falsches Review-Ergebnis vergeben. |
| KAB-0200/M01 | Ansichts- und Kamera-Einstieg in `app.js` über `startPitch`, `pdfView`, `exploreCameraShot`, `viewMode` und `renderExplorerUI` identifiziert. |
| KAB-0200/M03 | Sichtbares Element konkret: PDF-Schachtwand, kreisförmige LR-Öffnungen und 3D-Kamera sind getrennt von farbigen Kabelwegen und Planmarkern. |
| KAB-0200/M04 | Prüfkriterium nur als Soll festgelegt: Vier PDF-V2-Wände müssen zusammen genau 91 LR-Öffnungen haben, Planmarker dürfen nicht als zusätzliche physische LR zählen. |
| KAB-0200/M14 | Bro-Sichtprüfung und externe Bau-Freigabe ausdrücklich **offen**; kein falsches Review-Ergebnis vergeben. |
| KAB-0201/M01 | Ansichts- und Kamera-Einstieg in `app.js` über `startPitch`, `pdfView`, `exploreCameraShot`, `viewMode` und `renderExplorerUI` identifiziert. |
| KAB-0201/M03 | Sichtbares Element konkret: PDF-Schachtwand, kreisförmige LR-Öffnungen und 3D-Kamera sind getrennt von farbigen Kabelwegen und Planmarkern. |
| KAB-0201/M04 | Prüfkriterium nur als Soll festgelegt: 3D-Startkamera muss beim Pitch die vier Wände erkennbar machen; die optische Abnahme fehlt. |
| KAB-0201/M14 | Bro-Sichtprüfung und externe Bau-Freigabe ausdrücklich **offen**; kein falsches Review-Ergebnis vergeben. |
| KAB-0202/M01 | Ansichts- und Kamera-Einstieg in `app.js` über `startPitch`, `pdfView`, `exploreCameraShot`, `viewMode` und `renderExplorerUI` identifiziert. |
| KAB-0202/M03 | Sichtbares Element konkret: PDF-Schachtwand, kreisförmige LR-Öffnungen und 3D-Kamera sind getrennt von farbigen Kabelwegen und Planmarkern. |
| KAB-0202/M04 | Prüfkriterium nur als Soll festgelegt: Wandfokus muss aktive Wand plus lesbaren Titel zeigen; die optische Abnahme fehlt. |

**Grenzen:** KAB-0051/0052/0053 bleiben als Bedienungsfeatures offen; bislang ist nur die statische Grundlage und der Regressionstest geliefert. KAB-0054 und KAB-0199–0202 erfordern für visuelle Abnahme weiter echten Browser und Feedback. KAB-0884 braucht echte Darstellung/Screenshot gegen Bro. Bestehende Quellenkoordinaten werden nicht als vermessen bezeichnet.
