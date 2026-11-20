# D05.1 – Gehärteter Staging-Build (Play-Protect-Befund #81)

**Status:** Isolierter Entwicklungs-PR, kein Release und keine Sicherheitsentwarnung.
**Grundlage:** D05.1-Branch aus PR #67 inklusive gemergtem Dependency-Sicherheits-PR #83.
**Wichtige Abgrenzung:** Der bisher von Play Protect blockierte Debug-Build bleibt unverändert. Die neue Staging-Variante ist eine **neue APK-Identität**, nicht die als unbedenklich deklarierte alte APK.

## Technische Eigenschaften

| Merkmal | Bisherige Debug-APK | Neue Staging-Variante |
|---|---|---|
| Package ID | `com.valoon4.communicationtrainer.d051guidedpreview` | `com.valoon4.communicationtrainer.d051staging` |
| Label | CommunicationTrainer D05 Lernhilfe | CommunicationTrainer Staging |
| Android `debuggable` | true | **false** |
| Build-Abhängigkeiten | `debugImplementation(ui-tooling)` | **release-kompatibel**, kein `ui-tooling` |
| Exportierte Compose `PreviewActivity` | vorhanden | **im fertigen Manifest verboten** |
| Android Backup | false | false |
| Signatur | kurzlebiger CI-Debug-Schlüssel | nur nach **separater autorisierter Signatur** mit stabilem, geschütztem Staging-Key |
| Data migration | keine | keine; getrennte Paketidentität |
| Bestimmung | Entwicklung im Emulator/Debug | ausschließlich interner Sicherheits- und Emulatortest |

Der Gradle-Build `:app:assembleStaging` erzeugt absichtlich **nur eine unsigned APK**. Selbst wenn `:app:testStagingUnitTest` und die Manifestprüfung erfolgreich sind, wird **keine installierbare APK verteilt**.

Das neue Prüftool `security/verify_staging_apk.py` inspiziert direkt die kompilierte Binärdatei mit `aapt`: Package, Label, targetSdk, `android:debuggable=false`, `android:allowBackup=false` und Abwesenheit von `androidx.compose.ui.tooling.PreviewActivity`. Eine Fehlprüfung verhindert die weitere Staging-Verarbeitung. Ein grüner Scanner oder eine fehlende Debug-Markierung **beweisen nicht**, dass die App malwarefrei ist.

## Sicherheitsregel für dauerhafte Signatur

Ein **stabiler** Signierschlüssel darf niemals in Git, bei Issues, in Logs oder als öffentliches Build-Artefakt erscheinen.

Für eine dauerhaft signierte Staging-APK muss der Repository-Eigentümer einmalig die folgenden Voraussetzungen herstellen:

1. Ein **eigenes, nur für CommunicationTrainer-Staging** verwendetes KeyStore-Zertifikat offline erzeugen und sicher sichern (keinesfalls den Play-Store-/Release-Schlüssel verwenden). Beispiel auf einem vertrauenswürdigen Computer:

   ```bash
   keytool -genkeypair -v -storetype PKCS12 \
     -keystore CommunicationTrainer-staging.p12 \
     -alias communicationtrainer-staging \
     -keyalg RSA -keysize 3072 -validity 3650 \
     -dname "CN=CommunicationTrainer Staging (Internal)"
   keytool -list -v -keystore CommunicationTrainer-staging.p12 \
     -alias communicationtrainer-staging
   ```

2. Unter **GitHub → Settings → Environments** die geschützte Umgebung `communicationtrainer-staging` anlegen, **required reviewers** aktivieren und die zugelassenen Branches auf `main` beschränken. Nur selbst kontrollierte Personen dürfen freigeben.
3. In dieser Umgebung **Secrets** einrichten:
   - `CT_STAGING_KEYSTORE_B64` – base64-kodierte **gesamte** PKCS12-Datei
   - `CT_STAGING_STORE_PASSWORD` – KeyStore-Passwort
   - `CT_STAGING_KEY_PASSWORD` – Key-Passwort
4. Dort außerdem **Variablen** einrichten:
   - `CT_STAGING_KEY_ALIAS` – z. B. `communicationtrainer-staging`
   - `CT_STAGING_SIGNER_SHA256` – der **öffentliche Zertifikat-Fingerprint SHA-256**, 64 Hex-Zeichen (Doppelpunkte optional)
5. KeyStore **nur** im sicheren Original + verschlüsseltem Backup halten. Ein Schlüsselwechsel verhindert Updates der gleichnamigen installierten App; deshalb ist die Stabilität wichtig. Keystore oder Passwörter niemals als Chatnachricht oder GitHub-PR-Kommentar senden.

Der manuelle Job `signed-staging` läuft ausschließlich auf **`main`**, ausschließlich nach `workflow_dispatch`, nach erfolgreicher Staging-Kompilation und mit GitHub-Environment-Freigabe. Ohne diese **vorher vom Eigentümer gesetzten** Werte **bricht er ausdrücklich ab** – niemals unbemerkt mit einem flüchtigen Debug-Schlüssel signieren. Das Skript `security/sign_staging.sh` validiert nach dem Signieren zusätzlich den festgelegten Zertifikat-Fingerprint.

Solange PR #66 und PR #67 nicht einzeln freigegeben/gemergt sind, existiert dieser Workflow noch **nicht als main-Staging-Signer**. Der PR kann nur den *unsigned* Build und einen CI-Signing-Smoke-Test ausführen; der Smoke-Test nutzt einen im Runner erzeugten **Einmal-Schlüssel**, der unmittelbar gelöscht und **niemals verteilt** wird.

## Abnahme der Entwicklungsstufe

- [ ] `:app:testStagingUnitTest` bestanden
- [ ] `:app:assembleStaging` bestanden
- [ ] Kompiliertes APK-Manifest überprüft: dedizierte ID, Label, debuggable false, Backup false, keine PreviewActivity
- [ ] Signature-Smoke-Test mit **disposable** CI-Schlüssel bestanden, **keine** APK aus diesem Schritt hochgeladen
- [ ] Normaler D05.1-Debug-CI unverändert erfolgreich; Room-v1-Schema unverändert
- [ ] Dauerhaftes Staging-Zertifikat durch Repository-Eigentümer in geschützter GitHub-Umgebung hinterlegt und manuelle Signatur verifiziert – **noch kein automatisches Gate**
- [ ] Original blockierte APK im isolierten Android-System analysiert; Google Play-Protect-Blockierung verstanden – **separates, noch offenes Gate**
- [ ] Vor tatsächlicher Verteilung Signatur-/Berechtigungs-/Datenschutz-/KI-Zugriffsmodell gegen Live-Verhalten geprüft

### Nicht umgesetzt / nicht versprochen

- Kein Debugging-, Frida-/Root-/Emulator-Detektions-Bypass
- Keine Deaktivierung von Play Protect
- Keine private Schlüsselaufbewahrung durch ChatGPT
- Kein automatisches Neuerstellen/Ersetzen von Benutzer-Apps oder Datenmigration
- Keine Freigabe der original blockierten APK
- Kein vollautomatischer sicherheitskritischer Release; vorher Review, isolierter Verhaltensscan und Freigabe erforderlich

**Sicherheitshinweis:** Das neue Package kann eine bisher unbekannte oder die gleiche Play-Protect-Bewertung erhalten. Eine neue Paketkennung oder Signatur ist **keine** Malware-Entwarnung. Auf dem privaten Samsung A32 5G nicht gegen eine Sicherheitswarnung installieren.
