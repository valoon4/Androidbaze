'use strict';
const assert=require('node:assert/strict'),crypto=require('node:crypto');
const builder=require('../build-standalone.js');
const result=builder.build();
assert.ok(result.startsWith('<!doctype html>'));
assert.ok(result.includes('Kabelsalat'));
assert.ok(result.includes('KabelsalatReferences'));
assert.ok(result.includes('KabelsalatSamples'));
assert.ok(!/<script src=/.test(result),'Offline file must not load external scripts');
const csp=result.match(/<meta http-equiv="Content-Security-Policy" content="([^"]+)">/)?.[1];
assert.ok(csp,'Main document CSP should be present');
assert.ok(!csp.includes("script-src 'unsafe-inline'"),'Unsafe script execution must not be permitted');
assert.ok(csp.includes("'sha256-"),'Inline scripts must be individually hash-authorized');
const fs=require('node:fs'),path=require('node:path');
const hashes=[...csp.matchAll(/'sha256-([a-zA-Z0-9+/=]+)'/g)].map(x=>x[1]);
const sources=['recovery.js','pdf-geometry.js','schematic-positions.js','spacing-adjust.js','pipe-dimensions.js','photo-candidates.js','concrete-wall.js','construction-v2.js','pdf-visual.js','vendor/three.min.js','editor2d.js','components.js','network.js','network-route-planner.js',
 'quality.js','reports.js','reference-data.js','examples.js','app.js'];
assert.equal(hashes.length,sources.length,'CSP must authorize all scripts exactly');
for(const src of sources){
 const code=fs.readFileSync(path.join(__dirname,'..',src),'utf8').replace(/<\/script/gi,'<\\/script');
 const hash=crypto.createHash('sha256').update(code).digest('base64');
 assert.ok(hashes.includes(hash),'CSP must contain SHA-256 for '+src);
 assert.ok(result.includes('<script>'+code+'</script>'),'Complete script embedded: '+src);
}
assert.ok(result.includes('HTML-Referenz'));
console.log('OK: standalone HTML includes 19 scripts, source examples and strict CSP hashes');
