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
