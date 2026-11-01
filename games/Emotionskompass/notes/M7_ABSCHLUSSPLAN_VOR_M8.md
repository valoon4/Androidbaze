# M7-Abschlussplan vor M8 — geprüfter Stand vom 01.11.2026

**Wichtig:** Die gemergte M7-UX-Runde (#31) schließt **die Teilbereiche M7.0–M7.3** und deren Navigation ab, **nicht automatisch den vollständigen Meilenstein M7**. Diese Datei konkretisiert den tatsächlichen Projektstand, die ausstehenden Arbeitspakete und eine risikoarme Reihenfolge. Keine Android-Codeänderung in diesem Dokumentations-PR.

## 1. Geprüfter Ist-Stand

| Etappe | Ist-Stand auf main | Nachweis |
|---|---|---|
| M7.0 Fundament | implementiert | vorausgehende M7-PRs |
| M7.1 Rückblick / Zeitfilter / Belege | implementiert | PR #24 |
| M7.2 vorsichtige Kombinationen / Belege | implementiert | PR #25 |
| M7.3.0–M7.3.1 Erkenntnismodell und koordinierte verschlüsselte EKB2-Sicherungen | implementiert | PR #28 |
| M7.3.2 Schreiben/Bearbeiten/Suche/Archiv/Verknüpfen | implementiert | PR #29 |
| M7.3.3 freiwillige Reflexionsfragen | implementiert | PR #30 |
| M7-Bereichs-UX-Runde | implementiert; 28 Compose-Testdurchläufe plus 3 Keystore-Backuptests erfolgreich | PR #31 |
| M7.4 Wissen ↔ Erkenntnisse ↔ Strategieexperimente | **noch nicht implementiert** | Original-Roadmap beschreibt diese Etappe, kein entsprechender Implementierungs-PR |
| M7.5 freiwillige Visualisierungen | **als Roadmap konzipiert, nicht implementiert** | PR #26 ergänzte nur die Planung |
| M7.6 unabhängige und manuelle Release-Abnahme | **nicht vollständig nachgewiesen** | automatisierte Teiltests vorhanden; Umfang der manuellen/externen Gates weiterhin offen |

**Vorbedingung für M7.4:** Die Strategiewelt M6.3 wurde bis zur UX-Qualitätsrunde integriert (u. a. 30 Strategie-Karten, Fachtexte und Verknüpfungen, PR #21). Das ist eine inhaltliche Basis, **keine** bereits geprüfte Wirksamkeitszuordnung. Die unabhängige redaktionelle/fachliche Prüfung bleibt als Qualitätsgate ausstehend.

## 2. Empfohlene Reihenfolge

### Schritt A — M7.4.0: kleiner UX-/Sicherheitsentwurf (als eigener Draft-PR)

Zuerst festlegen, wie ein freiwilliger, nicht wertender Ablauf aussieht:

1. Eine *vorhandene, kuratierte* Strategie aus dem Atlas selbst auswählen **oder** eine eigene Handlungsoption frei formulieren. Ein direktes Gefühl → Strategie-Matching ohne explizite Auswahl ist ausgeschlossen.
2. Freiwillig angeben, was man ausprobieren möchte; Status **geplant / ausprobiert / aktuell nicht passend / offen / beendet** ohne Erfolgsnote.
3. Nach Wunsch später nachspüren: Was hat sich verändert? Was war schwierig? Was möchte ich anders machen? Leere oder negative Ergebnisse sind genauso gültig.
4. Optional über **stabile geprüfte IDs** zu Wissensartikel/Strategieatlas/Bedürfnisatlas wechseln und zur vorherigen Stelle zurückkehren.
5. Klare Unterscheidung zwischen **Wissen**, **persönlicher Erkenntnis** und **eigener Strategie-Erprobung**; keine zusätzliche Hauptnavigation und kein Kausalversprechen.

**Verbindliches Datenschutz-/Backup-Gate vor persönlichem Speichern:** EKB2 deckt derzeit Journal + Erkenntnisse ab, nicht automatisch neue StrategyExperiment-Daten. Formatweiterentwicklung, Wiederherstellung auf zweitem Testgerät, EKB1/EKB2-Altimport ohne stillen Verlust neuer Datensätze, Sperrlogik, Edit/Delete und crash-konsistente Wiederherstellung zuerst spezifizieren und testen. Erst danach werden Experimente dauerhaft gespeichert.

**Exit:** redaktionell plausible UX, die freiwillige Ausführung und Nicht-Ausführung gleichwertig behandelt, stabile Link-Fallbacks, getestetes verschlüsseltes Roundtrip-Backup, Android-Tests bei 200 % Schrift und erfolgreiche Nutzerabnahme. Kein automatischer Merge.

### Schritt B — M7.5.0: Nutzenentscheidung zu Visualisierungen vor Code

Keine Diagramme nur um schöner auszusehen. Zuerst drei statische Vergleichsvarianten mit fiktiven Einträgen beurteilen:

- Text + kleine horizontale Balken für Nennungen;
- Text + neutrale Wochenansicht der **dokumentierten** Einträge/Nennungen;
- optional ein neutraler Eintragskalender, niemals „gute/schlechte Tage“.

Entscheidung je Variante: **bauen, vereinfachen oder bewusst auf spätere Version verschieben**, mit dokumentierter Begründung. Die Text-/Belegnavigation bleibt mindestens gleichwertig. Erst bei nachvollziehbarem Mehrwert die in der Roadmap definierten M7.5.1/M7.5.2-Teilschritte als separate Code-PRs bearbeiten. Geplante fünf freiwillige Verständlichkeits-Tests, 320 dp/200 % Schrift, TalkBack und Dark Mode nicht als bereits erfolgt ausgeben.

### Schritt C — M7.6: übergreifende Abnahme und Release-Risiken

- Regression auf 0/10/100/1000/3000 fiktiven Einträgen; gezielte Performance- und Zeitgrenzen-/DST-Tests.
- Echte Geräte: Navigation, Hardware-Zurück, Rotation, Dunkelmodus, 200 % Schrift, TalkBack, App-Sperre/Hintergrund, längere Texte.
- EKB1-/EKB2- und gegebenenfalls neuer Backup-Stand: falsches Passwort, beschädigte Sicherung, Import-Unterbrechung, zwei getrennte Geräte; kein stiller Datenverlust.
- Fachlich-redaktionelle Prüfung auf Suggestion, mögliche Stigmatisierung, irreführende Mustererklärungen und potenziell gefährliche Strategieempfehlungen; unabhängigen Sicherheitsreview **als offen dokumentieren**, bis er tatsächlich durchgeführt wurde.
- Usability mit mehreren freiwilligen Erstnutzern und ausschließlich fiktiven Daten; Feedback zum selbstständigen Finden der Funktionen und zu „Was wird gespeichert?“.
- Release nur, wenn keine offenen P0-Probleme und die vereinbarten Freigabegates nachweisbar erfüllt sind. Eine grüne CI ist wichtig, aber kein Ersatz für unabhängige Fach-/Sicherheitsabnahme.

### Schritt D — M8-Startentscheidung (erst nach priorisierter M7-Abnahme)

M8 im alten Umsetzungsplan listet App-Sperre, Erinnerungen, interaktives Körperbild und optional KI. **Eine optionale Gerätesperre ist bereits vorhanden**; deshalb nicht als neues Feature doppelt bauen. Die M8-Themen vor Implementierung in einzelnen ADRs nach Nutzermehrwert, Aufwand, Barrierefreiheit, Privacy und unbeabsichtigtem Druck priorisieren:

- Erinnerungen nur ausdrücklich freiwillig, ohne Schuldgefühle oder Streaks;
- Körperbild nur bei belegtem Nutzen und vorsichtiger Interpretation körperlicher Empfindungen;
- KI höchstens als gesonderte, zustimmungsbasierte Entscheidung nach starkem Datenschutzkonzept, nicht als Voraussetzung;
- für vorhandene App-Sperre eher UX-/Geräteabsicherung statt Neuerstellung.

## 3. Verbindliche Gates pro PR

1. Isolierter Draft-PR mit Nutzen, Umfang, Sicherheits-/Datenmodellentscheidung, Edge Cases und Akzeptanztests.
2. Grüne CI und, bei Android-Codeänderungen, signierte Debug-APK mit Commit-SHA.
3. Reale manuelle UX-Abnahme vor Freigabe; mit nur fiktiven Testdaten.
4. **Merge ausschließlich nach ausdrücklicher Genehmigung durch den Nutzer.**
5. Nach dem Merge die Roadmap und das Abschlussregister aktualisieren, bevor M8 beginnt.

## 4. Was jetzt als Erstes ansteht

**M7.4.0 – Wissen und Strategien mit selbstbestimmtem Ausprobieren verbinden.** Zuerst einen schlanken UX-/Sicherheitsentwurf, keine überdimensionierte Experiment-Tracking-App. Die existierenden M6.3-Karten und deren geprüfte ID-Referenzen wiederverwenden. Danach separate Implementierung und verschlüsselte Backup-Erweiterung. Die M7.5-Visualisierungen erst anhand eines nachgewiesenen Verständlichkeitsgewinns priorisieren.

## M7.4.0 – Konkreter Konzeptstand (noch nicht implementiert)

Der Funktions- und Sicherheitsentwurf liegt in [M7.4.0 Strategieexperimente UX-Konzept](M740_STRATEGIEEXPERIMENTE_UX_KONZEPT.md), ergänzt durch [ADR EKS1/EKB3/EKR2](adr/ADR_M740_STRATEGIEEXPERIMENTE_DATEN_UND_BACKUP.md) und die [Test- und Abnahmematrix](M740_ABNAHME_TESTMATRIX.md). Dies **ändert noch keine Android-Oberfläche und keinen Speicher**; vor M7.4.1 sind die Konzept- und Backup-Entscheidungen gesondert freizugeben.

**Zusätzlich aus der Codeprüfung:** Der aktuelle EKB2-Koordinator schützt Import/Export und mehrere Löschoperationen, aber `AppViewModel.saveCheckIn` schreibt weiterhin direkt über `EncryptedJournalRepository.save`. Vor einem dritten Speicher ist eine geschlossene Koordinationsgrenze einschließlich dieser Schreiboperation eine zwingende Sicherheitsvoraussetzung.

## 5. Quellen im Repository

- [M7-Funktions- und Umsetzungsplan](M7_RUECKBLICK_ERKENNTNISSE_PLAN.md)
- [M6.3 Strategiewelt-Konzept](M63_STRATEGIEWELT_KONZEPT.md)
- [M7-UX-Qualitätsrunde](M7_UX_QUALITAETSRUNDE.md)
- [Projektweiter Umsetzungsplan](UMSETZUNGSPLAN_V1.md)

**Status:** Dieser Plan ist ein korrigierender Dokumentationsentwurf, keine behauptete abgeschlossene M7-Release-Abnahme.
