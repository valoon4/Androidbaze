# M7.3.3 – Geführte, freiwillige Selbstreflexion

**Status:** Separater Draft-PR #30, nicht gemergt.

## Nutzerziel

Die App hilft Menschen, eigene Beobachtungen bewusst einzuordnen, ohne Interpretationen vorzuschreiben. Im Bereich Rückblick / Meine eigenen Erkenntnisse stehen drei freiwillige, redaktionell formulierte Reflexionsfragen bereit. Bei M7.2-Musterbeobachtungen gibt es außerdem eine ausdrücklich anklickbare Einladung zur Reflexion.

- Fragen zur freien Reflexion: Wahrnehmen, was wichtig war, und einen eigenen Gedanken mitnehmen.
- Fragen aus einer Beobachtung: An konkrete Situationen erinnern, Unterschiede wahrnehmen, eigene Sicht formulieren.
- Jede Frage darf leer bleiben. Antworten bleiben beim Zurückgehen bearbeitbar; ein Abbruch verwirft den flüchtigen Text.
- Nach dem letzten Schritt entsteht ausschließlich aus selbst geschriebenen Antworten ein Entwurf. Die Frageformulierungen und beobachteten Begriffe werden nicht in die Erkenntnis kopiert. Erst ein zweites ausdrückliches Speichern im bestehenden Editor legt eine Erkenntnis an.
- Kein Automatismus für Belegverknüpfungen, keine Diagnose, kein Score, kein Streak, keine psychologische Deutung oder Handlungsempfehlung.
- Sämtliche redaktionelle Inhalte funktionieren offline; keine zusätzliche App-Berechtigung.

## Datenschutz

Private Antworten, Frageindex und Musteretikett verbleiben ausschließlich im aktuellen, nicht gespeicherten Compose-Arbeitsspeicher. Nichts davon wird in Android-Bundles, Logs, unverschlüsselte Einstellungen oder Analytics übernommen. Der Dialog hält maximal drei Antworten mit je 550 Zeichen. Bei einem Lade- oder Sperrfehler darf kein gespeicherter Erkenntnistext ersatzweise sichtbar werden.

## Manuelle Abnahme

1. Mit leerem Tagebuch die freiwillige Reflexion öffnen, Fragen überspringen und abbrechen: Es darf nichts gespeichert werden.
2. Auf Frage 1 antworten, auf Frage 2 nichts schreiben, auf Frage 3 antworten; zurückgehen und ändern; anschließend nur die eigenen Antworten im Entwurf erwarten.
3. Bei komplett leeren Antworten bleibt Speichern im bestehenden Editor deaktiviert.
4. Den fertigen Entwurf ausdrücklich speichern; neu starten, suchen, archivieren, via EKB2 exportieren/importieren und unveränderte Textdaten prüfen.
5. Mit geeigneten M7.2-Mustern aus dem Rückblick den Einstieg über Beobachtungskarte und Belegdetail testen. Der gemeldete Zusammenhang ist kein Beweis einer Ursache und darf nicht als Erkenntnistext vorausgefüllt sein.
6. Bei einem fehlgeschlagenen oder abgebrochenen Import, Sperren/Entsperren und Hintergrundwechsel keine offenen privaten Eingaben weiter anzeigen.
7. 320 dp, Schriftgröße 200 %, Dark Mode, TalkBack, Tastatur und Zurück-Geste prüfen. Alle drei Schritte und Schaltflächen müssen erreichbar bleiben.

## Automatisierte Gates

JVM-Tests sichern die unverfälschte Übergabe ausschließlich selbst geschriebener Antworten und das Überspringen. Drei neue Compose-Emulatortests prüfen die explizite Speichergrenze, den leeren Entwurf und den Einstieg über eine Musterbeobachtung. Insgesamt werden 12 UI-Tests jeweils bei 100 % und 200 % Schrift ausgeführt, zuzüglich der drei bestehenden EKB1/EKB2-Keystore-Tests.

Nach grüner CI bleiben die manuelle Geräteprüfung und die redaktionelle Sichtung der Fragen vor der Merge-Freigabe notwendig.
