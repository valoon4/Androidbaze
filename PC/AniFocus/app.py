"""AniFocus v0.1 — cozy, low-maintenance Pomodoro for Master/Research/Exploring."""
from __future__ import annotations

import math
import queue
import threading
import webbrowser
import tkinter as tk
from tkinter import messagebox, simpledialog

from core import AREAS, FocusTimer, clock_label, load_state, save_state
from github_progress import SUPPORTED, SOURCE_PAGE, fetch_progress

W, H = 1120, 720
BG = "#f6e9e5"
INK = "#523b3d"
MUTED = "#927a78"
PINK = "#f3c3ce"
PASTELS = ("#f8c9d5", "#cbdcf0", "#dce8b6", "#f8ddb2",
           "#ded2ef", "#d2e9e3", "#f6d2bb", "#e9d7d5")


class AniFocus:
    def __init__(self, root: tk.Tk):
        self.root = root
        root.title("AniFocus v0.1")
        root.geometry(f"{W}x{H}")
        root.resizable(False, False)
        root.configure(bg=BG)
        self.state = load_state()
        self.timer = FocusTimer(self.state["minutes"])
        self.canvas = tk.Canvas(root, width=W, height=H, bg=BG, highlightthickness=0)
        self.canvas.pack()
        self.counter = 0
        self.time_item = None
        self.pages = {area: 0 for area in AREAS}
        self.github_cache = {}
        self.github_loading = set()
        self.github_errors = {}
        self.github_results = queue.Queue()
        self.render()
        self.maybe_refresh_github()
        root.after(160, self.heartbeat)

    def rounded(self, x1, y1, x2, y2, radius=18, fill="#ffffff", outline="", width=1, tags=()):
        r = min(radius, (x2-x1)/2, (y2-y1)/2)
        p = [x1+r,y1, x2-r,y1, x2,y1, x2,y1+r, x2,y2-r,
             x2,y2, x2-r,y2, x1+r,y2, x1,y2, x1,y2-r, x1,y1+r, x1,y1]
        return self.canvas.create_polygon(p, smooth=True, splinesteps=12,
                                           fill=fill, outline=outline, width=width, tags=tags)

    def label(self, x, y, text, size=15, color=INK, bold=False, anchor="center", tags=()):
        return self.canvas.create_text(x,y,text=text,fill=color,anchor=anchor,
                                       font=("Segoe UI",size,"bold" if bold else "normal"),tags=tags)

    def button(self, x1, y1, x2, y2, text, action, bg="#fffaf5",
               fg=INK, size=15, selected=False):
        self.counter += 1
        tag = f"hit{self.counter}"
        self.rounded(x1,y1,x2,y2,13,fill=bg,
                     outline="#d98d9d" if selected else "#e3cfc6",
                     width=2 if selected else 1,tags=(tag,))
        self.label((x1+x2)/2,(y1+y2)/2,text,size,fg,True,tags=(tag,))
        self.canvas.tag_bind(tag,"<Button-1>",lambda _event: action())
        self.canvas.tag_bind(tag,"<Enter>",lambda _event: self.canvas.configure(cursor="hand2"))
        self.canvas.tag_bind(tag,"<Leave>",lambda _event: self.canvas.configure(cursor=""))
        return tag

    def paw(self,x,y,s=1,color="#e3b4ac"):
        c=self.canvas
        c.create_oval(x-7*s,y-2*s,x+7*s,y+8*s,fill=color,outline="")
        for dx,dy in ((-9,-8),(-3,-13),(5,-13),(11,-8)):
            c.create_oval(x+(dx-3)*s,y+(dy-4)*s,x+(dx+3)*s,y+(dy+3)*s,fill=color,outline="")

    def blossom(self,x,y,r=12):
        c=self.canvas
        for a in range(5):
            theta=a*math.tau/5
            cx=x+math.cos(theta)*r*.65
            cy=y+math.sin(theta)*r*.65
            c.create_oval(cx-r*.48,cy-r*.48,cx+r*.48,cy+r*.48,fill="#f6bdc8",outline="")
        c.create_oval(x-3,y-3,x+3,y+3,fill="#f5d989",outline="")

    def cat(self,x,y):
        c=self.canvas
        # Sleeping cat peeking over the notebook, entirely decorative.
        c.create_oval(x-57,y-30,x+53,y+28,fill="#c5a39a",outline="#876961",width=2)
        c.create_polygon(x-46,y-11,x-43,y-55,x-16,y-33,fill="#c5a39a",outline="#876961",width=2)
        c.create_polygon(x+20,y-34,x+43,y-57,x+44,y-9,fill="#c5a39a",outline="#876961",width=2)
        c.create_polygon(x-39,y-27,x-37,y-44,x-26,y-32,fill="#eebdbb",outline="")
        c.create_polygon(x+30,y-31,x+39,y-45,x+38,y-25,fill="#eebdbb",outline="")
        c.create_arc(x-29,y-9,x-5,y+9,start=195,extent=155,style="arc",outline="#654d49",width=3)
        c.create_arc(x+4,y-9,x+28,y+9,start=195,extent=155,style="arc",outline="#654d49",width=3)
        c.create_polygon(x-4,y+8,x+4,y+8,x,y+13,fill="#bc7476",outline="")
        c.create_arc(x-10,y+9,x,y+18,start=190,extent=140,style="arc",outline="#654d49",width=2)
        c.create_arc(x,y+9,x+10,y+18,start=210,extent=140,style="arc",outline="#654d49",width=2)
        for side in (-1,1):
            c.create_line(x+side*20,y+13,x+side*48,y+10,fill="#876961",width=2)
            c.create_line(x+side*20,y+16,x+side*45,y+21,fill="#876961",width=2)
        c.create_oval(x-35,y+19,x-4,y+37,fill="#e8cbc4",outline="#876961",width=2)
        c.create_oval(x+4,y+19,x+35,y+37,fill="#e8cbc4",outline="#876961",width=2)

    def tomato(self,x,y):
        c=self.canvas
        c.create_oval(x-117,y-102,x+117,y+116,fill="#e6534b",outline="#b63f3d",width=5)
        c.create_oval(x-92,y-82,x-44,y+29,fill="#f98271",outline="")
        c.create_oval(x+66,y+33,x+87,y+72,fill="#de4643",outline="")
        c.create_polygon(x,y-99,x-53,y-115,x-30,y-130,x-5,y-116,
                         x+16,y-144,x+28,y-120,x+72,y-119,x+49,y-98,x+16,y-108,
                         fill="#73a65c",outline="#517845",width=2,smooth=True)
        c.create_line(x+2,y-105,x+11,y-143,fill="#537744",width=6,smooth=True)
        c.create_oval(x-77,y-42,x-53,y-17,fill="#ffab94",outline="")

    def persist(self):
        try:
            save_state(self.state)
        except OSError as exc:
            messagebox.showwarning("AniFocus",f"Einstellungen konnten nicht gespeichert werden:\n{exc}",parent=self.root)

    def change_area(self,area):
        self.state["area"]=area
        self.persist()
        self.render()
        self.maybe_refresh_github()

    def choose_module(self,module):
        self.state["selected"][self.state["area"]]=module
        self.persist()
        self.render()
        self.maybe_refresh_github()

    def add_module(self):
        area=self.state["area"]
        if len(self.state["modules"][area])>=16:
            messagebox.showinfo("AniFocus","Maximal 16 Unterbereiche pro Kategorie.",parent=self.root)
            return
        name=simpledialog.askstring("Unterbereich hinzufügen",f"Name für {area} (einmalig anlegen):",parent=self.root)
        if name is None:
            return
        name=" ".join(name.strip().split())[:32]
        if not name:
            return
        if name.casefold() in (item.casefold() for item in self.state["modules"][area]):
            messagebox.showinfo("AniFocus","Dieser Unterbereich existiert schon.",parent=self.root)
            return
        self.state["modules"][area].append(name)
        self.state["selected"][area]=name
        self.persist()
        self.pages[area]=(len(self.state["modules"][area])-1)//7
        self.render()
        self.maybe_refresh_github()

    def module_menu(self,event,module):
        area=self.state["area"]
        menu=tk.Menu(self.root,tearoff=0)
        menu.add_command(label="Umbenennen",command=lambda: self.rename_module(area,module))
        menu.add_command(label="Entfernen",command=lambda: self.delete_module(area,module))
        try:
            menu.tk_popup(event.x_root,event.y_root)
        finally:
            menu.grab_release()

    def rename_module(self,area,old):
        if old not in self.state["modules"][area]:
            return
        name=simpledialog.askstring("Unterbereich umbenennen","Neuer Name:",initialvalue=old,parent=self.root)
        if name is None:
            return
        name=" ".join(name.strip().split())[:32]
        if not name or any(n.casefold()==name.casefold() and n!=old for n in self.state["modules"][area]):
            return
        idx=self.state["modules"][area].index(old)
        self.state["modules"][area][idx]=name
        if self.state["selected"][area]==old:
            self.state["selected"][area]=name
        self.persist()
        self.render()

    def delete_module(self,area,name):
        if not messagebox.askyesno("Unterbereich entfernen",f"'{name}' wirklich entfernen?",parent=self.root):
            return
        if name in self.state["modules"][area]:
            self.state["modules"][area].remove(name)
        if self.state["selected"][area]==name:
            entries=self.state["modules"][area]
            self.state["selected"][area]=entries[0] if entries else None
        self.persist()
        self.render()

    def edit_progress(self):
        area=self.state["area"]
        current=self.state["progress"][area]
        value=simpledialog.askstring("Gesamtfortschritt",f"{area}: 0 bis 100 Prozent eingeben.\nLeer lassen = Angabe entfernen.",
                                     initialvalue="" if current is None else str(current),parent=self.root)
        if value is None:
            return
        value=value.strip()
        if not value:
            self.state["progress"][area]=None
        elif value.isdecimal() and 0<=int(value)<=100:
            self.state["progress"][area]=int(value)
        else:
            messagebox.showerror("AniFocus","Bitte eine ganze Zahl zwischen 0 und 100 eingeben.",parent=self.root)
            return
        self.persist()
        self.render()

    def edit_minutes(self):
        value=simpledialog.askinteger("Pomodoro-Dauer","Fokusdauer in Minuten (1–180):",
                                     initialvalue=self.state["minutes"],minvalue=1,maxvalue=180,parent=self.root)
        if value is None:
            return
        if self.timer.running and not messagebox.askyesno(
            "Timer ändern","Laufenden Timer stoppen und neue Dauer setzen?",parent=self.root):
            return
        self.state["minutes"]=value
        self.timer.reset(value)
        self.persist()
        self.render()

    def toggle_timer(self):
        if self.timer.running:
            self.timer.pause()
        else:
            self.timer.start()
        self.render()

    def reset_timer(self):
        self.timer.reset()
        self.render()


    def maybe_refresh_github(self, force=False):
        """Automatic first read on selecting OST/Playlists/Seasonvids; manual refresh."""
        name = self.state["selected"].get("Research") if self.state["area"] == "Research" else None
        if name not in SUPPORTED or name in self.github_loading:
            return
        if name in self.github_cache and not force:
            return
        self.github_loading.add(name)
        self.github_errors.pop(name, None)
        worker = threading.Thread(target=self._github_worker, args=(name,), daemon=True)
        worker.start()
        self.render()

    def _github_worker(self, name):
        try:
            result = fetch_progress(name)
            self.github_results.put((name, result, None))
        except Exception:
            self.github_results.put((name, None, "GitHub-Stand derzeit nicht abrufbar."))

    def github_detail(self):
        name = self.state["selected"].get("Research")
        record = self.github_cache.get(name)
        if record:
            messagebox.showinfo(f"{name} · GitHub", record["details"], parent=self.root)

    def github_source(self):
        webbrowser.open(SOURCE_PAGE)

    def page(self, delta):
        area = self.state["area"]
        max_page = max(0, (len(self.state["modules"][area]) - 1) // 7)
        self.pages[area] = max(0, min(max_page, self.pages[area] + delta))
        self.render()

    def heartbeat(self):
        changed = False
        while True:
            try:
                name, record, error = self.github_results.get_nowait()
            except queue.Empty:
                break
            self.github_loading.discard(name)
            if record:
                self.github_cache[name] = record
                self.github_errors.pop(name, None)
            else:
                self.github_errors[name] = error
            changed = True
        if changed:
            self.render()
        finished=self.timer.tick()
        if self.time_item is not None:
            self.canvas.itemconfigure(self.time_item,text=clock_label(self.timer.seconds_left()))
        if finished:
            self.root.bell()
            self.render()
            messagebox.showinfo("AniFocus","Fokuszeit beendet! 🍅",parent=self.root)
        self.root.after(160,self.heartbeat)


    def render(self):
        c = self.canvas
        c.delete("all")
        self.counter = 0

        # Cream paper, notebook binding and gentle card shadows.
        c.create_rectangle(0,0,W,H,fill=BG,outline="")
        self.rounded(42,30,1092,706,30,fill="#dfc4be")
        self.rounded(32,20,1080,695,29,fill="#e9d3cb")
        self.rounded(23,12,1071,684,29,fill="#fffaf3",outline="#dec2b9",width=2)

        for y in range(170,655,34):
            c.create_line(73,y,1038,y,fill="#f6ece8",width=1)
        c.create_line(76,174,76,660,fill="#f1d8d5",width=2)
        for y in range(80,658,54):
            c.create_oval(17,y-9,42,y+9,fill=BG,outline="#b69a91",width=2)
            c.create_arc(11,y-15,50,y+15,start=83,extent=207,
                         style="arc",outline="#ad9286",width=3)

        # Corner flowers and notebook brand.
        for x,y in ((1040,69),(1020,91),(1060,102)):
            self.blossom(x,y,10)
        self.label(99,51,"AniFocus",31,INK,True,"w")
        self.label(101,83,"Dein cozy Fokusplatz",12,MUTED,anchor="w")
        self.paw(286,55,.6,color="#e2b0ae")
        for i,area in enumerate(AREAS):
            x=395+i*203
            current=area==self.state["area"]
            self.button(x,39,x+190,99,area,
                        lambda name=area:self.change_area(name),
                        bg="#f8c9d5" if current else "#fff4ea",
                        selected=current,size=17)
        c.create_line(97,126,1018,126,fill="#e9d4cd",width=2)

        # Left: category list. Everything is optional; no task checkbox.
        self.rounded(96,161,542,619,24,fill="#ead7ce")
        self.rounded(92,155,537,611,24,fill="#fffdf9",outline="#efdad2")
        area=self.state["area"]
        names=self.state["modules"][area]
        active=self.state["selected"][area]
        self.label(120,187,area,27,INK,True,"w")
        self.label(120,220,"Fokusbereich auswählen",12,MUTED,anchor="w")
        self.paw(489,200,.52)
        c.create_line(122,238,504,238,fill="#f1e4dd",width=1)
        page = min(self.pages[area], max(0, (len(names)-1)//7))
        self.pages[area] = page
        visible = names[page*7:(page+1)*7]
        if not visible:
            self.label(126,300,"Noch keine Unterbereiche",15,MUTED,anchor="w")
        for i,name in enumerate(visible):
            x,y=120,248+i*45
            idx=page*7+i
            self.counter+=1
            tag=f"module{self.counter}"
            current=name==active
            self.rounded(x+3,y+3,509,y+41,12,fill="#efe4df",tags=(tag,))
            self.rounded(x,y,506,y+38,12,
                         fill="#f8d0d9" if current else PASTELS[idx%len(PASTELS)],
                         outline="#d890a6" if current else "#e6d2c8",
                         width=2 if current else 1,tags=(tag,))
            if current:
                self.rounded(x+7,y+7,x+13,y+31,3,fill="#bd758f",tags=(tag,))
            self.label(x+28,y+19,"▸" if current else "•",15,
                       "#a0767b",current,anchor="w",tags=(tag,))
            display = name if len(name)<=31 else name[:30]+"…"
            self.label(x+59,y+19,display,14,INK,current,anchor="w",tags=(tag,))
            c.tag_bind(tag,"<Button-1>",lambda _e,n=name:self.choose_module(n))
            c.tag_bind(tag,"<Button-3>",lambda e,n=name:self.module_menu(e,n))
            c.tag_bind(tag,"<Enter>",lambda _e:c.configure(cursor="hand2"))
            c.tag_bind(tag,"<Leave>",lambda _e:c.configure(cursor=""))

        self.button(120,573,374,605,"+ Unterbereich",self.add_module,
                    bg="#fff3ea",size=13)
        if len(names)>7:
            self.button(386,573,432,605,"‹",lambda:self.page(-1),size=17)
            self.button(461,573,507,605,"›",lambda:self.page(1),size=17)
            self.label(447,590,f"{page+1}/{(len(names)+6)//7}",9,MUTED)
        else:
            self.label(487,590,"⋯",18,MUTED)
        self.label(317,634,"Rechtsklick: umbenennen / entfernen",10,MUTED)

        # Right: one large tomato, no clutter or task management.
        self.rounded(568,161,1034,619,24,fill="#ead7ce")
        self.rounded(565,155,1030,610,24,fill="#fffdf9",outline="#efdad2")
        self.label(598,187,"Pomodoro",21,INK,True,"w")
        self.label(600,216,"Nur du und deine Fokuszeit",12,MUTED,anchor="w")
        self.cat(963,198)
        self.tomato(795,354)
        self.time_item=self.label(795,350,clock_label(self.timer.seconds_left()),
                                  42,"#fffaf4",True)
        self.label(795,429,active or area,13,"#fff7ee",True)

        self.button(611,485,789,531,"Ⅱ  Pause" if self.timer.running else "▶  Start",
                    self.toggle_timer,bg="#f6bccb",size=16,selected=True)
        self.button(799,485,905,531,"↺ Reset",self.reset_timer,bg="#f8eee6",size=14)
        self.button(913,485,991,531,"Dauer",self.edit_minutes,bg="#fff2e8",size=13)

        # Research: GitHub source shown read-only, not treated as an invented percentage.
        if area=="Research" and active in SUPPORTED:
            self.rounded(591,546,1005,604,13,fill="#fff2eb",outline="#f0ded6")
            record=self.github_cache.get(active)
            error=self.github_errors.get(active)
            if active in self.github_loading and not record:
                lines=["Lese aktuellen GitHub-Stand …"]
            elif record:
                lines=record["lines"]
            else:
                lines=[error or "GitHub-Stand noch nicht geladen."]
            self.label(611,560,(record["heading"] if record else "GitHub · "+active),
                       12,INK,True,anchor="w")
            for i,line in enumerate(lines[:2]):
                display=line if len(line)<=57 else line[:54]+"…"
                self.label(611,577+i*16,display,10,MUTED,anchor="w")
            self.button(928,550,994,572,"↻",lambda:self.maybe_refresh_github(True),
                        size=13,bg="#fff9f5")
            if record:
                self.button(928,577,994,600,"Info",self.github_detail,
                            size=11,bg="#fff9f5")
        else:
            self.label(796,571,"🍅  Einmal starten. Einfach fokussieren.",12,MUTED)
        # Per-area manual progress is independent of sourced Research status.
        self.rounded(94,650,1032,678,14,fill="#f5e5e1")
        self.label(112,664,f"{area} · Eigener Fortschritt",12,INK,True,anchor="w")
        value=self.state["progress"][area]
        self.rounded(430,656,856,671,7,fill="#ead5d2")
        if value is not None and value>0:
            self.rounded(431,657,431+423*value/100,670,6,fill="#e5a3b7")
        self.label(873,664,"–" if value is None else f"{value} %",12,INK,True,anchor="w")
        self.button(950,651,1023,676,"Setzen",self.edit_progress,bg="#fff3eb",size=11)
        self.label(1015,698,"v0.1",10,MUTED)

def main():
    root=tk.Tk()
    AniFocus(root)
    root.mainloop()

if __name__=="__main__":
    main()
