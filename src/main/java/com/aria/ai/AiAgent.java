package com.aria.ai;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as an AI agent element in the EARMS substrate.
 *
 * <p>Every implementation of {@link AiAgentHandler} MUST carry this annotation.
 * The CI script scans for {@code @AiAgent} on every merge and rebuilds
 * {@code docs/ai/INVENTORY.md} — a live, machine-readable inventory of every
 * AI element in the product. This is what makes AI visible in the codebase.
 *
 * <p>Reference: {@code docs/architecture/AI-PDLC.md} §3.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AiAgent {

    /** Matches the {@code code} column in {@code ai_agent} table. e.g. "boiler.safety-monitor". */
    String code();

    /** Human-readable name. */
    String displayName();

    /** Model identifier — e.g. "claude-opus-4-8", "sklearn.isolationforest.v1", "local.rule-cep". */
    String model() default "";

    /** Kind of agent — controls how AiAgentDispatcher invokes it. */
    Kind kind();

    /** Who this agent serves. */
    Population population();

    /**
     * The maximum mode this agent is allowed to reach.
     * A safety-critical agent might cap at GUARDED and never go AUTONOMOUS.
     */
    Mode maxMode() default Mode.GUARDED;

    /** Tool codes this agent may invoke (must match ai_agent_tool.code). */
    String[] tools() default {};

    /** One-line description of what the agent does. Shown in inventory. */
    String description();

    enum Kind    { LLM, ML_MODEL, RULE_CEP, HYBRID }
    enum Population { DEV, RUNTIME }
    enum Mode    { SHADOW, SUPERVISED, GUARDED, AUTONOMOUS, RETIRED }
}
