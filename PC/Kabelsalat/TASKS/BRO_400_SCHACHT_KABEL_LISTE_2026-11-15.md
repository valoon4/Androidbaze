# Bro · 400 Schächte → belegte Kabelverläufe → Übergabeliste

**Bro-Feedback, 15. November 2026 (sinngemäß):** „muss ne liste machen so kp wie hab diese 400 schichte und am besten der ki geben such mir die schächte raus die kabel raus wo die durchlaufen und diese liste muss ich dann ihm(?) geben“.

## Arbeitsziel

Aus rund **400 Schacht-IDs** und den Plänen/Beleglisten zu Kabeln entstehen zwei **empfängerfertige Kreuztabellen**: pro Kabel seine nachweisbar dokumentierte Schachtfolge und pro Schacht die in den Eingabedaten erwähnten Kabel. Zusätzlich eine Liste offener Zuordnungen. Die tatsächlichen 400 Schachtlisten/Bestandspläne wurden dem Repository **nicht zur Verfügung gestellt**; dort sind folglich **keine echten Kabelverläufe automatisch erkannt**.

### Schritt 1 – Schachtstammliste erfassen

Eine Excel-Tabelle als UTF-8-CSV (Semikolon). Jede Zeile eine stabile, eindeutige Schacht-ID.

```csv
schacht_id;schachtname
S001;Schacht Nord
S002;Schacht Mitte
S003;Schacht Süd
```

### Schritt 2 – Kabelbelege extrahieren (ggf. mit ChatGPT)

Eine zweite CSV – eine Zeile je dokumentierter Kabel/Schacht-Zuordnung, inklusive Reihenfolge und **Dokumentfundstelle**.

```csv
kabel_id;kabelname;schacht_id;reihenfolge;quelle;status
K01;Kabel Nord–Süd;S001;1;Plan A Seite 4;PLAN
K01;Kabel Nord–Süd;S002;2;Plan A Seite 5;PLAN
K01;Kabel Nord–Süd;S003;3;Plan A Seite 6;KANDIDAT
```

**Status:** `PLAN` = dokumentiert im Plan, `KANDIDAT` = unsichere Zuordnung, `VOR_ORT` = vom Menschen ausdrücklich als Vor-Ort-Angabe beigefügt. Keiner dieser CSV-Statuswerte stellt schon eine physische Durchgangs- oder Brandschutzfreigabe dar. CSV-Zeilen ohne Beleg oder mit unbekannter Schacht-ID werden abgelehnt.

**KI-Schritt:** In Kabelsalat steht ein **„Auftrag für KI (TXT)“** zum Herunterladen bereit. Den Auftrag mit echten Plan-/Schachtdateien in ChatGPT einreichen. Die lokale Offline-App **kontaktiert kein KI-Modell und liest nicht selbst binäre PDF-Pläne**. Nur aus einer Liste mit 400 Schachtnamen lässt sich der Kabelverlauf **nicht** zuverlässig bestimmen. Fragen offen lassen statt aus geografischer Nähe oder ähnlich klingenden IDs Durchgänge zu erfinden.

### Schritt 3 – 400er-Auswertung im Kabelsalat

Im Abschnitt **„Bros 400-Schacht-Liste“** die beiden CSV-Dateien hochladen oder die Tabellen einfügen. Mit **„Liste auswerten“** werden bis **500 Schächte** und **25.000 Kabel/Schacht-Zuordnungen** unabhängig vom 3D-Editor geprüft. Das vorhandene 3D-Projektnetz behält aus Stabilitätsgründen seine **30-Schacht-Grenze**. Es wird **nicht** stillschweigend aus 400 Zeilen ein 3D-Netzmodell erzeugt.

Kontrollen: eindeutige IDs, bekannte Schächte, belegte Quellen, numerische Laufreihenfolge, keine widersprüchlichen Mehrfachpositionen, fehlende Kabelstrecken, überzählige/fehlende Reihen, Excel-CSV-Injektion, UTF-8/BOM und Quoting. Suchfeld kann eine Kabel-ID mit allen zugeordneten Schächten oder eine Schacht-ID mit den dort gelisteten Kabeln finden. Keine Änderung an Originalrohren, Betondurchbrüchen oder Referenzdateien.

### Schritt 4 – Drei Listen weitergeben

| Export | Inhalt |
|---|---|
| **Kabel → Schächte** | Kabel-ID, Name, Schacht-ID, Reihenfolge, Rolle, Quellenbeleg, Status, vollständige dokumentierte Schachtfolge |
| **Schacht → Kabel** | Für **jeden** Schacht ID, Name, Kabelanzahl und referenzierte Kabel-IDs – auch leere Schächte als „keine Zuordnung eingetragen“, **nicht** als „physisch leer“ |
| **Offene Fragen** | Schächte ohne Kabelbeleg, Plan-/Kandidaten-Einträge, fehlende Reihenfolge und Kabel mit nur einer bekannten Schachtposition |

Dateien sind für Excel / Übergabe an den Kollegen gedacht und ausdrücklich **nicht** als Ausführungs- oder Belegungsgutachten gekennzeichnet.

## Umsetzung und technische Gates

- [Schachtlisten-/Kabelweg-Analyzer](../shaft-handover.js)
- [CSV-Upload, Suchfeld, drei CSV-Exporte und KI-Auftrag in der App](../index.html)
- [App-Verkabelung](../app.js)
- [89 bestandene gezielte Regressionen an realen GitHub-Dateien](../tests/shaft-handover.test.js), darunter eine vollständig fiktive **400-Schacht-Probe**, 400 Stufen eines Kabelverlaufs, doppelte IDs, Orphans, ungeklärte Verläufe, Excel-Formel-Injektion und Quellpflicht
- `npm test`, Electron-Windows-Paket und Offline-Einzeldatei enthalten das neue Modul. **Aktueller echter Browser-/Windows-CI-Gesamtlauf noch gesondert zu prüfen.**

**Backlog-Status:** Diese neue Rückmeldung ist als eigene Anforderung dokumentiert. Sie erhöht weder die 888 historischen Parent-IDs noch macht sie ohne einzelne Abnahme die nächsten 100 Parents automatisch erledigt.
