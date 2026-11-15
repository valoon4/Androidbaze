# Kabelsalat · 100er-Raid · Welle 3: letzte 25 technische Parent-Abnahmen

15.11.2026 · **Vorher 175/888 → danach 200/888**. Es geht um die technische Umsetzung und Regressionen, nicht die externe Fach-/Brandschutz- oder Vermessungsfreigabe.

## Tatsächlich geprüfter Code

- `tests/model-regression-giant.test.js`: **323 gezielte Assertions im tatsächlichen GitHub-Code** gegen `reference-data.js`, `examples.js`, `schematic-positions.js`, `pdf-geometry.js`, `quality.js` und `construction-v2.js`: 91 PDF-V2-Rohre, Einzelbelegung, sourceU/V-Bytewerte, 62-%-Anzeige ohne Modelländerung, SW1–SW4-Normalen, gerade/geneigte/Bézier-Wege, Null-Endpunkte, OLA/MS-Platten, Fremdrohrblocker und erneute Altversionsmigration. Der Ursprungsdatensatz bleibt unverändert.
- `tests/project-history-giant.test.js`: **35 gezielte Assertions gegen den realen `validProject`-, `change`- und `undoRedo`-Code aus `app.js`**, mit vier fiktiven Schächten und echten Netzmodul-Snapshots; neue Leitung, Leitungspunkt und Platte werden rückgängig und wiederhergestellt, eine neue Änderung löscht redo. Ein neu erstelltes, noch nicht normalisiertes Rohr erhält beim JSON-Reimport Default-Quellfelder; verglichen wird deshalb das **kanonische v5-Modell**, nicht behauptete identische Rohbytes vor der Normalisierung.
- Formatbeschreibung und nachprüfbarer Quell-/Migrationsweg: [PROJECT_FORMAT_V5.md](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/TASKS/PROJECT_FORMAT_V5.md). Beide Tests sind in [package.json](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/package.json) in `npm test` eingehängt.

## Einzelbelege

| Parent-ID | Konkreter positiver/negativer Fall | Testquelle |
|---|---|---|
| KAB-0415 | v5-Datenvertrag und Migration mit Quellgrenzen dokumentiert | [History-Test](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/project-history-giant.test.js) |
| KAB-0419 | JSON-Roundtrip v5 stabil, deterministische Feldreihenfolge | [History-Test](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/project-history-giant.test.js) |
| KAB-0420 | Vier-Schacht-Netz und lokale Kabel-/Rohr-Snapshots erhalten | [History-Test](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/project-history-giant.test.js) |
| KAB-0421 | Rohr-IDs in allen Wänden stabil nach Reimport | [History-Test](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/project-history-giant.test.js) |
| KAB-0422 | Kabel-IDs auch nach Reimport und Netz-Snapshot stabil | [History-Test](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/project-history-giant.test.js) |
| KAB-0425 | Rohrhinzufügen reversibel durch echten App-change/undoRedo | [History-Test](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/project-history-giant.test.js) |
| KAB-0426 | Wegpunktverschiebung mit tatsächlicher Ausgangsroute rücknehmbar | [History-Test](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/project-history-giant.test.js) |
| KAB-0427 | Plattendicke reversibel, kein fremder Zustand verändert | [History-Test](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/project-history-giant.test.js) |
| KAB-0428 | Redo stellt kanonisches Modell inkl. Snapshot wieder her | [History-Test](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/project-history-giant.test.js) |
| KAB-0429 | Neue Änderung nach Undo löscht verlassenen Redo-Zweig | [History-Test](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/project-history-giant.test.js) |
| KAB-0432 | Zwei benannte Varianten differenzieren Geometrie und Kabelkontext | [History-Test](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/project-history-giant.test.js) |
| KAB-0653 | PDF-V2 SW4 enthält exakt 23 Rohrmarker | [Modell-Test](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/model-regression-giant.test.js) |
| KAB-0654 | Demo-Verdichtung verändert keine Quellbelegung | [Modell-Test](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/model-regression-giant.test.js) |
| KAB-0655 | sourceU/sourceV je Rohr bleibt durch JSON-Roundtrip identisch | [Modell-Test](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/model-regression-giant.test.js) |
| KAB-0656 | Gegenwand-Kabel hat zwei Punktgeradensegmente ohne Knick | [Modell-Test](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/model-regression-giant.test.js) |
| KAB-0657 | Höhenversetzte Gegenwandkabel bleiben geneigte Geraden | [Modell-Test](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/model-regression-giant.test.js) |
| KAB-0658 | Seitlicher Wechsel Bézier-Kontrollpunkte und Rmin-Warnstatus | [Modell-Test](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/model-regression-giant.test.js) |
| KAB-0659 | Nicht belegter Endpunkt erzeugt keinen erfundenen Kabelweg | [Modell-Test](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/model-regression-giant.test.js) |
| KAB-0660 | Vier Wandnormalen und inverse U-Transformation geprüft | [Modell-Test](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/model-regression-giant.test.js) |
| KAB-0661 | Kollisionsbeispiel nach 62-%-Vorschauverdichtung ohne Modellmutation | [Modell-Test](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/model-regression-giant.test.js) |
| KAB-0662 | OLA-5-Stapelauflager an passender Höhenlage | [Modell-Test](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/model-regression-giant.test.js) |
| KAB-0663 | MS-4-Stützen bis Kanalboden | [Modell-Test](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/model-regression-giant.test.js) |
| KAB-0664 | Beide perforierten Endplatten enthalten Kabelöffnungen | [Modell-Test](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/model-regression-giant.test.js) |
| KAB-0665 | Fremdrohr-Blocker verhindert schematische Kanalfreigabe | [Modell-Test](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/model-regression-giant.test.js) |
| KAB-0666 | PDF-V2-Altversionsmigration deterministisch/idempotent; manuelle Änderungen geschützt | [Modell-Test](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/model-regression-giant.test.js) |

**Explizite Grenzen:** KAB-0416/0417/0418 (Altversionen v1/v3/v4 *verlustfrei* mit fremden Quellen importieren), KAB-0423/0424 (ID-Kollisionen), KAB-0431 (automatisches Migrationsbackup) bleiben offen. Keine neuen realen Browser-/CI-Erfolge behauptet: Die gezielten JavaScript-Läufe sind überprüft, aktueller vollständiger Chromium-/Windows-Workflow noch nicht unabhängig bestätigt. Parent-Checkboxen und 13.012 Mikro-Tasks sind unterschiedliche Nachweisräume; kein massenhaftes automatisches Mikro-Abhaken.
