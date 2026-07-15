package com.aria.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.aria.dao.ExceptionLogRepository;
import com.aria.dao.UserActivityRepository;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;

@Service
public class CleanupService {
	
    private static final Logger logger = LoggerFactory.getLogger(CleanupService.class);


    @Autowired
    private UserActivityRepository userActivityRepository;

    @Autowired
    private ExceptionLogRepository exceptionLogRepository;

    @Scheduled(cron = "0 0 0 * * *") // Runs daily at midnight
    @Transactional
    public void deleteOldRecords() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(20);

        // Delete records older than 20 days
        int userActivityDeleted = userActivityRepository.deleteByActivityTimeBefore(cutoffDate);
        int exceptionLogDeleted = exceptionLogRepository.deleteByTimestampBefore(cutoffDate);

        // Log the result
        logger.error("Deleted {} records from user_activity table.", userActivityDeleted);
        logger.error("Deleted {} records from exception_log table.", exceptionLogDeleted);
    }
}
