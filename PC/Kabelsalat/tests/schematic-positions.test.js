'use strict';
const assert=require('node:assert/strict'),pos=require('../schematic-positions.js');
assert.equal(pos.FACTOR,.62);
const old={id:'SW1_T1_LR01',u:pos.compact(.0513),v:pos.compact(.8373),
 sourceU:.0513,sourceV:.8373};
assert.ok(pos.legacy(old));
const migrated=pos.migrate(old,true);
assert.deepEqual(migrated,{...old,u:.0513,v:.8373});
assert.deepEqual(old,{id:'SW1_T1_LR01',u:.22181,v:.70913,sourceU:.0513,sourceV:.8373},
 'Legacy data must not mutate in place');
assert.deepEqual(pos.display(migrated,true),old,
 'Demo-only compaction matches old visual while preserving canonical source centers');
assert.equal(pos.display(migrated,false),migrated);
const moved={...migrated,u:.42,v:.55};
assert.equal(pos.migrate(moved,true),moved,'User edits must not be overwritten');
assert.equal(pos.display(moved,true),moved,'User edits must not be compacted again');
const other={id:'SW2',u:.35,v:.65};
assert.equal(pos.display(other,true),other,'Non-PDF holes unaffected');
const network={shafts:[
 {id:'13_U_028',snapshot:{sourceInfo:{sourceVersion:'PDF V2 (Konzept)'},walls:[{holes:[{...old}]}]}},
 {id:'13_T_021',snapshot:{sourceInfo:{sourceVersion:'HTML Quelle'},walls:[{holes:[{...old}]}]}}
]};
pos.transformProjectShunt(network);
assert.equal(network.shafts[0].snapshot.walls[0].holes[0].u,.0513);
assert.equal(network.shafts[1].snapshot.walls[0].holes[0].u,.22181);
console.log('OK: 62% compaction is reversible view-only; old untouched data migrates without touching edits');
