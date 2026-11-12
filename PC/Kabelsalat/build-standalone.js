'use strict';
/* Kabelsalat: single HTML document for opening locally in Chrome/Edge without an EXE.
 * All JavaScript dependencies are embedded with CSP SHA-256 hashes.
 * Three.js remains under its original MIT license; the copyright notice is in
 * vendor/LICENSE.threejs and the source repository.
 */
const fs=require('node:fs'),path=require('node:path'),crypto=require('node:crypto');
const pkg=require('./package.json');
function build(){
 let html=fs.readFileSync(path.join(__dirname,'index.html'),'utf8'),found=0;
 const hashes=[];
 html=html.replace(/<script src="([^"]+)"\s*><\/script>/g,(_tag,srcWithVersion)=>{
  const [src,query]=srcWithVersion.split('?');
  if(query!=='v='+pkg.version)throw Error('Ungültige Versionskennung: '+srcWithVersion);
  if(!/^(?:recovery\.js|pdf-geometry\.js|schematic-positions\.js|pipe-dimensions\.js|photo-candidates\.js|concrete-wall\.js|construction-v2\.js|pdf-visual\.js|vendor\/three\.min\.js|editor2d\.js|components\.js|network\.js|quality\.js|reports\.js|reference-data\.js|examples\.js|app\.js)$/.test(src))
   throw Error('Unerlaubte externe Script-Abhängigkeit: '+src);
  const full=path.resolve(__dirname,src);
  if(!full.startsWith(__dirname+path.sep))throw Error('Pfad außerhalb des Projekts: '+src);
  const js=fs.readFileSync(full,'utf8').replace(/<\/script/gi,'<\\/script');
  hashes.push("'sha256-"+crypto.createHash('sha256').update(js).digest('base64')+"'");
  found++;
  return '<script>'+js+'</script>';
 });
 if(found!==17)throw Error('17 lokale Script-Abhängigkeiten erwartet, gefunden: '+found);
 if(!html.includes("script-src 'self'"))throw Error('CSP im Editor nicht gefunden');
 html=html.replace("script-src 'self'","script-src "+hashes.join(' '));
 html=html.replace('</head>',`<!-- Eigenständige Offline-Datei; keine externe Script-Quelle.
 Die enthaltene Three.js-Bibliothek unterliegt der MIT-Lizenz.
 Lizenzhinweise: https://github.com/valoon4/Androidbaze/blob/main/PC/Kabelsalat/vendor/LICENSE.threejs
 Alle Referenzdaten sind schematisch und nicht für eine bautechnische Freigabe geeignet. -->
 </head>`);
 return html;
}
if(require.main===module){
 const target=path.join(__dirname,'dist',`Kabelsalat-${pkg.version}-Browser-Einzeldatei.html`);
 fs.mkdirSync(path.dirname(target),{recursive:true});
 fs.writeFileSync(target,build(),'utf8');
 process.stdout.write(target+'\n');
}
module.exports={build};
