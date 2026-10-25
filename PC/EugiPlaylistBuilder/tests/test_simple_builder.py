"""Regression tests for the simplified single-file Playlist Builder workflow."""
import tempfile
import unittest
from pathlib import Path

from research_table import parse_quarter, parse_research_table
from simple_builder import Builder


SOURCE_1996 = """#EXTM3U
#EXTINF:200,Artist - Mojakou
file:///F:/Musik/1996/Mojakou%20OP%202.flac
#EXTINF:230,Artist - Shamanic Princess
file:///F:/Musik/1996/Shamanic%20Princess%20OP.flac
#EXTINF:222,Main Cast - Cyber Formula
file:///F:/Musik/1996/Shin%20Seiki%20GPX%20Cyber%20Formula%20Saga.mp3
#EXTINF:230,Artist - Conan
file:///F:/Musik/1996/Meitantei%20Conan%20OP%202.flac
#EXTINF:230,Artist - Never mentioned
file:///F:/Musik/1996/Song%20Without%20Research%20ED.flac
"""

RESEARCH_1996 = """| Mojakou OP 2 | 1996 3. | 1 | 1 | 1 | 1 |
| --------------------------------- | ------- | - | - | - | - |
| Shamanic Princess OP | 1996 3. | 0 | 0 | 0 | 0 |
| Shin Seiki GPX Cyber Formula Saga | 1996 3. | 1 | 1 | 1 | 1 |
| Meitantei Conan OP 2 | 1996 4. | 1 | 1 | 1 | 1 |
"""


class TestSingleExport(unittest.TestCase):
    def setup_builder(self, folder: str, name: str = "1996 Unsorted.m3u8"):
        source = Path(folder) / name
        source.write_text(SOURCE_1996, encoding="utf-8")
        obj = Builder()
        obj.load(source)
        return obj

    def test_markdown_zero_zero_zero_zero_is_only_rest(self):
        with tempfile.TemporaryDirectory() as tmp:
            b = self.setup_builder(tmp)
            report = b.apply(RESEARCH_1996)
            self.assertEqual(len(report.assignments), 4)
            self.assertFalse(report.missing)
            self.assertFalse(report.ambiguous)
            self.assertEqual(b.counts(), [1, 4, 3, 3, 3])
            self.assertEqual(b.quarters[:4], [3, 3, 3, 4])
            self.assertIsNone(b.quarters[-1])
            self.assertIn("Never mentioned", b.rows("Best")[-1].title)
            self.assertEqual(len(b.rows("Ultra", 3)), 2)
            self.assertEqual(len(b.rows("Ultra", 4)), 1)
            self.assertEqual(b.unassigned_quarter_count(), 1)

    def test_higher_tiers_imply_best(self):
        with tempfile.TemporaryDirectory() as tmp:
            b = self.setup_builder(tmp)
            b.apply("| Mojakou OP 2 | 1996 3. | 0 | 0 | 1 | 0 |")
            self.assertEqual(b.flags[0], [1, 0, 1, 0])
            b.set_flag(0, "Best", False)
            self.assertEqual(b.flags[0], [0, 0, 0, 0])
            b.set_flag(0, "Mega", True)
            self.assertEqual(b.flags[0], [1, 0, 0, 1])

    def test_one_utf8_m3u8_no_extra_files_or_changed_paths(self):
        with tempfile.TemporaryDirectory() as tmp:
            b = self.setup_builder(tmp)
            b.apply(RESEARCH_1996)
            target = Path(tmp) / "1996_Q3_Ultra.m3u8"
            result = b.export_one(target, "Ultra", 3)
            self.assertEqual(result, target)
            self.assertEqual(sorted(x.name for x in Path(tmp).glob("*.m3u8")),
                             ["1996 Unsorted.m3u8", "1996_Q3_Ultra.m3u8"])
            content = target.read_text(encoding="utf-8")
            self.assertTrue(content.startswith("#EXTM3U\n"))
            self.assertEqual(content.count("#EXTINF:"), 2)
            self.assertIn("file:///F:/Musik/1996/Mojakou%20OP%202.flac", content)
            self.assertFalse(target.read_bytes().startswith(b"\xef\xbb\xbf"))

    def test_source_season_is_known_for_every_track(self):
        with tempfile.TemporaryDirectory() as tmp:
            b = self.setup_builder(tmp, "1996 Q3 Unsorted.m3u8")
            self.assertEqual(b.quarters, [3] * 5)
            b.apply("| Mojakou OP 2 | 1996 3. | 1 | 0 | 0 | 0 |")
            self.assertEqual(b.unassigned_quarter_count(), 0)
            self.assertEqual(len(b.rows("Best", 3)), 5)
            self.assertEqual(b.default_filename("Best", 3), "1996_Q3_Best.m3u8")

    def test_ambiguous_file_stems_not_selected_as_rest(self):
        with tempfile.TemporaryDirectory() as tmp:
            p = Path(tmp) / "2027 Unsorted.m3u8"
            p.write_text(
                "#EXTM3U\n"
                "#EXTINF:100,Original\nfile:///F:/Musik/2027/Anime%20OP.flac\n"
                "#EXTINF:100,Other\nfile:///F:/Musik/2027/Anime%20OP.mp3\n", encoding="utf-8")
            b = Builder()
            b.load(p)
            with self.assertRaisesRegex(ValueError, "Kein eindeutiger"):
                b.apply("| Anime OP | 2027 Q1 | 0 | 0 | 0 | 0 |")
            self.assertEqual(b.counts(), [0, 2, 0, 0, 0])

    def test_quarter_parser(self):
        self.assertEqual(parse_quarter("1996 3."), 3)
        self.assertEqual(parse_quarter("2027 Q4"), 4)
        self.assertEqual(parse_quarter("Winter 2026"), 1)
        self.assertEqual(parse_quarter("2027 Fall"), 4)
        self.assertIsNone(parse_quarter("2014 Unsorted"))

    def test_unicode_table_preserves_names(self):
        with tempfile.TemporaryDirectory() as tmp:
            src = Path(tmp) / "2027 Q1 Unsorted.m3u8"
            src.write_text(
                "#EXTM3U\n#EXTINF:120,初音ミク - Über\n"
                "file:///F:/Musik/2027/%E9%9F%B3%E6%A5%BD%20OP.flac\n", encoding="utf-8")
            b = Builder()
            b.load(src)
            report = b.apply("| 音楽 OP | 2027 Q1 | 1 | 0 | 1 | 0 |")
            self.assertEqual(len(report.assignments), 1)
            result = b.export_one(Path(tmp) / "2027_Q1_Ultra.m3u8", "Ultra", 1)
            txt = result.read_text("utf-8")
            self.assertIn("初音ミク - Über", txt)
            self.assertIn("%E9%9F%B3%E6%A5%BD", txt)


if __name__ == "__main__":
    unittest.main()
