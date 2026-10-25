"""Variant families in Unsorted: OPv2, OPv3, OPv57 inherit the base OP flags.

An independent 'OP 2' is a *different* opening from 'OPv2'.
Suffixes after the version number (MIX, Ger, Rus, A Capella, TV Size...)
do not split the family: they are part of that version's filename/URI.
"""
from __future__ import annotations

from collections import defaultdict
import re

from playlist_core import Track
from research_table import filename_key, stem_from_track


VERSIONED_THEME = re.compile(
    r"(?<![a-zA-Z0-9])(?:OP|ED)(?:\s+\d+)?\s*v\d+(?=\W|$)",
    re.I,
)
VERSION_SUFFIX = re.compile(r"v\d+", re.I)


def family_key(stem: str) -> str:
    """'Anime OPv57 Remix' -> key('Anime OP'); 'Anime OP 2v2' -> key('Anime OP 2').

    'Anime OP 2' is NOT part of the 'Anime OP' family.
    """
    match = VERSIONED_THEME.search(stem)
    if match is None:
        return filename_key(stem)
    version = VERSION_SUFFIX.search(match.group())
    if version is None:
        return filename_key(stem)
    base = stem[:match.start()] + match.group()[:version.start()].rstrip()
    return filename_key(base)


def families(source: list[Track]) -> dict[str, list[int]]:
    groups: dict[str, list[int]] = defaultdict(list)
    for idx, track in enumerate(source):
        groups[family_key(stem_from_track(track))].append(idx)
    return dict(groups)


def family_members(source: list[Track], track_index: int) -> list[int]:
    root = family_key(stem_from_track(source[track_index]))
    return [idx for idx, track in enumerate(source)
            if family_key(stem_from_track(track)) == root]


def inherit_positive_flags(
    source: list[Track],
    flags: list[list[int]],
    quarters: list[int | None] | None = None,
) -> list[int]:
    """Propagate Super, Ultra, Sleepy to every OP/ED version in same family.

    Best follows automatically. Never turn an unmatched variant into Rest.
    Quarters inherited only when all explicitly known quarters agree.
    Returns track indices whose flags changed.
    """
    if len(source) != len(flags) or any(len(row) != 4 for row in flags):
        raise ValueError("Expected one [Best, Super, Ultra, Sleepy] row per source track")
    changed: list[int] = []
    for indices in families(source).values():
        if len(indices) == 1:
            continue
        upper = [int(any(flags[idx][j] for idx in indices)) for j in range(1, 4)]
        if any(upper):
            for idx in indices:
                before = flags[idx].copy()
                flags[idx][0] = 1
                for offset, enabled in enumerate(upper, 1):
                    if enabled:
                        flags[idx][offset] = 1
                if flags[idx] != before:
                    changed.append(idx)
        if quarters is not None:
            known = {quarters[idx] for idx in indices if quarters[idx] is not None}
            if len(known) == 1:
                only = next(iter(known))
                for idx in indices:
                    if quarters[idx] is None:
                        quarters[idx] = only
    return changed
