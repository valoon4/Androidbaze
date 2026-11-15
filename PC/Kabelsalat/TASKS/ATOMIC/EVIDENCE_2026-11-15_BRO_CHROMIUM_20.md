# 20 Bro-Feedback-Mikroschritte: echtes Chromium (15.11.2026)

**Verifizierter Build:** [GitHub Actions #43708404172 – alle Schritte success](https://github.com/valoon4/Androidbaze/actions/runs/43708404172), SHA `b6dec368ab5f32721d374f814d348724307b494f`. Der reale Browser-Test `tests/browser-visual.js` war **success** mit aktiviertem WebGL. Das 15.771.623-Byte-[Screenshot-Artifact #14159621745](https://github.com/valoon4/Androidbaze/actions/runs/43708404172/artifacts/14159621745) wurde nachweisbar gespeichert; außerdem waren Spacing, Network, XYZ und Pitch-Wandbilder im selben Lauf grün.

| Task-IDs | Geprüfter konkreter Nachweis |
|---|---|
| KAB-0877/M02 | Unveränderte Referenz-Wandansichten und erster Basic-Schachtscreenshot vor synthetischen Änderungen erstellt. |
| KAB-0877/M07 | Basic-Schachttext, sichtbare WebGL-Darstellung und ausgeblendete lange Kabelpfade live geprüft. |
| KAB-0877/M08 | Profi-Schachteditor nach Umschalten aus Basic live geprüft; Projektname ändern / Undo und erneut 3D anzeigen erfolgreich. |
| KAB-0877/M09 | PDF-Wandvergleich SW1–SW4 und 2D-/3D-Darstellung im Browser geprüft; echte Quelle getrennt von schematischer Darstellung. |
| KAB-0877/M13 | Chromium-WebGL- und Browser-Visualtest der Beton-/Lochöffnungen live erfolgreich. |
| KAB-0877/M15 | Browser-Run + Screenshot-Artifact in `07-BRO-BETON-ROHR-DM.md` am Parent verlinkt. |
| KAB-0878/M05 | Dunkle/strukturierte Betonansichten und Review-Screenshots in derselben Parentdatei am richtigen Build verlinkt. |
| KAB-0882/M43 | Zwei direkt aufeinanderfolgende synthetische Foto-Uploads (ältere/neue Quelle) und Rücksetzen der mm-Kalibrierung in echtem Chromium; lokales Projekt bleibt bitgleich. |
| KAB-0885/M05 | `bro-lr-only-shaft-step.png`: Basic-Schacht mit Beton-/LR-Öffnungen. |
| KAB-0885/M06 | `bro-cable-routes-separate-step.png`: Kabelansicht klar von LR unterschieden. |
| KAB-0886/M02 | Unveränderte Referenzdaten vor fiktivem DN-Edit in `all-four-pdf-walls.png` / Basic-Screenshot. |
| KAB-0886/M08 | Editierbarer DN30–160-Profi-Editor und 3D-Ansicht in Chromium, mit `synthetic-dn-profi-editor.png`. |
| KAB-0886/M11 | PDF-V2 91 Originalöffnungen als zweite Quell-Gegenprobe nach synthetischer 94-LR-Demo unverändert wiederhergestellt; neben HTML-Quellen durch automatisierte Referenztests geprüft. |
| KAB-0886/M13 | Live Chromium DN30/50/110/160 mit 2D/3D, Kabelstufe und Profi-Editor: Erfolg. |
| KAB-0886/M15 | Screenshot/Testbeleg in `08-BRO-KURZE-LR-DN30-160.md` am Parent verlinkt. |
| KAB-0888/M02 | Unveränderte PDF-V2-Quelle in `all-four-pdf-walls.png` vor Demo-Injektion. |
| KAB-0888/M08 | Synthetischer DN-Vergleich in Profi-Editor-Ansicht `synthetic-dn-profi-editor.png` mit DN-Eingabeschritten geprüft. |
| KAB-0888/M09 | Dieselbe synthetische DN-Probe als sichtbare WebGL-3D-Ansicht und 2D-Schachtgrafik; `synthetic-lr-dn-2d-3d.png`. |
| KAB-0888/M13 | Vollständige Browser-Regression für die fiktive DN-Reihe mit 91-LR-PDF-Restore abgeschlossen. |
| KAB-0888/M15 | Exakter Run + Screenshot-Artifact im Parent `08-BRO-KURZE-LR-DN30-160.md` verlinkt. |

**Explizit NICHT erledigt:** Bros eigene ästhetische/technische Rückmeldung, echte maßstäbliche Rohr-/Lochdaten, echte Binär-PDF/OCR-Erkennung, 3D-Rohr-Picking wo nicht geprüft, vor/nach Materialänderung identische Kamerawinkel als gesonderter Vergleich oder Engineering-Freigabe. Dies sind keine 20 Parent-Task-Abnahmen, sondern **20 einzeln belegte Unteraufgaben**.