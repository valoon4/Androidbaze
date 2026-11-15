# KAB-0881 · zehn eigenständig geprüfte PDF-Textlayer-Mikroschritte

**Datum:** 15.11.2026 · **Scope:** konservativer Parser für **bereits extrahierte** PDF-Textlayer-Seiten. Keine Binär-PDF-Extraktion, OCR, automatische Rohrtypfreigabe oder reale DIN-/Brandschutzprüfung.

## Umsetzung
- `pipe-dimensions.js` bietet `parsePlanPages(pages, {source,documentVersion,maxPages,maxChars})` und `planCandidateTable(pages,options)` als read-only Schnittstellen mit Seite, Zeile, Rotation, Quellenkennung und Dokumentversion.
- Harte Eingabegrenzen: max. 100 erlaubte Seiten im konfigurierten Budget, standardmäßig 32; Textbudget standardmäßig 100.000 Zeichen. Kaputte Textlayerdaten bzw. ungültige Rotation werden abgewiesen statt interpoliert.
- Labels DN 30 und DN 160 bleiben nur beschriftungsgebundene Kandidaten (`scaleStatus: not-provided`, `measurementBasis: drawing-label`), keine aus Pixeln gemessenen mm.
- Zwei identische DN-Labels können unterschiedlichen expliziten Rohr-IDs zugeordnet werden. **Zwei Rohr-IDs in derselben Textzeile:** keine automatische erste-ID-Zuordnung; menschliche Prüfung erforderlich.
- Widersprüchliche DN-Beschriftungen derselben Rohr-ID über mehrere Seiten werden als `conflict` markiert, nie automatisch ins Originalprojekt geschrieben.

## Nachweise
- [Code: Parser-/Provenienzschnittstelle](https://github.com/valoon4/Androidbaze/commit/1aac173737ce88077a6e8fe3f32aedaf64a3f010)
- [Version/Skalenstatus](https://github.com/valoon4/Androidbaze/commit/9b1418d77c548b827fe4a3f01631ac4aeedf7b9a)
- [Mehrdeutige ID verhindern](https://github.com/valoon4/Androidbaze/commit/207f718e00b17f9fe52fb8d0e758dc7b17768e5b)
- [Regressionssuite](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/tests/pipe-dimensions.test.js)
- Die aktuellen Module wurden unmittelbar über die verbundene GitHub-App geladen und die Parserfunktionen im JS-V8-Isolat ausgeführt: **PASS** für sechs Kandidaten über drei synthetische PDF-Textseiten, DN30/160, Quellenrevision und Seite, Konflikte, gleiche DN mit verschiedenen IDs, mehrdeutige Doppel-ID, Rotation, Limits, unveränderte Eingaben und vorhandene TXT-Parserfixtures.
- **Kein** echter Browser-/Node-`npm test`-Lauf oder echte PDF-Seitenextraktion behauptet.

## Abgeschlossene Mikro-IDs
KAB-0881/M17, M20, M21, M22, M23, M25, M27, M28, M29, M36.

## Weiter offen
KAB-0881 als Parent bleibt offen: Binär-PDF-Textlayer-Lesen, konkrete UI-Kandidatenliste, reale Pläne, Projektintegrationsprüfung und vollständige Browserabnahme. Bros Sichtentscheidung bleibt davon unabhängig. `planCandidateTable` ist eine Modellfunktion, **keine fertig eingebundene UI**.
