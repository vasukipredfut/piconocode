package com.aria.model;

import java.util.Date;

public interface IScheduleTrainingFilterRegistration {

	Long getId();

//	SkillDetails getSkillDetails();

	Long getSkillId();

	String getSkillName();

//	TrainingType getTrainingType();

	Long getTrainingTypeId();

	String getTrainingTypeName();

	Date getTrainingDate();

	String getConductedBy();

	Date getLastRegistrationDate();

	Integer getSeatsLeft();
}
