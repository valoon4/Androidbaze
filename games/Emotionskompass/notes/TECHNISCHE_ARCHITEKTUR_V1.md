# Emotionskompass – technische Architektur v1.0
Stand: 28.10.2026 · Bezug: PRODUKT_SPEZIFIKATION_V1.md · Status: verbindliche technische Baseline mit gekennzeichneten Release-Gates

## 1. Randbedingungen, Technologiewahl, Architekturentscheidungen

**Plattform:** Native Android-Anwendung, primär Smartphones im Hochformat; Tablets/Querformat dürfen nicht abstürzen, adaptive Optimierung nach MVP. Deutsch als erste Sprache; Strings vollständig ausgelagert. API minSdk **26** als vorläufiges Kompatibilitätsziel; targetSdk/compileSdk und Abhängigkeitsversionen werden beim Projektbootstrap auf eine stabile, unterstützte und getestete Kombination festgelegt und anschließend per Version Catalog fixiert (nicht ungeprüft auf neue Majorversionen upgraden).

**Kernstack:** Kotlin, Jetpack Compose + Material 3 als UI-Basis, eine Activity, ViewModel, StateFlow, Coroutines/Flow, Navigation (aktuelle stabile Compose-Navigationsbibliothek; Navigation 3 nach Kompatibilitätsprüfung), Room/SQLite für relationale Nutzdaten, DataStore für nicht-sensible Einstellungen, Kotlin Serialization für Versionierung/Export, JUnit/Compose UI Tests, Gradle Kotlin DSL und Versionskatalog.

**Architektur:** Single source of truth in Repositories; UDF (UI-Aktionen -> ViewModel -> Use Case -> Repository -> Datenquelle; neues StateFlow -> UI). UI ist zustandsgetrieben und enthält keine Persistenz-/Filterlogik. Fachlogik in domain/usecases; IO in data. Keine globalen Singletons für persönliche Zustandsdaten.

**Entscheidungsprotokoll:**
- ADR-001: Native Android/Compose statt WebView, damit Accessibility, Systemnavigation, Offline-Betrieb und Datenschutz sauber kontrollierbar bleiben.
- ADR-002: Ein unabhängiges Gradle-Projekt unter games/Emotionskompass/src; bestehendes Androidbaze-Root-Projekt und TimeFlow bleiben unverändert.
- ADR-003: Room für relationale Daten und Transaktionen; DataStore nur für kleine, unkritische Einstellungen.
- ADR-004: Emotionskatalog versioniert und mit der App ausgeliefert; keine verpflichtende Netzwerkabhängigkeit.
- ADR-005: Keine Cloud-/Konto-/Analyse-Pflicht; keine personenbezogenen Daten im Repository oder CI.
- ADR-006: Persönliche Daten in Ruhe verschlüsselt und durch Android Keystore unterstütztes Schlüsselmanagement; kryptografische Implementierung wird vor P0-Release per Sicherheits-Spike und Tests überprüft.
- ADR-007: Kein KI-Modul im MVP. Später nur getrenntes, opt-in-gesteuertes Modul.

## 2. Systemübersicht

    ┌──────────────────── Compose Screens / Navigation ────────────────────┐
    │ Start | Atlas | Detail | Vergleich | Navigator | Reflexion | Journal │
    └──────────────────────────┬──────────────────────────────────────────┘
                               │ UiActions / StateFlow
    ┌──────────────────────────▼──────────────────────────────────────────┐
    │ ViewModels (pro Screen/Flow): ScreenUiState, Input Validation       │
    └──────────────────────────┬──────────────────────────────────────────┘
                               │ use cases / typed domain models
    ┌──────────────────────────▼──────────────────────────────────────────┐
    │ Domain: FindEmotions, CompareEmotions, SaveCheckIn, EditEntry,       │
    │ SaveDraft, DeleteEntry, BuildLocalInsights, ExportData             │
    └──────────────────────────┬──────────────────────────────────────────┘
                               │ repository contracts
       ┌───────────────────────┴──────────────────────┐
       ▼                                              ▼
    EmotionCatalogRepository                      JournalRepository
    - versioned seed + lookup                      - Room DAOs
    - categories + relations                       - protected storage
    - local search + filters                        - drafts + transactions
       │                                              │
       └──────────────────┬───────────────────────────┘
                          ▼
                Room/SQLite (encrypted)
              PreferencesRepository -> DataStore

Repositories expose immutable domain models/flows. Einträge werden nicht von Komponenten direkt in DAO geschrieben. Nicht-sensitive UI-Präferenzen separat.

## 3. Geplante Verzeichnis-/Paketstruktur

    games/Emotionskompass/
      README.md
      notes/                         # Versionskontrollierte Spezifikationen und ADRs
      src/
        settings.gradle.kts
        gradlew, gradlew.bat, gradle/wrapper/
        gradle/libs.versions.toml
        build.gradle.kts
        app/
          build.gradle.kts
          src/main/
            AndroidManifest.xml
            java/com/valoon4/emotionskompass/
              MainActivity.kt
              EmotionApp.kt
              core/designsystem/
              core/navigation/
              core/security/
              core/database/
              core/model/
              feature/home/
              feature/atlas/
              feature/comparison/
              feature/checkin/
              feature/reflection/
              feature/journal/
              feature/settings/
              feature/insights/       # P1
              feature/learning/       # P1
              data/catalog/
              data/journal/
              data/preferences/
              domain/
            assets/catalog/emotions.de.v1.json
            res/values/strings.xml
            res/xml/data_extraction_rules.xml
          src/test/                   # Domain + repository tests
          src/androidTest/            # Compose + Room + security integration
      apks/                           # Optional; keine personenbezogenen Artefakte

**Modulstrategie:** Zu Beginn ein Android-App-Gradle-Modul mit strikten Package-Grenzen statt unübersichtlicher vorzeitiger Multimodularisierung. Wenn begründet (Buildzeit/Ownership), später core/feature-Module extrahieren. Trennung wird durch Interfaces/Tests erreicht, nicht durch Anzahl der Gradle-Projekte.

**Package-ID (vorläufig):** com.valoon4.emotionskompass, vor Release explizit freizugeben. Produktname „Emotionskompass“ ist Arbeitstitel; Marken-/Namensprüfung vor öffentlicher Veröffentlichung.

## 4. Persistenz- und Domänenmodell

Primärschlüssel werden als stabile UUID (String) für persönliche Datensätze und stabile, semantisch neutrale String-IDs für Katalogeinträge verwendet. Alle Zeitpunkte als UTC Instant epochMillis gespeichert; Anzeige ausschließlich in Gerätezeitzone. Aktualisierung geschieht atomar. Fremdschlüssel und Indizes mit Room definieren; Migrationspfad verpflichtend.

### 4.1 Relationale Tabellen

| Tabelle | Spalten (Kern) | Schlüssel/Regeln |
|---|---|---|
| emotion | id TEXT, name_de TEXT, short_definition TEXT, type ENUM, synonyms_json TEXT, examples_json TEXT, body_notes_json TEXT, distinction_prompt TEXT, content_version INT, active BOOL | PK id; Name nicht als ID; redaktioneller Seed |
| emotion_category | id TEXT, label_de TEXT, sort_order INT | PK id |
| emotion_category_crossref | emotion_id TEXT, category_id TEXT | zusammengesetzter PK, FKs, eine Emotion kann mehrere Kategorien haben |
| emotion_relation | from_id TEXT, to_id TEXT, relation_type ENUM, explanation TEXT | FKs; Similar/Contrast/Overlaps, redaktionell kuratiert |
| journal_entry | id UUID, created_at_utc LONG, event_at_utc LONG NULL, modified_at_utc LONG, title TEXT NULL, situation TEXT NULL, thoughts TEXT NULL, body_sensations TEXT NULL, needs TEXT NULL, reaction TEXT NULL, insight TEXT NULL, uncategorized BOOL, source ENUM | PK; Zeitstempel unverwechselbar; freie Felder optional |
| entry_emotion | entry_id UUID, emotion_id TEXT NULL, custom_label TEXT NULL, intensity INT NULL, sort_order INT | PK (entry_id, sort_order); CHECK intensity 1..10 OR NULL; emotion_id oder custom_label (P1) |
| checkin_draft | id UUID, updated_at_utc LONG, step TEXT, state_json TEXT, origin TEXT | Separat von Journal; zur sicheren Fortsetzung; löschen/überführen |
| learning_progress | emotion_id TEXT, bookmarked BOOL, last_viewed LONG NULL, familiarity INT NULL | P1; keine implizite Befindlichkeitsbewertung |
| content_meta | catalog_version INT, applied_at_utc LONG, checksum TEXT | Prüft Seed- und Katalogupdates |

Nicht-sensitive UI-Präferenzen über DataStore: theme_mode, onboarding_completed, font_presentation_setting, optional_reminder_settings (P1). Besonders schützenswerte Texte niemals in Klartext-DataStore ablegen.

### 4.2 Domain-Modelle / Enums
- EmotionType: EMOTION, MOOD, PHYSICAL_STATE, NEED, OTHER; die UI erläutert Unterschiede ohne künstliche Taxonomie.
- EmotionRelationType: SIMILAR, CONTRAST, OVERLAPS.
- Valence: PLEASANT, UNPLEASANT, MIXED, NEUTRAL, UNKNOWN; optional.
- Activation: HIGH, MID, LOW, UNKNOWN; optional.
- EntrySource: QUICK_CHECKIN, DEEP_REFLECTION, FROM_ATLAS, MANUAL.
- CheckInDraft: draftId, originScreen, currentStep, valence?, activation?, selectedEmotions[], intensityByEmotion?, fields?, updatedAt.

**Datenkonsistenz:** Ein Eintrag darf 0 Emotionszuordnungen enthalten. Es dürfen mehrere Einträge am selben Zeitpunkt bestehen. Doppeltippen auf „Speichern“ ist idempotent über einmalige draftId/submit-lock. Beim Editieren wird eine Transaktion für Entry + Join-Tabelle verwendet, bei Abbruch bleibt die Originalversion unverändert. Eingaben anderer Tabs zerstören den Entwurf nicht.

### 4.3 Katalogupdates und Migrationen
- Katalogdatei mit version, locale, checksum, emotion records; statisch versioniert, validiert und per Tests kontrolliert.
- Beim Erststart Import in einer Transaktion; bei Update upsert anhand stabiler IDs. Persönliche Zuordnungen werden nicht blind gelöscht. Entfernte Begriffe werden soft-deprecated oder mit Migration versehen.
- Neue Sprachen später über separate Localized-Content-Tabelle; IDs unabhängig vom deutschen Wort.
- Kein ungetestetes destructiveMigration() für produktive Nutzerdaten. Migrationstests mit Vorversion-Datenbanken sind Pflicht.

## 5. Domain Use Cases und Suche

**Use Cases:**
1. LoadHomeContent(): verfügbare Einstiege, zuletzt gesehener Begriff, bestehender Entwurf.
2. SearchEmotions(query, filters): Normalisierung Unicode/Case/Diakritika, Name/Alias/Definition-Snippet, Ranking nach Namensübereinstimmung; Debounce nur bei sehr großen Katalogen.
3. GetEmotionDetail(id) / CompareEmotions(idA,idB): redaktionelle Texte und Relation; bei fehlender Relation keine erfundenen Fakten.
4. SuggestFamilies(valence?, activation?): transparente, lokal definierte Heuristik als *Anregung*; vollständiges Atlas-Suchfeld jederzeit frei verfügbar.
5. UpdateDraft(action): pure State-Reducer-Funktion plus persistierter Snapshot; Speicherung gedrosselt oder bei Lifecycle-Ereignis.
6. SaveJournalEntry(draftId): DB-Transaktion + deduplizierte Übernahme; Validierung nur strukturell (z.B. Intensitätsbereich).
7. UpdateJournalEntry(entryId, changes), DeleteJournalEntry(entryId), DeleteAllPersonalData().
8. ExportPersonalData(): expliziter User-Flow, versioniertes strukturiertes JSON, Android Storage Access Framework, keine stillen Dateianhänge.
9. CalculateInsights(range) erst P1: rein lokale Aggregate, kleine Fallzahlen vorsichtig interpretieren, keine psychologische Schätzung.

**UX-Wichtig:** Der Such- und Filteralgorithmus darf Begriffe nicht unsichtbar machen, nur weil sie scheinbar nicht zur Valenz/Energie passen. User-Eingaben steuern Ergebnispriorisierung, nicht Wahrheit.

## 6. Security/Privacy Threat Model und Release-Gates

Die Anwendung verarbeitet sehr persönliche Tagebuchdaten. Eine lokale Room-Datenbank ist **nicht automatisch verschlüsselt**. Verschlüsselung ist ein separates Implementierungs- und Testpaket; bis dahin ist die App keine freigegebene Produktivversion.

**Schutzmaßnahmen für P0-Release:**
1. **At-rest:** SQLite-Datenbank samt WAL/Journal und Drafts nach einem geprüften, Android-kompatiblen Verschlüsselungskonzept sichern (z. B. gepflegte SQLCipher-kompatible Room-Integration, nur nach Abhängigkeitsprüfung). Schlüsselmaterial über Android Keystore verwalten, keine Hardcoded Keys, keine Eigenbau-Kryptografie.
2. **Systembackup:** Auto Backup und Geräte-Transfer für persönliche Daten standardmäßig explizit ausschließen (inkl. Android-12+-dataExtractionRules und Legacy-Regeln). Erst später kontrolliertes verschlüsseltes Backup als Option.
3. **Datenabfluss:** Keine Netzwerkberechtigung für P0, soweit alle ausgewählten Bibliotheken ohne sie funktionieren; keine Werbung, Crash-SDKs mit Textinhalten oder Third-Party-Analytics. Crash-Berichte niemals mit personenbezogenen Inhalten.
4. **Logs/UI:** Sensible Texte nicht in Logcat, Exceptions, Clipboard ohne Nutzeraktion oder Lockscreen-Notifications; Vorschaubilder im Recents-Screen und Screenshots anhand Nutzbarkeit/Datenschutz abwägen und Schutzmaßnahme dokumentieren.
5. **Export:** Android SAF speichert nur nach expliziter Zielwahl. Für MVP: portables JSON mit deutlich sichtbarem Hinweis „Die Exportdatei kann unverschlüsselt sein und ist außerhalb der App nicht geschützt“. Verschlüsselter Export als P1 nur mit standardkonformem Verfahren. Exportformat mit schemaVersion, exportTimestamp, entries, links; niemals Schlüsselmaterial exportieren.
6. **Löschen:** Eintrag hart löschen mit Join-Daten/Entwürfen, „alle Daten löschen“ umfasst persönliche Einträge, Lernfortschritt und ggf. Exportcache. Vorsicht: physisches Secure Erase auf Flashspeicher kann nicht garantiert werden; UI darf das nicht behaupten.
7. **Consent:** Keine vorausgewählten Einwilligungen für zukünftige KI/Cloud. P2 nur mit erklärten Datenflüssen, Löschungs- und Widerrufsmöglichkeit.

**Freigabesperre:** Wenn Verschlüsselung, Backup-Ausschluss, Exportwarnung, Löschtests oder Datensicherheitstests scheitern, ist P0 nicht releasefähig. Vor Veröffentlichung Rechts-/Datenschutzprüfung zu Zweckbestimmung, Datenschutzhinweisen und geltenden Anforderungen durchführen.

## 7. Tests und Build-Qualität

**Unit-Tests (P0):** SearchEmotions Synonyme/Umlaute/Leeranfrage; Kategorierelation; 0–n Emotionen; gemischte Valenz; Intensität NULL/1/10 und ungültige 0/11; SaveDraft/Resume; SaveIdempotency; Edit ohne Überschreiben; Filter und Sortierung; redaktionelle JSON-Schema-Prüfung.

**Integrationstests:** Room Migrationen, Fremdschlüssel, Speichern/Neustart, atomare Edit-/Delete-Vorgänge, Ausfall beim Speichern, Verschlüsselung und Schlüsselmanagement, Backup-Ausschluss; Export-JSON mit Testdaten. Kein echter privater Inhalt im Testrepositorium.

**Instrumentierte UI-Tests:** Onboarding überspringen, beide Einstiege, Atlas-Suche, Atlas->Reflexion->Atlas Rückweg, Quick Check-in ohne Antworten, Mehrfachauswahl, Draft bei Activity-Neustart und Rotation, Editieren/Löschen, große Schrift und TalkBack-Handprüfung.

**Definition of Done jeder Änderung:** Kompiliert; Unit-/Integration-/UI-Tests entsprechend Scope; kein sensibles Logging; Offline-Pfade funktionieren; neue Strings lokalisiert; Accessibility geprüft; README/Changelog und Spezifikation ggf. aktualisiert; Review/Abnahme; neue Debug-APK aus reproduzierbarem CI-Build. Keine Aussage „fertig“, solange Build oder Tests nicht überprüft wurden.

## 8. GitHub- und CI-Strategie

**Repository:** valoon4/Androidbaze (öffentlich); Projekt in games/Emotionskompass/ im selben Stil wie games/TimeFlow. Kein Überschreiben von Root-Gradle, Root-App oder anderer Spiele.

**Branching:** main bleibt funktionierender Stand. Für jede Etappe feature/emotionskompass-* oder fix/emotionskompass-*, danach PR und Abnahme. Verschiedene Projektbereiche klar in Paths begrenzen. Änderungs-IDs und Testnachweise in PR-Beschreibung.

**CI ab Phase 1:** eigene .github/workflows/emotionskompass-ci.yml mit Path-Filtern für games/Emotionskompass/src/** (und Build-Konfiguration), working-directory im Unterprojekt; JDK/Android SDK/Gradle versionsgepinnt, Caches kontrolliert; Gradle Unit Tests, Lint, assembleDebug; instrumentierte Tests soweit Emulator verfügbar. Debug-APK als GitHub-Actions-Artefakt; optional nur ausgewählte benannte Meilensteine in apks/, um Git nicht mit Binärdateien aufzublähen. No secrets in artifacts, keine produktiven Nutzerdaten.

**Release-Tags:** emotionskompass-v0.1.0, v0.2.0 usw. Versionsschema SemVer-ish für Prototypen; Release-Notes enthalten neue Features, Teststatus und Einschränkungen.

## 9. Offene technische Verifikationen vor Code-/Release-Gate

Die folgenden Punkte sind ausdrücklich **keine stillschweigend erledigten Aufgaben**:
- Vor dem ersten Gradle-Commit: stabile Kotlin/AGP/Compose/Room/Navigation/Keystore-kompatible Versionen anhand offizieller Docs zusammen prüfen und in libs.versions.toml pinnen.
- Vor öffentlichem Test mit echten Daten: persistente Datenbankverschlüsselung auf konkreten Geräten/API-Leveln erfolgreich nachweisen (inklusive DB/WAL/Drafts, Geräte-Backup).
- Vor Release: finale App-ID/Name, Markenprüfung, Datenschutz-/Lizenzliste, Android-Target-Regeln, manuelle Accessibility-Tests und Import-/Recovery-Strategie klären.
- Falls kein robustes Krypto-Setup verfügbar ist, kein Release als „sicher“ deklarieren.

## 10. Referenzen (Architektur)

- https://developer.android.com/topic/architecture
- https://developer.android.com/topic/architecture/recommendations
- https://developer.android.com/training/data-storage/room
- https://developer.android.com/topic/libraries/architecture/datastore
- https://developer.android.com/privacy-and-security/risks/backup-best-practices
