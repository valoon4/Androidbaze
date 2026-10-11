(function () {
'use strict';
var defaultState = {projects:[],tasks:[],sessions:[],theme:'dark'};
var state = defaultState;
var page = 'dashboard', currentProject = null, currentTask = null, searchText = '';
var weekOffset = 0;
var palette = ['#827bff','#32c5a4','#f4a960','#e77dbb','#57b6ea','#dc7d69','#b3a1f1'];
var statusProject = ['Aktiv','Pausiert','Abgeschlossen','Archiviert'];
var statusTask = ['Offen','In Bearbeitung','Erledigt'];

function load() {
 try {
   var raw = window.TimeFlowNative ? TimeFlowNative.load() : localStorage.getItem('timeflow-state');
   if (raw) {
     var value = JSON.parse(raw);
     if (value && Array.isArray(value.projects) && Array.isArray(value.tasks) && Array.isArray(value.sessions)) {
       state = Object.assign({},defaultState,value);
     }
   }
 } catch(e) { console.error('Load',e); }
 document.body.className = state.theme==='light'?'light':'dark';
}
function persist() {
 try {
   var serialized=JSON.stringify(state);
   if (window.TimeFlowNative) TimeFlowNative.save(serialized);
   else localStorage.setItem('timeflow-state',serialized);
 } catch(e) { alert('Speichern fehlgeschlagen: '+e.message); }
}
function uid(){return 't'+Date.now().toString(36)+Math.random().toString(36).slice(2,9);}
function esc(s){return String(s==null?'':s).replace(/[&<>"']/g,function(c){return {'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c];});}
function fmt(ms){var s=Math.floor(Math.max(0,ms||0)/1000);return String(Math.floor(s/3600)).padStart(2,'0')+':'+String(Math.floor(s%3600/60)).padStart(2,'0')+':'+String(s%60).padStart(2,'0');}
function short(ms){var m=Math.floor(Math.max(0,ms||0)/60000);return m>=60? Math.floor(m/60)+'h '+(m%60)+'m':m+'m';}
function date(ts){return new Date(ts).toLocaleDateString('de-DE');}
function dateTime(ts){return new Date(ts).toLocaleString('de-DE',{day:'2-digit',month:'2-digit',year:'numeric',hour:'2-digit',minute:'2-digit'});}
function localDate(ts){var d=new Date(ts);return d.getFullYear()+'-'+String(d.getMonth()+1).padStart(2,'0')+'-'+String(d.getDate()).padStart(2,'0');}
function inputDateTime(ts){var d=new Date(ts);return localDate(ts)+'T'+String(d.getHours()).padStart(2,'0')+':'+String(d.getMinutes()).padStart(2,'0');}
function millis(v){return new Date(v).getTime();}
function pg(id){return state.projects.find(function(p){return p.id===id;});}
function task(id){return state.tasks.find(function(t){return t.id===id;});}
function sessionsFor(id){return state.sessions.filter(function(s){return s.taskId===id;});}
function active(){return state.sessions.find(function(s){return s.status==='running'||s.status==='paused';});}
function duration(s,now){
 if(typeof s.workMs==='number')return Math.max(0,s.workMs);
 return (s.segments||[]).reduce(function(total,part){return total+Math.max(0,(part.end||now||Date.now())-part.start);},0);
}
function taskMs(id){var now=Date.now();return sessionsFor(id).reduce(function(sum,s){return sum+duration(s,now);},0);}
function projectMs(id){return state.tasks.filter(function(t){return t.projectId===id;}).reduce(function(sum,t){return sum+taskMs(t.id);},0);}
function nTasks(id){return state.tasks.filter(function(t){return t.projectId===id;});}
function startOfWeek(ts){var d=new Date(ts||Date.now());d.setHours(0,0,0,0);d.setDate(d.getDate()-(d.getDay()+6)%7);return d.getTime();}
function dayMs(){
 var totals={};
 function add(key,ms){totals[key]=(totals[key]||0)+ms;}
 var now=Date.now();
 state.sessions.forEach(function(s){
   if(typeof s.workMs==='number') { add(localDate(s.start),s.workMs); return; }
   (s.segments||[]).forEach(function(part){
     var a=part.start,b=part.end||now;
     while(a<b){
       var midnight=new Date(a);midnight.setHours(24,0,0,0);
       var end=Math.min(b,midnight.getTime());
       if(end<=a)break;
       add(localDate(a),end-a);a=end;
     }
   });
 });
 return totals;
}
function totalDateRange(from,to) {
 var daily=dayMs(),sum=0;
 Object.keys(daily).forEach(function(key){if(key>=from&&key<=to)sum+=daily[key];});
 return sum;
}
function toast(msg){
 var el=document.getElementById('toast');el.textContent=msg;el.classList.remove('hidden');
 clearTimeout(window.toastTimeout);window.toastTimeout=setTimeout(function(){el.classList.add('hidden');},2700);
}
function render(){
 document.body.className=state.theme==='light'?'light':'dark';
 var body='';
 if(page==='dashboard')body=dashboard();
 if(page==='projects')body=projectsView();
 if(page==='project')body=projectView();
 if(page==='task')body=taskView();
 if(page==='analytics')body=analytics();
 if(page==='timer')body=timerView();
 if(page==='settings')body=settingsView();
 var navItems=[['dashboard','◫','Übersicht'],['projects','▦','Projekte'],['timer','◷','Zeiterfassung'],['analytics','▥','Auswertungen'],['settings','⚙','Einstellungen']];
 function nav(){return navItems.map(function(n){var on=(page===n[0]|| (n[0]==='projects'&&(page==='project'||page==='task')));return '<button class="'+(on?'active':'')+'" onclick="go(\''+n[0]+'\')"><span class="nav-icon">'+n[1]+'</span>'+n[2]+'</button>';}).join('');}
 function mobileNav(){return navItems.slice(0,4).map(function(n){var on=(page===n[0]||(n[0]==='projects'&&(page==='project'||page==='task')));return '<button class="'+(on?'active':'')+'" onclick="go(\''+n[0]+'\')"><span class="nav-icon">'+n[1]+'</span>'+n[2]+'</button>';}).join('');}
 var titles={dashboard:'Dein Überblick',projects:'Projekte',project:pg(currentProject)?pg(currentProject).name:'Projekt',task:task(currentTask)?task(currentTask).name:'Aufgabe',timer:'Zeiterfassung',analytics:'Auswertungen',settings:'Einstellungen'};
 var subtitles={dashboard:'DASHBOARD',projects:'WORKSPACE',project:'PROJEKT',task:'AUFGABE',timer:'TIMER',analytics:'INSIGHTS',settings:'PRÄFERENZEN'};
 var top='<header class="topbar"><div><div class="eyebrow">'+esc(subtitles[page])+'</div><h1>'+esc(titles[page])+'</h1></div><div class="top-actions"><input class="search" id="q" placeholder="Suchen …" aria-label="Suche" value="'+esc(searchText)+'" oninput="doSearch(this.value)"><button class="btn icon" onclick="toggleTheme()" title="Design ändern">'+(state.theme==='dark'?'☀':'☾')+'</button><button class="btn primary" onclick="modalProject()">+ Projekt</button></div></header>';
 var side='<aside class="sidebar"><div class="brand"><span class="logo">↗</span>TimeFlow</div><nav class="nav">'+nav()+'</nav><div class="side-meta">Deine Projekte</div><div class="side-projects">'+state.projects.filter(function(p){return p.status!=='Archiviert';}).slice(0,12).map(function(p){return '<button class="side-project" onclick="openProject(\''+p.id+'\')"><i class="dot" style="--dot:'+p.color+'"></i>'+esc(p.name)+'</button>';}).join('')+'</div><div class="side-bottom">● Offline & sicher auf deinem Gerät<br>TimeFlow 1.0</div></aside>';
 document.getElementById('app').innerHTML='<div class="shell">'+side+'<main class="main">'+top+body+'</main><nav class="mobile-nav">'+mobileNav()+'</nav>'+timerBar()+'</div>';
}
function section(label,action){return '<div class="section-head"><h2>'+label+'</h2>'+(action||'')+'</div>';}
function empty(icon,title,desc,btn){return '<div class="empty"><div class="large">'+icon+'</div><strong>'+title+'</strong><div>'+desc+'</div>'+(btn?'<div style="margin-top:18px">'+btn+'</div>':'')+'</div>';}
function stat(name,number,foot){return '<div class="card"><div class="stat-label">'+name+'</div><div class="stat-value">'+number+'</div><div class="stat-foot">'+foot+'</div></div>';}
function dashboard(){
 var now=new Date(), key=localDate(now), week=localDate(startOfWeek(now.getTime())), month=key.slice(0,7)+'-01';
 var stats='<div class="grid stats">'+stat('Heute',fmt(totalDateRange(key,key)),'Erfasste Arbeitszeit')+stat('Diese Woche',fmt(totalDateRange(week,key)),'Seit Montag')+stat('Dieser Monat',fmt(totalDateRange(month,key)),'Laufender Monat')+stat('Aktive Projekte',state.projects.filter(function(p){return p.status==='Aktiv';}).length,'Projekte im Fokus')+'</div>';
 var live=active();var box=live?'<div class="livebox" style="margin-top:17px"><div><div class="eyebrow">'+(live.status==='running'?'● LIVE':'Ⅱ PAUSIERT')+'</div><h2>'+esc(task(live.taskId)?task(live.taskId).name:'Aufgabe')+'</h2><p class="muted tiny">'+esc(pg(task(live.taskId)?task(live.taskId).projectId:'')?.name||'')+'</p></div><div class="row"><div class="time" data-live-time>'+fmt(duration(live,Date.now()))+'</div><button class="btn primary" onclick="go(\'timer\')">Timer öffnen ↗</button></div></div>':'';
 var last=state.sessions.slice().sort(function(a,b){return b.start-a.start;}).slice(0,5);
 var proj=state.projects.filter(function(p){return p.status!=='Archiviert';}).slice(0,3);
 return stats+live+'<div class="split"><div>'+section('Deine Projekte','<button class="btn small" onclick="go(\'projects\')">Alle anzeigen →</button>')+(proj.length?'<div class="grid project-grid" style="grid-template-columns:repeat(auto-fit,minmax(210px,1fr))">'+proj.map(projectCard).join('')+'</div>':empty('◈','Starte dein erstes Projekt','Erstelle ein Projekt und erfasse deine erste Arbeitseinheit.','<button class="btn primary" onclick="modalProject()">+ Projekt erstellen</button>'))+'</div><div>'+section('Diese Woche')+'<div class="card">'+weekBars(0)+'</div></div></div>'+section('Letzte Sessions')+(last.length?'<div class="card list">'+last.map(sessionRow).join('')+'</div>':empty('◷','Noch keine Sessions','Deine Arbeitszeiten erscheinen hier.',''));
}
function projectCard(p){
 var tasks=nTasks(p.id),done=tasks.filter(function(t){return t.status==='Erledigt';}).length;
 return '<button class="project" onclick="openProject(\''+p.id+'\')"><div class="project-top"><i class="dot" style="--dot:'+p.color+';width:15px;height:15px"></i><span class="tag">'+esc(p.status)+'</span></div><div class="project-name">'+esc(p.name)+'</div><div class="project-desc">'+esc(p.description||'Organisiere Aufgaben und erfasse deine Zeit.')+'</div><div class="progress"><span style="width:'+(tasks.length?Math.round(done/tasks.length*100):0)+'%"></span></div><div class="project-bottom"><span>'+tasks.length+' Aufgaben</span><b class="time">'+fmt(projectMs(p.id))+'</b></div></button>';
}
function projectsView(){
 var list=state.projects.filter(function(p){return matches(p.name+' '+p.description);});
 return '<div class="details"><p class="muted">Alle Projekte mit ihren Aufgaben und Arbeitszeiten an einem Ort.</p><button class="btn primary" onclick="modalProject()">+ Neues Projekt</button></div>'+section('Alle Projekte · '+list.length)+(list.length?'<div class="grid project-grid">'+list.map(projectCard).join('')+'</div>':empty('◈','Keine Projekte gefunden','Erstelle ein neues Projekt oder passe deine Suche an.','<button class="btn primary" onclick="modalProject()">+ Projekt</button>'));
}
function matches(s){return !searchText||String(s).toLowerCase().includes(searchText.toLowerCase());}
function projectView(){
 var p=pg(currentProject);if(!p)return projectsView();
 var arr=nTasks(p.id).filter(function(t){return matches(t.name+' '+t.description);});
 var completed=nTasks(p.id).filter(function(t){return t.status==='Erledigt';}).length;
 return '<div class="breadcrumbs"><button onclick="go(\'projects\')">Projekte</button>› '+esc(p.name)+'</div><div class="details"><div><div class="row"><i class="dot" style="--dot:'+p.color+';width:16px;height:16px"></i><span class="tag">'+esc(p.status)+'</span></div><p class="muted">'+esc(p.description||'Noch keine Projektbeschreibung.')+'</p></div><div class="actions"><button class="btn" onclick="modalProject(\''+p.id+'\')">✎ Bearbeiten</button><button class="btn danger" onclick="removeProject(\''+p.id+'\')">Löschen</button></div></div><div class="grid kpi-grid">'+stat('Gesamtzeit',fmt(projectMs(p.id)),'Alle Sessions')+stat('Aufgaben',nTasks(p.id).length,'In diesem Projekt')+stat('Fortschritt',nTasks(p.id).length?Math.round(completed/nTasks(p.id).length*100)+'%':'0%','Erledigte Aufgaben')+'</div>'+section('Aufgaben','<button class="btn primary" onclick="modalTask()">+ Aufgabe</button>')+(arr.length?'<div class="card list">'+arr.map(taskRow).join('')+'</div>':empty('◎','Noch keine Aufgaben','Lege eine Aufgabe in diesem Projekt an.','<button class="btn primary" onclick="modalTask()">+ Erste Aufgabe</button>'));
}
function taskRow(t){
 var a=active(),on=a&&a.taskId===t.id;
 return '<div class="list-row"><div class="circle">◷</div><div class="row-main clickable" onclick="openTask(\''+t.id+'\')"><div class="row-title">'+esc(t.name)+'</div><div class="row-sub">'+esc(t.status)+' · '+sessionsFor(t.id).length+' Sessions</div></div><div class="row-right"><span class="time">'+fmt(taskMs(t.id))+'</span>'+(on?'<button class="btn small" onclick="openTask(\''+t.id+'\')">Live</button>':'<button class="btn small primary" onclick="startTimer(\''+t.id+'\')">▶</button>')+'<button class="btn small" onclick="modalTask(\''+t.id+'\')">✎</button></div></div>';
}
function taskView(){
 var t=task(currentTask);if(!t)return projectsView();
 var p=pg(t.projectId),all=sessionsFor(t.id).sort(function(a,b){return b.start-a.start;});
 var live=active();var on=live&&live.taskId===t.id;
 var hero='<div class="livebox" style="margin:21px 0"><div><div class="eyebrow">ERFASSTE ARBEITSZEIT</div><div class="time" data-task-time>'+fmt(taskMs(t.id))+'</div><div class="tiny muted">'+all.length+' Sessions · '+esc(t.status)+'</div></div><div class="actions">'+(on?'<button class="btn primary" onclick="togglePause()">'+(live.status==='running'?'Ⅱ Pausieren':'▶ Fortsetzen')+'</button><button class="btn" onclick="stopTimer()">■ Stoppen</button>':'<button class="btn primary" onclick="startTimer(\''+t.id+'\')">▶ Session starten</button>')+'</div></div>';
 return '<div class="breadcrumbs"><button onclick="go(\'projects\')">Projekte</button>› <button onclick="openProject(\''+t.projectId+'\')">'+esc(p?p.name:'Projekt')+'</button>› '+esc(t.name)+'</div><div class="details"><p class="muted">'+esc(t.description||'Noch keine Aufgabenbeschreibung.')+'</p><div class="actions"><button class="btn" onclick="modalTask(\''+t.id+'\')">✎ Bearbeiten</button><button class="btn danger" onclick="removeTask(\''+t.id+'\')">Löschen</button></div></div>'+hero+section('Sessions','<button class="btn" onclick="modalSession()">+ Manuell erfassen</button>')+(all.length?'<div class="card list">'+all.map(sessionRow).join('')+'</div>':empty('◷','Noch keine Sessions','Starte den Timer oder trage eine Arbeitszeit nach.','<button class="btn primary" onclick="startTimer(\''+t.id+'\')">▶ Timer starten</button>'));
}
function sessionRow(s){
 var t=task(s.taskId),p=t?pg(t.projectId):null;
 return '<div class="list-row"><div class="circle">'+(s.status==='running'?'●':s.status==='paused'?'Ⅱ':'✓')+'</div><div class="row-main"><div class="row-title">'+esc(s.note|| (t?t.name:'Session'))+'</div><div class="row-sub">'+dateTime(s.start)+' · '+esc(p?p.name:'')+' · '+esc(t?t.name:'')+(s.status==='running'?' · Läuft':s.status==='paused'?' · Pausiert':'')+'</div></div><div class="row-right"><span class="time">'+fmt(duration(s,Date.now()))+'</span>'+(s.status==='done'?'<button class="btn small" onclick="modalSession(\''+s.id+'\')">✎</button>':'')+'<button class="btn small danger" onclick="removeSession(\''+s.id+'\')">×</button></div></div>';
}
function timerView(){
 var s=active(),t=s?task(s.taskId):null;
 var available=state.tasks.filter(function(t){return t.status!=='Erledigt'&&pg(t.projectId)&&pg(t.projectId).status!=='Archiviert'&&matches(t.name+' '+(pg(t.projectId)?.name||''));});
 var now=Date.now();
 var hero=s?'<div class="livebox"><div><div class="eyebrow">'+(s.status==='running'?'● TIMER LÄUFT':'Ⅱ PAUSIERT')+'</div><h2>'+esc(t?t.name:'Aufgabe')+'</h2><p class="muted">'+esc(t&&pg(t.projectId)?pg(t.projectId).name:'')+'</p></div><div><div class="time" data-live-time>'+fmt(duration(s,now))+'</div><div class="actions" style="margin-top:12px"><button class="btn primary" onclick="togglePause()">'+(s.status==='running'?'Ⅱ Pausieren':'▶ Fortsetzen')+'</button><button class="btn" onclick="stopTimer()">■ Stoppen</button></div></div></div>':empty('⏱','Bereit, wenn du es bist','Wähle eine Aufgabe aus der Liste aus, um die Zeit zu erfassen.','');
 return hero+section('Aufgaben starten','<button class="btn" onclick="go(\'projects\')">Projekte öffnen →</button>')+(available.length?'<div class="card list">'+available.map(taskRow).join('')+'</div>':empty('◎','Keine Aufgaben vorhanden','Lege zuerst ein Projekt und eine Aufgabe an.','<button class="btn primary" onclick="modalProject()">+ Projekt</button>'));
}
function timerBar(){
 var a=active();if(!a)return '';
 var t=task(a.taskId),p=t?pg(t.projectId):null;
 return '<div class="session-timer"><div><div class="eyebrow">'+(a.status==='running'?'● AUFZEICHNUNG LÄUFT':'Ⅱ TIMER PAUSIERT')+'</div><div style="font-size:13px;font-weight:700">'+esc(t?t.name:'Session')+' <span class="muted tiny">/ '+esc(p?p.name:'')+'</span></div></div><div class="row"><span class="time" data-live-time>'+fmt(duration(a,Date.now()))+'</span><button class="btn small primary" onclick="togglePause()">'+(a.status==='running'?'Ⅱ':'▶')+'</button><button class="btn small" onclick="stopTimer()">■</button></div></div>';
}
function weekBars(offset){
 var start=startOfWeek(Date.now());start+=offset*7*86400000;
 var daily=dayMs(),names=['Mo','Di','Mi','Do','Fr','Sa','So'],values=[];
 for(var i=0;i<7;i++){var d=new Date(start);d.setDate(d.getDate()+i);values.push(daily[localDate(d)]||0);}
 var max=Math.max(1,...values);
 return '<div class="bargraph">'+values.map(function(n,i){return '<div class="bar-col"><div class="bar" title="'+short(n)+'" style="height:'+Math.max(2,n/max*142)+'px"></div><span class="bar-label">'+names[i]+'</span></div>';}).join('')+'</div><div class="muted tiny" style="margin-top:8px;text-align:center">'+date(start)+' – '+date(start+6*86400000)+'</div>';
}
function analytics(){
 var now=Date.now(),dates=dayMs();
 var start=new Date(now);start.setDate(start.getDate()-29);
 var from=document.getElementById('filterFrom')?.value||window.analyticsFrom||localDate(start);
 var to=document.getElementById('filterTo')?.value||window.analyticsTo||localDate(now);
 var totals=state.projects.map(function(p){var count=state.tasks.filter(function(t){return t.projectId===p.id;}).reduce(function(sum,t){return sum+sessionsFor(t.id).filter(function(s){return localDate(s.start)>=from&&localDate(s.start)<=to;}).reduce(function(a,s){return a+duration(s,now);},0);},0);return {p:p,ms:count};}).sort(function(a,b){return b.ms-a.ms;});
 var total=totals.reduce(function(s,a){return s+a.ms;},0);
 return '<div class="details"><p class="muted">Verstehe, wofür du deine Arbeitszeit verwendest.</p><button class="btn primary" onclick="exportCsv()">↓ CSV exportieren</button></div><div class="filter-group" style="margin:18px 0"><div class="field" style="margin:0"><label>Von</label><input type="date" id="filterFrom" value="'+from+'" onchange="setFilter()"></div><div class="field" style="margin:0"><label>Bis</label><input type="date" id="filterTo" value="'+to+'" onchange="setFilter()"></div></div><div class="grid kpi-grid">'+stat('Zeitraum',fmt(totalDateRange(from,to)),'Arbeitszeit nach Kalendertag')+stat('Sessions',state.sessions.filter(function(s){return localDate(s.start)>=from&&localDate(s.start)<=to;}).length,'Im ausgewählten Zeitraum')+stat('Projekte',totals.filter(function(v){return v.ms>0;}).length,'Mit erfasster Zeit')+'</div>'+section('Projektverteilung')+(total?'<div class="card"><div class="pie-line">'+totals.filter(function(x){return x.ms>0;}).map(function(x){return '<span style="width:'+(x.ms/total*100)+'%;background:'+x.p.color+'"></span>';}).join('')+'</div>'+totals.filter(function(x){return x.ms>0;}).map(function(x){return '<div class="list-row"><i class="dot" style="--dot:'+x.p.color+'"></i><div class="row-main row-title">'+esc(x.p.name)+'</div><div class="time">'+fmt(x.ms)+'</div><span class="tag">'+Math.round(x.ms/total*100)+'%</span></div>';}).join('')+'</div>':empty('▥','Noch keine Auswertung','Im gewählten Zeitraum wurden keine Sessions erfasst.',''))+section('Letzte sieben Tage')+'<div class="card">'+weekBars(0)+'</div>';
}
function settingsView(){
 return '<div class="card"><h2>Darstellung</h2><p class="muted">Wechsle zwischen hellem und dunklem Design.</p><button class="btn" onclick="toggleTheme()">'+(state.theme==='dark'?'☀ Hellen Modus aktivieren':'☾ Dunklen Modus aktivieren')+'</button></div><div class="card" style="margin-top:16px"><h2>Deine Daten</h2><p class="muted">TimeFlow funktioniert vollständig offline. Deine Projekte, Aufgaben und Sessions werden lokal auf deinem Gerät gespeichert.</p><div class="actions"><button class="btn primary" onclick="exportCsv()">↓ Sessions als CSV exportieren</button></div><p class="tiny muted">Tipp: Exportiere deine Daten regelmäßig. Beim Löschen der App-Daten gehen die lokalen Einträge verloren.</p></div><p class="muted tiny" style="margin-top:25px">TimeFlow v1.0-debug · Androidbaze · © 2026</p>';
}
function closeModal(){document.getElementById('overlay').classList.add('hidden');document.getElementById('overlay').innerHTML='';}
function showDialog(title,fields,footer){
 document.getElementById('overlay').innerHTML='<div class="dialog" role="dialog" aria-modal="true"><div class="dialog-head"><h2>'+title+'</h2><button class="btn small" onclick="closeModal()">✕</button></div>'+fields+'<div class="dialog-footer"><button class="btn" onclick="closeModal()">Abbrechen</button>'+footer+'</div></div>';
 document.getElementById('overlay').classList.remove('hidden');
}
function field(label,id,value,kind){
 var control=kind==='textarea'?'<textarea id="'+id+'">'+esc(value)+'</textarea>':'<input id="'+id+'" type="'+(kind||'text')+'" value="'+esc(value)+'">';
 return '<div class="field"><label for="'+id+'">'+label+'</label>'+control+'</div>';
}
function select(label,id,values,current){
 return '<div class="field"><label for="'+id+'">'+label+'</label><select id="'+id+'">'+values.map(function(x){return '<option '+(current===x?'selected':'')+'>'+esc(x)+'</option>';}).join('')+'</select></div>';
}
function val(id){return document.getElementById(id)?.value.trim()||'';}
function modalProject(id){
 var p=id?pg(id):null;
 showDialog(p?'Projekt bearbeiten':'Neues Projekt',field('Projektname *','p-name',p?.name||'')+field('Beschreibung','p-desc',p?.description||'','textarea')+select('Status','p-status',statusProject,p?.status||'Aktiv')+'<div class="field"><label>Projektfarbe</label><div class="row">'+palette.map(function(c){return '<button type="button" class="btn small" style="background:'+c+';width:32px;height:32px;border:2px solid '+(c===(p?.color||palette[0])?'white':'transparent')+';border-radius:50%" onclick="setColor(\''+c+'\',this)"></button>';}).join('')+'</div><input type="hidden" id="p-color" value="'+(p?.color||palette[0])+'"></div>','<button class="btn primary" onclick="saveProject(\''+(id||'')+'\')">Speichern</button>');
}
function setColor(c,button){document.getElementById('p-color').value=c;button.parentNode.querySelectorAll('button').forEach(function(b){b.style.borderColor='transparent';});button.style.borderColor='white';}
function saveProject(id){
 if(!val('p-name'))return toast('Bitte Projektnamen eingeben');
 var p=id?pg(id):null;
 if(p){p.name=val('p-name');p.description=val('p-desc');p.status=val('p-status');p.color=val('p-color');}
 else state.projects.push({id:uid(),name:val('p-name'),description:val('p-desc'),status:val('p-status'),color:val('p-color'),createdAt:Date.now()});
 persist();closeModal();render();toast('Projekt gespeichert');
}
function modalTask(id){
 var t=id?task(id):null,pid=t?t.projectId:currentProject;
 if(!pid){if(!state.projects.length){toast('Erstelle zuerst ein Projekt');modalProject();return;}pid=state.projects[0].id;}
 var options=state.projects.map(function(p){return '<option value="'+p.id+'" '+(pid===p.id?'selected':'')+'>'+esc(p.name)+'</option>';}).join('');
 showDialog(t?'Aufgabe bearbeiten':'Neue Aufgabe','<div class="field"><label>Projekt</label><select id="t-proj">'+options+'</select></div>'+field('Aufgabenname *','t-name',t?.name||'')+field('Beschreibung','t-desc',t?.description||'','textarea')+select('Status','t-status',statusTask,t?.status||'Offen')+field('Geschätzte Stunden (optional)','t-budget',t?.budgetHours||'','number'),'<button class="btn primary" onclick="saveTask(\''+(id||'')+'\')">Speichern</button>');
}
function saveTask(id){
 if(!val('t-name'))return toast('Bitte Aufgabennamen eingeben');
 var budget=parseFloat(val('t-budget'));if(val('t-budget')&&(!isFinite(budget)||budget<0))return toast('Ungültiges Zeitbudget');
 var t=id?task(id):null;
 if(t){t.name=val('t-name');t.description=val('t-desc');t.status=val('t-status');t.projectId=val('t-proj');t.budgetHours=budget||0;}
 else{t={id:uid(),name:val('t-name'),description:val('t-desc'),status:val('t-status'),projectId:val('t-proj'),budgetHours:budget||0};state.tasks.push(t);}
 persist();closeModal();render();toast('Aufgabe gespeichert');
}
function startTimer(taskId){
 var a=active();if(a){toast('Bitte zuerst den aktiven Timer stoppen');return;}
 var t=task(taskId);if(!t)return;
 var now=Date.now();
 state.sessions.push({id:uid(),taskId:taskId,start:now,end:null,note:'',status:'running',segments:[{start:now,end:null}]});
 if(t.status==='Offen')t.status='In Bearbeitung';
 persist();render();toast('Timer gestartet');
}
function togglePause(){
 var s=active();if(!s)return;
 var now=Date.now();
 if(s.status==='running'){var last=s.segments[s.segments.length-1];if(last&&!last.end)last.end=now;s.status='paused';toast('Timer pausiert');}
 else{s.segments.push({start:now,end:null});s.status='running';toast('Timer fortgesetzt');}
 persist();render();
}
function stopTimer(){
 var s=active();if(!s)return;
 var now=Date.now();
 if(s.status==='running'){var last=s.segments[s.segments.length-1];if(last&&!last.end)last.end=now;}
 s.status='done';s.end=now;persist();render();toast('Session gespeichert');
}
function modalSession(id){
 var s=id?state.sessions.find(function(x){return x.id===id;}):null;
 if(s&&s.status!=='done')return toast('Aktive Session zuerst stoppen');
 var taskId=s?s.taskId:currentTask;
 if(!taskId) {toast('Öffne zuerst eine Aufgabe');return;}
 var start=s?s.start:Date.now()-3600000,end=s?(s.end||Date.now()):Date.now();
 var durationMin=s?Math.round(duration(s,Date.now())/60000*100)/100:60;
 showDialog(s?'Session bearbeiten':'Session nachtragen',field('Notiz / Sessionname','s-note',s?.note||'')+'<div class="form-row">'+field('Startdatum und -zeit','s-start',inputDateTime(start),'datetime-local')+field('Enddatum und -zeit','s-end',inputDateTime(end),'datetime-local')+'</div>'+field('Tatsächlich gearbeitet (Minuten, ohne Pausen)','s-work',durationMin,'number')+'<p class="muted tiny">Die Arbeitsdauer kannst du unabhängig von Start und Ende korrigieren. Pausenzeiten sind nicht enthalten.</p>','<button class="btn primary" onclick="saveSession(\''+(id||'')+'\',\''+taskId+'\')">Speichern</button>');
}
function saveSession(id,taskId){
 var start=millis(val('s-start')),end=millis(val('s-end')),minutes=parseFloat(val('s-work'));
 if(!isFinite(start)||!isFinite(end)||end<=start)return toast('Ungültiger Zeitraum');
 if(!isFinite(minutes)||minutes<0||minutes>(end-start)/60000+0.01)return toast('Arbeitsdauer muss zum Zeitraum passen');
 var s=id?state.sessions.find(function(x){return x.id===id;}):null;
 if(s){s.start=start;s.end=end;s.note=val('s-note');s.workMs=Math.round(minutes*60000);s.segments=[{start:start,end:end}];}
 else state.sessions.push({id:uid(),taskId:taskId,start:start,end:end,note:val('s-note'),status:'done',segments:[{start:start,end:end}],workMs:Math.round(minutes*60000)});
 persist();closeModal();render();toast('Session gespeichert');
}
function removeSession(id){
 var s=state.sessions.find(function(x){return x.id===id;});
 if(!s||!confirm('Diese Session unwiderruflich löschen?'))return;
 state.sessions=state.sessions.filter(function(x){return x.id!==id;});
 persist();render();toast('Session gelöscht');
}
function removeTask(id){
 if(!confirm('Aufgabe und alle zugehörigen Sessions unwiderruflich löschen?'))return;
 state.tasks=state.tasks.filter(function(t){return t.id!==id;});
 state.sessions=state.sessions.filter(function(s){return s.taskId!==id;});
 currentTask=null;page='project';persist();render();toast('Aufgabe gelöscht');
}
function removeProject(id){
 if(!confirm('Projekt, alle Aufgaben und Sessions unwiderruflich löschen?'))return;
 var ids=nTasks(id).map(function(t){return t.id;});
 state.projects=state.projects.filter(function(p){return p.id!==id;});
 state.tasks=state.tasks.filter(function(t){return t.projectId!==id;});
 state.sessions=state.sessions.filter(function(s){return ids.indexOf(s.taskId)===-1;});
 currentProject=null;currentTask=null;page='projects';persist();render();toast('Projekt gelöscht');
}
function exportCsv(){
 var rows=[['Projekt','Aufgabe','Session-Notiz','Start','Ende','Arbeitszeit (Sek.)','Arbeitszeit (HH:MM:SS)','Status']];
 state.sessions.slice().sort(function(a,b){return a.start-b.start;}).forEach(function(s){
  var t=task(s.taskId),p=t?pg(t.projectId):null;
  rows.push([p?.name||'',t?.name||'',s.note||'',dateTime(s.start),s.end?dateTime(s.end):'',Math.floor(duration(s,Date.now())/1000),fmt(duration(s,Date.now())),s.status==='done'?'Abgeschlossen':s.status==='running'?'Läuft':'Pausiert']);
 });
 var csv='\ufeff'+rows.map(function(row){return row.map(function(v){return '"'+String(v).replace(/"/g,'""')+'"';}).join(';');}).join('\r\n');
 if(window.TimeFlowNative){TimeFlowNative.exportCsv(csv);toast('Speicherort für CSV auswählen');}
 else{var blob=new Blob([csv],{type:'text/csv;charset=utf-8'}),url=URL.createObjectURL(blob),a=document.createElement('a');a.href=url;a.download='TimeFlow-Zeiterfassung.csv';a.click();setTimeout(function(){URL.revokeObjectURL(url);},1200);}
}
window.go=function(to){page=to;searchText='';render();window.scrollTo(0,0);};
window.openProject=function(id){currentProject=id;currentTask=null;page='project';searchText='';render();window.scrollTo(0,0);};
window.openTask=function(id){currentTask=id;currentProject=task(id)?.projectId||null;page='task';searchText='';render();window.scrollTo(0,0);};
window.doSearch=function(v){searchText=v;var inp=document.getElementById('q');var start=inp?.selectionStart||v.length;render();var newInput=document.getElementById('q');if(newInput){newInput.focus();newInput.setSelectionRange(start,start);}};
window.toggleTheme=function(){state.theme=state.theme==='dark'?'light':'dark';persist();render();};
window.setFilter=function(){window.analyticsFrom=val('filterFrom');window.analyticsTo=val('filterTo');render();};
window.modalProject=modalProject;window.saveProject=saveProject;window.removeProject=removeProject;
window.modalTask=modalTask;window.saveTask=saveTask;window.removeTask=removeTask;
window.setColor=setColor;window.modalSession=modalSession;window.saveSession=saveSession;window.removeSession=removeSession;
window.startTimer=startTimer;window.togglePause=togglePause;window.stopTimer=stopTimer;
window.closeModal=closeModal;window.exportCsv=exportCsv;
window.appBack=function(){
 if(!document.getElementById('overlay').classList.contains('hidden')){closeModal();return true;}
 if(page==='task'){window.openProject(currentProject);return true;}
 if(page==='project'){window.go('projects');return true;}
 if(page!=='dashboard'){window.go('dashboard');return true;}
 return false;
};
window.addEventListener('keydown',function(e){
 if(e.key==='Escape')closeModal();
 if(e.key.toLowerCase()==='n'&&e.ctrlKey){e.preventDefault();modalProject();}
});
setInterval(function(){
 var a=active();if(!a)return;
 document.querySelectorAll('[data-live-time]').forEach(function(el){el.textContent=fmt(duration(a,Date.now()));});
 var total=document.querySelector('[data-task-time]');if(total&&currentTask)total.textContent=fmt(taskMs(currentTask));
},1000);
load();render();
})();