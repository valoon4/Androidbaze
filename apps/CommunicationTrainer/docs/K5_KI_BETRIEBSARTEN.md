# K5-Ergänzung – Drei KI-Betriebsarten und Kostensenkung

**Stand:** 07.11.2026 · **Status:** technischer Konzeptentwurf; keine KI-Integration implementiert  
**Grundlage:** [K5 Architektur](K5_TECHNISCHE_ARCHITEKTUR.md), [Datenschutz](K5_DATENSCHUTZ_SICHERHEIT.md), [Betrieb/Kosten](K5_BETRIEB_KOSTEN.md), [Modellvergleich](K5_MODELLVERGLEICH_TESTPLAN.md), [Vertragsentwurf](../specs/k5_provider_modes_v1.json).

## 1. Entscheidung und Grenzen

**Empfehlung:** Alle Chat-, Replay- und Coach-Operationen sprechen eine abstrakte **InferenceGateway**-Schnittstelle an. Die konkrete KI-Betriebsart wird explizit ausgewählt; sie kann je Gerät/Account unterschiedlich verfügbar sein.

1. **Lokal auf Android:** ein geeignetes Open-Weight-Modell arbeitet auf einem kompatiblen Endgerät, ohne bei der Inferenz Chattext an einen Provider zu senden.
2. **ChatGPT-Plan (experimentell und bedingt):** offizielle „Sign in with ChatGPT“-Autorisierung für *berechtigte* lokal betriebene Open-Source-Clients, soweit native Android-Integration und Veröffentlichungsmodell zugelassen sind.
3. **Cloud:** ein abgesicherter Broker, der eine reguläre API **oder selbst gehostete Open-Weight-Modelle** verwendet. Keine Anbieter-Secrets in der APK.

Die Modi sind **Möglichkeiten, keine garantierten Produkteigenschaften**. Kein Modus darf als „kostenlos und unbegrenzt“ beworben werden; ein eigener Server hat GPU-, Strom-, Sicherheits- und Wartungskosten. Lokale Inferenz kostet Geräteenergie, Speicher und ggf. Modell-Download.

**Keine stillschweigende Verlagerung:** Wenn die lokale KI langsam, ungeeignet oder nicht installiert ist, dürfen Nachrichten nicht ohne erneute informierte Auswahl an einen Cloud-Anbieter gehen. Ist ChatGPT-Kontingent erschöpft, ist ein Wechsel zu regulär berechneter API ebenfalls **nur mit bewusster Zustimmung** zulässig.

## 2. Offizielle Grundlage und Lizenzgrenzen

- OpenAI: [Sign in with ChatGPT – Open-Source-Plan-Nutzung](https://developers.openai.com/siwc/token-sharing-open-source) und [Anmelderegeln](https://developers.openai.com/siwc/token-sharing-open-source/sign-in). Die Anleitung betrifft Open-Source-/lokal gehostete Apps; für kostenpflichtige oder entfernt gehostete Apps ist die Integrationsberechtigung gesondert zu klären. **Ob unser konkreter Android-OAuth-Callback und die spätere Distribution unterstützt werden, ist nicht nachgewiesen.**
- OpenAI: [ChatGPT und API werden grundsätzlich getrennt abgerechnet](https://help.openai.com/en/articles/9039756-managing-billing-for-chatgpt-and-the-api-platform). Die optionale ChatGPT-Plan-Nutzung über einen ausdrücklich berechtigten OAuth-Flow ist eine beschränkte Ausnahme, **kein allgemeiner API-Freifahrtschein**.
- Google: [LiteRT-LM](https://developers.google.com/edge/litert-lm/overview) mit Kotlin/Android-Unterstützung.
- llama.cpp: [Android-Binding und GGUF](https://github.com/ggml-org/llama.cpp/blob/master/docs/android.md).
- Qwen3 Originalkarten: [0.6B](https://huggingface.co/Qwen/Qwen3-0.6B), [1.7B](https://huggingface.co/Qwen/Qwen3-1.7B), [4B](https://huggingface.co/Qwen/Qwen3-4B). Originalmodelle unter Apache-2.0; Quantisierung/Distribution jeweils erneut prüfen.
- Google [Gemma 3 1B IT](https://huggingface.co/google/gemma-3-1b-it) ist eine Alternative mit **eigenen Gemma-Nutzungsbedingungen**, nicht automatisch Apache-2.0.
- OpenAI [gpt-oss-20b](https://huggingface.co/openai/gpt-oss-20b) ist ein offenes, für gewöhnliche Android-Einstiegsgeräte eher unpassend großes Modell. Primär mit Server-/Workstation-Varianten vergleichen.

**Rechtliche Unterscheidung:** Eine quelloffene Runtime (z. B. llama.cpp), ein Modell mit öffentlich verfügbaren Gewichten und eine permissive Modelllizenz sind drei unterschiedliche Aspekte. **Lizenz-/Redistributionscheck pro konkret paketierter Modell- und Runtime-Version** ist Release-Gate.

## 3. InferenceGateway und Capability-Erkennung

Logischer Datenvertrag:

\`\`\`text
Chat / Übung / Replay / Kommunikationscoach
                    │
        InferenceGateway + K1–K3-Regeln
                    │
      CapabilityCheck / Nutzer-Moduswahl
        ┌───────────┼─────────────┐
        │           │             │
  LocalAndroid   ChatGPTPlan   CloudBroker
    LiteRT/      offiziell     API | Self-Hosted
    llama.cpp    berechtigt    OSS
\`\`\`

Pro Modus: \`availability\`, Modell/Version, unterstützte Operationen (Dialog, Coach, Replay), Offline-/Netzanforderung, Speicherbedarf, voraussichtliche Nutzungslimits, Datenziel und Datenschutzbeschreibung.

Operationen \`generate_character_turn\`, \`generate_coach_feedback\`, \`generate_replay\`, \`cancel\`, \`get_request_status\`; stabile \`request_id\` und Persona-/Rubrik-/Szenarioversion unabhängig vom Provider. **Idempotenz, K2-Belegprüfung, K3-Grenzen und Safety** bleiben für alle Betriebsarten erforderlich. Bei rein lokaler Nutzung können die Prüfungen lokal erfolgen, dürfen aber nicht einfach entfallen.

Die App zeigt nur tatsächlich verfügbare Modi, sonst begründete deaktivierte Einträge. Bei einem Modell ohne akzeptable Coach-Qualität darf zwar eventuell der Chat laufen, aber es gibt **kein erfundenes oder unzuverlässiges KI-Feedback**.

## 4. Lokal auf Android – primärer Kostentest

### 4.1 Kandidaten, keine vorweggenommenen Sieger

| Kandidat | Rolle im Test | Risiko |
|---|---|---|
| Qwen3 0.6B | untere Ressourcen-/Qualitätsgrenze | voraussichtlich deutliche Einbußen bei komplexem Coaching; überprüfen |
| Qwen3 1.7B | erster Kompromisskandidat | Deutsch/Format-/Charaktertreue nicht belegt |
| Qwen3 4B | Qualitätsvergleich für geeignete leistungsstärkere Geräte | mehr RAM, Wartezeit und Akkubedarf |
| Gemma 3 1B IT | zweite Modellfamilie für Gegenprobe | Nutzungsbedingungen und tatsächliche Integration prüfen |
| gpt-oss-20b | eher eigene Workstation/GPU-Server-Benchmark | für gewöhnliche Smartphones kein sinnvoller Standard |

**Wichtig:** Modellgröße allein misst keine Charaktertreue oder Coachingqualität. Quantisierung, Kontextlänge, Chattemplate, NPU/GPU/CPU-Backend und konkrete Hardware stark berücksichtigen.

### 4.2 Runtime und Nutzerführung

**LiteRT-LM** als Kotlin-nahe erste Implementierung untersuchen; **llama.cpp** als Alternative mit GGUF/NDK und größerer Laufzeitflexibilität. **Nicht beide ungeprüft im ersten MVP integrieren** – hoher Wartungsaufwand.

Der Lokale-Modus benötigt:
1. Geräteprüfung für RAM, verfügbare Speicherung, ABI und tatsächlich getestete Beschleunigung.
2. Modellanzeige mit belegtem Speicherbedarf und vertrauenswürdigem Download; Prüfsumme/Signatur für Modell-Dateien.
3. Abbrechbare Installation, klare Zustände „nicht installiert“, „bereit“, „unzureichender Speicher“, „nicht kompatibel“, „Modell entfernen“.
4. Messung von erster Antwort, Tokens/s, Startdauer, Speicherhochpunkt, Batterie und thermischer Drosselung über längere Chats.
5. **Keine automatische externe Übertragung**, auch nicht als „Qualitätsverbesserung“.
6. K3-Kontext und K2-Belege auf demselben lokalen Datenschutzpfad halten.
7. Fehler und beschädigte/abgebrochene Downloads ohne Datenverlust behandeln.

Lokale Inferenz heißt **nicht automatisch komplett offline betriebene App**: Modell-Download, Updates, Konten oder Telemetrie können separat Netz erfordern; das Produkt muss präzise formulieren.

## 5. ChatGPT-Konto – bedingter Prototyp, keine gesicherte Android-Funktion

OpenAI dokumentiert für berechtigte lokale Open-Source-Clients eine optionale Erlaubnis, die ChatGPT-Plan-Nutzung für bestimmte Responses-API-Anfragen zu verwenden. Das ist **nicht** Zugriff auf bisherige ChatGPT-Chats, Erinnerungen oder uneingeschränkte API-Nutzung.

**Zulassungsgate vor Android-Implementierung:**
- Unterstützt die aktuelle offizielle OpenAI-Regelung **genau** unser natives Android-Appmodell und den Distributionsweg?
- Erlaubt sie eine spätere kostenpflichtige/gehostete Version oder ist eine separate Partnerfreigabe nötig?
- Können wir den vorgeschriebenen OAuth-Flow mit der dokumentierten Loopback-Redirect-Anforderung auf Android korrekt und sicher umsetzen? Die Desktop-Beispiele dürfen nicht als Nachweis gelten.
- Sind Modell und Funktionsumfang (z. B. strukturierte Coach-Ausgaben, Streaming) tatsächlich im gewährten Scope verfügbar?

**Wenn nicht nachgewiesen:** Feature-Flag \`disabled_unsupported\`; keine ungeprüften privaten ChatGPT-Webendpunkte, keine Session-Cookie-Weitergabe, keine Aufforderung, ChatGPT-Passwörter in unsere App einzugeben.

**Bei erfolgreicher Zulassung (konzeptionell):**
1. Systembrowser mit offizieller Anmeldung; pro Anfrage frisches \`state\`, \`nonce\` und PKCE.
2. offiziellen Client-/Host-Identifier verwalten; ID-Token-Signatur/Audience/Issuer/Nonce, Accountbindung und tatsächlich gewährte Scopes prüfen.
3. ChatGPT-Plan-Berechtigung nur aktivieren, wenn \`chatgpt.tokens.use.direct\` tatsächlich gewährt wurde; sonst gesondert informierten anderen Weg anbieten.
4. Tokens möglichst kurzlebig und in geeignet geschütztem lokalen Speicher halten; rotierende Refresh-Tokens atomar erneuern; nie in Logs/URLs/Backups/Git ablegen.
5. Limits, Fehler, Widerruf und Abmeldung technisch sauber verarbeiten. Kein „unbegrenzt“.
6. Kontoauswahl und Trennung bei mehreren ChatGPT-Konten; Nutzung in den ChatGPT-Kontoeinstellungen nachvollziehbar.
7. Für zulässige Anfragen dokumentierte Vorgaben wie \`store: false\`/Streaming beachten und tatsächliche Provider-Datenpolitik prüfen.

Die App fragt niemals nach dem ChatGPT-Kontopasswort. Der Nutzer kann den Zugang trennen, ohne lokal gespeicherte eigene Chats zwingend zu verlieren; Lösch-/Verknüpfungsregeln separat prüfen.

## 6. Cloud – gehostete Modelle als zwei technisch verschiedene Fälle

### 6.1 Reguläre API über Broker

Muss die K5-Gates erfüllen: serverseitige Secrets, Auth/Guest-Identität, Budget, Rate Limits, Consent, Safety, Idempotenz, Scope, Provider-Vertrag und Löschung. API-Nutzung ist grundsätzlich **separat kostenpflichtig** und wird nicht automatisch durch ChatGPT Plus/Pro gedeckt.

### 6.2 Eigene Open-Weight-Inferenz

Selbst betriebene Runtime auf eigener GPU oder gemieteter Hardware: hier entfallen möglicherweise Tokengebühren eines fremden API-Anbieters, aber nicht GPU/CPU-Stunden, RAM/VRAM, Hosting, Strom, Netzwerk, Updates, Moderation, Betrieb und Missbrauchsschutz.

**Prüfen statt unterstellen:** Ein eigener GPU-Server rechnet sich bei geringer Nutzung oft schlechter als nutzungsabhängige APIs. Break-even nur mit gemessener Auslastung und echten Preisangeboten berechnen. Modelllizenz und zulässige Hosting-/Redistribution beachten.

## 7. Keine implizite Preis- oder Datenschutzbehauptung

| Modus | KI-API-Kosten des Betreibers | Weitere Kosten / Risiken | Datenschutzversprechen |
|---|---|---|---|
| Lokal | keine pro lokalem Inferenzturn | Nutzerhardware, Modellverteilung, QA/Wartung | lokale Nachrichtenverarbeitung nur bei tatsächlich lokalem Betrieb |
| ChatGPT-Plan | ggf. Nutzung vorhandener berechtigter Limits statt regulärer Betreiber-API | Planlimit, Berechtigung, OAuth und Produkt-/Vertriebsgates | Konto- und Promptdaten werden im genehmigten OpenAI-Verfahren verarbeitet |
| Cloud API | nach Anbieterpreis und tatsächlicher Nutzung | Backend, Moderation, Retry, Quotas | Übertragung an Broker + Provider, vertragsabhängig |
| Self-Hosted OSS | kein fremdes per-Token-API-Entgelt (je Hostingmodell) | GPU/CPU, Hosting, Wartung und Safety | Betreiber verarbeitet Text selbst; nicht gleich „auf deinem Gerät“ |

Kostenformeln und Benchmarks: [K5 Modellvergleich](K5_MODELLVERGLEICH_TESTPLAN.md) und [Betriebskosten](K5_BETRIEB_KOSTEN.md).

## 8. Phasen statt überladenem ersten Release

**K6-Entscheidungsvorschlag:** Drei Betriebsarten im Architekturvertrag festhalten, aber **nicht alle drei sofort implementieren**.

- **Evaluationsphase:** ein lokal laufendes Modell und eine bewährte Cloud-Referenz anhand identischer K2-/K3-Dialoge vergleichen; eine unabhängige zweite Modellfamilie als Kontrolle.
- **MVP:** **ein** tatsächlich sicher, wirtschaftlich und qualitativ tragfähiger Modus als stabiler Standard; lokale Variante bei ausreichender Geräteabdeckung optional.
- **ChatGPT-Anbindung:** ausschließlich nach dokumentierter offizieller Zulassung und bestandenem nativen OAuth-POC; sonst sichtbar „derzeit nicht verfügbar“ oder ganz ausblenden.
- **Self-Hosted-Server:** erst nach nachgewiesenem Break-even und Betriebsverantwortung.
- **Später:** wählbarer Modus pro Chat, bewusster Moduswechsel mit klarer Datenfreigabe, Modellspeicherverwaltung und Anbieterwechsel.

**Kernprinzip:** Geringere Kosten dürfen nicht durch weniger sichere, künstlich begeisterte oder lernmethodisch fehlerhafte Antworten erkauft werden.

## 9. Akzeptanz und offene Fragen

1. Kommt ein kleines deutschtaugliches Modell auf den tatsächlich unterstützten Android-Geräten durch die **K2-Grenz- und Grounding-Gates**?
2. Können alle fünf K3-Personas bei langen Chats unterscheidbar und konsistent bleiben?
3. Welches Modell eignet sich für den Coach, wenn das lokale Dialogmodell dafür nicht reicht?
4. Wird ChatGPT-Plan-Nutzung auf Android mit unserem Open-Source-/Vertriebsmodell ausdrücklich unterstützt?
5. Welche realen Kosten pro Trainingssession entstehen je Modus **inklusive** Installation, Batterie, Support, GPU und Moderation?
6. Ist ein Hybrid sinnvoll, bei dem der Nutzer explizit lokale Gespräche und gesonderte Cloud-Analysen erlaubt?
7. Wie werden Modell-Dateien, Lizenzen, Updates und Provider-Funktionen über die App-Lebensdauer gepflegt?

**Abnahme:** Nur nach gemessenen K2/K3-Ergebnissen, Sicherheits-/Lizenzprüfung und klarer Nutzerwahl. Diese Ergänzung ist eine technische Designentscheidung **über austauschbare Modi**, keine verbindliche Zusage der Verfügbarkeit von ChatGPT-Plan-Nutzung oder kostenloser Offline-KI.
