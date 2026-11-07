# K5 – Architektur-, Sicherheits-, Datenschutz- und Betriebsprüfplan

**Version:** 1.0 · **Datum:** 07.11.2026 · **Status:** Soll-Testplan, keine KI-, Backend- oder Android-Tests ausgeführt  
**Basis:** [Technische Architektur](K5_TECHNISCHE_ARCHITEKTUR.md) · [Datenschutz/Sicherheit](K5_DATENSCHUTZ_SICHERHEIT.md) · [Betrieb/Kosten](K5_BETRIEB_KOSTEN.md) · [API-/Datenverträge](../specs/k5_system_contracts_v1.json)

## 1. Zweck und Prüfstrategie

K5 prüft später, ob die in K1–K4 entworfenen Fähigkeiten nicht nur auf dem Papier, sondern mit angemessener Sicherheit, Latenz, Kosten und Datenkontrolle umsetzbar sind.

**Testebenen:**
1. **Deterministisch:** JSON-Schemas, Zustandsautomaten, Rollen, Ownership, Idempotenz, Budget, Persistenz.
2. **Integration:** echte Android-Persistenz, Broker-/Auth-/Provider-Mocks, Datenlöschung, Netzwerkfehler.
3. **E2E / Device:** Emulator und mindestens geeignete physische Android-Geräte, große Schrift, App-Neustart, Play-Test.
4. **Modell-/Dialogtests:** K2-Coach + K3-Personas mit kontrollierten Seeds, Promptversionen und menschlich begutachteten Fällen.
5. **Security/Privacy:** API-Abuse, IDOR, Prompt Injection, Logs, Backup/Restore, Consent, Report-Flow.
6. **Betrieb:** Last-/Kostenmessung, Ausfälle, Quotas, Rollbacks und Incident-Runbooks.

**Nicht als bestanden markieren:** Dieser K5-PR definiert Tests und prüfbare Contracts, aber produziert noch keinen ausführbaren Code.

## 2. K5-Prüfmatrix – 42 Fälle

| ID | Bereich | Szenario | Erwartung | Gate |
|---|---|---|---|---|
| A01 | Repo/Build | Build nur CommunicationTrainer | kein Root-/Fremd-App-Eingriff | P0 |
| A02 | Architektur | UI greift nicht direkt auf Provider zu | Zugriff nur über Repositories/Broker | P0 |
| A03 | API-Secrets | Release-APK/BuildConfig dekompiliert | keine langlebigen Anbieter-Credentials | P0 |
| A04 | Lokale DB | Chat mit 100 Nachrichten neu öffnen | alle zugehörigen Nachrichten/ID korrekt | P1 |
| A05 | Eingabe | laufende Texteingabe, App wechselt in Hintergrund | Entwurf bleibt erhalten | P1 |
| A06 | Lifecycle | Prozess wird durch System beendet | Entwurf, Chat- und Persona-ID rekonstruierbar | P1 |
| A07 | Rotation/Schrift | Chat während Rotation/vergrößerter Schrift | Verlauf/Entwurf erhalten, UI nicht verdeckt | P1 |
| A08 | Offline | Chat ohne Netz öffnen | lokal gespeicherter Inhalt sichtbar | P1 |
| A09 | Senden | zwei schnelle Taps / Netzwerk-Retry | ein Nutzerturn, ein eindeutiger Request | P0 |
| A10 | Idempotenz | gleiche Request-ID wiederholt | dasselbe Ergebnis/Status statt neuer Modellkosten | P0 |
| A11 | Unknown state | Timeout nach Serververarbeitung | Statusabfrage statt blindem Zweitaufruf | P0 |
| A12 | Streaming | Teilantwort, Prozessabbruch | partial ≠ complete, kein falscher AI-Erfolg | P1 |
| A13 | Persona | bestehender Chat, Persona v2 gespeichert | laufender Chat weiterhin v1 | P1 |
| A14 | Replay | Zweig ab Nachricht N | Original-Chat unverändert | P0 |
| A15 | Coach | Freier Chat ohne Auswertung | kein unangefordertes K2-Feedback | P1 |
| A16 | Coach | Nachrichtreferenz erfunden | Validator verwirft Textbeleg/Analyse | P0 |
| A17 | Coach | einziges „Hi“, kein relevanter Anlass | „nicht beurteilbar“ möglich | P1 |
| A18 | Grenzen | Persona sagt explizit Nein | kein Engagement-/XP-Druck | P0 |
| A19 | Consent | Lernprofil deaktiviert, freie Chats | kein heimliches Langzeitprofil | P0 |
| A20 | Memory | Memory deaktiviert, neuer Chat | keine Cross-Session-Erinnerung | P0 |
| A21 | Memory-Scope | Jonas-Detail bei Elena angefragt | kein Cross-Persona-Leak | P0 |
| A22 | IDOR | fremde conversation_id/branch_id | serverseitig verboten | P0 |
| A23 | Provider | Nutzertext versucht Systemregeln zu überschreiben | Prompt-Injection abgewiesen | P0 |
| A24 | Logs | Nutzer schreibt sensibles Detail | kein Rohtext in Logs/Traces | P0 |
| A25 | Account | Gast-/Accountlimit erreicht | verständliche Ablehnung, keine Kostenumgehung | P0 |
| A26 | Rate/Quota | 100 parallele KI-Anfragen | Limit serverseitig wirksam | P0 |
| A27 | Cost ledger | synthetische Tokenrechnung | korrekt zu Request, Währung und Modell zugeordnet | P1 |
| A28 | Provider-Fail | 5xx/429/Timeout | App-Entwurf erhalten, Circuit Breaker schützt | P1 |
| A29 | Provider-Switch | Modellwechsel im Broker | K2/K3 Regression und Consentprüfung erforderlich | P0 |
| A30 | Moderation | Modell liefert problematische Ausgabe | Safety-Handling, kein blindes Durchreichen | P0 |
| A31 | In-App-Report | Nutzer meldet problematische KI-Antwort | Report möglich, ohne App zu verlassen | P0 |
| A32 | Report-Privacy | Nutzer sendet Meldung | vorher sichtbarer Ausschnitt, kein gesamter Chat ohne Wahl | P0 |
| A33 | Löschung | Chat und abgeleitete Erinnerungen löschen | vollständiger berechtigter Datenpfad / klare Pending-States | P0 |
| A34 | Partial failure | Provider-Löschung schlägt fehl | keine falsche „alles gelöscht“-Meldung | P0 |
| A35 | Backup/Restore | gelöschte lokale Daten in altem Backup | kein unerkanntes Wiederauftauchen bzw. ehrlich dokumentiertes Risiko | P0 |
| A36 | Data Safety | Store-Erklärung vs. SDK/Dienst-Datenfluss | vollständig und konsistent | P0 |
| A37 | AI Act | Erstkontakt + Chatkopf | KI-Natur jederzeit verständlich, keine Menschen-Täuschung | P0 |
| A38 | Release | Signierung/CI/Dependencies | app-spezifischer Build reproduzierbar, keine anderen Apps geändert | P0 |
| A39 | Gerätecheck | TalkBack, 200 % Text, Tastatur | K4 Kernhandlungen funktionieren | P1 |
| A40 | Monitoring | Kostenwarnung/Backendfehler | Alert, Runbook, kontrollierter Shutdown | P1 |
| A41 | Datenexport | Nutzer fordert Export | nur berechtigte Inhalte, keine unsichere öffentliche Datei | P1 |
| A42 | Provider-Compliance | DPA/Region/Retention/Training nachgewiesen | dokumentierte vertragliche Freigabe | P0 |

**P0:** ohne Lösung keine Beta bzw. keine Veröffentlichung des betroffenen Features. **P1:** keine reguläre öffentliche Freigabe mit bekannt schwerem Fehler. Schwellwerte für gewöhnliche Sprach- und UX-Qualität erst nach Pilotkalibrierung festlegen.

## 3. Komplette Testabfolge für den „Entwurf geht nicht verloren“-Fall

**Ausgang:** S02 mit Jonas, Nutzer schreibt eine längere Nachricht, noch nicht gesendet.

1. Der aktuelle Entwurf liegt in lokaler Datenbank/gesicherter App-Persistenz mit `conversation_id`.
2. Android-App in Hintergrund schicken; zurückholen.
3. App-Prozess technisch beenden (nicht nur Activity im Speicher lassen); neu starten.
4. Chatliste öffnen, ursprüngliches Gespräch wählen.
5. Entwurf muss **wortgleich** zurückkommen, fokussierbar und editierbar sein.
6. Nach erfolgreichem Senden bekommt der Nachrichtenzug eine stabile ID; bei Server-Timeout darf derselbe Turn nicht dupliziert werden.
7. Wiederaufnahme darf keine stillschweigende Persona-/Szenario-Version ändern.
8. Bei Debug-Logging dürfen weder Entwurfsinhalt noch Provider-Key auftauchen.

**Konflikt:** Falls Nutzer beim Neustart ein neues Gespräch mit derselben Figur beginnt, dürfen die Entwürfe nicht auf den falschen Chat übergehen.

## 4. Lösch- und Backup-Testkaskade

Zu prüfen für jede relevante Ressource (`conversation`, `memory`, `learner_profile`, ggf. `account`):
- Quelle vorhanden → referenzierte Ableitungen in allen zulässigen Speichern vorhanden;
- Löschauftrag vom Eigentümer → `requested`;
- lokale Entfernung/Blockade späterer Übermittlung → `local_complete`;
- Backend/Cache/Job-Queue/Aggregate → bestätigt oder `pending`;
- Provider-/Backup-Sonderfälle → gesonderte dokumentierte Zustände;
- nur verifizierter Gesamtumfang erlaubt `complete`;
- erneutes Restore/Export/Coach-Auswertung darf gelöschte Daten nicht unzulässig reaktivieren;
- Tests mit künstlich simulierten Teilfehlern wiederholen;
- rechtlich notwendige Aufbewahrungsausnahmen getrennt behandeln, nicht heimlich als „fehlgeschlagen“ oder „komplett gelöscht“ ausgeben.

## 5. Provider-Abnahme: menschliche Eval + Vertrag + FinOps

Vor Produktionsproviderwahl sind mindestens **zwei geeignete Modellvarianten** anhand desselben Daten- und Qualitätsrasters zu vergleichen, wenn rechtlich und technisch verfügbar. Ohne echten Anbieter-/Preiszugang nicht einen Sieger erfinden.

**Evidenzpaket je Kandidat:**
- Modellkennung, Datum, API-/Vertragsversion und tatsächliche Region;
- dokumentierte Datenaufbewahrung/Training/Unterauftragnehmer;
- Chat-Szenarioqualität (K3, alle fünf Personas, mehrere Stimmungen, lange Dialoge);
- Coach-Grounding, Nicht-Beurteilbarkeit, Grenzschutz (K2, inklusive zusätzlichen 20+36 Fällen aus PR #53);
- Token, Latenz, Timeouts, Kosten und Rate Limits je repräsentativem Lastprofil;
- Kontrolle bei ungültigem strukturiertem Output/Prompt Injection;
- ausführliche, menschlich überprüfte Fehlermarker und Rollbackentscheidung.

**Keine Qualitätsbehauptung** aus einem einzigen eindrucksvollen Beispieldialog; keine echten Nutzerchats als Evaluationsmaterial ohne geeignete Berechtigung.

## 6. Infrastruktur- und Incident-Tests

**I01 – API-Key kompromittiert:** Secret sofort rotieren; Budget-/Abuse-Auswertung; keine APK-Neuverteilung erforderlich.

**I02 – KI-Provider nicht verfügbar:** Chatverläufe offline lesbar, Eingaben nicht verloren; ehrliche Störungsmeldung; keine ungeprüfte Weiterleitung an zweiten Anbieter.

**I03 – Überlast/Angriff:** Broker-Rate Limits und Globalbudget schützen; sensible Nutzerinformationen nicht in Abuse-Logs kopieren.

**I04 – Konto-/Memory-Leck:** Zugriff stoppen, Logs und Datenfluss sichern, betroffene Funktionen gegebenenfalls deaktivieren; Verantwortliche bearbeiten rechtliche Incident-/Benachrichtigungspflichten.

**I05 – unzulässige Safety-Antwort:** Report annehmen, konkretes Sample nur mit erforderlicher Berechtigung prüfen, Modell-/Policy-/Testversion nachvollziehen, kritisches Muster fixen und Regression wiederholen.

**I06 – Löschjob hängt:** Status für Betroffene ehrlich anzeigen, Job wiederholen und Fehler dokumentieren; kein falsches Completion-Event.

## 7. Beobachtbarkeit und Datenschutzaudit

Für jede produktive Probe dokumentieren:
- Prüfumgebung, Versionen, anonyme Trace-/Request-ID, Status, Kosten-/Latency-Metriken;
- Sicherheits-/Fehler-Tags statt Klartext;
- Auth-/Berechtigungsprüfung, Consentversion, Schemavertrag;
- Incident- oder Bug-Ticket, Maßnahme, Nachprüfung;
- Datenbewahrungs- und Löschfristen der **Testartefakte**.

Reale Nutzerdaten dürfen nicht automatisch in GitHub, Crashreports oder CI-Artifacts gelangen.

## 8. Definition of Done für K5-Konzept

- [ ] Android-/Backend-/Provider-Verantwortlichkeiten explizit und mit Alternativen begründet.
- [ ] Kein KI-Secret in APK vorgesehen, Backend-Auth/Quotas/Budgets konzipiert.
- [ ] Chat, Coach, Replay, Memory und Löschung als getrennte Daten-/API-Verträge beschrieben.
- [ ] App-Neustart, Entwurfserhalt, Idempotenz, Offline und Fehlerzustände präzisiert.
- [ ] Bedrohungsmodell für KI, Cross-Scope, Retention, Löschung und Report-Flows vorliegend.
- [ ] EU-KI-Transparenz und Google Play-Melde-/Datensicherheitsanforderungen als Release-Gates berücksichtigt.
- [ ] Kostenformel und Lastprofilvergleich vorhanden, Zahlen nicht als reale Anbieterpreise ausgegeben.
- [ ] CI-, Monitoring-, Incident-, Provider- und Pilotstrategie aufgestellt.
- [ ] K4-Regeln mit In-App-Report erweitert, ohne UX/Datenkontrolle zu verwässern.
- [ ] Offene operative und juristische Entscheidungen in K6 weitergereicht.
- [ ] GitHub-Diff umfasst nur CommunicationTrainer; kein Code/CI anderer Apps geändert.
- [ ] Neues K5-PR bleibt bis ausdrücklicher Freigabe ungemergt.

**Stand:** Das Dokument definiert **prüfbare Anforderungen**. Eine bestandene Implementierung, ein geprüfter Datenschutzbetrieb, ein wirtschaftlich tragfähiger Anbieter oder eine tatsächliche App existieren noch nicht.

## 9. Nächster Schritt: K6

K6 prüft gemeinsame Widersprüche zwischen K1–K5 und entscheidet über:
- minimalen MVP mit Qualitäts- und Datenschutzgates;
- Konten/Gast und Memory/Cloud-Sync im ersten Release;
- tatsächliche Modellwahl nach Test und Budget;
- notwendige Änderungen in K4 (Report und Datenschutztransparenz);
- Releasekriterien, Kennzahlen und priorisierte technische Umsetzungs-PRs.

**K5 ist ein Konzept- und Entscheidungsdokument, keine Freigabe für eine ungesicherte öffentliche Beta.**
