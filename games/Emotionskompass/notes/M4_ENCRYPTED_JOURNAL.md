# M4 – verschlüsseltes Reflexionstagebuch

**Stand:** 29.10.2026 · **Android-Version:** 0.4.0-m4 (versionCode 9) · **Status:** Entwicklungs-/Testfassung.

## Kernfunktionen

- Vollständiger **M3.2/M3.3-Check-in** bleibt freiwillig, kurz oder vertieft, mit mehreren Gefühlen, Intensität (0 ≠ nicht angegeben), optionalen Bedürfnissen, körperlichen Signalen, Kontext, Erkenntnis und möglichen eigenen nächsten Schritten.
- Auf der Schlussseite kann der Nutzer die aktuelle Reflexion nun **ausdrücklich verschlüsselt speichern**. Ohne diese Aktion wird nichts ins Tagebuch geschrieben. Leere Check-ins werden nicht gespeichert.
- Neuer aktivierter **Tagebuch-Reiter**: chronologische Liste, Einzelansicht aller eigenen Inhalte, Wiedereinstieg zum Bearbeiten (gesamter geführter Check-in), erneutes sicheres Speichern ohne Änderung der Eintrags-ID und ursprünglichen Erstellungszeit.
- **Einzeleintrag löschen** nur nach Bestätigungsdialog; **alle Einträge löschen** mit separatem Warn-/Bestätigungsdialog. Keine App-internen Papierkörbe oder Streaks.
- Der bislang flüchtige **Bedürfniskompass** bleibt weiterhin ohne Speichern. Gefühls- und Bedürfnisatlanten bleiben komplett offline.

## Datenformat und Datenschutzgrenzen

- Keystore: non-exportable AndroidKeyStore-AES-256-Schlüssel, Alias `emotionskompass_private_journal_v1`, GCM-Mode, keine externe Berechtigung.
- Vor jeder Dateischreiboperation AES/GCM/NoPadding mit zufälligem, provider-generiertem 96-Bit-Nonce; 128-Bit-Authentifizierungstag und festes AAD für Anwendung/Dateiversion.
- Die **komplette** JSON-Datenstruktur einschließlich IDs, Zeitstempel, Gefühle, Bedürfnisse und Freitext wird als ein verschlüsselter Blob persistiert, mit Formatmarkierung `EKJ1`. JSON im Klartext wird niemals absichtlich auf Disk oder in SharedPreferences geschrieben.
- Android `AtomicFile` schützt vor partiellen Schreibvorgängen; Änderungen werden erst nach erfolgreicher Entschlüsselung der vorhandenen Datei durchgeführt.
- **Fehlerfall:** Fehlt der Keystore-Schlüssel, ist das Format unverständlich oder schlägt die Authentizitätsprüfung fehl, wird ein klarer Fehler angezeigt. Der verschlüsselte Bestand wird weder automatisch überschrieben noch neu initialisiert.
- `allowBackup=false` und zusätzliche `dataExtractionRules` schließen App-Daten aus Cloud-Backup und Android-Datentransfer aus. Keine INTERNET-Berechtigung, keine Benutzerkonten und keine Analytics.
- `FLAG_SECURE` schützt Check-in- und Tagebuchoberflächen vor normalen Android-Screenshots und App-Wechsler-Vorschauen. Kein Screen-Sharing-/Screenshot-Versprechen gegenüber kompromittierten Geräten.
- **Grenzen:** Die Daten sind auf das ursprüngliche Gerät/Keystore gebunden. Geräteverlust, App-Daten-Löschung, Deinstallation, Keystore-Verlust und bestimmte Systemwiederherstellungen bedeuten dauerhaft verlorene Einträge. Es gibt in M4 **noch keinen Export oder Wiederherstellungsschlüssel**. Mehrfache Dateikopien auf Flash-Speichern können bei Löschung nicht forensisch garantiert vernichtet werden. Debug-APKs sind keine sicherheitsgeprüften Release-Builds. Kein vom Nutzer festgelegtes PIN-/Biometrie-Gate; bei entsperrtem Gerät kann die App gelesen werden.
- **Größenlimits:** max. 3000 Einträge und ca. 3 MB entschlüsselte Gesamtdaten, um begrenzte Ressourcen zu schonen. Eine Größenüberschreitung führt zu einem Fehler, nie zum stillen Überschreiben.
- **Bewusste Folgephase:** M5 unabhängige Sicherheitstests, echte Geräteabnahme, überprüfte Ex-/Importstrategie und optionaler zusätzlicher App-Zugangsschutz.

## Automatische Tests

- Kompletter JSON-Roundtrip mehrerer Gefühle/Bedürfnisse, der Original-Intensität 0, der unbelegten Intensität null und aller Textfelder.
- Verschlüsselung/Entschlüsselung, veränderliche Nonces, falsche Schlüssel, manipulierte Header/Ciphertexte.
- Formatversion, doppelte IDs, ungültige Intensitäten und Größenlimits.
- Update mit stabiler ID und Erstellungszeit, Löschen nur ausgewählter Einträge und leeres Journal.
- Bestehende Unit-Tests aus M2/M3/M3.1/M3.2/M3.3 bleiben aktiv.
- GitHub Actions baut die Debug-APK, prüft APK-Paketkennung und Android-Signatur.

## Manuelle Gerätetests vor Merge

1. Vorhandene M3.3-Debug-APK aktualisieren, alle Atlanten und Hauptreiter kontrollieren.
2. Kurz-Check-in (zwei Gefühle, Intensität 0 und kein Wert) speichern, Tagebuch-Eintrag öffnen.
3. Vertieften Check-in (Situation, Interpretation, Bedürfnisse, Erkenntnis, freie Notizen) speichern.
4. Eintrag bearbeiten, Werte verändern, speichern. Ursprüngliches Datum und Eintragsidentität müssen stabil bleiben.
5. Eintrag löschen, Dialog abbrechen, dann bestätigen. Anderer Eintrag muss unverändert vorhanden sein.
6. App vollständig beenden und öffnen: gespeicherte Einträge sind wieder lesbar.
7. Über einen Check-in ohne Speichern schließen; keine zusätzliche Tagebuchzeile.
8. Alle Einträge löschen und nach Neustart auf leeren Bestand prüfen.
9. Rotation/Großschrift/TalkBack, Flugmodus und Bildschirmvorschau/Screenshot-Sperre prüfen.
10. Beschädigte oder nicht entschlüsselbare Journaldaten bei einem Testgerät sicher simulieren: Fehlermeldung, **keine Datenüberschreibung**.
11. Android-Backup/Restore-Ausschluss und Keystore-Verhalten auf echter Hardware separat verifizieren.

**Keine medizinische Funktion:** Emotionskompass ist eine persönliche Lern- und Reflexionshilfe. Keine Diagnostik, Therapie oder garantierte Wirkung. Eine unabhängige Sicherheitsprüfung ist vor öffentlichem Release zwingend.
