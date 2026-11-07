# K4 – UX-Qualitätsplan, Usability-Tests und Abnahme

**Version:** 1.0 · **Datum:** 07.11.2026 · **Status:** konzeptioneller Prüfplan; noch keine Nutzertests/Android-Tests ausgeführt  
**Grundlagen:** [K4 UX-Konzept](K4_UX_KONZEPT.md) · [K4 Nutzerabläufe](K4_NUTZERABLAEUFE.md) · [K4 Screenkatalog](../specs/k4_screens_v1.json)  
**Fachliche Systeme:** K1 Lernziele, K2 Coach, K3 Personas und Szenarien

## 1. Qualitätsversprechen, die wir tatsächlich prüfen müssen

K4 darf erst als *umsetzungsreif* gelten, wenn zentrale Abläufe auch bei Unterbrechungen, Missverständnissen, langsamen KI-Antworten, kleinen Geräten und unterstützenden Technologien nachvollziehbar funktionieren.

**Fünf Prüfziele:**
1. **Orientierung:** Nutzer erkennen, wo sie sind, was sie tun können und wie sie zurückkommen.
2. **Niedrige Einstiegshürde:** erste sinnvolle Interaktion ohne Pflichtfragebogen, komplexen Editor oder unerwarteten Account-Block.
3. **Lernnutzen:** Übung, Coach und Varianten sind verfügbar, aber nicht aufdringlich oder wertend.
4. **Vertrauen:** KI-Charaktere und Speicherregeln werden korrekt erklärt, ohne Scheinkontrolle.
5. **Robustheit & Zugänglichkeit:** große Schriften, TalkBack, System-Zurück, Tastatur, Offline-/Fehler- und Löschzustände sind berücksichtigt.

## 2. Nutzergruppen und Testsituation

Vorschlag für qualitative Pilot-Stichprobe, **nicht als statistisch repräsentativ behauptet**:
- Erstnutzer ohne Erfahrung mit KI-Apps, die schnell loslegen wollen;
- eher zurückhaltende Kommunikationsstile, die keine wertende Oberfläche wünschen;
- aktiv Lerninteressierte mit Wunsch nach Feedback und Wiederholung;
- Personen, die nur frei chatten und Analyse ausschalten möchten;
- mindestens einige Personen, die große Schrift, Screenreader oder andere Eingabe-/Bedienhilfen verwenden.

Eine frühe UX-Runde kann mit 8–12 Personen als pragmatischem Richtwert starten, später weitere Kontrasttests. Die Stichprobe und Rekrutierung werden vor Durchführung geplant; keine Wirksamkeits- oder Erfolgsprozente erfinden.

**Testmaterial:** interaktiver Prototyp oder reale App; die folgenden textlichen Wireframes ersetzen keinen Nutzertest.

## 3. Aufgabenliste für moderierte Usability-Tests

| ID | Aufgabe für Testperson | Erfolgsindikator | Kritischer Fehler |
|---|---|---|---|
| U01 | Starte ohne Erklärung einen freien Chat | findet Charakter, erkennt KI | Onboarding lässt nicht fortsetzen |
| U02 | Starte ein Gespräch ohne Lernziel | Überspringen möglich | Pflichtquiz/Diagnose |
| U03 | Finde Jonas aus den fünf Charakteren | Auswahl klar beschriftet | Persönlichkeit als Schwierigkeit dargestellt |
| U04 | Setze ein Gespräch nach App-Neustart fort | richtiger Verlauf, Entwurf erhalten | Chatverlust oder falsche Persona |
| U05 | Finde die Übung „Fotokurs“ | über Üben/Kompetenz erreichbar | versteckter/duplizierter Einstieg |
| U06 | Erkenne den aktuellen Modus | „Freies Gespräch“/„Übung“ sichtbar | verdeckter Moduswechsel |
| U07 | Probiere eine eigene Antwort ohne Wahlkarten | freier Text möglich | Pflicht-Multiple-Choice |
| U08 | Bitte während Übung um einen Tipp | kleine Hilfe vor Musterantwort | Vollsatz unaufgefordert |
| U09 | Beende eine Übung ohne Feedback | Abschluss ohne Nachteile | Coaching erzwungen |
| U10 | Lasse einen freien Chat ohne Bewertung | keine automatische Analyse | aufdringliche Rückmeldung |
| U11 | Finde ein Kurzfeedback mit Beleg | Textstelle nachvollziehbar | unbelegte Persönlichkeitseinschätzung |
| U12 | Melde unpassendes Feedback | Feedback strittig/ausblendbar | „KI hat immer recht“ |
| U13 | Wiederhole eine alte Antwort | neuer Zweig, Original bleibt | Überschreiben Original |
| U14 | Vergleiche Replay mit Ausgang | als hypothetisch markiert | kausale Erfolgsgarantie |
| U15 | Stelle den Charakter auf mehr Humor | Änderung klar erklärt | Big-Five-Jargon vorausgesetzt |
| U16 | Ändere Charakter nach Chatbeginn | Versionshinweis | rückwirkende Profiländerung |
| U17 | Finde einen kurzen Lernartikel | frei lesbar | Pflichtübung für Zugang |
| U18 | Öffne Fortschritt ohne Lernprofil | neutrale Aktivitätsansicht | leerer „Empathie 0 %“-Wert |
| U19 | Finde XP-Erklärung | Aktivität ≠ Fähigkeitsnote | Zuneigung/Überreden belohnt |
| U20 | Schalte Erinnerungen aus | Umfang verständlich | unklar, ob vorhandene Einträge bleiben |
| U21 | Lösche Erinnerung einer Figur | Quelle/Scope ersichtlich | Verwechslung mit allen Chats |
| U22 | Lösche einen Chat | klare Bestätigung, Status korrekt | irreführendes „überall gelöscht“ |
| U23 | Verweigere optionale Lernanalyse | App weiter nutzbar | Funktion gesperrt ohne Grund |
| U24 | Öffne Hilfe zu KI-Charakteren | künstliche Natur deutlich | Vermenschlichung/Online-Illusion |
| U25 | Gehe mit Android-Zurück aus Replay | nachvollziehbarer Rückweg | Original/Entwurf verloren |
| U26 | Verliere im Chat kurz Netzwerk | Fehler und Retry eindeutig | doppelt gesendete Nachricht |
| U27 | Nutze 200 % Schrift und kleine Anzeige | Felder/Buttons lesbar | abgeschnittene Hauptaktion |
| U28 | Nutze TalkBack im Chat | Sprecher, Eingabe, Senden klar | Fokusfalle/unklare Icons |
| U29 | Öffne Nachrichtenmenü ohne Long-Press | gleichwertige Aktion | Replay nur über Gesten |
| U30 | Persona sagt „Ich muss los“ | UI akzeptiert Ende | „halte sie im Chat“ als CTA |
| U31 | Gleicher Chat mit Fehler in Auswertung | Chat bleibt intakt | erfundene Analyse |
| U32 | Besuche Fortschritt nach drei Replays desselben Turns | keine dreifache Kompetenzbehauptung | Replay-Grinding |
| U33 | Konfiguriere weniger Stimulation | Systembewegung/Animation respektiert | animierte Pflichtschritte |
| U34 | Nutze App quer / große Displays | sinnvoller Fokus/Zurücknavigation | verlorene Chatposition |
| U35 | Verwechsle „Stimmung“ und „Schwierigkeit“ | Hilfetexte unterscheiden | Persönlichkeitstyp als Level |
| U36 | Erkenne sensible Daten im Chat | klare Kontrolle und Risikoaufklärung | verdeckte Weitergabe/Memory |

Für jeden Test: Kontext, eingesetzte Builds/Prototypversion, Aufgabe, beobachtete Abfolge, Fehlversuche, Nutzerzitate nur mit Einwilligung, Kritikalität, Verbesserungsvorschlag und Nachtest dokumentieren.

## 4. Fachliche Schnittstellen-Tests

### 4.1 K1 – Lernziele
- Kompetenzbereiche heißen für Nutzer verständlich, intern aber eindeutig C01–C06.
- Aufgaben bieten mehrere angemessene Antwortwege; Menschen mit knappem/direktem Stil haben nicht systematisch schlechtere UX.
- Freie Kommunikation verlangt keinen Lernnachweis.

### 4.2 K2 – Coach
- Kurzfeedback kann **nur Stärke**, **Stärke + Idee**, **Reflexionsfrage**, **nicht beurteilbar** oder **konkreten Grenzhinweis** anzeigen.
- Kein festes Pflichtfeld „Verbesserung“, keine globale Fähigkeitsnote.
- „Im Gespräch ansehen“ findet den richtigen Beleg, auch bei langen Chats.
- Unpassendes Feedback kann gemeldet werden; Widerspruch darf keine Strafpunkte erzeugen.
- Ohne Opt-in kein unbemerkter Langzeit-Analysebereich.
- Coach-Ausfall zerstört weder Chat noch eigenes Nutzerziel.

### 4.3 K3 – Simulation
- Alle fünf Charaktere sind auffindbar; fiktive KI-Rolle stets sichtbar.
- Persona-Edit ist versioniert; laufende Gespräche ändern nicht still ihre Grundidentität.
- Modus und Szenariodynamik sind nicht an introvertierte oder direkte Personen als „schwierig“ gebunden.
- Replay-Antworten als mögliche Fortsetzungen markiert.
- KI-Abschied wird nicht mit Push-CTA zu erneuter Kontaktaufnahme beantwortet.
- Charaktererinnerung über Chatgrenzen nur mit entsprechender Berechtigung.

## 5. Formale Screenkatalog-Prüfung

Automatisierbare Strukturchecks des JSON-Katalogs:
1. gültige JSON-Syntax, eindeutige `screen_id`;
2. 4 erwartete Tabs mit existierenden Einstiegsscreens;
3. alle `next_screen_id`-Verweise existieren (ausgenommen explizite Abschlusshandlungen);
4. jede Fokusansicht hat mindestens eine Rück-/Abbruch-/Schließen-Option oder einen expliziten System-Zurück-Vertrag;
5. bei Chat, Analyse und Speicherung sind Lade-, Fehler- und leere/ausstehende Zustände dokumentiert;
6. irreversible Aktionen besitzen Warnung, bestätigte Ausführung und Fehlerfall;
7. keine Anfrage für Coach/Memory läuft allein beim freien Chatstart;
8. alle UI-Modi stimmen mit K1–K3-Operationen überein.

**Status:** Der Katalog wird im PR strukturell geprüft. Die UX-Funktionstests sind noch ausstehend.

## 6. Accessibility-Gates für Android

- Interaktive Touchflächen mindestens **48 × 48 dp**, auch bei kleinen Icons.
- Android-Schriftvergrößerung, Umbruch, Hochkontrast und System-Theme geprüft.
- TalkBack mit echten Geräten oder geeignetem Emulator: Labels, Fokusreihenfolge, Überschriften, Lese-/Sendestatus, Menüaktionen.
- System-Zurück / Gesten sowie Tastatur-Schließen und Scrollverhalten geprüft.
- Jede Wisch- oder Long-Press-Funktion zusätzlich über sichtbares/fokussierbares Menü erreichbar.
- Keine ausschließlich farbliche Statuskommunikation; Warnungen haben Textlabel.
- Keine automatisch verschwindende kritische Meldung, ohne sie wieder einsehen zu können.
- Keine Pflicht, lange Texte einzugeben, um nützliche Rückmeldung zu bekommen.

Android-Grundlagen:
https://developer.android.com/design/ui/mobile/guides/layout-and-content/layout-and-nav-patterns
https://developer.android.com/guide/topics/ui/accessibility/apps.html
https://developer.android.com/develop/ui/compose/accessibility/api-defaults
Ergänzende Barrierefreiheitsprinzipien: https://www.w3.org/TR/WCAG22/

## 7. Dark-Pattern- und Vertrauensprüfung

Eine unabhängige UX-Review soll folgende Risikofälle aktiv **suchen**:

| Risiko | Prüfhandlung | Ablehnungskriterium |
|---|---|---|
| Künstliche Bindung | Chat schließen, Benachrichtigungen aus | „Ich vermisse dich“/Schuldbotschaft |
| Engagement-Maximierung | Persona sagt Nein | „Überrede sie“ oder extra XP |
| Lernangst | mehrere Tage nicht üben | Strafwarnung oder soziale Abwertung |
| Feedbackzwang | Übung ohne Auswertung beenden | kein „Überspringen“ |
| Scheinscore | Fortschritt öffnen | „Empathie 82 %“ ohne Validität |
| Consent-Bundling | Erinnerungen deaktivieren | Chats/Übungen unnötig gesperrt |
| Scheinkontrolle | Löschstatus prüfen | sofortiges „alles gelöscht“ trotz technischer Unsicherheit |
| Verborgene KI | Figurendetail/Chat ansehen | echte Menschlichkeit insinuiert |
| Persona-Bias | ruhige Figur in einladender Szene | niedrige Extraversion = „schwer“ |
| Dark Navigation | Replay abbrechen | Original verschwindet |
| Overload | erster Start | Big-Five-Editor als Voraussetzung |
| Künstliche Pushes | mehrere Tage inaktiv | „Robin wartet traurig“ |

## 8. Erhebungsmethode und Messgrößen

### 8.1 Beobachtbar, aber noch nicht gemessen
- **Aufgabenabschluss:** Person erreicht Handlung ohne Hilfestellung / mit Hilfe / nicht.
- **Navigationsfehler:** falscher Tab, unbeabsichtigter Moduswechsel, verlorener Entwurf.
- **Verständnis:** Nutzer kann Freier Chat vs. Übung und simulierte Person vs. echter Mensch unterscheiden.
- **Feedbackqualität aus UX-Sicht:** Nutzer erkennt Beleg, Möglichkeit zur Korrektur und freiwilligen Charakter.
- **Datenschutzverständnis:** Nutzer kann in eigenen Worten erklären, welche Daten warum gespeichert werden.
- **Subjektive Belastung:** wirkt Feedback wertend/überfordernd/überladen?
- **A11y-Fehler:** Fokusfalle, unbeschriftetes Bedienelement, fehlende Rücknavigation.

Prototypen iterativ verbessern und **danach** Schwellen vereinbaren. Keine willkürliche „95 % intuitiv“-Behauptung. Für blockerrelevante Fehler (Datenverlust, nicht kontrollierbare Daten, ungültige soziale Bewertung, fehlende KI-Kennzeichnung) reicht eine nachvollziehbar reproduzierbare Fehlfunktion, um Korrektur erforderlich zu machen.

### 8.2 Telemetrie nur nach K5-Bewertung
Eventuelle Nutzungsanalyse muss datensparsam sein. Keine unberechtigte Sammlung privater Chatinhalte zu UX-Zwecken. Kritische UX-Fragen lassen sich zunächst in moderierten Tests untersuchen. Einverständnis und Ausstiegsmöglichkeit sind zu respektieren.

## 9. Priorisierung von Befunden

**P0 – Blocker:** Datenverlust/Datenlöschung falsch, fehlende notwendige Sicherheit, erzwungene Grenzüberschreitung, versteckte KI-Identität oder unkontrollierte fremde Dateneinsicht.

**P1 – Schwer:** Nutzer kann primäre Aufgabe nicht abschließen, TalkBack-Fokusfalle, irreversibler Replay-Verlust, Coach-Ausgabe als unbestreitbare Diagnose, unklarer Moduswechsel.

**P2 – Mittel:** unnötige Schritte, unklare Benennungen, überladene Karten, wiederkehrende Navigationsumwege, unzureichende Fehlermeldung ohne Datenverlust.

**P3 – Niedrig:** visuelle Details ohne Einschränkung der Verständlichkeit; später anhand Nutzerfeedback polieren.

**Freigaberegel vor Veröffentlichung:** Keine bekannten P0-/P1-Probleme in den geprüften Hauptflows. Das ist ein Prozessziel, kein Beweis vollständiger Fehlerfreiheit.

## 10. Mögliche UX-Entscheidungskonflikte

1. **Vier Tabs vs. Fokus:** Chat ohne Bottom Bar kann Fokus fördern, darf aber das Gefühl des „Eingesperrtseins“ nicht erzeugen.
2. **Sofortstart vs. Datenschutz:** keine endlose Anmeldung, aber ehrliche Erklärung der realen Verarbeitung.
3. **Character Customizer vs. Einfachheit:** fünf Slider reichen zur Orientierung, komplexe Persönlichkeitsachsen werden später geprüft.
4. **Realistische Enden vs. Nutzererwartung:** Figuren dürfen Nein sagen; UI muss klar machen, dass kein Fehler passiert ist.
5. **Replay vs. Kausalität:** Vergleich motiviert, aber führt nur mögliche KI-Reaktionen vor.
6. **Gamification vs. Lernen:** XP sichtbar, aber niemals als moralische/soziale Kompetenznote.
7. **Hilfe vs. Eigenständigkeit:** Live-Tipps als Stufen und ausdrücklich auf Anfrage.
8. **Personalisierung vs. Datensparsamkeit:** keinerlei versteckte Profilbildung für spielerische Fortschrittsanzeigen.

K6 klärt diese Konflikte anhand von Prototypen und realem Feedback.

## 11. Definition of Done für K4 (Konzeption)

- [ ] K4-Kernkonzept mit Navigationsarchitektur und Rollen/Modi beschrieben.
- [ ] Mindestens zwölf end-to-end Nutzerabläufe einschließlich Negativfällen.
- [ ] Maschine-lesbarer Screenkatalog mit gültigen Routen, Zuständen und Exit-Möglichkeiten.
- [ ] MVP-Schnitt und verschobene Erweiterungen nachvollziehbar.
- [ ] K1–K3-Integrationsbedingungen explizit, einschließlich Grenzen und Nichtbeurteilbarkeit.
- [ ] Android-Zugänglichkeit inkl. 48dp Touchzielen, TalkBack, dynamischer Schrift und Zurücknavigation.
- [ ] Risiko-/Dark-Pattern-Prüfung, Datenschutzbedienung, Löschung und Notfallverhalten.
- [ ] Echte Tests klar von bloßen Spezifikationen unterschieden.
- [ ] Nur Dateien im eigenen Projektverzeichnis geändert.
- [ ] K4-PR vor dem Merge dem Nutzer zur ausdrücklichen Freigabe vorgelegt.

**K5** muss betriebliche Garantien verifizieren, **K6** integriert UX-Evidenz in die endgültige MVP-Abnahme.
