/* Kleine Welle – Die Schleiminsel
 * Original tiny cel-shaded island adventure. Requires bundled Three.js r150.
 * No external assets, APIs, fonts, or network connections.
 */
(function () {
  "use strict";

  const byId = (id) => document.getElementById(id);
  const stage = byId("stage");
  const startPanel = byId("startPanel");
  const endPanel = byId("endPanel");
  const heartsLabel = byId("hearts");
  const slimeLabel = byId("slimeCount");
  const toastEl = byId("toast");
  const errorEl = byId("error");

  function error(message) {
    errorEl.textContent = message;
    errorEl.classList.remove("hidden");
  }
  if (!window.THREE) {
    error("Die mitgelieferte 3D-Bibliothek konnte nicht geladen werden. Bitte den gesamten Ordner KleineWelle mit vendor/ und game.js zusammen lassen.");
    return;
  }

  let renderer;
  try {
    renderer = new THREE.WebGLRenderer({ antialias: true, alpha: false, powerPreference: "high-performance" });
  } catch (reason) {
    error("Dieses Spiel benötigt WebGL. Bitte Hardwarebeschleunigung im Browser aktivieren oder Chrome / Edge / Firefox ausprobieren.");
    return;
  }

  renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 1.65));
  renderer.setSize(window.innerWidth, window.innerHeight);
  renderer.shadowMap.enabled = true;
  renderer.shadowMap.type = THREE.PCFSoftShadowMap;
  renderer.outputEncoding = THREE.sRGBEncoding;
  renderer.toneMapping = THREE.NoToneMapping;
  stage.appendChild(renderer.domElement);

  const scene = new THREE.Scene();
  scene.background = new THREE.Color(0x9ce7eb);
  scene.fog = new THREE.Fog(0x9ce7eb, 55, 165);

  const camera = new THREE.PerspectiveCamera(49, window.innerWidth / window.innerHeight, 0.1, 240);
  const baseCamera = new THREE.Vector3(16.8, 18.5, 25.0);
  const cameraAim = new THREE.Vector3(0, 0.95, 0);
  camera.position.copy(baseCamera);
  camera.lookAt(cameraAim);

  const hemisphere = new THREE.HemisphereLight(0xeeffff, 0x9a9f76, 1.3);
  scene.add(hemisphere);
  const sunlight = new THREE.DirectionalLight(0xfff0cb, 1.75);
  sunlight.position.set(-17, 28, 16);
  sunlight.castShadow = true;
  sunlight.shadow.mapSize.set(2048, 2048);
  sunlight.shadow.camera.left = -21;
  sunlight.shadow.camera.right = 21;
  sunlight.shadow.camera.top = 21;
  sunlight.shadow.camera.bottom = -21;
  sunlight.shadow.camera.near = 0.5;
  sunlight.shadow.camera.far = 70;
  sunlight.shadow.bias = -0.00018;
  sunlight.shadow.normalBias = 0.03;
  scene.add(sunlight);
  scene.add(sunlight.target);

  const ramp = new THREE.DataTexture(new Uint8Array([70, 135, 207, 255]), 4, 1, THREE.RedFormat);
  ramp.needsUpdate = true;
  ramp.magFilter = THREE.NearestFilter;
  ramp.minFilter = THREE.NearestFilter;
  ramp.generateMipmaps = false;

  function mat(hex, more) {
    return new THREE.MeshToonMaterial(Object.assign({
      color: hex, gradientMap: ramp, flatShading: false
    }, more || {}));
  }
  const M = {
    ink: new THREE.MeshBasicMaterial({ color: 0x203b56, side: THREE.BackSide }),
    seafoam: new THREE.MeshBasicMaterial({ color: 0xcaffdb, transparent: true, opacity: 0.63, depthWrite: false }),
    grass: mat(0x7bdb78),
    grassLip: mat(0x4eba70),
    meadow: mat(0xa4ea83),
    darkGrass: mat(0x469e6a),
    sand: mat(0xffd891),
    sandEdge: mat(0xffbc7d),
    cliff: mat(0xb67e64),
    deepCliff: mat(0x755a66),
    bone: mat(0xfff5df),
    bark: mat(0x946251),
    woodLight: mat(0xc48b5e),
    leaf: mat(0x34be7c),
    leafLight: mat(0x81e88f),
    leafDark: mat(0x208f77),
    cream: mat(0xffdfac),
    coral: mat(0xff8b7b),
    roof: mat(0xff735b),
    roofTop: mat(0xffaa63),
    window: new THREE.MeshBasicMaterial({ color: 0x68e8ed }),
    pupil: new THREE.MeshBasicMaterial({ color: 0x263651 }),
    white: new THREE.MeshBasicMaterial({ color: 0xffffff }),
    sparkle: new THREE.MeshBasicMaterial({ color: 0xfff4bf, transparent: true, opacity: 0.85, depthWrite: false }),
    boot: mat(0x3b5675),
    tunic: mat(0x278db4),
    tunicBright: mat(0x50bed0),
    hair: mat(0x414f94),
    hairBright: mat(0x7d80d0),
    scarf: mat(0xffb647),
    scarfShade: mat(0xed7c65),
    metal: mat(0xdff8f9),
    gold: mat(0xffc55b),
    blush: new THREE.MeshBasicMaterial({ color: 0xf5a6a2 }),
    slimePink: mat(0xff6f9a),
    slimeYellow: mat(0xffc45a),
    slimeAqua: mat(0x62dbd0)
  };
  const sphere = (r, w, h) => new THREE.SphereGeometry(r, w || 12, h || 9);
  const box = (x, y, z) => new THREE.BoxGeometry(x, y, z);

  function part(parent, geometry, material, position, scale, outline, cast) {
    const holder = new THREE.Group();
    const face = new THREE.Mesh(geometry, material);
    face.castShadow = cast !== false;
    face.receiveShadow = false;
    holder.add(face);
    if (outline !== false) {
      const border = new THREE.Mesh(geometry, M.ink);
      border.scale.setScalar(typeof outline === "number" ? outline : 1.064);
      holder.add(border);
    }
    if (position) holder.position.set(position[0], position[1], position[2]);
    if (scale) holder.scale.set(scale[0], scale[1], scale[2]);
    parent.add(holder);
    return holder;
  }

  // Calm cel-shaded animated sea. The fragment colours use discrete bands.
  const seaUniforms = { uTime: { value: 0 } };
  const seaMat = new THREE.ShaderMaterial({
    uniforms: seaUniforms,
    vertexShader: [
      "varying vec2 vSea;",
      "void main() {",
      "  vec4 w = modelMatrix * vec4(position, 1.0);",
      "  vSea = w.xz;",
      "  gl_Position = projectionMatrix * viewMatrix * w;",
      "}"
    ].join("\n"),
    fragmentShader: [
      "precision mediump float;",
      "varying vec2 vSea;",
      "uniform float uTime;",
      "void main() {",
      "  float a = sin(vSea.x * 0.38 + sin(vSea.y * 0.19 + uTime * 0.66) * 1.25 + vSea.y * 0.24);",
      "  float b = sin(vSea.y * 0.51 - vSea.x * 0.14 - uTime * 0.57);",
      "  float v = a * 0.74 + b * 0.26;",
      "  vec3 c = vec3(0.08, 0.61, 0.70);",
      "  if (v > -0.20) c = vec3(0.12, 0.72, 0.76);",
      "  if (v >  0.44) c = vec3(0.19, 0.79, 0.78);",
      "  float glint = pow(max(0.0, sin(vSea.x * 1.35 + uTime * 0.67) * sin(vSea.y * 1.55 - uTime * 0.51)), 23.0);",
      "  c += vec3(0.15, 0.20, 0.16) * glint;",
      "  gl_FragColor = vec4(c, 1.0);",
      "}"
    ].join("\n"),
    side: THREE.DoubleSide
  });
  const ocean = new THREE.Mesh(new THREE.PlaneGeometry(320, 320), seaMat);
  ocean.rotation.x = -Math.PI / 2;
  ocean.position.y = -0.10;
  scene.add(ocean);

  function islandRadius(theta, radius) {
    const wobble = 1 + 0.018 * Math.sin(theta * 5 + 0.2) +
      0.012 * Math.sin(theta * 9 - 0.7) + 0.007 * Math.cos(theta * 15 + 0.4);
    return radius * wobble;
  }

  function islandDisc(r, y) {
    const pos = [0, y, 0];
    const ids = [];
    const n = 96;
    for (let i = 0; i < n; i++) {
      const a = 2 * Math.PI * i / n;
      const d = islandRadius(a, r);
      pos.push(Math.cos(a) * d, y, Math.sin(a) * d);
    }
    for (let i = 0; i < n; i++) {
      ids.push(0, 1 + ((i + 1) % n), 1 + i);
    }
    const g = new THREE.BufferGeometry();
    g.setAttribute("position", new THREE.Float32BufferAttribute(pos, 3));
    g.setIndex(ids);
    g.computeVertexNormals();
    return g;
  }

  function islandBand(innerR, outerR, innerY, outerY) {
    const p = [], ids = [], n = 96;
    for (let i = 0; i < n; i++) {
      const a = 2 * Math.PI * i / n;
      const inner = islandRadius(a, innerR);
      const outer = islandRadius(a, outerR);
      p.push(Math.cos(a) * inner, innerY, Math.sin(a) * inner);
      p.push(Math.cos(a) * outer, outerY, Math.sin(a) * outer);
    }
    for (let i = 0; i < n; i++) {
      const s = i * 2, next = ((i + 1) % n) * 2;
      ids.push(s, next, s + 1, s + 1, next, next + 1);
    }
    const g = new THREE.BufferGeometry();
    g.setAttribute("position", new THREE.Float32BufferAttribute(p, 3));
    g.setIndex(ids);
    g.computeVertexNormals();
    return g;
  }

  const island = new THREE.Group();
  scene.add(island);
  function ground(geometry, material) {
    const m = new THREE.Mesh(geometry, material);
    m.receiveShadow = true;
    island.add(m);
    return m;
  }
  ground(islandDisc(8.82, 1.13), M.grass);
  ground(islandBand(8.82, 9.18, 1.13, 1.02), M.grassLip);
  ground(islandBand(9.18, 10.48, 1.02, 0.49), M.sand);
  ground(islandBand(10.48, 10.91, 0.49, 0.06), M.sandEdge);
  ground(islandBand(10.91, 10.68, 0.06, -0.66), M.cliff);
  ground(islandBand(10.68, 10.10, -0.66, -1.32), M.deepCliff);
  // Open-sea foam and small broken halos along the beach.
  for (let i = 0; i < 2; i++) {
    const ring = new THREE.Mesh(
      new THREE.TorusGeometry(11.10 + i * 0.45, i === 0 ? 0.048 : 0.026, 5, 112),
      M.seafoam
    );
    ring.rotation.x = Math.PI / 2;
    ring.position.y = -0.052;
    ring.scale.set(1.012, 0.984, 1);
    scene.add(ring);
  }
  const nearRocks = new THREE.Group();
  island.add(nearRocks);

  let seed = 2517;
  function rng() {
    seed = (1664525 * seed + 1013904223) >>> 0;
    return seed / 4294967296;
  }
  const decorative = new THREE.Group();
  island.add(decorative);

  // Hand-built stylised round-canopy trees (thick silhouette + flat shade bands).
  function tree(x, z, size, variant) {
    const t = new THREE.Group();
    t.position.set(x, 1.11, z);
    t.scale.setScalar(size);
    decorative.add(t);
    part(t, new THREE.CylinderGeometry(0.19, 0.31, 2.25, 7), M.bark, [0, 1.1, 0], null, 1.09);
    part(t, sphere(1.18, 13, 9), variant ? M.leafLight : M.leaf, [-0.35, 2.62, 0], [1, .80, .93], 1.045);
    part(t, sphere(1.03, 12, 8), variant ? M.leaf : M.leafDark, [0.55, 2.67, -0.12], [1, .93, .95], 1.052);
    part(t, sphere(0.98, 12, 9), M.leafLight, [0.02, 3.22, .05], [1.04, .95, .99], 1.049);
    part(t, sphere(0.29, 9, 7), M.leafLight, [-1.02, 2.28, .4], [1.0, .66, .9], false);
    return t;
  }
  tree(-5.7, -4.6, 1.02, false);
  tree(-7.15, 1.45, 0.74, true);
  tree(5.95, 3.30, 0.83, false);

  // Little orange-roof cottage on the hill.
  const cottage = new THREE.Group();
  cottage.position.set(4.5, 1.17, -4.05);
  cottage.rotation.y = -0.14;
  island.add(cottage);
  part(cottage, box(2.65, 1.85, 2.24), M.cream, [0, .94, 0], null, 1.025);
  part(cottage, box(2.8, .22, 2.45), M.woodLight, [0, .11, 0], null, 1.037);
  const leftRoof = part(cottage, box(1.95, .19, 2.66), M.roof, [-.72, 2.20, 0], null, 1.056);
  leftRoof.rotation.z = .52;
  const rightRoof = part(cottage, box(1.95, .19, 2.66), M.roofTop, [.72, 2.20, 0], null, 1.056);
  rightRoof.rotation.z = -.52;
  part(cottage, box(.77, 1.32, .14), M.bark, [-.54, .73, 1.19], null, 1.045);
  part(cottage, sphere(.08, 8, 7), M.gold, [-.27, .78, 1.31], [1,.95,.6], false);
  part(cottage, box(.56, .57, .14), M.window, [.63, 1.15, 1.19], null, 1.09, false);
  part(cottage, box(.08, .69, .17), M.bone, [.63, 1.15, 1.28], null, false, false);
  part(cottage, box(.69, .08, .17), M.bone, [.63, 1.15, 1.28], null, false, false);
  part(cottage, box(.37, .63, .33), M.cliff, [.59, 2.85, -.44], null, 1.045);
  part(cottage, box(.66, .09, .45), M.roof, [.59, 3.14, -.44], null, 1.065);

  // Tiny wooden dock, a little ocean island silhouette detail.
  const dock = new THREE.Group();
  dock.position.set(0.9, .34, 10.03);
  island.add(dock);
  for (let i = 0; i < 5; i++) {
    part(dock, box(2.15, .18, .44), i % 2 ? M.woodLight : M.bark,
      [0, .09, i * .42], null, 1.028);
  }
  for (const x of [-.96, .96]) {
    for (const z of [.02, 1.68]) {
      part(dock, new THREE.CylinderGeometry(.14, .17, 1.5, 7), M.bark,
        [x, -.40, z], null, 1.07);
    }
  }

  function pebble(x, z, s) {
    const item = part(decorative, new THREE.DodecahedronGeometry(0.48, 0),
      rng() > 0.43 ? mat(0x9db8a2) : mat(0xdfc49b),
      [x, 1.2, z], [s, .45 * s, .8 * s], 1.052);
    item.rotation.y = rng() * Math.PI;
  }
  function flower(x, z, pink) {
    const stem = new THREE.Group();
    stem.position.set(x, 1.17, z);
    decorative.add(stem);
    part(stem, new THREE.CylinderGeometry(.025, .026, .25, 5), M.darkGrass,
      [0, .11, 0], null, false, false);
    for (let i = 0; i < 5; i++) {
      const a = i * Math.PI * 2 / 5;
      part(stem, sphere(.11, 7, 6), pink ? M.coral : M.bone,
        [Math.sin(a) * .14, .27, Math.cos(a) * .14], [1,.55,.85], false, false);
    }
    part(stem, sphere(.075, 7, 6), M.gold, [0, .285, 0], null, false, false);
  }
  for (let i = 0; i < 28; i++) {
    const a = rng() * Math.PI * 2, r = 2.4 + Math.sqrt(rng()) * 5.55;
    const x = Math.cos(a) * r, z = Math.sin(a) * r;
    if ((x > 2.45 && x < 6.65 && z > -5.95 && z < -2.25) ||
        (x > -1.7 && x < 2.0 && z > 3.9)) continue;
    if (rng() < .34) pebble(x, z, .22 + rng() * .39);
    else flower(x, z, rng() > .44);
  }
  // More low-poly beach rocks.
  for (let i = 0; i < 8; i++) {
    const a = i * Math.PI * .76 + .45;
    const r = 9.25 + rng() * .5;
    const rock = part(nearRocks, new THREE.DodecahedronGeometry(.52, 0),
      i % 2 ? M.cliff : M.cream,
      [Math.cos(a) * r, .95, Math.sin(a) * r],
      [.85, .55, .6], 1.056);
    rock.rotation.y = a;
  }

  // Light clouds and distant happy sky ornaments.
  const clouds = [];
  function cloud(x, y, z, scale, speed) {
    const cloudGroup = new THREE.Group();
    cloudGroup.position.set(x, y, z);
    cloudGroup.scale.setScalar(scale);
    for (const p of [[-1.05,0,0,.95],[0,.22,0,1.20],[1.16,-.05,0,.87]]) {
      part(cloudGroup, sphere(1, 12, 8), M.bone, [p[0],p[1],p[2]],
        [p[3],p[3]*.64,p[3]*.62], 1.028, false);
    }
    scene.add(cloudGroup);
    clouds.push({ mesh: cloudGroup, from: x, speed });
  }
  cloud(-23, 15.7, -29, 1.8, .23);
  cloud(22, 17.5, -36, 2.4, -.16);
  cloud(-7, 20, -45, 1.3, .14);
  part(scene, sphere(3.2, 32, 16),
    new THREE.MeshBasicMaterial({ color: 0xffefb3 }),
    [-32, 29, -70], null, false, false);

  // Hero: a deliberately original chibi adventurer, not a Zelda character.
  const hero = new THREE.Group();
  const heroVisual = new THREE.Group();
  hero.add(heroVisual);
  scene.add(hero);
  const legs = [];
  for (const x of [-.30, .30]) {
    const leg = part(heroVisual, box(.37, .59, .37), M.boot,
      [x, .35, 0], null, 1.068);
    part(heroVisual, sphere(.28, 9, 8), M.boot,
      [x, .11, .14], [1.13, .55, 1.36], 1.064);
    legs.push(leg);
  }
  part(heroVisual, new THREE.CylinderGeometry(.42, .57, 1.07, 9),
    M.tunic, [0, 1.02, 0], null, 1.055);
  part(heroVisual, box(.86, .18, .34), M.tunicBright, [0, .65, .35], null, false);
  for (const x of [-.58, .58]) {
    const arm = part(heroVisual, sphere(.27, 10, 8), M.tunic,
      [x, 1.29, -.02], [.85,1.15,.78], 1.07);
    part(heroVisual, sphere(.21, 9, 8), M.cream,
      [x*1.11, .99, .13], null, 1.06);
    arm.rotation.z = x > 0 ? -.20 : .20;
  }
  part(heroVisual, sphere(.65, 15, 11), M.cream,
    [0, 2.03, .10], [.98,1.02,.91], 1.043);
  part(heroVisual, sphere(.64, 15, 10), M.hair,
    [0, 2.47, -.06], [1.08,.48,1], 1.043);
  for (let i = -1; i <= 1; i++) {
    const spike = part(heroVisual, new THREE.ConeGeometry(.25, .54, 6),
      i === 0 ? M.hairBright : M.hair,
      [i*.36, 2.38, .44], null, 1.035);
    spike.rotation.z = .28 + i * .18;
  }
  for (const x of [-.245, .245]) {
    part(heroVisual, sphere(.092, 9, 8), M.pupil,
      [x, 2.075, .677], [1,1.3,.46], false, false);
    part(heroVisual, sphere(.03, 7, 6), M.white,
      [x-.018, 2.11, .721], null, false, false);
    part(heroVisual, sphere(.11, 10, 8), M.blush,
      [x*1.52, 1.88, .614], [1,.42,.22], false, false);
  }
  part(heroVisual, sphere(.052, 7, 6), M.bark,
    [0,1.88,.695], [.65,.32,.25], false, false);
  part(heroVisual, new THREE.CylinderGeometry(.53,.44,.20,10), M.scarf,
    [0,1.53,.06], null, 1.036);
  const scarfTail = part(heroVisual, box(.23,.6,.14), M.scarfShade,
    [-.46,1.17,-.18], null, 1.058);
  scarfTail.rotation.z = -.46;

  const sword = new THREE.Group();
  sword.position.set(.83, 1.04, .31);
  heroVisual.add(sword);
  part(sword, new THREE.CylinderGeometry(.09,.09,.43,7), M.bark,
    [0,.15,0], null, 1.065);
  part(sword, box(.51,.12,.2), M.gold,
    [0,.39,0], null, 1.065);
  part(sword, new THREE.CylinderGeometry(.035,.12,.95,4), M.metal,
    [0,.94,0], null, 1.035);
  sword.rotation.z = -.22;

  const heroShadow = new THREE.Mesh(new THREE.CircleGeometry(.68, 25),
    new THREE.MeshBasicMaterial({ color:0x335d4c, transparent:true, opacity:.25, depthWrite:false }));
  heroShadow.rotation.x = -Math.PI / 2;
  heroShadow.position.y = 1.144;
  scene.add(heroShadow);

  const slash = new THREE.Mesh(
    new THREE.RingGeometry(1.05, 1.57, 36, 1, -.35, Math.PI * .87),
    new THREE.MeshBasicMaterial({ color:0xfaffdf, transparent:true, opacity:.85, depthWrite:false, side:THREE.DoubleSide })
  );
  slash.rotation.x = -Math.PI / 2;
  slash.position.y = 2.10;
  slash.visible = false;
  scene.add(slash);

  function createSlime(color, x, z, phase) {
    const root = new THREE.Group();
    root.position.set(x, 1.15, z);
    const visual = new THREE.Group();
    root.add(visual);
    const profile = [
      new THREE.Vector2(0,0), new THREE.Vector2(.47,.04),
      new THREE.Vector2(.67,.20), new THREE.Vector2(.74,.43),
      new THREE.Vector2(.67,.72), new THREE.Vector2(.49,.92),
      new THREE.Vector2(.25,1.08), new THREE.Vector2(0,1.13)
    ];
    const goo = part(visual, new THREE.LatheGeometry(profile, 18), color,
      [0,0,0], null, 1.065);
    for (const eyeX of [-.25,.25]) {
      part(visual, sphere(.14, 10, 8), M.white,
        [eyeX,.63,.61], [1,1.10,.47], false, false);
      part(visual, sphere(.083, 9, 8), M.pupil,
        [eyeX,.62,.682], [.77,1.13,.43], false, false);
    }
    part(visual, sphere(.12, 9, 8), M.white,
      [-.29,.91,.38], [1.25,.58,.29], false, false);
    for (const eyeX of [-.51,.51]) {
      part(visual, sphere(.13, 9, 8), M.blush,
        [eyeX,.43,.42], [1,.45,.3], false, false);
    }
    const shadow = new THREE.Mesh(new THREE.CircleGeometry(.76,24),
      new THREE.MeshBasicMaterial({ color:0x35644b, transparent:true, opacity:.20, depthWrite:false }));
    shadow.rotation.x = -Math.PI/2;
    shadow.position.y = .008;
    root.add(shadow);
    scene.add(root);
    return {
      root, visual, goo, hp:2, alive:true, home:new THREE.Vector2(x,z),
      phase, knock:0, blink:0, attackTimer:0
    };
  }

  const slimeSpecs = [
    { c:M.slimePink, x:-3.85, z: 1.18, phase:0.5 },
    { c:M.slimeYellow, x: 3.24, z: 2.22, phase:2.1 },
    { c:M.slimeAqua, x:-1.33, z:-4.58, phase:3.8 }
  ];
  let slimes = [];
  let hearts = 3;
  let defeated = 0;
  let state = "menu";
  let heroHurt = 0;
  let attackTime = 0;
  let attackHit = false;
  let heroFacing = 0;
  let moveAmount = 0;
  let t = 0;
  const particles = [];
  const keys = new Set();
  let audio = null;
  let toastTimeout = null;

  function playTone(freq, duration, kind) {
    if (!audio) return;
    try {
      const osc = audio.createOscillator();
      const gain = audio.createGain();
      const now = audio.currentTime;
      osc.type = kind === "hurt" ? "triangle" : "sine";
      osc.frequency.setValueAtTime(freq, now);
      osc.frequency.exponentialRampToValueAtTime(kind === "hurt" ? freq*.53 : freq*1.35, now + duration);
      gain.gain.setValueAtTime(.065, now);
      gain.gain.exponentialRampToValueAtTime(.001, now + duration);
      osc.connect(gain);
      gain.connect(audio.destination);
      osc.start(now);
      osc.stop(now + duration);
    } catch (_ignored) {}
  }

  function showToast(message, ms) {
    toastEl.textContent = message;
    toastEl.classList.add("show");
    if (toastTimeout) clearTimeout(toastTimeout);
    toastTimeout = setTimeout(() => toastEl.classList.remove("show"), ms || 1100);
  }

  function hud() {
    heartsLabel.innerHTML = [0,1,2].map((n) =>
      '<span class="heart' + (n >= hearts ? ' empty' : '') + '">♥</span>'
    ).join("");
    slimeLabel.textContent = defeated + " / 3";
  }

  function removeParticles() {
    for (const p of particles) {
      scene.remove(p.mesh);
      p.mesh.geometry.dispose();
      p.mesh.material.dispose();
    }
    particles.length = 0;
  }

  function reset() {
    for (const slime of slimes) scene.remove(slime.root);
    removeParticles();
    slimes = slimeSpecs.map(s => createSlime(s.c,s.x,s.z,s.phase));
    hearts = 3;
    defeated = 0;
    heroHurt = 0;
    attackTime = 0;
    attackHit = false;
    heroFacing = Math.PI;
    hero.position.set(.0,1.15,4.4);
    hero.rotation.y = heroFacing;
    heroVisual.visible = true;
    keys.clear();
    state = "playing";
    startPanel.classList.add("hidden");
    endPanel.classList.add("hidden");
    hud();
    showToast("⚔️ Besiege die drei Schleime!", 1600);
  }

  function finish(won) {
    if (state !== "playing") return;
    state = won ? "win" : "lost";
    keys.clear();
    byId("endIcon").textContent = won ? "🏆" : "💔";
    byId("endEyebrow").textContent = won ? "ABENTEUER GESCHAFFT!" : "OH NEIN!";
    byId("endTitle").innerHTML = won ? "INSEL <em>GERETTET!</em>" : "NOCH EIN <em>VERSUCH?</em>";
    byId("endDescription").textContent = won ?
      "Alle drei Schleime wurden vertrieben. Deine winzige Insel kann wieder friedlich im Meer schaukeln! ✨" :
      "Die frechen Schleime haben dich erwischt. Aber kleine Helden geben nicht auf!";
    endPanel.classList.remove("hidden");
    playTone(won ? 740 : 210, .32, won ? "good" : "hurt");
  }

  function burst(x, z, color, count) {
    for (let i = 0; i < count; i++) {
      const geometry = new THREE.DodecahedronGeometry(.09 + rng() * .12, 0);
      const material = new THREE.MeshBasicMaterial({ color });
      const mesh = new THREE.Mesh(geometry, material);
      mesh.position.set(x, 1.83, z);
      scene.add(mesh);
      const a = rng()*Math.PI*2, s=1.4+rng()*2.2;
      particles.push({
        mesh, velocity:new THREE.Vector3(Math.cos(a)*s,1.6+rng()*3,Math.sin(a)*s),
        ttl:.6+rng()*.35, max:.95
      });
    }
  }

  function strike() {
    if (state !== "playing" || attackTime > 0.02) return;
    attackTime = .44;
    attackHit = false;
    playTone(360,.12,"swing");
  }

  function landHit() {
    const forward = new THREE.Vector2(Math.sin(heroFacing),Math.cos(heroFacing));
    for (const slime of slimes) {
      if (!slime.alive) continue;
      const dx=slime.root.position.x-hero.position.x;
      const dz=slime.root.position.z-hero.position.z;
      const dist=Math.hypot(dx,dz);
      if (dist > 2.35 || dist < .0001) continue;
      const dot=(dx*forward.x + dz*forward.y)/dist;
      if (dot < -.18) continue;
      slime.hp--;
      slime.blink = .28;
      slime.knock = .25;
      slime.root.position.x += (dx/dist)*.65;
      slime.root.position.z += (dz/dist)*.65;
      playTone(slime.hp <= 0 ? 640 : 460, .13, "good");
      if (slime.hp <= 0) {
        slime.alive=false;
        slime.root.visible=false;
        defeated++;
        burst(slime.root.position.x,slime.root.position.z,slime.goo.children[0].material.color,14);
        hud();
        showToast(defeated === 3 ? "🎉 Alle Schleime geschafft!" : "✨ Ein Schleim weniger!",1250);
        if (defeated === 3) {
          setTimeout(() => { if (state === "playing" && defeated === 3) finish(true); }, 1150);
        }
      } else {
        showToast("💥 Treffer!",580);
      }
    }
  }

  function hurt() {
    if (heroHurt > 0 || state !== "playing") return;
    heroHurt = 1.25;
    hearts--;
    hud();
    burst(hero.position.x,hero.position.z,0xffeea9,6);
    playTone(220,.22,"hurt");
    showToast("💔 Autsch! Pass auf!",800);
    if (hearts <= 0) {
      setTimeout(() => { if (state === "playing" && hearts <= 0) finish(false); }, 750);
    }
  }

  const RIGHT = new THREE.Vector2(.830, -.558);
  const UP = new THREE.Vector2(-.558, -.830);

  function updateHero(dt) {
    const ix = (keys.has("d") || keys.has("arrowright") ? 1 : 0) -
      (keys.has("a") || keys.has("arrowleft") ? 1 : 0);
    const iy = (keys.has("w") || keys.has("arrowup") ? 1 : 0) -
      (keys.has("s") || keys.has("arrowdown") ? 1 : 0);
    const moving = ix !== 0 || iy !== 0;
    moveAmount = THREE.MathUtils.lerp(moveAmount, moving ? 1 : 0, Math.min(1,dt*11));
    if (moving) {
      const d = new THREE.Vector2(RIGHT.x*ix+UP.x*iy, RIGHT.y*ix+UP.y*iy).normalize();
      const speed = attackTime > 0 ? 3.4 : 5.05;
      hero.position.x += d.x * speed * dt;
      hero.position.z += d.y * speed * dt;
      const edge = Math.hypot(hero.position.x,hero.position.z);
      if (edge > 8.48) {
        hero.position.x *= 8.48 / edge;
        hero.position.z *= 8.48 / edge;
      }
      heroFacing = Math.atan2(d.x,d.y);
    }
    const targetAngle = heroFacing;
    const diff = Math.atan2(Math.sin(targetAngle-hero.rotation.y),Math.cos(targetAngle-hero.rotation.y));
    hero.rotation.y += diff * Math.min(1,dt*15);
    heroShadow.position.set(hero.position.x,1.145,hero.position.z);
    heroVisual.position.y = Math.abs(Math.sin(t*12.5))*.105*moveAmount + Math.sin(t*2.4)*.025;
    heroVisual.rotation.z = Math.sin(t*12.5)*.035*moveAmount;
    scarfTail.rotation.z = -.48 + Math.sin(t*5)*.15;

    for (let i = 0; i < legs.length; i++) {
      legs[i].rotation.x = Math.sin(t*12 + i*Math.PI)*.40*moveAmount;
    }
    if (attackTime > 0) {
      attackTime=Math.max(0,attackTime-dt);
      const p=1-attackTime/.44;
      sword.rotation.z = -1.0 + Math.sin(p*Math.PI)*1.55;
      sword.rotation.y = -.95 + p*2.0;
      slash.visible = p > .12 && p < .72;
      slash.position.set(hero.position.x,2.13,hero.position.z);
      slash.rotation.set(-Math.PI/2,0,hero.rotation.y + 0.15);
      slash.material.opacity = Math.max(.05,.9*Math.sin(Math.PI*p));
      slash.scale.setScalar(.85+p*.5);
      if (!attackHit && p > .30) {
        attackHit=true;
        landHit();
      }
    } else {
      sword.rotation.z=THREE.MathUtils.lerp(sword.rotation.z,-.22,Math.min(1,dt*10));
      sword.rotation.y=THREE.MathUtils.lerp(sword.rotation.y,0,Math.min(1,dt*10));
      slash.visible=false;
    }
    if (heroHurt > 0) {
      heroHurt = Math.max(0,heroHurt-dt);
      heroVisual.visible = Math.floor(heroHurt*12)%2 === 0;
    } else {
      heroVisual.visible=true;
    }
  }

  function updateSlimes(dt) {
    for (const slime of slimes) {
      if (!slime.alive) continue;
      const root=slime.root;
      const visual=slime.visual;
      const bounce = Math.sin(t*4.0 + slime.phase);
      visual.position.y = Math.max(0,bounce)*.20;
      visual.scale.set(1+.055*bounce, 1-.07*bounce, 1+.055*bounce);
      if (slime.blink > 0) {
        slime.blink = Math.max(0,slime.blink-dt);
        visual.scale.multiplyScalar(1 + slime.blink*.35);
        slime.goo.children[0].material.emissive.setHex(0x472222);
        slime.goo.children[0].material.emissiveIntensity = slime.blink*1.2;
      } else {
        slime.goo.children[0].material.emissive.setHex(0x000000);
      }
      if (state !== "playing" || slime.knock > 0) {
        slime.knock=Math.max(0,slime.knock-dt);
        continue;
      }
      const hx=hero.position.x-root.position.x;
      const hz=hero.position.z-root.position.z;
      const dist=Math.hypot(hx,hz);
      let dx=0,dz=0,speed=.45;
      if (dist < 4.05 && dist > 0.01) {
        dx=hx/dist; dz=hz/dist; speed=1.75;
      } else {
        const destX=slime.home.x+Math.sin(t*.58+slime.phase)*.9;
        const destZ=slime.home.y+Math.cos(t*.48+slime.phase)*.85;
        const di=Math.hypot(destX-root.position.x,destZ-root.position.z);
        if (di>.13) { dx=(destX-root.position.x)/di; dz=(destZ-root.position.z)/di; }
      }
      root.position.x += dx*speed*dt;
      root.position.z += dz*speed*dt;
      const r=Math.hypot(root.position.x,root.position.z);
      if (r>8.18) { root.position.x *= 8.18/r; root.position.z *= 8.18/r; }
      if (dist < 1.16) hurt();
    }
  }

  function updateParticles(dt) {
    for (let i=particles.length-1;i>=0;i--) {
      const p=particles[i];
      p.ttl -= dt;
      p.velocity.y -= 7.8*dt;
      p.mesh.position.addScaledVector(p.velocity,dt);
      p.mesh.rotation.x += dt*6;
      p.mesh.rotation.z += dt*4;
      p.mesh.scale.setScalar(Math.max(.01,Math.min(1,p.ttl/.25)));
      if (p.ttl<=0) {
        scene.remove(p.mesh);
        p.mesh.geometry.dispose();
        p.mesh.material.dispose();
        particles.splice(i,1);
      }
    }
  }

  function animate(timeMs) {
    requestAnimationFrame(animate);
    const now = timeMs*.001;
    const dt = Math.min(.04,Math.max(0,now-(animate.previous || now)));
    animate.previous = now;
    t += dt;
    seaUniforms.uTime.value=t;
    clouds.forEach(c=>{ c.mesh.position.x=c.from+Math.sin(t*.24+c.speed)*2.4; });
    if (state==="playing") updateHero(dt);
    else {
      heroVisual.position.y = Math.sin(t*2)*.025;
      heroVisual.visible=true;
    }
    updateSlimes(dt);
    updateParticles(dt);
    const follow = state === "menu" ? 0 : .20;
    const destination = new THREE.Vector3(
      baseCamera.x + hero.position.x*follow,
      baseCamera.y,
      baseCamera.z + hero.position.z*follow
    );
    camera.position.lerp(destination,Math.min(1,dt*2.1));
    cameraAim.set(hero.position.x*follow,1.07,hero.position.z*follow);
    camera.lookAt(cameraAim);
    renderer.render(scene,camera);
  }

  function resize() {
    camera.aspect=window.innerWidth/window.innerHeight;
    camera.fov = window.innerWidth < 720 ? 59 : 49;
    camera.updateProjectionMatrix();
    renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1,1.65));
    renderer.setSize(window.innerWidth,window.innerHeight);
  }
  window.addEventListener("resize",resize);

  window.addEventListener("keydown",(ev)=>{
    const key=ev.key.toLowerCase();
    if ([" ","arrowleft","arrowright","arrowup","arrowdown"].includes(key)) ev.preventDefault();
    if (key==="r" && !ev.repeat) { reset(); return; }
    if (state!=="playing") return;
    keys.add(key);
    if (key===" " && !ev.repeat) strike();
  });
  window.addEventListener("keyup",(ev)=>keys.delete(ev.key.toLowerCase()));
  window.addEventListener("blur",()=>keys.clear());
  stage.addEventListener("pointerdown",(ev)=>{
    if (ev.button===0 && state==="playing") strike();
  });

  byId("playBtn").addEventListener("click",()=>{
    if (!audio) {
      try {
        const Ctx=window.AudioContext || window.webkitAudioContext;
        if (Ctx) audio=new Ctx();
      } catch (_ignored) {}
    }
    if (audio && audio.state==="suspended") audio.resume();
    reset();
  });
  byId("againBtn").addEventListener("click",reset);

  // The static menu world is already alive in the background.
  reset();
  state="menu";
  startPanel.classList.remove("hidden");
  endPanel.classList.add("hidden");
  toastEl.classList.remove("show");
  requestAnimationFrame(animate);
})();
