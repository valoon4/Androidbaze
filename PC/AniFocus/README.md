# AniFocus v0.1 🍅🐱

Cozy, bewusst **pflegearmer** Pomodoro-Timer für Windows. Helles Notizbuchdesign, Katze und große rote Tomate. Keine To-do-Listen, Streaks oder täglichen Pflicht-Eingaben.

## Windows-Download

**[AniFocus.exe v0.1](https://github.com/valoon4/Androidbaze/releases/download/anifocus-v0.1/AniFocus.exe)**

Der GitHub-Actions-Build erstellt eine portable EXE (kein Installer nötig). Link nach erfolgreichem Release verfügbar.

## Bedienung

- **Master:** RP1, ITM1, LEAD1, DF1.
- **Research:** OST, Playlists, Seasonvids, LA, Syncupdates.
- **Exploring:** Anime & Cartoon Serie, Anime & Cartoon Film, Liveaction Serie, Liveaction Film, Gaming, Manga, Comic.
- **Pomodoro:** 25 Minuten ab Werk. Start, Pause, Reset und frei wählbare Dauer (1–180 Minuten).
- **Bereiche anpassen:** Neue Unterbereiche einmalig anlegen; Rechtsklick zum Umbenennen oder Entfernen. Maximal 16 je Kategorie (Seitenwechsel bei über 7).
- **Fortschritt:** Einmalig eigener Prozentwert pro Hauptbereich optional. Leer bedeutet **unbekannt**, nicht 0 %.
- **GitHub Research-Stand:** Wähle bei Research **OST, Playlists oder Seasonvids**. AniFocus liest beim ersten Öffnen die öffentliche kanonische Datei `valoon4/Storytimez/research/RESEARCH_STATUS.md` (nur Lesen, kein GitHub-Login). Refresh per ↻. **Keine erfundenen Gesamtprozente:** OST zeigt P2-/P3-Restzahlen; Playlists die zuletzt dokumentierten offenen Fünfer-Builds; Seasonvids die expliziten Load/Add/Cut-Teilstände. Ein Klick auf **Info** zeigt die Details sowie den Quellpfad.
- **Offline:** Timer und lokale Einstellungen funktionieren vollständig offline; wenn GitHub nicht erreichbar ist, erscheint eine sachliche Meldung statt fiktiver Werte.
- Einstellungen unter `%APPDATA%\\AniFocus\\state.json`. Deine Daten aus **v0.0** werden beim ersten Start erhalten und um die Standardlisten von Research und Exploring ergänzt.

**Quellen:** [Research-Status (öffentlich)](https://github.com/valoon4/Storytimez/blob/main/research/RESEARCH_STATUS.md). Anzeige ist eine Leseansicht des GitHub-Status, keine eigenständige Recherche und keine automatische Erledigung von Zielen.

## Lokal starten & testen

Python 3.12 (mit Tkinter), keine externen Runtime-Abhängigkeiten:

```powershell
cd PC/AniFocus
python app.py
python -m unittest discover -s tests -v
```

Die [.github/workflows/anifocus-exe.yml](../../.github/workflows/anifocus-exe.yml) erstellt die Windows-EXE mit PyInstaller und veröffentlicht die erfolgreiche v0.1 unter dem Prerelease `anifocus-v0.1`. Andere Androidbaze-Apps werden nicht geändert.
