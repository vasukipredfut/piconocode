package com.aria.m11.dao;

import com.aria.m11.model.IntegrationInboundToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntegrationInboundTokenRepository
        extends JpaRepository<IntegrationInboundToken, Long> {

    Optional<IntegrationInboundToken> findByTokenHash(String tokenHash);

    List<IntegrationInboundToken> findByInstanceIdOrderByCreatedAtDesc(Long instanceId);
}
