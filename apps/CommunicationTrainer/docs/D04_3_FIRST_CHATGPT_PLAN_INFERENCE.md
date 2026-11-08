# D04.3 – erste echte ChatGPT-Plan-Antwort (Android-Vorschau)

**Stand 08.11.2026; neue Funktion noch nicht am Zielgerät ausgeführt.** Der Nutzer hat die Anmeldung und gesonderte Planberechtigung im bisherigen D04.2-Callback-Test bestätigt. Diese Bestätigung ersetzt **keinen** API-/Kosten-/Streaming-Gerätetest.

## Offizielle Schnittstelle

- [OpenAI: Models and inference](https://developers.openai.com/siwc/token-sharing-open-source/models-and-inference)
- [OpenAI: Preview limitations](https://developers.openai.com/siwc/token-sharing-open-source/preview-limitations)
- [ChatGPT-Kontingente und Credit-Nutzung](https://help.openai.com/en/articles/20001542-using-your-chatgpt-plan-in-other-apps-and-sites)

Nach erfolgreichem Login erhält D04.3 einen geprüften OAuth Bearer-Access-Token, wenn *sowohl* `resource.invoke` als auch `chatgpt.tokens.use.direct` gewährt wurden. Die App behält ihn **ausschließlich vorübergehend im RAM** (bis Tokenablauf oder Prozessende). Es gibt **keine Refresh-Token-Speicherung** und keine neuen Berechtigungen außer bereits vorhandenem Internet.

Für die Modellliste: `GET https://api.openai.com/v1/models` mit Bearer; nur `models[]` mit `visibility = list` und validiertem `slug` erscheinen in der Auswahl.

Für genau **einen** ausdrücklich autorisierten Test nach dem Login:
- `POST https://api.openai.com/v1/responses`, HTTPS mit OAuth Bearer, **kein OpenAI-API-Key** und kein inoffizielles ChatGPT-Backend.
- `model`: nur per User aus der tatsächlichen Kontoliste ausgewählter `slug`.
- `input`: ausschließlich ein fest codierter synthetischer deutscher Begrüßungssatz, **niemals** D03/D04-Nachrichten, Entwürfe, Tagebucheinträge, Profile oder Nutzerdaten.
- `store:false`, `stream:true`; keine auf der ChatGPT-Plan-Route nicht unterstützten Felder (insbesondere **kein** `max_output_tokens`, `temperature`, `previous_response_id`, `background`, `user`).
- Antworttext wird nur bei Terminalereignis `response.completed` als Erfolg gezeigt. `response.failed`, `response.incomplete`, Kontingentfehler, Stromabriss und Timeout werden als Fehler dargestellt. Partieller Text ist kein Erfolg.
- Der Test verbraucht schon beim Start den **einzigen** lokalen Versuchsplatz der aktuellen Anmeldung – auch bei Netzwerkfehler. **Kein automatischer Retry, kein Cloud-/API-Fallback, keine Umgehung von ChatGPT-Nutzungsgrenzen.**
- Ergebnis und End-to-End-Dauer sind ausschließlich für die offene App sichtbar und nicht persistent.
- Der Anmelde- und der Modelllistenabruf lösen keine Inferenzanfragen aus.

## Kosten- und Berechtigungsgate

**ChatGPT-Plus ist keine allgemeine kostenlose OpenAI-API-Flatrate.** Die ChatGPT-Plan-Verknüpfung belastet das bestehende Nutzungsbudget. **Falls der Nutzer ChatGPT-Credits für Apps ausdrücklich freigegeben hat**, kann eine Anfrage nach Erreichen der Kontingente auch Guthaben verbrauchen; die App kann diese serverseitige Präferenz nicht selbst abfragen oder garantieren, dass keinerlei zusätzliche Kosten entstehen. Die UI verlangt vor dem Test eine eigene Bestätigung, dass das Budget und die Credit-Einstellung unter **ChatGPT → Einstellungen → Nutzung** überprüft wurden. Nutzer ohne Kostenrisiko sollten die Credit-Freigabe dort ausschalten bzw. App-Limit unter 100 % setzen.

## Gerätetest: Galaxy A32 5G, Android 13, 4 GB RAM

1. Die separate **CommunicationTrainer ChatGPT KI-Test** APK (`.d043planpreview`) installieren. D04.2 ist eine andere Android-App; sie kann keinen Access-Token übertragen. In der neuen App **erneut anmelden**.
2. **Üben → D04.2 → Weiter mit ChatGPT**, erfolgreiche Planfreigabe prüfen. Falls Callback versagt, zuerst den lokalen Browser-Selbsttest.
3. Im Abschnitt **D04.3** auf **„Für mein Konto verfügbare Modelle laden“** tippen. Ist die Liste leer, stoppen und Fehlermeldung weitergeben. Keine erdachten Modellnamen.
4. Ein sichtbares Modell auswählen. In ChatGPT unter **Einstellungen → Nutzung** vorher Credits-App-Freigabe ausschalten, wenn keine zusätzlichen Kosten erwünscht sind. App-Kontingentlimit kontrollieren.
5. Die ausdrückliche Einmalbestätigung aktivieren und **„Einmalige ChatGPT-Testanfrage starten“** drücken. Nicht mit echten privaten Nachrichten testen.
6. Screenshot von Ergebnis/Fehler und End-to-End-Antwortzeit **ohne Anmelde-URL, Code, E-Mail, Token oder Kontoinfos** zurückmelden. Keine automatischen weiteren Anfragen.
7. Falls Android die App beendet, wird der flüchtige Access-Token verworfen; der lokale Test darf nicht automatisch wiederaufgenommen werden. Eine neue Einmalanfrage würde eine erneute Anmeldung und Zustimmung erfordern.

### Technische und fachliche Stop/Go-Gates

- [ ] GitHub CI (JVM/Robolectric-Tests inkl. Preview-Request-Felder, Konto-Katalog, SSE-Fehler/Terminalereignis) erfolgreich
- [ ] signierte APK/Package-Identität und SHA-256 überprüft
- [ ] erfolgreiche erneute Anmeldung auf Galaxy A32 5G
- [ ] Kontomodelle werden vom echten OpenAI-Endpunkt angezeigt
- [ ] Einmaliger synthetischer Test liefert `response.completed`, kein Real-Chat
- [ ] Antwortqualität, Geschwindigkeit und Kontingent-/Credit-Verhalten getrennt beurteilen

**Kein Merge ohne Nutzerfreigabe.** PR #61/#62/#63 bleiben unverändert offen; D04.3 ist ein gestapelter, getrennt installierbarer Draft-PR. Weder private Chats noch andere Apps werden verändert.
