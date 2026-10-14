# TRASH JUMP — The Floor Is a Scam

A self-contained Android Kotlin/Canvas vertical jumping roguelite, starting with the one-more-try platform hopping loop of Doodle Jump, then getting progressively weirder. Original procedural art and mechanics; no external assets.

## Controls
- Hold **left** or **right** half of the screen to steer, with wraparound edges.
- The trash goblin bounces automatically on landing.
- Top-right **II** pauses.
- Upgrades available on the title screen with earned tickets.

## Escalation / gameplay
- Six platform flavors: normal, moving, fragile, springboard, ice-slide, and teleporting glitch.
- Collect tickets, shields, jetpacks and coin magnets. Stomp patrolling "tax auditors" (or avoid them).
- Every few hundred meters reality mutates: reversed controls, moon gravity, wind, pinball physics and disco fever.
- Camera, stage palettes, skyline, screen shake, particles and custom Canvas creature sprites.
- Persistent high score, ticket bank, launch boots upgrade and reusable starting shield unlock.
- Endless deterministic-height-based procedural challenge and instant retry.

## Building
Run `gradle assembleDebug` in this directory with JDK 17, Gradle 8.11.1 and Android SDK 36, or use GitHub Actions workflow **Build Trash Jump APK**. APK: `app/build/outputs/apk/debug/app-debug.apk`.

Package: `com.eugi.trashjump`, version 1.0-trashwave.
