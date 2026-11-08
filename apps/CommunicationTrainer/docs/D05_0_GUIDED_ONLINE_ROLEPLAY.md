# D05.0 – erster KI-Trainingsdialog (experimentelle Android-Vorschau)

**Status: Implementiert als Draft-PR, noch nicht auf dem Galaxy A32 5G freigegeben. Keine Produktivfreigabe und keine Merge-Freigabe.**

## Ziel

Mehrere realistische KI-Antworten in einem *fiktiven* Kommunikationsrollenspiel ermöglichen, ohne bestehende D03-Chats, Entwürfe oder die Room-Datenbank zu verändern.

Die vorhandene, auf dem Samsung getestete D04.2-ChatGPT-Plan-Anmeldung und D04.3-Responses-Probe werden wiederverwendet. **Keine zusätzliche API-Key-Nutzung, keine kostenpflichtige API-Ausweichroute, kein Modell auf dem Gerät.**

## Szenarien

1. **Eine Bitte freundlich ablehnen:** Fiktive Kollegin Lena bittet kurz vor Feierabend um Übernahme einer Aufgabe. Ziel: freundlich und bestimmt eine Grenze setzen.
2. **Ein schwieriges Gespräch führen:** Fiktiver Freund Alex ist nach einer Absage enttäuscht. Ziel: Gefühle anerkennen und Missverständnisse klären.

Es sind keine echten Kontakte oder Kommunikationspartner. Die KI soll maximal zwei kurze deutsche Sätze je Zug liefern, auf den Verlauf eingehen und nach expliziter Grenze nachgeben. Coaching, Bewertung, Therapie und langfristige Erinnerungen sind bewusst **nicht** Bestandteil dieser Version.

## Technische und Datenschutzgrenzen

- D05 erscheint als **getrennter Abschnitt in Üben**, nicht in den gespeicherten D03-Testchats. Bestehende Room-v1-Tabelle/Schemadateien bleiben identisch.
- **Explizite Einwilligung vor Trainingsbeginn** und ein separater Button **„Diese Nachricht online senden“** bei jedem Zug. Kein automatisches Senden beim Tippen.
- **Nur** die vom Nutzer in genau diesem neuen D05-Rollenspiel eingegebenen Texte und die dort vollständig erhaltenen KI-Antworten gehen als `input` an ChatGPT. Keine D03-Chats, Entwürfe, andere Übungen, Profile, Namen aus Kontakten oder Telefon-Dateien.
- **Bis zu fünf User-Nachrichten pro Anmeldesitzung und nur ein D05-Rollenspiel pro Login**, je höchstens 500 Zeichen. Keine Hintergrundanfragen, automatischen Wiederholungen oder bezahlten Fallbacks. Abbruch/Fehler zählt als verbrauchter Versuch und beendet die Sitzung.
- Der `model`-Slug stammt ausschließlich aus `GET /v1/models` für den verifizierten Nutzer; `instructions` beschreibt das fiktive Rollenspiel und seine erste Äußerung. `input` enthält ausschließlich die lokal flüchtige Historie aus D05 und die neue Nachricht.
- Responses Preview: `POST https://api.openai.com/v1/responses` nur über verified ChatGPT-Plan Bearer; `store:false`, `stream:true`, ohne `previous_response_id`, `conversation`, `background`, `max_output_tokens`, `temperature`, `metadata` oder `user`. Kontext wird bei jedem Zug explizit im `input` gesendet.
- Nur nach SSE `response.completed` wird der neue User-/KI-Zug zur flüchtigen Liste hinzugefügt. Zu lange oder unterbrochene Streams werden abgewiesen, nicht als Erfolg dargestellt.
- D05-Verlauf wird nicht in Datenbanken, SharedPreferences, Dateien, Android SavedInstanceState, Analytics oder Logs gespeichert; nach Prozessende ist er verloren. **„Übung beenden und Verlauf löschen“** leert die Liste im RAM. Bei Neuanmeldung wird D05 ebenso zurückgesetzt.
- Tokenweitergabe nur innerhalb des eigenen Prozesses und nur solange der bereits signaturgeprüfte D04-Plan-Token gültig ist. Kein Refresh-Token-Speicher.
- Technische Grenzen: 500 Zeichen/Benutzernachricht, maximal 1600 Zeichen/KI-Antwort, höchstens fünf Fragen, maximal 30 KB Request, 1 MiB Stream, 16 KiB pro SSE-Event und 90 Sekunden Streamlaufzeit. App-Netzwerk-Timeout 20 Sekunden je Read.
- Credits: Nutzer muss ausdrücklich bestätigen, dass unter ChatGPT **Einstellungen → Nutzung** die App-Kontingente und eventuelle Credits geprüft wurden. Die App kann eine serverseitig erteilte Freigabe für Credits nicht selbst ausschließen.

## Android-Gerätetest

1. Getrennte D05-Debug-APK installieren, vorhandene D03/D04-Test-Apps bestehen lassen.
2. Unter **Üben** neu mit ChatGPT anmelden (getrennte App-Paketkennung, kein Token-Transfer).
3. Unter **D05** verfügbares Modell laden, Szenario auswählen, Credit-Hinweise lesen und Einwilligung aktivieren.
4. Fiktiven Startsatz anzeigen lassen, eine Antwort ohne private Angaben schreiben und ausdrücklich online senden. Eine echte mehrzügige Sequenz (2–3 Nachrichten) prüfen.
5. Kontrollieren, dass sich die KI auf die vorherige Nachricht bezieht und nicht immer den gleichen Begrüßungssatz antwortet.
6. Die D03-Chatliste getrennt öffnen: alle bisherigen gespeicherten Nachrichten/Entwürfe sollten unberührt sein.
7. **Übung beenden und Verlauf löschen**, bei erneuter Öffnung keine D05-Nachrichten mehr sichtbar.
8. Auf Fehler/Offline/Quota: keine automatischen Fallbacks und keine Wiederholung. Fehler melden, ohne OAuth-URLs, Codes oder echte private Inhalte weiterzugeben.

## Review-/Merge-Gates

- [ ] Kotlin-/Robolectric-Tests erfolgreich: Rollenfolge, nur D05-Kontext, Größenlimits, Modellvalidierung, SSE-Abschluss, Fehlerfälle
- [ ] Room-v1-Schema unverändert
- [ ] Android signierte Debug-APK mit korrekter separat installierbarer Kennung
- [ ] Geräteabnahme Android 13 / Galaxy A32 5G: Anmeldung, 2+ aufeinander aufbauende Nachrichten, Offline-/Fehlerverhalten, Löschen, lokale Chats unberührt
- [ ] Nutzerfreigabe vor Merge

**Noch offen für D05.1+:** sicherer Dauerbetrieb mit persistenten Sessions und Tokenrotation, Gesprächsqualität gegen Szenarien prüfen, explizite Lösch-/Aufbewahrungspolitik für mögliche Online-Chat-Speicherung, Kosten-/Quota-UX und längere Rollenverläufe. Kein Anschein einer fertigen Coaching-App.
