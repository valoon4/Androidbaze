'use strict';
const assert=require('node:assert/strict'),r=require('../reports.js');
const p={name:'<script>alert(1)</script>',network:{shafts:[{id:'S1',name:'A & <B>',
 x:0,z:0,snapshot:{dims:{w:2000,d:1800,h:2100},
 walls:[{id:'SW1',holes:[{id:'h1',u:.5,v:.5,dn:70}]}],
 cables:[{id:'c1',name:'<img src=x onerror=alert(1)>',group:'OLA',a:null,b:null}]}}],
 links:[],routes:[],reviews:[],audit:[]}};
const result={counts:{error:0,warning:1},config:{minPipeGapMm:null,minEdgeMm:20,minBendRadiusMm:null},
 issues:[{severity:'warning',code:'X',shaftId:'S1',ref:'c1',detail:'<problem>'}]};
const html=r.reportHTML(p,result);
assert.ok(html.includes('Kabelsalat Report v1'));
assert.ok(html.includes('&lt;script&gt;alert(1)&lt;/script&gt;'));
assert.ok(!html.includes('<problem>'));
const viewer=r.viewerHTML(p);
assert.ok(viewer.includes('Offline-Projektviewer'));
assert.ok(viewer.includes('Schematische Rohrpositionen – keine vermessenen Koordinaten'),
 'Offline viewer must clearly disclose unsurveyed positions');
assert.ok(!viewer.includes('Leerrohrpositionen in mm. Darstellung nicht zur Ausführungsplanung geeignet.'),
 'Old misleading millimetre claim must disappear');
assert.ok(viewer.includes("const pdf=p.sourceInfo?.sourceVersion?.startsWith('PDF V2')"),
 'PDF viewer must use actual PDF-wall span and not old HTML-wall orientation');
assert.ok(html.includes('Modell- und PDF-Vektorkoordinaten sind keine vermessenen Rohr- oder Schachtpositionen.'),
 'Printed report must declare schematic geometry');
assert.ok(html.includes('Projektwert, nicht vermessen'),'Print report must not imply measured shaft coordinates');
const pdfProject=JSON.parse(JSON.stringify(p));pdfProject.network.shafts[0].snapshot.sourceInfo={
 sourceVersion:'PDF V2 (nicht vermessen)',geometryStatus:'schematic'};
pdfProject.network.shafts[0].snapshot.walls[0].holes[0].sourceU=.4;
pdfProject.network.shafts[0].snapshot.walls[0].holes[0].sourceV=.5;
const pdfViewer=r.viewerHTML(pdfProject);
assert.ok(pdfViewer.includes('PDF V2 (nicht vermessen)')&&pdfViewer.includes('sourceU'),
 'Source data must remain available in offline export without declaring surveyed millimetres');
assert.ok(viewer.includes('const DATA='));
assert.ok(!viewer.includes('<img src=x onerror=alert(1)>'));
assert.ok(viewer.includes('\\\\u003c')||viewer.includes('\\u003c'));
assert.ok(viewer.includes('addEventListener'));
console.log('OK: print-ready report and read-only offline viewer escape input');
