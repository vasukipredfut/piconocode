package com.aria.ai.llm;

/**
 * Uniform interface every LLM provider implements — Ollama today,
 * Anthropic / OpenAI / Bedrock later.
 *
 * <p>Contract:
 * <ul>
 *   <li>{@link #generate} takes a system+user prompt pair, a JSON schema hint,
 *       and returns the model's JSON output as a string.</li>
 *   <li>{@link #healthy} is a fast (~50ms) reachability check — used by
 *       {@link LlmProxy} to fall back to deterministic behaviour when the
 *       provider is unreachable.</li>
 * </ul>
 *
 * <p>All calls are synchronous and short-timeout (30s hard cap by default).
 * Providers MUST throw {@link LlmException} on any error (network, bad
 * schema, cost cap hit); never return null. The dispatcher decides whether
 * to fall back or surface the error.
 */
public interface LlmProvider {

    /** The provider's canonical code — matches {@code tenant_ai_credential.provider}. */
    String code();

    /** Cheap reachability check. Should complete in &lt; 500ms. */
    boolean healthy();

    /**
     * Ask the model to produce a JSON response.
     *
     * @param systemPrompt         constraints + persona (e.g. "You output only JSON matching the schema.")
     * @param userPrompt           the actual customer request
     * @param jsonSchemaHint       shape hint the model should follow (Ollama accepts full JSON schema; some providers only support instruction)
     * @param maxTokens            hard cap on output tokens
     * @return                     the model's JSON output (string)
     * @throws LlmException        on network / model / schema failure
     */
    LlmResult generate(String systemPrompt, String userPrompt, String jsonSchemaHint, int maxTokens) throws LlmException;

    /** Response envelope. Contains cost + token counts for audit. */
    record LlmResult(
            String json,
            int tokensIn,
            int tokensOut,
            long latencyMs,
            String modelUsed
    ) {}

    /** Provider-neutral exception — the proxy translates to fallback or error. */
    class LlmException extends RuntimeException {
        public LlmException(String msg) { super(msg); }
        public LlmException(String msg, Throwable cause) { super(msg, cause); }
    }
}
