# ADR M7.4.0 – Eigene Strategieexperimente, verschlüsselte Speicherung und vollständige Sicherung

**Datum:** 2026-11-01  
**Status:** *Proposed* – Umsetzung ausdrücklich **nicht** freigegeben; vor M7.4.1 prüfen und separat genehmigen.  
**Betroffene Teile:** M7.4, Journal EKJ1, Erkenntnisse EKI1, Backup EKB1/EKB2, Wiederherstellungsauftrag EKR1, App-Sperre.

## 1. Verifizierte Ausgangslage

- `EncryptedJournalRepository` und `EncryptedPersonalInsightRepository` halten **zwei getrennte**, per Android Keystore AES-GCM/AtomicFile geschützte Bestände.
- `InsightBundleBackup` ist **EKB2**: passphrasenverschlüsselter Export, `EKB2`-Magic, versionierter `InsightBundleFormat`-Klartext mit exakt `journal` und `insights`. Die `decode`-Methode fordert ausdrücklich `version == 2`; die Erweiterung durch ein neues Feld ohne Formatversionierung ist **nicht** sicher.
- `InsightBundleBackup.open` erkennt auch **EKB1**; fehlende Erkenntnisse haben in diesem Fall die Semantik **„nicht enthalten“**, ausdrücklich **nicht** „leere Liste“.
- `CombinedBackupRepository` koordiniert EKB2-Import und EKR1-Replay für **zwei** lokale Stores; `RestoreTransactionRunner` schreibt beide Ziele erst nach dauerhafter verschlüsselter Absicht.
- **Zusätzliche Lücke, vor einer Erweiterung zu schließen:** `AppViewModel.saveCheckIn` ruft derzeit `store.save` direkt auf, also **außerhalb** des `@Synchronized`-Koordinators. Bei einem dritten Store darf eine parallele Speicheroperation einen Export/Restore nicht inkonsistent machen.
- Die optionale Gerätesperre ist ein UI-Gate; der vorhandene Restore-Key verlangt technisch **keine** Benutzer-Authentifizierung am Keystore. Daraus keine stärkere, hardwaregebundene Entsperrgarantie ableiten. Sicherheitsüberprüfung bleibt erforderlich.

**Folgerung:** Eine eigenständige Strategieexperiment-Datei oder ein neuer Status darf **nicht vor** einer vollständigen Backup-/Restore-/Koordinationslösung produktiv gespeichert werden.

## 2. Entscheidung A – Schlankes, explizites Domänenmodell

Vorgesehener reiner, begrenzter Datentyp `StrategyExperiment` (Name nur Konzept, noch keine Kotlin-Implementierung):

| Feld | Typ / Regel | Warum |
| --- | --- | --- |
| `id` | UUID, beim ersten ausdrücklich bestätigten Speichern erzeugt | Idempotenz, Edit/Delete |
| `createdAtUtcMillis`, `updatedAtUtcMillis` | UTC und monotone Änderungszeit | Ordnung ohne fragwürdiges Ereignisdatum |
| `sourceStrategyId` **oder** `customTitle` | genau eine Quelle: valide kuratierte Strategie-ID **oder** eigene Kurzbezeichnung bis 140 Zeichen | kein automatisch erschlossener Strategievorschlag |
| `sourceTitleFallback` | optionaler kuratierter Titel zur Anzeige, falls die ID in späteren App-Versionen fehlt; nie als neue Identität behandeln | Daten bleiben verständlich, ohne falsches Matching |
| `planText`, `cautionText` | freiwillige eigene Texte, je bis 800 Zeichen | individueller Kontext, keine Pflicht |
| `status` | `OPEN`, `SAVED_FOR_LATER`, `TRIED`, `NOT_TRIED`, `CLOSED` | tatsächliche eigene Entscheidung, kein Score |
| `evaluation` | optional: `HELPFUL`, `PARTLY`, `NOT_FITTING`, `UNCLEAR`; **null** als „keine Einschätzung“ | offen/uneindeutig sind gültig |
| `reflectionText` | optional bis 1200 Zeichen, ausschließlich selbst geschrieben | keine generierte Wirkungsaussage |
| `archived` | boolescher Sichtbarkeitsstatus | sanftes Ordnen ohne Datenlöschung |
| `relatedInsightIds`, `relatedJournalIds` | optionale ausdrücklich ausgewählte, bereits existierende IDs, jeweils maximal 12; `Set` ohne Duplikate | nachvollziehbare Bezüge ohne Auto-Verknüpfung |

**Obergrenze als Startwert für M7.4.1:** maximal 300 eigene Ideen; Größenlimits sind vor Code mit dem gesamten Backupbudget und 3000 Journalen abzugleichen. Beim Überschreiten: verständlich ablehnen, **niemals** Daten kürzen oder verwerfen. Statusänderungen erhalten `id` und `createdAt`. Wiederholtes Speichern mit derselben ID überschreibt nur den gezielt bearbeiteten Datensatz. Löschung nur nach Bestätigung.

**Validierung:** Zeitstempel, eindeutige IDs und maximale Gesamtlänge vor jeder Änderung prüfen. Kuratierte IDs werden gegen den gebündelten `StrategyCatalog` validiert; beim Restore unbekannte, *formal valide* IDs erhalten, aber als nicht auffindbar markieren, nicht durch Similarity-Matching ersetzen. Links auf nicht existente eigene Journal-/Erkenntnis-IDs werden ausschließlich gemäß explizit bestätigtem Import-/Löschvertrag behandelt.

## 3. Entscheidung B – Verschlüsselte lokale Speicherung

**Vorgeschlagener dritter Store `EKS1`:** eine eigene versionierte und gebundene `AtomicFile`-Datei; AES-256-GCM via Android Keystore mit separatem Zweck und Schlüsselalias, eindeutige AAD/Magic, zufälliger Nonce, klare Größen- und Anzahlgrenzen. Keine Klartextdatei, kein Room-/DataStore-Index, kein Android Auto Backup, kein Logging privater Inhalte. Alle Lese-, Speicher-, Archiv-, Lösch- und Exportoperationen laufen unter authentifiziertem App-Zugriff.

Die Daten werden nur nach erfolgreicher **vollständiger Authentifizierung aller vorhandenen privaten Bereiche** als geladen gekennzeichnet. Ein fehlender Schlüssel, ein GCM-Fehler oder ein unvollständiger Restore zeigt Fehler und **keine scheinbar leere Strategie- oder Erkenntnissammlung**. Sperren/Hintergrundwechsel entfernen alle entschlüsselten Zustände und Entwürfe gemäß vorhandenem Access-Epoch-Vertrag. Das System-UI-Gate bleibt klar von Android-Keystore-Benutzerauthentifizierung getrennt.

## 4. Entscheidung C – Ein **neues** vollständiges Portable-Backup `EKB3`

**Warum EKB3?** Die bestehenden EKB2-Payloads sind strikt versioniert und enthalten keine Strategieexperimente. Eine alte Datei mit „kein Experimentbereich“ darf keinesfalls wie „alle Experimente löschen“ interpretiert werden.

**Exportvertrag für neue Versionen:**

- Eigene Magic `EKB3`, eigene AEAD-AAD `portable-backup/v3`, neues versioniertes Payloadschema `version: 3` mit vollständig getrennten Bereichen `journal`, `insights` und `strategyExperiments` (letzterer **immer vorhanden**, kann explizit leer sein).
- Passwortbasierter Schlüssel mit geprüftem, ausreichend starkem KDF, eindeutigen Salt/Nonce pro Export, integrierte Manipulationserkennung; beim Umstieg auf die bestehende PBKDF2-Variante die Kosten und Geräteleistung erneut bewerten. Keine schwächere Verschlüsselung als EKB2.
- Gesamte Eingabegrößen, Anzahl von Datensätzen und referenzierte IDs werden **vor** dem Überschreiben vollständig geprüft. Keine implizite Null-Ersatzliste.
- Export darf erst nach erfolgreichem Laden und vollständiger Wiederherstellung aus einer eventuell noch anstehenden Redo-Absicht beginnen; alle drei Datensammlungen müssen aus einem koordinierten konsistenten Zustand stammen.
- Im Dateidialog unmissverständlich erläutern, dass diese Datei **alle drei** privaten Bereiche enthält und ohne Passwort nicht wiederhergestellt werden kann. Auch der bisherige Dateiname darf keine unverschlüsselte Nutzerinformation enthalten.

**Import- und Kompatibilitätsmatrix:**

| Eingehende Datei | Enthaltene Bereiche | Verhalten bei bestehendem Experimentbestand |
| --- | --- | --- |
| EKB1 | Nur Journal | Vorhandene Erkenntnisse **und** Strategieexperimente behalten. Ungültig gewordene optionale Journalbezüge nach sichtbarem Hinweis entfernen, nie deren eigene Texte. |
| EKB2 | Journal + Erkenntnisse | Diese beiden Bereiche gemäß **expliziter** Ersetzungsbestätigung übernehmen; Strategieexperimente behalten. Ungültige optionale Bezüge auf ersetzte Journal-/Erkenntnis-IDs nach sichtbarem Hinweis lösen. |
| EKB3 | Journal + Erkenntnisse + Experimente | Alle drei Bereiche **nur nach ausdrücklicher Warnung über die vollständige Ersetzung** übernehmen, auch wenn die neu importierte Experimentliste ausdrücklich leer ist. |
| Unbekannt / beschädigt / falsche Passphrase | Nicht vertrauenswürdig | **Keine** Datenänderung, kein scheinbar erfolgreicher Teilimport. |

**Ein fehlender Bereich ist nicht gleich einer leeren Liste.** Vor Bestätigung muss eine lokal entschlüsselte Vorschau zumindest Formatversion und Anzahl aller enthaltenen bzw. nicht enthaltenen Datenbereiche nennen; keine privaten Texte in Logs oder System-Bundles. Der bisherige Import-Dialog muss entsprechend überarbeitet werden. Ein Import mit potenziell gelösten Beziehungen braucht einen gesonderten klaren Hinweis; eigene Versuchstexte bleiben unangetastet.

**Downgrade-Grenze:** Ältere App-Versionen können ein neues EKB3-Format nicht öffnen. Die neue App muss daher verständlich „Sicherung aus neuerer Version“ erklären. Keine stillen „kompatiblen“ EKB2-Exporte, die Strategieexperimente weglassen; falls optionaler Legacy-Export einmal nötig ist, separat und sichtbar als unvollständig kennzeichnen.

## 5. Entscheidung D – Absturzsichere Koordination von **drei** Stores

Ein `@Synchronized`-Block allein kann **keine** atomare Transaktion über drei Dateisystem-Dateien garantieren.

- Vor dem ersten Write eine **vollständige, authentifizierte Zielmenge** aller drei Bereiche als separaten Keystore-verschlüsselten, atomar abgelegten Redo-Intent `EKR2` aufzeichnen. Nach Unterbrechung denselben vollständigen Zustand idempotent wiederholen, dann erst Absicht entfernen. Nie einen zweiten Intent über einen noch offenen schreiben.
- **Altlast-Migration:** Existiert auf einem Gerät bereits eine EKR1-Absicht, muss **vor** EKR2-Operationen zuerst `CombinedBackupRepository.recoverPending()` aus dem alten, gültigen Format erfolgen. Erst danach die drei authentifizierten Stores laden und gegebenenfalls einen neuen EKR2-Vorgang starten. Beschädigte EKR1/EKR2-Absichten blockieren neue Writes statt sie zu ignorieren.
- Journal- und Erkenntnisänderungen, die Links in Strategieexperimenten beeinflussen, müssen mit dem dritten Store unter **derselben** Transaktionsgrenze laufen. Speziell `AppViewModel.saveCheckIn` und alle weiteren direkten Journal-Schreibwege sind in der koordinierten Schreibschicht zu konsolidieren. Bestehende API-Tests verifizieren das, bevor eine neue Persistenzfunktion aktiviert wird.
- Bei reinen Änderungen in der Experimentdatei ist deren `AtomicFile` allein ausreichend, **sofern** sie aus demselben globalen Koordinator kommen und zuerst jede Alt-/Neu-Absicht abgearbeitet wird. Bei bereichsübergreifenden Änderungen ist die EKR2-Absicht Pflicht.
- Der Merge in den ViewModel-State erfolgt erst nach bestätigtem erfolgreichem persistentem Ergebnis; ein fehlgeschlagener Re-Read lässt alle privaten Bereiche „nicht verfügbar“ werden.
- Kein Auto-Reparieren beschädigter persönlicher Daten durch Löschen oder eine leere Sammlung. Keine heimliche Änderung an bestehenden EKB1-/EKB2-Binärformaten.
- Export, Import, Journal-Speicherung, Erkenntnisse und Experimente dürfen nicht nebeneinander unabhängig schreiben; Tests müssen auch *parallel* gestartete Aktionen und App-Lock-Access-Epochs abdecken.

## 6. Pflicht-Tests als Implementierungsblocker

1. EKS1: leere/volle Datei, Manipulation/fehlender Schlüssel, GCM-Siegelung, Versions- und Größenfehler, wiederholtes Speichern/Abbrechen/Archivieren/Löschen.
2. EKB3: 0/1/300 Strategieexperimente, Mehrfachverknüpfungen, Unicode, Grenzlängen und maximale kombinierte Journal-/Erkenntnis-/Strategiedatenmengen – Roundtrip byte-/inhaltstreu (soweit Ordnung definiert).
3. EKB1/EKB2-Import auf Gerät **mit** bestehenden Strategieexperimenten: persönliche Texte behalten, ungültige Bezüge korrekt und sichtbar lösen; kein stilles Zurücksetzen oder stilles Löschen.
4. EKB3-Import: explizite Ersetzungswarnung, absichtlich leere Liste übernimmt erst nach Bestätigung, Abbruch/Passwortfehler/manipulierte Bytes ändern nichts.
5. EKR1-Altintent + Upgrade: vollständige Wiederholung vor EKR2. EKR2-Abbruch **nach jedem** von drei Zielschreibschritten und vor Intent-Löschung: identischer Zielzustand nach Neustart oder eindeutiger Sperrfehler, niemals Mischzustand.
6. Parallelität: Speichern eines Check-ins/Insight/Experiments während Import/Export, Doppeltippen und App-Hintergrund. Entweder serialisiert oder sauber abgelehnt – keine halb aktualisierten Sicherungen.
7. Verweise: Löschen/Ersatz von Einträgen und Erkenntnissen, fehlende Strategie-ID, Reimport EKB2 mit beschädigtem oder nicht vorhandenen Quell-ID-Match, Wiederherstellung auf **zweitem separatem Gerät**.
8. UX: Kein Speichern bei Rückkehr/Fragenwechsel, Status „Nicht ausprobiert“, „Unklar“ und Leerbewertung gleichberechtigt; 320 dp/200 % Text, TalkBack, Dark Mode, System-Zurück und Autofill-/Clipboard-Verhalten prüfen.
9. Backup-Dokumentation und Versionshinweis: EKB3 wird von Altversionen nicht unterstützt; bei falsch eingegebener Passphrase private Daten bleiben unverändert.

**Exit-Gate M7.4.1:** Alle drei Bestände können verschlüsselt, atomar und inklusive Altimport getestet werden. Vorher existiert **kein** aktiver `saveStrategyExperiment()`-Pfad in der Nutzeroberfläche und es wird **keine** personenbezogene EKS1-Datei durch die UX erzeugt.

## 7. Bewusst offene Entscheidungspunkte für das Implementierungsreview

- Konkrete Limits anhand 3000 Journaleinträgen und EKB3-Größenbudget benchmarken.
- Ob alle Verknüpfungen im ersten MVP nötig sind oder ein einfacher, optionaler Erkenntnisbezug genügt (Datensparsamkeit bevorzugen).
- Prüfung, ob für den EKB3-KDF ein stärkerer bzw. speicherharter Algorithmus mit ausreichend breiter Android-Unterstützung sinnvoll ist; keine unbelegte Sicherheitsbehauptung.
- Fachreview der M6.3-Karten mit reiner Aufklärung vor Anzeige eines CTA.
- Freigabe des genauen deutschsprachigen Importdialogs erst nach UX-Test mit fiktiven Daten.

**Abgrenzung:** Dieses ADR beschreibt eine **geplante** Architektur. EKS1, EKB3 und EKR2 sind **noch nicht implementiert**, nicht getestet und werden nicht als bestehende Sicherheit ausgegeben.
