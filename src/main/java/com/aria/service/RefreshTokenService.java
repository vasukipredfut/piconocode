package com.aria.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aria.config.JwtUtils;
import com.aria.dao.RefreshTokenRepository;
import com.aria.dao.UserRepository;
import com.aria.model.RefreshToken;
import com.aria.utils.TokenRefreshException;

@Service
public class RefreshTokenService {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);

    @Value("${jwt.RefreshExpirationMs}")
    private Long refreshTokenDurationMs;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createRefreshToken(Long userId) {

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(userRepository.findById(userId).get());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            // Log the expiration details
            logger.warn("Refresh token expired: {}. Expiry date: {}", token.getToken(), token.getExpiryDate());

            try {
                // first try to remove via custom query (safer than delete on a detached entity)
                int removed = refreshTokenRepository.deleteByToken(token.getToken());
                if (removed > 0) {
                    logger.info("Expired refresh token deleted successfully: {}", token.getToken());
                } else {
                    // if nothing was removed it may have already been deleted, log and continue
                    logger.info("Refresh token {} was already removed (or never existed)", token.getToken());
                }
            } catch (org.springframework.dao.DataAccessException e) {
                // Log if deletion fails, but do not rethrow – expiration should still result in exception
                logger.error("Failed to delete expired refresh token: {}. Error: {}", token.getToken(), e.getMessage(), e);
            }

            // Throw exception for expired token
            throw new TokenRefreshException(token.getToken(),
                    String.format("Refresh token expired at %s. Please make a new signin request.", token.getExpiryDate()));
        }
        return token;
    }

    public void delete(Long userId) {
        try {
            int removed = refreshTokenRepository.deleteByUserId(userId);
            logger.debug("Deleted {} refresh tokens for user {}", removed, userId);
        } catch (org.springframework.dao.DataAccessException e) {
            // Log but do not propagate - logout should still succeed even if cleanup
            logger.error("Error deleting refresh tokens for user {}: {}", userId, e.getMessage(), e);
        }
    }


}
