# Kabelsalat · KI-Zeitschätzungen je offenem Task

**Basis:** GitHub-Backlog am **13.11.2026** · **888 Tasks insgesamt**, davon **19 abgeschlossen** und **869 offen**. Für **jeden der 869 offenen Tasks** gibt es einen eindeutigen Datensatz in der [vollständigen Schätzliste (CSV)](./TASK_ESTIMATES.csv), **sortiert nach der echten [Prioritätenreihenfolge](./PRIORITY_ORDER.csv)**.

> **Zeitdefinition:** Das sind **grobe Schätzungen meines aktiven ChatGPT-/GitHub-Arbeitsaufwands**, nicht die Arbeitszeit eines menschlichen Entwicklers und **keine Zusage, wie lange eine spätere Lieferung dauert**. Eingerechnet: Aufgabenverständnis, Code-/Quellenarbeit, Tests, GitHub-Commit, kurzer Nachweis und erwartbare Nachbesserung. Nicht eingerechnet: Warten auf Bro, fachliche Freigaben, fehlende Originalpläne, externe Dienste oder Zeit zwischen Chat-Sitzungen. Damit kann ich auch nicht im Hintergrund weiterarbeiten, während du offline bist.

## Gesamtüberblick

| Welle | Noch offen | Optimistisch | Typisch | Vorsichtig |
|---|---:|---:|---:|---:|
| **S0 · Bros neueste Wünsche / 3D** | 92 | 108 h | **168 h** | 295 h |
| **S1 · Quellen, Rohre, Erkennung** | 125 | 197 h | **302 h** | 511 h |
| **S2 · Kabel und Brandschutzkanäle** | 166 | 297 h | **463 h** | 762 h |
| **S3 · AVL0116, 92 Schächte, Berichte** | 108 | 212 h | **339 h** | 603 h |
| **S4 · Editor und Bedienung** | 162 | 247 h | **369 h** | 617 h |
| **S5 · Schachtnetz** | 54 | 105 h | **161 h** | 265 h |
| **S6 · CAD/BIM, QA, Betrieb** | 162 | 270 h | **446 h** | 744 h |
| **Alle offenen Tasks** | **869** | **1.435 h** | **2.246 h** | **3.796 h** |

**Summe = rechnerische Addition von 869 unabhängigen Einzelaufgaben**, **kein Kalender-/Liefertermin**. Weil zusammengehörige Tasks Tests, Einarbeitung und Änderungen teilen, kann gebündelte Bearbeitung erheblich weniger benötigen. Andererseits können unbekannte Originaldaten, echte Fachprüfung, Browserprobleme oder Architekturänderungen die Spanne erhöhen. Die Zahlen sind bewusst **nicht** als zuverlässige Stundenmessung dargestellt.

## Bros wichtigste offenen Tasks zuerst

| Rang | Task | Mein Restaufwand – grob | Wichtige Voraussetzung |
|---:|---|---:|---|
| 1 | **KAB-0885 · LR/Kabel optisch kürzer** | 0,75–2 h (**typisch 1 h**) | Bros Sichtabnahme: Ist das richtige Objekt gemeint? |
| 2 | **KAB-0886 · DN30–160 im Editor** | 0,5–1,25 h (**typisch 0,75 h**) | Schon umgesetzt, Abschlussprüfung |
| 3 | **KAB-0887 · Fiktive DN-Mischung** | 0,5–1,25 h (**typisch 0,75 h**) | Bereits angelegt, Beispiel-/Screenshottest |
| 4 | **KAB-0888 · Browser-/Vergleichstests** | 1,25–3,5 h (**typisch 1,75 h**) | Laufende Chromium-Sichtprüfung |
| 5 | **KAB-0877 · Betonblock mit Löchern** | 1–3 h (**typisch 1,5 h**) | Funktion vorhanden, Bro-Abnahme |
| 6 | **KAB-0878 · dunklere Betontextur** | 0,75–2,5 h (**typisch 1,25 h**) | Farbabgleich mit Bro |
| 8 | **KAB-0879 · variable Rohrgrößen** | 1–3 h (**typisch 1,5 h**) | Reale Maße nicht aus DN erfinden |
| 10 | **KAB-0881 · PDF-Rohrmaßerkennung** | 4,5–18 h (**typisch 9 h**) | Geeignete beschriftete Originalpläne |
| 11 | **KAB-0882 · Foto-Rohrmaßerkennung** | 6–24 h (**typisch 12 h**) | Referenzmaß und kalibrierte Fotos |

Nicht aufgeführte Ränge 7 und 9 sind **bereits erledigt** (KAB-0880 und KAB-0883). Die **CSV enthält ausschließlich offene Aufgaben**.

## So benutzt du die CSV

- **Filter nach** Rang, `KAB-ID`, Welle, Arbeitspaket, Aufgabentyp und `Abhängigkeit`.
- Die drei **Stunden-Spalten sind echte Zahlen** mit deutschem Dezimalkomma; der Trenner ist `;` (Excel-/LibreOffice-freundlich).
- `Optimistisch` = günstige Umsetzung; `Typisch` = momentan sinnvollster Arbeitswert; `Vorsichtig` = aufwendigere Iteration. Keine der Zahlen behauptet eine gemessene Dauer.
- `Abhängigkeit` zeigt **75 Tasks mit zusätzlichen Voraussetzungen** – darunter Browser-Testumgebung (oft bereits verfügbar), originale Plan-/Artefaktquellen, fachliche Rücksprache oder Bros Feedback. **Nicht alle 75 sind blockiert.** Externe Wartezeit ist nicht eingerechnet.
- `Spezifischer Aufwand / Annahme` erklärt den Aufwand je Task. Die komplette ursprüngliche Aufgabe bleibt in `Aufgabe` lesbar.

**Reproduzierbar:** `node TASKS/estimate-time.js --write` aus `PC/Kabelsalat/` regeneriert die Liste nach Backlog-/Prioritätenänderungen. `node TASKS/check-estimates.js` prüft 1:1-Abdeckung, keine erledigten IDs, aufsteigende Schätzbereiche, und identische Beschreibungen und Ränge. Dieser Check ist Teil von `npm test`. Schätzwerte sind eine Planungshilfe; eine Task wird nur mit Umsetzung/Testnachweis im kanonischen Tracker abgehakt.
