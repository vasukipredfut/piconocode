#!/usr/bin/env bash
#
# Deploy Ollama + Caddy on an NVIDIA GPU host so EARMS can talk to a
# tenant's LLM over HTTPS with HTTP Basic Auth.
#
# Run this ON THE GPU MACHINE, not on your laptop or the EARMS server.
# Expects: recent Ubuntu-ish distro, Docker + docker compose plugin, an
# NVIDIA GPU with drivers already installed, and the NVIDIA container
# toolkit configured for the Docker runtime.
#
# What it deploys:
#   * ollama container — bound to the internal docker network only;
#     port 11434 is NEVER published on the host.
#   * caddy container  — listens on :443, terminates TLS (self-signed
#     via `tls internal`), requires HTTP Basic Auth, and reverse-
#     proxies to ollama:11434.
#   * UFW firewall     — only SSH + 443 are exposed to the internet.
#
# Idempotent — safe to re-run to rotate the password or re-provision.
#
# After the first successful run:
#   * Endpoint is https://<public-ip> (self-signed cert; use --insecure
#     or attach a real domain to get Let's Encrypt automatically).
#   * Credentials are saved to /root/ollama-creds.env (chmod 600).
#   * `docker exec ollama ollama pull <model>` adds more models later.
#
# Then update EARMS's tenant_ai_credential row so LlmProxy picks it up:
#   UPDATE tenant_ai_credential
#      SET endpoint='https://ai.yourdomain.com',
#          model='qwen2.5:7b-instruct',
#          api_key_ciphertext=CONCAT('basic ', TO_BASE64('earms:<pass>'))
#    WHERE tenant_id=<tenant>;
#
set -euo pipefail

INSTALL_DIR="${INSTALL_DIR:-/opt/ollama}"
CREDS_FILE="${CREDS_FILE:-/root/ollama-creds.env}"
OLLAMA_USER="${OLLAMA_USER:-earms}"
DEFAULT_MODEL="${DEFAULT_MODEL:-qwen2.5:7b-instruct}"

log()  { printf '\e[36m▶ %s\e[0m\n' "$*"; }
warn() { printf '\e[33m! %s\e[0m\n' "$*"; }
die()  { printf '\e[31m✗ %s\e[0m\n' "$*" >&2; exit 1; }

[[ $EUID -eq 0 ]] || die "run as root: sudo bash $0"

# ---------------------------------------------------------------- pre-flight
log "checking GPU"
command -v nvidia-smi >/dev/null || die "nvidia-smi not found — install NVIDIA drivers first"
nvidia-smi --query-gpu=name --format=csv,noheader | head -1

log "checking Docker"
command -v docker >/dev/null || die "docker not installed — install docker + docker-compose-plugin first"
docker compose version >/dev/null 2>&1 || die "docker compose plugin missing"

log "checking NVIDIA container toolkit"
docker run --rm --gpus all nvidia/cuda:12.6.0-base-ubuntu22.04 nvidia-smi >/dev/null 2>&1 \
    || die "GPU not visible to containers — run: nvidia-ctk runtime configure --runtime=docker && systemctl restart docker"

# ---------------------------------------------------------------- credentials
mkdir -p "$INSTALL_DIR"
cd "$INSTALL_DIR"

if [[ -f "$CREDS_FILE" ]]; then
    log "reusing existing credentials at $CREDS_FILE"
    # shellcheck disable=SC1090
    source "$CREDS_FILE"
else
    log "generating new credentials"
    OLLAMA_PASS="$(openssl rand -base64 24 | tr -d '=+/' | cut -c1-24)"
    umask 077
    cat > "$CREDS_FILE" <<CREDS
OLLAMA_USER="$OLLAMA_USER"
OLLAMA_PASS="$OLLAMA_PASS"
CREDS
    chmod 600 "$CREDS_FILE"
fi

log "hashing password with bcrypt"
BCRYPT_HASH="$(docker run --rm caddy:2 caddy hash-password --plaintext "$OLLAMA_PASS")"
[[ -n "$BCRYPT_HASH" ]] || die "empty bcrypt hash — docker socket problem?"

# ---------------------------------------------------------------- Caddyfile
#
# NOTE ON TLS.
#
# We ship two forms depending on whether $DOMAIN is set:
#
#   DOMAIN=ai.example.com bash ollama-gpu-host.sh
#     → real Let's Encrypt cert, standard `https://<domain>` block.
#
#   (no DOMAIN)  → falls through to bare-IP mode:
#     `tls internal` binds a self-signed cert to the public IP. The
#     bare `:443` idiom (which the earlier version of this script used)
#     confused Caddy's TLS layer on some cloud NICs — it would accept
#     TCP but reject the TLS handshake without logging. Explicit IP
#     scoping + default_sni + HTTP/1&2 only (no QUIC/HTTP-3, which
#     was flaky on the tight UDP buffers of hosted VPS NICs) fixed it.
#
PUBLIC_IP="${PUBLIC_IP:-$(curl -s https://api.ipify.org || echo 127.0.0.1)}"
HOST_BLOCK="${DOMAIN:-https://$PUBLIC_IP}"
GLOBAL_EXTRA=""
if [[ -z "${DOMAIN:-}" ]]; then
    GLOBAL_EXTRA=$'    default_sni '"$PUBLIC_IP"$'\n    servers {\n        protocols h1 h2\n    }'
fi

log "writing $INSTALL_DIR/Caddyfile (host block: $HOST_BLOCK)"
cat > "$INSTALL_DIR/Caddyfile" <<CADDY
{
    admin off
    auto_https disable_redirects
$GLOBAL_EXTRA
}

$HOST_BLOCK {
    tls internal

    basic_auth {
        $OLLAMA_USER $BCRYPT_HASH
    }

    # LLM generate calls can run 30-60s. Widen the upstream timeouts so
    # a slow model doesn't get cut off by Caddy's defaults.
    reverse_proxy ollama:11434 {
        transport http {
            response_header_timeout 300s
            read_timeout 300s
            dial_timeout 5s
        }
    }

    log {
        output stdout
        format console
    }
}
CADDY

# If DOMAIN was supplied, drop the `tls internal` line — Caddy will
# use its default auto-Let's-Encrypt path instead.
if [[ -n "${DOMAIN:-}" ]]; then
    sed -i.bak '/tls internal/d' "$INSTALL_DIR/Caddyfile" && rm -f "$INSTALL_DIR/Caddyfile.bak"
fi

# ---------------------------------------------------------------- compose
log "writing $INSTALL_DIR/docker-compose.yml"
cat > "$INSTALL_DIR/docker-compose.yml" <<'YAML'
services:
  ollama:
    image: ollama/ollama:latest
    container_name: ollama
    restart: unless-stopped
    volumes:
      - ollama-data:/root/.ollama
    # No `ports:` — only reachable by caddy over the shared docker network.
    deploy:
      resources:
        reservations:
          devices:
            - driver: nvidia
              count: all
              capabilities: [gpu]
    healthcheck:
      test: ["CMD-SHELL", "ollama list >/dev/null || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 40s

  caddy:
    image: caddy:2
    container_name: caddy
    restart: unless-stopped
    ports:
      - "443:443"
    volumes:
      - /opt/ollama/Caddyfile:/etc/caddy/Caddyfile:ro
      - caddy-data:/data
      - caddy-config:/config
    depends_on:
      ollama:
        condition: service_healthy

volumes:
  ollama-data:
  caddy-data:
  caddy-config:
YAML

# ---------------------------------------------------------------- firewall
log "configuring UFW (SSH + 443 only)"
if ! command -v ufw >/dev/null; then
    apt-get update -qq && apt-get install -y ufw
fi
ufw --force reset >/dev/null
ufw default deny incoming
ufw default allow outgoing
ufw allow OpenSSH
ufw allow 443/tcp
ufw --force enable
ufw status verbose

# ---------------------------------------------------------------- deploy
log "starting containers"
docker compose down --remove-orphans 2>/dev/null || true
docker compose up -d

log "waiting for Ollama to be healthy (up to 90s)"
status=starting
for _ in {1..30}; do
    status="$(docker inspect --format='{{.State.Health.Status}}' ollama 2>/dev/null || echo starting)"
    if [[ "$status" == "healthy" ]]; then break; fi
    sleep 3
done
[[ "$status" == "healthy" ]] || die "ollama did not become healthy — check: docker compose logs ollama"

# ---------------------------------------------------------------- model pull
if ! docker exec ollama ollama list | grep -q "$DEFAULT_MODEL"; then
    log "pulling model $DEFAULT_MODEL (~4.5 GB, one-time)"
    docker exec ollama ollama pull "$DEFAULT_MODEL"
else
    log "model $DEFAULT_MODEL already present"
fi

# ---------------------------------------------------------------- smoke test
log "smoke test: authenticated request from localhost"
if curl -sk -u "$OLLAMA_USER:$OLLAMA_PASS" https://localhost/api/tags | grep -q "$DEFAULT_MODEL"; then
    log "authenticated /api/tags OK"
else
    warn "auth check failed — inspect: docker compose logs caddy"
fi

log "smoke test: unauthenticated request must be rejected"
code="$(curl -sk -o /dev/null -w '%{http_code}' https://localhost/api/tags || echo err)"
if [[ "$code" == "401" ]]; then
    log "anon request correctly rejected with 401"
else
    warn "expected 401 for anon request, got: $code"
fi

# ---------------------------------------------------------------- done
PUBLIC_IP="$(curl -s https://api.ipify.org || echo '<your-ip>')"
cat <<DONE

╭──────────────────────────────────────────────────────────────╮
│ Ollama is up.                                                │
├──────────────────────────────────────────────────────────────┤
│ Endpoint : https://$PUBLIC_IP
│ User     : $OLLAMA_USER
│ Pass     : $OLLAMA_PASS
│ Model    : $DEFAULT_MODEL
│ Creds    : $CREDS_FILE  (chmod 600)
╰──────────────────────────────────────────────────────────────╯

Test from a client (self-signed cert → --insecure / -k):

  curl -k -u '$OLLAMA_USER:$OLLAMA_PASS' https://$PUBLIC_IP/api/tags

Point EARMS at it — SQL for tenant N:

  UPDATE tenant_ai_credential
     SET endpoint='https://$PUBLIC_IP',
         model='$DEFAULT_MODEL',
         api_key_ciphertext=CONCAT('basic ',
             TO_BASE64('$OLLAMA_USER:$OLLAMA_PASS'))
   WHERE tenant_id=<N>;

Ops:
  docker compose ps               # health
  docker compose logs -f caddy    # auth events
  docker compose logs -f ollama   # inference
  sudo bash $0                    # re-run — rotates password only if
                                  # $CREDS_FILE is missing
DONE
