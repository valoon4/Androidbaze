# Kabelsalat · Sprint #3 · 50 nachgewiesene Mikro-Schritte

**Datum:** 14.11.2026 · **Umfang:** 50 voneinander getrennte Code-/Quellprüfungen, neue synthetische Regressionstests und fokussierte Dokumentation. **Keine** behauptete Originalplan-Verifikation, Browser-Sichtabnahme oder Fachfreigabe.

**Code-Änderungen:** [`tests/sprint-50-2026-11-14.test.js`](../../tests/sprint-50-2026-11-14.test.js) ergänzt Plantext-/Foto-Regressionen; `package.json` bindet sie in `npm test` ein. **Parent-Änderung:** [`07-BRO-BETON-ROHR-DM.md`](../07-BRO-BETON-ROHR-DM.md) enthält kompakte KAB-0879-Prüftabelle. **Tests werden nicht als bestanden behauptet, solange CI nicht grün ist.**

| Mikro-ID | Erledigter konkreter Schritt |
|---|---|
| KAB-0879/M01 | Dokumentationsziel: proportional gerenderte Öffnungen, Quellgrenzen und Abnahme für Bro definiert. |
| KAB-0879/M02 | Kanonische KAB-0879-Stelle in `TASKS/07-BRO-BETON-ROHR-DM.md` ergänzt; ältere Kontextpassage bleibt erhalten. |
| KAB-0879/M03 | Wortlaut abgeglichen: 60/110/116 sind Beispiel-mm-Werte, nicht vermessene Bestandsdaten; Nennweite und Öffnungs-Ø getrennt. |
| KAB-0879/M04 | Kurze Soll-/Code-/Test-/Offen-Tabelle in der Parentdatei formuliert. |
| KAB-0879/M05 | Direkte, prüfbare Links auf `concrete-wall.js` und `tests/concrete-wall.test.js` im Parent ergänzt. |
| KAB-0879/M06 | Bro-Sichtabnahme, Hersteller-/Originalmaßdaten und Fachfreigabe ausdrücklich als noch offen dokumentiert. |
| KAB-0879/M07 | Kompakttabelle mit fünf relevanten Angaben statt langem Audittext erstellt. |
| KAB-0879/M08 | Relative Quellpfade `../concrete-wall.js` und `../tests/concrete-wall.test.js` ab `TASKS/` auf existierende Repo-Dateien aufgelöst. |
| KAB-0879/M09 | Parent-DoD gegengeprüft: Größenrelation, keine Fingierung realer Maße, Originalkoordinatenschutz, Sichtabnahme als Blocker. |
| KAB-0879/M10 | KAB-0879 als offener Parent innerhalb 888er Trackers belassen; Mikro-ID nicht als zusätzlichen Parent gezählt. |
| KAB-0879/M11 | Fokussierter Commit `d0611e12525652bbc0763d7968490d34840efcdc` zur KAB-0879-Dokumentation gespeichert. |
| KAB-0879/M12 | mm-Einheit und DN vs. `dimensionSpec.innerMm/outerMm/openingMm` im Parent verifiziert. |
| KAB-0881/M01 | `parsePlanText(text,meta)` dokumentiert: maximal 100.000 Eingabezeichen, 1.500 Zeilen, 800 Zeichen/Zeile, 600 Kandidaten; Ausgabefelder `line,source,pipeId,wall,loosePipeId,label,candidateMm,status,confidence,reason`. |
| KAB-0881/M02 | Synthetischer Fixture-Test `SW1/T1/LR05 DN 50` erwartet Kandidat 50 und ID `SW1_T1_LR05`. |
| KAB-0881/M03 | Test `SW2/T3/LR02 DN 110` erwartet 110 und eindeutige LR-ID. |
| KAB-0881/M04 | Test `SW3/T2/LR09 DM 116` prüft Kandidat 116, bewahrtes `DM`-Label und Status `candidate` statt geprüftem Außenmaß. |
| KAB-0881/M05 | Test `SW4/T1/LR01 DN 30` deckt untere Bro-DN-Grenze ab. |
| KAB-0881/M06 | Test `SW4/T1/LR02 DN 160` deckt obere Bro-DN-Grenze ab. |
| KAB-0881/M07 | Test ohne erkannte LR-ID erwartet ausdrücklich `pipeId:null`. |
| KAB-0881/M09 | Test mit Kommentarzeile überprüft `line:2` und Originalquellenname `synthetic-plan.txt`. |
| KAB-0881/M10 | Test konserviert JSON-Snapshot einer bestehenden Öffnung mit `dn:75` trotz anderslautendem Parse-Fund. |
| KAB-0881/M13 | Test sichert: Parser-Kandidaten haben `status:'candidate'` und nie ungeprüft `reviewed`. |
| KAB-0881/M15 | Statischer UI-Regressionstest sichert Hinweis, dass PDF-Binärtext/Bildtext nicht automatisch extrahiert wird. |
| KAB-0881/M18 | Fehlender PDF-Textlayer als eigenständiger blockierter Originaldokument-Fall beschrieben: keine OCR/kein stiller Erfolg; ohne echte PDF-Datei keine positive Akzeptanz. |
| KAB-0881/M19 | TXT-Datei und eingefügter Plantext von PDF-Binärextraktion abgegrenzt; Quellzuordnung `planTextFile` / `planDimensionText` im UI statisch geprüft. |
| KAB-0881/M24 | Zwei Fixtures für `DN 110` und `DM 110` sichern unveränderte Bezeichner trotz gleichem Kandidaten-mm. |
| KAB-0881/M26 | Negativtest: `SW1/T1/LR05` auf Zeile 1 und `DN 50` auf Zeile 2 dürfen nicht automatisch zu einer Rohr-ID zusammengesetzt werden. |
| KAB-0881/M35 | Parser-Abschluss-Assertion: alle synthetisch erkannten Werte ausschließlich `candidate`, nicht freigegeben. |
| KAB-0881/M40 | Externer Blocker präzisiert: mindestens eine echte fachlich verwendbare, beschriftete PDF mit Dokumentversion/Seite und LR-IDs fehlt für die PDF-Binär-/Textlayer-Validierung. |
| KAB-0881/M44 | Rechte-/Fähigkeitsgrenze dokumentiert: PDF-Import benötigt berechtigte Originalplandatei, keinen behaupteten KI-/OCR-Fertigstand; kein Kopieren geschützter Plantexte in Fixtures. |
| KAB-0882/M01 | `photo-candidates.js`: `detect(pixels,width,height,options)` liefert Pixelkreis-Kandidaten (max. 4 MP Analysefläche), `calibrate(candidates,index,referenceMm)` berechnet mm aus explizitem Referenzkreis; Qualität ist heuristisch, nicht trainierte KI. |
| KAB-0882/M05 | Synthetisches 120×120-Pixelbild mit zwei dunklen Kreisen getestet; unkallibrierte Ausgabe enthält `diameterPx` und kein `candidateMm`. |
| KAB-0882/M07 | Negativassertion für Kalibrierreferenz 0 mm. |
| KAB-0882/M08 | Negativassertion für `NaN` und nicht-numerisches `text`-Referenzmaß. |
| KAB-0882/M09 | Negativassertion für Referenzindex 99 bei zwei gefundenen Kreisen. |
| KAB-0882/M11 | Statischer Upload-Regressionscheck für exakte MIME-Whitelist `image/png`, `image/jpeg`, `image/webp`. |
| KAB-0882/M12 | Statischer Upload-Regressionscheck für `file.size>10000000` (dezimal 10 MB). |
| KAB-0882/M14 | Statische Anwendungspfad-Assertion prüft `applyPhotoDimension` mit `status:'candidate'`; Test der Kalibrierung verlangt ausschließlich `candidate`. |
| KAB-0882/M15 | Statische Anwendungspfad-Assertion prüft Quellenpräfix `Foto: `, Dateiname und 1-basierte Kreisnummer. |
| KAB-0882/M16 | UI-Hinweis `heuristisch, keine KI/OCR` und Modulerklärung über Kandidaten statt verifizierte Maße geprüft. |
| KAB-0882/M18 | Benötigte Originalfixtures erfasst: kalibriertes Foto mit nachweislichem Referenz-Ø, dunkel/hell, Perspektive, schrägen Kreisöffnungen und unterschiedlichen LR-Größen; nicht vorhanden = Blocker. |
| KAB-0882/M32 | Negativassertionen für NaN/Null-Pixelreferenz; kalibrierte Testkandidaten müssen endliche mm-Werte haben, keine NaN-Persistierung. |
| KAB-0882/M47 | Externe Abnahmevoraussetzung dokumentiert: reales Foto mit belegtem Maßstab/Referenzkreis, Rohr-ID-Zuordnung und Bildrechten; keine synthetische Fixture als echte Quelle ausgegeben. |
| KAB-0882/M49 | Bro-Wunsch gegen Stand abgeglichen: offline heuristische Kreiskandidaten, nur relativ ohne Maßstab, mit Kalibrierung `candidate`, keine trainierte KI, keine sichere Rohrtypklassifikation. |
| KAB-0048/M01 | Kollisionspfad lokalisiert: `quality.auditWallOpenings(project,projector)` → `pairs/overlaps/edgeCrossings/invalidIds`; Daten aus `project.dims`, Wandlöchern und optionalem 62%-Ansichtsprojektor. |
| KAB-0048/M02 | Einheiten festgehalten: `project.dims` in mm, `u/v` dimensionslose normierte 0..1-Koordinaten, `xMm/yMm` berechnete mm; renderseitiges 3D ist in Metern. |
| KAB-0048/M03 | Reproduzierbarer synthetischer Fall `SW1`: Wand 1000×1000 mm, DN60 bei u=.4/v=.5, DN110 bei u=.5/v=.5; Abstand der Mittelpunkte 100 mm, lichter Abstand 15 mm, erwartet keine Überschneidung. |
| KAB-0048/M08 | Bestehender Regressionstest `tests/pdf-wall-clearance.test.js` prüft read-only Auswertung über `JSON.stringify(project)===original`; keine Kabel-/Rohr-ID-Mutation in diesem Pfad. |
| KAB-0048/M13 | Prüfwert 100 - 30 - 55 = 15 mm, unverrundet (Berechnung in `quality.js`); Renderer nutzt getrennt Meterdarstellung, ohne negative Abstände zu verbergen. |
| KAB-0048/M15 | Geltungsgrenze explizit: berechnete lichte Distanz ist schematische Modellgeometrie, keine Vermessung und keine DIN-/Brandschutzfreigabe; Browser-Warnanzeige bleibt separat offen. |

## Offene externe Fälle

- **KAB-0881:** Original-PDF mit erlaubtem Zugriff, eindeutiger Dokumentversion, PDF-Seite und sichtbaren Rohr-IDs; eingescannte Seiten ohne Textlayer müssen eigens/OCR-gestützt geprüft werden. Bis dahin keine behauptete Binär-PDF-Erkennung.
- **KAB-0882:** Echtes Foto plus Referenzkreis mit belegtem Durchmesser, Bildquelle/-rechte, Rohr-ID- bzw. Wandzuordnung, Perspektive und Testfälle mit mehreren Rohrgrößen. Keine automatische Fachfreigabe.
- **KAB-0879 und KAB-0048:** Erst nach Bros Sichtprüfung und tatsächlichem Aufmaß kann eine bauliche Eignung beurteilt werden; mm-Abstände sind gegenwärtig Modellwerte.

## Validierung

- Syntaxprüfung der neuen Testdatei wurde durchgeführt; automatisierte Laufzeitprüfungen/CI sind separat nachzusehen.
- Mikro-IDs werden nur dann in `ATOMIC/COMPLETED.csv` aufgenommen, wenn ihr jeweils beschriebenes Fertig-Kriterium nachweisbar erfüllt ist.
