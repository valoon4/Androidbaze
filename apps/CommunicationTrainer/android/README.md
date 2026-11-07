# CommunicationTrainer – Android-Grundgerüst D01

**Status:** installierbares Kotlin-/Jetpack-Compose-Grundgerüst als vorgeschlagener PR, noch **keine echte KI**, keine Chattexte, keine lokale Chatpersistenz oder Authentifizierung.

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
