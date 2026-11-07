# D03 – Chat mit ausdrücklich simulierten Antworten

**Stand:** Implementierung zur Review in einem eigenen D03-PR; kein Merge ohne Nutzerfreigabe.

## Erster nutzbarer Chat

Unter **Chats → Neues Testgespräch** kann man nun lokale Unterhaltungen beginnen. Eigene Textnachrichten werden in Room gespeichert. Der Testpartner antwortet ausschließlich mit **deterministischen, vorformulierten Beispielen**. Auf jedem Gespräch ist sichtbar: **Simulation – keine echte KI, keine reale Person, keine Netzwerkübertragung.** Es gibt kein KI-Modell, keine Provider-API, keine Schlüssel und keine Internetberechtigung.

D03 verwendet unverändert das D02-Room-Schema **v1**. Die Entwurfs-Speicherung wird nicht ausgetauscht; die schon versionierte Room-v1-Schemadatei muss exakt unverändert bleiben. Vor einer Änderung an v1 ist eine explizite Migration notwendig.

## Nachrichten-Lebenszyklus und Schutz vor Datenverlust

1. Jede Eingabe wird unverzüglich als Draft mit streng wachsender `editRevision` gespeichert. Der **Senden**-Button wird erst nach bestätigter Room-Transaktion aktiviert.
2. Eine Room-`@Transaction` prüft `expectedRevision`, legt genau **eine** User-Message mit eindeutiger `clientMessageId` und `status=queued` an und setzt erst danach den gesendeten Entwurf auf leer.
3. Der lokale Simulator schreibt eine Antwort mit abgeleiteter stabiler `clientMessageId` (Namensraum `mock:`). Als Teil derselben Transaktion erhält die ursprüngliche User-Message `status=sent`.
4. Falls die Antwort absichtlich fehlschlägt, wird die User-Message `failed`. **Testantwort erneut versuchen** benutzt die vorhandene Message und erzeugt weder eine zweite eigene Nachricht noch eine zweite Antwort.
5. Bei einem harten Prozessabbruch zwischen dem Commit der User-Message und der Testantwort bleibt `queued` dauerhaft gespeichert. Nach dem Neustart kann der Nutzer die **gleiche** simulierte Antwort explizit erneut anfordern; keine heimlichen Hintergrund-Retries.
6. Eine frühere bzw. verzögerte Draft-Speicherung darf die bereits geleerte, höher revisionierte Draft-Zeile nicht überschreiben. Ein DB-Fehler vor der Transaktion lässt Draft und bisherige Messages bestehen.

Die UI zeigt laufende, wartende und fehlgeschlagene Testantworten ausdrücklich an. Noch nicht bestätigte Eingaben können bei sofortigem Prozess-Tod weiterhin verloren gehen; deshalb bleibt der Button bis zur Speicherbestätigung gesperrt.

## D03 Scope / Absicherung

- Nachrichtenblasen unterscheiden Sender durch Ausrichtung **und Beschriftung**, nicht nur durch Farben.
- Schriftfelder bleiben mit IME-resizing und Scrollbereich bedienbar. Kein großes Hero-Banner innerhalb eines laufenden Chats.
- Üben zeigt weiterhin eine Platzhalter-Vorschau. Figuren-Personas, echte KI, Coaching, Replay-UX, Netz-/Cloud-Modus, Anbieterwahl und Sicherheitsmoderation sind spätere Meilensteine.
- Daten bleiben in Room innerhalb der App-Sandbox. **Keine** zusätzliche App-Level-DB-Verschlüsselung wird behauptet; Android-Backup bleibt aus.
- GitHub-Debug-APK hat die eigene Paketkennung `com.valoon4.communicationtrainer.d03preview` und Label **CommunicationTrainer D03 Test**, damit D02-Daten nicht für eine neue Signatur deinstalliert werden müssen. Die D03-Preview startet mit **eigenem leerem** Datenspeicher; sie übernimmt **keine** D02-Entwürfe. Eine stabile, geheim verwaltete Debug-Signatur / Versionsupdate-Pipeline ist als separates Qualitätsgate vor datenhaltigen Mehrversions-Tests weiterhin offen.

## Testplan

| Prüfung | Automatisiert | Manuell auf Android |
|---|---|---|
| Erster Versand | Room-Sende-Transaktion inkl. Draft-Löschung | Nachricht eingeben, auf „Entwurf gespeichert“ warten, Senden |
| Doppeltes Tippen | unique `clientMessageId`, identische Sendung wird nicht dupliziert | Zweimal schnell auf Senden tippen |
| Unbestätigte Eingabe | ungültige Revision / leerer Text wird abgewiesen | Text eingeben und sofort Senden kontrollieren |
| Antwort | deterministische Mock-Engine, eine Antwort pro User-Nachricht | gut erkennbare Kennzeichnung „Testpartner · Simulation“ |
| Fehler/Retry | failed → queued → sent; keine zweite User-Message | Checkbox „Nächste Testantwort absichtlich scheitern lassen“, dann Retry |
| Prozessabbruch | Room-Close/Reopen erhält queued-Status und Nachrichten | Force-Stop und Neustart, queued-Nachricht erneut versuchen |
| Mehrere Gespräche | FK- und Isolationstests aus D02 | zwei Chats getrennt fortführen |
| Löschen | CASCADE für Conversation, Nachrichten und Drafts | Gespräch löschen, nur dieses verschwindet |
| Rotation / Tastatur | bestehender Compose-Speicheransatz | lange Texte, große Schrift, Dark Mode, Tastatur und Rotation |
| Migration/CI | Room-Schema-v1 unverändert, Unit-Tests, Debug-APK mit Signatur | D03-App parallel zu D02 installieren |

**Abnahmekriterium:** APK-CI grün, keine fremden App-/Workflowdateien, manuelle Gerätetests noch gesondert bestätigen. Ohne Gerätelauf keine Behauptung vollständiger UX-/TalkBack-Abnahme.
