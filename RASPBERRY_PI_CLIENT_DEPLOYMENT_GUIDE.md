# Raspberry Pi Client — Deployment Guide

Ops/installation guide for the two standalone device scripts in [`raspberry-pi-client/`](raspberry-pi-client/):

| Script | Purpose | Protocol/API doc |
|---|---|---|
| `conveyor_client.py` | Drives one conveyor line's barcode scan + PLC signal + dispatch-task tracking | [`CONVEYOR_SCANNER_WORKFLOW_GUIDE.md`](CONVEYOR_SCANNER_WORKFLOW_GUIDE.md) |
| `parking_bay_scanner.py` | Tracks lorry parking bay in/out-time from a gate-pass QR scan | [`PARKING_BAY_SCANNER_GUIDE.md`](PARKING_BAY_SCANNER_GUIDE.md) |

This document covers *running* them on real hardware (dependencies, config, process supervision, logs). For request/response shapes and backend behavior, see the linked guide for each script.

Both scripts talk to permit-all backend endpoints (no login, no JWT — a Pi has no interactive user to authenticate as) and are designed to run **unattended for weeks**: each has its own top-level supervisor loop that catches any unexpected exception, logs it, and restarts from scratch after a short delay rather than letting the process die.

## 1. Prerequisites

- Python 3.9+ on the Pi (or any Linux/Windows box that can reach the backend over the network).
- Dependencies:
  ```bash
  cd raspberry-pi-client
  pip install -r requirements.txt   # just `requests`
  ```

## 2. Config setup

Each script reads a JSON config file living next to it. Copy the relevant `*.example.json` and fill in real values — never commit the filled-in copy (it's typically machine-specific: real IPs, real device ids).

```bash
cd raspberry-pi-client
cp config.example.json config.json                                    # conveyor_client.py
cp parking_bay_scanner_config.example.json parking_bay_scanner_config.json  # parking_bay_scanner.py
```

Key fields common to both: `baseUrl` (backend URL, e.g. `http://192.168.1.10:8080`), `deviceId`, and either `scannerIp`/`scannerPort` (network TCP/IP barcode/QR scanner) or leave `scannerIp` unset to read from stdin (dev/testing, or a USB scanner that enumerates as a keyboard). See each script's own guide for the rest of its fields.

## 3. Running manually (foreground — dev/testing)

```bash
cd raspberry-pi-client
python conveyor_client.py            # uses config.json by default
python parking_bay_scanner.py
```

Ctrl+C stops either one cleanly (`conveyor_client.py` also sends a final `STOPPED`/`OFFLINE` heartbeat on the way out). Both log to stdout AND a rotating file next to the script — watch stdout while testing, but the file is what you go back to after the fact once this runs unattended.

## 4. Running two independent conveyor scanners on one Pi

`conveyor_client.py` is one-config-per-process by design (one `ConveyorClient` = one `bayCode` = one dispatch task's qty tracking = one heartbeat identity). To run a **second, fully independent** conveyor scanner off the same Pi — different bay, different scanner, possibly a different workflow — pass its config file as the first CLI argument instead of duplicating the script:

```bash
cd raspberry-pi-client
cp config.example.json config_bay2.json   # or config_bay2.example.json as a starting point
# edit config_bay2.json: bayCode, deviceId, scannerIntegrationId, scannerIp/scannerPort

python conveyor_client.py                 # process 1 — config.json (default)
python conveyor_client.py config_bay2.json  # process 2 — fully independent
```

Each process gets its own rotating log file too, named after its config file's stem, so the two never interleave writes into the same file:

| Config | Log file |
|---|---|
| `config.json` (default) | `conveyor_client.log` |
| `config_bay2.json` | `conveyor_client.config_bay2.log` |

`parking_bay_scanner.py` does **not** support this yet (its config path is still hardcoded to `parking_bay_scanner_config.json`) — it's a single-process script. Ask if you need the same CLI-argument treatment there too.

## 5. Production deployment (systemd)

**Why you need this:** if the Pi loses power or reboots, the Python process is gone — nothing brings it back on its own. `systemd` is Linux's built-in "keep this running, start it on boot, restart it if it dies" tool, and it's already installed on Raspberry Pi OS. You paste one block below per script you're deploying; each block both *creates* the service and *starts* it. Do this once per script — after that you never touch it again except with the cheat-sheet in §5.5.

Each block below is self-contained: select the whole thing, paste it into a terminal on the Pi, press enter. Only edit it first if your repo isn't checked out at `/home/pi/ariabackend` — change `WorkingDirectory` (and the `User=pi` line, if you deploy as a different user) to match.

### 5.1 One conveyor scanner (the common case — start here)

```bash
sudo tee /etc/systemd/system/conveyor-client.service > /dev/null <<'EOF'
[Unit]
Description=Conveyor line client
After=network-online.target
Wants=network-online.target

[Service]
Type=simple
User=pi
WorkingDirectory=/home/pi/ariabackend/raspberry-pi-client
ExecStart=/usr/bin/python3 conveyor_client.py
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable --now conveyor-client
sudo systemctl status conveyor-client
```

`systemctl status` should print `active (running)` in green. That's it — it now starts automatically on every boot, and restarts itself 5 seconds after dying for any reason (`Restart=always` + `RestartSec=5`; `enable --now` means "start on every future boot, AND start it right now").

### 5.2 Two conveyor scanners on one Pi

Same idea, run twice — only the service name and the config file on `ExecStart`'s last word differ:

```bash
sudo tee /etc/systemd/system/conveyor-client-bay1.service > /dev/null <<'EOF'
[Unit]
Description=Conveyor line client (bay 1)
After=network-online.target
Wants=network-online.target

[Service]
Type=simple
User=pi
WorkingDirectory=/home/pi/ariabackend/raspberry-pi-client
ExecStart=/usr/bin/python3 conveyor_client.py config.json
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
EOF

sudo tee /etc/systemd/system/conveyor-client-bay2.service > /dev/null <<'EOF'
[Unit]
Description=Conveyor line client (bay 2)
After=network-online.target
Wants=network-online.target

[Service]
Type=simple
User=pi
WorkingDirectory=/home/pi/ariabackend/raspberry-pi-client
ExecStart=/usr/bin/python3 conveyor_client.py config_bay2.json
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable --now conveyor-client-bay1 conveyor-client-bay2
sudo systemctl status conveyor-client-bay1 conveyor-client-bay2
```

### 5.3 Parking bay scanner

```bash
sudo tee /etc/systemd/system/parking-bay-scanner.service > /dev/null <<'EOF'
[Unit]
Description=Parking bay scanner
After=network-online.target
Wants=network-online.target

[Service]
Type=simple
User=pi
WorkingDirectory=/home/pi/ariabackend/raspberry-pi-client
ExecStart=/usr/bin/python3 parking_bay_scanner.py
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable --now parking-bay-scanner
sudo systemctl status parking-bay-scanner
```

Runs fine alongside the conveyor unit(s) from §5.1/§5.2 on the same Pi — different endpoints, no shared state, each is its own independent service.

### 5.4 What am I actually pasting? (read once, applies to every block above)

| Line | In plain words |
|---|---|
| `After=network-online.target` / `Wants=network-online.target` | Don't even try to start until the Pi has network access |
| `User=pi` | Run as this Linux user — change it if your Pi uses a different account |
| `WorkingDirectory=...` | Run as if you'd `cd`'d into `raspberry-pi-client` first — this is why `ExecStart` doesn't need a full path to the script |
| `ExecStart=...` | The actual command being run, exactly as you'd type it yourself |
| `Restart=always` + `RestartSec=5` | If it dies for *any* reason, wait 5 seconds and start it again |
| `WantedBy=multi-user.target` | Start this automatically every time the Pi boots |
| `enable --now` | Turns on both "start on every future boot" and "start it right now" in one go |

### 5.5 Cheat sheet — everyday commands

Swap in whichever service name you used above (`conveyor-client`, `conveyor-client-bay1`, `parking-bay-scanner`, ...).

| I want to... | Command |
|---|---|
| Check it's running | `sudo systemctl status conveyor-client` |
| Watch live logs | `journalctl -u conveyor-client -f` (Ctrl+C to stop watching — doesn't stop the service) |
| Restart it (e.g. after `git pull`) | `sudo systemctl restart conveyor-client` |
| Stop it right now | `sudo systemctl stop conveyor-client` |
| Stop it AND stop auto-starting on boot | `sudo systemctl disable --now conveyor-client` |

## 6. Updating a deployed client

```bash
cd /home/pi/ariabackend   # or wherever this repo is checked out on the Pi
git pull
sudo systemctl restart conveyor-client   # or whichever service name(s) you created in §5
```

If you deployed the two-scanner or parking-bay setups, restart all of the service names you actually created, e.g. `sudo systemctl restart conveyor-client-bay1 conveyor-client-bay2 parking-bay-scanner`.

Config files (`config.json`, `config_bay2.json`, `parking_bay_scanner_config.json`) are gitignored/untracked machine-specific files — a `git pull` never touches them.

## 7. Troubleshooting

| Symptom | Likely cause | Check |
|---|---|---|
| Process logs `crashed unexpectedly ... restarting` in a loop | Backend unreachable, or workflow not PUBLISHED | `baseUrl` reachable from the Pi? `curl $baseUrl/api/m0/datasource/health`. Workflow code spelled/published correctly? |
| `scanner connection failed ... retrying` forever | Wrong `scannerIp`/`scannerPort`, or scanner powered off/network-isolated | `ping`/`telnet` the scanner's IP:port from the Pi directly |
| Two conveyor processes' logs are interleaved in one file | Both launched with the same (or no) config argument | Confirm each systemd unit's `ExecStart` names a *different* config file (§5.2) |
| Nothing in the log file, only stdout | Running manually without redirecting, but expecting file rotation — the file handler is separate and always active | Check the log file path printed at startup (`using config ... (log file ...)`) — it's next to the script, named from the config's stem |
