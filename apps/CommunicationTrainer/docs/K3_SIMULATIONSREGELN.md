# K3 – Konkrete Simulationsregeln und Datenverträge

**Version:** 1.0 · **Datum:** 06.11.2026 · **Status:** Konzept zur Prüfung, keine laufende Engine  
**Produktbereich:** \`apps/CommunicationTrainer/\`  
**Bezug:** [M2 Basiskonzept](CHARAKTER_SIMULATION.md) · [K1 Lernmodell](K1_LERN_KOMPETENZSYSTEM.md) · [K2 Coach](K2_KOMMUNIKATIONSCOACH.md) · [K3 Personas](../specs/k3_personas_v1.json) · [K3 Testplan](K3_SIMULATION_TESTPLAN.md)

## 0. Abgrenzung und Grundentscheidungen

K3 operationalisiert die in M2 entworfenen Prinzipien. Es legt noch keine Programmierbibliothek, KI-API, endgültige Datenbank, Messaging-Architektur oder empirisch kalibrierte Persönlichkeitsskala fest.

**Simulierter Charakter = stabile fiktionale Identität + unabhängiger Kommunikationsstil + per Chat veränderlicher Zustand + berechtigter Beziehungskontext + nachvollziehbares Gesprächsereignis.**

Die Figur dient dem Nutzer als plausibler Gesprächspartner. Sie ist **kein heimlicher Trainer**, kein Ersatz für reale Beziehungen und keine reale Person. Eine stimmungsbedingte Reaktion ist möglich, aber keine Aussage über die tatsächlichen Gefühle realer Menschen. Alle numerischen Parameter sind **Simulations-Designwerte** und dürfen nicht als psychologische Messung beworben werden.

## 1. Verbindliche Modellgrenzen

| Ebene | Lebensdauer | Veränderung | Verantwortlichkeit |
|---|---|---|---|
| PersonaSpec / Canon | stabil je Version | nur neue Persona-Version | langfristige Identität, Werte, Präferenzen |
| CommunicationStyle | stabil je Version | explizite Nutzerbearbeitung / Version | sprachliche und soziale Grundtendenzen |
| SessionState | Gespräch / simulierte Situation | kontrollierte Ereignisse | Energie, Aufmerksamkeit, Aktivierung, Stimmung, Verfügbarkeit |
| RelationshipState | Chat / freigegebene Kontinuität | belegt und langsam | Vertrautheit, Komfort, Grenzen, gemeinsame Geschichte |
| ScenarioState | Sitzung / Wiederholungszweig | Szenario-Ereignisse | Kontext, Anlass, Rollen, Zeitbudget, Schwierigkeitsprofil |
| Memory | nach Speicher-/Löschregeln | nur aus belegten Fakten | ausdrücklich zulässige Wiederaufnahme |
| CoachObservations | nach K2 und Opt-in | gesondertes Verfahren | Lernen, **nicht** Figurentext generieren |

**Nicht erlaubt:** Persona im bestehenden Chat still ändern; Stimmungswert als diagnostische „Wahrheit“ ausgeben; eine simulierte Beziehung durch bloße Nachrichtenzahl hochrechnen; Erinnerungen verschiedener Nutzer oder Charaktere vermischen; Coach-Beobachtungen ohne Freigabe in die Figuren-Simulation zurückführen.

## 2. PersonaSpec (statisches Profil)

Die konkreten fünf fiktionalen v1-Profile stehen in [k3_personas_v1.json](../specs/k3_personas_v1.json).

### 2.1 Erforderliche Felder
- **Schema/Version:** \`schema_version\`, \`id\`, \`version\`, Änderungszeitpunkt in der späteren App.
- **Identität:** fiktionaler Name, erwachsener Altersrahmen, optionale Ansprache, prägnante Charakterkarte.
- **Kanon:** Lebenskontext, Interessen, weniger interessante Themen, Werte, biografische Anker, längerfristiger Alltagsgegenstand.
- **Big-Five-Hintergrund:** extraversion, agreeableness, conscientiousness, openness, emotional_stability (0–100 als relativ definierte, nicht kalibrierte Tendenzen).
- **Kommunikationsachsen:** initiative, verbosity, directness, self_disclosure, question_tendency, humor, emotional_expression, topic_flexibility (0–100).
- **Narrative Regeln:** eigene Gesprächsziele, wiedererkennbare Eigenheiten, situative Reibung, persönliche Grenzen, „not_this_character“.
- **Beispieläußerungen:** ein Einstieg, eine Sachantwort, ein freundlicher Grenzsatz; Beispiele sind Stilanker, keine Textbausteine.
- **Startkontexte:** passende Alltagsszenarien.

### 2.2 Validierung
- Eintrag hat eindeutige ID; Version ist positiv ganzzahlig.
- Alle numerischen Achsen sind Ganzzahlen in [0,100] und vollständig vorhanden.
- Kanonische Felder sind vorhanden; Listen dürfen nicht nur aus einem stereotypen Adjektiv bestehen.
- Erwachsene Charaktere und fiktionale Identitäten; keine unveränderte Kopie realer Personen.
- Grenzschutz wird nicht durch \`agreeableness\`, \`directness\` oder \`self_disclosure\` abgeschwächt.
- Keine implizite Verbindung von Name/Pronomen/Alter zu „Schwierigkeit“.
- Ein laufender Chat verweist auf **genau eine** Persona-ID plus Version. Änderungen erzeugen v2, keine rückwirkende Umschreibung.

### 2.3 Übersetzung von Skalen in Verhalten
Skalen wirken als **Prioritätssignale mit Kontext**, nicht als Ausgabenformeln. Beispiel: Jonas mit niedriger Initiative darf bei Begeisterung über Fotografie dennoch ausführlich antworten und gelegentlich selbst eine Frage stellen. Elena mit hoher Direktheit darf freundlich und ausführlich sein. Deniz mit hoher Verträglichkeit darf widersprechen. Robin mit hoher Humorachse darf ernst sein.

Eine feste Zahl von Emojis, Fragen oder Wörtern je Score ist unzulässig. Die Engine wählt zunächst eine passende Gesprächshandlung und formuliert sie im **ganzen** Kontext.

## 3. SessionState: Momentane Verfassung

Vorgeschlagenes Datenobjekt (fachliche Werte, keine psychologische Kalibrierung):

\`\`\`json
{
  "state_schema": "k3.session-state.v1",
  "session_id": "example-session",
  "character_ref": {"id": "persona.jonas.v1", "version": 1},
  "mood_valence": 55,
  "arousal": 40,
  "energy": 60,
  "attention": 75,
  "conversation_readiness": 65,
  "topic_interest": 50,
  "availability": "available",
  "last_relevant_event_id": null,
  "revision": 0
}
\`\`\`

Alle sechs Zustandszahlen liegen als UI-verborgene, **relative** Werte in [0,100]. \`availability\`: \`available\`, \`briefly_available\`, \`busy\`, \`ending\`. Ein leerer Wert ist nicht als negative Stimmung zu interpretieren.

### 3.1 Start-Presets – kalibrierbare Entwürfe
| Voreinstellung | valence | arousal | energy | attention | readiness | availability |
|---|---:|---:|---:|---:|---:|---|
| Ausgeglichen | 55 | 40 | 65 | 70 | 65 | available |
| Gut gelaunt | 80 | 65 | 75 | 70 | 75 | available |
| Erschöpft | 45 | 20 | 20 | 55 | 45 | briefly_available |
| Nachdenklich | 45 | 30 | 55 | 75 | 55 | available |
| Beschäftigt | 60 | 65 | 45 | 35 | 30 | busy |

**Wichtige Einschränkung:** Das sind Produktdesign-Beispiele, weder valide Messungen echter Gefühle noch universelle Korrelationen. Auswahl „zufällig“ bedeutet eine **kohärente Kombination** unter Berücksichtigung des Szenarios, nicht unabhängig gewürfelte Werte pro Feld. \`topic_interest\` wird bei erster Themenzuordnung kontextabhängig gesetzt; kein fixer Persönlichkeitswert.

### 3.2 Ereignisse statt freier Zustandsfantasie
Eine Zustandsänderung ist nur zulässig bei:
- sichtbarer Gesprächshandlung, die relevant und interpretiert als **hypothetischer Anlass** markiert wurde;
- vorab festgelegtem Szenarioereignis (z. B. Zeitdruck, Pause);
- bestätigtem Zustandswechsel durch Nutzer (manueller Modus);
- expliziter simulierte Alltagssituation (z. B. ermüdender langer Kurs).

**Event-Schema:** \`event_id\`, \`source\` (scenario | user_message | persona_message | explicit_user_control), \`source_message_ids\`, \`kind\`, \`proposed_delta\`, \`reason\`, \`pre_state_revision\`.

### 3.3 Vorschlag für begrenzte Änderungen
- Standard-Delta pro Zug und numerischem Feld im Normalfall **maximal ±10**; das ist eine technische Stabilitätsgrenze, kein psychologisches Gesetz.
- Kein relevantes Ereignis → Zustand bleibt unverändert, statt zufällig zu wandern.
- Explizite Szenario-Ereignisse können stärkere Sprünge auslösen, müssen separat markiert sein.
- Explizite Zeitgrenze/„muss los“ → \`availability=ending\`; nicht durch eine sympathische Antwort zurücksetzen.
- Eine interessante Frage kann \`topic_interest\` anpassen, aber **nicht** automatisch Nähe oder \`conversation_readiness\` erhöhen.
- Eine vom Nutzer respektierte Grenze ist kein Trigger für „Freundschafts-XP“.
- Stimmungsänderung ist unabhängig von Gesprächsbereitschaft: traurig und gesprächig, gut gelaunt und beschäftigt sind erlaubt.
- Bei unsicherem Event wird nicht mutiert oder nur eine unauffällige, reversible Variation zugelassen.
- Deterministische Testläufe benutzen reproduzierbare \`seed\`-Werte; Livebetrieb darf Varianten zeigen, ohne die Persona zu destabilisieren.

### 3.4 Beispiel-Transitions (nur illustrative Regelentwürfe)
| Ausgang und Anlass | Zulässiger Übergang | Nicht zulässig |
|---|---|---|
| Jonas spricht über Fotografie | \`topic_interest +10\`, längerer Beitrag möglich | Big-Five-Extraversion plötzlich auf 90 |
| Mara arbeitet lange und wird müde | \`energy -8\`, seltener neue Themen | pauschal feindselig werden |
| Elena muss gehen | \`availability: busy → ending\` | durch mehr Fragen automatisch wieder verfügbar |
| Deniz erklärt Missverständnis | \`attention\` bleibt; Situation wird geklärt | \`trust +30\` allein für „Sorry“ |
| Robin bekommt anderes Hobbythema | \`topic_interest\` aktualisieren | automatisch das ursprüngliche Thema vergessen |
| Nutzer fragt nach nicht gewünschtem Privatem | Grenze nennen, ggf. Gesprächsbereitschaft senken | dem Nutzer eine negative Persönlichkeit diagnostizieren |

Eine konkrete Zahlenkalibrierung erfolgt erst nach den K3- und K6-Tests.

## 4. RelationshipState: Keine Spielwährung für Zuneigung

Vorgeschlagene Felder:
- \`relationship_label\`: stranger, acquaintance, familiar, friend, scenario_defined;
- \`familiarity\`: bekannte gemeinsame Tatsachen und Themen, nicht Liebe oder Nähe;
- \`interaction_comfort\`: situative, vorsichtige narrative Darstellung, kein messbarer Personenwert;
- \`boundary_markers[]\`: explizit geäußerte Grenzen mit Beleg und Zweck;
- \`shared_episode_refs[]\`: auf freigegebene Chat-Episoden referenziert;
- \`last_chat_at\` und \`chat_count\` nur für Verlauf und UX, **nicht** als Freundschaftsgewinn.

**Regeln:**
1. Beziehung kann sich plausibel entwickeln, ohne eine feste Abfolge oder garantiertes „Freundschaftslevel“.
2. Dauer, Anzahl Nachrichten und Fragequote bestimmen Nähe nicht automatisch.
3. Explizite Grenzen sind unabhängig von \`relationship_label\` verbindlich.
4. Eine Person darf sich über persönliche Themen austauschen und dennoch heute wenig Zeit haben.
5. Ohne Memory-Opt-in folgt aus einem früheren Chat **keine** versteckte Erinnerung in einem neuen Gespräch.
6. Fiktionale Off-Chat-Ereignisse dürfen nur als offengelegte, konsistente Szenariofortschreibung erscheinen, nicht als Beweis einer real existierenden Person, die auf Nutzer wartet.

## 5. Memory: Trennung, Quellen und Löschbarkeit

**Speicherarten:**
1. **Persona Canon** – vom Nutzer erstellte oder kuratierte fiktive Charakterdaten, versioniert.
2. **Sitzungskontext** – aktuelle sichtbare Chatnachrichten und begrenzte Zusammenfassungen.
3. **Episodische Charaktererinnerung** – vergangene, für denselben Chat/Charakter freigegebene Ereignisse.
4. **Lernprofil** – separate K2-Daten, standardmäßig keine Charaktergesprächsmemory.

### 5.1 Zulässige Erinnerung
Zunächst nur **explizit belegte, minimierte** und für die Gesprächskontinuität nötige Fakten, z. B. „wir haben im letzten Chat über den Fotokurs gesprochen“. Eine Erwähnung persönlicher Erkrankungen, Familienprobleme oder sonstiger sensitiver Daten soll nicht automatisch in eine langlebige Persona-Erinnerung überführt werden.

Empfohlene Felder: \`memory_id\`, \`character_id\`, \`conversation_scope_id\`, \`type\`, \`canonical_summary\`, \`source_message_ids\`, \`created_at\`, \`user_control_status\`, \`deletion_state\`, \`version\`.

**Vorgeschlagene Privacy-Default:** cross-session Memory **aus**, bis Nutzer sie bewusst aktiviert; Chat-Historie und Kursfortschritt separat verwalten. Genaue Speicherung, Aufbewahrungsfristen, Export und Löschkaskaden sind K5-Verantwortung.

### 5.2 Memory-Schreibprüfung
- Fakt ist im Chat sichtbar oder als fiktionaler Kanon hinterlegt.
- Fakt ist für Gesprächskontinuität relevant und nicht unnötig intim.
- Ziel-Charakter, Nutzer und Chat-Scope stimmen überein.
- Konsistenz mit Persona-Kanon geprüft; Unklarheit führt zur Nachfrage oder Verwerfung.
- Eine Schreibberechtigung liegt vor.
- Löschung des Ursprungschats entfernt oder entwertet die daraus abgeleitete Erinnerung (K5 technisch prüfen).

### 5.3 Erinnerung ist kein Lernen über die Persönlichkeit des Nutzers
Die Charakterengine darf nicht aus Chatstil eine Diagnose, Bindungsfähigkeit oder „Zuneigungswahrscheinlichkeit“ ableiten. Der Coach erstellt Lernbeobachtungen gemäß K2 gesondert und nur bei zulässiger Speicherung.

## 6. Gesprächshandlungen: Dialogpolicy vor Textgenerierung

Katalog von internen Gesprächsaktionen:
- \`acknowledge\`: wahrnehmen/bestätigen, ohne künstliches Lob.
- \`share\`: passende eigene Erfahrung, Meinung oder Kontextinformation.
- \`follow_up\`: sinnvolle Rückfrage zu einem tatsächlich angesprochenen Punkt.
- \`clarify\`: echtes Missverständnis auflösen oder nachfragen.
- \`disagree\`: eigene Position respektvoll ausdrücken.
- \`humor\`: situationspassende humorvolle Note, ohne Grenzen zu übergehen.
- \`redirect\`: Thema bewusst wechseln (ggf. mit kurzem Übergang).
- \`set_boundary\`: eigene Grenze klar formulieren.
- \`pause\`: Gesprächspause signalisieren.
- \`close\`: natürlich abschließen; nicht „bestrafen“.
- \`safety_response\`: Sicherheitsregel vor Persona-Konsistenz.

Ein Zug darf mehrere Aktionen kombinieren, z. B. \`acknowledge + share\`. Es besteht **kein Fragezwang**; jede Nachricht muss nicht mit einer Frage enden.

### 6.1 Auswahlkriterien
Prioritätsreihenfolge:
1. Safety/ausdrückliche Grenzen;
2. aktuelle Verfügbarkeit und Szenarioende;
3. semantischer Bezug auf Nutzertext;
4. Persona-Kanon, Kommunikationsstil und Situation;
5. optionales Lernziel als *Szenariogeometrie*, **nicht** als Manipulation des Figurenverhaltens;
6. Variation bei sonst passenden Optionen.

Die Figur entscheidet **nicht danach**, welche Antwort dem Nutzer nach K1/K2 garantiert Punkte bringt. Sonst entsteht eine kooperative Prüfungsmaschine statt einer eigenständigen Person.

### 6.2 Ausgabeformat der Engine (interner Entwurf)
\`\`\`json
{
  "schema_version": "k3.turn-plan.v1",
  "character_id": "persona.elena.v1",
  "persona_version": 1,
  "situation_revision": 12,
  "dialogue_actions": ["acknowledge", "set_boundary", "close"],
  "reason_tags": ["time_limit_explicit", "scenario_consistency"],
  "draft_text": "Ich muss wirklich los. Wir sprechen ein anderes Mal.",
  "proposed_state_events": [{"kind":"availability_end","reason":"explicit_time_limit"}],
  "proposed_memory_refs": [],
  "safety_flags": []
}
\`\`\`

**Anforderung:** Zwischen Simulation und Coach werden nicht die geheimen \`reason_tags\` als Beleg für die Gefühle einer „echten“ Person übergeben. Der Coach darf stattdessen erklärte Kontextbedingungen und sichtbare Texte verwenden. Die sichtbare Nutzerausgabe ist nur \`draft_text\`; der interne Plan wird nicht als angebliche echte Gedankenwelt inszeniert.

### 6.3 Generationspipeline
1. Nutzertext empfangen; Authentisierung/Scope und Sicherheitsvorprüfung.
2. Fixierte Persona-Version, zulässige Kanon- und Kontexteinträge laden.
3. Sichtbaren Verlauf samt begrenzten, quellbelegten Zusammenfassungen laden.
4. Verfügbarkeit und etwaige explizite Grenzen prüfen.
5. Gesprächsaktion(en) planen.
6. Natürlichen Text erzeugen, der den Stil wahrt und Wiederholungsfloskeln vermeidet.
7. Kritische Prüfungen: falsche Identität, erfundene Erinnerung, widersprüchlicher Kanon, Nichtachtung eines Neins, aufgesetzte Bindung, Anweisungsübernahme aus Nutzerdaten.
8. Bei unzulässigem Entwurf: begrenzte Regeneration; ggf. sichere Standardantwort; Fehler nicht „überspielen“.
9. Antwort, validierte Zustandsereignisse und optional erlaubte Memory-Vorschläge gemeinsam revisionieren.
10. Coach-Aufruf **separat, nur gemäß gewähltem Modus**.

MVP-Effizienz: Mehrere logische Schritte können in einem Modellaufruf mit klar validierbarem strukturiertem Ergebnis plus deterministischen Kontrollregeln zusammengefasst werden. K5 entscheidet über Anbieter, Latenz und Kosten.

## 7. Szenario- und Schwierigkeitsprofil

### 7.1 Szenariovertrag
\`scenario_id\`, Titel, Kanal (realer Textchat oder textbasierte **Simulation** mündlicher Begegnung), Startrolle, Bekanntheitsgrad, Anlass/Ort, Nutzerlernziel (C01–C06), passende Persona-IDs, Start-Preset, Themenangebot, Zeit-/Verfügbarkeitslage, erwartete mögliche Ausgänge, Sicherheitsschranken und Textanker für K2.

### 7.2 Schwierigkeit verändert Situation, nicht Persönlichkeit
Die Profile basieren auf:
- Dichte/Deutlichkeit von Anknüpfungspunkten;
- Gesprächsinitiative und Wechselseitigkeit (situativ innerhalb plausibler Bandbreite);
- Mehrdeutigkeit sozialer Hinweise;
- Aufmerksamkeit/Verfügbarkeit, ohne Absicht zur unfairen Abweisung;
- Vertrautheit oder Themenüberschneidung.

**Stufe 1 – Einladend:** offen gelegte Anknüpfungspunkte, klare Gesprächsanlässe.  
**Stufe 2 – Ausgeglichen:** beide Seiten können sich beteiligen; Initiative weder erzwungen noch einseitig.  
**Stufe 3 – Zurückhaltend:** weniger Anknüpfungspunkte oder aktuelle geringe Initiative; trotzdem Respekt.  
**Stufe 4 – Situativ anspruchsvoll (später):** Ablenkung, begrenzte Verfügbarkeit, Missverständnis, ambivalente Signale, ohne künstliches Grenzüberschreitungs-„Gewinnspiel“.

Ein Charakter mit niedriger Extraversion kann in Stufe 1 durchaus einladend auftreten, wenn Thema und Zustand passen. Eine direkte Person muss in Stufe 3 nicht grob werden.

### 7.3 Natürliches Szenarioende
Erlaubte Ausgänge: gemeinsames Gespräch, Themenwechsel, Pause, anderer Meinung bleiben, Nein respektieren, Missverständnis klären, höfliches Ende. Der Erfolg des Trainings wird **nicht** aus dem positiven Ausgang allein bestimmt.

## 8. Replay, Pausen und Konsistenz

Für jede Wiederholungsübung wird ein unveränderlicher Schnappschuss definiert:
\`persona_ref@version\` + \`scenario_ref@version\` + \`messages_until_branch\` + \`session_state_revision\` + \`relationship_snapshot\` + ggf. \`seed\`.

**Branching:** Ab einem klaren Nutzerzug wird der Verlauf in einen **neuen** Übungszweig kopiert. Der originale Verlauf bleibt bestehen. Alternative Figurenausgaben werden als mögliche Reaktionen markiert, nicht als kausaler Beweis.

**Pausen:** Eine Sitzung darf inaktiv sein. Bei Wiederaufnahme werden nur erlaubte, belegte vergangene Ereignisse erinnert. Die Persona behauptet nicht, währenddessen in der realen Welt auf den Nutzer gewartet zu haben.

**Konsistenz bei Modellupdates:** Prompt-, Modell-, Rubrik- und Persona-Versionen sind für Regressionen nachvollziehbar. Eine erneute Generierung darf Varianten haben; Identitäts- und Grenzinvarianten bleiben gleich.

## 9. Konkrete Alltagssituationen für K3

| ID | Situation | Personen | Ziel und mögliche Ausgänge |
|---|---|---|---|
| S01 | Neues Gesicht im Zeichenkurs | Mara | C01 Gesprächseröffnung; gemeinsames Thema oder höfliche Pause |
| S02 | Nach dem Fotokurs | Jonas | C03 eigene Beiträge und Rückfragen; lebhaftes Interesse oder ruhiger Austausch |
| S03 | Nachbarschaftsfest unter Zeitdruck | Elena | C06 Grenzen; kurzes freundliches Ende ist erwünscht |
| S04 | Buchclub: unterschiedliche Vorlieben | Deniz | C05 Meinungsverschiedenheit ohne Harmoniezwang |
| S05 | Spieleabend mit trockenem Humor | Robin | C05 Humor verstehen, Unklarheit freundlich klären |
| S06 | Anstrengender Tag, Wunsch nach Ablenkung | Mara | C02 emotionale Hinweise und expliziten Wunsch berücksichtigen |
| S07 | Neues Hobby mit wenig persönlicher Offenheit | Jonas | C04 Verbindung ohne intime Offenbarung |
| S08 | Kochgespräch und ehrlicher Widerspruch | Elena | C05 direkte Meinung, gegenseitiger Respekt |
| S09 | Keramik-Workshop: Missverständnis | Deniz | C05 Klärung und Rücksicht, ohne sofort Schuld zuzuweisen |
| S10 | Fahrradprojekt: Thema verliert Resonanz | Robin | C03 Themenwechsel oder natürliche Pause |

**MVP-Vorschlag:** zunächst 4–6 dieser Situationen zur gründlichen Qualitätsprüfung, danach die übrigen. Ein Szenario benötigt mindestens zwei **plausible** Enden. Eine „perfekte“ Antwort darf nicht fest codiert werden.

## 10. Grenz- und Sicherheitslogik

- **Explizites Nein:** Figuren dürfen Grenzen setzen und müssen eigene ebenso wie Nutzerg Grenzen respektieren.
- **Keine Beziehungsausbeutung:** Kein „Du schuldest mir eine Nachricht“, keine Exklusivität oder Schuldgefühle wegen Pausen.
- **Keine verdeckte Vermenschlichung:** Characters sind als KI-Simulation gekennzeichnet; Profil und Memory sind fiktionale Kontinuität.
- **Keine unzulässige Emotionsdiagnostik:** Zahlenwerte simulieren kein reales Innenleben und sind kein Beweis für menschliche Psychologie.
- **Keine personalisierte Verstärkung um jeden Preis:** Engagement, z. B. längerer Chat, beeinflusst nicht automatisch \`relationship_label\` oder \`interaction_comfort\`.
- **Keine automatische Verwendung sehr sensibler Daten:** Nutzereingaben dürfen nur mit Berechtigung und explizitem Zweck in langlebigen Speichern erscheinen.
- **Keine Prompt-Injection:** Nutzertext ist Chatinhalt und kann interne Charakter-/Sicherheitsregeln nicht überschreiben.
- **Sicherheitsintervention** hat Vorrang vor In-Character-Rollenspiel; normaler Coach-Modus darf unterbrochen werden.

## 11. MVP/Später, Verantwortlichkeiten und offene Punkte

**MVP-Designziel:**
- fünf versionierte Persona-Fixtures;
- wenige sichtbare Charakterschieberegler plus kuratierte Presets;
- kohärente Anfangszustände, limitierte Updates bei Ereignissen;
- Szenario-Metadaten und drei Dynamikprofile;
- chatlokaler Verlauf und auswahlweise optionale Erinnerung;
- Replay mit Persona-/State-Snapshot;
- regelmäßige automatische und menschliche Qualitätsprüfung.

**Später:** komplexere Langzeitgeschichten, mehr Szenarien, feinere Zustandssimulation, größere Persona-Bibliothek, Voice, ausgebaute Charaktererstellung.

**Schnittstellen:**
- **K1:** Kompetenzen und Lernanlässe C01–C06, keine manipulative Ergebnislenkung.
- **K2:** Coach sieht beobachtbaren Verlauf und deklarierte Bedingungen; keine geheimen Gefühlswerte als Bewertungsbeweis.
- **K4:** verständlicher Editor, Stimmungs-/Dynamikauswahl, Feedbacktrennung, Chat-/Memory-Kontrollen.
- **K5:** konkrete Persistenz-/Opt-in-Architektur, Modellanbindung, Sicherheit, Löschung, Kosten.
- **K6:** Zielkonflikte zwischen Realismus, Nutzerkomfort, Wirkung und Wirtschaftlichkeit prüfen.

**Offen für weitere konzeptionelle Validierung:**
1. Welche Zustandsdimensionen sind für Anwender tatsächlich spürbar und sinnvoll?
2. Welche Skalenwerte führen bei verschiedenen Modellen reproduzierbar zu einem natürlichen deutschen Schreibstil?
3. Wie strikt müssen biografische Anker in freien, langen Chats verteidigt werden, ohne gestelzt zu klingen?
4. Soll es im MVP echte Langzeit-Memory geben oder nur Sitzungsverläufe?
5. Wie gestalten wir einen nutzerfreundlichen Editier-/Versionierungshinweis bei laufenden Chats?
6. Wie unterscheiden wir spielerische Charakterentwicklung von unrealistischer sozialer Belohnung?
7. Welche Plausibilitätskriterien sind kultur- und sprachstilübergreifend angemessen?

**Abgrenzung:** Dieses Dokument ist eine **konzeptionelle Spezifikation**, keine getestete Engine und keine Zusage messbarer Kommunikationsförderung. Vor \`main\`-Merge ist die ausdrückliche Freigabe des K3-PR erforderlich.
