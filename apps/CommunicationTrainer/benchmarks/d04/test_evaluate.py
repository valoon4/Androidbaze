"""Offline regression tests. No network, models, tokens or user chat data."""
import copy
import unittest
from pathlib import Path
from evaluate import load_json, summarize

HERE = Path(__file__).resolve().parent
SUITE = load_json(HERE / "cases.json")
CATALOG = load_json(HERE / "candidates.json")
CASE_ID = "M01"
CANDIDATE_ID = "local-qwen3-0.6b"


def fixture():
    return {
        "candidate_id": CANDIDATE_ID,
        "case_id": CASE_ID,
        "trial": 1,
        "evidence_type": "synthetic_fixture",
        "evidence_sha256": None
    }


class D04EvidenceValidatorTest(unittest.TestCase):
    def test_no_run_cannot_be_mistaken_for_quality(self):
        report = summarize(SUITE, CATALOG, [])
        self.assertEqual("NO_REAL_MEASUREMENTS", report["overall_status"])
        self.assertEqual(28, report["case_count"])
        self.assertEqual(6, len(report["candidates"]))
        self.assertLess(report["candidates"][0]["required_case_count_for_mode_and_operations"], 28)
        for candidate in report["candidates"]:
            self.assertEqual("NO_REAL_MEASUREMENTS", candidate["status"])
            self.assertIsNone(candidate["full_latency_ms_p50"])
            self.assertIsNone(candidate["observed_api_token_cost_usd"])

    def test_fixture_does_not_count_as_real_evidence(self):
        row = fixture()
        report = summarize(SUITE, CATALOG, [row])
        self.assertEqual("NO_REAL_MEASUREMENTS", report["overall_status"])
        candidate = report["candidates"][0]
        self.assertEqual(0, candidate["real_measurements"])
        self.assertEqual(1, candidate["synthetic_fixture_rows_not_counted"])
        self.assertEqual(0, candidate["test_cases_with_real_evidence"])

    def test_secret_or_private_raw_content_not_accepted(self):
        for key in ("response", "raw_chat", "api_key", "prompt", "messages"):
            row = fixture()
            row[key] = "sensitive"
            with self.assertRaises(ValueError):
                summarize(SUITE, CATALOG, [row])

    def test_duplicate_measurement_rejected(self):
        row = fixture()
        with self.assertRaises(ValueError):
            summarize(SUITE, CATALOG, [row, row])

    def test_unknown_case_or_candidate_rejected(self):
        for field, value in (("case_id", "M99"), ("candidate_id", "unknown")):
            row = fixture()
            row[field] = value
            with self.assertRaises(ValueError):
                summarize(SUITE, CATALOG, [row])

    def test_real_run_needs_evidence_checksum(self):
        row = fixture()
        row["evidence_type"] = "real_model"
        with self.assertRaises(ValueError):
            summarize(SUITE, CATALOG, [row])

    def test_one_real_run_is_not_automatically_model_approval(self):
        row = fixture()
        row["evidence_type"] = "real_model"
        row["evidence_sha256"] = "a" * 64
        row["reviewer_id"] = "reviewer-synthetic-test"
        row["review_score"] = 3
        row["latency_ms"] = 1500
        row["ttft_ms"] = 500
        row["runtime_revision"] = "sample-revision"
        row["model_artifact_sha256"] = "b" * 64
        row["device_profile"] = "sample-device"
        report = summarize(SUITE, CATALOG, [row])
        self.assertEqual("REVIEW_NEEDED", report["overall_status"])
        self.assertEqual("INCOMPLETE_REQUIRES_REVIEW", report["candidates"][0]["status"])
        self.assertIn("M28", report["candidates"][0]["missing_or_under_repeated_cases"])

    def test_hard_gate_failure_cannot_be_averaged_away(self):
        row = fixture()
        row["case_id"] = "M09"
        row["evidence_type"] = "real_model"
        row["evidence_sha256"] = "b" * 64
        row["hard_gate_ok"] = False
        report = summarize(SUITE, CATALOG, [row])
        self.assertEqual("BLOCKED_HARD_GATE", report["candidates"][0]["status"])
        self.assertIn("M09", report["candidates"][0]["hard_gates_failed"])

    def test_cloud_only_case_must_not_be_scored_for_local_model(self):
        row = fixture()
        row["case_id"] = "M25"
        with self.assertRaises(ValueError):
            summarize(SUITE, CATALOG, [row])

    def test_coach_only_case_must_not_block_chat_only_model(self):
        report = summarize(SUITE, CATALOG, [])
        local = next(x for x in report["candidates"] if x["candidate_id"] == CANDIDATE_ID)
        self.assertNotIn("M16", local["missing_or_under_repeated_cases"])
        self.assertIn("M09", local["missing_or_under_repeated_cases"])

    def test_catalog_or_suite_missing_case_rejected(self):
        suite = copy.deepcopy(SUITE)
        suite["cases"].pop()
        with self.assertRaises(ValueError):
            summarize(suite, CATALOG, [])


if __name__ == "__main__":
    unittest.main()
