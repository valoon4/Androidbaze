# M6.3.1 – Strategieatlas: Ergänzung auf 30 Strategiekarten

**Stand:** 30.10.2026 · **Status:** Inhaltliche Erweiterung und technische Integration im Draft-PR; fachliche Prüfung auf Unabhängigkeit und reale Gerätetests stehen noch aus.
**Basis:** M6.3.1 erster Fünfer-Katalog, in main durch PR #18. **Ziel:** die ursprünglich geplanten 30 Strategiekarten vor M6.3.2 vollständig verfügbar machen.

## Umfang

Die fünf bestehenden Referenzkarten bleiben im ursprünglichen Katalog und unverändert in ID und Inhalt. 25 eigenständige Ergänzungen verteilen sich auf **neun versionierte lokale JSON-Dateien** nach Themenbereich: Situation (5), Regulation A/B (3+3), Kognition A/B (3+2), soziale Strategien (3), Werte (2), Erholung (2) und problematische Muster (2).

**30 Karten insgesamt** mit mindestens zwei möglichen Vorteilen und Nachteilen, Alltagsbeispiel, passender Situation, Grenzen, Missverständnis, korrigierender Einordnung, Reflexionsfrage, ungefährlicher freiwilliger Anregung, stabiler ID, Verweisen und transparenten Quellenarten. Zwei Karten zu langfristiger Vermeidung und Unterdrückung sind Erklärungs-/Reflexionskarten – keine praktische Anleitung zur Ausführung.

Die **sieben Atlasfamilien** sind eine eigens entwickelte didaktische Navigation, keine empirisch validierte oder universelle Taxonomie. Mehrfachzuordnung ist ausdrücklich vorgesehen; Querverweise sind redaktionelle Möglichkeiten, keine automatischen Deutungen aus persönlichen Daten. Der genaue Nutzen ist von Situation und Person abhängig.

## Datenarchitektur und Kompatibilität

Ein Ausgangskatalog (`catalog/strategies.de.v1.json`) + neun neue JSON-Teilkataloge mit `schemaVersion:1` und `locale:de` werden **ausschließlich offline** zu einem Speicherobjekt zusammengeführt. Die bestehende strikte `StrategyCatalog.fromJson`-Validierung prüft das gesamte Ergebnis inklusive referenzieller Integrität über alle Teilkataloge hinweg. Ein defektes Katalogteil führt zu einer kontrollierten Fehlermeldung im Strategieatlas, beeinträchtigt aber weder Tagebuch noch andere Atlanten.

Der ursprüngliche Katalog bleibt rückwärtskompatibel mit `fromJson`; `fromJsonParts` ist die neue Erweiterung. Ursprüngliche IDs, Artikel-IDs, Bedürfnis-IDs und Journalformat werden nicht verändert. **Kein zusätzlicher persönlicher Datenspeicher**, keine neue INTERNET-Berechtigung, keine Profile/Experimente. Die Integration mit späteren M7-Experimenten erfordert gesonderte Freigabe.

## Wissenschaftliche Orientierung und Grenzen

Als Ausgangspunkte dienen Bonanno & Burton (2013) zur kontextbezogenen Flexibilität, Skinner et al. (2003) zu Coping-Klassifikationen, Gross (1998) zur Emotionsregulation, sowie WHO/PAHO, NHS und CDC als frei lesbare Praxisquellen. Die Karten benennen Quellentypen. Diese Belege sind **keine individuellen Wirksamkeitsnachweise für alle beschriebenen Übungen**. Insbesondere bei Körper- und Atemwahrnehmung, unsicheren Beziehungen, Exposition/Vermeidung und emotionaler Unterdrückung gelten besondere Einschränkungen. Keine Diagnosen oder Therapieanweisungen.

## Testmatrix

- 30 eindeutige Strategie-IDs, die bisherigen 5 vollständig unverändert, 7 befüllte Familien.
- Strenge Kartenpflichtfelder und gültige Verweise auf andere Strategien, Bedürfnisse, Wissensartikel.
- Falsche Teilkatalog-Version, doppelte Strategie-ID und defekte Referenz werden abgelehnt.
- Suche mit Umlaut-/ASCII-Varianten, Synonymen, Themenfilter, Kombination und Leerzustand.
- 0/5/30-Karten- sowie beschädigter Katalogzustand: übrige App bleibt zugänglich.
- Vor Merge nachweisbar: Kotlin-JVM-Testlauf, Debug-APK, Paketkennung, Signatur und Hash per GitHub Actions.
- **Ungeprüft bis zur Abnahme:** 200-%-Schrift, TalkBack, Dark Mode, kleine und große Displays, System-Zurück, Strategie-zu-Wissensartikel-Sprung, Flugmodus, reale Geräte.
- **Fachlicher Review ausstehend:** Quellenbehauptungen pro Karte, Grenzen, nichtdiagnostische Sprache, Sicherheit und Verwechslungsgefahren.

## Was nicht enthalten ist

Die **12 vertiefenden Wissensartikel, drei geführten Lernreihen und 15 Vergleichs-/Fallübungen sind M6.3.2** und werden erst nach Freigabe der inhaltlichen Grundlage als eigener Arbeitsschritt umgesetzt. Kein Merge dieses Draft-PRs ohne ausdrückliche Genehmigung.
