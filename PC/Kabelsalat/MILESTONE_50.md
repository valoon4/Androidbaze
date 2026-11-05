# Kabelsalat — 50-%-Meilenstein (v0.5.0)

**Begriffsklärung:** „50 %“ beschreibt die zweite vereinbarte Entwicklungsetappe. Es ist **keine** nachgewiesene Erfüllungsquote sämtlicher AVL0116-Anforderungen. Insbesondere ist Kabelsalat keine normgeprüfte Bau- oder Elektroplanungssoftware.

## Neu gegenüber dem 25-%-Meilenstein

1. **OLA-/MS-Einhausungen mit sechs unabhängig editierbaren Platten:** Boden, Deckel, zwei Seiten und zwei Stirnplatten, jede mit stabiler ID, Material, Breite/Länge, Dicke, Versatz, Sichtbarkeit und Deckkraft. Die Platten sind einzeln im 3D-Modell auswählbar.
2. **Einfache geometrische Warnungen:** Prüft mit axis-aligned bounding boxes (AABB) näherungsweise, ob Platten eine fremde Leerrohröffnung überdecken könnten. Diese Warnung ist keine geprüfte Kollisionsberechnung.
3. **Konsolen und Auflager:** Bauteile können hinzugefügt, benannt, auf SW1–SW4 bezogen, mit X/Y/Z-Position und Maßen bearbeitet und in 3D sowie der zugeordneten 2D-Wandprojektion dargestellt werden.
4. **Planungsvarianten:** Bis zu zwölf benannte, separat gespeicherte Planungsstände. Änderungen an Mengen, Maßen, Wänden/Rohren, Kabeln, Platten und Auflagern werden im vereinfachten Vergleich angezeigt. Ein gespeicherter Stand kann wiederhergestellt oder gelöscht werden.
5. **Versioniertes Projektformat v3:** Platten, Auflager und Varianten werden zusammen mit den Kabeln und Wänden als JSON exportiert. Projekte aus älteren Versionen werden beim Import normalisiert.

## Automatisierte Qualitätssicherung

- tests/smoke.test.js: Struktur der Bedienoberfläche, vier Wände, Komponenten, JSON-Roundtrip und Migration alter Projekte.
- tests/editor2d.test.js: 2D-Positionsumrechnung, 1-mm-Koordinaten, Rohrkopie und Mindestabstandsgeometrie.
- tests/components.test.js: sechs Einzelplatten, Dicke/Versatz/Material, Konsolen-Daten, Varianten-Snapshots und Differenzen.
- Windows-EXE und Offline-Browser-ZIP werden durch GitHub Actions gebaut und veröffentlicht.

## Abnahmetest für den Fachanwender

1. OLA und MS nacheinander bearbeiten: Jede der sechs Platten auswählen und Dicke, Versatz, Material und Transparenz ändern. In 3D die Auswirkung kontrollieren.
2. Im 3D-Modell eine Platte anklicken; der Eigenschaften-Editor muss deren ID anzeigen.
3. Eine Konsole anlegen, SW2 zuordnen, Höhen- und Quermaße ändern und in 2D/3D wiederfinden.
4. Einen Planungsstand als Variante speichern; anschließend eine Platte und ein Leerrohr verschieben. Der Variantenvergleich muss eine Änderung erkennen.
5. Die Variante wiederherstellen und alle Werte kontrollieren; danach das Projekt als JSON speichern, erneut öffnen und IDs/Dicken/Kabel/Varianten vergleichen.

## Was noch fehlt

- Frei modellierbare zusätzliche Wände und beliebige Schachtgrundrisse.
- Fachlich überprüfbare Kabelbiegeradien, Rohrfüllung, Brandschutz, Kollisionen und Tragfähigkeit.
- Geprüfte Material-, Konsolen- und Auflagerkonstruktion sowie direktes Drag-and-Drop sämtlicher Einzelplatten.
- PDF-Export, interaktiver Viewer und visuelle, punktweise Variantendifferenz.
- Zusammenhängendes Netz mehrerer Schächte, vollständige Kabelverfolgung, Plan-/Vermessungsimport und Quellen-/Freigabehistorie.

**Nicht zur Ausführung oder Abnahme freigegeben.** Die 3D-Geometrie ist ein Planungsprototyp, die angezeigten Prüfergebnisse sind schematisch bzw. näherungsweise.
