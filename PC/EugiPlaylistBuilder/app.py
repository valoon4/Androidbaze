"""Eugi Playlist Builder: one Unsorted + pasted Markdown + one M3U8."""
from __future__ import annotations

import tkinter as tk
from pathlib import Path
from tkinter import filedialog, messagebox, ttk

from research_table import stem_from_track
from simple_builder import Builder, LEVELS, source_quarter_from_filename


class App(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title("Eugi Playlist Builder · Research → M3U8")
        self.geometry("1190x810")
        self.minsize(900, 620)
        self.builder = Builder()
        self.level = tk.StringVar(value="Ultra")
        self.period = tk.StringVar(value="Ganzes Jahr")
        self.edit_level = tk.StringVar(value="Best")
        self.edit_quarter = tk.StringVar(value="Q1")
        self.search = tk.StringVar()
        self.file_info = tk.StringVar(value="Noch keine Unsorted geladen.")
        self.stats = tk.StringVar(value="0 Songs geladen")
        self.status = tk.StringVar(value="Bereit · Die Audiodateien werden nie geändert.")
        self._build_ui()

    def _build_ui(self):
        top = ttk.Frame(self, padding=(12, 10))
        top.pack(fill="x")
        ttk.Label(top, text="Eugi Playlist Builder", font=("Segoe UI", 19, "bold")).pack(anchor="w")
        ttk.Label(
            top, text="Einmal Unsorted laden, Research-Tabelle reinkopieren, eine Playlist als UTF-8 M3U8 exportieren."
        ).pack(anchor="w", pady=(2, 9))
        ttk.Button(top, text="1  Unsorted laden", command=self.load_source).pack(anchor="w")
        ttk.Label(top, textvariable=self.file_info, foreground="#666").pack(anchor="w", pady=(5, 0))

        research = ttk.LabelFrame(
            self, text="2  Research-Tabelle einfügen  |  Titel | Jahr/Quartal | Best | Super | Ultra | Mega |",
            padding=8
        )
        research.pack(fill="x", padx=12, pady=(0, 6))
        textbox = ttk.Frame(research)
        textbox.pack(fill="x")
        self.paste = tk.Text(textbox, height=6, wrap="none", font=("Consolas", 10), undo=True)
        self.paste.pack(side="left", fill="x", expand=True)
        scrollbar = ttk.Scrollbar(textbox, orient="vertical", command=self.paste.yview)
        scrollbar.pack(side="right", fill="y")
        self.paste.configure(yscrollcommand=scrollbar.set)
        apply_row = ttk.Frame(research)
        apply_row.pack(fill="x", pady=(7, 0))
        ttk.Button(apply_row, text="Tabelle zuordnen", command=self.apply_table).pack(side="left")
        ttk.Label(
            apply_row,
            text="0/0/0/0 = Rest  ·  sonst Best  ·  Super/Ultra/Mega sind Verfeinerungen",
            foreground="#555"
        ).pack(side="left", padx=12)

        exports = ttk.LabelFrame(self, text="3  Genau EINE Playlist exportieren", padding=9)
        exports.pack(fill="x", padx=12, pady=(0, 7))
        ttk.Label(exports, text="Stufe:").pack(side="left")
        ttk.Combobox(exports, textvariable=self.level, values=LEVELS, state="readonly", width=9).pack(
            side="left", padx=(6, 18))
        ttk.Label(exports, text="Zeitraum:").pack(side="left")
        ttk.Combobox(
            exports, textvariable=self.period,
            values=("Ganzes Jahr", "Q1", "Q2", "Q3", "Q4"),
            state="readonly", width=13
        ).pack(side="left", padx=(6, 18))
        ttk.Button(exports, text="M3U8 exportieren", command=self.export).pack(side="left")
        ttk.Label(exports, text="Originalpfade und Song-Metadaten bleiben erhalten.", foreground="#555").pack(
            side="left", padx=12)

        edit = ttk.Frame(self, padding=(12, 0, 12, 4))
        edit.pack(fill="x")
        ttk.Label(edit, text="Suche:").pack(side="left")
        ttk.Entry(edit, textvariable=self.search, width=27).pack(side="left", padx=(5, 10))
        self.search.trace_add("write", lambda *_: self.refresh())
        ttk.Label(edit, text="Markierte →").pack(side="left", padx=(6, 4))
        ttk.Combobox(edit, textvariable=self.edit_level, values=LEVELS, state="readonly", width=8).pack(
            side="left")
        ttk.Button(edit, text="✓", width=3, command=lambda: self.bulk(True)).pack(side="left", padx=(4, 1))
        ttk.Button(edit, text="✕", width=3, command=lambda: self.bulk(False)).pack(side="left", padx=(1, 15))
        ttk.Combobox(
            edit, textvariable=self.edit_quarter,
            values=("Q1", "Q2", "Q3", "Q4"), state="readonly", width=4
        ).pack(side="left")
        ttk.Button(edit, text="Quartal setzen", command=self.assign_quarter).pack(side="left", padx=5)
        ttk.Label(edit, text="Häkchen in der Tabelle anklicken", foreground="#666").pack(side="right")

        table_frame = ttk.Frame(self, padding=(12, 2, 12, 2))
        table_frame.pack(fill="both", expand=True)
        columns = ("nr", "file", "title", "quarter", "Rest", "Best", "Super", "Ultra", "Mega", "match")
        self.table = ttk.Treeview(table_frame, columns=columns, show="headings", selectmode="extended")
        headings = {
            "nr": "#", "file": "Musik-Dateiname", "title": "Interpret – Song",
            "quarter": "Q", "Rest": "Rest", "Best": "Best",
            "Super": "Super", "Ultra": "Ultra", "Mega": "Mega", "match": "Research"
        }
        widths = {
            "nr": 42, "file": 275, "title": 315, "quarter": 45,
            "Rest": 50, "Best": 50, "Super": 55, "Ultra": 50, "Mega": 50, "match": 89
        }
        for column in columns:
            self.table.heading(column, text=headings[column])
            self.table.column(column, width=widths[column], stretch=column in ("file", "title"),
                              anchor="w" if column in ("file", "title", "match") else "center")
        scrolly = ttk.Scrollbar(table_frame, orient="vertical", command=self.table.yview)
        scrollx = ttk.Scrollbar(table_frame, orient="horizontal", command=self.table.xview)
        self.table.configure(yscrollcommand=scrolly.set, xscrollcommand=scrollx.set)
        self.table.grid(row=0, column=0, sticky="nsew")
        scrolly.grid(row=0, column=1, sticky="ns")
        scrollx.grid(row=1, column=0, sticky="ew")
        table_frame.rowconfigure(0, weight=1)
        table_frame.columnconfigure(0, weight=1)
        self.table.bind("<Button-1>", self.click_cell)

        footer = ttk.Frame(self, padding=(12, 4, 12, 10))
        footer.pack(fill="x")
        ttk.Label(footer, textvariable=self.stats, font=("Segoe UI", 10, "bold")).pack(anchor="w")
        ttk.Label(footer, textvariable=self.status, foreground="#555", wraplength=1150).pack(
            anchor="w", pady=(4, 0))

    def load_source(self):
        filename = filedialog.askopenfilename(
            title="Unsorted M3U8 auswählen",
            filetypes=[("Playlist-Dateien", "*.m3u8 *.m3u"), ("Alle Dateien", "*.*")]
        )
        if not filename:
            return
        try:
            self.builder.load(filename)
        except Exception as exc:
            messagebox.showerror("Unsorted-Fehler", str(exc))
            return
        self.paste.delete("1.0", "end")
        self.search.set("")
        self.file_info.set(f"Unsorted: {filename}")
        q = source_quarter_from_filename(filename)
        self.period.set(f"Q{q}" if q else "Ganzes Jahr")
        self.status.set(f"{len(self.builder.source)} Songs geladen. Alle vorerst Best; nur explizite 0/0/0/0 werden Rest.")
        self.refresh()

    def apply_table(self):
        try:
            report = self.builder.apply(self.paste.get("1.0", "end"))
        except Exception as exc:
            messagebox.showerror("Research-Fehler", str(exc))
            return
        self.refresh()
        rest_auto = len(self.builder.source) - len(self.builder.matched)
        self.status.set(
            f"{len(self.builder.matched)} Research-Matches. "
            f"{rest_auto} nicht in der Tabelle → sicherheitshalber Best. "
            f"{len(report.missing)} fehlen; {len(report.ambiguous)} mehrdeutig."
        )
        if report.has_issues:
            self.show_report(report.text(len(self.builder.source)))
        else:
            messagebox.showinfo("Research übernommen", f"{len(self.builder.matched)} Songs eindeutig zugeordnet.")

    @staticmethod
    def mark(value):
        return "☑" if value else "☐"

    def refresh(self):
        old_selection = set(self.table.selection())
        self.table.delete(*self.table.get_children())
        query = self.search.get().casefold().strip()
        for idx, song in enumerate(self.builder.source):
            stem = stem_from_track(song)
            if query and query not in stem.casefold() and query not in song.title.casefold():
                continue
            flags = self.builder.flags[idx]
            q = self.builder.quarters[idx]
            row = (idx + 1, stem, song.title, f"Q{q}" if q else "?",
                   self.mark(not any(flags)), *(self.mark(bit) for bit in flags),
                   "✓" if idx in self.builder.matched else "Best auto")
            self.table.insert("", "end", iid=str(idx), values=row)
        valid = [x for x in old_selection if self.table.exists(x)]
        if valid:
            self.table.selection_set(valid)
        c = self.builder.counts()
        self.stats.set(
            f"Gesamt: {len(self.builder.source)}  |  Rest: {c[0]}  ·  Best: {c[1]}  ·  "
            f"Super: {c[2]}  ·  Ultra: {c[3]}  ·  Mega: {c[4]}  |  "
            f"Quartal unbekannt: {self.builder.unassigned_quarter_count()}"
        )

    def click_cell(self, event):
        if self.table.identify_region(event.x, event.y) != "cell":
            return
        col = self.table.identify_column(event.x)
        row = self.table.identify_row(event.y)
        if not row or col not in ("#5", "#6", "#7", "#8", "#9"):
            return
        level = LEVELS[int(col[1:]) - 5]
        idx = int(row)
        current = not any(self.builder.flags[idx]) if level == "Rest" else bool(
            self.builder.flags[idx][LEVELS.index(level) - 1])
        self.builder.set_flag(idx, level, not current)
        self.refresh()
        return "break"

    def bulk(self, enabled):
        selected = self.table.selection()
        if not selected:
            messagebox.showinfo("Markierung fehlt", "Bitte erst Songs in der Tabelle markieren.")
            return
        for i in selected:
            self.builder.set_flag(int(i), self.edit_level.get(), enabled)
        self.refresh()
        self.status.set(f"{len(selected)} Songs: {self.edit_level.get()} {'gesetzt' if enabled else 'entfernt'}.")

    def assign_quarter(self):
        selected = self.table.selection()
        if not selected:
            messagebox.showinfo("Markierung fehlt", "Bitte erst Songs in der Tabelle markieren.")
            return
        q = int(self.edit_quarter.get()[-1])
        for row in selected:
            self.builder.set_quarter(int(row), q)
        self.refresh()
        self.status.set(f"{len(selected)} Songs explizit Q{q} zugeordnet.")

    def export(self):
        if not self.builder.source:
            messagebox.showwarning("Unsorted fehlt", "Bitte erst Unsorted laden.")
            return
        q = None if self.period.get() == "Ganzes Jahr" else int(self.period.get()[-1])
        if q and self.builder.unassigned_quarter_count():
            unknown = self.builder.unassigned_quarter_count()
            if not messagebox.askyesno(
                "Quartal noch unvollständig",
                f"{unknown} Songs im Unsorted-Pool haben noch keine Quartalszuordnung.\n\n"
                f"Diese können keiner einzelnen Season sicher zugeordnet werden und bleiben "
                f"bei diesem Q{q}-Export außen vor.\n\nTrotzdem exportieren?",
            ):
                return
        level = self.level.get()
        if not self.builder.rows(level, q):
            messagebox.showinfo("Leere Playlist", f"Keine Songs für {level} / {self.period.get()} vorhanden.")
            return
        target = filedialog.asksaveasfilename(
            title="Eine M3U8 exportieren",
            initialfile=self.builder.default_filename(level, q),
            defaultextension=".m3u8",
            filetypes=[("UTF-8 Playlist", "*.m3u8")]
        )
        if not target:
            return
        try:
            output = self.builder.export_one(target, level, q)
        except Exception as exc:
            messagebox.showerror("Exportfehler", str(exc))
            return
        count = len(self.builder.rows(level, q))
        self.status.set(f"{count} Songs → {output.name} · UTF-8 M3U8. Unsorted unverändert.")
        messagebox.showinfo("Export fertig", f"{count} Songs in genau eine Playlist geschrieben:\n{output}")

    def show_report(self, message):
        dialog = tk.Toplevel(self)
        dialog.title("Research-Prüfbericht")
        dialog.geometry("850x480")
        text = tk.Text(dialog, font=("Consolas", 10), wrap="word")
        text.insert("1.0", message)
        text.configure(state="disabled")
        text.pack(fill="both", expand=True, padx=9, pady=9)
        ttk.Button(dialog, text="Schließen", command=dialog.destroy).pack(pady=(0, 8))


if __name__ == "__main__":
    App().mainloop()
