# Konzeptentscheidungen und Änderungsverlauf

## 2026-11-07 – Konzeptstand 0.7 (K4-PR ausstehend): UX und Bedienkonzept

**Auftrag:** Der Nutzer hat PR #50 ausdrücklich freigegeben und die Fortsetzung mit K4 beauftragt. PR #50 wurde in main übernommen und anschließend verifiziert.

**Neu ausgearbeitet:**
- [K4_UX_KONZEPT.md](K4_UX_KONZEPT.md): umfassende Informationsarchitektur mit vier Haupttabs, schneller Einstieg, freie Chats, Szenario-Training, Coach auf Wunsch, Replay, Charaktereditor, Fortschritt, Datenschutzbedienung, Android-Konventionen, Barrierefreiheit und Fehlerzustände.
- [K4_NUTZERABLAEUFE.md](K4_NUTZERABLAEUFE.md): zwölf end-to-end Abläufe mit realistischen Negativfällen und fünf textuellen Wireframes.
- [K4_UX_TESTPLAN.md](K4_UX_TESTPLAN.md): 36 UX-Prüfaufgaben, K1–K3-Integration, Bedienhilfen, Dark-Pattern-/Vertrauensrisiken und Priorisierung.
- [k4_screens_v1.json](../specs/k4_screens_v1.json): 29 UX-Ansichten mit Aktionen, UI-Zuständen, Navigations- und Abbruchregeln.

**Vorgeschlagene Leitentscheidungen:**
- Vier primäre Tabs: Chats, Üben, Fortschritt, Profil; im zweiten Bereich lautet die Hauptüberschrift „Entdecken & Üben“.
- Erster KI-Chat ohne langen Fragebogen oder Big-Five-Pflichtkonfiguration.
- Chatmodus klar als frei oder Übung gekennzeichnet; KI-Charakter jederzeit erkennbar; Coach im freien Modus ausschließlich freiwillig.
- Kurzfeedback evidenzbasiert und ggf. ohne Verbesserungspunkt; Fehler werden meldbar und korrigierbar.
- Replay verzweigt den Verlauf, überschreibt niemals das Original und behauptet keine zuverlässige reale Wirkung.
- Fortgeschrittener Charaktereditor und sehr komplexe Fortschrittsmetriken bleiben zunächst optional/später.
- XP ist Übungsaktivität, kein globaler Sozialkompetenzwert. Ein höfliches Gesprächsende ist ein Erfolg.
- Erinnerung, Chatverlauf und Lernprofil erhalten getrennte echte Kontrollen – sofern K5 ihre technische Wirkung sicherstellt.
- Android-native Bedienung, TalkBack, dynamische Schriftgrößen, mindestens 48dp Touchziele, klare Fehlermeldungen und kontextbezogenes Zurück gehören zu den Qualitätsanforderungen.

**Offene Abhängigkeiten:** Gastmodus, server-/lokale Speicherung, Daten-/Löschgarantien, KI-Ausfallverhalten und Kostenmodell (K5); echte UX-/Nutzerprüfung und MVP-Freeze (K6).

**Status:** K4 ist eine **konzeptionelle Spezifikation**, keine bereits gebaute UI und keine durchgeführten Usability-Tests. Änderungen verbleiben bis ausdrücklicher Genehmigung des K4-Pull-Requests auf dem Entwicklungsbranch; `main` bleibt unverändert.

## 2026-11-06 – Konzeptstand 0.6 (PR #50 gemergt): Fünf Personas und konkretes Simulationsmodell

**Auftrag:** Der Nutzer hat PR #49 ausdrücklich freigegeben und K3 beauftragt. Der Merge wurde verifiziert. K3 wird in einem eigenständigen Entwicklungsbranch vorgelegt.

**Neue Unterlagen:**
- [K3_CHARAKTERE.md](K3_CHARAKTERE.md): detaillierte Beschreibungen von Mara, Jonas, Elena, Deniz und Robin, einschließlich Dialogen, Grenzen, Gegenbeispielen und typischen Zustandsvariationen.
- [K3_SIMULATIONSREGELN.md](K3_SIMULATIONSREGELN.md): Persona-, Zustands-, Beziehungs- und Gedächtnisregeln, Übergänge, Dialogaktionen, Replay, Sicherheit und Kopplung an K1/K2.
- [K3_SIMULATION_TESTPLAN.md](K3_SIMULATION_TESTPLAN.md): 36 Prüffälle und 10 ausformulierte Negativ-/Kontrastfälle.
- [k3_personas_v1.json](../specs/k3_personas_v1.json): fünf versionierte kanonische Personas als spätere App-Fixtures.
- [k3_scenarios_v1.json](../specs/k3_scenarios_v1.json): sechs Szenarien mit Kompetenzen, Startzustand, möglichen Ausgängen und Coaching-Grenzen.

**Vorgeschlagene verbindliche Gestaltungsprinzipien:**
- Kein Persönlichkeitswert determiniert einzelne Nachrichten. Ruhige Figuren dürfen bei Interesse lebhaft sein; direkte Figuren bleiben respektvoll; verträgliche Figuren dürfen widersprechen.
- Szenario-Schwierigkeit verändert Gesprächsbedingungen, nicht die zugrunde liegende Person oder ihre Moral.
- Persona-Version bleibt pro Chat stabil. Kurzfristige Zustände ändern sich nur mit begründetem Ereignis. Beziehung ist keine Nachrichtenzahl-Zuneigungsleiste.
- Erinnerungen sind quellenbelegt, scope-begrenzt, berechtigt und löschbar; Lernprofil und Charaktergedächtnis bleiben getrennt.
- Figuren haben eigene Meinungen, Interessen, Grenzen und dürfen Gespräche beenden. Keine künstliche Bindungsmaximierung.
- K2 erhält sichtbare Texte und deklarierte Bedingungen, nicht vermeintlich „echte“ emotionale Innenzustände.
- Charakter-JSON und Szenario-JSON wurden auf Grundstruktur, Wertebereiche und Querverweise geprüft; **noch kein Modell-/Dialogtest ausgeführt**.

**Offen:** genaue KI-Modellwahl, Validierung der numerischen Designparameter, konkreter Editor, Speicher-/Löschtechnologie, betriebswirtschaftliche Parameter, reale Pilotqualität.

**Statusaktualisierung (07.11.2026):** Der Nutzer hat PR #50 ausdrücklich genehmigt; der PR wurde nach main gemergt und die K3-Dokumente sind dort vorhanden. Keine andere App wurde durch diesen PR verändert; noch keine Simulationsengine implementiert.

## 2026-11-06 – Konzeptstand 0.5 (PR #49 gemergt): K2 Kommunikationscoach und Feedback

**Auftrag:** Der Nutzer hat PR #47 ausdrücklich genehmigt und die professionelle, sorgfältige Ausarbeitung von K2 beauftragt. PR #47 wurde im Repository als bereits gemergt verifiziert; kein zweiter Merge ausgeführt.

**Neue Dokumente:**
- [K2_KOMMUNIKATIONSCOACH.md](K2_KOMMUNIKATIONSCOACH.md): Begrenzter Coaching-Auftrag; Nutzungsmodi; Beobachtungsschema mit Evidenzklassen; Belegprüfung; kontextabhängige Deutungen; Sicherheitspriorisierung; Kurz- und Detailfeedback; Live-Hilfe; Widerspruchs-/Korrekturverfahren; qualifiziertes, optionales Lernprofil; XP- und Sicherheitsgrenzen; Übergaben an K3–K6.
- [K2_FEEDBACK_QUALITAET_TESTPLAN.md](K2_FEEDBACK_QUALITAET_TESTPLAN.md): 16 ausformulierte Kontrastfälle, 32 Testfälle, Review-Raster, menschliche Kalibrierung und kritische Qualitätsgates.

**Konzeptionelle Vorschläge:**
- Jede Coaching-Aussage ist auf konkrete sichtbare Textstellen zu stützen; Interpretationen und mögliche Wirkungen dürfen nicht als innere Gewissheit dargestellt werden.
- Coach und Persona bleiben getrennt. Positives Verhalten der simulierten Figur ist kein Beweis für kommunikative Qualität des Nutzers.
- Eine Rückmeldung kann nur eine Stärke, eine Reflexionsfrage, ein „nicht beurteilbar“ oder einen Grenzhinweis enthalten. Es gibt **keine Pflicht zur Kritik**.
- Maximal eine priorisierte Versuchsidee im Kurzfeedback, abhängig von Beleg, Lernziel und Nutzerpräferenz.
- Grenzen, Privatsphäre und Sicherheit gehen vor Engagement, Nachrichtenlänge oder XP.
- Widerspruchs- und Korrekturmöglichkeiten sowie datensparsame, optionalisierte Lernprofilbeobachtungen werden explizit vorgesehen.
- Kritische Bewertungsfehler werden blockierend behandelt und nicht mit gutem Durchschnittsfeedback verrechnet.

**Noch offen:** faktische Implementierung, Modellentscheidung, kalibrierte Schwellen aus menschlichen Tests, exakte Onboarding-/Opt-in-Texte, technische Löschpfade und MVP-Balancing.

**Statusaktualisierung:** PR #49 wurde vom Nutzer ausdrücklich freigegeben und am 06.11.2026 erfolgreich nach main gemergt. K2 ist damit als Konzept aufgenommen; eine KI-Implementierung liegt weiterhin nicht vor.

## 2026-11-06 – Konzeptstand 0.4 (PR #47 gemergt): K1 Lern- und Kompetenzsystem

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

**Statusaktualisierung:** Der Nutzer hat PR #47 ausdrücklich genehmigt; der PR ist bereits gemergt und die K1-Dokumente liegen auf main. Die K1-Konzeption ist damit aufgenommen, ohne dass dies eine Implementierungsfreigabe darstellt.

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
