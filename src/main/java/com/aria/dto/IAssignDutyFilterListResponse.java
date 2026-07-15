package com.aria.dto;

import java.util.Date;

public interface IAssignDutyFilterListResponse {

	 Long getAllotmentId();
	 String getEmployeeName();
	 Long getEmployeeId();
	 String getAssignedShift();
	 Date getAssignedDate();
	 String getAssignedDuty();
	 String getCurrentDuty();
	 String getStatus();
}
