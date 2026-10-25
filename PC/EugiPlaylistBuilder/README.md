# Eugi Playlist Builder (PC, portable)

Offline-Windows-App, um aus einer **Unsorted.m3u** die fünf Playlist-Stufen zu erstellen:

| Stufe | Name | Logik |
|---:|---|---|
| 0 | Rest | automatisch, wenn Best/Super/Ultra/Mega überall 0 |
| 1 | Best | binär 0/1 |
| 2 | Super | binär 0/1 |
| 3 | Ultra | binär 0/1 |
| 4 | Mega | binär 0/1 |

Die vier Kategorie-Flags sind **unabhängig** (ein Track kann in mehreren Levels stehen). Es gibt keine implizite Hoch-/Herabstufung. Beim Import einer Referenz-M3U wird **nur deren Kategorie** neu befüllt.

## Bedienung

1. **Unsorted laden** (M3U/M3U8 mit `#EXTINF` und Datei-URIs).
2. Optional bestehende **Best/Super/Ultra/Mega**-M3Us importieren. Der Pfad ist der eindeutige Schlüssel – nicht der Songtitel. Für unklare Matches erscheint ein Prüfbericht. Keine fuzzy Matches, kein stiller Verlust.
3. In der Tabelle pro Song direkt die **Checkbox (☑/☐)** anklicken oder Zeilen markieren und Häkchen setzen/entfernen. Intern bleiben die 0/1-Flags erhalten. Suche filtert nur die Anzeige.
4. **Alle M3U exportieren** → `Jahr_0_Rest.m3u` bis `Jahr_4_Mega.m3u`, plus `Jahr_Zuordnung.csv` mit den binären Werten und bei Bedarf `Jahr_Pruefbericht.txt`.
5. **Projekt speichern** (.json) und später fortsetzen. Bei geänderter Unsorted wird das Projekt absichtlich nicht blind übertragen.

**Sicherheit:** Keine Audiodateien werden geöffnet, verändert, verschoben oder gelöscht. Alle `#EXTINF`-Zeilen und `file:///F:/...`-Pfade aus **Unsorted** bleiben erhalten; nur die Track-Auswahl wird verändert. Export ist UTF-8 mit BOM (Windows-freundlich). Reihenfolge wie Unsorted.

## Portable EXE herunterladen

- **Direktdownload:** [EugiPlaylistBuilder.exe](https://github.com/valoon4/Androidbaze/releases/download/eugi-playlist-builder-latest/EugiPlaylistBuilder.exe) (sobald der automatische Windows-Build die erste Release-Datei veröffentlicht hat).
- **Release-Seite:** [Eugi Playlist Builder](https://github.com/valoon4/Androidbaze/releases/tag/eugi-playlist-builder-latest).
- GitHub Actions: Der Workflow `Eugi Playlist Builder Windows EXE` testet und erstellt die **einzelne portable EXE** automatisch bei Änderungen und veröffentlicht sie direkt als **GitHub Release Asset**. Das bisherige Actions-Artifact bleibt als Backup verfügbar. Keine Installation nötig.
- Lokal unter Windows mit Python 3.12+: `python -m pip install pyinstaller` und anschließend `python -m PyInstaller --noconfirm --clean --onefile --windowed --name EugiPlaylistBuilder app.py`. Ergebnis: `dist/EugiPlaylistBuilder.exe`.
- Direkt per Python starten: `python app.py` (benötigt nur Standardbibliothek/Tkinter).

Tests: `python -m unittest discover -s tests -v`.

## Beispiel 2014

Die Unsorted 2014 aus dem Gespräch enthält 517 Tracks. In einer Ultra-Referenzliste kann der Builder Varianten wie `Kaitou Joker OP` und `Kaitou Joker OPv2` anhand des Originalpfads unterscheiden. Die Schaltflächen importieren jeden Level unabhängig. Angehängte Super-/Best-Dateien können später genauso geladen werden.
