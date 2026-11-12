# Kabelsalat · Aufgabensteuerung ab v2.0.1

**Kanonischer Tasktracker:** sieben verlinkte Markdown-Dateien mit **884 eindeutig nummerierten Einzelaufgaben** in 54 Arbeitspaketen. Basis: 828 Tasks am 10.11.2026 aus Roadmap, Konzept und Referenzdaten; **48 aus dem hochgeladenen älteren Bro-Chat abgeleitete Aufgaben am 11.11.2026** (36 direkt plus 12 nach vertieftem Artefakt-/Prüffall-Abgleich). [Quellabgleich / wichtige Widersprüche](./BRO_CHAT_CROSSWALK.md).

| Bereich | Aufgaben | ID-Bereich | Live-Datei |
|---|---:|---|---|
| P0 · Quellen, Schachtwand, Kabelwege, OLA/MS und Pitch | 216 | KAB-0001–KAB-0216 | [01-P0-QUELLE-UND-PITCH.md](./01-P0-QUELLE-UND-PITCH.md) |
| P1 · Frei editierbarer Schacht, 2D/3D, Netz und Dateien | 216 | KAB-0217–KAB-0432 | [02-P1-EDITOR-UND-NETZ.md](./02-P1-EDITOR-UND-NETZ.md) |
| P2 · Geometrieprüfung, Herkunft, Import und Freigabegrenzen | 216 | KAB-0433–KAB-0648 | [03-P2-DATEN-UND-PRUEFUNG.md](./03-P2-DATEN-UND-PRUEFUNG.md) |
| P3 · QA, Security, Performance, Veröffentlichung, Feedback | 180 | KAB-0649–KAB-0828 | [04-P3-QA-BETRIEB-UND-FEEDBACK.md](./04-P3-QA-BETRIEB-UND-FEEDBACK.md) |
| P0/P1/P2 · Bro-Chat: korrigierte 3D-Vorgaben, 92-Schacht-Prüfung, PDF/Excel | 36 | KAB-0829–KAB-0864 | [05-BRO-CHAT-AVL-UND-REPORTS.md](./05-BRO-CHAT-AVL-UND-REPORTS.md) |
| P2/P1 · Historische 92-Schacht-Fälle und Berichtsversionen | 12 | KAB-0865–KAB-0876 | [06-BRO-CHAT-PRUEFFALL-UND-ARTEFAKT-QA.md](./06-BRO-CHAT-PRUEFFALL-UND-ARTEFAKT-QA.md) |
| P0 · Bros jüngstes Feedback: klassischer Betonblock, DM/DN und Erkennung | 8 | KAB-0877–KAB-0884 | [07-BRO-BETON-ROHR-DM.md](./07-BRO-BETON-ROHR-DM.md) |
| **Gesamt aktuell (12.11.2026)** | **884** | **KAB-0001–KAB-0884** | |

## Initialer Status am 10.11.2026

- **Neu angelegte Tasks:** 828.
- **Als erledigt nachgewiesen:** 0 von 828, da die neuen Aufgaben noch nicht einzeln abgearbeitet oder geprüft wurden. Das ist **keine** Aussage, dass vorher keine Software entwickelt wurde.
- **Offen:** 828. **Nächster Kandidat:** KAB-0001 – PDF-Index für Schacht 13_U_028 eindeutig versionieren. Danach KAB-0002 ff., wobei abhängige oder blockierte Aufgaben dokumentiert übersprungen werden dürfen.
- **Wichtig:** P3 enthält QA/CI/Security, die bereits bei jedem passenden P0/P1/P2-Sprint erledigt werden müssen; Priorität P3 heißt nicht, bis zum Schluss mit Tests zu warten.

## Nachtrag aus Bros altem Chat · 11.11.2026

- **Trackerstand nach Übernahme:** **0 erledigt / 876 offen / 876 gesamt** (Zwischenstand vor Bros jüngstem Feedback). Wir haben die Chat-Quellen geprüft und Aufgaben **angelegt**, nicht die Softwareaufgaben umgesetzt.
- **Neue Tasks:** KAB-0829–0876 (36 technische/Prüf-/Berichtsfunktionen plus 12 detaillierte Prüf- und Altar­tefaktaufgaben). **Vorrangiger Klärungsblocker:** KAB-0829–0832 (Höhenregel A/B, 75-mm-/100-mm-Konflikt, 62-%-Demo-Belegung). Die ursprüngliche KAB-0001 ist weiter der erste offene numerische Task.
- **Neuprüfungs-Daten:** Im Chat werden 92 Schächte und ein 1.957-seitiger Plansatz erwähnt; weder diese Originaldateien noch die vollständige AVL0116 lagen hier vor. Deshalb sind die Neuprüfungen **nicht** als erledigt markiert.

- **Vertiefte Abnahme:** [KAB-0865–0876](./06-BRO-CHAT-PRUEFFALL-UND-ARTEFAKT-QA.md) sichern alte Dateiversionen, bilden 92 echte Schachtprüffälle erst mit der Originalschachtliste und prüfen die im Chat genannten 274 Seiten / 35 / 276 Bilder. Keine dieser Zahlen wurde bislang unabhängig bestätigt.
- **Quelle:** [BRO_CHAT_CROSSWALK.md](./BRO_CHAT_CROSSWALK.md). Das historische Gespräch und Assistentenbehauptungen sind **keine fachliche Freigabe**.


## Aktueller Sprintstand · 12.11.2026

**[Bros 10-Aufgaben-Entwicklungssprint](./SPRINT_2026-11-12_BRO_10.md)** lieferte echte Änderungen an Rendering, Maßdaten, unbestätigter Plan-/Bild-Kandidatenerkennung, Quellenfixtures und Demo-Geometrietrennung. **9 KAB-Tasks wurden mit konkret zugeordneten Commits/Tests abgeschlossen**, weitere 10+ Arbeitspakete technisch begonnen oder vertieft. Nicht abschließbar bleiben Bros visuelle Abnahme, fehlende Originalmaßquellen, trainierte KI bzw. echte PDF-/Foto-Rohrtyp-Erkennung und die 75-/100-mm-Bauartregel. Die ID-Rangfolge bleibt unverändert.

## Gesamtpriorität nach Bros neuesten Praxistests (11.11.2026)

- **Verbindliche Abarbeitungsreihenfolge:** [PRIORITIES.md](./PRIORITIES.md) mit der [vollständigen Rangliste aller 884 Tasks](./PRIORITY_ORDER.csv). **IDs sind stabil, Reihenfolge ist Rang 1–884, nicht die Nummer der KAB-ID.**
- **Heute priorisiert:** KAB-0877 (Betonblock mit echten Löchern) → KAB-0878 (dunklere Betonoptik) → KAB-0880 (Durchmesser-Datenmodell) → KAB-0879 (gemischte Öffnungsgrößen) → KAB-0883 (sichtbare Dimension) → KAB-0881/0882 (PDF-/Foto-Erkennung mit Quellabgleich).
- **Aktueller Zähler nach erstem Bro-Sprint:** **9 erledigt / 875 offen / 884 gesamt**, einschließlich bestehender, jetzt durch die Tests belegter Quell-/Ansichtsaufgaben. Die grundlegende Schachtsoftware existierte schon vorher; nur tatsächliche Einzelabnahmen zählen hier.
- **Live prüfen:** `node TASKS/check-backlog.js` zählt den Status, `node TASKS/check-priorities.js --next=20` validiert alle Ränge und zeigt die nächsten offenen Tasks.
- **Quellenstopps:** 100-mm-/75-mm-Regel, Original-Maße, 92 Schacht-IDs und echte Rohrdurchmesser sind nicht aus dem Chat heraus fachlich bestätigbar. Dennoch können Betonrendering, sichere Demo und Tests sofort umgesetzt werden.

## Ein Task nach dem anderen – Arbeitsregel

1. **Aufgabe wählen:** niedrigsten offenen **Rang** aus `PRIORITY_ORDER.csv` verwenden (nicht kleinste KAB-ID), unter Berücksichtigung von fachlichen Blockern, Bros jüngstem Feedback und notwendigen QA-Tasks. Vorher aktualisierten Repository-Stand sowie den betreffenden Task lesen.
2. **Starten:** Optional ein GitHub Issue mit Task-ID im Titel anlegen (nicht 828 GitHub-Issues auf einmal). Offenes Issue ist die operative Karte mit `in progress` oder `blocked`; die *kanonischen* Task-Checkboxen stehen in diesen Dateien.
3. **Arbeiten:** Genau den abgegrenzten Task liefern. Beim Commit die ID `KAB-0001` o.ä. nennen. Quellannahmen strikt von vermessener oder fachlich bestätigter Geometrie unterscheiden. Bei fachlichen Abhängigkeiten kein Ergebnis fingieren.
4. **Abnehmen:** Nur auf `[x]` wechseln, wenn die **Definition of Done des Arbeitspakets**, der konkrete Task, Tests und soweit relevant die Sichtprüfung erfüllt sind. Commit-/Issue-Nachweis mit dokumentieren. Ein grüner Build ist keine technische Brandschutz- oder Ausführungsfreigabe.
5. **Zählen und priorisieren:** `node TASKS/check-backlog.js` zählt den Live-Status; `node TASKS/check-priorities.js --next=20` prüft alle individuellen Ränge/IDs und listet die nächsten offenen Aufgaben.
6. **Ergänzen:** Weitere Bro-Chats, Bildkorrekturen und neue Konzepte als **neue Task-IDs ab KAB-0885** anlegen, sofern nicht eindeutig bereits abgedeckt. IDs werden nie recycelt. Der Nenner darf über 828 wachsen. Bei Überschneidung bestehende Aufgabe aktualisieren und Quelle verlinken, statt künstliche Duplikate zu zählen.

## Status- und Qualitätsregeln

- `- [ ]` = offen, `- [x]` = erledigt und nachweislich abgenommen. `blocked`/`in progress` gehört ins aktive Issue bzw. eine neben dem Task dokumentierte Notiz, ohne die Checkbox zu schließen.
- Die 97 PDF-V2-Visualtests sind **Tests**, nicht 97 erledigte Tasks. Der abgeschlossene 25/50/75/100-Meilensteinplan war ein Prototyp-Plan, keine 100%ige Konzeptabdeckung.
- Referenzdaten: 13_U_028 PDF V2 = 91 Rohre, 86 Kabelobjekte, 66 unvollständige Endpunktverläufe. Diese Werte sind Prüfgrundlage der **aktuellen Referenz**, nicht Nachweis technischer Richtigkeit.
- Alle im Konzept ungeklärten Abstände, Herstellerwerte, Endpunkte, Platten-/Auflagermaße, 75-mm-/100-mm-Konflikte und externe Prüfungen bleiben **offen**, bis belastbare Quellen/Fachprüfung vorliegen.
- Systematische Änderungen vor jedem GitHub-Commit kritisch gegen Anforderungen und Tests prüfen (Akiyama-Check aus AIContext).

## Quellen und Fortsetzung

- [ROADMAP.md](../ROADMAP.md) · [CONCEPT_AUDIT.md](../CONCEPT_AUDIT.md) · [V2_0_STATUS.md](../V2_0_STATUS.md) · [EUGI_MODE_ACCEPTANCE.md](../EUGI_MODE_ACCEPTANCE.md)
- Für den nächsten Chat: **„Lade Androidbaze/PC/Kabelsalat/TASKS/README.md, prüfe den GitHub-Live-Stand und arbeite die nächste offene, nicht blockierte Aufgabe ab.“**
