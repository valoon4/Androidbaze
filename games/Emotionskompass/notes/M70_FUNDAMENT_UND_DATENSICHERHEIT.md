# M7.0 – Datenschutzsichere Grundlage für Rückblick und Erkenntnisse

**Stand:** 30.10.2026 · **Implementierung:** Draft-PR #23 · **Zielversion:** 0.7.0-m7.0-foundation

## 1. Ergebnis und Grenze der Etappe

M7.0 legt den sicheren, nachprüfbaren **Auswertungsvertrag** für M7.1 und M7.2 an. Es schaltet noch keinen neuen Rückblickscreen frei. Dieser bleibt bis M7.1 bewusst eine Platzhalterseite. Es entstehen keine persönlichen Analyse-Dateien, automatisierten Deutungen, neuen Berechtigungen oder ungesicherten Zustände.

**Tatsächlicher Bestand nach M6.3.4:** verschlüsseltes EKJ1-JSON in `private_journal_v1.enc` (AES-256-GCM mit Android Keystore und Android `AtomicFile`). `JournalEntry` enthält stabile String-ID, `createdAt` und `updatedAt` in UTC-Millis sowie `CheckInDraft`. Strukturierte Gefühle liegen als `Map<String,Int?>` (intensität null/0–10), Bedürfnisse als `Set<String>`, Kontext als freiwilliger `LifeArea?`. **Kein separat gespeichertes Ereignisdatum.** Das EKB1-Backup enthält bisher ausschließlich Journal-Daten. Die alte Room/SQLite-Architekturidee trifft nicht auf die App zu.

## 2. Sicherheitsgrenze und Datenfluss

```text
EKJ1 im Android-App-Speicher (bestehender Keystore/AtomicFile)
  └─ bestehender EncryptedJournalRepository.list(): authentifiziert+entschlüsselt
       └─ AppViewModel.JournalUiState: loaded, busy, error, entries
            └─ insightsSnapshotIfAvailable(): null bei gesperrt, nicht fertig,
               fehlerhaft oder busy; ansonsten flüchtiger JournalSnapshot
                 └─ CalculateOverview + InsightTimeWindow
                      └─ (ab M7.1) Rückblick-UI, Quellenlinks zu Journal-IDs
```

Die **Freigabe zum Snapshot** verbleibt ausschließlich bei `AppViewModel`. `JournalSnapshot.fromEntries` ist eine reine Projektion und selbst kein zusätzlicher Authentifikationsmechanismus: andere Aufrufer dürfen niemals ungesperrt auf private Journal-Einträge zugreifen. In der aktuellen M7.0-Version wird der Snapshot **noch von keiner UI konsumiert und nicht zwischengespeichert**. `loaded` unterscheidet „wirklich leer nach erfolgreichem Lesen“ von „noch nicht gelesen“ und „Entschlüsselung fehlgeschlagen“. Bei Sperrung wird das vorhandene `JournalUiState` wie bisher gelöscht, ein künftiges M7-ViewModel muss abgeleitete Ergebnisse und laufende Rechenjobs beim Sperren ebenfalls freigeben.

Die Projektion übernimmt **ausschließlich** ID, Zeitstempel, explizite Gefühl-IDs samt `null`/Intensitätswert, Bedürfnis-IDs und optionalen Lebensbereich. Nicht übernommen werden Situation, Notiz, Interpretation, eigene Erkenntnis, nächster Schritt, Freitext, Körperwahrnehmung, App-Schritte oder Check-in-Entwurf. Es gibt **keine** unverschlüsselten Caches, Indizes oder neue gespeicherte M7-Datei. Trotzdem sind die strukturierten IDs sensibel und dürfen nicht protokolliert, exportiert oder in Android-Screenshots gezeigt werden.

## 3. Zeit-/Zählregeln als getesteter Vertrag

- Zeitraum: **inklusive lokale Kalendertage**, umgesetzt als `[startLocalDay.atStartOfDay(zone), (endLocalDay+1).atStartOfDay(zone))` in UTC-Millis.
- 7/30/90 Tage: inklusive **heutigen** lokalen Kalendertags, nicht „N×24 Stunden ab jetzt“; freie Auswahl auf max. 3650 Tage begrenzt.
- Sommer-/Winterzeit: Berlin 29.03.2026 hat **23** Stunden, 25.10.2026 hat **25** Stunden; keine `+ 86400000`-Vereinfachung. Clock und Zone werden explizit an die Berechnung übergeben.
- Zeitfilter: `createdAt` bis ein Nutzer-Ereignisdatum wirklich existiert; spätere `updatedAt`-Änderungen verschieben einen Eintrag **nicht** in einen anderen Auswertungszeitraum.
- **Einträge** ≠ **Nennungen**: je Journal-ID ein Eintrag; jede explizite Gefühl-/Bedürfnis-ID höchstens einmal pro Eintrag; mehrere IDs möglich; Quellen-IDs bleiben sichtbar und werden stabil nach Zeit, bei Gleichstand nach ID geordnet.
- Fehlende Auswahl ≠ nicht erfüllt; `__unklar__` ≠ benanntes Bedürfnis; Intensität **0** ≠ **null**; für die Nennungszählung zählen beide expliziten Gefühl-Auswahlen jeweils einmal.
- Keine Auswertung von Notizen oder versteckten Schlussfolgerungen, keine Wochenwertung, kein „emotionale Gesundheit“-Score.
- Bei **0 auswertbaren Einträgen** keine Scheingenauigkeit: Zahlen sind 0, eine prozentuale Evidenz ohne Nenner ist **null**.
- Die `InsightEvidence`-Schnittstelle für M7.2 verlangt tatsächliche **eligible IDs** als Nenner und passende **matching IDs** als Teilmenge. Eine Quote ist weder Kausalität noch eine zuverlässige psychologische Eigenschaft. Es werden in M7.0 **keine** Muster automatisch ermittelt.

## 4. Architektur für persönliche Erkenntnisse und Strategieexperimente (ab M7.3/M7.4)

**Noch keine Implementierung oder Datenspeicherung in M7.0.** Aber die Persistenzentscheidung ist verbindlich:

- Separate versionierte, **Keystore-AES-256-GCM-geschützte AtomicFile-Persistenz** nur für ausdrücklich selbst verfasste `PersonalInsight`- und `StrategyExperiment`-Objekte, getrennt vom bestehenden EKJ1. Eindeutige IDs, UTC-Zeiten, maximale Längen/Datensatzmengen, strikte Validierung, fail-closed bei beschädigten Dateien und keine automatische Überschreibung.
- Kein heimlicher Nachtrag in EKJ1, keine Room-Migration, keine Freitextstatistik, keine Cloud, keine Logs oder unverschlüsselte DataStore-Einträge.
- **Backup-Gate vor Speichern persönlicher Erkenntnisse**: Erst das verschlüsselte, auf zweitem Gerät wiederherstellbare Backup/Importformat und sichere Datenbereichsauswahl oder eine gleich sichere Alternative abschließen. EKB1-Altbackups weiter lesen; Import darf Erkenntnisse/Experimente weder still löschen noch ohne explizite Warnung ersetzen. Das ist eine bewusst vorgezogene Bedingung gegenüber dem ursprünglichen M7-Plan, in dem Export erst M7.4 vorgesehen war.
- Löschen/Ändern eines Tagebucheintrags darf niemals veraltete Belege zeigen. Eigene Erkenntnisse bleiben dem Nutzer erhalten; unzutreffende Referenzen werden entfernt oder sichtbar als verwaist markiert, ohne Text ungefragt zu löschen.
- Diese Maßnahmen sind **vorläufiger Architekturvertrag**: Das konkrete Format, Backup-Versionsschema und die Nutzerauswahl werden vor M7.3 in einem eigenständigen Review mit Rückwärtskompatibilitätstests festgelegt. Keine implizite Freigabe neuer Speicherung durch M7.0.

## 5. Tests und Abnahmeregeln

| Testklasse | Prüfgegenstand | Erwartung |
|---|---|---|
| `InsightsFoundationTest` | Kein Eintrag / keine Quellbelege | null Prozent ohne Nenner; keine angeblichen Muster |
| `InsightsFoundationTest` | mehrere Gefühle/Bedürfnisse je Eintrag | Nennungen je ID korrekt, Einträge separat gezählt |
| `InsightsFoundationTest` | 0 vs. null / __unklar__ | unterscheidbar, kein unbenanntes Bedarfsergebnis |
| `InsightsFoundationTest` | Datenprojektion/Änderungen/Löschen | keine Freitexte, keine Referenzen auf alte Snapshot-IDs im neuen Berechnungslauf |
| `InsightsFoundationTest` | lokale Zeitgrenzen, 7 Tage, DST, manuelle Filter | halb-offen, 23-/25-Stunden-Tage, deterministisch |
| `InsightsFoundationTest` | ungültige IDs, Zeitstempel, Dubletten, >3000 | Auswertung lehnt ungültige Eingabe ab |
| `InsightsFoundationTest` | Nenner/Zähler | eindeutige Quell-IDs, echte Teilmenge |
| `JournalLockTest` | nicht authentifiziert, Hintergrund/Sperre | kein Snapshot, vor erfolgreichem Load auch nicht „0 Einträge“ |
| bestehende Tests | EKJ1, EKB1, Backup, Journalsicherheit und Navigation | Regressionen bestanden |
| GitHub Actions | Kotlin-JVM-Tests, Instrumentierungskompilierung, signierte Debug-APK; Android-Emulator soweit zum Scope sinnvoll | neuester PR-HEAD vollständig grün vor Merge |

**M7.0 nicht erledigt:** M7.1-Rückblick-UI, konkrete Pattern-Heuristik, persönliche Erkenntnispersistenz, Exporterweiterung, Charts, menschlicher TalkBack-/Fachreview. Kein Mergen ohne ausdrückliche Freigabe.

## 6. Entscheidung für M7.1 (nächster Schritt)

Den bereits bestehenden Bottom-Tab **Rückblick** aktivieren und zunächst ausschließlich **Überblick** mit klaren Zahlen, 7/30/90-/freiem Zeitfilter, 0-Daten-Zustand, deutlicher Kennzeichnung „Nennungen“ und klickbaren Eintragsbelegen anbieten. Keine automatische Deutung, kein Leistungswert. Der Nutzer soll bei jedem Wert nachvollziehen können, **welche selbst gespeicherten Einträge** darin berücksichtigt sind. Bereits beim Einstieg mit App-Sperre darf kein Inhalt vor Authentifizierung erscheinen; Fehler beim Entschlüsseln sind keine leeren Datensätze.

Die vollständige Roadmap bleibt `M7_RUECKBLICK_ERKENNTNISSE_PLAN.md`. M7.0 ändert `main` erst nach ausdrücklicher Freigabe.
