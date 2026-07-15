package com.aria.workflow.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * SpEL-based guard evaluator for workflow transitions.
 *
 * <p>{@link SimpleEvaluationContext} (vs StandardEvaluationContext) blocks
 * {@code T(...)} type references, constructors, and reflection — safe for
 * expressions stored in tenant-authored workflow definitions.
 *
 * <p>Variables exposed to guards:
 * <ul>
 *   <li>{@code #submitted} — the new page submission's form data (Map).</li>
 *   <li>{@code #instance.submittedData} — the full accumulated submission
 *       bag keyed by pageCode.</li>
 *   <li>{@code #user} — the calling user's id (Long).</li>
 *   <li>{@code #tenant} — the active tenant id (Long).</li>
 * </ul>
 *
 * <p>Failures: any {@link SpelEvaluationException} or parse error returns
 * {@link Outcome#ERROR} with the message; the engine then writes an
 * {@code ERROR} workflow_event and rejects the transition with 409.
 */
@Component
public class SpelGuardEvaluator {

    private static final Logger log = LoggerFactory.getLogger(SpelGuardEvaluator.class);
    private final ExpressionParser parser = new SpelExpressionParser();

    public Outcome evaluate(
            String guardExpression,
            Map<String, Object> submitted,
            InstanceView instance,
            Long userId,
            Long tenantId) {
        if (guardExpression == null || guardExpression.isBlank()) {
            return Outcome.allow();
        }
        try {
            SimpleEvaluationContext ctx = SimpleEvaluationContext
                    .forReadOnlyDataBinding()
                    .withInstanceMethods()
                    .build();
            ctx.setVariable("submitted", submitted == null ? Map.of() : submitted);
            ctx.setVariable("instance", instance);
            ctx.setVariable("user", userId);
            ctx.setVariable("tenant", tenantId);

            Expression expr = parser.parseExpression(guardExpression);
            Object result = expr.getValue(ctx);
            if (result == null) return Outcome.deny("guard returned null");
            if (result instanceof Boolean b) return b ? Outcome.allow() : Outcome.deny("guard false");
            return Outcome.deny("guard returned non-boolean: " + result.getClass().getSimpleName());
        } catch (SpelEvaluationException e) {
            log.warn("Guard evaluation failed for [{}]: {}", guardExpression, e.getMessage());
            return Outcome.error(e.getMessage());
        } catch (RuntimeException e) {
            log.warn("Guard parse failed for [{}]: {}", guardExpression, e.getMessage());
            return Outcome.error(e.getMessage());
        }
    }

    /** What guards can return. */
    public enum Result { ALLOW, DENY, ERROR }

    public static final class Outcome {
        public final Result result;
        public final String reason;

        private Outcome(Result r, String reason) { this.result = r; this.reason = reason; }
        public static Outcome allow() { return new Outcome(Result.ALLOW, null); }
        public static Outcome deny(String reason) { return new Outcome(Result.DENY, reason); }
        public static Outcome error(String reason) { return new Outcome(Result.ERROR, reason); }
        public boolean allowed() { return result == Result.ALLOW; }
        public boolean errored() { return result == Result.ERROR; }
    }

    /** Read-only view of a workflow instance for guard expressions. */
    public interface InstanceView {
        String getCurrentState();
        Map<String, Object> getSubmittedData();
    }
}
