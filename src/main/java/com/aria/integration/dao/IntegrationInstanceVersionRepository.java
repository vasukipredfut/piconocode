package com.aria.integration.dao;

import com.aria.integration.model.IntegrationInstanceVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntegrationInstanceVersionRepository
        extends JpaRepository<IntegrationInstanceVersion, Long> {

    List<IntegrationInstanceVersion> findByInstanceIdOrderByVersionDesc(Long instanceId);

    Optional<IntegrationInstanceVersion> findByInstanceIdAndVersion(Long instanceId, Integer version);
}
