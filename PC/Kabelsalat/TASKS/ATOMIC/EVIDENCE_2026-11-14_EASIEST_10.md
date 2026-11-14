# Kabelsalat · 10 statische Mikroprüfungen · 14.11.2026

**Umfang:** 10 leicht überprüfbare Mikroaufgaben (Code-/Anforderungsprüfung), **kein** Browser-Sichttest, **keine** fachliche oder Bro-Abnahme. Diese Nachweise ersetzen keine noch offenen Parent-Tasks. Geprüfter Stand: `main` vor Anlegen dieses Berichts.

| Mikro-ID | Nachgewiesenes Ergebnis | Quellanker |
|---|---|---|
| KAB-0885/M01 | Einseitiges Kabelende wird mit `.16` Metern (= 160 mm) zur Wandnormalen markiert; vorherige 360-mm-Markierung im entsprechenden Pfad nicht mehr vorhanden. | `app.js` → `drawCable`, `addScaledVector(normal(known.wall),.16)`; `tests/bro-dn-range.test.js` prüft explizit 160 statt 360 mm. |
| KAB-0885/M02 | Basic-Schacht-Stage unterdrückt durchgehende Kabelwege in 3D. | `app.js` → `drawCable`: `if(exploreMode&&exploreStage==='shaft')return;` |
| KAB-0885/M03 | Beim Wechsel zu `cable` greift die Schacht-Filterbedingung nicht; `openQuest('cable')` setzt die Stage und rendert neu. | `app.js` → `rememberQuest`, `openQuest`, `questCable`-Handler. |
| KAB-0885/M04 | Expertenmodus schaltet `exploreMode` aus und damit die Stage-Filterung ab; Kabelwege werden vom 3D-Renderpfad berücksichtigt. | `app.js` → `enterEditWorkspace`, `drawCable`. |
| KAB-0886/M01 | Relevante Zustände identifiziert: `questShaft` und `questCable` für die Basic-3D-Ansicht, `toggleExpert` für die editierbare Profi-Ansicht, `wallDn`/`holeDn` als Eingabefelder. | `app.js` → `openQuest` / `toggleExpert`; `index.html` → DN-Eingabefelder. |
| KAB-0886/M03 | Sichtbare Objekte sind eindeutig getrennt: LR = Wandöffnung, lange farbige `TubeGeometry` = Kabelpfad, `stub` = kurze schematische Endmarkierung. | `app.js` → `drawCable`, `exploreSceneText`; `concrete-wall.js` für ausgesparte Öffnungen. |
| KAB-0886/M04 | Prüfkriterium festgelegt: In der **fiktiven** DN-Demo muss die sichtbare Kreisöffnung mit DN 30 < 50 < 110 < 160 wachsen; in der Basic-Schachtansicht dürfen lange Kabelwege nicht als LR erscheinen; Originalkoordinaten bleiben unverändert. **Dies ist das Soll, keine Behauptung eines durchgeführten Browsertests.** | `TASKS/08-BRO-KURZE-LR-DN30-160.md`; `tests/bro-dn-range.test.js`. |
| KAB-0886/M16 | DN ist nur eine Nenn-/Zeichnungsangabe, keine automatische Aussage über Innen-, Außen- oder tatsächlichen Lochdurchmesser. `dimensionSpec.openingMm` ist eine separate explizite Größe. Werte werden in mm geführt; die Demo hat keine Vermessungsquelle. | `pipe-dimensions.js` → `normalize`, `apertureMm`; `examples.js` → `diameters`; `TASKS/08-BRO-KURZE-LR-DN30-160.md`. |
| KAB-0887/M01 | Prüffähige Akzeptanzbehauptung isoliert: `examples.create('diameters')` enthält eine **fiktive** 94-Loch-Demo mit sämtlichen zwölf DN 30/40/50/60/63/75/90/110/116/125/140/160; die Demo darf kein `sourceInfo` erhalten. | `examples.js` → `manifest`, `if(id==='diameters')`; `tests/bro-dn-range.test.js`. |
| KAB-0887/M02 | Test und Fixture lokalisiert: `tests/bro-dn-range.test.js` benutzt `examples.create('diameters')` aus `examples.js`, `pipe-dimensions.js` und `concrete-wall.js`. | Genannte Dateien, jeweils im Verzeichnis `PC/Kabelsalat/`. |

## Abgrenzung

- **Nicht durchgeführt:** Browser-Screenshots, echte Browser-/CI-Testläufe in dieser Sitzung, Bros Sichtabnahme, physikalische Rohrvermessung.
- **Weiter offen:** KAB-0885, KAB-0886 und KAB-0887 als Parent-Tickets und alle weiteren Mikroaufgaben dieser Parent-Tickets.
- **Wiederholbar:** Quellstellen und vorhandene Assertions im Repository nachvollziehen; automatisierten Test separat mit `node tests/bro-dn-range.test.js` ausführen.
