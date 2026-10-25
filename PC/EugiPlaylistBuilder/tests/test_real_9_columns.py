"""Real pasted 2014 nine-column research table regression."""
import unittest
from pathlib import Path

from research_table import parse_research_table, lookup_keys
from simple_builder import Builder

ROOT = Path(__file__).resolve().parents[1]
PASTE = """| Samurai Flamenco OP 2              | 2014 1. | OP | 1 | Ai Ai Ai ni Utarete Bye Bye Bye | FLOW             | 1 | 1 | 1 |
| ---------------------------------- | ------- | -- | - | ------------------------------- | ---------------- | - | - | - |
| Seitokai Yakuindomo 2              | 2014 1. | OP | 1 | Hanasaku Saikyou Legend Days    | Triple Booking   | 1 | 1 | 0 |
| Sekai Seifuku - Bouryaku no Zvezda | 2014 1. | OP | 1 | Be mine!                        | Maaya Sakamoto   | 1 | 0 | 0 |
| SoniAni - Super Sonico             | 2014 1. | OP | 1 | Super Soni                      | Super Sonico     | 1 | 1 | 0 |
| Space Dandy                        | 2014 1. | OP | 1 | Viva Namida                     | Yasuyuki Okamura | 1 | 1 | 1 |
| Strange Plus                       | 2014 1. | OP | 1 | Beauty Boy                      | Jun Fukuyama     | 1 | 0 | 0 |
| Strike the Blood OP 2              | 2014 1. | OP | 1 | Fight 4 Real                    | ALTIMA           | 1 | 1 | 0 |
| Tesagure! Bukatsumono Encore       | 2014 1. | OP | 1 | Chanto Stand Up!!!!             | Main Cast        | 1 | 0 | 0 |
| Toaru Hikuushi e no Koiuta         | 2014 1. | OP | 1 | azurite                         | petit milady     | 1 | 0 | 0 |
| Tokyo Ravens OP 2                  | 2014 1. | OP | 1 | Outgrow                         | Gero             | 1 | 0 | 0 |
| Tonari no Seki-kun                 | 2014 1. | OP | 1 | Meiwaku Spectacle               | Kana Hanazawa    | 1 | 1 | 0 |
| Uchuu Kyoudai OP 8                 | 2014 1. | OP | 1 | Big Bang                        | THE Yatou        | 1 | 1 | 1 |
| Wake Up, Girls! OP                 | 2014 1. | OP | 1 | Tachiagare!                     | Wake Up, Girls!  | 1 | 0 | 0 |
"""


class TestNineColumnResearch(unittest.TestCase):
    def test_actual_2014_research_all_thirteen_match(self):
        b = Builder()
        b.load(ROOT / "examples" / "2014-Unsorted.m3u")
        report = b.apply(PASTE)
        self.assertEqual(len(b.source), 517)
        self.assertEqual(report.input_count, 13)
        self.assertEqual(len(report.assignments), 13, report.text(len(b.source)))
        self.assertEqual(report.missing, [])
        self.assertEqual(report.ambiguous, [])
        self.assertEqual(b.counts(), [0, 517, 13, 3, 0])  # OPv2 ... OPv7 inherit Super
        self.assertEqual(len(b.rows("Ultra", 1)), 3)
        self.assertEqual(b.unassigned_quarter_count(), 498)  # six OP variants inherit known quarters

    def test_missing_sleepy_means_unset(self):
        rows = parse_research_table(PASTE)
        self.assertEqual(rows[0].flags, (1, 1, 1, 0))
        self.assertEqual(rows[1].flags, (1, 1, 0, 0))
        self.assertEqual(rows[0].kind, "OP")
        self.assertEqual(rows[0].song, "Ai Ai Ai ni Utarete Bye Bye Bye")
        self.assertEqual(rows[0].artist, "FLOW")

    def test_9_column_explicit_rest(self):
        row = "| Example | 2027 Q1 | OP | 1 | Song | Artist | 0 | 0 | 0 |"
        self.assertEqual(parse_research_table(row)[0].flags, (0, 0, 0, 0))

    def test_10_column_explicit_sleepy(self):
        row = "| Example | 2027 Q1 | OP | 1 | Song | Artist | 1 | 0 | 0 | 1 |"
        self.assertEqual(parse_research_table(row)[0].flags, (1, 0, 0, 1))

    def test_prefer_explicit_op2_over_first_op(self):
        row = parse_research_table(
            "| Samurai Flamenco OP 2 | 2014 1. | OP | 1 | Song | Artist | 1 | 1 | 1 |"
        )[0]
        keys = lookup_keys(row)
        self.assertEqual(len(keys), 1)


if __name__ == "__main__":
    unittest.main()
