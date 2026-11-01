# M7.3.2 – Eigene Erkenntnisse: UX- und Sicherheitsabnahme

**Status:** Implementierung im separaten Draft-PR #29; nicht gemergt.

## Produktumfang

Im vorhandenen Reiter **Rückblick** führt die eigenständige Schaltfläche **Meine eigenen Erkenntnisse** zu einer freiwilligen persönlichen Sammlung. Keine zusätzliche Bottom-Navigation und kein diagnostischer Score. Alle Texte werden ausschließlich auf explizite Nutzeraktion verfasst und gespeichert. Beim Öffnen werden bestehende EKB2-Erkenntnisse, einschließlich archivierter und verknüpfter, aus authentifiziert geladenem Keystore-Speicher angezeigt.

- Schreiben/Bearbeiten, Zeichengrenze 2.000; gezielte Speicherung ohne Doppeltipp; Suche (inkl. Teilwörtern und Groß-/Kleinschreibung); getrennte Filter „Aktuelle“ und „Archiv“; Wiederherstellung aus Archiv; Löschen nur nach Bestätigung.
- Optional eine der sieben jüngsten Tagebucheintrag-IDs als zusätzliche Verknüpfung auswählen; bestehende Links bleiben bei Textänderungen erhalten und öffnen über die bestehende Journal-Navigation.
- EKB2 enthält Einträge und eigene Erkenntnisse; EKB1-Altimport kann keinen Erkenntnistext anlegen und bewahrt bereits gespeicherte Texte.
- Für sensible Eingaben nur `remember`, nicht `rememberSaveable`. Keine persönliche Texteingabe oder Suchphrase in Android Bundles, DataStore, Logs oder Analytics.
- Fehlgeschlagene Entschlüsselung oder ein unterbrochener Import zeigt **keine** alten Inhalte als vermeintlich gültig an. Bei aktivierter App-Sperre wird InsightUiState zusammen mit JournalUiState verworfen.

## Manuelle Tests auf Debug-APK mit ausschließlich erfundenen Daten

1. EKB2-Testpaket mit 45 Einträgen und 8 Erkenntnissen importieren. Unter Rückblick > Meine eigenen Erkenntnisse prüfen, ob 6 aktuelle und 2 archivierte Erkenntnisse gezeigt werden. Archiv separat öffnen.
2. Neue selbst formulierte Erkenntnis erstellen. Schaltfläche „Ausdrücklich speichern“ bleibt ohne Text inaktiv; nach Speichern wird genau eine Erkenntnis angezeigt.
3. Neue Erkenntnis mit einem der vorhandenen Tagebucheinträge verbinden, Link öffnen und zurücknavigieren.
4. Text bearbeiten; ursprüngliche ID und ursprünglicher Erstellungszeitpunkt müssen erhalten bleiben. Inhalt bleibt nach vollständigem App-Neustart vorhanden.
5. Suchfeld mit Teilwort und abweichender Groß-/Kleinschreibung testen. Nur passende Texte in der aktiven Liste bzw. im Archiv.
6. Erkenntnis archivieren und wieder aus dem Archiv zurückholen; dabei darf kein Text verloren gehen.
7. Löschen abbrechen: unveränderte Erkenntnis. Löschung bestätigen: Erkenntnis entfernt, zugehöriger Tagebucheintrag bleibt bestehen.
8. EKB2 nach einer Änderung exportieren, App auf separatem Testgerät installieren und wiederherstellen. Alle Texte, Archivzustände, IDs und Journal-Beleglinks müssen übereinstimmen.
9. EKB1-Altbackup importieren: Journal wird ersetzt, bestehende Erkenntnistexte bleiben erhalten; ungültige Journal-Links verschwinden.
10. Falsche Passphrase/kaputte Sicherung sowie fehlender Keystore-Schlüssel: keine scheinbar leeren Erfolge, keine vorhandenen Texte ungewollt überschreiben.
11. App-Sperre einschalten, Erkenntnis öffnen, App in Hintergrund bringen, erneut authentifizieren. Vor Entsperren darf kein privater Text sichtbar sein.
12. Android 200 % Schrift, schmale Gerätebreite, Rotation, Dark Mode, TalkBack: alle Texte/Buttons/Dialoge auch bei langer Erkenntnis erreichbar.

## Vor Merge noch zu überprüfen

Automatisierte JVM-/Compose-/Emulatortests und APK-Version müssen am jeweiligen Commit erfolgreich sein. Tatsächliche Android-Gerätetests mit importierter EKB2-Sicherung, ein separater Gerätewechsel sowie Sicherheits- und Zugänglichkeitsreview bleiben zusätzliche Freigabegates. Persönliche Erkenntnisse werden **nie** aus den M7.2-Musterhinweisen automatisch erzeugt.

**Ausbau in M7.3.3:** Redaktionell geprüfte freiwillige Reflexionsfragen, direkte Einladung aus Musterbeobachtungen, erweiterte Belegwahl und redaktionelle Abnahme.
