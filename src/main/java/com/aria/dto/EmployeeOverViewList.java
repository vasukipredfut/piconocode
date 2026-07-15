package com.aria.dto;

import java.time.LocalDate;

public interface EmployeeOverViewList {
	
	Long getId();

    LocalDate getAttendancedate();

    String getIntime();
    
    String getOuttime();
    
    String getTotalworkingdays();

    String getReason();

}
