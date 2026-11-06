# M7.5.2d – Übergreifende UX- und Qualitätsrunde

Stand: 06.11.2026. Aufbauend auf dem gemergten PR #42: M7.5.2a Tagebuchkalender, M7.5.2b Wochenübersichten und M7.5.2c freiwillige Wochenreflexion.

## Ziele und Befunde

Prüfkriterien: Nutzen, verständliche Navigation, klare Semantik der Zählungen, keine Pflicht oder Wertung, Datenschutz bei Sperre/Import/Löschen, Bedienbarkeit bei 320 dp und 200 % Schrift, echte belegbasierte Navigation und freiwillige Speicherung.

| Priorität | Gefundener UX-/Sicherheitsaspekt | Umsetzung in M7.5.2d | Rest |
|---|---|---|---|
| Hoch | Bei Abbrechen einer freiwilligen Wochenreflexion ging ein bereits geschriebener Text ohne Bestätigung verloren. | Gemeinsame Bestätigung „Ungespeicherte Eingabe verwerfen?“ in `PersonalInsightsScreen`: „Weiter bearbeiten“ behält den Entwurf; „Eingabe verwerfen“ verwirft nur diesen Entwurf und kehrt zur zuvor geöffneten Woche zurück. Leere oder unveränderte Editoren können unmittelbar verlassen werden. Android-/Dialog-Back nutzt dieselbe Prüfung. | Während eines tatsächlichen Speichervorgangs sind Abbrechen und erneutes Speichern bewusst deaktiviert. |
| Mittel | Mehrfaches Antippen von „Ausdrücklich speichern“ konnte vor der Rückmeldung mehrere identische Speicherversuche auslösen. | Solange `saving` aktiv ist, sind Bestätigen und Abbrechen deaktiviert; erst eine bestätigte Rückmeldung beendet den Vorgang. Bei neuem Eintrag wird zudem das bestehende Erkenntnislimit geprüft. | Fachliche Ende-zu-Ende-Sicherheitsprüfung der Persistenz weiterhin offen. |
| Mittel | Nach Navigation in mehrere alte Monate war die Rückkehr zum aktuellen Zeitraum unnötig mühsam. | Beide Monats-Navigationsflächen zeigen bei abweichendem Monat einen mindestens 52 dp hohen Knopf **„Zum aktuellen Monat“**. Beim Klick wird nur die flüchtige Tages- bzw. Wochenauswahl zurückgesetzt; kein Datensatz verändert. | Keine neue Hauptnavigation, bewusst keine Gewohnheitsanzeige. |
| Hoch, geprüft | Journal nicht sicher ladbar oder gesperrt: Risiko privater, alter Kalender-/Wochenzustände. | Bestehende Snapshot-/Keystore-Grenze bleibt bestehen; automatischer kombinierter Test kontrolliert das Leeren von Tag, Woche und Belegliste sowie das vollständige Ausblenden der optionalen Oberflächen. | Separate manuelle Sperr-/Resume-Abnahme offen. |
| Hoch, bereits abgedeckt | Eintragslücken, unbekannte Bedürfnisse, Null-Intensität, Randwochen und Zeitraumfilter dürfen keine irreführenden „Trends“ bilden. | Die bestehenden reinen JVM-Tests und bisherigen Compose-Regressionen laufen unverändert weiter. Null-Intensität ist eine Nennung, unbekannter Bedarf keine benannte Nennung; lokale Wochen berücksichtigen Sommerzeit und Randmonate. | Keine automatischen psychologischen Schlüsse; bei neuem Auswertungsformat erneut prüfen. |
| Mittel, bereits abgedeckt | Extrem große Schrift, viele Einträge und schmale Geräte dürfen Daten oder Bedienelemente nicht abschneiden. | Bestehende automatische 320-dp-/200-%-Darstellung bleibt Standard-Regression; Listen sind separat lazy scrollbare Elemente und Aktionen mindestens 48 dp hoch. Die neuen „aktueller Monat“-Tasten werden bei 200 % mitgetestet. | **Echte TalkBack-, VoiceAccess-, Dark-Mode-, Rotations- und physische Gerätetests weiterhin offen**. |

## Konsistenz und Sicherheit

- Der Tagebuchkalender zeigt Tage mit exakter Anzahl, keine Streaks oder Bewertungen. Die Wochenübersicht zählt **dokumentierte Nennungen**, nicht Stärke, Stimmung, Entwicklung oder Fortschritt. Leere Tage/Wochen werden neutral beschrieben.
- Beide optionalen Navigationswege beziehen sich auf **alle sicher geladenen Journaleinträge**, nicht versehentlich auf den gesonderten 7/30/90-Tage-Statistikfilter.
- Der Wochenreflexions-Editor beginnt leer; kein Satz und kein Journal-Link wird vorausgefüllt. Es wird nur freiwillig eingegebener Text gespeichert, nach ausdrücklichem Tippen. Keine neue lokale Ablage, keine Netzübertragung, keine Änderung der verschlüsselten EKB3/Keystore-Formate.
- `confirmDiscardDraft`, `draft`, Monat/Woche/Tag und der Wochenkontext bleiben in `remember`-Zustand, **nie `rememberSaveable`**. Beim fehlenden/fehlerhaften Journal werden Kalender und Woche nicht angezeigt; beim Laden, Sperren, Löschen und Import werden Auswahlen zurückgesetzt.

## Automatisierte Abnahme

- Vorheriger Stand M7.5.2c: 47 Compose-Tests bei 100 % + 47 bei 200 %, alle bestanden, plus 3 EKB2 und 9 EKB3 KeyStore-Tests.
- **Fünf neue Compose-Tests**, insgesamt Ziel 52 pro Schriftgröße (= 104 Ausführungen), zu: (1) „Aktueller Monat“ im Kalender mit echten Tagesbelegen, (2) „Aktueller Monat“ der Wochenübersicht mit korrekten Zählungen, (3) gemeinsamer Journal-Lock löscht beide optionalen Navigationszustände, (4) normaler Erkenntnis-Editor bestätigt den Verlust ungespeicherter Worte, (5) verhindert doppeltes Speichern bei ausstehender Antwort. Bisheriger Wochen-Reflexionstest erweitert: „Weiter bearbeiten“ und „Eingabe verwerfen“ mit unverändertem Wochenkontext.
- Unverändert: reine JVM-Journaltests zu leeren/großen Datenbeständen, Sommerzeit, Schaltjahr, eindeutigen IDs und vom Statistikfilter unabhängigen Ansichten.
- Backup-Regressionen **3 EKB2 + 9 EKB3** bleiben Pflicht.
- Finaler Debug-Build: `versionCode=35`, `0.7.5-m7.5.2d-quality-preview`.
- Stand der CI- und APK-Abnahme wird erst nach Abschluss im PR dokumentiert, nicht vorweggenommen.

## Manuelle Abnahme noch ausstehend

Diese Qualitätsrunde ist **keine** empirische Studie und ersetzt weder eine Fach- noch Sicherheitsprüfung. Als noch offene, vor Release benötigte Gates gelten: physische Android-Geräte verschiedener Größen, Rotation, systemweites Dark Mode, echte TalkBack- und Schaltersteuerung, Autofill/IME, Background-Sperre sowie wiederholter Restore/Export mit realen (vorher gesicherten) Testbeständen. Die bisher auf Nutzerwunsch übersprungenen manuellen Gates dürfen nicht nachträglich als „bestanden“ gelten.

**PR bleibt Draft; kein Merge ohne Freigabe. Nächster Meilenstein: M7.6 technischer M7-Abschluss und dann Planung M8.**
