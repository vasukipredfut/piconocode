package com.aria.integration.dao;

import com.aria.integration.model.IntegrationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntegrationScheduleRepository extends JpaRepository<IntegrationSchedule, Long> {

    List<IntegrationSchedule> findByInstanceIdOrderByCreatedAtDesc(Long instanceId);
}
