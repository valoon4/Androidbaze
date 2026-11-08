# CommunicationTrainer – D04 InferenceGateway und Modellvergleich

**D04-Entwicklungsstand:** Es gibt jetzt ein einheitliches, testbares Gateway für spätere KI-Betriebsarten. Der aktuell benutzbare Chat bleibt **lokale D03-Simulation mit festen Antworten**; es gibt weiterhin **keinen echten KI-Provider, keine installierten Modellgewichte, keine Internetberechtigung** und **keine echten Modellbenchmarks**.

Die neue Debug-APK installiert sich als **CommunicationTrainer D04 Gateway-Test**, eigenständig neben D03 (`com.valoon4.communicationtrainer.d04preview`). In der UI ist das bewährte D03-Testgespräch zu prüfen: Text speichern, senden, Beispielantworten, Fehler-Retry, Neustart. D04 ändert **keine** realen Chatdaten in D03.

**Hardware-Preflight (D04):** Auf dem bestätigten Galaxy A32 5G (Android 13, 4 GB RAM) sind laut Nutzer etwa 20 GB auf der microSD frei, aber intern nur ungefähr 1,5 GB. Unter **Üben → „SD-Speicher jetzt prüfen“** kann die D04-Preview den verfügbaren Platz auf einer als entnehmbar erkannten App-SD-Directory anzeigen. Es ist **nur ein Lesetest**: kein Download, keine KI, kein Chattransfer und keine Modell-Dateikopie. Der native Modellzugriff bleibt noch gesondert zu testen.

[Architektur, Kandidaten, sichere Gates und offener Messstatus](../docs/D04_INFERENCE_MODELLVERGLEICH.md) · [Messvalidator und Testkorpus](../benchmarks/d04/README.md).

---
# Historie: D03 Mock-Chats mit Aurora Pulse

**Stand: PR #60 zur Freigabe.** In D03 kannst du erstmals Textnachrichten lokal senden und feste, deterministische Testantworten erhalten. **Es ist keine echte KI angeschlossen**, keine Kommunikation wird an einen Server übertragen, und die Antworten stammen nicht von einer echten Person.

## D03-Test-APK: sicher neben D02

Der D03-Debug-Build hat absichtlich die eigene Paketkennung `com.valoon4.communicationtrainer.d03preview` und den sichtbaren App-Namen **CommunicationTrainer D03 Test**. Du musst **D02 nicht deinstallieren**. Beide Testversionen besitzen eigene Daten und **übernehmen keine Chats oder Entwürfe voneinander**.

Grund: GitHub verwendet ohne dauerhaft verwalteten Signing-Key unterschiedliche Debug-Signaturen; normale In-Place-Updates funktionieren dadurch nicht zuverlässig. Das Preview-Suffix verhindert die erneute Installationskollision, **ersetzt aber keine stabile Debug-Signing-Lösung** für künftige Upgrades mit erhaltenen Nutzerdaten. Bitte nur Testtexte verwenden.

## D03 – So testest du die App

1. Unter **Chats → Neues Testgespräch** öffnen und z. B. „Hallo, wie geht es dir?“ eingeben.
2. Warten, bis **„Entwurf gespeichert“** erscheint, dann **Senden** tippen. Die eigene Nachricht sollte genau einmal sichtbar werden.
3. Nach kurzer Zeit muss eine als **„Testpartner · Simulation“** markierte Antwort erscheinen. Kein KI-Modell ist beteiligt.
4. Mehrmals sehr schnell **Senden** tippen. Es darf keine zweite eigene Nachricht für dieselbe Eingabe entstehen.
5. Unter dem Composer die Checkbox **„Nächste Testantwort absichtlich scheitern lassen“** aktivieren. Eine neue Nachricht senden; die Antwort muss den Fehlerstatus anzeigen. **„Testantwort erneut versuchen“** anklicken: genau eine Testantwort muss ergänzt werden, ohne eine zweite eigene Nachricht zu erzeugen.
6. Ein zweites Gespräch anlegen, unterschiedliche Texte senden, zwischen den Gesprächen wechseln.
7. App schließen und neu starten. Gespräche und Entwürfe müssen weiterhin vorhanden sein; wenn ein Testantwort-Auftrag unterbrochen wurde, bleibt dieser als wartend sichtbar und lässt sich explizit wiederholen.
8. Einen Testchat mit Bestätigung löschen. Nur der betroffene Chat darf verschwinden.
9. Mit geöffneter Tastatur, langen Texten, größerer Systemschrift, Rotation sowie System/Hell/Dunkel prüfen.

**Wichtige Grenzen:** Unbestätigte Tastendrücke sind vor dem SQLite-Commit nicht garantiert dauerhaft gespeichert. Deshalb wird der Senden-Button erst nach der Speicherbestätigung aktiviert. Android-Backup bleibt deaktiviert. Keine KI, keine Netz-/Cloud-Anbindung, keine echte Persona-Simulation.

[Technik und Abnahmematrix D03](../docs/D03_MOCK_CHAT.md) · [D02-Datenarchitektur](../docs/D02_LOKALE_PERSISTENZ.md)

## Reproduzierbarer Android-Build

JDK 17, Gradle 8.13, AGP 8.13.2, Kotlin/Compose-Compiler 2.4.21, compileSdk/targetSdk 36, minSdk 26.

Im Verzeichnis `apps/CommunicationTrainer/android/`:

```bash
gradle :app:testDebugUnitTest --no-daemon
gradle :app:assembleDebug --no-daemon
```

Alternativ `bash ci/build-debug.sh :app:testDebugUnitTest :app:assembleDebug --no-daemon`. Der Script-Bootstrapper ist **kein offizieller Gradle-Wrapper**, aber prüft den Gradle-Download per SHA-256. Der eigene Workflow `.github/workflows/communicationtrainer-debug-apk.yml` führt Tests, Room-Schema-Check, APK-Identitäts- und Signaturprüfung sowie Artifact-Upload aus.

## Projektgrenzen

Eigenständiger Gradle-Root unter `apps/CommunicationTrainer/android/`, **nicht** mit dem Root-Wormblade-Build verknüpft. Keine anderen Apps oder deren Workflows wurden geändert. Es erfolgt kein Merge ohne ausdrückliche PR-Freigabe.

---
## Vorherige Meilensteine

- D00: technisches Repo-Audit
- D01: eigenständiges Compose-Grundgerüst mit Aurora Pulse
- D02: Room-Datenbank, lokale Entwürfe, DataStore-Einstellungen, Löschung, wiederherstellbare lokale Daten
