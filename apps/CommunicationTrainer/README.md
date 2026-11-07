# CommunicationTrainer (Arbeitstitel)

Eigenständiger Projektbereich für die geplante KI-gestützte App zum Üben von **Alltagskommunikation und sozialen Kontakten**.

**Stand:** Konzeption (07.11.2026). Hier existiert bisher **keine App-Implementierung**. Ein endgültiger App-Name steht noch aus.

## Dokumentation

- [Produktkonzept](docs/KONZEPT.md) – aktuelle gemeinsame Arbeitsgrundlage mit Vision, Funktionen, Charaktermodell, Feedback, UX, MVP, Risiken und Meilensteinen.
- [Entscheidungen und Änderungen](docs/ENTSCHEIDUNGEN.md) – nachvollziehbare Festlegungen und Konzeptversionen.
- [Charakter- und Simulationssystem](docs/CHARAKTER_SIMULATION.md) – M2-Detailkonzept für Persona, Zustände, Erinnerung, Gesprächsregie und Editor.
- [Charakter-Qualität & Testplan](docs/CHARAKTER_SIMULATION_TESTPLAN.md) – Evaluationsfälle, Qualitätskriterien und Regressionstests.

- [Lern- und Kompetenzsystem (K1)](docs/K1_LERN_KOMPETENZSYSTEM.md) – sechs Kompetenzbereiche, Anker, zwölf Lektionen, didaktischer Kreislauf und Übergabe an K2.
- [K1-Dialogkontraste und Prüffälle](docs/K1_KOMPETENZ_PRUEFFELLE.md) – neun Dialogbeispiele und 24 fachliche Gegenprüfungen.

- [K2 Kommunikationscoach und Feedbacksystem](docs/K2_KOMMUNIKATIONSCOACH.md) – Beobachtungslogik, Feedbackvarianten, Priorisierung, Lernprofil und Qualitätsanforderungen.
- [K2 Test- und Annotationsplan](docs/K2_FEEDBACK_QUALITAET_TESTPLAN.md) – konkrete Kontrastfälle, Fehlerklassen, menschliche Prüfungen und Sicherheitsgates.
- [K2 ergänzende Feedbackbeispiele](docs/K2_COACH_BEISPIELE.md) – 20 zusätzliche deutschsprachige Kontrast- und Coachingfälle aus PR #48.
- [K2 ergänzende Qualitätsprüfungen](docs/K2_COACH_QUALITAET.md) – 36 weitere Prüffälle, ergänzend zum maßgeblichen K2-Testplan.

- [K3 Charakterporträts](docs/K3_CHARAKTERE.md) – fünf fiktive erwachsene Personas mit Verhalten, Grenzen und Dialogen.
- [K3 Simulations- und Zustandsregeln](docs/K3_SIMULATIONSREGELN.md) – Persona, Zustände, Memory, Gesprächsaktionen und Schwierigkeitsdynamik.
- [K3 Simulationstestplan](docs/K3_SIMULATION_TESTPLAN.md) – 36 Fälle, menschliche Ratings und Sicherheitskriterien.
- [K3 Persona-Fixtures (JSON)](specs/k3_personas_v1.json) – versionierte Konfigurationsdaten aller fünf Charaktere.
- [K3 Szenario-Fixtures (JSON)](specs/k3_scenarios_v1.json) – sechs konkrete Alltagsszenarien mit Lernzielen.

- [K4 UX-Konzept](docs/K4_UX_KONZEPT.md) – vier Bereiche, Einstieg, Chat, Übung, Coach, Replay, Fortschritt, Privatsphäre und Barrierefreiheit.
- [K4 Nutzerabläufe & Wireframes](docs/K4_NUTZERABLAEUFE.md) – zwölf konkrete End-to-End-Flows und textuelle Bildschirmskizzen.
- [K4 UX-Qualitätsplan](docs/K4_UX_TESTPLAN.md) – 36 Prüfaufgaben, A11y-/Vertrauens-Gates und Pilotmethodik.
- [K4 Screen-Katalog (JSON)](specs/k4_screens_v1.json) – stabile UI-IDs, 29 Screens, Aktionen und Zustände als konzeptioneller Vertrag.

- [K5 Technische Architektur](docs/K5_TECHNISCHE_ARCHITEKTUR.md) – Android-Schichten, lokaler Zustand, KI-Broker, Providerwahl, Idempotenz und Replay.
- [K5 Datenschutz und Sicherheit](docs/K5_DATENSCHUTZ_SICHERHEIT.md) – Privacy-by-Design, DSGVO-/KI-/Play-Anforderungen, Bedrohungsmodell, Meldung und Löschung.
- [K5 Betrieb und Kosten](docs/K5_BETRIEB_KOSTEN.md) – hypothetische Tokenkosten, Budgets, Quotas, Providerbetrieb, Monitoring und Runbooks.
- [K5 Technik- und Sicherheitstestplan](docs/K5_TESTPLAN.md) – 42 Prüfungen, Privacy- und Release-Gates.
- [K5 API- und Datenschnittstellen (JSON)](specs/k5_system_contracts_v1.json) – versionierte Verträge für acht Typen und acht API-Endpunkte.

- [K5 KI-Betriebsarten](docs/K5_KI_BETRIEBSARTEN.md) – lokale Open-Weight-KI, bedingte ChatGPT-Plan-Anbindung und Cloud-Broker/Self-Hosting.
- [K5 Modellvergleich und Kosten-Gates](docs/K5_MODELLVERGLEICH_TESTPLAN.md) – 28 zusätzliche Testfälle für die Auswahl einer zuverlässigen, wirtschaftlichen KI.
- [K5 Provider-Modi (JSON)](specs/k5_provider_modes_v1.json) – vier technische Adapter, drei nutzerseitige Modusoptionen, Verfügbarkeit und Safety-/Privacy-Regeln.

- [K6 Gesamtaudit](docs/K6_GESAMTAUDIT.md) – 26 Zielkonflikte, Risiken und nachvollziehbare Priorisierung.
- [K6 MVP-Umfang](docs/K6_MVP_UMFANG.md) – getrennte technische Alpha und fokussierte V1 mit fünf Figuren/sechs Szenarien.
- [K6 Umsetzungsfahrplan](docs/K6_UMSETZUNGSPLAN.md) – kleine, getrennt freizugebende Entwicklungsstufen D00–D12.
- [K6 Abnahme und Entscheidungen](docs/K6_ABNAHME_UND_ENTSCHEIDUNGEN.md) – Safety-/Privacy-/Qualitätsgates und offene Architekturentscheidungen.
- [K6 MVP-Scope (JSON)](specs/k6_mvp_scope_v1.json) – versionierte Features, Phasen, Tests und Freigabegates.

- [D00 Repository- und Build-Audit](docs/D00_REPO_BUILD_AUDIT.md) – tatsächlicher Ist-Zustand, bestehende CI-Risiken, Android-Toolchain-Vorschlag und D01-Startbedingungen.
- [D00 Maschinenlesbares Build-Audit](specs/d00_build_audit_v1.json) – inventarisierter Git-Snapshot, zehn Risiken und klare Isolationsregeln für D01.

## D01 – Erstes eigenständiges Android-Projekt (PR zur Prüfung)

Der D01-Branch ergänzt unter [android/](android/) ein **eigenes Kotlin-/Jetpack-Compose-Projekt** mit zwei umschaltbaren Bereichen **Chats** und **Üben**, eindeutiger Kennzeichnung als technische Demo und ersten Unit-Tests. Es gibt **noch keine echten Chats, keine KI, kein Backend und keine persistenten Nutzerdaten**. Die tatsächliche APK-Verfügbarkeit hängt von einem erfolgreichen Build ab.

Der **ausdrücklich beauftragte** neue GitHub-Actions-Workflow [communicationtrainer-debug-apk.yml](../../.github/workflows/communicationtrainer-debug-apk.yml) liegt technisch außerhalb dieses App-Verzeichnisses und ist strikt auf `apps/CommunicationTrainer/android/**` sowie die eigene Workflow-Datei gefiltert; `contents: read`, keine Schreib-/Merge-Rechte.

Weder die Root-Gradle-Konfiguration noch Kabelsalat, InnerWave, TimeFlow oder ihre CI-Dateien werden verändert. Die bereits in D00 identifizierte Root-Wormblade-Automatisierung bleibt **unverändert** und ein getrenntes Risikothema.

**D01-Build- und Testanleitung:** [android/README.md](android/README.md). Nach erfolgreichem Workflow-Lauf liegt die Debug-APK als GitHub-Artefakt vor; D01 ist **keine** freigegebene Veröffentlichung.

## D02 – Lokale Gespräche und Entwürfe (PR #59 zur Freigabe)

Die erste tatsächlich nutzbare lokale Speicherung ist unter [android/](android/) umgesetzt: Room mit Gesprächen/Nachrichten/Entwürfen/Replay-Referenzen und DataStore für den System-/Hell-/Dunkelmodus sowie die zuletzt ausgewählte Unterhaltung. Der Chat versendet weiterhin **keine** Nachrichten; echte KI folgt nicht vor der späteren expliziten Modellfreigabe.

[D02-Speicherarchitektur, Grenzen und Testmatrix](docs/D02_LOKALE_PERSISTENZ.md). Der eigene GitHub-Workflow baut die D02-Debug-APK nach erfolgreichem Test; **keine andere App oder ihr Workflow wird verändert**.

## Projektgrenzen

Alle künftigen app-spezifischen Dateien sollen unter `apps/CommunicationTrainer/` liegen. Andere Apps in diesem Repository und der vorhandene Root-Android-Build bleiben unberührt. D00 hat die eigenständige Android-Gradle-Wurzel unter `apps/CommunicationTrainer/android/` als D01-Ziel empfohlen. Eine neue CI-Workflow-Datei außerhalb des Projektordners erfordert vor Erstellung eine ausdrückliche, eng begrenzte Ausnahmefreigabe.

## GitHub-Workflow

1. Konzept- und Codeänderungen in einem separaten Branch vorbereiten.
2. Pull Request mit Beschreibung und nachvollziehbarem Änderungsumfang erstellen.
3. Dem Nutzer den PR zur Prüfung vorlegen.
4. **Erst nach seiner ausdrücklichen Freigabe dieses PR** in `main` mergen.
5. Keine automatische Merge-Freigabe aus früheren PRs ableiten.

Konzeptionelle Vorschläge sind im Produktkonzept von bereits getroffenen Entscheidungen zu unterscheiden.
