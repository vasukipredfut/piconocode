package com.aria.ai.llm;

import com.aria.m11.secrets.TenantSecretCodec;
import com.aria.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * Tenant-aware LLM router.
 *
 * <p>Every LLM call in EARMS goes through this service. It:
 * <ol>
 *   <li>Reads {@code tenant_ai_credential} for the current tenant to
 *       pick provider + model + endpoint + cost cap</li>
 *   <li>Checks the kill switch — refuses to call if operator set it</li>
 *   <li>Checks daily cost budget — refuses if exceeded</li>
 *   <li>Delegates to the matched {@link LlmProvider} implementation</li>
 *   <li>Logs every call to {@code tenant_ai_query_log} for audit + cost tracking</li>
 * </ol>
 *
 * <p>Callers get back an {@link Optional} — empty means "no LLM available;
 * fall back to your deterministic scaffold". This is the graceful-
 * degradation contract every customer agent relies on.
 */
@Service
public class LlmProxy {

    private static final Logger log = LoggerFactory.getLogger(LlmProxy.class);

    private final JdbcTemplate jdbc;
    private final OllamaLlmProvider ollama;
    private final OpenAiCompatibleLlmProvider openai;
    private final TenantSecretCodec secretCodec;

    @Autowired
    public LlmProxy(JdbcTemplate jdbc,
                    OllamaLlmProvider ollama,
                    OpenAiCompatibleLlmProvider openai,
                    TenantSecretCodec secretCodec) {
        this.jdbc = jdbc;
        this.ollama = ollama;
        this.openai = openai;
        this.secretCodec = secretCodec;
    }

    /**
     * Try to generate. Returns empty Optional if:
     * <ul>
     *   <li>No {@code tenant_ai_credential} row exists for this tenant</li>
     *   <li>Kill switch is on</li>
     *   <li>Daily cost cap exceeded</li>
     *   <li>Provider is unreachable / errors out</li>
     * </ul>
     * Every reason is logged; callers fall back to their deterministic path.
     */
    public Optional<LlmProvider.LlmResult> tryGenerate(String systemPrompt, String userPrompt,
                                                       String jsonSchemaHint, int maxTokens) {
        Long tenantId = TenantContext.get();
        if (tenantId == null) {
            log.debug("no tenant context — skipping LLM call");
            return Optional.empty();
        }

        Map<String, Object> cred = loadActiveCredential(tenantId);
        if (cred == null) {
            log.debug("no active tenant_ai_credential for tenant {} — falling back", tenantId);
            return Optional.empty();
        }

        Boolean killed = (Boolean) cred.getOrDefault("kill_switch", Boolean.FALSE);
        if (Boolean.TRUE.equals(killed)) {
            log.info("kill switch ON for tenant {} — refusing LLM call", tenantId);
            return Optional.empty();
        }

        if (isBudgetExceeded(tenantId, (Long) cred.get("id"),
                             ((Number) cred.getOrDefault("cost_cap_paise_per_day", 100000)).longValue())) {
            log.warn("cost cap exceeded for tenant {} today — falling back", tenantId);
            return Optional.empty();
        }

        String provider  = (String) cred.get("provider");
        String model     = (String) cred.get("model");
        String endpoint  = (String) cred.get("endpoint");
        // api_key_ciphertext is stored either as an EnvelopeCipher payload
        // (enc$<kid>$<base64ct>) or plaintext (legacy rows / no-key-needed
        // dev seeds). Codec handles both.
        String authToken = secretCodec.decryptIfEncrypted((String) cred.get("api_key_ciphertext"));

        try {
            LlmProvider.LlmResult result;

            switch (provider) {
                case "ollama-local":
                case "ollama-hosted":
                    if (!ollama.healthy(endpoint == null ? "http://localhost:11434" : endpoint, authToken)) {
                        log.info("ollama unreachable for tenant {} at {} — falling back", tenantId, endpoint);
                        return Optional.empty();
                    }
                    result = ollama.generate(
                            endpoint == null ? "http://localhost:11434" : endpoint,
                            model, authToken,
                            systemPrompt, userPrompt, jsonSchemaHint, maxTokens);
                    break;

                // "openai" and "openai-compatible" share one adapter — the wire
                // format is identical. Real OpenAI: endpoint https://api.openai.com,
                // key = sk-... Local: LM Studio / vLLM / LiteLLM — endpoint is the
                // customer's on-prem URL and key is often blank.
                case "openai":
                case "openai-compatible": {
                    String ep = endpoint == null ? "https://api.openai.com" : endpoint;
                    if (!openai.healthy(ep, authToken)) {
                        log.info("openai-compatible unreachable for tenant {} at {} — falling back",
                                 tenantId, ep);
                        return Optional.empty();
                    }
                    result = openai.generate(ep, model, authToken,
                            systemPrompt, userPrompt, jsonSchemaHint, maxTokens);
                    break;
                }

                default:
                    log.warn("provider '{}' not yet implemented — falling back", provider);
                    return Optional.empty();
            }

            logCall(tenantId, (Long) cred.get("id"), result, "OK", null);
            return Optional.of(result);

        } catch (LlmProvider.LlmException e) {
            log.warn("LLM call failed for tenant {}: {}", tenantId, e.getMessage());
            logCall(tenantId, (Long) cred.get("id"), null, "ERROR", e.getMessage());
            return Optional.empty();
        }
    }

    // ---- private helpers ---------------------------------------------------

    private Map<String, Object> loadActiveCredential(Long tenantId) {
        try {
            return jdbc.queryForMap(
                    "SELECT id, provider, model, endpoint, api_key_ciphertext, " +
                    "cost_cap_paise_per_day, kill_switch " +
                    "FROM tenant_ai_credential " +
                    "WHERE tenant_id=? " +
                    "ORDER BY id ASC LIMIT 1", tenantId);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isBudgetExceeded(Long tenantId, Long credId, long dailyCapPaise) {
        try {
            Long spentToday = jdbc.queryForObject(
                    "SELECT COALESCE(SUM(cost_paise), 0) FROM tenant_ai_query_log " +
                    "WHERE tenant_id=? AND credential_id=? AND DATE(called_at)=CURRENT_DATE",
                    Long.class, tenantId, credId);
            return spentToday != null && spentToday >= dailyCapPaise;
        } catch (Exception e) {
            // Table not applied yet — permit
            return false;
        }
    }

    private void logCall(Long tenantId, Long credId, LlmProvider.LlmResult result,
                         String status, String error) {
        try {
            long costPaise = result == null ? 0 : estimateCostPaise(result);
            jdbc.update(
                    "INSERT INTO tenant_ai_query_log " +
                    "(tenant_id, credential_id, called_at, kind, tokens_in, tokens_out, " +
                    " cost_paise, prompt_hash, response_hash, status, error) " +
                    "VALUES (?, ?, ?, 'CHAT', ?, ?, ?, NULL, NULL, ?, ?)",
                    tenantId, credId, Instant.now(),
                    result == null ? 0 : result.tokensIn(),
                    result == null ? 0 : result.tokensOut(),
                    costPaise, status, error);
        } catch (Exception e) {
            log.warn("failed to log tenant_ai_query_log: {}", e.getMessage());
        }
    }

    private long estimateCostPaise(LlmProvider.LlmResult r) {
        // Ollama is free — no per-token cost. For cloud providers this
        // reads from a provider rate card. Placeholder returns 0.
        return 0L;
    }
}
