package com.aria.dao;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aria.model.ExceptionLog;

@Repository
public interface ExceptionLogRepository extends JpaRepository<ExceptionLog,Long> {

	 	@Modifying
	    @Transactional
	    @Query("DELETE FROM ExceptionLog e WHERE e.timestamp < :cutoffDate")
	    int deleteByTimestampBefore(LocalDateTime cutoffDate);

}
