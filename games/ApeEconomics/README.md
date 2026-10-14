# APE ECONOMICS 2.0: The Islands of Ambition 🐒🍌

A work-in-progress, native Android pixel-art economic adventure. The `main` build now starts **ApeVillageV2**, a new tile/pixel-rendered world: it does not overlay the older v0.13 renderer. All legacy prototype sources are retained for historical comparison.

## Playable vertical slice: Level 0 — The First Banana

- Interactive **world hub** with five visibly different level slots. Only Level 0 is playable; Levels 1–4 say *Coming in a future update*. We do not pretend they exist.
- Original pixel-art island with tile-patterned ground, tree-lined paths, banana farm, market, town hall, bank, river and bridge, village homes, lanterns, flowers and NPCs.
- A walking monkey protagonist with animated sprite and 4-direction-facing detail.
- Left-bottom thumbpad steering, right-bottom **ACT** near buildings, or **tap the map to auto-walk to the tapped point**.
- Guided quest with six phases: visit farm → harvest → visit market → trade → visit town hall → donate.
- Real gameplay resources: bananas, coins, influence; day cycle, changing banana price, income trickle, optional bank account and interest.
- Persistent tutorial-clear badge; a completion celebration, free play and return to hub.
- A separate GitHub Actions build and permanent development release with directly downloadable APK.

## Planned worlds (not yet implemented)

1. Merchant Isles: prices and supply chains
2. Capital of Apes: politics and influence
3. The Stock Jungle: finance and banking
4. Revolution Ridge: power and public unrest

Worlds will be built **sequentially**, each introducing a focused mechanic after the tutorial works well.

## Build

`cd games/ApeEconomics && gradle assembleDebug` (Java 17, Gradle 8.11.1, Android SDK 36). GitHub Actions: `Build Ape Economics 2.0 APK`; version `2.0-level0`.

## Limitations

This is an **early vertical slice** with code-drawn pixel art rather than a handcrafted commercial sprite atlas. The economy currently involves 4 locations, small transactions and ambient NPC motion. It is not yet the full autonomous-affensociety simulation. Device play-testing and visual iteration remain necessary.
