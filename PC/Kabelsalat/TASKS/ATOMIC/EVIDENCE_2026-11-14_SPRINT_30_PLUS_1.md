# Kabelsalat · Sprint 6 · 30 Mikroschritte + KAB-0888 Großaufgabe

**Version:** 2.0.7-beta.1 · 14.11.2026. Die 30 Einträge sind separate, konkrete Teilprüfungen. KAB-0888 ist der größere zusätzliche Integrationsschritt, kein weiterer Mikro-Task.

**CI-Nachweise:** [GitHub-Pages/npm test](https://github.com/valoon4/Androidbaze/actions/workflows/kabelsalat-pages.yml) · [echte Chromium-Screenshots](https://github.com/valoon4/Androidbaze/actions/workflows/kabelsalat-visual.yml) · [Offline-Windows-Release](https://github.com/valoon4/Androidbaze/releases/tag/kabelsalat-latest).

| Mikro-ID | Konkreter Teilnachweis | Stelle |
|---|---|---|
| KAB-0881/M08 | Doppelte DN-Labels derselben Rohr-ID liefern 2 getrennte Kandidaten; kein automatischer Ersatz. | tests/sprint-30-release-2026-11-14.test.js |
| KAB-0881/M11 | UI blockiert Übernahme einer Kandidaten-Rohr-ID auf ein anderes Rohr. | app.js + tests/sprint-30-release-2026-11-14.test.js |
| KAB-0881/M12 | Ohne Rohr-ID wird beim Übernehmen eine explizite Nutzerbestätigung verlangt. | app.js + tests/sprint-30-release-2026-11-14.test.js |
| KAB-0881/M14 | Kandidat einschließlich Bezeichnung, Quelle, Zeile und Status bleibt durch JSON-Serialisierung erhalten. | tests/sprint-30-release-2026-11-14.test.js |
| KAB-0881/M16 | Neue synthetische Planlabel-Regression ist in `npm test` integriert. | package.json + GitHub-Pages-Testlauf |
| KAB-0882/M02 | Leeres synthetisches Bild liefert 0 Kreis-Kandidaten. | tests/sprint-30-release-2026-11-14.test.js |
| KAB-0882/M03 | Einzelner Dunkelkreis erzeugt genau einen Pixel-Durchmesser-Kandidaten. | tests/sprint-30-release-2026-11-14.test.js |
| KAB-0882/M04 | Zwei unterschiedliche Kreise werden getrennt erkannt und nach Größe sortiert. | tests/sprint-30-release-2026-11-14.test.js |
| KAB-0882/M06 | Kalibrierung mit bekanntem 110-mm-Kreis ergibt mm-*Kandidat*, nie `reviewed`. | tests/sprint-30-release-2026-11-14.test.js |
| KAB-0882/M10 | Bilder > 4 MP werden vor dem Detektieren zurückgewiesen. | tests/sprint-30-release-2026-11-14.test.js |
| KAB-0882/M13 | Stark elliptische Kontur wird nicht als sicherer runder Rohrkandidat klassifiziert. | tests/sprint-30-release-2026-11-14.test.js |
| KAB-0882/M17 | Deterministischer synthetischer Foto-Regressionslauf via `npm test`. | GitHub-Pages-Testlauf |
| KAB-0885/M05 | Chromium-Screenshot für Basic-Schacht: `bro-lr-only-shaft-step.png`, Beton mit LR-Öffnungen. | tests/browser-visual.js |
| KAB-0885/M06 | Chromium-Screenshot für Kabelansicht: `bro-cable-routes-separate-step.png`. | tests/browser-visual.js |
| KAB-0885/M08 | Prüffrage nach kürzeren Kabelwegen vs echter Rohrtiefe mit Issue #72 im Parent dokumentiert. | TASKS/08-BRO-KURZE-LR-DN30-160.md + issue #72 |
| KAB-0886/M05 | Basic-Sichtbarkeitsoption sowie `DN30–160`-Editorsteuerung in Browseransicht überprüft. | tests/browser-visual.js |
| KAB-0886/M07 | Basic Schacht zeigt LR-Öffnungen getrennt von Kabelansicht. | tests/browser-visual.js |
| KAB-0886/M08 | Profi-Modus: `holeDn`-Feld mit min=30, max=160, step=1 geprüft. | tests/browser-visual.js |
| KAB-0886/M13 | Realer Chromium-Lauf prüft WebGL-Pixel und Browser-Fehler nach DN-Demo. | GitHub-Visual-Workflow + tests/browser-visual.js |
| KAB-0887/M08 | DN-Test in tatsächlicher npm-Suite mit Pass/Fail-Ausgabe ausgeführt. | GitHub-Pages-Testlauf |
| KAB-0887/M11 | Chromium-Browserlauf des fiktiven DN-Schachts gegen GitHub-Commit geprüft. | GitHub-Visual-Workflow |
| KAB-0888/M05 | DN-Demo als eigene Ansichtsoption in echtem Browser kontrolliert. | tests/browser-visual.js |
| KAB-0888/M07 | Basic zeigt Wandöffnungen statt durchgehender farbiger Kabelrouten. | tests/browser-visual.js |
| KAB-0888/M08 | Profi-Ansicht und editierbare Nennweiten geprüft, ohne PDF-Bestandsdaten zu mutieren. | tests/browser-visual.js |
| KAB-0888/M13 | Echter Chromium-3D-Test mit Konsolenfehlersammlung und Farb-Pixel-Prüfung. | GitHub-Visual-Workflow |
| KAB-0888/M15 | Screenshots/Browser-Testbeleg mit exakter CI-Version im Parent verlinkt. | TASKS/08-BRO-KURZE-LR-DN30-160.md + Visual-Workflow |
| KAB-0877/M05 | Betonwand mit Aussparungen im echten WebGL-Ansichtszustand geprüft. | tests/browser-visual.js + tests/concrete-wall.test.js |
| KAB-0877/M07 | Basic-Betonblock zeigt LR-Öffnungen und unterdrückt lange Kabelwege. | tests/browser-visual.js |
| KAB-0877/M08 | Profi-Modus kontrolliert, gespeicherte Original-Geometrie bleibt unberührt. | tests/browser-visual.js |
| KAB-0877/M13 | Realer Chromium-Betonwand-Sichttest mit Browser-Konsolenprüfung. | GitHub-Visual-Workflow |

## Schwere Aufgabe KAB-0888

In `tests/browser-visual.js` wird die fiktive DN-30/50/110/160-Demo mit echtem WebGL, 2D-/3D-Bildpaar, Kabel-/Schachtansicht, Profi-DN-Feldern, 12 Nennweiten, JSON-Roundtrip und Wiederherstellung des originalen 91-LR-PDF-Schachts abgenommen. Die Screenshot-Dateien werden als Actions-Artefakte bereitgestellt.

**Abnahmegrenzen:** Keine erfundenen Innen-/Außen-/Lochdurchmesser, keine echten Rohrlängenänderungen, kein PDF-OCR. Bros optische Freigabe bleibt offen. Browser-bezogene Mikro-IDs dürfen nur nach grünem Visual-Workflow als erledigt gelten.
