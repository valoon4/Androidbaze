# M7.5.2b – Aufklappbare Wochenübersichten (Umsetzung und Abnahme)

Stand: 05.11.2026. Setzt die in PR #38 vereinbarte **freiwillige Wochenübersicht** auf der Basis des gemergten Tagebuchkalenders M7.5.2a (PR #40) um. **Die freiwillige Wochenreflexion gehört erst zu M7.5.2c**; es werden in M7.5.2b keine Erkenntnisse erzeugt oder gespeichert.

## Bedienung

Unter **Rückblick → Übersicht & Muster** ergänzt „Wochenrückblick ansehen“ den optionalen Tagebuchkalender. Der Wochenrückblick ist beim Öffnen zunächst **geschlossen** und zeigt nach ausdrücklichem Antippen:

1. Monats-/Jahresüberschrift und je eine gut lesbare „Vorheriger Monat“-/„Nächster Monat“-Aktion, bei 320 dp und 200 % Schrift **untereinander** und mindestens 52 dp hoch.
2. Für den gewählten Monat alle **vollständigen Montag–Sonntag-Wochen, die diesen Monat berühren** (typisch vier bis sechs). **Randwochen umfassen angrenzende Monate**, was ausdrücklich erklärt wird. Jede zunächst geschlossene Wochenkarte nennt **vollen Datumsbereich inklusive Jahr**, Anzahl tatsächlich gespeicherter Einträge und „Details anzeigen“.
3. Nur eine Woche kann gleichzeitig geöffnet sein; dann werden **ausdrücklich dokumentierte Gefühls- und Bedürfnisnennungen** mit **exakter Anzahl** gezeigt. Die jeweils drei meistgenannten Begriffe sind direkt anklickbar und führen zu den genau zugehörigen Tagebucheinträgen. Bei mehr als drei Begriffen je Kategorie kann man **alle dokumentierten Begriffe** als separat scrollbare, vollständige Liste öffnen, einschließlich Belegnavigation.
4. „Einträge dieser Woche ansehen“ führt zur **vollständigen Liste echter Tagebucheinträge** dieser Woche, unter Beibehaltung der bestehenden stabilen ID-basierten geschützten Tagebuchlinks. Einzelne Einträge werden als separate LazyColumn-Items dargestellt, nicht als lange verschachtelte Karte.
5. Eine **leere Woche** trägt „0 gespeicherte Einträge“ und enthält neutral: „Daraus lässt sich nichts über dein Erleben ableiten“. Kein Eintragszwang, keine Fortschrittsanzeige oder Reflexionsaufforderung als Lückenfüller.
6. Die Wochenübersicht und der Tagebuchkalender sind **separate freiwillige Navigationshilfen**. Beide nutzen den **gesamten sicher authentifizierten Journal-Snapshot**, unabhängig vom Statistikfilter 7/30/90 Tage; dieser Bezugsrahmen ist für beide sichtbar erklärt.

## Daten- und Sicherheitsgrenze

- `WeeklyReview.month(snapshot, month, zone)` ist eine **pure, nicht persistierende** Projektion: sieben lokale Kalendertage (Montag bis Sonntag), Journal-IDs und **nur explizit ausgewählte** emotion/need-IDs aus `InsightEntry`. Keine Freitexte, Intensitätsauswertung, Inferenz, Netzwerkanfrage oder eigene Speichertabellen.
- Gezählt wird **einmal pro Begriff und Tagebucheintrag**: die angezeigte Nennungszahl kann über die Anzahl der Einträge hinaus zu einer höheren Gesamtsumme führen, wenn ein Eintrag mehrere Gefühle und Bedürfnisse enthält. Unbekannte Bedarfs-ID `CheckInDraft.UNKNOWN_NEED_ID` wird **nicht** als benanntes Bedürfnis gezählt. Die Intensität **0** ist bei einer echten Auswahl eine Nennung.
- Wochen sind lokal exakt durch `InsightTimeWindow.inclusiveDays` begrenzt; kein Addition von 7×24 Stunden, damit die Sommerzeit-Umstellung korrekt bleibt.
- Der Zustand (ausgewählter Monat, Woche, Begriff und Belegliste) liegt nur im **Compose-Arbeitsspeicher**; beim App-Lock, Ladefehler, Import, Editieren und Löschen wird er ungültig. Keine Speicherung in `rememberSaveable`, Android Bundle oder Backup. Nur eine **entsperrte**, erfolgreich geladene Journalprojektion ermöglicht die Anzeige.
- Die vorhandene Journal-/EKB3-Verschlüsselung bleibt unverändert, und eine reine Anzeige erzeugt **keinen** Check-in, keine persönliche Erkenntnis und keine Strategie.
- **Keine psychologische Wertung:** keine „beste“ Woche, kein Trendpfeil, kein Score, keine Streaks, keine Interpretation von Eintragslücken.

## Testplan

- Reine JVM-Tests mit fiktiven Daten für 0/1/10/100/1000/3000 Einträge, jeden Montag–Sonntag-Bereich, leere Wochen, Monats- und Jahresübergänge, Sommerzeit Europe/Berlin, korrekte ID-Reihenfolge und termgenaue Belege, 0-Intensität sowie unbestimmte Bedürfnisse, Änderung/Löschen und Statistikfilterunabhängigkeit.
- Vier neue Android-Compose-Tests an **normaler und 200-%-Schriftgröße**: Opt-in und echter Tagebuchlink, neutrale leere Woche über Jahreswechsel, vollständige Begriffs- und Belegliste, sicheres Ausblenden bei Journalfehlern. Gesamtziel: **43 Tests × zwei Schriftgrößen = 86 UI-Ausführungen**, zusätzlich 3 EKB2- und 9 EKB3-Regressionstests.
- Debug-APK: `0.7.5-m7.5.2b-weekly-preview`, `versionCode = 33`.
- Echte manuelle TalkBack-Hörabnahme, physische Geräte-/Rotation-/Dark-Mode-Tests, unabhängige Fach-/Sicherheitsprüfung und freiwillige Nutzertests bleiben **offen bzw. auf Wunsch übersprungen**, nicht als bestanden vermerkt.

## Folgeetappen

- **M7.5.2c:** Freiwillige Wochenreflexion als rein optionaler Einstieg in den vorhandenen Bereich „Meine Erkenntnisse“; kein vorbefüllter Text, keine automatische Analyse oder Speicherung.
- **M7.5.2d:** Übergreifende UX- und Qualitätsprüfung des Kalenders und aller Wochenfunktionen.

**Draft-PR; kein automatischer Merge.**
