"""Parse pasted Markdown/TSV research tables and match against Unsorted by filename.

Research columns: Title | Year/quarter | [OP/ED | Nr | Song | Artist] | Best | Super | Ultra | [Mega].
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
    kind: str = ''
    number: str = ''
    song: str = ''
    artist: str = ''


@dataclass
class MatchReport:
    assignments: dict[int, tuple[int, int, int, int]]
    quarters: dict[int, int | None]
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
            f"Davon mit Quartal: {sum(q is not None for q in self.quarters.values())}",
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
    """Paste Markdown or TSV, accepting short and extended tables.

    Short:   Title | Year/quarter | Best | Super | Ultra [| Mega]
    Extended: Title | Year/quarter | OP/ED | Nr | Song | Artist |
              Best | Super | Ultra [| Mega]
    Mega defaults to 0 when omitted. Column Nr is not a playlist flag.
    """
    results: list[ResearchRow] = []
    for number, raw in enumerate(text.splitlines(), 1):
        stripped = raw.strip()
        if not stripped or stripped.startswith(chr(96) * 3):
            continue
        if stripped.startswith("|"):
            cells = [cell.strip() for cell in stripped.strip("|").split("|")]
        elif "\t" in stripped:
            cells = [cell.strip() for cell in stripped.split("\t")]
        else:
            continue
        if not cells:
            continue
        # Markdown separator line, including single '-' cells.
        if all(re.fullmatch(r":?-+:?", cell.replace(" ", "")) for cell in cells):
            continue
        if cells[0].casefold() in ("name", "titel", "title", "anime", "song", "lied", "track"):
            if cells[-3:] and not all(v in ("0", "1") for v in cells[-3:]):
                continue

        length = len(cells)
        if length in (5, 9):
            count = 3
        elif length in (6, 10):
            count = 4
        elif 7 <= length <= 8:
            count = 3
        else:
            raise ValueError(
                f"Zeile {number}: {length} Spalten erkannt. "
                "Unterstützt werden 5/6 (kurz) oder 9/10 (Anime, Jahr, OP/ED, Nr, Song, Interpret, Flags)."
            )

        values = cells[-count:]
        if any(value not in ("0", "1") for value in values):
            raise ValueError(
                f"Zeile {number} ({cells[0]}): Die letzten {count} Playlist-Spalten "
                "müssen 0 oder 1 enthalten."
            )
        if not cells[0]:
            raise ValueError(f"Zeile {number}: Anime/Titel fehlt.")
        flags = tuple(int(v) for v in values) + (0,) * (4 - count)
        metadata = cells[2:-count]
        kind = metadata[0] if metadata else ""
        sequence = metadata[1] if len(metadata) >= 2 else ""
        song = metadata[2] if len(metadata) >= 3 else ""
        artist = metadata[3] if len(metadata) >= 4 else ""
        results.append(ResearchRow(
            name=cells[0], period=cells[1], flags=flags, line=number,
            kind=kind, number=sequence, song=song, artist=artist
        ))
    if not results:
        raise ValueError("Keine Research-Zeilen gefunden. Bitte die Markdown- oder TSV-Tabelle einfügen.")
    return results


def lookup_keys(row: ResearchRow) -> list[str]:
    """Exact stem first; support rows where OP/ED is a separate column.

    Extra variants are only considered if the stated filename itself has no
    uniquely matching entry; no fuzzy suggestions are selected automatically.
    """
    names = [row.name]
    kind = row.kind.upper().strip()
    if kind in ("OP", "ED", "OST", "IN", "INSERT"):
        # An explicit 'OP 2' in the anime column always has priority.
        if not re.search(r"(?<!\w)" + re.escape(kind) + r"\s*\d*\s*$", row.name, re.I):
            names.append(f"{row.name} {kind}")
            # 'Nr' can be a theme number; never let it displace the plain OP/ED.
            if row.number.isdecimal() and int(row.number) > 1:
                names.append(f"{row.name} {kind} {row.number}")
    return list(dict.fromkeys(filename_key(value) for value in names if filename_key(value)))


def parse_quarter(period: str) -> int | None:
    """Recognize YYYY 3., YYYY Q3, Q3 and season labels."""
    match = re.search(r"(?<!\w)[Qq]\s*([1-4])(?!\d)", period)
    if match:
        return int(match.group(1))
    match = re.search(r"(?<!\d)(?:19|20)\d{2}\s*[-./ ]\s*([1-4])(?:\.|\b)", period)
    if match:
        return int(match.group(1))
    names = ("winter", "spring", "sommer", "summer", "fall", "autumn", "herbst", "frühling", "fruehling")
    mapping = {"winter": 1, "spring": 2, "frühling": 2, "fruehling": 2, "summer": 3, "sommer": 3, "fall": 4, "autumn": 4, "herbst": 4}
    for name in names:
        if re.search(r"(?<!\w)" + re.escape(name) + r"(?!\w)", period, flags=re.I):
            return mapping[name]
    return None


def match_research(source: list[Track], rows: list[ResearchRow]) -> MatchReport:
    """Match uniquely by the actual music filename, never by artist/title EXTINF."""
    index: dict[str, list[int]] = defaultdict(list)
    for idx, track in enumerate(source):
        key = filename_key(stem_from_track(track))
        if key:
            index[key].append(idx)

    chosen: dict[int, tuple[int, int, int, int]] = {}
    quarters: dict[int, int | None] = {}
    blocked: set[int] = set()
    missing: list[str] = []
    ambiguous: list[str] = []
    warnings: list[str] = []
    for row in rows:
        variants = lookup_keys(row)
        candidates: list[int] = []
        key = variants[0] if variants else ""
        for potential in variants:
            possible = index.get(potential, [])
            if len(possible) == 1:
                candidates = possible
                key = potential
                break
            if len(possible) > 1:
                candidates = possible
                key = potential
                break
        if len(candidates) != 1:
            detail = f"Zeile {row.line}: {row.name} ({row.period})"
            if len(candidates) > 1:
                ambiguous.append(f"{detail}: {len(candidates)} Einträge mit gleichem Dateinamen; keine automatische Entscheidung.")
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
            if chosen[idx] == row.flags and quarters[idx] == parse_quarter(row.period):
                warnings.append(f"Zeile {row.line}: {row.name} steht doppelt mit gleichen Flags in der Tabelle.")
            else:
                ambiguous.append(f"Zeile {row.line}: {row.name}: widersprüchliche 0/1-Zuordnungen; Track bleibt unklassifiziert.")
                chosen.pop(idx)
                quarters.pop(idx, None)
                blocked.add(idx)
            continue
        chosen[idx] = row.flags
        quarters[idx] = parse_quarter(row.period)
    return MatchReport(chosen, quarters, len(rows), missing, ambiguous, warnings)
