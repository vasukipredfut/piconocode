package com.aria.integration.dao;

import com.aria.integration.model.IntegrationEventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface IntegrationEventLogRepository extends JpaRepository<IntegrationEventLog, Long> {

    List<IntegrationEventLog> findTop100ByInstanceIdOrderByTsDesc(Long instanceId);

    List<IntegrationEventLog> findByInstanceIdAndTsAfterOrderByTsDesc(Long instanceId, Instant since);
}
