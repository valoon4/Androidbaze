# M7.4 – Test- und Abnahmeplanung: freiwillige Strategieexperimente

**Status:** M7.4.0 (Konzept; keine Tests als durchgeführt markieren). Gilt als Prüfbasis für M7.4.1–M7.4.4. Fiktive Daten ausschließlich.

## A. UX-Szenarien und erwartetes Verhalten

| ID | Szenario | Erwartetes Ergebnis | Umsetzung |
| --- | --- | --- | --- |
| M740-U01 | Ohne gespeicherte Ideen „Ausprobieren“ öffnen | Neutraler Leerzustand mit freien Optionen, kein „0 Erfolge“-Text | M7.4.2 |
| M740-U02 | Kuratierte geeignete Strategie auswählen | Titel/Grenzen/Alternative sichtbar, kein automatisches Speichern | M7.4.3 |
| M740-U03 | Katalogkarte nur lesen, Zurück tippen | Kein persönlicher Datensatz | M7.4.3 |
| M740-U04 | Eigene Idee ohne Strategieatlas schreiben | Eigener Titel, optionaler Kontext; keine automatische Klassifikation | M7.4.2 |
| M740-U05 | Speicherdialog abbrechen / Android-Zurück | Kein Datensatz; bei bereits eingegebenem privaten Text passende Verwerfen-Bestätigung | M7.4.2 |
| M740-U06 | Speichern doppelt antippen | Ein Datensatz mit einer stabilen UUID | M7.4.2 |
| M740-U07 | „Vorgemerkt“ zu „Bewusst nicht ausprobiert“ | Ohne Bewertung gültig, kein Fehlschlag-Text | M7.4.2 |
| M740-U08 | Versuch „Ausprobiert“, Bewertung „Noch unklar“ | Wird genau so gespeichert, keine Wirksamkeitsbehauptung | M7.4.2 |
| M740-U09 | Bewertung ganz weglassen | Erlaubt; keine Pflicht-Reflexion | M7.4.2 |
| M740-U10 | Eigene Erkenntnis ausdrücklich als Bezug wählen | Nur ID-Link, Erkenntnistext wird nicht kopiert | M7.4.3 |
| M740-U11 | Kuratierte Quelle später entfernt/umbenannt | Bestehende Idee bleibt sichtbar; erklärt „nicht mehr im Atlas verfügbar“ | M7.4.3 |
| M740-U12 | Reine Aufklärungskarte der Familie patterns öffnen | **Kein** „Ausprobieren“-CTA | M7.4.3 |
| M740-U13 | Idee archivieren/wiederherstellen | Derselbe Datensatz, keine unfreiwillige Löschung | M7.4.2 |
| M740-U14 | Löschen bestätigen/abbrechen | Nur explizit bestätigtes Löschen; Journal-/Erkenntnistexte bleiben | M7.4.2 |
| M740-U15 | Tabs bei 320 dp + 200 % Schrift, TalkBack | 3 Bereiche erreichbar, korrekte Fokusfolge, keine versteckte Aktion | M7.4.2 |
| M740-U16 | Wissen/Strategie öffnen und zum Versuch zurückkehren | Vorheriger Kontext erhalten, keine doppelten Back-Ziele | M7.4.3 |
| M740-U17 | App sperren / in den Hintergrund schicken | Entschlüsselter Strategie- und Editorzustand verborgen/gelöscht, erneuter Auth-Zugang erforderlich | M7.4.2 |
| M740-U18 | Freie Worte „hat nicht funktioniert“ | Keine Umdeutung, Korrektur oder moralische Bewertung durch die App | M7.4.2 |
| M740-U19 | Strategie für gefährdende Verhaltensweisen | Keine aktive Anleitungs-/Versuchsaktion; nachvollziehbarer Grenzenhinweis | M7.4.3 |

## B. Persistenz, Backup und Migration – Pflicht vor UI-Personalisierung

| ID | Szenario | Erwartung |
| --- | --- | --- |
| M740-S01 | Bestehende App mit EKJ1, EKI1, EKB2 nach Upgrade | Beide bisherigen Sammlungen unverändert; EKS1 neu und **leer nur nach gültiger Initialisierung**, nicht nach Decryptfehler |
| M740-S02 | EKB1 einspielen, lokale Strategieexperimente vorhanden | Journal ersetzt, Erkenntnisse/Experimente behalten; optionale verwaiste Referenzen nach Vorwarnung gelöst |
| M740-S03 | EKB2 einspielen, lokale Strategieexperimente vorhanden | Journal + Erkenntnisse nach Bestätigung ersetzt; Experimente nicht gelöscht; ungültige Links explizit bereinigt |
| M740-S04 | EKB3 einspielen (inkl. Experimentbereich) | Alles nach eindeutiger Vorschau/Warnung vollständig ersetzt |
| M740-S05 | EKB3 mit **absichtlich leerem** Experimentbereich | Nach Bestätigung explizit leer, nicht „nicht im Backup enthalten“ |
| M740-S06 | EKB3 falsch entschlüsselt / Tag beschädigt | Keine einzige lokale Datei geändert |
| M740-S07 | Wiederherstellung auf zweitem Testgerät | Journal, Erkenntnisse und Experimente samt referenzierter IDs/Status vollständig |
| M740-S08 | EKR1-Intent von früherem Stand vorhanden | EKR1-Replay vor jeder EKR2-Operation; keine verlorenen Insights |
| M740-S09 | EKR2-Intent nach jedem von drei Teil-Writes unterbrochen | Nach Neustart ein vollständiger konsistenter Zielzustand, niemals Teilbestand |
| M740-S10 | EKR2-Intent beschädigt / Key fehlt | Sicheres Sperren, keine „leere“ Sammlung und kein neuer Schreibvorgang |
| M740-S11 | Parallel Check-in-Speichern und EKB3-Export/Import | Gleiche koordinierte Transaktion/Snapshot-Grenze; kein gemischtes Backup |
| M740-S12 | Parallel Archivieren, Löschen, App-Lock | Keine späte Wiederanzeige von Daten aus vorherigem Access-Epoch |
| M740-S13 | Löschen eines referenzierten Journals / einer Erkenntnis | Fremdverweise korrekt gelöst; eigener Experimenttext unverändert |
| M740-S14 | Alte App versucht EKB3 zu öffnen | Klare Versionsinkompatibilität, niemals stilles Abschneiden |
| M740-S15 | Datensatz- und Byte-Grenzfälle, 3000 Tagebucheinträge | Sicherer Fehler bei Überschreitung, keine still gekürzten Felder und keine UI-Blockade |
| M740-S16 | Reines List-/Such-/Edit/Delete-Szenario ohne Geräte-PIN | Schutz gemäß tatsächlich aktivierter optionaler App-Sperre; kein stärkeres Keystore-Versprechen |

## C. UX-Prototyp und fachliches Review

Für M7.4.0 werden drei fiktive Aufgaben als textbasierter Prototyp geprüft:

**Aufgabe 1 – Nur merken:** Eine fiktive Person liest „Kleine Schritte planen“, merkt die Karte freiwillig vor, formuliert *keine* eigene Begründung, öffnet und schließt die App. Frage: Versteht sie, dass sie weder die Strategie versucht noch als hilfreich bewertet hat?

**Aufgabe 2 – Eigene Idee:** Die Person notiert „Bei unklaren Aufgaben eine Nachfrage formulieren“, bricht zuerst ab und speichert erst im zweiten Durchlauf ausdrücklich. Frage: Ist der Unterschied zwischen Entwurf und gespeichertem Eintrag jederzeit klar?

**Aufgabe 3 – Etwas passt nicht:** Die Person bezeichnet eine Option als „Bewusst nicht ausprobiert“ bzw. nach einem freiwilligen Versuch als „Für mich nicht passend“. Frage: Entsteht Druck oder wirkt dies wie ein Misserfolg? Gewünscht ist das Gegenteil.

**Messbare Kriterien vor Design-Freeze:**
- Jede Aufgabe ist ohne Entwicklererklärung erreichbar.
- „Automatische Empfehlung“ wird von keinem Testenden für die tatsächlich gezeigte Auswahl gehalten.
- Jede Person erkennt, ob persönliche Daten schon dauerhaft gespeichert wurden.
- Nicht-Ausprobieren / unklar / nicht passend werden als selbstbestimmte Entscheidungen verstanden.
- Keine sicherheitsrelevante Warnung wird beim ersten freiwilligen Speichern übersehen.
- Bei 320 dp und 200 % Schrift sind alle Hauptaktionen einschließlich „Abbrechen“ bedienbar.

Die tatsächlichen Testpersonen, Ergebnisse und Verbesserungen müssen **später separat** protokolliert werden. Dieses Konzept behauptet **keinen** bereits durchgeführten Usability-Test und **keine** unabhängige Sicherheitsfreigabe.

## D. Freigabegates und offene Risiken

| Gate | M7.4.0-Stand | Muss erfüllt sein vor |
| --- | --- | --- |
| Grundsätzliches Funktions- und Datenschutzkonzept | **entworfen, zur fachlichen/Nutzerprüfung** | M7.4.1 |
| EKB3/EKR2-Architektur / Altimporte | **als ADR vorgeschlagen, noch nicht implementiert** | erster persistenter Versuch |
| Alle Schreiben im gemeinsamen Koordinator | **offene technische Implementierung** | erster persistenter Versuch |
| Redaktionsprüfung geeigneter Strategie-Karten | **offen** | Strategie-CTA |
| JVM-/Android-/Keystore-/Crash-Tests | **offen** | Merge der Implementierungs-PRs |
| Echte Geräte / TalkBack / 200-%-Schrift | **offen** | M7.4.4 |
| Zwei-Geräte-Backup-Roundtrip | **offen** | M7.4.4 |
| Unabhängige Security- und Fachprüfung | **offen** | M7.6 bzw. Release |
| Ausdrückliche Freigabe durch den Nutzer | **offen** | jeder Merge |

## E. Verweise

- [M7.4.0 UX- und Funktionskonzept](M740_STRATEGIEEXPERIMENTE_UX_KONZEPT.md)
- [ADR EKS1/EKB3/EKR2](adr/ADR_M740_STRATEGIEEXPERIMENTE_DATEN_UND_BACKUP.md)
- [M7-Abschlussplan vor M8](M7_ABSCHLUSSPLAN_VOR_M8.md)
- [M6.3 Strategieatlas-Konzept](M63_STRATEGIEWELT_KONZEPT.md)

**Kein Auto-Merge.** Nach Abnahme dieser Dokumente kann M7.4.1 den Speicher und die Backup-Erweiterung unter eigenem Draft-PR umsetzen.
