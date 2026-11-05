# M7.5.2a – Neutraler Tagebuchkalender: Umsetzung und Abnahme

Stand: 05.11.2026. Eigenständiger Unterabschnitt von M7.5.2; Wochenkarten und freiwillige Wochenreflexion folgen erst in M7.5.2b/c. Grundlage ist der freigegebene Konzeptstand aus PR #38.

## Neue Funktion

Im bestehenden **Rückblick → Übersicht & Muster** erscheint für ein sicher geöffnetes Tagebuch der freiwillige, standardmäßig geschlossene Einstieg **„Tagebuch nach Datum finden“**. Er ist keine Hauptnavigation und kein tägliches Ritual.

- Bei Öffnen: Monatsüberschrift mit Jahr, vorheriger/nächster Monat und ausdrücklicher Hinweis, dass **alle sicher geladenen Journaleinträge** des Monats gezeigt werden, **unabhängig von 7/30/90-Tage-Filtern**.
- Bei ausreichender Breite (mindestens 7×48 dp für Wochentagszellen) und höchstens 130 % Schriftgröße: neutrales Raster Montag bis Sonntag, Tageszahl und numerische Eintragszahl bei vorhandenen Daten; bei null Einträgen keine farbliche Kennzeichnung. Keine Streaks, Stimmungs-/Qualitätssymbole, Punktebewertungen oder „Lücken schließen“-Hinweise.
- Bei 320 dp/200 % Schrift, schmaleren Ansichten oder eigener Auswahl **„Als Datumliste ansehen“**: inhaltlich gleichwertige chronologische Monatsliste **mit allen 28–31 Tagen**, eindeutigem vollem Datum und exakten Zahlen. Ist ausreichend Platz vorhanden, lässt sich zum Raster zurückwechseln.
- Tageswahl öffnet eine eigene Datumsebene mit **vollständigem Datum, dokumentierter Eintragszahl und den tatsächlich zugehörigen Tagebuchbelegen**. Die Karten zeigen den Zeitpunkt und **„Tagebucheintrag ansehen“**, der bestehende echte Journal-Deep-Link wird wiederverwendet. Leere Tage: „Daraus lässt sich nichts über dein Erleben ableiten“; **kein** Eintrag wird erzeugt.
- **„Zurück zum Monat“** führt zur gewählten Monatsansicht zurück. Beim Besuch eines bestehenden Tagebucheintrags bleiben Monat und Tag während der laufenden entsperrten Sitzung als bloße Navigation im **Arbeitsspeicher** erhalten. Android-Back im Rückblick kehrt aus der Tagesebene zum Monat zurück.

## Sicherer Datenpfad

`DiaryCalendar.month(snapshot, month, zone)` erstellt einen reinen, transienten `DiaryCalendarMonth` aus dem **bestehenden authentifizierten `JournalSnapshot`**. Er enthält nur Datum und stabile Journal-IDs, **keine Freitexte, Gefühls-/Bedürfnisbewertungen oder neue Speicherung**.

- Monat und lokale Tage basieren auf `InsightTimeWindow.inclusiveDays(...)`, dessen halb-offene UTC-Grenzen lokale Sommerzeitumstellungen berücksichtigen.
- Mehrere Einträge pro Tag werden einzeln gezählt und in der vorhandenen Journal-Reihenfolge verlinkt; Daten aus benachbarten Monaten erscheinen nicht. Monatsansichten haben höchstens 31 Tageszellen; im Tagesdetail sind selbst 3000 IDs **LazyColumn-Items**, keine Massenkomposition.
- Vertrauliche Zähler und ID-Links erscheinen **nur** nach sicherem Laden. Fehlt der Snapshot, ist die App gesperrt, ist ein Fehler aufgetreten oder wird neu geladen, verschwindet der Kalender. Die App-Shell setzt die nicht persistierte Auswahl bei Sperre/unsicherem Laden und nach Änderungen, Löschen oder Import zurück.
- Es gibt **keine** Datenbank, neue Kalendertabelle, Preferences, Analytics, Netzkommunikation oder zusätzliche Exportbereiche. Vorhandene Keystore-/EKB3-Logik und Rückblick-Zählung bleiben unverändert.

## Automatisierte Abnahme und offene Gates

- Neue reine JVM-Tests: 0/1/10/100/1000/3000 fiktive Einträge, mehrere Einträge am selben Tag, tatsächliche IDs, leap day, lokaler Monats-/Jahreswechsel und Sommerzeit, unabhängiger Statistikfilter.
- Vier neue Compose-Instrumentierungstests: Opt-in und Belegnavigation, Jahres-/Leer-Monat, Lock-/Fehler-Gate und Wiederkehr vom Journal-Detail. In der kleinen 320dp/200%-Variante werden Datumliste und lesbare Aktionen statt enger Raster erprobt. Insgesamt erwartete **39 Compose-Tests pro Schriftgröße (78 Ausführungen)**, plus die bestehenden drei EKB2- und neun EKB3-Backup-Tests.
- **Nicht als abgeschlossen behaupten:** eigenständige Testpersonenstudien, physische TalkBack-Hörprüfung, reale Geräte-/Rotation-/Dark-Mode-Abnahme und unabhängige Fach-/Sicherheitsreviews. Die laufende Entwicklung überspringt diese manuell bestätigungspflichtigen Gates nur auf Nutzerwunsch, ohne sie als bestanden auszuweisen.

**Eigener Draft-PR. Kein Merge ohne ausdrückliche Freigabe.**
