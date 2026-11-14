# Kabelsalat · Bros Nachtrag: kürzere LR-Darstellung und DN 30–160

**Eingang:** Originalfeedback, 12.11.2026: „die müssen etwas kürzer ... verschiedene Leerrohre (LR) ... DN 30 bis DN 160 ... DN 50 entsprechend [kleiner], DN 110 entsprechend [größer]“. Vorrangiger Bro-Bedarf. Dies ist ein **Praxistest-Wunsch zur Ansicht** und kein neues verbindliches Vermessungs- oder Normdokument.

**Technische Interpretation der Kürze:** Die einzige bisher stark herausstehende 3D-Komponente am unbelegten Ende sind die **360-mm-Kabelstutzen für Kabel mit nur einem bekannten Anschluss**. Diese werden auf 160 mm gekürzt. Betonwandtiefe und echtes LR-Längenmaß bleiben **unverändert**, denn sie sind nicht bestätigt. Der Bro prüft visuell, ob das seinem gemeinten „kürzer“ entspricht.

## P0-18 · Kurze Vorschau, LR-DN und gemischte Betonlöcher (KAB-0885–KAB-0888)

**Definition of Done:** DN30 ist sichtbar kleiner als DN50, DN50 sichtbar kleiner als DN110 und DN160 am größten; echte PDF-/HTML-Schachtkoordinaten, Kabelendpunkte und Quellen bleiben identisch; das gemischte Größenbeispiel ist klar **fiktiv**; keine DIN-, VDE- oder Ausführungsaussage.

- [ ] **KAB-0885** – Freie Kabel-Endmarkierungen an nur einseitig bekannten Leerrohren in der 3D-Vorschau von 360 auf 160 mm verkürzen und ihre Stärke an kleine Rohröffnungen anpassen; echte Rohr-/Wandlängen nicht ohne Aufmaß ändern und Optik von Bro abnehmen lassen
- [ ] **KAB-0886** – Benutzereditierbare LR-Nennweiten DN 30 bis DN 160 in 1-mm-Schritten bereitstellen und Lochkreise in 2D/3D proportional rendern, ohne vorhandene Alt- oder Quellwerte stillschweigend zu begrenzen
- [ ] **KAB-0887** – Separaten ausdrücklich fiktiven Testschacht mit gemischten DN 30, 40, 50, 60, 63, 75, 90, 110, 116, 125, 140 und 160 für Bros visuellen Größenvergleich bereitstellen, ohne Original-Schächte zu ändern
- [ ] **KAB-0888** – Automatisierte Vergleichs- und Browseransicht für DN 30/50/110/160, verkürzte nur schematische Endmarkierungen, JSON-Roundtrip und 91-Rohr-PDF-Quellintegrität anlegen

**Verweise:** Bestehende KAB-0877 (Beton mit ausgeschnittenen Löchern), KAB-0878 (dunkle Betonoptik), KAB-0879 (Größen proportional), KAB-0880 (getrennte Innen-/Außen-/Lochmaße), KAB-0883 (Maßangaben mit Quellenstatus). Diese Tasks werden nicht erneut angelegt.

**Unveränderliche Regel:** DN/Nennangabe ist nicht automatisch der reale Außen- oder Lochdurchmesser. `dimensionSpec.openingMm` kann die Anzeigegröße bei einer dokumentierten physikalischen Öffnung explizit ersetzen. Die bestehenden `13_T_021` / `13_U_028` Referenzen dürfen durch die neue fiktive Vergleichsprobe nicht verschoben werden.

## Visuelle Präzisierung nach Bros Folgefeedback · 12.11.2026

Bro: „ja bruder sieht schon besser aus aber diese lr die da drin sind wieso sind die so lang“. **Ursache des sichtbaren Eindrucks:** In der 3D-Darstellung liefen lange farbige `TubeGeometry`-*Kabelwege* bereits in der Schachtübersicht; diese können wie längliche LR aussehen. Die **LR sind tatsächlich Kreisöffnungen in der Betonwand**. Der vorherige Fix 360→160 mm kürzte nur einen Endmarker bei einseitig bekannten Kabelanschlüssen, **nicht** diese durchgehenden farbigen Linien.

**Neuer Code in v2.0.6-beta.1:** Basic-Schachtansicht zeigt nur Beton und Rohrdurchbrüche; Kabelwege erscheinen erst in der klar beschrifteten Kabelansicht. Das ist eine **nicht destruktive Ansichtskorrektur**, keine physische Rohr-Längenänderung und keine Rohrverschiebung. Der visuelle Schritt ist implementiert, aber **KAB-0885 bleibt bis Bros Sichtabnahme offen**. Referenzdaten, Wandstärken und Kabelrouten sind nicht verändert.

## Synthetische Zusatzregression · 14.11.2026

- **KAB-0887:** [Tests mit 12 Demo-Nennweiten, fehlender Quelle und zweiter Fixture](../tests/sprint-30-2026-11-14.test.js) ergänzen den bestehenden [DN-Vergleichstest](../tests/bro-dn-range.test.js). Die ausdrücklich fiktive Demo und die PDF-V2-Referenz bleiben getrennt.
- **KAB-0886/0888:** Größenrelation DN 50 zu DN 110 und unveränderte 91 PDF-Referenzöffnungen sind als Unit-Assertions hinterlegt. Das ist **keine** Browser-Sichtabnahme.
- **Noch offen:** Bros Prüfung der Optik, 2D-/3D-Sichtvergleich, physikalische Öffnungsmaße, ein möglicher Referenzvergleich anhand unabhängiger Originalunterlagen. Kein Eltern-Ticket hiermit automatisch abgeschlossen.

## Testversion 2.0.7-beta.1 · 14.11.2026

**KAB-0888 – größerer Browser-/3D-Arbeitsschritt:** [Echter Chromium-Test mit DN30/50/110/160, 2D/3D, Kabel-/LR-Ansicht, Profi-Editor und JSON-Wiederherstellung](../tests/browser-visual.js). Der Test schreibt separate **fiktive** Vergleichs-Screenshots in das GitHub-Actions-Artefakt `Kabelsalat-1.9.2-visuelle-Pruefung` und stellt danach die 91-LR-PDF-V2-Referenz wieder her. [Visual-Workflow](https://github.com/valoon4/Androidbaze/actions/workflows/kabelsalat-visual.yml).

**Abnahmefrage für Bro (Issue [#72](https://github.com/valoon4/Androidbaze/issues/72)):** Sind die vermeintlich langen „LR“ in der **Basic-Schachtansicht** jetzt korrekt verschwunden, sobald lange **Kabelwege** in die separate Kabelansicht verschoben werden? Oder meinst du echte Rohrlängen **innerhalb der Betonwand**? Letztere sind ohne Originalmaße weiterhin nicht verändert.

**Noch offen:** Fachliche/visuelle Freigabe von Bro, Hersteller-/Originalmaße und exakte PDF-Binärmaß-Erkennung. Der Nachweis einer realen CI-Browserausführung zählt erst bei einem erfolgreichen Workflow.
