# D04.1 – isolierter Qwen3-0.6B-SD-Gerätetest (Draft)

**Status: Code-/CI-Prototyp, kein bestätigter Android-Hardwaretest.** Der D04-PR #61 bleibt separat und ungemergt.

## Ziel und Datenschutz

Erste *echte* lokale CPU-Inferenz auf dem Samsung Galaxy A32 5G (Android 13, 4 GiB RAM), ohne Chat-Datenbank oder bestehende Entwürfe zu öffnen. Eigenständige `.d04qwenpreview`-Paketkennung; getrennte Android-Sandbox, keine Migration aus D03/D04. App hat weiterhin **keine INTERNET-Berechtigung**. Kein Modell ist in der APK; keine automatische Modellladung, kein Download, keine Cloud. Es wird nur ein fest codierter synthetischer Begrüßungsprompt verarbeitet. Das Ergebnis bleibt transient im UI und wird nicht an die normalen Chats angehängt.

## Quellen und exakte Modellidentität

- Basis: `Qwen/Qwen3-0.6B` (Apache-2.0-Modellfamilie; konkrete GGUF-Herausgabe des Quantisierers prüfen).
- Quantisierung: `unsloth/Qwen3-0.6B-GGUF`, **Revision `17429132c7b3c2dbe0ec7e06ee085aa89a96994f`**, Datei `Qwen3-0.6B-Q4_K_M.gguf` (~397 MB).
- Quelle: https://huggingface.co/unsloth/Qwen3-0.6B-GGUF/blob/17429132c7b3c2dbe0ec7e06ee085aa89a96994f/Qwen3-0.6B-Q4_K_M.gguf
- **Verbindlicher SHA-256:** `ac2d97712095a558e31573f62f466a3f9d93990898b0ec79d7c974c1780d524a`. Abweichende Modellrevisionen oder andere Quantisierungen dürfen nicht gestartet werden.
- Engine: `ggml-org/llama.cpp`, Commit `a691b2345844ce1b430201fc5fd404ce062e05f2`. Quellen werden bei CI aus GitHub geholt und müssen exakt diesem Commit entsprechen. Keine Modellgewichte werden im Repository, CI oder der APK gespeichert.

## Testablauf auf dem Zielgerät

1. **Erst nach erfolgreicher CI und expliziter Benutzerentscheidung** D04.1-Test-APK installieren. Vorhandene Versionen bleiben getrennt.
2. Auf dem Telefon die oben verlinkte **397-MB-GGUF** über einen Browser herunterladen und über die Dateiverwaltung auf die SD-Karte legen. Das Downloadziel und verfügbare interne Kapazität beachten; diese App kopiert die Datei nicht. In der Dateiauswahl manuell die SD-Karte öffnen.
3. In **Üben → D04.1 → GGUF auf SD-Karte auswählen** die Datei via Android-Dateiauswahl (SAF) öffnen. Danach ausdrücklich **GGUF-Prüfsumme überprüfen** tippen. Dies liest die Datei einmal, schreibt keine Kopie und vergleicht den vollen SHA.
4. Erst bei korrekter Prüfsumme **Lokalen Modelltest bewusst starten** tippen. Vorher prüft die App SD-Status, arm64-v8a, Android low-memory, wenigstens 1100 MiB aktuell verfügbaren RAM und 512 MiB internen Sicherheitsfreiraum.
5. Native `llama.cpp` lädt über `/proc/self/fd/<N>` den vom Android DocumentProvider bereitgestellten **seekbaren** Read-only-Dateideskriptor. **Wichtig:** Ob der konkrete Samsung-Provider das Wiederöffnen und mmap erlaubt, ist ungeklärt. Wenn nicht, ist der Test negativ und die App darf **nicht** eine interne Schattenkopie anlegen.
6. Begrenzungen: CPU-only, 2 Threads, 1024 Kontexttoken, max. 48 Output-Token, nur ein fest codierter synthetischer Prompt. Kein Netzwerk, keine GPU-/NPU-Garantien. Ein Hintergrundlauf kann kurzzeitig den RAM/CPU belasten.
7. Ergebnis oder Fehler-Screenshot melden, mit Speicher-/Temperaturbeobachtung. Nicht in die normalen Chats wechseln, um Modellqualität daraus zu schlussfolgern. Bei Hänger/Erwärmung Android-App schließen; keine automatische Wiederholung.

## Blockierende / offene Gates

- Ein erfolgreicher GitHub-APK-Build ist **kein** Beleg für echte Token-Generierung oder ausreichenden RAM.
- Die offene native Dateideskriptor-/mmap-Kompatibilität auf Samsung und mögliche Android-OOM-Kills können erst am Zielgerät getestet werden.
- Kein Modellqualitäts-Score, kein echter Gesprächspartner, kein K2-Coaching oder Freigabe einer produktiven KI.
- Benutzerbestätigung ist erforderlich, bevor eine Modelldatei im Browser heruntergeladen und ein ressourcenintensiver Test gestartet wird.
- Bei Crash: keine persönlichen Nachrichten verlieren, da dieses APK getrennte Daten und nur synthetische Testprompts verwendet.
- Vor Merge: vollständige GitHub CI, native Cross-Compile, APK-Signaturprüfung, das Debug-APK auf dem Gerät ausführen, Ergebnis dokumentieren.

## Bauen

Im Verzeichnis `apps/CommunicationTrainer/android`:

```bash
bash scripts/bootstrap-qwen-probe.sh
gradle :app:testDebugUnitTest :app:assembleDebug --no-daemon
```

Erforderlich: JDK 17, Android SDK 36, NDK 27.2.12479018 und CMake 3.22.1. Modell separat; ein natives Kompilat ist keine Modellinstallation.
