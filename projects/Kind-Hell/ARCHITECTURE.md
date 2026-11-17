# Kind Hell – Android-Implementierungsstruktur

## Festlegung (17.11.2026)
**Native Android-App**, kein WebView und keine mobile Website als Hauptprodukt. Android-first; Backend separat, damit echte Mehrnutzerkommunikation möglich ist.

## Vorhandene Infrastruktur
Das Repository `Androidbaze` hat Gradle/Android-Tooling, ein Bootstrap-Buildscript und GitHub Actions für den Root-Build. **Achtung:** Das Root-Projekt `:app` baut aktuell Wormblade (`com.valoon4.wormblade`). Es darf **nicht** durch Kind Hell ersetzt werden.

## Zielstruktur
```text
projects/Kind-Hell/
├── README.md
├── MVP.md
├── ARCHITECTURE.md
├── android/                     # eigenständiges natives Gradle-Projekt
│   ├── settings.gradle.kts
│   ├── build.gradle.kts
│   └── app/
│       ├── build.gradle.kts
│       └── src/main/
│           ├── AndroidManifest.xml
│           └── java/.../
│               ├── MainActivity.kt
│               ├── ui/          # Marktplatz, Ankunft, Kompass
│               ├── domain/      # Modelle und Use Cases
│               └── data/        # API, Repositories, lokale Speicherung
└── backend/                     # später eigenes Service-/API-Projekt
```

## Technischer Vorschlag für v0.1
- Kotlin, native Android-Benutzeroberfläche (Jetpack Compose geplant).
- Ein Hauptbildschirm mit Navigation für **Marktplatz**, **Ankunft**, **Kompass**.
- Zuerst lokale Demo-Daten und Klickwege; anschließend echte Nutzerkonten und Serverdaten.
- Backend mit authentifizierten APIs für Profile, Beiträge, Antworten, Begrüßungsrunden, Blockierungen und Meldungen; genaue Stackwahl offen.
- Kein globaler Zwangsalgorithmus. Filter und Sichtbarkeit bleiben unter Nutzerkontrolle.
- Keine Bild-Uploads im Pilot; Moderation/Meldewege bleiben trotzdem notwendig.
- Schutz persönlicher Daten, begrenzte Datenspeicherung, Export und Backups müssen vor einer öffentlichen Beta geplant und geprüft werden.

## Späterer Datenschutz-Baustein: „Verbindungen“
Eine mögliche spätere Erweiterung des **Kompasses** umfasst Aktivitäten, Mitgestaltung und optional verschiedene Beziehungsperspektiven. **Noch keine technische Implementierungsentscheidung.** Öffentliche Profildaten, private Kontaktwünsche und besonders sensible Beziehungsinformationen brauchen klar getrennte Daten- und Berechtigungskonzepte; keine pauschale Veröffentlichung oder sensible Suche im Pilot. Erwachsene Inhalte und Beziehungsoffenheit (z. B. ENM) erst nach separatem 18+-Gate, expliziten/widerrufbaren Freigaben, Schutz vor Outing und Moderationskonzept. Keine automatische Ableitung von Einwilligung aus einem Match. Siehe [CONNECTIONS.md](CONNECTIONS.md).

## Inkrementelle Meilensteine
1. **0.0:** Native App-Grundstruktur + lokal navigierbare Screens.
2. **0.1:** Öffentlicher Text-Marktplatz mit Post/Antwort und einfachem Profil; Pilot-Backend.
3. **0.2:** Opt-in-Ankunftsrunden.
4. **0.3:** Kompass light mit freiwilligen Filtern.
5. **Beta:** Mehrnutzer-Test, Blockieren/Melden, Backup/Restore, Datenschutz- und Sicherheitsreview.

## Build-Isolation
- Kind Hell nutzt ein **eigenes** Gradle-Projekt in `projects/Kind-Hell/android`.
- Root-`app` sowie Root-CI bleiben zunächst unverändert.
- Eigenständiger Workflow `.github/workflows/kind-hell-debug-apk.yml` für Build und APK-Artefakt auf dem Feature-Branch eingerichtet.
- **Buildnachweis:** [GitHub Actions #44182891562](https://github.com/valoon4/Androidbaze/actions/runs/44182891562) erfolgreich, Paket `com.valoon4.kindhell`, Debug-APK mit Signatur verifiziert. Noch kein physischer Gerätetest.

## Implementierungsstand v0.0.1 (17.11.2026)
- Meilenstein **0.0 (UI)**: 3 Jetpack-Compose-Tabs, lokale Beiträge/Antworten, freiwillige lokale Vorstellung, Kompass-Filter/Merkliste, Alias-Dialog; Daten im Android-Sandboxbereich via SharedPreferences.
- **Nicht erledigt:** 0.1 Backend, echte Accounts/Mehrnutzerbetrieb, 0.2 serverbasierte Ankunftsrunden, 0.3 serverbasierter Kompass, Blockieren/Melden, Datenschutz-/Sicherheitsreview, Backup/Export.
- **Isolation:** Entwicklung auf `kind-hell/v0.0-native-guild` unter `projects/Kind-Hell/android/`; Root-`:app`/Wormblade bleibt im Vergleich unverändert. APK hat eigene Application-ID `com.valoon4.kindhell`.
