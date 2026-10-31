# M7 – Rückblick, Erkenntnisse und Strategien: Funktions- und Umsetzungsplan

**Stand:** 31.10.2026 · **Status:** M7.0 und M7.1 in main (PR #24 gemergt); M7.2 als Draft-PR #25; M7.3–M7.6 geplant · **Basis:** M7.1 · **Ziel:** 0.7.x-m7 · **Planergänzung:** optionale Visualisierungen für M7.5

## 1. Produktziel
Den bestehenden Gefühls-/Bedürfnisatlas, Check-in, das verschlüsselte Tagebuch und die Wissenswelt sinnvoll verbinden: Erfahrungen überblicken, nachvollziehbare Zusammenhänge untersuchen, eigene Erkenntnisse formulieren, freiwillig Strategien ausprobieren und erneut reflektieren. Keine Diagnostik, keine Glücks-/Gesundheitsnote, keine KI-Interpretation, kein Gamification-Druck. Alles offline.


## Vorbedingung: Strategiewelt M6.3

Vor der Umsetzung von M7.4 wird gemäß [M6.3-Strategiewelt-Konzept](M63_STRATEGIEWELT_KONZEPT.md) ein redaktionell geprüfter, offline verfügbarer Strategieatlas vorbereitet. M7.0–M7.3 können grundsätzlich unabhängig davon entwickelt werden; M7.4 verwendet später ausschließlich stabile, geprüfte Strategie-IDs und Links, keine automatischen Gefühl-→Bedürfnis-→Strategie-Deutungen. Der bisherige M7-Funktionsumfang und seine Sicherheits-/Merge-Gates bleiben unverändert. Der M6.3-Konzeptschritt umfasst ausdrücklich keine Android-Codeänderungen.

## 2. Funktionsumfang
| ID | Umfang | Priorität | Akzeptanz |
|---|---|---|---|
| F01 | Persönlicher Rückblick mit 7/30/90 Tagen und frei wählbarem Zeitraum | P0 | Jeder Wert mit Eintragsbezug |
| F02 | Gefühl-/Bedürfnishäufigkeiten | P0 | Nennungen statt behaupteter Erlebnisfrequenz; Mehrfachauswahl berücksichtigt |
| F03 | Vorsichtige Musterhinweise | P0 | Nenner, Anzahl, Zeitraum, Evidenz-IDs, Unsicherheit; keine Kausalität |
| F04 | Eigene Erkenntnisse speichern, bearbeiten, suchen, archivieren, löschen | P0 | Nur explizite Speicherung, optionaler Journalbezug |
| F05 | Kontextbezogene Reflexionsfragen | P0 | Redaktionell, offline, freiwillig |
| F06 | Verbindung mit Wissensartikeln und Atlasbegriffen | P1 | Stabile IDs, Fallback bei fehlendem Ziel |
| F07 | Strategie auswählen, ausprobieren, später reflektieren | P1 | Auch „nicht passend“/„unklar“/„nicht ausprobiert“ gültig |
| F08 | Optionale visuelle Rückblick-Ansichten: Begriffs-Balken, Beleg-Vergleichsbalken, Wochenverläufe, neutraler Eintragskalender | P2 | Zahlen/Begriffs-IDs/Zeiträume wie Textansicht; anklickbare Belege; barrierefreie gleichwertige Text-/Tabellenansicht; keine Wertung oder Scheingenauigkeit |

Alle acht Funktionen gehören zum M7-Gesamtumfang, die Priorität regelt die Reihenfolge.

## 3. UX/Navigation
Bestehenden Reiter **Rückblick** aktivieren, keine zusätzliche Bottom-Bar. Innerhalb drei klare Wege: **Überblick** (Zeitfilter, Nennungen, max. drei behutsame Beobachtungen), **Erkenntnisse** (selbst verfasste und verwaltbare Einsichten), **Ausprobieren** (freiwillige Strategievorhaben). Jede Statistik und Beobachtung führt zu konkreten Tagebucheinträgen. Filter/Scrollposition beim Zurückkehren erhalten. Leere, knappe und beschädigte Datenbestände verständlich behandeln. Kein automatischer, wertender Erkenntnistext.

## 4. Architekturentscheidung
**Die tatsächliche Implementierung hat Vorrang vor der alten Room-Vision:** Seit M4 liegt das Journal vollständig AES-256-GCM-verschlüsselt in einer Android-Keystore-geschützten, via AtomicFile geschriebenen JSON-Datei (EKJ1); der M6-Lernstand ist separat (EKL1) verschlüsselt. Keine DB-Migration und keine Änderung bestehender Schlüssel/Dateiformate für M7.

Vorgeschlagene Schichten: JournalSnapshotRepository (authentifizierter, konsistenter Read-only-Snapshot) → reine Use-Cases CalculateOverview, DiscoverPatterns, BuildEvidence, SelectReflectionPrompts → ViewModel/StateFlow → Compose. Auswertung und eventuelle Caches nur flüchtig im geschützten Arbeitsspeicher; beim App-Lock oder Fehler beseitigen. Keine unverschlüsselten Indizes/Analysedateien, keine Internetberechtigung.

**Neue persönliche Daten:** PersonalInsight (UUID, creation/update UTC, eigener Text, optionaler Status, Tagebuch-/Atlasreferenzen) und StrategyExperiment (UUID, Beschreibung, optionale Bezugs-IDs, freiwilliger Zustand/Reflexion). Eigenes versioniertes, in AtomicFile AES-GCM-verschlüsseltes Format samt Größen-/Längenbegrenzung, atomaren Transaktionen, Fehlerbehandlung ohne stilles Überschreiben. Abgeleitete PatternObservation mit Quell-IDs/Zähler/Nenner wird nicht als persönliche Interpretation gespeichert. Bei Löschung eines Journaleintrags dessen Referenzen entfernen, ohne selbst verfasste Erkenntnisse ungefragt zu löschen.

Bestehende Größenlimits des Journals (bis 3000 Einträge/ca. 3 MB Klartext) respektieren; keine Speicherung sensibler Inhalte in DataStore, Logs, Screenshots, Analytics, Benachrichtigungen oder Android Auto Backup.

## 5. Zähl- und Evidenzregeln
Zeitraum standardmäßig am Erstellungszeitpunkt orientieren, sofern kein explizites Ereignisdatum erfasst ist; UTC-Persistenz, lokale Zeitzonenanzeige, halb-offene Intervalle und DST-Tests. Anzahl Einträge getrennt von Anzahl Begriffsnennungen; jeder Begriff pro Eintrag maximal einmal, mehrere unterschiedliche Begriffe je Eintrag zulässig. Nullintensität und fehlende Angabe nicht gleichsetzen; leere Bedürfnisfelder nicht als unerfülltes Bedürfnis deuten. Kein automatisches Freitext-Mining oder NLP.

Erste Beobachtungstypen: Gefühl+Bedürfnis, Gefühls-Kombination, Gefühl+explizit strukturierter Kontext, Wiederkehr über Zeitabschnitte. **Vorläufige nicht-klinische Produktheuristik:** mindestens 10 für die Beobachtung auswertbare Einträge, mindestens 3 gemeinsame Einträge, >=25 % Anteil, Verteilung über mehr als einen Tag. Diese Schwellen vor Umsetzung fachlich/UX-seitig prüfen. Bei unzureichender Datenlage keinen Hinweis. Hinweise enthalten Zeitraum, Zähler, passenden Nenner, konkrete Quelleneinträge und deutlich erkennbare Grenzen; niemals Ursache oder Eigenschaft behaupten. Maximal drei Hinweise auf dem Überblick; ausblendbar.

## 6. Etappen und Exit-Gates
### M7.0 – Fundament
Ist-Code/-Datenformate verifizieren, Snapshot-/Use-Case-/Evidenzverträge und separate Persistenz festlegen, Sicherheits- und Regressionstestbasis. **Exit:** deterministische Ergebnisse, Journal nicht verändert. **Implementierung:** [M7.0-Fundament & Datenschutz](M70_FUNDAMENT_UND_DATENSICHERHEIT.md) – flüchtiger strukturierter Snapshot, Zeitfenster, Nennungszählung, explizite Evidenz, nur nach erfolgreichem authentifizierten Journal-Laden. Die Rückblick-Oberfläche bleibt bis M7.1 unverändert.

### M7.1 – Rückblick
7/30/90 Tage + eigener Filter, Journalanzahl, getrennte Gefühls-/Bedürfnisnennungen, Navigation zu Einträgen, sinnvolle Leerzustände. **Exit:** Testdaten exakt gezählt, Zeitzonen, 0–n Begriffe, Updates und Löschungen korrekt. **Umsetzung:** [M7.1 Rückblick – UX und Testmatrix](M71_RUECKBLICK_UX_ABNAHME.md), PR #24 erfolgreich gemergt. Bestehender Rückblick-Tab ist sichtbar, jede Nennung führt über die Belegliste zum tatsächlich gespeicherten Tagebucheintrag. Ergänzende grafische Begriffs-Balken sind für M7.5 geplant, nicht Bestandteil des M7.1-Release.

### M7.2 – Behutsame Muster
Erklärbarer deterministischer Regelkatalog, Mindestmengen, Evidenz und Muster-Detail. **Exit:** Jeder Hinweis hat gültige Quell-IDs, unzureichende Daten ergeben keinen Hinweis; keine Kausalbehauptung. **Status:** Implementierung in Draft-PR #25, noch nicht gemergt. Kleine proportionale Belegbalken können in M7.5 ergänzt werden; bis dahin bleiben Zähler, passende Nenner und Quellbelege textuell verpflichtend.

### M7.3 – Eigene Erkenntnisse
Explizit gespeicherte Erkenntnisse samt Bearbeiten/Suchen/Archivieren/Löschen, optionalen Links, redaktionelle kontextbezogene Fragen (jede überspringbar; Antworten nur ausdrücklich speichern). **Zusätzliches Pflicht-Gate vor der ersten Speicherung:** Ein gesicherter, rückwärtskompatibler, passphraseverschlüsselter Export/Import inklusive neuer persönlicher Erkenntnisse ist in derselben Etappe verfügbar und geprüft – keine privaten Einträge ohne wiederherstellbare Sicherung. **Exit:** Verschlüsselter Roundtrip, kontrollierte Löschung, stabile Referenzen, Backup-Roundtrip und kein Diagnosetext.

### M7.4 – Wissen, Strategie, Backup-Erweiterung
Verweise auf bestehende Artikel/Atlasbegriffe über redaktionelle stabile IDs; Strategie selbst wählen oder eigene formulieren, erproben und freiwillig als hilfreich/teilweise/nicht passend/unklar reflektieren, schließen oder archivieren. **Backup-Gate:** Der seit M7.3 verpflichtende, verschlüsselte Backup-Mechanismus wird vor Speichern neuer Strategieexperimente erweitert; Datenbereichsauswahl, Überschreibwarnung und EKB1-Altimport bleiben verpflichtend, alte Sicherungen dürfen neue Daten nicht still löschen. **Exit:** Roundtrip auf zweitem Testgerät und keine Klartextreste.

### M7.5 – Verständliche, freiwillige Visualisierungen (erweiterter UX-Umfang)
**Leitprinzip:** Eine Grafik ist nur dann sinnvoll, wenn sie die Interpretation selbst dokumentierter Einträge vereinfacht, ohne sie als objektive Gefühls-/Gesundheitsmessung erscheinen zu lassen. **Kein neues Bottom-Navigationsziel:** Alles bleibt im bestehenden Rückblick und wird nach Bedarf aufgeklappt; zuerst Überblick und Belege, dann Details.

**M7.5.0 – Design- und Verständlichkeitsprüfung (zwingend vor Code):**
- Gegenüberstellung von Text-/Listenansichten und Visualisierungen anhand ausschließlich fiktiver Testtagebücher; überprüfbarer Nutzen für Auffindbarkeit, Lesbarkeit, Verständnis und freiwillige Nutzung.
- Prototyp bei 320 dp und 200 % Schrift, TalkBack-Semantik und Dark Mode, Prüfung der Farbkodierung und neutraler Wortwahl. Diagramme dürfen nicht schneller zu falschen Schlüssen verleiten als die Textansicht.
- Entscheidung pro Grafik nach messbarem Mehrwert. Keine Grafik allein aufgrund ihrer Optik.

**M7.5.1 – Priorität 1: unmittelbare Orientierung (kleine, verständliche Balken):**
- Horizontale Balkendiagramme für die ausdrücklich ausgewählten **Gefühls- und Bedürfnisnennungen**, nach Nennungen geordnet. Label und exakte Anzahl direkt am Balken, optionaler Zeitraumfilter; jeder Balken öffnet dieselben konkreten Einträge wie die M7.1-Liste.
- In M7.2-Beobachtungskarten optionale, dezente Vergleichsbalken mit **Treffern / korrektem Nenner**, zusätzlicher Textangabe beider Zahlen und Link zu den Belegen. Nicht die Relation als Ursache, Stärke eines Bedürfnisses oder Vorhersage deuten.
- Die Text-/Listenansicht bleibt ohne Zusatzschritte erreichbar und inhaltlich vollständig.

**M7.5.2 – Priorität 2: Zeit und Dokumentationsaktivität:**
- Zeitverläufe: Wochenweise Zahl der **dokumentierten Nennungen** eines vom Nutzer ausgewählten Gefühls sowie die Zahl der erfassten Einträge als Bezugsgröße. Keine geglätteten Stimmungskurven, keine Fortschrittsnote, keine Trendbehauptung bei dünner Datenlage. Fehlende Tage als „kein Eintrag“, nicht als „Gefühl nicht vorhanden“ darstellen; bei wenig Daten stattdessen erklärenden Leerzustand.
- Neutraler Tagebuchkalender mit Anzahl **gespeicherter Einträge pro Tag** und antippbaren Tagen. Intensität und Farben kodieren nicht „gute“/„schlechte“ Tage; keine Streaks, kein Druck, keine moralische Wertung.
- Wöchentliche Diagramme und Kalender haben eine genauso erreichbare, chronologische Text-/Tabellenansicht mit identischen Ergebnissen und Eintragsnavigation.

**Bewusst ausgeschlossen:** Kreis-/Donutdiagramme mit vielen Gefühlen, komplexe Kausal-Netzwerke, rote/grüne Stimmungstage, psychologische Scores, Gamification/Streaks, automatische Empfehlungen aus Musterbalken sowie ungefragte Intensitäts-Durchschnittswerte. Eine spätere Änderung benötigt eine neue fachliche/UX-Entscheidung.

**Gemeinsame Daten- und Accessibility-Regeln:**
- Alle Zahlen stammen aus dem bestehenden flüchtigen, authentifiziert geladenen JournalSnapshot und denselben deterministischen M7.1/M7.2-Use-Cases. Keine gesonderte unverschlüsselte Diagrammdatei, kein Analytics/Netzwerk, kein Freitext-Mining.
- Gleiche Filtergrenzen, lokale Kalendertage/DST, Zähler/Nenner und stabile Tagebuch-IDs in Diagramm und Text; Änderungen/Löschungen aktualisieren die Anzeige ohne veraltete Belege. Maximal drei Musterhinweise bleiben bestehen und sind ausblendbar.
- Prozentwerte nur mit klar ausgewiesenem zutreffendem Nenner; die Anzahl ausgewählter Begriffe kann größer als die Anzahl der Einträge sein, daher **keine impliziten 100-%-Summen**. Nullintensität ist nicht fehlende Angabe.
- TalkBack liest vollständige Achsenbeschriftung, Begriff, Zähler, Nenner/Zeitraum und aktiviert denselben Eintragslink. Kein ausschließlich farblich codierter Inhalt; ausreichend große Berührungsflächen und Kontraste. Funktioniert bei 200 % Schrift und schmalem Bildschirm; bei Platzmangel einfache Liste statt überladener Grafik.
- Dokumentationslücken, kleine Stichproben, 0-Daten-Zustand und beschädigte/gesperrte Journale werden ehrlich kommuniziert; keine Zahlen, solange sich das Journal nicht sicher laden lässt.

**Exit:** (1) Wertgleichheit von Diagramm, Text und Quell-IDs automatisiert getestet; (2) 0/1/10/100/1000/3000 Einträge, Zeitgrenzen/DST, Löschung/Änderung getestet; (3) mindestens fünf freiwillige UX-Sessions mit fiktiven Daten zeigen tatsächlich besseres Verständnis ohne zusätzliche Fehlinterpretation; (4) TalkBack, 200 % Schrift, Dark Mode, Rotation und reale Android-Geräte geprüft. Nicht nachgewiesener Mehrwert oder irreführende Darstellung blockiert die betreffende Grafik, nicht den restlichen Rückblick.

### M7.6 – Qualitäts-/Release-Abnahme
Regression und Performance mit 0/10/100/1000/3000 Testeinträgen; echte Android-Gerätetests, App-Lock/Wechsel, Dark Mode, Rotation, Großschrift, TalkBack; unabhängige Security- und redaktionelle Fachprüfung; mindestens fünf freiwillige UX-Tests mit ausschließlich fiktiven Daten. **Exit:** keine offenen P0-Probleme, nachgewiesene Freigabegates; kritische psychologische Fehlinterpretation blockiert Release.

## 7. Testmatrix (mindestens)
M7-T01: null Einträge → hilfreiche Leerseite.
M7-T02: mehrere Gefühle in einem Eintrag → je Begriff maximal eine Nennung.
M7-T03: mehrere Einträge an einem Tag → unterschiedliche IDs.
M7-T04: lokale Zeitzone/DST/Zeitraumgrenze → korrekt.
M7-T05: Intensität 0 vs null → unverfälscht.
M7-T06: kleine/lückenhafte Stichprobe → kein Muster.
M7-T07: ausreichend belegte Kombination → Nenner/Zähler/Quell-IDs exakt.
M7-T08: Eintrag editieren/löschen → Auswertung und Links aktuell.
M7-T09: fehlende Atlas-/Wissens-ID → Fallback.
M7-T10: Doppelspeichern Erkenntnis → ein Datensatz.
M7-T11: Strategie ohne Nachreflexion → gültig.
M7-T12: beschädigte/falsch entschlüsselte neue Datei → Fehler, keine Überschreibung.
M7-T13: EKB1-Altimport / neuer exportierter Datenbestand → kein stiller Datenverlust.
M7-T14: App-Hintergrund/Sperre → sensible Daten und Caches nicht sichtbar.
M7-T15: große Datenmengen → Benchmark ohne UI-Blockade.
M7-T16: 200-%-Schrift/TalkBack/Dark Mode → alle Daten gleichwertig verständlich.
M7-T17: Diagrammbalken/Tabellen/Zähler verwenden identische M7.1-Zählungen und tatsächliche Journalbelege (auch bei Mehrfachauswahl).
M7-T18: M7.2-Belegbalken zeigen korrekten Nenner statt vermeintlich 100 %; Null-/geringe Datenmenge führt nicht zu scheinbaren Trends.
M7-T19: Zeitverlauf/Kalender nutzen lokale Tage und DST, unterscheiden fehlende Einträge von Abwesenheit eines Gefühls und aktualisieren sich nach Edit/Delete.
M7-T20: Alle Diagrammaktionen per TalkBack/200-%-Schrift erreichbar, Kontrast und vollständige Textalternative ohne Farbcodierungszwang.
M7-T21: Entsperr-/Fehlerzustand → kein Rendern persönlicher Diagrammdaten; keine unverschlüsselten Artefakte, Caches oder Telemetrie.

Testarten: reine JVM-Domain-/Format-/Krypto-Regression, instrumentierte Compose/UI-/Android-Tests, reale Geräte, fachliches Review. CI-Build und signierte Debug-APK ersetzen keine Geräte- oder unabhängige Sicherheitsabnahme.

## 8. Security, Git und Merge
Offline, ohne Account/Analytics oder neue INTERNET-Permission; FLAG_SECURE für sensible M7-Ansichten; bei optionaler App-Sperre nur nach Freigabe laden und bei Hintergrundwechsel den entschlüsselten UI-State löschen. Die bestehende Gerätesperre ist ein UI-Gate, keine Keystore-Schlüsselbindung. Keine forensisch garantierte Löschung behaupten. Verschlüsselter Backup-Import nur nach explizitem Verständnis der ersetzten Daten.

Je Etappe isolierter Branch feature/emotionskompass-m7-*, zunächst Draft-PR mit Akzeptanztests, Risikoliste, Commit-SHA, CI-/APK-Prüfsumme bei Code-PRs. Andere Repo-Projekte nicht verändern. Kein Auto-Merge; Merge und Veröffentlichung erst nach ausdrücklicher Freigabe und bestandenen Release-Gates.

## 9. Änderungsprotokoll
Dieser Plan ersetzt die knappe M7-Beschreibung im ursprünglichen Umsetzungsplan **inhaltlich**, ohne die Grundprinzipien (Vorsicht bei wenigen Daten, keine Diagnosen, verständlicher Leerzustand) aufzugeben. Der ursprüngliche Plan verlinkt dieses Dokument als aktuelle M7-Detailplanung. M7.0 und M7.1 sind inzwischen gemergt; M7.2 ist als Draft-PR #25 in Arbeit. **Ergänzung vom 31.10.2026:** M7.5 umfasst nun priorisierte, freiwillige Balken-, Musterbeleg-, Zeitverlaufs- und Kalendervisualisierungen mit expliziten Design-, Accessibility-, Datenschutz- und Verständnisgates. Die Code-PRs für M7.2 und spätere Stufen bleiben eigenständig, kein Auto-Merge.
