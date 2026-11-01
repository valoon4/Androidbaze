# M7.4.1 – Verschlüsselte Strategieexperimente und vollständiges EKB3-Backup

Stand: 2026-11-01. **Implementierungs-Draft, vor Merge und manueller Geräteprüfung.**

## Zweck und Abgrenzung

Die neue persönliche Strategieexperiment-Datenkategorie darf erst aus M7.4.2 heraus beschrieben werden, wenn sie bereits sicher in den gemeinsamen Sicherungen enthalten ist. **M7.4.1 fügt daher keine neue Benutzer-Schaltfläche zum Speichern von Strategieexperimenten hinzu.**

### Implementierte Bausteine

- `StrategyExperiment`: stark begrenzter, neutraler, nutzerverfasster Datentyp mit eindeutiger UUID, eindeutig entweder kuratierter ID oder freiem Titel, ausdrücklich gewähltem Status und optionaler Bewertung ohne Wirksamkeitsannahme. Validierungen für Länge, Menge, Referenzen und eindeutige IDs.
- `EKS1`: dritter lokaler Android-Keystore-AES-GCM-Store mit zufälliger IV, festgelegter AAD, eigenem Alias und `AtomicFile`. Kein Netzwerk, keine unverschlüsselten Dateien, keine eigenen privaten Analytics.
- `EKB3`: verschlüsselter, versionierter **vollständiger** Export für Journal, Erkenntnisse und Strategieexperimente mit AEAD und PBKDF2, eigenem Header und Versionsvertrag. Alte EKB1/EKB2-Dateien bleiben lesbar.
- Legacy-Semantik: EKB1 übernimmt Journal und **erhält** vorhandene Erkenntnisse und Experimente. EKB2 übernimmt Journal/Erkenntnisse und **erhält** Experimente. Fehlende Bereiche ≠ explizit leere Listen. Entfernt werden lediglich ungültige optionale Referenzen; selbst geschriebene Texte bleiben erhalten.
- `EKR2`: separater Keystore-verschlüsselter, dauerhaft vorgemerkter Drei-Store-Zielzustand, der vor dem ersten Write angelegt und nach Crash beliebig wiederholt werden kann. EKR1-Altaufträge werden zuerst abgeschlossen. Eine ungültige/fehlende Entschlüsselung blockiert den gesamten Vorgang.
- **Koordination**: Neues Check-in-Speichern geht ebenso über `CombinedBackupRepository.saveJournalEntry()` wie die bisherige Backup-/Erkenntnisverwaltung. Eingehende beschädigte EKS1-Daten dürfen weder exportiert noch überschrieben werden.
- `AppViewModel` lädt auch Experimentdaten erst mit den übrigen persönlichen Bereichen authentifiziert und entfernt den flüchtigen Zustand bei App-Sperre; bei Fehler kein „0 Ideen“-Ersatz.
- Der Export-/Importdialog benennt EKB3 und beschreibt EKB1/EKB2/EKB3-Ersetzungsregeln. **Vor** der Bestätigung authentifiziert er die gewählte Datei und zeigt das konkrete Format sowie die Anzahl enthaltener bzw. nicht enthaltener Datenbereiche. Beim Import wird der SHA-256-Fingerabdruck der verschlüsselten Datei mit der Vorschau verglichen; ausgetauschte Dateien werden abgewiesen. Die Vorschau scrollt auch bei großer Schrift. Ältere App-Versionen können EKB3 nicht lesen; dies steht in der Oberfläche.

## Sicherheits- und UX-Prüfungen

| Gate | Art | Status nach Entwicklung |
| --- | --- | --- |
| Strategie-Modell und EKS1-Format | JVM, strikter Roundtrip, Längen und Duplikate | CI |
| EKB3-Format und echte AEAD-Manipulationserkennung | JVM | CI |
| EKB1/EKB2-Import ohne Löschung des Experimentbestands | JVM und Android-Keystore-Emulator | CI |
| EKB3-Restore in zweitem isolierten Android-Kontext | AndroidKeyStore-Emulator | CI |
| EKR2-Replay bei Abbruch nach jedem der drei Writes | JVM-Failure-Injection | CI |
| EKR1->EKR2-Altintent-Migration | realer Android-Keystore-Test mit künstlich angelegtem verschlüsseltem EKR1-Intent | **CI bestanden; physische Geräteabnahme offen** |
| Authentifiziertes Laden bei beschädigtem EKS1 | AndroidKeyStore-Emulator | CI |
| Gleichzeitiger EKB3-Export und Check-in-Speichern | Android-Integrationstest mit zwei Threads | **CI bestanden**; paralleler Restore-/Schreib-Stresstest weiter offen |
| Tatsächlich zwei **physisch getrennte** Geräte | manueller Test | **offen, nicht durch Kontexte auf einem Emulator ersetzt** |
| Vorab-Format-/Anzahlvorschau und SHA-256-Dateibindung | Android-Integrationsprüfung der authentifizierten Formate / Android-UI-Implementierung | **implementiert; CI-Vorschautest bestanden, manuelle SAF-Abnahme offen** |
| TalkBack, 200%-Schrift, 320 dp, Dark Mode | Emulator plus manuell | **CI + manuelle Prüfung offen** |
| Unabhängige Kryptografie-/Security-Abnahme | externer Review | **offen** |

### Prüffolge (nur fiktive Daten)

1. Auf einem frischen Testgerät altes Tagebuch und Erkenntnisse anlegen; im neuen Build EKB3 exportieren. Die Datei hat EKB3-Header und beinhaltet alle drei Bereiche, zunächst eine leere experimentelle Liste.
2. EKB3 auf einem anderen Testgerät mit der korrekten Passphrase importieren; Journal und Erkenntnisse müssen unverändert sein, Strategiesammlung bleibt leer und **als sicher geladen** markiert.
3. Mit automatisiertem Testinstrument fiktive Experimente hinzufügen, EKB3 exportieren und auf einem anderen isolierten Kontext wiederherstellen. In M7.4.1 **noch keine direkte UI-Eingabe** für solche Experimente.
4. EKB1/EKB2 mit bestehendem fiktivem Experimentbestand importieren; nur die enthaltenen Bereiche ersetzen und vorhandene persönliche Experimenttexte erhalten. Ungültige IDs nur als Verknüpfung entfernen.
5. Falsche Passphrase und absichtlich beschädigte Datei testen. Kein Bereich darf verändert werden.
6. Abbruch zwischen Journal-/Erkenntnis-/Experiment-Schreibvorgängen simulieren; beim Neustart ausschließlich den vollständig bestätigten Zustand zulassen.
7. App-Sperre, Wechsel in Hintergrund und fehlende Keystore-Schlüssel gegen alle drei Datenbereiche prüfen.
8. Nachfolge-PR M7.4.2 darf Speicherung von Ideen erst freischalten, wenn offene kritische Sicherheitsgates geschlossen sind.

### Vorhandenes fiktives Testpaket

Ein separates Testpaket `Emotionskompass-M741-EKB3-Testpaket.zip` enthält eine validierte EKB3-Sicherung (45 fiktive Tagebucheinträge, 8 Erkenntnisse, 3 Strategieideen) sowie die ältere EKB2-Sicherung. Passphrase: `Emotionskompass-Testpaket-2026!`. M7.4.1 hat **noch keine sichtbare Strategieideen-Verwaltung**, daher können Strategieideen nur über technische Tests / den Sicherungscontainer, noch nicht durch Nutzereingabe kontrolliert werden.

### Verifizierte technische Gates

Für Implementierungsstand `fe9227e971fd1e38a3173c7ebd520b6abee6f67a` wurde bereits ein signierter Debug-Build erstellt; die Kotlin-Tests und Android-Testkompilierung waren grün. Auf Android API 35 bestanden 14 UI-Tests bei 100 % sowie dieselben 14 bei 200 % Schriftgröße, 3 EKB2-/Keystore-Tests und 8 neue EKB3-/Keystore-Tests (einschließlich EKR1-Migration, Vorschau und parallelisiertem Export). Der nachfolgende UX-Scroll-Fix muss separat auf seinem abschließenden Commit erneut CI-geprüft werden. Physische Geräte- und unabhängige Sicherheitsabnahme sind weiterhin offen.

**Wichtige Grenze:** EKR2 schützt bereichsübergreifende Transaktionen, ist aber keine unabhängige forensische Sicherheitsprüfung. Der AndroidKeyStore-Schlüssel ist nicht biometrisch an jeden Zugriff gebunden; optionale PIN-Abfrage ist wie zuvor primär ein UI-Gate. Hardware-/Recovery-Zusagen müssen bei der Release-Abnahme separat geprüft werden.

## M7.4.1 Exit-Entscheidung

Eine grüne CI zeigt die implementierten automatisierten Pfade. Bei offenen sicherheitsrelevanten Punkten darf M7.4.2 weiterhin keine persönlichen Strategieexperimente dauerhaft speichern. Dieser PR bleibt Draft bis zur **ausdrücklichen Freigabe**.
