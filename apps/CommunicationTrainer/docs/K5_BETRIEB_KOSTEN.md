# K5 – Betrieb, KI-Kosten, Ausfallsicherheit und Geschäftsmodell

**Version:** 1.0 · **Datum:** 07.11.2026 · **Status:** kalkulatorisches Konzept, keine verifizierten Modellpreise und kein laufender Produktionsbetrieb  
**Grundlage:** [Architektur](K5_TECHNISCHE_ARCHITEKTUR.md) · [Privacy & Safety](K5_DATENSCHUTZ_SICHERHEIT.md) · [Qualität](K5_TESTPLAN.md)

## 0. Betriebsziel

Die App soll unter realistischen Kosten und begrenztem Wartungsaufwand zuverlässige Gespräche bieten, ohne Qualität, Nutzervertrauen oder Datenschutz zu opfern. Das Betriebsmodell folgt dem K1–K4-Grundsatz: **nützliches Lernen statt erzwungener Gesprächszeit**.

**Empfehlung:** kleine kontrollierte Beta mit Kostenobergrenze, ein austauschbarer KI-Provider, ein Broker, konservative Nachricht-/Feedbackbudgets, kein unbegrenzter Gratiszugang, keine unkontrollierte Kontextakkumulation.

## 1. Kosten entstehen pro generiertem Turn – nicht pro App-Installation

Wesentliche Kostenblöcke:

| Block | Kostentreiber | Steuerungsmaßnahme |
|---|---|---|
| Charakterchat | Eingangstokens + Ausgangstokens pro Modellantwort | kurze Kontextauswahl, moderierte Ausgabelängen, passende Modellklasse |
| K2-Auswertung | zusätzlicher Modelldurchlauf / Validatoren | nur auf Nutzerwunsch, begrenzte Belege, wenige Hinweise |
| Replay | alternative KI-Fortsetzungen | als neuer budgetierter Aufruf, keine unendliche Gratis-Regeneration |
| Moderation | Klassifikation/Review/Meldungen | risikobasierte Vor-/Nachprüfung und klare Eskalation |
| Memory/Zusammenfassung | Auswahl, Revision, Speicher | auf konkreten Bedarf und Berechtigung begrenzen |
| Backend | Hosting, Datenbank, Egress, Monitoring, Secrets | schlanker Dienst, minimale Logs und Autoscaling nur nach Bedarf |
| Android-Betrieb | CI-Runner, Builds, Tests, Play-Gebühren, Signierung | getrennter Workflow, Cache, Releasekontrollen |
| Menschliche Arbeit | Moderation, Prompt-/Produktpflege, Support | nicht als „kostenlos“ behandeln |

### 1.1 Kalkulationsgleichungen

Mit `P_in` und `P_out` als Anbieterpreis **pro 1 Mio. Tokens** (Währung konsistent) gilt:

```text
C_chat_turn = (T_in / 1,000,000) * P_in
            + (T_out / 1,000,000) * P_out
            + C_policy_and_validation
C_session = sum(C_chat_turn) + C_feedback + C_replay + C_summaries
C_month = sessions * average(C_session)
        + server + storage + egress + logs + moderation + support + taxes_and_fees
```

**Echte Preise, Servicegebühren, Wechselkurse und Modellkosten werden erst zum Entscheidungszeitpunkt aus offiziellen Anbieterangeboten eingetragen.** Dieses Dokument behauptet keine realen Marktpreise für November 2026.

### 1.2 Beispielrechnung – explizit hypothetische Testwerte

**Nur eine Sensitivitätsrechnung, kein Preisangebot:**
- 40 generierte KI-Antworten je Session;
- gemittelt 1.200 Eingabetokens und 160 Ausgabetokens pro Antwort;
- frei angenommene `P_in = 0,80` und `P_out = 3,20` Währungseinheiten je 1 Mio. Tokens;
- zusätzlicher Coach-Aufruf mit 3.000 Eingabe- und 400 Ausgabetokens;
- 1.000 solcher Sessions im Monat.

```text
Chat-Token: 40 * 1,200 = 48,000 in; 40 * 160 = 6,400 out
Hypothetischer Chatbetrag: 0.03840 + 0.02048 = 0.05888 Einheiten
Hypothetischer Coachbetrag: 0.00240 + 0.00128 = 0.00368 Einheiten
Zusammen: 0.06256 Einheiten pro Session,
          62.56 Einheiten bei 1,000 Sessions
          PLUS Hosting, Moderation, Retries, Speicher, Support, Steuern etc.
```

**Empfindlichkeit:** Doppelte durchschnittliche Eingabelänge verdoppelt nicht automatisch alle Kosten, erhöht aber den Inputanteil. Längere Gespräche mit immer wieder kompletter Historie können deutlich stärker wachsen als linear. Caching, Summarisierung und Providerpreise sind erst nach konkreter Messung einzuplanen.

### 1.3 Drei realistische Last-Profile zum Messen

| Profil | Beschreibung | Zu messen |
|---|---|---|
| Light | wenige Chats, kurze Übung, kaum Coach | Kosten pro aktivem Nutzer und Erstnutzer |
| Typical | wiederholtes Üben, einige freie Chats, sporadischer Coach | reale Turn- und Context-Tokenverteilung |
| Heavy | lange Chats, viele Replays, häufiges Feedback | Budgetspitzen, Abuse-Risiko, Frust bei Limits |

Nicht nur Durchschnittswerte betrachten. **p95/p99** für Turndauer, Tokenverbrauch und Kosten pro aktivem Account/Session beobachten. Volltexte brauchen dafür nicht geloggt zu werden.

### 1.4 Betriebsarten statt bloß API-Tarifvergleich

Der spätere Kostentest berücksichtigt nun ausdrücklich vier technische Inferenzvarianten in **drei nutzerseitigen Modi** (Details: [KI-Betriebsarten](K5_KI_BETRIEBSARTEN.md), [Modellvergleich](K5_MODELLVERGLEICH_TESTPLAN.md)):

- **Lokal:** keine extern berechneten Inferenz-Tokens pro lokalem Turn; jedoch Akku, Geräte-RAM, Downloads, Modellvertrieb, QA und Support.
- **ChatGPT-Plan bei berechtigter Nutzung:** mögliche Kontingentnutzung statt normaler Betreiber-API-Rechnung, jedoch geteilte Limits und OAuth-/Zulassungsaufwand. **Nicht** mit unbegrenzter kostenloser API verwechseln.
- **Managed Cloud API:** Modell-/Tokengebühren, Broker, Moderation, Quotas und Support.
- **Self-Hosted Open-Weight auf Cloud/GPU-Server:** keine notwendige fremde Inferenz-Tarifgebühr je Token, aber reale GPU-/Host-/Strom-/Betriebs-/Supportkosten; Nutzungsauslastung entscheidet den Break-even.

**Wirtschaftlicher Prüfpunkt:** Durchschnittskosten allein reichen nicht. K2-Coachqualität, K3-Personatreue und niedrige Gerätelatenz sind Qualitäts-Gates, bevor der günstigste Modus gewählt wird. Selbsthosting ist bei kleiner Nutzerzahl **nicht automatisch** billiger als eine Managed API. Kein konkreter Break-even oder OpenAI-Plan-Vorteil wird ohne gemessene Last und ausdrückliche Berechtigung versprochen.

## 2. Kosten- und Missbrauchsschutz

### 2.1 Budget-Gates
- globales Tages-/Monatsbudget im Backend; Anbieter-Limits als zweite Schranke;
- konto-/gastbezogene begrenzte Budgets und wiederverwendbare Session-/Request-IDs;
- Limits pro Zeitfenster, gleichzeitigen Generierungen, Antwortlänge und aktiven Sessions;
- getrennte, nachvollziehbare Kontingente für Chat, Coach und Replay;
- Hard Cutoff und sichere Nutzererklärung bei erschöpftem Budget (kein stilles Paywall-Switching);
- providerseitige Abrechnungsdaten mit internem Usage-Ledger plausibilisieren;
- Kostenprüfungen **vor** Aufruf; retry/timeout dürfen nicht unbegrenzt neue kostenpflichtige Aufrufe auslösen.

**Schutzproblem bei Gastnutzung:** Anonyme Identität ist auf mobilen Geräten missbrauchsanfällig. Kein unbegrenzter „kostenloser Gastzugang“ ohne Limit, verifizierte Schutzmaßnahmen und Budget. Google Play Integrity kann ergänzen, ist aber kein unfehlbarer Identitätsnachweis und ersetzt serverseitige Berechtigung nicht: https://developer.android.com/google/play/integrity/standard .

### 2.2 Modellrouting

Mögliche Kostenoptimierung, **erst nach Tests**:
- einfacher textbasierter Smalltalk → effizientes Modell, wenn persona-stabil;
- anspruchsvolle Konfliktszene → geeigneteres Modell bei Bedarf;
- Coach mit besonders hohen Grounding-Anforderungen → geprüftes Modell plus Validator;
- einfache Zustandsgrenzen/ID-Prüfungen → regelbasiert, kein LLM nötig.

Nicht nur nach billigstem Preis routen. K2-Kritik, Datenschutz, Grenzschutz und Persona-Konsistenz müssen mindestens die menschlich festgelegten Qualitätsgates erreichen. Modellwechsel müssen versioniert, testbar und ggf. rollbackfähig sein.

## 3. Kapazität und Latenz

### 3.1 Zu messende Größen
- p50/p95 Antwortzeit pro Turn und je Provider;
- Time to first meaningful content bei Streaming, ohne künstliche Mensch-Tippanimation;
- Quote `request_failed`, `provider_timeout`, `schema_invalid`, `grounding_rejected`;
- Duplikate nach Timeout oder Retries;
- Anzahl nicht abgeschlossener Löschaufträge und deren Dauer;
- Kosten je Turn, je Session, je Lernmodus und pro zahlendem/aktiven Nutzer;
- tatsächlich gemeldete problematische Antworten pro Nutzungsumfang;
- freiwillige Hilfreichkeits- und UX-Rückmeldung statt bloß Nachrichtenzahl.

**Verbindliche Teststrategie:** vor verbindlichen SLO-Zahlen auf echten Geräten, Mobilfunkbedingungen und geeigneter Beta-Infrastruktur messen. Für sicherheitskritische Bereiche (Idempotenz/IDOR/Löschung) gibt es unabhängig davon Blocker-Gates.

### 3.2 Fehlerbudget & Circuit Breaker
- Timeouts für Connect, Serverbearbeitung und Modellgenerierung getrennt setzen.
- Bei wiederholten Provider-Fehlern Aufrufe temporär stoppen (Circuit Breaker).
- Bei unklarer Request-Endlage Statusabfrage statt blinder Zweitanfrage.
- Kein anderer Anbieter erhält automatisch private Chats als Fallback, **bevor** Datenfreigabe, Vertrag und Offenlegung geprüft sind.
- Bei Brokerfehler kann die UI gespeicherte Chats zeigen und lokale Entwürfe erhalten; keine erfundenen KI-Antworten.
- Ein teurer Retry darf nur einmal budgetiert/entschieden werden, nicht über parallele Client/Server-Schichten.

## 4. Datenschutzfreundliche Observability

Standardmäßig nur:
- pseudonyme Request-ID, Fehler- und Statuscodes;
- Provider-/Prompt-/Persona-/Rubrikversion;
- tokenbezogene Summen und geschätzte Rechnungskosten;
- technische Dauer und Anzahl generierter Versuche;
- durch Sicherheitsprüfung freigegebene Report-/Abuse-Ereignisse ohne unnötigen Klartext.

**Nicht standardmäßig:** Chat-Rohdaten, Promptvolltexte, vertrauliche Notizen, private Namen, Gesundheitsangaben, Zugangstokens oder Screenshots.

Alerting auf Kostenexplosion, Sicherheitsfehler, verdächtige Fehlerraten, ungelöschte Daten, 5xx/429-Fehler und Provider-Ausfälle. Produktions-Rollback und Key-Rotation müssen ohne neue APK möglich sein.

## 5. Hosting, Provider und Betriebsträger

Vor Wahl:
- Anbieterregion / tatsächlicher Verarbeitungsort / Subprozessoren;
- Vertrag und Datenschutzrollen inklusive Unterauftragnehmer;
- Eingabe-/Ausgabelimits und Preisänderungsklauseln;
- Logging/Retention/Training und Opt-out-Fähigkeiten;
- Revisions-/Exportmöglichkeiten;
- Ausfall-/Wartungsverhalten;
- Authentisierung, Secret Management, Backups, Wiederherstellbarkeit;
- Betriebserfahrung des Teams und jährliche Grundkosten.

**Weder bestimmte Cloud noch KI-Provider ist genehmigt.** Ein expliziter Provider-Entscheidungsdatensatz (ADR) folgt nach Proof-of-Concept und Kostenvergleich.

## 6. Geschäftsmodell: Qualität zuerst

Produktwert entsteht aus Lernnutzen und glaubwürdigen Charakteren, nicht aus künstlichem Bedarf, Nachrichten zu sammeln.

**Optionen für K6-Vergleich:**
1. **Begrenzter kostenloser Einstieg + transparentes Abo** für zusätzliche KI-Nutzung/Szenarien; Pflichtfunktionen wie Datenlöschung/Datenschutz niemals hinter Paywall.
2. **Monatliches Nutzungskontingent** mit klarer Info vor Erreichen; kein unsichtbarer Kostenüberlauf.
3. **Bezahlte Lernpakete** mit festen Übungen plus getrenntem KI-Verbrauch – in der UX vorsichtig, keine Verwirrung zwischen Lerninhalt und Tokenbudget.

Gegen eine zu frühe Monetarisierung: Markt, Zahlungsbereitschaft und Preispsychologie sind nicht validiert. Zuerst Qualitäten, reale Kosten und Pilotnutzen messen.

**Abgelehnt:** Pay-per-Emotion, künstliche Romanzen, erhöhte Vertrautheit als Premiumbelohnung, Zwangs-Streaks, manipulative Abo-Unterbrechung bei persönlichen Gesprächen.

## 7. Release- und CI-Betrieb

**Getrennte Pfade:** `apps/CommunicationTrainer/android`, `backend`, `specs`, `docs`. Repo-Root und andere App-Workflows nicht anfassen.

Vorgeschlagene Prüfstufen:
- PR-Gate: Markdown-/JSON-Links, Schemas, Security Secret Scanning, isolierte Android-Buildtests;
- Backend-Gate: Schema, Auth, Idempotency, Datenschutz/Scope, Testprovider-Mocks, Rate/Budget;
- Android-Gate: Unit-, UI-/Navigation-, Lifecycle-/Entwurfs-, Offline- und Emulatorchecks;
- E2E-Sandbox: Fake Provider für deterministische Tests plus kontrollierte echte KI-Evals;
- Release: signierte APK/AAB, reproduzierbare Version, Changelog, Store-Richtlinien/Datenschutz und Freigabecheckliste;
- Sicherheitsfreigabe: keine kritischen bekannten Fehler; Monitoring/Abschaltmöglichkeit vorhanden.

Secrets gehören in geschützte CI-/Cloud-Secrets, nicht als Klartext in Builds oder Repository-Files. Abhängigkeiten/SDKs vor Übernahme prüfen; Rollback-Strategie und Store-Zertifikate dokumentieren.

## 8. Zuständigkeiten und Runbooks

Für einen echten Produktbetrieb muss mindestens eine verantwortliche Person zugeordnet werden für:
- Releases, Rollbacks, CI/Signing und Android-Kompatibilität;
- API/Broker-/Providerrechnung, Quotas und Warnschwellen;
- Sicherheitsmeldungen, Missbrauch und Nutzerreports;
- Datenschutzanfragen, Löschung, DPA und Providerkontakt;
- K1/K2/K3-Qualitätsregressionen und Nutzerfeedback;
- Störungsmeldungen/Kommunikation bei Nichtverfügbarkeit.

**Runbooks vor Pilot:** Provider-Ausfall, Budget aufgebraucht, API-Key kompromittiert, Datenleckverdacht, fehlerhafte Löschung, Content-Report, fehlerhaftes K2-Feedback, Missbrauchsangriff. Das Unternehmen/Team und Kontaktwege sind noch nicht festgelegt.

## 9. Betriebsreife und Kosten-Gates

**P0 – vor Provider-Integration:** Schlüssel nicht in Android, Missbrauchslimits am Broker, pseudonyme Kostenmessung, Retry-Idempotenz.
**P1 – vor geschlossener Beta:** verifizierte Providerrechnung, harte Budgets, kostenbegrenzter Testplan, Ausfall-/Rollbacklauf, In-App-Reports, Datenschutz-/DPA-Gates.
**P2 – vor öffentlichem Release:** reale Lastprofile, belastbares Unit-Economics-Modell, Play-Policy-Gates, Support-/Meldeprozess, Incident-Handbuch, transparente Kontingente.

**Nicht erfüllt durch ein Dokument:** echte Betriebsreife, tatsächliche Preisgarantien, Launch-Freigabe oder wirtschaftlicher Erfolg.

## 10. Noch offene Entscheidungen für K6

1. Welche kostenlose Nutzung ist bei realen Modellkosten finanzierbar?
2. Können wir für die erste Beta mit begrenzter Teilnehmerzahl und Einladungs-/Accountzugang starten?
3. Brauchen wir Server-Chatpersistenz oder reichen lokale Chatdaten und temporärer Providerkontext?
4. Welche optionalen Funktionen werden für einen sauberen, wirtschaftlichen MVP verschoben (z. B. Cloud-Sync, volle Langzeit-Memory, viele Replay-Zweige)?
5. Welches Pilotbudget ist tatsächlich verfügbar? Ohne konkrete Budgetzusage nur symbolische Modellrechnung.
6. Welche monetären und UX-Folgen hat ein Sicherheits-/Privacy-bedingter Providerwechsel?

## 11. Quellen und Einordnung

- Android Offline-First-Strategien: https://developer.android.com/topic/architecture/data-layer/offline-first .
- Android Architektur und Schichtentrennung: https://developer.android.com/topic/architecture/recommendations .
- Google Play Integrity als ergänzender Abuse-Schutz: https://developer.android.com/google/play/integrity/standard .
- Play Policy zu KI-Inhalten und Meldungen: https://support.google.com/googleplay/android-developer/answer/13985936?hl=de .

**Wichtig:** Die Kostenwerte in Abschnitt 1 sind ausdrücklich **hypothetisch**, keine November-2026-Preise. K5 verpflichtet uns zu einem echten Anbietervergleich, bevor wir uns auf ein Geschäftsmodell und eine Preisstruktur festlegen.
