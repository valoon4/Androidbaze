# InnerWave – Branding, Android-Icon und Splash Screen

Stand: 06.11.2026. Umsetzung des vom Nutzer ausgewählten Namens „InnerWave“, des hochgeladenen organischen Wellenlogos und des anschließend ausdrücklich freigegebenen Splash-Designs.

## Grundentscheidung und Geräte-Update

**Die technisch-installierte App bleibt dieselbe:**
- `applicationId = com.valoon4.emotionskompass` und Kotlin-Package bleiben **unverändert**. Nur der für Menschen sichtbare Name wird InnerWave.
- Ein Package-Wechsel würde aus Android-Sicht eine **andere App** erzeugen und den Zugriff auf lokal verschlüsselte Bestandsdaten, Keystore-Schlüssel sowie bestehende Backup-/Exportlogik erschweren oder verhindern.
- Deshalb auch **keine** Änderungen an EKB1/EKB2/EKB3, lokaler Datenhaltung, Berechtigungen, Konten, M7-Datentypen oder den übrigen Apps in Androidbaze.
- **Nicht deinstallieren**, um ein neues Logo zu testen: GitHub Actions signiert Debug-APKs mit möglicherweise unterschiedlichen ephemeren Zertifikaten. Vor einem erforderlichen Neuinstallieren unbedingt eine funktionierende verschlüsselte Sicherung und ihre Wiederherstellbarkeit prüfen.

## Konkrete Änderungen und Dateien

| Android-Datei | Inhalt |
|---|---|
| `src/app/src/main/res/values/strings.xml` | `app_name = InnerWave`. Manifest referenziert diesen Wert bereits mit `@string/app_name`. |
| `src/app/src/main/java/com/valoon4/emotionskompass/MainActivity.kt` | Begrüßung, AppBar, Entsperrbildschirm, Startkennzeichnung und App-Info verwenden den neuen Namen. Bisheriges `Mark()` wird durch das Wellenlogo ersetzt. `InnerWaveSplashScreen()` stellt die genehmigte Gestaltung adaptiv dar. |
| `src/app/src/main/res/drawable/innerwave_launcher_foreground.xml` | Vektorform des Nutzersymbols: organische konzentrische Ringe und warmer Orange-/Cremeverlauf, ausreichend Innenabstand für adaptive Launcher-Masken. Durch Pixelkonturen des Originalmotivs vektorisiert. |
| `src/app/src/main/res/drawable/innerwave_monochrome.xml` | Formgleiche, einfarbige Maske für Androids thematische Icons. |
| `src/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` | Adaptive-Icon-Konfiguration mit Hintergrund, Vordergrund, Monochrom. Gilt ab API 26; die App hat `minSdk = 26`. |
| `src/app/src/main/res/values/colors.xml` | Petrol-Hintergrundfarbe `#004C49` für den Launcher. |
| `src/app/src/main/res/values/styles.xml` | Fensterstartfarben auf Petrol (ältere Android-Versionen). |
| `src/app/src/main/res/values-v31/styles.xml` | Android-12+-System-Splash mit App-Symbol und Hintergrund; **nicht** versuchsweise ein Vollbildbild in `windowSplashScreenAnimatedIcon` setzen, das die OS-Maske abschneiden würde. |
| `src/app/src/androidTest/java/com/valoon4/emotionskompass/InnerWaveBrandingEmulatorTest.kt` | Prüft Name, unveränderte Paketidentität, decodierbare Icon-/Splash-Ressourcen, sichtbaren Splash und Zugänglichkeit der Wortmarke. |
| `src/app/build.gradle.kts` | Version Code 37, `0.7.7-innerwave-branding-preview` als technischer Teststand. |
| `src/ci/run-emulator-tests.sh`, `.github/workflows/emotionskompass-debug-apk.yml` | Drei neue Branding-Emulatortests bei normaler und 200 % Schrift, APK-Signatur, Paket-ID, Label und Version prüfen; Debug-Artefakt unter InnerWave-Namen. |

**Gestaltung:** Die freigegebene statische Splash-Illustration wird als **native, responsiv nachgezeichnete Umsetzung** realisiert, nicht als gestreckter Screenshot. Dadurch bleiben Logo und Wortmarke scharf und für Android-/Displaygrößen anpassbar. Das eigentliche Android-12-Startfenster zeigt technisch bedingt nur einen kurzen, einfachen System-Splash mit identischem Logo/Petrol; direkt danach ist der vollständige Kompositionsscreen für **650 ms bei kaltem Start** sichtbar. Kein künstlicher Ladefortschritt und kein Splash bei normaler Rotation.

## Tests und Abnahme

Automatisierte Gates: erfolgreiche Kotlin-JVM-Domänentests; Android-Debug- und AndroidTest-Assemblierung; **55 UI-Tests bei 100 % plus 55 bei 200 %** Schrift; 3 EKB2 und 9 EKB3 reale AndroidKeyStore-Backup-Tests; `aapt`-Paket-ID/Label/Version; `apksigner verify` und APK-SHA256.

**Zusätzliche manuelle Gates vor Produktfreigabe (nicht automatisch „bestanden“):**
1. Auf echtem Android-Gerät nach dem Installieren den **Launcher-Namen „InnerWave“** und das neue Logo in runder und abgerundeter Maske ansehen.
2. Kalten App-Start und anschließenden Einstieg, Sperr-/Entsperren und Onboarding mit normaler/200-%-Schrift und Dark Mode auf Layout oder sichtbare Blitzer prüfen.
3. Android-12+-System-Splash mit der nativen Komposition vergleichen: Gerät und Android können den kurzen System-Splash selbst steuern. Kein Anspruch auf pixelgenau identische Animation oder fixe Dauer des OS-Splash.
4. In Einstellungen/Onboarding/oberem App-Titel dürfen keine veralteten **sichtbaren** Markenlabels stehen (interne Paket-/Repo-/Modulnamen bleiben bewusst bestehen).
5. Update mit **gleicher** Signatur auf echten Testbeständen, Backup vor möglichem Schlüsselwechsel, manuelle Sicherheits-/Zugänglichkeitsprüfung.

## Nicht Teil dieses PR

Kein Rename der Android-Paket-ID, kein ungetesteter Datenmigrationscode, kein komplettes UI-Theming aller App-Seiten, kein neuer Markenclaim, keine Hintergründe auf privaten Screens und keine Änderung an den anderen Projekten.

**Branch:** `feature/innerwave-branding-splash`. **Kein Merge ohne ausdrückliche Nutzerfreigabe.**
