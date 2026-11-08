# Eugi Movie Cleaner v0.1

Windows-App zum Aufräumen eines Jahresordners wie `D:\\Filme\\1981`.

## Download

Unter **Actions** im Repository den Workflow **Build Eugi Movie Cleaner EXE** öffnen. Den neuesten **erfolgreichen** Lauf auswählen, im Abschnitt **Artifacts** die Datei **EugiMovieCleaner-Windows-EXE** herunterladen, ZIP entpacken und `EugiMovieCleaner.exe` starten.

## Bedienung

1. Einen Jahresordner wählen; das Jahr wird aus dem Ordnernamen abgeleitet.
2. Vorschau prüfen. Bekannte Filmtitel und CD1/CD2 werden automatisch erkannt.
3. Bei **Prüfen** den Film auswählen und mit **Markierten Titel korrigieren** freigeben.
4. **Alle sicheren Änderungen ausführen** verarbeitet nur freigegebene Einträge.

Bei passenden externen Untertiteln (.srt, .ass, .ssa oder .idx + .sub) wird via **MKVToolNix** ohne Video-Neucodierung in .mkv umgewandelt. DE/ENG-Sprachen werden markiert. Externe Dateien mit `forced` im Namen werden ignoriert. Bereits im Video enthaltene Untertitel werden nicht verändert.

Bei einem Remux verschiebt die App Quellvideo und verwendete Untertitel nach `_EugiCleaner_Backup`, sofern der Backup-Pfad noch frei ist. Bei einer reinen Umbenennung wird die Datei verschoben, nicht kopiert. **Es werden keine Dateien oder Ordner gelöscht.** Protokolle liegen in `_EugiCleaner_Logs`.

## Vor dem ersten großen Durchlauf

Mit einem kleinen Testordner **aus Kopien** ausprobieren. Das Programm erkennt viele, aber nicht alle Release-Namen zuverlässig; keine automatische Online-Recherche in v0.1. MKVToolNix ist separat erforderlich, das `mkvmerge.exe` typischerweise unter `C:\\Program Files\\MKVToolNix\\` installiert.

Die EXE ist nicht digital signiert, weshalb Windows möglicherweise eine Warnung zeigt.
