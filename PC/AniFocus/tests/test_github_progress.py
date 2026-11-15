"""Confirm that all research numbers are bundled and offline."""
import sys
import unittest
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from github_progress import SUPPORTED, SNAPSHOT_DATE, get_progress

class OfflineResearchTests(unittest.TestCase):
    def test_three_fixed_statuses(self):
        self.assertEqual(SUPPORTED,{"OST","Playlists","Seasonvids"})
        self.assertEqual(SNAPSHOT_DATE,"15.11.2026")
        for name in SUPPORTED:
            snapshot=get_progress(name)
            self.assertEqual(len(snapshot["lines"]),2)
            self.assertIn(SNAPSHOT_DATE,snapshot["heading"])
            self.assertNotIn("100 %",snapshot["details"])
    def test_ost(self):
        self.assertEqual(get_progress("OST")["lines"],
                         ["P2 Nipponsei: 392 offen","P3 Gets: 4.092 offen"])
    def test_playlists(self):
        details=get_progress("Playlists")["details"]
        for term in ("2000–2001","2011 Q3/Q4","2026 Q1–Q4","Sleepy"):
            self.assertIn(term,details)
    def test_seasonvids(self):
        d=get_progress("Seasonvids")["details"]
        self.assertIn("2016 Q4: Load 69/79, Add 3/79, Cut 0/79",d)
        self.assertIn("2026 Q4: Load/Add/Cut 52/79, Merge unbestätigt",d)
    def test_unrecognized_returns_none_and_no_aliasing(self):
        self.assertIsNone(get_progress("LA"))
        a=get_progress("OST")
        a["lines"].clear()
        self.assertEqual(len(get_progress("OST")["lines"]),2)

if __name__=="__main__":
    unittest.main()
