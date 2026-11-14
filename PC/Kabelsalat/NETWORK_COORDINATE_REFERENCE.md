# KAB-0326 / KAB-0328 · Optionale Y-Höhe und Koordinatenreferenz

**Datum:** 15.11.2026 · **Version:** v2.0.11-beta.1 (Schemaergänzung, kein physisches 3D-Aufmaß).

## Quelle und Datenvertrag

Das lokale [network.js](./network.js) normalisiert Schacht-IDs und lokale Projekt-Snapshots. Vorher enthielt jeder globale Schacht ausschließlich `x` und `z` (schematische Lage in Modell-mm); eine unbekannte vertikale Position war **nicht** als vermessene Höhe belegbar.

Jetzt kann ein Schacht **optional** `y` (Ganzzahl mm, expliziter Modell-Offset) enthalten. Wenn das Feld fehlt, wird **kein y=0** in das JSON hinzugefügt; eine ungültige mitgelieferte Höhe wird als `null` erhalten, niemals in eine gemessene Lage umgedeutet.

Das gesamte Netz kann optional `coordinateReference` enthalten, einen maximal 120 Zeichen langen, vom Projekt vorgegebenen Text. Dieser Text ist **keine** automatisch geprüfte EPSG-ID, Datum-/Höhensystem-Referenz oder Vermessungsfreigabe. Fehlt das Feld, bleibt es abwesend; ist es leer, wird es zu `null` statt erfundenem Bezug.

Die vorhandene [network-world-route.js](./network-world-route.js) baut Welt-Modellpunkte nur aus expliziten lokalen und globalen Werten:
`[localX + shaftX, localY + (shaftY falls numerisch belegt), localZ + shaftZ]`.
Ohne Y-Feld wird der lokale y-Wert nur als **lokale Modellhöhe** weitergegeben. Der Weltpunkt markiert `globalElevationReferenceKnown=false` und `coordinateReferenceVerified=false`, unabhängig vom CRS-Text.

## Einzelbeispiele und 2-Kanten-Route

| Schacht | Netz-X | Netz-Z | Optional Y | Lokaler Punkt | Ergebnis |
|---|---:|---:|---:|---|---|
| S1 | 0 | 0 | 1200 | [200,900,100] mm | [200,2100,100] mm (unverifiziert) |
| S2 | 10000 | 3000 | -500 | [200,900,100] mm | [10200,400,3100] mm (unverifiziert) |
| S3 | 25000 | 1000 | 50 | [200,900,100] mm | [25200,950,1100] mm (unverifiziert) |
| Legacy S1 | 0 | 0 | **nicht vorhanden** | [200,900,100] mm | [200,900,100] mm **nur lokal** |

S1→S2→S3 hat zwei ausdrücklich modellierte Netzlinks, aber **zwei unbestätigte physische Kabelübergänge**. Die lokale Kabel-ID bleibt in jedem Snapshot erhalten. Eine unbekannte Ziel-ID oder ein fehlender Link wird nicht automatisch überbrückt. Ein doppelter Schacht-ID-Eintrag darf den ursprünglichen Schacht nicht ersetzen.

## Nachweis und Einschränkung

- [network-reference-metadata.test.js](./tests/network-reference-metadata.test.js): Zwei gültige Schächte, Legacy/fehlendes Y, Fremd-Daten/ungültige Höhe, doppelte Schacht-ID, fehlender Zielschacht, unterbrochene Kante, drei Schächte und zwei physische Lücken, unveränderte Ausgangs-JSONs, Roundtrip.
- [network.test.js](./tests/network.test.js): Netzwerk-IDs, Positions-CSV nur X/Z, getrennte Kabelreferenzen und Kantenfälle.
- [network-continuity.test.js](./tests/network-continuity.test.js): selbst ein vorhandener Netzlink beweist keinen echten Kabel-/Rohrübergang.
- [browser-network-path.js](./tests/browser-network-path.js): bestehende Schematisierung/Netzauswahl und Undo/Redo werden im Chromium separat geprüft. **Die neue optionale Y-Höhe bzw. CRS hat noch keinen separaten visuellen Eingabeeditor**; eine angebliche neue UI-Freigabe wird nicht behauptet.

## Noch offen

KAB-0326/0328 sind technisch im Modell vorbereitet, aber **kein bestätigter globaler Höhenbezug oder EPSG-Transformationsalgorithmus** vorhanden. Änderungen am echten Projekt erfordern Original-Vermessungsdaten und eigene UI-Sichtprüfung. Die M01–M10-Quell-/Modell-/Negativschritte und M13/M15-JSON-/Prüffälle sind durch Code und Tests belegt; weitere UI-/Browserabnahme (M11/12/14/16) bleibt offen.
