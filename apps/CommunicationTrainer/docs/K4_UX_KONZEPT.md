# K4 – UX-Konzept und Nutzererlebnis

**Version:** 1.0 · **Datum:** 07.11.2026 · **Status:** Konzept zur Prüfung, noch keine UI-Implementierung  
**Produkt:** CommunicationTrainer, Android-first, Kommunikation im Alltag und soziale Kontakte  
**Abhängigkeiten:** [K1 Lernsystem](K1_LERN_KOMPETENZSYSTEM.md) · [K2 Coach](K2_KOMMUNIKATIONSCOACH.md) · [K3 Charaktere](K3_CHARAKTERE.md) · [K3 Regeln](K3_SIMULATIONSREGELN.md)  
**Weitere K4-Artefakte:** [Nutzerabläufe](K4_NUTZERABLAEUFE.md) · [UX-Testplan](K4_UX_TESTPLAN.md) · [Screen- und Zustandskatalog](../specs/k4_screens_v1.json)

## 0. Zusammenfassung und Produktentscheidung

K4 übersetzt das K1–K3-Konzept in eine verständliche und einheitliche Android-Bedienung.

**Primäre UX-These:** Nutzer müssen nicht wissen, wie Persona-, State- oder Coach-Engine funktionieren, um nach wenigen verständlichen Entscheidungen einen sinnvollen Chat zu beginnen. Freies Gespräch bleibt frei; Übungen und Analyse sind nur dann präsent, wenn sie gewünscht sind.

Die bisher geplanten vier Bereiche werden beibehalten:
1. **Chats** – offene und archivierte Gespräche, Einstieg in neue freie Chats.
2. **Üben** – Alltagssituationen, Kompetenzziele, kurze Lerninhalte, freiwillige Wiederholung.
3. **Fortschritt** – Spielaktivität, freiwillige Reflexion, vorsichtig formulierte Lernbeobachtungen.
4. **Profil** – Ziele, Darstellung, Charakterverwaltung, Datenschutz, Hilfe und Einstellungen.

*Entdecken & Üben* bleibt die Seitenüberschrift im zweiten Bereich, während der kompakte Tab **Üben** heißt. Die UI zeigt niemals „K1“, „K2“, „M2“ oder interne Schema-IDs an.

## 1. Erfolgsdefinition und Anti-Ziele

### 1.1 Erfolgreiche Nutzerreise
Ein Erwachsener ohne Erklärung kann:
- direkt erkennen, dass Charaktere KI-Simulationen und keine echten Personen sind;
- einen Charakter wählen und eine Unterhaltung beginnen, ohne komplexe Einrichtung;
- zwischen freiem Chat, Übung und optionalem Coach klar unterscheiden;
- die Stimmung einer Persona **vor dem Chat** bei Bedarf beeinflussen;
- einen Übungsmodus wählen und nach dem Chat kurze, nicht wertende Rückmeldung erhalten;
- eine Nachricht erneut versuchen, ohne das ursprüngliche Gespräch zu verlieren;
- Chats/Erinnerungen/Lernprofil getrennt verwalten und löschen;
- ein früheres Gespräch sicher fortsetzen;
- jederzeit abbrechen und zur vorherigen Ansicht zurückkehren, ohne Entwürfe zu verlieren.

### 1.2 Bewusst nicht anstreben
- Keine Chatwelt mit aufdringlichen „Online“- oder „denkt gerade an dich“-Signalen.
- Kein langes Onboarding, keine verpflichtende Persönlichkeitserhebung.
- Keine Fortschrittsanzeigen, die menschliche Sozialkompetenz in Prozent messen sollen.
- Keine endlosen Cards, CTA-Dopplungen, Paywalls zwischen Auswahl und erstem Chat oder simulierte Beziehungsschulden.
- Kein Coach, der permanent in den freien Chat hineinredet.
- Kein wiederholter Einwilligungsdialog bei jedem Öffnen: klare, nachvollziehbare Einstellungen.
- Keine überladenen Persona-Regler als Voraussetzung für einen ersten Chat.

## 2. Informationsarchitektur und Navigation

### 2.1 Tab-Struktur
**Chats:** Standardstart bei Folgeaufrufen (sofern letzter Zustand nicht sinnvoll wiederherstellbar). Zwei primäre Aktionen: „Gespräch fortsetzen“ bei vorhandenem Chat und „Neues Gespräch“. Charakterkarten separat erreichbar.

**Üben:** „Heute ausprobieren“ als Vorschlag, Kompetenzbereiche, 6 Szenarien aus K3 in der ersten Bibliothek, Mini-Lektionen, zuletzt bearbeitete Übung; kein verpflichtender Tagesplan.

**Fortschritt:** Aktivität/XP deutlich getrennt von beobachteten Lernbeispielen. Ohne aktiviertes Lernprofil nur Aktivitäten und freiwillige Selbstnotizen anzeigen; keine leeren Diagnosebalken.

**Profil:** Lernwünsche, eigene Charaktere, Darstellung/Bedienhilfen, Daten und Privatsphäre, Hilfe, Informationen zu KI, Benachrichtigungen (anfangs deaktiviert).

**Android-Verhalten:** Bottom Navigation auf kompakten Handys mit vier Zielen; in tieferen Fokusansichten (Chat, Übung, Editor) darf die untere Leiste für Bildschirm-/Tastaturplatz ausgeblendet werden, sofern Zurücknavigation klar bleibt. Auf großen Displays später Navigation Rail. Das ist konsistent mit Androids empfohlenen Mustern für drei bis fünf primäre Ziele.

### 2.2 Navigationsregeln
- Jeder Tab behält seinen eigenen Navigationsverlauf und ggf. Entwurf, soweit praktikabel.
- Android-Zurück führt zunächst innerhalb der aktuellen Teilaufgabe zurück; eine laufende Nachricht/Analyse wird nicht stillschweigend verworfen.
- Chatansichten sind per eindeutiger Chat-ID wiederherstellbar.
- Ein Übungsergebnis hat einen klaren Weg „Weiterüben“, „Zum Chat“ (falls erlaubt), „Schließen“.
- Keine überraschenden modalen Sackgassen; Dialoge nur bei irreversiblen Aktionen, expliziter Datenschutzentscheidung oder drohendem Datenverlust.
- Keine Verschleierung des Modus durch Wechsel ohne Nutzerentscheidung.

## 3. Der erste Start: maximal wenige Entscheidungen

### 3.1 Vorgeschlagener Minimalablauf
1. **Einstiegsseite:** „Alltagsgespräche ausprobieren und neue Möglichkeiten entdecken.“ Zwei gleich gut auffindbare Aktionen: „Gespräch starten“ und „Gezielt üben“. Kurz: „Du schreibst mit KI-Charakteren.“
2. **Ziel (optional):** drei vorformulierte Wünsche („Leichter ins Gespräch kommen“, „Gespräche lebendig halten“, „Einfach ausprobieren“) + „Später“. Keine Pflicht, Persönlichkeit zu bewerten.
3. **Charakterwahl:** zwei bis drei verständliche Kurzbeschreibungen prominent, übrige sichtbar; „Welche Person passt zu dir?“ mit Interessen, Gesprächsstil, keine Rankings.
4. **Chat starten:** bei freiem Chat ohne vorangegangenen Fragebogen; im Training passende erste Szene und klares Lernziel anzeigen.
5. **Erster Coach:** im freien Modus nur über „Auswerten“, bei Übung als freiwillige Abschlussoption; Erklärung „Feedback ist ein Lernangebot, keine Benotung.“

**Registrierung:** möglichst erst dann verlangen, wenn geräteübergreifende Synchronisierung oder ein technisch erforderlicher Dienst genutzt wird. Der bevorzugte Konzeptweg ist eine auf dem Gerät nutzbare, datensparsame Einstiegserfahrung. Ob ein echter Gastmodus mit externem KI-Dienst sicher und wirtschaftlich machbar ist, wird in K5 entschieden. Falls nicht, muss ein ehrlicher, minimaler alternativer Einstieg entworfen werden; nicht heimlich Tracking oder kostenlose unbegrenzte Nutzung versprechen.

### 3.2 Einwilligung und transparente Speicherung
Bereits im ersten Einstiegsdialog soll klar sein: KI simuliert Gesprächspartner; Nachrichten können zur Antworterzeugung an einen Dienst übermittelt werden, sofern das die spätere Architektur verlangt. Konkrete Datennutzung wird vor Übermittlung verständlich erklärt.

Separate, jederzeit zugängliche Entscheidungen für:
- Verlauf auf dem Gerät / im Account;
- **charakterübergreifend nicht geteilte** Erinnerung über Chats hinweg;
- personenbezogene Lernbeobachtungen;
- optionales Diagnose-Logging nur falls erforderlich und transparent.

Das UX-Konzept ersetzt keine rechtliche Prüfung; K5 bestimmt Datendienst, Zwecke, Rechtsgrundlagen und konkrete Optionen.

## 4. Chats: Übersicht, Verlauf und Sessionmodell

### 4.1 Chats-Liste
- Oben klarer Titel „Chats“ und primäre Aktion „Neues Gespräch“.
- Karten/Zeilen: Charaktername, deutliches KI-Kennzeichen, letzter selbstbestimmter Chat-Titel/kurzer Vorschau-Text, Zeitpunkt der letzten **tatsächlichen Chataktivität**, bei Bedarf Modus „Freies Gespräch“ oder Szenario.
- „Angepinnt“ und „Archiviert“ optional später; MVP mindestens öffnen, umbenennen, löschen. **Kein künstlich berechnetes Online-/Verfügbarkeits-Signal** als soziale Dringlichkeit.
- Neue/nicht vorhandene Chats: Leerezustand mit „Gespräch starten“ und direktem Link zu „Üben“.
- Viele Chats: Suche optional MVP+, nicht für erste Version zwingend.
- Löschen im Kontextmenü mit klarer Erklärung: Verlauf, zugeordnete Erinnerungen und daraus abgeleitete Profilbeobachtungen beachten; K5 muss die Löschkaskade implementieren.

### 4.2 Chatansicht
Kopfzeile zeigt:
- Charaktername und Kennzeichen **„KI-Charakter“** (nicht versteckt);
- kleines, verständliches Statuslabel **„Freies Gespräch“** oder **„Übung: [Ziel]“**;
- Öffnen einer Charakter-Kurzinfo („Was prägt diese Figur?“);
- Menü für „Auswerten“ (frei), „Erneut versuchen“ (bei geeignetem Verlauf), „Gespräch beenden“, „Daten verwalten“.

Nachrichtenbereich:
- klare Sendertrennung und lesbare Textblasen, Zeitangaben nur bei Bedarf;
- kein dynamisches Erzwingen einer Antwort oder manipulative Warteanzeigen;
- eigene Entwürfe bleiben erhalten, wenn man in eine Unteransicht wechselt;
- lange Antworten brechen um und müssen mit großen Schriftgrößen lesbar bleiben;
- keine automatische Interpretation von Ton oder Mimik, die schriftlich nicht gegeben sind.

Composer:
- Textfeld mit Zeilenumbruch; Senden klar beschriftet für Screenreader;
- während KI-Antwort wird exakt **eine** ausstehende Antwort/Sendestatus angezeigt;
- keine Doppel-Sendungen beim schnellen Tippen;
- Tastatur darf Nutzereingabe, Senden und relevante Nachrichten nicht verdecken;
- „Tipp“ nur im Trainingsmodus auffindbar; im freien Chat keine ungebetene Hilfemeldung.

### 4.3 Chatbeginn und -ende
- Benutzer darf mit eigener Nachricht beginnen; bei Szenarien kann die Figur die erste Nachricht senden.
- Eine Persona kann ein Gespräch natürlich beenden. UI erklärt bei Bedarf, dass in dieser Szene gerade keine Antwort zu erwarten ist; sie legt **keinen** „Gewinnen“-Button nahe.
- Der Nutzer kann danach „Neues Gespräch mit [Figur]“, „Andere Situation“ oder „Zur Chatliste“ wählen. Neue Session bleibt eindeutig vom bisherigen Gespräch getrennt.
- Freie Chats dürfen pausiert und später fortgesetzt werden; eine fiktive Off-Chat-Aktivität wird nicht als reale Person ausgegeben.

## 5. Üben – Alltagssituationen und Lernpfade

### 5.1 Entdecken & Üben
Startansicht:
- Option „In 5 Minuten etwas ausprobieren“ als unverbindliche, nicht minutengenaue Orientierung;
- sechs K1-Kompetenzbereiche in verständlicher Sprache;
- aktuell sechs konkrete K3-Szenarien (S01–S06) und später weitere;
- Filter nach Fähigkeit, Gesprächssituation und gewünschter Dynamik;
- kurzer Mini-Artikel, der ohne Übungszwang frei lesbar bleibt;
- freiwilliges persönliches Lernziel, das geändert werden kann.

Szenario-Karte: „Was passiert?“, „Worum geht es?“, „Mit wem?“, „Wie herausfordernd?“. Keine „Erfolgsgarantie“ und kein negatives Label für Personen.

### 5.2 Übungsstart
1. Szenarioübersicht in wenigen verständlichen Sätzen (Anlass, Beziehung, Ziel).
2. Charakter- und Dynamikinfo optional aufklappbar; keine überfrachtete Einstellungsseite.
3. Aktion „Übung starten“.
4. Der Chat zeigt das aktuelle Lernziel, beispielsweise „Heute: eigene Gedanken einbringen“.
5. Am Ende: „Möchtest du dir die Übung ansehen?“ → „Kurzfeedback“, „Noch einmal probieren“ oder „Fertig“.

**Zeitangabe** bezieht sich auf eine grobe Erwartung; keine Uhr, die sozialen Druck aufbaut.

### 5.3 Lektionen
Leseansicht: ein klarer Titel, eine Idee in Alltagssprache, ein positives Beispiel, ein Gegenbeispiel mit situativer Einschränkung, direkte Option zur passenden Übung. Keine langen Pflichtprüfungen, keine „Lesson-Lock“-Mechanik für freie Chats.

## 6. Coach-UX (K2), ohne den Chat zu kapern

### 6.1 Standard-Kurzfeedback
- Eine **konkrete, belegte** Stärke.
- Höchstens eine **freiwillige** Idee für den nächsten Versuch.
- Wenn angemessen: nur Stärke, „Hier lässt sich noch nichts beurteilen“, optional eine Reflexionsfrage oder Grenzhinweis.
- Sichtbarer Link „Im Gespräch ansehen“, der zur betreffenden Textstelle scrollt.
- „Warum dieser Hinweis?“ und „Nicht passend“ als sekundäre, gut erreichbare Aktionen.
- Kein globaler Score, kein riesiger Kompetenz-Gauge, keine Behauptung über verborgene Figurenemotion.

### 6.2 Feedbackintensität
- **Kurz:** oben genannte knappe Form.
- **Mehr erfahren:** Beobachtung → mögliche Deutung → Variante(n) → Kontext/Unsicherheit; keine komplette Chat-Seite duplizieren.
- **Live-Tipp:** nur auf Nutzereingabe, zunächst Blickwinkel/Hinweis, dann auf Wunsch Formulierungsbeispiel.
- **Stumm:** im freien Chat Standard; in Übungen jederzeit „Feedback überspringen“.
- **Grenzhinweis:** klar, nicht beschämend. Bei Sicherheitsinhalten normale Lernbewertung aussetzen.

### 6.3 Widerspruch und Fehlerkorrektur
„Der Hinweis passt nicht“ öffnet eine kleine Auswahl:
- „Mein Ziel war ein anderes“
- „Der Kontext fehlt“
- „Ich sehe das anders“
- „Das ist inhaltlich falsch“

Danach Auswahl „Rückmeldung aktualisieren“ oder „Nur ausblenden“. Keine Rechtfertigungspflicht, kein Bestrafungseffekt auf XP. Eine strittige Bewertung wird bis zur Entscheidung nicht als langfristiger Lernbeleg verwendet; K2/K5 regeln Berechnung und Löschung.

## 7. Wiederholung und alternative Antwort (Replay)

**Zentrale UX-Anforderung:** Nutzer müssen erkennen, dass sie eine frühere Stelle **verzweigen**, nicht das Original überschreiben.

Ablauf:
1. Nachricht lange drücken oder Menü „An dieser Stelle erneut versuchen“ wählen. Es gibt eine gleichwertige zugängliche Menüaktion statt ausschließlich Long-Press.
2. Vorschau der gewählten Nachricht mit Kontext: „Du probierst ab dieser Stelle eine andere Antwort aus.“
3. Dialogentwurf anpassen. Original darf eingesehen werden; Variante erhält eigene ID.
4. Neue mögliche Figurenreaktion – sichtbar als **Simulation**, nicht als Beweis der menschlichen Wirkung.
5. Optional „Varianten vergleichen“; ursprünglicher Verlauf bleibt unverändert.
6. „Im Original fortfahren“ oder „Variante als neue Übung öffnen“.

**MVP-Schnitt:** Eine alternative Nutzerantwort und ein neuer zweigbezogener Dialog reichen. Automatisch generierte Multi-Branch-Vergleichsgalerien sind spätere Erweiterung, wenn Latenz und Kosten tragbar bleiben.

## 8. Charakterwahl und Charaktereditor

### 8.1 Charakterkarten
MVP-Auswahl mit den fünf K3-Figuren Mara, Jonas, Elena, Deniz, Robin. Karte zeigt:
- Name + fiktionaler Charakterstatus;
- ein prägnanter Kommunikationsschwerpunkt, kein Diagnose-Etikett;
- 2–3 Themeninteressen;
- zwei kurze Beispielzeilen („So schreibt diese Figur möglicherweise“);
- ein klarer CTA „Mit [Name] sprechen“ und optional „Mehr über [Name]“.

**Keine** Schwierigkeitssterne direkt auf Personen, kein behaupteter Kompatibilitätswert, kein „heute online“-Status.

### 8.2 Einfacher Editor
Vorgeschlagener Startumfang:
- Name / kurze Idee / einige Interessen;
- **4–6 sichtbare** Regler in Alltagssprache, bevorzugt Gesprächsinitiative, Antwortumfang, Direktheit, Humor und persönliche Offenheit (fünf);
- Stimmung als Voreinstellung oder „Überrasche mich“;
- optional bekannte Beziehung/Anlass;
- verständliche Vorschau und Aktion „Charakter erstellen“.

**Erweiterte Persönlichkeit** mit Big Five, weiteren Achsen, Langzeitwerten und komplexen Reglern nicht auf der ersten Oberfläche. Die Regler geben Tendenzen wieder; ein Wert „viel Humor“ bedeutet nicht jede Nachricht als Witz.

### 8.3 Bearbeitung einer laufenden Persona
Änderungen an einem Charakter erstellen eine neue Persona-Version. Die UI informiert: „Bereits begonnene Chats bleiben beim bisherigen Charakterstand. Für neue Einstellungen starte ein neues Gespräch.“ Keine stillschweigende Mutation. Für eigene Charaktere müssen Speichern, Duplizieren, Zurücksetzen und Löschen nachvollziehbar sein; irreversible Schritte bestätigen.

### 8.4 Stimmung und Dynamik nicht vermischen
- **Stimmung:** wie es der Figur gerade geht (optional vor Chatbeginn).
- **Schwierigkeit:** wie herausfordernd die konkrete Situation ist (in Übung).
- **Persönlichkeit:** welche Tendenzen dauerhaft bleiben.

Die Auswahl von „zurückhaltend“ im Übungsprofil etikettiert **keinen** introvertierten Charakter als schwierig. Beispiele im UI zeigen diesen Unterschied.

## 9. Fortschritt und Motivation

### 9.1 Drei getrennte Bereiche
1. **Deine Übungspraxis** – abgeschlossene Übungen, Varianten und spielerisches XP.
2. **Was du beobachtet hast** – eigene Reflexionen, persönliche Lernwünsche.
3. **Hinweise aus mehreren Übungen** – nur mit gesondert aktiviertem Lernprofil und ausreichenden K2-Belegen; datiert, belegbar, korrigierbar.

Ohne Lernprofil-Opt-in nur neutraler Zustand: „Deine Übungen werden hier angezeigt. Persönliche Beobachtungen sind ausgeschaltet.“ CTA „Mehr über Lernbeobachtungen“, nicht „Schalte deinen wahren Score frei“.

### 9.2 Positive Motivation ohne Druck
- XP für Lernaktivität, nicht für Zuneigung, Dauer, Überreden oder Offenbarung;
- Level als spielerisches Meilensteinsystem, kein sozialer Fähigkeitswert;
- Abzeichen für Vielfalt, Wiederholung oder freiwilligen Transfer;
- keine Warnfarben wegen verpasster Tage; keine verpflichtende Streak;
- kein sozialer Vergleich oder globales Ranking;
- „Du musst heute noch üben“ wird vermieden.

## 10. Datenschutzbedienung und Vertrauen

Im Profil klar getrennte Karten:
- **Chats und Verlauf** – anzeigen, exportieren, archivieren/löschen;
- **Charaktererinnerungen** – aktivieren/deaktivieren, je Charakter einsehen, korrigieren, löschen;
- **Persönliche Lernbeobachtungen** – Opt-in, Belege ansehen, ausblenden/korrigieren, alles löschen;
- **Konten und Synchronisierung** – falls im Betrieb tatsächlich verfügbar;
- **Wie KI funktioniert** – keine realen Personen, hypothetische Antworten und Grenzen.

**Keine Scheinkontrolle:** Ein Toggle darf nur angezeigt werden, wenn K5 technisch garantiert, was die UI verspricht. „Nur auf deinem Gerät“ oder „sofort überall gelöscht“ darf nicht ohne belegbare Architektur behauptet werden.

Löschung zeigt **vorher** verständlich die Auswirkungen und ggf. nicht rückgängig zu machende Datenarten. Bei verbundenen Daten wird die abhängige Entfernung erklärt, ohne Datenschutzdialoge als Hürde zu missbrauchen. Datensparsamkeit vor kosmetischer Einwilligungssteuerung.

## 11. Fehler-, Lade- und Sonderzustände

| Zustand | Nutzererlebnis | Verbindliche Funktion |
|---|---|---|
| Kein Chat vorhanden | freundlicher Einstieg „Starte dein erstes Gespräch“ | echte Aktion zu Charakterwahl |
| Noch keine Übungen | Übungsauswahl und Erklärung | kein leerer Fortschrittsdruck |
| Noch keine Lernbeobachtungen | neutraler Text, keine leeren Prozentanzeigen | optionaler Hilfelink |
| Kein Netzwerk | „Du bist offline. Deine gespeicherten Chats bleiben sichtbar.“ | neuer Versand wartet auf ehrlichen Retry, wenn unterstützt |
| KI antwortet langsam | deutlicher Sendestatus + „Antwort wird erstellt“ | kein Fake-Tippen einer realen Person |
| Serverfehler/Timeout | „Die Antwort konnte nicht erstellt werden. Erneut versuchen.“ | Nutzerentwurf erhalten, keine doppelten Nachrichten |
| Analyse nicht verfügbar | „Feedback gerade nicht verfügbar.“ | Chat bleibt intakt, keine erfundene Auswertung |
| Übung abgebrochen | „Du kannst später fortfahren.“ | Fortschritt nicht automatisch verloren |
| Charakter beendet Gespräch | plausibles Abschlusslabel | Wiederaufnahme oder neues Gespräch nur ausdrücklich |
| Memory deaktiviert | erklären, dass neue Chats nicht auf frühere Erinnerungen zugreifen | keine heimliche Cross-Session-Memory |
| Löschen in Arbeit | transparenter Status, kein fiktives „bereits überall gelöscht“ | Folgefehler sichtbar machen |
| Personenbezogener Fehler im Coach | „Nicht passend“ / Neubewertung | strittige Beobachtung nicht im Lernprofil belassen |
| Gerät rotiert / App gekillt | Eingabe und Tabzustand wiederherstellbar, soweit technisch möglich | K5 prüft Persistenz/Android-Lifecycle |

**Offline-Zusage:** Nur lokal vorhandene Texte und Screens müssen ohne Netz angezeigt werden können, wenn die App die Daten lokal speichert. Offline-Generierung wird nicht zugesagt; K5 bewertet die Architektur.

## 12. Visuelle Sprache und Schreibstil

Designprinzipien:
- ruhige, freundliche, erwachsene Anmutung, nicht infantile „Schul-App“ oder romantische Companion-App;
- lesbare Typografie, sinnvolle Überschriften, genügend Leerraum und eindeutige Hierarchie;
- wenige primäre Aktionen pro Bildschirm; Sekundäraktionen über klar beschriftetes Menü;
- Charakterkarten dürfen Persönlichkeit durch Farbe/Illustration andeuten, dürfen aber Klischees und falsche Menschlichkeit nicht verstärken;
- Semantische Farben: nicht nur rot/grün für Erfolg, keine falschen „Fehler“-Fahnen bei ungewöhnlichem Gesprächsstil;
- deutsche Begriffe durchgängig: „Freies Gespräch“, „Übung“, „Auswerten“, „Erneut versuchen“, „Was merkt sich dieser Charakter?“;
- keine künstliche Dringlichkeit, niemals „[Charakter] vermisst dich“;
- optional später reduzierte Bewegungen und Kontraste; Systemkonfiguration standardmäßig respektieren.

**Konzeptstatus:** Farbpalette, App-Name, Illustration und exakte Typografie sind noch nicht final. Visuelle Ästhetik darf die Bedienqualität nicht überdecken.

## 13. Barrierefreiheit, Android-Konventionen und responsives Verhalten

**Mindestanforderungen für die Implementierung:**
- Touchziele mindestens 48 × 48 dp, auch bei Icons und Drei-Punkt-Menüs.
- Dynamische Schriftgrößen und Android-System-Skalierung, keine abgeschnittenen Buttons bei großem Text.
- TalkBack: sinnvolle Semantik, sichtbare Senderkennzeichnung, Überschriften, Reihenfolge, „Senden“/„Wiederholen“ ohne ausschließlich symbolische Bedienung.
- Interaktive Funktionen dürfen nicht ausschließlich durch Wischen oder langes Drücken erreichbar sein.
- Hell-/Dunkelmodus und Kontrast, keine wichtige Information nur über Farbe oder Emoji.
- Tastatur, Android-Zurück, Gestennavigation, Bildschirmrotation und Zustandserhalt berücksichtigen.
- Ladeindikatoren und Fehlermeldungen ohne visuelle Daueranimation; Screenreader-Ankündigungen sparsam und nicht störend.
- Keine Pflichteingabe langer Texte; gleicher Lernwert auch bei kurzen, direkten Antworten.
- Bei großen Displays später Navigation Rail und Zwei-Spalten-Chatübersicht; im MVP zuerst mobile Einspaltigkeit sauber prüfen.

Android empfiehlt für primäre kompakte Navigation drei bis fünf Ziele und für interaktive Steuerelemente mindestens 48 dp; siehe:
- https://developer.android.com/design/ui/mobile/guides/layout-and-content/layout-and-nav-patterns
- https://developer.android.com/guide/topics/ui/accessibility/apps.html
- https://developer.android.com/develop/ui/compose/accessibility/api-defaults

W3C WCAG 2.2 dient als ergänzende Orientierung für Wahrnehmbarkeit, Bedienbarkeit und Robustheit, ersetzt aber nicht native Android-Tests: https://www.w3.org/TR/WCAG22/

## 14. Navigation und Zustände als implementierbarer Vertrag

[K4-Screenkatalog](../specs/k4_screens_v1.json) enthält:
- feste `screen_id` und Einstiegsrouten;
- `parent_tab`, Modus, primäre/sekundäre Handlungen;
- Leer-/Lade-/Fehler-/Erfolgszustände und Exit-Wege;
- Rücksprung-/Modusregeln und Accessibility-Pflichten.

Für Prototyping soll genau eine Umsetzung jedes Hauptflows existieren, ohne die Architektur durch doppelte Pfade (z. B. „Feedback“ an drei unklaren Orten) zu verwirren. Der Datenvertrag wird vor K5 technisch konkretisiert.

## 15. MVP-Schnitt und bewusste Verschiebungen

**MVP UX zwingend:**
1. transparente KI-Kennzeichnung und verständlicher Einstieg;
2. vier Tabs;
3. Charakterauswahl aus fünf K3-Personas;
4. freie Chats und Chatübersicht inkl. Wiederaufnahme/Löschen;
5. sechs K3-Übungsszenarien als guter Start;
6. klarer Modus und einfaches Lernziel;
7. Abschluss-Kurzfeedback freiwillig, „keine Kritik“ und „nicht beurteilbar“ möglich;
8. eine alternative Antwort ausprobieren ohne Originalverlust;
9. XP für Übung, einfache Fortschrittsansicht ohne Scheinscores;
10. eigener Charakter mit einfachem Editor, gespeicherte Versionen;
11. Datenkontrollen (mindestens echte Chatlöschung, zusätzliche Features nur wenn zuverlässig implementiert);
12. elementare Barrierefreiheit, Tastatur/Zurück/Netzwerk-/Fehlerfälle.

**Später:** umfangreiche Persona-Filter, komplexe Big-Five-Regler, spontane Push-Nachrichten, Voice, Gruppenchats, soziale Rankings, ausführliche Coach-Analyse mit vielen Branches, adaptive Echtzeit-Kompetenzmetriken, komplexe Erinnerungsbeziehungen. **Kein** persönlicher Fähigkeits-Gesamtwert als Produktziel.

## 16. Fachliche Abnahmekriterien und K4-QA

K4 ist konzeptionell reviewfähig, wenn:
- jeder zentrale Bereich einen klaren Nutzerzweck und primäre Aktion hat;
- Erststart, freier Chat, Übung, Coach, Replay, Charaktereditor, Fortschritt und Datenlöschung end-to-end beschrieben sind;
- bei kritischen Schritten Leer-, Lade-, Fehler- und Abbruchzustände festgelegt sind;
- der Nutzer Modus, simulierte Rolle und Datenbehandlung versteht;
- Grenzen und Gesprächsende nicht als UX-Misserfolg gelten;
- kein erzwungenes Feedback, keine Diagnose-/Zuneigungswerte und keine dunklen Muster vorgeschlagen sind;
- Android-Navigation, Bedienhilfen, dynamische Schriftgrößen und große Touchziele verbindlich sind;
- K1–K3-IDs in den fachlichen Abläufen korrekt referenziert sind;
- UX-Tests mit realistischen, nicht nur positiven Nutzerwegen vorgesehen sind.

Begleitend: [K4 Nutzerabläufe](K4_NUTZERABLAEUFE.md) und [K4 UX-Testplan](K4_UX_TESTPLAN.md).

## 17. Offene K4-Entscheidungen und Übergabe an K5/K6

- **K5:** Kann eine echte Gastnutzung mit KI-Dienst angeboten werden? Welche Daten lokal, welche am Server? Welche Lösch- und Export-Funktionen sind tatsächlich umsetzbar?
- **K5:** Wie werden Sendeschlangen, Streaming, Netzwerk-Retry, Cache und Kosten technisch abgesichert?
- **K5:** Wie werden Datenschutz und Sicherheit nutzerseitig wirklich durchgesetzt?
- **K6:** Soll „Chats“ oder „Üben“ als Defaultstart unter verschiedenen Nutzertypen gewählt werden? A/B-/Usability-Test statt Annahme.
- **K6:** Welche 4–6 Slider sind verständlich, ohne Charaktere zu schematisieren?
- **K6:** Welche Zahl an Lerninhalten und Szenarien ist zum Start gerade richtig?
- **K6:** Wie unterscheiden Nutzer die Figurensimulation von realer menschlicher Kommunikation, auch nach längerer Nutzung?
- **Später:** konkrete visuelle Identität und App-Name (separate Designarbeit nach Abschluss der Kern-UX).

**GitHub-Regel:** Diese K4-Unterlagen bleiben bis zur ausdrücklichen PR-Freigabe im Entwicklungsbranch. Keine anderen Apps oder bestehenden Buildkonfigurationen verändern.
