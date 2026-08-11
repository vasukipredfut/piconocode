#!/usr/bin/env python3
"""
HandScanner (Raspberry Pi side) — scans a Gate Pass QR code and drives the
M10.FGLoading loading-bay flow.

One job: read a Gate Pass QR (the plain gp_number string, e.g. "GP-1-1624",
nothing more) and POST it to the backend. The backend resolves gate pass ->
workflow instance -> order/SKU dispatch details -> (when the workflow is at
LOADING_IN_PROGRESS) an attempted bay assignment, in one round trip — see
HandScannerService on the backend for the exact logic. This script
does no orchestration itself; it only sends the scanned value and logs the
response.

This is a separate, standalone script from conveyor_client.py and
parking_bay_scanner.py — different device, different purpose (loading-bay
assignment via the gate pass, not lorry parking or SKU-conveyor scanning) —
but follows the same plumbing conventions (config file, retry/backoff HTTP
helper, dual console+rotating-file logging, crash-restart supervisor loop)
so it's operationally familiar to anyone who has already run those.

This endpoint is permit-all on the backend (no login, no JWT) since this
device has no interactive user to authenticate as — see WebSecurityConfig's
"/api/handscanner/**" entry.

Flow:
  loop: read QR text (network scanner over TCP, or stdin if unconfigured)
        -> POST /api/handscanner/scan-gate-pass {"gpNumber": "..."} -> log result

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

CONFIG_PATH = Path(__file__).parent / "hand_scanner_config.json"
LOG_FILE_PATH = Path(__file__).parent / "hand_scanner.log"
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

log = logging.getLogger("hand_scanner")
log.setLevel(logging.INFO)
log.addHandler(_console_handler)
log.addHandler(_file_handler)


def load_config():
    with open(CONFIG_PATH, "r", encoding="utf-8") as f:
        return json.load(f)


class HandScannerClient:
    def __init__(self, config):
        self.base_url = config["baseUrl"].rstrip("/")
        self.device_id = config.get("deviceId", "HandScanner")

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

    def scan_gate_pass(self, gp_number):
        resp = self._request("POST", "/api/handscanner/scan-gate-pass", {"gpNumber": gp_number})
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

def log_scan_result(gp_number, result):
    if "error" in result:
        log.warning("REJECT gpNumber=%s error=%s", gp_number, result["error"])
        return

    gate_pass = result.get("gatePass") or {}
    workflow_instance = result.get("workflowInstance") or {}
    order_dispatch_details = result.get("orderDispatchDetails") or []
    bay_assignment = result.get("bayAssignment") or {}

    log.info(
        "gpNumber=%s vehicleNo=%s status=%s workflowState=%s orderRows=%d",
        gp_number, gate_pass.get("vehicle_no"), gate_pass.get("status"),
        workflow_instance.get("current_state"), len(order_dispatch_details),
    )
    if bay_assignment.get("attempted"):
        log.info(
            "BAY_ASSIGNMENT bayName=%s ok=%s message=%s",
            bay_assignment.get("bayName"), bay_assignment.get("ok"), bay_assignment.get("message"),
        )
    else:
        log.info("BAY_ASSIGNMENT not attempted: %s", bay_assignment.get("reason"))


def run(config):
    """
    One full run: read+submit QR scans until either the input source cleanly
    ends (stdin EOF — dev/testing only; ScannerSocketReader never does this)
    or KeyboardInterrupt. Any OTHER exception propagates to main()'s
    supervisor loop, which restarts this from scratch.
    """
    client = HandScannerClient(config)
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
                result = client.scan_gate_pass(gp_number)
            except Exception as exc:  # noqa: BLE001 - keep the loop alive on a bad scan round-trip
                log.error("scan submission failed for '%s': %s", gp_number, exc)
                continue

            log_scan_result(gp_number, result)
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
            log.exception("hand scanner crashed unexpectedly (%s) — restarting in %ds",
                           exc, CRASH_RESTART_DELAY_S)
            time.sleep(CRASH_RESTART_DELAY_S)


if __name__ == "__main__":
    main()
