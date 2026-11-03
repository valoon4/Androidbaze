# M7.5.0 – Entscheidung: freiwillige Visualisierungen im Rückblick

Stand: 03.11.2026. **Designentscheidung auf Basis fiktiver Beispiele und Ist-Architektur; keine Nutzertests, manuellen Android-Gerätetests oder Fach-/Security-Audits durchgeführt.** M7.4.4 (PR #37) ist gemergt. Auf ausdrücklichen Wunsch werden manuelle Abnahmegates bei der *weiteren Entwicklung* übersprungen, nicht als bestanden dargestellt. Ein späterer Release braucht eine separate Risikoentscheidung.

## Ausgangslage und Leitplanken

M7.1 zeigt bereits exakte Nennungen, Zeiträume und konkrete Belege; M7.2 zeigt vorsichtige Kombinationen samt Zähler/Nenner; M7.3 und M7.4 bleiben persönliche, freiwillige Texte und Versuche. Eine zusätzliche Grafik darf **keine weitere Interpretation** vornehmen und darf die Belegnavigation nicht verschlechtern. Keine neue Hauptnavigation, keine Kalender- oder Fortschrittspflicht, keine externen Dienste und keine zusätzliche Speicherung. Der Tagebuchkalender ist eine **freiwillig aufrufbare Navigationshilfe** zu vorhandenen Einträgen, kein Gewohnheits-Tracker.

**Fiktives Beispiel (keine realen Nutzerdaten):** 10 Check-ins innerhalb 30 Tagen. „Anspannung“ in 6 Einträgen, „Ruhe“ in 3, „Überforderung“ in 2; pro Eintrag können mehrere Begriffe gewählt werden, also ergeben Summen nicht 10. Acht Tage mit Einträgen, 22 Tage ohne Eintrag. „Kein Eintrag“ heißt weder „keine Gefühle“ noch „guter Tag“. Vergleiche beziehen sich nur auf dokumentierte Nennungen, nie auf die wahre Häufigkeit oder Intensität von Erleben.

## Vergleich und Entscheidung

| Variante | Mehrwert gegenüber bestehendem Text | Missverständnis-/UX-Risiko | Entscheidung |
|---|---|---|---|
| A: kleine **horizontale Balken** neben exaktem Begriff + Nennungszahl; die *gesamte Zeile* öffnet bisherige Belege | Größe und Rangfolge der dokumentierten Nennungen sind schneller sichtbar, Text bleibt vollständig | Zahlen können als „Gefühlsstärke“ missverstanden werden; lange Labels bei 320 dp | **Bauen, M7.5.1**, aber nur optional aufklappbar; Zahlen und Beleglinks stets gleichwertig, keine Prozent-Summen |
| B: neutrale **Wochenzahlen** als chronologische Liste, optional winziger Balken „Einträge“ statt „Stimmung“ | Erleichtert Orientierung an Dokumentationslücken und Zeiträumen | Wochen mit wenig Dokumentation als emotionale Entwicklung interpretierbar; konkurrierende Größen „Nennungen“ vs. „Check-ins“ | **Vereinfachen und zurückstellen**: zuerst reine Text-/Zahlenansicht, später nur bei überprüfbarem Nutzen M7.5.2 |
| C: **neutraler Tagebuchkalender** (Monatsansicht, pro Tag nur Anzahl gespeicherter Einträge) | **Direkter, vertrauter Weg zu einem bestimmten Tag und seinen Tagebucheinträgen**, besonders bei längerer Nutzung | Streak-Anmutung, Leistungsdruck, enge Touch-Ziele und TalkBack-Grid | **Bauen in M7.5.2** als optionalen Navigationsweg; ohne Wertung, Serien, Belohnungen oder Pflicht zum Eintragen. Eine gleichwertige chronologische Textliste bleibt erreichbar. |

**Ergänzung Musterbelege:** Für M7.5.1 nur ein sehr dezenter proportionaler Anteil *Treffer / korrekter Nenner*, beide Zahlen ausgeschrieben und nur zusammen mit „Wie entsteht diese Beobachtung?“ und Quellbelegen. Nicht erforderlich, falls der Balken den Erklärungstext verdrängt.

## Textuelle Mockups (fiktive Werte)

**A – optional geöffnet, keinerlei neue Kategorie**

> Häufig genannte Begriffe in deinen Einträgen (30 Tage)  
> Anspannung  ██████  6 Nennungen · Einträge ansehen  
> Ruhe        ███     3 Nennungen · Einträge ansehen  
> Überforderung ██    2 Nennungen · Einträge ansehen  
> Gezählt werden ausgewählte Begriffe in gespeicherten Einträgen. Mehrere Begriffe pro Eintrag sind möglich.

Bei 320 dp und 200 % Schrift umbrechen Label, Balken und Zahl **vertikal**, ohne Abschneiden; ohne Platz nur Label + Zahl + Belegaktion, Balken entfällt. Balken ist dekorative redundante Information; Screenreader liest Label + exakte Zahl + Zeitraum + Aktion *einmal*.

**B – zunächst vereinfachte chronologische Textansicht**

> Woche 1: 3 dokumentierte Einträge, 2 Nennungen „Anspannung“  
> Woche 2: 0 dokumentierte Einträge – keine Aussage über Erleben möglich  
> Woche 3: 4 dokumentierte Einträge, 3 Nennungen  
> Woche 4: 3 dokumentierte Einträge, 1 Nennung

Keine geglättete Kurve, kein Farb-„Trend“, keine Aussage „mehr/weniger Anspannung“ ohne Abdeckung. Wochen an lokalem Kalender ausrichten; Zeitzone und DST-Grenzen testen.

**C – M7.5.2: optionaler Tagebuchkalender, Navigation statt Bewertung**

> Tagebucheinträge nach Datum finden · November 2026  
> Vorheriger Monat · Nächster Monat  
> Mo · Di · Mi · Do · Fr · Sa · So (neutrales Monatsraster)  
> 03. Nov: 2 gespeicherte Einträge · Einträge ansehen  
> 04. Nov: kein Eintrag · über das Erleben ist nichts bekannt  
> Alternativ: Chronologische Datumliste anzeigen

Nur ein neutraler Punkt oder eine Zahl kennzeichnet vorhandene Einträge. **Kein Rot/Grün, kein „perfekter Monat“, keine Streaks, keine Erinnerungsaufforderung**. Leere Tage dürfen keine negative grafische Hervorhebung erhalten.

## M7.5.2 – Tagebuchkalender: verbindlicher UX-/Datenvertrag

**Produktzweck:** Bestehende eigene Einträge über ein bekanntes Datum wiederfinden, *nicht* Regelmäßigkeit oder psychische Entwicklung messen. Der Kalender ist freiwillig unter dem bestehenden Rückblick verfügbar; keine neue Hauptnavigation, keine Aufforderung zum täglichen Eintragen. Die bestehende chronologische Tagebuchliste und direkte Eintragsnavigation bleiben unverändert nutzbar.

### Interaktionsverhalten

1. Kalenderbereich ist initial geschlossen; „Tagebuch nach Datum finden“ öffnet eine Monatsansicht. Vorheriger/nächster Monat wechseln lediglich den angezeigten Zeitraum, **ohne automatische Speicherung**. Die Monatsüberschrift nennt Monat und Jahr ausdrücklich.
2. Eine Tageszelle zeigt Datum und – falls vorhanden – die **Anzahl gespeicherter Check-ins/Tagebucheinträge**; bei null Einträgen weder Bewertung noch „Versäumt“-Status. Antippen eines Tages zeigt „N Einträge an diesem Tag“, bei N > 0 einen klaren Zugang **„Einträge ansehen“** zu den **tatsächlich gespeicherten IDs**; bei N = 0 nur den neutralen Leerhinweis.
3. Rücknavigation vom Eintrag führt nachvollziehbar zum zuvor gewählten Kalenderdatum/Monat zurück, solange die private Sitzung sicher geladen ist. Ein Filter der Statistik (7/30/90 Tage) darf die Monatsansicht nicht *stillschweigend* beschneiden: Kalender arbeitet mit allen sicher geladenen Journaleinträgen im gewählten lokalen Monat und sagt dies deutlich.
4. Neben dem Raster steht „Als Datumliste ansehen“. Diese Alternative enthält **dieselben Daten, Daten/Zahlen und Links**, chronologisch und vollständig, ohne visuelle Markierungspflicht. Für große Schrift/schmale Ansichten darf **automatisch die Liste** statt eines unbedienbaren Rasters verwendet werden.
5. Keine Ampelfarben, Intensitäts-/Stimmungscodes, Tagesnoten, Fortschrittsanzeige, Streaks, Belohnungen, „Lücken schließen“-CTA, Benachrichtigung oder automatische Eintragserstellung. Die Anzeige ist rein lokal und lesend.

### Korrekte Daten und Datenschutz

- Aggregation ausschließlich aus **authentifiziert geladenen** bestehenden Journal-Einträgen; lokale Tage/Monate gemäß derselben Zeitzonen- und DST-Definition wie der Rückblick. Mehrere Einträge pro Tag werden korrekt gezählt; ein Tag ohne Eintrag bleibt **unbekanntes Erleben**, nicht „kein Gefühl“.
- Es gibt keine zweite persistierte Kalendertabelle, keine unverschlüsselte Zusammenfassung, keine separaten Statistiken im Backup, keine Netzwerkanfrage. Nach Sperren, fehlerhafter Entschlüsselung oder Datenlöschung werden private Zahlen und Eintragslinks sofort ausgeblendet. Editieren, Löschen, Import oder Wiederherstellung invalidieren die flüchtige Monatsansicht.
- Bei sehr vielen Einträgen (bis zum bestehenden Limit) performant aus Journal-Snapshot ableiten, ohne alle Tage als riesige Compose-Liste vorzuhalten. Zeitgrenzen und lokale Sommerzeitumstellungen mit fiktiven Einträgen prüfen.

### Accessibility und automatisierbare Abnahme

- Jede aktivierbare Tageszelle hat eine verständliche TalkBack-Semantik: **Wochentag, vollständiges Datum, N gespeicherte Einträge, Aktion** (sofern vorhanden). Die Anzeige darf nie nur über Farbe oder Punkt vermittelt werden. Mindestens 48 dp Touch-Ziel oder statt engem Raster zwingend die gleichwertige Liste.
- CI: 0/1/10/100/1000/3000 fiktive Einträge; Monatswechsel über Jahresgrenze; DST; mehrfacher Check-in am selben Tag; Löschen/Ändern; Daten-Sperre/Fehler; Textlisten-Parität; korrekte ID-Navigation und System-Zurück; Compose-Tests soweit automatisierbar bei 320 dp und 200 % Schrift.
- Manuelle TalkBack-/Dark-Mode-/Geräteabnahmen und die fünf Nutzersessions sind auf Wunsch **übersprungen, nicht bestanden**. Vor tatsächlicher Release-Freigabe Risiko erneut benennen, nicht durch grüne CI ersetzen.

## Akzeptanzplan M7.5.1 – nur automatisierbar, ohne behauptete manuelle Abnahme

1. **Wertgleichheit:** alle Nennungszahlen und stabilen Beleg-IDs werden unverändert vom bestehenden authentifiziert geladenen Rückblick übernommen; keine zweite Berechnung und keine zusätzliche Datei.
2. **Opt-in:** Rückblick bleibt standardmäßig die bisherige textuelle Darstellung; Grafik ist freiwillig ein-/ausblendbar und verschwindet beim Sperren/unsicheren Laden.
3. **Zugänglichkeit:** Zahl/Begriff/Zeitraum und „Einträge ansehen“ ohne Farbannahmen; Text ist eigenständig, auch wenn Balken nicht angezeigt wird. Automatisierte Semantiktests für die Aktionsfläche.
4. **Grenzen:** 0/1/10/100/1000/3000 fiktive Einträge, Mehrfachnennungen, Filter, gespeicherte und gelöschte Einträge, lange Labels, 320 dp/200 % Schrift soweit durch CI automatisiert prüfbar.
5. **Transparenz:** erläuternder Satz über Nennungen statt objektiver Gefühlsmessung. Keine Vorschläge, Diagnosen, Kausalitäts- oder Fortschrittsaussagen.
6. **Noch nicht nachgewiesen:** visuelle Geräteabnahme, TalkBack-Hörprüfung, Dark Mode/Rotation und fünf freiwillige Nutzersessions. Diese Anforderungen bleiben offen, auch wenn automatisierte Tests grün sind.

## Umsetzung in separaten PRs

- **M7.5.1:** opt-in Mini-Balken für die vorhandene Begriffsübersicht; optional minimaler Beleg-Anteil in bestehenden Musterkarten, nur sofern risikofrei und textgleich. Datenquelle und Belegaktion wiederverwenden. Eigene CI + Debug APK, kein automatischer Merge.
- **M7.5.2:** optionaler neutraler Tagebuchkalender **implementieren**, daneben vereinfachte Wochenübersicht als Text statt Stimmungsdiagramm. Eigenständiger Implementierungs-PR mit automatisierten Tests und Debug-APK; kein automatischer Merge.
- **M7.6:** automatisierbare Regressionen und offenes Restrisiko dokumentieren; wegen übersprungener manueller Prüfungen keinen vollständig geprüften Release behaupten.

## Konkrete UX-Erfolgskriterien für spätere, freiwillige Studien

Fünf Teilnehmende mit **ausschließlich fiktiven** Daten beantworten ohne Anleitung: (1) „Wofür steht die 6?“ (korrekt: Nennungen im ausgewählten Zeitraum), (2) „Sind 22 Tage ohne Eintrag gute Tage?“ (korrekt: unbekannt), (3) „Wie öffne ich die Belege?“ (aus der gleichen Zeile), (4) „Wie schalte ich die Grafik wieder aus?“, (5) „Wie finde ich einen früheren Eintrag im Kalender ohne einen fehlenden Tag als Misserfolg zu verstehen?“. Gegen bestehende Textansicht vergleichen; Grafik nur behalten, wenn Verständnis nicht sinkt und Bedienung verbessert wird. **Nicht durchgeführt.**
