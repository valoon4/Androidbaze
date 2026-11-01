# M7 – UX-Qualitätsrunde (Rückblick, Muster, Erkenntnisse, Selbstreflexion)

Stand: 2026-11-01. Umsetzung: Draft-PR #31. Die Änderungen sind vor der ausdrücklichen Freigabe **nicht** Bestandteil von main.

## 1. Befunde aus der realen Bedienung und Codeprüfung

| Priorität | Beobachtung / Risiko | Korrektur im Draft | Akzeptanzkriterium |
| --- | --- | --- | --- |
| P0 | „Meine Erkenntnisse“ wirkt wie ein eigener, versteckter Bereich des Rückblicks. Nutzer finden Muster nicht, wenn sie auf der Erkenntnisse-Seite stehen. | Dauerhaft sichtbare, gleichwertige Navigation „Übersicht & Muster“ / „Meine Erkenntnisse“ oberhalb der jeweiligen Liste. Redundanter Zurück-Link entfällt hier. | Beide Bereiche sind von jeder Rückblick-Ansicht aus direkt erreichbar, auch auf schmalem Display bei 200 % Schrift. |
| P0 | „Behutsame Beobachtungen“ ist ohne Erklärung abstrakt und kann als persönliche Beurteilung missverstanden werden. | „Gemeinsam ausgewählte Begriffe“ und transparente Erklärung: Nur selbst gewählte Kombinationen, keine Ursache. Details per „Wie entstehen diese Hinweise?“ ausklappbar. | Bedeutung und Mindestkriterien sind im UI erreichbar, ohne externe Anleitung. |
| P1 | Lange Rückblick-Ansicht: Die Hinweise werden hinter Zeitraum und Statistik übersehen. | Direkter Sprung „Zu gemeinsam ausgewählten Begriffen“ aus der Zusammenfassung. | Bei belegtem Journal führt die Aktion zur richtigen Überschrift. |
| P1 | Es werden nur die ersten drei der zulässigen Muster angezeigt, ohne Hinweis auf weitere. | Schaltfläche zum bewussten Anzeigen aller erkannten Kombinationen; Rückkehr zu kompakter Ansicht. | Bei mehr als drei Kombinationen sind ausnahmslos alle erreichbar. |
| P1 | Kein erkannter Hinweis wirkt wie ein Fehler oder „zu wenig persönliche Entwicklung“. | Neutraler Leerzustand und optional „90 Tage betrachten“. | Keine Diagnose, kein Score, kein falsch als „0 Muster“ behaupteter Erfolg. |
| P1 | Beim Verknüpfen einer Erkenntnis sind nur sieben Tagebucheinträge auswählbar. | Weitere Einträge ausdrücklich anbieten und nach Datum/Uhrzeit durchsuchen. | Auch ein älterer Eintrag ist freiwillig auswählbar. |
| P1 | Verknüpfung könnte als automatisch erkannte Ursache missverstanden werden. | Sichtbarer Hinweis auf freiwillige Verknüpfung und jederzeitiges Lösen. | Kein automatisches Befüllen von Beleg-IDs aus einer Beobachtung. |
| P2 | Viele Funktions- und Datenschutztexte können bei großer Schrift viel Platz beanspruchen. | Kurzer Einstiegstext, optional ausführliche Kriterien, scrollbar aufgebaute Inhalte. | 320 dp mit 200 % Schrift bleibt vollständig bedienbar. |

## 2. Informationsarchitektur nach der Runde

- **Rückblick** ist der übergeordnete Tab.
- **Übersicht & Muster**: Zeitraum, Anzahl der eigenen Einträge, freiwillige Kombinationen, belegende Tagebucheinträge, Auswertung von Gefühlen und Bedürfnissen.
- **Meine Erkenntnisse**: ausdrücklich selbst geschriebene Texte, Suchen/Bearbeiten/Archivieren und freiwillige Reflexionsfragen. Von einer Musterkarte kann die Reflexion hier gestartet werden; der Kontext ist ausschließlich eine unverbindliche Anregung.
- **Reflexionsfragen**: kein automatischer Text, keine Speicherung bei „Weiter“, nur Nutzerantworten im Zwischenentwurf. Der vorhandene Editor erfordert weiterhin ausdrücklich „Speichern“.

## 3. Regeln, die sich nicht ändern

- Muster sind **wiederholt gemeinsame Auswahlen**, keine medizinische oder psychologische Schlussfolgerung.
- Die Mindestkriterien für die Mustererkennung bleiben unverändert: 10 vergleichbare Einträge, wenigstens 3 Treffer, an mindestens 2 verschiedenen Tagen und in wenigstens einem Viertel der Vergleichseinträge.
- Die App wertet für Muster keine Freitexte aus. EKB1-/EKB2-Verschlüsselung, Keystore, App-Sperre, Archiv-/Löschregeln, Datenmigration und Datenformat bleiben unverändert.
- Keine zusätzliche Berechtigung, kein Netzverkehr und keine automatisch formulierte persönliche Erkenntnis.

## 4. Testplan

1. Im Rückblick in beiden neuen Bereichen wechseln; vor und nach dem Wechsel muss die Navigation sichtbar bleiben, auch mit 200 % Schrift. Ein gewählter Zeitraum soll beim Zurückwechseln erhalten bleiben.
2. Mit dem EKB2-Testdatensatz die Zusammenfassung öffnen und den Sprung zu „Gemeinsam ausgewählte Begriffe“ ausprobieren.
3. Erklärung der Kriterien öffnen/schließen; die Formulierung muss laienverständlich sein und keine Ursache behaupten.
4. Bei mindestens vier nachgewiesenen Kombinationen „Alle … Kombinationen anzeigen“ und „Weniger Kombinationen anzeigen“ testen.
5. Mit wenigen Einträgen den neutralen Leerzustand und „90 Tage betrachten“ testen.
6. Über eine Musterkarte oder deren Belegansicht freiwillige Reflexion starten und abbrechen: Keine neue Erkenntnis und keine automatische Belegverknüpfung.
7. Im Erkenntnis-Editor bei mindestens acht Tagebucheinträgen weitere Einträge anzeigen, nach Datum suchen und freiwillig einen älteren Eintrag wählen. Nach Speichern lässt sich die Verknüpfung auch wieder lösen.
8. Erkenntnisse erstellen, durchsuchen, bearbeiten, archivieren, löschen; gesperrte/fehlgeschlagene Zugriffe dürfen keinen privaten Text anzeigen.
9. EKB2-Export/Import mit Erkenntnissen und EKB1-Altimport unverändert testen.
10. Mindestens einmal auf einem echten Android-Gerät: 200 % Schrift, schmale Breite, System-Zurück, Rotation, TalkBack, Dark Mode und gesperrte App.

## 5. Automatisierte Gates

- Vorhandene JVM-Tests einschließlich Mustererkennung, Reflection und Speichergrenzen.
- 14 instrumentierte Compose-Tests jeweils bei 100 % und 200 % Schrift (28 UI-Testläufe), darunter neue Navigations-/Muster-Vollständigkeits- und Linkauswahl-Regressionen.
- Drei vorhandene reale AndroidKeyStore-/EKB2-Integrationsprüfungen.
- Build, Signatur und installierbare Debug-APK müssen auf demselben finalen Commit geprüft sein.

## 6. Weitere mögliche Verbesserungen (bewusst nicht in diesem PR)

- Ein vollständiger TalkBack-Usability-Test mit echter Vorlesereihenfolge und semantischen Gruppen.
- Qualitative Nutzertests mit mehreren Personen, insbesondere Erstnutzern ohne M7-Vorwissen.
- Erweiterte Metadaten/Filter für verknüpfte Einträge (z. B. mehrere Einträge am selben Tag) – nur nach zusätzlicher Datenschutzevaluation.
- Eine spätere UX-Redaktionsrunde für den gesamten Atlas, Wissen und Tagebuch außerhalb von M7.

**Freigaberegel:** grüner Build und erfolgreiches manuelles Testen, danach ausdrückliche Nutzerfreigabe für Merge.