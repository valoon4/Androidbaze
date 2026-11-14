# Kabelsalat · Globale XYZ-Segmentansicht · v2.0.10-beta.1

**Stand 14.11.2026 · Großaufgabe KAB-0375 (technischer Teil).**

Das neue Werkzeug transformiert **existierende lokale Kabelsegmente** aus Schacht-Snapshots in globale **schematische Modellkoordinaten [X, Y, Z] in Millimetern**. Der Clou ist die explizite Trennung zwischen *bekannten lokalen Modellpunkten* und *unbekannten physischen Übergängen zwischen Schächten*.

## Bedienung für Bro

Im **Profi-Modus → Netz-Kabelwege** einen bereits gespeicherten schachtübergreifenden Kabelweg mit lokalen Segmenten auswählen und **„XYZ-Segmente berechnen“** drücken. Die App zeigt die modellierten lokalen Teilstücke in der Netzkarte und im schematischen Höhenprofil; der CSV-Export enthält jeden vorhandenen XYZ-Modellpunkt.

Die App zeigt jeden Schachtübergang **rot als offene Lücke** in der Aufstellung. Sie zieht **keine durchgehende 3D-Kabellinie** über unbekannte Durchführungen oder fremde Schächte. Das Werkzeug ist vollständig read-only: Es verändert keine Rohr-/Kabel-IDs, Schachtpositionen, Originalprojekte oder gesicherte Snapshots. Bei Änderungen an Kabeln, Schächten und Netzlinks wird die berechnete Vorschau verworfen.

### Annahmen und Sicherheitsgrenzen

| Eingabe | Transformation | Verifizierungsstatus |
|---|---|---|
| `snapshot.walls[].holes[].u/v` | Normierte Werte × Schachtabmessungen → lokaler Modellpunkt | `schematic` |
| `snapshot.cables[].route[]` | Lokale Kabel-Wegpunkte [x,y,z] in **mm** | Gespeichertes Modell, nicht Aufmaß |
| `network.shafts[].x/z` | [localX + shaftX, **localY**, localZ + shaftZ] | Schematische globale Position; keine Höhenverschiebung erfunden |
| `sourceInfo.sourceVersion = PDF V2…` | PDF-V2-Wandkoordinaten exakt wie in `pdf-geometry.js` (einschl. SW1/SW3-Umkehr und 24-mm-Wandversatz) | **Kein** externer Messnachweis |
| Legacy-HTML ohne PDF-V2 | Vorhandene Legacy-Wandorientierung entsprechend `app.js` | Originaldaten bleiben unverändert |
| Schachtübergang | Link-IDs und Luftlinien-Lücke als *ungeprüft* auflisten | **Nie** als echten Kabelanschluss interpolieren oder freigeben |
| Modell-Kabellänge | Summe **lokaler Polygonzugstrecken** (nur wenn alle lokalen Enden bekannt) | Echte Leitungslänge grundsätzlich unbekannt |

**Grenzen:** maximal 30 Schächte, 150 Links, 60 Kabelsegmente je Route, 12 Wegpunkte je lokalem Kabel. Umrechnung in globale XYZ-Punkte erfolgt ohne Drehwinkel eines Schachts – solche Schachtdrehwinkel liegen im aktuellen Netzmodell **nicht vor**. Unbekannte Querschnitts-/Durchführungs-/Wandversatz-Maße, Bogenradien, echte Rohrlängen und lokale Kabelenden werden **nicht** hinzugefügt oder geraten.

Die Ausgabe ist **keine** durchgehende, fachlich bestätigte XYZ-Kabeltrasse; die ursprüngliche Anforderung KAB-0375 bleibt deshalb bis zu richtigen globalen Quellanschlüssen und Bros Prüfung fachlich offen.

## QA und Abnahme

- [Unit-Test: globale XYZ-Transformationen, PDF/Legacy-Wandachsen, Fehlerfälle und CSV-Sicherheit](./tests/network-world-route.test.js).
- [Chromium-Browser: 3-Schacht-Route, Höhenprofil, CSV, unbekannte Übergänge, beschädigte Loch-ID, Original-PDF-Wiederherstellung](./tests/browser-world-route.js).
- [Bestehender Graph-/Kontinuitäts-Audit](./NETWORK_ROUTE_PLANNER.md) und [Bro-Review #73](https://github.com/valoon4/Androidbaze/issues/73).
- [GitHub Actions: Sicht- und Browsertests](https://github.com/valoon4/Androidbaze/actions/workflows/kabelsalat-visual.yml), [Build](https://github.com/valoon4/Androidbaze/actions/workflows/kabelsalat.yml).

### Was Bro konkret anschauen sollte

1. Zeichnet die Netzkarte in der fiktiven dreifachen Kabelroute die lokalen Teilstücke getrennt voneinander?
2. Zeigt das Höhenprofil die **Modellhöhen** je Segment, ohne eine durchgehende Verbindung über Schächte zu behaupten?
3. Werden bei fehlender Rohr-ID **Länge unbekannt** und die entsprechende offene Schacht-Lücke gemeldet?
4. Welche **echten dokumentierten Leitungsverläufe, Eintritts-/Austrittsrohre und Höhenbezüge** sind für eine spätere fachliche XYZ-Rekonstruktion überhaupt verfügbar?

**Externe Fachfreigabe/Originalpläne sind weiterhin erforderlich.**
