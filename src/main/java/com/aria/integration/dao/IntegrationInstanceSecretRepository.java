package com.aria.integration.dao;

import com.aria.integration.model.IntegrationInstanceSecret;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntegrationInstanceSecretRepository
        extends JpaRepository<IntegrationInstanceSecret, Long> {

    List<IntegrationInstanceSecret> findByInstanceIdOrderByCreatedAtDesc(Long instanceId);

    Optional<IntegrationInstanceSecret> findFirstByInstanceIdOrderByCreatedAtDesc(Long instanceId);

    void deleteByInstanceId(Long instanceId);
}
