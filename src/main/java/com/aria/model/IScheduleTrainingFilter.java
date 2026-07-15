package com.aria.model;

import java.util.Date;

public interface IScheduleTrainingFilter {

	Long getId();

	String getTrainingTypeName();

	Long getDepartmentId();

	String getDepartmentName();

	Long getDesignationId();

	String getDesignationName();

	Long getSkillId();

	String getSkillName();

	Integer getTotalSeats();

	Integer getTotalRegisteredEmployees();

	String getStatus();

	Date getTrainingDate();

	Date getLastRegistrationDate();

}
