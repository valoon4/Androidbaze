# KAB-0882/M19 · Bildquellen je Foto-Kreiskandidat

Stand: 15.11.2026

- `photo-candidates.js`: optionales, begrenztes `source`-Feld wird an jedem Kreisvorschlag als `imageSource` geführt.
- `diameterPx` erhält ausdrücklich `diameterUnit: 'px'`. Ohne explizite Referenz bleibt jeder Kreis ein unbestätigter Kandidat; keine Millimeterwerte werden aus Pixeln erfunden.
- `calibrate` bewahrt Pixel-Einheit und Bildquellenkennung.
- `tests/photo-candidates.test.js`: synthetische Zweikreis-Fotoquelle, fehlende Quelle, fehlerhafte mehrzeilige Quelle und Kalibrierungs-Roundtrip.
- Direkte JavaScript-Ausführung des aktuellen GitHub-Moduls im V8-Isolat: PASS (zwei Kandidaten, Quelle+Einheit, Kalibrierung, ungültige Quellenkennung).
- Code-Commits: [Implementierung](https://github.com/valoon4/Androidbaze/commit/fd95789829dbd5eb11ad9e0661d2c6c301412076) · [Regression](https://github.com/valoon4/Androidbaze/commit/544bc1b8dd83e02360e69bbc5df962e9a206089d).

**Kein 100%-Versprechen:** Kein echter Browser-/CI-Run und keine visuelle Bro-Abnahme in diesem Nachweis. KAB-0882/M19 ist isoliert technisch erfüllt, der Parent KAB-0882 bleibt offen.
