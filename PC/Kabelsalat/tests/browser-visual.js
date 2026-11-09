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
 const sample3D=()=>page.evaluate(()=>{
  const c=document.querySelector('#scene');
  const gl=c.getContext('webgl2')||c.getContext('webgl');
  if(!gl)return {uniqueColors:0,visible:false,width:c.width,height:c.height};
  const w=gl.drawingBufferWidth,h=gl.drawingBufferHeight,buf=new Uint8Array(w*h*4);
  gl.readPixels(0,0,w,h,gl.RGBA,gl.UNSIGNED_BYTE,buf);
  const colors=new Set();
  for(let y=0;y<h;y+=Math.max(1,Math.floor(h/28)))
   for(let x=0;x<w;x+=Math.max(1,Math.floor(w/28))){
    const n=(y*w+x)*4;
    colors.add(buf[n]+','+buf[n+1]+','+buf[n+2]);
   }
  return {uniqueColors:colors.size,visible:getComputedStyle(c).display!=='none',width:w,height:h};
 });
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
  const beforeOverview=await sample3D();
  assert.ok(beforeOverview.visible&&beforeOverview.uniqueColors>15,
   '3D canvas must contain actual colored walls and pipes, not just a blank rectangle: '+
   JSON.stringify(beforeOverview));
  // Real Chromium regression for the Fachanwender: no trapping in subviews.
  await page.locator('#questNetwork').click();
  await page.waitForFunction(()=>document.querySelector('#viewport').classList.contains('network-open'));
  await page.locator('#returnToShaft').click();
  assert.equal(await page.locator('#viewport').evaluate(el=>el.classList.contains('network-open')),false);
  assert.ok(await page.locator('#viewTabs [data-view="3D"]').evaluate(el=>el.classList.contains('active')));
  await page.waitForTimeout(200);
  const afterOverview=await sample3D();
  assert.ok(afterOverview.visible&&afterOverview.uniqueColors>15,
   'Returned 3D view must STILL draw pipes/walls, not only labels: '+JSON.stringify(afterOverview));
  await page.locator('#pdfOpen').click();
  await page.locator('#pdfGoShaft').click();
  assert.equal(await page.locator('#pdfVisualPanel').evaluate(el=>el.hidden),true);
  await page.locator('#toggleExpert').click();
  await page.locator('#runQuality').click();
  await page.locator('#qualityBackToShaft').click();
  assert.ok(await page.locator('#viewTabs [data-view="3D"]').evaluate(el=>el.classList.contains('active')));
  await page.locator('#toggleExpert').click(); // Back to read-only Basic.
  // Reproduce user report: both panes collapsed and PDF view on top.
  await page.locator('#sidebarToggleLeft').click();
  await page.locator('#sidebarToggleRight').click();
  assert.ok(await page.locator('body').evaluate(el=>
   el.classList.contains('sidebar-left-collapsed')&&
   el.classList.contains('sidebar-right-collapsed')));
  await page.locator('#pdfOpen').click();
  await page.locator('#enableEditing').click();
  assert.equal(await page.locator('#pdfVisualPanel').evaluate(el=>el.hidden),true);
  const editState=await page.evaluate(()=>({
   basic:document.body.classList.contains('explore-mode'),
   leftCollapsed:document.body.classList.contains('sidebar-left-collapsed'),
   rightCollapsed:document.body.classList.contains('sidebar-right-collapsed'),
   network:document.querySelector('#viewport').classList.contains('network-open')
  }));
  assert.deepEqual(editState,{basic:false,leftCollapsed:false,rightCollapsed:false,network:false},
   'The shortcut did not restore editable 3D controls');
  const oldName=await page.locator('#projectName').inputValue();
  assert.ok(oldName);
  await page.locator('#projectName').fill('Kabelsalat Test Editor');
  await page.locator('#projectName').dispatchEvent('change');
  assert.equal(await page.locator('#viewInfo').textContent(),
   'Kabelsalat Test Editor','Editor must apply real form changes to project');
  await page.locator('#undo').click();
  assert.equal(await page.locator('#projectName').inputValue(),oldName,
   'Undo must restore original project data');
  const afterEditing=await sample3D();
  assert.ok(afterEditing.visible&&afterEditing.uniqueColors>15,
   '3D rendering must still work after switching from Basic to real editing: '+JSON.stringify(afterEditing));
  await page.screenshot({path:path.join(out,'shaft-editable.png'),fullPage:true});
  await page.locator('#toggleExpert').click();
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
