package com.aria.model;

import java.time.LocalDateTime;
import java.util.Date;


public interface ListShiftRotation {

	String getCategory();
	String getEmployeeName();

	Long getEmpId();
	
	String getDepartmentName();
	
	Date getFromDate();
	Date getToDate();

	Long getRotationId();
	
	int getEmpType();
	LocalDateTime getCreatedDate();

}