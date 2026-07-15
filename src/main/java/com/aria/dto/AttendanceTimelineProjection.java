package com.aria.dto;

import java.time.Duration;
import java.time.LocalDateTime;

public interface AttendanceTimelineProjection {

    Long getId();

    LocalDateTime getShiftdate();

    LocalDateTime getIntime();

    LocalDateTime getOuttime();

    String getTotalWorkHours();
    
    String getReason();

    Long getBioid();

    String getEmployeename();

    String getFingerprintstation();

    String getShift();
}
