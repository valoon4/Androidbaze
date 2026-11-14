# Kabelsalat · Netz-Wegfinder v2.0.9-beta.1

**Stand 14.11.2026 · Schematische Planung, keine Ausführungs-/Sicherheitsfreigabe.**

## Anwendung

Im **Profi-Modus → Netzübersicht → Netz-Wegfinder** zwei gespeicherte Schächte wählen, zwischen **kürzester Modell-Luftlinie** und **wenigsten Schachtübergängen** umschalten und auf **Weg suchen** klicken. Die Netzkarte hebt die vorgeschlagene Verbindung **türkis gestrichelt** hervor; wenn möglich kann man eine alternative Route auswählen. Der CSV-Export enthält die Reihenfolge der Schacht-IDs sowie die bestehenden Link-IDs.

| Funktion | Geltungsbereich | Vorsicht |
|---|---|---|
| Kürzester Weg | Graph der **bereits vorhandenen Netzlinks**, Kantenlänge aus X/Z-Punktdistanz in Modell-mm | Nicht die reale Rohr-/Kabeltrasse; unterirdische Hindernisse unbekannt |
| Wenigste Übergänge | Anzahl existierender Schachtlinks | Länge kann größer sein als beim Distanzmodus |
| Alternativen | Pro primärem Kantenlink eine alternative Route durch dessen temporären Ausschluss, bis zu 3 verschiedene Vorschläge | **Keine** Garantie auf vollständige Liste der k-kürzesten Wege |
| Zusammenhang | Verbundene Komponenten, isolierte Schächte, Zyklen, 0-mm-Doppellage | Ohne nachgewiesene Verbindung wird **keine** künstliche Verbindung ergänzt |
| Bestehende Kabelwege | Schematische Linkstrecke, fehlende Kabel-IDs und fehlende Links werden benannt | Gleiche IDs in mehreren Schächten beweisen **keine** physische Kabelkontinuität |
| Quellenschutz | Nur lesende Graphsuche; Projekt-JSON, Originalkoordinaten, Kabelendpunkte und Rohrmaße unverändert | Fachgerechte Vor-Ort-Kontrolle zwingend erforderlich |

## Technische Schnittstelle

`network-route-planner.js` ist ein isoliertes Offline-UMD-Modul, ohne zusätzliche npm-Abhängigkeit oder fremden Webdienst.

- `topology(net)` → Komponenten, Grad, Zyklen, isolierte Schächte und Warnungen.
- `find(net,from,to,{mode,excludeLinks})` → deterministischer Dijkstra-Pfad, oder `status:unreachable`; nur explizit vorhandene Links.
- `alternatives(net,from,to,{mode,max})` → Hauptweg und verschiedene Ein-Link-Sperrvarianten.
- `compareRoute(net,route)` → Dokumentationsauswertung eines bereits gespeicherten Kabelwegs.
- `csv(result)` → UTF-8/BOM/CRLF und Schutz gegen CSV-Formelinjektion.

**Eingabegrenzen:** maximal 30 Schächte, 150 Links, eindeutige Links und IDs, sichere Modell-Ganzzahlen in ±100.000.000 mm. Die App selbst erzwingt diese Obergrenzen beim Einlesen. Ungültige oder widersprüchliche Eingaben schlagen sichtbar fehl; sie werden **nicht** heimlich korrigiert oder als baulich freigegeben behandelt.

### QA und getrennte Datenquellen

| Prüfung | Datei | Inhalt |
|---|---|---|
| Unit-/Grenzfälle | [network-route-planner.test.js](./tests/network-route-planner.test.js) | 4er-Zyklus, Disconnected Graph, Gleichstand, Alternativen, CSV, 30er-Netz, source-immutability |
| Deterministische Graphmatrix | [network-route-fuzz.test.js](./tests/network-route-fuzz.test.js) | **320 synthetische Graphkonfigurationen**, unabhängiger Bellman-Ford-Orakelvergleich; Zahl ist **Testfälle**, keine künstliche KAB-Task-Zählung |
| Echte Browseransicht | [browser-network-path.js](./tests/browser-network-path.js) | Karte, Suchmodus, Alternative, Export, getrennte Komponenten, Browser-Fehler und unverändertes Projekt |
| Gesamte UI | [app.js](./app.js) / [index.html](./index.html) | Vorschauzustand ist **nicht** gespeichert; bei tatsächlicher Projektänderung verworfen |
| Veröffentlichen | [Workflow: Browser](https://github.com/valoon4/Androidbaze/actions/workflows/kabelsalat-pages.yml), [Workflow: Windows](https://github.com/valoon4/Androidbaze/actions/workflows/kabelsalat.yml) | Webversion und Offline-Pakete enthalten dasselbe Wegfindermodul |

**Offene Parent-Tickets:** KAB-0352 (getrennte Netzkomponenten), KAB-0353 (Zyklus), KAB-0355/0356 (skalierte und größere Netzkarten), KAB-0365 (Link ≠ physischer Durchbruch), KAB-0375 (echter globaler XYZ-Kabelverlauf). Insbesondere KAB-0375 wird **nicht** als erledigt markiert: Diese Version berechnet ausschließlich globale X/Z-Modellluftlinien. Weder Y-Höhe noch echte Kabelanschlussgeometrie oder ein gebundener geplanter Rohrverlauf wird ergänzt. Bros fachlicher Sichtvergleich und reale Quellmaße fehlen.

**Reviewfragen an Bro:** Lassen sich die schrittweise getrennten Netzkomponenten und die gestrichelte Alternativroute verständlich unterscheiden? Welche echten Schachtverbindungen sind belegt, und an welchen Stellen existieren nur Plan-Links? Ohne diese Nachweise wird keine Ausführungsfreigabe erteilt.
