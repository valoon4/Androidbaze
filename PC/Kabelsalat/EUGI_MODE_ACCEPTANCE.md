# Kabelsalat – Basic-Modus: nutzerorientierte Abnahme

**Version:** 1.0.0-beta.3. Anlass: Ein Erstnutzer konnte auf dem Screenshot nicht erkennen, wozu der Schachteditor dient. Ziel: die Anwendung von der sichtbaren fachlichen Vollansicht in einen erklärenden, fokusorientierten Einstieg übersetzen, ohne Fachfunktionen zu entfernen.

## Erwartete Bedienung in weniger als drei Schritten

1. Beim Start links **„Was willst du entdecken?“** mit drei großen Missionen sehen. Rechts **„Ein Kabel, ein Weg“** und die farbige Kabelliste. Keine Prüfstatus-, Material- oder technische Mindestwerteingabe dominieren das erste Bild.
2. Auf **„1 · Das große Ganze“** klicken: Es erscheint die Netzkarte. Schächte und Verbindungen sind sichtbar und ein Doppelklick öffnet den gewählten Schacht.
3. Auf **„2 · Rein in den Schacht“** klicken: Es erscheint eine große kontrastreiche 3D-Darstellung mit Legende „Schachtwand / Leerrohr / Kabel“. Auf **„3 · Ein Kabel verfolgen“** klicken: Das ausgewählte Kabel wird kräftig gezeichnet, andere werden transparent und Start/Ziel werden erklärt.

Weitere Prüfungen:
- 2D-Wandplan mit einem einzigen Button ein-/ausblenden; er verwendet dieselben Modellobjekte.
- Glossar aufklappen, ohne die Anwendung zu verlassen.
- Häkchen in den Missionen erscheinen beim Klick und bleiben beim Neustart vorhanden.
- Oben **„Profi-Modus“** klicken: alle bekannten Bearbeitungsfelder, Einhausungen, Konsolen und Prüfdokumentation sind da.
- Wieder auf **„Basic-Modus“** klicken; kein Projektinhalt darf verschwinden. Die Moduswahl bleibt beim Neustart erhalten.
- Ein Projekt als JSON exportieren, Modus wechseln, erneut exportieren: Alle fachlichen Projektdaten bleiben unverändert. Die separat gespeicherte UI-Moduswahl gehört nicht zur Projektdatei.
- Bestehende persönliche Projektdateien dürfen weder durch den Moduswechsel noch beim Neustart überschrieben werden.
- Bedienung mit Maus und Tastatur (Tab, Enter, Leertaste), Schriftlesbarkeit bei Windows-Skalierung 125–150 % prüfen.

## Technische Regressionstests

- JavaScript-Syntax einschließlich der neuen Mission-Events.
- Alle über JavaScript referenzierten UI-Elemente sind in index.html vorhanden.
- Qualitäts-/Netz-/Leerrohr-/Beispiel-Roundtrip-Tests unverändert.
- Paket-Build inklusive Offline-HTML und Electron-EXE über Windows Actions.

**Grenzen:** Es handelt sich um UX-Onboarding, nicht um medizinische ADHS-Behandlung oder validierte Barrierefreiheit. Auch die Engineering-Freigabe des Originalkonzepts bleibt offen; siehe CONCEPT_AUDIT.md.
