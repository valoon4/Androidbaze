# M6 – Interaktives Lernen (0.6.0-m6)

**Stand:** 30.10.2026. **Android:** `com.valoon4.emotionskompass`, versionCode **12**, Android 8+. **Status:** Entwicklungs-/Testfassung.

## Zusammenfassung

M6 baut den bereits vorhandenen Offline-Gefühls- und Bedürfnisatlas zu einem persönlichen, freiwilligen Lernbereich aus. Die untere Hauptnavigation bleibt bewusst bei fünf Bereichen. Über die Startseite öffnet sich **„Lernen & Entdecken“** mit sechs Bereichen:

1. **Heute:** deterministic offline ausgewähltes Gefühl und Bedürfnis des Tages, abgeleitet aus dem *lokalen* Kalendertag (`LocalDate.now()`). Kein Push, keine Uhrzeitabfrage, kein Remote-Dienst.
2. **Favoriten:** Beide Katalogtypen lassen sich auf ihren Atlas-Detailseiten, im Tagesbereich und in Lernpfaden markieren bzw. wieder entfernen. Eigene Liste mit Wortsuche und Typ-Filter.
3. **Üben:** Redaktionell begründete Unterscheidungen aus den vorhandenen 41 Gefühls- und 20 Bedürfnisvergleichen, plus Bedürfnis-vs.-Strategie-Aufgaben aus den Bedürfnisfeldern. Fünf Aufgaben, nicht automatisch benotet; nach jeder Wahl wird die Zuordnung aus dem Atlas erklärt. Keine Einordnung der tatsächlichen Emotionen des Nutzers.
4. **Wiederholen:** Freiwillige Fünfer-Runde aus ausdrücklich als entdeckt markierten oder favorisierten Wörtern. Wörter mit weniger Wiederholungen werden bevorzugt, Favoriten ebenfalls. Kein Spaced-Repetition-Heilungsversprechen, keine Strafpunkte oder Streaks.
5. **Lernpfade:** Die vorhandenen **14 Gefühls- und 8 Bedürfnis-Lernpfade** erhalten abrufbare Stationstexte, Reflexionsfragen, Favoriten-Aktion und explizites „Station abschließen“. Eine Stationsmarkierung speichert automatisch auch „Wort entdeckt“, atomar in einer Lernstandsänderung. Überspringen und Abbruch jederzeit möglich.
6. **Übersicht:** Anzahl der bewusst entdeckten Gefühls- und Bedürfniswörter, Favoriten, Wiederholungen, abgeschlossenen Pfade und Stationen. Alles ohne psychologische Bewertung. Bestätigtes Zurücksetzen löscht **nur** den Lernstand, nicht Tagebucheinträge oder Atlanten.

## Schutz und Datenmodell

- Lokal persistiert werden nur Schlüssel wie `e:wehmut` oder `n:naehe`, Favoriten- und Entdeckt-Sets, Wiederholungszähler und explizite Pfadstationsschlüssel.
- **Keine persönlichen Check-in-Notizen**, Intensitäten oder Situationen werden im Lernstand gespeichert. Keine Synchronisation, Netzverbindung, Werbung, Profilbildung, Diagnose oder Analyse.
- Dateipfad `files/learning_progress_v1.enc` und separater nicht exportierbarer **AndroidKeystore AES-256-GCM**-Schlüssel `emotionskompass_learning_progress_v1`.
- Ganze JSON-Struktur einschließlich Favoriten/Verlauf ist mit AES-GCM verschlüsselt (`EKL1`, zufällige IV, authentifiziertes AAD). Datei `AtomicFile` zur atomaren Aktualisierung, maximal 512 KB entschlüsselte Daten; vor dem Öffnen Größenprüfung, danach Authentizitätsprüfung, strikte Formatschlüsselprüfung.
- Bestehende lokale Journaldaten bleiben mit **unverändertem** eigenen Schlüssel und `private_journal_v1.enc` gespeichert. Kein M6-Journal-Schemaupgrade.
- Wenn die zusätzliche M5-Gerätezugangssperre aktiviert ist, wird der Lernstand erst nach Bestätigung eingelesen; beim App-Background-Lock wird dessen entschlüsselter UI-Cache geleert. Dies ist weiterhin eine **UI-Sperre, keine kryptografische Keystore-Authentifizierungsbindung**.
- Der M5-Passphrase-Export enthält absichtlich nur das Tagebuch, **nicht** die eigenständigen Lernfavoriten. Lernverlauf ist nicht geräteübertragbar; bei Deinstallation verschwinden die lokalen Markierungen.
- **Fehlerfall:** Falls das verschlüsselte Lernstandsformat unlesbar ist oder der Schlüssel fehlt, wird die Datei nicht stillschweigend überschrieben. Die UI zeigt einen Fehler und bietet „erneut öffnen“.

## Automatische Tests

`LearningEngineTest` prüft:
- beide Katalogtypen, eindeutige stabile Schlüssel und Textvollständigkeit;
- deterministische tägliche Zuordnung;
- alle 22 Lernpfade und deren Stationen;
- redaktionelle Fragen, Unterschiede und Bedürfnis-vs.-Strategie;
- kurze Übungen mit beiden Themen;
- Favoriten, Entdeckt-Markierung, Wiederholungszählung und priorisierte Wiederholungen;
- verschlüsselungsinternes JSON-Datenformat (Roundtrip, Versionierung und ungültige Schlüssel).

Alle bisherigen Tests (Atlas, Check-in, verschlüsseltes Journal, Backups, M5.1-Härtung) bleiben aktiv. GitHub Actions führt `testDebugUnitTest`, `assembleDebug`, `aapt`, `apksigner` und SHA-256 aus.

## Manuelle Akzeptanz auf einem echten Smartphone (noch offen)

1. M5.1 auf M6 aktualisieren; Tagebucheinträge, Verschlüsselung, App-Sperre und Backup/Import unverändert verfügbar.
2. Startseite „Lernen & Entdecken“ öffnen; Hauptnavigation bleibt fünf Reiter.
3. Gefühl und Bedürfnis des Tages auf gleichem Kalendertag identisch, auf Folgetag geänderte Vorschläge.
4. Gefühls- und Bedürfnisdetail aufrufen, Favorit aktivieren; im Lernbereich filtern, suchen, Favorit wieder entfernen.
5. Übung mit emotionstypischen und bedürfnisbezogenen Fragen durchspielen; redaktionelle Erklärung erscheint erst nach Wahl.
6. Freiwillige Wiederholung aus Favoriten oder bewusst entdeckten Wörtern beginnen; ohne Favoriten/Entdeckungen sinnvoller Leerzustand.
7. Gefühls- und Bedürfnislernweg öffnen, Station abschließen, zurück, App neu starten: Markierung muss erhalten bleiben.
8. Lernstand ausdrücklich zurücksetzen und kontrollieren, dass das Tagebuch vollständig erhalten bleibt.
9. Ohne Netzwerk testen; große Schrift (200 %), TalkBack, Querformat, kleine Displays und Dark Mode.
10. Zusätzliche App-Sperre aktivieren: Lernstand vor Authentifizierung nicht sichtbar, Hintergrundwechsel sperrt UI und räumt Cache.
11. Testgerät mit beschädigter Lernstandsdatei: Fehlermeldung statt stiller Datenüberschreibung.
12. Inhaltliche Prüfung der Lernfragen/Unterschiede durch menschliche Fachredaktion vor öffentlicher Veröffentlichung.

**Grenze:** Kein medizinisches Diagnosesystem, keine Therapie, keine validierten psychometrischen Scores. Kein unabhängiger Security- oder Accessibility-Audit und keine physische Geräteabnahme durch CI.
