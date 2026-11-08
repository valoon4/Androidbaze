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
7. **Preflight auf 4-GB-Gerät:** freien Speicherplatz (Modell plus sicherer Reserve), verfügbare RAM-Menge und Android-`ActivityManager.isLowRamDevice`/`MemoryInfo.lowMemory`, CPU-ABI und laufende Energiesparmaßnahmen zuerst prüfen; bei knappen Ressourcen **keine Modellladung**. Die 4 GB sind Gesamtspeicher und stehen einer App nicht vollständig zur Verfügung. Keine automatische RAM-Plus-Erhöhung und keine riskanten OOM-Stresstests auf privaten Geräten.\n8. **Kandidatentests sind keine D04-Fertigmeldung:** `InferenceGateway` und Offline-Evidenzprüfung aus PR #61 sind nutzbar, aber echte On-device-Lab-Integration und Messung bleiben separate noch nicht autorisierte Hardware-/Runtime-Arbeiten.

## Gerätespeicher: Nutzer hat aktuell nur ungefähr 1,5 GB frei

**Vom Nutzer bestätigt, 08.11.2026:** ca. **1,5 GB freier interner Speicher**. Das ist **Speicherplatz**, nicht RAM. **Produktentscheidung für den ersten Test:** **Kein Modell-Download / keine lokale KI-Installation auf diesem Gerät**, solange dieser geringe freie Platz besteht. D04-Debug-APK bleibt weiterhin eine reine Simulation ohne Modellgewichte.

- Realistische Größe eines Qwen3-0.6B-Q4_K_M-GGUF-Artefakts: je nach Quantisierungsquelle grob **397–484 MB**, **ohne** Runtime, Installationsdateien, Cache und vorübergehend doppelte Dateien. Beispieldaten: [Hugging Face prithivMLmods](https://huggingface.co/prithivMLmods/Qwen3-0.6B-GGUF) und [TensorBlock](https://huggingface.co/tensorblock/Qwen_Qwen3-0.6B-GGUF).
- Die Native-Runtime-Größe hängt stark von Compiler-Flags, ABI und eingeschlossenen CPU-/GPU-Bibliotheken ab. Dritte dokumentieren für einen kleinen CPU-nahen Android-Build **ca. 20 MB** an nativen Bibliotheken und für eine breite GPU-/VLM-Konfiguration ein **ca. 95–100-MB-APK**. Dies sind **keine** Messergebnisse unseres zukünftigen Builds. Quellen: [BitNet Android JNI](https://github.com/abhikumar08/react-native-bitnet/blob/main/native-build.md) und [llama.cpp Android Prebuilt](https://github.com/xentron-bit/llama-android-prebuilt).
- **Vorläufiges Vorsichtsgate (Projektentscheidung, keine Android-Systemvorgabe):** Mindestens **3 GB internen Speicher frei** vor einem Modell-Installations- oder CPU-Test. Zuerst exakten Download- und Entpack-/Umbenennungsablauf implementieren und messen. Download muss abbrechbar sein, vorab freien Speicher und eine Notreserve prüfen, fehlgeschlagene Dateien aufräumen und darf niemals App-Nachrichten oder bestehende Datensätze löschen.
- Modell-Download **nur bewusst durch den Nutzer**, niemals automatisch beim Öffnen eines Chats. Keine gerätespezifische Stresstest-Freigabe allein aus Dateigröße ableiten; 4-GB-**RAM**-Druck bleibt separat kritisch.
- Das Galaxy A32 5G unterstützt je nach Ausführung microSD (Herstellerangabe). Ein externer Modelldatei-Speicherort könnte später geprüft werden, aber ist **in D04 weder implementiert noch als laufzeitkompatibel getestet**. Nicht behaupten, dass die vorhandene App oder das Modell sofort auf eine SD-Karte verschoben werden kann.

**Alternative für die Entwicklung:** Model- und Runtime-Benchmarks vorläufig auf einem separaten Rechner oder Gerät mit ausreichend freiem Speicher durchführen und nur anonymisierte Messmetadaten übernehmen. So bleibt das Nutzergerät geschützt. Cloud-Broker-Tests bleiben unabhängig davon bis zu gesonderter Freigabe gesperrt.

## Daten, die für den konkreten Test noch benötigt werden

- **RAM: 4 GB laut Nutzer bestätigt.** Nicht erneut als offene Frage führen; tatsächlich verfügbarer Arbeitsspeicher im Lauf muss separat gemessen werden.
- **Android 13 ist bestätigt.** Noch offen sind die genaue Modellnummer (z. B. SM-A326B), Buildnummer und der frei verfügbare Speicherplatz.
- Für technische Messungen: ABI, Kernel/Build, Energiesparmodus, Temperatur-/Thermalstatus und getestete Runtimeversion (nur in autorisierter Testumgebung; keine privaten Kennungen, Chats oder Geräteseriennummern speichern).

**Kein automatisches Modell-Deployment, keine produktive Aktivierung und kein Cloudzugriff ohne separate Freigabe.**
