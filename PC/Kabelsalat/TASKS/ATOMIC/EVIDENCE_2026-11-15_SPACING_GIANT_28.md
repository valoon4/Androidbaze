# KAB-0051 bis KAB-0053 · Rohrabstands-Editor 2.0 · 15.11.2026

**Ergebnis: technisch vollständig abgenommen (synthetische/eigene Schächte).** Keine Bau-, Mess- oder Normfreigabe, keine Veränderung importierter Original-PDF-/HTML-Schächte.

| Prüfstufe | Beleg |
|---|---|
| **Echter Chromium-/WebGL-Gesamtlauf** | [GitHub Actions #43712272472](https://github.com/valoon4/Androidbaze/actions/runs/43712272472), **success** für Commit [3ac5fcb](https://github.com/valoon4/Androidbaze/commit/3ac5fcbced0db69b0c78509417bfe1af90634494) |
| **Chrome End-to-End, Abstand** | `tests/browser-spacing.js` → CI-Log: `KAB-0051/52/53 BROWSER OK: preview, 2D/3D state, explicit apply, cable-ID invariance, undo/redo and PDF protection` |
| **Screenshot-Artefakt** | [#14160926940](https://github.com/valoon4/Androidbaze/actions/runs/43712272472/artifacts/14160926940), 21.489.212 Bytes; `spacing-before-after-preview.png` und `spacing-after-apply-3d.png` |
| **Browser-Webseite/npm test** | [GitHub Pages #43712272539](https://github.com/valoon4/Androidbaze/actions/runs/43712272539), **success** |
| **Windows EXE/Offline** | [GitHub Actions #43712272507](https://github.com/valoon4/Androidbaze/actions/runs/43712272507), **success** |
| **Gezielte Regressionssuite** | `tests/spacing-adjust.test.js`, 71 Assertion-Prüfungen der aktuellen GitHub-JavaScriptdateien zusätzlich direkt im V8-Isolat: **PASS** |

## KAB-0051 · verlässlicher Regler mit Undo/Redo

`spacing-adjust.js` erzeugt eine **nicht verändernde** mm/U/V-Vorschau. In der App erscheinen Werte für lichte Rohrabstände und Randabstände vorher/nachher sowie gestrichelte 2D-Geisterkreise. Übernehmen ist eine bewusst bestätigte Undo/Redo-Operation. Zielwerteingaben verwerfen eine veraltete Vorschau. Identitäts-/Quell-Snapshots verhindern stale applies und doppelte Rohr-IDs.

## KAB-0052 · unveränderte Kabelanschlüsse

Die einzigen übernommenen Datenfelder sind `hole.u` und `hole.v`. Rohr-IDs, DN, verifizierte/physische Maße, Kabel-IDs, beide Kabelendpunkte, Routen und andere Schachtwände bleiben byteidentisch. Änderungen an Kabeltopologie zwischen Vorschau und Übernahme invalidieren den Plan. Auch **korrupte/gefälschte Vorschauen** werden atomar vor der ersten Koordinatenänderung verworfen; separate Negativtests decken falsche IDs, NaN/Infinity, negative/off-wall Koordinaten, falsche Quellenpositionen, duplizierte Änderungen und unerfüllte Mindestabstände ab.

## KAB-0053 · vorher/nachher sichtbar und vergleichbar

Schematisch: Abstand und Rand `Vorher → Nachher`, Anzahl bewegter LR, 2D-Overlay vor Übernahme und dokumentierte 3D-Ansicht nach Übernahme. Eigenständige DN75/160-SW2-Zweitfixture ergänzt DN60/110/50-SW1, positiver `0 mm`-Grenzfall, gezielte Kollision/Negativfälle und ausgeführte Browservergleiche.

## Einzeln abgenommene bisher offene Mikro-IDs (28)

| Parent | Mikro-IDs | Nachweis |
|---|---|---|
| **KAB-0051** | M04, M05, M06, M07, M09, M10, M11, M14, M16 | Gültige Geometrie, 0-mm-Grenze, atomarer Regler, SW2-Zweitwand, Kollision/Negativfälle, 2D-/3D-Chromium, erfolgreiche Regression, Code-/CI-Links |
| **KAB-0052** | M04, M05, M06, M07, M09, M10, M11, M14, M16, M17 | Zusätzliche Kabel-ID-/Link- und Routenintegrität, stale Links, keine partiellen Mutationen, CI/2D/3D, negativen Nebeneffekt isoliert |
| **KAB-0053** | M04, M05, M06, M07, M09, M10, M11, M14, M16 | metrischer Vorher-/Nachher-Vergleich, 2D-Overlay, 3D-Bild, Zweitwand, Grenz-/Negativtest, CI |

**Technische Code-Commits:** [atomare Validierung / Kabelsnapshot](https://github.com/valoon4/Androidbaze/commit/79b265f725363af2d733703b1ad0fade3d2d46e7) · [UX bei Eingabeänderung](https://github.com/valoon4/Androidbaze/commit/c36ea8f58f74bb365aab59e4d1066e8645e274a9) · [negativ getestete Korruption](https://github.com/valoon4/Androidbaze/commit/04833c98f543e2220ed06c9a795afdc5a36dafd4) · [Chromium-Regression](https://github.com/valoon4/Androidbaze/commit/8e60fce938f40854b2de2524f57bf5be4115f383) · [SW2-Zweitfixture](https://github.com/valoon4/Androidbaze/commit/3ac5fcbced0db69b0c78509417bfe1af90634494).

**Grenze:** Berechnete Abstände stammen ausschließlich aus Modellwerten. PDF-/HTML-Importprojekte werden strikt geschützt; niemand darf die 3D-Darstellung als vermessene Bauausführung, DIN-/AVL0116-Prüfung oder freigegebene Rohrversetzung ausgeben.
