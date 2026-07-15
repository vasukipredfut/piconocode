package com.aria.redis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Per-(tenant, route) token-bucket rate limit (REARCH ADR-007).
 *
 * Slap this on a controller method to cap throughput. Exhaustion returns HTTP 429.
 * The bucket is keyed by {@code route + ":" + userId} — i.e. each authenticated user
 * gets their own bucket. For anonymous endpoints we fall back to IP.
 *
 * Suggested limits per the design doc:
 *   - read endpoints: 200 r/s
 *   - write endpoints: 50 r/s
 *   - admin endpoints: 10 r/s
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    /** Sustained refill rate (tokens / second). */
    int perSecond();

    /**
     * Burst capacity. Defaults to {@code perSecond} so a hot client can spike to
     * one second's budget before being shaped.
     */
    int burst() default -1;
}
