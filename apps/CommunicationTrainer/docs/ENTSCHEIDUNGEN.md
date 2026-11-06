# Konzeptentscheidungen und Änderungsverlauf

## 2026-11-06 – Konzeptstand 0.4 (PR offen): K1 Lern- und Kompetenzsystem

**Auftrag:** Nach Durchsicht der konzeptionellen Lücken wurde eine vorgeschaltete Konzeptabschlussphase K1–K6 empfohlen. Der Nutzer hat zugestimmt: Beginn mit K1, anschließend K2, K3, K4, K5 und K6.

**Neue K1-Dokumente:**
- [K1_LERN_KOMPETENZSYSTEM.md](K1_LERN_KOMPETENZSYSTEM.md) – Lernphilosophie, sechs Kompetenzbereiche mit vier Unterkompetenzen je Bereich, beobachtbare Verhaltensanker, Kontextabhängigkeit, Übungsformen, zwölf Beispiellektionen, Lernkreislauf und Schnittstelle zu K2.
- [K1_KOMPETENZ_PRUEFFELLE.md](K1_KOMPETENZ_PRUEFFELLE.md) – ausformulierte alternative Antwortpfade, 24 Fallprüfungen, Einordnung von Mehrdeutigkeit, Grenzschutz, fairer Bewertung und Nicht-Beurteilbarkeit.

**Wesentliche konzeptionelle Entscheidungen als neue Vorschläge:**
- Kompetenzfortschritt ist keine globale psychologische oder soziale Bewertung und folgt keinen simplen Nachrichtenmetriken.
- Mehrere Antworten können situativ angemessen sein; Interesse, eigener Beitrag und respektvolle Zurückhaltung werden miteinander in Beziehung gesetzt.
- Kurzfeedback verlangt konkrete Textbelege und erlaubt „kein Verbesserungspunkt belegbar“.
- Ein klares Nein wird als Grenze respektiert; kurze oder endende Unterhaltungen sind nicht automatisch schlechte Gespräche.
- Lernprofil und Coaching werden getrennt; langfristige Beobachtungen erfordern wiederholte vergleichbare Kontexte.
- Keine impliziten Defizitzuschreibungen aus kulturellen, neurodivergenten oder introvertierten Kommunikationsweisen.
- K1 liefert das Fachmodell; K2 definiert später konkrete Coach-Auswahlregeln, Qualitätsschwellen und Rückmeldeformen.

**Status:** Vom Nutzer genehmigter Arbeitsauftrag zur Konzeptentwicklung, aber **keine automatische Freigabe des K1-Pull-Requests oder Merges**. Alle K1-Änderungen bleiben bis zur ausdrücklichen PR-Freigabe auf dem Entwicklungsbranch.

## 2026-11-06 – Konzeptstand 0.3 (PR #45 gemergt): M2 Charakter- und Simulationssystem

**Anlass:** Der Nutzer hat PR #44 ausdrücklich freigegeben und als nächstes die detaillierte Konzeption des Charakter- und Simulationssystems beauftragt. PR #44 ist nach main gemergt.

**Neue Arbeitsdokumente im separaten M2-Konzept-PR:**
- [CHARAKTER_SIMULATION.md](CHARAKTER_SIMULATION.md) – acht Komponenten, PersonaSpec, Kommunikationsachsen, situativer Zustand, Beziehungen und Erinnerungen, Szenarien, Schwierigkeit, Dialogpolitik, Lebenszyklus, UX-Editor, MVP und Unteretappen M2.1–M2.6.
- [CHARAKTER_SIMULATION_TESTPLAN.md](CHARAKTER_SIMULATION_TESTPLAN.md) – Prüffälle, menschliches Bewertungsraster, Qualitätsgates und Testorganisation.

**Wesentliche vorgeschlagene Präzisierungen:**
- Big Five sind weiche Hintergrundtendenzen, konkrete Kommunikationseigenschaften werden getrennt modelliert.
- Stimmung, Energie, Aufmerksamkeit, Gesprächsinteresse und Gesprächsbereitschaft werden nicht gleichgesetzt.
- Charaktere behalten eigene Interessen, Grenzen, Meinungen und die Fähigkeit, Gespräche zu beenden.
- Erinnerungen haben Quellen, definierte Zugriffsrechte und Lösch-/Korrekturmöglichkeiten.
- Gesprächsregie wird von der Sprachgenerierung logisch getrennt; der Coach bleibt separate Funktion.
- Schwierigkeit entsteht durch eine Gesprächsdynamik, nicht durch den Persönlichkeitswert.
- Persona-Versionen sind pro laufendem Chat stabil; Wiederholungsübungen halten den Ausgangskontext fest.
- Qualitätsprüfungen beinhalten lange Chats, Grenzen, Datenschutz, Prompt Injection, Sykophanzie und realistische Variabilität.

**Statusaktualisierung:** PR #45 wurde vom Nutzer ausdrücklich freigegeben und nach main gemergt. Die M2-Detailkonzeption ist damit dokumentiert; es wurde noch keine App implementiert.

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
