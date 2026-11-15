'use strict';
/* KAB-0235..0250: actual Chromium 2D/3D editor integration on synthetic
 * shaft data only. No engineering or imported-source certification implied.
 */
const assert=require('node:assert/strict');
const path=require('node:path'),fs=require('node:fs');
(async()=>{
 const playwright=require(process.env.PLAYWRIGHT_CORE||'playwright-core');
 const browser=await playwright.chromium.launch({
  executablePath:process.env.CHROME_BIN||'/usr/bin/google-chrome',headless:true,
  args:['--no-sandbox','--disable-dev-shm-usage','--enable-unsafe-swiftshader',
   '--use-gl=angle','--use-angle=swiftshader']
 });
 try{
  const page=await browser.newPage({viewport:{width:1500,height:930},deviceScaleFactor:1});
  const errs=[];page.on('pageerror',e=>errs.push(e.message));
  page.on('dialog',d=>d.accept());
  const out=path.resolve(__dirname,'../dist/visual-qa');
  fs.mkdirSync(out,{recursive:true});
  await page.goto('file://'+path.resolve(__dirname,'../index.html'),
   {waitUntil:'load',timeout:40000});
  await page.waitForFunction(()=>document.body.classList.contains('app-ready'),{timeout:30000});
  await page.evaluate(()=>{
   const p=window.KabelsalatSamples.create('diameters');
   p.sourceInfo=null;p.name='Fiktiver 2D-Leerrohr-Editor QA';
   p.walls.forEach(w=>{w.holes=[];w.visible=true;});
   p.walls[0].holes.push({id:'BASE',row:0,col:0,u:.4,v:.4,dn:50});
   p.walls[1].holes.push({id:'OTHER',row:0,col:0,u:.7,v:.5,dn:75});
   p.cables=[{id:'C-SOURCE',name:'Verbindungs-Test',
    group:'Weitere',color:'#3388aa',diameter:12,route:[],
    a:{wall:'SW1',hole:'BASE'},b:{wall:'SW2',hole:'OTHER'}}];
   p.network.shafts[0].snapshot=window.KabelsalatNetwork.snapshot(p);
   localStorage.setItem('eugi.schachtwand.3d.v1',JSON.stringify(p));
   localStorage.setItem('kabelsalat_ui_mode_v1','expert');
  });
  await page.reload({waitUntil:'load'});
  await page.waitForFunction(()=>document.body.classList.contains('app-ready'),{timeout:30000});
  if(await page.locator('body').evaluate(el=>el.classList.contains('explore-mode')))
   await page.locator('#toggleExpert').click();
  const read=()=>page.evaluate(()=>JSON.parse(localStorage.getItem('eugi.schachtwand.3d.v1')));
  const baseline=await read();
  // KAB-0235/0237/0248: click-to-add with explicit DN and a 25-mm grid.
  await page.locator('#draftWallSelect').selectOption('SW4');
  await page.locator('#draftNewDn').fill('75');
  await page.locator('#draftSnapStep').selectOption('25');
  await page.locator('#draftAdd').click();
  const canvasPoint=async(wallId,xMm,yMm)=>page.evaluate(([wallId,xMm,yMm])=>{
   const p=JSON.parse(localStorage.getItem('eugi.schachtwand.3d.v1'));
   const canvas=document.querySelector('#draftCanvas'),rect=canvas.getBoundingClientRect();
   const pdf=p.sourceInfo?.sourceVersion?.startsWith('PDF V2');
   const on13=wallId==='SW1'||wallId==='SW3';
   const span=(on13?pdf:!pdf)?p.dims.w:p.dims.d;
   const m=window.Kabelsalat2D.metrics(span,p.dims.h,rect.width,rect.height);
   const pos=window.Kabelsalat2D.toPixels(m,xMm,yMm);
   return {x:rect.x+pos.x,y:rect.y+pos.y};
  },[wallId,xMm,yMm]);
  const addedAt=await canvasPoint('SW4',350,350);
  await page.mouse.click(addedAt.x,addedAt.y);
  const afterAdd=await read();
  const added=afterAdd.walls[3].holes;
  assert.equal(added.length,1,'2D pointer click must add one pipe on SW4');
  assert.equal(added[0].dn,75);
  assert.equal(Math.round(added[0].u*afterAdd.dims.w),350);
  assert.equal(Math.round(added[0].v*afterAdd.dims.h),350);
  assert.deepEqual(afterAdd.cables,baseline.cables);
  await page.locator('#draftWallSelect').selectOption('SW3');
  await page.locator('#groupStartX').fill('250');
  await page.locator('#groupStartY').fill('300');
  await page.locator('#groupRows').fill('2');
  await page.locator('#groupCols').fill('2');
  await page.locator('#groupDx').fill('200');
  await page.locator('#groupDy').fill('200');
  await page.locator('#groupDns').fill('30,110,50,160');
  await page.locator('#groupPreview').click();
  assert.match(await page.locator('#groupStatus').textContent(),/4 LR vorgeschlagen/);
  assert.equal(await page.locator('#groupApply').isDisabled(),false);
  assert.deepEqual(await read(),afterAdd,'Preview must not mutate the shaft model');
  await page.screenshot({path:path.join(out,'hole-editor-group-preview.png'),fullPage:true});
  await page.locator('#groupApply').click();
  const afterGroup=await read(),newHoles=afterGroup.walls.find(w=>w.id==='SW3').holes;
  assert.equal(newHoles.length,4);
  assert.deepEqual(newHoles.map(h=>h.dn),[30,110,50,160]);
  assert.equal(new Set(newHoles.map(h=>h.id)).size,4);
  assert.deepEqual(afterGroup.cables,baseline.cables);
  await page.screenshot({path:path.join(out,'hole-editor-mixed-dn-3d.png'),fullPage:true});
  // Exact-mm keyboard entry and separation of pipe-wall gauge from OD/DN.
  await page.locator('#holeU').fill('310');
  await page.locator('#holeV').fill('310');
  await page.locator('#holeDn').fill('63');
  await page.locator('#applyHole').click();
  let updated=await read(),first=updated.walls[2].holes.find(h=>h.id===newHoles[0].id);
  assert.equal(Math.round(first.u*updated.dims.d),310);
  assert.equal(Math.round(first.v*updated.dims.h),310);
  assert.equal(first.dn,63);
  await page.locator('#holeOuterMm').fill('75');
  await page.locator('#holeWallMm').fill('6');
  await page.locator('#holeDimensionSource').fill('Eigene synthetische Prüfangabe');
  await page.locator('#saveHoleDimensions').click();
  updated=await read();first=updated.walls[2].holes.find(h=>h.id===newHoles[0].id);
  assert.equal(first.dimensionSpec.outerMm,75);
  assert.equal(first.dimensionSpec.wallThicknessMm,6);
  assert.equal(first.dn,63);
  await page.locator('#draftCopy').click();
  const copied=await read(),copyWall=copied.walls.find(w=>w.id==='SW3').holes;
  assert.equal(copyWall.length,5);
  assert.equal(new Set(copyWall.map(h=>h.id)).size,5);
  assert.ok(copyWall.some(h=>h.id.includes('-copy-')));
  assert.deepEqual(copied.cables,baseline.cables);
  // The final copy cannot inherit a human measurement certification.
  const theCopy=copyWall.find(h=>h.id.includes('-copy-'));
  assert.equal(theCopy.dimensionSpec.status,'unverified');
  // KAB-0242/0243: real pointer drag, once with 25-mm grid and once at 1 mm.
  const dragFirst=async(toX,toY)=>{
   const p=await read(),h=p.walls[2].holes.find(h=>h.id===newHoles[0].id);
   const from=await canvasPoint('SW3',h.u*p.dims.d,h.v*p.dims.h);
   const to=await canvasPoint('SW3',toX,toY);
   await page.mouse.move(from.x,from.y);
   await page.mouse.down();
   await page.mouse.move(to.x,to.y,{steps:1});
   await page.mouse.up();
  };
  await dragFirst(800,900);
  let afterSnap=await read(),moved=afterSnap.walls[2].holes.find(h=>h.id===newHoles[0].id);
  assert.equal(Math.round(moved.u*afterSnap.dims.d),800);
  assert.equal(Math.round(moved.v*afterSnap.dims.h),900);
  await page.locator('#draftSnapStep').selectOption('1');
  await dragFirst(777,877);
  const afterFine=await read();
  moved=afterFine.walls[2].holes.find(h=>h.id===newHoles[0].id);
  assert.equal(Math.round(moved.u*afterFine.dims.d),777);
  assert.equal(Math.round(moved.v*afterFine.dims.h),877);
  assert.deepEqual(afterFine.cables,baseline.cables);
  await page.screenshot({path:path.join(out,'hole-editor-snap-on-off.png'),fullPage:true});
  // Transfer a connected bore on SW1 to free SW4, explicitly accepted dialogs.
  await page.locator('#draftWallSelect').selectOption('SW1');
  const coords=await page.evaluate(()=>{
   const p=JSON.parse(localStorage.getItem('eugi.schachtwand.3d.v1'));
   const canvas=document.querySelector('#draftCanvas'),rect=canvas.getBoundingClientRect();
   const m=window.Kabelsalat2D.metrics(p.dims.d,p.dims.h,rect.width,rect.height);
   const pos=window.Kabelsalat2D.toPixels(m,p.dims.d*.4,p.dims.h*.4);
   return {x:rect.x+pos.x,y:rect.y+pos.y};
  });
  await page.mouse.click(coords.x,coords.y);
  assert.equal(await page.locator('#holeDn').inputValue(),'50');
  await page.locator('#holeTransferWall').selectOption('SW4');
  await page.locator('#holeTransfer').click();
  const transferred=await read();
  assert.equal(transferred.walls[0].holes.some(h=>h.id==='BASE'),false);
  assert.equal(transferred.walls[3].holes.some(h=>h.id==='BASE'),true);
  assert.deepEqual(transferred.cables[0].a,{wall:'SW4',hole:'BASE'});
  assert.equal(transferred.cables[0].id,'C-SOURCE');
  await page.locator('#undo').click();
  const undone=await read();
  assert.equal(undone.walls[0].holes.some(h=>h.id==='BASE'),true);
  assert.deepEqual(undone.cables[0].a,{wall:'SW1',hole:'BASE'});
  await page.locator('#redo').click();
  assert.equal((await read()).walls[3].holes.some(h=>h.id==='BASE'),true);
  await page.screenshot({path:path.join(out,'hole-editor-transfer-undo-redo.png'),fullPage:true});
  // PDF reference: group and transfer cannot silently operate on source plan.
  await page.evaluate(()=>{
   const p=window.KabelsalatSamples.create('pdf-u028');
   localStorage.setItem('eugi.schachtwand.3d.v1',JSON.stringify(p));
  });
  await page.reload({waitUntil:'load'});
  await page.waitForFunction(()=>document.body.classList.contains('app-ready'),{timeout:30000});
  if(await page.locator('body').evaluate(el=>el.classList.contains('explore-mode')))
   await page.locator('#toggleExpert').click();
  const originalPdf=await read();
  await page.locator('#groupPreview').evaluate(el=>el.click());
  assert.match(await page.locator('#groupStatus').textContent(),/Referenzquelle geschützt/);
  assert.equal(await page.locator('#groupApply').isDisabled(),true);
  assert.deepEqual((await read()).walls.map(w=>w.holes.length),[29,13,26,23]);
  assert.deepEqual(await read(),originalPdf);
  assert.deepEqual(errs,[],'No browser runtime exceptions');
  console.log('KAB-0235..0250 BROWSER OK: batch DN, exact-mm editing, pipe gauge, copy, cable-safe transfer/undo/redo and PDF guard');
 }finally{await browser.close();}
})().catch(e=>{console.error(e);process.exitCode=1;});
