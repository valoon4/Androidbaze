"""AniFocus v0.0 — cozy offline Pomodoro for Master, Research and Exploring."""
from __future__ import annotations

import math
import tkinter as tk
from tkinter import messagebox, simpledialog

from core import AREAS, FocusTimer, clock_label, load_state, save_state

W, H = 1000, 690
BG = "#f4e8e4"
INK = "#594641"
MUTED = "#977f79"
PINK = "#f3c3ce"
PASTELS = ("#f8c9d5", "#cbdcf0", "#dce8b6", "#f8ddb2",
           "#ded2ef", "#d2e9e3", "#f6d2bb", "#e9d7d5")


class AniFocus:
    def __init__(self, root: tk.Tk):
        self.root = root
        root.title("AniFocus v0.0")
        root.geometry(f"{W}x{H}")
        root.resizable(False, False)
        root.configure(bg=BG)
        self.state = load_state()
        self.timer = FocusTimer(self.state["minutes"])
        self.canvas = tk.Canvas(root, width=W, height=H, bg=BG, highlightthickness=0)
        self.canvas.pack()
        self.counter = 0
        self.time_item = None
        self.render()
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

    def choose_module(self,module):
        self.state["selected"][self.state["area"]]=module
        self.persist()
        self.render()

    def add_module(self):
        area=self.state["area"]
        if len(self.state["modules"][area])>=8:
            messagebox.showinfo("AniFocus","Maximal 8 Unterbereiche pro Kategorie.",parent=self.root)
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
        self.render()

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

    def heartbeat(self):
        finished=self.timer.tick()
        if self.time_item is not None:
            self.canvas.itemconfigure(self.time_item,text=clock_label(self.timer.seconds_left()))
        if finished:
            self.root.bell()
            self.render()
            messagebox.showinfo("AniFocus","Fokuszeit beendet! 🍅",parent=self.root)
        self.root.after(160,self.heartbeat)

    def render(self):
        c=self.canvas
        c.delete("all")
        self.counter=0
        c.create_rectangle(0,0,W,H,fill=BG,outline="")
        # Notebook paper, spiral binding and faint ruled lines.
        self.rounded(32,24,970,661,28,fill="#e2c6bb")
        self.rounded(25,16,961,650,28,fill="#fff9f0",outline="#e4c7bc",width=2)
        for y in range(172,612,35):
            c.create_line(64,y,936,y,fill="#f7eae5",width=1)
        c.create_line(79,166,79,614,fill="#f4d7d2",width=2)
        for y in range(76,619,51):
            c.create_oval(19,y-7,40,y+7,fill=BG,outline="#b69a91",width=2)
            c.create_arc(13,y-13,47,y+13,start=80,extent=205,style="arc",outline="#a48d81",width=3)
        for x,y in ((911,55),(928,78),(893,94)):
            self.blossom(x,y,12)
        self.paw(270,62,.76)
        self.label(105,57,"AniFocus",30,bold=True,anchor="w")
        self.label(108,90,"Fokus ohne To-dos",13,color=MUTED,anchor="w")
        for i,area in enumerate(AREAS):
            x=335+i*195
            selected=area==self.state["area"]
            self.button(x,47,x+181,101,area,lambda a=area:self.change_area(a),
                        bg="#f7c7d2" if selected else "#fff4eb",size=17,selected=selected)

        area=self.state["area"]
        names=self.state["modules"][area]
        active=self.state["selected"][area]
        self.label(106,200,area,29,bold=True,anchor="w")
        self.paw(400,196,.65)
        self.label(107,230,"Unterbereich wählen",13,MUTED,anchor="w")
        # 2 columns of light, tactile notebook labels, maximum eight per area.
        for i,name in enumerate(names):
            x=100+(i%2)*178
            y=258+(i//2)*65
            self.counter+=1
            tag=f"module{self.counter}"
            selected=(active==name)
            bg="#efb8ca" if selected else PASTELS[i%len(PASTELS)]
            self.rounded(x,y,x+161,y+54,13,fill=bg,
                         outline="#ce8099" if selected else "#e3c8c0",
                         width=3 if selected else 1,tags=(tag,))
            self.label(x+14,y+27,"▸" if selected else "•",14,"#8f6f65",anchor="w",tags=(tag,))
            display=name if len(name)<=16 else name[:15]+"…"
            self.label(x+35,y+27,display,15,INK,True,anchor="w",tags=(tag,))
            c.tag_bind(tag,"<Button-1>",lambda _e,n=name:self.choose_module(n))
            c.tag_bind(tag,"<Button-3>",lambda e,n=name:self.module_menu(e,n))
            c.tag_bind(tag,"<Enter>",lambda _e:c.configure(cursor="hand2"))
            c.tag_bind(tag,"<Leave>",lambda _e:c.configure(cursor=""))
        if not names:
            self.label(108,306,"Keine Unterbereiche angelegt.",15,MUTED,anchor="w")
            self.label(108,334,"Der Timer funktioniert trotzdem.",13,MUTED,anchor="w")

        self.button(103,543,410,588,"+ Unterbereich anlegen",self.add_module,
                    bg="#fff5ea",size=14)
        self.label(255,606,"Rechtsklick auf einen Bereich: bearbeiten",10,MUTED)

        self.cat(838,181)
        self.tomato(706,349)
        self.time_item=self.label(706,346,clock_label(self.timer.seconds_left()),42,"#fff8ed",True)
        context=active or area
        self.label(705,435,context,15,"#fff4e6",True)
        self.button(580,477,744,530,"Pause" if self.timer.running else "▶  Start",
                    self.toggle_timer,bg="#f3b5c1",size=17,selected=True)
        self.button(755,477,846,530,"↺ Reset",self.reset_timer,bg="#f9e7db",size=14)
        self.button(851,477,929,530,"Dauer",self.edit_minutes,bg="#fff1e7",size=13)

        self.label(510,566,f"{area} · Gesamtfortschritt",14,INK,True,anchor="w")
        value=self.state["progress"][area]
        self.rounded(510,592,788,611,9,fill="#f2e2df",outline="#e9d3cd")
        if value is not None and value>0:
            self.rounded(512,594,512+274*value/100,609,7,fill="#e9a5b6")
        self.label(800,601,"—" if value is None else f"{value} %",14,INK,True,anchor="w")
        self.button(864,579,932,622,"Setzen",self.edit_progress,bg="#fbe9e4",size=12)
        self.label(901,643,"v0.0",11,MUTED)

def main():
    root=tk.Tk()
    AniFocus(root)
    root.mainloop()

if __name__=="__main__":
    main()
