package com.aria.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.aria.dao.ExceptionLogRepository;
import com.aria.model.ExceptionLog;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.crypto.SecretKey;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ResponseLoggingInterceptor implements HandlerInterceptor {

    @Autowired
    private ExceptionLogRepository exceptionLogRepository;
    
    @Value("${jwt.secret}")
    private String jwtSecret;


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        int statusCode = response.getStatus();

        if (statusCode != 200) {
            ExceptionLog log = new ExceptionLog();
            log.setHttpStatus(statusCode);
            log.setPath(request.getRequestURI());
            log.setMethod(request.getMethod());
            log.setIpAddress(request.getRemoteAddr());
            log.setExceptionMessage(ex != null ? ex.getMessage() : extractResponseBody(response)); // Log exception or response body

            // Extract username from token if available
            String username = extractUsernameFromToken(request);
            log.setUsername(username);

            // Save to database
            exceptionLogRepository.save(log);
        }
    }

    
    private String extractResponseBody(HttpServletResponse response) {
        if (response instanceof org.springframework.web.util.ContentCachingResponseWrapper) {
            ContentCachingResponseWrapper wrapper = (ContentCachingResponseWrapper) response;
            try {
                // Get the cached content as a string
                return new String(wrapper.getContentAsByteArray(), wrapper.getCharacterEncoding());
            } catch (Exception e) {
                return "Failed to extract response body: " + e.getMessage();
            }
        }
        return "Response body not available.";
    }

    
    private SecretKey signingKey() {
        byte[] raw = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (raw.length >= 64) {
            return Keys.hmacShaKeyFor(raw);
        }
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-512");
            return Keys.hmacShaKeyFor(sha.digest(raw));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to derive HS512 signing key", e);
        }
    }

    private String extractUsernameFromToken(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            String token = headerAuth.substring(7);
            try {
                return Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(token).getPayload().getSubject();
            } catch (ExpiredJwtException e) {
                return e.getClaims().getSubject();
            }catch (Exception e) {

            }
        }
        return null; // Return null if username is unavailable
    }
}
