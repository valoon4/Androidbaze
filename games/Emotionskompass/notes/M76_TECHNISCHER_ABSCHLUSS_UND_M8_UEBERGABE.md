# M7.6 – Technischer Abschluss M7 und geordnete M8-Übergabe

Stand: 06.11.2026. **Technischer Meilenstein / Debug-Vorabversion, kein öffentlich freigegebener Release.** Grundstand: PR #43 (M7.5.2d) nach ausdrücklicher Nutzerfreigabe und grüner CI in `main` gemergt. Dieser M7.6-PR bleibt bis zur erneuten ausdrücklichen Freigabe **Draft und ungemergt**.

## Produktziel und bewusste Grenzen

Der Rückblick soll Menschen helfen, eigene **dokumentierte** Gefühle, Bedürfnisse, Lebensbereiche und persönliche Beobachtungen verständlich wiederzufinden, zu reflektieren und – nur auf freiwillige Entscheidung – eigene Erkenntnisse oder Strategieideen festzuhalten. Er ist **kein** psychologischer Diagnostik-, Stimmungs-, Beurteilungs-, Therapie-, Verhaltens- oder Fortschrittsscore. Ein Tag ohne Tagebucheintrag ist weder ein Erfolg noch ein Versäumnis. Es gibt keine Streaks, Erinnerungszwänge, automatische Textinterpretation oder automatisch generierte Erkenntnisse.

M7.6 führt bewusst **keinen weiteren Navigationsbereich, neuen Datentyp oder neues Backupformat** ein. Die Qualität wird stattdessen durch End-to-End-Abgleiche zwischen den Projektionen und klare Sicherheits-/Release-Gates abgesichert.

## M7 – Funktions- und Nachweis-Matrix

| Etappe | Ergebnis | Automatisierter Nachweis / Grenze |
|---|---|---|
| M7.0 | Authentifizierte Datenbasis für den Rückblick; Lade-, Fehler- und App-Lock-Schranken | `AppViewModel.insightsSnapshotIfAvailable()` liefert bei gesperrtem, unvollständigem oder fehlerhaftem Laden keinen Snapshot; UI-Gates und Emulator-Sperrregressionen. |
| M7.1 | Neutrale, genaue Nennungszählung über wählbare Zeiträume mit Belegnavigation | JVM-Domänentests und Android-UI-Regressionen; Intensität 0 oder „nicht angegeben“ bleibt eine dokumentierte Auswahl, kein Gefühlswert. |
| M7.2 | Behutsame, ausdrücklich erklärbare Kombinationen mit zutreffendem Zähler/Nenner | `InsightEvidence` begrenzt Treffer auf echte Belege und kennt keine Prozentzahl bei Nenner 0; Muster und Beleglinks sind getrennte, freiwillige Aktionen. |
| M7.3 | Eigene Erkenntnisse, frei verfasst und ausdrücklich gespeichert | Erkenntnis-Editor, Editieren, Archivieren/Löschen und optionales einzelnes Journal-Linking; private Ablage/Backup wird durch AndroidKeyStore-Integrationsprüfungen geschützt. |
| M7.4 | Freiwillige eigene Strategieideen/Versuche, Bezug zu Erkenntnissen und gesicherte Drei-Bereich-Backups | EKB3-/Altformat-Integrationsprüfungen, automatische Android-UI-Tests, ohne automatische psychologische Empfehlung. |
| M7.5.1 | Fakultative, textgleichwertige Mini-Balken | Nennungswert und Belegaktion ohne Stimmungs-/Performancewertung; UI-Regressionsabdeckung. |
| M7.5.2a | Neutraler Tagebuchkalender | Vollständiger authentifizierter Journal-Snapshot, echter Tag/Anzahl/ID-Nachweis unabhängig vom 7/30/90-Tage-Statistikfilter. |
| M7.5.2b | Montag–Sonntag-Wochen mit nachvollziehbaren Begriffsnennungen und echten Belegen | Randwochen, Jahreswechsel, Sommerzeit, 0 bis 3000 Einträge und Edit-/Löschänderungen geprüft. |
| M7.5.2c | Freiwillige Wochenreflexion im bestehenden Erkenntnis-Editor | Leeres Textfeld, offene Frage, kein automatisches Speichern, keine automatische Journalverknüpfung; Rückkehr zur gleichen Woche. |
| M7.5.2d | Gemeinsame UX-Qualitätsrunde | Abbruchbestätigung für ungespeicherten Text, Doppelspeicher-Gate, schnelle Monatsnavigation, gemeinsamer Lock-Regressionstest; **104 Android-UI-Testdurchläufe + 12 AndroidKeyStore-Backup-Tests** im gemergten PR #43 bestätigt. |
| **M7.6** | **Technischer Abschluss, zeit-/datenübergreifende Konsistenz und dokumentierte Restrisiken** | **Neu:** `M7ReleaseConsistencyTest` gleicht Kalender/Tag/Woche/Termbelege für 0, 1, 10, 100, 1000 und 3000 fiktive Einträge ab, inklusive Jahreswechsel, Bearbeitung/Löschung und Berlins DST/UTC-Grenze. Keine neue Speicherung. Build prüft jetzt explizit App-ID, Versionscode und -name sowie APK-Signatur. CI-Ergebnis erst nach erfolgter Ausführung als bestanden werten. |

## M7.6 – Automatisierbare Freigabe-Gates

**Pflicht-Gates (für exakt denselben PR-Head-SHA, keine früheren erfolgreichen Läufe übertragen):**

1. Kotlin-`testDebugUnitTest` grün einschließlich neuer `M7ReleaseConsistencyTest`-Fälle; keine fachlich unzulässigen Aggregationsannahmen.
2. Android-`assembleDebugAndroidTest` kompiliert und `assembleDebug` produziert eine installierbare APK.
3. CI-Emulator vollständig grün: **52 Compose-Tests × 2 Schriftgrößen = 104 Testdurchläufe** bei Standard- und 320dp/200%-Layout, plus **3 EKB2** und **9 EKB3** echte AndroidKeyStore-Backup-Integrationstests.
4. `aapt dump badging` bestätigt `com.valoon4.emotionskompass`, `versionCode=36`, `versionName=0.7.6-m7-final-preview`. `apksigner verify` muss erfolgreich sein.
5. Artifact enthält die echte `Emotionskompass-M76-v0.7.6-debug.apk` und eine dazu passende `sha256`-Datei; Datei und Hash werden vor Weitergabe erneut geprüft.
6. M7.6 PR bleibt **Draft**; Merge nur auf ausdrückliche Nutzerfreigabe. Andere Anwendungen im gemeinsamen GitHub-Repository werden **nicht** geändert.

**Nicht automatisch nachgewiesen:** tatsächliche Benutzbarkeit im Alltag, sichere Gerätekonfiguration des Endnutzergeräts, Plattform-Barrierefreiheit und psychologische Qualität des erklärenden Textes. Grüne CI ist ausdrücklich keine fachliche oder öffentliche Release-Freigabe.

## Vor öffentlicher Veröffentlichung offene manuelle Gates

- **Zugänglichkeit:** reale TalkBack-Lesereihenfolge, Fokus und Zurück-Navigation, Switch Access/Voice Access, Tastatur-/IME-/Autofill-Verhalten, Kontraste in Dark Mode, Systemschrift größer als 200 % und Rotation.
- **Gerätekompatibilität:** mindestens ein reales kleines und ein großes Android-Gerät; Reaktion auf Hintergrundsperre, Prozessabbruch, wiederholtes Öffnen und Geräte-Neustart.
- **Datensicherheit:** unabhängige Security-Review der lokalen Verschlüsselung/Keystore-Grenze; Wiederherstellung aus **echter verschlüsselter Test-Sicherung** nach Import/Export auf passenden Geräten; Limits, falsches Passwort, unterbrochene SAF-Dateiauswahl, wiederholtes Löschen und Datenbereinigung.
- **Inhalt / Verständlichkeit:** kritische Fachprüfung neutraler, nichtdiagnostischer Texte, 5 freiwillige Nutzersessions mit **ausschließlich fiktiven** Daten, insbesondere ob Zählungen nicht mit Gefühlsstärke oder versäumten Tagen verwechselt werden; Rückweg und Belegnavigation ohne Anleitung finden.
- **Distribution:** App-Signing-/Update-Strategie, Release-Konfiguration, Datenschutzinformationen und gegebenenfalls Store-Prüfungen. Der aktuelle CI-Debug-Schlüssel ist **ephemer**: signierte APKs verschiedener GitHub-Runs können wegen unterschiedlicher Signatur nicht als Update installiert werden. **Nicht leichtfertig deinstallieren** – ohne zuvor erfolgreich erstellte/prüfbare verschlüsselte Sicherung können lokale Daten verloren gehen. Dieser Debug-Build ist nicht zur öffentlichen Auslieferung bestimmt.

Die manuellen Gates wurden auf früheren Nutzerwunsch für das **Entwicklungstempo** übersprungen, **nicht bestanden**. Vor einer echten Releaseentscheidung muss eine verantwortliche Person jedes Gate mit Belegen bewerten oder das Risiko transparent ausdrücklich übernehmen; fachlich kritische Risiken dürfen nicht als erledigt gelten.

## Akzeptanzentscheid zum Abschluss

- **M7 technisch implementiert und automatisiert regressionsgeprüft:** nur wenn alle aktuellen CI-Gates für den M7.6-Commit grün sind.
- **M7 als Endnutzer-Release voll abgenommen:** **nein**; die oben gelisteten manuellen Prüfungen sind offen.
- **Produktgrenze:** Jede M8-Funktion darf vorhandene private Erkenntnisse/Tagebucheinträge weder automatisch auswerten noch in neue Systeme oder unverschlüsselte Datenbestände übernehmen. Eine zusätzliche Auswertung erfordert eigenes Privacy- und UX-Gate.

## M8 – Übergabe und Ausgangspunkt

1. M8 erst **inhaltlich** priorisieren und im bestehenden Konzept konkretisieren: Ziel, klarer Mehrwert, Datenbasis, verständlicher Einstieg, Abbruchwege und keine Pflicht.
2. Vor Implementierung anhand des M7-Abschlussdokuments die Querschnittsanforderungen übernehmen: freiwillige Nutzung, neutraler Ton, optionales Lernen, sensible Daten nur nach ausdrücklicher Handlung, echte Belegbarkeit, klare Fehler-/Lock-Gates und barrierearme Navigation.
3. Für jeden M8-PR dieselben Regeln: nur `games/Emotionskompass/**`, isolierter Entwicklungsbranch, CI/Debug-APK, **kein Merge ohne Freigabe**.
4. **M8-Produktumfang ist hier bewusst nicht als bereits genehmigt dargestellt.** Seine einzelnen Etappen werden aus dem aktuellen Gesamtkonzept abgeleitet und vor Umsetzung durchgesprochen.
