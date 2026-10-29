# M3.3 – Bedürfnisatlas und Bedürfniskompass

**Android-Version:** 0.3.3-m3.3 (`versionCode 8`) · **Stand:** 29.10.2026 · **Build:** Debug-APK via GitHub Actions.

## Warum vor M4?

M3.2 ermöglicht freiwillige Bedürfnis-Reflexion, verwendete dafür aber 14 fest eingebaute Optionen ohne Erklärung. M3.3 schafft eine gemeinsame, offline verfügbare Inhaltsbasis, die sowohl selbstständig erkundet als auch im bestehenden Check-in verwendet wird. M4 kann darauf aufbauend später stabile IDs speichern, statt unsichere Bildschirmtexte zu referenzieren.

## Umfang und Wissensvermittlung

- **82 sorgfältig getrennte deutschsprachige Bedürfnisbegriffe** in acht Familien: Sicherheit & Stabilität, Verbundenheit & Nähe, Autonomie & Grenzen, Wertschätzung & Fairness, Ruhe & Wohlbefinden, Entwicklung & Kompetenz, Sinn & Orientierung, Lebendigkeit & Freude.
- Je Begriff: verständliche Erklärung, je ein Beispiel für mögliches Erfülltsein/Nichterfülltsein, Synonyme, verwandte Bedürfnisse, offene Reflexionsfrage und eine konkrete **Beispielstrategie**. Eine Strategie ist nicht identisch mit dem Bedürfnis.
- **20** redaktionell formulierte Bedürfnisvergleiche, **acht** Lernpfade, A–Z-Filter, Familienfilter, Suche und Detailseiten.
- Der Atlas ist **keine abgeschlossene oder klinisch validierte Taxonomie**. Verschiedene psychologische Modelle unterscheiden beispielsweise Grundbedürfnisse, Motivationen, soziale Werte und körperliche Bedürfnisse. Darum sind die Begriffe hier als lernorientiertes Vokabular klassifiziert, nicht als universell gültige wissenschaftliche Liste.
- Mehrfachfamilien erlaubt, IDs stabil und Inhaltsrevision festgelegt: `assets/catalog/needs.de.v1.json`, `schemaVersion=1`.

## Neuer Hauptreiter

Die bisherige Bottom Navigation **Start – Entdecken – Tagebuch – Rückblick** wird zu **Start – Gefühle – Bedürfnisse – Tagebuch – Rückblick**.

### Bedürfnisatlas
- Benutzer kann alle 82 Begriffe suchen, mit A–Z oder über Familien filtern, Definitionen und Beispiele lesen, verwandten Begriffen folgen.
- Lernpfade vertiefen einzelne Themen; zentrale Vergleiche unterscheiden semantisch ähnliche Bedürfnisse.
- In jeder Detailseite wird eine mögliche Strategie klar als Beispiel gekennzeichnet, nicht als einzig richtiger Weg.

### Bedürfniskompass
- Vier **selbst gewählte** Blickwinkel: „Gerade wichtig“, „Gut erfüllt“, „Kommt zu kurz“, „Noch unklar“.
- Erst acht zugängliche Startbegriffe, über Suche/Filter vollständiger Atlas zugänglich.
- Mehrere Bedürfnisse auswählbar, pro Begriff ein anderer Blickwinkel. Die entstehende Momentaufnahme zeigt die **eigene Auswahl** und eine passende Reflexionsfrage, keine automatischen Rückschlüsse.
- Der Kompass ist **nicht gespeichert**, kein Test, keine Punkte und keine Bewertung. Die Auswahl ist im flüchtigen Compose-State und wird beim Verlassen verworfen.
- Während der Kompass geöffnet ist, schützt `FLAG_SECURE` die persönliche Auswahl vor Android-Screenshots und App-Wechsler-Vorschauen.

## Verknüpfung mit dem M3.2-Check-in

- Die vorherigen festen `PersonalNeed`-Einträge werden durch **stabile String-IDs aus dem NeedCatalog** ersetzt.
- Die häufigsten Bedürfnisse erscheinen zunächst als acht Einsteiger-Chips; Volltextsuche, Familien und alle 82 Begriffe sind verfügbar.
- Nach Auswahl lassen sich Definition, Beispiele und Reflexionsfrage innerhalb des Check-ins lesen – ohne ihn verlassen zu müssen.
- Das Nachspüren bleibt **freiwillig**. Es gibt **keine automatische Verbindung** von Emotion auf Bedürfnis; „Ich weiß es noch nicht“ ist weiterhin eine gültige, abwählbare Auswahl.
- Die Zusammenfassung verwendet lesbare Namen aus dem gemeinsamen Atlas, der interne Entwurf speichert nur IDs. Der Check-in wird weiterhin **nicht dauerhaft gespeichert**, der Kurzmodus bleibt erhalten.

## Qualität / Datenschutzzusagen

- Keine neue Internetberechtigung, keine Nutzerkonten, keine Cloud, keine persönliche Langzeitaufzeichnung.
- Aus dem gesamten Check-in werden weiterhin beim Schließen sämtliche privaten Eingaben verworfen. Nur das Onboarding-Abschlussflag bleibt wie bisher gespeichert.
- Fachliche Grenzen der Inhalte transparent beschrieben; keine automatische Diagnose oder Empfehlung und keine festen Ursache-Gefühl-Bedürfnis-Ketten.
- Der Kotlin-Parser prüft referenzielle Integrität, ID-Eindeutigkeit, acht Familien, mindestens 75 Begriffe, 16 Vergleiche, sechs Lernpfade, erforderliche Beispiele und verschiedene Familienzuordnungen.
- Zusätzliche JVM-Tests für 82 Einträge, Sortierung, Synonyme, Vergleiche, Lernpfade, Check-in-Integration und Löschung.
- APK: identische Package-ID `com.valoon4.emotionskompass`, minSdk 26, `versionCode 8`. Bestehende M3.2-Installation kann bei gleicher Debug-Signatur aktualisiert werden.

## Manuelle Smartphone-Abnahme

1. **Fünf Hauptreiter** auf einem kleinen Android-Smartphone mit normaler sowie großer Schrift testen; „Bedürfnisse“ muss lesbar sein.
2. Suchbegriffe „Geborgenheit“, „Naehe“, „Autonomie“, „Ruhe“ und „Verstandenwerden“ ausprobieren.
3. Gefühlsatlas mit 289 Wörtern unverändert; Bedürfnisatlas mit 82 Wörtern separat.
4. Lernpfad öffnen, zu Bedürfnisdetail und zurück gehen; Vergleich „Sicherheit / Geborgenheit“ durchlesen.
5. Bedürfniskompass: zwei Bedürfnisse mit unterschiedlichen Blickwinkeln auswählen, anschließend eines abwählen.
6. Kompass verlassen und erneut betreten: keine persönliche Auswahl darf zurückbleiben.
7. Vertieften Check-in starten: Bedürfnis aus Familie oder Suche wählen, Details lesen, später in der Zusammenfassung den korrekten Namen sehen.
8. Check-in abbrechen: die Bestätigung und vollständiges Verwerfen privater Eingaben müssen weiterhin funktionieren.
9. Kurzmodus weiterhin nur drei Schritte. Großschrift, Dark Mode, TalkBack und Flugmodus prüfen.
10. Während der persönlichen Kompass-Auswahl Android-Screenshot und App-Wechsler testen.

**Nicht enthalten:** Verschlüsseltes Tagebuch (M4), Datenexport, Statistiken und automatisch erstellte psychologische Interpretationen.
