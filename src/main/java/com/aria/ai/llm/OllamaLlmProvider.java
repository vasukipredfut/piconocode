package com.aria.ai.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Ollama LLM provider — talks to a local or remote Ollama daemon over HTTP(S).
 *
 * <p>Endpoint URL is per-tenant (from {@code tenant_ai_credential.endpoint}).
 * Two typical shapes:
 * <ul>
 *   <li>{@code http://localhost:11434}
 *       — a dev laptop running Ollama directly, no auth.</li>
 *   <li>{@code https://ai.yourdomain.com}
 *       — production: a dedicated GPU host provisioned by
 *       {@code scripts/deploy/ollama-gpu-host.sh}, behind Caddy + Basic Auth.
 *       {@code tenant_ai_credential.api_key_ciphertext} carries the pre-encoded
 *       auth header value (e.g. {@code "basic ZWFybXM6cGFzcw=="}).</li>
 * </ul>
 *
 * <p>For hosts using self-signed TLS (Caddy's {@code tls internal} before you
 * attach a domain), set {@code ai.ollama.insecure-tls-hosts} in
 * application.properties to a comma-separated allowlist. Never enable this
 * for production — get a real cert.
 */
@Component
public class OllamaLlmProvider implements LlmProvider {

    private static final Logger log = LoggerFactory.getLogger(OllamaLlmProvider.class);
    private static final ObjectMapper JSON = new ObjectMapper();

    private final String defaultEndpoint;
    private final String defaultModel;
    private final HttpClient http;
    private final HttpClient httpInsecure;
    private final Set<String> insecureHosts;

    public OllamaLlmProvider(
            @Value("${ai.ollama.default-endpoint:http://localhost:11434}") String defaultEndpoint,
            @Value("${ai.ollama.default-model:qwen2.5:7b-instruct}") String defaultModel,
            @Value("${ai.ollama.insecure-tls-hosts:}") String insecureHostsCsv) {

        this.defaultEndpoint = defaultEndpoint;
        this.defaultModel = defaultModel;
        this.insecureHosts = Arrays.stream(insecureHostsCsv.split(","))
                .map(String::trim).filter(s -> !s.isEmpty())
                .collect(Collectors.toUnmodifiableSet());

        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        this.httpInsecure = buildTrustAllClient();

        if (!insecureHosts.isEmpty()) {
            log.warn("ollama insecure-TLS allowlist: {} — do NOT ship this to production", insecureHosts);
        }
    }

    @Override public String code() { return "ollama-local"; }

    // ------------------------------------------------------------------
    //  Health
    // ------------------------------------------------------------------

    /** Legacy no-arg overload — probes the injected default endpoint. */
    @Override
    public boolean healthy() {
        return healthy(defaultEndpoint, null);
    }

    /** Per-tenant health check with optional auth. */
    public boolean healthy(String endpoint, String authToken) {
        try {
            HttpRequest.Builder b = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint + "/api/tags"))
                    .timeout(Duration.ofSeconds(2))
                    .GET();
            if (authToken != null && !authToken.isBlank()) {
                b.header("Authorization", authToken);
            }
            HttpResponse<String> res = clientFor(endpoint).send(b.build(), HttpResponse.BodyHandlers.ofString());
            return res.statusCode() == 200;
        } catch (Exception e) {
            log.debug("ollama health check failed for {}: {}", endpoint, e.getMessage());
            return false;
        }
    }

    // ------------------------------------------------------------------
    //  Generate
    // ------------------------------------------------------------------

    /** Legacy no-endpoint overload — uses the injected defaults. */
    @Override
    public LlmResult generate(String systemPrompt, String userPrompt,
                              String jsonSchemaHint, int maxTokens) throws LlmException {
        return generate(defaultEndpoint, defaultModel, null,
                        systemPrompt, userPrompt, jsonSchemaHint, maxTokens);
    }

    /**
     * Per-tenant overload — LlmProxy calls this after resolving the
     * credential row. {@code authToken} is the pre-encoded header value
     * (e.g. {@code "basic ZWFybXM6cGFzcw=="}) or {@code null} for no auth.
     */
    public LlmResult generate(String endpoint, String model, String authToken,
                              String systemPrompt, String userPrompt,
                              String jsonSchemaHint, int maxTokens) throws LlmException {

        long started = System.currentTimeMillis();
        try {
            String promptCombined = systemPrompt + "\n\n" + userPrompt
                    + "\n\nRespond with ONLY valid JSON matching this shape:\n" + jsonSchemaHint;

            String body = JSON.writeValueAsString(java.util.Map.of(
                    "model", model,
                    "prompt", promptCombined,
                    "stream", false,
                    "format", "json",
                    "options", java.util.Map.of(
                            "num_predict", maxTokens,
                            "temperature", 0.2)
            ));

            HttpRequest.Builder rb = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint + "/api/generate"))
                    // CPU inference on a 3B model can take 30-60s for the first call
                    // and 15-30s for a large system prompt. GPU makes this ~1-3s, but
                    // we still allow the CPU-safe ceiling; anything longer is a real
                    // problem (Ollama hung, model missing) and should fail fast.
                    .timeout(Duration.ofSeconds(90))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body));
            if (authToken != null && !authToken.isBlank()) {
                rb.header("Authorization", authToken);
            }

            HttpResponse<String> res = clientFor(endpoint).send(rb.build(), HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                throw new LlmException("ollama returned " + res.statusCode() + ": " + truncate(res.body(), 200));
            }

            JsonNode payload = JSON.readTree(res.body());
            String responseText = payload.path("response").asText("");
            int inTokens  = payload.path("prompt_eval_count").asInt(0);
            int outTokens = payload.path("eval_count").asInt(0);

            long latency = System.currentTimeMillis() - started;
            log.info("ollama {} → {}ms, in={} out={}", model, latency, inTokens, outTokens);

            return new LlmResult(responseText, inTokens, outTokens, latency, model);

        } catch (LlmException e) {
            throw e;
        } catch (Exception e) {
            throw new LlmException("ollama call failed: " + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------
    //  TLS handling — trust self-signed only for the configured allowlist
    // ------------------------------------------------------------------

    private HttpClient clientFor(String endpoint) {
        try {
            String host = URI.create(endpoint).getHost();
            if (host != null && insecureHosts.contains(host)) return httpInsecure;
        } catch (Exception ignore) {
            /* fall through — use the strict client */
        }
        return http;
    }

    /** Build an HttpClient that trusts any TLS cert. USE ONLY FOR DEV. */
    private static HttpClient buildTrustAllClient() {
        try {
            TrustManager[] trustAll = new TrustManager[] { new X509TrustManager() {
                @Override public void checkClientTrusted(X509Certificate[] c, String a) { }
                @Override public void checkServerTrusted(X509Certificate[] c, String a) { }
                @Override public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            }};
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, trustAll, new SecureRandom());
            return HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(3))
                    .sslContext(ctx)
                    .build();
        } catch (Exception e) {
            log.error("failed to build trust-all HttpClient: {}", e.getMessage());
            return HttpClient.newHttpClient();
        }
    }

    private String truncate(String s, int n) {
        return s == null ? "" : (s.length() <= n ? s : s.substring(0, n) + "…");
    }
}
