# KAB-0882/M33 · Foto-Kalibrierung rückgängig machen

**15.11.2026** · Foto-LR-Prüfung, keine reale Vermessung.

- Sichtbarer Button `photoResetCalibration` unter dem Kalibrierknopf ergänzt.
- `app.js` hält die ursprünglichen Pixelkandidaten separat in `photoUncalibrated` und setzt nur das **Fotoanalysemodell**, nicht die Projektgeometrie zurück.
- Die kalibrierten Millimeter-Kandidaten werden beim Zurücksetzen vollständig auf die unveränderten, nur relativen Pixelkandidaten rückgestellt.
- `photoImportEpoch` verhindert zusätzlich, dass eine alte, langsam ladende Fotodatei ein inzwischen neu ausgewähltes Bild überschreibt; kein echter Browser-Race-Test behauptet.
- `tests/photo-candidates.test.js` prüft Kalibrierungsoriginal, Reset-UI/Handler-Code und Stale-Image-Guard. Die aktuelle Foto-Testdatei wurde mit dem GitHub-Code in einer JS-V8-Umgebung ausgeführt: **PASS**.
- Der **tatsächliche Reset-Click-Handler-Body** aus `app.js` wurde separat mit einem 110-mm-Testkandidaten ausgeführt: **PASS**, Pixelkandidat 44 px wiederhergestellt, kein `candidateMm`, UI-Refresh aufgerufen.

**Commits:** [UI](https://github.com/valoon4/Androidbaze/commit/2873e41f523a58c2a9a7d34924bed464aa6a3454), [Code](https://github.com/valoon4/Androidbaze/commit/ce39cbfc48d6c5c5391178d6eabe7225a3172527), [Tests](https://github.com/valoon4/Androidbaze/commit/e7d966c0ae884420fdd051133ed71aacd4b8204c).

**Grenzen:** Kein realer Chromium-Lauf, keine volle Foto-/Rohrtyp-KI und keine Bro-Sichtabnahme. KAB-0882 bleibt als Parent offen; M43 (Bildwechsel-Browserabbruch) bleibt offen.
