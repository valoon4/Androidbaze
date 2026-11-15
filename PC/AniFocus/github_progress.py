"""Offline Research status snapshots — sourced from Storytimez on 15.11.2026.

This module deliberately contains NO GitHub request, auth, file IO or API.
The app shows only confirmed details, not made-up overall percentages.
"""
from copy import deepcopy

SNAPSHOT_DATE = "15.11.2026"
SOURCE = "valoon4/Storytimez/research/RESEARCH_STATUS.md"
SNAPSHOTS = {
    "OST": {
        "heading": "OST · Stand 15.11.2026",
        "lines": ["P2 Nipponsei: 392 offen", "P3 Gets: 4.092 offen"],
        "details": ("OST · bestätigter Stand 15.11.2026\n"
                    "Dec 6 abgeschlossen; Dec 7–9 offen.\n"
                    "P2 Nipponsei: 392 Einträge offen.\n"
                    "P3 Gets: 4.092 Songs offen.\n"
                    "Flacz/P1 wird im P3-Ablauf mitgeprüft, nicht automatisch erledigt.\n"
                    "Quelle: " + SOURCE),
    },
    "Playlists": {
        "heading": "Playlists · Stand 15.11.2026",
        "lines": ["Dec 0–6: teilweise gebaut", "Offen: 1986, 2000–01, 2003–06, …"],
        "details": ("Playlists · bestätigter Stand 15.11.2026\n"
                    "Offene 5er-Builds: 1986, 2000–2001, 2003–2006, "
                    "2011 Q3/Q4, 2026 Q1–Q4.\n"
                    "Ein Fünfer-Build besteht aus Best, Super, Ultra, Sleepy und Rest.\n"
                    "Bekannte QA-Korrekturen bleiben offen; keine Gesamtquote.\n"
                    "Quelle: " + SOURCE),
    },
    "Seasonvids": {
        "heading": "Seasonvids · Stand 15.11.2026",
        "lines": ["2006 Q4: Load/Add/Cut 18/75", "2016 Q4: Load 69/79 · Add 3/79"],
        "details": ("Seasonvideos · bestätigter Stand 15.11.2026\n"
                    "1986 Q4: Load/Add/Cut 15/15, Merge offen\n"
                    "1996 Q4: Load/Add/Cut 27/27, Merge offen\n"
                    "2006 Q4: Load/Add/Cut 18/75, Merge offen\n"
                    "2016 Q4: Load 69/79, Add 3/79, Cut 0/79, Merge offen\n"
                    "2026 Q4: Load/Add/Cut 52/79, Merge unbestätigt\n"
                    "Dec 7–9 nicht beziffert; 1 Nachtrag ohne Season-Zuordnung.\n"
                    "Quelle: " + SOURCE),
    },
}
SUPPORTED = frozenset(SNAPSHOTS)


def get_progress(category: str):
    """Return a standalone in-memory copy. Absolutely no network access."""
    result = SNAPSHOTS.get(category)
    return deepcopy(result) if result is not None else None
