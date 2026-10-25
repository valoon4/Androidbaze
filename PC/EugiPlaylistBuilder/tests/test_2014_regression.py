"""Integration regression using Eugi's actual 2014 M3U examples."""
from pathlib import Path
from tempfile import TemporaryDirectory
import unittest

from playlist_model import PlaylistProject

ROOT = Path(__file__).resolve().parents[1]


class Test2014Regression(unittest.TestCase):
    def test_2014_ultra_filter(self):
        p = PlaylistProject()
        p.load_unsorted(ROOT / "examples" / "2014-Unsorted.m3u")
        report = p.import_tier(ROOT / "examples" / "2014-Ultra.m3u", "Ultra")
        self.assertEqual(len(p.source), 517)
        self.assertEqual(report.source_count, 35)
        self.assertEqual(report.matched, 35)
        self.assertFalse(report.missing)
        self.assertFalse(report.ambiguous)
        self.assertEqual(p.counts(), [482, 0, 0, 35, 0])
        with TemporaryDirectory() as tmp:
            exports = p.export(tmp, "2014")
            self.assertEqual(len(exports), 5)
            self.assertEqual(exports[3].read_text("utf-8-sig").count("#EXTINF:"), 35)


if __name__ == "__main__":
    unittest.main()
