# M6.3.3 – UX-/Qualitätsrunde der Emotionskompass-Strategiewelt

**Stand:** 30.10.2026 · **Scope:** 30 Strategiekarten, 82 Bedürfnisse, 32 Wissensartikel, 7 Lernreihen, 15 Praxisfälle.
**Status:** Code- und Inhaltsverknüpfungsreview, gezielte Korrekturen und Regressionstests im Draft-PR. Unabhängiges Fachgutachten und reale Geräteabnahme **nicht durchgeführt**. Kein Merge ohne Freigabe.

## 1. Kritische Bestandsaufnahme (vor den Korrekturen)

| Priorität | Gefundener Fehler | Konkrete Nutzerfolge | Änderung |
|---|---|---|---|
| Hoch | 30 Strategiekarten enthalten Bedürfnis-IDs, zeigen diese nicht | Ein Kernzusammenhang ist nur intern im Katalog vorhanden | Benannte, direkt erreichbare Bedürfnisdetails |
| Hoch | In der Gegenrichtung fehlen passende Strategiekarten | Bedürfnisatlas endet bei einem Freitextbeispiel; kein Atlasübergang | Nur explizit in den Daten zugeordnete Karten, höchstens drei, plus ehrlicher Leerzustand |
| Hoch | Praxisfälle führen nur in die allgemeine Strategieübersicht | Nutzer verliert den Bezug zur Frage | Verlinkte Karten einzeln mit ihrem echten Namen, Fall-ID bleibt beim Rückweg erhalten |
| Hoch | Strategieverweise auf Wissensartikel teils als „Artikel ansehen“ | Keine Entscheidung nach Inhalt möglich | Echte Titel aus dem aktuellen Wissenskatalog statt statischer Titel-Switch |
| Hoch | Die 12 neuen Artikel sind nicht direkt in 30 Strategiekarten verknüpft | Kaum Entdeckung der Vertiefungen trotz hohem Contentumfang | Ein explizit redaktionell kuratierter Link pro Karte, ergänzt die alten Links |
| Mittel | „Coping“ wird vorausgesetzt, ausführliche Einführung oberhalb der Suche | Hohe kognitive Einstiegshürde | Zweizeilige Einordnung, direkt verlinktes Glossarkapitel, lange Einordnung optional |
| Mittel | Strategie-Details setzen wichtige Grenzen erst nach mehreren Abschnitten | Sicherheit und Passung nicht sofort erfassbar | „Auf einen Blick“: mögliche Passung und Grenze direkt nach Definition, danach vertiefte Vor-/Nachteile |
| Mittel | Rücksprung nach einer Alternative führt immer zur Atlasliste | Kontextverlust beim Vergleichen | Lokale, begrenzte Navigationshistorie mit Android-Zurück |
| Mittel | Suche umfasst nicht Reflexionen/Fehlannahmen/Feedback | Relevante Inhalte werden nicht gefunden | Volltextindex auf vorhandene Felder ausgeweitet, keine neue Personensuche |
| Mittel | Quellenlinks wiederholen generische Bezeichnungen | TalkBack und Sichtprüfung können mehrere Quellen schlecht unterscheiden | Nummerierung und aussagekräftigere Quellenlabels |
| Mittel | Fallfragen bieten nur allgemeinen Atlas-Absprung | Konkretes Lernen wird unterbrochen | Echte fachliche Titel und gezielte Querverweise |
| Niedrig | M6.3.2-Fallbereich steht als letztes horizontales Untertab | Auf kleinen Displays weniger sichtbar | Untertabs neu angeordnet, Fallbereich direkt nach Artikel |

## 2. Prinzipien und bewusste Grenzen

- **Nicht mehr Inhalte, sondern bessere Orientierung.** Hauptnavigation bleibt bei fünf Zielen; darunter drei gleichrangige Atlanten. Keine neuen Bottom-Tabs.
- **Keine automatische Gefühls→Bedürfnis→Strategie-Kette:** Links beziehen sich ausschließlich auf ausdrücklich redaktionell erfasste Katalog-IDs, niemals auf Nutzergefühle, Tagebuchtext oder private Merkmale.
- **Keine irreführenden Treffer:** Für viele der 82 Bedürfnisse existiert derzeit kein einzelner fachlich geprüfter Atlas-Link. Statt aus Synonymen eine Empfehlung zu erfinden, zeigt die App dort einen ehrlichen Hinweis und die neutrale Möglichkeit, Strategien selbst zu entdecken.
- Erklärende **Vermeidungs-/Emotionsunterdrückungs-Muster** werden nicht als mögliche Wege in den Bedürfnisdetails vorgeschlagen, bleiben aber im Strategieatlas für Bildungszwecke lesbar.
- Kurzfristiger Nutzen, Belastungsgrenzen und Sicherheit werden ohne Heils- und Therapie-Versprechen vermittelt. Die didaktischen sieben Familien bleiben ausdrücklich **keine empirisch validierte Taxonomie**.
- Querverweise verknüpfen **alle 30 Karten mit jeweils einem der 12 neuen Vertiefungsartikel** und erhalten ältere Artikel-IDs. Höchstens drei benannte Artikel pro Karte, damit Detailseiten nicht zu einer Linkliste werden.
- Die 15 erfundenen Praxisfälle verlinken ihre bereits vorhandenen, über IDs geprüften Strategien; Auswahl/Antworten bleiben flüchtig im Arbeitsspeicher.
- Keine neue Netzwerkberechtigung, kein neues Nutzertracking, keine Änderungen an Journalverschlüsselung, Backup oder Lernstandsformat.

### Abdeckung und bewusste Nicht-Zuordnung

Die 30 Strategiekarten enthalten **48 Bedürfnis-Referenzen, aber nur 8 verschiedene Bedürfnis-IDs**. Von den 82 Bedürfnisdetails erhalten daher viele bewusst **keine** direkte Strategiekartenliste. Die neue neutral formulierte Atlas-Verlinkung bleibt offen, statt 74 ungeprüfte Zuordnungen automatisch zu erzeugen. Eine größere, tatsächlich fachlich kuratierte Verknüpfungsabdeckung wäre ein **eigener redaktioneller Auftrag**, kein Nebenprodukt einer UI-Qualitätsrunde.

Die 30 Karten enthalten **62 Quellenverweise auf nur 16 unterschiedliche URLs**. Besonders ein theoretischer Überblick von Bonanno/Burton wird für viele Karten wiederverwendet. Der bloße Link reicht deshalb **nicht** als Beleg dafür, dass alle konkreten Behauptungen über Nutzen, Nachteile oder Sicherheit der einzelnen Karte empirisch belegt sind. Die fachliche Claim-by-Claim-Prüfung bleibt hoch priorisiert; schwächere Belege sollten später gekennzeichnet oder spezifischer ersetzt werden.

## 3. Quellen- und Inhaltseinschätzung (stichprobenartig)

Für das konzeptuelle Grundgerüst wurden die folgenden Quellen-IDs anhand der veröffentlichten Artikel-/Abstractangaben abgeglichen:
- Bonanno & Burton (2013), Regulatory Flexibility, PubMed 26173226. Kontext, Repertoire, Feedback; **theoretischer Rahmen**, keine Wirksamkeitszusage pro Karte.
- Skinner et al. (2003), Searching for the structure of coping, PubMed 12696840. Kritik grober Kategoriensysteme; unterstützt **nicht** die Validität unserer sieben Familien.
- Gross (2015), Emotion Regulation: Current Status and Future Prospects, DOI 10.1080/1047840X.2014.940781.
- Troy et al. (2013), Cognitive reappraisal and context, PubMed 24145331. Beobachtende Kontextassoziationen; **keine** einfache individuelle Behandlungsregel.
- Cohen & Wills (1985), Social support and the buffering hypothesis, PubMed 3901065. Älterer Forschungsüberblick, kein Wirksamkeitsbeweis für einzelne Dialogbeispiele.

**Nicht behauptet:** Vollprüfung aller Katalog-URLs, Claim-by-Claim-Zuordnung sämtlicher Nutzen-/Risikoaussagen, externe psychologische Fachfreigabe, individuelle medizinische Wirksamkeit. Diese offenen Punkte bleiben Release-Gates.

## 4. Automatisierbare Prüfpunkte

- 30 Karten haben eindeutige redaktionelle Vertiefungslinks, genau die 12 neuen Artikel werden insgesamt erreicht.
- Alle vorhandenen alten Artikel-/Lernreihen-IDs bleiben erhalten und mit echten Titeln auflösbar.
- An Bedürfnisdetails nur explizit zugeordnetes, begrenztes, nicht zu problematischen Mustern gehöriges Material.
- Alle 15 Praxisfälle erreichen benannte Strategiedetails; keine fehlenden IDs.
- Volltextsuche findet auch Reflexionen, Grenzen, Quellenbeschreibungen, Fallfeedback und Vergleichsoptionen.
- Keine automatische Inferenzen aus persönlichen Daten, keine geänderten Keystore-/Journaldateien.
- CI: **alle** JVM-Tests, assembleDebug, APK-Identität, Signatur und SHA-256 erfolgreich.

## 5. Manuelle Android-/Fach-Abnahme vor dem Merge oder öffentlichen Release

1. Handy klein (360dp und 320dp), Android-Schrift 100/200 %, Hoch- und Querformat: keine abgeschnittenen Navigationselemente, Buttons oder Pflichttexte.
2. Tablet ab 600dp: NavigationRail, ausreichend große Inhalte und erreichbare Zurück-Aktionen.
3. TalkBack: Untertab-Auswahl, Quellen 1/2, „Auf einen Blick“, Bedürfnis- und Strategienamen, Fallantworten und Feedback sinnvoll vorgelesen; Fokus bei Deep-Links nachvollziehbar.
4. Drei Durchgänge: **Strategie → Bedürfnis → zurück**, **Bedürfnis → Strategie → zurück**, **Praxisfall → Strategie → zurück**, zusätzlich **Strategie → Artikel → zurück** und **Strategie A → Alternative B → zurück zu A**.
5. Suche und Filter beim Themenwechsel erhalten, korrektes Verhalten bei Rotation und Android-Prozesswiederherstellung.
6. Offline-/Flugmodus: alle Inhalte und interne Links ohne Netzwerk, externe Quellen nur auf expliziten Tap.
7. Fachpsychologische unabhängige Prüfung aller Aussagen sowie Sicherheitsprüfung bei unsicheren Beziehungen, Körperübungen, Akzeptanz und Vermeidung.

**Status der Geräte- und externen Fachchecks:** ausstehend, da kein Android-Gerät/qualifizierter externer Gutachter hier tatsächlich bedient bzw. beauftragt wurde. M6.3.3 ist eine Code-/UX-Qualitätsrunde, kein klinisches Review.
