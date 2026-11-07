# InnerWave – Wortkarten entdecken: gemischter Entdeckungsmodus

## Produktentscheidung (07.11.2026)

**Nur Wortkarten entdecken** nutzt nun einen zufällig gemischten, wiederholungsfreien Kartenstapel. Der normale Gefühlsatlas bleibt für Suche, Alphabet, Vergleiche und gezielte Navigation bewusst alphabetisch sortiert.

- Bei Einstieg in den Kartenmodus wird jede verfügbare Katalog-ID einmal zufällig angeordnet.
- **Nächste Karte** führt zur nächsten Karte desselben Stapels; **Zurück** zeigt dieselben bereits besuchten Karten erneut.
- **Neu mischen** startet eine neue Reihenfolge ab Karte 1. Die gerade angezeigte Karte wird, wenn mehr als eine Karte verfügbar ist, nicht direkt wiederholt.
- Nach der letzten Karte heißt die Aktion **Neuer Durchgang** und erstellt wieder eine neue zufällige Reihenfolge ohne sofortige Wiederholung der zuletzt sichtbaren Karte.
- Gefühlsfamilien-Filter bleiben erhalten und mischen ausschließlich die passenden Karten neu. Jede Filteränderung setzt die Position und das Aufdecken zurück.
- Die Kartenreihenfolge, Familienfilter, aktuelle Position und Aufdeckzustand bleiben über **SaveableStateHolder** beim Besuch einer Detailansicht, nach einem kurzen App-Wechsel und bei einer vorübergehenden Entsperrung erhalten, solange Android die Aktivität bzw. ihren gespeicherten Zustand verfügbar hält.
- Gespeichert werden ausschließlich öffentliche Katalog-IDs und UI-Auswahlwerte. Keine privaten Texte, Antworten, Lernstatistiken, Streaks oder Netzwerkanfragen.

## Technische Umsetzung

- `core/model/WordCardDeck.kt`: reine, testbare Shuffle-Logik für deduplizierte Katalog-IDs. Keine Änderungen an `EmotionCatalog.search`.
- `MainActivity.kt`: `LearningCardsScreen` verwendet einen Saveable-Stapel, stabile Rückwärtsnavigation, gefilterte Auswahl und Schaltfläche **Neu mischen**; Katalogrevision/ungültig gewordene IDs führen zu einem frischen Stapel.
- `WordCardDeckTest.kt`: JVM-Tests für Vollständigkeit, Einmaligkeit, keine unmittelbare Wiederholung, Familienfilter, Ein-Karten-Grenzfall und unveränderte Eingaben.
- `WordCardShuffleEmulatorTest.kt`: drei echte UI-Tests bei 100 % und 200 % Schriftgröße für Nächste/Zurück/Neu mischen, Filter und State-Restoration.
- CI: Sollstand `61` UI-Tests je Schriftgröße (zusammen `122`) plus unverändert `3` EKB2- und `9` EKB3-Integrationstests.
- Preview: `versionCode=40`, `versionName=0.7.10-innerwave-wordcards-preview`.

## Abnahme

CI-Status und tatsächliche Download-Artefakte erst nach abgeschlossenem GitHub-Actions-Lauf als erfolgreich ausweisen. Manuelle Prüfung auf echtem Android-Gerät (TalkBack, enge Displays, App-Sperre) bleibt erforderlich.

**Draft-PR: kein Merge ohne ausdrückliche Nutzerfreigabe.**
