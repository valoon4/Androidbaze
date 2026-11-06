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

## Projektgrenzen

Alle künftigen app-spezifischen Dateien sollen unter `apps/CommunicationTrainer/` liegen. Andere Apps in diesem Repository und der vorhandene Root-Android-Build bleiben unberührt. Eine spätere eigene Build-Struktur wird vor einer Umsetzung konzipiert und überprüft.

## GitHub-Workflow

1. Konzept- und Codeänderungen in einem separaten Branch vorbereiten.
2. Pull Request mit Beschreibung und nachvollziehbarem Änderungsumfang erstellen.
3. Dem Nutzer den PR zur Prüfung vorlegen.
4. **Erst nach seiner ausdrücklichen Freigabe dieses PR** in `main` mergen.
5. Keine automatische Merge-Freigabe aus früheren PRs ableiten.

Konzeptionelle Vorschläge sind im Produktkonzept von bereits getroffenen Entscheidungen zu unterscheiden.
