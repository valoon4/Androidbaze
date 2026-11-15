'use strict';
/* KAB-0397–0414: deterministic multi-shaft faceted search; no invented matches. */
const assert=require('node:assert/strict');
const q=require('../quality.js');
const fs=require('node:fs'),path=require('node:path');
const net={shafts:[
 {id:'S1',name:'Nordschacht',x:1400,z:0,snapshot:{sourceInfo:{
  sourceVersion:'PDF V2 2026-11-15',sourceDate:'2026-11-15'},
  walls:[{id:'SW1',name:'Nordwand',holes:[
   {id:'SW1_T1_LR01',sourceAddress:'Trasse 1'}]}],
  cables:[
   {id:'MS-SNG-0038',name:'Äther Kabel',group:'MS',
    a:{wall:'SW1',hole:'SW1_T1_LR01'},b:null,reviewStatus:'Zur Prüfung'},
   {id:'OLA-01',name:'Alpha',group:'OLA',
    a:{wall:'SW1',hole:'SW1_T1_LR01'},b:{wall:'SW1',hole:'SW1_T1_LR01'}}]}},
 {id:'S2',name:'Südschacht',x:9000,z:200,snapshot:{sourceInfo:{
  sourceVersion:'Zeichnung A',sourceDate:'2025-03-04'},
  walls:[{id:'SW1',name:'Südwand',holes:[{id:'SW1_T2_LR01',sourceAddress:'Trasse 2'}]}],
  cables:[
   {id:'MS-SNG-0038',name:'Äther Kabel',group:'MS',a:null,b:null},
   {id:'TK-1',name:'Sonstige',group:'Weitere',a:null,b:null}]}}
],reviews:[{shaftId:'S2',entityType:'cable',entityId:'MS-SNG-0038',status:'Vor Ort abgeglichen'}]};
const search=(str,opts={})=>q.searchNetwork(net,str,120,opts);
assert.ok(search('S1').some(x=>x.kind==='shaft'&&x.shaftId==='S1'));
assert.ok(search('Nordschacht').some(x=>x.kind==='shaft'&&x.shaftId==='S1'));
assert.ok(search('S1SW1').some(x=>x.kind==='wall'&&x.wallId==='SW1'));
assert.ok(search('trasse1').some(x=>x.kind==='hole'&&x.holeId==='SW1_T1_LR01'));
assert.equal(search('MSSNG0038').filter(x=>x.kind==='cable').length,2);
assert.equal(search('aetherkabel').filter(x=>x.kind==='cable').length,2);
assert.equal(search('',{group:'OLA'}).length,1);
assert.equal(search('',{group:'MS'}).length,2);
assert.equal(search('',{group:'Weitere'}).length,1);
assert.equal(search('',{unresolved:true}).filter(x=>x.kind==='cable').length,3);
assert.equal(search('',{review:'Zur Prüfung'}).length,1);
assert.equal(search('',{review:'Vor Ort abgeglichen'}).length,1);
assert.equal(search('',{sourceVersion:'20261115'}).filter(x=>x.kind==='cable').length,2);
assert.ok(search('no-real-id').length===0);
assert.equal(search('MSSNG0038').filter(x=>x.kind==='cable').map(x=>x.shaftId).join(','),'S1,S2',
 'Duplicate names need all distinct shaft identities');
const before=JSON.stringify(net);
search('alpha');search('sw1', {group:'OLA',sourceVersion:'PDFV2'});
assert.equal(JSON.stringify(net),before,'Search can never change model, reviews or source');
assert.deepEqual(search(''),[]);
const massive={shafts:[{id:'BIG',snapshot:{
 walls:[],cables:Array.from({length:5500},(_,i)=>({id:'C'+i,name:'Kabel '+i,
 group:'MS',a:null,b:null}))}}],reviews:[]};
assert.equal(q.searchNetwork(massive,'C5499',120).filter(x=>x.kind==='cable').length,1);
assert.equal(q.searchNetwork(massive,'',60,{group:'MS'}).length,60,'Result cap must hold');
const html=fs.readFileSync(path.join(__dirname,'../index.html'),'utf8');
const app=fs.readFileSync(path.join(__dirname,'../app.js'),'utf8');
for(const id of ['globalSearch','globalResults','globalFilterGroup','globalFilterUnresolved',
 'globalFilterReview','globalFilterSource'])
 assert.ok(html.includes('id="'+id+'"'),'Search UI missing '+id);
assert.ok(app.includes("if(hit.kind==='wall')")&&app.includes("if(hit.kind==='cable')"));
assert.ok(app.includes('qualityModel.searchNetwork(net,query,90,filters)'));
console.log('GLOBAL SEARCH GIANT OK: 18 requirements incl 5,500 cable stress and source-aware nonmutating filters');