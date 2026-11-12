/* Kabelsalat: printable technical overview + single-file read-only project viewer. */
(function(root,factory){
 const api=factory();
 if(typeof module==='object'&&module.exports)module.exports=api;
 if(root)root.KabelsalatReports=api;
})(typeof window!=='undefined'?window:null,function(){
'use strict';
const VIEWER="<!doctype html><html lang=\"de\"><head><meta charset=\"utf-8\">\n<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">\n<meta http-equiv=\"Content-Security-Policy\" content=\"default-src 'none'; script-src 'unsafe-inline'; style-src 'unsafe-inline'\">\n<title>Kabelsalat · Offline-Viewer</title>\n<style>\n*{box-sizing:border-box}body{margin:0;background:#eff4f5;color:#25414a;font:13px/1.5 \"Segoe UI\",Arial,sans-serif}\nheader{background:#174a46;color:white;padding:15px 20px}header b{font-size:18px}\nheader p{margin:0;color:#bfd6d1}.warning{padding:9px 20px;background:#fff1dc}\nmain{display:grid;grid-template-columns:280px minmax(280px,1fr) 310px;gap:12px;padding:14px;max-width:1600px;margin:auto}\n.panel{border:1px solid #d9e5e7;background:white;border-radius:11px;padding:12px;min-width:0}\nh2{font-size:15px;margin:0 0 9px}select,input{width:100%;padding:8px;border-radius:7px;border:1px solid #d6e0e3;margin:6px 0 12px}\nsvg{background:#f9fcfd;border:1px solid #dce6e9;border-radius:9px;width:100%;height:auto}\n.item{padding:8px;margin:5px 0;border:1px solid #dce6e9;border-radius:7px;cursor:pointer;word-break:break-word}\n.item.active{border-color:#61a78e;background:#eaf8f2}small{color:#6c838b}\n@media(max-width:900px){main{grid-template-columns:1fr}}\n</style></head><body>\n<header><b>Kabelsalat · Offline-Projektviewer</b><p>Nur Lesezugriff · Schachtnetz, Leerrohre, Kabel</p></header>\n<div class=\"warning\">Ungeprüfter Planungsentwurf. Keine baurechtliche, elektrotechnische oder brandschutztechnische Freigabe.</div>\n<main>\n<section class=\"panel\"><h2>Netzkarte</h2><svg id=\"map\" viewBox=\"0 0 280 200\"></svg>\n<label>Schacht<select id=\"shaft\"></select></label><label>Suche<input id=\"search\" placeholder=\"Kabelname, ID, System\"></label><div id=\"cables\"></div></section>\n<section class=\"panel\"><h2 id=\"title\">Schacht</h2><small id=\"dims\"></small><label>Wand<select id=\"wall\"></select></label>\n<svg id=\"wallSvg\" viewBox=\"0 0 600 480\"></svg><small>Schematische Rohrpositionen – keine vermessenen Koordinaten. Die mm-Umrechnung aus Modellbreiten ist kein Aufmaß.</small></section>\n<section class=\"panel\"><h2>Netz-Kabelwege</h2><div id=\"routes\"></div><h2>Schachtverbindungen</h2><div id=\"links\"></div></section>\n</main>\n<script>\nconst DATA=__DATA__;\n(function(){\n const get=id=>document.getElementById(id),ns='http://www.w3.org/2000/svg';\n const element=(tag,txt,cls)=>{const e=document.createElement(tag);if(cls)e.className=cls;e.textContent=String(txt);return e;};\n const svg=(tag,attrs)=>{const e=document.createElementNS(ns,tag);for(const k in attrs)e.setAttribute(k,attrs[k]);return e;};\n let shaftId=DATA.shafts[0]?DATA.shafts[0].id:'',wallId='SW1',selectedCable='';\n const shaft=()=>DATA.shafts.find(s=>s.id===shaftId);\n const clear=el=>el.replaceChildren();\n function map(){\n const el=get('map');clear(el);\n const xs=DATA.shafts.map(s=>s.x),zs=DATA.shafts.map(s=>s.z);\n const minX=Math.min(0,...xs),maxX=Math.max(0,...xs),minZ=Math.min(0,...zs),maxZ=Math.max(0,...zs);\n const locate=s=>({x:26+(s.x-minX)/Math.max(1,maxX-minX)*228,y:24+(s.z-minZ)/Math.max(1,maxZ-minZ)*145});\n for(const l of DATA.links){\n const a=DATA.shafts.find(s=>s.id===l.from),b=DATA.shafts.find(s=>s.id===l.to);\n if(!a||!b)continue;\n const p=locate(a),q=locate(b);el.append(svg('line',{x1:p.x,y1:p.y,x2:q.x,y2:q.y,stroke:'#a3bbc1','stroke-width':'4'}));\n }\n for(const s of DATA.shafts){\n const p=locate(s),circle=svg('circle',{cx:p.x,cy:p.y,r:'12',fill:s.id===shaftId?'#186755':'#668a99'});\n circle.addEventListener('click',()=>{shaftId=s.id;wallId='SW1';selectedCable='';render();});\n el.append(circle);const t=svg('text',{x:p.x,y:p.y-18,fill:'#375565','text-anchor':'middle','font-size':'10'});\n t.textContent=s.id;el.append(t);\n }\n }\n function wall(){\n const el=get('wallSvg');clear(el);\n const s=shaft(),p=s?.snapshot,w=p?.walls.find(x=>x.id===wallId);if(!w)return;\n const pdf=p.sourceInfo?.sourceVersion?.startsWith('PDF V2'),width=(wallId==='SW1'||wallId==='SW3')?(pdf?p.dims.w:p.dims.d):(pdf?p.dims.d:p.dims.w),height=p.dims.h;\n const scale=Math.min(535/width,400/height),l=(600-width*scale)/2,t=(460-height*scale)/2;\n el.append(svg('rect',{x:l,y:t,width:width*scale,height:height*scale,fill:'#e7eff1',stroke:'#688290','stroke-width':'2'}));\n for(const h of w.holes){\n const circle=svg('circle',{cx:l+h.u*width*scale,cy:t+(1-h.v)*height*scale,\n r:Math.max(2,(h.dimensionSpec?.openingMm||h.dn||70)/2*scale),fill:'#bfe1d9',stroke:'#5f8a82'});\n const title=svg('title',{});title.textContent=h.id+' / DN '+h.dn+' (schematisch) / '+(pdf?'PDF-Vektorpunkt u='+h.sourceU+', v='+h.sourceV:'Modellpunkt u='+h.u+', v='+h.v)+' / keine vermessene Schachtposition';\n circle.append(title);el.append(circle);\n }\n const label=svg('text',{x:300,y:475,'text-anchor':'middle','font-size':12,fill:'#557580'});\n label.textContent='Modell-Spanne '+width+' × '+height+' mm (ungeprüft) · '+w.holes.length+' Öffnungen';el.append(label);\n }\n function cables(){\n const el=get('cables');clear(el);const s=shaft(),q=get('search').value.toLowerCase();\n for(const c of s?.snapshot.cables||[]){\n if(q&&![c.id,c.name,c.group].some(x=>String(x||'').toLowerCase().includes(q)))continue;\n const item=element('div',c.name+' · '+c.group+' ['+c.id+']','item'+(selectedCable===c.id?' active':''));\n item.addEventListener('click',()=>{selectedCable=c.id;render();});el.append(item);\n if(selectedCable===c.id){\n el.append(element('small','Anschluss A '+(c.a?c.a.wall+'/'+c.a.hole:'–')+\n ' · B '+(c.b?c.b.wall+'/'+c.b.hole:'–')));\n }\n }\n }\n function routes(){\n const el=get('routes');clear(el);\n for(const route of DATA.routes){\n el.append(element('div',route.name+' ('+route.group+')','item'));\n for(const step of route.steps){\n const item=element('div',step.shaftId+' / '+step.cableId,'item');\n item.addEventListener('click',()=>{shaftId=step.shaftId;selectedCable=step.cableId;render();});\n el.append(item);\n }\n }\n const links=get('links');clear(links);\n for(const l of DATA.links)links.append(element('div',l.from+' ↔ '+l.to,'item'));\n }\n function render(){\n const s=shaft();if(!s)return;\n get('title').textContent=s.name+' · '+s.id;\n get('dims').textContent='Projektlage X '+s.x+' / Z '+s.z+' mm (nicht vermessen) · Modellmaße '+s.snapshot.dims.w+' × '+s.snapshot.dims.d+' × '+s.snapshot.dims.h+' mm (ungeprüft) · Quelle: '+(s.snapshot.sourceInfo?.sourceVersion||'Schematischer Entwurf');\n const select=get('shaft');clear(select);\n for(const sh of DATA.shafts){const o=element('option',sh.name+' ('+sh.id+')');o.value=sh.id;select.append(o);}\n select.value=shaftId;\n const walls=get('wall');clear(walls);\n for(const w of s.snapshot.walls){const o=element('option',w.id);o.value=w.id;walls.append(o);}\n walls.value=wallId;\n map();wall();cables();routes();\n }\n get('shaft').addEventListener('change',e=>{shaftId=e.target.value;wallId='SW1';selectedCable='';render();});\n get('wall').addEventListener('change',e=>{wallId=e.target.value;wall();});\n get('search').addEventListener('input',cables);\n render();\n})();\n</script></body></html>";
const esc=x=>String(x??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;','\'':'&#39;'}[c]));
const row=values=>'<tr>'+values.map(x=>'<td>'+esc(x===null||x===undefined?'–':x)+'</td>').join('')+'</tr>';
const table=(columns,rows)=>'<table><thead><tr>'+columns.map(x=>'<th>'+esc(x)+'</th>').join('')+'</tr></thead><tbody>'+
 rows.map(row).join('')+'</tbody></table>';
function reportHTML(project,result){
 const net=project.network||{shafts:[],links:[],routes:[]},shafts=net.shafts||[],
 counts=result.counts||{},cfg=result.config||{};
 let h='<!doctype html><html lang="de"><head><meta charset="utf-8">'+
 '<meta http-equiv="Content-Security-Policy" content="default-src &#39;none&#39;;style-src &#39;unsafe-inline&#39;">'+
 '<title>Kabelsalat · Prüfbericht</title><style>'+
 '@page{size:A4;margin:15mm}body{font:12px/1.5 Arial,sans-serif;color:#263840;margin:24px auto;max-width:920px}'+
 'h1{color:#185650}h2{font-size:17px;border-bottom:1px solid #cadadf;padding-top:15px}'+
 'table{width:100%;border-collapse:collapse;margin:10px 0 18px;table-layout:fixed}'+
 'th,td{border:1px solid #d6e1e4;padding:5px;text-align:left;vertical-align:top;overflow-wrap:anywhere}'+
 'th{background:#edf3f5}tr{break-inside:avoid}.warn{background:#fff1e2;padding:12px;border-left:4px solid #a76a33}'+
 '@media print{body{max-width:none;margin:0}}'+
 '</style></head><body><!-- Kabelsalat Report v1 -->'+
 '<h1>Kabelsalat · Prüf- und Bestandsbericht</h1><p>'+esc(project.name)+
 ' · '+esc(new Date().toISOString())+'</p>'+
 '<p class="warn"><b>ENTWURF / KEINE FACHFREIGABE.</b> Automatisierte Daten- und Geometriehinweise sind kein Nachweis der DIN-/VDE-, Bau- oder Brandschutz-Konformität. Manuelle Statusangaben sind nicht unabhängig verifiziert. Modell- und PDF-Vektorkoordinaten sind keine vermessenen Rohr- oder Schachtpositionen.</p>'+
 '<h2>Bestand im Projekt</h2>'+
 table(['Schächte','Verbindungen','Netzkabelwege','Fehler','Warnungen'],[[
 shafts.length,net.links.length,net.routes.length,counts.error||0,counts.warning||0]])+
 '<h2>Projektwerte für Hinweise (nicht normativ)</h2>'+
 table(['Rohr-Rohr-Abstand','Randabstand','Bogenradius-Näherung'],[[
 cfg.minPipeGapMm===null?'Nicht gesetzt':cfg.minPipeGapMm+' mm',
 cfg.minEdgeMm===null?'Nicht gesetzt':cfg.minEdgeMm+' mm',
 cfg.minBendRadiusMm===null?'Nicht gesetzt':cfg.minBendRadiusMm+' mm']]);
 for(const shaft of shafts){
 const p=shaft.snapshot;
 h+='<h2>'+esc(shaft.id)+' – '+esc(shaft.name)+'</h2><p>Lage X '+esc(shaft.x)+
 ' / Z '+esc(shaft.z)+' mm (Projektwert, nicht vermessen) · Modellabmessungen '+esc(p.dims.w)+' × '+esc(p.dims.d)+
 ' × '+esc(p.dims.h)+' mm (ungeprüft) · Quelle: '+esc(p.sourceInfo?.sourceVersion||'Projektentwurf')+'</p>'+
 table(['Kabel-ID','Bezeichnung','System','Anschluss A','Anschluss B'],
 (p.cables||[]).map(c=>[c.id,c.name,c.group,
 c.a?c.a.wall+' / '+c.a.hole:'Nicht zugeordnet',
 c.b?c.b.wall+' / '+c.b.hole:'Nicht zugeordnet']));
 }
 h+='<h2>Netz-Kabelwege</h2>'+
 table(['ID','Name','System','Segmente'],net.routes.map(r=>[
 r.id,r.name,r.group,r.steps.map(s=>s.shaftId+' / '+s.cableId).join(' → ')]))+
 '<h2>Daten- und Geometriehinweise</h2>'+
 table(['Stufe','Code','Schacht','Objekt','Beschreibung'],
 (result.issues||[]).slice(0,500).map(x=>[x.severity,x.code,x.shaftId,x.ref,x.detail]))+
 '<h2>Manuelle Prüfvermerke</h2>'+
 table(['Schacht','Objekt','Status','Quelle','Person','Zeitpunkt','Notiz'],
 (net.reviews||[]).map(x=>[x.shaftId,x.entityId,x.status,x.source,x.checkedBy,x.checkedAt,x.note]))+
 '<h2>Lokale Änderungshistorie (letzte 50)</h2>'+
 table(['Zeitpunkt','Schacht','Vorgang','Details'],
 (net.audit||[]).slice(-50).reverse().map(x=>[x.at,x.shaftId,x.action,x.description]))+
 '<p><small>Kabelsalat ist kein zugelassenes Ingenieur- oder Abnahmesystem. Reale Begehungs- und Normnachweise sind separat erforderlich.</small></p></body></html>';
 return h;
}
function viewerHTML(project){
 const json=JSON.stringify(project.network||{shafts:[],links:[],routes:[]})
 .replace(/</g,'\\u003c').replace(/>/g,'\\u003e').replace(/&/g,'\\u0026')
 .replace(/\u2028/g,'\\u2028').replace(/\u2029/g,'\\u2029');
 return VIEWER.replace('__DATA__',json);
}
return {esc,reportHTML,viewerHTML};
});
