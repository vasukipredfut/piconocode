package com.aria.dao;

import com.aria.model.ProSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProScheduleRepository extends JpaRepository<ProSchedule,Long> {
}
