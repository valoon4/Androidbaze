# K6 – Gesamtkonzept-Audit: Konflikte, Risiken und Entscheidungen

**Version:** 1.0 · **Datum:** 07.11.2026 · **Status:** fachliche Abschlussprüfung zur Freigabe, keine implementierte/validierte App.
**Produkt:** CommunicationTrainer (Arbeitstitel) · **Repo-Grenze:** ausschließlich apps/CommunicationTrainer/.
**Grundlagen:** [Produktkonzept](KONZEPT.md), [K1](K1_LERN_KOMPETENZSYSTEM.md), [K2](K2_KOMMUNIKATIONSCOACH.md), [K3](K3_SIMULATIONSREGELN.md), [K4](K4_UX_KONZEPT.md), [K5](K5_TECHNISCHE_ARCHITEKTUR.md).
**Weitere K6-Artefakte:** [MVP](K6_MVP_UMFANG.md), [Umsetzungsplan](K6_UMSETZUNGSPLAN.md), [Qualitätsgates](K6_ABNAHME_UND_ENTSCHEIDUNGEN.md), [Scope-Vertrag](../specs/k6_mvp_scope_v1.json).

## 1. Maßstab und Gesamturteil

Der pädagogische Kern **Erleben → Verstehen → Weiterentwickeln** ist sinnvoll. K1 setzt keine normierte Persönlichkeitsskala voraus; K2 verlangt Belege, K3 eigenständige Figuren, K4 eine unaufdringliche Bedienung und K5 datensparsame Inferenz mit Sicherheitsgates.

**Die größte Lücke ist keine fehlende Funktion, sondern das Fehlen eines realistischen ersten Implementierungsumfangs.** Ein MVP mit fünf Figuren, vielen Szenarien, umfassendem Charaktereditor, beliebigem Replay, Live-Coach, XP, Langzeitprofil, mehreren KI-Betriebsarten, Datenschutz- und Betriebsinfrastruktur wäre vor erstem End-to-End-Test zu riskant.

**Prioritäten:** P0 = Safety/Datenschutz/Datenverlust/unerlaubte Zugriffe/irreführende Aussagen, Blocker vor betroffener Beta oder Veröffentlichung. P1 = Kernqualität, begrenztes MVP, Kostenkontrolle und UX. P2 = Ausbauoption.

**Wichtig:** „In K6 geklärt“ heißt **Konzeptentscheidung vorgeschlagen**, nicht technische Umsetzung oder bestandener Test. Der Nutzer muss den K6-PR separat freigeben.

## 2. Nachvollziehbarer Abgleich K1–K5

| ID | Priorität | Befund / Widerspruch | K6-Lösungsvorschlag | Stand |
|---|---|---|---|---|
| R01 | P1 | Hauptkonzept nennt 8–12 Szenarien, K3/K4 sechs | V1 nutzt die **sechs** versionierten Szenarien S01–S06; 8–12 bleibt Ausbauziel | K6 vorgeschlagen |
| R02 | P1 | K1 hat zwölf Lernartikel, vollständige Erstellung vor App-Test teuer | Alpha 1, V1 3 geprüfte Mini-Lektionen; zwölf als Curriculum-Backlog | K6 vorgeschlagen |
| R03 | P1 | K4 beschreibt 30 Ansichten und 99 Aktionen | 30 semantische Screen-IDs sind **keine** Verpflichtung zu 30 einzelnen Layouts; etwa 11–14 wiederverwendbare Vorlagen als UI-Entwurfsziel | K6 vorgeschlagen |
| R04 | P1 | K3 definiert fünf Figuren, alle zugleich zu implementieren kostet QA | Alpha testet Mara, Jonas, Elena; V1 behält nach Tests alle **fünf** (inkl. Deniz/Robin) | K6 vorgeschlagen |
| R05 | P1 | Eigener Charaktereditor aus K4-MVP erfordert Versionierung, Safety, Datenlöschung | V1.1 statt V1; zunächst fünf sorgfältig kuratierte Charaktere | K6 vorgeschlagen |
| R06 | P1 | Vollständiger Replay/Deep-Coach vervielfacht Modellkosten und UI-Zustände | V1: **eine** alternative Antwort/Branch + kurzer Coach mit Quellenbeleg; komplexe Galerie später | K6 vorgeschlagen |
| R07 | P1 | Vier Tabs, Fortschritt darf aber keine Scheinkompetenz messen | Alpha nur Chats/Üben; V1 vier Tabs mit Übungen/XP und freiwilliger Reflexion, **kein Prozent-Skill** | K6 vorgeschlagen |
| R08 | P0 | ChatGPT-Kontoanbindung aktuell für dieses Android-/Vertriebsmodell nicht bestätigt | Experiment hinter Gate, **kein V1-Versprechen**; nur offizieller OAuth, nie Passwort-/Cookie-Umweg | OFFEN |
| R09 | P1 | Lokale Open-Weight-KI könnte bei Deutsch/Coach/älteren Geräten unbrauchbar sein | Früher K2/K3- und Hardwarevergleich mit Cloudreferenz; ein geprüfter Standardmodus für V1 | OFFEN / Experiment |
| R10 | P0 | Cloudprovider müssen Text zur Inferenz verarbeiten; „E2EE“ wäre irreführend | Datenziel, Zweck und Grenzen vor Übermittlung erklären; keine E2EE-Behauptung | OFFEN / Architektur |
| R11 | P0 | Server ohne Chatarchiv, aber mit Idempotenz, Budgets und Abuse-Logging | Minimaler serverseitiger Request-/Status-/Kostenledger mit begründeter TTL; keine stillen Rohchatlogs | OFFEN / Implementierung |
| R12 | P0 | Lokale Chats, Auto-Backup, Löschung und Restore können widersprechen | Backup-/Wiederherstellungs-/Löschpfad testen; nie voreilig „überall gelöscht“ behaupten | OFFEN / Test |
| R13 | P0 | Android-App-Unterbrechung kann Entwürfe oder Inferenzzustände vernichten | persistente Entwürfe vor Netzaufruf, Request-ID/Statusabfrage/Idempotenz, Prozess-Tod-Test | OFFEN / Test |
| R14 | P0 | K2 könnte erfundene Belege, Diagnosen oder falsche Sicherheit ausgeben | Grounding, NOT_ASSESSABLE, Grenz-Gate und unabhängige Human-Eval vor Coach-Beta | OFFEN / Test |
| R15 | P0 | Figurenreaktionen sind kein Beweis über Gefühle echter Menschen | keine Zuneigungsanzeige, Nein respektieren, natürliches Gesprächsende; keine Überredungs-Belohnung | OFFEN / Test |
| R16 | P0 | Langzeit-Memory und KI-Lernprofile benötigen komplexe Consent-/Löschkontrollen | Nicht in V1; **lokal gespeicherter sichtbarer Chatverlauf** bleibt aber möglich | K6 vorgeschlagen |
| R17 | P0 | Play-Anforderung In-App-Meldung problematischer KI-Ausgaben | K5-K4-Nachtrag vorhanden; Berichtskanal samt Datenschutzvorschau **Pflicht vor Play** | Konzepteintrag, Umsetzung OFFEN |
| R18 | P1 | Es gibt keine gemessenen Modellpreise, Latenzen oder Geräte-/Hostkosten | Geräte-/Cloud-Spike und echtes Kosten-/Qualitätsprofil, bevor Modell/Preismodell gewählt wird | OFFEN / Messung |
| R19 | P0 | Gastmodus ohne Limits ermöglicht Kosten-/Missbrauchsangriffe | Geschlossener Einladungs-Pilot, später kontrolliertes Konten-/Gastmodell | OFFEN / Auth |
| R20 | P0 | Figuren könnten echte Personen oder emotionale Abhängigkeit vortäuschen | klares „KI-Charakter“-Label, keine Vermissens-/Online-Illusion, Safety-/Transparenztexte | Konzepteintrag |
| R21 | P1 | K4 zeigt umfangreiche Memory-/Account-/Provider-Controls, K5 kann nicht alle garantieren | Nur **implementierte, verifizierte** Toggles anzeigen; keine Scheinkontrolle | K6 vorgeschlagen |
| R22 | P1 | 24 K1-Unterkompetenzen sind für ersten Einstieg zu kompliziert | intern volle K1-IDs; sichtbares Lernziel je Szene und klare Alltagssprache | K6 vorgeschlagen |
| R23 | P1 | Wenige Übungschats belegen keine wissenschaftliche Kompetenzsteigerung | Kein „Du bist X % empathischer“ oder therapeutischer Wirksamkeitsanspruch | K6 vorgeschlagen |
| R24 | P1 | XP kann lange Chats, Offenbarung und Persona-Gefallen belohnen | XP für **freiwillige Lernaktivität**, nicht Chatlänge, Überreden oder intime Details | K6 vorgeschlagen |
| R25 | P0 | Recht, Vertrag, Modelllizenz, Moderation und Store-Policy nicht implementiert | Klare Compliance-/Sicherheits-Gates vor Beta/Play, keine vorweggenommene Freigabe | OFFEN |
| R26 | P1 | K1–K5 sind Dokumentation, keine getestete Android-App | Erst Mock-Provider-Skeleton und kleinster Vertical Slice, dann echte KI-Qualitätsgates | K6 vorgeschlagen |

## 3. Wo wir gerade **nicht** mehr erfinden dürfen

### K1/K2 – Coaching
- K1-Beobachtung bezieht sich auf konkretes **Verhalten in der Situation**, keine Nutzerpersönlichkeit.
- K2 darf „nicht beurteilbar“, „keine Verbesserung nötig“ und nachvollziehbar begrenzte Hypothesen ausgeben.
- Ein positives KI-Charakterecho ist **kein** Nachweis sozialer Kompetenz.
- **V1-Grenze:** kurzes Feedback nach ausgewählter Übung, freiwillig; keine permanenten KI-Schätzungen und kein langfristiger Fähigkeits-Score.

### K2/K3 – Simulation
- Persona-Versionen und eigenes Szenariogeschehen bleiben nachvollziehbar.
- Stimmung und Interesse ändern sich begründet, nicht alle Parameter nach jeder Nachricht.
- Der Coach darf geheime Simulationswerte **nicht** als Beleg innerer Gefühle verwenden.
- Replay demonstriert eine mögliche KI-Fortsetzung, keine kausale Realweltprognose.

### K3/K4 – Einfachheit
- Introvertiert ≠ schwierig, direkt ≠ unfreundlich, freundlich ≠ immer zustimmend.
- **V1:** fünf kuratierte Figuren, ein Lernziel je Übung, begrenzte Dynamik.
- Charaktereditor und komplexe Big-Five-Parameter später.

### K4/K5 – technische Wahrheit
- Dauerhaft gespeicherter **Entwurf** und Wiederaufnahme nach Android-Prozesstod sind ein wichtiges Kernnutzenversprechen.
- Eine App-Funktion ist nur aktiv, wenn die zugrunde liegenden Daten-/Backendoperationen wirklich existieren.
- Lokale Inferenz, ChatGPT-Kontingent, Cloud und eigener GPU-Server **sind Optionen**, keine garantierten bereits verfügbaren Betriebsarten.
- Keine heimliche Weiterleitung von lokalem Text an einen Cloudserver und kein heimlicher Wechsel auf bezahlte API.

## 4. Abwägungen mit Konsequenzen

| Entscheidung | Warum | Was wir verlieren | Wann erneut bewerten |
|---|---|---|---|
| 3 Personas in Alpha, 5 in V1 | schnell reale Qualität prüfen | spätere Testabdeckung aller Stile | nach Persona-Alpha-Eval |
| 6 Szenarien statt 8–12 | vorhandene kuratierte K3-Daten | geringere Vielfalt | nach Pilot |
| eigener Editor erst V1.1 | Versionierung, Qualität und Safety zuerst | weniger Personalisierung beim Start | nach stabiler 5-Persona-App |
| nur ein Replay-Zweig in V1 | Lernwert ohne Branch-Explosion | weniger Varianten | nach Kosten-/UX-Test |
| nur Kurzfeedback in V1 | belegtes Lernen statt Analyseflut | weniger Tiefe | nach K2-Kalibrierung |
| kein cross-session Memory in V1 | Datenminimierung und geringere Täuschung | keine Langzeitbindung an einzelne Figuren | nach expliziter Consent-/Datenprüfung |
| ein Inferenzmodus für V1 | keine ungetesteten Provider-Kombinationen | weniger Wahl am Anfang | nach Modellvergleich |
| vier Tabs erst V1 | Fortschritt sonst in Alpha zu dünn | Alpha weniger vollständig | nach vertikalem Chat-Flow |

## 5. Fazit / Beschlussstatus

**Fachliche Richtung ist tragfähig, aber noch nicht in Code oder Pilottests nachgewiesen.** Vorrang haben reale Chatqualität, evidenzbasiertes Feedback, natürlicher Umgang mit Grenzen, verlässliche Speicherung, Datenschutz und Kostenkontrolle.

K6 macht deshalb einen **zweistufigen Scope-Vorschlag**: sehr kleine technische Alpha vor erstem Pilot, danach fokussiertes **V1-MVP**. Ältere MVP-Listen in K1–K5 werden nicht gelöscht, sondern hier als früheres Ausbauziel gegenüber dem aktuell priorisierten MVP eingeordnet.

**Noch keine V1-Releasefreigabe, keine Implementierungsfreigabe und kein neuer K6-PR-Merge ohne die ausdrückliche Nutzerentscheidung.**
