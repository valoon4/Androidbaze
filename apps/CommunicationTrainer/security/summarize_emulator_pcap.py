#!/usr/bin/env python3
"""Read an emulator packet capture locally and publish only aggregate counts.

Capture contains Google Android system traffic; it is NOT proof that a packet
came from CommunicationTrainer. Raw packets must NEVER be uploaded.
"""
import json
from pathlib import Path
import struct
import sys

CAPTURE = Path("runtime-private/android13-traffic.pcap")
EVENTS = Path("runtime-reports/emulator-observations.json")
OUTPUT = Path("runtime-reports/network-summary.json")

def pcap_frames(path):
    with path.open("rb") as f:
        raw = f.read(24)
        if len(raw) < 24:
            raise RuntimeError("PCAP header missing")
        magic = raw[:4]
        endian, divisor = {
            b"\xd4\xc3\xb2\xa1": ("<", 1_000_000),
            b"\xa1\xb2\xc3\xd4": (">", 1_000_000),
            b"\x4d\x3c\xb2\xa1": ("<", 1_000_000_000),
            b"\xa1\xb2\x3c\x4d": (">", 1_000_000_000),
        }.get(magic, (None, None))
        if endian is None:
            raise RuntimeError("Unsupported capture format; do not report clean")
        linktype = struct.unpack(endian + "I", raw[20:24])[0]
        while True:
            header = f.read(16)
            if not header:
                break
            if len(header) < 16:
                raise RuntimeError("Truncated packet header")
            seconds, fractions, saved, actual = struct.unpack(endian + "IIII", header)
            if saved > 4_000_000:
                raise RuntimeError("Unreasonable captured packet length")
            packet = f.read(saved)
            if len(packet) != saved:
                raise RuntimeError("Truncated packet payload")
            yield seconds + fractions / divisor, linktype, packet

def classification(at, phases):
    if at < phases.get("baseline_end", 0):
        return "baseline"
    if at >= phases.get("app_open_start", float("inf")) and at <= phases.get("app_open_end", 0):
        return "app_open"
    if at >= phases.get("local_callback_start", float("inf")) and at <= phases.get("local_callback_end", 0):
        return "local_callback"
    return "other"

def read_packet(linktype, p):
    try:
        # Ethernet DLT_EN10MB or raw-IP DLT_RAW
        if linktype == 1:
            if len(p) < 14:
                return None
            ether_type = int.from_bytes(p[12:14], "big")
            offset = 14
            while ether_type == 0x8100:
                ether_type = int.from_bytes(p[offset + 2:offset + 4], "big")
                offset += 4
            if ether_type not in (0x0800, 0x86dd):
                return None
            p = p[offset:]
        elif linktype != 101:
            return None
        if not p:
            return None
        version = p[0] >> 4
        if version == 4 and len(p) >= 20:
            header_len = (p[0] & 15) * 4
            proto = p[9]
            dst = ".".join(str(b) for b in p[16:20])
            payload = p[header_len:]
        elif version == 6 and len(p) >= 40:
            proto = p[6]
            dst = p[24:40].hex()
            payload = p[40:]
        else:
            return None
        if proto not in (6, 17):
            return (dst, proto, b"")
        return (dst, proto, payload)
    except (IndexError, ValueError):
        return None

def main():
    events = json.loads(EVENTS.read_text())
    phases = events.get("phases_unix", {})
    info = {
        "status": "complete",
        "source": "Android emulator host-side -tcpdump, parsed locally, original raw PCAP deleted/not uploaded",
        "attribution": "system_wide_capture_NOT_app_attributed",
        "limits": ["Traffic includes Android framework / Google system processes",
                   "Encrypted HTTPS contents not inspected",
                   "Matching visible domain substrings is heuristic, not a complete DNS/TLS scan",
                   "No credentialed OAuth or AI calls made during experiment"],
        "phases": {x: {"packets": 0, "unique_destinations": 0,
                       "openai_visible_domain_packets": 0, "tcp": 0, "udp": 0}
                   for x in ("baseline", "app_open", "local_callback", "other")},
        "capture_file_bytes": CAPTURE.stat().st_size if CAPTURE.exists() else 0,
    }
    destinations = {phase:set() for phase in info["phases"]}
    count = 0
    for ts, linktype, packet in pcap_frames(CAPTURE):
        count += 1
        phase = classification(ts, phases)
        x = info["phases"][phase]
        x["packets"] += 1
        parsed = read_packet(linktype, packet)
        if parsed:
            dst, proto, payload = parsed
            destinations[phase].add(dst)
            if proto == 6: x["tcp"] += 1
            if proto == 17: x["udp"] += 1
            if any(domain in packet for domain in (b"api.openai.com", b"auth.openai.com")):
                x["openai_visible_domain_packets"] += 1
    for phase in info["phases"]:
        info["phases"][phase]["unique_destinations"] = len(destinations[phase])
    info["packets_total"] = count
    if count < 1:
        info["status"] = "incomplete_no_packets"
        raise RuntimeError("Network capture empty")
    OUTPUT.write_text(json.dumps(info, indent=2), encoding="utf-8")
    print("Network capture summarized:", info["packets_total"], "total packets; raw capture never uploaded")

if __name__ == "__main__":
    try:
        main()
    except Exception as exc:
        OUTPUT.parent.mkdir(parents=True, exist_ok=True)
        OUTPUT.write_text(json.dumps({"status":"incomplete", "reason":str(exc)[:200]}, indent=2))
        raise
