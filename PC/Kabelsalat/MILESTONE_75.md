# Kabelsalat – 75-%-Entwicklungsmeilenstein (v0.6.0)

**Die Zahl 75 % steht für die dritte Projektetappe, nicht für eine nachgewiesene Quote der vollständigen AVL0116-Anforderungen.**

Neu: Mehrere unabhängige Schächte samt eigener Wände, Kabel, Auflager und Varianten in einer Projektdatei; globale X/Z-mm-Koordinaten; 2D-Netzkarte mit Zoom, Pan und Doppelklick; Schacht-Verbindungen; benannte Netz-Kabelwege mit geordneten lokalen Kabel-IDs; Fehlerhinweise bei fehlenden Kabeln/Links; CSV-Netzinventar und bestätigungspflichtiger CSV-Koordinatenimport mit dem Header shaft_id;x_mm;z_mm; JSON-Migration bisheriger Einzel-Schachtprojekte in das v4-Netzmodell.

**Automatisierte Tests:** network.test.js (Schächte, Links, Routen, CSV) und smoke.test.js (v3→v4-Migration und JSON-Roundtrip mit mehreren Schächten), zusätzlich bisherige 2D-/Komponententests.

**Abnahme durch den Bruder:** Zwei weitere Schächte anlegen, jedem eigene Leerrohre und Kabel geben; zwischen den Schächten wechseln und Inhalte prüfen; Schächte verbinden, Gesamt-Kabelweg anlegen, im Netz hervorheben und per Segment zum lokalen Kabel navigieren; Verbindung löschen und die Warnung überprüfen; Projekt als JSON sichern und erneut öffnen; absichtlich fehlerhafte CSV-Koordinaten ablehnen lassen und valide CSV erst nach ausdrücklicher Bestätigung übernehmen.

**Nicht enthalten:** Physisch nachgewiesene Kabelkontinuität, geprüfte Mindestbiegeradien, Rohrfüllung, Brandschutz/Statik, CAD-/BIM-Import, KI-Planerkennung, PDF-Fachpläne und Mehrbenutzerverwaltung. Ein korrekt verbundener schematischer Netzgraph belegt keine reale Kabelverlegung.

**Keine Ausführungs- oder Abnahmefreigabe.**
