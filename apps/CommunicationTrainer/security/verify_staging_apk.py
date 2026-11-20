#!/usr/bin/env python3
"""Fail-closed checks on an assembled CommunicationTrainer staging APK.

Checks the *compiled* binary manifest rather than relying on Gradle source.
Run with ANDROID_HOME and aapt from Android Build Tools 36.
Never interprets a clean manifest as a malware-free application.
"""
import os
from pathlib import Path
import re
import subprocess
import sys

EXPECTED_ID = "com.valoon4.communicationtrainer.d051staging"
EXPECTED_LABEL = "CommunicationTrainer Staging"

def main(apk: Path):
    if not apk.is_file() or apk.stat().st_size < 10000:
        raise RuntimeError("Staging APK missing or suspiciously small")
    sdk = Path(os.environ["ANDROID_HOME"])
    aapt = sdk / "build-tools/36.0.0/aapt"
    badging = subprocess.check_output([str(aapt), "dump", "badging", str(apk)], text=True)
    xmltree = subprocess.check_output([str(aapt), "dump", "xmltree", str(apk), "AndroidManifest.xml"], text=True)

    if not re.search(r"package: name='" + re.escape(EXPECTED_ID) + r"'", badging):
        raise RuntimeError("Unexpected staging package ID")
    if f"application-label:'{EXPECTED_LABEL}'" not in badging:
        raise RuntimeError("Unexpected staging label")
    if "targetSdkVersion:'36'" not in badging:
        raise RuntimeError("Unexpected targetSdk")

    # Inspect APPLICATION attributes, not unrelated library manifest values.
    lines = xmltree.splitlines()
    start = next((i for i, line in enumerate(lines)
                  if re.search(r"^\s*E: application(?:\s|$)", line)), None)
    if start is None:
        raise RuntimeError("Compiled application tag absent")
    indent = len(lines[start]) - len(lines[start].lstrip())
    attrs = []
    for line in lines[start + 1:]:
        depth = len(line) - len(line.lstrip())
        if depth <= indent and re.search(r"\bE:", line):
            break
        if depth == indent + 2 and "A:" in line:
            attrs.append(line)
    for line in attrs:
        if "android:debuggable" in line and not re.search(r"0x0(?:\s|$)", line):
            raise RuntimeError("App is debuggable")
        if "android:allowBackup" in line and not re.search(r"0x0(?:\s|$)", line):
            raise RuntimeError("Android backup unexpectedly enabled")

    if "androidx.compose.ui.tooling.PreviewActivity" in xmltree:
        raise RuntimeError("Exported Compose PreviewActivity embedded in staging")
    if 'android:debuggable' not in " ".join(attrs):
        print("Compiled staging manifest: no debuggable attribute (default false)")
    else:
        print("Compiled staging manifest: debuggable explicitly false")
    print("Compiled staging manifest: package, label, targetSdk, no PreviewActivity verified")

if __name__ == "__main__":
    if len(sys.argv) != 2:
        raise SystemExit("Usage: verify_staging_apk.py <compiled.apk>")
    main(Path(sys.argv[1]))
