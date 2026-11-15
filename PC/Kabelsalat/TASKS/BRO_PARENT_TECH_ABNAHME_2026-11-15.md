# Kabelsalat · technisch abgeschlossene Bro-Parent-Tickets · 15.11.2026

**Geltungsbereich:** Nur technische Lieferobjekte, deren eigene Definition of Done durch Tests, Screenshots und unveränderte Quelldaten abgedeckt ist. Eine CI-Prüfung ist keine Fach-/Betonoptikfreigabe durch Bro.

| Parent | Ergebnis | Beleg |
|---|---|---|
| **KAB-0884** | **Technisch abgeschlossen:** Wiederholbare Betonwand-, Material-, DN/Lochmaß-, Negativfall- und Quellschutzregression mit synthetischen 60/110/116 und DN30–160; Browser-/Screenshots vorhanden | `tests/concrete-wall.test.js`, `tests/pipe-dimensions.test.js`, `tests/browser-visual.js`; vollständiger erfolgreicher Chromium-Run |
| **KAB-0886** | **Technisch abgeschlossen:** Neue/editorisch bearbeitbare LR-Nennweiten 30–160 in 1-mm-Schritten, proportionaler 2D-/3D-Kreisdurchmesser, vorhandene Import-/Altwerte nicht automatisch auf die neuen Eingabegrenzen begrenzt | `tests/bro-dn-range.test.js`, Browser-DN30/50/110/160, 2D-/3D-Regressionsscreenshots |
| **KAB-0887** | **Technisch abgeschlossen:** Separater ausdrücklich **fiktiver** DN30/40/50/60/63/75/90/110/116/125/140/160-Testschacht; keine Umschreibung der 48/78/91 referenzierten Rohröffnungen | `examples.js`, `tests/bro-dn-range.test.js`, `tests/sprint-30-2026-11-14.test.js`, Chromium-Demo |
| **KAB-0888** | **Technisch abgeschlossen:** Automatisierte Vergleichs- und Browserprüfungen inkl. DN30/50/110/160, schematischer 160-mm-Endmarkierung, Kabel-vs-LR-Trennung, JSON-Roundtrip, 91-PDF-Referenzquellintegrität, 2D/3D und Screenshotartefakt | `tests/browser-visual.js`, `tests/bro-dn-range.test.js`, `tests/browser-bro-picking.js` |

**Live nachprüfbar:**
- [Vollständig erfolgreicher GitHub-Actions-Chromium-Run #43709846491](https://github.com/valoon4/Androidbaze/actions/runs/43709846491), einschließlich 3D-Picking, PDF-SW1–SW4, Netzrouten, Pitch und Screenshot-Upload
- [Screenshot-Artefakt #14160177271 (21.488.954 Bytes)](https://github.com/valoon4/Androidbaze/actions/runs/43709846491/artifacts/14160177271)
- [Dokumentierte elf technische Abschluss-Mikroschritte](./ATOMIC/EVIDENCE_2026-11-15_BRO_FINAL_11.md) und [weitere Chromium-Nachweise](./ATOMIC/EVIDENCE_2026-11-15_BRO_CHROMIUM_20.md)
- [Geometrische und DN-Testregressionen](../tests/concrete-wall.test.js), [DN-Range-Fixtures](../tests/bro-dn-range.test.js)

## Nicht als abgeschlossen behauptet

- **KAB-0877/0878/0879:** Die übergreifende **Sichtabnahme durch Bro an beiden Referenzschächten** liegt weiterhin nicht dokumentiert vor; insbesondere ist die mittel-/dunkelgraue Betonoptik nicht explizit freigegeben.
- **KAB-0881/0882:** Echte Binär-PDF-Verarbeitung, OCR/Scan-Extraktion und zuverlässige Rohrtypklassifikation aus Foto/Plan sind nicht fertig; vorhandene Kandidatenerkennung ersetzt diese Anforderungen nicht.
- **KAB-0885:** Bros Bestätigung des kürzeren visuellen LR-/Kabelwegestils steht aus. Ohne reale Maße werden keine physischen Bauteillängen geändert.
- **KAB-0878/M09:** Der einzige noch offene aus den ursprünglich 200 Bro-Feedback-Mikroschritten ist das Zuordnen von **Bros neuer konkreter Antwort** zur Betonoptik. Die frühere Aussage „sieht besser aus, aber warum ... so lang“ betrifft primär die lange Kabel-/LR-Darstellung und ist keine eindeutige, positive Farb-/Betonmaterialabnahme.

**Wichtig:** Selbst wenn alle 200 Mikro-IDs eines Parents abgehakt sind, gilt ein Parent nicht automatisch als abgeschlossen, wenn ein fachliches oder externes Akzeptanzkriterium fehlt. Alle Rohrgrößen aus der Demo bleiben schematisch/nominell und ersetzen kein Aufmaß, AVL-/DIN-/Brandschutz- oder Herstellerurteil.
