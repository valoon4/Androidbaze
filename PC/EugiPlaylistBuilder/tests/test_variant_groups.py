"""Regression: user rule 'If OP is Super, OPv2, OPv3, OPv57 are Super too'."""
from __future__ import annotations

import unittest
from pathlib import Path

from research_table import filename_key, stem_from_track
from simple_builder import Builder
from variant_groups import family_key


SOURCE = Path(__file__).resolve().parents[1] / "examples" / "2014-Unsorted.m3u"


class TestVersionInheritance(unittest.TestCase):
    def test_same_op_family_even_for_remix_and_language_versions(self):
        self.assertEqual(family_key("Tonari no Seki-kun OP"),
                         family_key("Tonari no Seki-kun OPv57 (Spanish mix)"))
        self.assertEqual(family_key("Tonari no Seki-kun OP"),
                         family_key("Tonari no Seki-kun OPv3 Technopop MIX"))
        self.assertEqual(family_key("Tonari no Seki-kun ED"),
                         family_key("Tonari no Seki-kun EDv2"))
        self.assertNotEqual(family_key("Tonari no Seki-kun OP"),
                            family_key("Tonari no Seki-kun ED"))
        self.assertNotEqual(family_key("Daiya no Ace OP"),
                            family_key("Daiya no Ace OP 2v2"))
        self.assertEqual(family_key("Daiya no Ace OP 2"),
                         family_key("Daiya no Ace OP 2v2"))

    def test_actual_2014_op_variants_all_inherit_super_and_sleepy(self):
        b = Builder()
        b.load(SOURCE)
        report = b.apply(
            "| Tonari no Seki-kun | 2014 1. | OP | 1 | Meiwaku Spectacle | Kana Hanazawa | 1 | 1 | 0 | 1 |\n"
            "| Tonari no Seki-kun | 2014 1. | ED | 1 | Set Them Free | Akira Jimbo | 1 | 0 | 0 | 0 |\n"
            "| Kaitou Joker OP | 2014 4. | OP | 1 | Kaitou Miracle Shounen Boy | Arukarider | 1 | 1 | 1 | 0 |\n"
        )
        self.assertEqual(len(report.assignments), 3, report.text(len(b.source)))
        byname = {stem_from_track(t): idx for idx, t in enumerate(b.source)}
        for name in (
            "Tonari no Seki-kun OP",
            "Tonari no Seki-kun OPv2 EUROBEAT MIX",
            "Tonari no Seki-kun OPv3 Technopop MIX",
            "Tonari no Seki-kun OPv4 80'GAME SOUND MIX",
            "Tonari no Seki-kun OPv5 (A Capella Ver.) (TV Size)",
            "Tonari no Seki-kun OPv6 Ger",
            "Tonari no Seki-kun OPv7 Rus",
        ):
            self.assertIn(name, byname)
            self.assertEqual(b.flags[byname[name]], [1, 1, 0, 1], name)
            self.assertEqual(b.quarters[byname[name]], 1, name)
        self.assertEqual(b.flags[byname["Tonari no Seki-kun ED"]], [1, 0, 0, 0])
        self.assertEqual(b.flags[byname["Tonari no Seki-kun EDv2"]], [1, 0, 0, 0])
        self.assertEqual(b.flags[byname["Kaitou Joker OPv2"]], [1, 1, 1, 0])
        self.assertEqual(b.quarters[byname["Kaitou Joker OPv2"]], 4)

    def test_ui_changes_apply_to_entire_family(self):
        b = Builder()
        b.load(SOURCE)
        stems = [stem_from_track(t) for t in b.source]
        idx = stems.index("Tonari no Seki-kun OP")
        variants = [i for i, name in enumerate(stems)
                    if family_key(name) == family_key(stems[idx])]
        b.set_flag(idx, "Ultra", True)
        self.assertTrue(all(b.flags[i][2] for i in variants))
        b.set_flag(idx, "Ultra", False)
        self.assertTrue(all(not b.flags[i][2] for i in variants))

    def test_unmatched_variants_remain_best_without_automatic_rest(self):
        b = Builder()
        b.load(SOURCE)
        b.apply("| Kaitou Joker OP | 2014 Q4 | OP | 1 | Title | Artist | 1 | 1 | 1 | 0 |")
        stems = [stem_from_track(t) for t in b.source]
        version = stems.index("Kaitou Joker OPv2")
        self.assertEqual(b.flags[version], [1, 1, 1, 0])
        self.assertIn(b.source[version], b.rows("Ultra"))
        self.assertNotIn(b.source[version], b.rows("Rest"))


if __name__ == "__main__":
    unittest.main()
