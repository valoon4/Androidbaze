import tempfile
import unittest
from pathlib import Path
from movie_cleaner import clean_name, disc_number, scan, subtitle_parts

class TestCleaner(unittest.TestCase):
    def test_names(self):
        self.assertEqual(clean_name("Soviel.Wind.und.keine.Segel.1981.German.DVDRip.X264","1981")[0],"Soviel Wind und keine Segel")
        self.assertEqual(clean_name("Wunder auf dem Eis 1981","1981")[0],"Wunder auf dem Eis")
        self.assertEqual(clean_name("rsg-towerdeath-1080p","1981")[0],"")
        self.assertIn("Jahr ",clean_name("Film.1980.German.DVDRip","1981")[1])
        self.assertEqual(disc_number("etm-dfx-cd2"),2)
        self.assertTrue(subtitle_parts(Path("movie.forced.srt")) is None)
        self.assertEqual(subtitle_parts(Path("movie.DE.srt"))[1],"deu")

    def test_discs(self):
        with tempfile.TemporaryDirectory() as d:
            root=Path(d)/"1981"
            folder=root/"Die.Fälschung.1981.German.DVDRip.XVID-ETM"
            folder.mkdir(parents=True)
            for n in (1,2):(folder/f"etm-dfx-cd{n}.avi").touch()
            jobs=scan(root,False)
            self.assertEqual([j.target.name for j in jobs],["Die Fälschung (1).avi","Die Fälschung (2).avi"])
            self.assertEqual([j.status for j in jobs],["Umbenennen","Umbenennen"])

    def test_subtitles(self):
        with tempfile.TemporaryDirectory() as d:
            root=Path(d)/"1981"
            folder=root/"Die.Sensationsreporterin.German.DL.1981.DVDRip.Xvid-FF"
            folder.mkdir(parents=True)
            name="Die.Sensationsreporterin.German.DL.1981.DVDRip.Xvid-FF"
            (folder/(name+".avi")).touch()
            for n in (".DE.srt",".ENG.srt",".forced.srt"):
                (folder/(name+n)).touch()
            jobs=scan(root,True)
            self.assertEqual(len(jobs),1)
            self.assertEqual(jobs[0].status,"Muxen")
            self.assertEqual(len(jobs[0].subs),2)
            self.assertEqual(jobs[0].target.name,"Die Sensationsreporterin.mkv")

    def test_collision(self):
        with tempfile.TemporaryDirectory() as d:
            root=Path(d)/"1981"
            root.mkdir()
            for name in ("A","B"):
                folder=root/name
                folder.mkdir()
                (folder/"Filmname.1981.German.DVDRip.avi").touch()
            jobs=scan(root,False)
            self.assertEqual([j.status for j in jobs],["Prüfen","Prüfen"])

if __name__=="__main__":
    unittest.main()
