# M6.3.1a – Navigation und Startseite: UX-Qualitätsrunde in PR #18

Stand: 30.10.2026 · Änderung: Innerhalb von PR #18 · Status: implementiert; reale Geräte- und Accessibility-Abnahme offen.

## Produktentscheidung

Feste Navigation mit genau fünf Hauptzielen: Start – Entdecken – Wissen – Tagebuch – Rückblick. Der Entdecken-Bereich bietet drei gleichrangige Atlanten: Gefühle – Bedürfnisse – Strategien. Keine horizontal scrollbare untere Hauptnavigation. Unter Wissen bleiben frei lesbare Wissensartikel, Lernreihen, Merkliste und zusätzlich der Einstieg zu Übungen/Wortkarten erreichbar.

Begründung: Sieben Bottom-Nav-Punkte wären auf kleinen Displays nicht gleichzeitig sichtbar, besonders bei großer Schrift. Eine scrollende Hauptnavigation versteckt Ziele. Der gemeinsame Atlasbereich erhält fachliche Gleichwertigkeit, ohne die Hauptnavigation zu überfrachten. Der M7-Rückblick bleibt vorerst ein Platzhalter.

## Umsetzung

- Startseite: prominenter freiwilliger Check-in und drei gleichrangige Atlas-Kacheln mit direkten Deep-Links zu ihren Entdecken-Tabs; zusätzliche direkte Wissenswelt-Karte. Redundante „Lernen & Entdecken“-Kachel entfällt; Übungen bleiben über Wissen erreichbar.
- Atlas-Tabs: standardmäßig nebeneinander; bei stark vergrößerter Schrift oder sehr schmalem Bildschirm vollständig sichtbar untereinander statt versteckt.
- Material3 NavigationBar auf Smartphones; NavigationRail bei mindestens 600 dp Breite und mindestens 480 dp Höhe. Bei kleinen Landscape-Höhen bleibt die untere Navigation.
- Strategie → Wissensartikel → „Zur Strategie“ bzw. System-Zurück führt wieder zum ursprünglichen Strategiedetail.
- Auswahl, Suche, Filter und mögliche Scrollposition über Haupt- und Atlaswechsel durch saveable State und SaveableStateHolder erhalten. Die Gefühlsfilter bleiben im AppViewModel, die Bedürfnis-Untertabs behalten ihren Suchzustand. Persönliche Bedürfnis-Kompass-Angaben werden nicht neu persistent gespeichert.
- System-Zurück schließt zuerst Strategie- oder Bedürfnisdetail, statt die App zu verlassen.
- Fehler im Strategie-/Wissenskatalog dürfen andere Atlanten oder Tagebuch nicht blockieren.
- Keine Änderungen an Journal, EKJ1/EKL1-Verschlüsselung, Backup und App-Berechtigungen.

## Abnahmematrix

| Szenario | Erwartetes Verhalten |
|---|---|
| Start Standard-Schrift | Check-in plus drei gleichrangige Atlas-Kacheln |
| Start → Strategien | Entdecken mit aktivem Strategie-Tab, ohne Wissenswelt-Umweg |
| Untere Hauptnavigation | Genau Start, Entdecken, Wissen, Tagebuch, Rückblick |
| Entdecken | Gefühle, Bedürfnisse und Strategien sichtbar und frei wählbar |
| Wissen | Artikel, Reihen, Merkliste sowie Übungen zugänglich |
| Strategie → Artikel → zurück | Ursprüngliche Strategieauswahl und Detail wieder sichtbar |
| Strategien: Suche/Filter/Scroll, dann Wissen, zurück | Suche/Filter/Position erhalten |
| Bedürfnisse: Suche/Filter, dann Lernpfade, zurück | Suche/Filter erhalten |
| Ein Atlasdetail + System-Zurück | Rückkehr zur jeweiligen Übersicht |
| 200-%-Schrift/kleines Gerät | Drei Atlas-Ziele nicht versteckt und bedienbar |
| Tablet breit/hoch | Seitliche NavigationRail |
| Querformat mit geringer Höhe | Keine überlaufende Rail |
| TalkBack | Klare Bezeichnungen für alle Hauptziele und Atlas-Tabs |
| Flugmodus und vorhandenes Tagebuch | Atlas-/Wissensinhalte funktionieren offline, Journal unberührt |

## Quality Gates

- JVM-Vertragstests für fünf Hauptziele und drei Atlas-Tabs; bestehende Regressionen für Katalog und Verschlüsselung.
- GitHub Actions: testDebugUnitTest, assembleDebug, Paketkennung, Debug-Signatur und SHA-256 müssen erfolgreich sein.
- Geräteabnahme ausstehend: echte Installation, 200-%-Schrift, TalkBack, kleiner Bildschirm, Querformat, Rotation, Prozessneustart, Such-/Scrollzustand, Artikel↔Strategie-Links und App-Sperre.
- Kein Merge ohne ausdrückliche Freigabe. Nicht enthalten: weitere 25 Karten, M7-Funktionen, unabhängiger fachredaktioneller Review.
