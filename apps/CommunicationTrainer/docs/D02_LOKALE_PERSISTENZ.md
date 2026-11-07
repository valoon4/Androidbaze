# D02 – Lokale Persistenz und Entwurfswiederherstellung

**Status:** Implementierung im D02-PR, keine Freigabe oder Migration nach `main` ohne ausdrückliche Nutzerzustimmung.

## Warum D02 vor D03?

Die erste Version bietet noch keine Nachrichtenübertragung und keine KI. D02 schafft die überprüfbare lokale Datengrundlage, damit längere Eingaben und Chat-Metadaten künftig nicht beim normalen Navigieren verloren gehen.

## Schichten

- `data/ChatEntities.kt`: Room-Tabellen **conversations**, **messages**, **drafts**, **replay_branches** mit dauerhaften IDs, referenzierten Branches, Persona-/Szenarioversionen und Entwurfsrevisionen.
- `data/ChatDao.kt`: atomare Anlage von Gespräch und Root-Branch, revisionssichere Entwurfsupdates, Room Flow für Gesprächsliste, kaskadierendes lokales Löschen.
- `data/ChatDatabase.kt`: Datenbank **Version 1**, Schema-Export und **kein** `fallbackToDestructiveMigration`. Es gab vorher **keine CommunicationTrainer-Datenbank**, daher gibt es zum D02-Erstaufbau keine historischen Nutzerdaten zu migrieren. Das nach einem erfolgreichen Build exportierte **v1-Schema ist unter `android/app/schemas/com.valoon4.communicationtrainer.data.ChatDatabase/1.json` versioniert**; der CommunicationTrainer-CI-Lauf prüft per `git diff`, dass die erneute Generierung es nicht verändert. Ab dem nächsten Schema-Versionswechsel sind ein expliziter Migration-Codepfad und Migrationstests obligatorisch.
- `data/AppPreferences.kt`: DataStore für gewählten Darstellungsmodus und zuletzt geöffnetes Gespräch (ID, **niemals Chattext**).
- `data/ChatViewModel.kt`: vorläufige UI-Zustandsverwaltung mit SQLite-Acknowledgement, gespeicherter Revisionsnummer und Fehlersignalisierung.

## Datenintegrität und Privatsphäre

- Keine Internetberechtigung / keine KI-Anbindung, keine Geheimnisse und keine Gesprächslogs. Android-Backup bleibt **deaktiviert**.
- Die Daten liegen in der App-Sandbox als Room-SQLite-Datenbank und Preferences-DataStore; **keine zusätzliche anwendungsseitige Datenbankverschlüsselung** behaupten.
- Jede Replay-Branch ist genau einer Konversation zugeordnet. Composite Foreign Key verhindert Verknüpfungen von Nachrichten/Entwürfen mit fremden Gesprächsbranches.
- `DELETE conversations` entfernt mit FK-CASCADE alle Nachrichten, Zweige und Entwürfe dieses Gesprächs aus der lokalen aktiven Datenbank. Diese D02-Funktion garantiert **keine** forensische Speicherbereinigung, Löschung aus etwaigen alten Geräte-Backups oder Drittanbieter-Löschungen; letztere werden in D02 gar nicht verwendet.
- Alle Entwurfsschreibvorgänge beginnen **ohne Debounce**. Das UI meldet erst nach erfolgreichem Commit **„Auf diesem Gerät gespeichert“**. Ein harter Stromausfall/Prozessabbruch, während **„Wird gespeichert …“** angezeigt wird, kann noch nicht bestätigte Änderungen verlieren; das wird nicht als gesichert verkauft.
- Entwürfe sind keine gesendeten Nachrichten. D03 wird die Übergabe Draft→Message mit Transaktion/Idempotenz absichern.

## Prüfmatrix

| Fall | Automatisierter Nachweis | Zusätzlich manuell |
|---|---|---|
| Persistenz bei Neustart | Room DB schließen, erneut öffnen, Entwurf prüfen | App im Task-Switcher entfernen, neu öffnen |
| Langsame/alte Schreibvorgänge | ältere `editRevision` nach neuer Revision speichern, Text bleibt neu | schnell tippen und mehrfach Tabs wechseln |
| Scope-Trennung | zwei Gespräche speichern, Entwürfe separat | zwei Entwürfe mit eindeutig unterschiedlichen Texten |
| Gespräch löschen | Room Foreign-Key-Löschkaskade inkl. Message/Branch | Dialog bestätigen, Gespräch darf nicht mehr erscheinen |
| Aufbewahrung Darstellung | DataStore neu öffnen, System/Hell/Dunkel bleibt erhalten | Gerät drehen und App neu starten |
| Datenbank-Migration | initiales Room-v1-Schema; noch keine v0-Daten zu migrieren | **Pflicht-Test**, sobald v2 angelegt wird |
| Android Prozess-Tod | persistierte Daten nach DB-Reopen; kein echter OS-Prozesstod in Unit-Tests | `adb shell am force-stop com.valoon4.communicationtrainer` und Relaunch |
| Großer Text / Dark Mode | bestehende Farbkontrasttests | lange Entwürfe, große Schrift, TalkBack, Bildschirmdrehung |

**D02-Exit-Gate:** `gradle :app:testDebugUnitTest :app:assembleDebug` erfolgreich, Prüfschritte für APK und Speicher-Tests nachvollziehbar, keine Änderungen an anderen Android-Apps oder ihren CI-Workflows. Ohne echte Gerätetests nicht behaupten, dass sämtliche Lifecycle-/Accessibility-Fälle bereits geprüft wären.

## Grenzen für D03

- Kein Senden/Empfangen/KI-Simulator in D02; das schützt gegen unehrliche Behauptungen zur Chatfunktion.
- Datenbank-Schema auf zukünftige `Message`-, `ReplayBranch`- und unveränderliche `personaRef`-Bezüge vorbereitet; D03 muss im realen Chat alle Transaktionen und Sendestatus nachrüsten.
- Vollständige Datenexport-/Lösch-/Backup-/Verschlüsselungsoptionen sind getrennte Freigabefragen vor einer Beta.
