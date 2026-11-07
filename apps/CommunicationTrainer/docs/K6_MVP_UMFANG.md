# K6 – Priorisierter MVP-Umfang und gestufter Produktstart

**Version:** 1.0 · **Datum:** 07.11.2026 · **Status:** priorisierter Vorschlag zur fachlichen Freigabe; kein lauffähiger Build
**Grundlage:** [K6-Audit](K6_GESAMTAUDIT.md), [K1](K1_LERN_KOMPETENZSYSTEM.md), [K2](K2_KOMMUNIKATIONSCOACH.md), [K3](K3_CHARAKTERE.md), [K4](K4_UX_KONZEPT.md), [K5](K5_TECHNISCHE_ARCHITEKTUR.md).
**Technische Priorisierung:** [K6 Scope-Fixture](../specs/k6_mvp_scope_v1.json).

## 1. Wertversprechen der ersten realen App

Ein erwachsener Nutzer kann **mit einem klar als KI gekennzeichneten fiktiven Charakter** einen freien Dialog führen, eine alltagsnahe Kommunikationssituation üben und freiwillig eine **beleggebundene, faire kurze Rückmeldung** erhalten. Seine ungesendeten Texte und gespeicherten Chats bleiben beim normalen Appwechsel/Neustart erhalten. Es wird keine allgemeine soziale Kompetenz, Diagnostik oder Bindungsfähigkeit behauptet.

**Wichtiger Unterschied:**
- **Technisches Alpha:** nur eine kontrollierte, interne bzw. eingeladene Lern-/Machbarkeitsstufe. Dient der Fehlersuche und technischen Entscheidung. Keine Produktreife behaupten.
- **V1-MVP:** kleinste **realistisch nutzbare und veröffentlichungsfähige** Version mit sauberen Datenschutz-, Safety-, Support-, Modell- und UX-Gates.
- **V1.1+**: bewusste Erweiterungen nach echten Nutzungs- und Qualitätsdaten.

Das schließt nicht aus, dass die erste Alpha auf einem Mock-Provider beruht. **Keine ungeprüften echten Nutzerdaten zu Evaluation verwenden.**

## 2. Phase A – Technisches Alpha (nicht öffentlich)

**Minimum für den ersten durchgängigen Test:**

| Teil | Soll |
|---|---|
| Android | isolierter Debug-Build in apps/CommunicationTrainer/, Kotlin/Compose, zwei Kernziele Chats + Üben |
| Figuren | **3** aus K3: Mara, Jonas, Elena; unveränderliche kuratierte v1-Profile |
| Szenarien | **2** aus K3: S02 Fotokurs / C03 und S03 zeitliche Grenze / C06, damit sowohl Gesprächsfluss als auch Grenzen geprüft werden |
| Chat | freie Nachrichten, KI-Rolle sichtbar, Senden, Warten, Fehler, Wiederaufnahme |
| Zustand | lokale Chats, Entwürfe, Persona-/Session-IDs und Requeststatus korrekt gespeichert |
| KI | zuerst Mock-Provider, dann ein kontrollierter echter Provider oder lokales Spike-Modell nach getrennten Sicherheits-/Qualitätsgates |
| Safety | Szenario-Grenzen und keine offensichtliche Prompt-Injection-Übernahme |
| Coach | K2-Auswertung zunächst offline mit synthetischen Fällen testen; **keine** unkalibrierte automatische Bewertung realer Nutzer |
| Daten | keine dauerhafte Lernprofilbildung, keine Cross-Session-Memory, klare Testdatentrennung |
| Betrieb | Request-Idempotenz und harte Nutzungs-/Kostenlimits beim echten Broker |

**Alpha-Abnahmesatz:** Auf dem ausgewählten Gerät kann man ein Gespräch beginnen, den Entwurf bei Prozessneustart wiederfinden, genau einmal senden, eine plausible Charakterantwort erhalten und das Gespräch bei Bedarf sauber beenden. Die Testevidenz ist dokumentiert.

Ein „schöner Screenshot“ ohne Daten-/Lifecycle-Zuverlässigkeit gilt nicht als Alpha-Abnahme.

## 3. Phase B – Geprüftes V1-MVP

### 3.1 Unverzichtbar im V1

| ID | Bestandteil | Minimale Umsetzung | K1–K5-Quelle |
|---|---|---|---|
| F01 | Ehrlicher Einstieg | KI-Hinweis, kurzes optionales Ziel, technische Datenoffenlegung vor externem Transfer | K4, K5 |
| F02 | Navigation | **4 Tabs**: Chats, Üben, Fortschritt, Profil; in tieferen Ansichten Fokus und Android-Zurück | K4 |
| F03 | Charakterwahl | fünf kuratierte fiktive Erwachsene: Mara, Jonas, Elena, Deniz, Robin | K3 |
| F04 | Freie Textchats | Chatliste, Neues Gespräch, gespeicherte Unterhaltung, Senden/Retry, sichere Fehlerstates | K3–K5 |
| F05 | Entwurfserhalt | Entwürfe vor Netzwerkversand lokal persistiert; App-/Prozessneustart/Rotation bestanden | K4, K5 |
| F06 | Szenariotraining | sechs K3-Szenarien S01–S06, ein klar sichtbares K1-Lernziel, freiwilliger Abschluss | K1, K3, K4 |
| F07 | Schwierigkeit | drei **situative** Dynamikstufen einladend, ausgeglichen, zurückhaltend; kein Charakterranking | K3 |
| F08 | Coach-Kurzfeedback | optional nach Übung, mit echten Textbelegen und maximal einem Hebel; Stärke-only/NOT_ASSESSABLE/Grenzhinweis | K1, K2 |
| F09 | Coach-Widerspruch | „Nicht passend“ melden/ausblenden, keine ungeprüften strittigen Beobachtungen | K2, K4 |
| F10 | Ein Replay-Zweig | eine alternative Nutzerantwort ausprobieren; Original unveränderlich; Folge als Simulation markiert | K2–K4 |
| F11 | Mini-Wissen | drei sorgfältig redigierte kurze Lektionen, erreichbar über Üben, freiwillig | K1, K4 |
| F12 | Fortschritt | abgeschlossene Übungen, einfaches XP/Aktivität und eigene optionale Reflexion; **keine Kompetenznote** | K1, K2 |
| F13 | Datenkontrolle | echte Löschung von Chats und allen tatsächlich gehaltenen Ableitungen; Datenschutzhinweis passend zu realem Datenfluss | K4, K5 |
| F14 | Safety und Reporting | KI-Kennzeichnung, Grenzschutz, notwendige Moderation, In-App-Meldung problematischer KI-Ausgaben | K2, K3, K5 |
| F15 | Android-Zugänglichkeit | Touchziele, TalkBack, Schriftgrößen, Keyboard-Sicherheit, Back-Navigation, Low-/No-network | K4, K5 |
| F16 | Stabiler KI-Pfad | **ein** rechtlich/qualitativ/betrieblich freigegebener Standardmodus, Budget, Rate Limits und Tracking-freie Kernnutzung | K5 |
| F17 | Nachvollziehbare Grenzen | keine Bindungs-, Fähigkeits- oder Therapiebehauptungen, keine KI-Person-Echtzeitillusion | K1–K5 |

### 3.2 Mindestregel für ein V1-Feature

Ein Feature darf nur als umgesetzt zählen, wenn:
1. es eine Nutzerhandlung und einen echten Funktionseffekt hat;
2. gute und schlechte Eingaben sowie Fehler-/Abbruchfälle geprüft sind;
3. Privacy-/Safety-/Accessibility-Vorgaben aus K1–K5 eingehalten sind;
4. keine nicht vorhandene Cloud-/Offline-/Löschgarantie in der UI behauptet wird;
5. es bei einem Rückschritt auf den letzten sicheren Stand zurückgesetzt oder deaktiviert werden kann.

### 3.3 Bereits verfügbare Konzeptressourcen

- **Personas:** die fünf JSON-Fixtures in ../specs/k3_personas_v1.json; das sind noch keine ausführbaren KI-Personas.
- **Szenarien:** S01–S06 in ../specs/k3_scenarios_v1.json; müssen an echte K1-Anker und die K3-Runtime angebunden werden.
- **Kompetenzen:** C01–C06 und 24 Unterkompetenzen, intern vollständig erhalten, im MVP nur aktuell relevantes Ziel angezeigt.
- **Wissen:** drei initiale Lektionen aus dem 12-Artikel-Curriculum; fachlich erst noch redaktionell zu schreiben/abzunehmen.
- **Testfälle:** K1/K2/K3/K4/K5 als Soll-Fälle; vor Veröffentlichung muss ein maschinen-/human-eval-gestützter Kern regressionsfähig sein.

## 4. Explizit nicht Bestandteil der ersten V1

| Funktion | Zielversion / Begründung |
|---|---|
| Eigene Charaktere mit vollem Editor und dauerhafter Persona-Versionierung | **V1.1**; zuerst kuratierte Figuren sichern und validieren |
| Cross-Session-Charaktererinnerung | nach V1, nur mit echter Opt-in-, Scope- und Löschimplementierung |
| KI-aggregiertes Lernprofil und psychologische Entwicklungsscores | aggregiertes Profil nach V1 und nur qualifiziert; globale soziale Scores **grundsätzlich nicht** |
| Mehrere parallele Replay-Zweige und umfangreiche Vergleichsgalerien | V1.1+ nach Kosten- und UX-Prüfung |
| Ausführlicher Coachbericht und permanente Live-Auswertung | V1.1+; V1 fokussiert auf beweisbares Kurzfeedback |
| Vollständiges 12-Lektionen-Curriculum und Lernpfade | V1.1+ nach redaktioneller QA |
| 8–12+ Szenarien | spätere Bibliothek, V1 nutzt die sechs K3-Fixtures |
| Sprachanrufe, Gruppen, Video, künstliche Push-Nachrichten | kein V1; eigenständiger Datenschutz-/Risikoumfang |
| Streak-Druck, Zuneigungslevel, soziale Ranglisten | **nicht** als Produktwert eingeplant |
| Kontozwang/Gastmodus als frei festes Marketingversprechen | K5-Auth-/Abuse-Entscheidung notwendig; Pilot zunächst begrenzt |
| ChatGPT-Kontoanbindung als verlässlicher V1-Modus | offiziell unterstützte Android-/Distributionsberechtigung **nicht** nachgewiesen |
| Kostenlos nutzbare lokale KI für alle Android-Handys | geräte-/modell-/qualitätsabhängig, erst nach Benchmark |
| Eigener großer GPU-Server als Betriebsstandard | erst bei gemessenem Break-even und Betriebsverantwortung |

## 5. Prioritäten innerhalb von V1

**P0 – Nicht verhandelbare Basiskomponenten:** Auth/Ownership falls Server, Budget/Abuse, keine Secrets in APK, validierter KI-Datenfluss, klare KI-Kennzeichnung, Chat-/Entwurfspersistenz und Löschung, Safety/Report, Daten-/Backup-Richtigkeit, Accessibility-Blocker.

**P1 – Kernnutzen:** freie Chats, fünf Figuren, sechs Szenarien, gebundenes Kurzfeedback und ein Replay-Zweig.

**P2 – Kleine unterstützende Features:** drei Lektionen, freiwillige einfache XP-/Aktivitätsdarstellung, ein Lernwunsch. P2 darf **keine** P0-Arbeit verdrängen und keine eigenen Safety/Privacy-Pflichten umgehen.

**Scope-Fallback bei Zeit-/Qualitätsdruck:** Nicht Safety, Datensicherheit oder ehrliche Datenoffenlegung reduzieren. Stattdessen Anzahl der freigeschalteten Szenarien/Charaktere **für eine weitere Beta** begrenzen oder die öffentliche V1 verschieben; unzureichend validierte Coach-/Replay-Funktion deaktivieren statt als fertig auszugeben.

## 6. Produkt-Metriken, die sinnvoll und fair sind

| Dimension | Positive Beobachtung | Unzulässiger Ersatz |
|---|---|---|
| Orientierung | Aufgabenabschluss bei Chatstart, Feedback und Zurück | Bildschirm-Views |
| Lernnutzen | Nutzer kann einen konkreten Ansatz erklären und freiwillig ausprobieren | „KI fand dich sympathisch“ |
| Qualität | nachvollziehbare Textbelege, geringe kritische Fehler, menschliche Einschätzung | Tonfall des Modells allein |
| Vertrauen | KI-Natur und Datenziel verstanden, Beschwerde-/Löschweg auffindbar | einfach nur Opt-in-Klickrate |
| Stabilität | Entwurf bleibt erhalten, genau ein Sendeereignis, sicherer Fehlerzustand | Zahl generierter Antworten |
| Kosten | realer Betrag je Nutzungsprofil bei tragfähiger Qualität | nur billigster Tokenpreis |
| Vielfalt | K3-Personas unterscheiden sich ohne Klischees | hoher Emoji-/Frageanteil |

**Keine Wirksamkeitsbehauptung:** Ob sich reale Alltagskommunikation verbessert, braucht getrennte, langfristige Evaluation; Simulationserfolg allein genügt nicht.

## 7. Design und Technik: bewusst einfach

- Keine 30 individuell designten Pflichtbildschirme. Die 30 Screen-IDs aus K4 repräsentieren Zustände/Funktionen; etwa **11–14 wiederverwendbare UI-Vorlagen** als Ausgangspunkt, die Anzahl später nach Prototyp festlegen.
- Kein verpflichtender Big-Five-Editor, damit ein erster Chat ohne Fachjargon beginnt.
- Klare Moduskennzeichnung und Chatfortsetzung statt manipulativer Messenger-Illusion.
- Keine versteckte ChatGPT-/lokal-/Cloud-Umschaltung. Die V1 zeigt den tatsächlich eingesetzten und zulässigen Modus verständlich an.

## 8. Offene Entscheidungen vor Umsetzung

1. Welcher **einzige** Inferenzmodus bestand die K2-/K3-/Datenschutz-/Kostenprüfung?
2. Welche konkrete Modellversion, Modelllizenz, Serverregion und Provider-Vertragslage?
3. Wie ist der Auth-/Gastpfad für Beta/V1, und wie verhindern wir Missbrauch ohne Invasion der Privatsphäre?
4. Werden Chatverläufe ausschließlich lokal gespeichert? Wie ist Android Backup/Restore geregelt?
5. Welcher konkrete Kosten-/Budgetrahmen ist vom Betreiber freigegeben?
6. Welche rechtlich geprüften Informations-/Melde-/Löschtexte dürfen wir anzeigen?
7. Wer betreibt Moderation, Incident-Response und Sicherheits-/Releasefreigabe?
8. Wie wird die QA für deutsche Dialoge und K2-Feedback mit echten Prüfern aufgesetzt?

Für Entscheidungen und Prüfungen siehe [K6-Abnahme](K6_ABNAHME_UND_ENTSCHEIDUNGEN.md). **V1 ist eine Zieldefinition, keine automatische Implementierungs- oder Releasefreigabe.**
