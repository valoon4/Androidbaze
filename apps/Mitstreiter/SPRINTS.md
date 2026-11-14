# MITSTREITER — Sprintplan (Entwurf, 14.11.2026)

**Status:** Planung; noch keine App gebaut. **Projekt:** ENM-freundliche Begegnungs-App / Community. Arbeitsordner: `valoon4/Androidbaze/apps/Mitstreiter/`. Konzeptquelle: privates `valoon4/Psychologicalz/ideen/mitstreiter-community-produktidee-2026-11-13.md` — keine privaten Geschichten/Namen in dieses Repo kopieren.

## Ziele und Grenzen

- Ziel: gemeinsam eine Android-App iterativ gestalten, früh als **installierbare Debug-APK** testen.
- Produktkern: Menschen finden, die gemeinsam Interessen, Freundschaft und ggf. freiwillig Romantik/Erotik erkunden möchten; ENM als Option, nicht Voraussetzung für alle Nutzer.
- Beziehungs-/ENM-Informationen standardmäßig privat, nur bewusst und selektiv teilbar.
- Im Prototyp: lokale Demo-/Testdaten und Simulationen sind **klar als solche gekennzeichnet**, keine Behauptung echter Matches oder Chats.
- Sicherheitsminimum für spätere Online-Funktionen: 18+, Consent, Melden/Blockieren, Datensparsamkeit, kontrollierte Sichtbarkeit. Ein einfacher Altersbutton ist keine wirksame Altersverifikation.
- Kein produktiver Austausch intimer Informationen oder öffentliche Registrierung, bevor Backend, Authentifizierung, Moderation, Alters- und Datenschutzkonzept tragfähig sind.
- **Buildstrategie:** die vorhandene Androidbaze-Root-App nicht unbemerkt ersetzen; Mitstreiter als separaten App-Prototyp aufbauen. APK-Build über eigenen Buildpfad/Workflow, sobald technisch eingerichtet.

## Sprints (vorläufige Reihenfolge; Ziel, nicht IST)

| Sprint | Ziel | Abnahme/Demo |
|---|---|---|
| 0 — Gemeinsam definieren | Design, Navigation, Zielgruppe, Funktionsumfang und MVP-Grenze **zusammen** festlegen | Abgenommene kompakte Feature-/Screenliste und grober Stil |
| 1 — Erste APK | Eigenständiges Android-Grundgerüst mit Startscreen und Navigation | Debug-APK installierbar, navigierbar |
| 2 — Profil | Profil anlegen/bearbeiten, Interessen und Verbindungswünsche, Sichtbarkeitssteuerung (lokale Demo) | Einstellungen bleiben beim Neustart erhalten |
| 3 — Entdecken | Profilkarten, Filter und gegenseitige Interessen (lokale simulierte Profile) | Demo-Matches/Kompatibilität nachvollziehbar, keine fiktiven echten Nutzer |
| 4 — Kontakt | Kontaktanfrage und Demo-Chat (lokale Simulation), klare Grenzen | Nutzer kann Kontakt annehmen/ablehnen und Demo-Konversation testen |
| 5 — Vertrauen & Qualität | Privatsphäre-Flow, Block-/Melde-UI, Daten löschen, Accessibility und Fehlerbehandlung | Abnahmetests, neue Debug-APK |
| 6 — Online-Pilot (nur nach Freigabe) | Backend, echte Accounts, Moderation, rechtliche/Alters-/Sicherheitsprüfung | Separat definierter geschützter Test; keine automatische Freischaltung |

## Sprint 0 — Aufgaben

| ID | Task | Status |
|---|---|---|
| S0-01 | Design und Funktionalitäten **gemeinsam** festlegen: gewünschter Look, Screens, Matching-/Kontaktlogik, Sichtbarkeit, Prioritäten | ⬜ Offen |
| S0-02 | Aus S0-01 ein Minimal-MVP und bewusst zurückgestellte Funktionen abgrenzen | ⬜ Offen |
| S0-03 | App-Name und Icon-Richtung entscheiden (Arbeitstitel MITSTREITER) | ⬜ Offen |
| S0-04 | Technischen Weg für **separate** Debug-APK und Repo-Struktur festlegen | ⬜ Offen |
| S0-05 | Erste konkrete Akzeptanzkriterien und Testablauf für Sprint 1 formulieren | ⬜ Offen |

## Definition of Done für jeden Build-Sprint

- Quellcode versioniert; bestehende Androidbaze-Projekte nicht versehentlich beschädigt.
- Debug-APK tatsächlich gebaut und verfügbar, nicht bloß angekündigt.
- Grundfunktionen auf Start-/Rücknavigation und Neustart geprüft; bekannte Fehler sichtbar dokumentiert.
- Keine Demo-Funktion als echte Online-Funktion ausgeben.
- Nach jeder APK Nutzerfeedback aufnehmen und den nächsten kleinen Sprint konkretisieren.

**Nächste gemeinsame Aktion:** S0-01 — Design + Funktionen besprechen. Keine vorweggenommenen Designentscheidungen als bestätigt markieren.
