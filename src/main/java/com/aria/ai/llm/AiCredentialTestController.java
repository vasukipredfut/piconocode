package com.aria.ai.llm;

import com.aria.integration.secrets.TenantSecretCodec;
import com.aria.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * "Test connection" endpoint for the ai:tenant-credentials page.
 *
 * <p>The customer registers a provider + endpoint + model (+ optional key)
 * and hits the Test button. That posts here. We resolve the credential row,
 * probe the provider's health endpoint, and return
 * {@code {ok, latencyMs, message}} so the UI can show a green/red pill.
 *
 * <p>The row is looked up scoped to the caller's tenant — we never let a
 * customer test another tenant's credential.
 */
@RestController
@RequestMapping("/api/ai/credentials")
public class AiCredentialTestController {

    private static final Logger log = LoggerFactory.getLogger(AiCredentialTestController.class);

    private final JdbcTemplate jdbc;
    private final OllamaLlmProvider ollama;
    private final OpenAiCompatibleLlmProvider openai;
    private final TenantSecretCodec secretCodec;

    @Autowired
    public AiCredentialTestController(JdbcTemplate jdbc,
                                      OllamaLlmProvider ollama,
                                      OpenAiCompatibleLlmProvider openai,
                                      TenantSecretCodec secretCodec) {
        this.jdbc = jdbc;
        this.ollama = ollama;
        this.openai = openai;
        this.secretCodec = secretCodec;
    }

    /**
     * Probe the provider identified by this credential row.
     * Returns {@code {ok:true, latencyMs, provider, endpoint, model}} on
     * success, {@code {ok:false, message}} on failure.
     */
    @PostMapping("/{id}/test")
    @PreAuthorize("hasAnyAuthority('ROLE_PLANT_ADMIN','ROLE_ADMIN','ROLE_TENANT_ADMIN')")
    public Map<String, Object> test(@PathVariable Long id) {
        Long tenantId = TenantContext.get();
        Map<String, Object> out = new LinkedHashMap<>();

        if (tenantId == null) {
            out.put("ok", false);
            out.put("message", "No tenant context");
            return out;
        }

        Map<String, Object> cred;
        try {
            cred = jdbc.queryForMap(
                    "SELECT id, provider, model, endpoint, api_key_ciphertext " +
                    "FROM tenant_ai_credential WHERE id=? AND tenant_id=?",
                    id, tenantId);
        } catch (Exception e) {
            out.put("ok", false);
            out.put("message", "Credential not found for this tenant");
            return out;
        }

        String provider = (String) cred.get("provider");
        String endpoint = (String) cred.get("endpoint");
        String model    = (String) cred.get("model");
        String key      = secretCodec.decryptIfEncrypted((String) cred.get("api_key_ciphertext"));

        out.put("provider", provider);
        out.put("endpoint", endpoint);
        out.put("model", model);

        long t0 = System.currentTimeMillis();
        boolean ok;
        String detail = null;

        try {
            switch (provider) {
                case "ollama-local":
                case "ollama-hosted":
                    ok = ollama.healthy(endpoint == null ? "http://localhost:11434" : endpoint, key);
                    if (!ok) detail = "Ollama /api/tags did not return 200";
                    break;
                case "openai":
                case "openai-compatible":
                    ok = openai.healthy(endpoint == null ? "https://api.openai.com" : endpoint, key);
                    if (!ok) detail = "GET /v1/models did not return 200 (check endpoint + API key)";
                    break;
                default:
                    ok = false;
                    detail = "Provider '" + provider + "' not yet supported. "
                           + "Supported: ollama-local, ollama-hosted, openai, openai-compatible";
            }
        } catch (Exception e) {
            log.warn("test-connection failed for credential {}: {}", id, e.getMessage());
            ok = false;
            detail = e.getMessage();
        }

        out.put("ok", ok);
        out.put("latencyMs", System.currentTimeMillis() - t0);
        if (detail != null) out.put("message", detail);
        return out;
    }
}
