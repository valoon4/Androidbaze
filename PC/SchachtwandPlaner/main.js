'use strict';
const {app,BrowserWindow,Menu}=require('electron');
function createWindow(){
 const win=new BrowserWindow({width:1540,height:900,minWidth:990,minHeight:625,backgroundColor:'#f1f4f6',
 title:'Schachtwand Planer',webPreferences:{nodeIntegration:false,contextIsolation:true,sandbox:true,webSecurity:true}});
 Menu.setApplicationMenu(null);
 win.loadFile('index.html');
 win.webContents.setWindowOpenHandler(()=>({action:'deny'}));
}
app.whenReady().then(()=>{createWindow();app.on('activate',()=>{if(BrowserWindow.getAllWindows().length===0)createWindow();});});
app.on('window-all-closed',()=>{if(process.platform!=='darwin')app.quit();});
