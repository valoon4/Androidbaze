# Kabelsalat – 25-%-Meilenstein (v0.4.0)

**Status:** Erste Entwicklungsstufe der Editor-Grundfunktionen. Die Angabe 25 % ist eine Projektetappe, **keine rechnerisch geprüfte Quote** der gesamten Anforderungen aus dem AVL0116-Konzept.

## In dieser Etappe verbessert

1. **Parallele 2D-/3D-Ansicht:** eine millimetergenaue Zeichenfläche zeigt die ausgewählte Schachtwand gleichzeitig mit dem 3D-Modell.
2. **Ein gemeinsames Datenmodell:** Leerrohre im 2D-Plan auswählen und in 1-mm-Schritten per Maus versetzen; das 3D-Modell wird aus denselben Daten sofort aktualisiert. Die 3D-Selektion aktiviert das Leerrohr auch in 2D.
3. **Rohrwerkzeuge:** neue Öffnungen in 2D anlegen, vorhandene kopieren (mit neuer stabiler ID), Maße/DN numerisch ändern, Änderungen rückgängig machen.
4. **Prüfbarkeit:** Unit-Tests für die 2D-Koordinatenabbildung und inverse Mausposition, Überlappung, Randabstände und Rohroduplikate; JSON-Speicher-/Ladetests der bestehenden App.

## Manuelle Abnahme auf Windows

- Öffne das Programm, aktiviere „2D und 3D gleichzeitig anzeigen“.
- Wähle SW1, klicke rechts in 2D auf ein Leerrohr und prüfe die Markierung im 3D-Modell.
- Ziehe das Rohr in der 2D-Zeichenfläche. Prüfe die angezeigten millimetergenauen Koordinaten und dieselbe Position in 3D.
- Klicke „Kopieren“: das neue Rohr bleibt unabhängig bearbeitbar.
- Speichere das Projekt als JSON und lade es erneut. Rohr-IDs, Positionen und DN müssen gleich bleiben.
- Wechsle zu SW2, SW3 und SW4. Die gezeigte Wand und ihre Leerrohre müssen jeweils synchron wechseln.

**Nicht getestet:** Bedienbarkeit durch eine Elektrofachkraft auf einem echten Windows-PC; automatische elektrotechnische und brandschutztechnische Regelkonformität. Änderungen in 2D betreffen derzeit primär Leerrohre, nicht alle Objektklassen.

## Noch offen

Freie zusätzliche Wände, projektbezogene Rohrabstände, geprüfte Mindestbiegeradien, einzelne Einhausungsplatten mit Material und Dicke, Konsolen und Auflager, Varianten/PDF/Viewer, übergreifende Netzübersicht, Planimport/Erkennung und Quellen-/Änderungshistorie.

Für eine Ausführungs- oder Abnahmeplanung ist Kabelsalat weiterhin **nicht** zugelassen.
