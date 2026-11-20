#!/usr/bin/env python3
"""Audit resolved Maven dependencies against OSV. Never sends APK or source code.

Only Maven package coordinates and versions leave GitHub Actions; the output
keeps runtime/test scopes separate. Fail clearly if OSV cannot be reached.
"""
import json
import pathlib
import re
import sys
import urllib.request

RX = re.compile(r'([a-zA-Z][a-zA-Z0-9_.-]+):([a-zA-Z0-9_.-]+):([0-9][a-zA-Z0-9_.+\-]*)(?:\s*->\s*([0-9][a-zA-Z0-9_.+\-]*))?')

def parse_file(file_name):
    seen = {}
    for line in pathlib.Path(file_name).read_text().splitlines():
        for match in RX.finditer(line):
            group, artifact, original, resolved = match.groups()
            version = resolved or original
            if group.startswith(("androidx.", "org.", "com.", "io.", "net.", "junit", "javax.")):
                seen[(group, artifact, version)] = {
                    "package": {"ecosystem": "Maven", "name": group + ":" + artifact},
                    "version": version,
                }
    return list(seen.values())

def query(queries):
    response = urllib.request.Request(
        "https://api.osv.dev/v1/querybatch",
        data=json.dumps({"queries": queries}).encode(),
        method="POST",
        headers={"Content-Type": "application/json", "User-Agent": "CommunicationTrainer-SecurityAudit/1.0"},
    )
    with urllib.request.urlopen(response, timeout=70) as stream:
        return json.loads(stream.read(20_000_000))["results"]

def main():
    output = {"status": "complete", "scopes": {}}
    for scope, path in [("android-debug-runtime", "security-output/debugRuntimeClasspath.txt"),
                        ("android-debug-unit-tests", "security-output/debugUnitTestRuntimeClasspath.txt")]:
        deps = parse_file(path)
        findings = []
        for start in range(0, len(deps), 100):
            batch = deps[start:start + 100]
            results = query(batch)
            if len(results) != len(batch):
                raise RuntimeError("OSV result count differs from the submitted dependency count")
            for dep, result in zip(batch, results):
                if result.get("vulns"):
                    findings.append({
                        "dependency": dep["package"]["name"],
                        "version": dep["version"],
                        "vulnerabilities": [v.get("id") for v in result["vulns"]],
                    })
        output["scopes"][scope] = {"checked": len(deps), "findings": findings}
        print(scope, "dependencies checked:", len(deps), "vulnerable coordinates:", len(findings))
        for finding in findings:
            print("  ", finding["dependency"], finding["version"], finding["vulnerabilities"])
    pathlib.Path("security-output/osv-results.json").write_text(json.dumps(output, indent=2, ensure_ascii=False))
    # Findings are a reason for human review, not a failed command.
    # A query failure MUST fail the pipeline, never produce a clean report.

if __name__ == "__main__":
    try:
        main()
    except Exception as exc:
        pathlib.Path("security-output/osv-results.json").write_text(json.dumps({
            "status": "error", "detail": str(exc)
        }, indent=2))
        print("OSV audit incomplete:", type(exc).__name__, str(exc), file=sys.stderr)
        raise
