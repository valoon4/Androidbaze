#!/usr/bin/env python3
"""D04 offline benchmark evidence validator. Does not run models or use any network.

Real evaluations are supplied as JSONL evidence without raw transcripts.
No model is rated or approved from a synthetic fixture or from missing data.
"""
from __future__ import annotations

import argparse
import json
import math
import re
import statistics
from collections import Counter, defaultdict
from pathlib import Path

ALLOWED_FIELDS = {
    "candidate_id", "case_id", "trial", "evidence_type", "evidence_sha256",
    "reviewer_id", "review_score", "hard_gate_ok", "latency_ms", "ttft_ms",
    "tokens_per_second", "peak_ram_mb", "input_tokens", "output_tokens",
    "input_price_usd_per_million", "output_price_usd_per_million",
    "device_profile", "runtime_revision", "model_artifact_sha256",
}
REQUIRED_FIELDS = {
    "candidate_id", "case_id", "trial", "evidence_type", "evidence_sha256"
}
ALLOWED_TYPES = {"real_model", "synthetic_fixture"}
BANNED_FIELDS = {
    "prompt", "response", "output", "text", "messages", "raw_chat",
    "api_key", "authorization", "token", "user_id", "email", "conversation",
}
HEX_SHA256 = re.compile(r"^[a-fA-F0-9]{64}$")


def load_json(path: Path) -> dict:
    data = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(data, dict):
        raise ValueError(f"Expected JSON object in {path}")
    return data


def load_jsonl(path: Path) -> list[dict]:
    if not path.exists():
        raise ValueError(f"Missing measurements file: {path}")
    results = []
    for idx, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        if line.strip():
            row = json.loads(line)
            if not isinstance(row, dict):
                raise ValueError(f"Line {idx}: expected JSON object")
            results.append(row)
    return results


def validate_suite(suite: dict, catalog: dict) -> tuple[dict, dict]:
    if suite.get("schema_version") != "d04.cases.v1":
        raise ValueError("Wrong suite schema")
    cases = suite.get("cases")
    if not isinstance(cases, list):
        raise ValueError("Missing case list")
    ids = [case.get("id") for case in cases]
    if len(ids) != 28 or set(ids) != {f"M{i:02}" for i in range(1, 29)}:
        raise ValueError("The K5 M01–M28 suite must be complete and have unique IDs")
    for case in cases:
        if case.get("repetitions", 0) < 1 or not isinstance(case.get("hard_gate"), bool):
            raise ValueError(f"Invalid gate or repeats for {case['id']}")
        if case.get("fixture") != "synthetic_only":
            raise ValueError("No private chat fixtures are permitted")
    if catalog.get("schema_version") != "d04.candidates.v1":
        raise ValueError("Wrong candidate schema")
    candidates = catalog.get("candidates")
    if not isinstance(candidates, list) or len(candidates) < 2:
        raise ValueError("Compare at least two candidates")
    names = [candidate.get("id") for candidate in candidates]
    if len(set(names)) != len(names) or any(not name for name in names):
        raise ValueError("Candidate IDs must be unique and nonempty")
    return {case["id"]: case for case in cases}, {candidate["id"]: candidate for candidate in candidates}


def validate_rows(rows: list[dict], cases: dict, candidates: dict) -> None:
    seen = set()
    for row in rows:
        extra = set(row) - ALLOWED_FIELDS
        missing = REQUIRED_FIELDS - set(row)
        if extra or missing:
            raise ValueError(f"Unsafe/unknown fields {sorted(extra)} or missing {sorted(missing)}")
        if set(row).intersection(BANNED_FIELDS):
            raise ValueError("Raw conversations or secrets are forbidden in evidence files")
        if row["candidate_id"] not in candidates or row["case_id"] not in cases:
            raise ValueError("Unknown candidate or test case")
        if type(row["trial"]) is not int or row["trial"] < 1:
            raise ValueError("Trial must be positive integer")
        if row["evidence_type"] not in ALLOWED_TYPES:
            raise ValueError("Unknown evidence type")
        if row["evidence_type"] == "real_model" and not HEX_SHA256.fullmatch(
            str(row["evidence_sha256"])
        ):
            raise ValueError("Real model results require a SHA256 evidence file")
        for key in ("latency_ms", "ttft_ms", "tokens_per_second", "peak_ram_mb",
                    "input_price_usd_per_million", "output_price_usd_per_million"):
            value = row.get(key)
            if value is not None and (
                isinstance(value, bool) or not isinstance(value, (int, float))
                or not math.isfinite(value) or value < 0
            ):
                raise ValueError(f"Invalid numeric measurement: {key}")
        for key in ("input_tokens", "output_tokens"):
            value = row.get(key)
            if value is not None and (type(value) is not int or value < 0):
                raise ValueError(f"Invalid token count: {key}")
        rating = row.get("review_score")
        if rating is not None and (type(rating) is not int or not 1 <= rating <= 5):
            raise ValueError("Human review score must be 1–5")
        if row.get("hard_gate_ok") is not None and type(row["hard_gate_ok"]) is not bool:
            raise ValueError("hard_gate_ok must be boolean or null")
        pair = (row["candidate_id"], row["case_id"], row["trial"])
        if pair in seen:
            raise ValueError(f"Duplicate case/trial: {pair}")
        seen.add(pair)


def percentile(values: list[float], percentage: float) -> float | None:
    if not values:
        return None
    values = sorted(values)
    return values[math.ceil(len(values) * percentage) - 1]


def summarize(suite: dict, catalog: dict, rows: list[dict]) -> dict:
    cases, candidates = validate_suite(suite, catalog)
    validate_rows(rows, cases, candidates)
    summaries = []
    for candidate_id, candidate in candidates.items():
        candidate_rows = [row for row in rows if row["candidate_id"] == candidate_id]
        real = [row for row in candidate_rows if row["evidence_type"] == "real_model"]
        fixture = [row for row in candidate_rows if row["evidence_type"] == "synthetic_fixture"]
        counts = Counter(row["case_id"] for row in real)
        missing = sorted(case_id for case_id, case in cases.items()
                         if counts[case_id] < case["repetitions"])
        gates_failed = sorted({row["case_id"] for row in real
                               if cases[row["case_id"]]["hard_gate"] and
                               row.get("hard_gate_ok") is False})
        gates_unreviewed = sorted({row["case_id"] for row in real
                                   if cases[row["case_id"]]["hard_gate"] and
                                   row.get("hard_gate_ok") is None})
        ratings_missing = any(not row.get("reviewer_id") or row.get("review_score") is None
                              for row in real if cases[row["case_id"]]["category"]
                              in {"dialog", "coach", "safety"})
        real_evidence_metadata_missing = any(
            not row.get("device_profile") or not row.get("runtime_revision")
            or not row.get("model_artifact_sha256")
            for row in real
        )
        runtime_eligibility_open = (
            not candidate.get("artifact_sha256") or
            not candidate.get("runtime_version") or
            not candidate.get("device_profile") or
            candidate.get("legal_review") != "approved"
        )
        if not real:
            status = "NO_REAL_MEASUREMENTS"
        elif gates_failed:
            status = "BLOCKED_HARD_GATE"
        elif missing or gates_unreviewed or ratings_missing or real_evidence_metadata_missing or runtime_eligibility_open:
            status = "INCOMPLETE_REQUIRES_REVIEW"
        else:
            status = "REVIEW_READY_NOT_APPROVED"
        latency = [row["latency_ms"] for row in real if row.get("latency_ms") is not None]
        ttft = [row["ttft_ms"] for row in real if row.get("ttft_ms") is not None]
        speeds = [row["tokens_per_second"] for row in real if row.get("tokens_per_second") is not None]
        ram = [row["peak_ram_mb"] for row in real if row.get("peak_ram_mb") is not None]
        costs = [
            (row["input_tokens"] * row["input_price_usd_per_million"] +
             row["output_tokens"] * row["output_price_usd_per_million"]) / 1_000_000
            for row in real
            if all(row.get(field) is not None for field in (
                "input_tokens", "output_tokens", "input_price_usd_per_million",
                "output_price_usd_per_million"))
        ]
        summaries.append({
            "candidate_id": candidate_id,
            "model_id": candidate.get("model_id"),
            "mode": candidate.get("mode"),
            "status": status,
            "real_measurements": len(real),
            "synthetic_fixture_rows_not_counted": len(fixture),
            "test_cases_with_real_evidence": len(counts),
            "missing_or_under_repeated_cases": missing,
            "hard_gates_failed": gates_failed,
            "hard_gates_unreviewed": gates_unreviewed,
            "human_ratings_missing": ratings_missing,
            "runtime_or_licence_gate_open": runtime_eligibility_open,
            "full_latency_ms_p50": statistics.median(latency) if latency else None,
            "full_latency_ms_p95": percentile(latency, .95),
            "ttft_ms_p50": statistics.median(ttft) if ttft else None,
            "generation_tokens_per_second_p50": statistics.median(speeds) if speeds else None,
            "peak_ram_mb_max": max(ram) if ram else None,
            "observed_api_token_cost_usd": round(sum(costs), 8) if costs else None,
            "observed_cost_rows": len(costs),
        })
    return {
        "schema_version": "d04.summary.v1",
        "overall_status": "REVIEW_NEEDED" if any(x["real_measurements"] for x in summaries) else "NO_REAL_MEASUREMENTS",
        "warning": "Never treat fixture rows, unreviewed responses or incomplete data as measured model quality. No automatic winner is selected.",
        "case_count": len(cases),
        "candidates": summaries,
    }


def main() -> int:
    base = Path(__file__).resolve().parent
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--suite", type=Path, default=base / "cases.json")
    parser.add_argument("--candidates", type=Path, default=base / "candidates.json")
    parser.add_argument("--measurements", type=Path, default=base / "measurements.jsonl")
    parser.add_argument("--output", type=Path, default=None)
    args = parser.parse_args()
    report = summarize(
        load_json(args.suite), load_json(args.candidates), load_jsonl(args.measurements)
    )
    result = json.dumps(report, indent=2, ensure_ascii=False)
    if args.output:
        args.output.write_text(result + "\n", encoding="utf-8")
    print(result)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
