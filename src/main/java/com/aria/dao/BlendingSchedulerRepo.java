package com.aria.dao;

import com.aria.model.BlendingScheduler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlendingSchedulerRepo extends JpaRepository<BlendingScheduler,Long> {
}
