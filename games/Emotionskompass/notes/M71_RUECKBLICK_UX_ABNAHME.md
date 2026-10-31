# M7.1 – Persönlicher Rückblick: UX, Datenregeln und Abnahme

**Status:** Implementierung in Draft-PR #24. **Version:** 0.7.1-m7.1-review. **Kein Merge ohne Freigabe.**

## Produktziel

Nutzerinnen und Nutzer können ihre *ausdrücklich gespeicherten* Erfahrungen übersichtlich betrachten und selbst prüfen, auf welche Tagebucheinträge eine Anzahl zurückgeht. Der Rückblick ist ein Ort für selbstbestimmtes Nachdenken, keine psychologische Analyse oder Bewertung.

## Funktionaler Ablauf

1. Den bestehenden unteren Reiter **Rückblick** öffnen. Keine zusätzlichen globalen Navigationseinträge.
2. **7, 30 oder 90 lokale Kalendertage** (einschließlich heute) auswählen oder über **Eigener Zeitraum** zwei Kalenderdaten wählen. Der Filter zeigt die tatsächlichen lokalen Randdaten an. Bei umgekehrten oder zu großen Intervallen wird nichts als gültiges Ergebnis ausgegeben.
3. Hauptübersicht: **gespeicherte Einträge**, **Gefühlsnennungen**, **Bedürfnisnennungen**. Die Zahlen werden ausdrücklich getrennt; kein positiver/negativer Gefühlsscore.
4. Gefühl/Bedürfnis antippen → **konkrete Einträge** mit dem ausgewählten Begriff im Zeitraum → Tagebucheintrag öffnen. Android-Zurück beziehungsweise „Zurück zum Rückblick“ führt zum Rückblick; dort bleibt der Zeitraum erhalten. „Alle Einträge ansehen“ enthält den ganzen Zeitraum.
5. Bei 0 Einträgen: verständliche, druckfreie Leerseite und freiwilliger Check-in. Bei wenigen Einträgen: keine automatische Musterinterpretation.
6. Bei beschädigter/falsch entschlüsselter Journaldatenbank: **keine Nullstatistik** oder vorhandenen Einträge; Erklärung und ausdrücklicher Wiederholversuch. Bei noch ladenden Einträgen: Ladezustand.
7. Bewusst ausgewähltes **„Ich weiß es noch nicht“** für Bedürfnisse wird neutral separat erwähnt, nicht als Bedürfnis oder unerfüllter Bedarf gezählt.

## Daten und Datenschutz

M7.1 nutzt ausschließlich die in M7.0 eingeführten `JournalSnapshot`, `CalculateOverview` und `InsightTimeWindow` aus dem bestehenden, authentifiziert geladenen Tagebuch. Zeiträume verwenden lokale Kalendertage und halb-offene UTC-Grenzen; auch Sommer-/Winterzeit. **Keine neuen Dateien, Speicherformate, Schlüssel, Indizes, Netzberechtigungen oder Hintergrundberechnungen.**

Für Rückblick und die von dort geöffneten Journal-Details gilt `FLAG_SECURE` wie beim Tagebuch. Der Snapshot wird nur für ein geladenes, nicht gesperrtes, nicht beschäftigtes und fehlerfreies Journal erzeugt; bei Sperre und bei Journaländerungen ist das Ergebnis kein langlebiger Cache. Persönliche Wort-/Belegauswahl wird nicht in Android Bundle SavedState geschrieben. Kalenderauswahl kann beim Zurückwechseln erhalten bleiben.

Quellbezüge sind `JournalEntry.id`, keine Fundstellen in privaten Freitexten. Ist ein Eintrag entfernt worden, darf er nicht mehr über den Rückblick als noch vorhanden angezeigt werden. Ein aktualisierter Eintrag wird aus den neuen expliziten Begriffen berechnet; `updatedAt` beeinflusst nicht den ursprünglichen Zeitraum (`createdAt`). `null`-Intensität und Wert `0` sind unterschiedliche Zustände, zählen aber jeweils einmal als explizite Gefühlsnennung.

## Verständlichkeit und Accessibility

- Deutsche Formulierungen mit **Nennungen in Einträgen** statt „oft gefühlt“ und ohne Diagnose-/Kausalformulierungen.
- Wichtige Überschriften für Screenreader als Überschrift markiert; jede Begriffskarte ist ein zusammenhängendes Klickziel mit beschreibender Aktion.
- Zeiträume als horizontal scrollbare, beschriftete Filterchips; frei wählbarer Zeitraum über große, eigenständige Datumsschaltflächen.
- Mit 200%-Schrift und schmalem 320dp-Viewport auf Android-35-Emulator zu prüfen. **Echter TalkBack-Menschentest** bleibt gesonderte Release-Voraussetzung.

## Testmatrix und Grenzen

- M7.0 JVM-Regression: 0 / mehrere Begriffe / `null` vs `0` / `__unklar__` / DST 23-/25-Stunden-Tag / Änderungen und Löschungen / ID-/Zeitstempelvalidierung.
- M7.1 Emulator: präzise Nennungszahlen, Begriff→Beleg→Eintrag; druckfreier Leerzustand; Verschlüsselungsfehler ohne „0 Einträge“; eigener Zeitraum bei 100 % und 200 % Schrift.
- Bestehende Strategie-/Bedürfnisnavigationstests bleiben erhalten, jetzt zusammen mit M7.1 als vollständiger Emulator-Regressionstest.
- Build: JVM-Tests, Android-Instrumentierungskompilierung, signierte Debug-APK mit SHA256.

**Nicht in M7.1:** interpretierte Muster (M7.2), eigene dauerhafte Erkenntnisse und ihre Backup-Erweiterung (M7.3), Strategieexperimente (M7.4), Zeit-Charts (M7.5), unabhängige psychologische Wirksamkeitsbehauptungen.

**Merge-Gate:** vollständige CI des letzten PR-HEAD, geänderte Dateien nur Emotionskompass und separate explizite Benutzerfreigabe.
