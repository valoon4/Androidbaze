# Emotionskompass – native Android-App M4

**Version:** 0.4.0-m4 · **Paket:** `com.valoon4.emotionskompass` · **minSdk 26 (Android 8+)**

Dieses Android-Unterprojekt ist vollständig isoliert unter `games/Emotionskompass/src`. Keine anderen Apps im Repository sind von der Entwicklung betroffen.

## Funktionen

### Emotionsatlas M2.2 (weiterhin enthalten)
- 289 offline verfügbare, deutschsprachige Einträge in acht Familien.
- 41 redaktionell formulierte Begriffsvergleiche und 14 thematische Lernpfade.
- Je Begriff Definition, zwei Beispiele, Synonyme, verwandte Begriffe und Reflexionsfrage.
- A–Z-Filter, Suche, Kategorien, Gefühl/Stimmung/Zustand, Entdeckungs-Karten.
- Atlasinhalte bleiben redaktionelle Arbeitsfassung; keine medizinischen Diagnosen und keine externe klinische Validierung.

### M3.1 – persönlicher Gefühlsnavigator
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

Der isolierte GitHub-Actions-Workflow `.github/workflows/emotionskompass-debug-apk.yml` führt JVM-Tests aus, baut eine signierte Debug-APK, prüft Signatur und Package-ID und bietet das Artefakt `Emotionskompass-M4-debug-apk` an.

Testfälle: Offline-Katalog-Integrität, 289 Wörter, 41 Vergleiche, 14 Pfade sowie Check-in-State-Reducer (Mehrfachauswahl, freiwillige Eingaben, 0–10, Zurück, Verwerfen, Tabwechsel und ungültige IDs).

Weitere Hinweise: `games/Emotionskompass/notes/M3_GEFUEHLSNAVIGATOR.md`.


## M3.1 – Niedrigschwellige Gefühlswahl

Ein Check-in kann **kurz** (drei Schritte) oder **vertieft** (vier Schritte) durchgeführt werden. Bei der Gefühlswahl startet der Nutzer mit sieben alltäglichen Beschreibungen statt der gesamten 289-Wörter-Liste; alle Begriffe bleiben aber ausdrücklich durchsuchbar. Die ausgewählte Wortliste ist beim Scrollen immer sichtbar. Die App zeigt bis zu zwei vorhandene redaktionelle Begriffsvergleiche als freiwillige Lernhilfe, ohne Emotionen automatisch zu interpretieren. Persönliche Angaben bleiben im flüchtigen Arbeitsspeicher und werden verworfen. Details: `games/Emotionskompass/notes/M31_GEFUEHLSNAVIGATOR.md`.


## M3.2 – Eigene Erkenntnisse und nächste Schritte

Der **Kurz-Check-in** bleibt bei drei Schritten. Der **vertiefte Check-in** enthält jetzt einen freiwilligen fünften Schritt, in dem beobachtete Situation, eigene Deutung, mögliche Bedürfnisse, selbst formulierte Erkenntnisse und ein selbst gewählter nächster Schritt getrennt erfasst werden können. Es gibt keine automatischen Diagnosen, Bedürfnis-Zuordnungen oder Handlungsanweisungen. Die Abschlussübersicht zeigt nur persönliche Angaben und die ausgewählten Atlasbegriffe. Ein Bestätigungsdialog schützt vor versehentlichem Verwerfen; während des Check-ins verbirgt `FLAG_SECURE` sensible Inhalte vor Screenshots und Android-App-Vorschauen. **Nach wie vor keine dauerhafte Datenspeicherung.** Details: `games/Emotionskompass/notes/M32_SELBSTERKENNTNIS.md`.


## M3.3 – Bedürfniskompass und Bedürfnisatlas

Ein eigenständiger **Bedürfnisse-Reiter** enthält jetzt **82** redaktionell verfasste Bedürfnisbegriffe in acht Familien, **20** Begriffspaare und **acht** Lernpfade – alles offline. Zu jedem Bedürfnis gibt es Erfüllungs- und Nichterfüllungsbeispiele sowie eine mögliche Strategie, klar vom Bedürfnis unterschieden. Der interaktive **Bedürfniskompass** unterscheidet persönliche Wichtigkeit, mögliche Erfüllung, mögliche Nichterfüllung und Ungewissheit. Alle persönlichen Kompass-Angaben bleiben flüchtig. Der M3.2-Check-in nutzt nun dieselben stabilen Bedürfnis-IDs und zeigt passende Begriffserklärungen direkt bei der Auswahl; ohne automatische psychologische Zuordnungen oder dauerhafte Speicherung. Näheres: `games/Emotionskompass/notes/M33_BEDUERFNISATLAS.md`.


## M4 – Geschütztes Tagebuch

Ein neuer aktiver **Tagebuch-Reiter** erlaubt Check-ins nur nach ausdrücklicher Zustimmung zu speichern, wieder zu öffnen, vollständig zu bearbeiten und nach Bestätigung einzeln oder vollständig zu löschen. Die App speichert lokal ein **AES-GCM-verschlüsseltes, atomar geschriebenes JSON-Dokument**, mit nicht exportierbarem Android-Keystore-Schlüssel. Offline, ohne Cloud, Konto oder INTERNET-Berechtigung. Die persönlichen Inhalte einschließlich Zeitstempeln werden zusammen verschlüsselt. Bei Schlüssel-/Authentizitätsfehlern wird kein bestehender Bestand überschrieben. `FLAG_SECURE` schützt Tagebuch und Check-in vor normalen Screenshots. **Kein Export, keine Wiederherstellung und keine garantierte forensische Löschung**; Gerätewechsel und App-Datenverlust bedeuten Datenverlust. Vor produktiver Verwendung sind unabhängige Security-Tests und eine reale Geräteabnahme erforderlich. Details: `games/Emotionskompass/notes/M4_ENCRYPTED_JOURNAL.md`.
