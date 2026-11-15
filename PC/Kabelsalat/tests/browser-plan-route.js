'use strict';
/* Real Chromium integration of Bro's local PDF/text -> review -> 400-shaft CSV. */
const assert=require('node:assert/strict'),fs=require('node:fs'),path=require('node:path');
(async()=>{
 const playwright=require(process.env.PLAYWRIGHT_CORE||'playwright-core');
 const browser=await playwright.chromium.launch({
  executablePath:process.env.CHROME_BIN||'/usr/bin/google-chrome',headless:true,
  args:['--no-sandbox','--disable-dev-shm-usage','--enable-unsafe-swiftshader',
   '--use-gl=angle','--use-angle=swiftshader']});
 try{
  const page=await browser.newPage({viewport:{width:1440,height:950},acceptDownloads:true});
  const errors=[];page.on('pageerror',e=>errors.push(e.message));
  const out=path.resolve(__dirname,'../dist/visual-qa');fs.mkdirSync(out,{recursive:true});
  await page.goto('file://'+path.resolve(__dirname,'../index.html'),{waitUntil:'load',timeout:40000});
  await page.waitForFunction(()=>document.body.classList.contains('app-ready'),{timeout:30000});
  const before=await page.evaluate(()=>localStorage.getItem('eugi.schachtwand.3d.v1'));
  const csv='schacht_id;schachtname\n'+Array.from({length:400},(_,i)=>
   'S'+String(i+1).padStart(4,'0')+';Schacht '+(i+1)).join('\n')+'\n';
  await page.locator('#handoverShaftCsv').evaluate((el,text)=>{
   el.value=text;el.dispatchEvent(new Event('input',{bubbles:true}));
  },csv);
  await page.locator('#planRoutePaste').evaluate(el=>{
   el.value='Kabel K01: S0001 > S0002 > S0003\n'+
    'Kabel K02: S0002 > S0099\nKabel K02: S0003 > S0099\n'+
    'Kabel K03: S0002 > S0999';
   el.dispatchEvent(new Event('input',{bubbles:true}));
  });
  await page.locator('#planRouteAnalyze').evaluate(el=>el.click());
  await page.waitForFunction(()=>document.querySelector('#planRouteStatus').textContent.includes('3 Kandidaten'));
  assert.equal(await page.locator('#planRouteResults input[type="checkbox"]').count(),3);
  assert.match(await page.locator('#planRouteStatus').textContent(),/1 Kabel mit Alternativrouten/);
  assert.match(await page.locator('#planRouteIssues').textContent(),/Unbekannte/);
  // Pick only one K02 option. Never invent missing sequence between the alternatives.
  await page.locator('#planRouteResults input[type="checkbox"]').nth(0)
   .evaluate(el=>{el.checked=true;el.dispatchEvent(new Event('change',{bubbles:true}));});
  await page.locator('#planRouteResults input[type="checkbox"]').nth(1)
   .evaluate(el=>{el.checked=true;el.dispatchEvent(new Event('change',{bubbles:true}));});
  await page.locator('#planRouteApply').evaluate(el=>el.click());
  assert.match(await page.locator('#planRouteStatus').textContent(),/5 geprüfte Kandidatenzeilen/);
  const rows=await page.locator('#handoverPassCsv').inputValue();
  assert.match(rows,/K01/);assert.match(rows,/K02/);assert.doesNotMatch(rows,/K03/);
  await page.locator('#handoverAnalyze').evaluate(el=>el.click());
  assert.match(await page.locator('#handoverStatus').textContent(),/400 Schächte/);
  assert.match(await page.locator('#handoverStatus').textContent(),/5 Zuordnungen/);
  assert.equal(await page.locator('#handoverExportCable').isDisabled(),false);
  const downloadPromise=page.waitForEvent('download');
  await page.locator('#handoverExportCable').evaluate(el=>el.click());
  const download=await downloadPromise;
  assert.match(download.suggestedFilename(),/400-Schacht-cables\.csv/);
  assert.equal(await page.evaluate(()=>localStorage.getItem('eugi.schachtwand.3d.v1')),before,
   'Plan analysis and handover must not mutate 3D project');
  // Browser-uploaded digital PDF, not mock application state.
  const pdf='%PDF-1.4\n'+
   '1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n'+
   '2 0 obj\n<< /Type /Pages /Count 1 /Kids [3 0 R] >>\nendobj\n'+
   '3 0 obj\n<< /Type /Page /Parent 2 0 R /Contents 4 0 R >>\nendobj\n'+
   '4 0 obj\n<< /Length 45 >>\nstream\n'+
   'BT (Kabel K88: S0020 > S0021) Tj ET\nendstream\nendobj\n%%EOF';
  await page.locator('#planRoutePaste').evaluate(el=>{
   el.value='';el.dispatchEvent(new Event('input',{bubbles:true}));
  });
  await page.locator('#planRouteFiles').setInputFiles({
   name:'testplan.pdf',mimeType:'application/pdf',buffer:Buffer.from(pdf,'latin1')});
  await page.locator('#planRouteAnalyze').evaluate(el=>el.click());
  await page.waitForFunction(()=>document.querySelector('#planRouteStatus').textContent.includes('1 Kandidaten'));
  assert.match(await page.locator('#planRouteResults').textContent(),/testplan.pdf S. 1 Z. 1/);
  assert.match(await page.locator('#planRouteResults').textContent(),/K88/);
  assert.equal(await page.evaluate(()=>localStorage.getItem('eugi.schachtwand.3d.v1')),before);
  await page.screenshot({path:path.join(out,'bro-400-plan-pdf-reviewed-candidates.png'),fullPage:true});
  assert.deepEqual(errors,[],'Plan workflow must not produce JS page errors');
  console.log('BRO 400 PLAN BROWSER OK: 400 shafts, TXT+PDF upload, candidate review, conflicts, CSV, source protection');
 }finally{await browser.close();}
})().catch(err=>{console.error(err);process.exitCode=1;});
