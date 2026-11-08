"""Eugi Movie Cleaner v0.1: Windows GUI, offline, standard library only.

Shows a preview; never deletes or overwrites original files.
MKVToolNix is required only for subtitle muxing.
"""
from __future__ import annotations
import json
import os
import queue
import re
import shutil
import subprocess
import threading
import time
from dataclasses import dataclass, field
from pathlib import Path
import tkinter as tk
from tkinter import ttk, filedialog, messagebox, simpledialog

VIDEOS={".mkv",".avi",".mp4",".m4v",".mov",".wmv",".mpg",".mpeg",".ts",".m2ts",".webm"}
SUBS={".srt",".ass",".ssa",".idx"}
EXCLUDED={"_eugicleaner_backup","_eugicleaner_logs","$recycle.bin","system volume information"}
RELEASE=re.compile(r"(?i)^(?:german|english|deutsch|dl|dubbed|ac3|dts|ddp[0-9.]*|aac|eac3|mp3|atmos|"
                   r"dvdrip|bdrip|brrip|bluray|webdl|webrip|web|hdtv|tvrip|remastered|"
                   r"1080p|2160p|720p|480p|x264|x265|h264|h265|hevc|xvid|divx|"
                   r"proper|repack|internal|uncut|extended|hdr|10bit|pal|ntsc|"
                   r"watchable|contribution|inklusion|limited|subbed|multi)$")
DISC=re.compile(r"(?i)(?:^|[._\s-])(?:cd|disc|disk|part|teil)[._\s-]*([1-9])(?:$|[._\s-])")
EPISODE=re.compile(r"(?i)(?:^|[._\s-])(?:e\d{1,3}|s\d{1,2}e\d{1,3})(?:$|[._\s-])")
FORCED=re.compile(r"(?i)(?:^|[._\s-])forced(?:$|[._\s-])")
LANG={"de":"deu","deu":"deu","ger":"deu","german":"deu",
      "en":"eng","eng":"eng","english":"eng"}
BAD={"film","movie","video","teil","part","sample","cd","dvd","unbekannt"}

def clean_name(name, year):
    """Title and caution. Never attempt fuzzy online identification."""
    if EPISODE.search(name):
        return "", "Episode/Teil ist nicht eindeutig"
    words=re.split(r"[._\s]+",name)
    title=[]
    warning=""
    for token in words:
        if not token: continue
        word=token.strip(" []()")
        if re.fullmatch(r"(?:19|20)\d{2}",word):
            if word!=year:
                warning="Jahr "+word+" unterscheidet sich von Ordner "+year
            break
        if RELEASE.fullmatch(word) or re.fullmatch(r"(?i)(?:cd|disc|disk|teil|part)\d",word):
            break
        title.append(token)
    value=" ".join(title).strip(" -._")
    if re.fullmatch(r"(?i)(?:teil|part|cd|disc|disk)[ ._-]*[1-9]", value):
        return "", "Nur Teilenummer; Filmtitel manuell bestimmen"
    if re.match(r"(?i)^[a-z]{2,5}-[a-z0-9-]+$",name) and "-" in name:
        return "", "Release-Kürzel statt sicherem Filmtitel"
    if re.match(r"(?i)^[a-z]{2,6}-[a-z0-9-]+$",value):
        return "", "Kryptischer Name"
    if not value or len(value)<4 or value.lower() in BAD:
        return "", "Kein eindeutiger Filmtitel"
    return value[0].upper()+value[1:],warning

def disc_number(text):
    found=DISC.search(text)
    return int(found.group(1)) if found else None

def mkvmerge_path():
    path=shutil.which("mkvmerge.exe") or shutil.which("mkvmerge")
    if path:return path
    for p in (r"C:\Program Files\MKVToolNix\mkvmerge.exe",
              r"C:\Program Files (x86)\MKVToolNix\mkvmerge.exe"):
        if Path(p).is_file():return p
    return None

def normalized(s):
    return re.sub("[^a-z0-9]","",s.casefold())

def subtitle_parts(path):
    stem=path.stem
    if FORCED.search(stem):return None
    parts=re.split(r"[._ -]+",stem)
    lang=LANG.get(parts[-1].lower(),"und") if parts else "und"
    if parts and parts[-1].lower() in LANG:
        stem=stem[:-(len(parts[-1])+1)]
    if path.suffix.lower()==".idx":
        binary=path.with_suffix(".sub")
        if not binary.is_file():return None
    else:
        binary=None
    return stem,lang,binary

def subtitles_for(video, same_folder):
    matching=[]
    for p in video.parent.iterdir():
        if not p.is_file() or p.suffix.lower() not in SUBS:continue
        parsed=subtitle_parts(p)
        if not parsed:continue
        stem,lang,binary=parsed
        a,b=normalized(stem),normalized(video.stem)
        # Accept obvious filename variants; with a single movie in a folder
        # accept only generic subtitle names if video filename differs.
        fits=(a==b or (len(a)>=8 and a in b) or (len(b)>=8 and b in a))
        if len(same_folder)==1 and a in {"subtitle","subtitles","de","en","eng","ger"}:
            fits=True
        if fits:matching.append((p,lang,binary))
    return sorted(matching,key=lambda x:x[0].name.lower())

@dataclass
class Job:
    source:Path
    title:str
    target:Path|None
    status:str
    reason:str=""
    number:int|None=None
    subs:list=field(default_factory=list)

def scan(root,mux_available=True):
    root=root.resolve()
    if not root.is_dir() or not re.fullmatch(r"\d{4}",root.name):
        raise ValueError("Bitte einen Jahresordner wählen, z. B. 1981.")
    paths=[]
    for directory,dirs,files in os.walk(root,followlinks=False):
        dirs[:]=[d for d in dirs if d.lower() not in EXCLUDED and not (Path(directory)/d).is_symlink()]
        for f in files:
            p=Path(directory)/f
            if p.suffix.lower() in VIDEOS and not p.is_symlink():
                paths.append(p)
    paths.sort(key=lambda p:str(p).casefold())
    by_dir={}
    for p in paths:by_dir.setdefault(p.parent,[]).append(p)
    jobs=[]
    for p in paths:
        name,warning=clean_name(p.stem,root.name)
        number=disc_number(p.stem)
        parent,parentwarning=clean_name(p.parent.name,root.name) if p.parent!=root else ("","")
        if not name or (number and parent):
            title,reason=parent,parentwarning
        else:
            title,reason=name,warning
        if "Jahr " in warning:reason=warning
        if "Jahr " in parentwarning:reason=parentwarning
        if p.stem.lower().endswith(("-sample",".sample","_sample")):
            title,reason="","Sample: nicht automatisch verarbeiten"
        subs=subtitles_for(p,by_dir[p.parent])
        if not title or reason:
            jobs.append(Job(p,title,None,"Prüfen",reason or "Unbekannt",number,subs))
            continue
        result=title+(f" ({number})" if number else "")+(".mkv" if subs else p.suffix)
        target=root/result
        if subs and not mux_available:
            status,why="Prüfen","MKVToolNix/mkvmerge fehlt"
        elif target==p and not subs:
            status,why="Fertig","Bereits richtig benannt"
        else:
            status,why=("Muxen" if subs else "Umbenennen"),""
        jobs.append(Job(p,title,target,status,why,number,subs))
    targets={}
    for j in jobs:
        if j.target:
            key=str(j.target).casefold()
            targets[key]=targets.get(key,0)+1
    for j in jobs:
        if j.target and j.status in ("Muxen","Umbenennen"):
            if targets[str(j.target).casefold()]>1 or (j.target.exists() and j.target!=j.source):
                j.status="Prüfen"
                j.reason="Ziel existiert oder Name mehrfach vergeben"
    return jobs

def move_safely(src,dst):
    dst.parent.mkdir(parents=True,exist_ok=True)
    if dst.exists():raise FileExistsError(str(dst))
    shutil.move(str(src),str(dst))

def run_job(root,job,mkvmerge):
    if job.status not in ("Muxen","Umbenennen") or not job.target:
        return "Übersprungen"
    if job.target.exists() and job.target!=job.source:
        raise FileExistsError("Ziel existiert: "+str(job.target))
    if job.status=="Umbenennen":
        move_safely(job.source,job.target)
        return "Umbenannt/Verschoben"
    if not mkvmerge:raise RuntimeError("mkvmerge ist nicht installiert")
    temporary=root/(".EugiCleaner-"+str(time.time_ns())+"-partial.mkv")
    args=[mkvmerge,"-o",str(temporary),str(job.source)]
    for p,lang,binary in job.subs:
        args.extend(["--language","0:"+lang,"--default-track-flag","0:no",
                     "--forced-display-flag","0:no",str(p)])
    try:
        proc=subprocess.run(args,capture_output=True,text=True,errors="replace")
        if proc.returncode not in (0,1) or not temporary.exists() or temporary.stat().st_size<1024:
            raise RuntimeError("Mux fehlgeschlagen: "+(proc.stderr or proc.stdout)[-400:])
        verify=subprocess.run([mkvmerge,"-J",str(temporary)],capture_output=True,text=True,errors="replace")
        if verify.returncode!=0:
            raise RuntimeError("MKV konnte nicht identifiziert werden")
        info=json.loads(verify.stdout)
        count=sum(track.get("type")=="subtitles" for track in info.get("tracks",[]))
        if count<len(job.subs):
            raise RuntimeError("Untertitel fehlen in der erzeugten MKV")
        if job.target.exists():raise FileExistsError(str(job.target))
        temporary.rename(job.target)
        for src in [job.source]+[x for sub,lang,binary in job.subs for x in (sub,binary) if x]:
            backup=root/"_EugiCleaner_Backup"/src.relative_to(root)
            if backup.exists():continue
            try:move_safely(src,backup)
            except OSError:pass
        return "MKV erstellt; Originale soweit möglich im Backup"
    finally:
        if temporary.exists():temporary.unlink()

class App(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title("Eugi Movie Cleaner v0.1")
        self.geometry("1150x690")
        self.minsize(780,490)
        self.folder=None
        self.jobs=[]
        self.busy=False
        self.mkvmerge=mkvmerge_path()
        self.events=queue.Queue()
        self.build_gui()
        self.after(150,self.poll)

    def build_gui(self):
        top=ttk.Frame(self,padding=12)
        top.pack(fill="x")
        ttk.Label(top,text="Eugi Movie Cleaner",font=("Segoe UI",18,"bold")).pack(anchor="w")
        ttk.Label(top,text="Vorschau zuerst · keine Dateien werden gelöscht").pack(anchor="w")
        row=ttk.Frame(self,padding=(12,5))
        row.pack(fill="x")
        self.choose_button=ttk.Button(row,text="Jahresordner wählen",command=self.choose)
        self.choose_button.pack(side="left",padx=(0,7))
        self.scan_button=ttk.Button(row,text="Neu scannen",command=self.rescan,state="disabled")
        self.scan_button.pack(side="left",padx=(0,7))
        self.edit_button=ttk.Button(row,text="Markierten Titel korrigieren",command=self.edit,state="disabled")
        self.edit_button.pack(side="left")
        self.go_button=ttk.Button(row,text="Alle sicheren Änderungen ausführen",command=self.go,state="disabled")
        self.go_button.pack(side="right")
        self.summary=tk.StringVar(value="Bitte einen Jahresordner auswählen.")
        ttk.Label(self,textvariable=self.summary,padding=12).pack(fill="x")
        box=ttk.Frame(self,padding=(12,0,12,10))
        box.pack(fill="both",expand=True)
        columns=("status","source","target","subs","reason")
        self.tree=ttk.Treeview(box,columns=columns,show="headings",selectmode="browse")
        for c,label,width in [("status","Status",95),("source","Quelle",300),
                              ("target","Ziel",250),("subs","Subs",60),("reason","Hinweis",360)]:
            self.tree.heading(c,text=label)
            self.tree.column(c,width=width,minwidth=65)
        self.tree.pack(side="left",fill="both",expand=True)
        scroll=ttk.Scrollbar(box,orient="vertical",command=self.tree.yview)
        scroll.pack(side="right",fill="y")
        self.tree.configure(yscrollcommand=scroll.set)
        self.tree.bind("<<TreeviewSelect>>",lambda e:self.buttons())
        ttk.Label(self,text="Protokoll",padding=(12,0)).pack(anchor="w")
        self.log=tk.Text(self,height=7,state="disabled",wrap="word")
        self.log.pack(fill="x",padx=12,pady=(0,12))
        self.say("MKVToolNix: "+(self.mkvmerge or "nicht vorhanden; Muxing gesperrt"))

    def say(self,s):
        self.log.config(state="normal")
        self.log.insert("end",str(s)+"\n")
        self.log.see("end")
        self.log.config(state="disabled")

    def buttons(self):
        selected=bool(self.tree.selection())
        ready=any(x.status in ("Muxen","Umbenennen") for x in self.jobs)
        self.choose_button.config(state="disabled" if self.busy else "normal")
        self.scan_button.config(state="normal" if self.folder and not self.busy else "disabled")
        self.edit_button.config(state="normal" if selected and not self.busy else "disabled")
        self.go_button.config(state="normal" if ready and not self.busy else "disabled")

    def choose(self):
        chosen=filedialog.askdirectory(title="Jahresordner wie 1981 auswählen")
        if chosen:
            self.folder=Path(chosen)
            self.rescan()

    def rescan(self):
        if self.busy or not self.folder:return
        try:self.jobs=scan(self.folder,bool(self.mkvmerge))
        except Exception as e:
            messagebox.showerror("Fehler beim Scannen",str(e));return
        self.refresh()
        self.say("Scan: "+str(self.folder)+" — "+str(len(self.jobs))+" Videos")

    def refresh(self):
        for item in self.tree.get_children():self.tree.delete(item)
        for idx,j in enumerate(self.jobs):
            self.tree.insert("","end",iid=str(idx),values=(j.status,
                             str(j.source.relative_to(self.folder)),
                             j.target.name if j.target else "—",
                             len(j.subs),j.reason))
        counters={name:sum(j.status==name for j in self.jobs)
                  for name in ("Umbenennen","Muxen","Prüfen","Fertig","Erledigt","Fehler")}
        self.summary.set(str(self.folder)+"  |  "+"  ·  ".join(f"{k}: {v}" for k,v in counters.items()))
        self.buttons()

    def edit(self):
        if not self.tree.selection() or not self.folder:return
        j=self.jobs[int(self.tree.selection()[0])]
        title=simpledialog.askstring("Film identifizieren",j.source.name+"\n\nFilmtitel ohne Jahr:",
                                      initialvalue=j.title,parent=self)
        if title is None:return
        title=title.strip(" .")
        if not title or any(c in title for c in '<>:"/\\|?*'):
            messagebox.showerror("Ungültiger Titel","Unzulässige Zeichen im Titel.");return
        j.title=title
        j.target=self.folder/(title+(f" ({j.number})" if j.number else "")+
                              (".mkv" if j.subs else j.source.suffix))
        collision=(j.target.exists() and j.target!=j.source)
        collision|=any(x is not j and x.target is not None and
                       x.target.name.casefold()==j.target.name.casefold() for x in self.jobs)
        if collision:
            j.status,j.reason="Prüfen","Zielname bereits vergeben"
        elif j.subs and not self.mkvmerge:
            j.status,j.reason="Prüfen","MKVToolNix nicht gefunden"
        else:
            j.status,j.reason=("Muxen" if j.subs else "Umbenennen"),"Manuell freigegeben"
        self.refresh()

    def go(self):
        batch=[j for j in self.jobs if j.status in ("Muxen","Umbenennen")]
        if not batch or self.busy:return
        answer=messagebox.askyesno("Änderungen freigeben?",
            f"{len(batch)} Filme verarbeiten?\n\n"
            "Dateien werden im Jahresordner abgelegt. Originale gemuxter Videos "
            "und verwendete Subs landen im Backup. Nichts wird gelöscht.")
        if not answer:return
        self.busy=True;self.buttons()
        threading.Thread(target=self.work,args=(batch,),daemon=True).start()

    def work(self,batch):
        records=[]
        for j in batch:
            try:
                note=run_job(self.folder,j,self.mkvmerge)
                j.status,j.reason="Erledigt",note
                records.append({"source":str(j.source),"target":str(j.target),"result":"ok","note":note})
                self.events.put(("log","OK "+j.source.name+" -> "+j.target.name))
            except Exception as e:
                j.status,j.reason="Fehler",str(e)[:200]
                records.append({"source":str(j.source),"result":"error","note":str(e)})
                self.events.put(("log","FEHLER "+j.source.name+": "+str(e)))
            self.events.put(("refresh",None))
        try:
            logs=self.folder/"_EugiCleaner_Logs";logs.mkdir(exist_ok=True)
            file=logs/("run-"+time.strftime("%Y%m%d-%H%M%S")+".json")
            file.write_text(json.dumps(records,ensure_ascii=False,indent=2),encoding="utf-8")
            self.events.put(("log","Protokoll: "+str(file)))
        except Exception as e:
            self.events.put(("log","Protokollfehler: "+str(e)))
        self.events.put(("done",None))

    def poll(self):
        try:
            while True:
                event,data=self.events.get_nowait()
                if event=="log":self.say(data)
                elif event=="refresh":self.refresh()
                elif event=="done":
                    self.busy=False;self.refresh()
                    messagebox.showinfo("Fertig","Batch abgeschlossen. Bitte Ergebnisse prüfen.")
        except queue.Empty:pass
        self.after(150,self.poll)

if __name__=="__main__":
    App().mainloop()
