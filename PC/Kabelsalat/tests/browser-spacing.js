'use strict';
/* KAB-0051..0053: end-to-end real Chromium UI verification.
 * All fixtures are synthetic and intentionally contain overlapping holes.
 */
const assert=require('node:assert/strict');
const fs=require('node:fs'),path=require('node:path');
(async()=>{
 const playwright=require(process.env.PLAYWRIGHT_CORE||'playwright-core');
 const browser=await playwright.chromium.launch({
  executablePath:process.env.CHROME_BIN||'/usr/bin/google-chrome',headless:true,
  args:['--no-sandbox','--disable-dev-shm-usage','--enable-unsafe-swiftshader',
   '--use-gl=angle','--use-angle=swiftshader']
 });
 try{
  const page=await browser.newPage({viewport:{width:1500,height:930}});
  page.on('dialog',d=>d.accept());
  const errors=[];page.on('pageerror',e=>errors.push(e.message));
  const out=path.resolve(__dirname,'../dist/visual-qa');
  fs.mkdirSync(out,{recursive:true});
  await page.goto('file://'+path.resolve(__dirname,'../index.html'),{waitUntil:'load',timeout:40000});
  await page.waitForFunction(()=>document.body.classList.contains('app-ready'),{timeout:30000});
  // Deliberately create the overlap in a fake source-less demo only.
  await page.evaluate(()=>{
   const p=window.KabelsalatSamples.create('diameters');
   p.name='FIKTIVE TEST-Demo · 2 Rohre mit Überschneidung';
   p.walls[0].holes=p.walls[0].holes.slice(0,2);
   p.walls[0].holes[0].u=.48;p.walls[0].holes[0].v=.5;p.walls[0].holes[0].dn=110;
   p.walls[0].holes[1].u=.52;p.walls[0].holes[1].v=.5;p.walls[0].holes[1].dn=110;
   p.cables=[{id:'TEST-CABLE',name:'Ein unverändertes Testkabel',
    color:'#3377aa',group:'Weitere',diameter:12,route:[],
    a:{wall:'SW1',hole:p.walls[0].holes[0].id},
    b:{wall:'SW1',hole:p.walls[0].holes[1].id}}];
   p.sourceInfo=null;
   p.network.shafts[0].snapshot=window.KabelsalatNetwork.snapshot(p);
   localStorage.setItem('eugi.schachtwand.3d.v1',JSON.stringify(p));
   localStorage.setItem('kabelsalat_ui_mode_v1','expert');
  });
  await page.reload({waitUntil:'load'});
  await page.waitForFunction(()=>document.body.classList.contains('app-ready'),{timeout:30000});
  if(await page.locator('body').evaluate(x=>x.classList.contains('explore-mode')))
   await page.locator('#toggleExpert').click();
  assert.equal(await page.locator('#spacingPreview').isDisabled(),false);
  const before=await page.evaluate(()=>localStorage.getItem('eugi.schachtwand.3d.v1'));
  const beforeObj=JSON.parse(before);
  await page.locator('#spacingGap').fill('60');
  await page.locator('#spacingEdge').fill('40');
  await page.locator('#spacingPreview').click();
  assert.match(await page.locator('#spacingCompare').textContent(),/Vorher → Nachher/);
  assert.equal(await page.locator('#spacingApply').isDisabled(),false);
  assert.equal(await page.evaluate(()=>localStorage.getItem('eugi.schachtwand.3d.v1')),before,
   'Preview must not persist anything');
  await page.screenshot({path:path.join(out,'spacing-before-after-preview.png'),fullPage:true});
  await page.locator('#spacingApply').click();
  const after=JSON.parse(await page.evaluate(()=>localStorage.getItem('eugi.schachtwand.3d.v1')));
  assert.notDeepEqual(after.walls[0].holes.map(h=>[h.u,h.v]),
   beforeObj.walls[0].holes.map(h=>[h.u,h.v]));
  assert.deepEqual(after.cables[0].a,beforeObj.cables[0].a);
  assert.deepEqual(after.cables[0].b,beforeObj.cables[0].b);
  assert.equal(after.cables[0].id,beforeObj.cables[0].id);
  await page.screenshot({path:path.join(out,'spacing-after-apply-3d.png'),fullPage:true});
  await page.locator('#undo').click();
  const undone=JSON.parse(await page.evaluate(()=>localStorage.getItem('eugi.schachtwand.3d.v1')));
  assert.deepEqual(undone.walls[0].holes.map(h=>[h.u,h.v]),
   beforeObj.walls[0].holes.map(h=>[h.u,h.v]));
  await page.locator('#redo').click();
  const redone=JSON.parse(await page.evaluate(()=>localStorage.getItem('eugi.schachtwand.3d.v1')));
  assert.deepEqual(redone.walls[0].holes.map(h=>[h.u,h.v]),
   after.walls[0].holes.map(h=>[h.u,h.v]));
  // Original PDF source is deliberately protected at UI level.
  await page.evaluate(()=>{
   const p=window.KabelsalatSamples.create('pdf-u028');
   localStorage.setItem('eugi.schachtwand.3d.v1',JSON.stringify(p));
  });
  await page.reload({waitUntil:'load'});
  await page.waitForFunction(()=>document.body.classList.contains('app-ready'),{timeout:30000});
  if(await page.locator('body').evaluate(x=>x.classList.contains('explore-mode')))
   await page.locator('#toggleExpert').click();
  assert.equal(await page.locator('#spacingPreview').isDisabled(),true,
   'Original PDF must never be auto-relocated');
  assert.deepEqual(await page.evaluate(()=>JSON.parse(localStorage.getItem('eugi.schachtwand.3d.v1'))
   .walls.map(w=>w.holes.length)),[29,13,26,23]);
  assert.deepEqual(errors,[],'Browser must not throw runtime errors');
  console.log('KAB-0051/52/53 BROWSER OK: preview, 2D/3D state, explicit apply, cable-ID invariance, undo/redo and PDF protection');
 }finally{await browser.close();}
})().catch(err=>{console.error(err);process.exitCode=1;});
