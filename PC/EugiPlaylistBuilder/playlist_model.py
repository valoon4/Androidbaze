"""Five-level binary playlist model (0=Rest, 1=Best, 2=Super, 3=Ultra, 4=Mega)."""
from __future__ import annotations

import csv
import json
from collections import Counter, defaultdict
from dataclasses import dataclass, field
from pathlib import Path
from typing import Optional

from playlist_core import Track, name_key, parse_m3u, path_key, read_playlist, render_m3u

LEVELS = ('Best', 'Super', 'Ultra', 'Mega')


@dataclass
class ImportReport:
    level: str
    source_count: int
    matched: int
    missing: list[Track] = field(default_factory=list)
    ambiguous: list[str] = field(default_factory=list)
    warnings: list[str] = field(default_factory=list)

    def description(self) -> str:
        lines = [f'{self.level}: {self.matched}/{self.source_count} Einträge per Pfad zugeordnet',
                 f'Nicht gefunden: {len(self.missing)}', f'Mehrdeutig: {len(self.ambiguous)}', '']
        if self.missing:
            lines += ['NICHT GEFUNDEN:'] + [f'- {t.title or t.uri} | {t.uri}' for t in self.missing]
        if self.ambiguous:
            lines += ['', 'MEHRDEUTIG:'] + self.ambiguous
        if self.warnings:
            lines += ['', 'HINWEISE:'] + self.warnings
        return '\n'.join(lines) + '\n'


class PlaylistProject:
    def __init__(self) -> None:
        self.source_path = ''
        self.source: list[Track] = []
        self.flags: list[list[int]] = []
        self.import_reports: dict[str, ImportReport] = {}

    def load_unsorted(self, path: str | Path) -> None:
        tracks = parse_m3u(read_playlist(path))
        if not tracks:
            raise ValueError('Unsorted-Playlist ist leer.')
        self.source_path = str(path)
        self.source = tracks
        self.flags = [[0, 0, 0, 0] for _ in tracks]
        self.import_reports = {}

    @staticmethod
    def level_index(level: str) -> int:
        if level not in LEVELS:
            raise ValueError(f'Unbekannte Stufe: {level}')
        return LEVELS.index(level)

    def set_flag(self, index: int, level: str, state: bool) -> None:
        self.flags[index][self.level_index(level)] = int(bool(state))

    def toggle(self, index: int, level: str) -> None:
        j = self.level_index(level)
        self.flags[index][j] ^= 1

    def import_tier(self, path: str | Path, level: str) -> ImportReport:
        """Clear JUST this level. Exact unique file-path matching; never fuzzy guesses."""
        if not self.source:
            raise ValueError('Bitte zuerst Unsorted laden.')
        j = self.level_index(level)
        tracks = parse_m3u(read_playlist(path))
        index: dict[str, list[int]] = defaultdict(list)
        for i, t in enumerate(self.source):
            key = path_key(t.uri)
            if key:
                index[key].append(i)
        proposed: set[int] = set()
        missing: list[Track] = []
        ambiguous: list[str] = []
        warnings: list[str] = []
        seen: Counter[str] = Counter()
        for entry in tracks:
            key = path_key(entry.uri)
            if not key or key not in index:
                missing.append(entry)
                continue
            seen[key] += 1
            candidates = index[key]
            if len(candidates) != 1:
                ambiguous.append(f'- {entry.title or entry.uri}: {len(candidates)} Treffer in Unsorted')
                continue
            idx = candidates[0]
            proposed.add(idx)
            original = self.source[idx]
            if entry.title and original.title and name_key(entry.title) != name_key(original.title):
                warnings.append(f'Titel weicht ab (Pfad identisch): {entry.title} | {original.title}')
        for key, count in seen.items():
            if count > 1:
                warnings.append(f'Doppelter Referenzpfad {count}×: {key}')
        # Commit only after the full reference was parsed successfully.
        for flags in self.flags:
            flags[j] = 0
        for idx in proposed:
            self.flags[idx][j] = 1
        report = ImportReport(level, len(tracks), len(proposed), missing, ambiguous, warnings)
        self.import_reports[level] = report
        return report

    def rows_for_level(self, level: int) -> list[Track]:
        if level == 0:
            return [t for t, flags in zip(self.source, self.flags) if not any(flags)]
        if not 1 <= level <= 4:
            raise ValueError('Stufe muss zwischen 0 und 4 liegen.')
        return [t for t, flags in zip(self.source, self.flags) if flags[level - 1]]

    def counts(self) -> list[int]:
        return [len(self.rows_for_level(i)) for i in range(5)]

    def export(self, directory: str | Path, stem: str = 'Playlist') -> list[Path]:
        if not self.source:
            raise ValueError('Bitte zuerst Unsorted laden.')
        directory = Path(directory)
        directory.mkdir(parents=True, exist_ok=True)
        saved = []
        for number, name in enumerate(('Rest', *LEVELS)):
            output = directory / f'{stem}_{number}_{name}.m3u'
            output.write_bytes(render_m3u(self.rows_for_level(number)).encode('utf-8-sig'))
            saved.append(output)
        with (directory / f'{stem}_Zuordnung.csv').open('w', encoding='utf-8-sig', newline='') as file:
            writer = csv.writer(file, delimiter=';')
            writer.writerow(('Track', 'Dateipfad', 'Rest', *LEVELS))
            for song, flags in zip(self.source, self.flags):
                writer.writerow((song.title, song.uri, int(not any(flags)), *flags))
        if self.import_reports:
            (directory / f'{stem}_Pruefbericht.txt').write_text(
                '\n'.join(report.description() for report in self.import_reports.values()), encoding='utf-8-sig'
            )
        return saved

    def save(self, path: str | Path) -> None:
        if not self.source:
            raise ValueError('Keine Unsorted geladen.')
        data = {
            'format': 'eugi-playlist-builder-1',
            'source_path': self.source_path,
            'tracks': [{'uri': t.uri, 'flags': f} for t, f in zip(self.source, self.flags)],
        }
        Path(path).write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding='utf-8')

    def load_project(self, path: str | Path, unsorted_override: Optional[str] = None) -> None:
        obj = json.loads(Path(path).read_text(encoding='utf-8-sig'))
        if obj.get('format') != 'eugi-playlist-builder-1':
            raise ValueError('Unbekanntes Projektformat.')
        source_path = unsorted_override or obj.get('source_path')
        if not source_path or not Path(source_path).is_file():
            raise FileNotFoundError('Unsorted-Datei nicht gefunden; bitte neu auswählen.')
        self.load_unsorted(source_path)
        saved = obj.get('tracks', [])
        if len(saved) != len(self.source):
            raise ValueError('Unsorted hat inzwischen eine andere Anzahl Tracks; Import abgebrochen.')
        if any(path_key(item.get('uri', '')) != path_key(track.uri) for item, track in zip(saved, self.source)):
            raise ValueError('Trackreihenfolge/Pfade haben sich geändert; Import abgebrochen.')
        for idx, item in enumerate(saved):
            flags = item.get('flags', [])
            if len(flags) != 4 or any(x not in (0, 1, False, True) for x in flags):
                raise ValueError(f'Ungültige Flags in Track {idx + 1}.')
            self.flags[idx] = [int(x) for x in flags]
