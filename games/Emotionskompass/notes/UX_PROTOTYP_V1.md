# UX-Prototyp M0.5 – Umsetzungsnachweis und Designentscheidungen
Stand: 28.10.2026 · Status: entwickelt und technisch durchgespielt · Bezug: Produkt-Spezifikation v1.0

## 1. Ergebnis

Ein responsiver, vollständig clientseitiger HTML-Prototyp unter `games/Emotionskompass/prototype/index.html`. Direkt per Browser öffnbar. Optische Leitlinie: Design D (warm/natürlich × minimalistisch/elegant). Die zwei inhaltlichen Säulen haben gleichwertige Einstiege. Keine Datenerfassung oder Server-API.

**Unterschied zur nativen App:** In dieser Etappe werden Nutzerinteraktionen und UX-Verhalten validiert, nicht Android-Architektur oder Datensicherheit. Browserdaten sind nur temporäre Simulationsobjekte. Keine „persistenten“ oder „verschlüsselten“ Daten suggerieren.

## 2. Realisierte UX-Elemente

| ID | Element | Verhalten |
|---|---|---|
| UX-01 | Start | Duale Haupteinstiege, klarer Primary CTA, Entdeckungsimpuls |
| UX-02 | Navigation | Desktop-Sidebar / Smartphone-Bottom-Bar; Kontext-Back in Flows |
| UX-03 | Atlas | Acht Familien und 36 echte textliche Demo-Einträge; alphabetische/semantische Suche im lokalen Datensatz |
| UX-04 | Detail | Begriff, Beschreibung, Beispiel, verwandte/kontrastierende Begriffe, Compare/Reflektieren |
| UX-05 | Vergleich | Zwei frei wechselbare Emotionen, Unterschiede und Überschneidungen, Frustration/Enttäuschung besonders erläutert |
| UX-06 | Check-in | Zeitumfang kurz/tief; drei Ausgangspunkte, Valenz/Aktivierung optional, 0–n Gefühle |
| UX-07 | Mehrfachauswahl | Pro Emotion optionale Intensität; Fragezeichen bedeutet „nicht eingeschätzt“ |
| UX-08 | Situation zuerst | Bei entsprechendem Pfad Beginn mit freiwilligem Situationstext, danach emotionale Einordnung |
| UX-09 | Reflexion | Sechs freiwillige Impulsfragen; Weiter/Zurück/Überspringen; Texte bleiben im Session-State |
| UX-10 | Abschluss | Übersicht der Emotionen, Wahrnehmung und Antworten, Änderungswege, freiwillige Übernahme |
| UX-11 | Tagebuch | Temporäre Session-Einträge ansehen, bearbeiten, löschen; Demo-Beispiel explizit markiert |
| UX-12 | Rückblick/Info | Zukunftszustand, Erklärung des Prototypen und Reset |

## 3. Browser-Smoke-Test – Ergebnis

**Testumgebung:** Headless Chromium, Playwright Python, direktes HTML über `page.set_content` ohne Netzwerk; Viewports 390×844 (Smartphone) und 1280×900 (Desktop).

**Automatisch erfolgreich durchgespielt:**
1. Home > Atlas > Suche „frust“ (Begriffsname wird priorisiert) > Frustration > Vergleich.
2. Langer Check-in > „gemischt“ > „viel Energie“ > Auswahl „Frustration“ + „Enttäuschung“ > Detailansicht aus Auswahl > zurück, beide Begriffe bleiben.
3. Freitext „Situation“ > alle Folgefragen > Zusammenfassung > temporär speichern > Detail > bearbeiten > keine Dublette > löschen.
4. Kurzer Check-in > Wahrnehmung und Emotion überspringen > gültige Zusammenfassung > ohne Speichern beenden.
5. Pfad „Situation zuerst“ > Freitext > Einordnung überspringen > Reflexion startet bei Gedanken; vorheriger Situationstext erscheint in Zusammenfassung.
6. Desktop-Layout mit sichtbarer Sidebar; keine JavaScript-Laufzeitfehler in den Testläufen.

**Nicht geprüft:** reelle Nutzertests, vollständig automatisierte Barrierefreiheitsprüfung, reale Android-Back-Navigation, Offline-APK, verschlüsselte Datenhaltung, CI-Build. Dies darf in der Kommunikation nicht als abgeschlossen bezeichnet werden.

## 4. Entscheidungen aus M0.5

- Einstieg über *Anliegen* statt zuerst einer riesigen Wortliste.
- Schneller und tiefer Pfad werden im Check-in explizit vorab gewählt.
- Emotionssuche bewertet treffende Namen höher als bloße Erwähnungen in Definitionen.
- Emotionen können unklar, gleichzeitig oder intensitätslos bleiben.
- Der Wechsel von Reflexion zur Emotionsdetailseite und zurück erhält die Auswahl.
- Eine Situation kann vor der Gefühlsauswahl beschrieben werden; die Frage wird anschließend nicht zwingend doppelt gestellt.
- Einträge werden erst durch ausdrückliche Bestätigung in die temporäre Demo-Timeline übernommen.
- Keine täuschende Erfassung echter persönlicher Daten im Designprototyp.

## 5. UX-Review vor Start M1

- Interaktiven Ablauf auf schmalen Android-Displays mit Nutzerfeedback prüfen.
- Verständlichkeit des Übergangs von schnellem zu ausführlichem Check-in prüfen.
- Prüfen, ob Begriffe als *Hilfsangebote* und nicht als Diagnosen wirken.
- Entscheidung treffen, ob Atlas zuerst Suchfeld oder visuelle Emotionsfamilien zeigen soll.
- Fachreview der späteren mindestens 80 Einträge und Unterscheidungen einplanen.
- Kontrastmessungen, Zoom/Schriftvergrößerung und assistive Navigation auf realen Geräten durchführen.
- Keine MVP-Freigabe ohne kryptografische Persistenz-, Backup- und Löschtests.

## 6. Architektur-Fortführung

Die HTML-Demo ist **kein** Teil der nativen Laufzeit; kein Einbetten als produktive WebView empfohlen. M1 setzt die geprüften Abläufe mit Kotlin/Jetpack Compose neu um. Die getrennten Screen-Routen und Demo-Datensätze dienen als Referenz. Keine Änderungen an Androidbaze-Root-Projekt oder TimeFlow.
