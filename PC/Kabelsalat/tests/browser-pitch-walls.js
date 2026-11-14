'use strict';
// KAB-0203…0206: real Chromium evidence for four independent PDF/62%-demo wall pairs.
const assert=require('node:assert/strict');
const fs=require('node:fs'),path=require('node:path');
(async()=>{
 const playwright=require(process.env.PLAYWRIGHT_CORE||'playwright-core');
 const browser=await playwright.chromium.launch({
  executablePath:process.env.CHROME_BIN||'/usr/bin/google-chrome',headless:true,
  args:['--no-sandbox','--disable-dev-shm-usage','--enable-unsafe-swiftshader',
   '--use-gl=angle','--use-angle=swiftshader']});
 try{
  const page=await browser.newPage({viewport:{width:1350,height:1020}});
  const errors=[];page.on('pageerror',e=>errors.push(e.message));
  const out=path.resolve(__dirname,'../dist/visual-qa');fs.mkdirSync(out,{recursive:true});
  await page.goto('file://'+path.resolve(__dirname,'../pitch.html'),{waitUntil:'load',timeout:40000});
  assert.equal(await page.locator('#wall-compare-link a[href="pitch-walls.html"]').count(),1);
  await page.locator('#wall-compare-link a[href="pitch-walls.html"]').click();
  await page.waitForURL(/pitch-walls\.html/,{timeout:15000});
  await page.waitForFunction(()=>document.querySelector('#compareStatus')?.textContent.includes('Vier Wandpaare geladen'),{timeout:25000});
  const status=await page.locator('#compareStatus').innerText();
  assert.match(status,/91 eindeutige Quell-IDs/);
  const expected={SW1:29,SW2:13,SW3:26,SW4:23};
  for(const [wall,count] of Object.entries(expected)){
   const section=page.locator('#wall-'+wall);
   assert.equal(await section.count(),1);
   assert.match(await section.locator('h2').textContent(),new RegExp(wall+'.*'+count));
   assert.equal(await section.locator('.panel').count(),2);
   assert.equal(await section.locator('.original svg circle').count(),count);
   assert.equal(await section.locator('.demo svg circle').count(),count);
   const values=await section.evaluate(root=>{
    const list=selector=>[...root.querySelectorAll(selector)].map(e=>({id:e.querySelector('title')?.textContent?.split(' · ')[0],
     x:Number(e.getAttribute('cx')),y:Number(e.getAttribute('cy'))}));
    return {original:list('.original svg circle'),demo:list('.demo svg circle')};
   });
   assert.deepEqual(values.demo.map(v=>v.id),values.original.map(v=>v.id),'Source ID order unchanged');
   assert.equal(new Set(values.original.map(v=>v.id)).size,count);
   assert.ok(values.original.some((v,i)=>v.x!==values.demo[i].x||v.y!==values.demo[i].y),
    wall+': the actual 62%-view must visibly differ from original');
   assert.ok(await section.locator('.original').isVisible());
   assert.ok(await section.locator('.demo').isVisible());
   await section.screenshot({path:path.join(out,'pitch-'+wall+'-pdf-vs-demo.png')});
  }
  const all=await page.locator('.original svg circle').count();
  assert.equal(all,91);
  assert.equal(await page.locator('.demo svg circle').count(),91);
  const noMutation=await page.evaluate(()=>{
   const before=localStorage.getItem('eugi.schachtwand.3d.v1');
   const p=window.KabelsalatReferences.create('pdf-u028');
   const snapshot=JSON.stringify(p);
   window.KabelsalatPitchWalls.compareAll(p,window.KabelsalatSchematicPositions);
   return {storedBefore:before,storedAfter:localStorage.getItem('eugi.schachtwand.3d.v1'),
    sourceUnchanged:JSON.stringify(p)===snapshot};
  });
  assert.equal(noMutation.storedAfter,noMutation.storedBefore,'Pitch comparison must never persist project data');
  assert.equal(noMutation.sourceUnchanged,true);
  assert.deepEqual(errors,[],'No JS runtime errors');
  console.log('PITCH CHROMIUM OK: 4 wall pairs, 91 distinct PDF IDs, 62%-view and four screenshots, no source edits');
 }finally{await browser.close();}
})().catch(err=>{console.error(err);process.exitCode=1;});
