# Kabelsalat – finaler Konzeptabgleich (AVL0116)

**Originalquelle:** Softwarekonzept_SchachtPlan_3D.pdf, „SchachtPlan 3D“, AVL0116, Stand 05.11.2026, Version 0.2, Seiten 1–2.
**Prüfversion:** Kabelsalat 1.0.0-beta.2, basierend auf dem tatsächlichen Quellcode und den automatisierten Tests.
**Gesamtergebnis:** **NICHT 100 % konzeptkonform.** Die früheren 25/50/75/100-%-Angaben waren Entwicklungsmeilensteine und kein nachgewiesener Erfüllungsgrad.

Die Statusangabe **Umgesetzt** bedeutet: konkrete Funktion ist im Quellcode vorhanden und soweit möglich automatisiert überprüft, jedoch nicht von Fachleuten abgenommen. **Teilweise** bedeutet: ein eingeschränkter/schematischer Funktionsumfang. **Offen** bedeutet: nicht implementiert oder fachlich nicht nachgewiesen.

## 1. Verbindlicher Editor-Grundumfang – Konzept Seite 1

| ID | Originalanforderung | Status | Nachweis und Abweichung |
|---|---|---|---|
| E01 | Schachtwände frei anlegen, verändern und in Länge/Breite/Höhe/Stärke konfigurieren | **Teilweise** | Abmessungen/Wandstärken editierbar, aber ausschließlich vier feststehende Wände SW1–SW4; keine freien Grundrisse |
| E02 | Jede Wand eindeutig benennen und in 2D bearbeiten | **Teilweise** | Vier Wände mit eigenem 2D-Plan, aber Namen/Anzahl nicht frei |
| E03 | Leerrohre hinzufügen, kopieren, verschieben, entfernen, DN und mm-Koordinaten | **Umgesetzt** | 2D-Drag, Raster, freie Rohre, numerische Werte; editor2d.js |
| E04 | Rohr- und Wandabstände millimetergenau festlegen | **Teilweise** | Numerische Abstände + Projektwarnschwellen, keine zwingende bzw. normative Einbauprüfung |
| E05 | Kabel an Start-/Zielrohre anschließen und per Maus bearbeiten | **Teilweise** | 3D-Endgriffe und Kontrollpunkte, synchron wählbare 2D-Projektion; keine vollständige freie 2D-Leitungsbearbeitung |
| E06 | Gerade Standardverbindung für gegenüberliegende Anschlüsse | **Offen** | Automatik verwendet weiterhin eine Catmull-Rom-Kurve, keine garantierte Gerade |
| E07 | Echte Richtungswechsel/Schleifen mit gewährleistetem Mindestbiegeradius | **Offen** | Nur unverbindliche Drei-Punkt-Näherungswarnung, keine korrekte Spline-Krümmungsprüfung je Kabel |
| E08 | OLA und MS separat vollständig einhausen; Boden/Deckel/Seiten/Stirnplatten | **Teilweise** | Zwölf einzelne 3D-Platten, aber keine bautechnisch nachgewiesene vollständige Schließung/Fugengeometrie |
| E09 | Einzelplatten: Material, Dicke, Größe, Lage, Sichtbarkeit, Transparenz | **Umgesetzt** | Numerisch bearbeitbare Einzelplatten inkl. Material, Dicke, Versatz und Deckkraft; components.js |
| E10 | Fremde Leerrohre von Einhausungen freihalten | **Teilweise** | Vereinfachte AABB-Überdeckungswarnung; keine fachliche Garantie |
| E11 | Konsolen/Auflager hinzufügen, zuordnen, bearbeiten und in 2D/3D darstellen | **Teilweise** | Objekte mit ID, Name, X/Y/Z und Maßen; statische/technische Freigabe und freie Direktmanipulation fehlen |
| E12 | Projektdatei speichern/laden, Varianten anlegen und vergleichen | **Teilweise** | JSON-Roundtrip, Versionen und einfache Diff-Liste; kein vollständiger visueller Planvergleich |
| E13 | Ansichten als PDF/Bild und Projekt/Viewer für Dritte exportieren | **Teilweise** | 3D-PNG, PDF-/HTML-Projektbericht, interaktiver schreibgeschützter HTML-Viewer; kein vollständiger maßhaltiger CAD-/2D-Zeichnungssatz |

## 2. Bedienprinzip und Betrieb – Konzept Seite 1

| ID | Originalanforderung | Status | Nachweis und Abweichung |
|---|---|---|---|
| B01 | Große Zeichenfläche, Objektliste, Eigenschaften und numerische Eingaben | **Umgesetzt** | Zweigeteilter 2D-/3D-Editor plus Eigenschafts- und Listenleisten |
| B02 | Freie 3D-Drehung, Zoom, Vogelperspektive, Beschriftungen und Farben | **Umgesetzt** | Three.js, orthografische Ansichten, Netzkarte und Systemfarben |
| B03 | Rückgängig/Wiederholen sowie durchgehend synchrone Bearbeitung | **Teilweise** | Undo/Redo und gemeinsames Rohr-/Kabelmodell; einzelne Objektklassen noch nicht in jeder Ansicht direkt bearbeitbar |
| B04 | Offline-Browser ohne Installation und lokale Projektdatei | **Umgesetzt** | Browser-ZIP + JSON; zusätzlich portable Windows-EXE |

## 3. Netzübersicht und durchgängige Kabelverfolgung – Konzept Seite 2

| ID | Originalanforderung | Status | Nachweis und Abweichung |
|---|---|---|---|
| N01 | Zoombare Vogelperspektive aller Schächte und Verbindungen | **Teilweise** | Netz-Canvas mit Zoom/Pan, globalen X/Z-Millimetern; max. 30 Schächte, kein bestätigter Koordinatenbezug |
| N02 | Suche/Filter nach ID, System und Status | **Teilweise** | Globale Textsuche nach Schächten/Rohren/Kabeln, lokale Systemfilter; echter netzweiter Statusfilter fehlt |
| N03 | Schacht anklicken und fließend in 3D hineinzoomen | **Teilweise** | Schachtwechsel/Doppelklick funktioniert, aber keine fließende Zoom-Animation |
| N04 | Rohr anklicken: DN, Position und sämtliche Kabel | **Umgesetzt** | Auswahl in 2D/3D, Belegungsliste mit anklickbaren Kabeln |
| N05 | Durchgängige Navigation Netz > Schacht > Leerrohr > Kabel | **Teilweise** | Globale Suche und lokale Navigation; kein vollständiger hierarchischer Breadcrumb-Pfad |
| N06 | Vollständige Leitung mit Start/Ziel, Schachtfolge, belegten Rohren und Abzweigen hervorheben | **Teilweise** | Geordnete lokale Kabelsegmente, Schematik in Netzkarte; keine geprüfte physische Übergangs- und Abzweiggeometrie |
| N07 | „Gesamtverlauf anzeigen“ und synchronisierte Auswahl | **Teilweise** | Route hervorheben, Segment klicken und lokalen Kabelabschnitt öffnen; Auswahl nicht bei jedem Ansichtswechsel voll synchron |

## 4. Daten, automatische Erkennung und Genauigkeit – Konzept Seite 2

| ID | Originalanforderung | Status | Nachweis und Abweichung |
|---|---|---|---|
| D01 | Stabile IDs für Schächte, Wände, Rohre, Kabel, Platten, Auflager | **Teilweise** | Persistente lokale IDs und Netzlink-IDs, jedoch kein vollständiges globales Objektreferenzmodell |
| D02 | Globale Lage/Höhe, lokale Maße, DN, Kabel-Ø, Mindestbiegeradius und Netzfolge | **Teilweise** | Globale X/Z ohne absolute Höhe und Referenzsystem; DN, Durchmesser, Routen vorhanden, kein gesicherter Radius je Kabeltyp |
| D03 | Plan-/Kabel-/Rohr-/Vermessungsdaten importieren und verknüpfen | **Offen** | Nur JSON-Projektdateien und koordinatenbasierte Schacht-CSV, kein echter strukturierter Plan-/Bestandsimport |
| D04 | Plan- und Bilderkennung mit geprüften Vorschlägen und Widerspruchsbestätigung | **Offen** | Noch keine Plan-/Bilderkennung |
| D05 | Objektbezogene Quelle, Plan-/Bestandsstand, Prüfstatus, Änderungshistorie | **Teilweise** | Manuelle Schacht-/Kabelvermerke mit Quelle und Datum; lokaler manipulierbarer Auditlog, nicht pro Geometrieobjekt |
| D06 | Schematische und bestätigte Vermessungsdaten unterscheiden | **Teilweise** | Beispiele und Entwurfsstatus gekennzeichnet; kein vollständiger georeferenzierter Mess-/Abnahme-Workflow |
| D07 | Kollisionen, Abstände, Biegeradius, Fremdrohrfreiheit prüfen | **Teilweise** | Qualitätshinweise und AABB-Geometrie, keine nachgewiesene Fachkonformität |

## 5. Abnahmekriterien – Konzept Seite 2

| ID | Originales Abnahmekriterium | Prüfstand |
|---|---|---|
| A01 | Maße und Leerrohrpositionen stimmen in 2D und 3D | Gemeinsame Positionen und mathematische 2D/3D-Koordinatentests. Reale Windows-Bedienabnahme fehlt |
| A02 | Kabel, Einhausungen und Auflager frei bearbeiten; alle Seiten einsehbar | Eigenschaften und 3D-Auswahl implementiert; noch keine freie direkte Bearbeitung sämtlicher Bauteile |
| A03 | Ein Kabelklick zeigt denselben Verlauf in allen Ansichten | Gleiche Kabel-ID und 2D-Projektion/3D-Auswahl, aber keine vollständige gemeinsame physische Trassengeometrie |
| A04 | IDs, Maße und Belegung nach Speichern/Laden unverändert | Automatischer JSON-Roundtrip aller drei großen Beispiele und des Legacy-Formats |

## Bereinigung und Beispielabnahme

Die Anwendung hat jetzt drei **direkt im Offline-Programm ladbare** Beispiele:

1. **Referenzschacht 13_T_021 (DEMO):** 1 Schacht, 94 Rohre, 16 farbige Kabel, 3 Konsolen/Auflager, Einhausungen und Variante.
2. **Bahnhofsnetz (DEMO):** 4 Schächte, 376 Rohre, 64 Kabel, 4 Links und 3 geordnete Kabelwege. Syntaktische Verbindungsprüfung: keine ungültigen Referenzen.
3. **Prüffall (DEMO):** 1 Schacht, 96 Rohre, 16 Kabel; bewusst fehlerhafter Rand, Rohrüberlappung, zu großer Kabel-Ø und fehlender Anschluss. Die Datenprüfung darf nicht abstürzen.

Die Demo-Geometrie ist synthetisch und stellt **keine echten Vermessungs- oder Bestandsdaten des Referenzschachts 13_T_021** dar.

**Bereinigte Fehler:** Prüfberechnung bei unvollständigem Kabelanschluss, neues Projekt mit falscher Dateiversion, unzureichend sichtbare Beispiele und fehlende Belegungsanzeige am Rohr. Regressionstests für Beispieldateien, Projektversionen, Warnungen und referenzierte Netz-Kabelwege wurden erweitert.

**Automatisch geprüft:** JS-Syntax, UI-IDs, Geometriemodell, Komponenten, Netz, Qualität, Berichte, Import/Export-Daten, JSON-Roundtrip, Erstellung von Windows-EXE und Offline-ZIP.
**Noch nicht automatisch oder fachlich geprüft:** realer Nutzerworkflow in Windows, Performance mit großen Bahnhofsprojekten, technischer Mindestbiegeradius/Brandschutz, geprüfte Bestandsdaten, manipulationssichere Abnahme.

## Verbleibende Prioritäten vor fachlicher Produktfreigabe

1. Frei modellierbare Schachtgrundrisse und beliebige Wände.
2. Reale 3D-Leitungsgeometrie, physische Rohr-/Kabelkontinuität, Kabeltyp-Mindestbiegeradien und Rohrfüllung.
3. Fachgerechte Einhausung/Brandschutz/Tragfähigkeit und belastbare Kollisionsregeln.
4. Georeferenzierte Vermessungs-, CAD-/BIM- und Bestandsimporte mit nachvollziehbarer Datenzuordnung.
5. Vollständiger Objekt-/Freigabe-Audittrail, Rollen und prüfbare Abnahmeunterlagen.

**Ergebnis: BETA-DEMONSTRATOR – keine Bau-, Elektro-, Brandschutz- oder Ausführungsfreigabe.**
