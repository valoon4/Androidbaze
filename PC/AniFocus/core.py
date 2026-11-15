"""Persistence and clock logic for AniFocus v0.1 (no GUI dependencies)."""
from __future__ import annotations

import json
import math
import os
import time
from pathlib import Path

AREAS = ("Master", "Research", "Exploring")
DEFAULT_MODULES = {
    "Master": ["RP1", "ITM1", "LEAD1", "DF1"],
    "Research": ["OST", "Playlists", "Seasonvids", "LA", "Syncupdates"],
    "Exploring": [
        "Anime & Cartoon Serie", "Anime & Cartoon Film",
        "Liveaction Serie", "Liveaction Film", "Gaming", "Manga", "Comic",
    ],
}


def fresh_state() -> dict:
    return {
        "schema": 2,
        "minutes": 25,
        "area": "Master",
        "modules": {area: names.copy() for area, names in DEFAULT_MODULES.items()},
        "selected": {area: (names[0] if names else None) for area, names in DEFAULT_MODULES.items()},
        "progress": {area: None for area in AREAS},
    }


def normalize_state(data: object) -> dict:
    state = fresh_state()
    if not isinstance(data, dict):
        return state

    minutes = data.get("minutes")
    if type(minutes) is int and 1 <= minutes <= 180:
        state["minutes"] = minutes

    if data.get("area") in AREAS:
        state["area"] = data["area"]

    source_modules = data.get("modules")
    if isinstance(source_modules, dict):
        for area in AREAS:
            source = source_modules.get(area)
            if not isinstance(source, list):
                continue
            clean = []
            for entry in source:
                if isinstance(entry, str):
                    name = " ".join(entry.strip().split())[:32]
                    if name and name.casefold() not in (s.casefold() for s in clean):
                        clean.append(name)
                if len(clean) == 16:
                    break
            # v0.0 stored empty Research/Exploring lists. Add the new v0.1
            # defaults once, without overwriting or duplicating custom items.
            if data.get("schema", 1) == 1 and area in ("Research", "Exploring"):
                original = clean
                clean = DEFAULT_MODULES[area].copy()
                clean.extend(n for n in original if n.casefold() not in
                             (v.casefold() for v in clean))
            state["modules"][area] = clean[:16]

    selected = data.get("selected")
    for area in AREAS:
        available = state["modules"][area]
        candidate = selected.get(area) if isinstance(selected, dict) else None
        state["selected"][area] = candidate if candidate in available else (available[0] if available else None)

    progress = data.get("progress")
    if isinstance(progress, dict):
        for area in AREAS:
            value = progress.get(area)
            if type(value) is int and 0 <= value <= 100:
                state["progress"][area] = value

    return state


def state_path() -> Path:
    if os.name == "nt":
        base = Path(os.environ.get("APPDATA") or (Path.home() / "AppData" / "Roaming"))
    else:
        base = Path(os.environ.get("XDG_CONFIG_HOME") or (Path.home() / ".config"))
    return base / "AniFocus" / "state.json"


def load_state(path: Path | None = None) -> dict:
    target = path or state_path()
    try:
        return normalize_state(json.loads(target.read_text(encoding="utf-8")))
    except (OSError, UnicodeError, ValueError, TypeError):
        return fresh_state()


def save_state(data: dict, path: Path | None = None) -> None:
    target = path or state_path()
    target.parent.mkdir(parents=True, exist_ok=True)
    temp = target.with_suffix(".json.tmp")
    try:
        temp.write_text(json.dumps(normalize_state(data), ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        os.replace(temp, target)
    finally:
        try:
            temp.unlink(missing_ok=True)
        except OSError:
            pass


def clock_label(seconds: float) -> str:
    remaining = max(0, math.ceil(seconds))
    minutes, secs = divmod(remaining, 60)
    return f"{minutes:02d}:{secs:02d}"


class FocusTimer:
    """Monotonic deadline: accurate after minimization, sleep or lag."""

    def __init__(self, minutes: int = 25):
        if type(minutes) is not int or not 1 <= minutes <= 180:
            raise ValueError("minutes must be between 1 and 180")
        self.duration = minutes * 60
        self.remaining = float(self.duration)
        self.deadline: float | None = None

    @property
    def running(self) -> bool:
        return self.deadline is not None

    def seconds_left(self, now: float | None = None) -> float:
        if self.deadline is None:
            return self.remaining
        current = time.monotonic() if now is None else now
        return max(0.0, self.deadline - current)

    def start(self, now: float | None = None) -> None:
        if self.running:
            return
        if self.remaining <= 0:
            self.remaining = float(self.duration)
        current = time.monotonic() if now is None else now
        self.deadline = current + self.remaining

    def pause(self, now: float | None = None) -> None:
        if not self.running:
            return
        self.remaining = self.seconds_left(now)
        self.deadline = None

    def reset(self, minutes: int | None = None) -> None:
        if minutes is not None:
            if type(minutes) is not int or not 1 <= minutes <= 180:
                raise ValueError("minutes must be between 1 and 180")
            self.duration = minutes * 60
        self.deadline = None
        self.remaining = float(self.duration)

    def tick(self, now: float | None = None) -> bool:
        """Return True exactly once when a running session finishes."""
        if self.running and self.seconds_left(now) <= 0:
            self.deadline = None
            self.remaining = 0.0
            return True
        return False
