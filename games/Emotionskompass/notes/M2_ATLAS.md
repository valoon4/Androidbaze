# M2 – Emotionsatlas v2: Content- und Funktionsabnahme

Datum: 29.10.2026 · Branch: feature/emotionskompass-m2-atlas

## Umsetzung

- Katalog-Schema Version **2**, Dateipfad `src/app/src/main/assets/catalog/emotions.de.v2.json`.
- Datenfelder pro Eintrag: ID, deutscher Name, Hauptfamilie, 1–n Familien, Typ (EMOTION/MOOD/STATE), Definition, zwei Alltagsszenarien, Synonyme, verwandte IDs und eine offene Reflexionsfrage.
- 48 Einträge, verteilt auf acht Familien; 9 ausformulierte Paarvergleiche mit Gemeinsamkeit, zwei Unterscheidungen und einer Frage.
- Ein Eintrag kann mehreren Familien angehören (z. B. Enttäuschung sowohl Traurigkeit als auch Ärger). Der Typ unterscheidet Emotionen von Stimmungen/Zuständen.
- Der Katalog ist **unveränderlich und lokal**, bei App-Start aus Assets geladen. **Noch keine Nutzer-DB / Room**, da keine Journalfunktion.
- Kotlin `EmotionCatalog.fromJson` lehnt falsche Schema-Versionen, ungültige Referenzen, doppelte IDs und leere fachlich nötige Angaben ab.
- Suche ist offline und findet deutsche Begriffe auch über Synonyme, Umlaut-Varianten, Groß-/Kleinschreibung sowie erklärende Texte; Namens- und Präfix-Treffer werden vor bloßen Erklärungstreffern eingeordnet.
- Vergleich funktioniert unabhängig von der Paar-Reihenfolge; bei nicht hinterlegten Paaren wird ausdrücklich *keine* erfundene fachliche Abgrenzung erzeugt.
- Native UI zeigt zwei Beispiele je Begriff, Synonyme, offene Frage, thematische Überlappung, verwandte Begriffe und gezielte Vergleichsmöglichkeiten.
- Bereitstellung als isoliertes Android-Unterprojekt; keine Eingriffe in andere Apps / Root-App.

## Qualitäts- und Inhaltsgrenzen

**Redaktioneller Status:** Arbeitsfassung mit alltagsnah formulierten Beispielen – **keine psychologische oder klinische Expertenvalidierung**. Inhalte sind keine medizinischen Ratschläge und keine Diagnose. M5 verlangt eine weitergehende Fachprüfung und mindestens 80 hochwertige Begriffseinträge.

Die Familien dienen der Navigation, sind kein universelles psychologisches Klassifikationssystem; Begriffe können mehreren Familien zugeordnet sein. Suche filtert nicht aufgrund vermeintlicher Stimmungstypen Begriffe weg. Selbstreflexion wird nicht automatisch ausgewertet.

## Tests und Akzeptanz

Automatisierte JVM-Tests umfassen:
- JSON-Datei ist lesbar und Schema-Version korrekt;
- 48 stabile, eindeutige IDs und acht Kategorien;
- mindestens zwei Beispiele, Alias und Reflexionsfrage je Begriff;
- alle Querverweise und Vergleichspaare verweisen auf vorhandene IDs;
- Suche nach Name, Synonym, Groß-/Kleinschreibung, Umlauten und Präfix;
- Mehrfachfamilien;
- Umkehrung eines Vergleichspaares erhält korrekte Zuordnung;
- ungültiges Schema/fehlende Referenzen werden nicht akzeptiert;
- unbekannte Vergleiche werden nicht erfunden.

**Build-Gate:** `testDebugUnitTest`, `assembleDebug`, Signatur- und Paketnamenprüfung im GitHub-Actions-Workflow. Ein erfolgreicher CI-Build belegt noch keinen auf echten Geräten getesteten Barrierefreiheitsstatus.

## Manuelle Prüffälle nach der APK

1. Atlas öffnen und „Frust“, „Ärger“, „Aerger“, „aufgeregt“ und „Enttäuschung“ suchen.
2. Familie „Ärger“ öffnen: Enttäuschung muss auffindbar sein, ebenso in „Traurigkeit“.
3. Frustration öffnen, beide Beispiele und die Nachspürfrage lesen, dann „Frustration und Enttäuschung“ vergleichen.
4. Vergleich vom anderen Ausgangsbegriff öffnen: die Erklärungen müssen zum richtigen Begriff passen.
5. Verwandten Begriff öffnen und Android-Zurück benutzen; kein unerwarteter Neustart, keine Datenverluste.
6. System-Dark-Mode, 200 % Schriftgröße, TalkBack und kleine Displaybreite manuell durchgehen.

## Nicht Teil von M2

Kein neues Tagebuch, keine persönliche Speicherung, keine Verschlüsselungsvortäuschung, keine Erkenntnis-/Stimmungsbewertung, keine Cloud und keine KI. M3/M4/M5 bleiben eigenständige Schritte.

**Folgende technische Entscheidung:** Die vom Assets-File geladene `EmotionCatalog`-Instanz wird dem `AppViewModel` als Abhängigkeit bereitgestellt. Bei späterer Einführung des Room-Katalogs kann das über ein Repository-Interface ersetzt werden, ohne die fachlichen Einträge umzubenennen.
