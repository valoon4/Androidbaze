# D04 – Zielgerät Galaxy A32 5G: erster On-Device-KI-Test

**Stand:** 2026-11-08. **Kein tatsächlicher Modelltest erfolgt.** Zielgerät vom Nutzer genannt: Samsung Galaxy A32 5G. **4 GB physischer RAM und Android 13 laut Nutzer bestätigt** (keine eigenständige Geräteauslesung). Exakte Buildnummer, ABI und freier Speicher sind noch nicht erfasst.

## Herstellerbasis

- Samsung Galaxy A32 5G (Modellvarianten der Serie SM-A326), Markteinführung 2021. CPU-Plattform: **MediaTek Dimensity 720 / MT6853**. Referenz: [Samsung A32 5G Specs PDF](https://image-us.samsung.com/SamsungUS/samsungbusiness/pdfs/spec-sheet/Galaxy-A32-5G-Series-Spec-Sheet-Unlocked.pdf).
- CPU: **2× Cortex-A76 und 6× Cortex-A55**, GPU **Mali-G57 MC3**. Referenz: [MediaTek Dimensity 720 Product Brief](https://mediatek-marketing.files.svdcdn.com/production/documents/MediaTek-Dimensity-720-Product-Brief-PDFMD720PB-0720.pdf?dm=1684470668).
- Varianten besitzen je nach Markt **4 GB oder 6 GB RAM** (Samsung US nennt 4 GB, Samsung Hongkong 6 GB). Für dieses Zielgerät sind **4 GB RAM vom Nutzer bestätigt** (RAM Plus nicht mitgerechnet). Referenzen: [Samsung USA](https://www.samsung.com/us/business/support/owners/product/galaxy-a32-5g-unlocked/), [Samsung HK](https://www.samsung.com/hk_en/news/product/samsung-launches-mid-tier-smartphone-galaxy-a32-5g/).
- **Android 13 ist auf dem Nutzergerät bestätigt.** Es ist außerdem für SM-A326B in offiziellen [Samsung Software-Update-Verläufen](https://doc.samsungmobile.com/SM-A326B/018740210504/eng.html) dokumentiert; die genaue Buildnummer bleibt unbekannt.

## Konservatives Testprofil (4 GB vom Nutzer bestätigt)

**Ziel:** Nachweisen, ob kleine lokale Inferenz überhaupt stabil ist, ohne App-/Android-Prozesse durch Speicher- oder Thermikdruck zu stören. Keine behaupteten Antwortzeiten, Tok/s oder Qualität.

1. **CPU-first mit ARM64-Android-Runtime** (z. B. nach Prüfung `llama.cpp`), zunächst **Qwen3-0.6B** in geprüfter Q4_K_M-GGUF-Version. Die GGUF-Datei ist grob **0,4–0,5 GB** groß, abhängig vom konkreten quantisierten Artefakt. Runtime-/Model-Lizenz, SHA-256 und Chat-Template vor Installation prüfen. Keine unüberprüften Drittgewichte, keine Modellfiles im Git.
2. Start mit **max. 1.024 Kontexttoken**, später 2.048 als getrennte Messung; 2 und 4 CPU-Threads getrennt testen. Weder GPU- noch NPU-Beschleunigung voraussetzen.
3. **Messprotokoll:** Cold Start, TTFT, Gesamtdauer p50/p95, Tok/s, maximale Prozess-/System-RAM-Nutzung, thermische Drosselung und Akku-/Abbruchverhalten. Testumfang/Antwortlängen und exakt installierten Android-Build dokumentieren. **Vorläufig keine Mindestgeschwindigkeit vorgeben**.
4. **Sicherheit:** ausschließlich synthetische Gespräche aus K5, kein Netzwerk-Fallback, keine API-Schlüssel, kein absichtliches Überschreiben vorhandener D03/D04-Daten, keine auto-startenden Modell-Downloads. Bei OOM / nicht ausreichend Speicher verständliche Meldung und Abbruch ohne Nachrichtenverlust.
5. **Qualitätsprüfung separat:** natürliches Deutsch, respektvolle Grenzen, stabile Rollen, keine erfundenen Erinnerungen. Ein schneller 0.6B-Chat ist nicht automatisch als sozial verantwortungsvolle Trainings-KI ausreichend.
6. **Qwen3-1.7B:** für dieses 4-GB-Gerät **zunächst zurückstellen**, unabhängig davon, ob die Modelldatei herunterladbar wäre. Erst nach stabiler 0.6B-Baseline und gemessenem Speicher-/Thermikspielraum kann ein gesondert abgesicherter Versuch erwogen werden. **Qwen3-4B:** nicht auf diesem A32 5G priorisieren; ein leistungsfähigeres Referenzgerät vorsehen.
7. **Preflight auf 4-GB-Gerät:** freien Speicherplatz (Modell plus sicherer Reserve), verfügbare RAM-Menge und Android-`ActivityManager.isLowRamDevice`/`MemoryInfo.lowMemory`, CPU-ABI und laufende Energiesparmaßnahmen zuerst prüfen; bei knappen Ressourcen **keine Modellladung**. Die 4 GB sind Gesamtspeicher und stehen einer App nicht vollständig zur Verfügung. Keine automatische RAM-Plus-Erhöhung und keine riskanten OOM-Stresstests auf privaten Geräten.
8. **Kandidatentests sind keine D04-Fertigmeldung:** `InferenceGateway` und Offline-Evidenzprüfung aus PR #61 sind nutzbar, aber echte On-device-Lab-Integration und Messung bleiben separate noch nicht autorisierte Hardware-/Runtime-Arbeiten.

## Gerätespeicher: Nutzer hat aktuell nur ungefähr 1,5 GB frei

**Vom Nutzer bestätigt, 08.11.2026:** ca. **1,5 GB freier interner Speicher**. Das ist **Speicherplatz**, nicht RAM. **Produktentscheidung:** Bei so wenig internem Platz **keine Modellinstallation intern**. Ein späterer Modelltest auf der SD-Karte ist denkbar, aber erst nach Prüfung des SD-Dateizugriffs und des tatsächlichen Speicherbedarfs der nativen Runtime. D04-Debug-APK bleibt weiterhin eine reine Simulation ohne Modellgewichte.

- Realistische Größe eines Qwen3-0.6B-Q4_K_M-GGUF-Artefakts: je nach Quantisierungsquelle grob **397–484 MB**, **ohne** Runtime, Installationsdateien, Cache und vorübergehend doppelte Dateien. Beispieldaten: [Hugging Face prithivMLmods](https://huggingface.co/prithivMLmods/Qwen3-0.6B-GGUF) und [TensorBlock](https://huggingface.co/tensorblock/Qwen_Qwen3-0.6B-GGUF).
- Die Native-Runtime-Größe hängt stark von Compiler-Flags, ABI und eingeschlossenen CPU-/GPU-Bibliotheken ab. Dritte dokumentieren für einen kleinen CPU-nahen Android-Build **ca. 20 MB** an nativen Bibliotheken und für eine breite GPU-/VLM-Konfiguration ein **ca. 95–100-MB-APK**. Dies sind **keine** Messergebnisse unseres zukünftigen Builds. Quellen: [BitNet Android JNI](https://github.com/abhikumar08/react-native-bitnet/blob/main/native-build.md) und [llama.cpp Android Prebuilt](https://github.com/xentron-bit/llama-android-prebuilt).
- **Vorläufiges Vorsichtsgate (Projektentscheidung, keine Android-Systemvorgabe):** Mindestens **3 GB internen Speicher frei** vor einem Modell-Installations- oder CPU-Test. Zuerst exakten Download- und Entpack-/Umbenennungsablauf implementieren und messen. Download muss abbrechbar sein, vorab freien Speicher und eine Notreserve prüfen, fehlgeschlagene Dateien aufräumen und darf niemals App-Nachrichten oder bestehende Datensätze löschen.
- Modell-Download **nur bewusst durch den Nutzer**, niemals automatisch beim Öffnen eines Chats. Keine gerätespezifische Stresstest-Freigabe allein aus Dateigröße ableiten; 4-GB-**RAM**-Druck bleibt separat kritisch.
- Das Galaxy A32 5G unterstützt je nach Ausführung microSD (Herstellerangabe). Eine **reine SD-Speichererkennung** ist in D04 nun implementiert, das **native Laden eines GGUF direkt von microSD ist weiterhin nicht implementiert oder getestet**. Nicht behaupten, dass die vorhandene App oder das Modell sofort auf eine SD-Karte verschoben werden kann.

**Alternative für die Entwicklung:** Model- und Runtime-Benchmarks vorläufig auf einem separaten Rechner oder Gerät mit ausreichend freiem Speicher durchführen und nur anonymisierte Messmetadaten übernehmen. So bleibt das Nutzergerät geschützt. Cloud-Broker-Tests bleiben unabhängig davon bis zu gesonderter Freigabe gesperrt.

## Option: 20-GB-microSD statt Modell im internen Speicher

**Nutzerangabe, 08.11.2026:** Eine **Speicherkarte mit rund 20 GB tatsächlich freiem Platz** ist laut Nutzer im Galaxy A32 5G vorhanden (keine direkte App-Messung). Die 20 GB sind **frei**, nicht die Gesamtkapazität. Der interne Speicher hat laut Nutzer weiterhin nur etwa **1,5 GB frei**. Diese Angabe nicht mit dem weiterhin knappen **4-GB-RAM** verwechseln.

**Neue, vorsichtige D04-Option:** Große GGUF-Modelldatei auf der microSD halten, native Android-Laufzeit und Room-Datenbank zunächst intern belassen. Ein Qwen3-0.6B-Q4_K_M-Artefakt liegt je nach Quantisierung grob bei **0,4–0,5 GB**; die tatsächliche native Runtime- und Cache-Größe ist noch nicht gemessen. Kein automatischer Download, bevor der direkte SD-Zugriff ohne interne Schattenkopie verifiziert ist.

### Bereits implementierter ungefährlicher D04-Schritt

**D04-SD-Preview (eigene Paketkennung `.d04sdpreview`, getrennt von der bisherigen D04-Preview), Bereich Üben → „SD-Speicher jetzt prüfen“:** Die Android-App prüft auf Knopfdruck **ohne Dateien herunterzuladen**, ob ein entnehmbares app-spezifisches Speichervolumen sichtbar und eingehängt ist. Sie liest dessen freien Speicher sowie optional den freien internen App-Speicher. Das Erfolgsergebnis bedeutet ausschließlich: **Platz für einen späteren Dateitest vorhanden**, nicht „KI installiert“, nicht „Modell direkt von SD ladbar“ und nicht „4 GB RAM reichen“.

Reiner Preflight-Code: `inference/SdStoragePreflight.kt`, JVM-Grenztests: `D04SdStoragePreflightTest.kt`. Als vorsichtige Reserve gilt im Probe-Modus **2 GiB freier SD-Platz** (Projektwert, keine Android-Vorgabe). Die Nutzerangabe „20 GB frei“ wird erst durch den tatsächlichen Button-Lauf bestätigt beziehungsweise korrigiert.

### Implementationspfad A (bevorzugt für ein von der App verwaltetes Modell)

1. Über `Context.getExternalFilesDirs(null)` das tatsächlich eingehängte SD-App-Verzeichnis ermitteln, mit `Environment.isExternalStorageRemovable(dir)` von emuliertem „external“ unterscheiden. Freien Platz mit `StatFs` **auf dem gewählten Volume** messen, Status `Environment.MEDIA_MOUNTED` prüfen. Nicht annehmen, dass jedes Gerät eine beschreibbare SD-App-Directory anbietet.
2. Einen ausdrücklich vom Nutzer bestätigten Download direkt in die **app-spezifische SD-Directory** schreiben; `.partial`-Datei ebenfalls **auf der SD-Karte**, am Ende Hash-/Größenprüfung und kontrollierte Umbenennung auf demselben Volume. Nicht in internen `cacheDir` herunterladen/entpacken und dann zur SD verschieben. Kein unnötiger zweiter vollständiger Modell-Download.
3. Die native Runtime mit dem tatsächlichen App-SD-**Dateipfad** laden; vorab kleinformatigen End-to-End-Probeversuch mit Seek-/mmap-Zugriff auf diesem Gerät durchführen. Bestehende Chat-Datenbank bleibt im internen App-Sandbox-Speicher, falls SD entfernt wird.
4. Bei `SD_MISSING`, `NO_SPACE`, `FILE_READ_ERROR`, falschem SHA, nicht seekbarer Datei oder OOM eindeutig abbrechen und Modell-/Zwischendateien sicher bereinigen. **Niemals** still auf intern oder Cloud umschalten; Chatentwürfe behalten. Hinweis in der Oberfläche, dass App-spezifische SD-Dateien beim Deinstallieren verloren gehen können.

Android-Quelle: [App-specific storage](https://developer.android.com/training/data-storage/app-specific). Auf Android 13 sind für die app-spezifischen SD-Verzeichnisse **keine allgemeinen Speicherberechtigungen** erforderlich; die Karte kann dennoch entfernt oder unzugänglich sein.

### Implementationspfad B (nutzerausgewählte .gguf-Datei)

Über `ACTION_OPEN_DOCUMENT` wählt der Nutzer die Modell-Datei selbst aus. Die App erhält einen `content://`-URI und kann eine dauerhafte Leseberechtigung mit `takePersistableUriPermission` anfordern. Das **garantiert keinen direkt an eine native Modellbibliothek übergebbaren Dateipfad**. Wir müssen prüfen, ob die Runtime den bereitgestellten `ParcelFileDescriptor` **seekbar** lesen kann; sonst B als inkompatibel kennzeichnen, statt heimlich intern zu kopieren. Auch die SD-Root-Directory-Auswahl ist unter Android 11+ eingeschränkt, daher keine breite Root-Berechtigung verlangen.

Android-Quelle: [Storage Access Framework](https://developer.android.com/training/data-storage/shared/documents-files).

### Neue Stop/Go-Gates vor dem Test auf dem Nutzergerät

- **Freier SD-Speicher vom Nutzer bestätigt:** ca. 20 GB – ausreichend für den geplanten 0.6B-Modell-Dateitest. Vor einem Download zusätzlich **on-device** Speicherziel, Schreibzugriff und tatsächlich verfügbaren freien Platz messen; diese Kapazitätsangabe ist keine Aussage über RAM, native Runtime-Kompatibilität oder Performance.
- **Noch kein GO:** `llama.cpp` muss in einem separaten, nachvollziehbar getesteten Android-Build die GGUF direkt von dieser Karte laden können, ohne interne Modellkopie oder kritischen zusätzlichen Cache.
- **Noch kein GO:** Interner freier Speicher von ca. 1,5 GB ist für OS/Installation riskant knapp; nach tatsächlichem Runtime-Build prüfen, wie viel internen Speicher das Testpaket/Cache braucht, ohne die 3-GB-Vorsichtsschwelle für *interne Modellinstallationen* fälschlich auf SD-Laden anzuwenden.
- **Unverändert:** 4 GB RAM sind die enge Grenze für LLM-Inferenz; keine Qwen3-1.7B/4B-Tests auf diesem Gerät, bevor die 0.6B-Testreihe stabil nachgewiesen wurde.
- **Datenschutz:** Keine privaten Chats als Benchmarks, keine allgemeinen Storage-/ALL_FILES_ACCESS-Berechtigungen, keine heimliche Cloudnutzung, kein Modell-Download ohne gesonderte Zustimmung.

## Daten, die für den konkreten Test noch benötigt werden

- **RAM: 4 GB laut Nutzer bestätigt.** Nicht erneut als offene Frage führen; tatsächlich verfügbarer Arbeitsspeicher im Lauf muss separat gemessen werden.
- **Android 13 ist bestätigt.** Noch offen sind die genaue Modellnummer (z. B. SM-A326B), Buildnummer und der frei verfügbare Speicherplatz.
- Für technische Messungen: ABI, Kernel/Build, Energiesparmodus, Temperatur-/Thermalstatus und getestete Runtimeversion (nur in autorisierter Testumgebung; keine privaten Kennungen, Chats oder Geräteseriennummern speichern).

**Kein automatisches Modell-Deployment, keine produktive Aktivierung und kein Cloudzugriff ohne separate Freigabe.**
