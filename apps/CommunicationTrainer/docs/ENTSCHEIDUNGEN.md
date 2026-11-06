# Konzeptentscheidungen und Änderungsverlauf

## 2026-11-06 – Konzeptstand 0.2: bisherige Verbesserungsvorschläge vollständig aufgenommen

**Rückmeldung des Nutzers:** Die bisher vorgeschlagenen Verbesserungen, Konkretisierungen und Ideen werden insgesamt befürwortet. Sie sollen als inhaltliche Arbeitsgrundlage erhalten bleiben.

**Ergänzungen gegenüber 0.1:**
- Detaillierter Charaktereditor (einfach/erweitert), Big-Five-Grundlage, situative Zustände, Beziehungskontext, Speichern und Bearbeiten.
- Charakterkonsistenz, glaubwürdige Gesprächsausgänge, Zustandspflege und realistische Testfälle.
- Konkrete beobachtbare Kompetenzsignale; Unsicherheit und Verzicht auf pauschale Diagnosewerte.
- Präzise Coach-Regeln, Beispiele, Feedbackmeldungen, menschlich validierte Testdialoge und Fehlerkorrektur.
- Freiwillige Mini-Lektionen, Lernpfade und kleine reale Alltagsschritte.
- Beispiel-XP, getrennte Spiel-/Lernfortschritte, positive Erfolge und Verzicht auf manipulative Belohnung.
- Vereinfachtes Onboarding, mögliche Gastnutzung, Character-Verwaltung und UX-Grundsätze.
- Mehr Datenschutz-, Sicherheits- und Qualitätsanforderungen sowie Pilotierung und Lernwirkungsprüfung.

**Wichtig:** Die grundsätzliche Befürwortung gilt der **Produktkonzeption**, nicht einer automatischen Freigabe jeder Detailausprägung oder eines PR-Merges. Alle Änderungen sind derzeit nur auf dem Entwicklungsbranch von PR #44; `main` bleibt bis zur ausdrücklichen Freigabe unverändert.

**Dokumentreferenz:** [KONZEPT.md](KONZEPT.md), insbesondere Abschnitt 18.

## 2026-11-06 – Konzeptstand 0.1

**Entscheidungen aus der bisherigen Unterhaltung:**
- Die App fokussiert zunächst Alltagskommunikation, soziale Kontakte und persönliche Weiterentwicklung.
- Freies Chatten, gezielte Übungen, wiederholbares Ausprobieren und individuelles KI-Feedback sind zentrale Bestandteile.
- KI-Charaktere sollen unterschiedliche Persönlichkeiten, Kommunikationsstile und situative Stimmungen zeigen; eigene Charaktere sollen erstellbar sein.
- Laufende und selbst erstellte Chats sollen in einer Übersicht zugänglich sein.
- Spielerische Motivation mit XP und Leveln wird vorgesehen, ohne lange Gespräche oder Beeinflussung anderer Menschen als primären Erfolg zu belohnen.
- Das GitHub-Repository `valoon4/Androidbaze` wird als Ablage genutzt; andere Apps bleiben unberührt.
- Vor jedem Merge eines Entwicklungsbranches in `main` ist ausdrücklich die Genehmigung des jeweiligen Pull Requests einzuholen.

**Aus der kritischen Konzeption resultierende, vorläufige Vorschläge:**
- Sechs Kompetenzbereiche, drei verbundene Nutzungsmodi, fünf Schichten pro Charakter.
- Unterschiedliche Gesprächsdynamiken als Schwierigkeitsmodell.
- Coach mit optionaler Analyse, Testdialogen und vorsichtigen Langzeitbeobachtungen.
- Vier Hauptbereiche in der Navigation sowie MVP und Meilensteine M1–M5.

**Noch offen:** App-Name, detaillierte Charaktersimulation und Bewertungsregeln, technische Implementierung, konkrete UX, KI-Anbieter, Datenschutzkonzept, Kostenmodell und exakte MVP-Spezifikation.

**Dokumentreferenz:** [KONZEPT.md](KONZEPT.md)

## Pflegehinweise

Bei jeder wesentlichen inhaltlichen Änderung:
1. `KONZEPT.md` aktualisieren.
2. Hier Datum, neue Entscheidungen, überarbeitete Annahmen und offene Punkte festhalten.
3. Im PR klar zwischen inhaltlichen Änderungen und technischen Änderungen unterscheiden.
4. Vor Merge ausdrückliche PR-Genehmigung einholen.
