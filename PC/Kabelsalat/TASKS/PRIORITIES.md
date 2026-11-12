# Kabelsalat · priorisierter Gesamtplan nach Bros Feedback · 11.11.2026

**Entscheidung:** Priorität richtet sich ab jetzt nach Bros tatsächlicher praktischer Nutzung, vor allem den **jüngsten** Beanstandungen (Betonblock zu hell / Rohröffnungen wirken künstlich / Größen 60, 110, 116 und automatische Rohrmaß-Erkennung). Die alte KAB-ID ist eine **dauerhafte Identität**, **nicht** mehr die Abarbeitungsreihenfolge.

**Zusätzlich:** [KI-Zeitbedarf für alle aktuell 869 offenen Tasks](./TASK_ESTIMATES.csv) · [Aufwand nach Prioritätswelle und Annahmen](./TASK_ESTIMATES.md). Es handelt sich um **grobe aktive ChatGPT-Arbeitszeit pro Task**, keine Zusage zu Lieferung oder externen Prüffristen.

**Vollständige globale Rangliste aller 888 Aufgaben:** [PRIORITY_ORDER.csv](./PRIORITY_ORDER.csv) (sortiert nach `Rang`, jeder Task genau einmal; sieben priorisierte Arbeitswellen S0–S6). **Live-Fortschritt + nächste offene Aufgaben:** `node TASKS/check-priorities.js --next=20` aus `PC/Kabelsalat/`. Die Checkboxen und Quellbeschreibungen bleiben in den acht einzelnen Backlogdateien kanonisch.

## Neuester Stand · DN30–160 und kürzere Endmarkierungen (12.11.2026)

**Top 4 der Gesamtrangliste jetzt KAB-0885 → 0886 → 0887 → 0888**, direkt nach Bros erneutem Praxistest: Endmarkierungen kürzer, DN30/50/110/160 visuell proportional, separate fiktive Muster-Demo, Roundtrip-/Browser-Regression. [Taskdetails](./08-BRO-KURZE-LR-DN30-160.md). Die folgenden historischen Start-Ränge werden in der CSV jetzt um vier Plätze nach hinten verschoben; die **stabile Task-ID** ändert sich nicht. Die vier neuen Aufgaben bleiben bis zur Code-/Visualprüfung offen. **Gesamtstand 888**, nicht 884.

## Warum diese Reihenfolge?

1. **Bros aktuellstes Praxisfeedback zuerst:** Der Schacht soll als normaler mittel-/dunkelgrauer Betonblock mit runden Löchern funktionieren, nicht als zu helles Rohrbündel. Größen müssen erkennbar unterschiedlich sein. **Es geht um korrektes, vorzeigbares Rendering statt bloß kosmetischer Tricks.**
2. **Maßdaten verständlich und ehrlich:** DN/Nennweite, Innen-/Außendurchmesser und Lochmaß dürfen nicht verwechselt werden. Der Bro nennt 60/110/116 als Beispiele – ihre tatsächliche Verwendung im Bestand ist nicht belegt. Die KI soll Dimensionen aus einer **beschrifteten Quelle oder messbaren Referenz** vorschlagen, nicht aus nichtmaßstäblichen Bildpixeln behaupten.
3. **Bestehende Rohrpositionen und Wände bewahren:** Beide Schächte `13_T_021`, `13_U_028` müssen richtige Wandausrichtung und Rohrbelegung behalten. Die aktuelle 62-%-Demo-Verdichtung muss als optischer Effekt vom Fachmodell getrennt sein.
4. **3D-Kabel / Brandschutz nachrangig zur gesicherten Geometrie:** Gerade gegenüberliegende Kabel, großzügige seitliche Bögen, Einhausung ohne Fremdrohre. A/B ist höhenabhängig und **nicht** automatisch OLA=A/MS=B. Die konkurrierenden Grenzangaben 75/100 mm und fehlende reale Schachtmaße bleiben dokumentierte Blocker für technische Aussagen.
5. **AVL0116-92-Schacht-Audit und Profi-Berichte danach:** Rechteckschemata, reale Nachweise und PDF/Excel sind wichtig; die alten 92 Schachtfälle, 1.957 Planseiten und 35/276 Abbildungen aus dem Chat sind **unabhängig zu verifizieren**, bevor man technische Bewertungen als erfüllt ausgibt.
6. **Netzweit skalieren, CAD/BIM, Mehrbenutzer und Performance später:** Erst die Schachtdarstellung und Datenqualität stabilisieren, dann ausbauen.

## Historische Startreihenfolge vor Bros jüngstem DN-Nachtrag

| Rang | Task | Umsetzung | Quellabhängigkeit |
|---:|---|---|---|
| 1 | **KAB-0877** | **Betonblock mit runden Durchbrüchen** statt isolierten Einzelrohr-Ringen rendern | Vorhandene Demo reicht für visualisierte Erstabnahme |
| 2 | **KAB-0878** | Beton realistischer, mittel- bis dunkelgrau, deutlich weniger hell | Sichtprüfung mit Bro |
| 3 | **KAB-0880** | DN/Nennweite, Innen-, Außendurchmesser, Lochmaß und Herkunft getrennt führen | Bestehende Referenzdaten; fehlende Werte offen lassen |
| 4 | **KAB-0879** | Tatsächlich unterschiedlich große Öffnungen aus angegebenen/gesicherten Maßwerten anzeigen | Fehlende reale Durchmesser bleiben Platzhalter |
| 5 | **KAB-0883** | Rohrgröße in Tooltip, 2D/3D und Eigenschaften eindeutig kennzeichnen | Kein unbestätigter Wert als real |
| 6 | **KAB-0881** | KI-/PDF-/Planerkennung für Rohrzahl, Rohrtyp und Größenbezeichnungen 60/110/116 etc. | Braucht geeignete beschriftete Originalpläne oder explizite Testfixtures; ohne Maßstab keine exakten Pixel-Ø |
| 7 | **KAB-0882** | KI-/Fotoerkennung mit Größen-/Typkandidaten und manueller Bestätigung | Für absolute Millimeterwerte Referenzmaß oder Beschriftung nötig |

**Frühe Pflichtbegleiter (als Nächstes nach obiger Prioritätenfolge in S0):** KAB-0833/0834 (beide Schachtquellen konservieren), KAB-0832 + KAB-0037–0039 (62-%-Demo vom Fachmodell entkoppeln), KAB-0019–0022 (Wandorientierung), KAB-0054 (Bros 3D-Abnahme), KAB-0884 (gemischte Maße + Betonlook regressionssicher testen). Diese QA-Arbeit darf zeitgleich mit der Feature-Arbeit erfolgen – die Rangliste ist keine Aufforderung, Tests bis zum Schluss zu verschieben.

## Alle 888 Tasks sind priorisiert

| Welle | Aufgaben | Position | Inhalt / Ziel |
|---|---:|---:|---|
| **S0 · Bro jetzt** | **110** | 1–110 | Jüngste Beton-/DM-Forderungen, sichtbare Wandbelegung, sicherer 3D-Pitch und dazugehörige Soforttests |
| **S1 · Referenzen & Erkennung** | **126** | 111–236 | PDF-Wand-/Rohrbelegung, Kabelzuordnungen, Rohrtypen, Import-/Bildkandidaten |
| **S2 · Kabel & Kanäle** | **166** | 237–402 | Kabelführung, Mindestbiegeradien, Platten, Kollisionen, Varianten A/B |
| **S3 · AVL & Profi-Berichte** | **108** | 403–510 | Historische AVL-Neuprüfung, 92-Schacht-Datensätze, Rechteckschemata, PDF-/Excel-Prüfbefunde |
| **S4 · Editor & Bedienung** | **162** | 511–672 | 2D/3D-Bearbeitung, flexible Wände, Projektspeicherung, Navigation |
| **S5 · Schachtnetz** | **54** | 673–726 | Globale Schächte, Netzlinks, physische Kabelkontinuität |
| **S6 · Ausbau & Betrieb** | **162** | 727–888 | CAD/BIM, Rollen, Performance, allgemeine QA/Sicherheit, Doku und Release-Betrieb |
| **Gesamt** | **888** | **1–888** | Jeder Task hat einen eindeutigen Rang; kein Task gelöscht |

**Wichtig:** Die **S0–S6-Wellen sind Geschäfts-/Umsetzungsprioritäten**, nicht die alten Entwicklungslabel `P0–P3` in den Taskdateien. Die Reihenfolge ist ein Plan, **kein behaupteter Entwicklungsfortschritt**. Test-, Security- und Freigabeschritte werden *immer* als passende Abnahmekriterien der jeweiligen Featureaufgabe mitgezogen, auch wenn eine eigene QA-Task später in der CSV steht. Ein Task mit externer Quelle darf bis dahin offen/blockiert bleiben, ohne den restlichen Sprint aufzuhalten.

## Blocker, die die Arbeit nicht unnötig stoppen sollen

- **100 mm versus 75 mm (KAB-0830, KAB-0838):** Beides als ungeklärt führen. Keine automatische technisch abgenommene A/B-Wahl vor Klärung.
- **Unbekannte reale Rohrmaße (KAB-0880–0882):** Demo und manuelle Bestätigung möglich; keine 60/110/116-mm-Klassifikation aus einem bloßen Foto ohne Maßstabsbezug behaupten.
- **92-Schacht-Originalregister / AVL-Wortlaut (KAB-0841–0843, KAB-0867):** Bis zur Vorlage keine vollständige fachliche AVL-Freigabe. Berichtsgestaltung darf parallel mit Testdaten entstehen.
- **Schacht-V2-Quellkonflikte (KAB-0001, KAB-0071):** Bis zur überprüften Quelle bleiben Anschlusskonflikte offen; Kabelenden nicht erfinden.

## Nächste Arbeit in GitHub

1. **KAB-0877** als nächstes aktives GitHub-Issue bearbeiten: realistische, massive Betonoberfläche mit kreisförmigen Öffnungen für die bestehende Demo.
2. Danach KAB-0878 und KAB-0880, dann KAB-0879 und KAB-0883; bei notwendiger Quellenabhängigkeit KAB-0881/0882 als offenes Feature vorbereiten.
3. Pro Änderung: Source-SHA lesen, Akiyama-Check, Code/Test/Screenshots, Rückmeldung des Bros, dann Checkbox inkl. Nachweis abhaken.
4. Vor jedem Folgesprint `node TASKS/check-priorities.js` benutzen: niedrigste **offene, praktisch umsetzbare** Priorität zuerst. Die globalen Ränge bleiben stabil, selbst wenn neue Tasks ab **KAB-0889** folgen.

**Historischer Status beim Erstellen:** 0 von 884 in den Taskdateien als erledigt markiert. Das ist kein Urteil über die bereits vorhandene Software, sondern über die **noch nicht einzeln abgeschlossenen** Akzeptanzaufgaben.

## Fortschritt nach Bros 10er-Sprint · 12.11.2026

[Ausführlicher Bericht](./SPRINT_2026-11-12_BRO_10.md) · **10 abgeschlossene, nachgewiesene Tasks / 874 offene / 884 gesamt**. Bisher wurden KAB-0880, KAB-0883, KAB-0832–0834, KAB-0037–0039, KAB-0049 und KAB-0050 abgeschlossen. **KAB-0877/0878/0879** warten trotz funktionierender Rendering-Implementierung auf Bros visuelles Feedback. **KAB-0881/0882** sind nur konservative PDF-Text- bzw. Foto-Kandidatenprototypen, noch keine vollständige KI- oder Dokumentenerkennung. **KAB-0884** benötigt die Endkontrolle des zusätzlichen realen Browser-Screenshots mit synthetischen 60/110/116-mm-Öffnungen.

## Fortschritt nach zweitem Bro-Fokus-Sprint · 12.11.2026

[Details: Koordinaten / Rohrabstände / rote Kollisionen](./SPRINT_2026-11-12_BRO_WALL_10.md) · **19 Tasks abgeschlossen / 865 offen / 884 gesamt**. Für KAB-0019–0023 sind die vier korrekten PDF-V2-Schachtwände samt Weltnormalen, Innenkamera, rechtshändiger Achsdefinition und inverser 2D-/3D-Abbildung anhand der bestehenden Quellen getestet. KAB-0044–0047 berechnen korrekte schematische Leerräume und Wandrandabstände für die Sonder-DN. KAB-0048 ist im 2D-/3D-Renderer implementiert, bleibt bis zum realen visuellen Nachweis offen. Der Gesamtplan bleibt auf Bros Praxiswünsche ausgerichtet: **Quellenmaßangaben dürfen nie automatisch als Vermessungswerte gelten.**
