# D01 – Aurora Pulse: verbindliches Farbsystem

**Entscheidung:** Der Nutzer hat aus Playful Pulse, Aurora Flow und Aurora Pulse ausdrücklich **Aurora Pulse (Hybrid)** ausgewählt.

**Zielbild:** lebendig, modern, freundlich, leicht futuristisch und erwachsen – nicht verspielt auf Kosten der Lesbarkeit. Die Oberfläche bleibt eine ehrliche **D01-Demo ohne KI**.

## Markenfarben und Einsatzgrenzen

| Rolle | Markenfarbe | Einsatz |
|---|---|---|
| Indigo | `#6A6BFF` | Markenakzente, Illustration und Akzentflächen (nicht mit weißem kleinen Text) |
| Pink | `#F35E91` | Menschlichkeit, soziale Momente, kleine Akzente (nicht mit weißem kleinen Text) |
| Mint | `#25CFC0` | sanfte positive Akzente, Fortschritt, nicht als alleiniger Fehler-/Erfolgsindikator |
| Gold | `#FFC85A` | Lernaktivität/XP künftig, sehr sparsam und nicht als künstliche Kompetenzmessung |

**Barrierefreiheit:** Die ursprünglich diskutierten Hauptmarkenfarben sind **kein geeigneter Hintergrund für kleine weiße Buttonbeschriftungen**: `#6A6BFF`/Weiß ca. **4,07:1**, `#F35E91`/Weiß ca. **3,08:1**. Für gefüllte Light-Mode-Elemente wurden zugängliche Töne definiert: `#5D50CF`/Weiß ca. **5,98:1**, `#CB3470`/Weiß ca. **4,92:1**. Der dunklere Mint-Interaktionston `#0F756B`/Weiß erreicht ca. **5,56:1**. Die Originalfarben bleiben Marken- bzw. Dekorationswerte.

## Material 3 – Tokenkonzept

Die tatsächlichen Werte und das Umschalten von Light/Dark Mode liegen in **[AuroraPulseTheme.kt](../android/app/src/main/java/com/valoon4/communicationtrainer/ui/theme/AuroraPulseTheme.kt)**.

| Token | Light | Dark |
|---|---|---|
| Primary / onPrimary | `#5D50CF` / `#FFFFFF` | `#A7A8FF` / `#2E2F7C` |
| PrimaryContainer / onPrimaryContainer | `#E4E3FF` / `#26287C` | `#39347B` / `#E4E3FF` |
| Secondary / onSecondary | `#CB3470` / `#FFFFFF` | `#FF9ABA` / `#5D1732` |
| SecondaryContainer / onSecondaryContainer | `#FFD9E4` / `#6A1236` | `#5D1732` / `#FFD9E4` |
| Tertiary / onTertiary | `#0F756B` / `#FFFFFF` | `#5FE8DA` / `#0A403A` |
| TertiaryContainer / onTertiaryContainer | `#E4FBF6` / `#0F5E54` | `#164C48` / `#DBFFF6` |
| Background / onBackground | `#FCFAFF` / `#27253A` | `#171620` / `#F6F3FF` |
| Surface / onSurface | `#FFFFFF` / `#27253A` | `#211F2B` / `#F6F3FF` |
| SurfaceVariant / onSurfaceVariant | `#F2F0F8` / `#615E73` | `#2B2836` / `#C8C2D5` |

## UI-Komponenten in D01

- **Hero-Karte**: dunkler Indigo-Verlauf `#34226F → #5948BF → #5D50CF` mit weißer, kontrastreicher Schrift und mintfarbenem Zierakzent.
- **Bottom Navigation**: ruhige Oberfläche; gewählter Tab in Primary mit deutlich sichtbarem Indicator; verständliche Labels statt Farbe allein.
- **Cards**: weiße / dunkle Surface, dezente Kontur, thematische pinke/mintfarbene Symbolcontainer. Icons rein dekorativ, nicht als alleinige Information.
- **Dark Mode**: eigene Palette, nicht nur die Umkehrung des Light Modes.
- **Große Schrift**: scrollbare Inhalte, keine fixen Kartenhöhen. Manuelle UI-/Screenreader-/Schriftgrößenprüfung bleibt obligatorisch.

## Vorausschau: spätere D-Schritte

- **Chat-Bubbles**: eigene Nachrichten in PrimaryContainer/onPrimaryContainer (oder als gefüllte Primary/onPrimary), Charakter-Nachrichten in SurfaceVariant/onSurfaceVariant; nicht nach Farbe allein unterscheiden.
- **Coach/Emotionen**: Pink-Container für empathische Hinweise, Mint-Container für konstruktive nächste Schritte; niemals unbelegte Charakter-/Leistungsbewertungen optisch „bestätigen“.
- **XP/Lernen**: Gold als kleiner Motivationsakzent, nicht als verlässliche Kompetenzkennzahl.

## Abnahmeregel

In D01 prüfen JVM-Unit-Tests definierte Farben und **statische Vordergrund-/Hintergrund-Kontrastpaare** mit WCAG-AA-Schwelle **4,5:1**. Das beweist **nicht** umfassende Barrierefreiheit aller UI-Zustände, Touchziele, Screenreader oder dynamischer Kontraste. Dazu sind später reale Emulator-/Gerätetests bei normaler und großer Schrift sowie Dark Mode nötig.

**Keine Scope-Erweiterung:** Aurora Pulse ist eine Gestaltungsentscheidung innerhalb D01. Neue KI, Persistenz und echte Chatfunktionen bleiben in D02/D03.
