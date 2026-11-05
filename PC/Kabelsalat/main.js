'use strict';
const {app,BrowserWindow,Menu,ipcMain,dialog}=require('electron');
const path=require('node:path'),fs=require('node:fs/promises');
async function saveReportPDF(_event,html,requestedName){
 if(typeof html!=='string'||html.length>3500000||!html.includes('<!-- Kabelsalat Report v1 -->')||
 /<script\b/i.test(html))return {ok:false,error:'Ungültiger Bericht oder unerlaubte aktive Inhalte'};
 const raw=String(requestedName||'Kabelsalat-Pruefbericht.pdf');
 const name=path.basename(raw).replace(/[^a-zA-Z0-9äöüÄÖÜß._-]/g,'_').slice(0,100);
 const defaultPath=name.toLowerCase().endsWith('.pdf')?name:'Kabelsalat-Pruefbericht.pdf';
 const parent=BrowserWindow.getFocusedWindow()||BrowserWindow.getAllWindows()[0];
 const chosen=await dialog.showSaveDialog(parent,{
  title:'Kabelsalat – Prüfbericht als PDF speichern',defaultPath,
  filters:[{name:'PDF-Dokument',extensions:['pdf']}],properties:['showOverwriteConfirmation']
 });
 if(chosen.canceled||!chosen.filePath)return {ok:false,cancelled:true};
 let hidden;
 try{
  hidden=new BrowserWindow({show:false,width:840,height:1100,webPreferences:{
   nodeIntegration:false,contextIsolation:true,sandbox:true,webSecurity:true,javascript:false}});
  await hidden.loadURL('data:text/html;charset=utf-8,'+encodeURIComponent(html));
  const buffer=await hidden.webContents.printToPDF({pageSize:'A4',printBackground:true,preferCSSPageSize:true});
  await fs.writeFile(chosen.filePath,buffer);
  return {ok:true};
 }catch(error){return {ok:false,error:String(error?.message||error).slice(0,240)};}
 finally{if(hidden&&!hidden.isDestroyed())hidden.destroy();}
}
function createWindow(){
 const win=new BrowserWindow({width:1540,height:900,minWidth:990,minHeight:625,backgroundColor:'#f1f4f6',
 title:'Kabelsalat',webPreferences:{nodeIntegration:false,contextIsolation:true,sandbox:true,webSecurity:true,
 preload:path.join(__dirname,'preload.js')}});
 Menu.setApplicationMenu(null);
 win.loadFile('index.html');
 win.webContents.setWindowOpenHandler(()=>({action:'deny'}));
}
app.whenReady().then(()=>{ipcMain.handle('kabelsalat:save-report-pdf',saveReportPDF);createWindow();app.on('activate',()=>{if(BrowserWindow.getAllWindows().length===0)createWindow();});});
app.on('window-all-closed',()=>{if(process.platform!=='darwin')app.quit();});
