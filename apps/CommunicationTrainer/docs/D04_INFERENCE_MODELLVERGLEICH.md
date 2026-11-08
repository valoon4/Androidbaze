# D04 – InferenceGateway, Modellkandidaten und reproduzierbarer Modellvergleich

**Status:** D04-Infrastruktur und Forschungsstand, 08.11.2026. **Noch keine echten lokalen oder Cloud-Inferenzmessungen und keine Modellfreigabe**. D03 bleibt die einzige funktionierende Antwortquelle (deterministische Simulation), nun hinter einem einheitlichen InferenceGateway. Änderungen werden in einem eigenen PR vorgeschlagen und nicht ohne Freigabe gemergt.

## 1. Was D04 technisch wirklich liefert

- Kotlin-`InferenceGateway` mit `generateCharacterTurn`, `generateCoachFeedback`, `generateReplay`, `cancel`, `getRequestStatus`.
- Operation-spezifische `supportedOperations`, stabile Modi `D03_MOCK`, `LOCAL_ANDROID`, `CLOUD_BROKER`, `SELF_HOSTED` und `CHATGPT_PLAN`. Jede Anfrage trägt `requestId`, Conversation-/Branch-/Persona-Referenz, Versions-/Privacy-Felder und Nutzereingabe.
- Die produktiv nutzbare D03-Testantwort ruft **ausschließlich** `DeterministicMockGateway` auf; keine Änderung der Chatdatenbank und keine Modellinstallation.
- Alle anderen Modi liefern **typisierte Fehler**, statt auf ein anderes Modell umzuschalten. Insbesondere kein stiller lokal→Cloud-Fallback, keine vermeintlich bereits verfügbare On-device-KI und keine kostenpflichtige API bei ChatGPT-Plan-Ablehnung.
- Unit-Tests prüfen negative Operationen, ungültige Anfragen, Mode-Fallback-Verbot und unveränderte Mock-Unterhaltung. Die vorherigen D02-/D03-Speichertests und Room-v1-Schema bleiben bestehen.
- Python-Stdlib-Evaluationswerkzeug unter [../benchmarks/d04](../benchmarks/d04/README.md) kennt alle **M01–M28**-K5-Fälle, nimmt nur anonymisierte Metrik-/Review-Metadaten entgegen und meldet fehlende Modellläufe ausdrücklich als `NO_REAL_MEASUREMENTS`. Es ruft **keinen** KI-Dienst auf.

**Bewusst offen:** Keine reale `LiteRT-LM`-/`llama.cpp`-Integration, kein Modell-Binary, keine Cloud-API, kein GPU-Benchmark, kein Inference-Broker, keine Schlüssel und keine Modell-Rangfolge. D04 ist erst **vollständig fachlich abnehmbar**, wenn die unten genannten Labor-Gates anhand echter Messungen geklärt sind.

## 2. Kandidaten, nur als Vergleichshypothesen

Stand der öffentlichen Modell-/Runtime-Unterlagen: **08.11.2026**. Die Kandidaten-IDs sind in [candidates.json](../benchmarks/d04/candidates.json) gespeichert. Vor dem Test genaue Modell-Snapshot-ID, Hardware, Runtime-/Quantisierungsversion, SHA-256, Lizenz und Vertragsbedingungen erneut verifizieren.

| Kandidat | Denkbarer Einsatz | Warum prüfen? | Offene Hürden |
|---|---|---|---|
| **Qwen3-0.6B** | Local-S, sehr kleines Modell | Minimaler technischer Fußabdruck als Hypothese | Deutsch, Kontexttreue, unzureichende Gesprächsqualität möglich |
| **Qwen3-1.7B** | Local-S/M | Auf Smartphones noch denkbare Modellgröße | echte Hardwareleistung, RAM, Qualität und Quantisierungsartefakt |
| **Qwen3-4B** | Local-M | mögliche bessere Dialogqualität | RAM, Thermik, Modellladen, tatsächliche Gerätekompatibilität |
| **GPT-6 Luna** | günstige Managed-API-Referenz | Kostenuntergrenze einer kleinen Cloudklasse | Qualität, reale Kontoberechtigung, Budget, Security-/Broker-Gate |
| **GPT-6.1 Terra** | stärkere Managed-API-Referenz | Abgleich, ob Qualitätsgewinn den Aufpreis rechtfertigt | Budget, Datenübermittlung, Verträge, Broker, reale Latenz |
| **Gemini 3.7 Flash** | unabhängige Managed-API-Referenz von Google | Anbieterdiversität, deutscher Gesprächsstil, Preis-/Antwortvergleich | eigenständiger Vertrag/Datenschutz, Brokerintegration, echte Verfügbarkeit |

Die Qwen3-Modellkarten sind mit **Apache-2.0** gekennzeichnet. Dies ist **keine** automatische Freigabe jedes Drittanbieter-GGUF-/LiteRT-Konvertats, jeder Runtime oder kommerzieller Verteilung: exakte Gewichte, Chat-Template, Derivate und eigene Lizenzprüfung dokumentieren.

**Offizielle Quellen:** [Qwen3-0.6B](https://huggingface.co/Qwen/Qwen3-0.6B), [Qwen3-1.7B](https://huggingface.co/Qwen/Qwen3-1.7B), [Qwen3-4B](https://huggingface.co/Qwen/Qwen3-4B), [LiteRT-LM Kotlin/Android](https://developers.google.com/edge/litert-lm/android), [llama.cpp Android](https://github.com/ggml-org/llama.cpp/blob/master/docs/android.md), [OpenAI-Modellübersicht](https://developers.openai.com/api/docs/models), [OpenAI-API-Preise](https://developers.openai.com/api/docs/pricing), [Gemini-API-Modellübersicht](https://ai.google.dev/gemini-api/docs/models), [Gemini-API-Preise](https://ai.google.dev/gemini-api/docs/pricing).

**Wichtig:** Das ist keine Aussage, dass alle drei Qwen-Modelle mit einem bestimmten LiteRT-Binary kompatibel sind. Android Runtime und passendes quantisiertes Modell sind zusammen zu validieren. Für `llama.cpp` muss ein geprüftes GGUF-Gewicht und das konkrete ARM64-Geräteprofil vorliegen. Modelle weder in APK noch Repo einchecken, bevor Hardware, Hash, Lizenz und Downloadweg freigegeben sind.

## 3. Grober Preisindikator, kein tatsächlicher App-Preis

Die offiziellen Standard-API-Preise zum **08.11.2026**: GPT-6 Luna **0,10 USD / 1 Mio. Eingabetoken**, **0,50 USD / 1 Mio. Ausgabetoken**; GPT-6.1 Terra **1 USD / 1 Mio. Eingabetoken**, **5 USD / 1 Mio. Ausgabetoken**. Diese Angaben müssen vor einer Live-Evaluierung erneut anhand des gewählten Tarifs verifiziert werden.

Für Gemini 3.7 Flash nennt die offizielle Preisübersicht (zeitlich befristeter Standardtarif bis 31.12.2026) **0,75 USD / 1 Mio. Eingabetoken** und **3,75 USD / 1 Mio. Ausgabetoken inklusive Thinking-Tokens**. Damit wären bei der unten genannten Beispielmenge **0,002625 USD** an reinen Modelltokenkosten fällig. In der Praxis können Denkbudget, Caching und tatsächliche Antworttoken die Vergleichbarkeit beeinflussen. Ohne konkrete API-Tokens ist auch das **keine gemessene App-Kostenrechnung**.

Eine *rein beispielhafte* Antwort mit 2.000 Eingabe- und 300 Ausgabetoken ergäbe **0,00035 USD** (Luna) oder **0,0035 USD** (Terra) an reinen Modelltokenkosten. Die Formel ist `input_tokens * input_price_per_million / 1e6 + output_tokens * output_price_per_million / 1e6`.

**Nicht enthalten:** längerer Chathintergrund, Coaching als separate Abfrage, Wiederholungen, Reasoningtoken-Overhead, Cache-/Routenpreise, Moderation, API-/Netz-Broker, Hosting, Support, Steuer, Ausfälle, Latenz und Betrieb. On-device hat **keine API-Tokenrechnung**, aber Modellgröße, Hardwarestrom, Batterieverschleiß, Geräteverfügbarkeit, Packaging und Pflege. Keine endgültige Kostenaussage ohne echten Sitzungsmix.

## 4. Versuchsdesign – Pflicht vor Modellwahl

1. **Gate A / Freigabe:** Modell- und Runtime-Version und Nutzungsrechte; passende konkrete Android-Testgeräte (klein/mittel/leistungsstark), Testrahmen und Budget; Cloud nur über abgesicherten Test-Broker mit geprüftem Vertrag, Quotas und Zweckfreigabe.
2. **Reproduzierbarkeit:** exakte Modellrevision, Quantisierungsformat, Chat-Template, Runtime-Revision, Gerätemodell/ABI/Androidversion, CPU/GPU/NPU-Beschleunigung, Prompt-/Persona-Version, festgelegter Seed und SHA-256. Lokale Test-Binaries dürfen außerhalb des Repos bleiben.
3. **Sprach- und Rollenqualität:** gleicher synthetischer K3-/K2-Korpus (M01–M28), mehrere Seeds und menschliche Zweitbewertung; Persona, natürliches Deutsch, Grenze, Mehrturn-Konsistenz, Not-Assessable-Verhalten und überprüfbare Coach-Zitate **getrennt** bewerten.
4. **Harte Blocker:** M09 (Nein/Grenze), M14 (keine erfundene Kritik), M16 (alle Zitat-IDs existieren), M18 (Prompt Injection), M21–M27 (Status, Ausfälle, Datenschutz, Kosten-Fallback) und M28 (Lizenz) können **nicht** durch eine Durchschnittsnote ausgeglichen werden. **Gates gelten pro relevanter Betriebsart und Operation:** M16 blockiert einen Coach, aber nicht automatisch einen ausdrücklich **chat-only** freigegebenen lokalen Modus; M25 gilt für Cloud, nicht für Offline-Runtime. Der Validator verhindert unfaire Cross-Mode-Vergleiche und meldet fehlende Gate-Evidenz für jeden tatsächlich geplanten Betriebsmodus.
5. **Leistung auf echter Hardware:** TTFT, gesamte Antwort p50/p95, Tokens/s, Cold Start, RAM-Spitze inklusive KV-Cache, Hitze/Akku, Modell-Download/Storage, Crash- und Timeoutquote über einen dokumentierten Stichprobenumfang.
6. **Finanzen und Sicherheit:** Prompt-/Antwort-Tokenverbrauch je tatsächlicher Session mit Retry und optional Coach; Kosten/Tarife auf Messung statt erfundene Nutzungszahlen stützen. Cloud-Modus darf keine privaten Chats ohne informierte explizite Freigabe übertragen. Kein Operator-API-Key im APK.
7. **Entscheidung:** Mindestens ein lokaler Kandidat und eine Cloud-Referenz mit vergleichbarem Korpus und je eigener vollständiger Provenienz. Review durch unabhängig bewertende Menschen, dokumentierte Rest-Risiken, konkrete Hardware-/Budgetfreigabe. Ergebnis als eigener Gate-A-Entscheid, **keine automatische Aktivierung des besten Scores**.

**Kein Benchmark wurde in dieser CI auf einem echten Modell ausgeführt.** Im eingehängten Datenbestand sind deshalb **0 reale Messungen**; Python- und Kotlin-Unit-Tests prüfen ausschließlich die technischen Verträge und das Auswertungsschema. Die Empfehlung nach dieser Stufe bleibt **„Modellentscheidung offen“**.

## 5. Datenschutz- und Release-Schutz

- Der Android-Manifeststand aus D03 enthält **keine INTERNET-Berechtigung**. D04 fügt sie nicht hinzu, prüft keine externen Verbindungen und schreibt keine Zugangsdaten. `CloudBroker` und `ChatGPTPlan` sind blockierte Typen.
- Ein Cloud-Modus muss später über D05 (autorisierter Broker, serverseitige Geheimnisse, Rate Limits, Idempotency Ledger, Abusekontrollen, echte Löschung/Reporting, Zweck- und Regionstransparenz, DPA/AVV und ggf. Rechtsprüfung) implementiert werden.
- Lokale Gespräche dürfen **nicht** beim Mangel eines On-device-Modells heimlich in die Cloud geschickt werden.
- Ein ChatGPT-Abonnement gilt **nicht** automatisch als API-Nutzungserlaubnis für eine fremde Android-App. Der Modus bleibt bis zur offiziell bestätigten Berechtigung gesperrt.
- D04 Test-APK bekommt einen eigenen Paketnamen **`.d04preview`**, um eine Installation neben dem D03-Build trotz rotierender CI-Debug-Zertifikate zu ermöglichen. Keine stille Datenübernahme; dauerhafte Signatur/Upgrade-Migration bleibt ein separates Qualitätsgate.

## 6. Reviewkriterien für den PR

- bestehende D01–D03-JVM-/Room-Tests und D04-Gateway-Tests grün;
- 28 K5-Cases unverändert und eindeutig; Validierung des Evidenzformats verhindert rohe Chats und „Fake-Daten als Ergebnisse“;
- D04-Status des Benchmarkreports `NO_REAL_MEASUREMENTS` bleibt ehrlich;
- CI testet und signiert eigenständige D04-Debug-APK;
- keine Änderungen an Kabelsalat, InnerWave, TimeFlow, Wormblade, deren Gradle-Projekten oder Workflows;
- **D04-Recherche- und Gateway-PR ungemergt bis ausdrückliche Freigabe; keine echte KI-Integration ohne neues separates Go.**
