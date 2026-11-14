# Kabelsalat · 30-Mikroschritte-Sprint · 14.11.2026

**Umfang:** 30 konkrete Teilprüfungen / synthetische Unit-Testfälle; nicht 30 vollständig abgenommene Features. Tatsächliche Browser-, Foto-, PDF- und Bro-Fachabnahme ist hiermit nicht behauptet.

**Code:** [Synthetische Regressionstests](../../tests/sprint-30-2026-11-14.test.js) · [Test-Suite](../../package.json) · [Referenzdaten](../../examples.js) · [Foto-Detektor](../../photo-candidates.js) · [DN-Modell](../../pipe-dimensions.js).

**Ausführung:** [Erfolgreicher GitHub-Pages-Workflow (enthält `npm test`)](https://github.com/valoon4/Androidbaze/actions/runs/43631271941): Test-Log meldet `OK KAB sprint: photo heuristics/DN synthetic fixtures; no real-image approval` und `ATOMIC OK`. Ausgeführt auf Commit `845676e301485fd96b91e0f41c9eec1ba9d99788`. Die anschließende Parent-Dokumentation verändert diese Tests nicht.

| Mikro-ID | Nachweis |
|---|---|
| KAB-0882/M20 | Kalibrierreferenz als separate Eingaben: ausgewählter Kreisindex und bekanntes Referenzmaß in mm; kein impliziter DN. |
| KAB-0882/M21 | Synthetischer Dunkelkreis bei hellem Hintergrund: Mittelpunkt und genau ein Pixel-Kandidat per Unit-Test. |
| KAB-0882/M22 | Synthetischer Kreis in dunkler Umgebung: ungefähr 35 px, eigener Regressionstest. |
| KAB-0882/M23 | Zwei getrennte benachbarte Kreise liefern zwei Kandidaten, keine Verschmelzung. |
| KAB-0882/M24 | Zwei unterschiedliche Pixelkreise: großer Durchmesser mehr als doppelt so groß, ohne automatische Nenn-DN-Zuordnung. |
| KAB-0882/M25 | Deutlich elliptische Öffnung als kein sicherer Kreiskandidat behandelt; Test für Perspektivgrenze. |
| KAB-0882/M26 | Teilweise verdeckte Öffnung als heuristischer Unsicherheitsfall; nur Kandidat bzw. nicht erkannt, nie sichere mm-Werte. |
| KAB-0882/M27 | Heller Blendfleck vor dunkler Umgebung als synthetischer Negativfall; keinerlei physische mm-Freigabe. |
| KAB-0882/M28 | Textähnliche Strichstruktur: keine erfundene absolute Rohrgröße aus visueller Erkennung. |
| KAB-0882/M29 | Angeschnittener Kreis an Bildkante: kein sicherer Millimeterdurchmesser ohne Referenzmaß. |
| KAB-0882/M30 | Explizite 50-mm-Kalibrierung: Referenzkreis hat danach genau 50-mm-*Kandidatenwert*. |
| KAB-0882/M31 | Explizite 110-mm-Kalibrierung: derselbe Pixelkreis ergibt 110 mm, keine hartkodierte 50er-Umrechnung. |
| KAB-0882/M35 | Unkalibrierte Kreise nur relativ groß/klein und mit Pixel-Durchmesser; keine mm-Angabe. |
| KAB-0882/M38 | Erkennung erzeugt keine Rohr-ID oder `reviewed`-Freigabe, nur `candidate`. |
| KAB-0882/M39 | Explizite Kalibrierung auf 200 mm wird nicht auf DN160 gekappt; weiterhin ungeprüft. |
| KAB-0882/M40 | Upload-Allowlist im Anwendungscode überprüft: PNG/JPEG/WebP zulässig, nicht genannte MIME-Typen abgewiesen. Nur statischer UI-Test, keine simulierte Browserdatei. |
| KAB-0882/M41 | Bildskalierung vor Analyse im Code geprüft: längste Bildseite 900 px, keine Behauptung von Originalpixelmaßen nach Skalierung. |
| KAB-0882/M42 | Negativtests für mehr als 4 MP Analysefläche und unpassende Pixeldatenlänge. |
| KAB-0882/M44 | Qualitätsscore auf Bereich 0–1 geprüft; bleibt heuristische Größe, nicht physikalische Konfidenz. |
| KAB-0882/M46 | Reproduzierbare synthetische Positiv-/Negativ-Foto-Fixtures in der `npm test`-Suite eingebunden. |
| KAB-0882/M48 | Ohne bekannte Referenz keine physische Messung; selbst kalibrierter Output nur `candidate`. |
| KAB-0887/M04 | Assertion für alle zwölf DN-Werte 30/40/50/60/63/75/90/110/116/125/140/160 in fiktiver Demo ergänzt. |
| KAB-0887/M05 | Zweite gültige synthetische Probe und DN30-vs-DN160-Größenrelation ergänzt. |
| KAB-0887/M06 | Negativbeleg: `status: reviewed` ohne belegte Herkunft wird zu `unverified` zurückgestuft; Demo ohne `sourceInfo`. |
| KAB-0887/M12 | Beleg direkt im Parent `08-BRO-KURZE-LR-DN30-160.md` verlinkt; visuelle Abnahme und echte Maße bleiben offen. |
| KAB-0887/M14 | Negativtest für Quellenänderung: neue DN-Demo lässt PDF-V2-Originalreferenz und 91 Rohr-IDs/Anzahl unverändert. |
| KAB-0886/M06 | Schematische DN50/DN110-Abmessungsrelation 50:110 = 1:2,2 als Assertion verifiziert. |
| KAB-0886/M17 | Negativtest: DN-Demo mutiert weder PDF-V2-Rohrquellen noch ihre ursprünglichen IDs/Anzahlen. |
| KAB-0888/M06 | DN50/DN110 im zweiten Vergleichstask über gemeinsamen dimensionsgetreuen Assertionspfad geprüft. |
| KAB-0888/M11 | Gegenprobe mit getrenntem 91-LR-PDF-Schacht zusätzlich zum synthetischen DN-Mischschacht; keine Vermischung der Quellen. |

**Wichtige Grenzen:** Fotodetektor ist eine einfache Heuristik (kein trainiertes Modell, keine OCR), Perspektiven/Überlappungen/Verdeckung können falsche Kandidaten verursachen. Physische Größen ohne Referenz/Quellmaß dürfen nicht als geprüft gelten. 91 Öffnungen sind die PDF-V2-Referenzdaten, keine vermessene Ausführungsbestätigung.
