"""Source parser regression checks: canonical rows, no fabricated percentages."""
import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from github_progress import parse_progress, SOURCE_PAGE

SAMPLE = """# Research
### 🎬 Seasonvideos
| Jahr / Season | Load | Add | Cut | Merge | Rest |
|---|---|---|---|---|---|
| **1986 Q4** | 🟩 **15/15** | 🟩 **15/15** | 🟩 **15/15** | 🟥 | |
| **1996 Q4** | 🟩 **27/27** | 🟩 **27/27** | 🟩 **27/27** | 🟥 | |
| **2006 Q4** | 🟨 **18/75** | 🟨 **18/75** | 🟨 **18/75** | 🟥 | |
| **2016 Q4** | 🟨 **69/79** | 🟨 **3/79** | 🟥 **0/79** | 🟥 | |
| **2026 Q4** | 🟨 **52/79** | 🟨 **52/79** | 🟨 **52/79** | ⬜ | |
### 🎵 OST
**2026/Dec 6:** abgeschlossen. zuerst **392 Nipponsei (P2)**, dann **P3 Gets** chronologisch.
| OST | 🟩 Dec 6 | **Dec 7–9: 4.092 Gets + P1/P2-Rest offen** |
### 📻 Playlists — EIN Fortschrittswert: fertig gebaut
**Aktueller PL-Aufgabenplan:** Offen bleiben u. a. **1986; 2011 Q3/Q4; 2026 Q1–Q4; 2000–2001 und 2003–2006**, dazu bekannte QA-Korrekturen.
### Anime-Kernbereiche
"""


class ProgressTests(unittest.TestCase):
    def test_ost_is_rest_not_percent(self):
        data = parse_progress(SAMPLE, "OST")
        self.assertEqual(data["lines"],["P2 Nipponsei: 392 offen","P3 Gets: 4.092 offen"])
        self.assertNotIn("%",data["details"])

    def test_playlists_counts_only_built_scope(self):
        data = parse_progress(SAMPLE,"Playlists")
        self.assertIn("2011 Q3/Q4",data["lines"][1])
        self.assertIn("fünf geprüften",data["details"])
        self.assertEqual(data["url"],SOURCE_PAGE)

    def test_sv_distinguishes_stages(self):
        data = parse_progress(SAMPLE,"Seasonvids")
        self.assertIn("2006 Q4: Load 18/75",data["lines"][0])
        self.assertIn("2016 Q4: Load 69/79",data["lines"][1])
        self.assertIn("Cut 0/79",data["details"])

    def test_fail_closed_when_missing(self):
        for name in ("OST","Playlists","Seasonvids"):
            with self.subTest(name=name), self.assertRaises(ValueError):
                parse_progress("No relevant data",name)
        with self.assertRaises(ValueError):
            parse_progress(SAMPLE,"LA")


if __name__ == "__main__":
    unittest.main()
