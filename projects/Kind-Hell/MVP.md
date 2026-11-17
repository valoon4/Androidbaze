# Kind Hell — v0.1 / erste testbare Fassung

## Hypothese
Ein offener, textbasierter Treffpunkt mit persönlicher Begrüßung erleichtert es neuen Menschen, spontan miteinander zu reden und freiwillig wiederzukommen.

## Erste vertikale Scheibe
1. Ein einfaches Profil (Alias, optionale Interessen, Sprache und gewünschte Kontakte); datensparsame Registrierung.
2. **Marktplatz:** ein öffentlicher Text-Feed mit kurzen Beiträgen und Antworten, ohne Themenzwang.
3. **Ankunft:** opt-in Vorstellungsrunde; neue Mitglieder können in Gruppen von ungefähr 20 begrüßt werden, auch wenn zunächst weniger da sind.
4. **Kompass light:** Profile nach Interessen / Freundschaft oder Austausch filtern; Zufallsentdeckung optional; noch kein komplexer Matching-Score.
5. Blockieren, Melden, grundlegende Missbrauchsabwehr und einfache, überprüfbare Moderationsentscheidungen.
6. Datenexport und verlässliche Backups als Infrastrukturziel.

## Anschlusskonzept: Verbindungen jenseits von Dating
Der Gründer möchte die frühere „Mitstreiter“-/ENM-Idee **in Kind Hell integrieren**. Für den Kompass sind zusätzliche **freiwillige, nicht-sensitive** Achsen (z. B. gemeinsame Aktivitäten, Mitgestaltung, Kontaktwünsche) als zukünftige Erweiterung vorgesehen. Beziehungsformen wie ENM/Polyamorie und intime Angaben sind **keine** MVP-Profileigenschaften und nicht standardmäßig öffentlich. Eine gesondert freigegebene Erwachsenenfunktion darf erst nach Alters-, Privacy-, Einwilligungs- und Moderationskonzept entstehen. Siehe [CONNECTIONS.md](CONNECTIONS.md); die bestehende v0.1-Pilotdefinition bleibt unverändert.

## Feste spätere Kompass-Anforderung (Gründerentscheidung 17.11.2026)
- **Drei separate Ja/Nein-Angaben:** `Neue Freundschaften gesucht?`, `Romantisch offen?` und `Sexuell offen?` (keine implizite Kopplung; alle acht Kombinationen sind legitim). Menschen dürfen nur Hookups oder nur eine romantische Partnerschaft suchen, ohne neue Freundschaften zu suchen.
- **Ziel:** Kontakte können die grundsätzliche Offenheit erkennen, statt Freundschaften durch Fragen zur Beziehungssituation zu belasten. Ein `Ja` ist **keine Einwilligung** und keine Aussage zum Beziehungsstatus.
- **Produktdesign:** Für eine **freiwillig genutzte, gesondert gesicherte 18+-Kontaktsuche** bewusst alle drei Werte setzen und den berechtigten Mitgliedern sichtbar machen; ohne Offenlegung bleibt die allgemeine Gildennutzung möglich. Keine unfreiwillige öffentliche Sichtbarkeit.
- **Semantik:** Freundschaft `Nein` = aktuell keine **neuen** Freundschaften gesucht; keine Abwertung oder Sperre der allgemeinen Gildenteilnahme. `Ja` bei Sex/Romantik = grundsätzliche Anfrageoffenheit, **keine** Einwilligung.
- **Abhängigkeit:** Altersprüfung, Einwilligung, Zugriffsschutz, Widerruf, Meldung/Blockierung, Datenschutzprüfung. **Nicht** v0.0.1 und **nicht** Teil des aktuellen v0.1-Pilotumfangs. [Ausführliche Spezifikation](CONNECTIONS.md).

## Bewusst nicht in v0.1
- Endlosscroll-Algorithmus, Gamification, aufwendiges KI-Matching
- Bilder-Uploads, Audio, Videostreams
- Erotische Profildaten oder Dating-spezifische Funktionen
- Bezahlschranken und Premium

## Test mit einer kleinen Pilotgruppe
Zielgröße: etwa 20 freiwillige Testpersonen (keine bestehende Mitgliederzahl behaupten).
Erfolgssignale: Anteil der Neulinge, die eine Antwort bekommen; echte Unterhaltungen über mehrere Tage; freiwillige Rückkehr; Rückmeldungen zu Sicherheit und Willkommenskultur.
Weder ein Match noch lange Bildschirmzeit wird als garantierter Glücksgewinn ausgegeben.

## Offene technische Entscheidungen
- Bestehenden Code in GitHub genauer prüfen; kein fertiger EK-Prototyp bestätigt.
- **Entschieden:** native Android-App (Kotlin); separate Buildstruktur unter `projects/Kind-Hell/android/` aufbauen.
- Authentifizierung, Datenhaltung, Hosting, Backup/Restore und Moderation minimal, aber belastbar konzipieren.
- Datenschutz und ggf. Altersgrenzen vor öffentlichem Launch überprüfen.

**Status (17.11.2026):** v0.0.1 Offline-Demo auf isoliertem Feature-Branch erstellt; GitHub Actions #44182891562 hat eine signierte Debug-APK erfolgreich gebaut und geprüft. Die v0.1-Mehrnutzer-MVP-Anforderungen (Accounts, Server, echte Posts, Moderation) bleiben offen. Kein Android-Gerätetest belegt.
