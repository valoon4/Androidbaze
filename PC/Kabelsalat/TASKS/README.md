# Kabelsalat · Aufgabensteuerung ab v2.0.1

**Kanonischer Tasktracker:** vier unten verlinkte Markdown-Dateien mit **828 eindeutig nummerierten Einzelaufgaben**, gegliedert in 46 fachliche Arbeitspakete. Erstellt am 10.11.2026 anhand `ROADMAP.md`, `CONCEPT_AUDIT.md`, `V2_0_STATUS.md`, `EUGI_MODE_ACCEPTANCE.md` und der vorhandenen Referenzdaten. **Zusätzliches Feedback von Bro ist noch nicht eingearbeitet.**

| Bereich | Aufgaben | ID-Bereich | Live-Datei |
|---|---:|---|---|
| P0 · Quellen, Schachtwand, Kabelwege, OLA/MS und Pitch | 216 | KAB-0001–KAB-0216 | [01-P0-QUELLE-UND-PITCH.md](./01-P0-QUELLE-UND-PITCH.md) |
| P1 · Frei editierbarer Schacht, 2D/3D, Netz und Dateien | 216 | KAB-0217–KAB-0432 | [02-P1-EDITOR-UND-NETZ.md](./02-P1-EDITOR-UND-NETZ.md) |
| P2 · Geometrieprüfung, Herkunft, Import und Freigabegrenzen | 216 | KAB-0433–KAB-0648 | [03-P2-DATEN-UND-PRUEFUNG.md](./03-P2-DATEN-UND-PRUEFUNG.md) |
| P3 · QA, Security, Performance, Veröffentlichung, Feedback | 180 | KAB-0649–KAB-0828 | [04-P3-QA-BETRIEB-UND-FEEDBACK.md](./04-P3-QA-BETRIEB-UND-FEEDBACK.md) |
| **Gesamt bei Anlage** | **828** | **KAB-0001–KAB-0828** | |

## Initialer Status am 10.11.2026

- **Neu angelegte Tasks:** 828.
- **Als erledigt nachgewiesen:** 0 von 828, da die neuen Aufgaben noch nicht einzeln abgearbeitet oder geprüft wurden. Das ist **keine** Aussage, dass vorher keine Software entwickelt wurde.
- **Offen:** 828. **Nächster Kandidat:** KAB-0001 – PDF-Index für Schacht 13_U_028 eindeutig versionieren. Danach KAB-0002 ff., wobei abhängige oder blockierte Aufgaben dokumentiert übersprungen werden dürfen.
- **Wichtig:** P3 enthält QA/CI/Security, die bereits bei jedem passenden P0/P1/P2-Sprint erledigt werden müssen; Priorität P3 heißt nicht, bis zum Schluss mit Tests zu warten.

## Ein Task nach dem anderen – Arbeitsregel

1. **Aufgabe wählen:** niedrigste sinnvoll bearbeitbare offene ID aus P0, unter Berücksichtigung von fachlichen Blockern, Fehlern aus Bro-Feedback und notwendigen QA-Tasks. Vorher aktualisierten Repository-Stand sowie den betreffenden Task lesen.
2. **Starten:** Optional ein GitHub Issue mit Task-ID im Titel anlegen (nicht 828 GitHub-Issues auf einmal). Offenes Issue ist die operative Karte mit `in progress` oder `blocked`; die *kanonischen* Task-Checkboxen stehen in diesen Dateien.
3. **Arbeiten:** Genau den abgegrenzten Task liefern. Beim Commit die ID `KAB-0001` o.ä. nennen. Quellannahmen strikt von vermessener oder fachlich bestätigter Geometrie unterscheiden. Bei fachlichen Abhängigkeiten kein Ergebnis fingieren.
4. **Abnehmen:** Nur auf `[x]` wechseln, wenn die **Definition of Done des Arbeitspakets**, der konkrete Task, Tests und soweit relevant die Sichtprüfung erfüllt sind. Commit-/Issue-Nachweis mit dokumentieren. Ein grüner Build ist keine technische Brandschutz- oder Ausführungsfreigabe.
5. **Zählen:** `node TASKS/check-backlog.js` aus `PC/Kabelsalat` ausführen. Dieses Skript liest **die echten Checkboxen** und meldet `offen / erledigt / gesamt` sowie doppelte oder fehlende IDs.
6. **Ergänzen:** Bro-Chat, Bildkorrekturen und neue Konzepte als **neue Task-IDs ab KAB-0829** anlegen, sofern nicht eindeutig bereits abgedeckt. IDs werden nie recycelt. Der Nenner darf über 828 wachsen. Bei Überschneidung bestehende Aufgabe aktualisieren und Quelle verlinken, statt künstliche Duplikate zu zählen.

## Status- und Qualitätsregeln

- `- [ ]` = offen, `- [x]` = erledigt und nachweislich abgenommen. `blocked`/`in progress` gehört ins aktive Issue bzw. eine neben dem Task dokumentierte Notiz, ohne die Checkbox zu schließen.
- Die 97 PDF-V2-Visualtests sind **Tests**, nicht 97 erledigte Tasks. Der abgeschlossene 25/50/75/100-Meilensteinplan war ein Prototyp-Plan, keine 100%ige Konzeptabdeckung.
- Referenzdaten: 13_U_028 PDF V2 = 91 Rohre, 86 Kabelobjekte, 66 unvollständige Endpunktverläufe. Diese Werte sind Prüfgrundlage der **aktuellen Referenz**, nicht Nachweis technischer Richtigkeit.
- Alle im Konzept ungeklärten Abstände, Herstellerwerte, Endpunkte, Platten-/Auflagermaße, 75-mm-/100-mm-Konflikte und externe Prüfungen bleiben **offen**, bis belastbare Quellen/Fachprüfung vorliegen.
- Systematische Änderungen vor jedem GitHub-Commit kritisch gegen Anforderungen und Tests prüfen (Akiyama-Check aus AIContext).

## Quellen und Fortsetzung

- [ROADMAP.md](../ROADMAP.md) · [CONCEPT_AUDIT.md](../CONCEPT_AUDIT.md) · [V2_0_STATUS.md](../V2_0_STATUS.md) · [EUGI_MODE_ACCEPTANCE.md](../EUGI_MODE_ACCEPTANCE.md)
- Für den nächsten Chat: **„Lade Androidbaze/PC/Kabelsalat/TASKS/README.md, prüfe den GitHub-Live-Stand und arbeite die nächste offene, nicht blockierte Aufgabe ab.“**
