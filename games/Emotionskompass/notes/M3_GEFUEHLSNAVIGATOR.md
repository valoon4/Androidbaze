# M3 – geführter Gefühlsnavigator

**Stand:** 29.10.2026 · **Version:** 0.3.0-m3 · **Entwicklungsbranch:** feature/emotionskompass-m3-guided-checkin

## Produktziel

Nutzer sollen Gefühle mit möglichst wenig Druck wahrnehmen, einen oder mehrere passende Begriffe finden, die Intensität optional beschreiben und ihren Moment in einer stillen Zusammenfassung betrachten können. M3 folgt auf den umfassenden Offline-Atlas M2.2 und ergänzt bewusst **noch keine dauerhafte persönliche Datenspeicherung**.

## Interaktionsablauf

1. **Ankommen:** bewusst wahrnehmen; freiwillig Wärme, Enge, Anspannung, Leichtigkeit, Unruhe, Entspannung, Müdigkeit oder anderes wählen.
2. **Benennen:** lokal 289 Atlasbegriffe durchsuchen; Familienfilter und 0–n Mehrfachauswahl; keine Entscheidung erzwingen.
3. **Nachspüren:** für jeden gewählten Begriff eine optionale Intensität 0–10; leeres Feld bleibt explizit *ohne Angabe*. Optional Lebensbereich und bis zu 500 Zeichen Freitext.
4. **Ausklang:** ausschließlich deskriptive Zusammenfassung, keine Deutung. *Neuer Check-in* verwirft die bisherige Auswahl. *Beenden* entfernt den Entwurf aus der App-Navigation.

Jeder Schritt enthält die Information, dass keine Daten gespeichert werden. Zurücknavigation durchläuft die Schritte. Abbruch über die eigene Aktion „Verwerfen“ oder Android-Zurück auf der ersten Seite setzt das Session-Modell zurück. Die Bottom-Navigation und Einstellungen sind während des Check-ins nicht verfügbar, um versehentliches Verlassen ohne Löschen zu vermeiden. Tabwechsel-Operationen im ViewModel löschen einen eventuell vorhandenen Entwurf ebenfalls.

## Datenschutz und Modellgrenzen

- `CheckInDraft` ist eine nicht serialisierte Kotlin-Datenklasse. `AppViewModel` hält einen `StateFlow<CheckInDraft>` nur während der aktiven Navigation.
- Kein `rememberSaveable`, keine DataStore-/Room-Schreibzugriffe für persönliche Angaben, keine Hintergrundjobs, keine Cloud, keine Netzwerkberechtigung und kein Emotions-Tracking.
- Trotz dieser Absicht sind M3-Daten nur ein **temporärer Entwurf, kein verschlüsselter Datenspeicher**. Sie gehen verloren, wenn die App geschlossen oder der Prozess beendet wird. M4 entwirft und implementiert geschützte dauerhafte Speicherung und Löschfunktionen gesondert.
- Keine Diagnostik, keine automatisierte psychologische Auswertung, keine verpflichtende Frage oder Werteskala.
- Die Atlas-Taxonomie ist ein redaktioneller Lernvorschlag, keine wissenschaftlich vollständige Klassifikation.

## Automatisierte Tests

- Leerer Start ohne persönliche Daten.
- Mehrfachselektion, Toggle-Auswahl, Null-Intensität ≠ 0.
- Gültiger Wertebereich 0–10 und Nichtauswählbarkeit unbekannter IDs.
- Alle vier Schritte funktionieren auch mit null Gefühlsbegriffen.
- Reversible Körperhinweise, freiwilliger Kontext und Freitext-Grenze.
- Atlasdetail kann mit vorgewähltem Begriff starten.
- Nach Schließen/Abbruch/Tabwechsel ist das komplette aktive Entwurfsmodell leer.
- Bestehende Atlas-Tests für 289 Wörter, 41 Vergleiche und 14 Lernpfade.
- GitHub Actions führt JVM-Tests, APK-Build und APK-Signaturprüfung aus.

## Manuelle Android-Abnahme

1. Onboarding beenden, auf Start den Navigator öffnen, Schritte ohne Eingaben durchlaufen.
2. Navigator erneut öffnen, „Freude“ und „Unsicherheit“ auswählen, nur einem Gefühl Intensität 0 und dem anderen 8 geben.
3. Familien- und Suchfilter testen, ausgewählten Begriff wieder entfernen.
4. Körperempfindungen, Lebensbereich und Freitext auswählen; Zusammenfassung auf richtige Inhalte prüfen.
5. Android-Zurück von der Zusammenfassung, Inhalte dürfen noch im aktiven Check-in sein.
6. „Beenden und Angaben verwerfen“; erneut öffnen – alle persönlichen Felder müssen leer sein.
7. Atlasdetail „Wehmut“ öffnen, dort „Über meine Erfahrung nachdenken“ wählen – Wehmut ist vorgewählt.
8. Dark Mode, große Schrift, TalkBack, Bildschirmdrehung und Flugmodus testen.
9. Tagebuch und Rückblick bleiben verständlich als **noch nicht aktiv** gekennzeichnet.

**Nicht Teil von M3:** Verschlüsseltes Journal, Export, Stimmungsstatistik, Erinnerungen, automatisierte Diagnosen. Diese Punkte werden erst später implementiert.
