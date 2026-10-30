# M6.3.4 – Qualitätstore vor M7: Android, Barrierefreiheit, fachliche Plausibilität

**Datum:** 30.10.2026
**Status:** technisch-redaktionelle Prüfungen und Korrekturen als Draft-PR; **kein öffentliches Freigabezertifikat und keine unabhängige fachpsychologische Begutachtung**.
**Umfang:** bestehender Emotionskompass aus main nach PR #21; keine neuen Nutzerfunktionen oder automatische Personalisierung.

## 1. Prüfansatz und Ergebnisgrenzen

| Dimension | Tatsächlich geprüfter Gegenstand | Aussagekraft | Offen |
|---|---|---|---|
| Datenintegrität | 82 Bedürfnisdefinitionen, 30 Strategien, 143 kuratierte Kanten für 76 Bedürfnisse, 6 bewusst ohne Zuordnung | Reale Referenzprüfung sowie explizite Ausnahme-/Sicherheitstests (JVM) | unabhängige Einzelaussagen-/Quellenprüfung |
| Fachliche Plausibilität | Alle 143 redaktionellen Begründungen auf bedingte Formulierungen und Garantien geprüft; kritische Fälle separat verglichen | Redaktionsreview, **kein Wirksamkeitsnachweis** | klinisch/psychologisch qualifizierte Zweitgutachterin bzw. Zweitgutachter |
| UI-Bedienbarkeit | Compose-UI-Prüfungen für vier konkrete Wege auf Android-Emulator API 35 bei 100 % und 200 % Schrift; zweite Einstellung zusätzlich 320dp Breite | Reale Android-Tests, **kein physisches Smartphone** | andere Android-Versionen, OEM, Displaygrößen, Rotation, visuelle manuelle Abnahme |
| Accessibility | Karten-Touchziele, zusammengehörige Screenreader-Einheiten, semantische Überschriften, Bedienbarkeit mit stark vergrößerter Schrift | Compose-Semantik und Touchgröße automatisiert prüfbar | **TalkBack mit echter menschlicher Bedienperson**, Fokusverlauf, Gesten, Sprachqualität |
| Datenschutz | Keine neuen Berechtigungen, keine Speicherung persönlicher Linkauswahl, kein Zugriff auf Tagebuchtexte | Code-Scope-Review, bestehende Offline-Funktionen unverändert | unabhängige Datenschutz-/Sicherheitsabnahme |

**Wichtig:** Ein grüner Emulator-Test ist weder ein TalkBack-Nutzertest noch eine psychologische Fachfreigabe.

## 2. Durchgeführter redaktioneller Plausibilitätsabgleich

Die acht veränderbaren Linkdatensammlungen bilden 143 Beziehungen zwischen 76 Bedürfnissen und 20 unterschiedlichen bestehenden Strategien ab. Alle 143 Begründungen sind **bedingt** statt als zwingendes Verhalten formuliert (z. B. „wenn“, „kann“ oder „könnte“); strukturierte Tests sperren ausdrückliche Zwangs-/Heil- und Wirksamkeitsversprechen. Es wurden keine fehlenden Bedürfnisse automatisch mit Synonymen oder Gefühlswörtern aufgefüllt.

**Fachlich relevante Kategorien und Ergebnis:**

| Prüfgruppe | Kritische Fehlinterpretation | Befund / Schutz |
|---|---|---|
| Schutz, Sicherheit, Würde, Gleichbehandlung | Nutzer könne gewaltvolle/ungleiche Verhältnisse selbst per Gespräch reparieren | Sicherheitskontext, freiwillige Grenzen und unabhängige Unterstützung genannt; bei Gefahr keine Konfrontationspflicht |
| Soziale Akzeptanz | Akzeptanz durch andere mit innerem Annehmen einer Situation verwechseln | Nicht mit „Annehmen, was gerade ist“ verlinkt; Erklärung im Bedürfnisdetail; Selbstmitgefühl nun ausdrücklich **nur ergänzend**, kein Ersatz für soziale Annahme |
| Hoffnung, Erfolg, Zugehörigkeit, Liebe | Kleine wertebezogene Handlung würde gewünschten Erfolg/Gefühl erzeugen | Bedingte Aussagen, keine Garantie; solche Verbindungen sind oft **indirekte Orientierung**, keine kausalen Erfolgsnachweise |
| Schlaf, Gesundheit, Nährung | Coping- oder Entspannungstechnik als körperliche Versorgung darstellen | **Keine** direkte Strategiezuordnung; spezifischer Hinweis, dass Übungen Schlaf, Ernährung oder medizinische Hilfe nicht ersetzen |
| Spiritualität, Humor, Spontaneität | Beliebige Karte als scheinbar passende Therapie/Technik präsentieren | Weiter ohne direkte Karte; freie Exploration bleibt erhalten |
| Problemmuster | Langfristiges Vermeiden oder Gefühlsunterdrückung als Bedarfserfüllung anbieten | Zwei erklärende Musterkarten ausdrücklich aus der Link-Sammlung ausgeschlossen |
| Körperübungen | Schmerz, Atemnot oder unangenehme Wahrnehmung ignorieren | Strategiekarten betonen Freiwilligkeit und Abbruch bei Beschwerden, **keine medizinische Empfehlung** |

Die Linkbeziehungen werden nicht pauschal als 143 empirisch belegte Interventionen dargestellt. Für ihre Aussagekraft gibt es derzeit **kein wissenschaftlich validiertes 82×30-Zuordnungssystem**.

### Quellenabgleich mit Grenzen

- **Bonanno & Burton (2013)**: https://pubmed.ncbi.nlm.nih.gov/26173226/ – kontextsensitives, flexibles Bewältigungsmodell; keine generelle Wirksamkeit einer Karte für jede Person.
- **Skinner et al. (2003)**: https://pubmed.ncbi.nlm.nih.gov/12696840/ – Kritik und Möglichkeiten von Coping-Kategorisierungen; die sieben App-Familien sind keine validierte Taxonomie.
- **Troy et al. (2013)**: https://pubmed.ncbi.nlm.nih.gov/24145331/ – Wirksamkeit von Neubewertung hängt vom Kontext ab; **keine automatische Individualempfehlung**.
- **Ryan & Deci (2020)**: https://doi.org/10.1016/j.cedpsych.2020.101860 – SDT-Grundbedürfnisse Autonomie, Kompetenz, Bezogenheit; nicht identisch mit dem redaktionellen 82-Begriffe-Atlas.
- **NHS Atem-Übungsseite**: https://www.nhs.uk/mental-health/self-help/guides-tools-and-activities/breathing-exercises-for-stress/ – Praxisbeispiel, keine universelle Behandlung.
- **WHO/PAHO Stressleitfaden**: https://www.paho.org/en/documents/doing-what-matters-times-stress-illustrated-guide – aufrufbar ggf. abhängig von Website-Zugriff; beim Webcheck in der Testumgebung HTTP 403, **nicht** als geprüfte Verfügbarkeit für alle Nutzenden ausgeben.
- **Cohen & Wills (1985)**: https://pubmed.ncbi.nlm.nih.gov/3901065/ – Publikationsnachweis im aktuellen Webzugriff nicht vollständig abrufbar; Quelle vor fachlicher Freigabe noch einmal über die Veröffentlichungsdaten prüfen.

**Offenes Hochprioritätsthema:** Die Strategieartikel enthalten 62 Quellenverweise auf 16 verschiedene URLs (Analyse aus M6.3.3). Einzelne Quellentypen sind Theorie oder Praxisleitfäden; sie belegen **nicht automatisch jede** der 143 redaktionellen Aussagen. Unabhängiger Claim-by-Claim-Review mit genauer Literaturzuordnung und Korrekturliste bleibt ein verpflichtender Release-Schritt.

## 3. Konkrete UX-/Barrierefreiheitskorrekturen

1. **Begründete Strategie-Karten in Bedürfnisdetails:** Titel, konkrete „Wann/warum?“-Begründung und Aktion bilden ein zusammenhängendes, ausreichend großes anklickbares Element. Die Semantik lässt den Zusammenhang vorlesen, statt Titelbutton und Erklärung getrennt anzubieten.
2. **Große Schrift im Bedürfnisdetail:** „Merken“ und „Als entdeckt markieren“ liegen als jeweils vollständig breite Schaltflächen untereinander statt in einer potenziell überlaufenden Reihe.
3. **Suche:** Trefferzahl und Zurücksetzen werden bei engem Raum untereinander angezeigt; keine Konkurrenz um dieselbe horizontale Fläche.
4. **Heading-Semantik:** Titel von Bedürfnisdetails, Strategieartikeln und wichtigen Strategieabschnitten als echte Überschriften für die Android-Accessibility-Navigation markiert.
5. Keine neuen Hauptnavigationseinträge, kein Tracking, keine Änderung des Tagebuchschlüssels.

Android empfiehlt mindestens **48dp große interaktive Touchflächen** und eine echte Vorlese-/Gestenprüfung. Siehe https://developer.android.com/guide/topics/ui/accessibility/apps sowie https://developer.android.com/codelabs/jetpack-compose-accessibility

## 4. Automatisierter Geräte-Testplan (Android-Emulator, nicht bloß Compile)

Im neuen GitHub-Workflow `.github/workflows/emotionskompass-ux-emulator.yml` startet ein Android-35-Emulator, führt vier Compose-UI-Tests bei normaler Systemschrift aus, setzt **720×1280px und 360dpi** (ca. 320dp Breite) und **200 % Schriftskalierung** und führt dieselben Tests erneut aus:

- Bedürfnis „Akzeptanz“ → begründete Karte „Emotionalen Beistand erbitten“ → richtige Strategie-ID; kein reflexartiger Verweis auf inneres Annehmen.
- Strategie „Eigene Grenzen ausdrücken“ → konkretes Bedürfnis „Privatsphäre“ → richtige Bedürfnis-ID.
- Bedürfnis „Schlaf“ → ehrlicher Leerzustand statt Ersatzempfehlung → freier Atlas dennoch erreichbar.
- Praxisfall mit unsicherer Antwort → **zuerst kein Feedback**, nach „Ich bin unsicher“ erklärende Einordnung.

Die Tests bedienen echte Compose-Oberflächen mit Scrollen und Klicks und prüfen bei mindestens einem Link die **48dp-Mindesthöhe**. Ein zweiter unabhängiger JVM-Sicherheits-/Wordingtest prüft alle 143 Fälle auf Kontextformulierung, bestimmte unzulässige Versprechen und kritische Ausschlüsse.

**Ergebnisse nur als „bestanden“ dokumentieren, wenn der jeweils neueste PR-Commit mit dem Emulator-GitHub-Workflow tatsächlich grün ist.**

## 5. Zwingende manuelle/Fach-Abnahme vor einem öffentlichen Release

**Diese Anforderungen können nicht durch das Sprachmodell oder CI als erledigt bestätigt werden:**

- Qualifizierte psychologische Fachperson: jedes der 143 Bedürfnis-Strategie-Paare mit Kontext, Grenzen und Claim-Quelle beurteilen; pro Paar „passend“, „nur indirekt/bedingt“, „streichen“, Korrekturbegründung.
- Android-Smartphone mit TalkBack: komplette Wege **Bedürfnis ↔ Strategie**, **Strategie ↔ Artikel**, **Praxisfall ↔ Strategie**, Fokusreihenfolge, Gesten, Scrollen, Quellenlinks und System-Zurück tatsächlich bedienen.
- Android 8 und aktuelle Android-Version, klein/normal/groß, Hoch-/Querformat, 200 % Schrift, Dark Mode, App-Fortsetzung nach Hintergrund und Rotation.
- Bei geänderten App-Signaturen Update ohne Datenverlust testen; die Benutzer dürfen alte App-Daten **nicht zur Fehlerbehebung unbedacht deinstallieren**.
- Accessibility Scanner und anwendungsorientierte Tests mit mindestens einer Person, die assistive Technologien nutzt.

**Freigabelogik:** Ein CI-Erfolg darf technische Komponenten freigeben, aber die unabhängige fachliche, manuelle Accessibility- und Geräteprüfung bleibt **offen**. M7 wird in dieser PR bewusst nicht begonnen.

**Merge:** nur nach ausdrücklicher Freigabe des Nutzers.
