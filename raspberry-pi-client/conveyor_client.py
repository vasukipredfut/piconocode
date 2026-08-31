#!/usr/bin/env python3
"""
Conveyor line integration client (Raspberry Pi side).

Drives one conveyor line entirely from backend-supplied config: no
line-specific logic is hardcoded here. See CONVEYOR_SCANNER_WORKFLOW_GUIDE.md
at the repo root for the full API contract this script implements.

These endpoints are permit-all on the backend (no login, no JWT) since a
conveyor device has no interactive user to authenticate as — see
WebSecurityConfig's "/api/scanner/workflows/**" / "/api/conveyor/**" entries.

Flow:
  1. GET  /api/scanner/workflows/{workflowId}      -> integration bindings, rule sets, dispatch tasks
  2. loop: read barcode (network scanner over TCP, or stdin if unconfigured)
           -> local qty pre-check -> POST /api/conveyor/scan -> ACCEPT/REJECT
  3. background thread: POST /api/conveyor/heartbeat every HEARTBEAT_INTERVAL_S

Barcode transport: set config.json's "scannerIp"/"scannerPort" to read from a
network (TCP/IP) barcode scanner (ScannerSocketReader below, newline-framed,
auto-reconnecting). Leave "scannerIp" unset to read barcodes from stdin
instead (dev/testing, or a USB scanner enumerating as a keyboard on the Pi).

Running two independent scanners on one Pi: this script is single-config,
single-scanner by design (one ConveyorClient = one bayCode = one dispatch
task's qty tracking = one heartbeat identity). To run a second, independent
one, launch a second OS process of this SAME script with its own config file
as the first CLI argument:

    python conveyor_client.py                 # uses config.json (default)
    python conveyor_client.py config_bay2.json # a second, independent process

Each process gets its own rotating log file too (named after its config
file's stem) so two processes never interleave writes into the same file —
see resolve_paths().

Step 2's local pre-check (see ConveyorClient.check_qty_limit) compares the
scan's quantity against the tracked dispatch task's remaining allowed
quantity (target_qty - processed_qty) BEFORE calling the backend at all. If
the scan would exceed it, the scan is rejected locally and never sent over
the network — this is purely a fast-fail on the device; the backend's own
QTY_OK rule (if configured on the task's rule set) is the authoritative check.

Requires: pip install requests
"""

import json
import logging
import logging.handlers
import socket
import sys
import threading
import time
from datetime import datetime, timezone
from decimal import Decimal
from pathlib import Path

import requests

SCRIPT_DIR = Path(__file__).parent
DEFAULT_CONFIG_PATH = (SCRIPT_DIR / "config.json").resolve()
LOG_FILE_MAX_BYTES = 5 * 1024 * 1024
LOG_FILE_BACKUP_COUNT = 5
HEARTBEAT_INTERVAL_S = 5
REQUEST_TIMEOUT_S = 10
RETRY_BACKOFF_S = (1, 2, 5, 10)  # capped exponential backoff for transient failures
SCANNER_RECONNECT_DELAY_S = 3
SCANNER_CONNECT_TIMEOUT_S = 10
CRASH_RESTART_DELAY_S = 5

# Trace-friendly log line: timestamp, level, thread name (heartbeat runs on
# its own thread, so this is what tells the two apart when reading the log),
# then the message. Everything goes to BOTH the console and a rotating file
# on disk — a device that's meant to run unattended for weeks needs a log you
# can go back and read after the fact, not just whatever scrolled past on a
# terminal nobody was watching.
_LOG_FORMAT = "%(asctime)s %(levelname)-7s [%(threadName)s] %(message)s"

log = logging.getLogger("conveyor_client")


def resolve_paths():
    """
    Config path comes from the first CLI argument, defaulting to config.json
    next to this script — see the module docstring for running a second,
    independent scanner process. The log file name is derived from the
    config file's stem (default config.json -> conveyor_client.log,
    unchanged from before) so two processes on the same Pi, each with their
    own config file, never interleave writes into the same rotating file.
    """
    config_path = Path(sys.argv[1]).expanduser().resolve() if len(sys.argv) > 1 else DEFAULT_CONFIG_PATH
    log_stem = "conveyor_client" if config_path == DEFAULT_CONFIG_PATH else f"conveyor_client.{config_path.stem}"
    log_path = SCRIPT_DIR / f"{log_stem}.log"
    return config_path, log_path


def configure_logging(log_file_path):
    formatter = logging.Formatter(_LOG_FORMAT)

    console_handler = logging.StreamHandler(sys.stdout)
    console_handler.setFormatter(formatter)

    file_handler = logging.handlers.RotatingFileHandler(
        log_file_path, maxBytes=LOG_FILE_MAX_BYTES, backupCount=LOG_FILE_BACKUP_COUNT, encoding="utf-8",
    )
    file_handler.setFormatter(formatter)

    log.setLevel(logging.INFO)
    log.addHandler(console_handler)
    log.addHandler(file_handler)


def load_config(config_path):
    with open(config_path, "r", encoding="utf-8") as f:
        return json.load(f)


def utcnow_iso():
    return datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")


class ConveyorClient:
    def __init__(self, config):
        self.base_url = config["baseUrl"].rstrip("/")
        self.workflow_code = config["workflowCode"]
        self.step_code = config.get("stepCode", "")
        self.bay_code = config["bayCode"]
        self.device_id = config["deviceId"]
        self.scanner_integration_id = config.get("scannerIntegrationId")

        self.session = requests.Session()
        self.session.headers.update({"Content-Type": "application/json"})

        self._last_scan_at = None
        self._current_dispatch_task_id = None
        self._integration_health = {}
        self._stop_event = threading.Event()
        self._active_task = None  # {"id", "target_qty", "processed_qty"} Decimals; None = untracked/ambiguous

    # ---- local qty pre-check -----------------------------------------------

    def track_tasks_from_workflow(self, workflow):
        """
        Call once after fetch_workflow_config (and again after any re-sync) to
        seed the local view of this bay's active dispatch task, so scans can be
        qty-checked before ever hitting the network. Only tracks when exactly
        one Open task exists for this bay — with several concurrent
        tasks on one bay, which task a scan belongs to isn't known until the
        backend resolves it, so the local pre-check is skipped (deferred to
        the backend) rather than guessed at.
        """
        candidates = [
            t for t in workflow.get("dispatchTasks", [])
            if t.get("bay_code") == self.bay_code and t.get("status") == "Open"
        ]
        self._active_task = self._to_local_task(candidates[0]) if len(candidates) == 1 else None

    def has_tracked_task(self):
        return self._active_task is not None

    @staticmethod
    def _to_local_task(task):
        return {
            "id": task.get("id"),
            "target_qty": Decimal(str(task["target_qty"])),
            "processed_qty": Decimal(str(task["processed_qty"])),
        }

    def check_qty_limit(self, qty):
        """
        Returns (ok, error_message). ok=False means: do NOT call the backend
        for this scan — the scanned quantity would push the tracked task's
        processed_qty past its target_qty. Always ok=True when no single
        active task is tracked for this bay (untracked/ambiguous — the
        backend is the fallback authority in that case).
        """
        if self._active_task is None:
            return True, None
        remaining = self._active_task["target_qty"] - self._active_task["processed_qty"]
        if qty > remaining:
            return False, (
                f"scan qty {qty} exceeds remaining allowed qty {remaining} "
                f"(target={self._active_task['target_qty']}, processed={self._active_task['processed_qty']})"
            )
        return True, None

    # ---- HTTP helpers -----------------------------------------------------

    def _request(self, method, path, json_body=None):
        url = f"{self.base_url}{path}"
        last_exc = None
        for attempt, delay in enumerate((0,) + RETRY_BACKOFF_S):
            if delay:
                time.sleep(delay)
            started = time.monotonic()
            try:
                resp = self.session.request(
                    method, url, json=json_body, timeout=REQUEST_TIMEOUT_S
                )
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

    # ---- API calls ----------------------------------------------------------

    def fetch_workflow_config(self):
        resp = self._request(
            "GET", f"/api/scanner/workflows/{self.workflow_code}?bayCode={self.bay_code}"
        )
        if resp.status_code == 404:
            raise RuntimeError(
                f"Workflow '{self.workflow_code}' not found or not PUBLISHED"
            )
        resp.raise_for_status()
        return resp.json()

    def submit_scan(self, barcode, task_id=None, sku=None, order_ref=None, qty=None):
        effective_qty = Decimal("1") if qty is None else Decimal(str(qty))
        ok, error_message = self.check_qty_limit(effective_qty)
        if not ok:
            log.error("LOCAL REJECT barcode=%s: %s (not sent to backend)", barcode, error_message)
            return {
                "outcome": "REJECT",
                "reason": "LOCAL_QTY_LIMIT_EXCEEDED",
                "dispatchTask": None,
                "plcSignalOk": None,
                "sentToBackend": False,
            }

        body = {
            "workflowCode": self.workflow_code,
            "stepCode": self.step_code,
            "bayCode": self.bay_code,
            "barcode": barcode,
            "taskId": task_id,
            "scannerIntegrationId": self.scanner_integration_id,
            "sku": sku,
            "orderRef": order_ref,
            "qty": qty,
        }
        resp = self._request("POST", "/api/conveyor/scan", body)
        resp.raise_for_status()
        result = resp.json()
        result["sentToBackend"] = True
        self._last_scan_at = utcnow_iso()
        dispatch_task = result.get("dispatchTask")
        if dispatch_task:
            self._current_dispatch_task_id = dispatch_task.get("id")
            if self._active_task is not None and dispatch_task.get("id") == self._active_task["id"]:
                self._active_task = self._to_local_task(dispatch_task)
        return result

    def send_heartbeat(self, workflow_status="RUNNING", device_status="ONLINE"):
        body = {
            "workflowCode": self.workflow_code,
            "deviceId": self.device_id,
            "workflowStatus": workflow_status,
            "currentDispatchTaskId": self._current_dispatch_task_id,
            "deviceStatus": device_status,
            "lastScanAt": self._last_scan_at,
            "integrationHealth": self._integration_health,
        }
        resp = self._request("POST", "/api/conveyor/heartbeat", body)
        resp.raise_for_status()
        return resp.json()

    # ---- background heartbeat loop ----------------------------------------

    def start_heartbeat_loop(self):
        def _loop():
            while not self._stop_event.is_set():
                try:
                    self.send_heartbeat()
                except Exception as exc:  # noqa: BLE001 - heartbeat must never crash the process
                    log.error("heartbeat failed: %s", exc)
                self._stop_event.wait(HEARTBEAT_INTERVAL_S)

        thread = threading.Thread(target=_loop, name="heartbeat", daemon=True)
        thread.start()
        return thread

    def stop(self):
        self._stop_event.set()


# ---- barcode input --------------------------------------------------------

def read_barcode_stdin():
    """
    USB barcode scanners typically enumerate as a keyboard HID device and
    type the code followed by Enter, so a plain stdin readline covers that
    case. Used when config.json has no scannerIp — dev/testing transport.
    """
    line = sys.stdin.readline()
    if not line:
        return None
    return line.strip()


class ScannerSocketReader:
    """
    Reads newline-terminated barcodes from a network (TCP/IP) barcode
    scanner. Many industrial scanners expose a raw TCP port that streams
    each decoded barcode as a line; this connects once and reconnects
    automatically (with a fixed delay) on any socket error or a connection
    the scanner itself closes, so a scanner power-cycle or network blip
    doesn't kill the whole client process.
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
                log.info("connected to barcode scanner at %s:%d", self.host, self.port)
                return sock
            except OSError as exc:
                log.warning(
                    "scanner connection failed (%s:%d): %s — retrying in %ds",
                    self.host, self.port, exc, self.reconnect_delay_s,
                )
                time.sleep(self.reconnect_delay_s)

    def read_barcode(self):
        """Blocks until one full barcode line is available. Never returns None — reconnects forever."""
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


# ---- main ------------------------------------------------------------------

def run(config):
    """
    One full run: fetch config, start heartbeating, read+submit scans until
    either the barcode source cleanly ends (stdin EOF — dev/testing only;
    ScannerSocketReader never does this) or KeyboardInterrupt. Any OTHER
    exception propagates to main()'s supervisor loop, which restarts this
    from scratch — see main() for why that's the right place to catch it.
    """
    client = ConveyorClient(config)

    log.info("==== run cycle start: device=%s workflow=%s bay=%s ====",
              client.device_id, client.workflow_code, client.bay_code)
    log.info("fetching workflow config for %s", client.workflow_code)
    workflow = client.fetch_workflow_config()
    log.info(
        "workflow '%s' loaded: %d integration binding(s), %d dispatch task(s)",
        workflow["workflowCode"],
        len(workflow.get("integrations", [])),
        len(workflow.get("dispatchTasks", [])),
    )
    client.track_tasks_from_workflow(workflow)
    if not client.has_tracked_task():
        log.warning(
            "no single Open task tracked for bay '%s' — local qty pre-check is disabled "
            "until exactly one active task exists for this bay", client.bay_code,
        )

    client.start_heartbeat_loop()

    scanner_ip = config.get("scannerIp")
    scanner_port = config.get("scannerPort")
    scanner_socket = None
    if scanner_ip:
        scanner_socket = ScannerSocketReader(scanner_ip, scanner_port)
        read_barcode = scanner_socket.read_barcode
        log.info("reading barcodes from network scanner %s:%s", scanner_ip, scanner_port)
    else:
        read_barcode = read_barcode_stdin
        log.info("no scannerIp configured — reading barcodes from stdin (dev/testing mode)")

    log.info("heartbeat started (every %ds), waiting for barcode scans (Ctrl+C to stop)", HEARTBEAT_INTERVAL_S)

    try:
        while True:
            try:
                barcode = read_barcode()
            except Exception as exc:  # noqa: BLE001 - a scanner-transport bug must not go unlogged before the restart
                log.error("reading barcode failed: %s", exc)
                raise

            if barcode is None:
                break
            if not barcode:
                continue

            try:
                result = client.submit_scan(barcode)
            except Exception as exc:  # noqa: BLE001 - keep the loop alive on a bad scan round-trip
                log.error("scan submission failed for '%s': %s", barcode, exc)
                continue

            outcome = result["outcome"]
            if outcome == "ACCEPT":
                task = result.get("dispatchTask") or {}
                log.info(
                    "ACCEPT barcode=%s processed_qty=%s/%s status=%s plcSignalOk=%s",
                    barcode, task.get("processed_qty"), task.get("target_qty"),
                    task.get("status"), result.get("plcSignalOk"),
                )
            else:
                log.warning(
                    "REJECT barcode=%s reason=%s plcSignalOk=%s",
                    barcode, result.get("reason"), result.get("plcSignalOk"),
                )
    finally:
        if scanner_socket is not None:
            scanner_socket.close()
        client.stop()
        try:
            client.send_heartbeat(workflow_status="STOPPED", device_status="OFFLINE")
        except Exception as exc:  # noqa: BLE001 - shutdown must not raise past this point
            log.warning("final STOPPED heartbeat failed: %s", exc)


def main():
    """
    Supervisor: run() should run forever. If it exits normally (stdin EOF —
    dev/testing convenience) or via Ctrl+C, that is a deliberate stop and this
    process exits too. ANY other exception — backend unreachable at startup,
    an unknown/unpublished workflow, a bug in a barcode transport, whatever —
    is logged and retried from scratch after a short delay instead of letting
    the process die, since nothing else in this deployment restarts it.
    """
    config_path, log_file_path = resolve_paths()
    configure_logging(log_file_path)
    log.info("using config %s (log file %s)", config_path, log_file_path)
    config = load_config(config_path)
    while True:
        try:
            run(config)
            log.info("stopping (barcode source ended)")
            return
        except KeyboardInterrupt:
            log.info("stopping (Ctrl+C)")
            return
        except Exception as exc:  # noqa: BLE001 - never let this process die
            log.exception("conveyor client crashed unexpectedly (%s) — restarting in %ds", exc, CRASH_RESTART_DELAY_S)
            time.sleep(CRASH_RESTART_DELAY_S)


if __name__ == "__main__":
    main()
