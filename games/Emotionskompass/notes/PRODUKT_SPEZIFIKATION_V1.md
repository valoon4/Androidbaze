# Emotionskompass – verbindliche Produktspezifikation v1.0
Stand: 28.10.2026 · Status: Baseline für die inkrementelle Umsetzung · Designvariante D

## 1. Vision, Nutzen, Grenzen

**Produktversprechen:** „Ich weiß noch nicht, was ich fühle – diese App hilft mir, es selbst herauszufinden.“

Emotionskompass ist ein privates deutschsprachiges Lern- und Selbstreflexionswerkzeug für Erwachsene. Es verbindet zwei gleichwertige Anwendungszwecke:

1. **Emotionen verstehen:** Gefühle benennen, Nuancen vergleichen, Wortschatz aufbauen.
2. **Eigene Emotionen erkunden:** Wahrnehmen, Situationen einordnen, Bedürfnisse reflektieren, Erfahrungen festhalten.

**Nicht-Ziele:** Kein Diagnosetool, keine Therapie, kein „Glücksscore“, keine Aussage über psychische Erkrankungen, keine automatische Deutung ohne Zustimmung, kein Gamification-Druck, keine Community, keine Konto-/Cloud-Pflicht, kein KI-Chat im MVP.

**Produktprinzipien:** Neugier statt Bewertung; Möglichkeiten statt Zuschreibungen; Offenheit für Ambivalenz; jederzeit überspringbar; Datensouveränität; inklusiv und ruhig; Alltagstauglichkeit in kurzen wie langen Sitzungen. „Emotion“ und „körperliche Empfindung“ sind nicht gleichzusetzen; individuelle Unterschiede müssen sichtbar bleiben.

**Zielgruppe:** Primär deutschsprachige Erwachsene, die ihren emotionalen Wortschatz differenzieren oder Erfahrungen reflektieren möchten. Vorerfahrung mit Psychologie ist nicht erforderlich. Keine Minderjährigen-Fokussierung, kein klinischer Einsatz im MVP.

## 2. Funktionsumfang und Prioritäten

Kennzeichnung: **P0 = für ersten öffentlich nutzbaren MVP zwingend**, **P1 = nächster Ausbau**, **P2 = optional/später**.

| ID | Funktion | Priorität | Verbindliche Anforderung |
|---|---|---|---|
| F01 | Onboarding | P0 | Maximal drei optionale Einführungsschritte; „ohne Konto beginnen“; keine personenbezogene Pflichtangabe. |
| F02 | Start | P0 | Zwei gleichwertige Einstiege: „Gefühl erkunden“ und „Emotionen entdecken“; zuletzt genutzt; diskrete Einstellungen. |
| F03 | Emotionsatlas | P0 | Themenfamilien, alphabetische Liste, lokale Volltextsuche (Name/Synonyme), Filter, Offline-Verfügbarkeit. |
| F04 | Emotionsdetails | P0 | Definition, Beispiele, typische Varianten, verwandte Begriffe, Abgrenzung, „In Reflexion übernehmen“. |
| F05 | Begriffsvergleich | P0 | Zwei Emotionen nebeneinander, Überschneidungen und Unterschiede; keine objektive „korrekte“ Zuschreibung. |
| F06 | Schneller Check-in | P0 | Einstieg ohne Gefühlswissen, emotionale Valenz/Aktivierung optional, ein oder mehrere Begriffe, Intensität optional, Speichern/Verwerfen. |
| F07 | Vertiefende Reflexion | P0 | Situation, Gedanken, Körperwahrnehmung, vermutete Bedürfnisse, Reaktion, Erkenntnis; jede Frage optional. |
| F08 | Zusammenfassung | P0 | Alle Angaben vor Speichern prüfen/bearbeiten; „nicht sicher“ bleibt zulässig. |
| F09 | Tagebuch | P0 | Chronologie, Detailansicht, Suche/Filter, Eintrag bearbeiten/löschen, mehrere Gefühle je Eintrag. |
| F10 | Datensouveränität | P0 | Lokale Speicherung, sichere Datenhaltung, vollständige Löschung, bewusster manueller Export; keine Anmeldung. |
| F11 | Barrierefreiheit | P0 | Screenreader, skalierbare Schrift, hohe Kontraste, nicht allein farbliche Kodierung, min. 48dp Tap-Ziele. |
| F12 | Wortschatzübungen | P1 | Freiwilliges Mini-Quiz mit mehreren plausiblen Antworten, „Emotion des Tages“, Favoriten, Lernhistorie. |
| F13 | Rückblick | P1 | Eigene Emotionshäufigkeiten, zeitliche Verteilung, häufige Kontexte; ausdrücklich keine Diagnosen. |
| F14 | Freiwillige Erinnerung | P1 | Opt-in, konfigurierbare Häufigkeit/Zeit; neutraler Benachrichtigungstext. |
| F15 | App-Sperre | P1 | Lokale biometrische/gerätegebundene Sperre, angemessenes Wiederherstellungsverhalten. |
| F16 | Körperbild | P2 | Optional lokalisierte Empfindungen; nie Schlussfolgerung „Körperstelle = Emotion“. |
| F17 | KI-Reflexionshilfe | P2 | Nur mit separater ausdrücklicher Einwilligung und transparenter Datenweitergabe; jederzeit deaktivierbar. |
| F18 | Verschlüsselte Synchronisierung | P2 | Erst nach Sicherheits-/Datenschutzkonzept und separater Entscheidung. |

**Inhaltsumfang:** Bei Start des MVP in der Entwicklungsumgebung mindestens 24 redaktionell vollständige Einträge; für einen breiten Release mindestens 80 inhaltlich geprüfte deutschsprachige Begriffe. Die Liste ist eine Orientierung, keine universelle psychologische Taxonomie.

## 3. Informationsarchitektur und Screen-Vertrag

**Hauptnavigation:** Start | Entdecken | Tagebuch | Rückblick (P1; bis dahin erklärter Platzhalter oder Navigation mit nur drei Tabs). Primärer CTA „Gefühl erkunden“ auf Start dauerhaft sichtbar. Einstellungen über Kopfzeile. Kein Tab für jede Unteransicht.

| Screen-ID | Titel | Inhalte | Interaktionen | Zustände und Randfälle |
|---|---|---|---|---|
| S01 | Willkommen | Ziel und zwei Wege, kurzer Datenschutzhinweis | Überspringen/Weiter/Loslegen | Erneut über Hilfe erreichbar; kein Login-Popup |
| S02 | Start | Große gleichrangige Karten „Gefühl erkunden“/„Emotionen entdecken“, optional Entdeckung des Tages | CTA öffnen, Eintrag fortsetzen, Einstellungen | Erster Start, zuletzt genutzter Zustand, keine Tagebuchdaten |
| S03 | Atlas | Suchfeld, 8 Themenfamilien, Listenansicht, Favoritenfilter später | Suchen, navigieren, filtern | Keine Treffer mit Synonym-Hinweis und Löschen-Filter |
| S04 | Emotionsdetail | Begriff, alltagsnahe Definition, typische Situationen, mögliche Empfindungen, ähnliche/kontrastierende Gefühle | Vergleichen, zur Reflexion übernehmen, zurück | Aus beliebigem Einstieg Rücknavigation ohne Eingabeverlust |
| S05 | Vergleich | Begriff A / B, Gemeinsamkeiten, Unterschiede, 2 Alltagsszenarien | Begriffe wechseln, im Atlas öffnen | Keine unhaltbaren psychologischen Gewissheiten |
| S06 | Check-in Einstieg | „Unklar“, „ungefähre Ahnung“, „Situation reflektieren“; Schnell/Tief | Pfad wählen | Alle Pfade ohne Pflichtantwort |
| S07 | Eingrenzen | Optional angenehm–unangenehm, ruhig–aktiviert, freie Notiz/Körpersignal | Auswahl, weiter, überspringen | Neutral, gemischt, unklar ausdrücklich möglich |
| S08 | Gefühlsauswahl | Kategorien, mehrere Emotionen, Suchfeld, ggf. mögliche Begriffsvorschläge | Chips an/abwählen, Erklärungen öffnen, Intensität 1–10 oder unbekannt | 0 Emotionen zulässig; keine falsche „ungültig“-Meldung |
| S09 | Reflexion | Ein Frageimpuls je Karte: Situation, Gedanke, Körper, Bedürfnis, Reaktion, Erkenntnis | Text eingeben, weiter, zurück, überspringen | Autosave als lokaler Entwurf; explizit Verwerfen möglich |
| S10 | Abschluss | Zusammenfassung, alle Angaben editierbar, Speichern/Verwerfen | Speichern, Bearbeiten, Abschluss ohne Speichern | Doppeltippen erzeugt keinen Doppeleintrag; keine Verlustgefahr |
| S11 | Tagebuch | Timeline, Kalendermodus später, Suche, Emotionsfilter | Öffnen, bearbeiten, löschen, neuer Eintrag | Leerer Zustand, Bestätigungsdialog bei Löschung |
| S12 | Eintragsdetail | Zeitpunkt, Emotionen, Intensitäten, freie Texte, Reflexionsfelder | Bearbeiten, löschen, zurück zum Atlas | Historische Namen bleiben referenziert; Änderungen konsistent |
| S13 | Rückblick | Lokale aggregierte Darstellung, offene Reflexionsimpulse | Zeitraum, Details, zurück | Wenig Daten: keine voreiligen Musterbehauptungen |
| S14 | Einstellungen | Theme, Datenschutz, Datenexport, alle Daten löschen, Hilfe, App-Info | Opt-in und Datenkontrolle | Kritische Aktionen bestätigen, Export nur explizit |

**Navigation:** Back bringt zur letzten Ansicht und behält ungespeicherte Entwürfe. Deep-Link vom Atlas zur Reflexion übernimmt nur den gewünschten Emotionsterm; keine automatische Tagebuchspeicherung. Von Einträgen gelangt man über die zugeordneten Emotionen wieder zu Detailseiten. Bei Prozessabbruch: „Entwurf behalten“, „Verwerfen“, „Weiterbearbeiten“.

## 4. Detaillierte Journeys

### J1 – „Ich weiß nicht, was ich fühle“ (P0)
1. Auf S02 „Gefühl erkunden“ > S06 „Ich bin unsicher“.
2. S07 fragt optional nach angenehmer/unangenehmer Stimmung und Aktivierung; „weiß nicht“ und „gemischt“ bleiben möglich.
3. S08 bietet 3–8 grobe Kategorien; diese sind Vorschläge, keine automatische Diagnose; stets manuelle Suche verfügbar.
4. Nutzer wählt 0–n Emotionen, kann S04 lesen und auf Wunsch für jede Intensität 1–10 festhalten.
5. Optional S09 für Reflexionsfragen; S10 zeigt Zusammenfassung.
6. Erst „Speichern“ erstellt genau einen Eintrag, danach S11 oder Startseite. Ohne Speichern keine Tagebuchhistorie.

### J2 – „Ich möchte einen Begriff lernen“ (P0)
S02 > S03 > Kategorie oder Suche > S04 > S05 Vergleich. Über „Über eigene Erfahrung reflektieren“ kann ein neuer Check-in mit vorausgewähltem Begriff geöffnet werden. Zurück führt in die ursprüngliche Kategorie und Scrollposition.

### J3 – „Ich möchte eine Situation verstehen“ (P0)
S02 > S06 „Situation reflektieren“ > S09 Situation frei schildern > S08 Gefühle suchen/mehrfach wählen > S09 optionale restliche Fragen > S10 prüfen > speichern oder ohne Speichern schließen.

### J4 – „Ich ändere eine Eintragung“ (P0)
S11 > S12 > Bearbeiten > emotionale Zuordnungen/Intensitäten/Texte ändern > Speichern. Datensatz-ID und ursprüngliches Erstelldatum bleiben erhalten; Änderungsdatum wird aktualisiert. Abbruch verwirft nur die aktuellen Änderungen, nicht den Originaleintrag.

### J5 – „Ich kann nichts benennen“ (P0)
Nutzer darf alle Emotionsfelder leer lassen, „nicht einordbar“ markieren, freie Texte festhalten und speichern. Keine Blockade, keine Wertung. Eine unklare Erfahrung ist gültig.

### J6 – „Ich möchte neue Begriffe üben“ (P1)
S02/Entdecken > kurze Übungen > situativer Mehrfachantwortvergleich > Erklärungen > auf Wunsch Favorit/Wiederholung. Kein Verlust von Streaks; keine „schlechte Stimmung“-Punkte.

### J7 – „Ich möchte meine Daten kontrollieren“ (P0)
S14 > „Daten exportieren“ > explizite Warnung vor unverschlüsselter Datei, Ziel über Android-Dateiauswahl. Oder „Alle persönlichen Daten löschen“ > deutliche Bestätigung > ausschließlich lokale personenbezogene Daten löschen, App-Lexikon bleibt erhalten. Kein versteckter Serverrest.

## 5. Fachmodell Emotionen und redaktionelle Regeln

### 5.1 Kategorien als flexible Navigation
Acht vorläufige Familien: Freude/Begeisterung; Zuneigung/Verbundenheit; Traurigkeit/Verlust; Angst/Unsicherheit; Ärger/Widerstand; Scham/Schuld/Selbstbewertung; Überraschung/Staunen; Ruhe/Entlastung.

Kategorien sind keine erschöpfende oder universelle Klassifikation. Ein Begriff kann zu mehreren Familien gehören. Emotionen können gleichzeitig auftreten; angenehme/unangenehme Valenz ist kontextabhängig. „Erschöpfung“ ist beispielsweise primär Zustand, kann aber als alltagsnaher Suchbegriff als Zustandskonzept erfasst und entsprechend gekennzeichnet werden.

### 5.2 Pflichtfelder pro Lexikonbegriff
- Stabile ID (nicht vom angezeigten Namen abhängig), deutscher Name, Alias/Synonyme; Typ (Emotion/Zustand/Empfindung/Bedürfnis).
- 1–2 Sätze Definition in verständlichem Deutsch, 2 konkrete Alltagsszenarien, 1–3 mögliche Merkmale (als *kann*, nie *muss*), Differenzierungsfrage.
- 1–5 ähnliche Begriffe und mind. ein geprüfter Kontrast, sofern sinnvoll; Kategorie(n); redaktionelle Versionsnummer und Quelle/Prüfnotiz.
- Kein Anspruch auf eindeutige körperliche Marker, keine Diagnose, keine stereotype Geschlechts-/Kulturzuordnung.
- Inhalte werden auf respektvolle, verständliche, nicht-stigmatisierende Sprache geprüft; Begriffe mit Mehrdeutigkeit werden als solche erklärt.

### 5.3 Auswahl und Vorschläge
Schnellfilter auf Valenz und Aktivierung ist **optional und nur heuristisch**. Alle Emotionsbegriffe bleiben über Suche erreichbar. Vorschläge zeigen „Könnte passen“; niemals „Du fühlst X“. Nutzer kann Reihenfolge beliebig ändern, manuell suchen, widersprechen und eigene Wörter als Freitext ergänzen (P1 als separate Notiz, nicht automatisch Lexikoneintrag).

## 6. UX-Vertragsregeln

- **Kurzer Weg:** Schnell-Check-in grundsätzlich ohne Freitext in ca. 60–90 Sekunden möglich (Usability-Ziel, keine Garantie).
- **Tiefer Weg:** 5–10 Minuten optionale Reflexion ohne Zeitlimit.
- **Keine Pflicht-Selbstoffenbarung:** Jedes Feld optional, außer einer ausdrücklichen Speichern-Aktion.
- **Interaktionskontinuität:** Eingaben bleiben bei Unterbrechung, Rotation, Hintergrund und Navigation als lokaler Entwurf erhalten; Entwürfe getrennt von finalen Einträgen.
- **Datenintegrität:** Beim Tippen von Emotionen, Bearbeiten von Intensitäten oder Wechseln zum Lexikon entstehen niemals ungültige Zeiträume oder Datenverluste.
- **Rückmeldungen:** Knappe, freundlich-neutrale Bestätigung; Fehler benennen und konkrete Wiederholung anbieten.
- **Privatsphäre:** Kein Inhalt persönlicher Einträge in Notifications, Crash-Logs, Analytics, Screenshots der App-Switcher (geeignetes Schutzkonzept prüfen), Zwischenablage ohne explizite Aktion.
- **Inklusion:** Genderneutral, Screenreader-Labels, System-Schriftgröße, Kontrast, Haptik optional, reduzierte Bewegung respektieren; Dark Mode funktional.

## 7. Designsystem D – warm + minimalistisch

**Ton:** Vertrauensvoll, erwachsen, ruhig, zugewandt; keine klinischen Formulare und kein übertriebenes „Wellness“-Design.

**Vorgeschlagene semantische Tokens (müssen auf echten Displays/mit Kontrasttest bestätigt werden):**
- Background warm: #F7F7F3; Surface: #FFFFFF; Surface muted: #EEF2EE.
- Primary (dunkles Petrol): #34535D; Primary variant: #547681.
- Sage accent: #8AA697; Sand accent: #EAE2D7; Border: #D7DED8.
- Text primary: #24312F; Text secondary: #53625E; Error: #9E4545.
- Dark Mode: eigene semantische Gegenstücke; keine simple Farbumkehr.
- Farben von Emotionsfamilien dekorativ/optional, nicht als Aussagen über „gut/schlecht“ oder alleinige Unterscheidungsmerkmale.

**Typografie:** Android-Systemschrift oder lizenzfreie lokal gebündelte Schrift, Basisgröße etwa 16sp, Überschriften 22–30sp; System-Skalierung ohne Abschneiden. Text auf Karten sparsam, Inhaltssprache deutsch.

**Layout:** 8dp Raster, 16–20dp horizontale Ränder, Kartenradius 16–20dp, Mindesttouchfläche 48x48dp, klare Primäraktion, sichtbarer Fokus/Pressed-State. Ein Frageimpuls pro Reflexionskarte. Bottom-Navigation max. vier Punkte.

**Motion:** Dezente Übergänge 150–250ms nur falls sinnvoll, keine flackernden/confetti-Effekte; „reduzierte Animationen“ respektieren. Jede Emotion auch ohne Farbwissen identifizierbar.

## 8. Qualitätsziele, Risiken, Produktmetriken

**Erfolgskriterien (in Tests zu prüfen):**
- Neue Nutzer erreichen ohne Anmeldung in höchstens zwei Minuten ihren ersten Check-in.
- 100 % der Check-in-Fragen können übersprungen werden, und 0 ausgewählte Gefühle ist ein gültiger Zustand.
- Atlas-Begriff aus der Reflexion erreichbar und zurück ohne Verlust.
- Tagebucheinträge über App-Neustart beständig; Editieren/Löschen funktioniert.
- Offline-Nutzung sämtlicher P0-Funktionen.
- Kein persönlicher Text verlässt die App ohne nutzerinitiierte, erklärte Aktion.
- Manuelle UX-Tests mit mindestens 5 diversen Personen vor öffentlich nutzbarer Version; keine Pflicht-Telemetrie.

**Wesentliche Risiken:** Fachliche Ungenauigkeit von Emotionsbegriffen, unangebrachte psychologische Suggestion, Verlust sensibler Notizen, Fehlinterpretation von Statistiken, Accessibility-Lücken, Rückschritte bei iterativen Erweiterungen. Für jedes Risiko sind in Architektur/Umsetzungsplan Gegenmaßnahmen definiert.

**Änderungsregel:** Diese Datei bildet den verbindlichen Arbeitsstand v1.0. Neue Wünsche erhalten eine Anforderungs-ID mit Priorität und expliziten Abnahmekriterien; Änderungen an bereits festgelegtem Verhalten werden dokumentiert und versioniert. Vor einem ersten Release sind Name, finale Paket-ID, Datenschutztexte, visuelle Kontrastwerte und redaktionelle Abnahme noch formell zu bestätigen.
