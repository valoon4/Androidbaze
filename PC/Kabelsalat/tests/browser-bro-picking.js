'use strict';
/* KAB-0877/M10, KAB-0886/M10, KAB-0888/M10.
 * Genuine Chromium pointer picking at computed screen-space centers of
 * NOMINAL / synthetic LR holes. No real-plan coordinates certified.
 */
const assert=require('node:assert/strict'),path=require('node:path');
(async()=>{
 const playwright=require(process.env.PLAYWRIGHT_CORE||'playwright-core');
 const browser=await playwright.chromium.launch({headless:true,
  executablePath:process.env.CHROME_BIN||'/usr/bin/google-chrome',
  args:['--no-sandbox','--disable-dev-shm-usage','--enable-unsafe-swiftshader',
   '--use-gl=angle','--use-angle=swiftshader']});
 try{
  const page=await browser.newPage({viewport:{width:1500,height:930}});
  page.setDefaultTimeout(10000);
  await page.goto('file://'+path.resolve(__dirname,'../index.html'));
  await page.waitForFunction(()=>document.body.classList.contains('app-ready'));
  await page.evaluate(()=>localStorage.setItem('eugi.schachtwand.3d.v1',
   JSON.stringify(window.KabelsalatSamples.create('diameters'))));
  await page.reload();
  await page.waitForFunction(()=>document.body.classList.contains('app-ready'));
  if(await page.locator('body').evaluate(el=>el.classList.contains('explore-mode')))
   await page.locator('#toggleExpert').click();
  const before=await page.evaluate(()=>localStorage.getItem('eugi.schachtwand.3d.v1'));
  const candidates=await page.evaluate(()=>{
   const THREE=window.THREE,model=JSON.parse(localStorage.getItem('eugi.schachtwand.3d.v1'));
   if(!THREE||!model.name.startsWith('FIKTIVE DEMO'))throw Error('Missing synthetic shaft or THREE');
   const canvas=document.querySelector('#scene'),rect=canvas.getBoundingClientRect();
   const d={w:model.dims.w/1000,d:model.dims.d/1000,h:model.dims.h/1000};
   const camera=new THREE.PerspectiveCamera(42,Math.max(.3,rect.width/rect.height),.015,200);
   const az=.72,el=.42,r=Math.max(d.w,d.d,d.h)*1.7;
   const target=new THREE.Vector3(0,d.h*.48,0);
   camera.position.set(r*Math.sin(az)*Math.cos(el),target.y+r*Math.sin(el),r*Math.cos(az)*Math.cos(el));
   camera.lookAt(target);camera.updateProjectionMatrix();camera.updateMatrixWorld();
   const hits=[];
   for(const w of model.walls)for(const h of w.holes){
    const u=h.u,y=d.h*h.v;
    const pos=w.id==='SW1'?new THREE.Vector3(-d.w/2+.024,y,-d.d/2+d.d*u):
     w.id==='SW2'?new THREE.Vector3(-d.w/2+d.w*u,y,-d.d/2+.024):
     w.id==='SW3'?new THREE.Vector3(d.w/2-.024,y,-d.d/2+d.d*u):
     new THREE.Vector3(-d.w/2+d.w*u,y,d.d/2-.024);
    const projected=pos.clone().project(camera);
    const x=(projected.x+1)*rect.width/2+rect.left;
    const yy=(1-projected.y)*rect.height/2+rect.top;
    if(Math.abs(projected.x)<.8&&Math.abs(projected.y)<.8&&projected.z>0&&projected.z<1)
     hits.push({x,y:yy,id:h.id,dn:h.dn,wall:w.id,
      uMm:Math.round(h.u*(w.id==='SW1'||w.id==='SW3'?model.dims.d:model.dims.w)),
      vMm:Math.round(h.v*model.dims.h),
      distance:pos.distanceTo(camera.position)});
   }
   return hits.map(h=>({...h,nearest:Math.min(...hits.filter(q=>q.id!==h.id)
    .map(q=>Math.hypot(h.x-q.x,h.y-q.y)))}))
    .sort((a,b)=>b.nearest-a.nearest||a.distance-b.distance).slice(0,20);
  });
  assert.ok(candidates.length>0,'Projected LR candidates must be visible on 3D canvas');
  let chosen=null,details=[];
  for(const c of candidates){
   await page.mouse.click(c.x,c.y);
   const picked=await page.evaluate(()=>({
    title:document.querySelector('#holeTitle')?.textContent||'',
    diameter:Number(document.querySelector('#holeDn')?.value),
    uMm:Number(document.querySelector('#holeU')?.value),
    vMm:Number(document.querySelector('#holeV')?.value),
    editorVisible:document.querySelector('#holeEditor')?.style.display==='block'}));
   details.push({candidate:c.id,picked:picked.title,nearest:c.nearest});
   if(picked.editorVisible&&picked.title.includes(c.wall)&&picked.diameter===c.dn&&
    picked.uMm===c.uMm&&picked.vMm===c.vMm){chosen=c;break;}
  }
  assert.ok(chosen,'3D raycast pointer must pick a synthetic nominal LR: '+JSON.stringify(details));
  assert.equal(await page.evaluate(()=>localStorage.getItem('eugi.schachtwand.3d.v1')),before,
   'Pure 3D selection must not change source data');
  console.log('BRO 3D PICK PASS:',chosen.wall,chosen.id,'DN',chosen.dn,'pixel clearance',chosen.nearest.toFixed(1));
 }finally{await browser.close();}
})().catch(e=>{console.error(e);process.exitCode=1});
