"""Small, single-output build session. Best is the safety net for the full Unsorted pool."""
from __future__ import annotations

from pathlib import Path
import re
from typing import Iterable

from playlist_core import Track, parse_m3u, read_playlist, render_m3u
from research_table import MatchReport, match_research, parse_quarter, parse_research_table, path_year

LEVELS = ("Rest", "Best", "Super", "Ultra", "Mega")


def source_quarter_from_filename(path: str | Path) -> int | None:
    """A deliberately quarterly Unsorted file assigns its entire pool to that quarter."""
    return parse_quarter(Path(path).stem)


class Builder:
    def __init__(self):
        self.path = ""
        self.source: list[Track] = []
        self.flags: list[list[int]] = []
        self.quarters: list[int | None] = []
        self.matched: set[int] = set()
        self.report: MatchReport | None = None
        self.has_research = False

    def load(self, path: str | Path) -> None:
        source = parse_m3u(read_playlist(path))
        if not source:
            raise ValueError("Unsorted enthält keine Musik-Einträge.")
        self.path = str(path)
        self.source = source
        self.flags = [[1, 0, 0, 0] for _ in source]
        q = source_quarter_from_filename(path)
        self.quarters = [q] * len(source)
        self.matched = set()
        self.report = None
        self.has_research = False

    def apply(self, raw_table: str) -> MatchReport:
        if not self.source:
            raise ValueError("Erst Unsorted.m3u8 laden.")
        rows = parse_research_table(raw_table)
        report = match_research(self.source, rows)
        if not report.assignments:
            raise ValueError("Kein eindeutiger Treffer. Bitte Research-Titel und Unsorted-Datei prüfen; bestehende Daten bleiben unverändert.")
        flags = [[1, 0, 0, 0] for _ in self.source]
        quarterly_file = source_quarter_from_filename(self.path)
        quarters: list[int | None] = [quarterly_file] * len(self.source)
        for idx, (best, super_, ultra, mega) in report.assignments.items():
            if not any((best, super_, ultra, mega)):
                flags[idx] = [0, 0, 0, 0]  # Explicit Rest
            else:
                if not best and any((super_, ultra, mega)):
                    report.warnings.append(
                        f"{rows[0].name if len(rows) == 1 else self.source[idx].uri}: "
                        "höhere Stufe aktiv, daher Best automatisch mitgesetzt."
                    )
                flags[idx] = [1, super_, ultra, mega]
            row_q = report.quarters.get(idx)
            if quarterly_file is not None:
                if row_q is not None and row_q != quarterly_file:
                    report.warnings.append(
                        f"{self.source[idx].uri}: Research sagt Q{row_q}, "
                        f"die Unsorted-Datei ist Q{quarterly_file}; bitte prüfen."
                    )
            else:
                quarters[idx] = row_q
        self.flags = flags
        self.quarters = quarters
        self.matched = set(report.assignments)
        self.report = report
        self.has_research = True
        return report

    def set_flag(self, idx: int, level: str, enabled: bool) -> None:
        if level not in LEVELS:
            raise ValueError(f"Unbekannte Stufe: {level}")
        if level == "Rest":
            self.flags[idx] = [0, 0, 0, 0] if enabled else [1, 0, 0, 0]
        elif level == "Best":
            if enabled:
                self.flags[idx][0] = 1
            else:
                # Best must remain a superset of Super, Ultra and Mega.
                self.flags[idx] = [0, 0, 0, 0]
        else:
            pos = LEVELS.index(level) - 1
            self.flags[idx][pos] = int(enabled)
            if enabled:
                self.flags[idx][0] = 1
        self.matched.add(idx)

    def set_quarter(self, idx: int, quarter: int) -> None:
        if quarter not in (1, 2, 3, 4):
            raise ValueError("Quartal muss 1, 2, 3 oder 4 sein.")
        self.quarters[idx] = quarter

    def rows(self, level: str, quarter: int | None = None) -> list[Track]:
        if level not in LEVELS:
            raise ValueError(f"Unbekannte Stufe: {level}")
        if quarter is not None and quarter not in (1, 2, 3, 4):
            raise ValueError("Ungültiges Quartal.")
        n = LEVELS.index(level)
        return [song for i, song in enumerate(self.source)
                if (quarter is None or self.quarters[i] == quarter)
                and (not any(self.flags[i]) if n == 0 else self.flags[i][n - 1])]

    def counts(self, quarter: int | None = None) -> list[int]:
        return [len(self.rows(level, quarter)) for level in LEVELS]

    def unassigned_quarter_count(self) -> int:
        return sum(q is None for q in self.quarters)

    def export_one(self, filepath: str | Path, level: str, quarter: int | None = None) -> Path:
        if not self.source:
            raise ValueError("Erst Unsorted laden.")
        if not self.has_research and not self.matched:
            raise ValueError("Bitte erst Research-Tabelle einfügen oder Titel markieren.")
        target = Path(filepath)
        if target.suffix.lower() != ".m3u8":
            raise ValueError("Ausgabe muss auf .m3u8 enden.")
        if target.resolve() == Path(self.path).resolve():
            raise ValueError("Unsorted darf nicht überschrieben werden!")
        selected = self.rows(level, quarter)
        if not selected:
            raise ValueError(f"Für {level} / {f'Q{quarter}' if quarter else 'Jahr'} gibt es keine Titel.")
        target.write_bytes(render_m3u(selected).encode("utf-8"))
        return target

    def default_filename(self, level: str, quarter: int | None) -> str:
        m = re.search(r"(?<!\d)((?:19|20)\d{2})(?!\d)", Path(self.path).stem)
        year = m.group(1) if m else next((path_year(t) for t in self.source if path_year(t)), "Playlist")
        q = quarter if quarter is not None else source_quarter_from_filename(self.path)
        return f"{year}{f'_Q{q}' if q else ''}_{level}.m3u8"
