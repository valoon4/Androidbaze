'use strict';
/* Optional real Chromium screenshot smoke. Uses playwright-core supplied by CI,
   no browser deps in production or in the browser-only GitHub Pages build. */
const assert=require('node:assert/strict'),fs=require('node:fs'),path=require('node:path');
(async()=>{
 const playwright=require(process.env.PLAYWRIGHT_CORE||'playwright-core');
 const executablePath=process.env.CHROME_BIN||'/usr/bin/google-chrome';
 const out=path.resolve(__dirname,'../dist/visual-qa');
 fs.mkdirSync(out,{recursive:true});
 const browser=await playwright.chromium.launch({
  executablePath,headless:true,
  args:['--no-sandbox','--disable-dev-shm-usage','--enable-unsafe-swiftshader',
   '--use-gl=angle','--use-angle=swiftshader']
 });
 const page=await browser.newPage({viewport:{width:1500,height:930},deviceScaleFactor:1});
 const errors=[];
 page.on('pageerror',e=>errors.push(e.message));
 await page.goto('file://'+path.resolve(__dirname,'../index.html'),{waitUntil:'load',timeout:40000});
 await page.waitForTimeout(3000);
 const ready=await page.evaluate(()=>document.body.classList.contains('app-ready'));
 if(ready){
  const data=await page.evaluate(()=>({
   canvasWidth:document.querySelector('#scene').width,
   canvasHeight:document.querySelector('#scene').height,
   version:document.querySelector('meta[name="kabelsalat-version"]').content,
   projectName:document.querySelector('#viewInfo')?.textContent,
   visualToggle:!!document.querySelector('#pdfOpen')
  }));
  assert.ok(data.canvasWidth>300&&data.canvasHeight>100,'Visible 3D canvas must be dimensioned');
  assert.ok(data.visualToggle);
  await page.screenshot({path:path.join(out,'shaft-3d.png'),fullPage:true});
  console.log('WebGL scene initialized:',JSON.stringify(data));
 }else{
  console.log('Software-WebGL unavailable in this headless runner: verifying 2D PDF fallback.');
  await page.waitForTimeout(7000);
 }
 if(await page.locator('#pdfVisualPanel').evaluate(e=>e.hidden)){
  await page.locator('#pdfOpen').click();
 }
 await page.waitForFunction(()=>document.querySelectorAll('#pdfVisualCanvas [data-pipe]').length===91);
 const walls=await page.locator('#pdfVisualCanvas .pdf-wall-card').count();
 const pipes=await page.locator('#pdfVisualCanvas [data-pipe]').count();
 assert.equal(walls,4);
 assert.equal(pipes,91);
 await page.screenshot({path:path.join(out,'all-four-pdf-walls.png'),fullPage:true});
 await page.locator('#pdfVisualPanel [data-pdf-wall="SW1"]').click();
 assert.equal(await page.locator('#pdfVisualCanvas [data-pipe]').count(),29);
 await page.screenshot({path:path.join(out,'sw1-pdf-reference.png'),fullPage:true});
 await browser.close();
 if(ready)assert.deepEqual(errors,[],'Browser runtime must not throw errors');
 console.log('VISUAL QA: 91 displayed PDF pipes, 4 walls and SW1 detail; WebGL initialized='+ready);
})().catch(e=>{console.error(e);process.exitCode=1;});
