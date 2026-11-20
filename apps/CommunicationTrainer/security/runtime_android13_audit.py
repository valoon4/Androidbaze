#!/usr/bin/env python3
"""Run original APK in an isolated Android 13 AVD without credentials.

Only summarized observations are persisted. Do not upload full logcat, UI
hierarchies, screenshots, packets, tokens, or Android userdata.
"""
import json
import re
import subprocess
import time
from pathlib import Path
from xml.etree import ElementTree as ET

APK = "runtime-private/CommunicationTrainer-D051-Lernhilfe-debug.apk"
PACKAGE = "com.valoon4.communicationtrainer.d051guidedpreview"
ACTIVITY = "com.valoon4.communicationtrainer.MainActivity"
RESULT = Path("runtime-reports/emulator-observations.json")

def adb(*args, timeout=45, check=True):
    result = subprocess.run(["adb", *args], capture_output=True, text=True, timeout=timeout)
    if check and result.returncode:
        raise RuntimeError("ADB failed (%s): %s" % (" ".join(args[:2]), result.stderr[-350:]))
    return result.stdout

def view_xml():
    result = adb("shell", "uiautomator", "dump", "/sdcard/runtime-window.xml", timeout=35, check=False)
    if "dumped to" not in result and "dumped to" not in result.lower():
        # uiautomator often still writes the XML despite printing a warning.
        pass
    xml = adb("shell", "cat", "/sdcard/runtime-window.xml", check=False)
    if "<hierarchy" not in xml:
        return None
    return ET.fromstring(xml[xml.index("<hierarchy"):])

def find_node(text):
    root = view_xml()
    if root is None:
        return None
    for node in root.iter("node"):
        label = node.attrib.get("text", "") + " " + node.attrib.get("content-desc", "")
        if text in label:
            bounds = node.attrib.get("bounds", "")
            match = re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", bounds)
            if match:
                x0, y0, x1, y1 = map(int, match.groups())
                return (x0+x1)//2, (y0+y1)//2
    return None

def press_text(text, scroll=False):
    for attempt in range(6 if scroll else 2):
        coords = find_node(text)
        if coords:
            adb("shell", "input", "tap", *map(str, coords))
            time.sleep(2)
            return True
        if scroll:
            adb("shell", "input", "swipe", "420", "1450", "420", "560", "380")
            time.sleep(1)
    return False

def active_process():
    return bool(adb("shell", "pidof", PACKAGE, check=False).strip())

def safe_component_summary():
    package_dump = adb("shell", "dumpsys", "package", PACKAGE, timeout=30, check=False)
    perms = sorted(set(re.findall(r"android\.permission\.[A-Z_]+", package_dump)))
    return {
        "package_present": PACKAGE in adb("shell", "pm", "list", "packages", "-3"),
        "requested_android_permissions_in_dumpsys": perms,
        "is_active_process": active_process(),
    }

def collect_service_running():
    # Only count explicit app service mentions, never upload process dump.
    services = adb("shell", "dumpsys", "activity", "services", PACKAGE, timeout=30, check=False)
    return "ChatGptKeepAliveService" in services

def run():
    RESULT.parent.mkdir(parents=True, exist_ok=True)
    report = {
        "scope": "Android 13 x86_64 google_apis emulator, original hash-pinned Play-Protect-blocked D05.1 debug APK",
        "play_protect_available": False,
        "play_protect_reproduction": "not_tested; google_apis emulator is not a certified Google Play Store device",
        "real_user_credentials": False,
        "external_account_sign_in": False,
        "app_ai_request": False,
        "limitations": ["No real ChatGPT account login or AI transmission",
                        "Emulator x86_64 is not the user's Samsung ARM device",
                        "Socket polling can miss short-lived connections",
                        "Full packet capture includes emulator system processes, so app attribution is limited"],
        "phases_unix": {},
        "events": [],
    }
    try:
        report["phases_unix"]["baseline_start"] = time.time()
        time.sleep(8)
        report["phases_unix"]["baseline_end"] = time.time()
        adb("install", "-r", APK, timeout=80)
        report["events"].append("installed_original_apk_without_play_protect_override")
        report["package"] = safe_component_summary()
        adb("shell", "am", "start", "-n", PACKAGE + "/" + ACTIVITY)
        report["phases_unix"]["app_open_start"] = time.time()
        time.sleep(10)
        report["phases_unix"]["app_open_end"] = time.time()
        report["app_launch"] = {
            "process_alive_after_10_seconds": active_process(),
            "unexpected_oauth_service_after_launch": collect_service_running(),
        }
        if not report["app_launch"]["process_alive_after_10_seconds"]:
            raise RuntimeError("Original app process not alive after launch")
        if report["app_launch"]["unexpected_oauth_service_after_launch"]:
            raise RuntimeError("OAuth foreground service started before user action")

        report["ui_navigation"] = {"practice_tab_found": press_text("Üben")}
        if report["ui_navigation"]["practice_tab_found"]:
            time.sleep(1)
            button = press_text("Lokale Browser-Rückleitung testen", scroll=True)
            report["ui_navigation"]["offline_browser_test_button_found"] = button
            if button:
                report["phases_unix"]["local_callback_start"] = time.time()
                seen_service = False
                for i in range(8):
                    time.sleep(1)
                    seen_service = seen_service or collect_service_running()
                adb("shell", "input", "keyevent", "4", check=False)
                time.sleep(2)
                root = view_xml()
                visible_text = " ".join((x.attrib.get("text", "") for x in root.iter("node"))) if root is not None else ""
                report["phases_unix"]["local_callback_end"] = time.time()
                report["local_browser_callback"] = {
                    "foreground_service_observed": seen_service,
                    "success_text_seen": "Browser-Rückleitung erfolgreich" in visible_text,
                    "failure_text_seen": "Rückleitung" in visible_text and "fehlgeschlagen" in visible_text,
                    "service_still_running_after_test": collect_service_running(),
                }
        report["phases_unix"]["end"] = time.time()
        report["app_process_survived"] = active_process()
        # No raw logcat; only a filtered crash indicator.
        logs = adb("logcat", "-d", "-s", "AndroidRuntime:E", timeout=35, check=False)
        report["crash_indicator"] = "FATAL EXCEPTION" in logs and PACKAGE in logs
        if report["crash_indicator"]:
            raise RuntimeError("App-related FATAL EXCEPTION in AndroidRuntime")
        if not report["ui_navigation"].get("practice_tab_found"):
            report["limitations"].append("Practice UI not accessible using accessibility tree; browser-only check incomplete")
        if not report.get("local_browser_callback", {}).get("success_text_seen"):
            report["limitations"].append("Local browser callback success not confirmed; OAuth lifecycle only partially tested")
        report["status"] = "completed_with_limitations"
    except Exception as exc:
        report["status"] = "failed_or_incomplete"
        report["failure_type"] = type(exc).__name__
        report["failure_summary"] = str(exc)[:240]
        raise
    finally:
        RESULT.write_text(json.dumps(report, indent=2, ensure_ascii=False), encoding="utf-8")
        print("Runtime analysis:", report["status"])
        print("Report:", RESULT)

if __name__ == "__main__":
    run()
