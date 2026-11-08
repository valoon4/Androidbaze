package main

import (
	"encoding/json"
	"errors"
	"fmt"
	"io/fs"
	"os"
	"os/exec"
	"path/filepath"
	"regexp"
	"sort"
	"strconv"
	"strings"
	"time"
	"unicode"
)

// The engine uses only Go's standard library. It does not need a network connection.
// A separate MKVToolNix installation is required for subtitle embedding.
type Subtitle struct {
	Path   string
	Lang   string
	Binary string // .idx/.sub is a single VobSub input to mkvmerge.
}

type Task struct {
	Source string
	Target string
	Title  string
	Status string
	Reason string
	Subs   []Subtitle
	Disc   int
	Manual bool
}

type Cleaner struct {
	Root  string
	Year  string
	MKV   string
	Tasks []*Task
}

var videoExt = map[string]bool{".mkv": true, ".avi": true, ".mp4": true, ".m4v": true, ".mov": true, ".wmv": true, ".mpg": true, ".mpeg": true, ".ts": true, ".m2ts": true, ".webm": true}
var subtitleExt = map[string]bool{".srt": true, ".ass": true, ".ssa": true, ".idx": true}
var ignoredDirs = map[string]bool{"_eugicleaner_backup": true, "_eugicleaner_logs": true, "$recycle.bin": true, "system volume information": true}
var yearsRE = regexp.MustCompile(`^(19|20)\d{2}$`)
var discRE = regexp.MustCompile(`(?i)(?:^|[\s._-])(?:cd|disc|disk|teil|part)[\s._-]*([1-9])(?:$|[\s._-])`)
var episodeRE = regexp.MustCompile(`(?i)(?:^|[\s._-])(?:s\d{1,2}e\d{1,3}|e\d{1,3})(?:$|[\s._-])`)
var forcedRE = regexp.MustCompile(`(?i)(?:^|[\s._-])forced(?:$|[\s._-])`)
var codeRE = regexp.MustCompile(`(?i)^(?:tt\d{6,}|[a-z0-9]{2,6}-[a-z0-9-]+)$`)
var discTokenRE = regexp.MustCompile(`(?i)^(?:cd|disc|disk|part|teil)\d+$`)
var resolutionRE = regexp.MustCompile(`(?i)^\d{3,4}p$`)
var wordsAlphaRE = regexp.MustCompile(`[^\pL\pN]+`)
var genericTitles = map[string]bool{"teil": true, "part": true, "movie": true, "film": true, "sample": true, "video": true, "disc": true, "disk": true, "dvd": true, "cd": true, "unbekannt": true}
var metaWords = map[string]bool{}

func init() {
	for _, s := range strings.Fields(`german english deutsch dl dubbed dts ac3 eac3 aac mp3 truehd atmos ddp dvdrip bdrip brrip bluray blu-ray bdremux remux webdl web-rip webrip web hdtv tvrip tv-rip 1080p 2160p 720p 480p x264 x265 h264 h265 hevc xvid divx avc hdr 10bit proper repack remastered internal limited readnfo uncut extended watchable contribution inklusion ff etm owl 5.1 2.0 6ch 8ch multi subbed complete ws pal ntsc tvrip`) {
		metaWords[s] = true
	}
}

func discNumber(s string) int {
	match := discRE.FindStringSubmatch(s)
	if len(match) < 2 {
		return 0
	}
	n, _ := strconv.Atoi(match[1])
	return n
}

func cleanTitle(s, year string) (string, string) {
	if episodeRE.MatchString(s) {
		return "", "Episoden-/Serienkennung: manuell prüfen"
	}
	if strings.Contains(strings.ToLower(s), "sample") && (strings.Contains(strings.ToLower(s), ".sample") || strings.Contains(strings.ToLower(s), "-sample")) {
		return "", "Sample-Datei"
	}
	if codeRE.MatchString(s) {
		return "", "Releasecode oder ID statt Filmtitel"
	}
	replaced := strings.NewReplacer(".", " ", "_", " ").Replace(s)
	words := strings.Fields(replaced)
	title := make([]string, 0, len(words))
	warning := ""
	for _, word := range words {
		w := strings.ToLower(strings.Trim(word, " [](){}"))
		if yearsRE.MatchString(w) {
			if w != year {
				warning = "Jahr im Namen passt nicht zum Jahresordner (" + w + " statt " + year + ")"
			}
			break
		}
		if metaWords[w] || resolutionRE.MatchString(w) || discTokenRE.MatchString(w) || strings.HasPrefix(w, "ddp5") {
			break
		}
		title = append(title, word)
	}
	result := strings.Trim(strings.Join(title, " "), " ._-()")
	if result == "" || utf8Length(result) <= 3 || genericTitles[strings.ToLower(result)] || strings.EqualFold(result, "Teil 1") {
		return "", "Kein eindeutiger Filmtitel"
	}
	if codeRE.MatchString(result) {
		return "", "Kryptischer Releasecode"
	}
	if strings.Contains(result, "-") && !strings.Contains(result, " ") && regexp.MustCompile(`(?i)^[a-z0-9]{2,6}-`).MatchString(result) {
		return "", "Kryptischer Dateiname"
	}
	r := []rune(result)
	if len(r) > 0 {
		r[0] = unicode.ToUpper(r[0])
	}
	return string(r), warning
}

func utf8Length(s string) int { return len([]rune(s)) }

func normalize(s string) string { return strings.ToLower(wordsAlphaRE.ReplaceAllString(s, "")) }

func subtitleInfo(path string) (stem, lang string, forced bool) {
	stem = strings.TrimSuffix(filepath.Base(path), filepath.Ext(path))
	forced = forcedRE.MatchString(stem)
	stem = regexp.MustCompile(`(?i)[._ -]forced(?:$|[._ -])`).ReplaceAllString(stem, "")
	lang = "und"
	sep := regexp.MustCompile(`[._ -]+`)
	parts := sep.Split(stem, -1)
	if len(parts) > 0 {
		last := strings.ToLower(parts[len(parts)-1])
		languages := map[string]string{"de": "deu", "ger": "deu", "deu": "deu", "german": "deu", "en": "eng", "eng": "eng", "english": "eng"}
		if language, ok := languages[last]; ok {
			lang = language
			// Trim the language token and its preceding delimiter.
			stem = strings.TrimRight(stem[:len(stem)-len(parts[len(parts)-1])], "._ -")
		}
	}
	return stem, lang, forced
}

func getSubtitles(video string, videos []string) []Subtitle {
	entries, err := os.ReadDir(filepath.Dir(video))
	if err != nil {
		return nil
	}
	result := []Subtitle{}
	vb := normalize(strings.TrimSuffix(filepath.Base(video), filepath.Ext(video)))
	vbTitle, _ := cleanTitle(strings.TrimSuffix(filepath.Base(video), filepath.Ext(video)), "0000")
	for _, e := range entries {
		if e.IsDir() {
			continue
		}
		f := filepath.Join(filepath.Dir(video), e.Name())
		ext := strings.ToLower(filepath.Ext(f))
		if !subtitleExt[ext] {
			continue
		}
		stem, lang, forced := subtitleInfo(f)
		if forced {
			continue
		}
		binary := ""
		if ext == ".idx" {
			binary = strings.TrimSuffix(f, filepath.Ext(f)) + ".sub"
			if _, err := os.Stat(binary); err != nil {
				continue
			}
		}
		sn := normalize(stem)
		if len(videos) > 1 {
			if sn != vb && !(len(sn) > 8 && strings.Contains(vb, sn)) {
				continue
			}
		} else {
			subTitle, _ := cleanTitle(stem, "0000")
			generic := sn == "subtitle" || sn == "subtitles" || sn == "de" || sn == "eng" || sn == "en" || sn == "ger"
			if !generic && sn != vb && !strings.Contains(vb, sn) && !strings.Contains(sn, vb) && !(normalize(vbTitle) == normalize(subTitle) && vbTitle != "") {
				continue
			}
		}
		result = append(result, Subtitle{Path: f, Lang: lang, Binary: binary})
	}
	sort.Slice(result, func(i, j int) bool { return strings.ToLower(result[i].Path) < strings.ToLower(result[j].Path) })
	return result
}

func findMKVmerge() string {
	for _, name := range []string{"mkvmerge.exe", "mkvmerge"} {
		if p, e := exec.LookPath(name); e == nil {
			return p
		}
	}
	for _, p := range []string{`C:\Program Files\MKVToolNix\mkvmerge.exe`, `C:\Program Files (x86)\MKVToolNix\mkvmerge.exe`} {
		if info, e := os.Stat(p); e == nil && !info.IsDir() {
			return p
		}
	}
	return ""
}

func NewCleaner(root string) (*Cleaner, error) {
	abs, err := filepath.Abs(strings.TrimSpace(root))
	if err != nil {
		return nil, err
	}
	info, err := os.Stat(abs)
	if err != nil {
		return nil, err
	}
	if !info.IsDir() {
		return nil, errors.New("Bitte einen Jahresordner auswählen")
	}
	year := filepath.Base(filepath.Clean(abs))
	if !yearsRE.MatchString(year) {
		return nil, fmt.Errorf("Ordnername muss ein Jahr sein (z.B. 1981), aktuell: %q", year)
	}
	c := &Cleaner{Root: abs, Year: year, MKV: findMKVmerge()}
	if err := c.Scan(); err != nil {
		return nil, err
	}
	return c, nil
}

func (c *Cleaner) Scan() error {
	files := []string{}
	folders := map[string][]string{}
	err := filepath.WalkDir(c.Root, func(path string, ent fs.DirEntry, err error) error {
		if err != nil {
			return err
		}
		if ent.Type()&os.ModeSymlink != 0 {
			if ent.IsDir() {
				return filepath.SkipDir
			}
			return nil
		}
		if ent.IsDir() {
			if path != c.Root && ignoredDirs[strings.ToLower(ent.Name())] {
				return filepath.SkipDir
			}
			return nil
		}
		if !ent.Type().IsRegular() {
			return nil
		}
		if !videoExt[strings.ToLower(filepath.Ext(path))] {
			return nil
		}
		files = append(files, path)
		folders[filepath.Dir(path)] = append(folders[filepath.Dir(path)], path)
		return nil
	})
	if err != nil {
		return err
	}
	sort.Slice(files, func(i, j int) bool { return strings.ToLower(files[i]) < strings.ToLower(files[j]) })
	c.Tasks = nil
	for _, path := range files {
		basename := strings.TrimSuffix(filepath.Base(path), filepath.Ext(path))
		title, reason := cleanTitle(basename, c.Year)
		disc := discNumber(basename)
		if filepath.Dir(path) != c.Root {
			ptitle, preason := cleanTitle(filepath.Base(filepath.Dir(path)), c.Year)
			if (title == "" || disc != 0 || strings.EqualFold(basename, "Teil 1") || strings.EqualFold(basename, "Teil 2")) && ptitle != "" {
				title = ptitle
				reason = preason
			}
			if strings.Contains(reason, "Jahr im Namen") && strings.Contains(reason, "statt") { /* preserve actual year warning */
			}
		}
		subs := getSubtitles(path, folders[filepath.Dir(path)])
		t := &Task{Source: path, Title: title, Reason: reason, Subs: subs, Disc: disc}
		c.retarget(t)
		c.Tasks = append(c.Tasks, t)
	}
	c.checkCollisions()
	return nil
}

func (c *Cleaner) retarget(t *Task) {
	if t.Title == "" || t.Reason != "" {
		t.Target = ""
		t.Status = "Prüfen"
		if t.Reason == "" {
			t.Reason = "Filmtitel unklar"
		}
		return
	}
	name := t.Title
	if t.Disc != 0 {
		name = fmt.Sprintf("%s (%d)", name, t.Disc)
	}
	ext := filepath.Ext(t.Source)
	if len(t.Subs) > 0 {
		ext = ".mkv"
	}
	t.Target = filepath.Join(c.Root, name+ext)
	if len(t.Subs) > 0 && c.MKV == "" {
		t.Status = "Prüfen"
		t.Reason = "MKVToolNix nicht gefunden"
		return
	}
	if filepath.Clean(t.Target) == filepath.Clean(t.Source) && len(t.Subs) == 0 {
		t.Status = "Fertig"
		t.Reason = "Bereits korrekt benannt"
		return
	}
	if len(t.Subs) > 0 {
		t.Status = "Muxen"
	} else {
		t.Status = "Umbenennen"
	}
	t.Reason = ""
}

func exists(path string) bool   { _, err := os.Stat(path); return err == nil }
func samePath(a, b string) bool { return strings.EqualFold(filepath.Clean(a), filepath.Clean(b)) }
func (c *Cleaner) checkCollisions() {
	counter := map[string]int{}
	for _, t := range c.Tasks {
		if t.Target != "" {
			counter[strings.ToLower(filepath.Clean(t.Target))]++
		}
	}
	for _, t := range c.Tasks {
		if t.Target == "" || !(t.Status == "Umbenennen" || t.Status == "Muxen") {
			continue
		}
		if counter[strings.ToLower(filepath.Clean(t.Target))] > 1 || (exists(t.Target) && !samePath(t.Source, t.Target)) {
			t.Status = "Prüfen"
			t.Reason = "Zieldatei bereits vorhanden / doppelter Zielname"
		}
	}
}

func validTitle(s string) bool {
	if s == "" || s == "." || s == ".." {
		return false
	}
	if strings.TrimSpace(s) != s {
		return false
	}
	if strings.ContainsAny(s, "<>:\"/\\|?*\r\n\t") {
		return false
	}
	if strings.HasSuffix(s, ".") || strings.HasSuffix(s, " ") {
		return false
	}
	upper := strings.ToUpper(s)
	for _, reserved := range []string{"CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"} {
		if strings.SplitN(upper, ".", 2)[0] == reserved {
			return false
		}
	}
	return true
}

func (c *Cleaner) Approve(index int, title string) error {
	if index < 0 || index >= len(c.Tasks) {
		return errors.New("Bitte Film aus Liste auswählen")
	}
	title = strings.TrimSpace(title)
	if !validTitle(title) {
		return errors.New("Ungültiger Filmtitel (keine Pfadzeichen, Sondernamen oder Leerzeichen am Ende)")
	}
	t := c.Tasks[index]
	t.Title = title
	t.Reason = ""
	t.Manual = true
	c.retarget(t)
	c.checkCollisions()
	return nil
}

func backupFile(c *Cleaner, source string) error {
	relative, err := filepath.Rel(c.Root, source)
	if err != nil {
		return err
	}
	if relative == ".." || strings.HasPrefix(relative, ".."+string(filepath.Separator)) {
		return fmt.Errorf("Quelle außerhalb Jahresordner: %s", source)
	}
	target := filepath.Join(c.Root, "_EugiCleaner_Backup", relative)
	if exists(target) {
		return fmt.Errorf("Backup existiert bereits: %s", target)
	}
	if err := os.MkdirAll(filepath.Dir(target), 0755); err != nil {
		return err
	}
	return os.Rename(source, target)
}

func (c *Cleaner) RunOne(t *Task) error {
	if t.Target == "" || !(t.Status == "Umbenennen" || t.Status == "Muxen") {
		return errors.New("Eintrag ist nicht freigegeben")
	}
	if exists(t.Target) && !samePath(t.Source, t.Target) {
		return fmt.Errorf("Ziel bereits vorhanden: %s", t.Target)
	}
	if !exists(t.Source) {
		return fmt.Errorf("Quelldatei fehlt: %s", t.Source)
	}
	if t.Status == "Umbenennen" {
		if samePath(t.Source, t.Target) {
			t.Status = "Fertig"
			return nil
		}
		if err := os.Rename(t.Source, t.Target); err != nil {
			return err
		}
		t.Status = "Fertig"
		t.Reason = "Umbenannt und in Jahresordner verschoben"
		return nil
	}
	if c.MKV == "" {
		return errors.New("MKVToolNix nicht gefunden")
	}
	tmp := filepath.Join(c.Root, fmt.Sprintf(".EugiMovieCleaner-PARTIAL-%d.mkv", time.Now().UnixNano()))
	defer os.Remove(tmp)
	args := []string{"-o", tmp, t.Source}
	for _, sub := range t.Subs {
		args = append(args, "--language", "0:"+sub.Lang, "--default-track-flag", "0:no", "--forced-display-flag", "0:no", sub.Path)
	}
	out, err := exec.Command(c.MKV, args...).CombinedOutput()
	if err != nil {
		return fmt.Errorf("mkvmerge fehlgeschlagen: %s; %s", err, tail(string(out), 500))
	}
	f, err := os.Stat(tmp)
	if err != nil {
		return err
	}
	if f.Size() < 1024 {
		return errors.New("MKV-Ausgabe auffällig klein")
	}
	raw, err := exec.Command(c.MKV, "-J", tmp).Output()
	if err != nil {
		return fmt.Errorf("Ausgabe nicht lesbar: %w", err)
	}
	var probe struct {
		Tracks []struct {
			Type string `json:"type"`
		} `json:"tracks"`
	}
	if err := json.Unmarshal(raw, &probe); err != nil {
		return err
	}
	subtitleCount := 0
	videoCount := 0
	for _, track := range probe.Tracks {
		if track.Type == "subtitles" {
			subtitleCount++
		}
		if track.Type == "video" {
			videoCount++
		}
	}
	if videoCount < 1 || subtitleCount < len(t.Subs) {
		return errors.New("Validierung fehlgeschlagen: Videospur oder Untertitel fehlen")
	}
	// If an MKV is already perfectly named, the output path equals its input.
	// Back up the input BEFORE publishing the validated new MKV, and roll back
	// if publication fails. Never overwrite original data in place.
	if samePath(t.Source, t.Target) {
		rel, err := filepath.Rel(c.Root, t.Source)
		if err != nil {
			return err
		}
		backup := filepath.Join(c.Root, "_EugiCleaner_Backup", rel)
		if exists(backup) {
			return fmt.Errorf("Backup existiert bereits: %s", backup)
		}
		if err := os.MkdirAll(filepath.Dir(backup), 0755); err != nil {
			return err
		}
		if err := os.Rename(t.Source, backup); err != nil {
			return err
		}
		if err := os.Rename(tmp, t.Target); err != nil {
			if restoreErr := os.Rename(backup, t.Source); restoreErr != nil {
				return fmt.Errorf("Ausgabe fehlgeschlagen (%v); Original liegt in %s: %v", err, backup, restoreErr)
			}
			return err
		}
	} else {
		if exists(t.Target) {
			return fmt.Errorf("Zieldatei inzwischen vorhanden: %s", t.Target)
		}
		if err := os.Rename(tmp, t.Target); err != nil {
			return err
		}
	}
	t.Status = "Fertig"
	t.Reason = "MKV erzeugt, Originaldateien ins Backup verschoben"
	used := []string{}
	if !samePath(t.Source, t.Target) {
		used = append(used, t.Source)
	}
	for _, sub := range t.Subs {
		used = append(used, sub.Path)
		if sub.Binary != "" {
			used = append(used, sub.Binary)
		}
	}
	for _, source := range used {
		if err := backupFile(c, source); err != nil {
			t.Reason = "MKV fertig; Backup teilweise offen: " + err.Error()
			break
		}
	}
	return nil
}

func tail(s string, n int) string {
	r := []rune(s)
	if len(r) > n {
		r = r[len(r)-n:]
	}
	return string(r)
}

type History struct {
	Created string              `json:"created"`
	Year    string              `json:"year"`
	Results []map[string]string `json:"results"`
}

func (c *Cleaner) RunReady() (success int, fail int, notes []string) {
	seenTargets := map[string]bool{}
	for _, t := range c.Tasks {
		if t.Status != "Umbenennen" && t.Status != "Muxen" {
			continue
		}
		key := strings.ToLower(filepath.Clean(t.Target))
		if seenTargets[key] {
			t.Status = "Prüfen"
			t.Reason = "Ziel doppelt"
			fail++
			continue
		}
		seenTargets[key] = true
		if err := c.RunOne(t); err != nil {
			t.Status = "Fehler"
			t.Reason = err.Error()
			notes = append(notes, filepath.Base(t.Source)+": "+err.Error())
			fail++
		} else {
			success++
		}
	}
	c.writeHistory()
	return
}
func (c *Cleaner) writeHistory() {
	dir := filepath.Join(c.Root, "_EugiCleaner_Logs")
	if err := os.MkdirAll(dir, 0755); err != nil {
		return
	}
	h := History{Created: time.Now().Format(time.RFC3339), Year: c.Year}
	for _, t := range c.Tasks {
		h.Results = append(h.Results, map[string]string{"source": t.Source, "target": t.Target, "status": t.Status, "reason": t.Reason})
	}
	bytes, err := json.MarshalIndent(h, "", "  ")
	if err != nil {
		return
	}
	_ = os.WriteFile(filepath.Join(dir, "run-"+time.Now().Format("20060102-150405")+".json"), bytes, 0644)
}

func (c *Cleaner) Summary() string {
	counts := map[string]int{}
	for _, t := range c.Tasks {
		counts[t.Status]++
	}
	mkv := "gefunden"
	if c.MKV == "" {
		mkv = "nicht gefunden (für Untertitel erforderlich)"
	}
	return fmt.Sprintf("%d Filme | Umbenennen: %d | Muxen: %d | Prüfen: %d | Fertig: %d | Fehler: %d | MKVToolNix: %s", len(c.Tasks), counts["Umbenennen"], counts["Muxen"], counts["Prüfen"], counts["Fertig"], counts["Fehler"], mkv)
}