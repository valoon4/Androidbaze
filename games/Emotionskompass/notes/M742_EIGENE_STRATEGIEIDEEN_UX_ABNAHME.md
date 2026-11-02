# M7.4.2 – Eigene Strategieideen: Bedienung und Abnahme

**Stand:** 2026-11-02. **Status:** Umsetzung in eigenem Draft-PR #35, noch nicht gemergt.

## Ziel und Abgrenzung

Der freiwillige Bereich **Rückblick → Ausprobieren** ergänzt „Übersicht & Muster“ und „Meine Erkenntnisse“. Man kann persönliche Handlungsoptionen selbst formulieren, ausdrücklich speichern, ihren Status ändern und freiwillig Erfahrungen dazu festhalten.

**M7.4.2 deckt die selbst formulierten Ideen ab.** Der direkte opt-in-Einstieg über geeignete Karten aus dem bestehenden Strategieatlas, kuratierte Warn-/Grenztexte und optionale ID-Verknüpfungen zu Erkenntnissen sind bewusst der gesonderten Etappe **M7.4.3** vorbehalten. Es werden keine automatischen Empfehlungen aus Bedürfnissen, Gefühlen, persönlichen Texten oder Rückblickmustern generiert.

### Nutzerwege

1. Rückblick öffnen, **„Ausprobieren“** auswählen. Auch ohne bestehende Tagebucheinträge ist eine eigene Idee möglich, wenn der verschlüsselte Bestand sicher geladen wurde.
2. **„Eigene Idee notieren“** antippen. Pflichtfeld „Meine Idee“ (max. 140 Zeichen), zusätzliche freiwillige Felder zum Plan (800), Grenzen/Vorsicht (800) und zur persönlichen Beobachtung (1200 Zeichen).
3. Status frei wählen: **Offen**, **Vorgemerkt**, **Ausprobiert**, **Bewusst nicht ausprobiert**, **Abgeschlossen**. Kein Streak, keine Bewertung oder zeitliche Verpflichtung.
4. Einschätzung freiwillig wählen oder offenlassen: **Keine Einschätzung**, **Hilfreich**, **Teilweise**, **Für mich nicht passend**, **Noch unklar**. „Nicht ausprobiert“ und „Noch unklar“ bleiben gültige, nicht beschämende Ergebnisse.
5. Im Dialog führt **„Abbrechen“** zu einer echten **Verwerfen-Bestätigung**. „Ausdrücklich speichern“ ist der **einzige** neue persistente Schreibauslöser. Anschließend kann die Idee gesucht, überarbeitet, archiviert, wiederhergestellt oder nach zweiter Bestätigung gelöscht werden.
6. Alle Inhalte werden ausschließlich lokal über den gemeinsamen M7.4.1-EKS1/Keystore-Koordinator gespeichert, sind Bestandteil des EKB3-Exports, und werden bei App-Sperre/fehlgeschlagener Wiederherstellung nicht angezeigt.

### UX- und Datenschutzeigenschaften

- Nur echte persönliche Eingaben und ausdrücklich gewählte Statuswerte werden gespeichert. Die App interpretiert nicht, ob eine Idee wirksam war.
- Keine neuen App-Berechtigungen, kein Netzverkehr und keine externen Datenflüsse.
- Private Editorentwürfe, Suchwörter und ausgewählte Einträge verwenden Compose **remember**, nicht **rememberSaveable** oder Android SavedState.
- Bei Ladefehlern: **kein** Erstellen, Editieren oder Anzeigen früher entschlüsselter persönlicher Ideen; sichtbarer „Erneut sicher laden“-Weg.
- Bei Journal-/Erkenntnislöschung: EKR2 entfernt verwaiste Referenz-IDs, nicht eigene Strategietexte. ViewModel aktualisiert die angezeigte Strategiesammlung oder sperrt bei Unklarheit den Zugriff.
- Im Rückblick passen drei Bereichs-Chips via FlowRow in mehrere Zeilen. Editor sowie Liste sind vertikal scrollbar, einschließlich bei 320 dp und 200 % Schrift.
- Bei 300 Ideen blockiert die App weitere neue Ideen; bestehende lassen sich noch bearbeiten oder nach expliziter Bestätigung löschen.
- **Sicherheitsgrenze aus M7.4.1:** Der UI-Schutz durch freiwillige Geräte-Anmeldung ist keine per Zugriff erzwungene Keystore-Benutzerauthentifizierung. Keine stärkere Sicherheit behaupten.

## Prüfanleitung auf Android (nur fiktive Daten)

1. Rückblick öffnen und zwischen „Übersicht & Muster“, „Meine Erkenntnisse“ und „Ausprobieren“ wechseln. Alle drei Bereiche müssen auf kleinem Display bei 200 % Schrift lesbar erreichbar sein.
2. Eine fiktive Idee eingeben, **Abbrechen → Entwurf verwerfen** wählen. Nach erneutem Öffnen darf sie nicht gespeichert sein.
3. Wiederholen, „Bewusst nicht ausprobiert“ und „Keine Einschätzung“ wählen, ausdrücklich speichern. Die Idee muss ohne Punkte/Erfolgsmarkierung erscheinen.
4. Die Idee erneut öffnen, Status „Ausprobiert“, Einschätzung „Noch unklar“ und einen freiwilligen eigenen Satz eingeben, ausdrücklich speichern. Text und Auswahl müssen unverändert erscheinen.
5. Dieselbe Idee archivieren, über Archiv wieder öffnen, zurückholen und gezielt nach zweiter Bestätigung löschen. Tagebucheinträge und Erkenntnisse bleiben unverändert.
6. Mehrere Ideen anlegen, Suche testen. Nach App-Neustart (und, falls aktiviert, erneuter Geräte-Anmeldung) müssen die gespeicherten Ideen wieder erscheinen.
7. EKB3 mit mindestens einer Idee sichern, in **isolierter Testinstallation** wiederherstellen und Inhalt/Status/Archivierung kontrollieren. Alte EKB1/EKB2-Dateien dürfen bereits vorhandene Ideen nicht still löschen.
8. Mit **fiktivem** EKB3-Testpaket aus M7.4.1 (3 Beispielideen) überprüfen, ob diese nach dem Import in „Ausprobieren“ sichtbar sind.
9. App sperren, in den Hintergrund schicken, wieder öffnen; Text eines offenen Entwurfs darf bei aktiver Sperre nicht ohne erneute Anmeldung sichtbar bleiben.
10. TalkBack, System-Zurück, Rotation, Dark Mode sowie 320 dp/200 % Schrift manuell prüfen. Keine versteckten Schaltflächen oder unbeabsichtigten Speicheraktionen.

### Automatische Regression

- Bestehende EKB2- und EKB3-/EKR1-Emulator- und JVM-Tests laufen weiter.
- 18 echte Compose-UI-Tests bei 100 % und 200 % Schriftgröße = 36 UI-Testläufe, darunter vier neue Tests für Abbruch, bewusstes Nicht-Ausprobieren, spätere Reflexion, Archiv/Löschen und Fehlerzustände.
- 3 vorhandene EKB2- und 8 EKB3-/Keystore-Integrationstests bleiben Teil des Emulator-Gates.
- APK-Build/Signaturprüfung müssen auf dem finalen PR-Commit erfolgreich sein.

## Risiken und offene Folgeetappen

- **M7.4.3**: Katalog-Anbindung mit reinen Aufklärungskarten ohne Experiment-CTA, optionaler freier Erkenntnisbezug, sichere Rücknavigation.
- **M7.4.4/M7.6**: unabhängiger Sicherheits-/Fachreview, Restore auf zwei physisch getrennten Geräten, manuelle TalkBack-/Geräteabnahme.
- Fehlende Sicherheitsnachweise werden nicht durch erfolgreiches CI ersetzt. PR #35 bleibt bis zur ausdrücklichen Nutzerfreigabe ein Draft; keine automatische Zusammenführung.
