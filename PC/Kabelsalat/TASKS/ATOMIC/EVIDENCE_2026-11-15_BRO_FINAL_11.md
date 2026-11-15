# Bro-Sprint · 11 abschließbare Rest-Mikro-Tasks · 15.11.2026

**Echtes Chromium / GitHub Actions:** [erfolgreicher vollständiger Run #43709846491](https://github.com/valoon4/Androidbaze/actions/runs/43709846491) für Commit [63f96e19ca78faa76c3656017db19cfbb445e44f](https://github.com/valoon4/Androidbaze/commit/63f96e19ca78faa76c3656017db19cfbb445e44f). PDF/3D, eigenständiges 3D-Picking, Rohrabstand, Netzweg, 30-Schacht-Netz, globale XYZ-Routen, Pitch-Wandvergleich und Screenshot-Upload sind **success**. [Screenshot-Artefakt #14160177271](https://github.com/valoon4/Androidbaze/actions/runs/43709846491/artifacts/14160177271) ist 21.488.954 Bytes groß.

| Mikro-ID | Abnahmeergebnis |
|---|---|
| KAB-0877/M10 | **Echter 3D-Pointer-Raycaster-Test** wählt eine synthetische LR-Öffnung aus; Log: `BRO 3D PICK PASS: SW2 SW2-r1-o3 DN 75 pixel clearance 57.4`. Wall-ID, DN, U/V stimmen im Editor, keine Mutation der gespeicherten Quelle. |
| KAB-0877/M12 | Vorher/Nachher-Canvas-Screenshots bei gleicher Kamera und identischen Abmessungen: `bro-fixed-camera-before-material.png`, `bro-fixed-camera-after-material.png`. Materialdeckkraft wird reversibel verändert. |
| KAB-0881/M42 | Live-Plantextkandidatentest im Chromium: beschriftete positive Seite (2 Kandidaten DN30/DN160) und negative Seite (0 Kandidaten); Eingabe-/Tabellenanzeige und unverändertes Projekt geprüft. `bro-plan-label-positive-negative.png`. **Kein PDF-Binärimport/OCR.** |
| KAB-0885/M07 | Live 2D-Hitmapping gegen dieselben synthetischen LR-IDs/DN und denselben Schacht, der in 3D-WebGL dargestellt wird; `bro-same-lr-2d-3d-selected.png`. Das prüft die Daten-/Hitmap-Konsistenz, nicht die pixelidentische 3D-Projektion. |
| KAB-0886/M05 | Material-/Ansichtsoption `opacity` im Profi-Editor in echtem Chromium auf 65 geändert; Rohrgeometrie unverändert; Undo setzt Originaldeckkraft zurück. |
| KAB-0886/M09 | 2D-Schachtkoordinaten und 3D-Quelldaten einer synthetischen LR-Öffnung abgeglichen; DN und ID bleiben identisch. Zusätzlich bestehende 2D/3D-Screenshots im Run. |
| KAB-0886/M10 | Realer 3D-Picking-Regressionstest mit synthetischer DN30–160-Demo und verifiziertem SW2/DN75-Pick erfolgreich. |
| KAB-0886/M12 | Derselbe vor/nach materialisierte WebGL-Canvas bei gleicher Kamera, identischer Viewportgröße; Screenshot-Paar im Artefakt. |
| KAB-0888/M05 | Experten-Material-/Ansichtsoption Deckkraft verstellbar/rücksetzbar, ohne synthetische DN-Reihe oder Quelle zu verändern. |
| KAB-0888/M10 | Realer 3D-Pick auf geometrisch berechnete Bildkoordinaten eines LR-Objekts, bestätigte DN75 und Wall/Position. |
| KAB-0888/M12 | Vorher/Nachher-Screenshots mit unverändertem Kameraorbit; Ansichtsänderung bei gleichen Canvas-Abmessungen. |

**Noch offen: KAB-0878/M09.** Hier wird *Bros tatsächliche neue Rückmeldung* und ihre Zuordnung zur passenden KAB-ID verlangt. Eine echte Antwort liegt im GitHub-Tasknachweis nicht vor; eine erfundene Bro-Freigabe wird ausdrücklich nicht eingetragen.

**Grenzen:** Keine Fach-/Brandschutz-/Aufmaßfreigabe und keine echte OCR behauptet. Parent-Tickets werden nicht allein aus Mikro-Tests als fachlich abgenommen erklärt.