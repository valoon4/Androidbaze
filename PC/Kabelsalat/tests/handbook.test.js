'use strict';
/* KAB-0325/0327, 0564, 0630, 0775–0792, 0804, 0811–0828:
 * 41 source-specific handbook entries, 7 historical feedback blocker notes.
 * Validate every local and GitHub-blob reference and no fake acceptance.
 */
const assert=require('node:assert/strict');
const fs=require('node:fs'),path=require('node:path');
const root=path.resolve(__dirname,'../../..'),project=path.join(root,'PC/Kabelsalat');
const handbook=path.join(project,'TASKS/HANDBOOK'),feedback=path.join(project,'TASKS/FEEDBACK');
const ids=['0325','0327','0564','0630',...Array.from({length:18},(_,i)=>String(775+i).padStart(4,'0')),
 '0804',...Array.from({length:18},(_,i)=>String(811+i).padStart(4,'0'))]
 .map(n=>'KAB-'+n);
assert.equal(ids.length,41);
const fbIDs=['0793','0794','0796','0797','0798','0799','0800'].map(n=>'KAB-'+n);
function checkLinks(markdown,file){
 const regex=/\]\(([^)]+)\)/g;
 let count=0,match;
 while((match=regex.exec(markdown))){
  const url=match[1].split('#')[0];
  if(!url||url.startsWith('https://github.com/valoon4/Androidbaze/issues/')||
    url.startsWith('https://github.com/valoon4/Androidbaze/actions/')||
    url.startsWith('https://valoon4.github.io/'))continue;
  let abs;
  const prefix='https://github.com/valoon4/Androidbaze/blob/main/';
  if(url.startsWith(prefix))abs=path.join(root,decodeURIComponent(url.slice(prefix.length)));
  else if(url.startsWith('http'))throw Error('Unverified external source '+url+' in '+file);
  else abs=path.resolve(path.dirname(file),decodeURIComponent(url));
  assert.ok(abs.startsWith(root+path.sep),'Source outside repo: '+url);
  assert.ok(fs.existsSync(abs),'Dead handbook link '+url+' in '+file);
  count++;
 }
 assert.ok(count>=2,'Handbook must link at least two actual files: '+file);
 return count;
}
let sources=0;
for(const id of ids){
 const file=path.join(handbook,id+'.md');
 assert.ok(fs.existsSync(file),'Missing handbook chapter '+id);
 const data=fs.readFileSync(file,'utf8');
 assert.ok(data.startsWith('# '+id+' · '),'Wrong handbook parent ID '+id);
 assert.ok(data.length>1450,'Empty/padded handbook '+id);
 assert.ok(data.includes('13.012'),'Mikro-Task denominator must be clear');
 assert.ok(data.includes('888'),'Parent task count must be clear');
 assert.match(data,/keine|nicht|unbestätigt|offen|Grenze/i,'Technical limitation must be explicit '+id);
 assert.ok(data.includes('## '),'Organized documentation '+id);
 sources+=checkLinks(data,file);
}
for(const id of fbIDs){
 const file=path.join(feedback,id+'.md');
 assert.ok(fs.existsSync(file),'Missing feedback note '+id);
 const s=fs.readFileSync(file,'utf8');
 assert.ok(s.startsWith('# '+id+' · '));
 for(const key of ['BRO_CHAT_CROSSWALK.md','issues/76','## Konkrete einzelne Frage','## Mutationsfreie Zwischenlösung','M01–M08','M09','M10'])
  assert.ok(s.includes(key),'Missing feedback source/blocker section '+id+' '+key);
 assert.ok(s.length>1500);
 sources+=checkLinks(s,file);
}
// Strong source-specific checks for the extra M12 obligations:
const read=id=>fs.readFileSync(path.join(handbook,id+'.md'),'utf8');
assert.match(read('KAB-0784'),/31 historische/i);
assert.equal((read('KAB-0784').match(/^\| (?:E\d\d|B\d\d|N\d\d|D\d\d) \|/gm)||[]).length,31);
assert.match(read('KAB-0776'),/Zweiter Datensatz/);
assert.match(read('KAB-0777'),/SW1=29, SW2=13, SW3=26, SW4=23/);
assert.match(read('KAB-0792'),/Verbotener Seiteneffekt/);
assert.match(read('KAB-0814'),/Zweiter Datensatz/);
assert.match(read('KAB-0823'),/Verbotener Nebeneffekt/);
const screenshots=read('KAB-0791');
for(const w of ['SW1','SW2','SW3','SW4'])
 assert.ok(screenshots.includes('pitch-'+w+'-pdf-vs-demo.png'));
assert.ok(sources>=110,'Too few verified source references');
console.log('HANDBOOK OK: '+ids.length+' precise technical docs, '+fbIDs.length+
 ' feedback blockers, '+sources+' real source links, 31 audited criteria, 4 image pairs');
