"""Eugi Playlist Builder — offline Windows GUI, standard library only."""
from __future__ import annotations

import os
import sys
import tkinter as tk
from pathlib import Path
from tkinter import filedialog, messagebox, ttk

from playlist_model import LEVELS, PlaylistProject


class App(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title('Eugi Playlist Builder · Rest / Best / Super / Ultra / Mega')
        self.geometry('1220x790')
        self.minsize(960, 600)
        self.project = PlaylistProject()
        self.filter_text = tk.StringVar()
        self.level_select = tk.StringVar(value='Best')
        self.file_label = tk.StringVar(value='Noch keine Unsorted geladen')
        self.stats_label = tk.StringVar(value='0 Songs geladen')
        self.status = tk.StringVar(value='Bereit · Alles offline; Audiodateien bleiben unangetastet.')
        self.current_indices: list[int] = []
        self._build_ui()

    def _build_ui(self):
        top = ttk.Frame(self, padding=14)
        top.pack(fill='x')
        ttk.Label(top, text='Eugi Playlist Builder', font=('Segoe UI', 19, 'bold')).pack(anchor='w')
        ttk.Label(top, text='Häkchen = Song gehört zur Playlist. Ein Song darf in mehreren Kategorien sein.').pack(anchor='w', pady=(2, 10))

        actions = ttk.Frame(top)
        actions.pack(fill='x')
        ttk.Button(actions, text='1  Unsorted laden', command=self.load_unsorted).pack(side='left', padx=(0, 8))
        ttk.Button(actions, text='Projekt öffnen', command=self.open_project).pack(side='left', padx=(0, 8))
        ttk.Button(actions, text='Projekt speichern', command=self.save_project).pack(side='left', padx=(0, 8))
        ttk.Button(actions, text='3  Alle M3U8 exportieren', command=self.export_all).pack(side='right')
        ttk.Label(top, textvariable=self.file_label, foreground='#666').pack(anchor='w', pady=(9, 0))

        importing = ttk.LabelFrame(self, text='2  Bestehende Playlists importieren (gleiche Dateipfade markieren)', padding=10)
        importing.pack(fill='x', padx=14, pady=(0, 8))
        for level in LEVELS:
            ttk.Button(importing, text=f'{level} laden', command=lambda lev=level: self.import_level(lev)).pack(side='left', padx=(0, 8))
        ttk.Label(importing, text='Best, Super, Ultra und Mega sind unabhängig voneinander.').pack(side='left', padx=8)

        edit = ttk.Frame(self, padding=(14, 3))
        edit.pack(fill='x')
        ttk.Label(edit, text='Suche:').pack(side='left')
        search = ttk.Entry(edit, textvariable=self.filter_text, width=35)
        search.pack(side='left', padx=(5, 10))
        self.filter_text.trace_add('write', lambda *_: self.populate())
        ttk.Label(edit, text='Markierte Songs →').pack(side='left', padx=(12, 6))
        ttk.Combobox(edit, textvariable=self.level_select, values=LEVELS, width=8, state='readonly').pack(side='left')
        ttk.Button(edit, text='Häkchen setzen', command=lambda: self.set_selected(True)).pack(side='left', padx=(8, 3))
        ttk.Button(edit, text='Häkchen entfernen', command=lambda: self.set_selected(False)).pack(side='left', padx=3)
        ttk.Label(edit, text='Tipp: Checkbox anklicken!', foreground='#666').pack(side='right')

        table_frame = ttk.Frame(self, padding=(14, 8))
        table_frame.pack(fill='both', expand=True)
        cols = ('number', 'song', 'filepath', 'Rest', *LEVELS)
        self.table = ttk.Treeview(table_frame, columns=cols, show='headings', selectmode='extended')
        labels = {'number': '#', 'song': 'Interpret – Song', 'filepath': 'M3U-Dateipfad',
                  'Rest': 'Rest (auto)', 'Best': 'Best', 'Super': 'Super',
                  'Ultra': 'Ultra', 'Mega': 'Mega'}
        widths = {'number': 50, 'song': 280, 'filepath': 420, 'Rest': 75,
                  'Best': 75, 'Super': 80, 'Ultra': 75, 'Mega': 75}
        for col in cols:
            self.table.heading(col, text=labels[col])
            self.table.column(col, width=widths[col], stretch=col in ('song', 'filepath'),
                              anchor='center' if col not in ('song', 'filepath') else 'w')
        scrolly = ttk.Scrollbar(table_frame, orient='vertical', command=self.table.yview)
        scrollx = ttk.Scrollbar(table_frame, orient='horizontal', command=self.table.xview)
        self.table.configure(yscrollcommand=scrolly.set, xscrollcommand=scrollx.set)
        self.table.grid(row=0, column=0, sticky='nsew')
        scrolly.grid(row=0, column=1, sticky='ns')
        scrollx.grid(row=1, column=0, sticky='ew')
        table_frame.rowconfigure(0, weight=1)
        table_frame.columnconfigure(0, weight=1)
        self.table.bind('<Button-1>', self.click_cell)
        self.table.bind('<space>', self.space_toggle)

        bottom = ttk.Frame(self, padding=(14, 2, 14, 12))
        bottom.pack(fill='x')
        ttk.Label(bottom, textvariable=self.stats_label, font=('Segoe UI', 10, 'bold')).pack(anchor='w')
        ttk.Label(bottom, textvariable=self.status, foreground='#666', wraplength=1160).pack(anchor='w', pady=(5, 0))

    def load_unsorted(self):
        file = filedialog.askopenfilename(title='Unsorted M3U auswählen', filetypes=[('M3U8 / M3U Playlists', '*.m3u8 *.m3u'), ('Alle Dateien', '*.*')])
        if not file:
            return
        try:
            if self.project.source and not messagebox.askyesno('Neues Projekt?', 'Das lädt eine neue Unsorted und löscht die bisherigen Playlist-Zuordnungen. Fortfahren?'):
                return
            self.project.load_unsorted(file)
            self.file_label.set(f'Unsorted: {file}')
            self.populate()
            self.status.set(f'{len(self.project.source)} Songs geladen. Jetzt Playlists importieren oder Checkboxen anklicken.')
        except Exception as ex:
            messagebox.showerror('Importfehler', str(ex))

    def import_level(self, level):
        if not self.project.source:
            messagebox.showwarning('Unsorted fehlt', 'Bitte zuerst Unsorted laden.')
            return
        file = filedialog.askopenfilename(title=f'{level}-Playlist importieren', filetypes=[('M3U8 / M3U Playlists', '*.m3u8 *.m3u'), ('Alle Dateien', '*.*')])
        if not file:
            return
        if any(flags[LEVELS.index(level)] for flags in self.project.flags) and not messagebox.askyesno('Kategorie ersetzen?', f'Die bisherigen {level}-Häkchen werden durch diese Datei ersetzt. Fortfahren?'):
            return
        try:
            report = self.project.import_tier(file, level)
            self.populate()
            self.status.set(f'{level}: {report.matched}/{report.source_count} Matches · {len(report.missing)} fehlend · {len(report.ambiguous)} mehrdeutig')
            if report.missing or report.ambiguous or report.warnings:
                self.show_report(report.description())
            else:
                messagebox.showinfo('Import fertig', f'{level}: {report.matched} Songs anhand der Datei-URIs zugeordnet.')
        except Exception as ex:
            messagebox.showerror('Importfehler', str(ex))

    def show_report(self, report):
        dialog = tk.Toplevel(self)
        dialog.title('Import-Prüfbericht')
        dialog.geometry('850x450')
        body = tk.Text(dialog, wrap='none', font=('Consolas', 10))
        body.insert('1.0', report)
        body.configure(state='disabled')
        body.pack(fill='both', expand=True, padx=10, pady=10)
        ttk.Button(dialog, text='Schließen', command=dialog.destroy).pack(pady=(0, 10))

    @staticmethod
    def mark(checked):
        """Checkbox-like marker; click on cell to toggle."""
        return '☑' if checked else '☐'

    def populate(self):
        query = self.filter_text.get().casefold().strip()
        selected = set(self.table.selection())
        self.table.delete(*self.table.get_children())
        self.current_indices = []
        for i, (track, flags) in enumerate(zip(self.project.source, self.project.flags)):
            if query and query not in track.title.casefold() and query not in track.uri.casefold():
                continue
            self.current_indices.append(i)
            self.table.insert('', 'end', iid=str(i), values=(i + 1, track.title, track.uri,
                                   self.mark(not any(flags)), *(self.mark(flag) for flag in flags)))
        self.table.selection_set([idx for idx in selected if self.table.exists(idx)])
        c = self.project.counts()
        self.stats_label.set(f'Gesamt: {len(self.project.source)}  |  Rest: {c[0]}  ·  Best: {c[1]}  ·  Super: {c[2]}  ·  Ultra: {c[3]}  ·  Mega: {c[4]}')

    def click_cell(self, event):
        if self.table.identify_region(event.x, event.y) != 'cell':
            return
        column = self.table.identify_column(event.x)
        row = self.table.identify_row(event.y)
        if row and column in ('#5', '#6', '#7', '#8'):
            level = LEVELS[int(column[1:]) - 5]
            self.project.toggle(int(row), level)
            self._refresh_row(int(row))
            return 'break'

    def space_toggle(self, _event):
        selected = self.table.selection()
        if selected:
            for row in selected:
                self.project.toggle(int(row), self.level_select.get())
            self.populate()
            return 'break'

    def _refresh_row(self, idx):
        track, flags = self.project.source[idx], self.project.flags[idx]
        self.table.item(str(idx), values=(idx + 1, track.title, track.uri,
                                              self.mark(not any(flags)), *(self.mark(flag) for flag in flags)))
        c = self.project.counts()
        self.stats_label.set(f'Gesamt: {len(self.project.source)}  |  Rest: {c[0]}  ·  Best: {c[1]}  ·  Super: {c[2]}  ·  Ultra: {c[3]}  ·  Mega: {c[4]}')

    def set_selected(self, value):
        selected = self.table.selection()
        if not selected:
            messagebox.showinfo('Nichts markiert', 'Bitte zuerst einen oder mehrere Songs markieren.')
            return
        for row in selected:
            self.project.set_flag(int(row), self.level_select.get(), value)
        self.populate()
        state = 'ausgewählt' if value else 'abgewählt'
        self.status.set(f'{len(selected)} Song(s): {self.level_select.get()} {state}.')

    def export_all(self):
        if not self.project.source:
            messagebox.showwarning('Unsorted fehlt', 'Bitte zuerst Unsorted laden.')
            return
        directory = filedialog.askdirectory(title='Ausgabeordner auswählen')
        if not directory:
            return
        try:
            stem = Path(self.project.source_path).stem.replace('Unsorted', '').strip(' _-') or 'Playlist'
            files = self.project.export(directory, stem)
            self.status.set(f'5 M3U8-Playlists + Zuordnungs-CSV exportiert nach {directory}')
            messagebox.showinfo('Export fertig!', f'5 M3U8-Playlists gespeichert:\n\n' + '\n'.join(p.name for p in files) + '\n\nPlus Zuordnungs-CSV und ggf. Prüfbericht.')
        except Exception as ex:
            messagebox.showerror('Exportfehler', str(ex))

    def save_project(self):
        if not self.project.source:
            messagebox.showwarning('Keine Daten', 'Bitte zuerst Unsorted laden.')
            return
        file = filedialog.asksaveasfilename(title='Playlist-Projekt speichern', defaultextension='.json', filetypes=[('Eugi Projekt', '*.json')])
        if file:
            try:
                self.project.save(file)
                self.status.set(f'Projekt gespeichert: {file}')
            except Exception as ex:
                messagebox.showerror('Speicherfehler', str(ex))

    def open_project(self):
        file = filedialog.askopenfilename(title='Playlist-Projekt öffnen', filetypes=[('Eugi Projekt', '*.json')])
        if not file:
            return
        try:
            try:
                self.project.load_project(file)
            except FileNotFoundError:
                alt = filedialog.askopenfilename(title='Unsorted-Datei erneut auswählen', filetypes=[('M3U8 / M3U Playlists', '*.m3u8 *.m3u')])
                if not alt:
                    return
                self.project.load_project(file, alt)
            self.file_label.set(f'Unsorted: {self.project.source_path}')
            self.populate()
            self.status.set(f'Projekt geladen: {file}')
        except Exception as ex:
            messagebox.showerror('Projektfehler', str(ex))


if __name__ == '__main__':
    App().mainloop()
