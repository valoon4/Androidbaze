# Kabelsalat · KI-Zeitschätzungen je offenem Parent-Task

**Aktuell: 15.11.2026 · 888 Parent-Tickets · 42 erledigt · 846 offen.** Die [846 Einzel-Schätzungen](./TASK_ESTIMATES.csv) sind nach der [kanonischen Priorität](./PRIORITY_ORDER.csv) sortiert. Aktive KI-/GitHub-Arbeitszeit, **keine Liefer- oder Wartezeitzusage**.

| Welle | Offen | Optimistisch | Typisch | Vorsichtig |
|---|---:|---:|---:|---:|
| S0 · Bro/Schacht | 69 | 76,8 h | 119,0 h | 213,5 h |
| S1 · Quellen/Erkennung | 125 | 196,8 h | 301,5 h | 510,8 h |
| S2 · Kabel/Kanäle | 166 | 297,0 h | 462,5 h | 761,8 h |
| S3 · AVL/Berichte | 108 | 211,8 h | 339,0 h | 602,5 h |
| S4 · Editor | 162 | 247,0 h | 368,5 h | 617,3 h |
| S5 · Schachtnetz | 54 | 104,5 h | 160,8 h | 265,3 h |
| S6 · CAD/QA/Betrieb | 162 | 269,8 h | 445,8 h | 744,3 h |
| **Gesamt** | **846** | **1403,5 h** | **2197,0 h** | **3715,3 h** |

*Die Zeiten sind modellierte Brutto-Einzelaufwände und dürfen weder additiv als zuverlässiger Projektplan noch als reale Zeitmessung verstanden werden. 73 offene Tasks haben zusätzliche Abhängigkeiten; Fachfreigaben, Bro-Antworten, Originaldateien und Wartezeit sind nicht eingerechnet. Gemeinsame Implementierung kann den tatsächlichen Aufwand reduzieren.*

## Bros große noch offene Aufgaben

| Task | Typisch | Hauptabhängigkeit |
|---|---:|---|
| KAB-0885 · LR/Kabel kürzer | 1,0 h | Bro prüft Ansicht |
| KAB-0877 · Betonblock | 1,5 h | Sichtabnahme an beiden Schächten |
| KAB-0878 · Betonoptik | 1,25 h | Bros konkrete Farb-/Materialantwort |
| KAB-0879 · proportionale Lochgrößen | 1,5 h | Bro-Sichtvergleich / reale Maßquellen |
| KAB-0881 · PDF-/Planerkennung | 9,0 h | Echte Binär-PDF-/OCR- und Typzuordnung |
| KAB-0882 · Fotoerkennung | 12,0 h | Kalibrierte Originalfotos und Typprüfung |

**Neu technisch abgeschlossen:** KAB-0235–0250 (16 Parent-Tickets, Leerrohr-Editor 2.1: 2D/3D, DN, Fangraster, Gruppen, Wandwechsel, Kollisionsschutz) · [Testbeleg](./ATOMIC/EVIDENCE_2026-11-15_HOLE_EDITOR_GIANT_245.md). Darüber hinaus: KAB-0051–0053 (Rohrabstands-Editor mit Undo/Redo, Kabel-ID-Schutz und Vorher/Nachher) sowie KAB-0884 (Regression), 0886 (DN-Editor), 0887 (fiktive DN-Demo), 0888 (Browser-/Quelltests). [Getrennte technische Abnahme](./BRO_PARENT_TECH_ABNAHME_2026-11-15.md). Dass alle Mikro-IDs dieser Parents abgeschlossen sind, ersetzt keine externe Fach-/Sichtabnahme anderer Tasks.

## Fortschreibung

Aus `PC/Kabelsalat`:
- `node TASKS/estimate-time.js --write` erzeugt die CSV neu.
- `node TASKS/check-estimates.js` prüft Ränge, offene Task-IDs und Schätzwerte.
- `node TASKS/audit-workflow.js` prüft 888 Eltern-Tickets, 13.012 Mikro-Tasks und Belege.

Die CSV enthält **ausschließlich noch offene** Parent-Tickets. Frühere Summen im älteren Sprintbericht sind historisch, nicht der aktuelle Stand.
