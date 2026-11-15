# AniFocus v0.0 🍅🐱

Cozy, bewusst **pflegearmer** Pomodoro-Timer für Windows. Designrichtung: helle Pastell-Notizbuchoberfläche mit Katze, Kirschblüten und **großer roter Tomate**.

## Download

**[AniFocus.exe herunterladen](https://github.com/valoon4/Androidbaze/releases/download/anifocus-v0.0/AniFocus.exe)**

Der Link wird nach erfolgreichem Windows-Build über GitHub Actions verfügbar. Portable Einzeldatei; keine Installation, kein Konto, keine Netzverbindung.

## Funktionen v0.0

- Drei Hauptbereiche: **Master, Research, Exploring**.
- Master direkt mit **RP1, ITM1, LEAD1, DF1** vorbelegt.
- Research und Exploring **absichtlich leer**, bis du deine eigenen Unterbereiche einmalig anlegst.
- Großer roter Tomaten-Pomodoro: **25 Minuten** als Standard; **Start / Pause / Reset / Dauer** (1–180 Minuten).
- Timer basiert auf einer monotonen Uhr und läuft nach Minimieren korrekt weiter. Er stoppt beim Beenden der App.
- Unterbereiche: anklicken zum Auswählen, per **Rechtsklick umbenennen/entfernen**, einmalig anlegen (maximal 8 je Hauptbereich).
- Optionaler **Gesamtfortschritt je Hauptbereich** (0–100 %), nur auf bewusste Eingabe; bis dahin **keine Prozentzahl**.
- Die Einstellungen werden lokal und automatisch gespeichert unter `%APPDATA%\\AniFocus\\state.json`.

**Bewusst nicht vorhanden:** Aufgabenlisten, Tagesziele, Notizen, Streaks, erfundene Werte, verpflichtende Eingaben oder GitHub-Sync. Gesamtfortschritt und Pomodorozeit sind voneinander unabhängig.

## Lokal starten

Python 3.12 mit Tkinter:

```bash
cd PC/AniFocus
python app.py
```

Tests:

```bash
python -m unittest discover -s tests -v
```

## Windows-EXE

Der Workflow [anifocus-exe.yml](../../.github/workflows/anifocus-exe.yml) testet auf `windows-latest`, erstellt die portable `AniFocus.exe` mit PyInstaller und aktualisiert bei erfolgreichem `main`-Build das prerelease `anifocus-v0.0`. Android-Projekte und andere PC-Apps bleiben unberührt.

**Version 0.0:** Erster funktionaler Desktop-Prototyp. Visuelle Details können noch vom Konzeptbild abweichen.
