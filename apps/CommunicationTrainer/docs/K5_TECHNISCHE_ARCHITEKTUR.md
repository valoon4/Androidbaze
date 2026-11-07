# K5 – Technische Architektur und Integrationskonzept

**Version:** 1.0 · **Datum:** 07.11.2026 · **Status:** Architekturempfehlung zur Prüfung, keine Implementierung
**Projektgrenze:** ausschließlich apps/CommunicationTrainer/ · **Ziel:** Android-MVP mit freiem Chat, Training, K2-Coach, K3-Personas und K4-UX
**Verknüpfungen:** [K5 Datenschutz und Sicherheit](K5_DATENSCHUTZ_SICHERHEIT.md) · [K5 Betrieb und Kosten](K5_BETRIEB_KOSTEN.md) · [K5 Prüfplan](K5_TESTPLAN.md) · [K5 Schnittstelleninventar](../specs/k5_system_contracts_v1.json)

## 0. Entscheidung auf einen Blick

**Empfehlung zur K6-Abnahme:** Android-Anwendung in Kotlin mit Jetpack Compose, ViewModels und klaren Repository-Grenzen; lokal lesbare Chat-Historie, Entwürfe und Übungsstatus; für generative Antworten ein minimaler, abgesicherter Backend-Broker, der Modellzugriff, Berechtigungen, Kostenlimits, Safety, Prompt-/Persona-Versionierung und fachliche Validatoren kontrolliert. Der Modellanbieter bleibt austauschbar.

Diese Kombination ist **kein Versprechen** von vollständiger Offline-Generierung, Ende-zu-Ende-Verschlüsselung gegenüber dem KI-Anbieter, unbegrenzter anonymer Nutzung oder sofortiger Löschung aller Drittanbieter-Backups. Solche Funktionen werden nicht angeboten, bevor die Architektur ihre Aussagen nachweisen kann.

### 0.1 Entscheidungsmatrix

| Alternative | Vorteile | Schwächen | K5-Einschätzung |
|---|---|---|---|
| A: rein auf dem Gerät generieren | kein notwendiger externer Modellaufruf pro Nachricht, ggf. hohe Datensouveränität | große Modell- und Speicheranforderungen, Qualität auf Einstiegsgeräten, Energieverbrauch, schwierige Updates | **separat evaluieren**, kein ungeprüfter MVP-Default |
| B: Android-App ruft KI-Dienst direkt auf | schneller Prototyp, wenige Backend-Bausteine | Schlüssel-/Kostenmissbrauch, schwierige Quotas, begrenzte zentrale Safety/Abrechnung, Datenschutz-/Vendor-Steuerung | **kein öffentlicher Release** mit Betreiber-API-Schlüssel im Client |
| C: Android + Broker + externer KI-Anbieter | serverseitige Secrets, Modellwechsel, Regeln/Kosten-/Missbrauchsschutz, flexible Evaluierung | Betriebs-, Sicherheits- und Datenschutzaufwand, Netzverbindung notwendig | **präferierter MVP-Entwurf**, nach Kosten- und Datenschutzprüfung |

**Gastzugang:** Gegenwärtig **bedingt**. Ein kostenloser, unbegrenzter und gleichzeitig missbrauchsgeschützter Gastzugang ist kein realistischer impliziter Standard. Vor öffentlichem Betrieb müssen Identitäts- und Kostenkontrolle erprobt werden; ansonsten ehrliche Anmeldung bzw. begrenzte Testkontingente. Keine Identifizierung über invasive Geräte-Fingerprints.

### 0.2 Ergänzung nach Kostenanalyse: austauschbare KI-Betriebsarten

Das ursprüngliche MVP-Referenzdesign **Android + abgesicherter Broker + austauschbarer Cloud-Provider** bleibt eine technisch zu evaluierende Basis, ist aber nicht mehr die einzige vorgesehene Inferenzoption. Der Nutzer hat zusätzlich die Untersuchung von **lokaler Open-Weight-KI** und einer **freiwilligen ChatGPT-Plan-Anbindung** beauftragt.

- [K5_KI_BETRIEBSARTEN.md](K5_KI_BETRIEBSARTEN.md) erläutert lokale Android-Inferenz, offiziellen ChatGPT-Kontozugang (nur bei nachgewiesener Plattform-/Vertriebsberechtigung) und Cloud-KI mit Managed API oder selbst gehostetem Modell.
- [k5_provider_modes_v1.json](../specs/k5_provider_modes_v1.json) definiert vier technische Provider-Adapter-Konfigurationen (lokal, ChatGPT-Plan, Cloud-Broker und Self-Hosted als Cloud-Untermodus) für **drei nutzerseitige Betriebsarten**. Die App zeigt ausschließlich tatsächlich verfügbare Optionen.
- [K5_MODELLVERGLEICH_TESTPLAN.md](K5_MODELLVERGLEICH_TESTPLAN.md) legt reproduzierbare, K1–K3-kompatible Geräte-, Qualitäts-, Sicherheits- und Kostenbenchmarks fest.

**Modulgrenze:** Android-UI, Chat-, Coaching- und Replay-Domain sprechen eine InferenceGateway-Abstraktion an. Fähigkeit und Qualität werden pro Operation und Modus getrennt geprüft. Bei zu schwachem lokalen Coaching ist keine unüberprüfte Analyse zulässig. Ein stiller Wechsel von lokaler Inferenz zu externer Übertragung oder von ChatGPT-Kontingent zu bezahlter API ist **verboten**.

**MVP-Realismus:** Kompatibilität des nativen Android-OAuth-Flows mit den veröffentlichten OpenAI-Vorgaben ist **noch nicht bestätigt**; die ChatGPT-Option bleibt deaktiviert, bis die Berechtigung geprüft ist. Ebenso hängt echte Offline-KI von Gerät, Modellinstallation, Lizenz und Qualitätsprüfung ab. Es ist **nicht** vorgesehen, sofort drei Runtimes produktiv zu betreiben.

## 1. Verantwortlichkeiten und logische Schichten

```text
Android (app-spezifischer Build unter apps/CommunicationTrainer/)
  Compose Screens und Navigation (K4)
       │ UI State / Events / Lifecycle
  ViewModels und optionale Domain UseCases
       │
  Repositories: Chat, Session, Persona, Scenario, Draft, Feedback, Memory, Profile
       ├─ Room: Chats / Messages / Branches / Drafts / lokal zulässige Beobachtungen
       ├─ DataStore: kleine Präferenzen und Opt-in-Entscheidungen
       ├─ verschlüsselte sichere Token-/Schlüsselablage (falls benötigt)
       └─ NetworkClient: HTTPS, kurzlebige Credentials, Request-IDs
                         │
                         ▼
  Backend-Broker (kein Client-Schlüssel des KI-Providers)
       Auth/Guest Enforcement + Rate Limits + Budgets + Abuse Control
       Consent/Scope Check + Data Minimization + Redaction Policy
       Persona/Scenario Versioning + Message Ordering + Idempotency
       Safety and Boundaries + Model Adapter(s) + Schema Validation
       Optional Feedback Pipeline (K2) + minimal audit / deletion jobs
                         │
                         ▼
  Externer KI-Provider mit geprüftem Datenverarbeitungsvertrag
```

**Klarer Grundsatz:** Die App soll fachlich nicht vom Provider-API-Format abhängen. Domänenobjekte, K1-Skill-IDs, K3-Persona-IDs, Fehlercodes und zustandsbezogene Entscheidungen existieren unabhängig von einem Modell.

### 1.1 Android-Seite

Empfohlene technische Basis:
- Kotlin und Jetpack Compose als moderne Android-UI; **Single Activity** mit Navigation-Compose oder funktionsgleicher Navigation.
- UI/Domain/Data-Schichten und unidirektionaler Datenfluss; `ViewModel` mit StateFlow; `SavedStateHandle` für kleine Wiederherstellungsmarker, **keine** alleinige Persistenz von Chattext.
- `Room` für strukturierte Daten mit Fremdschlüsseln, Transaktionen und Migrationstests; `DataStore` für kleine Einstellungen, **nicht** für Chatverläufe.
- Kotlin Coroutines/Flow; Dispatcher und Timeouts explizit; UI darf während Providerantworten nicht blockieren.
- `WorkManager` für klar definierte wiederholbare Hintergrundoperationen wie bestätigte Lösch-/Synchronisationsjobs. Kein blindes Autoretry, das doppelte Modellkosten erzeugt.
- Mehrere Gradle-Module nur dann, wenn Wartung oder isolierte Tests davon profitieren; eine klar geschichtete einzelne App ist für frühe Umsetzung vertretbar. **Keine Eingriffe in Root-Gradle und fremde Apps.**
- Manifestberechtigungen minimal. Keine Kontakte, Standort, Mikrofon, Kamera oder Benachrichtigungen für das Text-MVP erforderlich.

Android-Grundlagen:
- https://developer.android.com/topic/architecture/recommendations
- https://developer.android.com/topic/architecture/data-layer/offline-first
- https://developer.android.com/topic/libraries/architecture/datastore/

### 1.2 Backend-Seite

**Minimale logische Dienste**, die anfangs in *einem* deploybaren Dienst laufen dürfen:
1. **Access:** Authentisierung, Berechtigung für Chat/Persona/Session, nicht rate-limit-freie Gastidentität.
2. **Conversation:** Servervalidierung von message_id, turn_id, `persona_version`, Szenario und Branch.
3. **ProviderAdapter:** austauschbare Modellanbindung inkl. Preis-/Token-/Timeout-Beobachtung.
4. **SimulationPolicy:** sichere Rollen- und Kontexttrennung, K3-Kanon, State-/Memory-Gates.
5. **CoachPolicy:** K2-Belege, Kontext, Feedback-Validierung, Grenzvorrang, Einwilligung.
6. **Usage/Billing:** pro Nutzer/Session budgetierte, deduplizierte KI-Aufrufe.
7. **DataGovernance:** minimal notwendige Aufbewahrung, Opt-in-/Lösch-/Export-Jobs, Provideranfragen.
8. **Moderation/Reporting:** eingehende oder erzeugte kritische Inhalte erkennen, Nutzerberichte entgegennehmen, Abuse sicher bearbeiten.

**Wichtige Designentscheidung:** Keine frühzeitige Mikroservice-Architektur ohne Skalierungsbeleg. Kernfunktionalität kann mit einem kleinen Backend und getrennten logischen Komponenten beginnen; Anbieter und Hosting stehen noch nicht fest.

## 2. Lokale Daten und Source of Truth

### 2.1 Modellskizze

| Entität | Schlüssel / Referenzen | Persistenzwunsch | Eigentümer |
|---|---|---|---|
| `Character` | `character_id, persona_version` | fünf kuratierte Profile + eigene Charakterversionen | App/Benutzer |
| `Scenario` | `scenario_id, version` | kuratierte K3-Szenarien, versioniert | App |
| `Conversation` | `conversation_id, mode, persona_ref, origin` | lokal nach Nutzerwahl | Benutzer |
| `Message` | `message_id, conversation_id, branch_id, client_created_at, status` | lokal in Transaktion, Rollenpflicht | Benutzer |
| `Draft` | `conversation_id, branch_id, text, edit_revision` | lokal vor Sendung und bei Navigation | Benutzer |
| `ReplayBranch` | `branch_id, parent_id, fork_message_id, snapshot_ref` | original unveränderlich | Benutzer |
| `SessionState` | `state_revision, character_version, scenario_version` | sitzungsbezogen, revisioniert | Engine |
| `AllowedMemory` | `memory_id, character_id, source_ids, consent_version` | nur mit spezieller Berechtigung | Benutzer |
| `CoachObservation` | `observation_id, evidence_refs, quality_state` | optionaler Opt-in | Benutzer |
| `UsageRecord` | `request_id, tokens, cost_bucket, status` | serverseitig minimal / TTL | Betreiber |

**Rollen:** `user`, `character`, `system_marker`, `coach` nicht in einer untypisierten Nachricht vermischen. Ein Coachtext ist nie eine Nachricht des KI-Charakters.

### 2.2 Entwürfe und Wiederaufnahme

**Verbindliche Produktanforderung aus K4:**
- Eingaben werden vor dem ersten Netzwerkaufruf lokal **dauerhaft** gespeichert; beim Verlassen der App bleiben sie mit Chat-ID verknüpft.
- Chat- und Persona-Version bleiben bei Rotation, App-Prozessneustart und Tabwechsel identisch.
- Sendung erfolgt mit eindeutiger `client_message_id` und `request_id`; UI zeigt `draft` → `queued` → `sent` → `responding` → `complete` bzw. `failed`.
- Bei unbekanntem Serverergebnis erst Requeststatus abfragen, bevor Generierung erneut gestartet wird; **Doppelabrechnung** und doppelte Antworten verhindern.
- Ein nur teilweise gestreamter KI-Text ist als `partial` erkennbar und wird nicht als bestätigte fertige Nachricht behandelt.
- Bei Prozessabbruch: lokale Datenbank bzw. Checkpoint rekonstruieren, nicht nur ViewModel-Zustand.

**Wichtig:** Ein lokaler Cache kann keine serverseitig noch laufende Generierung rückgängig machen. Cancellation, Statusabfrage und Abrechnungslogik müssen unabhängig konzipiert werden.

### 2.3 Offlinefähigkeit

**Sicher als MVP-Ziel:** kuratierte Artikel, bereits gespeicherte Chats und lokale Entwürfe öffnen und bearbeiten, verfügbare Charakterkarten ansehen, Einstellungen prüfen.

**Nicht als Offline-Funktion versprechen:** neue KI-Antworten, Live-Coach und neue KI-Varianten, solange sie einen Server benötigen.

**Sendewarteschlange:** Im MVP lieber ein klarer Zustand „Nicht gesendet – Netzwerk erforderlich“ mit manueller Wiederholung statt unbemerkt private Nachrichten Stunden später automatisch an KI-Provider zu schicken. Späterer Auto-Sync nur nach bewusst geprüfter UX/Einwilligung.

## 3. Gesprächs- und Coaching-Flows

### 3.1 Chatturn

1. UI speichert Entwurf als Nachricht mit Status und `request_id`.
2. Lokale Repositoryschicht prüft Sessionversion, Branch, Zustand und bekannte Sendeanfragen.
3. Broker prüft Auth, Besitz, Budget, Rate Limit, Modus, Profilschema, Nutzereingabe und Consent.
4. Broker erstellt *serverkontrollierte* Gesprächskontext-Nachrichten; Nutzertext bleibt untrusted data. Spezifische Persona-, Szenario- und Sicherheitsregeln dürfen nicht vom Client durch freie Systemprompts ersetzt werden.
5. K3-Simulation wählt logische Gesprächshandlungen und verfasst Antwort mit minimiertem Kontext.
6. Format-/Kanon-/Boundary-/Safety-Validator prüft den Entwurf; gescheiterte Inhalte werden nur begrenzt neu generiert bzw. sauber abgebrochen.
7. Idempotente Commit-Operation ordnet Ergebnis einer Nachricht und State-Revision zu; lokale App verarbeitet bestätigtes Ergebnis.
8. In freiem Chat **keine** automatische K2-Auswertung.

### 3.2 Coaching

K2-Auswertung nur per ausdrücklich gewähltem Modus:
- Input nur erlaubte Nachrichtenausschnitte, K1-Unterkompetenzen und deklarierter Szenariokontext.
- **Keine** geheimen Emotionen oder Bindungswerte als Beweis.
- Beobachtungs-ID und originale Beleg-IDs werden maschinell geprüft, keine halluzinierte Textstelle.
- Ergebnisklassen: `STRENGTH_ONLY`, `STRENGTH_WITH_OPTION`, `REFLECTION`, `NOT_ASSESSABLE`, `BOUNDARY_NOTICE`, `SAFETY_HANDOFF`.
- Nutzer kann widerrufen, ausblenden oder alternative Absicht melden; strittige Beobachtung ist bis Klärung kein Lernprofilbeleg.

### 3.3 Replay

`ReplayBranch` friert Chatstand, Persona-/Szenarioversion, Ausgangs-State und ggf. Seed ein. Neuer Zweig überschreibt Original **niemals**; Varianten sind mögliche simulierte Entwicklungen, nicht eine Vorhersage realer Menschen. Kosten und Begrenzung eigener Replay-Aufrufe gehören zum Nutzungsbudget.

## 4. Provider-Auswahl: harte Qualitäts- und Vertragstore

Es ist zu früh, ein bestimmtes KI-Modell als Sieger festzuschreiben. Prüfkriterien:

| Dimension | Praktische Entscheidungsvoraussetzung |
|---|---|
| Deutscher Dialog | natürlich, eigenständig, kurz/langlebig, keine ständigen Fragen |
| Persona-Konsistenz | K3-Fälle, 30/100+ Turns, Grenzen, Memory |
| Coach-Qualität | K2-Kontraste, Grounding, Abstention, Grenzschutz |
| Strukturierte Ausgabe | maschinell validierbar, Versionen/IDs |
| Latenz | real auf Android über Mobilfunk und bei Streaming |
| Kosten | Eingabe-/Ausgabe-/Cachingpreise, Retry- und Moderationskosten |
| Datenschutz | tatsächliche Speicherung, Trainingseinstellung, Subunternehmer, Region, Vertragsunterlagen |
| Sicherheit | Modellverhalten, Abuse-Regeln, Report-/Blockmechanismus |
| Verfügbarkeit | Rate Limits, Ausfälle, Anbieterwechsel möglich |
| Governance | Lösch- und Zugriffsprozesse, DPA, Drittlandtransfer, nachvollziehbare Version |

**Austauschbarkeit** ist nicht gleich „ein Prompt funktioniert bei allen Modellen“. Adapter und Eval-Daten können Anbieterwechsel vereinfachen; Prompting und Stilparameter müssen neu kalibriert werden.

### 4.1 Kontextfenster und Kosten

- Kurze feste Persona-/Szenario-Kanonkarte statt gesamter Charakterbibliothek.
- Sichtbaren relevanten Chatverlauf begrenzen; Langzeitsummen nur mit K3-Memory-Rechten und Quellen.
- Zusammenfassungen besitzen Quellen-IDs und werden bei Löschung invalidiert.
- Kontextkürzung darf keine expliziten Grenzen, wichtige Sicherheitsinformationen oder aktuelle Sprecherrollen verlieren.
- Optional Prompt-Caching nur, wenn Providerverhalten und Datenschutz geprüft sind; **keine verdeckte persistente Chatablage**.

## 5. Vertrauensgrenzen und Datenfluss

```text
USER DEVICE --(HTTPS mit minimalem freigegebenem Kontext)--> BROKER
                [User Auth + Scope / Budget / Consent]
                         │
                         ├─ request_id und begrenzte Nutzungsmetadaten
                         │
                         ▼
                 AI PROVIDER (Vertrag/Region/Retention prüfen)
                         │
                     Ergebnis
                         ▼
                POLICY/SCHEMA VALIDATION
                         ▼
                 ANDROID LOCAL REPOSITORY
```

Das Endgerät schützt lokale Daten durch Android-App-Sandbox und Geräteschutz. Zusätzliche Ende-zu-Ende-Schutzbehauptungen sind unzulässig, wenn der Broker oder Provider Chattext zur Inferenz im Klartext verarbeitet.

**Hard Requirement:** Nie Betreiber-Provider-Secrets ins APK, in App-Assets, öffentliches Git, `BuildConfig` oder Crashlogs. Alle Third-Party-Aufrufe werden hinter geschützten Server-Credentials vermittelt. Serverrate-/Budgetlimits müssen ohne Vertrauen in einen clientseitigen Zähler funktionieren.

## 6. Modulverantwortung und Repository-Grenzen

Anfänglicher Vorschlag nach K6:
```text
apps/CommunicationTrainer/
  README.md
  docs/                    # K1–K6, ADRs, Tests und Entscheidungen
  specs/                   # Persona/Scenario, API- und Datenverträge
  android/                 # eigene Android-Gradle-Wurzel, unabhängig vom Repo-Root
    app/src/main/...       # UI, Navigation, DI, Repositories
    app/src/test/...       # Kotlin Unit- und Vertragsprüfungen
    app/src/androidTest/...# Compose UI / Lifecycle / TalkBack-Smoke
  backend/                 # eigener minimaler Broker, Tests, Konfiguration
  ci/                      # app-spezifische Skripte
```

**Noch nicht angelegt:** Das ist eine geplante Pfadstruktur, kein vorhandener Code. Die tatsächliche Gradle-Integration darf keinen anderen App-Build brechen. Ein separater app-spezifischer Workflow wird erst nach Prüfung des Repo-Zustands eingeführt.

## 7. Testbarkeit, Observability und Versionierung

Jede generierte Antwort sollte nachvollziehbar zugeordnet werden können zu:
- `request_id`, Session-/Branch-/Message-IDs (pseudonymisierte Schlüssel; keine Rohtexte in Standardlogs);
- `persona_ref`, `scenario_ref`, `policy_version`, `model_id`;
- `input_tokens`, `output_tokens`, Retry/Timeout, Validierungsfehler;
- verwendeter Consent-/Policy-Version, ohne unnötige identifizierende Daten.

**Nicht in Standardtelemetrie:** rohe Chats, Nutzererinnerungen, reflexive Selbstangaben, sensible Freitexte, vollständige Prompts, Providerantworten, Access-Token und feste Gerätekennungen.

Debug-Logs mit echten Chatinhalten nur in explizit kontrollierter, rechtlich geprüfter Testumgebung, nicht als Produktstandard. Versionsmigration muss auch die Möglichkeit beinhalten, dass frühere Beobachtungen nach einer K2-Rubrikänderung neu bewertet oder nicht weiter verwendet werden.

## 8. Offene Architekturentscheidungen vor Code

- Welcher Android `minSdk`, Compose-/Kotlin-/Gradle-Versionssatz passt zur Isolation von anderen Repo-Apps?
- Welcher Backend-Stack ist für das kleine Team sicher betreibbar?
- Ist beschränkter Gastzugang mit serverseitiger Identität technisch/ökonomisch verantwortbar?
- Werden Chats ausschließlich lokal oder auch synchronisiert gespeichert? Wo lebt der authoritative Memory-Store?
- Wie wird optional lokal sensible Room-Datenverschlüsselung inklusive Key-Backup/Wiederherstellung gestaltet?
- Welche Limits und Ablaufzeiten gelten für Providerkontext, Logs, Löschaufträge und Backups?
- Welcher Provider erfüllt die K2/K3-Qualität und vertraglichen Anforderungen?
- Welche Transparenz-/Reporting-/Moderationsfunktionen müssen vor Beta verpflichtend ausgeliefert werden?
- Kann die erste Beta ohne langfristige Memory und ohne Cloud-Synchronisation starten, um Risiko und Aufwand zu reduzieren?

## 9. Umsetzungsvorschlag nach K6

> **K6-Scope-Präzisierung (07.11.2026):** Die bisherige P0–P4-Skizze wird für konkrete PRs vom [K6-Fahrplan D00–D12](K6_UMSETZUNGSPLAN.md) operationalisiert. D00/D01 prüfen zunächst Repo- und Android-Build-Isolation; D02/D03 testen Entwurfspersistenz und Mock-Chat **vor echten KI-Aufrufen**. Das [K6-MVP](K6_MVP_UMFANG.md) plant nur **einen** nach echten K2/K3-/Kosten-/Privacy-Prüfungen freigegebenen Inferenzstandard. Weder die lokale noch die ChatGPT-Plan-Anbindung sind automatisch verfügbar.



**P0** – testbarer isolierter Android-Skeleton-Build, lokale Modelle/DB, Navigation, Zustandserhalt und Mock-Broker.  
**P1** – Backend Auth/Budget/Idempotency, eine Providerintegration, basic Safety, stabiler Chatfluss.  
**P2** – K3 Persona-/Szenario-Runner, minimaler K2-Coach mit Grounding-Gates, UI-Feedback.  
**P3** – Datenkontrolle, Report/Block, Retention/Löschung, Android/Server-Sicherheit und Datenschutztexte.  
**P4** – Pilot-Evals, Rate-/Kosten-/Qualitäts-Baselines, begrenzte Beta; nur nach bestandenen Release-Gates.

**Abnahme K5:** Nur eine überprüfbare, abgewogene Architekturentscheidung ist hier geliefert – keine fertige Backend-Schnittstelle oder abgenommene App.
