package com.aria.dao;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import com.aria.model.UserActivity;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity,Long> {
	
	@Modifying
    @Transactional
    @Query("DELETE FROM UserActivity u WHERE u.activityTime < :cutoffDate")
    int deleteByActivityTimeBefore(LocalDateTime cutoffDate);

}
