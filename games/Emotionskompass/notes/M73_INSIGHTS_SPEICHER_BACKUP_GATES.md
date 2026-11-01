# M7.3 – Eigene Erkenntnisse: sichere schrittweise Umsetzung

**Beginn:** 31.10.2026 · **Branch:** feature/emotionskompass-m73-insights-secure-foundation · **Status:** M7.3.0 und M7.3.1 im Draft-PR #28 implementiert; M7.3.2-Oberfläche und vollständige Abnahme stehen aus. Keine neue Nutzerspeicherung freigegeben.

## Nutzerziel

Menschen dürfen eigene Beobachtungen und Gedanken **frei formulieren und ausdrücklich speichern**. Eine App-Beobachtung wird niemals automatisch zur persönlichen Erkenntnis. Die Funktionen umfassen Erstellen, Bearbeiten, Suchen, Archivieren, Löschen, optionale Links zum Tagebuch und später freiwillige, überspringbare Reflexionsfragen. Keine psychologischen Interpretationen, keine implizite Bewertung.

## Reihenfolge / harte Datensicherheitsgates

1. **M7.3.0 – Domain + gesicherter Exportvertrag:** stabile UUIDs, Zeitstempel, Text- und Mengenbegrenzungen; reine CRUD-Operationen; separate Versionierung des kombinierten EKB2-Dateninhalts (Journal + Erkenntnisse); AES-256-GCM, frischer Salt/Nonce, PBKDF2-SHA256; vorerst nur Tests/Produktcode, **kein öffentliches Speichern**.
2. **M7.3.1 – Verschlüsselte Persistenz und Wiederherstellung:** separate Android-Keystore-geschützte AtomicFile-Datei, strenge Dekodierung und Fehlerbehandlung. Koordinierter EKB2-Export beider Datenbereiche, Import mit Crash-/Rollback-Konzept und Integritätsprüfung. **EKB1-Altimport ersetzt nie ungefragt schon vorhandene Erkenntnisse**: fehlender Bereich ist kein leerer Bereich. Gerätewechsel und Recovery müssen getestet werden.
3. **M7.3.2 – Nutzerschnittstelle:** Ein eigener Bereich „Erkenntnisse“ innerhalb des bestehenden Rückblicks, keine neue Bottom-Navigation. Explizite Speicherschaltfläche, Bearbeiten, Suchfeld, getrenntes Archiv, Löschen mit Bestätigung, optionaler Belegbezug; gut verständliche Fehlermeldungen und Rückkehr zur richtigen Ansicht.
4. **M7.3.3 – Kontextbezogene Reflexionsfragen und Abnahme:** redaktionell formulierter, offline verfügbarer, freiwilliger Fragenkatalog; Antworten nur auf Nutzeraktion in einer Erkenntnis speichern. Tests mit 0/1/100/500 Erkenntnissen, gesperrtem Journal, korrupter Datei, fehlendem Schlüssel, unterbrochenem Import, EKB1/EKB2, Rotation, 200-%-Schrift/TalkBack sowie tatsächlichem Gerätewechsel.

## Umsetzung M7.3.1 (technischer Entwicklungsstand)

- Die bestehende Tagebuch-Export-/Import-Oberfläche nutzt nun `CombinedBackupRepository`: Export erstellt EKB2 mit Tagebuch **und** persönlichen Erkenntnissen, Import erkennt EKB1 und EKB2.
- Ein EKB2-Import ersetzt nach ausdrücklicher Bestätigung beide Bereiche. Ein EKB1-Altimport bewahrt bestehende Erkenntnistexte; nur Verknüpfungen zu nicht mehr vorhandenen Journal-IDs werden entfernt.
- Beide bestehenden Datenbereiche bleiben jeweils Android-Keystore/AES-GCM-verschlüsselte `AtomicFile`-Dateien. Es gibt keine Klartext-Zwischendatei und keine neuen Berechtigungen.
- **Crash-Replay statt falschem Transaktionsversprechen:** Vor jeder dateiübergreifenden Änderung wird das komplette geprüfte Ziel als `EKR1`-Wiederherstellungsauftrag mit eigenem Keystore-Schlüssel verschlüsselt und atomar abgelegt. Erst dann werden Journal und Erkenntnisse geschrieben. Beim nächsten authentifizierten Laden wird ein unterbrochener Auftrag vollständig wiederholt, bevor irgendein Journal-Snapshot angezeigt wird.
- Fehlt der Wiederherstellungsschlüssel, ist die Markierung beschädigt oder schlägt die Wiederholung fehl, werden keine scheinbar leeren oder teilweise wiederhergestellten Daten angezeigt. Weitere Schreibaktionen sind bis zum erfolgreichen Neuladen blockiert.
- Auch beim Löschen eines Journaleintrags oder des gesamten Tagebuchs bleiben selbst verfasste Erkenntnisse erhalten; nur zugehörige Belegverweise werden entfernt. Diese Änderungen verwenden denselben Wiederherstellungsauftrag.
- Neue JVM-Tests simulieren Absturz zwischen beiden Dateischreibvorgängen, fehlende Marker, fehlerhaftes Markerformat und fehlgeschlagenes Aufräumen. Android-Emulatortests nutzen fiktive Einträge und den echten Android Keystore, einschließlich EKB2-Export/Import, EKB1-Altimport, Löschregeln und falscher Passphrase.
- Versionskennung der Test-APK: `0.7.3-m7.3-backup-preview`. Das Backup ist über die Tagebuch-Sicherheitsoberfläche zugänglich; der eigene Erkenntnis-Editor bleibt bewusst gesperrt.

**Verbleibende Freigabegates:** manuelle EKB1-/EKB2-Roundtrips auf zwei **tatsächlichen** getrennten Geräten, unterbrochener Android-Dateipicker, beschädigtes/fehlendes Keystore-Material, echte Lebenszyklus-/Rotationstests und externe Security-Review. Bestehende EKB1-Testdateien bleiben importierbar, enthalten jedoch naturgemäß keine Erkenntnisse. Debug-APK und CI ersetzen diese Tests nicht.

## Datenverträge

`PersonalInsight`: UUID, Erstellungs-/Änderungszeitpunkt UTC, persönlicher Text bis 2.000 Zeichen, Archivstatus, optionale Tagebuch-/Gefühls-/Bedürfnis-IDs mit begrenzter Anzahl. Bis 500 Einträge. Ein Tagebucheintrag darf gelöscht werden, ohne dass dadurch der selbst verfasste Erkenntnistext verschwindet; nur die betreffende Referenz wird entfernt. Alle Daten nur verschlüsselt, niemals in Logs, Analytics, unverschlüsselten Settings oder Auto Backup.

Ein **EKB2**-Export enthält das vollständige Journal samt Erkenntnissen und ist eigenständig passphraseverschlüsselt. Ein **EKB1**-Import bleibt lesbar, enthält aber strukturell keine Erkenntnisse; das Import-UI muss bestehende Erkenntnisse daher explizit schützen. Reine Datenformat-Unterstützung bedeutet noch nicht, dass das Ergebnis schon in der App importierbar wäre.

## Sperren vor Merge/Freigabe

- Keine Nutzerspeicherung, bis EKB2 in der echten UI einen geprüften Export-/Import-Roundtrip ermöglicht.
- Keine zwei Dateischreibvorgänge bei einem Import ohne nachgewiesene Recovery-Garantie.
- Keine stillen Überschreibungen bei Fehlentschlüsselung, fehlenden Keys, EKB1, kaputten Quell-IDs oder älteren Versionsständen.
- Selbst wenn CI/Debug-APK erfolgreich sind: reale Gerätetests, unabhängige Datenschutzprüfung und ausdrückliche Merge-Freigabe bleiben nötig.
