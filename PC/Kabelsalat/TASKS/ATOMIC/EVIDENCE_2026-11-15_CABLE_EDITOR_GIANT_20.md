# KAB-0251–0270 · Kabeleditor-Gigant · 15.11.2026

**Umfang:** 20 technische Parent-Tickets in P1-02/P1-03. Implementierung auf [PR #78](https://github.com/valoon4/Androidbaze/pull/78), Squash-Merge [68841175](https://github.com/valoon4/Androidbaze/commit/68841175ddf875aebb79a120c18ed62779312a5e). **Alle Parent-DoD-Funktionen wurden technisch implementiert oder durch zuvor vorhandene, integrierte Netzfunktionen belegt.** Die 320 historisch erzeugten Mikro-Checkboxen werden **nicht** pauschal abgeschlossen: Einzelne Mikro-Abnahmen wie responsives Layout und externer Quellabgleich bleiben einzeln offen, bis ihr Nachweis vorliegt. Kein Nachweis über bautechnische Freigabe.

| Parent | Geprüfte konkrete Funktion / Grenze |
|---|---|
| KAB-0251 | Leerrohrlöschung nur nach bestätigtem Dialog, betroffene Kabel mit Name und Ende aufgelistet; PDF/HTML-Importrohre werden blockiert |
| KAB-0252 | Rohrliste nach Wand, Trassen-/Quellhinweis oder DN sortiert, Selektionsbutton bleibt mit Wand- und Loch-ID verknüpft |
| KAB-0253 | Neues Kabel mit eindeutiger stabiler ID, Name und beidseitig zunächst offenem Anschluss |
| KAB-0254 | Numerischer Kabelaußen-Ø 2–80 mm, ungültige Werte zurückgewiesen |
| KAB-0255 | Eigener kabelbezogener Typkatalog im Projekt inkl. Name, Kategorie, Ø und Typreferenz |
| KAB-0256 | Kabelgruppe OLA/MS/Weitere unabhängig von frei editierbarer technischer Kategorie gespeichert |
| KAB-0257 | Startrohr mit exakter Wand-/Loch-ID aus vorhandener Rohrliste zuordenbar |
| KAB-0258 | Zielrohr separat mit exakter Wand-/Loch-ID zuordenbar |
| KAB-0259 | Einseitiger Anschluss bleibt ausdrücklich null, keine künstliche Gegenverbindung |
| KAB-0260 | Vor A/B-Tausch Bestätigung; unaufgelöste Metadaten werden mitgetauscht |
| KAB-0261 | Vorhandenen Anschluss auf ein anderes existierendes Rohr umhängen; unbekannte IDs abgewiesen |
| KAB-0262 | Entferntes Startrohr: betroffenes Kabel bleibt mit unveränderter ID, unresolvedA dokumentiert die ursprüngliche LR-ID |
| KAB-0263 | Entferntes Zielrohr: betroffenes Kabel bleibt mit unveränderter ID, unresolvedB dokumentiert die ursprüngliche LR-ID |
| KAB-0264 | Anschluss über zwei verschiedene Wände im selben Schacht, nicht auf identische Rohr-ID ohne Wand konvertiert |
| KAB-0265 | Schon bestehende Netzrouten modellieren Schachtübergänge als Segmente pro Schacht und Kabel-ID; bestehender 30-Schacht- und Continuity-Test sowie neuer Routenlöschschutz bleiben grün |
| KAB-0266 | Umbenennung ändert nur Anzeigename, alle Referenzen bleiben per ID |
| KAB-0267 | Duplikat mit neuer ID trotz gleicher Bezeichnung/Quelle; Netzrouten verwenden weiterhin IDs statt Namen |
| KAB-0268 | Status Entwurf / Zur Prüfung separat in Modell und Oberfläche; technische Freigabe wird nicht still behauptet |
| KAB-0269 | Kabelkopie inklusive Herkunftsangaben, neuer ID, sourceCopyOf sowie Zur-Prüfung-Markierung |
| KAB-0270 | Vor Kabelentfernung explizite Bestätigung mit betroffenen Netzrouten; Netzroutenschritte bleiben als fehlend dokumentiert, nie heimlich umgeroutet |

## Verifizierte technische Nachweise

- **GitHub Pull-Request-CI vollständig grün:** [Run 43718610863](https://github.com/valoon4/Androidbaze/actions/runs/43718610863), Linux/Node und alle Chromium-Prozeduren. Enthält die bestehenden PDF-, Bro-Picking-, Rohrabstands-, Netzwegfinder-, 30-Schacht- und XYZ-Tests.
- **Neuer realer Chromium-Regressionstest:** `tests/browser-cable-editor.js` prüft Anlage, Diameter/Name, separate OLA und technische Kategorie, neuen Typ, Anschlüsse, Tausch, Kopie, Rohrlöschung an SW1/LR1 bei gleichnamigem SW2/LR1, erhaltene Endpunkt-Provenienz, Undo/Redo, Routenwarnung und Reload. Screenshot unter Actions-Artefakten.
- **Neuer isolierter Unit-Test:** `tests/cable-editor-ops.test.js` prüft wall-spezifische Löschabhängigkeit, ungültige Endpunkte, fehlende Typen, unabhängige Namen und IDs, DNSortierung, Beleg-Herkunft und keine stille Routenänderung.
- **Alte JSON-Beispiele unverändert:** `tests/examples.test.js` und `tests/references.test.js` vergleichen ihre kompletten gespeicherten Projektobjekte byte-strukturgetreu auf Gleichheit. Neue Metadaten bleiben optional, wenn alte Daten sie nicht enthalten.
- **GitHub Pages:** [Run 43719174170](https://github.com/valoon4/Androidbaze/actions/runs/43719174170) auf Merge-Commit erfolgreich. App und Pitch haben dieselbe Version `2.0.12-beta.1`. Auch `hole-editor-ops.js` wird jetzt wirklich ausgeliefert.
- **Offline-Absicherung:** exakt 22 versionierte lokale Skripte mit CSP-SHA256 sowie `cable-editor-ops.js` in EXE-Assetliste, unveränderte Original-Importdaten.

**Fachliche Grenzen:** Keine Behauptungen über gemessene Rohrmittelpunkte, Brandschutz, reale Kabel-Radien, VDE/DIN, AVL oder Herstellerfreigaben. UI-Status `Zur Prüfung` ist ausdrücklich keine Abnahme. Importierte Originalbohrungen können nicht über diese Löschfunktion verändert werden. Unterschied zwischen DN, Außendurchmesser und tatsächlichem Öffnungsdurchmesser bleibt erhalten.

**Offene atomare Detailabnahmen:** Die Mikro-IDs bleiben in `ATOMIC/COMPLETED.csv` offen, solange für die jeweilige Einzelhandlung kein eigenständiger Nachweis (z. B. kleine Viewports, Basic-Tastatur, externes Feedback) eingetragen ist; die 20 Parent-DoD sind davon als Feature-Abschluss getrennt. Kein pauschales 320er-Abhaken.
