# Kind Hell – native Android Demo v0.0.1

Eigenständiges Android-/Kotlin-/Jetpack-Compose-Projekt. **Nicht** das Root-Projekt `:app` (Wormblade).

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
