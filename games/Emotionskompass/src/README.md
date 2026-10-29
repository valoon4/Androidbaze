# Emotionskompass – native Android-App (M2)

**Version:** 0.2.0-m2 · **Status:** Offline-Emotionsatlas als Android-Testversion. Kein produktives Tagebuch, keine medizinische Anwendung.

## Inhalte dieser Etappe

- Native Kotlin-/Jetpack-Compose-App mit Onboarding ohne Konto, Startseite und Design D in Hell/Dunkel.
- Vier Navigationsbereiche: Start, Entdecken, Tagebuch (Platzhalter), Rückblick (Platzhalter).
- **48 versionierte deutschsprachige Begriffe** in acht Emotionsfamilien, wobei ein Begriff zu mehreren Kategorien gehören darf.
- 48 Definitionen, je zwei Beispiele, Synonyme, verwandte Einträge und offene Reflexionsfragen.
- **Neun redaktionell formulierte Vergleiche**; bei anderen Paaren bewusst nur Gegenüberstellung ohne erfundene Abgrenzung.
- Vollständig lokale Suche nach Namen, Alias und Beschreibung, tolerant für Umlaute und Groß-/Kleinschreibung, mit Relevanzsortierung.
- Qualitätssicherung mit JVM-Unit-Tests zu Schema, Verweisen, Familien, Vergleichen und Suche.
- Jede persönliche Journal-/Check-in-Funktion ist ausdrücklich noch nicht nutzbar. Es wird nur der Abschluss des Onboardings lokal als DataStore-Präferenz gespeichert.

Der Atlas ist eine **redaktionelle Arbeitsfassung**, nicht durch eine externe psychologische Fachredaktion klinisch validiert. Die Breitenprüfung mit mindestens 80 kuratierten Begriffen ist für das M5-Release vorgesehen.

## Projektstruktur

```
games/Emotionskompass/src/
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── assets/catalog/emotions.de.v2.json  # einziger fachlicher Datenstand
│       │   └── java/com/valoon4/emotionskompass/
│       │       ├── MainActivity.kt
│       │       └── core/{model,navigation,designsystem,preferences}/
│       └── test/java/com/valoon4/emotionskompass/EmotionCatalogTest.kt
└── gradle/libs.versions.toml
```

## Build und Test

Das Unterprojekt wird unabhängig vom Androidbaze-Root-Projekt gebaut. Unter `games/Emotionskompass/src`:

```sh
gradle :app:testDebugUnitTest :app:assembleDebug
```

Voraussetzungen: Java 17, Android SDK 36, Gradle 8.13. Alternativ nutzt GitHub Actions den Pfadfilter-Workflow `.github/workflows/emotionskompass-debug-apk.yml` und publiziert einen signierten Debug-Build als herunterladbares Workflow-Artefakt. Die CI verifiziert Paketname und Signatur.

**Paketkennung:** `com.valoon4.emotionskompass` · **minSdk 26** · **versionCode 2**, aktualisiert daher die M1-Installation mit derselben Paket-ID (Android-Debugsignatur muss übereinstimmen).

## Datenschutz- und Funktionsgrenzen

Keine Internetberechtigung, kein Server, keine personenbezogene Emotionshistorie und keine Analyse. Ein Atlas kann persönliche Reflexion **nicht diagnostizieren**. Die Formulierungen sind Vorschläge zur eigenen Einordnung.

Nächste Schritte:
- **M3:** Gefühlsnavigator, optionale Wahrnehmung, 0–n gleichzeitig ausgewählte Emotionen, Intensitäten, unterbrechungsfeste Entwürfe.
- **M4:** Reflexion und Tagebuch; echte Speicherung nur mit geeignetem Schutzkonzept.
- **M5:** Sicherheits- und Accessibility-Release-Gates einschließlich verschlüsselter Persistenz und redaktioneller Fachprüfung.

Weitere Details: `games/Emotionskompass/notes/M2_ATLAS.md` sowie die Produktspezifikation.
