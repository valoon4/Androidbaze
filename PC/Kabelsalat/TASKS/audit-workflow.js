'use strict';
/* Kabelsalat: read-only evidence/counter audit; zero status or source changes.
 * KAB-0811…0828. Parent tasks, atomic microtasks, milestones and release
 * versions are deliberately counted in separate namespaces.
 */
const fs=require('node:fs'),path=require('node:path'),assert=require('node:assert/strict');
const SOURCE=['01-P0-QUELLE-UND-PITCH.md','02-P1-EDITOR-UND-NETZ.md',
 '03-P2-DATEN-UND-PRUEFUNG.md','04-P3-QA-BETRIEB-UND-FEEDBACK.md',
 '05-BRO-CHAT-AVL-UND-REPORTS.md','06-BRO-CHAT-PRUEFFALL-UND-ARTEFAKT-QA.md',
 '07-BRO-BETON-ROHR-DM.md','08-BRO-KURZE-LR-DN30-160.md'];
const WAVE=['S0','S1','S2','S3','S4','S5','S6'];
function csv(line,delimiter){
 const out=[];let value='',quote=false;
 for(let i=0;i<line.length;i++){
  const c=line[i];
  if(c==='"'){if(quote&&line[i+1]==='"'){value+='"';i++;}else quote=!quote;}
  else if(c===delimiter&&!quote){out.push(value);value='';}
  else value+=c;
 }
 if(quote)throw Error('CSV: unmatched quote');
 out.push(value);return out;
}
function canonical(files){
 const ids=new Map(),titleSet=new Set(),byFile={};
 for(const file of SOURCE){
  if(typeof files[file]!=='string')throw Error('Missing canonical file '+file);
  const lines=files[file].split(/\r?\n/);
  let count=0;
  for(const line of lines){
   if(!line.startsWith('- [')||!line.includes('KAB-'))continue;
   const m=line.match(/^- \[([ x])\] \*\*(KAB-(\d{4}))\*\* – (.+)$/);
   if(!m)throw Error('Invalid parent status '+file+': '+line.slice(0,110));
   const [,mark,id,n,title]=m;
   if(ids.has(id))throw Error('Duplicate canonical ID '+id);
   if(titleSet.has(title.replace(/\s*[·—-]\s*Nachweis:\s*.*$/i,'').toLowerCase()))
    throw Error('Duplicate parent title '+id);
   titleSet.add(title.replace(/\s*[·—-]\s*Nachweis:\s*.*$/i,'').toLowerCase());
   if(mark==='x'&&!/Nachweis:\s*\S+/.test(title))throw Error('Parent closed without evidence '+id);
   ids.set(id,{id,n:Number(n),file,done:mark==='x',title});count++;
  }
  byFile[file]=count;
 }
 const sorted=[...ids.values()].sort((a,b)=>a.n-b.n);
 for(let i=0;i<sorted.length;i++)if(sorted[i].n!==i+1)
  throw Error('Canonical ID gap/recycle near '+(i+1));
 return {ids,byFile,parents:sorted};
}
function atomic(files,parents){
 const rows=new Map(),counts={},byWave={};
 for(const wave of WAVE){
  const source=files['ATOMIC/'+wave+'.csv'];
  if(typeof source!=='string')throw Error('Missing atomic CSV '+wave);
  const lines=source.trim().split(/\r?\n/);
  const hdr=csv(lines.shift(),';');
  if(hdr[2]!=='Mikro-ID'||hdr[8]!=='Status')throw Error('Atomic column mismatch '+wave);
  const stat={total:0,complete:0};
  for(const line of lines){
   const cols=csv(line,';'),[rank,parent,id,w,epic,act,criterion,minutes,status]=cols;
   if(cols.length!==11||w!==wave||!parent.match(/^KAB-\d{4}$/)||
    !id.startsWith(parent+'/M')||rows.has(id)||!parents.ids.has(parent)||
    !['offen','erledigt'].includes(status)||!Number.isInteger(Number(minutes))||
    !epic||!act||!criterion)throw Error('Invalid/duplicate atomic row '+id);
   rows.set(id,{parent,wave,epic,status,minutes:Number(minutes)});
   stat.total++;
   if(status==='erledigt')stat.complete++;
  }
  byWave[wave]=stat;
 }
 const completed=files['ATOMIC/COMPLETED.csv'];
 if(typeof completed!=='string')throw Error('Missing completed CSV');
 const lines=completed.trim().split(/\r?\n/);
 if(lines.shift()!=='Mikro-ID;Abgeschlossen-am;Nachweis')throw Error('Completed header changed');
 const proofs=new Map();
 for(const line of lines){
  const [id,date,url]=line.split(';');
  if(!/^KAB-\d{4}\/M\d{2}$/.test(id)||!/^20\d{2}-\d{2}-\d{2}$/.test(date)||
    !/^https:\/\/github\.com\/valoon4\/Androidbaze\/blob\/[0-9a-f]{40}\/PC\/Kabelsalat\//.test(url)||proofs.has(id)||
    !rows.has(id))throw Error('Missing, duplicated or invalid completion proof '+id);
  proofs.set(id,{date,url});
 }
 for(const [id,row] of rows)if((row.status==='erledigt')!==proofs.has(id))
  throw Error('Atomic tracker and proof ledger disagree '+id);
 for(const p of parents.parents)counts[p.id]=0;
 for(const r of rows.values())counts[r.parent]++;
 return {rows,proofs,byWave,byParent:counts};
}
function audit(files){
 const c=canonical(files),a=atomic(files,c);
 const n={parents:c.parents.length,closed:c.parents.filter(x=>x.done).length,
  open:c.parents.filter(x=>!x.done).length,micros:a.rows.size,
  completed:a.proofs.size,remaining:a.rows.size-a.proofs.size};
 if(n.parents!==888||n.micros!==13012||n.closed+n.open!==n.parents)
  throw Error('Canonical baseline changed; review is required');
 if(Object.values(a.byWave).reduce((n,x)=>n+x.total,0)!==n.micros)
  throw Error('Wrong wave aggregation');
 if(a.byWave.S0.total!==1351||a.byWave.S6.total!==2071)
  throw Error('Unexpected atomic stage baseline');
 const epic=new Map(),waves=[];
 for(const [name,status] of Object.entries(a.byWave)){
  waves.push({wave:name,...status,open:status.total-status.complete});
 }
 for(const r of a.rows.values()){
  const e=epic.get(r.epic)||{epic:r.epic,total:0,completed:0};
  e.total++;if(r.status==='erledigt')e.completed++;epic.set(r.epic,e);
 }
 const epics=[...epic.values()].sort((a,b)=>a.epic.localeCompare(b.epic,'de'));
 return {totals:n,waves,epics,
  milestones:{statement:'Entwicklungsmeilensteine 25/50/75/100 sind keine Task-Abnahmequoten',
   denominator:n.micros},parentFileCounts:c.byFile,ids:[...c.ids.keys()]};
}
function disk(root=__dirname){
 const files={};for(const file of SOURCE)files[file]=fs.readFileSync(path.join(root,file),'utf8');
 for(const w of WAVE)files['ATOMIC/'+w+'.csv']=fs.readFileSync(path.join(root,'ATOMIC',w+'.csv'),'utf8');
 files['ATOMIC/COMPLETED.csv']=fs.readFileSync(path.join(root,'ATOMIC','COMPLETED.csv'),'utf8');
 return files;
}
function markdown(report){
 const t=report.totals,fmt=x=>String(x);
 const lines=['# Kabelsalat · nachweisbasierter Status','',
  '| Bereich | Erledigt | Offen | Gesamt |','|---|---:|---:|---:|',
  '| **Mikro-Tasks** | **'+t.completed+'** | **'+t.remaining+'** | **'+t.micros+'** |',
  '| Eltern-Tickets | '+t.closed+' | '+t.open+' | '+t.parents+' |','',
  '## Wellen','', '| Welle | Erledigt | Offen | Gesamt |',
  '|---|---:|---:|---:|',...report.waves.map(w=>'| '+w.wave+' | '+w.complete+' | '+w.open+' | '+w.total+' |'),
  '','## Epics','',
  '| Epic | Erledigt | Offen | Gesamt |','|---|---:|---:|---:|',
  ...report.epics.map(e=>'| '+e.epic.replaceAll('|','/')+' | '+e.completed+' | '+(e.total-e.completed)+' | '+e.total+' |'),
  '','**Wichtig:** Meilensteine sind keine Abnahmequoten. Eine [x]-Parent-Checkbox erfordert einen eigenen Nachweis; ein Mikrostatus ersetzt diese nicht.'];
 return lines.join('\n')+'\n';
}
if(require.main===module){
 const result=audit(disk());
 if(process.argv.includes('--json'))process.stdout.write(JSON.stringify(result,null,2)+'\n');
 else if(process.argv.includes('--md'))process.stdout.write(markdown(result));
 else process.stdout.write('KABELSALAT AUDIT OK: '+result.totals.completed+'/'+
  result.totals.micros+' Mikro, '+result.totals.closed+'/'+result.totals.parents+
  ' Parents; '+result.waves.length+' Wellen, '+result.epics.length+' Epics.\n');
}
module.exports={csv,canonical,atomic,audit,disk,markdown,SOURCE,WAVE};
