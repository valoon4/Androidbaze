# TimeFlow – Zeiterfassung (Android Debug)

Eigenständige, deutschsprachige Android-Zeiterfassungs-App im Androidbaze-Monorepo.
Paket-ID: `com.valoon4.timeflow`. Der bestehende Root-Build für Wormblade wird nicht verändert.

## Funktionen

- Projekte (Farbe, Beschreibung, Status), Aufgaben und Sessions verwalten
- Start / Pause / Fortsetzen / Stopp einer Session, immer nur ein aktiver Timer
- Nachtragen, Bearbeiten und Löschen von Sessions mit Sicherheitsabfragen
- Arbeitszeiten auf Projekt- und Aufgabenebene, Dashboard, Wochenchart, Analyse
- Suche, Hell-/Dunkelmodus, CSV-Export
- Persistenz über Android SharedPreferences, offline, ohne Konto oder Internet
- Pausen werden bei getrackten Sessions aus der Arbeitszeit ausgeschlossen.
- Wird die App geschlossen, bleibt eine laufende Session anhand gespeicherter Zeitstempel korrekt erhalten. Android zeichnet im geschlossenen Zustand jedoch keine Hintergrund-Benachrichtigung auf.

## Build

Im Repository unter **Actions → Build TimeFlow Debug APK** findest du den GitHub-Actions-Build. Die hochgeladene Datei `TimeFlow-debug-apk` enthält die signierte und installierbare `TimeFlow-debug.apk`.

Lokal (mit Android SDK 35 und Gradle 8.9):
`gradle -p games/TimeFlow assembleDebug`

APK: `games/TimeFlow/app/build/outputs/apk/debug/app-debug.apk`

## Datenschutz

Alle Daten werden ausschließlich lokal auf dem Gerät in den App-Einstellungen gespeichert. Das Löschen der App-Daten entfernt die erfassten Zeiten. Vorher CSV exportieren.
