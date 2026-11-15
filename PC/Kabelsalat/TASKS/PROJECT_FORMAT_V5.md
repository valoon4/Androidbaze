# Kabelsalat · Projektformat v5, Migration, Sicherung und Undo

Stand: 15.11.2026. **Technische Datenvertrags-Dokumentation**, keine Bestands-, Geometrie- oder Brandschutzfreigabe.

## Versionen und Vertrag

| Format | Behandlung der Anwendung | Fachliche Grenze |
|---|---|---|
| v1 (historisch) | Import wird nur nach Einzelnachweis als verlustfrei akzeptiert | Kein pauschales Migrationsversprechen |
| v3 (historisch) | `validProject` normalisiert Schachtwände, Rohre, Kabel und Komponenten; fehlende Felder erhalten explizite Defaults | Quellbelege bleiben ungeprüft |
| v4 (historisch) | Vorher gesichertes JSON über den normalen Import, anschließend komplette Objekt-ID- und Endpunktprüfung | Kein blindes Überschreiben neuer Daten |
| v5 (aktuell) | Kanonisches Format `version:5` mit `dims`, `walls`, `cables`, `enclosures`, `supports`, `variants`, `network`, `activeShaftId` | Kein unbekannter Zahlenwert wird als Messung freigegeben |

**Konkrete Codepfade:** [App-Validator](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/app.js) `validProject`, [Komponentenmodell](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/components.js) `snapshot/normalizeVariants/differences`, [Netzmodell](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/network.js) `snapshot/normalizeNetwork`, [PDF-Demo-Migration](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/schematic-positions.js) `migrate/transformProjectShunt`.

## Migrationspfad

1. Original-Projektdatei unverändert als Sicherung exportieren. Kein automatisches Upgrade ohne Quellkopie.
2. Import zunächst durch `validProject` normalisieren; nur bekannte, validierte Felder übernehmen, nicht bestätigte Maße bleiben unbestätigt.
3. Wand-/Rohr-/Kabel-IDs und A-/B-Anschlüsse nach dem Import gegen das Backup vergleichen. Netzrouten referenzieren nur IDs, sie bestätigen keine physische Kontinuität.
4. Besonderheit PDF-V2-Demos: alte unveränderte 62-%-Ansichtskoordinaten werden mit `sourceU/sourceV` erkannt und auf Quellwerte zurückgeführt; manuell verschobene Positionen werden nicht heimlich überschrieben.
5. JSON-Roundtrip und Undo/Redo vor Weiterbearbeitung an fiktiven Testfällen prüfen. Bei echtem Altprojekt mit fehlenden Quellen Blocker dokumentieren statt eine Fachfreigabe zu behaupten.

## Undo/Redo-Historie

Die in [app.js](https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/app.js) vorhandene Funktion `change(fn,label)` speichert vor der Änderung ein komplettes Projekt-JSON in `undo`, leert `redo` bei neuer Änderung und protokolliert eine kurze Aktion im Audit. `undoRedo(back)` stellt mit `validProject` das gespeicherte Projektmodell wieder her und synchronisiert den aktiven Schacht-Snapshot. Ein Drag hat einen einzigen Vorher-Zustand, nicht einen pro Pointer-Move. Lokale Browser-Speicherung zeigt Fehler sichtbar an, erfüllt aber keine externe Backup-Pflicht.

## Reproduzierbare Prüfschritte

Aus `PC/Kabelsalat`: `node tests/project-history-giant.test.js` und `node tests/model-regression-giant.test.js`; anschließend `npm test`. Eine neue Live-Chromium-Abnahme wird separat ausgewiesen. Kein Test darf einen Import als sicher erklären, nur weil die JSON-Struktur syntaktisch gültig ist.
