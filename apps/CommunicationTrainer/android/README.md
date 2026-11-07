# CommunicationTrainer – D02 lokale Speicherung mit Aurora Pulse

**Aktueller Entwicklungsstand:** D02 stellt Room-gespeicherte Gesprächsentwürfe, DataStore-Darstellungseinstellungen und lokale Löschfunktionen bereit. Die App verschickt weiterhin keine Nachrichten und verwendet keine KI. PR #59 benötigt vor dem Merge nach main eine eigene Freigabe.

## D02 ausprobieren

1. Unter **Chats** auf **„Neuen Entwurf anlegen“** tippen.
2. Einen längeren Text schreiben und warten, bis **„Auf diesem Gerät gespeichert“** sichtbar ist.
3. Zur Übersicht zurückkehren, Entwurf erneut öffnen; den gespeicherten Text prüfen.
4. App im Task-Switcher schließen oder per `adb shell am force-stop com.valoon4.communicationtrainer` beenden und erneut starten; der zuletzt geöffnete Entwurf soll wieder erscheinen (bei persistierter Auswahl).
5. Zwei Entwürfe erstellen und prüfen, ob beide unabhängig voneinander bestehen bleiben.
6. Einen Entwurf über **„Löschen“** mit Bestätigung entfernen; nur dieser darf verschwinden.
7. Unter **Üben** die Darstellung System/Hell/Dunkel wechseln, App neu starten und Einstellung prüfen.
8. Test: Drehen, lange Texte, größere Schrift, Offline-Modus. Keine Netzwerkberechtigung erforderlich.

**Wichtig:** Nur ein mit SQLite bestätigter Entwurf („Auf diesem Gerät gespeichert“) kann bei einem sofortigen Prozessabbruch als dauerhaft zugesichert werden. Die Anzeige „Wird gespeichert …“ ist **keine** Speicherbestätigung.

[D02-Datenschutz-/Testmatrix](../docs/D02_LOKALE_PERSISTENZ.md). Die bisherige D01-Dokumentation bleibt unten als Historie erhalten.

## Historie: D01

**Status:** installierbares Kotlin-/Jetpack-Compose-Grundgerüst als vorgeschlagener PR, noch **keine echte KI**, keine Chattexte, keine lokale Chatpersistenz oder Authentifizierung.

## Ausgewähltes Design: Aurora Pulse

Der Nutzer hat **Aurora Pulse** als endgültige Farb- und Stilrichtung ausgewählt. Das Design nutzt Indigo für Navigation/Hauptaktionen, Pink für menschliche Akzente, Mint für unterstützende Hinweise und Gold künftig sparsam für Lernaktivität. Beide System-Farbmodi sind in `ui/theme/AuroraPulseTheme.kt` zentral definiert. Kleine Texte auf gefüllten Schaltflächen verwenden **kontrastkorrigierte** Indigo-/Pink-Töne; D01 enthält ergänzende Farb-/Kontrast-Unit-Tests.

[Gestaltungsentscheidung, Tokenwerte und Barrierefreiheit](../docs/D01_AURORA_PULSE_DESIGN.md).

## Abgrenzung

Eigenständige Gradle-Root: `apps/CommunicationTrainer/android/`. Dieses Projekt ist **nicht** in den Root-Build von Androidbaze (Wormblade) eingebunden und verändert weder andere Apps noch deren Workflows. Die externe GitHub-Action `.github/workflows/communicationtrainer-debug-apk.yml` wird ausschließlich für dieses Projekt angelegt.

Bereits funktional vorgesehen:
- Zwei navigierbare Hauptbereiche **Chats** und **Üben**, inkl. Zustandswiederherstellung der Tab-Auswahl bei Android-Konfigurationswechsel bzw. gespeichertem Zustand.
- Klare Kennzeichnung **„Technische Demo ohne KI“**.
- Hell-/Dunkeldesign, Material3, lesbare Texte, Android-Edge-to-Edge.
- Kein INTERNET-Permission, keine API-Schlüssel, keine Datenübertragung, keine Datenspeicherung.
- Zwei einfache JVM-Unit-Tests zur Navigation und Bezeichnung.

**Nicht** enthalten: sendbare Chats, Testcharaktere, echte Szenarien, KI-Provider, Coach, Replay, Entwurfspersistenz und Produktrelease. D02/D03 folgen separat.

## Reproduzierbarer Build

JDK **17**, Android SDK Plattform **36**, Build Tools **36.0.0**, Gradle **8.13**, AGP **8.13.2**, Kotlin/Compose-Compiler **2.4.21**.

Arbeitsverzeichnis: `apps/CommunicationTrainer/android/`.

Mit installiertem Gradle 8.13:

```bash
gradle :app:testDebugUnitTest --no-daemon
gradle :app:assembleDebug --no-daemon
```

Alternativ auf Linux/macOS mit geprüftem Gradle-Download (offizieller SHA-256 von https://gradle.org/release-checksums/):

```bash
bash ci/build-debug.sh :app:testDebugUnitTest :app:assembleDebug --no-daemon
```

**Wichtig:** `ci/build-debug.sh` ist ein transparenter, per SHA-256 verifizierender Bootstrapper und **kein offizieller Gradle-Wrapper**. Ein offizieller Gradle-Wrapper-JAR ist in D01 noch nicht versioniert; in GitHub Actions wird stattdessen das offizielle `gradle/actions/setup-gradle@v4` mit exakt Version 8.13 verwendet. Ein echter Wrapper kann in einem späteren eigenen PR durch `gradle wrapper` generiert und geprüft werden.

APK-Pfad nach erfolgreichem Build: `app/build/outputs/apk/debug/app-debug.apk`.

GitHub Actions: Workflow **CommunicationTrainer Debug APK** für PRs/Änderungen an `apps/CommunicationTrainer/android/**`, manuell über `workflow_dispatch`, sowie Pushes auf `main` nur bei relevanten Dateien. Der Workflow führt den Unit-Test aus, erzeugt die Debug-APK, prüft Paketkennung, SDK-Ziel, Signatur und SHA-256 und lädt ein Build-Artefakt hoch. Ein Debug-Artefakt ist kein Play-Release und seine Signatur kann zwischen GitHub-Runs wechseln.

## D01 manuelle Abnahme

1. APK herunterladen und installieren; App-Bezeichnung muss **CommunicationTrainer** lauten.
2. **Chats** und **Üben** mehrfach wechseln: Überschriften und Inhalte passen sich an.
3. Gerät drehen/App kurz verlassen: die Navigation bleibt benutzbar.
4. Große Schrift/System-Dark-Mode: Inhalte lesbar und scrollbar, untere Navigation bedienbar.
5. Keine Chat-Eingabe und keine KI-Behauptungen; korrekt als Demo erkennbar.
6. Von diesem Projekt aus bauen: Root-Wormblade und alle `games/**`-Apps unverändert.
7. Einen fehlgeschlagenen CI-Run nicht als erfolgreiche APK-Freigabe ausgeben.

Für das nächste Feature: [K6-Fahrplan](../docs/K6_UMSETZUNGSPLAN.md), danach D02 für die Persistenz und D03 für die Mock-Unterhaltung.
