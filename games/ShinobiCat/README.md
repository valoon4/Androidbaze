# ShinobiCat

Imported from the ChatGPT handoff archive `ShinobiCat-GitHub-Handoff-v0.14.1-PUBLIC-TESTKEY.zip`.

## Version

- Game/build: Shinobi Cat MVP 0.14.1 prerelease debug
- Android package: `com.openai.shinobicat`
- Orientation: landscape
- Architecture: fully offline HTML5 Canvas/JavaScript game wrapped in a minimal Android WebView APK
- Signing: disposable PUBLIC prerelease test key (never use for production/store releases)

## Playable build

Canonical APK filename:

`shinobi-cat-mvp-v0.14.1-prerelease-debug.apk`

SHA-256:

`6fad4870b693b467d8f856470aba98ded70e167c9477785d6e67e5362ed3bceb`

The uploaded handoff archive SHA-256 is:

`d6a8e91a92eda787b0db7cd2c950e4809ce17a3fa95b47ae4731bfa12b778ddb`

## Handoff contents

The supplied handoff contains the playable APK plus the development repository material: `game.js`, self-contained `project/assets/index.html`, `build_apk.py`, tests, public prerelease keystore, changelog, architecture/build documentation, campaign/roadmap notes and known issues.

The current game is not a Godot project. A future Godot version would be an explicit migration/rewrite.

## v0.14.1 notes

This build is the Dungeon 1 hotfix / Director's Cut puzzle-design pass. It retains the campaign, shops, outfits, bosses, heart containers and procedural music while expanding the dungeon puzzle arcs. The 0.14.1 hotfix removes the Forest Webweight Garden from the playable route and connects Forest Shadow directly to the spider boss.

## Signing warning

The prerelease keystore from the original handoff is intentionally public/disposable. Because it differs from the historical prototype signing key, older Shinobi Cat prototype APKs must be uninstalled once before installing builds signed with this public test key. Never use this key for a production release.
