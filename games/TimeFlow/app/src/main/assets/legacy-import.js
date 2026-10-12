/* TimeFlow: import of CSV exports from version 1.0/1.1, no dependencies. */
(function(root,factory){
 var api=factory();
 if(typeof module!=='undefined'&&module.exports)module.exports=api;
 root.TimeFlowImport=api;
})(typeof globalThis!=='undefined'?globalThis:this,function(){
 'use strict';
 function csvRows(raw){
   var data=String(raw||'').replace(/^\uFEFF/,'');
   var rows=[],row=[],field='',quoted=false;
   for(var i=0;i<data.length;i++){
     var c=data[i];
     if(quoted){
       if(c==='"'&&data[i+1]==='"'){field+='"';i++;}
       else if(c==='"')quoted=false;
       else field+=c;
     }else if(c==='"'&&field==='')quoted=true;
     else if(c===';'){row.push(field);field='';}
     else if(c==='\r'||c==='\n'){
       if(c==='\r'&&data[i+1]==='\n')i++;
       row.push(field);field='';
       if(row.some(function(v){return v.trim();}))rows.push(row);
       row=[];
     }else field+=c;
   }
   row.push(field);if(row.some(function(v){return v.trim();}))rows.push(row);
   return rows;
 }
 function parseDate(s){
   var match=String(s||'').trim().match(/^(\d{1,2})\.(\d{1,2})\.(\d{4}),?\s+(\d{1,2}):(\d{2})(?::(\d{2}))?$/);
   if(!match)return null;
   var d=+match[1],m=+match[2]-1,y=+match[3],h=+match[4],min=+match[5],sec=+(match[6]||0);
   var v=new Date(y,m,d,h,min,sec);
   if(v.getFullYear()!==y||v.getMonth()!==m||v.getDate()!==d||v.getHours()!==h||v.getMinutes()!==min)return null;
   return v.getTime();
 }
 function parse(raw){
   var rows=csvRows(raw),headers=(rows.shift()||[]).map(function(s){return s.trim();});
   var required=['Projekt','Aufgabe','Start','Ende','Arbeitszeit (Sek.)'];
   if(required.some(function(name){return !headers.includes(name)}))throw Error('Die Datei ist kein unterstützter TimeFlow-CSV-Export.');
   var idx=function(name){return headers.indexOf(name);},records=[],skipped=0;
   rows.forEach(function(columns){
     var get=function(name){var i=idx(name);return i<0?'':String(columns[i]||'').trim();};
     var project=get('Projekt'),task=get('Aufgabe'),start=parseDate(get('Start')),end=parseDate(get('Ende'));
     var sec=Number(get('Arbeitszeit (Sek.)'));
     if(!project||!task||start===null||end===null||end<=start||!Number.isSafeInteger(sec)||sec<0){skipped++;return;}
     var workMs=sec*1000;
     if(workMs>end-start)end=start+workMs;
     records.push({project:project,task:task,note:get('Session-Notiz'),start:start,end:end,workMs:workMs,pauseMs:end-start-workMs});
   });
   return {records:records,skipped:skipped};
 }
 return {csvRows:csvRows,parseDate:parseDate,parse:parse};
});
