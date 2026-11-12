# Kabelsalat · zusätzliche Bro-Chat-Tasks (KAB-0829–KAB-0864)

**Quelle:** Nutzerupload `dumm.txt` am 11.11.2026 (historischer Bro-/ChatGPT-Gesprächsexport). Die im Chat genannten PDF-/Excel-Dateien, 1.957 Planseiten und 92 geprüften Schächte sind **Berichte aus dem Chat, keine hier unabhängig geprüften Originale**. Quellenbezogene Tatsachen erfordern nachgereichte Originalpläne und AVL0116. Alle 36 Tasks starten **offen**. Die bestehenden KAB-0001–KAB-0828 bleiben erhalten.

**Kritische Sperre:** Die alte 100-mm-Vorgabe und spätere 75-mm-Bildvorgabe widersprechen sich. Die bisherige Produkt-Zuordnung `OLA=A, MS=B` ersetzt nicht den fachlichen Abstandsentscheid. Die 62-%-Rohrverdichtung der 3D-Demo ist keine vermessene Rohrposition. Bis zur Klärung dürfen daraus keine technischen Zusicherungen oder grüne Freigaben folgen.

## P0-13 · Bros 3D-Korrekturen – Abweichungen v2.0.1 (KAB-0829–KAB-0840)

**Ursprung:** hochgeladener Bro-Chat, Abschnitt „Technische Korrekturen der 3D-Schachtmodelle 13_U_028 und 13_T_021“, Punkte 1–5; V2_0_STATUS.md. **Definition of Done:** Für beide Schächte anhand vorhandener Originalpläne reproduzierbar; bei fehlenden echten Maßen als blockiert/unbestätigt, niemals scheinbar freigegeben.

- [ ] **KAB-0829** – Variantenwahl A/B von der Gewerkezuordnung OLA/MS entkoppeln: jede Trasse darf je nach geprüfter Höhenlage A oder B benötigen
- [ ] **KAB-0830** – Anforderungskonflikt 100 mm (alter Chat) gegenüber 75 mm (spätere Bildvorgabe) samt Quelle und gültiger Versionsentscheidung fachlich klären
- [ ] **KAB-0831** – Bei unbekannter Höhe Schachtboden → Leerrohr-Unterkante keine automatische Variante A/B als technisch richtig behaupten
- [x] **KAB-0832** – Die 62-%-Verdichtung in 13_U_028 als reine optionale Demonstrationsansicht isolieren; reale Modell-/Kabelanschluss-Koordinaten nicht stillschweigend verschieben · Nachweis: https://github.com/valoon4/Androidbaze/commit/aaa96ab73d5ab53dea97eaa3d4456d4a1a5ae638
- [x] **KAB-0833** – Originale Wandzuordnung und Rohranzahl bei 13_T_021 vor/nach Geometriekorrektur als unveränderliche Vergleichsfixture sichern · Nachweis: https://github.com/valoon4/Androidbaze/commit/132d2285612244eaf4413dc8d25d25bed3850a72
- [x] **KAB-0834** – Originale Wandzuordnung und Rohranzahl bei 13_U_028 für HTML- und PDF-V2-Quelle separat als unveränderliche Vergleichsfixture sichern · Nachweis: https://github.com/valoon4/Androidbaze/commit/132d2285612244eaf4413dc8d25d25bed3850a72
- [ ] **KAB-0835** – Den Abstand Boden → Unterkante Leerrohr aus echter Bodenhöhe und bestätigtem Rohraußendurchmesser explizit und mit Einheiten berechnen
- [ ] **KAB-0836** – Variante A bei bestätigtem Grenzwert exakt 100 mm auf Grenzfallverhalten testen, sofern 100 mm fachlich verbindlich bestätigt wird
- [ ] **KAB-0837** – Variante B bei bestätigtem Grenzwert oberhalb 100 mm testen, ohne diesen Wert vor Konfliktentscheidung als verbindlich auszugeben
- [ ] **KAB-0838** – Das ungeklärte Intervall 75–100 mm als Entscheidungssperre mit dokumentiertem Konflikt behandeln
- [ ] **KAB-0839** – Brandschutzkanalführung an bereits korrekt gerouteten Kabeln ausrichten und verhindern, dass der Kanal einen zusätzlichen Kabelknick erzwingt
- [ ] **KAB-0840** – Die bestehenden Interaktionen beider 3D-Schachtmodelle nach jeder fachlichen Geometrieänderung auf Verlust von Auswahl, Kamera, Editierung und Export prüfen

## P2-13 · AVL0116-92-Schacht-Neuprüfung – konkrete Fehlerfälle (KAB-0841–KAB-0852)

**Ursprung:** hochgeladener Bro-Chat, Gespräch über 92 OLA-Schächte, AVL0116-Neuprüfung und alte Prüfungsfehler; Pläne und Original-AVL liegen hier nicht vor. **Definition of Done:** Jede Feststellung mit überprüfbarem Originalplandokument, Seite, Kabeltyp und AVL-Wortlaut; ungeklärte Fälle nicht als konform oder nicht konform endgültig einstufen.

- [ ] **KAB-0841** – Den im Altchat genannten Plansatz mit angeblich 1.957 Seiten anhand tatsächlich bereitgestellter Originaldateien, Ausgabe und Blattindex verifizieren
- [ ] **KAB-0842** – Die vollständige AVL0116 inklusive Originalwortlaut und eventueller Anlagen beschaffen und Prüfregeln aus dem Original versionieren
- [ ] **KAB-0843** – Die angebliche 92-Schacht-Auswertung mit Original-Schachtliste und Version des Excel-Registers auf Vollständigkeit und Duplikate abgleichen
- [ ] **KAB-0844** – Bei AVL0116-Prüfung den konkreten 15-kV-Kabelnachweis statt bloßer Gewerkekennung OLA als Eingangskriterium definieren
- [ ] **KAB-0845** – Die im Altchat genannten zehn OLA-Schächte ohne ausgewiesenes 15-kV-Kabel einzeln identifizieren und Quellseiten belegen
- [ ] **KAB-0846** – 13_F_041 und 13_G_011 erneut auf nachgewiesene 15-kV-Belegung prüfen und einen unbelegten Negativbefund zurücknehmen
- [ ] **KAB-0847** – 13_W_027 und 13_W_033 auf tatsächliche gemeinsame Belegung in Trasse 1 / Leerrohr 5 mit Planseiten nachprüfen
- [ ] **KAB-0848** – Für die beiden LST-LWL bei 13_W_027 / 13_W_033 Herstelleraufbau bzw. metallische Armierung und AVL-Relevanz getrennt belegen
- [ ] **KAB-0849** – 11_A_011: einbetonierten Teilbereich räumlich abgrenzen und keine pauschale Schachtfreigabe daraus ableiten
- [ ] **KAB-0850** – 12_A_001.1 bis 12_A_001.3: bisher übersehenen TK/BOS-Feeder nach Planbeleg berücksichtigen
- [ ] **KAB-0851** – Historischen Betrachtungsabschnitt bis 11_L_011 / QN-010 als reproduzierbaren Netz-/Berichtsfilter anbieten
- [ ] **KAB-0852** – Je geprüftem Schacht gemeinsame Trasse, gemeinsames Leerrohr, tatsächliches 15-kV-Vorkommen und offenen Nachweis als getrennte Prüffelder erfassen

## P1-13 · 2D-Schachtschemata und professionelle PDF-/Excel-Prüfberichte (KAB-0853–KAB-0864)

**Ursprung:** hochgeladener Bro-Chat, Feedback zum Rechteckschema, Querschnitt und Prüfbefund, professionellen PDF- und Excel-Register. **Definition of Done:** Verständliche, fachlich korrekt beschriftete Beispieldateien; nicht aus Planlagen ableitbare reale Kreuzungen und Höhen bleiben schematisch/offen.

- [ ] **KAB-0853** – Jeden ausgewählten Schacht als Rechteck-Grundriss mit eindeutig zugeordneten Schachtwänden darstellen
- [ ] **KAB-0854** – Farbige Kabelverbindungen innerhalb des Rechtecks an belegten Wandrohren starten und enden lassen, ohne ins Leere laufende Fantasielinien
- [ ] **KAB-0855** – Im Rechteckschema sichtbar zwischen belegtem Wandanschluss, Höhenversatz und nur schematisch möglicher Kabelkreuzung unterscheiden
- [ ] **KAB-0856** – Für reale Überkreuzung und Aufstieg zur Schachtwand eine Quellenbestätigung verlangen, statt 2D-Linien als gemessenen 3D-Verlauf auszugeben
- [ ] **KAB-0857** – Den vom Bro ausdrücklich akzeptierten Titel „Kabelverbindung im Schacht“ im Schema beibehalten
- [ ] **KAB-0858** – Die Berichtsrubrik einheitlich „Querschnitt und Prüfbefund“ nennen und „korrigiert“ aus offiziellen Blattüberschriften weglassen
- [ ] **KAB-0859** – Gemeinsames physisches Leerrohr als roten Befund mit sachlicher Textbezeichnung und Prüffallkennzeichnung darstellen
- [ ] **KAB-0860** – Gemeinsame Trasse bei getrennten Leerrohren als orangefarbenen Prüfbedarf mit sachlicher Textbezeichnung darstellen
- [ ] **KAB-0861** – Grünen AVL-Erfüllungsstatus ausschließlich bei vollständig belegtem Originalregelabgleich und dokumentierter Fachentscheidung vergeben
- [ ] **KAB-0862** – Fehlenden AVL-Nachweis neutral/weiß kennzeichnen und von tatsächlichem Nichtbestehen trennen; Farbwörter nicht als professionelle Feldüberschriften verwenden
- [ ] **KAB-0863** – Mehrseitiges PDF-Prüfheft aus Rechteckschema, Querschnitt, Wandbelegung, Originalplanreferenz und offenem Nachweis je ausgewähltem Schacht aufbauen
- [ ] **KAB-0864** – Filterbares Excel-Schachtregister mit denselben Statusdefinitionen, eindeutig referenzierten Querschnittsbildern, verlinkten Planseiten und neutralem Dateinamen exportieren


## Erledigungsnachweise aus Entwicklungssprint 12.11.2026

**KAB-0832/0833/0834**: Quellpositions-Trennung im PDF-V2-Projekt, reversible reine Vorschauverdichtung, feste HTML/PDF-Snapshots für 13_T_021 und 13_U_028 mit 48/78/91 Leerrohren, Wand-IDs, Quelle und Kabelendpunkten. Weitere A/B- und AVL-Fachfragen sind unverändert offen. [Sprintbericht](./SPRINT_2026-11-12_BRO_10.md).
