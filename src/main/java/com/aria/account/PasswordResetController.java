package com.aria.account;

import com.aria.integration.service.TenantMailService;
import com.aria.security.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Forgotten-password reset by emailed code (replaces ERPNext send_reset_otp / verify_reset_otp /
 * reset_password). Lives under /api/auth/** which WebSecurityConfig already permits.
 *
 * <ul>
 *   <li>{@code POST /api/auth/password-reset/request} {username}: always answers the same text
 *       (no account enumeration); when the login exists, is active and has an email, a 6-digit
 *       code (SecureRandom) is mailed and only its salted SHA-256 is stored, valid 15 minutes.
 *       At most 3 requests per account per hour.</li>
 *   <li>{@code POST /api/auth/password-reset/confirm} {username, otp, new_password}: checks the
 *       newest unused code (5 attempts, reserved atomically) and sets the BCrypt password in the
 *       same call. ERPNext had a separate "verified" flag that reset_password trusted, so a
 *       brute-forced 4-digit code (unlimited tries) reset any account.</li>
 * </ul>
 * Both: 10 calls per IP per 10 minutes. Password rule: 8-72 characters with a letter and a digit.
 */
@RestController
@RequestMapping("/api/auth/password-reset")
public class PasswordResetController {

    private static final String GENERIC = "If the account exists and has an email address, a code has been sent to it.";
    private static final int TTL_MINUTES = 15, MAX_ATTEMPTS = 5, MAX_REQUESTS_PER_HOUR = 3;
    private static final int WINDOW_MS = 10 * 60 * 1000, MAX_PER_WINDOW = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;
    private final TenantMailService mail;
    private final Map<String, Deque<Long>> hits = new ConcurrentHashMap<>();

    public PasswordResetController(JdbcTemplate jdbc, PasswordEncoder passwordEncoder, TenantMailService mail) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
        this.mail = mail;
    }

    @PostMapping("/request")
    public ResponseEntity<Map<String, Object>> request(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        if (limited(req)) return ResponseEntity.status(429).body(Map.of("status", "error", "message", "Too many requests, try again later"));
        Map<String, Object> user = user(body);
        if (user == null || user.get("email") == null || user.get("email").toString().isBlank()) {
            return ResponseEntity.ok(Map.of("status", "success", "message", GENERIC));
        }
        long userId = ((Number) user.get("id")).longValue();
        Integer recent = jdbc.queryForObject("SELECT COUNT(*) FROM m0_password_reset WHERE user_id=? AND created_at > NOW() - INTERVAL 1 HOUR",
                Integer.class, userId);
        if (recent != null && recent >= MAX_REQUESTS_PER_HOUR) return ResponseEntity.ok(Map.of("status", "success", "message", GENERIC));

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        byte[] saltBytes = new byte[16];
        RANDOM.nextBytes(saltBytes);
        String salt = HexFormat.of().formatHex(saltBytes);
        Long tenantId = ((Number) user.get("tenant_id")).longValue();
        // The hash binds the row id, which only exists after the insert: insert with an unusable
        // placeholder hash (never equal to a hex SHA-256), then set the real one.
        Object[] values = {tenantId, userId, "-", salt, Timestamp.valueOf(LocalDateTime.now().plusMinutes(TTL_MINUTES)), req.getRemoteAddr()};
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO m0_password_reset (tenant_id, user_id, code_hash, code_salt, expires_at, requested_ip) VALUES (?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < values.length; i++) ps.setObject(i + 1, values[i]);
            return ps;
        }, keys);
        long id = keys.getKey().longValue();
        jdbc.update("UPDATE m0_password_reset SET code_hash=? WHERE id=?", hash(salt, code, id), id);

        String to = user.get("email").toString();
        String html = "<p>Dear " + HtmlUtils.htmlEscape(String.valueOf(user.get("employeename"))) + ",</p>"
                + "<p>Your password reset code is: <b style='font-size:18px'>" + code + "</b></p>"
                + "<p>It is valid for " + TTL_MINUTES + " minutes. If you did not ask for this, ignore this mail; your password is unchanged.</p>";
        // Off the request thread: an SMTP round-trip only for existing accounts would reveal them by timing.
        CompletableFuture.runAsync(() -> {
            TenantContext.set(tenantId);
            try {
                mail.sendNow(to, "Password Reset OTP", html, true);
            } finally {
                TenantContext.clear();
            }
        });
        return ResponseEntity.ok(Map.of("status", "success", "message", GENERIC));
    }

    @PostMapping("/confirm")
    public ResponseEntity<Map<String, Object>> confirm(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        if (limited(req)) return ResponseEntity.status(429).body(Map.of("status", "error", "message", "Too many requests, try again later"));
        String otp = body == null || body.get("otp") == null ? "" : body.get("otp").toString().trim();
        String password = body == null || body.get("new_password") == null ? "" : body.get("new_password").toString();
        if (password.length() < 8 || password.length() > 72 || !password.matches(".*[A-Za-z].*") || !password.matches(".*[0-9].*")) {
            return bad("Password must be 8-72 characters and contain a letter and a digit");
        }
        Map<String, Object> user = user(body);
        if (user == null) return bad("Invalid or expired code");
        long userId = ((Number) user.get("id")).longValue();
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id, code_hash, code_salt FROM m0_password_reset WHERE user_id=? AND used_at IS NULL AND expires_at > NOW() " +
                        "ORDER BY id DESC LIMIT 1", userId);
        if (rows.isEmpty()) return bad("Invalid or expired code");
        Map<String, Object> r = rows.get(0);
        long id = ((Number) r.get("id")).longValue();
        if (jdbc.update("UPDATE m0_password_reset SET attempts=attempts+1 WHERE id=? AND attempts < ?", id, MAX_ATTEMPTS) == 0) {
            return bad("Too many wrong attempts; request a new code");
        }
        boolean match = otp.matches("\\d{6}") && MessageDigest.isEqual(
                r.get("code_hash").toString().getBytes(StandardCharsets.US_ASCII),
                hash(r.get("code_salt").toString(), otp, id).getBytes(StandardCharsets.US_ASCII));
        if (!match) return bad("Invalid or expired code");
        if (jdbc.update("UPDATE m0_password_reset SET used_at=NOW() WHERE id=? AND used_at IS NULL", id) == 0) {
            return bad("Invalid or expired code");
        }
        jdbc.update("UPDATE employee_details SET password=? WHERE id=?", passwordEncoder.encode(password), userId);
        jdbc.update("UPDATE m0_password_reset SET used_at=NOW() WHERE user_id=? AND used_at IS NULL", userId);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Password updated. Please sign in with the new password."));
    }

    /** Active login by username (same rule as AuthUserDetailsService). */
    private Map<String, Object> user(Map<String, Object> body) {
        Object u = body == null ? null : body.get("username");
        if (u == null || u.toString().isBlank() || u.toString().length() > 255) return null;
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id, tenant_id, email, employeename FROM employee_details WHERE username=? AND (status='' OR status='A')",
                u.toString().trim());
        return rows.size() == 1 ? rows.get(0) : null;
    }

    static String hash(String salt, String code, long id) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest((salt + ":" + code + ":" + id).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean limited(HttpServletRequest req) {
        long now = System.currentTimeMillis();
        Deque<Long> q = hits.computeIfAbsent(req.getRemoteAddr(), k -> new ArrayDeque<>());
        synchronized (q) {
            while (!q.isEmpty() && now - q.peekFirst() > WINDOW_MS) q.pollFirst();
            if (q.size() >= MAX_PER_WINDOW) return true;
            q.addLast(now);
        }
        if (hits.size() > 10_000) hits.entrySet().removeIf(e -> e.getValue().isEmpty());
        return false;
    }

    private static ResponseEntity<Map<String, Object>> bad(String message) {
        return ResponseEntity.badRequest().body(Map.of("status", "error", "message", message));
    }
}
