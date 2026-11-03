# M7.5.1 – Freiwillige Nennungsbalken (Abnahme und Datenschutz)

Stand: 03.11.2026. Die Maßnahme setzt den freigegebenen Konzeptstand M7.5.0 aus PR #38 um. **Kein psychologischer Score, keine Intensitätsdarstellung, kein automatischer Trend.** Von M7.5.2 (Wochenübersicht/Kalender/Reflexion) unabhängig.

## Bedienung und Nutzen

- **Rückblick → Übersicht & Muster → Nennungen**: Neue Aktion „Nennungsbalken anzeigen“ oberhalb der bestehenden Gefühls-/Bedürfniskarten. Die Ausgangsdarstellung bleibt **Text ohne Balken**. Mit „Nennungsbalken ausblenden“ werden ausschließlich die Dekorationen entfernt; Karten, Begriffe, exakte Zahlen und ihre Belege bleiben.
- Jede Karte zeigt einen schmalen **horizontalen Vergleichsbalken unter dem bestehenden Begriff und der exakten Nennungszahl**. Das Layout verläuft vertikal und lässt auch bei 320 dp/200 % Schrift den Text umbrechen.
- Die Balkenlänge ist **innerhalb jeder Kategorie relativ zum meistgenannten Begriff**, nicht als Prozentwert aller Tage, Einträge, Gefühle oder Bedürfnisse zu lesen. Bei genau einem Begriff wird er relativ maximal dargestellt; **keine 100-%-Angabe**. Die Erklärung im UI stellt ausdrücklich klar: Nennungen sind **weder Gefühlsstärke noch Fortschritt**.
- Tippen auf **dieselbe vollständige Karte** öffnet unverändert den bereits implementierten, authentifiziert geladenen Belegpfad mit stabilen Journal-IDs. Kein zweiter Link, keine neue Berechnung oder Filterlogik.
- Die Gestaltung ist reine Redundanz; TalkBack benutzt die existierende Beschriftung mit Begriff, Anzahl und Eintragsaktion, nicht die Form oder Farbe des Balkens. Keine zusätzliche eigenständige Aktion des Balkens.

## Sicherheits- und Datenvertrag

Die vorhandene deterministische `CalculateOverview`-Ermittlung aus dem geschützten `JournalSnapshot` bleibt **unverändert**. Verwendet werden ausschließlich `overview.emotionCounts` und `overview.needCounts` samt exakter `mentions` und `sourceJournalIds`. Eine Kategorie wird nur relativ zu ihrer eigenen maximalen Nennung skaliert. Es entstehen **keine persistierten Auswertungen, neuen Personendaten, Netzwerkanfragen, Backups oder Logs**. Der visuelle Schalter ist nur im Compose-Arbeitsspeicher und wird bei App-Sperre/unsicherem Laden (Snapshot fehlt, Fehler oder Busy) deaktiviert. Die existierenden Leermeldungen werden nicht durch scheinbar echte Nullbalken ersetzt.

Ein einzelner Gefühls-/Bedürfnisbegriff kann in mehreren Check-ins ausgewählt werden, und mehrere Begriffe können pro Check-in vorkommen. Summen der Nennungen dürfen die Anzahl Check-ins übersteigen. Null-Intensität ist eine **ausdrückliche Auswahl**, keine Nullnennung. Die Kategorie „Ich weiß es noch nicht“ ist **kein benanntes Bedürfnis** und erhält keinen Balken.

## Automatisierte Abnahme

- Kotlin-Unit-Tests für die bestehende unveränderte Zählung.
- Neue Android-Compose-Tests prüfen bei normaler und 200-%-Schrift: expliziten Opt-in-Schalter, vorhandene reale Belegnavigation bei sichtbarem Balken, exakte unveränderte Zähler und Ausblenden ohne Entfernen der Textkarte; bestehende Tests decken leere Einträge sowie fehlerhaftes Laden ab.
- Emulator-Testskript prüft **35 Compose-Tests je Schriftstufe (70 Ausführungen)** plus drei EKB2- und neun EKB3-Keystore-Tests; Kotlin-Tests, Testkompilierung und signierte M7.5.1-Debug-APK mit Prüfsumme werden getrennt in CI geprüft.
- Weitere manuelle Prüfungen (echte Geräte, Dark Mode, physisches TalkBack, fünf freiwillige Testpersonen und unabhängiges Fach-/Security-Review) sind auf Wunsch übersprungen und **nicht als erfolgreich dokumentiert**. Ebenso ersetzt grüne CI keine abschließende unabhängige Abnahme.

**Kein automatischer Merge; nur nach ausdrücklicher Nutzerfreigabe.**
