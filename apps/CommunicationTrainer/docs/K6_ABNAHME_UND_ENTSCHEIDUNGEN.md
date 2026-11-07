# K6 – Entscheidungen, Qualitätsgates und Konzeptabschluss

**Version:** 1.0 · **Datum:** 07.11.2026 · **Status:** Kriterien-/Entscheidungsvorlage, keine bestandene Release-Abnahme.
**Grundlage:** [K6 Gesamtaudit](K6_GESAMTAUDIT.md), [K6 MVP](K6_MVP_UMFANG.md), [K6 Fahrplan](K6_UMSETZUNGSPLAN.md), [K5 Modellvergleich](K5_MODELLVERGLEICH_TESTPLAN.md).
**Geltung:** K1–K5 bleiben die Fachspezifikationen; K6 priorisiert Umfang und Reihenfolge für technische Alpha und V1.

## 1. Was K6 abschließt – und was nicht

**Konzeptionell fertig zur Überprüfung:**
- klares Produktversprechen und dessen ethische Grenzen;
- sechs K1-Kompetenzfelder und verhaltensbezogene K2-Coachregeln;
- fünf versionierte K3-Personas und sechs versionierte Szenario-Fixtures;
- K4-Navigations-, Datenkontroll- und Accessibility-Rahmen;
- K5-Architekturalternativen (lokal, bedingter ChatGPT-Plan, Cloud), Safety/Datenschutz, Betrieb und Kosten;
- begründete MVP-Reduktion, Entwicklungsschritte und Testgates.

**Nicht erledigt:** Android-App, funktionierendes Backend, Providerintegration, Kostenmessung, Datenschutz-Folgenabschätzung, offizielle OAuth-Zulassung, Modellbenchmark, Anwenderpilot, wissenschaftlicher Wirkungsnachweis, Release.

**„Konzeptabschluss“ ist kein Freifahrtschein:** Jedes zukünftige Feature unterliegt seinen echten P0/P1-Tests, jeder PR eigener Merge-Freigabe und Veröffentlichung eigener Produkt-/Rechtsfreigabe.

## 2. Vor Implementierungsbeginn (K6 Definition of Ready)

| Gate | Frage | Nachweis | Sperre |
|---|---|---|---|
| DR01 | Repository korrekt isoliert? | Bestandsaufnahme D00, Build-/Toolchainplan | D01 keine fremde App beeinflussen |
| DR02 | Scope eindeutig? | K6 MVP und maschinenlesbare F01–F17 | keine stillen Featureerweiterungen |
| DR03 | Personadaten versioniert? | K3-JSON-IDs/Szenarien/Tests | keine Laufzeitschema-Inkonsistenz |
| DR04 | Coach-Qualitätskriterien nachvollziehbar? | K2-Validierungs- und Negativfälle | kein ungeprüftes Echtfeedback |
| DR05 | Privacy und Safety verstanden? | Datenfluss-, Aufbewahrungs- und Report-Plan | keine echte Nutzung ohne passende Dateninformation |
| DR06 | Kostenschutz vorhanden? | providerbezogenes Budget-/Retry-Konzept | kein unbegrenzter produktiver API-Zugang |
| DR07 | ChatGPT-/OSS-Aussagen korrekt? | offizielle Zulassung/Lizenz/Device-Gate, jeweils mit Status | kein ungeprüftes „inklusive ChatGPT“ |
| DR08 | Review- und Rollenverantwortung klar? | Zuständige für Merge, Qualität, Recht, Moderation und Budget | keine öffentliche Beta ohne Betreiber |
| DR09 | Risiken mit Fallback geklärt? | Scope-Entscheid „Feature verschieben statt Safety reduzieren“ | keine erzwungene Scheinimplementierung |

Diese Gates sind **konzeptionelle Vorbedingungen**. Die genaue Android-Toolchain und Providerverträge werden erst in D00/D04/D05 anhand der tatsächlichen Umgebung ausgewählt.

## 3. Release-Gates (Risikowertung)

**P0 – blockierend bei bekannten Fehlern:**
1. KI-Kennzeichnung im Chat oder Erstkontakt fehlt bzw. reale Person wird vorgetäuscht;
2. explizite Grenzen werden systematisch übergangen, manipulative Bindung wird belohnt;
3. K2 erfindet Textstellen, bewertet Persönlichkeit/Diagnosen oder generiert unsichere Aussagen;
4. Chat-/Entwurf-/Replay-Verlust bei reproduzierbarem Kernflow;
5. fremde Chats, Erinnerungen oder Daten werden sichtbar (IDOR/Cross-scope);
6. API-Secrets in APK/Git oder nicht begrenzbare Anbieterrechnung;
7. Lernprofil/Memory wird ohne zulässigen Zweck und tatsächliches Opt-in gespeichert;
8. fehlende oder irreführende Löschung, Restore- und Backupfehler;
9. unerlaubter lokaler → Cloud-Transfer, unzulässiger ChatGPT-OAuth-/Tokenflow;
10. fehlende In-App-Meldung bei KI-Generierung in relevanter Play-Auslieferung;
11. Datenschutz-/AI-/Store-/Lizenz-/Vertragsrisiken ohne erforderliche Freigabe;
12. fehlende sichere Behandlung schwerwiegender unzulässiger Inhalte.

**P1 – vor regulärem V1 beheben:** blockierende Chat-UX, defekte Zurücknavigation, schwere Accessibility-Probleme, nicht nachvollziehbares Kurzfeedback, unsichere Retry-/Timeout-Nutzerführung, unklare Nutzungsbudgets.

**P2 – iterativ:** visuelle Details, zusätzliche Szenarien, Filter, feinere Hilfetexte und Komfortfunktionen – sofern kein P0/P1 daraus entsteht.

**Keine erfundenen Trefferquoten:** „100 % korrekte Chatbeleg-IDs“ ist eine deterministische Validatorforderung; „95 % empathischer Coach“ wäre ohne kalibrierte Messmethodik unseriös. Für subjektive Qualitätsdimensionen zunächst Baselines und Mensch-zu-Mensch-Abweichungen erheben, dann nachvollziehbare Schwellen festlegen.

## 4. Fachliche Abnahme

### 4.1 Persona
- Alle fünf Persona-IDs gültig, Kanon und Kommunikationsstil in langen wie kurzen Dialogen wiedererkennbar.
- Unterschied zwischen Eigenschaft und momentaner Energie/Verfügbarkeit sichtbar; keine reinen Emoji-Varianten.
- Elena darf „Nein“ sagen, Jonas über Fotografie enthusiastisch sein, Deniz widersprechen, Robin ernst reagieren, Mara erschöpft sein.
- **Keine** Bindungssteigerung als bloße Nachrichtenfunktion.

### 4.2 Coach
- Angemessenes Lob **mit** echtem Beleg; maximal eine priorisierte Option, wenn sinnvoll.
- Kein verpflichtender Negativpunkt. „Nicht beurteilbar“ bei zu wenig Material.
- Keine vorgetäuschte Kenntnis innerer Persona-Gefühle und keine Diagnose des Nutzers.
- Grenzhinweis vor Engagement-Tipp.
- Strittiges Feedback kann beanstandet und ggf. aus Bewertung entfernt werden.

### 4.3 UX/Verständnis
- Testperson kann mit wenigen eigenen Schritten einen freien Chat beginnen und eine Übung starten;
- Freier Chat und Übung unterscheidbar; Coach nicht ungefragt im freien Chat;
- erste Auswertung führt nachvollziehbar zur zugrundeliegenden Textstelle;
- 200 % Schrift, TalkBack und Tastatur/Zurück funktionieren;
- unsent Draft, Retry und Original-Replay bleiben erhalten;
- Opt-in- und Löschkontrollen sind verständlich und **wirksam**;
- Berichtsmöglichkeit vorhanden, ohne die App zu verlassen.

### 4.4 Wissenschaftliche/ethische Aussage
- Es wird **keine** klinische Therapie, Diagnose, standardisierte soziale Intelligenz, garantierte Beziehungspflege oder messbare Realweltverbesserung versprochen.
- Ergebnis im Chat ist Lernmaterial, kein objektiver Menschen- oder Persönlichkeitstest.
- Für echte Wirksamkeitsbehauptungen wäre eigenständige Studie/Transfererhebung nötig.

## 5. Technische und betriebliche Abnahme

| Ebene | Mindestnachweis |
|---|---|
| Repo/CI | ausschließlich isolierter App-Build, reproduzierbare Debug-Version, grüne relevanten CI-Checks |
| Android | Room-/DataStore-Schema, Migration, Zustandserhalt bei Prozess-Tod, Retry-Idempotenz |
| Auth | gültige Eigentümer-/Rollenprüfung, Guest-/Account-Missbrauchsschutz |
| Provider | ausgewählte Modellversion, Vertrag/Lizenz, Kosten und Latenz gemessen |
| Privacy | Datenfluss, Empfänger/Region, Aufbewahrung, Backup, Löschkaskade, Consent verifiziert |
| Safety | Server-/Client-Grenzen, Reports, Moderation, Eskalation und K2-/K3-Risikofälle |
| Monitoring | Kostengrenze, Alerting, Fehler-/Incident-Runbook, keine Rohchats in Normaltelemetrie |
| Accessibility | unterstützte Android-Geräte, TalkBack, große Schrift, Fokus und sichtbare Fehler |
| Resilienz | Netzabbruch, Provider-Ausfall, teilgestreamte Antwort und Neustart ohne Datenverlust |
| Release | getrennte Freigabe, Store-Rechtstexte, Daten-/Kontolöschansprüche und Signierung |

## 6. Menschliche Pilotierung (erst nach Safety-/Privacy-Gates)

**Qualitatives Testdesign, keine bereits erhobenen Ergebnisse:**

1. **Frühe UX-Tests:** erste Benutzung, schwierige Navigation, Verständnis KI vs. Mensch, Datenfreigabe.
2. **Dialog-Prüfung:** fünf Personas in mehreren Szenarien; mindestens zwei unabhängig beurteilende Reviewer pro festgelegter Stichprobe, mit Dissensprotokoll.
3. **Coach-Prüfung:** K2-Negativ-/Kontrastdialoge und freiwillige Übungsrückblicke auf Kontext, Beleg, Ton und Nutzen.
4. **Geräteprüfung:** ein ressourcenärmeres, ein mittleres und ein leistungsfähigeres Zielgerät; exakte Modelle später.
5. **Praxisfeedback:** Nutzer können freiwillig berichten, ob sie eine Idee in Alltagssituationen ausprobiert haben. Das sind Selbstberichte, **kein** objektiver Wirksamkeitsbeweis.
6. **Fehlertracking:** P0/P1-Befunde dokumentieren, nachbeheben und erneut prüfen, anstatt Durchschnittswerte schönzurechnen.

**Nutzerstichprobe und Grenzwerte** werden vor Durchführung verantwortungsvoll bestimmt; eine kleine qualitative Studie ist keine statistische Erfolgsquote.

## 7. Explizite Entscheidungen für den Nutzer/K6-Review

| ID | Beschlussvorschlag | Verbindlich erst nach |
|---|---|---|
| E01 | Alpha 3 Personen + 2 Szenarien; V1 5 Personen + 6 Szenarien | K6-PR-Freigabe |
| E02 | V1 mit freiwilligem Kurzfeedback und genau einem Replay-Zweig | K6-PR-Freigabe, spätere technische Gates |
| E03 | Charaktereditor, Langzeit-Memory und KI-Lernprofil nach V1 | K6-PR-Freigabe |
| E04 | ein geprüfter Inferenzstandard für V1, keine verpflichtende ChatGPT-Anbindung | K6-PR-Freigabe und echter Modellvergleich |
| E05 | vier Tabs in V1, Alpha kleiner, Fortschritt ohne Scheinscores | K6-PR-Freigabe |
| E06 | zuerst D00/D01 mit isoliertem Android-Build und Mock-Provider | K6-PR-Freigabe |
| E07 | P0-Sicherheits-/Datenschutz-/Play-Reporting-Gates nie zur Kostenersparnis kürzen | K6-PR-Freigabe; echte Umsetzung |
| E08 | Monetarisierung nach gemessenen Kosten, keine künstliche Bindung | K6-PR-Freigabe und Wirtschaftlichkeitsprüfung |

**Unaufgelöste Betriebsentscheidungen:** konkretes Android-SDK/Backend, Provider/Region/Verträge, Produktionsbudget, Account-/Gastmodell, Backup-/Löschfristen, zulässiger OpenAI-Android-OAuth-Flow, Verantwortliche und reale Qualitäts-/Kostenwerte. Diese Punkte werden **nicht durch Annahmen entschieden**.

## 8. Konzeptabschluss-Checkliste

- [x] K1–K5 dokumentiert und in K6 auf Zielkonflikte geprüft.
- [x] 26 konkrete Auditfälle mit P0/P1 und Lösungsvorschlägen erfasst.
- [x] Technische Alpha und V1-MVP unterscheidbar definiert.
- [x] Scope und Nicht-Ziele offen dokumentiert.
- [x] Entwicklungs-PR-Abfolge mit Tests/Isolationsvertrag vorgegeben.
- [x] KI-Modelloptionen und Kontoanbindung kritisch begrenzt.
- [x] Release-/Rechts-/Safety-Gates festgelegt.
- [ ] Nutzer hat **diesen** K6-Pull-Request ausdrücklich genehmigt.
- [ ] D00/D01: tatsächliche Repo-/Buildprobe.
- [ ] D04/D05: realer KI-Qualitäts-/Preis-/Vertragstest.
- [ ] Pilot und öffentliches Release vollständig freigegeben.

**Abschließende Feststellung:** Ein Konzept kann prüfreif sein, ohne die App, die Tests, die Rechtskonformität oder den Geschäftsbetrieb zu validieren. K6 schafft die **Reihenfolge und Prüfbarkeit**, nicht eine vorweggenommene technische Erfolgsmeldung.
