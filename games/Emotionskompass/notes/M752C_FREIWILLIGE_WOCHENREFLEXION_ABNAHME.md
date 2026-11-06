# M7.5.2c – Freiwillige Wochenreflexion (Implementierung und Abnahme)

Stand: 06.11.2026. Baut auf dem ausdrücklich genehmigten und gemergten PR #41 (M7.5.2b) auf. Die gesonderte M7.5.2d-Qualitätsrunde bleibt offen.

## UX-Ablauf

1. **Rückblick → Übersicht & Muster → Wochenrückblick ansehen → eine Woche mit mindestens einem Eintrag aufklappen**.
2. Die dezente, mindestens 52 dp hohe Aktion **„Freiwillig über diese Woche nachdenken“** erscheint innerhalb der geöffneten Wochenkarte unter den bestehenden Eintragslinks. Eine neutrale Erklärung stellt klar: keine automatische Interpretation, Verknüpfung oder Speicherung.
3. Antippen öffnet **den bisherigen, leeren Editor von „Meine Erkenntnisse“**. Darin wird die **überspringbare Frage** „Was ist mir rückblickend an dieser Woche aufgefallen?“ und der volle Datumsbereich der aktuell betrachteten Woche angezeigt. Dieser Datumsbereich ist ein reiner Navigations-/Anzeigekontext: Er wird **nicht in den Entwurf kopiert**, nicht als eigenes Feld gespeichert und nicht automatisch mit Journal-IDs verknüpft.
4. **„Abbrechen“** oder Schließen des Editors verwirft einen eventuellen ungespeicherten Entwurf und führt **zur gleichen aufgeklappten Woche** zurück; gespeicherte Erkenntnisse bleiben unangetastet. Erneutes Öffnen startet mit einem leeren Textfeld.
5. Erst **„Ausdrücklich speichern“** mit selbst formulierten, nichtleeren Worten ruft die **vorhandene** verschlüsselte Erkenntnisspeicherung auf. Die bestehende optionale Einzel-Journalverknüpfung erfolgt ausschließlich per ausdrücklicher Auswahl; keine automatische Übernahme aller Wocheneinträge. Nach dem Speichern bleibt „Meine Erkenntnisse“ sichtbar, mit einer Aktion **„Zurück zur ausgewählten Woche“**.
6. Für **leere Wochen** erscheint keine Reflexionsaktion (kein Druck zum Nachtragen), und bei nicht sicher geladenen, ausgelasteten oder fehlerhaften Erkenntnissen ist die Aktion deaktiviert. Es gibt keine Erinnerung, Tagespflicht, Reflexionsquote, Auto-Textgenerierung, Wochenbewertung, extrahierte Textmuster oder Fortschrittsanzeige.

## Datenschutz / Rückwärtskompatibilität

- `weeklyReflectionContext` ist nur ein kurzlebiger **Compose-`remember`-Wert** (vollständiger Datumsbereich, keine Freitexte), niemals `rememberSaveable`, Backup-, Datenbank- oder Preference-Inhalt.
- Die vorhandene `PersonalInsightsScreen` nutzt denselben Texteditor mit `draft = ""` und `selectedJournalId = null` beim freiwilligen Einstieg. Der Text, ausgewählte Journaleinträge und die Wochenfrage werden **nicht vorausgefüllt oder gespeichert**, solange die Nutzerin/der Nutzer nicht selbst schreibt und ausdrücklich speichert.
- Anzeigekontext wird beim Verlassen der Erkenntnisse, beim Wechsel von Tabs oder bei Journal-Lock, Lade-/Entschlüsselungsfehler gelöscht. Eine abgebrochene Eingabe erzeugt kein privates Objekt.
- Journal-Snapshot, Wochenaggregation, EKB3, Keystore, Versionsnummern der verschlüsselten Formate und vorhandene Erkenntnisgrenzen bleiben unverändert.

## Automatisierte Prüfung

- Vier zusätzliche Compose-Tests in `PersonalReviewEmulatorTest`: (1) leere Eingabe/kein Speichern beim Öffnen, (2) Abbrechen und erneutes Öffnen ohne Textübernahme, (3) selbst verfassten Text ausschließlich nach Speichern und ohne vorgewählte Journal-ID, (4) nur bei verfügbaren Erkenntnissen und gefüllter Woche aktiv.
- **47 Compose-Tests je Schriftgröße (94 Durchläufe)** auf Android 35 (100 % und 320 dp/200 %), plus bestehende **3 EKB2 und 9 EKB3** verschlüsselte Backup-Regressionen.
- Debug-APK `0.7.5-m7.5.2c-reflection-preview`, Code 34, aus GitHub Actions mit SHA256.
- **Offene manuelle Gates:** physische Geräte-/Dark-Mode-/Rotation-/TalkBack-Hörprüfung, Fach-/Security-Audit und Nutzertests wurden nicht durchgeführt und dürfen nicht als „bestanden“ bezeichnet werden.

## Folgeetappe

**M7.5.2d:** Gemeinsame UX- und Qualitätsprüfung für Kalender, aufklappbare Wochenübersichten und Wochenreflexion. PR #42 bleibt Draft bis zur ausdrücklichen Nutzerfreigabe.
