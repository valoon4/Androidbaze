'use strict';
/* KAB-0355/0356/0365–0378 integration in genuine Chromium.
 * 30 shafts and 29 long edges; separate broken-physical-continuity case.
 * All fixtures are synthetic, never real infrastructure or approved dimensions.
 */
const assert=require('node:assert/strict');
const fs=require('node:fs'),path=require('node:path');
(async()=>{
 const pw=require(process.env.PLAYWRIGHT_CORE||'playwright-core');
 const browser=await pw.chromium.launch({executablePath:process.env.CHROME_BIN||'/usr/bin/google-chrome',
  headless:true,args:['--no-sandbox','--disable-dev-shm-usage','--enable-unsafe-swiftshader',
   '--use-gl=angle','--use-angle=swiftshader']});
 try{
  const page=await browser.newPage({viewport:{width:1500,height:960}});
  const errors=[];page.on('pageerror',e=>errors.push(e.message));
  const out=path.resolve(__dirname,'../dist/visual-qa');fs.mkdirSync(out,{recursive:true});
  await page.goto('file://'+path.resolve(__dirname,'../index.html'),{waitUntil:'load',timeout:40000});
  await page.waitForFunction(()=>document.body.classList.contains('app-ready'),{timeout:30000});
  await page.evaluate(()=>{
   const p=window.KabelsalatSamples.create('station');
   const template=p.network.shafts[0];
   p.network.shafts=Array.from({length:30},(_,i)=>({
    ...JSON.parse(JSON.stringify(template)),id:'S'+String(i+1).padStart(2,'0'),
    name:'Fiktiv '+(i+1),x:i*500000,z:i%2?200000:0
   }));
   p.network.links=Array.from({length:29},(_,i)=>({
    id:'L'+(i+1),from:'S'+String(i+1).padStart(2,'0'),
    to:'S'+String(i+2).padStart(2,'0'),note:'Fiktive Verbindung'
   }));
   p.network.routes=[];p.name='FIKTIVE 30-SCHACHT-DEMOMATRIX';p.activeShaftId='S01';
   localStorage.setItem('eugi.schachtwand.3d.v1',JSON.stringify(p));
   localStorage.setItem('kabelsalat_ui_mode_v1','expert');
  });
  await page.reload({waitUntil:'load'});
  await page.waitForFunction(()=>document.body.classList.contains('app-ready'),{timeout:30000});
  if(await page.locator('body').evaluate(e=>e.classList.contains('explore-mode')))
   await page.locator('#toggleExpert').click();
  assert.equal(await page.locator('#netPlannerFrom option').count(),30);
  assert.match(await page.locator('#netTopologyStatus').textContent(),/30 Schächte/);
  assert.match(await page.locator('#netTopologyStatus').textContent(),/1 Netzkomponente/);
  await page.locator('#netPlannerFrom').selectOption('S01');
  await page.locator('#netPlannerTo').selectOption('S30');
  const original=await page.evaluate(()=>localStorage.getItem('eugi.schachtwand.3d.v1'));
  await page.locator('#netPlannerFind').click();
  await page.waitForFunction(()=>document.querySelector('#viewport').classList.contains('network-open'));
  assert.match(await page.locator('#netPlannerSummary').textContent(),/29 Links/);
  assert.equal(await page.locator('#netPlannerShow').isDisabled(),false);
  assert.equal(await page.evaluate(()=>localStorage.getItem('eugi.schachtwand.3d.v1')),original);
  await page.screenshot({path:path.join(out,'network-30-shafts-long-distance.png'),fullPage:true});
  // Continuity test: a model link exists, but a middle shaft has a broken
  // local hole, a group mismatch, and no physical transition evidence.
  await page.evaluate(()=>{
   const p=window.KabelsalatSamples.create('station');
   p.name='FIKTIVE KONTINUITAETS-LUECKE';p.network.shafts=p.network.shafts.slice(0,3);
   const ss=p.network.shafts;
   p.network.links=[
    {id:'L1',from:ss[0].id,to:ss[1].id,note:'Ungeprüft'},
    {id:'L2',from:ss[1].id,to:ss[2].id,note:'Ungeprüft'}
   ];
   ss[1].snapshot.cables[0].b={wall:'SW1',hole:'NICHT_VORHANDEN'};
   ss[1].snapshot.cables[0].group='MS';
   p.network.routes=[{id:'R-CONTINUITY',name:'Fehlerhafter Testverlauf',group:'OLA',
    steps:ss.map(s=>({shaftId:s.id,cableId:s.snapshot.cables[0].id}))}];
   p.activeShaftId=ss[0].id;
   localStorage.setItem('eugi.schachtwand.3d.v1',JSON.stringify(p));
  });
  await page.reload({waitUntil:'load'});
  await page.waitForFunction(()=>document.body.classList.contains('app-ready'),{timeout:30000});
  if(await page.locator('body').evaluate(e=>e.classList.contains('explore-mode')))
   await page.locator('#toggleExpert').click();
  const before=await page.evaluate(()=>localStorage.getItem('eugi.schachtwand.3d.v1'));
  await page.locator('#netRouteSelect').selectOption('R-CONTINUITY');
  const status=await page.locator('#netTraceStatus').textContent();
  assert.match(status,/Lokale Unterbrechung/);
  assert.match(status,/Systemkonflikt/);
  assert.match(status,/Kabelkontinuität UNGEPRÜFT|physikalischen Schachtübergänge|keine verifizierte/i);
  assert.equal(await page.evaluate(()=>localStorage.getItem('eugi.schachtwand.3d.v1')),before);
  await page.locator('#netShowTrace').click();
  await page.screenshot({path:path.join(out,'network-cable-broken-local-endpoint.png'),fullPage:true});
  // Prior PDF reference remains independently loadable after all synthetic cases.
  await page.evaluate(()=>{
   const original=window.KabelsalatSamples.create('pdf-u028');
   localStorage.setItem('eugi.schachtwand.3d.v1',JSON.stringify(original));
  });
  await page.reload({waitUntil:'load'});
  await page.waitForFunction(()=>document.body.classList.contains('app-ready'),{timeout:30000});
  const ref=await page.evaluate(()=>JSON.parse(localStorage.getItem('eugi.schachtwand.3d.v1')));
  assert.deepEqual(ref.walls.map(w=>w.holes.length),[29,13,26,23]);
  assert.equal(ref.sourceInfo?.shaft,'13_U_028');
  assert.deepEqual(errors,[]);
  console.log('NETZ GIANT BROWSER OK: 30 shafts/long links, 29 hops, broken hole and MS/OLA mismatch, PDF 91-pipe restoration');
 }finally{await browser.close();}
})().catch(e=>{console.error(e);process.exitCode=1;});
