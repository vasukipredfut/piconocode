package com.aria.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface EmployeeDailyLogList {
	
	String getFingerid();          // i.enrollnumber
    LocalDate getDate();           // i.date
    LocalDateTime getDateandtime();// i.attdatetime
    Long getLoggedid();            // i.slno
    String getFingermechineid();   // i.sysip
    String getLocation();          // b.location

}
