# Kabelsalat · 30 belegte Mikro-Prüfungen · 14.11.2026

**Version:** 2.0.7-beta.1. Das sind 30 abgeschlossene atomare Prüf-, Quellen- und Testschritte, nicht 30 komplett neue Funktionen. **1 große zusätzliche Entwicklungsaufgabe:** KAB-0888 – echte DN-WebGL-/2D-/Browserregression als Code ergänzt, finale Chromium-Bestätigung separat in CI. Die 18 browserabhängigen Punkte aus der *vorläufigen* Sprintplanung bleiben bis zum bestandenen Visual-Lauf ausdrücklich offen.

**Laufender Testnachweis:** [Grüner GitHub-Pages-/npm-test-Workflow](https://github.com/valoon4/Androidbaze/actions/runs/43632177462). GitHub-Release [2.0.7-beta.1](https://github.com/valoon4/Androidbaze/releases/tag/kabelsalat-latest) enthält Windows-EXE, Browser-ZIP und Browser-Einzeldatei.

| Mikro-ID | Konkret verifizierte Leistung |
|---|---|
| KAB-0881/M08 | Synthetische Doppelbeschriftung derselben Rohr-ID bleibt 2 getrennte Kandidaten (50/110), keine automatische Datenmutation. |
| KAB-0881/M11 | Fremde Kandidaten-ID wird beim Übernehmen in `app.js` explizit abgewiesen; Regex-Assertion in neuem Unit-Test. |
| KAB-0881/M12 | Ohne erkennbare Rohr-ID fordert `app.js` ausdrücklich `confirm()` vor Zuweisung. |
| KAB-0881/M14 | Kandidat mm/Quelle/Zeile/Status bleibt per JSON-Roundtrip erhalten; Rohr-DN bleibt unverändert. |
| KAB-0881/M16 | Plantext-Sonderfälle als `sprint-30-release-2026-11-14.test.js` in `npm test` integriert; grüner Pages-CI. |
| KAB-0882/M02 | Leerbild-Fixure ergibt 0 Kreiskandidaten. |
| KAB-0882/M03 | Dunkelkreis-Fixure erzeugt 1 Kandidat samt Pixel-Durchmesser. |
| KAB-0882/M04 | Zwei verschieden große Kreise erzeugen 2 sortierte Kandidaten. |
| KAB-0882/M06 | Kalibrierung erzeugt `candidateMm=110`, bleibt `candidate`, nie freigegeben. |
| KAB-0882/M10 | Ein >4-MP-Bild wird vor Pixelanalyse per Fehler abgewiesen. |
| KAB-0882/M13 | Stark verzerrte Ellipse wird nicht als sicherer Kreis erkannt. |
| KAB-0882/M17 | Synthetische Fotoregressionen im erfolgreichen `npm test`-GitHub-CI ausgeführt. |
| KAB-0885/M08 | Abnahmefrage „Kabelwege oder reale Rohrtiefe?“ mit Issue #72 im Parent festgehalten, Abnahme offen. |
| KAB-0887/M08 | Fiktiven DN-30–160-Einzeltest tatsächlich im grünen `npm test`-Workflow ausgeführt. |
| KAB-0887/M11 | CI-Status mit tatsächlich erfolgreichem Pages-Testlauf und dessen SHA abgeglichen; kein Browsererfolg behauptet. |
| KAB-0881/M33 | Bei fremder Rohr-ID verweigert `applyPlanDimension` den Transfer (Fehlzuordnung bleibt untersagt). |
| KAB-0881/M34 | Ein ohne Rohr-ID erkannter Kandidat darf nur nach expliziter Rückfrage einem Loch zugeordnet werden. |
| KAB-0881/M41 | `pdf-u028`-Regressionen behalten 4 Wände mit 29/13/26/23 = 91 LR und Quellen-JSON unverändert. |
| KAB-0881/M45 | Unit-/Parser-Regressionen sind in `npm test` verdrahtet und im Pages-CI grün gelaufen. |
| KAB-0881/M46 | Positive Label-Fälle DN30/50/110/160, DM116 und negative ohne ID / Textlayer-Grenze dokumentiert. |
| KAB-0882/M34 | Foto-Dateiwechsel berechnet `photoAnalysis=photoCandidates.detect(...)` neu; Fehlerzweig setzt `photoAnalysis=null` statt alte Maße fortzuführen. |
| KAB-0882/M36 | UI zeigt explizit `Perspektivenfehler möglich.` und Kandidaten bleiben nicht freigegeben. |
| KAB-0882/M37 | Übernahme eines Foto-Kandidaten speichert `Foto: Dateiname (Kreiskandidat n)` als Quelle; kein ungeprüfter Rohrtyp. |
| KAB-0882/M45 | Dropdown-Auswahl nutzt Kandidatenindex und bricht bei fehlendem/unkalibriertem Kandidaten ab. |
| KAB-0878/M08 | Bro-Farbabnahme als externer Blocker im Parent notiert; keine Wartezeit als erledigte Implementierung. |
| KAB-0878/M10 | Reviewfrage samt Link zu Testversion und visuellem Workflow im Parent hinterlegt; noch kein positiver Reviewstatus. |
| KAB-0879/M13 | Negativtest für verbotenen Effekt: Größe nur schematisch, Referenzpositionen und Rohrdaten werden durch Rendern nicht geändert. |
| KAB-0886/M07 | Basic-Schacht: `drawCable`-Filter entfernt Kabel aus LR-Übersicht; bestehende automatisierte Assertion im DN-Test. |
| KAB-0888/M07 | Dieselbe Basic-Schacht-Sichtbarkeitsregel ist über `bro-dn-range.test.js` separat gesichert. |
| KAB-0048/M16 | KAB-0048-Code- und Testbeleg im kanonischen P0-Parent verlinkt; schematische Geltungsgrenze dokumentiert. |

**Quellen:** [neue Plan-/Foto-Tests](../../tests/sprint-30-release-2026-11-14.test.js), [bisherige DN-Tests](../../tests/bro-dn-range.test.js), [Foto-Analyse](../../photo-candidates.js), [Applikationslogik](../../app.js), [Beton-Geometrie-Test](../../tests/concrete-wall.test.js), [P0-Parent](../01-P0-QUELLE-UND-PITCH.md), [Rohr-/DN-Parent](../08-BRO-KURZE-LR-DN30-160.md), [Beton-Parent](../07-BRO-BETON-ROHR-DM.md).

**Nicht freigegeben:** physische Rohrtiefe/Öffnungsmaße, echte Bild-/PDF-AI-Erkennung, Bros Sichtabnahme und der Browser-Lauf der großen KAB-0888-Regressionsaufgabe, solange er nicht grün ist.
