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
