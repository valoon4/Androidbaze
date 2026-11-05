# Kabelsalat · Level 100 / v1.0.0-beta.1

**100 % bedeutet den Abschluss des von uns festgelegten vierstufigen Prototyp-Meilensteinplans (25/50/75/100).** Dieser Wert ist **keine** gemessene Erfüllungsquote des Konzeptschreibens und auch keine fachliche Freigabe. Die Version bleibt ausdrücklich eine Beta.

## Neue Funktionen

1. Globale Suche im Schachtnetz nach Schacht-IDs/-namen, Kabel-IDs/-namen, Systemen und Leerrohr-IDs. Treffer führen in den passenden lokalen Editor.
2. Geometrische Prüfhinweise mit konfigurierbaren **Projektwerten** (Rohr-Rohr-Abstand, Rohr-Wandrand, Drei-Punkt-Biegeradius-Näherung). Fehlende Kabelanschlüsse, zu große Kabel-Ø gegenüber dem angegebenen Rohr-DN und fehlende Netzsegmente werden ebenfalls gemeldet.
3. Manuelle Prüfvermerke pro Schacht bzw. Kabel (Status, Quelle/Beleg, Bearbeiter, Datum, Notiz). Status „Vor Ort abgeglichen“ erfordert eine angegebene Quelle. Lokale einfache Änderungshistorie ohne Manipulationsschutz.
4. Projektbericht mit Liste aller Schächte, Kabel, Routen, Prüfhilfe-Hinweise und Prüfvermerke; auf Windows als PDF speicherbar, im Browser als HTML-Bericht zur PDF-Ausgabe über Drucken.
5. Vollständig lokaler, **lesender** HTML-Projektviewer. Schachtkarte, lokale 2D-Leerrohre, Kabelsuche und netzweite Segmentnavigation. Kein Server und keine Schreibmöglichkeit.
6. Gemeinsame Kabel-Selektion durch Anklicken eines auf die 2D-Wand projizierten Kabelwegs.
7. Projektdatei v5 mit rückwärtskompatibler Normalisierung. Die neuen Metadaten werden zusammen mit Schachtnetz und lokalen Schacht-Snapshots gespeichert.

## Automatische Tests

- tests/quality.test.js: netzweite Suche, Projektwerte, Überlappung, Kabel/Rohr-Ø und Review-Daten.
- tests/reports.test.js: Bericht, HTML-Viewer und Escaping untrusted Nutzereingaben.
- tests/smoke.test.js: UI-Datenmodell, v1/v3/v4-Migration nach v5, Prüfdaten-/Review-/Audit-Roundtrip.
- Bisherige Editor-/Netz-/Baugruppen-Tests bleiben erhalten.
- GitHub-Actions-Build der Windows-EXE und Browser-ZIP.

## Manuelle Abnahme (nicht durch automatisierte Tests ersetzt)

1. Vorhandenes Projekt-JSON sichern; in v1.0 Beta öffnen, Daten der alten Schächte, Kabel und Varianten vergleichen.
2. Im gesamten Netz nach einem Kabel suchen, Treffer wählen. Schacht-/Kabel-Auswahl, 3D- und 2D-Ansicht müssen übereinstimmen.
3. Projekt-Mindestabstände eintragen und absichtlich überlappende Rohre erzeugen. Warnungen müssen erscheinen. Werte wieder löschen: normative Prüfbehauptungen dürfen nicht erscheinen.
4. Ein Kabel größer als die zugeordnete Rohr-DN einstellen; Datenfehler muss erscheinen.
5. Für ein Kabel „Vor Ort abgeglichen“ ohne Quelle speichern: Muss abgelehnt werden. Mit Beleg zulassen und Projekt-JSON sichern/laden.
6. HTML-Bericht öffnen und visuell kontrollieren; Windows-PDF direkt speichern und die fertige PDF auf Seitenumbruch und Lesbarkeit überprüfen.
7. Offline-Viewer als HTML speichern, Internetverbindung trennen, Datei in Edge/Chrome öffnen. Durch die Schächte navigieren, Kabel finden und feststellen, dass die Datei nicht bearbeitbar ist.
8. Zwei Schächte verbinden, einen Netz-Kabelweg mit mehreren Segmenten erstellen und nach Löschen des Netzlinks die Unvollständigkeitswarnung prüfen.

## Offene Anforderungen / nicht als „fertig“ darstellen

- Beliebig viele frei orientierte Schachtwände und echte CAD-Geometrie.
- Technisch valide Biege-Radiusberechnung anhand wirklicher Kabeltypen, Verlegearten, Herstellerdaten, Rohrfüllung, Brandschutz, Einziehlängen und projektspezifischer DIN/VDE-/EBA-Anforderungen.
- Durchgehende physische Schachtübergänge, geometrische Kabelkontinuität und realitätsgeprüfte Ist-/Soll-Abgleiche.
- Automatische Plan-/PDF-/Bild-/Vermessungserkennung mit überprüfbaren Quellen.
- Mehrbenutzerverwaltung, rollenbasierte Freigabe, unveränderbarer Audit-Trail und rechtsverbindliche Abnahmen.
- Nachgewiesene Produktstabilität/Performance unter realen großen Bahnhofsprojekten.

**Achtung:** Automatisierte Hinweise sind geometrische oder syntaktische Vorprüfungen. Keine Bau-, Elektro-, Brandschutz-, Prüf- oder Abnahmefreigabe.
