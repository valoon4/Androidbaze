# Kabelsalat · 828-Task-Backlog · Teil 1/4: P0 – Quelltreue und Pitch

> **Kanonischer Aufgabenstatus:** Jede `- [ ]`-Zeile ist ein OFFENER, eigenständiger Task. `- [x]` erst nach belegter Abnahme. Keine bestehenden Features rückwirkend als erledigt markieren. Gesamt-IDs: KAB-0001 bis KAB-0828. Quelle: v2.0.1 + AVL0116-Konzept; zusätzliche Bro-Aufgaben folgen als neue IDs.

**Abnahmeregel:** Eine Task-Checkbox darf erst nach konkretem Ergebnis (Code, Test, Quellvergleich oder Screenshot, passend zur Aufgabe), bestandenem Regressionscheck und belegbarer Referenz im Commit/Issue abgehakt werden. Schematische Bauwerte bleiben als schematisch markiert; keine Elektro-/Brandschutzfreigabe behaupten.

## P0-01 · PDF-V2-Referenz und 91 Rohre (KAB-0001–KAB-0018)

**Quelle:** PDF S.21–24, E03, V2_0_STATUS. **Definition of Done:** Je Aufgabe: Quellenabgleich mit Seitenangabe und reproduzierbarer Soll/Ist-Prüfung.

- [ ] **KAB-0001** – PDF-Index für Schacht 13_U_028 eindeutig versionieren
- [ ] **KAB-0002** – SW1-Trasse-1-Rohre 01–05 mit PDF S.21 einzeln abgleichen
- [ ] **KAB-0003** – SW1-Trasse-1-Rohre 06–10 mit PDF S.21 einzeln abgleichen
- [ ] **KAB-0004** – SW1-Trasse-1-Rohre 11–15 mit PDF S.21 einzeln abgleichen
- [ ] **KAB-0005** – SW1-Trasse-1-Rohre 16–21 mit PDF S.21 einzeln abgleichen
- [ ] **KAB-0006** – SW1-Trasse-1-Rohre 22–27 mit PDF S.21 einzeln abgleichen
- [ ] **KAB-0007** – SW1-Trasse-2-Rohre 01–02 mit PDF S.21 abgleichen
- [ ] **KAB-0008** – SW2-Trasse-1-Rohre 01–05 mit PDF S.22 abgleichen
- [ ] **KAB-0009** – SW2-Trasse-1-Rohre 06–11 mit PDF S.22 abgleichen
- [ ] **KAB-0010** – SW2-Trasse-3-Rohre 01–02 mit PDF S.22 abgleichen
- [ ] **KAB-0011** – SW3-Trasse-1-Rohre 01–05 mit PDF S.23 abgleichen
- [ ] **KAB-0012** – SW3-Trasse-1-Rohre 06–10 mit PDF S.23 abgleichen
- [ ] **KAB-0013** – SW3-Trasse-1-Rohre 11–15 mit PDF S.23 abgleichen
- [ ] **KAB-0014** – SW3-Trasse-1-Rohre 16–20 mit PDF S.23 abgleichen
- [ ] **KAB-0015** – SW3-Trasse-1-Rohre 21–26 mit PDF S.23 abgleichen
- [ ] **KAB-0016** – SW4-Trasse-1-Rohre 01–12 mit PDF S.24 abgleichen
- [ ] **KAB-0017** – SW4-Trasse-1-Rohre 13–23 mit PDF S.24 abgleichen
- [ ] **KAB-0018** – Vollständigkeitsprüfung 29/13/26/23 und eindeutige Leerrohr-IDs automatisieren

## P0-02 · Schachtwandorientierung und Transformation (KAB-0019–KAB-0036)

**Quelle:** PDF S.4, E01, A01. **Definition of Done:** Koordinatenbeispiel, inverse Transformation und 2D/3D-Regression pro Änderung.

- [x] **KAB-0019** – SW1-Stirnwand auf korrekte Innen-/Außennormale prüfen · Nachweis: https://github.com/valoon4/Androidbaze/commit/8c384b567e7842adcfcab1c4aeba8938835227c2
- [x] **KAB-0020** – SW2-rechte Wand auf korrekte Innen-/Außennormale prüfen · Nachweis: https://github.com/valoon4/Androidbaze/commit/8c384b567e7842adcfcab1c4aeba8938835227c2
- [x] **KAB-0021** – SW3-Gegenwand auf korrekte Innen-/Außennormale prüfen · Nachweis: https://github.com/valoon4/Androidbaze/commit/8c384b567e7842adcfcab1c4aeba8938835227c2
- [x] **KAB-0022** – SW4-linke Wand auf korrekte Innen-/Außennormale prüfen · Nachweis: https://github.com/valoon4/Androidbaze/commit/8c384b567e7842adcfcab1c4aeba8938835227c2
- [x] **KAB-0023** – Rechtshändige Weltachsen für PDF-V2-Schächte dokumentieren · Nachweis: https://github.com/valoon4/Androidbaze/commit/8c384b567e7842adcfcab1c4aeba8938835227c2
- [ ] **KAB-0024** – Lokale U/V-Koordinaten für SW1 eindeutig definieren
- [ ] **KAB-0025** – Lokale U/V-Koordinaten für SW2 eindeutig definieren
- [ ] **KAB-0026** – Lokale U/V-Koordinaten für SW3 eindeutig definieren
- [ ] **KAB-0027** – Lokale U/V-Koordinaten für SW4 eindeutig definieren
- [ ] **KAB-0028** – Wandspiegelung bei Blick von Schachtinnenseite testen
- [ ] **KAB-0029** – Wandspiegelung bei Blick von Schachtaußenseite testen
- [ ] **KAB-0030** – Schacht-Eckpunkte zwischen den vier Wandgeometrien synchron halten
- [ ] **KAB-0031** – 2D-Leerrohrposition in exakte 3D-Weltposition umrechnen
- [ ] **KAB-0032** – 3D-Leerrohrposition invertiert in Wandkoordinaten umrechnen
- [ ] **KAB-0033** – Wandauswahl mit orthografischer Einzelansicht synchronisieren
- [ ] **KAB-0034** – Kamera-Zielpunkt je Wand an tatsächliches Wandzentrum binden
- [ ] **KAB-0035** – Wanddurchbruch-Tiefe ohne vertauschte Normalen zeichnen
- [ ] **KAB-0036** – Regression für SW1–SW4 gegen Original-HTML-Achskonvention isolieren

## P0-03 · Rohrabstände und Wandbelegung in der Demo (KAB-0037–KAB-0054)

**Quelle:** v2.0.1, E04. **Definition of Done:** Schematische Darstellung klar markiert, keine Überlappung; Original- und Planungskoordinaten dürfen durch rein visuelle Rohrverdichtung nicht eigenmächtig verändert werden.

- [x] **KAB-0037** – Original-PDF-Mittelpunkte sourceU/sourceV im Projekt-Roundtrip absichern · Nachweis: https://github.com/valoon4/Androidbaze/commit/aaa96ab73d5ab53dea97eaa3d4456d4a1a5ae638
- [x] **KAB-0038** – Demo-Verdichtung als optionale Ansichtstransformation von Quell- und Planungskoordinaten trennen, ohne Rohre im Datenmodell zu verschieben · Nachweis: https://github.com/valoon4/Androidbaze/commit/aaa96ab73d5ab53dea97eaa3d4456d4a1a5ae638
- [x] **KAB-0039** – Historische 62-Prozent-Demo-Verdichtung nur in explizit schematischer Vorschau anbieten und im Fachmodell deaktivieren · Nachweis: https://github.com/valoon4/Androidbaze/commit/aaa96ab73d5ab53dea97eaa3d4456d4a1a5ae638
- [ ] **KAB-0040** – Unabhängige horizontale Rohrabstandsregel je Schachtwand prüfen
- [ ] **KAB-0041** – Unabhängige vertikale Rohrabstandsregel je Schachtwand prüfen
- [ ] **KAB-0042** – Sonderabstände für Trasse 2 auf SW1 berücksichtigen
- [ ] **KAB-0043** – Sonderabstände für Trasse 3 auf SW2 berücksichtigen
- [x] **KAB-0044** – DN-160-Rohre gegen direkt benachbarte DN-110-Rohre prüfen · Nachweis: https://github.com/valoon4/Androidbaze/commit/8c384b567e7842adcfcab1c4aeba8938835227c2
- [x] **KAB-0045** – DN-75-Rohr auf SW2-Trasse-3 auf Wandrandabstand prüfen · Nachweis: https://github.com/valoon4/Androidbaze/commit/8c384b567e7842adcfcab1c4aeba8938835227c2
- [x] **KAB-0046** – DN-63-Rohre der SW1-Trasse-2 auf Randabstand prüfen · Nachweis: https://github.com/valoon4/Androidbaze/commit/8c384b567e7842adcfcab1c4aeba8938835227c2
- [x] **KAB-0047** – Mindestlichte zwischen zwei Kreisöffnungen berechnen · Nachweis: https://github.com/valoon4/Androidbaze/commit/8c384b567e7842adcfcab1c4aeba8938835227c2
- [ ] **KAB-0048** – Kollisionen bei engerer Vorschau sichtbar kennzeichnen
- [x] **KAB-0049** – Schematische Rohrpositionen nicht als vermessene Millimeter exportieren · Nachweis: https://github.com/valoon4/Androidbaze/commit/c24a6f3818ae0665a1e5c7aff0b03344e02212eb
- [x] **KAB-0050** – PDF-Originaldarstellung bei Parameteränderung unangetastet lassen · Nachweis: https://github.com/valoon4/Androidbaze/commit/79a3c69b6e145c1836ac8032cea2075d4c486f7f
- [ ] **KAB-0051** – Rohrabstand-Regler mit Undo/Redo koppeln
- [ ] **KAB-0052** – Rohrabstand-Regler ohne Veränderung von Kabelanschluss-IDs umsetzen
- [ ] **KAB-0053** – Geometrie-Vergleich vor und nach Abstandsanpassung anbieten
- [ ] **KAB-0054** – Schachtwandbelegung nach 2.0.1 mit Bro anhand Screenshot abnehmen

## P0-04 · Kabelbelegung der Leerrohre (KAB-0055–KAB-0072)

**Quelle:** PDF S.21–24, N04, D02. **Definition of Done:** Kabelzahl je Rohr vor und nach Änderung gleich; jede Zuordnung trägt Quelle oder Offen-Vermerk.

- [ ] **KAB-0055** – 106 dokumentierte Kabelvorkommen unabhängig nachzählen
- [ ] **KAB-0056** – 86 importierte Kabelobjekte gegen belegte Rohrenden prüfen
- [ ] **KAB-0057** – SW1-LR21 mit MS-SNG-0038 und NS-HUP-0203 prüfen
- [ ] **KAB-0058** – SW1-LR27 mit MS-SNG-0056 prüfen
- [ ] **KAB-0059** – SW2-LR05 mit MS-SNG-0056 prüfen
- [ ] **KAB-0060** – SW3-LR26 mit MS-SNG-0038 prüfen
- [ ] **KAB-0061** – SW2-LR09 mit OLSP 2060–2063 prüfen
- [ ] **KAB-0062** – SW2-LR10 mit OLSP 2960–2963 prüfen
- [ ] **KAB-0063** – SW2-Trasse-3-LR01 mit fünf EEA-Vorkommen prüfen
- [ ] **KAB-0064** – SW2-Trasse-3-LR02 ausdrücklich leer belassen
- [ ] **KAB-0065** – SW3-LR18 mit sechzehn OLSP-Vorkommen prüfen
- [ ] **KAB-0066** – Mehrfachbelegung in 2D als erkennbare Segmente rendern
- [ ] **KAB-0067** – Mehrfachbelegung in 3D auf einzelne Kabel auswählbar machen
- [ ] **KAB-0068** – Rohr-Tooltip um belegte Kabelnamen erweitern
- [ ] **KAB-0069** – Doppelte Kabelnamen ohne unbelegte Pseudo-Verbindung auflösen
- [ ] **KAB-0070** – Unbenannte Kabelobjekte mit eindeutigen provisorischen IDs kennzeichnen
- [ ] **KAB-0071** – Quellkonflikt SW2-Trasse-3 zwischen PDF S.13 und S.22 dokumentieren
- [ ] **KAB-0072** – Änderung an Rohrbelegung mit prüfbarem Diff und Warnung sichern

## P0-05 · Kabelendpunkte und Zuordnung (KAB-0073–KAB-0090)

**Quelle:** E05, N06, PDF S.21–24. **Definition of Done:** Nur quellenbelegte Paare verbinden; offene Verläufe bleiben sichtbar als offen markiert.

- [ ] **KAB-0073** – Endpunktreferenzen auf stabile Wand- und Rohr-IDs normalisieren
- [ ] **KAB-0074** – MS-SNG-0038 mit SW1-LR21 und SW3-LR26 abgleichen
- [ ] **KAB-0075** – MS-SNG-0056 mit SW1-LR27 und SW2-LR05 abgleichen
- [ ] **KAB-0076** – 66 offene Kabelendpunkte in Referenz als ungeklärt ausweisen
- [ ] **KAB-0077** – Fehlende Kabelziele niemals aus ähnlichen Namen automatisch erfinden
- [ ] **KAB-0078** – Quellgleichheit von Kabelnamen vor Paarung normalisieren
- [ ] **KAB-0079** – Mehrdeutige gleiche Namen als Auswahlkonflikt anzeigen
- [ ] **KAB-0080** – Endpunkte je Kabel mit A-/B-Richtung unterscheidbar anzeigen
- [ ] **KAB-0081** – Zwei Kabel mit identischem Rohrpaar separat modellieren
- [ ] **KAB-0082** – Unbelegte Pseudorohr-Marker aus der Endpunktliste ausschließen
- [ ] **KAB-0083** – Endpunkt bei gelöschtem Rohr geordnet entkoppeln
- [ ] **KAB-0084** – Endpunkt bei verschobenem Rohr ohne ID-Verlust mitziehen
- [ ] **KAB-0085** – Quellseiten pro A- und B-Ende getrennt speichern
- [ ] **KAB-0086** – Endpunktbestätigung durch Fachanwender protokollieren
- [ ] **KAB-0087** – Fehlerhafte Schachtwand-ID bei Import vor Rendering abfangen
- [ ] **KAB-0088** – Endpunktvorschläge mit Vertrauens- und Quellenhinweis anzeigen
- [ ] **KAB-0089** – Offene Enden in der 3D-Präsentation mit sichtbarer Kappe versehen
- [ ] **KAB-0090** – Endpunkt-Zuordnungsdiff für manuelle Abnahme exportieren

## P0-06 · Gerade und gebogene 3D-Kabelwege (KAB-0091–KAB-0108)

**Quelle:** E06, E07, V2_0_STATUS. **Definition of Done:** Geraden bleiben gerade; Kurven stetig; Quelle und offene Geometrie nachvollziehbar.

- [ ] **KAB-0091** – Gegenüberliegende Endpunkte ohne Zwischenpunkt durch Gerade verbinden
- [ ] **KAB-0092** – Schräg auf verschiedenen Höhen liegende Endpunkte ohne Knick verbinden
- [ ] **KAB-0093** – Seitliche Wandpaare mit stetiger Tangente am Eintritt verbinden
- [ ] **KAB-0094** – Wechsel SW1 zu SW2 auf Bezier-Geometrie prüfen
- [ ] **KAB-0095** – Wechsel SW1 zu SW4 auf Bezier-Geometrie prüfen
- [ ] **KAB-0096** – Wechsel SW2 zu SW3 auf Bezier-Geometrie prüfen
- [ ] **KAB-0097** – Wechsel SW3 zu SW4 auf Bezier-Geometrie prüfen
- [ ] **KAB-0098** – Wegpunkte im Profi-Modus nach Einfügereihenfolge sortieren
- [ ] **KAB-0099** – Explizite manuelle Wegpunkte vor Auto-Routing priorisieren
- [ ] **KAB-0100** – Alte Demo-Kurven nur im Präsentationsmodus überschreiben
- [ ] **KAB-0101** – Eingangsrichtung am Quellrohr geometrisch definieren
- [ ] **KAB-0102** – Ausgangsrichtung am Zielrohr geometrisch definieren
- [ ] **KAB-0103** – Ungeklärte Endpunkte als kurze Einzelstücke belassen
- [ ] **KAB-0104** – Selbstüberschneidungen von Kurven erkennen
- [ ] **KAB-0105** – Zu nahe durch Fremdrohre laufende Kabel warnend markieren
- [ ] **KAB-0106** – Kabellänge aus tatsächlicher 3D-Kurve berechnen
- [ ] **KAB-0107** – Geometriestress mit extrem nahen Endpunkten testen
- [ ] **KAB-0108** – 3D-Wegevergleich Original-HTML versus PDF-V2 dokumentieren

## P0-07 · Biegeradien und Kabeltypen (KAB-0109–KAB-0126)

**Quelle:** E07, D02, D07. **Definition of Done:** Herstellerwerte nur mit Beleg; ungültige Radien ablehnen und Konflikte reproduzierbar testen.

- [ ] **KAB-0109** – Mindestbiegeradius als optionale Kabeltyp-Eigenschaft modellieren
- [ ] **KAB-0110** – Herstellerangabe samt Datenblatt-Version am Kabeltyp speichern
- [ ] **KAB-0111** – Biegeradius für gerade Segmente als unkritisch behandeln
- [ ] **KAB-0112** – Krümmungsminimum entlang Bezier-Kurve numerisch bestimmen
- [ ] **KAB-0113** – Krümmungsminimum entlang Catmull-Rom-Kurve numerisch bestimmen
- [ ] **KAB-0114** – Knoten zwischen zwei Teilkurven auf Tangentenknick prüfen
- [ ] **KAB-0115** – Kabeldurchmesser und Biegeradius-Eingabe getrennt validieren
- [ ] **KAB-0116** – Kabeltyp ohne belegten Hersteller-Rmin als ungeprüft markieren
- [ ] **KAB-0117** – Radiuswarnung in Millimetern samt betroffener Stelle anzeigen
- [ ] **KAB-0118** – Konflikt zwischen Projekt-Rmin und Hersteller-Rmin transparent entscheiden
- [ ] **KAB-0119** – Nicht einhaltbaren Radius als Problem statt Auto-Korrektur ausgeben
- [ ] **KAB-0120** – Bei geänderter Rohrposition alle betroffenen Radien neu berechnen
- [ ] **KAB-0121** – Einzelkabel-Biegeradius im 3D-Kabeltooltip anzeigen
- [ ] **KAB-0122** – Tests für nahezu geraden Bogen ohne Zahleninstabilität ergänzen
- [ ] **KAB-0123** – Tests für extrem engen rechtwinkligen Richtungswechsel ergänzen
- [ ] **KAB-0124** – Tests für falschen Null-/Negativradius ergänzen
- [ ] **KAB-0125** – Gespeicherten Radius nach JSON-Reimport bytegetreu erhalten
- [ ] **KAB-0126** – Biegeradiusbericht mit Quelle und noch offener Fachfreigabe ergänzen

## P0-08 · OLA-Demofall: roter Kanal mit historischer A-Darstellung (KAB-0127–KAB-0144)

**Quelle:** V2_0_STATUS, E08. **Definition of Done:** OLA bleibt ein eigenständiger Kanal. Die fünf horizontalen Stapelplatten sind ein bisheriger **Demofall**, keine feste OLA-Regel. Bauart A/B ist abhängig von belegter Höhenvorgabe; bei fehlendem Aufmaß bleibt sie ungeklärt.

- [ ] **KAB-0127** – Für OLA sowohl Bauart A als auch B anhand bestätigter Höhenkriterien ermöglichen, statt OLA fest Bauart A zuzuweisen
- [ ] **KAB-0128** – OLA-Rot als eigene Konstruktion unabhängig von MS rendern
- [ ] **KAB-0129** – Fünf gestapelte horizontale Auflagerplatten als historischen OLA-Demofall testen; reale Stapelhöhe nur aus belegten Maßen ableiten
- [ ] **KAB-0130** – OLA-Bodenplatte als eigenständiges Objekt erzeugen
- [ ] **KAB-0131** – OLA-Deckelplatte als eigenständiges Objekt erzeugen
- [ ] **KAB-0132** – OLA-linke Seitenplatte als eigenständiges Objekt erzeugen
- [ ] **KAB-0133** – OLA-rechte Seitenplatte als eigenständiges Objekt erzeugen
- [ ] **KAB-0134** – OLA-vordere Stirnplatte als eigenständiges Objekt erzeugen
- [ ] **KAB-0135** – OLA-hintere Stirnplatte als eigenständiges Objekt erzeugen
- [ ] **KAB-0136** – OLA-Stirnöffnungen um belegte Kabelanschlüsse ausstanzen
- [ ] **KAB-0137** – OLA-Kanalmittellinie aus echten Kabelendpunkten ableiten
- [ ] **KAB-0138** – OLA-Kanalhöhenlage nicht aus nicht belegten Fantasiewerten ableiten
- [ ] **KAB-0139** – OLA-Auflager ohne Überdeckung von Fremdleerrohren positionieren
- [ ] **KAB-0140** – OLA-Kanalöffnung gegen Außendurchmesser der Kabel prüfen
- [ ] **KAB-0141** – OLA-Plattenfarbe unabhängig von Kabelsystemfarbe einstellen
- [ ] **KAB-0142** – OLA-Transparenz im Pitchmodus zwischen zwei Stufen schalten
- [ ] **KAB-0143** – OLA-Konstruktion bei fehlendem Endpunkt ausdrücklich ausblenden
- [ ] **KAB-0144** – OLA-Variante-A-Screenshot mit Bros rotem Referenzkasten vergleichen

## P0-09 · MS-Demofall: blauer Kanal mit historischer B-Darstellung (KAB-0145–KAB-0162)

**Quelle:** V2_0_STATUS, E08. **Definition of Done:** MS bleibt unabhängig von OLA. Vier Stützen sind nur der historische **Demofall**, nicht die feste MS-Regel. Bauart A/B richtet sich nach bestätigtem Höhenkriterium; offene Schwellen dürfen nicht als verbindlich ausgegeben werden.

- [ ] **KAB-0145** – Für MS sowohl Bauart A als auch B anhand bestätigter Höhenkriterien ermöglichen und MS getrennt von OLA halten
- [ ] **KAB-0146** – MS-Blau als eigenes Konstruktionsmodell mit IDs rendern
- [ ] **KAB-0147** – Drei bis vier vertikale Stützelemente je nach belegter Kanallänge vorsehen; vier als bisherigen MS-Demofall absichern
- [ ] **KAB-0148** – Durchgehende MS-Kanalbodenplatte erzeugen
- [ ] **KAB-0149** – MS-Deckelplatte eigenständig modellieren
- [ ] **KAB-0150** – MS-linke Seitenplatte eigenständig modellieren
- [ ] **KAB-0151** – MS-rechte Seitenplatte eigenständig modellieren
- [ ] **KAB-0152** – MS-vordere Stirnplatte eigenständig modellieren
- [ ] **KAB-0153** – MS-hintere Stirnplatte eigenständig modellieren
- [ ] **KAB-0154** – MS-Stirnöffnungen pro belegtem Kabel ausstanzen
- [ ] **KAB-0155** – Höhenlage der MS-Stützen an Bodenplatte binden
- [ ] **KAB-0156** – MS-Gesamtkonstruktion bei seitlichen Anschlüssen kontrolliert sperren
- [ ] **KAB-0157** – MS-Kanal nicht in Fremdrohr-Öffnungen hineinbauen
- [ ] **KAB-0158** – MS-Farbcode von Plattenmaterial getrennt handhaben
- [ ] **KAB-0159** – MS-Durchblickansicht für Innenräume der Einhausung testen
- [ ] **KAB-0160** – MS-Variante-B-Querschnitt in 2D lesbar zeichnen
- [ ] **KAB-0161** – MS-Verbindung 0038 auf geschlossene Plattengeometrie testen
- [ ] **KAB-0162** – MS-Verbindung 0056 auf geschlossene Plattengeometrie testen

## P0-10 · Einzelplatten und Durchführungen (KAB-0163–KAB-0180)

**Quelle:** E09, E10. **Definition of Done:** Änderungen je Platte persistieren; Bohrungen berühren keine Fremdöffnungen.

- [ ] **KAB-0163** – Einzelplatten-ID von Gruppen-ID unabhängig machen
- [ ] **KAB-0164** – Plattenmaterial aus dokumentierter Materialliste wählen
- [ ] **KAB-0165** – Plattendicke pro Platte getrennt bearbeiten
- [ ] **KAB-0166** – Plattenbreite pro Platte getrennt bearbeiten
- [ ] **KAB-0167** – Plattenhöhe pro Platte getrennt bearbeiten
- [ ] **KAB-0168** – Plattenlage als 3D-Translation editieren
- [ ] **KAB-0169** – Plattenlage als 3D-Rotation editieren
- [ ] **KAB-0170** – Platten-Deckkraft als reine Ansichtseigenschaft behandeln
- [ ] **KAB-0171** – Geänderte Platte nach Speichern/Laden exakt wiederherstellen
- [ ] **KAB-0172** – Stirnplattenlöcher aus Kabelmittelpunkt und Radius ableiten
- [ ] **KAB-0173** – Überlagerte Kabeldurchführungen geometrisch vereinigen
- [ ] **KAB-0174** – Kabeldurchführung am Plattenrand gesondert warnen
- [ ] **KAB-0175** – Plattenstoß zwischen Boden und Seiten auf Spalt prüfen
- [ ] **KAB-0176** – Plattenstoß zwischen Seiten und Deckel auf Spalt prüfen
- [ ] **KAB-0177** – Plattenstoß zur Stirnplatte auf Spalt prüfen
- [ ] **KAB-0178** – Blech-/Plattenkollision mit Schachtwand sichtbar machen
- [ ] **KAB-0179** – Platten nach verdeckt und transparent einzeln selektieren
- [ ] **KAB-0180** – Änderungsdiff einer Platte in den Variantenvergleich aufnehmen

## P0-11 · Auflager, Stützen und Befestigungen (KAB-0181–KAB-0198)

**Quelle:** E11, AVL0116 Konzept. **Definition of Done:** Unbestätigte Statik bleibt unbestätigt; Bauteile korrekt zugeordnet und editierbar.

- [ ] **KAB-0181** – Auflager-ID je Bauteil stabil vergeben
- [ ] **KAB-0182** – Auflager mit OLA- oder MS-System verknüpfen
- [ ] **KAB-0183** – Freistehende Auflager ohne Systemzuordnung gesondert markieren
- [ ] **KAB-0184** – Wandkonsole mit konkreter Schachtwand referenzieren
- [ ] **KAB-0185** – Konsolenlage in lokalen Wandkoordinaten editieren
- [ ] **KAB-0186** – Konsolenlage im 3D-Grundriss editieren
- [ ] **KAB-0187** – Auflager-Abmessungen in Millimetern getrennt speichern
- [ ] **KAB-0188** – Stützenunterseite gegen Schachtboden prüfen
- [ ] **KAB-0189** – Stützenoberseite gegen Kanalboden prüfen
- [ ] **KAB-0190** – Stütze auf Kollision mit Kabelrohren prüfen
- [ ] **KAB-0191** – Auflagerfolge entlang Kanalachse auswählbar machen
- [ ] **KAB-0192** – Befestigungsart als offene Prüf-Eigenschaft führen
- [ ] **KAB-0193** – Unbestätigte Tragfähigkeit deutlich kennzeichnen
- [ ] **KAB-0194** – Eingegebene Tragfähigkeitsquelle dokumentieren
- [ ] **KAB-0195** – Auflager in 2D-Wandansicht maßstäblich projizieren
- [ ] **KAB-0196** – Auflager in 3D ohne Blickrichtungsverwechslung hervorheben
- [ ] **KAB-0197** – Auflager beim Löschen einer Baugruppe sicher entkoppeln
- [ ] **KAB-0198** – Vergleich von OLA-Stapelplatten und MS-Stützen im Pitch dokumentieren

## P0-12 · Pitchansicht und Bros Feedback (KAB-0199–KAB-0216)

**Quelle:** V2_0_STATUS, EUGI_MODE_ACCEPTANCE. **Definition of Done:** In echtem Chromium überprüft, 2D/3D-Darstellung für Bro reproduzierbar; Feedback dokumentiert.

- [ ] **KAB-0199** – Präsentieren startet den korrekten PDF-V2-Schacht
- [ ] **KAB-0200** – Präsentieren zeigt 91 physische Rohre statt Planmarker
- [ ] **KAB-0201** – Startkamera stellt alle vier Wände verständlich dar
- [ ] **KAB-0202** – Aktive Wand wird in 3D samt lesbarem Titel hervorgehoben
- [ ] **KAB-0203** – SW1-Wandbelegung im Pitch als Original und Demo vergleichbar machen
- [ ] **KAB-0204** – SW2-Wandbelegung im Pitch als Original und Demo vergleichbar machen
- [ ] **KAB-0205** – SW3-Wandbelegung im Pitch als Original und Demo vergleichbar machen
- [ ] **KAB-0206** – SW4-Wandbelegung im Pitch als Original und Demo vergleichbar machen
- [ ] **KAB-0207** – Leerrohrabstände in der Präsentation ohne Dimensionsbehauptung beschriften
- [ ] **KAB-0208** – Gerade Kabelzüge im Pitch ohne perspektivisch falschen Knick rendern
- [ ] **KAB-0209** – Rote OLA-Konstruktion separat ein- und ausschalten
- [ ] **KAB-0210** – Blaue MS-Konstruktion separat ein- und ausschalten
- [ ] **KAB-0211** – Gleichzeitige Ansicht beider Systeme auf Überdeckung prüfen
- [ ] **KAB-0212** – Kamera-Preset von oben für Schachtkonstruktion bereitstellen
- [ ] **KAB-0213** – Kamera-Preset innen für Rohranschlüsse bereitstellen
- [ ] **KAB-0214** – PDF-Wandbilder ohne WebGL als Fallback öffnen
- [ ] **KAB-0215** – Verlassen der Präsentation ohne Verlust des aktiven Projekts prüfen
- [ ] **KAB-0216** – Bros letzte Abnahme mit Bildpaaren und offenen Einwänden dokumentieren


## Quellpositions-Nachweise 12.11.2026

KAB-0037/0038/0039 und KAB-0050 wurden mit eigenen Source-/View-/Roundtriptests abgenommen: `sourceU/sourceV` bleiben in JSON und Planvergleich unverändert, die 62-%-Vorschau ist Basic-only, PDF-Vektor-Wandbilder werden von der Vorschau nicht beeinflusst. **PDF-Diagrammkoordinaten sind weiterhin keine realen Aufmaße.** [Sprintbericht](./SPRINT_2026-11-12_BRO_10.md).

**KAB-0049 (12.11.2026):** Offline-Projektviewer und Druckbericht nennen Rohr- und Schachtkoordinaten nicht mehr einfach vermessene Millimeter. PDF-V2-Wandbreiten werden richtungsrichtig ausgewertet und die unveränderten sourceU/sourceV-Planpunkte als *schematisch* statt als gemessene XYZ-Positionen erläutert. Unit-Test: `tests/reports.test.js`.

**Bro-Sprint 2 (12.11.2026):** KAB-0019–0023 und KAB-0044–0047 per PDF-Weltachsenaudit, Vektor↔3D-Inversion, modellbasiertem Rohr-Ø-/Randabstand und automatisiertem `tests/pdf-wall-clearance.test.js` abgeschlossen. `npm test` in GitHub Pages und Windows-Workflow bestanden. Die 2D-/3D-Kollisionsmarkierung KAB-0048 ist implementiert, bleibt jedoch **offen bis zur echten visuellen Prüfung**; diese ist getrennt dokumentiert in [WALL_COORDINATES_AND_CLEARANCE.md](../WALL_COORDINATES_AND_CLEARANCE.md). **Kein Vermessungs- oder Normnachweis.**
