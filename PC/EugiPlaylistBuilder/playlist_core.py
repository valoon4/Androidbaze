"""Lossless, conservative M3U playlist filtering. No audio files are touched."""

from __future__ import annotations

from collections import Counter, defaultdict
from dataclasses import dataclass
from pathlib import Path
import re
import unicodedata
from urllib.parse import unquote, urlsplit


@dataclass(frozen=True)
class Track:
    info: str  # complete #EXTINF line (possibly empty)
    uri: str
    raw: tuple[str, ...]  # exact source lines, without line endings
    line: int

    @property
    def title(self) -> str:
        return self.info.split(',', 1)[1].strip() if ',' in self.info else ''


def read_playlist(path: str | Path) -> str:
    raw = Path(path).read_bytes()
    for codec in ('utf-8-sig', 'cp1252'):
        try:
            text = raw.decode(codec)
            if '\x00' in text:
                raise ValueError('Datei sieht nicht wie eine Text-Playlist aus.')
            return text
        except UnicodeDecodeError:
            pass
    raise ValueError('Playlist-Kodierung nicht unterstützt.')


def parse_m3u(text: str) -> list[Track]:
    tracks: list[Track] = []
    pending: list[str] = []
    pending_info = ''
    pending_line = 0
    for lineno, raw in enumerate(text.splitlines(), 1):
        line = raw.strip()
        if not line:
            if pending:
                pending.append(raw)
            continue
        if line.startswith('#EXTINF:'):
            if pending_info:
                raise ValueError(f'Zeile {lineno}: EXTINF ohne Dateipfad davor.')
            pending_info = raw
            pending_line = lineno
            pending = [raw]
        elif line.startswith('#'):
            if pending:
                pending.append(raw)
        else:
            if pending_info:
                lines = (*pending, raw)
                info = pending_info
                start = pending_line
            else:
                lines = (raw,)
                info = ''
                start = lineno
            tracks.append(Track(info, line, lines, start))
            pending = []
            pending_info = ''
            pending_line = 0
    if pending_info:
        raise ValueError(f'Zeile {pending_line}: EXTINF ohne folgenden Dateipfad.')
    return tracks


def path_key(raw: str) -> str:
    """Compare file URI and Windows paths independent of escaping/case."""
    value = raw.strip().strip('"').replace('\\', '/')
    if not value:
        return ''
    if value.lower().startswith('file:'):
        url = urlsplit(value)
        if url.scheme.lower() != 'file':
            return ''
        value = unquote(url.path)
        if url.netloc and url.netloc.lower() not in ('', 'localhost'):
            value = f'//{url.netloc}{value}'
        # file:///F:/... -> F:/...
        if re.match(r'^/[A-Za-z]:/', value):
            value = value[1:]
    elif not (re.match(r'^[A-Za-z]:/', value) or value.startswith('/') or value.startswith('//')):
        return ''
    return unicodedata.normalize('NFKC', value).casefold()


def name_key(raw: str) -> str:
    raw = unicodedata.normalize('NFKC', raw).casefold()
    return ' '.join(raw.split())


@dataclass
class Result:
    all_tracks: list[Track]
    kept: list[Track]
    missing: list[Track]
    suggestions: list[tuple[Track, Track]]
    warnings: list[str]
    matched_by_label: int


def filter_playlist(source: list[Track], reference: list[Track], *, fallback: bool = False) -> Result:
    if not source:
        raise ValueError('Die Unsorted-Playlist enthält keine Tracks.')
    if not reference:
        raise ValueError('Die Ultra-/Referenzliste enthält keine Tracks.')

    source_path = defaultdict(list)
    source_name = defaultdict(list)
    ref_path_counts = Counter()
    for track in source:
        key = path_key(track.uri)
        if key:
            source_path[key].append(track)
        if track.title:
            source_name[name_key(track.title)].append(track)
    for track in reference:
        key = path_key(track.uri)
        if key:
            ref_path_counts[key] += 1

    chosen = set()
    matched_refs = set()
    warnings: list[str] = []
    suggestions: list[tuple[Track, Track]] = []
    missing: list[Track] = []
    matched_by_label = 0

    for i, target in enumerate(reference):
        key = path_key(target.uri)
        candidates = source_path.get(key, []) if key else []
        if candidates:
            chosen.update(id(c) for c in candidates)
            matched_refs.add(i)
            for c in candidates:
                if target.title and c.title and name_key(target.title) != name_key(c.title):
                    warnings.append(f'Titelabweichung (Pfad stimmt): {target.title} / {c.title}')
            continue
        # Metadata-only matching works only if the title is unique on BOTH sides.
        title = name_key(target.title or (target.uri if not key else ''))
        matching = source_name.get(title, []) if title else []
        unique_ref = sum(1 for t in reference if name_key(t.title or (t.uri if not path_key(t.uri) else '')) == title) == 1
        if len(matching) == 1 and unique_ref:
            if fallback:
                chosen.add(id(matching[0]))
                matched_by_label += 1
                matched_refs.add(i)
                warnings.append(f'Nur Metadaten-Match: {target.title or target.uri} -> {matching[0].uri}')
            else:
                suggestions.append((target, matching[0]))
        elif len(matching) > 1:
            warnings.append(f'Mehrdeutiger Titel: {target.title or target.uri} ({len(matching)} Kandidaten)')
        if i not in matched_refs:
            missing.append(target)

    for key, n in ref_path_counts.items():
        if n > 1:
            warnings.append(f'Referenz enthält denselben Pfad {n}×: {key}')
    for key, songs in source_path.items():
        if len(songs) > 1 and id(songs[0]) in chosen:
            warnings.append(f'Unsorted enthält denselben Pfad {len(songs)}×: {key}')

    kept = [t for t in source if id(t) in chosen]  # unsorted order retained
    return Result(source, kept, missing, suggestions, warnings, matched_by_label)


def render_m3u(tracks: list[Track]) -> str:
    return '#EXTM3U\n' + ''.join('\n'.join(t.raw) + '\n' for t in tracks)


def render_report(result: Result) -> str:
    lines = [
        'Eugi Playlist Builder — Prüfbericht',
        f'Unsorted: {len(result.all_tracks)}',
        f'Übernommen: {len(result.kept)}',
        f'Entfernt: {len(result.all_tracks) - len(result.kept)}',
        f'Referenz-Einträge ohne bestätigten Match: {len(result.missing)}',
        f'Davon alternative Titel-Matches automatisch übernommen: {result.matched_by_label}',
        '', 'FEHLENDE REFERENZ-EINTRÄGE',
    ]
    lines.extend(f'  - {t.title or t.uri} | {t.uri}' for t in result.missing)
    lines += ['', 'MÖGLICHE TITEL-TREFFER (NICHT automatisch gewählt)']
    lines.extend(f'  - {ref.title or ref.uri} => {cand.title} | {cand.uri}' for ref, cand in result.suggestions)
    lines += ['', 'WARNUNGEN']
    lines.extend(f'  - {w}' for w in result.warnings)
    return '\n'.join(lines) + '\n'
