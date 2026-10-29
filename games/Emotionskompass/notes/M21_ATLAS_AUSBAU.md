# M2.1 – Ausbau des Emotionsatlas

Status: Redaktionelle Arbeitsfassung · 29. Oktober 2026

## Produktziel

Der Wortschatz ist ein gleichberechtigter Pfeiler neben der Selbsterforschung. In M2.1 wird er von 48 auf **144 unterschiedliche, deutschsprachige Begriffe** erweitert. Die abschließende psychologische Fachprüfung steht aus: keine kanonische, wissenschaftlich endgültige Taxonomie von „allen Emotionen“ wird behauptet.

## Ergebnis

- **144** Einträge in acht flexiblen Familien, davon mehr als 20 mit Mehrfachzuordnung.
- Je Begriff: Definition, zwei alltagsnahe Beispiele, mehrere Suchsynonyme, verwandte Begriffe und eine nicht-diagnostische Reflexionsfrage.
- Typkennzeichnung: **Gefühl**, **Stimmung** oder **Zustand**. Dadurch lassen sich verwandte Phänomene erklären, ohne sie als dasselbe auszugeben.
- **21** redaktionell geschriebene Paarvergleiche, u. a. Neid/Eifersucht, Resignation/Akzeptanz, Scham/Schuldgefühl, Stolz/Überlegenheit und Groll/Verbitterung. Für nicht kuratierte Paare gibt es bewusst keine erfundene Deutung.
- **Sechs** thematische Entdeckungswege: Töne der Freude, Neid/Eifersucht, Rückschläge, Nähe, Ärger, innere Unruhe. Der Lernpfad ist frei navigierbar und bewertet niemanden.
- **A–Z**, Familienfilter, Typfilter, Synonymsuche und „Überrasch mich“; Filter bleiben beim Öffnen und Zurückkehren zu einem Begriff in der laufenden App erhalten.
- Unveränderte Datenschutzzusage: Atlas lokal im APK-Asset, keine Internetberechtigung, keine Tagebucheinträge, keine Auswertung.
- Unveränderte technische Grundstruktur: `games/Emotionskompass/src` vollständig eigenständig.

## Versionierung und Technik

APK: `0.2.1-m2.1`, Android `versionCode 3`, gleiche Application-ID `com.valoon4.emotionskompass`. Fachdatendatei: `app/src/main/assets/catalog/emotions.de.v2.json`, `schemaVersion=2`, `contentRevision=2026-10-m2.1`. Schema 2 bleibt erhalten, weil die bestehenden Felder kompatibel sind; das optionale erweiterte Feld `learningPaths` wird ab M2.1 im Lader verpflichtend geprüft.

Kotlin `EmotionCatalog` validiert Referenzen, eindeutige IDs, Familien, Paare und Lernpfade. Test-Gates: mindestens 120 Wörter, durchgängige Beziehungen, zwei Beispiele, Filterkombinationen, Lernpfade, Stimmungs-/Zustandstypen, Umlaute, Umkehrung von Paarvergleichen und Ablehnung falscher oder defekter Daten.

## Fachliche Redaktion und Grenzen

Die Einordnung ist **lerndidaktisch**, keine Diagnose. Begriffe überlappen sich und variieren kulturell oder individuell. Mehrfachfamilien sind erlaubt, nicht alle Kategorien sind ein wissenschaftliches Klassifikationssystem. Für einen öffentlichen Release werden insbesondere die Unterscheidungen zwischen Emotionen, Stimmungen, Körperzuständen, Bedürfnissen und sozialen Bewertungen sowie sensible Formulierungen sorgfältig extern geprüft.

Nutzer können den Atlas erkunden, ohne ein Gefühl auswählen oder persönliche Informationen angeben zu müssen. Kein Zwang zur Reflexion oder Selbstoffenbarung.

## Abnahme auf Android-Handy

1. Update der M2-Installation auf v0.2.1; die Versionskennung steigt von 2 auf 3.
2. Suche „Neid“, „Eifersucht“, „Aerger“, „Verblüffung“ und „Mutlosigkeit“.
3. Wähle unter „Entdecken“ den Lernpfad „Neid, Eifersucht & Vergleich“ und öffne die entsprechenden Seiten.
4. Öffne einen Begriff und gehe zurück: die gewählten Familien-, Typ- und Buchstabenfilter sollen erhalten bleiben.
5. Filtere nach „Stimmungen“ und anschließend nach „Gefühle“, teste die A–Z-Chips.
6. Stelle die Systemschrift groß und aktiviere Dark Mode; prüfe lange Texte und Navigation.
7. Prüfe, dass Check-in und Tagebuch weiterhin nur als Vorschau angeboten werden.

**Nach M2.1:** M2.2 erweitert auf ca. 250–350 Begriffe und verbessert differenzierte Lernbeziehungen und Navigationshilfen. Danach können M3–M5 die gleichgewichtige persönliche Reflexion inklusive Datenschutz-Release-Gates umsetzen.

## Pull-Request-Reihenfolge

Dieses M2.1-Branch baut auf dem offenen **PR #4 (M2)** auf. PR #4 sollte zuerst nach `main` übernommen werden; anschließend PR #5 auf `main` umbauen und separat mergen. Keine fremden Android-Projekte wurden verändert.
