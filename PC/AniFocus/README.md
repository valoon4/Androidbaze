# AniFocus v0.1.1 🍅🐱

Cozy, komplett offlinefähiger Windows-Pomodoro mit roter Tomate und Katze – kein Task-Management, keine Streaks.

## Portable Windows EXE

[AniFocus.exe v0.1.1](https://github.com/valoon4/Androidbaze/releases/download/anifocus-v0.1.1/AniFocus.exe) (nach erfolgreichem GitHub-Actions-Build).

## Bereiche

- **Master:** RP1, ITM1, LEAD1, DF1
- **Research:** OST, Playlists, Seasonvids, LA, Syncupdates
- **Exploring:** Anime & Cartoon Serie, Anime & Cartoon Film, Liveaction Serie, Liveaction Film, Gaming, Manga, Comic

Der Timer startet bei 25 Minuten, lässt sich auf 1–180 Minuten einstellen und bietet Start, Pause, Reset. Bereiche können einmalig bearbeitet werden (Rechtsklick). Der optionale manuelle Gesamtfortschritt pro Hauptbereich bleibt davon unabhängig.

## Fest integrierte Research-Stände (GitHub-Snapshot 15.11.2026)

Die EXE benötigt **keine Internetverbindung**. Die drei Forschungsstände sind in `github_progress.py` fest eincodiert:

| Bereich | Bestätigter Stand aus Storytimez |
|---|---|
| OST | Dec 6 fertig; P2 392 und P3 4.092 offen |
| Playlists | Dec 0–6 teilweise gebaut; offene Fünfer-Builds 1986, 2000–01, 2003–06, 2011 Q3/Q4, 2026 Q1–Q4 |
| Seasonvids | 2006 Q4 18/75 bis Cut; 2016 Q4 Load 69/79 und Add 3/79; 2026 Q4 52/79 bis Cut |

**Info** zeigt weitere bestätigte Einzelstände. Es werden **keine Gesamtprozente abgeleitet**. Der Stand ist bewusst datiert und ändert sich erst mit einem neuen EXE-Build.

Kanonische Quelle: [Storytimez Research Status](https://github.com/valoon4/Storytimez/blob/main/research/RESEARCH_STATUS.md). Sie wird *bei der Entwicklung* geprüft, nicht beim Benutzen der EXE abgerufen.

Alle lokalen Einstellungen aus v0.0/v0.1 bleiben erhalten unter `%APPDATA%\\AniFocus\\state.json`.

## Entwicklung

```bash
cd PC/AniFocus
python -m unittest discover -s tests -v
python app.py
```

Die GitHub Action `.github/workflows/anifocus-exe.yml` erstellt eine portable Windows-EXE mit PyInstaller.
