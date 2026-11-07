# K4 – Nutzerabläufe und textuelle Wireframes

**Version:** 1.0 · **Datum:** 07.11.2026 · **Status:** funktionale UX-Spezifikation, keine gerenderten Screens/Prototypen  
**Basis:** [K4 UX-Konzept](K4_UX_KONZEPT.md) · [Screen-Katalog](../specs/k4_screens_v1.json) · K1–K3  
**Konvention:** Jede Aktion ist als **Nutzerentscheidung** gedacht. Screenshots, präzise Pixel, Visual Design und Android-Code folgen erst nach Umsetzungsentscheidungen.

## 1. Navigationsübersicht

```text
APP
├─ 1. Chats
│  ├─ Chatliste (leer / vorhanden)
│  ├─ Neuen Chat starten → Charakterauswahl → Freier Chat
│  ├─ Chat öffnen → Chatansicht → (Auswerten auf Wunsch)
│  ├─ Erneut versuchen → Verzweigung → Vergleich / Variante
│  └─ Chatoptionen → Umbenennen / Löschen / Erinnerungsinfo
├─ 2. Üben
│  ├─ Entdecken & Üben
│  ├─ Kompetenzbereich → Lektion oder passende Szenarien
│  ├─ Szenariodetail → Vorbereiten → Übungs-Chat
│  ├─ Abschluss → Kurzfeedback / Wiederholen / Fertig
│  └─ Mini-Lektion → optional direkt üben
├─ 3. Fortschritt
│  ├─ Übungsaktivität + XP
│  ├─ Lernziele und eigene Reflexionen
│  └─ optionale Lernbeobachtungen → Belege / Korrektur
└─ 4. Profil
   ├─ Eigene Charaktere → Editor / Versionierung
   ├─ Daten & Privatsphäre → Verlauf / Memory / Lernprofil
   ├─ Darstellung und Bedienhilfen
   └─ Hilfe / KI-Informationen / Einstellungen
```

**Regel:** Eine wichtige Funktion hat einen klar benannten Haupt-Einstieg. Tiefe Chatansicht kann die Bottom Navigation zugunsten von Fokus ausblenden; Zurück und Titel bleiben erhalten. Kein versteckter Moduswechsel bei bestehender Nachricht.

## 2. F01 – Erster Appstart, freier Chat (MVP-Pflicht)

**Ausgang:** neue Nutzerin, keine Chats, keine Lernprofile, unsicher ob die App kompliziert wird.

1. **Willkommen:** Titel „Unterhaltungen ausprobieren“. Kurztext „Du chattest mit fiktiven KI-Charakteren und kannst auf Wunsch Kommunikation üben.“ Aktionen **„Gespräch starten“** (primär) und „Gezielt üben“ (sekundär).
2. **Einfache Zielwahl:** optional „Leichter beginnen“, „Gespräche gestalten“, „Einfach ausprobieren“, „Überspringen“. Keine Punktzahl.
3. **Person wählen:** Charakterkarten (Mara, Jonas, Elena, Deniz, Robin) mit Interessen und Kommunikationsstil. Klare Aktion „Mit Jonas sprechen“.
4. **Datenhinweis:** einmalig verständlich über KI-Nutzung und technische Übermittlung informieren; nur zulässige Datenverarbeitung fortsetzen. Details über Button sichtbar, nicht in versteckter Fußnote.
5. **Freier Chat:** deutliche Titelzeile „Jonas · KI-Charakter“ und Status „Freies Gespräch“. Composer aktiv. Keine automatische Coach-Einblendung.
6. **Erster Turn:** Nachricht senden; bei erfolgreicher Übertragung im Verlauf sichtbar; ggf. neutraler Antwortstatus.
7. **Pause:** über Zurück zur Chatliste; Chat und nicht gesendeter Entwurf bleiben erhalten (soweit K5-Persistenz gewährleistet).
8. **Optionaler Coach:** Menü „Auswerten“ → kurze Einordnung / bei zu wenig Material „Noch nicht beurteilbar“ → „Zurück zum Chat“.

**Abbruch-/Sonderwege:** Ohne Zustimmung zu erforderlicher Datenübertragung kein ungefragter KI-Aufruf; Gastkonzept abhängig von K5. Bei Fehler eigene Nachricht erhalten, Retry explizit, kein Doppelversand.

**Akzeptanz:** Nutzer startet ohne Big-Five-Editor, Konto-Fragebogen oder Gamification-Erklärung einen Chat; weiß, dass Jonas KI ist; bleibt in freiem Chat, bis eigener Wechsel.

## 3. F02 – In fünf Minuten etwas üben (MVP-Pflicht)

**Ausgang:** Nutzer möchte lernen, eigene Erfahrungen passend zu erzählen.

1. Tab **Üben** → Kompetenzkarte „Gespräche gestalten“.
2. Detail C03 → Szenario „Fotokurs und Gesprächsfluss“ (S02), sichtbar: „Heute: eigene Beiträge“.
3. Szene zusammengefasst: „Du kennst Jonas aus dem Fotokurs. Der letzte Teil war ihm zu lang.“ Schwierigkeitsprofil „Ausgeglichen“, bei Bedarf auf „Einladend“ änderbar.
4. „Übung starten“ → gesonderter Übungs-Chat mit Moduslabel.
5. Nutzer antwortet frei; kein Pflicht-Klick auf Multiple-Choice-Optionen. „Tipp“ öffnet höchstens eine Beobachtungsfrage und auf Wunsch weitere Hilfestufen.
6. Abschluss oder „Übung beenden“ → optional **„Kurzfeedback ansehen“**, „Noch einmal versuchen“, „Fertig“.
7. Feedback zeigt konkreten Textbeleg, Stärke und ggf. eine kleine Idee. „Nicht passend“ erreichbar.
8. „Noch einmal versuchen“ → Verzweigung oder neue Sitzung; „Üben“ als Rückweg. XP für absolvierte Aktivität, unabhängig davon, ob Jonas enthusiastisch reagiert.

**Akzeptanz:** Ziel, Modus, Charakter und Abschlussoption sind durchgängig erkennbar. Keine Bewertung der introvertierten Persona als „schwierig“.

## 4. F03 – Chat bei klarem Nein angemessen beenden

**Ausgang:** S03, Elena schreibt „Ich muss los, ich habe keine Zeit mehr.“

1. Nutzer: „Alles klar, bis dann.“
2. Figur antwortet ggf. mit kurzem Abschied; Situation kann enden.
3. Abschlussbanner: „Gespräch beendet“ statt „Leider beendet“.
4. Auf Wunsch Feedback: „Du hast die Zeitgrenze respektiert“ ohne zwingenden Verbesserungspunkt.
5. Nutzer kann „Anderes Szenario“ oder „Neuen Chat mit Elena“ starten.

**Negativfall:** Eine Nachricht „Nur noch eine Frage!“ darf nicht durch UI, XP oder Coaching als Erfolg zum Überreden belohnt werden.

**Akzeptanz:** Kurzer Abschluss wird nicht als verlorenes Training oder „negative Engagementrate“ präsentiert.

## 5. F04 – Eine Antwort erneut ausprobieren (Replay)

**Ausgang:** Szenario S02; Nutzer möchte eine frühere Formulierung ändern.

1. Im Nachrichtenmenü „Hier erneut versuchen“ (für TalkBack auch über beschriftete Menüaktion).
2. Vorschau: ursprüngliche Nachricht + Kontext. Erklärung „Wir erstellen einen neuen möglichen Verlauf. Der bisherige Chat bleibt erhalten.“
3. Nutzer schreibt alternative Nachricht.
4. „Variante ausprobieren“ startet einen **neuen Zweig**; sichtbarer Marker „Variante 1“.
5. Eine neue Figurantwort erscheint als mögliche Fortsetzung, nicht als echter Beweis sozialer Wirkung.
6. Aktionen „Mit Original vergleichen“, „Zur Variante“, „Zum ursprünglichen Chat“.
7. Optional K2-Vergleich: unterschiedliche Gesprächshandlungen und mögliche Deutungen, keine „perfekte Antwort“ und kein versteckter Nähe-Score.

**Abbruch:** Bei Cancel bleibt Original unverändert. Bei Offline-/Serverfehler bleibt alternativ eingegebener Text erhalten; noch kein neuer Verlauf wird als erfolgreich dargestellt.

**Akzeptanz:** Nutzer verliert weder Original noch geänderten Entwurf und verwechselt Simulation nicht mit kausaler Wahrheit.

## 6. F05 – Eigenen Charakter erstellen

**Ausgang:** Nutzer möchte einen ruhigen Charakter mit Interesse an Brettspielen.

1. Chatliste → „Neues Gespräch“ → „Eigenen Charakter erstellen“ oder Profil → „Eigene Charaktere“.
2. Titel „Charakter erstellen“ mit klarer Kennzeichnung „fiktive KI-Person“.
3. Basis: Name (erforderlich), Interessen, Kurzidee (optional). Vorschau mit verständlicher Orientierung.
4. Wenige Slider: Gesprächsinitiative, Ausführlichkeit, Direktheit, Humor, persönliche Offenheit.
5. Stimmung für das **neue Gespräch** (unabhängig vom Charakter): „Ausgeglichen“ oder „Überrasche mich“, optional beschäftigt/erschöpft.
6. „Speichern“ und „Neuen Chat starten“ als zwei unterschiedliche Aktionen oder klarer gemeinsamer Abschluss, ohne versteckte Datenfreigabe.
7. Im Chat kann über die Charakterinfo das Profil angesehen, aber die fixierte Persona-Version nicht heimlich geändert werden.
8. Nach Bearbeiten: Hinweis „Neue Version gilt für neue Chats. Bestehende Chats bleiben unverändert.“

**Validierung:** keine leeren Pflichtnamen, begrenzte Feldlängen, Sicherheits-/Imitationsschutz. Keine Aussage, die Slider seien psychologische Messungen.

**Akzeptanz:** Erste Nutzerperson kann einfachen Charakter ohne Kenntnis der Big Five erstellen; bestehende Chats behalten konsistente Einstellungen.

## 7. F06 – Chat fortsetzen und Erinnerung verwalten

**Ausgang:** Nutzer hat schon mit Jonas geschrieben und möchte an den Fotokurs anknüpfen.

1. Chats → Jonas-Chat öffnen; **sichtbarer Verlauf** ist verfügbar, soweit gespeichert.
2. Bei erlaubter Erinnerung kann Jonas ggf. ein belegtes früheres Thema wiederaufgreifen. Er tut nicht so, als habe er den Nutzer außerhalb des Chats real begleitet.
3. Profil → Daten & Privatsphäre → Charaktererinnerungen → Jonas.
4. Liste mit knappen Erinnerungen und Quellenhinweisen, z. B. „Im Chat über Fotokurs gesprochen“.
5. „Eintrag korrigieren“, „Löschen“, „Alle Erinnerungen dieser Figur löschen“, Toggle „Für neue Chats merken“.
6. Bei Deaktivierung transparente Erklärung, ob nur **künftiges Speichern**, **künftiges Verwenden** und/oder **vorhandene Erinnerungen** betroffen sind; **kein** missverständliches Alles-in-einem-Toggle.
7. Bestätigung zeigt Umfang und Auswirkung; technische Löschbestätigung nur nach tatsächlichem Erfolg (K5).

**Akzeptanz:** Verlauf, erinnerte Details und Lernprofil werden nicht verwechselt; keine Charakterübergreifende Erinnerung.

## 8. F07 – Fehlbewertung des Coaches widersprechen

**Ausgang:** Mara hat um Ablenkung gebeten; Coach behauptet, Nutzer habe ihre Belastung ignoriert.

1. Feedbackansicht → „Nicht passend“.
2. Kleine Auswahl „Der Kontext fehlt“, „Mein Ziel war anders“, „Das ist falsch“, „Andere Meinung“.
3. Nutzer kann optional „Sie wollte ausdrücklich Ablenkung“ ergänzen.
4. System markiert Beobachtung als strittig und verwendet sie nicht ungeprüft im Lernprofil.
5. „Rückmeldung neu ansehen“ kann neutral korrigiert werden: „Du hast ihre Bitte um Ablenkung berücksichtigt.“
6. Rückweg „Zum Chat“, keine negative XP-/Levelwirkung.

**Akzeptanz:** Widerspruch ist nicht versteckt, keine Verteidigung der KI-Analyse als objektive Wahrheit.

## 9. F08 – Fortschritt ohne Lernprofil

**Ausgang:** Nutzer hat Lernanalyse nicht aktiviert.

1. Tab Fortschritt → Übungspraxis und XP sichtbar, sofern lokale Speicherung freigegeben.
2. Bereich „Lernbeobachtungen“ erklärt: „Aus. Wir speichern keine persönlichen Coach-Muster.“
3. Aktion „Mehr erfahren“ → Zwecke und Beispiele, getrennte Aktivierung falls implementiert.
4. Ohne Aktivierung bleibt die App nutzbar, Üben und Chats sind nicht gesperrt.
5. Kein leerer Ring „0 % Empathie“ oder FOMO-Prompt.

**Akzeptanz:** Freiwilligkeit wird durch UX eingehalten, nicht nur behauptet.

## 10. F09 – Fehler beim Senden / instabiles Netzwerk

**Ausgang:** Nutzer schreibt drei Sätze; der KI-Dienst antwortet nicht.

1. Lokaler Sendestatus sichtbar, Eingabe nicht vernichtet.
2. Bei Timeout: „Antwort konnte nicht erstellt werden.“ Aktionen „Erneut versuchen“ und „Zurück“.
3. Retry über identischen Request-Token und Chat-ID, sobald K5 dies technisch abbildet; kein doppelter Nutzerturn.
4. Wenn keine Retry-Garantie technisch möglich, Fehlermeldung ehrlich und erneut senden nur nach Nutzerentscheidung.
5. Bei Rückkehr bleibt der Status nachvollziehbar; kein künstliches menschliches Tipp- oder Lesesignal.

**Akzeptanz:** keine stille Nachrichtenduplikation, kein falsches „Antwort unterwegs“ nach Abbruch.

## 11. F10 – Datenlöschung ohne versteckte Konsequenzen

**Ausgang:** Nutzer möchte einen gespeicherten Chat löschen.

1. Chatoptionen → „Chat löschen“.
2. Vor dem Löschen kurzer, konkreter Text: Welche Chatdaten und zugehörigen Erinnerungen/Lernbeobachtungen betroffen sind; welche Informationen möglicherweise in Backups verzögert gelöscht werden (K5).
3. Destruktive Aktion eindeutig benennen („Chat endgültig löschen“) und abbrechbar gestalten.
4. Nach tatsächlicher Löschung Ergebnis zeigen. Bei Teilfehler nicht pauschal „vollständig gelöscht“ behaupten, sondern Status und weitere Möglichkeiten transparent machen.
5. Nutzer kann ggf. separate Bereiche (Charaktererinnerungen/Lernprofil) zusätzlich löschen, falls deren Inhalte anders entstanden sind.

**Akzeptanz:** kein Dark Pattern, keine fälschliche Datenschutzgarantie und keine verbleibenden verwaisten abgeleiteten Notizen.

## 12. F11 – Mini-Lektion lesen und freiwillig anwenden

**Ausgang:** Nutzer möchte das Thema „passend nachfragen“ verstehen.

1. Tab Üben → „Kurz erklärt“ / passende Mini-Lektion.
2. 1 Kernidee, 1 Alltagsszene, 1 Gegenbeispiel, 1 Hinweis auf Situation.
3. „Mit einer kurzen Übung ausprobieren“ → Szene mit C02.2, oder „Zurück“.
4. Keine Pflicht zur Quizfrage, keine Bestrafung für Überspringen.
5. Nach Übung optionale Reflexion: „Welche Formulierung würde zu dir passen?“

**Akzeptanz:** Wissen und Übung sind verbunden, aber frei nutzbar.

## 13. F12 – Große Schrift, Screenreader und Android-Zurück

**Ausgang:** Nutzer verwendet 200 % Schriftgröße / TalkBack oder Navigation per Systemgeste.

1. Vier Tabs mit Textlabel lesbar; keine ausschließlich ikonische Navigation.
2. Nachrichten werden korrekt als „Du“ bzw. „Jonas, KI-Charakter“ ausgegeben, relevante Textreihenfolge erhalten.
3. Nachrichtmenü „Erneut versuchen“ per Fokus/Tap erreichbar, nicht nur durch langes Drücken.
4. Tastatur verdeckt keine kritische Aktion; Scrollposition und Entwurf erhalten.
5. Android-Zurück führt im Replay zuerst zum ursprünglichen Chat oder zur vorherigen Ansicht, nicht unbemerkt aus der App.
6. Fehlermeldungen werden verständlich angekündigt, ohne den Screenreader fortlaufend zu unterbrechen.

**Akzeptanz:** Alltagsnutzung funktioniert mit unterstützenden Technologien und unabhängig von Gesten.

## 14. Textuelle Layoutskizzen

### 14.1 Chats

```text
┌──────────────────────────────────┐
│ Chats                  + Neu     │
│                                     
│ Jonas   · KI-Charakter             │
│ Fotokurs – gestern                  │
│                                     
│ Mara    · KI-Charakter              │
│ Konzert – letzte Woche              │
│                                     
│ [Neues Gespräch starten]            │
│                                     │
│ Chats     Üben   Fortschritt Profil │
└──────────────────────────────────┘
```

Bei leerer Liste die vorhandenen Chat-Zeilen durch eine kurze, unaufdringliche Erklärung ersetzen. Nicht „Du hast bisher keine Freunde“.

### 14.2 Freies Gespräch

```text
┌──────────────────────────────────┐
│ ← Jonas · KI-Charakter        ⋮   │
│ Freies Gespräch                   │
│──────────────────────────────────│
│ Jonas: Der Kurs war gut, aber …   │
│                                   │
│ Du: Den Schluss fand ich auch …   │
│                                   │
│ [ Nachricht schreiben … ] [Senden]│
│──────────────────────────────────│
│ Optional: über ⋮ „Auswerten“       │
└──────────────────────────────────┘
```

Keine pauschale Skill-Punktzahl neben jeder Textblase.

### 14.3 Übung

```text
┌──────────────────────────────────┐
│ ← Übung: Fotokurs            ⋮    │
│ Heute: eigene Gedanken einbringen │
│──────────────────────────────────│
│ Jonas: War gestern ganz okay …    │
│                                   │
│ Du: ...                           │
│                                   │
│ [Tipp] [ Nachricht … ] [Senden]    │
│──────────────────────────────────│
│ [Übung beenden]                   │
└──────────────────────────────────┘
```

„Übung beenden“ darf einen kurzen Auswahlbildschirm öffnen, ohne automatisch Feedback aufzuzwingen.

### 14.4 Kurzfeedback

```text
┌──────────────────────────────────┐
│ ← Dein Rückblick                  │
│                                   │
│ Das ist dir gelungen              │
│ „Du hast die Müdigkeit bemerkt …“ │
│ [Im Chat ansehen]                 │
│                                   │
│ Eine Idee zum Ausprobieren        │
│ „Eine Möglichkeit wäre …“         │
│ (nur wenn sinnvoll / belegt)      │
│                                   │
│ [Erneut versuchen]                │
│ Mehr erfahren · Nicht passend     │
│ [Fertig]                          │
└──────────────────────────────────┘
```

Die zweite Karte entfällt vollständig, wenn kein sinnvoller Änderungspunkt vorliegt.

### 14.5 Charaktereditor

```text
┌──────────────────────────────────┐
│ ← Eigenen Charakter erstellen     │
│ Name [                         ]  │
│ Interessen [Fotografie, Natur  ]  │
│                                   │
│ Gesprächsinitiative [────●────]   │
│ Ausführlichkeit     [──●──────]   │
│ Direktheit          [──────●──]   │
│ Humor               [────●────]   │
│ Persönliche Offenheit[───●─────]  │
│                                   │
│ [Beispiel ansehen]                │
│ [Charakter speichern]             │
└──────────────────────────────────┘
```

Die Regler sind illustrative Controls, keine validierten psychometrischen Messwerte. Die UI muss später passende Bezeichnungen und Erläuterungen mit Nutzern testen.

## 15. Zustands- und Zurücknavigation als Invarianten

- Original-Chat und Replay sind **niemals** dieselbe bearbeitbare Historie.
- Chattyp bleibt explizit `free` oder `practice`, ein Wechsel ist eine bewusste Aktion.
- Coach-Ausgabe ersetzt nie eine gesendete Persona-Nachricht.
- Persona-Version bleibt pro Chat fixiert.
- Rückmeldung kann immer geschlossen werden, ohne Lernprofil-Einwilligung zu erzwingen.
- Aus einer fehlgeschlagenen Netzwerkaktion entstehen keine erfundenen AI-Antworten oder „bereits gesendeten“ Zustände.
- Bei jeder erkennbaren Lösch-/Korrekturaktion müssen die eigenen Daten und abhängige Ableitungen berücksichtigt werden.
- UI zeigt keine fiktiven Gefühle als exakte Zustände oder als Garant für echte menschliche Wirkung.

## 16. Abgrenzung zur Umsetzung

Dies sind **Text-Wireframes und Benutzerabläufe**, keine Pixel-Designs, App-Screens oder Tests auf realen Geräten. Die Reihenfolge und Beschriftung müssen in K6 und späteren Nutzerstudien validiert werden. K5 klärt, welche Persistenz-, Gast-, Offline- und Synchronisationszusagen technisch sicher möglich sind.
