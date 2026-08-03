#!/usr/bin/env python3
"""
ParkingBayScanner (Raspberry Pi side) — lorry parking bay in-time/out-time
tracking, driven by scanning a gate pass QR code.

Nothing else: this script has exactly one job — read a gate pass QR (which
encodes the plain gp_number string, nothing more) and POST it to the backend.
The backend derives IN vs OUT itself (no open dispatch_parking_bay_entry row
for this gate pass -> IN; an open one exists -> OUT) and writes both
dispatch_parking_bay_entry and m0_audit_log. See ParkingBayScanService on the
backend for the exact logic.

This is a separate, standalone script from conveyor_client.py — different
device, different purpose (lorry parking, not the packing-line conveyor) —
but follows the same plumbing conventions (config file, retry/backoff HTTP
helper, dual console+rotating-file logging, crash-restart supervisor loop) so
it's operationally familiar to anyone who has already run conveyor_client.py.

This endpoint is permit-all on the backend (no login, no JWT) since this
device has no interactive user to authenticate as — see WebSecurityConfig's
"/api/parking/**" entry.

Flow:
  loop: read QR text (network scanner over TCP, or stdin if unconfigured)
        -> POST /api/parking/scan {"gpNumber": "..."} -> IN / OUT / REJECT

QR transport: set config.json's "scannerIp"/"scannerPort" to read from a
network (TCP/IP) QR/barcode scanner (ScannerSocketReader below, newline-
framed, auto-reconnecting). Leave "scannerIp" unset to read from stdin
instead (dev/testing, or a USB scanner enumerating as a keyboard on the Pi).

Requires: pip install requests
"""

import json
import logging
import logging.handlers
import socket
import sys
import time
from pathlib import Path

import requests

CONFIG_PATH = Path(__file__).parent / "parking_bay_scanner_config.json"
LOG_FILE_PATH = Path(__file__).parent / "parking_bay_scanner.log"
LOG_FILE_MAX_BYTES = 5 * 1024 * 1024
LOG_FILE_BACKUP_COUNT = 5
REQUEST_TIMEOUT_S = 10
RETRY_BACKOFF_S = (1, 2, 5, 10)  # capped exponential backoff for transient failures
SCANNER_RECONNECT_DELAY_S = 3
SCANNER_CONNECT_TIMEOUT_S = 10
CRASH_RESTART_DELAY_S = 5

_LOG_FORMAT = "%(asctime)s %(levelname)-7s [%(threadName)s] %(message)s"
_formatter = logging.Formatter(_LOG_FORMAT)

_console_handler = logging.StreamHandler(sys.stdout)
_console_handler.setFormatter(_formatter)

_file_handler = logging.handlers.RotatingFileHandler(
    LOG_FILE_PATH, maxBytes=LOG_FILE_MAX_BYTES, backupCount=LOG_FILE_BACKUP_COUNT, encoding="utf-8",
)
_file_handler.setFormatter(_formatter)

log = logging.getLogger("parking_bay_scanner")
log.setLevel(logging.INFO)
log.addHandler(_console_handler)
log.addHandler(_file_handler)


def load_config():
    with open(CONFIG_PATH, "r", encoding="utf-8") as f:
        return json.load(f)


class ParkingBayScannerClient:
    def __init__(self, config):
        self.base_url = config["baseUrl"].rstrip("/")
        self.device_id = config.get("deviceId", "ParkingBayScanner")

        self.session = requests.Session()
        self.session.headers.update({"Content-Type": "application/json"})

    def _request(self, method, path, json_body=None):
        url = f"{self.base_url}{path}"
        last_exc = None
        for attempt, delay in enumerate((0,) + RETRY_BACKOFF_S):
            if delay:
                time.sleep(delay)
            started = time.monotonic()
            try:
                resp = self.session.request(method, url, json=json_body, timeout=REQUEST_TIMEOUT_S)
                elapsed_ms = round((time.monotonic() - started) * 1000)
                if resp.status_code >= 500:
                    raise requests.HTTPError(f"{resp.status_code} from {url}")
                log.info("TRACE %s %s -> %s (%dms)%s", method, path, resp.status_code, elapsed_ms,
                         f", attempt {attempt + 1}" if attempt else "")
                return resp
            except (requests.ConnectionError, requests.Timeout, requests.HTTPError) as exc:
                elapsed_ms = round((time.monotonic() - started) * 1000)
                last_exc = exc
                log.warning("TRACE %s %s -> FAILED after %dms (attempt %d): %s",
                            method, path, elapsed_ms, attempt + 1, exc)
        raise last_exc

    def submit_scan(self, gp_number):
        resp = self._request("POST", "/api/parking/scan", {"gpNumber": gp_number})
        resp.raise_for_status()
        return resp.json()


# ---- QR input ---------------------------------------------------------------

def read_qr_stdin():
    """
    USB QR/barcode scanners typically enumerate as a keyboard HID device and
    type the decoded value followed by Enter, so a plain stdin readline covers
    that case. Used when config.json has no scannerIp — dev/testing transport.
    """
    line = sys.stdin.readline()
    if not line:
        return None
    return line.strip()


class ScannerSocketReader:
    """
    Reads newline-terminated QR/barcode values from a network (TCP/IP)
    scanner. Connects once and reconnects automatically (with a fixed delay)
    on any socket error or a connection the scanner itself closes, so a
    scanner power-cycle or network blip doesn't kill the whole client process.
    """

    def __init__(self, host, port, reconnect_delay_s=SCANNER_RECONNECT_DELAY_S,
                 connect_timeout_s=SCANNER_CONNECT_TIMEOUT_S):
        self.host = host
        self.port = port
        self.reconnect_delay_s = reconnect_delay_s
        self.connect_timeout_s = connect_timeout_s
        self._sock = None
        self._buffer = b""

    def _connect(self):
        while True:
            try:
                sock = socket.create_connection((self.host, self.port), timeout=self.connect_timeout_s)
                log.info("connected to QR scanner at %s:%d", self.host, self.port)
                return sock
            except OSError as exc:
                log.warning(
                    "scanner connection failed (%s:%d): %s — retrying in %ds",
                    self.host, self.port, exc, self.reconnect_delay_s,
                )
                time.sleep(self.reconnect_delay_s)

    def read_value(self):
        """Blocks until one full scanned value is available. Never returns None — reconnects forever."""
        if self._sock is None:
            self._sock = self._connect()
        while b"\n" not in self._buffer:
            try:
                chunk = self._sock.recv(4096)
            except OSError as exc:
                log.warning("scanner socket read failed: %s — reconnecting", exc)
                self._sock = self._connect()
                continue
            if not chunk:
                log.warning("scanner closed the connection — reconnecting")
                self._sock = self._connect()
                continue
            self._buffer += chunk
        line, _, self._buffer = self._buffer.partition(b"\n")
        return line.decode("utf-8", errors="replace").strip()

    def close(self):
        if self._sock is not None:
            try:
                self._sock.close()
            except OSError:
                pass
            self._sock = None


# ---- main --------------------------------------------------------------------

def run(config):
    """
    One full run: read+submit QR scans until either the input source cleanly
    ends (stdin EOF — dev/testing only; ScannerSocketReader never does this)
    or KeyboardInterrupt. Any OTHER exception propagates to main()'s
    supervisor loop, which restarts this from scratch.
    """
    client = ParkingBayScannerClient(config)
    log.info("==== run cycle start: device=%s base=%s ====", client.device_id, client.base_url)

    scanner_ip = config.get("scannerIp")
    scanner_port = config.get("scannerPort")
    scanner_socket = None
    if scanner_ip:
        scanner_socket = ScannerSocketReader(scanner_ip, scanner_port)
        read_qr = scanner_socket.read_value
        log.info("reading QR scans from network scanner %s:%s", scanner_ip, scanner_port)
    else:
        read_qr = read_qr_stdin
        log.info("no scannerIp configured — reading QR scans from stdin (dev/testing mode)")

    log.info("waiting for gate pass QR scans (Ctrl+C to stop)")

    try:
        while True:
            try:
                gp_number = read_qr()
            except Exception as exc:  # noqa: BLE001 - a scanner-transport bug must not go unlogged before the restart
                log.error("reading QR scan failed: %s", exc)
                raise

            if gp_number is None:
                break
            if not gp_number:
                continue

            try:
                result = client.submit_scan(gp_number)
            except Exception as exc:  # noqa: BLE001 - keep the loop alive on a bad scan round-trip
                log.error("scan submission failed for '%s': %s", gp_number, exc)
                continue

            outcome = result.get("outcome")
            if outcome in ("IN", "OUT"):
                entry = result.get("parkingBayEntry") or {}
                log.info(
                    "%s gpNumber=%s vehicleNo=%s inTime=%s outTime=%s",
                    outcome, gp_number, entry.get("vehicle_no"), entry.get("in_time"), entry.get("out_time"),
                )
            else:
                log.warning("REJECT gpNumber=%s reason=%s", gp_number, result.get("reason"))
    finally:
        if scanner_socket is not None:
            scanner_socket.close()


def main():
    """
    Supervisor: run() should run forever. If it exits normally (stdin EOF —
    dev/testing convenience) or via Ctrl+C, that is a deliberate stop and this
    process exits too. ANY other exception is logged and retried from scratch
    after a short delay instead of letting the process die, since nothing else
    in this deployment restarts it.
    """
    config = load_config()
    while True:
        try:
            run(config)
            log.info("stopping (QR source ended)")
            return
        except KeyboardInterrupt:
            log.info("stopping (Ctrl+C)")
            return
        except Exception as exc:  # noqa: BLE001 - never let this process die
            log.exception("parking bay scanner crashed unexpectedly (%s) — restarting in %ds",
                           exc, CRASH_RESTART_DELAY_S)
            time.sleep(CRASH_RESTART_DELAY_S)


if __name__ == "__main__":
    main()
