# M5 – Sicherheit, Datenschutz und Datenkontrolle

Version **0.5.0-m5** · Android `versionCode 10` · Status **Entwicklungs-/Testversion**.

## Bestehende Sicherheitsarchitektur (M4 bleibt erhalten)
- Journal als ein einziges verschlüsseltes, atomar ersetztes Dokument mit AES-256-GCM unter `AndroidKeyStore`; keine Klartext-Journaldateien.
- `allowBackup=false`, keine Internetberechtigung, keine Accounts oder Analytics, `FLAG_SECURE` für Tagebuch und Check-ins.
- Änderungen oder Löschungen lesen den bisherigen Datenbestand authentifiziert, bevor er ersetzt wird. Bei nicht entschlüsselbarem Bestand wird nicht automatisch neu initialisiert.

## M5.1: Optionale zusätzliche App-Sperre
- Einschalten und Ausschalten nach Bestätigung der vorhandenen **Android-Geräte-PIN, des Musters oder Passworts** mit `KeyguardManager.createConfirmDeviceCredentialIntent`.
- Gerät ohne sichere Sperre: Funktion kann nicht aktiviert werden; Nutzerhinweis zeigt Android-Einstellungen als Voraussetzung.
- Schalter ist das einzige dauerhaft in unverschlüsselten `SharedPreferences` gespeicherte Merkmal. Es werden keine Geräte-PINs, biometrischen Merkmale oder App-Passwörter gespeichert.
- Wenn aktiviert: Beim Start zunächst Zugangskontrolle **vor dem Entschlüsseln** des Journals. Beim Wechsel in den Hintergrund werden private ViewModel-Einträge, Entwurf und Navigation aus dem UI-State entfernt; bei Rückkehr ist erneut die Geräteanmeldung nötig.
- Die **Android-Dateiauswahl** ist ein Sonderfall: Während einer explizit gestarteten SAF-Dateiauswahl wird das automatische Relock kurz ausgesetzt, damit der Export/Import nach Dateiauswahl fortgesetzt werden kann. Nach Rückkehr gelten die üblichen Regeln.
- Wichtige Grenze: Die Gerätesperre ist ein **zusätzlicher UI-Gate**, keine an eine Authentifizierung gebundene `AndroidKeyStore`-Zugriffsbeschränkung. Der lokale AES-Schlüssel selbst wurde **nicht** auf `setUserAuthenticationRequired(true)` umgestellt, um bestehende M4-Journale nicht durch Schlüsselverlust/Migration zu gefährden. Auf entsperrten/kompromittierten Geräten ist das kein Schutz vor Root-/Instrumentation-Angriffen.
- M5 bietet Geräte-PIN/Muster/Passwort, **keine garantierte biometrische Entsperrung** und keinen selbst gesetzten separaten App-PIN.

## M5.2: Portable Passphrase-Sicherung
- Anders als die lokale Keystore-Datei ist das exportierte Backup auf einem anderen Android-Gerät wiederherstellbar.
- Nur auf expliziten Nutzerwunsch über das **Storage Access Framework** (`CreateDocument`) wird eine Datei erstellt. Die App sendet selbst keine Daten ins Internet; der Nutzer kann als Ziel allerdings auch einen externen Dokumentenanbieter wählen. Dies ist deshalb **kein garantierter ausschließlich lokaler Ablageort**.
- **Kein Klartext-Export:** vor der Übergabe an den Dokumentenanbieter wird die gesamte serialisierte Journalstruktur im Arbeitsspeicher mit AES-GCM verschlüsselt.
- Dateiformat `EKB1 || 16 Byte Salt || 12 Byte Nonce || AES-GCM-Ciphertext+16 Byte Tag`.
- Schlüsselableitung `PBKDF2WithHmacSHA256`, 250.000 Iterationen, 256 Bit, frisches zufälliges Salt, frische Nonce, authentifiziertes Versions-AAD.
- Passphrase für neue Sicherungen mindestens 12 Zeichen. Sie wird nicht gespeichert. **Passphraseverlust bedeutet unwiederbringlichen Verlust des exportierten Backups**, sofern das lokale Tagebuch nicht mehr verfügbar ist.
- Die Passphrase wird während des Export-/Importdialogs kurzzeitig als Kotlin `String` im UI und als `CharArray` verwendet; String-Speicher lässt sich nicht verlässlich aus dem JVM-Heap löschen. Das bleibt eine bekannte Grenze.
- Bestehende App-Sperre ersetzt **nicht** die Sicherungspassphrase.

## M5.3: Wiederherstellen mit ausdrücklicher Bestätigung
- Nutzer wählt `EKB1`-Datei mit `OpenDocument` und gibt die Sicherungspassphrase ein.
- Bestätigungsdialog warnt, dass die **gesamte lokale Sammlung ersetzt** wird; kein automatisches Importieren, Mischen oder stilles Überschreiben.
- Zuerst authentifizierte Entschlüsselung und Parsen/Validieren; ungültige Version, falsche Passphrase, manipulierte Datei oder zu große Datei werden abgewiesen.
- Dann muss die bestehende Journaldatei ordnungsgemäß entschlüsselbar sein. Erst danach wird der importierte Bestand unter dem *lokalen* AndroidKeyStore-Schlüssel atomar neu verschlüsselt.
- Import ist **kein Zusammenführen**; zuerst aktuelles Tagebuch separat exportieren. Wenn der bisherige Keystore-Schlüssel fehlt oder die lokale Datei korrupt ist, wird die Wiederherstellung absichtlich **nicht** zum stillen Überschreiben verwendet. Eine manuelle Reset-/Recovery-Prozedur ist nicht enthalten.

## Teststand
- `JournalBackupTest` überprüft Roundtrip inkl. privater Felder, keine sichtbaren Klartext-Fragmente, zufällige Salts/Nonces, falsche Passphrase, Header-/Ciphertext-Tampering, Mindestlänge, Größenlimit, ungültiges Format und leere Sicherung.
- Bestehende Tests zu Keystore-Journalformat, Check-in- und Bedürfniskatalog laufen weiter.
- GitHub Actions prüft Kotlin/JVM, Android-APK und Signatur. Tests allein ersetzen **keinen Sicherheits-Audit**.

## Manuelle Android-Abnahme vor Merge
1. Upgrade von 0.4.0 auf 0.5.0 mit zwei Testeinträgen, App-Neustart, Bearbeitung/Löschung und Datenerhalt.
2. App-Sperre nur bei eingerichtetem Geräteschutz aktivieren, App in den Hintergrund und zurück bringen, Entsperrung verifizieren.
3. Abbrechen bzw. falsche Geräteanmeldung darf keinen Zugang freischalten. Ein-/Ausschalten benötigt erneute Gerätebestätigung.
4. Beim Entsperren muss das Tagebuch wieder korrekt aus der lokalen verschlüsselten Datei geladen werden.
5. Testtagebuch exportieren, Passphrase eingeben und bestätigen, Datei schließen und erneut öffnen: keine Klartextdaten im Backup.
6. Auf zweitem Testgerät nach frischer Installation exportiertes Backup einlesen: exakte emotionale Intensitäten, Bedürfnisse, Erstellungszeit und Notizen wiederherstellen.
7. Falsche Passphrase, manipuliertes Backup und Übergröße: Fehlermeldung, Originaljournal bleibt intakt.
8. Import mit vorhandenem Tagebuch: Bestätigungsdialog abbrechen; erst nach ausdrücklicher Zustimmung und authentifizierter Sicherung ersetzen.
9. SAF-Dateiauswahl mit aktivierter App-Sperre, Rotation, TalkBack, Großschrift, Dark Mode, Flugmodus und App-Wechsler prüfen.
10. Deinstallation und Keystore-Ausfall: keine automatische Wiederherstellung versprechen, nur exportiertes passphrasegeschütztes Backup kann geräteübergreifend helfen.

## Offene Arbeiten / ehrliche Grenzen
- Unabhängiges Security Review, echte Gerätetests und Bedrohungsmodell stehen aus.
- Kein dedizierter BiometricPrompt, keine schlüsselgebundene biometrische Authentifizierung und kein Schutz gegen Root/kompromittierte Geräte.
- Keine forensisch beweisbare Löschung auf Flash-Speichern, keine automatische Cloud- oder Gerätebackup-Funktion.
- Keine automatisierte Sicherung: Nutzer muss selbst das Backup aktualisieren und die geheime Passphrase sicher aufbewahren.
