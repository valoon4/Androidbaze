# Kommunikations-Trainer – Produktkonzept

**Status:** Konzeptstand 0.5 – K2 Kommunikationscoach und Feedbacksystem zur Prüfung im separaten PR (06.11.2026); K1 durch PR #47 auf main übernommen  
**Produktname:** Arbeitstitel „CommunicationTrainer“, noch nicht festgelegt  
**Zielplattform:** zunächst Android geplant; technische Umsetzung noch nicht festgelegt  
**Produktphase:** Konzeption, noch keine App-Implementierung  
**Geltungsbereich:** ausschließlich `apps/CommunicationTrainer/` im Repository Androidbaze

> **Leitidee:** Eine lebendige KI-Chatwelt, in der Menschen durch glaubwürdige Alltagsgespräche, reflektiertes Feedback und Wiederholung ihre Kommunikationsmöglichkeiten erweitern.

## 1. Herkunft, Ziel und Abgrenzung

Das Produkt geht aus der Idee hervor, Menschen in einem sicheren, freiwilligen Trainingsraum Kontaktaufnahme, aktives Zuhören, aufmerksames Antworten, eigenes Einbringen und den Umgang mit unterschiedlichen Gesprächspartnern üben zu lassen.

**Strategische Entscheidung (getroffen):** Der erste Fokus liegt auf **Alltagskommunikation und sozialen Kontakten**. Berufsspezifisches Training, Konfliktmoderation, Therapie- bzw. Diagnostikversprechen und Sprachtraining sind für die erste Version nicht der Kern.

Die App soll:
- Menschen helfen, neue Kontakte aufzubauen und sich im Gespräch sicherer und flexibler zu fühlen;
- Übung durch freies Ausprobieren, Wiederholung und verständliche Rückmeldungen fördern;
- differenzierte Gesprächspartner mit eigenem Charakter und situativ stimmigem Verhalten simulieren;
- eigenes Kommunikationsverhalten anhand konkreter beobachtbarer Chatstellen reflektieren;
- Spaß und persönliche Entwicklung verbinden, ohne „perfekte“ Nachrichten als alleinigen Erfolg darzustellen;
- freiwillige Anwendung im realen Alltag anregen.

**Nicht das Ziel:** Menschen zu manipulieren, Kontakt gegen den Willen des Gegenübers zu erzwingen, Introversion als Defizit zu behandeln oder objektive „Sozialkompetenz-Scores“ bzw. psychologische Diagnosen zu behaupten.

## 2. Zielgruppe

Angesprochen sind erwachsene Personen, die ihre alltäglichen Kommunikationsfähigkeiten verbessern möchten. Drei besonders relevante Motive:
1. **Neugierige:** möchten Varianten ausprobieren und interessante Gesprächsdynamiken erleben.
2. **Zurückhaltende:** möchten Kontaktaufnahme und Gesprächsinitiative mit geringem sozialem Risiko üben.
3. **Weiterentwickler:** möchten feinfühliger zuhören, mehr von sich zeigen und situationsangemessener reagieren.

Die App soll unterschiedliche kommunikative Stile respektieren. Ziel ist die Erweiterung der eigenen Möglichkeiten, nicht die Vereinheitlichung von Persönlichkeiten.

## 3. Produktprinzipien

1. **Menschlichkeit vor Perfektion.** Es gibt meist mehrere passende Antworten.
2. **Gegenseitigkeit vor Gesprächslänge.** Grenzen, Desinteresse und Ablehnung sind legitime Ergebnisse.
3. **Lernfortschritt vor Benotung.** Rückmeldung bezieht sich auf Verhalten und Kontext, nicht pauschal auf Persönlichkeit.
4. **Spielerisch, nicht prüfungsartig.** Freies Chatten bleibt ohne verpflichtende Auswertung möglich.
5. **Glaubwürdigkeit vor Gefälligkeit.** Charaktere sind nicht automatisch immer freundlich, zustimmend oder gesprächig.
6. **Transfer in den Alltag.** Freiwillige kleine reale Übungen ergänzen simulierte Chats.
7. **Datensparsamkeit und Transparenz.** KI-Rollen, Speicherung und Analyse müssen verständlich sein.

## 4. Kernmodi

### 4.1 Freier Chat
Charakter auswählen, Gespräch beginnen, ohne festes Ziel oder Zeitlimit. Chatverläufe und ausgewählte Charaktere sind in einer Übersicht erreichbar. Coaching und Reflexion sind freiwillig.

### 4.2 Szenario-Training
Kurze, alltagsnahe Situationen mit gezielt trainierter Fähigkeit, geeignetem Charakter und passender Dynamik. Häufig reichen etwa fünf Minuten; konkrete Dauer ist zu testen.

### 4.3 Verbesserungsrunde / Experimentiermodus
Zu einer Chatstelle zurückkehren, eine alternative Formulierung ausprobieren und plausible unterschiedliche Folgegespräche beobachten. Keine „magische“ Musterlösung; gleichbleibende Ausgangsbedingungen erleichtern den Vergleich.

**Wichtig:** Die Modi sind verbunden. Aus einem freien Chat kann eine freiwillige Übung entstehen; nach einer Übung kann die freie Unterhaltung fortgesetzt werden.

## 5. Kompetenzatlas

Sechs erste Kompetenzbereiche:
1. **Kontakt aufnehmen:** angemessen ansprechen, Begrüßungen, natürliche Gesprächsöffner, erste Nachrichten.
2. **Aufmerksam sein:** Aussagen aufgreifen, sinnvoll nachfragen, emotionale Hinweise wahrnehmen, aktives Zuhören.
3. **Gespräche gestalten:** eigene Gedanken und Erlebnisse beitragen, Themen vertiefen und wechseln, Gegenseitigkeit.
4. **Verbindung aufbauen:** Gemeinsamkeiten entdecken, Wertschätzung, authentisches persönliches Teilen, angemessenes Vertrauen.
5. **Flexibel reagieren:** mit knappen Antworten, anderen Ansichten, Missverständnissen und Unerwartetem umgehen.
6. **Grenzen respektieren:** Desinteresse erkennen, ein Nein akzeptieren, Gespräche respektvoll beenden, eigene Grenzen benennen.

**Beobachtbarkeit:** Kompetenzsignale sind kontextabhängige Verhaltensbeobachtungen. Es wird kein diagnostisch valider Gesamtwert behauptet. Ein einzelner Chat begründet keine stabilen Persönlichkeitsurteile.

## 6. KI-Charaktere

### 6.1 Fünfschichtiges Modell
1. **Stabile Persönlichkeit:** Werte, Interessen, Humor, Temperament, biografischer Hintergrund; Big Five können interne Grundlage sein.
2. **Kommunikationsstil:** Initiative, Direktheit, Kürze/Länge, Rückfragen, Selbstoffenbarung, Humor.
3. **Aktueller Zustand:** Laune, Energie, Ablenkung, aktuelle Gesprächsbereitschaft; manuell oder zufällig wählbar.
4. **Beziehungskontext:** Fremde, Bekanntschaft, Kollegenkontakt, Freundschaft oder vertraute Person; gemeinsame Erlebnisse.
5. **Situation:** Anlass, Ort bzw. Kommunikationskanal, gegenwärtiges Anliegen, begrenzte Verfügbarkeit.

**Trennung:** Niedrige Extraversion bedeutet nicht automatisch wortkarg oder uninteressiert. Kommunikationsinitiative und Situation werden eigenständig modelliert.

### 6.2 Editor und Einstiegsfreundlichkeit
- **Einsteiger:** aus klar beschriebenen, fertigen Charakteren wählen oder wenige Alltagseigenschaften anpassen.
- **Fortgeschrittene:** eigenen Charakter erstellen, Persönlichkeit und Kommunikationsstil über verständliche Schieberegler konfigurieren.
- Big Five sind als primäre wissenschaftlich anschlussfähige Persönlichkeitsbasis vorgesehen; optionale MBTI-orientierte Darstellung ist offen und vor Umsetzung wissenschaftlich sowie rechtlich zu prüfen.
- Stimmung darf gewählt oder zufällig bestimmt werden. Die Auswirkungen sollen glaubwürdig und nachvollziehbar sein.

### 6.3 Beispielcharaktere (illustrativ)
- **Mara:** neugierig, humorvoll, gesprächsfreudig; bei Erschöpfung zurückhaltender.
- **Jonas:** nachdenklich, aufmerksam, zunächst vorsichtig; bei vertrauten Themen ausführlich.
- **Elena:** direkt, selbstbewusst und warmherzig; widerspricht gegebenenfalls sachlich.

### 6.4 Qualitätsanforderungen
Charaktere müssen eigenständige Interessen, Grenzen und Gedächtniskonsistenz zeigen; sie dürfen Meinungen widersprechen, Themen wechseln, abgelenkt sein oder Unterhaltungen höflich beenden. Sie dürfen nicht ununterbrochen zustimmen oder jede Nachricht mit übermäßigem Lob und Fragen belohnen. Zugleich keine künstlich provozierte Feindseligkeit zum Erhöhen des Schwierigkeitsgrads.

## 7. Schwierigkeit als Gesprächsdynamik

Nicht Persönlichkeitstypen in „leicht/schwer“ einordnen. Schwierigkeit beschreibt, was das Gespräch vom Nutzer verlangt:
- **Stufe 1 – Einladend:** viele Anknüpfungspunkte, proaktive Beteiligung.
- **Stufe 2 – Ausgeglichen:** beide Seiten tragen zur Unterhaltung bei.
- **Stufe 3 – Zurückhaltend:** weniger Eigeninitiative, aber glaubwürdiges Interesse.
- **Später: Stufe 4 – Situativ anspruchsvoll:** Ablenkung, Müdigkeit, Missverständnisse, ungleiche Themeninteressen.

Ein gutes Ergebnis kann auch sein, den Wunsch nach Gesprächsende zu respektieren. Adaptiver Schwierigkeitsgrad ist später denkbar, muss aber erklärbar und übersteuerbar bleiben.

## 8. Szenarienbibliothek

Vier erste Themenfelder:
- **Neue Kontakte:** Kurs, Hobby, Nachbarschaft, neue Bekanntschaft anschreiben.
- **Gespräche lebendig halten:** eigene Erfahrungen teilen, kurze Antworten, Themenwechsel.
- **Freundschaften entwickeln:** Gemeinsamkeiten, persönliche Erzählungen, Kontaktpflege.
- **Unerwartete Situationen:** Missverständnisse, verschiedene Ansichten, Desinteresse, unpassende Witze.

Jedes Szenario enthält: kurzen Kontext, Charakter, aktuelle Verfassung, Beziehung, primäre Kompetenz, zulässige Gesprächsausgänge, Bewertungsanker und mögliche Reflexionsfragen.

## 9. Kommunikationscoach und Feedback

### 9.1 Feedbackstufen
- **Dezentes Feedback nach dem Chat:** eine konkrete Stärke und ein sinnvoller Verbesserungsvorschlag.
- **Ausführliche Analyse auf Wunsch:** ausgewählte Chatstellen, mögliche Interpretationen, alternative Ansätze.
- **Freiwilliger Live-Tipp:** Hinweis während einer Übung, der nicht automatisch die ganze Antwort vorformuliert.

### 9.2 Verbindliche Feedbackregeln
- Beobachtung und Interpretation sprachlich trennen.
- Immer konkrete Textstellen und Situation berücksichtigen.
- Absichten oder Gefühle eines KI-Charakters nicht als sicher „ausgelesene“ Tatsachen verkaufen.
- Mehrere angemessene Antworten zulassen; keine pauschale Idealformulierung.
- Rückfragen sind nicht automatisch besser als eigene Beiträge, lange Nachrichten nicht automatisch besser als kurze.
- Unklare Beobachtungen mit Unsicherheit kennzeichnen und keine stabilen Diagnosen ableiten.
- Kritik konkret, respektvoll, lernorientiert und nicht beschämend formulieren.
- Nutzern erlauben, unpassende Rückmeldungen zu melden.

Für die Evaluation sollte es eine Sammlung menschlich geprüfter Testdialoge geben. Gesprächssimulation und Bewertung werden logisch getrennt.

## 10. Lernkreislauf und Langzeitbeobachtung

**Ziel wählen → kurze Gesprächsübung → Feedback → freiwillige Wiederholung → optionaler Alltagstransfer.**

Langfristige Muster werden nur auf Basis mehrerer geeigneter Beispiele erkannt, möglichst über vergleichbare Szenarien hinweg. Beispiele: Verhältnis von Fragen und eigenen Beiträgen, Reaktion auf emotionale Hinweise, Umgang mit knappen Antworten. Darstellung als vorsichtige Beobachtung statt Charakterurteil. Lernprofile und Erinnerungen bleiben kontrollierbar.

**Kleine Schritte im Alltag:** freiwillige reale Anregungen, z. B. eine eigene Erfahrung passend in ein Gespräch einbringen, mit optionaler Selbstreflexion; keine Leistungspflicht.

## 11. Motivation und Gamification

Zwei getrennte Fortschrittsebenen:
- **Spielerisch:** XP, Level, freigeschaltete Szenarien, Entdeckungen und Erfolge für Übung, Reflexion, Wiederholung und Konstanz.
- **Lernorientiert:** beobachtete Fähigkeiten, konkrete Entwicklung in vergleichbaren Übungen, selbst gewählte Ziele.

Keine XP für erzwungene Gesprächslänge, „Überreden“ des Gegenübers oder vermeintliches Gewinnen von Zuneigung. Keine aggressive tägliche Streak-Pflicht. Punktwerte erst nach Nutzertests balancieren.

## 12. UX und Informationsarchitektur

Vier Hauptbereiche:
1. **Chats:** offene Gespräche, gespeicherte KI-Charaktere, neues Gespräch, Charakterverwaltung.
2. **Entdecken & Üben:** Szenarien, geführte Übungen, kurze Lernartikel, Herausforderungen.
3. **Fortschritt:** XP, Lernbereiche, vorsichtige Musterbeobachtungen, Reflexionen.
4. **Profil:** Ziele, Einstellungen, Datenkontrolle, Datenschutz.

**Erster Start:** Trainingswunsch auswählen → fertigen Charakter kennenlernen → direkt chatten → kurze optionale Rückmeldung. Kein obligatorischer komplexer Charaktereditor oder langer Fragebogen.

## 13. Konzeptionelle Architektur

Logisch getrennte Verantwortlichkeiten, die technisch nicht zwingend getrennte KI-Modelle erfordern:
- **Charakter-Engine:** konsistente Persona und Chatantworten.
- **Situations-Engine:** Kontext, Stimmung, Gesprächszustand, Dynamik.
- **Coach-Engine:** nachvollziehbare kommunikative Analyse.
- **Lernprofil:** freigegebene Erkenntnisse, Ziele, Übungsvorschläge.
- **Fortschritts-Engine:** Übungsstatus, XP, Level.
- **Sicherheits-/Datenschicht:** Inhaltssicherheit, Zugriff, Speicherung, Löschung.

Für Charakterzustände sind strukturierte Daten statt ausschließlich unkontrollierter Chatprompts vorgesehen. Für Vergleichsübungen werden Charakter und Ausgangslage möglichst konstant gehalten.

## 14. Datenschutz, Sicherheit und Grenzen

- Deutlich machen, dass mit KI und nicht mit Menschen gechattet wird.
- Chats, Charaktergedächtnis und Lernanalyse getrennt und verständlich kontrollierbar gestalten.
- Datensparsamkeit, Löschung und Export früh berücksichtigen, insbesondere DSGVO-Anforderungen bei EU-Nutzern.
- Kein Versprechen psychologischer Diagnose, Therapie oder messbarer persönlicher Defizite.
- Mögliche übermäßige Bindung an KI-Charaktere berücksichtigen; reale Kontakte nicht systematisch verdrängen.
- Textchats trainieren zunächst primär schriftliche Kommunikationsentscheidungen; mündliche Fertigkeiten sind nur begrenzt abbildbar.

## 15. MVP-Umfang (Vorschlag, vor Umsetzung zu validieren)

**Enthalten:**
- 4–5 überzeugende, unterschiedliche Charaktere;
- frei zugängliche Textchats mit Chatübersicht und gespeicherten Verläufen;
- 8–12 ausgewählte Alltagsszenarien;
- 3 Schwierigkeitsstufen als Gesprächsdynamik;
- einfache Stimmungsvoreinstellungen inkl. Zufallswahl;
- einfacher Charaktereditor;
- kurzes Feedback und optionaler vertiefter Coach;
- eine Nachricht erneut versuchen und Folgen vergleichen;
- XP, Übungsfortschritt, erste vorsichtige Kompetenzbeobachtungen;
- ein gleichzeitig aktives persönliches Lernziel.

**Zurückgestellt:** komplexer Big-Five-/MBTI-Editor, Gruppenchats, Sprachanrufe, aufwendige adaptive Beziehungssimulation, umfangreiche Diagnostik-/Benchmark-Dashboards, berufsbezogene Spezialtrainings.

## 16. Geplanter Entwicklungsfahrplan

- **M1 – Lernfundament:** Kompetenzen, Beobachtungskriterien, Feedbackstandards, Szenariospezifikation.
- **M2 – KI-Charaktere:** Charaktermodell, Zustandslogik, Konsistenztests, Dynamiken.
- **M3 – Chat-Prototyp:** Android-UX, Charakterauswahl, Chats, Szenariostart und Verlauf.
- **M4 – Kommunikationscoach:** situatives Feedback, Verbesserungsrunde, Lernziel und Musterbeobachtungen.
- **M5 – Qualität & Motivation:** XP und Level, Usability, Feedbackvalidierung, Pilot-Test und Alltagstransfer.

## 17. Evaluation und Risiken

Drei Qualitätsachsen: **Glaubwürdigkeit der Charaktere**, **Nützlichkeit/Korrektheit des Feedbacks**, **Lernwirkung/Alltagstransfer**. Früh kleine qualitative Nutzertests, später größere kontrolliertere Tests. Erfolgsmetriken gehen über Chatdauer und Anzahl Nachrichten hinaus.

Offene Risiken:
- Persona-Drift und Überfreundlichkeit bei langen Chats;
- Bewertungsfehler, Scheingenauigkeit, ungerechte Punktvergabe;
- Gewöhnung an KI-Gespräche statt Übertragbarkeit auf reale Kontakte;
- laufende KI-Kosten und finanzierbare Nutzung;
- Datenschutz, Einwilligung, Sicherheit und zukünftige regulatorische Anforderungen.

## 18. Konkretisierung der gemeinsam befürworteten Verbesserungen (0.2)

Dieser Abschnitt hält die in den Konzeptgesprächen erläuterten Details fest. Die **produktstrategische Richtung ist befürwortet**; konkrete Parametrierung und technische Umsetzung sind nicht automatisch abgenommen.

### 18.1 Charaktereditor und individuelles Erleben
- Einsteiger wählen sofort aus klaren Charakterkarten und wenigen leicht verständlichen Eigenschaften (z. B. „gesprächig“, „nachdenklich“, „neugierig“). Charakterkarten dürfen Beispiele für Interessen und typische Gesprächsdynamik zeigen.
- Der fortgeschrittene Editor bietet Slider für u. a. Extraversion, Offenheit, Kooperation/Verträglichkeit, emotionale Stabilität, Direktheit, Humor, Gesprächsinitiative, Antwortlänge, Vertrauen/Nähewunsch und aktuelle Energie bzw. Gesprächsinteresse. **Nicht alle Regler sind Persönlichkeitsdiagnosen:** Big-Five-Eigenschaften, Kommunikationsparameter und situative Zustände werden klar getrennt.
- Einstellbare Charaktermerkmale umfassen auch thematische Interessen, Hintergrund, Werte, bevorzugte Kontaktintensität und persönliche Grenzen.
- Für Charaktere gibt es Speichern, Wiederverwenden und Bearbeiten. Über gespeicherte Chats ist nachvollziehbar, welche Persona-Konfiguration zu welchem Chat gehört; spätere Änderungen dürfen den laufenden Verlauf nicht widersprüchlich umschreiben.
- **Big Five vor MBTI als Modellbasis.** Ein optionales MBTI-inspiriertes Nutzerinterface wird erst nach Klärung des Nutzens, wissenschaftlicher Einschränkungen und Rechten angeboten. Kein automatischer Rückschluss von einer Typologie auf einzelnen Gesprächserfolg.
- Stimmungen und Gesprächsbereitschaft werden explizit, zufällig oder über ein Szenario festgelegt. Ein zufälliger Zustand ist keine willkürliche Verhaltensänderung im laufenden Gespräch.
- Beziehungskontext (fremd/bekannt/befreundet/vertraut) mit gemeinsamer, begrenzter Erinnerung; Nähe entsteht situativ und mit Gegenseitigkeit, nicht automatisch durch Nachrichtenzahl.

### 18.2 Realismus und Charakterzustand
- Zur Persona gehören wiedererkennbare Interessen, eigene Gesprächsziele, Grenzen und gelegentlich Initiative zu neuen Themen.
- Verhalten muss kohärent sein: Ein ruhiger Charakter darf bei Lieblingsthemen ausführlich werden; eine gesprächige Person kann bei Müdigkeit kürzer reagieren.
- Der Zustand (Energie, aktuelle Aufmerksamkeit, Thema, Gesprächsinteresse, Beziehung) wird strukturiert und nach begrenzten, testbaren Regeln aktualisiert. Keine abrupten Charakterwechsel ohne Anlass.
- **Erlaubte natürliche Gesprächsausgänge:** angenehme Verbindung, schlichtes Auslaufen, höflicher Abbruch, inhaltliche Uneinigkeit, Missverständnis oder freiwilliger Themenwechsel. Nicht jedes Szenario muss „gewonnen“ werden.
- Ein Charakter soll auf Grenzen und Desinteresse reagieren, ohne Nutzer künstlich zu beschämen oder durch künstliche Abweisung zu prüfen.
- Testfälle umfassen lang andauernde Chats, Wiederaufnahme nach Pausen, Widerspruch, missverständliche Nachrichten, wechselnde Stimmung und ungeschickte Gesprächsführung.

### 18.3 Beobachtbare Kompetenzsignale
Für jede der sechs Kompetenzen sollen konkrete Indikatoren entwickelt werden; als Beispiel:
- **Kontaktaufnahme:** angemessener Bezug zur Situation, einladender aber nicht aufdringlicher Gesprächseinstieg.
- **Zuhören:** Gesagtes aufgreifen und Verständnis prüfen, ohne bloß die Aussage mechanisch zu wiederholen.
- **Gesprächsgestaltung:** Balance aus Fragen, eigenem Beitrag und thematischer Weiterentwicklung.
- **Verbindung:** echtes statt erzwungenes Teilen, situativ angemessene persönliche Tiefe, Respekt.
- **Flexibilität:** mit knappen oder unerwarteten Reaktionen umgehen; eigene Annahmen korrigieren.
- **Grenzen:** ein Nein akzeptieren, höflich abschließen und eigene Bedürfnisse angemessen benennen.

**Bewertung:** keine pauschale „78 % Sozialkompetenz“. Bewertungen sind lokale, kontextuelle Übungsbeobachtungen; Entwicklungen basieren auf mehreren vergleichbaren Beispielen und sind als unsicher gekennzeichnet, wenn Evidenz fehlt.

### 18.4 Verhaltensnaher Kommunikationscoach
- Kurze Standardauswertung: **eine konkrete Stärke und ein konkret nutzbarer Verbesserungsgedanke**, nicht ein überladener Befund.
- Erweiterte Analyse zeigt die relevante Stelle, erkennbare Gesprächsabsicht (wenn plausibel), mögliche Wirkungen, alternative Formulierungen und den jeweiligen Kontext.
- Beispiel-Prinzip: Auf „Heute war es bei der Arbeit echt stressig“ mit unvermitteltem Themenwechsel zu reagieren ist **nicht automatisch falsch**, könnte aber ein übergangenes emotionales Signal sein. Eine Alternative könnte zunächst Anteilnahme zeigen oder fragen, ob Ablenkung erwünscht ist.
- Tipps sollten ein eigenes Ausprobieren unterstützen; eine fertige Antwort ist nur optional. Live-Coach ist freiwillig, im freien Modus nicht aufdringlich.
- Bewertung und Personaantwort werden technisch bzw. logisch voneinander getrennt. Der Coach erklärt Textbelege und nennt keine KI-internen simulierten Gefühle als gesicherte Tatsachen.
- Feedback darf nicht länger = besser, mehr Fragen = besser oder Einverständnis = Erfolg annehmen.
- Nutzer können Feedback als hilfreich, nicht passend oder missverständlich bewerten sowie eigene Interpretation ergänzen. Fehlbewertungen werden in Qualitätsprüfungen berücksichtigt.
- Bibliothek menschlich geprüfter Dialoge mit positiven, ambivalenten und negativen Fällen; regelmäßige Kalibrierung und Regressionstests vor Releases.

### 18.5 Mini-Lektionen, Übungen, Reflexion
- Freiwillige, kurz lesbare Mini-Lektionen erklären z. B. offene vs. suggestive Fragen, Zuhören, angemessene Selbstoffenbarung, Gesprächspausen oder natürliche Themenwechsel. Bei Bedarf folgt direkt ein passendes Szenario.
- Geführte Lernpfade sind optional. Nutzer dürfen jederzeit auch frei chatten und Lerninhalte überspringen.
- Nach einer Übung erfolgt eine kleine optionale Reflexion oder Wiederholungsrunde; kein Feedbackzwang.
- Alltagstransfer mit kleinen, freiwilligen Schritten (z. B. einmal eine eigene Erfahrung beitragen) und späterer Selbstreflexion. Keine Bewertung realer Personen.
- Szenarien können unterschiedlich beginnen und enden; Nutzer sollen üben, Unterschiede statt starre Antwortskripte zu erkennen.

### 18.6 Fortschrittsdarstellung und Gamification
- Übersicht mit getrenntem **Spielerlevel** (Aktivität) und **Lernprofil** (kontextabhängige Beobachtungen).
- Kompetenzpfade können eigene Lernstände und konkrete Übungsziele anzeigen, **nicht** objektive, allgemeinvergleichbare Fähigkeitsgrade.
- XP werden vor allem für abgeschlossene Übung, erneuten Versuch, freiwillige Reflexion und das Ausprobieren neuer Kompetenzen vergeben.
- Beispielwerte als ausschließlich illustratives Balancing: +20 abgeschlossene Kurzübung, +10 Reflexion, +15 alternative Antwort, +20 neue Fähigkeit, +30 persönliche Herausforderung.
- Sammelbare Erfolge würdigen z. B. gutes Eingehen auf ein Gefühl, eigene Beiträge, höfliches Gesprächsende, respektvoll geklärtes Missverständnis.
- Keine XP für Überreden, Zuneigung, künstlich verlängerte Chats, pausenloses Üben oder verpflichtende Streaks.
- Langzeitbeobachtungen müssen in einer rückblickenden, datierten, korrigierbaren Form formuliert werden (z. B. „in sechs vergleichbaren Übungen häufiger eigene Erlebnisse eingebracht“), statt ein dauerhaftes Persönlichkeitsurteil abzugeben.

### 18.7 Einfache UX und Start ohne Hürden
- Vier Bereiche: **Chats**, **Entdecken & Üben**, **Fortschritt**, **Profil**.
- Startfolge: **Was möchtest du üben? → passenden fertigen Charakter wählen → direkt chatten → freiwilliges Kurzfeedback**.
- Im Gespräch: möglichst vertraute Chatoberfläche mit sichtbarer Möglichkeit für Tipps, Pause, Wiederholung und Abschlussreflexion; keine ständige Benotung.
- Chats-Liste zeigt offene und zuletzt genutzte Unterhaltungen; Nutzer können erstellte Charaktere wiederfinden und verwalten.
- Kein langer Pflichtfragebogen. Ein datensparsamer Demo-/Gastmodus vor einer verpflichtenden Registrierung ist anzustreben, soweit sicher und technisch vertretbar.
- Der erweiterte Charaktereditor ist auffindbar, aber niemals Voraussetzung für die erste Übung.
- Lern- und Spielanzeigen sollen motivieren, ohne Nutzer mit Zahlen oder Warnhinweisen zu überfordern; Zugänglichkeit und eindeutige Beschriftung sind Teil der UX-Qualitätsprüfung.

### 18.8 Ergänzende System- und Sicherheitskriterien
- Für Wiederholungsübungen wird der Ausgangskontext festgehalten. Folgeantworten zeigen plausible Varianten, keine angeblich deterministische sozialpsychologische Kausalität.
- Datenschutz by design: möglichst getrennte Speicherung/Einwilligungssteuerung für Chatverlauf, Personaerinnerungen und Lernanalyse; transparente Löschung und Export, klare Aufbewahrungsfristen; konkrete DSGVO-Prüfung vor Release.
- Keine therapeutischen Versprechen, keine psychologischen Diagnosen, keine versteckte menschliche Gegenstelle. Umgang mit emotional belastenden Inhalten und missbräuchlicher Nutzung ist zu planen.
- Eine KI-Bindung darf nicht durch manipulative Erinnerungs-/Benachrichtigungsmechanismen maximiert werden; reale Kontakte bleiben das Ziel.
- Technischer und wirtschaftlicher Rahmen: laufende Modell- und Auswertungskosten messen, kostengünstige Varianten testen, Preis- und Nutzungslimits erst datenbasiert festlegen.

### 18.9 Pilotierung und Messung
- Start mit einem **kleinen qualitativen Pilot** (als Richtwert ca. 15–25 Erwachsene; abhängig von Rekrutierung und Fragestellung), danach größere Tests.
- Charaktertests: Unterschiedlichkeit, Kohärenz, Realismus, Varianz und Umgang mit Zurückweisung, über viele Nachrichten.
- Coach-Tests: belegte Beobachtungen, hilfreiche Vorschläge, angemessene Unsicherheit, falsche Zuschreibungen, Benotungs-Bias und menschliche Übereinstimmung.
- Lerntests: ähnliche Aufgaben vor/nach Training, unabhängige Bewertung wo möglich, Nutzerberichte zum realen Alltagstransfer. Eine gute App-Nutzungsdauer ersetzt keinen Wirksamkeitsnachweis.
- Nutzung: freiwillige Wiederkehr, Szenarioabschluss, Coach-Hilfreichkeit, Wiederholungsmodus, subjektive Überforderung und Datenschutzverständnis.
- Entscheidungen über zusätzliche Komplexität erfolgen anhand von Qualitäts- und Nutzersignalen, nicht nur Featureumfang.

### 18.10 Einordnung von Ideen nach Verbindlichkeit
- **Grundsätzlich akzeptierte Konzept-Richtung:** sämtliche hier beschriebenen Produktverbesserungen sollen als Designprinzipien und mögliche Ausbaustufen erhalten bleiben.
- **Nicht bereits final festgelegt:** exakte Anzahl der Slider, Trainingsszenarien, Charaktere, Pfad-Level, XP, Schwellenwerte, technische Implementierungen, Datenschutzdetails und Pilotmethodik.
- **MVP-Vorrang:** Zu Beginn eine überschaubare, gut getestete Version mit wenigen hervorragenden Charakteren und vertrauenswürdigem Feedback statt einer großen Anzahl unreifer Features.
- **GitHub-Vorgabe:** Selbst ein inhaltlich begrüßter Vorschlag ist keine ausdrückliche Genehmigung eines GitHub-PR oder Merges.

### 18.11 M2: Vertiefte Charakter- und Simulationsspezifikation

Die bisherige Charaktervision ist nun in zwei **separaten, ausführlichen Arbeitsdokumenten** konkretisiert:
- **[CHARAKTER_SIMULATION.md](CHARAKTER_SIMULATION.md):** Charakteridentität, Big-Five-inspirierte Hintergrunddimensionen, eigenständige Kommunikationsachsen, momentaner Zustand, Beziehungskontext, kontrollierbare Erinnerungen, Szenario- und Schwierigkeitsmodelle, Gesprächsregie, Antwortpipeline, Wiederholungsmodus, Charaktereditor, Beispiele und MVP-Etappen M2.1–M2.6.
- **[CHARAKTER_SIMULATION_TESTPLAN.md](CHARAKTER_SIMULATION_TESTPLAN.md):** 24 konkrete Prüffälle, menschliches Bewertungsraster, Sicherheits-/Qualitätsgates, Langzeit- und Regressionstests.

**Konzeptionelle Leitentscheidungen:** Charaktereigenschaften, Kommunikationstendenzen, situative Stimmung, Gesprächsbereitschaft und Beziehung dürfen nicht vermischt werden. Figuren müssen eigene Interessen, Initiativen, Grenzen und realistische Gesprächsenden zeigen. Die Simulationsengine nutzt Versionierung und begrenzte Zustandsänderungen; sie hält Nutzer-Nachrichten von Systemanweisungen und die spätere Coach-Auswertung von der Persona-Simulation getrennt. Individuelle Schieberegler, Schwierigkeitswerte und Memory-Kriterien bedürfen vor Implementierung einer separaten fachlichen Validierung.

**Status:** Die M2-Vertiefung ist eine neue Ausarbeitung zur Prüfung, keine bereits abgenommene Code-Implementierung. Sie ersetzt die grundsätzlichen Produktentscheidungen aus v0.2 nicht, sondern präzisiert sie.

### 18.12 K1: Lern- und Kompetenzsystem (Konzeptabschlussphase)

Auf Basis der bisher vereinbarten sechs Kompetenzfelder wurde das didaktische Fundament für den Kommunikationscoach **detailliert konkretisiert**:
- **[K1_LERN_KOMPETENZSYSTEM.md](K1_LERN_KOMPETENZSYSTEM.md)**: sechs Kompetenzfelder mit je vier Unterkompetenzen; beobachtbare, kontextbezogene Anker; Lernkreislauf; Übungsformen; zwölf Beispiellektionen; Progression und Alltagstransfer; qualitative Bewertungsregeln; gerechte Interpretation unterschiedlicher Kommunikationsstile; fachliche Übergabeschnittstelle an K2.
- **[K1_KOMPETENZ_PRUEFFELLE.md](K1_KOMPETENZ_PRUEFFELLE.md)**: neun ausformulierte Dialogkontraste, 24 Prüffälle, Testregeln zu Mehrdeutigkeit, Grenzen und nicht beurteilbaren Nachrichten.

**Wesentliche Ergänzung:** Ein „gutes Gespräch“ wird nicht aus Länge, Anzahl der Rückfragen, Zustimmung oder Sympathie der simulierten Figur abgeleitet. Der Coach betrachtet die tatsächlich sichtbare Gesprächshandlung, den Anlass und mehrere plausible Deutungen. Mehrere verschiedene Antworten können gut sein; ausdrückliches Nein und Privatsphäre haben Vorrang. Es gibt keine globalen Sozialkompetenzwerte.

**Projektsequenz nach K1:** K2 Coach-/Feedbacksystem → K3 Persona-Konkretisierung (vormals M2.1) → K4 UX → K5 Technik/Datenschutz/Betrieb → K6 Gesamtkonzept-Qualitätsrunde. Die frühere Etappenübersicht M1–M5 bleibt als **Umsetzungsvorschlag nach dem Konzeptabschluss** erhalten; die K-Bezeichnungen sind eine vorgelagerte Konzeptphase.

**Status:** K1 ist zur Prüfung ausgearbeitet, noch keine freigegebene Implementierung oder abschließende Wirksamkeitsvalidierung. Fachliche Messschwellen und Feedbackausgabe werden in K2 und späteren Tests präzisiert.

### 18.13 K2: Kommunikationscoach, Feedback und Qualitätskontrolle

Die aus K1 abgeleiteten Regeln für den Kommunikationscoach sind als **ausführliche, noch zu prüfende Fachspezifikation** dokumentiert:

- **[K2_KOMMUNIKATIONSCOACH.md](K2_KOMMUNIKATIONSCOACH.md)**: freiwillige Coach-Modi, Belegpflicht, Datenzugriff, Trennung von Beobachtung/Interpretation/Unsicherheit/Option, Nicht-Beurteilbarkeit, Feedbackpriorisierung, Lernprofil, Nutzerkorrektur, strukturiertes Ausgabeformat und MVP-Abgrenzung.
- **[K2_COACH_BEISPIELE.md](K2_COACH_BEISPIELE.md)**: zwanzig ausgearbeitete Feedback- und Kontrastbeispiele mit zulässigen und unzulässigen Rückmeldungen.
- **[K2_COACH_QUALITAET.md](K2_COACH_QUALITAET.md)**: 36 Prüffälle, Kontext-Kontrasttests, menschliches Bewertungsraster und kritische Qualitätsgates.

**Kernprinzipien:** Ein guter Coach muss nicht jede Nachricht bewerten. Er erläutert nur belegte Verhaltensweisen, erlaubt verschiedene angemessene Antworten und darf ausdrücklich feststellen, dass **keine Verbesserung nötig** oder **noch keine verlässliche Bewertung möglich** ist. Freie Chats bleiben ohne angeforderten Coach ungestört. Die simulierte Reaktion der Persona ist kein Beweis für die Qualität der Nutzerantwort. Ausdrückliche Grenzen, Privatsphäre und Selbstbestimmung stehen über Gesprächslänge und Engagement.

**Lernprofil:** einzelne Ereignisse bleiben von wiederkehrenden Mustern getrennt. Für eine Pilotierung wird als **nicht validierte, konservative Startbedingung** mindestens drei geeignete unabhängige Beobachtungsgelegenheiten in mindestens zwei unterschiedlichen Kontexten vorgeschlagen. Diese Regel darf nur nach fachlicher Evaluation verwendet werden; bei unzureichender Zuverlässigkeit bleibt automatische Mustererkennung deaktiviert.

**Abhängigkeiten:** K3 wird konkrete Personas und Kontextinformationen liefern, K4 das Coach-Erlebnis in der Oberfläche ausarbeiten, K5 technische Datenverträge und Datenschutz konkretisieren und K6 die Gesamtwirkung prüfen. Es ist noch keine KI-Coach-Funktion implementiert oder validiert.

## 19. Status offener Entscheidungen

**Bereits abgestimmt:** Fokus auf Alltagskommunikation und soziale Kontakte; freier Chat plus Übung und Wiederholung; unterschiedliche KI-Charaktere und Stimmungen; Feedback, Lernbeobachtungen und Spielmotivation; getrennte Projektablage; PR-Freigabe vor Merge.

**Grundsätzlich befürwortete konzeptionelle Vorschläge:** Kompetenzatlas, drei verbundene Modi, mehrschichtige Charaktere, differenzierte Schwierigkeitsdynamiken, lernorientierter Coach, freiwillige Mini-Lektionen, nachvollziehbarer Fortschritt, Alltagstransfer, vierteilige Navigation und stufenweiser MVP. Die konkrete Zahl der Inhalte, exakte Parameter, technische Wahl und Ausgestaltung sind weiterhin vor Implementierung zu prüfen.

**Noch zu entscheiden:** endgültiger App-Name; genauer Onboarding-Ablauf; Datenhaltung und KI-Dienste; Preismodell; konkrete Charakterprofile und Editor-Defaults; Gesprächslänge und Pausen; endgültige Coach-UX und Einwilligungsgestaltung; evidenzbasierte Mess- und Qualitätsschwellen; detaillierte Datenschutz- und Pilotstrategie.

## 20. Weiterführung und Änderungsprozess

Dieses Dokument ist die **maßgebliche konzeptionelle Arbeitsgrundlage** im Projektbereich. Neue Besprechungen sollen hier konsistent eingearbeitet werden; relevante Entscheidungen werden im Änderungsprotokoll dokumentiert. Vorschläge und ausdrücklich beschlossene Festlegungen bleiben unterscheidbar.

GitHub-Arbeitsregel: Für Änderungen eigene Entwicklungsbranches und Pull Requests verwenden. **Kein Merge in `main` ohne vorherige ausdrückliche Genehmigung des Nutzers für den jeweiligen PR.** Andere Apps, Verzeichnisse und die bestehende Root-Build-Konfiguration bleiben unangetastet, solange der Nutzer keine Änderung daran beauftragt.
