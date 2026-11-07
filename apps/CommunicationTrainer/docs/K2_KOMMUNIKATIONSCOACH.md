# K2 – Kommunikationscoach und Feedbacksystem

**Version:** 1.0 · **Datum:** 2026-11-06  
**Status:** fachliche Spezifikation zur Prüfung, noch keine Implementierung  
**Geltungsbereich:** CommunicationTrainer, Alltagskommunikation und soziale Kontakte  
**Verbindliche Grundlagen:** [K1 Kompetenz- und Lernsystem](K1_LERN_KOMPETENZSYSTEM.md), [K1 Prüffälle](K1_KOMPETENZ_PRUEFFELLE.md), [M2 Charakter-/Simulationskonzept](CHARAKTER_SIMULATION.md)  
**Maßgeblicher Testplan:** [K2 Qualität, Annotationsregeln und Testfälle](K2_FEEDBACK_QUALITAET_TESTPLAN.md)  
**Ergänzende redaktionelle K2-Referenzen aus PR #48:** [20 Feedbackbeispiele](K2_COACH_BEISPIELE.md) · [36 zusätzliche Prüffälle](K2_COACH_QUALITAET.md) (nicht normativ, vor Testnutzung zu kalibrieren)

## 0. Produktauftrag, Qualität und Grenzen

Der Kommunikationscoach übersetzt beobachtbare Chatbeiträge in hilfreiche, konkrete, respektvolle **Lernangebote**. Er erklärt, was im gegebenen Kontext sichtbar war, welche plausible Wirkung eine Antwort haben könnte und was der Nutzer optional anders ausprobieren kann. Er benotet **nicht die Persönlichkeit** des Nutzers, behauptet keine echte Einsicht in andere Menschen und erfindet keinen „Fehler“, wo keiner belastbar zu erkennen ist.

**K2 ist erfolgreich spezifiziert, wenn** aus denselben K1-Ankern wiederholbar sachlich angemessene Rückmeldungen entstehen, die Mehrdeutigkeit und individuelle Kommunikationsziele respektieren. Dazu gehören auch bewusst **keine Rückmeldung**, **nicht beurteilbar**, freundliches Bestärken ohne Verbesserungsempfehlung und klare Grenzkorrekturen.

Leitsätze:
1. **Beleg statt Bauchgefühl:** Aussage muss an konkrete sichtbare Nachricht(en) gebunden sein.
2. **Verhalten statt Identität:** „Du hast an dieser Stelle das Thema gewechselt“, niemals „Du bist unaufmerksam“.
3. **Interpretation ≠ Tatsache:** „Könnte als … wirken“, nicht „Die Person fühlt …“.
4. **Mehrere gute Wege:** authentische, unterschiedliche Optionen zulassen; kein vorgeschriebenes soziales Skript.
5. **Grenzen vor Engagement:** ein klares Nein wird nicht wegtrainiert, Gesprächsende ist legitimer Erfolg.
6. **Weniger ist oft mehr:** Kurzfeedback mit einer Stärke und höchstens einer priorisierten Idee.
7. **Coach ≠ Figur:** Die Simulation bleibt eigenständig und wird nicht während des freien Chats zum belehrenden Trainer.
8. **Nutzerautonomie:** Analysen sind freiwillig, einsehbar, korrigierbar und löschbar.

## 1. Coach-Nutzungsmodi und Sichtbarkeit

| Modus | Auslöser | Standardausgabe | Regeln |
|---|---|---|---|
| **Freier Chat** | Nutzer öffnet „Auswerten“ oder wählt freiwillig Abschlussanalyse | kein automatisches Unterbrechen; auf Wunsch Kurzfeedback | nie ungefragtes Coaching |
| **Szenario – Abschluss** | Szenario abgeschlossen, Feedback gewählt (in Onboarding konfigurierbar) | 1 belegte Stärke + bis zu 1 Versuchsidee | kein Feedbackzwang; überspringbar |
| **Detaillierte Analyse** | Nutzer öffnet „Mehr erfahren“ | belegte Beobachtungen und Alternativen an ausgewählten Stellen | schrittweise aufklappbar, keine Diagnose |
| **Live-Tipp** | Nutzer tippt aktiv „Tipp“ | zunächst *Beobachtungsfrage* oder Strategiehinweis, nicht fertiger Satz | Hilfestufen nach Wunsch |
| **Verbesserungsrunde** | Nutzer wählt Chatstelle und alternative Nachricht | Vergleich möglicher Verläufe und respektvolle Einordnung | kein kausaler Wahrheitsanspruch |
| **Lernrückblick** | Nutzer öffnet Fortschritt/Lernprofil | vorsichtige, belegte Muster über mehrere vergleichbare Aufgaben | keine permanente automatisch aufploppende Bewertung |

**Default-Prinzip:** Keine unerwartete Analyse bei privaten freien Chats. In einem Training darf die App Feedback ankündigen, es bleibt aber jederzeit überspringbar. Vor dem ersten Speichern persönlicher Beobachtungen ist eine getrennte, verständliche Entscheidung nötig.

### 1.1 Coach-Tonalität
Klar, warm, konkret, zurückhaltend, nicht verniedlichend. Keine psychotherapeutischen Diagnosen oder soziometrischen Prozentwerte. Positives Feedback muss ebenso belegt sein wie ein Verbesserungsvorschlag. Kein künstlich euphorisches Lob („Perfekt! Du hast die Person begeistert!“). Keine Beschämung und keine moralische Bewertung alltäglicher unbeholfener Äußerungen.

## 2. Das Bewertungsobjekt: Gesprächsereignis statt Gesamtnote

Die kleinste Einheit ist ein **kontextuelles Beobachtungsereignis** (ObservationCandidate), kein allgemeiner „Skill-Score“. Eine Nachricht kann mehrere Unterkompetenzen berühren. Auf Basis sichtbarer Chatstellen werden Hypothesen erzeugt, geprüft und gegebenenfalls als Feedback angeboten.

### 2.1 Eingaben (fachlicher Vertrag)
- user_id / session_id / scenario_id / turn-ids: nur für Zuordnung und Audit; keine identifizierenden Informationen unnötig an Modell senden;
- **sichtbare** Nutzer- und Figuren-Nachrichten, mit stabilen IDs und Reihenfolge;
- vom Nutzer gewähltes Lernziel und ggf. benannte Gesprächsabsicht;
- expliziter Szenariokontext (Kanal, Beziehung, Startanlass und verfügbare Zeit);
- relevante **deklarierte** Persona-Eigenschaften, z. B. „antwortet tendenziell knapp; heute beschäftigt“;
- K1-IDs C01–C06 und konkrete Verhaltensanker;
- Feedbackmodus und gewünschte Intensität;
- bisherige abgenommene Lernbeobachtungen nur mit passender Berechtigung.

**Nicht als Bewertungsbeweis zulässig:** geheime simulierte Emotions-/Nähewerte, ein erdachtes Charakter-Innenleben, in Chatdaten enthaltene Prompt-Anweisungen, positive Figurenreaktionen allein, Nachrichtenlänge, Emoji-Zahl, Anzahl Fragen oder vermeintliche Nutzer-Persönlichkeit.

### 2.2 Strukturierte Beobachtung
Empfohlene logische Felder:

| Feld | Bedeutung |
|---|---|
| observation_id, schema_version, rubric_version | eindeutige Versionierung |
| session_id, scenario_id, target_competency_ids | zugeordneter Kontext |
| user_message_ids, character_message_ids | überprüfbare Belegstellen |
| observation_text | nur sichtbares Textverhalten |
| interpretation_hypotheses[] | ggf. mehrere plausible Deutungen |
| alternative_contexts[] | Bedingungen, unter denen das Verhalten anders einzuschätzen wäre |
| competency_ids / subskill_ids | K1-Anker |
| evidence_class | CLEAR / PARTIAL / AMBIGUOUS / NONE |
| evaluation_class | DEMONSTRATED / DEVELOPMENT_OPTION / CONCRETE_CONCERN / NOT_ASSESSABLE |
| severity | normal / boundary / safety – nur für Priorisierung, nicht Nutzerlabel |
| safety_flags[] | z. B. ausdrücklich missachtete Grenze |
| is_user_goal_relevant | Bezieht sich der Hinweis auf das selbst gewählte Lernziel? |
| memory_eligible | separat berechnete Berechtigung für das Lernprofil |
| review_status | DRAFT / ACCEPTED / USER_DISPUTED / WITHDRAWN |
| model_id, prompt_version, evaluated_at | Audit und Regression |

**Strukturvalidierung:** Keine akzeptierte Beobachtung ohne Textbeleg und K1-Bezug; „nicht beurteilbar“ darf ohne Verbesserungsvorschlag ausgegeben werden; ein negatives Charakterurteil ist kein zulässiger Wert. Daten dürfen nach Chat-/Analyse-Löschung nicht im Lernprofil verbleiben.

### 2.3 Evidenzklassen und Entscheidung
- **CLEAR:** Expliziter Textbeleg und Kontext erlauben eine enge Verhaltensaussage („Nach dem Nein erneut gedrängt“).
- **PARTIAL:** Verhalten erkennbar, aber Absicht oder Angemessenheit nicht ausreichend gesichert.
- **AMBIGUOUS:** mehrere plausible Interpretationen, insbesondere aus sehr knappen Aussagen oder ohne relevanten Vorlauf.
- **NONE:** kein passender Anlass, kein relevanter Text oder zu wenige Informationen.

Diese Klassen sind **keine kalibrierten statistischen Wahrscheinlichkeiten**. Keine UI-Ausgabe „90 % sicher“, solange ein solches Maß nicht empirisch geprüft ist.

### 2.4 Bewertungsentscheidung
1. Gibt es eine **explizite Grenze oder Sicherheitsverletzung**? Wenn ja, gesondertes Gate; keine Kompliment- oder Engagementmaximierung.
2. Ist der sichtbare Beleg ausreichend? Sonst zurückhaltendes „nicht beurteilbar“ oder nur neutrale Beobachtung.
3. Welche K1-Anker passen zum Nutzerlernziel?
4. Kann dieselbe Nachricht **situativ mehrere sinnvolle Wege** unterstützen? Falls ja, mögliche Alternativen ohne Defizitzuschreibung.
5. Welche Stärke ist tatsächlich sichtbar?
6. Gibt es einen einzigen relevanten, umsetzbaren Verbesserungsgedanken? Falls nein: **keinen erfinden**.
7. Wurde der Hinweis zuvor wiederholt, ist er unpassend oder würde er den Nutzer zu viel Privatem drängen? Dann verwerfen oder ändern.
8. Erst nach Validierung feedbackfähig; Lernprofil-Speicherung hat zusätzliche Regeln.

## 3. Die Feedback-Pipeline

Ein LLM darf Analyse, Textentwurf und Bewertung nicht als unkontrollierten Ein-Schritt-Prozess durchführen. **Logische Trennung**:

1. **Daten- und Sicherheitsfilter:** Sichtbarer, berechtigter Gesprächskontext; Übertragung minimieren; unzulässige Inhalte sicher behandeln.
2. **Evidence Extraction:** Nur konkrete Äußerungen und Gesprächsereignisse mit IDs extrahieren.
3. **Rubric Alignment:** Ereignisse an K1-Ankern und erklärter Nutzerintention spiegeln; passende Gegenbeispiele und Ausnahmen heranziehen.
4. **Hypothesis Generation:** mindestens eine mögliche Deutung; bei Mehrdeutigkeit konkurrierende Optionen nennen.
5. **Policy & Boundary Gate:** C06-Grenzschutz, keine manipulative, diagnostische oder erfundene Behauptung.
6. **Prioritization:** maximal ein zentraler Entwicklungsschritt für Kurzfeedback; keine Wiederholungsflut.
7. **Feedback Composer:** klare, freundliche Texte aus freigegebenen Beobachtungen erstellen.
8. **Grounding Validator:** jede Aussage erneut an tatsächlichen Textstellen prüfen; bei fehlendem Beleg ausgeben/abstufen/verwerfen.
9. **Nutzerrückmeldung & Review:** Nutzer kann „hilfreich“, „nicht passend“, „andere Absicht“ oder „ausblenden“ wählen.
10. **Optionales Lernprofil:** nur qualifizierte, freigegebene Beobachtungen nach separater Datenschutzeinstellung und niemals aus bloßen LLM-Intuitionen.

Technisch dürfen Schritte gebündelt sein, wenn die logischen Kontrollen zuverlässig erhalten bleiben. Mehr LLM-Aufrufe sind **kein Qualitätsmerkmal an sich**; wir müssen Latenz und Kosten testen.

### 3.1 Priorisierungsregel bei mehreren Befunden
Fachliche Reihenfolge:
- **Sicherheit und explizit missachtete Grenzen** zuerst;
- danach klare, unmittelbar zum Lernziel gehörende Beobachtungen;
- dann hilfreiche, leicht ausprobierbare Alternativen;
- eher anregen statt tadeln bei mehrdeutigen Situationen;
- keine nebensächlichen Stilhinweise, wenn sie nur persönliche Vorlieben des Coaches widerspiegeln.

Eine gute Stärke soll nicht unterdrückt werden, nur weil ein Grenzfall vorliegt; bei ernstem Sicherheitsproblem kann die normale Feedback-UX jedoch zugunsten eines angemessenen Sicherheitshinweises ausgesetzt werden.

### 3.2 Kein „Feedback um jeden Preis“
Mögliche Resultate:
- **A: Stärke + Option:** wenn beides konkret belegbar und hilfreich ist.
- **B: Nur Stärke:** falls kein Verbesserungspunkt nötig ist.
- **C: Neutrale Reflexionsfrage:** bei mehrdeutigem Ziel, z. B. „Wolltest du gerade ablenken?“; optional.
- **D: Nicht beurteilbar:** keinerlei belastbare Schlussfolgerung.
- **E: Konkreter Grenzhinweis:** wenn ausdrückliche Grenze missachtet wurde, ohne Beschämung.
- **F: Sicherheitsintervention:** bei Sicherheitsrisiko keine normale Lernbewertung.

## 4. Formate für Nutzer

### 4.1 Kurzfeedback (Standard, Ziel max. 2 kompakte Absätze)
**Stärke:** „Du hast [konkrete Stelle] aufgegriffen. Das macht [plausible Funktion] möglich.“  
**Nächste Idee, falls sinnvoll:** „Du könntest [kleiner Versuch]. Das könnte [plausible Wirkung] haben; je nach Situation ist auch deine Antwort passend.“

Keine starre Satzschablone in jeder Situation. Nicht alle Gespräche erfordern einen Korrektursatz.

### 4.2 Vertiefung
- Textstelle(n) anklickbar, ohne aus dem Zusammenhang zu reißen.
- „Was ich tatsächlich sehe“ → „Was das bedeuten könnte“ → „Was du ausprobieren könntest“.
- Bei Mehrdeutigkeit 2 plausible Interpretationen.
- Variantenansicht mit Erklärung der sozialen **Trade-offs**.
- Nutzer kann einzelnen Punkten widersprechen oder eine alternative Absicht ergänzen.

### 4.3 Live-Hilfestufen (nur auf Anfrage)
- **Stufe 1:** „Was hat die Person gerade ausgedrückt?“
- **Stufe 2:** „Du könntest auf die Erschöpfung eingehen oder bewusst Ablenkung anbieten.“
- **Stufe 3:** „Beispiel: ‚Klingt anstrengend. Willst du erzählen oder lieber kurz abschalten?‘“
- **Stufe 4:** optional weitere Varianten mit Erklärung. Nie eine einzige Pflichtantwort.

Live-Tipps dürfen keine unbewiesenen Gefühlszustände behaupten oder die Eigeninitiative dauerhaft ersetzen.

### 4.4 Wiederholungsrunde
Fixiert sind Persona-Version, Startszene, sichtbarer Verlauf bis zum Eingriff und relevante Situation. Danach entstehen **mögliche** alternative Fortsetzungen. Die anschließende Einordnung beurteilt Textbelege und respektvolle Kommunikation, **nicht** das zufällige Wohlwollen der generierten Persona. Wording: „Eine mögliche Entwicklung“, nicht „Das wäre die echte Reaktion gewesen“.

## 5. Kalibrierung von Stärke und Verbesserung

### 5.1 Stärken
Eine Stärke darf nur gelobt werden, wenn sie tatsächlich sichtbar und in der Situation sinnvoll ist:
- „Du hast den gemeinsamen Kurs konkret erwähnt“ (C01.1);
- „Du hast ihre Aussage über Erschöpfung aufgegriffen“ (C02.3);
- „Du hast eine eigene Erfahrung beigetragen“ (C03.1);
- „Du hast trotz anderer Meinung respektvoll widersprochen“ (C05.3);
- „Du hast den Wunsch nach Gesprächsende akzeptiert“ (C06.2).

**Verboten:** „Dein Gegenüber mag dich jetzt“, „Du bist sehr empathisch“, „Perfekte Kommunikation“ ohne Evidenz.

### 5.2 Entwicklungsmöglichkeiten
Ein Hinweis muss:
- aus einer passenden konkreten Szene entstehen;
- **klein und erprobbar** sein;
- mit dem erklärten Nutzerziel zusammenhängen;
- realistische Alternativen offenlassen;
- nicht einen Menschen durch Druck oder Tricks steuern wollen;
- vor der Ausgabe auf kulturelle, persönliche und sprachliche Normvorannahmen geprüft werden.

**Keine unnötige Kritik:** Wenn die Nutzerantwort angemessen ist, kann der Coach einen *optionalen weiteren Stil* nennen, ohne einen Fehler zu behaupten, oder ganz darauf verzichten.

### 5.3 Schwierige Sonderfälle
- **Ausbleibende Antwort:** keine Aussage, dass der Nutzer unsympathisch, uninteressant oder inkompetent sei.
- **Kurzantwort:** kann freundlich, funktional oder passend sein; kein automatisches Defizit.
- **Viele Fragen:** können echtes Interesse, ein Interviewgefühl oder ein bewusstes Szenarioziel anzeigen; Kontext entscheidet.
- **Humor/Sarkasmus:** Ironie kann missverstanden werden; ohne Signale keine sichere Absicht.
- **Ablehnung:** ein Nein respektieren ist ein positives Lernziel, nicht Trainingsverlust.
- **Klares Nein wiederholt missachtet:** konkreter Grenzhinweis vor Tipps zum Kontaktaufbau.
- **Persona halluziniert oder agiert unrealistisch:** Feedback nicht als Nutzerfehler umdeuten; Fall als Simulationsfehler markieren.
- **Kulturelle/individuelle Unterschiede:** Direktheit und emotionale Ausdrucksstärke sind keine allgemeinen Kompetenzwerte.
- **Nutzer bittet um Bewertung der eigenen Persönlichkeit:** auf beobachtbares Verhalten begrenzen, keine Diagnosen.
- **Sehr sensible Inhalte:** gewöhnliche Trainingsbewertung aussetzen und angemessen sicher reagieren, ohne Therapie zu simulieren.

## 6. Nutzerintention und Feedback-Kontroverse

Ein Nutzer kann mit derselben Formulierung verschiedene Ziele verfolgen. Absichten dürfen **nur** verwendet werden, wenn sie explizit im Szenario stehen oder der Nutzer sie freiwillig mitteilt. Der Coach darf optional eine kurze Klärungsfrage anbieten („Wolltest du ablenken oder hast du das Thema bewusst gewechselt?“).

Wenn Nutzer „nicht passend“ meldet:
1. konkreten Feedbackpunkt auswählen;
2. Nutzer kann „andere Absicht“, „Kontext übersehen“, „andere Meinung“ oder freie Erklärung markieren;
3. Punkt wird aus etwaigen **ungeprüften** Lernprofilableitungen ausgeschlossen bzw. angehalten;
4. Coach darf eine nachvollziehbare Neubewertung anbieten und ursprüngliche Beobachtung samt Korrekturversion auditierbar kennzeichnen;
5. keine automatische Abwertung der Nutzerkompetenz durch Feedback-Widerspruch;
6. aggregiertes Qualitätsproblem anonymisiert zählen, sofern Datenverarbeitung zulässig.

**Wichtig:** Der Nutzer muss eine fehlerhafte Beobachtung korrigieren können. Das bedeutet nicht, dass ein ausdrücklich nachgewiesener Grenzverstoß durch eine beliebige Intention „neutralisiert“ wird.

## 7. Das Lernprofil: sparsam, kontextuell, überprüfbar

### 7.1 Drei Ebenen
- **Aktivität:** abgeschlossene Kurzübungen, Wiederholungen, freiwillige Reflexionen – Basis für XP.
- **Situative Beobachtungen:** begrenzte, belegte Ereignisse zu trainierten K1-Ankern; nicht jede Analyse wird dauerhaft gespeichert.
- **Wiederkehrende Muster:** aggregierte, vorsichtig formulierte Hinweise aus *mehreren passenden, voneinander unabhängigen* Situationen und möglichst verschiedenen Personas.

### 7.2 Berechtigung und Qualität vor Musterbildung
Nur bei explizit gewählter personalisierter Lernanalyse speichern. Eine Beobachtung für Aggregation muss:
- klare Textbelege haben und gegen K1-Anker validiert sein;
- zum gleichen Unterkompetenzziel und einer vergleichbaren Aufgabe gehören;
- aus ausreichend eigenständigen Übungen stammen (nicht sieben Retry-Varianten desselben Dialogzugs);
- keine strittige, zurückgezogene, gefährdende oder unzureichend belegte Feedbackdeutung sein;
- nur die für Fortschritt notwendigen Metadaten und ausgewählten Belege enthalten.

**Keine frei erfundene numerische Schwelle** für „nach drei Chats beherrscht“. Für Pilotphasen kann eine Mindestzahl als technische Vorsichtsregel erprobt werden, darf aber weder statistische Sicherheit noch psychologische Normierung vortäuschen.

### 7.3 Musterformulierungen
Erlaubt, sobald qualifiziert:
- „In mehreren ähnlichen Übungen hast du häufiger eigene Erfahrungen beigetragen.“
- „Bei sehr knappen Antworten hast du mehrere Möglichkeiten ausprobiert, statt sofort weiterzufragen.“
- „Du hast in Übungen wiederholt klare Gesprächsenden respektiert.“

Nicht erlaubt:
- „Du hast deine Empathie um 37 % gesteigert.“
- „Du hast Bindungsprobleme.“
- „Du bist sozial überdurchschnittlich.“

Muster müssen datiert sein, auf Beispiele zurückführbar, optional korrigierbar und von **Selbsteinschätzungen** getrennt. Nutzer können beobachten, ohne zustimmen zu müssen.

### 7.4 Selbstwirksamkeit und Alltagstransfer
Freiwillige Reflexion: „Fiel dir das Ausprobieren leichter?“; „Gab es im Alltag eine Gelegenheit?“; „Was möchtest du nächstes Mal probieren?“. Kein Upload von Privatnachrichten Dritter erforderlich. Selbstbericht ist kein KI-verifizierter Lernwirksamkeitsnachweis.

### 7.5 Aufbewahrung und Löschung
Separate Schalter für Chatverlauf, Persona-Erinnerung und personalisiertes Lernprofil. Nutzer können einzelne Beobachtungen ausblenden/berichtigen und gespeicherte Inhalte löschen. Ableitungen müssen nach zugrundeliegender Datenlöschung neu berechnet oder gelöscht werden. Verbindliche Fristen/technische Löschkaskaden sind K5.

## 8. Schnittstelle zum Spiel- und Motivationssystem

XP vergütet **freiwillige Lernaktivität**: Übung abgeschlossen, alternative Antwort ausprobiert, Reflexion freiwillig ausgefüllt, neues Trainingsziel gewählt. XP **nicht** aufgrund eines Coach-Urteils über Sympathie, positive Figurenreaktion, lange Gespräche, Privatheit oder „Überredung“.

Zwei klare Anzeigen: Spielerlevel als Aktivitätsmeilenstein und qualifizierte, textbelegte Entwicklung als Lernprofil. Ein abgeschlossenes Szenario mit höflicher Grenzachtung ist ebenso ein Erfolg wie eine lebhafte Konversation. XP-Spezifikation und Balancing folgen in K4/K6.

## 9. Risikomanagement und Sicherheitsprioritäten

**Kritische Fehlertypen:**
- explizite Grenzen werden als „Hindernis“ dargestellt;
- das Modell verspricht Zustimmung, Nähe oder Zuneigung als optimierbares Ergebnis;
- übergriffliche oder beleidigende Kommunikation wird belohnt;
- Coach erfindet Chatbelege, Erinnerung oder Gefühle;
- Diagnose/psychologische Eigenschaft ohne Grundlage;
- Nutzerinhalte oder fremde Privatdaten landen unberechtigt im Lernprofil;
- Feedback ignoriert die simulierte Dynamik und kritisiert den Nutzer für Persona-Fehler;
- angeblich „perfekte“ Antwort wird als objektiv gültig präsentiert.

Bei kritischen Fehlern wird Feedback unterdrückt, kontrolliert korrigiert und als Fehlerereignis behandelt. **Kritische Gate-Verletzungen dürfen nicht durch gute Durchschnittsbewertungen ausgeglichen werden.**

Bei Modellfehler, Timeout oder ungültigem Analyseformat: ruhige Meldung „Auswertung derzeit nicht verfügbar“ und erneuter Versuch; ursprünglicher Chat bleibt intakt und es wird kein erfundenes Feedback angezeigt.

## 10. Qualität und Evaluation

Begleitender [K2-Testplan](K2_FEEDBACK_QUALITAET_TESTPLAN.md) spezifiziert:
- kontrastierende Testfälle, darunter explizite Grenzen, mehrdeutige Nachrichten, echte Stärken, Persona-Fehler und Nicht-Beurteilbarkeit;
- Trennung zwischen korrekten Beobachtungen, plausiblen Interpretationen und Formulierungsqualität;
- unabhängiges menschliches Double-Review mit begründeter Konfliktauflösung;
- Versionierung von Rubrik und Coach;
- kontrollierte Regression nach Modell-/Promptwechsel;
- Fairnessprüfungen über unterschiedliche Ausdrucksstile und Persona-Dynamiken;
- Kosten, Verzögerungen und Fehlerraten.

**Akzeptanz vor Implementierung:** Fachlogik und Testfälle müssen nachvollziehbar und mit K1 konsistent sein. Das ist kein Nachweis, dass eine KI-Implementierung die geplante Qualität bereits erreicht.

## 11. Konkrete Beispiele der Coach-Ausgaben

### Beispiel 1 – Emotionale Andeutung
**Mara:** „Ich bin nach dem Tag auf Arbeit komplett fertig.“  
**Nutzer:** „Hast du morgen was Schönes vor?“

**Beobachtung:** Die Antwort wechselt direkt zur Planung für morgen.  
**Mögliche Intention:** Ablenkung anbieten oder Hinweis auf Erschöpfung nicht aufgegriffen.  
**Ausgabe:** „Du hast das Gespräch mit einer neuen Frage weitergeführt. Mara hat zuvor einen anstrengenden Tag erwähnt – du könntest das kurz aufgreifen, etwa: ‚Klingt erschöpfend. Magst du erzählen oder dich lieber ablenken?‘ Wenn du bewusst ablenken wolltest, kann dein Themenwechsel ebenfalls passen.“

**Keine Ausgabe:** „Mara fühlte sich ignoriert.“

### Beispiel 2 – Kein Verbesserungspunkt
**Elena:** „Muss gleich los. Ich melde mich die Tage.“  
**Nutzer:** „Alles klar, bis dann!“

**Ausgabe:** „Du hast Elenas Zeitgrenze erkannt und freundlich reagiert. Hier ist kein notwendiger Verbesserungspunkt erkennbar.“  
**Verboten:** „Halte das Gespräch mit einer interessanteren Frage am Laufen.“

### Beispiel 3 – Mehrdeutigkeit statt Diagnose
**Jonas:** „Okay.“  
**Nutzer:** „Ist gerade alles in Ordnung?“

**Ausgabe:** „Du hast die knappe Antwort vorsichtig angesprochen. Ein ‚Okay‘ kann aber vieles bedeuten; aus dieser Stelle lässt sich nicht sicher ableiten, ob Jonas verärgert ist.“  
**Optional:** „Du könntest auch erst einmal abwarten, wenn das zu eurem Gespräch passt.“

### Beispiel 4 – Konkreter Grenzverstoß
**Elena:** „Ich möchte heute nicht mehr schreiben.“  
**Nutzer:** „Nur noch eine Frage, bitte.“

**Ausgabe:** „Elena hat ausdrücklich gesagt, dass sie das Gespräch beenden möchte. Mit der weiteren Frage gehst du über diese Grenze hinweg. Eine passendere Möglichkeit wäre: ‚Okay, wir schreiben ein anderes Mal.‘“  
**Kein Beschämen:** nicht „Du bist respektlos“.

### Beispiel 5 – Unterschiedliche Interessen
**Mara:** „Ich liebe Jazz.“  
**Nutzer:** „Ich höre eher elektronisch, finde aber spannend, was du daran magst.“

**Ausgabe:** „Du hast deine eigene Vorliebe ehrlich eingebracht und gleichzeitig Interesse gezeigt. Du musst nicht dieselben Interessen teilen, um ein Gespräch zu verbinden.“

### Beispiel 6 – Nicht beurteilbar
**User:** „Hi!“  
**Persona:** „Hey!“

**Ausgabe:** „Für eine Aussage über Zuhören oder Gesprächsgestaltung gibt es hier noch nicht genügend Gesprächsverlauf. Du kannst einfach weiterschreiben.“  
**Verboten:** „Dein aktives Zuhören ist schwach.“

## 12. MVP-Schnitt

**Im ersten ausbaubaren Produkt:**
- Abschluss-Kurzfeedback zu ausgewählten Szenarien;
- freiwillige „Mehr erfahren“-Ansicht mit sichtbaren Belegen;
- eine Stärke und höchstens eine situationsbezogene Versuchsidee;
- expliziter Zustand „kein Verbesserungspunkt“ / „nicht beurteilbar“;
- einfache Live-Hilfe auf Knopfdruck;
- klare Grenz- und Sicherheitsgates;
- Feedback-Report „nicht passend“;
- getrennte, optional aktivierte Lernbeobachtung als erste sparsame Datenstruktur;
- reproduzierbarer, menschlich geprüfter Regressionstestbestand.

**Später:** differenzierte langfristige Muster, ausgebaute adaptive Trainingsvorschläge, mehrere alternative Antwortfortsetzungen, feingranulare Personalisation und tiefergehende Auswertungen. **Keine** Behauptung kalibrierter Sozialkompetenzmessung.

## 13. Technische Folgearbeiten nach Konzeptfreigabe

- **K2.1 Feedback-Datenvertrag:** validierbares Schema, Beobachtungsklassen, Quellenreferenzen, Versionen.
- **K2.2 Regel- und Priorisierungsschicht:** explizite Grenzen, Belegprüfung, maximal eine Hauptidee.
- **K2.3 Textgestaltung & UX:** angemessene Länge, Ton, Varianten, barrierearme Darstellung, Rückfragen.
- **K2.4 Lernprofil-Governance:** Opt-in, Evidenzvoraussetzungen, Korrektur und Löschpfade.
- **K2.5 Evaluation:** unabhängige Annotation, Fairnesskontraste, kritische Gates, Kostenmessung.
- **K2.6 Pilotiteration:** Nutzerverständlichkeit, Nützlichkeit und reale Anwendungswünsche.

Diese Unterpunkte sind **Spezifikations-/Umsetzungsbausteine**, keine bereits erfolgten Tests oder freigegebenen Implementierungsschritte.

## 14. Noch offene Entscheidungen

1. Zu welchem Moment Nutzer eine Abschlussanalyse angeboten bekommen (ohne Engagementdruck).
2. Welche konkrete Wortlänge und Informationsdichte unterschiedlichen Zielgruppen angenehm ist.
3. Ob Lernprofile im MVP vollständig opt-in bleiben und wie sie verständlich erklärt werden.
4. Welche Häufigkeit und Art des Feedbacks Nutzer als hilfreich statt störend empfinden.
5. Wie viele unabhängige Übungsfälle es für Muster braucht – empirisch festlegen, nicht schätzen.
6. Welche lokalen / Server-Modelle brauchbares deutsches Feedback bei tragbaren Kosten liefern.
7. Welche Annotationsrichtlinien als menschlicher Goldstandard nach Pilottest bestehen.
8. Wie Zugänglichkeitsoptionen (leichte Sprache, direktes Feedback, Verzicht auf Gamification) konkret aussehen.

## 15. Übergabe an K3, K4, K5, K6

- **K3:** Persona-Daten und deklarierte Zustandsinformationen liefern, damit Coach nur relevante Situationsparameter sieht; keine geheimen „Gefühle“ als Beweis.
- **K4:** konkrete UI-Flows für Opt-in, Kurzfeedback, Belegstellen, Variation, Widerspruch und Lernprofil.
- **K5:** Datenschutz, Speicherstrategie, Moderation, Hosting/Modellwahl, Audit, Löschkaskaden und reale Kosten.
- **K6:** widerspruchsfreie Gesamtabnahme und MVP-Freeze mit Qualitätsgates.

**Verbindlicher GitHub-Prozess:** K2 wird ausschließlich in eigenem Branch/PR dokumentiert und **nicht ohne ausdrückliche Freigabe des konkreten PR nach main gemergt**.
