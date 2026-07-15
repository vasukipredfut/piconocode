package com.aria.ai;

/**
 * THE interface every AI agent implements.
 *
 * <p>Contract:
 * <ol>
 *   <li>Implementation is annotated with {@link AiAgent}</li>
 *   <li>{@link #code()} matches {@link AiAgent#code()}</li>
 *   <li>{@link #run(AgentContext)} is a PURE function — no side effects,
 *       no DB writes, no HTTP calls except through tools declared in the annotation</li>
 *   <li>Return an {@link AgentDecision} — the dispatcher decides whether to apply</li>
 * </ol>
 *
 * <p>Side-effects (workflow transitions, notifications, entity writes) happen in
 * {@link AiAgentDispatcher} based on the agent's {@link AiAgent.Mode} — never
 * inside the handler. This is what makes agents testable in isolation.
 */
public interface AiAgentHandler {

    /** Must match the {@code code} column in {@code ai_agent} and {@link AiAgent#code()}. */
    String code();

    /** Produce a decision from the context. Pure function. */
    AgentDecision run(AgentContext ctx);
}
