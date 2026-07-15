package com.aria.ai.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * OpenAI Chat-Completions compatible provider.
 *
 * <p>One adapter covers every "on-prem customer brings own model" scenario
 * that speaks the OpenAI wire format:
 * <ul>
 *   <li><b>OpenAI</b> — endpoint {@code https://api.openai.com}, model e.g.
 *       {@code gpt-4o-mini}, {@code api_key_ciphertext} = raw
 *       {@code sk-...} key (Bearer token added by us).</li>
 *   <li><b>LM Studio</b> desktop app — endpoint
 *       {@code http://<lm-studio-host>:1234}, model = whatever the customer
 *       loaded, key blank.</li>
 *   <li><b>vLLM / TGI / LocalAI / LiteLLM / OpenRouter / Groq</b> — same
 *       shape, customer supplies endpoint + model + optional key.</li>
 *   <li><b>Ollama</b> ≥ 0.1.24 also exposes {@code /v1/chat/completions} so
 *       this adapter works with Ollama too if the customer prefers the
 *       OpenAI wire format.</li>
 * </ul>
 *
 * <p>Enrollment: the customer opens the substrate page
 * {@code ai:tenant-credentials}, picks provider {@code openai-compatible},
 * fills in endpoint + model + (optional) API key, then presses
 * <b>Test</b>. That hits {@link #healthy(String, String)} which does a
 * cheap {@code GET /v1/models} probe.
 *
 * <p>All actual generation lives at {@code POST /v1/chat/completions} with
 * {@code response_format: {"type":"json_object"}} — every OpenAI-compatible
 * server we care about honours this (LM Studio + vLLM + real OpenAI). If a
 * host doesn't, we still get a JSON-shaped response back most of the time
 * because our prompts already say "output ONLY JSON".
 */
@Component
public class OpenAiCompatibleLlmProvider implements LlmProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleLlmProvider.class);
    private static final ObjectMapper JSON = new ObjectMapper();

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    @Override public String code() { return "openai-compatible"; }

    @Override public boolean healthy() { return false; /* legacy no-arg unused */ }

    /**
     * Cheap reachability probe — {@code GET /v1/models}. All OpenAI-compatible
     * servers implement this (it's the model-list endpoint the OpenAI SDK
     * calls). LM Studio, vLLM, and real OpenAI all return 200 on it.
     */
    public boolean healthy(String endpoint, String apiKey) {
        try {
            HttpRequest.Builder b = HttpRequest.newBuilder()
                    .uri(URI.create(trimTrailingSlash(endpoint) + "/v1/models"))
                    .timeout(Duration.ofSeconds(3))
                    .GET();
            if (apiKey != null && !apiKey.isBlank()) {
                b.header("Authorization", "Bearer " + apiKey);
            }
            HttpResponse<String> res = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
            return res.statusCode() == 200;
        } catch (Exception e) {
            log.debug("openai-compatible health check failed for {}: {}", endpoint, e.getMessage());
            return false;
        }
    }

    @Override
    public LlmResult generate(String systemPrompt, String userPrompt,
                              String jsonSchemaHint, int maxTokens) {
        throw new LlmException("openai-compatible provider requires per-tenant endpoint + model");
    }

    /**
     * Per-tenant call — LlmProxy invokes this after resolving the credential
     * row. {@code apiKey} is the raw key (we prepend {@code Bearer }) or
     * {@code null}/blank for keyless local servers like LM Studio.
     */
    public LlmResult generate(String endpoint, String model, String apiKey,
                              String systemPrompt, String userPrompt,
                              String jsonSchemaHint, int maxTokens) {
        long started = System.currentTimeMillis();
        try {
            String promptCombined = userPrompt
                    + (jsonSchemaHint == null || jsonSchemaHint.isBlank()
                        ? ""
                        : "\n\nRespond with ONLY valid JSON matching this shape:\n" + jsonSchemaHint);

            String body = JSON.writeValueAsString(java.util.Map.of(
                    "model", model,
                    "messages", java.util.List.of(
                            java.util.Map.of("role", "system", "content", systemPrompt),
                            java.util.Map.of("role", "user",   "content", promptCombined)
                    ),
                    "max_tokens", maxTokens,
                    "temperature", 0.2,
                    "response_format", java.util.Map.of("type", "json_object")
            ));

            HttpRequest.Builder rb = HttpRequest.newBuilder()
                    .uri(URI.create(trimTrailingSlash(endpoint) + "/v1/chat/completions"))
                    .timeout(Duration.ofSeconds(90))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body));
            if (apiKey != null && !apiKey.isBlank()) {
                rb.header("Authorization", "Bearer " + apiKey);
            }

            HttpResponse<String> res = http.send(rb.build(), HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                throw new LlmException("openai-compatible returned " + res.statusCode()
                        + ": " + truncate(res.body(), 200));
            }

            JsonNode payload = JSON.readTree(res.body());
            String content = payload.path("choices").path(0).path("message").path("content").asText("");
            int inTokens  = payload.path("usage").path("prompt_tokens").asInt(0);
            int outTokens = payload.path("usage").path("completion_tokens").asInt(0);

            long latency = System.currentTimeMillis() - started;
            log.info("openai-compatible {} → {}ms, in={} out={}", model, latency, inTokens, outTokens);

            return new LlmResult(content, inTokens, outTokens, latency, model);

        } catch (LlmException e) {
            throw e;
        } catch (Exception e) {
            throw new LlmException("openai-compatible call failed: " + e.getMessage(), e);
        }
    }

    private static String trimTrailingSlash(String s) {
        return s == null ? "" : (s.endsWith("/") ? s.substring(0, s.length() - 1) : s);
    }

    private static String truncate(String s, int n) {
        return s == null ? "" : (s.length() <= n ? s : s.substring(0, n) + "…");
    }
}
