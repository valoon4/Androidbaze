# D04 – Zielgerät Galaxy A32 5G: erster On-Device-KI-Test

**Stand:** 2026-11-08. **Kein tatsächlicher Modelltest erfolgt.** Gerät vom Nutzer genannt: Samsung Galaxy A32 5G. Tatsächlich verbauter RAM, Android-Build, ABI und verfügbarer Speicher **noch nicht am Gerät erfasst**.

## Herstellerbasis

- Samsung Galaxy A32 5G (Modellvarianten der Serie SM-A326), Markteinführung 2021. CPU-Plattform: **MediaTek Dimensity 720 / MT6853**. Referenz: [Samsung A32 5G Specs PDF](https://image-us.samsung.com/SamsungUS/samsungbusiness/pdfs/spec-sheet/Galaxy-A32-5G-Series-Spec-Sheet-Unlocked.pdf).
- CPU: **2× Cortex-A76 und 6× Cortex-A55**, GPU **Mali-G57 MC3**. Referenz: [MediaTek Dimensity 720 Product Brief](https://mediatek-marketing.files.svdcdn.com/production/documents/MediaTek-Dimensity-720-Product-Brief-PDFMD720PB-0720.pdf?dm=1684470668).
- Varianten besitzen je nach Markt **4 GB oder 6 GB RAM** (Samsung US nennt 4 GB, Samsung Hongkong 6 GB). Konkrete Nutzerkonfiguration **unbekannt**. Referenzen: [Samsung USA](https://www.samsung.com/us/business/support/owners/product/galaxy-a32-5g-unlocked/), [Samsung HK](https://www.samsung.com/hk_en/news/product/samsung-launches-mid-tier-smartphone-galaxy-a32-5g/).
- Android 13 wird für SM-A326B in offiziellen [Samsung Software-Update-Verläufen](https://doc.samsungmobile.com/SM-A326B/018740210504/eng.html) dokumentiert. Die *installierte* Version des Nutzergeräts ist nicht bestätigt.

## Konservatives Testprofil (4-GB-Fall annehmen)

**Ziel:** Nachweisen, ob kleine lokale Inferenz überhaupt stabil ist, ohne App-/Android-Prozesse durch Speicher- oder Thermikdruck zu stören. Keine behaupteten Antwortzeiten, Tok/s oder Qualität.

1. **CPU-first mit ARM64-Android-Runtime** (z. B. nach Prüfung `llama.cpp`), zunächst **Qwen3-0.6B** in geprüfter Q4_K_M-GGUF-Version. Die GGUF-Datei ist grob **0,4–0,5 GB** groß, abhängig vom konkreten quantisierten Artefakt. Runtime-/Model-Lizenz, SHA-256 und Chat-Template vor Installation prüfen. Keine unüberprüften Drittgewichte, keine Modellfiles im Git.
2. Start mit **max. 1.024 Kontexttoken**, später 2.048 als getrennte Messung; 2 und 4 CPU-Threads getrennt testen. Weder GPU- noch NPU-Beschleunigung voraussetzen.
3. **Messprotokoll:** Cold Start, TTFT, Gesamtdauer p50/p95, Tok/s, maximale Prozess-/System-RAM-Nutzung, thermische Drosselung und Akku-/Abbruchverhalten. Testumfang/Antwortlängen und exakt installierten Android-Build dokumentieren. **Vorläufig keine Mindestgeschwindigkeit vorgeben**.
4. **Sicherheit:** ausschließlich synthetische Gespräche aus K5, kein Netzwerk-Fallback, keine API-Schlüssel, kein absichtliches Überschreiben vorhandener D03/D04-Daten, keine auto-startenden Modell-Downloads. Bei OOM / nicht ausreichend Speicher verständliche Meldung und Abbruch ohne Nachrichtenverlust.
5. **Qualitätsprüfung separat:** natürliches Deutsch, respektvolle Grenzen, stabile Rollen, keine erfundenen Erinnerungen. Ein schneller 0.6B-Chat ist nicht automatisch als sozial verantwortungsvolle Trainings-KI ausreichend.
6. **Qwen3-1.7B:** erst als optionalen zweiten Versuch nach tatsächlicher RAM-Prüfung und stabiler 0.6B-Baseline. **Qwen3-4B:** auf dem A32 5G vorerst nicht priorisieren; für ein besser ausgestattetes Referenzgerät vorsehen.
7. **Kandidatentests sind keine D04-Fertigmeldung:** `InferenceGateway` und Offline-Evidenzprüfung aus PR #61 sind nutzbar, aber echte On-device-Lab-Integration und Messung bleiben separate noch nicht autorisierte Hardware-/Runtime-Arbeiten.

## Daten, die für den konkreten Test noch benötigt werden

- Tatsächliche **RAM-Größe** (nicht „RAM Plus“/virtueller RAM).
- Android-Version, Modellnummer (z. B. SM-A326B) und derzeit frei verfügbarer Speicherplatz.
- Für technische Messungen: ABI, Kernel/Build, Energiesparmodus, Temperatur-/Thermalstatus und getestete Runtimeversion (nur in autorisierter Testumgebung; keine privaten Kennungen, Chats oder Geräteseriennummern speichern).

**Kein automatisches Modell-Deployment, keine produktive Aktivierung und kein Cloudzugriff ohne separate Freigabe.**
