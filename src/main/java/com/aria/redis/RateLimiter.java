package com.aria.redis;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Token-bucket rate limiter implemented as an atomic Lua script (REARCH ADR-007).
 *
 * The script lives entirely in Redis — no round trips for "read, decide, write".
 * It stores two fields per bucket:
 *   - tokens: current token count (may be fractional, encoded as a string)
 *   - ts:     timestamp (ms) of the last refill
 *
 * On every call:
 *   - Refill = (now - ts) / 1000 * rate, capped at burst
 *   - If tokens >= 1, consume 1 and ALLOW
 *   - Else DENY
 *
 * Returns 1 if allowed, 0 if denied. PEXPIRE keeps the key from accumulating
 * forever if the route goes idle.
 */
@Aspect
@Component
public class RateLimiter {

    private static final Logger log = LoggerFactory.getLogger(RateLimiter.class);

    /** ARGV: rate, burst, now-ms ; KEYS[1]: bucket key. Returns 1 (allow) or 0 (deny). */
    private static final String LUA =
            "local b   = redis.call('HMGET', KEYS[1], 'tokens', 'ts')\n" +
            "local rate  = tonumber(ARGV[1])\n" +
            "local burst = tonumber(ARGV[2])\n" +
            "local now   = tonumber(ARGV[3])\n" +
            "local tokens = tonumber(b[1])\n" +
            "local ts     = tonumber(b[2])\n" +
            "if tokens == nil then tokens = burst end\n" +
            "if ts == nil then ts = now end\n" +
            "local elapsed = math.max(0, now - ts) / 1000.0\n" +
            "tokens = math.min(burst, tokens + elapsed * rate)\n" +
            "local allowed = 0\n" +
            "if tokens >= 1 then\n" +
            "  tokens = tokens - 1\n" +
            "  allowed = 1\n" +
            "end\n" +
            "redis.call('HMSET', KEYS[1], 'tokens', tostring(tokens), 'ts', tostring(now))\n" +
            "redis.call('PEXPIRE', KEYS[1], 60000)\n" +
            "return allowed\n";

    private final DefaultRedisScript<Long> script;

    @Autowired
    private StringRedisTemplate redis;

    public RateLimiter() {
        this.script = new DefaultRedisScript<>();
        this.script.setScriptText(LUA);
        this.script.setResultType(Long.class);
    }

    /**
     * Atomic token-bucket check. Public so non-AOP callers (filters, scheduled jobs)
     * can use the same primitive.
     */
    public boolean allow(String bucketKey, int ratePerSec, int burst) {
        Long allowed = redis.execute(
                script,
                Collections.singletonList(bucketKey),
                String.valueOf(ratePerSec),
                String.valueOf(burst),
                String.valueOf(System.currentTimeMillis()));
        return allowed != null && allowed == 1L;
    }

    @Around("@annotation(rl)")
    public Object enforce(ProceedingJoinPoint pjp, RateLimit rl) throws Throwable {
        String route = ((MethodSignature) pjp.getSignature()).toShortString();
        String principal = currentPrincipal();
        String key = "ratelimit:" + route + ":" + principal;
        int burst = rl.burst() > 0 ? rl.burst() : rl.perSecond();

        if (!allow(key, rl.perSecond(), burst)) {
            log.warn("RATE_LIMIT_EXCEEDED route={} principal={} cap={}/s", route, principal, rl.perSecond());
            Map<String, Object> body = new HashMap<>();
            body.put("status", "429");
            body.put("error", "Too Many Requests");
            body.put("message", "Rate limit exceeded: " + rl.perSecond() + " r/s");
            body.put("route", route);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("Retry-After", "1")
                    .body(body);
        }
        return pjp.proceed();
    }

    private static String currentPrincipal() {
        try {
            Authentication a = SecurityContextHolder.getContext().getAuthentication();
            if (a != null && a.isAuthenticated() && a.getName() != null) return a.getName();
        } catch (Exception ignored) { /* fall through */ }
        // Anonymous fallback: per-IP bucket.
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                String xff = req.getHeader("X-Forwarded-For");
                if (xff != null && !xff.isEmpty()) return "ip:" + xff.split(",")[0].trim();
                return "ip:" + req.getRemoteAddr();
            }
        } catch (Exception ignored) { /* fall through */ }
        return "anon";
    }

    // Silence unused-import warnings on Arrays without forcing manual cleanup.
    @SuppressWarnings("unused")
    private static final Object KEEP = Arrays.asList();
}
