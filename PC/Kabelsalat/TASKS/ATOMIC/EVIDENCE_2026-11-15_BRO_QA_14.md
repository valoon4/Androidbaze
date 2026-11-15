# Bro-High-Priority: 14 verifizierte Mikroschritte (15.11.2026)

**Stand und Abnahmekriterium:** Die GitHub-Pages-CI auf [Run #43706636908](https://github.com/valoon4/Androidbaze/actions/runs/43706636908) für [Commit 173023fca3010d7edd21e48e18e27050f9b9b5a7](https://github.com/valoon4/Androidbaze/commit/173023fca3010d7edd21e48e18e27050f9b9b5a7) ist **success**. Der Schritt „Statische Browserdateien prüfen“ lief mit `npm test` erfolgreich. Das Joblog enthält u. a. `OK KAB-0877: concrete wall through-holes, two materials, 48/78/91 source pipes unchanged` und `OK: independent DN/inner/outer/bore/candidate dimensions and sourced PDF-label proposals`.

| Mikro-IDs | Konkreter Nachweis / Grenze |
|---|---|
| KAB-0881/M30 | `inspectPlanPages` gibt `manual-review` und `missing-text-layer` mit eindeutigem OCR-/manuellen Hinweis statt DN-Zahlen für eine synthetisch gescannte Seite zurück. **Keine OCR implementiert.** |
| KAB-0881/M37 | Fehlerhafte Seiten-Batches werden über den Read-only-Parser ohne Mutation der übergebenen Plan-Seiten verarbeitet; Test-Snapshot unverändert. **Kein Binär-PDF-Import behauptet.** |
| KAB-0881/M38 | Harte Standard- und Konfigurationslimits für Seiten/Zeichen, getrennte Fehlerausgabe; Positiv-/Negativfälle in `tests/pipe-dimensions.test.js`. |
| KAB-0881/M39 | JSON-Roundtrip für Seiten-/Zeilen-/Versions-/Quellenmetadaten der Kandidaten, ohne das Rohr-Quellprojekt zu verändern. |
| KAB-0881/M43 | Synthetische defekte Rotations-/Textlayerseite produziert isolierbaren Fehlereintrag `invalid-pdf-text` mit prüfbarer Ursache. |
| KAB-0884/M05 | Zweite unabhängige synthetische Geometrieprobe DN30/50/110/160 ergänzt (neben 60/110/116). |
| KAB-0884/M06 | Nullradius und fehlende Quellen-/Prüferangaben werden nicht als reale / reviewed Rohrdaten durchgewunken. |
| KAB-0884/M08 | Lokaler Geometrie-Einzeltest im V8 ausgeführt (35 Assertions PASS); derselbe `tests/concrete-wall.test.js` in GitHub-CI nachweislich bestanden. |
| KAB-0884/M09 | Zuvor festgestellte Probleme in neuen Parser-/UI-Testfixtures (Escaping, VM-Context-Kollision) gezielt korrigiert; CI bestätigt aktuellen regressionsfreien Stand. Keine ungeprüften Tests künstlich grün gesetzt. |
| KAB-0884/M11 | CI-Lauf exakt mit obigem Commit abgeglichen, Pages/Node-Tests success. **Separater Chromium-Workflow wird nicht als bestanden ausgegeben.** |
| KAB-0877/M05 | Material-/Ansichtsoptionen in `tests/concrete-wall.test.js`: zwei Materialien, deckend/transparent, Dunkelheit/Struktur getrennt geprüft; beide Testpfade grün. |
| KAB-0877/M06 | Proportionale DN60/110/116-Geometrie in 3D-Öffnungen und explizites abweichendes Lochmaß getestet. |
| KAB-0877/M11 | Zweite Originalschacht-Referenz und PDF-V2-Wand mit 48/78/91 Originalöffnungen durch die Tests unverändert erhalten. |
| KAB-0887/M09 | Synthetische DN30–160-Demo, Quelltrennung und Dimensionstests sind im aktuellen CI erfolgreich; kein nicht behobener **bekannter Unit-Testfehler** dieses Demo-Bereichs. |

**Technische Änderungen:** [PDF-Fehlerstatus](https://github.com/valoon4/Androidbaze/commit/4694e0c02ce3826b4151ec0887ffcea9af7c8be1) · [PDF-Negativtests](https://github.com/valoon4/Androidbaze/commit/a9b2c9f2a81e28123a5a301377ddf24ab053c760) · [zweite Beton-/DN-Fixture](https://github.com/valoon4/Androidbaze/commit/173023fca3010d7edd21e48e18e27050f9b9b5a7).

**Bleibt ausdrücklich offen:** Bro-Sichtabnahme, reale geometrische Quellen, trainierte PDF-/Foto-OCR-KI, vollständiger echter Browser-Visualtest sowie alle anderen nicht einzeln abgenommenen Mikro-IDs. Parent-Tasks bleiben offen, bis sämtliche Endbedingungen erfüllt sind.