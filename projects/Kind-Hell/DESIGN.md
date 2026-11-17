# Kind Hell – ausgewähltes App-Design D: „Seele Mystic“

**Entscheidung:** 17. November 2026, **direkte Nutzerwahl „Wir nehmen D“** nach der viergeteilten visuellen Gegenüberstellung. Das ist die **gewählte Designrichtung**, nicht eine technische Implementierungsgarantie.

## Entscheidungsverlauf und Abgrenzung
Der Nutzer wollte **vier tatsächlich unterschiedliche UI-Designs in einem Vergleichsbild**, nicht vier fast identische Bildschirme mit anderer Deko; die ersten Vergleiche wurden als zu „KI-haft“ und zu wenig quirky/mit „Kaito, Joker, Seele“-Persönlichkeit kritisiert. Im zuletzt gezeigten 2×2-Designboard:
- **A – Guild Fantasy:** warm, nostalgisch, klassische Fantasy-Gilde – **nicht gewählt**.
- **B – Kaito Urban:** neongrelle, freche Street-Ästhetik – **nicht gewählt**.
- **C – Joker Playful:** spielerisch, rot-schwarze Karten-/Joker-Motive – **nicht gewählt**.
- **D – Seele Mystic:** **AUSDRÜCKLICH GEWÄHLT**. Ruhig, nächtlich, mystisch und menschlich – nicht generischer KI-Fantasy-Pomp.

## Visuelle DNA von D (Designvorgabe)
- **Farbwelt:** Mitternachtsblau und dunkles Indigo; Mondlicht-Lavendel und leichtes Eis-Cyan als Leuchtakzente. Weißblau für Schrift. Kein Gold-/Rosa-Dominanzlook wie A/B/C.
- **Stimmung:** Stille Nacht, Mond, kleine Sterne, leichte Lichtspuren / subtile Natur- oder Seelen-Symbolik. Nicht überladen. Elegante, zurückhaltende Oberfläche mit **eigenständiger Seele**.
- **UI:** mobile Android-first, klare Lesbarkeit und Touchflächen; sanft gerundete dunkelblaue Karten mit dünnen Linien, gut lesbare Texte, großzügige Luft. Illustrierte Atmosphäre sparsam einsetzen, nicht als Ersatz für echten App-Inhalt.
- **Navigation:** bestehende drei produktiven Bereiche **Marktplatz**, **Ankunft**, **Kompass**. Es ist **kein** Entscheid für eine neue vierte Pflichtnavigation.
- **Kompass:** später bei geschützter Erwachsenensuche die **drei getrennten Ja/Nein-Achsen** für Freundschaft, Romantik und sexuelle Offenheit. Aktuelle Demoprofile dürfen KEINE erfundenen sexuellen Offenheitsangaben oder echten Beziehungen vortäuschen.
- **Nicht verwechseln:** „Seele Mystic“ ist der **Designname von Option D**, nicht zwingend der finale Appname oder ein Anspruch auf eine konkrete Anime-Figur/ein Character-Skin. Der Markenname bleibt vorläufig „Kind Hell“.
- **Qualitätskriterium:** Die reale App soll sich **eigenständig, hochwertig und etwas quirky** anfühlen, statt wie ein generisches KI-generiertes Gildenposter.

## Umsetzungsschritt v0.0.2
- Der erste Code-Umbau auf dem isolierten Kind-Hell-Branch ersetzt die frühere warm-violett/goldene Theme-Sprache durch Nachthimmel, Lavendel, Eis-Cyan, klare Konturen und **in Compose gezeichnete Mond/Sterne**.
- Layout und Text der drei Screens wurden überarbeitet; vorhandene lokale Beiträge, Antworten, Vorstellungen, Kompassfilter, Merkliste und Aliasbearbeitung sollen erhalten bleiben.
- Es sind **keine** Anime-/Charakterbilder aus dem Vergleich übernommen, keine Bild-/Internetberechtigungen, keine Netzwerkfunktion und kein Social-Proof vorgetäuscht.
- Das Bild ist ein **visueller Ideengeber**, keine pixelgenaue technische Spezifikation. App-Build-/Geräteteststatus separat in `android/README.md` bzw. CI prüfen; nicht allein durch dieses Dokument als bestanden markieren.

## Noch offen
- Konkrete Schriftart/Markenlogo, eigene optionale Originalillustration und Feinschliff auf realem Android-Gerät.
- Accessibility, Kontrast, kleine Displays, Querformat und langfristiges Designsystem.
- Einmal nach installierter v0.0.2-Demo prüfen, ob D genügend „Seele“ besitzt und wo mehr Originalität ohne UI-Überladung nötig ist.

## Verbindliche App-Icon-Auswahl: Motiv 4 (17.11.2026)
**Nutzerabsicht und Bestätigung:** Der Nutzer stellte ein eigenes **2×2-Bild mit vier runden App-Icon-Motiven** zur Verfügung und sagte: „Dein fav von 1 2 oder 4 wird app icon btw.“ Der Assistent wählte **Motiv 4** als Favorit, der Nutzer bestätigte mit **„Yo save it“**. Für Kind Hell ist damit **Bild/Variante 4 = unten rechts** als gewünschtes App-Icon **festgehalten**. Dies ist eine **andere Auswahl als die frühere Designrichtung D/Seele Mystic**: D ist das App-UI; 4 ist das konkrete Icon-Motiv.

**Exakte visuelle Referenz:** Ein freundliches blondes, gekröntes Fantasy-Mädchen links und ein braunhaariger Junge mit blau-weißem Gewand rechts, beide strecken einladend eine Hand nach vorne; die beiden halten sich mittig an der Hand. Hellblaue Wolken, tiefblauer Sternenhimmel, **großer leuchtender goldener Kreis/Ring** um beide Figuren, Stern am oberen Kreisbogen. Das unterscheidet **4** klar von 1 (blauer Taghimmel), 2 (goldene Himmels-/Palasttreppe) und 3 (rosa Sonnenuntergang). Variante 4 ist **kein** austauschbares "mystisches Logo" – die beiden Figuren und der Ring sollen erhalten bleiben.

**Quellenidentität:** Vom Nutzer im Chat hochgeladenes Original-Kontaktblatt `1000052077.png` (1536×1536 Pixel, in Wirklichkeit JPEG-kodiert), SHA-256 `2caf9c8ec4ef24728da65bc941be6088d6482e5b59a66022082bece1773f03f0`. Die Auswahl ist das **untere rechte Quadrant** im 2×2-Raster, exakt x=768..1536, y=768..1536. Aus dieser Originaldatei wurde im Chat ein unveränderter Quadrant und ein 512×512-PNG als getrennte Vorschau zugeschnitten. Name 512-PNG `Kind-Hell-Icon-D-4-512.png`, SHA-256 `2692fbc0fea5dab014822bcc119fb8f1f56b75dac8d74e69eaaeaeab64d4d941`. Die binären Bilddateien selbst sind **noch nicht ins GitHub-Repo eingespielt**; sie liegen als Chat-Artefakte vor und müssen vor dem finalen Android-Icon-Build dauerhaft als Ressource gesichert werden.

**Entschieden vs. vorgeschlagen:** **App-Icon = 4 ist bestätigt.** Der Gedanke, Variante 2 eventuell als Splash-/Promo-Art zu verwenden, stammte **nur vom Assistenten** und ist **nicht** vom Nutzer beschlossen. Keine automatische Festlegung auf Splash 2!

**Implementierungsstatus:** Noch **nicht** als Android Launcher-Icon in `android/app/src/main/res/mipmap-*` oder als Adaptive-Icon eingebunden. Die vorhandene v0.0.2-APK darf **nicht** als fertiges neues Icon beworben werden. Nächste Aktion: Original-Crop als Bildressource ins Projekt übernehmen, Android-Maskierung/Foreground-Safe-Zone und kleine Launcher-Größen prüfen, APK bauen, Icon auf Gerät testen; andere App-Funktionen und Wormblade unangetastet lassen.

