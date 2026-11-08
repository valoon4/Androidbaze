# D05.0 – KI-gestützter Trainingsdialog, isolierter Pilot

**Status:** Implementierter Android-Prototyp, GitHub-CI und echter Gerätetest sind separate Freigabe-Gates.

## Ziel

Ein realistischer, mehrzügiger fiktiver Gesprächspartner zur Kommunikationsübung über die vom Nutzer bereits autorisierte ChatGPT-Plan-Nutzung. Bewusst kein stiller Ersatz der D03-Mock-Chats: vorhandene Room-v1-Daten, Entwürfe und lokale Chats bleiben unverändert. Keine Datenbankmigration.

Zwei feste Übungssituationen:
1. Freundlich Nein sagen – eine Kollegin bittet kurzfristig um zusätzliche Arbeit.
2. Aktiv zuhören – ein enttäuschter Freund möchte ernst genommen werden.

Die App sendet pro Runde nur die festgelegte fiktive Rollenbeschreibung, den **ausschließlich innerhalb dieses Pilotdialogs eingegebenen** Text und dessen bisherige Rollen-Historie. Ein leerer/synthetischer Nutzerverlauf wird nicht durch private Daten aus Room ergänzt. Für Kontextzwecke werden bereits in diesem Pilot erfolgte Runden erneut mitgesendet.

## Sichtbarer Schutz & Datenfluss

- Benutzer muss sich in der **separat installierbaren D05-Pilot-App** selbst per ChatGPT-OAuth anmelden und ein tatsächlich kontoberechtigtes Modell aus `GET /v1/models` auswählen.
- **Vor jeder Nachricht** separate Checkbox: Online-Übertragung genau dieses Textes und bisherigen Pilotverlaufs. Checkbox setzt sich unmittelbar beim Sendestart zurück.
- `POST https://api.openai.com/v1/responses` mit dem ausschließlich im Prozessspeicher vorhandenen OAuth Bearer, `store:false`, `stream:true`; kein API-Key oder kostenpflichtiger Provider-Fallback.
- Genau zwei Szenarien, max. **4 Nutzerrunden**, max. **600 Zeichen** pro Nutzerturn, Antwort max. **2500 Zeichen**, begrenzte Event- und Streamgrößen, kein automatischer Retry und keine heimlichen Folgeanfragen.
- Antwort gilt nur nach vollständig erhaltenem `response.completed` als erfolgreich; sonst bleibt der unsent lokale Text stehen und ein Fehler wird angezeigt. Ein Nutzer darf anschließend durch einen neuen Klick bewusst einen Versuch starten; Server-Deduplikation ist in diesem Pilot noch nicht vorhanden.
- Keine automatische Speicherung von Dialogen, Entwürfen, Tokens, Modellantworten oder Consent in Room/Preferences. Pilot-Entwurf und Verlauf bleiben bei UI-Navigation im App-Prozess erhalten, aber **nicht nach Prozessende** (warnende UI). Nicht für sensible/echte Privatinhalte benutzen.
- Beim OAuth-Kontowechsel werden Pilot-Entwurf/Verlauf/Consent zurückgesetzt; auslaufende ältere Anfragen werden anhand Session-Generation verworfen.
- ChatGPT-Credits/App-Limits müssen **vor Test durch Nutzer geprüft werden**; App selbst kann Kontoeinstellungen nicht abrufen und keine Null-Kosten-Garantie geben.
- Kein Coaching/Feedback, keine Psychotherapie, keine diagnostischen Aussagen, kein Produktionsmodus.
- Die D04.3-Einmal-Testfunktion bleibt parallel vorhanden; der D05-Pilot ist ein separat bestätigter Übungsmodus. Bestehender D03-Mockmodus bleibt lokaler Offline-Standard.

## Gerätetest (Galaxy A32 5G, Android 13)

1. D05-Debug-APK mit separatem App-Label `CommunicationTrainer D05 Pilot` und Paketkennung `.d05trainingpreview` installieren.
2. Unter **Üben** ChatGPT-Konto verbinden; sicherstellen, dass die Plan-Berechtigung bestätigt wurde.
3. Unter **D05.0** ein Szenario wählen und per separatem Button das Kontomodellangebot laden; ein Modell auswählen.
4. Nur fiktiven Text eingeben, z. B. `Danke fürs Fragen, aber heute kann ich das nicht übernehmen.`
5. Prüfen, dass ohne aktivierte Zustimmung **keine** Anfrage gesendet werden kann. Einwilligung aktivieren, dann absenden; Antwort sollte fiktiv und situationsbezogen sein.
6. Eine zweite Pilotnachricht senden. Die Antwort soll den vorherigen Trainingsverlauf berücksichtigen. Es werden dabei keine bisherigen D03-Chats übertragen.
7. „Neues Training beginnen“ verwirft alles (ohne Cloud-Löschung, da es nicht in App-DB gespeichert wird). Szenariowechsel leert Verlauf und Consent. Bei Logout/anderem Konto wird Verlauf gelöscht.
8. Im Fehlerfall kein automatisches Wiederholen. Test-Screenshot ohne OAuth-URL, Access-Token oder sensible Inhalte melden.

## Qualität- und Sicherheitsgates

- [ ] CI-Tests inkl. consent, Eingabegrenzen, Szenario-/Kontextrollen, Session-Invalidierung bestanden
- [ ] APK-Paketkennung, Signatur, Room-v1-Schema und APK-SHA-256 überprüft
- [ ] Echte zweizügige Rollenspielinteraktion auf Galaxy A32 5G bestätigt
- [ ] Sichtbare Datenschutzhinweise und Credit-Limit-Einstellungen verständlich
- [ ] Unterbrechungen, Kontingentgrenzen, fehlerhafte Antworten und Szenariowechsel manuell geprüft

**Vor einer echten Integration in den normalen Chatbereich:** dauerhafte und sichere Speicherung mit Nutzerzustimmung, robustes Request-Idempotency-Konzept, Abbruch, Restore nach Prozess-Tod, klare Rollentrennung, sichere Session-/Token-Verwaltung und qualitative Übungsevaluation. Kein automatischer Merge.


## D05.0.1 – Szenario beginnt mit konkretem Gegenüber (UX-Fix, 08.11.2026)

**Grund der Nachbesserung:** Im ersten Gerätetest wurde das Szenario „Freundlich Nein sagen“ trotz natürlich wirkender und kontextbezogener KI-Antworten **nicht tatsächlich geübt**: Nach zwei Nutzernachrichten hatte der KI-Partner noch keine Bitte ausgesprochen. Die positive Aussage über Dialogqualität war daher nicht ausreichend, um die Szenariotauglichkeit zu bestätigen.

**Korrektur:**
- Jedes Szenario beginnt mit einem **festen, vollständig sichtbaren und ausdrücklich als vorgegeben markierten** Gegenüber-Turn, noch bevor der Nutzer erstmals antwortet.
- „Freundlich Nein sagen“: Die Kollegin bittet konkret darum, ihren Bericht noch heute fertigzustellen.
- „Aktiv zuhören“: Der Freund äußert bereits eine konkrete Enttäuschung über das ausgefallene Treffen.
- Der vorgenerierte Einstieg ist kostenlos, ohne Netzwerkzugriff; **nicht** als echte KI-Antwort ausgeben. KI wird erst nach Eingabe und erneuter Freigabe des Nutzers aufgerufen.
- Die Szene und der genaue Einstieg sind Bestandteil jeder folgenden Online-Anfrage. Rollenhistorie wird strikt auf `assistant` (Einstieg), dann abwechselnd `user`/`assistant` geprüft. Manipulierte oder unvollständige Historie wird blockiert.
- Szenario- oder Modellwechsel startet den Einstieg neu, verwirft lokalen Pilotverlauf und setzt Consent zurück. Keine Veränderung an vorhandenen D03-Chats oder Room-Entwürfen.
- Für die neue Test-APK wird eine weitere eigene Paketkennung `com.valoon4.communicationtrainer.d05openingpreview` verwendet; damit entsteht kein Signaturkonflikt mit dem bisherigen D05-Pilot-Build.

**Erneute D05-Abnahme:**
1. „Freundlich Nein sagen“ wählen. **Vor** der ersten Eingabe muss die Kollegin bereits eine klare Bitte ausgesprochen haben.
2. Die erste eigene Antwort soll direkt eine freundliche Absage sein können; es ist **keine zusätzliche Aufwärmrunde** nötig.
3. Nach bestätigter Online-Anfrage prüfen, ob die KI weiterhin die Kollegin spielt und die Absage sinnvoll aufgreift. Mindestens eine weitere Runde testen.
4. „Aktiv zuhören“ auswählen; der Freund soll **bereits beim Start** konkrete Enttäuschung äußern.
5. Auf Szenario- oder Modellwechsel prüfen: alter Verlauf nicht sichtbar, Consent zurückgesetzt, neuer Einstieg vorhanden.
6. Screenshot des Anfangs und eine Beobachtung zur KI-Kontexttreue teilen, ohne echte Personendaten, OAuth-Tokens oder Login-URLs.

**Kein automatischer Merge:** Der bisherige erfolgreiche CI-Lauf und der erste qualitative Gerätetest bleiben dokumentiert, aber die Szenario-Abnahme wird mit der korrigierten Version wiederholt.


## D05.0.2 – Rollenfakten eindeutig gemacht

**Anlass:** In der erfolgreich getesteten Übung „Aktiv zuhören“ schloss die KI aus dem zunächst unbestimmten Wortlaut, dass die übende Person das Treffen abgesagt hat. Diese Annahme wurde erst während des Dialogs getroffen und war in der Szenariobeschreibung nicht festgelegt.

**Entscheidung:** Im Szenario ist **die übende Person diejenige, die das Treffen abgesagt hat**; der Freund ist deshalb enttäuscht. Diese Festlegung passt zum bereits getesteten Dialog, wird vor dem ersten Turn sichtbar erklärt und in der vorgegebenen ersten Aussage („Als du unser Treffen abgesagt hast …“) genannt. Nutzer sollen zunächst das Gefühl verstehen, nicht zwingend sofort ihre Absage rechtfertigen.

Der Dialog-Prompt enthält dieselben Rollenfakten und untersagt widersprüchliche Rollenfestlegungen sowie erfundene Erklärungen für die Absage. So entsteht weniger Interpretationsspielraum; KI-Gehorsam kann dennoch nicht allein durch einen Prompt garantiert werden. Ein Robolectric-Test prüft die sichtbare Beschreibung, den vorgegebenen Einstieg und den ausgehenden KI-Kontext.

**Abnahmekriterium:** In der neuen APK auf „Aktiv zuhören“ wechseln, vor dem Senden Rollenklärung lesen, Einstieg kontrollieren und ggf. einen weiteren kurzen Dialog auf Rollenstabilität prüfen. Die vorherige Praxiserfahrung belegt realistische KI-Reaktionen, nicht die Vollständigkeit aller möglichen Antwortverläufe. PR #66 bleibt bis Nutzerfreigabe ungemergt.
