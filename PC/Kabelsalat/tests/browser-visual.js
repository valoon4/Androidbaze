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
  const shaftCaption=await page.locator('#exploreSceneText').innerText();
  assert.match(shaftCaption,/Lange Kabelwege sind hier ausgeblendet/,
   'Basic shaft step must explain that LR holes and long cables are different');
  await page.screenshot({path:path.join(out,'bro-lr-only-shaft-step.png'),fullPage:true});
  await page.locator('#questCable').click();
  assert.match(await page.locator('#exploreSceneText').innerText(),/KABEL, nicht LR/,
   'Cable step must identify long strands as cables, not Leerrohre');
  await page.screenshot({path:path.join(out,'bro-cable-routes-separate-step.png'),fullPage:true});
  await page.locator('#questShaft').click();
  const beforeOverview=await sample3D();
  assert.ok(await page.locator('#demoCompactionToggle').isVisible(),
   'PDF demo must offer a reversible, non-destructive source-vs-compact switch');
  const savedBefore=await page.evaluate(()=>localStorage.getItem('eugi.schachtwand.3d.v1'));
  await page.locator('#demoCompactionToggle').click();
  await page.locator('#demoCompactionToggle').click();
  const savedAfter=await page.evaluate(()=>localStorage.getItem('eugi.schachtwand.3d.v1'));
  assert.equal(savedAfter,savedBefore,'Demo compaction toggle may not alter project JSON');
  assert.ok(await page.locator('#exploreDimensionInfo').isVisible(),
   'Basic scene must disclose dimension provenance');
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
 // KAB-0884: deliberately synthetic mixed-bore fixture, NEVER treated as real shaft dimensions.
 // Screenshot is generated after all original reference checks to avoid interfering with 91-pipe validation.
 if(ready){
  const originalRaw=await page.evaluate(()=>{
   const key='eugi.schachtwand.3d.v1',raw=localStorage.getItem(key);
   if(!raw)return null;
   const project=JSON.parse(raw),holes=project.walls.find(w=>w.id==='SW1')?.holes||[];
   if(holes.length<3)return null;
   [60,110,116].forEach((diameter,i)=>{
    holes[i].dimensionSpec={nominalMm:diameter,openingMm:diameter,
     status:'candidate',method:'manual',source:'synthetischer KAB-0884-Test; nicht reale Rohrmaße'};
   });
   project.name='TEST: Betonblock mit Ø 60 / 110 / 116 (nur Testwerte)';
   localStorage.setItem(key,JSON.stringify(project));
   return raw;
  });
  assert.ok(originalRaw,'Original project missing when preparing mixed-bore test fixture');
  await page.reload({waitUntil:'load'});
  await page.waitForFunction(()=>document.body.classList.contains('app-ready'),{timeout:30000});
  const diameters=await page.evaluate(()=>JSON.parse(
   localStorage.getItem('eugi.schachtwand.3d.v1')).walls.find(w=>w.id==='SW1').holes
   .slice(0,3).map(h=>h.dimensionSpec.openingMm));
  assert.deepEqual(diameters,[60,110,116],'Synthetic bore sizes not kept through project reload');
  await page.screenshot({path:path.join(out,'synthetic-concrete-60-110-116.png'),fullPage:true});
  // Bro DN30–DN160 test: open the dedicated SYNTHETIC example in actual Chromium.
  // The PDF reference is restored immediately afterwards.
  await page.evaluate(()=>{
   const sample=window.KabelsalatSamples.create('diameters');
   localStorage.setItem('eugi.schachtwand.3d.v1',JSON.stringify(sample));
  });
  await page.reload({waitUntil:'load'});
  await page.waitForFunction(()=>document.body.classList.contains('app-ready'),{timeout:30000});
  const widths=await page.evaluate(()=>{
   const shaft=JSON.parse(localStorage.getItem('eugi.schachtwand.3d.v1'));
   return shaft.walls.flatMap(w=>w.holes.map(h=>h.dn));
  });
  for(const dn of [30,50,110,160])
   assert.ok(widths.includes(dn),'Bro synthetic LR demo must display DN '+dn);
  assert.equal(widths.length,94,'Synthetic demo must be independent of PDF 91-LR reference');
  // KAB-0888: real Chromium scene verification and 2D/3D evidence.
  const demo3D=await sample3D();
  assert.ok(demo3D.visible&&demo3D.uniqueColors>15,
   'DN comparison demo must render a real WebGL scene, not just JSON values');
  const demoSnapshot=await page.evaluate(()=>JSON.parse(localStorage.getItem('eugi.schachtwand.3d.v1')));
  assert.ok(demoSnapshot.name.startsWith('FIKTIVE DEMO'));
  assert.equal(demoSnapshot.sourceInfo,undefined,'Synthetic demo must never inherit measured source info');
  assert.deepEqual(demoSnapshot.walls.map(w=>w.holes.length),[24,28,24,18]);
  const allDn=demoSnapshot.walls.flatMap(w=>w.holes.map(h=>h.dn));
  assert.ok([30,50,110,160].every(dn=>allDn.includes(dn)));
  assert.ok(allDn.indexOf(50)!==allDn.indexOf(110));
  assert.equal(JSON.stringify(JSON.parse(JSON.stringify(demoSnapshot))),JSON.stringify(demoSnapshot),
   'DN demo must survive a strict JSON round-trip');
  await page.screenshot({path:path.join(out,'synthetic-lr-dn30-50-110-160.png'),fullPage:true});
  // Read-only Basic 2D view: one wall at a time, same ids and opening counts.
  await page.locator('#explore2DToggle').click();
  const twoD=await page.evaluate(()=>({
   visible:document.body.classList.contains('explore-with-2d'),
   draft:document.querySelector('#draft2d')?.getBoundingClientRect().width||0
  }));
  assert.ok(twoD.visible&&twoD.draft>100,'2D wall diagram should be visible beside DN 3D demo');
  await page.screenshot({path:path.join(out,'synthetic-lr-dn-2d-3d.png'),fullPage:true});
  await page.locator('#explore2DToggle').click();
  // Distinguish actual cable route stage from hollow LR-only stage.
  assert.match(await page.locator('#exploreSceneText').innerText(),/LR = runde Öffnungen/);
  await page.locator('#questCable').click();
  assert.match(await page.locator('#exploreSceneText').innerText(),/Linien zeigen schematische Kabelwege|KABEL, nicht LR/);
  await page.screenshot({path:path.join(out,'synthetic-dn-cable-stage.png'),fullPage:true});
  await page.locator('#questShaft').click();
  // Expert mode must keep DN editing accessible without editing imported PDF sources.
  await page.locator('#toggleExpert').click();
  assert.ok(!(await page.locator('body').evaluate(e=>e.classList.contains('explore-mode'))));
  assert.equal(await page.locator('#holeDn').getAttribute('step'),'1');
  assert.equal(await page.locator('#holeDn').getAttribute('min'),'30');
  assert.equal(await page.locator('#holeDn').getAttribute('max'),'160');
  await page.screenshot({path:path.join(out,'synthetic-dn-profi-editor.png'),fullPage:true});
  await page.locator('#toggleExpert').click();
  await page.evaluate(raw=>localStorage.setItem('eugi.schachtwand.3d.v1',raw),originalRaw);
  await page.reload({waitUntil:'load'});
  await page.waitForFunction(()=>document.body.classList.contains('app-ready'),{timeout:30000});
  const restored=await page.evaluate(()=>JSON.parse(localStorage.getItem('eugi.schachtwand.3d.v1')));
  assert.deepEqual(restored.walls.map(w=>w.holes.length),[29,13,26,23]);
  assert.equal(restored.sourceInfo?.shaft,'13_U_028');
  console.log('KAB-0888 BROWSER: synthetic DN 30/50/110/160 3D, 2D, cable-stage, expert edit and PDF source restoration verified.');
  console.log('KAB-0884 VISUAL: synthetic 60/110/116 mm bore screenshot captured; original source project restored.');
 }
 await browser.close();
 if(ready)assert.deepEqual(errors,[],'Browser runtime must not throw errors');
 console.log('VISUAL QA: 91 displayed PDF pipes, 4 walls and SW1 detail; WebGL initialized='+ready);
})().catch(e=>{console.error(e);process.exitCode=1;});
