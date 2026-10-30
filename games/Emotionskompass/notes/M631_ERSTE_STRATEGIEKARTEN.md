# M6.3.1 – Erste fünf Strategie-Referenzkarten (Implementierungs- und Reviewprotokoll)

**Stand:** 30.10.2026 · **Branch:** feature/emotionskompass-m631-strategieatlas-5 · **Version:** 0.6.3.1-m6.3.1 · **Status:** PR-Entwurf, nicht freigegeben, fachliche/UX-Geräteabnahme ausstehend.

## Nutzerwert
Aus **Wissenswelt → Strategieatlas** gelangen Nutzer zu fünf einzeln lesbaren offline vorhandenen Referenzkarten:
1. Probleme Schritt für Schritt angehen
2. Bewusste, zeitweise Ablenkung
3. Eine Situation neu betrachten (kognitive Neubewertung)
4. Soziale Unterstützung suchen
5. Annehmen, was gerade ist (Akzeptanz)

Jede Karte enthält Kurzdefinition, Zweck und geeignete Situationen, mindestens drei mögliche Vorteile und Nachteile, Grenzen, Alltagsbeispiel, typisches Missverständnis mit Auflösung, freiwillige Frage und Übungsanregung, Alternativen und nach Quellentyp ausgezeichnete externe Quellen. Das sind **keine persönlichen Empfehlungen** und **keine Therapie**.

## Architektur
- Neuer read-only Katalog: app/src/main/assets/catalog/strategies.de.v1.json; schemaVersion 1, eigene Inhaltsrevision, stabiler Strategie- und Familien-ID.
- Kotlin StrategyCatalog.fromJson mit strikten Schema-, Referenz-, Umfangs- und Inhaltschecks. Offene Weiterentwicklung: zusätzliche Strategiekarten im selben stabilen Format.
- Compose StrategyAtlasScreen mit Suche (auch umlaut-tolerant), sieben Filterfamilien und Detailansicht, die Vorteile/Grenzen gleichermaßen darstellt. Kein neuer Bottom-Tab.
- Vom Strategieartikel per stable ID zu vorhandener Wissenswelt und weiteren Referenzstrategien; zurück zur Strategieübersicht. Die 82 vorhandenen Bedürfnisbeispiele werden nicht automatisch gemappt, sondern bleiben zunächst unverändert. RelatedNeedIds dienen nur als geprüfte Metadaten für spätere M6.3.3-Verknüpfung.
- Fehlerhafte/inkompatible Strategie-Assets können die App nicht unbrauchbar machen: Strategieatlas zeigt einen Fehler, die bisherigen Atlanten und das verschlüsselte Tagebuch bleiben zugänglich.
- Bestehender Journalschlüssel, Backups, M6.2.1-Lernfortschritt und Sicherheits-Einstellungen bleiben unverändert. Keine neue INTERNET-Berechtigung und keine zusätzliche persönliche Speicherung.
- Version Code 15 / versionName 0.6.3.1-m6.3.1; eigener CI-APK-Artifact-Name und SHA-256.

## Quellen-/Evidenzstandard und Unsicherheit
Die Karten nennen unterschiedliche Quellenarten, darunter wissenschaftliche Theorieübersichten, Einzelstudien, Beobachtungsdaten, Meta-Analyse und öffentliches Material zur Selbsthilfe. **Nicht** jede benannte Quelle untersucht exakt alle beschriebenen Alltagssituationen. Aussagen über Nutzen und Risiken bleiben als mögliche, situationsabhängige Effekte formuliert. Starke Sicherheitshinweise gelten insbesondere bei tatsächlicher Gefahr, Gewalt, unsicheren Beziehungen, chronischem Vermeiden und bei der Verwechslung von Akzeptanz mit Duldung von Unrecht.

Quellenprüfung als Entwurfsbasis: Bonanno & Burton (2013, https://pubmed.ncbi.nlm.nih.gov/26173226/), Gross (1998, https://pubmed.ncbi.nlm.nih.gov/9457784/), Troy et al. (2013, https://pubmed.ncbi.nlm.nih.gov/24145331/), Ablenkungsstudie (https://pubmed.ncbi.nlm.nih.gov/28286372/), Cohen & Wills (1985, https://pubmed.ncbi.nlm.nih.gov/3901065/), Macri & Rogge (2024, https://pubmed.ncbi.nlm.nih.gov/38615492/), NHS (https://www.nhs.uk/every-mind-matters/mental-wellbeing-tips/self-help-cbt-techniques/problem-solving/), WHO (https://www.who.int/europe/publications/i/item/9789240003910). Keine ungeprüfte Übernahme urheberrechtlich geschützter Übungen.

## Tests und Review-Gates
- Unit: fünf stabile Referenz-IDs, sieben navigierbare Familien (auch noch leere), Mehrfachzuordnung, Suchbegriffe/Umlaute, Filter + Suche, vollständige Quellen und Vorteile/Nachteile, alternative Referenzen, Auflösung der Wissenswelt-/Bedürfnis-IDs; Invalid-Version/ID/Referenz wird abgewiesen.
- CI: testDebugUnitTest, assembleDebug, Signatur, Package-ID, SHA-256, APK-Artifact. Tatsächliche Testergebnisse vor Merge im PR prüfen.
- Geräteabnahme (ausstehend): Einstieg Wissenswelt → Strategien; fünf Karten durchlesen; Gerät zurück/detailliert navigieren; Quelle extern öffnen und ohne Browser testen; Dark Mode, 200-%-Schrift, TalkBack, kleiner Bildschirm, Querformat, Flugmodus.
- Redaktionelle Prüfung (ausstehend): Fachperson kontrolliert Neutralität, Evidenzgrenzen, Sicherheit, Vielfalt und Nicht-Diagnostik. UX-Prüfung: Versteht eine Testperson den Unterschied zwischen kurz hilfreicher Ablenkung und langfristigem Vermeiden sowie Akzeptanz und Resignation?
- **Nicht in diesem PR:** die restlichen 25 Strategiekarten, persönliche Strategieversuche, Strategie-Bookmark-/Lesestatus, automatische Bedürfnisse-Strategien-Zuordnung, M7-Implementierung, Merge.
