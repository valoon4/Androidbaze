# D04 – Messprotokoll und Evidenzvalidator

Enthält den **synthetischen** K5-Fallkatalog M01–M28 und Modellkandidaten, **keine** Modellantworten und **keine** API-Schlüssel.

- `cases.json` – 28 Fälle, jeder mit Fachkriterium, Kategorie, Hard-Gate und nötigen Testwiederholungen;
- `candidates.json` – zu prüfende Modelle, noch **ohne geprüften Artefakt-Hash, Hardware, Lizenzfreigabe oder Benchmark-Ergebnis**;
- `measurements.jsonl` – zunächst **leer**. Unveröffentlichte Kandidaten dürfen hier nur nach echten kontrollierten Runs mit SHA-256-Evidenz-Hash aufgeführt werden; Rohchats bleiben außerhalb des Repos.
- `evaluate.py` – strenger Offline-Parser mit Status `NO_REAL_MEASUREMENTS`, `INCOMPLETE_REQUIRES_REVIEW`, `BLOCKED_HARD_GATE` oder `REVIEW_READY_NOT_APPROVED`; **niemals** eine automatische Produktfreigabe;
- `test_evaluate.py` – Python-Tests, die besonders einen **synthetischen Testfall als fälschlichen Qualitätsnachweis** verhindern.

Von der Projektwurzel:

```bash
python3 -m unittest discover -s apps/CommunicationTrainer/benchmarks/d04 -p 'test_*.py' -v
python3 apps/CommunicationTrainer/benchmarks/d04/evaluate.py
```

Der zweite Aufruf muss mit der leeren Datei `NO_REAL_MEASUREMENTS` melden. Das **ist das korrekte, ehrliche Ergebnis**.

## Aufnahme echter Messergebnisse (nur autorisierte Labore)

Füge nur Metrik-Metadaten nach konkretem Modell-/Geräte-Test hinzu; jede JSONL-Zeile hat u. a.:

```json
{
  "candidate_id": "local-qwen3-0.6b",
  "case_id": "M01",
  "trial": 1,
  "evidence_type": "real_model",
  "evidence_sha256": "<sha256-der-geschützt-gespeicherten-messdatei>",
  "reviewer_id": "<pseudonym-reviewer>",
  "review_score": 3,
  "hard_gate_ok": true,
  "latency_ms": 1200,
  "ttft_ms": 300,
  "tokens_per_second": 18.0,
  "peak_ram_mb": 980,
  "runtime_revision": "<exakter-build>",
  "model_artifact_sha256": "<sha256-der-gewichte>",
  "device_profile": "<android-modell-und-build>"
}
```

**Achtung:** Die Zahlen im obigen Schema dienen ausschließlich als **Syntaxbeispiele**, nicht als Messdaten. Nicht in die echte `measurements.jsonl` kopieren. Keine Chattexte, Prompt-/Antwortinhalte, Personenprofile, Zugangsdaten, Tokens oder Benutzerkennungen speichern. Reale Testdaten nur nach explizitem Prüfauftrag und mit geeigneter Datenhaltung/Accesskontrolle.

Die Auswertung enthält p50/p95 für vollständige Antwortzeiten **nur aus tatsächlich eingetragenen** Modellläufen; sie lässt fehlende Latenzen leer statt sie zu schätzen. `hard_gate_ok: false` blockiert die Modellabnahme. Selbst `REVIEW_READY_NOT_APPROVED` ist nur eine Einladung zum menschlichen Review und keine Integrationsfreigabe.

## Methodik

Die vollständige fachliche Erwartung der 28 Fälle steht in [K5_MODELLVERGLEICH_TESTPLAN.md](../../docs/K5_MODELLVERGLEICH_TESTPLAN.md). Angaben wie 30 oder 100+ Gesprächszüge sind hier **Prüfaufträge**, keine fertig generierten Langdialog-Fixturns. Für echte Qualitätsvergleiche müssen die synthetischen, versionierten Dialogskripte und separate unabhängige Reviewerbewertungen vorliegen. Andernfalls bleibt die fachliche Entscheidung offen.

Das CLI stellt **keine Netzwerkverbindung her** und führt selbst keine lokale LLM-Engine aus. Ein späterer autorisierter Lab-Runner für LiteRT-LM/llama.cpp/Cloud-Broker muss als **eigener klar begrenzter PR** mit Modelllizenzen, Hashes und Datenschutz-Review ergänzt werden.
