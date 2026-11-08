# D04.2 – Android „Sign in with ChatGPT“ / Kontingentfreigabe (OAuth-Experiment)

**Status: experimentelle Android-Implementierung. Noch keine erfolgreiche Nutzeranmeldung oder ChatGPT-Inferenz nachgewiesen.** Basierend auf dem ungemergten PR #61; getrennt von D04.1-Qwen (PR #62). Keine Änderung an InnerWave, TimeFlow oder anderen Apps.

## Verifizierte Quellen (08.11.2026)

- https://developers.openai.com/siwc/quickstart
- https://developers.openai.com/siwc/token-sharing-open-source
- https://developers.openai.com/siwc/token-sharing-open-source/sign-in
- https://developers.openai.com/siwc/token-sharing-open-source/models-and-inference
- https://developers.openai.com/siwc/token-sharing-open-source/preview-limitations
- https://help.openai.com/en/articles/20001542-using-your-chatgpt-plan-in-other-apps-and-sites

Offizielle OSS-OAuth-Clients können **nach ausdrücklicher Zustimmung** berechtigte ChatGPT-Plan-Nutzung mit OAuth erreichen; keine API-Key-Eingabe nötig. ChatGPT-Abonnement ist **nicht** automatisch eine allgemeine API-Flatrate. Anrechenbare Modellnutzung belastet das ChatGPT-Kontingent; explizit erlaubte Credits können Kosten verursachen. Es gibt separate App-Usage-Limits, die Nutzer in ChatGPT verwalten können.

**Plattformgrenze:** Beispiele und SDK `@siwc/local` beschreiben primär Desktop-Laufzeit mit lokalem Loopback-Callback. Die Android-spezifische Browser-/Callback- und Prozess-Lifecycle-Unterstützung ist noch nicht empirisch bestätigt. Keine falsche Aussage über erfolgreiche Android-OAuth-Verbindung.

## D04.2-APK (nur Anmeldeprobe)

- Eigenständige Paketkennung `com.valoon4.communicationtrainer.d04chatgptpreview`, App-Label `CommunicationTrainer ChatGPT-Test`, keine Datenmigration. D03/D04 bleiben getrennt.
- Bereich **Üben → D04.2 → Weiter mit ChatGPT**, ausdrücklich manuell gestartet.
- Öffnet **Systembrowser**, kein eingebettetes WebView und keine Passwortabfrage innerhalb der App.
- Dynamisches OSS-Client-Onboarding `client_id=dynamic_agent_client`, `agent_name_hint=CommunicationTrainer`, **einmaliger** Host-ID UUIDv4 `urn:uuid:...` pro Installation, nur Host-ID wird in App-Preferences dauerhaft gespeichert.
- Pro Anmeldeversuch neuer `state`, `nonce`, PKCE-S256 Verifier; `http://127.0.0.1:<random-port>/auth/callback` lauscht ausschließlich auf Loopback und hat Ablaufzeit. Prüft Rückmeldung auf Pfad und state, übernimmt ausschließlich einen von OpenAI ausgestellten `oaiapp_...` Client-ID.
- POST an offiziellen Token-Endpunkt mit Authorization Code + PKCE; liest JWKS per HTTPS, überprüft `RS256`-Signatur, Issuer, Audience, Ablaufzeit und Nonce des ID-Tokens.
- Die App wertet **tatsächlich gewährte** `resource.invoke` und `chatgpt.tokens.use.direct` Berechtigungen aus; der Identitäts-Login allein ist kein Nachweis für ChatGPT-Plan-Nutzung.
- **Nur Statusbeweis:** Kein `POST /v1/responses`, kein `GET /v1/models`, kein Chat-Verlauf wird hochgeladen, keine Cloud-Ausweichlogik; kein Modell wird geladen. Die OAuth-Credentials, E-Mail und Client-ID werden **nicht** auf dem Telefon persistiert; nur der Verifikationsstatus wird während der offenen App angezeigt. Bei App-Neustart wird erneut angemeldet.
- Keine Server-Secrets, kein API-Key, kein Logging von Tokens, Redirect-Query oder Nutzeridentität.
- Die hinzugefügte Android `INTERNET`-Permission wird nur durch explizite OAuth-Anmeldung verwendet; die lokale D03-Mock-Engine führt weiterhin keine Netzwerkkommunikation aus.

## Test auf dem Galaxy A32 5G, Android 13

1. Die neue APK installiert sich neben den bisherigen Testversionen; bestehende Chat-Daten anderer Paketkennungen nicht migrieren/ändern.
2. **Üben → D04.2**: Seite auf verständliche Opt-in-Texte prüfen, ohne anzumelden.
3. **Weiter mit ChatGPT** manuell wählen. Systembrowser muss zum offiziellen `auth.openai.com` führen (nicht Login-Daten in die App eingeben).
4. Konto/Workspace bewusst auswählen. Bei der Einwilligung insbesondere Plan-Nutzung prüfen; **in ChatGPT unter Einstellungen → Nutzung/Usage die App-Limits überprüfen und Credits deaktiviert lassen**, wenn keinerlei Verbrauch zusätzlicher Credits gewünscht ist.
5. Nach Redirect muss im lokalen Browser „Du kannst zu CommunicationTrainer zurückkehren“ erscheinen. App wieder öffnen und Ergebnis ansehen. Bei fehlendem Loopback-Redirect, Timeout, `invalid_client` oder sonstigem Fehler **keine alternative Anmeldung / keine Zugangsdaten senden**; Screenshot mit unkenntlich gemachter E-Mail und allen Token-/Code-URLs schicken.
6. **Erfolg bedeutet lediglich:** OAuth-Registrierung und ID-Token-Prüfung liefen erfolgreich; optional beantragte Plan-Berechtigung wurde erteilt. Es wurden keine KI-Anfragen durchgeführt und keine Modellverfügbarkeit getestet.

## Blocker / nicht Teil dieser APK

- Native Android-OAuth-Unterstützung und sichere Lebensdauer über Pause/Rotation/Prozess-Tod noch auf Gerät zu testen.
- Späterer Betrieb benötigt verschlüsselte Profilspeicherung per Android Keystore, sichere Refresh-Token-Rotation, Revocation, sichere Abmeldung und ggf. ein standardisiertes SDK.
- Echte ChatGPT-Antworten erst in einer separaten Phase mit explizitem UI zur Freigabe jedes Online-Dialogs und bestätigten App-Usage-Limits; nur synthetische Eingaben für die erste API-Messung.
- Preview-Responses-API verlangt `store:false`, `stream:true` und korrektes `input`-Array. Keine automatisch kostenpflichtige Fallback-API.
- Für eine kommerzielle oder remote gehostete Version gelten gesonderte Teilnahmebedingungen; OSS-Personal-App-Regeln nicht still auf alle Endnutzer-Szenarien übertragen.

## Abnahme

- [ ] Kotlin-/Robolectric-Unit-Tests inkl. RFC 7636 PKCE, registrierte Client-ID, JWT-Signatur/Nonce/Audience
- [ ] Room-Schema unverändert
- [ ] signierte, eindeutig installierbare APK im GitHub-Actions-Artefakt
- [ ] manueller Android-13-Systembrowser-Callback erfolgreich
- [ ] Identitätsprüfungen und separat erteilte Plan-Berechtigung bestätigt
- [ ] Datenschutz- und Produkttest vor jedem Merge; keine echten Nutzergespräche übertragen

**Kein Merge ohne gesonderte ausdrückliche Freigabe.**


## D04.2 Android-Callback-Härtung nach Ecosia-Timeout (08.11.2026)

**Gerätebeobachtung:** Der Browser (Ecosia) zeigte nach dem OAuth-Redirect auf `http://127.0.0.1:<port>/auth/callback` `ERR_CONNECTION_TIMED_OUT`. Dies belegt **keinen** falschen Login und auch keinen erfolgreichen Credential-Austausch. Mögliche Ursachen: Listener nach drei Minuten abgelaufen, App-Prozess im Hintergrund beendet, Browser-/Loopback-Inkompatibilität. Ursache bisher nicht exakt isoliert.

**Änderungen in dieser Iteration:**

- Eine getrennt installierbare `.d04callbackpreview`-App heißt `CommunicationTrainer Callback-Test`.
- Vor dem erneuten OAuth-Login steht **„1. Lokale Browser-Rückleitung testen (ohne Konto)“**. Dieser Test öffnet eine zufällige lokale `127.0.0.1`-Adresse, prüft einen einmaligen Nonce und zeigt eine Bestätigung ohne Kontakt zu OpenAI, Modellanfragen oder Nutzerdaten.
- Die Listener-Lebensdauer beträgt für OAuth höchstens acht Minuten; für den Selbsttest 90 Sekunden. Beide laufen in einem app-weit verwalteten Worker und nicht in einer beim Browserwechsel zerstörbaren Compose-Coroutine.
- Ein während der Anmeldung sichtbarer, zeitlich begrenzter Android-Foreground-Service mit einer unkritischen Benachrichtigung verhindert nach Möglichkeit, dass der Prozess beim Browserwechsel zu einem Cache-Prozess herabgestuft wird. Der Dienst beendet sich nach Erfolg/Fehler. **Keine absolute Garantie gegen Android-Prozessbeendigung.**
- Eine nicht geheime Statusanzeige nennt die Phase (Listener/Browser/Callback/HTTPS-Validierung). Browser-Codes, volle OAuth-URLs, Tokens und E-Mail werden weder geloggt noch in der Benachrichtigung angezeigt.
- Die originalen OAuth-Sicherheitsprüfungen (PKCE, State, Nonce, JWT-Signatur) bleiben erhalten, und die URL bleibt `127.0.0.1`: Die offizielle SIWC-Open-Source-Dokumentation verlangt weiterhin den Loopback-Redirect und unterstützt nicht beliebige Custom-Scheme-Ersatz-URIs.
- **Keine** Änderungen am normalen Chat, keine Inferenz und keine automatische Kredit-/API-Nutzung.

**Testabfolge:** Nur die neue Callback-Test-App installieren, unter Üben zuerst „Lokale Browser-Rückleitung testen“ auswählen; im Browser muss eine schlichte Bestätigungsseite erscheinen. Anschließend zurück zur App wechseln und die Statusmeldung überprüfen. Wenn bereits der lokale Test scheitert, **keine weitere ChatGPT-Anmeldung** versuchen; optional vorab mit Chrome als Standardbrowser statt Ecosia den identischen rein lokalen Test wiederholen. Kein Screenshot mit vollständiger `/auth/callback?...code=...`-Adresszeile weitergeben.

**Erst wenn der lokale Browsertest erfolgreich ist:** Mit „2. Weiter mit ChatGPT“ den offiziellen OAuth-Flow erneut ausprobieren. Der Browser darf weiterhin eine Seite auf `127.0.0.1` öffnen, nun mit lokal erreichbarem Listener. Ob die Anmeldung auf diesem Gerät vollständig funktioniert, ist weiterhin empirisch offen.

**CI-Gate:** Neue APK muss unter Paketkennung `com.valoon4.communicationtrainer.d04callbackpreview` signiert werden und alle bestehenden OAuth-Sicherheitstests bestehen. Erst danach zur Installation geben.
