# Androidbaze

Reusable Android starter for fast debug APK builds in fresh environments, plus a simple library for keeping multiple prototype apps/games organized.

## One-command build (Linux/macOS)

```bash
./scripts/bootstrap-and-build.sh
```

This can bootstrap a local Android SDK into `.android-sdk/`, bootstrap Gradle into `.gradle-dist/`, install Android platform/build tools, and build the debug APK.

Output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## If Android SDK already exists

```bash
./gradlew assembleDebug
```

`gradlew` is self-bootstrapping and does not require a global Gradle installation.

## Windows

```bat
scripts\build-debug.bat
```

## GitHub Actions

Every push to `main`, pull request, or manual workflow dispatch builds the current root Android project and uploads the debug APK as an artifact.

## Prototype library

Each random app/game gets its own folder under:

```text
games/<GameName>/
```

Recommended layout:

```text
games/<GameName>/
├─ README.md
├─ src/
├─ apks/
└─ notes/
```

- `src/` keeps game-specific source/assets or snapshots when needed.
- `apks/` keeps named playable debug APK milestones.
- `notes/` keeps design/build notes.
- APKs should use names like `<GameName>-debug-v001.apk`, `v002`, etc.

## Current games

- `games/Pikminlike/` – first squad-based creature-command prototype.

## Purpose

Use this repo as the permanent build toolbox for throwaway prototypes and random APK ideas. The root Android project is the working build target; finished/testable APK snapshots can be filed under the matching game folder.
