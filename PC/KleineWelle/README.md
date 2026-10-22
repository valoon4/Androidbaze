# Kleine Welle – Die Schleiminsel 🏝️

Ein winziges, komplett eigenständiges **3D-PC-Browserspiel im Cel-/Toon-Shading-Look**: eine tropische Insel im türkisblauen Meer, ein kleiner Held mit Schwert, und **genau drei** hüpfende Schleime.

## Sofort spielen (Windows, Linux, macOS)

1. **Am einfachsten:** Lade die Datei [KleineWelle-offline.html](./KleineWelle-offline.html) herunter.
2. Öffne sie mit Doppelklick in Chrome, Firefox oder Edge.

**Keine Installation, kein Internet, kein Build, kein Konto erforderlich.** Du brauchst nur einen Browser mit WebGL/Hardwarebeschleunigung.

Alternativ kannst du den ganzen Ordner herunterladen und `index.html` öffnen. Die 3D-Engine liegt lokal in `vendor/`.

## Steuerung

| Taste | Aktion |
| --- | --- |
| W A S D oder Pfeiltasten | Auf der Insel laufen |
| Leertaste oder Linksklick | Mit dem Schwert angreifen |
| R | Spiel zurücksetzen |

Du hast drei Herzen. Jeder Schleim verträgt zwei Treffer. Vertreibe alle drei Schleime, um die kleine Insel zu befreien.

## Technik

- Three.js r150, selbstständig beigelegt, keine CDN-Abhängigkeit
- Prozedurale 3D/Low-Poly-Geometrie, Toon-Shading-Farbstufen, dunkle Silhouetten
- Animiertes türkisblaues Meer, Wolken, Vegetation, Hütte, Steg
- Bewegung, kleine Slime-KI, Schwerttreffer, Lebensanzeige und Sieg/Niederlage
- Kein Asset-Download, keine Cookies, kein Tracking

## Projektdateien

- `index.html`: Oberfläche und HUD
- `game.js`: 3D-Welt, Animationen und komplette Spiellogik
- `vendor/three.min.js`: lokal gespeicherte 3D-Bibliothek
- `vendor/LICENSE.threejs`: MIT-Lizenz von Three.js
- `KleineWelle-offline.html`: gebündelte Einzeldatei zum direkten Spielen
- `tools/build-standalone.js`: baut diese Einzeldatei aus den Quelldateien neu

Nach Änderungen an `index.html` oder `game.js` im Ordner `PC/KleineWelle/` einfach `node tools/build-standalone.js` ausführen, damit auch die Offline-Einzeldatei aktuell ist.

**Kleine Welle** ist ein eigenständiges, von klassischen farbenfrohen Cel-Shading-Abenteuern inspiriertes Mini-Projekt, ohne Zelda-Assets oder geschützte Nintendo-Figuren.
