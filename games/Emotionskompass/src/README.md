# Emotionskompass – natives Android-Unterprojekt (M1)

Status: **0.2.0-m2**, Design D. Native Kotlin-/Jetpack-Compose-App. **Keine vollständige Tagebuch-App.**

## Build

Ab Repository-Root:

```bash
cd games/Emotionskompass/src
gradle testDebugUnitTest assembleDebug
```

Voraussetzungen: JDK 17+, Gradle 8.13+, Android SDK Platform 36. Das Projekt wird unabhängig vom Androidbaze-Root gebaut. Build-Ausgabe: `app/build/outputs/apk/debug/app-debug.apk`.

Alternativ: GitHub Actions [Emotionskompass M1 Debug APK](../../../.github/workflows/emotionskompass-debug-apk.yml) (PR, Push nach main mit passenden Pfaden oder manuell).

## M2 enthält

- First-run-Onboarding ohne Konto, einmalig über DataStore abgeschlossen (einzige persistente Einstellung).
- Startseite mit zwei gleichwertigen Einstiegen und Design D.
- Adaptive Bottom-Navigation mit Start / Entdecken / Tagebuch / Rückblick.
- Offline-Demoatlas (19 Beispielbegriffe), Kategorien, Freitextsuche, Emotionsdetail und simple Vergleiche.
- Klar gekennzeichnete Vorschau/Platzhalter für Check-in (M3), Tagebuch (M4) und Rückblick (M7).
- System-Dark-Mode, eigenständiges App-Icon, klare Rücknavigation, keine Internetberechtigung.
- Unit-Tests für Katalog und Suchverhalten.

## Sicherheitsgrenze und folgende Meilensteine

**Noch keine Tagebucheinträge!** Die App speichert in M2 ausschließlich den Abschluss des Onboardings als kleine Einstellung, keine persönlichen Emotionseinträge. Keine Verschlüsselung notwendig für diese nicht-sensible Einstellung; echte Datenhaltung, Entwürfe, Verschlüsselung, Backup-/Löschtests und Exporte folgen erst in M3–M5. Die App ist ein Lern-/UI-Prototyp, kein therapeutisches Tool.

M2: 48 versionierte Atlas-Begriffe und neun redaktionelle Vergleichspaare, lokal im JSON-Asset. Dieser Katalog ist eine redaktionelle Arbeitsfassung, nicht klinisch validiert.
M3: geführter Check-in mit mehreren Gefühlen, optionalen Angaben, Drafts.
M4: sichere Reflexion-/Tagebucheinträge (lokales Modell + Transaktionen).
M5: Release-Sicherheits-Gates, mindestens 80 geprüfte Begriffe.

## Architektur

Ein Modul `:app` mit vorläufigen Bereichen `core/designsystem`, `core/navigation`, `core/model`, `core/preferences`; künftig `feature/*`, `data/*`, `domain/*`. `AppViewModel` enthält Navigations-StateFlow; `OnboardingPreferences` kapselt DataStore. Kein UI-Widget schreibt eine Tagebuchdatenbank. Dokumentation unter `games/Emotionskompass/notes` bleibt Referenz.

## Abnahme M1

- Installierbare signierte Debug-APK unter `com.valoon4.emotionskompass`.
- Kein Eingriff in andere GitHub-Projekte oder Root-Build.
- Ohne Netzverbindung startfähig und Atlas verwendbar.
- Onboarding wird nach „Ohne Konto starten“ beim Neustart nicht wiederholt.
- Wechsel zwischen Tabs, Lexikon -> Detail -> Vergleich -> Zurück ohne Crash.
- Unit-Tests erfolgreich. Manueller Geräte-Test für TalkBack und großes Schriftbild weiterhin empfohlen.
