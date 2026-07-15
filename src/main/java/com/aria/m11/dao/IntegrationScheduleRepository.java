package com.aria.m11.dao;

import com.aria.m11.model.IntegrationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntegrationScheduleRepository extends JpaRepository<IntegrationSchedule, Long> {

    List<IntegrationSchedule> findByInstanceIdOrderByCreatedAtDesc(Long instanceId);
}
