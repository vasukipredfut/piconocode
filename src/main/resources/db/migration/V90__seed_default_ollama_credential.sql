-- V90 — Seed a default Ollama credential for tenant 1 so agents can find
-- a provider without the customer needing to register anything.
--
-- Ollama runs locally at http://localhost:11434 by default. Install:
--   curl -fsSL https://ollama.com/install.sh | sh
--   ollama pull qwen2.5:3b-instruct
--
-- If Ollama isn't running, LlmProxy.tryGenerate() returns empty and the
-- agents fall back to their deterministic scaffolds. Zero-config graceful
-- degradation.
--
-- Idempotent — safe to reapply.

INSERT INTO tenant_ai_credential
  (tenant_id, provider, model, api_key_ciphertext, endpoint,
   max_tokens_per_day, cost_cap_paise_per_day, kill_switch,
   created_at, updated_at)
SELECT 1, 'ollama-local', 'qwen2.5:3b-instruct',
       'no-key-needed-for-local-ollama',
       'http://localhost:11434',
       500000, 100000, FALSE,
       NOW(), NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM tenant_ai_credential WHERE tenant_id=1 AND provider='ollama-local'
);
