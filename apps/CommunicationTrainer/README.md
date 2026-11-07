# CommunicationTrainer (Arbeitstitel)

Eigenständiger Projektbereich für die geplante KI-gestützte App zum Üben von **Alltagskommunikation und sozialen Kontakten**.

**Stand:** Konzeption (06.11.2026). Hier existiert bisher **keine App-Implementierung**. Ein endgültiger App-Name steht noch aus.

## Dokumentation

- [Produktkonzept](docs/KONZEPT.md) – aktuelle gemeinsame Arbeitsgrundlage mit Vision, Funktionen, Charaktermodell, Feedback, UX, MVP, Risiken und Meilensteinen.
- [Entscheidungen und Änderungen](docs/ENTSCHEIDUNGEN.md) – nachvollziehbare Festlegungen und Konzeptversionen.
- [Charakter- und Simulationssystem](docs/CHARAKTER_SIMULATION.md) – M2-Detailkonzept für Persona, Zustände, Erinnerung, Gesprächsregie und Editor.
- [Charakter-Qualität & Testplan](docs/CHARAKTER_SIMULATION_TESTPLAN.md) – Evaluationsfälle, Qualitätskriterien und Regressionstests.

- [Lern- und Kompetenzsystem (K1)](docs/K1_LERN_KOMPETENZSYSTEM.md) – sechs Kompetenzbereiche, Anker, zwölf Lektionen, didaktischer Kreislauf und Übergabe an K2.
- [K1-Dialogkontraste und Prüffälle](docs/K1_KOMPETENZ_PRUEFFELLE.md) – neun Dialogbeispiele und 24 fachliche Gegenprüfungen.

- [K2 Kommunikationscoach und Feedbacksystem](docs/K2_KOMMUNIKATIONSCOACH.md) – Beobachtungslogik, Feedbackvarianten, Priorisierung, Lernprofil und Qualitätsanforderungen.
- [K2 Test- und Annotationsplan](docs/K2_FEEDBACK_QUALITAET_TESTPLAN.md) – konkrete Kontrastfälle, Fehlerklassen, menschliche Prüfungen und Sicherheitsgates.

- [K3 Charakterporträts](docs/K3_CHARAKTERE.md) – fünf fiktive erwachsene Personas mit Verhalten, Grenzen und Dialogen.
- [K3 Simulations- und Zustandsregeln](docs/K3_SIMULATIONSREGELN.md) – Persona, Zustände, Memory, Gesprächsaktionen und Schwierigkeitsdynamik.
- [K3 Simulationstestplan](docs/K3_SIMULATION_TESTPLAN.md) – 36 Fälle, menschliche Ratings und Sicherheitskriterien.
- [K3 Persona-Fixtures (JSON)](specs/k3_personas_v1.json) – versionierte Konfigurationsdaten aller fünf Charaktere.
- [K3 Szenario-Fixtures (JSON)](specs/k3_scenarios_v1.json) – sechs konkrete Alltagsszenarien mit Lernzielen.

- [K4 UX-Konzept](docs/K4_UX_KONZEPT.md) – vier Bereiche, Einstieg, Chat, Übung, Coach, Replay, Fortschritt, Privatsphäre und Barrierefreiheit.
- [K4 Nutzerabläufe & Wireframes](docs/K4_NUTZERABLAEUFE.md) – zwölf konkrete End-to-End-Flows und textuelle Bildschirmskizzen.
- [K4 UX-Qualitätsplan](docs/K4_UX_TESTPLAN.md) – 36 Prüfaufgaben, A11y-/Vertrauens-Gates und Pilotmethodik.
- [K4 Screen-Katalog (JSON)](specs/k4_screens_v1.json) – stabile UI-IDs, 29 Screens, Aktionen und Zustände als konzeptioneller Vertrag.

## Projektgrenzen

Alle künftigen app-spezifischen Dateien sollen unter `apps/CommunicationTrainer/` liegen. Andere Apps in diesem Repository und der vorhandene Root-Android-Build bleiben unberührt. Eine spätere eigene Build-Struktur wird vor einer Umsetzung konzipiert und überprüft.

## GitHub-Workflow

1. Konzept- und Codeänderungen in einem separaten Branch vorbereiten.
2. Pull Request mit Beschreibung und nachvollziehbarem Änderungsumfang erstellen.
3. Dem Nutzer den PR zur Prüfung vorlegen.
4. **Erst nach seiner ausdrücklichen Freigabe dieses PR** in `main` mergen.
5. Keine automatische Merge-Freigabe aus früheren PRs ableiten.

Konzeptionelle Vorschläge sind im Produktkonzept von bereits getroffenen Entscheidungen zu unterscheiden.
