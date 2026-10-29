# M3.2 – Vertiefte Selbsterkenntnis ohne Deutung von außen

**Version:** 0.3.2-m3.2 · **Android versionCode:** 7 · **Stand:** 29.10.2026

## Ziel

Der Gefühlsnavigator soll nicht nur helfen, mehrere Gefühle zu benennen, sondern auf Wunsch einen Zusammenhang zwischen Erleben, eigenen Gedanken, Bedürfnissen und einem möglichen nächsten Schritt erkennbar machen. **Keine Aussage der App behauptet, eine Emotion habe eine einzige Ursache.** Alles ist optional und bleibt unter der Deutungshoheit des Nutzers.

## Ablauf

**Kurzmodus (3 Schritte)** bleibt genau so leichtgewichtig wie in M3.1:
1. Gefühle benennen (oder ohne Auswahl weitergehen)
2. Intensität je Gefühl optional angeben (einschließlich 0 oder *keine Angabe*)
3. Neutrale Zusammenfassung

**Vertiefter Modus (5 Schritte)**:
1. Ankommen – optionale körperliche Eindrücke
2. Gefühle benennen – geführte Offline-Orientierung, alle 289 Begriffe erreichbar
3. Nachspüren – optionale Intensitäten, Lebensbereich und vorhandener Freitext
4. **Mich besser verstehen** – fünf freiwillige Abschnitte:
   - „Was ist geschehen?“: möglichst beobachtbare Situation in eigenen Worten
   - „Wie habe ich es erlebt?“: persönliche Interpretation, klar vom Ereignis getrennt
   - „Was könnte mir wichtig sein?“: frei wählbare Bedürfnisse, auch mehrere oder *Ich weiß es noch nicht*
   - „Was ist mir klarer geworden?“: eigene Erkenntnis in Freitext, auch leer zulässig
   - „Was würde mir jetzt guttun?“: selbstgewählte Möglichkeit wie Pause, Gespräch, Unterstützung, Grenze oder ausdrücklich *Gerade nichts tun*, plus eigener Text
5. **Ausklang** – nur tatsächlich ausgewählte oder selbst geschriebene Inhalte; keine computergenerierte psychologische Erklärung oder Handlungsempfehlung

**Umschalten:** Von vertieft nach kurz wird die Reflexion übersprungen; bereits getätigte Eingaben bleiben nur für den aktiven Check-in erhalten. Zurückgehen bleibt möglich, kein Schritt ist verpflichtend.

## Interaktions- und Datenschutzentscheidungen

- Kein Bedürfnis wird automatisch aufgrund eines Gefühls vorgeschlagen oder zugeordnet. Die Chips sind allgemeine, frei auswählbare Reflexionshilfen.
- *Ich weiß es noch nicht* ist bei Bedürfnissen als einzelne exklusive Auswahl implementiert, wird beim Wählen eines konkreten Bedürfnisses entfernt.
- Ausgewählte Handlungsideen sind eigene Absichten des Nutzers; die App bewertet sie nicht und setzt nichts automatisch um.
- Getrennte Felder für beobachtete Situation und eigene Interpretation unterstützen differenziertes Nachdenken ohne scheinbare Tatsachenbehauptungen.
- Längenbegrenzungen: Situation/Interpretation/Handlungsnotiz jeweils 280 Zeichen, Erkenntnis 400 Zeichen, bisheriger allgemeiner Freitext 500 Zeichen.
- Alle Check-in-Daten bleiben in einer **ephemeren Kotlin-StateFlow-Datenklasse im Arbeitsspeicher**, werden weder persistiert noch übertragen. Bei Verwerfen, Neustart oder Schließen wird der gesamte Entwurf zurückgesetzt. Kein Journal und kein Export.
- Vor Verwerfen **ausgefüllter** Check-ins zeigt die Oberfläche einen Bestätigungsdialog; leere Check-ins lassen sich direkt beenden.
- Während des aktiven Check-ins setzt die Activity `FLAG_SECURE`, sodass Android-Screenshots und die Vorschau der zuletzt genutzten Apps geschützt werden. Das Flag wird beim Verlassen wieder entfernt.
- Keine INTERNET-Berechtigung; keine KI, Diagnostik, automatisierten Ratschläge oder psychologische Wirkungsaussagen.

## Automatisierte Akzeptanztests

- Fünf Tiefenstufen einschließlich freiwilliger leerer Reflexion.
- Der 3-Schritt-Kurzmodus überspringt die neue Tiefenstufe auch beim Zurückgehen.
- Moduswechsel aus Reflexion nach Kurz endet direkt bei der Übersicht.
- Mehrere Bedürfnisse, optionales *Unklar*, Abwahl, persönliche Texte und Längenlimits.
- Situation und Interpretation bleiben getrennte Dateneingaben.
- Freie oder explizit **keine** Handlung ist erlaubt.
- Gefühle, auch mit Intensität 0, bleiben beim Navigieren erhalten.
- ViewModel verwirft **alle** zusätzlichen persönlichen Angaben beim Schließen.
- Bestehende Katalog-, M3- und M3.1-Tests bleiben erhalten.

## Manuelle Android-Abnahme

1. Kurzen Check-in ohne Gefühle oder Eingaben zu Ende führen; er zeigt genau drei Schritte.
2. Vertiefter Check-in: mehrere Gefühle, teils mit Intensität 0, teils ohne Angabe.
3. Aus dem Nachspüren in „Mich besser verstehen“ wechseln; alle Felder leer lassen können.
4. Eine Situation und eine abweichende persönliche Interpretation eingeben; in der Übersicht prüfen.
5. Zwei Bedürfnisse auswählen, anschließend „Ich weiß es noch nicht“ – die konkreten Werte müssen entfallen.
6. Eine selbst formulierte Erkenntnis und eine Idee hinzufügen; in der Zusammenfassung nur eigene Inhalte sehen.
7. Im Check-in zurückgehen, in Kurzmodus wechseln und alle vorherigen Gefühlsauswahlen kontrollieren.
8. Mit eingegebenen privaten Texten „Verwerfen“ wählen: Bestätigungsdialog muss erscheinen. „Weiter reflektieren“ erhält den Entwurf, „Angaben verwerfen“ löscht ihn.
9. Während eines Check-ins Screenshot und Android-App-Wechsler testen; außerhalb sollen normale Screenshots wieder möglich sein.
10. App schließen und neu öffnen: keine privaten Check-in-Texte sind wiederherstellbar.
11. Großschrift, TalkBack, Dark Mode und Flugmodus durchgehen.

## M4 bleibt separat

Eine dauerhafte lokale Speicherung mit Verschlüsselung, Löschung, Datenschutzprüfung und Export wird nicht durch dieses Feature vorweggenommen. Die Check-in-Reflexion ist in M3.2 bewusst **temporär** und keine ärztliche oder psychotherapeutische Unterstützung.
