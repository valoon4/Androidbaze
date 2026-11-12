# Kabelsalat · atomare Mikroaufgaben

**Stand:** 13.11.2026 · **Pilot – noch nicht alle offenen 869 Parent-Tasks zerlegt.**

## Regeln

- **KAB-XXXX bleibt das stabile übergeordnete Ticket.** Darunter einzelne Mikroschritte `KAB-XXXX/M01`, `M02` usw. Der kanonische Zähler bleibt **888 Parent-Tickets / 869 offen**; Mikrotasks werden getrennt gezählt. Keine künstliche Doppelzählung.
- **Ziel je Mikroschritt: 2–10 Minuten meiner aktiven Arbeit**, jeweils **eine Handlung mit überprüfbarem Ergebnis**. Nicht garantierte Laufzeiten; bei >10 Minuten vor Beginn weiter splitten, keine Einzeltasks mit 3 Stunden.
- **Test/Commit/Review sind echte eigene Schritte**, aber sinnvolle Änderungen dürfen gemeinsam committet werden. Keine sinnlosen 1-Zeilen-Commits, kein bloßes Aufblähen der Zahl.
- **Kein Abhaken ohne Test-/Commit-/Quellnachweis.** Ein Parent gilt erst als abgeschlossen, wenn die relevanten Mikroschritte samt Integration und Abnahme erledigt sind.
- **Externe Antworten sind kein 5-Minuten-Task:** Bei Bro-, Hersteller-/AVL- oder Originalplan-Abhängigkeiten nur meine Vorbereitungsarbeit schätzen; danach `blockiert: externe Entscheidung` sichtbar halten.
- **Fortlaufende, fachliche Zerlegung in kleinen Blöcken** nach Bros Rangliste statt automatisiert tausende generische Pseudotasks zu erzeugen. Vor jeder nächsten Implementierung die erforderlichen atomaren Schritte konkretisieren. Alte grobe 2.246-h-Schätzung nicht künstlich auf viele Kleinstaufgaben verteilen.

## Erste konkrete Mikroblöcke

### KAB-0885 · LR wirken zu lang – Bro-Sichtabnahme

*Geltungsbereich:* vollständiger aktueller Prüf- und Abschlussblock; externe Antwort von Bro nicht mit Zeit bepreisen.

- [ ] **KAB-0885/M01** · **5 Min.** – Code prüfen: 3D-Endmarkierung ist 160 mm, nicht mehr 360 mm **Fertig, wenn:** Beide Werte in `app.js` eindeutig kontrolliert.
- [ ] **KAB-0885/M02** · **5 Min.** – Code prüfen: Basic-Schachtansicht unterdrückt lange Kabelwege **Fertig, wenn:** 3D-Filter im `drawCable` nachgewiesen.
- [ ] **KAB-0885/M03** · **5 Min.** – Code prüfen: Kabelansicht blendet die langen Kabelwege wieder ein **Fertig, wenn:** Stage-Wechsel schaltet das Filtern zurück.
- [ ] **KAB-0885/M04** · **5 Min.** – Code prüfen: Profi-Modus lässt Kabelwege sichtbar **Fertig, wenn:** Keine versehentliche Filterung im Editor.
- [ ] **KAB-0885/M05** · **10 Min.** – Browser-Screenshot der Basic-Schachtansicht erzeugen **Fertig, wenn:** Datei oder CI-Artefakt mit Beton und LR-Öffnungen.
- [ ] **KAB-0885/M06** · **10 Min.** – Browser-Screenshot der Kabelansicht erzeugen **Fertig, wenn:** Datei oder CI-Artefakt mit beschrifteten Kabelsträngen.
- [ ] **KAB-0885/M07** · **10 Min.** – 2D- und 3D-Schachtansicht auf dieselben LR-Öffnungen vergleichen **Fertig, wenn:** Anzahl, Wandzuordnung und Bildpaar geprüft.
- [ ] **KAB-0885/M08** · **5 Min.** – Nachweis verlinken und Bro-Frage 'Kabel oder tatsächliche Rohrtiefe?' dokumentieren **Fertig, wenn:** Issue-Link + konkret offene Abnahmefrage. Warten nicht als KI-Zeit zählen.

### KAB-0881 · PDF-/Plantext: Rohrdimension als prüfbaren Kandidaten erfassen

*Geltungsbereich:* Teil A: isolierte Text-Erkennung und Zuordnung; echte PDF-Binär-/Vektorverarbeitung folgt in weiteren Mikroblöcken. Quelle und Negativfälle erforderlich.

- [ ] **KAB-0881/M01** · **5 Min.** – Bestehende Funktion `parsePlanText` und ihren Datenvertrag lesen **Fertig, wenn:** Eingabewerte, Ausgabefelder und Grenzen notiert.
- [ ] **KAB-0881/M02** · **5 Min.** – Positive Testzeile mit LR-ID und DN 50 formulieren **Fertig, wenn:** Fixture erwartet `candidateMm=50`.
- [ ] **KAB-0881/M03** · **5 Min.** – Positive Testzeile mit LR-ID und DN 110 ergänzen **Fertig, wenn:** Fixture erwartet `candidateMm=110`.
- [ ] **KAB-0881/M04** · **5 Min.** – Positive Testzeile mit 'DM 116' ergänzen **Fertig, wenn:** Test trennt DM-Angabe von geprüftem Außen-Ø.
- [ ] **KAB-0881/M05** · **5 Min.** – Test für DN 30 ergänzen **Fertig, wenn:** Unterer vom Bro gewünschter Beispieldurchmesser geprüft.
- [ ] **KAB-0881/M06** · **5 Min.** – Test für DN 160 ergänzen **Fertig, wenn:** Oberer vom Bro gewünschter Beispieldurchmesser geprüft.
- [ ] **KAB-0881/M07** · **5 Min.** – Test mit fehlender Rohr-ID anlegen **Fertig, wenn:** Ausgabe bleibt `pipeId` unbekannt statt erfunden.
- [ ] **KAB-0881/M08** · **10 Min.** – Test mit zwei Maßlabels zu derselben Rohr-ID anlegen **Fertig, wenn:** Mehrdeutigkeit wird erkannt; keine automatische Änderung.
- [ ] **KAB-0881/M09** · **5 Min.** – Test für Planzeilennummer und Quellenname ergänzen **Fertig, wenn:** Beide Metadaten an Kandidat gespeichert.
- [ ] **KAB-0881/M10** · **5 Min.** – Test: `h.dn` wird durch Planparser nicht überschrieben **Fertig, wenn:** Original-Nennweite bleibt unverändert.
- [ ] **KAB-0881/M11** · **10 Min.** – Test: fremde Rohr-ID beim Übernehmen blockieren **Fertig, wenn:** Kandidat wird nicht fälschlich auf gewähltes Rohr übertragen.
- [ ] **KAB-0881/M12** · **10 Min.** – Test: unklare Rohr-ID nur nach manueller Nachfrage zuordnen **Fertig, wenn:** Keine stillschweigende ID-Zuweisung.
- [ ] **KAB-0881/M13** · **5 Min.** – Test: Kandidatenstatus ist nie automatisch `reviewed` **Fertig, wenn:** Unbestätigte Maße bleiben Kandidaten.
- [ ] **KAB-0881/M14** · **10 Min.** – Test: Kandidat bleibt nach JSON-Roundtrip nachvollziehbar **Fertig, wenn:** Maß, Quelle, Zeile und Status erhalten.
- [ ] **KAB-0881/M15** · **5 Min.** – Hinweis 'keine PDF-Binärtext-Extraktion' im UI gegenprüfen **Fertig, wenn:** Kein erfundener OCR-/PDF-Anspruch.
- [ ] **KAB-0881/M16** · **10 Min.** – Erste synthetische Testreihe in CI aufnehmen **Fertig, wenn:** Tests aus `npm test` erreichbar.

### KAB-0882 · Foto: Größen nur mit Maßstabsbezug vorschlagen

*Geltungsbereich:* Teil A: bestehende heuristische Kreiserkennung und Kalibrierung testen; echte trainierte KI und Perspektivkorrektur erfordern weitere Blöcke/Beispielbilder.

- [ ] **KAB-0882/M01** · **5 Min.** – Bestehende `photo-candidates.js`-Schnittstellen und Grenzen lesen **Fertig, wenn:** Erkennung, `calibrate` und Status dokumentiert.
- [ ] **KAB-0882/M02** · **10 Min.** – Testbild ohne Kreis als synthetische Fixture bauen **Fertig, wenn:** Null Kandidaten ohne Absturz.
- [ ] **KAB-0882/M03** · **10 Min.** – Testbild mit einem dunklen Kreis bauen **Fertig, wenn:** Ein Kandidat mit Pixel-Ø.
- [ ] **KAB-0882/M04** · **10 Min.** – Testbild mit kleinem und großem Kreis bauen **Fertig, wenn:** Beide im relativen Größenvergleich erkannt.
- [ ] **KAB-0882/M05** · **5 Min.** – Test: keine mm-Angabe ohne Referenzkreis **Fertig, wenn:** Nur Pixel-Ø oder relative Größen.
- [ ] **KAB-0882/M06** · **10 Min.** – Test: gültiger Referenzkreis liefert Kandidaten-mm **Fertig, wenn:** Kalibrierter Kandidat, trotzdem unbestätigt.
- [ ] **KAB-0882/M07** · **5 Min.** – Test: Referenzmaß Null wird abgewiesen **Fertig, wenn:** Eindeutiger Fehler statt falscher mm.
- [ ] **KAB-0882/M08** · **5 Min.** – Test: nicht-numerisches Referenzmaß wird abgewiesen **Fertig, wenn:** Eindeutiger Fehler.
- [ ] **KAB-0882/M09** · **5 Min.** – Test: fehlende Referenz-Kreiswahl wird abgefangen **Fertig, wenn:** Keine automatische Kalibrierung.
- [ ] **KAB-0882/M10** · **10 Min.** – Test: Bild über 4 MP Analysefläche wird abgewiesen **Fertig, wenn:** Keine übermäßige Speicherbelegung.
- [ ] **KAB-0882/M11** · **5 Min.** – Test: PNG/JPEG/WebP-Filter im Upload prüfen **Fertig, wenn:** Nur unterstützte MIME-Typen.
- [ ] **KAB-0882/M12** · **5 Min.** – Test: 10-MB-Dateigrenze im Upload prüfen **Fertig, wenn:** Zu große Datei wird abgewiesen.
- [ ] **KAB-0882/M13** · **10 Min.** – Test: perspektivisch verzerrter Kreis nicht als sicher markieren **Fertig, wenn:** Hinweis auf Unsicherheit sichtbar.
- [ ] **KAB-0882/M14** · **5 Min.** – Test: Kandidat bleibt `candidate` nach `applyPhotoDimension` **Fertig, wenn:** Keine automatische Fachfreigabe.
- [ ] **KAB-0882/M15** · **5 Min.** – Test: Quelle enthält Bilddatei und Kreisnummer **Fertig, wenn:** Nachvollziehbarer Herkunftsbeleg.
- [ ] **KAB-0882/M16** · **5 Min.** – Negativhinweis 'keine trainierte KI, kein OCR' in UI prüfen **Fertig, wenn:** Fähigkeitsgrenzen ehrlich angegeben.
- [ ] **KAB-0882/M17** · **10 Min.** – Regressionslauf für synthetische Fotos ausführen **Fertig, wenn:** Testnachweis mit fehlgeschlagenen Fällen dokumentiert.
- [ ] **KAB-0882/M18** · **5 Min.** – Liste noch benötigter echter Referenzfotos anlegen **Fertig, wenn:** Konkreter Quelldaten-Blocker für nächsten Block.

**Pilot:** 42 konkrete Mikroaufgaben über 3 Parents; Summe der groben Einzelschätzungen 285 Minuten. Bei KAB-0881/0882 sind dies **nur die ersten Teilblöcke**, nicht die gesamten ursprünglich geschätzten Funktionen.
