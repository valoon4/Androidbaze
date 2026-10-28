# Emotionskompass – klickbarer UX-Prototyp M0.5

**Stand:** 28.10.2026 · **Version:** M0.5 · **Status:** Testbarer, eigenständiger HTML-Prototyp – **keine Android-App und keine APK**.

## Starten

Die Datei [index.html](index.html) herunterladen und im Browser öffnen. Alle CSS-, JavaScript- und Beispieldaten sind **direkt eingebettet**: keine Abhängigkeiten, keine Netzwerkaufrufe, kein Backend. Alternativ die Datei über einen lokalen Webserver ausliefern (z.B. `python -m http.server` in diesem Verzeichnis).

Der Prototyp zeigt responsive Ansichten für Smartphone und Desktop und kann offline benutzt werden. Er ist ein UX-Simulator und wird nicht als produktives Tagebuch empfohlen.

## Bedienbare Bereiche

- **Start:** Gleichwertige Einstiege „Gefühl erkunden“ und „Emotionen entdecken“.
- **Emotionsatlas:** Acht Familien, derzeit 36 exemplarische Begriffe, Suche mit priorisierter Namensübereinstimmung.
- **Emotion im Detail:** verständliche Definition, Beispiel, verwandte Emotionen, Wechsel in Vergleich oder Reflexion.
- **Vergleich:** zwei Begriffe auswählbar; Frustration/Enttäuschung als ausgearbeitetes Demo-Beispiel.
- **Check-in:** kurz/ausführlich, drei Einstiegspfade (unklar, ungefähre Ahnung, Situation), optionale Valenz/Aktivierung, mehrere Emotionen samt optionaler Intensität, Lexikon-Querverweise.
- **Reflexion:** sechs Fragen zu Situation, Gedanken, Körperwahrnehmung, Bedürfnissen, Reaktion und Erkenntnis; alle freiwillig.
- **Zusammenfassung:** Angaben prüfen, Gefühle/Gedanken bearbeiten oder ohne Speichern beenden.
- **Tagebuch:** Einträge **nur in laufender Browser-Sitzung**, ansehen, bearbeiten, löschen. Ein klar gekennzeichneter fiktiver Beispieldatensatz.
- **Rückblick:** erklärte P1-Vorschau ohne echte Auswertung.
- **Infos:** Datenschutz-Hinweis, Demo-Daten zurücksetzen.

## Datenschutzgrenze dieser Vorschau

Keine Konten, kein Backend, keine Netzwerkrequests, **keine dauerhafte Speicherung** (weder localStorage noch IndexedDB/SQLite). Einträge verbleiben nur im JavaScript-Arbeitsspeicher der geöffneten Seite und gehen beim Neuladen verloren. Daher keine echten, vertraulichen Tagebuchtexte eingeben. Die native App muss erst durch späteres P0-Security-Gate, inklusive verschlüsselter lokaler Datenhaltung.

## Testfälle und bekannte Abgrenzungen

Automatisierte Playwright/Chromium-Smoke-Tests wurden an einem responsiven Browser-Render ausgeführt (siehe [UX-Protokoll](../notes/UX_PROTOTYP_V1.md)). Getestet: Atlas-Suche, Detail, Vergleich, lange und kurze Flows, gemischte Gefühle, Mehrfachauswahl, Begriffsdetails aus dem Check-in und Rückkehr ohne Auswahlverlust, unklare Check-ins, fiktive Session-Daten übernehmen, bearbeiten, löschen, Situation-zuerst-Flow sowie Desktop-Layout.

Noch **nicht** Teil des Prototyps: Android-APK, echte Datenbank, Verschlüsselung, geschützter Export, statistische Auswertung, vollständig redaktionell geprüfter 80-Begriffe-Atlas, native Android-Back-Taste und persistierte Drafts, abschließende Accessibility- und Nutzertests.

## Nächster Schritt

UX-Review (Wording, Navigation, Verständlichkeit, Screenreader/Dynamische Textgrößen) und danach **M1: eigenständiges Kotlin/Compose-Projekt** unter `games/Emotionskompass/src`. Die bestehende Repo-Root-App bleibt erhalten.
