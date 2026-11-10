# Kabelsalat · 828-Task-Backlog · Teil 3/4: P2 – Datenqualität, Import und Fachprüfung

**Status:** 216 neue Aufgaben offen. IDs KAB-0433–KAB-0648. **Abnahme:** Prüfbarkeit und Quellenbeleg vor Automatismus; Software ersetzt keine Elektro-, Bau- oder Brandschutz-Freigabe. Quellen und Regeln im [Task-Index](./README.md).

## P2-01 · Geometrische Qualitätsregeln (KAB-0433–KAB-0450)

**Quelle:** E04, D07, quality.js. **Definition of Done:** Schwellen sind projektbezogen, Warnungen lokalisiert, nie ohne Beleg normativ.

- [ ] **KAB-0433** – Minimale lichte Rohr-Rohr-Distanz für jede Wand berechnen
- [ ] **KAB-0434** – Minimale Rohr-Wandrand-Distanz für jede Wand berechnen
- [ ] **KAB-0435** – Rohrüberdeckung mit unterschiedlichen Durchmessern erkennen
- [ ] **KAB-0436** – Rohr außerhalb der Wandgrenzen als Fehler melden
- [ ] **KAB-0437** – Fehlende Wanddicke als Datenlücke statt Nullwert melden
- [ ] **KAB-0438** – Schachtmaße als bestätigt oder Platzhalter klassifizieren
- [ ] **KAB-0439** – Projektweiten Mindestabstand ohne Normbehauptung konfigurieren
- [ ] **KAB-0440** – Wandbezogenen Mindestabstand separat konfigurieren
- [ ] **KAB-0441** – Rohrtypbezogenen Mindestabstand getrennt konfigurieren
- [ ] **KAB-0442** – Warnung mit betroffener Objekt-ID ausgeben
- [ ] **KAB-0443** – Warnung mit Millimeter-Istwert und Projekt-Sollwert ausgeben
- [ ] **KAB-0444** – Geometriewarnungen nach Schwere sortieren
- [ ] **KAB-0445** – Gleiche Warnung nach mehrfacher Berechnung deduplizieren
- [ ] **KAB-0446** – Warnung nach Reparatur automatisch entfernen
- [ ] **KAB-0447** – Quality-Check ohne WebGL lauffähig machen
- [ ] **KAB-0448** – Grenzwertgleichheit ohne Off-by-one-Fehler testen
- [ ] **KAB-0449** – Sehr große Wand- und Rohranzahl ohne Endlosschleife prüfen
- [ ] **KAB-0450** – Qualitätsbericht mit offenen und bestätigten Daten getrennt ausgeben

## P2-02 · 3D-Kollision, Kabelabstand und Füllung (KAB-0451–KAB-0468)

**Quelle:** D07, E10, construction-v2.js. **Definition of Done:** Geometrisch falsche Wege warnen sichtbar, unklare Materialwerte blockieren positive Aussagen.

- [ ] **KAB-0451** – Kabel gegen eigenen Rohrinnenradius geometrisch prüfen
- [ ] **KAB-0452** – Mehrfachbelegung gegen dokumentierten Rohrinnendurchmesser prüfen
- [ ] **KAB-0453** – Rohrfüllgrad nur bei vorhandenen Kabelaußendurchmessern berechnen
- [ ] **KAB-0454** – Fehlenden Rohrinnendurchmesser als Prüfblocker anzeigen
- [ ] **KAB-0455** – Kabelkurve gegen Schachtwandvolumen auf Durchdringung prüfen
- [ ] **KAB-0456** – Kabelkurve gegen andere Kabelkurven auf Abstand prüfen
- [ ] **KAB-0457** – Kabelkurve gegen OLA-Einhausung auf Übertritt prüfen
- [ ] **KAB-0458** – Kabelkurve gegen MS-Einhausung auf Übertritt prüfen
- [ ] **KAB-0459** – Kabelkurve gegen Auflagerkörper auf Kollision prüfen
- [ ] **KAB-0460** – Kabelkurve gegen Schachtboden auf Unterschreitung prüfen
- [ ] **KAB-0461** – Kabelkurve gegen Schachtdecke auf Unterschreitung prüfen
- [ ] **KAB-0462** – Schrägen Kabelverlauf über segmentierte Abstände auswerten
- [ ] **KAB-0463** – Durchführungsöffnung gegen reales Kabelradiusprofil prüfen
- [ ] **KAB-0464** – Mehrfachkabel im selben Rohr als gebündelten Startbereich behandeln
- [ ] **KAB-0465** – Angenäherte AABB-Prüfung von exakterer 3D-Prüfung unterscheiden
- [ ] **KAB-0466** – Kollisionsfall mit Berührung knapp unter Grenzwert testen
- [ ] **KAB-0467** – Kollisionsfall mit Berührung exakt auf Grenzwert testen
- [ ] **KAB-0468** – 3D-Kollisionshinweis direkt am betroffenen Objekt markieren

## P2-03 · Brandschutz und Systemtrennung (KAB-0469–KAB-0486)

**Quelle:** E08, E10, AVL0116. **Definition of Done:** Keine Selbstfreigabe: offene Brandschutzfragen, Expertenentscheid und Nachweise explizit.

- [ ] **KAB-0469** – OLA/MS-Trennanforderung aus Fachkonzept als verlinkten Prüfpunkt führen
- [ ] **KAB-0470** – Fremdkabel in OLA-Kanal als Konflikt sichtbar machen
- [ ] **KAB-0471** – Fremdkabel in MS-Kanal als Konflikt sichtbar machen
- [ ] **KAB-0472** – Fremdleerrohr hinter Stirnplatte auf Kollision prüfen
- [ ] **KAB-0473** – Allseitige Einhausung gegen sechs benötigte Flächen prüfen
- [ ] **KAB-0474** – Fugen zwischen angrenzenden Powerpanel-Platten auf Lücken prüfen
- [ ] **KAB-0475** – Anschlussfuge zu Rohröffnung als offenen Prüfpunkt markieren
- [ ] **KAB-0476** – Dokumentierte Plattenmaterialbezeichnung je Bauteil speichern
- [ ] **KAB-0477** – Materialstärke nie ungeprüft als zugelassen markieren
- [ ] **KAB-0478** – Herstellerdokument zur Plattenkombination referenzieren
- [ ] **KAB-0479** – Nachweis zu Kabeldurchführung und Abschottung erfassen
- [ ] **KAB-0480** – Nachweis zu Schachttür und Revisionszugang erfassen
- [ ] **KAB-0481** – Grenzwertkonflikt 75 mm gegen 100 mm formal dokumentieren
- [ ] **KAB-0482** – Variante-A-/Variante-B-Auswahl nur mit nachvollziehbarer Begründung speichern
- [ ] **KAB-0483** – Quellenabhängige Brandschutzwarnung im Bericht ausgeben
- [ ] **KAB-0484** – Baurechtlichen Freigabeprozess als externen Schritt verknüpfen
- [ ] **KAB-0485** – Unbestätigte Brandschutzfelder im Projekt eindeutig hervorheben
- [ ] **KAB-0486** – Negativtest: schematische 3D-Konstruktion erzeugt niemals Freigabestatus

## P2-04 · Quelldaten und Provenienzmodell (KAB-0487–KAB-0504)

**Quelle:** D01, D05, D06. **Definition of Done:** Jede Quellaussage ist lokalisierbar, revisionsfähig und von Schätzwerten unterschieden.

- [ ] **KAB-0487** – Quellen-ID für jedes importierte Rohr speichern
- [ ] **KAB-0488** – Quellen-ID für jeden importierten Kabelanschluss speichern
- [ ] **KAB-0489** – Quellen-ID für jede eingelesene Wandabmessung speichern
- [ ] **KAB-0490** – Dokumenttitel und Dokumentversion getrennt speichern
- [ ] **KAB-0491** – Seitenzahl und optionalen Planbereich speichern
- [ ] **KAB-0492** – Quelle für abgeleiteten DN-Wert kennzeichnen
- [ ] **KAB-0493** – Quelle für vermessenen DN-Wert kennzeichnen
- [ ] **KAB-0494** – Unverifizierte grafische Radiusableitung kennzeichnen
- [ ] **KAB-0495** – Geschätzte Koordinate von gemessener Koordinate unterscheiden
- [ ] **KAB-0496** – Erfassungsdatum getrennt vom Prüfdatum speichern
- [ ] **KAB-0497** – Quellwidersprüche mit beiden Fundstellen erfassen
- [ ] **KAB-0498** – Nutzerentscheidung über Quellwiderspruch protokollieren
- [ ] **KAB-0499** – Nicht zugeordnete Planmarker als eigene Datenart modellieren
- [ ] **KAB-0500** – Vermutete Kabelverknüpfung von bestätigter unterscheiden
- [ ] **KAB-0501** – Originalwerte bei Ansichtstransformation erhalten
- [ ] **KAB-0502** – Projektobjekt auf Originalquelle rückverfolgbar machen
- [ ] **KAB-0503** – Quellenliste nach Version und Seitenbereich durchsuchen
- [ ] **KAB-0504** – Quellenverlust bei Export oder Migration als Fehler behandeln

## P2-05 · CSV-, Tabellen- und Vermessungsimporte (KAB-0505–KAB-0522)

**Quelle:** D03, ROADMAP P2. **Definition of Done:** Import erzeugt prüfbare Vorschau und Rücknahme, keine automatische Änderung ohne Bestätigung.

- [ ] **KAB-0505** – CSV-Schema für Schacht-ID und lokale Koordinaten definieren
- [ ] **KAB-0506** – CSV-Schema für Wand-ID und Wandgeometrie definieren
- [ ] **KAB-0507** – CSV-Schema für Rohr-ID und DN definieren
- [ ] **KAB-0508** – CSV-Schema für Kabel-ID und Endpunkt definieren
- [ ] **KAB-0509** – CSV-Schema für globale XYZ-Koordinaten definieren
- [ ] **KAB-0510** – Zeichensatz UTF-8 mit BOM korrekt erkennen
- [ ] **KAB-0511** – Dezimaltrennzeichen Punkt und Komma unterscheiden
- [ ] **KAB-0512** – Einheit Meter versus Millimeter explizit abfragen
- [ ] **KAB-0513** – Fehlende Spalte mit zeilengenauer Fehlermeldung ausgeben
- [ ] **KAB-0514** – Doppelte Rohr-ID beim Import blockieren
- [ ] **KAB-0515** – Doppelte Kabel-ID beim Import blockieren
- [ ] **KAB-0516** – Unbekannte Wand-ID beim Import als offenen Zuordnungsfall anzeigen
- [ ] **KAB-0517** – Kabelanschluss auf nicht existentes Rohr markieren
- [ ] **KAB-0518** – Vermessungshöhe und Koordinatensystem importieren
- [ ] **KAB-0519** – CSV-Import vor Übernahme im Diff anzeigen
- [ ] **KAB-0520** – Teilimport nach Fehler ohne halbfertige Projektdaten zurückrollen
- [ ] **KAB-0521** – Importierten Datensatz samt Quelle und Datum protokollieren
- [ ] **KAB-0522** – Exportierte CSV-Datei auf verlustfreien Wiedereinlesetest prüfen

## P2-06 · PDF-Vektorpläne und Referenzabgleich (KAB-0523–KAB-0540)

**Quelle:** D03, D04, PDF V2. **Definition of Done:** Parser erzeugt nur Kandidaten; jede Rohrmitte/Belegung wird anhand PDF-Seite überprüfbar.

- [ ] **KAB-0523** – PDF-Dokument mit mehrseitigen technischen Zeichnungen verarbeiten
- [ ] **KAB-0524** – Seitenindex und Blattgröße aus PDF speichern
- [ ] **KAB-0525** – Vektor-Kreisbögen als mögliche Rohre identifizieren
- [ ] **KAB-0526** – Kreisellipse von kreisförmigem Rohr unterscheiden
- [ ] **KAB-0527** – Rohrkreis-Mittelpunkte in Bildkoordinaten extrahieren
- [ ] **KAB-0528** – Rohrgrößen aus gezeichneter Kreisgröße nur als unbestätigt markieren
- [ ] **KAB-0529** – Rohrnummer aus benachbartem Vektortext lesen
- [ ] **KAB-0530** – Trassen-Nummer aus Kopfbereich zuordnen
- [ ] **KAB-0531** – Wandkennung aus PDF-Seitenlegende erfassen
- [ ] **KAB-0532** – Spiegelung zwischen PDF-Außen- und Innenansicht vorschlagen
- [ ] **KAB-0533** – Mehrfachbelegung aus zugehöriger Textliste als Kandidat erzeugen
- [ ] **KAB-0534** – Unklare Textzeile nicht stillschweigend einem Rohr zuweisen
- [ ] **KAB-0535** – Gescannte PDF-Seite als eigene Verarbeitungsart behandeln
- [ ] **KAB-0536** – PDF-Seiten-Overlay mit erkannten Kreismittelpunkten zeigen
- [ ] **KAB-0537** – Importvorschau mit 2D-Wandbild synchronisieren
- [ ] **KAB-0538** – Erkannten Kreis manuell korrigierbar machen
- [ ] **KAB-0539** – Freigegebene PDF-Erkennung mit Quellkoordinaten versionieren
- [ ] **KAB-0540** – Roundtrip-Test des PDF-V2-Schachts gegen 91 Rohrquellen ergänzen

## P2-07 · Bildanalyse und unterstützte Zuordnung (KAB-0541–KAB-0558)

**Quelle:** D04, CONCEPT_AUDIT. **Definition of Done:** Erkennung ist Vorschlag, Unsicherheit sichtbar; keine automatische technische Freigabe.

- [ ] **KAB-0541** – Bild-Upload nur nach bewusster Nutzeraktion aktivieren
- [ ] **KAB-0542** – Bilddateiformat auf zulässige Typen beschränken
- [ ] **KAB-0543** – Bildauflösung für Quellenvergleich dokumentieren
- [ ] **KAB-0544** – Schachtwandrahmen im Foto manuell markieren
- [ ] **KAB-0545** – Rohröffnungen im Foto als Kandidaten markieren
- [ ] **KAB-0546** – Rohrnummern mit manueller Korrektur erfassen
- [ ] **KAB-0547** – Trassennummern mit manueller Korrektur erfassen
- [ ] **KAB-0548** – Kabelschrift im Foto als Kandidatenliste darstellen
- [ ] **KAB-0549** – Erkennungsunsicherheit pro Textfeld anzeigen
- [ ] **KAB-0550** – Überlagerte Rohröffnungen als mehrdeutig behandeln
- [ ] **KAB-0551** – Perspektivenverzerrung als Näherung sichtbar machen
- [ ] **KAB-0552** – Bildkoordinaten nicht ohne Maßreferenz in mm umwandeln
- [ ] **KAB-0553** – Originalbild neben interpretiertem Wandbild zeigen
- [ ] **KAB-0554** – Kandidaten vor Übernahme einzeln bestätigen lassen
- [ ] **KAB-0555** – Falsch erkannte Rohr-ID ohne Datenverlust korrigieren
- [ ] **KAB-0556** – Ablehnung eines Kandidaten als überprüfbare Entscheidung speichern
- [ ] **KAB-0557** – Erkennungsquelle und Softwareversion pro Import protokollieren
- [ ] **KAB-0558** – Testdatensatz mit absichtlich falscher Fotoerkennung auswerten

## P2-08 · CAD-, BIM- und georeferenzierte Pläne (KAB-0559–KAB-0576)

**Quelle:** D03, ROADMAP P2. **Definition of Done:** Import nur für klar definierte Formate, Skalierung und Koordinatensystem mit Preview.

- [ ] **KAB-0559** – DXF-2D-Importumfang fachlich festlegen
- [ ] **KAB-0560** – DXF-Layer auf Schachtwände abbilden
- [ ] **KAB-0561** – DXF-Kreisobjekte auf Rohrkandidaten abbilden
- [ ] **KAB-0562** – DXF-Linien auf Schachtwandkanten abbilden
- [ ] **KAB-0563** – DXF-Einheiten explizit prüfen
- [ ] **KAB-0564** – DWG-Unterstützung und Lizenzgrenzen dokumentieren
- [ ] **KAB-0565** – IFC-Modellimportgrenzen fachlich festlegen
- [ ] **KAB-0566** – IFC-Schachtobjekt mit stabiler ID verknüpfen
- [ ] **KAB-0567** – IFC-Öffnungsobjekt als Rohrcandidate behandeln
- [ ] **KAB-0568** – Koordinatensystem-Transformation für importierte Baupläne testen
- [ ] **KAB-0569** – Höhenbezug auf einheitliches Projektdatum umstellen
- [ ] **KAB-0570** – Nicht unterstützte CAD-Elemente als Verlustliste ausgeben
- [ ] **KAB-0571** – Importierte Layer vor dem Mapping anzeigen
- [ ] **KAB-0572** – Geometrie-Mapping mit Stichproben manuell bestätigen
- [ ] **KAB-0573** – Quellmodell-Versionswechsel als Diff behandeln
- [ ] **KAB-0574** – CAD-Export für Schachtwand und Rohrachsen als Ziel definieren
- [ ] **KAB-0575** – BIM-Export ohne behauptete bautechnische Freigabe kennzeichnen
- [ ] **KAB-0576** – Projekt mit gedrehtem lokalen CAD-Ursprung testen

## P2-09 · Audittrail, Objektstatus und Reviews (KAB-0577–KAB-0594)

**Quelle:** D05, D06, MILESTONE_100. **Definition of Done:** Quelle/Bearbeiter/Zeit/alter-neuer Wert nachvollziehbar; alter Auditlog bleibt erhalten.

- [ ] **KAB-0577** – Prüfstatus pro Schacht führen
- [ ] **KAB-0578** – Prüfstatus pro Wand führen
- [ ] **KAB-0579** – Prüfstatus pro Leerrohr führen
- [ ] **KAB-0580** – Prüfstatus pro Kabel führen
- [ ] **KAB-0581** – Prüfstatus pro Platte führen
- [ ] **KAB-0582** – Prüfstatus pro Auflager führen
- [ ] **KAB-0583** – Bearbeiterkennung pro Änderung speichern
- [ ] **KAB-0584** – Änderungsdatum mit Zeitzone speichern
- [ ] **KAB-0585** – Alten und neuen numerischen Wert protokollieren
- [ ] **KAB-0586** – Importänderungen von manuellen Änderungen unterscheiden
- [ ] **KAB-0587** – Statuswechsel mit Kommentar erzwingen können
- [ ] **KAB-0588** – Status Vor Ort abgeglichen nur mit Quellenbeleg erlauben
- [ ] **KAB-0589** – Prüfstatus nach Geometrieänderung ggf. invalidieren
- [ ] **KAB-0590** – Offene Prüffragen als Objektliste anzeigen
- [ ] **KAB-0591** – Reviewhistorie nach Schacht filtern
- [ ] **KAB-0592** – Reviewhistorie nach Datumsbereich filtern
- [ ] **KAB-0593** – Vermerk nach Export und Wiederimport vollständig erhalten
- [ ] **KAB-0594** – Manipulationsschutz als offen ausweisen solange nur lokale JSON-Datei existiert

## P2-10 · Berichte, Zeichnungen und Exporte (KAB-0595–KAB-0612)

**Quelle:** E13, A04, reports.js. **Definition of Done:** Export trennt Quell- und Demo-Werte, enthält Warnungen und ist ohne App lesbar.

- [ ] **KAB-0595** – Schachtübersicht als druckfertige Titelseite ausgeben
- [ ] **KAB-0596** – Rohrbelegung pro Wand mit LR-IDs exportieren
- [ ] **KAB-0597** – Kabelverzeichnis mit A-/B-Endpunkten ausgeben
- [ ] **KAB-0598** – Liste ungeklärter Kabelenden exportieren
- [ ] **KAB-0599** – Ausgewählte 3D-Kameraansicht als PNG exportieren
- [ ] **KAB-0600** – 2D-Wandplan als skalierte Grafik exportieren
- [ ] **KAB-0601** – Mehrseitigen PDF-Ausdruck ohne abgeschnittene Tabellen testen
- [ ] **KAB-0602** – Projektquelle und Versionsdatum im Bericht ausgeben
- [ ] **KAB-0603** – Schematische Maße ausdrücklich kennzeichnen
- [ ] **KAB-0604** – Geometrische Warnungen nach Objekt-ID im Bericht listen
- [ ] **KAB-0605** – Brandschutzfragen ohne Freigabebehauptung ausgeben
- [ ] **KAB-0606** – Geometrieänderungen zwischen Varianten als Diff ausgeben
- [ ] **KAB-0607** – Offline-Viewer mit ausgewähltem Schacht starten
- [ ] **KAB-0608** – Offline-Viewer ohne Editiermöglichkeit absichern
- [ ] **KAB-0609** – HTML-Bericht gegen injizierte Texte escapen
- [ ] **KAB-0610** – Bildexport bei hoher DPI und Windows-Skalierung testen
- [ ] **KAB-0611** – CSV-Ausgabe für Rohrbelegungslisten anbieten
- [ ] **KAB-0612** – Ein Exportpaket aus Bericht, Quellenliste und Projekt-JSON definieren

## P2-11 · Rollen, Freigaben und Zusammenarbeit (KAB-0613–KAB-0630)

**Quelle:** ROADMAP P2, D05. **Definition of Done:** Noch keine rechtsverbindliche Freigabe; Rollen wirken nur nach überprüfter Autorisierung.

- [ ] **KAB-0613** – Rollenbild Betrachter/Bearbeiter/Prüfer spezifizieren
- [ ] **KAB-0614** – Schreibrechte für Betrachter einschränken
- [ ] **KAB-0615** – Prüfvermerke auf Prüferrolle begrenzen
- [ ] **KAB-0616** – Rollenzuweisungen als eigene Datensätze modellieren
- [ ] **KAB-0617** – Projektfreigabe strikt von visueller Darstellung trennen
- [ ] **KAB-0618** – Unsignierte lokale Prüfvermerke als solche kennzeichnen
- [ ] **KAB-0619** – Projekt-Export mit Freigabestatus kennzeichnen
- [ ] **KAB-0620** – Export bei fachlich offenen Prüffragen warnend markieren
- [ ] **KAB-0621** – Bearbeitung gesperrter Projektstände kontrolliert verweigern
- [ ] **KAB-0622** – Neue Version nach Änderung eines freigegebenen Stands anlegen
- [ ] **KAB-0623** – Konkurrierende Änderungen zweier Bearbeiter erkennen
- [ ] **KAB-0624** – Versionskonflikt vor Überschreiben sichtbar machen
- [ ] **KAB-0625** – Kommentarfunktion an konkrete Rohr-IDs koppeln
- [ ] **KAB-0626** – Kommentarfunktion an konkrete Kabel-IDs koppeln
- [ ] **KAB-0627** – Liste noch unbeantworteter Fachfragen bereitstellen
- [ ] **KAB-0628** – Prüferentscheid mit Quelle und Versionsstand protokollieren
- [ ] **KAB-0629** – Freigabe-Entzug bei substantieller Geometrieänderung vorsehen
- [ ] **KAB-0630** – Anforderungen an spätere Server-Authentifizierung dokumentieren

## P2-12 · Sicherheits- und Freigabegrenzen (KAB-0631–KAB-0648)

**Quelle:** CONCEPT_AUDIT, V2_0_STATUS. **Definition of Done:** Keine automatische bauliche Freigabe aus Modellprüfungen, offene Annahmen klar markiert.

- [ ] **KAB-0631** – Schematisch-Flag in Projektkopf verpflichtend anzeigen
- [ ] **KAB-0632** – Unbekannten Rohr-DN nicht durch stillen Standardwert als bestätigt behandeln
- [ ] **KAB-0633** – Unbekannte Wanddicke nicht als bestätigtes Aufmaß behandeln
- [ ] **KAB-0634** – Unbekannte Leitungslänge nicht als Null-Länge darstellen
- [ ] **KAB-0635** – Unbekannten Biegeradius nicht als bestanden darstellen
- [ ] **KAB-0636** – Quellwiderspruch vor fachlichem Abschluss kennzeichnen
- [ ] **KAB-0637** – Abgeschlossene Softwaretests von baulicher Freigabe unterscheiden
- [ ] **KAB-0638** – PDF-V2-Wandbild und 3D-Demo mit getrenntem Status versehen
- [ ] **KAB-0639** – Materialbezeichnung ohne Zulassungsnummer nicht als geprüft markieren
- [ ] **KAB-0640** – Technische Konstruktion nur mit prüfbaren Eingangsdaten klassifizieren
- [ ] **KAB-0641** – Fremdrohr-Kollision trotz grüner UI-Ansicht sichtbar warnen
- [ ] **KAB-0642** – Export ohne Quellenliste als unvollständigen Nachweis markieren
- [ ] **KAB-0643** – Grenzwertänderung mit alter und neuer Annahme protokollieren
- [ ] **KAB-0644** – Kabelendpunkt mit nur einem Quellbeleg als ungeklärt belassen
- [ ] **KAB-0645** – Realen Abgleich nur mit dokumentierter vermessener Koordinate setzen
- [ ] **KAB-0646** – Planfreigabe ohne Prüferangabe kontrolliert verhindern
- [ ] **KAB-0647** – Softwarehaftungs- und Gültigkeitshinweise lesbar platzieren
- [ ] **KAB-0648** – Abnahme-Szenario mit absichtlich falschem Datenstand negativ testen

