'use strict';
/* Startup recovery independent of the 3D renderer; never clears project JSON. */
(()=>{
 const keys=['kabelsalat_sidebar_layout_v1','kabelsalat_ui_mode_v1','kabelsalat_explore_progress_v1'];
 let pending='';
 function fail(message){
  const panel=document.getElementById('startupRecovery');
  if(!panel){pending=message;return;}
  document.getElementById('recoveryReason').textContent=message;
  document.body.classList.add('startup-failed');
 }
 function fresh(){
  const url=new URL(location.href);
  url.searchParams.set('fresh',String(Date.now()));
  location.replace(url.toString());
 }
 function init(){
  document.getElementById('reloadFresh')?.addEventListener('click',fresh);
  document.getElementById('resetDisplay')?.addEventListener('click',()=>{
   try{keys.forEach(k=>localStorage.removeItem(k));}catch(_){}
   fresh();
  });
  if(pending)fail(pending);
  setTimeout(()=>{
   if(!document.body.classList.contains('app-ready'))
    fail('Der Editor konnte nicht vollständig starten. Frisch neu laden oder Anzeige zurücksetzen.');
  },7000);
 }
 addEventListener('error',e=>{
  if(!document.body?.classList.contains('app-ready'))
   fail('Startfehler: '+String(e.message||'Eine Datei konnte nicht geladen werden').slice(0,150));
 },true);
 addEventListener('unhandledrejection',e=>{
  if(!document.body?.classList.contains('app-ready'))
   fail('Programmfehler: '+String(e.reason?.message||'Unbekannter Fehler').slice(0,150));
 });
 if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',init,{once:true});
 else init();
})();