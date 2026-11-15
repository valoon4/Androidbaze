# Kabelsalat · 100er-Raid · Welle 2: Netz und lokale Kabelsegmente

**15.11.2026** · vor dieser Welle 147/888 Parent-Abnahmen · **28 zusätzliche** technische Gates · nach dieser Welle **175/888**.

**Befund:** `tests/network-parent-giant.test.js` gegen den wirklich via verbundene GitHub-App abgerufenen produktiven Code `network.js`, `network-route-planner.js` und `network-world-route.js` in isolierter JavaScript/V8-Runtime erfolgreich geprüft: **55 Assertions**. Hierzu zählen fünf explizit fiktive Schächte mit variierenden Wänden, 30-Schacht-Graphsuchprobe, Ring, unverbundene Knoten, per Link ungeklärte physische Übergänge, fehlender Kabel-/Rohrbezug, Quellenstatus und unmodifizierte Ursprungsdaten. Zusätzliche UI-Schnittstellen in `app.js`/`index.html` gegen tatsächliche Dateien geprüft. `npm test` bindet den neuen Test ein. **Kein unabhängig bestätigter GitHub Actions / Chromium Lauf für diesen Commit**; keine Norm-/Brandschutzfreigabe. 

**Sicherheitsgrenzen:** Schematische Netzverknüpfung ist kein physischer Kabelübergang; Status hat nur `unverified` und `needs-evidence`. Import oder Vermessung von Originalschächten nicht behauptet. 0344 (bestehenden Schacht importieren), 0347 (gerichtete Links), 0350 (Parallel-Links), 0356 (30-Schacht-UI-Leistung), 0357 (über 30 Schächte) sind **explizit nicht abgeschlossen**.

| Parent | Konkreter belegter Funktionsfall | Regression |
|---|---|---|
| KAB-0343 | Neuer leerer Schacht per netNewShaft, neue stabile ID und Null-Rohre | [Netz-Regressionssuite](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/network-parent-giant.test.js) |
| KAB-0345 | Shift-Drag X/Z im Netz; bestehender Schacht-Snapshot bleibt unverändert | [Netz-Regressionssuite](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/network-parent-giant.test.js) |
| KAB-0346 | Explizite Verknüpfung zweier verschiedener Schächte | [Netz-Regressionssuite](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/network-parent-giant.test.js) |
| KAB-0348 | Link-ID und strikt ungeprüfter physischer Status, JSON-normalisiert | [Netz-Regressionssuite](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/network-parent-giant.test.js) |
| KAB-0349 | Abhängige Kabelrouten mit ID vor Linkentfernung anzeigen | [Netz-Regressionssuite](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/network-parent-giant.test.js) |
| KAB-0351 | Selbstverbindung nicht gespeichert | [Netz-Regressionssuite](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/network-parent-giant.test.js) |
| KAB-0352 | Getrennte Netzkomponenten erkannt | [Netz-Regressionssuite](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/network-parent-giant.test.js) |
| KAB-0353 | Vier-Schacht-Zyklus erkannt, kein Endlossuchen | [Netz-Regressionssuite](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/network-parent-giant.test.js) |
| KAB-0354 | Kollisionsreduzierte Bildschirm-Labels ohne Datenänderung | [Netz-Regressionssuite](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/network-parent-giant.test.js) |
| KAB-0355 | Maßstabsbezogene X/Z-Luftlinie in Netzansicht | [Netz-Regressionssuite](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/network-parent-giant.test.js) |
| KAB-0358 | Link-Anfang und -Ziel werden in UI unterschieden | [Netz-Regressionssuite](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/network-parent-giant.test.js) |
| KAB-0359 | Physischer Status bleibt 'unverified' / 'needs-evidence' | [Netz-Regressionssuite](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/network-parent-giant.test.js) |
| KAB-0360 | Fünf fiktive Schächte mit 3/4-Wand-Modellen | [Netz-Regressionssuite](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/network-parent-giant.test.js) |
| KAB-0361 | Eine globale Route mit mehreren expliziten lokalen Kabelsegmenten | [Netz-Regressionssuite](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/network-parent-giant.test.js) |
| KAB-0362 | Lokale Kabel-IDs je Schacht referenziert; globale Route-ID unverändert | [Netz-Regressionssuite](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/network-parent-giant.test.js) |
| KAB-0363 | Reihenfolge von drei Schächten reproduzierbar | [Netz-Regressionssuite](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/network-parent-giant.test.js) |
| KAB-0364 | A/B-Rohranschlüsse der Durchgangsschächte getrennt gespeichert | [Netz-Regressionssuite](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/network-parent-giant.test.js) |
| KAB-0365 | Netzlinie niemals automatisch bestätigter Kabelübergang | [Netz-Regressionssuite](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/network-parent-giant.test.js) |
| KAB-0366 | Fehlender Kabelendpunkt meldet Unterbrechung | [Netz-Regressionssuite](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/network-parent-giant.test.js) |
| KAB-0367 | Nicht bestätigter Übergang bleibt explizite Lücke | [Netz-Regressionssuite](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/network-parent-giant.test.js) |
| KAB-0369 | Ringroute mit fünf Segmenten terminiert deterministisch | [Netz-Regressionssuite](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/network-parent-giant.test.js) |
| KAB-0370 | Zwei aufeinanderfolgende Segmentwechsel geprüft | [Netz-Regressionssuite](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/network-parent-giant.test.js) |
| KAB-0373 | Gelöschtes lokales Kabel ergibt fehlendes Routensegment | [Netz-Regressionssuite](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/network-parent-giant.test.js) |
| KAB-0374 | OLA/MS-Konflikt je Route gemeldet | [Netz-Regressionssuite](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/network-parent-giant.test.js) |
| KAB-0375 | Globale XYZ-Segment-Polylinien aus lokalen Snapshots | [Netz-Regressionssuite](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/network-parent-giant.test.js) |
| KAB-0376 | Pro Segment eigene Status-/Quellversionsfelder | [Netz-Regressionssuite](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/network-parent-giant.test.js) |
| KAB-0377 | Fehler in genau einem Segment durchgängig erkannt | [Netz-Regressionssuite](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/network-parent-giant.test.js) |
| KAB-0378 | physische Ende-zu-Ende-Kontinuität bleibt false ohne Belege | [Netz-Regressionssuite](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/network-parent-giant.test.js) |

Relevante Implementierungen: [Netzmodell](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/network.js), [Netzpfadsuche](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/network-route-planner.js), [XYZ-Modelle](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/network-world-route.js), [App-Interaktionen](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/app.js), [Netz-UI](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/index.html), [Regression](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/network-parent-giant.test.js). Mikro-Tasks erhalten **keine** pauschalen Abschluss-Häkchen; ihr Beleg-Ledger bleibt separat. 
