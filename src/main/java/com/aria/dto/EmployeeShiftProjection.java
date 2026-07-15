package com.aria.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;


public interface EmployeeShiftProjection {
	
	 String getEmployeeName();

	    LocalDate getShiftDate();

	    String getShiftName();
	    
	    LocalDateTime getIntime();
	    
	    LocalDateTime getOuttime();
	    
	    Long getEmpId();
	    Long getBioId();
	    String getStartTime();
	    String getEndTime();
	    
}
