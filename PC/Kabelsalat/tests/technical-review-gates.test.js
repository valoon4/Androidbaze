'use strict';
const assert=require('node:assert/strict');
const g=require('../technical-review-gates.js');
const real={shaftId:'13_U_028',wallId:'SW1',
 source:{document:'Originalplan',version:'2',page:'21'},
 undersideMm:80,openingMm:110,conflictingThresholdsMm:[75,100]};
const original=JSON.stringify(real);
const a=g.review(real);
assert.equal(a.status,'offen');assert.equal(a.passed,false);
assert.equal(a.approved,false);assert.equal(a.compliance,null);
assert.equal(a.variant,null);assert.equal(a.ruleMm,null);
assert.equal(a.requiresExternalReview,true);
assert.deepEqual(a.conflictingThresholdsMm,[75,100]);
assert.match(a.neutralFinding,/75\/100/);
assert.equal(a.source.document,'Originalplan');
assert.equal(a.source.version,'2');
assert.equal(a.source.page,'21');
assert.equal(a.source.sourceVerified,false);
assert.match(g.summary(a),/NACHWEIS OFFEN/);
assert.match(g.summary(a),/Keine automatisch bestätigte/);
assert.equal(JSON.stringify(real),original);
const cases=[
 {name:'unknown height',value:{...real,undersideMm:undefined},text:/Unterkante/},
 {name:'unknown diameter',value:{...real,openingMm:null},text:/durchmesser/},
 {name:'no original',value:{...real,source:null},text:/Originalplan/},
 {name:'no source page',value:{...real,source:{document:'PDF',version:'2'}},text:/Originalplan/},
 {name:'no wall',value:{...real,wallId:'SW5'},text:/Wand-ID/},
 {name:'no shaft',value:{...real,shaftId:''},text:/Schacht-ID/},
 {name:'boundary below',value:{...real,openingMm:0},text:/durchmesser/},
 {name:'negative floor',value:{...real,undersideMm:-1},text:/Unterkante/},
 {name:'invalid numeric string',value:{...real,undersideMm:'80'},text:/Unterkante/},
 {name:'missing both rules',value:{...real,conflictingThresholdsMm:[]},text:/Fachliche Abnahme/},
 {name:'75 only',value:{...real,conflictingThresholdsMm:[75]},text:/Fachliche Abnahme/},
 {name:'100 only',value:{...real,conflictingThresholdsMm:[100]},text:/Fachliche Abnahme/}
];
for(const x of cases){
 const b=g.review(x.value);assert.equal(b.status,'offen',x.name);
 assert.equal(b.passed,false,x.name);assert.equal(b.approved,false,x.name);
 assert.equal(b.ruleMm,null,x.name);assert.equal(b.variant,null,x.name);
 assert.match(b.neutralFinding,x.text,x.name);
}
const simulated=g.review({...real,synthetic:true,source:null});
assert.equal(simulated.synthetic,true);assert.equal(simulated.passed,false);
assert.throws(()=>g.review(null),/Prüfeingabe/);
assert.throws(()=>g.summary({status:'freigegeben',passed:true}),/neutraler/);
assert.deepEqual(g.source({document:'A',page:'21',version:'2'}),{
 document:'A',page:'21',version:'2'
});
assert.equal(g.source({document:'A',version:'2'}),null);
const status=require('node:fs').readFileSync(require('node:path').join(__dirname,'../TASKS/BRO_CHAT_CROSSWALK.md'),'utf8');
assert.ok(status.includes('75 mm'));assert.ok(status.includes('100 mm'));
console.log('TECHNICAL REVIEW GATE OK: missing survey/diameter, two contradictory thresholds, 12 negatives, zero invented compliance');
