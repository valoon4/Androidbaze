# Emotionskompass (Arbeitstitel)

**Status:** Produktspezifikation v1.0, 28.10.2026. Konzept-/Architekturphase; noch keine App und keine APK.  
**Ziel:** Eine deutschsprachige, offlinefähige Android-App, die Emotionswissen und persönliche Selbstreflexion gleichwertig miteinander verbindet.

## Dokumentation

1. [Produktspezifikation v1](notes/PRODUKT_SPEZIFIKATION_V1.md) – Zielgruppen, Anforderungen, Screens, Nutzerabläufe, Emotionsmodell, UX, Design.
2. [Technische Architektur v1](notes/TECHNISCHE_ARCHITEKTUR_V1.md) – Android-Architektur, Datenmodell, Datensicherheit, Repository-Struktur und Qualitätsanforderungen.
3. [Inkrementeller Umsetzungsplan v1](notes/UMSETZUNGSPLAN_V1.md) – Etappen, Akzeptanzkriterien, Teststrategie, Definition of Done und Änderungsmanagement.

## Verbindliche Leitlinien

- **Zwei gleichwertige Säulen:** Emotionen verstehen und sich selbst verstehen.
- **Keine Diagnosen, kein Leistungsdruck:** Lern- und Reflexionswerkzeug, keine Therapie.
- **Offline-first und datensparsam:** Keine Anmeldung oder Cloud-Pflicht. Keine echten privaten Einträge in Git.
- **User agency:** Vorschläge niemals als Gewissheiten darstellen; mehrere/unklare Gefühle sind ausdrücklich erlaubt.
- **Inkrementell:** Nach jeder Entwicklungsstufe ein lauffähiger Stand mit Tests; das bestehende Root-Projekt im Repository bleibt unangetastet.
- **Design D:** warm-natürlich mit minimalistisch-eleganter Informationsarchitektur.

## Vorgesehene Projektstruktur

    games/Emotionskompass/
      README.md
      notes/
        PRODUKT_SPEZIFIKATION_V1.md
        TECHNISCHE_ARCHITEKTUR_V1.md
        UMSETZUNGSPLAN_V1.md
      src/                 # eigenständiges Android/Gradle-Projekt ab Umsetzungsphase
      apks/                # optional freigegebene Meilensteine; CI-Artefakte bevorzugt

Die aktuelle Repo-Root-App bleibt erhalten. Der Name **Emotionskompass** und die finale App-ID sind vor dem ersten Release zu bestätigen.