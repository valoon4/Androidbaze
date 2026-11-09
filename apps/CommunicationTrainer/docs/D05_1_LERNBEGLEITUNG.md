# D05.1.0 – Lernziele, Schwierigkeit und eigene Reflexion (erste Etappe)

**Entwicklungsstatus:** bewusst kleine auf D05.0 aufbauende Etappe, noch kein KI-Coach, keine Bewertung von Personen.

## Was soll sich für den Nutzer verbessern?

D05.0 zeigt einen realistischen Rollenspielpartner. Der Nutzer konnte ein Nein setzen und einen enttäuschten Freund durch eine offene Frage besser verstehen. Das allein belegt noch keinen Lernfortschritt. D05.1 muss deshalb die Trainingsaufgabe klarer machen und eigene Beobachtungen ermöglichen, ohne zu behaupten, dass ein einzelnes Rollenspiel messbar bessere Kommunikationsfähigkeit beweist.

### 1. Klarer Trainingsauftrag vor jedem Szenario

- **Freundlich Nein sagen:** eine Grenze freundlich und klar setzen; Rückfragen angemessen behandeln.
- **Aktiv zuhören:** vorsichtig Gefühle spiegeln, offen nachfragen und Lösungen nicht vorschnell vorschlagen.
- Kurz und verständlich: ein Lernziel plus konkrete Handlungsorientierung, kein Lehrbuch-Text.

### 2. Drei wählbare Schwierigkeitsgrade

- **Einstieg:** entgegenkommender Partner, klare Grenzen werden schnell respektiert.
- **Alltag** (voreingestellt): eine realistische Nachfrage oder Unsicherheit.
- **Anspruchsvoll:** ein zusätzlicher milder Widerstand oder mehrdeutige Gefühle; keine manipulativen Schuldvorwürfe, Grenzannahme nach Klarstellung.
- Die Werte sind **Prompt-Vorgaben**, keine Garantie, dass das Modell sie immer erfüllt. Modellkandidaten werden nicht nach erfundenen Qualitätsscores sortiert.
- Wechsel des Levels beendet den aktuellen D05-Pilotdialog und setzt Entwurf sowie Online-Consent zurück. **Bei vorhandenem Entwurf oder begonnenem Chat fragt die UI erst nach Bestätigung**, statt unbemerkt Daten zu verwerfen. Erst die nächste ausdrücklich gesendete Nachricht löst eine neue KI-Anfrage aus.
- Szenario- und Rollenfakten bleiben über alle Schwierigkeitsstufen hinweg stabil.

### 3. Offline-Selbstreflexion statt erfundener Bewertung

- Erst nach mindestens einer erfolgreich abgeschlossenen Nutzernachricht samt KI-Antwort werden freiwillige Reflexionsfragen angezeigt.
- Drei szenariospezifische Beobachtungen zum Abhaken sowie ein freies Reflexionsfeld (max. 400 Zeichen).
- **Nur der Nutzer** sagt, was ihm gelungen ist. Die App erstellt keine Noten, objektiven Kompetenzwerte, Diagnosen oder KI-Bewertungen.
- Kein zusätzlicher Cloud-Aufruf für Reflexion, keine Übertragung dieser Antworten im D05-Rollenspiel, keine Speicherung; bei App-Prozessende gehen die Angaben verloren.
- Nach Szenario-/Level-/Modellwechsel und Kontowechsel wird die Reflexion verworfen. Szenario-/Modellwechsel sowie „Neues Training“ fragen bei ungespeicherten Inhalten **vorher** nach Bestätigung.
- Neue KI-Antworten heben die bisherige „vorgemerkt“-Bestätigung auf, damit keine veraltete Zwischenreflexion als Abschluss erscheint.

## Technische Leitplanken

- D05.1 baut auf dem **offenen PR #66** auf, um keine teilweise integrierte D05-Chatfunktion nach `main` zu bringen.
- D03-Room-Chat, Chat-Entwürfe und Theme-Einstellungen bleiben unberührt.
- Die Rolle in „Aktiv zuhören“ ist präzisiert: **Die übende Person hat das Treffen abgesagt**; der Freund zeigt seine Enttäuschung.
- OAuth-Bearer bleibt flüchtig; bestehende Zustimmung pro Online-Runde, `store:false`, `stream:true` und kein kostenpflichtiger API-Fallback bleiben erhalten.

## Erfolgskriterien (noch nicht pauschal erfüllt)

- [ ] CI: alle bisherigen D03–D05-Tests, neue D05.1-Offline-Tests, Room-Schema, debug-Signatur bestanden
- [ ] APK auf Galaxy A32 5G installierbar, UI bei Android 13 mit Tastatur und kleinem Display benutzbar
- [ ] Beide Lernziele sind vor der ersten Nutzerantwort verständlich
- [ ] Level-Wechsel verwirft alte Chats, ohne eine neue KI-Anfrage auszulösen oder bisherige D03-Daten anzutasten
- [ ] Nach einer echten Antwort erscheint Reflexion; ohne Antwort keine falsche Selbsteinschätzung
- [ ] Die eigene Reflexion bleibt lokal und ist **nicht** Bestandteil weiterer Modellanfragen
- [ ] KI beachtet den Level hinreichend: bei Einstieg eine klare Grenze akzeptieren; bei anspruchsvoll realistische, respektvolle Herausforderung
- [ ] Nutzer versteht, dass Reflexion nicht dauerhaft gespeichert wird

## Weiterer D05.1-Plan – ausdrücklich nicht in dieser ersten Etappe

1. **D05.1.1 – Reflexionsqualität:** Nutzereingaben und Selbsteinschätzung anhand tatsächlicher Beobachtungen evaluieren; Schwierigkeiten wie uneindeutige Rollen/ungenügend respektierte Grenzen systematisch erfassen.
2. **D05.1.2 – Optionales KI-Feedback:** separater, ausdrücklich freigegebener Cloud-Aufruf, nur mit markiertem Ausschnitt des Pilotdialogs und klaren Kriterien. Kein pauschales Lob, keine starren Noten, keine Psychodiagnosen; nachvollziehbare Beispiele statt Halluzinationen.
3. **D05.2 – Sitzungsstabilität und Datenschutz:** sichere lokale Persistenz und Wiederherstellung von Entwürfen, Schutz bei App-Wechsel/Prozessende, Abbruch- und Deduplikationskonzept, transparente Cloud-Datenkontrolle, robuste Tokenverwaltung.

**Kein Merge ohne ausdrückliche Nutzerfreigabe.** PR #66 und die D05.1-Folge-PR werden unabhängig abgenommen.


## D05.1.0 – Qualitätsnachbesserung nach Gerätetest „Anspruchsvoll“ (09.11.2026)

**Konkreter Nutzerbefund:** Bei der ersten höflichen Absage („Ich habe leider keine Kapazitäten dafür. Ich bin mit meiner Arbeit selbst in Rückstand.“) antwortete die KI schon mit „Ich respektiere dein Nein und kümmere mich selbst darum“. Das ist sozial angemessen, aber belegt keine herausfordernde Gesprächssituation. Der bisherige Prompt erlaubte bereits allein den Ausdruck einer Enttäuschung als `Anspruchsvoll`, daher musste das Modell nicht nachfragen.

**Verfeinerung:** Der Schwierigkeitsgrad erhält für „Freundlich Nein sagen“ jetzt ausdrücklich *rundenbezogene* Instruktionen. Auf ein erstes höfliches Nein soll das Gegenüber – sofern Nachfragen nicht ausdrücklich ausgeschlossen wurden – **genau eine kleine, freiwillige Anschlussfrage** stellen, ohne Schuldgefühle oder Druck aufzubauen. Bei einer zweiten klaren Grenze lautet die Anweisung: keine weitere Bitte, Nein akzeptieren. Der Einsteigermodus bleibt nach einer Absage ohne Nachverhandlung. Eine einzelne Modellantwort bleibt probabilistisch; Offline-Tests verifizieren nur, dass die richtigen Instruktionen übertragen werden, nicht dass jedes Modell sie garantiert befolgt.

**Zweiter Nutzerbefund:** Im Freitextfeld der Reflexion steht „Weiß nicht“. Die Reflexionsaufforderung war zu offen. Neue, szenariospezifische **optionale Denkimpulse** helfen mit konkreten Beobachtungsfragen, ohne ein positives Ergebnis zu suggerieren. Keine automatische Textbefüllung, Bewertung, zusätzliche KI-Anfrage oder Speicherung. Ein „Weiß nicht“ bleibt eine voll gültige persönliche Reflexion.

**Testannahme:** Schwierigkeit „Anspruchsvoll“ sollte nach der ersten Absage eine *höfliche, kleine* Nachfrage eher wahrscheinlich machen. Nach einer wiederholten klaren Absage muss die KI die Grenze respektieren; keine künstliche Eskalation. Die Reflexionshilfe soll bei Bedarf sichtbar sein und die Eingabe nicht überlagern. Die UI soll bei kleinen Displays mit Bildschirmtastatur nutzbar bleiben.

Die alte Test-APK ist nicht zum Überschreiben durch eine zufällige Debug-Signatur geeignet. Neue APK bekommt eine eigene Paketkennung; nicht mehr benötigte Pilot-Apps können anschließend entfernt werden, um Speicher zu sparen.
