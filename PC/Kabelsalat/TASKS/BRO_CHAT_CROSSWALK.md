# Kabelsalat · Quellabgleich Bro-Gespräch (11.11.2026)

**Eingang:** Nutzerupload `dumm.txt` mit 138 Textzeilen (Datei-Hash SHA-256 `80ac4b257e8d0bfd3f2e0c75da329dcacea9a0748d22bea2a4c0bee55fc916ad`). Die Datei ist **kein Original der AVL0116**, keine Originalplan- oder Herstellerquelle und enthält neben den Nutzeranweisungen auch unbelegte Aussagen eines früheren Assistenten. Diese Zusammenfassung übernimmt ausschließlich die fachlich erforderlichen Punkte, nicht die gesamte Gesprächskopie. **Dateinamen im Gespräch sind Hinweise auf benötigte Quellen; die Dateien selbst lagen dem Abgleich nicht bei.**

## Zwei unterschiedliche Arbeitsstränge sauber trennen

| Strang | Anliegen aus dem Upload | Datenlage / Grenzen | Rückverweis |
|---|---|---|---|
| AVL0116-Schachtprüfung | Nachweis zu (angeblich) 92 OLA-Schächten aus (angeblich) 1.957 Planseiten, 15-kV-Kabel, LST/TK und Schutztrennung; revidierte Berichte | 92 und 1.957 wurden im Altchat genannt, nicht mit Originaldateien nachgezählt; ursprüngliche AVL0116 im Altchat ausdrücklich nicht vollständig vorhanden | Neu KAB-0841–0852; bestehend KAB-0469, KAB-0490, KAB-0496, KAB-0479 |
| 3D-Modelle | Technische Korrektur der **beiden bestehenden** Modelle 13_U_028 und 13_T_021, ohne Leerrohr-/Kabel-/Schachtbauteile zu erfinden oder vorhandene Rohre zu verschieben | Maße sollen nachgeliefert werden; aktuelle Koordinaten sind schematisch. In v2.0.1 sind PDF-Originalpunkte als `sourceU/sourceV` erhalten, die Demo-`u/v` aber um 62 % verdichtet | Neu KAB-0829–0840; bestehend KAB-0037–0054, KAB-0091–0126, KAB-0127–0198 |
| Präsentation | Professionelle, sachlich benannte PDF-/Excel-Auswertung, Rechteckdarstellung je Schacht mit Kabelwegen, Querschnitten und Wandbildern | Die im Gespräch behaupteten 35 Querschnitte bzw. 276 Excel-Abbildungen und frühere Berichtsdateien sind **nicht** als Assets verifiziert | Neu KAB-0853–0864; bestehend KAB-0613–0630, KAB-0791 |

## Anforderungen, die bereits im 828er-Backlog steckten

- Gerade Verbindungen gegenüberliegender Rohre und großzügige Bögen bei seitlichen Rohren: KAB-0091–0108; technisch gesicherter Biegeradius und Herstellerdaten: KAB-0109–0126.
- Ausreichend freie Fremdrohre, separate OLA/MS-Kanäle und Kabeldurchführungen: KAB-0137–0180, KAB-0451–0486.
- Rohr-Maße, unveränderte Quellpunkte, PDF-Wände und explizite Unsicherheit: KAB-0001–0054, KAB-0487–0504.
- Quellenimport, Änderungen und Prüfung: KAB-0505–0648; visuelle QA für beide 3D-Modelle: KAB-0649–0684.
- Quellgetreuer visueller Schachtplan, Druck/Viewer/Excel-Ausgabe: größtenteils KAB-0271–0288, KAB-0613–0630.

**Darum keine doppelten neuen Aufgaben** für „Kabel gerade führen“, „Fremdleerrohre freihalten“, „Kanalplatten bearbeiten“ oder „Biegeradius messen“ angelegt. Die neuen Tasks spezifizieren konkrete bisher fehlende **Fallunterscheidungen, Fehlerfälle und Lieferformate**.

## Kritische fachliche Abweichungen / Entscheidungssperren

1. **A/B ist eine Höhenentscheidung, keine Gewerkzuordnung.** Upload: Variante A bis einschließlich **10 cm** von Schachtboden zu *Unterkante* Leerrohr, waagerecht gestapelte Platten; Variante B bei **mehr als 10 cm**, senkrechte Stützen (beispielhaft 3–4). Das bestehende v2.0.1-Schaubild nutzt **OLA/Rot=A, MS/Blau=B**. Beides kann als historischer Demo-Fall dargestellt werden, aber nicht als allgemeine Bauregel gelten. Neu KAB-0829.
2. **100 mm gegen 75 mm ungeklärt.** In `V2_0_STATUS.md` ist eine neuere 75-mm-Bildvorgabe vom 09.11.2026 dokumentiert, während der hier gelieferte ältere Text 100 mm vorgibt. Weder die neuere Bilddatei noch eine fachliche Bestätigung liegen in diesem Abgleich vor. **Nicht** einfach 75 oder 100 als technisch verbindlich deklarieren. Neu KAB-0830, KAB-0838.
3. **Keine eigenmächtige Rohrverschiebung.** Der Upload verbietet Veränderungen bestehender Leerrohrpositionen ohne echtes Aufmaß. Das v2.0.1-Demo verdichtet `u/v` auf 62 % und erhält Quellpunkte zusätzlich unter `sourceU/sourceV`. Solche Demo-Koordinaten müssen als **rein visuelle Transformation** von Modell-/Quellkoordinaten getrennt werden, bevor technische Geometrie oder Kabelwege damit bewertet werden. Neu KAB-0832, bestehend KAB-0038/0039 (präzisiert).
4. **Kanal dem Kabelweg anpassen.** Nicht Kabel zur Kanalgeometrie knicken. Gemeinsame und getrennte Leerrohre sowie Höhenversätze dürfen nicht geraten werden. Neu KAB-0839, bestehend KAB-0091–0126.
5. **Kein pauschales „AVL erfüllt/nicht erfüllt“.** Der historische Neuprüfbericht meldet frühere Fehleinschätzungen: `13_F_041/13_G_011`, `13_W_027/13_W_033`, `11_A_011`, `12_A_001.1–.3` sowie zehn Schächte ohne nachgewiesene 15-kV-Kabel. Das sind **erneut zu prüfende Hinweise**, keine hier bestätigten Messergebnisse. Neu KAB-0844–0852.

## Berichtsgestaltung aus dem Chat

- Der Bro bewertet die **rechteckige Schachtskizze mit farbigen Kabelverbindungen** positiv; Überkreuzungen/Aufstiege müssen ausdrücklich schematisch bleiben, solange der Plan keine 3D-Verlegung belegt.
- Überschrift **„Kabelverbindung im Schacht“** beibehalten; Rubrik **„Querschnitt und Prüfbefund“** ohne „korrigiert“.
- **Rot** = gemeinsames physisches Leerrohr, **Orange** = gemeinsame Trasse / getrennte Leerrohre, **Grün** = nur nachgewiesene AVL-Konformität, **Neutral** = Nachweis offen; Farbwörter sind **nicht** als Überschriften gedacht. Ein rotes Feld bedeutet nicht automatisch einen abschließend nachgewiesenen Regelverstoß ohne Prüfung des verbindlichen Wortlauts.
- Professionelle PDF-/Excel-Ausgabe mit neutralem Dateinamen, filterbarem Schachtregister, Planverweisen und konsistenten Bildern. Die angeblich im Altchat angehängten PDF-/XLSX-Dateien wurden **nicht** technisch geprüft und sind keine Grundlage für einen als erledigt markierten Task.

## Priorisierte Arbeitsfolge

1. **Sofort zu klären:** KAB-0829–0832 (A/B, 75/100-mm, Rohrkoordinaten) vor der nächsten fachlich gemeinten 3D-Demo. Ein unverändertes Demomodell darf weiter existieren, solange es deutlich schematisch bleibt.
2. **Quellen sichern:** KAB-0841–0843 (Originalplansatz, Original-AVL, frühere Excel-Liste). Ohne diese Quellen keine endgültige Statusfarbe Grün.
3. **Schemata liefern:** KAB-0853–0864 auf zunächst einem Schacht prüfen; anschließend auf alle nachweisbar verfügbaren Schächte skalieren.
4. **Nach jedem Sprint:** bestehende KAB-0793–0810 (Feedback) und automatisierte Tests/Visualläufe berücksichtigen.

**Erledigungsstand durch diesen Quellabgleich:** Es wurden Aufgaben ergänzt und präzisiert; **keine** fachliche Implementierungsaufgabe gilt damit als abgeschlossen.
