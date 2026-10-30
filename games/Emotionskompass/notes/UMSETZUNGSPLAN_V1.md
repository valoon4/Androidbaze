# Emotionskompass – inkrementeller Umsetzungs- und Qualitätssicherungsplan v1.0
Stand: 28.10.2026 · Baseline zu Produkt- und Architekturkonzept

## 1. Arbeitsmodus

Wir planen die Gesamtarchitektur vorab, implementieren aber immer nur einen **kleinen, durchgängig funktionierenden vertikalen Ausschnitt**. Nach jedem Meilenstein existieren eine reproduzierbare Build-Konfiguration, Tests und ein prüfbarer Stand. Keine Massengenerierung sämtlicher Funktionen in einem Schritt. Keine bestehende Androidbaze-App wird dafür überschrieben.

**Jeder Entwicklungsschritt hat:**
- Ziel/Nutzerwert; Umfang und bewusst ausgeschlossene Teilfeatures.
- Screens + Interaktionen, notwendige Domain-/Datenänderungen, Edge Cases.
- Automatisierte Tests und manuelle Prüfschritte.
- Akzeptanzkriterien; Abnahme-Notiz und gegebenenfalls Screenshots mit ausschließlich fiktiven Daten.
- Commit/PR, Changelog, lauffähiges Build-Artefakt soweit Buildumgebung vorhanden.

**Verbindliche Arbeitsregel:** Abweichungen vom Pflichtenheft bekommen eine neue Version/Entscheidung (ADR), nicht stilles „Wegoptimieren“. Bestehende User-Flows werden vor jeder Änderung mit Regressionstests geschützt.

## 2. Meilensteinplan mit Exit-Kriterien

| Phase | Ziel und Output | Abnahme / Exit-Gate |
|---|---|---|
| M0 – Konzeption (dieser Stand) | Produkt-, UX-, Architektur- und Umsetzungsspezifikation im GitHub-Branch | Dokumente überprüfbar, Versionierung vorhanden, bestehende Projekte unberührt |
| M0.5 – UX-Prototyp | Fünf zentrale Flows klickbar: Start, Atlas, Detail, Navigator, Reflexion/Abschluss; Design Tokens und Navigation | Alle Hauptaktionen mit fiktiven Daten durchspielbar; mindestens fünf kurze Nutzertests als Ziel vor Design-Freeze |
| M1 – Android-Grundgerüst | Isoliertes Android-Unterprojekt, Compose Theme D, Navigation, Onboarding, Start, CI | ./gradlew test assembleDebug erfolgreich im Unterprojekt; APK startbar; Root-Build bleibt unverändert |
| M2 – Wissenssystem | 24 geprüfte Emotionsdatensätze, Kategorien, Suche, Detailseiten, Vergleich, Katalogvalidator | Suche inklusive Synonyme/Umlaute; Offline; kein Duplikat; Detail->Vergleich->zurück korrekt |
| M3 – Gefühlsnavigator | Schneller Check-in, Unklar/Mehrfachauswahl, optionale Intensität, lokaler Entwurf | Unklare Gefühle und 0–n Auswahl erlaubt; Unterbrechung überlebt; kein automatisches Urteil |
| M4 – Reflexion + Tagebuch | Vertiefung, Zusammenfassung, persistente Einträge, Edit/Delete, Datenexport | Transaktionen und Migrations-/Löschtests; Navigation ohne Datenverlust; ausschließlich fiktive Testdaten |
| M5 – Sicherheits-/MVP-Freigabe | Nachgewiesene DB/Draft-Verschlüsselung, Backupregeln, Datenschutz- und Accessibility-Check, mindestens 80 redaktionell geprüfte Begriffe | Alle P0-Kriterien grün; kein Release ohne Security-Gates; reproducible Debug-APK |
| M6 – Lernfunktionen | Emotion des Tages, Favoriten, differenzierende Übungen, freiwillige Wiederholung | Lernen nicht strafend, Mehrdeutigkeit erläutert, lokale Historie korrekt |
| M6.3 – Strategiewelt: Coping und Emotionsregulation | [M6.3-Konzept](M63_STRATEGIEWELT_KONZEPT.md): sieben didaktische Familien, kuratierter Strategieatlas (Zielgröße 30 Karten), 12 vertiefende Artikel, drei freiwillige Lernreihen und 15 Fall-/Vergleichsübungen; Brücke von Bedürfnissen zu M7 | Keine pauschal guten/schlechten Strategien; überprüfbare Quellen/Behauptungen, mehrfache Familienzuordnung, sichere Beispiele, Nutzertests und redaktioneller Fachreview; Umsetzung erst nach separater Freigabe |
| M7 – Rückblick und Selbsterkenntnis | Erweiterter [M7-Funktions- und Umsetzungsplan](M7_RUECKBLICK_ERKENNTNISSE_PLAN.md): Zeitraumfilter, Gefühls-/Bedürfnisübersichten, belegbare Musterhinweise, eigene Erkenntnisse, Reflexionsfragen, Wissenswelt, Strategieexperimente und Zeitvisualisierungen | Keine Diagnosen/Scores; 0-Daten-Zustand; Quelleneinträge; Verschlüsselung, Backup-/Migrationstests, Barrierefreiheit und explizite Merge-Freigabe |
| M8 – Optionale Erweiterungen | App-Sperre, Erinnerungen, interaktives Körperbild und optional später KI | Separat priorisierte ADRs, Consent, Datenschutzprüfung und neue Tests |

**MVP definiert als M1–M5**, nicht schon als erstes installierbares Grundgerüst. M6/M7 sind wichtige P1-Ausbaustufen, aber nicht zwingend für den ersten vollständig nutzbaren Kern. Der Anteil Wissen/Reflexion bleibt im MVP gleichwertig.

## 3. Verbindliche Akzeptanzszenarien

Die folgenden Szenarien werden als Tests/Checklisten implementiert; alle personenbezogenen Beispiele sind fiktiv.

### AC-01: Start ohne Konto
**Gegeben** Erstinstallation, **wenn** die Person „Ohne Konto beginnen“ wählt, **dann** gelangt sie ohne Registrierung und ohne Netzverbindung auf die Startseite.

### AC-02: Atlas-Suche
**Gegeben** Katalog enthält „Frustration“, **wenn** nach „frust“ gesucht wird, **dann** erscheint dieser Begriff und öffnet seine redaktionelle Detailseite. Synonyme und Groß-/Kleinschreibung werden berücksichtigt; eine erfolglose Suche zeigt einen hilfreichen leeren Zustand.

### AC-03: Bidirektional
**Gegeben** Emotionsdetail „Frustration“, **wenn** „In Reflexion übernehmen“ gewählt wird, **dann** erscheint „Frustration“ vorausgewählt im Check-in. Ein Besuch im Atlas während des Check-ins löscht die übrigen Eingaben nicht.

### AC-04: Nicht sicher
**Gegeben** neuer Check-in, **wenn** alle Emotionsfelder übersprungen werden, **dann** ist „Zusammenfassung“ möglich, kann leer/unklar bleiben und lässt sich bei ausdrücklicher Bestätigung speichern.

### AC-05: Mehrere Emotionen
**Gegeben** ein Check-in, **wenn** „Erleichterung“ und „Trauer“ gemeinsam ausgewählt werden, **dann** werden beide unabhängig mit optionalen Intensitäten gespeichert. Ein späterer Wechsel der Intensität von A verändert B nicht.

### AC-06: Unterbrechung
**Gegeben** bereits geschriebene Situation und ausgewählte Emotionen, **wenn** Gerät rotiert / Activity neu angelegt / App beendet und erneut geöffnet wird, **dann** kann der lokale Entwurf ohne Textverlust wiederhergestellt oder bewusst verworfen werden.

### AC-07: Eintrag editieren
**Gegeben** ein bestehender Eintrag, **wenn** Emotionen/Text korrigiert und gespeichert werden, **dann** bleibt dieselbe Entry-ID erhalten, modified_at aktualisiert sich, und die Änderung ist nach Neustart sichtbar.

### AC-08: Löschen
**Gegeben** ein Eintrag mit 3 Emotionen, **wenn** Löschung bestätigt wird, **dann** verschwindet er samt Join-Daten aus UI und DB. „Alle persönlichen Daten löschen“ löscht persönliche Einträge, Entwürfe und Lernhistorie, nicht das allgemeine Lexikon.

### AC-09: Keine Suggestion
**Gegeben** eine unbekannte emotionale Situation, **wenn** die App Vorschläge anzeigt, **dann** enthalten Formulierungen keine festen Diagnosen, und die freie Suche nach anderen Gefühlen bleibt verfügbar.

### AC-10: Lokaler Betrieb
**Gegeben** Flugmodus, **wenn** Atlas, Vergleich, Check-in und Tagebuch genutzt werden, **dann** funktionieren alle P0-Aufgaben vollständig, einschließlich Speichern, Suchen und Editieren.

### AC-11: Schutz persönlicher Daten
**Gegeben** eine produktionsnahe Release-Konfiguration, **wenn** DB/WAL/Draft-Backingfiles, Backupregeln und Exportfunktionen geprüft werden, **dann** liegen keine privaten Inhalte ungeschützt in Dateien/Logs/automatischem Cloudbackup; manuelle Exportwarnung ist sichtbar. Wenn nicht erfüllt: Release wird blockiert.

### AC-12: Accessibility
**Gegeben** 200 % Android-Schriftgröße und TalkBack, **wenn** durch Start, Atlas und Navigator navigiert wird, **dann** sind wichtige Inhalte erreichbar, beschriftet und ohne farbbasierte Pflichtinformation verständlich.

### AC-13: Idempotentes Speichern
**Gegeben** ein offener Check-in, **wenn** „Speichern“ zweimal schnell betätigt wird, **dann** gibt es genau einen neuen Tagebucheintrag.

### AC-14: Kompatibilität mit bestehendem Repo
**Gegeben** Branch mit Emotionskompass-Änderungen, **wenn** Builds und CI laufen, **dann** bleiben Root-Wormblade, games/TimeFlow und andere Projekte unverändert; Emotionskompass wird ausschließlich im Unterprojekt gebaut.

## 4. Review-Checkliste pro PR

- [ ] Feature oder Bug mit Anforderungs-ID (Fxx) und AC-Referenz angegeben
- [ ] Nur vorgesehene Projektpfade geändert; andere Androidbaze-Projekte nicht betroffen
- [ ] Unit-/Integrationstests passend zum Scope vorhanden und grün
- [ ] App/Debug-APK erfolgreich gebaut (bei Code-PR), genaue Commit-SHA angegeben
- [ ] Manuelle Hauptnavigation funktioniert, Back/Rotation/Unterbrechung geprüft
- [ ] Offline und Datenschutzprinzipien eingehalten; keine sensiblen Daten in Logs/Artefakten
- [ ] Texte deutsch und respektvoll; keine diagnostischen Versprechen
- [ ] Screenreader, Schriftvergrößerung, Kontraste und Touchflächen geprüft
- [ ] Migrationen/Export/Löschtests bei Datenmodelländerungen geprüft
- [ ] Relevante Dokumentation/ADR/Changelog aktualisiert
- [ ] Bekannte Einschränkungen dokumentiert, kein ungeprüftes „fertig“

## 5. Manuelle Nutzererprobung (nach M0.5, M3 und M5)

**Aufgaben:** „Finde ein Wort für eine unklare Emotion“; „Unterscheide Frustration und Enttäuschung“; „Halte zwei Gefühle zu demselben Ereignis fest“; „Bearbeite eine Erinnerung“; „Lösche alle Einträge“. Beobachten: Abbruchstellen, Unsicherheit, Verwechslungen und subjektives Gefühl von Kontrolle.

**Evaluationsfragen (freiwillig):**
- Wann fühlte sich die App wertend oder belehrend an?
- War immer klar, ob etwas nur vorgeschlagen oder wirklich gespeichert wurde?
- Welche Wörter/Beispiele waren missverständlich?
- Konnte die Person jederzeit abbrechen, ohne etwas zu verlieren?
- Waren die zwei Hauptwege gleich leicht auffindbar?

**Wichtig:** Für Tests nur fiktive Szenarien anbieten. Nutzer sind nicht verpflichtet, intime reale Erlebnisse offenzulegen; keine Aufzeichnung ohne informierte Einwilligung.

## 6. Risiken und Gegenmaßnahmen

| Risiko | Gegenmaßnahme |
|---|---|
| Emotionsbegriffe zu stark vereinfacht | Editorial Guidelines, Varianten, Mehrdeutigkeit, Redaktionsreview; Versionierung |
| Zu viel Bedienkomplexität | Zwei gleichwertige Einstiege, optionale Vertiefung, Progressive Disclosure, Nutzertests |
| Versehentliche psychologische „Diagnosen“ | Neutrale Wortwahl, manuelle Kontrolle, keine automatische Feststellung |
| Verlust persönlicher Daten | Transaktionen, atomare Drafts, Migrations-/Crash-Tests, explizite Exportmöglichkeit |
| Privacy-Falle durch Android Auto Backup | Data Extraction Rules, standardmäßiger Ausschluss sensibler Dateien und manuelle Tests |
| Verschlüsselungsbibliothek inkompatibel | Security-Spike früh; stabilen Stack pinnen; Freigabesperre statt unsicherer Fallback |
| Repo enthält viele andere Apps | Isoliertes Unterprojekt, Path-spezifische CI, keine Root-Konfigurationsänderung |
| Überambitionierte erste Version | P0/P1/P2, klare Exit-Kriterien, vollständige vertikale Inkremente |
| Scheinpräzise Statistiken | Keine klinischen Schlüsse, Datenmengen-Hinweis, neutrale Reflexionsfragen |
| Fehlende Accessibility | Automatisierte Checks + manuelle TalkBack/Schrift- und Kontrasttests |

## 7. Versionierung und Änderungsmanagement

- Diese Basis heißt **Product Spec 1.0 / Architecture 1.0 / Delivery Plan 1.0**.
- Neue Anforderungen werden als Fxx ergänzt, mit Nutzerwert und priorisierten ACxx. Änderungen über Pull Request mit nachvollziehbarer Begründung.
- Wichtige technische Weichenstellungen als ADRs im notes/adr/-Ordner.
- Code-PRs referenzieren Dokumentversion und akzeptierte Abweichungen.
- Kein Merge in main ohne verifizierten Build (ab M1) und zufriedenstellende Tests.
- Semantisches Versionsschema für testbare Builds: v0.1.0, v0.2.0, ...; Changelog mit Status „Prototyp“/„Test“/„Release“.
- Ein GitHub Actions Artefakt ist keine Veröffentlichung. Für eine offizielle APK zusätzlich Signierung/Veröffentlichungsprozess festlegen.

## 8. Nächster umsetzbarer Auftrag: M0.5

Die nächste Etappe ist ein **zusammenhängender klickbarer UX-Prototyp**. Er umfasst S02 Start, S03 Atlas, S04 Detail, S06/S07/S08 Navigator und S09/S10 Reflexion/Abschluss. Fiktive Beispieldaten genügen; keine Datenbank-/Security-Abkürzungen als scheinbar produktive Funktion.

**Vorgehen:** (1) Screens/Farb-Tokens als Wireframes; (2) Klickpfade mit Back-/Überspringen-Zuständen; (3) mobile Lesbarkeit und TalkBack-Grundregeln; (4) Feedback zur Verständlichkeit; (5) Design-Freeze für M1. Android-Code erst nach diesem UX-Durchlauf.

**M0.5-Definition of Done:** Beide Einstiege, mindestens ein Vergleich, ein unklarer Check-in mit mehreren Emotionen und eine Zusammenfassung funktionieren als Simulation; Navigation verliert keine Eingaben. Der Prototyp behauptet keine lokale Verschlüsselung, wo noch keine echte Persistenz implementiert ist.
