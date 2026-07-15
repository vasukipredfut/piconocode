package com.aria.config;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import jakarta.servlet.http.HttpServletRequest;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.aria.dao.UserActivityRepository;
import com.aria.model.UserActivity;
import org.springframework.security.core.userdetails.UserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.ExpirationMs}")
    private int jwtExpirationMs;

    @Autowired
    private UserActivityRepository userActivityRepository;

    /**
     * Derives a HMAC-SHA512-compatible key from the configured secret.
     * HS512 requires >= 512 bits; we SHA-256 the secret bytes and concatenate
     * with the raw bytes to deterministically produce a 512-bit key without
     * changing token signatures based on the same secret across restarts.
     *
     * NOTE: Secret-in-source / weak-key removal tracked separately
     * (STANDARDIZATION BE-26).
     */
    private SecretKey signingKey() {
        byte[] raw = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (raw.length >= 64) {
            return Keys.hmacShaKeyFor(raw);
        }
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-512");
            byte[] derived = sha.digest(raw);
            return Keys.hmacShaKeyFor(derived);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to derive HS512 signing key", e);
        }
    }

    public void logUserActivity(String username, String token, String activityType, HttpServletRequest request) {
        try {
            Claims claims = Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(token).getPayload();
            LocalDateTime expiryDate = claims.getExpiration().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

            UserActivity activity = new UserActivity();
            activity.setUsername(username);
            activity.setActivityType(activityType);
            activity.setIpAddress(request.getRemoteAddr());
            activity.setUserAgent(request.getHeader("User-Agent"));
            activity.setActivityTime(LocalDateTime.now());
            activity.setTokenExpiryTime(expiryDate);
            userActivityRepository.save(activity);

            logger.info("JWT usage logged for user: {}, Activity: {}, Expiry: {}", username, activityType, expiryDate);
        } catch (ExpiredJwtException e) {
            UserActivity activity = new UserActivity();
            activity.setUsername(e.getClaims().getSubject());
            activity.setActivityType(activityType);
            activity.setIpAddress(request.getRemoteAddr());
            activity.setUserAgent(request.getHeader("User-Agent"));
            activity.setActivityTime(LocalDateTime.now());
            activity.setTokenExpiryTime(e.getClaims().getExpiration().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            userActivityRepository.save(activity);
            logger.warn("Attempted to log expired JWT for user: {}. Token: {}", username, token);
        } catch (Exception e) {
            UserActivity activity = new UserActivity();
            activity.setUsername(null);
            activity.setActivityType(activityType);
            activity.setIpAddress(request.getRemoteAddr());
            activity.setUserAgent(request.getHeader("User-Agent"));
            activity.setActivityTime(LocalDateTime.now());
            userActivityRepository.save(activity);
            logger.warn("Attempted to log invalid JWT for Token: {}",  token);
        }
    }

    public String generateJwtToken(UserDetails userPrincipal) {
        return generateTokenFromUsername(userPrincipal.getUsername(), tenantIdFor(userPrincipal));
    }

    public String generateTokenFromUsername(String username) {
        // Backwards-compatible default — refresh-token path and other legacy
        // callers that don't know a tenant id yet. Falls back to tenant 1
        // (the only seeded tenant); the M0 schema will be expanded to multi
        // tenant in Round 1G+ as customers onboard.
        return generateTokenFromUsername(username, com.aria.security.TenantContext.DEFAULT_TENANT_ID);
    }

    public String generateTokenFromUsername(String username, Long tenantId) {
        return Jwts.builder()
                .subject(username)
                .claim("tid", tenantId == null
                        ? com.aria.security.TenantContext.DEFAULT_TENANT_ID
                        : tenantId)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(signingKey(), Jwts.SIG.HS512)
                .compact();
    }

    public String generateTokenFromUsername1(String username) {
        return generateTokenFromUsername(username, com.aria.security.TenantContext.DEFAULT_TENANT_ID);
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(token).getPayload().getSubject();
    }

    /**
     * Reads the {@code tid} (tenant id) claim from a verified JWT.
     * Returns {@code null} if the claim is absent (the filter then falls
     * back to {@link com.aria.security.TenantContext#DEFAULT_TENANT_ID}
     * with a WARN).
     */
    public Long getTenantIdFromJwtToken(String token) {
        Claims claims = Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(token).getPayload();
        Object raw = claims.get("tid");
        if (raw == null) return null;
        if (raw instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(raw.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Derives the tenant id for token issuance. The {@code employee_details}
     * table now carries a {@code tenant_id} column (Flyway V4); the legacy
     * {@code AuthUserDetails} doesn't expose it yet — Round 1G surfaces it
     * via the principal. For now we default to tenant 1 (the only seeded
     * tenant) at issuance time; the column populates correctly via the
     * schema default so the filter-side scoping is unaffected.
     */
    private Long tenantIdFor(UserDetails principal) {
        // Round 1G: replace with principal.getTenantId().
        return com.aria.security.TenantContext.DEFAULT_TENANT_ID;
    }

    public boolean validateJwtToken(String authToken, HttpServletRequest request) {
        try {
            Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(authToken);
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
            logUserActivity(null, authToken, "VALIDATION_FAILED_SIGNATURE", request);
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            logUserActivity(null, authToken, "VALIDATION_FAILED_MALFORMED", request);
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
            logUserActivity(null, authToken, "VALIDATION_FAILED_EXPIRED", request);
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
            logUserActivity(null, authToken, "VALIDATION_FAILED_OTHER", request);
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
            logUserActivity(null, authToken, "VALIDATION_FAILED_MALFORMED", request);
        }
        return false;
    }
}
