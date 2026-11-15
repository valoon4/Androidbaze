'use strict';
const assert=require('node:assert/strict'),photo=require('../photo-candidates.js');
const w=120,h=120,img=new Uint8ClampedArray(w*h*4);
for(let y=0;y<h;y++)for(let x=0;x<w;x++){
 const i=(y*w+x)*4,dark=Math.hypot(x-34,y-36)<12||Math.hypot(x-80,y-80)<19;
 img[i]=img[i+1]=img[i+2]=dark?20:150;img[i+3]=255;
}
const input=img.slice(),result=photo.detect(img,w,h);
assert.equal(result.candidates.length,2,'Two dark round opening candidates expected');
assert.ok(result.candidates[0].diameterPx>result.candidates[1].diameterPx);
assert.equal(result.candidates[0].relativeSize,'groß');
assert.equal(result.candidates[1].relativeSize,'mittel');
assert.ok(result.candidates.every(x=>!Object.hasOwn(x,'candidateMm')),
 'No absolute mm may be inferred without calibrated reference');
assert.deepEqual(img,input,'Image processing must never change original input pixels');
const sourced=photo.detect(img,w,h,{source:'synthetic-bro-fixture.png'});
assert.ok(sourced.candidates.every(c=>c.imageSource==='synthetic-bro-fixture.png'&&c.diameterUnit==='px'));
assert.deepEqual(photo.detect(img,w,h).candidates.map(c=>c.imageSource),[null,null]);
assert.throws(()=>photo.detect(img,w,h,{source:'bad\nsource'}),/Bildquelle/);
const calibrated=photo.calibrate(result.candidates,0,110);
assert.equal(calibrated[0].candidateMm,110);
assert.ok(calibrated[1].candidateMm<110);
assert.ok(calibrated.every(x=>x.status==='candidate'&&x.method==='photo-calibrated'));
const sourcedCalibration=photo.calibrate(sourced.candidates,0,110);
assert.ok(sourcedCalibration.every(x=>x.imageSource==='synthetic-bro-fixture.png'&&x.diameterUnit==='px'));
const fs=require('node:fs'),path=require('node:path');
const app=fs.readFileSync(path.resolve(__dirname,'../app.js'),'utf8');
const html=fs.readFileSync(path.resolve(__dirname,'../index.html'),'utf8');
assert.ok(html.includes('id="photoResetCalibration"'),'Calibration reset control must be visible');
assert.match(app,/photoUncalibrated=photoAnalysis\.candidates\.map/,
 'Keep a separate uncalibrated candidate snapshot');
assert.match(app,/photoAnalysis\.candidates=photoUncalibrated\.map/,
 'Reset must restore the original pixel candidates, not alter the model');
assert.match(app,/epoch!==photoImportEpoch/,
 'Old asynchronous image decoding must not overwrite a newer image');
const originalCandidates=sourced.candidates.map(candidate=>({...candidate}));
const afterCalibration=photo.calibrate(originalCandidates,0,110);
assert.ok(afterCalibration.some(candidate=>Number.isFinite(candidate.candidateMm)));
assert.ok(originalCandidates.every(candidate=>!Object.hasOwn(candidate,'candidateMm')),
 'Undo source remains uncalibrated despite subsequent calibration');
assert.throws(()=>photo.calibrate(result.candidates,0,0));
assert.throws(()=>photo.detect(img,10000,10000));
const blank=new Uint8ClampedArray(w*h*4);
for(let i=0;i<blank.length;i+=4){blank[i]=blank[i+1]=blank[i+2]=200;blank[i+3]=255;}
assert.equal(photo.detect(blank,w,h).candidates.length,0);
console.log('OK: photo hole proposals are relative unless explicit scale is supplied; no AI claims');
