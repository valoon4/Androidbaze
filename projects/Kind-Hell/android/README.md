# Kind Hell – native Android Demo v0.0.2 (Design D / Seele Mystic)

Eigenständiges Android-/Kotlin-/Jetpack-Compose-Projekt. **Nicht** das Root-Projekt `:app` (Wormblade).

## Gewählte visuelle Richtung
- **D „Seele Mystic“** wurde vom Gründer aus vier unterschiedlichen Designvorschlägen gewählt (17.11.2026), siehe [DESIGN.md](../DESIGN.md).
- Native Compose-Umsetzung: dunkelblauer Nachthimmel, lavendel-/cyanfarbenes Mondlicht, dezente von Compose gezeichnete Mond-/Sternenelemente, ruhige Karten und erneuerte Bereichstexte.
- Es wurden **keine** generierten Anime-Figuren oder urheberrechtlich fremde Grafiken eingebaut. Keine neuen Online-Funktionen oder erfundenen Nutzer-Kontaktpräferenzen.
- **Hinweis zu Debug-Signaturen:** GitHub-hosted Builds können bei jeder neuen CI-Ausführung einen anderen Android-Debug-Signaturschlüssel bekommen. Eine bestehende Debug-APK lässt sich dann möglicherweise nicht drüberinstallieren. Bei „App nicht installiert“ alte Test-App erst deinstallieren (dabei gehen lokal gespeicherte Beiträge/Alias/Vorstellung/Merkliste verloren) und neue APK frisch installieren. Langfristig einen konstanten, sicher verwahrten Signierschlüssel verwenden.

## Enthalten
- **Marktplatz:** lokale Beispielbeiträge, neue Beiträge schreiben und beantworten.
- **Ankunft:** Demo-Begrüßungsrunde mit freiwilliger, lokal gespeicherter Vorstellung.
- **Kompass:** fiktive Profile nach Interesse, Kontaktart und Sprache filtern sowie lokal merken.
- **Profil:** Alias auf dem Gerät festlegen.
- Durchgehend deutliche **Offline-/Demo-Kennzeichnung**; keine echten Benutzer, Nachrichten oder Kontakte.

Die Inhalte werden in privaten Android SharedPreferences gespeichert (lokal, unverschlüsselt im App-Sandboxbereich). Keine Netzberechtigung. Kein Backend, keine Registrierung, kein Tracking. App deinstallieren löscht lokale Daten, sofern keine OS-Sicherung erfolgt (App-Backup ist im Manifest deaktiviert).

## Build
Dieses Projekt ist unabhängig vom Androidbaze-Root-Build:
```sh
cd projects/Kind-Hell/android
gradle :app:assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```
JDK 17, Android SDK 35 und Gradle 8.9 werden für den CI-Build bereitgestellt. Der GitHub-Actions-Workflow `.github/workflows/kind-hell-debug-apk.yml` baut ausschließlich Kind Hell und lädt eine signierte Debug-APK als Workflow-Artefakt hoch.

## Grenzen vor einem öffentlichen Pilot
Echte Mehrnutzerkommunikation, Authentifizierung, Moderation (Blockieren/Melden), Missbrauchsschutz, Export, Backup-/Restore-Konzept und Datenschutzprüfung fehlen noch. Dieser Build ist **nur ein lokaler UI- und Interaktionsprototyp**. Die Feature-Roadmap steht in `../MVP.md` und `../ARCHITECTURE.md`.

## v0.0.2 CI-Nachweis
- GitHub Actions: [#44187424714](https://github.com/valoon4/Androidbaze/actions/runs/44187424714) **erfolgreich** (Gradle/Kotlin, APK-Metadaten, Signatur).
- Artefakt: `Kind-Hell-v0.0.2-debug`, Paket `com.valoon4.kindhell`, VersionCode 2, VersionName 0.0.2, minSdk 26, targetSdk 35.
- APK SHA-256: `e8510f0655987aa55d4cf39bcbdae4e04524392da760302fa1dfc83d9ab85d5b`.
- **Kein Gerätetest**, keine Serveranbindung. V0.0.2 ist nur die visuell erneuerte Offline-Demo.
