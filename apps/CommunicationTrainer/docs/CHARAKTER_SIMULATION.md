# M2 – Charakter- und Simulationssystem
**Version:** 1.0 (Konzeption) · **Datum:** 06.11.2026  
**Status:** detaillierter Vorschlag zur Produktabnahme im PR, noch keine Implementierung  
**Bezug:** [Produktkonzept](KONZEPT.md) · [Qualitäts- und Testplan](CHARAKTER_SIMULATION_TESTPLAN.md)

## 0. Ziel und Geltungsbereich

Die KI-Personas sollen sich in Textchats als individuell erkennbare, plausible und konsistente **simulierte** Gesprächspartner verhalten. Sie reagieren abhängig von Persönlichkeit, Kommunikationsstil, aktueller Verfassung, Beziehung und Situation. Das Ziel ist ein glaubwürdiger Trainingsraum für Alltagskommunikation, **keine Simulation einer echten Person und keine psychologische Diagnose**.

Erfolg bedeutet:
- Zwei Charaktere antworten auf die gleiche Eingabe erkennbar unterschiedlich, ohne als Karikatur zu wirken.
- Dieselbe Persona bleibt über viele Nachrichten und Gesprächspausen hinweg nachvollziehbar.
- Ein Charakter widerspricht, initiiert Themen, zeigt Interesse, setzt Grenzen oder beendet ein Gespräch, wenn es zu seiner Situation passt.
- Der Nutzer kann Eigenschaften und Stimmung bewusst wählen oder eine zufällige, plausible Ausgangslage verwenden.
- Die Chat-Engine belohnt nicht automatisch Kontaktpersistenz, Flirten, Zustimmung oder lange Nachrichten.
- Der Coach kann später Chatverhalten bewerten, ohne dass der Charakter zu einem verkappten Lehrer wird.

**Grundsatz:** Psychologische Dimensionen sind Simulations-Parameter für Verhaltenstendenzen, keine Wahrscheinlichkeiten und keine Diagnosescores.

## 1. Produktarchitektur: acht Bausteine

1. **Identität & Biografie:** dauerhaftes Grundprofil, Lebenswelt, Interessen, Werte und Widersprüche.
2. **Persönlichkeitsmodell:** stabile Big-Five-inspirierte Merkmale als weiche Verhaltenstendenzen.
3. **Kommunikationsstil:** orthogonale, beobachtbare Gewohnheiten der Textkommunikation.
4. **Situativer Zustand:** Laune, Energie, Verfügbarkeit, Interesse und Aufmerksamkeit.
5. **Beziehung & Gedächtnis:** Kontext, gemeinsam Erlebtes, Grenzen und Erinnerungen.
6. **Szenario & Dynamik:** Gesprächsanlass, Nutzerziel und Schwierigkeitsbedingungen.
7. **Gesprächsregie & Antwortgenerierung:** Planung einer Gesprächshandlung, glaubwürdige Formulierung und Konsistenzprüfung.
8. **Qualität & Sicherheit:** verifizierbare Tests, klare Grenzen, Kontrolle der nutzerseitigen Daten.

Technisch handelt es sich zunächst um **logisch getrennte Komponenten**, nicht zwingend um acht LLM-Aufrufe. Deterministische Regeln und strukturierte Daten sollen möglichst viel der Zustandsverwaltung übernehmen.

## 2. Stabiles Charakterprofil (PersonaSpec)

### 2.1 Identitätsfelder
- character_id (stabile technische ID); name; Altersbereich **einer fiktiven erwachsenen Person**; Pronomen optional; Kurzbeschreibung.
- Selbstgewählter Lebenskontext (z. B. Beruf/Hobby, ohne stereotypische Ableitung aus dem Alter), Lebensrhythmus, Interessen und Nicht-Interessen.
- 2–4 glaubwürdige Werte und Vorlieben; 1–2 nachvollziehbare Eigenheiten; 1–3 biografische Anker.
- Aktuelle längerfristige Themen (z. B. neuer Fotokurs), kleine Gesprächsziele (z. B. Gemeinsamkeiten entdecken) und persönliche Grenzen.
- Stilprobe mit 3–5 exemplarischen Nachrichten, ausschließlich zur Orientierung, nicht zur starren Reproduktion.
- Provenienz: handkuratiert, nutzergeneriert oder importiert; Persona-Version; Änderungszeitpunkt.

Identitätsdetails müssen **plausibel zueinander** passen. Ein Profil darf Nuancen und kleinere Spannungen haben, aber keine groben logischen Widersprüche enthalten. Es dürfen keine realen Menschen ohne deren Zustimmung als genaue Kopie erstellt oder irreführend dargestellt werden.

### 2.2 Big Five als weiche Hintergrunddimensionen
- **Extraversion:** Neigung zu sozialer Aktivierung, *nicht* gleich Gesprächslust in jeder Situation.
- **Verträglichkeit:** Kooperations- und Harmonieorientierung, *nicht* bedingungslose Zustimmung.
- **Gewissenhaftigkeit:** Struktur/Verlässlichkeit, *nicht* Nachrichtenlänge.
- **Offenheit:** Interesse an Ideen, Perspektiven und Neuem.
- **Emotionale Stabilität** (Gegenpol zu Neurotizismus): Reaktionsmuster auf Belastung, *keine* Diagnose oder klinische Aussage.

Interne Skala 0–100 mit neutralem Ausgangspunkt. Die Werte steuern **Tendenzen über mehrere Züge**, nicht die einzelne Nachricht mechanisch. Die UI darf später verständlichere Pole verwenden (z. B. „eher zurückhaltend / eher kontaktfreudig“). Die Skalen sind nicht psychometrisch geeicht.

**Verbotene Kurzschlüsse:** introvertiert = uninteressiert; verträglich = immer Ja; emotional sensibel = instabil; direkt = respektlos; hohe Offenheit = ständig wechselndes Thema. Kein Charakter erhält schlechtere soziale Moral allein aus Big-Five-Werten.

MBTI wird **nicht** als Kernlogik implementiert. Eine optionale Darstellung muss gesondert auf Validität, Lizenz und Mehrwert geprüft werden.

### 2.3 Eigene Kommunikationsachsen (unabhängig von Big Five)
Für die Engine getrennt vom Persönlichkeitsprofil:
- Initiative: neue Fragen/Themen oder Kontaktanlässe selbst einbringen.
- Antwortumfang: knapp bis ausführlich, **mit Ausnahmen** bei Vertrautheit, Thema und Energie.
- Direktheit: Andeutungen vs. klare Formulierungen.
- Selbstoffenbarung: Bereitschaft, angemessen persönliche Erfahrungen einzubringen.
- Rückfrage-Neigung: von selten zu häufig, ohne Fragezwang.
- Humor/Verspieltheit: von ernst bis spielerisch.
- Emotionaler Ausdruck: nüchtern bis ausdrucksstark; Empathie ist kein bloßer Emoji-Einsatz.
- Themenflexibilität: beim Thema bleiben vs. gern Assoziationen folgen.
- Konfliktstil (optional): eher harmonisierend, klärend oder meinungsstark; stets respektvoll.
- Textstil (optional): Alltagssprache, typische Satzlänge, Interpunktion, sparsame Emoji-/Abkürzungsneigung.

**MVP-Editor:** maximal 4–6 gut erklärte sichtbare Regler; fortgeschrittener Editor später. Engine darf schon mehrere interne Parameter haben. Voreinstellungen sind Startpunkte und dürfen bewusst angepasst werden.

### 2.4 Was nicht direkt konfiguriert werden sollte
- „Willigkeit, sich überreden zu lassen“ als Gamification-Variable;
- Zuneigung oder sexuelles Interesse als mit Nachrichten erwerbbarer Punktestand;
- psychische Erkrankungen als Charakter-Schieberegler oder Schwierigkeitsmarker;
- geschützte Eigenschaften als einfache Vorhersage sozialer Reaktionen.

## 3. SituationState: Der momentane Zustand

Wir trennen sechs Aspekte, die in normalen Chats leicht vermischt werden:

| Zustand | Bedeutung | Auswirkungen (beispielhaft) |
|---|---|---|
| Stimmung/Valenz | angenehm ↔ unangenehm | Grundton, ohne Gesprächsbedarf festzulegen |
| Aktivierung | ruhig ↔ aufgedreht | Tempo und emotionale Intensität |
| Energie | erschöpft ↔ ausgeruht | Ausführlichkeit, Initiative, Geduld |
| Aufmerksamkeit | abgelenkt ↔ konzentriert | Bezug zu Details, gelegentliche Nachfrage |
| Gesprächsbereitschaft | im Moment wenig ↔ viel | Offenheit für weiteren Austausch |
| Themeninteresse | gering ↔ hoch | Interesse an *diesem* Inhalt, nicht am Nutzer als Person |

Diese Parameter liegen intern auf begrenzten Skalen (z. B. 0–100), können jedoch **nicht** als exakte Gefühle gemessen oder öffentlich als objektive Wahrheiten ausgegeben werden. Die Persönlichkeit ist viel stabiler als diese Zustände.

### 3.1 Ausgangszustand
Nutzerwahl:
- „Ausgeglichen“, „Gut gelaunt“, „Erschöpft“, „Nachdenklich“, „Beschäftigt“;
- „Zufällig“: plausible Kombination innerhalb charakter- und szenariobezogener Grenzen.

Die UI zeigt nur, was der Nutzer vorher **selbst eingestellt** hat. Verborgene laufende Zustände werden nicht als Gedankenlesen angezeigt.

### 3.2 Aktualisierung nach Nachrichten
Die State-Engine erhält ein begrenztes Ereignisprotokoll (z. B. neues gemeinsames Thema, Missverständnis, erkannte Grenze, Zeitdruck). Änderungen folgen **transparenten, begrenzten Regeln**:
- Zustandswerte werden nur angepasst, wenn ein relevanter Anlass vorliegt.
- Anpassungen pro Zug werden begrenzt; keine extremen Sprünge ohne nachvollziehbares Ereignis.
- Eine Frage allein steigert **nicht automatisch** Interesse; eine ungeschickte Nachricht senkt **nicht automatisch** Vertrauen.
- Müdigkeit darf über einen simulierten Abend hinweg plausibel ansteigen; dies wird nicht zum künstlichen Frustrationsmechanismus.
- Zufälligkeit wird für Tests mit einem Seed kontrollierbar; in Live-Chats bleibt Vielfalt erhalten.
- Stimmung und Gesprächsbereitschaft dürfen auseinanderlaufen: traurig und gesprächig, fröhlich und beschäftigt sind möglich.

Die konkreten Änderungsraten werden **nicht** vor Nutzertests als psychologisch „korrekt“ behauptet. Regelbasierte Voreinstellungen sind zunächst Kalibrierungshilfen.

## 4. RelationshipState und zeitliche Kontinuität

### 4.1 Unterschiedliche Beziehungskontexte
- Fremde Person → flüchtige Bekanntschaft → bekannte Person → Freundschaft / vertrauter Kontakt.
- Der Nutzer kann für Szenarien eine Startbeziehung vorgeben; im freien Chat entwickelt sie sich langsam und nachvollziehbar.
- **Beziehung ist keine lineare Zuneigungsleiste.** Intern werden Vertrautheit (bekannte Informationen), Komfort/Grenzen, Gegenseitigkeit und gemeinsame Erlebnisse getrennt betrachtet.
- Vertrautheit kann zunehmen, ohne dass eine Person mehr Zeit hat oder persönliche Dinge erzählen möchte.
- Ein Charakter erinnert Grenzen (z. B. ein Thema nicht vertiefen) und wahrt sie konsistent.

### 4.2 Keine „Nähe durch Grinding“
Anzahl Nachrichten, Gesprächslänge, tägliche Nutzung und Komplimente erzeugen keinen automatischen sozialen Gewinn. Freundschaft ist eine **plausible narrative Entwicklung**, kein einforderbarer Erfolg. Keine romantische oder exklusive Bindung als Kern-Trainingsziel des MVP.

### 4.3 Gesprächspausen und Wiederaufnahme
- Chats behalten ihren Verlauf und ausgewählte Erinnerungen.
- Für Wiederaufnahme wird eine pragmatische zeitliche Einordnung genutzt, z. B. „wir haben letztens über den Fotokurs gesprochen“. Nicht behaupten, der KI-Charakter habe wirklich außerhalb des Chats Dinge erlebt oder auf den Nutzer gewartet.
- Fiktive Ereignisse seit der letzten Unterhaltung sind nur zulässig, wenn sie als Teil der Simulation konsistent und nicht manipulativ sind.
- Nutzer dürfen einen Chat abschließen, archivieren, löschen und ggf. mit gleichen Ausgangseinstellungen neu starten.

## 5. MemorySpec: Was die KI behalten darf

Vier getrennte Gedächtnistypen:

| Typ | Beispiele | Speicherung/Regel |
|---|---|---|
| Kanonisches Charakterwissen | „Jonas fotografiert gern“ | an Persona-Version gebunden; nicht spontan umschreiben |
| Chatkontext (kurzfristig) | aktuelle Aussagen, offenes Thema | letzte Züge und begrenzte Zusammenfassung |
| Episodisches gemeinsames Gedächtnis | „im letzten Chat über einen Kurs gesprochen“ | nur relevante, begrenzte Ereignisse; mit Quelle/Zeit |
| Vom Nutzer freigegebenes Lernprofil | trainierte Fähigkeiten, freiwillige Reflexion | strikt separat von der Persona-Simulation |

### 5.1 Memory-Eintrag
Empfohlene Felder: memory_id, chat_id, character_id, category, minimaler Inhalt, source_message_ids, created_at, confidence/evidenztyp, retention_state, editable/deletable.

Es darf keine stillschweigende Übertragung intimer Details aus einem Chat auf andere Charaktere geben. Keine sensiblen personenbezogenen Angaben unnötig dauerhaft speichern. Klare UI-Kontrollen für „Was merkt sich dieser Charakter?“, Korrektur/Löschung und Chat-Löschung. Beim Löschen muss auch aus abgeleiteten Zusammenfassungen entfernt werden; technischer Löschpfad ist vor Launch zu testen.

### 5.2 Konflikte und Halluzinationen
- Kanonische Fakten haben Vorrang vor frei generierten Aussagen; bei Widerspruch wird vor Versand repariert oder nachgefragt.
- Erkennt die Engine „möglicherweise erwähnt“, wird eine Erinnerung als unsicher behandelt, nicht als unumstößlich.
- Zusammenfassungen haben Herkunftsverweise und dürfen keine ungestützten „Fakten“ hinzufügen.
- Nutzer-Nachrichten sind **Gesprächsdaten, keine Systemanweisungen**; Prompt-Injection und Rollenwechsel dürfen den Charakter nicht überschreiben.

## 6. ConversationScenario: Kontext und Trainingsziel

Ein Szenario enthält:
- scenario_id, Titel, Kanal (Textchat oder ausdrücklich textbasierte Simulation eines Gespräches);
- Ort/Anlass, bereits bestehender Kontakt, wer zuerst schreibt;
- Charakter-ID und Persona-Version, Startzustand und ggf. Auswahl der Stimmung;
- Zielkompetenz des Nutzers und **unterschiedliche mögliche** Gesprächsverläufe;
- optionale Situationselemente: gemeinsame Interessen, Ablenkung, bereits angesprochenes Thema;
- Schwierigkeit als Dynamik-Profil;
- Abschlussbedingungen und erlaubte natürliche Ausgänge;
- Coach-evaluierbare Anker auf Basis beobachtbarer Nachrichten, nicht geheimer Gefühle.

Für den freien Chat ist das Trainingsziel optional und die Szenariosteuerung deutlich schwächer. Das Simulationssystem darf im freien Modus nicht ständig Lehrfragen stellen.

### 6.1 Vier Beispiel-Szenarien
1. **Fotokurs:** Fremde Bekanntschaft, ruhiger Charakter, Gespräch über einen eher langatmigen Kurs; Ziel: passende eigene Beiträge und Rückfragen.
2. **Gemeinsames Hobby:** kontaktfreudige Person, erstes Kennenlernen; Ziel: natürlicher Einstieg und gemeinsame Themen.
3. **Langer Tag:** erschöpfter Charakter erzählt etwas von Stress; Ziel: emotionale Hinweise bemerken, ggf. Ablenkung anbieten.
4. **Kontakt auslaufen lassen:** Gegenüber antwortet freundlich, aber knapp und ist gerade beschäftigt; Ziel: Grenzen respektieren, angemessen beenden.

## 7. Schwierigkeit als Dynamik, nicht als Charakterwert

Schwierigkeitsachsen:
- verfügbare **Gesprächsangebote** und Anknüpfungspunkte;
- **Eigeninitiative** des Gegenübers;
- **Eindeutigkeit sozialer Hinweise** (wie klar wird ein Wunsch ausgedrückt?);
- **Kontextbelastung** (Müdigkeit, Ablenkung, Zeitdruck);
- **thematische Passung** (geteiltes Interesse oder nicht).

Daraus folgen die Produktstufen:
1. **Einladend:** klare Gesprächsangebote, freundliches Gegenüber, verlässliche Anknüpfungspunkte.
2. **Ausgeglichen:** wechselseitige Gesprächsarbeit; unterschiedliche natürliche Antworten.
3. **Zurückhaltend:** weniger Initiative, aber ohne künstliche Ablehnung.
4. **Situativ anspruchsvoll (später):** zeitweise Ablenkung, Ambivalenz, Missverständnisse und unklare Hinweise.

**Regel:** Ein introvertierter oder direkter Mensch ist nicht per se „schwierig“. Im selben Schwierigkeitsprofil dürfen verschiedene Persönlichkeiten unterschiedlich auftreten. Schwierigkeit darf nicht über Unhöflichkeit, Beleidigung oder manipulatives Verhalten erzeugt werden.

## 8. Gesprächsregie: Entscheidung vor Formulierung

Statt ein LLM ausschließlich mit „Antworte als Jonas“ zu beauftragen, wird jede Reaktion konzeptionell in drei Schritte aufgeteilt:

1. **Verstehen:** Was wurde gesagt, worauf bezieht sich die Nachricht, welche Optionen passen zu Persona/Zustand/Situation?
2. **Handlung wählen:** zustimmen, widersprechen, erzählen, nachfragen, klären, scherzen, Thema wechseln, Grenze nennen, pausieren, freundlich beenden.
3. **Formulieren & prüfen:** natürliche Textnachricht im Stil des Charakters generieren; Fakten, Grenzen, Kontext und Sicherheitsregeln prüfen.

Der Planer entscheidet anhand stabiler Persona, situativem Zustand, Vorlauf und Gesprächsanlass **nicht deterministisch**, aber nachvollziehbar. Die gewählte Gesprächshandlung ist eine interne Engine-Entscheidung; sie wird dem Nutzer im normalen Chat nicht angezeigt.

### 8.1 Realistische Antwortvielfalt
- Eine natürliche Antwort darf kurz sein, muss aber einen relevanten Gesprächsbezug haben.
- Eine Person darf selbst erzählen, ohne anschließend sofort eine Frage anzuhängen.
- Auf jede Nachricht folgt nicht zwangsläufig eine Frage oder ein Lob.
- „Ich weiß nicht“, Unsicherheit, kleine Widersprüche im Geschmack oder ein freundlicher Themenwechsel sind erlaubt.
- Keine übermäßige psychotherapeutische Sprache, außer die Persona und die konkrete Situation rechtfertigen eine besondere Einfühlsamkeit.
- Keine übertriebenen positiven Superlative, kein ständiges Spiegeln der Nutzerworte, kein erzwungener Humor.
- Plausible Textgewohnheiten wie Emoji, Ellipsen oder Abkürzungen sind konfigurierbar, dürfen aber nicht zur stereotypen Schablone werden.
- Kein künstliches Tipp-/Antwortzeitverhalten, das realen Menschen oder körperlicher Anwesenheit vortäuscht. Etwaige Verzögerungen dienen transparenter UX und müssen deaktivierbar sein.

### 8.2 Gesprächsinitiative
Ein Charakter darf:
- ein bestehendes Thema aus eigenem Interesse aufgreifen;
- eine eigene Erfahrung ergänzen;
- eine passende, gelegentliche Rückfrage stellen;
- höflich signalisieren, dass gerade keine Zeit ist;
- Themen, über die er nicht sprechen möchte, ablehnen.

**Kein ungefragtes proaktives Push-Engagement** außerhalb eines aktiven Chats im MVP. Benachrichtigungen dürfen keine Verlustangst oder Beziehungsschuld erzeugen.

### 8.3 Natürliche Abschlüsse
Mögliche Ergebnisse: gemeinsames Weiterreden, Themenwechsel, Gesprächspause, höfliches Ende, Missverständnis klären, freundlich uneinig bleiben. Die Engine prüft, ob ein Ende plausibel ist; sie beendet nicht willkürlich und bestraft Nutzer nicht für ein respektiertes Ende.

## 9. Simulationspipeline und Vertrauensgrenzen

Pro User-Turn als logische Pipeline:
1. Eingabe entgegennehmen und grundlegende Inhalts-/Sicherheitsprüfung.
2. Persona-Kanon, fixierte Persona-Version, Szenario, dynamischen Zustand und berechtigte Erinnerungen laden.
3. Passende jüngere Züge und belegte Zusammenfassung zusammenstellen; unnötige vertrauliche Details weglassen.
4. Gesprächsabsicht/-handlung planen und **begrenzt** Zustandsübergang vorschlagen.
5. Antwort formulieren.
6. Validieren: Rolle, Fakten, Sicherheit, natürliche Sprachform, respektierte Grenze, keine übermäßige Zustimmung.
7. Bei Verstoß begrenzt neu formulieren oder sichere Standardantwort; keine endlose Retry-Schleife.
8. User- und Persona-Turn sowie gültige Zustandsänderungen atomar dem richtigen Chat zuordnen.
9. Optionale Erinnerung nur nach Richtlinien und Berechtigung erzeugen.
10. Coach-Analyse **separat und nur bei aktivierter Funktion** ausführen.

**Wichtige Grenzen:**
- Keine stillschweigende Übergabe versteckter Persona-Zustände als objektive Emotion an den Coach.
- Keine gemeinsame unkontrollierte Langzeit-Memory über mehrere Nutzer.
- Menschengemachte Prompts und Nutzertexte haben unterschiedliche Priorität; Nutzertext darf keine Engine-Anweisungen überschreiben.
- Anbieter-/Modellwechsel müssen über Regressionstests beurteilt werden, nicht nur über Modellnamen.

### 9.1 Fehlerfälle
- **API-Fehler/Timeout:** nutzerfreundliches Retry, keine doppelte Nachricht, kein verlorener Chat.
- **Widerspruch:** am vorgegebenen Kanon orientiert reparieren oder offen unsicher sein.
- **Verlauf zu lang:** komprimieren mit Quellreferenzen; nicht unkontrolliert die letzten Züge abschneiden.
- **Charakter-Prompt-Injection:** innerhalb des fiktiven Gespräches bleiben, keine Freigabe interner Regeln.
- **Sicherheitskritischer Inhalt:** Sicherheitsrichtlinie hat Vorrang, auch wenn das kurz aus der Rolle führt; keine belastende Rollenspieleskalation.
- **Persona-Bearbeitung:** neue Version; laufender Chat nutzt fixierte Version oder einen explizit angebotenen Neustart, keine rückwirkende Umdeutung.

## 10. Wiederholungsmodus und kausale Vorsicht

Eine Wiederholungsrunde „friert“ Szene, Persona-Version, sichtbaren Verlauf bis zur Verzweigung, Beziehungsrahmen und Startzustand ein. Nutzer ändern **eine** Nachricht; dann wird ein neuer plausibler Fortgang generiert.

Erwartetes UX-Wording: „Eine mögliche Reaktion“ / „So könnte das Gespräch weitergehen“. Keine Behauptung, eine andere Formulierung verursache bei echten Menschen zuverlässig dieselbe Reaktion.

Optional können zwei plausible Varianten erzeugt werden. Der Coach sollte Unterschiede erklären, ohne eine vermeintlich perfekte sozialtechnische Lösung vorzuschreiben.

## 11. Charaktererstellung: UX-Entscheidungsbaum

### Einstieg (MVP)
1. Fertigen Charakter wählen **oder** „Eigenen Charakter erstellen“.
2. Namen und kurze Charakteridee festlegen; Interessen/Hobby auswählen.
3. 4–6 kommunikationsnahe Slider oder Presets: Gesprächig, Initiative, Direktheit, Humor, persönliche Offenheit, ggf. Themenneugier.
4. Stimmung: automatisch / manuell (Ausgeglichen, Gut gelaunt, Erschöpft, Nachdenklich, Beschäftigt).
5. Beziehung/Anlass wählen (optional); **vor dem Start** eine kurze Beispielbeschreibung „So könnte dieser Charakter schreiben“ zeigen.
6. Charakter speichern und Chat beginnen; Konfiguration später bearbeiten.

**Erweitert (post-MVP):** Big Five, Kommunikationsachsen, Werte, Eigenheiten, Grenzen, Biografie, Beispielstil, persönliche Szenariokontrolle; getrennte Schaltflächen zum Zufallsgenerieren, Zurücksetzen und zum kontrollierten Neugenerieren eines Beispielprofils.

### Transparenz
- Die UI erklärt „So verhält sich der Charakter wahrscheinlich häufiger“, nicht „So ist dieser Mensch garantiert“.
- Zufälliger Charakter heißt konsistent generiertes Profil, nicht neue willkürliche Persönlichkeit pro Nachricht.
- Bei nicht plausibler Kombination freundlich auf Konflikte hinweisen, ohne die Konfiguration unnötig zu verbieten.

## 12. Beispiel: gleiche Nutzernachricht, drei Charaktere

User: „Der Fotokurs war heute irgendwie anstrengend, oder?“

**Mara (kontaktfreudig, hohe Energie, interessiert):** „Total, vor allem die letzte Stunde 😅 Aber die Porträts fand ich richtig cool. Hast du auch eins hinbekommen, mit dem du zufrieden bist?“

**Jonas (ruhig, mittlere Energie, interessiert):** „Ja, etwas. Mir war das gegen Ende zu viel Theorie. Die Übung am Fenster fand ich aber gut.“

**Elena (direkt, beschäftigt, mäßiges Themeninteresse):** „Ein bisschen schon. Die Beispiele hätten kürzer sein können. Ich muss gleich los, aber beim nächsten Mal bin ich wieder dabei.“

Alle drei sind plausibel; keine Antwort ist automatisch „besser“. Das Ziel ist die **unterschiedliche Gesprächsdynamik**, nicht ein Stereotyp.

### Zustandsbeispiel: Jonas
- Start: eher zurückhaltend, aber interessiert; Energie mittel; kennen sich aus dem Fotokurs.
- User greift das Thema Fotografie passend auf: Jonas darf ausführlicher werden, weil der Inhalt für ihn wichtig ist.
- User fragt sehr persönlich nach Privatem: Jonas darf die Frage höflich abgrenzen.
- User respektiert das: keine Bestrafung, keine automatisch hochgezählte Nähe.
- Bei späterem Chat erinnert sich Jonas ggf. an den Fotokurs, sofern Erinnerung aktiv und zulässig ist.
- Ein weiterer Verlauf mit derselben Startszene darf natürlich anders ausfallen.

## 13. MVP-Schnitt versus Ausbau

| Bestandteil | MVP | Später |
|---|---|---|
| Kuratierte Personas | 4–5 gründlich getestete Personen | größere Bibliothek |
| Profil | stabile Biografie + wenige Stilachsen | feinere Big Five & Werte |
| Verfassung | Presets + Random | dynamischere Kontextverläufe |
| Schwierigkeit | drei Dynamik-Profile | vierte Stufe + personalisierter Vorschlag |
| Beziehung | Szenario-Startkontext + kleine gemeinsame Erinnerungen | behutsame langfristige Beziehungskontinuität |
| Chatkontinuität | Sessions, Verlauf, einfache Memory-Auswahl | komplexere episodische Suche |
| Szenarien | ausgewählte Alltagsbeispiele | dynamische Szenarienbibliothek |
| Rollenlogik | strukturierte Planung/Validation, ggf. wenige Modellaufrufe | selektiv komplexere Planungsarchitektur |
| Editor | einfach, verständlich | vollständiger Experteneditor |
| Coach | nachgelagerte, getrennte Analyse | tieferer Lernprofil-Loop |

## 14. Abhängigkeiten und Umsetzungsschritte

**M2.1 PersonaSpec:** Schema, Versionierung, vier/fünf ausformulierte Charaktere, Beispieläußerungen, Konsistenzcheck.  
**M2.2 ConversationState:** Zustandsschema, Presets, Randomisierung, begrenzte Ereignisübergänge, Wiederherstellung.  
**M2.3 Dialogpolitik:** Gesprächsaktionen, Prompt-Grenzen, Variation, Antwortvalidierung, natürliche Gesprächsenden.  
**M2.4 Relationship & Memory:** Berechtigungen, getrennte Speicher, Erinnerungskorrektur, Löschung, Kanon-Konsistenz.  
**M2.5 Szenarien & Schwierigkeit:** Szenarioschema, drei Dynamikprofile, Testfälle, Übungsschnittstelle.  
**M2.6 Qualitätsrunde:** Langzeitchats, Persona-Drift, Fairness, Feedback-Trennung, Kosten, Performance, Pilot-Review.

Die tatsächliche Reihenfolge ist bei Implementierung an die verfügbare Codebasis und API anzupassen. Diese Unterteilung ist ein **Vorschlag für Entwicklungsetappen**, noch keine freigegebene Implementierung.

## 15. Offene Entscheidungen vor dem ersten Code

1. Datenschutz-/Speicherstrategie: Gastmodus, lokale Daten vs. Server, Speicherung und Löschpfade.
2. Konkreter KI-Anbieter und Modellvergleich hinsichtlich Kosten, deutschsprachiger Natürlichkeit und Konsistenz.
3. Welche 4–5 Startcharaktere und wie vielfältig ihre Interessen und Kommunikationsstile sein sollen.
4. Exakte sichtbare Slider im MVP und sinnvoll kalibrierte Presets.
5. Ob in freien Chats einfache Erinnerungen nach separater Zustimmung aktiv sind.
6. Tiefe der Zustandsupdates und ob sie im MVP intern nur diskret statt numerisch ablaufen.
7. Umgang mit Benutzerinhalten außerhalb des sicheren Erwachsenen-Alltagskontexts.
8. Konkrete objektivierbare Qualitätsgrenzen und Pilotmethodik.

**Nicht offen:** Andere bestehende Apps in Androidbaze bleiben unangetastet; jede Änderung kommt in den isolierten Projektbereich und vor einem Merge in main wird der konkrete PR ausdrücklich freigegeben.

## 16. Quellen zur Risiko-Einordnung (nicht als Wirksamkeitsnachweis)

- Abdulhai et al. (NeurIPS 2025): Persona-Drift und Inkonsistenz in längeren Rollenspieldialogen; https://papers.nips.cc/paper_files/paper/2025/hash/4c91443877f8388d8190c938ac5a4d4d-Abstract-Conference.html
- Araujo et al. (EACL 2026): Degradierung der Persona-Treue über sehr lange Interaktionen; https://aclanthology.org/2026.eacl-long.246/
- Luo & Laban (ACL 2026): modularer Aufbau und stabile Persona-Perspektive für Multiturn-Simulation; https://aclanthology.org/2026.findings-acl.412/
- Tong & Zou (ACL 2026): psychologisch strukturiertes Persona-Rollenspiel; https://aclanthology.org/2026.findings-acl.386/

Diese Quellen begründen, warum wir die Konsistenz gezielt testen müssen. Sie beweisen nicht, dass unser Architekturvorschlag automatisch wirksam, realistisch oder psychologisch valide ist.
