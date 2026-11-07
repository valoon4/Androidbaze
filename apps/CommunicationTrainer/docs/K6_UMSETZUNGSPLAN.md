# K6 – Implementierungsfahrplan nach Konzeptabschluss

**Version:** 1.0 · **Datum:** 07.11.2026 · **Status:** vorbereitender Entwicklungsplan; noch keine Umsetzung, Schätzung oder Buildfreigabe.
**Ziel:** Kleine, unabhängig prüfbare PRs, statt einen unüberschaubaren Android-/KI-Monolithen in einem Schritt zu entwickeln.
**Scope:** [K6 MVP](K6_MVP_UMFANG.md), [Gesamtaudit](K6_GESAMTAUDIT.md), [Qualitätsgates](K6_ABNAHME_UND_ENTSCHEIDUNGEN.md).

## 1. Entwicklungssystematik

Jeder technische PR erhält:
- **eng abgegrenzte Änderung** mit Nutzen und expliziter Nicht-Zielsetzung;
- nachvollziehbare Schnittstellen / Datenmigration / Sicherheit und erkennbare Abhängigkeiten;
- reproduzierbare lokale Build-/Unit-/UI-/Contract-Tests, soweit die jeweilige Stufe das zulässt;
- konkrete manuelle Testanleitung mit erwarteten Ergebnissen;
- Screenshot oder kurzes Screenrecording für neue UI und nötige Dokumentation;
- dokumentierte Konfiguration, keine Benutzergeheimnisse, API-Keys oder echten Chattexte im Repository;
- keine Änderung an anderer Android-App, Root-Build oder fremden GitHub-Workflows ohne gesonderte Zustimmung;
- **ausdrückliche Nutzerfreigabe für jeden PR vor dem Merge nach main**.

Vor dem ersten Android-Build den Ist-Zustand der Repo-Infrastruktur wirklich prüfen. Der Konzeptordner liefert noch keinen Nachweis für vorhandene Kotlin-/Compose-/Backend-Codebasis.

### D00-Bericht (07.11.2026; statisch geprüft)

Die angeforderte Repository-Bestandsaufnahme ist in [D00_REPO_BUILD_AUDIT.md](D00_REPO_BUILD_AUDIT.md) und [d00_build_audit_v1.json](../specs/d00_build_audit_v1.json) dokumentiert. **Keine Android-/CI-Tests wurden dabei ausgeführt.**

- CommunicationTrainer besitzt bisher **kein Android-Buildprojekt**; D01 muss eine eigene Gradle-Wurzel unter `apps/CommunicationTrainer/android/` erstellen.
- Bestehender Root-Build heißt **Androidbaze**, enthält nur `:app` (Wormblade) und darf nicht um unser Projekt erweitert werden.
- Die existierende **Root-Wormblade-CI** reagiert derzeit auf **jeden** main-Push und kann eigenen Code committen; das ist ein identifiziertes CI-Scope-Risiko und verdient einen separat genehmigten Härtungs-PR.
- Die App-eigene Debug-APK-CI benötigt künftig eine neue GitHub-Workflow-Datei unter `.github/workflows/` **außerhalb** des CommunicationTrainer-Ordners; vor jedem Schreiben dorthin ist eine konkrete Freigabe nötig.
- Toolchain-Kandidat (D01 zu bauen/prüfen): JDK 17, Gradle 8.13, Android-Gradle-Plugin 8.13.2, Kotlin/Compose 2.4.21, compileSdk 36, **targetSdk mindestens 36** für die neue Play-App. Keine Versionskombination als bereits kompiliert ausgeben.

## 2. Umsetzungs-PRs (empfohlene Reihenfolge)

| ID | Bereich | Liefergegenstand | Prüfung und Eintrittsgate |
|---|---|---|---|
| D00 | Ist-Analyse / Setup | Repo-Pfade und vorhandene Workflows prüfen, genaue Gradle-/SDK-/Java-Versionen und Isolationsvertrag | Nachweis: andere Apps bleiben unverändert, passende Android-Toolchain verfügbar |
| D01 | Android Skeleton | eigenes isoliertes Kotlin/Compose-Projekt, Debug-APK, 2 Kernnavigationen und Fake-Daten | App installiert, startet, Android-Zurück/Schriftgröße, app-spezifische CI |
| D02 | Lokale Persistenz | Room Chat/Message/Draft/PersonaRef/ReplayRef-Migration, DataStore Präferenzen | Prozess-Tod, Rotation, Datenmigration, Löschung und lokale Scope-Tests |
| D03 | Fake-Conversation | vollständige Chat-UI, feste KI-Kennzeichnung, Mock-Antworten, Sendestatus und Fehler/Retry | keine verlorenen Entwürfe, keine doppelte Nachricht, Offlinezustände |
| D04 | InferenceGateway + Modell-Spikes | einheitlicher Adaptervertrag, **lokales Testmodell und Cloudreferenz** getrennt benchmarken | K3-Deutsch- und K2-Grenztests, Geräteprofil, Latenz, RAM, Kosten-/Lizenzdaten dokumentiert |
| D05 | Sicherer Broker (falls Cloud gewählt) | Auth/Einladung bzw. begrenzter Gast, serverseitiger Provider-Key, Quotas/Budgets, Idempotenz, Rate Limits, minimaler Statusledger | IDOR, Abuse, Logs, Secrets und Datenschutz-Transfer P0; kein echter Provideraufruf vorher |
| D06 | K3 Charakter-Alpha | Mara/Jonas/Elena, S02/S03, Persona-Versionen, Zustand/Begrenzungen, Gesprächsende | menschliche Testdialoge, keine Kanonbrüche/Sicherheitsverletzungen |
| D07 | K2 Kurzfeedback-Alpha | klare K1-Ziele, validierte Textreferenzen, nur freiwilliges Kurzfeedback, NOT_ASSESSABLE | synthetische K2-Goldfälle, human reviewer, keine erfundenen Belege |
| D08 | Erweiterung auf V1-Inhalte | Deniz/Robin, S01/S04/S05/S06, situative Dynamik, drei Lektionen | fünf unterscheidbare Figuren, sechs konsistente Szenarien, lokale Artikel |
| D09 | Replay als ein Branch | alternative eigene Antwort mit Fixpunkt im Verlauf, Original unveränderlich | Branch-/Recovery-/Lösch-/Kosten-/UX-Tests |
| D10 | V1-Navigation & Fortschritt | vier Tabs, XP nur für Lernaktivität, eigene freiwillige Reflexion, Hilfetext | keine Fähigkeitsnote, UX-Test, TalkBack und Back-Stack |
| D11 | Datenschutz/Safety/Release-Härtung | Report-Flow, Moderationsbetrieb, Privacy-/Lösch-/Backup-/Data-Safety- und Verträge, Fehlerbehandlung | **alle P0-Gates**, Play-/Rechtsprüfung, echte Löschpfade, unabhängige QA |
| D12 | Geschlossene Beta → V1-Abnahme | Dokumentierte Pilot-/Modell-/Kosten-/Privacy-Evaluierung, Fehlertickets, Releasekontrolle | keine bekannten P0/P1, verifizierte Qualität und echte Freigabeentscheidung |

**Reihenfolge ist eine Empfehlung, keine automatische Serienfreigabe.** D04 kann parallel zu D02/D03 als isolierter Benchmark laufen, ohne Produktdaten anzufordern. D05 ist bei rein lokaler KI in anderer Form erforderlich: lokale Sicherheit, Datenintegrität und Gerätequalität bleiben Pflicht; nur serverbezogene Schritte entfallen. D11 kann als eigener PR spät zusammenführen, **kritische Sicherheits-/Consent-Maßnahmen müssen aber schon in allen vorherigen echten Provider-PRs vorhanden sein**.

## 3. Drei Haltepunkte für Architekturentscheidungen

### Gate G-A: vor echter KI-Integration

Vor D04/D05:
- Android-Build und lokale Persistenz funktionieren;
- Toolchain und Repo-Grenze verifiziert;
- synthetische K2-/K3-Fälle für Vergleich vorhanden;
- Datenschutzstatus (wie werden Texte tatsächlich übertragen?) transparent;
- Budget-/Kostenobergrenze und Verantwortliche für Modellaufrufe bestimmt;
- kein Betreiber-API-Key im APK;
- Modell-/Runtime-Lizenzprüfung gestartet.

**Entscheid:** Welche zwei oder mehr konkreten Modell-/Runtime-Kandidaten vergleichen wir, auf welcher Hardware, in welchen rechtlich zulässigen Umgebungen?

### Gate G-B: vor geschlossener realistischer Beta

- End-to-End-Chat mit stabilem Draft/Lifecycle, Sicherheitskontrollen;
- Persona- und Coach-Grenzfälle / Risiko-Negativfälle reviewed;
- keine fremden Chats/Memory sichtbar; serverseitiger Ownership-Schutz;
- sicherer Datentransfer, valider Consent, geklärte Vertrags-/Hostinggrundlage;
- Budget/Quotas und Monitoring real getestet;
- KI-Reporting/Moderation, Rettung bei Ausfall, Löschpfad und Backupregel;
- nur technisch tatsächlich vorhandene UI-Kontrollen sichtbar;
- Testdaten und Teilnehmerbedingungen korrekt.

**Entscheid:** Ist ein stark begrenzter Pilot ohne akutes Sicherheits-/Qualitätsrisiko verantwortbar?

### Gate G-C: vor Play-/öffentlichem V1-Release

- fünf Profile und sechs Szenarien fachlich abgenommen;
- freiwilliges K2-Kurzfeedback und ein Replay-Zweig bestehen Review/Tests oder bleiben bis zur Lösung gesperrt;
- K4-Kernnutzung inkl. TalkBack, dynamischer Schrift, Keyboard/Zurück und Entwurf-Wiederaufnahme validiert;
- Store-Policies, KI-Transparenz, Reporting, Data Safety, Datenschutztexte, Lösch-/Accountansprüche und Lizenzen geprüft;
- gemessene Kosten/Kapazität, Providerverfügbarkeit, Monitoring, Incident-Runbook;
- keine offenen P0/P1-Defekte, die reale Nutzbarkeit/Sicherheit gefährden;
- **separate manuelle Releasefreigabe**, auch wenn alle automatischen Tests grün sind.

## 4. Der kleinste sinnvolle „Vertical Slice“

Nicht mit einer beliebigen Vielzahl von Screens beginnen. Erste funktionale Demonstration:

1. App startet und zeigt zwei Einstiege „Chats“ und „Üben“.
2. Nutzer wählt Jonas aus einem unveränderlichen kuratierten Profil.
3. Nutzer schreibt eine freie Antwort zum Fotokurs.
4. Der **Entwurf** wird gespeichert; danach sendet die App an einen Mock-Provider.
5. Mock liefert eine Antwort mit echter K3-Rollen-/Nachrichtenstruktur (synthetisch, nicht als reale KI verkauft).
6. UI zeigt Gespräch und Status, kann die App beenden und neu starten, ohne Entwurf oder Verlauf zu verlieren.
7. Zweites Szenario: Elena beendet unter Zeitdruck freundlich; kein Button „Überreden“.
8. Fehlerprobe: Request scheitert, Nutzertext bleibt erhalten, erneutes Senden erzeugt keine doppelten Turns.

**Erst danach** dürfen echte Modellqualität und Coachlogik als eigene Arbeitspakete hinzugefügt werden. So lassen sich Android-/Datenfehler von KI-/Produktfehlern unterscheiden.

## 5. Test- und Reviewer-Aufteilung

- **Android/UX:** interaktive Layouts, Lokalpersistenz, Tastatur, Navigation, TalkBack, API-/Device-Ausfall.
- **Backend/Security:** Auth/Owner, Idempotenz, Datenschutz, Providerkeys, Budget, Abuse, Löschung, Reporting.
- **Simulation:** Persona-Version, fünf unterschiedliche Stimmen, Grenzen, situative Dynamik, Konsistenz.
- **Coaching:** Textbelege, passende Unsicherheit, Qualifikationsgates, „keine Kritik“, faire Ausdrucksstile.
- **Produktlernen:** freiwilliges Verständnis, Unterbrechungsfreiheit, konkrete Transferideen, keine Scheinkompetenzmetriken.
- **Betrieb/Recht:** Datenfluss, Provider-/Modelllizenz, DPA, Aufbewahrung, Play/AI-Transparenz.

Zuerst maschinell deterministische Regeln prüfen; dann KI-Ausgaben über unabhängige menschliche Review mit ausreichend diversen Testfällen und begründeten Abweichungen kalibrieren. Komplexe modellabhängige „Qualität = 95 %“ nicht ohne Baseline definieren.

## 6. Bereits bekannte Abhängigkeiten

1. K2-Coach ist **nicht** vertrauenswürdig, nur weil Modellantwort JSON ist: Validator muss Belege prüfen.
2. K3-Charakterqualität erfordert echten Mehrturn-Dialog, nicht nur eine Startnachricht.
3. K4 erlaubt „Offline“ nur für lokale bereits vorhandene Daten, außer ein wirklich lokales Modell ist aktiv.
4. K5-ChatGPT-Plan-Nutzung ist derzeit **kein** verlässlich verfügbares Android-Feature; nicht an D01–D12 koppeln.
5. Serverstatus-/Idempotenz-Ledger ohne dauerhafte Rohchatpersistenz muss technisch bewiesen und die TTL festgelegt werden.
6. Solltests aus K1–K5 sind ein wertvolles Fundament, aber keine bestandene Implementation.
7. Eigene Charaktereditoren, Cross-Session-Memory und langfristige Lernprofilauswertung kommen nur nach eigenständiger Produkt-/Privacy-QA.

## 7. Ausdrückliche GitHub- und Freigaberegel

- Jeder D-Schritt ist ein **neuer PR mit überprüfbarem, isoliertem Diff**, nicht automatisch ein Merge.
- Nutzer muss **jeden einzelnen PR ausdrücklich genehmigen**.
- Keine anderen Apps unter apps/, keine Root-Androidkonfiguration und keine fremden Workflows beiläufig verändern.
- Ein abgeschlossener K6-Dokumentations-PR gibt **keinen** Android-Code, KI-Anbieter oder Produktrelease automatisch frei.
- Neue Ideen dürfen die MVP-Baseline nur mit dokumentiertem Trade-off und neuem Scope-Entscheid erweitern.

## 8. Realistische Erwartung

Der Fahrplan ist eine **Abhängigkeitsfolge, keine Zeit- oder Kostenprognose**. Eine glaubwürdige Schätzung ist erst nach Repository-/Toolchain-Prüfung, Android-Vertical-Slice und realen Modell-/Betriebsdaten möglich.

**Nächste Handlung nach K6-Freigabe:** mit **D00 – Repo-/Build-Bestandsprüfung** beginnen und daraus den kleinen **D01 Android-Skeleton-PR** ableiten.
