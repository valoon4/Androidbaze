# Androidbaze

Reusable Android starter for fast debug APK builds in fresh environments.

## One-command build (Linux/macOS)

```bash
./scripts/bootstrap-and-build.sh
```

This can bootstrap a local Android SDK into `.android-sdk/`, bootstrap Gradle 8.9 into `.gradle-dist/`, install Android platform/build tools, and build the debug APK.

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

Every push to `main`, pull request, or manual workflow dispatch builds the debug APK and uploads it as the `androidbaze-debug-apk` artifact.

## Purpose

Clone/copy this repository for throwaway prototypes and random APK ideas, then replace the starter activity/package with the actual app/game code.
