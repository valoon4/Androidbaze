# Kabelsalat · 828-Task-Backlog · Teil 2/4: P1 – Editor und Schachtnetz

**Status:** Alle untenstehenden 216 neuen Aufgaben offen. IDs KAB-0217–KAB-0432. **Abnahme:** Implementierung mit reproduzierbaren Eingaben, aussagekräftigen Tests und unverändert funktionierenden Altprojekten. Quellen und Regeln im [Task-Index](./README.md).

## P1-01 · Freie Schachtwände und Grundrisse (KAB-0217–KAB-0234)

**Quelle:** E01, E02, P0-02. **Definition of Done:** Geometrische Fälle mit 3, 4, 5 und 8 Wänden öffnen, editieren und speichern.

- [ ] **KAB-0217** – Wandmodell von vier festen Wänden auf dynamische Wandliste umstellen
- [ ] **KAB-0218** – Neue Wand per UI mit stabiler ID erstellen
- [ ] **KAB-0219** – Vorhandene Wand samt zugehörigen Rohren sicher löschen
- [ ] **KAB-0220** – Wandlänge numerisch in Millimetern bearbeiten
- [ ] **KAB-0221** – Wandhöhe numerisch in Millimetern bearbeiten
- [ ] **KAB-0222** – Wanddicke numerisch in Millimetern bearbeiten
- [ ] **KAB-0223** – Wandposition als 3D-Startpunkt bearbeiten
- [ ] **KAB-0224** – Wandrichtung als Endpunkt oder Winkel bearbeiten
- [ ] **KAB-0225** – Wandnamen unabhängig von System-ID ändern
- [ ] **KAB-0226** – Geschlossenen polygonalen Grundriss aus Wänden aufbauen
- [ ] **KAB-0227** – Offenen Wandzug ohne automatisch erfundene Abschlusswand erlauben
- [ ] **KAB-0228** – Nichtrechtwinklige Wandanschlüsse geometrisch verbinden
- [ ] **KAB-0229** – Wandversatz mit asynchron aktualisierten Innenmaßen prüfen
- [ ] **KAB-0230** – Schacht mit dreieckigem Grundriss als Testfall speichern
- [ ] **KAB-0231** – Schacht mit fünfeckigem Grundriss als Testfall speichern
- [ ] **KAB-0232** – Mehrere Wände mit gleicher Länge und verschiedenen IDs unterscheiden
- [ ] **KAB-0233** – Alte SW1–SW4-Projekte in dynamisches Wandmodell migrieren
- [ ] **KAB-0234** – Wand-Reihenfolge im JSON ohne Positionsverlust ändern

## P1-02 · Leerrohr-Editor und Rohrtypen (KAB-0235–KAB-0252)

**Quelle:** E03, E04, P0-03. **Definition of Done:** Rohr-CRUD, IDs, DN, Randabstände und Rundlauf 2D/3D getestet.

- [ ] **KAB-0235** – Neues Leerrohr auf beliebiger Wand mit freiem Mittelpunkt anlegen
- [ ] **KAB-0236** – Leerrohr-ID bei Kopieren sicher neu generieren
- [ ] **KAB-0237** – Leerrohr-DN aus validierbarer Vorschlagsliste auswählen
- [ ] **KAB-0238** – Freien Rohr-Außendurchmesser separat vom Nenndurchmesser speichern
- [ ] **KAB-0239** – Leerrohrwandstärke optional separat erfassen
- [ ] **KAB-0240** – Leerrohr x-Mittelpunkt numerisch editieren
- [ ] **KAB-0241** – Leerrohr y-Mittelpunkt numerisch editieren
- [ ] **KAB-0242** – Leerrohr in 2D mit aktivem Fangraster verschieben
- [ ] **KAB-0243** – Leerrohr in 2D ohne Fangraster verschieben
- [ ] **KAB-0244** – Rohrgruppe rechteckig mit definiertem Mittelpunktabstand duplizieren
- [ ] **KAB-0245** – Rohrgruppe mit unterschiedlichen DN duplizieren
- [ ] **KAB-0246** – Rohrloch von einer Wand auf eine andere übertragen
- [ ] **KAB-0247** – Rohrloch beim Wandwechsel mit Anschlusskonflikten warnen
- [ ] **KAB-0248** – Rohr über Wandgrenze nicht unbemerkt platzieren
- [ ] **KAB-0249** – Rohr über Nachbarrohr nicht unbemerkt platzieren
- [ ] **KAB-0250** – Rohr durch Tastatureingabe auf exakten Millimeter setzen
- [ ] **KAB-0251** – Leerrohrlöschung mit zugeordneten Kabeln vorab bestätigen
- [ ] **KAB-0252** – Rohrliste nach Wand, Trasse und DN sortierbar machen

## P1-03 · Kabeleditor: Objekt und Anschluss (KAB-0253–KAB-0270)

**Quelle:** E05, P0-04. **Definition of Done:** Jedes Kabel ist über ID eindeutig selektierbar, in beiden Ansichten konsistent und roundtrip-fähig.

- [ ] **KAB-0253** – Neues Kabel mit ID und Anzeigename erstellen
- [ ] **KAB-0254** – Kabelaußendurchmesser pro Kabel numerisch bearbeiten
- [ ] **KAB-0255** – Kabeltyp als katalogisierbaren Stammdatensatz hinterlegen
- [ ] **KAB-0256** – Kabelgruppe von technischer Kategorie getrennt speichern
- [ ] **KAB-0257** – Startrohr aus verfügbarer Rohrliste auswählen
- [ ] **KAB-0258** – Zielrohr aus verfügbarer Rohrliste auswählen
- [ ] **KAB-0259** – Einseitigen Kabelanschluss ohne künstliches Ziel zulassen
- [ ] **KAB-0260** – Start- und Zielanschluss mit Vorwarnung tauschen
- [ ] **KAB-0261** – Kabel in bestehendem Rohr gezielt umhängen
- [ ] **KAB-0262** – Kabel bei Entfernen des Startrohrs als ungeklärt behalten
- [ ] **KAB-0263** – Kabel bei Entfernen des Zielrohrs als ungeklärt behalten
- [ ] **KAB-0264** – Kabel mit zwei Seiten desselben Schachts verbinden
- [ ] **KAB-0265** – Kabel über einen Schachtübergang in Segmenten modellieren
- [ ] **KAB-0266** – Kabelname ändern ohne Referenzen auf Kabel-ID zu brechen
- [ ] **KAB-0267** – Kabel mit gleicher Bezeichnung aber anderem Verlauf getrennt führen
- [ ] **KAB-0268** – Kabelstatus Bearbeitung vor technischer Freigabe anzeigen
- [ ] **KAB-0269** – Einzelnes Kabel inklusive Quelle sicher duplizieren
- [ ] **KAB-0270** – Kabelentfernung mit Anzeige aller betroffenen Routen bestätigen

## P1-04 · 2D-Zeichenfläche und Werkzeuge (KAB-0271–KAB-0288)

**Quelle:** A01, B01, E05. **Definition of Done:** Reale Maus- und Tastaturaktionen in 2D erhalten Modellkonsistenz.

- [ ] **KAB-0271** – 2D-Wandplan auf beliebiges Fensterformat passend skalieren
- [ ] **KAB-0272** – 2D-Wandpan ohne versetzte Mauskoordinaten implementieren
- [ ] **KAB-0273** – 2D-Zoom um aktuellen Mauspunkt implementieren
- [ ] **KAB-0274** – 2D-Beschriftung bei starker Verkleinerung kollisionsarm anzeigen
- [ ] **KAB-0275** – 2D-Maßkette zwischen zwei Rohrmittelpunkten zeichnen
- [ ] **KAB-0276** – 2D-Lichtmaß zwischen Rohrmantelflächen zeichnen
- [ ] **KAB-0277** – 2D-Randabstand auf allen vier Seiten anzeigen
- [ ] **KAB-0278** – 2D-Mehrfachauswahl durch Rechteckselektion ermöglichen
- [ ] **KAB-0279** – 2D-Mehrfachauswahl per Umschalttaste erweitern
- [ ] **KAB-0280** – 2D-Rohrgruppe gemeinsam verschieben
- [ ] **KAB-0281** – 2D-Auswahl eines Kabelsegments statt nur eines Rohres ermöglichen
- [ ] **KAB-0282** – 2D-Kabelwegpunkt per Drag verschieben
- [ ] **KAB-0283** – 2D-Stützpunkt per Tastatur präzise verschieben
- [ ] **KAB-0284** – 2D-Seitenplatte mit Auswahlgriff sichtbar machen
- [ ] **KAB-0285** – 2D-Plattenkontur bei Selektion hervorheben
- [ ] **KAB-0286** – 2D-Belegungslegende bei identischen Farben differenzieren
- [ ] **KAB-0287** – 2D-Rückgängig nach Drag-Operation auf einen Schritt reduzieren
- [ ] **KAB-0288** – 2D-Druckansicht mit Maßstab und Blattformat ergänzen

## P1-05 · 3D-Kamera, Auswahl und Manipulation (KAB-0289–KAB-0306)

**Quelle:** B02, B03, A02. **Definition of Done:** Objekttreffer unabhängig von Perspektive und UI-Modus reproduzierbar.

- [ ] **KAB-0289** – 3D-Picking-Rangfolge für Rohre vor transparente Wände legen
- [ ] **KAB-0290** – 3D-Picking-Rangfolge für Kabel vor nichtselektierbare Raster legen
- [ ] **KAB-0291** – 3D-Hervorhebung ausgewählter Schachtwand einführen
- [ ] **KAB-0292** – 3D-Hervorhebung einzelner Powerpanel-Platte einführen
- [ ] **KAB-0293** – 3D-Hervorhebung einzelner Stütze einführen
- [ ] **KAB-0294** – 3D-Werkzeug zum Verschieben eines Rohrs auf der Wand anbieten
- [ ] **KAB-0295** – 3D-Werkzeug zum Verschieben eines Kabelwegpunkts anbieten
- [ ] **KAB-0296** – 3D-Manipulator auf aktive Wandebene einschränken
- [ ] **KAB-0297** – 3D-Objektbewegung durch numerische Koordinaten ergänzen
- [ ] **KAB-0298** – 3D-Kamera um ausgewähltes Objekt drehen
- [ ] **KAB-0299** – 3D-Kamera-Preset Vorderansicht hinzufügen
- [ ] **KAB-0300** – 3D-Kamera-Preset Rückansicht hinzufügen
- [ ] **KAB-0301** – 3D-Kamera-Preset Seitenansicht hinzufügen
- [ ] **KAB-0302** – 3D-Kamera-Preset Bodenansicht hinzufügen
- [ ] **KAB-0303** – 3D-Kamera-Zoom bei ausgeblendeten Seitenleisten neu kalibrieren
- [ ] **KAB-0304** – 3D-Transparenzsortierung bei überlappenden Platten prüfen
- [ ] **KAB-0305** – 3D-Picking durch Rohrhülsen hindurch kontrolliert ermöglichen
- [ ] **KAB-0306** – 3D-Screenshot ohne Bedienoberfläche in voller Canvasgröße exportieren

## P1-06 · Editor-Oberfläche und Nutzungsführung (KAB-0307–KAB-0324)

**Quelle:** B01, B03, EUGI_MODE_ACCEPTANCE. **Definition of Done:** Einmal durch Basic/Profi und zurück, ohne Projektmutation oder verlorene Eingaben.

- [ ] **KAB-0307** – Basic- und Profi-Modus strikt von den Projektdaten trennen
- [ ] **KAB-0308** – Jede Bearbeitungsaktion im Basic-Modus bewusst blockieren
- [ ] **KAB-0309** – Direkten Wechsel zu Profi ohne versteckte Seitenelemente ermöglichen
- [ ] **KAB-0310** – Beide Seitenleisten einzeln einklappbar halten
- [ ] **KAB-0311** – Ausgeblendete Seitenleiste per Tastatur wieder öffnen
- [ ] **KAB-0312** – Aktive Auswahl im Eigenschaftenbereich deutlich kennzeichnen
- [ ] **KAB-0313** – Fehlende Pflichtwerte direkt am Feld erläutern
- [ ] **KAB-0314** – Änderungsbestätigung nur bei destruktiven Aktionen verlangen
- [ ] **KAB-0315** – Ungespeicherte Änderung vor Beispielwechsel anzeigen
- [ ] **KAB-0316** – Aktiven Projektstand im Kopfbereich darstellen
- [ ] **KAB-0317** – Bearbeitungshistorie mit verständlichen Aktionen benennen
- [ ] **KAB-0318** – Fokusmodus mit Escape sicher verlassen
- [ ] **KAB-0319** – Befehlsleiste für Rohr/Kabel/Platte kontextsensitiv gestalten
- [ ] **KAB-0320** – Sehr lange Kabelnamen ohne Textüberlauf anzeigen
- [ ] **KAB-0321** – Fehlertexte ohne internen Stacktrace benutzerverständlich machen
- [ ] **KAB-0322** – Abwechselndes Arbeiten in Netz- und Schachtansicht beschleunigen
- [ ] **KAB-0323** – Hilfetexte für Fachbegriffe als aufklappbares Glossar anbieten
- [ ] **KAB-0324** – Einstiegsansicht für neue Projekte ohne irreführende Demodaten gestalten

## P1-07 · Globale Schachtkoordinaten (KAB-0325–KAB-0342)

**Quelle:** N01, D02, P0-08. **Definition of Done:** Schacht-Lage mit dokumentiertem Ursprung gespeichert und überall identisch transformiert.

- [ ] **KAB-0325** – Globale X-Koordinate pro Schacht dokumentieren
- [ ] **KAB-0326** – Globale Y-Höhe pro Schacht modellieren
- [ ] **KAB-0327** – Globale Z-Koordinate pro Schacht dokumentieren
- [ ] **KAB-0328** – Koordinatenreferenzsystem als optionalen Projektwert einführen
- [ ] **KAB-0329** – Lokale Schachtachsen gegen globale Projektachsen transformieren
- [ ] **KAB-0330** – Globale Lage einer Rohröffnung aus Schachtposition ableiten
- [ ] **KAB-0331** – Globale Lage eines Kabelendes aus Schachtposition ableiten
- [ ] **KAB-0332** – Schacht-Rotation um vertikale Achse speichern
- [ ] **KAB-0333** – Gedrehten Schacht in Netzkarte korrekt anzeigen
- [ ] **KAB-0334** – Schacht-Ursprung als explizit wählbaren Bezug definieren
- [ ] **KAB-0335** – Unbekannte Höhe von Höhe Null unterscheidbar speichern
- [ ] **KAB-0336** – Umrechnung Millimeter zu Meter ohne Rundungsdrift testen
- [ ] **KAB-0337** – Projekt mit zwei versetzt gedrehten Schächten speichern
- [ ] **KAB-0338** – Globale Koordinaten beim Projektexport kennzeichnen
- [ ] **KAB-0339** – Lokale Koordinaten beim Projektexport kennzeichnen
- [ ] **KAB-0340** – Netzlink beim Verschieben des Schachts konsistent aktualisieren
- [ ] **KAB-0341** – Nicht referenzierte Lagewerte als schematisch warnen
- [ ] **KAB-0342** – Koordinatenmigration alter X/Z-Projekte ohne Höhenphantasie durchführen

## P1-08 · Netztopologie: Schächte und Verbindungen (KAB-0343–KAB-0360)

**Quelle:** N01, N06, P1-Netz. **Definition of Done:** Schächte und Links bleiben eindeutig, auch nach Löschen und Wiederherstellen.

- [ ] **KAB-0343** – Neuen Schacht aus leerer Vorlage ins Netz einfügen
- [ ] **KAB-0344** – Schacht mit bestehender Rohrbelegung ins Netz importieren
- [ ] **KAB-0345** – Schacht in Netzkarte verschieben ohne Innengeometrie zu verändern
- [ ] **KAB-0346** – Zwei Schächte mit explizitem Link verbinden
- [ ] **KAB-0347** – Linkrichtung gegen beidseitige Nutzung unterscheiden
- [ ] **KAB-0348** – Link mit stabiler ID und Status speichern
- [ ] **KAB-0349** – Linkentfernung auf betroffene Kabelrouten prüfen
- [ ] **KAB-0350** – Doppelte Links zwischen denselben Schächten differenzieren
- [ ] **KAB-0351** – Selbstverbindung eines Schachts gezielt verhindern
- [ ] **KAB-0352** – Mehrere getrennte Netzkomponenten anzeigen
- [ ] **KAB-0353** – Netzzyklus mit vier Schächten korrekt darstellen
- [ ] **KAB-0354** – Schachtbezeichnung in Netzkarte ohne Überlappung kürzen
- [ ] **KAB-0355** – Netzlink bei großen Entfernungen maßstabsbewusst zeichnen
- [ ] **KAB-0356** – Netzübersicht bei 30 Schächten interaktiv halten
- [ ] **KAB-0357** – Netzliste über derzeitige 30-Schächte-Grenze skalieren
- [ ] **KAB-0358** – Ausgangs- und Zielschacht pro Link klar darstellen
- [ ] **KAB-0359** – Link-Verbindung als physisch ungeprüft markieren können
- [ ] **KAB-0360** – Netz mit fünf Schächten und unterschiedlichen Wandzahlen testen

## P1-09 · Durchgängige Kabelkontinuität (KAB-0361–KAB-0378)

**Quelle:** N06, N07, D02. **Definition of Done:** Segmentfolge ist explizit und prüfbar; nicht bestätigte Übergänge bleiben offen.

- [ ] **KAB-0361** – Globales Kabelobjekt mit mehreren lokalen Segmenten einführen
- [ ] **KAB-0362** – Lokale Kabel-ID stabil auf globales Kabel zurückführen
- [ ] **KAB-0363** – Kabelreihenfolge über drei Schächte speichern
- [ ] **KAB-0364** – Eintrittsrohr und Austrittsrohr je Durchgangsschacht zuordnen
- [ ] **KAB-0365** – Physische Verbindung an Schachtgrenze von bloßer Netzlink-Linie unterscheiden
- [ ] **KAB-0366** – Fehlende Anschlussbrücke im Schacht als Unterbrechung melden
- [ ] **KAB-0367** – Unbestätigten Schachtübergang als offenen Endpunkt anzeigen
- [ ] **KAB-0368** – Kabelverzweigung mit mehreren legitimen Zielen abbilden
- [ ] **KAB-0369** – Ringförmigen Kabelverlauf ohne Endlosnavigation darstellen
- [ ] **KAB-0370** – Segmentwechsel über zwei aufeinanderfolgende Schächte validieren
- [ ] **KAB-0371** – Angegebene Leitungslänge mit Segmentlängen abgleichen
- [ ] **KAB-0372** – Kabelkontinuität bei Umnummerierung eines Rohres bewahren
- [ ] **KAB-0373** – Kabelkontinuität bei gelöschtem Segment gezielt invalidieren
- [ ] **KAB-0374** – Segmentübergreifende Systemzuordnung prüfen
- [ ] **KAB-0375** – Routenverlauf in globalen XYZ-Koordinaten zusammensetzen
- [ ] **KAB-0376** – Kabelverlauf nach Quelle und Prüfstatus segmentweise darstellen
- [ ] **KAB-0377** – Gesamtverlauf mit einer gezielten Fehlerstelle testen
- [ ] **KAB-0378** – Ende-zu-Ende-Kontinuität niemals ohne belegten Übergang bestätigen

## P1-10 · Netznavigation und Auswahlzustand (KAB-0379–KAB-0396)

**Quelle:** N03, N05, N07. **Definition of Done:** Von Netz über Rohr zu Kabel und zurück ohne Auswahlverlust navigierbar.

- [ ] **KAB-0379** – Netzklick selektiert Schacht ohne sofortigen Modusverlust
- [ ] **KAB-0380** – Netzdoppelklick öffnet exakt den gewählten Schacht
- [ ] **KAB-0381** – Aus Schachtansicht per Button zum Netz zurückkehren
- [ ] **KAB-0382** – Aktiven Schacht beim Rücksprung auf Netzkarte hervorheben
- [ ] **KAB-0383** – Aus globaler Suche ein bestimmtes Rohr im 3D selektieren
- [ ] **KAB-0384** – Aus globaler Suche ein bestimmtes Kabel im 3D selektieren
- [ ] **KAB-0385** – Aus Netz-Kabelroute das zugehörige lokale Segment öffnen
- [ ] **KAB-0386** – Breadcrumb Netz > Schacht > Wand > Rohr einführen
- [ ] **KAB-0387** – Breadcrumb Netz > Schacht > Kabelweg einführen
- [ ] **KAB-0388** – Zur vorherigen Auswahl über Zurücknavigation gelangen
- [ ] **KAB-0389** – Ausgewählten Kabelabschnitt über Schachtwechsel erhalten
- [ ] **KAB-0390** – Ausgewähltes Rohr bei Umschalten 2D/3D erhalten
- [ ] **KAB-0391** – Schachtwechsel bei aktivem Bearbeitungsmodus sicher behandeln
- [ ] **KAB-0392** – Netz-Zoomstellung beim Wechsel zu einem Schacht merken
- [ ] **KAB-0393** – Netz-Panstellung beim Wechsel zu einem Schacht merken
- [ ] **KAB-0394** – Nicht mehr vorhandene Selektion nach Löschen sauber auflösen
- [ ] **KAB-0395** – Tastaturnavigation durch Schachtliste ermöglichen
- [ ] **KAB-0396** – Navigationsablauf Netz→Kabel→Rohr→Netz im Browsertest prüfen

## P1-11 · Globale Suche und Filter (KAB-0397–KAB-0414)

**Quelle:** N02, D05. **Definition of Done:** Trefferlisten finden richtige IDs und zeigen keine fremden oder ausgedachten Objekte.

- [ ] **KAB-0397** – Schächte per exakter ID suchen
- [ ] **KAB-0398** – Schächte per Teilnamen suchen
- [ ] **KAB-0399** – Wände per Schacht- und Wand-ID suchen
- [ ] **KAB-0400** – Leerrohre per Trassen-/LR-ID suchen
- [ ] **KAB-0401** – Kabel per exakter Kabelkennung suchen
- [ ] **KAB-0402** – Kabel per normalisierter Schreibweise suchen
- [ ] **KAB-0403** – Kabel nach OLA-System filtern
- [ ] **KAB-0404** – Kabel nach MS-System filtern
- [ ] **KAB-0405** – Kabel nach anderen technischen Gruppen filtern
- [ ] **KAB-0406** – Kabel nach ungeklärtem Endpunkt filtern
- [ ] **KAB-0407** – Objekte nach Prüfstatus filtern
- [ ] **KAB-0408** – Objekte nach Quellversionsdatum filtern
- [ ] **KAB-0409** – Suchtreffer mit Schachtkontext und Typ anzeigen
- [ ] **KAB-0410** – Mehrdeutige Treffer ohne willkürliche Erstauswahl auflisten
- [ ] **KAB-0411** – Suchanfrage bei Schachtwechsel erhalten
- [ ] **KAB-0412** – Leere Trefferliste mit korrektem Hinweis anzeigen
- [ ] **KAB-0413** – Suche mit mehr als 5000 Kabeln performancetesten
- [ ] **KAB-0414** – Globale Suchauswahl synchron in 2D und 3D anzeigen

## P1-12 · Projektdateien, Migration und Undo (KAB-0415–KAB-0432)

**Quelle:** E12, B03, A04. **Definition of Done:** Bestehende JSON-Daten roundtrip-fähig und destruktive Schritte sicher rücknehmbar.

- [ ] **KAB-0415** – Projektformat-Version und Migrationspfad dokumentieren
- [ ] **KAB-0416** – Altes Projekt-v1-Format ohne Datenverlust importieren
- [ ] **KAB-0417** – Altes Projekt-v3-Format ohne Datenverlust importieren
- [ ] **KAB-0418** – Altes Projekt-v4-Format ohne Datenverlust importieren
- [ ] **KAB-0419** – Aktuelles Projekt-v5-Format bytewertgleich roundtrip-testen
- [ ] **KAB-0420** – Mehrschacht-Netzdaten zusammen mit lokalen Snapshots speichern
- [ ] **KAB-0421** – Stabile Rohr-IDs nach Import erhalten
- [ ] **KAB-0422** – Stabile Kabel-IDs nach Import erhalten
- [ ] **KAB-0423** – Wand-ID-Kollisionen beim Import erkennen
- [ ] **KAB-0424** – Kabel-ID-Kollisionen beim Import erkennen
- [ ] **KAB-0425** – Undo für Rohrhinzufügen implementieren
- [ ] **KAB-0426** – Undo für Kabel-Wegpunktverschiebung implementieren
- [ ] **KAB-0427** – Undo für Plattenänderung implementieren
- [ ] **KAB-0428** – Redo nach Undo ohne geänderten Modellinhalt prüfen
- [ ] **KAB-0429** – History bei einer neuen Änderung nach Undo verzweigen lassen
- [ ] **KAB-0430** – Autosave bei Speicherfehler sichtbar melden
- [ ] **KAB-0431** – Projekt vor riskanter Migration automatisch als JSON sicherbar machen
- [ ] **KAB-0432** – Zwei benannte Varianten geometrisch und semantisch diffen

