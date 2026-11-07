# Konzeptentscheidungen und Änderungsverlauf

## 2026-11-07 – Aurora Pulse als endgültige Farbwelt für CommunicationTrainer

**Nutzerentscheidung:** Nach vergleichender Bewertung von Playful Pulse, Aurora Flow und der Hybridvariante wurde **Aurora Pulse** ausdrücklich gewählt.

**D01-Umsetzung in PR #58:** Zentralisierte Material-3-Theme-Tokens in `android/app/src/main/java/com/valoon4/communicationtrainer/ui/theme/AuroraPulseTheme.kt`; Indigo-Verlauf, warme Pink- und frische Mint-Akzente, ruhige Karten, kontrastbewusste Bottom Navigation und Dark Mode. Verbindliche Dokumentation in [D01_AURORA_PULSE_DESIGN.md](D01_AURORA_PULSE_DESIGN.md).

**Kritischer Kontrastpunkt:** Die gewählten ursprünglichen Indigo-/Pink-Markenfarben liefern zu wenig Kontrast mit kleiner weißer Beschriftung. Deshalb gelten zugänglichere Interaktionsfarben `#5D50CF` und `#CB3470` für Light-Mode-Buttons; originale Farben bleiben Dekor. Automatisierte Tokenkontrasttests mit 4,5:1-Schwelle ergänzen die bestehenden Navigations-Unit-Tests. Vollständige Screenreader-/Schriftgrößen-/Gerätevalidierung bleibt erforderlich.

**Keine Änderung außerhalb CommunicationTrainer und seines eigenen D01-CI-Workflows**, keine Erweiterung um KI oder Speicherung, kein Merge ohne ausdrückliche PR-Freigabe.

## 2026-11-07 – D01 Android-Skeleton und eigene Debug-APK-CI (PR zur Freigabe)

**Nutzerauftrag:** Nach dem freigegebenen und gemergten PR #57 für D00 wurde der Beginn von **D01 inklusive eines eigenen CommunicationTrainer-GitHub-Actions-Workflows** ausdrücklich beauftragt. Damit ist **genau die neue Datei** unter `.github/workflows/communicationtrainer-debug-apk.yml` eine gezielt autorisierte Ausnahme von der sonstigen Projektordnergrenze.

**Konkrete Codebasis:** Unter `apps/CommunicationTrainer/android/` existiert nun ein eigenständiges Kotlin-/Compose-Projekt, das **Chats** und **Üben** als navigierbare, noch funktionsarme Demo ansieht. Material3, Hell-/Dunkelmodus, lokale Navigationszustandssicherung und Unit-Tests sind angelegt; es gibt **noch kein** Netzwerk, keine Chatdaten und keinen KI-Provider. D02/D03 bleiben für Persistenz und Chatlaufzeit verantwortlich.

**Toolchain:** JDK 17, Gradle 8.13, Android Gradle Plugin 8.13.2, Kotlin/Compose-Compiler 2.4.21, compileSdk/targetSdk 36, minSdk 26; Versionspins dienen der Reproduzierbarkeit. CI verwendet das offizielle GitHub-Setup für Gradle 8.13; lokal ist ein mit SHA-256 geprüfter Bootstrapper als **nicht-offizieller Gradle-Wrapper-Ersatz** dokumentiert.

**GitHub Actions:** `pull_request` und `push` nur auf Änderungen an `apps/CommunicationTrainer/android/**` bzw. dem eigenen Workflow, sowie manuelle Ausführung; `contents: read`, keine Push-/Auto-Merge-Aktion; Unit-Tests, Assemble, APK-Paket-/targetSdk-/Signaturprüfung und Artefakt-Upload.

**Isolation:** Alle anderen Projekt- und Workflow-Dateien sind unverändert, darunter `PC/Kabelsalat/` und `.github/workflows/kabelsalat*.yml`. Der Root-Wormblade-Workflow wird hier **nicht** geändert; D00 hat seine main-Push-Trigger gesondert dokumentiert.

**Status:** Code und Workflow im D01-Branch erstellt; **Build/CI-Ausführung ist gesondert anhand tatsächlicher GitHub-Runs zu bestätigen**. Keine installierbare APK behaupten, bevor eine erfolgreich erzeugt und verifiziert wurde. PR benötigt eigenständige Freigabe vor dem Merge.

## 2026-11-07 – D00 Repository-/Build-Audit durchgeführt (D00-PR zur Freigabe)

**Auftrag:** Der Nutzer hat nach dem ausdrücklich genehmigten und verifiziert gemergten **PR #56** den Start der technischen Phase **D00** beauftragt.

**Tatsächlich ermittelt:** GitHub-main-Snapshot `830280455c51f7ab854c4476e5eec526a4213ad3`; vollständiger Git-Tree mit 547 Einträgen/409 Dateien, 11 bestehenden GitHub-Actions-Workflows und 33 CommunicationTrainer-Dateien. **CommunicationTrainer hat noch kein Android-Projekt, kein Manifest, keine Kotlin-Dateien und keine APK.** Die Root-Gradle-Dateien bauen Wormblade (`:app`), während eigenständige Projekte wie InnerWave/TimeFlow unter `games/` liegen.

**Dokumentiert:** [D00_REPO_BUILD_AUDIT.md](D00_REPO_BUILD_AUDIT.md) mit überprüften Gradle-/Java-/Android-Versionen, eigenständigem D01-Projektpfad, kompatiblem Toolchain-**Kandidaten**, aktueller Play-targetSdk-Anforderung (für neue Android-Apps mindestens API 36) sowie zehn technischen Risiken; [d00_build_audit_v1.json](../specs/d00_build_audit_v1.json) als maschinenlesbarer Prüfstand.

**Kritisches CI-Risiko:** Der bestehende Root-Workflow `.github/workflows/debug-apk.yml` läuft bei allen main-Pushes und kann über das Wormblade-Skript Änderungen an fremdem Code zurück nach main pushen. **D00 verändert diesen Workflow nicht.** Eine Einschränkung auf Wormblade gehört in einen gesondert ausdrücklich genehmigten Sicherheits-PR.

**Sonderfreigabe erforderlich:** Eine automatisch laufende CommunicationTrainer-Debug-APK-CI braucht künftig eine neue Datei unter `.github/workflows/`, außerhalb der vereinbarten `apps/CommunicationTrainer/`-Grenze. Eine Änderung erfolgt erst nach einer **konkreten, gezielten Genehmigung**. Bis dahin können app-eigene Gradle-/CI-Skripte innerhalb des Projektordners vorbereitet werden.

**Empfehlung D01:** eigenständige Gradle-Wurzel `apps/CommunicationTrainer/android/`, eindeutiger vorgeschlagener Paketname `com.valoon4.communicationtrainer`, JDK 17, zunächst Gradle 8.13 / AGP 8.13.2 / Kotlin 2.4.21 / compileSdk 36 / targetSdk 36 (unter Vorbehalt des echten D01-Builds). Zwei Compose-Einstiege und Mock-Provider, keine echte KI.

**Status:** D00 **statisch durchgeführt**, **kein** Build/Emulator/CI-Test der neuen App. D00-PR bleibt bis ausdrücklicher Freigabe offen; D01 wird in separatem PR angelegt.

## 2026-11-07 – Konzeptstand 0.9 (PR #56 gemergt): Gesamtaudit, fokussiertes MVP und Umsetzungsgates

**Nutzerauftrag:** Nach dem gemergten PR #54 mit K5 und drei KI-Betriebsarten hat der Nutzer die Ausarbeitung der letzten Konzeptetappe **K6** beauftragt. Die Arbeit erfolgt in einem gesonderten PR ohne automatische Mergefreigabe.

**Neue Dokumente:**
- [K6_GESAMTAUDIT.md](K6_GESAMTAUDIT.md): 26 konkreten Konflikte/Risiken mit P0/P1 und Lösungsvorschlägen;
- [K6_MVP_UMFANG.md](K6_MVP_UMFANG.md): technische Alpha (3 Personas/2 Szenarien), erste V1 nach echten Gates (5 Personas/6 Szenarien, Kurzcoach, ein Replay-Zweig, drei Lektionen, vier Tabs, Safety/Privacy);
- [K6_UMSETZUNGSPLAN.md](K6_UMSETZUNGSPLAN.md): 13 isolierte PR-Stufen D00–D12 von Repo-/Buildprüfung und Mock-Chat bis zu Beta-/Releasegates;
- [K6_ABNAHME_UND_ENTSCHEIDUNGEN.md](K6_ABNAHME_UND_ENTSCHEIDUNGEN.md): Definition of Ready, P0/P1-Gates, Konzeptabnahme und offene Entscheidungen;
- [k6_mvp_scope_v1.json](../specs/k6_mvp_scope_v1.json): maschinenlesbar 17 V1-Funktionsbereiche, Alpha-/MVP-Scope, drei Release-Gates, Entscheidungs- und Aufgabenliste.

**Wichtigste fachliche Vorschläge:**
- Die K1–K5-Fachspezifikationen bleiben gültig; **K6 priorisiert** die Reihenfolge und das frühe UI-/Releasevolumen, statt frühere Detaildokumente zu löschen.
- Konzepte nennen 8–12 Szenarien, K3 hat sechs kuratierte Szenario-Fixtures: **V1 zunächst sechs**, spätere Erweiterung bei Bedarf.
- K1 hat zwölf Lernlektionen als vollständiges Curriculum; **V1 zunächst drei** geprüfte Mini-Lektionen.
- Eigener Charaktereditor, dauerhaftes Persona-Memory, umfangreiche Live-Coach-Berichte und KI-Lernprofil **nach V1**, um vor der ersten Modellvalidierung Kosten, Privacy- und QA-Komplexität zu senken.
- Eine positive KI-Figurenreaktion ist keine „Sozialkompetenznote“; weiterhin Belegpflicht/NOT_ASSESSABLE, Nein respektieren, keine Persönlichkeitsscores oder manipulative Bindung.
- KI-Backend/Provider, offizieller ChatGPT-Android-OAuth, lokale Modellqualität, Konten/Gast, tatsächliche Kosten, Daten-/Löschfristen, rechtliche und öffentliche Releasefreigabe bleiben **offene, testabhängige Entscheidungen**.
- Erster technischer Schritt nach K6-Freigabe: **D00 Repo-/Build-Bestandsprüfung**, danach D01 isolierter Android-Skeleton. Jeder einzelne Code-PR erfordert eigene ausdrückliche Genehmigung.

**Status:** Fachliches Konzept zur Review, noch **keine** Android-App, kein Backend, keine Providerbenchmarks, keine Nutzerstudie und kein rechtlich abgenommener Betrieb. **Kein Merge dieses K6-PR ohne die ausdrückliche Nutzerfreigabe.**

## 2026-11-07 – K5-Kostenoptimierung und drei KI-Betriebsarten (PR #54)

**Nutzerauftrag:** Es wurde gefragt, ob das eigene ChatGPT-Konto oder Open-Source-KI laufende Kosten sparen kann. Anschließend beauftragte der Nutzer ausdrücklich, diese Möglichkeiten **im offenen PR #54** als technische Optionen zu ergänzen. Während der Bearbeitung hat der Nutzer **PR #54 ausdrücklich für den Merge freigegeben**.

**Neue K5-Dokumente:**
- [K5_KI_BETRIEBSARTEN.md](K5_KI_BETRIEBSARTEN.md): lokales Android-Modell, bedingte OpenAI-ChatGPT-Plan-Nutzung, Cloud mit Managed API oder eigenem Open-Weight-Server; Lizenz-, Hardware-, Datenschutz- und Kostengrenzen.
- [K5_MODELLVERGLEICH_TESTPLAN.md](K5_MODELLVERGLEICH_TESTPLAN.md): 28 konkrete Betriebsarten-, Persona-, Coach-, Security- und Performancefälle; Modellkandidaten und faire Vergleichsmethodik.
- [k5_provider_modes_v1.json](../specs/k5_provider_modes_v1.json): vier Adapterkonfigurationen in drei nutzerseitigen Modi mit Fähigkeits-/Verfügbarkeits-/Fehlerzuständen.

**Neue Architekturregel:** Providerunabhängiges InferenceGateway, explizite Wahl des Verarbeitungsorts, keine stillen lokalen → externen Transfers und kein automatischer ChatGPT-Plan → kostenpflichtige API-Fallback. Modellgüte und Safety sind Gates vor Kostenoptimierung.

**Fachliche Einschränkung:** Die offizielle OpenAI-Plan-Nutzung ist für berechtigte lokal gehostete Open-Source-Apps dokumentiert, nicht als ungeprüfte universelle Android-Funktion oder kostenlose API. Android-OAuth-Zulässigkeit/Redirect und Distributionsmodell müssen vor Umsetzung belegt werden. Open-Weight-Modelle haben unterschiedliche Lizenzen; lokale Leistung und Kosten sind noch **nicht gemessen**.

**Statusaktualisierung:** PR #54 wurde nach ausdrücklicher Nutzerfreigabe mit dem K5-Kosten-/KI-Modi-Nachtrag nach main gemergt (Merge-Commit 80d34404be4761635a13c53693d71d1d20d4bbb0). Keine ChatGPT-Verbindung, Geräte-/Modellbenchmark oder Backend-Implementierung erfolgt.

## 2026-11-07 – Konzeptstand 0.8 (PR #54 gemergt): Architektur, Daten und Betrieb

**Auftrag:** Nutzer hat PR #53 ausdrücklich freigegeben und die Fortsetzung mit K5 beauftragt. Der Squash-Merge von PR #53 in main wurde bestätigt; K5 folgt als separater Entwicklungs-PR.

**Neue Dokumente:**
- [K5_TECHNISCHE_ARCHITEKTUR.md](K5_TECHNISCHE_ARCHITEKTUR.md) – Alternativen, Android-App/Repositorys, lokale Chat-/Entwurfspersistenz, gesicherter schlanker KI-Broker, idempotente Requests, getrennte K2-/K3-Policy, Replay und Offline-Design.
- [K5_DATENSCHUTZ_SICHERHEIT.md](K5_DATENSCHUTZ_SICHERHEIT.md) – Daten- und Rechtsinventar, Bedrohungsmodell, Einwilligung, Privatsphäre, AI-Transparenz, Google-Play-KI-Reporting, Löschung, Kontrollen und Release-Gates.
- [K5_BETRIEB_KOSTEN.md](K5_BETRIEB_KOSTEN.md) – Kostenrechnung als hypothetisches Modell, Budgets, Rate-/Abuse-Schutz, Betriebs-/Providerkriterien, Runbooks.
- [K5_TESTPLAN.md](K5_TESTPLAN.md) – 42 technische, Datenschutz- und Betriebsfälle mit kritischen Abnahmeschranken.
- [k5_system_contracts_v1.json](../specs/k5_system_contracts_v1.json) – acht entworfene API-/Datentypen, acht Endpunkte, fünf Datenklassen und Zustands-/Sicherheitsinvarianten.

**Architekturempfehlung, noch nicht final beschlossen:** Kotlin/Compose und Room/DataStore auf Android; über Repositories gebundene, persistierte Chats/Entwürfe; abgesicherter minimaler KI-Broker mit serverseitigen Provider-Secrets, Auth/Quota/Idempotenz/Safety; austauschbare Modelle. Keine unbegrenzte Gast- oder Offline-KI-Nutzung versprechen. Keine End-to-End-Schutz-/Datenlöschgarantie, wenn Provider und Backups sie nicht tragen. Memory und langlebiges Lernprofil standardmäßig aus, bis rechtskonform/technisch verifiziert.

**Wichtige Prüfung und gezielte K4-Korrektur:** Die offiziellen Google-Play-KI-Regeln verlangen einen In-App-Meldeweg für anstößige KI-Antworten. K4 wurde dafür im K5-Branch ergänzt: neuer Screen `safety_report`, neuer Nutzerablauf F13 und Tests U37/U38. Aktuelle K4-Planungszahlen: **30 Screens, 99 Aktionen, 115 definierte Zustände**.

**Finanzen/Legal:** Preisbeispiel ist ausdrücklich hypothetisch; keine Marktpreise und keine gemessenen Qualitäts-/Latenzwerte. DSGVO, AI-Act-Transparenz, Play Data Safety und KI-Reporting sind Compliance-Prüfpunkte, keine vorweggenommene Erfüllung.

**Entscheidungen an K6 übergeben:** Anbieter/Hosting/Region/Vertrag, Gast- vs. Kontoauth, Betreiberbudget, tatsächlich zu speichernde Daten, Opt-in/Löschung, Langzeitmemory und MVP-Schnitt, Release-/Pilotgates.

**Status:** K5 ist ein prüfbarer **Konzeptentwurf**, keine Android- oder Backend-Implementierung. Neuer PR wartet auf ausdrückliche Freigabe. Alle Änderungen liegen innerhalb `apps/CommunicationTrainer/`.

## 2026-11-07 – PR #53 gemergt: Ergänzende K2-Beispiele gesichert

Der Nutzer genehmigte den Merge von PR #53 ausdrücklich. Der Merge nach `main` wurde am 07.11.2026 verifiziert. Die zusätzlichen 20 deutschsprachigen Beispiel- und 36 Prüffälle ergänzen K2, ohne die Hauptspezifikation aus PR #49 zu überschreiben. PR #48 bleibt geschlossen/ungemergt.

## 2026-11-07 – Paralleles K2-Dokument aufgelöst: ergänzende Inhalte aus PR #48

**Anlass:** Der Nutzer fragte nach den offenen PRs #48 und #52 und genehmigte, PR #52 zu mergen sowie aus #48 die noch wertvollen Inhalte gesondert zu sichern und #48 anschließend als überholt zu schließen.

**Ergebnis:** PR #52 wurde nach `main` gemergt. Die aus PR #48 stammende abweichende **Hauptspezifikation** für K2 wird **nicht** in die bereits freigegebene [K2_KOMMUNIKATIONSCOACH.md](K2_KOMMUNIKATIONSCOACH.md) hineingemergt. Maßgeblich bleiben K2 aus PR #49 und der dazugehörige [K2_FEEDBACK_QUALITAET_TESTPLAN.md](K2_FEEDBACK_QUALITAET_TESTPLAN.md).

**Als eigenständige, zusätzliche Konzeptanhänge im neuen PR gesichert:**
- [K2_COACH_BEISPIELE.md](K2_COACH_BEISPIELE.md): 20 alternative deutschsprachige Beispiel- und Kontextfälle; einzelne Hobbys an K3-Personakanon angepasst.
- [K2_COACH_QUALITAET.md](K2_COACH_QUALITAET.md): 36 ergänzende Prüfungen, Kontrast- und Evaluationsideen.

**Redaktionelle Schutzregeln:** Beide Anhänge tragen einen ausdrücklichen Hinweis auf ihre ergänzende/nicht normative Rolle. Bestehende Beleg-, Safety-, Datenschutz- und Unsicherheitsregeln aus K1–K3 gelten weiterhin. Doppelungen in den Testplänen werden vor einer späteren CI-Umsetzung dedupliziert. Keine Tests werden als ausgeführt dargestellt.

**Genehmigungslage:** Nutzer hat die *Sicherung* und das **Schließen des alten PR #48 nach Sicherung** genehmigt. Der neu angelegte Ergänzungs-PR erhält keine automatische Merge-Freigabe; er bleibt offen bis zu einer neuen ausdrücklichen Nutzerentscheidung.

## 2026-11-07 – Konzeptstand 0.7 (PR #52 gemergt): UX und Bedienkonzept

**Auftrag:** Der Nutzer hat PR #50 ausdrücklich freigegeben und die Fortsetzung mit K4 beauftragt. PR #50 wurde in main übernommen und anschließend verifiziert.

**Neu ausgearbeitet:**
- [K4_UX_KONZEPT.md](K4_UX_KONZEPT.md): umfassende Informationsarchitektur mit vier Haupttabs, schneller Einstieg, freie Chats, Szenario-Training, Coach auf Wunsch, Replay, Charaktereditor, Fortschritt, Datenschutzbedienung, Android-Konventionen, Barrierefreiheit und Fehlerzustände.
- [K4_NUTZERABLAEUFE.md](K4_NUTZERABLAEUFE.md): zwölf end-to-end Abläufe mit realistischen Negativfällen und fünf textuellen Wireframes.
- [K4_UX_TESTPLAN.md](K4_UX_TESTPLAN.md): 36 UX-Prüfaufgaben, K1–K3-Integration, Bedienhilfen, Dark-Pattern-/Vertrauensrisiken und Priorisierung.
- [k4_screens_v1.json](../specs/k4_screens_v1.json): 29 UX-Ansichten mit Aktionen, UI-Zuständen, Navigations- und Abbruchregeln.

**Vorgeschlagene Leitentscheidungen:**
- Vier primäre Tabs: Chats, Üben, Fortschritt, Profil; im zweiten Bereich lautet die Hauptüberschrift „Entdecken & Üben“.
- Erster KI-Chat ohne langen Fragebogen oder Big-Five-Pflichtkonfiguration.
- Chatmodus klar als frei oder Übung gekennzeichnet; KI-Charakter jederzeit erkennbar; Coach im freien Modus ausschließlich freiwillig.
- Kurzfeedback evidenzbasiert und ggf. ohne Verbesserungspunkt; Fehler werden meldbar und korrigierbar.
- Replay verzweigt den Verlauf, überschreibt niemals das Original und behauptet keine zuverlässige reale Wirkung.
- Fortgeschrittener Charaktereditor und sehr komplexe Fortschrittsmetriken bleiben zunächst optional/später.
- XP ist Übungsaktivität, kein globaler Sozialkompetenzwert. Ein höfliches Gesprächsende ist ein Erfolg.
- Erinnerung, Chatverlauf und Lernprofil erhalten getrennte echte Kontrollen – sofern K5 ihre technische Wirkung sicherstellt.
- Android-native Bedienung, TalkBack, dynamische Schriftgrößen, mindestens 48dp Touchziele, klare Fehlermeldungen und kontextbezogenes Zurück gehören zu den Qualitätsanforderungen.

**Offene Abhängigkeiten:** Gastmodus, server-/lokale Speicherung, Daten-/Löschgarantien, KI-Ausfallverhalten und Kostenmodell (K5); echte UX-/Nutzerprüfung und MVP-Freeze (K6).

**Statusaktualisierung (07.11.2026):** PR #52 wurde vom Nutzer ausdrücklich freigegeben, per Squash-Merge nach `main` übernommen und die Dokumentation dort überprüft. K4 bleibt eine Konzept-/UX-Spezifikation; noch keine Android-UI oder Usability-Tests ausgeführt.

## 2026-11-06 – Konzeptstand 0.6 (PR #50 gemergt): Fünf Personas und konkretes Simulationsmodell

**Auftrag:** Der Nutzer hat PR #49 ausdrücklich freigegeben und K3 beauftragt. Der Merge wurde verifiziert. K3 wird in einem eigenständigen Entwicklungsbranch vorgelegt.

**Neue Unterlagen:**
- [K3_CHARAKTERE.md](K3_CHARAKTERE.md): detaillierte Beschreibungen von Mara, Jonas, Elena, Deniz und Robin, einschließlich Dialogen, Grenzen, Gegenbeispielen und typischen Zustandsvariationen.
- [K3_SIMULATIONSREGELN.md](K3_SIMULATIONSREGELN.md): Persona-, Zustands-, Beziehungs- und Gedächtnisregeln, Übergänge, Dialogaktionen, Replay, Sicherheit und Kopplung an K1/K2.
- [K3_SIMULATION_TESTPLAN.md](K3_SIMULATION_TESTPLAN.md): 36 Prüffälle und 10 ausformulierte Negativ-/Kontrastfälle.
- [k3_personas_v1.json](../specs/k3_personas_v1.json): fünf versionierte kanonische Personas als spätere App-Fixtures.
- [k3_scenarios_v1.json](../specs/k3_scenarios_v1.json): sechs Szenarien mit Kompetenzen, Startzustand, möglichen Ausgängen und Coaching-Grenzen.

**Vorgeschlagene verbindliche Gestaltungsprinzipien:**
- Kein Persönlichkeitswert determiniert einzelne Nachrichten. Ruhige Figuren dürfen bei Interesse lebhaft sein; direkte Figuren bleiben respektvoll; verträgliche Figuren dürfen widersprechen.
- Szenario-Schwierigkeit verändert Gesprächsbedingungen, nicht die zugrunde liegende Person oder ihre Moral.
- Persona-Version bleibt pro Chat stabil. Kurzfristige Zustände ändern sich nur mit begründetem Ereignis. Beziehung ist keine Nachrichtenzahl-Zuneigungsleiste.
- Erinnerungen sind quellenbelegt, scope-begrenzt, berechtigt und löschbar; Lernprofil und Charaktergedächtnis bleiben getrennt.
- Figuren haben eigene Meinungen, Interessen, Grenzen und dürfen Gespräche beenden. Keine künstliche Bindungsmaximierung.
- K2 erhält sichtbare Texte und deklarierte Bedingungen, nicht vermeintlich „echte“ emotionale Innenzustände.
- Charakter-JSON und Szenario-JSON wurden auf Grundstruktur, Wertebereiche und Querverweise geprüft; **noch kein Modell-/Dialogtest ausgeführt**.

**Offen:** genaue KI-Modellwahl, Validierung der numerischen Designparameter, konkreter Editor, Speicher-/Löschtechnologie, betriebswirtschaftliche Parameter, reale Pilotqualität.

**Statusaktualisierung (07.11.2026):** Der Nutzer hat PR #50 ausdrücklich genehmigt; der PR wurde nach main gemergt und die K3-Dokumente sind dort vorhanden. Keine andere App wurde durch diesen PR verändert; noch keine Simulationsengine implementiert.

## 2026-11-06 – Konzeptstand 0.5 (PR #49 gemergt): K2 Kommunikationscoach und Feedback

**Auftrag:** Der Nutzer hat PR #47 ausdrücklich genehmigt und die professionelle, sorgfältige Ausarbeitung von K2 beauftragt. PR #47 wurde im Repository als bereits gemergt verifiziert; kein zweiter Merge ausgeführt.

**Neue Dokumente:**
- [K2_KOMMUNIKATIONSCOACH.md](K2_KOMMUNIKATIONSCOACH.md): Begrenzter Coaching-Auftrag; Nutzungsmodi; Beobachtungsschema mit Evidenzklassen; Belegprüfung; kontextabhängige Deutungen; Sicherheitspriorisierung; Kurz- und Detailfeedback; Live-Hilfe; Widerspruchs-/Korrekturverfahren; qualifiziertes, optionales Lernprofil; XP- und Sicherheitsgrenzen; Übergaben an K3–K6.
- [K2_FEEDBACK_QUALITAET_TESTPLAN.md](K2_FEEDBACK_QUALITAET_TESTPLAN.md): 16 ausformulierte Kontrastfälle, 32 Testfälle, Review-Raster, menschliche Kalibrierung und kritische Qualitätsgates.

**Konzeptionelle Vorschläge:**
- Jede Coaching-Aussage ist auf konkrete sichtbare Textstellen zu stützen; Interpretationen und mögliche Wirkungen dürfen nicht als innere Gewissheit dargestellt werden.
- Coach und Persona bleiben getrennt. Positives Verhalten der simulierten Figur ist kein Beweis für kommunikative Qualität des Nutzers.
- Eine Rückmeldung kann nur eine Stärke, eine Reflexionsfrage, ein „nicht beurteilbar“ oder einen Grenzhinweis enthalten. Es gibt **keine Pflicht zur Kritik**.
- Maximal eine priorisierte Versuchsidee im Kurzfeedback, abhängig von Beleg, Lernziel und Nutzerpräferenz.
- Grenzen, Privatsphäre und Sicherheit gehen vor Engagement, Nachrichtenlänge oder XP.
- Widerspruchs- und Korrekturmöglichkeiten sowie datensparsame, optionalisierte Lernprofilbeobachtungen werden explizit vorgesehen.
- Kritische Bewertungsfehler werden blockierend behandelt und nicht mit gutem Durchschnittsfeedback verrechnet.

**Noch offen:** faktische Implementierung, Modellentscheidung, kalibrierte Schwellen aus menschlichen Tests, exakte Onboarding-/Opt-in-Texte, technische Löschpfade und MVP-Balancing.

**Statusaktualisierung:** PR #49 wurde vom Nutzer ausdrücklich freigegeben und am 06.11.2026 erfolgreich nach main gemergt. K2 ist damit als Konzept aufgenommen; eine KI-Implementierung liegt weiterhin nicht vor.

## 2026-11-06 – Konzeptstand 0.4 (PR #47 gemergt): K1 Lern- und Kompetenzsystem

**Auftrag:** Nach Durchsicht der konzeptionellen Lücken wurde eine vorgeschaltete Konzeptabschlussphase K1–K6 empfohlen. Der Nutzer hat zugestimmt: Beginn mit K1, anschließend K2, K3, K4, K5 und K6.

**Neue K1-Dokumente:**
- [K1_LERN_KOMPETENZSYSTEM.md](K1_LERN_KOMPETENZSYSTEM.md) – Lernphilosophie, sechs Kompetenzbereiche mit vier Unterkompetenzen je Bereich, beobachtbare Verhaltensanker, Kontextabhängigkeit, Übungsformen, zwölf Beispiellektionen, Lernkreislauf und Schnittstelle zu K2.
- [K1_KOMPETENZ_PRUEFFELLE.md](K1_KOMPETENZ_PRUEFFELLE.md) – ausformulierte alternative Antwortpfade, 24 Fallprüfungen, Einordnung von Mehrdeutigkeit, Grenzschutz, fairer Bewertung und Nicht-Beurteilbarkeit.

**Wesentliche konzeptionelle Entscheidungen als neue Vorschläge:**
- Kompetenzfortschritt ist keine globale psychologische oder soziale Bewertung und folgt keinen simplen Nachrichtenmetriken.
- Mehrere Antworten können situativ angemessen sein; Interesse, eigener Beitrag und respektvolle Zurückhaltung werden miteinander in Beziehung gesetzt.
- Kurzfeedback verlangt konkrete Textbelege und erlaubt „kein Verbesserungspunkt belegbar“.
- Ein klares Nein wird als Grenze respektiert; kurze oder endende Unterhaltungen sind nicht automatisch schlechte Gespräche.
- Lernprofil und Coaching werden getrennt; langfristige Beobachtungen erfordern wiederholte vergleichbare Kontexte.
- Keine impliziten Defizitzuschreibungen aus kulturellen, neurodivergenten oder introvertierten Kommunikationsweisen.
- K1 liefert das Fachmodell; K2 definiert später konkrete Coach-Auswahlregeln, Qualitätsschwellen und Rückmeldeformen.

**Statusaktualisierung:** Der Nutzer hat PR #47 ausdrücklich genehmigt; der PR ist bereits gemergt und die K1-Dokumente liegen auf main. Die K1-Konzeption ist damit aufgenommen, ohne dass dies eine Implementierungsfreigabe darstellt.

## 2026-11-06 – Konzeptstand 0.3 (PR #45 gemergt): M2 Charakter- und Simulationssystem

**Anlass:** Der Nutzer hat PR #44 ausdrücklich freigegeben und als nächstes die detaillierte Konzeption des Charakter- und Simulationssystems beauftragt. PR #44 ist nach main gemergt.

**Neue Arbeitsdokumente im separaten M2-Konzept-PR:**
- [CHARAKTER_SIMULATION.md](CHARAKTER_SIMULATION.md) – acht Komponenten, PersonaSpec, Kommunikationsachsen, situativer Zustand, Beziehungen und Erinnerungen, Szenarien, Schwierigkeit, Dialogpolitik, Lebenszyklus, UX-Editor, MVP und Unteretappen M2.1–M2.6.
- [CHARAKTER_SIMULATION_TESTPLAN.md](CHARAKTER_SIMULATION_TESTPLAN.md) – Prüffälle, menschliches Bewertungsraster, Qualitätsgates und Testorganisation.

**Wesentliche vorgeschlagene Präzisierungen:**
- Big Five sind weiche Hintergrundtendenzen, konkrete Kommunikationseigenschaften werden getrennt modelliert.
- Stimmung, Energie, Aufmerksamkeit, Gesprächsinteresse und Gesprächsbereitschaft werden nicht gleichgesetzt.
- Charaktere behalten eigene Interessen, Grenzen, Meinungen und die Fähigkeit, Gespräche zu beenden.
- Erinnerungen haben Quellen, definierte Zugriffsrechte und Lösch-/Korrekturmöglichkeiten.
- Gesprächsregie wird von der Sprachgenerierung logisch getrennt; der Coach bleibt separate Funktion.
- Schwierigkeit entsteht durch eine Gesprächsdynamik, nicht durch den Persönlichkeitswert.
- Persona-Versionen sind pro laufendem Chat stabil; Wiederholungsübungen halten den Ausgangskontext fest.
- Qualitätsprüfungen beinhalten lange Chats, Grenzen, Datenschutz, Prompt Injection, Sykophanzie und realistische Variabilität.

**Statusaktualisierung:** PR #45 wurde vom Nutzer ausdrücklich freigegeben und nach main gemergt. Die M2-Detailkonzeption ist damit dokumentiert; es wurde noch keine App implementiert.

## 2026-11-06 – Konzeptstand 0.2: bisherige Verbesserungsvorschläge vollständig aufgenommen

**Rückmeldung des Nutzers:** Die bisher vorgeschlagenen Verbesserungen, Konkretisierungen und Ideen werden insgesamt befürwortet. Sie sollen als inhaltliche Arbeitsgrundlage erhalten bleiben.

**Ergänzungen gegenüber 0.1:**
- Detaillierter Charaktereditor (einfach/erweitert), Big-Five-Grundlage, situative Zustände, Beziehungskontext, Speichern und Bearbeiten.
- Charakterkonsistenz, glaubwürdige Gesprächsausgänge, Zustandspflege und realistische Testfälle.
- Konkrete beobachtbare Kompetenzsignale; Unsicherheit und Verzicht auf pauschale Diagnosewerte.
- Präzise Coach-Regeln, Beispiele, Feedbackmeldungen, menschlich validierte Testdialoge und Fehlerkorrektur.
- Freiwillige Mini-Lektionen, Lernpfade und kleine reale Alltagsschritte.
- Beispiel-XP, getrennte Spiel-/Lernfortschritte, positive Erfolge und Verzicht auf manipulative Belohnung.
- Vereinfachtes Onboarding, mögliche Gastnutzung, Character-Verwaltung und UX-Grundsätze.
- Mehr Datenschutz-, Sicherheits- und Qualitätsanforderungen sowie Pilotierung und Lernwirkungsprüfung.

**Wichtig:** Die grundsätzliche Befürwortung gilt der **Produktkonzeption**, nicht einer automatischen Freigabe jeder Detailausprägung oder eines PR-Merges. Alle Änderungen sind derzeit nur auf dem Entwicklungsbranch von PR #44; `main` bleibt bis zur ausdrücklichen Freigabe unverändert.

**Dokumentreferenz:** [KONZEPT.md](KONZEPT.md), insbesondere Abschnitt 18.

## 2026-11-06 – Konzeptstand 0.1

**Entscheidungen aus der bisherigen Unterhaltung:**
- Die App fokussiert zunächst Alltagskommunikation, soziale Kontakte und persönliche Weiterentwicklung.
- Freies Chatten, gezielte Übungen, wiederholbares Ausprobieren und individuelles KI-Feedback sind zentrale Bestandteile.
- KI-Charaktere sollen unterschiedliche Persönlichkeiten, Kommunikationsstile und situative Stimmungen zeigen; eigene Charaktere sollen erstellbar sein.
- Laufende und selbst erstellte Chats sollen in einer Übersicht zugänglich sein.
- Spielerische Motivation mit XP und Leveln wird vorgesehen, ohne lange Gespräche oder Beeinflussung anderer Menschen als primären Erfolg zu belohnen.
- Das GitHub-Repository `valoon4/Androidbaze` wird als Ablage genutzt; andere Apps bleiben unberührt.
- Vor jedem Merge eines Entwicklungsbranches in `main` ist ausdrücklich die Genehmigung des jeweiligen Pull Requests einzuholen.

**Aus der kritischen Konzeption resultierende, vorläufige Vorschläge:**
- Sechs Kompetenzbereiche, drei verbundene Nutzungsmodi, fünf Schichten pro Charakter.
- Unterschiedliche Gesprächsdynamiken als Schwierigkeitsmodell.
- Coach mit optionaler Analyse, Testdialogen und vorsichtigen Langzeitbeobachtungen.
- Vier Hauptbereiche in der Navigation sowie MVP und Meilensteine M1–M5.

**Noch offen:** App-Name, detaillierte Charaktersimulation und Bewertungsregeln, technische Implementierung, konkrete UX, KI-Anbieter, Datenschutzkonzept, Kostenmodell und exakte MVP-Spezifikation.

**Dokumentreferenz:** [KONZEPT.md](KONZEPT.md)

## Pflegehinweise

Bei jeder wesentlichen inhaltlichen Änderung:
1. `KONZEPT.md` aktualisieren.
2. Hier Datum, neue Entscheidungen, überarbeitete Annahmen und offene Punkte festhalten.
3. Im PR klar zwischen inhaltlichen Änderungen und technischen Änderungen unterscheiden.
4. Vor Merge ausdrückliche PR-Genehmigung einholen.
