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
const actual=(result.match(/<script>/g)||[]).length;
assert.equal(actual,9,'All nine scripts must be embedded');
const hashes=[...result.matchAll(/'sha256-([a-zA-Z0-9+/=]+)'/g)].map(x=>x[1]);
assert.equal(hashes.length,9,'Each script needs a CSP SHA-256 hash');
const bodies=[...result.matchAll(/<script>([\s\S]*?)<\/script>/g)].map(x=>x[1]);
assert.equal(bodies.length,9);
for(let i=0;i<9;i++){
 const computed=crypto.createHash('sha256').update(bodies[i]).digest('base64');
 assert.equal(computed,hashes[i],'CSP must hash exact browser script contents');
}
assert.ok(result.includes('HTML-Referenz'));
console.log('OK: standalone HTML includes 9 scripts, source examples and strict CSP hashes');
