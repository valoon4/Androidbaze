"""Regression tests for offline state and monotonic timer."""
import json
import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from core import AREAS, FocusTimer, clock_label, fresh_state, load_state, normalize_state, save_state


class StateTests(unittest.TestCase):
    def test_defaults_have_no_fake_progress_or_tasks(self):
        state=fresh_state()
        self.assertEqual(state["modules"]["Master"],["RP1","ITM1","LEAD1","DF1"])
        self.assertEqual(state["modules"]["Research"],[])
        self.assertEqual(state["modules"]["Exploring"],[])
        self.assertTrue(all(state["progress"][area] is None for area in AREAS))
        self.assertNotIn("tasks",state)
        self.assertNotIn("streaks",state)

    def test_bad_input_is_sanitized(self):
        state=normalize_state({
            "minutes":999,"area":"NotReal",
            "modules":{"Master":["RP1","RP1"," ITM1 ","",42],
                       "Research":["Live Action","live action"]},
            "selected":{"Master":"Missing"},
            "progress":{"Master":101,"Research":37,"Exploring":True},
        })
        self.assertEqual(state["minutes"],25)
        self.assertEqual(state["area"],"Master")
        self.assertEqual(state["modules"]["Master"],["RP1","ITM1"])
        self.assertEqual(state["modules"]["Research"],["Live Action"])
        self.assertEqual(state["selected"]["Master"],"RP1")
        self.assertIsNone(state["progress"]["Master"])
        self.assertEqual(state["progress"]["Research"],37)
        self.assertIsNone(state["progress"]["Exploring"])

    def test_save_load_and_corruption(self):
        with tempfile.TemporaryDirectory() as directory:
            path=Path(directory)/"AniFocus"/"state.json"
            state=fresh_state()
            state["progress"]["Master"]=0
            state["modules"]["Research"]=["OST"]
            state["selected"]["Research"]="OST"
            save_state(state,path)
            self.assertEqual(load_state(path),state)
            path.write_text("{not-json",encoding="utf-8")
            self.assertEqual(load_state(path),fresh_state())


class TimerTests(unittest.TestCase):
    def test_pause_resume_uses_elapsed_time(self):
        timer=FocusTimer(25)
        timer.start(now=100)
        self.assertEqual(timer.seconds_left(160),1440)
        timer.pause(now=160)
        self.assertEqual(timer.seconds_left(10000),1440)
        timer.start(now=200)
        self.assertEqual(timer.seconds_left(260),1380)
        self.assertFalse(timer.tick(1639))
        self.assertTrue(timer.tick(1640))
        self.assertFalse(timer.tick(1641))
        self.assertEqual(clock_label(timer.seconds_left()),"00:00")

    def test_reset_and_new_duration(self):
        timer=FocusTimer(25)
        timer.start(50)
        timer.reset(10)
        self.assertFalse(timer.running)
        self.assertEqual(clock_label(timer.seconds_left()),"10:00")
        timer.start(100)
        self.assertTrue(timer.tick(700))
        timer.start(800)
        self.assertEqual(timer.seconds_left(800),600)

    def test_formatting_and_validation(self):
        self.assertEqual(clock_label(0),"00:00")
        self.assertEqual(clock_label(60.1),"01:01")
        self.assertEqual(clock_label(-4),"00:00")
        with self.assertRaises(ValueError):
            FocusTimer(0)
        with self.assertRaises(ValueError):
            FocusTimer(True)


if __name__=="__main__":
    unittest.main()
