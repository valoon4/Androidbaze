# APE ECONOMICS — Visual Acceptance Contract
## Status: previous reference is NOT reproduced

**Binding visual target:** the user-provided original screenshot of Ape Economics with a highly textured, hand-authored 16-bit-style village, three distinctive buildings on the north bank, a market in the southeast, wooded edges, a river with a narrow bridge, individually drawn dressed monkeys, and beautifully textured parchment/wood HUD. The screenshot is the specific target; “generic Zelda-style” is *not* an acceptable substitute.

### Why v2.4/v2.5 failed despite compiling
- The procedural CC0 art atlas produces an unrelated aesthetic, not the reference's painterly-but-pixel-clean asset language.
- Four unrelated pasted buildings create inconsistent perspective, line thickness and roofs. The reference's plantation is a warm gabled farm with fenced rows, parliament is a grand gray dome/stone building, bank is a tall brown gabled treasury, and market is an open market stall with striped awning.
- Tree foliage, water edges, ground texture, dirt paths and shadows are unrelated asset families; the screenshot has a coherent art direction.
- Character costumes, gestures, thought bubbles and visual density matter as much as basic shape and color. Repainting existing generic assets does not achieve them.
- The GUI must be engraved/illustrated wood+parchment with icons, not generic stacked debug boxes.
- Merely adding detail, flowers or more objects to the v2 renderer does not solve the art mismatch.

### Corrective architecture
- **Do not patch the old geometry renderer further to claim a visual match.**
- Begin with an **authored reference-matched visual scene** using one coherent asset collection, not a collage of mismatched tiles.
- Build level-zero layered scene: ground, decorative low objects, walk-behind objects, actors, front-layer canopies/roofs, lighting/atmosphere, HUD.
- Reuse the Level 0 quest logic and world hub *after* art is visually approved.
- Separate art assets from collision/navmesh; derive interaction zones from actual visual door and stall coordinates.
- Preserve scale and character-to-building proportions from original reference; test an entire phone-screen composite, not just a flat standalone map.
- Use a screenshot comparison checklist with the user reference side by side; color, composition, texture/brushwork, building silhouette, UI, character scale, density and animation must all look cohesive.

### Nonnegotiable review gate
1. Image comparison shows the **same visual direction**, not a different fantasy tileset.
2. Consistent assets across ALL 4 buildings, foliage, bridge/riverbank, monkeys and HUD.
3. No legacy placeholder geometry or visibly mismatched textures.
4. Gameplay functional on Android; device test still needs doing.
5. Debug APK may be built internally, but **do not publish a new public APK or announce graphical completion** before 1–4 are met. CI uploads private-to-project Actions artifacts only for debugging, not a public release.

### Concept approval
This file defines *requirements only*, not evidence they have been met. The latest v2.x visual fails this gate.
