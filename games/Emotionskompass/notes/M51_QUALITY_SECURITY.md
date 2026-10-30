# M5.1 – Qualitäts- und Sicherheitsnachbesserung

**Version:** 0.5.1-m5.1 (`versionCode 11`)  
**Branch:** `feature/emotionskompass-m51-security-hardening`  
**Status:** Debug-Testfassung, **keine Sicherheitszertifizierung**.

## Ergebnis des gezielten Code-Reviews

M5 schützt das lokale Journal bereits mit Android Keystore und AES-GCM und besitzt explizite portable AES-GCM-Sicherungen. M5.1 verändert **keine** Schlüssel, Dateiformate, Datenstruktur oder Journal-IDs; ein Update von M5 auf M5.1 soll vorhandene Daten behalten.

### Tatsächlich behobene Risiken

1. **Unbegrenzter lokaler Datei-Read:** `EncryptedJournalRepository.list()` verwendete bislang `InputStream.readBytes()`, bevor die kryptografische Größenprüfung stattfand. Ein außergewöhnlich großer oder manipulierter verschlüsselter Bestand konnte dadurch unnötigen Speicherbedarf verursachen. Jetzt liest `BoundedJournalInput` maximal `MAX_PLAINTEXT_BYTES + 128` Byte und bricht sonst vor vollständiger Allokation ab.
2. **Gleiche Eingabegrenze für importierte Backups:** `AppViewModel.importJournal()` verwendet denselben getesteten, größenbeschränkten Reader. Er toleriert auch ungewöhnliche Dokumentenanbieter, die `available()=0` oder einmalig `read(...)=0` zurückgeben.
3. **Geringere Klartext-Lebensdauer:** Bei der lokalen Journal-Entschlüsselung werden temporäre Klartext-ByteArrays nach dem Decodieren in `finally` genullt. Das beseitigt nicht alle unvermeidbaren Kopien in JVM-Strings oder UI-State und ersetzt keine Vollspeicherlöschung.
4. **Veraltete Resultate nach erneutem Sperren:** Eine fortlaufende asynchrone Leseoperation darf nach Lock/Unlock nicht mehr den Journalzustand einer neuen Sitzung überschreiben. Eine Zugriffs-Epoch wird beim Sperren/Entsperren hochgezählt; Lade- und Schreiboperations-Rückmeldungen aus alten Sitzungen werden verworfen. Von der Person ausdrücklich gestartete Schreiboperationen können noch im Hintergrund fertig werden; auf dem nächsten Einstieg werden Einträge neu geladen.
5. **Fehler beim erneuten Lesen:** Scheitert das Authentifizieren/Lesen, wird der alte entschlüsselte UI-Cache nun verworfen, statt Daten aus einer alten erfolgreichen Lesung stehenzulassen. Die vorhandene verschlüsselte Originaldatei wird unverändert belassen.
6. **Dateiauswahl lässt sich nicht starten:** Falls ein Android-Dokumentenanbieter fehlt oder `launch()` fehlschlägt, wird der Picker-Ausnahmezustand für das Hintergrund-Re-Lock **sofort** zurückgesetzt. Damit bleibt die App nicht aus Versehen dauerhaft im Ausnahmemodus. Abgebrochene Imports verwerfen die eingegebene Passphrase.
7. **Große Android-Schriftgröße:** Der gesperrte Startbildschirm lässt sich nun scrollen, damit die Entsperraktion bei Großschrift oder im Querformat erreichbar bleibt.

### Sicherheitsgrenzen bleiben ausdrücklich bestehen

- Kein unabhängiger Pentest, kein echtes Hardware-/Keystore-Abnahmelabor, kein verifizierter Store-Release.
- Die App-Sperre ist **nur** ein zusätzlicher UI-Schutz; sie bindet den `AndroidKeyStore`-Schlüssel nicht an frische Authentifizierung.
- Beim geöffneten Android-Dateipicker existiert weiterhin eine *bewusst begrenzte* Ausnahme vom normalen Background-Lock, damit dessen Rückgabewert verarbeitet werden kann. Ein robuster, über Konfigurationswechsel/Prozessverlust authentifizierter Pending-Transfer-Flow bleibt eine offene Härtungsaufgabe. Die Ausnahme wird nach Abbruch/Rückgabe oder fehlgeschlagenem `launch()` zurückgesetzt.
- `String`-Kopien von Journaltext und Sicherungspassphrasen im JVM-/Compose-Speicher können nicht garantiert vollständig überschrieben werden.
- `AtomicFile` vermindert Schreibabbrüche; ohne unabhängige Hardwaretests besteht keine absolute Crash-/Flash-Wipe-Garantie.
- Keine Garantie für unabsichtliches Gerätebackup durch vom Nutzer selbst ausgewählte Cloud-Dokumentenanbieter; die exportierte Sicherung ist immer passphraseverschlüsselt.

## Automatische Regressionstests

- **7 zusätzliche JVM-Tests** für `BoundedJournalInput`: leere Datei, exakte Maximalgröße, ein Byte zu groß, ungültige Limits, `available()=0`, vorübergehend Null-Reads und Streaming ohne Dateigrößeninformation.
- Weiterhin aktive M2–M5-Tests für Emotions- und Bedürfnisatlanten, Check-ins, Journalformat, AES-GCM-Authentifizierung, Backup- und App-Sperre.
- GitHub Actions: `testDebugUnitTest`, `assembleDebug`, `aapt dump badging`, `apksigner verify` und SHA-256 für die APK.

## Manuelle Smartphone-Abnahme (noch nicht durchgeführt)

1. M5 (0.5.0) auf M5.1 (0.5.1) aktualisieren; zuvor angelegte Test-Tagebucheinträge müssen vollständig erhalten bleiben.
2. Ausgewählte Testeinträge öffnen, bearbeiten, speichern und löschen; Neustart prüfen.
3. Geräte-PIN-/Muster-/Passwort-Sperre aktivieren. Mit großem Android-Font (200 %) und Querformat muss die Unlock-Aktion erreichbar sein.
4. App in den Hintergrund schicken und zurückkehren: erneute Gerätebestätigung. Im App-Wechsler keine Tagebuchinhalte sichtbar.
5. Datei-Picker starten und abbrechen; bei aktivierter App-Sperre anschließend erneut in den Hintergrund gehen – Re-Lock muss wieder greifen.
6. Dateipicker ohne Dokumentanbieter / absichtlich fehlschlagende Dateiauswahl auf Testgerät simulieren: Fehlermeldung, Ausnahmeflag entfernt.
7. Mit zwei verschiedenen passphrasegeschützten `.ekbackup`-Dateien exportieren; danach Backup A testweise wiederherstellen, Datenfelder vergleichen, anschließend erneut das ursprüngliche Backup B laden.
8. Manipulierte und viel zu große Dateien als Import anbieten; alte Einträge dürfen dabei weder gelöscht noch neu initialisiert werden.
9. Android neu starten, App-Sperre, Keystore- und Backup-Verfügbarkeit prüfen.
10. TalkBack-Beschriftung, Dark Mode, kleine/ große Displays und Flugmodus prüfen.
11. Datei-Lesen während App in Hintergrund geht und mit PIN wieder freigeschaltet wird, gezielt auf veraltete UI-Ergebnisse prüfen.
12. Die Sicherheit und Barrierefreiheit durch unabhängige Fachleute überprüfen lassen, bevor die App veröffentlicht wird.

## Freigabehinweis

Die CI-Ergebnisse bestätigen ausschließlich automatisierte Tests und einen kompilierten/signerbar verifizierten Debug-Build. Sie bestätigen **nicht** die Funktion in jedem OEM-Android, einen unabhängigen Security Audit oder die Barrierefreiheit mit TalkBack.
