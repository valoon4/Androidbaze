# M7.5.0 – Entscheidung: freiwillige Visualisierungen im Rückblick

Stand: 03.11.2026. **Designentscheidung auf Basis fiktiver Beispiele und Ist-Architektur; keine Nutzertests, manuellen Android-Gerätetests oder Fach-/Security-Audits durchgeführt.** M7.4.4 (PR #37) ist gemergt. Auf ausdrücklichen Wunsch werden manuelle Abnahmegates bei der *weiteren Entwicklung* übersprungen, nicht als bestanden dargestellt. Ein späterer Release braucht eine separate Risikoentscheidung.

## Ausgangslage und Leitplanken

M7.1 zeigt bereits exakte Nennungen, Zeiträume und konkrete Belege; M7.2 zeigt vorsichtige Kombinationen samt Zähler/Nenner; M7.3 und M7.4 bleiben persönliche, freiwillige Texte und Versuche. Eine zusätzliche Grafik darf **keine weitere Interpretation** vornehmen und darf die Belegnavigation nicht verschlechtern. Keine neue Hauptnavigation, keine Kalender- oder Fortschrittspflicht, keine externen Dienste und keine zusätzliche Speicherung. Der Tagebuchkalender ist eine **freiwillig aufrufbare Navigationshilfe** zu vorhandenen Einträgen, kein Gewohnheits-Tracker.

**Fiktives Beispiel (keine realen Nutzerdaten):** 10 Check-ins innerhalb 30 Tagen. „Anspannung“ in 6 Einträgen, „Ruhe“ in 3, „Überforderung“ in 2; pro Eintrag können mehrere Begriffe gewählt werden, also ergeben Summen nicht 10. Acht Tage mit Einträgen, 22 Tage ohne Eintrag. „Kein Eintrag“ heißt weder „keine Gefühle“ noch „guter Tag“. Vergleiche beziehen sich nur auf dokumentierte Nennungen, nie auf die wahre Häufigkeit oder Intensität von Erleben.

## Vergleich und Entscheidung

| Variante | Mehrwert gegenüber bestehendem Text | Missverständnis-/UX-Risiko | Entscheidung |
|---|---|---|---|
| A: kleine **horizontale Balken** neben exaktem Begriff + Nennungszahl; die *gesamte Zeile* öffnet bisherige Belege | Größe und Rangfolge der dokumentierten Nennungen sind schneller sichtbar, Text bleibt vollständig | Zahlen können als „Gefühlsstärke“ missverstanden werden; lange Labels bei 320 dp | **Bauen, M7.5.1**, aber nur optional aufklappbar; Zahlen und Beleglinks stets gleichwertig, keine Prozent-Summen |
| B: **aufklappbare Wochenkarten** mit dokumentierter Eintragszahl, ausgewählten Gefühls- und Bedürfnisnennungen und direktem Eintragszugriff | Überschaubare Zeitabschnitte, Wiederfinden von konkreten Erlebnissen und freiwillige eigene Wochenreflexion | Missverständnis von Nennungen als Stimmungstrend oder fehlenden Einträgen als Versäumnis; zu viele UI-Elemente bei großer Schrift | **Bauen in M7.5.2**, primär als kompakte chronologische Text-/Zahlenkarten; Details und persönlicher Reflexionseinstieg nur auf ausdrückliches Öffnen, ohne Trendwertung |
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

**B – M7.5.2: aufklappbare Wochenübersicht (fiktive Daten)**

> **Wochenrückblick – Oktober 2026**  
> 05.–11. Okt. · 3 gespeicherte Einträge · Details anzeigen  
> 12.–18. Okt. · keine gespeicherten Einträge · Details anzeigen  
> 19.–25. Okt. · 4 gespeicherte Einträge · Details anzeigen  
> 26. Okt.–01. Nov. · 3 gespeicherte Einträge · Details anzeigen  
>
> **19.–25. Okt. – geöffnet:**  
> Gefühle (dokumentierte Nennungen): Freude 3×, Anspannung 2×  
> Bedürfnisse (dokumentierte Nennungen): Verbindung 3×, Erholung 2×  
> **Einträge dieser Woche ansehen**  
> **Freiwillig über diese Woche nachdenken**  
> Was ist mir rückblickend an dieser Woche aufgefallen? (nur als unverbindliche Frage)
>
> **12.–18. Okt. – geöffnet:** Keine Einträge vorhanden. Über das Erleben dieser Woche lässt sich daraus nichts ableiten.

Die kompakten Wochenkarten bleiben chronologisch und zeigen immer den **vollen Datumsbereich samt Jahr** bei Jahreswechsel sowie die Anzahl gespeicherter Einträge. Details werden nur bei Auswahl ausgeklappt. Kein geglätteter Verlauf, kein Farb-„Trend“, kein Wochenvergleich mit Fortschrittswertung. Jede ausgewählte Begriffsnennung bleibt anhand tatsächlich gespeicherter Einträge belegbar. Wochen folgen **Montag bis Sonntag** der lokalen Kalenderdefinition, DST-/Jahreswechselgrenzen werden geprüft.

**C – M7.5.2: optionaler Tagebuchkalender, Navigation statt Bewertung**

> Tagebucheinträge nach Datum finden · November 2026  
> Vorheriger Monat · Nächster Monat  
> Mo · Di · Mi · Do · Fr · Sa · So (neutrales Monatsraster)  
> 03. Nov: 2 gespeicherte Einträge · Einträge ansehen  
> 04. Nov: kein Eintrag · über das Erleben ist nichts bekannt  
> Alternativ: Chronologische Datumliste anzeigen

Nur ein neutraler Punkt oder eine Zahl kennzeichnet vorhandene Einträge. **Kein Rot/Grün, kein „perfekter Monat“, keine Streaks, keine Erinnerungsaufforderung**. Leere Tage dürfen keine negative grafische Hervorhebung erhalten.

## M7.5.2 – Aufklappbare Wochenübersicht und freiwillige Wochenreflexion

**Ziel:** Einträge einer Woche schnell wiederfinden, die *selbst dokumentierten* Gefühle und Bedürfnisse überblicken und optional eine **eigene Erkenntnis** formulieren. Keine automatische Wochenbewertung, kein Musterurteil und keine Fortschrittsdarstellung. Wochenübersicht und Kalender sind zwei gleichwertige Navigationshilfen im bestehenden Rückblick; keine neue Hauptnavigation.

### Kompakte und geöffnete Wochenkarten

1. Anzeige als **chronologische Wochenliste**, von Montag bis Sonntag in der lokalen Zeitzone. Jede zunächst geschlossene Karte zeigt den konkreten Datumsbereich und die **Anzahl der wirklich gespeicherten Journaleinträge**. Nutzer wählen eine Woche ausdrücklich aus; es gibt **keine automatisch geöffnete Bewertung**.
2. Beim Aufklappen erscheinen (a) Zahl der dokumentierten Einträge, (b) je eine **kompakte Auswahl häufig genannter Gefühle und Bedürfnisse** mit exakten **Nennungszahlen** (keine Intensität), (c) „Einträge dieser Woche ansehen“ mit denselben stabilen Journal-IDs und (d) eine zurückhaltende, optionale Reflexionsaktion. Zu viele Begriffe nicht verstecken: vollständige Begriffs-/Belegansicht bleibt erreichbar; bei wenig Platz haben Klartext/Links Vorrang vor Grafik. Mehrfachnennungen können zu Summen über der Eintragszahl führen.
3. **Leere Woche:** neutrales „Keine Einträge dokumentiert; daraus lässt sich nichts über dein Erleben ableiten“, ohne rot markierte Lücke, Belohnungsmechanik oder Aufforderung zum Nachtragen. Die Woche bleibt navigierbar, wird aber nicht als „schlechte Woche“ gewertet.
4. **Wochenfilter und Kalender:** Der Rückblick muss offenlegen, ob die Wochenliste unabhängig vom aktuellen 7/30/90-Tage-Statistikfilter alle sicher geladenen Einträge zeigt oder auf einen explizit ausgewählten Zeitraum begrenzt ist. Kein heimliches Mischen von Bezugszeiträumen. Woche anklicken → zu dieser Wochenbelegliste; Datum in der Liste → dieselben Einträge wie im Kalender. Beim Zurück bleibt die ausgewählte, aufgeklappte Woche erhalten, solange die Sitzung entsperrt ist.
5. **Kein Score/Trend:** Keine „beste/schlechteste Woche“, kein Stimmungsgraph, keine interpretierende Veränderungsbehauptung und keine automatischen Empfehlungen. Die erfassten Nennungen sind **keine Messung** der tatsächlichen Gefühlsdauer, -häufigkeit oder -stärke.

### Freiwillige Wochenreflexion → „Meine Erkenntnisse“

1. In der aufgeklappten Woche steht der **sekundäre** Einstieg „Freiwillig über diese Woche nachdenken“. Er ist nie Pflicht, wird nicht hervorgehoben, löst **keine automatische Speicherung** aus und wird nicht durch eine Erinnerung oder Benachrichtigung eingefordert.
2. Nach Antippen öffnet sich ein **leerer Entwurf** in der bestehenden Funktion **„Meine Erkenntnisse“**, mit der **freiwilligen, überspringbaren Reflexionsfrage** „Was ist mir rückblickend an dieser Woche aufgefallen?“. Der betrachtete Zeitraum darf als reiner **Navigationskontext** angezeigt werden; daraus wird **kein automatisch gespeicherter Wochenbezug** und kein neuer privater Datentyp. Es werden keine Gefühle, Bedürfnisse, Freitexte, Muster oder Journalverknüpfungen vorausgefüllt.
3. Nur die **eigene Formulierung** kann über den bestehenden ausdrücklichen Speichervorgang in der vorhandenen **verschlüsselten Erkenntnisablage** gesichert werden (mit deren bestehendem Backup-/Löschverhalten). Falls Nutzer Journalbelege anknüpfen möchten, geschieht dies **separat, ausdrücklich und innerhalb der bestehenden Limits**; keine implizite Verknüpfung sämtlicher Wocheneinträge. Kein eigenes „Wochenreflexions“-Archiv.
4. **Abbrechen/Zurück** verwirft keinen bereits vorhandenen Eintrag und erzeugt keinen neuen. Für einen ungespeicherten Text gilt dieselbe sichere Verwerfen-/Bestätigungslogik wie bei „Meine Erkenntnisse“. Nach Abbruch gelangt man zum aufgeklappten Wochenkontext zurück; nach Speichern wird die gespeicherte Erkenntnis im gewohnten Bereich angezeigt und kann über die Navigation wieder verlassen werden.
5. Bei **0 Einträgen** soll zunächst keine Reflexionsaufforderung als „Lückenfüller“ erscheinen; freies Schreiben unter „Meine Erkenntnisse“ bleibt unabhängig davon erreichbar. Keine automatisch aus Wochenstatistiken generierten Erkenntnisse.

### Accessibility, Tests, Grenzen

- Eine Woche ist anhand von **Datumsbereich, Eintragszahl und Aktion „Details anzeigen/ausblenden“** per TalkBack erfassbar; Zahl und Begriff dürfen nie nur über Farben/Balken erkennbar sein. Bei 320 dp und 200 % Schrift müssen Karten und Aktionen umbruchfähig, mit 48-dp-Touch-Zielen, sein.
- Android-/JVM-Tests mit fiktiven Daten: 0/1/10/100/1000/3000 Einträge; mehrere Check-ins pro Tag; leere Wochen; Montag–Sonntag-/Monats-/Jahres-/DST-Grenzen; konsistente IDs mit Kalender und Journal; Filter, Editieren, Löschen, Import und App-Sperre. UI-Test: **Ansehen erzeugt keine Erkenntnis**, Reflexionsaufruf allein speichert nichts, Auswahl von Journalbelegen erfolgt nur ausdrücklich, Cancel verwirft sicher, Save nur nach bewusster Aktion.
- Kein zusätzlicher persistierter Wochenindex oder Trackingdienst. Wochenzahlen nur aus authentifiziertem Journal-Snapshot, bei Sperre/Fehler sofort ausblenden. Die gespeicherte Reflexion benutzt die bereits bestehende Verschlüsselung und das bestehende EKB3-Backup.
- Reale Nutzertests, TalkBack-Hörprüfung, Dark-Mode-/Geräteprüfung sowie unabhängige Fach-/Security-Prüfung sind **offen/auf Wunsch übersprungen**, nicht bestanden.

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
- **M7.5.2:** neutralen Tagebuchkalender **und aufklappbare Wochenkarten** implementieren, inklusive gleichwertiger Text-/Belegnavigation und **freiwilligem Reflexionseinstieg in „Meine Erkenntnisse“** ohne automatischen Text, Link oder Speichervorgang. Keine Stimmungskurve. Eigenständiger Implementierungs-PR mit automatisierten Tests und Debug-APK; kein automatischer Merge.
- **M7.6:** automatisierbare Regressionen und offenes Restrisiko dokumentieren; wegen übersprungener manueller Prüfungen keinen vollständig geprüften Release behaupten.

## Konkrete UX-Erfolgskriterien für spätere, freiwillige Studien

Fünf Teilnehmende mit **ausschließlich fiktiven** Daten beantworten ohne Anleitung: (1) „Wofür steht die 6?“ (korrekt: Nennungen im ausgewählten Zeitraum), (2) „Sind 22 Tage ohne Eintrag gute Tage?“ (korrekt: unbekannt), (3) „Wie öffne ich die Belege?“ (aus der gleichen Zeile), (4) „Wie schalte ich die Grafik wieder aus?“, (5) „Wie finde ich einen früheren Eintrag im Kalender ohne einen fehlenden Tag als Misserfolg zu verstehen?“. Gegen bestehende Textansicht vergleichen; Grafik nur behalten, wenn Verständnis nicht sinkt und Bedienung verbessert wird. **Nicht durchgeführt.**
