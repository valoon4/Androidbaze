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
