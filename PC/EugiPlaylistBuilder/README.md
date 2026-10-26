# Eugi Playlist Builder (Windows, portable)

Kleiner **Offline-M3U8-Builder** für Musik-Research. Kein Projekt speichern, keine Datenbank und keine fünf Dateien auf einmal. Die komplette **Unsorted** ist immer der vollständige Musik-Pool.

## Download

**[EugiPlaylistBuilder.exe – direkt von GitHub Releases](https://github.com/valoon4/Androidbaze/releases/download/eugi-playlist-builder-latest/EugiPlaylistBuilder.exe)**

Der automatische Windows-Workflow testet den Code, baut die portable EXE und aktualisiert den Release-Download. Keine Installation nötig.

## Bedienung

1. **Unsorted.m3u8** laden. Auch .m3u wird gelesen.
2. **Research-Tabelle** aus ChatGPT/Markdown oder als TSV einfügen. Standard sind **10 Spalten** (Anime | Jahr/Quartal | OP/ED | Load | Song | Interpret | Best | Super | Ultra | Sleepy). 9 Spalten ohne Sleepy funktionieren ebenfalls. Dann **Tabelle zuordnen** drücken. Alternativ funktionieren die bisherige kurze 5-/6-Spalten-Tabelle und eine zehnte Spalte für Sleepy.
3. Gewünschte Playlist **Rest / Best / Super / Ultra / Sleepy** und Zeitraum **Ganzes Jahr / Q1 / Q2 / Q3 / Q4** wählen.
4. **M3U8 exportieren**: Es entsteht genau **eine** Datei.

Beispiel: direkt die komplette Tabelle einfügen. Sie darf die Markdown-Trennzeile enthalten:

    | Samurai Flamenco OP 2      | 2014 1. | OP | 1 | Ai Ai Ai ni Utarete Bye Bye Bye | FLOW           | 1 | 1 | 1 |
    | -------------------------- | ------- | -- | - | ------------------------------- | -------------- | - | - | - |
    | Seitokai Yakuindomo 2      | 2014 1. | OP | 1 | Hanasaku Saikyou Legend Days    | Triple Booking | 1 | 1 | 0 |
    | Space Dandy                | 2014 1. | OP | 1 | Viva Namida                     | Yasuyuki Okamura | 1 | 1 | 1 |

Bei 9 Spalten stehen am Ende **Best, Super, Ultra**. Die optionale 10. Spalte ist **Sleepy (nicht Mega)**. Die 4. Spalte heißt **Load** (0/1 = Dateiverfügbarkeit), **keine** OP-/ED-Nummer.

- Der Dateiname in Unsorted ist der primäre Matchschlüssel. Ist "OP" oder "ED" separat angegeben, kann der Builder den Typ an den Anime-Namen anhängen: etwa "Space Dandy" + "OP" → "Space Dandy OP".
- Ein bereits vorhandenes "OP 2" wird **nicht** durch die Zahl in der Nr-Spalte überschrieben: "Samurai Flamenco OP 2" bleibt "Samurai Flamenco OP 2".
- Die zweite Spalte unterstützt "1996 3.", "2027 Q4" sowie "Winter 2026".
- Nicht eindeutige Treffer werden in einem Prüfbericht aufgeführt, nie per Fuzzy-Matching blind gesetzt.

## Die wichtigste Logik: Best als Auffangnetz

- Der komplette Unsorted-Pool liegt zunächst in **Best**. So wird niemals ein im Pool vorhandener Track durch eine unvollständige Research-Tabelle vergessen.
- **Nur ein ausdrücklich zugeordnetes 0/0/0** (bzw. 0/0/0/0 bei zusätzlicher Sleepy-Spalte) bedeutet **Rest**.
- Wenn **Super, Ultra oder Sleepy** gesetzt ist, gehört der Song **immer auch zu Best**, selbst wenn die Best-Spalte in der Research-Liste 0 war. Die drei oberen Stufen sind unabhängig voneinander, nicht automatisch ineinander enthalten.
- Nicht im Research erkannte Titel bleiben in **Best** und sind in der Tabelle mit **Best auto** gekennzeichnet. Nicht gefundene und mehrdeutige Research-Zeilen werden separat angezeigt.
- **Dateiname ist verbindlich:** Der Musik-**Dateiname** aus Unsorted, nicht der Interpret/Song aus `#EXTINF`, entscheidet bei eindeutiger Research-Zuordnung über die Playlist-Flags. Auch bei **abweichenden Metadaten** gilt die Research-Einstufung einschließlich `0/0/0/0 = Rest`; `#EXTINF` bleibt im Export unverändert. Beispiel: `Aquarian Age - Sign for Evolution ED 2.mp3` wird aus Research als `ED 2` klassifiziert, obwohl das `#EXTINF` etwas anderes benennt. Nur bei **fehlendem Dateinamen-Treffer** darf Metadatenvergleich als Hinweis bei der manuellen Alias-Prüfung helfen. Leerzeichen/Satzzeichen werden konservativ normalisiert, `OP` vs. `OP 2` unterscheidbar. **OPv2, OPv3, OPv57 und Mischungen/Sprachversionen erben die Kategorien des ursprünglichen OP; EDv2 entsprechend von ED.** Ungeprüfte Fuzzy-Aliasse werden nicht automatisch übernommen.
- Die Checkboxen in der UI erlauben manuelle Nachbearbeitung, inklusive explizitem Rest.

## Einzelne Seasons (Q1–Q4) – auch für 2026 und 2027

- Bei saisonalen Unsorted-Dateien wie "2026 Q3 Unsorted.m3u8" ist die **gesamte Datei automatisch Q3**. Fehlendes Q4 wird nicht erfunden.
- Bei einer **Jahres-Unsorted** wird das Quartal aus der zweiten Research-Spalte pro Song übernommen; unbekannte Quartale stehen als "?" in der Tabelle.
- Songs ohne sichere Quartalszuordnung werden **nicht still einer Season zugeordnet**. Vor einem Q1–Q4-Export gibt es eine Warnung über die noch nicht zugeordneten Songs. Man kann ein Quartal für markierte Songs manuell festlegen.
- Der Jahres-Export behält immer die volle Auswahl des Unsorted-Pools. "Ganzes Jahr" bedeutet alle aktuell vorhandenen Songs in der Unsorted, **nicht** dass die Saison vollständig recherchiert ist.

## Technische Garantien

- Ausgabe ist genau eine **.m3u8**, UTF-8 ohne BOM.
- Original-EXTINF-Zeilen und Datei-URIs (z. B. file:///F:/Musik/...) bleiben unverändert und in Originalreihenfolge.
- Keine FLAC- oder MP3-Datei wird geöffnet, bewegt, überschrieben oder gelöscht.
- Die Eingabe-Unsorted kann **nicht** als Ausgabe überschrieben werden.
- Das Fenster benötigt nur die Python-Standardbibliothek (Tkinter). Der portable Windows-Build erfolgt mit PyInstaller.

Entwicklungs-Tests: python -m unittest discover -s tests -v

**Hinweis zu Sonderfällen:** Namens-Aliasse, andere Transliterationen und fehlende Quellen bleiben Prüf-Fälle. **Numerische v-Versionen werden dagegen automatisch gruppiert**: `OP`, `OPv2`, `OPv57` bekommen gleiche Super-/Ultra-/Sleepy-Häkchen; `OP 2` ist eine andere Theme-Gruppe. Die automatische Gruppierung darf keinen Track aus Best entfernen.
