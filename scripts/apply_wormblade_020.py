from pathlib import Path
import base64, gzip, re

ROOT = Path(__file__).resolve().parents[1]

payloads = {
    "app/src/main/java/com/valoon4/wormblade/MainActivity.kt": ROOT / "scripts/wormblade020_main.b64",
    "app/src/main/java/com/valoon4/wormblade/GameData.kt": ROOT / "scripts/wormblade020_data.b64",
    "app/src/main/java/com/valoon4/wormblade/WormbladeGameView.kt": ROOT / "scripts/wormblade020_view.b64",
    "games/Wormblade/src/MainActivity.kt": ROOT / "scripts/wormblade020_main.b64",
    "games/Wormblade/src/GameData.kt": ROOT / "scripts/wormblade020_data.b64",
    "games/Wormblade/src/WormbladeGameView.kt": ROOT / "scripts/wormblade020_view.b64",
}
for rel, payload in payloads.items():
    target = ROOT / rel
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_bytes(gzip.decompress(base64.b64decode(payload.read_text().strip())))

build = ROOT / "app/build.gradle.kts"
text = build.read_text().replace("versionCode = 2", "versionCode = 3").replace('versionName = "0.1.1-debug"', 'versionName = "0.2.0-debug"')
if "versionCode = 3" not in text or 'versionName = "0.2.0-debug"' not in text:
    raise SystemExit("Could not patch Wormblade version")
build.write_text(text)

(ROOT / "games/Wormblade/README.md").write_text("""# Wormblade

Portrait swarm-action Android prototype.

## 0.2.0-debug

- 10 selectable overworld levels from the start.
- Persistent clear markers and banked coins.
- Every living worm segment shows its remaining HP directly on the bubble.
- Marked treasure segments drop chests.
- Chests pause combat and offer 3 random upgrades; choose exactly one.
- Real upgrade stats include multishot, projectile damage, fire rate, piercing, crit, extra blades, blade damage/reach, armor, max HP, magnet range, frost slow, overdrive and treasure sense.
- Level 5 and 10 feature larger Royal Worm boss chains with crowns and extra treasure.
- Free drag movement in the lower battlefield, auto-fire, orbit blades, coins, gems, heals, XP, wave healing, pause/retry/map flow.
- Procedural Canvas visuals; no external art dependency.

Package: `com.valoon4.wormblade`
Version: `0.2.0-debug` (`versionCode 3`)
""")

root_readme = ROOT / "README.md"
rt = root_readme.read_text()
line = "- `games/Wormblade/` – portrait swarm shooter with segmented worms, treasure upgrades and a 10-level overworld."
if "- `games/Wormblade/`" not in rt:
    rt = rt.replace("- `games/ShinobiCat/` – Shinobi Cat MVP 0.14.1 prerelease Android/WebView action-adventure handoff.", "- `games/ShinobiCat/` – Shinobi Cat MVP 0.14.1 prerelease Android/WebView action-adventure handoff.\n" + line)
else:
    rt = re.sub(r"- `games/Wormblade/`[^\n]*", line, rt)
root_readme.write_text(rt)
print("Applied Wormblade 0.2.0 source and content pass")
