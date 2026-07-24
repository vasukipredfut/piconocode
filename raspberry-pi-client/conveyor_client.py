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
  2. loop: read barcode -> POST /api/conveyor/scan -> ACCEPT/REJECT
  3. background thread: POST /api/conveyor/heartbeat every HEARTBEAT_INTERVAL_S

Requires: pip install requests
"""

import json
import logging
import sys
import threading
import time
from datetime import datetime, timezone
from pathlib import Path

import requests

CONFIG_PATH = Path(__file__).parent / "config.json"
HEARTBEAT_INTERVAL_S = 5
REQUEST_TIMEOUT_S = 10
RETRY_BACKOFF_S = (1, 2, 5, 10)  # capped exponential backoff for transient failures

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s %(message)s",
)
log = logging.getLogger("conveyor_client")


def load_config():
    with open(CONFIG_PATH, "r", encoding="utf-8") as f:
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

    # ---- HTTP helpers -----------------------------------------------------

    def _request(self, method, path, json_body=None):
        url = f"{self.base_url}{path}"
        last_exc = None
        for attempt, delay in enumerate((0,) + RETRY_BACKOFF_S):
            if delay:
                time.sleep(delay)
            try:
                resp = self.session.request(
                    method, url, json=json_body, timeout=REQUEST_TIMEOUT_S
                )
                if resp.status_code >= 500:
                    raise requests.HTTPError(f"{resp.status_code} from {url}")
                return resp
            except (requests.ConnectionError, requests.Timeout, requests.HTTPError) as exc:
                last_exc = exc
                log.warning("request failed (attempt %d): %s", attempt + 1, exc)
        raise last_exc

    # ---- API calls ----------------------------------------------------------

    def fetch_workflow_config(self):
        resp = self._request("GET", f"/api/scanner/workflows/{self.workflow_code}")
        if resp.status_code == 404:
            raise RuntimeError(
                f"Workflow '{self.workflow_code}' not found or not PUBLISHED"
            )
        resp.raise_for_status()
        return resp.json()

    def submit_scan(self, barcode, task_id=None, sku=None, order_ref=None, qty=None):
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
        self._last_scan_at = utcnow_iso()
        dispatch_task = result.get("dispatchTask")
        if dispatch_task:
            self._current_dispatch_task_id = dispatch_task.get("id")
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

def read_barcode():
    """
    Reads one barcode value. USB barcode scanners typically enumerate as a
    keyboard HID device and type the code followed by Enter, so a plain
    stdin readline is the common case. Swap this out for GPIO/serial reads
    if the scanner integration on this line uses a different transport.
    """
    line = sys.stdin.readline()
    if not line:
        return None
    return line.strip()


# ---- main ------------------------------------------------------------------

def main():
    config = load_config()
    client = ConveyorClient(config)

    log.info("fetching workflow config for %s", client.workflow_code)
    workflow = client.fetch_workflow_config()
    log.info(
        "workflow '%s' loaded: %d integration binding(s), %d dispatch task(s)",
        workflow["workflowCode"],
        len(workflow.get("integrations", [])),
        len(workflow.get("dispatchTasks", [])),
    )

    client.start_heartbeat_loop()
    log.info("heartbeat started (every %ds), waiting for barcode scans (Ctrl+C to stop)", HEARTBEAT_INTERVAL_S)

    try:
        while True:
            barcode = read_barcode()
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
    except KeyboardInterrupt:
        log.info("stopping")
    finally:
        client.stop()
        client.send_heartbeat(workflow_status="STOPPED", device_status="OFFLINE")


if __name__ == "__main__":
    main()
