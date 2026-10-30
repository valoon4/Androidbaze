# M6.3.2 – Vertiefende Strategiewelt: Artikel, Reihen und Praxisfälle

**Stand:** 30.10.2026 · **Status:** Implementierung im Draft-PR, unabhängiger redaktionell-fachlicher Review und Geräte-/TalkBack-Abnahme ausstehend.
**Voraussetzung:** M6.3.1 mit 30 Offline-Strategiekarten ist in main.

## Nutzerwert und bewusster Umfang

M6.3.2 ergänzt die bisherige Wissenswelt von **20 auf 32 frei lesbare Artikel**. Die bestehenden 20 Artikel und vier Lernreihen bleiben in IDs und Inhalten erhalten. Zusätzlich:
- **12 vertiefende, selbstständig lesbare Artikel** in drei versionierten offline Assets.
- **3 freiwillige, vollständig offene Lernreihen** mit jeweils vier Kapiteln: Grundlagen verstehen, Mit Belastung umgehen, Flexibel handeln.
- **15 erfundene Alltagsszenarien** in drei versionierten Offline-Dateien mit zwei plausiblen Vergleichsoptionen, explizitem „Ich bin unsicher“-Weg und erläuterndem Feedback **erst nach der Wahl**.
- Zugang zu „Praxisfälle“ direkt aus dem Wissenswelt-Menü und dem Wissenswelt-Start. Kein neuer Bottom-Navigation-Punkt. Die vorhandenen drei Atlanten bleiben gleichrangig.
- Kein verpflichtender Kurs, keine Streaks, kein Punkte-/Leistungswert. Antworten auf Praxisfälle werden weder gespeichert noch an das Journal übergeben; die Wissenswelt übernimmt die bereits bestehende freiwillige Gelesen-/Merkliste.

## Zwölf Titel (neue, stabil referenzierbare IDs)

| Reihe | Artikel |
|---|---|
| Grundlagen | Strategie, Coping und Emotionsregulation unterscheiden |
| Grundlagen | Warum Belastungen unterschiedliche Antworten brauchen |
| Grundlagen | Coping-Modelle verständlich einordnen |
| Grundlagen | Bewältigungsflexibilität: prüfen statt festlegen |
| Belastung | Was kann ich beeinflussen – und was nicht? |
| Belastung | Gedanken prüfen, ohne Erfahrungen kleinzureden |
| Belastung | Gefühle begleiten: Körper, Wahrnehmung und Pausen |
| Belastung | Unterstützung suchen: hilfreich und sicher |
| Flexibel | Ablenkung, Rückzug und Vermeidung unterscheiden |
| Flexibel | Akzeptanz, Resignation und Grenzen |
| Flexibel | Strategien kombinieren, ohne sich zu überfordern |
| Flexibel | Wenn eine Strategie nicht hilft: neu orientieren |

Die neuen Artikel ergänzen bewusst vorhandene Grundlagentexte (z. B. „Bedürfnis und Strategie unterscheiden“, „Mehr als einen Weg ausprobieren“ und „Was tun, wenn eine Strategie nicht hilft?“): mehr Kontext, Grenzen, wissenschaftliche Begriffe und situative Beispiele, keine wortgleichen Duplikate.

## Didaktik und Sicherheit

- Behandelt die Funktionen allgemeiner Strategien, Coping und Emotionsregulation als **überlappende Konzepte**, nicht als trennscharfe Diagnosen.
- Vermeidet moralische Pauschalurteile über Akzeptanz, Rückzug, Ablenkung, Unterdrückung oder Neubewertung.
- Unterscheidet freiwillige kurze Pause von langfristig einschränkendem Vermeiden; **keine ungeschützte Konfrontation/Exposition**.
- Betont bei Gewalt oder unsicheren Beziehungen Schutz vor Offenheit, Grenzsetzung durch Konfrontation und inneren Entspannungsübungen.
- Bei Atem/Körperwahrnehmung freiwilliger Abbruch bei Unwohlsein, keine medizinische Beratung.
- Jede Praxisfrage hat zwei nachvollziehbare Optionen und erklärt, welche Aspekte **in dieser erfundenen Lage** stärker wiegen. Die Unsicher-Option ist kein Fehler und eröffnet ebenfalls das Feedback.
- Quellen werden freiwillig im externen Browser geöffnet. Die App hat keine INTERNET-Berechtigung. Wissenschaftliche Modelle sind Konzeptgrundlagen; sie belegen **nicht** jede einzelne Alltagsempfehlung oder die Wirksamkeit einer Methode für jeden Menschen.

## Architektur / Offline-Datenintegrität

- `LessonCatalog.fromJsonParts(original, supplements)` führt neue Kapitel und Reihen zusammen, wendet anschließend die vorhandene strikte Gesamtprüfung an: IDs, Quellen, Inhaltsumfang und Referenzen.
- Bei Fehlern in einer Ergänzungsdatei bleibt die **ursprüngliche Wissenswelt** mit 20 Artikeln und vier Reihen lesbar; Check-in und verschlüsseltes Tagebuch sind unabhängig.
- `StrategyCaseCatalog.fromJsonParts(parts, strategyIds)` prüft Schema, eindeutige Fall-IDs, Pflichtfelder, Antwortpositionen und referenzielle Verknüpfung mit allen 30 Strategie-IDs.
- Beschädigte Fallinhalte blenden nur die Fallübungen aus. Es gibt keine neue private Persistenz, keine Netzwerkschnittstelle, keine Änderungen an EKJ1/EKL1 und keine neue Berechtigung.

## Qualitätssicherung

Automatisch:
- 20 vorhandene Artikel/4 Reihen unverändert, neue Gesamtgröße exakt 32/7 und 12 neue IDs genau einmal zugeordnet.
- Bereits gespeicherte Artikel-Lesemarken und Favoriten bleiben gültig.
- Neue Quellen, Lernziele, Alltagstexte, Erklärungssätze und gemischte korrekte Antwortpositionen vorhanden.
- 15 Fall-IDs eindeutig, alle referenzierten Strategie-IDs existieren, Such-/Filterfunktionen.
- Versionen, doppelte IDs und defekte Verweise werden abgewiesen.
- Gradle testDebugUnitTest, assembleDebug, Package-ID/Signatur/SHA-256 aus GitHub Actions kontrollieren.

Noch manuell/fachlich zu prüfen (**kein behauptetes Abschlussgate**):
- 200-%-Schrift, TalkBack, kleiner Bildschirm, Dark Mode, Querformat, Rotation, Offline-Modus und System-Zurück.
- Verständlichkeit der 12 Artikel, Plausibilität der 15 Optionspaare, neutrale Unsicher-Option, Feedback nach statt vor der Antwort.
- Quellen-Claim-Mapping durch qualifizierte Fachredaktion; keine pauschalen Wirksamkeitsversprechen.
- Abgleich mit M6.3.3-UX-/Sicherheitsrunde und der M7-Planung.

**Nicht enthalten:** persönliche Strategie-Versuchsprotokolle, automatische Empfehlungen oder Analysen, weitere Bottom-Tabs, Merge ohne ausdrückliche Freigabe.
