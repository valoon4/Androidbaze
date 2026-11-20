#!/usr/bin/env python3
"""Query OSV for every resolved Android debug runtime Maven dependency.

Sends only Maven package names and versions to api.osv.dev; NEVER APK bytes
or credentials. A scanner/API error is a separate non-green state, never
reported as 'no vulnerabilities'.
"""
import json
import os
import re
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path

PATTERN = re.compile(r"^SECURITY_DEP\s+([^ ]+)\s+([^ ]+)\s+([^ ]+)\s*$")

def collect_modules(path: Path):
    data = []
    for line in path.read_text(encoding="utf-8", errors="replace").splitlines():
        matched = PATTERN.match(line.strip())
        if matched:
            data.append(matched.groups())
    return sorted(set(data))

def query_osv(modules):
    url = "https://api.osv.dev/v1/querybatch"
    findings = []
    batches = 0
    for offset in range(0, len(modules), 150):
        batch = modules[offset: offset + 150]
        queries = [{"version": v, "package": {"name": g + ":" + a, "ecosystem": "Maven"}}
                   for g, a, v in batch]
        req = urllib.request.Request(
            url, data=json.dumps({"queries": queries}).encode("utf-8"),
            headers={"Content-Type": "application/json", "User-Agent": "CommunicationTrainer-Security-Audit/1"},
            method="POST")
        payload = None
        for attempt in range(3):
            try:
                with urllib.request.urlopen(req, timeout=45) as resp:
                    payload = json.load(resp)
                break
            except (OSError, ValueError) as e:
                if attempt == 2:
                    raise RuntimeError("OSV API failed after retries: " + str(e)) from e
                time.sleep(3 * (attempt + 1))
        results = payload.get("results")
        if not isinstance(results, list) or len(results) != len(batch):
            raise RuntimeError("OSV response missing matching results; do not report clean status")
        batches += 1
        for (group, artifact, version), result in zip(batch, results):
            for item in result.get("vulns", []):
                findings.append({"dependency": f"{group}:{artifact}", "version": version,
                                 "advisory": item.get("id", "unknown")})
    return findings, batches

def main():
    if len(sys.argv) != 3:
        raise SystemExit("Usage: audit_runtime_osv.py <gradle-log> <output.json>")
    src, dest = map(Path, sys.argv[1:])
    base = {"source": "Gradle debugRuntimeClasspath resolved modules",
            "api": "https://api.osv.dev/v1/querybatch",
            "timestamp_utc": __import__("datetime").datetime.now(__import__("datetime").timezone.utc).isoformat(),
            "packages": [], "findings": [], "result": "incomplete"}
    try:
        modules = collect_modules(src)
        if len(modules) < 10:
            raise RuntimeError(f"Only {len(modules)} Maven modules detected; resolution incomplete")
        base["packages"] = [{"group":g,"artifact":a,"version":v} for g,a,v in modules]
        findings, batches = query_osv(modules)
        base.update(findings=findings, checked_packages=len(modules), batches=batches,
                    result="advisories_found" if findings else "no_known_advisories_returned")
    except Exception as exc:
        base.update(result="scan_failed", error=str(exc))
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_text(json.dumps(base, indent=2, ensure_ascii=False), encoding="utf-8")
    print(f"OSV audit: {base['result']}; {len(base['packages'])} modules; "
          f"{len(base['findings'])} advisory references")
    for f in base["findings"][:60]:
        print(f"OSV-FINDING {f['dependency']}:{f['version']} {f['advisory']}")
    if base["result"] == "scan_failed":
        print("OSV-ERROR " + base["error"])
        raise SystemExit(2)
    # Findings are blocking for approval, not a scanner/tool crash.
    raise SystemExit(0)

if __name__ == "__main__":
    main()
