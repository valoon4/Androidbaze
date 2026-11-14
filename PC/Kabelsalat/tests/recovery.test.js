'use strict';
/* KAB-0786: view-reset may never erase a user's saved shaft JSON.
 * Real recovery.js in a minimal deterministic DOM/LocalStorage test harness. */
const assert=require('node:assert/strict');
const vm=require('node:vm');
const fs=require('node:fs'),path=require('node:path');
const source=fs.readFileSync(path.join(__dirname,'../recovery.js'),'utf8');
const mainKey='eugi.schachtwand.3d.v1';
function run(readyState='complete'){
 const data=new Map([[mainKey,'{"name":"Unveraendertes Projekt"}'],
 ['kabelsalat_sidebar_layout_v1','dock'],['kabelsalat_ui_mode_v1','expert'],
 ['kabelsalat_explore_progress_v1','finished'],['foreign_key','untouched']]);
 const buttons={},handlers={},classes=new Set(),calls=[];
 const elements={
  startupRecovery:{},recoveryReason:{textContent:''},
  reloadFresh:{addEventListener:(event,callback)=>{buttons.reloadFresh=callback}},
  resetDisplay:{addEventListener:(event,callback)=>{buttons.resetDisplay=callback}}
 };
 const dom={
  readyState,body:{classList:{contains:name=>classes.has(name),add:name=>classes.add(name)}},
  getElementById:id=>elements[id]||null,
  addEventListener:(event,fn)=>{handlers[event]=fn}
 };
 const loc={href:'https://example.org/editor/?foo=1',replace:url=>calls.push(url)};
 const g={
  document:dom,location:loc,URL,setTimeout:(fn,delay)=>{handlers.timed=fn;return 1},
  addEventListener:(event,fn)=>{handlers[event]=fn},
  localStorage:{removeItem:key=>data.delete(key),getItem:key=>data.get(key)||null}
 };
 vm.runInNewContext(source,g,{filename:'recovery.js',timeout:1500});
 return {data,buttons,handlers,classes,calls,elements};
}
const first=run();assert.equal(first.data.size,5);
assert.equal(typeof first.buttons.resetDisplay,'function');
first.buttons.resetDisplay();
assert.equal(first.data.has(mainKey),true,'View reset must preserve project JSON');
assert.equal(first.data.get(mainKey),'{"name":"Unveraendertes Projekt"}');
assert.equal(first.data.get('foreign_key'),'untouched');
for(const key of ['kabelsalat_sidebar_layout_v1','kabelsalat_ui_mode_v1','kabelsalat_explore_progress_v1'])
 assert.equal(first.data.has(key),false,'Only targeted layout keys may be removed: '+key);
assert.match(first.calls[0],/foo=1/);
assert.match(first.calls[0],/fresh=/);
const reload=run();reload.buttons.reloadFresh();
assert.equal(reload.data.size,5);
assert.equal(reload.data.has(mainKey),true);
const error=run();error.handlers.error({message:'test-renderer failed'});
assert.equal(error.classes.has('startup-failed'),true);
assert.match(error.elements.recoveryReason.textContent,/Startfehler/);
const ready=run();ready.classes.add('app-ready');
ready.handlers.error({message:'late'});
assert.equal(ready.classes.has('startup-failed'),false);
ready.handlers.timed();
assert.equal(ready.classes.has('startup-failed'),false);
const delayed=run('loading');
assert.equal(delayed.buttons.resetDisplay,undefined);
delayed.handlers.DOMContentLoaded();
assert.equal(typeof delayed.buttons.resetDisplay,'function');
delayed.handlers.timed();
assert.ok(delayed.classes.has('startup-failed'),'Startfail should not pretend app loaded');
console.log('RECOVERY OK: display keys only; backup JSON kept in all reset/reload/error paths');
