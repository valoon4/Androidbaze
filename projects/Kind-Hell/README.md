# Kind Hell
**Arbeitsname:** Kind Hell  
**Projekt:** EK 2.0 / Eugens Königreich  
**Status:** v0.0.1 – native Android-Offline-Demo erfolgreich per GitHub Actions gebaut (Feature-Branch); kein Mehrnutzerbetrieb  
**Start der eigenständigen Projektablage:** 2026-11-17

## Vision
Ein sozialer Treffpunkt, der Menschen dabei hilft, neue Bekanntschaften, Freundschaften und freiwillig auch Beziehungen aufzubauen. Inspiriert vom offenen Gemeinschaftsgefühl des früheren „Anime Germany“ / „Eugens Königreich“ (EK), aber mit moderner, nutzerkontrollierter Entdeckung von Menschen.

**Leitidee:** Menschen kommen wegen eines Interesses, bleiben wegen der Menschen.

## Produktkern
1. **Marktplatz:** Öffentlicher, themenoffener Austausch ohne Offtopic-Polizei. Spontane kurze Posts sind willkommen. Text-first.
2. **Ankunft:** Willkommensrunden für neue Mitglieder, inspiriert von Sammelposts für etwa 20 Neuzugänge. Vorstellungen sind freiwillig; der soziale Einstieg soll aktiv erleichtert werden.
3. **Kompass:** Freiwilliges Entdecken anderer Menschen anhand von Interessen, gewünschter Kontaktart und Kommunikationsstil. Sowohl Gemeinsamkeiten als auch unerwartete Begegnungen ermöglichen.

## Grundsätze
- Community statt Content-Farm: Zugehörigkeit und hilfreiche Gespräche wichtiger als Verweildauer.
- Möglichst wenige soziale Vorschriften; keine willkürliche Themenpolizei oder „Low Effort“-Strafen.
- Gleichzeitig klare, verhältnismäßige Schutzmechanismen gegen Belästigung, nicht-einvernehmliche Inhalte, Missbrauch und rechtswidrige Inhalte. Blockieren/Melden und transparente Entscheidungen.
- Nutzer bestimmen selbst, welche persönlichen Angaben sichtbar werden; sensible Angaben nur freiwillig, kontextbezogen und ggf. nach gegenseitiger Freigabe.
- Erstmal keine Bild-Uploads prüfen, um den Fokus auf Gespräche zu legen und den Moderationsaufwand zu senken; Entscheidung noch offen.
- Langfristig selbst kontrollierte Infrastruktur, Backups und Exportmöglichkeiten, damit die Gemeinschaft nicht von einer fremden Plattform abhängt.
- Ein mögliches Premium-Modell soll Zugehörigkeit unterstützen, nicht Einsamkeit ausbeuten. Grundlegende Kommunikation darf nicht zum Bezahlzwang werden.
- Nicht alles ist Dating. Freundschaft und allgemeiner Austausch sind gleichwertige Ziele.

## Herkunft und Stand
- Historische Inspiration: Anime Germany / Eugens Königreich, laut Gründer 2014 rund 10.000 Mitglieder.
- Das frühere Konzept lebt als neue, eigenständige Produktidee weiter.
- Die private Ideenskizze aus `Psychologicalz/ideen/mitstreiter-community-produktidee-2026-11-13.md` wurde **nicht verschoben oder veröffentlicht**. Diese Projektakte enthält nur eine datensparsame, produktbezogene Zusammenfassung.
- In `Androidbaze` liegt die eigenständige native Demo jetzt unter `projects/Kind-Hell/android/` auf Branch `kind-hell/v0.0-native-guild`. Wormblade im Rootprojekt blieb unverändert.

## Nächster Schritt
Siehe [MVP.md](MVP.md) für die erste testbare Version und [ARCHITECTURE.md](ARCHITECTURE.md) für die festgelegte native Android-Struktur. Das bestehende Wormblade-Root-Buildtarget bleibt unangetastet.

## Erster nachgewiesener Build (17.11.2026)
- **Version:** Kind Hell v0.0.1, Kotlin/Jetpack Compose, APK-ID `com.valoon4.kindhell`.
- **Branch:** `kind-hell/v0.0-native-guild`; Root-`:app` (Wormblade) nicht geändert.
- **Build-Nachweis:** [GitHub Actions #44182891562](https://github.com/valoon4/Androidbaze/actions/runs/44182891562), erfolgreich inkl. Paket-/Signaturprüfung; APK-Artefakt `Kind-Hell-v0.0.1-debug`.
- **Funktionsumfang:** Offline-Marktplatz mit Beiträgen/Antworten, Ankunft mit lokaler Vorstellung, Kompass mit Filtern/Merkliste; alle Beispielprofile klar als Demo markiert.
- **Teststatus:** Build bestätigt, APK erzeugt; **noch kein Praxistest auf einem Android-Gerät** und keine Backend-Kommunikation.
- [Android-Projekt, Build und Einschränkungen](android/README.md).
