# Kabelsalat · jüngstes Bro-Feedback: Betonblock und Rohrdurchmesser

**Eingang:** Eugis Wiedergabe von Bros Praxistest am 11.11.2026: Rohre wirken unnatürlich; Ziel ist ein klassischer, weniger heller Betonblock mit runden, unterschiedlich großen Löchern. Die KI soll Rohrarten und Rohrmaße (beispielhaft 60, 110, 116) erkennen. **Alle 8 Aufgaben offen.**

**Abnahmeregel:** 60/110/116 sind vom Bro genannte Beispiele, keine bestätigten Bestandswerte. DN (Nennweite), Rohr-Innen-/Außendurchmesser und Lochdurchmesser sind technisch zu unterscheiden. Bild-/PDF-Pixelgröße allein ohne Maßreferenz liefert keine echte Millimeterdimension. Originalpläne, Rohr-IDs, Positionen und Kabelanschlüsse nicht unbemerkt verändern.

## P0-15 · Betonoptik und unterschiedlich große Öffnungen (KAB-0877–KAB-0879)

**Definition of Done:** Sichtprüfung mit Bro an beiden Referenzschächten, ohne Rohrkoordinaten oder Belegungen zu mutieren.

- [ ] **KAB-0877** – Beton-Schachtwand als zusammenhängenden massiven Block mit echten runden Durchbrüchen rendern statt hervorstehenden isolierten Rohrringen; beide Referenzschächte prüfen
- [ ] **KAB-0878** – Schachtblock mit klassisch mittel- bis dunkelgrauem Betonmaterial (weniger hell) und dezenter Oberflächenstruktur darstellen; Farbgebung in Tages-/Nachtansicht gegen Bro-Feedback prüfen
- [ ] **KAB-0879** – Kreisöffnungen proportional zu dokumentierten Rohrdurchmessern darstellen; unterschiedliche Öffnungsgrößen (z. B. 60, 110, 116 mm als Testwerte) ohne fingierte Originalmaße erkennbar machen

## P0-16 · Durchmesser-Datenmodell und prüfbare Erkennung (KAB-0880–KAB-0883)

**Definition of Done:** Automatische Kandidaten sind quellengebunden, falsche Maße korrigierbar und ohne Nutzerbestätigung nicht als echte Maße klassifiziert.

- [ ] **KAB-0880** – Rohr-Nennweite DN, Innendurchmesser, Außendurchmesser, erkannte Maßangabe und bestätigte Maßangabe mit Einheit und Quellenstatus getrennt modellieren; DN und tatsächliches Lochmaß nicht gleichsetzen
- [ ] **KAB-0881** – PDF-/Planerkennung: Rohranzahl, Rohrtyp und Maßangaben wie 60, 110, 116 aus Dokumentbeschriftung als Kandidaten mit eindeutiger Rohrzuordnung erkennen; ohne Maßstab keine Durchmesser aus Pixeln behaupten
- [ ] **KAB-0882** – Foto-/Bilderkennung: Öffnungsgröße und Rohrtyp mit explizitem Maßstab/Referenzkörper oder sichtbarer Beschriftung als prüfbare Kandidaten vorschlagen; ohne Referenz nur relative Größen schätzen
- [ ] **KAB-0883** – Erkannte, manuell gesetzte und bestätigte Rohrdimension mit Einheit und Quelle im Tooltip, Eigenschaftenpanel, 2D-Wandplan und 3D-Inspektion gut lesbar anzeigen

## P0-17 · Darstellung und Erkennung dauerhaft absichern (KAB-0884)

**Definition of Done:** Wiederholbare automatisierte Tests und Screenshots einschließlich fehlender Quellmaße und großer/kleiner Öffnungen.

- [ ] **KAB-0884** – Automatisierte Render- und Datenregression für Betonblock mit gemischten Öffnungen (60/110/116 als Testdaten), korrekte DN-/DM-Trennung, Schatten/Helligkeit und geschützte Originalrohrpositionen aufbauen

**Verwandt, nicht dupliziert:** KAB-0037–0054 (unveränderte Originalkoordinaten), KAB-0235–0252 (Rohrmodell), KAB-0523–0558 (Plan-/Bilderkennung), KAB-0829–0840 (fachliche Bauartlogik). Die hier neuen Tasks sind die expliziten Abnahmekriterien aus dem **jüngsten** Feedback.
