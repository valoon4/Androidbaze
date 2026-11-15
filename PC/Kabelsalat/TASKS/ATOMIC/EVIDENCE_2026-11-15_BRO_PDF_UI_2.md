# KAB-0881/M31–M32 · Plan-Kandidaten sichtbar prüfen

15.11.2026 · zwei Mikroabnahmen im vorhandenen **TXT-/eingefügten Textlayer**-Dialog.

- `app.js`: `planCandidateTable` sortiert Rohr-ID, Seite und Zeile; diese Reihenfolge erscheint sowohl im Auswahlfeld als auch in der neuen sichtbaren Kandidatentabelle.
- `index.html`: Tabelle mit **Rohr-ID, DN/Ø, Quelle · Seite · Zeile, Prüfstatus**.
- Textspalten ausschließlich über `textContent`, keine `innerHTML`-Auswertung aus unbekanntem Plantext.
- `tests/plan-candidate-ui.test.js` reproduziert den tatsächlichen `parsePlanDimensions`-Klickhandler mit drei unbestätigten synthetischen Quellzeilen und prüft Sortierung, Tabelleninhalt, Auswahl und Status. Im GitHub-V8-Isolat mit Node-API-Mocks ausgeführt: **PASS**. Die neue Testdatei ist im `npm test`-Script registriert.
- Die Quellkennzeichnung für Übernahme enthält explizite Seiten- und Zeilenangabe.

Commits: [Tabelle](https://github.com/valoon4/Androidbaze/commit/f63a58577fd863d955b908609c3e6e1ece3c8292) · [Rendering](https://github.com/valoon4/Androidbaze/commit/85888da4370717eec623b206ebd38f54554f1d64) · [Test](https://github.com/valoon4/Androidbaze/commit/9af71c171620cd082101bffb2f0c139819ad52e9) · [npm-Integration](https://github.com/valoon4/Androidbaze/commit/7b625405f2f9e850d2500aa9b5b0e2e75c037dcd).

**Offen:** Binäre PDF-Datei wird hier nicht verarbeitet, echte Planseiten mit Textlayer müssen separat extrahiert werden. Keine Bro- oder Chromium-Sichtabnahme behauptet, KAB-0881 als Parent bleibt offen.
