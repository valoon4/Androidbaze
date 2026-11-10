# Kabelsalat · 828-Task-Backlog · Teil 4/4: P3 – Tests, Betrieb und Zusammenarbeit

**Status:** 180 neue Aufgaben offen. IDs KAB-0649–KAB-0828. **Hinweis:** P3 ist der Strukturblock für QA/Betrieb, **nicht** die Aussage, Tests dürften bis zum Schluss warten: Sicherheits- und Regressionsprüfungen sind für jeden relevanten Sprint Pflicht. Quellen und Regeln im [Task-Index](./README.md).

## P3-01 · Modell- und Geometrie-Regressionstests (KAB-0649–KAB-0666)

**Quelle:** tests/*.test.js, V2_0_STATUS. **Definition of Done:** Automatisierte Tests mit eindeutigem Fehlerfall und unveränderten Referenzfixtures.

- [ ] **KAB-0649** – Test für PDF-V2-Rohrzahl 91 festschreiben
- [ ] **KAB-0650** – Test für SW1-Rohrzahl 29 festschreiben
- [ ] **KAB-0651** – Test für SW2-Rohrzahl 13 festschreiben
- [ ] **KAB-0652** – Test für SW3-Rohrzahl 26 festschreiben
- [ ] **KAB-0653** – Test für SW4-Rohrzahl 23 festschreiben
- [ ] **KAB-0654** – Test für unveränderte Quellbelegung nach Demo-Verdichtung ergänzen
- [ ] **KAB-0655** – Test für originalen sourceU/sourceV-Wert nach JSON-Roundtrip ergänzen
- [ ] **KAB-0656** – Test für geraden Kabelweg zweier gegenüberliegender Rohre ergänzen
- [ ] **KAB-0657** – Test für schrägen knickfreien Kabelweg ergänzen
- [ ] **KAB-0658** – Test für seitlichen Bezier-Wandwechsel ergänzen
- [ ] **KAB-0659** – Test für nicht belegten Kabelendpunkt ergänzen
- [ ] **KAB-0660** – Test für Wandnormale und inverse Transformation ergänzen
- [ ] **KAB-0661** – Test für Rohr-Kollision nach Abstandsverdichtung ergänzen
- [ ] **KAB-0662** – Test für OLA-Stapelauflager Anzahl und Lage ergänzen
- [ ] **KAB-0663** – Test für MS-Stützen Anzahl und Lage ergänzen
- [ ] **KAB-0664** – Test für Kabeldurchführungen in Stirnplatten ergänzen
- [ ] **KAB-0665** – Test für Fremdleerrohr als Kanalblocker ergänzen
- [ ] **KAB-0666** – Test für reproduzierbare Projektmigration alter Versionen ergänzen

## P3-02 · Echte Browser- und visuelle Qualitätssicherung (KAB-0667–KAB-0684)

**Quelle:** tests/browser-visual.js, Workflow. **Definition of Done:** Erwartete Anzeige im headless Chromium belegt; Screenshots als CI-Artefakte.

- [ ] **KAB-0667** – Echte Chrome-Seite nach vollständigem JS-Start prüfen
- [ ] **KAB-0668** – WebGL-Start ohne Konsolenfehler als Regression prüfen
- [ ] **KAB-0669** – Browser-Fallback ohne WebGL als Regression prüfen
- [ ] **KAB-0670** – Schachtübersicht mit 91 Rohren screenshotten
- [ ] **KAB-0671** – Einzelansicht SW1 mit PDF-V2 vergleichen
- [ ] **KAB-0672** – Einzelansicht SW2 mit PDF-V2 vergleichen
- [ ] **KAB-0673** – Einzelansicht SW3 mit PDF-V2 vergleichen
- [ ] **KAB-0674** – Einzelansicht SW4 mit PDF-V2 vergleichen
- [ ] **KAB-0675** – 3D-Ansicht der roten OLA-Variante screenshotten
- [ ] **KAB-0676** – 3D-Ansicht der blauen MS-Variante screenshotten
- [ ] **KAB-0677** – Kameradrehung mit sichtbar gerader Kabelstrecke prüfen
- [ ] **KAB-0678** – Transparente Platten gegen undurchsichtige Version vergleichen
- [ ] **KAB-0679** – Mehrfachbelegte Rohre per Klick auslesen
- [ ] **KAB-0680** – Projektliste nach Browser-Reload auf Korrektheit prüfen
- [ ] **KAB-0681** – Basic/Profi-Wechsel mit echten UI-Klicks testen
- [ ] **KAB-0682** – Beide Seitenleisten einklappen und Editor bedienen
- [ ] **KAB-0683** – Fehlende Font-/Asset-Ladevorgänge im Browserprotokoll melden
- [ ] **KAB-0684** – Visuellen Teststatus mit zugehörigem Commit verlinken

## P3-03 · Performance und große Planungen (KAB-0685–KAB-0702)

**Quelle:** Schachtnetz, WebGL, Browser. **Definition of Done:** Messwerte und feste Datensätze statt vermuteter Geschwindigkeit; keine Features verlieren.

- [ ] **KAB-0685** – 3D-Renderzeit bei 91 Rohren benchmarken
- [ ] **KAB-0686** – 3D-Renderzeit bei 500 Rohren benchmarken
- [ ] **KAB-0687** – 3D-Renderzeit bei 2000 Rohren benchmarken
- [ ] **KAB-0688** – 3D-Renderzeit bei 500 Kabeln benchmarken
- [ ] **KAB-0689** – 3D-Renderzeit bei 5000 Kabeln benchmarken
- [ ] **KAB-0690** – 2D-Renderzeit bei 600 Rohren auf einer Wand messen
- [ ] **KAB-0691** – Netzkarte bei 30 Schächten benchmarken
- [ ] **KAB-0692** – Netzkarte bei 100 Schächten benchmarken
- [ ] **KAB-0693** – Globales Suchfeld mit 10000 IDs benchmarken
- [ ] **KAB-0694** – Speicherverbrauch nach 50 Schachtwechseln prüfen
- [ ] **KAB-0695** – WebGL-Geometrien bei Redraw korrekt freigeben
- [ ] **KAB-0696** – Texturressourcen nach Labelwechsel korrekt freigeben
- [ ] **KAB-0697** – Raycaster-Zielobjekte bei Neuzeichnung bereinigen
- [ ] **KAB-0698** – PDF-Vektorvergleich ohne WebGL-Initialisierung messen
- [ ] **KAB-0699** – Projektimportzeit für 5-MB-JSON messen
- [ ] **KAB-0700** – Autosave-Dauer für großes Netzprojekt messen
- [ ] **KAB-0701** – UI-Eingabe-Verzögerung bei stark belegten Schächten messen
- [ ] **KAB-0702** – Performance-Regressionsgrenzen in CI nachvollziehbar definieren

## P3-04 · Sicherheit und Robustheit (KAB-0703–KAB-0720)

**Quelle:** CSP, Electron, reports.js. **Definition of Done:** Sichere Defaults und reproduzierbare Negativtests für fremde Projektinhalte.

- [ ] **KAB-0703** – Projektname beim Einfügen ins HTML escapen
- [ ] **KAB-0704** – Kabelname beim Einfügen ins HTML escapen
- [ ] **KAB-0705** – Wandname beim Einfügen ins HTML escapen
- [ ] **KAB-0706** – Quellenvermerk beim Einfügen ins HTML escapen
- [ ] **KAB-0707** – Benutzerkommentar beim Einfügen ins HTML escapen
- [ ] **KAB-0708** – Unbekannte Projektfelder bei Import kontrolliert behandeln
- [ ] **KAB-0709** – Übergroße JSON-Datei mit klarer Fehlermeldung ablehnen
- [ ] **KAB-0710** – Zyklische Objektstruktur vor Serialisierung verhindern
- [ ] **KAB-0711** – Gefälschte Kabelreferenz außerhalb des Projekts verwerfen
- [ ] **KAB-0712** – Pfadtraversal in Export-Dateinamen verhindern
- [ ] **KAB-0713** – Content-Security-Policy der Offline-Einzeldatei testen
- [ ] **KAB-0714** – Keine remote ausführbaren Skripte ins Browserpaket aufnehmen
- [ ] **KAB-0715** – Electron-Kontextisolierung und Node-Zugriff prüfen
- [ ] **KAB-0716** – Externen Link mit sicherem rel-Attribut öffnen
- [ ] **KAB-0717** – Alte Browser-LocalStorage-Daten bei Parsefehler sicher behandeln
- [ ] **KAB-0718** – HTML-Viewer gegen Skript-Injektion testen
- [ ] **KAB-0719** – Debug-Logs auf personenbezogene Projektdaten prüfen
- [ ] **KAB-0720** – Abhängigkeitenliste auf bekannte Sicherheitswarnungen prüfen

## P3-05 · Barrierefreiheit und Tastaturbedienung (KAB-0721–KAB-0738)

**Quelle:** EUGI_MODE_ACCEPTANCE. **Definition of Done:** Fokus und Eingaben mit Tastatur und lesbarer Beschriftung überprüft.

- [ ] **KAB-0721** – Tab-Reihenfolge der Hauptnavigation durchgehen
- [ ] **KAB-0722** – Tab-Reihenfolge der Rohr-Eigenschaften prüfen
- [ ] **KAB-0723** – Tab-Reihenfolge der Kabel-Eigenschaften prüfen
- [ ] **KAB-0724** – Tab-Reihenfolge der Platten-Eigenschaften prüfen
- [ ] **KAB-0725** – Tastaturzugriff auf Auswahl des aktuellen Schachts ermöglichen
- [ ] **KAB-0726** – Tastaturzugriff auf Auswahl der aktuellen Wand ermöglichen
- [ ] **KAB-0727** – Tastaturzugriff auf Auswahl des aktuellen Rohres ermöglichen
- [ ] **KAB-0728** – Tastaturzugriff auf Auswahl des aktuellen Kabels ermöglichen
- [ ] **KAB-0729** – Escape zum Schließen des PDF-Vergleichs prüfen
- [ ] **KAB-0730** – Escape zum Schließen der Präsentation prüfen
- [ ] **KAB-0731** – Screenreader-Label für alle Werkzeugsymbole setzen
- [ ] **KAB-0732** – Screenreader-Beschriftung der Rohrbelegung ausgeben
- [ ] **KAB-0733** – Farbunterscheidung MS/OLA zusätzlich textuell absichern
- [ ] **KAB-0734** – Kontrast der Warnmeldungen gegen WCAG-Orientierung testen
- [ ] **KAB-0735** – Fokus bei geschlossenem Dialog an Auslöser zurückgeben
- [ ] **KAB-0736** – Layout bei 125-Prozent-Windows-Skalierung testen
- [ ] **KAB-0737** – Layout bei 150-Prozent-Windows-Skalierung testen
- [ ] **KAB-0738** – Mobile schmale Ansicht ohne verdeckten Bearbeitungsbutton prüfen

## P3-06 · Offline-Browser und Electron-Pakete (KAB-0739–KAB-0756)

**Quelle:** package.json, build-standalone.js. **Definition of Done:** Alle Pakete starten offline und laden dieselbe Version und alle benötigten Module.

- [ ] **KAB-0739** – Browser-Offline-ZIP ohne Netzwerkstart testen
- [ ] **KAB-0740** – Offline-ZIP enthält construction-v2.js prüfen
- [ ] **KAB-0741** – Offline-ZIP enthält pdf-geometry.js prüfen
- [ ] **KAB-0742** – Offline-ZIP enthält pdf-visual.js prüfen
- [ ] **KAB-0743** – Offline-ZIP enthält alle aktuellen Beispiele prüfen
- [ ] **KAB-0744** – Offline-ZIP enthält Three.js und Lizenzhinweis prüfen
- [ ] **KAB-0745** – Offline-Einzeldatei mit eingebetteten Skript-Hashes bauen
- [ ] **KAB-0746** – Offline-Einzeldatei ohne externen Request öffnen
- [ ] **KAB-0747** – Windows-EXE auf aktuelle Electron-Version bauen
- [ ] **KAB-0748** – Windows-EXE ohne Installation auf Testsystem starten
- [ ] **KAB-0749** – Windows-EXE mit lokalen JSON-Dateien öffnen
- [ ] **KAB-0750** – Windows-EXE im geschlossenen Netzwerk betreiben
- [ ] **KAB-0751** – Projektexport zwischen Browser-ZIP und EXE vergleichen
- [ ] **KAB-0752** – Projektimport zwischen EXE und Einzeldatei vergleichen
- [ ] **KAB-0753** – Fehlende Skriptdatei mit verständlicher Startdiagnose melden
- [ ] **KAB-0754** – Versionsnummer in HTML, Paket und Cache-Parametern synchron halten
- [ ] **KAB-0755** – Alter Projektstand vor Versionswechsel exportierbar halten
- [ ] **KAB-0756** – Download-Artefakte maschinell auf vollständige Dateiliste prüfen

## P3-07 · GitHub-Workflows und Releases (KAB-0757–KAB-0774)

**Quelle:** GitHub Actions, Releases. **Definition of Done:** Grüner Workflow plus überprüfbare Artefakte; keine Freigabe allein aus grünem Build.

- [ ] **KAB-0757** – Pages-Workflow mit npm-Test als Pflichtschritt erhalten
- [ ] **KAB-0758** – Windows-Workflow mit npm-Test als Pflichtschritt erhalten
- [ ] **KAB-0759** – Browser-Visual-Workflow auf aktuellem Chromium ausführen
- [ ] **KAB-0760** – Abgebrochene CI-Läufe von fehlgeschlagenen getrennt anzeigen
- [ ] **KAB-0761** – Actions-Log bei Syntaxfehler eindeutig annotieren
- [ ] **KAB-0762** – Versionsname der Release-Dateien aus package.json ableiten
- [ ] **KAB-0763** – Browser-ZIP-Namen mit Versionsnummer prüfen
- [ ] **KAB-0764** – Windows-EXE-Namen mit Versionsnummer prüfen
- [ ] **KAB-0765** – Einzeldatei-Namen mit Versionsnummer prüfen
- [ ] **KAB-0766** – Release-Versand nur nach erfolgreichem Build erlauben
- [ ] **KAB-0767** – GitHub Pages mit HEAD-Commit SHA im Abnahmeprotokoll verknüpfen
- [ ] **KAB-0768** – Pages-Cache nach Versionwechsel gezielt invalidieren
- [ ] **KAB-0769** – Veröffentlichung auf korrekte Hauptanwendungs-URL prüfen
- [ ] **KAB-0770** – Release-Datei gegen SHA256-Checksumme verifizieren
- [ ] **KAB-0771** – Alte Release-Artefakte ohne Datenverlust archivieren
- [ ] **KAB-0772** – Fehlenden Windows-Build als offenen Release-Blocker markieren
- [ ] **KAB-0773** – Visuelle QA getrennt von Softwaretests als Status führen
- [ ] **KAB-0774** – Versions-Changelog automatisch aus genehmigten Task-IDs erstellen

## P3-08 · Entwicklerdokumentation und Datenverträge (KAB-0775–KAB-0792)

**Quelle:** README, ROADMAP, CONCEPT_AUDIT. **Definition of Done:** Beispiele, Grenzen, Migrationspfade und offene Entscheidungen finden sich ohne Chatverlauf.

- [ ] **KAB-0775** – Repository-Einstieg auf aktuelle Kabelsalat-Version aktualisieren
- [ ] **KAB-0776** – Schacht- und Wand-Datenmodell vollständig dokumentieren
- [ ] **KAB-0777** – Rohr-ID-Format samt Trassenkonvention dokumentieren
- [ ] **KAB-0778** – Kabel-ID-Format samt Endpunktkonvention dokumentieren
- [ ] **KAB-0779** – Globale und lokale Koordinatensysteme illustrieren
- [ ] **KAB-0780** – PDF-V2-Quellseiten und Schematisierung dokumentieren
- [ ] **KAB-0781** – Referenz-HTML und PDF-V2-Modell bewusst unterscheiden
- [ ] **KAB-0782** – OLA-Variante A mit schematischer Bauteilliste dokumentieren
- [ ] **KAB-0783** – MS-Variante B mit schematischer Bauteilliste dokumentieren
- [ ] **KAB-0784** – Status offen versus umgesetzt für 31 Audit-Anforderungen aktualisieren
- [ ] **KAB-0785** – Versionsmigration und JSON-Backup-Schritte dokumentieren
- [ ] **KAB-0786** – LocalStorage-Schlüssel und Rücksetzverhalten dokumentieren
- [ ] **KAB-0787** – Browser-ZIP-Startanleitung für Windows aktualisieren
- [ ] **KAB-0788** – EXE-Startanleitung für Windows aktualisieren
- [ ] **KAB-0789** – Automatisierte Tests und Reproduktionsbefehle dokumentieren
- [ ] **KAB-0790** – Bekannte technische Grenzen in einer kanonischen Liste führen
- [ ] **KAB-0791** – Screenshot-Beispiele für vier PDF-Schachtwände bereitstellen
- [ ] **KAB-0792** – Handreichung zur technischen Fachabnahme ohne Freigabeversprechen verfassen

## P3-09 · Bros Feedback, Review und Priorisierung (KAB-0793–KAB-0810)

**Quelle:** Nutzerfeedback 08.–10.11.2026. **Definition of Done:** Äußerungen in testbare Anforderungen überführt, Unklarheiten sichtbar statt erfunden.

- [ ] **KAB-0793** – Originalchat vom Bro als separate Quelle mit Datum erfassen
- [ ] **KAB-0794** – Feedbackaussagen von Softwarevorschlägen des Assistenten trennen
- [ ] **KAB-0795** – Jede fachliche Korrektur einem konkreten Schacht zuordnen
- [ ] **KAB-0796** – Feedback zur Schachtwandbelegung mit PDF-Seite verknüpfen
- [ ] **KAB-0797** – Feedback zur Rohrabstandsanpassung als UI-Präferenz oder Maßfrage markieren
- [ ] **KAB-0798** – Feedback zu knickfreien Kabeln als Geometrieanforderung markieren
- [ ] **KAB-0799** – Feedback zur roten OLA-Konstruktion mit Referenzbild verknüpfen
- [ ] **KAB-0800** – Feedback zur blauen MS-Konstruktion mit Referenzbild verknüpfen
- [ ] **KAB-0801** – Unbestätigte Anforderungen als offene Rückfrage notieren
- [ ] **KAB-0802** – Neue Feedbackaufgabe mit unveränderlicher KAB-ID versehen
- [ ] **KAB-0803** – Feedback-Duplikate gegen vorhandene Tasks prüfen
- [ ] **KAB-0804** – Regression nach jeder Bro-Korrektur dokumentieren
- [ ] **KAB-0805** – Vorher-/Nachher-Screenshot je wesentlicher Bro-Korrektur sichern
- [ ] **KAB-0806** – Bro-Abnahmeentscheidung als Kommentar und Status speichern
- [ ] **KAB-0807** – Ablehnung einer Variante ohne Löschen der Vorgängerfassung erfassen
- [ ] **KAB-0808** – Priorität nach Nutzwert statt nach bloßer Neuheit bestimmen
- [ ] **KAB-0809** – Feedback aus zukünftigen Bro-Chats ohne feste Obergrenze ergänzen
- [ ] **KAB-0810** – Nach jedem Sprint sichtbaren Bro-Testlauf planen

## P3-10 · Aufgabentracker, Sprintplanung und Messung (KAB-0811–KAB-0828)

**Quelle:** TASKS, ROADMAP, Audit. **Definition of Done:** Eindeutige IDs, nachvollziehbare Statusupdates und keine künstlichen Fertigzahlen.

- [ ] **KAB-0811** – Kanonische Task-ID-Serie KAB-0001 bis KAB-0828 validieren
- [ ] **KAB-0812** – Aufgaben-IDs gegen doppelte Nummern prüfen
- [ ] **KAB-0813** – Aufgaben-IDs auf lückenlose Sequenz prüfen
- [ ] **KAB-0814** – Alle neuen Aufgaben initial als offen führen
- [ ] **KAB-0815** – Erledigte Aufgaben nur mit Commit-/Testnachweis markieren
- [ ] **KAB-0816** – In Arbeit befindliche Aufgabe separat kennzeichnen
- [ ] **KAB-0817** – Blockierte Aufgabe mit Abhängigkeitsgrund kennzeichnen
- [ ] **KAB-0818** – Prioritäten P0–P3 verständlich dokumentieren
- [ ] **KAB-0819** – Task-ID in künftigen Commit-Nachrichten referenzieren
- [ ] **KAB-0820** – Sprintauswahl aus offenen P0-Aufgaben treffen
- [ ] **KAB-0821** – Abhängigkeiten vor Start einer Aufgabe prüfen
- [ ] **KAB-0822** – Abnahmeprotokoll zu jeder erledigten Aufgabe verlinken
- [ ] **KAB-0823** – Gleichbedeutende Aufgaben nach Review zusammenführen ohne IDs zu recyceln
- [ ] **KAB-0824** – Anforderung aus Bros Chat bei Bedarf als neue fortlaufende ID anlegen
- [ ] **KAB-0825** – Offene und erledigte Aufgaben per Skript zählen
- [ ] **KAB-0826** – Fortschritt nach Epic und Priorität ausgeben
- [ ] **KAB-0827** – Langzeitmeilensteine getrennt von tatsächlicher Taskquote führen
- [ ] **KAB-0828** – Nach 828er-Basisplan die nächste konkrete Aufgabe festlegen

