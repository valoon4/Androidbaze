# M2.2 – Emotionsatlas: 289 Begriffe, 41 Vergleiche, 14 Wege

Status: **redaktionelle Arbeitsfassung**, 29.10.2026.

## Ziel und Ergebnis

Emotionskompass soll Menschen einen vielfältigen, sorgfältig unterscheidbaren deutschen Wortschatz zugänglich machen. M2.2 erweitert M2.1 von 144 auf **289 Begriffe** in den acht bestehenden Familien. Es ist weder eine abschließende Liste aller Emotionen noch eine wissenschaftlich validierte Taxonomie. Neben klassischen Emotionen enthält der Atlas ausdrücklich markierte **Stimmungen** und **innere Zustände** – das hilft bei der begrifflichen Orientierung, ohne alles als Emotion auszugeben.

### Fachliche Inhalte

- 145 neue, individuell formulierte Begriffe; sämtliche 144 M2.1-Begriffe bleiben mit unveränderter ID erhalten.
- Jeder Eintrag hat eine deutsche Definition, zwei unterscheidbare Alltagssituationen, Synonyme, verknüpfte Begriffe und eine offene Reflexionsfrage.
- 20 zusätzliche, bewusst ausformulierte Vergleichspaare; insgesamt **41** mit Gemeinsamkeiten, unterschiedlichen Merkmalen und Nachspürfragen.
- Acht zusätzliche Lernpfade; insgesamt **14**.
- Viele Begriffe können mehreren thematischen Familien angehören, die primäre Familienzuordnung bleibt eindeutig.
- Beispiele neu: Kummer/Schwermut, Ekel/Abscheu, Bammel/Panikgefühl, Selbstmitgefühl/Selbstmitleid, Stolz/Leistungsstolz, Gelassenheit/Seelenruhe.

### Native App-Funktionen

- Bestehender Familien-, Typ- und A–Z-Filter und die Offline-Volltextsuche bleiben erhalten.
- **Wortkarten**: Nutzer sehen zunächst die Beschreibung und ein Beispiel, können den Begriff aufdecken und in die vollständige Erklärung wechseln. Familienfilter und Vor-/Zurück-Navigation. Keine Bewertung, kein Zwang und keine Speicherung des Lernfortschritts.
- **Vergleichsübersicht**: Alle 41 kuratierten Paare sind zentral aufrufbar.
- **Lernpfade**: 14 alltagsnahe Themenrouten über den gesamten Wortschatz, weiterhin ohne Leistungsdruck.
- Das Design D, deutsche Sprache, Dark Mode, das Onboarding und bestehende vier App-Bereiche bleiben erhalten.

### Architektur und Datenschutz

- Daten bleiben **lokal als JSON-Asset** (`assets/catalog/emotions.de.v2.json`) eingebaut; `schemaVersion=2`, `contentRevision=2026-10-m2.2`.
- Kein Netzwerkrecht, keine Benutzerkonten, keine Tagebucheinträge. Nur der Abschluss des Onboardings bleibt als Einstellung gespeichert.
- `EmotionCatalog` verlangt mindestens 250 Einträge, 35 Vergleichspaare, 12 Lernpfade, eindeutige IDs und gültige Relationen. Alt-IDs sind für spätere Journalverknüpfungen stabil.
- `versionCode=4`, `versionName=0.2.2-m2.2`, gleicher App-Paketname.
- Der GitHub-Actions-Workflow baut Debug-APK, führt JVM-Unit-Tests aus und prüft Paketkennung und Android-Debugsignatur.

### Grenzen und manuelle Abnahme

Die Formulierungen sind **keine klinisch validierten Erklärungen**. Einige Einträge beschreiben innere Zustände, soziale Erfahrungen oder Haltungen statt Emotionen im engeren Sinn. Sie sind als solche gekennzeichnet und bedürfen vor öffentlicher Veröffentlichung fachlicher und sprachlicher Prüfung.

Auf einem echten Android-Gerät prüfen:
1. Bestehende Installation mit v0.2.2 aktualisieren; Start und alle vier Tabs testen.
2. Atlas-Suche nach „Ärger“, „Aerger“, „Panikgefühl“, „Wissensdurst“, „Selbstmitgefühl“.
3. „Wortkarten“ öffnen, Begriff aufdecken, nächste Karte und Familienwechsel testen.
4. „Vergleiche“ öffnen und z. B. Ekel vs. Abscheu in beiden Richtungen lesen.
5. Lernpfad „Mit dir selbst umgehen“ öffnen und über Detailansicht zurückkehren.
6. A–Z, Dark Mode, große Schrift, TalkBack und kleinere Displays prüfen.
7. Im Flugmodus sicherstellen, dass Atlas, Lernpfade und Karten weiterhin funktionieren.
8. Sicherstellen, dass keine Einträge oder privaten Journaldaten gespeichert werden.

**Nächste Priorität:** M3 macht Gefühle persönlich erforschbar, mit optionalen Eingaben und geschützter, tatsächlich verantwortungsvoll umgesetzter Datenhaltung in M4–M5.
