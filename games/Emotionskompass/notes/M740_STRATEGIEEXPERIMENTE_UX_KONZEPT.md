# M7.4.0 – Strategien selbstbestimmt ausprobieren: UX- und Funktionskonzept

**Stand:** 01.11.2026 · **Status:** Konzept/Draft, keine Android-Codeänderung · **Startbasis:** PR #32 in main · **Nächste Umsetzung:** M7.4.1 ff.

## 1. Produktentscheidung und Nutzen

**Ziel:** Nach dem Rückblick nicht nur über Gefühle und Bedürfnisse nachdenken, sondern auf Wunsch eine **selbst ausgewählte Handlungsmöglichkeit** für den Alltag vormerken, später festhalten, wie ein Versuch erlebt wurde, und ohne Bewertung daraus lernen. **Kein** Behandlungsvorschlag, keine automatisch passende Strategie und kein Versprechen, dass etwas helfen wird.

Bestehende, bereits implementierte Bausteine werden *wiederverwendet*, nicht gedoppelt:

- Im Rückblick gibt es nach PR #31 **„Übersicht & Muster“** und **„Meine Erkenntnisse“**.
- Die M6.3-Strategiewelt enthält redaktionelle Strategie-Karten mit stabilen `StrategyCard.id`, Vor-/Nachteilen, Grenzen, Alternativen und verwandten Lernartikel-/Bedürfnis-IDs. Darunter befinden sich auch **Warn-/Verständniskarten** (z. B. schädliche Bewältigungsmuster): sie dürfen **nicht** als praktische Übungen angeboten werden.
- Das bestehende M7.3-Erkenntnismodell speichert nur tatsächlich selbst geschriebene Texte und kann ausdrücklich gewählte Journalbelege enthalten; M7.2-Beobachtungen behaupten keine Ursachen.
- In der aktuellen EKB2-Sicherung liegen nur Tagebuch und Erkenntnisse. Die Erweiterung der gesicherten Daten ist ein **Blocker für jede neue dauerhafte Strategieversuch-Speicherung**.

**Nicht-Ziele:** Therapieprogramm, Erfolgs-/Wirksamkeitsindex, Rangliste, Stimmungstrend, Push-Erinnerungen, Streaks, KI-Beratung, automatisches Verknüpfen von Gefühlen, Bedürfnissen oder Musterkarten mit Strategien.

## 2. Navigation und Auffindbarkeit

Wenn die Umsetzung samt sicherem Speicher freigegeben ist, bekommt Rückblick **drei gleichrangige Unterbereiche**:

| Rückblick-Bereich | Zweck | Typische Nutzerfrage |
| --- | --- | --- |
| **Übersicht & Muster** | Eigene Einträge und mehrfach gemeinsam ausgewählte Begriffe anschauen | „Was habe ich dokumentiert?“ |
| **Meine Erkenntnisse** | Eigene Gedanken und freiwillige Reflexionen schreiben | „Was nehme ich für mich mit?“ |
| **Ausprobieren** | Eine Handlungsmöglichkeit selbst auswählen, vormerken und bei Wunsch reflektieren | „Was könnte ich selbst versuchen – oder bewusst lassen?“ |

**Keine neue Bottom-Bar.** Die drei gleichwertigen Auswahlflächen dürfen bei 320 dp und 200 % Textskalierung in zwei Zeilen umbrechen; Fokus- und Screenreader-Reihenfolge bleibt eindeutig. Bereits geöffnete Inhalte werden beim Wechsel nicht als erfolgreich/erledigt markiert. Private Texte und IDs für eigene Versuche dürfen nicht via `rememberSaveable` in Android-Bundles liegen.

Zusätzliche, *optionale* Einstiege:

- Auf einer **geeigneten** kuratierten Strategie-Detailkarte: **„Für mich zum Ausprobieren vormerken“** (niemals auf reinen Warn-/Verständniskarten). Zuerst **Grenzen / Wann eher nicht?** sichtbar machen; keine eigene Verpflichtung durch den Klick.
- In **„Meine Erkenntnisse“**: **„Eigene Handlungsoption überlegen“**. Das öffnet einen *leeren* Entwurf, optional mit **ausdrücklich bestätigtem** Bezug auf genau diese Erkenntnis. Der Erkenntnistext wird nicht automatisch kopiert.
- Im Ausprobieren-Bereich: **„Aus dem Strategieatlas auswählen“** oder **„Eigene Idee notieren“**.
- **Keine automatische Empfehlung** direkt aus M7.2-Mustern. Vom Rückblick kann nur neutral zur vorhandenen Wissens-/Strategiewelt navigiert werden.

Bei Verlassen einer Strategie-/Wissensdetailseite führt „Zurück zum Versuch“ an die vorherige Stelle zurück; bei einem entfernten Katalogeintrag erscheint ein verständlicher Fallback ohne Datenverlust.

## 3. Vier kurze, freiwillige Nutzerwege

### A. Kuratierte Strategie vormerken

1. Person öffnet Ausprobieren und wählt **„Strategieatlas öffnen“**; dort sucht/liest sie eine Strategie einschließlich möglicher Grenzen und Alternativen.
2. Sie tippt **selbst** auf „Für mich zum Ausprobieren vormerken“. Ein privater Entwurf zeigt den Namen als zitierte Katalogwahl und den Hinweis: „Das ist nur eine Möglichkeit, keine Empfehlung oder Verpflichtung.“
3. Freies, optionales Feld „Was möchte ich – wenn überhaupt – versuchen?“; optional „Was sollte ich dabei beachten?“.
4. **Speichern ist separat und ausdrücklich**: „Idee speichern“. Nach Erfolg erscheint ein neutraler Eintrag mit dem Status **„Offen“**. Vorher gibt es **keine** dauerhafte Dateioperation.

### B. Eigene Idee ohne Katalog

1. Ausprobieren → „Eigene Idee notieren“.
2. Kurzes Pflichtfeld „Meine Idee“; keine vermeintliche Strategieklassifizierung durch NLP.
3. Optionaler Kontext und ausdrücklich gewählte Links (maximal eine bestehende Erkenntnis, später optionale Tagebucheinträge). Keine automatische Übernahme privater Wörter.
4. „Idee speichern“ bestätigt die Speicherung explizit. Leere Eingaben und Doppeltippen erzeugen keinen Datensatz.

### C. Später reflektieren – unabhängig vom Ergebnis

1. Öffnen der selbst gespeicherten Idee; Aktionen **„Status ändern“**, **„Eigene Beobachtung ergänzen“**, **„Bearbeiten“**, **„Archivieren“** und **„Löschen …“**.
2. Neutrale Statusauswahl: **Offen**, **Vorgemerkt**, **Ausprobiert**, **Bewusst nicht ausprobiert**, **Abgeschlossen**. Kein automatisches Voranschreiten, keine Frist, kein Fortschrittsbalken. Statuswechsel ist nicht gleichbedeutend mit einem Wirksamkeitsurteil.
3. Erst **nach eigener Wahl** kann freiwillig bewertet werden: **Hilfreich**, **Teilweise**, **Für mich nicht passend**, **Noch unklar**, **Keine Einschätzung**. „Nicht ausprobiert“ darf ohne Bewertung stehen. Die App erzeugt **keine** geschätzte Wirkung.
4. Freie Frage „Was habe ich wahrgenommen?“, optional „Was möchte ich beim nächsten Mal anders machen?“; keine Pflicht auf intime Offenbarung, keine automatische Interpretation von Texten.
5. „Änderungen speichern“ ist explizit; „Verwerfen“ lässt zuletzt gespeicherte Angaben unverändert.

### D. Eine Idee nicht ausprobieren oder löschen

- „Bewusst nicht ausprobiert“ und „Abgeschlossen“ sind gleichwertige, neutrale Enden – kein Fehlschlag.
- Archivierung versteckt aus „Aktuelle“, ist umkehrbar und keine automatische Löschung.
- Löschen erfordert einen Dialog mit **„Endgültig löschen“** und **„Abbrechen“**, erläutert die Folgen für optionale Links; gespeicherte Tagebucheinträge und selbst verfasste Erkenntnisse bleiben erhalten.
- Beim Löschen verknüpfter Tagebucheinträge/Erkenntnisse bleiben eigene Versuchstexte bestehen, während die ungültigen IDs in einer **atomaren, nachvollziehbaren** Datenänderung gelöst werden.

## 4. Textbeispiele für die spätere Implementierung

**Start im Bereich Ausprobieren**

> **Deine Ideen zum Ausprobieren**  
> Du entscheidest selbst, ob du etwas versuchen möchtest. Du kannst auch Ideen sammeln, ohne sie umzusetzen, und später festhalten, was für dich passend war – oder eben nicht.

**Leerer Zustand**

> Hier hast du noch keine eigene Idee gespeichert. Du kannst eine Möglichkeit im Strategieatlas ansehen oder selbst eine Idee notieren. Beides ist freiwillig.

**Auf geeigneter Strategie-Karte**

> **Für mich vormerken**  
> Diese Karte beschreibt eine Möglichkeit, keine Empfehlung. Lies auch „Wann eher nicht?“. Du kannst jederzeit abbrechen, ohne etwas zu speichern.

**Nach einem Versuch**

> Was hast du selbst bemerkt?  
> Vielleicht hat die Idee geholfen, vielleicht war sie unpassend oder du weißt es noch nicht. Jede Antwort ist in Ordnung; auch gar keine Einschätzung.

**Sicheres Laden fehlgeschlagen**

> Deine gespeicherten Ideen konnten nicht sicher geöffnet werden. Sie werden nicht als leere Liste dargestellt und können gerade nicht verändert werden. Erneut sicher laden.

## 5. UI-Regeln und kritische Sonderfälle

- **Wissen ist nicht persönlich:** Die unveränderlichen M6.3-Karten bleiben öffentlich/offline lesbar, während Experiment-Texte, Status und Bezüge persönliche, gesperrte Daten sind.
- **Kein Auto-Speichern:** Rücknavigation, „Weiter“, Lesen einer Karte oder Klick auf eine Beobachtung erzeugen keinen Versuch.
- **Warnkarten:** Karten aus der M6.3-Familie `patterns` sowie andere ausdrücklich als nicht zum Nachmachen markierte Inhalte erhalten **keinen** „vormerken/ausprobieren“-CTA. Keine mechanische Ableitung allein aus Begriffsname oder `optionalPractice`.
- **Bezüge nur nach Entscheidung:** Strategiekarte-ID oder eigenes freies Thema (genau eines davon); Journal-/Erkenntnis-IDs freiwillig; Wissenslinks ausschließlich auf vorhandene stabile IDs. Fehlende ID ⇒ sicherer Hinweis und Text erhalten, niemals automatische Ersetzung durch eine vermeintlich ähnliche Strategie.
- **Begrenzung:** Konkrete Längen, Mengen und optionale Referenzen sind im Daten-ADR festzulegen und durch Eingabeprüfung/Tests abzusichern.
- **Mehrfachklick / Absturz:** Ein einzelner bestätigter Speichervorgang darf nie zwei Einträge erzeugen. Bei unterbrochenem Export/Import darf keine scheinbar leere Sammlung angezeigt oder überschrieben werden.
- **Kontextwechsel:** Bei optional aktivierter App-Sperre keine privaten Daten, Namen eigener Ideen, Status oder Dialogentwürfe vor Wiederfreigabe anzeigen. Android-Hintergrund und Prozessneustart getrennt prüfen.
- **Barrierefreiheit:** Vollbreite Touchflächen, erkennbare Statuslabels statt Farben, TalkBack einschließlich Link-/Warntext, 320-dp-Display bei 200 % Schrift, Querformat, Dark Mode und System-Zurück. Kein horizontal abgeschnittener Statusfilter.
- **Sicherheitswarnung bei Bedarf:** Bei belastenden/gefährdenden Inhalten keine Versuchsanleitung durch die App oder „Exposition“ vorschlagen, externe Hilfehinweise sachlich und ohne Beschämung.

## 6. Umfang der nächsten Teil-PRs

| Teilphase | Inhalt | Vorbedingung |
| --- | --- | --- |
| **M7.4.0** (dieser PR) | UX-Pfade, Daten-/Backupsicherheitsentscheidung, Risiko- und Testmatrix | Redaktionelle und Nutzerfreigabe |
| **M7.4.1** | Verschlüsselte Experiment-Persistenz **plus** komplette EKB3-/Restore-Transaktionen und Migrations-/Crash-Tests | Kein privates UI-Speichern davor; Sicherheitsgate bestanden |
| **M7.4.2** | Ausprobieren-Ansicht mit CRUD, Status und Reflektion, Schutz bei App-Lock, echte UX-Tests | M7.4.1 grün |
| **M7.4.3** | Kontextsensitive, ausschließlich opt-in Verknüpfungen aus Strategieatlas, Wissensartikeln und eigenen Erkenntnissen, sichere Rückwege | Stabile geprüfte IDs und neutrale Texte |
| **M7.4.4** | Vollständige UX-/Datenschutz-Abnahme und Debug-APK auf echtem Gerät | Alle vorherigen Gates und ausdrückliche Merge-Freigabe |

Diese Teilung ist eine **Arbeitsplanung**, kein impliziter Auftrag zur automatischen Umsetzung/Merge. Bei erheblichen Sicherheitsabweichungen wird zuerst der ADR geändert.

## 7. Noch zu überprüfende Punkte vor Code

- Unabhängige fachliche Sichtung der Strategie-Warnhinweise, besonders Katalogfamilie `patterns` und mögliche andere reine Erklär-Karten.
- Test auf zwei voneinander unabhängigen Android-Geräten für exportierte/importierte neue Sicherungen; Emulator allein ersetzt diesen Test nicht.
- Verständlichkeit für Erstnutzer: Können sie eine eigene Idee speichern und einen Versuch beenden, **ohne** ein Wirksamkeitsurteil abzugeben?
- Relevanz zusätzlicher Verlinkung zu Artikeln: wirklich verständnisfördernd oder vermeidbare Navigationskomplexität?
- Datenschutz: Umfang optionaler Verknüpfungen so gering wie möglich halten; keine neue Berechtigung, keine Cloud, keine Analytics.

**Akzeptanz M7.4.0:** Der Nutzer kann anhand dieser konkreten Flows entscheiden, ob der Funktionsumfang sinnvoll und intuitiv ist. Die technische Sicherung ist spezifiziert, aber **noch nicht implementiert**. Keine neue APK und kein neues privates Speicherformat in diesem Konzept-PR.
