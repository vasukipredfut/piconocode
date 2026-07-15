# Deployment scripts

Standalone scripts that provision infrastructure EARMS talks to.

Never run these against the EARMS backend host itself — they target
external services. Each script is idempotent and safe to re-run.

## `ollama-gpu-host.sh`

Deploy Ollama on a remote NVIDIA GPU box (e.g. a rented A4000 VPS) with
HTTPS + HTTP Basic Auth in front, and a UFW firewall that only exposes
SSH + 443.

- Runs on the GPU box, not the EARMS backend.
- Requires: recent Ubuntu, Docker + compose plugin, NVIDIA drivers,
  and the NVIDIA container toolkit configured for the docker runtime.
- Writes credentials to `/root/ollama-creds.env` (chmod 600) and prints
  them once at the end. Re-runs reuse those creds unless the file is
  deleted.

Wire EARMS by updating `tenant_ai_credential.endpoint` +
`api_key_ciphertext` to the URL and Basic-Auth token the script prints.
`LlmProxy.tryGenerate` picks up the change with no code deploy.

Full walk-through: `docs/architecture/AI-LLM-SETUP.md`.
