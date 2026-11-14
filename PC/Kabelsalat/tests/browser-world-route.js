'use strict';
/* KAB-0375 extreme integration: real Chromium global XYZ model coordinates,
 * independent schematic segments, no fabricated intershaft cable joins,
 * elevation and PDF source-immutability. */
const assert=require('node:assert/strict');
const fs=require('node:fs'),path=require('node:path');
(async()=>{
 const pw=require(process.env.PLAYWRIGHT_CORE||'playwright-core');
 const browser=await pw.chromium.launch({executablePath:process.env.CHROME_BIN||'/usr/bin/google-chrome',
  headless:true,args:['--no-sandbox','--disable-dev-shm-usage','--enable-unsafe-swiftshader',
   '--use-gl=angle','--use-angle=swiftshader']});
 try{
  const page=await browser.newPage({viewport:{width:1500,height:950},acceptDownloads:true});
  const errors=[];page.on('pageerror',e=>errors.push(e.message));
  const out=path.resolve(__dirname,'../dist/visual-qa');fs.mkdirSync(out,{recursive:true});
  await page.goto('file://'+path.resolve(__dirname,'../index.html'),{waitUntil:'load',timeout:40000});
  await page.waitForFunction(()=>document.body.classList.contains('app-ready'),{timeout:30000});
  await page.evaluate(()=>{
   const project=window.KabelsalatSamples.create('station');
   project.name='FIKTIVE XYZ-SEGMENTE MIT OFFENEN ÜBERGÄNGEN';
   project.network.shafts=project.network.shafts.slice(0,3);
   const shafts=project.network.shafts;
   shafts.forEach((s,i)=>{
    s.x=i*10000;s.z=i*3000;
    s.snapshot.sourceInfo=null;
    s.snapshot.cables=[{
     id:'C'+i,name:'Fiktives lokales Kabel '+i,group:'OLA',diameter:12,
     a:{wall:'SW1',hole:s.snapshot.walls.find(w=>w.id==='SW1').holes[0].id},
     b:{wall:'SW3',hole:s.snapshot.walls.find(w=>w.id==='SW3').holes[0].id},
     route:[[0,900,0],[400,1200,200]]
    }];
   });
   project.network.links=[
    {id:'L12',from:shafts[0].id,to:shafts[1].id,note:'Schematischer Link'},
    {id:'L23',from:shafts[1].id,to:shafts[2].id,note:'Schematischer Link'}
   ];
   project.network.routes=[{id:'R-XYZ',name:'XYZ Test',group:'OLA',
    steps:shafts.map((s,i)=>({shaftId:s.id,cableId:'C'+i}))}];
   project.activeShaftId=shafts[0].id;
   localStorage.setItem('eugi.schachtwand.3d.v1',JSON.stringify(project));
   localStorage.setItem('kabelsalat_ui_mode_v1','expert');
  });
  await page.reload({waitUntil:'load'});
  await page.waitForFunction(()=>document.body.classList.contains('app-ready'),{timeout:30000});
  if(await page.locator('body').evaluate(x=>x.classList.contains('explore-mode')))
   await page.locator('#toggleExpert').click();
  assert.equal(await page.locator('#worldRouteAnalyze').isDisabled(),false);
  const original=await page.evaluate(()=>localStorage.getItem('eugi.schachtwand.3d.v1'));
  await page.locator('#netRouteSelect').selectOption('R-XYZ');
  await page.locator('#worldRouteAnalyze').click();
  await page.waitForFunction(()=>document.querySelector('#viewport').classList.contains('network-open'));
  const result=await page.locator('#worldRouteStatus').innerText();
  assert.match(result,/3 lokale Kabelsegmente/);
  assert.match(result,/2 unbestätigte Schachtübergänge/);
  assert.match(result,/Echte Kabellänge UNBEKANNT/);
  assert.equal(await page.locator('#worldRouteCsv').isDisabled(),false);
  assert.equal(await page.locator('#worldRouteElevation').isVisible(),true);
  assert.equal(await page.locator('#worldRouteList .trace-item').count(),5);
  assert.match(await page.locator('#worldRouteList').innerText(),/Unbestätigter physischer Übergang/);
  assert.equal(await page.evaluate(()=>localStorage.getItem('eugi.schachtwand.3d.v1')),original,
   'Global XYZ route analysis never mutates stored cable snapshots');
  await page.screenshot({path:path.join(out,'world-xyz-local-cable-segments-and-unknown-gaps.png'),fullPage:true});
  const downloadPromise=page.waitForEvent('download');
  await page.locator('#worldRouteCsv').click();
  const file=await downloadPromise;
  assert.match(file.suggestedFilename(),/xyz-lokale-segmente-ungeprueft\.csv$/);
  const dl=path.join(out,'world-route-xyz-export.csv');await file.saveAs(dl);
  const csv=fs.readFileSync(dl,'utf8');
  assert.ok(csv.startsWith('\uFEFF"Art"'));
  assert.equal((csv.match(/"unbekannter Übergang"/g)||[]).length,2);
  assert.ok(csv.includes('"10000";"900";"3000"'));
  assert.ok(!csv.includes('physisch freigegeben'));
  // Truncate one local endpoint. Result MUST be incomplete, never interpolate.
  await page.evaluate(()=>{
   const p=JSON.parse(localStorage.getItem('eugi.schachtwand.3d.v1'));
   p.network.shafts[1].snapshot.cables[0].b={wall:'SW3',hole:'BROKEN-ID'};
   localStorage.setItem('eugi.schachtwand.3d.v1',JSON.stringify(p));
  });
  await page.reload({waitUntil:'load'});
  await page.waitForFunction(()=>document.body.classList.contains('app-ready'),{timeout:30000});
  if(await page.locator('body').evaluate(x=>x.classList.contains('explore-mode')))
   await page.locator('#toggleExpert').click();
  await page.locator('#netRouteSelect').selectOption('R-XYZ');
  const brokenBefore=await page.evaluate(()=>localStorage.getItem('eugi.schachtwand.3d.v1'));
  await page.locator('#worldRouteAnalyze').click();
  assert.match(await page.locator('#worldRouteStatus').textContent(),/Datenfehler/);
  assert.match(await page.locator('#worldRouteList').textContent(),/unbekanntes Leerrohr/);
  assert.match(await page.locator('#worldRouteStatus').textContent(),/Echte Kabellänge UNBEKANNT/);
  assert.equal(await page.evaluate(()=>localStorage.getItem('eugi.schachtwand.3d.v1')),brokenBefore);
  await page.screenshot({path:path.join(out,'world-xyz-broken-cable-endpoint-do-not-connect.png'),fullPage:true});
  // Protected 91-pipe reference remains available after all synthetic tests.
  await page.evaluate(()=>{
   localStorage.setItem('eugi.schachtwand.3d.v1',
    JSON.stringify(window.KabelsalatSamples.create('pdf-u028')));
  });
  await page.reload({waitUntil:'load'});
  await page.waitForFunction(()=>document.body.classList.contains('app-ready'),{timeout:30000});
  const originalPdf=await page.evaluate(()=>JSON.parse(localStorage.getItem('eugi.schachtwand.3d.v1')));
  assert.deepEqual(originalPdf.walls.map(w=>w.holes.length),[29,13,26,23]);
  assert.equal(originalPdf.sourceInfo?.shaft,'13_U_028');
  assert.deepEqual(errors,[]);
  console.log('WORLD XYZ BROWSER OK: local 3D mm, explicit unknown transitions, CSV, broken endpoints and unchanged 91-pipe PDF source');
 }finally{await browser.close();}
})().catch(e=>{console.error(e);process.exitCode=1;});
