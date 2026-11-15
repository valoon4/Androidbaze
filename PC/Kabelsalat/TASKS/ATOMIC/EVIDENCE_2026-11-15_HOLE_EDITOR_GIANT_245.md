# KAB-0235–0250 · Leerrohr-Editor 2.1 · 15.11.2026

**Umfang:** 16 technisch eigenständig abschließbare Parent-Tickets und 245 vorhandene S0-Mikro-IDs. Reiner **schematischer Modell-Editor** für Nutzer-/Demo-Schächte; keine Vermessungs-, Rohrzulassungs-, Brandschutz- oder DIN-/VDE-Freigabe. Importierte PDF-/HTML-Originale sind für automatische Gruppen- und Wandoperationen gesperrt.

| Parent | Erfüllter technischer Umfang |
|---|---|
| **KAB-0235** | Rohre auf jeder Wand via 2D oder 3D neu anlegen; Mittelpunkt und DN validiert; 600er-Obergrenze und Originalquellhinweis |
| **KAB-0236** | Globale eindeutige Rohr-ID bei Einzelkopie und Gruppenkopie; bestehende IDs dürfen nicht dupliziert werden, auch nicht beim Wandwechsel |
| **KAB-0237** | 12 DN-Vorschläge von 30 bis 160 und frei wählbare ganze 1-mm-Zwischenschritte; keine stille Begrenzung ungültiger Eingaben |
| **KAB-0238** | Separater optionaler physischer Rohr-Außen-Ø im bestehenden `dimensionSpec`, getrennt von DN und Öffnungs-Ø; JSON-Roundtrip |
| **KAB-0239** | Separate optionale Rohrwandstärke `dimensionSpec.wallThicknessMm` von 0,1–200 mm; Eingabe, Beschreibung, Persistenz, Negativwerte |
| **KAB-0240** | Rohr-X entlang der Wand auf genaue ganze Millimeter einstellen; eindeutiger normierter U-Wert, gültige Wand- und Öffnungsgrenze |
| **KAB-0241** | Rohr-Y/Höhe auf genaue ganze Millimeter einstellen; eindeutiger normierter V-Wert, gültige Wand- und Öffnungsgrenze |
| **KAB-0242** | 2D-Mausverschiebung mit wählbarem 10-/25-/50-mm-Fangraster und Undo; im Chromium nachgewiesen |
| **KAB-0243** | 2D-Mausverschiebung ohne gröberes Raster (1-mm-Präzision), negative Bewegungen ohne unnötigen Undo-Eintrag blockiert; Chromium-Test |
| **KAB-0244** | Mehrreihige/mehrspaltige Rohrgruppe mit festem X-/Y-Mittelpunktabstand vorprüfen und als eine undo-fähige Operation ergänzen |
| **KAB-0245** | Dieselbe Gruppe mit gemischter DN-Folge (z. B. 30/110/50/160), eindeutige neue IDs und validierte Maße |
| **KAB-0246** | Rohr mit physisch beibehaltenem mm-Abstand entlang der Wand auf eine andere Wand versetzen, Undo/Redo und eindeutige ID |
| **KAB-0247** | Bei belegten Kabelanschlüssen Warnung + ausdrückliche Bestätigung; Kabel-ID und Route bleiben unverändert, geänderte Anschlusswand wird zur Nachprüfung gekennzeichnet |
| **KAB-0248** | Durchbruchradius zählt zur Wandgrenze; 2D-/3D-Neuanlage, mm-Edit und Drag werden außerhalb abgewiesen, nicht still geklemmt |
| **KAB-0249** | Überlappung mit anderen Bohrungen wird vor Neuanlage/Kopie/Gruppe/Transfer/3D-Drag erkannt; Drag gesperrt, bei absichtlicher Anordnung explizite Rückfrage |
| **KAB-0250** | Exakte DN- und X-/Y-mm-Tastatureingaben, inkl. Ablehnung nichtganzzahliger, leerer und außerhalb der Wand liegender Werte |

## Durchgeführte Prüfungen

- **84 direkt ausgeführte V8-Unit-Assertions** auf den aktuellen GitHub-Modulen `hole-editor-ops.js`, `pipe-dimensions.js`, `app.js` und `index.html`. Enthalten: vier Wandachsen, PDF-Gegenachsen, DN-Vorschläge, 1/10/25/50-mm-Snap, IDs, positive/negative Wand- und Paargeometrie, Rohrwandstärke/OD/DN als getrennte Werte, zwei-/mehrreihige gemischte DN-Gruppen, rückstandsfreie Quelle, Kabel-ID-/Route-Erhalt, Quelle/Geometrie-Conflict.
- [GitHub Pages #43715517644](https://github.com/valoon4/Androidbaze/actions/runs/43715517644) war auf dem technisch erweiterten Commit **success**; führt auch alle Unit-, Smoke-, Beispiel-, DIN-Nichtbehauptungs- und Offline-HTML-Hash-Tests aus.
- [Echter Chromium-Workflow #43715517704](https://github.com/valoon4/Androidbaze/actions/runs/43715517704): Schritt `Leerrohr-Editor mit echtem Chromium testen` im neuesten Code **success**; `tests/browser-hole-editor.js` klickt neue DN75-SW4-Öffnung, erzeugt 4 gemischte DN-Rohre, prüft Modell-Unveränderlichkeit der Vorschau, bearbeitet X/Y/DN auf 310 mm/DN63, speichert Außen-Ø und Rohrwand, kopiert mit neuer ID, zieht im 25-mm- und 1-mm-Raster, wechselt ein angeschlossenes Rohr von SW1 auf SW4 mit Dialogbestätigungen, Undo/Redo, prüft PDF-Quelle mit unveränderten 91 Löchern.
- Vollständiger visueller Workflow und Windows-Portable-Build werden separat via GitHub Actions ausgewertet; ein erfolgreicher Einzeltest ist kein realer Bauplanbeweis.
- `tests/hole-editor-ops.test.js` ist in `npm test` und `tests/browser-hole-editor.js` in `.github/workflows/kabelsalat-visual.yml` eingebunden. `hole-editor-ops.js` ist in Desktop-EXE, Web und signierter CSP-/SHA256-Offline-Einzeldatei registriert.

**Technische Änderungen:** [transaktionaler Hole-Editor](https://github.com/valoon4/Androidbaze/commit/9d8b2829972a74feb389f6c6c8fa783907281057) · [metrischer Editor/Source-Gates](https://github.com/valoon4/Androidbaze/commit/35c9c2b83438c9e2f0e3afe42230321c700c7559) · [2D-Fangraster](https://github.com/valoon4/Androidbaze/commit/47bcc846ffea677a03e056b4f8f81aab899f8b06) · [Gruppen-/Wandwechsel](https://github.com/valoon4/Androidbaze/commit/31de3ab34d32a8f2a436efdaa224ba4ac9954770) · [3D-Grenzschutz](https://github.com/valoon4/Androidbaze/commit/b76ca9988969c8e934c2a652fe8880132efe5ae9) · [Doppelt-ID-Negativtest](https://github.com/valoon4/Androidbaze/commit/1c949d219e367318d618cca389185393ef79a184).

**Bewusste Grenzen:** Gruppenkonstruktion basiert auf geplanter DN-Folge (keine automatische CAD- oder Foto-Analyse). Beim Wandwechsel bleibt die Kabelroute bestehen und wird explizit als fachlich zu überprüfen gekennzeichnet. Ein ungeprüftes mm-Label ist keine tatsächliche Abmessung. Die bestätigten Bro-Referenzdaten werden nicht überschrieben.
