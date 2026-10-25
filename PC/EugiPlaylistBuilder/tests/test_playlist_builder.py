import tempfile
import unittest
from pathlib import Path
from playlist_core import parse_m3u, path_key, render_m3u
from playlist_model import PlaylistProject

SOURCE = '#EXTM3U\n#EXTINF:307,Yui Horie - The Worlds End\nfile:///F:/Musik/2014/Golden%20Time%20OP%202.flac\n#EXTINF:255,Arukarider - Kaitou Miracle Shounen Boy\nfile:///F:/Musik/2014/Kaitou%20Joker%20OP.mp3\n#EXTINF:257,Arukarider - Kaitou Miracle Shounen Boy (Remix)\nfile:///F:/Musik/2014/Kaitou%20Joker%20OPv2.mp3\n'
ULTRA = '#EXTM3U\n#EXTINF:255,Arukarider - Kaitou Miracle Shounen Boy\nfile:///F:/Musik/2014/Kaitou%20Joker%20OP.mp3\n#EXTINF:307,Yui Horie - The Worlds End\nfile:///F:/Musik/2014/Golden%20Time%20OP%202.flac\n'


class TestPlaylistBuilder(unittest.TestCase):
    def setup_files(self, tmp):
        s = Path(tmp) / 'Unsorted.m3u'
        u = Path(tmp) / 'Ultra.m3u'
        s.write_text(SOURCE, encoding='utf-8')
        u.write_text(ULTRA, encoding='utf-8')
        return s, u

    def test_2014_path_escaping(self):
        self.assertEqual(path_key('file:///F:/Musik/2014/Golden%20Time%20OP%202.flac'),
                         path_key('F:\\Musik\\2014\\Golden Time OP 2.flac'))

    def test_preserve_variant_and_binary_flags(self):
        with tempfile.TemporaryDirectory() as tmp:
            s, u = self.setup_files(tmp)
            p = PlaylistProject()
            p.load_unsorted(s)
            result = p.import_tier(u, 'Ultra')
            self.assertEqual((result.matched, len(result.missing)), (2, 0))
            self.assertEqual(p.counts(), [1, 0, 0, 2, 0])
            self.assertEqual([t.title for t in p.rows_for_level(0)], ['Arukarider - Kaitou Miracle Shounen Boy (Remix)'])
            p.set_flag(2, 'Best', True)
            self.assertEqual(p.counts(), [0, 1, 0, 2, 0])
            exports = p.export(Path(tmp) / 'out', '2014')
            self.assertEqual(len(exports), 5)
            actual = exports[3].read_text('utf-8-sig')
            self.assertEqual(actual, render_m3u([p.source[0], p.source[1]]))
            self.assertTrue((Path(tmp) / 'out' / '2014_Zuordnung.csv').exists())

    def test_unknown_reference_not_guessed(self):
        with tempfile.TemporaryDirectory() as tmp:
            s, u = self.setup_files(tmp)
            u.write_text('#EXTM3U\n#EXTINF:123,Yui Horie - The Worlds End\nfile:///F:/Musik/2014/DIFFERENT.flac\n', encoding='utf-8')
            p = PlaylistProject()
            p.load_unsorted(s)
            report = p.import_tier(u, 'Ultra')
            self.assertEqual(report.matched, 0)
            self.assertEqual(len(report.missing), 1)
            self.assertEqual(p.counts(), [3, 0, 0, 0, 0])

    def test_save_and_restore(self):
        with tempfile.TemporaryDirectory() as tmp:
            s, _ = self.setup_files(tmp)
            p = PlaylistProject()
            p.load_unsorted(s)
            p.set_flag(0, 'Mega', True)
            path = Path(tmp) / 'project.json'
            p.save(path)
            q = PlaylistProject()
            q.load_project(path)
            self.assertEqual(q.flags, p.flags)

    def test_parse_rejects_broken_entry(self):
        with self.assertRaises(ValueError):
            parse_m3u('#EXTM3U\n#EXTINF:123,Oops\n')


if __name__ == '__main__':
    unittest.main()
