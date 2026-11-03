# M7.4.4 – UX-, Datenschutz- und Sicherheits-Qualitätsrunde für „Ausprobieren“

**Stand:** 03.11.2026 · **Status:** Implementierungs- und Test-Draft; **kein** behaupteter physischer Gerätetest und **kein** unabhängiges Security- oder Fachgutachten.

## Ziel und Produktentscheidung

M7.4.1–M7.4.3 sind in `main` integriert. Die freiwillige Funktion hat drei Einstiege: eigene Idee, kuratierte Strategiekarte und selbst formulierte Erkenntnis. M7.4.4 erweitert die Funktion **nicht** um zusätzliche automatische Empfehlungen, persönliche Analysen, Fortschrittsscores oder Benachrichtigungen. Stattdessen beseitigt die Runde konkrete UX-/Datenrisiken und prüft die bestehende Sicherheitsarchitektur vor M7.5.

**Grundprinzip:** Nicht-Ausprobieren, unklare Erfahrungen und unpassende Strategien sind ausdrücklich gültig. Aus einer kuratierten Karte wird niemals eine individuell passende oder wirksame Strategie abgeleitet.

## Gefundene Schwachstellen und umgesetzte Korrekturen

| ID | Ist-Problem | Änderung | Nachweis |
| --- | --- | --- | --- |
| U1 | Rückweg `Ausprobieren → Atlas-Karte → Atlasübersicht` verlor den ursprünglichen Quellbereich | Rückkehrziel bleibt bis zum ausdrücklich gewählten „Zurück zu Ausprobieren“ erhalten | Code-Diff, manuelle UX-Abnahme; direkte Komponenten-Regression bereits in M7.4.3 |
| U2 | Im eigenen Katalogentwurf nur allgemeiner Sicherheitshinweis statt **konkreter** Kartenbegrenzung | Originaltext aus `StrategyCard.limitations` samt zugänglicher Überschrift direkt im Entwurf, **vor** dem persönlichen Speichern | Android-Compose-Test |
| U3 | Lange Plan-/Grenzen-/Reflexionstexte blähten bis zu 300 Listen-Karten stark auf, das selbst notierte Warnfeld war nur im Editor sichtbar | Ab 180 Zeichen eine private, jederzeit aufklappbare Vorschau; Grenzen/Warntext auch in der Übersicht sichtbar; keine Textänderung | Android-Compose-Test bei 100 % und 200 % |
| U4 | Suche suchte nur Titel und Plan, nicht eigene Beobachtungen oder Warnungen | Volltextsuche über Titel, Plan, Grenzen und freiwillige Reflexion, ohne automatische Deutung | Android-Compose-Test |
| U5 | Kurzzeitig beschäftigter Erkenntnisspeicher sah wie ein Entschlüsselungsfehler aus und konnte einen noch nicht gespeicherten Entwurf zurücksetzen | „Wird geladen“ und „Sicheres Laden fehlgeschlagen“ getrennt; bei *kurzem* Busy Entwurf nur verbergen, nicht löschen; bei Lock/Restore/Fehler weiterhin konsequent im Speicher verwerfen | Android-Compose-Tests für Busy und erneutes Sperren |
| D1 | Ein früher ausgewählter Erkenntnisbezug kann während des Editierens gelöscht werden | Ungültige Links sichtbar melden, eigenes Entfernen anbieten, bis dahin Speichern blockieren; Texte bleiben erhalten | Android-Compose-Test |
| D2 | Ein während des Bearbeitens gelöschter Tagebuchbezug konnte aus einem alten Editor-Snapshot erneut an den Store weitergegeben werden | Die unveränderbaren Journalbezüge stammen beim Speichern aus dem aktuellen authentifizierten Bestand, nicht aus einem alten UI-Snapshot | ViewModel-Codeprüfung; vorhandene EKB3-Referenztests; manueller Lösch-/Edit-Race offen |
| D3 | Die ursprüngliche Strategie-Katalog-ID ließ sich auf Domänenebene bei einem Update verändern | Quellidentität im `StrategyExperimentChanges.save()` als unveränderlich validiert, ohne freie eigene Texte/Status zu beschränken | JVM-Unit-Test |
| S1 | Parallel gestarteter EKB3-Import und Check-in-Speichern waren bisher nicht explizit als integrierter Race-Test belegt | Android-Keystore-Stresstest: Import mit bestehenden verlinkten Erkenntnissen und Experimenten konkurriert mit neuem Check-in. Nur zwei wohldefinierte serielle Endzustände zulässig, anschließend neuer EKB3-Roundtrip | Android-Integrationsprüfung |
| F1 | Die zweite `patterns`-Erklärungskarte wurde nicht separat gegen aktives Vormerken automatisiert geprüft | Zusätzlicher Android-UI-Test zur Emotionsunterdrückung-Karte | Android-Compose-Test |

**Datenschutzcheck im Code:** Private Strategieentwürfe, Suchtexte und Link-Auswahlen bleiben `remember`-Zustand, nicht `rememberSaveable`/Android-Bundle. `FLAG_SECURE` deckt den Rückblick ab. EKS1 bleibt Keystore-AES-GCM-verschlüsselt; EKB3 enthält alle drei privaten Bereiche und verlangt eine Passphrase. Ein unvollständig entschlüsselter Bestand darf nicht als leere Liste erscheinen. App-Sperre entfernt entschlüsselte Zustände; kurzzeitig beschäftigte Stores dürfen keine unerwarteten Schreibvorgänge auslösen. Dies ist eine **Code- und automatische Pfadprüfung**, kein externer Security-Audit.

## Automatische Abnahme / CI

Der PR erfordert auf **dem abschließenden Commit**:

- Kotlin/JVM-Domain- und Format-Tests, einschließlich unveränderbarer Strategie-Quellidentität.
- **33 Compose-UI-Tests** bei 100 % und bei 200 % Schrift = **66 Ausführungen**; darunter 7 neue M7.4.4-Fälle zu Grenztexten, Langtexten, Suche, verwaisten Links, transientem Laden, erneuter Sperre und zweiter Erklärungskarte.
- 3 bestehende EKB2-/AndroidKeyStore-Tests.
- **9** EKB3-/AndroidKeyStore-Tests (vorher 8), insbesondere die neue parallele Import-/Check-in-Regressionsprüfung.
- Signierten, installierbaren Debug-Build und erfolgreiche Identitäts-/Prüfsummenprüfung.

**Nicht als bestanden eintragen, bevor die GitHub-CI auf genau diesem Commit grün ist.**

## Verpflichtende manuelle Abnahme auf Android

**Nur fiktive Daten und am besten ein separates Testgerät verwenden.** Nicht eine echte Tagebuchinstallation durch Testbackups ersetzen, ohne vorher den Bestand und die Passphrase sicher zu sichern.

| Gate | Durchführung und erwartetes Ergebnis | Status |
| --- | --- | --- |
| M1 – 320 dp / 200 % | Strategieatlas → Karte „Eigene Grenzen ausdrücken“ → Vorschlag: konkrete Warnung sichtbar, danach Form und Abbrechen/Speichern erreichbar. Auch Querformat/Dark Mode | **offen** |
| M2 – TalkBack | Rückblick-Chips, Status, Warnüberschrift, Quellenbezüge, Dialog schließen, Bestätigungsbuttons, lange Notizen; klare Bezeichnungen und Fokusreihenfolge | **offen** |
| M3 – Rücknavigation | Ausprobieren → Atlasliste → Strategiekarte → Atlasliste → „Zurück zu Ausprobieren“; danach erneut zu gespeicherter Karte und zu Wissensartikel, ohne Kontextverlust | **offen** |
| M4 – Entwürfe | Frei schreiben, nicht speichern, abbrechen; kurzzeitig Erkenntnisspeicher beschäftigen; neue und bestehende persönliche Texte dürfen nicht still verschwinden. App-Sperre dagegen muss ungespeicherte Texte entfernen | **offen** |
| M5 – Lange Texte | Fiktive 800/800/1200-Zeichen-Idee speichern; suchen nach Sicherheitswort, Ausklappen/Rückklappen und Bearbeiten ohne Trunkierung | **offen** |
| M6 – Verknüpfungen | Fiktive Erkenntnis ausdrücklich verknüpfen, anschließend verknüpfte Erkenntnis löschen; eigener Plan/Status bleibt bestehen, ungültige Referenzen werden nicht still zurückgespeichert | **offen** |
| M7 – Kein Handlungsdruck | „Bewusst nicht ausprobiert“, „Für mich nicht passend“ und „Noch unklar“ frei wählen, keine Erfolgswertung, keine automatische Frist | **offen** |
| M8 – Zwei physische Geräte | EKB3 mit eigenen fiktiven Texten/Links/Archiv, auf anderes **physisches** Android-Gerät importieren; EKB1/EKB2-Legacy-Import erhält bereits gespeicherte Strategieideen | **offen** |
| M9 – Beschädigte Daten | Falsche Passphrase, manipulierter EKB3-Export und beschädigter EKS1-Store: keine unbemerkte Teilersetzung, kein scheinbar leerer Bestand | **automatisch abgedeckt; physisch offen** |
| M10 – Redaktionsreview | Alle 28 freigegebenen Karten unabhängig auf Grenzen, Risiken, suggestive Begriffe und ungeeignete Handlungsaufforderungen prüfen; zwei `patterns`-Erklärungskarten ohne CTA | **offen** |
| M11 – Security-Review | Unabhängige Prüfung der EKS1-/EKB3-/EKR2-Kryptografie, App-Lock-Garantien, Geräte-Wechsel, Restore-Replay und Datenkorruption | **offen** |

## Entscheidung nach dieser Runde

- **M7.4.4 Code-Exit:** CI vollständig grün, diff geprüft, separate Nutzerfreigabe für den Merge von PR #37.
- **M7.4.4 Abnahme-Exit:** Nachvollziehbar protokollierte M1–M9-Gerätetests; offene M10/M11-Gates in M7.6 **sichtbar offen** halten. Weder ein grüner Emulatorlauf noch der Nutzer-Merge allein belegt zwei echte Geräte oder eine externe Sicherheitsprüfung.
- **Danach:** M7.5.0 – Nutzenentscheidung zu freiwilligen Visualisierungen anhand fiktiver Beispiele. Keine Charts bauen, bevor echte Verständlichkeitsvorteile festgestellt wurden.

## Verweise

- [M7-Abschlussplan vor M8](M7_ABSCHLUSSPLAN_VOR_M8.md)
- [M7.4.0 UX- und Funktionskonzept](M740_STRATEGIEEXPERIMENTE_UX_KONZEPT.md)
- [M7.4.3 Testanleitung und Grenzen](M743_STRATEGIEATLAS_ERKENNTNISSE_VERKNUEPFUNG_ABNAHME.md)
- [EKS1/EKB3/EKR2 Sicherheits-ADR](adr/ADR_M740_STRATEGIEEXPERIMENTE_DATEN_UND_BACKUP.md)
