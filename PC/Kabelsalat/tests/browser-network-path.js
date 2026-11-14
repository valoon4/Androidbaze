'use strict';
/* Real Chromium integration: deterministic graph path search and alternatives,
 * safe CSV report, disconnected graph, source immutability, and map rendering.
 * Only synthetic/network-model coordinates; NEVER real installed cable routes.
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
  const page=await browser.newPage({viewport:{width:1520,height:900},acceptDownloads:true});
  const errors=[];page.on('pageerror',e=>errors.push(e.message));
  const out=path.resolve(__dirname,'../dist/visual-qa');
  fs.mkdirSync(out,{recursive:true});
  await page.goto('file://'+path.resolve(__dirname,'../index.html'),{waitUntil:'load',timeout:40000});
  await page.waitForFunction(()=>document.body.classList.contains('app-ready'),{timeout:30000});
  const info=await page.evaluate(()=>{
   const original=window.KabelsalatSamples.create('station');
   const shafts=original.network.shafts;
   if(shafts.length!==4)throw Error('Station demo needs four shafts');
   [0,1000,2000,1000].forEach((x,i)=>{shafts[i].x=x;shafts[i].z=i===3?1500:0;});
   const ids=shafts.map(x=>x.id);
   original.name='FIKTIVE NETZWEGFINDER-TESTDATEN';
   original.network.links=[
    {id:'E1',from:ids[0],to:ids[1],note:'Demo'},
    {id:'E2',from:ids[1],to:ids[2],note:'Demo'},
    {id:'E3',from:ids[0],to:ids[3],note:'Demo'},
    {id:'E4',from:ids[3],to:ids[2],note:'Demo'}
   ];
   original.network.routes=[];
   original.activeShaftId=ids[0];
   localStorage.setItem('eugi.schachtwand.3d.v1',JSON.stringify(original));
   localStorage.setItem('kabelsalat_ui_mode_v1','expert');
   return {ids};
  });
  await page.reload({waitUntil:'load'});
  await page.waitForFunction(()=>document.body.classList.contains('app-ready'),{timeout:30000});
  if(await page.locator('body').evaluate(x=>x.classList.contains('explore-mode')))
   await page.locator('#toggleExpert').click();
  assert.equal(await page.locator('#netPlannerFind').isDisabled(),false);
  assert.match(await page.locator('#netTopologyStatus').textContent(),/1 Netzkomponente/);
  assert.match(await page.locator('#netTopologyStatus').textContent(),/Zyklus/);
  await page.locator('#netPlannerFrom').selectOption(info.ids[0]);
  await page.locator('#netPlannerTo').selectOption(info.ids[2]);
  const original=await page.evaluate(()=>localStorage.getItem('eugi.schachtwand.3d.v1'));
  await page.locator('#netPlannerFind').click();
  await page.waitForFunction(()=>document.querySelector('#viewport').classList.contains('network-open'));
  const report=await page.locator('#netPlannerSummary').innerText();
  assert.match(report,/2 Links/);
  assert.match(report,/2(?:[,\.]0)? m \(Modell\)/);
  assert.match(report,/Keine realen Trassen-/);
  assert.equal(await page.locator('#netPlannerList [data-network-alternative]').count(),2);
  assert.equal(await page.locator('#netPlannerExport').isDisabled(),false);
  assert.equal(await page.locator('#netPlannerShow').isDisabled(),false);
  assert.equal(await page.locator('#netPlannerList [data-network-alternative="-1"]').getAttribute('aria-pressed'),'true');
  assert.equal(await page.evaluate(()=>localStorage.getItem('eugi.schachtwand.3d.v1')),original,
   'Search must not modify persisted shafts/cables/network');
  await page.screenshot({path:path.join(out,'network-path-shortest-3d-linked-shafts.png'),fullPage:true});
  await page.locator('#netPlannerList [data-network-alternative="0"]').click();
  assert.equal(await page.locator('#netPlannerList [data-network-alternative="0"]').getAttribute('aria-pressed'),'true');
  assert.match(await page.locator('#netPlannerSummary').textContent(),/3[,\.]6 m \(Modell\)/);
  await page.screenshot({path:path.join(out,'network-path-alternative-schematic.png'),fullPage:true});
  const waiter=page.waitForEvent('download');
  await page.locator('#netPlannerExport').click();
  const dl=await waiter;
  assert.match(dl.suggestedFilename(),/netzweg-schematisch\.csv$/);
  await page.locator('#netPlannerMode').selectOption('hops');
  await page.locator('#netPlannerFind').click();
  assert.match(await page.locator('#netPlannerSummary').textContent(),/Übergangs-Suche/);
  assert.equal(await page.evaluate(()=>localStorage.getItem('eugi.schachtwand.3d.v1')),original);
  // Deliberately disconnected. The application must NEVER bridge missing links.
  await page.evaluate(()=>{
   const p=JSON.parse(localStorage.getItem('eugi.schachtwand.3d.v1'));
   p.network.links=[];localStorage.setItem('eugi.schachtwand.3d.v1',JSON.stringify(p));
  });
  await page.reload({waitUntil:'load'});
  await page.waitForFunction(()=>document.body.classList.contains('app-ready'),{timeout:30000});
  if(await page.locator('body').evaluate(x=>x.classList.contains('explore-mode')))
   await page.locator('#toggleExpert').click();
  await page.locator('#netPlannerFrom').selectOption(info.ids[0]);
  await page.locator('#netPlannerTo').selectOption(info.ids[2]);
  const disconnectedSource=await page.evaluate(()=>localStorage.getItem('eugi.schachtwand.3d.v1'));
  await page.locator('#netPlannerFind').click();
  assert.match(await page.locator('#netPlannerSummary').textContent(),/Kein gespeicherter Netzweg/);
  assert.match(await page.locator('#netTopologyStatus').textContent(),/4 Netzkomponente/);
  assert.match(await page.locator('#netTopologyStatus').textContent(),/isoliert/);
  assert.equal(await page.locator('#netPlannerExport').isDisabled(),true);
  assert.equal(await page.locator('#netPlannerShow').isDisabled(),true);
  assert.equal(await page.evaluate(()=>localStorage.getItem('eugi.schachtwand.3d.v1')),disconnectedSource);
  await page.screenshot({path:path.join(out,'network-path-unreachable-no-invented-link.png'),fullPage:true});
  assert.deepEqual(errors,[],'Browser must not throw runtime errors');
  console.log('KAB NETZWEGFINDER BROWSER OK: path/alternative/distance/hops/CSV/immutability/disconnected');
 }finally{await browser.close();}
})().catch(err=>{console.error(err);process.exitCode=1;});
