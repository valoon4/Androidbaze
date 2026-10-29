# Emotionskompass – native Android-App M3

**Version:** 0.3.0-m3 · **Paket:** `com.valoon4.emotionskompass` · **minSdk 26 (Android 8+)**

Dieses Android-Unterprojekt ist vollständig isoliert unter `games/Emotionskompass/src`. Keine anderen Apps im Repository sind von der Entwicklung betroffen.

## Funktionen

### Emotionsatlas M2.2 (weiterhin enthalten)
- 289 offline verfügbare, deutschsprachige Einträge in acht Familien.
- 41 redaktionell formulierte Begriffsvergleiche und 14 thematische Lernpfade.
- Je Begriff Definition, zwei Beispiele, Synonyme, verwandte Begriffe und Reflexionsfrage.
- A–Z-Filter, Suche, Kategorien, Gefühl/Stimmung/Zustand, Entdeckungs-Karten.
- Atlasinhalte bleiben redaktionelle Arbeitsfassung; keine medizinischen Diagnosen und keine externe klinische Validierung.

### M3 – persönlicher Gefühlsnavigator
- Schritt 1: *Ankommen*, optionale körperliche Eindrücke benennen.
- Schritt 2: *Gefühle benennen*, keine, eine oder beliebig viele Begriffe aus dem Offline-Atlas wählen; Suche und Familienfilter.
- Schritt 3: *Nachspüren*, für jedes ausgewählte Gefühl optional Intensität 0 bis 10 einschätzen (0 ist nicht dasselbe wie keine Angabe); optionaler Lebensbereich und freier Text (max. 500 Zeichen).
- Schritt 4: *Zusammenfassung*, ausgewählte Wörter, Intensitäten, Körperhinweise und Gedanken respektvoll anzeigen.
- Aus jedem Schritt freiwillig aussteigen und Angaben **verwerfen**, mit Android-Zurück die Schritte rückwärts durchlaufen. Bei Wechsel des Tabs werden persönliche Angaben ebenfalls verworfen.
- Begriffskarte aus dem Atlas kann den Navigator bereits mit diesem Begriff vorgewählt öffnen.
- Kein Zwang zu Angaben, keine automatischen Interpretationen, Bewertungen, Diagnosen oder Streaks.

### Datenschutz – wichtige Abgrenzung

**M3 speichert keine Check-in-Daten dauerhaft.** Die privaten Inhalte liegen während des aktiven Navigators ausschließlich im Arbeitsspeicher des App-ViewModels. Beim Beenden/Verwerfen werden sie aus dem aktiven Entwurfsmodell entfernt. Ein abgebrochener Check-in ist nicht wiederherstellbar. Nur das **Onboarding-abgeschlossen**-Flag wird wie bisher in DataStore gespeichert.

Es gibt **keine INTERNET-Berechtigung**, keinen Account, keine Analyse und noch kein verschlüsseltes Tagebuch. **M4** entwickelt die lokale, verantwortungsvoll geschützte Persistenz mit transparenter Löschung und Datenexport; **M5** überprüft Sicherheits- und Freigabegates.

## Build und Tests

Java 17, Android SDK 36, Gradle 8.13:

```bash
cd games/Emotionskompass/src
gradle :app:testDebugUnitTest :app:assembleDebug
```

Der isolierte GitHub-Actions-Workflow `.github/workflows/emotionskompass-debug-apk.yml` führt JVM-Tests aus, baut eine signierte Debug-APK, prüft Signatur und Package-ID und bietet das Artefakt `Emotionskompass-M3-debug-apk` an.

Testfälle: Offline-Katalog-Integrität, 289 Wörter, 41 Vergleiche, 14 Pfade sowie Check-in-State-Reducer (Mehrfachauswahl, freiwillige Eingaben, 0–10, Zurück, Verwerfen, Tabwechsel und ungültige IDs).

Weitere Hinweise: `games/Emotionskompass/notes/M3_GEFUEHLSNAVIGATOR.md`.
