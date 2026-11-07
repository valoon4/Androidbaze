# D00 – Tatsächliche Repository- und Android-Build-Bestandsaufnahme

**Datum:** 07.11.2026 · **Status:** statische Bestandsaufnahme durchgeführt, D01 noch nicht implementiert
**Repository:** [valoon4/Androidbaze](https://github.com/valoon4/Androidbaze), Branch **main**, untersuchter Commit **830280455c51f7ab854c4476e5eec526a4213ad3**
**Projekt:** CommunicationTrainer (Arbeitstitel) · **Auftragsgrenze:** nur **apps/CommunicationTrainer/** ändern
**Methode:** vollständiger rekursiver Git-Tree (kein Truncation-Flag), Versions-/Manifest-/CI-Dateien über GitHub gelesen; Aussagen unten beziehen sich auf diesen Snapshot. **Keine APK und kein Gradle-Build ausgeführt.**

## 1. Zusammenfassung und Ampel

| Prüffeld | Beobachtung | D00-Bewertung |
|---|---|---|
| Existierendes CommunicationTrainer-App-Modul | **Nicht vorhanden**: 33 Dateien im Zielbereich, ausschließlich README, Markdown-Konzepte und JSON-Spezifikationen; keine Kotlin-/Gradle-/Manifestdatei | **D01 nötig** |
| Root-Build | Vorhandenes eigenständiges Android-Projekt **Wormblade**, nur Modul :app | **Nicht anfassen** |
| Weitere Android-Apps | Eigenständige Projekte unter **games/**, darunter InnerWave/Emotionskompass, TimeFlow, ApeEconomics, EugiDoors, TrashJump | **Isolationsprinzip bestätigt** |
| GitHub Actions | **11** vorhandene Workflow-Dateien | **CI-Kollisionen beachten** |
| Root-Debug-Workflow | Start bei **jedem** Push auf main, **ohne** paths-Filter; führt ein Wormblade-Umschreibeskript aus und kann ggf. Änderungen auf main pushen | **Wichtige D01-Risikoflagge** |
| Android-Toolchain | funktionierende **Beispiele der Konfiguration**, aber kein ausgeführter CommunicationTrainer-Build | **Kombination in D01 validieren** |
| Öffentliches Android-V1-Ziel | Neue Google-Play-Apps benötigen nach aktuellem Stand **targetSdk >= 36** | **D01 nicht blind targetSdk 35 übernehmen** |
| Kommunikationstrainer-Workflow | **Nicht vorhanden** | **Neue CI-Datei an anderer Stelle braucht Sonderfreigabe** |
| Nutzerbereitstellung | Aktuell **keine CommunicationTrainer-APK**, keine Download-Artefakte, kein Backend | **D01 bis D05 erforderlich** |

### 1.1 Tatsächlicher Umfang im Git-Snapshot

- Git-Hauptbaum: **547** Einträge, darunter **409** Dateien, vollständig nicht-trunkiert.
- Stammverzeichnis: Gradle-Root, **app/**, **games/**, **PC/**, **apps/** und **.github/workflows/**.
- Einziger unmittelbarer Unterordner von **apps/**: **apps/CommunicationTrainer/**.
- CommunicationTrainer: **33 Dateien** unter docs/, specs/ und README.md; **0 Kotlin-Quelldateien, 0 AndroidManifest.xml, 0 build.gradle.*, 0 settings.gradle.***.
- 11 Workflow-Dateien unter **.github/workflows/**; 10 sind auf bestimmte Pfade bzw. PR-Ereignisse begrenzt, der Workflow **debug-apk.yml** reagiert ohne paths-Filter auf jeden Push auf main.

**Keine Annahme:** Die Existenz eines CI-Workflows oder einer Gradle-Konfiguration beweist nicht, dass heute alle Tests grün oder eine APK installierbar sind. D00 ist ein **Datei-/Konfigurationsaudit**, kein erfolgreich ausgeführter Build.

## 2. Bestehende Buildlandschaft mit geprüften Dateien

### 2.1 Repository-Root: Wormblade, keine Multi-App-Sammelkonfiguration

- [settings.gradle.kts](https://github.com/valoon4/Androidbaze/blob/main/settings.gradle.kts): rootProject.name = **Androidbaze**, einziges eingebundenes Modul **:app**.
- [build.gradle.kts](https://github.com/valoon4/Androidbaze/blob/main/build.gradle.kts): Android Gradle Plugin **8.7.3**, Kotlin-Android-Plugin **2.0.21**.
- [gradle/wrapper/gradle-wrapper.properties](https://github.com/valoon4/Androidbaze/blob/main/gradle/wrapper/gradle-wrapper.properties): Gradle **8.9**.
- [app/build.gradle.kts](https://github.com/valoon4/Androidbaze/blob/main/app/build.gradle.kts): Namespace und applicationId **com.valoon4.wormblade**, Java **17**, compileSdk **35**, minSdk **24**, targetSdk **35**.
- [scripts/bootstrap-and-build.sh](https://github.com/valoon4/Androidbaze/blob/main/scripts/bootstrap-and-build.sh) und [README.md im Root](https://github.com/valoon4/Androidbaze/blob/main/README.md): richten den **Root-Wormblade-Build** ein, nicht einen universellen Multi-App-Builder.

**Schluss:** CommunicationTrainer **nicht** mit `include(":CommunicationTrainer")` oder ähnlichem in Root-settings.gradle.kts aufnehmen. Root-Gradle-Versionen und Root-Scripts gelten nur für Wormblade und sind **keine** passende automatische Projektbasis.

### 2.2 Eigenständige Android-Projekte als technische Referenzen

| Projekt | Gradle-/Kotlin-Befund | Relevanz |
|---|---|---|
| InnerWave / Emotionskompass | Eigene Gradle-Wurzel unter **games/Emotionskompass/src/**; AGP **8.13.2**, Kotlin-/Compose-Plugin **2.4.21**; compileSdk **36**, minSdk **26**, targetSdk **35**, Java **17** | **Nächste praktische Referenz** für Compose und isolierte CI |
| TimeFlow | Eigene Gradle-Wurzel unter **games/TimeFlow/**; AGP **8.7.3**, compileSdk **35**, targetSdk **35**, Java **17** | Nachweis, dass sich app-spezifische Gradle-Projekte getrennt organisieren lassen |
| ApeEconomics | Eigene Gradle-Wurzel unter **games/ApeEconomics/**; compileSdk **36**, Java **17** | Weitere Build-/CI-Isolationsreferenz |

**Vorsicht:** Keinen Code, App-Identifier, MainActivity oder Versionen aus anderen Apps ungeprüft kopieren. Alle bestehenden Referenzkonfigurationen mit targetSdk 35 wären für **eine neue Google-Play-App nach dem 31. August 2026 unzureichend**. Unser neues Projekt plant deshalb mindestens targetSdk 36; vor Veröffentlichung ist die dann aktuelle Store-Regel nochmals zu verifizieren.

### 2.3 Verifizierte Konfigurations-Kompatibilität aus offiziellen Quellen

1. Android beschreibt für **AGP 8.13** eine Mindest-Gradle-Version **8.13**, JDK **17** und Unterstützung bis API **36.1**: https://developer.android.com/build/releases/agp-8-13-0-release-notes .
2. Die offizielle Kotlin-Tabelle nennt für **Kotlin 2.4.21** eine unterstützte Spanne mit Gradle 8.13 und AGP 8.13.2: https://kotlinlang.org/docs/gradle-configure-project.html .
3. Für Compose mit Kotlin ab 2.0 gehört das Compose-Compiler-Gradle-Plugin passend zur Kotlin-Version dazu: https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler .
4. Die derzeitige Play-Anforderung für **neue Android-Apps** lautet **targetSdk mindestens 36**, seit 31.08.2026: https://developer.android.com/google/play/requirements/target-sdk .

**D01-Einstiegskandidat:** JDK 17, Gradle 8.13, AGP 8.13.2, Kotlin/Compose-Compiler 2.4.21, compileSdk 36, targetSdk 36, minSdk 26. Diese Kombination ist aus vorhandenen Konfigurationen und offiziellen Kompatibilitätsangaben abgeleitet – **nicht real gebaut**. Versionen der Compose-BOM und übrigen AndroidX-Bibliotheken müssen in D01 konkret gepinnt und durch eine saubere Build-/Testausführung bestätigt werden. Neuere AGP 9/Compose-Releases erfordern ggf. neue Compiler-/Gradle-Regeln und dürfen nicht ohne Probe gemischt werden.

## 3. CI-Bestandsaufnahme und kritischer Seiteneffekt

### 3.1 Gefährdeter Root-Workflow

Der bestehende [debug-apk.yml](https://github.com/valoon4/Androidbaze/blob/main/.github/workflows/debug-apk.yml):
- wird **bei jedem Push auf main** und manuell ausgeführt, **ohne paths-Filter**;
- hat Schreibberechtigung **contents: write**;
- führt **scripts/apply_wormblade_020.py** aus;
- staged Änderungen unter **app/**, **games/Wormblade/** und **README.md** und führt bei Änderungen **git push origin HEAD:main** aus;
- baut und veröffentlicht nur die Wormblade-Debug-APK.

Das [Wormblade-Skript](https://github.com/valoon4/Androidbaze/blob/main/scripts/apply_wormblade_020.py) schreibt tatsächlich Dateien auf dem Runner. Ob bei einem spezifischen künftigen Run daraus ein Git-Commit entsteht, hängt davon ab, ob sich der Inhalt geändert hat; **nicht behaupten, dass bereits fremder Code geändert wurde**.

**Konsequenz:** Auch ein reiner CommunicationTrainer-Dokumentations- oder D01-Merge nach main löst **zunächst** den Root-Workflow aus. Der getrennte Gradle-Build verhindert Modulkonflikte, **aber nicht** diesen CI-Seiteneffekt.

**Empfehlung:** Root-Workflow in einem **gesondert genehmigten**, kleinen Sicherheits-PR auf sein eigenes Wormblade-Scope einschränken, z. B. passende paths-Filter und bewusste Behandlung von Schreibrechten und Aktualisierungsschritt. **Nicht** innerhalb von D00 ändern. Vor der Änderung den Wormblade-Build und seinen bisherigen Veröffentlichungsweg prüfen; keine stillen Veränderungen an fremden Apps.

### 3.2 Andere Workflow-Beispiele

- [emotionskompass-debug-apk.yml](https://github.com/valoon4/Androidbaze/blob/main/.github/workflows/emotionskompass-debug-apk.yml): projectbezogene paths-Filter für push/PR, **contents: read**, working-directory **games/Emotionskompass/src**, JDK 17, Gradle 8.13, SDK 36, Build-, Unit- und Instrumentierungskompilierung, Signatur-/APK-Identitätsprüfung, Artefakt-Upload.
- [emotionskompass-ux-emulator.yml](https://github.com/valoon4/Androidbaze/blob/main/.github/workflows/emotionskompass-ux-emulator.yml): separater Emulator-PR-Workflow mit KVM-Check, Testreport.
- [timeflow-debug-apk.yml](https://github.com/valoon4/Androidbaze/blob/main/.github/workflows/timeflow-debug-apk.yml): pfadbegrenzt und eigenes working-directory.

**Empfehlung für CommunicationTrainer:** eigene Debug-APK-/Unit-Test-Pipeline, nur auf Änderungen in **apps/CommunicationTrainer/android/** bzw. der Pipeline selbst (plus manuelle Ausführung); **contents: read** und niemals git push nach main.

### 3.3 GitHub-Actions-Sonderfreigabe notwendig

GitHub Actions erkennt klassische Workflowdateien unter **.github/workflows/** im Repository-Root. Eine YAML unter **apps/CommunicationTrainer/ci/** ist als Script-/Konfigurationsdatei hilfreich, aber **kein eigenständiger automatisch erkannter GitHub-Actions-Workflow**.

Damit die geplante automatische Debug-APK entsteht, ist mindestens eine neue Datei wie **.github/workflows/communicationtrainer-debug-apk.yml** nötig – **außerhalb** der bisherigen strikten Änderungserlaubnis.

**D00-Beschlussvorschlag:** Der Nutzer soll eine **gezielte, ausdrücklich bezeichnete Ausnahme** für genau diesen neuen, pfadgefilterten Workflow in einem künftigen D01-PR genehmigen. Die Ausnahme beinhaltet **keine** generelle Erlaubnis, andere Root-/Workflowdateien oder andere Apps zu ändern. Bis zur Freigabe liegen Buildscript und Tests ausschließlich unter **apps/CommunicationTrainer/** und lassen sich ggf. lokal/manuell ausführen.

## 4. Zielstruktur für D01 – vollständig unabhängig

```text
apps/CommunicationTrainer/
├── README.md
├── docs/                 # K1–K6 und D00 – keine Auswirkung auf fremde Apps
├── specs/                # K3–K6 JSON
├── android/              # eigenständige Gradle-Root (D01)
│   ├── .gitignore        # auch nested .gradle/.kotlin/build, SDK und Keys schützen
│   ├── settings.gradle.kts
│   ├── build.gradle.kts
│   ├── gradle.properties
│   ├── gradle/
│   │   └── wrapper/      # Wrapper-Dateien, versioniert + Checksumme
│   ├── gradlew
│   ├── gradlew.bat
│   └── app/
│       ├── build.gradle.kts
│       └── src/main/
│           ├── AndroidManifest.xml
│           └── java/com/valoon4/communicationtrainer/
├── ci/                   # app-spezifische Skripte/Testkonfiguration
└── backend/              # erst bei entschiedenem Cloud-Modus (D05)
```

**Konkrete Vorschläge, noch nicht als Code umgesetzt:**
- `namespace` und `applicationId`: **com.valoon4.communicationtrainer**; im untersuchten Repository kein entsprechender App-Identifier gefunden, endgültige globale Play-Eindeutigkeit noch zu bestätigen.
- `minSdk` **26** als pragmatische Android-Vorstufe; Zielgeräteabdeckung später überprüfen.
- `compileSdk` **36**, `targetSdk` **36** als erster kompatibler Buildkandidat, nicht als dauerhafter Stand.
- JDK **17**; Gradle-/AGP-/Kotlin-/Compose-Paar aus Abschnitt 2.3 als Startpunkt.
- Einfaches **Single Activity + Compose** mit zwei Navigationseinstiegen **Chats** und **Üben**, klar **Mock-/Demo-Modus**. Noch keine echten KI-Accounts, keine Backend-API, keine privaten Chatinhalte, keine Play-Publikation.
- Kotlin/Compose/AndroidX-Versionen über **projektspezifischen** Version Catalog oder vergleichbare zentrale Versionsbindung; keine Root-Versionen überschreiben.
- Eigener Gradle-Wrapper mit festem, verifiziertem Distributionshash; root `./gradlew` bleibt Wormblade. **Der korrekte Arbeitsordner ist apps/CommunicationTrainer/android/.**

### 4.1 Build-/Smoke-Kommandos, erst in D01 ausführbar

Aus dem Unterverzeichnis **apps/CommunicationTrainer/android/**:

```bash
./gradlew :app:testDebugUnitTest --no-daemon
./gradlew :app:assembleDebug --no-daemon
```

Danach:
- APK vorhanden unter **android/app/build/outputs/apk/debug/app-debug.apk**.
- `aapt dump badging`: applicationId **com.valoon4.communicationtrainer**, beabsichtigte Version; nicht Wormblade/InnerWave/TimeFlow.
- `apksigner verify`: Debug-APK korrekt signiert.
- Installations-/Starttest auf einem Emulator oder Gerät; UI zeigt die beiden Einstiege mit Android-Zurück/Lesbarkeit.
- Prüfen, dass Root-Gradle und Projekte unter games/ **unverändert** blieben.
- Falls GitHub-Actions-Ausnahme genehmigt: PR-Artefakt erzeugen und Integrität/Identität nachweisen.

## 5. Build-Isolation und Geheimnisschutz

1. Kein `include(":communicationtrainer")` in Root-settings.gradle.kts.
2. Keine Änderung an Root-AGP, Root-Gradle, Root-README oder anderen games/**-Dateien.
3. Eigene Projektdatei **android/.gitignore**, da die bestehende [Root-.gitignore](https://github.com/valoon4/Androidbaze/blob/main/.gitignore) die verschachtelten **android/app/build/**, **android/.gradle/** und **android/.kotlin/** nicht vollständig abdeckt. Projektweit auch lokale SDK-Einstellungen, Crash- und Signingmaterial ausschließen.
4. Keine produktiven Secrets/API-Tokens in Kotlin-Code, BuildConfig, CI-Logs, Dateien oder Debug-APK.
5. Eindeutige Paket-ID und Debug-Signatur; Debug-Signaturen aus CI können bei wechselnden Runnern Neuinstallation erfordern.
6. Nur Android-SDK-/Gradle-Versionen verwenden, die die offiziellen Kompatibilitätsanforderungen erfüllen; Versionswechsel erst nach eigenem Buildbeleg.
7. Vor jeder künftigen Root-/Workflowänderung konkreten Diff und separate Freigabe einholen.
8. Vor PR-Merge immer Verzeichnis-/Dateiscope prüfen.

## 6. Offene Risiken nach D00

| ID | Schwere | Noch zu bestätigen | Nächster Schritt |
|---|---|---|---|
| D00-R01 | **Hoch** | Root-debug-apk-Workflow löst auch bei fremden main-Pushes aus und kann Änderungen zurückschreiben | Gesonderte Genehmigung für Root-CI-Härtung; **nicht** innerhalb CommunicationTrainer-D00 verändern |
| D00-R02 | **Mittel/Hoch** | Neue CommunicationTrainer-Actions-Datei liegt außerhalb der App-Projektgrenze | Ausdrückliche Ausnahme vor D01-Workflow-Schreibvorgang einholen |
| D00-R03 | Mittel | AGP/Kotlin/Compose-Versionen wurden für neue App nicht real kompiliert | D01 Gradle-Sync + debug build + Unit-/UI-Smoke |
| D00-R04 | Mittel | Nested .gitignore bisher nicht vorhanden | D01 Projekt-.gitignore hinzufügen, Git-Diff testen |
| D00-R05 | Mittel | minSdk-/Geräteabdeckung noch nicht getestet | D01 API-Level/Emulator, spätere Gerätematrix in D04 |
| D00-R06 | Mittel | Keine echte CommunicationTrainer-CI-/APK-/Signaturprüfung möglich | erst D01-Build + ggf. freigegebene CI-Ausnahme |
| D00-R07 | Hoch für Release | Alle geprüften existierenden Apps nutzen targetSdk 35, Google Play fordert für **neue** Apps mindestens 36 | D01 targetSdk >=36 und aktuelle Policy vor öffentlichem Release erneut prüfen |
| D00-R08 | Mittel | Gradle-Wrapper-Distribution/JAR und Checksums noch nicht erstellt | D01 mit echtem Wrapper-Generator erzeugen/validieren |
| D00-R09 | Mittel | Keine definitive Unterstützung für Cloud-/ChatGPT-/lokale KI durch D00 | D04/D05, keine API-Schlüssel/App-Versprechen vorab |
| D00-R10 | Mittel | Bestehende CI-Runs wurden nicht ausgeführt/ausgewertet | bei D01 echte grüne Build-/Testläufe oder dokumentierten Blocker nachweisen |

## 7. D00-Abnahme und D01-Vorbereitung

**Für D00 erfüllt (statische Prüfung):**
- [x] Repository und Standardbranch über GitHub verifiziert.
- [x] Vollständigen Git-Tree, vorhandene Android-Roots und CommunicationTrainer-Dateien inventarisiert.
- [x] Root-/Standalone-Gradleversionen aus echten Konfigurationsdateien gelesen.
- [x] Bestehende 11 Workflows und Pfadfilter statisch verglichen.
- [x] Root-Wormblade-Automatisierungsrisiko mit tatsächlichem Python-Skript belegt.
- [x] Isolierte, konkrete D01-Projektstruktur und erster kompatibler Toolchain-Kandidat beschrieben.
- [x] Google-Play-targetSdk-Anforderung mit aktueller offizieller Quelle abgeglichen.
- [x] Build-/CI-/Emulator-Bewertung ehrlich als **noch nicht ausgeführt** markiert.
- [x] Root- und Fremd-App-Dateien nicht bearbeitet.

**Vor D01 noch erforderlich:**
- [ ] D00-Dokumentations-PR ausdrücklich vom Nutzer genehmigt und nach main gemergt.
- [ ] Entscheidung, ob **genau** eine neue pfadgefilterte Workflow-Datei unter .github/workflows/ im künftigen D01-PR ausnahmsweise erlaubt wird.
- [ ] Entscheidung über eine separate Root-Wormblade-Workflow-Härtung zur Vermeidung von Main-Push-Seiteneffekten.
- [ ] Echten Android-Build, APK-Identität und Signatur durchführen und nachweisen.

**D00 ist eine **Bestandsaufnahme**, keine ausführbare App. Das D01-Codeprojekt wird erst in einem separaten, erneut zu genehmigenden PR erstellt.**
