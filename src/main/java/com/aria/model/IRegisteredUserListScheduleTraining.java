package com.aria.model;

import java.util.Date;

public interface IRegisteredUserListScheduleTraining {

	Long getEmployeeId();

	String getEmployeename();

	Long getDepartmentId();

	String getDepartmentName();

	Long getDesignationId();

	String getDesignationName();

	Long getScheduleTrainingId();

	Date getRegistrationDate();
}
