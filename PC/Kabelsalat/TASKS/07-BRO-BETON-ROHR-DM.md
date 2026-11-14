# Kabelsalat · jüngstes Bro-Feedback: Betonblock und Rohrdurchmesser

**Eingang:** Eugis Wiedergabe von Bros Praxistest am 11.11.2026: Rohre wirken unnatürlich; Ziel ist ein klassischer, weniger heller Betonblock mit runden, unterschiedlich großen Löchern. Die KI soll Rohrarten und Rohrmaße (beispielhaft 60, 110, 116) erkennen. **Bei Erfassung waren alle acht Aufgaben offen.** Den Live-Status zeigen die Checkboxen sowie [Sprintbericht](./SPRINT_2026-11-12_BRO_10.md) und [Gesamtzähler](./README.md).

**Abnahmeregel:** 60/110/116 sind vom Bro genannte Beispiele, keine bestätigten Bestandswerte. DN (Nennweite), Rohr-Innen-/Außendurchmesser und Lochdurchmesser sind technisch zu unterscheiden. Bild-/PDF-Pixelgröße allein ohne Maßreferenz liefert keine echte Millimeterdimension. Originalpläne, Rohr-IDs, Positionen und Kabelanschlüsse nicht unbemerkt verändern.

## P0-15 · Betonoptik und unterschiedlich große Öffnungen (KAB-0877–KAB-0879)

**Definition of Done:** Sichtprüfung mit Bro an beiden Referenzschächten, ohne Rohrkoordinaten oder Belegungen zu mutieren.

- [ ] **KAB-0877** – Beton-Schachtwand als zusammenhängenden massiven Block mit echten runden Durchbrüchen rendern statt hervorstehenden isolierten Rohrringen; beide Referenzschächte prüfen
- [ ] **KAB-0878** – Schachtblock mit klassisch mittel- bis dunkelgrauem Betonmaterial (weniger hell) und dezenter Oberflächenstruktur darstellen; Farbgebung in Tages-/Nachtansicht gegen Bro-Feedback prüfen
- [ ] **KAB-0879** – Kreisöffnungen proportional zu dokumentierten Rohrdurchmessern darstellen; unterschiedliche Öffnungsgrößen (z. B. 60, 110, 116 mm als Testwerte) ohne fingierte Originalmaße erkennbar machen

## P0-16 · Durchmesser-Datenmodell und prüfbare Erkennung (KAB-0880–KAB-0883)

**Definition of Done:** Automatische Kandidaten sind quellengebunden, falsche Maße korrigierbar und ohne Nutzerbestätigung nicht als echte Maße klassifiziert.

- [x] **KAB-0880** – Rohr-Nennweite DN, Innendurchmesser, Außendurchmesser, erkannte Maßangabe und bestätigte Maßangabe mit Einheit und Quellenstatus getrennt modellieren; DN und tatsächliches Lochmaß nicht gleichsetzen · Nachweis: https://github.com/valoon4/Androidbaze/commit/78968adfc6f684890ef13976ceb429ed1d676ea6
- [ ] **KAB-0881** – PDF-/Planerkennung: Rohranzahl, Rohrtyp und Maßangaben wie 60, 110, 116 aus Dokumentbeschriftung als Kandidaten mit eindeutiger Rohrzuordnung erkennen; ohne Maßstab keine Durchmesser aus Pixeln behaupten
- [ ] **KAB-0882** – Foto-/Bilderkennung: Öffnungsgröße und Rohrtyp mit explizitem Maßstab/Referenzkörper oder sichtbarer Beschriftung als prüfbare Kandidaten vorschlagen; ohne Referenz nur relative Größen schätzen
- [x] **KAB-0883** – Erkannte, manuell gesetzte und bestätigte Rohrdimension mit Einheit und Quelle im Tooltip, Eigenschaftenpanel, 2D-Wandplan und 3D-Inspektion gut lesbar anzeigen · Nachweis: https://github.com/valoon4/Androidbaze/commit/78968adfc6f684890ef13976ceb429ed1d676ea6

## P0-17 · Darstellung und Erkennung dauerhaft absichern (KAB-0884)

**Definition of Done:** Wiederholbare automatisierte Tests und Screenshots einschließlich fehlender Quellmaße und großer/kleiner Öffnungen.

- [ ] **KAB-0884** – Automatisierte Render- und Datenregression für Betonblock mit gemischten Öffnungen (60/110/116 als Testdaten), korrekte DN-/DM-Trennung, Schatten/Helligkeit und geschützte Originalrohrpositionen aufbauen

**Verwandt, nicht dupliziert:** KAB-0037–0054 (unveränderte Originalkoordinaten), KAB-0235–0252 (Rohrmodell), KAB-0523–0558 (Plan-/Bilderkennung), KAB-0829–0840 (fachliche Bauartlogik). Die hier neuen Tasks sind die expliziten Abnahmekriterien aus dem **jüngsten** Feedback.

## Zwischenstand KAB-0877 · 12.11.2026

Die neue Darstellung der zusammenhängenden Betonwand mit durchgängigen ausgeschnittenen Öffnungen, entfernten hervorstehenden Demo-Hülsen und weiterhin anklickbaren Rohr-IDs ist **implementiert** in `concrete-wall.js` und `app.js`. Für `13_T_021`, `13_U_028` HTML und PDF V2 existiert der neue automatisierte Geometrie-/Unverändertheitstest `tests/concrete-wall.test.js`; die 3D-Browserprüfung läuft zusätzlich über GitHub Actions. **KAB-0877 bleibt offen**, bis die Sichtprüfung mit Bros Rückmeldung und die Build-/Browser-Ergebnisse nachweisbar bestanden sind. [Issue #70](https://github.com/valoon4/Androidbaze/issues/70).

## Entwicklungssprint vom 12.11.2026 · v2.0.3-beta.1

- **KAB-0880 und KAB-0883:** Quellgebundenes Rohrmaßmodell und Größen-/Statusanzeige in Profi-Editor, 2D und 3D sind implementiert, per automatisierten Tests abgenommen. Echte physische Daten sind weiterhin keine automatisch bestätigten Maße.
- **KAB-0878 und KAB-0879:** Dunklere texturierte Betonwände und unterschiedliche Lochgrößen sind technisch umgesetzt. **Sichtabnahme von Bro und reale Maßquellen fehlen weiterhin**; beide Aufgaben bleiben offen.
- **KAB-0881:** Lokale Erkennung von DN/DM/Ø aus klar beschriftetem TXT-Plantext mit Manueller Prüfung – **keine automatische PDF-Binärverarbeitung/OCR/KI**, deshalb nicht vollständig erledigt.
- **KAB-0882:** Offline-Kreiserkennung aus Foto mit relativer Größe oder ausdrücklich kalibriertem Referenzkreis; **nur Kandidaten, kein trainiertes KI-Modell und keine sichere Rohrtypklassifikation**. Task bleibt offen.
- **KAB-0884:** Automatisierte 60/110/116-mm-Regression und Browser-Screenshot eines **ausdrücklich synthetischen Beispiels** ergänzt. Endabnahme erfolgt erst nach abgeschlossenem echten Chromium-Browserlauf. [Sprintbericht](./SPRINT_2026-11-12_BRO_10.md).

## KAB-0879 · kompakter Quellen- und Geometrievermerk (14.11.2026)

| Punkt | Nachweis |
|---|---|
| Soll | Öffnungen mit DN 60, 110 und 116 **schematisch proportional** darstellen, ohne echte Innen-/Außen-/Lochmaße zu behaupten. |
| Code | [concrete-wall.js](../concrete-wall.js) → `build`: Kreisradius `r` pro Öffnung, `THREE.Path.absarc` und `ExtrudeGeometry` mit echten Durchbrüchen. |
| Test | [tests/concrete-wall.test.js](../tests/concrete-wall.test.js): synthetische Öffnungen 60/110/116 mm und gesondertes `dimensionSpec.openingMm` als Lochmaß-Override. |
| Originalschutz | Derselbe Test prüft unveränderte Quellenmodelle (HTML: 48/78 Öffnungen; PDF V2: 91) nach Geometrieerzeugung. |
| Offen | Bros visueller Vergleich, reale Hersteller-/Messwerte und Fachfreigabe. DN gilt nur als nomineller Zeichenwert, nicht als geprüftes Lochmaß. |

**Quellenbegriffe:** Die beispielhaften Größen 60/110/116 wurden am 11.11.2026 in Bros Feedback *als Beispiele* genannt. `dn`, `dimensionSpec.innerMm`, `outerMm`, `openingMm` und `candidateMm` bleiben getrennt. Dieses Update verändert weder Rohr-IDs/Positionen noch Bestandsdaten. Es ist kein Beleg einer bereits durchgeführten neuen Browserprüfung.

## Offener Bro-Review · Testversion 2.0.7-beta.1 · 14.11.2026

**KAB-0878: blockiert auf Bros Sichtentscheid.** Prüffrage: „Ist der Beton dunkel und dezent genug, auch in der 3D-Tages-/Nachtansicht, oder muss die Farbe angepasst werden?“ **Status:** keine Antwort/Freigabe vorhanden. Prüfstelle: [visueller Chromium-Workflow](https://github.com/valoon4/Androidbaze/actions/workflows/kabelsalat-visual.yml); Testprojekt für Bro: [GitHub-Release](https://github.com/valoon4/Androidbaze/releases/tag/kabelsalat-latest). Keine physikalischen Betonmaterial-/Aufmaßannahmen.
