# M7.3 – Eigene Erkenntnisse und wiederherstellbare Sicherung

**Stand:** 31.10.2026 · **Umsetzung:** Draft-PR #27, nicht gemergt · **Version:** 0.7.3-m7.3-insights

## Nutzerwert / Umfang
Im bestehenden Reiter **Rückblick** zwischen **Überblick** und **Meine Erkenntnisse** wechseln (kein sechster Hauptreiter). Die Person schreibt Erkenntnisse **in eigenen Worten**, kann sie bearbeiten, durchsuchen, archivieren, zurückholen und nach Bestätigung einzeln löschen. Ein Bezug zu einem bestehenden Tagebucheintrag ist freiwillig und entfernbar. Der Tagebuchbeleg bleibt separat und wird nicht durch das Löschen der Erkenntnis gelöscht. Beim Löschen/Ersetzen eines Tagebucheintrags bleibt der selbst verfasste Text erhalten, ein nicht mehr gültiger Verweis wird jedoch entfernt.

Vier offline-redaktionelle Reflexionsfragen sind freiwillig und überspringbar. Nur explizite Betätigung von **Ausdrücklich speichern** persistiert persönliche Inhalte. Keine automatische Antwort, Musterdiagnose, KI-Interpretation, Statistik, Streak oder Bewertung.

## Datenschutz und lokaler Speicher
- Versioniertes `PersonalInsightFormat` v1 mit stabilen IDs, UTC-Erstellungs-/Änderungszeit, Text, Archivzustand und optionaler Journal-ID.
- Maximal 1.000 Erkenntnisse, 1.200 Zeichen je Text und 1,5 MB Formatgröße. Strikte Validierung vor dem Schreiben.
- Separate, mit Android-Keystore-AES-256-GCM geschützte AtomicFile-Datei `private_personal_insights_v1.enc` (EKI1), eigener Schlüsselalias. Bestehendes EKJ1-Tagebuch wird **nicht** migriert, ersetzt oder im Dateiformat verändert.
- Keine Speicherung persönlicher Eingaben in `SavedStateHandle`, `rememberSaveable`, DataStore, Logs, Screenshots, Analytics oder Auto-Backup. Bestehendes `FLAG_SECURE` für Rückblick gilt auch für die Erkenntnisansicht. App-Sperre verwirft den entschlüsselten UI-State.
- Fehler beim Lesen oder Authentifizieren dürfen nicht als leere Sammlung dargestellt werden. Kein automatisches Schlüssel-Reset.

## Backup-Gate: Vor erstmaligem Schreiben integriert
- **EKB2 exportieren:** Eine Datei enthält das gesamte Tagebuch **und** die gesamte Erkenntnissammlung, beides gemeinsam über PBKDF2-HMAC-SHA256 (250.000 Iterationen, 16-Byte-Salt) und AES-256-GCM (12-Byte-Nonce, versioniertes AAD) authentifiziert und verschlüsselt. Nur vom Nutzer gewählte Datei und Passphrase; Passphrase wird nicht persistent gehalten. Format validiert beide Sammlungen beim Import vollständig **vor jedem Schreibzugriff**.
- **EKB2 importieren:** Nach expliziter Ersetzungswarnung werden Tagebuch und Erkenntnisse ersetzt. Ein transaktionales Recovery-Protokoll hält vor dem Austausch bereits Keystore-verschlüsselte Dateikopien vor. Eine Markerdatei wird erst nach beiden Sicherheitskopien geschrieben, nach erfolgreichen beiden Schreibvorgängen entfernt; unterbrochene Importe werden beim nächsten App-Start zurückgerollt.
- **EKB1 importieren:** Alte Sicherungen bleiben importierbar. Sie ersetzen nur das Tagebuch; **bereits verfasste Erkenntnisse bleiben erhalten**. Verweise auf danach nicht vorhandene Tagebucheinträge werden entfernt, Text bleibt unverändert.
- Der Export ist weiterhin freiwillig. Gerätewechsel/Deinstallation ohne passphrasegeschütztes Backup führt zum Verlust der lokalen Keystore-Daten. **Vor Import immer vorhandene Daten separat sichern.**

## Test- und UX-Abnahme
- Reine JVM-Tests: eigene Formate, Nicht-Duplikation, Textlimits, Verschlüsselung, Roundtrip aller Felder, EKB1-Kompatibilität, falsche Passphrase und Tamper Detection.
- Instrumentierte Tests: Editor nur explizit speichern / Abbruch, echte Keystore-Persistenz, Editieren/Archivieren/Löschen sowie Rollback nach simuliertem Fehler. Auf Android-35-Emulator mit 100 % und 200 % Schriftgröße.
- Manuelle Abnahme ausstehend: lange Texte/TalkBack/320dp/Rotation, EKB1-Testimport der 42 fiktiven Einträge, EKB2-Export/Import auf einem zweiten **Testgerät**, importierte Erkenntnisse inklusive Link, beschädigte Quelldateien, App-Hintergrund / Gerätesperre, Offline-Modus, 0/1/1000 Datensätze, Energie-/Crash-Szenarien.
- Unabhängige Sicherheits- und fachredaktionelle Prüfung vor offiziellem Release noch erforderlich.

**Risiko-/Designgrenzen:** Die Fragen sind nicht klinisch validiert; Antworten sind ausschließlich eigene Gedanken. Das EKB2-Format ist für Tagebuch und Erkenntnisse; noch keine Strategieexperimente (M7.4). Ein Rückblick auf fehlerhafte/gesperrte Quellen wird nicht durch scheinbar leere Daten ersetzt. `PR #27` bleibt Draft bis zur gesonderten Freigabe.
