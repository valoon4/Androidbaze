# TimeFlow – Zeiterfassung (Android Debug)

Eigenständige, deutschsprachige Android-Zeiterfassungs-App im Androidbaze-Monorepo.
Paket-ID: `com.valoon4.timeflow`. Der bestehende Root-Build für Wormblade wird nicht verändert.

## Was ist neu in v1.1?

- Das vom Nutzer gelieferte TimeFlow-Logo erscheint als Launcher- und Adaptive Icon sowie im App-Header.
- Verbesserter Session-Editor: Start, Ende, Pausen und Arbeitszeit synchronisieren sich nach jeder abgeschlossenen Feldänderung.
- Aufgezeichnete Pausen werden korrekt angezeigt, bei Änderungen als explizite Gesamtdauer gespeichert.
- Datum und Uhrzeit sekundengenau, Arbeits-/Pausenzeit im Format HH:MM:SS, Kurzformat 1:30 und verständliche Eingabefehler.
- Statistiken berücksichtigen auch Sessions über Mitternacht (für nachbearbeitete aggregierte Sessions werden Zeiten proportional auf die betroffenen Kalendertage verteilt).
- Automatisierte JavaScript- und Zeitlogik-Tests.
- CSV-Import für alte v1.0-Exporte: stellt Projekte, Aufgaben, Sessions und Arbeitszeiten wieder her. Nicht im alten CSV enthaltene Projekt-Metadaten können nicht wiederhergestellt werden.

**Update-Hinweis:** Die mit dem temporären GitHub-Actions-Debugschlüssel signierte v1.0-APK kann nicht garantiert durch eine spätere Debug-APK überschrieben werden. Ohne gleichen Signierschlüssel verweigert Android ein Update. Deinstallation löscht lokale Daten. **Sicherer Wechsel:** In v1.0 zuerst sämtliche laufenden Sessions beenden, unter Einstellungen die CSV exportieren und diese Datei sicher speichern; erst danach v1.0 deinstallieren, v1.1 installieren und unter Einstellungen → „CSV-Backup importieren“ den Export einlesen. Die CSV enthält nur Sessions und Projekt-/Aufgabennamen, keine vollständigen Projektbeschreibungen, Farben oder Status. Für wirklich nahtlose Updates ist zukünftig ein dauerhaft geschützter Signing Key erforderlich.

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

Im Repository unter **Actions → Build TimeFlow Debug APK** findest du den GitHub-Actions-Build. Die hochgeladene Datei `TimeFlow-v1.1-debug-apk` enthält die signierte und installierbare `TimeFlow-v1.1-debug.apk`.

Das Logo basiert auf der originalen hochgeladenen Artwork-Datei, deren WebP-Daten platzsparend in fünf Base64-Teilen unter `assets/icon.part*` liegen und nur während des Builds zu Android-Bildressourcen zusammengesetzt werden.

Lokal (mit Android SDK 35 und Gradle 8.9):
`gradle -p games/TimeFlow assembleDebug`

APK: `games/TimeFlow/app/build/outputs/apk/debug/app-debug.apk`

## Datenschutz

Alle Daten werden ausschließlich lokal auf dem Gerät in den App-Einstellungen gespeichert. Das Löschen der App-Daten entfernt die erfassten Zeiten. Vorher CSV exportieren.
