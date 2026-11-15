'use strict';
/* Actual Chromium regression for cable catalogue, endpoints, pipe deletion and network references. */
const assert=require('node:assert/strict'),path=require('node:path'),fs=require('node:fs');
(async()=>{
 const playwright=require(process.env.PLAYWRIGHT_CORE||'playwright-core');
 const browser=await playwright.chromium.launch({
  executablePath:process.env.CHROME_BIN||'/usr/bin/google-chrome',headless:true,
  args:['--no-sandbox','--disable-dev-shm-usage','--enable-unsafe-swiftshader','--use-gl=angle','--use-angle=swiftshader']
 });
 try{
  const page=await browser.newPage({viewport:{width:1550,height:980}});
  const errors=[];page.on('pageerror',e=>errors.push(e.message));
  page.on('dialog',d=>d.accept());
  await page.goto('file://'+path.resolve(__dirname,'../index.html'),{waitUntil:'load',timeout:40000});
  await page.waitForFunction(()=>document.body.classList.contains('app-ready'),{timeout:30000});
  await page.evaluate(()=>{
   const p=window.KabelsalatSamples.create('diameters');
   p.name='Fiktiver Kabel-Editor-Integrationstest';p.sourceInfo=null;
   p.walls.forEach(w=>w.holes=[]);
   p.walls[0].holes=[
    {id:'LR1',u:.4,v:.4,dn:50,sourceAddress:'Trasse A'},
    {id:'LR2',u:.7,v:.7,dn:110,sourceAddress:'Trasse B'}];
   p.walls[1].holes=[{id:'LR1',u:.6,v:.6,dn:30,sourceAddress:'Trasse Z'}];
   p.cables=[{id:'c1',name:'Bestandskabel',group:'Weitere',color:'#398bdb',diameter:12,route:[],
    a:{wall:'SW1',hole:'LR2'},b:{wall:'SW2',hole:'LR1'}}];
   p.network.routes=[{id:'R1',name:'Netzroute zur Prüfung',group:'Weitere',
    steps:[{shaftId:p.activeShaftId||'S1',cableId:'c1'}]}];
   p.network.shafts[0].snapshot=window.KabelsalatNetwork.snapshot(p);
   localStorage.setItem('eugi.schachtwand.3d.v1',JSON.stringify(p));
   localStorage.setItem('kabelsalat_ui_mode_v1','expert');
  });
  await page.reload({waitUntil:'load'});
  await page.waitForFunction(()=>document.body.classList.contains('app-ready'),{timeout:30000});
  if(await page.locator('body').evaluate(x=>x.classList.contains('explore-mode')))
   await page.locator('#toggleExpert').click();
  const read=()=>page.evaluate(()=>JSON.parse(localStorage.getItem('eugi.schachtwand.3d.v1')));
  const base=await read();
  await page.locator('#pipeSort').selectOption('dn');
  assert.match(await page.locator('#pipeList').textContent(),/DN30/);
  await page.locator('#addCable').click();
  let updated=await read();
  assert.equal(updated.cables.length,2);
  const id=updated.cables[1].id;
  assert.equal(id,'cable-0001');
  await page.locator('#cableName').fill('Neues Kabeleditor-Kabel');
  await page.locator('#cableName').dispatchEvent('change');
  await page.locator('#cableDiameter').fill('22.5');
  await page.locator('#cableDiameter').dispatchEvent('change');
  await page.locator('#cableGroup').selectOption('OLA');
  await page.locator('#cableCategory').fill('Datentechnik');
  await page.locator('#cableCategory').dispatchEvent('change');
  await page.locator('#newCableType').fill('Fiktiver Prüftyp');
  await page.locator('#newCableTypeDiameter').fill('22.5');
  await page.locator('#addCableType').click();
  await page.locator('#cableA').selectOption('SW1|LR1');
  await page.locator('#cableB').selectOption('SW2|LR1');
  updated=await read();
  const c=updated.cables.find(c=>c.id===id);
  assert.equal(c.name,'Neues Kabeleditor-Kabel');
  assert.equal(c.diameter,22.5);assert.equal(c.group,'OLA');
  assert.equal(c.technicalCategory,'Datentechnik');
  assert.equal(c.typeId,updated.cableTypes[0].id);
  assert.deepEqual(c.a,{wall:'SW1',hole:'LR1'});
  assert.deepEqual(c.b,{wall:'SW2',hole:'LR1'});
  await page.locator('#swapCable').click();
  updated=await read();
  assert.deepEqual(updated.cables.find(x=>x.id===id).a,{wall:'SW2',hole:'LR1'});
  await page.locator('#copyCable').click();
  updated=await read();
  assert.equal(updated.cables.length,3);
  assert.equal(updated.cables[2].sourceCopyOf,id);
  assert.equal(new Set(updated.cables.map(x=>x.id)).size,3);
  await page.screenshot({path:(()=>{
   const out=path.resolve(__dirname,'../dist/visual-qa');fs.mkdirSync(out,{recursive:true});
   return path.join(out,'cable-editor-giant.png');})(),fullPage:true});
  await page.locator('#pipeList [data-pipe-wall="SW1"][data-pipe-id="LR1"]').click();
  await page.locator('#deleteHole').click();
  updated=await read();
  assert.equal(updated.walls[0].holes.some(h=>h.id==='LR1'),false);
  assert.equal(updated.walls[1].holes.some(h=>h.id==='LR1'),true);
  const linked=updated.cables.filter(x=>x.id!==base.cables[0].id);
  assert.equal(linked.every(x=>x.b===null&&x.unresolvedB?.hole==='LR1'),true);
  await page.locator('#undo').click();
  updated=await read();assert.equal(updated.walls[0].holes.some(h=>h.id==='LR1'),true);
  await page.locator('#redo').click();
  updated=await read();assert.equal(updated.walls[0].holes.some(h=>h.id==='LR1'),false);
  await page.locator('#cableList [data-cable="c1"]').click();
  await page.locator('#deleteCable').click();
  updated=await read();
  assert.equal(updated.cables.some(x=>x.id==='c1'),false);
  assert.equal(updated.network.routes[0].steps[0].cableId,'c1',
   'Deleting cable must not silently reroute network plans');
  await page.reload({waitUntil:'load'});
  updated=await read();
  assert.equal(updated.cableTypes[0].name,'Fiktiver Prüftyp');
  assert.equal(updated.cables.find(x=>x.id===id).unresolvedB.hole,'LR1');
  assert.deepEqual(errors,[],'No uncaught browser errors');
  console.log('OK: real Chromium cable creation, type, sorting, endpoints, copy, safe remove, Undo/Redo, network-warning preservation');
 }finally{await browser.close();}
})().catch(e=>{console.error(e);process.exitCode=1;});
