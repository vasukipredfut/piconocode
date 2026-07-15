package com.aria.model;

import java.util.Date;

public interface ITrainerTrainingHistory {

	Long getTrainerDetailId();

	Long getDepartmentId();

	String getDepartmentName();

	Long getDesignationId();

	String getDesignationName();

	Long getSkillId();

	String getSkillName();

	Date getTrainingDate();

	Double getAverageScore();
}
