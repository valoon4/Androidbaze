# M6.2 – Wissenswelt: frei lesbare Artikel und geführte Lernreihen

**App:** Emotionskompass 0.6.2-m6.2, versionCode 13, weiterhin Android 8+, offline und ohne INTERNET-Permission.  
**Inhalt:** 20 deutschsprachige eigenständig formulierte Lernartikel in vier Themenwelten plus vier geführte Lernreihen à fünf Lektionen.

## Umsetzung

- **Gefühle verstehen:** Signalfunktion von Gefühlen, differenzierter Wortschatz, gleichzeitige Gefühle, Angst/Ärger, körperliche Signale.
- **Bedürfnisse erkennen:** Grundlagen, Zusammenhang mit Gefühlen, Nähe/Zugehörigkeit, Autonomie/Grenzen, unerfüllte Bedürfnisse.
- **Strategien & Kommunikation:** Bedürfnisse vs. Strategien, Handlungsalternativen, Bitte vs. Forderung, Beobachtung vs. Interpretation, Wirkung von Strategien.
- **Selbstwahrnehmung:** Einstieg in die Reflexion, Gedanken vs. Gefühle, sanfte Achtsamkeit, Selbstmitgefühl, kleine nächste Schritte.

Jeder Artikel hat einen verständlichen Einstieg, mindestens drei Abschnitte, Alltagsbeispiel, Kerngedanken, freiwillige Reflexionsfrage sowie explizite externe Quellenlinks. Die Lesezeit wird aus dem Text geschätzt. Alle Texte sind in `app/src/main/assets/lessons/knowledge.de.v1.json` enthalten, **nicht aus dem Internet nachgeladen**. Der Artikel des Tages richtet sich nach dem lokalen Datum; keine Pflicht, kein Streak.

Im vorhandenen Bereich „Lernen & Entdecken“ gibt es einen zusätzlichen Tab **Wissenswelt**. Innerhalb der Wissenswelt: freie Bibliothek mit Volltextsuche/Themenfilter, tägliche Empfehlung, Lesezeichenliste und geführte Lernreihen. Reihen sind **nicht gesperrt**: Alle Kapitel können in beliebiger Reihenfolge gelesen werden. Der Fortschritt wird nur durch die ausdrückliche Aktion „Als gelesen markieren“ aktualisiert. Im Artikel führt „Nächste Lektion“ zur folgenden Station der Reihe.

## Daten & Migration

M6.2 ergänzt `LearningState` um die Mengen `articleBookmarks` und `readArticles`. Beide werden im **bestehenden AES-256-GCM-verschlüsselten** `learning_progress_v1.enc` gespeichert. Das `LearningProgressFormat` bleibt bei Schema 1 und liest neue Felder **optional**, damit alte M6-Lernstände ohne Datenverlust kompatibel sind. Keine personenbezogenen Artikelantworten werden gespeichert. Ausdrückliches Zurücksetzen des Lernstands entfernt auch diese Markierungen, niemals das separate Tagebuch.

Der Passwortexport von Tagebucheinträgen bleibt unverändert und enthält weiterhin **keine Lern- oder Artikelfavoriten**. Bei aktivierter Gerätezugangssperre werden Lernmetadaten wie bisher beim Sperren aus dem UI-Cache geleert.

## Inhaltliche Standards und Grenzen

Quellen zur Orientierung (nicht als automatisch bestätigte Wirksamkeitsbelege für einzelne Übungen):
- Emotionsdifferenzierung: https://pubmed.ncbi.nlm.nih.gov/40816203/
- Emotionsregulation: https://pubmed.ncbi.nlm.nih.gov/31961170/
- Interozeption: https://pubmed.ncbi.nlm.nih.gov/39423429/
- Selbstmitgefühl: https://pubmed.ncbi.nlm.nih.gov/33749936/
- Bedürfnisse / Gewaltfreie Kommunikation: https://nonviolentcommunication.com/learn-nonviolent-communication/feelings/
- Bedürfnisse und Grenzen als Lernhilfe: https://wp.nyu.edu/coaching/tools/needs-inventory/
- Beobachtung, Gefühl, Bedürfnis und Bitte: https://baynvc.org/nonviolent-communication-quick-reference-guide/

Die Wissensartikel sind **redaktionelle Lernentwürfe** und benötigen noch eine fachliche Prüfung, insbesondere hinsichtlich der vereinfachenden Begriffe zu Bedürfnissen und der Grenzen allgemeiner Selbsthilferatschläge. Keine Diagnose, Therapie oder automatische Persönlichkeitseinschätzung. Die Anwendung bleibt ohne Netzwerkpermission; Quellen öffnen sich ausschließlich auf Wunsch des Nutzers in einem externen Browser.

## Qualitätssicherung

Automatisiert:
- 20 gültige Artikel, je fünf pro Themenwelt, gültige Texte/Quellen;
- 4 Lernreihen mit eindeutigen und vollständigen Artikelverweisen;
- deterministische tägliche Rotation und freie Volltextsuche;
- persistente Lesezeichen und explizite Lesemarkierungen;
- Abwärtskompatibilität zu bereits verschlüsselten M6-Lernständen;
- Ablehnung ungültiger Artikel-IDs und Katalogversionen;
- alle bestehenden M2–M6-Tests plus APK-Signatur und Paket-ID über GitHub Actions.

Manuell auf echten Geräten noch zu prüfen:
1. M6 auf M6.2 aktualisieren; alte Favoriten, Lernstand und Tagebuch müssen erhalten bleiben.
2. Wissenswelt öffnen, Artikel lesen, Schriftgröße auf 200 % setzen, TalkBack testen.
3. Suchtext und Themenfilter kombinieren, Leerzustände kontrollieren.
4. Artikel merken, App neu starten: Lesezeichen bleiben; nach Entmarkierung verschwindet der Artikel aus der Merkliste.
5. Artikel explizit als gelesen markieren; Fortschritt in Lernreihe und Übersicht aktualisiert sich.
6. Lernreihe bei Kapitel drei beginnen; dann zurück und unabhängig ein anderes Kapitel öffnen.
7. Artikel-des-Tages-Logik bei Datumswechsel und vollständig offline prüfen.
8. Ohne Browser „Quellen öffnen“ versuchen: Lesefunktion darf nicht abbrechen.
9. App-Sperre aktivieren, Hintergrundwechsel und Wiederentsperren testen.
10. Lernstand ausdrücklich zurücksetzen, danach kontrollieren, dass Tagebucheinträge unverändert sind.

**Release-Gate:** Die CI belegt Codekompilierung und JVM-Testabdeckung, keinen echten Geräte-/Accessibility-Test und keinen unabhängigen Security Audit.
