'use strict';
const {contextBridge,ipcRenderer}=require('electron');
contextBridge.exposeInMainWorld('kabelsalatDesktop',Object.freeze({
 saveReportPDF:(html,filename)=>ipcRenderer.invoke('kabelsalat:save-report-pdf',html,filename)
}));
