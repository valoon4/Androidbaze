# Kabelsalat 2.0 Beta – AVL0116 / 13_T_021 und 13_U_028
Stand: 09.11.2026. Diese Version ist ein geometrisches **Konzept**, keine Ausführungs- oder Brandschutzfreigabe.

## Umsetzung
- Neue rein geometrische Logik in `construction-v2.js` und WebGL-Anbindung in `app.js`.
- Gegenüberliegende, einander zugeordnete Leerrohre: direkte, knickfreie 3D-Kabelstrecke (bei unterschiedlichen Höhen schräg, nicht künstlich geknickt).
- Seitliche Endpunktpaare: weiche Bézier-Kabelführung ohne harte Ecke. Der angenäherte kleinste geometrische Krümmungsradius wird mit einem vom Anwender hinterlegten Projekt-Rmin verglichen. Hersteller-Rmin ist nicht aus den Quelldaten bekannt.
- Im **Basic-Modus** werden für OLA/MS alte illustrative Import-Wegpunkte nicht zur Kabelführung gezwungen; im **Profi-Modus** bleiben explizite bearbeitbare Wegpunkte und die bisherigen Bearbeitungsfunktionen erhalten. Die gespeicherten Originaldaten bleiben unverändert.
- Zwei echte 3D-Kanäle als separate sichtbare Konstruktionsvarianten: **OLA/Rot: Variante A** mit fünf gestapelten horizontalen Platten; **MS/Blau: Variante B** mit vier senkrechten Stützelementen, durchgehender Kanalbodenplatte und bündigen Endplatten mit echten Kabeldurchführungen.
- Aus bestehenden Portkoordinaten berechnete Schutzbereiche werden nur dargestellt, wenn gegenüberliegende Anschlüsse vollständig sind, ausreichend Platz verbleibt und fremde Öffnungen nicht durch Endplatten verdeckt würden. Andernfalls bleibt die geschlossene Einhausung aus und ein offener Prüfhinweis sichtbar.
- Varianten und Schutzbauwerke sind **darstellend**, mit schematischen Platten-/Stützenstärken. Bestehende Schachtwanddaten, Rohrzuordnung, Projekt-JSON und die alten Pitch-Originaleditoren werden nicht überschrieben.
- Browser, Electron/Windows und Offline-Einzeldatei berücksichtigen das neue Modul. `tests/construction-v2.test.js` enthält Tests für Varianten, Kabelwege, Biegeradiuswarnungen, Fremdleerrohre und Original-Referenzwerte.

## Datenlage
- `13_T_021`: Es lassen sich unter Verwendung der **illustrativen** Referenzkoordinaten die sechs Kanalplatten plus fünf Auflagerplatten für OLA und die sechs Kanalplatten plus vier Stützplatten für MS berechnen. Keine vermessenen Maße.
- `13_U_028` / Original-HTML: Höhenabweichungen bzw. seitliche MS-Abgänge verhindern derzeit eine belastbare waagerechte durchgehende Einhausung. Keine frei erfundene 3D-Lösung.
- `13_U_028` / neuere PDF-V2-Belegung: 86 Kabelobjekte, 66 davon ohne zweites bestätigtes Ende. Fehlende Endpunkte bleiben sichtbar offen. Rohrpositionen sind relativ aus der PDF-Grafik; keine vermessene 3D-Geometrie.
- **Widerspruch in den Konstruktionsvorgaben**: Der ältere Chat nennt 10 cm Abstand zur Wahl von Variante A/B; das neuere Bild vom 09.11.2026 nennt 75 mm. Die aktuelle Darstellung verwendet die gewünschte **Zuordnung nach System und Bild** (OLA=A, MS=B), nicht eine behauptete baurechtliche Prüfung des Grenzwerts.

## Vor technischer Freigabe erforderlich
Reale Schacht-Innenmaße, Rohrdurchmesser und Einbauhöhen, exakte Kabel-Endpunktzuordnung, Hersteller-Mindestbiegeradien, zulässige Platten- und Stützenabmessungen, lastabtragende Anschlüsse, befugte Brandschutzplanung und der verbindliche AVL0116-Wortlaut. Insbesondere die Kollisionen mit fremden Rohren müssen nach Aufmaß neu bewertet werden.

## Sicherung und Freigabe
Die ursprünglichen 1.9.4-Pitch-Dateien `pitch/13_T_021.html`, `pitch/13_U_028.html` und die beiden ergänzenden 1.9.4-Kopien wurden nicht geändert. Die V2.0-Hauptanwendung ist als **Beta** gekennzeichnet; ein grüner GitHub-Pages-Build und ein Browser-/End-to-End-Test müssen separat bestätigt werden.

## Ergänzung v2.0.1 · Schachtwandbelegung und Rohrabstände
Die kanonisch neu ladbare `13_U_028`-PDF-V2-Belegung enthält SW1/2/3/4 = 29/13/26/23 Rohre. IDs, DN, Trassen, Kabelbelegungen und Original-PDF-Mittelpunkte bleiben erhalten. Die 3D-/Editor-Demo verdichtet die schematischen Mittelpunktsabstände auf 62 %; die Originaldaten bleiben unter `sourceU/sourceV` erhalten und die PDF-Vergleichsansicht nutzt sie unverändert. **Keine maßhaltige Abstandsfestlegung.** Alte lokal gespeicherte Projekte werden nicht automatisch überschrieben; die aktuelle Belegung kann mit Sicherheitsabfrage neu geladen werden.

## Quellabgleich älterer Bro-Chat · 11.11.2026

Aus dem Nutzerupload `dumm.txt` wurden 36 prüfbare Folgeaufgaben KAB-0829–0864 angelegt und bestehende P0-Aufgaben präzisiert. Technische Quellableitung: [TASKS/BRO_CHAT_CROSSWALK.md](./TASKS/BRO_CHAT_CROSSWALK.md).

- **A/B-Korrektur:** Laut älterem Chat wählen **beide** Kabelsysteme die Bauart nach belegtem Abstand **Schachtboden → Unterkante Leerrohr**: A bei bis einschließlich 100 mm (waagerechte Stapelplatten), B bei über 100 mm (senkrechte Stützplatten). Die aktuelle v2.0.1 legt OLA=A und MS=B als **Demo** fest und trifft damit noch **keine korrekte allgemeine Bauartentscheidung**.
- **Widerspruch ausdrücklich offen:** Ein späteres, nicht in diesem Upload enthaltenes Referenzbild vom 09.11.2026 nennt **75 mm**. Bis Originalvorgaben und Fachentscheidung vorliegen: weder 75 noch 100 mm als endgültige Anforderungsgrenze verwenden.
- **Rohrpositionen:** Der Bro-Chat verbietet eigenmächtiges Verschieben vorhandener Rohrmittelpunkte. In v2.0.1 werden für den 3D-/Editor-Demofall die `u/v`-Mittelpunkte auf 62 % verdichtet, während `sourceU/sourceV` die PDF-Originalpositionen bewahren. Diese **Darstellungsänderung darf nicht für reale 3D-Konstruktionsmaße und eine fachliche Leitungsprüfung gehalten werden**; die Trennung ist offen (KAB-0832).
- **Weitere Quellschächte:** Beide Modelle `13_U_028` und `13_T_021` müssen mit ihren jeweiligen Originalbelegungen und interaktiven Funktionen überprüft werden. Die im Chat besprochenen 92-Schacht-PDF/Excel-Auswertungen sind ein **gesonderter Daten- und Berichtsstrang**, dessen frühere Ergebnisse nicht ohne Originalplansatz bestätigt werden können.

**Keine Softwareänderung, technische Abnahme oder AVL0116-Freigabe wurde allein durch das Erfassen dieser Aufgaben durchgeführt.**

## 2.0.2-beta.1 · KAB-0877 – Beton-Wanddarstellung

Die bisherigen extrudierten Wandflächen enthielten bereits kreisförmige Durchbrüche, wurden in der 3D-Demo jedoch stark transparent und mit hervorstehenden Metallhülsen/-kragen gezeigt. Die neue Darstellung nutzt das modulare `concrete-wall.js`, höhere Betondeckkraft in der Referenz-Exploration, einen einheitlichen Betonkörper und dunkler schattierte Lochinnenwände. Die 20-cm-Demohülsen der PDF-Szene entfallen; unsichtbare 3D-Picking-Flächen erhalten die Rohrselektion, farbige Belegungsränder bleiben sichtbar.

**Keine Änderung an Daten und Leitungswegen:** Quellbelegung, `sourceU/sourceV`, Demo-`u/v`, DN, Rohrachsen, Kabelendpunkte und Schachtgeometrie bleiben wie zuvor. **Offen**: 62-%-Schematisierung, reale Rohr-/Lochmaße, finale Betontextur, Bro-Sichtabnahme, fachliche Kontrolle. Details: [TASKS/07-BRO-BETON-ROHR-DM.md](./TASKS/07-BRO-BETON-ROHR-DM.md) und [Issue #70](https://github.com/valoon4/Androidbaze/issues/70).

## 2.0.3-beta.1 · Bros 10er-Entwicklungssprint (12.11.2026)

Die v2.0.2-Betonwand wurde um **dunklere, dezente, deterministisch erzeugte Betontextur**, eigenständig schattierte Durchbrüche und unterschiedlich große explizite Lochdurchmesser erweitert. Das Feld `dn` ist weiterhin nur eine **Nenn-/Modellangabe**, nicht automatisch ein realer Betonloch-Ø. Zusatzfelder `dimensionSpec` enthalten getrennte Maße und Prüfstatus mit Quellenverweis. Datenerkennung bietet ausschließlich lokal verarbeitete, unbestätigte Kandidaten aus beschriftetem TXT-Plantext oder per heuristischer Foto-Kreisfindung; weder trainierte KI noch Binär-PDF/OCR sind implementiert.

**Kritischer Modellfix:** Die PDF-V2-`u/v`-Koordinaten entsprechen wieder den unveränderten `sourceU/sourceV` aus der PDF-Vektorgrafik. Der alte 62-%-Skalierungsfaktor wird nur im separaten **Basic-Demoansichtsmodus** angewendet; im Profi-Editor gelten die unbearbeiteten PDF-Diagrammkoordinaten. Vor der automatischen Rückmigration alter gespeicherter Projekte wird exakt auf unveränderte frühere Demo-Koordinaten geprüft; individuelle Benutzeränderungen werden nicht rückgängig gemacht. **Auch die unveränderten PDF-Punkte sind nicht vermessene XYZ-Geometrie.** 48/78/91-Source-Snapshots und PDF-Vergleichsbilder werden getestet.

**Offene Fach- und UX-Fragen:** Optische Betonabnahme durch Bro, reales Rohr-/Schachtaufmaß, belastbare Rohr-/Lochdaten und Konstruktionsmaße, 75-/100-mm-Grenze, KI-Foto- und Plan-PDF-Erkennung, Montage-/Brandschutzfreigabe. [Nachweis und Taskstatus](./TASKS/SPRINT_2026-11-12_BRO_10.md).
