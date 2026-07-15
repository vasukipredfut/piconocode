package com.aria.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface AttendanceTimelineList {

	LocalDate getCreateddate();

    String getLocation();
    
    String getIpaddress();

    String getIntime();

    String getOuttime();
    
    String getTotalworkingdays();

    String getEmployeename();

    String getReason();
    
    String getShiftname();
    LocalDateTime getCheckIn();
    LocalDateTime getCheckOut();
    
}
