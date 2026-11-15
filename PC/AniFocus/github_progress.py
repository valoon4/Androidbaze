"""Read-only, opt-in GitHub progress for AniFocus Research.

The timer and settings work without Internet. All values come from the current
canonical Storytimez research status; never infer an overall percentage.
"""
from __future__ import annotations

import re
import urllib.request

SOURCE_URL = "https://raw.githubusercontent.com/valoon4/Storytimez/main/research/RESEARCH_STATUS.md"
SOURCE_PAGE = "https://github.com/valoon4/Storytimez/blob/main/research/RESEARCH_STATUS.md"
SUPPORTED = frozenset(("OST", "Playlists", "Seasonvids"))


def _section(text: str, start: str, end: str) -> str:
    match = re.search(start + r"(.*?)" + end, text, re.IGNORECASE | re.DOTALL | re.MULTILINE)
    return match.group(1) if match else ""


def parse_progress(markdown: str, category: str) -> dict:
    """Return only confirmed, source-derived status. Raise if format changes."""
    if category not in SUPPORTED:
        raise ValueError("No GitHub status defined for this area")
    if not isinstance(markdown, str) or not markdown.strip():
        raise ValueError("Empty research status")

    if category == "OST":
        section = _section(markdown, r"^### [^\n]*OST\s*$", r"^#### |^### ")
        p2 = re.search(r"\*\*([\d.]+)\s+Nipponsei\s+\(P2\)\*\*", section)
        p3 = re.search(r"\b([\d.]+)\s+Gets\s*\+", markdown)
        if not p3:
            p3 = re.search(r"Dec 7[–-]9:\s*([\d.]+)\s+Gets", markdown)
        if not p2 or not p3:
            raise ValueError("OST: canonical remaining counts not found")
        entries = [
            f"P2 Nipponsei: {p2.group(1)} offen",
            f"P3 Gets: {p3.group(1)} offen",
        ]
        return {
            "category": category,
            "heading": "OST · GitHub-Stand",
            "lines": entries,
            "details": " · ".join(entries) + "\nQuelle: " + SOURCE_PAGE,
            "url": SOURCE_PAGE,
        }

    if category == "Playlists":
        section = _section(markdown, r"^### [^\n]*Playlists[^\n]*$", r"^### ")
        match = re.search(r"Offen bleiben u\. a\.\s*\*\*(.+?)\*\*", section)
        if not match:
            raise ValueError("Playlists: confirmed outstanding builds not found")
        names = match.group(1).replace(" und ", "; ").split(";")
        names = [n.strip() for n in names if n.strip()]
        if not names:
            raise ValueError("Playlists: no confirmed status")
        return {
            "category": category,
            "heading": "Playlists · GitHub-Stand",
            "lines": ["Offene Fünfer-Builds (Auswahl):", " · ".join(names)],
            "details": "Offen laut Forschungsstatus:\n" + "\n".join("- " + n for n in names)
                       + "\nEin Abschnitt zählt nur mit fünf geprüften M3U8-Kategorien als gebaut."
                       + "\nQuelle: " + SOURCE_PAGE,
            "url": SOURCE_PAGE,
        }

    section = _section(markdown, r"^### [^\n]*Seasonvideos\s*$", r"^### ")
    parsed = []
    for line in section.splitlines():
        match = re.match(r"^\|\s*\*\*(\d{4}\s+Q[1-4])\*\*\s*\|", line)
        if not match:
            continue
        cells = [cell.strip() for cell in line.strip().strip("|").split("|")]
        if len(cells) < 5:
            continue
        stages = []
        for label, cell in zip(("Load", "Add", "Cut"), cells[1:4]):
            match_stage = re.search(r"\*\*(\d+/\d+)", cell)
            if match_stage:
                stages.append(f"{label} {match_stage.group(1)}")
        if stages:
            parsed.append((match.group(1), stages))

    required = {"2006 Q4", "2016 Q4", "2026 Q4"}
    names = {name for name, _ in parsed}
    if not required.issubset(names):
        raise ValueError("Seasonvideos: current named stages missing")
    headline = {name: stages for name, stages in parsed}
    return {
        "category": category,
        "heading": "Seasonvids · GitHub-Stand",
        "lines": [
            f"2006 Q4: {headline['2006 Q4'][0]}",
            f"2016 Q4: {headline['2016 Q4'][0]} · 2026 Q4: {headline['2026 Q4'][0]}",
        ],
        "details": "\n".join(f"{name}: " + " · ".join(stages) for name, stages in parsed)
                   + "\nAngegeben sind einzelne Workflow-Phasen, kein Gesamtprozent."
                   + "\nQuelle: " + SOURCE_PAGE,
        "url": SOURCE_PAGE,
    }


def fetch_progress(category: str, timeout: int = 8) -> dict:
    """Download only the public Markdown source; no token or write access."""
    if category not in SUPPORTED:
        raise ValueError("Unsupported category")
    req = urllib.request.Request(
        SOURCE_URL,
        headers={"User-Agent": "AniFocus/0.1 (+read-only-source)", "Accept": "text/plain"},
    )
    with urllib.request.urlopen(req, timeout=timeout) as response:
        # Avoid unexpectedly massive downloads.
        raw = response.read(512_001)
    if len(raw) > 512_000:
        raise ValueError("Research status exceeds size limit")
    return parse_progress(raw.decode("utf-8-sig"), category)
