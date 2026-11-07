# K5 – Datenschutz, Sicherheit, Moderation und Rechtsprüfung

**Version:** 1.0 · **Datum:** 07.11.2026 · **Status:** Privacy-by-Design-/Threat-Model-Entwurf, keine Rechtsberatung und keine abgeschlossene Compliance-Prüfung  
**Scope:** CommunicationTrainer Android + optionaler minimaler Backend-Broker + ausgewählter KI-Anbieter  
**Ergänzend:** [Technische Architektur](K5_TECHNISCHE_ARCHITEKTUR.md) · [Betrieb & Kosten](K5_BETRIEB_KOSTEN.md) · [Testplan](K5_TESTPLAN.md)

## 0. Produkt- und Datenschutzentscheidung

Kommunikationstraining kann private Interessen, Beziehungen, Sorgen und sensible Informationen berühren. Auch wenn **die Figuren fiktiv sind**, sind die vom Nutzer geschriebenen Inhalte reale personenbezogene Daten, sobald sie einer Person zugeordnet werden können.

**Empfehlung:** lokale Historie und Entwürfe als Standard, ein kurzfristig verarbeitender Broker für KI-Inferenz, keine langfristige Cloud-Chatarchivierung im ersten MVP, keine standardmäßige plattformübergreifende Persona-Erinnerung und kein automatisches persönliches Kompetenzprofil. **Konkret vor Release entscheiden**, ob ein Chatverlauf überhaupt auf dem Server dauerhaft nötig ist. Ein optionales Feature darf nicht über UI-Toggles versprochen werden, solange Backend/Dienstleister es nicht durchsetzen.

**Vorrangregel:** Berechtigungen, Zweckspezifikation, Datensparsamkeit, Transparenz, Löschbarkeit und Schutz vor manipulativer KI-Kommunikation gehen vor Engagement und KI-Komfort.

## 1. Verarbeitungsverzeichnis als Planungsgrundlage

| Datenklasse | Beispiele | Zweck | Vorgeschlagener Speicherort | Default und Lebensdauer |
|---|---|---|---|---|
| Charakterkanon | Mara, Jonas, Elena etc. | Figurendarstellung | signierte/versionsgebundene App-Fixtures | Produktinhalt, ohne Personenbezug |
| Eigene Figur | Name, Interessen, Stilachsen | individueller Chatpartner | lokal | bis Nutzer löscht |
| Nachrichten und Entwürfe | private Texte, Gesprächsverlauf | Chat fortsetzen | lokal; temporär an Broker/Provider je Anfrage | Nutzerkontrolle, kein Dauer-Cloud-Log |
| Session-State | K3-Revisionen und Parameter | konsistentes Verhalten | lokal und pro Anfrage in Broker | an Chat/Branch gebunden |
| Replay | Originalreferenz, alternativer Zweig | Lernvariation | lokal | bis Nutzer löscht |
| Erinnerung | minimierte Ereignisse und Quellen | freiwillige Kontinuität | lokal bzw. nur falls nötig serverseitig | **aus** bis bewusst gewählt |
| Coach-Ereignisse | K1-Anker, Textbelege, Einordnung | freiwillige Rückschau | lokal; optional aktivierte Aggregation | kein Langzeitprofil ohne Wahl |
| Account/Anmeldedaten | pseudonyme Kennung, Token | Zugang und Budget | Backend + sichere Clientablage | abhängig von gewähltem Zugang |
| Nutzungsmetadaten | Request-ID, Tokenbudget, Fehlertyp | Kosten und Missbrauchsschutz | Backend, möglichst ohne Klartext | **kurze, begründete TTL** |
| Report | gemeldete KI-Antwort + Beleg, bei Freigabe | Moderation, Sicherheit | besonders geschützter Berichtskanal | zweckgebunden und befristet |
| Crash/Diagnose | technische Fehlermarker | Betriebssicherheit | lokal oder minimal serverseitig | ohne Chatrohtext; opt-in wo nötig |
| Forschungs-/Pilotmaterial | synthetische/zugestimmte Beispiele | Qualitätsevaluation | getrennte Umgebung | separat einwilligen/prüfen |

**Vor Beta verpflichtend:** Betreiber und Verantwortlichkeiten, Rechtsgrundlagen je Zweck, Empfänger, Drittländer, konkrete Löschfristen, technische Kontrollen, AV-Verträge/Subprozessoren und Datenschutztexte mit Rechtsprüfung klären.

## 2. Rechts- und Plattformanforderungen (Stand 07.11.2026)

Die folgenden Quellen sind **Recherchegrundlagen**, keine pauschale Aussage, dass die geplante App bereits compliant wäre:

- **DSGVO:** Datensparsamkeit, Zweckbindung, Speicherbegrenzung und geeignete Transparenz-/Löschprozesse; Einwilligung nur dort behaupten, wo eine tatsächlich freiwillige und erforderliche Einwilligung vorgesehen ist. Es können je nach Zweck andere Rechtsgrundlagen nötig sein. Quellen: https://commission.europa.eu/law/law-topic/data-protection/information-business-and-organisations/principles-gdpr_en und https://eur-lex.europa.eu/eli/reg/2016/679/oj .
- **EU-KI-Verordnung (AI Act):** Transparenzpflichten für Interaktionen mit KI nach Artikel 50 gelten nach Auskunft der EU-Kommission seit **2. August 2026**. Für unser Produkt: klar und rechtzeitig sagen, dass es fiktionale KI-Charaktere sind, ohne sich auf eine bloße versteckte AGB-Notiz zu verlassen. Die konkreten Betreiber-/Anbieterverpflichtungen müssen juristisch eingeordnet werden. Quelle: https://digital-strategy.ec.europa.eu/de/faqs/transparency-obligations-under-article-50-ai-act .
- **Google Play – KI-generierte Inhalte:** Für Apps mit generativen KI-Chats fordert Google Play Schutz vor unzulässigen Inhalten und eine **In-App-Meldemöglichkeit** für anstößige KI-Ausgaben, ohne die App zu verlassen. Diese Funktion fehlte als expliziter Pflichtscreen in der ursprünglichen K4-Planung und ist **vor Play-Veröffentlichung nachzutragen**. Quelle: https://support.google.com/googleplay/android-developer/answer/13985936?hl=de .
- **Google Play – Daten:** vollständige, korrekte Data-Safety-Angaben, Datenschutzerklärung, Offenlegung tatsächlich verarbeiteter Kategorien und zugängliche Accountlöschung, falls Konten angeboten werden. Auch SDKs/Drittanbieter müssen einbezogen werden. Quelle: https://support.google.com/googleplay/android-developer/answer/10144311 .
- **Android Backup:** Sensible Inhalte nicht unreflektiert in Systembackups aufnehmen; Sicherheits- und Backup-Policy müssen zusammenpassen. Quelle: https://developer.android.com/privacy-and-security/risks/backup-best-practices .

**Offener Rechtscheck:** Sind Art.-9-Daten im vorgesehenen Chatverlauf wahrscheinlich? Welche Rechtsgrundlage und welchen Schutz benötigen wir dafür? Braucht das geplante Profiling oder das spezifische Risikobild eine Datenschutz-Folgenabschätzung? Diese Antworten dürfen nicht durch eine pauschale „wir holen Zustimmung ein“-Formel ersetzt werden.

### 2.1 Datenschutz je KI-Betriebsart (K5-Nachtrag)

- **Lokales Modell:** Chattext bleibt während der tatsächlichen lokalen Inferenz auf dem Gerät. Andere Netzfunktionen, Modell-Downloads, Backups, Reports und Absturzdiagnostik müssen weiterhin gesondert geprüft und erklärt werden.
- **ChatGPT-Plan:** Ein offizieller berechtigter OAuth-/Responses-Flow verarbeitet zur Antwortgenerierung ausdrücklich freigegebene Inhalte bei OpenAI; Login/Planfreigabe sind **keine** Erlaubnis, Chats, Erinnerungen oder Lernprofile in unsere App auszulesen. OAuth-Tokens gehören geschützt auf das Gerät und **nicht** in Telemetrie/URLs.
- **Cloud-Broker:** Chattext wird an den eigenen Broker und ggf. externen Modellanbieter oder selbst gehostete Infrastruktur übertragen. Region, Vertrag, Aufbewahrung, Löschung und Drittlandbezug je Konfiguration offenlegen; „lokal“ darf nicht als Etikett für Server-Inferenz benutzt werden.

**Kritische zusätzliche Risiken:** unerlaubter stiller Fall-Back zu Cloud, OAuth-Fehlkonfiguration/Tokenabfluss, ChatGPT-Planlimits mit ungewolltem kostenpflichtigen API-Fallback, Modell-/Quantisierungsartefakte ohne Lizenz/Hash sowie Speicher-/Thermal-Überlast auf Android. Die in [K5_KI_BETRIEBSARTEN.md](K5_KI_BETRIEBSARTEN.md) und [K5_MODELLVERGLEICH_TESTPLAN.md](K5_MODELLVERGLEICH_TESTPLAN.md) definierten Schutzprüfungen gelten zusätzlich zu T01–T18.

**Vor jeder Modusumschaltung:** Datenziel, Nutzen, Limit/Kosten und aktive Lernprofil-/Memory-Einstellungen nachvollziehbar zeigen. Kein automatischer Transfer vollständiger vergangener Chatverläufe aus dem lokalen Modus zur Cloud.

## 3. Bedrohungsmodell (STRIDE-orientiert)

| ID | Gefahr | Angriff / Fehler | Maßnahmen | Prüfklasse |
|---|---|---|---|---|
| T01 | Geheimnis in APK | Betreiber-API-Key aus dekompilierter APK | Broker, Server-Secrets, Key-Rotation, Secret Scanning | P0 |
| T02 | IDOR / Cross-user data | fremde Chat-/Memory-ID in API | serverseitige Objektberechtigung bei jedem Zugriff; keine Client-Vertrauensannahme | P0 |
| T03 | Cross-persona leak | Erinnerung von Jonas erscheint bei Elena | `user_id + character_id + conversation_scope` gebunden | P0 |
| T04 | Prompt Injection | Chatinhalte fordern andere Systemrollen oder Token | Rollenbarriere, serverkontrollierte Regeln, validierter Output, Testkorpus | P0 |
| T05 | Abrechnungs-/Quota-Missbrauch | automatisierte Clients senden massenhaft Anfragen | Auth/Guest-Limits, Quotas, Budget, Abuse-Erkennung | P0 |
| T06 | Replay Attack | wiederholte identische Generierungsanfrage | serverseitige Idempotency Keys, persistenter Requeststatus | P0/P1 |
| T07 | Daten im Log | Prompt/Chattext in Traces und Crashreports | standardmäßig contentfreie Logs, Redaktionskontrollen | P0 |
| T08 | Memory aus gelöschtem Chat | abgeleitete Erinnerung bleibt | Referenz-/Löschkaskade und Tombstones | P0 |
| T09 | Backups / Restore | gelöschte/entschützte Daten tauchen auf | Backup-Regeln, Recovery-Design, Restore-/Löschtests | P0/P1 |
| T10 | Man-in-the-middle | fremde Transportendpunkte, falsche Zertifikate | TLS, strikte Netzsicherheit, keine Klartextausnahme | P0 |
| T11 | lokaler Geräteverlust | entsperrte App offenbart private Chats | App-Sandbox, Geräteschutz, optionale App-Sperre; ggf. DB-Verschlüsselung | P1 |
| T12 | unklare Einwilligung | Profilbildung trotz ausgeschaltetem Toggle | serverseitig erzwungene Berechtigungen, Consent-Versionen | P0 |
| T13 | Provider-Retention | Provider speichert/schult mit Text wider Erwarten | Vertrag, Konfiguration, Datenflussprüfung, Anbieterwahl | P0 |
| T14 | manipulative KI | Figur erzeugt Schuld/Bindungsdruck | K3-Policy, Moderation, Berichte, menschliche Prüfung | P0 |
| T15 | unzulässige Coach-Behauptung | KI diagnostiziert aus Text | K2-Grounding, Abstention, Safety Gate | P0 |
| T16 | persönliche Angriffe | Explizite Drohung oder ernste Krise im Chat | Safety-Response statt normalem Training, keine medizinische Diagnostik | P0 |
| T17 | Bericht als Datenleck | unsichtbar gesamte Chatgeschichte mitsenden | Vorschau des konkreten gemeldeten Inhalts, minimale zusätzliche Belege, Berechtigung | P0 |
| T18 | störanfällige Löschjobs | „gelöscht“ obwohl Providerjob scheiterte | Löschstatus und Wiederholung, verständliche Rückmeldung | P0 |

**P0** bedeutet: Produktfunktion bis zur Korrektur sperren oder nicht ausliefern. Kein positiver Durchschnittswert kann den Schutz einer kritischen Grenze ersetzen.

## 4. Datenschutz-Defaults und Nutzerkontrolle

### 4.1 Vier getrennte Speicherzwecke
1. **Chatverlauf:** damit der Nutzer den Dialog auf seinem Gerät fortsetzen kann.
2. **Charaktergedächtnis:** nutzt nur explizit berechtigte, quellenbelegte Informationen auch in *neuen* Chats; vorzugsweise zunächst aus.
3. **Lernbeobachtungen:** optionales K2-Langzeitprofil, unabhängig von Charaktererinnerung.
4. **Nutzungs-/Sicherheitsmetadaten:** technisch notwendige Kosten-/Missbrauchskontrolle, minimiert und klar dokumentiert.

Eine Nutzerwahl in Kategorie 2 aktiviert niemals heimlich Kategorie 3. Ein „Freies Gespräch“ wird nicht zu einem stillen Persönlichkeitstest.

### 4.2 Consent-State-Vertrag
Je Zweck: `purpose_id`, `choice` (enabled/disabled), `policy_version`, `decided_at`, `source`, `withdrawn_at` soweit zutreffend. Berechtigungsprüfung **sowohl im Client als auch im Backend**, falls backendseitig gespeichert oder ausgewertet wird.

Deaktivieren bedeutet in UI **nicht automatisch dasselbe wie Löschen**. Drei klare Aktionen:
- „Nicht mehr speichern/verwenden“,
- „Vorhandene Daten ansehen“,
- „Vorhandene Daten löschen“.

Im UI muss ausdrücklich stehen, was diese jeweilige Aktion umfasst. Ohne gesicherte Backendgarantie darf keine absolut formulierte Löschbestätigung erscheinen.

## 5. Datenlösch- und Exportpfad

### 5.1 Löschung als technischer Prozess
1. Nutzer fordert Löschung einer `conversation_id`, `memory_id`, eines Lernprofils oder ganzen Kontos.
2. App kennzeichnet betroffene Objekte zunächst lokal als `deletion_pending`, damit sie nicht erneut an den Server übertragen werden.
3. Autorisierter Serverzugriff löst Entfernung aus aktiven Datenbanken, Caches, Snapshots, Suchindizes, Zusammenfassungen, Lernableitungen und moderierten Logs **im Umfang der dafür geltenden Rechts-/Aufbewahrungspflichten** aus.
4. Bei Provider-/Subprozessorhaltung: dokumentierten Lösch-/Ablaufprozess starten, inklusive Einschränkungen aus Vertrag und Recht.
5. Backup-Speicher folgt festgelegtem Restore-/TTL-/Erasure-Konzept; ggf. Tombstones unterbinden die Wiederherstellung.
6. Status auf Basis tatsächlich bestätigter Schritte anzeigen: `requested`, `local_complete`, `backend_pending`, `provider_pending`, `complete`, `partial_failure`.
7. Fehler müssen nachverfolgt und korrigiert werden; die App darf **nicht** bei lokalem Erfolg behaupten, dass Drittanbieter/backups sofort endgültig gelöscht sind.
8. Löschauftrag selbst enthält keine unnötige Kopie des Chattextes.

**Architekturgrenze:** Wenn der Server keinen dauerhaften Chattext hält, vereinfacht dies die Löschung, eliminiert aber nicht zwangsläufig alle Provider-/Safety-/Abrechnungsmetadaten. Vor Datenverarbeitung Verträge prüfen.

### 5.2 Datenexport
Vorgeschlagener MVP+-Export: von Nutzer ausgelöst, maschinenlesbar (z. B. JSON) mit lokalen Chats, eigenen Charakteren, eigenen Notizen und optionalen Lernbeobachtungen; klare Kennzeichnung hypothetischer Replay-Zweige. Sicherer Android-Share-/Storage-Flow statt öffentlichem Download-URL mit Token. Export-Verfügbarkeit muss vor Release konkret implementiert, getestet und rechtlich geprüft sein.

### 5.3 Backup/Device-Migration
In K5 verbindlich zu entscheiden:
- Welche sensiblen Daten gehören in Android Auto Backup / Cloud Backup?
- Wie werden eventuell verschlüsselte lokale Daten nach Restore lesbar oder bewusst nicht restauriert?
- Wie verhindern wir, dass eine schon erfolgte Löschung bei einem alten Gerätebackup wieder auftaucht?
- Welche Daten überleben App-Deinstallation und wie kommunizieren wir das?
- Gibt es überhaupt Account-Sync im MVP? Falls nein: kein unehrliches Mehrgeräte-Versprechen.

## 6. Kryptographie und Zugriff

- HTTPS/TLS und keine unverschlüsselten Debug-Endpunkte für Produktdaten.
- Server-Secrets über Secret Manager; niemals Client-SDK/BuildConfig/Git.
- Android Keystore für lokal zu schützende Schlüssel und Tokens wo sinnvoll; keine eigene selbstgebastelte Kryptographie.
- Lokale `Room`-Daten sind **nicht per se automatisch auf Anwendungsebene Ende-zu-Ende-verschlüsselt**. Geräteschutz/Filesystem-Verschlüsselung, Backup und ggf. zusätzliche Datenbankverschlüsselung müssen getrennt geprüft werden.
- Backend: Identitäts- und Besitzkontrolle je Ressource, Least Privilege, TLS, sichere Secrets, beschränkte Adminrechte, getrennte Umgebungen.
- Provider-Adapter darf nur zulässige Daten übertragen. Kein Rekonstruieren fehlender Chats aus Telemetrie.
- Keine Werbe-SDKs, Kontaktzugriffe oder versteckten Tracking-SDKs im frühen MVP.

## 7. Missbrauchs- und Sicherheitsreaktionen

**MVP-Sicherheitsverhalten:**
- In-App-Menü „Problematische Antwort melden“ bei jeder KI-Nachricht; auch ein allgemeiner Feedback-/Hilfebereich.
- Bei Meldung: verständliche Auswahl, Vorschau des **konkret** zu übermittelnden Ausschnitts, optional kurzer Kommentar, Schutz gegen Missbrauch, tatsächlicher Entwickler-Meldekanal und Bearbeitungsprozess.
- Moderationspolicy zu verbotenen Inhalten, unzulässiger Minderjährigenansprache, Gewalt, Selbstverletzung, expliziter Übergriffigkeit sowie erkennbarer Krise; keine Rollenspiel-„Herausforderungen“ bei ernstem Risiko.
- Dialog bleibt klar als KI gekennzeichnet. Figuren dürfen keine real existierende Verfügbarkeit oder exklusive emotionale Bindung vortäuschen.
- Bei Bedarf Schutzantwort, Verweigerung gefährlicher Anleitung, realweltliche Hilfepfade – ohne therapeutische Kompetenz vorzutäuschen.
- Grenzen (explizites Nein) schlagen Engagementziele; ungültige Antworten werden verworfen statt durch Lob ersetzt.
- Menschliche Bearbeitung von Reports getrennt von automatischer Datensammlung; Zugriff auditierbar.

## 8. Alter und Zielgruppe

K1/K3 definieren ein Erwachsenenprodukt. **Konzeptvorschlag:** 18+ als Zielgruppe bis eine verantwortbare Jugendschutz-/Alterskonzeption entwickelt ist. Das ist **keine** technische Altersverifikation. Play-Altersfreigabe, Jugendschutz, rechtliche Pflichten und Zugriffsmaßnahmen müssen anhand geplanter Veröffentlichung festgelegt werden.

Nicht unnötig Ausweis-/Geburtsdaten erheben. Falls eine Altersverifikation tatsächlich rechtlich oder produktsicherheitsseitig erforderlich ist, muss ein datensparsames, dokumentiertes Verfahren gewählt werden.

## 9. Datenschutz-Reife-Gates

**Vor einer geschlossenen Beta:**
- dokumentierter Datenfluss Android → Backend → Provider → lokale Speicherung;
- geprüfte Rechtsgrundlagen, DPA/AVV und Unterauftragsverarbeitung;
- Datenschutzrisiko- und gegebenenfalls DSFA/DPIA-Entscheidung mit Begründung;
- ehrliche In-App-Offenlegung der KI-Natur und tatsächlichen Datenverarbeitung;
- klare Report-Funktion und definierte Moderationszuständigkeit;
- getestete Scope-/Auth- und Memory-Isolation;
- implementierte Retention/Löschung inkl. Scheiterpfad;
- Rechtekonzept für Serverlogs, minimal notwendige Logs und vollständige Redaktionsprüfung;
- überprüfte Backup-/App-Daten-Regeln;
- Play Data Safety/Datenschutzerklärung/Accountlöschung bei tatsächlichem Release abgestimmt.

**Vor öffentlicher Veröffentlichung:** reale Rechtsprüfung, Policy-/Storeprüfung, unabhängiger Sicherheitstest nach Risiko, dokumentierte Rest-Risiken und Freigabe durch Betreiber. Kein Dokument im K5-PR ist ein Ersatz dafür.

## 10. Offene Fragen und K6-Empfehlung

1. Konkreter Betreiber, Hostingland, Datenschutzkontakt und zuständige Rollen.
2. Exakter KI-Provider einschließlich Vertrag, Subprozessoren, Speicher- und Trainingsbedingungen.
3. Benutzerkonto vs. begrenzte Gastidentität – Kosten-/Abuse-Abwägung.
4. Ob wir für den MVP **charakterübergreifende Langzeit-Memory ganz verschieben** sollten (aus Privatsphärensicht empfehlenswert).
5. Zulässige Cloud-/Backup- und Synchronisationsstrategie.
6. Tatsächliche Löschfristen, Logs und Backupretention, nicht frei festgelegt.
7. Zeitpunkt und Tiefe einer Datenschutz-Folgenabschätzung.
8. Content-Reporting-Moderationspflicht für die geplante Vertriebsplattform.
9. Ob eine optionale App-Sperre für private Chats zum ersten Release nötig ist.

**Verbindlicher Reviewhinweis:** Vor K6 nicht aus abstrakten Datenschutzabsichten „DSGVO-konform“ oder „verschlüsselt Ende-zu-Ende“ als Produkteigenschaft ableiten.
