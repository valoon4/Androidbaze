"""Parse pasted Markdown/TSV research tables and match against Unsorted by filename.

Research columns: Theme | Year+quarter | Best | Super | Ultra | Mega.
No approximate/artist-title matches are applied automatically.
"""
from __future__ import annotations

from collections import defaultdict
from dataclasses import dataclass, field
from difflib import get_close_matches
from pathlib import PurePosixPath
import re
import unicodedata
from urllib.parse import unquote

from playlist_core import Track


@dataclass(frozen=True)
class ResearchRow:
    name: str
    period: str
    flags: tuple[int, int, int, int]
    line: int


@dataclass
class MatchReport:
    assignments: dict[int, tuple[int, int, int, int]]
    input_count: int
    missing: list[str] = field(default_factory=list)
    ambiguous: list[str] = field(default_factory=list)
    warnings: list[str] = field(default_factory=list)

    @property
    def has_issues(self) -> bool:
        return bool(self.missing or self.ambiguous or self.warnings)

    def text(self, source_count: int) -> str:
        out = [
            f"Research-Zeilen: {self.input_count}",
            f"Eindeutig zugeordnet: {len(self.assignments)}",
            f"Noch nicht zugeordnete Unsorted-Songs: {source_count - len(self.assignments)}",
            f"Nicht gefunden / Jahr falsch: {len(self.missing)}",
            f"Mehrdeutig / Konflikt: {len(self.ambiguous)}",
            "",
        ]
        for heading, items in (
            ("NICHT GEFUNDEN", self.missing),
            ("MEHRDEUTIG / KONFLIKT", self.ambiguous),
            ("HINWEISE", self.warnings),
        ):
            if items:
                out.append(heading)
                out.extend("  " + item for item in items)
                out.append("")
        return "\n".join(out).rstrip() + "\n"


def filename_key(value: str) -> str:
    """Unicode-safe name matching: Mojakou OP 2 == Mojakou OP2, not OPv2."""
    value = unicodedata.normalize("NFKC", value)
    return "".join(char for char in value.casefold() if char.isalnum())


def stem_from_track(track: Track) -> str:
    uri = unquote(track.uri.replace("\\", "/"))
    return PurePosixPath(uri).stem


def path_year(track: Track) -> str | None:
    decoded = unquote(track.uri.replace("\\", "/"))
    match = re.search(r"/Musik/((?:19|20)\d\d)/", decoded, flags=re.I)
    return match.group(1) if match else None


def parse_research_table(text: str) -> list[ResearchRow]:
    """Accept Markdown with six cells or tab-separated research rows."""
    results: list[ResearchRow] = []
    for number, raw in enumerate(text.splitlines(), 1):
        stripped = raw.strip()
        if not stripped or stripped.startswith(chr(96) * 3):
            continue
        if stripped.startswith("|"):
            cells = [x.strip() for x in stripped.strip("|").split("|")]
        elif "\t" in stripped:
            cells = [x.strip() for x in stripped.split("\t")]
        else:
            # Allow surrounding explanatory text or markdown headings.
            continue
        if not cells or all(re.fullmatch(r":?-{2,}:?", x.replace(" ", "")) for x in cells):
            continue
        if cells[0].casefold() in ("name", "titel", "title", "anime", "song", "lied", "track"):
            if not all(v in ("0", "1") for v in cells[-4:]):
                continue
        if len(cells) != 6:
            raise ValueError(f"Zeile {number}: Erwartet 6 Spalten (Titel | Jahr/Quartal | Best | Super | Ultra | Mega); gefunden: {len(cells)}.")
        if not cells[0]:
            raise ValueError(f"Zeile {number}: Songtitel/Anime-Bezeichnung fehlt.")
        if any(flag not in ("0", "1") for flag in cells[2:]):
            raise ValueError(f"Zeile {number} ({cells[0]}): Best/Super/Ultra/Mega müssen jeweils 0 oder 1 sein.")
        results.append(ResearchRow(cells[0], cells[1], tuple(int(x) for x in cells[2:]), number))
    if not results:
        raise ValueError("Keine Research-Zeilen gefunden. Bitte die Markdown-Tabelle mit sechs Spalten einfügen.")
    return results


def match_research(source: list[Track], rows: list[ResearchRow]) -> MatchReport:
    """Match uniquely by the actual music filename, never by artist/title EXTINF."""
    index: dict[str, list[int]] = defaultdict(list)
    for idx, track in enumerate(source):
        key = filename_key(stem_from_track(track))
        if key:
            index[key].append(idx)

    chosen: dict[int, tuple[int, int, int, int]] = {}
    blocked: set[int] = set()
    missing: list[str] = []
    ambiguous: list[str] = []
    warnings: list[str] = []
    for row in rows:
        key = filename_key(row.name)
        candidates = index.get(key, [])
        if len(candidates) != 1:
            detail = f"Zeile {row.line}: {row.name} ({row.period})"
            if len(candidates) > 1:
                ambiguous.append(f"{detail}: {len(candidates)} Unsorted-Einträge mit gleichem Namen; keine automatische Entscheidung.")
            else:
                suggestions = get_close_matches(key, index.keys(), n=2, cutoff=0.76)
                extras = [stem_from_track(source[index[k][0]]) for k in suggestions if len(index[k]) == 1]
                missing.append(detail + (f" · mögliche Kandidaten (nicht zugeordnet): {', '.join(extras)}" if extras else ""))
            continue
        idx = candidates[0]
        row_year_match = re.search(r"(?<!\d)((?:19|20)\d{2})(?!\d)", row.period)
        src_year = path_year(source[idx])
        if row_year_match and src_year and row_year_match.group(1) != src_year:
            missing.append(f"Zeile {row.line}: {row.name}: Research-Jahr {row_year_match.group(1)} ≠ Dateijahr {src_year}.")
            continue
        if idx in blocked:
            continue
        if idx in chosen:
            if chosen[idx] == row.flags:
                warnings.append(f"Zeile {row.line}: {row.name} steht doppelt mit gleichen Flags in der Tabelle.")
            else:
                ambiguous.append(f"Zeile {row.line}: {row.name}: widersprüchliche 0/1-Zuordnungen; Track bleibt unklassifiziert.")
                chosen.pop(idx)
                blocked.add(idx)
            continue
        chosen[idx] = row.flags
    return MatchReport(chosen, len(rows), missing, ambiguous, warnings)
