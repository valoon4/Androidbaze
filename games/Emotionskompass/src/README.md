# Emotionskompass – native Android-App M6.3.3

**Version:** 0.6.3.3-m6.3.3 · **Paket:** `com.valoon4.emotionskompass` · **minSdk 26 (Android 8+)**

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


### M6.3.1a – Gemeinsame Navigation und neue Startseite
- Fünf feste Hauptbereiche: **Start – Entdecken – Wissen – Tagebuch – Rückblick**. Innerhalb von Entdecken sind Gefühle, Bedürfnisse und Strategien drei gleichrangige Atlas-Tabs.
- Neu strukturierte Startseite mit freiwilligem Check-in, drei gleichrangigen Atlas-Kacheln und direktem Wissenswelt-Einstieg. Übungen/Wortkarten/Wiederholungen bleiben über Wissen erreichbar.
- Auf Tablets seitliche NavigationRail, auf Smartphones feste Bottom Navigation. Bei großer Schrift werden die Atlas-Tabs und Startkarten nicht abgeschnitten, sondern untereinander angeordnet.
- Suche, Filter und Scrollzustand beim Wechsel weitgehend erhalten; System-Zurück schließt Detailseiten; Strategie → Wissensartikel → Zurück führt zur vorherigen Strategie.
- Details und manuelle Abnahmematrix: notes/M631_NAVIGATION_UX.md.

### M6.3.1 – Strategieatlas (Referenzpaket)
- 30 offline lesbare Strategiekarten einschließlich Problemlösen, bewusster Ablenkung, Neubewertung, sozialer Unterstützung und Akzeptanz.
- Direkt erreichbar über **Start → Strategien** und **Entdecken → Strategien**, gemeinsam und gleichberechtigt mit Gefühls- und Bedürfnisatlas; Suche mit Umlaut-/ASCII-Toleranz, sieben didaktische Familien mit Mehrfachzuordnung, Vorteile/Nachteile, Grenzen, Beispiele, Alternativen und Quellen.
- Alle Querverweise auf bestehende Wissensartikel sind freiwillig; die 82 bisherigen Bedürfnis-Strategiebeispiele bleiben unangetastet. Kein automatisches persönliches Strategie-Matching, kein Nachverfolgen von Strategieversuchen (erst M7).
- Der Katalog ist ein **fachredaktioneller Entwurf**, kein unabhängig begutachtetes Behandlungsangebot. Die 25 Ergänzungskarten sind nun integriert; die unabhängige Fach- und Geräteprüfung folgt weiter in M6.3.3.
- JSON-Schema, referenzielle Integrität und Suche werden per JVM-Test geprüft. Externe Quellen werden nur nach bewusstem Tippen im installierten Browser geöffnet; die App selbst benötigt kein Netzwerk.

### M6.3.2 – Wissenswelt und Strategien vertiefen
- 12 neue Artikel als drei vierteilige, vollständig offene Lernreihen (Grundlagen, Belastung, Flexibilität). Die 20 alten Artikel und vier Reihen bleiben erhalten: **32 Artikel und 7 Reihen** insgesamt.
- 15 freiwillige Praxis-/Vergleichsfälle mit zwei nachvollziehbaren Kontextoptionen, neutralem „Ich bin unsicher“-Weg und erklärtem Feedback erst nach einer Wahl. Keine Bewertung der Person, kein Score und keine Speicherung der Antworten.
- Direkt über Wissen → Praxisfälle verfügbar. Alle Quellen sind offline im Text verlinkt und öffnen sich nur auf Wunsch extern; keine neue Internetberechtigung.
- Strikte Versionierung/Referenzprüfungen, vollständige JVM-Tests, behutsamer Fallback zu alten Artikeln bei einem defekten neuen Inhaltsasset.
- Details und noch offene fachliche/manuelle Abnahme: `games/Emotionskompass/notes/M632_WISSENSWELT_VERTIEFUNG.md`.

### Ergänzung: Bedürfnisse ↔ Strategien nachvollziehbar verbinden
- Alle 82 Bedürfnisse einzeln gegen die 30 vorhandenen Strategien geprüft. **76 Bedürfnisse** erhalten insgesamt **143 bedingte, redaktionell begründete und direkt anklickbare Verknüpfungen**; **6** ohne fachlich genügend konkreten Bezug.
- Je Bedürfnis maximal drei Optionen **mit einem eigenen „Wann könnte das passen?“-Grund** statt bloßen Titelchips oder automatischer Erkennung. Strategieartikel zeigen die verknüpften Bedürfnisse mit einer aufklappbaren Liste.
- Keine pauschale Empfehlung für Schlaf, Gesundheit, Ernährung, Spiritualität, Humor oder Spontaneität; keine krankheitsbezogene Pseudoberatung und keine Musterkarten als Handlungsoption.
- Acht zusätzliche strikt validierte Offline-Katalogteile; bei beschädigtem Zusatzasset sichere Rückkehr zu den früheren Links. Alle Tagebuch- und Lernstandsdaten bleiben unverändert.
- Vollständige redaktionelle Beurteilung: `games/Emotionskompass/notes/M633_BEDUERFNIS_STRATEGIE_ABGLEICH.md`. **Kein unabhängiger psychologischer Einzelbeleg für die 143 Verknüpfungen.**

### M6.3.3 – Übersichtliche Verbindungen statt zusätzlicher Funktionen
- Strategieatlas: kurzer Einstieg, freiwilliges Glossar, wichtige Grenzen direkt im Überblick und maximal drei wirklich benannte Vertiefungsartikel je Karte.
- Alle 30 Karten mit den zwölf neuen Vertiefungen redaktionell verbunden; bisherige Artikel bleiben erreichbar.
- Direkte, gezielte Wechsel **Strategie ↔ Bedürfnis**, **Strategie ↔ Praxisfall**, **Strategie ↔ Wissensartikel**; nachvollziehbarer Zurück-Weg und eine begrenzte Historie für Strategie-Alternativen.
- Bedürfnisdetails zeigen nur ausdrücklich erfasste, harmlose Beispielstrategien; für fehlende Verknüpfungen wird nichts geraten.
- Volltextsuche findet auch Grenzen, Fehlannahmen und Reflexionsfragen; Quellenlinks für Screenreader unterscheidbar beschriftet. Die fünf Hauptnavigationseinträge bleiben unverändert.
- Ausführliche kritische Gegenüberstellung, Testmatrix und noch **nicht** abgeschlossene Geräte-/Fachprüfungen in `games/Emotionskompass/notes/M633_UX_QUALITAETSRUNDE.md`.

## Build und Tests

Java 17, Android SDK 36, Gradle 8.13:

```bash
cd games/Emotionskompass/src
gradle :app:testDebugUnitTest :app:assembleDebug
```

Der isolierte GitHub-Actions-Workflow `.github/workflows/emotionskompass-debug-apk.yml` führt JVM-Tests aus, baut eine signierte Debug-APK, prüft Signatur und Package-ID und bietet das Artefakt `Emotionskompass-M631-debug-apk` an.

Testfälle: Offline-Katalog-Integrität, 289 Wörter, 41 Vergleiche, 14 Pfade sowie Check-in-State-Reducer (Mehrfachauswahl, freiwillige Eingaben, 0–10, Zurück, Verwerfen, Tabwechsel und ungültige IDs).

Weitere Hinweise: `games/Emotionskompass/notes/M3_GEFUEHLSNAVIGATOR.md` und `games/Emotionskompass/notes/M631_STRATEGIEATLAS_30_KARTEN.md`.


## M3.1 – Niedrigschwellige Gefühlswahl

Ein Check-in kann **kurz** (drei Schritte) oder **vertieft** (vier Schritte) durchgeführt werden. Bei der Gefühlswahl startet der Nutzer mit sieben alltäglichen Beschreibungen statt der gesamten 289-Wörter-Liste; alle Begriffe bleiben aber ausdrücklich durchsuchbar. Die ausgewählte Wortliste ist beim Scrollen immer sichtbar. Die App zeigt bis zu zwei vorhandene redaktionelle Begriffsvergleiche als freiwillige Lernhilfe, ohne Emotionen automatisch zu interpretieren. Persönliche Angaben bleiben im flüchtigen Arbeitsspeicher und werden verworfen. Details: `games/Emotionskompass/notes/M31_GEFUEHLSNAVIGATOR.md`.


## M3.2 – Eigene Erkenntnisse und nächste Schritte

Der **Kurz-Check-in** bleibt bei drei Schritten. Der **vertiefte Check-in** enthält jetzt einen freiwilligen fünften Schritt, in dem beobachtete Situation, eigene Deutung, mögliche Bedürfnisse, selbst formulierte Erkenntnisse und ein selbst gewählter nächster Schritt getrennt erfasst werden können. Es gibt keine automatischen Diagnosen, Bedürfnis-Zuordnungen oder Handlungsanweisungen. Die Abschlussübersicht zeigt nur persönliche Angaben und die ausgewählten Atlasbegriffe. Ein Bestätigungsdialog schützt vor versehentlichem Verwerfen; während des Check-ins verbirgt `FLAG_SECURE` sensible Inhalte vor Screenshots und Android-App-Vorschauen. **Nach wie vor keine dauerhafte Datenspeicherung.** Details: `games/Emotionskompass/notes/M32_SELBSTERKENNTNIS.md`.


## M3.3 – Bedürfniskompass und Bedürfnisatlas

Ein eigenständiger **Bedürfnisse-Reiter** enthält jetzt **82** redaktionell verfasste Bedürfnisbegriffe in acht Familien, **20** Begriffspaare und **acht** Lernpfade – alles offline. Zu jedem Bedürfnis gibt es Erfüllungs- und Nichterfüllungsbeispiele sowie eine mögliche Strategie, klar vom Bedürfnis unterschieden. Der interaktive **Bedürfniskompass** unterscheidet persönliche Wichtigkeit, mögliche Erfüllung, mögliche Nichterfüllung und Ungewissheit. Alle persönlichen Kompass-Angaben bleiben flüchtig. Der M3.2-Check-in nutzt nun dieselben stabilen Bedürfnis-IDs und zeigt passende Begriffserklärungen direkt bei der Auswahl; ohne automatische psychologische Zuordnungen oder dauerhafte Speicherung. Näheres: `games/Emotionskompass/notes/M33_BEDUERFNISATLAS.md`.


## M4 – Geschütztes Tagebuch

Ein neuer aktiver **Tagebuch-Reiter** erlaubt Check-ins nur nach ausdrücklicher Zustimmung zu speichern, wieder zu öffnen, vollständig zu bearbeiten und nach Bestätigung einzeln oder vollständig zu löschen. Die App speichert lokal ein **AES-GCM-verschlüsseltes, atomar geschriebenes JSON-Dokument**, mit nicht exportierbarem Android-Keystore-Schlüssel. Offline, ohne Cloud, Konto oder INTERNET-Berechtigung. Die persönlichen Inhalte einschließlich Zeitstempeln werden zusammen verschlüsselt. Bei Schlüssel-/Authentizitätsfehlern wird kein bestehender Bestand überschrieben. `FLAG_SECURE` schützt Tagebuch und Check-in vor normalen Screenshots. **Kein Export, keine Wiederherstellung und keine garantierte forensische Löschung**; Gerätewechsel und App-Datenverlust bedeuten Datenverlust. Vor produktiver Verwendung sind unabhängige Security-Tests und eine reale Geräteabnahme erforderlich. Details: `games/Emotionskompass/notes/M4_ENCRYPTED_JOURNAL.md`.


## M5 – App-Sperre und portable verschlüsselte Sicherungen

Im Tagebuch-Reiter kann optional eine zusätzliche **Android-Geräte-PIN-/Muster-/Passwort-Abfrage** eingeschaltet werden. Beim Hintergrundwechsel räumt die App entschlüsselte Einträge aus dem UI-State. Der lokale Keystore-Schlüssel wird dabei *nicht* an diese Prüfung gebunden. Außerdem kann das ganze Tagebuch nach Eingabe einer separaten mindestens zwölf Zeichen langen Passphrase als AES-256-GCM-verschlüsselte `.ekbackup`-Datei über Androids Dateiauswahl exportiert und nach ausdrücklicher Bestätigung als vollständiger Ersatz wiederhergestellt werden. Keine unverschlüsselten Exporte, keine automatische Cloud-Sicherung oder Passphrase-Speicherung. Bitte Exportpassphrase und Backupdatei sorgfältig schützen; eine unabhängige Sicherheitsprüfung steht aus. Details: `games/Emotionskompass/notes/M5_SECURITY_AND_BACKUPS.md`.


## M5.1 – Qualitäts- und Sicherheitsnachbesserung

M5.1 übernimmt den unveränderten lokalen AES-GCM-Datei- und Keystore-Schlüsselstand von M5. Beim Öffnen lokaler verschlüsselter Dateien und beim importierten Backup begrenzt nun ein gemeinsamer Streaming-Reader die Größe vor der vollständigen Allokation. Temporäre entschlüsselte Byte-Puffer werden genullt. Veraltete asynchrone Journal-Ladeergebnisse aus früheren Sperrsitzungen dürfen nicht in den aktuellen UI-Zustand zurückschreiben; im Fehlerfall wird der Journal-Cache verworfen. Wenn ein Android-Dateipicker nicht startet, wird die Ausnahme vom automatischen Wieder-Sperren zuverlässig aufgehoben; abgebrochene Importdialoge löschen die Passphrase aus dem UI-State. Die Unlock-Seite unterstützt Scrollen bei großer Schrift. Neue JVM-Tests überprüfen Streaming-Grenzen und ungewöhnliche Dateianbieter. **Kein unabhängiger Sicherheits-Audit, keine echten Smartphone-/TalkBack-Gerätetests durch CI.** Details: `games/Emotionskompass/notes/M51_QUALITY_SECURITY.md`.


## M6 – Interaktives Lernen

Über die Startseite steht jetzt **Lernen & Entdecken** mit sechs Bereichen zur Verfügung: Gefühl und Bedürfnis des Tages, gemeinsame Gefühls-/Bedürfnisfavoriten mit Suche, kleine redaktionell erklärte Unterscheidungsübungen, freiwillige Wiederholungen, **14+8** interaktive Lernpfade und eine lokale Lernübersicht mit bestätigtem Zurücksetzen. Die fünf Hauptreiter, die Gefühls- und Bedürfnisatlanten, Check-in, Tagebuch, Keystore-Schlüssel und passphrasegeschützte Backupformate bleiben erhalten. Der Lernstand liegt separat AES-256-GCM-verschlüsselt auf dem Gerät, wird nicht mit dem Tagebuchexport übertragen und bei aktiver optionaler App-Sperre aus dem UI-Cache entfernt. Alle Inhalte funktionieren offline; es gibt keine Streaks oder psychologischen Noten. Tests und manuelle Abnahme: `games/Emotionskompass/notes/M6_INTERAKTIVES_LERNEN.md`.


## M6.2 – Wissenswelt

Die neue **Wissenswelt** in „Lernen & Entdecken“ enthält **20 lokal verfügbare Artikel** in vier Themenfeldern (Gefühle, Bedürfnisse, Strategien, Selbstwahrnehmung). Sie sind unabhängig voneinander suchbar und lesbar. Vier **geführte Lernreihen à fünf Lektionen** bieten eine freiwillige Reihenfolge, ohne Artikel zu sperren. Tägliche Leseempfehlung, Themenfilter, Merkliste, Quellen und ausdrücklich markierbarer Lesefortschritt sind integriert. Alle Inhalte liegen unter `app/src/main/assets/lessons/knowledge.de.v1.json`. Artikel-Lesezeichen und Lesemarkierungen erweitern rückwärtskompatibel die bereits verschlüsselten M6-Lernmetadaten; das persönliche Tagebuch bleibt unverändert. Redaktionelle Lernentwürfe, noch keine unabhängige fachliche Prüfung. Details: `games/Emotionskompass/notes/M62_WISSENSWELT.md`.


### M6.2.1: Verbesserte Wissenswelt (PR #15)

Der Wissenswelt-Einstieg ist jetzt direkt auf der App-Startseite verlinkt. In der Wissenswelt: Tagesartikel, „Hier weiterlesen“, explizite Bibliotheks- und Lernreihenaktionen. Alle 20 Lektionen sind um Lernziel, häufiges Missverständnis und freiwillige Verständnisfrage inklusive erklärtem Feedback ergänzt. Das nächste Kapitel startet am Anfang, die Reihen haben eine nicht verpflichtende Fortsetzen-Funktion. Suche versteht ASCII-/Umlautvarianten. Quellen erscheinen mit Titeln und Einordnung statt nackter Nummerierung. Ein fehlerhafter Wissensinhalt blockiert die übrigen App-Bereiche nicht. Zusätzlich wurden sieben JVM-Regressionstests ergänzt; Version 0.6.2.1-m6.2.1, versionCode 14. Weitere Details: `games/Emotionskompass/notes/M62_WISSENSWELT.md`.
